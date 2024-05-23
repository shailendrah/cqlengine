/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/synopses/WindowSynopsis.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares WindowSynopsis in package oracle.cep.execution.synopses.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 udeshmuk  01/21/09 - API to compare two consecutive tuples in synopsis
 hopark    10/22/07 - remove TimeStamp
 hopark    06/11/07 - logging - remove ExecContext
 hopark    05/24/07 - logging support
 najain    03/14/07 - cleanup
 najain    01/05/07 - spill over support
 najain    07/13/06 - ref-count timeStamp support 
 najain    06/27/06 - add remove 
 najain    06/14/06 - query deletion support 
 najain    03/08/06 - change Element to StorageElement
 skaluska  02/16/06 - Creation
 skaluska  02/16/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/synopses/WindowSynopsis.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.synopses;

import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.windows.Window;
import oracle.cep.execution.snapshot.IPersistable;
import oracle.cep.dataStructures.internal.ITuplePtr;

/**
 * WindowSynopsis is the interface used by operators that use a
 * window synopsis.
 *
 * @author skaluska
 */
public interface WindowSynopsis extends IPersistable{
  /**
   * Inserts the specifed tuple in the synopsis.
   * @param elem StorageElement to be inserted (contains the tuple and timestamp)
   */
  void insertTuple(ITuplePtr elem, long ts) throws ExecException;

  /**
   * Checks if the synopsis is empty.
   * @return true if empty else false
   */
  boolean isEmpty() throws ExecException;

  /**
   * Gets the oldest tuple.
   * @return oldest tuple if any, else null
   */
  ITuplePtr getOldestTuple() throws ExecException;

  /**
   * Gets the oldest timestamp.
   * @return oldest timestamp if any, else null
   */
  long getOldestTimeStamp() throws ExecException;

  /**
   * Deletes the (one) oldest tuple if any
   */
  void deleteOldestTuple() throws ExecException;

  /** remove the synopsis from the underlying store */
  void remove() throws ExecException;
  
  /** compares oldest and next oldest tuple in synopsis after finding their visTs.
   * returns true if next oldest ts > oldest ts
   * return false if next oldest ts == oldest ts OR next oldest tuple does not 
   *              exist
   * @return
   * @throws ExecException
   */
  boolean compareConsecutiveTuples(Window window) throws ExecException;
}
