/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/queues/ISharedQueueReader.java /main/5 2011/04/10 21:20:46 sborah Exp $ */

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
    anasrini    04/05/11 - add get/set readerContext
    parujain    05/04/09 - lifecycle management
    hopark      04/17/08 - add stat
    hopark      02/26/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/queues/ISharedQueueReader.java /main/4 2009/05/12 19:25:47 parujain Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.queues;

import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.queues.IQueue.QueueStats;

public interface ISharedQueueReader
{
  /**
   * Getter for id in Queue
   * 
   * @return Returns the id
   */
  int getId();

  /**
   * @return Returns the physical queue Id.
   */
  int getPhyId();

  /**
   * Setter for physical id
   * 
   * @param phyOptId
   *          The id of the corresponding physical queue to set.
   */
  void setPhyId(int phyId);
  
  /**
   * Enqueues the specified element into the queue. The contents of the element
   * are copied into queue managed memory.
   * 
   * @param e
   *          Element to be enqueued
   * @throws ExecException
   */
  void enqueue(QueueElement e) throws ExecException;

  /**
   * Dequeue the next element from the queue.
   * 
   * @return Dequeued element
   */
  QueueElement dequeue(QueueElement buf) throws ExecException;;

  /**
   * Peek at the next element in the queue without dequeuing it.
   * 
   * @return Peeked element
   */
  QueueElement peek(QueueElement buf);

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

  /**
   * @return Returns the destOp.
   */
  ExecOpt getDestOp();

  /**
   * @param destOp The destOp to set.
   */
  void setDestOp(ExecOpt destOp);

  int getReaderId();
  void setReaderId(int id);
  
  /**
   * Getter for writer in SharedQueueReader
   * 
   * @return Returns the writer
   */
  ISharedQueueWriter getWriter();

  /**
   * Setter for writer in SharedQueueReader
   * 
   * @param writer
   *          The writer to set.
   */
  void setWriter(ISharedQueueWriter writer);

  QueueStats getStats();

  /**
   * Setter for reader context associated with this reader
   * @param readerCtx reader context associated with this reader
   */
  void setReaderContext(QueueReaderContext readerCtx);

  /**
   * Getter for reader context associated with this reader
   * @return reader context associated with this reader
   */
  QueueReaderContext getReaderContext();
  
  /**
   * Copy the queue contents from other queue contents to this queue
   * @param other
   * @throws ExecException
   */
  void copyFrom(ISharedQueueReader other) throws ExecException;
}
