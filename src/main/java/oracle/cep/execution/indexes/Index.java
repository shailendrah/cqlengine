/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/indexes/Index.java /main/12 2009/09/22 06:58:20 udeshmuk Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares Index in package oracle.cep.execution.indexes.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    udeshmuk  09/16/09 - remove fullscan as it is specific to hashindex
    hopark    12/26/07 - use IDumpable
    hopark    10/25/07 - make evictable
    hopark    09/17/07 - add clear
    hopark    06/15/07 - add getId
    hopark    06/11/07 - logging - remove ExecContext
    hopark    06/15/07 - add getId
    hopark    06/11/07 - logging - remove ExecContext
    hopark    05/24/07 - debug logging
    najain    04/11/07 - bug fix
    najain    04/03/07 - bug fix
    najain    03/14/07 - cleanup
    najain    01/05/07 - spil over support
    skaluska  03/01/06 - Creation
    skaluska  03/01/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/indexes/Index.java /main/12 2009/09/22 06:58:20 udeshmuk Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.indexes;

import oracle.cep.execution.ExecException;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.logging.IDumpable;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IEvictableObj;

/**
 * Index
 *
 * @author skaluska
 */
public interface Index extends IEvictableObj, IDumpable
{
  int getId();
  
  /**
   * Clear this index so that it contains no key.
   * 
   * Note : Since the implementation of Index interface does not
   * understand the underlying datastructure of <key, value>s.
   * it is client's responsiblity to make sure that the underlying
   * datastructure is cleaned up(e.g handling refcount etc) before
   * invoking this method. 
   */
  void clear();
  
  void setFactory(IAllocator<ITuplePtr> factory);

  void insertTuple(ITuplePtr tuple) throws ExecException;

  void deleteTuple(ITuplePtr tuple) throws ExecException;

  /**
   * Scans the index for bound tuple.
   *  
   * @return
   * @throws ExecException
   */
  TupleIterator getScan() throws ExecException;
  
  void releaseScan(TupleIterator iter) throws ExecException;
}
