/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/RangeWindow.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */
/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 udeshmuk    07/24/13 - bug 16966411
 sbishnoi    10/01/11 - XbranchMerge sbishnoi_bug-12720971_ps5 from
                        st_pcbpel_11.1.1.4.0
 sbishnoi    09/13/11 - fix bug 12720971
 udeshmuk    04/22/09 - don't bypass for slide and extensible window
 udeshmuk    04/13/09 - set flag to false always for slide and extensible
                        window processing
 sbishnoi    04/10/09 - adding minNextTs
 sbishnoi    04/09/09 - initializing timestamp varible to
                        Constant.MIN_EXEC_TIME
 udeshmuk    01/16/09 - total ordering optimization.
 hopark      10/10/08 - remove statics
 hopark      10/09/08 - remove statics
 sbishnoi    09/23/08 - 
 sborah      09/22/08 - check for queue full before enqueue
 hopark      01/31/08 - queue optimization
 hopark      12/06/07 - cleanup spill
 hopark      10/30/07 - remove IQueueElement
 hopark      10/21/07 - remove TimeStamp
 parujain    10/04/07 - delete op
 hopark      07/13/07 - dump stack trace on exception
 parujain    07/03/07 - cleanup
 parujain    06/26/07 - mutable state
 hopark      06/19/07 - cleanup
 hopark      05/24/07 - debug logging
 hopark      05/16/07 - add arguments for OutOfOrderException
 parujain    05/08/07 - monitoring statistics
 hopark      04/19/07 - fix refcount
 hopark      04/06/07 - fix pincount
 hopark      03/24/07 - add unpin
 najain      03/14/07 - cleanup
 parujain    03/23/07 - cleanup
 parujain    03/08/07 - Extensible Window support
 najain      03/12/07 - bug fix
 najain      01/05/07 - spill over support
 parujain    02/27/07 - NPE bug
 hopark      12/29/06 - make getVisibleTs public
 parujain    12/11/06 - propagating relations
 najain      12/04/06 - store independent of allocator
 najain      11/01/06 - manager timestamps
 najain      09/25/06 - static relns with slide
 najain      08/02/06 - refCounting optimizations
 najain      08/10/06 - add asserts
 rkomurav    08/09/06 - slide and cleanup
 rkomurav    08/08/06 - remove unused import
 rkomurav    08/08/06 - bug 5405722
 najain      07/19/06 - ref-count tuples 
 najain      07/13/06 - ref-count timestamps 
 najain      07/13/06 - ref-count timestamps 
 najain      07/13/06 - ref-count timeStamp support 
 najain      07/10/06 - move inStore to parent 
 najain      07/06/06 - cleanup
 najain      06/14/06 - query deletion support 
 najain      04/18/06 - time is a part of tuple 
 skaluska    04/05/06 - reuse timestamp for - elements also 
 skaluska    04/04/06 - add tsStorageAlloc 
 najain      03/31/06 - bug fixes 
 anasrini    03/24/06 - toString
 skaluska    03/20/06 - implementation
 skaluska    03/14/06 - query manager 
 anasrini    03/14/06 - make constructor public 
 najain      03/08/06 - store return StorageElement
 skaluska    02/06/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/RangeWindow.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.operators;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.windows.Window;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.execution.snapshot.SynopsisPersistenceContext;
import oracle.cep.execution.synopses.ExecSynopsis;
import oracle.cep.execution.synopses.WindowSynopsis;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.ExecContext;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.logging.Level;

/**
 * RangeWindow
 *
 * @author skaluska
 */
public class RangeWindow extends ExecOpt
{
  /** windowSpec */
  private Window   window;

  /** winSynopsis */
  @DumpDesc(ignore=true)
  private WindowSynopsis winSynopsis;
  
  /** buffered plus tuples */
  LinkedList<ITuplePtr>  plusBufferedOutTuples;
  
  /** buffered plus tuples */
  LinkedList<Long>  plusBufferedOutTuplesTs;
  
  /** flag to check if we have pending buffered elements to output */
  boolean hasBufferedElements;
  
  /** flag to check if the current output tuple should be buffered without
   * sending it to output queue */
  boolean shouldBeBuffered;
  
  /** a flag to check if we should scan the synopsis for MINUS tuples */
  boolean synopsisScanRequired;
  
