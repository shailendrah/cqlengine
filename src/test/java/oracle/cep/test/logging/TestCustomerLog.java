/* $Header: pcbpel/cep/test/src/oracle/cep/test/logging/TestCustomerLog.java /main/4 2009/05/01 16:16:48 hopark Exp $ */

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
    hopark      11/19/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/logging/TestCustomerLog.java /main/4 2009/05/01 16:16:48 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.CustomerLogMsg;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.impl.JavaLoggerWrapper;
import oracle.cep.test.logging.TestDump.MyMemoryHandler;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestCustomerLog extends TestCase
{
  MyMemoryHandler m_handler;

  public TestCustomerLog(String test)
  {
    super(test);
  }
  
  public void testCustomerLogMsg() throws CEPException
  {
    String msg = CEPException.getMessage(CustomerLogMsg.ACTIVATE_DDL_FAILURE, "Test");
    assertEquals(msg, "DDL  invocation is failed. DDL was Test");
  }
  

  public void testCustomerLog() throws CEPException
  {
    JavaLoggerWrapper logger = (JavaLoggerWrapper) LogUtil.getLogger(LoggerType.CUSTOMER);
    Logger customerLogger = logger.getLogger();
    m_handler = new MyMemoryHandler();
    customerLogger.addHandler(m_handler);
    m_handler.setLevel(Level.ALL);
    customerLogger.setLevel(Level.ALL);

    String ddl = "Test DDL";
    LogUtil.info(LoggerType.CUSTOMER, 
        CEPException.getMessage(CustomerLogMsg.ACTIVATE_DDL, ddl));
    LogUtil.info(LoggerType.CUSTOMER, 
        CEPException.getMessage(CustomerLogMsg.ACTIVATE_DDL_FAILURE, ddl));
    LogUtil.info(LoggerType.TRACE, 
        CEPException.getMessage(CustomerLogMsg.ACTIVATE_DDL, ddl));
    LogUtil.info(LoggerType.TRACE, 
        CEPException.getMessage(CustomerLogMsg.ACTIVATE_DDL_FAILURE, ddl));
    
    for (LogRecord l : m_handler.m_records)
    {
      System.out.println(l.getMessage());
    }
  }
  
  public static Test suite()
  {
    if (SINGLE_TEST_NAME != null)
    {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestCustomerLog(SINGLE_TEST_NAME));
      return suite;
    } else {
      return new TestSuite(TestCustomerLog.class);
    }
  }
  
  public static final String SINGLE_TEST_NAME = "testCustomerLog";
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(TestCustomerLog.suite());
  }
}
