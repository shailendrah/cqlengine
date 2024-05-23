/* $Header: pcbpel/cep/server/src/oracle/cep/execution/stores/stored/WinStoreImpl.java /main/6 2009/04/02 23:58:00 udeshmuk Exp $ */

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
    udeshmuk    01/21/09 - API to compare two consecutive tuples in store.
    sborah      12/17/08 - handle constants
    hopark      12/02/08 - move LogLevelManager to ExecContext
    hopark      10/10/08 - remove statics
    hopark      06/19/08 - logging refactor
    hopark      02/20/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/stores/stored/WinStoreImpl.java /main/6 2009/04/02 23:58:00 udeshmuk Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.stores.stored;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.BitSet;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ExpandableArray;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.stored.TuplePtr;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.StoreImplIter;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.internals.windows.Window;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.ExecStoreType;
import oracle.cep.execution.stores.RelStore;
import oracle.cep.execution.stores.WinStore;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.memmgr.SimplePageManager;
import oracle.cep.memmgr.IAllocator.NameSpace;
import oracle.cep.memmgr.SimplePageManager.BaseEntry;
import oracle.cep.memmgr.SimplePageManager.EntryGen;
import oracle.cep.memmgr.SimplePageManager.PagePtr;
import oracle.cep.memmgr.SimplePageManager.EntryRef;
import oracle.cep.memmgr.factory.paged.TupleFactory;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;

@DumpDesc(attribTags={"Id", "PhyId", "NumStubs"}, 
          attribVals={"getId", "getPhyId", "getNumStubs"},
          infoLevel=LogLevel.STORE_INFO,
          evPinLevel=LogLevel.STORE_TUPLE_PINNED,
          evUnpinLevel=LogLevel.STORE_TUPLE_UNPINNED,
          dumpLevel=LogLevel.STORE_DUMP,
          verboseDumpLevel=LogLevel.STORE_DUMPELEMS)
