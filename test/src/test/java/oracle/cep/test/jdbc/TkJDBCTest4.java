/* $Header: pcbpel/cep/test/src/oracle/cep/test/jdbc/TkJDBCTest4.java /main/9 2009/03/05 11:32:49 hopark Exp $ */

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
    hopark      03/01/09 - use TkJDBCTestBase
    skmishra    10/21/08 - removing getProcedures()
    mthatte     10/15/08 - changed to use schema,
		sbishnoi    09/09/08 - changing url structure
    hopark      05/23/08 - 
    mthatte     08/20/07 - Test for DBMetadata interface
    mthatte     08/20/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/jdbc/TkJDBCTest4.java /main/9 2009/03/05 11:32:49 hopark Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import oracle.cep.jdbc.CEPBaseConnection;

public class TkJDBCTest4 extends TkJDBCTestBase
{
  public static void main(String[] args) throws Exception
  {
    TkJDBCTest4 test = new TkJDBCTest4();
    test.init(args);
    test.run();
    test.exit();
  }
     
  protected void runTest() throws Exception
  {
      stmt.execute("create relation R1(c1 integer, c2 float, c3 double)");
      stmt.execute("create query q1 as select c1 as icol, c2 as fcol, c3 as dcol from R1");
      //get DBMetaData object
      DatabaseMetaData dbm = con.getMetaData();
      System.out.println("This is Oracle DB version: " + dbm.getDatabaseMajorVersion());
      dumpMetadata(((CEPBaseConnection)con).getSchemaName(), dbm, "RELATION");
      dumpMetadata(((CEPBaseConnection)con).getSchemaName(), dbm, "QUERY");

      //TODO: Need hopark to validate the schema for procedures.
      /* 
      ResultSet rs1 = dbm.getProcedures("", "sys", "");
      System.out.println("#Columns:" + rs1.getMetaData().getColumnCount());

      while (rs1.next()) {
    	  System.out.println();
    	  System.out.println("Procedure Name: " + rs1.getString(3) + " OR " + rs1.getString("PROCEDURE_NAME"));
      
      }
    */
      ResultSet rsCat = dbm.getCatalogs();
      while(rsCat.next()) {
          System.out.println(rsCat.getString(1));
      }
      System.out.println("#columns: " + rsCat.getMetaData().getColumnCount());
      
      ResultSet rsSchema = dbm.getSchemas();
      System.out.println("#columns: " + rsSchema.getMetaData().getColumnCount());
      while(rsSchema.next()) {
          System.out.println(rsSchema.getString(1));
      }
      
      ResultSet rsDataTypes = dbm.getTypeInfo();
      System.out.println("#columns: " + rsDataTypes.getMetaData().getColumnCount());
      while(rsDataTypes.next()) {
          System.out.println(rsDataTypes.getString(1));
      }
  }

  private void dumpMetadata(String schemaName, DatabaseMetaData dbm, String type)
   throws Exception
  {
      String[] types = new String[1];
      types[0] = type; 
      
      ResultSet rs = dbm.getTables("", schemaName, "", types);
      while (rs.next()) {
    	  System.out.println();
    	  System.out.println("Table Name: " + rs.getString(3) + " OR " + rs.getString("TABLE_NAME"));
    	  try{
    	  ResultSet rs2 = dbm.getColumns(null, schemaName, rs.getString(3), null);
    	  
    	  while(rs2.next()) {
    		  System.out.println("Column: " + rs2.getString("COLUMN_NAME")+" Type: " + rs2.getInt("DATA_TYPE"));
    	  }
    	  }
    	  
    	  catch(SQLException re) {
    		  System.err.println("Caught that SQLE");
    	  }
 				catch(Exception e) {
					e.printStackTrace();
				}
      }
  }
  
}
