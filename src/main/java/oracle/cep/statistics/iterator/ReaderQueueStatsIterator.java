/* $Header: pcbpel/cep/server/src/oracle/cep/statistics/iterator/ReaderQueueStatsIterator.java /main/3 2009/05/12 19:25:47 parujain Exp $ */

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
    parujain    12/08/08 - reader queue stats iterator
    parujain    12/08/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/statistics/iterator/ReaderQueueStatsIterator.java /main/3 2009/05/12 19:25:47 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.statistics.iterator;

import java.util.Iterator;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.queues.ISharedQueueReader;
import oracle.cep.execution.queues.ISharedQueueWriter;
import oracle.cep.execution.queues.SharedQueueReaderStats;
import oracle.cep.execution.queues.SharedQueueWriterStats;
import oracle.cep.phyplan.PhyQueue;
import oracle.cep.statistics.IStats;
import oracle.cep.service.ExecContext;


public class ReaderQueueStatsIterator extends StatsIterator
{
   // iterator to traverse the shared queue reader array
  private Iterator<PhyQueue> itr;

  // Loop over all the execution operators and return the statistics
  // corresponding to them. Note that we deliberately do not take a latch
  // while traversing the operators. So, it is possible that we miss an
  // operator if it gets added in the beginning of the operator array after
  // we have traversed the initial segment of the array. Also, it is possible
  // that the different operators are showing statistics for different
  // timestamps
  public ReaderQueueStatsIterator(ExecContext ec)
  {
    super(ec);
  }

  public void init()
  {
    //itr = execContext.getExecMgr().getSharedQReaderIterator();
    itr = execContext.getPlanMgr().getSharedQueueReaderIterator();
  }

  public IStats getNext() throws CEPException
  {
    if(factory == null)
      return null;

    if (itr == null)
      return null;

    ISharedQueueReader qReader = null;

    if(itr.hasNext())
      qReader = (ISharedQueueReader)itr.next().getInstQueue();
      
    if (qReader == null)
      return null;

    ISharedQueueWriter writer = qReader.getWriter();
    IStats qReaderStat;

    synchronized (writer)
    {
       SharedQueueReaderStats qReaderStats = (SharedQueueReaderStats) qReader.getStats();
       SharedQueueWriterStats qWriterStats = (SharedQueueWriterStats) writer.getStats();

      // Note that since we dont know when the memory for this row will get
      // freed, we haven't integrated this with the StorageFactory. Once the
      // JMX piece gets finalized and we have more clarity on this, we should
      // integrate this with the SimpleStorageFactory.

       int numPresent = (qWriterStats.getTotalNumElements()
                      - qReaderStats.getTotalNumElements() - qReaderStats.getInitElements()
                      - qReaderStats.getNumOthers());
       int numPosPresent = (qWriterStats.getTotalNumPosElements()
                         - qReaderStats.getTotalNumPosElements() - qReaderStats.getInitPosElements()
                         - qReaderStats.getNumPosOthers());
       int numNegPresent = (qWriterStats.getTotalNumNegElements()
                          - qReaderStats.getTotalNumNegElements() 
                          - qReaderStats.getInitNegElements());
       int numHeartbeatsPresent = (qWriterStats.getTotalNumHeartbeats()
                            - qReaderStats.getTotalNumHeartbeats()
                            - qReaderStats.getInitHeartbeats());

       qReaderStat = factory.createReaderQueueStat(qReader.getId(),
                                qReader.getDestOp().getId(),
                                qReader.getReaderId(),
                                qReaderStats.getTotalNumElements(),
                                qReaderStats.getTotalNumPosElements(),
                                qReaderStats.getTotalNumNegElements(),
                                qReaderStats.getTotalNumHeartbeats(),
                                numPresent, numPosPresent,
                                numNegPresent,
                                numHeartbeatsPresent,
                                qReaderStats.getTsLastElement(),
                                qReaderStats.getTsLastPosElement(),
                                qReaderStats.getTsLastNegElement(),
                                qReaderStats.getTsLastHeartbeat());
      }

         return qReaderStat;
  }

  public void close()
  {
    itr = null;
  }

}

