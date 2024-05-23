/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCTest2.java /main/10 2009/12/30 21:49:27 hopark Exp $ */

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
    hopark      12/04/08 - add typeMismatch test case
    sbishnoi    09/09/08 - changing url structure
    hopark      05/23/08 - change port to 1199
    mthatte     11/06/07 - using Constants.SCHEMA
    mthatte     09/06/07 - 
    parujain    05/09/07 - 
    najain      05/03/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCTest2.java /main/10 2009/12/30 21:49:27 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A sample negative jdbc program. The datais being inserted in a stream for
 * which no query has been registered. Appropriate error should be thrown.
 *
 * @author najain
 */
public class TkJDBCTest2 extends TkJDBCTestBase
{
  public static void main(String[] args) throws Exception
  {
    TkJDBCTest2 test = new TkJDBCTest2();
    test.init(args);
    test.run();
    test.exit();
  }
     
  protected void runTest() throws Exception
  {
      String outfile = getFileName("2_neg");
      openOutputFile(outfile);
      
      runddl("register stream S_JDBC_2_1 (c1 integer, c2 integer)");
      runddl("alter stream S_JDBC_2_1 add source push");

      PreparedStatement pstmt = con
          .prepareStatement("insert into S_JDBC_2_1 values (?, ?, ?)");

      try
      {
        pstmt.setLong(1, 1000);
        pstmt.setInt(2, 0);
        pstmt.setInt(3, 0);
        runpddl(pstmt);
      }
      catch(Exception e)
      {
          printException(e);
      }
      runddl("create query q_JDBC_2_1 as istream(select * from S_JDBC_2_1[range 1])");
      String dest1 = "\"<EndPointReference><Address>file://" + 
        workFolder + "/cep/out_S_JDBC_2_1.txt" +
        "</Address></EndPointReference>\"";
      stmt.executeUpdate("alter query q_JDBC_2_1 add destination "+dest1);
      runddl("alter query q_JDBC_2_1 start");

      try
      {      
        pstmt.setLong(1, 1000);
        pstmt.setInt(2, 0);
        pstmt.setInt(3, 0);
        runpddl(pstmt);
      }
      catch(Exception e)
      {
          printException(e);
      }

      PreparedStatement pstmt1 = con
          .prepareStatement("insert into S_JDBC_2_1 values (?, ?, ?)");
      try
      {
        pstmt1.setLong(1, 1000);
        pstmt1.setLong(2, 0);
        pstmt1.setInt(3, 0);
        runpddl(pstmt1);
      }
      catch(Exception e)
      {
          printException(e);
      }

      stmt.executeUpdate("alter system run");

      pstmt.close();
      pstmt = null;
      pstmt1.close();
      pstmt = null;

  }
}
