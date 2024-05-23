/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/Slide.java /main/3 2013/11/29 05:16:31 sbishnoi Exp $ */

/* Copyright (c) 2012, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    05/29/12 - Creation
 */

package oracle.cep.execution.operators;

import java.util.LinkedList;
import java.util.logging.Level;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.QueueElement.Kind;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.synopses.ExecSynopsis;
import oracle.cep.execution.synopses.RelationSynopsis;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.ExecContext;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/Slide.java /main/3 2013/11/29 05:16:31 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class Slide extends ExecOpt
{
  /** slide interval specified in nanos */
  private long numSlideNanos;
  
  /** relation synopsis scan id*/  
  private int relScanId;
  
  /** Output synopsis */
  private RelationSynopsis outSyn;
  
  /** A flag to track if the timestamp has progressed or not on the new input*/
  private boolean isTimeProgressed;
  
  /** A collection of pending elements which are waiting to emit as output */
  private LinkedList<ITuplePtr> pendingInputs;
  
  /** Pending MINUS tuples which needs to send output on next timestamp change*/
  private LinkedList<ITuplePtr> pendingMinusOutputs;
  
  /** Evaluation Context for current execution operator */
  private IEvalContext evalContext;
    
  /**
   * Construct Execution Operator for SLIDE operation
   * @param ec
   */
  public Slide(ExecContext ec)
  {
    super(ExecOptType.EXEC_SLIDE, new SlideState(ec), ec);  
    pendingInputs = new LinkedList<ITuplePtr>();
    pendingMinusOutputs = new LinkedList<ITuplePtr>();
  }

  @Override
  protected int run(int timeslice) throws CEPException
  {
    SlideState s = (SlideState)mut_state;
    
    assert s.state != ExecState.S_UNINIT;

    // Update Stats
    s.stats.incrNumExecutions();    
    
    if (s.state == ExecState.S_PROPAGATE_OLD_DATA)
    {
      setExecSynopsis((ExecSynopsis)outSyn);
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
          handleMinus(s);         
        }
        
        // update the last input timestamp and input kind
        s.lastInputTs = s.inputTs;
        s.lastInputKind = s.inputElement.getKind();
        
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

  
  private void handleHeartbeat(SlideState s) throws ExecException
  {
    // If Timestamp has progressed, then check if we need to send any pending
    // elements
    if(isTimeProgressed)
    {
      handleTimestampProgress(s);
    }
    propagateHeartbeat(s);
  }


  private void handlePlus(SlideState s) throws ExecException
  {
    // If timestamp has progressed, then check if we have any pending elements
    // needs to send output.
    if(isTimeProgressed)
    {
      handleTimestampProgress(s);
    }
    
    // Determine the next output visible ts
    long visibleTs = getVisibleTs(s.inputTs);
    
    // If the next timestamp is greater than current input timestamp, then save
    // it in list of pending elements
    if(s.inputTs < visibleTs)
    {      
       pendingInputs.add(s.inputTuple);
       // As we are not emitting any output on current input tuple, check if we
       // can send a heartbeat to facilitate timestamp progress in downstream op
       propagateHeartbeat(s);
    }
    else
    {
      assert s.inputTs == visibleTs;
      
      // Send current tuple to output
      outSyn.insertTuple(s.inputTuple);
      
      // Output total ordering flag will be same as input total ordering flag
      // Reason: At this point of time t, we have no pending elements and we can
      // send this element as output with flag = true if this element itself is
      // last element of the timestamp  = t.
      boolean isTotalOrderingGuarantee = s.currentTotalOrderingFlag;
      outputTuple(s.inputTuple, 
                  Kind.E_PLUS, 
                  s, 
                  isTotalOrderingGuarantee,
                  s.inputTs);
      s.lastOutputTs = s.inputTs;
    }
  }

  private void handleMinus(SlideState s) throws ExecException
  {
    // If timestamp has progressed, then check if we can propagate any waiting
    // element to output queue.
    if(isTimeProgressed)
    {
      handleTimestampProgress(s);
    }
    
    // Get the iterator on pending inputs
    for(int i = 0; i < pendingInputs.size(); i++)
    {
      ITuplePtr currTuple = pendingInputs.get(i);
      if(currTuple.compare(s.inputTuple))
      {
        // If the corresponding PLUS for current minus is never propagated, then
        // remove it from list of pending tuples. 
        pendingInputs.remove(i);
        // As we are not doing anything on current input, see if we can emit
        // a heartbeat to perform a timestamp progress on downstream operators.
        propagateHeartbeat(s);
        return;
      }
    }
    
    // If the tuple is not present in the list of pending elements, then it is
    // definitely a part of current output relation. So we should delete it from
    // output synopsis
    evalContext.bind(s.inputTuple, IEvalContext.INPUT_ROLE);
    s.tupIter = outSyn.getScan(relScanId);
    ITuplePtr searchedTuple = s.tupIter.getNext();
    assert searchedTuple != null;    
    outSyn.deleteTuple(searchedTuple);
    outSyn.releaseScan(relScanId, s.tupIter);
    
    long visibleTs = getVisibleTs(s.inputTs);
    
    if(s.inputTs == visibleTs)
    {
      // Output total ordering flag will be same as input total ordering flag
      // Reason: At this point of time t, we have no pending elements and we can
      // send this element as output with flag = true if this element itself is
      // last element of the timestamp  = t.
      boolean isTotalOrderingGuarantee = s.currentTotalOrderingFlag;
      outputTuple(s.inputTuple, 
                  Kind.E_MINUS, 
                  s, 
                  isTotalOrderingGuarantee,
                  s.inputTs);
      s.lastOutputTs = s.inputTs;
    }
    else
    {
      pendingMinusOutputs.add(s.inputTuple);
    }
  }
  
  private void handleTimestampProgress(SlideState s) throws ExecException
  {
    long lastVisibleTs = getVisibleTs(s.lastInputTs);
    
    if(s.inputTs < lastVisibleTs)
    {
      // We cann't emit the pending elements as the visible timestamp is higher
      // than current input timestamp
      // So we will propagate a heartbeat to keep the timestamp progress
      propagateHeartbeat(s);
    }
    else
    {
      ITuplePtr currTuple = pendingMinusOutputs.peek();
      while(currTuple != null)
      {        
        //bug 17701008 : the element should be removed before calculating
        // total ordering guarantee since it picks from that data structure.
        pendingMinusOutputs.remove();
        // Calculate total ordering guarantee flag
        boolean isTotalOrderingGuarantee = getTotalOrderGuarantee(s);
                
        outputTuple(currTuple, 
                    Kind.E_MINUS, 
                    s, 
                    isTotalOrderingGuarantee,
                    lastVisibleTs);
        s.lastOutputTs = lastVisibleTs;
        currTuple = pendingMinusOutputs.peek();
      }
      
      currTuple = pendingInputs.peek();
      while(currTuple != null)
      {
        //bug 17701008 : the element should be removed before calculating
        // total ordering guarantee since it picks from that data structure.
        pendingInputs.remove();
        // Send current tuple to output
        outSyn.insertTuple(currTuple);

        // if Timestamp progress is done by PLUS or MINUS tuple
        boolean isTotalOrderingGuarantee = getTotalOrderGuarantee(s);
        
        outputTuple(currTuple, 
                    Kind.E_PLUS, 
                    s, 
                    isTotalOrderingGuarantee,
                    lastVisibleTs);
        s.lastOutputTs = lastVisibleTs;
        currTuple = pendingInputs.peek();
      }     
    }
  }
  

  private boolean getTotalOrderGuarantee(SlideState s)
  {
    long lastVisibleTs = getVisibleTs(s.lastInputTs);
    
    boolean isTotalOrderingGuarantee = false;
    
    //Condition 1:If any pending minus or plus elements, flag will be false. 
    if(pendingMinusOutputs.peek() != null || pendingInputs.peek() != null)
    {
      isTotalOrderingGuarantee = false;
    }
    // Condition 2: If no pending element and all pending elements are being
    // sent at a timestamp less than current input timestamp
    else if(s.inputTs > lastVisibleTs)
    {
      isTotalOrderingGuarantee = true;
    }
    // Condition 3: If all elements are being sent at current input time,
    // then flag will be true only if current input is a heartbeat with
    // ordering flag true.
    else if(s.inputTs == lastVisibleTs)
    {
      isTotalOrderingGuarantee 
        = s.inputElement.getKind() == Kind.E_HEARTBEAT &&
          s.currentTotalOrderingFlag == true;
    }
    else
    {
      isTotalOrderingGuarantee = false;
    }
    
    return isTotalOrderingGuarantee;
  }
  
  private long getVisibleTs(long inputTs)
  {    
    long t = inputTs / numSlideNanos;
    if(inputTs % numSlideNanos == 0)
    {
      return t * numSlideNanos;
    }
    else
    {
      return (t+1) * numSlideNanos;
    }
  }
  
  private void propagateHeartbeat(SlideState s) throws ExecException
  {
    // If current timetsamp is higher than last output timestamp, then we can
    // send a heartbeat to downstream operators
    if((s.inputTs > s.lastOutputTs) ||
       (s.inputTs == s.lastOutputTs && s.currentTotalOrderingFlag && !s.lastOutputTotalOrderingFlag))
    {
      s.outputElement.setKind(Kind.E_HEARTBEAT);
      s.outputElement.setTs(s.inputTs);      
      s.outputElement.setTotalOrderingGuarantee(s.currentTotalOrderingFlag);
      s.lastOutputTs = s.inputTs;
      s.lastOutputTotalOrderingFlag = s.currentTotalOrderingFlag;
      outputQueue.enqueue(s.outputElement);
      s.stats.incrNumOutputHeartbeats();
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
  private void outputTuple(ITuplePtr outputTuple, 
                           Kind kind,
                           SlideState s, 
                           boolean isTotalOrderingGuarantee, 
                           long outputTs)
    throws ExecException
  {
    s.outputElement.setTuple(outputTuple);
    s.outputElement.setTs(outputTs);    
    s.outputElement.setKind(kind);
    s.outputElement.setTotalOrderingGuarantee(isTotalOrderingGuarantee);
    s.lastOutputTs = outputTs;
    s.lastOutputTotalOrderingFlag = isTotalOrderingGuarantee;
    outputQueue.enqueue(s.outputElement);   
    s.stats.incrNumOutputs();
  }



  @Override
  public void deleteOp()
  {
    // TODO Auto-generated method stub    
  }

  /**
   * @param numSlideNanos the numSlideNanos to set
   */
  public void setNumSlideNanos(long numSlideNanos)
  {
    this.numSlideNanos = numSlideNanos;
  }

  /**
   * @param outSyn the outSyn to set
   */
  public void setOutSyn(RelationSynopsis outSyn)
  {
    this.outSyn = outSyn;
  }

  /**
   * @param relScanId the relScanId to set
   */
  public void setRelScanId(int relScanId)
  {
    this.relScanId = relScanId;
  }
  
  /**
   * @param fullScanId the propScanId is a full scan
   */
  public void setPropScanId(int fullScanId)
  {
    this.propScanId = fullScanId;
  }

  /**
   * @param evalContext the evalContext to set
   */
  public void setEvalContext(IEvalContext evalContext)
  {
    this.evalContext = evalContext;
  }  
}
