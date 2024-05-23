/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/stores/RelStoreImpl.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */
/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares RelStoreImpl in package oracle.cep.execution.stores.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 sborah    12/17/08 - handle constants
 hopark    10/10/08 - remove statics
 hopark    06/19/08 - logging refactor
 hopark    03/21/08 - fix refcnt
 hopark    03/03/08 - set TupleFactory for list
 hopark    02/25/08 - fix tuple eviction
 hopark    02/05/08 - parameterized error
 hopark    12/27/07 - support xmllog
 hopark    12/07/07 - cleanup spill
 hopark    11/15/07 - init NodeFac
 hopark    11/06/07 - change getHandle api
 hopark    10/15/07 - add evict
 hopark    09/22/07 - use ListNodeHandle
 hopark    09/19/07 - stubBits optimization
 hopark    08/31/07 - list api change
 sbishnoi  08/27/07 - add refCol
 najain    05/24/07 - add getNumElems
 hopark    06/20/07 - cleanup
 hopark    06/07/07 - use LogArea
 hopark    05/23/07 - debuglogging
 hopark    05/16/07 - remove printStackTrace
 hopark    05/14/07 - fix assertion in pinTuple
 najain    05/11/07 - variable length support
 hopark    05/08/07 - ITuple api cleanup
 hopark    04/09/07 - fix pincount
 najain    03/14/07 - cleanup
 najain    03/12/07 - bug fix
 najain    03/08/07 - cleanup
 najain    03/06/07 - bug fix
 najain    02/05/07 - coverage
 najain    01/04/07 - spill over support
 hopark    01/09/07 - RelStoreImplIter change
 najain    12/04/06 - stores are not storage allocators
 parujain  11/30/06 - Use DoublyListIter Factory
 najain    11/08/06 - DoublyList is a StorageElement
 najain    08/16/06 - concurrency issues
 najain    08/02/06 - refCount optimizations
 parujain  07/28/06 - Generic doubly linkedlist 
 najain    07/18/06 - ref-count tuples 
 najain    07/10/06 - add getFactory 
 najain    07/03/06 - cleanup
 najain    06/15/06 - cleanup
 najain    06/13/06 - query deletion support
 najain    06/05/06 - add addReaderTuple 
 najain    05/19/06 - bug fix
 ayalaman  04/28/06 - implement some abstract methods 
 najain    04/19/06 - add Refcounting methods 
 anasrini  03/22/06 - use Constants.BITS_PER_BYTE 
 najain    03/22/06 - add columns for storing bitsets for stubs
 anasrini  03/21/06 - add setNumStubs 
 najain    03/09/06 - subclass of ExecStore
 skaluska  03/08/06 - Creation
 skaluska  03/08/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/stores/RelStoreImpl.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.stores;

import java.util.ArrayList;
import java.util.BitSet;

import java.util.logging.Level;

import oracle.cep.dataStructures.internal.ExpandableArray;
import oracle.cep.dataStructures.internal.ITupleDoublyList;
import oracle.cep.dataStructures.internal.ITupleDoublyListNode;
import oracle.cep.dataStructures.internal.IListNodeHandle;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.execution.ExecException;
import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ITupleDoublyListIter;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.service.ExecContext;
import oracle.cep.util.BitVectorUtil;
import oracle.cep.util.StringUtil;

/**
 * RelStoreImpl
 *
 * @author skaluska
 */
@DumpDesc(attribTags={"Id", "PhyId"}, 
          attribVals={"getId", "getPhyId"},
          infoLevel=LogLevel.STORE_INFO,
          evPinLevel=LogLevel.STORE_TUPLE_PINNED,
          evUnpinLevel=LogLevel.STORE_TUPLE_UNPINNED,
          dumpLevel=LogLevel.STORE_DUMP,
          verboseDumpLevel=LogLevel.STORE_DUMPELEMS)
public class RelStoreImpl extends ExecStore implements RelStore
{
  /** Storage manager for the list */
  private IAllocator<ITupleDoublyList> lFactory;

