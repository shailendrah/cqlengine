/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ValueRelationWindow.java /main/18 2013/10/08 10:15:01 udeshmuk Exp $ */

/* Copyright (c) 2011, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    08/14/13 - set snapshotid as current snapshotid for archiver
                           records
    udeshmuk    07/11/13 - fix loggin related to archived relation framework
    udeshmuk    09/09/12 - propagate snapshotid and handle event id
    sbishnoi    08/19/12 - bug 14502856
    sbishnoi    08/13/12 - bug 14206838
    udeshmuk    06/22/12 - add logging for the case when tuple is not visible
    udeshmuk    05/27/12 - propagate snapshotid and archived flag
    udeshmuk    02/10/12 - send heartbeat after snapshot
    sbishnoi    01/13/12 - modified timestamp unit to nanoseconds
    sbishnoi    10/17/11 - fix bug
    sbishnoi    09/24/11 - support for slide in value relation window
    sbishnoi    09/08/11 - support for current hour and current period
    sbishnoi    09/07/11 - support for curernt hour and current period value
                           window
    sbishnoi    02/27/11 - Creation
 */

package oracle.cep.execution.operators;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.logging.Level;

import oracle.cep.common.Datatype;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.synopses.ExecSynopsis;
import oracle.cep.execution.synopses.LineageSynopsis;
import oracle.cep.execution.queues.ISharedQueueWriter;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.service.ExecContext;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ValueRelationWindow.java /main/18 2013/10/08 10:15:01 udeshmuk Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class ValueRelationWindow extends ExecOpt
{
  /** evaluation context */
  private IEvalContext   evalContext;  
  
  /** position */
  private int            position;
  
  /** A flag to check if window is defined on element_time pseudo column*/
  private boolean        isWindowOnElementTime;

  /** winSynopsis */
  private LineageSynopsis outSynopsis;
  
  /** final queue will contain top n elements*/ 
  private PriorityQueue<ITuplePtr> valueWindowElements;
  
  /** A flag to track if the timestamp has progressed or not on the new input*/
  private boolean isTimeProgressed;
  
  /** iterator over output synopsis */
  private TupleIterator            outSynIter;
  
  /** eval to copy attribute value from outputTuple to nextOutputTuple */
  private IAEval                   outEval;
  
  /** eval to copy all but last attribute value from source to target tuple*/
  private IAEval                   copyEval;
  
  /** value window */
  private oracle.cep.execution.internals.windows.ValueWindow    window;
  
  /** datatype of the column whose value will be compared */
  private Datatype colType;
  
  /** a batch of plus output tuples which will be sent to output when window
   * will move by slide size*/
  private LinkedList<ITuplePtr> plusBufferedOutputTuples;
  
  /** output timestamp of buffered plus tuples */
  private LinkedList<Long>      plusBufferedOutputTuplesTs;
  
  /** a batch of minus output tuples which will be sent to output when window
   * will move by slide size*/
  private LinkedList<ITuplePtr> minusBufferedOutputTuples;
  
  /** output timestamp of buffered minus tuples */
  private LinkedList<Long>      minusBufferedOutputTuplesTs;
  
  /** a flag to check if we have pending buffered output tuples */
  private boolean hasBufferedElements;
  
  /** minimum timestamp value of all buffered tuples */
  private long bufferedTuplesMinTs;
  
  /** list of pending input tuples which will be included in value window */
  private PriorityQueue<ITuplePtr> pendingInputTuples;

  private boolean oldDataPropNeeded = true;
  
  /** A flag to check whether the current heartbeat input needs to propagate
   *  to downstream operators*/
  private boolean requiresHbtPropagation = false;
  
  /**
   * Constructor
   * @param ec TODO
   */
  public ValueRelationWindow(ExecContext ec)
  {
    super(ExecOptType.EXEC_VALUE_WIN, new ValueRelationWindowState(ec), ec);    
    plusBufferedOutputTuples    = new LinkedList<ITuplePtr>();
    minusBufferedOutputTuples   = new LinkedList<ITuplePtr>();
    plusBufferedOutputTuplesTs  = new LinkedList<Long>();
    minusBufferedOutputTuplesTs = new LinkedList<Long>();
    hasBufferedElements         = false;    
    bufferedTuplesMinTs         = Long.MAX_VALUE;
    requiresHbtPropagation      = false;
  }
  
  

  
  @Override
  public void deleteOp()
  {}

  @Override
  protected int run(int timeslice) throws CEPException
  {    
    ValueRelationWindowState s = (ValueRelationWindowState)mut_state;
    
    assert s.state != ExecState.S_UNINIT;

    // Update Stats
    s.stats.incrNumExecutions();
    
    if (s.state == ExecState.S_PROPAGATE_OLD_DATA)
    {
      if(oldDataPropNeeded)
      {
        setExecSynopsis((ExecSynopsis)outSynopsis);
        handlePropOldData();
      }
      else
        oldDataPropNeeded = true;
    } 

    try
    {
      while ((s.stats.getNumInputs() < timeslice))
      {
        // read an input element 
        s.inputElement = inputQueue.dequeue(s.inputElementBuf);
        
        s.inputTuple = null;
            
        // If input queue is empty, return
        if (s.inputElement == null)
        {
          break;        
        }

        // Update input stats
        if(s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
          s.stats.incrNumInputHeartbeats();
        else
          s.stats.incrNumInputs();
        
        // Get the input tuple from queue element
        ITuplePtr inpTuple = s.inputElement.getTuple();
        
        // If window is defined on ELEMENT_TIME pseudo column, then operator
        // will add a new column in the tuple and set it to timestamp of input event.
        // This column will be used later for computation of window operations.
        if(isWindowOnElementTime && s.inputElement.getKind() != QueueElement.Kind.E_HEARTBEAT)
        {
          s.inputTuple = tupleStorageAlloc.allocate();
          evalContext.bind(inpTuple, IEvalContext.INPUT_ROLE);
          evalContext.bind(s.inputTuple, IEvalContext.NEW_OUTPUT_ROLE);
          copyEval.eval(evalContext);
          ITuple o = s.inputTuple.pinTuple(IPinnable.WRITE);
          o.lValueSet(position, s.inputElement.getTs());
          s.inputTuple.unpinTuple();
        }
        else
          s.inputTuple = inpTuple;
        
        //set the inputTuple snapshotId to inputElement snapshotid
        if(s.inputTuple != null)
          s.inputTuple.setSnapshotId(s.inputElement.getSnapshotId());
                
        // Get the input timestamp
        s.inputTs = s.inputElement.getTs();    
        
        // Get the current total ordering guarantee flag
        s.currentTotalOrderingFlag 
          = s.inputElement.getTotalOrderingGuarantee();
        
        // Check if time has progressed or not
        isTimeProgressed = s.inputTs > s.lastInputTs;
        
        // Initialize the hbt propagation flag to true(default) if the input
        // is a heartbeat. Mark it false if it is not required to propagate.
        requiresHbtPropagation = 
          s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT &&
          isTimeProgressed;

        // We should have a progress of time.
        if (s.lastInputTs > s.inputTs)
        {
          throw ExecException.OutOfOrderException(
                  this,
                  s.lastInputTs, 
                  s.inputTs, 
                  s.inputElement.toString());
        }
        
        assert s.inputTs >= s.minNextTs:
          getDebugInfo(s.inputTs, s.minNextTs,
            s.inputElement.getKind().name(), s.lastInputKind.name());

        // calculate the expected timestamp for next input tuple
        s.minNextTs = s.inputElement.getTotalOrderingGuarantee() ?
                      s.inputTs+1 : s.inputTs;

        // update the last input timestamp and input kind
        s.lastInputTs = s.inputTs;
        s.lastInputKind = s.inputElement.getKind();
        
        // Get the latest visible timestamp        
        //s.visTs = window.getVisibleVal(s.inputTs);
        
        // Note: If slide > 1, then Output will be sent at a timestamp
        // multiple of slide value.
        // If input element's timestamp is not a multiple of slide value
        // then we will buffer it into a temporary collection.
        // In this section, As we have received a new input element, we 
        // will check whether we can send the buffered batch as output
        // before processing the current input element.

        if(hasBufferedElements && 
           isTimeProgressed && 
           s.inputTs >= bufferedTuplesMinTs)
        {          
          outputBufferedElements(s);
          hasBufferedElements = false;          
        }
        
        // Process the input according to the type
        if (s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
        {
          handleHeartbeat(s);
          // Propagate this heartbeat to downstream operator if required
          if(requiresHbtPropagation)
          {
            outputTuple(null, QueueElement.Kind.E_HEARTBEAT, s, false, s.inputTs);
          }
        }
        else if (s.inputElement.getKind() == QueueElement.Kind.E_PLUS)
        {
          handlePlus(s);
        }
        else if (s.inputElement.getKind() == QueueElement.Kind.E_MINUS)
        {
          handleMinus(s);
        }
        
        // input element consumed
        if (s.inputTuple != null)
        {
          inTupleStorageAlloc.release(s.inputTuple);
        }
      }
    }
    catch (SoftExecException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      return 0;
    }

    return 0;
    
  }
  
  private void handlePlus(ValueRelationWindowState s)
    throws CEPException
  {
    // Check the current output queue and see if any tuple need to be expired
    handleHeartbeat(s);
    
    // Set the flag if the current tuple is expired from window
    boolean isTupleExpired = isTupleExpired(s.inputTuple);
    
    // Set the flag if the current tuple is visible in window
    boolean isTupleVisible = isTupleVisible(s.inputTuple);

    if(!isTupleExpired)
    {
      if(isTupleVisible)
      {
       // Update the priority queue
        //inputTuple has the correct snapshotid value set in it since it is
        //copied over from inputElement already.
        valueWindowElements.add(s.inputTuple);
             
        long visTs = window.getVisibleVal(s.inputTs);
        // If the tuple is not visible now, then don't update the synopsis and
        // don't send the tuple to output     
        if(visTs > s.inputTs)
        {
          //inputTuple has the correct snapshotid value set in it since it is 
          //copied over from inputElement.
          plusBufferedOutputTuples.add(s.inputTuple);
          plusBufferedOutputTuplesTs.add(visTs);
          hasBufferedElements = true;
          bufferedTuplesMinTs
            = visTs < bufferedTuplesMinTs ? visTs : bufferedTuplesMinTs;
        }
        else
        {
          // Update the output synopsis
          s.tupleLineage[0] = s.inputTuple;
          s.nextOutputTuple = tupleStorageAlloc.allocate();
          
          evalContext.bind(s.nextOutputTuple, IEvalContext.NEW_OUTPUT_ROLE);
          evalContext.bind(s.inputTuple, IEvalContext.INPUT_ROLE);
          outEval.eval(evalContext);
          
          //copy the snapshot id in output
          s.nextOutputTuple.setSnapshotId(s.inputTuple.getSnapshotId());
          
          outSynopsis.insertTuple(s.nextOutputTuple, s.tupleLineage);
          
          // propagate the output with input tuple's guarantee flag
          boolean isTotalOrderGuarantee = s.currentTotalOrderingFlag;
          
          // Propagate the output tuple to output queue
          outputTuple(s.nextOutputTuple, 
                      QueueElement.Kind.E_PLUS, 
                      s,
                      isTotalOrderGuarantee,
                      visTs);   
        }  
      }
      else
      {
        //inputTuple has the correct snapshotid value set in it
        pendingInputTuples.add(s.inputTuple);
      }              
    }    
  }
  
  private void handleMinus(ValueRelationWindowState s)
    throws CEPException
  {
    
    // Check the current output queue and see if any tuple need to be expired    
    handleHeartbeat(s);
    
    // Check if that tuple is present in valueWindowElements
    // If yes, remove it from the queue and delete from synopsis
    Iterator<ITuplePtr> iter = valueWindowElements.iterator();
    ITuplePtr expiredTuple = null;
    boolean found = false;
    while(iter.hasNext())
    {
      expiredTuple = iter.next();
      if(isWindowOnElementTime)
      {
        if(s.inputTuple.compare(expiredTuple, new int[]{position}))
        {
          //expired tuple and input tuple have exact same values for all attrs except
          // the attribute value at "position". This is populated element_time
          // which will be different for minus and plus events.
          //however expired tuple represents the plus tuple which was part of 
          //the window whereas inputTuple is the current minus tuple.
          //so their snapshotids would be different.
          found = true;
          break;
        }
      }
      else if(s.inputTuple.compare(expiredTuple))
      {
        //expired tuple and input tuple have exact same values for all attrs.
        //however expired tuple represents the plus tuple which was part of 
        //the window whereas inputTuple is the current minus tuple.
        //so their snapshotids would be different.
        found = true;
        break;
      }
    }
    if(found)
    {
      // Update the priority queue
      valueWindowElements.remove(expiredTuple);
      
      // calculate the timestamp at which MINUS will be sent
      long visTs = window.getVisibleVal(s.inputTs);
      
      if(visTs > s.inputTs)
      {
        //Here we should put a tuple in minusBufferedOutputTuples list
        //only if plus corresponding to it is not a part of 
        //plusBufferedOutputTuples list.
        //It won't be a part of plusBufferedOutputTuples list if it is
        //present in the output synopsis.
        //Checking in the synopsis is efficient.
        s.tupleLineage[0] = expiredTuple;
        outSynIter = outSynopsis.getScan_l(s.tupleLineage);
        ITuplePtr tuplePtr = outSynIter.getNext();
        outSynopsis.releaseScan_l(outSynIter);
        
        if(tuplePtr != null)
        {
          //plus tuple corresponding to expiredTuple is present in synopsis
          //and not in plusBufferedOutputTuple list so we can add it 
          //in minusBufferedOutputTuples list.
          //Set the snapshot id to the snapshot of current minus tuple.
          expiredTuple.setSnapshotId(s.inputTuple.getSnapshotId());
          minusBufferedOutputTuples.add(expiredTuple);
          minusBufferedOutputTuplesTs.add(visTs);
          hasBufferedElements = true;
          bufferedTuplesMinTs
            = visTs < bufferedTuplesMinTs ? visTs : bufferedTuplesMinTs;
        }
        else
        {
          //plus tuple is present in plusBufferedOutputTuples list.
          //so don't add it in minus and delete from plus as well.
          int pos = plusBufferedOutputTuples.indexOf(expiredTuple);
          if(pos < 0) 
          {
            LogUtil.info(LoggerType.TRACE, this.getOptName()+ 
              " is ignoring following tuple for which no corresponding "+ 
              "plus was received:" + expiredTuple);
          }
          else
          {
            plusBufferedOutputTuples.remove(pos);
            plusBufferedOutputTuplesTs.remove(pos);
          }
        }
      }
      else
      {
        // Update the output synopsis
        s.tupleLineage[0] = expiredTuple;
        outSynIter = outSynopsis.getScan_l(s.tupleLineage);
        s.nextOutputTuple = outSynIter.getNext();
        if(s.nextOutputTuple != null)
        {
          outSynopsis.deleteTuple(s.nextOutputTuple);   
          outSynopsis.releaseScan_l(outSynIter);
          
          // propagate the output with input tuple's guarantee flag
          boolean isTotalOrderGuarantee = s.currentTotalOrderingFlag;
          
          long outputTs = window.getVisibleVal(s.inputTs);
          
          //copy over the snapshotid from input
          s.nextOutputTuple.setSnapshotId(s.inputTuple.getSnapshotId());
          
          // Propagate the output tuple to output queue
          outputTuple(s.nextOutputTuple, 
                      QueueElement.Kind.E_MINUS, 
                      s,
                      isTotalOrderGuarantee,
                      outputTs);    
        }
        else
        {
          int pos = plusBufferedOutputTuples.indexOf(expiredTuple);
          if(pos < 0)
          {
            LogUtil.info(LoggerType.TRACE, this.getOptName()+ " is ignoring "
              + "following tuple for which no corresponding plus was received:"
              + expiredTuple);
          }
          else
          {
            plusBufferedOutputTuples.remove(pos);
            plusBufferedOutputTuplesTs.remove(pos);
          }
        }
      }       
    }
  }

  private void handleHeartbeat(ValueRelationWindowState s)
    throws CEPException
  {
    setBaseValue(s);
    
    // If time has not changed from previous timestamp then don't do anything
    ITuplePtr oldestTuple = null;
    boolean done = false;
    if(isTimeProgressed)
    {
      oldestTuple = valueWindowElements.peek();
      while(oldestTuple != null && !done)
      {
        boolean isOldestTupleExpired = isTupleExpired(oldestTuple);
          
        if(isOldestTupleExpired)
        {
          // Update the priority queue
          valueWindowElements.remove();
          
          // Get the expired timestamp
          long expTs    = getExpiredTs(oldestTuple);          
          long visTs = window.getVisibleVal(expTs);
          
          if(visTs > s.inputTs)
          {
            //Here we should put a tuple in minusBufferedOutputTuples list
            //only if plus corresponding to it is not a part of 
            //plusBufferedOutputTuples list.
            //It won't be a part of plusBufferedOutputTuples list if it is
            //present in the output synopsis.
            //Checking in the synopsis is efficient.
            s.tupleLineage[0] = oldestTuple;
            outSynIter = outSynopsis.getScan_l(s.tupleLineage);
            ITuplePtr tuplePtr = outSynIter.getNext();
            outSynopsis.releaseScan_l(outSynIter);
            
            if(tuplePtr != null)
            {
              //plus tuple corresponding to expiredTuple is present in synopsis
              //and not in plusBufferedOutputTuple list so we can add it 
              //in minusBufferedOutputTuples list.
              //TODO: argue correctness of getcurrentsnapshotid + 1
              oldestTuple.setSnapshotId(execContext.getPlanMgr().getCurrentSnapshotId()+1);
              minusBufferedOutputTuples.add(oldestTuple);
              minusBufferedOutputTuplesTs.add(visTs);
              hasBufferedElements = true;
              bufferedTuplesMinTs
                = visTs < bufferedTuplesMinTs ? visTs : bufferedTuplesMinTs;
            }
            else
            {
              //plus tuple is present in plusBufferedOutputTuples list.
              //so don't add it in minus and delete from plus as well.
              int pos = plusBufferedOutputTuples.indexOf(oldestTuple);
              plusBufferedOutputTuples.remove(pos);
              plusBufferedOutputTuplesTs.remove(pos);
            }
            
          }
          else
          {
            // Update the output synopsis
            s.tupleLineage[0] = oldestTuple;
            outSynIter = outSynopsis.getScan_l(s.tupleLineage);
            s.nextOutputTuple = outSynIter.getNext();
            if(s.nextOutputTuple != null)
            {
              outSynopsis.deleteTuple(s.nextOutputTuple);
              outSynopsis.releaseScan_l(outSynIter);
              
              //TODO: argue correctness of getcurrentsnapshotid + 1
              s.nextOutputTuple.setSnapshotId(execContext.getPlanMgr().getCurrentSnapshotId()+1);
              
              // Get next oldest tuple         
              oldestTuple = valueWindowElements.peek();  
              
              // total ordering flag will be true only if the current input
              // 1) is a heartbeat
              // 1) has total ordering flag true
              // 2) we are expiring the last tuple from queue 
              boolean isTotalOrderGuarantee = 
                s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT &&
                s.currentTotalOrderingFlag && 
                oldestTuple == null;
                
              // Propagate the output tuple to output queue
              outputTuple(s.nextOutputTuple, 
                          QueueElement.Kind.E_MINUS, 
                          s, 
                          isTotalOrderGuarantee,
                          visTs);
            }
            else
            {
              int pos = plusBufferedOutputTuples.indexOf(oldestTuple);
              if(pos < 0)
              {
                LogUtil.info(LoggerType.TRACE, this.getOptName()+ " is ignoring"
                  + " following tuple for which no corresponding plus was "
                  + "received:" + oldestTuple);
              }
              else
              {
                plusBufferedOutputTuples.remove(pos);
                plusBufferedOutputTuplesTs.remove(pos);
              }
            }
             
          }
          // Get next oldest tuple         
          oldestTuple = valueWindowElements.peek();        
          done = oldestTuple == null;
        }
        else
        {
          done = true;
        }
      }
      
      processPendingElements(s);
    }
  }
  
  private void processPendingElements(ValueRelationWindowState s)
      throws CEPException
  {
    // Check if the pending input tuple list is empty
    boolean done = pendingInputTuples.size() == 0;
    
    // Peek the header element
    ITuplePtr oldestTuple = pendingInputTuples.peek();
    
    while(!done)
    {      
      // Set the flag if the tuple is expired from window
      boolean isOldestTupleExpired = isTupleExpired(oldestTuple);
      
      // Set the flag if the tuple is present in the window
      boolean isOldestTupleVisible = isTupleVisible(oldestTuple);
      
      // Don't scan further pending tuples if the oldest tuple is not visible
      if(!isOldestTupleVisible)
      {
        done = true;
        break;  
      }
      
      // Process the pending tuple only if it is present in the window 
      // otherwise ignore it
      if(!isOldestTupleExpired)
      {
        // Update the priority queue
        valueWindowElements.add(oldestTuple);
             
        long visTs = window.getVisibleVal(s.inputTs);
        
        // If the tuple is not visible now, then don't update the synopsis and
        // don't send the tuple to output
        if(visTs > s.inputTs)
        {
          plusBufferedOutputTuples.add(oldestTuple);
          plusBufferedOutputTuplesTs.add(visTs);
          hasBufferedElements = true;
          bufferedTuplesMinTs
            = visTs < bufferedTuplesMinTs ? visTs : bufferedTuplesMinTs;
        }
        else
        {
          // Update the output synopsis
          s.tupleLineage[0] = oldestTuple;
          s.nextOutputTuple = tupleStorageAlloc.allocate();
          
          evalContext.bind(s.nextOutputTuple, IEvalContext.NEW_OUTPUT_ROLE);
          evalContext.bind(oldestTuple, IEvalContext.INPUT_ROLE);
          outEval.eval(evalContext);
          
          //copy the snapshot id from the oldestTuple
          s.nextOutputTuple.setSnapshotId(oldestTuple.getSnapshotId());
          outSynopsis.insertTuple(s.nextOutputTuple, s.tupleLineage);
          
          // Propagate the output tuple to output queue
          outputTuple(s.nextOutputTuple, 
                      QueueElement.Kind.E_PLUS, 
                      s,
                      false,
                      s.inputTs);        
        }        
      }      
      // Remove the header element as we have processed that eleement
      pendingInputTuples.remove();
      
      // Check if any other pending tuple is remaining
      oldestTuple = pendingInputTuples.peek();
      // Exit if there is no pending tuple
      done = (oldestTuple == null);
    }
  }
  
  public void outputTuple(ITuplePtr outputTuple,
                           QueueElement.Kind kind,
                           ValueRelationWindowState s,
                           boolean isTotalOrderingGuarantee,
                           long outputTs)
    throws CEPException
  {    
    if(kind == QueueElement.Kind.E_HEARTBEAT)
    {
      s.outputElement.heartBeat(outputTs);
      s.outputElement.setTotalOrderingGuarantee(isTotalOrderingGuarantee);
      s.outputElement.setKind(kind);
      s.lastOutputTs = outputTs;
      outputQueue.enqueue(s.outputElement);
      s.stats.incrNumOutputHeartbeats();
      return;
    }
    s.outputElement.setTuple(outputTuple);
    
    //set event id val as tuple id if needed
    if(this.shouldUseEventIdVal())
    {
      assert eventIdColNum != -1 : "eventIdColNum not set in "
                                   +this.getOptName();
      
      if(outputTuple != null)
      {
        ITuple outTuple = outputTuple.pinTuple(IPinnable.WRITE);             
        //use event identifier col value as tuple id if needed
        outTuple.setId(outTuple.lValueGet(eventIdColNum));
        outputTuple.unpinTuple();
      }
    }
    // Set hbtPropagation to FALSE if there is any output on receiving current
    // input tuple.
    requiresHbtPropagation = false;

    //put the snapshotid from the tuple into the element
    s.outputElement.setSnapshotId(outputTuple.getSnapshotId());
    s.outputElement.setTs(outputTs);    
    s.outputElement.setKind(kind);
    s.outputElement.setTotalOrderingGuarantee(isTotalOrderingGuarantee);
    s.lastOutputTs = outputTs;
    outputQueue.enqueue(s.outputElement);   
    s.stats.incrNumOutputs();
  }
  
  /**
   * Check if the current tuple exists in window or not
   * @param s
   * @param currentTuple
   * @return true if element is inside window
   *         false otherwise
   * @throws CEPException
   */
  private boolean isTupleExpired(ITuplePtr currentTuple) 
    throws CEPException
  {
    ITuple o = currentTuple.pinTuple(IPinnable.READ);   
    
    // Throw Soft Exception if timestamp attribute value is null
    if(o.isAttrNull(position))
    {
      currentTuple.unpinTuple();
      throw new SoftExecException(
        ExecutionError.INVALID_TIMESTAMP_COLUMN_VALUE);
    }
    
    // Get the column value; This value should be comparable to timestamp
    long oldLongAttrVal = o.longValueGet(position);
    
    // Check if the tuple is expired or not
    if(window.expiredW(oldLongAttrVal))
    {
      currentTuple.unpinTuple();        
      return true; 
    }   
       
    currentTuple.unpinTuple();
    return false;
  }  
  
  public boolean isTupleVisible(ITuplePtr currentTuple) 
    throws CEPException
  {
    ITuple o = currentTuple.pinTuple(IPinnable.READ);   
    
    // Throw Soft Exception if timestamp attribute value is null
    if(o.isAttrNull(position))
    {
      currentTuple.unpinTuple();
      throw new SoftExecException(
        ExecutionError.INVALID_TIMESTAMP_COLUMN_VALUE);
    }
    
    // Get the column value; This value should be comparable to timestamp
    long oldLongAttrVal = o.longValueGet(position);
      
    // Check if the tuple is expired or not
    if(window.visibleW(oldLongAttrVal))
    {
      currentTuple.unpinTuple();        
      return true; 
    }   
       
    currentTuple.unpinTuple();
    return false;
  }
  
  public long getExpiredTs(ITuplePtr currentTuple) throws CEPException
  {
    ITuple o = currentTuple.pinTuple(IPinnable.READ);   
    
    // Throw Soft Exception if timestamp attribute value is null
    if(o.isAttrNull(position))
    {
      currentTuple.unpinTuple();
      throw new SoftExecException(
        ExecutionError.INVALID_TIMESTAMP_COLUMN_VALUE);
    }
    
    // Get the column value; This value should be comparable to timestamp
    long oldLongAttrVal = o.longValueGet(position);
    
    long expiredTs = window.getExpiredVal(oldLongAttrVal);
    
    currentTuple.unpinTuple();
    return expiredTs;
  }
    
  
  private void setBaseValue(ValueRelationWindowState s) throws CEPException
  {
    long baseLValue = s.inputTs;     
    window.setBaseValue(baseLValue);
  }
  
  private void outputBufferedElements(ValueRelationWindowState s)
    throws CEPException
  {
    int pendingMinusTuples = 0;
    int pendingPlusTuples = 0;    
    boolean done = false;
    long nextMinusBufferedTs = Long.MAX_VALUE;
    long nextPlusBufferedTs = Long.MAX_VALUE;
    
    if(minusBufferedOutputTuples != null)
    {
      pendingMinusTuples = minusBufferedOutputTuples.size();
    }
    if(plusBufferedOutputTuples != null)
    {
      pendingPlusTuples = plusBufferedOutputTuples.size();
    }

    nextMinusBufferedTs 
      = pendingMinusTuples > 0 ? 
          minusBufferedOutputTuplesTs.peek() : Long.MAX_VALUE;
    nextPlusBufferedTs
      = pendingPlusTuples > 0 ? 
          plusBufferedOutputTuplesTs.peek() : Long.MAX_VALUE;
    done = (nextMinusBufferedTs == Long.MAX_VALUE && 
            nextPlusBufferedTs == Long.MAX_VALUE) ||
            s.inputTs < bufferedTuplesMinTs;
    while(!done)
    {      
      if(nextMinusBufferedTs <= nextPlusBufferedTs)
      {
        ITuplePtr minusTuple = minusBufferedOutputTuples.remove();
        long minusTupleTs = minusBufferedOutputTuplesTs.remove();
        // update the synopsis
        s.tupleLineage[0] = minusTuple;
        outSynIter = outSynopsis.getScan_l(s.tupleLineage);
        s.nextOutputTuple = outSynIter.getNext();
        if(s.nextOutputTuple != null)
        {
          outSynopsis.deleteTuple(s.nextOutputTuple);   
          
          outSynopsis.releaseScan_l(outSynIter);
          
          //put the snapshot id from minustuple
          s.nextOutputTuple.setSnapshotId(minusTuple.getSnapshotId());
          
          // Propagate the output tuple to output queue
          outputTuple(s.nextOutputTuple, 
                      QueueElement.Kind.E_MINUS, 
                      s,
                      false,
                      minusTupleTs);   
        }
        else
        {
          int pos = plusBufferedOutputTuples.indexOf(minusTuple);
          if(pos < 0)
          {
            LogUtil.info(LoggerType.TRACE, this.getOptName()+ " is ignoring"
              + " following tuple for which no corresponding plus was received:"
              + minusTuple);
          }
          else
          {
            plusBufferedOutputTuples.remove(pos);
            plusBufferedOutputTuplesTs.remove(pos);            
          }
        }
        
        pendingMinusTuples = minusBufferedOutputTuples.size();
        nextMinusBufferedTs 
        = pendingMinusTuples > 0 ? 
            minusBufferedOutputTuplesTs.peek() : Long.MAX_VALUE;
      }
      else
      {
        ITuplePtr plusTuple = plusBufferedOutputTuples.remove();
        long plusTupleTs = plusBufferedOutputTuplesTs.remove();
        
        // Update the output synopsis
        s.tupleLineage[0] = plusTuple;
        s.nextOutputTuple = tupleStorageAlloc.allocate();
        
        evalContext.bind(s.nextOutputTuple, IEvalContext.NEW_OUTPUT_ROLE);
        evalContext.bind(plusTuple, IEvalContext.INPUT_ROLE);
        outEval.eval(evalContext);
        
        //copy the snapshot id from plusTuple
        s.nextOutputTuple.setSnapshotId(plusTuple.getSnapshotId());
        
        outSynopsis.insertTuple(s.nextOutputTuple, s.tupleLineage);
        
        // Propagate the output tuple to output queue
        outputTuple(s.nextOutputTuple, 
                    QueueElement.Kind.E_PLUS, 
                    s,
                    false,
                    plusTupleTs);   
        
        pendingPlusTuples = plusBufferedOutputTuples.size();
        nextPlusBufferedTs
          = pendingPlusTuples > 0 ? 
            plusBufferedOutputTuplesTs.peek() : Long.MAX_VALUE;
      }
      
      bufferedTuplesMinTs = nextMinusBufferedTs < nextPlusBufferedTs ? 
                            nextMinusBufferedTs : nextPlusBufferedTs;
      
      hasBufferedElements = 
        !(nextMinusBufferedTs == Long.MAX_VALUE && 
          nextPlusBufferedTs == Long.MAX_VALUE);
      
      done = !hasBufferedElements || s.inputTs < bufferedTuplesMinTs;
    }  
  }
  
  /**
   * @param window the window to set
   */
  public void setWindow(
    oracle.cep.execution.internals.windows.ValueWindow window)
  {
    this.window = window;
  }

  /**
   * Set the timestamp column position
   * @param position
   */
  public void setPosition(int position)
  {
    this.position = position;
  }

  /**
   * Set the flag whether the window is defied on ELEMENT_TIME pseudo column.
   * @param isWindowOnElementTime
   */
  public void setWindowOnElementTime(boolean isWindowOnElementTime)
  {
    this.isWindowOnElementTime = isWindowOnElementTime;
  }

  /**
   * Set the output synopsis
   * @param outSynopsis
   */
  public void setOutSynopsis(LineageSynopsis outSynopsis)
  {
    this.outSynopsis = outSynopsis;
  }
  
  /**
   * Set the outEval
   * @param outEval
   */
  public void setOutEval(IAEval outEval)
  {
    this.outEval = outEval;
  }
  
  /**
   * Set the copyEval
   * @param copyEval
   */
  public void setCopyEval(IAEval copyEval)
  {
    this.copyEval = copyEval;
  }

  /**
   * Set the priority queue for output
   * @param valueWindowElements
   */
  public void setValueWindowElements(
    PriorityQueue<ITuplePtr> valueWindowElements)
  {
    this.valueWindowElements = valueWindowElements;
  }

  /**
   * @param pendingInputTuples the pendingInputTuples to set
   */
  public void setPendingInputTuples(PriorityQueue<ITuplePtr> pendingInputTuples)
  {
    this.pendingInputTuples = pendingInputTuples;
  }


  /**
   * Set the eval context
   * @param evalContext
   */
  public void setEvalContext(IEvalContext evalContext)
  {
    this.evalContext = evalContext;
  }
  
  /**
   * @param colType the colType to set
   */
  public void setColType(Datatype colType)
  {
    this.colType = colType;
  }


  protected boolean isHeartbeatPending() {
    return false;
  }

  public void initializeState() throws CEPException
  {
    if(archivedRelationTuples != null)
    {
      ValueRelationWindowState s = (ValueRelationWindowState)mut_state;
      
      // Set the window base value 
      window.setBaseValue(snapShotTime);
      
      for(ITuplePtr currentTuple : archivedRelationTuples)
      {
        // Check if the tuple is visible; 
        boolean isTupleVisible = isTupleVisible(currentTuple);
        
        if(isTupleVisible)
        {
          // Update the priority queue
          valueWindowElements.add(currentTuple);
              
          long visTs = window.getVisibleVal(snapShotTime);
          // If the tuple is not visible now, then don't update the synopsis and
          // don't send the tuple to output     
          if(visTs > snapShotTime)
          {
            //TODO: May be the code setting snapshotid in buffered tuples doesn't
            // to be there as ValueRelwindow cannot be view root.
            currentTuple.setSnapshotId(execContext.getPlanMgr().getCurrentSnapshotId()+1);
            plusBufferedOutputTuples.add(currentTuple);
            plusBufferedOutputTuplesTs.add(visTs);
            hasBufferedElements = true;
            bufferedTuplesMinTs
              = visTs < bufferedTuplesMinTs ? visTs : bufferedTuplesMinTs;
          }
          else
          { 
            // Insert into output synopsis
            outSynopsis.insertTuple(currentTuple, new ITuplePtr[]{currentTuple});
          
            // Propagate the output tuple to output queue
            s.inputTs = snapShotTime;
            s.lastInputTs = s.inputTs;
            s.outputElement.setTuple(currentTuple);
            s.outputElement.setTs(s.inputTs);    
            s.outputElement.setKind(QueueElement.Kind.E_PLUS);
            s.outputElement.setTotalOrderingGuarantee(false);
            s.outputElement.setSnapshotId(execContext.getPlanMgr().getCurrentSnapshotId());
            s.lastOutputTs = s.inputTs;
           
            ((ISharedQueueWriter)outputQueue).enqueue(s.outputElement, 
                                                    this.getArchiverReaders());
            /*
            LogUtil.finest(LoggerType.TRACE, "ARF# "+ 
                         this.getOptName()+
                         " propagated archiver tuple: "+currentTuple);
            */
          }
        }
        else
        {
          /*
           * A pending tuple received from archiver would be processed if this 
           * opt is above a view root. Operators above won't have snapshotid set
           * in queue and so this will be propagated.
           * 
           * If this is a view root or below view root then we don't want this 
           * tuple to go further since operators downstream would have accounted
           * for this while querying their snapshots. So set snapshotid to
           * currentSnapshot+1. Operators downstream would have value higher or
           * equal to this and so will ignore it.
           */
          currentTuple.setSnapshotId(execContext.getPlanMgr().getCurrentSnapshotId()+1);
          pendingInputTuples.add(currentTuple);
          LogUtil.finer(LoggerType.TRACE, "ARF# "+
            this.getOptName()+
            " does not propagate archiver tuple as it is"+
            " not visible, added to the list of pending"+
            " tuples: "+currentTuple);
        }       
        currentTuple.unpinTuple();        
      }
      
      //send heartbeat with ordering guarantee false
      s.lastOutputTs=snapShotTime + 1;
      s.outputElement.heartBeat(s.lastOutputTs);
      s.outputElement.setTotalOrderingGuarantee(false);
      s.outputElement.setKind(QueueElement.Kind.E_HEARTBEAT);
      s.outputElement.setSnapshotId(execContext.getPlanMgr().getCurrentSnapshotId());
      ((ISharedQueueWriter) outputQueue).enqueue(s.outputElement, 
                                                 this.getArchiverReaders());
      LogUtil.finer(LoggerType.TRACE, "ARF# "+
                   this.getOptName()+
                   " sent heartbeat of "+s.lastOutputTs+ " with ordering guarantee false");
      s.stats.incrNumOutputs();

      LogUtil.finer(LoggerType.TRACE, "ARF# "+
                   "Initialized state of "+this.getOptName()+
                   " and propagated events received from archiver downstream");
      //only if 
      //1. There are archived relation tuples which have been enqueued
      // AND
      //2. Propagation (of old data) is indicated as required at this point
      //Set the oldDataPropNeeded to false to avoid duplicate output.
      if((archivedRelationTuples.size() > 0) && (this.propagationReqd()))
        oldDataPropNeeded  = false;
      
      //remove all the archived relation tuples.
      archivedRelationTuples.clear();
    }
  }
}
