/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkVWAP.java /main/2 2011/10/12 07:03:25 udeshmuk Exp $ */

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
    udeshmuk    10/18/07 - Change the signature of handlePlus and handleMinus
                           functions.
    mthatte     10/16/07 - 
    sbishnoi    06/19/07 - Creation
 */

/**
 *  @version $Header: TkVWAP.java 13-may-2008.09:02:43 hopark Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.AggrInteger;
import oracle.cep.extensibility.functions.AggrFloat;
import oracle.cep.extensibility.functions.AggrBigInt;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.UDAException;

public class TkVWAP extends AggrFunctionImpl implements IAggrFnFactory, Cloneable {

  long  volume;
  float price;
  float sumVP;
  long  sumVol;

  public IAggrFunction newAggrFunctionHandler() throws UDAException {
    return new TkVWAP();
  }

  public void freeAggrFunctionHandler(IAggrFunction handler) throws UDAException {
  }

  public void initialize() throws UDAException {
    sumVP  = 0;
    sumVol = 0;
  }

  public void handlePlus(AggrValue[] args, AggrValue result) throws UDAException {
    price  = ((AggrFloat)args[0]).getValue();
    volume = ((AggrBigInt)args[1]).getValue();
    sumVol = sumVol + volume;
    sumVP  = sumVP + (price * volume);
    ((AggrFloat)result).setValue(sumVP/sumVol);
  }

  public void handleMinus(AggrValue[] args, AggrValue result) throws UDAException {
    price  = ((AggrFloat)args[0]).getValue();
    volume = ((AggrBigInt)args[1]).getValue();
    sumVol = sumVol - volume;
    sumVP  = sumVP - (price * volume);
    ((AggrFloat)result).setValue(sumVP/sumVol);
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
