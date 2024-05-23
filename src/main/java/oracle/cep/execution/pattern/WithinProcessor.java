/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/WithinProcessor.java /main/2 2011/02/07 03:36:26 sborah Exp $ */

/* Copyright (c) 2010, 2011, Oracle and/or its affiliates. 
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
    udeshmuk    10/15/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/WithinProcessor.java st_pcbpel_udeshmuk_reorg_and_variable_nonevent/2 2010/12/21 05:25:28 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.pattern;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.TreeSet;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.operators.PatternStrmClassBState;
import oracle.cep.util.NFA;
import oracle.cep.util.Transition;

public abstract class WithinProcessor extends PatternProcessor
{
  /**
   * Used to indicate when partition other than current input partition is
   * being processed
   */
  protected boolean             processOtherPartitions = false;
  
   /** temp variable. used in non-event with partnattrs */
  protected ActiveItem          currentActiveItem = null; 
   
  /** temp variable. used in non-event with partnattrs */
  protected TreeSet<ActiveItem> activeItems = null; 
  
  /** override any flag and apply S0 transition */
  protected boolean             applyS0trans = false;
  
  /** Empty Constructor for HA*/
  public WithinProcessor()
  {
    super();
  }
  
  public WithinProcessor(PatternStrmClassBState s)
  {
    super(s);
  }
  
  @Override
  protected void setTargetTime(Binding b, long inputTs) throws ExecException
  {
    b.setTargetTime(inputTs + durationValue);    
  }
  
  /**
   * Add the unsure binding to the collection of unsure bindings.
   * @param b - unsure binding to be added
   */
  protected abstract void addToUnsureList(Binding b) throws ExecException;
  
  public boolean applyAlph(Binding activeBind, Transition trans)
    throws ExecException
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
        
        // within processing - propagate target time
        s.activeTemp.setTargetTime(activeBind.getTargetTime());
        
        s.activeTemp.setCurState(step);
        s.tempConsumed = true;
        
        if(isFinal[step])
        {
          s.uniqueFinal = s.activeTemp;
          s.activeTemp.setMatchedTs(s.inputTs);
          
          //add the new entry into final list at the end
          //pc.bindSyn.addEndOfFinalList(s.activeTemp);
          addToUnsureList(s.activeTemp);

          //the new transition is both in final and active list
          //WITHIN case final bindings can be growable
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
            //WITHIN processing requires activeItems 
            if(pc.hasPartnAttrs)
              pc.bindSyn.addActiveItem(copyBind);
            pc.bindSyn.addToActiveBindings(s.activeIter, copyBind);
          }
          
          if(shouldRemainingBindingsBeDeleted())
            del_remaining = true;
        }
        else
        { //!isFinal[step]
          //WITHIN processing requires activeItems
          if(pc.hasPartnAttrs)
            pc.bindSyn.addActiveItem(s.activeTemp);
          pc.bindSyn.addToActiveBindings(s.activeIter, s.activeTemp);
        }
      }
    }
    return del_remaining;
  }
  
  public boolean processBinding(Binding activeBind) throws ExecException
  {
    int                      curState;
    boolean                  del_remaining;
    Transition               trans;
    ArrayList<Transition>    transList;
    ListIterator<Transition> transIter;
    //TT - Target Time of the binding for a non event case
    long                     TT;
    //IT - Input Time of the current input element or heartbeat
    long                     IT;
    
    TT             = activeBind.getTargetTime();
    IT             = s.inputTs;
    curState       = activeBind.getCurState();
    del_remaining  = false;
    
    if((pc.isHeartBeat || processOtherPartitions) && (TT > IT))
      return del_remaining;
    
    //Handle within case.
    //Here TT represents bindingStartTime + within duration.
    if((pc.isWithin && (TT <= IT)) || (pc.isWithinInclusive && (TT < IT))) 
    {
      //this active binding need not be processed further as it violates 
      //Within clause condition
      return del_remaining;
    }
    
    alphEval.clear();
    alphMatch.clear();
    
    //get all the transitions just excluding FINAL for ALL MATCHES
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
  public void writeExternal(ObjectOutput out) throws IOException
  {
    super.writeExternal(out);
    out.writeBoolean(processOtherPartitions);
    out.writeObject(currentActiveItem);
    out.writeObject(activeItems);
    out.writeBoolean(applyS0trans);
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    super.readExternal(in);
    this.processOtherPartitions = in.readBoolean();
    this.currentActiveItem = (ActiveItem) in.readObject();
    this.activeItems = (TreeSet<ActiveItem>) in.readObject();
    this.applyS0trans = in.readBoolean();
  }
  
  public void copyFrom(PatternProcessor otherProc)
  {
    super.copyFrom(otherProc);
    WithinProcessor other = (WithinProcessor) otherProc;
    this.processOtherPartitions = other.processOtherPartitions;
    this.activeItems = other.activeItems;
    this.currentActiveItem = other.currentActiveItem;
    this.applyS0trans = other.applyS0trans;
  }
}
