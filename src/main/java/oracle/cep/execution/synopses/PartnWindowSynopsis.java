/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/synopses/PartnWindowSynopsis.java /main/13 2012/06/20 05:24:32 pkali Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares PartnWindowSynopsis in package oracle.cep.execution.synopses.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    pkali     05/31/12 - added windows size tracking methods
    sbishnoi  12/01/11 - support for variable duration partition window
    udeshmuk  09/24/08 - 
    anasrini  09/20/08 - add method replaceOldestTuple
    hopark    10/22/07 - remove TimeStamp
    rkomurav  09/13/07 - add getpartitioniter
    hopark    06/11/07 - logging - remove ExecContext
    hopark    05/24/07 - logging support
    hopark    04/30/07 - add getPartnOldestTimedTuple
    najain    03/14/07 - cleanup
    hopark    01/26/07 - remove TimedTuple
    najain    01/05/07 - spill over support
    hopark    12/15/06 - supports range 
    hopark    12/13/06 - use timestamp on inserting
    ayalaman  07/29/06 - implementation
    skaluska  02/16/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/synopses/PartnWindowSynopsis.java /main/13 2012/06/20 05:24:32 pkali Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.synopses;

import oracle.cep.execution.ExecException;
import oracle.cep.execution.stores.PartnWindowStore;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.dataStructures.internal.ITuplePtr;

/**
 * Synopsis for partition windows
 *
 * @author skaluska
 */
public interface PartnWindowSynopsis {

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
   * Gets the oldest timed tuple.
   * @return oldest timed tuple if any, else null
   */
  public PartnWindowStore.TimedTuple getOldestTimedTuple() throws ExecException;

  /**
   * Gets the oldest timed tuple in a partition.
   * @return oldest timed tuple if any, else null
   */
  public ITuplePtr getPartnOldestTuple(ITuplePtr partnSpec) 
    throws ExecException;

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
   * Removes the synopsis from teh underlying store 
   */
  public void remove() throws ExecException;
  
  /**
   * Deletes a tuple from the synopsis.
   *
   * @param tuple The tuple to be deleted.
   * @param ts element timestamp value
   * @throws ExecException 
   */
  public void deleteTuple(ITuplePtr tuple, long ts) throws ExecException;
  
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
  public int getPartnWindowSize(ITuplePtr partnSpec) throws ExecException;
  
  /**
   * Increments the window size corresponding to the partition spec
   *
   * @param partnSpec Partition spec
   * @throws ExecException if unable to scan the synopsis
   */
  public void incrementWindowSize(ITuplePtr partnSpec) throws ExecException;
  
  /**
   * Decrements the window size corresponding to the partition spec
   *
   * @param partnSpec Partition spec
   * @throws ExecException if unable to scan the synopsis
   */
  public void decrementWindowSize(ITuplePtr partnSpec) throws ExecException;
 }
