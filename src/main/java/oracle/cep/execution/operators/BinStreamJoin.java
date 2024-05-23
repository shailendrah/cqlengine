/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/BinStreamJoin.java /main/44 2012/06/18 06:29:07 udeshmuk Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares BinStreamJoin in package oracle.cep.execution.operators.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 udeshmuk  05/22/12 - make getInputQueue public
 anasrini  12/19/10 - replace eval() with eval(ec)
 sbishnoi  03/03/10 - BinStreamJoin extends BinJoinBase
 udeshmuk  11/19/09 - bind inner tuple
 sborah    06/12/09 - Memory Optimization
 sborah    05/08/09 - fix concurrency issue , refer bug 8500610
 anasrini  05/07/09 - req hbt from system timestamped lineage
 anasrini  04/23/09 - fix extra hbt during piggybacking
 udeshmuk  04/13/09 - add getDebugInfo to assertion
 sbishnoi  04/09/09 - optimizing dequeue by keeping minNextTs for each queue
 udeshmuk  03/25/09 - total ordering ts
 sborah    03/19/09 - siggen optimization: removing viewstrmsrc
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 hopark    10/07/08 - use execContext to remove statics
 sborah    09/25/08 - update stats
 sbishnoi  06/27/08 - updating lastInputTs on each input tuple
 najain    04/16/08 - 
 hopark    02/28/08 - resurrect refcnt
 hopark    12/27/07 - support xmllog
 hopark    12/07/07 - cleanup spill
 parujain  12/05/07 - operator logging
 parujain  12/19/07 - inner and outer
 uarujain  12/06/07 - bug fix
 parujain  11/29/07 - getNoInputQueues
 parujain  11/19/07 - External Source
 hopark    10/30/07 - remove IQueueElement
 hopark    10/22/07 - remove TimeStamp
 parujain  10/04/07 - delete op
 hopark    09/07/07 - eval refactor
 hopark    07/13/07 - dump stack trace on exception
 parujain  06/26/07 - mutable state
 hopark    06/11/07 - logging - remove ExecContext
 hopark    05/24/07 - debug logging
 hopark    05/16/07 - pass arguments for OutOfOrderException
 parujain  05/08/07 - monitoring statistics
 najain    04/11/07 - bug fix
 hopark    04/09/07 - fix pincount
 hopark    03/23/07 - throws exception from QueueElement
 hopark    03/21/07 - add TuplePtr pin
 najain    03/14/07 - cleanup
 najain    03/12/07 - bug fix
 najain    01/05/07 - spill over support
 parujain  02/27/07 - NPE bug
 najain    11/10/06 - bug fix
 najain    11/07/06 - add getOldestTs
 najain    08/02/06 - refCounting optimizations
 najain    08/10/06 - add asserts
 najain    08/01/06 - handle silent relations
 najain    07/19/06 - ref-count tuples 
 najain    07/13/06 - ref-count timestamps 
 najain    07/12/06 - ref-count elements 
 najain    07/10/06 - add inStores 
 najain    06/09/06 - bug fix 
 najain    05/31/06 - bug fix 
 najain    05/26/06 - implementation
 skaluska  02/06/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/BinStreamJoin.java /main/44 2012/06/18 06:29:07 udeshmuk Exp $
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

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.queues.ISharedQueueReader;
import oracle.cep.execution.queues.Queue;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.service.ExecContext;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.execution.snapshot.SynopsisPersistenceContext;
import oracle.cep.execution.synopses.ExternalSynopsis;
import oracle.cep.execution.synopses.RelationSynopsis;

/**
 * @author najain
 *
 */
public class BinStreamJoin extends BinJoinBase
{
  /** Synopsis for the inner relation */
  RelationSynopsis              innerSyn;
  
  /** External Synopsis for inner relation when external */
  ExternalSynopsis              innerExtSyn;
  
  /** True if inner is an external relation */
  boolean                       isExternal;

  /** Scan identifier for scanning the inner tuples */
  int                           innerScanId;

  /** Scan identifier for full scanning the inner tuples */
  int                           innerFullScanId;
  
