/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/evictPolicy/BottomUpPolicy.java /main/12 2009/02/23 06:47:35 sborah Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      12/17/08 - handle constants
    hopark      12/04/08 - pass cepMgr
    hopark      10/10/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    hopark      06/18/08 - logging refactor
    hopark      05/05/08 - add isFullSpill
    hopark      02/25/08 - fix test code
    hopark      02/25/08 - fix test code
    hopark      02/07/08 - implement dump
    hopark      01/01/08 - support xmllog
    hopark      11/08/07 - add m_test
    hopark      10/15/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/evictPolicy/BottomUpPolicy.java /main/12 2009/02/23 06:47:35 sborah Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.memmgr.evictPolicy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;

import oracle.cep.execution.ExecException;
import oracle.cep.execution.ExecManager;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.scheduler.SchedulerManager;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.PageManager;
import oracle.cep.memmgr.PagedFactory;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.planmgr.PlanManager;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;
import oracle.cep.util.StringUtil;

@DumpDesc(autoFields=true,
    evPinLevel=LogLevel.SPILL_ARG,
    infoLevel=LogLevel.SPILL_EVICTINFO,
    dumpLevel=LogLevel.SPILL_DUMP,
    verboseDumpLevel=LogLevel.SPILL_DUMP_DETAIL)
public class BottomUpPolicy extends BasePolicy
{
  protected long memThreshold0;
  protected long memThreshold1;
  protected long memThreshold2;
  protected long countThreshold0;
  protected long countThreshold1;
  protected long debugCount;
  protected int  mode;
  protected long minEvictable;
  
  protected long countThreshold;
  protected long memThreshold;
  protected long targetMemThreshold;   //memory threshold after gc is invoked
  
  // If run in synchronously through newStorageAlloc, number of invocation
  // before start evicting. Once the number of invocation reaches this threshold
  // , the evictor starts to check the memory threshold
  protected static int CHECK_COUNT = 100000;
  protected static int CHECK_COUNT0 = 300000;
  protected static int DEBUG_COUNT = 1207;
  
  // Percentage of free memory from the total memory that triggers evictor.
  protected static int MEM_THRESHOLD = 25;  
  protected static int MEM_HI_THRESHOLD = 10;

  // If the number of element is smaller than this threshold, the data structure
  // will not be considerered evictable.
  protected static int EVICTABLE_MIN_SIZE = 1000;

  public BottomUpPolicy()
  {
    super();
    mode = MODE_NORMAL;
    //runType = RUNTYPE_FACTORY | RUNTYPE_BACKGROUND;
    runType = RUNTYPE_SCHEDULER;
    
    test = false;
    testWait = false;
    long totalMem = Runtime.getRuntime().totalMemory();
    memThreshold1 =  totalMem * MEM_THRESHOLD / 100;
    long storageCache = totalMem * 10 / 100; 
    memThreshold0 = memThreshold1 + storageCache;
    memThreshold2 = totalMem * MEM_HI_THRESHOLD / 100;
    countThreshold0 = CHECK_COUNT0;
    countThreshold1 = CHECK_COUNT;
    debugCount = DEBUG_COUNT;
  }
  public void setCEPManager(CEPManager cep) {cepMgr = cep;}
  
  public void setMode(int v) {mode = v;}
  public void setRunType(int v) {runType = v;}
  public void setTest(boolean v) {test = v;}
  public void setTestWait(boolean v) {testWait = v;}
  public void setMemThreshold0(long v)  {memThreshold0 = v; }
  public void setMemThreshold1(long v) { memThreshold1 = v; }
  public void setMemThreshold2(long v) {memThreshold2 = v; } 
  public void setCountThreshold0(long v) { countThreshold0 = v; }
  public void setCountThreshold1(long v) { countThreshold1 = v; }
  public void setMinEvictable(long v) { minEvictable = v; }
  public void setDebugCount(long v) { debugCount = v; }
  public boolean isFullSpill() {return true;}
  
