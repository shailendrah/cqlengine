/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/VariableRangeWindow.java /main/3 2013/12/15 23:50:49 sbishnoi Exp $ */

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
    sbishnoi    12/12/13 - bug 17623763
    sbishnoi    04/17/13 - bug 16655733
    sbishnoi    04/11/13 - bug 16585681
    sbishnoi    03/17/11 - Creation
 */
package oracle.cep.execution.operators;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.logging.Level;

import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.QueueElement.Kind;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.internals.memory.EvalContext;
import oracle.cep.execution.synopses.ExecSynopsis;
import oracle.cep.execution.synopses.LineageSynopsis;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.service.ExecContext;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/VariableRangeWindow.java /main/3 2013/12/15 23:50:49 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class VariableRangeWindow extends ExecOpt
{
  
  /** evaluation context */
  private IEvalContext    evalContext;
  
  /** position of attribute in current tuple which keeps the calculated value 
   *  of the expiry timestamp */
  private int             expTsPos;
  
  /**
   * Value of slide associated with this operator
   */
  private long            slideAmount;
 
  /** winSynopsis */
  private LineageSynopsis outSynopsis;
  
  /** a priority queue which will keep the output window elements */ 
  private PriorityQueue<ITuplePtr> expiryTimeOrderedElements;
  
  /** a queue which will keep the pending elements which are still
   * not inserted into output */
  private LinkedList<ITuplePtr> pendingElements;
  
  /** A flag to track if the timestamp has progressed or not on the new input*/
  private boolean         isTimeProgressed;
  
  /** iterator over output synopsis */
  private TupleIterator   outSynIter;
  
  /** eval to copy attribute value from outputTuple to nextOutputTuple */
  private IAEval          expTsEval;
  
  /** position to keep the element time*/
  private int             elementTimePos;
  

  /**
   * Constructor
   * @param ec
   */
  public VariableRangeWindow(ExecContext ec)
  {
    super(ExecOptType.EXEC_VARIABLE_RANGE_WIN, 
          new VariableRangeWindowState(ec), ec);    
  }


  @Override
  protected int run(int timeslice) throws CEPException
  {
    VariableRangeWindowState s = (VariableRangeWindowState)mut_state;
    
    assert s.state != ExecState.S_UNINIT;

    // Update Stats
    s.stats.incrNumExecutions();
    
    
    if (s.state == ExecState.S_PROPAGATE_OLD_DATA)
    {
      setExecSynopsis((ExecSynopsis)outSynopsis);
      handlePropOldData();
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
        s.inputTuple = s.inputElement.getTuple();
        
        // Get the input timestamp
        s.inputTs = s.inputElement.getTs();    
        
        // Get the current total ordering guarantee flag
        s.currentTotalOrderingFlag 
          = s.inputElement.getTotalOrderingGuarantee();
        
        // Check if time has progressed or not
        isTimeProgressed = s.inputTs > s.lastInputTs;

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
        
        // Process the input according to the type
        if (s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
        {
          handleHeartbeat(s);
        }
        else if (s.inputElement.getKind() == QueueElement.Kind.E_PLUS)
        {
          handlePlus(s);
        }
        else if (s.inputElement.getKind() == QueueElement.Kind.E_MINUS)
        {
          // Should be unreachable
          assert false;         
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
  
  /**
   * Handle the time progression, either by a heartbeat or by an input tuple
   * This procedure will check if we can expire existing window elements after
   * the timestamp change
   * @param s
   * @param isHeartbeat
   * @throws CEPException
   */
  private void handleHeartbeat(VariableRangeWindowState s) throws CEPException
  {
    // If time has not changed from previous timestamp then don't do anything
    if(isTimeProgressed)
    {
      handleExpiredTs(s);
      handleVisibleTs(s);
      handleExpiredTs(s);
    }
    // else Ignore the heartbeat
  }

  /**
   * 
   * @param s
   * @throws CEPException
   */
  private void handleVisibleTs(VariableRangeWindowState s) throws CEPException
  {
    ITuplePtr nextTuple = null;
    boolean done = false;
    
    nextTuple = pendingElements.peek();
    while(nextTuple != null && !done)
    {
      // Return visibleTs for current tuple, if the tuple is visible;
      // else return null;
      Long visibleTs = isTupleVisible(s, nextTuple);
      
      boolean isTupleVisible = visibleTs != null;
      if(!isTupleVisible)
      {
        // There are no more tuples which can be visible
        done = true;
      }
      else
      {
        // Update the visible time ordered elements list
        s.nextOutputTuple = pendingElements.remove();
        
        // Update the output synopsis
        s.tupleLineage[0] = s.nextOutputTuple;     
        outSynopsis.insertTuple(s.nextOutputTuple, s.tupleLineage);
        
        nextTuple = pendingElements.peek();
        
        // Get timestamp of next plus tuple
        Long nextTupleTs = null;
        if(nextTuple != null)
          nextTupleTs = isTupleVisible(s, nextTuple);
        
        // Get timestamp of next expiry tuple         
        ITuplePtr nextExpiryTuple = expiryTimeOrderedElements.peek(); 
        Long expiryTs = 
          nextExpiryTuple != null ? isTupleExpired(s, nextExpiryTuple) : null;
        
        // propagate the output with input tuple's guarantee flag
        boolean isTotalOrderGuarantee = 
          (s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT )&&
          (nextTuple == null || (nextTuple != null && nextTupleTs != null && visibleTs < nextTupleTs)) &&
          (expiryTs == null || (expiryTs != null && visibleTs < expiryTs));
        
        // Propagate the output tuple to output queue
        outputTuple(s.nextOutputTuple, 
                    QueueElement.Kind.E_PLUS, 
                    s,
                    isTotalOrderGuarantee,
                    visibleTs);        
        
        expiryTimeOrderedElements.add(s.nextOutputTuple);
      }
    }
  }
  
  private Long isTupleVisible(VariableRangeWindowState s, ITuplePtr currentTuple)
    throws CEPException
  {
     ITuple o = currentTuple.pinTuple(IPinnable.READ);
    
    // Throw Soft Exception if range expression value is null
    if(o.isAttrNull(elementTimePos))
      throw new SoftExecException(
        ExecutionError.INVALID_TIMESTAMP_COLUMN_VALUE);
        
    long elementTs = o.longValueGet(elementTimePos);
    
    long visTs = getVisibleTs(elementTs);
    
    if(visTs > s.inputTs)
    {
      currentTuple.unpinTuple();
      return null;
    }
    else
    {
      currentTuple.unpinTuple();
      return visTs;
    }
  }


  /**
   * 
   * @param s
   * @throws CEPException
   */
  private void handleExpiredTs(VariableRangeWindowState s) throws CEPException
  {
    ITuplePtr oldestTuple = null;
    Long expiryTs = null;
    
    // A flag to check if there is any tuple pending to expire; 
    // isOldestTupleExpired will be true if there are no pending tuple to expire
    boolean isOldestTupleExpired = false;
    
    // A flag to check if there are any pending plus tuple which are waiting
    // to propagate to output.
    boolean isPendingPlusTuples = false;
    
    oldestTuple = expiryTimeOrderedElements.peek();
    
    // Check if the tuple should be expired or not
    // Return expirtyTs if the tuple is getting expired
    // else return null
    if(oldestTuple != null)
    {
      expiryTs = isTupleExpired(s, oldestTuple);
      isOldestTupleExpired = (expiryTs == null);
      
      ITuplePtr pendingPlusTuple = pendingElements.peekLast();
      // If there are any pending PLUS tuples whose output timestamp is less than
      // expiry time of next MINUS tuple,
      // then don't expire next MINUS tuple
      if(pendingPlusTuple != null)
      {
        Long pendingPlusTupleVisibleTs = isTupleVisible(s, pendingPlusTuple);
        if(pendingPlusTupleVisibleTs != null && expiryTs != null)
        {
          isPendingPlusTuples = pendingPlusTupleVisibleTs <= expiryTs;
        }
      }
    }
    
    // Expire next minus only if
    // 1) there are pending minus tuple to expire who are visible
    // 2) there are no pending plus tuple with same or less timestamp than
    //    next expiry timestamp.
    while(oldestTuple != null && !isOldestTupleExpired && !isPendingPlusTuples)
    {
      // Update the priority queue
      expiryTimeOrderedElements.remove();
      long outputTs = expiryTs;
      
      // Update the output synopsis
      s.tupleLineage[0] = oldestTuple;
      outSynIter = outSynopsis.getScan_l(s.tupleLineage);
      s.nextOutputTuple = outSynIter.getNext();
      outSynopsis.deleteTuple(s.nextOutputTuple);                
      outSynopsis.releaseScan_l(outSynIter);
      
      // Get next oldest tuple         
      oldestTuple = expiryTimeOrderedElements.peek(); 
      expiryTs = oldestTuple != null ? isTupleExpired(s, oldestTuple) : null;
      isOldestTupleExpired = (expiryTs == null);
      
      // total ordering flag will be true only if the current input
      // 1) is a heartbeat
      // 1) has total ordering flag true OR having timestamp higher than expiry
      //    timestamp of this last expiring tuple
      // 2) we are expiring the last tuple from queue 
      boolean isTotalOrderGuarantee =  
        s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT &&
        (s.currentTotalOrderingFlag || 
         (!s.currentTotalOrderingFlag && s.inputTs > outputTs)) && 
        isOldestTupleExpired;
        
      // Propagate the output tuple to output queue
      outputTuple(s.nextOutputTuple, 
                  QueueElement.Kind.E_MINUS, 
                  s, 
                  isTotalOrderGuarantee,
                  outputTs);
      
      tupleStorageAlloc.release(s.nextOutputTuple);
    }
  }

  /**
   * Send the output tuple
   * @param outputTuple
   * @param kind
   * @param s
   * @param isTotalOrderingGuarantee
   * @throws CEPException
   */
  private void outputTuple(ITuplePtr outputTuple, Kind kind,
      VariableRangeWindowState s, boolean isTotalOrderingGuarantee, 
      long outputTs)
    throws CEPException
  {
    s.outputElement.setTuple(outputTuple);
    s.outputElement.setTs(outputTs);    
    s.outputElement.setKind(kind);
    s.outputElement.setTotalOrderingGuarantee(isTotalOrderingGuarantee);
    s.lastOutputTs = outputTs;
    outputQueue.enqueue(s.outputElement);
    if(s.outputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
      s.stats.incrNumOutputHeartbeats();
    else
      s.stats.incrNumOutputs();
  }

  /**
   * Check if the parameter tuple can be expired or not 
   * @param s
   * @param currentTuple
   * @return the expiry time OR null if the tuple is not expired yet
   * @throws CEPException
   */
  private Long isTupleExpired(VariableRangeWindowState s,
                              ITuplePtr currentTuple)
    throws CEPException
  {
    ITuple o = currentTuple.pinTuple(IPinnable.READ);
    
    // Throw Soft Exception if range expression value is null
    if(o.isAttrNull(expTsPos))
      throw new SoftExecException(
        ExecutionError.INVALID_TIMESTAMP_COLUMN_VALUE);
        
    Long expiryTs;
    if(slideAmount > 1)
    {
      long elementTs = o.longValueGet(elementTimePos);
      long rangeVal = o.longValueGet(expTsPos) - elementTs;
      expiryTs = getVisibleTs(elementTs + rangeVal);
    }
    else
    {
      expiryTs = o.longValueGet(expTsPos);
    }
    
    if(expiryTs > s.inputTs)
    {
      currentTuple.unpinTuple();
      return null;
    }
    else
    {
      currentTuple.unpinTuple();
      return expiryTs;
    }
  }

  private long getVisibleTs(long expTs)
  {
    if(slideAmount > 1)
    {
      long t = expTs / slideAmount;
      if((expTs % slideAmount) == 0)
        return(t*slideAmount);
      else
        return((t+1)*slideAmount);
    }
    else
    {
      return expTs;
    }
    
  }


  /**
   * Handle the PLUS input element
   * @param s
   * @throws CEPException
   */
  private void handlePlus(VariableRangeWindowState s) throws CEPException
  {
    // check the current output queue and see if any tuple need to be expired
    handleHeartbeat(s);
    
    s.outputTuple = tupleStorageAlloc.allocate();
    
    // Calculate the range expression
    evalContext.bind(s.inputTuple, IEvalContext.INPUT_ROLE);
    evalContext.bind(s.outputTuple, EvalContext.NEW_OUTPUT_ROLE);
    expTsEval.eval(evalContext);
    
    // Validate the range values in the tuple
    validateTuple(s.outputTuple);
    
    // Check if current tuple should exist in window or not
    boolean isTupleExpired = 
      isTupleExpired(s, s.outputTuple) != null;
    
    if(!isTupleExpired)
    {
      Long visibleTs = isTupleVisible(s, s.outputTuple);
      boolean isTupleVisible = visibleTs != null;
      
      if(isTupleVisible)
      {
        // Update the output synopsis
        s.tupleLineage[0] = s.outputTuple;     
        outSynopsis.insertTuple(s.outputTuple, s.tupleLineage);
        
        // propagate the output with input tuple's guarantee flag
        boolean isTotalOrderGuarantee = s.currentTotalOrderingFlag;
        
        // Propagate the output tuple to output queue
        outputTuple(s.outputTuple, 
                    QueueElement.Kind.E_PLUS, 
                    s,
                    isTotalOrderGuarantee,
                    visibleTs);
        
        // Update the priority queue
        expiryTimeOrderedElements.add(s.outputTuple);
      }
      else
      {
        pendingElements.add(s.outputTuple);
      }
      
    }
    else
    {
      tupleStorageAlloc.release(s.outputTuple);
    }
  }

  

  /**
   * Do the following checks on given tuple
   * 1) Range should not be a negative value
   * 2) Slide should not be greater than range
   * @param outputTuple
   */
  private void validateTuple(ITuplePtr currentTuple) throws CEPException
  {
    ITuple o = currentTuple.pinTuple(IPinnable.READ);
    long expTs     = o.longValueGet(expTsPos);
    long elementTs = o.longValueGet(elementTimePos);
    long rangeVal  = expTs - elementTs;
    
    // Check-1 (For Negative Range Values)    
    if(rangeVal < 0)
    {
      throw new CEPException(ExecutionError.NEGATIVE_RANGE_VALUE, 
                             new Object[]{rangeVal});
    }
    
    // Check-2 (For Range-Slide Values)
    if(slideAmount > rangeVal)
    {
      throw new CEPException(ExecutionError.SLIDE_GREATER_THAN_RANGE,
                             new Object[]{slideAmount, rangeVal});
    }
    
    currentTuple.unpinTuple();
  }


  public void setEvalContext(IEvalContext evalContext)
  {
    this.evalContext = evalContext;
  }

  public void setOutSynopsis(LineageSynopsis outSynopsis)
  {
    this.outSynopsis = outSynopsis;
  }

  public void setExpTsEval(IAEval expTsEval)
  {
    this.expTsEval = expTsEval;
  }

  @Override
  public void deleteOp()
  {
    // TODO Auto-generated method stub
  }
 
  /**
   * @param slideAmount the slideAmount to set
   */
  public void setSlideAmount(long slideAmount)
  {
    this.slideAmount = slideAmount;
  }

  /**
   * @param pendingElements the pendingElements to set
   */
  public void setPendingElements(LinkedList<ITuplePtr> pendingElements)
  {
    this.pendingElements = pendingElements;
  }


  /**
   * @param expiryTimeOrderedElements the expiryTimeOrderedElements to set
   */
  public void setExpiryTimeOrderedElements(
      PriorityQueue<ITuplePtr> expiryTimeOrderedElements)
  {
    this.expiryTimeOrderedElements = expiryTimeOrderedElements;
  }


  /**
   * @param expiryTsPos the expTsPos to set
   */
  public void setExpTsPos(int expiryTsPos)
  {
    this.expTsPos = expiryTsPos;
  }


  /**
   * @param elementTimePos the elementTimePos to set
   */
  public void setElementTimePos(int elementTimePos)
  {
    this.elementTimePos = elementTimePos;
  }
}
