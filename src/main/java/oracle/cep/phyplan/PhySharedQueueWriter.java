/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhySharedQueueWriter.java /main/2 2009/06/02 12:21:26 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Physical representation of a shared queue writer

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 parujain    06/01/09 - id assigned by PlanManager
 najain      05/04/06 - support sharing 
 najain      03/20/06 - more setters/getters etc.
 anasrini    03/16/06 - Creation
 anasrini    03/16/06 - Creation
 anasrini    03/16/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhySharedQueueWriter.java /main/2 2009/06/02 12:21:26 parujain Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.phyplan;


/**
 * Physical representation of a shared queue writer
 * <p>
 * A shared queue writer is always associated with one or more shared queue
 * readers. The shared queue writer is associated with a source operator
 * only.
 *
 * @since 1.0
 */

public class PhySharedQueueWriter extends PhyQueue {

  /** The associated source operator */
  PhyOpt     source;

  /**
   * Constructor
   */
  public PhySharedQueueWriter(int queue_id) {
    super(queue_id);
  }

  // Getter methods

  public PhyQueueKind getQueueKind() {
    return PhyQueueKind.WRITER_Q;
  }

  /**
   * Get the associated source operator
   * @return the associated source operator
   */
  public PhyOpt getSourceOp() {
    return source;
  }

  /**
   * @return Returns the source.
   */
  public PhyOpt getSource() {
    return source;
  }

  /**
   * @param source The source to set.
   */
  public void setSource(PhyOpt source) {
    this.source = source;
  }

}
