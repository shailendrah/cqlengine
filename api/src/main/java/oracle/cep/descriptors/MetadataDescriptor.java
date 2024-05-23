/* $Header: pcbpel/cep/common/src/oracle/cep/descriptors/MetadataDescriptor.java /main/3 2008/09/10 14:06:32 skmishra Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 skmishra    08/20/08 - changing package name
 mthatte     11/05/07 - cleanup
 mthatte     10/09/07 - adding precision and scale methods
 mthatte     08/21/07 - Base class for metadata descriptor shared between
 Client and Server
 mthatte     08/21/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/descriptors/MetadataDescriptor.java /main/3 2008/09/10 14:06:32 skmishra Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.descriptors;

import java.io.InputStream;
import java.io.Reader;
import java.io.Externalizable;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

public abstract class MetadataDescriptor implements Externalizable
{
  private static final long serialVersionUID = 5758891749060197093L;

  /*	
   * Required for ResultSetMetadata
   * */

  public String getCatalogName(int column) throws SQLException
  {
    throw new SQLException("Method not supported");
  }

  public String getColumnClassName(int column) throws SQLException
  {
    throw new SQLException("Method not supported");
  }

  public int getColumnCount() throws SQLException
  {
    throw new SQLException("Method not supported");
  }

  public int getColumnDisplaySize(int column) throws SQLException
  {
    throw new SQLException("Method not supported");
  }

  public String getColumnLabel(int column) throws SQLException
  {
    throw new SQLException("Method not supported");
  }

  public String getColumnName(int column) throws SQLException
  {
    throw new SQLException("Method not supported");
  }

  public int getColumnType(int column) throws SQLException
  {
    throw new SQLException("Method not supported");
  }

  public String getColumnTypeName(int column) throws SQLException
  {
    throw new SQLException("Method not supported");
  }

  public int getPrecision(int column) throws SQLException
  {
    throw new SQLException("Method not supported");
  }

  public int getScale(int column) throws SQLException
  {
    throw new SQLException("Method not supported");
  }

  public String getSchemaName(int column) throws SQLException
  {
    throw new SQLException("Method not supported");
  }

  public String getTableName(int column) throws SQLException
  {
    throw new SQLException("Method not supported");
  }

  public boolean isAutoIncrement(int column) throws SQLException
  {
    throw new SQLException("Method not supported");
  }

  public boolean isCaseSensitive(int column) throws SQLException
  {
    throw new SQLException("Method not supported");
  }

  public boolean isCurrency(int column) throws SQLException
  {
    throw new SQLException("Method not supported");
  }

  public boolean isDefinitelyWritable(int column) throws SQLException
  {
    throw new SQLException("Method not supported");
  }

  public int isNullable(int column) throws SQLException
  {
    return DatabaseMetaData.columnNullable;
  }

  public boolean isReadOnly(int column) throws SQLException
  {
    throw new SQLException("Method not supported");
  }

  public boolean isSearchable(int column) throws SQLException
  {
    throw new SQLException("Method not supported");
  }

  public boolean isSigned(int column) throws SQLException
  {
    throw new SQLException("Method not supported");
  }

  public boolean isWritable(int column) throws SQLException
  {
    throw new SQLException("Method not supported");
  }

  /*
   * Required for ResultSet.
   */
  public int findColumn(String columnName) throws SQLException
  {
    throw new SQLException("Method not supported");
  }

  public Array getArray(int i) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Array getArray(String colName) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public InputStream getAsciiStream(int columnIndex) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public InputStream getAsciiStream(String columnName) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public BigDecimal getBigDecimal(int columnIndex) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public BigDecimal getBigDecimal(String columnName) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public BigDecimal getBigDecimal(int columnIndex, int scale)
      throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public BigDecimal getBigDecimal(String columnName, int scale)
      throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public InputStream getBinaryStream(int columnIndex) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public InputStream getBinaryStream(String columnName) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Blob getBlob(int i) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Blob getBlob(String colName) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public boolean getBoolean(int columnIndex) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public boolean getBoolean(String columnName) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public byte getByte(int columnIndex) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public byte getByte(String columnName) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public byte[] getBytes(int columnIndex) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public byte[] getBytes(String columnName) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Reader getCharacterStream(int columnIndex) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Reader getCharacterStream(String columnName) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Clob getClob(int i) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Clob getClob(String colName) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public String getCursorName() throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Date getDate(int columnIndex) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Date getDate(String columnName) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Date getDate(int columnIndex, Calendar cal) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Date getDate(String columnName, Calendar cal) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public double getDouble(int columnIndex) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public double getDouble(String columnName) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public int getFetchDirection() throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public int getFetchSize() throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public float getFloat(int columnIndex) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public float getFloat(String columnName) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public int getInt(int columnIndex) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public int getInt(String columnName) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public long getLong(int columnIndex) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public long getLong(String columnName) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public ResultSetMetaData getMetaData() throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Object getObject(int columnIndex) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Object getObject(String columnName) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Object getObject(int i, Map<String, Class<?>> map) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Object getObject(String colName, Map<String, Class<?>> map)
      throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Ref getRef(int i) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Ref getRef(String colName) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public int getRow() throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public short getShort(int columnIndex) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public short getShort(String columnName) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Statement getStatement() throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public String getString(int columnIndex) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public String getString(String columnName) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Time getTime(int columnIndex) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Time getTime(String columnName) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Time getTime(int columnIndex, Calendar cal) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Time getTime(String columnName, Calendar cal) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Timestamp getTimestamp(int columnIndex) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Timestamp getTimestamp(String columnName) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Timestamp getTimestamp(int columnIndex, Calendar cal)
      throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public Timestamp getTimestamp(String columnName, Calendar cal)
      throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public URL getURL(String columnMame) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public URL getURL(int columnIndex) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public InputStream getUnicodeStream(int columnIndex) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }

  public InputStream getUnicodeStream(String columnName) throws SQLException
  {
    throw new SQLException("Method not supported.");
  }
}
