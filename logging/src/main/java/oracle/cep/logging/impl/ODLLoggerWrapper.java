/* $Header: pcbpel/cep/logging/src/oracle/cep/logging/impl/ODLLoggerWrapper.java /main/1 2009/05/01 16:16:48 hopark Exp $ */

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
 *  @version $Header: pcbpel/cep/logging/src/oracle/cep/logging/impl/ODLLoggerWrapper.java /main/1 2009/05/01 16:16:48 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logging.impl;

import org.apache.commons.logging.Log;

import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.core.ojdl.logging.ODLLevel;

public class ODLLoggerWrapper implements Log
{
  Logger m_logger = null;
  
  public ODLLoggerWrapper(Logger logger)
  {
    m_logger = logger;
  }
  
  // Only for unit testing..
  public Logger getLogger() {return m_logger;}
  
  @Override
  public void trace(Object arg0)
  {
    m_logger.finest(arg0 == null ? "null":arg0.toString());
  }

  @Override
  public void trace(Object arg0, Throwable arg1)
  {
    m_logger.finest(arg0 == null ? "null":arg0.toString()+" : " + arg1.toString());
  }


  @Override
  public boolean isTraceEnabled()
  {
    return m_logger.isLoggable(Level.FINEST);
  }
  
  @Override
  public void debug(Object arg0)
  {
    m_logger.fine(arg0 == null ? "null":arg0.toString());
  }

  @Override
  public void debug(Object arg0, Throwable arg1)
  {
    m_logger.fine(arg0 == null ? "null":arg0.toString()+" : " + arg1.toString());
  }


  @Override
  public boolean isDebugEnabled()
  {
    return m_logger.isLoggable(Level.FINE);
  }
  
  @Override
  public void info(Object arg0)
  {
    m_logger.info(arg0 == null ? "null":arg0.toString());
  }

  @Override
  public void info(Object arg0, Throwable arg1)
  {
    m_logger.info(arg0 == null ? "null":arg0.toString()+" : " + arg1.toString());
  }


  @Override
  public boolean isInfoEnabled()
  {
    return m_logger.isLoggable(Level.INFO);
  }

  @Override
  public void warn(Object arg0)
  {
    m_logger.warning(arg0 == null ? "null":arg0.toString());
  }

  @Override
  public void warn(Object arg0, Throwable arg1)
  {
    m_logger.warning(arg0 == null ? "null":arg0.toString()+" : " + arg1.toString());
  }
  
  @Override
  public boolean isWarnEnabled()
  {
    return m_logger.isLoggable(Level.WARNING);
  }

  @Override
  public void error(Object arg0)
  {
    m_logger.severe(arg0 == null ? "null":arg0.toString());
  }

  @Override
  public void error(Object arg0, Throwable arg1)
  {
    m_logger.severe(arg0 == null ? "null":arg0.toString()+" : " + arg1.toString());
  }


  @Override
  public boolean isErrorEnabled()
  {
    return m_logger.isLoggable(Level.SEVERE);
  }

  @Override
  public void fatal(Object arg0)
  {
    m_logger.log(ODLLevel.INCIDENT_ERROR, arg0 == null ? "null":arg0.toString());
  }

  @Override
  public void fatal(Object arg0, Throwable arg1)
  {
    m_logger.log(ODLLevel.INCIDENT_ERROR, arg0 == null ? "null":arg0.toString()+" : " + arg1.toString());
  }

  @Override
  public boolean isFatalEnabled()
  {
    return m_logger.isLoggable(ODLLevel.INCIDENT_ERROR);
  }

}
