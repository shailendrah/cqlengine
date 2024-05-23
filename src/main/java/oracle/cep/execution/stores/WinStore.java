/* $Header: pcbpel/cep/server/src/oracle/cep/execution/stores/WinStore.java /main/9 2009/04/02 23:58:00 udeshmuk Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
 DESCRIPTION
 Declares WinStore in package oracle.cep.execution.stores.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 udeshmuk  01/21/09 - API to compare two consecutive tuples in store
 hopark    10/22/07 - remove TimeStamp
 hopark    06/11/07 - logging - remove ExecContext
 hopark    05/24/07 - logging support
 najain    03/14/07 - cleanup
 najain    01/04/07 - spill over support
 parujain  12/07/06 - propagating relation
 najain    07/13/06 - ref-count timeStamp support 
 najain    06/27/06 - add remove 
 najain    06/14/06 - query deletion support 
 najain    03/08/06 - store of StorageElement
 skaluska  02/15/06 - Creation
 skaluska  02/15/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/stores/WinStore.java /main/9 2009/04/02 23:58:00 udeshmuk Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.stores;

import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.internals.windows.Window;
import oracle.cep.dataStructures.internal.ITuplePtr;

/**
 * WinStore interface.
 * This is the interface used by a window synopsis.
 * @author skaluska
 *
 */
public interface WinStore
{
  /**
   * Inserts a tuple into the store.
   * @param elem StorageElement to be inserted
   * @param elem timeStamp to be inserted
   * @param stubId Stub that identifies the synopsis
   */
  void insertTuple_w(ITuplePtr tuple, long ts, 
                     int stubId) throws ExecException;

  /**
   * Checks if the specified store is empty
   * @param stubId Stub that identifies the synopsis
   * @return true if empty or false
   */
  boolean isEmpty_w(int stubId) throws ExecException;

  /**
   * Returns the oldest StorageElement in the store for the specified synopsis.
   * @param stubId Stub that identifies the synopsis
   * @return The oldest StorageElement if any, else null
   */
  ITuplePtr getOldestTuple_w(int stubId) throws ExecException;

  /**
   * Returns the oldest TimeStamp in the store for the specified synopsis.
   * @param stubId Stub that identifies the synopsis
   * @return The oldest TimeStamp if any, else null
   */
  long getOldestTimeStamp_w(int stubId) throws ExecException;

  void deleteOldestTuple_w(int stubId) throws ExecException;

  public abstract void removeStub(int stubId) throws ExecException;
  
  public TupleIterator getScan_r(int stubId) throws ExecException;
  
  public void releaseScan_r(TupleIterator iter, int stubId)
    throws ExecException;
  
  public boolean compareConsecutiveTuples(Window window, int stubId) throws ExecException;
  
  /** Returns the number of active stubs associated with this store */
  public int getNumStubs();
  
  /** Returns true if the tuples of store is recovered after loading data from snapshot */
  public boolean isRecovered();
  
  /** Set to true if the tuples of store is recovered after loading data from snapshot */
  public void setRecovered(boolean flag);
}
