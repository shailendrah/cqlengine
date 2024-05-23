/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/PatternStrmClassB.java /main/53 2011/12/28 18:07:26 udeshmuk Exp $ */
/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    12/26/11 - XbranchMerge udeshmuk_bug-13527537_ps6 from
                           st_pcbpel_pt-ps6
    udeshmuk    12/20/11 - throw hardexception instead of soft exec exception
    sborah      01/23/11 - change to eval(evalContext)
    udeshmuk    07/24/09 - integrate RC fix
    udeshmuk    06/30/09 - Reorg - move from state based to function based
                           model
    udeshmuk    06/10/09 - Fix RC issues
    udeshmuk    05/21/09 - handle boundary conditions of non-event
    udeshmuk    05/15/09 - use activeItems to quickly find partns for non-event
    udeshmuk    04/22/09 - do not use input tuple as partnTuple.
    udeshmuk    04/12/09 - fix non-event
    udeshmuk    04/08/09 - total ordering optimization
    udeshmuk    03/31/09 - partn by without all matches opt
    udeshmuk    03/10/09 - restructuring bindstore.
    udeshmuk    03/05/09 - change copyEval to measureEval
    udeshmuk    02/02/09 - support for duration arith_expr in pattern
    udeshmuk    11/05/08 - rename pattern store to private store.
    udeshmuk    10/28/08 - orderby support in xmlagg in pattern.
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    udeshmuk    10/12/08 - pattern specific partition store.
    udeshmuk    10/01/08 - getPartnArr related changes.
    sborah      09/25/08 - update stats.
    udeshmuk    09/24/08 - optimization for single noon-group variable pattern.
    udeshmuk    09/24/08 - partition window store related optimization
    udeshmuk    09/13/08 - manually profiling the operator.
    udeshmuk    09/07/08 - recurring nonevent refresh
    sbishnoi    08/18/08 - 
    udeshmuk    07/31/08 - fix bug 7240994
    sbishnoi    07/28/08 - support for nanosecond ts; updating var names and
                           comments
    udeshmuk    07/12/08 - 
    rkomurav    07/07/08 - support for recring non event detection
    sbishnoi    06/29/08 - override isHeartbeatPending
    rkomurav    05/15/08 - support non event detection
    rkomurav    03/20/08 - support subset
    rkomurav    03/12/08 - fix ALL MATCHES bug to get all trans instead of just
                           oredered trans
    rkomurav    03/04/08 - support duplicate correlation names
    rkomurav    02/25/08 - redo Aggr calculations with bitvector
    rkomurav    02/21/08 - replace DFA with NFA
    hopark      02/28/08 - resurrect refcnt
    anasrini    02/07/08 - all matches and partition - call remove empty partns
    rkomurav    02/01/08 - remove hard coded value 0 for undefined state
    rkomurav    01/24/08 - add undefined statein DFA\
    rkomurav    01/03/08 - optimize by calling DFA's ordered transitions API
    hopark      12/14/07 - copy opt
    hopark      12/07/07 - cleanup spill
    hopark      10/30/07 - remove IQueueElement
    hopark      10/21/07 - remove TimeStamp
    rkomurav    10/18/07 - ALL MATCHES 
    anasrini    09/26/07 - ALL MATCHES support
    rkomurav    10/09/07 - cleanup partnheader removal
    rkomurav    10/03/07 - remove emptly parittion headers
    rkomurav    09/27/07 - support non mandatory correlation defs
    rkomurav    09/12/07 - add prtn win store
    rkomurav    09/12/07 - refcont fix for prtnindex
    hopark      09/07/07 - eval refactor
    rkomurav    09/06/07 - add prevrole
    rkomurav    08/06/07 - partnby timestamp ordering bug
    rkomurav    07/16/07 - uda
    rkomurav    07/19/07 - add pin to prevTuple
    hopark      07/13/07 - dump stack trace on exception
    anasrini    07/12/07 - support for PARTITION BY
    parujain    07/03/07 - cleanup
    rkomurav    06/27/07 - add into finalList in the end
    parujain    06/26/07 - mutable state
    hopark      06/22/07 - logging
    rkomurav    05/15/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/PatternStrmClassB.java st_pcbpel_udeshmuk_reorg_and_variable_nonevent/4 2010/12/13 01:51:52 udeshmuk Exp $ *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;

