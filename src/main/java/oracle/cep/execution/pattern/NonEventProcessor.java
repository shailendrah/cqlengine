/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/NonEventProcessor.java /main/4 2013/09/18 09:06:19 udeshmuk Exp $ */

/* Copyright (c) 2009, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/17/13 - add check for null in durationExpr
    udeshmuk    02/02/12 - add error for durationexpr <=0
    sborah      01/23/11 - change to eval(evalContext)
    udeshmuk    08/11/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/NonEventProcessor.java /main/4 2013/09/18 09:06:19 udeshmuk Exp $
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

import oracle.cep.common.TimeUnit;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.operators.PatternStrmClassBState;
import oracle.cep.util.NFA;
import oracle.cep.util.Transition;

/**
 * Super-class for non-event pattern processing (Fixed and Variable both).
 * @author udeshmuk
 */
public abstract class NonEventProcessor extends PatternProcessor
{
  /** does the duration clause contain expr */
  protected boolean             isDurationExpr;
  
  /** eval to compute duration value if it is given as expr */
  protected IAEval              durationEval; 
  
  /** role which will contain the evaluated duration expr */
  protected int                 durationRole; 
  
  /** pos in the tuple at the specified role where duration expr will get evaluated */
  protected int                 durationPos; 
  
  /** timeunit for duration clause expression, null if not applicable */
  protected TimeUnit            durationUnit;
  
  /** alphabet Index for the duration symbol */
  protected int                 durationSymAlphIndex; 
  
  /** flag indicating recurring non event detection */
  protected boolean             isRecurringNonEvent; 
  
  /** temp variable. used in non-event with partnattrs */
  protected ActiveItem          currentActiveItem = null; 
   
  /** temp variable. used in non-event with partnattrs */
  protected TreeSet<ActiveItem> activeItems = null; 

  /** override any flag and apply S0 transition */
  protected boolean             applyS0trans = false;
  
  /** Set to TRUE when processing partition other than the current input's
   *  partition. Used during Non-event processing */
  protected boolean             processOtherPartitions = false; 
  
  /** Empty Constructor for HA*/
  public NonEventProcessor()
  {}
  
  public NonEventProcessor(PatternStrmClassBState s)
  {
    super(s);
  }
  
  /**
   * Add the unsure binding to the collection of unsure bindings.
   * @param b - unsure binding to be added
   */
  protected abstract void addToUnsureList(Binding b) throws ExecException;
  
  /*
   * Setters
   */
  
  public void setDurationExpr(boolean isDurationExpr)
  {
    this.isDurationExpr = isDurationExpr;
  }

  public void setDurationEval(IAEval durationEval)
  {
    this.durationEval = durationEval;
  }

  public void setDurationRole(int durationRole)
  {
    this.durationRole = durationRole;
  }

  public void setDurationPos(int durationPos)
  {
    this.durationPos = durationPos;
  }

  public void setDurationUnit(TimeUnit durationUnit)
  {
    this.durationUnit = durationUnit;
  }

  public void setDurationSymAlphIndex(int durationSymAlphIndex)
  {
    this.durationSymAlphIndex = durationSymAlphIndex;
  }

  public void setRecurringNonEvent(boolean isRecurringNonEvent)
  {
    this.isRecurringNonEvent = isRecurringNonEvent;
  }
  
  protected void setTargetTime(Binding b, long inputTs) 
    throws ExecException
  {
    if(isDurationExpr)
    {
      durationValue = getDurationExprValue();
    }
    b.setTargetTime(inputTs + durationValue);
  }
  
  /**
   * Computes the duration value for variable duration case
   * @return duration value
   * @throws ExecException
   */
  private long getDurationExprValue() throws ExecException
  {
    assert isDurationExpr == true && durationEval != null;
    durationEval.eval(pc.evalContext);
    ITuple resultTuple = pc.evalContext.getRoles()[durationRole];
    if(resultTuple.isAttrNull(durationPos))
      throw new ExecException(ExecutionError.INVALID_DURATION_VALUE,
                              new Object[]{"null"});
    //get the evaluated value and convert to nanosec
    long l = resultTuple.iValueGet(durationPos);
    l = oracle.cep.common.RangeConverter.interpRange(l, durationUnit);
    if(l <= 0)
      throw new ExecException(ExecutionError.INVALID_DURATION_VALUE,
                              new Object[]{l});
    return l;
  }
  
  //returns if the given transList has # transition
  protected boolean hasHashTrans(ArrayList<Transition> transList)
  {
    ListIterator<Transition> transIter;
    transIter = transList.listIterator();
    while(transIter.hasNext())
    {
      if(transIter.next().getAlphId() == durationSymAlphIndex)
        return true;
    }
    return false;
  }
  
  protected Transition getHashTrans(ArrayList<Transition> transList)
  {
    ListIterator<Transition> transIter;
    Transition               trans;
    
    transIter = transList.listIterator();
    while(transIter.hasNext())
    {
      trans = transIter.next();
      if(trans.getAlphId() == durationSymAlphIndex)
        return trans;
    }
    return null;
  }

