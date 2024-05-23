/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkUsrDoubleCount.java /main/2 2011/10/12 07:03:25 udeshmuk Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
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
    udeshmuk    10/18/07 - Rewrite to make use of generic handlePlus and
                           handleMinus functions.
    mthatte     10/16/07 - 
    sbishnoi    02/16/07 - Creation
 */

/**
 *  @version $Header: TkUsrDoubleCount.java 13-may-2008.09:02:05 hopark Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.UDAException;
import oracle.cep.extensibility.functions.AggrInteger;
import oracle.cep.extensibility.functions.AggrBigInt;
import oracle.cep.extensibility.functions.AggrFloat;


public class TkUsrDoubleCount extends AggrFunctionImpl implements IAggrFnFactory, Cloneable {

  int count;

  public IAggrFunction newAggrFunctionHandler() throws UDAException {
    return new TkUsrDoubleCount();
  }

  public void freeAggrFunctionHandler(IAggrFunction handler) throws UDAException {
  }

  public void initialize() throws UDAException {
    count = 0;
  }

  public void handlePlus(AggrInteger value, AggrInteger result) throws UDAException {
    if(!value.isNull())
    {
      count++;
    }
    if(count == 0)
      result.setNull(true);
    else
      result.setValue(2 * count);
  }

  public void handleMinus(AggrInteger value, AggrInteger result) throws UDAException {
    if(!value.isNull())
    {
      count--;
    }
    if(count == 0)
      result.setNull(true);
    else
      result.setValue(2 * count);
  }

  public void handlePlus(AggrInteger value, AggrBigInt result) throws UDAException {
    if(!value.isNull())
    {
      count++;
    }
    if(count == 0)
      result.setNull(true);
    else
      result.setValue((long)(2 * count));
  }

  public void handleMinus(AggrInteger value, AggrBigInt result) throws UDAException {
    if(!value.isNull())
    {
      count--;
    }
    if(count == 0)
      result.setNull(true);
    else
      result.setValue((long)(2 * count));
  }

  public void handlePlus(AggrBigInt value, AggrBigInt result) throws UDAException {
    if(!value.isNull())
    {
      count++;
    }
    if(count == 0)
      result.setNull(true);
    else
      result.setValue((long)(2 * count));
  }

  public void handleMinus(AggrBigInt value, AggrBigInt result) throws UDAException {
    if(!value.isNull())
    {
      count--;
    }
    if(count == 0)
      result.setNull(true);
    else
      result.setValue((long)(2 * count));
  }

  public void handlePlus(AggrBigInt value, AggrInteger result) throws UDAException {
    if(!value.isNull())
    {
      count++;
    }
    if(count == 0)
      result.setNull(true);
    else
      result.setValue(2 * count);
  }

  public void handleMinus(AggrBigInt value, AggrInteger result) throws UDAException {
    if(!value.isNull())
    {
      count--;
    }
    if(count == 0)
      result.setNull(true);
    else
      result.setValue(2 * count);
  }

  public void handlePlus(AggrFloat value, AggrInteger result) throws UDAException {
    if(!value.isNull())
    {
      count++;
    }
    if(count == 0)
      result.setNull(true);
    else
      result.setValue(2 * count);
  }

  public void handleMinus(AggrFloat value, AggrInteger result) throws UDAException {
    if(!value.isNull())
    {
      count--;
    }
    if(count == 0)
      result.setNull(true);
    else
      result.setValue(2 * count);
  }

  public void handlePlus(AggrFloat value, AggrBigInt result) throws UDAException {
    if(!value.isNull())
    {
      count++;
    }
    if(count == 0)
      result.setNull(true);
    else
      result.setValue((long)(2 * count));
  }

  public void handleMinus(AggrFloat value, AggrBigInt result) throws UDAException {
    if(!value.isNull())
    {
      count--;
    }
    if(count == 0)
      result.setNull(true);
    else
      result.setValue((long)(2 * count));
  }

  public void handlePlus(AggrValue[] args, AggrValue result) throws UDAException {
    
    if (args[0] instanceof AggrInteger)
    {
      if (result instanceof AggrInteger)
        handlePlus(((AggrInteger) args[0]), ((AggrInteger) result));
      else if (result instanceof AggrBigInt)
        handlePlus(((AggrInteger) args[0]), ((AggrBigInt) result));
      else 
        assert false;
    }
    else if (args[0] instanceof AggrBigInt)
    {
      if (result instanceof AggrBigInt)
        handlePlus(((AggrBigInt) args[0]), ((AggrBigInt) result));
      else if (result instanceof AggrInteger)
        handlePlus(((AggrBigInt) args[0]), ((AggrInteger) result));
      else 
        assert false;
    }
    else if (args[0] instanceof AggrFloat)
    {
      if (result instanceof AggrInteger)
        handlePlus(((AggrFloat) args[0]), ((AggrInteger) result));
      else if (result instanceof AggrBigInt)
        handlePlus(((AggrFloat) args[0]), ((AggrBigInt) result));
      else 
        assert false;
    }
  }
  
  public void handleMinus(AggrValue[] args, AggrValue result) throws UDAException {
    
    if (args[0] instanceof AggrInteger)
    {
      if (result instanceof AggrInteger)
        handleMinus(((AggrInteger) args[0]), ((AggrInteger) result));
      else if (result instanceof AggrBigInt)
        handleMinus(((AggrInteger) args[0]), ((AggrBigInt) result));
      else 
        assert false;
    }
    else if (args[0] instanceof AggrBigInt)
    {
      if (result instanceof AggrBigInt)
        handleMinus(((AggrBigInt) args[0]), ((AggrBigInt) result));
      else if (result instanceof AggrInteger)
        handleMinus(((AggrBigInt) args[0]), ((AggrInteger) result));
      else
        assert false;
    }
    else if (args[0] instanceof AggrFloat)
    {
      if (result instanceof AggrInteger)
        handleMinus(((AggrFloat) args[0]), ((AggrInteger) result));
      else if (result instanceof AggrBigInt)
        handleMinus(((AggrFloat) args[0]), ((AggrBigInt) result));
      else 
        assert false;
    }
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
