/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/statistics/iterator/QueryStatsIterator.java /main/6 2013/10/08 10:15:01 udeshmuk Exp $ */

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
    parujain    01/29/09 - transaction mgmt
    parujain    12/08/08 - query stats iterator
    parujain    12/08/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/statistics/iterator/QueryStatsIterator.java /main/6 2013/10/08 10:15:01 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.statistics.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.ExecStats;
import oracle.cep.jmx.stats.QueryStatsRow;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.ArchiverStats;
import oracle.cep.metadata.MetadataStats;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.planmgr.PlanManager;
import oracle.cep.service.ExecContext;
import oracle.cep.transaction.ITransaction;
import oracle.cep.statistics.IStats;

public class QueryStatsIterator extends StatsIterator
{
  Iterator<Integer> iter;
  long numExec;
  long time;

  public QueryStatsIterator(ExecContext ec)
  {
    super(ec);
  }

  /**
   * Get the execution statistics of given query
   * @param schema
   * @param queryId
   * @return
   */
  public IStats getQueryStats(String schema, String queryId)
  {
    ExecOpt rootOp = null;
    IStats queryStat = null;

    if(factory == null)
      return null;

    ITransaction txn = execContext.getTransactionMgr().begin();
    execContext.setTransaction(txn);
    PlanManager planMgr = execContext.getPlanMgr();
   
    int qryId;
    String serviceSchema = execContext.getServiceSchema(schema);
    try
    {
      qryId = execContext.getQueryMgr().findQuery(queryId, serviceSchema);
    
      PhyOpt opt = planMgr.getQueryRootOpt(qryId);
      if(opt != null)
        rootOp = opt.getInstOp();

      if(rootOp == null)
        return null;

      queryStat =  getQueryStat(qryId, opt, rootOp);
    }
    catch(CEPException e)
    {
      LogUtil.info(LoggerType.TRACE, "QueryStatsIterator can't Find Query Root for queryId" + queryId 
        +" schema:" + serviceSchema + " Reason:" + e.getMessage());
    }
    finally
    {
      execContext.getTransactionMgr().commit(txn);
      execContext.setTransaction(null);
    }
    return queryStat;
  }
  
  public IStats getNext() throws CEPException {

    ExecOpt rootOp;
    boolean found = false;
    IStats queryStat = null;

    if(factory == null)
      return null;

    ITransaction txn = execContext.getTransactionMgr().begin();
    execContext.setTransaction(txn);
    PlanManager planMgr = execContext.getPlanMgr();
    while(!found)
    {
      rootOp = null;
      int id;

      if(iter.hasNext())
        id = iter.next().intValue();
      else
      {
        execContext.getTransactionMgr().commit(txn);
        execContext.setTransaction(null);
        return null;
      }

      PhyOpt opt = planMgr.getQueryRootOpt(id);
      if(opt != null)
        rootOp = opt.getInstOp();

      if(rootOp == null)
        continue;

      queryStat =  getQueryStat(id, opt, rootOp);

      found = true;
    }
    execContext.getTransactionMgr().commit(txn);
    execContext.setTransaction(null);
    return queryStat;
  }

  /**
   * Get Stats for a given operator.
   * @param id
   * @param opt
   * @param rootOp
   * @return
   */
  private IStats getQueryStat(int id, PhyOpt opt, ExecOpt rootOp)
  {
      MetadataStats stat = execContext.getQueryMgr().getQueryStats(id);
      List<PhyOpt> outputs = execContext.getPlanMgr().getAllQueryOutputs(id);
      numExec = rootOp.getStats().getNumExecutions();
      time = rootOp.getStats().getTotalTime();
      /*
      float avg = (float)0.0;
      
      if(outputs != null)
      {
        getNumExecutionsAndTime(opt);
        addOutputsExecutionsAndTime(outputs);
        AtomicLong latency = new AtomicLong(0);
        //getTotalLatency(outputs, latency);
        
        if(outputs.size() > 0)
          avg = ((float)latency.longValue()/(float)outputs.size());
      }

      float percent = 0;
      if(ExecStats.getRunningTime() > 0)
       percent = ((float)time/(float)ExecStats.getRunningTime())*100;

      if(archiverStats.isInitialized())
      {
        return factory.createArchiverBasedQueryStat(id, 
          stat.getText(),
          stat.getName(),
          stat.getIsMetadata(),
          stat.isInternal(),
          rootOp.getStats().getNumOutputs(),
          rootOp.getStats().getStartTime(),
          rootOp.getStats().getEndTime(),
          rootOp.getStats().getNumOutputsLatest(),
          numExec,
          time,
          avg,
          percent,
          opt.getOrderingConstraint().name(),
          archiverStats.getQueryOpsIdentificationAndConstructionTime(),
          archiverStats.getArchiverQueryExecTime(),
          archiverStats.getConversionOfResultSetToTuplesTime(),
          archiverStats.getSnapshotPropagationTime(),
          archiverStats.getTotalStartTime(),
          archiverStats.getNumRecordsReturned());
      }
      else
      {*/
        return factory.createQueryStat(id, 
          stat.getText(), 
          stat.getName(),
          stat.getIsMetadata(),
          stat.isInternal(),
          rootOp.getStats().getNumOutputs(),
          rootOp.getStats().getNumOutputHeartbeats(),
          rootOp.getStats().getStartTime(),
          rootOp.getStats().getEndTime(),
          rootOp.getStats().getNumOutputsLatest(),
          numExec,
          time,
          0.0f,
          0, 
          opt.getOrderingConstraint().name());
      //}
  }

  private void getNumExecutionsAndTime(PhyOpt op)
  {
    PhyOpt[] opts = op.getInputs();
    for(int i=0; i<op.getNumInputs(); i++)
    {
      numExec += opts[i].getInstOp().getStats().getNumExecutions();
      time += opts[i].getInstOp().getStats().getTotalTime();
      getNumExecutionsAndTime(opts[i]);
    }
  }

  private void addOutputsExecutionsAndTime(List<PhyOpt> outputs)
  {
    if(outputs.isEmpty())
      return;

    Iterator<PhyOpt> iterator = outputs.iterator();
    while(iterator.hasNext())
    {
      PhyOpt opt = iterator.next();
      numExec += opt.getInstOp().getStats().getNumExecutions();
      time += opt.getInstOp().getStats().getTotalTime();
    }
  }

  private void getTotalLatency(List<PhyOpt> outputs, AtomicLong latency)
  {
    if(outputs.isEmpty())
      return;

    Iterator<PhyOpt> iterator = outputs.iterator();
    while(iterator.hasNext())
    {
      PhyOpt opt = iterator.next();
      if(opt.getInstOp().getIsStatsEnabled())
        latency.addAndGet(opt.getInstOp().getStats().getLatency());
    }
  }

  public void init()
  {
    ArrayList<Integer> qryIdList = execContext.getPlanMgr().getRootQueryIds();
    iter = qryIdList.iterator();
    numExec = 0;
    time = 0;
  }

  public void close() {
    iter = null;
  }

}

