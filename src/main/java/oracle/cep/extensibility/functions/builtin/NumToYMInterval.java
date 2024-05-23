/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/NumToYMInterval.java /main/1 2011/09/05 22:47:27 sbishnoi Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    09/04/11 - Creation
 */

package oracle.cep.extensibility.functions.builtin;

import java.math.BigDecimal;
import java.util.HashMap;

import oracle.cep.common.IntervalConverter;
import oracle.cep.common.TimeUnit;
import oracle.cep.exceptions.UDFError;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

/**
 *  @version $Header: NumToYMInterval.java 04-sep-2011.12:42:19 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class NumToYMInterval implements SingleElementFunction
{
  private static HashMap<String, Integer> unitMap;
  static
  {
    unitMap = new HashMap<String, Integer>();
    unitMap.put("year", 
        IntervalConverter.getYToMMultiplicationFactor(TimeUnit.YEAR));
    unitMap.put("years", 
        IntervalConverter.getYToMMultiplicationFactor(TimeUnit.YEAR));
    unitMap.put("YEAR", 
        IntervalConverter.getYToMMultiplicationFactor(TimeUnit.YEAR));
    unitMap.put("YEARS", 
        IntervalConverter.getYToMMultiplicationFactor(TimeUnit.YEAR));
    unitMap.put("month", 
        IntervalConverter.getYToMMultiplicationFactor(TimeUnit.MONTH));
    unitMap.put("months", 
        IntervalConverter.getYToMMultiplicationFactor(TimeUnit.MONTH));
    unitMap.put("MONTH", 
        IntervalConverter.getYToMMultiplicationFactor(TimeUnit.MONTH));
    unitMap.put("MONTHS", 
        IntervalConverter.getYToMMultiplicationFactor(TimeUnit.MONTH));
  }
  
  @Override
  public Object execute(Object[] args) throws UDFException
  {
    if(args[0] == null || args[1] == null)
      return null;
    
    String timeUnitStr = (String) args[1];
    Integer lMultiplicationFactor      = unitMap.get(timeUnitStr);
    
    // Note: If the TIME UNIT is not YEAR | MONTH; 
    //       then Raise an exception
    if(lMultiplicationFactor == null)
    {
      LogUtil.fine(LoggerType.TRACE, "Invalid time unit " + timeUnitStr + 
        " specified for the function NUMTOYMINTERVAL. Valid timeunits are " +
        "YEAR OR MONTH");
      throw new UDFException(UDFError.ILLEGAL_ARGUMENT_FOR_FUNCTION);
    }
    
    try
    {
      BigDecimal multiplicand         = new BigDecimal(args[0].toString());       
      BigDecimal multiplicationFactor = new BigDecimal(lMultiplicationFactor);
      BigDecimal intervalValue        = multiplicationFactor.multiply(multiplicand); 

      return intervalValue.longValue();
    }
    catch(NumberFormatException e)
    {
      LogUtil.fine(LoggerType.TRACE, "Invalid number as parameter. Use only numeric parameters");
      throw new UDFException(UDFError.ILLEGAL_ARGUMENT_FOR_FUNCTION);
    }
  }
  
}
