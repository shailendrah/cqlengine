/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/BindStoreImplFactory.java /main/3 2008/10/24 15:50:20 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/10/08 - remove statics
    rkomurav    07/26/07 - cleanup
    rkomurav    05/15/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/BindStoreImplFactory.java /main/3 2008/10/24 15:50:20 hopark Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr.factory;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.stores.BindStoreImpl;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.ObjectFactory;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.StoreFactoryContext;
import oracle.cep.service.ExecContext;

public class BindStoreImplFactory extends ObjectFactory
{  
  public BindStoreImpl allocate(ObjectFactoryContext ctx)
    throws CEPException
  {
    assert ctx instanceof StoreFactoryContext; 
    StoreFactoryContext sfc = (StoreFactoryContext) ctx;
    ExecContext ec = sfc.getExecContext();
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    return new BindStoreImpl(ec);
  }
  
  public boolean isPrimary()
  {
    return true;
  }
  
  public void free(ObjectFactoryContext ctx)
  throws CEPException
  {
    Object obj = ctx.getObject();
    assert obj instanceof BindStoreImpl;
  }

}
