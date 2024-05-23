/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/indexes/IIndex.java /main/1 2009/09/22 06:58:20 udeshmuk Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    Interface for the Index data manipulation and Scan operations

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    09/07/09 - Creation
    anasrini    09/07/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/indexes/IIndex.java /main/1 2009/09/22 06:58:20 udeshmuk Exp $
 *  @author  anasrini
 *  @since   11.1.2
 */

package oracle.cep.extensibility.indexes;

/**
 * Interface for the Index data manipulation and Scan operations
 * Cartridges providing their own indexing scheme must implement this
 * interface.
 */

public interface IIndex
{

  // Index Maintenance Operations
  /**
   * Insert a new record into the index
   * @param newkey - the key - the "actual" type of this will typically be
   *               a domain-specific type
   * @param val - the value to be associated with this key. The index will
   *              treat this as opaque satellite data that is associated with
   *              the key. Typically, this would be something internal to the
   *              CQL Engine (for example - tuple identifier of the tuple
   *              that contains the key).
   */
  public void insert(Object newkey, Object val);

  /**
   * Delete an existing record from the index
   * @param oldkey - the key to be deleted - the "actual" type of this will
   *               typically be a domain-specific type
   * @param oldval - the value associated with this key.  The index will
   *                 treat this as opaque satellite data that is associated
   *                 with the key. Typically, this would be something internal 
   *                 to the CQL Engine (for example - tuple identifier of the
   *                 tuple that contains the key).
   */
  public void delete(Object oldkey, Object oldval);


  /**
   * Update an existing record in the index
   * @param oldkey - the key to be deleted
   * @param newkey - the key to be inserted
   * @param oldval - the opaque value associated with the oldkey
   * @param newval - the opaque value associated with the newkey
   */
  public void update(Object oldkey, Object newkey,
                     Object oldval, Object newval);


  // Index Scan Operations
  /**
   * Start an index scan
   * @param args - these correspond (in number and type) to the arguments of
   *               the operation that is being executed through this index scan
   * @param indexCallbackContext - this is the callback context info returned
   *                               as part of the index information returned
   *                               by the IIndexInfoLocator
   */
  public void startScan(Object indexCallbackContext, Object[] args);

  /**
   * @return the satellite data associated with the next matching key.
   *         If no more matches, return null
   */
  public Object getNext();

  /**
   * Releases the index scan
   */
  public void releaseScan();

}



