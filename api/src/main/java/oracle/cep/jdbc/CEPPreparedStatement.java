/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/jdbc/CEPPreparedStatement.java /main/19 2013/09/13 10:38:07 udeshmuk Exp $ */

/* Copyright (c) 2007, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk    09/11/13 - in case of stale table source exception, set
                        servercontext to null and retry DML
 hopark      04/30/11 - fix isSet
 sbishnoi    09/19/10 - XbranchMerge sbishnoi_bug-10068411_ps3 from
                        st_pcbpel_11.1.1.4.0
 sbishnoi    09/01/10 - support for input batching
 alealves    05/25/10 - Log TableNotFound and StaleSource as informational and
                        not warning
 sbishnoi    02/02/10 - fix NPE bug 9273983
 sbishnoi    02/01/10 - fix NPE bug
 hopark      12/28/09 - set cause to SQLException
 sborah      07/20/09 - support for bigdecimal
 hopark      05/21/09 - add serverContext
 alealves    05/11/09 - mthatte - bug 8472811
 sborah      04/01/09 - modifying executeDML to handle the case when tableName
                        is null
 sbishnoi    04/01/09 - initialize serverContext in executeDML
 anasrini    03/23/09 - insert optimization
 hopark      02/17/09 - support boolean as external datatype
 hopark      02/02/09 - objtype support
 hopark      11/03/08 - fix schema
 hopark      10/09/08 - remove statics
 skmishra    10/14/08 - fixing schema support, tuple
 sbishnoi    09/22/08 - support for schema
 skmishra    08/26/08 - adding executeDML from hopark
 skmishra    08/12/08 - adding imports
 skmishra    07/25/08 - replacing CEPConnection with Connection
 hopark      04/23/08 - remove unnecessary log
 mthatte     04/16/08 - bug
 mthatte     03/14/08 - jdbc re-org
 parujain    02/12/08 - no severe msg to customer
 udeshmuk    02/01/08 - support for double data type.
 udeshmuk    01/17/08 - change in the way of setting timestamp of tuple.
 mthatte     12/06/07 - adding bLengthSet with bValueSAet
 mthatte     11/30/07 - fix for setString()
 mthatte     09/13/07 - Adding methods to make compliant with Java 6
 mthatte     09/05/07 - Adding heartbeat support & setInt, setTimestamp for timestamp column
 hopark      07/13/07 - dump stack trace on exception
 sbishnoi    05/27/07 - 
 parujain    05/09/07 - 
 najain      04/27/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/jdbc/CEPPreparedStatement.java /main/19 2013/09/13 10:38:07 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.Collection;

import oracle.cep.common.Constants;
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
import oracle.cep.dataStructures.external.TimestampAttributeValue;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.external.XmltypeAttributeValue;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.StaleTableSourceException;
import oracle.cep.exceptions.TableSourceNotFoundException;
import oracle.cep.jdbc.parser.CEPInsertNode;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.CEPServerXface;
import oracle.cep.service.IServerContext;

/**
 * This is CEP's implementation of the jdbc Statement class. We only allow DDLs
 * and DMLs to be processed. Note that DDLs can be in the form of registering a
 * query. There is a dummy SELECT implemented in order to always return an empty
 * ResultSet with associated ResultSetMetadata.
 * 
 * @author najain
 */

