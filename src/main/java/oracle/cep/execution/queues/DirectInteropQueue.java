/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/queues/DirectInteropQueue.java /main/13 2013/06/11 08:46:11 udeshmuk Exp $ */

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
    udeshmuk    05/21/13 - 16820093 : check snapshot id for heartbeat
    udeshmuk    05/28/12 - check for snapshotid of input to be not -1 in
                           runoperator
    udeshmuk    05/09/12 - use readerCtx to compare snapshotId in runOperator
    anasrini    08/09/11 - XbranchMerge anasrini_bug-12845846_ps5 from
                           st_pcbpel_11.1.1.4.0
    anasrini    08/08/11 - introduce an enqueueConcurrent method that is
                           invoked by the Concurrent operators
    anasrini    04/05/11 - support for ReaderContext
    hopark      05/29/09 - fix NPE on rc mode
    hopark      05/29/09 - fix NPE on rc mode
    hopark      05/29/09 - fix NPE on rc mode
    anasrini    02/10/09 - fix deadlock issue
    anasrini    01/17/09 - handling runtime exceptions
    sborah      12/17/08 - handle constants
    hopark      12/02/08 - move LogLevelManager to ExecContext
    anasrini    11/08/08 - Creation
    anasrini    11/08/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/queues/DirectInteropQueue.java /main/13 2013/06/11 08:46:11 udeshmuk Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.queues;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.logging.Level;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ExpandableArray;
import oracle.cep.dataStructures.internal.IListNodeHandle;
import oracle.cep.dataStructures.internal.IQSinglyList;
import oracle.cep.dataStructures.internal.IQSinglyListNode;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.phyplan.PhysicalPlanException;
import oracle.cep.service.ExecContext;
import oracle.cep.util.DebugUtil;

/**
 * A queue that is suitable for direct interoperability, i.e., each 
 * enqueue will result in immediate invocation of all readers. That is, 
 * execution proceeds in a depth-first manner.
 *
 * This queue also provides shared buffering (using a linked list) to 
 * support those subset of readers that may block and require buffering.
 *
 * @author anasrini
 */
@DumpDesc(attribTags={"Id", "PhyId", "NumReaders"}, 
          attribVals={"getId", "getPhyId", "getNumReaders"},
          infoLevel=LogLevel.QUEUE_INFO,
          evPinLevel=LogLevel.QUEUE_ELEMENT_PINNED,
          evUnpinLevel=LogLevel.QUEUE_ELEMENT_UNPINNED,
          dumpLevel=LogLevel.QUEUE_DUMP,
          verboseDumpLevel=LogLevel.QUEUE_DUMPELEMS)
public class DirectInteropQueue extends Queue implements ISharedQueueWriter, Externalizable
{
  private static final long serialVersionUID = 553806359524107074L;
      
  private static final int DEFAULT_READERS = 5;
  
  /** the source operator */
  private ExecOpt srcOp;

  /** Number of readers */
  private int numReaders;

  private IListNodeHandle readers[];
  
  private ArrayList<IQSinglyList> nextPvtDeq;

  /** The position where the next IElement is enqueued */
  private IQSinglyList nextEnqueue;

  private IListNodeHandle queueTail;

  /** Storage manager for the list/node */
  private IAllocator<IQSinglyList>       lFactory;

  /** Storage manager for the tuples (inside elements being enqueued) */
  private IAllocator<ITuplePtr>                               tupleFactory;

  private IAllocator lNodeFactory;

  // Related to Direct Invocation of destination operators
  private ArrayList<ExecOpt>            destOps;
  private ArrayList<QueueReaderContext> readerCtxs;
  private QueueElement                  currentElement;
  private BitSet                        pendingElement;
  private BitSet                        bufferedReaders;
  private int                           numBufferedReaders;
  private boolean                       bufferingReqd;
  
  /** statistics */
  private SharedQueueWriterStats stats; 

  private SharedQueueReaderStats       rdrStats[];
  
