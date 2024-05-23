/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/statistics/iterator/StreamStatsIterator.java /main/3 2013/10/08 10:15:01 udeshmuk Exp $ */

/* Copyright (c) 2008, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    07/10/13 - saving statistics in stat variable
    parujain    01/29/09 - transaction mgmt
    parujain    12/08/08 - stream stats iterator
    parujain    12/08/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/statistics/iterator/StreamStatsIterator.java /main/3 2013/10/08 10:15:01 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.statistics.iterator;

import java.util.Iterator;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.ExecStats;
import oracle.cep.interfaces.input.QueueSourceStat;
import oracle.cep.metadata.MetadataStats;
import oracle.cep.metadata.TableManager;
import oracle.cep.service.ExecContext;
import oracle.cep.transaction.ITransaction;
import oracle.cep.statistics.IStats;

public class StreamStatsIterator extends StatsIterator
{
  //iterator to traverse the source(Stream/Relation) operator array
  private Iterator<ExecOpt> itr;

  //Loop over all the Source execution operators and return the statistics
  // corresponding to them. Note that we deliberately do not take a latch
  // while traversing the operators. So, it is possible that we miss an
  // operator if it gets added in the beginning of the source array after
  // we have traversed the initial segment of the array. Also, it is possible
  // that the different source operators are showing statistics for different
  // timestamps
  public StreamStatsIterator(ExecContext ec)
  {
    super(ec);
  }

  public void close() {
    itr = null;
  }

  public IStats getNext()  throws CEPException {
    if(factory == null)
      return null;

    if(itr == null)
      return null;

    ExecOpt sourceOp = null;

    IStats stats = null;
    
    ITransaction txn = execContext.getTransactionMgr().begin();
    execContext.setTransaction(txn);

    TableManager tblMgr = execContext.getTableMgr();

    if(!itr.hasNext())
      return null;

     // This needs to be modified
     float hitratio = (float)0.0;

     sourceOp = itr.next();

     MetadataStats tableStats = tblMgr.getTableStats(sourceOp.getStreamId());
     QueueSourceStat qsrcstat = tblMgr.getQueueSourceStat(sourceOp.getStreamId());
     boolean isArchived = tblMgr.isArchived(sourceOp.getStreamId());
     
     float avg = (float)0.0;
     if(sourceOp.getStats().getNumInputs() > 0)
        avg = ((float)sourceOp.getStats().getLatency()/(float)sourceOp.getStats().getNumInputs());

     long totalTime = sourceOp.getStats().getTotalTime();
     long totalTimeInSeconds = totalTime / 1000000000l;
     float rate = (float)0.0;
     if(totalTimeInSeconds > 0)
       rate = (((float)sourceOp.getStats().getNumInputs()/totalTimeInSeconds));
     
     float percent = (float)0.0;
     if(ExecStats.getRunningTime() >0)
       percent = (((float)sourceOp.getStats().getTotalTime()/(float)ExecStats.getRunningTime())*100);

     stats = factory.createStreamStat(sourceOp.getStreamId(),
                                   sourceOp.getId(),
                                   tableStats.getName(),
                                   tableStats.getIsMetadata(),
                                   tableStats.getText(),
                                   sourceOp.getStats().getNumInputs(),
                                   sourceOp.getStats().getStartTime(),
                                   sourceOp.getStats().getEndTime(),
                                   sourceOp.getStats().getNumInputsLatest(),
                                   avg,
                                   rate,
                                   percent,
                                   tableStats.getIsPushSrc(),
                                   qsrcstat.getTuplesInMem(),
                                   qsrcstat.getTuplesInDisk(),
                                   hitratio,
                                   isArchived,
                                   totalTimeInSeconds);
     
     execContext.getTransactionMgr().commit(txn);
     execContext.setTransaction(null);

     return stats;
  }

  public void init()
  {
   itr = execContext.getExecMgr().getSourceOpIterator();
  }

}