  public RangeWindowJournalEntry journalEntry;

  /**
   * Constructor for RangeWindow
   * @param ec TODO
   */
  public RangeWindow(ExecContext ec)
  {
    super(ExecOptType.EXEC_RANGE_WIN, new RangeWindowState(ec), ec);
    plusBufferedOutTuples = new LinkedList<ITuplePtr>();   
    plusBufferedOutTuplesTs = new LinkedList<Long>();    
    hasBufferedElements = false;
    shouldBeBuffered    = false;
    synopsisScanRequired = true;
  }

 
  /**
   * Getter for winSynopsis in RangeWindow
   * 
   * @return Returns the winSynopsis
   */
  public WindowSynopsis getWinSynopsis()
  {
    return winSynopsis;
  }
  
  /**
   * Getter for Window Specifications
   * 
   * @return Returns the Window Specs
   */
  public Window getWindow()
  {
    return window;
  }
  
  /**
   * Setter for Window Specifications
   * 
   * @param win WindowSpec
   */
  public void setWindow(Window win)
  {
    this.window = win; 
  }

  /**
   * Setter for winSynopsis in RangeWindow
   * 
   * @param winSynopsis
   *          The winSynopsis to set.
   */
  public void setWinSynopsis(WindowSynopsis winSynopsis)
  {
    this.winSynopsis = winSynopsis;
  }


  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.operators.ExecOpt#run(int)
   */
  public int run(int timeSlice) throws ExecException
  {
    int numElements = timeSlice;
    boolean done = false;
    RangeWindowState s = (RangeWindowState) mut_state;
    boolean exitState = true;
    boolean heartBeatSent = false;
    assert s.state != ExecState.S_UNINIT;
   
    // Stats
    s.stats.incrNumExecutions();
    try
    {
      while ((s.stats.getNumInputs() < numElements) || (!exitState) )
      {        
        switch (s.state)
        {
          case S_PROPAGATE_OLD_DATA:
            setExecSynopsis((ExecSynopsis)winSynopsis);
            handlePropOldData();
            break;
                  
          case S_INIT:
            // Get next input element
            // Inputs: none
            // Outputs: inputElement
            
            s.inputElement = inputQueue.dequeue(s.inputElementBuf);
            s.state = ExecState.S_INPUT_DEQUEUED;
          case S_INPUT_DEQUEUED:
            // Sanity checks, initialize variables based on input element
            // Inputs: inputElement, lastInputTs
            // Outputs: inputKind, inputTs, inputTuple, lastInputTs
            if (s.inputElement == null)
            {
              //bug 16966411: Make sure that rangewindow sends a heartbeat
              //of lastInputTs with flag true if one is received on the 
              //input. 
              //Consider the case when 1000 + tuple is followed by 1000 HB
              //that has flag true. Since lastInputTs == lastOutputTs we won't
              //be sending the hb in the existing setup whereas we should be
              //sending one.
              //Also, we want to send the heartbeat only once so use 
              //heartBeatSent flag to track it.
              if((s.lastInputTs > s.lastOutputTs)
                 || ((s.lastInputTs == s.lastOutputTs) &&
                     (s.lastInputOrderingFlag) && 
                     (s.inputKind == QueueElement.Kind.E_HEARTBEAT) &&
                     (!heartBeatSent)&& (! s.expiredTupleOrderingFlag))
                     )
              {
                // Output a heartbeat
                s.state = ExecState.S_GENERATE_HEARTBEAT;
                exitState = false;
                break;
              }
              
              s.state = ExecState.S_INIT;
              done = true;
              break;
              
            }
            else
            {
              exitState = false;
              // Bump our counts
              if(s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
                s.stats.incrNumInputHeartbeats();
              else
                s.stats.incrNumInputs();              
     
              // Get input tuple and its other attribute values from
              // the queue element
              s.inputTuple = s.inputElement.getTuple();
              s.inputKind  = s.inputElement.getKind();
              s.inputTs    = s.inputElement.getTs();               
              // We should not get MINUS tuples in the input
              assert s.inputKind != QueueElement.Kind.E_MINUS : s.inputElement;
              
              // Input should be timestamp ordered
              if (s.lastInputTs > s.inputTs)
              {
                s.state = ExecState.S_INIT;
                throw ExecException.OutOfOrderException(
                        this,
                        s.lastInputTs, 
                        s.inputTs, 
                        s.inputElement.toString());
              }
              
              // Input should follow total ordering flag
              assert s.inputTs >= s.minNextTs :
                getDebugInfo(s.inputTs, s.minNextTs, 
                             s.inputElement.getKind().name(),
                             s.lastInputKind.name());
              
              // Calculate visible timestamp
              s.tempTs.setTime(s.inputTs);
              window.visibleW(s.tempTs, s.visTs);             
              
              // Note: If slide > 1, then Output will be sent at a timestamp
              // multiple of slide value.
              // If input element's timestamp is not a multiple of slide value
              // then we will buffer it into a temporary collection.
              // In this section, As we have received a new input element, we 
              // will check whether we can send the buffered batch as output
              // before processing the current input element.
              
              if(hasBufferedElements)
              {                
                if(s.inputTs > s.lastInputTs)
                {              
                  // If buffered element's projected output timestamp is less
                  // than or equal to current input timestamp, then we can 
                  // output the buffered set.
                  if(s.batchEndTime <= s.inputTs)
                  {                    
                    outputPlusBufferedTuples(s);
                    hasBufferedElements = false;
                  }                  
                } 
              }   
              // Update RangeWindowState variables
              s.lastInputTs = s.inputTs;
              s.lastInputOrderingFlag
                = s.inputElement.getTotalOrderingGuarantee();       
              
              // Set minimum timestamp for next input tuple
              s.minNextTs = s.lastInputOrderingFlag ? s.inputTs+1 : s.inputTs;              
            }
            
            s.state = ExecState.S_PROCESSING1;
          case S_PROCESSING1:
            // Increment ref count of input tuple
            // Inputs: inputTuple
            // Outputs: inputTuple

            assert s.inputElement != null;

            if (s.inputKind == QueueElement.Kind.E_PLUS)
            {
              assert s.inputTuple != null;
            }
            
            // This is a new input tuple which may expire some tuples from 
            // synopsis; so set the flag to TRUE to scan the synopsis
            synopsisScanRequired = true;
            s.state = ExecState.S_PROCESSING2;
            
          case S_PROCESSING2:
            // Determine tuple to be expired
            // Inputs: inputKind, inputTs
            // Outputs: expiredTuple           
            s.tempTs.setTime(s.inputTs);
            
            if (window.expiredW(s.tempTs, s.expTs) && synopsisScanRequired)
            {
              if (winSynopsis.isEmpty())
                s.expiredTuple = null;
              else
              {
                // Get the oldest timeStamp
                long oldestTs = winSynopsis.getOldestTimeStamp();

                // Return the oldest tuple if its expiry time <= current visible time
                s.tempTs.setTime(oldestTs);
                window.expiredW(s.tempTs, s.expTs);
                
                if (s.expTs.getTime() > s.inputTs)
                  s.expiredTuple = null;
                else
                { 
                  s.expiredTuple = winSynopsis.getOldestTuple();
                  s.expiredTimeStamp = s.expTs.getTime();
                  /* Determine the orderingFlag for the tuple being expired */
                  if(s.expTs.getTime() < s.visTs.getTime()) //syn. tuple ts + range < input tuple ts
                  {
                    if(winSynopsis.compareConsecutiveTuples(window))
                      s.expiredTupleOrderingFlag = true; //next oldest ts > curr oldest ts
                    else 
                      s.expiredTupleOrderingFlag = false; // next oldest ts == curr oldest ts OR next = null
                  }
                  else // s.expTs.getTime() == s.visTs.getTime()
                  {
                    if(winSynopsis.compareConsecutiveTuples(window))
                    {
                      if(s.inputKind == QueueElement.Kind.E_HEARTBEAT) //current input is hb.
                      { 
                        if(s.inputTs == s.visTs.getTime()) 
                          s.expiredTupleOrderingFlag = s.inputElement.
                                                       getTotalOrderingGuarantee();
                        else //slide is greater than 1
                          /* Motivating example: tuples in window at actual ts 8000,9000 & 11000.
                           * Range is 10 and slide 5.
                           * So two plus tuples output at 10000 and they are set to expire at 20000.
                           * A plus tuple is output at 15000 and will expire at 25000.
                           * The tuple at actual ts 8000 will hv its flag set to false when output since 
                           * compareConsecutiveTuples will evaluate to false since visTs(8000) == visTs(9000). 
                           * However for 9000 tuple, since visTs(9000) < visTs(11000) the code will come in this branch.
                           * Assume we receive hb at actual ts 17000 with orderingFlag as true. Even though we don't 
                           * propagate hb immediately as any other normal tuple, here we cannot plainly
                           * copy the hb flag (TRUE) while outputing MINUS tuple for 9000 since there may a tuple at
                           * 18000 whose PLUS will be output at the same ts i.e. 20000.
                           * When slide is not greater than 1 such a plain copying of hb flag works correctly.
                           */
                          s.expiredTupleOrderingFlag = false;
                      }
                      else //current input is normal tuple, PLUS tuple will be sent after all MINUS tuples 
                        s.expiredTupleOrderingFlag = false;
                    }
                    else
                      s.expiredTupleOrderingFlag = false; // next oldest ts == curr oldest ts or next is null
                  } 
                }
              }
            }
            else
              s.expiredTuple = null;

            if (s.inputKind == QueueElement.Kind.E_HEARTBEAT)
            { 
              if (s.expiredTuple == null)
              {
                // Nothing more to be done              
                s.state = ExecState.S_INPUT_ELEM_CONSUMED;
                continue;
              }
              else
                s.inputTuple = null;
              
            }
            s.state = ExecState.S_OUTPUT_TIMESTAMP;
          case S_OUTPUT_TIMESTAMP:
            if (s.expiredTuple == null)
              s.outputTs = s.visTs.getTime();
            s.state = ExecState.S_OUTPUT_READY;
          case S_OUTPUT_READY:
            // Allocate queue element for output
            // Inputs: inputTuple, inputTs, expiredTuple
            // Outputs: outputElement
            assert ((s.inputTuple != null) || (s.expiredTuple != null));
            
            // At this place, either we will be processing expired tuple or
            // current input tuple.
            // Check if we should buffer the tuple or send it to output queue
            if(s.expiredTuple != null)
            {              
              s.tempTs.setTime(s.expiredTimeStamp);             
              window.visibleW(s.tempTs, s.tempVisTs);
              // check if we should send MINUS right now or wait; 
              // wait if shouldBeBuffered is TRUE
              shouldBeBuffered = s.tempVisTs.getTime() > s.inputTs;
              // Don't update the synopsis until MINUS is sent
              if(!shouldBeBuffered)
              {
                winSynopsis.deleteOldestTuple();
                if(journalEntry != null)
                  updateJournalEntry(s.expiredTuple, false);
              }
            }
            else if(s.inputTuple != null)
            {
              // check if we should send PLUS right now or wait; 
              // wait if shouldBeBuffered is TRUE
              shouldBeBuffered = s.visTs.getTime() > s.inputTs;
              if(!shouldBeBuffered)
              {
                // Add timestamp information to tuple pointer so that if
                // persisted, the timestamp information is not lost from synopsis
                s.inputTuple.setTimestamp(s.inputTs);
                winSynopsis.insertTuple(s.inputTuple, s.inputTs);
                if(journalEntry != null)
                  updateJournalEntry(s.inputTuple, true);
              }
            }
            
            // If the current element should be buffered, then mark the 
            // batch time for buffered tuples and insert into appropriate list
            if(shouldBeBuffered)
            {              
              if(s.expiredTuple != null)
              { 
                // Skip searching for expired tuples as the oldest tuple itself
                // cann't be expired as its visTs is greater than current input
                // timestamp; so wait for higher timestamp which can expire the
                // tuples
                synopsisScanRequired = false;
                s.state = ExecState.S_PROCESSING2;                
              }
              else if(s.inputTuple != null)
              {
                plusBufferedOutTuples.add(s.inputTuple); 
                plusBufferedOutTuplesTs.add(s.inputTs);
                s.batchEndTime = s.visTs.getTime();
                hasBufferedElements = true;
                s.state = ExecState.S_INPUT_ELEM_CONSUMED;
              }
              else
                assert false;            
              break;
            }

            if (s.expiredTuple == null)
              s.outputElement.copy(s.inputElement);
            s.state = ExecState.S_PROCESSING4;
          case S_PROCESSING4:
            if (s.expiredTuple != null)
            {
              s.outputElement.setKind(QueueElement.Kind.E_MINUS);
              s.outputTs = s.tempVisTs.getTime();
              s.outputElement.setTs(s.outputTs);
              s.outputElement.setTuple(s.expiredTuple);
              s.outputElement.setTotalOrderingGuarantee(s.expiredTupleOrderingFlag); 
              s.lastOutputTs = s.outputTs;
            }
            else
            {
              // Add input tuple to the window
              // Inputs: inputTuple
              // Outputs: inputTuple

              assert (s.inputKind == QueueElement.Kind.E_PLUS) : s.inputKind;
              
              s.outputElement.setKind(QueueElement.Kind.E_PLUS);
              s.outputElement.setTuple(s.inputTuple);
              s.outputElement.setTs(s.outputTs);
              if (s.visTs.getTime() > s.inputTs) //mainly due to slide > 1
              {            
                assert false;
              }
              else
              {
                //If input arrives at multiples of slide value copy the flag as is, else false
                /** Example:
                 *  range 10 slide 5
                 *  If input arrives at time 15 then vistTs will also be 15.
                 *  Here tuples that may expire at 15 are output first.
                 *  Now the current tuple that arrived at 15 is output. Here
                 *  we can copy the ordering flag from the input as is.
                 */          
                 s.outputElement.setTotalOrderingGuarantee(
                   s.inputElement.getTotalOrderingGuarantee()); 
              }
              s.lastOutputTs = s.outputTs;
            }
            s.state = ExecState.S_OUTPUT_ELEMENT;
          case S_OUTPUT_ELEMENT:
            // Enqueue output element
            // Inputs: outputElement
            // Outputs: none

            if (outputQueue.isFull())
            {
              done = true;
              break;
            }
            outputQueue.enqueue(s.outputElement);
            
            s.state = ExecState.S_OUTPUT_ENQUEUED;
          case S_OUTPUT_ENQUEUED:
            // Determine next state
            // Inputs: outputElement, expiredTuple
            // Outputs: none
            if(s.outputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
              s.stats.incrNumOutputHeartbeats();
            else
              s.stats.incrNumOutputs();

            if (s.expiredTuple != null)
            {
              // It is now safe to delete the oldest tuple
              //winSynopsis.deleteOldestTuple();
              // Look for more expired elements
              s.state = ExecState.S_PROCESSING2;
              break;
            }
            else
            {
              assert s.inputTuple != null;
              // The plus corresponding to the input tuple is output last
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            }
          case S_INPUT_ELEM_CONSUMED:
            assert s.inputElement != null;
            assert s.expiredTuple == null;

            s.state = ExecState.S_INIT;                        
            break;

          case S_GENERATE_HEARTBEAT:
            s.state = ExecState.S_PROCESSING5;

          case S_PROCESSING5:
            s.lastOutputTs = s.lastInputTs;
            s.outputElement.heartBeat(s.lastInputTs);
            s.outputElement.setTotalOrderingGuarantee(s.lastInputOrderingFlag); 
            s.state = ExecState.S_PROCESSING6;

          case S_PROCESSING6:
            if (outputQueue.isFull())
            {
              done = true;
              break;
            }
            outputQueue.enqueue(s.outputElement);
            
            s.stats.incrNumOutputHeartbeats();
            heartBeatSent = true;
            exitState = true;
            s.state = ExecState.S_INIT;
            break;
          default:
            assert false;
        }
        if (done)
          break;
      }      
    }
    catch (SoftExecException e)
    {
      // Ignore it for now
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      return 0;
    } 
    
    return 0;
  }
  
  public void outputPlusBufferedTuples(RangeWindowState s) 
    throws ExecException
  {
    long pendingPlusTuples = 0;
    long batchOutputTime = s.batchEndTime;   
    
    if(plusBufferedOutTuples != null)
    {
      pendingPlusTuples = plusBufferedOutTuples.size();
    }
    
    if(pendingPlusTuples > 0)
    {
      for(ITuplePtr tuple : plusBufferedOutTuples)
      {
        pendingPlusTuples--;
        long tupleInpTs = plusBufferedOutTuplesTs.remove();
        // Add timestamp information to tuple pointer so that
        // if persisted, the timestamp information is not lost from synopsis
        tuple.setTimestamp(tupleInpTs);
        winSynopsis.insertTuple(tuple, tupleInpTs);
        
        // Update journal entry to add newly created insert tuple
        if(journalEntry != null)
          updateJournalEntry(tuple, true);
        
        s.outputElement.setTuple(tuple);
        s.outputElement.setKind(QueueElement.Kind.E_PLUS);
        s.outputElement.setTs(batchOutputTime);
        s.lastOutputTs = s.outputTs;
        // Note: Given input may expire some tuples from synopsis which can't 
        // be determined here. so we are setting flag to FALSE
        s.outputElement.setTotalOrderingGuarantee(false);
        outputQueue.enqueue(s.outputElement);        
        s.stats.incrNumOutputs();
      }
      plusBufferedOutTuples.clear();
    }   
  }


  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.operators.ExecOpt#deleteOp()
   */
  @Override
  public void deleteOp()
  {
    // TODO Auto-generated method stub

  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<InputQueue>" + inputQueue.toString() + "</InputQueue>");
    sb.append("<OutputQueue>" + outputQueue.toString() + "</OutputQueue>");
    sb.append("<Synopsis>" + winSynopsis.toString() + "</Synopsis>");
    sb.append("</RangeWindow>");

    return sb.toString();
  }
  
  /**
   * Create snapshot of Range Window operator by writing the operator state
   * into param java output stream.
   * State of Range Window operator consists of following:
   * 1. Operator variables
   * 2. Mutable State
   * 3. Window Synopsis 
   * 
   * Please note that we will write the state of operator in above sequence, so
   * the loadSnapshot should also read the state in the same sequence.
   * @param output
   * @throws IOException 
   */
  protected void createSnapshot(ObjectOutputStream output) throws CEPException
  { 
    
    try
    {
      // Instance variables in RangeWindow operator
      output.writeObject(plusBufferedOutTuples);
      output.writeObject(plusBufferedOutTuplesTs);
      output.writeBoolean(this.hasBufferedElements);
      output.writeBoolean(this.shouldBeBuffered);
      output.writeBoolean(this.synopsisScanRequired);
      
      // Write RangeWindowState into output buffer
      output.writeObject((RangeWindowState)mut_state);
      
      // Write WindowSynopsis into output buffer
      IPersistenceContext synopsisPersistenceCtx = new SynopsisPersistenceContext();
      synopsisPersistenceCtx.setScanId(propScanId);
      winSynopsis.writeExternal(output, synopsisPersistenceCtx);
    } 
    catch (IOException e)
    {
      LogUtil.logStackTrace(e);
      throw new CEPException(ExecutionError.SNAPSHOT_CREATION_ERROR, e, e.getLocalizedMessage(), this.getOptName());
    }
    
  }
  
  /** Load full snapshot from given input stream. */
@SuppressWarnings("unchecked")
protected void loadSnapshot(ObjectInputStream input) throws ExecException
  {
    try
    {
      // Instance variables of RangeWindow operator
      this.plusBufferedOutTuples = (LinkedList<ITuplePtr>) input.readObject();
      this.plusBufferedOutTuplesTs = (LinkedList<Long>) input.readObject();
      this.hasBufferedElements = input.readBoolean();
      this.shouldBeBuffered = input.readBoolean();
      this.synopsisScanRequired = input.readBoolean();
      
      // Read RangeWindowState from input buffer
      RangeWindowState loaded_mutable_state = (RangeWindowState) input.readObject();
      ((RangeWindowState)mut_state).copyFrom(loaded_mutable_state);
      
      winSynopsis.readExternal(input, new SynopsisPersistenceContext());      
    } 
    catch (ClassNotFoundException e)
    {
      LogUtil.logStackTrace(e);
      throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR_CNF, e, this.getOptName(), e.getLocalizedMessage());
    } 
    catch (IOException e)
    {
      LogUtil.logStackTrace(e);
      throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR, e, e.getLocalizedMessage(), getOptName());
    }
    
  }
  
