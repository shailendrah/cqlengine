/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/SharedQueueWriterFactory.java /main/9 2009/03/12 22:23:25 parujain Exp $ */

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
    hopark      10/10/08 - remove statics
    hopark      05/05/08 - remove FullSpillMode
    hopark      03/27/08 - use getFullSpillMode
    hopark      02/25/08 - support paged queue
    hopark      03/13/07 - moved to memmgr.factory
    najain      02/06/07 - coverage
    najain      06/28/06 - cleanup
    najain      06/16/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/SharedQueueWriterFactory.java /main/9 2009/03/12 22:23:25 parujain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr.factory;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.queues.Queue;
import oracle.cep.execution.queues.SharedQueueWriter;
import oracle.cep.memmgr.IEvictPolicy;
import oracle.cep.memmgr.ObjectFactory;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;

/**
 * SharedQueueWriter Allocation Factory
 *
 * @since 1.0
 */

public class SharedQueueWriterFactory extends ObjectFactory
{  
  public Queue allocate(ObjectFactoryContext ctx)
    throws CEPException
  {
    ExecContext ec = ctx.getExecContext();
    
    IEvictPolicy evPolicy = ec.getServiceManager().getEvictPolicy();
    if (evPolicy == null || !evPolicy.isFullSpill())
      return new SharedQueueWriter(ec);
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
