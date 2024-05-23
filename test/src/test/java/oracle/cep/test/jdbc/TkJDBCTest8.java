/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCTest8.java /main/1 2010/07/27 03:08:03 sbishnoi Exp $ */

/* Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    07/22/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCTest8.java /main/1 2010/07/27 03:08:03 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.jdbc;

import java.sql.PreparedStatement;

public class TkJDBCTest8 extends TkJDBCTestBase
{
  public static void main(String[] args) throws Exception
  {
      TkJDBCTest8 test = new TkJDBCTest8();
    test.init(args);
    test.run();
    test.exit();
  }

  protected void runTest()
    throws Exception
  {
      stmt.executeUpdate("register stream S_JDBC_8_1 (c1 integer)");
      stmt.executeUpdate("alter stream S_JDBC_8_1 add source push");
      stmt.executeUpdate("create query q_JDBC_8_1 as select * from S_JDBC_8_1 [range 4 hours]");
      String dest1 = getFileDest("8_1");  
      stmt.executeUpdate("alter query q_JDBC_8_1 add destination " + dest1);
      stmt.executeUpdate("alter query q_JDBC_8_1 start");

      PreparedStatement pstmt = con.prepareStatement("insert into S_JDBC_8_1 values (?, ?)");
      
      for (int i = 0; i < 3; i++)
      {
        pstmt.setLong(1, 1000 + 1000 * i);
        pstmt.setInt(2, i);
        pstmt.executeUpdate();
      }

      stmt.executeUpdate("alter system run");


      //////////////////////////////////////////////

      stmt.executeUpdate("create query q_JDBC_8_2 as select * from S_JDBC_8_1 [now]");
      String dest2 = getFileDest("8_2");  
      stmt.executeUpdate("alter query q_JDBC_8_2 add destination " + dest2);
      stmt.executeUpdate("alter query q_JDBC_8_2 start");
      
      for (int i = 3; i < 6; i++)
      {
        pstmt.setLong(1, 1000 + 1000 * i);
        pstmt.setInt(2, i);
        pstmt.executeUpdate();
      }

      pstmt.close();
      pstmt = null;
  }
  
}
