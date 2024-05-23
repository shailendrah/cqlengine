/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/service/CEPServerXface.java hopark_cqlsnapshot/2 2016/02/26 10:21:32 hopark Exp $ */

/* Copyright (c) 2007, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/15/15 - add snapshot api
    sbishnoi    08/01/13 - add getter for CEPStats mbean
    sbishnoi    03/01/12 - fix 13774367
    sbishnoi    09/19/10 - XbranchMerge sbishnoi_bug-10068411_ps3 from
                           st_pcbpel_11.1.1.4.0
    sbishnoi    09/01/10 - support input batching for cqlengine
    hopark      05/21/09 - add serverContext
    sbishnoi    03/30/09 - adding an API for jdbc preparedstatement
    parujain    02/16/09 - getviewtypes
    skmishra    02/03/09 - correcting getSchemaName
    skmishra    01/21/09 - adding getQCXML()
    parujain    01/15/09 - support for memstorage
    hopark      01/09/09 - add getReservedKeywords
    skmishra    12/28/08 - adding validateQuery
    hopark      12/02/08 - add getLogLevel
    parujain    11/18/08 - support StatsRuntimeMBean
    hopark      11/17/08 - add setSchema
    hopark      10/09/08 - remove statics
    parujain    09/23/08 - multiple schema
    sbishnoi    09/21/08 - support for schema
    skmishra    08/20/08 - changing package name
    rkomurav    04/18/08 - add explainplan
    mthatte     03/19/08 - jdbc reorg
    skmishra    11/14/07 - 
    mthatte     11/07/07 - adding type as param to initnameSpace
    mthatte     08/28/07 - Add support for ArrayContext
    mthatte     08/20/07 - Adding support for queries on metadata storage
    parujain    05/09/07 - 
    najain      04/26/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/service/CEPServerXface.java hopark_cqlsnapshot/2 2016/02/26 10:21:32 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.service;

import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.descriptors.ArrayContext;
import oracle.cep.logging.ILogLevelManager;
import oracle.cep.metadata.QueryInfo;
import oracle.cep.metadata.ViewInfo;
import oracle.cep.statistics.IStatsIterator;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CEPServerXface extends Remote
{
  public enum StatType {System, Query, Operator, Stream, UserFunction; };

  //Execute statements
  int executeDDL(String cepDDLStr, String schemaName) throws RemoteException;

  int executeDML(TupleValue tuple, String schemaName) throws RemoteException;
  int executeDML(TupleValue tuple, String schemaName, String tableName) throws RemoteException;
  int executeDML(TupleValue tuple, String schemaName, IServerContext serverContext) throws RemoteException;

  // APIs to support input batching
  int executeDML(Iterator<TupleValue> tuple, String schemaName) throws RemoteException;
  int executeDML(Iterator<TupleValue> tuple, String schemaName, String tableName) throws RemoteException;

  int executeDML(Iterator<TupleValue> tuple, String schemaName, IServerContext serverContext) throws RemoteException;  

  //Close the server
  void close() throws RemoteException;
  
  //Describe metadata
  //Generic interface. Supports Namespace.QUERY, Namespace.USERFUNCTION, Namespace.SOURCE
  ArrayContext describeNamespace(String nameSpace, String[] types, String schema) throws RemoteException;
  //Special method for column description
  ArrayContext describeColumns(String catalog, String schemaPattern, String tableNamePattern,
			String columnNamePattern) throws RemoteException;
  //Special method to describe ONE table.
  ArrayContext describeTableByName(String tableName, String schema, boolean isView) throws RemoteException;

  //Does the table need a timestamp from the client? 
  boolean isClientTimeStamped(String tableName, String schema) throws RemoteException;
  
  //Close connection
  void closeConnection(List<Long> ids) throws RemoteException;
  
  //Get XML Plan dump
  String getXMLPlan2() throws RemoteException;
  
  //Get XML representation of DDL for QueryConstructor
  String getQCXML(String queryName, String schema, boolean isView) throws RemoteException;
  
  // Get any StatsIterator
  IStatsIterator getStatsIterator(StatType type) throws RemoteException;
  
  ILogLevelManager getLogLevelManager() throws RemoteException;
  
  /**
   * Return the string having comma-separated values listing all built-in functions
   * (aggregate and single-element functions)
   * @return list of all built-in functions.
   * @throws RemoteException
   */
  String getSystemFunctions() throws RemoteException;
  
  /**
   * Return the string having comma-separated values listing all aggregate
   * built-in functions
   * @return list of all built-in functions.
   * @throws RemoteException
   */
  String getAggrSystemFunctions() throws RemoteException;
  
  /**
   * Return the string having comma-separated values listing all single-element
   * built-in functions (a.k.a scalar functions)
   * @return list of all built-in functions.
   * @throws RemoteException
   */
  String getSingleElementSystemFunctions() throws RemoteException;
  
  String getReservedWords() throws RemoteException;
  
  //This method is used by evs-visualizer
  boolean validateQuery(String schema, String cql) throws RemoteException;
  
  //The following methods are only for evs-tooling in order to reuse the same servers
  String getSchema() throws RemoteException;
  void setSchema(String schemaName) throws RemoteException;
  void dropSchema(String schemaName) throws RemoteException;
  
  /** Returns the information about all views registered with given schema*/
  Map<String, ViewInfo> getViewInfo(String schema) throws RemoteException;    
  
  /** Returns the information about all queries registered with given schema*/
  Map<String, QueryInfo> getQueryInfo(String schema) throws RemoteException;
  
  Set<String> getSourceAttributeNamesForQueryOrView(String cqlQuery, String schema, boolean isView) 
    throws RemoteException;
  
  /**
   * Returns set of all table source names for a query, including any used by referenced views.
   *  
   * @param queryId
   * @param schema
   * @return
   * @throws RemoteException
   */
  Set<String> getQuerySourceNames(String queryId, String schema) throws RemoteException;
  
  IServerContext prepareStatement(String tableName, String schemaName) throws RemoteException;

  // Add query change listener to be notified when queries are added, removed, started, or stopped.
  void addQueryChangeListener(String schemaName, IQueryChgListener notifier);
  
  // Removes previously added query change listener
  void removeQueryChangeListener(String schemaName, IQueryChgListener notifier);

  // Getter for CEPStats MBean
  Object getCEPStatsMBean();
  
  // Get Detailed Execution Statistics of Given Query
  Map<String,Object> getQueryStats(String schema, String queryId);
  
  /*
   * Snapshot related APIs
   */
  void createSnapshot(String schemaName, ObjectOutputStream output, boolean fullSnapshot) throws RemoteException;
  void loadSnapshot(String schemaName, ObjectInputStream input, boolean fullSnapshot) throws RemoteException;
  void startBatch(String schemaName, boolean fullSnapshot) throws RemoteException;
  void endBatch(String schemaName, boolean fullSnapshot) throws RemoteException;
  
  /** Check if scheduler manager is still running*/
  public boolean isRunning();
}

