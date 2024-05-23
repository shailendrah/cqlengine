/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/ListAgg.java /main/3 2012/09/12 06:41:22 sbishnoi Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    09/12/12 - XbranchMerge sbishnoi_bug-14286422_ps6_pt.11.1.1.7.0
                           from st_pcbpel_pt-11.1.1.7.0
    sbishnoi    09/10/12 - XbranchMerge sbishnoi_bug-14286422_ps6 from
    sbishnoi    09/06/12 - removing clone in listagg
    anasrini    05/28/12 - XbranchMerge anasrini_bug-13974437_ps6 from
                           st_pcbpel_11.1.1.4.0
    anasrini    05/24/12 - fix NPE in clone - bug 13974437
    anasrini    01/31/12 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/ListAgg.java /main/3 2012/09/12 06:41:22 sbishnoi Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.extensibility.functions.builtin;

import oracle.cep.extensibility.functions.AggrObj;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;

import java.io.Serializable;

import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.UDAException;

/**
 * This class is implementation for builtin-aggregate function "current".
 * @author sbishnoi
 *
 * Here are the semantics of current() function:
 * current(c1) is a special aggregate function which is used to preserve a non-aggregated column
 * from being dropped out of select list.
 * 
 * Example:
 * CREATE STREAM S1(c1 integer, c2 integer)
 * 
 * SELECT COUNT(*), c1 FROM S1 GROUP BY c1
 * 
 * We can't include c2 in select list as either summaries or group by attributes are allowed in
 * select list.
 * 
 * To preserve the non-groupby attributes, current() is a special aggregate function
 * which will only propagate the latest value of an attribute from input event.
 * 
 * The above use-case can be solved using following:
 * SELECT COUNT(*), c1, current(c2) FROM S1 GROUP BY c1
 */
@SuppressWarnings("unchecked")
public class Current extends AggrFunctionImpl 
  implements IAggrFnFactory, Cloneable, Serializable
{
  private static final long serialVersionUID = -4143528869755978274L;
  
  Object currentVal;

  public Current() {
    currentVal = null;
  }

  public IAggrFunction newAggrFunctionHandler() throws UDAException 
  {
    return new Current();
  }
  
  public void freeAggrFunctionHandler(IAggrFunction handler) 
  throws UDAException 
  {
    currentVal = null;
  }
  
  public void handlePlus(AggrValue[] args, AggrValue result) 
  throws UDAException 
  {
    if(args[0].isNull()) {
      currentVal = null;
      result.setNull(true);
    }
    else {
      currentVal = args[0].getValue();
      result.setValue(currentVal);
    }
  }
  
  public void handleMinus(AggrValue[] args, AggrValue result) 
  throws UDAException 
  {
    if(currentVal == null)
      result.setNull(true);
    else
      result.setValue(currentVal);
  }
  
  public Object clone()
  {
    Current clone = new Current();
    clone.currentVal = this.currentVal;
    return clone;
  }

  @Override
  public void initialize() throws UDAException
  {
    currentVal = null;
  }
  
}
