
/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCTest6.java /main/5 2011/04/27 18:37:35 apiper Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
    hopark    03/01/09 - use TkJDBCTestBase
    hopark    01/21/09 - set thread name
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
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/jdbc/TkJDBCTest6.java /main/4 2009/03/05 11:32:49 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.jdbc;

import java.sql.PreparedStatement;

/**
 * A sample jdbc program. A very simple test with integer datatypes. New
 * tests can be modelled on it.
 *
 * @author najain
 */
public class TkJDBCTest6
{
  public static class TestRunner extends TkJDBCTestBase
  {
    int seq;
    
    public TestRunner(int seq)
    {
      super("CEP" + seq);
      this.seq = seq;
    }

    public TestRunner(String[] args, int seq)
    {
      super("CEP" + seq);
      init(args);
      this.seq = seq;
    }

    protected void runTest() throws Exception
    {
        stmt.executeUpdate("register stream S_JDBC_1_1 (c1 integer, c2 integer)");
        stmt.executeUpdate("alter stream S_JDBC_1_1 add source push");
        stmt.executeUpdate("create query q_JDBC_1_1 as select * from S_JDBC_1_1 [rows 1]");
        String dest1 = getFileDest("6_1s"+seq);  

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
  
        //scheduler has already started on connection.
        //stmt.executeUpdate("alter system run");
        //run for 10 seconds..
        Thread.sleep(10000);
        
        pstmt.close();
        pstmt = null;
    }
  }
  
  public static void main(String[] args) throws Exception
  {
    int no_threads = 5;
    
    Thread threads[] = new Thread[no_threads];
    for (int i = 0; i < no_threads; i++)
    {
      TestRunner r = new TestRunner(args, i);
      Thread threadR = new Thread(r, "Runner"+i);
      threads[i] = threadR;
      threadR.start();
    }
    for (int i = 0; i < no_threads; i++)
    {
      threads[i].join();
    }
  }
}
