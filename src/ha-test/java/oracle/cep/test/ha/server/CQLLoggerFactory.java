package oracle.cep.test.ha.server;

import oracle.cep.logging.ILoggerFactory;
import oracle.cep.logging.LoggerType;

import java.util.logging.Logger;

import org.apache.commons.logging.Log;


/**
 * CQLLoggerFactory implements cqlengine's ILoggerFactory.
 * 
 *  @version $Header: beam/main/modules/cqservice/core/src/main/java/com.oracle.cep.spark/CQLLoggerFactory.java /main/4 2014/01/21 21:38:13 shusun Exp $
 *  @author  hopark  
 *  @since   12c
 */

public class CQLLoggerFactory implements ILoggerFactory
{
  private Log loggers[];
  private String[] loggerNames;
  static CQLLoggerFactory s_instance;
  
 
  public static synchronized CQLLoggerFactory getInstance() {
	if (s_instance == null)
		s_instance = new CQLLoggerFactory();
	return s_instance;
  }
  
  public static Log getCustomerLog() {
    return s_instance.getLogger(LoggerType.CUSTOMER);
  }
  
  public static Log getTraceLog() {
    return s_instance.getLogger(LoggerType.TRACE);
  }
  
  private CQLLoggerFactory()
  {
    int sz = LoggerType.MAX.ordinal();
    loggers = new Log[sz];
    loggerNames = new String[sz];
    setLoggerName(LoggerType.CUSTOMER, "com.oracle.cep.spark");
    setLoggerName(LoggerType.TRACE, "com.oracle.cep.spark.trace");
    
    Log log = getLogger(LoggerType.CUSTOMER);
    log.trace("CQLLoggerFactory instantiated");
  }
  
  public String getLoggerName(LoggerType loggerType)
  {
    int idx = loggerType.ordinal();
    if (idx >= loggerNames.length)
    {
      return null;
    }
    return loggerNames[idx];
  }

  public void setLoggerName(LoggerType loggerType, String loggerName)
  {
    int idx = loggerType.ordinal();
    if (idx >= loggerNames.length)
    {
      return;
    }
    loggerNames[idx] = loggerName;
    loggers[idx] = new JavaLoggerWrapper(Logger.getLogger(loggerName));
  }  
  
  public Log getLogger(LoggerType loggerType)
  {
    return loggers[loggerType.ordinal()];
  }
}
