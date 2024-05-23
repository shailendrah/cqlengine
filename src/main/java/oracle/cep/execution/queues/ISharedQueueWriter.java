/* $Header: pcbpel/cep/server/src/oracle/cep/execution/queues/ISharedQueueWriter.java /main/7 2009/05/12 19:25:47 parujain Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    05/04/09 - lifecycle management
    hopark      12/02/08 - move LogLevelManager to ExecContext
    anasrini    10/29/08 - pass ISharedQueueReader in addReader API
    hopark      10/10/08 - remove statics
    najain      04/24/08 - stats
    hopark      04/17/08 - add stat
    hopark      02/26/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/queues/ISharedQueueWriter.java /main/7 2009/05/12 19:25:47 parujain Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.queues;

import java.util.BitSet;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.queues.IQueue.QueueStats;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILoggable;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IEvictableObj;
import oracle.cep.phyplan.PhysicalPlanException;

public interface ISharedQueueWriter extends ILoggable, IEvictableObj
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
  
  ExecOpt getSrcOp();

  /**
   * @param srcOp
   *          The srcOp to set.
   */
  void setSrcOp(ExecOpt srcOp);
  
  /**
   * Enqueues the specified element into the queue. The contents of the element
   * are copied into queue managed memory.
   * 
   * @param e
   *          Element to be enqueued
   * @throws ExecException
   */
  void enqueue(QueueElement e) throws ExecException;
  void enqueue(QueueElement e,int readerId) throws ExecException;
  void enqueue(QueueElement e, BitSet readerIds) throws ExecException;

  /**
   * Dequeue the next element from the queue.
   * 
   * @return Dequeued element
   */
  QueueElement dequeue(QueueElement buf) throws ExecException;;
  QueueElement dequeue(int readerId, QueueElement buf);

  /**
   * Peek at the next element in the queue without dequeuing it.
   * 
   * @return Peeked element
   */
  QueueElement peek(QueueElement buf);
  QueueElement peek(int readerId, QueueElement buf);

  /**
   * Whether the queue is full
   * 
   * @return true if full else false
   */
  boolean isFull();
  boolean isFull(int readerId);

  /**
   * Whether the queue is empty
   * 
   * @return true if full else false
   */
  boolean isEmpty();
  boolean isEmpty(int readerId);

  int getSize(int readerId) throws ExecException;
  
  BitSet getReaders();
  
  QueueStats getReaderStats(int readerId);
  
  int addReader(ISharedQueueReader rdr) throws PhysicalPlanException;
  
  void remove(int readerId); 
  
  void initRdrStats(int readerId);
  
  void setTupleFactory(FactoryManager factoryMgr, IAllocator<ITuplePtr> tupleFactory);
  
  void dumpElements(int readerId, IDumpContext dumper);
  
  QueueStats getStats();
  QueueStats populateAndGetStats();
  
  /**
   * Copy the contents from other queue to this queue
   * @param other
   * @throws ExecException
   */
  void copyFrom(ISharedQueueWriter other) throws ExecException;
}