public class CEPPreparedStatement extends CEPStatement
    implements PreparedStatement
{
  private static Calendar  calendar;

  static
  {
    calendar = Calendar.getInstance();
  }

  private String  tableName;
  private IServerContext serverContext;

  public CEPPreparedStatement()
  {
  }

  public CEPPreparedStatement(String sql, CEPBaseConnection conn,
      CEPServerXface serv)
  {
    super(conn, serv);
    serverContext = null;
    // LogUtil.info(LoggerType.TRACE, "SQL received: " + sql);
    tuple = new TupleValue();
    try
    {
      node = parser.parseCommand(sql);
      isddl = false;
      ddl = null;
      node.prepareStatement(this);

      if (tableName != null)
        serverContext = server.prepareStatement(tableName,
                                                conn.getSchemaName());
    } catch (SQLException sqe)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, sqe);
      return;
    } catch (Exception e)
    {
      // This is not an insert statement.
      // Assume it is a DDL and send it to server.
      isddl = true;
      ddl = sql;
      tuple = null;
    }
  }

  public void setTableName(String tableName)
  {
    this.tableName = tableName;
  }

  /**
   * Creates attrVal based on args. Args are passed in by CEPInsertNode.
   * 
   * @param args
   */
  public void createAttrVal(String[] args)
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

  public void addBatch() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement:addBatch() unsupported");
  }

  public void clearParameters() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement:clearParameters() unsupported");
  }

  public boolean execute() throws SQLException
  {
    executeUpdate();
    return false;
  }

  public ResultSet executeQuery() throws SQLException
  {
    if (result == null)
      throw new SQLException("ResultSet does not exist");
    return result;
  }

  public int executeUpdate() throws SQLException
  {
    if (isddl)
    {
      assert tuple == null;
      try
      {
        return server.executeDDL(ddl, conn.getSchemaName());
      } catch (RemoteException e)
      {
        throw new SQLException(e);
      }
    } else
    {
      assert ddl == null;
      try

      {
        for (boolean setField : isSet)
        {
          if (!setField)
            throw new SQLException("All fields not set!");
        }
        tuple.setAttrs(attrval);
        return server.executeDML(tuple, conn.getSchemaName());
      } catch (RemoteException e)
      {
        throw new SQLException(e);
      }
    }
  }

  public TupleValue getTuple()
  {
    return this.tuple;
  }

  public ResultSetMetaData getMetaData() throws SQLException
  {
    if (result == null)
      throw new SQLException("No resultSet");
    else
      return result.getMetaData();
  }

  public ParameterMetaData getParameterMetaData() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement:getParameterMetaData() unsupported");
  }

  public void setArray(int i, Array x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement:setArray(int, Array) unsupported");
  }

  public void setAsciiStream(int parameterIndex, InputStream x, int length)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement:setAsciiStream(int, InputStream, int) unsupported");
  }

  public void setBigDecimal(int parameterIndex, BigDecimal x)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement: setBigDecimal(int, BigDecimal) unsupported");
  }

  public void setBinaryStream(int parameterIndex, InputStream x, int length)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement: setBinaryStream(int, InputStream, int) unsupported");
  }

  public void setBlob(int i, Blob x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement: setBlob(int, Blob) unsupported");
  }

  public void setBoolean(int parameterIndex, boolean x) throws SQLException
  {
    if (((CEPInsertNode) node).isClientTimeStamped())
      parameterIndex--;
    try
    {
      ((BooleanAttributeValue) attrval[parameterIndex - 1]).boolValueSet(x);
      isSet[parameterIndex - 1] = true;
    } catch (CEPException ce)
    {
      isSet[parameterIndex - 1] = false;
      throw new SQLException("setBoolean broke with CEPException"
          + ce.getMessage());
    } catch (Exception e)
    {
      throw new SQLException(e);
    }
  }

  public void setByte(int parameterIndex, byte x) throws SQLException
  {
    byte[] xArr = new byte[1];
    xArr[0] = x;
    setBytes(parameterIndex, xArr);
  }

  public void setBytes(int parameterIndex, byte[] x) throws SQLException
  {
    if (((CEPInsertNode) node).isClientTimeStamped())
      parameterIndex--;
    try
    {
      ((ByteAttributeValue) attrval[parameterIndex - 1]).bValueSet(x);
      ((ByteAttributeValue) attrval[parameterIndex - 1]).bLengthSet(x.length);
      isSet[parameterIndex - 1] = true;
    } catch (CEPException ce)
    {
      isSet[parameterIndex - 1] = false;
      throw new SQLException("setBytes broke with CEPException"
          + ce.getMessage());
    } catch (Exception e)
    {
      throw new SQLException(e);
    }
  }

  public void setCharacterStream(int parameterIndex, Reader reader, int length)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement: setCharacterStream(int, Reader, int) unsupported");
  }

  public void setClob(int i, Clob x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement: setClob(int, Clob) unsupported");
  }

  public void setDate(int parameterIndex, Date x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement: setDate(int, Date) unsupported");
  }

  public void setDate(int parameterIndex, Date x, Calendar cal)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement: setDate(int, Date, Calendar) unsupported");
  }

  public void setDouble(int parameterIndex, double x) throws SQLException
  {
    if (((CEPInsertNode) node).isClientTimeStamped())
      parameterIndex--;
    try
    {
      ((DoubleAttributeValue) attrval[parameterIndex - 1]).dValueSet(x);
      isSet[parameterIndex - 1] = true;
    } catch (CEPException ce)
    {
      isSet[parameterIndex - 1] = false;
      throw new SQLException("setDouble broke with CEPException"
          + ce.getMessage());
    } catch (Exception e)
    {
      throw new SQLException(e);
    }
  }

  public void setFloat(int parameterIndex, float x) throws SQLException
  {
    if (((CEPInsertNode) node).isClientTimeStamped())
      parameterIndex--;
    try
    {
      ((FloatAttributeValue) attrval[parameterIndex - 1]).fValueSet(x);
      isSet[parameterIndex - 1] = true;
    } catch (CEPException ce)
    {
      isSet[parameterIndex - 1] = false;
      throw new SQLException("setFloat broke with CEPException"
          + ce.getMessage());
    } catch (Exception e)
    {
      throw new SQLException(e);
    }
  }

  public void setInt(int parameterIndex, int x) throws SQLException
  {

    if (((CEPInsertNode) node).isHeartbeat())
    {
      this.tuple.setTime(x);
      isSet = new boolean[1];
      isSet[0] = true;
      return;
    }
    if (parameterIndex == 1)
    {
      if (((CEPInsertNode) (node)).isClientTimeStamped())
      {
        tuple.setTime(x);
        return;
      }
    }

    if (((CEPInsertNode) node).isClientTimeStamped())
      parameterIndex--;
    try
    {
      ((IntAttributeValue) attrval[parameterIndex - 1]).iValueSet(x);
      isSet[parameterIndex - 1] = true;
    } catch (CEPException ce)
    {
      isSet[parameterIndex - 1] = false;
      throw new SQLException("setInt broke with CEPException" + ce.getMessage());
    } catch (Exception e)
    {
      throw new SQLException(e);
    }

  }

  public void setLong(int parameterIndex, long x) throws SQLException
  {
    if (((CEPInsertNode) node).isHeartbeat())
    {
      this.tuple.setTime(x);
      isSet = new boolean[1];
      isSet[0] = true;
      return;
    }
    if (parameterIndex == 1)
    {
      if (((CEPInsertNode) (node)).isClientTimeStamped())
      {
        tuple.setTime(x);
        return;
      }
    }

    if (((CEPInsertNode) node).isClientTimeStamped())
      parameterIndex--;
    try
    {
      ((BigintAttributeValue) attrval[parameterIndex - 1]).lValueSet(x);
      isSet[parameterIndex - 1] = true;
    } catch (CEPException ce)
    {
      isSet[parameterIndex - 1] = false;
      throw new SQLException("setLong broke with CEPException" + ce.getMessage());
    } catch (Exception e)
    {
      throw new SQLException(e);
    }

  }

  /**
   * Allows user to set int,float,double,char, timestamp as null. TODO: What
   * about XmlType, Interval, Byte?
   */
  public void setNull(int parameterIndex, int sqlType) throws SQLException
  {
    if ((sqlType == Types.ARRAY) || (sqlType == Types.BINARY)
        || (sqlType == Types.BIT) || (sqlType == Types.BLOB)
        || (sqlType == Types.CLOB) || (sqlType == Types.DISTINCT)
        || (sqlType == Types.LONGVARBINARY)
        || (sqlType == Types.LONGVARCHAR) || (sqlType == Types.NULL)
        || (sqlType == Types.NUMERIC) || (sqlType == Types.OTHER)
        || (sqlType == Types.REAL) || (sqlType == Types.REF)
        || (sqlType == Types.STRUCT) || (sqlType == Types.VARBINARY)
        || (sqlType == Types.VARCHAR) || (sqlType == Types.DATE)
        || (sqlType == Types.SMALLINT) || (sqlType == Types.TIME)
        || (sqlType == Types.DECIMAL) || (sqlType == Types.TINYINT))
      throw new SQLException("sqlType not supported");

    if ((parameterIndex == 1) && (sqlType != Types.TIMESTAMP)
        && ((CEPInsertNode) node).isClientTimeStamped())
      throw new SQLException("First field denotes timestamp: Long to bind");

    else if (parameterIndex == 1
        && ((CEPInsertNode) node).isClientTimeStamped()
        && sqlType == Types.TIMESTAMP)
    {
      tuple.setTime(Constants.NULL_TIMESTAMP);
      return;
    }

    else if (((CEPInsertNode) node).isClientTimeStamped())
      parameterIndex--;
    try
    {
      switch (sqlType)
      {
      case Types.INTEGER:
        ((IntAttributeValue) attrval[parameterIndex - 1]).setBNull(true);
        isSet[parameterIndex - 1] = true;
        break;

      case Types.BOOLEAN:
        ((BooleanAttributeValue) attrval[parameterIndex - 1]).setBNull(true);
        isSet[parameterIndex - 1] = true;
        break;

      case Types.BIGINT:
        ((BigintAttributeValue) attrval[parameterIndex - 1]).setBNull(true);
        isSet[parameterIndex - 1] = true;
        break;

      case Types.FLOAT:
        ((FloatAttributeValue) attrval[parameterIndex - 1]).setBNull(true);
        isSet[parameterIndex - 1] = true;
        break;

      case Types.DOUBLE:
        ((DoubleAttributeValue) attrval[parameterIndex - 1]).setBNull(true);
        isSet[parameterIndex - 1] = true;
        break;
        
      case Types.NUMERIC:
        ((BigDecimalAttributeValue) attrval[parameterIndex - 1]).setBNull(true);
        isSet[parameterIndex - 1] = true;
        break;

      case Types.JAVA_OBJECT:
        ((ObjAttributeValue) attrval[parameterIndex - 1]).setBNull(true);
        isSet[parameterIndex - 1] = true;
        break;

      case Types.CHAR:
        ((CharAttributeValue) attrval[parameterIndex - 1]).setBNull(true);
        isSet[parameterIndex - 1] = true;
        break;

      case Types.TIMESTAMP:
        ((TimestampAttributeValue) attrval[parameterIndex - 1]).setBNull(true);
        isSet[parameterIndex - 1] = true;
        break;
      default:
        assert false;
      }
    } catch (Exception e)
    {
      isSet[parameterIndex - 1] = false;
      throw new SQLException(e);
    }
  }

  public void setNull(int paramIndex, int sqlType, String typeName)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement: setNull(int, int, String) unsupported");
  }

  public void setObject(int parameterIndex, Object x) throws SQLException
  {
    if (((CEPInsertNode) node).isClientTimeStamped())
      parameterIndex--;
    try
    {
      ((ObjAttributeValue) attrval[parameterIndex - 1]).oValueSet(x);
      isSet[parameterIndex - 1] = true;
    } catch (CEPException ce)
    {
      isSet[parameterIndex - 1] = false;
      throw new SQLException("setObject broke with CEPException"
          + ce.getMessage());
    } catch (Exception e)
    {
      throw new SQLException(e);
    }
  }

  public void setObject(int parameterIndex, Object x, int targetSqlType)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement: setObject(int, Object, int) unsupported");
  }

  public void setObject(int parameterIndex, Object x, int targetSqlType,
      int scale) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement: setObject(int, Object, int, int) unsupported");
  }

  // This is NOT a JDBC method, it is a CEP special...
  public void setResultSet(ResultSet rs)
  {
    this.result = (CEPResultSet)rs;
  }

  public void setRef(int i, Ref x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement: setRef(int, Ref) unsupported");
  }

  public void setShort(int parameterIndex, short x) throws SQLException
  {
    setInt(parameterIndex, (int) x);
  }

  public void setString(int parameterIndex, String x) throws SQLException
  {
    if (((CEPInsertNode) node).isHeartbeat())
    {
      try
      {
        long time = CEPInsertNode.getTime(x);
        this.tuple.setTime(time);
      } catch (Exception e)
      {
        throw new SQLException(e);
      }
      isSet = new boolean[1];
      isSet[0] = true;
      return;
    }
    if (((CEPInsertNode) node).isClientTimeStamped())
      parameterIndex--;
    try
    {
      if (attrval[parameterIndex - 1] instanceof CharAttributeValue)
      {
        ((CharAttributeValue) attrval[parameterIndex - 1]).cValueSet(x
            .toCharArray());
        ((CharAttributeValue) attrval[parameterIndex - 1]).cLengthSet(x
            .length());
      } else if (attrval[parameterIndex - 1] instanceof XmltypeAttributeValue)
      {
        ((XmltypeAttributeValue) attrval[parameterIndex - 1]).xValueSet(x
            .toCharArray());
        ((XmltypeAttributeValue) attrval[parameterIndex - 1]).xLengthSet(x
            .length());
      } else
        throw new Exception("Attribute can not take String values");
      isSet[parameterIndex - 1] = true;
    } catch (CEPException ce)
    {
      isSet[parameterIndex - 1] = false;
      throw new SQLException("setString broke with CEPException"
          + ce.getMessage());
    } catch (Exception e)
    {
      throw new SQLException(e);
    }
  }

  public void setTime(int parameterIndex, Time x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement: setTime(int, Time) unsupported");
  }

  public void setTime(int parameterIndex, Time x, Calendar cal)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement: setTime(int, Time, Calendar) unsupported");
  }

  public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException
  {

    if (parameterIndex == 1)
    {
      if (((CEPInsertNode) (node)).isClientTimeStamped())
      {
        tuple.setTime(x.getTime());
        return;
      }
    }
    if (((CEPInsertNode) node).isClientTimeStamped())
        parameterIndex--;
    try
    {
      ((TimestampAttributeValue) (attrval[parameterIndex - 1])).tValueSet(x
          .getTime());
      isSet[parameterIndex - 1] = true;
    } catch (CEPException ce)
    {
      throw new SQLException("Error setting Long", ce.getMessage());
    } catch (Exception e)
    {
      throw new SQLException(e);
    }
  }

  public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
      throws SQLException
  {
    Timestamp target = new Timestamp(0);
    long ms = x.getTime();
    calendar.clear();
    calendar.setTimeInMillis(ms);
    target.setTime(calendar.getTimeInMillis());
    setTimestamp(parameterIndex, target);
  }

  public void setUnicodeStream(int parameterIndex, InputStream x, int length)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement: setUnicodeStream(int, InputStream, int) unsupported");
  }

  public void setURL(int parameterIndex, URL x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement: setURL(int, URL) unsupported");
  }

  public void setAsciiStream(int parameterIndex, InputStream x)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement.setAsciiStream(int,InputStream) unsupported");
  }

  public void setAsciiStream(int parameterIndex, InputStream x, long length)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement.setAsciiStream(int,InputStream,long) unsupported");
  }

  public void setBinaryStream(int parameterIndex, InputStream x)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement.setBinaryStream(int,InputStream) unsupported");
  }

  public void setBinaryStream(int parameterIndex, InputStream x, long length)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement.setBinaryStream(int,InputStream,long) unsupported");
  }

  public void setBlob(int parameterIndex, InputStream inputStream)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement.setBlob(int,InputStream) unsupported");
  }

  public void setBlob(int parameterIndex, InputStream inputStream, long length)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement.setBlob(int,InputStream,long) unsupported");
  }

  public void setCharacterStream(int parameterIndex, Reader reader)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement.setCharacterStream(int,Reader) unsupported");
  }

  public void setCharacterStream(int parameterIndex, Reader reader, long length)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement.setCharacterStream(int,Reader,long) unsupported");
  }

  public void setClob(int parameterIndex, Reader reader) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement.setClob(int,Reader) unsupported");
  }

  public void setClob(int parameterIndex, Reader reader, long length)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement.setClob(int,Reader,long) unsupported");
  }

  public void setNCharacterStream(int parameterIndex, Reader value)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement.setNCharacterStream(int,Reader) unsupported");
  }

  public void setNCharacterStream(int parameterIndex, Reader value, long length)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement.setNCharacterStream(int,Reader,long) unsupported");
  }

  public void setNClob(int parameterIndex, NClob value) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement.setNClob(int,NClob) unsupported");
  }

  public void setNClob(int parameterIndex, Reader reader) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement.setNClob(int,Reader) unsupported");
  }

  public void setNClob(int parameterIndex, Reader reader, long length)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement.setNClob(int,Reader,long) unsupported");
  }

  public void setNString(int parameterIndex, String value) throws SQLException
  {

    throw new UnsupportedOperationException(
        "CEPPreparedStatement.setNString(int,String) unsupported");
  }

  public void setRowId(int parameterIndex, RowId x) throws SQLException
  {

    throw new UnsupportedOperationException(
        "CEPPreparedStatement.setRowId(int,RowId) unsupported");
  }

  public void setSQLXML(int parameterIndex, SQLXML xmlObject)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPPreparedStatement.setSQLXML(int,String) unsupported");
  }

  public boolean isClosed() throws SQLException
  {
    return false;
  }

  public boolean isPoolable() throws SQLException
  {
    return false;
  }

  public void setPoolable(boolean poolable) throws SQLException
  {
    // TODO Auto-generated method stub

  }

  public boolean isWrapperFor(Class<?> iface) throws SQLException
  {
    return false;
  }

  public <T> T unwrap(Class<T> iface) throws SQLException
  {
    return null;
  }
 

  /*
    This method is intentionally designed to execute concurrently.
    The concurrency is supposed to be handled by cepServr.
  */
  public int executeDML(TupleValue tuple) throws SQLException
  {
    return executeDMLBase(null, tuple);
  }

  /*
    This method is intentionally designed to execute concurrently.
    The concurrency is supposed to be handled by cepServr.
  */
  public int executeDML(Collection<TupleValue> tupleBatch) throws SQLException
  {
    return executeDMLBase(tupleBatch, null);
  } 


  /**
   * This method will input either a batch or a single tuple to CEP engine
   * @param tupleBatch batch of input tuples
   * @param tuple a single tuple
   */
  private int executeDMLBase(Collection<TupleValue> tupleBatch, 
                             TupleValue tuple) 
    throws SQLException
  {
    if (server == null)
    {
      throw new SQLException("Invalid connection");
    }
    try
    {
      if (serverContext == null)
      {
        if(tableName != null)
          serverContext = server.prepareStatement(tableName, conn.getSchemaName());
        else
        {
          // Either call the server's batching API or single Tuple API
          if(tupleBatch != null)
            return server.executeDML(tupleBatch.iterator(), conn.getSchemaName());
          else
            return server.executeDML(tuple, conn.getSchemaName());
        }
      }
      
      //bug '8472811'
      //bug '9273983'
      // Note: serverContext may be null after executing the statement
      // Reason: On runtime hard exception, cqlengine may drop the query &
      //   hence it will drop the table source also as part of drop operation.
      //   [serverContext is a VersionedTableSource object]
      if(serverContext == null)
      {
        throw new TableSourceNotFoundException(
          "TableSource not found for table: " + tableName + 
          ". Possible cause: no queries registered against: " + tableName);
      }

      // Either call the server's batching API or single Tuple API
      if(tupleBatch != null)
        return server.executeDML(tupleBatch.iterator(), conn.getSchemaName(), serverContext);
      else
        return server.executeDML(tuple, conn.getSchemaName(), serverContext);
    }
    catch (RemoteException e)
    {
      /* Bug 17187906
       * When a StaleTableSourceException is thrown by engine -
       * 1. Do not re-throw it.
       * 2. Set the serverContext to null.
       * 3. Again try the executeDML with same arguments.
       */
      if (e.getCause() != null && 
          (e.getCause() instanceof StaleTableSourceException))
      {
        // Standard case where all queries of a source have been dropped, no need to flag this as a warning.
        LogUtil.info(LoggerType.TRACE, conn.getSchemaName() + 
            " executeDML failed with StaleTableSource. Resetting server context and retrying.. " + e.toString());
        serverContext = null;
        return executeDMLBase(tupleBatch, tuple);
      } 
      else 
      {
        LogUtil.warning(LoggerType.TRACE, conn.getSchemaName() + 
            " executeDML failed. Resetting server context. " + e.toString());
        LogUtil.logStackTrace(e);
        // Note: As the SQLException has been encountered while running executeDML,
        // CQLEventReceiver will reinitialize the CEPPreparedStatement before
        // next DML operation.
        serverContext = null;
      
        if(e.getCause() instanceof SQLException)
          throw (SQLException)e.getCause();
        else
          throw new SQLException(e);
      }
    }
  }

}
