package oracle.cep.logging.impl;

import oracle.cep.logging.ILoggerFactory;
import oracle.cep.logging.LoggerType;

import org.apache.commons.logging.Log;

import java.util.logging.Logger;

public class JavaLoggerFactory implements ILoggerFactory {
    private String[] loggerNames;
    private String[] loggerResources;

    public JavaLoggerFactory()
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
                logger = Logger.getLogger(loggerName, resource);
                break;
            default:
                logger = Logger.getLogger(loggerName);
                break;
        }
        return new JavaLoggerWrapper(logger);
    }
}
