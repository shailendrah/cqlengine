/* $Header: pcbpel/cep/common/src/oracle/cep/descriptors/ColumnMetadataDescriptor.java /main/3 2008/09/10 14:06:32 skmishra Exp $ */

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
 mthatte     10/09/07 - adding precision and scale methods
 mthatte     09/25/07 - Adding more info in descriptor
 mthatte     08/27/07 - To describe column metadata. Descriptor shared by
 client & server
 mthatte     08/27/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/descriptors/ColumnMetadataDescriptor.java /main/3 2008/09/10 14:06:32 skmishra Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.descriptors;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;

public class ColumnMetadataDescriptor extends MetadataDescriptor
{
  private static final long serialVersionUID = -8745019577921531077L;

  String TABLE_CAT      = null;                           /*
                                                           * table catalog (may
                                                           * be null)
                                                           */
  String TABLE_SCHEM    = null;                           /*
                                                           * table schema (may
                                                           * be null)
                                                           */
  String TABLE_NAME;                                      /* table name */
  String COLUMN_NAME;                                     /* column name */

  int    DATA_TYPE;                                       /*
                                                           * SQL type from
                                                           * java.sql.Types
                                                           */

  String TYPE_NAME      = "";                             /*
                                                           * Data source
                                                           * dependent type
                                                           * name, for a UDT the
                                                           * type name is fully
                                                           * qualified
                                                           */
  int    COLUMN_SIZE;                                     /*
                                                           * column size. For
                                                           * char or date types
                                                           * this is the maximum
                                                           * number of
                                                           * characters, for
                                                           * numeric or decimal
                                                           * types this is
                                                           * precision.
                                                           */
  int    BUFFER_LENGTH;                                   /* is not used. */
  int    DECIMAL_DIGITS;                                  /*
                                                           * the number of
                                                           * fractional digits
                                                           */
  int    NUM_PREC_RADIX = 10;                             /*
                                                           * Radix (typically
                                                           * either 10 or 2)
                                                           */
  int    NULLABLE       = DatabaseMetaData.columnNullable;
  /*
   * is NULL allowed. columnNoNulls - might not allow <code>NULL</code> values
   * columnNullable - definitely allows <code>NULL</code> values
   * columnNullableUnknown - nullability unknown
   */
  String REMARKS        = "N/A";                          /*
                                                           * comment describing
                                                           * column (may be
                                                           * null)
                                                           */
  String COLUMN_DEF     = null;                           /*
                                                           * default value (may
                                                           * be null)
                                                           */

  int    SQL_DATA_TYPE;                                   /* unused */
  int    SQL_DATETIME_SUB;                                /* unused */
  int    CHAR_OCTET_LENGTH;                               /*
                                                           * for char types the
                                                           * maximum number of
                                                           * bytes in the column
                                                           */
  int    ORDINAL_POSITION;                                /*
                                                           * index of column in
                                                           * table (starting at
                                                           * 1)
                                                           */

  String IS_NULLABLE    = "YES";                          /*
                                                           * "NO" means column
                                                           * definitely does not
                                                           * allow NULL values;
                                                           * "YES" means the
                                                           * column might allow
                                                           * NULL values. An
                                                           * empty string means
                                                           * nobody knows.
                                                           */
  String SCOPE_CATLOG   = "";                             /*
                                                           * catalog of table
                                                           * that is the scope
                                                           * of a reference
                                                           * attribute (if
                                                           * DATA_TYPE isn't
                                                           * REF)
                                                           */
  String SCOPE_SCHEMA   = null;                           /*
                                                           * schema of table
                                                           * that is the scope
                                                           * of a reference
                                                           * attribute (<code>null</code>
                                                           * if the DATA_TYPE
                                                           * isn't REF)
                                                           */
  String SCOPE_TABLE    = null;                           /*
                                                           * table name that
                                                           * this the scope of a
                                                           * reference attribute
                                                           * (null if the
                                                           * DATA_TYPE isn't
                                                           * REF)
                                                           */

  short  SOURCE_DATA_TYPE;                                /*
                                                           * source type of a
                                                           * distinct type or
                                                           * user-generated Ref
                                                           * type, SQL type from
                                                           * java.sql.Types
                                                           * (null if DATA_TYPE
                                                           * isn't DISTINCT or
                                                           * user-generated REF)
                                                           */

  public ColumnMetadataDescriptor()
  {
  }
  
  // Constructor
  public ColumnMetadataDescriptor(String table_name, String column_name,
      int data_type, int column_size, String type_name)
  {
    super();
    TABLE_NAME = table_name;
    COLUMN_NAME = column_name;
    DATA_TYPE = data_type;
    COLUMN_SIZE = column_size;
    SQL_DATA_TYPE = data_type;
    BUFFER_LENGTH = column_size;
    TYPE_NAME = type_name;
  }

