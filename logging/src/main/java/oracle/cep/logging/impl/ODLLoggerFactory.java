/* $Header: pcbpel/cep/logging/src/oracle/cep/logging/impl/ODLLoggerFactory.java /main/1 2009/05/01 16:16:47 hopark Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/logging/src/oracle/cep/logging/impl/ODLLoggerFactory.java /main/1 2009/05/01 16:16:47 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logging.impl;

import java.util.logging.Logger;

import oracle.cep.logging.ILoggerFactory;
import oracle.cep.logging.LoggerType;
import oracle.core.ojdl.logging.ODLLogger;

import org.apache.commons.logging.Log;

public class ODLLoggerFactory implements ILoggerFactory
{
  private String[] loggerNames;
  private String[] loggerResources;

  public ODLLoggerFactory() 
  {
    int sz = LoggerType.MAX.ordinal();
    loggerNames = new String[sz];
    loggerResources = new String[sz];
    loggerNames[LoggerType.CUSTOMER.ordinal()] = "oracle.soa.cep";
    loggerNames[LoggerType.TRACE.ordinal()] = "oracle.soa.cep.trace";
  }

  public String getLoggerName(LoggerType loggerType)
  {
    int idx = loggerType.ordinal();
    if (idx >= loggerNames.length)
    {
      System.out.println("Invalid loggerType used : " + loggerType.toString());
      assert false : "Invalid loggerType";
      return null;
    }
    return loggerNames[idx];
  }

  public void setLoggerName(LoggerType loggerType, String loggerName)
  {
    int idx = loggerType.ordinal();
    if (idx >= loggerNames.length)
    {
      System.out.println("Invalid loggerType used : " + loggerType.toString());
      assert false : "Invalid loggerType";
      return;
    }
    loggerNames[idx] = loggerName;
  }  
  
  public String getLoggerResources(LoggerType loggerType)
  {
    int idx = loggerType.ordinal();
    if (idx >= loggerResources.length)
    {
      System.out.println("Invalid loggerType used : " + loggerType.toString());
      assert false : "Invalid loggerType";
      return null;
    }
    return loggerResources[idx];
  }   
  
  public void setLoggerResources(LoggerType loggerType, String res)
  {
    int idx = loggerType.ordinal();
    if (idx >= loggerResources.length)
    {
      System.out.println("Invalid loggerType used : " + loggerType.toString());
      assert false : "Invalid loggerType";
      return;
    }
    loggerResources[idx] = res;
  }  
  
  public Log getLogger(LoggerType loggerType)
  {
    String resource = loggerResources[loggerType.ordinal()];
    String loggerName = loggerNames[loggerType.ordinal()];
    Logger logger = null;
    switch (loggerType)
    {
    case TRACE:
      logger = Logger.getLogger(loggerName, resource);
      logger.setUseParentHandlers(false);
      break;
    case CUSTOMER:
      logger = ODLLogger.getLogger(loggerName, resource);
      break;
    default:
      logger = Logger.getLogger(loggerName);
      break;
     }
     return new ODLLoggerWrapper(logger);
  }
}
