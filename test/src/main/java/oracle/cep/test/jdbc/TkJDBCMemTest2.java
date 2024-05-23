package oracle.cep.test.jdbc;
/* $Header: pcbpel/cep/test/src/TkJDBCTest8.java /main/3 2009/05/12 19:25:47 parujain Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    03/03/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/TkJDBCTest8.java /main/3 2009/05/12 19:25:47 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.lang.management.ManagementFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import oracle.cep.common.Constants;

public class TkJDBCMemTest2
{
  public static class TestRunner extends Thread 
  {
    int seq;
    // If true then single stream else multi stream
    boolean ss;
    boolean singletask;
    
    public TestRunner(boolean singletask, boolean ss, int seq)
    {
      super("TestRunner"+seq);
      this.singletask = singletask;
      this.seq = seq;
      this.ss = ss;
    }

    public void run()
    {
      String url = "";
      try
      {
        // Load the JDBC-ODBC bridge
        Class.forName("oracle.cep.jdbc.CEPDriver");
  
        // specify the JDBC data source's URL
        String serviceName = "CEP" + seq;
        String hostName = InetAddress.getLocalHost().getHostName();
        if (singletask)
          url = Constants.CEP_LOCAL_URL + ":@" + hostName + ":" + Constants.DEFAULT_JDBC_PORT + ":" + serviceName;
        else url = "jdbc:oracle:cep:@" + hostName + ":" + Constants.DEFAULT_JDBC_PORT + ":" + serviceName;
        
        System.out.println(url);
        
        // connect
        Connection con = DriverManager.getConnection(url, "system", "oracle");

        String regStream = " ";
        String alterStream = " ";
        String regQuery;
        String queryDest;
        String startQuery;
        String sn = "S";
        String qn;
        
        long startTime;
        long endTime;
        long totalTimeSecs;

        long h,nh,totmem;

        startTime = System.currentTimeMillis();
        int i;

        if(ss)
        {
          Statement stmt1 = con.createStatement();
          sn = "S";
          regStream = "register stream " + sn + "(c1 integer, c2 integer)";
          alterStream = "alter stream " + sn +" add source push";
          stmt1.executeUpdate(regStream);
          stmt1.executeUpdate(alterStream);
          stmt1.close();
        }
        

        for(i=0; i<100000; i++) {
          Statement stmt = con.createStatement();

          qn = "q" + i;

          if(!ss)
          {   
            sn = "S" + i;
            regStream = "register stream " + sn + "(c1 integer, c2 integer)";
            alterStream = "alter stream " + sn +" add source push";
          }

          regQuery = "create query " + qn + " as select * from " + sn +
            " where c1=" + i;
          queryDest = "alter query " + qn + " add destination \"<EndPointReference><Address>file:///tmp/outfile.txt</Address></EndPointReference>\""; 
           startQuery = "alter query " + qn + " start";
          
         if(!ss)
         { 
           stmt.executeUpdate(regStream);
           stmt.executeUpdate(alterStream);
         }

          stmt.executeUpdate(regQuery);
          stmt.executeUpdate(queryDest); 
          stmt.executeUpdate(startQuery);

          if ((i+1)%10000 == 0) {
            h = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / (1000 * 1000);
            nh = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed() / (1000 * 1000);
            totmem = h + nh;

            endTime = System.currentTimeMillis();
            totalTimeSecs = (endTime - startTime) / 1000;

            System.out.println("Done " + (i+1) + " iterations: " +
                               "h=" + h + "mb, " +
                               "nh=" + nh + "mb, " +
                               "tot=" + totmem + "mb, " +
                               "time=" + totalTimeSecs + "s");
          }
          stmt.close();
        }

        endTime = System.currentTimeMillis();
        totalTimeSecs = (endTime - startTime) / 1000;
        System.out.println("Completed " + i + " iterations in " +
                           totalTimeSecs + " seconds");

        // Sleep for 5 seconds before closing the connection
        Thread.sleep(5000);
        con.close();
        System.exit(0);
      }
      catch (java.lang.Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }
  
  public static void main(String[] args) throws Exception
  {
    boolean singletask = false;
    boolean ss = false;
    if (args.length > 0)
    {
      singletask = args[0].equals("singletask");
      if(args[1].equals("ss"))
        ss = true;
      else
        ss = false;
    }
    if (singletask)
    {
      try
      {
        //springframework uses log4j and prints some configuration related exceptions by default.
        //In order to suppress messages, set some logger configuration 
        System.setProperty("log4j.defaultInitOverride", "true");
        org.apache.log4j.BasicConfigurator.configure();
        org.apache.log4j.Logger rootLogger = org.apache.log4j.LogManager.getRootLogger();
        rootLogger.setLevel(org.apache.log4j.Level.WARN);

        ApplicationContext appContext = new ClassPathXmlApplicationContext(Constants.DEFAULT_CONFIG_FILE);
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    int i;
    for (i = 0; i < 1; i++)
    {
      TestRunner r = new TestRunner(singletask, ss, i);
      r.start();
    }


  }
}
