/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkSumRiskSummary.java /main/1 2012/09/12 06:41:22 sbishnoi Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    09/02/12 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkSumRiskSummary.java /main/1 2012/09/12 06:41:22 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.AggrObj;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.UDAException;

public class TkSumRiskSummary extends AggrFunctionImpl 
                              implements IAggrFnFactory, Cloneable 
{
  private TkRiskSummary total;

  public IAggrFunction newAggrFunctionHandler() throws UDAException 
  {
    TkSumRiskSummary newFnInstance = new TkSumRiskSummary();
    newFnInstance.total = new TkRiskSummary("0");
    return newFnInstance;
   }

  public void freeAggrFunctionHandler(IAggrFunction handler) 
    throws UDAException 
  {
  }

  public void initialize() throws UDAException 
  {
  }

  public void handlePlus(AggrValue[] value, AggrValue result) 
    throws UDAException 
  {
    AggrObj paramObj = (AggrObj)value[0];
    TkRiskSummary param = (TkRiskSummary) paramObj.getValue();

    total.add(param);

    if(total == null)
      result.setNull(true); 
    else
      result.setValue(total); 
  }

  public void handleMinus(AggrValue[] value, AggrValue result) 
    throws UDAException 
  {
    // This is implementation of a non-incremental aggregate function;
    // Hence handleMinus will not be called.
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
