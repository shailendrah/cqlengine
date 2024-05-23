/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/stores/PartnWindowStore.java /main/16 2012/06/20 05:24:31 pkali Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares PartnWindowStore in package oracle.cep.execution.stores.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    pkali     05/31/12 - added windows size tracking methods
    sbishnoi  12/01/11 - support for variable duration partition window
    udeshmuk  09/24/08 - 
    anasrini  09/20/08 - add method replaceOldestTuple_p
    hopark    11/29/07 - remove AtomicInteger
    hopark    10/22/07 - remove TimeStamp
    rkomurav  09/13/07 - add getpartniter
    hopark    06/11/07 - logging - remove ExecContext
    hopark    05/24/07 - logging support
    hopark    04/30/07 - add getOldestTuple_p
    najain    03/14/07 - cleanup
    najain    02/06/07 - coverage
    najain    01/05/07 - spill over support
    hopark    01/26/07 - remove timed tuple
    hopark    01/06/07 - add getScan_r
    hopark    12/15/06 - support range
    hopark    12/06/06 - add timestamp and scan
    ayalaman  08/02/06 - partition window implementation
    najain    03/09/06 - bug fix
    skaluska  02/16/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/stores/PartnWindowStore.java /main/16 2012/06/20 05:24:31 pkali Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.stores;

import oracle.cep.execution.ExecException;

import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.dataStructures.internal.IPartition;
import oracle.cep.dataStructures.internal.ITuplePtr;

/**
 *  Store used for partition window synopsis. This store maintains a table
 *  of partition keys (header tuples) with a hash index on them. Each
 *  header tuple has a pointer to the oldest and the newesti data tuples in
 *  the partition as well as the count of rows in the partition. Each data
 *  tuple in turn has a pointer to the next tuple within the partition.
 *  The 'pointers' are managed using the Object columns in the tuples.
 *
 * @author ayalaman
 */
public interface PartnWindowStore
{
  /**
   * Inserts the specified tuple into the store.
   * 
   * @param tuple   Tuple to be inserted
   * @param ts      timeStamp to be inserted
   * @param synId   Synopsis id
   *
   * @throws  ExecException for any underlying errors
   */
  public void insertTuple_p(ITuplePtr tuple, long ts, int synId) throws ExecException;

  /**
   * Deletes the oldest tuple in the a particular partition.
   * 
   * @param partnSpec       Partition spec
   * @param synId           Synopsis id
   *
   * @return Reference to the deleted tuple
   *
   * @throws  ExecException for any underlying errors
   */
  public ITuplePtr deleteOldestTuple_p(ITuplePtr partnSpec, int synId)
      throws  ExecException;
  
  /**
   * Deletes the specified tuple in the a particular partition.
   * 
   * @param tuple           tuple
   * @param ts              element timestamp of deleted tuple
   * @param synId           Synopsis id
   *
   * @return Reference to the deleted tuple
   *
   * @throws  ExecException for any underlying errors
   */
  public void deleteTuple_p(ITuplePtr tuple, long ts, int synId)
      throws  ExecException;

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
    throws ExecException;

  /**
   * Gets the oldest tuple in the a particular partition.
   * 
   * @param partnSpec       Partition spec
   * @param synId           Synopsis id
   *
   * @return Reference to the oldest tuple
   *
   * @throws  ExecException for any underlying errors
   */
  public ITuplePtr getOldestTuple_p(ITuplePtr partnSpec, int synId)
      throws  ExecException;

  /**
   * Returns the oldest timed tuple in the store for the specified synopsis.
   * @param stubId Stub that identifies the synopsis
   * @return The oldest timed tuple if any, else null
   */
  public TimedTuple getOldestTimedTuple_w(int stubId) throws ExecException;

  /**
   * Checks if the specified store is empty
   * @param stubId Stub that identifies the synopsis
   * @return true if empty or false
   */
  boolean isEmpty_w(int stubId) throws ExecException;

  /**
   * Gets the actual tuples count in the partition list 
   * corresponding to the partition spec. If you want to 
   * track the size of the partition window based on the tuples
   * arrival and expiry use getPartnWindowSize method
   * 
   * @param partnSpec     Partition spec
   * @param synId         Synopsis id
   * 
   * @return Size as in number of tuples in the partition.
   *
   * @throws  ExecException for any underlying errors
   */
  public int getPartnSize_p(ITuplePtr partnSpec, int synId) 
    throws ExecException;
  
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
  public int getPartnWindowSize(ITuplePtr partnSpec, int synId) 
    throws ExecException;

  /**
   * Add a new reader for this store.
   * 
   * @return The stubId
   * @throws ExecException
   */
  public int addStub() throws ExecException;

  /**
   * Delete a reader from this store.
   *
   * @param stubId The stubId
   * @throws  ExecException when attempted by an invalid reader
   */
  public void removeStub(int stubId) throws ExecException;

  /**
   * Scan the *entire* contents of stubId
   * 
   * @return The tuple iterator to scan the ttuples.
   */
  public TupleIterator getScan_r(int stubId) throws ExecException;

  /**
   * Release a scan that you previously got
   */
  public void releaseScan_r(TupleIterator iter, int stubId)
      throws ExecException;

  /**
   * Returns the partition corresponding to the partn spec
   * @param partnSpecPtr
   * @param stubId
   * @return
   * @throws ExecException
   */
  public IPartition getPartition(
      ITuplePtr partnSpecPtr, int stubId)
      throws ExecException;
  
  /**
   * TimedTuple is used simply as data carriers.
   * It is not supposed to be used as a general class other than 'getOldestTimedTuple_w' api.
   */
  public static class TimedTuple 
  {
    public long timeStamp;
    public ITuplePtr     tuple;
  }
  
  /**
   * Increments the window size corresponding to the partition spec
   *
   * @param partnSpecPtr Partition spec
   * @param stubId The stubId
   * @throws ExecException if unable to scan the store
   */
  public void incrementWindowSize(ITuplePtr partnSpecPtr, int stubId) 
      throws ExecException;
  
  /**
   * Decrements the window size corresponding to the partition spec
   *
   * @param partnSpecPtr Partition spec
   * @param stubId The stubId
   * @throws ExecException if unable to scan the store
   */
  public void decrementWindowSize(ITuplePtr partnSpecPtr, int stubId) 
      throws ExecException;
}
