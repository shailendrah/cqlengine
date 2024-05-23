/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/indexes/IIndexInfo.java /main/1 2009/09/22 06:58:20 udeshmuk Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    Interface corresponding to index information of a specific index type.
    Cartridges providing their own indexing scheme must implement this
    interface.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    09/07/09 - Creation
    anasrini    09/07/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/indexes/IIndexInfo.java /main/1 2009/09/22 06:58:20 udeshmuk Exp $
 *  @author  anasrini
 *  @since   11.1.2
 */

package oracle.cep.extensibility.indexes;

/**
 * Interface corresponding to index information of a specific index type.
 * Cartridges providing their own indexing scheme must implement this
 * interface.
 */

public interface IIndexInfo
{

  /**
   * This method will return the factory for the associated index type. The
   * factory provides APIs to instantiate (and destroy) an index.
   * @return Index Type Factory
   */
  public IIndexTypeFactory getIndexTypeFactory();

  /**
   * @return the callback context to be passed to the index during the scan 
   */
  public Object getIndexCallbackContext();

   /**
    * @return true if and only if the index returns exact results and
    *         no secondary filter / confirmation is required.
    *         Example - a BTree would return "true" for the "<,>,=" operations
    *                   a Spatial RTree would return "false" for the CONTAINS
    *                     operation since a secondary filter is required
    */
  public boolean areResultsExact();
    
}

