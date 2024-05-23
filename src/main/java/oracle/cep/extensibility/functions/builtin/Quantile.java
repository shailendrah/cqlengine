
/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/Quantile.java /main/1 2013/09/16 01:55:02 pkali Exp $ */

/* Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       09/10/13 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/Quantile.java /main/1 2013/09/16 01:55:02 pkali Exp $
 *  @author  pkali   
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.extensibility.functions.builtin;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import oracle.cep.exceptions.UDAError;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.UDAException;

public class Quantile extends AggrFunctionImpl implements IAggrFnFactory ,
                                                    Cloneable, Serializable
{
  private static final long serialVersionUID = 3744685812140045551L;
  
  DoubleArrayList doubleSortedList;
  int size = 0;
  double  phi = 0d;

  @Override
  public void initialize() throws UDAException
  {
    doubleSortedList = new DoubleArrayList();  
  }
  
  @Override
  public IAggrFunction newAggrFunctionHandler() throws UDAException
  {
    return new Quantile();
  }

  @Override
  public void freeAggrFunctionHandler(IAggrFunction handler)
      throws UDAException
  {
      doubleSortedList = null;
  }

  public void handlePlus(AggrValue[] args, AggrValue result) 
  throws UDAException 
  {
    if (args != null && args.length > 1)
    {
      AggrValue  inpVal0  = args[0];
      AggrValue  inpVal1  = args[1];
      
      double inpDoubleVal = 0d;
      
      if(inpVal0.isNull())
        return;
      
      Object phiVal = inpVal1.getValue();
      if(phiVal instanceof Double)
        phi = (Double)phiVal;
      else
        throw new UDAException(UDAError.INVALID_DATA_TYPE, new Object[]{phiVal, "double"} );
      
      if(phi < 0d || phi > 1d)
        throw new UDAException(UDAError.INVALID_QUANTILE_RANGE, new Object[]{phiVal} );
      
      Object val = inpVal0.getValue();
      if(val != null)
      {
        if(val instanceof Integer ||
            val instanceof Long ||
            val instanceof Float)
          inpDoubleVal = toDouble(val);
        else if(val instanceof Double)
          inpDoubleVal = (Double)val;
        else if(val instanceof BigDecimal)
          inpDoubleVal = ((BigDecimal)val).doubleValue();
        else
        {
          throw new UDAException(UDAError.INVALID_DATA_TYPE, 
              new Object[]{phiVal, "int, long, float, double, big decimal"} );
        }
      }
      int searchPos = doubleSortedList.binarySearch(inpDoubleVal);
      int insertPos = 0;
      if(searchPos >= 0) {insertPos = searchPos;}
      else {insertPos = (searchPos + 1) * -1;}
      doubleSortedList.beforeInsert(insertPos, inpDoubleVal);
      size++ ;
  
      double resultVal = getquantile();
      if(val instanceof Integer ||
          val instanceof Long ||
          val instanceof Float ||
          val instanceof Double)
        result.setValue(resultVal);
      else if(val instanceof BigDecimal)
      {
        BigDecimal bd = (BigDecimal) val;
        //divide by 1 to set the scale value correctly
        BigDecimal r = new BigDecimal(resultVal, new MathContext(bd.precision()))
                                  .divide(new BigDecimal(1.0d), bd.scale(), RoundingMode.HALF_UP);
        result.setValue(r);
      }
      
    }
  }
  
  public void handleMinus(AggrValue[] args, AggrValue result) 
  throws UDAException 
  {
    if (args != null && args.length > 1)
    {
      AggrValue  inpVal0  = args[0];
      AggrValue  inpVal1  = args[1];
      
      double inpDoubleVal = 0d;
      
      if(inpVal0.isNull())
        return;
      
      Object phiVal = inpVal1.getValue();
      if(phiVal instanceof Double)
        phi = (Double)phiVal;
      else
        throw new UDAException(UDAError.INVALID_DATA_TYPE, new Object[]{phiVal, "double"} );
      
      if(phi < 0d || phi > 1d)
        throw new UDAException(UDAError.INVALID_QUANTILE_RANGE, new Object[]{phiVal} );
      
      Object val = inpVal0.getValue();
      if(val != null)
      {
        if(val instanceof Integer ||
            val instanceof Long ||
            val instanceof Float)
          inpDoubleVal = toDouble(val);
        else if(val instanceof Double)
          inpDoubleVal = (Double)val;
        else if(val instanceof BigDecimal)
          inpDoubleVal = ((BigDecimal)val).doubleValue();
        else
        {
          throw new UDAException(UDAError.INVALID_DATA_TYPE, 
              new Object[]{phiVal, "int, long, float, double, big decimal"} );
        }
      }
      
      int pos = doubleSortedList.indexOf(inpDoubleVal);
      doubleSortedList.remove(pos);
      size--;
  
      double resultVal = getquantile();
      if(val instanceof Integer ||
          val instanceof Long ||
          val instanceof Float ||
          val instanceof Double)
        result.setValue(resultVal);
      else if(val instanceof BigDecimal)
      {
        BigDecimal bd = (BigDecimal) val;
        //divide by 1 to set the scale value correctly
        BigDecimal r = new BigDecimal(resultVal, new MathContext(bd.precision()))
                                  .divide(new BigDecimal(1.0d), bd.scale(), RoundingMode.HALF_UP);
        result.setValue(r);
      }
    }
  }
  
  private double getquantile()
  {
    return Descriptive.quantile(doubleSortedList,phi);
  }
  
  private double toDouble(Object value)
  {
    if(value instanceof Number)
      return ((Number)value).doubleValue();
    else
      return 0d;
  }
  
  public Object clone()
  {
    Quantile myClone = new Quantile();
    if(this.doubleSortedList != null)
      myClone.doubleSortedList = this.doubleSortedList.copy();
    else
      myClone.doubleSortedList = null;
    myClone.phi = this.phi;
    myClone.size = this.size;
    return myClone;
  }
}