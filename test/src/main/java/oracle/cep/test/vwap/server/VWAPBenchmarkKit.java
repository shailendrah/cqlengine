/* $Header: pcbpel/cep/test/src/oracle/cep/test/vwap/server/VWAPBenchmarkKit.java /main/12 2009/02/06 15:51:04 parujain Exp $ */

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
    parujain    01/28/09 - txn support
    hopark      10/10/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    parujain    09/24/08 - multiple schema
    sbishnoi    08/12/08 - support for nanosecond
    hopark      07/17/08 - fix log4j message
    sbishnoi    05/26/08 - testing to achive max tuples without cep insert
    sbishnoi    04/30/08 - making bdb storage changes
    hopark      03/18/08 - reorg config
    mthatte     04/22/08 - removed isBNull from TupleValue ctor
    parujain    02/13/08 - sched on new thread
    udeshmuk    01/17/08 - change in the data type of time field of TupleValue.
    sbishnoi    12/10/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/vwap/server/VWAPBenchmarkKit.java /main/12 2009/02/06 15:51:04 parujain Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.vwap.server;

import java.io.FileReader;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;

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
import oracle.cep.dataStructures.external.FloatAttributeValue;
import oracle.cep.dataStructures.external.IntAttributeValue;
import oracle.cep.dataStructures.external.TupleValue;
import net.esper.example.benchmark.server.StatsHolder;
//import oracle.cep.test.vwap.StatsHolder;
import net.esper.example.benchmark.Symbols;
import net.esper.example.benchmark.MarketData;

public class VWAPBenchmarkKit
{
  static long RATE;
  static int NUM_SYMBOLS;
  static long RUN_TIME_IN_MILLIS;
  static int NUM_THREADS;
  static String SCHED_NAME;
  static int SCHED_TIME_SLICE;
  static long TIME_INTERVAL;
  static String QUERY;
  /*static 
  {
    RATE               = 100000;
    NUM_SYMBOLS        = 1;
    RUN_TIME_IN_MILLIS = 15000;
    NUM_THREADS        = 1;
    SCHED_NAME         = "oracle.cep.execution.scheduler.RoundRobinScheduler";
    SCHED_TIME_SLICE   = 1000;
    QUERY              = "create query q$ as select * from Market";
    V                  = 1000;
  }*/
  
