/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/First.java /main/2 2011/10/12 07:03:25 udeshmuk Exp $ */

/* Copyright (c) 2008, 2011, Oracle and/or its affiliates. 
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
    udeshmuk    10/02/11 - implement clone
    rkomurav    03/31/08 - Creation
 */

/**
 *  @version $Header: First.java 31-mar-2008.07:28:05 rkomurav Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.extensibility.functions.builtin;

import oracle.cep.extensibility.functions.AggrInteger;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.UDAException;

public class First extends AggrFunctionImpl implements IAggrFnFactory
{
  int       index;
  int       size;
  AggrValue first;
  
  public IAggrFunction newAggrFunctionHandler() throws UDAException
  {
    return new First();
  }
  
  public void freeAggrFunctionHandler(IAggrFunction handler)
  {
  }
  
  public void initialize()
  {
    size = 0;
  }
  
  // index ranges from 1 to n
  public void handlePlus(AggrValue[] args, AggrValue result)
  {
    index = ((AggrInteger)args[1]).getValue();
    
    assert index > 0;
    
    if(size == (index - 1))
    {
      size++;
      first = args[0].clone();
    }
    else if(size < index)
    {
      size++;
      result.setNull(true);
      return;
    }
    
    first.copy(result);
  }
  
  public void handleMinus(AggrValue[] args, AggrValue result)
  {
    //for pattern streams it should never be called
    assert false;
  }
  
  public Object clone()
  {
    First myClone = new First();
    if(this.first != null)
      myClone.first = this.first.clone();
    else 
      myClone.first = null;
    myClone.index = this.index;
    myClone.size  = this.size;
    return myClone;
  }
}


