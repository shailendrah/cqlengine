/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/NormalProcessor.java /main/2 2011/02/07 03:36:26 sborah Exp $ */

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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/NormalProcessor.java st_pcbpel_udeshmuk_reorg_and_variable_nonevent/2 2009/09/03 04:13:33 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.pattern;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.ListIterator;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.operators.PatternStrmClassBState;
import oracle.cep.util.NFA;
import oracle.cep.util.Transition;

/**
 * Super-class for normal pattern processing.
 * @author udeshmuk
 */

public abstract class NormalProcessor extends PatternProcessor
{
  /** Empty Constructor */
  public NormalProcessor()
  {
    super();
  }
  
  public NormalProcessor(PatternStrmClassBState s)
  {
    super(s);
  }
  
  protected void setTargetTime(Binding b, long inputTs) 
    throws ExecException
  {
    b.setTargetTime(Constants.UNDEFINED_TARGET_TIME);
  }

  @Override
  protected boolean processBinding(Binding activeBind) throws ExecException
  {
    int                      curState;
    boolean                  del_remaining;
    Transition               trans;
    ArrayList<Transition>    transList;
    ListIterator<Transition> transIter;
    
    curState       = activeBind.getCurState();
    del_remaining  = false;
    
    alphEval.clear();
    alphMatch.clear();
    
    if(shouldAllTransitionsBeApplied())
      transList = nfa.getAllTransitions(curState);
    else
      transList = nfa.getOrderedTransitions(curState);
    
    transIter = transList.listIterator();
    
    while(transIter.hasNext())
    {
      trans = transIter.next();
      del_remaining = applyAlph(activeBind, trans);
      
      if(del_remaining)
        return true;
    }
    
    if(!s.tempConsumed)
      s.activeTemp.decrRef(inTupleStorageAlloc);
    
    return false;
  }
  
  @Override
  public boolean applyAlph(Binding activeBind, Transition trans) throws ExecException
  {
    int         k;
    int         step;
    int         alphIndex;
    int         curState;
    int         pos;
    int[]       posArr;
    boolean     del_remaining;
    
    ITuplePtr[] elems;
   
    foundMatch    = false; 
    alphIndex     = trans.getAlphId();
    curState      = activeBind.getCurState();
    del_remaining = false;
    posArr        = null;
    
    if((!alphEval.get(alphIndex)) || alphMatch.get(alphIndex))
    {
      alphEval.set(alphIndex);
      if(s.tempConsumed)
      {
        s.activeTemp   = pc.bindSyn.createBinding();
        s.tempConsumed = false;
        //copy attrs increases the ref count
        activeBind.copyAttrs(s.activeTemp, inTupleStorageAlloc, false);
      }
      else
      {
        //decrement refcount for tuples in temp binding whose
        //refcount was incremented by previous coppyAttrs operation
        releaseIndex(s.activeTemp);
        releaseAllocHandlers(s.activeTemp);
        s.activeTemp.decrRef(inTupleStorageAlloc);
        s.activeTemp.getInitIncrFlag().clear();
        //copy attrs increases the ref count
        activeBind.copyAttrs(s.activeTemp, inTupleStorageAlloc, true);
      }
      
      if(pc.aggrsPresent)
      {
        activeBind.copyInitIncrFlag(s.activeTemp);
        copyAggrTuple(activeBind, s.activeTemp);
      }
      
      inTupleStorageAlloc.release(s.activeTemp.getTuple(alphIndex));
      s.activeTemp.setCurrTuple(s.inputTuple, alphIndex, inTupleStorageAlloc);
      posArr = subsetPos[alphIndex];
      
      if(posArr != null)
      {
        for(int i = 0; i < posArr.length; i++)
        {
          pos = posArr[i];
          inTupleStorageAlloc.release(s.activeTemp.getTuple(pos));
          s.activeTemp.setCurrTuple(s.inputTuple, pos, inTupleStorageAlloc);
          if(hasAggrs[pos])
            applyInitIncrEvals(pos, s.activeTemp);
        }
      }
      
      if(hasAggrs[alphIndex])
        applyInitIncrEvals(alphIndex, s.activeTemp);   

      elems = s.activeTemp.getElems();
      k = elems.length - 1;
      pc.evalContext.bind(elems,pc.bindRole,k,false,pc.nullInputTuple); 
      if(elems[k] != null)
        pc.evalContext.bind(elems[k], pc.bindRole + k);
     
      //definition corresponding to undefined correlation name is null
      //using the fact that || operation is greedy:
      //if eval.get(x) returns true, defs is not evaluated
      if((defs[alphIndex] == null) || (alphMatch.get(alphIndex)) ||
         (defs[alphIndex].eval(pc.evalContext)))
      {
        alphMatch.set(alphIndex);
        step = trans.getDestState();
        assert step != NFA.UNDEFINED_STATE;
       
        if(curState == 0) 
          s.activeTemp.setStartIndex(s.sequence);
        else
          s.activeTemp.setStartIndex(activeBind.getStartIndex());
        
        //since normal processing
        s.activeTemp.setTargetTime(Constants.UNDEFINED_TARGET_TIME);
        
        s.activeTemp.setCurState(step);
        s.tempConsumed = true;
        
        if(isFinal[step])
        {
          if(specialPattern)
            foundMatch = true; //by defn of special pattern, binding won't grow
          s.uniqueFinal = s.activeTemp;
          s.activeTemp.setMatchedTs(s.inputTs);
          
          //add the new entry into final list at the end
          if(!specialPattern)
            pc.bindSyn.addEndOfFinalList(s.activeTemp);

          //the new transition is both in final and active list
          //non event final bindings can never be growable
          if(isGrowable(step))
          {
            Binding copyBind = pc.bindSyn.createBinding();
            s.activeTemp.copyAttrs(copyBind, inTupleStorageAlloc, false);
            if(pc.aggrsPresent)
            {
              s.activeTemp.copyInitIncrFlag(copyBind);
              copyAggrTuple(s.activeTemp, copyBind);
            }
            copyBind.setStartIndex(s.activeTemp.getStartIndex());
            copyBind.setCurState(s.activeTemp.getCurState());
            copyBind.setMatchedTs(s.inputTs);
            copyBind.setTargetTime(s.activeTemp.getTargetTime());
            pc.bindSyn.addToActiveBindings(s.activeIter, copyBind);
          }
          
          if(shouldRemainingBindingsBeDeleted())
            del_remaining = true;
        }
        else
        { //!isFinal[step]
          pc.bindSyn.addToActiveBindings(s.activeIter, s.activeTemp);
        }
      }
    }
    return del_remaining;
  }
  
  /**
   * Reporting logic specific to Special patterns. Covers all cases.
   * 
   * @return true if processing should quit false otherwise
   * @throws ExecException
   */
  protected boolean reportSpecialPattern() throws ExecException
  {
    boolean done = false;
    //nonEvent will be false here since specialPattern
    if(foundMatch)
    {
      s.outputBinding = s.activeTemp;
       /*e.g. i/p has 1000 10
                      1000 15
         both of which match A, then there will be two matches with same ts
         we don't have unsurelist so just copy the input flag */
      s.nextOutputOrderingFlag = s.lastInputTotalOrderingFlag;
      done = handleMeasures();
      foundMatch = false;
    }
    return done;
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    super.writeExternal(out);
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    super.readExternal(in);
  }
  
  @Override
  public void copyFrom(PatternProcessor otherProc)
  {
    super.copyFrom(otherProc);
  }
}