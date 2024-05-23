/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkUsrBigint_flt_bigint.java /main/3 2011/10/12 07:03:25 udeshmuk Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    User Defined Aggregation for Bigint datatype testing

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
    parujain    04/16/07 - throw UDFException
    rkomurav    01/05/07 - null uDA
    hopark      11/27/06 - Creation
 */

/**
 *  @version $Header: TkUsrBigint_flt_bigint.java 13-may-2008.09:01:54 hopark Exp $
 *  @author  anasrini
 *  @since   1.0
 */
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.AggrBigInt;
import oracle.cep.extensibility.functions.AggrFloat;
import oracle.cep.extensibility.functions.AggrInteger;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDAException;
import oracle.cep.extensibility.functions.UDFException;

public class TkUsrBigint_flt_bigint extends AggrFunctionImpl implements IAggrFnFactory, Cloneable
{
  long max;
  int count;
  
  public IAggrFunction newAggrFunctionHandler() throws UDAException {
    return new TkUsrBigint_flt_bigint();
  }

  public void freeAggrFunctionHandler(IAggrFunction handler) throws UDAException {
  }

  public void initialize() throws UDAException {
    max   = 0;
    count = 0;
  }

  public void handlePlus(AggrFloat value, AggrBigInt result) throws UDAException 
  {
    if(!value.isNull())
    {
      count ++;
      if (value.getValue() > max) {
         max = ((Float)(value.getValue())).longValue();
      }
    }
    if(count == 0)
      result.setNull(true);
    else
      result.setValue(max);
  }
  
  public void handlePlus(AggrValue[] args, AggrValue result) throws UDAException
  {
    if (args[0] instanceof AggrFloat)
      handlePlus(((AggrFloat) args[0]), ((AggrBigInt) result));
    else
      assert false;
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
  
