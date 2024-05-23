/* $Header: pcbpel/cep/test/src/oracle/cep/test/logging/TestDump.java /main/4 2009/05/01 16:16:48 hopark Exp $ */

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
 hopark      06/18/08 - logging refactor
 hopark      06/28/07 - support plan change
 parujain    06/26/07 - fix activate
 hopark      05/29/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/logging/TestDump.java /main/4 2009/05/01 16:16:48 hopark Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.logging;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.impl.JavaLoggerWrapper;

import junit.framework.Test;
import junit.framework.TestSuite;


public class TestDump extends TestLogging
{
  MyMemoryHandler m_handler;
  
  public static class MyMemoryHandler extends Handler
  {
    List<LogRecord> m_records;
    
    public MyMemoryHandler()
    {
      super();
      clear();
    }

    public void clear()
    {
      m_records = new LinkedList<LogRecord>();
    }
    
    public void close() throws SecurityException
    {
    }

    public void flush()
    {
    }

    public void publish(LogRecord record)
    {
      System.out.println(record.getMessage());
      m_records.add(record);
    }
  }

  public TestDump(String name)
  {
    super(name);
    
    JavaLoggerWrapper logger = (JavaLoggerWrapper) LogUtil.getLogger(LoggerType.TRACE);
    Logger traceLogger = logger.getLogger();
    m_handler = new MyMemoryHandler();
    traceLogger.addHandler(m_handler);
    m_handler.setLevel(Level.ALL);
    traceLogger.setLevel(Level.ALL);
  }
  
  public void setUp()
  {
    super.setUp();
    m_handler.clear();
  }
  
  protected void check(List<CheckDesc> items)
  {
    System.out.println("-------------------------");
    for (LogRecord l : m_handler.m_records)
    {
      System.out.println(l.getMessage());
    }
  }
  
  protected boolean isDump() {return true;}
  
  public static Test suite()
  {
    if (SINGLE_TEST)
    {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestDump(SINGLE_TEST_NAME));
      return suite;
    } else {
      return new TestSuite(TestDump.class);
    }
  }
 
 public static final boolean SINGLE_TEST = true;
 public static final String SINGLE_TEST_NAME = "testSysState_Level0";
 
 public static void main(String[] args)
 {
   junit.textui.TestRunner.run(TestDump.suite());
 }
}
