/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/PatternProcessor.java /main/2 2011/02/07 03:36:26 sborah Exp $ */

/* Copyright (c) 2009, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      01/23/11 - change to eval(evalContext)
    udeshmuk    08/11/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/PatternProcessor.java st_pcbpel_udeshmuk_reorg_and_variable_nonevent/3 2010/12/13 01:51:52 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.pattern;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.BitSet;
import java.util.PriorityQueue;

import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.operators.PatternStrmClassBState;
import oracle.cep.execution.queues.Queue;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.util.NFA;
import oracle.cep.util.Transition;

/**
 * Handles processing of PATTERN, DEFINE, DURATION and MEASURES clause for 
 * an input. Super class of the entire processor hierarchy. 
 * @author udeshmuk
 */

public abstract class PatternProcessor implements Externalizable
{
  /**
   * Pattern exec context reference for accessing common data
   */
  protected PatternExecContext    pc;
  
  /**
   * State of the owning pattern operator
   */
  protected PatternStrmClassBState s;
  
  protected IAllocator<ITuplePtr> inTupleStorageAlloc;
  
  protected IAllocator<ITuplePtr> tupleStorageAlloc;
  
  protected Queue                 outputQueue;
  
  /** NFA corresponding to regular expression */
  protected NFA       nfa;
  
  /** Correlation definitions */
  protected IBEval[]  defs;
  
  /** measure exprs eval - evaluates to output role */
  protected IAEval    measureEval;
  
  /** Incr evals for aggrs */
  protected IAEval[]  incrEvals; 
  
  /** Init evals for aggrs */
  protected IAEval[]  initEvals; 
  
  /** Boolean array to maintain if a particular alphabet is associated
   *  with any aggrs either in measures exprs or correlation defs */
  protected boolean[] hasAggrs; 
  
  /** number of uda */
  protected int       numUDA;
  
  protected boolean[] isFinal; 

  protected IAEval    releaseEval;
  
  protected int       aggrPos;
    
  /** alphEval specifies if the alphabet has been evaluated in the
   * context of current binding and alphMatch specifies if its a match or not*/
  protected BitSet    alphEval; 
  protected BitSet    alphMatch; 
  
  /** Referenced subset positions of correlation definitions */
  protected int[][]   subsetPos;
 
  /** used in special pattern optimization */
  protected boolean   foundMatch=false; 
  
  /** number of xmlagg functions referred by pattern */
  private int         numXmlAgg; 
  
  /** release eval for xmlagg indexes */
  private IAEval      releaseIndexEval; 

  /** a priority queue of readytooutput bindings */
  protected PriorityQueue<Binding> readyToOutputBindings; 

  /** Minimum startIndex for bindings in activeList. Used in the reporting
   * logic.
   */
  protected long minActiveIndex = Long.MAX_VALUE; 

  /** 
   * Minimum matchedTs among unsure bindings across all partitions. Used in 
   * the reporting logic.
   */
  protected long minTs = Long.MAX_VALUE; 
  
  /** true if pattern is special , false otherwise */
  protected boolean specialPattern = false;
  
  /** duration in nanoseconds for a non event detection and WITHIN cases */
  protected long durationValue; 
  
  /** Empty Constructor for HA */
  public PatternProcessor()
  {}

  public PatternProcessor(PatternStrmClassBState state)
  {
    this.s = state;
  }
   
  /**
   * Apply the 'trans' transition on 'activeBind' to see if it can
   * be extended or it becomes a match or is deleted.
   * @param activeBind - binding to be processed
   * @param trans - transition (correlation var) to be applied
   * @return true if remaining bindings should be deleted, false otherwise 
   */
  protected abstract boolean applyAlph(Binding activeBind, Transition trans)
    throws ExecException;
      
  /**
   * Process the 'activeBind' by applying different transitions that
   * the inputTuple matches to. The activeBind can grow, turn into match
   * or can be deleted as a result.
   * @param activeBind - binding to be processed
   * @return true if remaining bindings should not be processed, false otherwise
   * @throws ExecException
   */
  protected abstract boolean processBinding(Binding activeBind) 
    throws ExecException;
  
  /**
   * Tells whether all transitions out of current state should be applied
   * on a binding.
   * @return true if all transitions should be applied, false otherwise
   */
  protected abstract boolean shouldAllTransitionsBeApplied();
  
  /**
   * Tells whether all the remaining bindings be deleted once a match is found.
   * @return true if all remaining bindings should be deleted, false otherwise
   */
  protected abstract boolean shouldRemainingBindingsBeDeleted();
  
