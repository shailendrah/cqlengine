/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/BinJoin.java /main/60 2015/07/21 05:10:19 udeshmuk Exp $ */
/* Copyright (c) 2006, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares BinJoin in package oracle.cep.execution.operators.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 udeshmuk  06/09/15 - support initializeState
 udeshmuk  02/09/15 - support outer join ldo
 udeshmuk  08/26/13 - snapshotid assignment : don't need to check if one side
                      has long.Max_value since hb also has snapshotId assigned
 udeshmuk  05/06/13 - snapshotid should be smaller of the two if one side has
                      long.max_value
 vikshukl  02/18/13 - propagate heartbeat
 vikshukl  10/04/12 - deal with input syn on fact side and output syn for a
                      join with slow dimension
 udeshmuk  09/09/12 - propagate snapshotid and handle event id
 udeshmuk  08/24/12 - propagate snapshot id
 udeshmuk  05/22/12 - make getInputQueue public
 anasrini  12/19/10 - replace eval() with eval(ec)
 sbishnoi  03/03/10 - BinJoin extends BinJoinBase
 sbishnoi  07/30/09 - fix total ordering bug
 sborah    06/12/09 - Memory Optimization
 parujain  05/21/09 - ordering flag
 sbishnoi  05/21/09 - fixing binjoin piggyback
 sbishnoi  05/11/09 - require hbt from system timestamped lineage
 sbishnoi  05/08/09 - fixing concurrency problem
 anasrini  05/07/09 - outer join with external rel, remove from outerMatchHash
 udeshmuk  04/13/09 - add getDebugInfo to assertion
 sbishnoi  04/13/09 - fix heartbeat total ordering flag
 parujain  03/23/09 - total ordering ts opt
 parujain  03/17/09 - stateless server
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 hopark    10/07/08 - use execContext to remove statics
 parujain  07/31/08 - 
 sbishnoi  06/29/08 - overriding isHeartbeatPending
 hopark    06/19/08 - logging refactor
 najain    04/16/08 - add isSilentInput
 hopark    02/28/08 - resurrect refcnt
 hopark    12/26/07 - support xmllog
 hopark    12/07/07 - cleanup spill
 parujain  12/05/07 - operator logging
 hopark    11/27/07 - add operator specific dump
 parujain  12/19/07 - inner and outer
 parujain  12/17/07 - db-join
 parujain  12/13/07 - external relation
 hopark    10/30/07 - remove IQueueElement
 hopark    10/22/07 - remove TimeStamp
 parujain  10/04/07 - delete op
 hopark    09/07/07 - eval refactor
 najain    07/16/07 - debug
 hopark    07/13/07 - dump stack trace on exception
 parujain  07/03/07 - cleanup
 parujain  06/26/07 - mutable state
 hopark    06/11/07 - logging - remove ExecContext
 hopark    05/24/07 - debug logging
 hopark    05/16/07 - pass arguments for OutOfOrderException
 parujain  05/08/07 - monitoring statistics
 hopark    04/24/07 - fix refcount
 hopark    04/20/07 - change pinTuple semantics
 hopark    04/17/07 - fix pincount bug
 najain    04/11/07 - bug fix
 hopark    04/06/07 - pincount bug fix
 najain    03/30/07 - bug fix
 hopark    03/24/07 - add unpin
 najain    03/14/07 - cleanup
 najain    03/12/07 - bug fix
 parujain  02/27/07 - NPE bug
 rkomurav  01/12/07 - missing break stmts.
 najain    01/05/07 - spill over support
 parujain  12/11/06 - propagating relations
 rkomurav  11/10/06 - outer join support
 najain    11/10/06 - bug fix
 najain    11/07/06 - add getOldestTs
 najain    08/02/06 - refCounting optimizations
 najain    08/10/06 - add asserts
 skmishra  08/08/06 - join testing
 parujain  08/08/06 - join testing
 najain    08/01/06 - handle silent relations
 najain    07/19/06 - ref-count tuples 
 najain    07/13/06 - ref-count timestamps 
 najain    07/10/06 - add inStores 
 najain    06/09/06 - bug fix 
 najain    05/23/06 - heartbeat support 
 najain    04/10/06 - implementation
 skaluska  03/14/06 - query manager 
 skaluska  02/06/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/BinJoin.java /main/60 2015/07/21 05:10:19 udeshmuk Exp $
 *  @author  najain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import oracle.cep.common.Constants;
import oracle.cep.common.OuterJoinType;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.queues.ISharedQueueReader;
import oracle.cep.execution.queues.Queue;
import oracle.cep.execution.queues.ISharedQueueWriter;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogTags;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.serializer.ObjStreamFactory;
import oracle.cep.serializer.ObjStreamUtil;
import oracle.cep.service.ExecContext;
import oracle.cep.snapshot.SnapshotContext;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.execution.snapshot.SynopsisPersistenceContext;
import oracle.cep.execution.snapshot.journals.BinJoinJournalEntry;
import oracle.cep.execution.synopses.ExecSynopsis;
import oracle.cep.execution.synopses.ExternalSynopsis;
import oracle.cep.execution.synopses.LineageSynopsis;
import oracle.cep.execution.synopses.RelationSynopsis;

/**
 * Binjoin is the execution operator that performs the join
 *
 * @author skaluska
 *
 */
public class BinJoin extends BinJoinBase
{
  static final String TAG_INNER_MATCHHASH = "InnerMatchHash";
  static final String TAG_OUTER_MATCHHASH = "OuterMatchHash";
  
  /** Synopsis for the inner relation */
  RelationSynopsis           innerSyn;

  /** Synopsis for the outer relation */
  RelationSynopsis           outerSyn;

  /** Synopsis for the join relation */
  LineageSynopsis            joinSyn; 
  
  /** External Synopsis for inner relation when external */
  ExternalSynopsis           innerExtSyn;
  
  /** True if inner is an external relation */
  boolean                    isExternal;

  /** Scan identifier for scanning the outer tuples */
  int                        outerScanId;

  /** Scan identifier for scanning the inner tuples */
  int                        innerScanId;
  
  /** Scan identifer for a full scan over inner synopsis */
  int                        innerFullScanId;
  
  /** Scan identifier for a full scan over outer synopsis*/
  int                        outerFullScanId;

  /** outer input queue */
  private Queue              outerInputQueue;

  /** inner input queue */
  private Queue              innerInputQueue;

  /* does the outer/inner operator only depend on silent Relations */
  boolean                    outerSilentRelns;

  boolean                    innerSilentRelns;

  // The list of silent Relations that the operator depends on: This is needed
  // to propagate the heartbeat in case of a stall or a silent relation.
  // Currently, silent streams/relations are not handled, only static relations
  // (one for which the time is not specifed) and handled appropriately.
  LinkedList<RelSource>      outerInputRelns;

  LinkedList<RelSource>      innerInputRelns;

  /** outer input store */
  private IAllocator<ITuplePtr> outerTupleStorageAlloc;

  /** inner input store */
  private IAllocator<ITuplePtr> innerTupleStorageAlloc;

  /** outer join type */
  OuterJoinType              outerJoinType;

  /** Evaluation context in which all the action takes place */
  IEvalContext                evalContext;

  /** Arithmetic evaluator to construct the output tuple */
  IAEval                      outputConstructor;

  /** Null left tuple */
  ITuplePtr   nullLeftTuple;

  /** Null Right tuple */
  ITuplePtr   nullRightTuple;
  
  QueueElement outerPeekElement, innerPeekElement;
  
  /**
   * determin which side is dependent on archived dimension
   */
  private int                archivedDim = Constants.INVALID_VALUE;
 
  private boolean oldDataPropNeeded = true;
  
  private boolean needBindToRoleOnRecovery = false;
  
  private BinJoinJournalEntry journalEntry;
   
