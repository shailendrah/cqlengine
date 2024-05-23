/* $Header: pcbpel/cep/test/src/oracle/cep/test/jdbc/TkJDBCTest3.java /main/12 2009/03/05 11:32:49 hopark Exp $ */

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
    sbishnoi    09/09/08 - changing url structure
    hopark      05/23/08 - change port to 1199
    mthatte     04/21/08 - bug
    mthatte     03/05/08 - adding signed constants
    udeshmuk    02/04/08 - support for double data type.
    udeshmuk    01/18/08 - add null as literal in insert statement.
    mthatte     11/06/07 - using Constants.SCHEMA
    mthatte     09/04/07 - 
    sbishnoi    06/19/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/jdbc/TkJDBCTest3.java /main/12 2009/03/05 11:32:49 hopark Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.jdbc;

import java.sql.PreparedStatement;

//test case for insert without binds
//modified to test heartbeat (with AND without binds)
//stream S1 gets a heartbeat with bind, S2 gets a heartbeat without bind.
//TODO: add more comprehensive tests wrt datatypes, values, etc.
public class TkJDBCTest3 extends TkJDBCTestBase
{

  public static void main(String[] args) throws Exception
  {
    TkJDBCTest3 test = new TkJDBCTest3();
    test.init(args);
    test.run();
    test.exit();
  }
 
  protected void runTest() throws Exception
  {
      //Simplest DDL and DML's
      String outfile = getFileName("3_neg");
      openOutputFile(outfile);

      runddl("register stream S_JDBC_3_1 (c1 integer, c2 float, c3 char(5), c4 bigint)");
      runddl("alter stream S_JDBC_3_1 add source push");
      runddl("create query q_JDBC_3_1 as istream(select * from S_JDBC_3_1[range 1])");
      String dest1 = getFileDest("3_1");  
      stmt.executeUpdate("alter query q_JDBC_3_1 add destination "+dest1);
      runddl("alter query q_JDBC_3_1 start");
      runddl("insert into S_JDBC_3_1 values (1000, 100, 12.34, \"tarun\", 123456789l)");//\"18/11/2007 10:20:14\"
      runddl("insert into S_JDBC_3_1 values (1000, 200, 12.34, \"mohit\", 123456789l)");
      runddl("insert into S_JDBC_3_1 values (1000, 300, 12.01, \"namit\", 123456789l)");
      runddl("insert into S_JDBC_3_1 values (1000, 400, 12.99, \"parul\", 123456789l)");
      //stmt.executeUpdate("insert into S_JDBC_3_1 heartbeat at 2000");
      //Testing prepared Statements, TODO: need more datatypes here.
      PreparedStatement stmtS1 = con.prepareStatement("insert into S_JDBC_3_1 heartbeat at ?");
      stmtS1.setInt(1, 2000); //heartbeat with bind 
      stmtS1.executeUpdate();
      stmtS1.close();
      

      //Testing negative constants
      runddl("register stream S_JDBC_3_2 (c1 integer, c2 float, c3 double)");
      runddl("alter stream S_JDBC_3_2 add source push");
      runddl("create query q_JDBC_3_2 as istream(select * from S_JDBC_3_2[range 1])");
      String dest2 = getFileDest("3_2");  
      stmt.executeUpdate("alter query q_JDBC_3_2 add destination "+dest2);
      
      runddl("alter query q_JDBC_3_2 start");
      runddl("insert into S_JDBC_3_2 values (2000, -300, -12.34, -36.734d)"); //\"18/11/2007 11:20:14\"
      runddl("insert into S_JDBC_3_2 values (2000, -400, -193.34, -10D)");
      runddl("insert into S_JDBC_3_2 values (2000, null, -1.23f, -2.663774d)");
      runddl("insert into S_JDBC_3_2 values (2000, -500, -14.45F, -10.32746d)");
      runddl("insert into S_JDBC_3_2 heartbeat at 3000"); //time increased
      stmt.executeUpdate("alter system run");

  }
}

