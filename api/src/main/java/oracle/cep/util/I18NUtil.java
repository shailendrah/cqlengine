/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/util/I18NUtil.java /main/3 2012/02/24 11:44:51 alealves Exp $ */

/* Copyright (c) 2009, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    I18NUtil provides utility functions for wlevs 18N Message Catalog.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      05/16/11 - add custom loggername
    hopark      03/25/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/util/I18NUtil.java /main/3 2012/02/24 11:44:51 alealves Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.util;

import java.lang.reflect.*;

import com.oracle.osa.exceptions.ErrorCode;
import com.oracle.osa.exceptions.ErrorDescription;
import oracle.cep.exceptions.ErrorHelper;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

import java.text.MessageFormat;
import java.util.Formatter;
import java.util.Locale;
import java.util.MissingResourceException;

import weblogic.i18n.Localizer;
import weblogic.i18ntools.L10nLookup;

public class I18NUtil
{
  public static boolean USE_FALLBACK_TO_DESC = true;
  
  private static final int WLS_BASEID = 2050000;

  private static final int MESSAGE = 0;
  private static final int CAUSE = 1;
  private static final int ACTION = 2;
  
  public static String getMessage(ErrorCode ec, Object[] args)  
  {
    return getLocalizedString(ec, MESSAGE, args);
  }

  public static String getCause(ErrorCode ec, Object[] args)
  {
    return getLocalizedString(ec, CAUSE, args);
  }

  public static String getAction(ErrorCode ec, Object[] args)
  {
    return getLocalizedString(ec, ACTION, args);
  }
  
  public static String getLocalizedString(ErrorCode ec, int which, Object[] args)  
  {
    Class<?> ecClass = ec.getClass();
    String name = ecClass.getSimpleName();
    //Assume all error code ends wirh Error. (e.g SemanticError)
    String loggerBaseName= null;
    
    Method getLoggerNameMethod = null;
    try
    {
      Class params[] = {};
      Object paramsObj[] = {};
      getLoggerNameMethod = ecClass.getDeclaredMethod("getLoggerName", params);
      loggerBaseName = (String) getLoggerNameMethod.invoke(null, paramsObj);
    } catch(Exception e) 
    {  
       //eatup..
    }
    if ( getLoggerNameMethod == null || loggerBaseName == null)
    { 
      String loggerName;
      if (name.equals("CustomerLogMsg"))
      {
        loggerName = "Customer";
      }
      else 
      {
        int pos = name.indexOf("Error");
        assert (pos > 0);
        loggerName = name.substring(0, pos );
      }
      //assumption
      //Loggers are named like "oracle.cep.exceptions.CustomerLogger"
      //Localizers are named like "oracle.cep.exceptions.CustomerLogLocalizer"
      loggerBaseName = "oracle.cep.exceptions." + loggerName; 
    }
    int errnum = ErrorHelper.getNum(ec);
    errnum += WLS_BASEID;
    Formatter formatter = new Formatter();
    Formatter r = formatter.format("%06d", errnum);
    String id = r.toString();
    String localizerClass = loggerBaseName + "LogLocalizer";
    if (which != MESSAGE)
    {
      localizerClass += "Detail";
    }
    if (localizerClass == null)
      return "Unable to access undefined message, id = " + id;
    
    Locale locale = Locale.getDefault();
    Localizer localizer = null;
    try
    {
        // Look first into own bundle. This should succeed as properties are placed in the
        //  CQL engine server JAR.
        localizer = L10nLookup.getLocalizer(locale, localizerClass, 
                I18NUtil.class.getClassLoader());
    }
    catch(MissingResourceException missingresourceexception)
    {
      try
      {
        localizer = L10nLookup.getLocalizer(locale, localizerClass, 
                Thread.currentThread().getContextClassLoader());
      }
      catch(MissingResourceException missingresourceexception2)
      {
        try
        {
          localizer = L10nLookup.getL10n().getLocalizer(id, locale);
        }
        catch(Exception e)
        {
          //Logs are more confusing with the following error message. Just comment out for now
          //Fall back to the Error Code Definition
          if (USE_FALLBACK_TO_DESC) {
	          ErrorDescription ed = ec.getErrorDescription();
	          String fmt = null;
	          switch(which)
	          {
	          case MESSAGE: fmt = ed.getText(); break;
	          case CAUSE: fmt = ed.getCause(); break;
	          case ACTION: fmt = ed.getAction(); break;
	          }
	          if (fmt == null)
	            return null;
	          if (args != null)
	            return MessageFormat.format(fmt, args);
	          return fmt;
          } else {
              LogUtil.severe(LoggerType.TRACE, "Failed to get localizer for "+localizerClass+" in " 
                      + locale.toString());
              LogUtil.fine(LoggerType.TRACE, "Failed to get localizer using class loaders '" 
                      + I18NUtil.class.getClassLoader().toString() + "' and '" 
                      + Thread.currentThread().getContextClassLoader().toString() + "'");
              return "Failed to get localizer for "+localizerClass+" in " 
                      + locale.toString() + " id:"+id;
          }
        }
      }
    }
    
    
    String fmt = null;
    switch(which)
    {
    case MESSAGE: fmt = localizer.get(id);	break;
    case CAUSE: fmt = localizer.getCause(id); break;
    case ACTION: fmt = localizer.getAction(id); break;
    }
    if (fmt == null)
      return null;
    if (args != null)
      return MessageFormat.format(fmt, args);
    return fmt;

  }
}
