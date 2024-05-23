/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/queues/SharedQueueWriter.java /main/43 2012/06/18 06:29:07 udeshmuk Exp $ */
/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares SharedQueueWriter in package oracle.cep.execution.queues.
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 NOTES
 <other useful comments, qualifications, etc.>
 MODIFIED    (MM/DD/YY)
 udeshmuk  06/04/12 - allow heartbeat in enqueue with readerIds
 hopark    05/29/09 - fix NPE on rc mode
 hopark    05/29/09 - fix NPE on rc mode
 hopark    05/29/09 - fix NPE on rc mode
 sborah    12/17/08 - handle constants
 hopark    12/02/08 - move LogLevelManager to ExecContext
 hopark    10/10/08 - remove statics
 hopark    06/19/08 - logging refactor
 hopark    06/18/08 - logging refactor
 najain    04/24/08 - add more stats
 najain    04/16/08 - add trace
 hopark    03/21/08 - refcnt test
 hopark    03/17/08 - fix refcnt
 hopark    02/28/08 - use ISharedQueueWriter
 hopark    02/28/08 - use ISharedQueueWriter
 hopark    03/17/08 - fix refcnt
 hopark    03/03/08 - set TupleFactory for list
 hopark    01/25/08 - optimize
 hopark    12/27/07 - support xmllog
 hopark    12/07/07 - cleanup spill
 hopark    12/04/07 - nodeFactory life cycle
 hopark    11/15/07 - init NodeFac
 hopark    11/02/07 - share nodeFactory
 hopark    11/29/07 - remove AtomicInteger
 hopark    10/15/07 - add evict
 hopark    10/30/07 - remove IQueueElement
 hopark    10/19/07 - remove IQueueElement
 najain    07/30/07 - check in peek
 najain    07/23/07 - move stats to SharedQueueWriter
 najain    07/19/07 - synchronize peek
 hopark    06/19/07 - cleanup
 hopark    06/07/07 - use LogArea
 najain    06/06/07 - fix lint errors
 hopark    05/22/07 - debuglogging
 hopark    05/16/07 - remove printStackTrace
 najain    03/21/07 - cleanup
 parujain  03/20/07 - debuglevel
 najain    03/12/07 - bug fix
 najain    03/07/07 - bug fix
 parujain  02/26/07 - synchronize dequeue
 parujain  02/21/07 - remove from memory too
 najain    02/08/07 - list cleanup
 najain    02/05/07 - coverage
 najain    01/04/07 - spill over support
 parujain  12/06/06 - propagating relation
 najain    10/17/06 - add getStats
 najain    10/12/06 - add statistics
 najain    08/02/06 - ref-counting optimizations
 najain    07/25/06 - concurrency support
 parujain  07/26/06 - Generic objects
 parujain  07/24/06 - Generic LinkedList
 najain    07/18/06 - ref-count tuples
 najain    07/13/06 - ref-count timestamps
 najain    07/10/06 - add TupleFactory
 najain    07/05/06 - cleanup
 najain    06/28/06 - integration with memory manager
 najain    06/20/06 - add remove
 najain    06/13/06 - query deletion support
 najain    06/13/06 - bug fix
 najain    06/12/06 - bug fix
 najain    06/05/06 - support enqueue for a specific reader
 najain    05/04/06 - sharing support
 anasrini  03/24/06 - add toString
 skaluska  02/22/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/queues/SharedQueueWriter.java /main/43 2012/06/18 06:29:07 udeshmuk Exp $
 *  @author  najain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.queues;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ExpandableArray;
import oracle.cep.dataStructures.internal.IListNodeHandle;
import oracle.cep.dataStructures.internal.IQSinglyList;
import oracle.cep.dataStructures.internal.IQSinglyListNode;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.phyplan.PhysicalPlanException;
import oracle.cep.service.ExecContext;
import oracle.cep.util.DebugUtil;
import oracle.cep.util.StringUtil;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.logging.Level;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.logging.trace.LogTags;
import oracle.cep.memmgr.IAllocator;

/**
 * A linked list implementation of +multi-consumer queue
 *
 * @author skaluska
 */
@DumpDesc(attribTags={"Id", "PhyId", "NumReaders"}, 
          attribVals={"getId", "getPhyId", "getNumReaders"},
          infoLevel=LogLevel.QUEUE_INFO,
          evPinLevel=LogLevel.QUEUE_ELEMENT_PINNED,
          evUnpinLevel=LogLevel.QUEUE_ELEMENT_UNPINNED,
          dumpLevel=LogLevel.QUEUE_DUMP,
          verboseDumpLevel=LogLevel.QUEUE_DUMPELEMS)
public class SharedQueueWriter extends Queue implements ISharedQueueWriter, Externalizable
{
  private static final long serialVersionUID = 8240079503117629345L;

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
  
  /** statistics */
  private SharedQueueWriterStats stats; 

  private SharedQueueReaderStats       rdrStats[];

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

  public SharedQueueWriter()
  {}
  