  /*
   * Required for ResultSetMetadata
   */

  public int getColumnCount() throws SQLException
  {
    return 22;
  }

  public String getColumnClassName(int columnIndex) throws SQLException
  {
    if (columnIndex > 0 && columnIndex < 5)
      return "java.lang.String";
    if (columnIndex == 5)
      return "java.lang.Integer";
    if (columnIndex == 6)
      return "java.lang.String";
    else if (columnIndex > 6 && columnIndex < 12)
      return "java.lang.Integer";
    else if (columnIndex > 11 && columnIndex < 14)
      return "java.lang.String";
    else if (columnIndex > 13 && columnIndex < 18)
      return "java.lang.Integer";
    else if (columnIndex > 17 && columnIndex < 22)
      return "java.lang.String";
    else if (columnIndex == 22)
      return "java.lang.Short";
    else
      throw new SQLException("Bad arguments");
  }

  public int getColumnDisplaySize(int column) throws SQLException
  {
    return 20;
  }
  
  public String getColumnName(int column) throws SQLException
  {
    switch (column)
    {

    case 1:
      return "TABLE_CAT";
    case 2:
      return "TABLE_SCHEM";
    case 3:
      return "TABLE_NAME";
    case 4:
      return "COLUMN_NAME";
    case 5:
      return "DATA_TYPE";
    case 6:
      return "TYPE_NAME";
    case 7:
      return "COLUMN_SIZE";
    case 8:
      return "BUFFER_LENGTH";
    case 9:
      return "DECIMAL_DIGITS";
    case 10:
      return "NUM_PREC_RADIX";
    case 11:
      return "NULLABLE";
    case 12:
      return "REMARKS";
    case 13:
      return "COLUMN_DEF";
    case 14:
      return "SQL_DATA_TYPE";
    case 15:
      return "SQL_DATETIME_SUB";
    case 16:
      return "CHAR_OCTET_LENGTH";
    case 17:
      return "ORDINAL_POSITION";
    case 18:
      return "IS_NULLABLE";
    case 19:
      return "SCOPE_CATLOG";
    case 20:
      return "SCOPE_SCHEMA";
    case 21:
      return "SCOPE_TABLE";
    case 22:
      return "SOURCE_DATA_TYPE";
    default:
      throw new SQLException("Bad arguments.");
    }
  }

  public String getColumnLabel(int column) throws SQLException
  {

    switch (column)
    {
    case 1:
      return "TABLE_CAT";
    case 2:
      return "TABLE_SCHEM";
    case 3:
      return "TABLE_NAME";
    case 4:
      return "COLUMN_NAME";
    case 5:
      return "DATA_TYPE";
    case 6:
      return "TYPE_NAME";
    case 7:
      return "COLUMN_SIZE";
    case 8:
      return "BUFFER_LENGTH";
    case 9:
      return "DECIMAL_DIGITS";
    case 10:
      return "NUM_PREC_RADIX";
    case 11:
      return "NULLABLE";
    case 12:
      return "REMARKS";
    case 13:
      return "COLUMN_DEF";
    case 14:
      return "SQL_DATA_TYPE";
    case 15:
      return "SQL_DATETIME_SUB";
    case 16:
      return "CHAR_OCTET_LENGTH";
    case 17:
      return "ORDINAL_POSITION";
    case 18:
      return "IS_NULLABLE";
    case 19:
      return "SCOPE_CATLOG";
    case 20:
      return "SCOPE_SCHEMA";
    case 21:
      return "SCOPE_TABLE";
    case 22:
      return "SOURCE_DATA_TYPE";
    default:
      throw new SQLException("Bad arguments.");
    }
  }

  public int getColumnType(int columnIndex) throws SQLException
  {
    switch (columnIndex)
    {
    case 1:
    case 2:
    case 3:
    case 4:
    case 6:
    case 12:
    case 13:
    case 18:
    case 19:
    case 20:
    case 21:
      return Types.VARCHAR;
    case 22:
      return Types.SMALLINT;
    case 5:
    case 7:
    case 8:
    case 9:
    case 10:
    case 11:
    case 14:
    case 15:
    case 16:
    case 17:
      return Types.INTEGER;
    default:
      throw new SQLException("Bad arguments");
    }
  }

  public String getColumnTypeName(int column) throws SQLException
  {
    switch (column)
    {
    case 1:
    case 2:
    case 3:
    case 4:
    case 6:
    case 12:
    case 13:
    case 18:
    case 19:
    case 20:
    case 21:
      return "String";
    case 22:
      return "short";
    case 5:
    case 7:
    case 8:
    case 9:
    case 10:
    case 11:
    case 14:
    case 15:
    case 16:
    case 17:
      return "int";
    default:
      throw new SQLException("Bad arguments");
    }

  }

