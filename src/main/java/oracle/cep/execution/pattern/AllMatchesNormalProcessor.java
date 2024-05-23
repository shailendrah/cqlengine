/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/AllMatchesNormalProcessor.java /main/4 2013/11/14 08:24:46 udeshmuk Exp $ */

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
    udeshmuk    11/06/12 - XbranchMerge udeshmuk_bug-14397560_ps6 from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    10/29/12 - do not remove partitions till bindings are reported
    udeshmuk    04/10/11 - fix heartbeat handling
    udeshmuk    08/11/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/AllMatchesNormalProcessor.java /main/4 2013/11/14 08:24:46 udeshmuk Exp $
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
 * Processor for ALL MATCHES - normal case.
 * @author udeshmuk
 */
public class AllMatchesNormalProcessor extends NormalProcessor
{
  /** Empty Constructor HA */
  public AllMatchesNormalProcessor()
  {
    super();
  }
  
  public AllMatchesNormalProcessor(PatternStrmClassBState s)
  {
    super(s);
  }
  
  protected boolean shouldAllTransitionsBeApplied()
  {
    return true;
  }
  
  protected boolean shouldRemainingBindingsBeDeleted()
  {
    return false;
  }
  
  @Override
  protected void processTuple() throws ExecException
  {
    Binding activeBind;
    s.tempConsumed = true;
    
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
        processBinding(activeBind);
        
        releaseAllocHandlers(activeBind);
        activeBind.decrRef(inTupleStorageAlloc);
      }
    }
    
    //apply S0 trans always since all matches   
    setTargetTime(s.bindingB0, s.inputTs);
    processBinding(s.bindingB0);
    
    if(PatternExecContext.trackStats)
    {           
      pc.tempTime = System.currentTimeMillis() - pc.tempTime;
      s.stats.addCommonProcTime(pc.tempTime);
    }
  }

  @Override
  public void processPattern() throws ExecException
  {
    //Nothing to be done for heartbeats
    if(pc.isHeartBeat)
      return;
    
    processTuple();
  }
  
  public boolean reportBindings() throws ExecException
  {
    boolean done = false;

    //Nothing to be done for heartbeats
    if(pc.isHeartBeat)
      return done;
    
    if(specialPattern)
    {
      done = reportSpecialPattern();
      if(done)
      {
        //no need to call partn removal code inside if.
        return true;
      }
      if(pc.hasPartnAttrs && !pc.isHeartBeat)
        removeEmptyPartn();
    }
    else
    {
      //initialize the iterator
      s.unsureIter = pc.bindSyn.getUnsureIterator();
      
      while(s.unsureIter.hasNext())
      { //unsure list not empty - with or without partns
        
        s.outputBinding = s.unsureIter.next();
        s.unsureIter.remove();
        //Since all matches output is done as soon as there is a match.
        //Entire unsure list emptied(output) at the same ts.
        //So flag is false for all except the last
        if(s.unsureIter.hasNext()) //lookahead
          s.nextOutputOrderingFlag = false;
        else
          s.nextOutputOrderingFlag = s.lastInputTotalOrderingFlag;
  
        done = handleMeasures();
        if(done)
        {
          //no need to call partn removal code inside if.
          return true;
        }
      }
      
      //remove empty partitions after outputing unsure list.
      //earlier this code was present in processTuple. The problem
      //with that was if active list becomes empty then we attempted
      //to delete partition even though it had bindings in unsurelist
      //which will be output in reportBindings(). (tk14397560.cqlx)
      //For all other cases (allmatches-nonevent and within, skip past
      //last row-all cases) since either we have a common unsurelist
      //or we use readyToOutputBindings. Only in this case (all matches
      //-normal) this was a problem. So moving the partition removal
      //code here after unsurelist is processed.
      if(pc.hasPartnAttrs && !pc.isHeartBeat)
        removeEmptyPartn();
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
  
      s.stats.incrNumOutputHeartbeats();
      s.lastOutputTs = s.inputTs;
    }
    
    return false;
  }
}
