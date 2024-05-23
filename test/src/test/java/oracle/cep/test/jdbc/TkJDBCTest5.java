/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */
/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCTest5.java /main/6 2009/11/21 07:38:14 hopark Exp $ */


/*
   DESCRIPTION
    Test burst input case with push source.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      03/01/09 - use TkJDBCTestBase
    sbishnoi    09/09/08 - changing url structure
    hopark      08/21/08 - fix jdbc port
    hopark      03/10/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCTest5.java /main/6 2009/11/21 07:38:14 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.jdbc;

import java.sql.PreparedStatement;

public class TkJDBCTest5 extends TkJDBCTestBase
{
  public static void main(String[] args) throws Exception
  {
    TkJDBCTest5 test = new TkJDBCTest5();
    test.init(args);
    test.run();
    test.exit();
  }
     
  protected void runTest()  throws Exception
  {
      int len = 8000;
      stmt.executeUpdate("register stream S_JDBC_5_2 (c1 integer, c2 byte(2147483647), c3 char(2147483647))");
      stmt.executeUpdate("alter stream S_JDBC_5_2 add source push");
      stmt.executeUpdate("create query q_JDBC_5_2 as select * from S_JDBC_5_2 [range 1]");
      String dest2 = getFileDest("5_2");  
      stmt.executeUpdate("alter query q_JDBC_5_2 add destination "+dest2);
      
      stmt.executeUpdate("alter query q_JDBC_5_2 start");
      stmt.executeUpdate("alter system run");


      PreparedStatement pstmt = con
          .prepareStatement("insert into S_JDBC_5_2 values (?, ?, ?, ?)");

      System.out.println("Normal data flow...");
      int i;
      int t = 0;
      long ts = 1000;
      for (i = 0; i < 1000; i++)
      {
        pstmt.setLong(1, ts);
        pstmt.setInt(2, t);
        byte[] d = buildTestBytes(t, len);
        pstmt.setBytes(3, d);
        String s = buildTestString(t, len);
        pstmt.setString(4, s);
        pstmt.executeUpdate();
        Thread.sleep(10);    
        t++;  
        ts += 1000;
        if ((t % 100) == 0)  System.out.println("n " + t + ", ts="+ts);
      }

      // burst case
      System.out.println("Burst data flow...");
      for (i = 0; i < 500000; i++)
      {
        pstmt.setLong(1, ts);
        pstmt.setInt(2, t);
        byte[] d = buildTestBytes(t, len);
        pstmt.setBytes(3, d);
        String s = buildTestString(t, len);
        pstmt.setString(4, s);
        pstmt.executeUpdate();
        t++;
        ts += 10;
        if ((t % 1000) == 0)  System.out.println("b " + t + ", ts="+ts);
      }

      System.out.println("Normal data flow 2...");
      for (i = 0; i < 1000; i++)
      {
        pstmt.setLong(1, ts);
        pstmt.setInt(2, t);
        byte[] d = buildTestBytes(t, len);
        pstmt.setBytes(3, d);
        String s = buildTestString(t, len);
        pstmt.setString(4, s);
        pstmt.executeUpdate();
        Thread.sleep(100);    
        t++;  
        ts += 1000;
        if ((t % 100) == 0)  System.out.println("n " + t + ", ts="+ts);
      }

      pstmt.close();
      pstmt = null;
  }

  public byte[] buildTestBytes(int t, int len)
  {
    String s = buildTestString(t, len);
    return s.getBytes();
  }

  public String buildTestString(int t, int len)
  {
    StringBuilder b = new StringBuilder();
    int n = len / 32 - 1;
    for (int i = 0; i < n; i++)
    {
      b.append(Integer.toBinaryString(t));
    }
    return b.toString();
  }  
}