  @Override
  public boolean applyAlph(Binding activeBind, Transition trans)
    throws ExecException
  {
    int         k;
    int         step;
    int         alphIndex;
    int         curState;
    int         pos;
    long        numOutput;
    int[]       posArr;
    boolean     del_remaining;
    //if its # transition
    boolean     hashTrans;
    ITuplePtr[] elems;
   
    alphIndex     = trans.getAlphId();
    curState      = activeBind.getCurState();
    del_remaining = false;
    hashTrans     = (alphIndex == durationSymAlphIndex);
    posArr        = null;
    
    if(hashTrans || (!alphEval.get(alphIndex)) || alphMatch.get(alphIndex))
    {
      if(!hashTrans)
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
      
      if(!hashTrans)
      {
        inTupleStorageAlloc.release(s.activeTemp.getTuple(alphIndex));
        s.activeTemp.setCurrTuple(s.inputTuple, alphIndex, inTupleStorageAlloc);
        posArr = subsetPos[alphIndex];
      }
      
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
      
      if(!hashTrans)
      {
        if(hasAggrs[alphIndex])
          applyInitIncrEvals(alphIndex, s.activeTemp);
      }
      
      if(!hashTrans)
      {
        elems = s.activeTemp.getElems();
        k = elems.length - 1;
        pc.evalContext.bind(elems,pc.bindRole,k,false,pc.nullInputTuple); 
        if(elems[k] != null)
          pc.evalContext.bind(elems[k], pc.bindRole + k);
      }
    
      //definition corresponding to undefined correlation name is null
      //using the fact that || operation is greedy:
      //if eval.get(x) returns true, defs is not evaluated
      if(hashTrans || (defs[alphIndex] == null) || (alphMatch.get(alphIndex)) 
          || (defs[alphIndex].eval(pc.evalContext)))
      {
        if(!hashTrans)
          alphMatch.set(alphIndex);
        step = trans.getDestState();
        assert step != NFA.UNDEFINED_STATE;
        
        /* For recurring non event detection case, at the application of
         * "# transition", some additional processing is required.
         * 1. First calculate the number of bindings to be output because
         *    the difference between the IT and TT may be more than 
         *    (1 * duration) and more than one binding needs to be output.
         * 2. Output all these bindings
         * 3. Apply the current binding(before applying any final transition
         *    logic) on the current input(with all possible correlation names
         *    it matches to except #). On such application, add the bindings to
         *    the active list only if they are still active.
         * 4. On all these new bindings (all those being added to the active
         *    list), make the TT the next possible target time further to IT.
         */
        if(hashTrans && isRecurringNonEvent && isFinal[step])
        {
          /* numOutput gives the number of additional output bindings that need
          to be created. create new binding for each of the numOutput bindings,
          copy contents from activeTemp to the newly created binding and add it
          to the Final List. */
          numOutput = ((s.inputTs - activeBind.getTargetTime()) / durationValue);
          
          for(int i = 0; i < numOutput; i++)
          {
            s.nonEventTemp1 = pc.bindSyn.createBinding();
            s.activeTemp.copyAttrs(s.nonEventTemp1, inTupleStorageAlloc, true);
            if(pc.aggrsPresent)
            {
              s.activeTemp.copyInitIncrFlag(s.nonEventTemp1);
              copyAggrTuple(s.activeTemp, s.nonEventTemp1);
            }
            s.nonEventTemp1.setStartIndex(activeBind.getStartIndex());
            s.nonEventTemp1.setTargetTime(activeBind.getTargetTime() + (i * durationValue));
            s.nonEventTemp1.setCurState(step);
            s.nonEventTemp1.setMatchedTs(s.nonEventTemp1.getTargetTime());
          
            addToUnsureList(s.nonEventTemp1);
          }
          
          /* create a binding and add it to the activeIter 
           * with new target time. */
          s.nonEventTemp1 = pc.bindSyn.createBinding();
          s.activeTemp.copyAttrs(s.nonEventTemp1, inTupleStorageAlloc, true);
          if(pc.aggrsPresent)
          {
            s.activeTemp.copyInitIncrFlag(s.nonEventTemp1);
            copyAggrTuple(s.activeTemp, s.nonEventTemp1);
          }
          s.nonEventTemp1.setStartIndex(activeBind.getStartIndex());
          s.nonEventTemp1.setTargetTime(activeBind.getTargetTime() + 
                ((numOutput + 1) * durationValue));
          s.nonEventTemp1.setCurState(activeBind.getCurState());
          
          /* add activeTemp to final list after setting required fields */
          s.nonEventTemp2 = pc.bindSyn.createBinding();
          s.activeTemp.copyAttrs(s.nonEventTemp2, inTupleStorageAlloc, true);
          if(pc.aggrsPresent)
          {
            s.activeTemp.copyInitIncrFlag(s.nonEventTemp2);
            copyAggrTuple(s.activeTemp, s.nonEventTemp2);
          }
          s.nonEventTemp2.setStartIndex(activeBind.getStartIndex());
          s.nonEventTemp2.setTargetTime(activeBind.getTargetTime() + 
              (numOutput * durationValue));
          s.nonEventTemp2.setCurState(step);
          s.nonEventTemp2.setMatchedTs(s.nonEventTemp2.getTargetTime());
          
          //pc.bindSyn.addEndOfFinalList(s.nonEventTemp2);
          addToUnsureList(s.nonEventTemp2);
          
          if(!pc.isHeartBeat && !processOtherPartitions)
          {
            ArrayList<Transition> tempTrans = nfa.getAllTransitions(s.nonEventTemp1.getCurState());
            ListIterator<Transition> tempTransIter = tempTrans.listIterator();
            Transition tempTrans1;
            while(tempTransIter.hasNext())
            {
              tempTrans1 = tempTransIter.next();
              if(tempTrans1.getAlphId() == durationSymAlphIndex)
                continue;
              applyAlph(s.nonEventTemp1, tempTrans1);
            }
          }
          else
          { 
            if(pc.hasPartnAttrs)
            {
              pc.bindSyn.addActiveItem(s.nonEventTemp1);
            }
            pc.bindSyn.addToActiveBindings(s.activeIter, s.nonEventTemp1);
          }
          
          s.tempConsumed = true;
        }
        else
        {
          if(curState == 0) 
            s.activeTemp.setStartIndex(s.sequence);
          else
            s.activeTemp.setStartIndex(activeBind.getStartIndex());
          
          //non-event so call the method getTargetTime
          s.activeTemp.setTargetTime(activeBind.getTargetTime());
          
          s.activeTemp.setCurState(step);
          s.tempConsumed = true;
          if(isFinal[step])
          {
            s.uniqueFinal = s.activeTemp;
            if(hashTrans)
              s.activeTemp.setMatchedTs(s.activeTemp.getTargetTime());
            else
              s.activeTemp.setMatchedTs(s.inputTs);
            
            //the new transition cannot be both in final and active list since non-event.
            //add the new entry into final list at the end
            addToUnsureList(s.activeTemp);
              
            if(shouldRemainingBindingsBeDeleted())
              del_remaining = true;
          }
          else
          { //!isFinal[step]
            if(pc.hasPartnAttrs)
            { //add the corresponding activeItem
              pc.bindSyn.addActiveItem(s.activeTemp);
            } 
            pc.bindSyn.addToActiveBindings(s.activeIter, s.activeTemp);
          }
        }
      }
    }
    return del_remaining;
  }

