/* $Header: pcbpel/cep/server/src/oracle/cep/execution/synopses/PrivatePartnWindowSynopsis.java /main/2 2008/11/13 21:59:39 udeshmuk Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates.All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    11/05/08 - rename the class.
    udeshmuk    10/10/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/synopses/PrivatePartnWindowSynopsis.java /main/2 2008/11/13 21:59:39 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.synopses;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;

public interface PrivatePartnWindowSynopsis {

  /**
   * Inserts the specified tuple.
   *
   * @param tuple Tuple to be inserted
   * @throws ExecException if unable to scan the synopsis
   */
  public void insertTuple(ITuplePtr tuple, long ts) throws ExecException;

  /**
   * Deletes the oldest tuple.
   *
   * @param partnSpec Partition spec
   * @return Reference to the deleted tuple
   * @throws ExecException if unable to scan the synopsis
   */
  public ITuplePtr deleteOldestTuple(ITuplePtr partnSpec) throws ExecException;

  /**
   * Replaces the oldest tuple in the partition with provided tuple.
   * 
   * @param tuplePt Tuple to be inserted in the partition 
   * @param ts timestamp associated with the tuple
   * @return Reference to the deleted tuple
   * @throws ExecException for any underlying errors
   */
  public ITuplePtr replaceOldestTuple(ITuplePtr tuplePtr, long ts)
    throws ExecException;

  /**
   * Gets the size corresponding to the partition spec
   *
   * @param partnSpec Partition spec
   * @return Size
   * @throws ExecException if unable to scan the synopsis
   */
  public int getPartnSize(ITuplePtr partnSpec) throws ExecException;

  /**
   * Checks if the synopsis is empty.
   * @return true if empty else false
   */
  boolean isEmpty() throws ExecException;

  /**
   * Set the numRows and initialize the partnArr
   * @param numRows
   */
  public void setNumRows(int numRows);
  
  /**
   * Gets the tuple array of the partition
   * @param partnSpec
   * @return
   * @throws ExecException
   */
  public ITuplePtr[] getPartnArr(ITuplePtr partnSpec) throws ExecException;
  
  /**
   * Removes the synopsis from the underlying store 
   */
  public void remove() throws ExecException;
  
  /**
   * Expires tuples in the store for which timestamp+maxPrevRange < arg ts
   */
  public void  expireTuples(long ts) throws ExecException;
  
  /**
   * Sets the prevRangeExists variable
   * @param range true if the query has prev with range
   */
  public void setSupportRangeFunctionality(boolean range);
  
  /**
   * Sets the maxPrevRange variable
   * @param ts the maximum prev range duration
   */
  public void setTimeRange(long ts);
}

