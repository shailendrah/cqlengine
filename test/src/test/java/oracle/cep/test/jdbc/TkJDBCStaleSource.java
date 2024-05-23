/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCStaleSource.java /main/2 2011/04/27 18:37:35 apiper Exp $ */

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
 hopark      04/28/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/jdbc/TkJDBCStaleSource.java /main/1 2009/05/25 08:27:34 hopark Exp $
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

import java.rmi.RemoteException;
import java.sql.SQLException;

import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.IntAttributeValue;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.jdbc.CEPPreparedStatement;

public class TkJDBCStaleSource extends TkJDBCTestBase
{
  public static void main(String[] args) throws Exception
  {
    TkJDBCStaleSource test = new TkJDBCStaleSource();
    test.init(args);
    test.run();
    test.exit();
  }
     
  private void insert(CEPPreparedStatement pstmt, String dest, int i)
    throws Exception
  {
      //System.out.println("sending "+i);
      long ts = 1000 + 1000 * i;
      AttributeValue[] attrs = new AttributeValue[2];
      attrs[0] = new IntAttributeValue(i);
      attrs[1] = new IntAttributeValue(i*2);
      TupleValue tuple = new TupleValue(dest, ts, attrs, false);
      pstmt.executeDML(tuple);
      try
      {
        Thread.sleep(500);
      } catch (InterruptedException e)
      {
      }
  }
  
  protected void runTest()
    throws Exception
  {
      stmt.executeUpdate("register stream S_JDBC_stalesrc_1 (c1 integer, c2 integer)");
      stmt.executeUpdate("alter stream S_JDBC_stalesrc_1 add source push");

      stmt.executeUpdate("register stream S_JDBC_stalesrc_2 (c1 integer, c2 integer)");
      stmt.executeUpdate("alter stream S_JDBC_stalesrc_2 add source push");

      stmt.executeUpdate("create query q_JDBC_stalesrc_1 as select * from S_JDBC_stalesrc_1 [range 1]");
      String dest1 = getFileDest("stalesrc_1");  
      stmt.executeUpdate("alter query q_JDBC_stalesrc_1 add destination "+dest1);
      stmt.executeUpdate("alter query q_JDBC_stalesrc_1 start");
      stmt.executeUpdate("alter system run");

      CEPPreparedStatement pstmt = (CEPPreparedStatement)con.prepareStatement("insert into S_JDBC_stalesrc_1 values (?, ?, ?)");
      CEPPreparedStatement pstmt2 = (CEPPreparedStatement)con.prepareStatement("insert into S_JDBC_stalesrc_2 values (?, ?, ?)");
      int i = 0;
      while (i < 3)
      {
        insert(pstmt, "S_JDBC_stalesrc_1", i++);
      }      
      stmt.executeUpdate("alter query q_JDBC_stalesrc_1 stop");
      stmt.executeUpdate("drop query q_JDBC_stalesrc_1");

      stmt.executeUpdate("create query q_JDBC_stalesrc_2 as select * from S_JDBC_stalesrc_2 [range 1]");
      String dest2 = getFileDest("stalesrc_2");  
      stmt.executeUpdate("alter query q_JDBC_stalesrc_2 add destination "+dest2);
      stmt.executeUpdate("alter query q_JDBC_stalesrc_2 start");
      try
      {
        while (i < 6)
        {
          insert(pstmt, "S_JDBC_stalesrc_1", i++);
        }
      }
      catch(SQLException e)
      {
        //we should get STALE_TABLE_SOURCE
        System.out.println(e.toString());
      }
      i = 6;
      while (i < 9)
      {
        insert(pstmt2, "S_JDBC_stalesrc_2", i++);
      }      
      pstmt.close();
      pstmt = null;
      pstmt2.close();
      pstmt2 = null;
 }
}
