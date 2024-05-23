/* $Header: pcbpel/cep/server/src/oracle/cep/execution/queues/SimpleQueue.java /main/14 2008/10/24 15:50:22 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/10/08 - remove statics
    hopark      04/17/08 - add stats
    hopark      03/12/08 - use IQueue
    hopark      12/13/07 - fix synchronization
    hopark      12/04/07 - nodeFactory
    hopark      10/16/07 - use local node factory
    parujain    09/26/07 - epr for push source
    hopark      06/20/07 - cleanup
    hopark      05/16/07 - remove printStackTrace
    najain      03/12/07 - bug fix
    najain      03/12/07 - bug fix
    parujain    02/21/07 - use singlylist
    najain      01/15/07 - spill over support
    najain      08/22/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/queues/SimpleQueue.java /main/14 2008/10/24 15:50:22 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.queues;

import java.util.logging.Level;

import oracle.cep.dataStructures.internal.ISinglyList;
import oracle.cep.dataStructures.internal.ISinglyListNode;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.FactoryManager;

/**
 * A linked list implementation of a single-consumer queue
 *
 * @author najain
 */
public class SimpleQueue<E> implements IQueue<E>
{
  private ISinglyList<E> queue;
  
  private IAllocator<ISinglyList<E>> lFactory;
  private IAllocator<ISinglyListNode<E>> nFactory;
  
  long  tuplesInMemory;
  
  /**
   * Constructor for SimpleQueue
   * @param factoryMgr TODO
   */
  public SimpleQueue(FactoryManager factoryMgr)
  {
    lFactory = factoryMgr.get(FactoryManager.MSINGLY_LIST_FACTORY_ID);
    nFactory = factoryMgr.get(FactoryManager.MSINGLY_LIST_NODE_FACTORY_ID);

    try {
     queue = lFactory.allocate();
     queue.setFactory(nFactory);
    }
    catch (ExecException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
    }
  }

  public void initialize()
  {
  }

  /* (non-Javadoc)
   * @see oracle.cep.execution.queues.Queue#enqueue(oracle.cep.execution.queues.Element)
   */
  public synchronized void enqueue(E e) throws ExecException
  {
    tuplesInMemory++;
    queue.add(e);
  }

  /* (non-Javadoc)
   * @see oracle.cep.execution.queues.Queue#dequeue(oracle.cep.execution.queues.Element)
   */
  public synchronized E dequeue(E buf) throws ExecException
  {
    if(queue.isEmpty())
      return null;
    
    tuplesInMemory--;
    E e = queue.remove(true);
    return e;
  }

  /* (non-Javadoc)
   * @see oracle.cep.execution.queues.Queue#peek(oracle.cep.execution.queues.Element)
   */
  public synchronized E peek(E buf)
  {
    try
    {
      if(queue.isEmpty())
        return null;
      E e = queue.getFirst();
      return e;
    }
    catch(ExecException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }
    return null;
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
    return queue.isEmpty();
  }
  
  private class SimpleQueueStats extends QueueStats
  {
    public long getTuplesInMemory() {return tuplesInMemory;}
  }
  
  public synchronized QueueStats getStats()
  {
    return new SimpleQueueStats();
  }
}


