/* $Header: pcbpel/cep/server/src/oracle/cep/execution/queues/Queue.java /main/17 2009/05/12 19:25:47 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares Queue in package oracle.cep.execution.queues.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 parujain  05/04/09 - lifecycle management
 hopark    12/02/08 - move LogLevelManager to ExecContext
 hopark    06/18/08 - logging refactor
 hopark    04/17/08 - add stat
 hopark    03/12/08 - use IQueue
 hopark    12/27/07 - support xmllog
 hopark    12/27/07 - support xmllog
 hopark    10/15/07 - add evict
 hopark    10/30/07 - remove IQueueElement
 hopark    10/25/07 - remove QueueElement
 hopark    08/03/07 - structured log
 hopark    06/14/07 - add phyId
 hopark    06/11/07 - logging - remove ExecContext
 hopark    05/24/07 - logging support
 hopark    03/23/07 - throws exception
 najain    03/12/07 - bug fix
 hopark    03/07/07 - spill-over support
 najain    10/17/06 - add Id
 anasrini  03/24/06 - add getId 
 skaluska  02/07/06 - Creation
 skaluska  02/07/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/queues/Queue.java /main/17 2009/05/12 19:25:47 parujain Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.queues;

import java.util.concurrent.atomic.AtomicInteger;

import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILogLevelManager;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogTags;
import oracle.cep.memmgr.IEvictableObj;
import oracle.cep.service.ExecContext;
import oracle.cep.util.StringUtil;

/**
 * @author skaluska
 */
public abstract class Queue implements IQueue<QueueElement>, ILoggable, IEvictableObj
{
  protected ExecContext             execContext;
  
  /** queue id */
  protected int                     id;

  /** 
   * Id of the physical queue from which this is instantiated
   * This is very useful for debugging as it provides a starting point
   * to identify this queue in the global plan
   */
  protected int                     phyId;  
  
  protected static AtomicInteger nextId = new AtomicInteger(0);

  /** Empty Constructor for recreating queue from snapshot */
  public Queue() {}
  
  public Queue(ExecContext ec)
  {
    execContext = ec;
    id = nextId.getAndIncrement();
  }
  
  /**
   * Getter for id in Queue
   * 
   * @return Returns the id
   */
  public int getId()
  {
    return id;
  }

  /**
   * @return Returns the physical queue Id.
   */
  public int getPhyId()
  {
    return phyId;
  }

  /**
   * Setter for physical id
   * 
   * @param phyOptId
   *          The id of the corresponding physical queue to set.
   */
  public void setPhyId(int phyId)
  {
    this.phyId = phyId;
  }  
  
  /**
   * Evicts all elements in the underlying data structure
   *
   */
  public boolean evict() throws ExecException {return false;}

  /******************************************************************/
  // ILoggable implementation
  
  public int getTargetId() {return phyId;}
  public int getTargetType() {return -1;}
  public ILogLevelManager getLogLevelManager()
  {
    return execContext.getLogLevelManager();
  }
   
  public String getTargetName()
  {
    return StringUtil.getBaseClassName(this);
  }
  public abstract QueueStats getStats();

  public synchronized void dump(IDumpContext dump) 
  {
  }
  
  public void trace(IDumpContext dumper, ILogEvent event, int level, Object[] args)
  {
    if (level == LogLevel.QUEUE_STATS)
    {
      LogUtil.logTagVal(dumper, LogTags.STAT, getStats());
    }
  }

}
