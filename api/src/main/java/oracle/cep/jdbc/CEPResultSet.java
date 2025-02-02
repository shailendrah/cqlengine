/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/jdbc/CEPResultSet.java /main/6 2014/01/28 20:39:52 ybedekar Exp $ */

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
 hopark      10/09/08 - remove statics
 parujain    09/23/08 - multiple schema
 skmishra    07/25/08 - replacing CEPConnection with java.sql.Connection
 mthatte     03/17/08 - jdbc reorg
 mthatte     11/05/07 - bug fix
 mthatte     10/05/07 - Adding support for select
 mthatte     08/15/07 - Creation
 */

/**
 * 	This class implements the ResultSet interface for CEP.
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/jdbc/CEPResultSet.java /main/6 2014/01/28 20:39:52 ybedekar Exp $
 *  @author  mthatte 
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
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.ListIterator;
import java.util.Map;

import oracle.cep.descriptors.ArrayContext;
import oracle.cep.descriptors.MetadataDescriptor;

public class CEPResultSet implements java.sql.ResultSet
{

  private Statement                        statement               = null;
  private MetadataDescriptor               currentObjectDescriptor = null; // if
  // object
  // is
  // at
  // server
  private ArrayContext                     results                 = null; // If
  // entire
  // list
  // is
  // sent
  // back.
  private CEPBaseConnection                thisConn                = null;
  private ListIterator<MetadataDescriptor> resultIterator          = null;
  private long                             id;

  public CEPResultSet(long id, CEPBaseConnection conn, MetadataDescriptor desc)
  {
    this.id = id;
    thisConn = conn;
    currentObjectDescriptor = desc;
    thisConn.addResultSetID(id);
  }

  public CEPResultSet(ArrayContext res)
  {
    id = 0;
    if (res == null)
    {
      results = new ArrayContext();

    }
    results = res;
    currentObjectDescriptor = res.getIterator().next();
    resultIterator = res.getIterator();
  }

  public CEPResultSet(long id)
  {
    this.id = id;
  }

  public boolean absolute(int row) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPREsultSet.afterLast() not supported");
  }

  public void afterLast() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPREsultSet.afterLast() not supported");
  }

  public void beforeFirst() throws SQLException
  {
    if (results != null)
      resultIterator = results.getIterator(); // Point to new iterator
    else
      throw new UnsupportedOperationException(
          "CEPResultSet.beforeFirst() not supported");
  }

  public void cancelRowUpdates() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPREsultSet.cancelRowUpdates() not supported");
  }

  public void clearWarnings() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.clearWarnings() not supported");
  }

  public void close() throws SQLException
  {
    statement = null;
    currentObjectDescriptor = null;
    results = null;
    thisConn = null;
    resultIterator = null;
  }

  public void deleteRow() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.deleteRow() not supported");
  }

  public int findColumn(String columnName) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.findColumn(String) not supported");
  }

  public boolean first() throws SQLException
  {
    if (id == -1)
      return false;
    if (results == null)
      throw new UnsupportedOperationException(
          "CEPResultSet.first() not supported for all results");
    else
    {
      resultIterator = results.getIterator();
      if (resultIterator.hasNext())
      {
        resultIterator.next();
        return true;
      }

      else
        return false;
    }
  }

  public Array getArray(int i) throws SQLException
  {
    return currentObjectDescriptor.getArray(i);
  }

  public Array getArray(String colName) throws SQLException
  {
    return currentObjectDescriptor.getArray(colName);
  }

  public InputStream getAsciiStream(int columnIndex) throws SQLException
  {
    return currentObjectDescriptor.getAsciiStream(columnIndex);
  }

  public InputStream getAsciiStream(String columnName) throws SQLException
  {
    return currentObjectDescriptor.getAsciiStream(columnName);
  }

  public BigDecimal getBigDecimal(int columnIndex) throws SQLException
  {
    return currentObjectDescriptor.getBigDecimal(columnIndex);
  }

  public BigDecimal getBigDecimal(String columnName) throws SQLException
  {
    return currentObjectDescriptor.getBigDecimal(columnName);
  }

  public BigDecimal getBigDecimal(int columnIndex, int scale)
      throws SQLException
  {
    return currentObjectDescriptor.getBigDecimal(columnIndex, scale);
  }

  public BigDecimal getBigDecimal(String columnName, int scale)
      throws SQLException
  {
    return currentObjectDescriptor.getBigDecimal(columnName, scale);
  }

  public InputStream getBinaryStream(int columnIndex) throws SQLException
  {
    return currentObjectDescriptor.getBinaryStream(columnIndex);
  }

  public InputStream getBinaryStream(String columnName) throws SQLException
  {
    return currentObjectDescriptor.getBinaryStream(columnName);
  }

  public Blob getBlob(int i) throws SQLException
  {
    return currentObjectDescriptor.getBlob(i);
  }

  public Blob getBlob(String colName) throws SQLException
  {
    return currentObjectDescriptor.getBlob(colName);
  }

  public boolean getBoolean(int columnIndex) throws SQLException
  {
    return currentObjectDescriptor.getBoolean(columnIndex);
  }

  public boolean getBoolean(String columnName) throws SQLException
  {
    return currentObjectDescriptor.getBoolean(columnName);
  }

  public byte getByte(int columnIndex) throws SQLException
  {
    return currentObjectDescriptor.getByte(columnIndex);
  }

  public byte getByte(String columnName) throws SQLException
  {
    return currentObjectDescriptor.getByte(columnName);
  }

  public byte[] getBytes(int columnIndex) throws SQLException
  {
    return currentObjectDescriptor.getBytes(columnIndex);
  }

  public byte[] getBytes(String columnName) throws SQLException
  {
    return currentObjectDescriptor.getBytes(columnName);
  }

  public Reader getCharacterStream(int columnIndex) throws SQLException
  {
    return currentObjectDescriptor.getCharacterStream(columnIndex);
  }

  public Reader getCharacterStream(String columnName) throws SQLException
  {
    return currentObjectDescriptor.getCharacterStream(columnName);
  }

  public Clob getClob(int i) throws SQLException
  {
    return currentObjectDescriptor.getClob(i);
  }

  public Clob getClob(String colName) throws SQLException
  {
    return currentObjectDescriptor.getClob(colName);
  }

  public int getConcurrency() throws SQLException
  {
    return ResultSet.CONCUR_READ_ONLY;
  }

  public String getCursorName() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.getCursorName() not supported as of now");
  }

  public Date getDate(int columnIndex) throws SQLException
  {
    return currentObjectDescriptor.getDate(columnIndex);
  }

  public Date getDate(String columnName) throws SQLException
  {
    return currentObjectDescriptor.getDate(columnName);
  }

  public Date getDate(int columnIndex, Calendar cal) throws SQLException
  {
    return currentObjectDescriptor.getDate(columnIndex, cal);
  }

  public Date getDate(String columnName, Calendar cal) throws SQLException
  {
    return currentObjectDescriptor.getDate(columnName, cal);
  }

  public double getDouble(int columnIndex) throws SQLException
  {
    return currentObjectDescriptor.getDouble(columnIndex);
  }

  public double getDouble(String columnName) throws SQLException
  {
    return currentObjectDescriptor.getDouble(columnName);
  }

  public int getFetchDirection() throws SQLException
  {
    return ResultSet.FETCH_UNKNOWN;
  }

  public int getFetchSize() throws SQLException
  {
    return 0;
  }

  public float getFloat(int columnIndex) throws SQLException
  {
    return currentObjectDescriptor.getFloat(columnIndex);
  }

  public float getFloat(String columnName) throws SQLException
  {
    return currentObjectDescriptor.getFloat(columnName);
  }

  public int getInt(int columnIndex) throws SQLException
  {
    return currentObjectDescriptor.getInt(columnIndex);

  }

  public int getInt(String columnName) throws SQLException
  {
    return currentObjectDescriptor.getInt(columnName);
  }

  public long getLong(int columnIndex) throws SQLException
  {
    return currentObjectDescriptor.getLong(columnIndex);
  }

  public long getLong(String columnName) throws SQLException
  {
    return currentObjectDescriptor.getLong(columnName);
  }

  public ResultSetMetaData getMetaData() throws SQLException
  {
    if (currentObjectDescriptor == null)
      throw new SQLException("Empty result set");

    return new CEPResultSetMetadata(this);
  }

  public Object getObject(int columnIndex) throws SQLException
  {
    return currentObjectDescriptor.getObject(columnIndex);
  }

  public Object getObject(String columnName) throws SQLException
  {
    return currentObjectDescriptor.getObject(columnName);
  }

  public Object getObject(int i, Map<String, Class<?>> map) throws SQLException
  {
    return currentObjectDescriptor.getObject(i, map);
  }

  public Object getObject(String colName, Map<String, Class<?>> map)
      throws SQLException
  {
    return currentObjectDescriptor.getObject(colName, map);
  }

  public Ref getRef(int i) throws SQLException
  {
    return currentObjectDescriptor.getRef(i);
  }

  public Ref getRef(String colName) throws SQLException
  {
    return currentObjectDescriptor.getRef(colName);
  }

  public int getRow() throws SQLException
  {
    if (results != null)
      return resultIterator.nextIndex(); // ListIterator starts at 0;
    else
      throw new UnsupportedOperationException(
          "CEPResultSet.getRow() not supported");
  }

  public short getShort(int columnIndex) throws SQLException
  {
    return currentObjectDescriptor.getShort(columnIndex);
  }

  public short getShort(String columnName) throws SQLException
  {
    return currentObjectDescriptor.getShort(columnName);
  }

  public SQLXML getSQLXML(int columnIndex)
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.getSQLXML() not supported");
  }

  public SQLXML getSQLXML(String columnLabel)
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.getSQLXML() not supported");
  }

  public Statement getStatement() throws SQLException
  {
    return statement;
  }

  public String getString(int columnIndex) throws SQLException
  {
    return currentObjectDescriptor.getString(columnIndex);
  }

  public String getString(String columnName) throws SQLException
  {
    return currentObjectDescriptor.getString(columnName);
  }

  public Time getTime(int columnIndex) throws SQLException
  {
    return currentObjectDescriptor.getTime(columnIndex);
  }

  public Time getTime(String columnName) throws SQLException
  {
    return currentObjectDescriptor.getTime(columnName);
  }

  public Time getTime(int columnIndex, Calendar cal) throws SQLException
  {
    return currentObjectDescriptor.getTime(columnIndex, cal);
  }

  public Time getTime(String columnName, Calendar cal) throws SQLException
  {
    return currentObjectDescriptor.getTime(columnName, cal);
  }

  public Timestamp getTimestamp(int columnIndex) throws SQLException
  {
    return currentObjectDescriptor.getTimestamp(columnIndex);
  }

  public Timestamp getTimestamp(String columnName) throws SQLException
  {
    return currentObjectDescriptor.getTimestamp(columnName);
  }

  public Timestamp getTimestamp(int columnIndex, Calendar cal)
      throws SQLException
  {
    return currentObjectDescriptor.getTimestamp(columnIndex, cal);
  }

  public Timestamp getTimestamp(String columnName, Calendar cal)
      throws SQLException
  {
    return currentObjectDescriptor.getTimestamp(columnName, cal);
  }

  public int getType() throws SQLException
  {
    if (results == null)
      return ResultSet.TYPE_FORWARD_ONLY;
    else
      return ResultSet.TYPE_SCROLL_INSENSITIVE;
  }

  public URL getURL(int columnIndex) throws SQLException
  {
    return currentObjectDescriptor.getURL(columnIndex);
  }

  public URL getURL(String columnName) throws SQLException
  {
    return currentObjectDescriptor.getURL(columnName);
  }

  public InputStream getUnicodeStream(int columnIndex) throws SQLException
  {
    return currentObjectDescriptor.getUnicodeStream(columnIndex);
  }

  public InputStream getUnicodeStream(String columnName) throws SQLException
  {
    return currentObjectDescriptor.getUnicodeStream(columnName);
  }

  public SQLWarning getWarnings() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.getWarnings() not supported for all ResultSets");
  }

  public void insertRow() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.insertRow() not supported for all ResultSets");
  }

  public boolean isAfterLast() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.isAfterLast() not supported for all ResultSets");
  }

  public boolean isBeforeFirst() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.isBeforeFirst() not supported for all ResultSets");
  }

  public boolean isFirst() throws SQLException
  {
    if (id == -1)
      throw new SQLException("Empty set.");
    if (results != null)
      return !resultIterator.hasPrevious();
    throw new UnsupportedOperationException(
        "CEPResultSet.isLast() not supported for all ResultSets");

  }

  public boolean isLast() throws SQLException
  {
    if (id == -1)
      throw new SQLException("Empty set.");
    if (results != null)
      return !resultIterator.hasNext();
    throw new UnsupportedOperationException(
        "CEPResultSet.isLast() not supported for all ResultSets");
  }

  public boolean last() throws SQLException
  {
    if (id == -1)
      return false;
    if (results != null)
    {
      while (resultIterator.hasNext())
        resultIterator.next();
      return true;
    }
    throw new UnsupportedOperationException(
        "CEPResultSet.last() not supported for all ResultSets");
  }

  public void moveToCurrentRow() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.moveToCurrentRow() not supported");
  }

  public void moveToInsertRow() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.moveToInsertRow() not supported");
  }

  public boolean next() throws SQLException
  {
    // If the whole result set is cached on the client
    if (results != null)
    {
      if (resultIterator.hasNext())
      {
        currentObjectDescriptor = resultIterator.next();
        return true;
      } else
      {
        return false;
      }
    }

    if (currentObjectDescriptor == null)
    {
      return false;
    } else
    {
      return true;
    }
  }

  public boolean previous() throws SQLException
  {
    // Empty set
    if (id == -1)
      return false;

    // results cached at client
    if (results != null)
    {
      if (resultIterator.hasPrevious())
      {
        currentObjectDescriptor = resultIterator.previous();
        return true;
      }

      return false;
    }

    // if we reached here it means the results are on the DB and previous
    // is not supported
    throw new UnsupportedOperationException(
        "CEPResultSet.previous() not supported for all queries");
  }

  public void refreshRow() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.refreshRows() not supported for all ResultSets");
  }

  public boolean relative(int rows) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.relative() not supported for all ResultSets");
  }

  public boolean rowDeleted() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.rowDeleted() not supported for all ResultSets");
  }

  public boolean rowInserted() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.rowInserted() not supported for all ResultSets");
  }

  public boolean rowUpdated() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.rowUpdated() not supported for all ResultSets");
  }

  public void setFetchDirection(int direction) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.setFetchDirection() not supported for all ResultSets");
  }

  public void setFetchSize(int rows) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.setFetchSize() not supported for all ResultSets");
  }

  public void updateArray(int columnIndex, Array x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateArray() not supported for all ResultSets");
  }

  public void updateArray(String columnName, Array x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateArray() not supported for all ResultSets");
  }

  public void updateAsciiStream(int columnIndex, InputStream x, int length)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateASCIIStream() not supported for all ResultSets");
  }

  public void updateAsciiStream(int columnIndex, InputStream x, long length)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateASCIIStream() not supported for all ResultSets");
  }

  public void updateAsciiStream(int columnIndex, InputStream x)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateASCIIStream() not supported for all ResultSets");
  }

  public void updateAsciiStream(String columnName, InputStream x, int length)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateASCIIStream() not supported for all ResultSets");
  }

  public void updateAsciiStream(String columnName, InputStream x, long length)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateASCIIStream() not supported for all ResultSets");
  }

  public void updateBigDecimal(int columnIndex, BigDecimal x)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateBigDecimal() not supported for all ResultSets");
  }

  public void updateBigDecimal(String columnName, BigDecimal x)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateBigDecimal() not supported for all ResultSets");
  }

  public void updateBinaryStream(int columnIndex, InputStream x, int length)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateBinaryStream() not supported for all ResultSets");
  }

  public void updateBinaryStream(int columnIndex, InputStream x, long length)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateBinaryStream() not supported for all ResultSets");
  }

  public void updateBinaryStream(int columnIndex, InputStream x)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateBinaryStream() not supported for all ResultSets");
  }

  public void updateBinaryStream(String columnName, InputStream x, int length)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateBinaryStream() not supported for all ResultSets");
  }

  public void updateBinaryStream(String columnName, InputStream x, long length)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateBinaryStream() not supported for all ResultSets");
  }

  public void updateBlob(int columnIndex, Blob x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateBlob() not supported for all ResultSets");
  }

  public void updateBlob(String columnName, Blob x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateBlob() not supported for all ResultSets");
  }

  public void updateBoolean(int columnIndex, boolean x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateBoolean() not supported for all ResultSets");
  }

  public void updateBoolean(String columnName, boolean x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateBoolean() not supported for all ResultSets");
  }

  public void updateByte(int columnIndex, byte x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateByte() not supported for all ResultSets");
  }

  public void updateByte(String columnName, byte x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateByte() not supported for all ResultSets");
  }

  public void updateBytes(int columnIndex, byte[] x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateBytes() not supported for all ResultSets");
  }

  public void updateBytes(String columnName, byte[] x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateBytes() not supported for all ResultSets");
  }

  public void updateCharacterStream(int columnIndex, Reader x)
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateCharStream() not supported for all ResultSets");
  }

  public void updateCharacterStream(int columnIndex, Reader x, int length)
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateCharStream() not supported for all ResultSets");
  }

  public void updateCharacterStream(int columnIndex, Reader x, long length)
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateCharStream() not supported for all ResultSets");
  }

  public void updateCharacterStream(String columnLabel, Reader reader)
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateCharStream() not supported for all ResultSets");
  }

  public void updateCharacterStream(String columnLabel, Reader reader,
      int length)
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateCharStream() not supported for all ResultSets");
  }

  public void updateCharacterStream(String columnLabel, Reader reader,
      long length)
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateCharStream() not supported for all ResultSets");
  }

  public void updateClob(int columnIndex, Clob x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateClob() not supported for all ResultSets");
  }

  public void updateClob(String columnName, Clob x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateClob() not supported for all ResultSets");
  }

  public void updateDate(int columnIndex, Date x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateDate() not supported");
  }

  public void updateDate(String columnName, Date x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateDate() not supported");
  }

  public void updateDouble(int columnIndex, double x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateDouble() not supported");
  }

  public void updateDouble(String columnName, double x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateDouble() not supported");
  }

  public void updateFloat(int columnIndex, float x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateFloat() not supported");
  }

  public void updateFloat(String columnName, float x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateFloat() not supported");
  }

  public void updateInt(int columnIndex, int x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateInt() not supported");
  }

  public void updateInt(String columnName, int x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateInt() not supported");
  }

  public void updateLong(int columnIndex, long x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateLong() not supported");
  }

  public void updateLong(String columnName, long x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateLong() not supported");
  }

  public void updateNCharacterStream(int columnIndex, Reader x)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateNCharStrm() not supported");
  }

  public void updateNCharacterStream(int columnIndex, Reader x, long length)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateNCharStrm() not supported");
  }

  public void updateNCharacterStream(String columnLabel, Reader reader)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateNCharStrm() not supported");
  }

  public void updateNCharacterStream(String columnLabel, Reader reader,
      long length) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateNCharStrm() not supported");
  }

  public void updateNClob(int columnIndex, NClob nClob) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateNClob() not supported");
  }

  public void updateNClob(int columnIndex, Reader reader) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateNClob() not supported");
  }

  public void updateNClob(int columnIndex, Reader reader, long length)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateNClob() not supported");
  }

  public void updateNClob(String columnLabel, NClob nClob) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateNClob() not supported");
  }

  public void updateNClob(String columnLabel, Reader reader)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateNClob() not supported");
  }

  public void updateNClob(String columnLabel, Reader reader, long length)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateNClob() not supported");
  }

  public void updateNString(int columnIndex, String nString)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateNString() not supported");
  }

  public void updateNString(String columnLabel, String nString)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateNString() not supported");
  }

  public void updateNull(int columnIndex) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateNull() not supported");
  }

  public void updateNull(String columnName) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateNull() not supported");
  }

  public void updateObject(int columnIndex, Object x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateObject() not supported");
  }

  public void updateObject(String columnName, Object x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateObject() not supported");
  }

  public void updateObject(int columnIndex, Object x, int scale)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateObject() not supported");
  }

  public void updateObject(String columnName, Object x, int scale)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateObject() not supported");
  }

  public void updateRef(int columnIndex, Ref x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateRef() not supported");
  }

  public void updateRef(String columnName, Ref x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateRef() not supported");
  }

  public void updateRow() throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateRow() not supported");
  }

  public void updateRowId(int columnIndex, RowId x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPConnnection.updateRowId() not supported");
  }

  public void updateRowId(String columnLabel, RowId x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPConnnection.updateRowId() not supported");
  }

  public void updateShort(int columnIndex, short x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateShort() not supported");
  }

  public void updateShort(String columnName, short x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateShort() not supported");
  }

  public void updateSQLXML(int columnIndex, SQLXML xmlObject)
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateSQLXML() not supported");
  }

  public void updateSQLXML(String columnLabel, SQLXML xmlObject)
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateSQLXML() not supported");
  }

  public void updateString(int columnIndex, String x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateString() not supported");
  }

  public void updateString(String columnName, String x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateString() not supported");
  }

  public void updateTime(int columnIndex, Time x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateTime() not supported");
  }

  public void updateTime(String columnName, Time x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateTime() not supported");
  }

  public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateTimestamp() not supported");
  }

  public void updateTimestamp(String columnName, Timestamp x)
      throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSet.updateTimestamp() not supported");
  }

  public boolean wasNull() throws SQLException
  {
    return false;
  }

  public boolean isWrapperFor(Class<?> iface) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPStatement.isWrapperFor(Class<?>) not supported");
  }

  public <T> T unwrap(Class<T> iface) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPStatement.unWrap(Class<T>) not supported");
  }

  public MetadataDescriptor getDescriptor()
  {
    return this.currentObjectDescriptor;
  }

  public int getHoldability() throws SQLException
  {
    // TODO Auto-generated method stub
    return 0;
  }

  public Reader getNCharacterStream(int arg0) throws SQLException
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Reader getNCharacterStream(String arg0) throws SQLException
  {
    // TODO Auto-generated method stub
    return null;
  }

  public NClob getNClob(int arg0) throws SQLException
  {
    // TODO Auto-generated method stub
    return null;
  }

  public NClob getNClob(String arg0) throws SQLException
  {
    // TODO Auto-generated method stub
    return null;
  }

  public String getNString(int arg0) throws SQLException
  {
    // TODO Auto-generated method stub
    return null;
  }

  public String getNString(String arg0) throws SQLException
  {
    // TODO Auto-generated method stub
    return null;
  }

  public RowId getRowId(int arg0) throws SQLException
  {
    // TODO Auto-generated method stub
    return null;
  }

  public RowId getRowId(String arg0) throws SQLException
  {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isClosed() throws SQLException
  {
    // TODO Auto-generated method stub
    return false;
  }

  public void updateAsciiStream(String arg0, InputStream arg1)
      throws SQLException
  {
    // TODO Auto-generated method stub

  }

  public void updateBinaryStream(String arg0, InputStream arg1)
      throws SQLException
  {
    // TODO Auto-generated method stub

  }

  public void updateBlob(int arg0, InputStream arg1) throws SQLException
  {
    // TODO Auto-generated method stub

  }

  public void updateBlob(String arg0, InputStream arg1) throws SQLException
  {
    // TODO Auto-generated method stub

  }

  public void updateBlob(int arg0, InputStream arg1, long arg2)
      throws SQLException
  {
    // TODO Auto-generated method stub

  }

  public void updateBlob(String arg0, InputStream arg1, long arg2)
      throws SQLException
  {
    // TODO Auto-generated method stub

  }

  public void updateClob(int arg0, Reader arg1) throws SQLException
  {
    // TODO Auto-generated method stub

  }

  public void updateClob(String arg0, Reader arg1) throws SQLException
  {
    // TODO Auto-generated method stub

  }

  public void updateClob(int arg0, Reader arg1, long arg2) throws SQLException
  {
    // TODO Auto-generated method stub

  }

  public void updateClob(String arg0, Reader arg1, long arg2)
      throws SQLException
  {
    // TODO Auto-generated method stub

  }

  @Override
  public <T> T getObject(int columnIndex, Class<T> type) throws SQLException
  {
    throw new UnsupportedOperationException("CEPResultSet.getObject not supported yet.");
  }

  @Override
  public <T> T getObject(String columnLabel, Class<T> type) throws SQLException
  {
    throw new UnsupportedOperationException("CEPResultSet.getObject not supported yet.");
  }
}
