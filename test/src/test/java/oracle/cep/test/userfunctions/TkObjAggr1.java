/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkObjAggr1.java /main/3 2011/10/12 07:03:25 udeshmuk Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    User Defined Function using object

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    10/12/11 - XbranchMerge udeshmuk_bug-13060688_ps5 from
                           st_pcbpel_11.1.1.4.0
    hopark      02/13/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkObjAggr1.java /main/2 2009/09/13 23:57:27 sbishnoi Exp $
 *  @author  anasrini
 *  @since   1.0
 */
 
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.UDAException;
import oracle.cep.extensibility.functions.AggrObj;

public strictfp class TkObjAggr1 extends AggrFunctionImpl implements IAggrFnFactory, Cloneable {
    float sum;

    public IAggrFunction newAggrFunctionHandler() throws UDAException {
        return new TkObjAggr1();
    }

    public void freeAggrFunctionHandler(IAggrFunction handler) throws UDAException {
    }

    public void initialize() throws UDAException {
        sum = 0.0f;
    }

    public void handlePlus(AggrValue[] value, 
                           AggrValue result) throws UDAException 
    {
        if (!value[0].isNull()) 
        {
            TkObj a1 = (TkObj) ((AggrObj)value[0]).getValue();
            sum += a1.getFVal();
        }
        TkObj o = new TkObj();
        o.setFVal(sum);
        o.setIVal((int)sum);
        o.setLVal((long)sum*100);
        o.setDVal(sum/10000);
        o.setSVal("+sum="+sum);
        ((AggrObj)result).setValue(o);
    }

    public void handleMinus(AggrValue[] value, 
                            AggrValue result) throws UDAException {
        if (!value[0].isNull()) 
        {
            TkObj a1 = (TkObj) ((AggrObj)value[0]).getValue();
            sum -= a1.getFVal();
        }
        TkObj o = new TkObj();
        o.setFVal(sum);
        o.setIVal((int)sum);
        o.setLVal((long)sum*100);
        o.setDVal(sum/10000);
        o.setSVal("-sum="+sum);
        ((AggrObj)result).setValue(o);
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
