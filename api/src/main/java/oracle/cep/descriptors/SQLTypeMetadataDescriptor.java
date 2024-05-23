/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/descriptors/SQLTypeMetadataDescriptor.java /main/3 2009/08/31 10:56:49 alealves Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 skmishra    08/20/08 - changing package name
 skmishra    11/14/07 - 
 mthatte     11/04/07 - shared descriptor for describing Types in CEP
 mthatte     11/04/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/descriptors/SQLTypeMetadataDescriptor.java /main/3 2009/08/31 10:56:49 alealves Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.descriptors;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.SQLException;
import java.sql.Types;

import oracle.cep.common.Datatype;
import oracle.cep.jdbc.CEPDatabaseMetaData;

public class SQLTypeMetadataDescriptor extends MetadataDescriptor
{
  private static final long serialVersionUID = -8745019577921531077L;

  String  TYPE_NAME;         // TYPE_NAME String => Type name 
  int     DATA_TYPE;         //SQL data type from java.sql.Types 
  int     PRECISION;         //maximum precision 
  String  LITERAL_PREFIX;    //prefix used to quote a literal (may be null) 
  String  LITERAL_SUFFIX;    //suffix used to quote a literal (may be null) 
  String  CREATE_PARAMS;     //String => parameters used in creating the type (may 
  //be null) 
  short   NULLABLE;          // can you use NULL for this type. 
  boolean CASE_SENSITIVE;    //is it case sensitive. 
  short   SEARCHABLE;        //can you use "WHERE" based on this type: 
  boolean UNSIGNED_ATTRIBUTE; //is it unsigned. 
  boolean FIXED_PREC_SCALE;  // boolean => can it be a money value. 
  boolean AUTO_INCREMENT;    //boolean => can it be used for an auto-increment 
  //value. 
  String  LOCAL_TYPE_NAME;   // String => localized version of type name (may be 
  //null)	 
  short   MINIMUM_SCALE;     //short => minimum scale supported 
  short   MAXIMUM_SCALE;     // short => maximum scale supported 
  int     SQL_DATA_TYPE;     // int => unused 
  int     SQL_DATETIME_SUB;  // int => unused 
  int     NUM_PREC_RADIX;    // int => usually 2 or 10 

  public SQLTypeMetadataDescriptor()
  {
  }
  
  public SQLTypeMetadataDescriptor(Datatype dt)
  {
    this.TYPE_NAME = dt.toString();
    this.DATA_TYPE = dt.getSqlType();
    this.PRECISION = dt.getPrecision();
    this.NULLABLE = dt.getNullable();
    this.CASE_SENSITIVE = dt.isCaseSensitive();
    this.LITERAL_PREFIX = "\"";
    this.LITERAL_SUFFIX = " \"";
    this.NUM_PREC_RADIX = 10;
    this.SEARCHABLE = CEPDatabaseMetaData.typePredNone;
  }

  /*
   * Required for ResultSetMetadata
   * @see oracle.cep.shared.MetadataDescriptor#getColumnCount()
   */
  public int getColumnCount() throws SQLException
  {
    return 18;
  }

  public int getColumnDisplaySize(int column) throws SQLException
  {
    return 32;
  }

  public String getColumnClassName(int columnIndex) throws SQLException
  {

    switch (columnIndex)
    {

    case 1:
      return "java.lang.String";
    case 2:
      return "int";
    case 3:
      return "int";
    case 4:
      return "java.lang.String";
    case 5:
      return "java.lang.String";
    case 6:
      return "java.lang.String";
    case 7:
      return "short";
    case 8:
      return "boolean";
    case 9:
      return "short";
    case 10:
      return "boolean";
    case 11:
      return "boolean";
    case 12:
      return "boolean";
    case 13:
      return "java.lang.String";
    case 14:
      return "short";
    case 15:
      return "short";
    case 16:
      return "int";
    case 17:
      return "int";
    case 18:
      return "int";
    default:
      throw new SQLException("Bad args");
    }
  }

  public String getColumnLabel(int column) throws SQLException
  {
    return getColumnName(column);
  }