import oracle.cep.common.TimeUnit;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.pattern.AllMatchesNonEventProcessor;
import oracle.cep.execution.pattern.AllMatchesNormalProcessor;
import oracle.cep.execution.pattern.AllMatchesWithinProcessor;
import oracle.cep.execution.pattern.Binding;
import oracle.cep.execution.pattern.NonEventProcessor;
import oracle.cep.execution.pattern.PatternExecContext;
import oracle.cep.execution.pattern.PatternProcessor;
import oracle.cep.execution.pattern.SkipPastLastRowFixedNonEventProcessor;
import oracle.cep.execution.pattern.SkipPastLastRowNormalProcessor;
import oracle.cep.execution.pattern.SkipPastLastRowVariableNonEventProcessor;
import oracle.cep.execution.pattern.SkipPastLastRowWithinProcessor;
import oracle.cep.execution.synopses.BindingSynopsis;
import oracle.cep.execution.synopses.PrivatePartnWindowSynopsis;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.pattern.PatternSkip;
import oracle.cep.service.ExecContext;
import oracle.cep.snapshot.SnapshotContext;
import oracle.cep.util.NFA;

public class PatternStrmClassB extends ExecOpt
{
  /*
   * Pattern processor related variables
   */
  
  /** This has variables that are shared by PatternStrmClassB
   *  and PatternProcessor
   */
  private PatternExecContext pc;
  
  /** 
   * A helper object which will do pattern processing and reporting
   */
  private PatternProcessor   patternProc;
    
  /*
   * PREV related variables
   */
  
  /** Eval context for copy eval for partn tuple */
  private IEvalContext               partnCopyEvalContext;
  
  /** Copy eval for partn tuple */
  private IAEval                     partnCopyEval;
  
  /** partition tuple factory
   *  if no partition attrs present, partnTupleFactory is null */
  private IAllocator<ITuplePtr>      partnTupleFactory;
  
  /** Partition Window Synopsis */
  private PrivatePartnWindowSynopsis partnSyn;
  
  /** prev tuple role */
  private int                        prevRole;
  
  /** max number of prev tuples to be maintained */
  private int                        maxPrevIndex;
  
  /** true if query has prev with range, false otherwise */
  private boolean                    prevRangeExists;
  
  /** Partition size used for PREV processing with Partition Synopsis */
  private int                        partnSize = 0;

  /*
   * Non-event related variables 
   */
  
  /** flag indicating non event detection case */
  private boolean                    isNonEvent;

  /** set to TRUE if query has non-event with variable duration, false otherwise */
  private boolean                    isVariableDuration;
  
  /*
   * Miscellaneous variables
   */
  
  /** SKIP clause **/
  private PatternSkip           skipClause;
    
  /** Null eval for aggrs */
  private IAEval                nullEval;
      
  /** allocator for order by tuple */
  private IAllocator<ITuplePtr> orderByAllocator;
  
  /** true if time has advanced between successive tuples, false otherwise */
  private boolean               timeAdvanced;
    
  public PatternStrmClassB(ExecContext ec)
  {
    super(ExecOptType.EXEC_PATTERN_STRM_CLASSB, new PatternStrmClassBState(ec), ec);
    pc = new PatternExecContext();
  }
  
  public void initialize() throws ExecException
  {
    assert mut_state instanceof PatternStrmClassBState;
    PatternStrmClassBState s = (PatternStrmClassBState)mut_state;
    s.initialize(pc.nullInputTuple, pc.bindSyn.getBindingB0());
    
    //Create pattern processor object
    
    if(!isNonEvent)
    {
      switch(skipClause)
      {
        case ALL_MATCHES :
          if(pc.isWithin || pc.isWithinInclusive)
            patternProc = new AllMatchesWithinProcessor(s);
          else
            patternProc = new AllMatchesNormalProcessor(s);
          break;
        case SKIP_PAST_LAST_ROW:
          if(pc.isWithin || pc.isWithinInclusive)
            patternProc = new SkipPastLastRowWithinProcessor(s);
          else
            patternProc = new SkipPastLastRowNormalProcessor(s);
          break;
        default:
          patternProc = null;
      }
    }
    else
    {
      switch(skipClause)
      {
        case ALL_MATCHES :
          patternProc = new AllMatchesNonEventProcessor(s);
          break;
        case SKIP_PAST_LAST_ROW:
          if(!isVariableDuration)
            patternProc = new SkipPastLastRowFixedNonEventProcessor(s);
          else
            patternProc = new SkipPastLastRowVariableNonEventProcessor(s);
          break;
        default:
          patternProc = null;
      }
    }
    assert patternProc != null : "Pattern processor could not be created";
    //Set variables for which there is no explicit setter in PatternStrmClassB.
    patternProc.setPatternExecContext(pc);
    patternProc.setInTupleStorageAlloc(inTupleStorageAlloc);
    patternProc.setTupleStorageAlloc(tupleStorageAlloc);
    patternProc.setOutputQueue(outputQueue);
    patternProc.setAggrPos(s.bindingB0.getElems().length - 1);
      
  }
  
