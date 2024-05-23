/* $Header: pcbpel/cep/common/src/oracle/cep/descriptors/TableMetadataDescriptor.java /main/4 2008/09/23 07:48:32 sbishnoi Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    09/23/08 - changing constant names
 skmishra    08/20/08 - changing package name
 mthatte     11/06/07 - adding getColumnType()
 mthatte     10/09/07 - adding precision and scale methods
 mthatte     08/21/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/descriptors/TableMetadataDescriptor.java /main/4 2008/09/23 07:48:32 sbishnoi Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.descriptors;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.SQLException;
import java.sql.Types;

import oracle.cep.common.Constants;

public class TableMetadataDescriptor extends MetadataDescriptor
{
  private static final long serialVersionUID = -8745019577921531077L;
  String TABLE_CAT                 = ""; //table catalog (may be null)
  String TABLE_SCHEM               = ""; //table schema (may be null)
  String TABLE_NAME                = ""; //table name
  String TABLE_TYPE                = ""; // table type.  Typical types are "TABLE",
  //"VIEW",	"SYSTEM TABLE", "GLOBAL TEMPORARY", 
  //"LOCAL TEMPORARY", "ALIAS", "SYNONYM".
  String REMARKS                   = ""; //explanatory comment on the table
  String TYPE_CAT                  = ""; //the types catalog (may be null)
  String TYPE_SCHEM                = ""; //the types schema (may be null)
  String TYPE_NAME                 = ""; //type name (may be <code>null</code>)
  String SELF_REFERENCING_COL_NAME = ""; //name of the designated 
  //"identifier" column of a typed table 
  //(may be null)
  String REF_GENERATION            = ""; //specifies how values in 

  //SELF_REFERENCING_COL_NAME are created.
  //Values are"SYSTEM", "USER", "DERIVED".(may be null)	

  public TableMetadataDescriptor()
  {

  }

  /*
   * Required for ResultSetMetadata
   * @see oracle.cep.shared.MetadataDescriptor#getColumnCount()
   */
  public int getColumnCount() throws SQLException
  {
    return 10;
  }

  public String getColumnClassName(int columnIndex) throws SQLException
  {

    if (columnIndex > 0 && columnIndex < 10)
      return "java.lang.String";
    else
      throw new SQLException("Bad arguments");
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
      return "TABLE_TYPE";
    case 5:
      return "REMARKS";
    case 6:
      return "TYPE_CAT";
    case 7:
      return "TYPE_SCHEM";
    case 8:
      return "TYPE_NAME";
    case 9:
      return "SELF_REFERENCING_COL_NAME";
    case 10:
      return "REF_GENERATION";
    default:
      throw new SQLException("Bad arguments.");
    }

  }

  public int getColumnType(int column) throws SQLException
  {
    if (column > 0 && column < 10)
      return Types.VARCHAR;
    else
      throw new SQLException("Bad arguments");
  }

  public String getColumnTypeName(int column) throws SQLException 
  {
    if (column > 0 && column < 10)
      return "String";
    else
      throw new SQLException("Bad arguments");
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
      return this.TABLE_TYPE;
    case 5:
      return this.REMARKS;
    case 6:
      return this.TYPE_CAT;
    case 7:
      return this.TYPE_SCHEM;
    case 8:
      return this.TYPE_NAME;
    case 9:
      return this.SELF_REFERENCING_COL_NAME;
    case 10:
      return this.REF_GENERATION;
    default:
      throw new SQLException("Bad arguments.");
    }
    
  }
  
  //This is a table that describes all tables. It has no name.
  public String getTableName(int column) throws SQLException
  {
    if (column > 0 && column < 10)
      return "";
    else
      throw new SQLException("Bad argument.");
  }

  public boolean isReadOnly(int column) throws SQLException
  {
    if (column > 0 && column < 11)
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

  public TableMetadataDescriptor(String name, String type)
  {
    this.TABLE_NAME = name;
    this.TABLE_TYPE = type;
    this.TABLE_SCHEM = Constants.DEFAULT_SCHEMA;
    this.TABLE_CAT = Constants.CEP_CATALOG;
  }

  public String getString(int columnid) throws SQLException
  {

    switch (columnid)
    {
    case 1:
      return this.TABLE_CAT;
    case 2:
      return this.TABLE_SCHEM;
    case 3:
      return this.TABLE_NAME;
    case 4:
      return this.TABLE_TYPE;
    case 5:
      return this.REMARKS;
    case 6:
      return this.TYPE_CAT;
    case 7:
      return this.TYPE_SCHEM;
    case 8:
      return this.TYPE_NAME;
    case 9:
      return this.SELF_REFERENCING_COL_NAME;
    case 10:
      return this.REF_GENERATION;

    default:
      throw new SQLException(
          "Bad argument. Refer java.sql.DatabaseMetaData for correct arguments");
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
    else if (columnName.equals("TABLE_TYPE"))
      return this.TABLE_TYPE;
    else if (columnName.equals("REMARKS"))
      return this.REMARKS;
    else if (columnName.equals("TYPE_CAT"))
      return this.TYPE_CAT;
    else if (columnName.equals("TYPE_SCHEM"))
      return this.TYPE_SCHEM;
    else if (columnName.equals("TYPE_NAME"))
      return this.TYPE_NAME;
    else if (columnName.equals("SELF_REFERENCING COL_NAME"))
      return this.SELF_REFERENCING_COL_NAME;
    else if (columnName.equals("REF_GENERATION"))
      return this.REF_GENERATION;
    else
      throw new SQLException(
          "Bad arguments.Refer java.sql.DatabaseMetaData for correct arguments");
  }
  
  public void setRemarkText(String remarks) {
    this.REMARKS = remarks;
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
      out.writeObject(TABLE_CAT);
      out.writeObject(TABLE_SCHEM);
      out.writeObject(TABLE_NAME);
      out.writeObject(TABLE_TYPE);
      out.writeObject(REMARKS);
      out.writeObject(TYPE_CAT);
      out.writeObject(TYPE_SCHEM);
      out.writeObject(TYPE_NAME);
      out.writeObject(SELF_REFERENCING_COL_NAME);
      out.writeObject(REF_GENERATION);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
      TABLE_CAT = (String)in.readObject();
      TABLE_SCHEM = (String)in.readObject();
      TABLE_NAME = (String)in.readObject();
      TABLE_TYPE = (String)in.readObject();
      REMARKS = (String)in.readObject();
      TYPE_CAT = (String)in.readObject();
      TYPE_SCHEM = (String)in.readObject();
      TYPE_NAME = (String)in.readObject();
      SELF_REFERENCING_COL_NAME = (String)in.readObject();
      REF_GENERATION = (String)in.readObject();
  }
}
