/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/NumToDSInterval.java /main/1 2011/09/05 22:47:27 sbishnoi Exp $ */

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
 *  @version $Header: NumToDSInterval.java 04-sep-2011.05:33:49 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class NumToDSInterval implements SingleElementFunction
{
  private static HashMap<String, Long> unitMap;
  static
  {
    unitMap = new HashMap<String, Long>();
    unitMap.put("day", 
      IntervalConverter.getDToSMultiplicationFactor(TimeUnit.DAY));
    unitMap.put("days", 
      IntervalConverter.getDToSMultiplicationFactor(TimeUnit.DAY));
    unitMap.put("hour", 
      IntervalConverter.getDToSMultiplicationFactor(TimeUnit.HOUR));
    unitMap.put("hours", 
      IntervalConverter.getDToSMultiplicationFactor(TimeUnit.HOUR));
    unitMap.put("minute", 
      IntervalConverter.getDToSMultiplicationFactor(TimeUnit.MINUTE));
    unitMap.put("minutes", 
      IntervalConverter.getDToSMultiplicationFactor(TimeUnit.MINUTE));
    unitMap.put("second", 
      IntervalConverter.getDToSMultiplicationFactor(TimeUnit.SECOND));
    unitMap.put("seconds", 
      IntervalConverter.getDToSMultiplicationFactor(TimeUnit.SECOND));
  }

  @Override
  public Object execute(Object[] args) throws UDFException
  {    
    if(args[0] == null || args[1] == null)
      return null;
    
    String timeUnitStr = (String) args[1];
    Long lMultiplicationFactor      = unitMap.get(timeUnitStr);
    // Note: If the TIME UNIT is not DAY | MINUTE | SECOND; 
    //       then Raise an exception
    if(lMultiplicationFactor == null)
    {
      LogUtil.fine(LoggerType.TRACE, "Invalid time unit " + timeUnitStr + 
        " specified for the function NUMTODSINTERVAL. Valid timeunits are " +
        "DAY, HOUR, MINUTE OR SECOND");
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
