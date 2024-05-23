/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/DirectInteropQueueFactory.java /main/2 2008/12/10 18:55:57 hopark Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates.All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/02/08 - move LogLevelManaer to ExecContext
    anasrini    11/10/08 - Creation
    anasrini    11/10/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/DirectInteropQueueFactory.java /main/2 2008/12/10 18:55:57 hopark Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr.factory;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.queues.Queue;
import oracle.cep.execution.queues.DirectInteropQueue;
import oracle.cep.memmgr.IEvictPolicy;
import oracle.cep.memmgr.ObjectFactory;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.service.ExecContext;

/**
 * SharedQueueWriter Allocation Factory
 *
 * @since 1.0
 */

public class DirectInteropQueueFactory extends ObjectFactory
{  
  public Queue allocate(ObjectFactoryContext ctx)
    throws CEPException
  {
    ExecContext ec = ctx.getExecContext();
    
    IEvictPolicy evPolicy = ec.getServiceManager().getEvictPolicy();
    if (evPolicy == null || !evPolicy.isFullSpill())
      return new DirectInteropQueue(ec);
    else
      return new oracle.cep.execution.queues.stored.SharedQueueWriter(ec);
  }

  public void free(ObjectFactoryContext ctx)
    throws CEPException
  {
    Object obj = ctx.getObject();
    assert obj instanceof Queue;
  }

  public boolean isPrimary()
  {
    return true;
  }
}
