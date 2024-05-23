/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkUsrSecondMax.java /main/2 2011/10/12 07:03:25 udeshmuk Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Non Incremental User Defined Aggregation - Second Max

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    10/12/11 - XbranchMerge udeshmuk_bug-13060688_ps5 from
                           st_pcbpel_11.1.1.4.0
    hopark      05/13/08 - 
    udeshmuk    10/18/07 - support for byte, char, timestamp and interval.
    udeshmuk    10/17/07 - Rewrite to make use of generic handlePlus and
                           handleMinus functions.
    mthatte     10/16/07 - 
    sbishnoi    02/15/07 - implement handlePlus for Float,Float
    rkomurav    01/05/07 - null UDA
    skmishra    12/09/06 - 
    anasrini    07/17/06 - User Defined Aggregations Test 
    anasrini    07/17/06 - Creation
 */

/**
 *  @version $Header: TkUsrSecondMax.java 13-may-2008.09:02:33 hopark Exp $
 *  @author  anasrini
 *  @since   1.0
 */
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.AggrByte;
import oracle.cep.extensibility.functions.AggrChar;
import oracle.cep.extensibility.functions.AggrInteger;
import oracle.cep.extensibility.functions.AggrFloat;
import oracle.cep.extensibility.functions.AggrInterval;
import oracle.cep.extensibility.functions.AggrTimestamp;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.UDAException;

public class TkUsrSecondMax extends AggrFunctionImpl implements IAggrFnFactory, Cloneable {

  int   max;
  int   secondMax;
  int   numInputs;

  float fmax;
  float fsecondMax;
  
  char[] cmax;
  char[] csecondMax;

  byte[] bmax;
  byte[] bsecondMax;
  
  long lmax;
  long lsecondMax;
  
  long imax;
  long isecondMax;
  
  public IAggrFunction newAggrFunctionHandler() throws UDAException {
    return new TkUsrSecondMax();
  }

  public void freeAggrFunctionHandler(IAggrFunction handler) throws UDAException {
  }

  public void initialize() throws UDAException {
    max        = 0;
    secondMax  = 0;
    numInputs  = 0;
    fmax       = 0;
    fsecondMax = 0;
    cmax       = null;
    bmax       = null;
    csecondMax = null;
    bsecondMax = null;
    lmax       = 0;
    lsecondMax = 0;
    imax = 0;
    isecondMax =0;
  }

  public void handlePlus(AggrInteger value, AggrInteger result) throws UDAException {
    int v = 0;
    
    if(!value.isNull())
    {
      numInputs++;
      v = value.getValue();
      
      
      if(numInputs == 0)
      {
        result.setNull(true);
      }
  
      else if (numInputs == 1) {
        max = v;
        result.setValue(max);
        return;
      }
  
      else if (numInputs == 2) {
        if (v > max) {
          secondMax = max;
          max       = v;
        }
        else 
          secondMax = v;
      }
      else {
        if (v > max) {
          secondMax = max;
          max       = v;
        }
        else if (v > secondMax) 
          secondMax = v;
      }
  
      result.setValue(secondMax);
    }
    else
    {
      switch(numInputs)
      {
      case 0: 
        result.setNull(true);
        break;
      case 1: 
        result.setValue(max);
        break;
      default:
        result.setValue(secondMax);
      break;
      }
    }
  }

  public void handlePlus(AggrFloat value, AggrFloat result) throws UDAException {
    float v = 0.0f;

    if(!value.isNull())
    {
      numInputs++;
      v = value.getValue();
    
      if(numInputs == 0)
      {
        result.setNull(true);
      }
    
      else if (numInputs == 1) {
        fmax = v;
        result.setValue(fmax);
        return;
      }
  
      else if (numInputs == 2) {
        if (v > fmax) {
          fsecondMax = fmax;
          fmax       = v;
        }
        else
          fsecondMax = v;
      }
      else {
        if (v > fmax) {
          fsecondMax = fmax;
          fmax       = v;
        }
        else if (v > fsecondMax)
          fsecondMax = v;
      }
  
      result.setValue(fsecondMax);
    }
  	else // value.isNull() == true
    { 
      switch(numInputs)
      {
      case 0: 
        result.setNull(true);
        break;
      case 1: 
        result.setValue(fmax);
        break;
      default:
        result.setValue(fsecondMax);
      break;
      }
    }
  }
  
  public void handlePlus(AggrChar value, AggrChar result) throws UDAException {
    char[] v = null;
    String val, maxval, secondMaxval;
    
    if(!value.isNull())
    {
      numInputs++;
      v = value.getValue();
    }

    if (v != null)
    {
      if(numInputs == 0)
      {
        result.setNull(true);
      }
      else if (numInputs == 1) {
        cmax = v;
        result.setValue(cmax);
        return;
      }
      else if (numInputs == 2) {
        val    = new String(v);
        maxval = new String(cmax);
        if ((val.compareTo(maxval)) > 0) {
          csecondMax = cmax;
          cmax       = v;
        }
        else 
          csecondMax = v;
      }
      else {
        val          = new String(v);
        maxval       = new String(cmax);
        secondMaxval = new String(csecondMax); 
        if ((val.compareTo(maxval)) > 0) {
          csecondMax = cmax;
          cmax       = v;
        }
        else if ((val.compareTo(secondMaxval)) > 0) 
          csecondMax = v;
      }
  
      result.setValue(csecondMax);
    }
    else
    {
      switch(numInputs)
      {
      case 0: 
        result.setNull(true);
        break;
      case 1: 
        result.setValue(cmax);
        break;
      default:
        result.setValue(csecondMax);
      break;
      }
    }
  }
  
