/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/SampleStandardDeviation.java /main/2 2013/09/27 08:50:42 pkali Exp $ */

/* Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       09/24/13 - handles null input scenarios
    pkali       08/19/13 - incremental sample standard deviation function
    pkali       08/19/13 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/SampleStandardDeviation.java /main/2 2013/09/27 08:50:42 pkali Exp $
 *  @author  pkali   
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.extensibility.functions.builtin;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import oracle.cep.exceptions.UDAError;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.UDAException;
import oracle.cep.util.RunningVariance;

public class SampleStandardDeviation extends AggrFunctionImpl implements IAggrFnFactory ,
                                                    Cloneable, Serializable
{
  private static final long serialVersionUID = 5839278363720358911L;
  
  private RunningVariance rVariance;
  
  public SampleStandardDeviation()
  {
    rVariance = new RunningVariance(true); //sample variance
  }
  
  @Override
  public IAggrFunction newAggrFunctionHandler() throws UDAException
  {
    return new SampleStandardDeviation();
  }

  @Override
  public void freeAggrFunctionHandler(IAggrFunction handler)
      throws UDAException
  {
    rVariance = null;    
  }

  @Override
  public void initialize() throws UDAException
  {
    rVariance = new RunningVariance(true);
  }
  
  public void handlePlus(AggrValue[] args, AggrValue result) 
  throws UDAException 
  {
    try
    {
      processInput(args, result, true);
    }
    catch(UDAException e) {throw e;}    
  }
  
  public void handleMinus(AggrValue[] args, AggrValue result) 
  throws UDAException 
  {
    try
    {
      processInput(args, result, false);
    }
    catch(UDAException e) {throw e;}    
  }
  
  public void processInput(AggrValue[] args, AggrValue result, boolean isPlus) 
    throws UDAException
  {
    if (args != null && args.length > 0)
    {
      AggrValue  inpVal  = args[0];
      
      if(inpVal.isNull() && rVariance.getCount() == 0)
        result.setNull(true);
      
      if(inpVal.isNull())
      {
        Object objVariance = rVariance.getVariance();
        if(objVariance instanceof Float)
          result.setValue((float)StrictMath.sqrt((Float)objVariance));
        else if(objVariance instanceof Double)
          result.setValue(StrictMath.sqrt((Double)objVariance));
        else if(objVariance instanceof BigDecimal)
          result.setValue(getSqrt((BigDecimal)objVariance));
        return;
      }
      
      if(!inpVal.isNumeric() && !inpVal.isConvertibleToNumeric())
        throw new UDAException(UDAError.INVALID_NUMERIC_VALUE, new Object[]{inpVal.getValue()} );
        
      Object val = inpVal.getValue();
      if(val != null)
      {
        if(val instanceof Integer ||
            val instanceof Long ||
            val instanceof Float)
        {
          float value = toFloat(val);
          if(isPlus)
            result.setValue((float)StrictMath.sqrt(rVariance.add(value)));
          else
            result.setValue((float)StrictMath.sqrt(rVariance.remove(value)));
        }
        else if(val instanceof Double)
        {
          double value = (Double)val;
          if(isPlus)
            result.setValue(StrictMath.sqrt(rVariance.add(value)));
          else
            result.setValue(StrictMath.sqrt(rVariance.remove(value)));
        }
        else if(val instanceof BigDecimal)
        {
          BigDecimal value = (BigDecimal)val;
          if(isPlus)
            result.setValue(getSqrt(rVariance.add(value)));
          else
            result.setValue(getSqrt(rVariance.remove(value)));
        }
        else
        {
          throw new UDAException(UDAError.INVALID_NUMERIC_VALUE, new Object[]{val} );
        }
      }
    } 
  }

  private float toFloat(Object value)
  {
    if(value instanceof Number)
      return ((Number)value).floatValue();
    else
      return 0f;
  }
  
  private BigDecimal getSqrt(BigDecimal n)
  {
    double sqrt = StrictMath.sqrt(n.doubleValue());
    //divide by 1 to set the scale value correctly
    BigDecimal bgSqrt = new BigDecimal(sqrt, new MathContext(n.precision()))
                              .divide(new BigDecimal(1.0d), n.scale(), RoundingMode.HALF_UP);
    return bgSqrt;
  }
  
  public Object clone()
  {
    SampleStandardDeviation myClone = new SampleStandardDeviation();
    if(this.rVariance != null)
      myClone.rVariance = (RunningVariance)this.rVariance.clone();
    else
      myClone.rVariance = null;
    return myClone;
  }
}
