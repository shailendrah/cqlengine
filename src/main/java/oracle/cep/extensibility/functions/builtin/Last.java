/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/Last.java /main/2 2011/10/12 07:03:25 udeshmuk Exp $ */

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
 *  @version $Header: Last.java 09-apr-2008.21:54:17 rkomurav Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.extensibility.functions.builtin;

import java.util.LinkedList;

import oracle.cep.extensibility.functions.AggrInteger;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.UDAException;

public class Last extends AggrFunctionImpl implements IAggrFnFactory, Cloneable
{
  int                   index;
  int                   size;
  boolean               init;
  LinkedList<AggrValue> last;
  AggrValue             temp;
  AggrValue[]           aggrVals;
  
  public IAggrFunction newAggrFunctionHandler() throws UDAException
  {
    return new Last();
  }
  
  public void freeAggrFunctionHandler(IAggrFunction handler)
  {
  }
  
  public void initialize()
  {
    size = 0;
    init = false;
    last = new LinkedList<AggrValue>();
  }
  
  // index ranges from 1 to n
  public void handlePlus(AggrValue[] args, AggrValue result)
  {
    index = ((AggrInteger)args[1]).getValue();
    
    assert index > 0;
    
    if(!init)
    {
      init     = true;
      aggrVals = new AggrValue[index];
      for(int i = 0; i < index; i++)
      {
        aggrVals[i] = args[0].clone();
      }
    }
    
    if(size < (index - 1))
    {
      args[0].copy(aggrVals[size]);
      last.add(aggrVals[size]);
      size++;
      result.setNull(true);
      return;
    }
    else if(size == index - 1)
    {
      args[0].copy(aggrVals[size]);
      last.add(aggrVals[size]);
      size++;
    }
    else
    {
      temp = last.remove();
      args[0].copy(temp);
      last.add(temp);
    }
    last.getFirst().copy(result); 
  }
  
  public void handleMinus(AggrValue[] args, AggrValue result)
  {
    //for pattern streams it should never be called
    assert false;
  }
  
  public Object clone()
  {
    Last myClone = new Last();
    if(this.aggrVals != null)
    {
      myClone.aggrVals = new AggrValue[this.aggrVals.length];
      for(int i=0; i < this.aggrVals.length; i++)
      {
        myClone.aggrVals[i] = this.aggrVals[i].clone();
      }
    }
    else
      myClone.aggrVals = null;
    myClone.index = this.index;
    myClone.init = this.init;
    if(this.last != null)
    {
      myClone.last = new LinkedList<AggrValue>();
      for(AggrValue a : this.last)
      {
        myClone.last.add(a.clone());
      }
    }
    else
      myClone.last = null;
    myClone.size = this.size;
    if(this.temp != null)
      myClone.temp = this.temp.clone();
    else
      myClone.temp = null;
    return myClone;
  }
}


