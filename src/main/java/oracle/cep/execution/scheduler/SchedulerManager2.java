/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/scheduler/SchedulerManager2.java /main/17 2013/05/07 18:03:18 sbishnoi Exp $ */

/* Copyright (c) 2008, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    11/22/11 - fixing heartbeat timeout, need to notify waiting
                           thread
    anasrini    03/21/11 - use tableSource.getName
    sbishnoi    04/26/10 - moving HBT_TIMEOUT to Constants
    sbishnoi    05/15/09 - generalize the signature of requestForHeartbeat
    sbishnoi    05/11/09 - commenting the println statement
    anasrini    05/07/09 - system timestamped source lineage
    hopark      03/01/09 - clean circular eferences
    anasrini    02/13/09 - support heartbeat timeout
    parujain    01/28/09 - txn mgmt
    anasrini    01/21/09 - fix high CPU utilization
    anasrini    01/17/09 - runtime exception handling
    hopark      11/22/08 - change scheduler info debug level
    udeshmuk    11/17/08 - handle runtime!=0 case
    udeshmuk    11/17/08 - handle runtime!=0 case
    anasrini    11/12/08 - Direct Interop scheduler
    anasrini    11/12/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/scheduler/SchedulerManager2.java /main/17 2013/05/07 18:03:18 sbishnoi Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.scheduler;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Set;

import oracle.cep.common.Constants;
import oracle.cep.service.ExecContext;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.ExecSourceOpt;
import oracle.cep.interfaces.input.TableSource;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;


public class SchedulerManager2 extends SchedulerManager implements Runnable
{

  private static class HbtRequest
  {
    // The source operator to request a heartbeat
    ExecSourceOpt op;

    // The time at which heartbeat is sought
    long          hbtTime;

    HbtRequest(ExecSourceOpt op, long hbtTime)
    {
      this.op      = op;
      this.hbtTime = hbtTime;
    }
  }

  /** This indicates whether we are running regression tests */
  private boolean                       isRegressPushMode;

  /** These are used only for regression testing */
  private int                           size;
  private Thread[]                      t;
  private String[]                      opNames;

  private HashMap<Integer, TableSource> sourceList;
  private HashMap<Integer, TableSource> hbtTimeoutList;


  /** The last timestamp when round of heartbeat timeout processing finished */
  private long                           lastHbtTimeout = 0L;
   
  /** Number of sources that require heartbeat timeouts */
  private long                           numHbtTimeoutRequests = 0;

  /** 
   * Number of times source add/drop notifications have been recvd
   * where source required heartbeat timeouts
   */
  private long                           numHbtNotificationsOld = 0;
  private long                           numHbtNotificationsNew = 0;

  /**
   * List of explicit heartbeat requests (these are typically made by
   * binary operators when one queue is empty and the other has an element
   * waiting for progress of time
   */
  private List<HbtRequest>               hbtRequestList;

  private boolean                        hbtRequestProcessing = false;
  private boolean                        hbtTimeoutProcessing = false;

  /**
   * Constructor
   * @param ec execution context for this service
   */
  public SchedulerManager2(ExecContext ec)
  {
    super(ec);
    sourceList     = new HashMap<Integer, TableSource>();
    hbtTimeoutList = new HashMap<Integer, TableSource>();
    hbtRequestList = new LinkedList<HbtRequest>();
  }

  /**
   * Initialization method
   */
  public void init()
  {
  }
  
  /**
   * Main scheduler run method
   */
  public void run()
  {
    LogUtil.info(LoggerType.TRACE, "SchedulerManager (DI mode) : Started");

    isRegressPushMode 
      = execContext.getServiceManager().getConfigMgr().isRegressPushMode();

    setState(State.RUNNING);

    if (isRegressPushMode)
      runRegressPushMode();

    handleHeartbeats();

    setState(State.STOPPED);
    LogUtil.info(LoggerType.TRACE, "SchedulerManager (DI mode) : Stopped");
  }


  /**
   * This method is called only during REGRESSION testing.
   * This is used to emulate push mode even though tests use pull mode
   * sources like files.
   */
  public void runRegressPushMode()
  {
    TableSource   s;
    Set<TableSource> srcs = new HashSet<TableSource>();

    LogUtil.info(LoggerType.TRACE,
                 "****** RUNNING DIRECTINTEROP REGRESS ******");

    srcs.addAll(sourceList.values());

    size    = srcs.size();
    t       = new Thread[size];
    opNames = new String[size];
   
    Iterator<TableSource> iter = srcs.iterator();
    for(int i = 0; iter.hasNext(); i++)
    {
      s          = iter.next();
      opNames[i] = s.getName();
      t[i] = new Thread(s, opNames[i]);
      t[i].start();
      LogUtil.info(LoggerType.TRACE, "Starting thread : " +  opNames[i]);
    }
  }
   
  @Override
  public boolean isRegressTestDone()
  {
    assert isRegressPushMode : "This should be only called during regressions";
    for(int i = 0; i < size; i++)
    {
      if (t[i] != null && t[i].isAlive())
        return false;
      else
      {
        if (t[i] != null)
        {
          LogUtil.info(LoggerType.TRACE, "Thread : " + opNames[i] 
                       + " : completed");
          t[i] = null;
        }
      }
    }
    return true;
  }

  /**
   * This serves as the TIMER thread to support heartbeat timeouts for those
   * streams / relations that require them. Stream and relations that are 
   * part of queries involving time based range windows or partition by
   * range windows or pattern operators with non-event detection would 
   * require heartbeat timeouts to be setup in case stream/relation is 
   * system-timestamped
   */
  public void handleHeartbeats()
  {

    LogUtil.info(LoggerType.TRACE,
                 "*** DirectInterop TIMER thread for heartbeat " + 
                 "requests/timeouts ***");

    Set<TableSource> srcs = new HashSet<TableSource>();
    
    lastHbtTimeout = System.currentTimeMillis();

    while(true)
    {
      synchronized(this)
      {
        while (
               (numHbtTimeoutRequests == 0 || 
                System.currentTimeMillis() - lastHbtTimeout 
                 < Constants.DEFAULT_HBT_TIMEOUT_MILLIS) &&
                hbtRequestList.isEmpty() &&
                !stopRequest &&
               (!isRegressPushMode || !isRegressTestDone())
              )
        {
          try 
          {
            if (numHbtTimeoutRequests > 0)
            {
              long timeout = 
                lastHbtTimeout + Constants.DEFAULT_HBT_TIMEOUT_MILLIS
                - System.currentTimeMillis();
              if (timeout > 0)
                wait(timeout);
            }
            else if (isRegressPushMode)
              wait(100);
            else
              wait();
          }
          catch(InterruptedException ie) {}
          catch(IllegalArgumentException iae) {}
        }
         
        if (stopRequest)
        {
          stopRequest = false;
          setState(State.STOPPED);
          return;
        }


        hbtRequestProcessing = !(hbtRequestList.isEmpty());

        if (!(numHbtTimeoutRequests == 0 || 
              System.currentTimeMillis() - lastHbtTimeout < 
              Constants.DEFAULT_HBT_TIMEOUT_MILLIS))
        {
          hbtTimeoutProcessing = true;
          if (numHbtNotificationsNew != numHbtNotificationsOld)
          {
            srcs.addAll(hbtTimeoutList.values());
            numHbtNotificationsOld = numHbtNotificationsNew;
          }
        }
      }

      if (hbtRequestProcessing)
      {
        while(true)
        {
          TableSource t;
          HbtRequest  req;
          
          synchronized(hbtRequestList)
          {
            if(!hbtRequestList.isEmpty())
              req = hbtRequestList.remove(0);
            else
              break;
          }
        
          t = req.op.getSource();
          
          if (t != null)
          {
            /**
            System.out.println("Request hb for " +
                               ((ExecOpt)(req.op)).getOptName() +
                               " at " + req.hbtTime);
            */
            t.requestForHeartbeat(req.hbtTime);
          }
        }
        hbtRequestProcessing = false;
      }

      if (isRegressPushMode && isRegressTestDone())
        return;

      if (hbtTimeoutProcessing)
      {
        for (TableSource src : srcs)
          src.hbtTimeoutReminder();
        
        if (srcs.size() > 0)
          lastHbtTimeout = System.currentTimeMillis();

        hbtTimeoutProcessing = false;
      }
    }
  }

  public synchronized void requestForHeartbeat(Collection<ExecOpt> sourceList,
                                               long hbtTime)
                                        
  {
    Iterator<ExecOpt> iter;

    if (sourceList == null)
      return;

    synchronized(hbtRequestList) 
    {
      iter = sourceList.iterator();
      while (iter.hasNext())
      {
        hbtRequestList.add(new HbtRequest((ExecSourceOpt)(iter.next()),
                                          hbtTime));
      }

      notify();
    }
    
  } 

  public void shutdown()
  {
    super.shutdown();

    sourceList = null;
    hbtTimeoutList = null;
  }
  
  /**
   * This method is called to notify the scheduler when a new source operator
   * is added to the list of running operators 
   *
   * @param e the newly added execution operator 
   */
  public synchronized void notifyOnSourceOpAddition(ExecOpt e)
  {
    ExecSourceOpt sop;
    TableSource   s;
    
    assert e instanceof ExecSourceOpt : e.getOptName() + " not a source op";

    e.setTsSlice(1);

    sop = (ExecSourceOpt)e;
    s   = sop.getSource();

    // Do not register if this is set up as a  push source
    if (s.getInnerSource() != null)
      sourceList.put(e.getId(), s);
      
    // If given source is required to send a heartbeat timeout
    // then add this source entry into the designated list
    addToHbtTimeoutList(e);
    notify();
  }

  /**
   * This method is called to notify the scheduler when a source operator
   * is removed from the list of running operators 
   *
   * @param e the execution operator being removed
   */
  public synchronized void notifyOnSourceOpDrop(ExecOpt e)
  {
    if (sourceList.get(e.getId()) != null)
      sourceList.remove(e.getId());
    
    // If the given source operator is in the heartbeat timeout list
    // then we need to update the list by removing this operator
    removeFromHbtTimeoutList(e);    
    notify();
  }
  
  /**
   * Add or Subtract the given operator from the list of heartbeat timeout
   * operators 
   * @param e
   * @param isAdd
   */
  public void updateHbtTimeOutList(ExecOpt e, boolean isAdd)
  {
    if(isAdd)  
      addToHbtTimeoutList(e);
    else
      removeFromHbtTimeoutList(e);
  }
  
  public synchronized void addToHbtTimeoutList(ExecOpt e)
  {
    if(e != null)
    {
      ExecSourceOpt sop = (ExecSourceOpt)e;;
      TableSource   s   = sop.getSource();      
      
      if (sop.requiresHbtTimeout())
      {
        if(hbtTimeoutList.get(e.getId()) == null)
        {
          numHbtTimeoutRequests++;
          numHbtNotificationsNew++;
          hbtTimeoutList.put(e.getId(), s);
          LogUtil.info(LoggerType.TRACE,
                     "*** Heartbeat timeout request for " +
                     e.getOptName());
        }        
        notify();
      }
    }
  }
  
  public synchronized void removeFromHbtTimeoutList(ExecOpt e)
  {
    if(e != null)
    {
      if (hbtTimeoutList.get(e.getId()) != null)
      {
        hbtTimeoutList.remove(e.getId());
        numHbtTimeoutRequests--;
        numHbtNotificationsNew++;
        LogUtil.info(LoggerType.TRACE,
                     "***Cancelling Heartbeat timeout request for " +
                     e.getOptName());
      }
    }
  }
  
}
