/* $Header: pcbpel/cep/server/src/oracle/cep/execution/stores/BasePartnWindowStoreImpl.java /main/1 2008/11/13 21:59:39 udeshmuk Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates.All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    11/07/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/stores/BasePartnWindowStoreImpl.java /main/1 2008/11/13 21:59:39 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.stores;

import java.util.ArrayList;
import java.util.List;

import oracle.cep.dataStructures.internal.IPartition;
import oracle.cep.dataStructures.internal.IPartitionIter;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.indexes.HashIndex;
import oracle.cep.execution.indexes.Index;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.service.ExecContext;

public abstract class BasePartnWindowStoreImpl extends ExecStore
{
  /**
   * Position of the column (in the partition key-header) that stores a pointer
   * to the tuple list for the partition window.
   */
  protected int                             hdrWindowListPos;

  /** evaluation context for store/index operations */
  protected IEvalContext                     evalCtx;

  /** evaluator and instructions for copying into the partition header tuple. */
  protected IAEval                           hdrCopyEval;

  /** header/partition key tuple factory */
  protected IAllocator<ITuplePtr>           hdrTupFac;

  /** hash index for the partition keys */
  protected HashIndex                       headerTupIndex;

  /**
   * The stub for the Pattern operator that uses this store as as a
   * partition window store. 
   */
  protected int                             primaryStub;

  /**
   * List of timed tuples (used only when prev with range exists)
   */
  protected IPartition                      ttuples;

  /** Iterator over ttuples (the global list) */
  protected IPartitionIter                  oldestItr;  

  /** Storage allocator for the tuple list */
  protected IAllocator<IPartition>          ttupleListFac;
  protected IAllocator                      ttupleNodeFac;
    
  //Column number for storing reference to DoublyList Node 
  // which points to this tuple
  protected Column                          colRef;
  
  /** Storage manager for doubly list iterator */
  protected IAllocator<IPartitionIter>      i2Factory;
  
  public BasePartnWindowStoreImpl(ExecContext ec, ExecStoreType stype)
  {
    super(ec, stype);
    // TODO Auto-generated constructor stub
  } 

  public IAllocator<ITuplePtr> getHdrTupFactory()
  {
    return hdrTupFac;
  }
  
  /**
   * Sets the position of various columns in the partition header tuple
   * 
   * @param windowList
   *          position of the column storing list of tuples
   */
  public void setHeaderColPositions(int windowList)
  {
    this.hdrWindowListPos = windowList;
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
   * Set the evaluation context for the window store
   * 
   * @param evalCtx
   *          instance of evaluation context
   */
  public void setEvalContext(IEvalContext evalCtx)
  {
    this.evalCtx = evalCtx;
  }

  /**
   * Set the evaluator to copy the necessary attribute values into the header
   * tuple
   * 
   * @param copyEval
   *          copy evaluator
   */
  public void setHdrCopyEval(IAEval copyEval)
  {
    this.hdrCopyEval = copyEval;
  }

  /**
   * Set the hash index instance created to search the partition keys
   * 
   * @param hashIndex
   *          index instance to be set
   */
  public void setHeaderIndex(HashIndex hashIndex)
  {
    this.headerTupIndex = hashIndex;
  }

  /**
   * Set the primary stub that is created for the partition window operator.
   * Rest of the stubs sharing this store use it as a rel store
   */
  public void setPrimaryStub(int primStub)
  {
    this.primaryStub = primStub;
  }
  
  protected ITuplePtr getPartitionHdr(ITuplePtr partnSpecPtr, 
      int stubId)
    throws ExecException
  {
    if (partnSpecPtr == null) return null;
    TupleIterator hdrScan;
    ITuplePtr hdrtuple = null;
    
    // only a primary stub (belonging to that of a partition window operator)
    // can delete from a partition. Therefore, there is no need of any
    // synchronization
    assert stubId == primaryStub;
    
    // bind the input tuple in the INPUT ROLE
    evalCtx.bind(partnSpecPtr, IEvalContext.INPUT_ROLE);
    
    // find a partition with the given key spec
    hdrScan = headerTupIndex.getScan();
    
    // a partition with the matching key does not exist
    hdrtuple = hdrScan.getNext();
    
    // hdrTuple now points to the header tuple of the desired partition
    headerTupIndex.releaseScan(hdrScan);
    
    return hdrtuple;
  }

  
  protected void releaseHdrTuple(ITuplePtr hdrtuple, boolean del)
    throws ExecException
  {
    ITuple hdrTuple = hdrtuple.pinTuple(IPinnable.WRITE);
    IPartition tupList = hdrTuple.oValueGet(hdrWindowListPos); 
    ttupleListFac.release(tupList);
    hdrTuple.oValueSet(hdrWindowListPos, null);
    hdrtuple.unpinTuple();
    if (del)
    {
      headerTupIndex.deleteTuple(hdrtuple);
    }
    hdrTupFac.release(hdrtuple);
  }
  
  public IPartition getPartition(
      ITuplePtr partnSpecPtr, int stubId)
    throws ExecException
  {
    ITuplePtr hdrtuple = getPartitionHdr(partnSpecPtr, stubId);
    if (hdrtuple == null) return null;
    ITuple hdrTuple = hdrtuple.pinTuple(IPinnable.READ);
    IPartition res = hdrTuple.oValueGet(hdrWindowListPos);
    hdrtuple.unpinTuple();
    hdrTupFac.release(hdrtuple); //HashIndexIterstor.getNext in getPartition increases refcount
    return res;
  }
  
  /**
   * Gets the size corresponding to the partition spec
   * 
   * @param partnSpec
   *          Partition spec
   * @param stubId
   *          Synopsis id
   * 
   * @return Size as in number of ttuples in the partition.
   * 
   * @throws ExecException
   *           for any underlying errors
   */
  public int getPartnSize_p(ITuplePtr partnSpecPtr, int stubId) throws ExecException
  {
    //partnSpec==null means this tuple is from a heartbeat
    if (partnSpecPtr == null) return 0;
    IPartition partition = getPartition(partnSpecPtr, stubId);
    if (partition == null) return 0;
    int sz = partition.getSize();
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_GET, this, 
            "getPartnSize_p", partnSpecPtr, stubId, sz);
    return sz;
  }
  
  public List<Index> getIndexes() 
  {
    if (headerTupIndex == null)
      return null;
    ArrayList<Index> indexes = new ArrayList<Index>(2);
    indexes.add(headerTupIndex);
    return indexes;
  }
}