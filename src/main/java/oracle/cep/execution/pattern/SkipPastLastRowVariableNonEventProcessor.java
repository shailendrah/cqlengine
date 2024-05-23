/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/SkipPastLastRowVariableNonEventProcessor.java /main/2 2013/01/04 02:19:22 udeshmuk Exp $ */

/* Copyright (c) 2009, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    01/03/13 - XbranchMerge udeshmuk_bug-14774142_ps6 from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    12/11/12 - set the ordering guarantee flag correctly
    udeshmuk    08/26/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/SkipPastLastRowVariableNonEventProcessor.java /main/2 2013/01/04 02:19:22 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.pattern;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.execution.ExecException;
import oracle.cep.execution.operators.PatternStrmClassBState;

/**
 * Processor for SKIP PAST LAST ROW - Variable duration non-event case.
 * @author udeshmuk
 */
public class SkipPastLastRowVariableNonEventProcessor extends NonEventProcessor
{
  /**
   * Used to keep reference to the longest unsure binding out of the
   * possibly many bindings within a partition which can become unsure
   * on receiving current tuple
   */
  private Binding unsureBind = null;
  
  public SkipPastLastRowVariableNonEventProcessor()
  {super();}
  
  public SkipPastLastRowVariableNonEventProcessor(PatternStrmClassBState s)
  {
    super(s);
  }
    
  @Override
  protected boolean shouldAllTransitionsBeApplied()
  {
    return false;
  }

  @Override
  protected boolean shouldRemainingBindingsBeDeleted()
  {
    return false;
  }
  
  @Override
  protected void processTuple() throws ExecException
  {
    Binding cur = null;
    Binding activeBind = null;
    boolean activeBindRemaining = false;
    s.tempConsumed = true;
    applyS0trans   = false;
        
    if(PatternExecContext.trackStats)
      pc.tempTime = System.currentTimeMillis();
    
    /* common processing for everyone.
     * iterator over current active set of bindings that are maintained in 
     * target time order.
     */
    
    s.activeIter = pc.bindSyn.getIterator();  
    foundMatch   = false;
    unsureBind   = null;
    
    while(s.activeIter.hasNext())
    {
      activeBind = s.activeIter.next();
      //do not process the binding or remove it from the list when
      //input is a heartbeat and Target Time > Input Time
      if(activeBind.getTargetTime() > s.inputTs && 
         (pc.isHeartBeat || processOtherPartitions))
      {
        activeBindRemaining = true;
        break;
      }
      
      s.activeIter.remove();
      
      //remove the corresponding activeItem              
      if(pc.hasPartnAttrs)
        pc.bindSyn.removeActiveItem(activeBind);            
      
      //process the active binding if 
      //1. An unsure binding has not been found yet
      //2. An unsure binding is found but the current active binding has 
      //   started earlier than the unsure binding(unsureBind) so far.
      //unsureBind contains a reference to the unsure binding which has
      //the smallest startIndex among the possibly many bindings which can
      //become unsure at a certain input.
      if(!foundMatch || (foundMatch && 
         (activeBind.getStartIndex() < unsureBind.getStartIndex())))
      {
        processBinding(activeBind);
      }
      
      releaseAllocHandlers(activeBind);
      activeBind.decrRef(inTupleStorageAlloc);
    }
    
    if(foundMatch)
    {
      //add the unsureBind to unsure list, unsureItem will also be added, if needed
      pc.bindSyn.addEndOfFinalList(unsureBind);
      
      //set applyS0Trans appropriately based on the unsureBind
      //We would want to apply S0 trans only for normal tuple of SAME partition
      applyS0trans = ((unsureBind.getTargetTime() <= s.inputTs) && !pc.isHeartBeat
                      && !processOtherPartitions);
      
      //for the 'current tuple' partition processing this is not needed when 
      //'current tuple' is non hb tuple. This is because we iterate through
      //all the active bindings in the upper loop.
      if((processOtherPartitions || pc.isHeartBeat) && activeBindRemaining)
      {
        do
        {
          assert activeBind != null : "activeBind is null";
          if(activeBind.getStartIndex() >= unsureBind.getStartIndex())
          {
            releaseIndex(activeBind);
            releaseAllocHandlers(activeBind);
            activeBind.decrRef(inTupleStorageAlloc);
            s.activeIter.remove();
            //remove the corresponding activeItem              
            if(pc.hasPartnAttrs)
              pc.bindSyn.removeActiveItem(activeBind);
          }
          if(s.activeIter.hasNext())
            activeBind = s.activeIter.next();
          else
            break;
        } while(true);  
        
        activeBindRemaining = false;
      }
      
      //delete bindings from unsure list which are preferred less
      long uniFinStart = unsureBind.getStartIndex();
      
      s.unsureIter = pc.bindSyn.getUnsureIterator();
      while(s.unsureIter.hasNext())
      {
        cur = s.unsureIter.next();
        if(cur == unsureBind)
          continue;
        if(cur.getStartIndex() >= uniFinStart)
        {
          releaseIndex(cur);
          releaseAllocHandlers(cur);
          cur.decrRef(inTupleStorageAlloc);
          s.unsureIter.remove();
          //remove unsureItem
          if(pc.hasPartnAttrs)
            pc.bindSyn.removeUnsureItem(cur.getUnsureItem());
        }
      }
    }

    //Set minActiveIndex here.
    //This is because at this point any active binding which had 
    //startIndex >= unsureBind.startIndex is removed from activeList.
    //The ordering among the active bindings which have startIndex <
    //unsurBind.startIndex does not matter.
    //So the minActiveIndex set at this point would be correct.
    //Later a new active binding can get added due to bindingB0 (S0 trans).
    //This will have a higher startindex but can have TT < TT of an active
    //binding with startIndex < unsureBind.startIndex due to which it will be 
    //present before that binding in activelist. Using that as minActiveIndex
    //will be wrong as it will result in output of 'unsureBind'.
    /* Example
     *      c1, c2   PATTERN (AB*) DURATION c2
     * 1000 10, 15 - matches A
     * 3000 15, 5  - matches A, B
     * 6000 20, 25 - matches B
     * 9000 15, 3  - matches A, B 
     * 12000 15, 1 - matches A, B
     * h 16000
     */
    
    s.activeIter = pc.bindSyn.getIterator();
    if(s.activeIter.hasNext())
      minActiveIndex = s.activeIter.next().getStartIndex();
    else
      minActiveIndex = Long.MAX_VALUE;
        
    //add transitions from S0 only if 
    //1. applySotrans is set 
    //OR
    //2. match is not found and the current tuple belongs to the same partn 
    //and is not a heartbeat
    if((!foundMatch && !pc.isHeartBeat && !processOtherPartitions) || applyS0trans)
    {
      if(applyS0trans)
        applyS0trans = false;
      setTargetTime(s.bindingB0, s.inputTs);
      processBinding(s.bindingB0);
    }
         
    if(PatternExecContext.trackStats)
    {           
      pc.tempTime = System.currentTimeMillis() - pc.tempTime;
      s.stats.addCommonProcTime(pc.tempTime);
    }
  }

