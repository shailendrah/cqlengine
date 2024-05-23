/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkTrend.java /main/2 2011/10/12 07:03:25 udeshmuk Exp $ */

/* Copyright (c) 2008, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    10/12/11 - XbranchMerge udeshmuk_bug-13060688_ps5 from
                           st_pcbpel_11.1.1.4.0
    hopark      05/13/08 - 
    rkomurav    04/16/08 - Creation
 */

/**
 *  @version $Header: TkTrend.java 13-may-2008.09:01:43 hopark Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.AggrDouble;
import oracle.cep.extensibility.functions.AggrInteger;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.UDAException;

public class TkTrend extends AggrFunctionImpl implements IAggrFnFactory, Cloneable
{
  boolean    gotOne;
  AggrValue  prev;
  AggrValue  current;
  int        trend;
  
  public IAggrFunction newAggrFunctionHandler() throws UDAException
  {
    return new TkTrend();
  }
  
  public void freeAggrFunctionHandler(IAggrFunction handler)
  {
    prev    = null;
    current = null;
  }
  
  public void initialize()
  {
    trend   = 0;
    gotOne  = false;
  }
  
  // index ranges from 1 to n
  public void handlePlus(AggrValue[] args, AggrValue result)
  {
    AggrDouble input;
    double     p;
    double     c;

    c = 0;
    input  = (AggrDouble)args[0];
    
    if(gotOne)
    {
      current.copy(prev);
      input.copy(current);
      p = ((AggrDouble)prev).getValue();
      c = ((AggrDouble)current).getValue();
      if (p < c)
        trend++;
      else if (p > c)
        trend --;
    }
    else 
    {
      gotOne  = true;
      prev    = input.clone();
      current = input.clone();
    }
    ((AggrInteger)result).setValue(trend);
  }
  
  public void handleMinus(AggrValue[] args, AggrValue result)
  {
    //for pattern streams it should never be called
    assert false;
  }
  
  public Object clone()
  {
    TkTrend myClone = new TkTrend();
    if(this.current != null)
      myClone.current = this.current.clone();
    else
      myClone.current = this.current;
    myClone.gotOne = this.gotOne;
    if(this.prev != null)
      myClone.prev = this.prev.clone();
    else
      myClone.prev = null;
    myClone.trend = this.trend;
    return myClone;
  }
}
