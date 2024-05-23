/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkUsrVariance.java /main/2 2011/10/12 07:03:25 udeshmuk Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Incremental User Defined Aggregation - TkUsrVariance

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    10/12/11 - XbranchMerge udeshmuk_bug-13060688_ps5 from
                           st_pcbpel_11.1.1.4.0
    hopark      05/13/08 - 
    udeshmuk    02/03/08 - support for double data type.
    udeshmuk    10/18/07 - Rewrite to make use of generic handlePlus and
                           handleMinus functions.
    mthatte     10/16/07 - 
    sbishnoi    02/16/07 - support for float/bigint input
    rkomurav    01/04/07 - null support UDA
    skmishra    12/09/06 - 
    anasrini    07/18/06 - Incremental UDA test 
    anasrini    07/18/06 - Creation
 */

/**
 *  @version $Header: TkUsrVariance.java 13-may-2008.09:02:41 hopark Exp $
 *  @author  anasrini
 *  @since   1.0
 */
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.AggrDouble;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.UDAException;
import oracle.cep.extensibility.functions.AggrInteger;
import oracle.cep.extensibility.functions.AggrFloat;
import oracle.cep.extensibility.functions.AggrBigInt;

public class TkUsrVariance extends AggrFunctionImpl implements IAggrFnFactory, Cloneable {

  int    count;
  float  sum;
  float  sumsqr;  
  
  int    dcount;
  double dsum;
  double dsumsqr;

  public IAggrFunction newAggrFunctionHandler() throws UDAException {
    return new TkUsrVariance();
  }

  public void freeAggrFunctionHandler(IAggrFunction handler) throws UDAException {
  }

  public void initialize() throws UDAException {
    count   = 0;
    sum     = 0.0f;
    sumsqr  = 0.0f;
    dcount  = 0;
    dsum    = 0.0d;
    dsumsqr = 0.0d;
  }

  public void handlePlus(AggrInteger value, AggrFloat result) throws UDAException {
    if(!value.isNull())
    {
      float v = (float)value.getValue();
      count++;
      sum    += v;
      sumsqr += v * v;
    }

    if(count == 0)
      result.setNull(true); // the way u may want to set the output of UDA to be null?
    else
      result.setValue(getVar());
  }

  public void handleMinus(AggrInteger value, AggrFloat result) throws UDAException {
    if(!value.isNull())
    {
      float v = (float)value.getValue();
      count--;
      sum    -= v;
      sumsqr -= v * v;
    }

    if(count == 0)
      result.setNull(true);
    else
      result.setValue(getVar());
  }

  public void handlePlus(AggrFloat value, AggrFloat result) throws UDAException {
    if(!value.isNull())
    {
      float v = value.getValue();
      count++;
      sum    += v;
      sumsqr += v * v;
    }

    if(count == 0)
      result.setNull(true);
    else
      result.setValue(getVar());
  }

  public void handleMinus(AggrFloat value, AggrFloat result) throws UDAException {
    if(!value.isNull())
    {
      float v = value.getValue();
      count--;
      sum    -= v;
      sumsqr -= v * v;
    }

    if(count == 0)
      result.setNull(true);
    else
      result.setValue(getVar());
  }

  public void handlePlus(AggrDouble value, AggrDouble result) throws UDAException {
    if(!value.isNull())
    {
      double v = value.getValue();
      dcount++;
      dsum    += v;
      dsumsqr += v * v;
    }

    if(dcount == 0)
      result.setNull(true);
    else
      result.setValue(getDVar());
  }
  
  public void handleMinus(AggrDouble value, AggrDouble result) throws UDAException {
    if(!value.isNull())
    {
      double v = value.getValue();
      dcount--;
      dsum    -= v;
      dsumsqr -= v * v;
    }

    if(dcount == 0)
      result.setNull(true);
    else
      result.setValue(getDVar());
  }
  
  public void handlePlus(AggrBigInt value, AggrFloat result) throws UDAException {
    if(!value.isNull())
    {
      float v = (float)value.getValue();
      count++;
      sum    += v;
      sumsqr += v * v;
    }
    if(count == 0)
      result.setNull(true);
    else
      result.setValue(getVar());
  }

  public void handleMinus(AggrBigInt value, AggrFloat result) throws UDAException {
    if(!value.isNull())
    {
      float v = (float)value.getValue();
      count--;
      sum    -= v;
      sumsqr -= v * v;
    }
    if(count == 0)
      result.setNull(true);
    else
      result.setValue(getVar());
  }

  public void handlePlus(AggrValue[] args, AggrValue result) throws UDAException{
    
    if(args[0] instanceof AggrInteger)
      handlePlus(((AggrInteger) args[0]), ((AggrFloat) result));
    else if(args[0] instanceof AggrFloat)
      handlePlus(((AggrFloat) args[0]), ((AggrFloat) result));
    else if(args[0] instanceof AggrBigInt)
      handlePlus(((AggrBigInt) args[0]), ((AggrFloat) result));
    else if(args[0] instanceof AggrDouble)
      handlePlus(((AggrDouble) args[0]), ((AggrDouble) result));
    else 
      assert false;
    
  }

public void handleMinus(AggrValue[] args, AggrValue result) throws UDAException{
    
    if(args[0] instanceof AggrInteger)
      handleMinus(((AggrInteger) args[0]), ((AggrFloat) result));
    else if(args[0] instanceof AggrFloat)
      handleMinus(((AggrFloat) args[0]), ((AggrFloat) result));
    else if(args[0] instanceof AggrBigInt)
      handleMinus(((AggrBigInt) args[0]), ((AggrFloat) result));
    else if(args[0] instanceof AggrDouble)
      handleMinus(((AggrDouble) args[0]), ((AggrDouble) result));
    else 
      assert false;
    
  }

  private float getVar() {
    float avg;
    float avgsqr;
    float var;

    avg    = sum/((float)count);
    avgsqr = avg*avg;
    var    = sumsqr/((float)count) - avgsqr;
    return var;
  }

  private double getDVar() {
    double avg;
    double avgsqr;
    double var;
    
    avg    = dsum/((double)dcount);
    avgsqr = avg*avg;
    var    = dsumsqr/((double)dcount) - avgsqr;
    return var;
  }
  
  public Object clone()
  {
    try
    {
      return super.clone();
    }
    catch(CloneNotSupportedException e)
    {
      return this;
    }
  }
}