  /**
   * @param prevRangeExists the prevRangeExists to set
   */
  public void setPrevRangeExists(boolean prevRangeExists)
  {
    this.prevRangeExists = prevRangeExists;
  }

  /**
   * @param partnCopyEvalContext the partnCopyEvalContext to set
   */
  public void setPartnCopyEvalContext(IEvalContext partnCopyEvalContext)
  {
    this.partnCopyEvalContext = partnCopyEvalContext;
  }

  /**
   * @param partnTupleFactory the partnTupleFactory to set
   */
  public void setPartnTupleFactory(IAllocator<ITuplePtr> partnTupleFactory)
  {
    this.partnTupleFactory = partnTupleFactory;
  }

  /**
   * @param aggrTupleFactory the aggrTupleFactory to set
   */
  public void setAggrTupleFactory(IAllocator<ITuplePtr> aggrTupleFactory)
  {
    if(aggrTupleFactory != null)
      pc.aggrsPresent = true;
    else 
      pc.aggrsPresent = false;
  }

  /**
   * @param hasAggrs the hasAggrs to set
   */
  public void setHasAggrs(boolean[] hasAggrs)
  {
    if(patternProc != null)
      patternProc.setHasAggrs(hasAggrs);
  }

  /**
   * @param incrEvals the incrEvals to set
   */
  public void setIncrEvals(IAEval[] incrEvals)
  {
    if(patternProc != null)
      patternProc.setIncrEvals(incrEvals);
  }

  /**
   * @param initEvals the initEvals to set
   */
  public void setInitEvals(IAEval[] initEvals)
  {
    if(patternProc != null)
      patternProc.setInitEvals(initEvals);
  }

  /**
   * @param partnCopyEval the copyPartnEval to set
   */
  public void setPartnCopyEval(IAEval partnCopyEval)
  {
    this.partnCopyEval = partnCopyEval;
  }

  /**
   * @param nullEval the nullEval to set
   */
  public void setNullEval(IAEval nullEval)
  {
    this.nullEval = nullEval;
  }
  
  /**
   * @param releaseEval the releaseEval to set
   */
  public void setReleaseEval(IAEval releaseEval)
  {
    if(patternProc != null)
      patternProc.setReleaseEval(releaseEval);
  }

  /**
   * @param pc.bindSyn the bindSyn to set
   */
  public void setBindSyn(BindingSynopsis bindSyn)
  {
    pc.bindSyn = bindSyn;
  }

  /**
   * @param evalContext the evalContext to set
   */
  public void setEvalContext(IEvalContext evalContext)
  {
    pc.evalContext = evalContext;
  }

  /**
   * @param bindRole the bindRole to set
   */
  public void setBindRole(int bindRole)
  {
    pc.bindRole = bindRole;
  }

  /**
   * @param prevRole the prevRole to set
   */
  public void setPrevRole(int prevRole)
  {
    this.prevRole = prevRole;
  }

  /**
   * @param maxPrevIndex the maxPrevIndex to set
   */
  public void setMaxPrevIndex(int maxPrevIndex)
  {
    this.maxPrevIndex = maxPrevIndex;
  }

  /**
   * @param measureEval the measureEval to set
   */
  public void setMeasureEval(IAEval measureEval) 
  {
    if(patternProc != null)
      patternProc.setMeasureEval(measureEval);
  }

  /**
   * @param defs the defs to set
   */
  public void setDefs(IBEval[] defs)
  {
    if(patternProc != null)
      patternProc.setDefs(defs);
  }

