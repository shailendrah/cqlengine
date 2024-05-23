/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/Except.java /main/40 2012/06/18 06:29:07 udeshmuk Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares Except in package oracle.cep.execution.operators.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 udeshmuk  05/22/12 - make getInputQueue public
 anasrini  12/19/10 - replace eval() with eval(ec)
 sborah    06/12/09 - Memory Optimization
 sbishnoi  05/11/09 - request hbt from system timestamped lineage
 sborah    05/08/09 - fix concurrency issue , refer bug 8500610
 sbishnoi  04/13/09 - fixing ordering flag
 hopark    04/06/09 - total ordering opt
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 hopark    10/07/08 - use execContext to remove statics
 sborah    09/25/08 - update stats
 sbishnoi  06/29/08 - overriding isHeartbeatPending and code alignment
 najain    04/16/08 - add isSilentInput
 hopark    02/28/08 - resurrect refcnt
 hopark    12/07/07 - cleanup spill
 hopark    10/30/07 - remove IQueueElement
 hopark    10/21/07 - remove TimeStamp
 parujain  10/04/07 - delete op
 hopark    09/07/07 - eval refactor
 hopark    07/13/07 - dump stack trace on exception
 parujain  07/03/07 - cleanup
 parujain  06/26/07 - mutable state
 hopark    06/11/07 - logging - remove ExecContext
 hopark    05/24/07 - debug logging
 hopark    05/16/07 - add arguments for OutOfOrderException
 parujain  05/08/07 - monitoring statistics
 hopark    04/20/07 - change pinTuple semantics
 hopark    04/19/07 - fix refcount
 najain    04/03/07 - bug fix
 hopark    03/23/07 - throws exception from QueueElement
 hopark    03/21/07 - add TuplePtr pin
 najain    03/16/07 - cleanup
 najain    03/14/07 - cleanup
 najain    03/12/07 - bug fix
 parujain  02/27/07 - NPE bug
 najain    02/19/07 - bug fix
 najain    01/24/07 - bug fix
 najain    01/05/07 - spill over support
 parujain  12/19/06 - fullScanId for RelationSynopsis
 parujain  12/12/06 - propagating relations
 najain    12/04/06 - stores not storage allocators
 najain    11/14/06 - free count tuple
 najain    11/07/06 - add getOldestTs
 najain    08/14/06 - handle silent relations
 najain    08/07/06 - fix except
 dlenkov   06/24/06 - implementation
 skaluska  03/14/06 - query manager 
 skaluska  02/07/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/Except.java /main/40 2012/06/18 06:29:07 udeshmuk Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.util.LinkedList;
import java.util.logging.Level;

import oracle.cep.common.Constants;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.execution.queues.Queue;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.service.ExecContext;
import oracle.cep.execution.synopses.ExecSynopsis;
import oracle.cep.execution.synopses.RelationSynopsis;

/**
 * Except
 *
 * @author skaluska
 */
public class Except extends ExecOpt
{
  /** Synopsis for the output */
  private RelationSynopsis   outSyn;

  /** Count synopsis for keeping track of tuple counts */
  private RelationSynopsis   countSyn;

  private int                outScanId;
  
  /** Full Scan identifier */
  private int              fullScanId;

  private int                countScanId;

  private IAllocator<ITuplePtr>   countFactory;

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

  IAEval                      initEval;

  IAEval                      outEval;

  IAEval                      incrEval;

  IAEval                      decrEval;

  IBEval                      posEval;

  IBEval                      negEval;

  IBEval                      nonNegEval;

  IBEval                      zeroEval;

  /** left input store */
  private IAllocator<ITuplePtr> leftTupleStorageAlloc;

  /** right input store */
  private IAllocator<ITuplePtr> rightTupleStorageAlloc;
  
  QueueElement leftPeekElement, rightPeekElement;

