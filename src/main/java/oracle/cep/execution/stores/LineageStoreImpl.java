/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/stores/LineageStoreImpl.java /main/36 2013/10/08 10:15:01 udeshmuk Exp $ */
/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sborah      12/17/08 - handle constants
 hopark      12/02/08 - move LogLevelManager to ExecContext
 hopark      10/10/08 - remove statics
 hopark      06/19/08 - logging refactor
 hopark      03/03/08 - set TupleFactory for list
 hopark      02/25/08 - fix tuple eviction
 hopark      02/05/08 - parameterized error
 hopark      12/26/07 - support xmllog
 hopark      12/13/07 - fix synchronization
 hopark      12/07/07 - cleanup spill
 hopark      11/28/07 - fix NPE in dumpElems
 hopark      11/06/07 - change getHandle api
 hopark      12/19/07 - fix NPE in dump
 parujain    12/17/07 - db-join
 hopark      10/15/07 - add evict
 hopark      09/21/07 - use ListNodeHandle
 hopark      09/19/07 - stubBits optimization
 sbishnoi    09/04/07 - add RefCol
 najain      05/24/07 - add getNumElems
 hopark      06/20/07 - cleanup
 hopark      06/15/07 - add getIndexes
 hopark      06/07/07 - use LogArea
 hopark      05/23/07 - debuglogging
 hopark      05/16/07 - remove printStackTrace
 najain      05/11/07 - variable length support
 hopark      05/08/07 - ITuple api cleanup
 hopark      04/20/07 - change pinTuple semantics
 najain      04/11/07 - bug fix
 hopark      04/09/07 - fix pincount
 hopark      04/09/07 - fix pincount
 hopark      04/05/07 - memmgr reorg
 najain      03/14/07 - cleanup
 najain      03/12/07 - bug fix
 najain      02/05/07 - coverage
 hopark      01/12/07 - uses long for tuple id
 najain      01/04/07 - spill over support
 najain      01/24/07 - bug fix
 najain      12/04/06 - stores are not storage allocators
 parujain    11/30/06 - Use DoublyListIter Factory
 najain      11/08/06 - IDoublyList is a StorageElement
 najain      08/16/06 - concurrency issues
 najain      08/02/06 - refCount optimizations
 parujain    08/01/06 - Generic store list
 dlenkov     07/24/06 - fixed insertTuple & getTuple
 najain      07/18/06 - ref-count tuples 
 najain      07/10/06 - add getFactory 
 najain      06/16/06 - bug fix 
 najain      06/13/06 - bug fix 
 anasrini    04/10/06 - implement RelStore interface 
 anasrini    03/27/06 - temporarily implement StorageAlloc 
 anasrini    03/22/06 - use Constants.BITS_PER_BYTE 
 najain      03/22/06 - add colIns and colDel
 anasrini    03/21/06 - add setNumStubs 
 najain      03/14/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/stores/LineageStoreImpl.java /main/36 2013/10/08 10:15:01 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.stores;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import java.util.logging.Level;

import oracle.cep.dataStructures.internal.ExpandableArray;
import oracle.cep.dataStructures.internal.ITupleDoublyList;
import oracle.cep.dataStructures.internal.ITupleDoublyListNode;
import oracle.cep.dataStructures.internal.IListNodeHandle;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.indexes.Index;
import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ITupleDoublyListIter;
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
 * LineageStoreImpl
 *
 * @author najain
 */
@DumpDesc(attribTags={"Id", "PhyId"}, 
          attribVals={"getId", "getPhyId"},
          infoLevel=LogLevel.STORE_INFO,
          evPinLevel=LogLevel.STORE_TUPLE_PINNED,
          evUnpinLevel=LogLevel.STORE_TUPLE_UNPINNED,
          dumpLevel=LogLevel.STORE_DUMP,
          verboseDumpLevel=LogLevel.STORE_DUMPELEMS)