  private ArrayList<QueueElement>      queueDump;
  
  private boolean                      isRecoveringFromSnapshot = false;

  public void setTupleFactory(FactoryManager factoryMgr, IAllocator<ITuplePtr> tupleFactory)
  {
    this.tupleFactory = tupleFactory;
    try
    {
      lFactory = factoryMgr.get(FactoryManager.QSINGLY_LIST_FACTORY_ID);
      lNodeFactory = factoryMgr.get(FactoryManager.QSINGLY_LIST_NODE_FACTORY_ID);
      nextEnqueue = lFactory.allocate();
      nextEnqueue.setTupleFactory(tupleFactory);
      nextEnqueue.setFactory(lNodeFactory);
      // In order to avoid checking null in Enqueue.
      // we always create a dummy node in advance and set the value on enqueue time.
      IQSinglyListNode n = nextEnqueue.add();
      queueTail = n.getHandle(nextEnqueue);
    } catch (ExecException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
    }
  }

  static
  {
  }

  private BitSet activeReaders;

  @SuppressWarnings("unchecked")
  private void alloEnqDeqArr()
  {
    nextPvtDeq = new ExpandableArray<IQSinglyList>(Constants.INTIAL_NUM_OUT_BRANCHING);
  }

  /**
   * Empty constructor used while recovering queue contents from snapshot
   */
  public DirectInteropQueue()
  {}
  
  public DirectInteropQueue(ExecContext ec)
  {
    super(ec);
    alloEnqDeqArr();
    activeReaders = new BitSet(Constants.INTIAL_NUM_OUT_BRANCHING);
    readers = null;

    pendingElement     = new BitSet(Constants.INTIAL_NUM_OUT_BRANCHING);
    destOps            = new ExpandableArray<ExecOpt>(Constants.INTIAL_NUM_OUT_BRANCHING);
    readerCtxs         = new ExpandableArray<QueueReaderContext>(Constants.INTIAL_NUM_OUT_BRANCHING);
    currentElement     = null;
    bufferingReqd      = false;
    bufferedReaders    = new BitSet(Constants.INTIAL_NUM_OUT_BRANCHING);
    numBufferedReaders = 0;

    stats = new SharedQueueWriterStats(id, phyId);
    rdrStats = null;
    numReaders = 0;
    LogLevelManager.trace(LogArea.QUEUE, LogEvent.QUEUE_DDL, this, "new");
  }

  /**
   * @param readerId:
   *          reader identifer
   * @return returns the stats for the readerId specified
   */
  public SharedQueueReaderStats getReaderStats(int readerId)
  {
    if (rdrStats == null)
      return null;
    return rdrStats[readerId];
  }

  /**
   * @return Returns the srcOp.
   */
  public ExecOpt getSrcOp()
  {
    return srcOp;
  }

  /**
   * @param srcOp
   *          The srcOp to set.
   */
  public void setSrcOp(ExecOpt srcOp)
  {
    this.srcOp = srcOp;
  }

  /**
   * @return Returns the stats.
   */
  public SharedQueueWriterStats getStats()
  {
    return stats;
  }

  /**
   * @return Populate and Returns the stats.
   */
  public SharedQueueWriterStats populateAndGetStats()
  {	
    int rdrNo = 0;
    int minElemsDeq = rdrStats[0].getTotalNumElements();
    for (int i = 1; i < rdrStats.length; i++)
    {
      if (rdrStats[i].getTotalNumElements() < minElemsDeq)
      {
        minElemsDeq = rdrStats[i].getTotalNumElements();
        rdrNo = i;
      }
    }

    SharedQueueReaderStats rStats = rdrStats[rdrNo];
    stats.setNumElements(stats.getTotalNumElements() -
                         rStats.getTotalNumElements());
    stats.setNumPosElements(stats.getTotalNumPosElements() -
                            rStats.getTotalNumPosElements());
    stats.setNumNegElements(stats.getTotalNumNegElements() -
                            rStats.getTotalNumNegElements());
    stats.setNumHeartbeats(stats.getTotalNumHeartbeats() -
                           rStats.getTotalNumHeartbeats());  
	  
    return stats;
  }
  
