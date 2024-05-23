/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/SkipPastLastRowNormalProcessor.java /main/1 2011/01/04 06:40:13 udeshmuk Exp $ */

/* Copyright (c) 2009, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    08/18/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/SkipPastLastRowNormalProcessor.java st_pcbpel_udeshmuk_reorg_and_variable_nonevent/3 2010/12/22 06:27:50 udeshmuk Exp $
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
 * Processor for SKIP PAST LAST ROW - Normal case
 * @author udeshmuk
 */
public class SkipPastLastRowNormalProcessor extends NormalProcessor
{
  /** Empty Constructor for HA*/
  public SkipPastLastRowNormalProcessor()
  {
    super();
  }
  
  public SkipPastLastRowNormalProcessor(PatternStrmClassBState s)
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
    return true;
  }
   
  /**
   * Logic determinign which unsure bindings to output
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
  
  public boolean reportBindings() throws ExecException
  {
    boolean done = false;
    
    Binding cur = null;
    Binding nextToCur = null;
    
    if(specialPattern)
    {
      done = reportSpecialPattern();
      return done;
    }
    
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
    { 
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
  public void processPattern() throws ExecException
  {
    //Nothing to be done for heartbeats
    if(pc.isHeartBeat)
      return;
    
    processTuple();
    if(!specialPattern)
      initReportBindings();
    if(pc.hasPartnAttrs)
      removeEmptyPartn();
  }
  
  @Override
  protected void processTuple() throws ExecException
  {
    Binding cur = null;
    Binding activeBind;
    s.tempConsumed = true;
    boolean del_remaining = false;
    
    if(PatternExecContext.trackStats)
      pc.tempTime = System.currentTimeMillis();
    
    /* common processing for everyone
     * iterator over current active set of bindings
     */
    if(!specialPattern)
    {
      s.activeIter = pc.bindSyn.getIterator();  
      
      while(s.activeIter.hasNext())
      {
        activeBind = s.activeIter.next();
      
        s.activeIter.remove();
        
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
    }
    
    //add transitions from S0 only if delete remaining is not set
    if(!del_remaining)
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
}