  public void handlePlus(AggrByte value, AggrByte result) throws UDAException {
    byte[] v = null;
        
    if(!value.isNull())
    {
      numInputs++;
      v = value.getValue();
    }
    if (v != null)
    {
      if(numInputs == 0)
      {
        result.setNull(true);
      }
      else if (numInputs == 1) {
        bmax = v;
        result.setValue(bmax);
        return;
      }
      else if (numInputs == 2) {
        if ((compare(v, bmax)) == 1) {
          bsecondMax = bmax;
          bmax       = v;
        }
        else 
          bsecondMax = v;
      }
      else {
        if ((compare(v, bmax)) == 1) {
          bsecondMax = bmax;
          bmax       = v;
        }
        else if ((compare(v, bsecondMax)) == 1) 
          bsecondMax = v;
      }
  
      result.setValue(bsecondMax);
    }
    else
    {
      switch(numInputs)
      {
      case 0: 
        result.setNull(true);
        break;
      case 1: 
        result.setValue(bmax);
        break;
      default:
        result.setValue(bsecondMax);
      break;
      }
    }
  }
  public void handlePlus(AggrTimestamp value, AggrTimestamp result) throws UDAException {
    long v = 0;
   
    if(!value.isNull())
    {
      numInputs++;
      v = value.getValue();
       
      if(numInputs == 0)
      {
        result.setNull(true);
      }
  
      else if (numInputs == 1) {
        lmax = v;
        result.setValue(lmax);
        return;
      }
  
      else if (numInputs == 2) {
        if (v > lmax) {
          lsecondMax = lmax;
          lmax       = v;
        }
        else 
          lsecondMax = v;
      }
      else {
        if (v > lmax) {
          lsecondMax = lmax;
          lmax       = v;
        }
        else if (v > lsecondMax) 
          lsecondMax = v;
      }
  
      result.setValue(lsecondMax);
    }
    else
    {
      switch(numInputs)
      {
      case 0: 
        result.setNull(true);
        break;
      case 1: 
        result.setValue(lmax);
        break;
      default:
        result.setValue(lsecondMax);
      break;
      }
    }
  }
  
  
  public void handlePlus(AggrInterval value, AggrInterval result) throws UDAException {
    long v = 0;
    
    if(!value.isNull())
    {
      numInputs++;
      v = value.getValue();
      
      if(numInputs == 0)
      {
        result.setNull(true);
      }
  
      else if (numInputs == 1) {
        imax = v;
        result.setValue(imax);
        return;
      }
  
      else if (numInputs == 2) {
        if (v > imax) {
          isecondMax = imax;
          imax       = v;
        }
        else 
          isecondMax = v;
      }
      else {
        if (v > imax) {
          isecondMax = imax;
          imax       = v;
        }
        else if (v > isecondMax) 
          isecondMax = v;
      }
  
      result.setValue(isecondMax);
    }
    else
    {
      switch(numInputs)
      {
      case 0: 
        result.setNull(true);
        break;
      case 1: 
        result.setValue(imax);
        break;
      default:
        result.setValue(isecondMax);
      break;
      }
    }
  }
  
  public void handlePlus(AggrValue[] args, AggrValue result) throws UDAException{
    
    if (args[0] instanceof AggrInteger)
      handlePlus(((AggrInteger) args[0]), ((AggrInteger) result));
    else if (args[0] instanceof AggrFloat)
      handlePlus(((AggrFloat) args[0]), ((AggrFloat) result));
    else if (args[0] instanceof AggrChar)
      handlePlus(((AggrChar) args[0]), ((AggrChar) result));
    else if (args[0] instanceof AggrByte)
      handlePlus(((AggrByte) args[0]), ((AggrByte) result));
    else if (args[0] instanceof AggrTimestamp)
      handlePlus(((AggrTimestamp) args[0]), ((AggrTimestamp) result));
    else if (args[0] instanceof AggrInterval)
      handlePlus(((AggrInterval) args[0]), ((AggrInterval) result));
    else
      assert false;
  }
  
  /**
   * Compares two byte arrays left-to-right.
   * @param val1 1st argument
   * @param val2 2nd argument
   * @return integer. +1 if val1 > val2 , -1 otherwise.
   */
  private int compare(byte[] val1, byte[] val2)
  {
    if (val1 == null)
      return -1;
    else if (val2 == null)
      return 1;
    int len1 = val1.length;
    int len2 = val2.length;
    int len  = len1 < len2 ? len1 : len2;
    int i    = 0;
    
    for (i = 0; i < len; i++) {
      if (val1[i] < val2[i]) 
        return -1;
      else if (val1[i] > val2[i])
        return 1;
    }
    
    if (len1 > len2)
        return 1;
      else
        return -1;
  }

  public Object clone()
  {
    TkUsrSecondMax myClone = new TkUsrSecondMax();
    if(this.bmax != null)
      myClone.bmax = this.bmax.clone();
    else
      myClone.bmax = null;
    if(this.bsecondMax != null)
      myClone.bsecondMax = this.bsecondMax.clone();
    else
      myClone.bsecondMax = null;
    if(this.cmax != null)
      myClone.cmax = this.cmax.clone();
    else
      myClone.cmax = null;
    if(this.csecondMax != null)
      myClone.csecondMax = this.csecondMax.clone();
    else
      myClone.csecondMax = null;
    myClone.fmax = this.fmax;
    myClone.fsecondMax = this.fsecondMax;
    myClone.imax = this.imax;
    myClone.isecondMax = this.isecondMax;
    myClone.lmax = this.lmax;
    myClone.lsecondMax = this.lsecondMax;
    myClone.max = this.max;
    myClone.numInputs = this.numInputs;
    myClone.secondMax = this.secondMax;
    return myClone;
  }
}
