/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/ITuplePtr.java hopark_cqlsnapshot/2 2016/02/26 10:21:32 hopark Exp $ */

/* Copyright (c) 2007, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
   ITuplePtr is just an alias of ITuplePtr.
   It is to provide better readability of source codes.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/11/12 - add snapshotid
    hopark      04/09/09 - add copy
    hopark      12/27/07 - support xmllog
    hopark      12/06/07 - cleanup spill
    hopark      10/08/07 - use IListNodeElem
    hopark      09/29/07 - pass RefPtr
    hopark      07/12/07 - add compare
    hopark      06/19/07 - cleanup
    hopark      04/17/07 - remove refcount stuff
    hopark      03/21/07 - add pin
    najain      03/12/07 - cleanup
    hopark      03/06/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/ITuplePtr.java hopark_cqlsnapshot/2 2016/02/26 10:21:32 hopark Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.dataStructures.internal;

import oracle.cep.execution.ExecException;
import oracle.cep.execution.snapshot.IPersistable;
import oracle.cep.logging.IDumpable;
import oracle.cep.execution.snapshot.IPersistable;

public interface ITuplePtr 
  extends IListNodeElem, IDumpable, IPersistable, TimestampAware
{
  /**
   * Gets the id of the tuple if not null
   * zero otherwise.
   * @return
   */
  public long getId();
  
  /**
   * Get the snapshotid of the associated tuple.
   * Returns long.max_value if tuple is null.
   * @return
   */
  public long getSnapshotId();
  
  /**
   * set the snapshotid of the associated tuple.
   * Does nothing if tuple is null.
   * @param newSnapshotId
   */
  public void setSnapshotId(long newSnapshotId);
  
  /**
   * Set whether the tuple is recovered from snapshot as part of CQL HA.
   * @param flag
   */
  public void setRecovered(boolean flag);
  
  /**
   * Get whether the tuple is recovered from snapshot as part of CQL HA
   * @return
   */
  public boolean isRecovered();
  
  /**
   * Set true if this is last tuple while recovering synopsis contents during
   * snapshot load.
   * @param flag
   */
  public void setLastRecovered(boolean flag);
  
  /**
   * Returns true if this is last tuple while recovering synopsis contents
   * during snapshot load.
   * @return
   */
  public boolean isLastRecovered();
  
  /**
   * Set true if this is first tuple while recovering synopsis contents during
   * snapshot load.
   * @param flag
   */
  public void setFirstRecovered(boolean flag);
  
  /**
   * Returns true if this is first tuple while recovering synopsis contents
   * during snapshot load.
   * @return
   */  
  public boolean isFirstRecovered();

  /**
   * Peeks the tuple without retrieving
   *
   * @return the tuple
   */
  ITuple peek();

  /**
   * Pins the tuple. If the tuple has swapped out, retreive it
   * from the storage and reset the referent.
   * @return
   */
  ITuple pinTuple(int mode) throws ExecException;
  void unpinTuple() throws ExecException;
  
  void copy(ITuplePtr other, int numAttr) throws ExecException;
  void copy(ITuplePtr other, int[] srcAttrs, int[] destAttrs) throws ExecException;
  boolean compare(ITuplePtr src) throws ExecException;
  boolean compare(ITuplePtr srcPtr, int[] skipPos) throws ExecException;
}

