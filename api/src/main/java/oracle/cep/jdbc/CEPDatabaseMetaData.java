/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/jdbc/CEPDatabaseMetaData.java /main/13 2014/01/28 20:39:52 ybedekar Exp $ */

/* Copyright (c) 2007, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 parujain    01/15/09 - support of memstorage
 hopark      01/09/09 - fix getSQLKeywords
 hopark      10/09/08 - remove statics
 parujain    09/23/08 - multiple schema
 sbishnoi    09/23/08 - incorporating new constant name
 skmishra    08/04/08 - referencing CEPBaseConnection
 skmishra    07/30/08 - adding reference to Connection
 skmishra    07/25/08 - replacing CEPConnection with Connection
 mthatte     03/19/08 - adding isClientTS and isSysTS
 mthatte     03/13/08 - adding XmlType to getTypes()
 udeshmuk    01/30/08 - support for double data type.
 mthatte     11/02/07 - adding methods for squirrel
 mthatte     10/09/07 - adding ORDINAL_POSITION to getColumns()
 mthatte     08/15/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/jdbc/CEPDatabaseMetaData.java /main/13 2014/01/28 20:39:52 ybedekar Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.jdbc;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;

import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.descriptors.ArrayContext;
import oracle.cep.descriptors.ProcedureMetadataDescriptor;
import oracle.cep.descriptors.SQLTypeMetadataDescriptor;
import oracle.cep.descriptors.SchemaMetadataDescriptor;
import oracle.cep.descriptors.TableCatalogMetadataDescriptor;
import oracle.cep.descriptors.TableMetadataDescriptor;
import oracle.cep.descriptors.TableTypeMetadataDescriptor;
import oracle.cep.descriptors.UDTMetadataDescriptor;
import oracle.cep.logging.LogUtil;
import oracle.cep.metadata.cache.NameSpace;
import oracle.cep.service.CEPServerXface;

public class CEPDatabaseMetaData implements DatabaseMetaData
{
  //this connection binds this metadata object to a server
  private CEPServerXface server;
  private CEPBaseConnection connection;
  private String serverURL;
  
  public CEPDatabaseMetaData(CEPBaseConnection conn, CEPServerXface server, String url)
  {
    this.server = server;
    this.connection = conn;
    this.serverURL = url;
  }

  public boolean allProceduresAreCallable() throws SQLException
  {
    return true;
  }

  public boolean allTablesAreSelectable() throws SQLException
  {
    return false;
  }

  public boolean dataDefinitionCausesTransactionCommit() throws SQLException
  {
    return false;
  }

  public boolean dataDefinitionIgnoredInTransactions() throws SQLException
  {
    return false;
  }

  public boolean deletesAreDetected(int type) throws SQLException
  {
    return false;
  }

  public boolean doesMaxRowSizeIncludeBlobs() throws SQLException
  {
    return false;
  }

  public ResultSet getAttributes(String catalog, String schemaPattern,
      String typeNamePattern, String attributeNamePattern) throws SQLException
  {
    return new CEPResultSet(-1);
  }

  public ResultSet getBestRowIdentifier(String catalog, String schema,
      String table, int scope, boolean nullable) throws SQLException
  {
    return new CEPResultSet(-1);
  }

  public String getCatalogSeparator() throws SQLException
  {
    return ":";
  }

  public String getCatalogTerm() throws SQLException
  {
    return "";
  }

  public ResultSet getCatalogs() throws SQLException
  {
    // We do not support Catalogs. Hence return an "empty" ResultSet
    ArrayContext ctx = new ArrayContext();
    ctx.add(new TableCatalogMetadataDescriptor("Oracle-CEP"));
    return new CEPResultSet(ctx);
  }

  public ResultSet getColumnPrivileges(String catalog, String schema,
      String table, String columnNamePattern) throws SQLException
  {
    return new CEPResultSet(-1);
  }

  public ResultSet getColumns(String catalog, String schemaPattern,
      String tableNamePattern, String columnNamePattern) throws SQLException
  {
    ArrayContext ctx = null;
    try
    {
      ctx = server.describeColumns(catalog, schemaPattern, tableNamePattern,
          columnNamePattern);
    } catch (RemoteException e)
    {
      LogUtil.logStackTrace(e);
      throw new SQLException("getColumns broke");
    }
    return new CEPResultSet(ctx);
  }

  public Connection getConnection() throws SQLException
  {
    return connection;
  }

  public ResultSet getCrossReference(String primaryCatalog,
      String primarySchema, String primaryTable, String foreignCatalog,
      String foreignSchema, String foreignTable) throws SQLException
  {
    return new CEPResultSet(-1);
  }

  public int getDatabaseMajorVersion() throws SQLException
  {
    return 10;
  }

  public int getDatabaseMinorVersion() throws SQLException
  {
    return 1;
  }

  public String getDatabaseProductName() throws SQLException
  {
    return "Oracle CEP";
  }

  public String getDatabaseProductVersion() throws SQLException
  {
    return "1.0 Beta";
  }

  public int getDefaultTransactionIsolation() throws SQLException
  {
    return 0;
  }

  public int getDriverMajorVersion()
  {
    return 1;
  }

  public int getDriverMinorVersion()
  {
    return 1;
  }

  public String getDriverName() throws SQLException
  {
    return "CEPDriver";
  }

  public String getDriverVersion() throws SQLException
  {
    return "1.1";
  }

  public ResultSet getExportedKeys(String catalog, String schema, String table)
      throws SQLException
  {
    return new CEPResultSet(-1);
  }

  public String getExtraNameCharacters() throws SQLException
  {
    return "";
  }

  public String getIdentifierQuoteString() throws SQLException
  {
    return " "; // Indicates not supported
  }

  public ResultSet getImportedKeys(String catalog, String schema, String table)
      throws SQLException
  {
    return new CEPResultSet(-1);
  }

  public ResultSet getIndexInfo(String catalog, String schema, String table,
      boolean unique, boolean approximate) throws SQLException
  {
    return new CEPResultSet(-1);
  }

  public int getJDBCMajorVersion() throws SQLException
  {
    return 1;
  }

  public int getJDBCMinorVersion() throws SQLException
  {
    return 1;
  }

  public int getMaxBinaryLiteralLength() throws SQLException
  {
    return 50;
  }

  public int getMaxCatalogNameLength() throws SQLException
  {
    return 20;
  }

  public int getMaxCharLiteralLength() throws SQLException
  {
    return 50;
  }

  public int getMaxColumnNameLength() throws SQLException
  {
    return 30;
  }

  public int getMaxColumnsInGroupBy() throws SQLException
  {
    return 5;
  }

  public int getMaxColumnsInIndex() throws SQLException
  {
    return 0;
  }

  public int getMaxColumnsInOrderBy() throws SQLException
  {
    return 5;
  }

  public int getMaxColumnsInSelect() throws SQLException
  {
    return 0;
  }

  public int getMaxColumnsInTable() throws SQLException
  {
    return 0;
  }

  public int getMaxConnections() throws SQLException
  {
    return 0;
  }

  public int getMaxCursorNameLength() throws SQLException
  {
    return 0;
  }

  public int getMaxIndexLength() throws SQLException
  {
    return 0;
  }

  public int getMaxProcedureNameLength() throws SQLException
  {
    return 0;
  }

  public int getMaxRowSize() throws SQLException
  {
    return 0;
  }

  public int getMaxSchemaNameLength() throws SQLException
  {
    return 0;
  }

  public int getMaxStatementLength() throws SQLException
  {
    return 0;
  }

  public int getMaxStatements() throws SQLException
  {
    return 0;
  }

  public int getMaxTableNameLength() throws SQLException
  {
    return 0;
  }

  public int getMaxTablesInSelect() throws SQLException
  {
    return 0;
  }

  public int getMaxUserNameLength() throws SQLException
  {
    return 0;
  }

  public String getNumericFunctions() throws SQLException
  {
    return "abs,sin,cos,tan,ceil";
  }

  public ResultSet getPrimaryKeys(String catalog, String schema, String table)
      throws SQLException
  {
    return new CEPResultSet(-1);
  }

  public ResultSet getProcedureColumns(String catalog, String schemaPattern,
      String procedureNamePattern, String columnNamePattern)
      throws SQLException
  {
    return new CEPResultSet(-1);
    // throw new
    // UnsupportedOperationException("CEPDatabaseMetadata.getProcedureColumns()
    // unsupported");
  }

  public String getProcedureTerm() throws SQLException
  {
    return "User defined functions";
  }

  public ResultSet getProcedures(String catalog, String schemaPattern,
      String procedureNamePattern) throws SQLException
  {
    ArrayContext ctx = null;
    try
    {
      ctx = server.describeNamespace(NameSpace.USERFUNCTION.toString(),
            null,schemaPattern);
    } catch (RemoteException e)
    {
      LogUtil.logStackTrace(e);
    }
    CEPResultSet result = new CEPResultSet(ctx); // add sanity checks....
    return result;
  }

  public int getResultSetHoldability() throws SQLException
  {
    return 0;
  }

  public String getSQLKeywords() throws SQLException
  {
    try
    {
      return server.getReservedWords();
    }
    catch (RemoteException e)
    {
      LogUtil.logStackTrace(e);
    }
    return null;
  }

  public int getSQLStateType() throws SQLException
  {
    return sqlStateSQL99;
  }

  public String getSchemaTerm() throws SQLException
  {
    return "user";
  }

  public ResultSet getSchemas() throws SQLException
  {
    ArrayContext ctx = new ArrayContext();
    ctx.add(new SchemaMetadataDescriptor(Constants.DEFAULT_SCHEMA, 
                                         Constants.CEP_CATALOG));
    return new CEPResultSet(ctx);
  }

  public String getSearchStringEscape() throws SQLException
  {
    return "%";
  }

  public String getStringFunctions() throws SQLException
  {
    return "length, equals, concat";
  }

  public ResultSet getSuperTables(String catalog, String schemaPattern,
      String tableNamePattern) throws SQLException
  {
    return new CEPResultSet(-1);
  }

  public ResultSet getSuperTypes(String catalog, String schemaPattern,
      String typeNamePattern) throws SQLException
  {
    return new CEPResultSet(-1);
  }

  public String getSystemFunctions() throws SQLException
  {
    try
    {
      return server.getSystemFunctions();
    }
    catch (RemoteException e)
    {
      LogUtil.logStackTrace(e);
    }
    return null;
  }

  public ResultSet getTablePrivileges(String catalog, String schemaPattern,
      String tableNamePattern) throws SQLException
  {
    return new CEPResultSet(-1);
  }

  public ResultSet getTableTypes() throws SQLException
  {
    //Returns a list of "types" on the server side.
    //Query is masquerading as a TableType for the Squirrel UI
    
    ArrayContext ctx = new ArrayContext();
    ctx.add(new TableTypeMetadataDescriptor("STREAM"));
    ctx.add(new TableTypeMetadataDescriptor("RELATION"));
    ctx.add(new TableTypeMetadataDescriptor("VIEW"));
    ctx.add(new TableTypeMetadataDescriptor("QUERY"));
    return new CEPResultSet(ctx);
  }

  /**
   * 
   * @author Mohit Thatte
   * 
   */
  public ResultSet getTables(String catalog, String schemaPattern,
      String tableNamePattern, String[] types) throws SQLException
  {
    String type = "null";
    StringBuilder sb = new StringBuilder();
    if(types!=null) {
      for(String _type:types)
        sb.append(_type+" "); 
      type = sb.toString();
    }
    
    CEPResultSet result = new CEPResultSet(-1);
    ArrayContext ctx = null;
    try
    {
      
      // If hacked up call to get queries
      if (type.indexOf("QUERY") != -1)
        ctx = server.describeNamespace(NameSpace.QUERY.toString(), 
            types, schemaPattern);
      // If get all tables
      else if (tableNamePattern == null || tableNamePattern.equals("%")
          || tableNamePattern.equals(""))
        ctx = server.describeNamespace(NameSpace.SOURCE.toString(), 
            types, schemaPattern);
      // If get one table
      else if (!tableNamePattern.contains("%"))
      {
        boolean isView = false;
        if (types[0].equalsIgnoreCase("VIEW"))
          isView = true;
        ctx = server.describeTableByName(tableNamePattern, 
            schemaPattern, isView);
        if (ctx == null)
          return new CEPResultSet(-1, connection, new TableMetadataDescriptor());
        else
          return new CEPResultSet(ctx);
      } else
        return new CEPResultSet(-1);
    } catch (RemoteException e)
    {
      e.printStackTrace();
    }
    result = new CEPResultSet(ctx); // add
                                                                        // sanity
                                                                        // checks....
    return result;
  }

  public String getTimeDateFunctions() throws SQLException
  {
    return "now";
  }

  public ResultSet getTypeInfo() throws SQLException
  {
    ArrayContext ctx = new ArrayContext();
    
    //Get a list of public types from enum Datatype
    for(Datatype dt:Datatype.getPublicTypes())
      ctx.add(new SQLTypeMetadataDescriptor(dt));
    return new CEPResultSet(ctx);
  }

  public ResultSet getUDTs(String catalog, String schemaPattern,
      String typeNamePattern, int[] types) throws SQLException
  {
    return new CEPResultSet(-1, connection, new UDTMetadataDescriptor());
  }

  /**
   * @author mthatte
   * @return Returns the url where the CEP-JDBC server is listening.
   */

  public String getURL() throws SQLException
  {
    return serverURL;
  }

  public String getUserName() throws SQLException
  {
    return connection.getUser();
  }
  
  public ResultSet getVersionColumns(String catalog, String schema, String table)
      throws SQLException
  {
    return new CEPResultSet(-1);
  }

  public boolean insertsAreDetected(int type) throws SQLException
  {
    return true;
  }

  public boolean isCatalogAtStart() throws SQLException
  {
    return false;
  }
  
  public boolean isClientTimeStamped(String tableName, String schema) 
      throws SQLException
  {
    try
    {
      return server.isClientTimeStamped(tableName, schema);
    }catch(RemoteException r)
    {
      throw new SQLException("Server error.");
    }
  }
  
  public boolean isReadOnly() throws SQLException
  {
    return false;
  }

  public boolean locatorsUpdateCopy() throws SQLException
  {
    return false;
  }

  public boolean nullPlusNonNullIsNull() throws SQLException
  {
    return false;
  }

  public boolean nullsAreSortedAtEnd() throws SQLException
  {
    return false;
  }

  public boolean nullsAreSortedAtStart() throws SQLException
  {
    return false;
  }

  public boolean nullsAreSortedHigh() throws SQLException
  {
    return false;
  }

  public boolean nullsAreSortedLow() throws SQLException
  {
    return false;
  }

  public boolean othersDeletesAreVisible(int type) throws SQLException
  {
    return false;
  }

  public boolean othersInsertsAreVisible(int type) throws SQLException
  {
    return false;
  }

  public boolean othersUpdatesAreVisible(int type) throws SQLException
  {
    return false;
  }

  public boolean ownDeletesAreVisible(int type) throws SQLException
  {
    return false;
  }

  public boolean ownInsertsAreVisible(int type) throws SQLException
  {
    return false;
  }

  public boolean ownUpdatesAreVisible(int type) throws SQLException
  {
    return false;
  }
  

  public boolean storesLowerCaseIdentifiers() throws SQLException
  {
    return false;
  }

  public boolean storesLowerCaseQuotedIdentifiers() throws SQLException
  {
    return false;
  }

  public boolean storesMixedCaseIdentifiers() throws SQLException
  {
    return false;
  }

  public boolean storesMixedCaseQuotedIdentifiers() throws SQLException
  {
    return false;
  }

  public boolean storesUpperCaseIdentifiers() throws SQLException
  {
    return false;
  }

  public boolean storesUpperCaseQuotedIdentifiers() throws SQLException
  {
    return false;
  }

  public boolean supportsANSI92EntryLevelSQL() throws SQLException
  {
    return false;
  }

  public boolean supportsANSI92FullSQL() throws SQLException
  {
    return false;
  }

  public boolean supportsANSI92IntermediateSQL() throws SQLException
  {
    return false;
  }

  public boolean supportsAlterTableWithAddColumn() throws SQLException
  {
    return false;
  }

  public boolean supportsAlterTableWithDropColumn() throws SQLException
  {
    return false;
  }

  public boolean supportsBatchUpdates() throws SQLException
  {
    return false;
  }

  public boolean supportsCatalogsInDataManipulation() throws SQLException
  {
    return false;
  }

  public boolean supportsCatalogsInIndexDefinitions() throws SQLException
  {
    return false;
  }

  public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException
  {
    return false;
  }

  public boolean supportsCatalogsInProcedureCalls() throws SQLException
  {
    return false;
  }

  public boolean supportsCatalogsInTableDefinitions() throws SQLException
  {
    return false;
  }

  public boolean supportsColumnAliasing() throws SQLException
  {
    return false;
  }

  public boolean supportsConvert() throws SQLException
  {
    return false;
  }

  public boolean supportsConvert(int fromType, int toType) throws SQLException
  {
    return false;
  }

  public boolean supportsCoreSQLGrammar() throws SQLException
  {
    return false;
  }

  public boolean supportsCorrelatedSubqueries() throws SQLException
  {
    return false;
  }

  public boolean supportsDataDefinitionAndDataManipulationTransactions()
      throws SQLException
  {
    return false;
  }

  public boolean supportsDataManipulationTransactionsOnly() throws SQLException
  {
    return false;
  }

  public boolean supportsDifferentTableCorrelationNames() throws SQLException
  {
    return false;
  }

  public boolean supportsExpressionsInOrderBy() throws SQLException
  {
    return false;
  }

  public boolean supportsExtendedSQLGrammar() throws SQLException
  {
    return false;
  }

  public boolean supportsFullOuterJoins() throws SQLException
  {
    return false;
  }

  public boolean supportsGetGeneratedKeys() throws SQLException
  {
    return false;
  }

  public boolean supportsGroupBy() throws SQLException
  {
    return false;
  }

  public boolean supportsGroupByBeyondSelect() throws SQLException
  {
    return false;
  }

  public boolean supportsGroupByUnrelated() throws SQLException
  {
    return false;
  }

  public boolean supportsIntegrityEnhancementFacility() throws SQLException
  {
    return false;
  }

  public boolean supportsLikeEscapeClause() throws SQLException
  {
    return false;
  }

  public boolean supportsLimitedOuterJoins() throws SQLException
  {
    return false;
  }

  public boolean supportsMinimumSQLGrammar() throws SQLException
  {
    return false;
  }

  public boolean supportsMixedCaseIdentifiers() throws SQLException
  {
    return false;
  }

  public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException
  {
    return false;
  }

  public boolean supportsMultipleOpenResults() throws SQLException
  {
    return false;
  }

  public boolean supportsMultipleResultSets() throws SQLException
  {
    return false;
  }

  public boolean supportsMultipleTransactions() throws SQLException
  {
    return false;
  }

  public boolean supportsNamedParameters() throws SQLException
  {
    return false;
  }

  public boolean supportsNonNullableColumns() throws SQLException
  {
    return false;
  }

  public boolean supportsOpenCursorsAcrossCommit() throws SQLException
  {
    return false;
  }

  public boolean supportsOpenCursorsAcrossRollback() throws SQLException
  {
    return false;
  }

  public boolean supportsOpenStatementsAcrossCommit() throws SQLException
  {
    return false;
  }

  public boolean supportsOpenStatementsAcrossRollback() throws SQLException
  {
    return false;
  }

  public boolean supportsOrderByUnrelated() throws SQLException
  {
    return false;
  }

  public boolean supportsOuterJoins() throws SQLException
  {
    return true;
  }

  public boolean supportsPositionedDelete() throws SQLException
  {
    return false;
  }

  public boolean supportsPositionedUpdate() throws SQLException
  {
    return false;
  }

  public boolean supportsResultSetConcurrency(int type, int concurrency)
      throws SQLException
  {
    return false;
  }

  public boolean supportsResultSetHoldability(int holdability)
      throws SQLException
  {
    return false;
  }

  public boolean supportsResultSetType(int type) throws SQLException
  {
    return false;
  }

  public boolean supportsSavepoints() throws SQLException
  {
    return false;
  }

  public boolean supportsSchemasInDataManipulation() throws SQLException
  {
    return false;
  }

  public boolean supportsSchemasInIndexDefinitions() throws SQLException
  {
    return false;
  }

  public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException
  {
    return false;
  }

  public boolean supportsSchemasInProcedureCalls() throws SQLException
  {
    return false;
  }

  public boolean supportsSchemasInTableDefinitions() throws SQLException
  {
    return false;
  }

  public boolean supportsSelectForUpdate() throws SQLException
  {
    return false;
  }

  public boolean supportsStatementPooling() throws SQLException
  {
    return false;
  }

  public boolean supportsStoredProcedures() throws SQLException
  {
    return true;
  }

  public boolean supportsSubqueriesInComparisons() throws SQLException
  {
    return false;
  }

  public boolean supportsSubqueriesInExists() throws SQLException
  {
    return false;
  }

  public boolean supportsSubqueriesInIns() throws SQLException
  {
    return false;
  }

  public boolean supportsSubqueriesInQuantifieds() throws SQLException
  {
    return false;
  }

  public boolean supportsTableCorrelationNames() throws SQLException
  {
    return false;
  }

  public boolean supportsTransactionIsolationLevel(int level)
      throws SQLException
  {
    return false;
  }

  public boolean supportsTransactions() throws SQLException
  {
    return false;
  }

  public boolean supportsUnion() throws SQLException
  {
    return false;
  }

  public boolean supportsUnionAll() throws SQLException
  {
    return true;
  }

  public boolean updatesAreDetected(int type) throws SQLException
  {
    return false;
  }

  public boolean usesLocalFilePerTable() throws SQLException
  {
    return false;
  }

  public boolean usesLocalFiles() throws SQLException
  {
    return false;
  }

  public boolean autoCommitFailureClosesAllResultSets() throws SQLException
  {
    // TODO Auto-generated method stub
    return false;
  }

  public ResultSet getClientInfoProperties() throws SQLException
  {
    // TODO Auto-generated method stub
    return null;
  }

  public ResultSet getFunctionColumns(String arg0, String arg1, String arg2,
      String arg3) throws SQLException
  {
    // TODO Auto-generated method stub
    return null;
  }

  public ResultSet getFunctions(String arg0, String arg1, String arg2)
      throws SQLException
  {
    // TODO Auto-generated method stub
    return null;
  }

  public RowIdLifetime getRowIdLifetime() throws SQLException
  {
    // TODO Auto-generated method stub
    return null;
  }

  public ResultSet getSchemas(String arg0, String arg1) throws SQLException
  {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException
  {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isWrapperFor(Class<?> arg0) throws SQLException
  {
    // TODO Auto-generated method stub
    return false;
  }

  public <T> T unwrap(Class<T> arg0) throws SQLException
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException
  {
    throw new UnsupportedOperationException("CEPDatabaseMetaData.getPseudoColumns not supported yet.");
  }

  @Override
  public boolean generatedKeyAlwaysReturned() throws SQLException
  {
    throw new UnsupportedOperationException("CEPDatabaseMetaData.generatedKeyAlwaysReturned not supported yet.");
  }

}
