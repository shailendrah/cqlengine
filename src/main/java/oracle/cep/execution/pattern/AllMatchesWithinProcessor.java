/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/AllMatchesWithinProcessor.java /main/1 2011/01/04 06:40:13 udeshmuk Exp $ */

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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/AllMatchesWithinProcessor.java st_pcbpel_udeshmuk_reorg_and_variable_nonevent/3 2010/12/22 06:27:50 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.pattern;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;
import java.util.LinkedList;

import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.operators.PatternStrmClassBState;

public class AllMatchesWithinProcessor extends WithinProcessor
{
  /**
   * Contains unsure bindings that are generated irrespective of partition.
   * We need to maintain this(can't just output a match directly) so as to
   * give proper piggybacking flag value in the outputElement.
   * Used every time except recurring non-event.
   */
  private LinkedList<Binding> commonUnsureList;
  
  public AllMatchesWithinProcessor()
  {
    super();  
  }
  
  public AllMatchesWithinProcessor(PatternStrmClassBState s)
  {
    super(s);
    commonUnsureList = new LinkedList<Binding>();
  }

  /**
   * Add the unsure binding to the collection of unsure bindings.
   * @param b - unsure binding to be added
   */
  protected void addToUnsureList(Binding b) 
  {
    commonUnsureList.add(b);
  }
  
  @Override
  public void processPattern() throws ExecException
  {
    //processes normal and HB tuples both
    processTuple();
    //remove partition only for non HB tuple
    if(pc.hasPartnAttrs && !pc.isHeartBeat)
      removeEmptyPartn();
  }

