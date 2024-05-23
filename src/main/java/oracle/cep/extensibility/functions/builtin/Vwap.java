/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/Vwap.java /main/2 2011/10/10 10:31:44 udeshmuk Exp $ */

/* Copyright (c) 2010, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    10/10/11 - XbranchMerge udeshmuk_bug-11933156_ps5 from
                           st_pcbpel_11.1.1.4.0
    sborah      08/25/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/Vwap.java /main/1 2010/09/13 02:01:07 sborah Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.extensibility.functions.builtin;

import oracle.cep.extensibility.functions.AggrDouble;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.UDAException;

public class Vwap extends AggrFunctionImpl implements IAggrFnFactory, Cloneable
{
  
  double  volume;
  double  price;
  double  sumVP;
  double  sumVol;
  
  public IAggrFunction newAggrFunctionHandler() throws UDAException {
    return new Vwap();
  }
  
  public void freeAggrFunctionHandler(IAggrFunction handler) 
  throws UDAException 
  {
  }
  
  public void initialize() throws UDAException 
  {
    sumVP  = 0d;
    sumVol = 0d;
  }
  
  public void handlePlus(AggrValue[] args, AggrValue result) 
  throws UDAException 
  {
    price  = 0d;
    volume = 0d;
    
    if(!args[0].isNull())
      price  = ((AggrDouble)args[0]).getValue();
    if(!args[1].isNull())
      volume = ((AggrDouble)args[1]).getValue();   
    
    sumVol = sumVol + volume;
    sumVP  = sumVP + (price * volume);
    
    ((AggrDouble)result).setValue(sumVP/sumVol);
  }
  
  public void handleMinus(AggrValue[] args, AggrValue result) 
  throws UDAException 
  {
    price  = 0d;
    volume = 0d;
    
    if(!args[0].isNull())
      price  = ((AggrDouble)args[0]).getValue();
    if(!args[1].isNull())
      volume = ((AggrDouble)args[1]).getValue();
        
    sumVol = sumVol - volume;
    sumVP  = sumVP - (price * volume);
    
    ((AggrDouble)result).setValue(sumVP/sumVol);
  }
  
  public Object clone()
  {
    Vwap myClone = null;
    try
    {
      myClone = (Vwap)super.clone();
    }
    catch(CloneNotSupportedException e)
    {
      return this;
    }
    if(myClone == null)
      return this;
    else
      return myClone;
  }
  
}