  public void initRdrStats(int readerId)
  {
    assert rdrStats[readerId] != null;
    rdrStats[readerId].initialize(stats);
  }

  public synchronized int addReader(ISharedQueueReader rdr)
    throws PhysicalPlanException
  {    
    // Get next unused reader
    int rdrNo = activeReaders.nextClearBit(0);
     
    LogLevelManager.trace(LogArea.QUEUE, LogEvent.QUEUE_DDL, this,
                          "addReader", rdrNo);
    activeReaders.set(rdrNo);
    numReaders++;
    if (readers == null || rdrNo >= readers.length)
    {
      // expand readers and rdrStats
      int n = 1;
      if (readers != null)
        n = readers.length + 1;

      IListNodeHandle nreaders[] = new IListNodeHandle[n];
      if (readers != null)
      {
        System.arraycopy(readers, 0, nreaders, 0, readers.length);
      }
      readers = nreaders;

      SharedQueueReaderStats nrdrStats[] = new SharedQueueReaderStats[n];
      if (rdrStats != null)
      {
        System.arraycopy(rdrStats, 0, nrdrStats, 0, rdrStats.length);
        for (int i = rdrStats.length; i < n; i++)
          nrdrStats[i] = new SharedQueueReaderStats();
      }
      else
      {
        for (int i = 0; i < n; i++)
          nrdrStats[i] = new SharedQueueReaderStats();
      }
      rdrStats = nrdrStats;
    }

    // Related to direct invocation of destination operators
   
    destOps.set(rdrNo, rdr.getDestOp());	
    readerCtxs.set(rdrNo, rdr.getReaderContext());
   
    boolean bufferedRdr  = destOps.get(rdrNo).requiresBufferedInput();
    
    bufferedReaders.set(rdrNo, bufferedRdr);
    
    if (bufferedRdr) 
    {
      numBufferedReaders++;
      bufferingReqd = true;
        
      try
      {
        // the current queue is not visible to new readers. 
        // they will be propagated by the operator.
        readers[rdrNo] = queueTail;
        
        assert nextPvtDeq.get(rdrNo) == null;
        
        nextPvtDeq.set(rdrNo, lFactory.allocate());
        
        nextPvtDeq.get(rdrNo).setTupleFactory(tupleFactory);
        nextPvtDeq.get(rdrNo).setFactory(lNodeFactory);
      } catch (ExecException e)
      {
        LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      }
    }

    return rdrNo;
  }

  //TODO add remove and invoke it from PhyOpt.delete?
  //
  
  public synchronized void remove(int readerId)
  {
    LogLevelManager.trace(LogArea.QUEUE, LogEvent.QUEUE_DDL, this,
                          "removeReader", readerId);

    assert activeReaders.get(readerId) == true;
    activeReaders.clear(readerId);
    numReaders--;
    assert numReaders >= 0;

    if (bufferedReaders.get(readerId))
    {
      lFactory.release(nextPvtDeq.get(readerId));
      nextPvtDeq.set(readerId, null);
      readers[readerId]    = null;

      numBufferedReaders--;
      bufferedReaders.clear(readerId);
    }

    pendingElement.clear(readerId);
    destOps.set(readerId, null);
    readerCtxs.set(readerId, null);
  }

  public BitSet getReaders()
  {
    return activeReaders;
  }

  public int getNumReaders()
  {
    return numReaders;
  }