  public SharedQueueWriter(ExecContext ec)
  {
    super(ec);
    alloEnqDeqArr();
    activeReaders = new BitSet(Constants.INTIAL_NUM_OUT_BRANCHING);
    readers = null;
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
    stats.setNumElements(stats.getTotalNumElements() - rStats.getTotalNumElements());
    stats.setNumPosElements(stats.getTotalNumPosElements() - rStats.getTotalNumPosElements());
    stats.setNumNegElements(stats.getTotalNumNegElements() - rStats.getTotalNumNegElements());
    stats.setNumHeartbeats(stats.getTotalNumHeartbeats() - rStats.getTotalNumHeartbeats());  
   
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
    
    LogLevelManager.trace(LogArea.QUEUE, LogEvent.QUEUE_DDL, this, "addReader", rdrNo);
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
    return rdrNo;
  }

  //TODO add remove and invoke it from PhyOpt.delete?
  //
  
  public synchronized void remove(int readerId)
  {
    LogLevelManager.trace(LogArea.QUEUE, LogEvent.QUEUE_DDL, this, "removeReader", readerId);

    assert activeReaders.get(readerId) == true;
    activeReaders.clear(readerId);
    numReaders--;
    assert numReaders >= 0;
    lFactory.release(nextPvtDeq.get(readerId));
    nextPvtDeq.set(readerId, null);
    readers[readerId] = null;
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
  public synchronized void enqueue(QueueElement e) throws ExecException
  {
    LogLevelManager.trace(LogArea.QUEUE, LogEvent.QUEUE_ENQUEUE, this, e);

    ITuplePtr tuple = e.getTuple();
    if (tuple != null)
    {
      tupleFactory.addRef(tuple, numReaders - 1);
    }
    
    // update header
    IQSinglyListNode n = (IQSinglyListNode) queueTail.getNode(nextEnqueue, IPinnable.WRITE);
    n.set(e, numReaders);
    
    n = nextEnqueue.add();
    queueTail = n.getHandle(nextEnqueue);

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
    LogLevelManager.trace(LogArea.QUEUE, LogEvent.QUEUE_ENQUEUE, this, e, readerId); 

    assert activeReaders.get(readerId);
    ITuplePtr tuple = e.getTuple();
    if (tuple != null)
    {
      tupleFactory.addRef(tuple);
    }
    nextPvtDeq.get(readerId).add(e);
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
    if (nextPvtDeq.get(readerId).isEmpty())
      return true;
    return (queueTail.equals(readers[readerId]));
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
    if (!nextPvtDeq.get(readerId).isEmpty())
    {
      nextPvtDeq.get(readerId).dump(dumper);
    } else
    {
      if (queueTail.equals(readers[readerId]))
        return;
      
      try
      {
        IListNodeHandle reader = readers[readerId];
        IQSinglyListNode n = (IQSinglyListNode) reader.getNode(nextEnqueue, IPinnable.READ);
        while (n != null) 
        {
          n.dump(dumper);
          n = n.getNext(nextEnqueue);
        }
      }
      catch(ExecException e)
      {
        dumper.writeln(LogTags.DUMP_ERR, e.toString());
      }
    }
  }

  /*************************************************************************/
  // ILoggable interface implementation
  public synchronized void dump(IDumpContext dumper) 
  {
    if (!dumper.isVerbose())
    {
      String tag = LogUtil.beginDumpObj(dumper, this);
      dumper.writeln("Size", nextEnqueue.getSize());
      LogUtil.endDumpObj(dumper, tag);
      return;
    }
    String dumperKey = StringUtil.getBaseClassName(this);
    IDumpContext w = dumper.openDumper(dumperKey);
    String tag = LogUtil.beginDumpObj(w, this);
    nextEnqueue.dump(w);
    LogUtil.endDumpObj(w, tag);
    w.closeDumper(dumperKey, dumper);
  }
  
  public synchronized boolean evict()
    throws ExecException
  {
    return nextEnqueue.evict();
  }

  public void trace(IDumpContext dumper, ILogEvent event, int level, Object[] args)
  {
    if (level == LogLevel.QUEUE_STATS)
    {
      SharedQueueWriterStats stats = populateAndGetStats();

      dumper.writeln("SharedQueueWriterId", Integer.toString(getId()));

      dumper.writeln("SharedQueueWriterPhyId", Integer.toString(getPhyId()));

      dumper.writeln("numElementsWriter", Integer.toString(stats.getNumElements()));

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
    // We don't use SharedQueueWriter queue with Direct Interopt Scheduler
    throw new IOException("Snapshot Handling for SharedQueueWriter object is not supported");
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    // We don't use SharedQueueWriter queue with Direct Interopt Scheduler
    throw new IOException("Snapshot Handling for SharedQueueWriter object is not supported");
  }

  @Override
  public void copyFrom(ISharedQueueWriter other)
  {
    // We don't use SharedQueueWriter queue with Direct Interopt Scheduler
    assert false; 
  }
  
}