  /** outer input queue */
  private Queue                 outerInputQueue;

  /** inner input queue */
  private Queue                 innerInputQueue;

  /* does the outer/inner operator only depend on silent Relations */
  boolean                       outerSilentRelns;
   
  boolean                       innerSilentRelns;

  // The list of silent Relations that the operator depends on: This is needed
  // to propagate the heartbeat in case of a stall or a silent relation.
  // Currently, silent streams/relations are not handled, only static relations
  // (one for which the time is not specifed) and handled appropriately.
  LinkedList<RelSource>         outerInputRelns;

  LinkedList<RelSource>         innerInputRelns;

  /** outer input store */
  private IAllocator<ITuplePtr> outerTupleStorageAlloc;

  /** inner input store */
  private IAllocator<ITuplePtr> innerTupleStorageAlloc;

  /** Evaluation context in which all the action takes place */
  private IEvalContext                  evalContext;

  /** Arithmetic evaluator to construct the output tuple */
  IAEval                        outputConstructor;
  
  /** Position of the ELEMENT_TIME column */
  private int                    elemTimePos;

  QueueElement outerPeekElement, innerPeekElement;
  
  private boolean needBindToRoleOnRecovery = false;
    /**
   * Constructor for BinStreamJoin
   * @param ec TODO
   */
  public BinStreamJoin(ExecContext ec)
  {
    super(ExecOptType.EXEC_BIN_STREAM_JOIN, new BinStreamJoinState(ec), ec);
  }

  /**
   * @return Returns the evalContext.
   */
  public IEvalContext getEvalContext()
  {
    return evalContext;
  }

  /**
   * @param evalContext
   *          The evalContext to set.
   */
  public void setEvalContext(IEvalContext evalContext)
  {
    this.evalContext = evalContext;
  }

  /**
   * @return Returns the innerScanId.
   */
  public int getInnerScanId()
  {
    return innerScanId;
  }

  /**
   * @param innerScanId
   *          The innerScanId to set.
   */
  public void setInnerScanId(int innerScanId)
  {
    this.innerScanId = innerScanId;
  }

  public int getInnerFullScanId()
  {
    return innerFullScanId;
  }

  public void setInnerFullScanId(int innerFullScanId)
  {
    this.innerFullScanId = innerFullScanId;
  }

  /**
   * @return Returns the innerSyn.
   */
  public RelationSynopsis getInnerSyn()
  {
    return innerSyn;
  }

  /**
   * @param innerSyn
   *          The innerSyn to set.
   */
  public void setInnerSyn(RelationSynopsis innerSyn)
  {
    this.innerSyn = innerSyn;
  }
  
  /**
   * @return Returns the innerExtSyn.
   */
  public ExternalSynopsis getInnerExtSyn()
  {
    return innerExtSyn;
  }  
  
  /**
   * @param syn 
   *         The inner external synopsis
   */
  public void setInnerExtSyn(ExternalSynopsis syn)
  {
    this.innerExtSyn = syn;
  }

  /**
   * @return Returns the outputConstructor.
   */
  public IAEval getOutputConstructor()
  {
    return outputConstructor;
  }

  /**
   * @param outputConstructor
   *          The outputConstructor to set.
   */
  public void setOutputConstructor(IAEval outputConstructor)
  {
    this.outputConstructor = outputConstructor;
  }

  /**
   * Getter for outerInputQueue
   * 
   * @return Returns the outerInputQueue
   */
  public Queue getOuterInputQueue()
  {
    return outerInputQueue;
  }

  /**
   * Setter for outerInputQueue
   * 
   * @param outerInputQueue
   *          The outerInputQueue to set.
   */
  public void setOuterInputQueue(Queue outerInputQueue)
  {
    this.outerInputQueue = outerInputQueue;
  }

  /**
   * Getter for innerInputQueue
   * 
   * @return Returns the innerInputQueue
   */
  public Queue getInnerInputQueue()
  {
    return innerInputQueue;
  }