  /*
   * (non-Javadoc)
   *
   * @see oracle.cep.execution.queues.Queue#enqueue(oracle.cep.execution.queues.IElement)
   */
  @SuppressWarnings("unchecked")
  public void enqueue(QueueElement e) throws ExecException
  {
    int                rdrno;
    int                cnt   = 0;
    ExecOpt            nextToRun;   
    QueueReaderContext readerCtx;

    synchronized(this) 
    {
      /*
      System.out.println(srcOp.getOptName() + ": time= " + e.getTs() +
                         " flag= " + e.getTotalOrderingGuarantee() + 
                         " kind= " + e.getKind());
      */
      LogLevelManager.trace(LogArea.QUEUE, LogEvent.QUEUE_ENQUEUE, this, e);
      
      ITuplePtr tuple = e.getTuple();
      if (tuple != null)
      {
        tupleFactory.addRef(tuple, numReaders - 1);
      }
      
      if (bufferingReqd)
      {
        // update header
        IQSinglyListNode n = (IQSinglyListNode)
          queueTail.getNode(nextEnqueue, IPinnable.WRITE);
        n.set(e, numBufferedReaders);
        n = nextEnqueue.add();
        queueTail = n.getHandle(nextEnqueue);
      }
      
      stats.incrTotalNumElements();
      switch (e.getKind())
      {
      case E_PLUS:
        stats.incrTotalNumPosElements();
        break;
      case E_MINUS:
        stats.incrTotalNumNegElements();
        break;
      case E_HEARTBEAT:
        stats.incrTotalNumHeartbeats();
        break;
      default:
        assert false;
      }

      stats.setTsLastElement(e.getTs());
      switch (e.getKind())
      {
      case E_PLUS:
        stats.setTsLastPosElement(e.getTs());
        break;
      case E_MINUS:
        stats.setTsLastNegElement(e.getTs());
        break;
      case E_HEARTBEAT:
        stats.setTsLastHeartbeat(e.getTs());
        break;
      default:
        assert false;
      }
      
      // First store the input queue element
      currentElement = e;

      // Now, directly invoke the destination operators
      
      rdrno = activeReaders.nextSetBit(0);
    }
     
    // Enqueue can be invoked either by:
    // 1. Upstream operator is pushing an event to its output queue
    //    When invoked by operator, DI Scheduler would like to invoke the downstream
    //    operator to process this enqueued event.
    // 2. Recovering from snapshot
    //    Don't invoke the operator now. Listening operator of this queue will be invoked
    //    after next input event will arrive post snapshot load
    if(!isRecoveringFromSnapshot)
    {
      while(cnt < numReaders) {
        synchronized(this)
        {
          cnt++;
          nextToRun = destOps.get(rdrno);
          readerCtx = readerCtxs.get(rdrno);
          pendingElement.set(rdrno);
        }

      
        runOperator(nextToRun, e, readerCtx);
      
        synchronized(this) 
        {
          rdrno = activeReaders.nextSetBit(rdrno+1);
        }
      }
    }
  }

  /**
   * This method will beinvoked by a Concurrent execution operator
   * and only when all the readers of this queue are also concurrent 
   * operators. This assumes that none of the readers will perform
   * a "peek" or a "dequeue"
   *
   * Also note that modifications of this queue (like readerSet etc)
   * cannot happen concurrently while queries are running since DDL
   * and DML are mutually exclusive (via the PlanManager lock)
   */
  @SuppressWarnings("unchecked")
  public void enqueueConcurrent(QueueElement e) throws ExecException
  {
    int                rdrno;
    int                cnt   = 0;
    ExecOpt            nextToRun;   
    QueueReaderContext readerCtx;

    /*
      System.out.println(srcOp.getOptName() + ": time= " + e.getTs() +
      " flag= " + e.getTotalOrderingGuarantee() + 
      " kind= " + e.getKind());
    */
    LogLevelManager.trace(LogArea.QUEUE, LogEvent.QUEUE_ENQUEUE, this, e);
    
    ITuplePtr tuple = e.getTuple();
    if (tuple != null)
    {
      tupleFactory.addRef(tuple, numReaders - 1);
    }
      
    // Now, directly invoke the destination operators

    rdrno = activeReaders.nextSetBit(0);
    while(cnt < numReaders) {
      cnt++;
      nextToRun = destOps.get(rdrno);
      readerCtx = readerCtxs.get(rdrno);
      runOperator(nextToRun, e, readerCtx);
      rdrno = activeReaders.nextSetBit(rdrno+1);
    }
  }

