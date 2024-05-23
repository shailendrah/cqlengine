/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/stores/PrivatePartnWindowStoreImpl.java /main/4 2011/02/07 03:36:26 sborah Exp $ */

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
    anasrini    12/19/10 - replace eval() with eval(ec)
    udeshmuk    11/13/08 - refactoring into superclass
    udeshmuk    11/05/08 - rename to private instead of pattern.
    hopark      10/19/08 - pass ExecContext
    udeshmuk    10/07/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/stores/PrivatePartnWindowStoreImpl.java st_pcbpel_anasrini_eval_parallelism_2/1 2010/12/19 07:35:40 anasrini Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.stores;

import oracle.cep.execution.ExecException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.indexes.HashIndex;
import oracle.cep.execution.indexes.Index;

import java.util.ArrayList;
import java.util.List;

import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IPinnable;

import oracle.cep.dataStructures.internal.ITupleDoublyListNode;
import oracle.cep.dataStructures.internal.IListNodeHandle;
import oracle.cep.dataStructures.internal.IPartition;
import oracle.cep.dataStructures.internal.IPartitionIter;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.service.ExecContext;
import oracle.cep.util.StringUtil;

/**
 * Store used for partition window synopsis of pattern operator. This store
 * maintains a table of partition keys (header tuples) with a hash index on 
 * them. Each header tuple has a pointer to the list of tuples as well as the
 * count of rows in the partition. The 'pointers' are managed  using the Object
 * columns in the tuples. 
 * The global list is maintained only if prev with range exists.
 * @author udeshmuk
 */
@DumpDesc(attribTags={"Id", "PhyId"}, 
          attribVals={"getId", "getPhyId"},
          infoLevel=LogLevel.STORE_INFO,
          evPinLevel=LogLevel.STORE_TUPLE_PINNED,
          evUnpinLevel=LogLevel.STORE_TUPLE_UNPINNED,
          dumpLevel=LogLevel.STORE_DUMP,
          verboseDumpLevel=LogLevel.STORE_DUMPELEMS)