  /**
   * Setter for innerInputQueue
   * 
   * @param innerInputQueue
   *          The innerInputQueue to set.
   */
  public void setInnerInputQueue(Queue innerInputQueue)
  {
    this.innerInputQueue = innerInputQueue;
  }

  /**
   * Setter for outerSilentRelns
   * 
   * @param outerSilentRelns
   *          The outerSilentRelns to set.
   */
  public void setOuterSilentRelns(boolean outerSilentRelns)
  {
    this.outerSilentRelns = outerSilentRelns;
  }

  public void addOuterInputRelns(RelSource execOp)
  {
    if (outerInputRelns == null)
      outerInputRelns = new LinkedList<RelSource>();

    outerInputRelns.add(execOp);
  }

  /**
   * Setter for innerSilentRelns
   * 
   * @param innerSilentRelns
   *          The innerSilentRelns to set.
   */
  public void setInnerSilentRelns(boolean innerSilentRelns)
  {
    this.innerSilentRelns = innerSilentRelns;
  }

  public void addInnerInputRelns(RelSource execOp)
  {
    if (innerInputRelns == null)
      innerInputRelns = new LinkedList<RelSource>();

    innerInputRelns.add(execOp);
  }

  /**
   * Setter for outerTupleStorageAlloc
   * 
   * @param outerTupleStorageAlloc
   *          The outerTupleStorageAlloc to set.
   */
  public void setOuterTupleStorageAlloc(
      IAllocator<ITuplePtr> outerTupleStorageAlloc)
  {
    this.outerTupleStorageAlloc = outerTupleStorageAlloc;
  }
  
  IAllocator<ITuplePtr> getOuterTupleStorageAlloc()
  {
    return this.outerTupleStorageAlloc;
  }

  /**
   * Setter for innerTupleStorageAlloc
   * 
   * @param innerTupleStorageAlloc
   *          The innerTupleStorageAlloc to set.
   */
  public void setInnerTupleStorageAlloc(
      IAllocator<ITuplePtr> innerTupleStorageAlloc)
  {
    this.innerTupleStorageAlloc = innerTupleStorageAlloc;
  }
  
  IAllocator<ITuplePtr> getInnerTupleStorageAlloc()
  {
    return this.innerTupleStorageAlloc;
  }
  
  /**
   * Setter of Whether inner is an external relation or not
   * 
   * @param ext 
   *         True if external relation
   */
  public void setIsExternal(boolean ext)
  {
    this.isExternal = ext;
  }

  
  /**
   * Setter for ELEMENT_TIME column position
   * 
   * @param elemTimePos
   *          The ELEMENT_TIME column position
   */
  public void setElemTimePos(int elemTimePos)
  {
    this.elemTimePos = elemTimePos;
  }
  
