/* $Header: pcbpel/cep/common/src/oracle/cep/descriptors/SelectResultMetadataDescriptor.java /main/3 2008/09/10 14:06:32 skmishra Exp $ */

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
 mthatte     11/06/07 - adding getColumnType()
 mthatte     10/04/07 - Result for a dummy select statement
 mthatte     10/04/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/descriptors/SelectResultMetadataDescriptor.java /main/3 2008/09/10 14:06:32 skmishra Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.descriptors;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Externalizable;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;

public class SelectResultMetadataDescriptor extends MetadataDescriptor
{

  private class ColumnDescriptor implements Externalizable
  {
    private static final long serialVersionUID = 4794693207604300226L;

    String columnName;
    String tableName;
    String columnClassName;
    int    length;
    int    columnType;
    int    precision;

    public ColumnDescriptor()
    {
    }

    public ColumnDescriptor(String tableName, String columnName,
        String columnClassName, int length, int type, int precision)
    {
      super();
      this.columnName = columnName;
      this.tableName = tableName;
      this.columnClassName = columnClassName;
      this.length = length;
      this.columnType = type;
      this.precision = precision;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
         out.writeInt(length);
         out.writeInt(columnType);
         out.writeInt(precision);
         out.writeObject(columnName);
         out.writeObject(tableName);
         out.writeObject(columnClassName);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        length = in.readInt();
        columnType = in.readInt();
        precision = in.readInt();
        columnName = (String)in.readObject();
        tableName = (String)in.readObject();
        columnClassName = (String)in.readObject();
    }

  }

  private static final long serialVersionUID = -8745019577921531077L;
  List<ColumnDescriptor> columnList = null;

  public SelectResultMetadataDescriptor()
  {
    super();
    this.columnList = new LinkedList<ColumnDescriptor>();
  }

  private String intToType(int sql_type)
  {
    switch (sql_type)
    {
    case Types.INTEGER:
      return "int";
    case Types.BIGINT:
      return "bigint";
    case Types.FLOAT:
      return "float";
    case Types.CHAR:
      return "char";
    case Types.BOOLEAN:
      return "boolean";
    case Types.TIMESTAMP:
      return "timestamp";
    case Types.OTHER:
      return "byte";
    }

    return null;
  }

  public void addColumn(String tableName, String colName, int colType,
      int colSize)
  {
    int precision = 0;
    switch (colType)
    {
    case Types.FLOAT:
      precision = 23;
      break;
    case Types.DOUBLE:
      precision = 46;
      break;
    case Types.INTEGER:
      precision = 0;
      break;
    case Types.BIGINT:
      precision = 0;
      break;
    default:
      precision = 0;
    }
    ColumnDescriptor cd = new ColumnDescriptor(tableName, colName,
        intToType(colType), colSize, colType, precision);
    columnList.add(cd);
  }

  public String getColumnClassName(int column) throws SQLException
  {
    try
    {
      ColumnDescriptor cd = columnList.get(column - 1);
      return cd.columnClassName;
    }

    catch (IndexOutOfBoundsException e)
    {
      throw new SQLException("Bad argument for getColumnClassName, index = "
          + column);
    }

  }

  public int getColumnCount() throws SQLException
  {
    return columnList.size();
  }

  public int getColumnDisplaySize(int column) throws SQLException
  {
    try
    {
      ColumnDescriptor cd = columnList.get(column - 1);
      return cd.length;
    }

    catch (IndexOutOfBoundsException e)
    {
      throw new SQLException("Bad argument for getColumnDispSize, index = "
          + column);
    }
  }

  public String getColumnLabel(int column) throws SQLException
  {
    try
    {
      ColumnDescriptor cd = columnList.get(column - 1);
      return cd.columnName;
    }

    catch (IndexOutOfBoundsException e)
    {
      throw new SQLException("Bad argument for getColumnLabel, index = "
          + column);
    }
  }

  public String getColumnName(int column) throws SQLException
  {
    try
    {
      ColumnDescriptor cd = columnList.get(column - 1);
      return cd.columnName;
    }

    catch (IndexOutOfBoundsException e)
    {
      throw new SQLException("Bad argument for getColumnName, index = "
          + column);
    }
  }

  public int getColumnType(int column) throws SQLException
  {
    try
    {
      ColumnDescriptor cd = columnList.get(column - 1);
      return cd.columnType;
    }

    catch (IndexOutOfBoundsException e)
    {
      throw new SQLException("Bad argument for getColumnType, index = "
          + column);
    }
  }

