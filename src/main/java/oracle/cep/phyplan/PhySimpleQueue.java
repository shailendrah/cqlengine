/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhySimpleQueue.java /main/2 2009/06/02 12:21:26 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Physical layer representation of a simple queue

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 parujain    06/01/09 - id assigned by PlanManager
 najain      03/20/06 - more setters/getters etc.
 anasrini    03/16/06 - Creation
 anasrini    03/16/06 - Creation
 anasrini    03/16/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhySimpleQueue.java /main/2 2009/06/02 12:21:26 parujain Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.phyplan;


/**
 * Physical layer representation of a simple queue
 * <p>
 * A simple queue connects one source physical operator to one destination
 * physical operator
 *
 * @since 1.0
 */

public class PhySimpleQueue extends PhyQueue {

  /** The source physical operator */
  PhyOpt source;

  /** The destination physical operator */
  PhyOpt dest;

  /** The index corresponding to the input operator of the destination 
   *  that is the source operator of this queue 
   */
  int    destInpIndex;

  public PhySimpleQueue(int queue_id)
  {
    super(queue_id);
  }
  
  // Getter methods

  public PhyQueueKind getQueueKind() {
    return PhyQueueKind.SIMPLE_Q;
  }

  /**
   * Get the source operator for this queue
   * @return the source operator for this queue
   */
  public PhyOpt getSourceOp() {
    return source;
  }

  /**
   * Get the destination operator for this queue
   * @return the destination operator for this queue
   */
  public PhyOpt getDestOp() {
    return dest;
  }

  /**
   * @return Returns the destInpIndex.
   */
  public int getDestInpIndex() {
    return destInpIndex;
  }

  /**
   * Get the index of the input operator of the destination that is the
   * source operator of this queue
   * @return input index of source operator in the destination operator
   */
  public int getSrcIndexInDest() {
    return destInpIndex;
  }

  /**
   * @param dest The dest to set.
   */
  public void setDest(PhyOpt dest) {
    this.dest = dest;
  }

  /**
   * @param destInpIndex The destInpIndex to set.
   */
  public void setDestInpIndex(int destInpIndex) {
    this.destInpIndex = destInpIndex;
  }

  /**
   * @param source The source to set.
   */
  public void setSource(PhyOpt source) {
    this.source = source;
  }

}
