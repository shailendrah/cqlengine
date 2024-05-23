/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/indexes/IIndexInfoLocator.java /main/2 2009/12/02 02:35:20 alealves Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    Interface for the Index Information Locator.
    Cartridges providing their own indexing scheme must implement this
    interface.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    alealves    11/27/09 - Data cartridge context, default package support
    anasrini    09/07/09 - Creation
    anasrini    09/07/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/indexes/IIndexInfoLocator.java /main/2 2009/12/02 02:35:20 alealves Exp $
 *  @author  anasrini
 *  @since   11.1.2
 */

package oracle.cep.extensibility.indexes;

import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.functions.IUserFunctionMetadata;

/**
 * Interface for the Index Information Locator. 
 * Cartridges providing their own indexing scheme must implement this
 * interface.
 */

public interface IIndexInfoLocator 
{

  /**
   * Returns an array of index information. These represent all the index
   * types that support a collection version of the specified operation, 
   * with the paramPosition argument specifying which input is to be
   * treated as the collection.
   * 
   * @param operation the operation in question
   * @param paramPosition the parameter position that would be passed in as
   *                      as a collection or batch
   * @param context cartridge context associated to this extensible object                     
   * @return information related to index types that support a
   *         collection version of the specified operation
   */
  public IIndexInfo[] getIndexInfo(IUserFunctionMetadata operation,
                                   int  paramPosition, 
                                   ICartridgeContext context);

}
