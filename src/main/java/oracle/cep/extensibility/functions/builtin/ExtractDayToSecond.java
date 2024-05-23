/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/ExtractDayToSecond.java /main/2 2013/10/16 07:04:05 sbishnoi Exp $ */

/* Copyright (c) 2011, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    10/15/13 - bug 17077931
    sbishnoi    09/04/11 - Creation
 */
package oracle.cep.extensibility.functions.builtin;

import oracle.cep.common.IntervalConverter;
import oracle.cep.exceptions.UDFError;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.SoftUDFException;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/ExtractDayToSecond.java /main/2 2013/10/16 07:04:05 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class ExtractDayToSecond implements SingleElementFunction
{
  @Override
  public Object execute(Object[] args) throws UDFException
  {
    if(args[0] == null || args[1] == null)
      return null;
    assert args[1] instanceof java.lang.Long; 
    
    String timeUnitStr = args[0].toString();
    long numNanos      = (Long)args[1];
    long numSeconds    = numNanos / 1000000000l;
    long resultValue = 0l;   
    
    if(timeUnitStr.equalsIgnoreCase("day") || 
       timeUnitStr.equalsIgnoreCase("days"))
    {
      resultValue = numSeconds / IntervalConverter.DAYS;
    }
    else if(timeUnitStr.equalsIgnoreCase("hour") || 
        timeUnitStr.equalsIgnoreCase("hours"))
    {
      long numDays = numSeconds / IntervalConverter.DAYS;
      long value   = numSeconds - numDays * IntervalConverter.DAYS;
      resultValue  = value / IntervalConverter.HOUR;
    }
    else if(timeUnitStr.equalsIgnoreCase("minute") || 
        timeUnitStr.equalsIgnoreCase("minutes"))
    {
      long numDays  = numSeconds / IntervalConverter.DAYS;
      long value    = numSeconds - numDays * IntervalConverter.DAYS;
      long numHours = value / IntervalConverter.HOUR;
      value         = value - numHours * IntervalConverter.HOUR;
      resultValue   = value / IntervalConverter.MINUTE;
    }
    else if(timeUnitStr.equalsIgnoreCase("second") || 
        timeUnitStr.equalsIgnoreCase("seconds"))
    {
      long numDays      = numSeconds / IntervalConverter.DAYS;
      long value        = numSeconds - numDays * IntervalConverter.DAYS;
      long numHours     = value / IntervalConverter.HOUR;
      value             = value - numHours * IntervalConverter.HOUR;
      long numMinutes   = value / IntervalConverter.MINUTE;
      value             = value  - numMinutes * IntervalConverter.MINUTE;
      resultValue       = value;
    }
    else
    {
      // Note: If the TIME UNIT is not YEAR OR MONTH; 
      //       then Raise an exception
      LogUtil.fine(LoggerType.TRACE, "Invalid time unit " + timeUnitStr + 
        " specified for the function EXTRACT. Valid timeunits are " +
        "DAY, HOUR, MINUTE OR SECOND");
      throw new SoftUDFException(
        UDFError.ILLEGAL_ARGUMENT_FOR_FUNCTION_PARAMETRIZED, 
        timeUnitStr, 
        "Extract()");
    }    
    return resultValue;
  }
  
}