  /**
   * @param dfa the dfa to set
   */
  public void setNfa(NFA nfa)
  {
    if(patternProc != null)
      patternProc.setNfaRelatedVars(nfa);
  }

  /**
   * @param nullInputTuple the nullInputTuple to set
   */ 
  public void setNullInputTuple(ITuplePtr nullInputTuple)
  {
    pc.nullInputTuple = nullInputTuple;
  }

  /**
   * @param hasPartnAttrs true iff there are partition by attributes
   */
  public void setHasPartnAttrs(boolean hasPartnAttrs)
  {
    pc.hasPartnAttrs = hasPartnAttrs;
  }

  /**
   * @param numUDA the numUDA to set
   */
  public void setNumUDA(int numUDA)
  {
    if(patternProc != null)
      patternProc.setNumUDA(numUDA);
  }

  /**
   * @param partnSyn the prtnSyn to set
   */
  public void setPartnSyn(PrivatePartnWindowSynopsis partnSyn)
  {
    this.partnSyn = partnSyn;
  }

  /**
   * @param skipClause the SKIP clause to set
   */
  public void setSkipClause(PatternSkip skipClause)
  {
    this.skipClause   = skipClause;
  }

  /**
   * @param subsetPos the subsetPos to set
   */
  public void setSubsetPos(int[][] subsetPos)
  {
    if(patternProc != null)
      patternProc.setSubsetPos(subsetPos);
  }

  /**
   * @param duration the duration to set
   */
  public void setDurationValue(long duration)
  {
    if(patternProc != null)
      patternProc.setDurationValue(duration);
  }

  /**
   * @param isDurationExpr true if duration clause contains expr
   */
  public void setIsDurationExpr(boolean isDurationExpr)
  {
    if(patternProc != null)
      ((NonEventProcessor)patternProc).setDurationExpr(isDurationExpr);
  }
  
  /**
   * @param durEval eval to compute the duration value
   */
  public void setDurationEval(IAEval durEval)
  {
    if(patternProc != null)
      ((NonEventProcessor)patternProc).setDurationEval(durEval);
  }
  
  /**
   * @param role - role containing result of duration expr evaluation 
   */
  public void setDurationRole(int role)
  {
    if(patternProc != null)
      ((NonEventProcessor)patternProc).setDurationRole(role);
  }
  
   /**
    * @param pos - position in the durationRole that will have value of
    *              duration expr evaluation 
    */
  public void setDurationPos(int pos)
  {
    if(patternProc != null)
      ((NonEventProcessor)patternProc).setDurationPos(pos);
  }
  
  /**
   * @param unit - timeunit for duration clause expression, null if not applicable
   */
  public void setDurationUnit(TimeUnit unit)
  {
    if(patternProc != null)
      ((NonEventProcessor)patternProc).setDurationUnit(unit);
  }
  
  /**
   * @param isNonEvent the isNonEvent to set
   */
  public void setNonEvent(boolean isNonEvent)
  {
    this.isNonEvent = isNonEvent;
  }

  /**
   * @param isVariableDuration value to be set
   */
  public void setIsVariableDuration(boolean isVariableDuration)
  {
    this.isVariableDuration = isVariableDuration;  
  }
  
  /**
   * @param durationSymAlphIndex the durationSymAlphIndex to set
   */
  public void setDurationSymAlphIndex(int durationSymAlphIndex)
  {
    if(patternProc != null)
      ((NonEventProcessor)patternProc).setDurationSymAlphIndex(durationSymAlphIndex);
  }

  /**
   * @param isRecurringNonEvent the isRecurringNonEvent to set
   */
  public void setRecurringNonEvent(boolean isRecurringNonEvent)
  {
    if(patternProc != null)
      ((NonEventProcessor)patternProc).setRecurringNonEvent(isRecurringNonEvent);
  }

  public void setOrderByAllocator(IAllocator<ITuplePtr> orderByAllocator)
  {
    this.orderByAllocator = orderByAllocator;
  }

  public void setNumXmlAgg(int numXmlAgg)
  {
    if(patternProc != null)
      patternProc.setNumXmlAgg(numXmlAgg);
  }
  
  public void setReleaseIndexEval(IAEval releaseIndexEval)
  {
    if(patternProc != null)
      patternProc.setReleaseIndexEval(releaseIndexEval);
  }
  