  /** Apply Journal snapshot to Window */
  @Override
  protected void applySnapshot(Object journalEntry) throws ExecException
  {
    if(journalEntry instanceof RangeWindowJournalEntry)
      loadJournalEntry(((RangeWindowJournalEntry)journalEntry));
    else
      throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR_CNF, this.getOptName(), journalEntry.getClass().getName());
  }
  
  @Override
  public boolean usesJournaling()
  {
    return true;
  }
  
  @Override
  public void startBatch(boolean fullSnapshot) throws CEPException
  {
    super.startBatch(fullSnapshot);
    // In case of full snapshot, we don't need to instantiate journal entry
    if(!fullSnapshot)
      journalEntry = new RangeWindowJournalEntry();
  }
  
  @Override
  public void endBatch() throws CEPException
  {
    if(journalStream != null)
    {
      //Update Journal Entry
      updateJournalEntry();
    
      // Write journal entry for this batch to journal stream
      super.writeToJournal(journalEntry);
      LogUtil.fine(LoggerType.TRACE, "Operator:" + this.getOptName() 
          + ", JournalEntry:" + journalEntry);
    }
    // Invoke endBatch to 
    super.endBatch();
  }
  
  public void loadJournalEntry(RangeWindowJournalEntry entry) throws ExecException
  {
    // Load instance variables
    plusBufferedOutTuples = entry.getPlusBufferedOutTuples();
    plusBufferedOutTuplesTs = entry.getPlusBufferedOutTuplesTs();
    hasBufferedElements = entry.isHasBufferedElements();
    shouldBeBuffered = entry.isShouldBeBuffered();
    synopsisScanRequired = entry.isSynopsisScanRequired();
    
    // Load mutable state
    RangeWindowState opState = ((RangeWindowState)mut_state);
    opState.copyFrom(entry.getMutable_state());
        
    int added = 0;
    int removed = 0;
    
    // Load Window Synopsis 
    if(entry.getPlusChangeEvents() != null)
    {
      long ts = Long.MIN_VALUE;
      Iterator<Entry<Long, ITuplePtr>> iter = entry.getPlusChangeEvents().entrySet().iterator();
      while(iter.hasNext())
      {
        Entry<Long,ITuplePtr> next = iter.next();
        ITuplePtr tup = next.getValue();
        ts = tup.getTimestamp();
        winSynopsis.insertTuple(tup, ts);    
        added++;
      }
      
      // Delete all those tuples from synopsis which are out of range      
      boolean done = false;
      while(!done)
      {
        long oldestTs = winSynopsis.getOldestTimeStamp();       
        opState.tempTs.setTime(oldestTs);
        window.expiredW(opState.tempTs, opState.expTs);
        done = opState.expTs.getTime() > ts;
        if(!done)
        {          
          winSynopsis.deleteOldestTuple();
          removed++;
        }
      }        
    }
    
    // Log relevant information about snapshot summary
    LogUtil.fine(LoggerType.TRACE, "Loaded Snapshot in Range Window Operator " + this.getOptName() +
      ". Window Synopsis Change Events[inserted: " + added + ", removed:" + removed + "]");
  }
  
  /**
   * Update journal entry using two kinds of tuples:
   * 1) For newly added tuples in Window, Add the tuple in journal entry
   *    of this batch
   * 2) For expired tuple from window, Check if they were added in same 
   *    batch. If it is same batch, remove it from journal entry of
   *    this batch. Else ignore them and it will be handled while
   *    loading snapshot
   * @param tuple
   * @param isAddition
   */
  public void updateJournalEntry(ITuplePtr tuple, boolean isAddition)
  {
    if(isAddition)
      journalEntry.getPlusChangeEvents().put(tuple.getId(), tuple);
    else
      journalEntry.getPlusChangeEvents().remove(tuple.getId());
  }
  
  /**
   * Update journal entry with various state variables and mutable state
   */
  public void updateJournalEntry()
  {
    journalEntry.setHasBufferedElements(hasBufferedElements);
    journalEntry.setShouldBeBuffered(shouldBeBuffered);
    journalEntry.setSynopsisScanRequired(synopsisScanRequired);
    journalEntry.setPlusBufferedOutTuples(plusBufferedOutTuples);
    journalEntry.setPlusBufferedOutTuplesTs(plusBufferedOutTuplesTs);
    journalEntry.setMutable_state((RangeWindowState)mut_state);
  }
  
  
}