  @Override
  protected void addToUnsureList(Binding b) throws ExecException
  {
    if(unsureBind != null) 
    { //Some unsure binding was generated earlier for the current tuple & partn
      //Therefore compare with existing unsure and update if necessary.
      //unsureItem will be added when this unsureBind gets added to unsureList.
      assert foundMatch == true : "foundMatch false";
      if(unsureBind.getStartIndex() > b.getStartIndex())
      {
        //release the old binding
        releaseIndex(unsureBind);
        releaseAllocHandlers(unsureBind);
        unsureBind.decrRef(inTupleStorageAlloc);
        //update the unsureBind
        unsureBind = b;
      }
    }
    else
    {
      foundMatch = true;
      unsureBind = b;
    }
  }

  /**
   * Logic determining which unsure bindings to output
   * @throws ExecException
   */
  private void initReportBindings() throws ExecException
  {
    Binding cur = null;
    if(PatternExecContext.trackStats)
      pc.tempTime = System.currentTimeMillis();
   
    s.unsureIter = pc.bindSyn.getUnsureIterator();
    
    if(pc.hasPartnAttrs)
    {
      while(s.unsureIter.hasNext())
      {
        cur = s.unsureIter.next();
        if(cur.getStartIndex() < minActiveIndex)
        { //ensure longest match
          s.unsureIter.remove();
          pc.bindSyn.removeUnsureItem(cur.getUnsureItem());
          pc.bindSyn.addToReadyToOutputBindings(cur); 
        }
      }
      minTs = pc.bindSyn.getUnsureMinMatchedTs();
    }
  
    if(PatternExecContext.trackStats)
    {
      pc.tempTime = System.currentTimeMillis() - pc.tempTime;
      s.stats.addReadyToOutputListTime(pc.tempTime);
    }
  }
  
  private void doCommonProcessing() throws ExecException
  {
    processTuple();
    initReportBindings();
    if(pc.hasPartnAttrs)
      removeEmptyPartn();
  }
  
