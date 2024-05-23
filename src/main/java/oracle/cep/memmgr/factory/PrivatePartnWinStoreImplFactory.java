/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/PrivatePartnWinStoreImplFactory.java /main/3 2008/11/13 21:59:39 udeshmuk Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates.All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    11/05/08 - renarename the class.
    hopark      10/19/08 - pass ExecContext
    udeshmuk    10/11/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/PrivatePartnWinStoreImplFactory.java /main/3 2008/11/13 21:59:39 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr.factory;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.stores.PrivatePartnWindowStoreImpl;
import oracle.cep.memmgr.ObjectFactory;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.StoreFactoryContext;
import oracle.cep.service.ExecContext;

public class PrivatePartnWinStoreImplFactory extends ObjectFactory
{
  public PrivatePartnWindowStoreImpl allocate(ObjectFactoryContext ctx)
    throws CEPException
  {
    assert ctx instanceof StoreFactoryContext;
    StoreFactoryContext sfctx = (StoreFactoryContext) ctx;
    ExecContext ec = sfctx.getExecContext();
    // the partition window factory uses a second tuple factory for creating 
    // header or partition key tuples. Make sure it is setup in the context
    assert (sfctx.getSecFactory() != null);

    return new PrivatePartnWindowStoreImpl(ec, sfctx.getFactory(), sfctx.getSecFactory());
  }

  public void free(ObjectFactoryContext ctx)
    throws CEPException
  {
    Object obj = ctx.getObject();
    assert obj instanceof PrivatePartnWindowStoreImpl;
  }

  public boolean isPrimary()
  {
    return true;
  }

}
