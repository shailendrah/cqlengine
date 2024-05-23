/* $Header: cep/wlevs_cql/modules/cqlengine/logging/src/oracle/cep/logging/LogUtil.java /main/4 2010/03/20 08:53:21 sbishnoi Exp $ */

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
    sbishnoi    03/18/10 - changing default logging level to FINE
    hopark      01/27/10 - set default loggerFactory
    hopark      04/21/09 - use ILoggerFactory
    hopark      05/15/08 - limit the stacktrace to cep
    hopark      05/10/08 - fix npe
    hopark      03/26/08 - server reorg
    hopark      02/26/08 - use fine level for trace
    hopark      02/05/08 - check void dumper
    hopark      12/26/07 - add createDumper
    hopark      12/20/07 - add xmltag config
    hopark      12/06/07 - cleanup spill
    hopark      11/08/07 - handle exception
    hopark      08/28/07 - perf enh
    hopark      08/02/07 - handle dump
    hopark      05/17/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/logging/src/oracle/cep/logging/LogUtil.java /main/4 2010/03/20 08:53:21 sbishnoi Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;

import oracle.cep.logging.LoggerType;
import oracle.cep.logging.impl.JavaLoggerFactory;

import org.apache.commons.logging.Log; 

public class LogUtil
{
  public static final String TAG_LEVELS = "Levels";
  // Trace Event
  public static final String EVENT = "Event";
  public static final String ATTR_EVENT[] = {"Name"};
  public static final String ATTR_EVENTTARGET[] = {"Name", "TargetId", "TargetName"};

  // Trace Level 
  public static final String LEVEL = "Level";
  public static final String ATTR_LEVEL[] = {"Name", "Value"};
  
  // Stack trace
  public static final String STACKTRACE = "Stacktrace";
  
  // Event arguments (Arg0, Arg1, etc)
  public static final String ARG_POS = "Arg";

  // Dump Error
  public static final String DUMP_ERR = "DumpErr";
  
  // Default array attributes
  public static final String ARRAY_ATTRIBS[] = {"Length"};
    
  
   //default logger factory for unit tests
  //it will be overriden by the envconfig.
  private static ILoggerFactory s_loggerFactory =  new JavaLoggerFactory();
  
  private static HashMap<Level, Integer> levelMap  = null;
  
  static
  {
    levelMap 
    = new HashMap<Level,Integer>();
    levelMap.put(Level.SEVERE, 1);
    levelMap.put(Level.WARNING, 2);
    levelMap.put(Level.INFO, 3);
    levelMap.put(Level.CONFIG, 3);
    levelMap.put(Level.FINE, 4);
    levelMap.put(Level.FINER, 5);
    levelMap.put(Level.FINEST, 6);  
  }

  public static void setLoggerFactory(ILoggerFactory factory)
  {
    s_loggerFactory = factory;
  }

  public static Log getLogger(LoggerType loggerType)
  {
    return s_loggerFactory.getLogger(loggerType);
  }
    
  public static void severe(LoggerType loggerType, String msg)
  {
    Log logger = s_loggerFactory.getLogger(loggerType);
    if (logger == null)
    {
      System.out.println(loggerType.toString() + " : Fatal : " + msg);
      return;
    }
    logger.fatal(msg);
  }
  
  public static boolean isSevereEnabled(LoggerType loggerType)
  {
    Log logger = s_loggerFactory.getLogger(loggerType);
    if (logger == null)
      return false;
    return logger.isFatalEnabled();
  }
  
  public static void warning(LoggerType loggerType, String msg)
  {
    Log logger = s_loggerFactory.getLogger(loggerType);
    if (logger == null)
    {
      System.out.println(loggerType.toString() + " : Warn : " + msg);
      return;
    }
    logger.warn(msg);
  }
  
  public static boolean isWarningEnabled(LoggerType loggerType)
  {
    Log logger = s_loggerFactory.getLogger(loggerType);
    if (logger == null)
      return false;
    return logger.isWarnEnabled();
  }
  
