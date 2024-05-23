/* $Header: pcbpel/cep/server/src/oracle/cep/execution/queues/stored/SharedQueueWriter.java /main/9 2009/05/29 19:35:21 hopark Exp $ */
/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares SharedQueueWriter in package oracle.cep.execution.queues.
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 NOTES
 <other useful comments, qualifications, etc.>
 MODIFIED    (MM/DD/YY)
 hopark    05/18/09 - fix tsorder
 sborah    12/17/08 - handle constants
 hopark    12/02/08 - move LogLevelManager to ExecContext
 anasrini  10/29/08 - change in signature for addReader API
 hopark    10/10/08 - remove statics
 hopark    06/19/08 - logging refactor
 najain    04/24/08 - add more stats
 hopark    02/22/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/queues/stored/SharedQueueWriter.java /main/9 2009/05/29 19:35:21 hopark Exp $
 *  @author  najain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.queues.stored;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Externalizable;
import java.util.BitSet;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement.Kind;
import oracle.cep.dataStructures.internal.stored.TuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.QueueElementImpl;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.queues.ISharedQueueWriter;
import oracle.cep.execution.queues.ISharedQueueReader;
import oracle.cep.execution.queues.Queue;
import oracle.cep.execution.queues.SharedQueueReaderStats;
import oracle.cep.execution.queues.SharedQueueWriterStats;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.memmgr.SimplePageManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IAllocator.NameSpace;
import oracle.cep.memmgr.SimplePageManager.BaseEntry;
import oracle.cep.memmgr.SimplePageManager.EntryGen;
import oracle.cep.memmgr.SimplePageManager.EntryRef;
import oracle.cep.memmgr.factory.paged.TupleFactory;
import oracle.cep.phyplan.PhysicalPlanException;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;
import oracle.cep.util.DebugUtil;

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
  private static final long serialVersionUID = 537186135121022373L;

  private static final int DEFAULT_READERS = 5;
  
  // Page Entry
  //TODO. implement QueueElement and avoid copy
  static  class Entry extends BaseEntry
  {
    private static final long serialVersionUID = 8526704056926664578L;

    Kind        kind;
    ITuplePtr   tuple;
    long        ts;
    int         readers;
    boolean     tsorderGuarantee;
    
    public Entry()
    {
      super();
      ts = 55554444;
    }
    
    public void set(QueueElement e, int readers)
    {
      kind = e.getKind();
      ts = e.getTs();
      tuple = e.getTuple();
      tsorderGuarantee = e.getTotalOrderingGuarantee();
      this.readers = readers;
    }
    
    public QueueElement get(QueueElement buf)
    {
      buf.setKind(kind);
      buf.setTs(ts);
      buf.setTuple(tuple);
      buf.setTotalOrderingGuarantee(tsorderGuarantee);
      return buf;
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException
    {
      readers = in.readInt();
      short kindOrd = in.readShort();
      Kind[] vals = Kind.values();
      assert (kindOrd >=0 && kindOrd < vals.length);
      kind = vals[kindOrd];
      ts = in.readLong();
      tsorderGuarantee = in.readBoolean();
      int tupleFacId = in.readInt();
      long tupleId = in.readLong();
      TupleFactory tupleFactory = null;
      if (tupleFacId >= 0)
      {
        FactoryManager factoryMgr = CEPManager.getInstance().getFactoryManager();
        IAllocator<ITuplePtr>  tupleFac = factoryMgr.get(tupleFacId);
        tupleFactory = (TupleFactory) tupleFac;
      }
      tuple = null;
      if (tupleId >= 0 && tupleFactory != null)
        tuple = tupleFactory.get(tupleId);
      if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
      {
        if (tuple != null)
        {
          TupleFactory tf = (TupleFactory) tupleFactory;
          tf.addRefCountLog(tuple.getId(), "write readers="+readers + " kind=" + kind.toString() + " ts="+ts);
        }
      }
    }
    
    public void writeExternal(ObjectOutput out) throws IOException
    {
      out.writeInt(readers);
      short kindOrd = (short) kind.ordinal();
      out.writeShort(kindOrd);
      out.writeLong(ts);
      out.writeBoolean(tsorderGuarantee);
      int tupleFacId = -1;
      long tupleId = -1l;
      TupleFactory tupleFactory = null;
      if (tuple != null)
      {
        assert (tuple instanceof TuplePtr);
        TuplePtr ptuple = (TuplePtr) tuple;
        tupleFactory = ptuple.getFactory();
        tupleFacId = tupleFactory.getId();
        tupleId = tuple.getId();
      }
      out.writeInt(tupleFacId);
      out.writeLong(tupleId);
      if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
      {
        if (tuple != null)
        {
          TupleFactory tf = (TupleFactory) tupleFactory;
          tf.addRefCountLog(tuple.getId(), "write readers="+readers + " kind=" + kind.toString() + " ts="+ts);
        }
      }
    }
  };
  
  /** the source operator */
  private ExecOpt srcOp;

  /** Number of readers */
  private int numReaders;

  /** statistics */
  private SharedQueueWriterStats stats; 

  private BitSet activeReaders;

  /** Storage manager for the list/node */
  private SimplePageManager pm;
  
  private Stub[]        readers;

  private TupleFactory   tupleFactory;
  
  static class Stub
  {
    EntryRef     tail;
    EntryRef     head;
    SimplePageManager pvtpm;
    SharedQueueReaderStats rdrStat;
  }
  
  private static final int      s_pvtPageSize = 100;
  private static final int      s_pageSize = 1000;

  private static final EntryGen s_entryGen = new EntryGen() {
    public BaseEntry create() 
    {
      return new Entry();
    }
  };

  public SharedQueueWriter(ExecContext ec)
  {
    super(ec);
    activeReaders = new BitSet(Constants.INTIAL_NUM_OUT_BRANCHING);
    readers = null;
    stats = new SharedQueueWriterStats(getId(), getPhyId());
    numReaders = 0;
    pm = new SimplePageManager(FactoryManager.QUEUEPAGE_FACTORY_ID, 
                               NameSpace.QPAGE, s_pageSize, s_entryGen);
    
    LogLevelManager.trace(LogArea.QUEUE, LogEvent.QUEUE_DDL, this, "new");
  }

  @SuppressWarnings("unchecked")
  public void setTupleFactory(FactoryManager factoryMgr, IAllocator<ITuplePtr> tupleFactory)
  {
    this.tupleFactory = (TupleFactory) tupleFactory;
  }

  /**
   * @param readerId:
   *          reader identifer
   * @return returns the stats for the readerId specified
   */
  public SharedQueueReaderStats getReaderStats(int readerId)
  {
    assert activeReaders.get(readerId);
    Stub reader = readers[readerId];
    if (reader.rdrStat == null)
      return null;
    return reader.rdrStat;
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
   * @return Returns the stats.
   */
  public SharedQueueWriterStats populateAndGetStats()
  {  
    return stats;
  }
  
  public void initRdrStats(int readerId)
  {
    assert activeReaders.get(readerId);
    Stub reader = readers[readerId];
    assert reader.rdrStat != null;
    reader.rdrStat.initialize(stats);
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
      int n = DEFAULT_READERS;
      if (readers != null)
      {
        n = readers.length * 2;
      }
      Stub[] nreaders = new Stub[n];
      if (readers != null)
      {
        System.arraycopy(readers, 0, nreaders, 0, readers.length);
      }
      readers = nreaders;
    }
    // the current queue is not visible to new readers. 
    // they will be propagated by the operator.
    Stub reader = new Stub();
    readers[rdrNo] = reader;
    reader.head = pm.getTail().clone();
    reader.tail = pm.getTail();
    reader.rdrStat = new SharedQueueReaderStats();
    reader.pvtpm = null;
    return rdrNo;
  }

  //TODO add remove and invoke it from PhyOpt.delete?
  //
  
  public synchronized void remove(int readerId)
  {
    LogLevelManager.trace(LogArea.QUEUE, LogEvent.QUEUE_DDL, this, "removeReader", readerId);

    // remove all the messages
    QueueElement buf = new QueueElementImpl();
    while (true)
    {
      QueueElement b = dequeue(readerId, buf);
      if (b == null)
        break; 
    }

    assert activeReaders.get(readerId) == true;
    activeReaders.clear(readerId);
    numReaders--;
    assert numReaders >= 0;

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
      if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
      {
         TupleFactory tf = (TupleFactory) tupleFactory;
         tf.addRefCountLog(tuple.getId(), hashCode() + " enqueue readers="+numReaders + " " + e.toString());
      }
    }

    EntryRef tail = pm.getTail();
    Entry n = tail.pin(IPinnable.WRITE);
    n.set(e, numReaders);
    
    tail.advance(false, SimplePageManager.NO_EVICT);

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
    Stub reader = readers[readerId];
    SimplePageManager pvtpm = reader.pvtpm;
    assert (pvtpm != null);
    EntryRef pmtail = pvtpm.getTail();
    Entry n = pmtail.pin(IPinnable.WRITE);
    n.kind = e.getKind();
    n.tuple = e.getTuple();
    n.ts = e.getTs();
    n.tsorderGuarantee = e.getTotalOrderingGuarantee();
    pmtail.advance(false, SimplePageManager.NO_EVICT);
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
    assert (e.getKind() == QueueElement.Kind.E_PLUS);

    int i = readerIds.nextSetBit(0);
    int cnt = 0;

    while (i >= 0)
    {
      Stub reader = readers[i];
      SimplePageManager pvtpm = reader.pvtpm;
      if (pvtpm == null)
        reader.pvtpm = new SimplePageManager(FactoryManager.QUEUEPAGE_FACTORY_ID, 
                                             NameSpace.QPAGE, s_pvtPageSize, s_entryGen);
      enqueue(e, i);
      i = readerIds.nextSetBit(i + 1);
      cnt++;
    }

    // update elements for other readers
    i = readerIds.nextClearBit(0);
    while ((i >= 0) && (cnt < numReaders))
    {
      Stub reader = readers[i];
      reader.rdrStat.incrNumOthers();
      reader.rdrStat.incrNumPosOthers();

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
    assert activeReaders.get(readerId);
    Stub reader = readers[readerId];
    if (reader.pvtpm != null && !reader.pvtpm.isEmpty())
    {
      EntryRef t = reader.pvtpm.getHead();
      Entry elem = t.pin(IPinnable.WRITE);
      buf.setKind(elem.kind);
      buf.setTuple(elem.tuple);
      buf.setTs(elem.ts);
      buf.setTotalOrderingGuarantee(elem.tsorderGuarantee);
      t.advance(true, SimplePageManager.NO_EVICT);
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
      return buf;
    }
    EntryRef rhead = reader.head;
    EntryRef rtail = reader.tail;
    if (rhead.equals(rtail))
      return null;

    // copy the queue element to given buffer
    Entry node = rhead.pin(IPinnable.WRITE);
    e = node.get(buf);
    int curReaders = --node.readers;
    // advance the node
    // If the reader is the last reader of this node, remove the node
    rhead.advance( curReaders == 0 , SimplePageManager.NO_EVICT);
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      if (e != null)
      {
        ITuplePtr tuple = e.getTuple();
        if (tuple != null)
        {
          TupleFactory tf = (TupleFactory) tupleFactory;
          tf.addRefCountLog(tuple.getId(), hashCode() + " dequeue readers="+curReaders + " " + e.toString());
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
    if (activeReaders.get(readerId) == false)
      return null;

    QueueElement e = null;

    Stub reader = readers[readerId];
    if (reader.pvtpm != null && !reader.pvtpm.isEmpty())
    {
      EntryRef t = reader.pvtpm.getHead();
      Entry node = t.pin(IPinnable.WRITE);
      e = node.get(buf);
    } 
    else
    {
      EntryRef rhead = reader.head;
      EntryRef rtail = reader.tail;
      if (rhead.equals(rtail))
        return null;
      // copy the queue element to given buffer
      Entry node = rhead.pin(IPinnable.READ);
      e = node.get(buf);
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
    Stub reader = readers[readerId];
    if (reader.pvtpm != null && !reader.pvtpm.isEmpty())
      return false;
    return (reader.tail.equals(reader.head));
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
    Stub reader = readers[readerId];
    if (reader.pvtpm != null && !reader.pvtpm.isEmpty())
    {
      return reader.pvtpm.getSize();
    } else
    {
      EntryRef rtail = reader.tail;
      EntryRef rhead = reader.head;
      if (rtail.equals(rhead))
        return 0;
      
      EntryRef t = rhead.clone();
      int pos = 0;
      while (!t.equals(rtail) )
      {
        t.advance(false, SimplePageManager.NO_EVICT);
        pos++;
      }
      return pos;
    }
  }

  @SuppressWarnings("unchecked")
  public void dumpElements(int readerId, IDumpContext dumper)
  {
    Stub reader = readers[readerId];
    if (reader.pvtpm != null && !reader.pvtpm.isEmpty())
    {
      reader.pvtpm.dump(dumper);
    } else
    {
      EntryRef rhead = reader.head;
      EntryRef rtail = reader.tail;
      if (rtail.equals(rhead))
        return;
      
      EntryRef t = rhead.clone();
      while (!t.equals(rtail) )
      {
        Entry e = t.pin(IPinnable.READ);
        e.dump(dumper);
        t.advance(false, SimplePageManager.NO_EVICT);
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
      int cnt = pm.getSize();
      dumper.writeln("Size", cnt);
      LogUtil.endDumpObj(dumper, tag);
      return;
    }
    pm.dump(dumper);
  }
  
  public synchronized boolean evict()
    throws ExecException
  {
    int cnt = 0;
    for (Stub reader : readers)
    {
      if (reader != null)
      {
        if (reader.pvtpm != null && reader.pvtpm.evict(false))
          cnt++;
      }
    }
    return (cnt > 0) | pm.evict(false);
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