  /**
   * Constructor for Except
   * @param ec TODO
   */
  public Except(ExecContext ec)
  {
    super(ExecOptType.EXEC_EXCEPT, new ExceptState(ec), ec);
  }

  /**
   * @param syn
   *          Sets outSyn
   */
  public void setOutSyn(RelationSynopsis outSyn)
  {
    this.outSyn = outSyn;
  }

  /**
   * @param countSyn
   *          The countSyn to set.
   */
  public void setCountSyn(RelationSynopsis countSyn)
  {
    this.countSyn = countSyn;
  }

  /**
   * @param countFactory
   *          The countFactory to set.
   */
  public void setCountTupleFactory(IAllocator<ITuplePtr> countFactory)
  {
    this.countFactory = countFactory;
  }

  /**
   * @param countScanId
   *          The countScanId to set.
   */
  public void setCountScanId(int countScanId)
  {
    this.countScanId = countScanId;
  }

  /**
   * @param outScanId
   *          The outScanId to set.
   */
  public void setOutScanId(int outScanId)
  {
    this.outScanId = outScanId;
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
   * @param initEval
   *          Sets initEval
   */
  public void setInitEval(IAEval initEval)
  {
    this.initEval = initEval;
  }

  /**
   * @param incrEval
   *          The incrEval to set.
   */
  public void setIncrEval(IAEval incrEval)
  {
    this.incrEval = incrEval;
  }

  /**
   * @param decrEval
   *          The decrEval to set.
   */
  public void setDecrEval(IAEval decrEval)
  {
    this.decrEval = decrEval;
  }

  /**
   * @param negEval
   *          The negEval to set.
   */
  public void setNegEval(IBEval negEval)
  {
    this.negEval = negEval;
  }

  /**
   * @param nonNegEval
   *          The nonNegEval to set.
   */
  public void setNonNegEval(IBEval nonNegEval)
  {
    this.nonNegEval = nonNegEval;
  }

  /**
   * @param posEval
   *          The posEval to set.
   */
  public void setPosEval(IBEval posEval)
  {
    this.posEval = posEval;
  }

  /**
   * @param zeroEval
   *          The zeroEval to set.
   */
  public void setZeroEval(IBEval zeroEval)
  {
    this.zeroEval = zeroEval;
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
    ExceptState s = (ExceptState) mut_state;
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
            
            // Peek to revise min left timestamp estimate
            leftPeekElement = leftInputQueue.peek(s.leftElementBuf);
            if (leftPeekElement != null)
            {
              s.leftMinTs = leftPeekElement.getTs();
            }
            else
            {
              // Minimum timestamp possible on the next left element
              s.leftMinTs = s.minNextLeftTs;
            }

            // Peek to revise min right timestamp estimate
            rightPeekElement = rightInputQueue.peek(s.rightElementBuf);
            if (rightPeekElement != null)
            {
              s.rightMinTs = rightPeekElement.getTs();
            }
            else
            {
              // Minimum timestamp possible on the next right element
              s.rightMinTs = s.minNextRightTs;
            }
 
            // We have to process the left input if it has an element
            // waiting in the queue.
            if (s.leftMinTs < s.rightMinTs)
            {
              if(leftPeekElement != null)
                s.leftElement = leftInputQueue.dequeue(s.leftElementBuf);
              else
                s.leftElement = null;
              
              s.state = ExecState.S_OUTER_INPUT_DEQUEUED;
            }
            else if (s.rightMinTs < s.leftMinTs)
            {
              if(rightPeekElement != null)
                s.rightElement = rightInputQueue.dequeue(s.rightElementBuf);
              else
                s.rightElement = null;
              s.state = ExecState.S_INNER_INPUT_DEQUEUED;
            }
            else if (leftPeekElement != null)
            {
              s.leftElement = leftInputQueue.dequeue(s.leftElementBuf);
              s.state = ExecState.S_OUTER_INPUT_DEQUEUED;
            }
            else if (rightPeekElement != null)
            {
              s.rightElement = rightInputQueue.dequeue(s.rightElementBuf);
              s.state = ExecState.S_INNER_INPUT_DEQUEUED;
            }
            else
              s.state = ExecState.S_GENERATE_HEARTBEAT;
            break;
          }

          case S_PROCESSING1:
            // It is assumed that the INPUT_ROLE is already bound
            // Get the count tuple for this input tuple if it exists
            assert ((s.leftElement == null) ^ (s.rightElement == null));
            s.countScan = countSyn.getScan(countScanId);
            assert s.countScan != null;

            s.countTuple = s.countScan.getNext();

            // The count tuple does not exist
            if (s.countTuple == null)
              // allocation/population of countTuple is done in PROCESSING2
              s.state = ExecState.S_PROCESSING2;
            else
            {
              // Bind the countTuple
              evalContext.bind(s.countTuple, IEvalContext.SYN_ROLE);
              countSyn.releaseScan(countScanId, s.countScan);
              s.state = s.tmpState;
            }
            break;

          case S_PROCESSING2:
            // Create a new count tuple
            s.countTuple = countFactory.allocate();
            s.state = ExecState.S_PROCESSING3;

          case S_PROCESSING3:
            // Evaluate the countTuple
            evalContext.bind(s.countTuple, IEvalContext.SYN_ROLE);
            initEval.eval(evalContext);

            // Insert the count tuple into count synopsis
            countSyn.insertTuple(s.countTuple);
            countSyn.releaseScan(countScanId, s.countScan);
            s.state = s.tmpState;
            break;

          case S_OUTER_INPUT_DEQUEUED:
            if (s.leftElement == null)
            {
              // right queue has some elements which are waiting an input from
              // left queue
              if(outerSystsSourceLineage != null)
              {
                if(rightPeekElement != null &&
                   outerSystsSourceLineage.size() > 0)
                {
                  requestForHeartbeat(outerSystsSourceLineage, 
                                      Constants.OUTER, 
                                      rightPeekElement.getTs());
                }
              }
              s.state = ExecState.S_GENERATE_HEARTBEAT;
            }
            else
            {
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
              
              // current input timeStamp should be greater or equal to the
              // calculated expected timeStamp in previous execution
              assert s.leftTs >= s.minNextLeftTs: 
                getDebugInfo(s.leftTs, s.minNextLeftTs, 
                  s.leftElement.getKind().name(), s.lastLeftKind.name());
              
              // Update the last input Ts now
              s.lastLeftTs = s.leftTs;
              s.lastInputTs = s.lastLeftTs;
              s.lastLeftKind = s.leftElement.getKind();
              
              // set the expected timeStamp of next left input tuple
              s.minNextLeftTs = s.leftElement.getTotalOrderingGuarantee() ? 
                  s.leftTs+1 : s.leftTs;

              s.nextOutputTs = s.leftElement.getTs();
              s.leftTuple = s.leftElement.getTuple();
              s.nextElementKind = s.leftElement.getKind();

              if (s.nextElementKind == QueueElement.Kind.E_PLUS)
              {
                assert s.leftTuple != null;
                evalContext.bind(s.leftTuple, IEvalContext.INPUT_ROLE);

                // get the count tuple, and go back to processing left plus
                s.tmpState = ExecState.S_PROCESS_OUTER_PLUS;
                s.state = ExecState.S_PROCESSING1;
                break;
              }
              else if (s.nextElementKind == QueueElement.Kind.E_MINUS)
              {
                assert s.leftTuple != null;
                evalContext.bind(s.leftTuple, IEvalContext.INPUT_ROLE);

                // get the count tuple, and go back to processing left plus
                s.tmpState = ExecState.S_PROCESS_OUTER_MINUS;
                s.state = ExecState.S_PROCESSING1;
                break;
              }
              // nothing to be done for heartbeats
              else
                s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            }
            break;

          case S_PROCESS_OUTER_PLUS:
            incrEval.eval(evalContext);
            if (posEval.eval(evalContext))
              s.state = ExecState.S_ALLO_POPU_OUTPUT_TUPLE;
            else
              s.state = ExecState.S_OUTPUT_ENQUEUED;
            break;

          case S_PROCESS_OUTER_MINUS:
            decrEval.eval(evalContext);
            if (nonNegEval.eval(evalContext))
              s.state = ExecState.S_PROCESSING4;
            else
              s.state = ExecState.S_OUTPUT_ENQUEUED;
            break;

          case S_INNER_INPUT_DEQUEUED:
            if (s.rightElement == null)
            {
              // left queue has some elements which are waiting for an input
              // from right queue
              if(innerSystsSourceLineage != null)
              {
                if(leftPeekElement != null &&
                   innerSystsSourceLineage.size() > 0)
                {
                  requestForHeartbeat(innerSystsSourceLineage,
                                      Constants.INNER,
                                      leftPeekElement.getTs());
                }
              }
              s.state = ExecState.S_GENERATE_HEARTBEAT;
            }
            else
            {
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
              
              // current input timeStamp should be greater or equal to the
              // calculated expected timeStamp in previous execution
              assert s.rightTs >= s.minNextRightTs: 
                getDebugInfo(s.rightTs, s.minNextRightTs, 
                  s.rightElement.getKind().name(), s.lastRightKind.name());
                 
              // Update the last input TS now
              s.lastRightTs = s.rightTs;
              s.lastInputTs = s.lastRightTs;
              s.lastRightKind = s.rightElement.getKind();

              // set the expected timeStamp of next right input tuple
              s.minNextRightTs = s.rightElement.getTotalOrderingGuarantee() ? 
                  s.rightTs+1 : s.rightTs;
              
              s.nextOutputTs = s.rightElement.getTs();
              s.rightTuple = s.rightElement.getTuple();
              if (s.rightElement.getKind() == QueueElement.Kind.E_PLUS)
              {
                assert s.rightTuple != null;
                evalContext.bind(s.rightTuple, IEvalContext.INPUT_ROLE);
                s.nextElementKind = QueueElement.Kind.E_MINUS;

                // get the count tuple, and go back to processing left plus
                s.tmpState = ExecState.S_PROCESS_INNER_PLUS;
                s.state = ExecState.S_PROCESSING1;
                break;
              }
              else if (s.rightElement.getKind() == QueueElement.Kind.E_MINUS)
              {
                assert s.rightTuple != null;
                evalContext.bind(s.rightTuple, IEvalContext.INPUT_ROLE);
                s.nextElementKind = QueueElement.Kind.E_PLUS;

                // get the count tuple, and go back to processing left plus
                s.tmpState = ExecState.S_PROCESS_INNER_MINUS;
                s.state = ExecState.S_PROCESSING1;
                break;
              }
              // nothing to be done for heartbeats
              else
                s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            }
            break;

          case S_PROCESS_INNER_PLUS:
            decrEval.eval(evalContext);
            if (nonNegEval.eval(evalContext))
              s.state = ExecState.S_PROCESSING4;
            else
              s.state = ExecState.S_OUTPUT_ENQUEUED;
            break;

          case S_PROCESS_INNER_MINUS:
            incrEval.eval(evalContext);
            if (posEval.eval(evalContext))
              s.state = ExecState.S_ALLO_POPU_OUTPUT_TUPLE;
            else
              s.state = ExecState.S_OUTPUT_ENQUEUED;
            break;

          case S_GENERATE_HEARTBEAT:
            assert ((s.leftElement == null) && (s.rightElement == null));
            if ((s.lastOutputTs < s.leftMinTs)
                && (s.lastOutputTs < s.rightMinTs))
            {
              if (s.leftMinTs < s.rightMinTs)
                s.nextOutputTs = s.leftMinTs;
              else
                s.nextOutputTs = s.rightMinTs;
              s.outputTuple = null;
              s.nextElementKind = QueueElement.Kind.E_HEARTBEAT;
              s.state = ExecState.S_ALLOCATE_ELEM;
            }
            else
            {
              s.state = ExecState.S_INIT;
              done = true;
            }
            break;

          case S_ALLO_POPU_OUTPUT_TUPLE:
            // It is assumed that the countTuple is bound to the SYN_ROLE
            assert s.countTuple != null;
            s.outputTuple = tupleStorageAlloc.allocate();
            s.state = ExecState.S_OUTPUT_TUPLE;

          case S_OUTPUT_TUPLE:
            evalContext.bind(s.outputTuple, IEvalContext.NEW_OUTPUT_ROLE);
            outEval.eval(evalContext);
            s.state = ExecState.S_PROCESSING6;
            break;

          case S_PROCESSING4:
            // It is assumed that the SYN_ROLE is already bound
            // Get the out tuple for this count tuple
            assert (s.countTuple != null);
            s.outScan = outSyn.getScan(outScanId);
            assert s.outScan != null;

            s.outputTuple = s.outScan.getNext();
            assert s.outputTuple != null;

            outSyn.releaseScan(outScanId, s.outScan);
            s.state = ExecState.S_PROCESSING6;

          case S_PROCESSING6:
            assert ((s.leftElement == null) ^ (s.rightElement == null));
            if (s.nextElementKind == QueueElement.Kind.E_PLUS)
              outSyn.insertTuple(s.outputTuple);
            else
              outSyn.deleteTuple(s.outputTuple);

            // enqueue into output queue
            s.state = ExecState.S_ALLOCATE_ELEM;

          case S_ALLOCATE_ELEM:
            s.outputElement.setKind(s.nextElementKind);
            s.state = ExecState.S_OUTPUT_TIMESTAMP;

          case S_OUTPUT_TIMESTAMP:
          {
            s.outputElement.setTs(s.nextOutputTs);
            s.outputElement.setTuple(s.outputTuple);
            s.outputElement.setTotalOrderingGuarantee(
              s.nextOutputTs < s.minNextLeftTs && 
              s.nextOutputTs < s.minNextRightTs);
            s.state = ExecState.S_OUTPUT_READY;
          }

          case S_OUTPUT_READY:
            if (outputQueue.isFull())
            {
              done = true;
              break;
            }

            s.lastOutputTs = s.nextOutputTs;

            if (s.outputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
            {
              outputQueue.enqueue(s.outputElement);
              s.stats.incrNumOutputHeartbeats();
              s.state = ExecState.S_INIT;
              break;
            }
            else
            {
              outputQueue.enqueue(s.outputElement);
              s.state = ExecState.S_OUTPUT_ENQUEUED;
            }

          case S_OUTPUT_ENQUEUED:
            s.stats.incrNumOutputs();
            assert s.countTuple != null;
            if (zeroEval.eval(evalContext))
            {
              // nothing else needs to be done
              countSyn.deleteTuple(s.countTuple);
            } 
            countFactory.release(s.countTuple);

            s.state = ExecState.S_INPUT_ELEM_CONSUMED;

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
              }
            } 
            else
            {
              if (s.rightTuple != null)
              {
                rightTupleStorageAlloc.release(s.rightTuple);
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

  /*
   * return isHeartBeatPending
   * (non-Javadoc)
   * @see oracle.cep.execution.operators.ExecOpt#isHeartbeatPending()
   */
  @Override
  public boolean isHeartbeatPending()
  {
    ExceptState s = (ExceptState)mut_state;
    return (s.lastOutputTs < s.leftMinTs) && (s.lastOutputTs < s.rightMinTs);
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
    ExceptState m = (ExceptState) mut_state;
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