  public String getColumnName(int column) throws SQLException
  {
    switch (column)
    {
    case 1:
      return "TYPE_NAME";
    case 2:
      return "DATA_TYPE";
    case 3:
      return "PRECISION";
    case 4:
      return "LITERAL_PREFIX";
    case 5:
      return "LITERAL_SUFFIX";
    case 6:
      return "CREATE_PARAMS";
    case 7:
      return "NULLABLE";
    case 8:
      return "CASE_SENSITIVE";
    case 9:
      return "SEARCHABLE";
    case 10:
      return "UNSIGNED_ATTRIBUTE";
    case 11:
      return "FIXED_PREC_SCALE";
    case 12:
      return "AUTO_INCREMENT";
    case 13:
      return "LOCAL_TYPE_NAME";
    case 14:
      return "MINIMUM_SCALE";
    case 15:
      return "MAXIMUM_SCALE";
    case 16:
      return "SQL_DATA_TYPE";
    case 17:
      return "SQL_DATETIME_SUB";
    case 18:
      return "NUM_PREC_RADIX";
    default:
      throw new SQLException("Bad arguments.");
    }

  }

  public int getColumnType(int column) throws SQLException
  {
    switch (column)
    {
    case 1:
    case 4:
    case 5:
    case 6:
    case 13:
      return Types.VARCHAR;
    case 2:
    case 3:
    case 16:
    case 17:
    case 18:
      return Types.INTEGER;
    case 8:
    case 10:
    case 11:
    case 12:
      return Types.BOOLEAN;
    case 7:
    case 9:
    case 14:
    case 15:
      return Types.SMALLINT;
    default:
      throw new SQLException("Bad args");

    }

  }
  
  public String getColumnTypeName(int column) throws SQLException
  {
    switch (column)
    {
    case 1:
    case 4:
    case 5:
    case 6:
    case 13:
      return "String";
    case 2:
    case 3:
    case 16:
    case 17:
    case 18:
      return "int";
    case 8:
    case 10:
    case 11:
    case 12:
      return "boolean";
    case 7:
    case 9:
    case 14:
    case 15:
      return "short";
    default:
      throw new SQLException("Bad args");

    }
    
  }
  public Object getObject(int column) throws SQLException
  {
    switch (column)
    {
    case 1:
      return this.TYPE_NAME;
    case 2:
      return this.DATA_TYPE;
    case 3:
      return this.PRECISION;
    case 4:
      return this.LITERAL_PREFIX;
    case 5:
      return this.LITERAL_SUFFIX;
    case 6:
      return this.CREATE_PARAMS;
    case 7:
      return this.NULLABLE;
    case 8:
      return this.CASE_SENSITIVE;
    case 9:
      return this.SEARCHABLE;
    case 10:
      return this.UNSIGNED_ATTRIBUTE;
    case 11:
      return this.FIXED_PREC_SCALE;
    case 12:
      return this.AUTO_INCREMENT;
    case 13:
      return this.LOCAL_TYPE_NAME;
    case 14:
      return this.MINIMUM_SCALE;
    case 15:
      return this.MAXIMUM_SCALE;
    case 16:
      return this.SQL_DATA_TYPE;
    case 17:
      return this.SQL_DATETIME_SUB;
    case 18:
      return this.NUM_PREC_RADIX;
    default:
      throw new SQLException("Bad arguments.");
    }
    
  }

  //This is a table that describes all types. It has no name.
  public String getTableName(int column) throws SQLException
  {
    if (column > 0 && column < 19)
      return "";
    else
      throw new SQLException("Bad argument.");
  }

  public boolean isReadOnly(int column) throws SQLException
  {
    if (column > 0 && column < 19)
      return true;
    else
      throw new SQLException("Bad argument.");
  }

  public boolean isSearchable(int column) throws SQLException
  {
    return false;
  }

  public boolean isSigned(int column) throws SQLException
  {
    return false;
  }

  public boolean isWritable(int column) throws SQLException
  {
    return false;
  }

  public String getString(int column) throws SQLException
  {
    switch (column)
    {
    case 1:
      return this.TYPE_NAME;
    case 4:
      return this.LITERAL_PREFIX;
    case 5:
      return this.LITERAL_SUFFIX;
    case 6:
      return this.CREATE_PARAMS;
    case 13:
      return this.LOCAL_TYPE_NAME;
    default:
      throw new SQLException("Bad argument");
    }
  }

