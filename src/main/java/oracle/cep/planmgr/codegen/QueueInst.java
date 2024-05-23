/* $Header: QueueInst.java 21-jul-2006.17:15:08 parujain Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Factory for instantiating an execution representation of a queue

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    07/21/06 - Generic LinkedList 
    anasrini    03/16/06 - Creation
    anasrini    03/16/06 - Creation
    anasrini    03/16/06 - Creation
 */

/**
 *  @version $Header: QueueInst.java 21-jul-2006.17:15:08 skmishra Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.planmgr.codegen;

import java.util.HashMap;
import oracle.cep.phyplan.PhyQueueKind;
import oracle.cep.execution.queues.Queue;
import oracle.cep.exceptions.CEPException;

/**
 * Instantiates an execution queue corresponding to a physical queue
 * 
 * @author anasrini
 * @since 1.0
 */

public class QueueInst {

  private static HashMap<PhyQueueKind, ExecQueueFactory> execMap;

  static {
    populateExecMap();
  }

  private static void populateExecMap() {
    execMap = new HashMap<PhyQueueKind, ExecQueueFactory>();

    execMap.put(PhyQueueKind.READER_Q, new SharedQueueReaderFactory());
    execMap.put(PhyQueueKind.WRITER_Q, new SharedQueueWriterFactory());

  }

  public static Queue instQueue(QueueGenContext ctx) throws CEPException {
    Queue            execQueue;
    ExecQueueFactory factory;
    PhyQueueKind     kind;

    kind    = ctx.getPhyQueue().getQueueKind();
    factory = execMap.get(kind);
    assert factory != null : kind;

    execQueue = factory.newExecQueue(ctx);
    return execQueue;
  }
}
  