  public static void info(LoggerType loggerType, String msg)
  {
    Log logger = s_loggerFactory.getLogger(loggerType);
    if (logger == null)
    {
      return;
    }
    logger.info(msg);
  }

  public static boolean isInfoEnabled(LoggerType loggerType)
  {
    Log logger = s_loggerFactory.getLogger(loggerType);
    if (logger == null)
      return false;
    return logger.isInfoEnabled();
  }
  
  public static void config(LoggerType loggerType, String msg)
  {
    Log logger = s_loggerFactory.getLogger(loggerType);
    if (logger == null)
    {
      return;
    }
    logger.info(msg);
  }
  
  public static boolean isConfigEnabled(LoggerType loggerType)
  {
    Log logger = s_loggerFactory.getLogger(loggerType);
    if (logger == null)
      return false;
    return logger.isInfoEnabled();
  }
  
  public static void fine(LoggerType loggerType, String msg)
  {
    Log logger = s_loggerFactory.getLogger(loggerType);
    if (logger == null)
    {
      return;
    }
    logger.debug(msg);
  }

  public static boolean isFineEnabled(LoggerType loggerType)
  {
    Log logger = s_loggerFactory.getLogger(loggerType);
    if (logger == null)
      return false;
    return logger.isDebugEnabled();
  }
    
  public static void finer(LoggerType loggerType, String msg)
  {
    Log logger = s_loggerFactory.getLogger(loggerType);
    if (logger == null)
    {
      return;
    }
    logger.trace(msg);
  }

  public static boolean isFinerEnabled(LoggerType loggerType)
  {
    Log logger = s_loggerFactory.getLogger(loggerType);
    if (logger == null)
      return false;
    return logger.isTraceEnabled();
  }
  
  public static void finest(LoggerType loggerType, String msg)
  {
    Log logger = s_loggerFactory.getLogger(loggerType);
    if (logger == null)
    {
      return;
    }
    logger.trace(msg);
  }

  public static boolean isFinestEnabled(LoggerType loggerType)
  {
    Log logger = s_loggerFactory.getLogger(loggerType);
    if (logger == null)
      return false;
    return logger.isTraceEnabled();
  }
  
  public static void logStackTrace(Throwable cause)
  {
    //Note: Default Logging Level should be FINE
    // Reason: It will only dump messages when log levels will be
    // set to DEBUG OR TRACE
    logStackTrace(LoggerType.TRACE, Level.FINE, cause);
  }
  
  /**
   * Log the Error Message
   * @param loggerType Target Logger
   * @param level Target Logging Level
   * @param cause Cause of the Error
   */
  public static void logStackTrace(LoggerType loggerType, Level level, 
                                   Throwable cause)
  {  
    assert (cause != null);
    
    Log logger = s_loggerFactory.getLogger(loggerType);
    String errorMessage = cause.getMessage();
    
    

    // Dump the Error Message to console if no logger defined
    // else dump the error message to logger
    if (logger == null)
    {
      Writer result = new StringWriter();
      PrintWriter printWriter = new PrintWriter(result);
      cause.printStackTrace(printWriter);
      String trace = result.toString();
      System.out.println(loggerType.toString() + "Error:" + errorMessage);
      System.out.println("Caused By:\n" + trace);
    }
    else
    {
      dump(logger, level, errorMessage, cause);
    }
  }
    
  /**
   * Helper method to dump the error message
   * @param logger Target Logger
   * @param level Target Logging Level
   * @param message Error Message
   * @param cause Cause of the Error
   */
  private static void dump(Log logger, Level level, String message, 
                           Throwable cause)
  {
    Integer levelIntVal = levelMap.get(level);
    int levelVal = (levelIntVal != null) ? levelIntVal.intValue() : -1;
    
    switch(levelVal)
    {
    case 1:
      logger.fatal(message, cause);
      break;
    case 2:
      logger.warn(message, cause);
      break;
    case 3:
      logger.info(message, cause);
      break;
    case 4:
      logger.debug(message, cause);
      break;
    case 5:
      logger.trace(message, cause);
      break;
    default:
      logger.trace(message, cause);
    };    
  }
  