  public void setWithin(boolean isWithin)
  {
    this.pc.isWithin = isWithin;
  }
  
  public void setWithinInclusive(boolean isWithinInclusive)
  {
    this.pc.isWithinInclusive = isWithinInclusive;
  }
  
  private void applyNullEvals(Binding binding) throws ExecException
  {
    ITuplePtr[] elems = binding.getElems();
    int index = elems.length - 1;
    //Bind the aggregate tuple to the eval context
    pc.evalContext.bind(elems[index], pc.bindRole + index);
    nullEval.eval(pc.evalContext);
  }
  
  /**
   * Dequeues the input from input queue. Can probably be moved to super-class.
   * @param s Pattern State
   * @return true if processing should quit(because inputQueue is empty)
   *         false otherwise
   * @throws ExecException
   */
  private boolean dequeueInput(PatternStrmClassBState s) throws ExecException
  {
    s.inputElement = inputQueue.dequeue(s.inputElementBuf);
    if(s.inputElement == null)
      return true;
    else 
      return false;
  }
  
  /**
   * Initialize different variables and members of mutable state using the
   * current input.
   * @param s Pattern State
   * @throws SoftExecException
   * @throws ExecException
   */
  private void initializeVars(PatternStrmClassBState s) throws SoftExecException,
    ExecException
  {
    pc.isHeartBeat  = false;
    
    // Update our counts
    s.stats.incrNumInputs();

    // Increment the sequence number
    s.incrementSeq();
    
    // Update last input ts
    s.inputTs = s.inputElement.getTs();
    
    // update input tuple
    s.inputTuple = s.inputElement.getTuple();
    
    // We should have a progress of time.
    if (s.lastInputTs > s.inputTs)
    {
      throw ExecException.OutOfOrderException(
              this,
              s.lastInputTs,
              s.inputTs,
              s.inputElement.toString());
    }
    
    if(s.lastInputTs < s.inputTs)
      timeAdvanced = true;
    
    assert s.inputTs >= s.minNextTs :
           getDebugInfo(s.inputTs, s.minNextTs, 
                        s.inputElement.getKind().name(),
                        s.lastInputKind.name());
                  
    // Update the last input Ts now
    s.lastInputTs = s.inputTs;
    s.lastInputKind = s.inputElement.getKind();
    s.lastInputTotalOrderingFlag = s.inputElement.getTotalOrderingGuarantee();
    s.minNextTs = s.lastInputTotalOrderingFlag ? s.inputTs + 1 : s.inputTs;

    //output kind is E_PLUS irrespective of the case if input
    //is either of E_PLUS or E_HEARTBEAT
    s.outputKind = QueueElement.Kind.E_PLUS;
        
    if(s.inputElement.getKind() == QueueElement.Kind.E_PLUS)
      pc.evalContext.bind(s.inputTuple, IEvalContext.INPUT_ROLE);
    
    //heartbeat variable set
    if(s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
      pc.isHeartBeat = true;
  
    if(orderByAllocator != null)
    {
      s.orderByTuple = orderByAllocator.allocate();
      pc.evalContext.bind(s.orderByTuple, IEvalContext.XML_AGG_INDEX_ROLE);
    }
  }
  
  /**
   * Expire PREV tuples that are out of range from the Partition synopsis.
   * Useful only for queries having PREV with Range. 
   * @param s Pattern State
   * @throws ExecException
   */
  private void expireOutOfRange(PatternStrmClassBState s) throws ExecException
  {
    if(PatternExecContext.trackStats)
      pc.tempTime = System.currentTimeMillis(); 
    if(pc.hasPartnAttrs && prevRangeExists && timeAdvanced)
    {
      partnSyn.expireTuples(s.inputTs);
    }
    if(PatternExecContext.trackStats)
    {
      pc.tempTime = System.currentTimeMillis() - pc.tempTime;
      s.stats.addExpireOutOfRangeTime(pc.tempTime);
      s.stats.addPrevFuncTime(pc.tempTime);
    }
  }
  
  /**
   * Set up the PREV tuples for the current input
   * @param s Pattern state
   * @throws ExecException
   */
  private void setUpPrevTuples(PatternStrmClassBState s) throws ExecException
  {
    if(PatternExecContext.trackStats)
      pc.tempTime = System.currentTimeMillis();
    //Pre processing for Prev cases
    ITuplePtr[] prevArr = null;
    partnSize = 0;
    
    //This needs to be set here so as to get the partn context for 
    //current input tuple
    pc.bindSyn.setPartnContext(s.inputTuple);
    
    if(maxPrevIndex > 0 && pc.hasPartnAttrs && prevRangeExists)
    { //only in this case we use partn synopsis
      prevArr = partnSyn.getPartnArr(s.inputTuple);
      assert prevArr.length == maxPrevIndex;
      //getPrevArr returns array having tuples in proper order
      partnSize = pc.evalContext.bind(prevArr,prevRole,maxPrevIndex,true,
                                      pc.nullInputTuple);
    }
    else if(maxPrevIndex > 0)
    { /*
       * Covers 1. partnAttrs && maxPrevIndex > 0 && !prevRangeExists
       *        2. !partnAttrs && maxPrevIndex > 0 && prevRangeExists 
       *           (This would need some special handling)
       *        3. !partnAttrs && maxPrevIndex > 0 && !prevRangeExists
       * Out of the remaining 4 possibilities, 2 possibilities which will have 
       * maxPrevIndex <= 0 and prevRangeExists cannot occur. Nothing needs to 
       * be done for the remaining two.
       */
      prevArr = pc.bindSyn.getPrevArr();
      assert maxPrevIndex == prevArr.length;
      pc.evalContext.bind(prevArr,prevRole,maxPrevIndex,false,pc.nullInputTuple);
    }
    
    if(PatternExecContext.trackStats)
    {
      pc.tempTime = System.currentTimeMillis() - pc.tempTime;
      s.stats.addPreProcessTime(pc.tempTime);
      s.stats.addPrevFuncTime(pc.tempTime);
    }
  }
  
  /**
   * Pre-processing for PREV tuples.
   * Expire out of range tuples from the partition synopsis, if applicable.
   * Set up the PREV tuples.
   * @param s Pattern state
   * @throws ExecException
   */
  private void preProcess(PatternStrmClassBState s) throws ExecException
  {
    expireOutOfRange(s);
    setUpPrevTuples(s);
  }
  
  /**
   * Post-processing for PREV. 
   * @param s Pattern state
   * @throws ExecException
   */
  private void postProcess(PatternStrmClassBState s) throws ExecException
  {
    ITuplePtr copyInpTuple;
    if(PatternExecContext.trackStats)
      pc.tempTime = System.currentTimeMillis();

    //post processing
    
    if(maxPrevIndex > 0 && pc.hasPartnAttrs && prevRangeExists)
    {
      if(partnSize == maxPrevIndex)
      {
        ITuplePtr tptr = partnSyn.deleteOldestTuple(s.inputTuple);
        partnTupleFactory.release(tptr);
      }
      
      //make a copy of input tuple
      copyInpTuple = partnTupleFactory.allocate();
      partnCopyEvalContext.bind(copyInpTuple, IEvalContext.SCRATCH_ROLE);
      partnCopyEvalContext.bind(s.inputTuple, IEvalContext.INPUT_ROLE);
      //copy the tuple to scratch role and insert in synopsis
      partnCopyEval.eval(partnCopyEvalContext);
      partnSyn.insertTuple(copyInpTuple, s.inputTs);
    }
    else if(maxPrevIndex > 0)
    {
      s.prevTuple = s.inputTuple;
      inTupleStorageAlloc.addRef(s.prevTuple);
      pc.bindSyn.addPrevTuple(s.prevTuple, inTupleStorageAlloc);
    }
      
    if(PatternExecContext.trackStats)
    {        
      pc.tempTime = System.currentTimeMillis() - pc.tempTime;
      s.stats.addPostProcessTime(pc.tempTime);
      s.stats.addPrevFuncTime(pc.tempTime); 
    }
  }
     
  /**
   * Logic for reporting matches. Handles special pattern separately.
   * Skip clause based processing is done.
   * @param s Pattern state
   * @return true if processing should quit false otherwise
   * @throws ExecException
   */
  private boolean reportBindings(PatternStrmClassBState s) throws ExecException
  {
    boolean done = false;
    
    if(PatternExecContext.trackStats)
      pc.tempTime = System.currentTimeMillis();

    patternProc.reportBindings();
    
    if(PatternExecContext.trackStats)
    {
      pc.tempTime = System.currentTimeMillis() - pc.tempTime;
      s.stats.addBindingReportTime(pc.tempTime);
    }
    return done;
  }
  
  /**
   * End processing for an input
   * @param s Pattern state
   */
  private void inputConsumed(PatternStrmClassBState s)
  {
    assert s.inputElement != null;

    if (s.inputTuple != null)
    {
      inTupleStorageAlloc.release(s.inputTuple);
    }
  }
  
  /**
   * Run method. Contains the execution time logic for pattern operator.
   * @param timeSlice determines the max number of inputs processed by
   *        pattern operator during one execution 
   */
  public int run(int timeSlice) throws CEPException
  {
    PatternStrmClassBState s = (PatternStrmClassBState) mut_state;
    boolean done             = false;
    
    assert s.state != ExecState.S_UNINIT;
    s.stats.incrNumExecutions();
    timeAdvanced = false;
    
    //If state is PRE_INIT then do some initialization
    if(s.state == ExecState.S_PRE_INIT)
    {
      if(pc.aggrsPresent)
        applyNullEvals(s.bindingB0);
      //necessary to change state so that initialization happens only once.
      s.state = ExecState.S_INIT;
    }
    
    try
    {
      while((s.stats.getNumInputs() < timeSlice) && (!done))
      {
        //Dequeue input 
        done = dequeueInput(s);
        if(done)
          continue;
        
        // Initialize state and different variables
        initializeVars(s);
                
        //Process the input

        if (s.inputElement.getKind() == QueueElement.Kind.E_MINUS)
        {
          //Input to patternStrm operator is a stream
          assert false;
          continue;
        }
        else
        { 
          // expire out of range tuples and set up prev tuples for a non HB tuple
          if(!pc.isHeartBeat)
            preProcess(s);
          else
          {
            expireOutOfRange(s);
            //For HB tuple, processing is done only in non-event case.
            //Within that if no partitions then nonPartnCaseContext will be set
            //and for partitions the partition will be set appropriately while
            //processing bindings which expire
            if(!pc.hasPartnAttrs)
              pc.bindSyn.setPartnContext(s.inputTuple);
          }
          
          //process the tuple as per PATTERN, DEFINE and DURATION clause
          patternProc.processPattern();
          
          //report the final matches. Handling of MEASURES clause
          reportBindings(s);
          
          //post processing of prev tuples
          if(!pc.isHeartBeat)
            postProcess(s);
        }
        //input consumed processing
        inputConsumed(s);
      } //end while
    }
    catch (SoftExecException e1)
    {
      if(e1.getErrorCode() != ExecutionError.OUT_OF_ORDER)
        throw new ExecException((ExecutionError)e1.getErrorCode(), e1.getArgs());
      else
      {
        LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e1);
        return 0;
      }
    }    
    return 0;
  }
  
