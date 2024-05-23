/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/memmgr/evictPolicy/QueueSrcPolicy.java /main/6 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2008, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/03/11 - refactor storage
    hopark      12/04/08 - use ConfigManager for configuration
    hopark      06/18/08 - logging refactor
    hopark      05/23/08 - change msg level
    hopark      05/05/08 - add isFullSpill
    hopark      04/22/08 - log state change
    hopark      03/08/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/evictPolicy/QueueSrcPolicy.java /main/5 2008/12/10 18:55:57 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.memmgr.evictPolicy;

import java.util.LinkedList;
import java.util.logging.Level;

import oracle.cep.execution.ExecException;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.memmgr.IEvictPolicy;
import oracle.cep.memmgr.IEvictPolicyCallback;
import oracle.cep.memmgr.IAllocator.NameSpace;
import oracle.cep.memmgr.IEvictPolicyCallback.SpillCmd;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.service.CEPManager;
import oracle.cep.storage.StorageException;

public class QueueSrcPolicy extends BasePolicy implements IEvictPolicy
{
  enum SpillingState
  {
    NORMAL, PARTIAL_SPILL, FULL_SPILL, SYNCEVICT
  }
  
  // Percentage of free memory from the total memory that triggers evictor.

  enum QueueSrcPolicyParam
  {
    partialSpillThreshold,   //turn on spilling
    normalThreshold,    //turn off spilling
    fullSpillThreshold,  //force spill threshold
    syncSpillThreshold, //synchronous eviction (client slows down)
    windowSize,        // window size of moving avg of freeMem         
    test,
    testWait,
    runType,
    debugCount,
    MAX_PARAM
  };
  protected boolean m_testMode;
  protected int  m_runType;
  protected int  m_debugCount;
  protected long m_normalThreshold;
  protected long m_partialSpillThreshold;
  protected long m_fullSpillThreshold;
  protected long m_syncSpillThreshold;
  protected int  m_windowSize;

  protected SpillingState  m_oldSpillingState;
  protected SpillingState  m_spillingState;
  protected int  m_count;
  protected long m_freeMemAvg;
  protected long[] m_freeMemQ;
  protected int  m_freeMemQH;
  protected int  m_freeMemQT;
  @DumpDesc(ignore=true) protected LinkedList<IEvictPolicyCallback> m_callbacks;
  SpillCmd s_spillCmdTb[] = {
      SpillCmd.SET_NORMAL,      //NORMAL, 
      SpillCmd.SET_ASYNCSPILL,  //PARTIAL_SPILL, 
      SpillCmd.SET_ASYNCSPILL,  //FULL_SPILL, 
      SpillCmd.SET_SYNCSPILL,   //SYNCEVICT
  };
  
  public QueueSrcPolicy()
  {
    super();
    //make sure SpillingState and s_spillCmdTbl is synced.
    assert (SpillingState.NORMAL.ordinal() == 0);
    assert (SpillingState.PARTIAL_SPILL.ordinal() == 1);
    assert (SpillingState.FULL_SPILL.ordinal() == 2);
    assert (SpillingState.SYNCEVICT.ordinal() == 3);
    m_callbacks = new LinkedList<IEvictPolicyCallback>();
    m_normalThreshold =  Integer.MAX_VALUE;
    m_partialSpillThreshold = Integer.MAX_VALUE;
    m_fullSpillThreshold = Integer.MAX_VALUE;
    m_syncSpillThreshold = Integer.MAX_VALUE;
    m_windowSize = 5;   // 5 samples
    m_runType = BasePolicy.RUNTYPE_FACTORY;
  }
  
  public void setRunType(int v) {runType = v;}
  public void setTest(boolean v) {test = v;}
  public void setTestWait(boolean v) {testWait = v;}
  public void setNormalThreshold(long v)  {m_normalThreshold = v; }
  public void setPartialThreshold(long v) { m_partialSpillThreshold = v; }
  public void setFullThreshold(long v) {m_fullSpillThreshold = v; } 
  public void setSyncThreshold(long v) { m_syncSpillThreshold = v; }
  public void setWindowSize(int v) { m_windowSize = v; }
  public void setDebugCount(int v) { m_debugCount = v; }
  public boolean isFullSpill() {return false;}

  public void init(CEPManager cepMgr)
  {
    this.cepMgr = cepMgr;
    this.storage = cepMgr.getStorageManager().getSpillStorage();
    // initialize namespace
    try
    {
      for (NameSpace ns : NameSpace.values())
      {
        storage.addNameSpace(null, ns.toString(), false, null, null, null);
      }
    } catch(StorageException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }
  }
  
