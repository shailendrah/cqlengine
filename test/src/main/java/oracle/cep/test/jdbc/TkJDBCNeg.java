package oracle.cep.test.jdbc;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import oracle.cep.common.Constants;

/* $Header: pcbpel/cep/test/src/TkJDBCNeg.java /main/2 2008/09/22 16:53:25 sbishnoi Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    09/09/08 - changing url structure
    mthatte     03/27/08 - negative test for type casting in jdbc inserts
    mthatte     03/27/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/TkJDBCNeg.java /main/2 2008/09/22 16:53:25 sbishnoi Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */

public class TkJDBCNeg {
  
  public static void main(String[] args)
  {
    
    try{
      // Load the JDBC driver
    Class.forName("oracle.cep.jdbc.CEPDriver");

    // specify the JDBC data source's URL
    String hostName = InetAddress.getLocalHost().getHostName();
    String url = "jdbc:oracle:cep:@" + hostName + ":1199";

    // connect
    Connection con = DriverManager.getConnection(url, "system", "oracle");
    Statement stmt = con.createStatement();
  
    stmt.executeUpdate("create stream S_JDBC_NEG (a integer) is system timestamped");
    stmt.executeUpdate("alter stream S_JDBC_NEG add source push");
    stmt.executeUpdate("create query q_jdbc_neg as select * from S_JDBC_NEG");
    stmt.executeUpdate("alter query q_jdbc_neg add destination \"<EndPointReference><Address>file:///tmp/out_S_JDBC_NEG.txt</Address></EndPointReference>\"");
    stmt.executeUpdate("alter query q_jdbc_neg start");
    stmt.executeUpdate("alter system run duration = 0");
    
    stmt.executeUpdate("insert into S_JDBC_NEG values (1)");
    stmt.executeUpdate("insert into S_JDBC_NEG values (1.5)");
    stmt.close();
    
    PreparedStatement pstmt = con.prepareStatement("insert into S_JDBC_NEG values (?)");
    pstmt.setFloat(1, (float)1.5);
    pstmt.executeUpdate();
    pstmt.close();
    con.close();
    }catch(Exception e){
      
    }
  }

}
