/* $Header: pcbpel/cep/server/src/oracle/cep/execution/synopses/PrivatePartnWindowSynopsisImpl.java /main/3 2008/12/10 18:55:57 hopark Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates.All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/02/08 - move LogLevelManaer to ExecContext
    udeshmuk    11/05/08 - rename the class
    udeshmuk    10/10/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/synopses/PrivatePartnWindowSynopsisImpl.java /main/3 2008/12/10 18:55:57 hopark Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.synopses;

import oracle.cep.execution.stores.PrivatePartnWindowStore; 
import oracle.cep.execution.ExecException;
import oracle.cep.dataStructures.internal.IDoublyListNode;
import oracle.cep.dataStructures.internal.IPartition;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.service.ExecContext;

@DumpDesc(attribTags={"Id", "PhyId", "StubId"}, 
          attribVals={"getId", "getPhyId", "getStubId"},
          infoLevel=LogLevel.SYNOPSIS_INFO,
          evPinLevel=LogLevel.SYNOPSIS_TUPLE_PINNED,
          evUnpinLevel=LogLevel.SYNOPSIS_TUPLE_UNPINNED,
          dumpLevel=LogLevel.SYNOPSIS_DUMP,
          verboseDumpLevel=LogLevel.SYNOPSIS_DUMPELEMS)
public class PrivatePartnWindowSynopsisImpl extends ExecSynopsis 
    implements PrivatePartnWindowSynopsis
{

  /** Store to allocate and store the tuples in synopsis */
  private  PrivatePartnWindowStore   pStore; 
  
  /** number of rows per partition */
  private  int                numRows;
  
  /** Array of ITuplePtr of a partition */
  private  ITuplePtr[]        partnArr;

  /**
   * Constructor
   */
  public PrivatePartnWindowSynopsisImpl(ExecContext ec)
  {
    super(ExecSynopsisType.PVTPARTNWINDOW, ec);
    
    pStore = null; 
  }

  /**
   * Set the store instance for the synopsis 
   *
   * @param  pwStore    store instance
   */
  public void setStore(PrivatePartnWindowStore pwStore)
  {
    this.pStore = pwStore; 
  }

  public void init()
  {
    stubId = 0;
    pStore = null;
  }
  
  public void setNumRows(int numRows)
  {
    this.numRows  = numRows;
    this.partnArr = new ITuplePtr[numRows];
  }

  public void setSupportRangeFunctionality(boolean range)
  {
    pStore.setSupportRangeFunctionality(range);
  }
  
  public void setTimeRange(long ts)
  {
    pStore.setTimeRange(ts);
  }
  
  /** Remove the synopsis */
  public void remove() throws ExecException
  {
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_DDL, this, 
                  "remove", this);
    pStore.removeStub(stubId);
  }

  /**
   * Inserts the specified tuple.
   *
   * @param tuple Tuple to be inserted
   * @throws ExecException if unable to scan the synopsis
   */
  public void insertTuple(ITuplePtr tuple, long ts) throws ExecException
  {
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_INSERT, this, tuple, ts);
    pStore.insertTuple_p(tuple, ts, stubId); 
  }

  /**
   * Deletes the oldest tuple.
   *
   * @param partnSpec Partition spec
   * @return Reference to the deleted tuple
   * @throws ExecException if unable to scan the synopsis
   */
  public ITuplePtr deleteOldestTuple(ITuplePtr partnSpec) throws ExecException
  {
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_DELETE, this, partnSpec);
    return pStore.deleteOldestTuple_p(partnSpec, stubId); 
  }

  /**
   * Replaces the oldest tuple in the partition with provided tuple.
   * 
   * @param tuplePt Tuple to be inserted in the partition 
   * @param ts timestamp associated with the tuple
   * @return Reference to the deleted tuple
   * @throws ExecException for any underlying errors
   */
  public ITuplePtr replaceOldestTuple(ITuplePtr tuplePtr, long ts)
    throws ExecException
  {
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_DELETE, this, 
                          tuplePtr);
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_INSERT, this, 
                          tuplePtr, ts);
    return pStore.replaceOldestTuple_p(tuplePtr, ts, stubId); 
  }

  /**
   * Checks if the synopsis is empty.
   * @return true if empty else false
   */
  public boolean isEmpty() throws ExecException
  {
      return pStore.isEmpty_w(stubId);
  }

  /**
   * Gets the size corresponding to the partition spec
   *
   * @param partnSpec Partition spec
   * @return Size
   * @throws ExecException if unable to scan the synopsis
   */
  public int getPartnSize(ITuplePtr partnSpec) throws ExecException
  {
    int sz = pStore.getPartnSize_p(partnSpec, stubId); 
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_GET, this, 
                  "getPartnSize", partnSpec, sz);
    return sz;
  }
  
  /**
   * returns the partition array of ITuplePtr for the current partition
   * @param partnSpec
   * @return
   * @throws ExecException
   */
  public ITuplePtr[] getPartnArr(ITuplePtr partnSpec) throws ExecException
  {
    int                                size;
    int                                index;
    IPartition                         partn;
    IDoublyListNode<ITuplePtr>         head;
    
    size  = 0;
    index = 0;
    head  = null;
    partn = pStore.getPartition(partnSpec, stubId);
 
    if(partn != null)
    {
      size = partn.getSize();
      head = partn.getHead();
      while(head != null)
      {
        partnArr[size-1-index] = head.getNodeElem();
        index++;
        head = head.getNext(partn);
      }
      assert size <= numRows;
      // fill in null in the remaining slots.
      for(int i=size; i < numRows; i++)
        partnArr[i] = null;
    }
    else
    {
      for(int i=0; i < numRows; i++)
        partnArr[i] = null;
    }
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_GET, this,
                  "getPartnArr", partnSpec, size);
    return partnArr;
  }
 
  public synchronized void dump(IDumpContext dump) 
  {
    String tag = LogUtil.beginDumpObj(dump, this);
    ((ExecStore)pStore).dump(dump);
    LogUtil.endDumpObj(dump, tag);
  }
  
  public void expireTuples(long ts) throws ExecException
  { //TODO: may add logging stuff
    pStore.expireTuples(ts);
  }
}
