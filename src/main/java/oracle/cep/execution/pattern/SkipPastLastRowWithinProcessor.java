/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/SkipPastLastRowWithinProcessor.java /main/1 2011/01/04 06:40:13 udeshmuk Exp $ */

/* Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    10/15/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/SkipPastLastRowWithinProcessor.java st_pcbpel_udeshmuk_reorg_and_variable_nonevent/3 2010/12/22 06:27:50 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.pattern;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.execution.ExecException;
import oracle.cep.execution.operators.PatternStrmClassBState;

public class SkipPastLastRowWithinProcessor extends WithinProcessor
{
  /** Empty Constructor for HA*/
  public SkipPastLastRowWithinProcessor()
  {
    super();
  }
  
  public SkipPastLastRowWithinProcessor(PatternStrmClassBState s)
  {
    super(s);
  }

  protected void addToUnsureList(Binding b)
  {
    //need to maintain partition specific unsure list
    pc.bindSyn.addEndOfFinalList(b);
  }
  
  /**
   * Logic determining which unsure bindings can be output
   * @throws ExecException
   */
  private void initReportBindings() throws ExecException
  {
    assert !specialPattern : "No need to call init report " +
    "bindings for a special pattern";

    Binding cur = null;
    if(PatternExecContext.trackStats)
      pc.tempTime = System.currentTimeMillis();
    
    s.activeIter = pc.bindSyn.getIterator();
    s.unsureIter = pc.bindSyn.getUnsureIterator();
      
    if(s.activeIter.hasNext())
      minActiveIndex = s.activeIter.next().getStartIndex();
    else
      minActiveIndex = Long.MAX_VALUE;
  
    if(pc.hasPartnAttrs)
    { 
      while(s.unsureIter.hasNext())
      {
        cur = s.unsureIter.next();
        if(cur.getStartIndex() < minActiveIndex)
        {
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
  
  private void processActiveItems() throws ExecException
  {
    if(PatternExecContext.trackStats)
      pc.tempTime = System.currentTimeMillis();
    
    //We can set processOtherPartitions to true here as we have already
    //processed the relevant bindings of current tuple's partition.
    processOtherPartitions = true;
    
    /*
     * ActiveItem data-structure is used while implementing non-event and
     * WITHIN with partn attrs. An instance of activeItem is created for every
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
    
    if(!activeItems.isEmpty())
      currentActiveItem = activeItems.first();
    
    //Get an activeItem having TT <= IT.
    //Process the partition to which it belongs completely. 
    while((currentActiveItem != null) &&
          ((pc.isWithinInclusive && currentActiveItem.getReferredBinding().getTargetTime() < s.inputTs)
            ||(!pc.isWithinInclusive && currentActiveItem.getReferredBinding().getTargetTime() <= s.inputTs)
          )  
          )
    {
      pc.bindSyn.setNonEventPartnContext(currentActiveItem.getOwnerPartnContext());
      doCommonProcessing();
      //see if any more activeItems are left for processing
      if(!activeItems.isEmpty())
        currentActiveItem = activeItems.first();
      else
        currentActiveItem = null;
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
  
  @Override
  public void processPattern() throws ExecException
  {
    //for heartbeat with partitions case iterate through active items only
    if(!pc.isHeartBeat || !pc.hasPartnAttrs)
      doCommonProcessing();
    if(pc.hasPartnAttrs)
      processActiveItems();
  }

  @Override
  protected void processTuple() throws ExecException
  {
    Binding activeBind;
    Binding cur = null;
    s.tempConsumed = true;
    boolean del_remaining = false;
    
    if(PatternExecContext.trackStats)
      pc.tempTime = System.currentTimeMillis();
    
    /* common processing for everyone
     * iterator over current active set of bindings
     */
   
    s.activeIter = pc.bindSyn.getIterator();  
    
    while(s.activeIter.hasNext())
    {
      activeBind = s.activeIter.next();
      //do not process the binding or remove it from the list when
      //input is a heartbeat or we are processing some other partition if
      //1. TT > IT
      // OR
      //2. TT == IT but within inclusive is specified
      if(pc.isHeartBeat || processOtherPartitions)
      {
        if((activeBind.getTargetTime() > s.inputTs)
           ||(pc.isWithinInclusive && (activeBind.getTargetTime()==s.inputTs))
           )
          break;
      }

      s.activeIter.remove();
      
      //remove the corresponding activeItem              
      if(pc.hasPartnAttrs)
        pc.bindSyn.removeActiveItem(activeBind);          

      del_remaining = processBinding(activeBind);
      
      releaseAllocHandlers(activeBind);
      activeBind.decrRef(inTupleStorageAlloc);
      
      if(del_remaining)
        break;
    }
    
    if(del_remaining)
    {
      //delete remaining from the active list
      while(s.activeIter.hasNext())
      {
        activeBind = s.activeIter.next();
        
        releaseIndex(activeBind);
        releaseAllocHandlers(activeBind);
        activeBind.decrRef(inTupleStorageAlloc);
        s.activeIter.remove();
        //remove the corresponding activeItem              
        if(pc.hasPartnAttrs)
          pc.bindSyn.removeActiveItem(activeBind);            
      }
      //delete bindings from unsure list which are preferred less
      long uniFinStart = s.uniqueFinal.getStartIndex();
      
      s.unsureIter = pc.bindSyn.getUnsureIterator();
      while(s.unsureIter.hasNext())
      {
        cur = s.unsureIter.next();
        if(cur == s.uniqueFinal)
          continue;
        if(cur.getStartIndex() >= uniFinStart)
        {
          releaseIndex(cur);
          releaseAllocHandlers(cur);
          cur.decrRef(inTupleStorageAlloc);
          s.unsureIter.remove();
          if(pc.hasPartnAttrs)
            pc.bindSyn.removeUnsureItem(cur.getUnsureItem());
        }
      }
    }
    
    /*
     * Add transitions from S0 only if delete remaining is not set 
     * and input is not hb and not belonging to different partitions.
     */ 
    if(!del_remaining && !pc.isHeartBeat && !processOtherPartitions)
    {
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
  public boolean reportBindings() throws ExecException
  {
    boolean done = false;
    
    Binding cur = null;
    Binding nextToCur = null;
    
   
    if(pc.hasPartnAttrs) //not all matches with partition
    { 
      readyToOutputBindings = pc.bindSyn.getReadyToOutputBindings();
      while(!readyToOutputBindings.isEmpty())
      {
        cur = readyToOutputBindings.peek();
        if(cur.getMatchedTs() < minTs)
        {
          //output
          nextToCur = readyToOutputBindings.peek();
          if(nextToCur == null)
          { 
            s.nextOutputOrderingFlag = s.lastInputTotalOrderingFlag;
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
                s.nextOutputOrderingFlag = s.lastInputTotalOrderingFlag;
              }
            }
            else //Here nextToCur will be output always 
              s.nextOutputOrderingFlag = false;
          }
          s.outputBinding = cur;
          readyToOutputBindings.poll();
          done = handleMeasures();
          if(done)
            return true;
        }   
        else 
          break;
      }
    }
    else //not all matches without partition
    {  /* It is possible to have multiple matches being output due to 
          same input tuple.
          e.g. An active binding that starts at 1000 and within duration is
               till 8000.
               One unsure binding starts at 2500 and ends at 4500
               Other starts at 5500 and ends at 7000.
               Due to active binding that started at 1000 both these unsure
               bindings cannot be output.  
               Now when a tuple at 8000 arrives then the active binding expires
               WITHIN duration and is deleted. So both these unsure bindings
               can be output.
           */
      
      //initialize the iterator
      s.unsureIter = pc.bindSyn.getUnsureIterator();
      
      if(s.unsureIter.hasNext())
      { //non empty unsurelist
        Binding next = null;
        cur = s.unsureIter.next();
        
        while(true)
        {
          //ordering flag setting logic
          if(cur.getStartIndex() < minActiveIndex)
          {   
            //cur will be output - remove first then look for next
            s.unsureIter.remove();
            
            if(s.unsureIter.hasNext())
            { 
              next = s.unsureIter.next();
              
              if(next.getStartIndex() < minActiveIndex)
              { //next will also be output
                if(cur.getMatchedTs() == next.getMatchedTs())
                  s.nextOutputOrderingFlag = false;
                else // cur.getMatchedTs() < next.getMatchedTs()
                  s.nextOutputOrderingFlag = true;
              }
              else //cur will be the last match which is o/p as next won't be
              {
                s.nextOutputOrderingFlag = s.lastInputTotalOrderingFlag;
                next = null; //ensures we quit the loop as soon as cur is o/p
              }
            }
            else // cur is the last binding in the list
            {
              s.nextOutputOrderingFlag = s.lastInputTotalOrderingFlag;
              next = null; //ensures we quit the loop as soon as cur is o/p
            }
          }
          else //cur won't be output so return
            return done;
            
          //output
          s.outputBinding = cur;
          done = handleMeasures();
          if(done)
            return true;
          
          //move cur to next binding
          if(next != null)
            cur = next;
          else
            break;
        }
      }
    }
    return done;
  }

  @Override
  protected boolean shouldAllTransitionsBeApplied()
  {
    return false; // SKIP PAST LAST ROW
  }

  @Override
  protected boolean shouldRemainingBindingsBeDeleted()
  {
    return true;  // SKIP PAST LAST ROW
  }
}
