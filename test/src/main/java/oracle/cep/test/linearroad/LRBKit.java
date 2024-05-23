/* $Header: pcbpel/cep/test/src/oracle/cep/test/lrbkit/LRBKit.java /main/15 2009/01/14 01:46:33 hopark Exp $ */

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
    hopark      11/07/08 - activate refactor
    hopark      10/15/08 - remove sca import
    hopark      10/09/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    parujain    09/24/08 - multiple schema
    sbishnoi    08/11/08 - support of nanosecond
    hopark      03/18/08 - reorg config
    mthatte     04/22/08 - removed isBNull from TupleValue ctor
    hopark      04/17/08 - add jmx
    najain      04/11/08 - fix
    sbishnoi    04/11/08 - adding DDL for dump log after each 10000 inputs
    sbishnoi    03/19/08 - Removing println for DDL statements 
    sbishnoi    02/27/08 - Creation
 */
package oracle.cep.test.linearroad;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.OutOfMemoryError;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;

import net.esper.example.benchmark.server.StatsHolder;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.IntAttributeValue;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.execution.ExecManager;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.execution.scheduler.Scheduler;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.metadata.TableManager;
import oracle.cep.metadata.QueryManager;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/lrbkit/LRBKit.java /main/15 2009/01/14 01:46:33 hopark Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class LRBKit {
  
  static int NUM_THREADS;
  static String SCHED_NAME;
  static int SCHED_TIME_SLICE;
  
  static  
  {
    NUM_THREADS        = 1;
    SCHED_NAME         = "oracle.cep.execution.scheduler.FIFOScheduler";
    SCHED_TIME_SLICE   = 1000;
  }
  

  // getXMLPlan gets the XMLDump of the Physical plan and dumps it to a file.
  public static void getXMLPlan(ExecContext ec)
  {
    QueryManager qm = ec.getQueryMgr();
    String s = qm.getXMLPlan();
    PrintWriter xml = null;
    try
    {
      xml = new PrintWriter("/tmp/XMLDump.xml");
      xml.append(s);
      xml.flush();
    }
    catch (IOException e)
    {
      LogUtil.finest(LoggerType.TRACE, "problem with dumping xml");
    }
    finally
    {
      if (xml != null)
        xml.close();
    }
  }

  // getXMLPlan2 gets the XMLDump compatible for visuliser of the Physical plan
  // and dumps it to a file.
  public static void getXMLPlan2(ExecContext ec) throws CEPException
  {
    QueryManager qm = ec.getQueryMgr();
    String s = qm.getXMLPlan2();
    PrintWriter xml = null;
    try
    {
      xml = new PrintWriter("/tmp/XMLVisDump.xml");
      xml.append(s);
      xml.flush();
    }
    catch (IOException e)
    {
      LogUtil.finest(LoggerType.TRACE, "problem with dumping xml");
    }
    finally
    {
      if (xml != null)
        xml.close();
    }
  }
    

  
  
  public static void main(String args[]) throws Exception
  {
    FileReader reader = null;
    int length = 0;
    String text = "";
    char[] buffer = new char[512];
    CEPManager cepMgr;
    
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
      
      try {
        Thread.sleep(60000);
      } catch (InterruptedException e)
      {
        
      }
      cepMgr = (CEPManager) appContext.getBean("cepManager");
      System.out.printf(":::Benchmark Execution Starts at %d :::\n" , System.currentTimeMillis());
      
/*      
      text = "";
      reader = new FileReader(args[1]);
      while ((length = reader.read(buffer)) != -1)
        text += String.copyValueOf(buffer, 0, length);
      
      System.out.printf(":::DDL Statement Execution Starts at %d :::\n" , System.currentTimeMillis());
      execDDLs(cepMgr, text);
*/
      
      ExecContext ec = cepMgr.getSystemExecContext();
      getXMLPlan(ec);
      getXMLPlan2(ec);

      System.out.printf(":::DDL Statement Execution Ends at %d :::\n" , System.currentTimeMillis());
      System.out.printf(":::DML Statement Execution Starts at %d :::\n" , System.currentTimeMillis());
      execDMLs(ec, args[1]); 
      System.out.printf(":::DML Statement Execution Ends at %d :::\n" , System.currentTimeMillis());
      System.out.println("Main Thread is sleeping for 1 mins");
      Thread.sleep(60000);
      StatsHolder.dump("endToEnd");
      System.out.printf(":::Benchmark Execution Completed at %d :::\n" , System.currentTimeMillis());
      System.out.println(Thread.activeCount());
      cepMgr.close();
      System.out.println(Thread.activeCount());
    }
    catch(OutOfMemoryError e) {
      e.printStackTrace();
    }
    catch(Exception e)
    {
      System.out.println(e.toString());
      e.printStackTrace();
    }
    finally 
    {
    }
  }
  
  public static void execDDL(ExecContext ec, String ddlText) throws CEPException
  {
    ec.executeDDL(ddlText, false);
  }
  
  public static void execDMLs(ExecContext ec, String dataFileName) throws Exception, OutOfMemoryError
  {
    FileReader reader = null;
    BufferedReader br = null;
    String tupleStr   = "";
    
    // Collect Table Related Information
    TableManager tblManager = ec.getTableMgr();
    ExecManager  execMgr    = ec.getExecMgr();
    
    int tableId  = tblManager.getTableByName("tklinroadpush_CarLoc", Constants.DEFAULT_SCHEMA).getId();
    int numAttrs = tblManager.getNumAttrs(tableId);
    
    String attrName[] = new String[numAttrs];
    System.arraycopy(tblManager.getAttrNames(tableId), 0, attrName, 0, numAttrs);
  
   
   
    
    TupleValue tuple;
    
    reader = new FileReader(dataFileName);
    br = new BufferedReader(reader);
    br.readLine();
    
    long lastTs      = 0L;
    long lastTupleTs = 0L;
    long delay       = 0L;
    int count = 0;
    int i = 1;
    long startTs     = 0L;
    long extraDelayAmt = 0L;
    
    // Run Garbage Collection (System doesnt Ensure whether it Will run GC or Not)
    System.gc();
    System.gc();
    Thread.sleep(10000);
    System.gc();
    System.out.println("Before Insert Loop: ");
    System.out.println("Total Memory: " + Runtime.getRuntime().totalMemory());
    System.out.println("Free Memory: " + Runtime.getRuntime().freeMemory());
    System.out.println("Used Memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
   
    while((tupleStr = br.readLine()) != null)
    {
      // Allocate attributes
      AttributeValue[] attrVal = new AttributeValue[15];
      for(int j =0 ;j < numAttrs; j++)
        attrVal[j] = new IntAttributeValue(attrName[0], 0);
      tuple = new TupleValue(null, Constants.NULL_TIMESTAMP, attrVal, false);
      
      setTuple(tupleStr, tuple);
      // Note: Tuple's time is in millisecond unit.
      // So delay calculated below is amount of millisecond thread should sleep
      delay = tuple.getTime() - lastTs - extraDelayAmt;
      if(delay > 0L)
      {
        startTs = System.currentTimeMillis();
        Thread.sleep(delay);
        lastTs = tuple.getTime();
        while(System.currentTimeMillis()- startTs <= delay);
        extraDelayAmt = System.currentTimeMillis() - startTs - delay;
      }
      //Note: Tuples' timestamp is timestamp mentioned in input file
      //while(lastTupleTs > System.nanoTime());
      //lastTupleTs = System.nanoTime();
      //tuple.setTime(lastTupleTs);
      //System.out.println("startTs: " + startTs + " delay: "+ delay + " extraDelayAmt: " + extraDelayAmt);
      execMgr.insert(tuple, "tklinroadpush_CarLoc", Constants.DEFAULT_SCHEMA);

    }
    Thread.sleep(10000);
    System.gc();
    System.gc();
    System.out.println("After Loop: ");
    System.out.println("Total Memory: " + Runtime.getRuntime().totalMemory());
    System.out.println("Free Memory: " + Runtime.getRuntime().freeMemory());
    System.out.println("Used Memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
    
    
  }
  
  public static void setTuple(String tupleStr, TupleValue tuple) throws CEPException
  {
    int tsDelim = tupleStr.indexOf(' ');
    long tupleTs = Long.parseLong(tupleStr.substring(0, tsDelim));
    tuple.setTime(tupleTs);
    tupleStr = tupleStr.substring(tsDelim).trim();
    int colDelim = -1;
    int i =0;
    while((colDelim = tupleStr.indexOf(',')) != -1)
    {
      int colVal = Integer.parseInt(tupleStr.substring(0, colDelim));
      tuple.getAttribute(i).iValueSet(colVal);
      i++;
      tupleStr = tupleStr.substring(colDelim+1).trim();
    }
    assert i == tuple.getNoAttributes() -1;
    tuple.getAttribute(i).iValueSet(Integer.parseInt(tupleStr));
  }
}
