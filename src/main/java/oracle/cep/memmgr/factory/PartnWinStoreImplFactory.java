/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/PartnWinStoreImplFactory.java /main/5 2008/10/24 15:50:20 hopark Exp $ */

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
    ayalaman    08/03/06 - partition window store factory
    ayalaman    08/03/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/PartnWinStoreImplFactory.java /main/5 2008/10/24 15:50:20 hopark Exp $
 *  @author  ayalaman
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr.factory;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.stores.PartnWindowStoreImpl;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.ObjectFactory;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.StoreFactoryContext;
import oracle.cep.service.ExecContext;

/**
 * PartnWinStoreImplFactory Allocation Factory
 *
 * @since 1.0
 */

/**
 *  Factory class for creating new instances of partition window stores. 
 */
public class PartnWinStoreImplFactory extends ObjectFactory
{
  public PartnWindowStoreImpl allocate(ObjectFactoryContext ctx)
    throws CEPException
  {
    assert ctx instanceof StoreFactoryContext;
    StoreFactoryContext sfctx = (StoreFactoryContext) ctx;
    ExecContext ec = sfctx.getExecContext();
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    // the partition window factory uses a second tuple factory for creating 
    // header or partition key tuples. Make sure it is setup in the context
    assert (sfctx.getSecFactory() != null);

    return new PartnWindowStoreImpl(ec, sfctx.getFactory(), sfctx.getSecFactory());
  }

  public void free(ObjectFactoryContext ctx)
    throws CEPException
  {
    Object obj = ctx.getObject();
    assert obj instanceof PartnWindowStoreImpl;
  }

  public boolean isPrimary()
  {
    return true;
  }

}

