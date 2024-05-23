package com.oracle.cep.cartridge.java;

import java.util.Locale;

import weblogic.i18n.Localizer;
import weblogic.i18n.logging.CatalogMessage;
import weblogic.i18n.logging.MessageLogger;
import weblogic.i18n.logging.MessageLoggerRegistry;
import weblogic.i18n.logging.MessageLoggerRegistryListener;
import weblogic.i18n.logging.MessageResetScheduler;
import weblogic.i18ntools.L10nLookup;
import weblogic.logging.Loggable;

/** 
 * Copyright (c) 2003,2014, Oracle and/or its affiliates. All rights reserved.
 * @exclude
 */
public class JavaCartridgeLogger
{
  private static final String LOCALIZER_CLASS = "com.oracle.cep.cartridge.java.JavaCartridgeLogLocalizer";
  
  private static MessageLogger findMessageLogger() {
    return MessageLoggerRegistry.findMessageLogger(JavaCartridgeLogger.class.getName());
  }

  private static final class MessageLoggerInitializer implements MessageLoggerRegistryListener {

    private static final MessageLoggerInitializer INSTANCE = new MessageLoggerInitializer();
    
    private static final Localizer LOCALIZER = L10nLookup.getLocalizer(
      Locale.getDefault(), LOCALIZER_CLASS, JavaCartridgeLogger.class.getClassLoader());
    
    private MessageLogger messageLogger = findMessageLogger();
    
    private MessageLoggerInitializer() {
      MessageLoggerRegistry.addMessageLoggerRegistryListener(this);      
    }
            
    public void messageLoggerRegistryUpdated() {
      messageLogger = findMessageLogger();
    }
  }

  /**
   * No matching method has been found for class: "{0}". Expected method = "{1}", but found = "{2}".
   *
   * messageid:  2069100
   * severity:   warning
   */
  public static String noSuchMethodException(String arg0, String arg1, String arg2)  {
    Object [] args = { arg0, arg1, arg2 };
    Loggable l = (new Loggable("2069100", 16, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, JavaCartridgeLogger.class.getClassLoader()));
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l.getMessage();	
  }

  public static Loggable noSuchMethodExceptionLoggable(String arg0, String arg1, String arg2)  {
    Object[] args = { arg0, arg1, arg2 };
    Loggable l = new Loggable("2069100", 16, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, JavaCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * No matching constructor has been found for class: "{0}". Expected constructor = "{1}", but found = "{2}".
   *
   * messageid:  2069101
   * severity:   warning
   */
  public static String noSuchConstructorException(String arg0, String arg1, String arg2)  {
    Object [] args = { arg0, arg1, arg2 };
    Loggable l = (new Loggable("2069101", 16, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, JavaCartridgeLogger.class.getClassLoader()));
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l.getMessage();	
  }

  public static Loggable noSuchConstructorExceptionLoggable(String arg0, String arg1, String arg2)  {
    Object[] args = { arg0, arg1, arg2 };
    Loggable l = new Loggable("2069101", 16, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, JavaCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * Invalid usage of "{0}" function. Function expects "{1}" arguments. Actual number of arguments used was = "{2}".
   *
   * messageid:  2069102
   * severity:   error
   */
  public static String invalidNumberOfArgumentsForFunction(String arg0, Integer arg1, Integer arg2)  {
    Object [] args = { arg0, arg1, arg2 };
    Loggable l = (new Loggable("2069102", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, JavaCartridgeLogger.class.getClassLoader()));
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l.getMessage();	
  }

  public static Loggable invalidNumberOfArgumentsForFunctionLoggable(String arg0, Integer arg1, Integer arg2)  {
    Object[] args = { arg0, arg1, arg2 };
    Loggable l = new Loggable("2069102", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, JavaCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * Illegal argument type for "{0}" function. Function expected type of "{1}" for argument number "{2}". Actual type used was = "{3}".
   *
   * messageid:  2069103
   * severity:   error
   */
  public static String illegalArgumentTypeForFunction(String arg0, String arg1, Integer arg2, String arg3)  {
    Object [] args = { arg0, arg1, arg2, arg3 };
    Loggable l = (new Loggable("2069103", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, JavaCartridgeLogger.class.getClassLoader()));
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l.getMessage();	
  }

  public static Loggable illegalArgumentTypeForFunctionLoggable(String arg0, String arg1, Integer arg2, String arg3)  {
    Object[] args = { arg0, arg1, arg2, arg3 };
    Loggable l = new Loggable("2069103", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, JavaCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * Illegal cast from type "{0}" to type "{1}".
   *
   * messageid:  2069104
   * severity:   error
   */
  public static String illegalTypeCast(String arg0, String arg1)  {
    Object [] args = { arg0, arg1 };
    Loggable l = (new Loggable("2069104", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, JavaCartridgeLogger.class.getClassLoader()));
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l.getMessage();	
  }

  public static Loggable illegalTypeCastLoggable(String arg0, String arg1)  {
    Object[] args = { arg0, arg1 };
    Loggable l = new Loggable("2069104", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, JavaCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * Failed to convert from "{0}" to native CQL type "{1}".
   *
   * messageid:  2069105
   * severity:   error
   */
  public static String illegalConvert(String arg0, String arg1)  {
    Object [] args = { arg0, arg1 };
    Loggable l = (new Loggable("2069105", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, JavaCartridgeLogger.class.getClassLoader()));
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l.getMessage();	
  }

  public static Loggable illegalConvertLoggable(String arg0, String arg1)  {
    Object[] args = { arg0, arg1 };
    Loggable l = new Loggable("2069105", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, JavaCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * Failed to convert from oracle.sql.TIMESTAMP type to native CQL type "{0}".
   *
   * messageid:  2069106
   * severity:   error
   */
  public static String illegalTimestampConvert(String arg0)  {
    Object [] args = { arg0 };
    Loggable l = (new Loggable("2069106", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, JavaCartridgeLogger.class.getClassLoader()));
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l.getMessage();	
  }

  public static Loggable illegalTimestampConvertLoggable(String arg0)  {
    Object[] args = { arg0 };
    Loggable l = new Loggable("2069106", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, JavaCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }


  /**
   * Invalid time/date format = "{0}".
   *
   * messageid:  2069107
   * severity:   error
   */
  public static String illegalTimeDateFormat(String arg0)  {
    Object [] args = { arg0 };
    Loggable l = (new Loggable("2069107", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, JavaCartridgeLogger.class.getClassLoader()));
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l.getMessage();	
  }

  public static Loggable illegalTimeDateFormatLoggable(String arg0)  {
    Object[] args = { arg0 };
    Loggable l = new Loggable("2069107", 8, args, LOCALIZER_CLASS, MessageLoggerInitializer.INSTANCE.messageLogger, JavaCartridgeLogger.class.getClassLoader());
    l.setStackTraceEnabled(true);
    l.setExcludePartition(false);
    return l;
  }



}
