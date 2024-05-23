/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/RowWindow.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */
/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares RowWindow in package oracle.cep.execution.operators.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 sborah    04/13/09 - assertion check
 parujain  04/03/09 - negative ts
 udeshmuk  01/22/09 - total ts ordering opt.
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 sborah    09/25/08 - update stats
 parujain  06/17/08 - slide support
 hopark    12/06/07 - cleanup spill
 hopark    10/30/07 - remove IQueueElement
 hopark    10/22/07 - remove TimeStamp
 parujain  10/04/07 - delete op
 hopark    07/13/07 - dump stack trace on exception
 parujain  07/03/07 - cleanup
 parujain  06/26/07 - mutable state
 hopark    06/19/07 - cleanup
 hopark    05/24/07 - debug logging
 hopark    05/16/07 - add arguments for OutOfOrderException
 parujain  05/08/07 - monitoring statistics
 hopark    04/08/07 - fix pincount
 najain    04/03/07 - bug fix
 hopark    03/24/07 - add unpin
 najain    03/14/07 - cleanup
 najain    03/12/07 - bug fix
 parujain  02/27/07 - NPE bug
 najain    02/19/07 - bug fix
 najain    01/05/07 - spill over support
 parujain  12/11/06 - propagating relations
 najain    11/02/06 - bug fix
 najain    08/02/06 - refCounting optimizations
 najain    08/10/06 - add asserts
 najain    07/19/06 - ref-count tuples 
 najain    07/13/06 - ref-count timestamps 
 najain    07/13/06 - ref-count timeStamp support 
 najain    07/10/06 - move inStore to parent 
 najain    07/06/06 - cleanup
 dlenkov   05/17/06 - real row window creation
 skaluska  03/14/06 - query manager 
 skaluska  02/06/06 - creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/RowWindow.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Map.Entry;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.execution.snapshot.SynopsisPersistenceContext;
import oracle.cep.execution.synopses.ExecSynopsis;
import oracle.cep.execution.synopses.WindowSynopsis;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.ExecContext;

/**
 * RowWindow
 *
 * @author skaluska
 */
public class RowWindow extends ExecOpt
{
  /** windowSize */
  private long           windowSize;
  
  /** slide */
  private int            slideSize;

  /** winSynopsis */
  private WindowSynopsis winSynopsis;

  /** Journal Entry of Row Window operator for Journal Snapshot*/
  private RowWindowJournalEntry journalEntry;
  
  /**
   * Constructor
   * @param ec TODO
   */
  public RowWindow(ExecContext ec)
  {
    super(ExecOptType.EXEC_ROW_WIN, new RowWindowState(ec), ec);
  }

  /**
   * Getter for windowSize
   * 
   * @return Returns the windowSize
   */
  public long getWindowSize()
  {
    return windowSize;
  }
  
  /**
   * Getter for slideSize
   * 
   * @return Returns the slideSize
   */
  public int getSlideSize()
  {
    return slideSize;
  }

  /**
   * Setter for windowSize
   * 
   * @param windowSize
   *          The windowSize to set.
   */
  public void setWindowSize(long windowSize)
  {
    this.windowSize = windowSize;
  }
  
  /**
   * Setter for slideSize
   * 
   * @param slideSize
   *         The slideSize to set
   */
  public void setSlideSize(int slideSize)
  {
    this.slideSize = slideSize;
  }

  /**
   * Getter for winSynopsis
   * 
   * @return Returns the winSynopsis
   */
  public WindowSynopsis getWinSynopsis()
  {
    return winSynopsis;
  }

  /**
   * Setter for winSynopsis
   * 
   * @param winSynopsis
   *          The winSynopsis to set.
   */
  public void setWinSynopsis(WindowSynopsis winSynopsis)
  {
    this.winSynopsis = winSynopsis;
  }
  
 
  private boolean canTupleExpire(RowWindowState s)
  {
    // if slide=1, with every incoming tuple , a tuple can expire
    if(slideSize == 1)
    {
      return true;
    }
    // we will expire the tuple only when we have received sufficient no of tuples
    if((s.inputElems.size()+1) == slideSize)
    {
      return true;
    }
    return false;
  }