  public static String beginDumpObj(IDumpContext dumper, Object target)
  {
    String tag = null;
    DumpDesc dumpdesc = getDumpDesc(target);
    if (dumpdesc != null)
    {
      Class<?> targetClass = target.getClass();
      tag = dumpdesc.tag();
      if (tag.length() == 0) 
        tag = target.getClass().getSimpleName();
      // build attributes
      String[] attribTags = dumpdesc.attribTags();
      assert (attribTags.length == dumpdesc.attribVals().length);
      String[] valueTags = dumpdesc.valueTags();
      assert (valueTags.length == dumpdesc.values().length);
      Object[] attribVals = null;
      Object[] vals = null;
      // annotation parameter cannot be null.
      // so the default of String[] = ""
      if (attribTags.length > 1 || attribTags[0].length() > 0)
        attribVals = new Object[attribTags.length];
      if (valueTags.length > 1 || valueTags[0].length() > 0)
        vals  = new Object[valueTags.length];
      for (int i = 0; i < 2; i++)
      {
        String[] getters = (i == 0) ? dumpdesc.attribVals() : dumpdesc.values();
        Object[] dest = (i == 0) ? attribVals : vals;
        if (dest == null) continue;
        int pos = -1;
        for (String avalgetter : getters)
        {
          pos++;
          dest[pos] = null;
          if (avalgetter.length() == 0) continue;
          String fname = null;
          if (avalgetter.startsWith("@"))
          {
            // use field to get value
            fname = avalgetter.substring(1);
          }
          Exception err = null;
          Class<?> tc = targetClass;
          while (err == null && tc != null)
          {
            try
            {
              if (fname != null)
              {
                Field f = tc.getDeclaredField(fname);
                f.setAccessible(true);
                dest[pos] = f.get(target);
                break;
              }
              else
              {
                Method m = tc.getMethod(avalgetter, (Class[])null);
                dest[pos] = m.invoke(target, (Object[]) null);
                break;
              }
            }
            catch (NoSuchMethodException me)
            {
            }
            catch (NoSuchFieldException fe)
            {
            }
            catch (SecurityException se)
            {
              err = se;
            }
            catch (IllegalAccessException ae)
            {
              err = ae;
            }
            catch (IllegalArgumentException ae)
            {
              err = ae;
            }
            catch (InvocationTargetException te)
            {
              err = te;
            }
            tc = tc.getSuperclass();
          }
          if (err != null)
          {
            assert false :targetClass.getName() + "." + avalgetter + " " + err.toString();
          }
        }
      }
      dumper.beginTag(tag, attribTags, attribVals);
      if (vals != null)
      {
        int pos = 0;
        for (String valTag : valueTags)
        {
          Object val = vals[pos++];
          logTagVal(dumper, valTag, val);
        }
      }
      else
      {
        if (dumpdesc.autoFields())
          dumpFields(dumper, target);
      }
    }
    else
    {
      tag = target.getClass().getSimpleName();
      dumper.beginTag(tag, null, null);
    }
    
    return tag;
  }
  
  public static void endDumpObj(IDumpContext dumper, String tag)
  {
    assert (tag != null);
    dumper.endTag(tag);
  }
  
  public static void beginTag(IDumpContext dumper, String tag, 
                             String[] attrTags, Object... args)
  {
    assert (attrTags.length == args.length);
    Object[] vals = new Object[attrTags.length];
    int i = 0;
    for (Object v : args)
      vals[i++] = v;
    dumper.beginTag(tag, attrTags, vals);
  }
  
