/* $Header: RelStore.java 11-jun-2007.22:21:28 hopark Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 Declares RelStore in package oracle.cep.execution.stores.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    hopark    06/11/07 - logging - remove ExecContext
    hopark    05/24/07 - logging support
    najain    03/14/07 - cleanup
    najain    01/04/07 - spill over support
    parujain  11/30/06 - DoublyListIterator factory impl
    najain    07/03/06 - do not extend StorageAlloc
    najain    06/14/06 - query deletion support 
    najain    06/14/06 - bug fix 
    ayalaman  04/28/06 - RelStore extends StorageAlloc 
    skaluska  02/15/06 - Creation
    skaluska  02/15/06 - Creation
 */

/**
 *  @version $Header: RelStore.java 11-jun-2007.22:21:28 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.stores;

import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.ExecException;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.operators.ExecOpt;

/**
 * RelStore interface. RelStore is not a general purpose store for any
 * StorageElement, but it only works for Tuple. Moreover, it also assumes
 * the existence of some bitvectors in the Tuple to find out whether the
 * tuple belongs to a particular stub or not. It is assumed that the
 * tuple contains the following attributes:
 * position 0: byte array corresponding to the number of readers at that time
 * position 1: byte array corresponding to the number of readers at that time
 *
 * For example, if a total of 5 stubs have been added, but stub 2 has been deleted,
 * the bitvector would be:: 11011
 *
 * @author skaluska
 *
 */
public interface RelStore 
{
  /**
   * Insert a tuple into the synopsis stubId
   */

  abstract void insertTuple_r(ITuplePtr tuple, int stubId) 
    throws ExecException;

  /**
   * Delete the indicated tuple from the synopsis stubId
   */
  abstract void deleteTuple_r(ITuplePtr tuple, int stubId) 
    throws ExecException;

  /**
   * Scan the *entire* contents of stubId
   * @return The tuple iterator to scan the tuples.
   */
  abstract TupleIterator getScan_r(int stubId) throws ExecException;

  /**
   * Release a scan that you previously got
   */
  abstract void releaseScan_r(TupleIterator iter, int stubId) throws ExecException;

  public abstract void removeStub(int stubId) throws ExecException;
  
  /** Returns the number of active stubs associated with this store */
  public int getNumStubs();
  
  /** Returns true if the tuples of store is recovered after loading data from snapshot */
  public boolean isRecovered();
  
  /** Set to true if the tuples of store is recovered after loading data from snapshot */
  public void setRecovered(boolean flag);
}
