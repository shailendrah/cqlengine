/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/jdbc/CEPStatement.java /main/13 2014/01/28 20:39:52 ybedekar Exp $ */

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
 hopark      12/28/09 - set cause
 sborah      07/20/09 - support for bigdecimal
 hopark      02/02/09 - objtype support
 hopark      11/28/08 - use CEPDateFormat
 hopark      11/03/08 - fix schema
 hopark      10/23/08 - use serviceName for schema
 hopark      10/09/08 - remove statics
 skmishra    10/14/08 - schema support
 sbishnoi    09/21/08 - support for schema
 hopark      09/13/08 - parser error handling
 skmishra    07/25/08 - replacing CEPConnection with Connection
 mthatte     04/22/08 - heartbeat bug
 rkomurav    04/20/08 - support explain plan
 mthatte     03/14/08 - jdbc re-org
 parujain    02/12/08 - no severe msg to customer
 udeshmuk    02/01/08 - support for double data type.
 udeshmuk    01/17/08 - change in the data type of time field in TupleValue.
 udeshmuk    01/16/08 - handle null in the input.
 skmishra    10/29/07 - 
 mthatte     09/13/07 - Adding methods to make compliant with Java 6
 mthatte     09/04/07 - adding heartbeat support
 hopark      07/13/07 - dump stack trace on exception
 sbishnoi    05/22/07 - support for insert without binds
 parujain    05/09/07 - 
 najain      04/23/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/jdbc/CEPStatement.java /main/13 2014/01/28 20:39:52 ybedekar Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.jdbc;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import oracle.cep.common.CEPDateFormat;
import oracle.cep.common.Datatype;
import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.BigDecimalAttributeValue;
import oracle.cep.dataStructures.external.BigintAttributeValue;
import oracle.cep.dataStructures.external.BooleanAttributeValue;
import oracle.cep.dataStructures.external.ByteAttributeValue;
import oracle.cep.dataStructures.external.CharAttributeValue;
import oracle.cep.dataStructures.external.DoubleAttributeValue;
import oracle.cep.dataStructures.external.FloatAttributeValue;
import oracle.cep.dataStructures.external.IntAttributeValue;
import oracle.cep.dataStructures.external.ObjAttributeValue;
import oracle.cep.dataStructures.external.TupleKind;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.external.XmltypeAttributeValue;
import oracle.cep.descriptors.ArrayContext;
import oracle.cep.descriptors.ColumnMetadataDescriptor;
import oracle.cep.descriptors.MetadataDescriptor;
import oracle.cep.descriptors.SelectResultMetadataDescriptor;
import oracle.cep.jdbc.parser.CEPExplainPlanNode;
import oracle.cep.jdbc.parser.CEPInsertNode;
import oracle.cep.jdbc.parser.CEPParseTreeNode;
import oracle.cep.jdbc.parser.CEPSFWQueryNode;
import oracle.cep.jdbc.parser.Parser;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.parser.CEPConstExprNode;
import oracle.cep.parser.CEPNullConstExprNode;
import oracle.cep.service.CEPServerXface;

/**
 * This is CEP's implementation of the jdbc Statement class. We only allow DDLs
 * and DMLs to be processed. Note that DDLs can be in the form of registering a
 * query. There is a dummy SELECT implemented in order to always return an empty
 * ResultSet with associated ResultSetMetadata.
 * 
 * @author najain
 */

public class CEPStatement implements Statement
{
  Parser                     parser;

  CEPBaseConnection          conn;
  CEPServerXface             server;

  TupleValue                 tuple;
  CEPDateFormat              df     = CEPDateFormat.getInstance();

  CEPParseTreeNode           node   = null;

  AttributeValue[]           attrval;
  boolean[]                  isSet;

  String                     ddl;
  boolean                    isddl;

  boolean                    closed;

  ResultSet                  result = null;

  public CEPStatement()
  {
    this.parser = new Parser();
    closed = false;
  }

  public CEPStatement(CEPBaseConnection conn, CEPServerXface serv)
  {
    this.server = serv;
    this.conn = conn;
    this.parser = new Parser();
    closed = false;
  }

