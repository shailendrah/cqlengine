/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/stores/PartnWindowStoreImpl.java /main/54 2012/06/20 05:24:30 pkali Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Implements PartnWindowStore in package oracle.cep.execution.stores.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 Tuples are stored in two data structures, HashIndex and DoublyList.
 HashIndex is for the partitions and DoublList is for supporting range.
 The reference counts of Tuple is only managed by the doublyList in order to
 follow the same semantic as other stores.
 (e.g. copying into doublyList : +1).
 
 MODIFIED    (MM/DD/YY)
    pkali     05/29/12 - added windows size tracking logic
    sbishnoi  12/08/11 - support for variable duration partition window
    anasrini  12/19/10 - replace eval() with eval(ec)
    sborah    12/17/08 - handle constants
    hopark    12/02/08 - move LogLevelManager to ExecContext
    udeshmuk  11/13/08 - refactoring into superclass
    udeshmuk  11/07/08 - make BasePartnWindowStoreImpl as the superclass.
    hopark    10/10/08 - remove statics
    udeshmuk  09/24/08 - 
    anasrini  09/20/08 - add method replaceOldestTuple_p
    hopark    06/19/08 - logging refactor
    hopark    03/03/08 - set TupleFactory for list
    hopark    02/25/08 - fix tuple eviction
    hopark    02/05/08 - parameterized error
    hopark    12/26/07 - support xmllog
    hopark    12/07/07 - cleanup spill
    hopark    11/15/07 - init NodeFac
    hopark    10/31/07 - change DoublyList api
    hopark    12/18/07 - change iterator semantics
    hopark    10/15/07 - add evict
    hopark    10/22/07 - remove TimeStamp
    hopark    09/22/07 - use ListNodeHandle
    hopark    09/19/07 - stubBits optimization
    hopark    09/17/07 - use getFullScan
    rkomurav  09/13/07 - add getpartitioniter
    sbishnoi  09/07/07 - add RefCol
    hopark    09/07/07 - eval refactor
    hopark    08/30/07 - bind perf enh
    hopark    08/29/07 - getOldestTimedTuple perf enh
    hopark    07/18/07 - fix partition attr bug
    najain    05/24/07 - add getNumElems
    hopark    06/20/07 - cleanup
    hopark    06/15/07 - add getIndexes
    hopark    06/07/07 - use LogArea
    hopark    05/23/07 - debuglogging
    najain    05/11/07 - variable length support
    hopark    05/08/07 - ITuple api cleanup
    hopark    05/04/07 - fix assertion in removeStub
    hopark    04/30/07 - add getOldestTimedTuple_p
    hopark    04/24/07 - fix refcount
    hopark    04/20/07 - change pinTuple semantics
    hopark    04/14/07 - fix pincount
    najain    04/09/07 - bug fix
    hopark    04/09/07 - fix pincount
    hopark    03/27/07 - memmgr reorg
    najain    03/14/07 - cleanup
    hopark    03/07/07 - fix bug of pinning mode
    najain    03/12/07 - bug fix
    najain    03/08/07 - cleanup
    hopark    03/07/07 - spill-over support
    najain    03/06/07 - bug fix
    najain    03/06/07 - bug fix
    najain    02/05/07 - coverage
    hopark    01/26/07 - remove TimedTuple
    najain    01/24/07 - bug fix
    hopark    12/06/06 - support range
    najain    12/04/06 - stores are not storage allocators
    parujain  11/30/06 - Use DoublyListIter Factory
    najain    11/08/06 - DoublyList is a StorageElement
    najain    09/08/06 - concurrency support
    ayalaman  08/02/06 - partition window implementation
    skaluska  02/16/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/stores/PartnWindowStoreImpl.java /main/54 2012/06/20 05:24:30 pkali Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.stores;

import oracle.cep.common.Constants;
import oracle.cep.execution.ExecException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.internals.Column;

import java.util.ArrayList;

import oracle.cep.dataStructures.internal.ITupleDoublyListIter;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IPinnable;

import java.util.BitSet;

import oracle.cep.dataStructures.internal.ExpandableArray;
import oracle.cep.dataStructures.internal.ITupleDoublyListNode;
import oracle.cep.dataStructures.internal.IListNodeHandle;
import oracle.cep.dataStructures.internal.IPartition;
import oracle.cep.dataStructures.internal.IPartitionIter;
import oracle.cep.dataStructures.internal.IPartitionNode;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.service.ExecContext;
import oracle.cep.util.BitVectorUtil;
import oracle.cep.util.StringUtil;