  public static void dumpFields(IDumpContext w, Object target)
  {
    Class<?> c = target.getClass();
    while (c != null)
    {
      if (c == Object.class) break;
      Field[] fields = c.getDeclaredFields();
      if (fields != null)
      {
        for (Field f : fields)
        {
          DumpDesc dumpdesc = f.getAnnotation(DumpDesc.class);
          if (dumpdesc != null && dumpdesc.ignore())
          {
            continue;
          }
          //ignore static field
          if (Modifier.isStatic(f.getModifiers()))
            continue;
          String fname = f.getName();
          //ignore java internal fields such as $assertionsDisabled
          if (fname.startsWith("$")) 
            continue;
          if (dumpdesc != null && dumpdesc.tag().length() == 0)
          {
            fname = dumpdesc.tag();
          }
          try
          {
            f.setAccessible(true);
            Object v = f.get(target);
            logTagVal(w, fname, v);
          }
          catch (IllegalAccessException e)
          {
          }
        }
      }
      c = c.getSuperclass();
    }
  }
  
  public static DumpDesc getDumpDesc(Object target)
  {
    Class<?> targetClass = target.getClass();
    while (targetClass != null)
    {
      DumpDesc dumpdesc = targetClass.getAnnotation(DumpDesc.class);
      if (dumpdesc != null)
        return dumpdesc;
      targetClass =targetClass.getSuperclass();
    }
    return null;
   }

  public static String getTag(Object arg)
  {
    DumpDesc dumpdesc = getDumpDesc(arg);
    String tag = null;
    if (dumpdesc != null)
       tag = dumpdesc.tag();
    if (tag == null || tag.length() == 0)
      tag = arg.getClass().getSimpleName();
    return tag;
  }
  
  @SuppressWarnings("unchecked")
  public static void logTagVal(IDumpContext dumper, String tag, Object arg)
  {
    if (arg == null)
    {
      dumper.writeln(tag, "null");
      return;
    }
    if (arg instanceof IDumpable)
    {
      if (tag != null) dumper.beginTag(tag, null, null);
      ((IDumpable)arg).dump(dumper);
      if (tag != null) dumper.endTag(tag);
    }
    else if (arg instanceof Collection) 
    {
      if (tag == null)
        tag = getTag(arg);
      Collection<Object> c = (Collection<Object>) arg;
      Object[] avals = new Object[1];
      avals[0] = c.size();
      dumper.beginTag(tag, ARRAY_ATTRIBS, avals);
      String etag = tag + "Elem";
      for (Object o : c)
      {
        logTagVal(dumper, etag, o);
      }
      dumper.endTag(tag);
    }
    else if ( arg instanceof Object[])
    {
      if (tag == null)
        tag = getTag(arg);
      Object[] c = (Object[]) arg;
      Object[] avals = new Object[1];
      avals[0] = c.length;
      dumper.beginTag(tag, ARRAY_ATTRIBS, avals);
      String etag = tag + "Elem";
      for (Object o : c)
      {
        logTagVal(dumper, etag, o);
      }
      dumper.endTag(tag);
    }
    else
    {
      DumpDesc dumpdesc = getDumpDesc(arg);
      if (dumpdesc != null)
      {
        String tag1 = beginDumpObj(dumper, arg);
        endDumpObj(dumper, tag1);
      } 
      else
      {
        if (tag == null)
          tag = getTag(arg);
        dumper.writeln(tag, arg.toString());
      }
    }
  }

  public static void logMsg(IDumpContext dumper, int level, ILoggable target, Object[] args)
  {
    if (target == null && args == null) return;
     
    if (args != null)
    {
      int argpos = 0;
      for (Object arg : args)
      {
        //TODO maybe the caller can pass the name of arguments?
        String argName = ARG_POS + argpos++;
        if (arg == null)
        {
          dumper.writeln(argName, "null");
          continue;
        }
        logTagVal(dumper, argName, arg);
      }
    }
  }
  
}
