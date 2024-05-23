/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/WinSynopsisImplFactory.java /main/6 2009/03/12 22:23:25 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
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
    hopark      03/13/07 - moved to memmgr.factory
    najain      06/19/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/WinSynopsisImplFactory.java /main/6 2009/03/12 22:23:25 parujain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.memmgr.factory;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.synopses.WindowSynopsisImpl;
import oracle.cep.memmgr.ObjectFactory;
import oracle.cep.memmgr.ObjectFactoryContext;


/**
 * WinSynopsisImpl Allocation Factory
 *
 * @since 1.0
 */

public class WinSynopsisImplFactory extends ObjectFactory
{  
  public WindowSynopsisImpl allocate(ObjectFactoryContext ctx)
    throws CEPException
  {
    // check if something is available in the freelist, otherwise allocate
    // a new one
    //if (freeList.isEmpty())
      return new WindowSynopsisImpl(ctx.getExecContext());
    /*else
    {
      Object obj = freeList.removeFirst();
      assert obj instanceof WindowSynopsisImpl;
      ((WindowSynopsisImpl)obj).init();
      return (WindowSynopsisImpl)obj;
    }*/
  }

  public void free(ObjectFactoryContext ctx)
    throws CEPException
  {
    Object obj = ctx.getObject();
    assert obj instanceof WindowSynopsisImpl;
    ((WindowSynopsisImpl)obj).remove();
    //freeList.add(obj);
  }

  public boolean isPrimary()
  {
    return false;
  }
}