  public void addBatch(String sql) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPStatement:addBatch(String) unsupported");
  }

  public void cancel() throws SQLException
  {
    return;
  }

  public void clearBatch() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPStatement:clearBatch() unsupported");
  }

  public void clearWarnings() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPStatement:clearWarnings() unsupported");
  }

  public void close() throws SQLException
  {
    closed = true;
  }

  public boolean execute(String sql) throws SQLException
  {
    executeUpdate(sql);
    return false;
  }

  public boolean execute(String sql, int autoGeneratedKeys) throws SQLException
  {
    return (true);
    /*
     * throw new UnsupportedOperationException( "CEPStatement:execute(String,
     * int) unsupported");
     */
  }

  public boolean execute(String sql, int[] columnIndexes) throws SQLException
  {
    return (true);
    /*
     * throw new UnsupportedOperationException( "CEPStatement:execute(String,
     * int[]) unsupported");
     */
  }

  public boolean execute(String sql, String[] columnNames) throws SQLException
  {
    return (true);
    /*
     * throw new UnsupportedOperationException( "CEPStatement:execute(String,
     * String[]) unsupported");
     */
  }

  public int[] executeBatch() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPStatement:executeBatch() unsupported");
  }

  public ResultSet executeQuery(String sql) throws SQLException
  {
    String reln = null;
    ResultSet rs = null;
    try
    {
      node = parser.parseCommand(sql);

      if (node instanceof CEPExplainPlanNode)
      {
        String plan = server.getXMLPlan2();
        ColumnMetadataDescriptor cmd = new ColumnMetadataDescriptor(plan,
            "Plan", Datatype.INT.getSqlType(), Datatype.INT.getLength(),
            Datatype.INT.toString());
        ArrayContext ctx = new ArrayContext();
        ctx.add(cmd);
        return new CEPResultSet(ctx);
      }

      else if (!(node instanceof CEPSFWQueryNode))
        throw new SQLException("Not a select");
      // Cast to a select node
      CEPSFWQueryNode selectNode = (CEPSFWQueryNode) node;

      // Find project columns ie SELECT c1,c2...
      List<String> cols = selectNode.getProjectList();

      // Find the tablename ie SELECT c1,c2 FROM T....
      reln = selectNode.getFromClauseRelations();

      // Get the columns of this table
      DatabaseMetaData dbm = conn.getMetaData();
      String schemaName = conn.getSchemaName();
      rs = dbm.getColumns(null, schemaName, reln, null);

      if (rs != null)
      {
        SelectResultMetadataDescriptor smd = new SelectResultMetadataDescriptor();
        smd.addColumn(reln, "Timestamp", Datatype.TIMESTAMP.getSqlType(),
            Datatype.TIMESTAMP.getLength());
        while (rs.next())
        {
          String colName = rs.getString("COLUMN_NAME");
          for (String col : cols)
          {
            if (colName.equals(col))
            {
              String tableName = rs.getString("TABLE_NAME");
              int type = rs.getInt("DATA_TYPE");
              int attrLen = rs.getInt("COLUMN_SIZE");
              smd.addColumn(tableName, colName, type, attrLen);
            }
          }
        }

        long id = -1;
        MetadataDescriptor md = smd;
        return new CEPResultSet(id, conn, md);
      }

    }

    catch (Exception e)
    {
      e.printStackTrace();
    }

    return null;
  }

  public int executeUpdate(String sql) throws SQLException
  {
    try
    {
      node = parser.parseCommand(sql);
      if (!(node instanceof CEPInsertNode))
        throw new SQLException();
      tuple = new TupleValue();
      isddl = false; // we assume the command to be a DML
      ddl = null;
      tuple.setObjectName(((CEPInsertNode) node).getTableName());
      return evaluateAndSend();
    } catch (SQLException sqe)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, sqe);
      return -1;
    } catch (Exception e)
    {
      // LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      // Not an insert statement ... try it as a DDL
      isddl = true;
      ddl = sql;
      tuple = null;
      return evaluateAndSend();
    }
  }

  /*
   * This function first checks whether the command is a DDl or DML if it is a
   * DDL then executeDDL function is called that handles all DDL's if it is a
   * DML(an insert without binds) then a tuple is formed and passed to the
   * executeDML function
   */
  public int evaluateAndSend() throws SQLException
  {
    if (isddl)
    {
      assert tuple == null;
      try
      {
        CEPServerXface server = conn.getCEPServer();
        return server.executeDDL(ddl, conn.getSchemaName());
      } catch (RemoteException e)
      {
        SQLException se = new SQLException();
        se.initCause(e.getCause());
        throw se;
      }
    } 
    else
    {

      assert ddl == null;
      try
      {
        assert node instanceof CEPInsertNode;
        CEPInsertNode insertNode = (CEPInsertNode) node;
        LinkedList<CEPConstExprNode> tupleAttr = insertNode.getAttrList();

        // If it is a heartbeat, set time, set boolean flags and we are done.
        if (insertNode.isHeartbeat())
        {
          CEPConstExprNode timeVal = tupleAttr.removeFirst();
          long time = CEPInsertNode.getTime(timeVal);
          tuple.setBHeartBeat(true);
          tuple.setTime(time);
          tuple.setKind(TupleKind.HEARTBEAT);
          isSet = new boolean[1];
          isSet[0] = true;
        }

        // else it is an insert
        else
        {
          tuple.setKind(TupleKind.PLUS);
          tuple.setBHeartBeat(false);
          setAttributes(tupleAttr);
          tuple.setAttrs(attrval);
        }
      } catch (Exception e)
      {
        throw new SQLException(e);
      }
      try
      {
        for (boolean setField : isSet)
        {
          if (!setField)
            throw new SQLException("All fields not set");
        }
        return server.executeDML(tuple, conn.getSchemaName());
      } catch (RemoteException e)
      {
        throw new SQLException(e);
      }
    }
  }

  private void createAttrVal(String[] args)
  {
    int colCount = 0;
    attrval = new AttributeValue[args.length];
    isSet = new boolean[args.length];
    for (String colType : args)
    {
      for (Datatype dt : Datatype.getPublicTypes())
      {
        if (colType.equalsIgnoreCase(dt.toString()))
        {

          String className = dt.getAttrValClass();

          try
          {
            attrval[colCount++] = (AttributeValue) Class.forName(className)
                .newInstance();
          } catch (InstantiationException e)
          {
            e.printStackTrace();
          } catch (IllegalAccessException e)
          {
            e.printStackTrace();
          } catch (ClassNotFoundException e)
          {
            e.printStackTrace();
          }
        }
      }
    }

  }

  /*
   * This function populates the AttributeValue type array with the attributes
   * value in the insert without binds command eg. insert into S
   * values(attribute1,attribute2,.....) attributes can be of types: integer,
   * float, double, timestamp, char the AttributeValue type array is then used
   * to form a tuple
   */
  public void setAttributes(LinkedList<CEPConstExprNode> attrList)
      throws SQLException, ParseException
  {

    assert node instanceof CEPInsertNode;
    CEPInsertNode insertNode = (CEPInsertNode) node;

    insertNode.checkAndSetClientTS(conn);

    if (insertNode.isClientTimeStamped())
    {
      try
      {
        // The first column must be a timeStamp.
        CEPConstExprNode timeVal = attrList.removeFirst();
        long time = CEPInsertNode.getTime(timeVal);
        tuple.setTime(time);
      } catch (Exception e)
      {
        throw new SQLException("Could not set timestamp");
      }
    }

    // Get the schema from server and create an attrVal array
    CEPDatabaseMetaData dbm = (CEPDatabaseMetaData) conn.getMetaData();
    String schemaName = conn.getSchemaName();
    ResultSet rs = dbm.getColumns(null, schemaName, insertNode.getTableName(), "%");
    List<String> columns = new ArrayList<String>();
    while (rs.next())
    {
      String colType = rs.getString("TYPE_NAME");
      columns.add(colType);
    }
    if (insertNode.isClientTimeStamped())
    {
      // remove the Timestamp attr from the columnList.
      columns.remove(0);
    }

    if (attrList.size() != columns.size())
    {
      throw new SQLException("Schema mismatch. Re-write statement");
    }

    String[] colArgs = new String[columns.size()];
    columns.toArray(colArgs);
    createAttrVal(colArgs);

    int colCount = 0;
    for (AttributeValue attr : attrval)
    {
      try
      {
        // If the argument is null, then set null.
        if (attrList.get(colCount) instanceof CEPNullConstExprNode)
        {
          attr.setBNull(true);
          isSet[colCount] = true;
        }

        // else it must satisfy the type of the schema.
        else if (attr instanceof IntAttributeValue)
        {
          IntAttributeValue intAttr = (IntAttributeValue) attr;
          intAttr.iValueSet(((Number) attrList.get(colCount).getValue())
              .intValue());
          isSet[colCount] = true;
        } else if (attr instanceof BigintAttributeValue)
        {
          BigintAttributeValue bigintAttr = (BigintAttributeValue) attr;
          bigintAttr.lValueSet(((Number) attrList.get(colCount).getValue())
              .longValue());
          isSet[colCount] = true;
        } else if (attr instanceof FloatAttributeValue)
        {
          FloatAttributeValue floatAttr = (FloatAttributeValue) attr;
          floatAttr.fValueSet(((Number) attrList.get(colCount).getValue())
              .floatValue());
          isSet[colCount] = true;
        } else if (attr instanceof DoubleAttributeValue)
        {
          DoubleAttributeValue doubleAttr = (DoubleAttributeValue) attr;
          doubleAttr.dValueSet(((Number) attrList.get(colCount).getValue())
              .doubleValue());
          isSet[colCount] = true;
        } 
        else if (attr instanceof BigDecimalAttributeValue)
        {
          BigDecimalAttributeValue bigDecimalAttr = (BigDecimalAttributeValue) attr;
          BigDecimal val = ((BigDecimal)attrList.get(colCount).getValue());
          bigDecimalAttr.nValueSet(val, val.precision(), val.scale());
          isSet[colCount] = true;
        } 
        else if (attr instanceof BooleanAttributeValue)
        {
          BooleanAttributeValue boolAttr = (BooleanAttributeValue) attr;
          boolAttr.boolValueSet(((Boolean) attrList.get(colCount).getValue())
              .booleanValue());
          isSet[colCount] = true;
        } else if (attr instanceof ObjAttributeValue)
        {
          ObjAttributeValue objAttr = (ObjAttributeValue) attr;
          objAttr.oValueSet(attrList.get(colCount).getValue());
          isSet[colCount] = true;
        } else if (attr instanceof ByteAttributeValue)
        {
          ByteAttributeValue byteAttr = (ByteAttributeValue) attr;
          char[] hex = ((String) attrList.get(colCount).getValue())
              .toCharArray();
          byte[] bytes = Datatype.hexToByte(hex);
          byteAttr.bValueSet(bytes);
          byteAttr.bLengthSet(bytes.length);
          isSet[colCount] = true;
        } else if (attr instanceof CharAttributeValue)
        {
          CharAttributeValue charAttr = (CharAttributeValue) attr;
          char[] string = ((String) attrList.get(colCount).getValue())
              .toCharArray();
          charAttr.cValueSet(string);
          charAttr.cLengthSet(string.length);
          isSet[colCount] = true;
        } else if (attr instanceof XmltypeAttributeValue)
        {
          XmltypeAttributeValue xmlAttr = (XmltypeAttributeValue) attr;
          char[] string = ((String) attrList.get(colCount).getValue())
              .toCharArray();
          xmlAttr.xValueSet(string);
          xmlAttr.xLengthSet(string.length);
          isSet[colCount] = true;
        }
        colCount++;
      } catch (Exception e)
      {
        isSet[colCount] = false;
        throw new SQLException("Error setting attributes:" + e.getMessage(), e);
      }

    }

  }

  public int executeUpdate(String sql, int autoGeneratedKeys)
      throws SQLException
  {
    return (1);
    /*
     * throw new UnsupportedOperationException(
     * "CEPStatement:executeUpdate(String, int) unsupported");
     */
  }

  public int executeUpdate(String sql, int[] columnIndexes) throws SQLException
  {
    return (1);
    /*
     * throw new UnsupportedOperationException(
     * "CEPStatement:executeUpdate(String, int[]) unsupported");
     */
  }

  public int executeUpdate(String sql, String[] columnNames)
      throws SQLException
  {
    return (1);
    /*
     * throw new UnsupportedOperationException(
     * "CEPStatement:executeUpdate(String, String[]) unsupported");
     */
  }

  public Connection getConnection() throws SQLException
  {
    return conn;
  }

  public int getFetchDirection() throws SQLException
  {
    return (1);
    /*
     * throw new UnsupportedOperationException(
     * "CEPStatement:getFetchDirection() unsupported");
     */
  }

  public int getFetchSize() throws SQLException
  {
    return (100);
    /*
     * throw new UnsupportedOperationException( "CEPStatement:getFetchSize()
     * unsupported");
     */
  }

  public ResultSet getGeneratedKeys() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPStatement:getGeneratedKeys() unsupported");
  }

  public int getMaxFieldSize() throws SQLException
  {
    return (4000);
    /*
     * throw new UnsupportedOperationException( "CEPStatement:getMaxFieldSize()
     * unsupported");
     */
  }

  public int getMaxRows() throws SQLException
  {
    return (100);
    /*
     * throw new UnsupportedOperationException( "CEPStatement:getMaxRows()
     * unsupported");
     */
  }

  public boolean getMoreResults() throws SQLException
  {
    return (true);
    /*
     * throw new UnsupportedOperationException( "CEPStatement:getMoreResults()
     * unsupported");
     */
  }

  public boolean getMoreResults(int current) throws SQLException
  {
    return (true);
    /*
     * throw new UnsupportedOperationException(
     * "CEPStatement:getMoreResults(int) unsupported");
     */
  }

  public int getQueryTimeout() throws SQLException
  {
    return (100);
    /*
     * throw new UnsupportedOperationException( "CEPStatement:getQueryTimeout()
     * unsupported");
     */
  }

  public ResultSet getResultSet() throws SQLException
  {
    return result;
  }

  public int getResultSetConcurrency() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPStatement:getResultSetConcurrency() unsupported");
  }

  public int getResultSetHoldability() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPStatement:getResultSetHoldability() unsupported");
  }

  public int getResultSetType() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPStatement:getResultSetType() unsupported");
  }

  public int getUpdateCount() throws SQLException
  {
    return (1);
    /*
     * throw new UnsupportedOperationException( "CEPStatement:getUpdateCount()
     * unsupported");
     */
  }

  public SQLWarning getWarnings() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPStatement:getWarnings() unsupported");
  }

  public void setCursorName(String name) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPStatement:setCursorName(String) unsupported");
  }

  public void setEscapeProcessing(boolean enable) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPStatement:setEscapeProcessing(boolean) unsupported");
  }

  public void setFetchDirection(int direction) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPStatement:setFetchDirection(int) unsupported");
  }

  public void setFetchSize(int rows) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPStatement:setFetchSize(int) unsupported");
  }

  public void setMaxFieldSize(int max) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPStatement:setMaxFieldSize(int) unsupported");
  }

  public void setMaxRows(int max) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPStatement:setMaxRows(int) unsupported");
  }

  public void setQueryTimeout(int seconds) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPStatement:setQueryTimeout(int) unsupported");
  }

  public boolean isClosed() throws SQLException
  {
    return closed;
  }

  public boolean isPoolable() throws SQLException
  {
    // TODO Auto-generated method stub
    return false;
  }

  public void setPoolable(boolean arg0) throws SQLException
  {
    // TODO Auto-generated method stub

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
  public void closeOnCompletion() throws SQLException
  {
    throw new UnsupportedOperationException("CEPStatement.closeOnCompletion() not supported yet.");
  }

  @Override
  public boolean isCloseOnCompletion() throws SQLException
  {
    throw new UnsupportedOperationException("CEPStatement.isCloseOnCompletion not supported yet.");
  }
}