  /** 
   * Apply conditions on the current input to determine to which correlation
   * variables it matches. See if the existing active and unsure bindings can 
   * be extended/new ones generated. Depending on the SKIP clause determines 
   * what bindings to process.
   * @param s Pattern state
   * @throws ExecException
   */
  protected abstract void processTuple() throws ExecException;
  
  /**
   * Contains processing flow to handle PATTERN, DEFINE and DURATION clause
   * @throws ExecException
   */
  public abstract void processPattern() throws ExecException;

  /**
   * Sets the target time (time at which binding expires) for a binding.
   * Relevant for non-event processing.
   * @param b - Binding
   * @param inputTs - input timestamp of the first tuple of the binding
   * @throws ExecException
   */
  protected abstract void setTargetTime(Binding b, long inputTs) 
    throws ExecException;
  
  /**
   * Logic that does the output of matches. Handles MEASURES clause.
   * @return true if processing should be quit, false otherwise
   * @throws ExecException
   */
  public abstract boolean reportBindings() throws ExecException;
  
  /*
   * Setters
   */
  
  public void setPatternExecContext(PatternExecContext pc)
  {
    this.pc = pc;  
  }
  
  public void setInTupleStorageAlloc(IAllocator<ITuplePtr> inAlloc)
  {
    this.inTupleStorageAlloc = inAlloc;
  }
  
  public void setTupleStorageAlloc(IAllocator<ITuplePtr> outAlloc)
  {
    this.tupleStorageAlloc = outAlloc;
  }
  
  public void setOutputQueue(Queue outQueue)
  {
    this.outputQueue = outQueue;  
  }
  
  public void setHasAggrs(boolean[] hasAggrs)
  {
    this.hasAggrs = hasAggrs;
  }

  public void setIncrEvals(IAEval[] incrEvals)
  {
    this.incrEvals = incrEvals;
  }

  public void setInitEvals(IAEval[] initEvals)
  {
    this.initEvals = initEvals;
  }

  public void setReleaseEval(IAEval releaseEval)
  {
    this.releaseEval = releaseEval;
  }

  public void setMeasureEval(IAEval measureEval)
  {
    this.measureEval = measureEval;
  }
  
  public void setDefs(IBEval[] defs)
  {
    this.defs = defs;
  }

  public void setNumUDA(int numUDA)
  {
    this.numUDA = numUDA;
  }

  public void setAggrPos(int aggrPos)
  {
    this.aggrPos = aggrPos;
  }

  public void setSubsetPos(int[][] subsetPos)
  {
    this.subsetPos = subsetPos;
  }

  public void setNumXmlAgg(int numXmlAgg)
  {
    this.numXmlAgg = numXmlAgg;
  }

  public void setReleaseIndexEval(IAEval releaseIndexEval)
  {
    this.releaseIndexEval = releaseIndexEval;
  }

  public void setNfaRelatedVars(NFA nfa)
  {
    this.nfa  = nfa;
    isFinal   = nfa.getFinalStates();
    alphEval  = new BitSet(nfa.getNumAlphabets());
    alphMatch = new BitSet(nfa.getNumAlphabets());
    specialPattern = nfa.isSpecialPattern();
  }
  
  public void setDurationValue(long durationVal)
  {
    this.durationValue = durationVal;
  }
  
  /**
   * Returns if the current binding is growable further or not
   * Growable means if on further any input, if this binding develops to
   * a final state binding
   * @param pos position
   */
  protected boolean isGrowable(int pos)
  {
    return nfa.hasTransitionToFinal(pos);
  }
  
  protected void releaseAllocHandlers(Binding binding) throws ExecException 
  {
    if(numUDA > 0)
    {
      pc.evalContext.bind(binding.getTuple(aggrPos),
        IEvalContext.AGGR_ROLE);
      releaseEval.eval(pc.evalContext);
    }
  }
  
  protected void releaseIndex(Binding binding) throws ExecException
  {
    if(numXmlAgg > 0)
    {
      assert releaseIndexEval != null;
      pc.evalContext.bind(binding.getTuple(aggrPos), 
                       IEvalContext.AGGR_ROLE);
      releaseIndexEval.eval(pc.evalContext);
    }
  }
  
  protected void  applyInitIncrEvals(int inputIndex, Binding binding)
  throws ExecException
  {
    ITuplePtr[] elems = binding.getElems();
    pc.evalContext.bind(elems,pc.bindRole,elems.length,false,pc.nullInputTuple);  
    //check the initincr flag to decide whether to perform init or incr eval
    if(binding.getInitIncrFlag(inputIndex))
      incrEvals[inputIndex].eval(pc.evalContext);
    else
    {
      binding.setInitIncrFlag(inputIndex);
      initEvals[inputIndex].eval(pc.evalContext);
    }
  }
  
