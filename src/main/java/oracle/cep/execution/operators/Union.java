/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/Union.java /main/35 2012/06/18 06:29:07 udeshmuk Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares Union in package oracle.cep.execution.operators.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 udeshmuk  05/22/12 - make getInputQueue public
 anasrini  12/19/10 - replace eval() with eval(ec)
 sborah    06/12/09 - Memory Optimization
 sbishnoi  05/11/09 - require hbt from system timestamped lineage
 udeshmuk  04/13/09 - add getDebugInfo to assertion.
 sbishnoi  04/06/09 - piggyback optimization
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 sborah    09/25/08 - update stats
 sbishnoi  06/29/08 - override isHeartbeatPending
 najain    04/16/08 - add isSilentInput
 hopark    02/28/08 - resurrect refcnt
 hopark    12/07/07 - cleanup spill
 parujain  12/17/07 - db-join
 hopark    10/30/07 - remove IQueueElement
 hopark    10/21/07 - remove TimeStamp
 parujain  10/04/07 - delete op
 hopark    09/07/07 - eval refactor
 sbishnoi  07/27/07 - handling Propagation of old data
 hopark    07/13/07 - dump stack trace on exception
 parujain  07/03/07 - cleanup
 parujain  06/26/07 - mutable state
 hopark    05/24/07 - debug logging
 hopark    05/16/07 - add arguments for OutOfOrderException
 parujain  05/08/07 - monitoring statistics
 sbishnoi  04/07/07 - support for union all 
 hopark    03/21/07 - add TuplePtr pin
 najain    03/14/07 - cleanup
 najain    03/12/07 - bug fix
 parujain  02/27/07 - NPE bug
 najain    02/19/07 - bug fix
 najain    01/05/07 - spill over support
 parujain  12/12/06 - propagating relations
 najain    11/07/06 - add getOldestTs
 najain    08/14/06 - fix union
 dlenkov   06/19/06 - implementation
 skaluska  03/14/06 - query manager 
 skaluska  02/06/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/Union.java /main/35 2012/06/18 06:29:07 udeshmuk Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;

import oracle.cep.common.Constants;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.execution.queues.ISharedQueueReader;
import oracle.cep.execution.queues.Queue;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.service.ExecContext;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.execution.snapshot.SynopsisPersistenceContext;
import oracle.cep.execution.synopses.ExecSynopsis;
import oracle.cep.execution.synopses.LineageSynopsis;
import oracle.cep.execution.synopses.RelationSynopsis;
import oracle.cep.dataStructures.internal.ITuplePtr;

/**
 * Union
 *
 * @author skaluska
 */
public class Union extends ExecOpt
{
  private boolean isUnionAll;

  /** Synopsis for the output*/
  //  outSyn is used for Union All
  //  relSyn is used for Union
  
  LineageSynopsis            outSyn;
  RelationSynopsis           relSyn;
  
  
  /** Relation Scan identifier */
  private int                relScanId;
  
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

  /** left input store */
  private IAllocator<ITuplePtr> leftInputStore;

  /** right input store */
  private IAllocator<ITuplePtr> rightInputStore;

  /** Evaluation context in which all the action takes place */
  IEvalContext                evalContext;

  /** Evaluator to construct output tuples from left/right */
  IAEval                      outEval;
  
  IAEval                      initEval;
  
  IAEval                      incrEval;
  
  IAEval                      decrEval;
  
  IBEval                      oneEval;

  /** lineage information */
  ITuplePtr[]                 lineage;
  
  QueueElement leftPeekElement, rightPeekElement;
  
  
  /**
   * Constructor for Union
   * @param ec TODO
   */
  public Union(ExecContext ec)
  {
    super(ExecOptType.EXEC_UNION, new UnionState(ec), ec);
    lineage = new ITuplePtr[1];
  }
  
  public Union(ExecContext ec, boolean isUnionAll)
  {
    super(ExecOptType.EXEC_UNION, new UnionState(ec), ec);
    lineage         = new ITuplePtr[1];
    this.isUnionAll = isUnionAll;
  }

  /**
   * @param syn
   *          Sets outSyn
   */
  public void setOutSyn(LineageSynopsis syn)
  {
    this.outSyn = syn;
  }