  /** Currently, the elements are stored in a linked list */
  private ITupleDoublyList               listStore;

  private BitSet                          activeStubs;
  
  /**
   * This is used by readers for tracking their current position in the window
   * store. The current position in the window is maintained by the iterator.
   * Note that no synchronization is performed, so the stubs are only used for
   * readers (not writers). In this file, stubs and readers are used
   * interchangibly.
   */
  private ArrayList<TupleIterator>        stubs;

  // Column numbers for storing insertions and deletions
  private Column                          colIns;
  
  // Column number for storing reference to DoublyList Node 
  // which points to this tuple
  private Column                          colRef;

  private IAllocator<ITupleDoublyListNode> nFactory;
  
  /** Storage manager for doubly list iterator */
  private IAllocator<ITupleDoublyListIter> iFactory;

  /** Number of active stubs associated with this store */
  private int numStubs;
  
  /** A flag to check if the tuples of store is recovered after loading data from snapshot  */
  private boolean isRecovered;
  
  /**
   * Get the column where bits for insertion and deletion are stored
   * @return
   */
  public Column getColIns()
  {
    return this.colIns;
  }
  /**
   * @param colIns
   *          The colIns to set.
   */
  public void setColIns(Column colIns)
  {
    this.colIns = colIns;
  }

  /**
   * @param colRef
   *          The colRef is set
   */
  public void setColRef(Column colRef)
  {
    this.colRef = colRef;
  }
  
  /**
   * Constructor for RelStoreImpl
   * @param ec TODO
   */
  public RelStoreImpl(ExecContext ec, IAllocator<ITuplePtr> factory)
  {
    super(ec, ExecStoreType.RELATION);

    this.factory = factory;
    stubs = new ExpandableArray<TupleIterator>(Constants.INTIAL_NUM_STUBS);
    try
    {
      FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
      lFactory = factoryMgr.get(FactoryManager.TDOUBLY_LIST_FACTORY_ID);
      iFactory = factoryMgr.get(FactoryManager.TDOUBLY_LIST_ITER_FACTORY_ID);
      nFactory = factoryMgr.get(FactoryManager.TDOUBLY_LIST_NODE_FACTORY_ID);
      listStore = lFactory.allocate();
      listStore.setTupleFactory(factory);
      listStore.setFactory(nFactory);
    }
    catch (ExecException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
    }
    activeStubs = new BitSet();
    numStubs = 0;
    isRecovered = false;
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

    // Start an iterator for this stub
    RelStoreImplIter iter = new RelStoreImplIter(stubNo, colIns, factory);
   
    assert stubs.get(stubNo) == null;
    stubs.set(stubNo, iter);
   
    numStubs++;
    return stubNo;
  }