  private void doNonEventProcessing() throws ExecException
  {
    if(PatternExecContext.trackStats)
      pc.tempTime = System.currentTimeMillis();
        
    processOtherPartitions = true;
    /*
     * ActiveItem data-structure is used while implementing non-event
     * with partn attrs. An instance of activeItem is created for every
     * active binding. The lifespan of this instance is same as the 
     * lifespan of the active binding to which it corresponds. 
     * These ActiveItem instances are maintained in non-decreasing 
     * order of target time in a TreeSet data-structure. Purpose behind
     * this arrangement is to quickly identify the 'other' partitions
     * that need to be processed. Such partitions are those for which
     * there is at least one active binding whose target time < input time.
     */
    activeItems = pc.bindSyn.getActiveItems();
    assert activeItems != null : "activeItems not maintained";
    
    //Get an activeItem having TT <= IT.
    //Process the partition to which it belongs completely. 
    while(!activeItems.isEmpty() &&
      ((currentActiveItem = activeItems.first()).getReferredBinding().
                            getTargetTime() <= s.inputTs))
    {
      pc.bindSyn.setNonEventPartnContext(currentActiveItem.getOwnerPartnContext());
      doCommonProcessing();
    }
   
    //All 'needed' partitions are processed now.
    activeItems            = null;
    currentActiveItem      = null;
    processOtherPartitions = false;
    
    if(PatternExecContext.trackStats)
    {
      pc.tempTime = System.currentTimeMillis() - pc.tempTime;
      s.stats.addNonEventTime(pc.tempTime);
    }
  }
  
  public boolean reportBindings() throws ExecException
  {
    boolean done = false;
    Binding cur = null;
    Binding nextToCur = null;

    if(!pc.hasPartnAttrs)
    { 
      //As per the logic in processTuple, out of all the active bindings that 
      //expire at a particular tuple only one will be maintained in the
      //unsureBind and it is added to unsureList. 
      //If some active binding which started earlier than this exists then
      //this won't be output right now.
      //This way every binding that is output will have unique matchedTs. 
      //So piggybacking flag can be true.
      while(s.unsureIter.hasNext())
      {
        cur = s.unsureIter.next();
        if(cur.getStartIndex() < minActiveIndex)
        {
          //output
          s.outputBinding = cur;
          s.unsureIter.remove();
          s.nextOutputOrderingFlag = true;
          done = handleMeasures();
          if(done)
            return true;
        }
        else 
          break;
      }
    }
    else
    {
      readyToOutputBindings = pc.bindSyn.getReadyToOutputBindings();
      while(!readyToOutputBindings.isEmpty())
      {
        cur = readyToOutputBindings.peek();
        if(cur.getMatchedTs() < minTs)
        {
          //cur will be output so remove it from readyToOutputBindings
          readyToOutputBindings.poll();
          nextToCur = readyToOutputBindings.peek();
          if(nextToCur == null)
          { //cur is the last binding
            if(cur.getMatchedTs() == s.inputTs)
              s.nextOutputOrderingFlag = s.lastInputTotalOrderingFlag;
            else
              s.nextOutputOrderingFlag = true;
          }
          else
          {
            if(nextToCur.getMatchedTs() > cur.getMatchedTs())
            {
              if(nextToCur.getMatchedTs() < minTs)
              { //nextToCur will be output
                s.nextOutputOrderingFlag = true;
              }
              else //nextToCur won't be output so this is the last
              {
                if(cur.getMatchedTs() == s.inputTs)
                  s.nextOutputOrderingFlag = s.lastInputTotalOrderingFlag;
                else
                  s.nextOutputOrderingFlag = true;
              }
            }
            else //Here nextToCur will be output always 
              s.nextOutputOrderingFlag = false;
          }
          s.outputBinding = cur;
          done = handleMeasures();
          if(done)
            return true;
        }   
        else 
          break;
      }
    }
    return done;
  }
  
  @Override
  public void processPattern() throws ExecException
  { //TODO: Should a super class SkipPastLastRowNonEventProcessor be introduced?
    //      processPattern, doNonEventProcessing are identical for fixed and variable cases.
    //for heartbeats with partitions case just do non-event processing
    if(!pc.isHeartBeat || !pc.hasPartnAttrs)
      doCommonProcessing();
    if(pc.hasPartnAttrs)
      doNonEventProcessing();
    
  }
  
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    super.writeExternal(out);
    out.writeObject(unsureBind);
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    super.readExternal(in);
    this.unsureBind = (Binding) in.readObject();
  }
  
  @Override
  public void copyFrom(PatternProcessor otherProc)
  {
    super.copyFrom(otherProc);
    this.unsureBind = ((SkipPastLastRowVariableNonEventProcessor)otherProc).unsureBind;
  }
}
