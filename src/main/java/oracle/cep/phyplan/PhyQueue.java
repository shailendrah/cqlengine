/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhyQueue.java /main/3 2009/06/02 12:21:26 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Physical layer representation of queues

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    05/29/09 - maintain Ids in PlanManager
    hopark      06/14/07 - add getId
    hopark      06/05/07 - add visitor
    anasrini    03/16/06 - creation
    anasrini    03/16/06 - creation
    anasrini    03/16/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhyQueue.java /main/3 2009/06/02 12:21:26 parujain Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.phyplan;

import oracle.cep.execution.queues.Queue;
import oracle.cep.planmgr.IPlanVisitable;
import oracle.cep.planmgr.IPlanVisitor;

/**
 * Base class for the physical layer representation of queues
 *
 * @since 1.0
 */

public abstract class PhyQueue implements IPlanVisitable {
  
  /** unique queue internal identifier */
  protected int id;

  /** Execution layer equivalent of this queue */
  protected Queue instQueue;

  
  public PhyQueue(int queue_id) 
  {
    id = queue_id;
  }
  
  // Getter methods
  
  /**
   * Get the kind of queue
   * @return the kind of queue
   */
  public abstract PhyQueueKind getQueueKind();

  /**
   * Get the execution layer equivalent of this queue
   * @return the execution layer equivalent of this queue
   */
  public Queue getInstQueue() {
    return instQueue;
  }
      
  // setter methods

  /**
   * Set the execution layer equivalent of this queue
   * @param instQueue the execution layer equivalent of this queue
   */
  public void setInstQueue(Queue instQueue) {
    this.instQueue = instQueue;
    instQueue.setPhyId(id);
  }

  /**
   * @return Returns the id.
   */
  public int getId() {
    return id;
  }
  
  /**
   *
   * @param visitor
   */
  public void accept(IPlanVisitor visitor) 
  {
    visitor.visit(this);    
  }
}
