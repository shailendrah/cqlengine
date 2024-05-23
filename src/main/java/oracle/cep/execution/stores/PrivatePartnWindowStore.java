/* $Header: pcbpel/cep/server/src/oracle/cep/execution/stores/PrivatePartnWindowStore.java /main/3 2008/11/13 21:59:38 udeshmuk Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates.All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    11/05/08 - change the name of the class
    hopark      10/19/08 - pass ExecContext
    udeshmuk    10/07/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/stores/PrivatePartnWindowStore.java /main/3 2008/11/13 21:59:38 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.stores;

import oracle.cep.execution.ExecException;

import oracle.cep.dataStructures.internal.IPartition;
import oracle.cep.dataStructures.internal.ITuplePtr;

/**
 *  Store used for private partition window synopsis. This store maintains a table
 *  of partition keys (header tuples) with a hash index on them. Each
 *  header tuple has a pointer to the oldest and the newest data tuples in
 *  the partition as well as the count of rows in the partition. Each data
 *  tuple in turn has a pointer to the next tuple within the partition.
 *  The 'pointers' are managed using the Object columns in the tuples.
 *
 *  This is used in Pattern operator where we do not need to share it with
 *  any other operator.
 * @author udeshmuk
 */
public interface PrivatePartnWindowStore
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
   * Checks if the specified store is empty
   * @param stubId Stub that identifies the synopsis
   * @return true if empty or false
   */
  boolean isEmpty_w(int stubId) throws ExecException;

  /**
   * Gets the size corresponding to the partition spec
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
   * Returns the partition corresponding to the partn spec
   * @param partnSpecPtr
   * @param stubId
   * @return
   * @throws ExecException
   */
  public IPartition getPartition(
      ITuplePtr partnSpecPtr, int stubId)
      throws ExecException;
  
  public void expireTuples(long ts) throws ExecException;
  
  public void setSupportRangeFunctionality(boolean range);
  
  public void setTimeRange(long ts);
  
  /**
   * TimedTuple is used simply as data carriers.
   * It is not supposed to be used as a general class other than 'getOldestTimedTuple_w' api.
   */
  public static class TimedTuple 
  {
    public long timeStamp;
    public ITuplePtr     tuple;
  }
}

