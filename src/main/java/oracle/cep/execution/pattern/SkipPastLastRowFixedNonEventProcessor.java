/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/SkipPastLastRowFixedNonEventProcessor.java /main/1 2011/01/04 06:40:13 udeshmuk Exp $ */

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
    udeshmuk    08/26/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/SkipPastLastRowFixedNonEventProcessor.java st_pcbpel_udeshmuk_reorg_and_variable_nonevent/3 2010/12/22 06:27:50 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.pattern;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.operators.PatternStrmClassBState;

/**
 * Processor for SKIP PAST LAST ROW - Fixed duration non-event case
 * @author udeshmuk
 */
public class SkipPastLastRowFixedNonEventProcessor extends NonEventProcessor
{
  /** Empty Constructor for HA*/
  public SkipPastLastRowFixedNonEventProcessor()
  {
    super();
  }
  
  public SkipPastLastRowFixedNonEventProcessor(PatternStrmClassBState s)
  {
    super(s);
  }
  
  @Override
  protected void addToUnsureList(Binding b) throws ExecException
  {
    //Here no need for init_report_binding step. This is because by
    //fixed duration defn, a binding which starts later than binding 'X'
    //cannot become unsure earlier than binding 'X'. So both unsureMinMatchedTs
    //and check with 'minActiveIndex' are not required.
    pc.bindSyn.addToReadyToOutputBindings(b);
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

  @Override
  protected void processTuple() throws ExecException
  {
    Binding activeBind;
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
      //input is a heartbeat and Target Time >= Input Time
      if(activeBind.getTargetTime() > s.inputTs && 
         (pc.isHeartBeat || processOtherPartitions))
        break;
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
      /*
       * We do not need to do the deletion of unsure bindings which started
       * earlier or at the same time as that of the current 'uniqueFinal'
       * binding. This is because in Fixed duration non-event such a binding
       * cannot become unsure at an earlier ts.
       */
    }
    
    /*
     * Add transitions from S0 only if delete remaining is not set.
     * applyS0trans is used to override the del_remaining setting.
     * e.g. A final binding is detected and del_remaining is set 
     * to true. But since this is non-event and if TT of the final 
     * binding is less than or equal to IT (current tuple ts) and
     * current tuple belongs to same partition and is not a heartbeat
     * then we should apply current tuple on bindingB0. 
     */ 
    if((!del_remaining  && !pc.isHeartBeat && !processOtherPartitions )|| applyS0trans)
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
  
  public boolean reportBindings() throws ExecException
  {
    Binding nextToCur = null;
    boolean done = false;
    
    //It will always be non-special pattern since non-event clause is present
    
    readyToOutputBindings = pc.bindSyn.getReadyToOutputBindings();
    while(!readyToOutputBindings.isEmpty())
    {
      s.outputBinding = readyToOutputBindings.poll();
      nextToCur = readyToOutputBindings.peek();
      if(nextToCur == null)
      { //last binding across all partitions for this input
        if(s.outputBinding.getMatchedTs() == s.inputTs)
          s.nextOutputOrderingFlag = s.lastInputTotalOrderingFlag;
        else { 
          s.nextOutputOrderingFlag = true;
        }
      }
      else if(nextToCur.getMatchedTs() > s.outputBinding.getMatchedTs())
        s.nextOutputOrderingFlag = true;
      else 
        s.nextOutputOrderingFlag = false;
        
      done = handleMeasures();
      if(done)
        return true;
    }

    //send a heartbeat for the skip past last row case
    if (s.lastOutputTs < s.inputTs)
    {
      s.outputElement.setTs(s.inputTs);
      s.outputElement.setKind(QueueElement.Kind.E_HEARTBEAT);
      /* RC fix. set to null explicitly. Otherwise element might
       * point to old tuple which is already freed.
       */
      s.outputElement.setTuple(null);
      /* copy i/p flag. For all matches all bindings having matchedTs 'T'
       * are output at first tuple at 'T'. So there won't be any output
       * having matchedTs < 'T' after 'T'.
       */
      s.outputElement.setTotalOrderingGuarantee(s.lastInputTotalOrderingFlag);
      if (outputQueue.isFull())
      {
        return true;
      }
      outputQueue.enqueue(s.outputElement);

      s.stats.incrNumOutputHeartbeats();

      s.lastOutputTs = s.inputTs;
    }

    return done;
  }
  
  private void doCommonProcessing() throws ExecException
  {
    processTuple();

    if(pc.hasPartnAttrs)
      removeEmptyPartn();
  }
  
  private void doNonEventProcessing() throws ExecException
  {
    if(PatternExecContext.trackStats)
      pc.tempTime = System.currentTimeMillis();
        
    //we have already processed the active bindings of the current partition
    //so we can set processOtherPartitions to true.
    processOtherPartitions = true;
    
    /*
     * ActiveItem data-structure is used while implementing non-event
     * with partn attrs. An instance of activeItem is created for every
     * active binding. The life-span of this instance is same as the 
     * life-span of the active binding to which it corresponds. 
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
  
  @Override
  public void processPattern() throws ExecException
  {
    //for heartbeats with partitions case just do non-event processing
    if(!pc.isHeartBeat || !pc.hasPartnAttrs)
      doCommonProcessing();
    if(pc.hasPartnAttrs)
      doNonEventProcessing();
  }
}
