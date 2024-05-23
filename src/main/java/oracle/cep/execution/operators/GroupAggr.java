/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/GroupAggr.java hopark_cqlsnapshot/4 2016/02/26 11:55:07 hopark Exp $ */
/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Execution Layer GROUP/AGGREGATION operator
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    sbishnoi  12/21/15 - adding support for ha snapshot generation
    udeshmuk  08/14/13 - set snapshotid as current snapshotid for archiver
                         records
    udeshmuk  07/10/13 - fix logging related to archived relation framework
    sbishnoi  08/19/12 - bug 14502856
    udeshmuk  05/27/12 - snapshotid propagation and archived flag
    udeshmuk  02/10/12 - send heartbeat after snapshot
    sbishnoi  07/05/11 - XbranchMerge sbishnoi_bug-12722378_ps5 from
                         st_pcbpel_11.1.1.4.0
    sbishnoi  07/05/11 - XbranchMerge sbishnoi_fix_groupaggr_npe from main
    udeshmuk  06/24/11 - support archived relation
    udeshmuk  04/18/11 - archived relation support
    anasrini  12/19/10 - replace eval() with eval(ec)
    parujain  04/10/09 - fix heartbeat
    sbishnoi  01/19/09 - total ordering optimization
    hopark    10/10/08 - remove statics
    hopark    10/09/08 - remove statics
    sborah    09/25/08 - update stats
    skmishra  08/14/08 - adding alloc handling for xmlagg
    skmishra  08/08/08 - adding an allocator for orderby index tuples
    sbishnoi  07/21/08 - fix hbt bug related to null row emission(BUG:5856833)
    skmishra  06/13/08 - adding xmlagg
    hopark    02/28/08 - resurrect refcnt
    hopark    12/07/07 - cleanup spill
    hopark    10/30/07 - remove IQueueElement
    hopark    10/22/07 - remove TimeStamp
    parujain  10/04/07 - delete op
    sbishnoi  10/01/07 - modify output tuple allocation
    sbishnoi  09/26/07 - support of dirtySyn
    hopark    09/07/07 - eval refactor
    sbishnoi  07/22/07 - support for combining output tuples of same timestamp
    rkomurav  07/12/07 - restructuring uda
    hopark    07/13/07 - dump stack trace on exception
    parujain  07/03/07 - cleanup
    parujain  06/26/07 - mutable state
    hopark    06/19/07 - cleanup
    hopark    05/24/07 - debug logging
    hopark    05/16/07 - add arguments for OutOfOrderException
    parujain  05/08/07 - monitoring statistics
    najain    04/12/07 - 
    hopark    04/08/07 - fix pincount
    hopark    04/05/07 - memmgr reorg
    najain    04/03/07 - bug fix
    hopark    03/21/07 - add TuplePtr pin
    najain    03/14/07 - cleanup
    najain    03/12/07 - bug fix
    hopark    03/06/07 - spill support
    parujain  02/27/07 - NPE bug
    najain    02/19/07 - bug fix
    hopark    01/29/07 - fix out of order timestamp
    rkomurav  12/15/06 - rework on emit null row when no group by
    parujain  12/19/06 - fullScanId for RelationSynopsis
    parujain  12/08/06 - propagating relations
    rkomurav  12/07/06 - emit count() for zero rows without group by
    najain    11/16/06 - bug fix
    najain    08/31/06 - bug fix: heartbeats not handled
    najain    08/02/06 - refCounting optimizations
    najain    08/10/06 - Add asserts
    najain    07/19/06 - ref-count tuples 
    anasrini  07/17/06 - support for user defined aggregations 
    najain    07/13/06 - ref-count timestamps 
    najain    07/12/06 - ref-count elem protocol 
    najain    07/06/06 - cleanup
    anasrini  05/30/06 - support for GROUP/AGGREGATIOn operator 
    skaluska  03/14/06 - query manager 
    skaluska  02/06/06 - Creation
    skaluska  02/06/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/GroupAggr.java hopark_cqlsnapshot/4 2016/02/26 11:55:07 hopark Exp $
 *  @author  skaluska
 *  @since   1.0
 */

package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.queues.ISharedQueueWriter;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.execution.snapshot.SynopsisPersistenceContext;
import oracle.cep.execution.synopses.ExecSynopsis;
import oracle.cep.execution.synopses.RelationSynopsis;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.service.ExecContext;
import oracle.cep.snapshot.SnapshotContext;

/**
 * @author skaluska
 *
 */
public class GroupAggr extends ExecOpt
{
  /** Output synopsis */
  private RelationSynopsis outSyn;

  /** scan Id for scan on output synopsis */
  // / Scan over outputSynopsis to get the tuple corresponding to the
  // / "current" group - the group of the latest input tuple
  private int              outScanId;

  /** Full Scan identifier */
  private int              fullScanId;

  /** Input synopsis */
  private RelationSynopsis inSyn;

  /** scan Id for scan on input synopsis */
  // / Scan over input synopsis for getting all tuples belonging to a group
  private int              inScanId;
  
  /** full scan id for input synopsis  */
  private int              inFullScanId;
  
  /** Dirty Synopsis */
  private RelationSynopsis dirtySyn;
  
  /** full Scan identifier */
  private int              dirtyFullScanId;
  
  /** index scan identifier */
  private int              dirtyScanId;
  
  /** evaluation context */
  private IEvalContext      evalContext;

  /** Init Evaluator */
  //  initEval creates the first aggr. tuple for a group.
  private IAEval            initEval;

  /** Plus Evaluator */
  // / Consider a group that currently exists in the output
  // / synopsis. When a new PLUS tuple arrives we need to update
  // / this tuple to reflect the new tuple - the plusEval evaluator
  // / does this job.
  private IAEval            plusEval;

  /** Update Evaluator */
  // / This is identical in functionality to plusEval, except that
  // / the evaluation is done in-place [[ Explanation ]]
  private IAEval            updateEval;

  /** Scan Not Required Evaluator */
  // / This evaluator is used while processing MINUS tuples. It
  // / returns true if the aggregate values for a group cannot be
  // / incrementally updated (due to a MAX / MIN aggr) for the latest
  // / MINUS tuple. If it returns true, we need to scan the entire
  // / input (in inputSynopsis) to update our groups aggregates.
  private IBEval            scanNotReqEval;

  /** Arith Evaluator required for scanNotReqEval */
  // / scanNotReqEval needs to evaluate the parameter expressions to
  // / the aggregate functions. This evaluator takes care of that and
  // / has to be evaluated before scanNotReqEval is evaluated.
  private IAEval            arithScanNotReqEval;

  /** Minus Evaluator */
  // / minusEval is the anti-particle of the plusEval - it updates a
  // / groups tuple to reflect the deletion of a tuple. minusEval is
  // / used only if ScanReq() returns false.
  private IAEval            minusEval;

  /** Null Output Eval */
  // / nullOutputEval is to emit a null row when there are no valid
  // / rows in the output.
  private IAEval            nullOutputEval;

  /** Empty Group Evaluator */
  // / emptyGroupEval checks if the group has become empty
  private IBEval            emptyGroupEval;
  
  /** Release handler eval */
  private IAEval            releaseHandlerEval;
  
  /** reset handler eval */
  private IAEval            resetHandlerEval;
  
  /** alloc handler eval */
  private IAEval            allocHandlerEval;
  
  /** allocator for index tuple for order by */
  private IAllocator<ITuplePtr>        orderByAllocator;
  
  //the following three evals mimic the UDA pattern for XMLAGG
  /** allocator for xmlagg index */
  private IAEval            allocIndexEval;
  
  /** reset xmlagg index */
  private IAEval            resetIndexEval;
  
  /** release handler eval for xmlagg index */
  private IAEval            releaseIndexEval;

  /** roles for evaluation contex */
  private static final int INPUT_ROLE      = IEvalContext.INPUT_ROLE;

  private static final int OLD_OUTPUT_ROLE = IEvalContext.OLD_OUTPUT_ROLE;

  private static final int NEW_OUTPUT_ROLE = IEvalContext.NEW_OUTPUT_ROLE;

  private static final int XML_AGG_INDEX_ROLE = IEvalContext.XML_AGG_INDEX_ROLE;
  
  /** number of xmlagg aggrs */
  private int              numXmlAgg;
  
  /** number of user defined aggregations (UDAs) */
  private int              numUDA;

  /** number of UDA that require a full scan while processing a minus */
  private int              numFullUDA;

  /** Is the output of this instantaneous relation always one tuple */
  private boolean          oneGroup;

