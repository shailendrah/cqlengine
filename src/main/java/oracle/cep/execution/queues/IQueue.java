/* $Header: IQueue.java 17-apr-2008.10:32:15 hopark Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/17/08 - add stat
    hopark      03/12/08 - Creation
 */

/**
 *  @version $Header: IQueue.java 17-apr-2008.10:32:15 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.queues;

import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.execution.ExecException;
import java.util.Map;

public interface IQueue<E>
{
  /**
   * Enqueues the specified element into the queue. The contents of the element
   * are copied into queue managed memory.
   * 
   * @param e
   *          Element to be enqueued
   * @throws ExecException
   */
  void enqueue(E e) throws ExecException;

  /**
   * Dequeue the next element from the queue.
   * 
   * @return Dequeued element
   */
  E dequeue(E buf) throws ExecException;

  /**
   * Peek at the next element in the queue without dequeuing it.
   * 
   * @return Peeked element
   */
  E peek(E buf);

  /**
   * Whether the queue is full
   * 
   * @return true if full else false
   */
  boolean isFull();

  /**
   * Whether the queue is empty
   * 
   * @return true if full else false
   */
  boolean isEmpty();

  QueueStats getStats();
  
  //marker class
  public static class QueueStats
  {
    public long getTuplesInMemory() {return 0;}
    public long getTuplesInDisk() {return 0;}
  }

}