  public static void main(String[] args)
  {
    printUsage();
    char[] buffer = new char[512];
    int length = 0;
    String text = "";
    FileReader reader = null;
    CEPManager cepMgr;
    
    try
    {
      for(int i =0; i < args.length ; i++)
      {
        if("-rate".equalsIgnoreCase(args[i])){
          i++;
          RATE = Long.parseLong(args[i]);
        }
        else if("-runTime".equalsIgnoreCase(args[i])){
          i++;
          RUN_TIME_IN_MILLIS = Long.parseLong(args[i]);
        }
        else if("-query".equalsIgnoreCase(args[i])){
          i++;
          QUERY = args[i];
        }
        else if("-numSymbols".equalsIgnoreCase(args[i])){
          i++;
          NUM_SYMBOLS = Integer.parseInt(args[i]);
        }
        else if("-numThreads".equalsIgnoreCase(args[i])){
          i++;
          NUM_THREADS = Integer.parseInt(args[i]);
        }
        else if("-schedName".equalsIgnoreCase(args[i])){
          i++;
          SCHED_NAME = args[i];
        }
        else if("-schedTimeSlice".equalsIgnoreCase(args[i])){
          i++;
          SCHED_TIME_SLICE = Integer.parseInt(args[i]);
        }
        else if("-timeInterval".equalsIgnoreCase(args[i])){
          i++;
          TIME_INTERVAL = Long.parseLong(args[i]);
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
      
      // Print Config Data;
      printConfigData();
      
      // if init fails then today we are throwing exception
      cepMgr.init();
      ExecContext ec = cepMgr.getSystemExecContext();
      System.out.printf(":::Benchmark Execution Starts at %d :::\n" , System.currentTimeMillis());
      
      executeDDLs t_execDDL = new executeDDLs(ec);
      executeDMLs t_execDML = new executeDMLs(ec);
      
      t_execDDL.run();
      StatsHolder.reset();
      t_execDML.run();
      System.out.println("Wait for 15 more seconds..");
      Thread.sleep(15000);
      StatsHolder.dump("endToEnd");
      //StatsHolder.getEndToEnd().dump();
      System.out.printf(":::Benchmark Execution Completed at %d :::\n" , System.currentTimeMillis());
      System.out.println("Active Thread Count(Before CEPManager close): " + Thread.activeCount());
      cepMgr.close();
      System.out.println("Active Thread Count(After CEPManager close): " + Thread.activeCount());
      
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    }
  
  static class executeDDLs
  {
    ExecContext cep;
    
    CommandInterpreter cmd;
    Command c;
    
    String ddlText = QUERY;
    //String ddlText1 = "alter query q$ add destination" +
    //" \"<EndPointReference><Address>file:///ade/sbishnoi_cep6/oracle/work/cep/log/outq$.txt</Address></EndPointReference>\"";
    String ddlText1 = "alter query q$ add destination \"<EndPointReference><Address>java://oracle.cep.test.vwap.VWAPBenchmarkDestination</Address></EndPointReference>\"";
    
    String ddlText2 = "alter query q$ start";
    
    
    public executeDDLs(ExecContext cep)
    {
      this.cep = cep;
      cmd = cep.getCmdInt();
      c = new Command();
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
    
    public void setNextDDL()
    {
     
    }
    public void run() throws Exception
    {
      System.out.printf("\n:::DDL Execution Started at %d :::\n\n", System.currentTimeMillis());
      // Execute Create market statement
      runCommand("create stream Market(ticker char(4), volume integer, price float) is system timestamped");
  
      ITransaction txn = cep.getTransactionMgr().begin();
      cep.setTransaction(txn);
      cep.getTableMgr().addStreamPushSource("Market", Constants.DEFAULT_SCHEMA);
      txn.commit(cep);
      cep.setTransaction(null);
      
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
  
  static class executeDMLs
  {
    ExecContext cep;
    
    public executeDMLs(ExecContext cep)
    {
      this.cep = cep;
    }
    
    public void run()
    {
      MarketData market[];
      int tickerIndex;
      long eventsPerInterval;
      long currentTs;
      long sleepTime = 0L;
      long startTs;
      long tupleTs;
      long numTuples = 0L;
      long iterCount = 0L;
      long maxInputTuples = (RATE * RUN_TIME_IN_MILLIS) / 1000;
      
      TupleValue tuple;
      
      ExecManager exec = cep.getExecMgr();
      AttributeValue[] attrval;

      try
      {
        /**
         * RULE:   Suppose RATE = m Tuples/ second i.e.  m/1000 Tuples/ms
         *         TIME_INTERVAL = n milliseconds;
         *         eventsPerInterval = RATE * TIME_INTERVAL = (m/1000)*n
        */ 
        eventsPerInterval = (RATE * TIME_INTERVAL)/1000;

        /** 
         * If TIME_INTERVAL is more than total run time then
         * System should inject max input tuples only
        */
        if(eventsPerInterval > maxInputTuples)
          eventsPerInterval = maxInputTuples;

        System.out.println("** eventsPerInterval: " + eventsPerInterval);
        System.out.println("** max Input Tuples: " + maxInputTuples);
        System.out.println("Num Processors:" + Runtime.getRuntime().availableProcessors());       
        market = new MarketData[Symbols.SYMBOLS.length];
        
        for (int i = 0; i < market.length; i++)
          market[i] = new MarketData(Symbols.SYMBOLS[i], Symbols.nextPrice(10), Symbols.nextVolume(10));
        
        // set market data
        for (int i = 0; i < market.length; i++)
        {
          MarketData md = market[i];
          md.setPrice(Symbols.nextPrice(md.getPrice()));
          md.setVolume(Symbols.nextVolume(10));
        }

        
        printBenchmarkConfigData();
        System.out.printf("::: Data Feeding Starts at %d :::\n", System.currentTimeMillis());
        
        currentTs = System.currentTimeMillis();
        startTs  = currentTs;
        
        // Start Data Feeding
        while(currentTs - startTs < RUN_TIME_IN_MILLIS)
        {
          if(numTuples + eventsPerInterval > maxInputTuples)
            eventsPerInterval = maxInputTuples - numTuples;
          for(int j =0; j < eventsPerInterval; j++)
          {
            // Allocate attributes
            attrval    = new AttributeValue[3];
            attrval[0] = new CharAttributeValue("ticker", new char[4]);
            attrval[1] = new IntAttributeValue("volume", 0);
            attrval[2] = new FloatAttributeValue("price", 0);

            // Allocate tuple value
            tuple = new TupleValue(null, Constants.NULL_TIMESTAMP, attrval, false);

            
            // set market data
            tickerIndex = Symbols.nextVolume(100000) % market.length;
            MarketData md = market[tickerIndex];

            // Set tuple's attributes value using market data
            tuple.getAttribute(0).cValueSet(md.getTicker().toCharArray());
            tuple.getAttribute(1).iValueSet(md.getVolume());
            tuple.getAttribute(2).fValueSet(new Float(md.getPrice()));

            // Note: As Market stream is system timestamped; No need to set
            // tuple timestamp value.
            //tupleTs = System.nanoTime();
            //tuple.setTime(tupleTs);
            numTuples++;            
            exec.insert(tuple, "Market", Constants.DEFAULT_SCHEMA);
          }
          iterCount++;
          System.out.println("Itercount: " + iterCount +  " numTuples: " + numTuples);
          try
          { // Sleep for some time if loop terminates earlier than 50ms
            sleepTime = startTs + (iterCount * TIME_INTERVAL ) - System.currentTimeMillis();

            //if((sleepTime + System.currentTimeMillis() + startTs) > RUN_TIME_IN_MILLIS)
              // sleepTime = RUN_TIME_IN_MILLIS - (System.currentTimeMillis() - startTs);

            System.out.println("** Sleep time: " + sleepTime);
            if(sleepTime > 0L)
              Thread.sleep(sleepTime);
          }
          catch(IllegalArgumentException argException)
          {// Ignore the exception
          }  
          currentTs = System.currentTimeMillis();
        }
        System.out.printf("::: Data Feeding Completed at %d :::\n" , System.currentTimeMillis());
        System.out.printf("::: %d Numbers of Tuples Feeded in %d milliseconds :::\n", numTuples, currentTs - startTs);
        
      }
      catch(Exception e)
      {
       e.printStackTrace();
      }
      
    }
  }

  public static void printUsage(){
    System.out.println("\nBenchmark Usage:");
    System.out.println("oracle.cep.test.vwap.server.VWAPBenchmarkKit <-rate #> <-runTime #> <-query #> <-numSymbols #> <-numThreads #> <-schedName #> <-schedTimeSlice #>");
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
    System.out.println("\nCEP Engine Configuration:");
    System.out.printf("  NumThreads    : %s\n", NUM_THREADS);
    System.out.printf("  SchedName     : %s\n", SCHED_NAME);
    System.out.printf("  SchedTimeSlice: %s\n\n", SCHED_TIME_SLICE);
  }
  
  public static void printBenchmarkConfigData(){
    System.out.println("\nBenchmark Configuration:");
    System.out.printf("  Query      : %s\n", QUERY);
    System.out.printf("  NumSymbols : %d\n", NUM_SYMBOLS);
    System.out.printf("  Rate       : %d\n", RATE);
    System.out.printf("  RunTime(ms): %d\n", RUN_TIME_IN_MILLIS);    
    System.out.printf("  Time Interval(ms): %d\n\n", TIME_INTERVAL);    
  }

  private static String findStoragePath()
  {
    String path = System.getenv("T_WORK");
    String separator = System.getProperty("file.separator");
    if(path != null)
    {
      path += separator + "cep" + separator + "storage";
      return path;
    }
    String os_name = System.getProperty("os.name");
    
    if (os_name.toLowerCase().startsWith("windows"))
      path = "c:\temp";
    else if(os_name.toLowerCase().startsWith("linux"))
      path = "/tmp";
    else
      assert false;
    path += separator + "cep" + "separator" + "storage";
    return path;
  }
}