  /**
   * Enqueue a queueElement for a specific reader.
   * It's only used by state propagation for newly added operators.
   * @param e
   * @param readerId
   * @throws ExecException
   */
  public synchronized void enqueue(QueueElement e,
                                   int readerId) throws ExecException
  {
    LogLevelManager.trace(LogArea.QUEUE, LogEvent.QUEUE_ENQUEUE, this, e,
                          readerId); 

    assert activeReaders.get(readerId);
    ITuplePtr tuple = e.getTuple();
    if (tuple != null)
    {
      tupleFactory.addRef(tuple);
    }

    if (bufferedReaders.get(readerId))
      nextPvtDeq.get(readerId).add(e);

    currentElement = e;
    pendingElement.set(readerId);

    runOperator(destOps.get(readerId));
  }

  private void runOperator(ExecOpt op) 
  {
    try 
    {
      op.run();
    }
    catch(Throwable e) 
    {
      execContext.getExecMgr().addToErrorOpsList(e, op);
    }
  }
  
  private void runOperator(ExecOpt op, QueueElement input,
                           QueueReaderContext readerCtx) 
  {
    try 
    {
      //if the snapshot id of the input is higher than the reader's then
      //only run the operator for that input.
      
      if((readerCtx.getSnapshotId() != -1)
         && (input.getSnapshotId() != Long.MAX_VALUE)
        )
      {
        if(input.getSnapshotId() > readerCtx.getSnapshotId())
        {
          if (srcOp.getMutStats() != null)
            srcOp.getMutStats().setEndTime(System.nanoTime());
          op.run(input, readerCtx);
        }
        else
          LogUtil.fine(LoggerType.TRACE, 
                     input+" NOT sent to operator "+ op.getOptName()+": Since"
                     + " input snapshotId ("+input.getSnapshotId() + ") <= " +
                     "snapshotId of the i/p queue ("+readerCtx.getSnapshotId()
                     + ") of the operator"
                    );
      }
      else
      {
        if (srcOp.getMutStats() != null)
          srcOp.getMutStats().setEndTime(System.nanoTime());
        op.run(input, readerCtx);
      }
    }
    catch(Throwable e) 
    {
      execContext.getExecMgr().addToErrorOpsList(e, op);
    }
  }
    

  /**
   * A queueElement is enqueued for multiple readers.
   * It's only used by state propagation for newly added operators.
   *
   * @param e
   *          Queuelement to be enqueued
   * @param readerIds
   *          Reader ids of all the readers
   * @throws ExecException
   */
  @SuppressWarnings("unchecked")
  public synchronized void enqueue(QueueElement e,
                                   BitSet readerIds) throws ExecException
  {
    // only positive elements are enqueued in current representation
    //assert (e.getKind() == QueueElement.Kind.E_PLUS);

    int i = readerIds.nextSetBit(0);
    int cnt = 0;

    while (i >= 0)
    {
      enqueue(e, i);
      i = readerIds.nextSetBit(i + 1);
      cnt++;
    }

    // update elements for other readers
    i = readerIds.nextClearBit(0);

    while ((i >= 0) && (cnt < numReaders))
    {
      rdrStats[i].incrNumOthers();
      rdrStats[i].incrNumPosOthers();

      i = readerIds.nextClearBit(i + 1);
      cnt++;
    }

    stats.setTsLastElement(e.getTs());
    stats.setTsLastPosElement(e.getTs());
  }

  /*
   * (non-Javadoc)
   *
   * @see oracle.cep.execution.queues.Queue#dequeue()
   */

