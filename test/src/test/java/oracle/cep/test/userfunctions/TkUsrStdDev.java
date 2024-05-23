/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkUsrStdDev.java /main/2 2011/10/12 07:03:25 udeshmuk Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Incremental User Defined Aggregation - Standard Deviation

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
    rkomurav    01/05/07 - UDA null
    skmishra    12/09/06 - 
    anasrini    07/20/06 - Creation
 */

/**
 *  @version $Header: TkUsrStdDev.java 13-may-2008.09:02:36 hopark Exp $
 *  @author  anasrini
 *  @since   1.0
 */
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.AggrFloat;
import oracle.cep.extensibility.functions.AggrInteger;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.UDAException;

public class TkUsrStdDev extends AggrFunctionImpl implements IAggrFnFactory, Cloneable {

  private TkUsrVariance var;
  private AggrFloat varResult;

  public TkUsrStdDev() {
    var       = new TkUsrVariance();
    varResult = new AggrFloat();
  }

  public IAggrFunction newAggrFunctionHandler() throws UDAException {
    return new TkUsrStdDev();
  }

  public void freeAggrFunctionHandler(IAggrFunction handler) throws UDAException {
  }

  public void initialize() throws UDAException {
    var.initialize();
  }

  public void handlePlus(AggrInteger value, AggrFloat result) throws UDAException {
    varResult.setNull(false);
    var.handlePlus(value, varResult);
    if(varResult.isNull())
      result.setNull(true);
    else
      result.setValue((float)Math.sqrt(varResult.getValue()));
  }

  public void handleMinus(AggrInteger value, AggrFloat result) throws UDAException {
    varResult.setNull(false);
    var.handleMinus(value, varResult);
    if(varResult.isNull())
      result.setNull(true);
    else
      result.setValue((float)Math.sqrt(varResult.getValue()));
  }
  
  public void handlePlus(AggrValue[] args, AggrValue result) throws UDAException {
    
    if (args[0] instanceof AggrInteger)
      handlePlus(((AggrInteger) args[0]), ((AggrFloat) result));
    else 
      assert false;
    
  }
    
  public void handleMinus(AggrValue[] args, AggrValue result) throws UDAException {
    
    if (args[0] instanceof AggrInteger)
      handleMinus(((AggrInteger) args[0]), ((AggrFloat) result));
    else
      assert false;
    
  } 

  public Object clone()
  {
    TkUsrStdDev myClone = new TkUsrStdDev();
    if(this.var != null)
      myClone.var = (TkUsrVariance) this.var.clone();
    else
      myClone.var = null;
    if(this.varResult != null)
      myClone.varResult = (AggrFloat) this.varResult.clone();
    else
      myClone.varResult = null;
    return myClone;
  }
}