  @Override
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
    //flag indicating whether # transition to be applied
    boolean                  applyHashTrans;
    
    TT             = activeBind.getTargetTime();
    IT             = s.inputTs;
    curState       = activeBind.getCurState();
    applyHashTrans = false;
    del_remaining  = false;
    
    if((pc.isHeartBeat || processOtherPartitions) && (TT > IT))
      return del_remaining;
    
    alphEval.clear();
    alphMatch.clear();
    
    //get all the transitions just excluding FINAL for ALL MATCHES
    if(shouldAllTransitionsBeApplied())
      transList = nfa.getAllTransitions(curState);
    else
      transList = nfa.getOrderedTransitions(curState);
    
    transIter = transList.listIterator();
   
    if(hasHashTrans(transList))
    {
      if(TT > IT)
        applyHashTrans = false;
      else
        applyHashTrans = true;
    }
    
    if(TT <= IT)
    {
      //apply # transition only if it exists for this state. Else, the
      //binding anyway gets deleted from the active list
      if(hasHashTrans(transList))
        del_remaining = applyAlph(activeBind, getHashTrans(transList));
      
      //applyS0trans is used only in case of SKIP PAST LAST ROW -non event case

      if(!pc.isHeartBeat && !processOtherPartitions)
        applyS0trans = true;
      return del_remaining;
    }
    
    assert !applyHashTrans : "applyHashTrans should be false here";
    while(transIter.hasNext())
    {
      trans = transIter.next();
      if(trans.getAlphId() == durationSymAlphIndex)
        continue;
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
    out.writeObject(currentActiveItem);
    out.writeObject(activeItems);
    out.writeBoolean(processOtherPartitions);
    out.writeBoolean(applyS0trans);
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    super.readExternal(in);
    currentActiveItem = (ActiveItem) in.readObject();
    activeItems = (TreeSet<ActiveItem>) in.readObject();
    processOtherPartitions = in.readBoolean();
    applyS0trans = in.readBoolean();
  }
  
  @Override
  public void copyFrom(PatternProcessor other)
  {    
    super.copyFrom(other);
    NonEventProcessor otherProc = (NonEventProcessor) other;
    this.currentActiveItem = otherProc.currentActiveItem;
    this.activeItems = otherProc.activeItems;
    this.applyS0trans = otherProc.applyS0trans;
    this.processOtherPartitions = otherProc.processOtherPartitions;
  }
}
