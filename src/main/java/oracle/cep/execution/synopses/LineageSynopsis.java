/* $Header: LineageSynopsis.java 17-dec-2007.16:25:37 parujain Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 Declares LineageSynopsis in package oracle.cep.execution.synopses.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    parujain  12/17/07 - db-join
    hopark    06/11/07 - logging - remove ExecContext
    hopark    05/24/07 - logging support
    najain    03/14/07 - cleanup
    najain    01/05/07 - spill over support
    najain    06/28/06 - add remove 
    najain    03/10/06 - account for Exceptions
    skaluska  02/16/06 - Creation
    skaluska  02/16/06 - Creation
 */

/**
 *  @version $Header: LineageSynopsis.java 17-dec-2007.16:25:37 parujain Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.synopses;

import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.snapshot.IPersistable;
import oracle.cep.dataStructures.internal.ITuplePtr;

/**
 *
 * The basic functionality exported by lineage synopsis is as follows.
 * When a  tuple is  inserted, we specify  a *lineage* for  the tuple.
 * The lineage for  a tuple is the collection  of tuples that produced
 * the inserted  tuple.  The synopsis  offers methods to  lookup using
 * lineage.
 *
 * Lineage  synopsis  is  useful  for  joins  and  project  operators.
 * @author skaluska
 */
public interface LineageSynopsis extends IPersistable {

  /**
   * Insert a tuple with a given lineage.  At any given point in
   * time there should be exactly one tuple in the synopsis with a
   * given lineage.
   *
   * @param tuple The tuple to be inserted
   * @param lineage Array of lineage tuples
   */

  void insertTuple(ITuplePtr tuple, ITuplePtr[] lineage) throws ExecException;

  /**
   * Delete a tuple from the synopsis.
   * 
   * @param tuple The tuple to be deleted.
   */

  void deleteTuple(ITuplePtr tuple) throws ExecException;

  /**
   * Get the scan with a given lineage if it exists.
   *
   * @param lineage Array of lineage tuples
   * @return Scan corresponding to the lineage
   */
  public TupleIterator getScan_l(ITuplePtr[] lineage) throws ExecException;
  
  public void releaseScan_l(TupleIterator scan) throws ExecException;

  /** remove the synopsis from the underlying store */
  void remove() throws ExecException;
}