  /**
   * Sets relSyn
   * @param syn
   */
  public void setRelSyn(RelationSynopsis syn)
  {
    this.relSyn = syn;
  }
  
  /**
   * @param relScanId
   *          The relScanId to set.
   */
  public void setRelScanId(int relScanId)
  {
    this.relScanId = relScanId;
  }
  
  /**
   * @param fullScanId
   *          The fullScanId to set.
   */
  public void setFullScanId(int fullScanId)
  {
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
   * Sets left Input Store
   * 
   * @param leftInputStore
   *          Sets leftInputStore.
   */
  public void setLeftInputStore(IAllocator<ITuplePtr> leftInputStore)
  {
    this.leftInputStore = leftInputStore;
  }

  /**
   * Sets right Input Store
   * 
   * @param rightInputStore
   *          Sets leftInputStore.
   */
  public void setRightInputStore(IAllocator<ITuplePtr> rightInputStore)
  {
    this.rightInputStore = rightInputStore;
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
  
  public void setIncrEval(IAEval incrEval)
  {
    this.incrEval = incrEval; 
  }
  
  public void setDecrEval(IAEval decrEval)
  {
    this.decrEval = decrEval;
  }
  
  public void setOneEval(IBEval oneEval)
  {
    this.oneEval = oneEval;
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
    UnionState s = (UnionState) mut_state;
    boolean exitState = true;

    assert s.state != ExecState.S_UNINIT;
    
    boolean done = false;
    
    //  Stats
    s.stats.incrNumExecutions();

    try
    {
      
      while ((s.stats.getNumInputs() < numElements) || (!exitState))
      {
        switch (s.state)
        {
          case S_PROPAGATE_OLD_DATA:
            if(this.isUnionAll)
              setExecSynopsis((ExecSynopsis) outSyn);
            else
              setExecSynopsis((ExecSynopsis)relSyn);
            
            handlePropOldData();
            break;

          case S_INIT:
          {
            leftPeekElement = null;
            rightPeekElement = null;
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
            // scan the synopsis for required outputTuple
            assert ((s.leftElement == null) ^ (s.rightElement == null));
            s.countScan = relSyn.getScan(relScanId);
            assert s.countScan != null;

            s.outputTuple = s.countScan.getNext();
            
            relSyn.releaseScan(relScanId, s.countScan);
            s.state = s.tmpState;    // switch either to PROCESSING2 (Plus Tuple) or to PROCESSING3 (Minus Tuple)
            break;
            
          case S_PROCESSING2: 
            // handle PLUS tuples
            // The count tuple does not exist
            if (s.outputTuple == null)
            {
              s.state = ExecState.S_OUTPUT_TUPLE;
              s.tmpState = ExecState.S_PROCESSING5;
            }
            else
            {               
              evalContext.bind(s.outputTuple, IEvalContext.SYN_ROLE);
              incrEval.eval(evalContext);
              tupleStorageAlloc.release(s.outputTuple);
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            }
            break;

          case S_PROCESSING3:
            //Handle MINUS Tuples
            assert s.outputTuple != null;
            evalContext.bind(s.outputTuple, IEvalContext.SYN_ROLE);
            if(oneEval.eval(evalContext))
            { 
              relSyn.deleteTuple(s.outputTuple);
              s.state = ExecState.S_PROCESSING6;
            }
            else
            { 
              decrEval.eval(evalContext);
              tupleStorageAlloc.release(s.outputTuple);
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            }
            break;

            
          case S_PROCESSING5:
            //Initialize and insert tuple in Synopsis
            evalContext.bind(s.outputTuple, IEvalContext.SYN_ROLE);
            initEval.eval(evalContext);
            relSyn.insertTuple(s.outputTuple);
            s.state = ExecState.S_PROCESSING6;
              

          case S_PROCESSING6:
            evalContext.bind(s.outputTuple, IEvalContext.NEW_OUTPUT_ROLE);
            outEval.eval(evalContext);
            s.state = ExecState.S_ALLOCATE_ELEM;
            break;

          case S_OUTER_INPUT_DEQUEUED:
            if (s.leftElement == null)
            {
              // right queue has some elements which are waiting for an input
              // from left queue
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

              // Update the last input Ts now
              s.lastLeftTs = s.leftTs;
              s.lastInputTs = s.lastLeftTs;

              s.nextOutputTs = s.leftElement.getTs();
              s.leftTuple = s.leftElement.getTuple();

              s.nextElementKind = s.leftElement.getKind();
              
              // current left input timeStamp must be greater or equal to
              // minNextLeftTs which was calculated in previous execution
              assert s.leftTs >= s.minNextLeftTs : getDebugInfo(s.leftTs,
                s.minNextLeftTs, s.leftElement.getKind().name(),
                s.lastLeftKind.name()); 
              
              s.lastLeftKind = s.leftElement.getKind();
              // set minimum timeStamp of next left element              
              s.minNextLeftTs = s.leftElement.getTotalOrderingGuarantee() ? 
                                s.leftTs + 1 : s.leftTs;                  

              if (s.nextElementKind == QueueElement.Kind.E_PLUS)
              {
                assert s.leftTuple != null;
                evalContext.bind(s.leftTuple, IEvalContext.INPUT_ROLE);

                // allocate the output tuple, then back to processing left plus
                if (!this.isUnionAll)
                {
                  s.tmpState = ExecState.S_PROCESSING2;
                  s.state = ExecState.S_PROCESSING1;
                }
                else
                {
                  s.tmpState = ExecState.S_PROCESS_OUTER_PLUS;
                  s.state = ExecState.S_OUTPUT_TUPLE;
                }
                break;
              }
              
              else if (s.nextElementKind == QueueElement.Kind.E_MINUS)
              {
                assert s.leftTuple != null;
                if(this.isUnionAll)
                  s.state = ExecState.S_PROCESS_OUTER_MINUS;
                else
                {
                  evalContext.bind(s.leftTuple, IEvalContext.INPUT_ROLE);
                  s.tmpState = ExecState.S_PROCESSING3;
                  s.state = ExecState.S_PROCESSING1;
                }
                break;
              }
              // nothing to be done for heartbeats
              else
                s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            }
            break;

          case S_PROCESS_OUTER_PLUS:
            outEval.eval(evalContext);
            if (outSyn != null)
            {
              lineage[0] = s.leftTuple;
              outSyn.insertTuple(s.outputTuple, lineage);
            }
            s.state = ExecState.S_ALLOCATE_ELEM;
            break;

          case S_PROCESS_OUTER_MINUS:
            assert outSyn != null;

            // Lineage for a tuple is the set of tuples that produced it.
            lineage[0] = s.leftTuple;
            TupleIterator unionscan = outSyn.getScan_l(lineage);
            s.outputTuple = unionscan.getNext();
            outSyn.releaseScan_l(unionscan);

            assert s.outputTuple != null;
      
            outSyn.deleteTuple(s.outputTuple);
           
            s.state = ExecState.S_ALLOCATE_ELEM;
            break;

          case S_INNER_INPUT_DEQUEUED:
            if (s.rightElement == null)
            {
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
              // Update the last input Ts now
              s.lastRightTs = s.rightTs;
              s.lastInputTs = s.lastRightTs;

              s.nextOutputTs = s.rightElement.getTs();
              s.rightTuple = s.rightElement.getTuple();

              s.nextElementKind = s.rightElement.getKind();
              
              // current right input timeStamp must be greater or equal to
              // minNextRightTs which was calculated in previous execution
              assert s.rightTs >= s.minNextRightTs : getDebugInfo(s.rightTs,
                s.minNextRightTs, s.rightElement.getKind().name(),
                s.lastRightKind.name()); 
                
              // set minimum timeStamp of next right element 
              s.minNextRightTs = s.rightElement.getTotalOrderingGuarantee() ? 
                                 s.rightTs + 1 : s.rightTs;    

              s.lastRightKind = s.rightElement.getKind();
              if (s.nextElementKind == QueueElement.Kind.E_PLUS)
              {
                assert s.rightTuple != null;
                evalContext.bind(s.rightTuple, IEvalContext.INPUT_ROLE);
                
                if (!this.isUnionAll)
                {
                  s.tmpState = ExecState.S_PROCESSING2;
                  s.state = ExecState.S_PROCESSING1;
                }  
                else
                {
                  // allocate the output tuple, then process right plus
                  s.tmpState = ExecState.S_PROCESS_INNER_PLUS;
                  s.state = ExecState.S_OUTPUT_TUPLE;
                }
                break;
              }
              else if (s.nextElementKind == QueueElement.Kind.E_MINUS)
              { 
                assert s.rightTuple != null;
                
                if(this.isUnionAll)
                  s.state = ExecState.S_PROCESS_INNER_MINUS;
                else
                {
                  evalContext.bind(s.rightTuple, IEvalContext.INPUT_ROLE);
                  s.tmpState = ExecState.S_PROCESSING3;
                  s.state = ExecState.S_PROCESSING1;
                  
                }
                break;
              }
              // nothing to be done for heartbeats
              else
                s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            }
            break;

          case S_PROCESS_INNER_PLUS:
              outEval.eval(evalContext);
              if (outSyn != null)
              {
                lineage[0] = s.rightTuple;
                outSyn.insertTuple(s.outputTuple, lineage);
              }
              s.state = ExecState.S_ALLOCATE_ELEM;
              break;

          case S_PROCESS_INNER_MINUS:
              assert outSyn != null;
              // Lineage for a tuple is the set of tuples that produced it.
              lineage[0] = s.rightTuple;
              TupleIterator unionScan = outSyn.getScan_l(lineage);
              s.outputTuple = unionScan.getNext();
              outSyn.releaseScan_l(unionScan);
 
              assert s.outputTuple != null;
              outSyn.deleteTuple(s.outputTuple);
              
              s.state = ExecState.S_ALLOCATE_ELEM;
            break;
 
          case S_OUTPUT_TUPLE:
            s.outputTuple = tupleStorageAlloc.allocate();
            evalContext.bind(s.outputTuple, IEvalContext.NEW_OUTPUT_ROLE);
            s.state = s.tmpState;
            break;

          case S_ALLOCATE_ELEM:
            s.outputElement.setKind(s.nextElementKind);
            s.outputElement.setTotalOrderingGuarantee(
                s.nextOutputTs < s.minNextLeftTs &&
                s.nextOutputTs < s.minNextRightTs);
            s.state = ExecState.S_OUTPUT_TIMESTAMP;
            
          case S_OUTPUT_TIMESTAMP:
            s.outputElement.setTs(s.nextOutputTs);
            s.outputElement.setTuple(s.outputTuple);
            s.state = ExecState.S_OUTPUT_READY;
          
          case S_OUTPUT_READY:
            if (outputQueue.isFull())
            {
              done = true;
              break;
            }
            s.lastOutputTs = s.nextOutputTs;
            
            outputQueue.enqueue(s.outputElement);
            if(s.outputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
              s.stats.incrNumOutputHeartbeats();
            else
              s.stats.incrNumOutputs();
            
            if (s.outputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
            {
              s.state = ExecState.S_INIT;
              break;
            }
            else
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
                leftInputStore.release(s.leftTuple);
              }
            } 
            else
            {
              if (s.rightTuple != null)
              {
                rightInputStore.release(s.rightTuple);
              }
            }

            elem = null;
            s.leftElement = null;
            s.rightElement = null;
            exitState = true;
       
            s.state = ExecState.S_INIT;
            break;
          }

          case S_GENERATE_HEARTBEAT:
            assert ((s.leftElement == null) && (s.rightElement == null));
            // Output a hBt only when we have not transmitted output 
            if ((s.lastOutputTs < s.leftMinTs) &&
                (s.lastOutputTs < s.rightMinTs))
            {
              s.nextOutputTs 
                = s.leftMinTs < s.rightMinTs ? s.leftMinTs : s.rightMinTs;
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

          default:
            assert false;
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
   * Although UNION will generate a heartbeat when
   * the both inputs are null;
   * But isHeartbeatPending 
   *   = s.lastOutputTs < s.leftMinTs) && (s.lastOutputTs < s.rightMinTs)
   * doesn't work fine;
   * If fails by displaying NPE from Scheduler.getNext()
   */
  @Override
  public boolean isHeartbeatPending()
  {    
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
    UnionState m = (UnionState) mut_state;
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

  /**
   * Create snapshot of Union operator by writing the operator state
   * into param java output stream.
   * State of Union operator consists of following:
   * 1. Mutable State
   * 2. Relation Synopsis
   * 3. Lineage Synopsis
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
      // Write Mutable state to output stream
      output.writeObject((UnionState)mut_state);

      // Write relation synopsis to output stream
      if (relSyn == null)
          output.writeBoolean(true);
      else
      {
          output.writeBoolean(false);
          relSyn.writeExternal(output, new SynopsisPersistenceContext(propScanId));
      }

      if (outSyn == null)
          output.writeBoolean(true);
      else
      {
          output.writeBoolean(false);
          outSyn.writeExternal(output);
      }
    }
    catch (IOException e)
    {
      LogUtil.logStackTrace(e);
      throw new CEPException(ExecutionError.SNAPSHOT_CREATION_ERROR, e, e.getLocalizedMessage(), this.getOptName());
    }
  }

  protected void loadSnapshot(ObjectInputStream input) throws ExecException
  {
    try
    {
      // Read MutableState from input stream
      UnionState loaded_mutable_state = (UnionState) input.readObject();
      ((UnionState)mut_state).copyFrom(loaded_mutable_state);

      boolean isSynNull = input.readBoolean();
      if (!isSynNull)
      {
          IPersistenceContext sharedSynopsisRecoveryCtx = new SynopsisPersistenceContext();
          sharedSynopsisRecoveryCtx.setCache(new HashSet());

          // Read relSyn synopsis from input stream
          relSyn.readExternal(input, sharedSynopsisRecoveryCtx);
      }

      isSynNull = input.readBoolean();
      if (!isSynNull)
          outSyn.readExternal(input);
    }
    catch (ClassNotFoundException e)
    {
      LogUtil.logStackTrace(e);
      throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR_CNF, e, this.getOptName(), e.getMessage());
    }
    catch (IOException e)
    {
      LogUtil.logStackTrace(e);
      throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR,e,e.getLocalizedMessage(), getOptName());
    }
  }
  
  /**
   * Implementation for creating snapshot of interoperator queue.
   * This will store the state of both input queues of the union operator.
   * @param output
   * @throws CEPException
   */
  @Override
  protected void createQueueSnapshot(ObjectOutputStream output) throws ExecException
  {    
    if(leftInputQueue instanceof ISharedQueueReader && rightInputQueue instanceof ISharedQueueReader)
    {
      try
      {
        output.writeObject(((ISharedQueueReader)leftInputQueue));
        output.writeObject(((ISharedQueueReader)rightInputQueue));
      } 
      catch (IOException e)
      {
        LogUtil.logStackTrace(e);
        throw new ExecException(ExecutionError.SNAPSHOT_CREATION_ERROR, e, e.getLocalizedMessage(), this.getOptName());
      }
    }
  }
  
  /**
   * Implementation for loading snapshot for inter operator queue.
   * This will restore the state of both input queues for union operator
   */
  @Override
  protected void loadQueueSnapshot(ObjectInputStream input) throws ExecException
  {
    if(leftInputQueue instanceof ISharedQueueReader && rightInputQueue instanceof ISharedQueueReader)
    {
      try
      {        
        ISharedQueueReader recoveredLeftInputQueue = ((ISharedQueueReader)input.readObject());
        ((ISharedQueueReader) leftInputQueue).copyFrom(recoveredLeftInputQueue);
        
        ISharedQueueReader recoveredRightInputQueue = ((ISharedQueueReader)input.readObject());
        ((ISharedQueueReader) rightInputQueue).copyFrom(recoveredRightInputQueue);
      } 
      catch (ClassNotFoundException e)
      {
        LogUtil.logStackTrace(e);
        throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR_CNF, e, this.getOptName(), e.getLocalizedMessage());
      }
      catch(IOException e)
      {
        LogUtil.logStackTrace(e);
        throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR, e, e.getLocalizedMessage(), getOptName());
      }      
    }
  }
}