/**
 * Store used for partition window synopsis. This store maintains a table of
 * partition keys (header tuples) with a hash index on them. Each header tuple
 * has a pointer to the list of tuples as well as the count of rows
 * in the partition. The 'pointers' are managed
 * using the Object columns in the tuples.
 *
 * @author ayalaman
 */
@DumpDesc(attribTags={"Id", "PhyId"}, 
          attribVals={"getId", "getPhyId"},
          infoLevel=LogLevel.STORE_INFO,
          evPinLevel=LogLevel.STORE_TUPLE_PINNED,
          evUnpinLevel=LogLevel.STORE_TUPLE_UNPINNED,
          dumpLevel=LogLevel.STORE_DUMP,
          verboseDumpLevel=LogLevel.STORE_DUMPELEMS)
public class PartnWindowStoreImpl extends BasePartnWindowStoreImpl 
  implements PartnWindowStore, RelStore
{
  private static final boolean ITER_VERIFY = false;      //double check if oldest_iter logic holds the assumption.

  /** active readers */
  private BitSet                          activeStubs;

  /** Iterators associated with each synopsis */
  private ArrayList<TupleIterator>        stubs;

  /**
   * List of timed tuples
   */
  private IPartition                      ttuples;

  /** oldest node in ttuples associated with each synopsis */
  private TimedTuple                             oldestScratch; // scratch tt for returning result
  private IPartitionIter                         oldestItr;  // iterator
  private TimedTuple                             oldest; // current oldest.
  
  // Column numbers for storing insertions and deletions
  private Column                          colIns;
  
  /** Number of active stubs associated with this store */
  private int                             numStubs;
   
  /** Storage manager for doubly list iterator */
  private IAllocator<ITupleDoublyListIter> iFactory;
  
  /** A flag to check if the tuples of store is recovered after loading data from snapshot  */
  private boolean                         isRecovered;
  
  /**
   * Constructor Unlike other stores, partition window store has two tuple
   * factories, one for the partition keys and the other for the data ttuples.
   * @param ec TODO
   * @param factory
   *          TupleFactory
   * @param hdrTupFac
   *          TupleFactory for header tuples.
   * @throws ExecException
   */
  public PartnWindowStoreImpl(ExecContext ec, 
                              IAllocator<ITuplePtr> factory, IAllocator<ITuplePtr> hdrTupFactory)
      throws ExecException
  {
    super(ec, ExecStoreType.PARTNWINDOW);

    // this is the data tuple factory as opposed to the header tuple
    // factory and this is maintained by the super class ExecStore.
    this.factory = factory;

    this.hdrTupFac = hdrTupFactory;

    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    
    ttupleListFac = factoryMgr.get(FactoryManager.PARTITION_FACTORY_ID);
    
    iFactory = factoryMgr.get(FactoryManager.TDOUBLY_LIST_ITER_FACTORY_ID);

    i2Factory = factoryMgr.get(FactoryManager.PARTITION_ITER_FACTORY_ID);
    ttupleNodeFac = factoryMgr.get(FactoryManager.PARTITION_NODE_FACTORY_ID);
    ttuples = ttupleListFac.allocate();
    ttuples.setTupleFactory(factory);
    ttuples.setFactory(ttupleNodeFac);
    
    activeStubs = new BitSet();
    stubs = new ExpandableArray<TupleIterator>(Constants.INTIAL_NUM_STUBS);
    numStubs = 0;
    oldest = null;
    oldestItr = null;
    oldestScratch = new TimedTuple();
    isRecovered = false;
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
   * Initialize the partition window store
   * 
   * @param factory
   *          tuple factory for data tuples
   * @param hdrTupFactory
   *          tuple factory for header tuples
   */
  public void initialize(IAllocator<ITuplePtr> factory, IAllocator<ITuplePtr> hdrTupFactory)
  {
    assert ttuples != null;
    assert ttuples.isEmpty() == true;

    assert activeStubs != null;
    assert activeStubs.nextSetBit(0) == -1;

    this.factory = factory;
    this.hdrTupFac = hdrTupFactory;
    
    assert stubs != null;
    for (int i = 0; i < Constants.INTIAL_NUM_STUBS; i++)
      stubs.set(i, null);

    colIns = null;
  }


  /**
   * Inserts the specified tuple into the store.
   * 
   * @param tuple
   *          Tuple to be inserted
   * @param stubId
   *          Synopsis id
   * 
   * @throws ExecException
   *           for any underlying errors
   */
  public void insertTuple_p(ITuplePtr tuplePtr, long ts, int stubId) 
                                                          throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_INSERT, this, 
                  "insert_p", tuplePtr, ts, stubId);

    ITuple tuple = tuplePtr.pinTuple(IPinnable.WRITE);
    ITupleDoublyListNode newNode;
    TupleIterator hdrScan;
    ITuplePtr hdrtuple = null;

    // only a primary stub (belonging to that of a partition window operator)
    // can insert into a partition. Therefore, there is no need of any
    // synchronization
    assert stubId == primaryStub;

   // bind the input tuple in the INPUT ROLE
    evalCtx.bind(tuplePtr, IEvalContext.INPUT_ROLE);
    
    // find a partition with the given key spec
    hdrScan = headerTupIndex.getScan();

    // Primary Stub information is not stamped in the ttuples. Only the
    // secondary stubs set the stub information in the ttuples.

    IPartition tupList;

    // yes there is a matching header in the synopsis.
    if ((hdrtuple = hdrScan.getNext()) == null)
    {
      tupList = createPartitionList();
    } 
    else 
    {
      ITuple hdrTuple = hdrtuple.pinTuple(IPinnable.WRITE);
      tupList = hdrTuple.oValueGet(hdrWindowListPos);
      hdrTupFac.release(hdrtuple);
    }
    assert tupList != null;
    tupList.add(tuplePtr, ts);
    
    headerTupIndex.releaseScan(hdrScan);

    // Now insert the tuple in the global list.
    synchronized(ttuples) 
    {
      newNode = ttuples.add(tuplePtr, ts);
      tuple.oValueSet(colRef.getColnum(), newNode.getHandle(ttuples));
      newNode.unpin();

      // Now stamp the stub id.
      // As we have alread added in the global list, we cannot use insertTuple_r
      byte[] stubBits = tuple.bValueGet(colIns.getColnum());
      // assert that the stub info is not already in the tuple
      assert !BitVectorUtil.checkBit(stubBits, stubId);
  
      byte[] stubBits1 = BitVectorUtil.setBit(stubBits, stubId);
      if (stubBits != stubBits1)
        tuple.bValueSet(colIns.getColnum(), stubBits1, stubBits1.length);
    }

    tuplePtr.unpinTuple();

    factory.addRef(tuplePtr);
  }
  
  private IPartition createPartitionList() throws ExecException
  {
    IPartition tupList;
    
    // this is the first tuple with the partitioning key
    ITuplePtr hdrtuple = (ITuplePtr)hdrTupFac.allocate();

    // header tuple to be input into the has table
    evalCtx.bind(hdrtuple, IEvalContext.SYN_ROLE);
    hdrCopyEval.eval(evalCtx);
        
    // initialize values for the header tuple
    tupList = ttupleListFac.allocate();
    tupList.setTupleFactory(factory);
    tupList.setFactory(ttupleNodeFac);
    
    ITuple hdrTuple = hdrtuple.pinTuple(IPinnable.WRITE);
    hdrTuple.oValueSet(hdrWindowListPos, (Object) tupList);
    
    // insert the header tuple in the hash index
    headerTupIndex.insertTuple(hdrtuple);
    hdrtuple.unpinTuple();
    return tupList;
  }
  
   /**
   * Deletes the oldest tuple in the store.
   * 
   * @param partnSpec
   *          Partition spec
   * @param stubId
   *          Synopsis id
   * 
   * @return Reference to the deleted tuple
   * 
   * @throws ExecException
   *           for any underlying errors
   */
  public ITuplePtr deleteOldestTuple_p(
                            ITuplePtr partnSpecRef, int stubId)
      throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_DELETE, this, 
                 "delete_p", partnSpecRef, stubId);

    ITuplePtr hdrtuple = getPartitionHdr(partnSpecRef, stubId);
    if (hdrtuple == null) 
    {
      System.out.println("getPartitionHdr = null " + partnSpecRef.toString() );
      return null;
    }
    ITuple hdrTuple = hdrtuple.pinTuple(IPinnable.WRITE);
    // find the oldest tuple in this partition using the object column
    // that stores a reference to the oldest tuple.
    IPartition partition = hdrTuple.oValueGet(hdrWindowListPos); 
    hdrtuple.unpinTuple();
    
    ITuplePtr oldestTup = partition.getFirst();
    if (oldestTup == null) 
    {
      System.out.println("partition.getFirst = null");
    }
    IPartitionNode n = (IPartitionNode) partition.getHead(); 
    long oldestTime = n.getTs();
    boolean needAdvanceOldest = false;
    if (stubId == primaryStub && oldest != null)
    {
      // check if we are consuming oldest
      if (oldestTup.getId() == oldest.tuple.getId() && 
          oldestTime == oldest.timeStamp)
        needAdvanceOldest = true;
    }
    n.unpin();
    partition.removeFirst();
    if (partition.getSize() == 0) 
    {
        releaseHdrTuple(hdrtuple, true);    
    }
    hdrTupFac.release(hdrtuple);
    
    // It is important to advance oldest before the tuple is removed
    // from the global list because we still need to access the current node
    if (needAdvanceOldest)
      advanceOldest();

    deleteTuple_r(oldestTup, primaryStub);
    return oldestTup;
  }

  /**
   * Replaces the oldest tuple in the partition with provided tuple.
   * 
   * @param tuplePtr
   *          Tuple to be inserted in the partition 
   * @param ts
   *          timestamp associated with the tuple
   * @param stubId
   *          Synopsis id -- has to be the primary stub
   * 
   * @return Reference to the deleted tuple
   * 
   * @throws ExecException
   *           for any underlying errors
   */
  public ITuplePtr replaceOldestTuple_p(ITuplePtr tuplePtr, long ts, 
                                        int stubId)
      throws ExecException
  {
    // Assert that this method can only be called by the primary stub
    assert stubId == primaryStub;

    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_DELETE, this, 
                 "replace_p", tuplePtr, stubId);
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_INSERT, this, 
                  "replace_p", tuplePtr, ts, stubId);

    ITuplePtr hdrTuplePtr = getPartitionHdr(tuplePtr, stubId);
    ITuple    hdrTuple    = hdrTuplePtr.pinTuple(IPinnable.READ);

    // Get the list for this partition from the object column 
    // in the header tuple
    IPartition partition = hdrTuple.oValueGet(hdrWindowListPos); 
    hdrTuplePtr.unpinTuple();
    hdrTupFac.release(hdrTuplePtr);

    ITuplePtr oldestTup = partition.getFirst();

    // The assumption here is that there exists at least one tuple
    // in the partition. Caller needs to ensure this.
    assert (oldestTup != null);

    // Determine if we need to advance the oldest
    IPartitionNode n = (IPartitionNode) partition.getHead(); 
    long oldestTime = n.getTs();
    boolean needAdvanceOldest = false;
    if (stubId == primaryStub && oldest != null)
    {
      // check if we are consuming oldest
      if (oldestTup.getId() == oldest.tuple.getId() && 
          oldestTime == oldest.timeStamp)
        needAdvanceOldest = true;
    }
    n.unpin();

    // Delete from the partition list
    partition.removeFirst();

    // It is important to advance oldest before the tuple is removed
    // from the global list because we still need to access the current node
    if (needAdvanceOldest)
      advanceOldest();

    // Delete from the main relation list
    deleteTuple_r(oldestTup, primaryStub);

    // Now, insert the replacement tuple
    ITuple tuple = tuplePtr.pinTuple(IPinnable.WRITE);
    ITupleDoublyListNode newNode;

    // Insert into the partition list
    partition.add(tuplePtr, ts);

    // Now insert the tuple in the global list.
    synchronized(ttuples) 
    {
      newNode = ttuples.add(tuplePtr, ts);
      tuple.oValueSet(colRef.getColnum(), newNode.getHandle(ttuples));
      newNode.unpin();

      // Now stamp the stub id.
      // As we have alread added in the global list, 
      // we cannot use insertTuple_r

      byte[] stubBits = tuple.bValueGet(colIns.getColnum());
      // assert that the stub info is not already in the tuple
      assert !BitVectorUtil.checkBit(stubBits, stubId);
  
      byte[] stubBits1 = BitVectorUtil.setBit(stubBits, stubId);
      if (stubBits != stubBits1)
        tuple.bValueSet(colIns.getColnum(), stubBits1, stubBits1.length);
    }

    tuplePtr.unpinTuple();
    factory.addRef(tuplePtr);

    return oldestTup;
  }

  /**
   * Gets the oldest tuple in the partition corresponding to the partition spec.
   * 
   * @param partnSpec
   *          Partition spec
   * @param stubId
   *          Synopsis id
   * 
   * @return Reference to the deleted tuple
   * 
   * @throws ExecException
   *           for any underlying errors
   */
  public ITuplePtr getOldestTuple_p(ITuplePtr partnSpecRef, int stubId)
      throws ExecException
  {
    ITuplePtr hdrtuple = getPartitionHdr(partnSpecRef, stubId);
    if (hdrtuple == null) return null;
    ITuple hdrTuple = hdrtuple.pinTuple(IPinnable.READ);
    // find the oldest tuple in this partition using the object column
    // that stores a reference to the oldest tuple.
    IPartition partition = hdrTuple.oValueGet(hdrWindowListPos);
    hdrtuple.unpinTuple();
    
    ITuplePtr oldestTup = partition.getFirst();
    factory.addRef(oldestTup);

    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_GET, this, 
            "getOldestTuple_p", partnSpecRef, stubId, oldestTup);
    return oldestTup;
  }

  /**
   * Checks if the specified store is empty
   * @param stubId Stub that identifies the synopsis
   * @return true if empty or false
   */
  public boolean isEmpty_w(int stubId) throws ExecException
  {
    assert(stubId == primaryStub);

    return (ttuples.getSize() == 0);
  }

  private void advanceOldest() throws ExecException
  {
    synchronized(ttuples)
    {
      if (ttuples.getSize() == 0) 
      {
         oldest = null;
         //this will trigger advaceOldest in getOldest when new tuple is inserted
         oldestItr = null; 
         return;
      }
      if (oldest == null)
      {
        oldestItr.initialize(ttuples);
      }
      else
      {
        // start iterating from current oldest
        // the next position in the iterator may not be valid.
        oldestItr.resetCurrent();
      }
      LogLevelManager lm = execContext.getLogLevelManager();
      ITuplePtr tuplePtr;
      while ((tuplePtr = oldestItr.next()) != null) 
      {
        ITuple tuple = tuplePtr.pinTuple(IPinnable.READ);
        byte[] stubBits = tuple.bValueGet(colIns.getColnum());
        boolean hasStub  = BitVectorUtil.checkBit(stubBits, primaryStub);
  
        tuplePtr.unpinTuple();
        if (hasStub)
        {
          oldest = oldestScratch;
          oldest.tuple = tuplePtr;
          oldest.timeStamp = oldestItr.getTs();
          
          // debug code to make sure that the assumption stands.
          if (ITER_VERIFY)
          {
          IPartition partition = getPartition(tuplePtr, primaryStub);
          IPartitionNode ph = null;
          if (partition != null) ph = partition.getHead();
          if (partition == null || ph == null)
          {
            LogUtil.fine(LoggerType.TRACE, "----tuple-----");
            LogUtil.fine(LoggerType.TRACE, tuplePtr.toString());
            LogUtil.fine(LoggerType.TRACE, "----ttuples-----");
            IDumpContext dumper = lm.openDumper("ttuples", null);
            ttuples.dump(dumper);
            LogUtil.fine(LoggerType.TRACE, "----headerTupIndex-----");
            TupleIterator fullscan = headerTupIndex.getFullScan();
            ITuplePtr hdrTupPtr;
            while ( (hdrTupPtr = fullscan.getNext()) != null)
            {  
              ITuple hdrTup = hdrTupPtr.pinTuple(IPinnable.READ);
              IPartition tupList = hdrTup.oValueGet(hdrWindowListPos); 
              hdrTupPtr.unpinTuple();
              LogUtil.fine(LoggerType.TRACE, hdrTup.toString());
              tupList.dump(dumper);
            }
            headerTupIndex.releaseScan(fullscan);
            lm.closeDumper("ttuples", null, dumper);
          }
          assert (partition != null && ph != null); 
          IPartitionNode n = ph.pin(IPinnable.READ);
          long ts = n.getTs();
          ITuplePtr t = n.getNodeElem();
          assert (oldest.tuple.getId() == t.getId() && oldest.timeStamp == ts);
          ph.unpin();
          }
          
          return;
        }
      }
      oldest = null;
      //this will trigger advaceOldest in getOldest when new tuple is inserted
      oldestItr = null;
    }
  }
  
  /** Returns the oldest timed tuple in the store for the specified synopsis
   * @param stubId - stub that identifies the synopsis
   * @return the oldest timedtuple if any, else null
   */
  public TimedTuple getOldestTimedTuple_w(int stubId)
    throws ExecException
  {
    assert(stubId == primaryStub);
    if (oldestItr == null && oldest == null)
    {
      // first iteration.
      // oldestItr is set to null initially 
      // in order to avoid unnnecessary advancing of oldest 
      // on insertion (e.g without range window, we don't need to do it)
      oldestItr = i2Factory.allocate();
      advanceOldest();
    }
    if (oldest != null) 
    {
      factory.addRef(oldest.tuple);
    }
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_GET, this, "getOldestTimedTuple_w", stubId, oldest);
    return oldest;
  }

  /**
   * Remove the primary stub - the partition window store and move any residual
   * tuples to expired list.
   * 
   */
  private void removePrimaryStub() throws ExecException
  {
    IPartitionIter  iter = i2Factory.allocate();
    TupleIterator fullscan = headerTupIndex.getFullScan();
    ITuplePtr hdrTupPtr;
    while ( (hdrTupPtr = fullscan.getNext()) != null)
    {  
      ITuple hdrTup = hdrTupPtr.pinTuple(IPinnable.WRITE);
      IPartition tupList = hdrTup.oValueGet(hdrWindowListPos); 
      hdrTupPtr.unpinTuple();
      
      iter.initialize(tupList);
      ITuplePtr tuplePtr;
      while ((tuplePtr = iter.next()) != null) 
      {
        deleteTuple_r(tuplePtr, primaryStub);
      }
      tupList.clear();

      iter.release(tupList);
      releaseHdrTuple(hdrTupPtr, false);
    }
    headerTupIndex.releaseScan(fullscan);
    headerTupIndex.clear();
    i2Factory.release(iter);
    oldest = null;
  }

  /**
   * Delete a reader from this store.
   * 
   * @param stubId
   *          The stubId
   * @throws ExecException
   *           when attempted by an invalid reader
   */
  public void removeStub(int stubId) throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_DDL, this, "removeStub", stubId);
    
    // It should be a valid reader
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);

    if (stubId == primaryStub)
      removePrimaryStub();
    else
    {
      while (true)
      {
        // delete all the elements for this reader. Since we will be
        // disturbing the very iterator that we are scanning, we will
        // have to restart it after deleting each tuple.
        TupleIterator iter = getScan_r(stubId);
        ITuplePtr tuplePtr = iter.getNext();
    
        if (tuplePtr == null)
        {
          releaseScan_r(iter, stubId);
          break; // ran out of rows with this stub id.
        }
        deleteTuple_r(tuplePtr, stubId);
        releaseScan_r(iter, stubId);
      }
    }

    stubs.set(stubId, null);
    activeStubs.clear(stubId);
    numStubs--;
  }

  /**
   * Add a new reader for this store.
   * 
   * @return The stubId
   * 
   * @throws ExecException
   */
  public int addStub() throws ExecException
  {
    int stubNo = activeStubs.nextClearBit(0);
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_DDL, this, "addStub", stubNo);

    activeStubs.set(stubNo);
    // all the secondary stubs treat the partition window store
    // as a relstore which has all the tuples in a doubly linked
    // list. So, we will be reusing the RelStoreImplIter.
    RelStoreImplIter iter = new RelStoreImplIter(stubNo, colIns, factory);

    assert stubs.get(stubNo) == null;
    stubs.set(stubNo, iter);
    numStubs++;
    return stubNo;
  }

  public int getNumElems()
  {
    synchronized(ttuples)
    {
      return ttuples.getSize();
    }
  }
  
   /**
   * Insert a tuple into the synopsis stubId. This method is used by an operator
   * other than the partition window operator which is configured to share the
   * partition window store. The tuple being inserted is more likely in one of
   * the partitions. This method find the tuple in a partition and stamps the
   * tuple with the passed in stub information.
   * 
   * @param tuple
   *          tuple to be inserted into the store
   * 
   * @param stubId
   *          stubId for the operator using this stor
   * 
   * @throws ExecException
   *           if unable to insert the tuple into the store.
   */
  public void insertTuple_r(ITuplePtr tuplePtr, int stubId) throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_INSERT, this, 
                  "insert_r", tuplePtr, stubId);

    // partition window operator is not attempting this operation.
    // However insertTuple_p invoke this to stamp the stub Id.

    // should be a valid stub
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);

    ITuple tuple = tuplePtr.pinTuple(IPinnable.WRITE);
    ITupleDoublyListNode newNode;

    synchronized(ttuples) 
    {
      byte[] stubBits = tuple.bValueGet(colIns.getColnum());
      // This tuple may already be shared by the partition window operator as
      // well as one or more secondary operators. Although there is no stub
      // information pertaining to the partition window opeartor, the
      // secondary operators set their stub information.
      // Assumption
      // Even though the tuple has already been inserted in the global list by insertTuple_p
      // and the pribaryStub has already been set in the stub stamp, it is possible
      // that the tuple has already been removed by the PartitionWindow operator
      // as it is processing multiple tuples in a given time quota.
      if (BitVectorUtil.isNull(stubBits)) {
          newNode = ttuples.add(tuplePtr, 0);
          tuple.oValueSet(colRef.getColnum(), newNode.getHandle(ttuples));
          newNode.unpin();
      } else 
      {
          assert ttuples.contains(tuplePtr);
      }
      // assert that the stub info is not already in the tuple
      assert !BitVectorUtil.checkBit(stubBits, stubId);
  
      byte[] stubBits1 = BitVectorUtil.setBit(stubBits, stubId);
      if (stubBits != stubBits1)
        tuple.bValueSet(colIns.getColnum(), stubBits1, stubBits1.length);
    }
    tuplePtr.unpinTuple();
    factory.addRef(tuplePtr);
  }

  /**
   * Delete the indicated tuple from the synopsis stubId. This is used by an
   * operator other than the partition window operator which is configured to
   * share the partition window store. The tuple being deleted is likely the one
   * deleted from one of the partitions recently.
   */
  @SuppressWarnings("unchecked")
  public void deleteTuple_r(ITuplePtr tuplePtr, int stubId) throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_DELETE, this, 
                  "delete_r", tuplePtr, stubId);

    // should be a valid stub
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);

    ITuple tuple = tuplePtr.pinTuple(IPinnable.WRITE);
    synchronized (ttuples)
    {
      byte[] stubBits = tuple.bValueGet(colIns.getColnum());
  
      // assert that the tuple has this stub
      assert BitVectorUtil.checkBit(stubBits, stubId);
  
      byte[] stubBits1 = BitVectorUtil.clearBit(stubBits, stubId);
      if (stubBits != stubBits1)
        tuple.bValueSet(colIns.getColnum(), stubBits1, stubBits1.length);
    
      if (BitVectorUtil.isNull(stubBits))
      {
        // if bVal is NULL, this is the last secondary opeartor needing
        // this tuple. This tuple may or may not be in the partition
        // windows but it can be removed from the secondary tuple list.
        //boolean b = ttuples.remove(tuplePtr);
        //assert b;
        ttuples.remove((IListNodeHandle)tuple.oValueGet(colRef.getColnum()));
      }
    }
    tuplePtr.unpinTuple();
    
    factory.release(tuplePtr);
  }

  /**
   * Scan the *entire* contents of stubId
   * 
   * @return The tuple iterator to scan the ttuples.
   */
  public TupleIterator getScan_r(int stubId) throws ExecException
  {
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);

    RelStoreImplIter rIter = (RelStoreImplIter)stubs.get(stubId); 
    rIter.initialize(this, ttuples, iFactory); 
 
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_SCAN_START, this, 
                  "getScan_r", stubId, rIter);
    return (TupleIterator)rIter;
  }

  /**
   * Release a scan that you previously got
   */
  public void releaseScan_r(TupleIterator iter, int stubId)
      throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_SCAN_STOP, this, 
                  "releaseScan_r", iter, stubId);
    // For non primary stub 
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);
    
    RelStoreImplIter rIter = (RelStoreImplIter)stubs.get(stubId); 
    rIter.release(iFactory);
  }
  
  public synchronized void dump(IDumpContext dumper) 
  {
    if (!dumper.isVerbose())
    {
      String tag = LogUtil.beginDumpObj(dumper, this);
      ttuples.dump(dumper);
      headerTupIndex.dump(dumper);
      LogUtil.endDumpObj(dumper, tag);
      return;
    }
    String dumperKey = StringUtil.getBaseClassName(this);
    IDumpContext w = dumper.openDumper(dumperKey);
    String tag = LogUtil.beginDumpObj(w, this);
    ttuples.dump(w);
    headerTupIndex.dump(w);
    LogUtil.endDumpObj(w, tag);
    w.closeDumper(dumperKey, dumper);
  }
  
  public boolean evict()
    throws ExecException
  {
    // evicts list first
    boolean b = headerTupIndex.evict();
    b |= ttuples.evict();
    b|= super.evict();
    return b;
  }

   /**
   * Deletes the specified tuple in the store.
   * 
   * @param tuple
   *          parameter tuple
   * @param ts
   *          tuple timestamp
   * @param stubId
   *          Synopsis id
   * 
   * @return Reference to the deleted tuple
   * 
   * @throws ExecException
   *           for any underlying errors
   */
  @Override
  public void deleteTuple_p(ITuplePtr tuple, long ts, int stubId)
      throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_DELETE, this, 
        "delete_p", tuple, stubId);

    ITuplePtr hdrtuple = getPartitionHdr(tuple, stubId);
    if (hdrtuple == null) 
    {
      System.out.println("getPartitionHdr = null " + tuple.toString() );
      return;
    }
    ITuple hdrTuple = hdrtuple.pinTuple(IPinnable.WRITE);
    // find the oldest tuple in this partition using the object column
    // that stores a reference to the oldest tuple.
    IPartition partition = hdrTuple.oValueGet(hdrWindowListPos); 
    hdrtuple.unpinTuple();

    boolean needAdvanceOldest = false;
    if (stubId == primaryStub && oldest != null)
    {
      // check if we are consuming oldest
      if (tuple.getId() == oldest.tuple.getId() && 
          ts == oldest.timeStamp)
        needAdvanceOldest = true;
    }

    boolean isFoundAndDeleted = partition.remove(tuple);
    assert isFoundAndDeleted;
    
    if (partition.getSize() == 0) 
    {
      releaseHdrTuple(hdrtuple, true);    
    }
    hdrTupFac.release(hdrtuple);

    // It is important to advance oldest before the tuple is removed
    // from the global list because we still need to access the current node
    if (needAdvanceOldest)
      advanceOldest();

    deleteTuple_r(tuple, primaryStub);
    
  }
  
  /**
   * Gets the window size corresponding to the partition spec.
   * Window size is maintained according to the incoming and 
   * expiring tuples
   * 
   * @param partnSpec     Partition spec
   * @param synId         Synopsis id
   * 
   * @return Size based on the number of tuples arrived and expired
   * in the partition window
   *
   * @throws  ExecException for any underlying errors
   */
  public int getPartnWindowSize(ITuplePtr partnSpec, int stubId) 
                                                throws ExecException
  {
    //partnSpec==null means this tuple is from a heartbeat
    if (partnSpec == null) return 0;
    IPartition partition = getPartition(partnSpec, stubId);
    if (partition == null) return 0;
    int sz = partition.getWindowSize();
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_GET, this, 
            "getPartnWindowSize", partnSpec, stubId, sz);
    return sz;
  }
  
  public void incrementWindowSize(ITuplePtr partnSpecPtr, int stubId) 
      throws ExecException
  {
    if (partnSpecPtr != null)
    {
      IPartition partition = getPartition(partnSpecPtr, stubId);
      if (partition == null)
        partition = createPartitionList();
      assert partition != null;
      partition.incrementWindowSize();
    }
  }
  
  public void decrementWindowSize(ITuplePtr partnSpecPtr, int stubId) 
      throws ExecException
  {
    if (partnSpecPtr != null)
    {
      IPartition partition = getPartition(partnSpecPtr, stubId);
      if (partition != null)
        partition.decrementWindowSize();
    }
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
}
