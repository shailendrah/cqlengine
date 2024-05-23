package oracle.cep.test.ha.server;

import org.apache.commons.logging.Log;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JavaLoggerWrapper wraps the java logger into apache commons logging.
 * 
 *  @version $Header: beam/main/modules/cqservice/core/src/main/java/com.oracle.cep.spark/JavaLoggerWrapper.java /main/2 2012/04/07 20:02:54 hopark Exp $
 *  @author  hopark  
 *  @since   12c
 */

public class JavaLoggerWrapper implements Log
{
  Logger m_logger = null;
  
  public JavaLoggerWrapper(Logger logger)
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
    m_logger.log(Level.SEVERE, arg0 == null ? "null":arg0.toString());
  }

  @Override
  public void fatal(Object arg0, Throwable arg1)
  {
    m_logger.log(Level.SEVERE, arg0 == null ? "null":arg0.toString()+" : " + arg1.toString());
  }

  @Override
  public boolean isFatalEnabled()
  {
    return m_logger.isLoggable(Level.SEVERE);
  }

}
