/* $Header: pcbpel/cep/test/src/oracle/cep/test/vwap/server/EnhVWAPBenchmarkKit.java /main/7 2009/02/06 15:51:04 parujain Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    01/28/09 - txn support
    hopark      10/10/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    parujain    09/24/08 - multiple schema
    sbishnoi    08/12/08 - support for nanosecond
    hopark      07/17/08 - fix log4j message
    udeshmuk    05/19/08 - 
    sbishnoi    05/18/08 - changing Config parameter's according to new
                           CEPServer
    sbishnoi    05/15/08 - changing query
    sbishnoi    05/12/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/vwap/server/EnhVWAPBenchmarkKit.java /main/7 2009/02/06 15:51:04 parujain Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.vwap.server;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import oracle.cep.common.Constants;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.server.Command;
import oracle.cep.server.CommandInterpreter;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;
import oracle.cep.transaction.ITransaction;
import oracle.cep.execution.ExecManager;
import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.CharAttributeValue;
import oracle.cep.dataStructures.external.DoubleAttributeValue;
import oracle.cep.dataStructures.external.IntAttributeValue;
import oracle.cep.dataStructures.external.TupleValue;
import net.esper.example.benchmark.server.StatsHolder;
//import oracle.cep.test.vwap.StatsHolder;
import net.esper.example.benchmark.Symbols;
import net.esper.example.benchmark.MarketData;
import java.nio.file.Path;
import java.nio.file.Files;

public class EnhVWAPBenchmarkKit
{
  static int SERVER_PORT;
  static int TUPLE_SIZE;
  static int NUM_SYMBOLS;
  static int NUM_THREADS;
  static int SCHED_TIME_SLICE;
  static String QUERY;
  static String SCHED_NAME;

  static ServerSocket serverSocket;
  static Socket clientSocket;

  static 
  {
    NUM_THREADS        = 1;
    SCHED_NAME         = "oracle.cep.execution.scheduler.RoundRobinScheduler";
    SCHED_TIME_SLICE   = 1000;
    QUERY              = "create query q$ as select * from Market";
    SERVER_PORT        = 4444;
    NUM_SYMBOLS        = 1;
    TUPLE_SIZE         = 28;
  }
  
  public static void main(String[] args)
  {
    //printUsage();
    char[] buffer = new char[512];
    int length = 0;
    String text = "";
    FileReader reader = null;
    CEPManager cepMgr = null;
    try
    {
      for(int i =0; i < args.length ; i++)
      {
        if("-query".equalsIgnoreCase(args[i])){
          i++;
          QUERY = args[i];
        }
        else if("-numThreads".equalsIgnoreCase(args[i])){
          i++;
          NUM_THREADS = Integer.parseInt(args[i]);
        }
        else if("-numSymbols".equalsIgnoreCase(args[i])){
          i++;
          NUM_SYMBOLS = Integer.parseInt(args[i]);
        }
        else if("-schedName".equalsIgnoreCase(args[i])){
          i++;
          SCHED_NAME = args[i];
        }
        else if("-schedTimeSlice".equalsIgnoreCase(args[i])){
          i++;
          SCHED_TIME_SLICE = Integer.parseInt(args[i]);
        }
      }
    } catch (Exception e)
    {
      e.printStackTrace();
      printUsage();
    }
 
    // Configure and Start CEP Engine
    try
    {
      //springframework uses log4j and prints some configuration related exceptions by default.
      //In order to suppress messages, set some logger configuration 
      System.setProperty("log4j.defaultInitOverride", "true");
      org.apache.log4j.BasicConfigurator.configure();
      org.apache.log4j.Logger rootLogger = org.apache.log4j.LogManager.getRootLogger();
      rootLogger.setLevel(org.apache.log4j.Level.WARN);

      ApplicationContext appContext = new FileSystemXmlApplicationContext(args[0]);

      cepMgr = (CEPManager) appContext.getBean(Constants.CEPMGR_BEAN_NAME);
      ConfigManager config = cepMgr.getConfigMgr();
      config.setSchedRuntime(0);

      // Configured From Command Line Arguments
      config.setSchedNumThreads(NUM_THREADS);
      config.setSchedTimeSlice(SCHED_TIME_SLICE);
      config.setSchedulerClassName(SCHED_NAME);

      
      String path = findStoragePath();
      System.out.println("Path: " + path);
      config.setStorageFolder(path);

      // Default QueueSrc is Simple Queue and not a stored queue
      config.setUseSpilledQueueSrc(false);
      
      // Print Config Data;
      printConfigData();
      
      // if init fails then today we are throwing exception
      cepMgr.init();
      System.out.printf("Progress: Benchmark Execution Starts at %d :::\n" , System.currentTimeMillis());
     
      // Instantiate execDDL and execDML objects to run DDL and DML 
      // queries respectively
      executeDDLs t_execDDL = new executeDDLs(cepMgr);
      executeDMLs t_execDML = new executeDMLs(cepMgr);

      // Execute DDL Queries
      t_execDDL.run();
     
     
      while(true){ 
      
        // Start Server Process to recieve Injected Data by Client
        startServer();
        System.out.println("Progress: Server Started.." + "(listening on port#" + SERVER_PORT + ")");

        // Reset Stats Data
        StatsHolder.reset();

        // Execute DML statements
        t_execDML.run();

        // Wait for 10 seconds
        System.out.println("Waiting for few seconds..");
        Thread.sleep(7000);
        // Two options:1) engine StatsHolder (takes nanosecond data)
        //             2) endToEnd StatsHolder (takes millisecond data)
        // Note: if changing type of statsHolder here, modify appropriately in
        //       VWAPBenchmarkDestination
        //StatsHolder.dump("engine");
        StatsHolder.dump("endToEnd");
        //StatsHolder.getEndToEnd().dump();
        System.out.printf("Progress: Benchmark Execution Completed at %d :::\n" , System.currentTimeMillis());
      }
      
      //System.out.println("Active Thread Count(Before CEPManager close): " + Thread.activeCount());
      //CEPManager.close();
      //System.out.println("Active Thread Count(After CEPManager close): " + Thread.activeCount());
      
      //StatsHolder.getEndToEnd().dump();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    finally {
      try {
        cepMgr.close();
        closeConnections();
        System.out.println("Progress: CEP MAnager Closed");
        System.out.println("Progress: All Connection Closed");
      }
      catch(Exception e){
        e.printStackTrace();
      }
     
    }
    }
  
  static class executeDDLs
  {
    ExecContext execContext;
    CommandInterpreter cmd;
    Command c;
    
    String ddlText = QUERY;

    String ddlText1 = "alter query q$ add destination \"<EndPointReference><Address>java://oracle.cep.test.vwap.VWAPBenchmarkDestination</Address></EndPointReference>\"";
    
    String ddlText2 = "alter query q$ start";
    
    
    public executeDDLs(CEPManager mgr)
    {
      this.execContext = mgr.getSystemExecContext();
      cmd = execContext.getCmdInt();
      c   = new Command();
    }
    
    
    public void runCommand(String cql)
    {
      System.out.println("Statement is: " + cql);
      c.setCql(cql);
      cmd.execute(c);
      if(c.isBSuccess())
        System.out.println("Success");
      else
        System.out.println("Failure");
    }
    
    public void setNextDDL(){}

    public void run() throws Exception
    {
      System.out.printf("\n:::DDL Execution Started at %d :::\n\n", System.currentTimeMillis());

      // Execute Create market statement
      runCommand("create stream Market(ticker char(4), volume integer, price double) is system timestamped");

      ITransaction txn = execContext.getTransactionMgr().begin();
      execContext.setTransaction(txn);
      // Make MARKET table as push source
      execContext.getTableMgr().addStreamPushSource("Market", Constants.DEFAULT_SCHEMA);
      
      txn.commit(execContext);
      execContext.setTransaction(null);
      
      String ticker;
      for(int j = 0; j < NUM_SYMBOLS; j++)
      {
        ticker = Symbols.SYMBOLS[j];
        runCommand(ddlText.replaceAll("\\$", ticker));
        runCommand(ddlText1.replaceAll("\\$", ticker));
        runCommand(ddlText2.replaceAll("\\$", ticker));
      }
      runCommand("alter system run");
      System.out.printf(":::DDL Execution Completed at %d :::\n\n" , System.currentTimeMillis());
      
      
    }
  }

 
 /** DML Execution  */
  
  static class executeDMLs
  {
    ExecContext execContext;

    public executeDMLs(CEPManager mgr)
    {
      this.execContext = mgr.getSystemExecContext();
    }

    public void run()
    {

      TupleValue tuple;
      ExecManager exec = execContext.getExecMgr();
      int tableId = execContext.getTableMgr().getTableByName("Market", Constants.DEFAULT_SCHEMA).getId();
      
      // Wait for Data Injector Client..
      System.out.println("Progress: Waiting for Data Injector client..");
      waitForClient();
      System.out.println("Progress: Data Injector Client Connnected");

      AttributeValue[] attrval;
      try
      {
        int numTuples = 0;
        
        ObjectInputStream in 
          = new ObjectInputStream(clientSocket.getInputStream()); 

        long count = 0l;
        long tupleTs = 0l;
        MarketData md;
        byte[] data = new byte[TUPLE_SIZE];
        try
        { 
          while(true) 
          {
            in.readFully(data);
            ByteBuffer mData = ByteBuffer.wrap(data);
            md = MarketData.fromByteBuffer(mData);
 
            // Create Tuple's Attributes
            attrval    = new AttributeValue[3];
            attrval[0] = new CharAttributeValue("ticker", new char[4]);
            attrval[1] = new IntAttributeValue("volume", 0);
            attrval[2] = new DoubleAttributeValue("price", 0);
            
            // Allocate Tuple Object
            tuple = new TupleValue(null, Constants.NULL_TIMESTAMP, attrval, false);

            // Set Tuple's attributes value using recieved market data
            tuple.getAttribute(0).cValueSet(md.getTicker().toCharArray());
            tuple.getAttribute(1).iValueSet(md.getVolume());
            tuple.getAttribute(2).dValueSet(md.getPrice());

            // Set Tuple's Time stamp
            // Note: following two lines are commented as MARKET stream is
            // system-timestamped.
            //tupleTs = System.currentTimeMillis();
            //tuple.setTime(System.currentTimeMillis());
            // Insert data into CEP Engine
            exec.insert(tuple, tableId);
             
            count++;
          }
        }
        catch(java.io.EOFException ex) {
          // EOF Exception will be raised when all tuples has been recived
          System.out.println("Progress: Data Receieved Completely");
        }
        catch(java.io.IOException ex) {
          ex.printStackTrace();
        }
       System.out.println("Progress: Total "  + count + " tuples recieved");
        
      }
      catch(Exception e)
      {
       e.printStackTrace();
      }
      finally {
        closeConnections();
        System.out.println("Progress: Connection Closed with Data Injector Client");
      }
      
    }
  }


  public static void printUsage(){
    System.out.println("\n::Benchmark Usage:");
    System.out.println("oracle.cep.test.vwap.server.EnhVWAPBenchmarkKit <-rate #> <-runTime #> <-query #> <-numSymbols #> <-numThreads #> <-schedName #> <-schedTimeSlice #>");
    System.out.println("defaults:");
    System.out.println("  Rate(events/second)  : 100000");
    System.out.println("  RunTime(milliseconds): 15000");
    System.out.println("  Query     : create query q$ as select * from Market");
    System.out.println("  NumSymbols: 1");
    System.out.println("  NumThreads: 1");
    System.out.println("  SchedName : oracle.cep.execution.scheduler.RoundRobinScheduler");
    System.out.println("  SchedTimeSlice: 1000");
  }
  
  public static void printConfigData(){
    System.out.println("\n::CEP Engine Configuration:");
    System.out.printf("  NumThreads    : %s\n", NUM_THREADS);
    System.out.printf("  SchedName     : %s\n", SCHED_NAME);
    System.out.printf("  SchedTimeSlice: %s\n\n", SCHED_TIME_SLICE);
  }
  
  public static void printBenchmarkConfigData(){
    System.out.println("\n::Benchmark Configuration:");
    System.out.printf("  Query      : %s\n", QUERY);
    System.out.printf("  NumSymbols : %d\n", NUM_SYMBOLS);
  }

  private static String findStoragePath()
  {
    Path temp = null;
	try {
		temp = Files.createTempDirectory("cep");
	} catch (IOException e) {
		throw new RuntimeException(e);
	}
    return temp.toString() + File.separator + "storage";
  }

  private static void startServer() {
    try {
      serverSocket = new ServerSocket(SERVER_PORT);
    } 
    catch (IOException e) {
      System.err.println("Progress: Could not listen on port:" + SERVER_PORT + ".");
      System.exit(1);
    }
  }

  private static void waitForClient() 
  {
    try {
      clientSocket = serverSocket.accept();
    } 
    catch (IOException e) 
    {
      System.err.println("Progress: Client Connection Failed.");
    }
  }

 private static void closeConnections()
 {
  try{
    clientSocket.close();
    serverSocket.close();
  }
  catch(IOException ex)
  {
    ex.printStackTrace();
  }
 }


}