  /**
   * Delete a reader from this store.
   * 
   * @param stubId
   *          The stubId
   */
  public void removeStub(int stubId) throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_DDL, this, "removeStub", stubId);
    // It should be a valid reader
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);
 
    synchronized(listStore)
    {
      TupleIterator iter = getScan_r(stubId);
      while (true)
      {
        // delete all the elements for this reader
        ITuplePtr tuple = iter.getNext();
        if (tuple == null)
        {        
          break;
        }
        deleteTuple_r(tuple, stubId);
      }
      releaseScan_r(iter, stubId);
    }
    
    stubs.set(stubId, null);
    activeStubs.clear(stubId);
    numStubs--;
  }

  public int getNumElems()
  {
    return listStore.getSize();
  }
  
  /**
   * Returns the number of active stubs associated with this store 
   */
  public int getNumStubs()
  {
    return this.numStubs;
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

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.stores.RelStore#insertTuple_r(oracle.cep.execution.internals.Tuple,
   *      int)
   */
  public void insertTuple_r(ITuplePtr tuplePtr, int stubId) throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_INSERT, this, 
                  "insert_r", tuplePtr, stubId);

    // it should be a valid stub
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);

    ITuple          tuple = tuplePtr.pinTuple(IPinnable.WRITE);
    byte[]          stubBits;
    
    synchronized (listStore)
    {
      try
      {
        stubBits = tuple.bValueGet(colIns.getColnum());
      }
      catch (ExecException ex)
      {
        tuplePtr.unpinTuple();
        throw new ExecException(ExecutionError.INSERT_BITVECTOR_ABSENT, colIns.getColnum());
      }
      
      // Does the element needs to be inserted in the list
      //if (BitVectorUtil.isNull(stubBits))
        //listStore.add(tuplePtr);
      
      if (BitVectorUtil.isNull(stubBits) || tuplePtr.isRecovered())
      {
        ITupleDoublyListNode newNode;
        newNode = listStore.add(tuplePtr);
        tuple.oValueSet(colRef.getColnum(), newNode.getHandle(listStore));
        newNode.unpin();
        factory.addRef(tuplePtr);
      }

      byte[] stubBits1 = BitVectorUtil.setBit(stubBits, stubId);
      if (stubBits != stubBits1)
        tuple.bValueSet(colIns.getColnum(), stubBits1, stubBits1.length);
      
    }
    tuplePtr.unpinTuple();
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.stores.RelStore#deleteTuple_r(oracle.cep.execution.internals.Tuple,
   *      int)
   */
  @SuppressWarnings("unchecked")
  public void deleteTuple_r(ITuplePtr tuplePtr, int stubId) throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_DELETE, this, 
                  "delete_r", tuplePtr, stubId);

    // it should be a valid stub
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);

    ITuple tuple = tuplePtr.pinTuple(IPinnable.WRITE);
    byte[] stubBits;
    
    synchronized (listStore)
    {
      try
      {
        stubBits = tuple.bValueGet(colIns.getColnum());
      }
      catch (ExecException ex)
      {
        tuplePtr.unpinTuple();
        throw new ExecException(ExecutionError.INSERT_BITVECTOR_ABSENT, colIns.getColnum());
      }
  
      byte[] stubBits1 = BitVectorUtil.clearBit(stubBits, stubId);
      if (stubBits != stubBits1)
        tuple.bValueSet(colIns.getColnum(), stubBits1, stubBits1.length);
  
      // Does the tuple need to be deleted from the list
      if (BitVectorUtil.isNull(stubBits))
      {
        assert listStore.contains(tuplePtr);
        listStore.remove((IListNodeHandle)tuple.oValueGet(colRef.getColnum()));
        factory.release(tuplePtr);
      }
    }
    tuplePtr.unpinTuple();
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.stores.RelStore#getScan_r(int)
   */
  public TupleIterator getScan_r(int stubId) throws ExecException
  {
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);

    TupleIterator iter = stubs.get(stubId);
    assert iter != null;
    assert iter instanceof RelStoreImplIter;
    RelStoreImplIter iterR = (RelStoreImplIter) iter;
    iterR.initialize(this, listStore, iFactory);
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
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);

    assert iter != null;
    assert iter instanceof RelStoreImplIter;
    RelStoreImplIter iterR = (RelStoreImplIter) iter;
    iterR.release(iFactory);
  }
  
  public synchronized void dump(IDumpContext dumper) 
  {
    if (!dumper.isVerbose())
    {
      String tag = LogUtil.beginDumpObj(dumper, this);
      listStore.dump(dumper);
      LogUtil.endDumpObj(dumper, tag);
      return;
    }
    String dumperKey = StringUtil.getBaseClassName(this);
    IDumpContext w = dumper.openDumper(dumperKey);
    String tag = LogUtil.beginDumpObj(w, this);
    listStore.dump(w);
    LogUtil.endDumpObj(w, tag);
    w.closeDumper(dumperKey, dumper);
  }

  public boolean evict()
    throws ExecException
  {
    boolean b = listStore.evict();
    b |= super.evict();
    return b;
  }
}
