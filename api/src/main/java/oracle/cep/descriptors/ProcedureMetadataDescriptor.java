/* $Header: pcbpel/cep/common/src/oracle/cep/descriptors/ProcedureMetadataDescriptor.java /main/3 2008/09/10 14:06:32 skmishra Exp $ */

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
 mthatte     08/23/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/descriptors/ProcedureMetadataDescriptor.java /main/3 2008/09/10 14:06:32 skmishra Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.descriptors;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.SQLException;
import java.sql.Types;

public class ProcedureMetadataDescriptor extends MetadataDescriptor
{
  private static final long serialVersionUID = -8745019577921531077L;

  String PROCEDURE_CAT;
  String PROCEDURE_SCHEM;
  String PROCEDURE_NAME;
  String reserved1;
  String reserved2;
  String reserved3;
  String REMARKS;
  int    PROCEDURE_TYPE;

  public ProcedureMetadataDescriptor()
  {
  }

  public ProcedureMetadataDescriptor(String procedure_name, int procedure_type)
  {
    super();
    PROCEDURE_NAME = procedure_name;
    PROCEDURE_TYPE = procedure_type;
  }

  /*
   * Required for ResultSetMetadata
   */

  public int getColumnCount() throws SQLException
  {
    return 7;
  }

  public String getColumnClassName(int columnIndex) throws SQLException
  {
    if (columnIndex > 0 && columnIndex < 8)
      return "java.lang.String";
    else if (columnIndex == 8)
      return "java.lang.Integer";
    else
      throw new SQLException("Bad arguments");
  }

  public String getColumnName(int column) throws SQLException
  {
    switch (column)
    {
    case 1:
      return "PROCEDURE_CAT";
    case 2:
      return "PROCEDURE_SCHEM";
    case 3:
      return "PROCEDURE_NAME";
    case 4:
      return "reserved for future use";
    case 5:
      return "reserved for future use";
    case 6:
      return "reserved for future use";
    case 7:
      return "REMARKS";
    case 8:
      return "PROCEDURE_TYPE";

    default:
      throw new SQLException("Bad arguments.");
    }

  }

  public int getColumnType(int column) throws SQLException
  {
    if (column >= 1 && column <= 7)
      return Types.VARCHAR;
    else if (column == 8)
      return Types.INTEGER;
    else
      throw new SQLException("Bad argument");
  }

  public String getColumnTypeName(int column) throws SQLException
  {
    if (column >= 1 && column <= 7)
      return "String";
    else if (column == 8)
      return "int";
    else
      throw new SQLException("Bad argument");
  }
  public Object getObject(int column) throws SQLException
  {
    switch (column)
    {
    case 1:
      return this.PROCEDURE_CAT;
    case 2:
      return this.PROCEDURE_SCHEM;
    case 3:
      return this.PROCEDURE_NAME;
    case 4:
    case 5:
    case 6:
      throw new SQLException("UNUSED:Reserved for future use.");
    case 7:
      return this.REMARKS;
    case 8:
      return this.PROCEDURE_TYPE;
    default:
      throw new SQLException("Bad arguments.");
    }

  }

  //This is a table that describes columns in a table. It has no name.
  public String getTableName(int column) throws SQLException
  {
    if (column > 0 && column < 9)
      return "";
    else
      throw new SQLException("Bad argument.");
  }

  public boolean isReadOnly(int column) throws SQLException
  {
    if (column > 0 && column < 9)
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

  /*
  	End impl. for ResultSetMetadata
   */
  public String getString(int columnIndex) throws SQLException
  {
    switch (columnIndex)
    {
    case 1:
      return this.PROCEDURE_CAT;
    case 2:
      return this.PROCEDURE_SCHEM;
    case 3:
      return this.PROCEDURE_NAME;
    case 4:
      return "";
    case 5:
      return "";
    case 6:
      return "";
    case 7:
      return this.REMARKS;
    default:
      throw new SQLException(
          "Bad argument for getString(int).Possibly out of range.");
    }
  }

  public String getString(String columnName) throws SQLException
  {
    if (columnName.equals("PROCEDURE_CAT"))
      return this.PROCEDURE_CAT;
    else if (columnName.equals("PROCEDURE_SCHEM"))
      return this.PROCEDURE_SCHEM;
    else if (columnName.equals("PROCEDURE_NAME"))
      return this.PROCEDURE_NAME;
    else if (columnName.equals("REMARKS"))
      return this.REMARKS;
    else
      throw new SQLException(
          "Bad arguments.Refer java.sql.DatabaseMetaData for correct arguments");
  }

  public int getInt(int columnIndex) throws SQLException
  {
    if (columnIndex == 8)
      return this.PROCEDURE_TYPE;
    else
      throw new SQLException("Bad argument.");
  }

  public int getInt(String columnName) throws SQLException
  {
    if (columnName.equals("PROCEDURE_TYPE"))
      return this.PROCEDURE_TYPE;
    else
      throw new SQLException("Bad argument.");
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
       out.writeInt(PROCEDURE_TYPE);
       out.writeObject(PROCEDURE_CAT);
       out.writeObject(PROCEDURE_SCHEM);
       out.writeObject(PROCEDURE_NAME);
       out.writeObject(reserved1);
       out.writeObject(reserved2);
       out.writeObject(reserved3);
       out.writeObject(REMARKS);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
      PROCEDURE_TYPE = in.readInt();
      PROCEDURE_CAT = (String)in.readObject();
      PROCEDURE_SCHEM = (String)in.readObject();
      PROCEDURE_NAME = (String)in.readObject();
      reserved1 = (String)in.readObject();
      reserved2 = (String)in.readObject();
      reserved3 = (String)in.readObject();
      REMARKS = (String)in.readObject();
  }
}