  public void startEvictor()
  {
    if (memThreshold0 < 0) 
      memThreshold0 = totalMem * (-memThreshold0) / 100;
    if (memThreshold1 < 0) 
      memThreshold1 = totalMem * (-memThreshold1) / 100;
    if (memThreshold2 < 0) 
      memThreshold2 = totalMem * (-memThreshold2) / 100;

    if (memThreshold0 == 0)
    {
      memThreshold0 = memThreshold1 + storage.getCacheSize(); 
    }
    memThreshold = memThreshold0;
    countThreshold = countThreshold0;
    String names[] = getClass().getName().split("\\.");
    String name = names[names.length-1];
    LogUtil.config(LoggerType.TRACE, "===  " + name +" total=" + memStr(totalMem));
    if (mode != MODE_DEBUG)
    {
      LogUtil.config(LoggerType.TRACE, "threshold = " + memStr(memThreshold0) + "," + memStr(memThreshold1) + "," + memStr(memThreshold2));
      LogUtil.config(LoggerType.TRACE, "count = " + countThreshold0 + "," + countThreshold1);
    } else {
      LogUtil.config(LoggerType.TRACE, "debugCount = " + debugCount);
      runType &= ~RUNTYPE_BACKGROUND;
    }
    
    start();
  }
  
  public boolean needEviction(Source src)
  {
    count++;
    
    int flag = 0;
    switch(src)
    {
    case Factory: flag = RUNTYPE_FACTORY; break;
    case Scheduler: flag = RUNTYPE_SCHEDULER; break;
    case Background: flag = RUNTYPE_BACKGROUND; break;
    }
    if ((runType & flag) == 0) 
      return false;
    
    long freemem = runtime.freeMemory();

    // do not use invocation m_count from background.
    boolean useCountTh = (src != Source.Background);
    if (runCount == 0)
    {
        useCountTh = false;
    }
    String StartTimeStr = formatter.format(new Date());
    LogLevelManager.trace(LogArea.SPILL, LogEvent.SPILL_EVICT_BGCHK, this, 
      StartTimeStr + 
      " free = " + memStr(freemem) +
      " count = " + count +           
      " memThreshold = " + memStr(memThreshold) +
      " countThreshold = " + countThreshold +
      " useCount = " + useCountTh);

    if (mode != MODE_DEBUG)
    {
      if ((freemem > memThreshold2) &&
         (useCountTh && count < countThreshold)) return false;
      if (freemem > memThreshold) return false;
    } else 
    {
      // debug mode
      if (count < debugCount) return false;
    }
    return true;
  }
  
  public void runEvictor(Source src) 
  {
    String StartTimeStr = formatter.format(new Date());
    String name = StringUtil.getBaseClassName(this);

    long oldFreemem = runtime.freeMemory();
    LogLevelManager.trace(LogArea.SPILL, LogEvent.SPILL_EVICT_BEGIN, this, 
        StartTimeStr + " " + name +
        "(" + runCount + ") started : free = " + memStr(oldFreemem) +
        " m_count = " + count + 
        " memThreshold = " + memStr(memThreshold) +
        " countThreshold" + countThreshold);

    if (test) 
    {
      heapdump("before");
    }

    long starttime = System.currentTimeMillis();
    evcount = 0;

    stat.resetCollectGc();
    long freemem = invokeGC();
    // After GC, if the size of free memory is big enough, do not evits.
    // If we just use the same threshold, it is likely to hit the threshold
    // very soon.
    boolean run_evict = true;
    
    targetMemThreshold = memThreshold + memThreshold/4; 
    if (mode != MODE_DEBUG && freemem > targetMemThreshold) {
      run_evict = test;
      count = 0;
    } 
    if (run_evict) 
    {
      try {
        evict();
      } catch (ExecException e)
      {
        LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      }
      if (runCount == 0)
      {
        countThreshold = countThreshold1;
        memThreshold = memThreshold1;
      }
      runCount++;
    }  
    
    long newFreeMem = 0;        
    if (evcount > 0)
    {
      newFreeMem = invokeGC();
    }
    else
    {
      newFreeMem = runtime.freeMemory();
    }
    
    long endtime = System.currentTimeMillis();
    long difftime = (endtime - starttime);
    if (storage != null)
    {
      stat.addEvict(difftime);
    }        
    
    String EndTimeStr = formatter.format(new Date());
    String endstr = EndTimeStr + " " +  name + "(" + runCount + ") ended\ntotal " + evcount + " objects evicted : free = " + memStr(oldFreemem) + " -> " + memStr(newFreeMem) + " " + timeStr(difftime);
    LogLevelManager.trace(LogArea.SPILL, LogEvent.SPILL_EVICT_END, this, endstr);
    count = 0;   
    if (test) 
    {
      heapdump("after");
    }
  }
  
