/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkUsrHarmonicMean.java /main/2 2011/10/12 07:03:25 udeshmuk Exp $ */

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
    anasrini    06/19/07 - 
    sbishnoi    06/14/07 - Creation
 */

/**
 *  @version $Header: TkUsrHarmonicMean.java 13-may-2008.09:02:25 hopark Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.userfunctions;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

import oracle.cep.extensibility.functions.AggrFloat;
import oracle.cep.extensibility.functions.AggrInteger;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.UDAException;

public class TkUsrHarmonicMean extends AggrFunctionImpl 
  implements IAggrFnFactory, Cloneable {
  
  int             count;
  DoubleArrayList dal;

  public IAggrFunction newAggrFunctionHandler() throws UDAException {
    return new TkUsrHarmonicMean();
  }

  public void freeAggrFunctionHandler(IAggrFunction handler) 
    throws UDAException {
  }

  public void initialize() throws UDAException {
    count = 0;
    dal   = new DoubleArrayList();
  }
    
  public void handlePlus(AggrInteger value, AggrFloat result) 
    throws UDAException {

    if(!value.isNull()) {
      double v = (double)value.getValue();
      count++;
      dal.add(v);
    }

    if(count == 0) 
      result.setNull(true);
    else
      result.setValue(getHM());
  }
    
  public void handlePlus(AggrFloat value, AggrFloat result) 
    throws UDAException {

    if(!value.isNull()) {
      double v = (double)value.getValue();
      count++;
      dal.add(v);
    }

    if(count == 0) 
      result.setNull(true);
    else
      result.setValue(getHM());
  }
    
  public void handleMinus(AggrInteger value, AggrFloat result) 
    throws UDAException {

    if(!value.isNull()) {
      double v = (double)value.getValue();
      count--;
      dal.delete(v);
    }

    if(count == 0) 
      result.setNull(true);
    else
      result.setValue(getHM());
  }
    
  public void handleMinus(AggrFloat value, AggrFloat result) 
    throws UDAException {

    if(!value.isNull()) {
      double v = (double)value.getValue();
      count--;
      dal.delete(v);
    }

    if(count == 0) 
      result.setNull(true);
    else
      result.setValue(getHM());
  }
  
  public void handlePlus(AggrValue[] args, AggrValue result) throws UDAException {
    
    if (args[0] instanceof AggrInteger)
      handlePlus(((AggrInteger) args[0]), ((AggrFloat) result));
    else if (args[0] instanceof AggrFloat)
      handlePlus(((AggrFloat) args[0]), ((AggrFloat) result));
    else 
      assert false;
    
  }
    
  public void handleMinus(AggrValue[] args, AggrValue result) throws UDAException {
    
    if (args[0] instanceof AggrInteger)
      handleMinus(((AggrInteger) args[0]), ((AggrFloat) result));
    else if (args[0] instanceof AggrFloat)
      handleMinus(((AggrFloat) args[0]), ((AggrFloat) result));
    else 
      assert false;
    
  } 
  /*
   * Function getHM() returns the harmonic mean
   */
    
  public float getHM() {
    int    size = dal.size();
    double soi  = Descriptive.sumOfInversions(dal, 0, size-1);
    
    return (float)(Descriptive.harmonicMean(size, soi));
  }
  
  public Object clone()
  {
    TkUsrGeometricMean myClone = new TkUsrGeometricMean();
    myClone.count = this.count;
    if(this.dal != null)
      myClone.dal = this.dal.copy();
    else
      myClone.dal = null;
    return myClone;
  }
}