  int getElemTimePos()
  {
    return this.elemTimePos;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.operators.ExecOpt#run(int)
   */
  @Override
  public int run(int timeSlice) throws ExecException
  {
    int numElements;
    BinStreamJoinState s = (BinStreamJoinState) mut_state;
    boolean exitState = true;

    assert s.state != ExecState.S_UNINIT;

    boolean done = false;
    
    //stats
    s.stats.incrNumExecutions();
    
    try
    {
      numElements = timeSlice;
      
      while ((s.stats.getNumInputs() < numElements) || (!exitState))
      {
        switch (s.state)
        {
          case S_INIT:
          {
            // Peek to revise min outer timestamp estimate
            outerPeekElement = outerInputQueue.peek(s.outerPeekElementBuf);
            if (outerPeekElement != null)
            {
              s.outerMinTs = outerPeekElement.getTs();
            }
            else {
              // Minimum timestamp possible on the next outer element
              s.outerMinTs = s.minNextOuterTs;
            }
            // Peek to revise min inner timestamp estimate
            innerPeekElement = innerInputQueue.peek(s.innerPeekElementBuf);
            if (innerPeekElement != null)
            {
              s.innerMinTs = innerPeekElement.getTs();
            }
            else {
              // Minimum timestamp possible on the next inner element
              s.innerMinTs = s.minNextInnerTs;
            }
            
            // We have to process the outer if it has an element waiting in
            // the queue. Otherwise we cannot do any processing
            // If there is external relation then inner will not receive any input
            if (s.outerMinTs < s.innerMinTs || (isExternal))
            {
              if(outerPeekElement != null) {
                s.outerElement = outerInputQueue.dequeue(s.outerElementBuf);
              }
              else{
                s.outerElement = null;
              }                
              s.state = ExecState.S_OUTER_INPUT_DEQUEUED;
              break;
            }           
            else
            {
              if(innerPeekElement != null) {
                s.innerElement = innerInputQueue.dequeue(s.innerElementBuf);
              }
              else
                s.innerElement = null;
              s.state = ExecState.S_INNER_INPUT_DEQUEUED;
            }
          }

          case S_INNER_INPUT_DEQUEUED:
            if (s.innerElement == null)
            {
              if(innerSystsSourceLineage != null)
              {
                if(outerPeekElement != null && 
                   innerSystsSourceLineage.size() > 0)
                { //inner queue has some input
                  requestForHeartbeat(innerSystsSourceLineage, 
                                      Constants.INNER, 
                                      outerPeekElement.getTs());
                }
              }
              s.state = ExecState.S_GENERATE_HEARTBEAT;
              break;
            }
            else
            {
              // Update our counts
              if(s.innerElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
                s.stats.incrNumInputHeartbeats();
              else
                s.stats.incrNumInputs();

              // update inner ts
              s.innerTs = s.innerElement.getTs();
             
              // We should have a progress of time.
              if (s.lastInnerTs > s.innerTs)
              {
                s.state = ExecState.S_INIT;
                throw ExecException.OutOfOrderException(
                        this,
                        s.lastInnerTs, 
                        s.innerTs, 
                        s.innerElement.toString());
              }
              
              //update last input tuple ordering flag
              s.lastInputOrderingFlag = s.innerElement.getTotalOrderingGuarantee();

              // Update the last input Ts now
              s.lastInnerTs = s.innerTs;
              s.lastInputTs = s.lastInnerTs;
              // current input timeStamp should be greater than the 
              // expected minNextInnerTs calculated in previous iteration
              // In the first execution, minNextInnerTs will be the smallest
              // java.long.Long value
              assert s.innerTs >= s.minNextInnerTs : getDebugInfo(s.innerTs,
                s.minNextInnerTs, s.innerElement.getKind().name(),
                s.lastRightKind.name());
            
              s.lastRightKind = s.innerElement.getKind();  
              // update the minimum next inner queue element
              s.minNextInnerTs 
                = s.lastInputOrderingFlag ? s.innerTs + 1: s.innerTs;
              // Next element to be outputted
              s.innerTuple = s.innerElement.getTuple();
              
              if (s.innerElement.getKind() == QueueElement.Kind.E_PLUS)
              {
                evalContext.bind(s.innerTuple, IEvalContext.INNER_ROLE);
                s.state = ExecState.S_PROCESS_INNER_PLUS;
              }
              else if (s.innerElement.getKind() == QueueElement.Kind.E_MINUS)
              {
                evalContext.bind(s.innerTuple, IEvalContext.INNER_ROLE);
                s.state = ExecState.S_PROCESS_INNER_MINUS;
              }
              // nothing to be done for heartbeats
              else
                s.state = ExecState.S_INNER_INPUT_ELEM_CONSUMED;

              exitState = false;
            }
            break;

          case S_PROCESS_INNER_PLUS:
            // Insert the inner tuple into innerSynopsis
            innerSyn.insertTuple(s.innerTuple);
            s.state = ExecState.S_INNER_INPUT_ELEM_CONSUMED;
            break;

          case S_PROCESS_INNER_MINUS:
            // Delete the inner tuple into innerSynopsis
            innerSyn.deleteTuple(s.innerTuple);
            s.state = ExecState.S_INNER_INPUT_ELEM_CONSUMED;
            break;

          case S_OUTER_INPUT_DEQUEUED:
            if (s.outerElement == null)
            {
              // Do not do anything here even if inner input is waiting.
              // This is because BinStreamJoin (unlike other binary ops)
              // is asymmetric, in the sense, outer is dequeued only if
              // inner time is > outer time. Equality does not suffice.
              // Other binary ops are symmetric, if outer_time = inner_time,
              // the both queues can be cleared.
              //
              // Now, if we were to send hbt request on outer, we will
              // end up triggering a scenario of endless heartbeat updates,
              // on outer then inner then outer and so on ...
              s.state = ExecState.S_GENERATE_HEARTBEAT;
              break;
            }
            else
            {
              // Update our counts
              if(s.outerElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
                s.stats.incrNumInputHeartbeats();
              else
                s.stats.incrNumInputs();

              // update outer ts
              s.outerTs = s.outerElement.getTs();
              
              // We should have a progress of time.
              if (s.lastOuterTs > s.outerTs)
              {
                s.state = ExecState.S_INIT;
                throw ExecException.OutOfOrderException(
                        this,
                        s.lastOuterTs, 
                        s.outerTs, 
                        s.outerElement.toString());
              }
              
              // update last ordering flag
              s.lastInputOrderingFlag = s.outerElement.getTotalOrderingGuarantee();


              // Update the last input Ts now
              s.lastOuterTs = s.outerTs;
              s.lastInputTs = s.lastOuterTs;
              
              // current input timeStamp should be greater than the 
              // expected minNextOuterTs calculated in previous iteration
              // In the first execution, minNextOuterTs will be the smallest
              // java.long.Long value; thats why assert will pass
              assert s.outerTs >= s.minNextOuterTs : getDebugInfo(s.outerTs,
                s.minNextOuterTs, s.outerElement.getKind().name(),
                s.lastLeftKind.name());
              
              s.lastLeftKind = s.outerElement.getKind();
              // update the minimum next outer queue element
              s.minNextOuterTs 
                = s.lastInputOrderingFlag ? s.outerTs + 1: s.outerTs;

              // Next element to be outputted
              s.outerTuple = s.outerElement.getTuple();
              exitState = false;

              if (s.outerElement.getKind() == QueueElement.Kind.E_PLUS)
              {
                s.state = ExecState.S_PROCESS_OUTER_PLUS;

                // Bind the output
                evalContext.bind(s.outerTuple, IEvalContext.OUTER_ROLE);
              }
              else
              {
                // nothing to be done for heartbeats
                assert (s.outerElement.getKind() == QueueElement.Kind.E_HEARTBEAT);
                s.state = ExecState.S_OUTER_INPUT_ELEM_CONSUMED;
                break;
              }
            }

          case S_PROCESS_OUTER_PLUS:
            // Scan of inner tuples that join with outer tuple
            if(isExternal)
              s.innerScan = innerExtSyn.getScan(evalContext);
            else
              s.innerScan = innerSyn.getScan(innerScanId);
           
            // Get next inner tuple
            assert s.innerScan != null; 
            s.innerTuple = s.innerScan.getNext();

            s.state = ExecState.S_PROCESS_GET_NEXT_INNER_ELEM;

          case S_PROCESS_GET_NEXT_INNER_ELEM:
            //Note: inner Tuple has been scanned in previous states 
            if (s.innerTuple == null)
            {
              // No more tuples
              if(isExternal)
                innerExtSyn.releaseScan(s.innerScan);
              else
                innerSyn.releaseScan(innerScanId, s.innerScan);
              s.state = ExecState.S_OUTER_INPUT_ELEM_CONSUMED;
              break;
            }
            else
            {
              evalContext.bind(s.innerTuple, IEvalContext.INNER_ROLE);
              s.state = ExecState.S_ALLO_POPU_OUTPUT_TUPLE;
            }

          case S_ALLO_POPU_OUTPUT_TUPLE:
            // allocate space for the output tuple
            s.outputTuple = tupleStorageAlloc.allocate();

            // construct the output tuple
            evalContext.bind(s.outputTuple, IEvalContext.NEW_OUTPUT_ROLE);
            outputConstructor.eval(evalContext);
            
            s.outputTs = s.outerElement.getTs();
            
            // Handle ELEMENT_TIME
            ITuple o = s.outputTuple.pinTuple(IPinnable.WRITE);
            o.lValueSet(elemTimePos, s.outputTs);
            s.outputTuple.unpinTuple();
                        
            s.state = ExecState.S_OUTPUT_ELEMENT;

          case S_OUTPUT_ELEMENT:
            s.outputElement.setKind(QueueElement.Kind.E_PLUS);
            s.outputElement.setTs(s.outputTs);
            s.outputElement.setTuple(s.outputTuple);
            // Release the innerTuple
            if (s.innerTuple != null)
            {
              innerTupleStorageAlloc.release(s.innerTuple);
              s.innerTuple = null;
            }

            // Scan the next inner tuple and set the guarantee flag
            if((s.innerTuple = s.innerScan.getNext()) == null)
            {
              // this is the last tuple in the inner scan
              s.outputElement.setTotalOrderingGuarantee(s.lastInputOrderingFlag);
            }
            else
            {
              // some tuple in inner scan remaining so flag false
              s.outputElement.setTotalOrderingGuarantee(false);
            }   
            s.state = ExecState.S_OUTPUT_READY;
            break;

          case S_GENERATE_HEARTBEAT:
            if((s.lastEffectiveOutputTs < s.outerMinTs) && (isExternal))
            {
              s.lastOutputTs = s.outerMinTs;
              s.outputElement.heartBeat(s.lastOutputTs);
              //copy the flag of last input
              boolean flag = s.lastOutputTs < s.minNextOuterTs;
              s.outputElement.setTotalOrderingGuarantee(flag);
              s.state = ExecState.S_OUTPUT_READY;
            }
            else if ((!isExternal) &&
                     (s.lastEffectiveOutputTs < s.innerMinTs) &&
                     (s.lastEffectiveOutputTs < s.outerMinTs))
            {
              if (s.outerMinTs < s.innerMinTs)
                s.lastOutputTs = s.outerMinTs;
              else
                s.lastOutputTs = s.innerMinTs;
              s.outputElement.heartBeat(s.lastOutputTs);

              boolean flag = s.lastOutputTs < s.minNextOuterTs;
              //copy the flag of last input
              s.outputElement.setTotalOrderingGuarantee(flag);
              s.state = ExecState.S_OUTPUT_READY;
            }
            else
            {
              s.state = ExecState.S_INIT;
              done = true;
              break;
            }

          case S_OUTPUT_READY:
            if (outputQueue.isFull())
            {
              done = true;
              break;
            }

            boolean gflag = s.outputElement.getTotalOrderingGuarantee();
            s.lastOutputTs = s.outputElement.getTs();            
            s.lastEffectiveOutputTs = s.lastOutputTs + (gflag ? 1 : 0);
            if (s.outputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
            {
              s.state = ExecState.S_INIT;
              done = true;
            }
            else
              s.state = ExecState.S_PROCESS_GET_NEXT_INNER_ELEM;
            
            outputQueue.enqueue(s.outputElement);
            if(s.outputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
              s.stats.incrNumOutputHeartbeats();
            else
              s.stats.incrNumOutputs();

            break;

          case S_OUTER_INPUT_ELEM_CONSUMED:
            assert s.outerElement != null;

            if (s.outerTuple != null)
            {
              outerTupleStorageAlloc.release(s.outerTuple);
            }
            exitState = true;
            s.outerTuple = null;

            s.state = ExecState.S_INIT;
            break;

          case S_INNER_INPUT_ELEM_CONSUMED:
            assert s.innerElement != null;

            if (s.innerTuple != null)
            {
              innerTupleStorageAlloc.release(s.innerTuple);
              s.innerTuple = null;
            }
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
      s.state = ExecState.S_INIT; 
      // Ignore it for now
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      return 0;
    }
    
    // TODO Auto-generated method stub
    return 0;
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

  @Override
  protected boolean isHeartbeatPending()
  {
    return false;
  }
  
  protected int getNoInputQueues() 
  {
    if(isExternal)
      return 1;
    
    return 2;
  }

  public Queue getInputQueue(int n) 
  {
    if (n == Constants.OUTER) return outerInputQueue;
    if (n == Constants.INNER) return innerInputQueue;
    return null;
  }

  protected long getLastTs(int n)
  {
    BinStreamJoinState m = (BinStreamJoinState) mut_state;
    if (n == Constants.OUTER) return m.lastOuterTs;
    if (n == Constants.INNER) return m.lastInnerTs;
    assert false : "shouldn't be called";
    return -1;
  }

  protected boolean isSilentInput(int n)
  {
    if (n == Constants.OUTER) return outerSilentRelns;
    if (n == Constants.INNER) return innerSilentRelns;
    assert false : "shouldn't be called";
    return false;
  }

  @Override
  public void setOuterScanId(int outerScanId)
  {
    // This will never be called
    assert false;
  }

  @Override
  public void setOuterFullScanId(int outerFullScanId)
  {
    // This will never be called
    assert false;
  }

  @Override
  public void setOuterSyn(RelationSynopsis eOutSyn)
  {
    // This will never be called
    assert false;  
  }
 
  /**
   * Create snapshot of Binary Join operator by writing the operator state
   * into param java output stream.
   * State of Binary Join operator consists of following:
   * 1. Mutable State
   * 2. Inner Synopsis
   * Please note that we will write the state of operator in above sequence, so
   * the loadSnapshot should also read the state in the same sequence.
   * @param output
   * @throws IOException 
   */
  protected void createSnapshot(ObjectOutputStream output) throws CEPException
  {   
    try
    {
      output.writeObject((BinStreamJoinState)mut_state);    
      if(!isExternal)
      {        
        assert innerFullScanId != -1;
        innerSyn.writeExternal(output, new SynopsisPersistenceContext(innerFullScanId));
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
      BinStreamJoinState loaded_mutable_state = (BinStreamJoinState) input.readObject();
      ((BinStreamJoinState)mut_state).copyFrom(loaded_mutable_state);
      
      IPersistenceContext sharedSynopsisRecoveryCtx = new SynopsisPersistenceContext();
      sharedSynopsisRecoveryCtx.setCache(new HashSet());
      if(isNeedBindToRoleOnRecovery())
        sharedSynopsisRecoveryCtx.setRole(IEvalContext.INNER_ROLE);  
      if(!isExternal)
        innerSyn.readExternal(input, sharedSynopsisRecoveryCtx);
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
  
  
  /**
   * Implementation for creating snapshot of interoperator queue.
   * This will store the state of both input queues of the join operator.
   * @param output
   * @throws CEPException
   */
  @Override
  protected void createQueueSnapshot(ObjectOutputStream output) throws ExecException
  {    
    if(innerInputQueue instanceof ISharedQueueReader)
    {
      try
      {
        output.writeObject(((ISharedQueueReader)innerInputQueue));
        output.writeObject(((ISharedQueueReader)outerInputQueue));
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
   * This will restore the state of both input queues for join operator
   */
  @Override
  protected void loadQueueSnapshot(ObjectInputStream input) throws ExecException
  {
    if(innerInputQueue instanceof ISharedQueueReader && outerInputQueue instanceof ISharedQueueReader)
    {
      try
      {        
        ISharedQueueReader recoveredInnerInputQueue = ((ISharedQueueReader)input.readObject());
        ((ISharedQueueReader) innerInputQueue).copyFrom(recoveredInnerInputQueue);
        
        ISharedQueueReader recoveredOuterInputQueue = ((ISharedQueueReader)input.readObject());
        ((ISharedQueueReader) outerInputQueue).copyFrom(recoveredOuterInputQueue);
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

  public boolean isNeedBindToRoleOnRecovery()
  {
    return needBindToRoleOnRecovery;
  }

  public void setNeedBindToRoleOnRecovery(boolean needBindToRoleOnRecovery)
  {
    this.needBindToRoleOnRecovery = needBindToRoleOnRecovery;
  } 
}