  /**
   * Constructor for BinJoin
   * @param ec Execution Context
   */
  public BinJoin(ExecContext ec)
  {
    super(ExecOptType.EXEC_BIN_JOIN, new BinJoinState(ec), ec);
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
  @Override
  public void setInnerExtSyn(ExternalSynopsis syn)
  {
    this.innerExtSyn = syn;
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
   * @return Returns the joinSyn.
   */
  public LineageSynopsis getJoinSyn()
  {
    return joinSyn;
  }

  /**
   * @param joinSyn
   *          The joinSyn to set.
   */
  public void setJoinSyn(LineageSynopsis joinSyn)
  {
    this.joinSyn = joinSyn;
  }

  /**
   * @return Returns the outerScanId.
   */
  public int getOuterScanId()
  {
    return outerScanId;
  }

  /**
   * @param outerScanId
   *          The outerScanId to set.
   */
  public void setOuterScanId(int outerScanId)
  {
    this.outerScanId = outerScanId;
  }

  /**
   * @return Returns the outerSyn.
   */
  public RelationSynopsis getOuterSyn()
  {
    return outerSyn;
  }

  /**
   * @param outerSyn
   *          The outerSyn to set.
   */
  public void setOuterSyn(RelationSynopsis outerSyn)
  {
    this.outerSyn = outerSyn;
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
   * Setter for innerSilentRelns
   * 
   * @param innerSilentRelns
   *          The innerSilentRelns to set.
   */
  public void setInnerSilentRelns(boolean innerSilentRelns)
  {
    this.innerSilentRelns = innerSilentRelns;
  }

  /**
   * set which input is dependent on archived dimension.
   * @param archivedDim INNER or OUTER
   */
  public void setArchivedDim(int archivedDim) 
  {
    this.archivedDim = archivedDim;
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
   * @param outerJoinType
   *          The outerJoinType to set.
   */
  public void setOuterJoinType(OuterJoinType outerJoinType)
  {
    this.outerJoinType = outerJoinType;
  }

  /**
   * Getter of outerjoin type
   * 
   * @return Returns the outer join type
   */
  public OuterJoinType getOuterJoinType()
  { 
    return this.outerJoinType;
  }
  
  /**
   * @param nullLefTuple
   *          The nullLefTuple to set.
   */
  public void setNullLefTuple(ITuplePtr nullLefTuple)
  {
    this.nullLeftTuple = nullLefTuple;
  }

  /**
   * @param nullRightTuple
   *          The nullRightTuple to set.
   */
  public void setNullRightTuple(ITuplePtr nullRightTuple)
  {
    this.nullRightTuple = nullRightTuple;
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
    BinJoinState s = (BinJoinState) mut_state;
    boolean exitState = true;

    assert s.state != ExecState.S_UNINIT;
    assert s.stats.getNumInputs() == 0 : s.stats.getNumInputs();

    // Join Synopsis cannot be null for OuterJoin as negative tuples need to be
    // outputed
    if (joinSyn == null && outerJoinType != null)
      assert false;

    boolean done = false;
    
    //update stats
    s.stats.incrNumExecutions();

    try
    {
      numElements = timeSlice;      
      while ((s.stats.getNumInputs() < numElements) || (!exitState))
      {
        switch (s.state)
        {
          case S_PROPAGATE_OLD_DATA:
            if(oldDataPropNeeded)
            {
              setExecSynopsis((ExecSynopsis) joinSyn);
              handlePropOldData();
            }
            else
            {
              oldDataPropNeeded = true;
              s.state = s.lastState;
            }
            break;
          case S_INIT:
          {
            innerPeekElement = null;
            outerPeekElement = null;
            // Peek to revise min outer timestamp estimate
            outerPeekElement = outerInputQueue.peek(s.outerPeekElementBuf);

            if (outerPeekElement != null)
            {
              s.outerMinTs = outerPeekElement.getTs();
            }
            else
            {
              // Minimum timestamp possible on the next outer element
              s.outerMinTs = s.minNextOuterTs;
            }

            // Peek to revise min inner timestamp estimate
            innerPeekElement = innerInputQueue.peek(s.innerPeekElementBuf);

            if (innerPeekElement != null)
            {
              s.innerMinTs = innerPeekElement.getTs();
            }
            else
              // Minimum timestamp possible on the next inner element
              s.innerMinTs = s.minNextInnerTs;

            // We have to process the outer if it has an element waiting in
            // the queue. Otherwise we cannot do any processing
            if (s.outerMinTs < s.innerMinTs || (isExternal))
            {
              if(outerPeekElement != null)
                s.outerElement = outerInputQueue.dequeue(s.outerElementBuf);
              else
                s.outerElement = null;
              s.state = ExecState.S_OUTER_INPUT_DEQUEUED;
            }
            else if (s.innerMinTs < s.outerMinTs)
            {
              if(innerPeekElement != null)
                s.innerElement = innerInputQueue.dequeue(s.innerElementBuf);
              else
                s.innerElement = null;
              s.state = ExecState.S_INNER_INPUT_DEQUEUED;
              break;
            }
            else if (outerPeekElement != null)
            {
              s.outerElement = outerInputQueue.dequeue(s.outerElementBuf);
              s.state = ExecState.S_OUTER_INPUT_DEQUEUED;
            }
            else if (innerPeekElement != null)
            {
              s.innerElement = innerInputQueue.dequeue(s.innerElementBuf);
              s.state = ExecState.S_INNER_INPUT_DEQUEUED;
              break;
            }
            else
            {
              s.state = ExecState.S_GENERATE_HEARTBEAT;
              break;
            }
          }

          case S_OUTER_INPUT_DEQUEUED:
            if (s.outerElement == null)
            {
              if(outerSystsSourceLineage != null)
              {
                if(innerPeekElement != null && 
                   outerSystsSourceLineage.size() > 0)
                {
                  // inner queue has some inputs which are waiting for an element
                  // in the outer queue
                  requestForHeartbeat(outerSystsSourceLineage,
                                      Constants.OUTER, 
                                      innerPeekElement.getTs());
                }
              }
              s.state = ExecState.S_GENERATE_HEARTBEAT;                        
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
              

              if (s.lastOuterTs > s.outerTs)
              {
                s.state = ExecState.S_INIT;
                throw ExecException.OutOfOrderException(
                        this,
                        s.lastOuterTs, 
                        s.outerTs, 
                        s.outerElement.toString());
              }

              assert s.outerTs >= s.minNextOuterTs : getDebugInfo(s.outerTs,
                s.minNextOuterTs, s.outerElement.getKind().name(),
                s.lastLeftKind.name());

              s.minNextOuterTs = s.outerElement.getTotalOrderingGuarantee() ?
                                 s.outerTs + 1 :
                                 s.outerTs;

              // We should have a progress of time.
              // Update the last input Ts now
              s.lastOuterTs = s.outerTs;
              // last Input Ts can be either last inner Ts or last outer Ts
              s.lastInputTs = s.lastOuterTs;
              s.lastLeftKind = s.outerElement.getKind();

              // Next element to be outputted
              s.nextOutputTs = s.outerElement.getTs();
              s.outerTuple = s.outerElement.getTuple();
              
              //copy over the snapshotid from the outerElement into outerTuple
              if(s.outerTuple != null)
                s.outerTuple.setSnapshotId(s.outerElement.getSnapshotId());

              s.nextElementKind = s.outerElement.getKind();

              if (s.outerElement.getKind() == QueueElement.Kind.E_PLUS)
              {
                // Bind the output
                evalContext.bind(s.outerTuple, IEvalContext.OUTER_ROLE);
                s.state = ExecState.S_PROCESS_OUTER_PLUS;
              }
              else if (s.outerElement.getKind() == QueueElement.Kind.E_MINUS)
              {
                // Bind the output
                evalContext.bind(s.outerTuple, IEvalContext.OUTER_ROLE);
                s.state = ExecState.S_PROCESS_OUTER_MINUS;
              }
              // nothing to be done for heartbeats
              else
                s.state = ExecState.S_OUTER_INPUT_ELEM_CONSUMED;

              exitState = false;
            }

            break;

          case S_PROCESS_INNER_PLUS:
            //Here the snapshot id of innertuple will have been set to
            //snapshot id of innerElement
            s.outerScanEmpty = false;
            s.outputNegOuterNull = false;
            // Insert the inner tuple into innerSynopsis
            if (!isArchived() ||  
                getArchivedDim() == Constants.INNER)
            { 
              // ALWAYS maintain in the following three cases:
              // o all non-archived (regular CEP use cases)
              // o archived dimensions
              // o Fact table that is part of a join with a slow
              //   changin dimension but is not part of the view query
              //   (View query check is made in PlanManager and it clears
              //   the flag if not view query)
              innerSyn.insertTuple(s.innerTuple);
              if(journalEntry != null)
                updateJournalEntry(s.innerTuple, true, true);
            }
            s.state = ExecState.S_PROCESS_OUTER_SCAN;

          case S_PROCESS_OUTER_SCAN:
            if (s.outerElement != null)
            {
              s.outerElement = null;
            }
            // Scan of outer tuples that join with inner tuple
            s.innerMatchCount = 0;
            s.outerScan = outerSyn.getScan(outerScanId);
            s.nextScannedTuple = s.outerScan.getNext();
            s.state = ExecState.S_PROCESS_GET_NEXT_OUTER_ELEM;
            s.tmpState = s.state;

          case S_PROCESS_GET_NEXT_OUTER_ELEM:
            if (s.outerTuple != null && s.outerTuple != nullLeftTuple)
            {
              outerTupleStorageAlloc.release(s.outerTuple);
            }

            s.outerTuple = s.nextScannedTuple;
            // re-initialize to false
            s.nextOutputOrderingFlag = false;
            
            if (s.outerTuple == null)
            {
              // No more tuples
              if (outerJoinType != null)
              {
                assert (!s.innerMatchHash.containsKey(s.innerTuple));
                s.innerMatchHash.put(s.innerTuple, s.innerMatchCount);
              }
              outerSyn.releaseScan(outerScanId, s.outerScan);
              if (s.innerMatchCount == 0
                  && (outerJoinType == OuterJoinType.RIGHT_OUTER || 
                      outerJoinType == OuterJoinType.FULL_OUTER))
              {
                s.outerTuple = nullLeftTuple;
                s.outerScanEmpty = true;
                evalContext.bind(s.outerTuple, IEvalContext.OUTER_ROLE);
                s.nextOutputOrderingFlag = false;
                s.state = ExecState.S_ALLO_POPU_OUTPUT_TUPLE;
                break;
              }
              else
              {
                s.state = ExecState.S_INNER_INPUT_ELEM_CONSUMED;
                break;
              }
            }
            else
            {
              s.nextScannedTuple = s.outerScan.getNext();
              if (outerJoinType != null)
              {
                assert (s.outerMatchHash.containsKey(s.outerTuple));
                int count = s.outerMatchHash.get(s.outerTuple);
                assert (count >= 0);
                if (count == 0 && (outerJoinType == OuterJoinType.LEFT_OUTER ||outerJoinType == OuterJoinType.FULL_OUTER))
                {
                  s.outputNegOuterNull = true;
                }
                // (outer,count) -> (outer,count+1)
                s.outerMatchHash.remove(s.outerTuple);
                s.outerMatchHash.put(s.outerTuple, count + 1);
              }

              s.innerMatchCount++;
              evalContext.bind(s.outerTuple, IEvalContext.OUTER_ROLE);
              // If there are no more elements to scan further
              // and both next inner and next outer ts is going be greater
              // and both next inner and outer ts are going to be bigger than
              // output ts.
              // if inner is external then we will not receive any tuple
              // for inner
              // greater than current inner and outer ts respt.
              if((s.nextScannedTuple == null) &&
                  (
                     ((s.minNextInnerTs > s.innerTs) && (s.minNextInnerTs > s.nextOutputTs))
                     || (isExternal) 
                   ) &&
                  ((s.minNextOuterTs > s.outerTs) && (s.minNextOuterTs > s.nextOutputTs)) &&
                  !s.outputNegOuterNull
                )
                s.nextOutputOrderingFlag = true;
              
              s.state = ExecState.S_ALLO_POPU_OUTPUT_TUPLE;
              break;
            }

          case S_OUTPUT_NEG_OUTER_NULL:
            if (joinSyn != null)
            {
              ITuplePtr[] lineage = new ITuplePtr[2];

              lineage[0] = s.outerTuple;
              lineage[1] = nullRightTuple;

              TupleIterator joinScan = joinSyn.getScan_l(lineage);
              s.outputTuple = joinScan.getNext();
              joinSyn.releaseScan_l(joinScan);

              if (!isArchived()) 
                // ideally we should not scan joinSyn at all if special
                // archived fact-dim join.
                // FIXME: fix it in a follow-up, if possible. 
                // too much of the code will need null check for s.outputTuple.
                assert s.outputTuple != null;
            }
            
            long snapshotId = Long.MAX_VALUE;
            
            if(s.outerTuple.getSnapshotId() >= s.innerTuple.getSnapshotId()) 
            {
              snapshotId = s.outerTuple.getSnapshotId();
            }
            else
            {
              snapshotId = s.innerTuple.getSnapshotId();
            }
            if (s.outputTuple != null) 
              // could be null in special archived fact x dimension join case.
              s.outputTuple.setSnapshotId(snapshotId);
            
            s.tmpElementKind = s.nextElementKind;
            s.nextElementKind = QueueElement.Kind.E_MINUS;
            s.state = ExecState.S_OUTPUT_TIMESTAMP;
            break;

          case S_PROCESS_OUTER_PLUS:
            //Here the outertuple has snapshotid set from outerElement.
            s.innerScanEmpty = false;
            s.outputNegNullInner = false;
            // Insert the outer tuple into outerSynopsis
            if (!isArchived() ||  
                getArchivedDim() == Constants.OUTER)
            { 
              // ALWAYS maintain in the following three cases:
              // o all non-archived (regular CEP use cases)
              // o archived dimensions
              // o Fact table that is part of a join with a slow
              //   changin dimension but is not part of the view query
              //   (View query check is made in PlanManager and it clears
              //   the flag if not view query)
              outerSyn.insertTuple(s.outerTuple);
              if(journalEntry != null)
                updateJournalEntry(s.outerTuple, true, false);
            }             
            s.state = ExecState.S_PROCESS_INNER_SCAN;

          case S_PROCESS_INNER_SCAN:
            // Scan of inner tuples that join with outer tuple
            if(isExternal)
              s.innerScan = innerExtSyn.getScan(evalContext);
            else
              s.innerScan = innerSyn.getScan(innerScanId);
            s.outerMatchCount = 0;
            s.state = ExecState.S_PROCESS_GET_NEXT_INNER_ELEM;
            // lookahead a tuple
            s.nextScannedTuple = s.innerScan.getNext();
            s.tmpState = s.state;

          case S_PROCESS_GET_NEXT_INNER_ELEM:
            if (s.innerElement != null)
            {
              s.innerElement = null;
            }

            if (s.innerTuple != null && s.innerTuple != nullRightTuple)
            {
              innerTupleStorageAlloc.release(s.innerTuple);
            }

            s.innerTuple = s.nextScannedTuple;
            // re-initialize the flag
            s.nextOutputOrderingFlag = false;
            if (s.innerTuple == null)
            {
              // No more tuples
              if (outerJoinType != null)
              {
                // assert(outer,count) doesnot exist in LMP
                assert (!s.outerMatchHash.containsKey(s.outerTuple));

                // add(outer,outerMatchCount) in LMP if not external relation
                if (!isExternal)
                  s.outerMatchHash.put(s.outerTuple, s.outerMatchCount);
              }

              if(isExternal)
                innerExtSyn.releaseScan(s.innerScan);
              else
                innerSyn.releaseScan(innerScanId, s.innerScan);
              if (s.outerMatchCount == 0
                  && (outerJoinType == OuterJoinType.LEFT_OUTER || outerJoinType == OuterJoinType.FULL_OUTER))
              {
                s.innerTuple = nullRightTuple;
                s.innerScanEmpty = true;
                evalContext.bind(s.innerTuple, IEvalContext.INNER_ROLE);
                s.nextOutputOrderingFlag = false;
                s.state = ExecState.S_ALLO_POPU_OUTPUT_TUPLE;
              }
              else
              {
                s.state = ExecState.S_OUTER_INPUT_ELEM_CONSUMED;
                break;
              }
            }
            else
            {
              s.nextScannedTuple = s.innerScan.getNext();
              // External relation is always INNER, we just iterate through
              // the synopsis to find the corresponding matching tuple
              // We never receive an inner tuple for external relation so never
              // insert into innerMatchHash
              if ((outerJoinType != null) && (!isExternal))
              {
                // assert (innertuple,count) exists in RMP
                assert (s.innerMatchHash.containsKey(s.innerTuple));
                int count = s.innerMatchHash.get(s.innerTuple);

                assert (count >= 0);
                if (count == 0 && (outerJoinType == OuterJoinType.RIGHT_OUTER || outerJoinType == OuterJoinType.FULL_OUTER))
                {
                  s.outputNegNullInner = true;
                }

                // (innertuple,count) -> (innertuple,count+1)
                s.innerMatchHash.remove(s.innerTuple);
                s.innerMatchHash.put(s.innerTuple, count + 1);
              }

              s.outerMatchCount++;
              evalContext.bind(s.innerTuple, IEvalContext.INNER_ROLE);
              // If there are no more tuples to be scanned in the synopsis
              // 
              if((s.nextScannedTuple == null) &&
                 (
                   ((s.minNextInnerTs > s.innerTs) && (s.minNextInnerTs > s.nextOutputTs))
                   || (isExternal) 
                 ) &&
                 ((s.minNextOuterTs > s.outerTs) && (s.minNextOuterTs > s.nextOutputTs)) &&
                 !s.outputNegNullInner
                )
                 s.nextOutputOrderingFlag = true;
              
              s.state = ExecState.S_ALLO_POPU_OUTPUT_TUPLE;
            }
          case S_ALLO_POPU_OUTPUT_TUPLE:
            // allocate space for the output tuple
            s.outputTuple = tupleStorageAlloc.allocate();

            //assign a snapshotid for the output tuple
            //compare the snapshotid of the left and right tuple, greater of 
            //these two would be the snapshotid of the output tuple.
            //if one of the sides is nulltuple then it is the non-null tuple's 
            //snapshotid.
            //both sides cannot be null tuples.
            snapshotId = Long.MAX_VALUE;
            if(s.outerTuple == nullLeftTuple)
              snapshotId = s.innerTuple.getSnapshotId();
            else if(s.innerTuple == nullRightTuple)
              snapshotId = s.outerTuple.getSnapshotId();
            else if(s.outerTuple.getSnapshotId() >= s.innerTuple.getSnapshotId())
            {
              snapshotId = s.outerTuple.getSnapshotId();
            }
            else
            {
              snapshotId = s.innerTuple.getSnapshotId();
            }
                
            s.tmpInnerTuple = s.innerTuple;
            s.tmpOuterTuple = s.outerTuple;
            
            // construct the output tuple
            evalContext.bind(s.outputTuple, IEvalContext.NEW_OUTPUT_ROLE);
            if (s.innerScanEmpty)
              assert (outerJoinType == OuterJoinType.LEFT_OUTER || outerJoinType == OuterJoinType.FULL_OUTER);
            else if (s.outerScanEmpty)
              assert (outerJoinType == OuterJoinType.RIGHT_OUTER || outerJoinType == OuterJoinType.FULL_OUTER);

            outputConstructor.eval(evalContext);
            
            //set the snapshotid for the output
            if (s.outputTuple != null) // see comments for similar code fragment
              s.outputTuple.setSnapshotId(snapshotId);

            // We have to insert this tuple in joinSynopsis (if it exists) to
            // enable us to produce MINUS tuples
            s.state = ExecState.S_POPULATE_JOIN_SYNOPSIS;

          case S_POPULATE_JOIN_SYNOPSIS:
            if (joinSyn != null && (!isExternal))
            {
              /** Tuple array for lineages. 2 comes from "binary" */
              ITuplePtr[] lineage = new ITuplePtr[2];

              // Lineage for a tuple is the set of tuples that produced it.
              lineage[0] = s.outerTuple;// left
              lineage[1] = s.innerTuple;// right

              if (!isArchived())
              {
                joinSyn.insertTuple(s.outputTuple, lineage);
              }
            }
            else if(joinSyn != null && isExternal)
            {
              ITuplePtr[] lineage = new ITuplePtr[1];
              lineage[0] = s.outerTuple;
              joinSyn.insertTuple(s.outputTuple, lineage);
            }
            s.innerTuple = s.tmpInnerTuple;
            s.outerTuple = s.tmpOuterTuple;
            s.state = ExecState.S_OUTPUT_TIMESTAMP;
            break;

          case S_OUTPUT_NEG_NULL_INNER:
            if (joinSyn != null)
            {
              ITuplePtr[] lineage1 = new ITuplePtr[2];

              lineage1[0] = nullLeftTuple;
              lineage1[1] = s.innerTuple;

              TupleIterator joinScan = joinSyn.getScan_l(lineage1);
              s.outputTuple = joinScan.getNext();
              joinSyn.releaseScan_l(joinScan);

              if (!isArchived())
                assert (s.outputTuple != null);
            }
            
            snapshotId = Long.MAX_VALUE;
            
            if(s.outerTuple.getSnapshotId() >= s.innerTuple.getSnapshotId()) 
            {
              snapshotId = s.outerTuple.getSnapshotId();
            }
            else
            {
              snapshotId = s.innerTuple.getSnapshotId();
            }
            
            if (s.outputTuple != null) // see similar comments for such check
              s.outputTuple.setSnapshotId(snapshotId);
            s.tmpElementKind = s.nextElementKind;
            s.nextElementKind = QueueElement.Kind.E_MINUS;
            s.state = ExecState.S_OUTPUT_TIMESTAMP;
            break;

          case S_PROCESS_INNER_MINUS:
            //Here the snapshot id of the innertuple is already set to 
            //snapshotid of innerElement
            s.outerScanEmpty = false;
            s.outputPosOuterNull = false;
            // Delete the inner tuple into innerSynopsis
            if (!isArchived() ||  
                getArchivedDim() == Constants.INNER)
            { 
              // ALWAYS maintain in the following three cases:
              // o all non-archived (regular CEP use cases)
              // o archived dimensions
              // o Fact table that is part of a join with a slow
              //   changin dimension but is not part of the view query
              //   (View query check is made in PlanManager and it clears
              //   the flag if not view query)
              innerSyn.deleteTuple(s.innerTuple);
              if(journalEntry != null)
                updateJournalEntry(s.innerTuple, false, true);
            }
            s.state = ExecState.S_PROCESS_OUTER_SCAN_DEL;

          case S_PROCESS_OUTER_SCAN_DEL:
            // Scan of outer tuples that join with inner tuple
            s.outerScan = outerSyn.getScan(outerScanId);
            s.innerMatchCount = 0;
            s.state = ExecState.S_PROCESS_GET_NEXT_OUTER_ELEM_DEL;
            s.nextScannedTuple = s.outerScan.getNext();
            s.tmpState = s.state;

          case S_PROCESS_GET_NEXT_OUTER_ELEM_DEL:

            s.outerTuple = s.nextScannedTuple;
            s.nextOutputOrderingFlag = false;
            if (s.outerTuple == null)
            {
              // No more tuples
              if (outerJoinType != null)
              {
                // assert (inner,count) exists in RMH and count =
                // innerMatchCount
                assert (s.innerMatchHash.containsKey(s.innerTuple));
                int count = s.innerMatchHash.get(s.innerTuple);
                assert (count == s.innerMatchCount);

                // delete (inner,count) from RMH
                s.innerMatchHash.remove(s.innerTuple);
              }
              outerSyn.releaseScan(outerScanId, s.outerScan);

              if (s.innerMatchCount == 0
                  && (outerJoinType == OuterJoinType.RIGHT_OUTER || outerJoinType == OuterJoinType.FULL_OUTER))
              {
                s.outerTuple = nullLeftTuple;
                s.outerScanEmpty = true;
                s.state = ExecState.S_DELETE_OUTPUT_TUPLE;
                break;
              }
              else
              {
                s.state = ExecState.S_INNER_INPUT_ELEM_CONSUMED;
                break;
              }
            }
            else
            {
              int count = -1;
              s.nextScannedTuple = s.outerScan.getNext();
              if (outerJoinType != null)
              {
                // assert(outer,count) exists in LMH and assert(count>0)
                assert (s.outerMatchHash.containsKey(s.outerTuple));
                count = s.outerMatchHash.get(s.outerTuple);
                assert (count > 0);

                // (outer,count) -> (outer,count-1)
                s.outerMatchHash.remove(s.outerTuple);
                s.outerMatchHash.put(s.outerTuple, count - 1);
              }

              s.innerMatchCount++;
              evalContext.bind(s.outerTuple, IEvalContext.OUTER_ROLE);

              if (count == 1 && (outerJoinType == OuterJoinType.LEFT_OUTER || outerJoinType == OuterJoinType.FULL_OUTER))
              {
                s.outputPosOuterNull = true;
                s.state = ExecState.S_OUTPUT_POS_OUTER_NULL;
                break;
              }
              else
              {
                s.state = ExecState.S_DELETE_OUTPUT_TUPLE;
                if((s.nextScannedTuple == null) &&
                  (((s.minNextInnerTs > s.innerTs) &&
                    (s.minNextInnerTs > s.nextOutputTs)) || (isExternal) ) &&
                  ((s.minNextOuterTs > s.outerTs) && 
                   (s.minNextOuterTs > s.nextOutputTs)))
                  s.nextOutputOrderingFlag = true;
                
                break;
              }
            }

          case S_OUTPUT_POS_OUTER_NULL:
            s.outputTuple = tupleStorageAlloc.allocate();
            evalContext.bind(s.outputTuple, IEvalContext.NEW_OUTPUT_ROLE);
            
            //The +(outer, null) tuple is being sent on receiving 
            //an inner minus tuple.
            //assign a snapshotid for the output tuple
            //compare the snapshotid of the left and right tuple, greater of 
            //these two would be the snapshotid of the output tuple.
            
            snapshotId = Long.MAX_VALUE;
            
            if(s.outerTuple.getSnapshotId() >= s.innerTuple.getSnapshotId()) 
            {
              snapshotId = s.outerTuple.getSnapshotId();
            }
            else
            {
              snapshotId = s.innerTuple.getSnapshotId();
            }
            
            if (s.outputTuple != null)
              s.outputTuple.setSnapshotId(snapshotId);

            s.tmpInnerTuple = s.innerTuple;
            s.tmpOuterTuple = s.outerTuple;
            s.innerTuple = nullRightTuple;

            evalContext.bind(nullRightTuple, IEvalContext.INNER_ROLE);
            outputConstructor.eval(evalContext);
            // restore inner role
            evalContext.bind(s.tmpInnerTuple, IEvalContext.INNER_ROLE);
            s.tmpElementKind = s.nextElementKind;
            s.nextElementKind = QueueElement.Kind.E_PLUS;

            s.state = ExecState.S_POPULATE_JOIN_SYNOPSIS;
            break;

          case S_PROCESS_OUTER_MINUS:
            // Here the snapshot id of the outertuple is already set to 
            // snapshotid of outerElement
            // Delete the outer tuple into outerSynopsis
            s.innerScanEmpty = false;
            s.outputPosNullInner = false;
            if (!isArchived() ||  
                getArchivedDim() == Constants.OUTER)
            { 
              // ALWAYS maintain in the following three cases:
              // o all non-archived (regular CEP use cases)
              // o archived dimensions
              // o Fact table that is part of a join with a slow
              //   changin dimension but is not part of the view query
              //   (View query check is made in PlanManager and it clears
              //   the flag if not view query)
              outerSyn.deleteTuple(s.outerTuple);
              if(journalEntry != null)
                updateJournalEntry(s.outerTuple, false, false);
            }
            if(isExternal)
            { 
              // In case of external relations we will go ahead
              // and remove all the tuples having the lineage
              // which contains outer tuple
              s.state = ExecState.S_PROCESS_JOIN_SCAN_DEL;
              break;
            }
            else
              s.state = ExecState.S_PROCESS_INNER_SCAN_DEL;

          case S_PROCESS_INNER_SCAN_DEL:
            // Scan of inner tuples that join with outer tuple
            s.innerScan = innerSyn.getScan(innerScanId);
            s.outerMatchCount = 0;
            s.state = ExecState.S_PROCESS_GET_NEXT_INNER_ELEM_DEL;
            s.nextScannedTuple = s.innerScan.getNext();
            s.tmpState = s.state;

          case S_PROCESS_GET_NEXT_INNER_ELEM_DEL:
            if (s.innerTuple != null && s.innerTuple != nullRightTuple)
            {
              innerTupleStorageAlloc.release(s.innerTuple);
            }

            s.innerTuple = s.nextScannedTuple;
            s.nextOutputOrderingFlag = false;
            if (s.innerTuple == null)
            {
              // No more tuples
              if (outerJoinType != null)
              {
                // assert(outer,count) exists in LMP and count = outerMatchCount
            	/*
            	 * If this is archived view along with slow-changing dimension (dim on right)
            	 * then if we are processing a MINUS corresponding to a PLUS 
            	 * which had come in history data then outerMatchHash assertion will fail.
            	 * So bypass this check.
            	 */
            	if(!isArchived() || (getArchivedDim() != Constants.INNER)){
            	  
                  assert (s.outerMatchHash.containsKey(s.outerTuple)) : s.outerTuple;
                  int count = s.outerMatchHash.get(s.outerTuple);
                  assert (count == s.outerMatchCount);
            	}

                // delete(outer,count) from LMP
                s.outerMatchHash.remove(s.outerTuple);
              }
              innerSyn.releaseScan(innerScanId, s.innerScan);

              if (s.outerMatchCount == 0
                   && ( outerJoinType == OuterJoinType.LEFT_OUTER|| outerJoinType == OuterJoinType.FULL_OUTER))
              {
                s.innerTuple = nullRightTuple;
                s.innerScanEmpty = true;
                //the input tuple needs to be bound to the role.
                //This was not necessary as join synopsis was never null.
                evalContext.bind(s.innerTuple, IEvalContext.INNER_ROLE);
                s.state = ExecState.S_DELETE_OUTPUT_TUPLE;
                break;
              }
              else
              {
                s.state = ExecState.S_OUTER_INPUT_ELEM_CONSUMED;
                break;
              }
            }
            else
            {
              int count = -1;
              s.nextScannedTuple = s.innerScan.getNext();
              if (outerJoinType != null)
              {
                // assert(inner,count) exists in RMP and assert(count>0)
            	if(!isArchived() || (getArchivedDim() != Constants.INNER))  
                  assert (s.innerMatchHash.containsKey(s.innerTuple)) : s.innerTuple+" "+s.nextScannedTuple+" outer tuple "+s.outerTuple;
                count = s.innerMatchHash.get(s.innerTuple);
                if(!isArchived() || (getArchivedDim() != Constants.INNER))  
                  assert (count > 0) : count;

                // (inner,count) -> (inner,count-1) in RMP
                s.innerMatchHash.remove(s.innerTuple);
                s.innerMatchHash.put(s.innerTuple, count - 1);
              }

              s.outerMatchCount++;
              evalContext.bind(s.innerTuple, IEvalContext.INNER_ROLE);

              if (count == 1 && (outerJoinType == OuterJoinType.RIGHT_OUTER|| outerJoinType == OuterJoinType.FULL_OUTER))
              {
                // enqueue (null,inner)
                s.outputPosNullInner = true;
                s.state = ExecState.S_OUTPUT_POS_NULL_INNER;
                break;
              }
              else
              {
                s.state = ExecState.S_DELETE_OUTPUT_TUPLE;
                if((s.nextScannedTuple == null) &&
                   (
                     ((s.minNextInnerTs > s.innerTs) && (s.minNextInnerTs > s.nextOutputTs))
                     || (isExternal) 
                   ) &&
                   ((s.minNextOuterTs > s.outerTs) && (s.minNextOuterTs > s.nextOutputTs))                   
                  ) 
                  s.nextOutputOrderingFlag = true;
              }
            }

          case S_DELETE_OUTPUT_TUPLE:
            
            // can join synopsis be null in any case other than slow-changing dim join?	
            if (!isArchived())
            {
              assert joinSyn != null : "Join synopsis is null for "+this.getOptName();
              /** Tuple array for lineages. 2 comes from "binary" */
              ITuplePtr[] lineage = new ITuplePtr[2];

              // Lineage for a tuple is the set of tuples that produced it.
              lineage[0] = s.outerTuple;
              lineage[1] = s.innerTuple;
              
              TupleIterator joinScan = joinSyn.getScan_l(lineage);
              s.outputTuple = joinScan.getNext();
              joinSyn.releaseScan_l(joinScan);

              assert s.outputTuple != null;
            }
            else 
            {
              // for the special case of archived fact and dimension join
               // where we don't maintain the output synopsis, we need to
              // manufacture the output tuple from the left and right side
              // on the fly.
              s.tmpInnerTuple = s.innerTuple;
              s.tmpOuterTuple = s.outerTuple;
                
              // #(16937761): allocate space before evaluating.
              s.outputTuple = tupleStorageAlloc.allocate();

              // construct the output tuple
              evalContext.bind(s.outputTuple, IEvalContext.NEW_OUTPUT_ROLE);
              outputConstructor.eval(evalContext);
              assert s.outputTuple != null;
            }              
            
            //assign a snapshotid for the output tuple
            //compare the snapshotid of the left and right tuple, greater of 
            //these two would be the snapshotid of the output tuple.
            //if one of the sides is nulltuple then it is the non-null tuple's 
            //snapshotid.
            //both sides cannot be null tuples.
            snapshotId = Long.MAX_VALUE;
            if(s.outerTuple == nullLeftTuple)
              snapshotId = s.innerTuple.getSnapshotId();
            else if(s.innerTuple == nullRightTuple)
              snapshotId = s.outerTuple.getSnapshotId();
            else if(s.outerTuple.getSnapshotId() >= s.innerTuple.getSnapshotId()) 
            {
              snapshotId = s.outerTuple.getSnapshotId();
            }
            else
            {
              snapshotId = s.innerTuple.getSnapshotId();
            }
            
            //set the snapshotid for the output
            if(s.outputTuple != null)
              s.outputTuple.setSnapshotId(snapshotId);
            
            s.state = ExecState.S_OUTPUT_TIMESTAMP;
            break;
         
          case S_PROCESS_JOIN_SCAN_DEL:
            // This will be used only when external relation join.
            // not possible with achived relation. So we need not
            // set snapshotid here
            // Since we will not come here, nothing to do for joinSyn
            // for the join of a fact with archived 'dimension'.            
            ITuplePtr[] lineage = new ITuplePtr[1];
            lineage[0] = s.outerTuple;
            TupleIterator joinScan = joinSyn.getScan_l(lineage);
            s.outputTuple = joinScan.getNext();
            s.nextOutputOrderingFlag = false;
            s.tmpState = s.state;
            if(s.outputTuple == null)
             s.state = ExecState.S_OUTER_INPUT_ELEM_CONSUMED;
            else
            {
              s.nextScannedTuple = joinScan.getNext();
              if((s.nextScannedTuple == null)
                && (s.minNextOuterTs > s.outerTs))
                s.nextOutputOrderingFlag = true;
              s.state = ExecState.S_OUTPUT_TIMESTAMP;
            }
            joinSyn.releaseScan_l(joinScan);
            break;
            
          case S_OUTPUT_POS_NULL_INNER:
            s.outputTuple = tupleStorageAlloc.allocate();

            evalContext.bind(s.outputTuple, IEvalContext.NEW_OUTPUT_ROLE);
            
            //Here the +(null, inner) tuple is being output because of
            //a outer minus tuple
            //assign a snapshotid for the output tuple
            //compare the snapshotid of the left and right tuple, greater of 
            //these two would be the snapshotid of the output tuple.
            
            snapshotId = Long.MAX_VALUE;
            
            if(s.outerTuple.getSnapshotId() >= s.innerTuple.getSnapshotId()) 
            {
              snapshotId = s.outerTuple.getSnapshotId();
            }
            else
            {
              snapshotId = s.innerTuple.getSnapshotId();
            }
            
            if (s.outputTuple != null)
              s.outputTuple.setSnapshotId(snapshotId);

            // store the current outer_tuple and inner_tuple
            s.tmpOuterTuple = s.outerTuple;
            s.tmpInnerTuple = s.innerTuple;

            s.outerTuple = nullLeftTuple;

            evalContext.bind(nullLeftTuple, IEvalContext.OUTER_ROLE);
            outputConstructor.eval(evalContext);
            // restore the outer role
            evalContext.bind(s.tmpOuterTuple, IEvalContext.OUTER_ROLE);

            s.tmpElementKind = s.nextElementKind;
            s.nextElementKind = QueueElement.Kind.E_PLUS;

            s.state = ExecState.S_POPULATE_JOIN_SYNOPSIS;
            break;

          case S_INNER_INPUT_DEQUEUED:
            if (s.innerElement == null)
            {
              // outer queue has some elements which are waiting for an input
              // from inner element
              if(innerSystsSourceLineage != null)
              {
                if(outerPeekElement != null &&
                   innerSystsSourceLineage.size() > 0)
                {
                  requestForHeartbeat(innerSystsSourceLineage, 
                                      Constants.INNER, 
                                      outerPeekElement.getTs());
                }
              }
              s.state = ExecState.S_GENERATE_HEARTBEAT;
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

              assert s.innerTs >= s.minNextInnerTs : getDebugInfo(s.innerTs,
                s.minNextInnerTs, s.innerElement.getKind().name(),
                s.lastRightKind.name());

              s.minNextInnerTs = s.innerElement.getTotalOrderingGuarantee() ?
                                 s.innerTs + 1 :
                                 s.innerTs;

              // Update the last input Ts now
              s.lastInnerTs = s.innerTs;
              // last Input Ts can be either last inner Ts or last outer Ts
              s.lastInputTs = s.lastInnerTs;
              s.lastRightKind = s.innerElement.getKind();

              // Next element to be outputted
              s.nextOutputTs = s.innerElement.getTs();
              s.innerTuple = s.innerElement.getTuple();
              s.nextElementKind = s.innerElement.getKind();
              
              //copy over the snapshotid from innerElement to innertuple
              if(s.innerTuple != null)
                s.innerTuple.setSnapshotId(s.innerElement.getSnapshotId());

              if (s.innerElement.getKind() == QueueElement.Kind.E_PLUS)
              {
                // Bind the output
                evalContext.bind(s.innerTuple, IEvalContext.INNER_ROLE);
                s.state = ExecState.S_PROCESS_INNER_PLUS;
              }
              else if (s.innerElement.getKind() == QueueElement.Kind.E_MINUS)
              {
                // Bind the output
                evalContext.bind(s.innerTuple, IEvalContext.INNER_ROLE);
                s.state = ExecState.S_PROCESS_INNER_MINUS;
              }
              // nothing to be done for heartbeats
              else
                s.state = ExecState.S_INNER_INPUT_ELEM_CONSUMED;

              exitState = false;
            }
            break;

          case S_GENERATE_HEARTBEAT:
            if((s.lastOutputTs < s.outerMinTs) && isExternal)
            {
              s.lastOutputTs = s.outerMinTs;
              s.outputElement.heartBeat(s.lastOutputTs);
              s.nextOutputOrderingFlag = (s.minNextOuterTs > s.outerMinTs);
              s.outputElement.setTotalOrderingGuarantee(s.nextOutputOrderingFlag);
              s.state = ExecState.S_OUTPUT_READY;
              s.tmpState = ExecState.S_INIT;
              break;
            }
            else if ((s.lastOutputTs < s.innerMinTs)
                && (s.lastOutputTs < s.outerMinTs))
            {
              if (s.outerMinTs < s.innerMinTs)
              {
                s.lastOutputTs = s.outerMinTs;
                s.nextOutputOrderingFlag = (s.minNextOuterTs > s.outerMinTs);
                s.outputElement.setSnapshotId(s.prevOuterSnapshotId);
              }
              else
              {
                s.lastOutputTs = s.innerMinTs;
                s.nextOutputOrderingFlag 
                  = (s.innerMinTs == s.outerMinTs) ?
                    (s.minNextInnerTs > s.innerMinTs && 
                     s.minNextOuterTs > s.outerMinTs) 
                     :
                    (s.minNextInnerTs > s.innerMinTs);
                if(s.innerMinTs < s.outerMinTs)
                  s.outputElement.setSnapshotId(s.prevInnerSnapshotId);
                else 
                {
                  //s.innerMinTs == s.outerMinTs
                  //assign the smaller of the two, for the heartbeat.
                  if(s.prevOuterSnapshotId <= s.prevInnerSnapshotId)
                    s.outputElement.setSnapshotId(s.prevOuterSnapshotId);
                  else
                    s.outputElement.setSnapshotId(s.prevInnerSnapshotId);
                }
              }
              s.outputElement.heartBeat(s.lastOutputTs);
              s.outputElement.setTotalOrderingGuarantee(s.nextOutputOrderingFlag);
              s.state = ExecState.S_OUTPUT_READY;
              s.tmpState = ExecState.S_INIT;
              break;
            }
            else
            {
              s.state = ExecState.S_INIT;
              done = true;
              break;
            }
          case S_OUTPUT_TIMESTAMP:
            s.outputTs = s.nextOutputTs;
            s.state = ExecState.S_OUTPUT_ELEMENT;

          case S_OUTPUT_ELEMENT:
            s.outputElement.setKind(s.nextElementKind);
            s.outputElement.setTs(s.outputTs);
            s.outputElement.setTuple(s.outputTuple);
            //set event id val as tuple id if needed
            if(this.shouldUseEventIdVal())
            {
              assert eventIdColNum != -1 : "eventIdColNum not set in "
                                           +this.getOptName();
              
              if(s.outputTuple != null)
              {
                ITuple outTuple = s.outputTuple.pinTuple(IPinnable.WRITE);             
                //use event identifier col value as tuple id if needed
                outTuple.setId(outTuple.lValueGet(eventIdColNum));
                s.outputTuple.unpinTuple();
              }
            }
            // set the snapshotid in the tuple into the element.
            if (s.outputTuple != null)
              s.outputElement.setSnapshotId(s.outputTuple.getSnapshotId());
            
            s.outputElement.setTotalOrderingGuarantee(s.nextOutputOrderingFlag);
            s.lastOutputTs = s.outputElement.getTs();
            s.state = ExecState.S_OUTPUT_READY;

          case S_OUTPUT_READY:
            if (outputQueue.isFull())
            {
              done = true;
              break;
            }
            // For external relations we need to delete all the tuples
            // having the lineage all together because we are scanning joinSyn
            if ((s.outputElement.getKind() == QueueElement.Kind.E_MINUS) && 
                (joinSyn != null))
            {
              if (!isArchived())
                joinSyn.deleteTuple(s.outputTuple);
            }
            outputQueue.enqueue(s.outputElement);
            
            s.state = ExecState.S_OUTPUT_ENQUEUED;
            
          case S_OUTPUT_ENQUEUED:
              
            if(s.outputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
              s.stats.incrNumOutputHeartbeats();
            else
              s.stats.incrNumOutputs();
            
            //restore element kind
            if(s.tmpElementKind != null) {
              s.nextElementKind = s.tmpElementKind;
              s.tmpElementKind = null;
            }

            // if there is no further tuple in inner scan
            if (s.innerScanEmpty)
            {
              s.innerScanEmpty = false;
              if (s.outerTuple != null)
              {
                outerTupleStorageAlloc.release(s.outerTuple);
                s.outerTuple = null;
              }
              if (s.outerElement != null)
              {
                s.outerElement = null;
              }
              s.state = ExecState.S_INIT;
              break;
            }

            // if there is no further tuple in outer scan
            if (s.outerScanEmpty)
            {
              if (s.innerTuple != null)
              {
                innerTupleStorageAlloc.release(s.innerTuple);
                s.innerTuple = null;
              }
              if (s.innerElement != null)
              {
                s.innerElement = null;
              }
              s.outerScanEmpty = false;
              s.state = ExecState.S_INIT;
              break;
            }

            // output the tuple negative (null,inner)
            if (s.outputNegNullInner)
            {
              s.outputNegNullInner = false;
              s.state = ExecState.S_OUTPUT_NEG_NULL_INNER;
              break;
            }

            // after output of tuple positive (null,inner)
            // output negative (outer,inner)
            if (s.outputPosNullInner)
            {
              s.outputPosNullInner = false;
              s.state = ExecState.S_DELETE_OUTPUT_TUPLE;
              break;
            }

            // output the tuple negative (outer,null)
            if (s.outputNegOuterNull)
            {
              s.outputNegOuterNull = false;
              s.state = ExecState.S_OUTPUT_NEG_OUTER_NULL;
              break;
            }

            // after output of tuple positive (outer,null)
            // otuput nevgative (outer,inner)
            if (s.outputPosOuterNull)
            {
              s.outputPosOuterNull = false;
              s.state = ExecState.S_DELETE_OUTPUT_TUPLE;
              break;
            }

            // Now, get the next tuple in the scan to output
            s.state = s.tmpState;
            break;
            
           case S_OUTER_INPUT_ELEM_CONSUMED:
            assert s.outerElement != null;

            if (s.outerTuple != null)
            {
              outerTupleStorageAlloc.release(s.outerTuple);
              s.outerTuple = null;
            }
            //bug 16820093: 
            //save snapshotid - needed for hb's snapshotid.
            s.prevOuterSnapshotId = s.outerElement.getSnapshotId();
            s.outerElement = null;

            s.state = ExecState.S_INIT;
            exitState = true;
            break;

          case S_INNER_INPUT_ELEM_CONSUMED:
            assert s.innerElement != null;

            if (s.innerTuple != null)
            {
              innerTupleStorageAlloc.release(s.innerTuple);
              s.innerTuple = null;
            }
            //bug 16820093: 
            //save snapshotid - needed for hb's snapshotid.
            s.prevInnerSnapshotId = s.innerElement.getSnapshotId();
            s.innerElement = null;

            s.state = ExecState.S_INIT;
            exitState = true;
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
      // Ignore it for now
      return 0;
    }
    
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
    BinJoinState s = (BinJoinState) mut_state;    
    return s.lastOutputTs < s.innerMinTs && s.lastOutputTs < s.outerMinTs;
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
   BinJoinState m = (BinJoinState) mut_state;
   if (n == Constants.OUTER) return m.lastOuterTs;
   if (n == Constants.INNER) return m.lastInnerTs;
   assert false : "shouldn't be called";
   return -1;
 }

 protected boolean isSilentInput(int n)
 {
   if (n == Constants.OUTER) return outerSilentRelns;
   if (n == Constants.INNER) return innerSilentRelns;
   //assert false : "shouldn't be called";
   return false;
 } 

 /*
  * which side for this join is slow chaging dimension (INNER or OUTER)
  */
 protected int getArchivedDim()
 {
   return archivedDim;
 }

 public int getInnerFullScanId()
{
  return innerFullScanId;
}

public void setInnerFullScanId(int innerFullScanId)
{
  this.innerFullScanId = innerFullScanId;
}

public int getOuterFullScanId()
{
  return outerFullScanId;
}

public void setOuterFullScanId(int outerFullScanId)
{
  this.outerFullScanId = outerFullScanId;
}

/*
  * This join represent a join of a fact table and a slow dimension.
  */
 protected boolean isArchived()
 {
   return archivedDim != Constants.INVALID_VALUE;
 }

 public boolean isNeedBindToRoleOnRecovery()
{
  return needBindToRoleOnRecovery;
}

public void setNeedBindToRoleOnRecovery(boolean needBindToRoleOnRecovery)
{
  this.needBindToRoleOnRecovery = needBindToRoleOnRecovery;
}

/*
  * This method does not alter state/synopsis of join. It merely
  * propagates the archived tuples downstream (to the operators in the
  * the query plan of query being started). This is needed to handle
  * the cases where first operator immediately downstream to join
  * (which is also a view root) cannot construct its archiver query.
  * e.g. select signum1(c1) from ldo;
  * signum1 does not have a sql equivalent so querying responsibility
  * would fall back on the join (view root).
  */
 public void initializeState() throws CEPException
 {
   BinJoinState s = (BinJoinState) mut_state;
   if(archivedRelationTuples != null)
   {
     for(ITuplePtr currentTuple : archivedRelationTuples)
     {
       s.outputElement.setTuple(currentTuple);
       s.outputElement.setTs(snapShotTime);
       s.outputElement.setKind(QueueElement.Kind.E_PLUS);
       s.outputElement.setTotalOrderingGuarantee(false);
       s.outputElement.setSnapshotId(execContext.getPlanMgr().getCurrentSnapshotId());
       s.lastOutputTs = snapShotTime;
       ((ISharedQueueWriter) outputQueue).enqueue(s.outputElement,
                                                  this.getArchiverReaders());
       //LogUtil.finest(LoggerType.TRACE, "ARF# "+this.getOptName()+
       //               " propagated archiver tuple: "+currentTuple);
       s.stats.incrNumOutputs();
       currentTuple.unpinTuple();
     }
     //send heartbeat with ordering guarantee false
     s.lastOutputTs = snapShotTime + 1;
     heartbeatTime = snapShotTime + 1;
     s.outputElement.heartBeat(s.lastOutputTs);
     s.outputElement.setTotalOrderingGuarantee(false);
     s.outputElement.setKind(QueueElement.Kind.E_HEARTBEAT);
     s.outputElement.setSnapshotId(execContext.getPlanMgr().getCurrentSnapshotId());
     ((ISharedQueueWriter) outputQueue).enqueue(s.outputElement,
                                                this.getArchiverReaders());
     LogUtil.finer(LoggerType.TRACE, "ARF# "+
                   "Initialized state of "+this.getOptName()+
                   ", propagated events received from archiver downstream");
     s.stats.incrNumOutputs();
     if((archivedRelationTuples.size() > 0) && (this.propagationReqd()))
       oldDataPropNeeded = false;

     //remove all the archived relation tuples
     archivedRelationTuples.clear();
   }
 }
 
 public void dump(IDumpContext dumper)
 {
   assert (dumper != null);

   super.dump(dumper);
   BinJoinState s = (BinJoinState) mut_state;

   LogUtil.beginTag(dumper, TAG_INNER_MATCHHASH, LogTags.ARRAY_ATTRIBS, s.innerMatchHash.size());
   
   Set<Entry<ITuplePtr,Integer>> im = s.innerMatchHash.entrySet();
   if (dumper.getLevel() == LogLevel.OPERATOR_DUMP_DETAIL)
   {
     for (Entry<ITuplePtr,Integer> e : im)
     {
       dumper.beginTag(LogTags.HASH_ENTRY, null, null);
       ITuplePtr k = e.getKey();
       if (k != null)
         k.dump(dumper);
       Integer v = e.getValue();
       dumper.writeln(LogTags.VALUE, v);
       dumper.endTag(LogTags.HASH_ENTRY);
     }
   }
   dumper.endTag(TAG_INNER_MATCHHASH);

   LogUtil.beginTag(dumper, TAG_OUTER_MATCHHASH, LogTags.ARRAY_ATTRIBS, s.outerMatchHash.size());
   Set<Entry<ITuplePtr,Integer>> om = s.outerMatchHash.entrySet();
   if (dumper.getLevel() == LogLevel.OPERATOR_DUMP_DETAIL)
   {
     for (Entry<ITuplePtr,Integer> e : om)
     {
       dumper.beginTag(LogTags.HASH_ENTRY, null, null);
       ITuplePtr k = e.getKey();
       if (k != null)
         k.dump(dumper);
       Integer v = e.getValue();
       dumper.writeln(LogTags.VALUE, v);
       dumper.endTag(LogTags.HASH_ENTRY);
     }
   }
   dumper.endTag(TAG_OUTER_MATCHHASH);
 }
 
 
 /**
  * Create snapshot of Binary Join operator by writing the operator state
  * into param java output stream.
  * State of Binary Join operator consists of following:
  * 1. Mutable State
  * 2. Join Synopsis (If output is a relation)
  * 3. Inner Synopsis (If input is non-external relation)
  * 4. Outer Synopsis 
  * 
  * Please note that we will write the state of operator in above sequence, so
  * the loadSnapshot should also read the state in the same sequence.
  * @param output
  * @throws IOException 
  */
 @Override
 protected void createSnapshot(ObjectOutputStream output) throws CEPException
 {   
   try
   {
     // Case-1: If Operator Supports Partial Journaling, Then the Full Snapshot is
     // divided into two components:
     // i)  full snapshot of object which supports journaling
     // ii) full snapshot of object which doesn't support journaling
     
     // Reason to divide: While loading full snapshot, we only want to load
     // component(i) as we need to apply journal snapshot after that.
     // The component(ii) will be saved as pending bytes and will be loaded
     // only when the first event arrives.
     if(usesPartialJournaling())
     {
       // Write Content of Component-1 as described above
       // This will have the full snapshot of all those objects which supports journaling
       ObjStreamFactory fac = ObjStreamUtil.getObjStreamFactory();
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       ObjectOutputStream oos = fac.createObjectOutputStream(baos);
              
       if(isSilentInput(Constants.INNER))
       {
         if(innerSyn != null && innerFullScanId != -1)
           innerSyn.writeExternal(oos, new SynopsisPersistenceContext(innerFullScanId));
         oos.flush();
       }       
       if(isSilentInput(Constants.OUTER))
       {
         if(outerSyn != null && outerFullScanId != -1)
           outerSyn.writeExternal(oos, new SynopsisPersistenceContext(outerFullScanId));
         oos.flush();
       }
       
       byte[] component1 = baos.toByteArray();
       
       // Write Content of Component-2 as described above
       // This will have the full snapshot of all those objects which only supports full snapshot
       ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
       ObjectOutputStream oos2 = fac.createObjectOutputStream(baos2);
       oos2.writeObject((BinJoinState)mut_state);
       if(joinSyn != null)
         joinSyn.writeExternal(oos2);
       
       if(!isSilentInput(Constants.INNER))
       {
         if(innerSyn != null && innerFullScanId != -1)
           innerSyn.writeExternal(oos2, new SynopsisPersistenceContext(innerFullScanId));
         oos2.flush();
       }
       if(!isSilentInput(Constants.OUTER))
       {
         if(outerSyn != null && outerFullScanId != -1)
           outerSyn.writeExternal(oos2, new SynopsisPersistenceContext(outerFullScanId));
         oos2.flush();
       }
       byte[] component2 = baos2.toByteArray();
       
       output.writeObject(component1);
       output.writeObject(component2);
       LogUtil.fine(LoggerType.TRACE, "Operator " + this.getOptName() +
           " has created a full snapshot with two components [num-bytes-for-journaling-objects= " + component1.length +
           " , num-bytes-non-journaling-bytes=" + component2.length + "]");
     }     
     else
     {
       output.writeObject((BinJoinState)mut_state);
       if(joinSyn != null)
         joinSyn.writeExternal(output);
       if(innerSyn != null && innerFullScanId != -1)
         innerSyn.writeExternal(output, new SynopsisPersistenceContext(innerFullScanId));
       if(outerSyn != null && outerFullScanId != -1)
         outerSyn.writeExternal(output, new SynopsisPersistenceContext(outerFullScanId));
     }
   } 
   catch (IOException e)
   {
     LogUtil.logStackTrace(e);
     throw new CEPException(ExecutionError.SNAPSHOT_CREATION_ERROR, e, e.getLocalizedMessage(), this.getOptName());
   }
 }
 
 /**
  * Create snapshot of Binary Join operator by writing the operator state
  * into param java output stream.
  * State of Binary Join operator consists of following:
  * 1. Mutable State
  * 2. Join Synopsis (If output is a relation)
  * 3. Inner Synopsis (If input is non-external relation)
  * 4. Outer Synopsis 
  * 
  * Please note that we will write the state of operator in above sequence, so
  * the loadSnapshot should also read the state in the same sequence.
  * @param output
  * @throws IOException 
  */
 @Override
 protected void loadSnapshot(ObjectInputStream input) throws ExecException
 {   
   try
   {
     // Case-1: If Operator Supports Partial Journaling, Then the Full Snapshot is
     // divided into two components:
     // i)  full snapshot of object which supports journaling
     // ii) full snapshot of object which doesn't support journaling
     
     // Reason to divide: While loading full snapshot, we only want to load
     // component(i) as we need to apply journal snapshot after that.
     // The component(ii) will be saved as pending bytes and will be loaded
     // only when the first event arrives.
     
     // While loading full snapshot, we will only load the component(i)
     if(usesPartialJournaling())
     {
       byte[] component1 = (byte[]) input.readObject();
       byte[] component2 = (byte[]) input.readObject();
       
       ObjStreamFactory fac = ObjStreamUtil.getObjStreamFactory();
       ByteArrayInputStream bais = new ByteArrayInputStream(component1);
       ObjectInputStream ois = fac.createObjectInputStream(bais);
      
       // Load Content of Component-1(as defined above)
       // This will have the full snapshot of all those objects which supports journaling
       IPersistenceContext sharedSynopsisRecoveryCtx = new SynopsisPersistenceContext();
       sharedSynopsisRecoveryCtx.setCache(new HashSet());
       if(isSilentInput(Constants.INNER))
       {
         if(innerSyn != null && innerFullScanId != -1)
         {
           if(isNeedBindToRoleOnRecovery())
             sharedSynopsisRecoveryCtx.setRole(IEvalContext.INNER_ROLE);       
           innerSyn.readExternal(ois, sharedSynopsisRecoveryCtx);
         }
       }
       if(isSilentInput(Constants.OUTER))
       {
         if(outerSyn != null && outerFullScanId != -1)
         {
           if(isNeedBindToRoleOnRecovery())
             sharedSynopsisRecoveryCtx.setRole(IEvalContext.OUTER_ROLE);       
           outerSyn.readExternal(ois, sharedSynopsisRecoveryCtx);
         }
       }
       
       // Pending Content of Component-2.
       // This will have the full snapshot of all those objects which only supports full snapshot
       pendingPartialSnapshotBytes = component2;
       pendingPartialSnapshotVersion = SnapshotContext.CURRENT_VERSION;
       LogUtil.fine(LoggerType.TRACE, "Operator " + this.getOptName() +
         " has restored journaling objects from Partial Full Snapshot");
       LogUtil.fine(LoggerType.TRACE, "Operator " + this.getOptName() + 
         " has a pending Partial Full Snapshot [num-bytes=" + pendingPartialSnapshotBytes.length + "] for non journaling objects");
     }
     else
     {
       BinJoinState loaded_mutable_state = (BinJoinState) input.readObject();
       ((BinJoinState)mut_state).copyFrom(loaded_mutable_state);
       
       if(joinSyn != null)
         joinSyn.readExternal(input);
     
       IPersistenceContext sharedSynopsisRecoveryCtx = new SynopsisPersistenceContext();
       sharedSynopsisRecoveryCtx.setCache(new HashSet());
       // Read output synopsis from input stream
       if(innerSyn != null && innerFullScanId != -1)
       {
         if(isNeedBindToRoleOnRecovery())
           sharedSynopsisRecoveryCtx.setRole(IEvalContext.INNER_ROLE);       
         innerSyn.readExternal(input, sharedSynopsisRecoveryCtx);
       }
       if(outerSyn != null && outerFullScanId != -1)
       {
         if(isNeedBindToRoleOnRecovery())
           sharedSynopsisRecoveryCtx.setRole(IEvalContext.OUTER_ROLE);
         outerSyn.readExternal(input, sharedSynopsisRecoveryCtx);
       }
     }
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
 
 /**
  * Return true iff either left synopsis(relation) or right relation (synopsis) 
  * is based on a silent relation.
  */
 @Override
 public boolean usesPartialJournaling()
 {   
   return SnapshotContext.getVersion() >= SnapshotContext.PARTIAL_SNAPSHOT_IN_BINARY_OP_VERSION &&
          (innerSilentRelns || outerSilentRelns);
 }

 
 /**
  * Create a Partial Full Snapshot for the operator.<p> 
  * This method will take a full snapshot of all the state objects which
  * supports only full snapshot and can't be persisted incrementally.
  * <p>  
  * Sequence of Snapshot Objects
  * MUTABLE_STATE -> JOIN SYNOPSIS -> INNER SYNOPSIS [Optional] -> OUTER SYNOPSIS [Optional]
  */
 @Override
 protected void createPartialSnapshot(ObjectOutputStream output) throws CEPException
 {
   try
   {
     output.writeObject((BinJoinState)mut_state);
     if(joinSyn != null)
       joinSyn.writeExternal(output);
     
     // Don't Persist as a full snapshot if synopsis is based on silent source     
     if(!isSilentInput(Constants.INNER))
     {
       if(innerSyn != null && innerFullScanId != -1)
         innerSyn.writeExternal(output, new SynopsisPersistenceContext(innerFullScanId));
     }
     
     // Don't Persist as a full snapshot if synopsis is based on silent source
     if(!isSilentInput(Constants.OUTER))
     {
       if(outerSyn != null && outerFullScanId != -1)
         outerSyn.writeExternal(output, new SynopsisPersistenceContext(outerFullScanId));
     }
     
   } 
   catch (IOException e)
   {
     LogUtil.logStackTrace(e);
     throw new CEPException(ExecutionError.SNAPSHOT_CREATION_ERROR, e, e.getLocalizedMessage(), this.getOptName());
   }
 }
 
 
 /**
  * Load a Partial Full Snapshot for the operator.<p> 
  * This method will take a full snapshot of all the state objects which
  * supports only full snapshot and can't be persisted incrementally.
  * <p>  
  * Sequence of Snapshot Objects
  * MUTABLE_STATE -> JOIN SYNOPSIS -> INNER SYNOPSIS [Optional] -> OUTER SYNOPSIS [Optional]
  */
 @Override
 protected void loadPartialSnapshot(ObjectInputStream input) throws ExecException
 {   
   try
   {
     BinJoinState loaded_mutable_state = (BinJoinState) input.readObject();
     ((BinJoinState)mut_state).copyFrom(loaded_mutable_state);
     
     if(joinSyn != null)
       joinSyn.readExternal(input);
     
     IPersistenceContext sharedSynopsisRecoveryCtx = new SynopsisPersistenceContext();
     sharedSynopsisRecoveryCtx.setCache(new HashSet());
     
     // Don't Persist as a full snapshot if synopsis is based on silent source
     if(!isSilentInput(Constants.INNER))
     {
       if(innerSyn != null && innerFullScanId != -1)
       {
         if(isNeedBindToRoleOnRecovery())
           sharedSynopsisRecoveryCtx.setRole(IEvalContext.INNER_ROLE);
         innerSyn.readExternal(input, sharedSynopsisRecoveryCtx);
       }     
     }
     // Don't Persist as a full snapshot if synopsis is based on silent source
     if(!isSilentInput(Constants.OUTER))
     {
       if(outerSyn != null && outerFullScanId != -1)
       {
         if(isNeedBindToRoleOnRecovery())
           sharedSynopsisRecoveryCtx.setRole(IEvalContext.OUTER_ROLE);
         outerSyn.readExternal(input, sharedSynopsisRecoveryCtx);
       }
     }

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
  * Start the batch for this operator.<p>
  * This indicates an operator to begin a new journal and start
  * adding new entries for all changes events.
  */
 @Override
 public void startBatch(boolean fullSnapshot) throws CEPException
 {
   super.startBatch(fullSnapshot);
   
  // In case of full snapshot, we don't need to instantiate journal entry
   if(!fullSnapshot)
     journalEntry = new BinJoinJournalEntry();
 }

 /**
  * End the batch for this operator.<p>
  * This indicates an operator to persist the written journal entries
  * into journal stream.
  */
 @Override
 public void endBatch() throws CEPException
 {
   if(journalStream != null)
   {
     // Write journal entry for this batch to journal stream
     super.writeToJournal(journalEntry);     
     LogUtil.fine(LoggerType.TRACE, "Operator:" + this.getOptName() 
         + ", JournalEntry:" + journalEntry);
   }
   // Invoke endBatch to 
   super.endBatch();
 }
 
 /**
  * Apply the journal entry to operator state.
  */
 @Override
 protected void applySnapshot(Object journalEntry) throws ExecException {
   if(journalEntry instanceof BinJoinJournalEntry)
   {
     loadJournalEntry((BinJoinJournalEntry) journalEntry);
   }
   else
       throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR_CNF, 
           this.getOptName(), journalEntry.getClass().getName());
 }
 
 /**
  * Load Journal Entry to append the change events into restored
  * state objects. 
  * @param journalEntry
  * @throws ExecException
  */
 public void loadJournalEntry(BinJoinJournalEntry journalEntry) throws ExecException
 {
   /*
   if(isSilentInput(Constants.INNER))
   {
     Map<Long, ITuplePtr> changeEvents = journalEntry.getInnerMinusChangeEvents();
     Set<Long> minuses = changeEvents.keySet();
     for(long key: minuses)
     {
       ITuplePtr deletedTuple = changeEvents.get(key);
       evalContext.bind(deletedTuple, IEvalContext.INNER_ROLE);
       innerSyn.deleteTuple(deletedTuple);
     }   
     
     changeEvents = journalEntry.getInnerPlusChangeEvents();
     Set<Long> pluses = changeEvents.keySet();
     for(long key: pluses)
     {
       ITuplePtr addedTuple = changeEvents.get(key);
       evalContext.bind(addedTuple, IEvalContext.INNER_ROLE);
       innerSyn.insertTuple(addedTuple);
     }   
   }*/
   
   if(isSilentInput(Constants.INNER))
   {
     // Load Minus Events First
     Map<Long, ITuplePtr> changeEvents = journalEntry.getInnerMinusChangeEvents();
     loadJournalEntryHelper(changeEvents, false, innerSyn, IEvalContext.INNER_ROLE);
     changeEvents = journalEntry.getInnerPlusChangeEvents();
     loadJournalEntryHelper(changeEvents, true, innerSyn, IEvalContext.INNER_ROLE);
     LogUtil.fine(LoggerType.TRACE, "Loaded Journal Entry for Operator in Inner Synopsis:" + this.getOptName() 
         + ", JournalEntry:" + journalEntry);
   }
   
   if(isSilentInput(Constants.OUTER))
   {
    // Load Minus Events First
     Map<Long, ITuplePtr> changeEvents = journalEntry.getOuterMinusChangeEvents();
     loadJournalEntryHelper(changeEvents, false, outerSyn, IEvalContext.OUTER_ROLE);
     changeEvents = journalEntry.getOuterPlusChangeEvents();
     loadJournalEntryHelper(changeEvents, true, outerSyn, IEvalContext.OUTER_ROLE);
     LogUtil.fine(LoggerType.TRACE, "Loaded Journal Entry for Operator in Outer Synopsis:" + this.getOptName() 
         + ", JournalEntry:" + journalEntry);
   }
 }
 
 /**
  * Update Synopsis using journal entry.
  */
 private void loadJournalEntryHelper(Map<Long,ITuplePtr> changeEvents,
                                     boolean isAddition, 
                                     RelationSynopsis targetSyn,
                                     int role) throws ExecException
 {
   Set<Long> keys = changeEvents.keySet();
   for(long key: keys)
   {
     ITuplePtr next = changeEvents.get(key);
     evalContext.bind(next, role);
     if(isAddition)
       targetSyn.insertTuple(next);
     else
       targetSyn.deleteTuple(next);
   }    
 }
 
 
 /**
  * Update Journal Entry by new change event.
  * @param tuple journal entry object
  * @param isAddition true if journal entry for deletion from synopsis
  * @param isInner true if the journal entry for inner synopsis
  *                false if the journal entry for outer synopsis
  */
 public void updateJournalEntry(ITuplePtr tuple, boolean isAddition, boolean isInner)
 {
   if(isInner && isSilentInput(Constants.INNER))
   {     
     if(isAddition)
       journalEntry.getInnerPlusChangeEvents().put(tuple.getId(), tuple);
     else
     {
       if(journalEntry.getInnerPlusChangeEvents().containsKey(tuple.getId()))
         journalEntry.getInnerPlusChangeEvents().remove(tuple.getId());
       else
         journalEntry.getInnerMinusChangeEvents().put(tuple.getId(), tuple);
     }
   }
   else if(!isInner && isSilentInput(Constants.OUTER))
   {
     if(isAddition)
       journalEntry.getOuterPlusChangeEvents().put(tuple.getId(), tuple);
     else
     {
       if(journalEntry.getOuterPlusChangeEvents().containsKey(tuple.getId()))
         journalEntry.getOuterPlusChangeEvents().remove(tuple.getId());
       else
         journalEntry.getOuterMinusChangeEvents().put(tuple.getId(), tuple);
     }
   }
 }
}