  public Object getObject(int column) throws SQLException
  {

    switch (column)
    {

    case 1:
      return this.TABLE_CAT;
    case 2:
      return this.TABLE_SCHEM;
    case 3:
      return this.TABLE_NAME;
    case 4:
      return this.COLUMN_NAME;
    case 5:
      return this.DATA_TYPE;
    case 6:
      return this.TYPE_NAME;
    case 7:
      return this.COLUMN_SIZE;
    case 8:
      return this.BUFFER_LENGTH;
    case 9:
      return this.DECIMAL_DIGITS;
    case 10:
      return this.NUM_PREC_RADIX;
    case 11:
      return this.NULLABLE;
    case 12:
      return this.REMARKS;
    case 13:
      return this.COLUMN_DEF;
    case 14:
      return this.SQL_DATA_TYPE;
    case 15:
      return this.SQL_DATETIME_SUB;
    case 16:
      return this.CHAR_OCTET_LENGTH;
    case 17:
      return this.ORDINAL_POSITION;
    case 18:
      return this.IS_NULLABLE;
    case 19:
      return this.SCOPE_CATLOG;
    case 20:
      return this.SCOPE_SCHEMA;
    case 21:
      return this.SCOPE_TABLE;
    case 22:
      return this.SOURCE_DATA_TYPE;
    default:
      throw new SQLException("Bad arguments.");
    }

  }

  public void setOrdinalPosition(int ordinal_position)
  {
    this.ORDINAL_POSITION = ordinal_position;
  }

  // This is a table that describes columns in a table. It has no name.
  public String getTableName(int column) throws SQLException
  {
    if (column > 0 && column < 22)
      return "";
    else
      throw new SQLException("Bad argument.");
  }

  public boolean isCurrency(int column) throws SQLException 
  {
    if (column > 0 && column < 22)
      return false;
    else
      throw new SQLException("Bad argument.");
  }
  
  
  @Override
  public boolean isAutoIncrement(int column) throws SQLException
  {
    if (column > 0 && column < 22)
      return false;
    else
      throw new SQLException("Bad argument.");
  }

  public boolean isReadOnly(int column) throws SQLException
  {
    if (column > 0 && column < 23)
      return true;
    else
      throw new SQLException("Bad argument.");
  }

  public boolean isSearchable(int column) throws SQLException
  {
    if (column > 0 && column < 22)
      return false;
    else
      throw new SQLException("Bad argument.");
  }

  public boolean isSigned(int column) throws SQLException
  {
    if (column > 0 && column < 22)
      return false;
    else
      throw new SQLException("Bad argument.");
  }

  public boolean isWritable(int column) throws SQLException
  {
    if (column > 0 && column < 22)
      return false;
    else
      throw new SQLException("Bad argument.");
  }

  /*
   * End impl. for ResultSetMetadata
   */

  public String getString(int columnIndex) throws SQLException
  {

    switch (columnIndex)
    {
    case 1:
      return this.TABLE_CAT;
    case 2:
      return this.TABLE_SCHEM;
    case 3:
      return this.TABLE_NAME;
    case 4:
      return this.COLUMN_NAME;
    case 6:
      return this.TYPE_NAME;

    case 12:
      return this.REMARKS;
    case 13:
      return this.COLUMN_DEF;

    case 18:
      return this.IS_NULLABLE;

    case 19:
      return this.SCOPE_CATLOG;
    case 20:
      return this.SCOPE_SCHEMA;
    case 21:
      return this.SCOPE_TABLE;
    default:
      throw new SQLException("Bad argument.");
    }
  }

  public String getString(String columnName) throws SQLException
  {
    if (columnName.equals("TABLE_CAT"))
      return this.TABLE_CAT;
    else if (columnName.equals("TABLE_SCHEM"))
      return this.TABLE_SCHEM;
    else if (columnName.equals("TABLE_NAME"))
      return this.TABLE_NAME;
    else if (columnName.equals("COLUMN_NAME"))
      return this.COLUMN_NAME;
    else if (columnName.equals("TYPE_NAME"))
      return this.TYPE_NAME;
    else if (columnName.equals("REMARKS"))
      return this.REMARKS;
    else if (columnName.equals("COLUMN_DEF"))
      return this.COLUMN_DEF;
    else if (columnName.equals("IS_NULLABLE"))
      return this.IS_NULLABLE;
    else if (columnName.equals("SCOPE_CATALOG"))
      return this.SCOPE_CATLOG;
    else if (columnName.equals("SCOPE_SCHEMA"))
      return this.SCOPE_SCHEMA;
    else if (columnName.equals("SCOPE_TABLE"))
      return this.SCOPE_TABLE;
    else
      throw new SQLException("Bad argument.");
  }

