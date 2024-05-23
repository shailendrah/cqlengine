/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/synopses/PartnWindowSynopsisImpl.java /main/18 2012/06/20 05:24:29 pkali Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Implements PartnWindowSynopsis in package oracle.cep.execution.synopses.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    pkali     05/29/12 - added windows size tracking logic
    sbishnoi  12/08/11 - support for variable duration partition window
    hopark    12/02/08 - move LogLevelManaer to ExecContext
    udeshmuk  09/24/08 - 
    anasrini  09/20/08 - add method replaceOldestTuple
    hopark    06/18/08 - logging refactor
    hopark    12/27/07 - support xmllog
    hopark    11/06/07 - change list api
    hopark    10/22/07 - remove TimeStamp
    rkomurav  09/25/07 - cleanup getpartnArr call
    rkomurav  09/13/07 - add getpartitioniter.
    hopark    06/07/07 - use LogArea
    hopark    05/24/07 - logging support
    hopark    04/30/07 - add getPartnOldestTimedTuple
    najain    03/14/07 - cleanup
    hopark    01/26/07 - remove TimedTuple
    najain    01/05/07 - spill over support
    hopark    01/06/07 - add getScan
    hopark    12/15/06 - support range
    hopark    12/13/06 - add timestamp
    ayalaman  08/08/06 - part window synopsis implementation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/synopses/PartnWindowSynopsisImpl.java /main/18 2012/06/20 05:24:29 pkali Exp $
 *  @author  ayalaman
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.synopses;

import oracle.cep.execution.stores.PartnWindowStore; 
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.TupleIterator;
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

/**
 * Synopsis for partition windows
 *
 * @author ayalaman
 */
@DumpDesc(attribTags={"Id", "PhyId", "StubId"}, 
          attribVals={"getId", "getPhyId", "getStubId"},
          infoLevel=LogLevel.SYNOPSIS_INFO,
          evPinLevel=LogLevel.SYNOPSIS_TUPLE_PINNED,
          evUnpinLevel=LogLevel.SYNOPSIS_TUPLE_UNPINNED,
          dumpLevel=LogLevel.SYNOPSIS_DUMP,
          verboseDumpLevel=LogLevel.SYNOPSIS_DUMPELEMS)
public class PartnWindowSynopsisImpl extends ExecSynopsis 
    implements PartnWindowSynopsis
{

  /** Store to allocate and store the tuples in synopsis */
  private  PartnWindowStore   pStore; 
  
  /** number of rows per partition */
  private  int                numRows;
  
  /** Array of ITuplePtr of a partition */
  private  ITuplePtr[]        partnArr;
  

  /**
   * Constructor
   */
  public PartnWindowSynopsisImpl(ExecContext ec)
  {
    super(ExecSynopsisType.PARTNWINDOW, ec);
    
    pStore = null; 
  }

  /**
   * Set the store instance for the synopsis 
   *
   * @param  pwStore    store instance
   */
  public void setStore(PartnWindowStore pwStore)
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
   * Deletes the specified tuple.
   *
   * @param tuple parameter tuple
   * @param ts element timestamp
   * @return Reference to the deleted tuple
   * @throws ExecException if unable to scan the synopsis
   */
  @Override
  public void deleteTuple(ITuplePtr tuple, long ts) throws ExecException
  {
    LogLevelManager.trace(
      LogArea.SYNOPSIS, LogEvent.SYNOPSIS_DELETE, this, tuple);
    pStore.deleteTuple_p(tuple, ts, stubId); 
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
   * Gets the oldest tuple.
   * @return oldest tuple if any, else null
   */
  public PartnWindowStore.TimedTuple getOldestTimedTuple() throws ExecException
  {
    PartnWindowStore.TimedTuple ret = pStore.getOldestTimedTuple_w(stubId);
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_GET, this, "getOldestTimedTuple", 
                  (ret != null ? ret.tuple : null), 
                  (ret != null ? ret.timeStamp : null));
    return ret;
  }

  /**
   * Gets the oldest tuple in a partition.
   * @return oldest timed tuple if any, else null
   */
  public ITuplePtr getPartnOldestTuple(ITuplePtr partnSpec) 
    throws ExecException
  {
    ITuplePtr ret = pStore.getOldestTuple_p(partnSpec, stubId); 
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_GET, this, 
                  "getPartnOldestTuple", partnSpec, ret);
    return ret;
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
   * Gets the window size corresponding to the partition spec.
   * Window size is maintained according to the incoming and 
   * expiring tuples
   * 
   * @param partnSpec     Partition spec
   * 
   * @return Size based on the number of tuples arrived and expired
   * in the partition window
   *
   * @throws  ExecException for any underlying errors
   */
  public int getPartnWindowSize(ITuplePtr partnSpec) throws ExecException
  {
    int sz = pStore.getPartnWindowSize(partnSpec, stubId); 
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_GET, this, 
                  "getPartnWindowSize", partnSpec, sz);
    return sz;
  }
  /**
   * Increments the window size corresponding to the partition spec
   *
   * @param partnSpec Partition spec
   * @throws ExecException if unable to scan the synopsis
   */
  public void incrementWindowSize(ITuplePtr partnSpec) throws ExecException
  {
    pStore.incrementWindowSize(partnSpec, stubId); 
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_INSERT, this, 
                  "incrementWindowSize", partnSpec);
  }
  
  /**
   * Decrements the window size corresponding to the partition spec
   *
   * @param partnSpec Partition spec
   * @throws ExecException if unable to scan the synopsis
   */
  public void decrementWindowSize(ITuplePtr partnSpec) throws ExecException
  {
    pStore.decrementWindowSize(partnSpec, stubId); 
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_DELETE, this, 
                  "decrementWindowSize", partnSpec);
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
    IPartition                         partn;
    IDoublyListNode<ITuplePtr>         head;
    
    size  = 0;
    head  = null;
    partn = pStore.getPartition(partnSpec, stubId);
    
    if(partn != null)
      head = partn.getHead();
    
    //for a partition with rows = 5 and
    //if the current partition size is 3, 
    //the first two entries in the partn array will be null
    while(head != null)
    {
      partnArr[size] = head.getNodeElem();
      size ++;
      head = head.getNext(partn);
    }
    
    assert size <= numRows;
    
    if(size < numRows)
    {
      for(int i = 0; i < size; i++)
      {
        partnArr[numRows-1-i] = partnArr[size-1-i];
      }
      for(int i = 0; i < numRows - size; i++)
        partnArr[i] = null;
    }
    
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_GET, this,
                  "getPartnArr", partnSpec, size);
    return partnArr;
  }

  @Override
  public TupleIterator getScan(int scanId) throws ExecException
  {
    assert scanId == -1;
    TupleIterator ret = pStore.getScan_r(stubId);
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_SCAN_START, this, 
                  scanId, ret);
    return ret;
  }
  
  @Override
  public void releaseScan(int scanId, TupleIterator iter) throws ExecException
  {
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_SCAN_STOP, this, 
                  scanId, iter);
    assert scanId == -1;
    pStore.releaseScan_r(iter, stubId);
  }

  public synchronized void dump(IDumpContext dump) 
  {
    String tag = LogUtil.beginDumpObj(dump, this);
    ((ExecStore)pStore).dump(dump);
    LogUtil.endDumpObj(dump, tag);
  }

}