  protected void copyAggrTuple(Binding src, Binding dest) throws ExecException
  {
    //position of aggr tuple inside a binding
    int pos = src.getElems().length - 1;
    
    ITuplePtr srcptr = src.getTuple(pos);
    ITuplePtr destptr = dest.getTuple(pos);
    ITuple srct = srcptr.pinTuple(IPinnable.READ);
    ITuple destt = destptr.pinTuple(IPinnable.WRITE);
    destt.copy(srct);
    destptr.unpinTuple();
    srcptr.unpinTuple();
  }
  
  /**
   * Removes a partition if empty (activeList is empty).
   * If PREV is used (maxPrevIndex > 0) then partition is not deleted since
   * PartitionContext stores the PREV tuples specific to that partition.
   * 
   * @throws ExecException
   */
  protected void removeEmptyPartn() throws ExecException
  {
    if(PatternExecContext.trackStats)
      pc.tempTime = System.currentTimeMillis();
    
    pc.bindSyn.removeEmptyPartns();
 
    if(PatternExecContext.trackStats)
    {
      pc.tempTime = System.currentTimeMillis() - pc.tempTime;
      s.stats.addRemovePartnTime(pc.tempTime);
    }
  }
  
  /**
   * Output a single match. Used by all reporting functions.
   * 
   * @return true if processing should quit false otherwise
   * @throws ExecException
   */
  protected boolean handleMeasures() throws ExecException
  {
    if(PatternExecContext.trackStats)
      pc.tempTime = System.currentTimeMillis();
    // Allocate an output tuple
    s.outputTuple = tupleStorageAlloc.allocate();
   
    // Apply MeasureEval (MEASURES clause) on outputBinding to populate output tuple 
    ITuplePtr[] elems = s.outputBinding.getElems();
    int i = elems.length - 1;
    pc.evalContext.bind(elems,pc.bindRole,i,false,pc.nullInputTuple);
    //copy aggr tuple without pin
    if(elems[i] != null)
      pc.evalContext.bind(elems[i], pc.bindRole + i);
    
    pc.evalContext.bind(s.outputTuple, IEvalContext.NEW_OUTPUT_ROLE);
    measureEval.eval(pc.evalContext);
    
    //Set different properties for the outputElement
    assert s.outputTuple != null;
    s.outputElement.setTuple(s.outputTuple);
    s.outputElement.setTs(s.outputBinding.getMatchedTs());
    s.outputElement.setKind(QueueElement.Kind.E_PLUS);
    s.outputElement.setTotalOrderingGuarantee(s.nextOutputOrderingFlag);
    s.nextOutputOrderingFlag = false;
    
    // Update last output ts
    s.lastOutputTs = s.outputBinding.getMatchedTs();
    
    if (outputQueue.isFull())
    {
      if(PatternExecContext.trackStats)
      {
        pc.tempTime = System.currentTimeMillis() - pc.tempTime;
        s.stats.addBindingReportTime(pc.tempTime);
        pc.tempTime = System.currentTimeMillis();
      }
      return true;
    }
    
    outputQueue.enqueue(s.outputElement);
    
    s.stats.incrNumOutputs();
    
    //Release the references specific to the binding
    releaseIndex(s.outputBinding);
    releaseAllocHandlers(s.outputBinding);
    s.outputBinding.decrRef(inTupleStorageAlloc);

    if(PatternExecContext.trackStats)
    {
      pc.tempTime = System.currentTimeMillis() - pc.tempTime;
      s.stats.addBindingReportTime(pc.tempTime);
    }
    return false;
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeBoolean(this.foundMatch);
    out.writeObject(this.readyToOutputBindings);
    out.writeLong(this.minActiveIndex);
    out.writeLong(this.minTs);    
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    this.foundMatch = in.readBoolean();
    this.readyToOutputBindings = (PriorityQueue<Binding>) in.readObject();
    this.minActiveIndex = in.readLong();
    this.minTs = in.readLong();
  } 

  public void copyFrom(PatternProcessor otherProc)
  {
    this.foundMatch = otherProc.foundMatch;
    this.readyToOutputBindings = otherProc.readyToOutputBindings;
    this.minActiveIndex = otherProc.minActiveIndex;
    this.minTs = otherProc.minTs;
  }
}