public class WinStoreImpl extends ExecStore 
  implements WinStore, RelStore
{
  // Page Layout
  static  class Entry extends BaseEntry implements Externalizable
  {
    private static final long serialVersionUID = 1820724140175802831L;

    ITuplePtr   tuple;
    long        ts;
    int         readers;

    public Entry()
    {
      super();
    }
    
    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException
    {
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
      ts = in.readLong();
      readers = in.readInt();
    }
    
    public void writeExternal(ObjectOutput out) throws IOException
    {
      int tupleFacId = -1;
      long tupleId = -1l;
      if (tuple != null)
      {
        assert (tuple instanceof TuplePtr);
        TuplePtr ptuple = (TuplePtr) tuple;
        TupleFactory   tupleFactory = ptuple.getFactory();
        tupleFacId = tupleFactory.getId();
        tupleId = tuple.getId();
      }
      out.writeInt(tupleFacId);
      out.writeLong(tupleId);
      out.writeLong(ts);
      out.writeInt(readers);
    }
  };

  /** Number of stubs/readers */
  private int              numStubs;
  private BitSet           activeStubs;
  private ArrayList<Stub>  stubs;
  
  private SimplePageManager pm;
  private TupleFactory   tupleFactory;
  
  /** A flag to check if the tuples of store is recovered after loading data from snapshot  */
  private boolean        isRecovered;
  
  static class Stub
  {
    EntryRef     tail;
    EntryRef     head;
  }
  
  private static final int      s_pageSize = 1000;

  private static final EntryGen s_entryGen = new EntryGen() {
    public BaseEntry create() 
    {
      return new Entry();
    }
  };
   
  public WinStoreImpl(ExecContext ec, IAllocator<ITuplePtr> factory) throws ExecException
  {
    super(ec, ExecStoreType.WINDOW);

    this.factory = factory;
    numStubs = 0;
    isRecovered = false;
    activeStubs = new BitSet();
    stubs = new ExpandableArray<Stub>(Constants.INTIAL_NUM_STUBS);
    pm = new SimplePageManager(FactoryManager.WINSTOREPAGE_FACTORY_ID, 
                               NameSpace.WINSTOREPAGE, s_pageSize, s_entryGen);
  }
  


  @SuppressWarnings("unchecked")
  public void setTupleFactory(IAllocator<ITuplePtr> tupleFactory)
  {
    this.tupleFactory = (TupleFactory) tupleFactory;
  }
  
  @Override
  public int addStub() throws ExecException
  {
    int stubNo = activeStubs.nextClearBit(0);
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_DDL, this, "addStub", stubNo);

    activeStubs.set(stubNo);
    numStubs++;

    // For a stub start will point to the next location from where it
    // will start reading
    
    if(stubs.get(stubNo) == null)
    {
      stubs.set(stubNo, new Stub());  
    }
    
    EntryRef pmhead = pm.getHead();
    stubs.get(stubNo).head = pmhead.clone();
    stubs.get(stubNo).tail = pmhead.clone();
    
    return stubNo;
  }

  @Override
  public int getNumElems()
  {
    assert false;
    return 0;
  }
  
  /**
   * Returns the number of active stubs associated with this store 
   */
  @Override
  public int getNumStubs()
  {
    return this.numStubs;
  }
  
  /**
   * Returns true if the tuples of store is recovered after loading data from snapshot
   */
  @Override
  public boolean isRecovered()
  {
    return this.isRecovered;
  }
  
  /**
   * Set to true if the tuples of store is recovered after loading data from snapshot
   */
  @Override
  public void setRecovered(boolean flag)
  {
    this.isRecovered = flag;
  }

  @Override
  public void removeStub(int stubId) throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_DDL, this, "removeStub", stubId);
    // make sure that the stubId is a valid one
    assert (activeStubs.get(stubId));

    // remove all the messages
    while (true)
    {
      try
      {
        deleteOldestTuple_w(stubId);
      }
      catch (ExecException e)
      {
        if (e.getErrorCode() != ExecutionError.EMPTY_STORE)
          throw e;
        else
          break;
      }
    }

    numStubs--;
    assert numStubs >= 0;

    activeStubs.clear(stubId);
  }

  public void insertTuple_w(ITuplePtr tuple, long ts, int stubId)
    throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_INSERT, this, 
        "insert_w", tuple, ts, stubId);

    // Note that no synchronization has been performed in this function. This
    // is intentional because it is assumed that the stub/reader is
    // automatically performing the serialization. There is a one-to-one mapping
    // between a given operator and a stubId. The operator is only performing
    // one function at a time, which leads to client controlled serialization
    // The same applies to all the other stub related functions
    // (deleteOldestTuple_w, isEmpty_w etc.
    
    // make sure that the stubId is a valid one
    assert (activeStubs.get(stubId));
    
    Stub stub = stubs.get(stubId);
    EntryRef stail = stub.tail;
    EntryRef pmtail = pm.getTail();
    if (stail.equals(pmtail))
    {
      Entry e = stail.pin(IPinnable.WRITE);
      e.tuple = tuple;
      e.ts = ts;
      e.readers = numStubs;
      assert tuple != null;
      factory.addRef(tuple);
      stail.advance(false, SimplePageManager.NO_EVICT);
      synchronized(pmtail)
      {
        pmtail.copy(stail);
      }
    } else {
      if (ts != -1)
      {
        // this tuple was inserted by insert_r, now set the correct timestamp 
        Entry e = stail.pin(IPinnable.READ);
        if (e.ts == -1)
        {
          e.ts = ts;
          stail.setDirty(true);
        }
      }
      stail.advance(false, SimplePageManager.NO_EVICT);
    }
  }
  
  public boolean isEmpty_w(int stubId) throws ExecException
  {
    // make sure that the stubId is a valid one
    assert (activeStubs.get(stubId));

    Stub stub = stubs.get(stubId);
    return stub.tail.equals(stub.head);
  }
  
  public void deleteOldestTuple_w(int stubId) throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_DELETE, this, "delete_w", stubId);

    // make sure that the stubId is a valid one
    assert (activeStubs.get(stubId));

    Stub stub = stubs.get(stubId);
    if (stub.head.equals(stub.tail))
      throw new ExecException(ExecutionError.EMPTY_STORE);

    EntryRef t = stub.head;
    Entry e = t.pin(IPinnable.WRITE);
    EntryRef pmhead = pm.getHead();
    if (--e.readers == 0)
    {
      assert (t.equals(pmhead));
      
      factory.release(e.tuple);
      e.tuple = null;
      PagePtr oldp = null;
      if (t.getIndex() == (s_pageSize - 1))
      {
        oldp = t.getPage();
      } 
      t.advance(true, SimplePageManager.NO_EVICT);
      synchronized(pmhead)
      {
        pmhead.copy(t);
        if (oldp != null)
        {
          oldp.freePage();
        }
      }
    }
    else
    {
      t.advance(false, SimplePageManager.NO_EVICT);
    }
  }

  public long getOldestTimeStamp_w(int stubId) throws ExecException
  {
    // make sure that the stubId is a valid one
    assert (activeStubs.get(stubId));

    Stub stub = stubs.get(stubId);
    EntryRef t = stub.head;
    Entry e = t.pin(IPinnable.READ);
    long ts = e.ts; 
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_GET, this, 
        "getOldestTimeStamp_w", stubId, ts);
    return ts;
  }

  public ITuplePtr getOldestTuple_w(int stubId) throws ExecException
  {
    // make sure that the stubId is a valid one
    assert (activeStubs.get(stubId));

    Stub stub = stubs.get(stubId);
    EntryRef t = stub.head;
    Entry e = t.pin(IPinnable.READ);
    ITuplePtr tuple = e.tuple;
    factory.addRef(tuple);
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_GET, this, 
        "getOldest", stubId, tuple);
    return tuple;
  }


  public void insertTuple_r(ITuplePtr tuple, int stubId) throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_INSERT, this, 
        "insert_r", tuple, stubId);
    assert (activeStubs.get(stubId));

    // It is assumed that the reader will insert in a linear order
    insertTuple_w(tuple, -1, stubId);
  }

  public void deleteTuple_r(ITuplePtr tuple, int stubId) throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_DELETE, this, 
        "delete_r", tuple, stubId);
    assert (activeStubs.get(stubId));

    //ITuplePtr tPtr = getOldestTuple_w(stubId);
    //assert tPtr.equals(tuple);
    //factory.release(tPtr);

    deleteOldestTuple_w(stubId);
  }

  public TupleIterator getScan_r(int stubId) throws ExecException
  {
    // make sure that the stubId is a valid one
    assert (activeStubs.get(stubId));

    Stub stub = stubs.get(stubId);
    WinStoreImplIter iterR = new WinStoreImplIter(this, stub.tail, stub.head);
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_SCAN_START, this, 
        "getScan_r", stubId, iterR);

    return iterR;
  }

  public void releaseScan_r(TupleIterator iter, int stubId)
      throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_SCAN_STOP, this, 
        "releaseScan_r", iter, stubId);
    // make sure that the stubId is a valid one
    assert (activeStubs.get(stubId));
  }

  public void dump(IDumpContext dumper)
  {
    pm.dump(dumper);
  }

  public boolean evict() throws ExecException
  {
    return pm.evict(false);
  }

  public boolean compareConsecutiveTuples(Window window, int stubId)
  { //TODO: Correct implementation needs to be provided. How to do that?
    //probably need to use EntryRef.getPage().getNext() or some such thing to access next tuple
    return false;
  }

  class WinStoreImplIter extends StoreImplIter
  {
    EntryRef tail;
    EntryRef head;
    
    public WinStoreImplIter(ILoggable target, EntryRef h, EntryRef t)
    {
      tail = h.clone();
      head = t.clone();
      initialize(target);
    }

    /*
     * (non-Javadoc)
     * 
     * @see oracle.cep.execution.internals.TupleIterator#getNext()
     */
    public ITuplePtr getNext() throws ExecException
    {
      if (tail.equals(head))
        return null;

      Entry e = head.pin(IPinnable.READ);
      ITuplePtr tuple = e.tuple;
      factory.addRef(tuple);
      head.advance(false, SimplePageManager.NO_EVICT);
      
      LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_SCAN,
                        logTarget, tuple);
      return tuple;
    }
  }
}
