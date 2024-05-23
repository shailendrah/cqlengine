/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/extensibility/datasource/IArchiver.java /main/2 2012/09/25 06:20:29 udeshmuk Exp $ */

/* Copyright (c) 2011, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/14/12 - add javadoc comments
    udeshmuk    04/06/11 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/extensibility/datasource/IArchiver.java /main/2 2012/09/25 06:20:29 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.extensibility.datasource;

/*
 * The IArchiver interface. Encapsulates Archiver.
 * This interface should be implemented for supplying archiver implementation.
 * Actual persistence of the data, connection to the persistent source and
 * way of executing the query are the details which are specific to archiver.
 * The interface hides CQLEngine from these details.
 */
public interface IArchiver
{
  /**
   * Execute the queries against the archiver instance and fetch back the 
   * results.
   * The queries in the parameter array are executed one after the other
   * and the resultset returned are collectively returned as IArchiverQueryResult
   * instance. The index for accessing individual results starts from 0.
   * @param QueryRequest[] queries : The queries to be executed against the
   *        archiver along with their parameters.
   * @return Object which has one resultSet for each of the queries 
   *         in the parameter array.
   */
  public IArchiverQueryResult execute(QueryRequest[] queries);
  
  /**
   * Close the resultsets in IArchiverQueryResult instance
   */
  public void closeResult(IArchiverQueryResult result);

}