  /** Current number of elements populated in the udaInfo array */
  private int              currUDACount = 0;
  
  /** number of group by attributes or expressions*/
  private int              numGroupByAttrs;

  /** flag whether we need to emit empty(null) tuple or not.
   * The null output event is propagated once group is empty.*/
  private boolean          pendingEmptyGroup = false;
  
  private boolean oldDataPropNeeded = true;
  
  /**
   * Constructor for GroupAggr
   * @param ec TODO
   */
  public GroupAggr(ExecContext ec)
  {
    super(ExecOptType.EXEC_GROUP_AGGR, new GroupAggrState(ec), ec);
  }

  /**
   * Getter for output synopsis in GroupAggr
   * 
   * @return Returns the output synopsis
   */
  public RelationSynopsis getOutSynopsis()
  {
    return outSyn;
  }

  /**
   * Setter for output synopsis in GroupAggr
   * 
   * @param outSyn
   *          The output Synopsis to set.
   */
  public void setOutSynopsis(RelationSynopsis outSyn)
  {
    this.outSyn = outSyn;
  }

  /**
   * Setter for output synopsis scan id
   * 
   * @param scanId
   *          the id of the scan on the output synopsis
   */
  public void setOutScanId(int scanId)
  {
    this.outScanId = scanId;
  }

  /**
   * @return the resetIndexEval
   */
  public IAEval getResetIndexEval()
  {
    return resetIndexEval;
  }

  /**
   * @param resetIndexEval the resetIndexEval to set
   */
  public void setResetIndexEval(IAEval resetIndexEval)
  {
    this.resetIndexEval = resetIndexEval;
  }

  /**
   * @return the number of XmlAgg
   */
  public int getNumXmlAgg()
  {
    return numXmlAgg;
  }

