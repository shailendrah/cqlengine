/* $Header: pcbpel/cep/server/src/oracle/cep/statistics/iterator/WriterQueueStatsIterator.java /main/3 2009/05/12 19:25:47 parujain Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    05/04/09 - lifecycle mgmt
    parujain    04/21/09 - modified list
    parujain    01/29/09 - transaction mgmt
    parujain    12/08/08 - writer queue stats iterator
    parujain    12/08/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/statistics/iterator/WriterQueueStatsIterator.java /main/3 2009/05/12 19:25:47 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.statistics.iterator;

import java.util.Iterator;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.queues.ISharedQueueWriter;
import oracle.cep.execution.queues.SharedQueueWriterStats;
import oracle.cep.phyplan.PhyQueue;
import oracle.cep.statistics.IStats;
import oracle.cep.service.ExecContext;

public class WriterQueueStatsIterator extends StatsIterator
{
  // iterator to traverse the shared queue writer array
  private Iterator<PhyQueue> itr;

  // Loop over all the execution operators and return the statistics
  // corresponding to them. Note that we deliberately do not take a latch
  // while traversing the operators. So, it is possible that we miss an
  // operator if it gets added in the beginning of the operator array after
  // we have traversed the initial segment of the array. Also, it is possible
  // that the different operators are showing statistics for different
  // timestamps
  public WriterQueueStatsIterator(ExecContext ec)
  {
    super(ec);
  }

  public void init()
  {
    itr = execContext.getPlanMgr().getSharedQueueWriterIterator();
  }

  public IStats getNext() throws CEPException
  {
    if(factory == null)
      return null;

    if (itr == null)
      return null;

    ISharedQueueWriter qWriter = null;

    if (itr.hasNext())
      qWriter = (ISharedQueueWriter)itr.next().getInstQueue();

    if (qWriter == null)
      return null;

    IStats qWriterStat = null;

    synchronized (qWriter)
    {
      SharedQueueWriterStats qWriterStats = (SharedQueueWriterStats) qWriter.populateAndGetStats();

         // Note that since we dont know when the memory for this row will get
         // freed, we haven't integrated this with the StorageFactory. Once the
         // JMX piece gets finalized and we have more clarity on this, we should
         // integrate this with the SimpleStorageFactory.
      qWriterStat =
          factory.createWriterQueueStat(qWriter.getId(),
                              qWriter.getSrcOp().getId(),
                              qWriterStats.getNumElements(),
                              qWriterStats.getNumPosElements(),
                              qWriterStats.getNumNegElements(),
                              qWriterStats.getNumHeartbeats(),
                              qWriterStats.getTotalNumElements(),
                              qWriterStats.getTotalNumPosElements(),
                              qWriterStats.getTotalNumNegElements(),
                              qWriterStats.getTotalNumHeartbeats(),
                              qWriterStats.getTsLastElement(),
                              qWriterStats.getTsLastPosElement(),
                              qWriterStats.getTsLastNegElement(),
                              qWriterStats.getTsLastHeartbeat());
     }

     return qWriterStat;
  }

  public void close()
  {
    itr = null;
  }

}

