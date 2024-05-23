/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCLoggingTest1.java /main/13 2011/04/27 18:37:35 apiper Exp $ */

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
    hopark      09/15/08 - fix event no
    sbishnoi    09/09/08 - changing url structure
    skmishra    09/03/08 - changing port number
    sbishnoi    07/13/08 - correcting port number
    skmishra    06/20/08 - correcting port number
    mthatte     04/21/08 - bug
    mthatte     11/06/07 - using Constants.SCHEMA
    mthatte     10/01/07 - 
    anasrini    07/15/07 - fix parser error - named to identified
    sbishnoi    06/26/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCLoggingTest1.java /main/12 2010/07/08 11:42:23 apiper Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

// Test case for logging
package oracle.cep.test.jdbc;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

import oracle.cep.common.Constants;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;

public class TkJDBCLoggingTest1 {

    private boolean isLocal = false;

    public void setIsLocal(boolean isLocal) {
        this.isLocal = isLocal;
    }

    protected Connection getConnection() throws Exception {
      Class.forName("oracle.cep.jdbc.CEPDriver");
      String     hostName = InetAddress.getLocalHost().getHostName();
      String     url      = "jdbc:oracle:cep:@" + hostName + ":" + Constants.DEFAULT_JDBC_PORT;
      if (isLocal) {
          url = Constants.CEP_LOCAL_URL + ":@" + hostName + ":" + Constants.DEFAULT_JDBC_PORT;
      }
      return DriverManager.getConnection(url, "system", "oracle");
    }

  class InsertRecords implements Runnable {

    long maxRecords;
    long timeStampInterval;
    
    public InsertRecords(long maxRecords, long timeStampInterval) {
      this.maxRecords        = maxRecords;
      this.timeStampInterval = timeStampInterval;
    }
    
    public void run() {
      
      try {
        Connection con      = getConnection();
        
        PreparedStatement pstmt = con.prepareStatement("insert into SLogging" +
          " values (?, ?)");
      
        for (int i = 0; i < maxRecords; i++) {
          pstmt.setLong(1, timeStampInterval + timeStampInterval * i);
          pstmt.setInt(2, i);
          pstmt.executeUpdate();
          Thread.sleep(timeStampInterval);
        }
        
        //close statement and connection
        pstmt.close();
        con.close();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    
  }
  
  class Logging implements Runnable {
    
    long totalTime;
    long startTime;
    
    public Logging(long totalTime, long startTime) {
      this.totalTime = totalTime;
      this.startTime = startTime;
    }
    
    public void run() {
      try {
        Connection con      = getConnection();
        Statement  stmt     = con.createStatement();
        
        String evs = "query " +
                     "identified by qLogging event " + 
                     LogEvent.OPERATOR_ALL_DS.getValue() + 
                     " level " + LogLevel.OPERATOR_STRUCTURES_MORE;
        Thread.sleep((long)(0.2 * totalTime));
        stmt.executeUpdate("alter system enable logging " + evs);
        Thread.sleep((long)(0.7 * totalTime));
        stmt.executeUpdate("alter system disable logging " + evs);
        
        //close statement and connection
        stmt.close();
        con.close();
      }
      catch(Exception e) {
        e.printStackTrace();
      }
    }
    
  }

  public static void main(String[] args) throws Exception {
      TkJDBCLoggingTest1 test = new TkJDBCLoggingTest1();
      test.runTest(args);
  }
  
  public void runTest(String[] args) throws Exception {
    
    long totalTime         = Long.parseLong(args[0]);
    long recordsPerSec     = Long.parseLong(args[1]);
    long timeStampInterval = 1000/recordsPerSec;
    long maxRecords        = totalTime/timeStampInterval;
    long startTime;
    
    Connection con      = getConnection();
    Statement  stmt     = con.createStatement();
    
    stmt.executeUpdate("register stream SLogging(c1 integer)");
    stmt.executeUpdate("alter stream SLogging add source push");
    stmt.executeUpdate("create query qLogging as select * from" +
      " SLogging[range 1]");
    stmt.executeUpdate("alter query qLogging add destination " +
      "\"<EndPointReference><Address>file://" + (args[2].startsWith("/") ? "" : "/") + args[2].replace('\\', '/') +
      "/out_JDBC_logging.txt</Address></EndPointReference>\"");
    stmt.executeUpdate("alter query qLogging start");
    stmt.executeUpdate("alter system run");
    
    Thread tInsertRecords = 
      new Thread(new InsertRecords(maxRecords, timeStampInterval));
    tInsertRecords.start();
    startTime       = System.currentTimeMillis();
    Thread tLogging = new Thread(new Logging(totalTime, startTime));
    tLogging.start();
    //main thread waits for logging thread to die
    tLogging.join();
    //main thread waits for InsertRecords thread to die
    tInsertRecords.join(); 
    stmt.close();
    con.close();
  }
}