  public int getInt(int columnIndex) throws SQLException
  {

    switch (columnIndex)
    {
    case 5:
      return this.DATA_TYPE;
    case 7:
      return this.COLUMN_SIZE;
    case 8:
      return this.BUFFER_LENGTH;
    case 9:
      return this.DECIMAL_DIGITS;
    case 10:
      return this.NUM_PREC_RADIX;
    case 11:
      return this.NULLABLE;
    case 14:
      return this.SQL_DATA_TYPE;
    case 15:
      return this.SQL_DATETIME_SUB;
    case 16:
      return this.CHAR_OCTET_LENGTH;
    case 17:
      return this.ORDINAL_POSITION;
    default:
      throw new SQLException("Bad argument.");
    }
  }

  public int getInt(String columnName) throws SQLException
  {
    if (columnName.equals("DATA_TYPE"))
      return this.DATA_TYPE;
    else if (columnName.equals("COLUMN_SIZE"))
      return this.COLUMN_SIZE;
    else if (columnName.equals("BUFFER_LENGTH"))
      return this.BUFFER_LENGTH;
    else if (columnName.equals("DECIMAL_DIGITS"))
      return this.DECIMAL_DIGITS;
    else if (columnName.equals("NUM_PREC_RADIX"))
      return this.NUM_PREC_RADIX;
    else if (columnName.equals("NULLABLE"))
      return this.NULLABLE;
    else if (columnName.equals("SQL_DATA_TYPE"))
      return this.SQL_DATA_TYPE;
    else if (columnName.equals("SQL_DATETIME_SUB"))
      return this.SQL_DATETIME_SUB;
    else if (columnName.equals("CHAR_OCTET_LENGTH"))
      return this.CHAR_OCTET_LENGTH;
    else if (columnName.equals("ORDINAL_POSITION"))
      return this.ORDINAL_POSITION;
    else
      throw new SQLException("Bad argument.");
  }

  public short getShort(int columnIndex) throws SQLException
  {
    if (columnIndex == 22)
      return this.SOURCE_DATA_TYPE;
    else
      throw new SQLException("Bad argument");
  }

  public short getShort(String columnName) throws SQLException
  {
    if (columnName.equals("SOURCE_DATA_TYPE"))
      return this.SOURCE_DATA_TYPE;
    else
      throw new SQLException("Bad argument");
  }

  @Override
  public int getPrecision(int column) throws SQLException
  {
    return 5;

  }

  @Override
  public int getScale(int column) throws SQLException
  {
    return 5;
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
     out.writeInt(DATA_TYPE);
     out.writeInt(COLUMN_SIZE);
     out.writeInt(BUFFER_LENGTH);
     out.writeInt(DECIMAL_DIGITS);
     out.writeInt(NUM_PREC_RADIX);
     out.writeInt(NULLABLE);
     out.writeInt(SQL_DATA_TYPE);
     out.writeInt(SQL_DATETIME_SUB);
     out.writeInt(CHAR_OCTET_LENGTH);
     out.writeInt(ORDINAL_POSITION);
     out.writeShort(SOURCE_DATA_TYPE);
     out.writeObject(TABLE_CAT);
     out.writeObject(TABLE_SCHEM);
     out.writeObject(TABLE_NAME);
     out.writeObject(COLUMN_NAME);
     out.writeObject(TYPE_NAME);
     out.writeObject(REMARKS);
     out.writeObject(COLUMN_DEF);
     out.writeObject(IS_NULLABLE);
     out.writeObject(SCOPE_CATLOG);
     out.writeObject(SCOPE_SCHEMA);
     out.writeObject(SCOPE_TABLE);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
  {
      DATA_TYPE = in.readInt();
      COLUMN_SIZE = in.readInt();
      BUFFER_LENGTH = in.readInt();
      DECIMAL_DIGITS = in.readInt();
      NUM_PREC_RADIX = in.readInt();
      NULLABLE = in.readInt();
      SQL_DATA_TYPE = in.readInt();
      SQL_DATETIME_SUB = in.readInt();
      CHAR_OCTET_LENGTH = in.readInt();
      ORDINAL_POSITION = in.readInt();
      SOURCE_DATA_TYPE = in.readShort();
      TABLE_CAT      = (String)in.readObject();
      TABLE_SCHEM      = (String)in.readObject();
      TABLE_NAME      = (String)in.readObject();
      COLUMN_NAME      = (String)in.readObject();
      TYPE_NAME      = (String)in.readObject();
      REMARKS      = (String)in.readObject();
      COLUMN_DEF      = (String)in.readObject();
      IS_NULLABLE      = (String)in.readObject();
      SCOPE_CATLOG      = (String)in.readObject();
      SCOPE_SCHEMA      = (String)in.readObject();
      SCOPE_TABLE      = (String)in.readObject();
  }

}