  public String getString(String colName) throws SQLException
  {
    if (colName.equals("TYPE_NAME"))
      return this.TYPE_NAME;
    else if (colName.equals("LITERAL_PREFIX"))
      return this.LITERAL_PREFIX;
    else if (colName.equals("LITERAL_SUFFIX"))
      return this.LITERAL_SUFFIX;
    else if (colName.equals("LOCAL_TYPE_NAME"))
      return this.LOCAL_TYPE_NAME;
    else
      throw new SQLException("Bad args");

  }

  public int getInt(int column) throws SQLException
  {
    switch (column)
    {
    case 2:
      return this.DATA_TYPE;
    case 3:
      return this.PRECISION;
    case 16:
      return this.SQL_DATA_TYPE;
    case 17:
      return this.SQL_DATETIME_SUB;
    case 18:
      return this.NUM_PREC_RADIX;
    default:
      throw new SQLException("Bad args");
    }
  }

  public int getInt(String column) throws SQLException
  {
    if (column.equals("DATA_TYPE"))
      return this.DATA_TYPE;
    else if (column.equals("PRECISION"))
      return this.PRECISION;
    else if (column.equals("SQL_DATA_TYPE"))
      return this.SQL_DATA_TYPE;
    else if (column.equals("SQL_DATETIME_SUB"))
      return this.SQL_DATETIME_SUB;
    else if (column.equals("NUM_PREC_RADIX"))
      return this.NUM_PREC_RADIX;
    else
      throw new SQLException("Bad args");
  }

  public short getShort(int column) throws SQLException
  {
    switch (column)
    {
    case 7:
      return this.NULLABLE;
    case 9:
      return this.SEARCHABLE;
    case 14:
      return this.MINIMUM_SCALE;
    case 15:
      return this.MAXIMUM_SCALE;
    default:
      throw new SQLException("Bad args");
    }
  }

  public short getShort(String column) throws SQLException
  {
    if (column.equals("NULLABLE"))
      return this.NULLABLE;
    else if (column.equals("SEARCHABLE"))
      return this.SEARCHABLE;
    else if (column.equals("MINIMUM_SCALE"))
      return this.MINIMUM_SCALE;
    else if (column.equals("MAXIMUM_SCALE"))
      return this.MAXIMUM_SCALE;
    else
      throw new SQLException("Bad args");
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
      out.writeShort(NULLABLE);
      out.writeShort(SEARCHABLE);
      out.writeShort(MINIMUM_SCALE);
      out.writeShort(MAXIMUM_SCALE);

      out.writeInt(DATA_TYPE);
      out.writeInt(PRECISION);
      out.writeInt(SQL_DATA_TYPE);
      out.writeInt(SQL_DATETIME_SUB);
      out.writeInt(NUM_PREC_RADIX);

      out.writeBoolean(CASE_SENSITIVE);
      out.writeBoolean(UNSIGNED_ATTRIBUTE);
      out.writeBoolean(FIXED_PREC_SCALE);
      out.writeBoolean(AUTO_INCREMENT);

      out.writeObject(TYPE_NAME);
      out.writeObject(LITERAL_PREFIX);
      out.writeObject(LITERAL_SUFFIX);
      out.writeObject(LOCAL_TYPE_NAME);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
      NULLABLE = in.readShort();
      SEARCHABLE = in.readShort();
      MINIMUM_SCALE = in.readShort();
      MAXIMUM_SCALE = in.readShort();

      DATA_TYPE = in.readInt();
      PRECISION = in.readInt();
      SQL_DATA_TYPE = in.readInt();
      SQL_DATETIME_SUB = in.readInt();
      NUM_PREC_RADIX = in.readInt();

      CASE_SENSITIVE = in.readBoolean();
      UNSIGNED_ATTRIBUTE = in.readBoolean();
      FIXED_PREC_SCALE = in.readBoolean();
      AUTO_INCREMENT = in.readBoolean();

      TYPE_NAME = (String)in.readObject();
      LITERAL_PREFIX = (String)in.readObject();
      LITERAL_SUFFIX = (String)in.readObject();
      LOCAL_TYPE_NAME = (String)in.readObject();
  }

}
