/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/StoreFactoryContext.java /main/5 2008/10/24 15:50:21 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/10/08 - remove statics
    najain      03/16/07 - cleanup
    ayalaman    08/04/06 - secondary factory for partition window
    najain      06/16/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/StoreFactoryContext.java /main/5 2008/10/24 15:50:21 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.memmgr;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.service.ExecContext;

/**
 * Stores (SimpleStore, WinStoreImpl, RelStoreImpl and LineageStoreImpl)
 * Allocations Factory Context
 *
 * @since 1.0
 */

public class StoreFactoryContext extends ObjectFactoryContext
{
  ExecContext           execContext;
  IAllocator<ITuplePtr> factory;
  IAllocator<ITuplePtr> secFactory; 

  public StoreFactoryContext(ExecContext ec, String objectType, IAllocator<ITuplePtr> factory)
  {
    super(ec, objectType);
    execContext = ec;
    this.factory = factory;
    this.secFactory = null; 
  }

  public StoreFactoryContext(ExecContext ec, String objectType)
  {
    super(ec, objectType);
    execContext = ec;
  }

  public IAllocator<ITuplePtr> getFactory()
  {
    return factory;
  }

  public void setFactory(IAllocator<ITuplePtr> factory)
  {
    this.factory = factory;
  }

  /**
   * Get the secondary tuple factory 
   * 
   * @return  secondary tuple factory
   */
  public IAllocator<ITuplePtr> getSecFactory()
  {
    return secFactory; 
  }

  /**
   * Set the secondary tuple factory 
   * 
   * @param  secFactory  secondary tuple factory if needed. 
   */
  public void setSecFactory(IAllocator<ITuplePtr> secFactory)
  {
    this.secFactory = secFactory; 
  }
 
  public ExecContext getExecContext()
  {
    return execContext;
  }
  
}

