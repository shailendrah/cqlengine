/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/synopses/RelationSynopsis.java hopark_cqlsnapshot/3 2016/02/26 11:55:07 hopark Exp $ */

/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares RelationSynopsis in package oracle.cep.execution.synopses.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    hopark    06/11/07 - logging - remove ExecContext
    hopark    05/24/07 - logging support
    najain    03/14/07 - cleanup
    najain    01/05/07 - spill over support
    najain    06/27/06 - add remove 
    najain    06/14/06 - query deletion support 
    najain    06/14/06 - bug fix 
    najain    03/10/06 - 
    skaluska  02/16/06 - Creation
    skaluska  02/16/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/synopses/RelationSynopsis.java hopark_cqlsnapshot/3 2016/02/26 11:55:07 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.synopses;

import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.snapshot.IPersistable;

/**
 * RelationSynopsis is the interface used by operators that use a relation
 * synopsis.
 *
 * @author skaluska
 */
public interface RelationSynopsis extends IPersistable {
  /**
   * Inserts a new tuple into the synopsis.
   *
   * @param tuple The tuple to be inserted
   * @throws ExecException 
   */
  void insertTuple(ITuplePtr tuple) throws ExecException;

  /**
   * Deletes a tuple from the synopsis.
   *
   * @param tuple The tuple to be deleted.
   * @throws ExecException 
   */
  void deleteTuple(ITuplePtr tuple) throws ExecException;

  /**
   * Scan the  current bag  of tuples subject  to some  condition. A
   * synopsis only performs  certain pre-registered scans, and these
   * are identified by a  unique identifier.  In other words, scanId
   * implicitly identifies the scan conditions.
   * 
   * @param   scanId     Specification of which scan we want.
   * @return  iterator for the scan
   * @throws ExecException 
   */
  TupleIterator getScan(int scanId) throws ExecException;

  /**
   * Release a scan.
   */
  void releaseScan(int scanId, TupleIterator iter) throws ExecException;

  /** remove the synopsis from the underlying store */
  void remove() throws ExecException;
}
