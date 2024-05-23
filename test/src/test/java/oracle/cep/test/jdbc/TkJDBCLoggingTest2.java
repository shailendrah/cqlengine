/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCLoggingTest2.java /main/5 2010/07/08 11:42:23 apiper Exp $ */

/* Copyright (c) 2007, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    07/21/09 - incorporating the cep directory changes
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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/jdbc/TkJDBCLoggingTest2.java /main/5 2010/07/08 11:42:23 apiper Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

// Test case for logging
package oracle.cep.test.jdbc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;

import oracle.cep.logging.ILogArea;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.service.CEPLoadParser;
import oracle.cep.util.StringUtil;

public class TkJDBCLoggingTest2 extends TkJDBCTestBase
{
  public static void main(String[] args) throws Exception
  {
    TkJDBCLoggingTest2 test = new TkJDBCLoggingTest2();
    test.init(args);
    test.run();
    test.exit();
  }
     
  protected void runTest() throws Exception
  {
    LogUtil.info(LoggerType.TRACE, "================================================");
    String t_work = workFolder;
    String cqlengine_root = wlevs_home + 
                            File.separator + "modules" + 
                            File.separator + "cqlengine";
    String test_root = cqlengine_root + File.separator + "test";
    String testDataFolder = test_root + File.separator + "data";
    String testSqlFolder = test_root + File.separator + "sql";
    String testOutputFolder = t_work + File.separator + "cep";
    String fileName = testSqlFolder + File.separator + "tklog.cql";
    HashMap<String, String> valMap = new HashMap<String, String>();
    valMap.put("TEST_DATA", testDataFolder);
    valMap.put("TEST_OUTPUT", testOutputFolder);
    StringBuilder cqlxml = new StringBuilder();
    try
    {
      BufferedReader reader = new BufferedReader(new FileReader(fileName));
      String str;
      while ((str = reader.readLine()) != null)
      {
        String line = StringUtil.expand(str, valMap);
        cqlxml.append(line);
        cqlxml.append("\n");
      }
      reader.close();
    }
    catch (Exception e)
    {
      LogUtil.severe(LoggerType.TRACE, "IO Error while reading " + fileName + "!\n" 
                    + e.toString());
      throw e;
    }
    CEPLoadParser loadParser = new CEPLoadParser();
    boolean b = loadParser.parseStr(cqlxml.toString(), false /* retry on validation */);
    if (!b)
    {
      LogUtil.severe(LoggerType.TRACE, "Failed to parse\n" + cqlxml); 
      throw new Exception();
    }
    List<String> ddls = loadParser.getLoadDDLs();
    for (String ddl : ddls)
    {
      stmt.executeUpdate(ddl);
    }
    
    System.out.println("enable tracing...");
    // enable logging
    for (ILogArea a : LogArea.values())
    {
      if (a == LogArea.METADATA_CACHE || a == LogArea.SYSTEMSTATE)
        continue;
      String area = a.getName();
      if (a == LogArea.QUERY)
        a = LogArea.OPERATOR;
      String events = getEventsForArea(a);
      String levels = getLevelsForArea(a);
      if (levels == null)
      {
        System.out.println("no levels from area : " + area);
        continue;
      }
      String ddl;
      if (events != null)
      {
        ddl = String.format("alter system enable logging %s event %s level %s", area, events, levels);
      }
      else
      {
        ddl = String.format("alter system enable logging %s level %s", area, levels);
      }
      System.out.println(ddl);
      stmt.executeUpdate(ddl);
    }
    
    System.out.println("Running...");
    stmt.executeUpdate("alter system run");

    System.out.println("Completed...start dumping");
    // dumps
    for (ILogArea a : LogArea.values())
    {
      if (a == LogArea.METADATA_CACHE)
        continue;
      String area = a.getName();
      if (a == LogArea.QUERY)
        a = LogArea.OPERATOR;
      String levels = getLevelsForArea(a);
      if (levels == null)
      {
        System.out.println("no levels from area : " + area);
        continue;
      }
      String ddl = String.format("alter system dump logging %s level %s", area, levels);
      System.out.println(ddl);
      stmt.executeUpdate(ddl);
    }
    System.out.println("Completed...");
  }

  private String getEventsForArea(ILogArea area)
  {
    ILogEvent[] events = LogEvent.getEvents(area);
    if (events == null || events.length == 0)
      return null;
    StringBuilder b = new StringBuilder();
    int i = 0;
    for (ILogEvent ev : events)
    {
      if (ev.getLogArea() == area) 
      {
        if (i > 0)
          b.append(",");
        i++;
        b.append(ev.getValue());
      }
    }
    return b.toString();
  }
  
  private String getLevelsForArea(ILogArea area)
  {
    int[] levels = LogLevel.getLevels(area);
    if (levels == null || levels.length == 0)
      return null;
    StringBuilder b = new StringBuilder();
    int i = 0;
    for (int level : levels)
    {
      if (i > 0)
        b.append(",");
      i++;
      b.append(level);
    }
    return b.toString();
  }
  
}