public class PrivatePartnWindowStoreImpl extends BasePartnWindowStoreImpl 
  implements PrivatePartnWindowStore
{
  
  /** true if prev has range */
  private boolean                         supportRangeFunctionality=false;
  
  /** duration after which tuples will be deleted by expireTuples */
  private long                            timeRange=0;
  
  /** number of tuples in the store.
   *  used to determine size when ttuples list is not maintained 
   */
  private int                             numTuples=0;
  
  /**
   * Constructor Unlike other stores, partition window store has two tuple
   * factories, one for the partition keys and the other for the data ttuples.
   * @param ec TODO
   * @param factory
   *          TupleFactory
   * @param hdrTupFac
   *          TupleFactory for header tuples.
   * 
   * @throws ExecException
   */
  public PrivatePartnWindowStoreImpl(ExecContext ec, 
                              IAllocator<ITuplePtr> factory, IAllocator<ITuplePtr> hdrTupFactory)
      throws ExecException
  {
    super(ec, ExecStoreType.PARTNWINDOW);

    // this is the data tuple factory as opposed to the header tuple
    // factory and this is maintained by the super class ExecStore.
    this.factory = factory;

    this.hdrTupFac = hdrTupFactory;

    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    ttupleNodeFac = factoryMgr.get(FactoryManager.PARTITION_NODE_FACTORY_ID);
    ttupleListFac = factoryMgr.get(FactoryManager.PARTITION_FACTORY_ID);
    i2Factory = factoryMgr.get(FactoryManager.PARTITION_ITER_FACTORY_ID);
    ttuples = ttupleListFac.allocate();
    ttuples.setTupleFactory(factory);
    ttuples.setFactory(ttupleNodeFac);
    oldestItr = null;  
    numTuples = 0;
    oldestItr = i2Factory.allocate();
  }

  /**
   * Setter for support range functionality
   * @param supportRange true if range functionality is to be supported
   *        Global list needs to be maintained in that case
   */
  public void setSupportRangeFunctionality(boolean supportRange)
  {
    this.supportRangeFunctionality = supportRange;  
  }
  
  /**
   * Setter for timeRange
   * @param range range duration used in expireTuples
   */
  public void setTimeRange(long range)
  {
    this.timeRange = range;
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
    this.factory = factory;
    this.hdrTupFac = hdrTupFactory;
    numTuples = 0;
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
  public void insertTuple_p(ITuplePtr tuplePtr, long ts, int stubId) throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_INSERT, this, 
                  "insert_p", tuplePtr, ts, stubId);

    ITuple tuple = tuplePtr.pinTuple(IPinnable.WRITE);
    ITupleDoublyListNode newNode;
    TupleIterator hdrScan;
    ITuplePtr hdrtuple = null;

    // only a primary stub (belonging to that of a pattern operator)
    // can insert into a partition. Therefore, there is no need of any
    // synchronization
    assert stubId == primaryStub;

   // bind the input tuple in the INPUT ROLE
    evalCtx.bind(tuplePtr, IEvalContext.INPUT_ROLE);
    
    // find a partition with the given key spec
    hdrScan = headerTupIndex.getScan();

    IPartition tupList;

    if ((hdrtuple = hdrScan.getNext()) == null)
    {
      // this is the first tuple with the partitioning key
      hdrtuple = (ITuplePtr)hdrTupFac.allocate();

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
    } 
    else 
    {
      ITuple hdrTuple = hdrtuple.pinTuple(IPinnable.WRITE);
      tupList = hdrTuple.oValueGet(hdrWindowListPos);
      hdrTupFac.release(hdrtuple);
    }
    tupList.add(tuplePtr, ts);
    hdrtuple.unpinTuple();
    
    headerTupIndex.releaseScan(hdrScan);

    // Now insert the tuple in the global list.
    if(supportRangeFunctionality)
    {
      newNode = ttuples.add(tuplePtr, ts);
      tuple.oValueSet(colRef.getColnum(), newNode.getHandle(ttuples));
      newNode.unpin();
    }
    else 
      numTuples++;
    tuplePtr.unpinTuple();
    factory.addRef(tuplePtr);
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
    //remove from the partition
    partition.removeFirst();

    if(partition.getSize() == 0) 
    {
      releaseHdrTuple(hdrtuple, true);    
    }
    hdrTupFac.release(hdrtuple);
    
    //numTuples will be updated in deleteTuple_r    
    deleteTuple_r(oldestTup, stubId);
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

    // Delete from the partition list
    partition.removeFirst();

    // Delete from the global list
    deleteTuple_r(oldestTup, stubId);

    // Now, insert the replacement tuple
    ITuple tuple = tuplePtr.pinTuple(IPinnable.WRITE);
    ITupleDoublyListNode newNode;

    // Insert into the partition list
    partition.add(tuplePtr, ts);

    // Now insert the tuple in the global list.
    if(supportRangeFunctionality)
    {
      newNode = ttuples.add(tuplePtr, ts);
      tuple.oValueSet(colRef.getColnum(), newNode.getHandle(ttuples));
      newNode.unpin();
    }
    else
      numTuples++;
 
    tuplePtr.unpinTuple();
    factory.addRef(tuplePtr);

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
    if(supportRangeFunctionality)
      return (ttuples.getSize() == 0);
    else 
      return (numTuples == 0);
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

    if (stubId == primaryStub)
      removePrimaryStub();
    else 
      throw new ExecException(ExecutionError.INVALID_STUBID,stubId);
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
    return 0; //there will be only one reader for this store. That is the pattern operator.
  }

  public int getNumElems()
  {
    if(supportRangeFunctionality)
      return ttuples.getSize();
    else 
      return numTuples;
  }

  /**
   * Delete the indicated tuple from the global list of tuples (ttuples) if it 
   * is maintained. Decrement the numTuples otherwise. 
   */
  @SuppressWarnings("unchecked")
  public void deleteTuple_r(ITuplePtr tuplePtr, int stubId) throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_DELETE, this, 
                  "delete_r", tuplePtr, stubId);
    if(supportRangeFunctionality)
    {
      ITuple tuple = tuplePtr.pinTuple(IPinnable.READ);

      ttuples.remove((IListNodeHandle)tuple.oValueGet(colRef.getColnum()));
      tuplePtr.unpinTuple();
    } 
    else 
      numTuples--;
    factory.release(tuplePtr);
  }
  
  public void expireTuples(long ts) throws ExecException
  {
    assert supportRangeFunctionality == true;
   
    oldestItr.initialize(ttuples);
    ITuplePtr tuplePtr;
    long oldestTs;
    while((tuplePtr=oldestItr.next()) != null)
    {
      oldestTs = oldestItr.getTs();
      if((oldestTs != -1) && (oldestTs + timeRange < ts))
        deleteOldestTuple_p(tuplePtr, primaryStub);
      else 
        break;
    }
  }
  
  public synchronized void dump(IDumpContext dumper) 
  {
    if (!dumper.isVerbose())
    {
      String tag = LogUtil.beginDumpObj(dumper, this);
      if(supportRangeFunctionality)ttuples.dump(dumper);
      headerTupIndex.dump(dumper);
      LogUtil.endDumpObj(dumper, tag);
      return;
    }
    String dumperKey = StringUtil.getBaseClassName(this);
    IDumpContext w = dumper.openDumper(dumperKey);
    String tag = LogUtil.beginDumpObj(w, this);
    if(supportRangeFunctionality)
      ttuples.dump(w);
    headerTupIndex.dump(w);
    LogUtil.endDumpObj(w, tag);
    dumper.closeDumper(dumperKey, w);
  }
  
  public boolean evict()
    throws ExecException
  {
    // evicts list first
    boolean b = headerTupIndex.evict();
    if(supportRangeFunctionality)
      b |= ttuples.evict();
    b|= super.evict();
    return b;
  }
}

