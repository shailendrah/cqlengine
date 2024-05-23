/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/memmgr/evictPolicy/BasePolicy.java /main/14 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION

  BasePolicy provides the base implementation of Policy for RandomHalf and Half
  algorithm.  Once we have a good and concrete policy algorithm, it will move from
  EvictPolicyManager.

  BasePolicy will decide when to evict and RandomHalf/Half will decide what to 
  evict.
  
  - Invocation
  Currently, runEvictor is called in two places:
  1) EvictableStorageElementFactory.newStorageElement 
  2) A thread in BasePolicy every 1 minute

  - Parameters  
  The parameters are as follows:
  count - number of invocation of runEvictor
    this parameter is to make sure that some work is done before we start
    evicting.
    count0 - count threshold in initial mode
    count1 - count threshold in normal mode
  memThreshold - the size of free memory
    memThreshold0 - memory threshold in initial mode
    memThreshold1 - memory threshold in normal mode
    memThreshold2 - maximum memory threshold when eviction happens regardless
                    of count
  
  - Algorithm                    
  0) Basic algorithm
    If the number of count is less than the count threshold and the size 
    of free memory is below memThreshold2, it returns. 
    If the size of free memory is below memory Threshold, it returns.
    Otherwise, start eviction.
    e.g.
        if ((freemem > memThreshold2) &&
           (countchk && count < countTh)) return;
        if (freemem > mthreshold) return;

  1) Initial mode
    Before the first eviction happens, it is called 'initial mode' and 
    we use parameters for initial mode (count0, memThreshold0).
    In initial mode, the algorithm utilizes the following observations
    - BerkeleyDB will start allocating a cache 
    - There are lots of allocation for tuple, timestamp from StreamSource
    We want to make sure that there is enough working memory
    left for the storage system (e.g. BerkeleyDB) while maximizing the 
    utilization of utilizing the memory.
    So the initial memory threshold is 'normal memory threshold' + cache size of
    the storage system.
    By default, 10% of total memory is set to the BerkeleyDB.
    
  2) Normal mode  
    After the first eviction happens, we use parameters for normal mode
    (count1, memThreshold1).    


   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/03/11 - Storage refactor
    hopark      03/04/09 - fix max memory
    hopark      01/21/09 - set thread name
    hopark      12/02/08 - move LogLevelManager to ExecContext
    hopark      10/10/08 - remove statics
    hopark      09/12/08 - add schema indexing
    hopark      06/18/08 - logging refactor
    hopark      04/16/08 - fix parse parm
    hopark      04/16/08 - fix parse parm
    hopark      03/08/08 - add callback
    hopark      10/15/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/memmgr/evictPolicy/BasePolicy.java /main/13 2010/07/08 11:42:23 apiper Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr.evictPolicy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import oracle.cep.execution.ExecException;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.ILogLevelManager;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.logging.trace.LogTags;
import oracle.cep.memmgr.EvictStat;
import oracle.cep.memmgr.IEvictPolicy;
import oracle.cep.memmgr.IEvictPolicyCallback;
import oracle.cep.memmgr.IAllocator.NameSpace;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.service.CEPManager;
import oracle.cep.storage.IStorage;
import oracle.cep.storage.StorageException;
import oracle.cep.util.HeapDump;
import oracle.cep.util.StringUtil;

public abstract class BasePolicy implements IEvictPolicy
{
  public static final int MODE_NORMAL = 0;    //Run evictor in sync and async
  public static final int MODE_DEBUG = 1;     //Run evictor in debug mode using debug count
  public static final int RUNTYPE_FACTORY = 1;   // invoked from factory
  public static final int RUNTYPE_SCHEDULER = 2;      //invoked from scheduler
  public static final int RUNTYPE_BACKGROUND = 4;     //invoked from background
  
  @DumpDesc(ignore=true) protected EvictStat stat;
  @DumpDesc(ignore=true) protected SimpleDateFormat formatter;
  @DumpDesc(ignore=true) protected Runtime runtime = Runtime.getRuntime();
  protected boolean running;
  protected int  runType;
  protected int count;
  protected int runCount;
  protected int evcount;
  @DumpDesc(ignore=true) protected boolean  test;
  @DumpDesc(ignore=true) protected boolean testWait;
  
  @DumpDesc(ignore=true) protected long totalMem;
  @DumpDesc(ignore=true) protected IStorage storage;
  @DumpDesc(ignore=true) protected CEPManager cepMgr;
  
