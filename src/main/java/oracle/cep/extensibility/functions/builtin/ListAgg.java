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

import java.util.ArrayList;

import oracle.cep.extensibility.functions.AggrInteger;
import oracle.cep.extensibility.functions.AggrObj;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.UDAException;


@SuppressWarnings("unchecked")
public class ListAgg extends AggrFunctionImpl 
  implements IAggrFnFactory, Cloneable
{
  private ArrayList outList;

  public ListAgg() {
    try {
      initialize();
    }
    catch(UDAException e) {
    }
  }

  public IAggrFunction newAggrFunctionHandler() throws UDAException 
  {
    return new ListAgg();
  }
  
  public void freeAggrFunctionHandler(IAggrFunction handler) 
  throws UDAException 
  {
    outList = null;
  }
  
  public void initialize() throws UDAException 
  {
    outList = new ArrayList();
  }
  
  public void handlePlus(AggrValue[] args, AggrValue result) 
  throws UDAException 
  {
    outList.add(args[0].getValue());
    
    // Now clone the list since an operator downstream may
    // iterate over this list while it may be concurrently being modified
    // here
    ((AggrObj)result).setValue(outList);
  }
  
  public void handleMinus(AggrValue[] args, AggrValue result) 
  throws UDAException 
  {
    outList.remove(args[0].getValue());

    // Now clone the list since an operator downstream may
    // iterate over this list while it may be concurrently being modified
    // here
    ((AggrObj)result).setValue(outList);
  }
  
  public Object clone()
  {
    ListAgg myClone = new ListAgg();
    if (outList != null)
      myClone.outList.addAll(this.outList);
    return myClone;
  }
  
}
