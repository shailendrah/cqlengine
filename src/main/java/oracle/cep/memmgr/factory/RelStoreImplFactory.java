/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/RelStoreImplFactory.java /main/4 2008/10/24 15:50:20 hopark Exp $ */

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
    hopark      03/13/07 - moved to memmgr.factory
    najain      02/06/07 - coverage
    najain      06/16/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/RelStoreImplFactory.java /main/4 2008/10/24 15:50:20 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.memmgr.factory;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.stores.RelStoreImpl;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.ObjectFactory;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.StoreFactoryContext;
import oracle.cep.service.ExecContext;

/**
 * RelStoreImpl Allocation Factory
 *
 * @since 1.0
 */

public class RelStoreImplFactory extends ObjectFactory
{  
  public RelStoreImpl allocate(ObjectFactoryContext ctx)
    throws CEPException
  {
    assert ctx instanceof StoreFactoryContext;
    StoreFactoryContext sfctx = (StoreFactoryContext) ctx;
    ExecContext ec = sfctx.getExecContext();
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    return new RelStoreImpl(ec, sfctx.getFactory());
  }

  public void free(ObjectFactoryContext ctx)
    throws CEPException
  {
    Object obj = ctx.getObject();
    assert obj instanceof RelStoreImpl;
  }

  public boolean isPrimary()
  {
    return true;
  }

}

