/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/queues/QueueReaderContext.java /main/2 2012/06/18 06:29:07 udeshmuk Exp $ */

/* Copyright (c) 2011, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    05/08/12 - maintain snapshotId
    anasrini    04/05/11 - Creation
    anasrini    04/05/11 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/queues/QueueReaderContext.java /main/2 2012/06/18 06:29:07 udeshmuk Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.queues;

/**
 * Base class for the Reader Context for a specific input of an operator
 */
public class QueueReaderContext
{
  // The input number of this reader for the "reader" operator
  private int inputNo;
  
  //snapshotId associated with the readerQueue
  //Only inputs with a greater snapshotId would be read by the queue
  //Queue element snapshot ids would always be >= 0.
  //So default value of -1 here means all would pass.
  private long snapshotId = -1;

  // CONSTRUCTOR
  public QueueReaderContext(int inputNo)
  {
    this.inputNo = inputNo;
    this.snapshotId = -1;
  }
  
  //setters
  public void setSnapshotId(long sid)
  {
    this.snapshotId = sid;
  }

  // GETTERS
  public int getInputNo()
  {
    return inputNo;
  }
  
  public long getSnapshotId()
  {
    return snapshotId;
  }
}
