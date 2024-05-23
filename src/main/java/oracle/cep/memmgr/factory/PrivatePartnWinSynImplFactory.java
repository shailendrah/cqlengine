/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/PrivatePartnWinSynImplFactory.java /main/4 2009/03/12 22:23:25 parujain Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates.
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
    udeshmuk    10/11/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/PrivatePartnWinSynImplFactory.java /main/4 2009/03/12 22:23:25 parujain Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr.factory;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.synopses.PrivatePartnWindowSynopsisImpl;
import oracle.cep.memmgr.ObjectFactory;
import oracle.cep.memmgr.ObjectFactoryContext;

public class PrivatePartnWinSynImplFactory extends ObjectFactory
{
  /**
   *  Allocate a synopsis for the partition window operator
   */
  public PrivatePartnWindowSynopsisImpl allocate(ObjectFactoryContext ctx) throws CEPException
  {
    // check if something is available in the freelist, otherwise allocate
    // a new one
    //if (freeList.isEmpty())
      return new PrivatePartnWindowSynopsisImpl(ctx.getExecContext());
    /*else
    {
      Object obj = freeList.removeFirst();
      assert obj instanceof PrivatePartnWindowSynopsisImpl;
      ((PrivatePartnWindowSynopsisImpl)obj).init();
      return (PrivatePartnWindowSynopsisImpl)obj;
    }*/
  }

  public void free(ObjectFactoryContext ctx)
    throws CEPException
  {
    Object obj = ctx.getObject();
    assert obj instanceof PrivatePartnWindowSynopsisImpl;
    ((PrivatePartnWindowSynopsisImpl)obj).remove();
    //freeList.add(obj);
  }

  public boolean isPrimary()
  {
    return false;
  }
}

