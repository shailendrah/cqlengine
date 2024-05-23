/* $Header: pcbpel/cep/common/src/oracle/cep/descriptors/TableTypeMetadataDescriptor.java /main/2 2008/09/10 14:06:32 skmishra Exp $ */

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
 mthatte     11/04/07 - to describe tableType. ref. CEPDatabaseMetadata
 mthatte     11/04/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/descriptors/TableTypeMetadataDescriptor.java /main/2 2008/09/10 14:06:32 skmishra Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.descriptors;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.SQLException;
import java.sql.Types;

public class TableTypeMetadataDescriptor extends MetadataDescriptor
{
  private static final long serialVersionUID = -8745019577921531077L;
  String TABLE_TYPE;

  public TableTypeMetadataDescriptor()
  {
  }

  public TableTypeMetadataDescriptor(String table_type)
  {
    super();
    TABLE_TYPE = table_type;
  }

  public int getColumnCount() throws SQLException
  {
    return 1;
  }

  public int getColumnType(int column) throws SQLException
  {
    if (column == 1)
      return Types.VARCHAR;
    else
      throw new SQLException("bad argument");
  }

  public String getColumnTypeName(int column) throws SQLException
  {
    if (column == 1)
      return "String";
    else
      throw new SQLException("bad argument");
  }
  
  public String getColumnClassName(int column) throws SQLException
  {
    if (column == 1)
      return "java.lang.String";
    else
      throw new SQLException("bad argument");
  }
  
  public String getColumnName(int column) throws SQLException
  {
    if (column == 1)
      return "TABLE_TYPE";
    else
      throw new SQLException("bad argument");
  }
  
  public Object getObject(int column) throws SQLException
  {
    if (column == 1)
      return this.TABLE_TYPE;
    else
      throw new SQLException("bad argument");
  }

  public String getString(String colName) throws SQLException
  {
    if (colName.equalsIgnoreCase("TABLE_TYPE"))
      return this.TABLE_TYPE;
    else
      throw new SQLException(
          "Bad argument to TableTypeMetadataDescriptor.getString(String);");
  }

  public String getString(int column) throws SQLException
  {
    if (column == 1)
      return this.TABLE_TYPE;
    else
      throw new SQLException(
          "Bad argument to TableTypeMetadataDescriptor.getString(int);");
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
      out.writeObject(TABLE_TYPE);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
      TABLE_TYPE = (String)in.readObject();
  }
}