  public String getColumnTypeName(int column) throws SQLException
  {

    try
    {
      ColumnDescriptor cd = columnList.get(column - 1);

      return intToType(cd.columnType);
    }

    catch (IndexOutOfBoundsException e)
    {
      throw new SQLException("Bad argument for getColumnTypeName, index = "
          + column);
    }
  }

  public String getTableName(int column) throws SQLException
  {

    try
    {
      ColumnDescriptor cd = columnList.get(column - 1);
      return cd.tableName;
    }

    catch (IndexOutOfBoundsException e)
    {
      throw new SQLException("Bad argument for getTableName, index = " + column);
    }
  }

  @Override
  public int getPrecision(int column) throws SQLException
  {
    try
    {
      ColumnDescriptor cd = columnList.get(column - 1);
      return cd.precision;
    }

    catch (IndexOutOfBoundsException e)
    {
      throw new SQLException("Bad argument for getPrecision, index = " + column);
    }
  }

  @Override
  public int getScale(int column) throws SQLException
  {
    try
    {
      ColumnDescriptor cd = columnList.get(column - 1);
      return cd.precision;
    }

    catch (IndexOutOfBoundsException e)
    {
      throw new SQLException("Bad argument for getScale, index = " + column);
    }
  }

  public boolean isAutoIncrement(int column) throws SQLException

  {
    try
    {
      ColumnDescriptor cd = columnList.get(column - 1);
      return false;
    }

    catch (IndexOutOfBoundsException e)
    {
      throw new SQLException("Bad argument for isAutoIncrement, index = "
          + column);
    }
  }

  public boolean isCaseSensitive(int column) throws SQLException
  {
    try
    {
      ColumnDescriptor cd = columnList.get(column - 1);
      return false;
    }

    catch (IndexOutOfBoundsException e)
    {
      throw new SQLException("Bad argument for isCaseSensitive, index = "
          + column);
    }
  }

  public boolean isCurrency(int column) throws SQLException
  {
    try
    {
      ColumnDescriptor cd = columnList.get(column - 1);
      return false;
    }

    catch (IndexOutOfBoundsException e)
    {
      throw new SQLException("Bad argument for isCurrency, index = " + column);
    }
  }

  public boolean isDefinitelyWritable(int column) throws SQLException
  {
    try
    {
      ColumnDescriptor cd = columnList.get(column - 1);
      return true;
    }

    catch (IndexOutOfBoundsException e)
    {
      throw new SQLException("Bad argument for isDefinitelyWritable, index = "
          + column);
    }
  }

  public int isNullable(int column) throws SQLException
  {
    try
    {
      ColumnDescriptor cd = columnList.get(column - 1);
      return java.sql.ResultSetMetaData.columnNullableUnknown;
    }

    catch (IndexOutOfBoundsException e)
    {
      throw new SQLException("Bad argument for isNullable, index = " + column);
    }
  }

  public boolean isReadOnly(int column) throws SQLException
  {
    try
    {
      ColumnDescriptor cd = columnList.get(column - 1);
      return false;
    }

    catch (IndexOutOfBoundsException e)
    {
      throw new SQLException("Bad argument for isReadOnly, index = " + column);
    }
  }

  public boolean isSearchable(int column) throws SQLException
  {
    try
    {
      ColumnDescriptor cd = columnList.get(column - 1);
      return false;
    }

    catch (IndexOutOfBoundsException e)
    {
      throw new SQLException("Bad argument for isSearchable, index = " + column);
    }

  }

  public boolean isSigned(int column) throws SQLException
  {
    try
    {
      ColumnDescriptor cd = columnList.get(column - 1);
      return false;
    }

    catch (IndexOutOfBoundsException e)
    {
      throw new SQLException("Bad argument for isSigned, index = " + column);
    }
  }

  public boolean isWritable(int column) throws SQLException
  {
    try
    {
      ColumnDescriptor cd = columnList.get(column - 1);
      return true;
    }

    catch (IndexOutOfBoundsException e)
    {
      throw new SQLException("Bad argument for isWritable, index = " + column);
    }
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
      int n = columnList.size();
      out.writeInt(n);
      for (int i = 0; i < n; i++) {
          ColumnDescriptor c = columnList.get(i);
          out.writeObject(c);
      }
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
      int n = in.readInt();
      for (int i = 0; i < n; i++) {
          ColumnDescriptor c = (ColumnDescriptor)in.readObject();
          columnList.add(c);
      }
  }

}
