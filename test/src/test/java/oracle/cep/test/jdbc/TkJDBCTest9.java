/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCTest9.java /main/1 2012/06/20 05:24:32 pkali Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       06/12/12 - Test the memory consumption of partition window with
                           predicate
    pkali       06/12/12 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCTest9.java /main/1 2012/06/20 05:24:32 pkali Exp $
 *  @author  pkali   
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.jdbc;

import java.sql.PreparedStatement;

public class TkJDBCTest9 extends TkJDBCTestBase
{
  public static final int TOTAL_TUPLES = 1000000;
  public static void main(String[] args) throws Exception
  {
    TkJDBCTest9 test = new TkJDBCTest9();
    test.init(args);
    test.run();
    test.exit();
  }

  protected void runTest()
    throws Exception
  {
      stmt.executeUpdate("register stream S_JDBC_9_1 (c1 integer, c2 integer" +
      		                                        ", c3 java.lang.String)");
      stmt.executeUpdate("alter stream S_JDBC_9_1 add source push");
      stmt.executeUpdate("create query q_JDBC_9_1 as select * from " +
      		                 "S_JDBC_9_1 [PARTITION BY c1 ROWS 1] where c2 > 0");
      String dest1 = getFileDest("9_1");  
      stmt.executeUpdate("alter query q_JDBC_9_1 add destination " + dest1);
      stmt.executeUpdate("alter query q_JDBC_9_1 start");
      stmt.executeUpdate("alter system run");
      
      PreparedStatement pstmt = con.prepareStatement("insert into " +
      		                                 "S_JDBC_9_1 values (?, ?, ?, ?)");
      
      //one million tuples are given as input, the first half of the input 
      //satisfy the predicate and the remaining half will not satisfy.
      //However the partition key sequence is reset after the first half,
      //so the tuples which does not satisfy the predicate will remove the 
      //existing tuple with the same partition key and the new tuple will not 
      //be stored since it does not satisfied the predicate, which results in
      //memory optimization
      for (int i = 1, j = 1000, k = 1; i <= TOTAL_TUPLES; i++, j++)
      {
        pstmt.setLong(1, 1000 + 1000 * i);
        pstmt.setInt(2, j++);
        if( i == TOTAL_TUPLES / 2) 
        {
          //resetting the partition key sequence and predicate value
          k = 0;
          j = 1000;
        }
        pstmt.setInt(3, k);
        //long string to consume more memory
        pstmt.setString(4, "This is a lengthy string, This is a lengthy string" +
        		", This is a lengthy string, This is a lengthy string, " +
        		", This is a lengthy string, This is a lengthy string, " + 
        		", This is a lengthy string, This is a lengthy string, " + 
        		", This is a lengthy string, This is a lengthy string, " + 
        		", This is a lengthy string, This is a lengthy string, " + 
        		", This is a lengthy string, This is a lengthy string, " + 
        		", This is a lengthy string, This is a lengthy string, " + 
        		", This is a lengthy string, This is a lengthy string, " + 
        		", This is a lengthy string, This is a lengthy string ");
        pstmt.executeUpdate();
        if( i % (TOTAL_TUPLES / 10) == 0)
          System.out.println( i + " tuples processed");
      }
      pstmt.close();
      pstmt = null;
  }
  
}
