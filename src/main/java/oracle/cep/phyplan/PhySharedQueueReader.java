/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhySharedQueueReader.java /main/2 2009/06/02 12:21:26 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Physical representation of a shared queue reader

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 parujain    06/01/09 - id assigned by PlanManager
 najain      05/04/06 - sharing support 
 najain      03/20/06 - add setters/getters etc.
 anasrini    03/16/06 - Creation
 anasrini    03/16/06 - Creation
 anasrini    03/16/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhySharedQueueReader.java /main/2 2009/06/02 12:21:26 parujain Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.phyplan;


/**
 * Physical representation of a shared queue reader
 * <p>
 * A shared queue reader is always associated with exactly one shared queue
 * writer. The shared queue reader is associated with a destination operator
 * only.
 * 
 * @since 1.0
 */

public class PhySharedQueueReader extends PhyQueue {

  /** The associated shared queue writer who holds our state */
  PhySharedQueueWriter writer;

  /** The associated destination operator */
  PhyOpt               dest;
  
  public PhySharedQueueReader(int queue_id)
  {
    super(queue_id);
  }

  // Getter methods

  public PhyQueueKind getQueueKind() {
    return PhyQueueKind.READER_Q;
  }

  /**
   * Get the destination operator for this queue
   * 
   * @return the destination operator for this queue
   */
  public PhyOpt getDestOp() {
    return dest;
  }

  /**
   * @param dest
   *          The dest to set.
   */
  public void setDest(PhyOpt dest) {
    this.dest = dest;
  }

  /**
   * @param writer
   *          The writer to set.
   */
  public void setWriter(PhySharedQueueWriter writer) {
    this.writer = writer;
  }

  /**
   * @return Returns the writer.
   */
  public PhySharedQueueWriter getWriter()
  {
    return writer;
  }
}