  public QueueElement dequeue(QueueElement buf)
  {
    // should never be called
    assert false;
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see oracle.cep.execution.queues.Queue#peek()
   */

  public QueueElement peek(QueueElement buf)
  {
    // should never be called
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see oracle.cep.execution.queues.Queue#isFull()
   */
  public boolean isFull()
  {
    // TODO Auto-generated method stub
    return false;
  }

  /*
   * (non-Javadoc)
   *
   * @see oracle.cep.execution.queues.Queue#isEmpty()
   */
  public boolean isEmpty()
  {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * Dequeue a message for the specified reader
   *
   * @param readerId
   *          Id of the Reader who is dequeuing the IElement
   * @return
   */
  @SuppressWarnings("unchecked")
  public synchronized QueueElement dequeue(int readerId, QueueElement buf)
  {
    QueueElement e = null;

    // Not a buffered reader
    if (!bufferedReaders.get(readerId))
    {
      e = currentElement;
      if (pendingElement.get(readerId))
      {
        buf.copy(currentElement);
        if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
        {
          if (e != null)
          {
            ITuplePtr tuple = e.getTuple();
            if (tuple != null) 
            {
            tupleFactory.addRef(tuple, 1);
            tupleFactory.release(tuple);
          }  
        }
        }
        pendingElement.clear(readerId);
        return buf;
      }
      else
        return null;
    }

    // A buffered reader
    try
    {
      assert activeReaders.get(readerId);
      if (!nextPvtDeq.get(readerId).isEmpty())
      {
        e = nextPvtDeq.get(readerId).removeElem(buf);
        return e;
      }
      IListNodeHandle reader = readers[readerId];
      if (queueTail.equals(reader))
        return null;

      // copy the queue element to given buffer
      IQSinglyListNode node = (IQSinglyListNode) reader.getNode(nextEnqueue, IPinnable.WRITE);
      e = node.get(buf);
      int curReaders = node.decAndGetReaders();
      // advance the node
      IQSinglyListNode nxtnode = node.getNext(nextEnqueue);
      readers[readerId] = nxtnode.getHandle(nextEnqueue);
      if (curReaders == 0)
      {
        // If the reader is the last reader of this node, remove the node
        nextEnqueue.remove();
      }
    } catch (ExecException ex)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, ex);
      e = null;
    }
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      if (e != null)
      {
        ITuplePtr tuple = e.getTuple();
        if (tuple != null) 
        {
        tupleFactory.addRef(tuple, 1);
        tupleFactory.release(tuple);
        }
      }  
    }
    return e;
  }

