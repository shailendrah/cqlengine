/* $Header: pcbpel/cep/common/src/oracle/cep/descriptors/TableCatalogMetadataDescriptor.java /main/2 2008/09/10 14:06:32 skmishra Exp $ */

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
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/descriptors/TableCatalogMetadataDescriptor.java /main/2 2008/09/10 14:06:32 skmishra Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.descriptors;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.SQLException;
import java.sql.Types;

public class TableCatalogMetadataDescriptor extends MetadataDescriptor
{
  private static final long serialVersionUID = -8745019577921531077L;

  private String TABLE_CAT;

  public TableCatalogMetadataDescriptor()
  {
  }

  public TableCatalogMetadataDescriptor(String table_catalog)
  {
    TABLE_CAT = table_catalog;
  }

  /*
   * Required for ResultSetMetadata
   * @see oracle.cep.shared.MetadataDescriptor#getColumnCount()
   */
  public int getColumnCount() throws SQLException
  {
    return 1;
  }

  public String getColumnClassName(int columnIndex) throws SQLException
  {

    switch (columnIndex)
    {

    case 1:
      return "java.lang.String";
    default:
      throw new SQLException("Bad args");
    }
  }

  public int getColumnType(int column) throws SQLException
  {
    switch (column)
    {
    case 1:
      return Types.VARCHAR;
    default:
      throw new SQLException("Bad args");
    }
  }
  
  public String getColumnTypeName(int column) throws SQLException
  {
    switch (column)
    {
    case 1:
      return "String";
    default:
      throw new SQLException("Bad args");
    }
  }

  public String getColumnName(int column) throws SQLException
  {
    switch (column)
    {
    case 1:
      return "TABLE_CAT";
    default:
      throw new SQLException("Bad arguments.");
    }

  }

  public Object getObject(int column) throws SQLException
  {
    switch (column)
    {
    case 1:
      return this.TABLE_CAT;
    default:
      throw new SQLException("Bad arguments.");
    }
  }

  //This is a table that describes all types. It has no name.
  public String getTableName(int column) throws SQLException
  {
    if (column > 0 && column < 2)
      return "";
    else
      throw new SQLException("Bad argument.");
  }

  public boolean isReadOnly(int column) throws SQLException
  {
    if (column > 0 && column < 2)
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
    if (colName.equalsIgnoreCase("table_cat"))
      return this.TABLE_CAT;
    else
      throw new SQLException(
          "Bad argument to SchemaMetadataDescriptor.getString(String)");
  }

  public String getString(int colNumber) throws SQLException
  {
    switch (colNumber)
    {
    case 1:
      return this.TABLE_CAT;
    default:
      throw new SQLException(
          "Bad argument to SchemaMetadataDescriptor.getString(int)");
    }

  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
      out.writeObject(TABLE_CAT);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
      TABLE_CAT = (String)in.readObject();
  }
}
