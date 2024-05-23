/* $Header: pcbpel/cep/common/src/oracle/cep/descriptors/UDTMetadataDescriptor.java /main/2 2008/09/10 14:06:32 skmishra Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.*/

/*
 DESCRIPTION
 Retrieves a description of the user-defined types (UDTs) defined in a particular 
 schema. Schema-specific UDTs may have type JAVA_OBJECT, STRUCT, or 
 DISTINCT. 
 Only types matching the catalog, schema, type name and type criteria are 
 returned. They are ordered by DATA_TYPE, TYPE_CAT, TYPE_SCHEM and 
 TYPE_NAME. The type name parameter may be a fully-qualified name. In this 
 case, the catalog and schemaPattern parameters are ignored. 

 Note: If the driver does not support UDTs, an empty result set is returned. 

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 skmishra    08/20/08 - changing package name
 skmishra    11/14/07 - 
 mthatte     11/06/07 - descriptor for UDT's
 mthatte     11/06/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/descriptors/UDTMetadataDescriptor.java /main/2 2008/09/10 14:06:32 skmishra Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.descriptors;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.SQLException;
import java.sql.Types;

public class UDTMetadataDescriptor extends MetadataDescriptor
{
  private static final long serialVersionUID = -8745019577921531077L;

  String TYPE_CAT;  // the type's catalog (may be null)
  String TYPE_SCHEM; // type's schema (may be null)
  String TYPE_NAME; // type name
  String CLASS_NAME; // Java class name
  int    DATA_TYPE; // type value defined in java.sql.Types. One of
  // JAVA_OBJECT, STRUCT, or DISTINCT
  String REMARKS;   // explanatory comment on the type
  short  BASE_TYPE; // type code of the source type of a DISTINCT type

  // or the type that implements the user-generated
  // reference type of the SELF_REFERENCING_COLUMN of
  // a
  // structured type as defined in java.sql.Types
  // (null if DATA_TYPE is not DISTINCT or not STRUCT
  // with
  // REFERENCE_GENERATION = USER_DEFINED)

  public UDTMetadataDescriptor()
  {
    this.TYPE_CAT = "Oracle-CEP";
    this.TYPE_SCHEM = "sys";
  }

  /*
   * Required for ResultSetMetadata
   * 
   * @see oracle.cep.shared.MetadataDescriptor#getColumnCount()
   */
  public int getColumnCount() throws SQLException
  {
    return 7;
  }

  public String getColumnClassName(int columnIndex) throws SQLException
  {
    switch (columnIndex)
    {
    case 1:
    case 2:
    case 3:
    case 4:
    case 6:
      return "java.lang.String";
    case 5:
      return "int";
    case 7:
      return "short";
    default:
      throw new SQLException("Bad argument");

    }

  }

  public String getColumnName(int column) throws SQLException
  {
    switch (column)
    {

    case 1:
      return "TYPE_CAT";
    case 2:
      return "TYPE_SCHEM";
    case 3:
      return "TYPE_NAME";
    case 4:
      return "CLASS_NAME";
    case 5:
      return "DATA_TYPE";
    case 6:
      return "REMARKS";
    case 7:
      return "BASE_TYPE";
    default:
      throw new SQLException("Bad arguments.");
    }

  }

  public int getColumnType(int column) throws SQLException
  {
    switch (column)
    {
    case 1:
    case 2:
    case 3:
    case 4:
    case 6:
      return Types.VARCHAR;
    case 5:
      return Types.INTEGER;
    case 7:
      return Types.SMALLINT;
    default:
      throw new SQLException("Bad argument");
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
      return "String";
    case 5:
      return "int";
    case 7:
      return "short";
    default:
      throw new SQLException("Bad argument");
    }
  }
  
  public Object getObject(int column) throws SQLException
  {

    switch (column)
    {

    case 1:
      return this.TYPE_CAT;
    case 2:
      return this.TYPE_SCHEM;
    case 3:
      return this.TYPE_NAME;
    case 4:
      return this.CLASS_NAME;
    case 5:
      return this.DATA_TYPE;
    case 6:
      return this.REMARKS;
    case 7:
      return this.BASE_TYPE;
    default:
      throw new SQLException("Bad arguments.");
    }

  
    
  }

  // This is a table that describes all tables. It has no name.
  public String getTableName(int column) throws SQLException
  {
    if (column > 0 && column < 8)
      return "";
    else
      throw new SQLException("Bad argument.");
  }

  public boolean isReadOnly(int column) throws SQLException
  {
    if (column > 0 && column < 8)
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

  public int getInt(String colName) throws SQLException
  {
    if (colName.equals("DATA_TYPE"))
      return this.DATA_TYPE;
    else
      throw new SQLException("Bad args");
  }

  public int getInt(int column) throws SQLException
  {
    if (column == 5)
      return this.DATA_TYPE;
    else
      throw new SQLException("Bad args");
  }

  public short getShort(String colName) throws SQLException
  {
    if (colName.equals("BASE_TYPE"))
      return this.BASE_TYPE;
    else
      throw new SQLException("Bad args");
  }

  public short getShort(int column) throws SQLException
  {
    if (column == 7)
      return this.BASE_TYPE;
    else
      throw new SQLException("Bad args");
  }

  public String getString(int columnid) throws SQLException
  {

    switch (columnid)
    {
    case 1:
      return this.TYPE_CAT;
    case 2:
      return this.TYPE_SCHEM;
    case 3:
      return this.TYPE_NAME;
    case 4:
      return this.CLASS_NAME;
    case 6:
      return this.REMARKS;
    default:
      throw new SQLException(
          "Bad argument. Refer java.sql.DatabaseMetaData for correct arguments");
    }
  }

  public String getString(String columnName) throws SQLException
  {

    if (columnName.equals("TYPE_CAT"))
      return this.TYPE_CAT;
    else if (columnName.equals("TYPE_SCHEM"))
      return this.TYPE_SCHEM;
    else if (columnName.equals("TYPE_NAME"))
      return this.TYPE_NAME;
    else if (columnName.equals("CLASS_NAME"))
      return this.CLASS_NAME;
    else if (columnName.equals("REMARKS"))
      return this.REMARKS;
    else
      throw new SQLException(
          "Bad arguments.Refer java.sql.DatabaseMetaData for correct arguments");
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
      out.writeShort(BASE_TYPE);
      out.writeInt(DATA_TYPE);
      out.writeObject(TYPE_CAT);
      out.writeObject(TYPE_SCHEM);
      out.writeObject(TYPE_NAME);
      out.writeObject(CLASS_NAME);
      out.writeObject(REMARKS);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
      BASE_TYPE = in.readShort();
      DATA_TYPE = in.readInt();
      TYPE_CAT = (String)in.readObject();
      TYPE_SCHEM = (String)in.readObject();
      TYPE_NAME = (String)in.readObject();
      CLASS_NAME = (String)in.readObject();
      REMARKS = (String)in.readObject();
  }
}
