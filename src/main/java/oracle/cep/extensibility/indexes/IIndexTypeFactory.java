/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/indexes/IIndexTypeFactory.java /main/1 2009/09/22 06:58:20 udeshmuk Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    Interface for the index type factory

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    09/07/09 - Creation
    anasrini    09/07/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/indexes/IIndexTypeFactory.java /main/1 2009/09/22 06:58:20 udeshmuk Exp $
 *  @author  anasrini
 *  @since   11.1.2
 */

package oracle.cep.extensibility.indexes;

/**
 * Interface for the index type factory
 * Cartridges providing their own indexing scheme must implement this
 * interface.
 */

public interface IIndexTypeFactory
{

  // Index Definition Operations
  /**
   * Create a new instance of this index type
   * @param args - optional index instance specific parameters
   * @return Returns an index of this index type 
   */
  public IIndex create(Object[] args);

  /**
   *  Destroy / reclaim the specified index instance
   */
  public void drop(IIndex index);

}
