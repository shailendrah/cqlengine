/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/stores/WinStoreImpl.java /main/30 2010/07/27 03:08:03 sbishnoi Exp $ */

/* Copyright (c) 2006, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    07/23/10 - XbranchMerge sbishnoi_bug-9834643_ps3 from
                        st_pcbpel_11.1.1.4.0
 sbishnoi    07/21/10 - fix bug 9834643
 udeshmuk    04/22/09 - use expiredW in place of visibleW in
                        compareConsectiveTuples
 udeshmuk    01/21/09 - API to compare two consecutive tuples in store..
 hopark      10/10/08 - remove statics
 hopark      06/19/08 - logging refactor
 hopark      03/21/08 - fix refcnt
 hopark      03/03/08 - set TupleFactory for list
 hopark      02/25/08 - fix tuple eviction
 hopark      02/05/08 - parameterized error
 hopark      12/27/07 - support xmllog
 hopark      12/05/07 - cleanup spill
 hopark      11/15/07 - init NodeFac
 hopark      11/02/07 - share nodeFactory
 hopark      11/29/07 - remove AtomicInteger
 hopark      10/15/07 - add evict
 hopark      10/22/07 - remove TimeStamp
 najain      05/24/07 - add getNumElems
 hopark      06/20/07 - cleanup
 hopark      06/07/07 - use LogArea
 hopark      05/23/07 - debuglogging
 hopark      04/30/07 - refcount debug
 najain      04/09/07 - ref count bugs
 najain      03/14/07 - cleanup
 najain      03/12/07 - bug fix
 najain      02/13/07 - cleanup
 najain      02/06/07 - coverage
 najain      01/04/07 - spill over support
 najain      12/04/06 - stores are not storage allocators
 najain      12/04/06 - remove factory from store
 najain      08/16/06 - concurrency issues
 najain      08/02/06 - refCount optimizations
 najain      07/26/06 - multi-threading support 
 parujain    07/26/06 - Generic object 
 parujain    07/25/06 - use generic linkedlist 
 najain      07/18/06 - ref-count tuples 
 najain      07/13/06 - ref-count timeStamp support 
 najain      07/10/06 - add getFactory 
 najain      07/03/06 - cleanup
 najain      06/27/06 - add remove 
 najain      06/27/06 - add initialize 
 najain      06/15/06 - bug fix 
 najain      06/14/06 - bug fix 
 najain      05/08/06 - sharing support 
 najain      05/06/06 - bug fix 
 najain      04/19/06 - winstore implements relstore 
 najain      03/30/06 - bug fix 
 anasrini    03/24/06 - instantiate listStore 
 najain      03/17/06 - bugs etc.
 skaluska    03/13/06 - misc.
 anasrini    03/13/06 - should extend ExecStore 
 najain      03/06/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/stores/WinStoreImpl.java /main/30 2010/07/27 03:08:03 sbishnoi Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.stores;

import java.util.ArrayList;
import java.util.BitSet;

import oracle.cep.common.Constants;
import oracle.cep.common.EventTimestamp;
import oracle.cep.dataStructures.internal.ExpandableArray;
import oracle.cep.dataStructures.internal.ITimedTupleSinglyList;
import oracle.cep.dataStructures.internal.ITimedTupleSinglyListNode;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.internals.windows.Window;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.service.ExecContext;
import oracle.cep.util.StringUtil;

/**
 * WinStoreImpl class This is the store used by a range window synopsis. Note
 * that since WinStoreImpl also implements RelStore, it only works for tuples
 * and not any other StorageElement. Although WinStoreImpl implements RelStore,
 * it does not do so in a general manner - only a linear scan of all the tuples
 * is supported. In some sense, the reader (RelStore) will insert all tuples in
 * the same order.
 *
 * @author najain
 *
 */
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
  private ITimedTupleSinglyList                  list;

  private ArrayList<ITimedTupleSinglyList>       stubs;

  private ArrayList<WinStoreImplIter>            stubsIter;

  protected IAllocator<ITimedTupleSinglyList>    lFactory;

  private IAllocator<ITimedTupleSinglyListNode>  nFactory;
  
  private FactoryManager                         factoryMgr;
  
  /** Number of stubs/readers */
  private int                                    numStubs;

  private BitSet                                 activeStubs;
  
  /** A flag to check if the tuples of store is recovered after loading data from snapshot  */
  private boolean                                isRecovered;

  /**
   * Constructor
   * @param ec TODO
   * @param factory
   *          TupleFactory
   * @throws ExecException
   */
  @SuppressWarnings("unchecked")
  public WinStoreImpl(ExecContext ec, IAllocator<ITuplePtr> factory) throws ExecException
  {
    super(ec, ExecStoreType.WINDOW);

    this.factory = factory;
    this.factoryMgr = ec.getServiceManager().getFactoryManager();
    
    numStubs = 0;
    activeStubs = new BitSet();

    /** create the master list */
    lFactory = factoryMgr.get(FactoryManager.TTSINGLY_LIST_FACTORY_ID);
    nFactory = factoryMgr.get(FactoryManager.TTSINGLY_LIST_NODE_FACTORY_ID);
    list = lFactory.allocate();
    list.setTupleFactory(factory);
    list.setFactory(nFactory);
    
    /** add a dummy null element */
    list.add(null, -1, 0);

    //  unchecked conversion       
    stubs 
     = new ExpandableArray<ITimedTupleSinglyList>(Constants.INTIAL_NUM_STUBS); 
    stubsIter 
     = new ExpandableArray<WinStoreImplIter>(Constants.INTIAL_NUM_STUBS);
  }

  /**
   * Add a new reader for this store.
   * 
   * @return The stubId
   * @throws ExecException
   */
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
      stubs.set(stubNo, lFactory.allocate());  
          
      stubs.get(stubNo).setTupleFactory(factory);
      stubs.get(stubNo).setFactory(nFactory);
    } 
    else
      stubs.get(stubNo).clear();
    
    // All readers are reading the dummy
    stubs.get(stubNo).addLast(list);
   
    if (stubsIter.get(stubNo) == null)
    {
      stubsIter.set(stubNo, new WinStoreImplIter(factoryMgr, factory));  
    }
    else
      stubsIter.get(stubNo).clear();
    
    return stubNo;
  }

  /**
   * Delete a reader from this store.
   * 
   * @param stubId
   *          The stubId
   * @throws ExecException
   */
  public void removeStub(int stubId) throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_DDL, this, "removeStub", stubId);
    // make sure that the stubId is a valid one
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);

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

  /**
   * Set the head of stub to that node of global list of store,whose
   * tuple matches the given tuple.
   * @param tuple
   * @param stubId
   * @throws ExecException
   */
  public void setStubHead(ITuplePtr tuple, int stubId) throws ExecException
  {
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);

    ITimedTupleSinglyList stub = stubs.get(stubId);  
    ITuplePtr headTuple = null;
    if(stub.getSize() == 1)
    {      
      headTuple = stub.getFirst();
      if(!(tuple.compare(headTuple)))
      {
        boolean done = false;
        while(!done)
        {
          stub.addNext(list);         
          stub.remove();
          headTuple = stub.getFirst();
          done = headTuple.compare(tuple);
        }
      }
    }    
  }
  
  /** WinStore specific functions */

  /**
   * Inserts a Tuple into the store.
   * 
   * @param tuple
   *          Tuple to be inserted
   * @param stubId
   *          Stub that identifies the synopsis
   */
  public void insertTuple_w(ITuplePtr tuple, long ts, int stubId) 
   throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_INSERT, this, 
                  "insert_w", tuple, ts, stubId);

    // Note that no synchronization has been performed in this function. This
    // is intentional because it is assumed that the stub/reader is
    // automatically performing the serialization. There is a one-to-one mapping
    // between a given operator and a stubId. The operator is only performing
    // one function at a time, which leads to client cnotrolled serialization
    // The same applies to all the other stub related functions
    // (deleteOldestTuple_w, isEmpty_w etc.

    // make sure that the stubId is a valid one
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);

    ITimedTupleSinglyList stub = stubs.get(stubId);
    
    assert stub != null;
    
    // If this store is being shared by multiple synopsis (windows or relation),
    // then the first synopsis which is recovering on snapshot load will insert
    // its content into window store's global list. This syonpsis will set isRecovered
    // flag to true.
    
    // For the first recovered synopsis, we won't need the below mentioned special handling.
    
    // Special Handling for Window Store
    // Scenario:
    // If events in all synopsis(who shares this store) has exactly same set of data,
    // then we were not required to handle this scenario.
    // In the case where different synopsis has different data set, the insert opreation
    // into window store requires special handling.
    // e.g. SELECT * FROM S1[ROWS 4], S1[ROWS 2]
    //
    // In above query, we will have two windows on same stream, which is why both
    // windows will share same window store.
    // At any moment of time once windows are filled completely,
    // Synopsis (stub) for first row window should have 4 elements (stub-size=5){last element is tail.}
    // Synopsis (stub) for second row window should have 2 elements (stub-size=3).
    
    // Theory of operation:
    // When we insert an event into window store, we check if content of stub is already same
    // as the store's global list (by checking tail). 
    // If found same, then we we add a new node into stub as well as global list
    // and set currently tuple as this new node's element.
    // Otherwise, we add next element of global list into stub and global list is not modified.
    
    // Example:
    // While recovering from snapshot, if first synopsis inserts 4 events into window
    // store, then the global list will have 4 elements. (Say e1,e2,e3,e4)
    // The second synopsis which contains only last two records
    // of global list (Say e3,e4) because of ROWS 2.
    // While recovering second synopsis, we don't need to insert any new record 
    // into global list and the head of stub for the second synopsis should point
    // to the node which has element e3.
    // 
    // Because head of global list will be at e1, we will need to traverse until
    // we reaches e3. Once head is set, we can resume normal insert operation.

    if(isRecovered && tuple.isFirstRecovered())
      setStubHead(tuple, stubId);
    
    synchronized (list)
    {
      if (stub.isTailSame(list))
      {
        ITimedTupleSinglyListNode node = list.getTail();
        node.unpin();
        node = node.pin(IPinnable.WRITE); //unchecked cast
        node.setNodeElem(tuple);
        node.setTs(ts);
        node.setReaders(numStubs);
        node.unpin();
        factory.addRef(tuple);

        list.add(null, -1, 0);
      }

      if (ts != -1)
      {
        ITimedTupleSinglyListNode tail = 
            stub.getTail(); //unchecked cast
        long tsList = tail.getTs();
        tail.unpin();
        if (tsList == -1)
        {
          tail = tail.pin(IPinnable.WRITE);
          tail.setTs(ts);
          tail.unpin();
        }
      }
    }
    
    // cant assert that we are inserting the correct tuple, since we cannot
    // access the tail
    stub.addNext(list);
    
    assert tuple != null;
  }
  
  /**
   * Checks if the specified store is empty
   * 
   * @param stubId
   *          Stub that identifies the synopsis
   * @return true if empty or false
   */
  public boolean isEmpty_w(int stubId) throws ExecException
  {
    // make sure that the stubId is a valid one
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);

    ITimedTupleSinglyList stub = stubs.get(stubId);
    assert stub != null;
    if (stub.getSize() == 1)
      return true;
    return false;
  }

  /**
   * Returns the oldest tuple in the store for the specified synopsis.
   * 
   * @param stubId
   *          Stub that identifies the synopsis
   * @return The oldest tuple if any, else null
   */
  public ITuplePtr getOldestTuple_w(int stubId)
      throws ExecException
  {
    // make sure that the stubId is a valid one
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);

    ITimedTupleSinglyList stub = stubs.get(stubId);
    assert stub != null;

    ITuplePtr tuple = stub.getFirst();
    factory.addRef(tuple);
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_GET, this, 
                 "getOldest", stubId, tuple);
    return tuple;
  }

  /**
   * Returns the oldest timeStamp in the store for the specified synopsis.
   * 
   * @param stubId
   *          Stub that identifies the synopsis
   * @return The oldest timeStamp if any, else null
   */
  public long getOldestTimeStamp_w(int stubId) throws ExecException
  {
    // make sure that the stubId is a valid one
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);

    ITimedTupleSinglyList stub = stubs.get(stubId);
    assert stub != null;

    ITimedTupleSinglyListNode head = stub.getHead();
    long ts = head.getTs();
    head.unpin();
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_GET, this, 
            "getOldestTimeStamp_w", stubId, ts);
    return ts;
  }

  /**
   * Deletes the oldest tuple from the window
   */
  public void deleteOldestTuple_w(int stubId) throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_DELETE, this, "delete_w", stubId);

    // make sure that the stubId is a valid one
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);

    ITimedTupleSinglyList stub = stubs.get(stubId);
    assert stub != null;

    synchronized(list)
    {
      if (stub.getSize() == 1)
        throw new ExecException(ExecutionError.EMPTY_STORE);
  
      ITimedTupleSinglyListNode head = stub.getHead();
      head.unpin();
      head = head.pin(IPinnable.WRITE);
      boolean deleteTuple = head.decrementAndGet() == 0 ? true
          : false;
      ITuplePtr t = stub.remove();
      head.unpin();
      if (deleteTuple)
      {
        list.remove(true);
        factory.release(t);
      }
    }
  }

  /** RelStore specific functions */

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.stores.RelStore#insertTuple_r(oracle.cep.execution.internals.Tuple,
   *      int)
   */
  public void insertTuple_r(ITuplePtr tuple, int stubId)
      throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_INSERT, this, 
                  "insert_r", tuple, stubId);

    insertTuple_w(tuple, -1, stubId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.stores.RelStore#deleteTuple_r(oracle.cep.execution.internals.Tuple,
   *      int)
   */
  public void deleteTuple_r(ITuplePtr tuple, int stubId)
      throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_DELETE, this, 
                  "delete_r", tuple, stubId);

    ITuplePtr tPtr = getOldestTuple_w(stubId);
    assert tPtr.equals(tuple);
    factory.release(tPtr);

    deleteOldestTuple_w(stubId);
  }

  /**
   * Scan the *entire* contents of stubId
   * 
   * @return The tuple iterator to scan the tuples.
   */
  public TupleIterator getScan_r(int stubId) throws ExecException
  {
    // make sure that the stubId is a valid one
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);

    ITimedTupleSinglyList stub = stubs.get(stubId);
    assert stub != null;

    WinStoreImplIter iterR = stubsIter.get(stubId);
    assert iterR != null;

    iterR.initialize(this, stub);

    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_SCAN_START, this, 
                  "getScan_r", stubId, iterR);

    return iterR;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.stores.RelStore#releaseScan_r(oracle.cep.execution.internals.TupleIterator,
   *      int)
   */
  public void releaseScan_r(TupleIterator iter, int stubId)
      throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_SCAN_STOP, this, 
                  "releaseScan_r", iter, stubId);
    // make sure that the stubId is a valid one
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);

    WinStoreImplIter iterR = stubsIter.get(stubId);
    assert iterR != null;

    iterR.release();
  }

  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("<WinStoreImpl id=\"" + id + "\" numStubs=\"" + numStubs + "\">");
    sb.append(factory.toString());
    return sb.toString();
  }

  public synchronized void dump(IDumpContext dumper) 
  {
    if (!dumper.isVerbose())
    {
      String tag = LogUtil.beginDumpObj(dumper, this);
      list.dump(dumper);
      LogUtil.endDumpObj(dumper, tag);
      return;
    }
    String dumperKey = StringUtil.getBaseClassName(this);
    IDumpContext w = dumper.openDumper(dumperKey);
    String tag = LogUtil.beginDumpObj(w, this);
    list.dump(w);
    LogUtil.endDumpObj(w, tag);
    w.closeDumper(dumperKey, dumper);
  }

  public boolean evict()
    throws ExecException
  {
    boolean b = list.evict();
    b |= super.evict();
    return b;
  }
  
  public boolean compareConsecutiveTuples(Window window, int stubId) throws ExecException
  {
    // make sure that the stubId is a valid one
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);

    ITimedTupleSinglyList stub = stubs.get(stubId);
    assert stub != null;

    ITimedTupleSinglyListNode head = stub.getHead(); 
    ITimedTupleSinglyListNode nextToHead = head.getNext(stub);
    //if there is no next element then return false;
    if(nextToHead == null) 
      return false;
    long headTimestamp = head.getTs();
    long nextToHeadTimestamp = nextToHead.getTs();
    nextToHead.unpin();
    head.unpin();
    
    //TODO: may add logging info
    
    //return true if next tuple in synopsis has higher ts than the one
    //currently at the head. false if values are equal.
    // next ts < headTs won't happen.
    EventTimestamp tempTs = new EventTimestamp(Constants.MIN_EXEC_TIME);
    EventTimestamp headTs = new EventTimestamp(Constants.MIN_EXEC_TIME);
    EventTimestamp nextToHeadTs = new EventTimestamp(Constants.MIN_EXEC_TIME);
    
    tempTs.setTime(headTimestamp);
    window.expiredW(tempTs, headTs);
    tempTs.setTime(nextToHeadTimestamp);
    window.expiredW(tempTs, nextToHeadTs);
    
    return nextToHeadTs.getTime() > headTs.getTime(); 
  }
  

  /**
   * Getters for number of stubs
   */
  public int getNumStubs()
  {
    return numStubs;
  }
  
  public int getNumElems()
  {
    return list.getSize() - 1;
  }

  /**
   * Returns true if the tuples of store is recovered after loading data from snapshot
   */
  public boolean isRecovered()
  {
    return this.isRecovered;
  }
  
  /**
   * Set to true if the tuples of store is recovered after loading data from snapshot
   */
  public void setRecovered(boolean flag)
  {
    this.isRecovered = flag;
  }
}