  /**
   * @param numXmlAgg the number of XmlAgg to set
   */
  public void setNumXmlAgg(int numXmlAgg)
  {
    this.numXmlAgg = numXmlAgg;
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
   * Getter for input synopsis in GroupAggr
   * 
   * @return Returns the input synopsis
   */
  public RelationSynopsis getInSynopsis()
  {
    return inSyn;
  }

  /**
   * Setter for input synopsis in GroupAggr
   * 
   * @param inSyn
   *          The input Synopsis to set.
   */
  public void setInSynopsis(RelationSynopsis inSyn)
  {
    this.inSyn = inSyn;
  }

  /**
   * Setter for input synopsis scan id
   * 
   * @param scanId
   *          the id of the scan on the input synopsis
   */
  public void setInScanId(int scanId)
  {
    this.inScanId = scanId;
  }
  
  public void setInFullScanId(int inFullScanId)
  {
    this.inFullScanId = inFullScanId;
  }

  /**
   * Getter for evalContext in GroupAggr
   * 
   * @return Returns the evalContext
   */
  public IEvalContext getEvalContext()
  {
    return evalContext;
  }

  /**
   * Setter for dirty Synopsis in GroupAggr
   * 
   * @param dirtySyn
   *          The dirty synopsis is set
   */
  public void setDirtySyn(RelationSynopsis dirtySyn)
  {
    this.dirtySyn = dirtySyn;
  }
  
  /**
   * Setter for dirtyScanId in GroupAggr
   * 
   * @param dirtyScanId
   *          The dirty ScanId is set
   */
  public void setDirtyScanId(int dirtyScanId)
  {
    this.dirtyScanId = dirtyScanId;
  }
  
  /**
   * Setter for dirtyFullScanId in GroupAggr
   * 
   * @param dirtyFullScanId
   *          The dirty fullScanId is set
   */
  public void setDirtyFullScanId(int dirtyFullScanId)
  {
    this.dirtyFullScanId = dirtyFullScanId;
  }
  
  /**
   * Setter for evalContext in GroupAggr
   * 
   * @param evalContext
   *          The evalContext to set.
   */
  public void setEvalContext(IEvalContext evalContext)
  {
    this.evalContext = evalContext;
  }

  /**
   * Getter for initEval in GroupAggr
   * 
   * @return Returns the initEval
   */
  public IAEval getInitEval()
  {
    return initEval;
  }

  /**
   * Setter for initEval in GroupAggr
   * 
   * @param initEval
   *          The initEval to set.
   */
  public void setInitEval(IAEval initEval)
  {
    this.initEval = initEval;
  }

  /**
   * @return the allocIndexEval
   */
  public IAEval getAllocIndexEval()
  {
    return allocIndexEval;
  }

  /**
   * @param allocIndexEval the allocIndexEval to set
   */
  public void setAllocIndexEval(IAEval allocIndexEval)
  {
    this.allocIndexEval = allocIndexEval;
  }

  /**
   * @return the releaseIndexEval
   */
  public IAEval getReleaseIndexEval()
  {
    return releaseIndexEval;
  }

  /**
   * @param releaseIndexEval the releaseIndexEval to set
   */
  public void setReleaseIndexEval(IAEval releaseIndexEval)
  {
    this.releaseIndexEval = releaseIndexEval;
  }

  /**
   * Getter for plusEval in GroupAggr
   * 
   * @return Returns the plusEval
   */
  public IAEval getPlusEval()
  {
    return plusEval;
  }

  /**
   * Gets the orderByAllocator, maybe null
   * @return void
   */
  public IAllocator<ITuplePtr> getOrderByAllocator()
  {
    return orderByAllocator;
  }

  /**
   * Setter for order by allocator. Called by GroupAggrFactory
   * @param orderByAllocator
   */
  public void setOrderByAllocator(IAllocator<ITuplePtr> orderByAllocator)
  {
    this.orderByAllocator = orderByAllocator;
  }

  /**
   * Setter for plusEval in GroupAggr
   * 
   * @param plusEval
   *          The plusEval to set.
   */
  public void setPlusEval(IAEval plusEval)
  {
    this.plusEval = plusEval;
  }

  /**
   * Getter for updateEval in GroupAggr
   * 
   * @return Returns the updateEval
   */
  public IAEval getUpdateEval()
  {
    return updateEval;
  }

  /**
   * Setter for updateEval in GroupAggr
   * 
   * @param updateEval
   *          The updateEval to set.
   */
  public void setUpdateEval(IAEval updateEval)
  {
    this.updateEval = updateEval;
  }

  /**
   * Getter for minusEval in GroupAggr
   * 
   * @return Returns the minusEval
   */
  public IAEval getMinusEval()
  {
    return minusEval;
  }

  /**
   * Setter for minusEval in GroupAggr
   * 
   * @param minusEval
   *          The minusEval to set.
   */
  public void setMinusEval(IAEval minusEval)
  {
    this.minusEval = minusEval;
  }

  /**
   * @param nullOutputEval
   *          The nullOutputEval to set.
   */
  public void setNullOutputEval(IAEval nullOutputEval)
  {
    this.nullOutputEval = nullOutputEval;
  }

  /**
   * Getter for arithScanNotReqEval in GroupAggr
   * 
   * @return Returns the arithScanNOtReqEval
   */
  public IAEval getArithScanNotReqEval()
  {
    return arithScanNotReqEval;
  }

  /**
   * Setter for arithScanNotReqEval in GroupAggr
   * 
   * @param arithScanNotReqEval
   *          The arithScanNotReqEval to set.
   */
  public void setArithScanNotReqEval(IAEval arithScanNotReqEval)
  {
    this.arithScanNotReqEval = arithScanNotReqEval;
  }

  /**
   * Getter for scanNotReqEval in GroupAggr
   * 
   * @return Returns the scanNotReqEval
   */
  public IBEval getScanNotReqEval()
  {
    return scanNotReqEval;
  }

  /**
   * Setter for scanNotReqEval in GroupAggr
   * 
   * @param scanNotReqEval
   *          The scanNotReqEval to set.
   */
  public void setScanNotReqEval(IBEval scanNotReqEval)
  {
    this.scanNotReqEval = scanNotReqEval;
  }

  /**
   * Getter for emptyGroupEval in GroupAggr
   * 
   * @return Returns the emptyGroupEval
   */
  public IBEval getEmptyGroupEval()
  {
    return emptyGroupEval;
  }

  /**
   * Setter for emptyGroupEval in GroupAggr
   * 
   * @param emptyGroupEval
   *          The emptyGroupEval to set.
   */
  public void setEmptyGroupEval(IBEval emptyGroupEval)
  {
    this.emptyGroupEval = emptyGroupEval;
  }

  /**
   * @return the releaseHandlerEval
   */
  public IAEval getReleaseHandlerEval()
  {
    return releaseHandlerEval;
  }

  /**
   * @param releaseHandlerEval the releaseHandlerEval to set
   */
  public void setReleaseHandlerEval(IAEval releaseHandlerEval)
  {
    this.releaseHandlerEval = releaseHandlerEval;
  }

  /**
   * @return the resetHandlerEval
   */
  public IAEval getResetHandlerEval()
  {
    return resetHandlerEval;
  }

  /**
   * @param resetHandlerEval the resetHandlerEval to set
   */
  public void setResetHandlerEval(IAEval resetHandlerEval)
  {
    this.resetHandlerEval = resetHandlerEval;
  }

  /**
   * @param allocHandlerEval the allocHandlerEval to set
   */
  public void setAllocHandlerEval(IAEval allocHandlerEval)
  {
    this.allocHandlerEval = allocHandlerEval;
  }

  /**
   * Getter for number of UDAs in GroupAggr
   * 
   * @return number of UDAs
   */
  public int getNumUDA()
  {
    return numUDA;
  }

  /**
   * Setter for number of UDAs in GroupAggr
   * 
   * @param numUDA
   *          number of UDAs
   */
  public void setNumUDA(int numUDA)
  {
    this.numUDA = numUDA;
  }

  /**
   * Getter for number of UDAs that require a full scan while processing a minus
   * 
   * @return number of UDAs that require a full scan
   */
  public int getNumFullUDA()
  {
    return numFullUDA;
  }

  /**
   * Setter for number of UDAs that require a full scan while processing a minus
   * 
   * @param numFullUDA
   *          number of UDAs that require a full scan
   */
  public void setNumFullUDA(int numFullUDA)
  {
    this.numFullUDA = numFullUDA;
  }

  /**
   * Get whether the output of this instantaneous relation is always one tuple.
   * <p>
   * This happens when there is no GROUP BY clause
   * 
   * @return true iff the output of this instantaneous relation is always one
   *         tuple.
   */
  public boolean isOneGroup()
  {
    return oneGroup;
  }

  /**
   * Set whether the output of this instantaneous relation is always one tuple.
   * <p>
   * This happens when there is no GROUP BY clause
   * 
   * @param oneGroup
   *          true iff the output of this instantaneous relation is always one
   *          tuple.
   */
  public void setOneGroup(boolean oneGroup)
  {
    this.oneGroup = oneGroup;
  }
  
  public void setNumGroupByAttrs(int numGroupByAttrs)
  {
    this.numGroupByAttrs = numGroupByAttrs;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.operators.ExecOpt#run(int)
   */
  @Override
  public int run(int timeSlice) throws ExecException
  {
    int            numElements;
    boolean        done = false;
    GroupAggrState s = (GroupAggrState) mut_state;
    boolean exitState = true;
    
    assert s.state != ExecState.S_UNINIT;

    // Stats
    s.stats.incrNumExecutions();

    try
    {
      // Number of input elements to process
      numElements = timeSlice;
      while ((s.stats.getNumInputs() < numElements) || (!exitState))
      {
        switch (s.state)
        {

          case S_PROPAGATE_OLD_DATA:
            if(oldDataPropNeeded)
            {
              setExecSynopsis((ExecSynopsis) outSyn);
              handlePropOldData();
            }
            else
            {
              oldDataPropNeeded = true;
              s.state = s.lastState;
            }
            break;

          case S_INIT:
            
            // Get next input element
            s.inputElement = inputQueue.dequeue(s.inputElementBuf);
            if (s.inputElement != null)
              s.state = ExecState.S_INPUT_DEQUEUED;
            else
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
          case S_INPUT_DEQUEUED:
            // Update our counts
            exitState = false;
            if(s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
              s.stats.incrNumInputHeartbeats();
            else
              s.stats.incrNumInputs();

            // Get the timestamp
            s.inputTs = s.inputElement.getTs();
            // We should have a progress of time.
            if (s.lastInputTs > s.inputTs)
            {
                throw ExecException.OutOfOrderException(
                        this,
                        s.lastInputTs, 
                        s.inputTs, 
                        s.inputElement.toString());
            }
            
            // Initialize isTotalOrderingGuarantee flag            
            s.isTotalOrderingGuarantee 
              = s.inputElement.getTotalOrderingGuarantee();

            // current input timestamp should be equal or higher than timestamp
            // we calculated as next expected timestamp in previous execution
            assert s.inputTs >= s.minNextTs :
              getDebugInfo(s.inputTs, s.minNextTs, 
                s.inputElement.getKind().name(), s.lastInputKind.name());
            
            // set the expected timestamp of next input tuple
            s.minNextTs = s.isTotalOrderingGuarantee ? s.inputTs + 1:
                                                       s.inputTs;

            // update the lastTotalOrderingGuarantee flag now
            s.lastTotalOrderingGuarantee = s.isTotalOrderingGuarantee;
            
            
            // Output All Dirty Tuples belonging to previous timestamp
            // if either of following holds true:
            // 1) current tuple timestamp is more than previous input timestamp
            // OR
            // 2) current input tuple guarantees that next input tuple will
            //    be of higher timestamp.
           
            // After outputting all dirty tuples.. proceed to either  
            // S_PROCESS_PLUS or S_PROCESS_MINUS or heartbeat
            
            if ( (s.inputTs > s.lastInputTs) ||
                 (s.isTotalOrderingGuarantee))
            {
              s.oldState = s.state;
              outputDirtyTuples(s);
            }
            
            // Update the last input Ts now
            s.lastInputTs = s.inputTs;
            s.inputTuple = s.inputElement.getTuple();
            s.lastInputKind = s.inputElement.getKind();
            
            if (s.inputElement.getKind() == QueueElement.Kind.E_PLUS)
            {
              s.state = ExecState.S_PROCESS_PLUS;
              break;
            }
            else if (s.inputElement.getKind() == QueueElement.Kind.E_MINUS)
            {
              s.state = ExecState.S_PROCESS_MINUS;
              break;
            }
            else
            {
              // heartbeats - nothing to do
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;
              break;
            }

          case S_PROCESS_PLUS:
            s.oldState = s.state;
            handlePlus(s);
            break;
          case S_PROCESS_MINUS:
            s.oldState = s.state;
            handleMinus(s);
            break;
          case S_OUTPUT_TUPLE:
            s.outputTuple =tupleStorageAlloc.allocate();
            s.state = s.oldState;
            break;
          case S_OUTPUT_ELEMENT:
            s.state = ExecState.S_OUTPUT_TIMESTAMP;
          case S_OUTPUT_TIMESTAMP:
            s.state = s.oldState;
            break;
          case S_OUTPUT_READY:
            if (outputQueue.isFull())
            {
              done = true;
              break;
            }
            s.lastOutputTs = s.outputElement.getTs();
            outputQueue.enqueue(s.outputElement);
            
            if(s.outputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
              s.stats.incrNumOutputHeartbeats();
            else
              s.stats.incrNumOutputs();
            
            s.state = s.oldState;
            break;
          // Generate Heartbeat
          // Condition: if input queue is empty and lastInputTs > lastOutputTs
          case S_GENERATE_HEARTBEAT:
            //assert: input queue should be empty
            assert s.inputElement == null;
            // if init null row not emitted yet AND no groupby attrs
            //   Proceed to allocate output tuple and do nullOutputEval
            //   Transmit heartbeat
            // else
            //   Transmit heartbeat
            if(!s.isInitNullRowEmitted && nullOutputEval != null && numGroupByAttrs == 0)
            {
              s.oldState = ExecState.S_PROCESSING1;
              s.state    = ExecState.S_OUTPUT_TUPLE;
              // set the flag indicating that the null row was emitted at
              // t = Constant.MIN_EXEC_TIME
              // Without this, if the initial tuple will emit null row after
              // hearbeats and cause out of order time stamp
              s.isInitNullRowEmitted = true;
              // Action: Set the next tuple total order flag = false;
              // Reason: This state is reached when there is not current input
              //         tuples; and operator scheduled to propagate pending 
              //         heartbeats; Before transmit heartbeat; we want to 
              //         transmit any remaining Null tuple.
              //         There may be future inputs with the same timestamp 
              s.nextTupleTotalOrderingGuarantee = false;
            }
            else
              s.state = ExecState.S_PROCESSING4;
            break;
              
          case S_PROCESSING1:
            evalContext.bind(s.outputTuple, OLD_OUTPUT_ROLE);
            // Bind outputtuple to NEW_OUTPUT_ROLE and evaluate nullOutputEval
            evalContext.bind(s.outputTuple, NEW_OUTPUT_ROLE);
            if(numUDA > 0)
            {
              allocHandlerEval.eval(evalContext);
            }
            if(numXmlAgg > 0)
            {
              allocIndexEval.eval(evalContext);
            }        
            
            nullOutputEval.eval(evalContext);
            s.state = ExecState.S_PROCESSING2;
          case S_PROCESSING2:
            // Insert outputTuple to output synopsis
            outSyn.insertTuple(s.outputTuple);
            s.state = ExecState.S_PROCESSING3;
          case S_PROCESSING3:
            // set outputElement
            s.outputTs = Constants.MIN_EXEC_TIME;
            s.outputElement.setKind(QueueElement.Kind.E_PLUS);
            s.outputElement.setTs(s.outputTs);
            s.outputElement.setTuple(s.outputTuple);
            s.outputElement.setTotalOrderingGuarantee
              (s.nextTupleTotalOrderingGuarantee);
            // oldState = S_PROCESSING3 will tell that you have ONE pending
            // heartbeat generation
            s.oldState = ExecState.S_PROCESSING3; 
            s.state = ExecState.S_PROCESSING5;
            break;
          case S_PROCESSING4:
            // Generate Heartbeat Element 
            s.lastOutputTs = s.lastInputTs;
            s.outputElement.heartBeat(s.lastInputTs);
            //s.nextTupleTotalOrderingGuarantee = s.lastTotalOrderingGuarantee
            s.nextTupleTotalOrderingGuarantee = (s.lastInputTs < s.minNextTs)
                                                && (s.dirtyTupleCount == 0);
            // In case of heartbeat output; total ordering guarantee flag will:
            //  true : if last input tuple recieved had the flag = true;
            //         and there is not pending dirty tuples.
            //  false : if last input tuple recieved had the flag = false;
            //          it means that there may be more input tuples with same
            //          timestamp as previous
            s.outputElement.setTotalOrderingGuarantee
              (s.nextTupleTotalOrderingGuarantee);
            // oldState = S_PROCESSING4 will tell that you have NO pending
            // heartbeat generation
            s.oldState = ExecState.S_PROCESSING4; 
            s.state = ExecState.S_PROCESSING5;
          case S_PROCESSING5:
            // Insert outputElement into outputQueue
            if (outputQueue.isFull())
            {
              done = true;
              break;
            }
            outputQueue.enqueue(s.outputElement);
            if(s.outputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
              s.stats.incrNumOutputHeartbeats();
            else
              s.stats.incrNumOutputs();
            // Check if heartbeat pending ?
            // (s.oldState == S_PROCESSING3) => heartbeat pending
            // (s.oldState == S_PROCESSING4) => heartbeat not pending              
            if(s.oldState == ExecState.S_PROCESSING4)
              s.state = ExecState.S_INIT;
            else if(s.oldState == ExecState.S_PROCESSING3)
              s.state = ExecState.S_PROCESSING4;
            else
              assert false;
            break;
         case S_INPUT_ELEM_CONSUMED:
           assert s.inputElement != null;

           if (s.inputTuple != null)
           {
             inTupleStorageAlloc.release(s.inputTuple);
           }

           exitState = true;
           s.state = ExecState.S_INIT;
           break;

         default:
            assert false;
        }
        if (done){
        s.stats.setEndTime(System.nanoTime());
          break;
        }
      }
    }
    catch (SoftExecException e1)
    {
      e1.printStackTrace();
      // TODO Ignore them
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e1);
      return 0;
    }   
    
    return 0;
  }

  /**
   * This will output all dirty tuples of output Queue
   * @param s
   * @throws ExecException
   */
  private void outputDirtyTuples(GroupAggrState s) throws ExecException
  {
    boolean done = false;
    
    // No need to execute in while loop if dirty synopsis is empty
    if(s.dirtyTupleCount == 0)
      done = true;
    while(!done)
    {
      switch(s.dirtyOutputState)
      {
      case S_INIT:
        s.outDirtyTupleIter = dirtySyn.getScan(dirtyFullScanId);
        s.outDirtyTuple  = s.outDirtyTupleIter.getNext();       
               
        if(s.outDirtyTuple != null)
        {
          s.nextOutDirtyTuple = s.outDirtyTupleIter.getNext();
          s.dirtyOutputState = GroupAggrState.DirtyOutputState.S_OUTPUT_ELEMENT;          
        }
        else
          s.dirtyOutputState = GroupAggrState.DirtyOutputState.S_FINISHED;
                 
        break;      
        
      case S_RESCAN_INPUT_SYN:
        s.outDirtyTuple = s.nextOutDirtyTuple;
        if(s.outDirtyTuple != null)
          s.dirtyOutputState = GroupAggrState.DirtyOutputState.S_OUTPUT_ELEMENT;
        else
        {
          s.dirtyOutputState = GroupAggrState.DirtyOutputState.S_FINISHED;
          break;
        }
        s.nextOutDirtyTuple = s.outDirtyTupleIter.getNext();
      case S_OUTPUT_ELEMENT:
        s.dirtyOutputState = GroupAggrState.DirtyOutputState.S_OUTPUT_TIMESTAMP;
      case S_OUTPUT_TIMESTAMP:
        s.dirtyOutputState = GroupAggrState.DirtyOutputState.S_ENQUEUE_PLUS;
      case S_ENQUEUE_PLUS:
        s.outputTs = s.lastInputTs;
        // Set next tuple's total ordering flag
        // Conditions:
        // 1) If timestamp has been changed then
        //    set flag to TRUE iff it is last tuple in the dirty synopsis
        // 2) If timestamp was not changed and flushing is due to ordering flag
        //    then set flag to FALSE as there may be some output tuples for
        //    current input timestamp only if tuple is not a heartbeat

        if(s.inputTs > s.lastInputTs)
        {
          if(s.nextOutDirtyTuple == null)
            s.nextTupleTotalOrderingGuarantee = true;
          else
            s.nextTupleTotalOrderingGuarantee = false;
        }
        else if(s.isTotalOrderingGuarantee)
        {
          if(s.nextOutDirtyTuple == null &&
             s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
            s.nextTupleTotalOrderingGuarantee = true;
          else
            s.nextTupleTotalOrderingGuarantee = false;
        }
       
        s.outputElement.setKind(QueueElement.Kind.E_PLUS);
        s.outputElement.setTotalOrderingGuarantee(
            s.nextTupleTotalOrderingGuarantee);
        s.outputElement.setTs(s.outputTs);
        s.outputElement.setTuple(s.outDirtyTuple);
        s.dirtyOutputState = GroupAggrState.DirtyOutputState.S_OUTPUT_READY;
      case S_OUTPUT_READY:
        if (outputQueue.isFull())
        {
          done = true;
          break;
        }
        s.lastOutputTs = s.outputElement.getTs();
        outputQueue.enqueue( s.outputElement);
        if(s.outputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
          s.stats.incrNumOutputHeartbeats();
        else
          s.stats.incrNumOutputs();
        // Delete this tuple from dirtySyn because now it is no longer dirty
        dirtySyn.deleteTuple(s.outDirtyTuple);
        s.dirtyTupleCount--;
        s.dirtyOutputState = GroupAggrState.DirtyOutputState.S_RESCAN_INPUT_SYN;
        break;
      
      case S_FINISHED:
        dirtySyn.releaseScan(this.dirtyFullScanId, s.outDirtyTupleIter);
        done = true;
        s.dirtyOutputState = GroupAggrState.DirtyOutputState.S_INIT;
        break;
      } //end of switch statement
    } //end of while loop
    
    // this function will empty dirty synopsis
    assert s.dirtyTupleCount == 0;
  }
  
  private void handlePlus(GroupAggrState s) throws ExecException
  {
    boolean done        = false;
    boolean groupExists = false;
    
    while (!done)
    {
      switch (s.plusState)
      {
        case S_INIT:
          // Bind the input tuple
          evalContext.bind(s.inputTuple, INPUT_ROLE);
          if(orderByAllocator != null)
            s.plusState = GroupAggrState.PlusState.S_ORDER_BY_TUPLE;
          else
            s.plusState = GroupAggrState.PlusState.S_GET_OUTPUT_SCAN;
          break;
        case S_ORDER_BY_TUPLE:
          //Bind the order by tuple
          s.orderByTuple = orderByAllocator.allocate();
          evalContext.bind(s.orderByTuple, XML_AGG_INDEX_ROLE);
          s.plusState = GroupAggrState.PlusState.S_GET_OUTPUT_SCAN;
          
        case S_GET_OUTPUT_SCAN:
          s.outTupleIter = outSyn.getScan(outScanId);
          s.oldAggrTuple = s.outTupleIter.getNext();
          groupExists = s.oldAggrTuple != null;
          s.plusState = GroupAggrState.PlusState.S_RELEASE_OUTPUT_SCAN;
          
        case S_RELEASE_OUTPUT_SCAN:
          outSyn.releaseScan(outScanId, s.outTupleIter);
          // Note: We will create new aggregation tuple only when group
          // gets created first time. OR
          // Group is not dirty for present time stamp
          if (groupExists)
            s.plusState = GroupAggrState.PlusState.S_CHECK_IS_DIRTY;
          else
          {
            s.state     = ExecState.S_OUTPUT_TUPLE;
            done        = true;
            s.plusState = GroupAggrState.PlusState.S_NEW_GROUP;
          }
          break;
          
        case S_CHECK_IS_DIRTY:
          s.dirtyTupleIter = dirtySyn.getScan(dirtyScanId);
          s.dirtyTuple     = s.dirtyTupleIter.getNext();
          s.isDirty          = (s.dirtyTuple != null);
          dirtySyn.releaseScan(dirtyScanId, s.dirtyTupleIter);
          s.plusState = GroupAggrState.PlusState.S_GROUP_EXISTS;
          // Allocate a new output tuple if Group is not dirty
          if(!s.isDirty)
          {
            s.state     = ExecState.S_OUTPUT_TUPLE;
            done        = true;
          }
          break;

        case S_GROUP_EXISTS:
          // Plus evaluator does the job of computing the new aggregation
          // tuple from the new input tuple and the old aggregation tuple.
          if(!s.isDirty)
            evalContext.bind(s.outputTuple, NEW_OUTPUT_ROLE);
          else
            evalContext.bind(s.oldAggrTuple, NEW_OUTPUT_ROLE);
          evalContext.bind(s.oldAggrTuple, OLD_OUTPUT_ROLE);
          plusEval.eval(evalContext);
          s.plusState = GroupAggrState.PlusState.S_CHECK_IS_DIRTY_1;
          
        case S_CHECK_IS_DIRTY_1:
          // If Group is Dirty then 
          //   current dirtyTuple will have recalculated Aggregate value
          //   Goto S_PROCESSING_1
          // else if isTotalOrderGuaranteed = true
          //   then Insert outputTuple into outSyn;
          //   Don't insert tuple in dirtysyn
          //   Goto S_INSERT_INTO_OUT_SYNOPSIS_1
          // else
          //   insert new outputTuple in dirtySyn
          //   Insert new outputTuple into outSyn
          //   Goto S_INSERT_INTO_OUT_SYNOPSIS_1
          if(s.isDirty)
          {
            s.plusState = GroupAggrState.PlusState.S_PROCESSING_1;
          }
          else if(s.isTotalOrderingGuarantee)
          {
            s.plusState = GroupAggrState.PlusState.S_INSERT_INTO_OUT_SYNOPSIS_1; 
          }
          else
          {
            dirtySyn.insertTuple(s.outputTuple);
            s.dirtyTupleCount++;
            s.plusState = GroupAggrState.PlusState.S_INSERT_INTO_OUT_SYNOPSIS_1;   
          }
          break;
        case S_INSERT_INTO_OUT_SYNOPSIS_1:
          outSyn.insertTuple(s.outputTuple);
          s.plusState = GroupAggrState.PlusState.S_DELETE_FROM_OUT_SYNOPSIS;
        case S_DELETE_FROM_OUT_SYNOPSIS:
          outSyn.deleteTuple(s.oldAggrTuple);
          s.plusState = GroupAggrState.PlusState.S_PROCESSING_1;
        case S_PROCESSING_1:
          // If output Tuple is dirty then don't generate minus element.
          if((numUDA > 0 || numXmlAgg > 0) && s.isDirty)
            s.plusState = GroupAggrState.PlusState.S_UPDATE_INPUT_SYN;
          else if((numUDA > 0 || numXmlAgg > 0) && !s.isDirty)
            s.plusState = GroupAggrState.PlusState.S_RESET_AGGR_HANDLERS;
          else if(s.isDirty)
            s.plusState = GroupAggrState.PlusState.S_UPDATE_INPUT_SYN;
          else
            s.plusState = GroupAggrState.PlusState.S_PREPARE_MINUS_ELEMENT;
          break;
        case S_RESET_AGGR_HANDLERS:
          evalContext.bind(s.oldAggrTuple, IEvalContext.AGGR_ROLE);
          if(numUDA > 0)
            resetHandlerEval.eval(evalContext);
          if(numXmlAgg > 0)
            resetIndexEval.eval(evalContext);
          s.plusState = GroupAggrState.PlusState.S_PREPARE_MINUS_ELEMENT;
        case S_PREPARE_MINUS_ELEMENT:
          // Action: we are setting totalOrderingFlag = false
          // Reason: As this state is achieved on recieving a PLUS input tuple
          //         and group is not dirty yet; There are 2 cases:
          //  1) If input tuple's orderingFlag = T
          //       then MINUS of previous aggr tuple with current timestamp
          //       will be sent out; follow by PLUS aggr tuple of current
          //       timestamp value; so
          //       it means that MINUS tuple's totalOrderingFlag will be false
          //  2) If input tuple's orderingFlag = F
          //       then negative of previous aggr tuple with current timestamp
          //       will be sent out;
          //       instead of sending output of current timestamp; output will
          //       be inserted into dirtySyn.
          //       In this case; If next input tuple come with higher timestamp
          //        or same timestamp withh tuple ordering flag= T;
          //       then we will emit out all the dirty tuples with current timestamp;
          //       hence we cannt send negative with orderingFlag = true;
          //       If next input tuple come with same timestamp with flag= F;  
          //      then we will save it in dirty synopsis and whenever we will
          //      output these dirty tuples; output timestamp will be current 
          //      timestamp(timestamp of tuples in respect of which above cases
          //      is written)
             
          s.nextTupleTotalOrderingGuarantee = false;
          s.state = ExecState.S_OUTPUT_ELEMENT;
          done = true;
          s.plusState = GroupAggrState.PlusState.S_ENQUEUE_MINUS;
          break;
        case S_ENQUEUE_MINUS:
          s.outputTs = s.inputTs;
          s.outputElement.setKind(QueueElement.Kind.E_MINUS);
          s.outputElement.setTs(s.outputTs);
          s.outputElement.setTuple(s.oldAggrTuple);
          s.outputElement.setTotalOrderingGuarantee(
              s.nextTupleTotalOrderingGuarantee);
          s.state = ExecState.S_OUTPUT_READY;
          done = true;          
          s.plusState 
            = GroupAggrState.PlusState.S_PROCESS_IS_TOTAL_ORDERING_FLAG_1;
          break;
        case S_PROCESS_IS_TOTAL_ORDERING_FLAG_1:
          // Two Cases:
          // Case 1: If current input tuple ensures that next tuple will have
          //         higher timestamp value; then
          //         we will never insert this output tuple into dirty synopsis
          //         and will send it in output queue
          // Case 2: If current input tuple doesn't ensure that next tuple will
          //         have higher timestamp value; then
          //         We will store it in dirty synopsis and not send output now
          if(s.isTotalOrderingGuarantee)
          {
            s.nextTupleTotalOrderingGuarantee = true;
            s.plusState = GroupAggrState.PlusState.S_PREPARE_PLUS_ELEMENT_2;
          }
          else
            s.plusState = GroupAggrState.PlusState.S_UPDATE_INPUT_SYN;
          break;
          //TODO: Anand please review
        case S_NEW_GROUP:
          evalContext.bind(s.outputTuple, NEW_OUTPUT_ROLE);
          if (numUDA > 0 || numXmlAgg > 0)
            s.plusState = GroupAggrState.PlusState.S_ALLOC_AGGR_HANDLERS;
          else
            s.plusState = GroupAggrState.PlusState.S_INIT_NEW_GROUP;
          break;
        case S_ALLOC_AGGR_HANDLERS:
          evalContext.bind(s.outputTuple, OLD_OUTPUT_ROLE);
          if(numUDA > 0)
            allocHandlerEval.eval(evalContext);
          if(numXmlAgg > 0)
            allocIndexEval.eval(evalContext);
          s.plusState = GroupAggrState.PlusState.S_INIT_NEW_GROUP;
        case S_INIT_NEW_GROUP:
          // Emit a null row at t=0,
          // when no group by attrs to reflect the null relation
          // if nullOutputEval is executed, plusEval takes care of the
          // processing of the first tuple
          
          if (!s.isInitNullRowEmitted && nullOutputEval != null && numGroupByAttrs == 0)
          {
            nullOutputEval.eval(evalContext);
          }
          else if(!s.isTotalOrderingGuarantee)
          { 
            // if next input tuple is not guaranteed to have bigger timestamp
            // then insert current output tuple into DirtySyn            
            initEval.eval(evalContext);
            dirtySyn.insertTuple(s.outputTuple);
            s.dirtyTupleCount++;
          }
          else
            initEval.eval(evalContext);          
          s.plusState = GroupAggrState.PlusState.S_INSERT_INTO_OUT_SYNOPSIS_2;
        case S_INSERT_INTO_OUT_SYNOPSIS_2:
          outSyn.insertTuple(s.outputTuple);
          // 3 Cases:
          // case-1: If initial null row is not yet emitted
          //          then output null row
          // case-2: If next tuple will have higher timestamp; 
          //          then current output tuple will not be stored in dirtySyn
          //               and will be emitted now
          // case-3: If next tuple is not gurantedly higher timestamp;
          //           then current output tuple is stored in dirtySyn and
          //                here we will update input synopsis only.
          if (!s.isInitNullRowEmitted && nullOutputEval != null && numGroupByAttrs == 0)
          {
            s.nextTupleTotalOrderingGuarantee = false;
            s.plusState = GroupAggrState.PlusState.S_PREPARE_PLUS_ELEMENT_2;
          }
          else if(s.isTotalOrderingGuarantee)
          {
            // If we are sure that next input tuple is of higher timetamp value
            // then we can make sure that this is the last output tuple for 
            // current timestamp value
            s.nextTupleTotalOrderingGuarantee = true;
            s.plusState = GroupAggrState.PlusState.S_PREPARE_PLUS_ELEMENT_2;
          }
          else
            s.plusState = GroupAggrState.PlusState.S_UPDATE_INPUT_SYN;
          break;
        case S_PREPARE_PLUS_ELEMENT_2:
          s.state = ExecState.S_OUTPUT_ELEMENT;
          done = true;
          s.plusState = GroupAggrState.PlusState.S_ENQUEUE_PLUS_2;
          break;
        case S_ENQUEUE_PLUS_2:
          // Emit the null row at t = Constants.MIN_EXEC_TIME
          if (!s.isInitNullRowEmitted && nullOutputEval != null && numGroupByAttrs == 0)
            s.outputTs = Constants.MIN_EXEC_TIME;
          else
            s.outputTs = s.inputTs;
          s.outputElement.setKind(QueueElement.Kind.E_PLUS);
          s.outputElement.setTs(s.outputTs);
          s.outputElement.setTuple(s.outputTuple);
          s.outputElement.setTotalOrderingGuarantee
            (s.nextTupleTotalOrderingGuarantee);
          s.state = ExecState.S_OUTPUT_READY;
          done = true;

          // do not update the input synopsis while emitting the null row at 
          // t=0
          if (!s.isInitNullRowEmitted && nullOutputEval != null && numGroupByAttrs == 0)
            s.plusState = GroupAggrState.PlusState.S_FINISHED;
          else
            s.plusState = GroupAggrState.PlusState.S_UPDATE_INPUT_SYN;
          break;

        case S_UPDATE_INPUT_SYN:
          if (inSyn != null)
            inSyn.insertTuple(s.inputTuple);
        case S_FINISHED:
          s.plusState = GroupAggrState.PlusState.S_INIT;
          // Process the input tuple whose processing was halted until the
          // emission of null row at t= Constants.MIN_EXEC_TIME
          if (!s.isInitNullRowEmitted && nullOutputEval != null && numGroupByAttrs == 0)
            s.state = ExecState.S_PROCESS_PLUS;
          else
            s.state = ExecState.S_INPUT_ELEM_CONSUMED;
          // set the flag indicating that the null row was emited at 
          // t= Costant.MIN_EXEC_TIME
          s.isInitNullRowEmitted = true;
          done = true;
          break;
        default:
          assert false;
      }
    }
  }

  private void handleMinus(GroupAggrState s) throws ExecException
  {
    boolean done        = false;
    boolean groupExists = false;
    ITuplePtr inTuple   = null;

    while (!done)
    {
      switch (s.minusState)
      {
        case S_INIT:
          // Bind the input tuple
          s.isGroupEmpty = false;
          evalContext.bind(s.inputTuple, INPUT_ROLE);
          s.minusState = GroupAggrState.MinusState.S_UPDATE_INPUT_SYN;
        case S_UPDATE_INPUT_SYN:
          // Maintain the input synopsis if it exists. This should be done
          // before the rest of the processing since later code assumes that
          // the input synopsis is up-to-date for its correctness.
          if (inSyn != null)
          {
            inSyn.deleteTuple(s.inputTuple);
          }
          s.minusState = GroupAggrState.MinusState.S_GET_OUTPUT_SCAN;
        case S_GET_OUTPUT_SCAN:
          // Perform a scan to locate the current aggregation tuple for
          // the group to which the inpTuple belongs to, if it exists
          s.outTupleIter = outSyn.getScan(outScanId);

          // Since we have got a MINUS tuple now, the PLUS tuple for it should
          // have arrived earlier, implying that there should be an output
          // entry for this group.
          s.oldAggrTuple = s.outTupleIter.getNext();
          groupExists = s.oldAggrTuple != null;
          assert groupExists;
          evalContext.bind(s.oldAggrTuple, OLD_OUTPUT_ROLE);
          s.minusState = GroupAggrState.MinusState.S_RELEASE_OUTPUT_SCAN;
        case S_RELEASE_OUTPUT_SCAN:
          outSyn.releaseScan(outScanId, s.outTupleIter);
          s.minusState = GroupAggrState.MinusState.S_CHECK_IS_DIRTY;
        case S_CHECK_IS_DIRTY:
          s.dirtyTupleIter    = dirtySyn.getScan(dirtyScanId);
          s.dirtyTuple        = s.dirtyTupleIter.getNext();
          s.isDirty             = (s.dirtyTuple != null);
          dirtySyn.releaseScan(dirtyScanId, s.dirtyTupleIter);
          s.minusState = GroupAggrState.MinusState.S_CHECK_EMPTY_GROUP;
        case S_CHECK_EMPTY_GROUP:
          // There are two possibilities: the group becomes empty after this
          // minus tuple is processed, or otherwise. emptyGroupEval (a slight
          // misnomer) checks if there is only one element in this group.
          // emit a zero out if the group is empty and if the query has only
          // count()
          // without group by.
          s.isGroupEmpty = emptyGroupEval.eval(evalContext);
          // Emit the null row if the relation becomes null and there are no
          // group by attrs
          if (s.isGroupEmpty && nullOutputEval != null && !pendingEmptyGroup)
          {
            s.minusState = GroupAggrState.MinusState.S_EMIT_NULL_OUTPUT;
            
            // In S_EMIT_NULL_OUTPUT, we will create a null output tuple for the group.
            // If there is no group by attribute, then we need to remove the null tuple too
            // to avoid a pile up of null tuples for groups which no longer exists.
            // Hence set the pendingEmptyGroup flag to true so that we can remove
            // null tuple of this group from synopsis as well as emit the output(if required)
            if(numGroupByAttrs != 0)
              pendingEmptyGroup = true;
            break;
          }
          
          if (s.isGroupEmpty || pendingEmptyGroup)
          {
           if(pendingEmptyGroup)
             pendingEmptyGroup = false;
           s.minusState = GroupAggrState.MinusState.S_EMPTY_GROUP;
          }
          else
          {
            s.minusState = GroupAggrState.MinusState.S_ACTIVE_GROUP;
            break;
          }

        case S_EMPTY_GROUP:
          // delete the old aggregation tuple from our synopsis
          outSyn.deleteTuple(s.oldAggrTuple);
          if ((numUDA > 0  || numXmlAgg > 0) && !oneGroup)
            s.minusState = GroupAggrState.MinusState.S_RELEASE_AGGR_HANDLERS;
          else if (numUDA > 0  || numXmlAgg > 0)
            s.minusState = GroupAggrState.MinusState.S_RESET_AGGR_HANDLERS_1;
          else
            s.minusState = GroupAggrState.MinusState.S_PREPARE_MINUS_ELEMENT_1;
          break;
        case S_RESET_AGGR_HANDLERS_1:
          evalContext.bind(s.oldAggrTuple, IEvalContext.AGGR_ROLE);
          if(numUDA > 0)
            resetHandlerEval.eval(evalContext);
          if(numXmlAgg > 0)
            resetIndexEval.eval(evalContext);
          s.minusState = GroupAggrState.MinusState.S_PREPARE_MINUS_ELEMENT_1;
          break;
        case S_RELEASE_AGGR_HANDLERS:
          evalContext.bind(s.oldAggrTuple, IEvalContext.AGGR_ROLE);
          if(numUDA > 0)
            releaseHandlerEval.eval(evalContext);
          if(numXmlAgg > 0)
            releaseIndexEval.eval(evalContext);
          s.minusState = GroupAggrState.MinusState.S_PREPARE_MINUS_ELEMENT_1;
        case S_PREPARE_MINUS_ELEMENT_1:
          // If Group is Dirty
          //   Remove dirtyTuple from dirtySyn
          //   Goto S_FINISHED
          // else
          //   Output a Minus Tuple for this group
          // MINUS tuple's total Order Flag will not matter in this case;
          // If Group is dirty; then whether next tuple comes with higher ts
          // or not; group will be created as new and insertion into dirtysyn
          // will also be decided from next tuple's totalOrderFlag
          // So we will remove dirtyTuple from dirtySyn
          // If Group is not dirty;
          //  then it means that negative of previous aggr is not yet emitted
          //  we will output negative of previous aggr with current Timestamp
          //  value in both cases(current inp's totalOrderflag = true or false)
          if(s.isDirty)
          {
            dirtySyn.deleteTuple(s.dirtyTuple);
            s.dirtyTupleCount--;
            s.minusState = GroupAggrState.MinusState.S_FINISHED;
            break;
          }
          s.state = ExecState.S_OUTPUT_ELEMENT;
          done = true;
          s.minusState = GroupAggrState.MinusState.S_ENQUEUE_MINUS_1;
          break;
        case S_ENQUEUE_MINUS_1:
          // total Order Flag for the negative of previous aggr will depend 
          // on flag of current input tuple and current state of dirty syn
          // If current inps' flag = true && dirty synopsis is empty;
          //    It means next group formed will be of higher timestamp value
          // else next output timestamp value may be same as previous one
          s.nextTupleTotalOrderingGuarantee 
            = (s.dirtyTupleCount == 0) && s.isTotalOrderingGuarantee ;
          s.outputTs = s.inputTs;
          s.outputElement.setKind(QueueElement.Kind.E_MINUS);
          s.outputElement.setTs(s.outputTs);
          s.outputElement.setTuple(s.oldAggrTuple);
          s.outputElement.setTotalOrderingGuarantee
            (s.isTotalOrderingGuarantee);
          s.state = ExecState.S_OUTPUT_READY;
          done = true;
          s.minusState = GroupAggrState.MinusState.S_FINISHED;
          break;
          
        case S_EMIT_NULL_OUTPUT:
          s.state = ExecState.S_OUTPUT_TUPLE;
          done = true;
          s.minusState = GroupAggrState.MinusState.S_BIND_NULL_OUTPUT_TUPLE;
          break;

        case S_BIND_NULL_OUTPUT_TUPLE:
          evalContext.bind(s.outputTuple, IEvalContext.NEW_OUTPUT_ROLE);
          nullOutputEval.eval(evalContext);
          s.minusState = GroupAggrState.MinusState.S_INSERT_INTO_OUT_SYNOPSIS;
          break;
          
        case S_ACTIVE_GROUP:
          // If Group is Dirty
          //    Don't Create new Aggregation tuple
          // else
          //    Create a new Aggregation tuple
          if(!s.isDirty)
          {
            s.state = ExecState.S_OUTPUT_TUPLE;
            done = true;
          }
          s.minusState = GroupAggrState.MinusState.S_BIND_OUTPUT_TUPLE;
          break;
        case S_BIND_OUTPUT_TUPLE:
          if(s.isDirty)
            evalContext.bind(s.oldAggrTuple, NEW_OUTPUT_ROLE);
          else
            evalContext.bind(s.outputTuple, NEW_OUTPUT_ROLE);
          // Assert: At this point, evalContext contains the new input tuple
          // (MINUS), the old aggr. tuple for the input tuples group bound,
          // and the memory for the new aggr output tuple

          // We first determine if we need to scan the entire inner relation to
          // update the new aggregation tuple (this can happen if we have MAX/
          // MIN aggregation functions or if we have user defined aggregations
          // that require a full scan for recomputation - non incremental
          // processing)
          // or if we can incrementatlly produce the
          // new Aggr. tuple for the group from the old aggr. tuple and the new
          // input tuple.

          // And to determine if we need to scan the entire inner relation,
          // the expression parameters need to be evaluated first

          arithScanNotReqEval.eval(evalContext);
          if (numFullUDA == 0 && scanNotReqEval.eval(evalContext) &&
              numXmlAgg == 0)
          {
            minusEval.eval(evalContext);
            s.minusState = GroupAggrState.MinusState.S_PROCESSING_1;
            break;
          }
          else
            s.minusState = GroupAggrState.MinusState.S_RESCAN_INPUT_SYN;
        case S_RESCAN_INPUT_SYN:
          // scan iterator that returns all tuples in input synopsis
          // corresponding to the present group.
          s.inTupleIter = inSyn.getScan(inScanId);
          // If we come here the synopsis can't be empty
          inTuple = s.inTupleIter.getNext();
          evalContext.bind(inTuple, INPUT_ROLE);
          s.minusState = GroupAggrState.MinusState.S_RESCAN_INPUT_SYN_1;
        case S_RESCAN_INPUT_SYN_1:
          initEval.eval(evalContext);
          inTupleStorageAlloc.release(inTuple);
          s.inTuple = s.inTupleIter.getNext();
          s.minusState = GroupAggrState.MinusState.S_RESCAN_INPUT_SYN_2;
        case S_RESCAN_INPUT_SYN_2:
          while (s.inTuple != null)
          {
            evalContext.bind(s.inTuple, INPUT_ROLE);
            updateEval.eval(evalContext);
            inTupleStorageAlloc.release(s.inTuple);
            s.inTuple = s.inTupleIter.getNext();
          }
          s.minusState = GroupAggrState.MinusState.S_RELEASE_INPUT_SCAN;
        case S_RELEASE_INPUT_SCAN:
          inSyn.releaseScan(inScanId, s.inTupleIter);
          s.minusState = GroupAggrState.MinusState.S_PROCESSING_1;
          
        case S_PROCESSING_1:
          if(s.isDirty)
            s.minusState = GroupAggrState.MinusState.S_PROCESSING_2;
          else
            s.minusState = GroupAggrState.MinusState.S_INSERT_INTO_OUT_SYNOPSIS;
          break;
          
        case S_INSERT_INTO_OUT_SYNOPSIS:
          outSyn.insertTuple(s.outputTuple);
          s.minusState = GroupAggrState.MinusState.S_DELETE_FROM_OUT_SYNOPSIS;
        case S_DELETE_FROM_OUT_SYNOPSIS:
          outSyn.deleteTuple(s.oldAggrTuple);
          s.minusState = GroupAggrState.MinusState.S_PROCESSING_2;
        case S_PROCESSING_2:
          if ((numUDA > 0 || numXmlAgg > 0) && s.isDirty)
            s.minusState = GroupAggrState.MinusState.S_RESET_DIRTY_TUPLE;
          else if((numUDA > 0 || numXmlAgg > 0) && !s.isDirty)
            s.minusState = GroupAggrState.MinusState.S_RESET_AGGR_HANDLERS_2;
          else if(s.isDirty)
            s.minusState = GroupAggrState.MinusState.S_RESET_DIRTY_TUPLE;
          else
            s.minusState = GroupAggrState.MinusState.S_PREPARE_MINUS_ELEMENT_2;
          break;
        case S_RESET_AGGR_HANDLERS_2:
          evalContext.bind(s.oldAggrTuple, IEvalContext.AGGR_ROLE);
          if(numUDA > 0)
            resetHandlerEval.eval(evalContext);
          if(numXmlAgg >0)
            resetIndexEval.eval(evalContext);
          s.minusState = GroupAggrState.MinusState.S_PREPARE_MINUS_ELEMENT_2;
          break;
        case S_RESET_DIRTY_TUPLE:
          //   if Group becomes empty & nullEval is true
          //     then replace dirtyTuple by nullTuple
          //   else
          //     DirtyTuple will get modified automatically
          //     as both s.dirtyTuple and s.oldAggrTuple are refering to same
          //     tuple. Also s.dirtyTupleCount will remain unchanged.
          
          if(s.isGroupEmpty)
          {
            dirtySyn.deleteTuple(s.dirtyTuple);
            s.dirtyTupleCount-- ;
            if(!pendingEmptyGroup)
            {
              dirtySyn.insertTuple(s.outputTuple);
              s.dirtyTupleCount++;
            }
          }
          if(pendingEmptyGroup)
            s.minusState = GroupAggrState.MinusState.S_PREPARE_PLUS_ELEMENT;
          else
            s.minusState = GroupAggrState.MinusState.S_FINISHED;
          break;
          
        case S_PREPARE_MINUS_ELEMENT_2:
          s.state = ExecState.S_OUTPUT_ELEMENT;
          done = true;
          s.minusState = GroupAggrState.MinusState.S_ENQUEUE_MINUS_2;
          break;
        case S_ENQUEUE_MINUS_2:
          // Action: set nextTupleOrdering flag = false
          // Reason: We are sending a MINUS here because group is not dirty;
          //   So a) If input tuple flag = TRUE; then soon after this MINUS; 
          //         we will send a PLUS of newly calculated aggr tuple
          //  and b) If input tuple flag = FALSE; then either outputDirtyTuples
          //         or any future tuple with flag T can output at this ts
          s.nextTupleTotalOrderingGuarantee = false;
          s.outputTs = s.inputTs;
          s.outputElement.setKind(QueueElement.Kind.E_MINUS);
          s.outputElement.setTs(s.outputTs);
          s.outputElement.setTuple(s.oldAggrTuple);
          s.outputElement.setTotalOrderingGuarantee
            (s.nextTupleTotalOrderingGuarantee);
          s.state = ExecState.S_OUTPUT_READY;
          done = true;
          // If next Tuple has higher timestamp value;
          //   then no need to insert it into dirty synopsis
          // else
          //   insert newly calculated aggr output into dirty synopsis
          if(s.isTotalOrderingGuarantee || pendingEmptyGroup)
            s.minusState = GroupAggrState.MinusState.S_PREPARE_PLUS_ELEMENT;
          else
          {
            dirtySyn.insertTuple(s.outputTuple);
            s.dirtyTupleCount++;
            //if(pendingEmptyGroup)
            //{
            //  s.minusState = GroupAggrState.MinusState.S_GET_OUTPUT_SCAN;
            //}
            //else
            s.minusState = GroupAggrState.MinusState.S_FINISHED;
          }            
          break;

        case S_PREPARE_PLUS_ELEMENT:
          s.state = ExecState.S_OUTPUT_ELEMENT;
          done = true;
          s.minusState = GroupAggrState.MinusState.S_ENQUEUE_PLUS;
          break;
        case S_ENQUEUE_PLUS:
          // Condition to Reach here:
          //   * Current input tuple is MINUS
          //   * Current input tuple is modifying the group either making it
          //     empty or changing the outputTuple
          //   * So Output totalTotal ordering flag will depend on current 
          //     input tuple's flag and dirty synopsis state.
          //     Ordering also depends on whether this o/p tuple
          //     is part of pair of (+Null, -Null) tuple which is emitted when
          //     group is empty and their is no groupby attrs
          s.nextTupleTotalOrderingGuarantee 
            = (s.dirtyTupleCount == 0) && s.isTotalOrderingGuarantee && !pendingEmptyGroup;
          s.outputTs = s.inputTs;
          s.outputElement.setKind(QueueElement.Kind.E_PLUS);
          s.outputElement.setTs(s.outputTs);
          s.outputElement.setTuple(s.outputTuple);
          s.outputElement.setTotalOrderingGuarantee
            (s.nextTupleTotalOrderingGuarantee);
          s.state = ExecState.S_OUTPUT_READY;
          done = true;
          if(pendingEmptyGroup)
            s.minusState = GroupAggrState.MinusState.S_GET_OUTPUT_SCAN;
          else 
          s.minusState = GroupAggrState.MinusState.S_FINISHED;
          break;
        case S_FINISHED:
          s.minusState = GroupAggrState.MinusState.S_INIT;
          s.state = ExecState.S_INPUT_ELEM_CONSUMED;
          done = true;
          break;

        default:
          assert false;
      }
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
  
  public void initializeState() throws CEPException
  {
    
    if(archivedRelationTuples != null)
    {
      GroupAggrState s = (GroupAggrState)mut_state;
      for(ITuplePtr currentTuple : archivedRelationTuples)
      {
        ITuple r = currentTuple.pinTuple(IPinnable.READ);
        
        // Insert into output synopsis
        outSyn.insertTuple(currentTuple);
                 
        // Insert into output queue
        
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
        s.stats.incrNumOutputs();
        currentTuple.unpinTuple();   
        // Group is non null
        s.isInitNullRowEmitted = true;
      
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
                   " sent heartbeat of "+ s.lastOutputTs + " with ordering guarantee false");
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
        oldDataPropNeeded = false;
      
      //remove all the archived relation tuples.
      archivedRelationTuples.clear();
    }
  }
  
  /**
   * Create snapshot of Group Aggregate operator by writing the operator state
   * into param java output stream.
   * State of Group Aggregate operator consists of following:
   * 1. Mutable State
   * 2. Output Synopsis
   * 3. Dirty Synopsis
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
      output.writeObject((GroupAggrState)mut_state);

      if (SnapshotContext.getVersion() >= SnapshotContext.GROUPAGGR_IN_SYN_VERSION)
      {
          if (inSyn == null)
          {
              output.writeBoolean(true);
          }
          else
          {
              output.writeBoolean(false);
              inSyn.writeExternal(output, new SynopsisPersistenceContext(inFullScanId));
          }
      }
      
      // Write flag to output stream to set whether operator needs to propagate
      // null output
      if(SnapshotContext.getVersion() >= SnapshotContext.GROUPAGGR_EMPTY_GROUP_FLAG)
      {
        output.writeBoolean(pendingEmptyGroup);
      }
      
      // Write output synopsis to output stream
      outSyn.writeExternal(output, new SynopsisPersistenceContext(fullScanId));
      
      dirtySyn.writeExternal(output, new SynopsisPersistenceContext(dirtyFullScanId));
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
      // Read MutableState from input stream
      GroupAggrState loaded_mutable_state = (GroupAggrState) input.readObject();
      ((GroupAggrState)mut_state).copyFrom(loaded_mutable_state);

      if (SnapshotContext.getVersion() >= SnapshotContext.GROUPAGGR_IN_SYN_VERSION)
      {
          boolean isSynNull = input.readBoolean();
          if (!isSynNull) {
              IPersistenceContext sharedSynopsisRecoveryCtx = new SynopsisPersistenceContext();
              sharedSynopsisRecoveryCtx.setCache(new HashSet());

              // Read input synopsis from input stream
              inSyn.readExternal(input, sharedSynopsisRecoveryCtx);
          }
      }
      
      // Read flag from input stream to set whether operator needs to propagate
      // null output
      if(SnapshotContext.getVersion() >= SnapshotContext.GROUPAGGR_EMPTY_GROUP_FLAG)
      {
        pendingEmptyGroup = input.readBoolean();
      }

      // Both outSyn and dirtySyn synopses are sharing same store, we have to make sure
      // that while recovering synopsis, store doesn't receive duplicate tuples.
      // To ensure that, we will maintain a cache of tuple identifiers in shared
      // context. Each synopsis will update this cache and mark tuple as recovered
      // only if it is not present in cache.
      // Recovered tuple will be inserted into actual list of store; Otherwise
      // we will update the stub bits for tuple.
      
      IPersistenceContext sharedSynopsisRecoveryCtx = new SynopsisPersistenceContext();
      sharedSynopsisRecoveryCtx.setCache(new HashSet());
      
      // Read output synopsis from input stream
      outSyn.readExternal(input, sharedSynopsisRecoveryCtx);
      
      // Read dirty synopsis from input stream
      dirtySyn.readExternal(input, sharedSynopsisRecoveryCtx);
    } 
    catch (ClassNotFoundException e)
    {
      throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR_CNF, e, this.getOptName(), e.getMessage());
    } 
    catch (IOException e)
    {
      throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR,e,e.getLocalizedMessage(), getOptName());
    }
  }
}
