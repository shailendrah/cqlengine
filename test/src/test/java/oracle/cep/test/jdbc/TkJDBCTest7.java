/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCTest7.java /main/4 2009/09/13 23:57:27 sbishnoi Exp $ */

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
    hopark    03/16/09 - add obj join
    hopark    03/01/09 - use TkJDBCTestBase
    hopark    02/08/09 - creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCTest7.java /main/4 2009/09/13 23:57:27 sbishnoi Exp $
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
import oracle.cep.test.userfunctions.TkObj;

public strictfp class TkJDBCTest7 extends TkJDBCTestBase
{
  public static void main(String[] args) throws Exception
  {
      TkJDBCTest7 test = new TkJDBCTest7();
    test.init(args);
    test.run();
    test.exit();
  }
     
  protected void runTest() throws Exception
  {
      stmt.executeUpdate("register stream S_JDBC_7_1 (c1 float, c2 object)");
      stmt.executeUpdate("alter stream S_JDBC_7_1 add source push");
      stmt.executeUpdate("create function f_JDBC_Obj(o1 object) return object as language java name \"oracle.cep.test.userfunctions.TkObjFunc\"" );
      stmt.executeUpdate("create function a_JDBC_Obj(c1 float) return object aggregate using \"oracle.cep.test.userfunctions.TkObjAggr\" supports incremental computation");
      stmt.executeUpdate("create function a1_JDBC_Obj(o1 object) return object aggregate using \"oracle.cep.test.userfunctions.TkObjAggr1\" supports incremental computation");

      stmt.executeUpdate("create query q_JDBC_7_1 as select * from S_JDBC_7_1 [range 1]");
      String dest1 = getFileDest("7_1");  
      stmt.executeUpdate("alter query q_JDBC_7_1 add destination "+dest1);

      stmt.executeUpdate("create query q_JDBC_7_11 as select a.c2, b.c2 from S_JDBC_7_1 [range 1] as a, S_JDBC_7_1 [range 1] as b where a.c1 = b.c1");
      String dest11 = getFileDest("7_11");  
      stmt.executeUpdate("alter query q_JDBC_7_11 add destination "+dest11);

      stmt.executeUpdate("create query q_JDBC_7_2 as select c2, f_JDBC_Obj(c2) from S_JDBC_7_1 [rows 5]");
      String dest2 = getFileDest("7_2");  
      stmt.executeUpdate("alter query q_JDBC_7_2 add destination "+dest2);

      stmt.executeUpdate("create query q_JDBC_7_3 as select c1, a_JDBC_Obj(c1) from S_JDBC_7_1 [rows 5]  group by c1");
      String dest3 = getFileDest("7_3");  
      stmt.executeUpdate("alter query q_JDBC_7_3 add destination "+dest3);

      stmt.executeUpdate("create query q_JDBC_7_4 as select c1, a1_JDBC_Obj(c2) from S_JDBC_7_1 [rows 5] group by c1");
      String dest4 = getFileDest("7_4");  
      stmt.executeUpdate("alter query q_JDBC_7_4 add destination "+dest4);

      stmt.executeUpdate("alter query q_JDBC_7_1 start");
      stmt.executeUpdate("alter query q_JDBC_7_11 start");
      stmt.executeUpdate("alter query q_JDBC_7_2 start");
      stmt.executeUpdate("alter query q_JDBC_7_3 start");
      stmt.executeUpdate("alter query q_JDBC_7_4 start");

      PreparedStatement pstmt = con.prepareStatement("insert into S_JDBC_7_1 values (?, ?, ?)");
      
      for (int i = 0; i < 50; i++)
      {
        pstmt.setLong(1, 1000 + 1000 * i);
        pstmt.setFloat(2, i);
        TkObj obj = new TkObj();
        obj.setIVal(i);
        obj.setLVal(i*100);
        obj.setFVal(i/100.0f);
        obj.setDVal(i/10000.0);
        obj.setSVal("str"+i);
        pstmt.setObject(3, obj);
        pstmt.executeUpdate();
      }

      stmt.executeUpdate("alter system run");

      pstmt.close();
      pstmt = null;
  }
}
