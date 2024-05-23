/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/AllMatchesNonEventProcessor.java /main/3 2013/11/14 08:24:46 udeshmuk Exp $ */

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
    udeshmuk    11/13/13 - send heartbeat irrespective of whether partitions or
                           not
    udeshmuk    03/17/11 - always use readyToOutputBindings for recurring
                           non-event
    udeshmuk    08/11/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/AllMatchesNonEventProcessor.java /main/3 2013/11/14 08:24:46 udeshmuk Exp $
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

/**
 * Processor for ALL MATCHES - Non-event case (Fixed and variable both).
 * @author udeshmuk
 */
public class AllMatchesNonEventProcessor extends NonEventProcessor
{
  /** Empty Constructor for HA*/
  public AllMatchesNonEventProcessor()
  {
    super();
  }
  
  public AllMatchesNonEventProcessor(PatternStrmClassBState s)
  {
    super(s);
    commonUnsureList = new LinkedList<Binding>();
  }
  
  /**
   * Contains unsure bindings that are generated irrespective of partition.
   * We need to maintain this(can't just output a match directly) so as to
   * give proper piggybacking flag value in the outputElement.
   * Used every time except recurring non-event.
   */
  private LinkedList<Binding> commonUnsureList;
    
  @Override
  protected boolean shouldAllTransitionsBeApplied()
  {
    return true;
  }

  @Override
  protected boolean shouldRemainingBindingsBeDeleted()
  {
    return false;
  }
  
  protected void addToUnsureList(Binding b) throws ExecException
  {
    //For recurring non-event with partns, the bindings may be generated in 
    //out-of-order fashion, so need to maintain unsure bindings in PQ keeping
    //the bindings ordered by matchedTs.
    //e.g. 1000 10 - matches A        |  pattern(AB*) duration 10 
    //     2000 20 - matches A and B  |  define B as B.c1 != A.c1
    //     h 43000
    //For non-recurring case bindings will be generated in timestamp order
    //so the list suffices.
    if(isRecurringNonEvent)
      pc.bindSyn.addToReadyToOutputBindings(b);
    else
      commonUnsureList.add(b);
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
        
        //Iterate through activeItems
        while(!activeItems.isEmpty() && 
              (currentActiveItem=activeItems.first())
              .getReferredBinding().getTargetTime() <= s.inputTs)
        {
          
          PatternPartnContext pctx = currentActiveItem.getOwnerPartnContext();
          
          //set processOtherPartitions appropriately 
          if(pc.isHeartBeat)
            processOtherPartitions = true;
          else
            processOtherPartitions = (curContext != pctx);
          
          //set appropriate partition context
          pc.bindSyn.setNonEventPartnContext(pctx);
          
          //Need to ensure that the iterator for the current(tuple's) partition
          //is created only once. Otherwise active bindings that are newly added
          //during recurring non-event processing will undergo processing twice 
          //resulting in wrong results.
          //For 'other' partition this is not a problem.
          if(!processOtherPartitions)
          {
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
    } // end if (hasPartnAttrs)
    
    /*
     * Common processing for the current partition (partn to which the input tuple belongs):
     * 1. Partition case: Process the bindings which had TT > IT
     * 2. Non-Partition case : Process all the bindings (those with TT > IT and those with TT <= IT)
     */
    
    //Nothing to be done for heartbeats
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
        //input is a heartbeat and Target Time >= Input Time
        if(activeBind.getTargetTime() > s.inputTs && 
           pc.isHeartBeat)
          break;
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
  public void processPattern() throws ExecException
  {
    //processes normal and HB tuples both
    processTuple();
    //remove partition only for non HB tuple
    if(pc.hasPartnAttrs && !pc.isHeartBeat)
      removeEmptyPartn();
  }
  
  public boolean reportBindings() throws ExecException
  {
    boolean done = false;
    Binding nextBinding = null;
    
    if(!isRecurringNonEvent)
    {
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
            if(s.outputBinding.getTargetTime() == s.inputTs)
              s.nextOutputOrderingFlag = s.lastInputTotalOrderingFlag;
            else //since any further output will be at higher ts
              s.nextOutputOrderingFlag = true;
          }
          
          done = handleMeasures();
          if(done)
            return true;
          
          s.outputBinding = nextBinding;
        }while(nextBinding != null);
      }
    }
    else
    {
      readyToOutputBindings = pc.bindSyn.getReadyToOutputBindings();
      while(!readyToOutputBindings.isEmpty())
      {
        s.outputBinding = readyToOutputBindings.poll();
        nextBinding     = readyToOutputBindings.peek();
        if(nextBinding != null)
        {
          if(nextBinding.getMatchedTs() > s.outputBinding.getMatchedTs())
            s.nextOutputOrderingFlag = true;
          else
            s.nextOutputOrderingFlag = false;
        }
        else
        {
          if(s.outputBinding.getMatchedTs() == s.inputTs)
            s.nextOutputOrderingFlag = s.lastInputTotalOrderingFlag;
          else 
            s.nextOutputOrderingFlag = true;
        }
        
        done = handleMeasures();
        if(done)
          return true;
      }
    }
    
    //send a heartbeat for allmatches case
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
    
    return false;
  }
}