  /*
   * (non-Javadoc)
   *
   * @see oracle.cep.execution.queues.Queue#peek()
   */
  @SuppressWarnings("unchecked")
  public synchronized QueueElement peek(int readerId, QueueElement buf)
  {
    QueueElement e = null;

    // Not a buffered reader
    if (!bufferedReaders.get(readerId))
    {
      e = currentElement;
      if (activeReaders.get(readerId) == false)
        return null;
      if (pendingElement.get(readerId) == false)
        return null;

      buf.copy(e);
      
      LogLevelManager.trace(LogArea.QUEUE, LogEvent.QUEUE_PEEK, this, readerId,
                            e);
      return buf;
    }

    // A buffered reader
    try
    {
      if (activeReaders.get(readerId) == false)
	return null;

      if (!nextPvtDeq.get(readerId).isEmpty())
      {
        e = nextPvtDeq.get(readerId).getFirstElem(buf);
      } else
      {
        IListNodeHandle reader = readers[readerId];
        if (queueTail.equals(reader))
          return null;

        // copy the queue element to given buffer
        IQSinglyListNode node = (IQSinglyListNode) reader.getNode(nextEnqueue, IPinnable.READ);
        e = node.get(buf);
      }
    } catch (ExecException ex)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, ex);
      return null;
    }
    LogLevelManager.trace(LogArea.QUEUE, LogEvent.QUEUE_PEEK, this, readerId, e);
    return e;
  }

  /*
   * (non-Javadoc)
   *
   * @see oracle.cep.execution.queues.Queue#isFull()
   */
  public boolean isFull(int readerId)
  {
    assert activeReaders.get(readerId) == true;

    return false;
  }

  public synchronized boolean isEmpty(int readerId)
  {
    assert activeReaders.get(readerId) == true;

    // Not a buffered reader
    if (!bufferedReaders.get(readerId))
      return pendingElement.get(readerId);

    // Case of a buffered reader
    if (nextPvtDeq.get(readerId).isEmpty())
      return (queueTail.equals(readers[readerId]));
    else
      return false;
  }

  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("<SharedQueueWriter id=\"" + getId() + "\" numReaders=\"" +
              numReaders + "\" >");
    sb.append("</SharedQueueWriter>");

    return sb.toString();
  }

  /*************************************************************************/
  // ILoggable interface implementation
  @SuppressWarnings("unchecked")
  public int getSize(int readerId)
    throws ExecException
  {
    // Not a buffered reader
    if (!bufferedReaders.get(readerId)) 
    {
      return pendingElement.get(readerId) ? 1 : 0;
    }

    // A buffered reader
    if (!nextPvtDeq.get(readerId).isEmpty())
    {
      return nextPvtDeq.get(readerId).getSize();
    } else
    {
      if (queueTail.equals(readers[readerId]))
        return 0;
      
      IListNodeHandle reader = readers[readerId];
      IQSinglyListNode n = (IQSinglyListNode) reader.getNode(nextEnqueue, IPinnable.READ);
      int pos = 0;
      while (n != null) 
      {
        n = n.getNext(nextEnqueue);
        pos++;
      }
      return pos;
    }
  }

  @SuppressWarnings("unchecked")
  public void dumpElements(int readerId, IDumpContext dumper)
  {
  }

  /*************************************************************************/
  // ILoggable interface implementation
  public synchronized void dump(IDumpContext dumper) 
  {
  }
  
  public synchronized boolean evict()
    throws ExecException
  {
    return false;
  }

  public void trace(IDumpContext dumper, ILogEvent event, int level, Object[] args)
  {
    if (level == LogLevel.QUEUE_STATS)
    {
      SharedQueueWriterStats stats = populateAndGetStats();

      dumper.writeln("SharedQueueWriterId", Integer.toString(getId()));

      dumper.writeln("SharedQueueWriterPhyId", Integer.toString(getPhyId()));

      dumper.writeln("numElementsWriter",
                     Integer.toString(stats.getNumElements()));

      dumper.writeln("numPosElementsWriter", 
		     Integer.toString(stats.getNumPosElements()));

      dumper.writeln("numNegElementsWriter", 
		     Integer.toString(stats.getNumNegElements()));

      dumper.writeln("numHeartbeats", 
		     Integer.toString(stats.getNumHeartbeats()));
    }
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    try
    {
      createQueueFullDump();
      out.writeObject(this.queueDump);
    }
    catch (ExecException e)
    {
      throw new IOException("Exception during persistence of inter operator queue [" + this.toString()+ "]", e);
    }
  }
  
  private void createQueueFullDump() throws ExecException
  {
    queueDump = new ArrayList<QueueElement>();
    IQSinglyListNode next = this.nextEnqueue.getHead();
    while(next.getNodeElem() != null)
    {                
      queueDump.add((QueueElement)next);
      next = next.getNext(this.nextEnqueue);
    }    
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    this.queueDump = (ArrayList<QueueElement>) in.readObject();
  }

  @Override
  public void copyFrom(ISharedQueueWriter other) throws ExecException
  {
    this.isRecoveringFromSnapshot = true;
    if(other instanceof DirectInteropQueue)
    {      
      ArrayList<QueueElement> recoveredQueueItems = ((DirectInteropQueue)other).queueDump;
      for(QueueElement e: recoveredQueueItems)        
        enqueue(e);
    }
    else
      throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR);
    this.isRecoveringFromSnapshot = false;
  }
  
}