  public void startEvictor()
  {
    ConfigManager configMgr = cepMgr.getConfigMgr();
    if (m_normalThreshold == Integer.MAX_VALUE)
    {
      m_normalThreshold = configMgr.getQueueSrcSpillNormalThreshold();
    }
    if (m_partialSpillThreshold == Integer.MAX_VALUE)
    {
      m_partialSpillThreshold = configMgr.getQueueSrcSpillPartialThreshold();
    }
    if (m_fullSpillThreshold == Integer.MAX_VALUE)
    {
      m_fullSpillThreshold = configMgr.getQueueSrcSpillFullThreshold();
    }
    if (m_syncSpillThreshold == Integer.MAX_VALUE)
    {
      m_syncSpillThreshold = configMgr.getQueueSrcSpillSyncThreshold();
    }

    if (m_partialSpillThreshold < 0) 
      m_partialSpillThreshold = totalMem * (-m_partialSpillThreshold) / 100;
    if (m_normalThreshold < 0) 
      m_normalThreshold = totalMem * (-m_normalThreshold) / 100;
    if (m_fullSpillThreshold < 0) 
      m_fullSpillThreshold = totalMem * (-m_fullSpillThreshold) / 100;
    if (m_syncSpillThreshold < 0) 
      m_syncSpillThreshold = totalMem * (-m_syncSpillThreshold) / 100;
    LogUtil.config(LoggerType.TRACE, "===  " + getClass().getSimpleName() +" total=" + BasePolicy.memStr(totalMem));
    LogUtil.config(LoggerType.TRACE, 
          "normalThreshold = " + BasePolicy.memStr(m_normalThreshold) + "," +
          "partialSpillThreshold = " + BasePolicy.memStr(m_partialSpillThreshold) + "," + 
          "fullSpillThreshold = " + BasePolicy.memStr(m_fullSpillThreshold) + "," +
          "syncSpillThreshold = " + BasePolicy.memStr(m_syncSpillThreshold) );
    LogUtil.config(LoggerType.TRACE, "window = " + m_windowSize);
    if (m_testMode)
    {
      LogUtil.config(LoggerType.TRACE, "debugCount = " + m_debugCount);
    }
    
    long freemem = Runtime.getRuntime().freeMemory();
    m_freeMemQ = new long[m_windowSize];
    m_freeMemQH = 0;
    m_freeMemQT = m_windowSize - 1;
    for (int i = 0; i < m_windowSize; i++)
      m_freeMemQ[i] = freemem;
    m_freeMemAvg  = freemem;
    m_spillingState = SpillingState.NORMAL;
    m_oldSpillingState = SpillingState.NORMAL;
    
    start();
  }
  
  
  public boolean isUsingCallback() {return true;}
  
  public void addCallback(IEvictPolicyCallback cb)
  {
    m_callbacks.add(cb);
  }

  public void removeCallback(IEvictPolicyCallback cb)
  {
    cb.stop();
    m_callbacks.remove(cb);
  }
  
  public void forceEvict() throws ExecException
  {
  }

  /**
   * Calculates simple moving average(SMA)
   * 
   * SMA = (Pm + Pm_1 + ... + Pm_no_data_points+1) / no_data_points;
   * 
   * A new values comes into the sum and an old value drops out, 
   * so full summation each time is unnecessary.
   * SMA = SMA_1 - (Pm_no_data_points+1 / no_data_points) + (Pm / no_data_points) 
   */
  public synchronized void calcFreemem()
  {
    long freemem = Runtime.getRuntime().freeMemory();
    m_freeMemAvg  = m_freeMemAvg - (m_freeMemQ[m_freeMemQH] / m_windowSize) + (freemem / m_windowSize);

    // advance queue
    m_freeMemQH = (m_freeMemQH + 1) % m_windowSize;
    m_freeMemQT = (m_freeMemQT + 1) % m_windowSize;
    m_freeMemQ[m_freeMemQT] = freemem;
    LogLevelManager.trace(LogArea.SPILL, LogEvent.SPILL_EVICT_BGCHK, this, "free = " + m_freeMemAvg);
  }
  
