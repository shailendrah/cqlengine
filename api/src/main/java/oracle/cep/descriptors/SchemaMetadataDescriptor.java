
/* $Header: pcbpel/cep/common/src/oracle/cep/descriptors/SchemaMetadataDescriptor.java /main/2 2008/09/10 14:06:32 skmishra Exp $ */

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
 skmishra    11/14/07 - 
 mthatte     11/02/07 - describes schema names
 mthatte     11/02/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/descriptors/SchemaMetadataDescriptor.java /main/2 2008/09/10 14:06:32 skmishra Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.descriptors;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.SQLException;
import java.sql.Types;

public class SchemaMetadataDescriptor extends MetadataDescriptor
{
  private static final long serialVersionUID = -8745019577921531077L;

  private String TABLE_SCHEM;
  private String TABLE_CATALOG;

  public SchemaMetadataDescriptor()
  {
  }
  
  public SchemaMetadataDescriptor(String table_schem, String table_catalog)
  {
    super();
    TABLE_SCHEM = table_schem;
    TABLE_CATALOG = table_catalog;
  }

  /*
   * Required for ResultSetMetadata
   * @see oracle.cep.shared.MetadataDescriptor#getColumnCount()
   */
  public int getColumnCount() throws SQLException
  {
    return 2;
  }

  public String getColumnClassName(int columnIndex) throws SQLException
  {

    switch (columnIndex)
    {

    case 1:
      return "java.lang.String";
    case 2:
      return "java.lang.String";
    default:
      throw new SQLException("Bad args");
    }
  }

  public String getColumnName(int column) throws SQLException
  {
    switch (column)
    {
    case 1:
      return "TABLE_SCHEM";
    case 2:
      return "TABLE_CATALOG";
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
      return Types.VARCHAR;
    default:
      throw new SQLException("bad arg");

    }
  }
  
  public String getColumnTypeName(int column) throws SQLException
  {
    switch (column)
    {
    case 1:
    case 2:
      return "String";
    default:
      throw new SQLException("bad arg");

    }
  }

  public Object getObject(int column) throws SQLException
  {
    switch (column)
    {
    case 1:
      return this.TABLE_SCHEM;
    case 2:
      return this.TABLE_CATALOG;
    default:
      throw new SQLException("Bad arguments.");
    }
  }
  
  //This is a table that describes all types. It has no name.
  public String getTableName(int column) throws SQLException
  {
    if (column > 0 && column < 3)
      return "";
    else
      throw new SQLException("Bad argument.");
  }

  public boolean isReadOnly(int column) throws SQLException
  {
    if (column > 0 && column < 3)
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

  public String getString(String colName) throws SQLException
  {
    if (colName.equalsIgnoreCase("table_schem"))
      return this.TABLE_SCHEM;
    if (colName.equalsIgnoreCase("table_catalog"))
      return this.TABLE_CATALOG;
    else
      throw new SQLException(
          "Bad argument to SchemaMetadataDescriptor.getString(String)");
  }

  public String getString(int colNumber) throws SQLException
  {
    switch (colNumber)
    {

    case 1:
      return this.TABLE_SCHEM;
    case 2:
      return this.TABLE_CATALOG;
    default:
      throw new SQLException(
          "Bad argument to SchemaMetadataDescriptor.getString(int)");
    }

  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
      out.writeObject(TABLE_SCHEM);
      out.writeObject(TABLE_CATALOG);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
      TABLE_SCHEM = (String)in.readObject();
      TABLE_CATALOG = (String)in.readObject();
  }

}
