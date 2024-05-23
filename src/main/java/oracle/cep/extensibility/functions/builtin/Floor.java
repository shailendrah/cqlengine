/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/Floor.java /main/1 2013/03/04 00:54:59 sbishnoi Exp $ */

/* Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    02/25/13 - Creation
 */

package oracle.cep.extensibility.functions.builtin;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.exceptions.UDFError;

import java.math.BigDecimal;
import java.math.RoundingMode;
/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/Floor.java /main/1 2013/03/04 00:54:59 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class Floor implements SingleElementFunction 
{
  /**
   * FLOOR returns largest integer equal to or less than n.
   * This function takes as an argument any numeric datatype.
   * The function returns the same datatype as the numeric datatype of the argument.
   */
  public Object execute(Object[] args) throws UDFException 
  {
    Object paramVal = args[0];
    
    if (paramVal == null) 
      return null;

    // At this moment, Argument can only be of numeric types
    if(!(paramVal instanceof Number))
    {
      throw new UDFException(UDFError.ILLEGAL_ARGUMENT_FOR_FUNCTION);
    }
    
    Number param1 = (Number)paramVal;
    
    try 
    {
      if(param1 instanceof BigDecimal)
      {
        BigDecimal bdParam = (BigDecimal)param1;
        return bdParam.setScale(0, RoundingMode.FLOOR);
      }
      else
      {
        double dParam = param1.doubleValue();
        Double resultVal = java.lang.Math.floor(dParam);
       
        // Return type will be same as input type
        if(paramVal instanceof Integer)
        {
          return resultVal.intValue();
        }
        else if(paramVal instanceof Float)
        {
          return resultVal.floatValue();
        }
        else if(paramVal instanceof Long)
        {
          return resultVal.longValue();
        }
        else if(paramVal instanceof Double)
        {
          return resultVal;
        }
        else
          throw new UDFException(UDFError.ILLEGAL_ARGUMENT_FOR_FUNCTION, 
            "floor");
      }
      
    }
    catch(Exception e) 
    {
      e.printStackTrace();
      throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, 
         e, "floor");
    }
  }
}
  