  @Override
  protected void processTuple() throws ExecException
  {
    if(PatternExecContext.trackStats)
      pc.tempTime = System.currentTimeMillis();
    
    Binding activeBind;
    s.tempConsumed = true;
    boolean iteratorInitialized    = false;
    PatternPartnContext curContext = null;
    Iterator<Binding>   startedItr = null;
    
    if(pc.hasPartnAttrs)
    {
      activeItems = pc.bindSyn.getActiveItems();
      
      //If activeItems not null check if any can be expired
      if(activeItems != null)
      {
        if(!pc.isHeartBeat)
        {
          //save current partition context
          curContext = pc.bindSyn.getCurrContext();
        }
        else
          curContext = null;
        
        if(!activeItems.isEmpty())
          currentActiveItem = activeItems.first();
        
        //Iterate through activeItems
        while((currentActiveItem != null) && 
              (
               (pc.isWithinInclusive && currentActiveItem.getReferredBinding().getTargetTime() < s.inputTs)
               ||(!pc.isWithinInclusive && currentActiveItem.getReferredBinding().getTargetTime() <= s.inputTs)
              )
             )
        {
          
          PatternPartnContext pctx = currentActiveItem.getOwnerPartnContext();
          
          //set processOtherPartitions appropriately 
          if(pc.isHeartBeat)
            processOtherPartitions = true;
          else
            processOtherPartitions = (curContext != pctx);
          
          //set appropriate partition context
          pc.bindSyn.setNonEventPartnContext(pctx);
          
          if(!processOtherPartitions)
          { //create iterator for current tuple partition only once
            if(!iteratorInitialized)
            {
              s.activeIter = pc.bindSyn.getIterator();
              startedItr   = s.activeIter;
              iteratorInitialized = true;
            }
          }
          else 
            s.activeIter = pc.bindSyn.getIterator();
         
          //remove activeItem and activeBinding
          activeItems.remove(currentActiveItem);
          
          //Binding to be removed will be the first binding in activelist/treeset
          if(!processOtherPartitions && iteratorInitialized)
          {
            assert startedItr.hasNext() : "startedItr hasNext false";
            startedItr.next();
            startedItr.remove();
          }
          else
          {
            assert s.activeIter.hasNext() : "activeIter hasNext false";
            s.activeIter.next();
            s.activeIter.remove();
          }
          
          //call processBinding
          processBinding(currentActiveItem.getReferredBinding());
          
          releaseAllocHandlers(currentActiveItem.getReferredBinding());
          currentActiveItem.getReferredBinding().decrRef(inTupleStorageAlloc);
          
          //remove the partition (if empty it will be removed).
          if(processOtherPartitions)
            removeEmptyPartn();
          
          //see if any more activeItems are left for processing
          if(!activeItems.isEmpty())
            currentActiveItem = activeItems.first();
          else
            currentActiveItem = null;
          
        } //end of while
        
        //restore variables
        pc.bindSyn.setNonEventPartnContext(curContext);
        processOtherPartitions = false;
        
        if(PatternExecContext.trackStats)
        {           
          pc.tempTime = System.currentTimeMillis() - pc.tempTime;
          s.stats.addNonEventTime(pc.tempTime);
          //start timing for common processing part
          pc.tempTime = System.currentTimeMillis();
        }
      } //end if (activeItems != null)
    } //end if(hasPartnAttrs)
    
    /*
     * Common processing for the current partition (partn to which the input tuple belongs):
     * 1. Partition case: Process the bindings which had TT > IT
     * 2. Non-Partition case : Process all the bindings (those with TT > IT and those with TT <= IT)
     */
     
    if(!pc.hasPartnAttrs || (pc.hasPartnAttrs && !pc.isHeartBeat))
    {
      if(!iteratorInitialized)
        s.activeIter = pc.bindSyn.getIterator();
      else
        s.activeIter = startedItr;
     
      while(s.activeIter.hasNext())
      {
        activeBind = s.activeIter.next();
        //do not process the binding or remove it from the list when
        //1. input is a heartbeat and Target Time > Input Time
        //2. input is a heartbeat and WITHIN INCLUSIVE clause is specified and no partns and TT == IT
        if(pc.isHeartBeat)
        {
          if((activeBind.getTargetTime() > s.inputTs)
             ||
             (pc.isWithinInclusive && !pc.hasPartnAttrs
              && (activeBind.getTargetTime() == s.inputTs)))
            break;
        }
      
        s.activeIter.remove();
       
        //remove the corresponding activeItem              
        if(pc.hasPartnAttrs)
           pc.bindSyn.removeActiveItem(activeBind);    
      
        processBinding(activeBind);
       
        releaseAllocHandlers(activeBind);
        activeBind.decrRef(inTupleStorageAlloc);
      }
     
      //apply S0 trans always since all matches except when hb
      //In case of partitions, when code comes here we know we will be processing
      //current tuple's partition only and not some other partition.
      //So processOtherPartitions need not be checked.
      if(!pc.isHeartBeat)
      {
        setTargetTime(s.bindingB0, s.inputTs);
        processBinding(s.bindingB0);
      }
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
    Binding nextBinding = null;
    
  //iterate through the common unsure list
    s.unsureIter = commonUnsureList.listIterator();
    if(s.unsureIter.hasNext())
    {
      s.outputBinding = s.unsureIter.next();
      do
      {
        s.unsureIter.remove();
        if(s.unsureIter.hasNext())
        {
          nextBinding = s.unsureIter.next();
          if(nextBinding.getMatchedTs() > s.outputBinding.getMatchedTs())
            s.nextOutputOrderingFlag = true;
          else
            s.nextOutputOrderingFlag = false;
        }
        else
        {
          nextBinding = null;
          //just propagate the ordering flag
          s.nextOutputOrderingFlag = s.lastInputTotalOrderingFlag;
        }
        
        done = handleMeasures();
        if(done)
          return true;
        
        s.outputBinding = nextBinding;
      }while(nextBinding != null);
    }
    
    //send a heartbeat for allmatches and partns case
    if(pc.hasPartnAttrs) //unsure list empty and partns exist
    { 
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
  
        s.stats.incrNumOutputs();

        s.lastOutputTs = s.inputTs;
      }
    }
    return false;
  }

  @Override
  protected boolean shouldAllTransitionsBeApplied()
  {
    return true; // ALL MATCHES
  }

  @Override
  protected boolean shouldRemainingBindingsBeDeleted()
  {
    return false; // ALL MATCHES
  }
}
