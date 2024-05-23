/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/WinStoreImplFactory.java /main/7 2008/10/24 15:50:19 hopark Exp $ */

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
    hopark      05/05/08 - remove FullSpillMode
    hopark      03/27/08 - use getFullSpillMode
    hopark      02/21/08 - stored WinStore
    hopark      03/13/07 - moved to memmgr.factory
    najain      02/06/07 - coverage
    najain      06/27/06 - implement free 
    najain      06/16/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/WinStoreImplFactory.java /main/7 2008/10/24 15:50:19 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.memmgr.factory;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.stores.WinStore;
import oracle.cep.execution.stores.WinStoreImpl;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IEvictPolicy;
import oracle.cep.memmgr.ObjectFactory;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.StoreFactoryContext;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;

/**
 * WinStoreImpl Allocation Factory
 *
 * @since 1.0
 */

public class WinStoreImplFactory extends ObjectFactory
{  
  public WinStore allocate(ObjectFactoryContext ctx)
    throws CEPException
  {
    assert ctx instanceof StoreFactoryContext; 
    StoreFactoryContext sfc = (StoreFactoryContext) ctx;
    ExecContext ec = sfc.getExecContext();
    CEPManager cepMgr = ec.getServiceManager();
    FactoryManager factoryMgr = cepMgr.getFactoryManager();
    IEvictPolicy evPolicy = cepMgr.getEvictPolicy();
    if (evPolicy == null || !evPolicy.isFullSpill())
      return new WinStoreImpl(ec, sfc.getFactory());
    else
      return new oracle.cep.execution.stores.stored.WinStoreImpl(ec, sfc.getFactory());
  }

  public void free(ObjectFactoryContext ctx)
    throws CEPException
  {
    Object obj = ctx.getObject();
    assert obj instanceof WinStore;
  }

  public boolean isPrimary()
  {
    return true;
  }

}