public class LineageStoreImpl extends ExecStore 
    implements LineageStore, RelStore, LineageStoreInternal
{
  private static final String TAG_LINEAGESTORE_LIST = "ListStore";
  private static final String TAG_LINEAGESTORE_INDEX = "Index";
  
  /** Storage manager for the list */
  private IAllocator<ITupleDoublyList>     lFactory;

  /** Currently, the elements are stored in a simple linked list */
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

  // TODO::: although there are multiple stubs, currently they are not shared.
  // One tuple can only belong to one stub -- need to fix that later, and also
  // revisit this whole LineageStore

  /** The index used for scanning the tuple given a lineage */
  private Index                           index;

  /**
   * This buffer is shared between the EvalContext of the Index and the store.
   * The instantiation of the linear store binds this tupleBuf to the
   * evalContext and also sets it in the store. Since there is only one tupleBuf
   * right now, it will lead to a problem if there are multiple threads
   * processing this operator at the same time. This needs to be addressed in
   * future.
   */
  private ITuplePtr  tupleBuf;

  /** number of lineages stored in the tuple */
  private int                             numLins;

  /**
   * Currently, we assume that the number of lineages are fixed, and cannot be
   * changed dynamically. The tuple contains those many entries as the number of
   * lineages - one for each lineage.
   */

  /**
   * colIns and colDel are used to store the column numbers where the insert and
   * delete bitset for a tuple is stored.
   */
  private Column                          colIns;

  private ArrayList<Column>               colLineage;
  
  /** colRef are used to keep a reference to DoubleList inside tuple  */
  private Column                          colRef;

  private IAllocator<ITupleDoublyListNode> nFactory;

  /** Storage manager for doubly list iterator */
  private IAllocator<ITupleDoublyListIter> iFactory;

  /** Number of active stubs associated with this store */
  private int numStubs;
  
  /** A flag to check if the tuples of store is recovered after loading data from snapshot  */
  private boolean isRecovered;
  
  /**
   * @param colIns
   *          The colIns to set.
   */
  public void setColIns(Column colIns)
  {
    this.colIns = colIns;
  }

  /**
   * @param colLineage
   *          The colLineage to set.
   */
  public void setColLineage(ArrayList<Column> colLineage)
  {
    this.colLineage = colLineage;
  }
  
  public ArrayList<Column> getColLineage()
  {
    return this.colLineage;
  }

  /**
   * @param colRef
   *          The colRef to set.
   */
  public void setColRef(Column colRef)
  {
    this.colRef = colRef;
  }
  
  /**
   * @param index
   *          The index to set.
   */
  public void setIndex(Index index)
  {
    this.index = index;
  }

  /**
   * @return Returns the tupleBuf.
   */
  public ITuplePtr getTupleBuf()
  {
    return tupleBuf;
  }

  /**
   * @param tupleBuf
   *          The tupleBuf to set.
   */
  public void setTupleBuf(ITuplePtr tupleBuf)
  {
    this.tupleBuf = tupleBuf;
  }

  /**
   * @param numLins
   *          The numLins to set.
   */
  public void setNumLins(int numLins)
  {
    this.numLins = numLins;
  }
  
  /**
   * @return Return the number of lineage columns   
   */
  public int getNumLins()
  {
    return this.numLins;
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
  
  /**
   * Constructor for RelStoreImpl
   * @param ec TODO
   */
  public LineageStoreImpl(ExecContext ec, IAllocator<ITuplePtr> factory)
  {
    super(ec, ExecStoreType.LINEAGE);
    
    this.factory = factory;
    stubs = new ExpandableArray<TupleIterator>(Constants.INTIAL_NUM_STUBS);
    try
    {
      FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
      lFactory = factoryMgr.get(FactoryManager.TDOUBLY_LIST_FACTORY_ID);
      iFactory = factoryMgr.get(FactoryManager.TDOUBLY_LIST_ITER_FACTORY_ID);
      listStore = lFactory.allocate();
      nFactory = factoryMgr.get(FactoryManager.TDOUBLY_LIST_NODE_FACTORY_ID);
      listStore.setTupleFactory(factory);
      listStore.setFactory(nFactory);
    }
    catch (ExecException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
    }
    activeStubs = new BitSet();
    numStubs=0;
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
    LineageStoreImplIter iter = new LineageStoreImplIter(stubNo, colIns, factory);

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
   * @throws ExecException
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
    synchronized (listStore)
    {
      return listStore.getSize();
    }
  }

  /**
   * Insert a tuple into the lineage synopsis stubId
   * 
   * @param tuple
   *          Tuple to be inserted
   * @param lineage
   *          Tuple lineage
   * @param stubId
   *          Lineage synopsis stubId
   */
  public void insertTuple_l(ITuplePtr tuplePtr, ITuplePtr[] lineage, int stubId)
      throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_INSERT, this, 
                  "insert_l", tuplePtr, lineage, stubId);
    // it should be a valid stub
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);

    ITuple tuple = tuplePtr.pinTuple(IPinnable.WRITE);
    byte[] stubBits;
    
    // temp variable to handle position of newly inserted node
    ITupleDoublyListNode newNode;
    
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
      if (BitVectorUtil.isNull(stubBits))
      {
        newNode = listStore.add(tuplePtr);
        tuple.oValueSet(colRef.getColnum(), newNode.getHandle(listStore));
        newNode.unpin();
      }

      byte[] stubBits1 = BitVectorUtil.setBit(stubBits, stubId);
      if (stubBits != stubBits1)
        tuple.bValueSet(colIns.getColnum(), stubBits1, stubBits1.length);
      
      // Store the lineage and insert into the index
      assert lineage.length == numLins;
      assert colLineage.size() == numLins;

      for (int i = 0; i < numLins; i++)
      {
        ITuple ltuple = lineage[i].pinTuple(IPinnable.READ);
        tuple.lValueSet(colLineage.get(i).getColnum(), ltuple.getId());
        lineage[i].unpinTuple();
      }

      index.insertTuple(tuplePtr);
    }
    tuplePtr.unpinTuple();
    factory.addRef(tuplePtr);
  }


  /**
   * Insert a tuple into the lineage synopsis stubId
   * 
   * @param tuple
   *          Tuple to be inserted
   * @param lineage
   *          Tuple lineage only Ids
   * @param stubId
   *          Lineage synopsis stubId
   */
  public void insertTuple_l(ITuplePtr tuplePtr, long[] lineageTupleIds, int stubId)
      throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_INSERT, this, 
                  "insert_l_ids", tuplePtr, lineageTupleIds, stubId);
    // it should be a valid stub
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);

    ITuple tuple = tuplePtr.pinTuple(IPinnable.WRITE);
    byte[] stubBits;
    
    // temp variable to handle position of newly inserted node
    ITupleDoublyListNode newNode;
    
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

      // The element needs to be inserted in the list because is is retrieved from snapshot
      newNode = listStore.add(tuplePtr);
      tuple.oValueSet(colRef.getColnum(), newNode.getHandle(listStore));
      newNode.unpin();

      byte[] stubBits1 = BitVectorUtil.setBit(stubBits, stubId);
      if (stubBits != stubBits1)
        tuple.bValueSet(colIns.getColnum(), stubBits1, stubBits1.length);
      
      // Store the lineage and insert into the index
      assert lineageTupleIds.length == numLins;
      assert colLineage.size() == numLins;

      for (int i = 0; i < numLins; i++)
        tuple.lValueSet(colLineage.get(i).getColnum(), lineageTupleIds[i]);

      index.insertTuple(tuplePtr);
    }
    tuplePtr.unpinTuple();
    factory.addRef(tuplePtr);
  }
  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.stores.RelStore#deleteTuple_r(oracle.cep.execution.internals.Tuple,
   *      int)
   */
  @SuppressWarnings("unchecked")
  public void deleteTuple_l(ITuplePtr tuplePtr, int stubId) throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_DELETE, this, 
                  "deleteTuple_l", tuplePtr, stubId);
    
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
      assert (activeStubs.get(stubId) == true);
      
      byte[] stubBits1 = BitVectorUtil.clearBit(stubBits, stubId);
      if (stubBits != stubBits1)
        tuple.bValueSet(colIns.getColnum(), stubBits1, stubBits1.length);
      
      // Delete the tuple from the index
      index.deleteTuple(tuplePtr);

      // Does the tuple need to be deleted from the list
      if (BitVectorUtil.isNull(stubBits))
      {
        assert listStore.contains(tuplePtr);
        listStore.remove((IListNodeHandle)tuple.oValueGet(colRef.getColnum()));
      }
    }
    tuplePtr.unpinTuple();
    factory.release(tuplePtr);
  }

 /**
   * Get the Scan with the specified lineage for the synopsis stubId
   * 
   * @param lineage
   *          Tuple lineage
   * @param stubId
   *          Lineage synopsis stubId
   * @return Scan to iterate through all the tuples with given lineage
   */
  public TupleIterator getScan_l(ITuplePtr[] lineage, int stubId)
      throws ExecException 
  {
     TupleIterator scan;
     
     assert lineage != null;
     assert lineage.length == numLins;
     assert colLineage.size() == numLins;
     
     synchronized (listStore)
     {
       ITuple tbuf = tupleBuf.pinTuple(IPinnable.WRITE);
       for (int l = 0; l < numLins; l++)
       {
         ITuple ltuple = lineage[l].pinTuple(IPinnable.READ);
         tbuf.lValueSet(colLineage.get(l).getColnum(), ltuple.getId());
         lineage[l].unpinTuple();
       }
       tupleBuf.unpinTuple();

       // Scan that returns the tuple with the given lineage
       scan = index.getScan();
       return scan;
     }

  }
  
  public void releaseScan_l(TupleIterator scan) throws ExecException 
  {
    assert scan != null;
    index.releaseScan(scan);
  }
  
  // RelStore interface
  public void insertTuple_r(ITuplePtr tuplePtr, int stubId) throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_INSERT, this, 
                  "insert_r", tuplePtr, stubId);

    // it should be a valid stub
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);

    ITuple tuple = tuplePtr.pinTuple(IPinnable.WRITE);
    byte[] stubBits;
    
    // temp variable to handle position of newly inserted node
    ITupleDoublyListNode newNode;
    
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
      if (BitVectorUtil.isNull(stubBits))
      {
        newNode = listStore.add(tuplePtr);
        tuple.oValueSet(colRef.getColnum(), newNode.getHandle(listStore));
        newNode.unpin();
      }

      byte[] stubBits1 = BitVectorUtil.setBit(stubBits, stubId);
      if (stubBits != stubBits1)
        tuple.bValueSet(colIns.getColnum(), stubBits1, stubBits1.length);
    }
    
    tuplePtr.unpinTuple();
    factory.addRef(tuplePtr);
  }

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
  
      assert (activeStubs.get(stubId) == true);
      
      byte[] stubBits1 = BitVectorUtil.clearBit(stubBits, stubId);
      if (stubBits != stubBits1)
        tuple.bValueSet(colIns.getColnum(), stubBits1, stubBits1.length);
      
      // Does the tuple need to be deleted from the list
      if (BitVectorUtil.isNull(stubBits))
      {
        assert listStore.contains(tuplePtr);
        listStore.remove((IListNodeHandle)tuple.oValueGet(colRef.getColnum()));
      }
    }
    tuplePtr.unpinTuple();
    factory.release(tuplePtr);
  }

  public TupleIterator getScan_r(int stubId) throws ExecException
  {
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);

    TupleIterator iter = stubs.get(stubId);
    assert iter != null;
    assert iter instanceof LineageStoreImplIter;
    LineageStoreImplIter iterL = (LineageStoreImplIter) iter;
    iterL.initialize(this, listStore, iFactory);
    LogLevelManager lm = execContext.getLogLevelManager();
    lm.trace(LogArea.STORE, LogEvent.STORE_SCAN_START, this, 
                  "getScan_r", stubId, iterL);
    return iter;
  }

  public void releaseScan_r(TupleIterator iter, int stubId)
      throws ExecException
  {
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_SCAN_STOP, this, iter, stubId);
    if (activeStubs.get(stubId) == false)
      throw new ExecException(ExecutionError.INVALID_STUBID, stubId);

    assert iter != null;
    assert iter instanceof LineageStoreImplIter;
    LineageStoreImplIter iterL = (LineageStoreImplIter) iter;
    iterL.release(iFactory);
  }

  public List<Index> getIndexes() 
  {
    if (index == null) 
      return null;
    ArrayList<Index> indexes = new ArrayList<Index>(2);
    indexes.add(index);
    return indexes;
  }
  
  // toString method
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    return sb.toString();
  }

  public synchronized void dump(IDumpContext dumper)
  {
    if (!dumper.isVerbose())
    {
      String tag = LogUtil.beginDumpObj(dumper, this);
      if (listStore != null)
        listStore.dump(dumper);
      if (index != null)
        index.dump(dumper);
      LogUtil.endDumpObj(dumper, tag);
      return;
    }
    String dumperKey = StringUtil.getBaseClassName(this);
    IDumpContext b = dumper.openDumper(dumperKey);
    String tag = LogUtil.beginDumpObj(b, this);
    if (listStore != null)
    {
      b.beginTag(TAG_LINEAGESTORE_LIST, null, null);
      listStore.dump(b);
      b.endTag(TAG_LINEAGESTORE_LIST);
    }
    else
    {
      b.writeln(TAG_LINEAGESTORE_LIST, "null");
    }
    if (index != null)
    {
      b.beginTag(TAG_LINEAGESTORE_INDEX, null, null);
      index.dump(b);
      b.endTag(TAG_LINEAGESTORE_INDEX);
    }
    else
    {
      b.writeln(TAG_LINEAGESTORE_INDEX, "null");
    }
    LogUtil.endDumpObj(b, tag);
    b.closeDumper(dumperKey, dumper);
  }

  public boolean evict()
    throws ExecException
  {
    boolean b = index.evict();
    b |= listStore.evict() ;
    b |= super.evict();
    return b;
  }
}
