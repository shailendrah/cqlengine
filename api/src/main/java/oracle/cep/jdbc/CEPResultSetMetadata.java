/* $Header: pcbpel/cep/common/src/oracle/cep/jdbc/CEPResultSetMetadata.java /main/1 2008/09/10 14:06:32 skmishra Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 skmishra    07/30/08 - 
 mthatte     10/05/07 - Adding support for select
 mthatte     09/13/07 - Adding methods to make compliant with Java 6
 parujain    05/09/07 - 
 najain      04/19/07 - Creation
 */

/**
 * 	This class describes the corresponding CEPResultSet.	 
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/jdbc/CEPResultSetMetadata.java /main/1 2008/09/10 14:06:32 skmishra Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.jdbc;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

// Generic impl. of ResultSetMetadata. Asks the descriptor of its ResultSet
// to describe itself.
public class CEPResultSetMetadata implements ResultSetMetaData
{
  // Which result set is this metadata about?
  private CEPResultSet resultSet;

  public CEPResultSetMetadata(CEPResultSet rs)
  {
    this.resultSet = rs;
  }

  public String getCatalogName(int column) throws SQLException
  {
    return resultSet.getDescriptor().getCatalogName(column);
  }

  public String getColumnClassName(int column) throws SQLException
  {
    return resultSet.getDescriptor().getColumnClassName(column);
  }

  public int getColumnCount() throws SQLException
  {
    return resultSet.getDescriptor().getColumnCount();
  }

  public int getColumnDisplaySize(int column) throws SQLException
  {
    return resultSet.getDescriptor().getColumnDisplaySize(column);
  }

  public String getColumnLabel(int column) throws SQLException
  {
    return resultSet.getDescriptor().getColumnLabel(column);
  }

  public String getColumnName(int column) throws SQLException
  {
    return resultSet.getDescriptor().getColumnName(column);
  }

  public int getColumnType(int column) throws SQLException
  {
    return resultSet.getDescriptor().getColumnType(column);
  }

  public String getColumnTypeName(int column) throws SQLException
  {
    return resultSet.getDescriptor().getColumnTypeName(column);
  }

  public int getPrecision(int column) throws SQLException
  {
    return resultSet.getDescriptor().getPrecision(column);
  }

  public int getScale(int column) throws SQLException
  {
    return resultSet.getDescriptor().getScale(column);
  }

  public String getSchemaName(int column) throws SQLException
  {
    return resultSet.getDescriptor().getSchemaName(column);
  }

  public String getTableName(int column) throws SQLException
  {
    return resultSet.getDescriptor().getTableName(column);
  }

  public boolean isAutoIncrement(int column) throws SQLException
  {
    return resultSet.getDescriptor().isAutoIncrement(column);
  }

  public boolean isCaseSensitive(int column) throws SQLException
  {
    return resultSet.getDescriptor().isCaseSensitive(column);
  }

  public boolean isCurrency(int column) throws SQLException
  {
    return resultSet.getDescriptor().isCurrency(column);
  }

  public boolean isDefinitelyWritable(int column) throws SQLException
  {
    return resultSet.getDescriptor().isDefinitelyWritable(column);
  }

  public int isNullable(int column) throws SQLException
  {
    return resultSet.getDescriptor().isNullable(column);
  }

  public boolean isReadOnly(int column) throws SQLException
  {
    return resultSet.getDescriptor().isReadOnly(column);
  }

  public boolean isSearchable(int column) throws SQLException
  {
    return resultSet.getDescriptor().isSearchable(column);
  }

  public boolean isSigned(int column) throws SQLException
  {
    return resultSet.getDescriptor().isSigned(column);
  }

  public boolean isWritable(int column) throws SQLException
  {
    return resultSet.getDescriptor().isWritable(column);
  }

  public boolean isWrapperFor(Class<?> iface) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSetMetadata.isWrapperFor(Class<?>) not supported");
  }

  public <T> T unwrap(Class<T> iface) throws SQLException
  {
    throw new UnsupportedOperationException(
        "CEPResultSetMetadata.unwrap(Class<T>) not supported");
  }
}