  public BasePolicy()
  {
    stat = new EvictStat();
    formatter = new SimpleDateFormat("HH:mm:ss");
    count = 0;
    runCount = 0;
    totalMem = Runtime.getRuntime().maxMemory();
  }

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
    
  protected void start()
  {
    if ( (runType & RUNTYPE_BACKGROUND) != 0)
    {
      running = true;
      running = true;
      new Thread(new Runnable() {
        public void run() {
          try
          {
            while (running) {
              try {
                if (needEviction(Source.Background))
                {
                  runEvictor(Source.Background);
                }
             }
             catch(Exception e) 
             {
               LogUtil.fine(LoggerType.TRACE, e.toString());
             }
              Thread.sleep(60 * 1000); 
            }
         }
         catch(InterruptedException ex) {}
        }
      }, "Evictor").start();
    }
  }
  
  public void stopEvictor()
  {
    running = false;        
  }

  public boolean isUsingCallback() {return false;}
  
  protected long invokeGC() 
  {
      long lstarttime = System.currentTimeMillis();
      long freeMem0 = runtime.freeMemory();
      long freeMem1 = freeMem0, freeMem2 = 0;
      for (int i = 0; (freeMem1 > freeMem2) && (i < 3); ++ i)
      {
        runtime.runFinalization();
        runtime.gc();
        Thread.yield();
        freeMem2 = freeMem1;
        freeMem1 = runtime.freeMemory();
      }
    long lendtime = System.currentTimeMillis();
    long difftime = lendtime - lstarttime;
    stat.addGC(difftime);
    LogLevelManager.trace(LogArea.SPILL, LogEvent.SPILL_GC, this,
        "GC : " + memStr(freeMem0) + " -> " + memStr(freeMem1) +
        " , " + timeStr(difftime));      
    return freeMem1;
  }

  public static String memStr(long mem)
  {
     int m = (int) (mem / 1000000f);
     return m + "m";
  }
  
  protected String timeStr(long tm)
  {
    long s = tm / 1000;
    long m = s / 60;
    long h = m / 60;
    s = s % 60;
    m = m % 60;
    tm = tm % 1000;
    return h + ":" + m + ":" + s + "." + tm;
 }
  
  protected void heapdump(String prefix)
  {
    String filename = System.getProperty("java.io.tmpdir");
    filename += "/cep/heapdump"+ runCount+ "_" + prefix + ".hprof";
    HeapDump.dumpHeap(filename);
    System.out.println("Heap dumped to "+ filename);
    if (testWait)
    {
      try {
        System.out.println( "Press enter to start." );
        System.out.flush();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        in.readLine();
        System.out.println( "Ok." );
        System.out.flush();
      } catch (IOException e)
      {
      }
    }
  }

  public EvictStat getStat() {return stat;}
  
  public synchronized void forceEvict() throws ExecException
  {
    long starttime = System.currentTimeMillis();
    stat.resetCollectGc();
    long freemem = runtime.freeMemory();
    evcount = 0;

    evict();
    long newFreeMem = invokeGC();

    long endtime = System.currentTimeMillis();
    long difftime = (endtime - starttime);
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    String EndTimeStr = formatter.format(new Date());
    String name = StringUtil.getBaseClassName(this);
    String endstr = EndTimeStr + " " +  name + "(" + runCount + ") ended : total " + evcount + " objects evicted : free = " + memStr(freemem) + " -> " + memStr(newFreeMem) + " " + timeStr(difftime);
    LogLevelManager.trace(LogArea.SPILL, LogEvent.SPILL_EVICT_END, this, endstr);
    if (test) 
    {
      heapdump("forceEvict");
    }
  }
  
  public int getTargetId()
  {
    return 0;
  }

  public String getTargetName()
  {
    return StringUtil.getBaseClassName(this);
  }

  /* (non-Javadoc)
   * @see oracle.cep.logging.ILoggable#getTargetType()
   */
  public int getTargetType()
  {
    return -1;
  }

  public ILogLevelManager getLogLevelManager()
  {
    return CEPManager.getInstance().getLogLevelManager();
  }
    
  public synchronized void dump(IDumpContext dump) {}

  public void trace(IDumpContext dumper, ILogEvent event, int level, Object[] args)
  {
    switch(level)
    {
      case LogLevel.SPILL_STAT:
        LogUtil.logTagVal(dumper, LogTags.STAT, getStat());
        break;
    }
  }
  protected void evict() throws ExecException {}
  public void addCallback(IEvictPolicyCallback cb) {}
  public void removeCallback(IEvictPolicyCallback cb) {}
}