  /*
   * @see oracle.cep.execution.operators.ExecOpt#run(int)
   */
  public int run(int timeSlice) throws ExecException
  {
    int numElements;
    boolean done = false;
    RowWindowState s = (RowWindowState) mut_state;
    boolean exitState = true;
    QueueElement temp;

    assert s.state != ExecState.S_UNINIT;

    // Stats
    s.stats.incrNumExecutions();

    try
    {
      numElements = timeSlice;
      while ((s.stats.getNumInputs() < numElements) || (!exitState))
      {
        switch (s.state)
        {
          case S_PROPAGATE_OLD_DATA:
            setExecSynopsis((ExecSynopsis) winSynopsis);
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
              // we might still need to output a heartbeat
              if (s.lastInputTs <= s.lastOutputTs)
              {
                s.state = ExecState.S_INIT;
                done = true;
                break;
              }

              // Output a heartbeat
              s.state = ExecState.S_GENERATE_HEARTBEAT;
              break;
            }
            else
            {
              // Bump our counts
              if(s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
                s.stats.incrNumInputHeartbeats();
              else
                s.stats.incrNumInputs();
              
      
              exitState = false;
              s.inputTuple = s.inputElement.getTuple();
              s.inputKind = s.inputElement.getKind();
              s.inputTs = s.inputElement.getTs();
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
              
              // ensure that the time stamp value is as per the OrderingFlag 
              assert s.inputTs >= s.minNextTs :
                getDebugInfo(s.inputTs, s.minNextTs, 
                             s.inputElement.getKind().name(),
                             s.lastInputKind.name());
              
              s.lastInputTs           = s.inputTs;
              s.lastInputKind         = s.inputElement.getKind();
              s.lastInputOrderingFlag = s.inputElement.getTotalOrderingGuarantee();
              s.minNextTs             = s.lastInputOrderingFlag ? 
                                        s.inputTs+1 : s.inputTs;
            }
            s.state = ExecState.S_PROCESSING1;
          case S_PROCESSING1:
            // Increment ref count of input tuple
            // Inputs: inputTuple
            // Outputs: inputTuple

            assert s.inputElement != null;

            if (s.inputKind == QueueElement.Kind.E_PLUS)
              assert s.inputTuple != null;
            else if (s.inputKind == QueueElement.Kind.E_HEARTBEAT)
            {
              // Nothing more to be done
              assert s.inputTuple == null;
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;
              break;
            }
            s.canTupleExpire = canTupleExpire(s);
            s.state = ExecState.S_PROCESSING3;
          case S_PROCESSING3:
            // Determine tuple to be expired
            // Inputs: inputKind, lastInputTs
            // Outputs: expiredTuple

            // Find oldest tuple
            // synopsis can be empty if rows < slide eg rows = 2 and slide = 4
            // eg rows = 2 and slide =4 then numProcessed = 3 then we should not
            // send any -ve tuple for first two tuples, they will be deleted in
            // later stage from the synopsis. This is a special case for 
            // row < slide
            if (((s.canTupleExpire) && (s.numProcessed >= windowSize)) &&
               (!((windowSize < slideSize) && (s.numProcessed == (slideSize-1)))))
            {
              s.expiredTuple = winSynopsis.getOldestTuple();
              s.numProcessed--;
            }
            else
            { 
              s.expiredTuple = null;
              s.numProcessed++;
              s.canTupleExpire = false;
              if(slideSize > 1)
              {
                s.state = ExecState.S_POPULATE_SLIDE_LIST;
                break;
              }
            }

            s.state = ExecState.S_OUTPUT_TIMESTAMP;
          case S_OUTPUT_TIMESTAMP:
            s.state = ExecState.S_OUTPUT_READY;
          case S_OUTPUT_READY:
            // Allocate queue element for output
            // Inputs: inputTuple, inputTs, expiredTuple
            // Outputs: outputElement

            assert ((s.inputTuple != null) || (s.expiredTuple != null));
            if (s.expiredTuple == null)
              s.outputElement.copy(s.inputElement);
            s.state = ExecState.S_PROCESSING4;
          case S_PROCESSING4:
            if (s.expiredTuple != null)
            {
              s.outputElement.setKind(QueueElement.Kind.E_MINUS);
              s.outputElement.setTs(s.inputElement.getTs());
              s.outputElement.setTuple(s.expiredTuple);
              //always false guarantee since at least one tuple will be output
              //after expired tuples are output
              s.outputElement.setTotalOrderingGuarantee(false);
              s.lastOutputTs = s.outputTs;
            }
            else
            {
              // Add input tuple to the window
              // Inputs: inputTuple
              // Outputs: inputTuple
              // Here only if slide size is 1
              assert (s.inputKind == QueueElement.Kind.E_PLUS) : s.inputKind;
              assert s.outputElement.equals(s.inputElement);
              s.lastOutputTs = s.inputElement.getTs();
   
              // Add timestamp information to tuple pointer so that
              // if persisted, the timestamp information is not lost from synopsis
              s.inputTuple.setTimestamp(-1);
              winSynopsis.insertTuple(s.inputTuple, -1);
              s.numWindowElements++;
              if(journalEntry != null)
                updateJournalEntry(s.inputTuple, true);
              
              // slide size is 1 and no expired tuples left so copy the flag as is from input
              s.outputElement.setTotalOrderingGuarantee(
                              s.inputElement.getTotalOrderingGuarantee());
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
              winSynopsis.deleteOldestTuple();
              s.numWindowElements--;
              if(journalEntry != null)
                updateJournalEntry(s.expiredTuple, false);
              // Look for more expired elements
              s.state = ExecState.S_PROCESSING3;
            }
            else
            {
              if(s.isPopulatingOutputs)
                s.state = ExecState.S_POPULATE_ALL_OUTPUTS;
              else
              {
                exitState = true;
                // The plus corresponding to the input tuple is output last
                s.state = ExecState.S_INIT;
              }
            }
            break;
          case S_INPUT_ELEM_CONSUMED:
            assert s.inputElement != null;
            assert s.expiredTuple == null;
            assert s.inputKind == QueueElement.Kind.E_HEARTBEAT;

            exitState = true;

            s.state = ExecState.S_INIT;
            break;
          case S_POPULATE_SLIDE_LIST:
            // add the tuple to the list only if the slide size is greater than 1
            assert (s.inputKind == QueueElement.Kind.E_PLUS) : s.inputKind;
            temp = mut_state.allocQueueElement();
            temp.copy(s.inputElement);
            s.inputElems.add(temp);
            inTupleStorageAlloc.addRef(temp.getTuple());
            if(s.inputElems.size() < slideSize)
            {
              s.state = ExecState.S_INIT;
              exitState = true;
              break;
            }
            else if(slideSize > windowSize)
            {
              s.state = ExecState.S_IGNORE_POPULATING_OUTPUTS;
              break;
            }
            s.state = ExecState.S_POPULATE_ALL_OUTPUTS;
          case S_POPULATE_ALL_OUTPUTS:
            if(!s.inputElems.isEmpty())
            {
              s.isPopulatingOutputs = true;
              s.outputElement.copy(s.inputElems.getFirst());
              s.outputElement.setTs(s.inputElement.getTs());
              s.lastOutputTs = s.inputElement.getTs();
              //If this is the last tuple then copy the input flag as is, else FALSE
              if(s.inputElems.size() == 1)
                s.outputElement.setTotalOrderingGuarantee(
                                s.inputElement.getTotalOrderingGuarantee());
              else 
                s.outputElement.setTotalOrderingGuarantee(false);
              
              // Add timestamp information to tuple pointer so that if
              // persisted, the timestamp information is not lost from synopsis
              s.inputElems.getFirst().getTuple().setTimestamp(-1);
              winSynopsis.insertTuple(s.inputElems.getFirst().getTuple(), -1);
              s.numWindowElements++;
              if(journalEntry != null)
                updateJournalEntry(s.inputElems.getFirst().getTuple(), true);
              
              // decrement the ref count
              inTupleStorageAlloc.release(s.inputElems.getFirst().getTuple());
              s.inputElems.remove();
              s.state = ExecState.S_OUTPUT_ELEMENT;
            }
            else
            {
              s.isPopulatingOutputs = false;
              s.state = ExecState.S_INIT;
              exitState = true;
            }
            break;
          case S_IGNORE_POPULATING_OUTPUTS:
            // This is the state when rows > slide then some of the input
            // tuples will never be populated. They need to be ignored
            // and removed from the synopsis
            // only no of tuples = windowsize should be outputed
            while(s.inputElems.size() > windowSize)
            {
              // Add timestamp information to tuple pointer so that if
              // persisted, the timestamp information is not lost from synopsis
              s.inputElems.getFirst().getTuple().setTimestamp(-1);
              
              // This is required because of the assumption of window synopsis
              // Every tuple needs to enter and leave synopsis   
              ITuplePtr addedSynopsisTuple = s.inputElems.getFirst().getTuple();
              winSynopsis.insertTuple(addedSynopsisTuple, -1);
              s.numWindowElements++;
              if(journalEntry != null)
                updateJournalEntry(addedSynopsisTuple, true);
              
              ITuplePtr removedSynopsisTuple = winSynopsis.getOldestTuple();
              winSynopsis.deleteOldestTuple();
              s.numWindowElements--;
              if(journalEntry != null)
                updateJournalEntry(removedSynopsisTuple, false);
              
              inTupleStorageAlloc.release(s.inputElems.getFirst().getTuple());
              s.inputElems.remove();
              s.numProcessed--;
            }
            s.state = ExecState.S_POPULATE_ALL_OUTPUTS;
            break;
          case S_GENERATE_HEARTBEAT:
            assert exitState == true;
            s.state = ExecState.S_PROCESSING5;

          case S_PROCESSING5:
            s.lastOutputTs = s.lastInputTs;
            s.outputElement.heartBeat(s.lastInputTs);
            //use the flag of the last input
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
      return 0;
    }
    

    return 0;
  }

  /*
   * @see oracle.cep.execution.operators.ExecOpt#deleteOp()
   */

  public void deleteOp()
  {
    // TODO

  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<RowWindow id=\"" + id + "\" winSize=\"" + windowSize + "\" >");
    sb.append("<InputQueue>" + inputQueue.toString() + "</InputQueue>");
    sb.append("<OutputQueue>" + outputQueue.toString() + "</OutputQueue>");
    sb.append("<Synopsis>" + winSynopsis.toString() + "</Synopsis>");
    sb.append("</RowWindow>");

    return sb.toString();
  }

  /**
   * Create snapshot of Row Window operator by writing the operator state
   * into param java output stream.
   * State of Row Window operator consists of following:
   * 1. Mutable State
   * 2. Window Synopsis
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
      output.writeObject((RowWindowState)mut_state);
      IPersistenceContext synopsisPersistenceCtx = new SynopsisPersistenceContext();
      synopsisPersistenceCtx.setScanId(propScanId);
      winSynopsis.writeExternal(output, synopsisPersistenceCtx);
    } 
    catch (IOException e)
    {
      throw new CEPException(ExecutionError.SNAPSHOT_CREATION_ERROR, e, e.getLocalizedMessage(), this.getOptName());
    }
  }
  
  protected void loadSnapshot(ObjectInputStream input) throws ExecException
  {
    try
    {
      RowWindowState loaded_mutable_state = (RowWindowState) input.readObject();
      ((RowWindowState)mut_state).copyFrom(loaded_mutable_state);
      
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
      journalEntry = new RowWindowJournalEntry();
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
      
      // Log the journal entry details
      LogUtil.fine(LoggerType.TRACE, journalEntry.toString());
    }
    // Invoke endBatch to write journal stream to global output stream
    super.endBatch();
  }
  
  /** Apply Journal snapshot to Window */
  @Override
  protected void applySnapshot(Object journalEntry) throws ExecException
  {
    if(journalEntry instanceof RowWindowJournalEntry)
      loadJournalEntry(((RowWindowJournalEntry)journalEntry));
    else
      throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR_CNF, 
                               this.getOptName(), journalEntry.getClass().getName());            
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
    journalEntry.setMutable_state((RowWindowState)mut_state);
  }
  