  @SuppressWarnings("unchecked")
protected void evict(ExecContext ec)
    throws ExecException
  {
    SchedulerManager sched = ec.getSchedMgr();
    Runnable[] queued = sched.getQueued();
    HashMap<Integer, ExecOpt> queuedMap = new HashMap<Integer, ExecOpt>();
    if (queued != null)
    {
      for (Runnable r : queued)
      {
        ExecOpt opt = (ExecOpt) r;
        queuedMap.put(opt.getPhyOptId(), opt);
      }
    }
    Iterator<ExecOpt> itr = ec.getExecMgr().getSourceOpIterator();
    PlanManager pm = ec.getPlanMgr();
    LinkedList<PhyOpt> opts = new LinkedList<PhyOpt>();
    LinkedList<PhyOpt> scheduledOpts = new LinkedList<PhyOpt>();
    while (itr.hasNext())
    {
      ExecOpt sourceOp = itr.next();
      int phyOptId = sourceOp.getPhyOptId();
      PhyOpt phyOpt = pm.getPhyOpt(phyOptId);
      assert (phyOpt != null);
      if (queuedMap.get(phyOptId) == null)
        opts.add(phyOpt);
      else
        scheduledOpts.add(phyOpt);
    }

    int totalOps = pm.getNumOperators();
    int m_count = 0;
    // evicts states (queue, store) from the bottom operators in the query tree
    int oldevcount = evcount;
    while (opts.size() > 0)
    {
      PhyOpt opt = opts.removeFirst();
      if (opt != null) 
      {
        ArrayList<PhyOpt> outputs = opt.getOutputs();
        if (outputs != null) 
        {
          for (PhyOpt output : outputs)
          {
            if (output == null) continue;
            int phyOptId = output.getId();
            if (queuedMap.get(phyOptId) == null)
              opts.add(output);
            else
              scheduledOpts.add(output);
          }
        }
      }
      if (opts.size() == 0)
      {
        // scheduled operators will be the last candidate to be evicted. 
        opts.addAll(scheduledOpts);
        scheduledOpts.clear();
      }
      if (opt == null) continue;
      ExecOpt eopt = opt.getInstOp();
      if (eopt.evict())
        evcount++;
      
      m_count++;
      
      if (m_count >= (totalOps / 4) && evcount > oldevcount)
      {
        oldevcount = evcount;
        long freemem = invokeGC();
        m_count = 0;
        if (mode != MODE_DEBUG && freemem > targetMemThreshold) 
        {
          // we have enough memory to continue.
          if (!test)
            break;
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public synchronized void dump(IDumpContext dumper) 
  {
    FactoryManager factoryMgr = cepMgr.getFactoryManager();
    Iterator<IAllocator> facItr = factoryMgr.getIterator();
    String facsTag = "Factories";
    dumper.beginTag(facsTag, null, null);
    while (facItr.hasNext())
    {
      IAllocator sf = facItr.next();
      if (sf instanceof PagedFactory)
      {
        PagedFactory pf = (PagedFactory) sf;
        PageManager pm = pf.getPageManager();
        if (pm != null)
          pm.dump(dumper);
      }
    }        
    dumper.endTag(facsTag);
  }
}
