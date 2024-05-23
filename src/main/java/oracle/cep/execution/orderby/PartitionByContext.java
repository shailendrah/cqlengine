/* $Header: pcbpel/cep/server/src/oracle/cep/execution/orderby/PartitionByContext.java /main/2 2009/03/31 02:50:09 sbishnoi Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    03/19/09 - creating backupQueue only if input is relation
    sbishnoi    03/10/09 - Creation
 */

package oracle.cep.execution.orderby;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.PriorityQueue;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.comparator.TupleComparator;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/orderby/PartitionByContext.java /main/2 2009/03/31 02:50:09 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */


public class PartitionByContext implements Externalizable
{
  /** final queue will contain top n elements*/ 
  private PriorityQueue<ITuplePtr> finalQueue;
  
  /** backUp queue will keep remaining relation*/
  private PriorityQueue<ITuplePtr> backUpQueue;

  /**
   * The empty public constructor is needed for De-Serialization
   */
  public PartitionByContext()
  {
      super();
  }
  
  /**
   * Construct PartitionByContext object by initializing two queues
   * with given respective comparators
   * @param finalQueueTupleComparator
   * @param backUpQueueTupleComparator
   */
  public PartitionByContext(TupleComparator finalQueueTupComparator,
                            TupleComparator backupQueueTupComparator,
                            int numOrderByRows,
                            boolean isInputStream)
  {
    // tuple comparator for both queues must be initialized
    assert finalQueueTupComparator != null;
    assert backupQueueTupComparator != null;
    
    // Although we Initialize orderedQueue with initial capacity equal to
    // (numOrderByRows + 1); Extra ONE is the new tuples which will be
    // inserted at the time of arrival to compare with existing top tuples
    // Although PQueue is a growing data structure but finalQueue will never
    // grow larger than numOrderByRows
    finalQueue = new PriorityQueue<ITuplePtr>
         ((new Long(numOrderByRows + 1)).intValue(), 
          finalQueueTupComparator);
    
    // Initialize orderedQueue with initial capacity equal to 
    // ONE; It can grow as per the window size
    // Condition:
    //  If input is a stream; then there is no need to save the tuples in
    //  backup queue
    if(!isInputStream)
      backUpQueue = new PriorityQueue<ITuplePtr>(1, backupQueueTupComparator);
  }
  
  /**
   * Set Final Queue
   * @param paramQueue parameter queue
   */
  public void setFinalQueue(PriorityQueue<ITuplePtr> paramQueue)
  {
    finalQueue = paramQueue;
  }
  
  /**
   * Set Backup Queue
   * @param paramQueue parameter
   */
  public void setBackUpQueue(PriorityQueue<ITuplePtr> paramQueue)
  {
    backUpQueue = paramQueue; 
  }
  
  /**
   * Get final Queue
   * @return an instance of java.util.PriorityQueue
   */
  public PriorityQueue<ITuplePtr> getFinalQueue()
  {
    return finalQueue; 
  }
  
  /**
   * Get Backup Queue
   * @return an instance of java.util.PriorityQueue
   */
  public PriorityQueue<ITuplePtr> getBackUpQueue()
  {
    return backUpQueue;
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    if (finalQueue == null)
        out.writeBoolean(true);
    else
    {
        out.writeBoolean(false);
        out.writeObject(finalQueue);
    }

    if (backUpQueue == null)
        out.writeBoolean(true);
    else
    {
        out.writeBoolean(false);
        out.writeObject(backUpQueue);
    }
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException
  {
    boolean isQNull = in.readBoolean();
    if (!isQNull)
    {
        finalQueue = (PriorityQueue<ITuplePtr>) in.readObject();
    }

    isQNull = in.readBoolean();
    if (!isQNull)
    {
        backUpQueue = (PriorityQueue<ITuplePtr>) in.readObject();
    }
  }
}