  /**
   * Load operator state by applying given journal snapshot entry
   * @param entry snapshot object
   * @throws ExecException
   */
  public void loadJournalEntry(RowWindowJournalEntry entry) throws ExecException
  {
    // Load mutable state
    RowWindowState opState = ((RowWindowState)mut_state);
    opState.copyFrom(entry.getMutable_state());
        
    // Number of elements in window synopsis at the time of snapshot
    long numWindowElems = opState.numWindowElements;
    
    int added = 0;
    int removed=0;
    // Load Window Synopsis 
    if(entry.getPlusChangeEvents() != null)
    {      
      Iterator<Entry<Long, ITuplePtr>> iter = entry.getPlusChangeEvents().entrySet().iterator();
      while(iter.hasNext())
      {
        Entry<Long,ITuplePtr> next = iter.next();
        ITuplePtr tup = next.getValue();        
        winSynopsis.insertTuple(tup, -1);
        numWindowElems++;
        added++;
      }
      
      // Delete all those tuples from synopsis which are out of window size
      while(numWindowElems > windowSize)
      {
        winSynopsis.deleteOldestTuple();
        numWindowElems--;           
        removed++;
      }        
    }
    
    // Log relevant information about snapshot summary
    LogUtil.fine(LoggerType.TRACE, "Loaded Snapshot in Row Window Operator " + this.getOptName() +
      ". Window Synopsis Change Events[inserted: " + added + ", removed:" + removed + "]");
  }
  
}
