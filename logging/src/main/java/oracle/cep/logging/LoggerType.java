/* $Header: LoggerType.java 19-jun-2008.12:28:22 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      05/17/07 - Creation
 */

/**
 *  @version $Header: LoggerType.java 19-jun-2008.12:28:22 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logging;

public enum LoggerType
{
  TRACE,
  CUSTOMER,
  MAX;

  public static LoggerType fromString(String loggerType)
  {
    LoggerType[] vals = LoggerType.values();
    for (LoggerType k : vals) 
    {
      String kname = k.name();
      if (loggerType.equals(kname)) return k;
    } 
    return null;
  }
}