  public synchronized boolean needEviction(Source src)
  {
    if (m_callbacks.size() == 0)
      return false;
    
    m_count++;
    
    int flag = 0;
    switch(src)
    {
    case Factory: flag = BasePolicy.RUNTYPE_FACTORY; break;
    case Scheduler: flag = BasePolicy.RUNTYPE_SCHEDULER; break;
    case Background: flag = BasePolicy.RUNTYPE_BACKGROUND; break;
    }
    if ((m_runType & flag) == 0) 
      return false;

    if (m_testMode)
    {
      if (m_count > m_debugCount)
      {
        m_count = 0;
        int ord = m_spillingState.ordinal();
        ord++;
        SpillingState[] vals = SpillingState.values();
        if (ord >= vals.length) ord = 0;
        m_spillingState = vals[ord];
        return true;
      }
      return false;
    }
    
    calcFreemem();
    SpillingState old_spilling = m_spillingState;
    switch (m_spillingState)
    {
    case NORMAL:
      if (m_freeMemAvg < m_partialSpillThreshold)
      {
        m_spillingState = SpillingState.PARTIAL_SPILL;
      }
      break;
      
    case PARTIAL_SPILL:
      if (m_freeMemAvg < m_fullSpillThreshold)
      {
        m_spillingState = SpillingState.FULL_SPILL;
      }
      break;
    case FULL_SPILL:
      if (m_freeMemAvg < m_syncSpillThreshold)
      {
        m_spillingState = SpillingState.SYNCEVICT;
      }
      break;
    case SYNCEVICT:
      if (m_freeMemAvg > m_fullSpillThreshold)
      {
        m_spillingState = SpillingState.PARTIAL_SPILL;
      }
      break;
    }
    if (m_freeMemAvg > m_normalThreshold)
    {
      m_spillingState = SpillingState.NORMAL;
    }
    if (m_spillingState != old_spilling)
    {
      LogUtil.fine(LoggerType.TRACE, "QueueSrc state change : " + 
            m_spillingState.toString() + " " + memStr(m_freeMemAvg));
    }
    return (m_spillingState != old_spilling);
  }

  private IEvictPolicyCallback findMaxEvictables()
  {
    int maxcnt = -1;
    IEvictPolicyCallback maxcb = null;
    for (IEvictPolicyCallback cb : m_callbacks)
    {
      int cnt = cb.getEvictableCount();
      if (cnt > maxcnt)
      {
        maxcnt = cnt;
        maxcb = cb;
      }
    }
    assert (maxcb != null);
    return maxcb;
  }
  
  public synchronized void runEvictor(Source src)
  {
    LogLevelManager.trace(LogArea.SPILL, LogEvent.SPILL_EVICT_BEGIN, this, "spilling = "+m_spillingState.toString() + " " +
        BasePolicy.memStr(m_freeMemAvg) + 
        ", partial=" + BasePolicy.memStr(m_partialSpillThreshold) + 
        ", off=" + BasePolicy.memStr(m_normalThreshold) +
        ", full=" + BasePolicy.memStr(m_fullSpillThreshold) +
        ", sync=" + BasePolicy.memStr(m_syncSpillThreshold) );
    
    SpillCmd cmd = s_spillCmdTb[m_spillingState.ordinal()];
    
    switch (m_spillingState)
    {
    case SYNCEVICT:
      // have all queues to evict the pages in memory asynchronously. 
      // With this, all queues will not have more than two pages in memory.
      // (one for dequeue and other for enqueue)
      /*
      for (IEvictPolicyCallback cb : m_callbacks)
      {
        cb.evictionTriggered(src, this, SpillCmd.FORCE_EVICT, null);
      }
      // all cbs are notified SYNC_SPILL
      break;
      */
      // Currently, instead of evict all pages in memory in transition to syncevict,
      // we allow the queues which was not a largest queue keep the pages in memory.
      // This will guarantees that only two pages from the largest queue will be in
      // memory while other queus may have more.
      // In any case, it's possible to have outofmemory because we are not controlling
      // the full state.
    case FULL_SPILL:
      {
        //find the largest queue and evicts them forcefully (asynchronously).
        IEvictPolicyCallback maxcb = findMaxEvictables();
        maxcb.evictionTriggered(src, this, SpillCmd.FORCE_EVICT, null);

        // all cbs are notified FULL_SPILL
        break;
      }
      
    case PARTIAL_SPILL:
      IEvictPolicyCallback maxcb = findMaxEvictables();
      if (m_oldSpillingState.ordinal() < m_spillingState.ordinal())
      {
        // transition from NORMAL to PARTIAL.
        //find the largest queue and set ASYNC_EVICT
        maxcb.evictionTriggered(src, this, cmd, null);
        return;
      }
      else
      {
        // transition from SYNCEVIT to PARTIAL
        // largest queue : ASYNC_EVICT
        // other queus : NORMAL
        for (IEvictPolicyCallback cb : m_callbacks)
        {
          cb.evictionTriggered(src, this, (cb == maxcb) ? cmd : SpillCmd.SET_NORMAL, null);
        }
        return;
      }
    }
    for (IEvictPolicyCallback cb : m_callbacks)
    {
      cb.evictionTriggered(src, this, cmd, null);
    }
  }

  public void stopEvictor()
  {
    for (IEvictPolicyCallback cb : m_callbacks)
    {
      cb.stop();
    }
  }
}
