/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/SampleVariance.java /main/2 2013/09/27 08:50:42 pkali Exp $ */

/* Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       09/24/13 - handled null input scenarios
    pkali       08/15/13 - incremental sample variance function
    pkali       08/15/13 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/SampleVariance.java /main/2 2013/09/27 08:50:42 pkali Exp $
 *  @author  pkali   
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.extensibility.functions.builtin;

import java.io.Serializable;
import java.math.BigDecimal;

import oracle.cep.exceptions.UDAError;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.UDAException;
import oracle.cep.util.RunningVariance;

public class SampleVariance extends AggrFunctionImpl implements IAggrFnFactory ,
                                                    Cloneable, Serializable
{
  
  private static final long serialVersionUID = 5139278363720358953L;

  private RunningVariance rVariance;
  
  public SampleVariance()
  {
    rVariance = new RunningVariance(true); //sample variance
  }
  
  @Override
  public IAggrFunction newAggrFunctionHandler() throws UDAException
  {
    return new SampleVariance();
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
          result.setValue((Float)objVariance);
        else if(objVariance instanceof Double)
          result.setValue((Double)objVariance);
        else if(objVariance instanceof BigDecimal)
          result.setValue((BigDecimal)objVariance);
        return;
      }
      
      if(!inpVal.isNumeric())
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
            result.setValue(rVariance.add(value));
          else
            result.setValue(rVariance.remove(value));
        }
        else if(val instanceof Double)
        {
          double value = (Double)val;
          if(isPlus)
            result.setValue(rVariance.add(value));
          else
            result.setValue(rVariance.remove(value));
        }
        else if(val instanceof BigDecimal)
        {
          BigDecimal value = (BigDecimal)val;
          if(isPlus)
            result.setValue(rVariance.add(value));
          else
            result.setValue(rVariance.remove(value));
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
  
  public Object clone()
  {
    SampleVariance myClone = new SampleVariance();
    if(this.rVariance != null)
      myClone.rVariance = (RunningVariance)this.rVariance.clone();
    else
      myClone.rVariance = null;
    return myClone;
  }
}
