/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/SharedQueueReaderFactory.java /main/7 2011/04/10 21:20:46 sborah Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Factory for creation of a Shared Queue Reader

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    04/05/11 - set the QueueReaderContext
    anasrini    10/29/08 - pass in reader instance to addReader API
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    hopark      02/25/08 - support paged queue
    parujain    12/06/06 - propagating relation
    najain      12/04/06 - stores are not storage allocators
    najain      10/26/06 - add asserts
    najain      10/13/06 - add statistics
    najain      06/29/06 - factory allocation cleanup 
    najain      06/18/06 - cleanup
    najain      06/16/06 - query deletion enhancements 
    najain      06/13/06 - bug fix 
    najain      05/04/06 - sharing support 
    anasrini    03/16/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/SharedQueueReaderFactory.java /main/6 2008/11/13 22:18:50 anasrini Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.queues.ISharedQueueReader;
import oracle.cep.execution.queues.ISharedQueueWriter;
import oracle.cep.execution.queues.Queue;
import oracle.cep.execution.queues.QueueReaderContext;
import oracle.cep.execution.queues.SharedQueueReader;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyQueue;
import oracle.cep.phyplan.PhySharedQueueReader;
import oracle.cep.phyplan.PhyQueueKind;
import oracle.cep.planmgr.PlanManager;
import oracle.cep.service.ExecContext;
import oracle.cep.exceptions.CEPException;

/**
 * Factory for creation of a Shared Queue Reader
 *
 * @since 1.0
 */

class SharedQueueReaderFactory extends ExecQueueFactory {

  /**
   * The assumption here is that when this method is called, the 
   * associated shared queue writer (the out queue of the corresponding
   * input operator) is already instantiated
   */
  protected Queue newExecQueue(QueueGenContext ctx)
    throws CEPException {

    ExecContext          ec = ctx.getExecContext();
    PhyQueue             phyQueue  = ctx.getPhyQueue();
    QueueReaderContext   readerCtx = ctx.getReaderContext();
    PhySharedQueueReader phyrQ;
    PhyQueueKind         kind      = phyQueue.getQueueKind();
    ISharedQueueReader   execQueue;
    PhyQueue             pwq;
    ISharedQueueWriter   ewq;

    assert phyQueue != null;
    assert kind == PhyQueueKind.READER_Q : kind;

    phyrQ = (PhySharedQueueReader)phyQueue;

    ObjectFactoryContext objCtx = new ObjectFactoryContext(ec, SharedQueueReader.class.getName());
    objCtx.setOpt(phyrQ.getDestOp());
    execQueue = (ISharedQueueReader)ObjectManager.allocate(objCtx);

    // set the writer
    pwq = phyrQ.getWriter(); 
    ewq = (ISharedQueueWriter) pwq.getInstQueue();
    execQueue.setWriter(ewq);
    
    assert phyrQ.getDestOp().getInstOp() != null;
    execQueue.setDestOp(phyrQ.getDestOp().getInstOp());

    // Set the ReaderContext before registering through the "addReader" call
    execQueue.setReaderContext(readerCtx);

    // Register the execution reader queue as a subscriber to its 
    // execution writer queue
    int readerId = ewq.addReader(execQueue); 
    
    boolean flag = propagationReqd(ec, ewq, execQueue);
    
    // If flag is true then the reader needs to be propagated
    if(flag)
      ewq.getSrcOp().addReader(readerId);
    
    // set the reader id
    execQueue.setReaderId(readerId);
    
    ewq.initRdrStats(readerId);

    phyQueue.setInstQueue((Queue)execQueue);
    return (Queue) execQueue;
  }
  
  /**
   * Checks whether Propagation to readers is required or not.
   * This is basically necessary for dynamic query addition
   * 
   * @param writer
   *               Shared Queue Writer
   * @param reader
   *               Shared Queue Reader
   * @return Whether needs propagation or not
   */
  private boolean propagationReqd(ExecContext ec, ISharedQueueWriter writer, ISharedQueueReader reader)
  {
    ExecOpt writerOp = writer.getSrcOp(); 
    PhyOpt  writerPhyOp = ec.getPlanMgr().getPhyOpt(writerOp.getPhyOptId());
    
    // If the operator does not evaluate to a relation then return false
    if(writerPhyOp.getIsStream())
      return false;
    
    // In case of Views there can be two queryIds, 
    //  There can be Query on top of View on top of View etc.
    // So check if this the only reader getting added
    if(writerPhyOp.getNumOutputs() == 1)
      return false;
 
    return true;
  }
}
