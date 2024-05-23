/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/scheduler/SchedulerManager.java /main/30 2010/06/09 22:13:58 sbishnoi Exp $ */

/* Copyright (c) 2007, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    05/20/10 - adding new method to handle hbttimeout list
 sbishnoi    05/15/09 - generalize the signature of requestForHeartbeat
 anasrini    05/10/09 - notify on stop
 anasrini    05/07/09 - system timestamped source lineage
 hopark      03/01/09 - clean circular eferences
 parujain    01/28/09 - txn mgmt
 anasrini    01/17/09 - make execContext protected
 hopark      11/22/08 - change scheduler info debug level
 hopark      11/22/08 - remove System.out
 hopark      10/10/08 - remove statics
 hopark      10/09/08 - remove statics
 hopark      10/07/08 - use execContext to remove statics
 udeshmuk    10/01/08 - conditionally enable logging of operator stats.
 udeshmuk    09/17/08 - 
 anasrini    09/12/08 - print exec stats after sched run
 hopark      03/18/08 - reorg config
 parujain    04/17/08 - shutdown problem
 najain      03/25/08 - fix bugs
 udeshmuk    03/18/08 - restructure run method code
 udeshmuk    12/18/07 - restructure run method code.
 sbishnoi    12/13/07 - PausableThreadPoolExec will do waitResume only if
                        evictPolicy is notnull
 udeshmuk    11/27/07 - print schedule for debugging.
 hopark      11/26/07 - no getNext if eviction is in progress
 hopark      10/12/07 - add pause, invoke eviction
 parujain    10/10/07 - fix no of threads for stats
 najain      07/31/07 - debug linear road
 anasrini    07/26/07 - fix for fabric
 najain      07/09/07 - remove SCHED_SCHEDULER
 skmishra    06/23/07 - scheduler fix
 parujain    05/03/07 - scheduler stats
 parujain    03/21/07 - scheduler on new thread
 parujain    03/21/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/scheduler/SchedulerManager.java /main/30 2010/06/09 22:13:58 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.scheduler;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.management.ThreadInfo;

import oracle.cep.memmgr.IEvictPolicy;
import oracle.cep.memmgr.IEvictPolicy.Source;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;
import oracle.cep.common.Constants;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.execution.ExecManager;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.ExecOptTask;

public class SchedulerManager implements Runnable
{
  /** When set to true the operator stats will be printed */
  private boolean printStats = true;  

  private long               runTime;
  enum State {
    NOT_RUNNING, RUNNING, STOPPED 
  }
  private State              state = State.NOT_RUNNING;
  private int                stateHistory = 0;
  protected boolean          stopRequest = false;
  private Scheduler          sched;
  protected ExecContext      execContext; //execution context that this scheduler manager belongs to.
  private ExecManager        execMgr;
  
  private ArrayBlockingQueue<Runnable> workQueue;
  
  private PausableThreadPoolExecutor pThreadPool = null;
  
  private ThreadPoolExecutor         threadPool  = null;

  private ReentrantLock      lock ;
  
  private Condition          notEmpty;
    
  private int                outputCnt;
  
  /**
   * This variable keeps track of the number of operators that are either
   * executing or are scheduled for execution at any given point in time.
   */
  public AtomicInteger       opRunningOrScheduledCnt;

  public void incOutputCnt() 
  { 
    outputCnt++; 
  }

  private class CallerBlocksPolicy implements RejectedExecutionHandler
  {
    private BlockingQueue<Runnable> blkQueue;

    public void rejectedExecution(Runnable rTask, ThreadPoolExecutor tpExec)
    {
      if (blkQueue == null)
      {
        blkQueue = tpExec.getQueue();
      }
      try
      {
        // block until space becomes available
        blkQueue.put(rTask);
      }
      catch (InterruptedException e)
      {
        // someone is trying to interrupt us
        throw new RejectedExecutionException(e);
      }
    }
  }

  // WARNING !!!!!!!
  // This is temporary. Please do not use this.
  // This will be removed once the DirectInterop scheduler takes over
  public SchedulerManager()
  {
  }

  public SchedulerManager(ExecContext ec)
  {
    //create an AtomicInteger with initial value 0
    opRunningOrScheduledCnt = new AtomicInteger();
    execContext = ec;
    execMgr = ec.getExecMgr();
    outputCnt = 0;
  }

  public void init()
  {
    CEPManager    cepMgr = execContext.getServiceManager();
    ConfigManager cfgMgr = cepMgr.getConfigMgr();
    int           num    = cfgMgr.getSchedNumThreads();
    
    workQueue = new ArrayBlockingQueue<Runnable>(
        cfgMgr.getSchedThreadPoolQSize());
    CallerBlocksPolicy blkPolicy = new CallerBlocksPolicy();
    pThreadPool = new PausableThreadPoolExecutor(num, num, 0,
        TimeUnit.MICROSECONDS, workQueue, blkPolicy);
    
    if(cepMgr.getEvictPolicy() != null)
      threadPool = pThreadPool;
    else
      threadPool = new ThreadPoolExecutor(num, num, 0, TimeUnit.MICROSECONDS,
        workQueue, blkPolicy);

    lock     = execMgr.getOperatorLock();
    notEmpty = execMgr.getOperatorLockCondition();
  }

  public synchronized void setRunTime(long time)
  {
    this.runTime = time;
  }
  
  public void stop()
  {
    try
    {
      synchronized(this)
      {
        stopRequest = true;
        notify();
      }
      while (isRunning())
      {
        synchronized(this)
        {
          wait(2000);
        }
      }
      synchronized(this)
      {
        stateHistory = 0;
      }
    } catch(InterruptedException e)
    {
    }
  }
  
  protected void setState(State st)
  {
    synchronized(this)
    {
      int f = ((state == State.RUNNING) ? 1:0);
      stateHistory = (stateHistory << 1) | f;

      state = st;
      notifyAll();
      LogUtil.fine(LoggerType.TRACE, "Scheduler State : " + state.toString());
    }  
    
    // update the system state to running
//    try
//    {
//      CEPManager cepMgr = execContext.getServiceManager();
//      cepMgr.getSystemMgr()
//      .updateSystemState(st == State.RUNNING ? 
//            SystemState.SCHEDULER_RUNNING:
//            SystemState.SCHEDULER_STOPPED);
//    }
//    catch(MetadataException me)
//    {
//      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, me);
//    }
  }
  
  public void run()
  {

    try {

    synchronized(this)
    {
      if (stopRequest)
      {
        LogUtil.fine(LoggerType.TRACE, "Scheduler stop requested.");
        stopRequest = false;
        setState(State.STOPPED);

        LogUtil.fine(LoggerType.TRACE, getThreadInfo() + " SM: Stopped.");
        return;
      }
    }
    try
    {
      CEPManager cepMgr = execContext.getServiceManager();
      sched = cepMgr.getConfigMgr().getScheduler();
    }
    catch(Exception e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      sched = null;
    }
    LogUtil.fine(LoggerType.CUSTOMER, "Scheduler = "+  (sched == null ? "null":
        (sched.getClass().getName() + ":" + sched.hashCode() ) ) );
    if (sched == null)
    {
      setState(State.STOPPED);
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, 
          new CEPException(InterfaceError.NAME_NOT_FOUND));
      return;
    }
    execMgr.setSched(sched);

    setState(State.RUNNING);

      if (runTime == 0)
      { //infinitely running SM
        LogUtil.fine(LoggerType.TRACE, getThreadInfo() + " SM: Infinitly running : " + runTime);
        for (;;)
        {
          synchronized(this)
          {
            if (stopRequest)
            {
              LogUtil.fine(LoggerType.TRACE, "Scheduler stop requested.");
              stopRequest = false;
              break;
            }
          }
          if(threadPool instanceof PausableThreadPoolExecutor)
            pThreadPool.waitResume();
     
          Runnable    nxt    = sched.getNext(execContext);
          ExecOptTask task;
          ExecOpt     execop;

          if (nxt == null)
          { //no operator found to schedule
            if (opRunningOrScheduledCnt.get() == 0)
            { /* 
               * No operator is executing or is scheduled for execution
               * right now. Call getNext() one more time as the last run
               * operator may have generated input data for some other
               * operators making them ready for run() after we called 
               * getNext() the last time.
               */
              nxt    = sched.getNext(execContext);

              if (nxt == null)
              { // no such operator exists, so start waiting
                lock.lock();
                try 
                { 
                  // wait for finite time so that heartbeat propagation
                  // does not halt
                  notEmpty.await(Constants.INFINITE_SCHED_WAIT_TIME, 
                                 TimeUnit.MILLISECONDS);
                }
                catch(Exception e) {}
                finally 
                {
                  lock.unlock();
                } 
              }
              else
              { // found such an operator, so set the boolean,
                // increment the counter and then execute
                task   = (ExecOptTask)nxt;
                execop = task.getExecOp();

                execop.isScheduled.set(true);
                opRunningOrScheduledCnt.incrementAndGet();
                threadPool.execute(nxt);
              }
            }
          }
          else 
          { // found some operator to execute so set the boolean,
            // increment the counter and then execute
            task   = (ExecOptTask)nxt;
            execop = task.getExecOp();

            execop.isScheduled.set(true); 
            opRunningOrScheduledCnt.incrementAndGet();
            threadPool.execute(nxt);
          }
        }
      }
      else
      { //finitely running SM
        if(printStats)
          execMgr.printSched();
        long i=0;
        LogUtil.fine(LoggerType.TRACE, getThreadInfo() + " SM: Starting runtime value : " + runTime);
        for (;;)
        {
          synchronized(this)
          {
            if (stopRequest)
            {
              LogUtil.fine(LoggerType.TRACE, "Scheduler stop requested.");
              stopRequest = false;
              break;
            }
          }
          if (i == runTime)
            break;
          if(threadPool instanceof PausableThreadPoolExecutor)
            pThreadPool.waitResume();
          
          Runnable    nxt    = sched.getNext(execContext);
          ExecOptTask task;
          ExecOpt     execop;

          if (nxt == null)
          { // no operator found to schedule
            if (opRunningOrScheduledCnt.get() == 0)
            { /* 
               * No operator is executing or is scheduled for execution
               * right now. Call getNext() one more time as the last run
               * operator may have generated input data for some other
               * operators making them ready for run() after we called 
               * getNext() the last time.
               */
              nxt    = sched.getNext(execContext);

              if (nxt == null)
              { // no such operator exists, so quit
                break;
              }
              else
              { 
                // found such an operator, so set the boolean,
                // increment the counter and then execute
                task   = (ExecOptTask)nxt;
                execop = task.getExecOp();

                execop.isScheduled.set(true);
                opRunningOrScheduledCnt.incrementAndGet();
                threadPool.execute(nxt);
                i++;
              }
            }
          }  
          else 
          { 
            // found some operator to execute so set the boolean,
            // increment the counter and then execute
            task   = (ExecOptTask)nxt;
            execop = task.getExecOp();

            execop.isScheduled.set(true);
            opRunningOrScheduledCnt.incrementAndGet();
            threadPool.execute(nxt);
            i++;
          }
        }

        // wait for all operators to finish in case they are still running
        while (opRunningOrScheduledCnt.get() != 0);
      }

      if(printStats)
        execMgr.printSched();
      setState(State.STOPPED);

    LogUtil.fine(LoggerType.TRACE, getThreadInfo() + " SM: Completed.");

    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private String getThreadInfo()
  {
    StringBuilder b = new StringBuilder();
    String n = Thread.currentThread().getName();
    if (n != null  || n.length() > 0)
    {
      b.append(n);
    }
    b.append("(");
    b.append(Long.toString(Thread.currentThread().getId()));
    b.append(") ");
    b.append(execContext.getServiceName());
    return b.toString();
  }

  public void notifyOnSourceOpAddition(ExecOpt e)
  {
  }

  public void notifyOnSourceOpDrop(ExecOpt e)
  {
  }
  
  /**
   * Add or Subtract the given operator from the list of heartbeat timeout
   * operators 
   * @param e
   * @param isAdd
   */
  public void updateHbtTimeOutList(ExecOpt e, boolean isAdd)
  {  
  }

  public void requestForHeartbeat(Collection<ExecOpt> sourceList, long hbtTime)
  {
  } 

  //This method is not called generally.
  //It can be used to get thread statistics.
  private void printThreadStats() {

    long nano = 1000000000L;

    ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
    if ( ! bean.isThreadCpuTimeSupported( ) )
      return;

    long   cpuTime  = 0L;
    long   userTime = 0L;
    long   sysTime  = 0L;
    long[] ids  = bean.getAllThreadIds();

    System.out.println("");
    System.out.println("Thread Statistics : ");
    System.out.println("Total Number of threads : " + ids.length);

    for (int i=0; i<ids.length; i++ ) {
      long c = 0L;
      long u = 0L;
      long s = 0L;
      long blocked = -1L;

      ThreadInfo tinfo = bean.getThreadInfo(ids[i]);
      String     tname = tinfo.getThreadName();

      u = bean.getThreadUserTime(ids[i]);
      if (u != -1) {
        userTime += u;
      }

      c = bean.getThreadCpuTime(ids[i]);
      if (c != -1) {
        cpuTime += c;
        s = c - u;
        sysTime += s;

        if (bean.isThreadContentionMonitoringSupported()) {
          blocked = tinfo.getBlockedTime();
        }

        System.out.println("Thread " + ids[i] + " : " + tname + " : " +
                           c/nano + "(cpu) : " + 
                           u/nano + "(user) : " +
                           s/nano + "(sys) : " +
                           blocked + "(blocked)");
      }
    }

    System.out.println("Totals : " + cpuTime/nano + "(cpu) : " + 
                       userTime/nano + "(user) : " +
                       sysTime/nano + "(sys)");


    System.out.println("");

  }
  
  public synchronized int getRunHistory() {return stateHistory;}
  
  /**
   * Return true if scheduler(heartbeat) is running
   * @return
   */
  public synchronized boolean isRunning() 
  { 
    return state == State.RUNNING;
  }
  
  /**
   * Return true always as this scheduler implementation doesn't run
   * any regress test.
   */
  public boolean isRegressTestDone()
  {
    return true;
  }
  
  public int getNumThreads()
  {
    if (threadPool != null)
      return(threadPool.getPoolSize());
    
    return 0;
  }

  public void shutdown()
  {
      stop();
      
      if(threadPool != null)
      {
        threadPool.shutdown();
      }
      
      workQueue = null;
      pThreadPool = null;
      threadPool = null;
      execContext = null;
      execMgr = null;
      sched = null;
  }
  
  public Runnable[] getQueued()
  {
    if (workQueue == null)
      return null;

    synchronized(workQueue)
    {
      Runnable[] res = new Runnable[workQueue.size()];
      return workQueue.toArray(res);
    }
  }
  
  class PausableThreadPoolExecutor extends ThreadPoolExecutor 
  {
    private boolean isPaused;
    private ReentrantLock pauseLock = new ReentrantLock();
    private Condition unpaused = pauseLock.newCondition();
    private List<Runnable> runningList;
    private IEvictPolicy evictPolicy;
    
    
    public PausableThreadPoolExecutor(int corePoolSize, 
                                      int maximumPoolSize, 
                                      long keepAliveTime, 
                                      TimeUnit unit, 
                                      BlockingQueue<Runnable> workQueue, 
                                      RejectedExecutionHandler handler) 
    { 
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit,
            workQueue, handler); 
      runningList = new LinkedList<Runnable>();
      CEPManager    cepMgr = execContext.getServiceManager();
      evictPolicy = cepMgr.getEvictPolicy();
    }
  
    protected void beforeExecute(Thread t, Runnable r) 
    {
      super.beforeExecute(t, r);

      if (evictPolicy != null)
      {
        //only one thread invoke this
        synchronized(evictPolicy)
        {
          if (evictPolicy.needEviction(Source.Scheduler))
          {
            pause();
            evictPolicy.runEvictor(Source.Scheduler);
            resume();
          }
        }
      }
      
      // If the pause flag is on, wait until it's resumed.
      pauseLock.lock();
      try {
        while (isPaused) unpaused.await();
      } catch(InterruptedException ie) {
        t.interrupt();
      } finally {
        pauseLock.unlock();
      }
      
      synchronized(runningList)
      {
        runningList.add(r);
        runningList.notifyAll();
      }
    }
  
    protected void afterExecute(Runnable r, Throwable e)
    {
      super.afterExecute(r, e);
      synchronized(runningList)
      {
        runningList.remove(r);
        runningList.notifyAll();
      }
    }
    
    public void pause() 
    {
      pauseLock.lock();
      try {
        isPaused = true;
      } finally {
        pauseLock.unlock();
      }
      // wait until all running operator is done.
      // we cannot use getActiveCount for this 
      // because the javadoc says
      // it returns the 'APPROXIMATE' number of threads.
      synchronized(runningList)
      {
        while (runningList.size() > 0)
        {
          try
          {
            runningList.wait();
          }
          catch(InterruptedException ie) 
          {
          }
        }
      }
    }
  
    public void resume() 
    {
      pauseLock.lock();
      try {
        isPaused = false;
        unpaused.signalAll();
      } finally {
        pauseLock.unlock();
      }
    }
    
    public void waitResume()
    {
      // If the pause flag is on, wait until it's resumed.
      pauseLock.lock();
      try {
        while (isPaused) unpaused.await();
      } catch(InterruptedException ie) {
      } finally {
        pauseLock.unlock();
      }
    }
  }
  
}
