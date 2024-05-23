/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/Round.java /main/1 2013/03/04 00:54:59 sbishnoi Exp $ */

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

import java.math.BigDecimal;
import java.math.RoundingMode;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.exceptions.UDFError;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/Round.java /main/1 2013/03/04 00:54:59 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class Round implements SingleElementFunction 
{    
  /**
   * ROUND(n, integer) returns n rounded to integer places to the right of the 
   * decimal point. 
   * If you omit integer, then n is rounded to zero places. 
   * If integer is negative, then n is rounded off to the left of the decimal point.
   * 
   * n can be any numeric datatype.
   * If you omit integer, then the function returns the value ROUND(n, 0) 
   * in the same datatype as the numeric datatype of n.
   * 
   * If you include integer, then the function returns DOUBLE if numeric datatype
   * of n is either INTEGER, BIGINT, FLOAT OR DOUBLE.
   * If numeric datatype of n is BIGDECIMAL, It will return a BIGDECIMAL. 
   */
  public Object execute(Object[] args) throws UDFException 
  {
    /** input value whose round to be calculated */
    Object inpParam1 = null;
    
    /** number of rounding places (optional)*/
    Object inpParam2 = null;
    
    assert args.length > 0;
    
    /** Number of parameters */
    int    numParams = args.length;
    
    // Set first parameter
    inpParam1 = args[0];
    
    // Set second parameter if number of params are TWO
    if(numParams == 2)
      inpParam2 = args[1];
    
    int numRoundingPlaces = 0;
    
    // If input value is null, then return null
    if ((inpParam1 == null)) 
      return null;
    
    // If it is round(number,roundplace), then set numRoundingPlaces
    boolean isRoundingPlacesSpecified = numParams == 2 && 
                                        inpParam2 != null;
    
    // Only integer values are allowed for rounding places
    if(isRoundingPlacesSpecified)
    {
      assert inpParam2 instanceof Integer;
      numRoundingPlaces = ((Integer)inpParam2).intValue();
    }
    
    if(inpParam1 instanceof Integer)
    {
      if(isRoundingPlacesSpecified && numRoundingPlaces < 0)
      {
        BigDecimal paramVal = new BigDecimal((Integer)inpParam1);
        return round(paramVal, numRoundingPlaces).doubleValue();
      }
      else if(isRoundingPlacesSpecified)
      {
        return new Double((Integer)inpParam1);
      }
      else
        return inpParam1;
    }
    else if(inpParam1 instanceof Long)
    {
      if(isRoundingPlacesSpecified && numRoundingPlaces < 0)
      {
        BigDecimal paramVal = new BigDecimal((Long)inpParam1);
        return round(paramVal, numRoundingPlaces).doubleValue();
      }
      else if(isRoundingPlacesSpecified)
      {
        return new Double((Long)inpParam1);
      }
      else
        return inpParam1;
    }
    else if(inpParam1 instanceof Float)
    {
      if(isRoundingPlacesSpecified)
      {
        BigDecimal paramVal = new BigDecimal((Float)inpParam1);
        return round(paramVal, numRoundingPlaces).doubleValue();
      }
      else
      {
        return new Float(Math.round((Float)inpParam1));
      }
    }
    else if(inpParam1 instanceof Double)
    {
      if(isRoundingPlacesSpecified)
      {
        BigDecimal paramVal = new BigDecimal((Double)inpParam1);
        return round(paramVal, numRoundingPlaces).doubleValue();
      }
      else
      {
        return new Double(Math.round((Double)inpParam1));
      }
    }
    else if(inpParam1 instanceof BigDecimal)
    {
      BigDecimal paramVal = (BigDecimal)inpParam1;
      if(isRoundingPlacesSpecified)
      {
        return round(paramVal, numRoundingPlaces);        
      }
      else
      {
        return round(paramVal, 0);
      }
    }
    else
      throw new UDFException(UDFError.ILLEGAL_ARGUMENT_FOR_FUNCTION);
  }
  
  private BigDecimal round(BigDecimal paramVal, int numRoundingPlaces)
  {
    return paramVal.setScale(numRoundingPlaces, RoundingMode.HALF_UP);
  }
} 