  public void deleteOp()
  {
    
  }
  
  @Override
  protected boolean isHeartbeatPending()
  {
    return false;
  }
  
  /**
   * Create snapshot of Pattern operator by writing the operator state
   * into param java output stream.
   * State of Pattern operator consists of following:
   * 1. Mutable State
   * 2. Pattern Execution Context
   * 3. Pattern Processor
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
      if (SnapshotContext.getVersion() >= SnapshotContext.PATTERNB_VERSION) {
          // 1. Write Mutable state to output stream
          output.writeObject((PatternStrmClassBState)mut_state);
            
          // 2. Pattern ExecContext
          output.writeObject(pc);
          
          // 3. Pattern Processor
          output.writeObject(patternProc);
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
      if (SnapshotContext.getVersion() >= SnapshotContext.PATTERNB_VERSION) {
          // Read MutableState from input stream
          PatternStrmClassBState loaded_mutable_state = (PatternStrmClassBState) input.readObject();
          ((PatternStrmClassBState)mut_state).copyFrom(loaded_mutable_state);
          
          // Read Pattern ExecContext from input stream
          PatternExecContext loaded_pc = (PatternExecContext) input.readObject();
          pc.copyFrom(loaded_pc);
          
          // Read Pattern Processor from input stream
          PatternProcessor loaded_patternProc = (PatternProcessor) input.readObject();
          patternProc.copyFrom(loaded_patternProc);
      }     
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

}


