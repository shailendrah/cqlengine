/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/BindSynopsisImplFactory.java /main/3 2009/03/12 22:23:25 parujain Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      03/12/09 - fix synchronization
    hopark      12/02/08 - move LogLevelManaer to ExecContext
    rkomurav    05/15/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/BindSynopsisImplFactory.java /main/3 2009/03/12 22:23:25 parujain Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr.factory;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.synopses.BindingSynopsisImpl;
import oracle.cep.memmgr.ObjectFactory;
import oracle.cep.memmgr.ObjectFactoryContext;

public class BindSynopsisImplFactory extends ObjectFactory
{
  public BindingSynopsisImpl allocate(ObjectFactoryContext ctx)
  throws CEPException
  {
    return new BindingSynopsisImpl(ctx.getExecContext());
  }
  
  public void free(ObjectFactoryContext ctx)
    throws CEPException
  {
    //dummy
    Object obj = ctx.getObject();
    assert obj instanceof BindingSynopsisImpl;
    //((BindingSynopsisImpl)obj).remove();
    //freeList.add(obj);
  }
  
  public boolean isPrimary()
  {
    return false;
  }
}

