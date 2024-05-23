/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/queues/stored/SimpleQueue.java /main/6 2009/12/05 13:43:53 hopark Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/02/09 - add maxLen in layout
    hopark      03/26/09 - log api change
    hopark      10/10/08 - remove statics
    hopark      04/13/08 - server reorg
    hopark      04/17/08 - add stat
    hopark      04/16/08 - add stat
    hopark      03/08/08 - creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/queues/stored/SimpleQueue.java /main/6 2009/12/05 13:43:53 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.queues.stored;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.logging.Level;

import oracle.cep.common.Datatype;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.queues.IQueue;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IEvictPolicy;
import oracle.cep.memmgr.IEvictPolicyCallback;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.memmgr.PageLayout;
import oracle.cep.memmgr.SimplePageManager;
import oracle.cep.memmgr.SimplePageManagerStat;
import oracle.cep.memmgr.IAllocator.NameSpace;
import oracle.cep.memmgr.IEvictPolicy.Source;
import oracle.cep.memmgr.PageLayout.LayoutDesc;
import oracle.cep.memmgr.SimplePageManager.BaseEntry;
import oracle.cep.memmgr.SimplePageManager.EntryGen;
import oracle.cep.memmgr.SimplePageManager.EntryRef;
import oracle.cep.memmgr.evictPolicy.QueueSrcPolicy;
import oracle.cep.service.CEPManager;

public class SimpleQueue<E> implements IQueue<E>, IEvictPolicyCallback
{
  // Page Layout
  static  class QEntry extends BaseEntry
  {
    Object      value;

    public QEntry()
    {
    }
    
    public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException
    {
      value = in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException
    {
      out.writeObject(value);
    }
  };
  
  private static final int      s_pageSize = 40000;
  private static final int      s_minObjsPage = 10;
  private static final EntryGen s_entryGen = new EntryGen() {
    public BaseEntry create() 
    {
      return new QEntry();
    }
  };
  
  private SimplePageManager pm;
  private int spillMode = SimplePageManager.NO_EVICT;
  private CEPManager cepMgr;
  
  /**
   * Constructor for SimpleQueue
   * @param cepMgr TODO
   */
  public SimpleQueue(CEPManager cepMgr, Datatype[] dtypes, int[] maxLens)
  {
    this.cepMgr = cepMgr;
    assert (dtypes != null);
    int len = dtypes.length;
    LayoutDesc layoutDesc =  new LayoutDesc(len, 0);
    layoutDesc.setPageSize(s_pageSize);
    layoutDesc.setMinObjs(s_minObjsPage);
    for (int i = 0; i < len; i++)
    {
      layoutDesc.setType(i, dtypes[i], maxLens[i]);
    }
    FactoryManager fm = cepMgr.getFactoryManager();
    int id = fm.getNextId();
    PageLayout pgLayout = PageLayout.create(id, layoutDesc);
    int nObjs = pgLayout.getNoObjs();
    
    pm = new SimplePageManager(FactoryManager.SQPAGE_FACTORY_ID, 
                              NameSpace.SQPAGE, 
                              nObjs, s_entryGen);    
  }

  public void initialize()
  {
  }

  public void stop()
  {
    pm.stop();
  }
  
  public int getEvictableCount()
  {
    return pm.getNoEvictablePage();
  }
  
  public boolean evictionTriggered(Source src, IEvictPolicy policy, SpillCmd cmd, Object arg)
  {
    assert (policy instanceof QueueSrcPolicy);

    switch(cmd)
    {
    case SET_SYNCSPILL:
      spillMode = SimplePageManager.EVICT_SYNC;
      break;

    case FORCE_EVICT:
      pm.evict(true, 1);        //do not evict the head
      break;

    case SET_ASYNCSPILL:
      spillMode = SimplePageManager.EVICT_ASYNC;
      break;

    case SET_NORMAL:
      spillMode = SimplePageManager.NO_EVICT;
      break;
    }
    LogUtil.finer(LoggerType.TRACE,  
                "SimpleQueue : " + hashCode() + " Spill cmd : " + cmd);
    return true;
  }

  private void checkEviction()
  {
    IEvictPolicy evictPolicy = cepMgr.getEvictPolicy();
    if (evictPolicy != null && evictPolicy.needEviction(Source.Factory))
    {
      evictPolicy.runEvictor(Source.Factory);
    }
  }

  /* (non-Javadoc)
   * @see oracle.cep.execution.queues.Queue#enqueue(oracle.cep.execution.queues.Element)
   */
  public synchronized void enqueue(E e) throws ExecException
  {
    checkEviction();
    EntryRef tail = pm.getTail();
    QEntry n = tail.pin(IPinnable.WRITE);
    n.value = e;
    
    tail.advance(SimplePageManager.NO_FREE, spillMode);
  }

  /* (non-Javadoc)
   * @see oracle.cep.execution.queues.Queue#dequeue(oracle.cep.execution.queues.Element)
   */
  @SuppressWarnings("unchecked")
  public synchronized E dequeue(E buf) throws ExecException
  {
    EntryRef head = pm.getHead();
    EntryRef tail = pm.getTail();
    if (head.equals(tail))
      return null;
    QEntry e = head.pin(IPinnable.READ);
    @SuppressWarnings("unchecked")
    E val = (E) e.value;
    head.advance(SimplePageManager.FREE, SimplePageManager.NO_EVICT);
    checkEviction();
    return val;
  }

  /* (non-Javadoc)
   * @see oracle.cep.execution.queues.Queue#peek(oracle.cep.execution.queues.Element)
   */
  @SuppressWarnings("unchecked")
  public synchronized E peek(E buf)
  {
    EntryRef head = pm.getHead();
    EntryRef tail = pm.getTail();
    if (head.equals(tail))
      return null;
    QEntry e = head.pin(IPinnable.READ);
    return (E) e.value;
  }

  /* (non-Javadoc)
   * @see oracle.cep.execution.queues.Queue#isFull()
   */
  public boolean isFull()
  {
    return false;
  }

  /* (non-Javadoc)
   * @see oracle.cep.execution.queues.Queue#isEmpty()
   */
  public synchronized boolean isEmpty()
  {
    EntryRef head = pm.getHead();
    EntryRef tail = pm.getTail();
    return (head.equals(tail));
  }
  
  private static class SimpleQueueStats extends QueueStats
  {
    long tuplesInMemory;
    long tuplesInDisk;
    
    SimpleQueueStats(long tinmem, long tindisk)
    {
      tuplesInMemory = tinmem;
      tuplesInDisk = tindisk;
    }
    
    public long getTuplesInMemory() {return tuplesInMemory;}
    public long getTuplesInDisk() {return tuplesInDisk;}
  }
  
  public synchronized QueueStats getStats()
  {
    SimplePageManagerStat stat = pm.getStat();
    return new SimpleQueueStats(stat.getTuplesInMem(), stat.getTuplesInDisk());
  }  
}


