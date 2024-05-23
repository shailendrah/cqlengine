/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/NumToYMInterval.java /main/1 2017/11/13 22:47:27 sbishnoi Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    11/13/17 - Creation
 */

package oracle.cep.extensibility.functions.builtin;

import java.util.HashMap;

import oracle.cep.common.IntervalConverter;
import oracle.cep.common.TimeUnit;
import oracle.cep.exceptions.UDFError;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

/**
 *  @version $Header: YMIntervalToNum.java 13-nov-2017.12:42:19 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class YMIntervalToNum implements SingleElementFunction
{
  private static HashMap<String, Integer> unitMap;
  static
  {
    unitMap = new HashMap<String, Integer>();
    unitMap.put("year", 
        IntervalConverter.getYToMMultiplicationFactor(TimeUnit.YEAR));
    unitMap.put("years", 
        IntervalConverter.getYToMMultiplicationFactor(TimeUnit.YEAR));
    unitMap.put("month", 
        IntervalConverter.getYToMMultiplicationFactor(TimeUnit.MONTH));
    unitMap.put("months", 
        IntervalConverter.getYToMMultiplicationFactor(TimeUnit.MONTH));
  }
  
  @Override
  public Object execute(Object[] args) throws UDFException
  {
    if(args[0] == null || args[1] == null)
      return null;
    
    String timeUnitStr = (String) args[1];
    Integer divisor      = unitMap.get(timeUnitStr.toLowerCase());
    
    // Note: If the TIME UNIT is not YEAR | MONTH; 
    //       then Raise an exception
    if(divisor == null)
    {
      LogUtil.severe(LoggerType.TRACE, "Invalid time unit " + timeUnitStr + 
        " specified for the function YMINTERVALTONUM. Valid timeunits are " +
        "YEAR, YEARS, MONTH OR MONTHS");
      throw new UDFException(UDFError.ILLEGAL_ARGUMENT_FOR_FUNCTION);
    }
    
    try
    {
      assert args[0] instanceof Long;
      double out = ((Long)args[0]).doubleValue()/divisor;
      return out;
    }
    catch(NumberFormatException e)
    {
      LogUtil.severe(LoggerType.TRACE, "Invalid number as parameter. Use only numeric parameters");
      throw new UDFException(UDFError.ILLEGAL_ARGUMENT_FOR_FUNCTION);
    }
  }
  
}
