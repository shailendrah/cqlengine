/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/service/IQueryChgListener.java /main/1 2012/08/01 21:02:00 alealves Exp $ */

/* Copyright (c) 2007, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/07/08 - use execContext to remove statics
    hopark      06/27/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/service/IQueryChgListener.java /main/1 2012/08/01 21:02:00 alealves Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.service;

/**
 * Notifier is informed of query changes, such as:
 * <ul>
 *  <li>query is added
 *  <li>query is started
 *  <li>query is stopped
 *  <li>query is about to be dropped. 
 *  <li>query is dropped
 * </ul>
 * <p>
 * 
 * @author alealves
 * @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/service/IQueryChgListener.java /main/1 2012/08/01 21:02:00 alealves Exp $
 *
 */
public interface IQueryChgListener
{
  /**
   * Query <code>queryId</code> has been added to associated CQL server.
   * 
   * @param queryName query user-provider name
   * @param queryId query identifier
   * @param schemaName schema name
   * @param context internal context
   */
  void onQueryAdded(String queryName, int queryId, String schemaName, Object context);
  
  /**
   * Query <code>queryId</code> has been started in associated CQL server.
   * 
   * @param queryName query user-provider name
   * @param queryId query identifier
   * @param schemaName schema name
   * @param context internal context
   */
  void onQueryStarted(String queryName, int queryId, String schemaName, Object context);
  
  /**
   * Query <code>queryId</code> has been stopped in associated CQL server.
   * 
   * @param queryName query user-provider name
   * @param queryId query identifier
   * @param schemaName schema name
   * @param context internal context
   */
  void onQueryStopped(String queryName, int qryId, String schemaName, Object execContext);
  
  /**
   * Query <code>queryId</code> is about to be dropped from associated CQL server.
   *  
   * @param queryName query user-provider name
   * @param queryId query identifier
   * @param schemaName schema name
   * @param context internal context
   */
  void onBeforeQueryDrop(String queryName, int queryId, String schemaName, Object context);
  
  /**
   * Query <code>queryId</code> has been dropped from associated CQL server.
   * 
   * @param queryName query user-provider name
   * @param queryId query identifier
   * @param schemaName schema name
   * @param context internal context
   */
  void onAfterQueryDrop(String queryName, int queryId, String schemaName, Object context);
}

