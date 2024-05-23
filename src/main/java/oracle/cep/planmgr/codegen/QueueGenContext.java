/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/QueueGenContext.java /main/4 2011/04/10 21:20:46 sborah Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Context for execution queue code generation

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    04/05/11 - add QueueReaderContext
    hopark      10/09/08 - remove statics
    najain      03/14/07 - cleanup
    najain      07/07/06 - 
    anasrini    03/16/06 - Creation
    anasrini    03/16/06 - Creation
    anasrini    03/16/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/QueueGenContext.java /main/3 2008/10/24 15:50:17 hopark Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.execution.queues.QueueReaderContext;
import oracle.cep.phyplan.PhyQueue;
import oracle.cep.service.ExecContext;

/**
 * Context for code generation passed into all the individual
 * methods. This is an alternative to queue specific context,
 * and different signatures at the high-level.
 *
 * @author anasrini
 * @since 1.0
 */

public class QueueGenContext {

  private ExecContext execContext;
  private PhyQueue queue;
  private QueueReaderContext readerCtx;

  /**
   * Constructor
   * @param ec TODO
   * @param queue physical layer representation of the queue
   */
  public QueueGenContext(ExecContext ec, PhyQueue queue, 
                         QueueReaderContext readerCtx) {
    this.queue = queue;
    this.execContext = ec;
    this.readerCtx = readerCtx;
  }

  // Getter methods

  /**
   * Get the physical layer representation of the queue
   * @return the physical layer representation of the queue
   */
  public PhyQueue getPhyQueue() {
    return queue;
  }
  
  public ExecContext getExecContext() {
    return execContext;
  }
  
  public QueueReaderContext getReaderContext()
  {
    return readerCtx;
  }
  
}
