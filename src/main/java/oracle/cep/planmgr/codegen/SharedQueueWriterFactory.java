/* $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/SharedQueueWriterFactory.java /main/5 2008/11/13 22:18:50 anasrini Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    Factory for creation of a Shared Queue Writer

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    11/10/08 - support for direct interop
    hopark      10/10/08 - remove statics
    hopark      02/25/08 - support paged queue
    najain      12/04/06 - stores are not storage allocators
    najain      10/29/06 - add asserts
    najain      10/18/06 - 
    parujain    07/21/06 - Generic LinkedList 
    najain      06/29/06 - factory allocation cleanup 
    najain      06/18/06 - cleanup
    najain      06/16/06 - query deletion enhancements 
    najain      05/04/06 - sharing support 
    anasrini    03/21/06 - set the store for queue elements 
    anasrini    03/16/06 - Creation
    anasrini    03/16/06 - Creation
    anasrini    03/16/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/SharedQueueWriterFactory.java /main/5 2008/11/13 22:18:50 anasrini Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.execution.queues.ISharedQueueWriter;
import oracle.cep.execution.queues.Queue;
import oracle.cep.execution.queues.SharedQueueWriter;
import oracle.cep.execution.queues.DirectInteropQueue;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.phyplan.PhyQueue;
import oracle.cep.phyplan.PhyQueueKind;
import oracle.cep.phyplan.PhySharedQueueWriter;
import oracle.cep.service.ExecContext;
import oracle.cep.exceptions.CEPException;

/**
 * Factory for creation of a Shared Queue Writer
 *
 * @since 1.0
 */

class SharedQueueWriterFactory extends ExecQueueFactory {

  protected Queue newExecQueue(QueueGenContext ctx)
    throws CEPException {

    PhyQueue             phyQueue = ctx.getPhyQueue();
    ExecContext          ec = ctx.getExecContext();
    PhyQueueKind         kind     = phyQueue.getQueueKind();
    ISharedQueueWriter   execQueue;

    assert phyQueue != null;
    assert kind == PhyQueueKind.WRITER_Q : kind;
    PhySharedQueueWriter phywQ = (PhySharedQueueWriter)phyQueue;

    ObjectFactoryContext objCtx;

    if (ec.getServiceManager().getConfigMgr().getDirectInterop())
      objCtx = new ObjectFactoryContext(ec,
                                        DirectInteropQueue.class.getName());
    else
      objCtx = new ObjectFactoryContext(ec, SharedQueueWriter.class.getName());

    objCtx.setOpt(phywQ.getSourceOp());
    execQueue = (ISharedQueueWriter)ObjectManager.allocate(objCtx);
    assert phywQ.getSource().getInstOp() != null;
    execQueue.setSrcOp(phywQ.getSource().getInstOp());

    phyQueue.setInstQueue((Queue)execQueue);
    return (Queue) execQueue;
  }
}
