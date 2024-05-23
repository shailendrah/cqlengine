/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/Minus.java /main/20 2012/06/18 06:29:07 udeshmuk Exp $ */

/* Copyright (c) 2007, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    05/22/12 - make getInputQueue public
    vikshukl    08/17/11 - XbranchMerge vikshukl_bug-11939700_ps5 from
                           st_pcbpel_11.1.1.4.0
    vikshukl    08/15/11 - fix 11939700: intersect bug
    anasrini    12/19/10 - replace eval() with eval(ec)
    sborah      06/12/09 - Memory Optimization
    sbishnoi    05/21/09 - fixing piggyback flag set
    sbishnoi    05/11/09 - require hbt from system timestamped lineage
    sborah      05/08/09 - fix concurrency issue , refer bug 8500610
    udeshmuk    04/13/09 - add getDebugInfo to assertion
    parujain    04/10/09 - fix heartbeat
    udeshmuk    04/07/09 - total ordering optimization
    anasrini    01/15/09 - fix out of order timestamp issue
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    sborah      09/25/08 - update stats
    sbishnoi    08/18/08 - changing isHeartbeat Pending
    sbishnoi    06/29/08 - overriding isHeartbeatPending
    najain      04/16/08 - add isSilentInput
    hopark      02/28/08 - resurrect refcnt
    hopark      12/07/07 - cleanup spill
    parujain    12/17/07 - db-join
    hopark      10/30/07 - remove IQueueElement
    hopark      10/21/07 - remove TimeStamp
    parujain    10/04/07 - delete op
    sbishnoi    09/26/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/Minus.java /main/20 2012/06/18 06:29:07 udeshmuk Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.util.LinkedList;
import java.util.logging.Level;

import oracle.cep.common.Constants;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.execution.queues.Queue;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.service.ExecContext;
import oracle.cep.execution.synopses.ExecSynopsis;
import oracle.cep.execution.synopses.RelationSynopsis;
import oracle.cep.execution.synopses.LineageSynopsis;


public class Minus extends ExecOpt
{
  /** Synopsis for the output */
  private LineageSynopsis   outSyn;

  /** Synopsis for left input*/
  private RelationSynopsis   leftInputSyn;
  
  /** Synopsis for right input */
  private RelationSynopsis   rightInputSyn;
  
  /** Full Scan identifier for left input synopsis */
  private int                leftFullScanId;
  
  /** Full Scan identifier for right input synopsis */
  private int                rightFullScanId;
  
  /** Index Scan identifier for left input synopsis*/
  private int                leftSelectedScanId;
  
  /** Index Scan identifier for right input synopsis*/
  private int                rightSelectedScanId;
  
  private int                rightInputScanId;

  /** Full Scan identifier */
  private int                fullScanId;

  private IAllocator<ITuplePtr>   leftInputFactory;
  
  private IAllocator<ITuplePtr>   rightInputFactory;

  /** lineage information */
  ITuplePtr[]                 lineage;

  /** Left input queue */
  private Queue              leftInputQueue;

  /** Right input queue */
  private Queue              rightInputQueue;

  /* does the left/right operator only depend on silent Relations */
  boolean                    leftSilentRelns;

  boolean                    rightSilentRelns;

  // The list of silent Relations that the operator depends on: This is needed
  // to propagate the heartbeat in case of a stall or a silent relation.
  // Currently, silent streams/relations are not handled, only static relations
  // (one for which the time is not specifed) and handled appropriately.
  LinkedList<RelSource>      leftInputRelns;

  LinkedList<RelSource>      rightInputRelns;

  /** Evaluation context in which all the action takes place */
  IEvalContext                evalContext;
  
  IAEval                      outEval;

  /** left input store */
  private IAllocator<ITuplePtr> leftTupleStorageAlloc;

  /** right input store */
  private IAllocator<ITuplePtr> rightTupleStorageAlloc;

  private boolean               isNotInSetOp;
  
  private boolean               isRightPlus;
  
  
  /**
   * Constructor for Minus
   * @param ec TODO
   */
  public Minus(ExecContext ec)
  {
    super(ExecOptType.EXEC_MINUS, new MinusState(ec), ec);
    lineage     = new ITuplePtr[1];
    isRightPlus = false;
  }

  /**
   * Set Minus.isNotInSetOp
   * @param isNotInSetOp 
   */
  public void setIsNotInSetOp(boolean isNotInSetOp)
  {
    this.isNotInSetOp = isNotInSetOp;
  }
  
  /**
   * Get Minus.isNotInSetOp
   * @return true if this is NOTIN set operation
   *         false if this is MINUS set operation
   */     
  public boolean getIsNotInSetOp()
  {
    return this.isNotInSetOp;
  }
  /**
   * @param syn
   *          Sets outSyn
   */
  public void setOutSyn(LineageSynopsis outSyn)
  {
    this.outSyn = outSyn;
  }
  
  /**
   * Sets Minus.leftInputSyn
   * @param leftInputSyn
   */
  public void setLeftInputSyn(RelationSynopsis leftInputSyn)
  {
    this.leftInputSyn = leftInputSyn;
  }
  
  public void setLeftInputTupleFactory(IAllocator<ITuplePtr> leftInputFactory)
  {
    this.leftInputFactory = leftInputFactory;
  }
  
  /**
   * Sets Minus.rightInputSyn
   * @param rightInputSyn
   */
  public void setRightInputSyn(RelationSynopsis rightInputSyn)
  {
    this.rightInputSyn = rightInputSyn;
  }

  public void setRightInputTupleFactory(IAllocator<ITuplePtr> rightInputFactory)
  {
    this.rightInputFactory = rightInputFactory;
  }
  
  /**
   * @return Returns the fullScanId.
   */
  public int getFullScanId()
  {
    return fullScanId;
  }

  /**
   * @param fullScanId
   *          The fullScanId to set.
   */
  public void setFullScanId(int fullScanId)
  {
    this.fullScanId = fullScanId;
    this.propScanId = fullScanId;
  }

  public int getLeftFullScanId()
  {
    return this.leftFullScanId;
  }
  
  public void setLeftFullScanId(int leftFullScanId)
  {
    this.leftFullScanId = leftFullScanId;
  }
  
  public int getRightFullScanId()
  {
    return this.rightFullScanId;
  }
  
  public void setRightFullScanId(int rightFullScanId)
  {
    this.rightFullScanId = rightFullScanId;
  }
  
  public int getLeftSelectedScanId()
  {
    return this.leftSelectedScanId;
  }
  
  public void setLeftSelectedScanId(int leftSelectedScanId)
  {
    this.leftSelectedScanId = leftSelectedScanId;
  }
  
  public int getRightSelectedScanId()
  {
    return this.rightSelectedScanId;
  }
  
  public void setRightSelectedScanId(int rightInputScanId)
  {
    this.rightSelectedScanId = rightInputScanId;
  }
  
  public int getRightInputScanId()
  {
    return this.rightInputScanId;
  }
  
  public void setRightInputScanId(int rightInputScanId)
  {
    this.rightInputScanId = rightInputScanId;
  }

  /**
   * Sets left InputQueue
   * 
   * @param leftQueue
   *          Sets leftInputQueue
   */
  public void setLeftInputQueue(Queue leftQueue)
  {
    this.leftInputQueue = leftQueue;
  }

  /**
   * Sets right InputQueue
   * 
   * @param rightQueue
   *          Sets rightInputQueue
   */
  public void setRightInputQueue(Queue rightQueue)
  {
    this.rightInputQueue = rightQueue;
  }

  /**
   * @param evalContext
   *          Sets evalContext
   */
  public void setEvalContext(IEvalContext ctx)
  {
    this.evalContext = ctx;
  }

  /**
   * @param outEval
   *          Sets outEval
   */
  public void setOutEval(IAEval outEval)
  {
    this.outEval = outEval;
  }

  /**
   * Setter for leftTupleStorageAlloc
   * 
   * @param leftTupleStorageAlloc
   *          The leftTupleStorageAlloc to set.
   */
  public void setLeftTupleStorageAlloc(IAllocator<ITuplePtr> leftTupleStorageAlloc)
  {
    this.leftTupleStorageAlloc = leftTupleStorageAlloc;
  }

  /**
   * Setter for rightTupleStorageAlloc
   * 
   * @param rightTupleStorageAlloc
   *          The rightTupleStorageAlloc to set.
   */
  public void setRightTupleStorageAlloc(
      IAllocator<ITuplePtr> rightTupleStorageAlloc)
  {
    this.rightTupleStorageAlloc = rightTupleStorageAlloc;
  }

  /**
   * Setter for leftSilentRelns
   * 
   * @param leftSilentRelns
   *          The leftSilentRelns to set.
   */
  public void setLeftSilentRelns(boolean leftSilentRelns)
  {
    this.leftSilentRelns = leftSilentRelns;
  }

  public void addLeftInputRelns(RelSource execOp)
  {
    if (leftInputRelns == null)
      leftInputRelns = new LinkedList<RelSource>();

    leftInputRelns.add(execOp);
  }

  /**
   * Setter for rightSilentRelns
   * 
   * @param rightSilentRelns
   *          The rightSilentRelns to set.
   */
  public void setRightSilentRelns(boolean rightSilentRelns)
  {
    this.rightSilentRelns = rightSilentRelns;
  }

  public void addRightInputRelns(RelSource execOp)
  {
    if (rightInputRelns == null)
      rightInputRelns = new LinkedList<RelSource>();

    rightInputRelns.add(execOp);
  }

  /*
   * Run execution operator
   */
  public int run(int timeSlice) throws ExecException
  {
    
    int numElements = timeSlice;
    MinusState s = (MinusState) mut_state;
    boolean exitState = true;

    assert s.state != ExecState.S_UNINIT;

    boolean done = false;
    
    //handle stats
    s.stats.incrNumExecutions();
    
    try
    {
      numElements = timeSlice;
      while ((s.stats.getNumInputs() < numElements) || (!exitState))
      {
        switch (s.state)
        {
          case S_PROPAGATE_OLD_DATA:
            setExecSynopsis((ExecSynopsis)outSyn);
            handlePropOldData();
            break;
            
          case S_INIT:
          {
            QueueElement leftPeekElement, rightPeekElement;

            // Peek to revise min left timestamp estimate
            leftPeekElement = leftInputQueue.peek(s.leftElementBuf);
            if (leftPeekElement != null)
            {
              s.leftMinTs = leftPeekElement.getTs();
            }
            else
              // Minimum timestamp possible on the next left element
              s.leftMinTs = s.minNextLeftTs;

            // Peek to revise min right timestamp estimate
            rightPeekElement = rightInputQueue.peek(s.rightElementBuf);
            if (rightPeekElement != null)
            {
              s.rightMinTs = rightPeekElement.getTs();
            }
            else
              // Minimum timestamp possible on the next right element
              s.rightMinTs = s.minNextRightTs;

            if(leftPeekElement == null && rightPeekElement == null)
            {
              s.state = ExecState.S_GENERATE_HEARTBEAT;
            }
            else if(leftPeekElement == null)
            {
              // REMEMBER: right queue is non empty
              if(s.leftMinTs < s.rightMinTs)
              {
                // right input queue has some tuples which are waiting for 
                // input tuples from left queue
                if(outerSystsSourceLineage != null)
                {
                  if(outerSystsSourceLineage.size() > 0)
                  {
                    requestForHeartbeat(outerSystsSourceLineage, 
                                        Constants.OUTER,
                                        rightPeekElement.getTs() );
                  }
                }
                 s.state = ExecState.S_GENERATE_HEARTBEAT;
              }
              else if (s.leftMinTs >= s.rightMinTs)
              {
                s.rightElement = rightInputQueue.dequeue(s.rightElementBuf);

                s.state = ExecState.S_INNER_INPUT_DEQUEUED;
              }
              else 
              {
                s.state = ExecState.S_INIT;
                done = true;
                break;
              }
            }
            else if(rightPeekElement == null)
            {
              // REMEMBER : left queue is non empty
              if(s.rightMinTs < s.leftMinTs)
              {
                // left queue has some elements which are waiting for an input
                // from right queue
                if(innerSystsSourceLineage != null)
                {
                  if(innerSystsSourceLineage.size() > 0)
                  {
                    requestForHeartbeat(innerSystsSourceLineage, 
                                        Constants.INNER, 
                                        leftPeekElement.getTs());
                  }
                }
                s.state = ExecState.S_GENERATE_HEARTBEAT;
              }
              else if(s.rightMinTs >= s.leftMinTs)
              {
                s.leftElement = leftInputQueue.dequeue(s.leftElementBuf);
                s.state = ExecState.S_OUTER_INPUT_DEQUEUED;
              }       
              else 
              {
                s.state = ExecState.S_INIT;
                done = true;
                break;
              }       
            }
            else if (s.leftMinTs < s.rightMinTs)
            {
              s.leftElement = leftInputQueue.dequeue(s.leftElementBuf);
              s.state = ExecState.S_OUTER_INPUT_DEQUEUED;
            }
            else if (s.rightMinTs < s.leftMinTs)
            {
              s.rightElement = rightInputQueue.dequeue(s.rightElementBuf);
              s.state = ExecState.S_INNER_INPUT_DEQUEUED;
            }
            else
            { /* If both input tuples have same time stamp then prefer
                 choose left input tuple.
              */
              s.leftElement = leftInputQueue.dequeue(s.leftElementBuf);
              s.state = ExecState.S_OUTER_INPUT_DEQUEUED;
            }
            break;
          }
          
          case S_OUTER_INPUT_DEQUEUED:
            assert s.rightElement == null;
            
            if(s.leftElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
              s.stats.incrNumInputHeartbeats();
            else
              s.stats.incrNumInputs();
             
            // update left ts
            s.leftTs = s.leftElement.getTs();

            // We should have a progress of time.
            if (s.lastLeftTs > s.leftTs)
            {
              s.state = ExecState.S_INIT;
              throw ExecException.OutOfOrderException(
                      this,
                      s.lastLeftTs, 
                      s.leftTs, 
                      s.leftElement.toString());
            }
            
            exitState = false;
            
            assert s.leftTs >= s.minNextLeftTs : getDebugInfo(s.leftTs,
              s.minNextLeftTs, s.leftElement.getKind().name(),
              s.lastLeftKind.name());
            // Update the last input Ts now
            s.lastLeftTs = s.leftTs;
            s.lastInputTs = s.lastLeftTs;
            s.lastLeftKind = s.leftElement.getKind();
            s.lastInputTotalOrderingFlag = s.leftElement.getTotalOrderingGuarantee();
            s.minNextLeftTs = s.lastInputTotalOrderingFlag ? s.leftTs + 1 :
                                                             s.leftTs;
                
            s.nextOutputTs = s.leftElement.getTs();
            s.leftTuple = s.leftElement.getTuple();
            s.nextElementKind = s.leftElement.getKind();

            if (s.nextElementKind == QueueElement.Kind.E_PLUS)
            {
              assert s.leftTuple != null;
              evalContext.bind(s.leftTuple, IEvalContext.INPUT_ROLE);
              
              // get the count tuple, and go back to processing left plus
              s.state = ExecState.S_PROCESS_OUTER_PLUS;
              break;
            }
            else if (s.nextElementKind == QueueElement.Kind.E_MINUS)
            {
              assert s.leftTuple != null;
              evalContext.bind(s.leftTuple, IEvalContext.INPUT_ROLE);
              
              // get the count tuple, and go back to processing left plus
              s.state = ExecState.S_PROCESS_OUTER_MINUS;
              break;
            }
            // nothing to be done for heartbeats
            else{
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            }
            
            break;
            
          case S_PROCESS_OUTER_PLUS:
            // s.leftTuple is already bind to INPUT ROLE
            // Insert left tuple into LEFT INPUT SYNOPSIS            
            leftInputSyn.insertTuple(s.leftTuple);
            lineage[0] = s.leftTuple;
            s.state    = ExecState.S_PROCESSING1;
            s.tmpState = ExecState.S_PROCESSING3;
            break;
          
          case S_PROCESS_OUTER_MINUS:
            // s.leftTuple is already bind to INPUT ROLE
            // Delete s.leftTuple from Left Input synopsis
            leftInputSyn.deleteTuple(s.leftTuple);
            lineage[0] = s.leftTuple;
            s.state    = ExecState.S_PROCESSING1;
            s.tmpState = ExecState.S_PROCESSING4;
            break;
            
          case S_PROCESSING1:
            // Search tuple inside RIGHT INPUT SYNOPSIS
            // s.leftTuple is binded to INPUT ROLE
            // If RelSetOp = NOT IN then do an index scan over selected columns
            // If RelSetOp = MINUS then do an index scan over all columns
            if(getIsNotInSetOp())
              s.tupleIter = rightInputSyn.getScan(rightSelectedScanId);
            else
              s.tupleIter = rightInputSyn.getScan(rightFullScanId);
            
            s.searchedTuple = s.tupleIter.getNext();
            if(s.searchedTuple == null)
            {
              s.state    = s.tmpState;
              s.tmpState = null;
            }
            else
            {
              rightInputFactory.release(s.searchedTuple);
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            }
            break;
            
          case S_PROCESSING3:
            if(s.leftTuple != null)
            { //output for outer_plus
              evalContext.bind(s.leftTuple, IEvalContext.SYN_ROLE);
            }
            else if(s.rightTuple != null)
            { //output for inner_minus
              assert s.searchedTuple != null;
              evalContext.bind(s.searchedTuple, IEvalContext.SYN_ROLE);
            }
            s.nextElementKind = QueueElement.Kind.E_PLUS;
            s.state = ExecState.S_ALLO_POPU_OUTPUT_TUPLE;
            break;
            
          case S_PROCESSING4:
            // Find Output Tuple inside Output Synopsis where Lineage = s.leftTuple
            TupleIterator minusScan = outSyn.getScan_l(lineage);
            s.outputTuple = minusScan.getNext();
            outSyn.releaseScan_l(minusScan);

            assert s.outputTuple != null;
            s.nextElementKind = QueueElement.Kind.E_MINUS;
            s.state = ExecState.S_PROCESSING2;
            break;
            
          
          case S_INNER_INPUT_DEQUEUED:            
            assert s.leftElement == null;
            
            if(s.rightElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
              s.stats.incrNumInputHeartbeats();
            else
              s.stats.incrNumInputs();
            
            // update right ts
            s.rightTs = s.rightElement.getTs();

            // We should have a progress of time.
            if (s.lastRightTs > s.rightTs)
            {
              s.state = ExecState.S_INIT;
              throw ExecException.OutOfOrderException(
                      this,
                      s.lastRightTs, 
                      s.rightTs, 
                      s.rightElement.toString());
            }
            
            exitState = false;
            
            assert s.rightTs >= s.minNextRightTs : getDebugInfo(s.rightTs, 
              s.minNextRightTs, s.rightElement.getKind().name(),
              s.lastRightKind.name());
            // Update the last input Ts now
            s.lastRightTs = s.rightTs;
            s.lastInputTs = s.lastRightTs;
            s.lastRightKind = s.rightElement.getKind();
            s.lastInputTotalOrderingFlag = s.rightElement.getTotalOrderingGuarantee();
            s.minNextRightTs = s.lastInputTotalOrderingFlag ? s.rightTs + 1:
                                                              s.rightTs;

            s.nextOutputTs = s.rightElement.getTs();
            s.rightTuple = s.rightElement.getTuple();
            if (s.rightElement.getKind() == QueueElement.Kind.E_PLUS)
            {
              assert s.rightTuple != null;
              evalContext.bind(s.rightTuple, IEvalContext.INPUT_ROLE);
              s.nextElementKind = QueueElement.Kind.E_PLUS;
              // get the count tuple, and go back to processing left plus
              s.state = ExecState.S_PROCESS_INNER_PLUS;
              break;
            }
            else if (s.rightElement.getKind() == QueueElement.Kind.E_MINUS)
            {
              assert s.rightTuple != null;
              evalContext.bind(s.rightTuple, IEvalContext.INPUT_ROLE);
              s.nextElementKind = QueueElement.Kind.E_MINUS;
              
              // get the count tuple, and go back to processing left plus
              s.state = ExecState.S_PROCESS_INNER_MINUS;
              break;
            }
            // nothing to be done for heartbeats
            else
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            
          
            break;

          case S_PROCESS_INNER_PLUS:
            // First, Search tuple inside RIGHT INPUT SYNOPSIS
            // s.rightTuple is binded to INPUT ROLE
            // If RelSetOp = NOT IN then do an index scan over selected columns
            // If RelSetOp = MINUS then do an index scan over all columns
            if(getIsNotInSetOp())
              s.tupleIter = rightInputSyn.getScan(rightInputScanId);
            else
              s.tupleIter = rightInputSyn.getScan(rightFullScanId);
            
            // #(11939700): invoke getnext on the iterator first before 
            // inserting.
            s.searchedTuple = s.tupleIter.getNext();
            rightInputSyn.insertTuple(s.rightTuple);

            this.isRightPlus = true;
            // searchedTuple = null means 1st occurance of tuple in RightInpSyn
            if(s.searchedTuple == null)
              s.state = ExecState.S_PROCESSING5;
            else
            {
              rightInputFactory.release(s.searchedTuple);
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            }
            break;
            
          case S_PROCESSING5:
            // Search tuple inside LEFT INPUT SYNOPSIS
            // s.rightTuple | s.leftTuple is bind to INPUT ROLE
            // If RelSetOp = NOT IN then do an index scan over selected columns
            // If RelSetOp = MINUS then do an index scan over all columns
            if(getIsNotInSetOp())
              s.tupleIter = leftInputSyn.getScan(leftSelectedScanId);
            else
              s.tupleIter = leftInputSyn.getScan(leftFullScanId);
            
            s.searchedTuple = s.tupleIter.getNext();
            if(s.searchedTuple == null)
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            else
            {
              this.lineage[0] = s.searchedTuple;
              if(s.rightTuple != null && this.isRightPlus)
              {
                s.state    = ExecState.S_PROCESSING4;
                s.tmpState = ExecState.S_PROCESSING6;
              }
              else if(s.rightTuple != null && !(this.isRightPlus))
              {
                s.state    = ExecState.S_PROCESSING3;
                s.tmpState = ExecState.S_PROCESSING6;
              }
            }
            break;
            
          case S_PROCESSING6:
           //NOTE : next searchedTuple is already scanned
            if(s.searchedTuple != null)
            {
              this.lineage[0] = s.searchedTuple;
              if(s.rightTuple != null && this.isRightPlus)
              {
                s.state    = ExecState.S_PROCESSING4;
                s.tmpState = ExecState.S_PROCESSING6;
              }
              else if(s.rightTuple != null && !(this.isRightPlus))
              {
                s.state    = ExecState.S_PROCESSING3;
                s.tmpState = ExecState.S_PROCESSING6;
              }
            }
            else
            {
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;
              s.tmpState = null;
            }
            break;
            
          case S_PROCESS_INNER_MINUS:
            rightInputSyn.deleteTuple(s.rightTuple);
            
            // Now, Search tuple inside RIGHT INPUT SYNOPSIS to check
            // whether It was last occurence
            
            // s.rightTuple is binded to INPUT ROLE
            // If RelSetOp = NOT IN then do an index scan over selected columns
            // If RelSetOp = MINUS then do an index scan over all columns
            if(getIsNotInSetOp())
              s.tupleIter = rightInputSyn.getScan(rightInputScanId);
            else
              s.tupleIter = rightInputSyn.getScan(rightFullScanId);
            
            s.searchedTuple  = s.tupleIter.getNext();                                  
            this.isRightPlus = false;            
            // searchedTuple = null means last occurance of tuple in RightInpSyn            
            if(s.searchedTuple == null)
              s.state = ExecState.S_PROCESSING5;
            else
            {
              rightInputFactory.release(s.searchedTuple);
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            }
            break;
         
            
          case S_GENERATE_HEARTBEAT:
            if (! ((s.lastOutputTs < s.leftMinTs) &&
                   (s.lastOutputTs < s.rightMinTs)
                  ) )
            {
              s.state = ExecState.S_INIT;
              done = true;
              break;
            }
            s.nextOutputTs  = s.leftMinTs < s.rightMinTs ? s.leftMinTs:
                                                          s.rightMinTs;

            s.outputTuple = null;
            s.nextElementKind = QueueElement.Kind.E_HEARTBEAT;
            s.state = ExecState.S_ALLOCATE_ELEM;            
            break;

          case S_ALLO_POPU_OUTPUT_TUPLE:
            // Assumed that the Tuple(to be output) is bound to the SYN_ROLE
            s.outputTuple = tupleStorageAlloc.allocate();
            s.state = ExecState.S_OUTPUT_TUPLE;

          case S_OUTPUT_TUPLE:
            evalContext.bind(s.outputTuple, IEvalContext.NEW_OUTPUT_ROLE);
            outEval.eval(evalContext);
            s.state = ExecState.S_PROCESSING2;
            break;

          case S_PROCESSING2:
            assert ((s.leftElement == null) ^ (s.rightElement == null));
            if (s.nextElementKind == QueueElement.Kind.E_PLUS)
              outSyn.insertTuple(s.outputTuple, lineage);
            else
              outSyn.deleteTuple(s.outputTuple);

            if(s.searchedTuple != null)
            {
              if(s.rightTuple != null)
                leftInputFactory.release(s.searchedTuple);
              if(s.leftTuple != null)
                rightInputFactory.release(s.searchedTuple);
              s.searchedTuple = null;
            }
            s.searchedTuple = null;
            if((s.rightTuple != null) && (s.tmpState != null))
            { //processing inner 
              //scan the next tuple
              s.searchedTuple = s.tupleIter.getNext();
            }
            // enqueue into output queue
            s.state = ExecState.S_ALLOCATE_ELEM;

          case S_ALLOCATE_ELEM:
            
            s.outputElement.setKind(s.nextElementKind);
            s.state = ExecState.S_OUTPUT_TIMESTAMP;

          case S_OUTPUT_TIMESTAMP:
          {
            s.outputElement.setTs(s.nextOutputTs);
            s.outputElement.setTuple(s.outputTuple);
            s.state = ExecState.S_OUTPUT_READY;
          }

          case S_OUTPUT_READY:            
            if (outputQueue.isFull())
            {
              done = true;
              break;
            }

            s.lastOutputTs = s.nextOutputTs;
            boolean nextTotalOrderingGuarantee
              = s.nextOutputTs < s.minNextLeftTs && 
                s.nextOutputTs < s.minNextRightTs;

            if (s.outputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
            {
              s.outputElement.setTotalOrderingGuarantee(nextTotalOrderingGuarantee);
              outputQueue.enqueue(s.outputElement);
              s.stats.incrNumOutputHeartbeats();
              done = true;
              s.state = ExecState.S_INIT;
              break;
            }
            else
            {
              // set guarantee to TRUE only if the this is the last round of 
              // processing for current input tuple
              nextTotalOrderingGuarantee = 
                nextTotalOrderingGuarantee && s.searchedTuple == null;
              
              outputQueue.enqueue(s.outputElement);
              s.state = ExecState.S_OUTPUT_ENQUEUED;
            }

          case S_OUTPUT_ENQUEUED:
            s.stats.incrNumOutputs();
            
            // Include support for iterator
            if(s.rightTuple != null && s.tmpState != null)
              s.state = s.tmpState;
            else
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            
            break;
            
          case S_INPUT_ELEM_CONSUMED:
          {
            QueueElement elem;

            assert ((s.leftElement == null) ^ (s.rightElement == null));
            if (s.leftElement != null)
              elem = s.leftElement;
            else
              elem = s.rightElement;

            assert elem != null;

            if (s.leftElement != null)
            {
              if (s.leftTuple != null)
              {
                leftTupleStorageAlloc.release(s.leftTuple);
                s.leftTuple = null;
              }
            } 
            else
            {
              if (s.rightTuple != null)
              {
                rightTupleStorageAlloc.release(s.rightTuple);
                s.rightTuple = null;
              }
            }
            
            elem = null;
            s.leftElement = null;
            s.rightElement = null;
            exitState = true;
            s.state = ExecState.S_INIT;
            break;
          }

          default:
            assert false;
            break;
        }
        if (done)
          break;
      }
    }
    catch (SoftExecException e)
    {
      s.state = ExecState.S_INIT;
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      return 0;
    }
   
    return 0;
  }

  /*
   * Remove operator
   */
  public void deleteOp()
  {
    // TODO Auto-generated method stub

  }
  
  @Override
  public boolean isHeartbeatPending()
  {
    MinusState s = (MinusState)mut_state;
    QueueElement leftElem = leftInputQueue.peek(peekBuf1);
    QueueElement rightElem = rightInputQueue.peek(peekBuf2);
    
    if(leftElem == null && rightElem == null)
    {
      return ((s.lastOutputTs < s.leftMinTs) && 
              (s.lastOutputTs < s.rightMinTs));
    }
    else if(leftElem == null)
    {
      return (s.leftMinTs < rightElem.getTs() && s.lastOutputTs < s.leftMinTs);
    }
    else if(rightElem == null)
    {
      return (s.rightMinTs < leftElem.getTs() && s.lastOutputTs < s.rightMinTs);
    }
    else
      return false;    
  }
  
  protected int getNoInputQueues() {return 2;}

  public Queue getInputQueue(int n) 
  {
    if (n == 0) return leftInputQueue;
    if (n == 1) return rightInputQueue;
    return null;
  }

  protected long getLastTs(int n)
  {
    MinusState m = (MinusState) mut_state;
    if (n == 0) return m.lastLeftTs;
    if (n == 1) return m.lastRightTs;
    assert false : "shouldn't be called";
    return -1;
  }

  protected boolean isSilentInput(int n)
  {
    if (n == 0) return leftSilentRelns;
    if (n == 1) return rightSilentRelns;
    assert false : "shouldn't be called";
    return false;
  }
}
