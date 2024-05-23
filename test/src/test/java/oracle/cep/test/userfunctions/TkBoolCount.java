/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkBoolCount.java /main/2 2011/10/12 07:03:25 udeshmuk Exp $ */

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
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/userfunctions/TkBoolCount.java /main/1 2009/02/25 14:23:51 hopark Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.AggrBoolean;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.UDAException;
import oracle.cep.extensibility.functions.AggrInteger;
import oracle.cep.extensibility.functions.AggrBigInt;
import oracle.cep.extensibility.functions.AggrFloat;


public class TkBoolCount extends AggrFunctionImpl implements IAggrFnFactory, Cloneable
{

  int count;

  public IAggrFunction newAggrFunctionHandler() throws UDAException 
  {
    return new TkBoolCount();
  }

  public void freeAggrFunctionHandler(IAggrFunction handler) throws UDAException 
  {
  }

  public void initialize() throws UDAException 
  {
    count = 0;
  }

  public void handlePlus(AggrBoolean value, AggrInteger result) throws UDAException 
  {
    if(!value.isNull())
    {
      if (value.getValue())
        count++;
    }
    if(count == 0)
      result.setNull(true);
    else
      result.setValue(count);
  }

  public void handleMinus(AggrBoolean value, AggrInteger result) throws UDAException 
  {
    if(!value.isNull())
    {
      if (value.getValue())
        count--;
    }
    if(count == 0)
      result.setNull(true);
    else
      result.setValue(count);
  }

  public void handlePlus(AggrValue[] args, AggrValue result) throws UDAException 
  {
    assert (args[0] instanceof AggrBoolean);
    assert (result instanceof AggrInteger);
    handlePlus(((AggrBoolean) args[0]), ((AggrInteger) result));
  }
  
  public void handleMinus(AggrValue[] args, AggrValue result) throws UDAException 
  {
    assert (args[0] instanceof AggrBoolean);
    assert (result instanceof AggrInteger);
    handleMinus(((AggrBoolean) args[0]), ((AggrInteger) result));
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
