/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/memmgr/AggrFunctionStorageElement.java /main/11 2011/10/12 07:03:25 udeshmuk Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Storage Element wrapper class for an aggregation function

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    10/12/11 - XbranchMerge udeshmuk_bug-13060688_ps5 from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    10/10/11 - XbranchMerge udeshmuk_bug-11933156_ps5 from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    09/26/11 - support clone
    sbishnoi    09/22/10 - XbranchMerge sbishnoi_bug-10132979_ps3 from
                           st_pcbpel_11.1.1.4.0
    sbishnoi    09/20/10 - adding getHandler()
    udeshmuk    10/17/07 - Remove input and return type specific versions of
                           handlePlus and handleMinus and add generic version
                           of handlePlusVoid.
    sbishnoi    06/12/07 - support for multi-arg UDAs
    najain      03/12/07 - bug fix
    najain      03/07/07 - bug fix
    hopark      02/22/07 - add default constructor for serialization
    hopark      01/23/07 - use StorageElementImpl
    rkomurav    01/05/07 - null UDA
    hopark      11/28/06 - add bigint datatype
    anasrini    07/16/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/memmgr/AggrFunctionStorageElement.java /main/9 2010/09/22 08:52:05 sbishnoi Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.memmgr;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.UDAException;

/**
 * Storage Element wrapper class for an aggregation function
 * 
 * @since 1.0
 */

public class AggrFunctionStorageElement 
  implements IAggrFunction, Serializable , Cloneable
{

  /** The handler for the aggregate function */
  private IAggrFunction handler;

  public AggrFunctionStorageElement() 
  {
    super();
    this.handler = null;
  }

  /**
   * Constructor
   * @param handler the handler for the aggregate function
   */
  public AggrFunctionStorageElement(IAggrFunction handler) 
  {
    super();
    this.handler = handler;
  }

  public void clear() {
  }

  // IAggrFunction interface

  public void initialize() throws UDAException {
    handler.initialize();
  }
  
  public void handlePlus(AggrValue[] args, AggrValue result) throws UDAException {
    handler.handlePlus(args,result);
  }
  
  public void handleMinus(AggrValue[] args, AggrValue result) throws UDAException {
    handler.handleMinus(args, result);
  }

  public IAggrFunction getHandler()
  {
    return handler;
  }
  
  public Object clone() 
  {
    if((this.handler == null)
      ||(!(this.handler instanceof Cloneable)))
    {
      return this;
    }
    else
    {
      try {
        Method m = this.handler.getClass().getMethod("clone",(Class<?>[])null);
        if(m != null)
        {
          IAggrFunction handle = (IAggrFunction)m.invoke(this.handler);
          return new AggrFunctionStorageElement(handle);
        }
        else
          return this;
      } catch(NoSuchMethodException nsme) {
        return this;
      } catch(InvocationTargetException e) {
        if(e.getCause() != null)
          throw new RuntimeException(e.getCause());
        else
          throw new RuntimeException(e);
      } catch(IllegalAccessException e) {
          return this;
      }
    }
    
  }
}


