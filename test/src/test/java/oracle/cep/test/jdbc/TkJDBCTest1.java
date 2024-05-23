/* $Header: pcbpel/cep/test/src/oracle/cep/test/jdbc/TkJDBCTest1.java /main/10 2009/03/05 11:32:49 hopark Exp $ */

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
    sbishnoi  09/09/08 - changing url structure
    hopark    05/19/08 - change rmi port
    hopark    03/28/08 - 
    mthatte   11/06/07 - using Constants.SCHEMA
    mthatte   10/01/07 - 
 mthatte     08/16/07 - 
 mthatte     09/06/07 - 
 sbishnoi    05/23/07 - 
 parujain    05/09/07 - 
 najain      04/25/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/jdbc/TkJDBCTest1.java /main/10 2009/03/05 11:32:49 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */


/**
 * A sample jdbc program. A very simple test with integer datatypes. New
 * tests can be modelled on it.
 *
 * @author najain
 */
package oracle.cep.test.jdbc;

import java.sql.PreparedStatement;

public class TkJDBCTest1 extends TkJDBCTestBase
{
  public static void main(String[] args) throws Exception
  {
    TkJDBCTest1 test = new TkJDBCTest1();
    test.init(args);
    test.run();
    test.exit();
  }
     
  protected void runTest()
    throws Exception
  {
      stmt.executeUpdate("register stream S_JDBC_1_1 (c1 integer, c2 integer)");
      stmt.executeUpdate("alter stream S_JDBC_1_1 add source push");

      stmt.executeUpdate("create query q_JDBC_1_1 as select * from S_JDBC_1_1 [range 1]");
      String dest1 = getFileDest("1_1");  
      stmt.executeUpdate("alter query q_JDBC_1_1 add destination "+dest1);
      stmt.executeUpdate("alter query q_JDBC_1_1 start");

      PreparedStatement pstmt = con.prepareStatement("insert into S_JDBC_1_1 values (?, ?, ?)");
      
      for (int i = 0; i < 50; i++)
      {
        pstmt.setLong(1, 1000 + 1000 * i);
        pstmt.setInt(2, i);
        pstmt.setInt(3, i * 2);
        pstmt.executeUpdate();
      }

      stmt.executeUpdate("alter system run");

      pstmt.close();
      pstmt = null;
  }
}
