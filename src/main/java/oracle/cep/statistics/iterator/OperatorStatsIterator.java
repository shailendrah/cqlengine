/* $Header: pcbpel/cep/server/src/oracle/cep/statistics/iterator/OperatorStatsIterator.java /main/3 2009/02/06 15:51:04 parujain Exp $ */

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
    parujain    01/29/09 - transaction mgmt
    parujain    01/06/09 - pass execopid
    parujain    12/08/08 - operator stats iterator
    parujain    12/08/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/statistics/iterator/OperatorStatsIterator.java /main/3 2009/02/06 15:51:04 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.statistics.iterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.ExecStats;
import oracle.cep.jmx.stats.OperatorStatsRow;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.planmgr.PlanManager;
import oracle.cep.statistics.IStats;
import oracle.cep.service.ExecContext;
import oracle.cep.transaction.ITransaction;
import oracle.cep.util.DAGHelper;
import oracle.cep.util.DAGNode;


public class OperatorStatsIterator extends StatsIterator
{
  // iterator to traverse the operator array
  private Iterator<ExecOpt> itr;

  public OperatorStatsIterator(ExecContext ec)
  {
    super(ec);
  }

  public void init()
  {
    itr = execContext.getExecMgr().getExecOpIterator();
  }

  /**
   * Get the operator level statistics for the given query
   * @param schema
   * @param queryId
   * @return
   */
  public Map<String, Object> getOperatorStats(String schema, String queryId)
  {
    Map<String,Object> queryOpStats = null;
    List<String> opNames = new LinkedList<String>();
    HashMap<String,List<Object>> opStatMap = new HashMap<String,List<Object>>();
    Map<String,String> opTypeMap = new HashMap<String,String>();       
    HashMap<String,List<String>> opParentsMap = new HashMap<String,List<String>>();
    
    // Return null if stats factory isn't initialized
    if(factory == null)
      return queryOpStats;
    
    String serviceSchema = execContext.getServiceSchema(schema);
    ITransaction txn = execContext.getTransactionMgr().begin();
    execContext.setTransaction(txn);
    PlanManager planMgr = execContext.getPlanMgr();
    
    int qryId;
    try
    {
      // Find query id from query manager
      qryId = execContext.getQueryMgr().findQuery(queryId, serviceSchema);
    
      // Determine the root operator of query. Note that this won't be the Output operator
      PhyOpt root = planMgr.getQueryRootOpt(qryId);
      if (root == null) 
          return queryOpStats;
      
      // Traverse the query plan and prepare the statistics for each operator
      ArrayList<DAGNode> nodes = DAGHelper.getTopologicalSort(root);
      for (DAGNode op : nodes) 
      {
        PhyOpt opt = (PhyOpt) op;
        ExecOpt execOpt = opt.getInstOp();
        OperatorStatsRow opStats = (OperatorStatsRow) getOpStat(execOpt);
        List<Object> stats = new LinkedList<Object>();
        
        // The list should contain the operator stats in the following order:
        // [Input Events, Output Events, Input Hbts, Output Hbts, Throughput, Latency]
        
        stats.add(opStats.getNumInMessages());
        stats.add(opStats.getNumOutMessages());
        stats.add(opStats.getNumInHbts());
        stats.add(opStats.getNumOutHbts());
        long numOutMsgs = opStats.getNumOutMessages();
        long totalTimeMicros = opStats.getTotalTime()/1000L;
        long pstmtTTime = opStats.getPstmtTTime();
        long pstmtTExecs = opStats.getPstmtTExec();
        
        // Compute Throughput of Operator in the unit of (events per second)
        if(totalTimeMicros != 0)
          stats.add((numOutMsgs*1000000L)/totalTimeMicros);
        else
          stats.add(Long.MIN_VALUE);
        
        // Compute Latency of operator in unit of microsecond
        if(numOutMsgs != 0)
          stats.add(totalTimeMicros/numOutMsgs);
        else
          stats.add(Long.MIN_VALUE);

        stats.add(opStats.isSrcCached());
        stats.add(opStats.getCacheName());
        stats.add(opStats.getCacheHit());
        stats.add(opStats.getCacheMiss());
        if(pstmtTExecs != 0)
            stats.add(pstmtTTime/pstmtTExecs);
        else
        	stats.add(0L);
        stats.add(pstmtTExecs);
       

        
        // Determine parents of this operator
        PhyOpt[] inputs = opt.getInputs();
        List<String> parents = new ArrayList<String>();
        if(inputs != null)
        {
          for(PhyOpt inp: inputs)
            parents.add(inp.getInstOp().getOptName());
        }
        opParentsMap.put(execOpt.getOptName(), parents);

        opStatMap.put(execOpt.getOptName(), stats);
        opTypeMap.put(execOpt.getOptName(),execOpt.getOpttyp().toString());
        opNames.add(execOpt.getOptName());
        
      }
      queryOpStats = new HashMap<String,Object>();
      queryOpStats.put("operator.list", opNames); 
      queryOpStats.put("operator.types", opTypeMap); 
      queryOpStats.put("operator.stats", opStatMap);
      queryOpStats.put("operator.parents", opParentsMap);
    }
    catch(CEPException e)
    {
      LogUtil.info(LoggerType.TRACE, "OperatorStatsIterator can't Find Query Root for queryId" + queryId 
        +" schema:" + serviceSchema + " Reason:" + e.getMessage());
    }
    finally
    {
      execContext.getTransactionMgr().commit(txn);
      execContext.setTransaction(null);
    }
    return queryOpStats; 
  }
  
  public IStats getNext() throws CEPException
  {
    if(factory == null)
      return null;

    if (itr == null)
      return null;

    ExecOpt op = null;
    boolean found = false;
    IStats opStats = null;
    ITransaction txn = execContext.getTransactionMgr().begin();
    execContext.setTransaction(txn);

    while(!found && itr.hasNext())
    {

      op = itr.next();

      if (op == null)
        continue;

      opStats = getOpStat(op);

      found = true;
    }
    execContext.getTransactionMgr().commit(txn);
    execContext.setTransaction(null);
    if(found)
      return opStats;
    else
      return null;
  }

  public void close()
  {
    itr = null;
  }

  private IStats getOpStat(ExecOpt op)
  {
    int qid;
    if (op.getOutputQueue() == null)
      qid = -1;
    else
      qid = op.getOutputQueue().getId();

    float percent = 0;
    if(ExecStats.getRunningTime() >0)
      percent = (((float)op.getStats().getTotalTime()/(float)ExecStats.getRunningTime())*100);

     return factory.createOperatorStat(op.getId(), op.getPhyOptId(), qid,
                                  op.getStats().getNumOutputs(),
                                  op.getStats().getNumInputs(),
                                  op.getStats().getNumOutputHeartbeats(),
                                  op.getStats().getNumInputHeartbeats(),
                                  op.getStats().getNumExecutions(),
                                  op.getStats().getTotalTime(),
                                  op.getStats().getStartTime(),
                                  op.getStats().getEndTime(),
                                  op.getStats().getNumInputsLatest(),
                                  op.getStats().getNumOutputsLatest(),
                                  op.getOpttyp().toString(),
                                  op.getOptName(),
                                  percent,
                                  op.getStats().getCacheMisses(),
                                  op.getStats().getCacheHits(),
                                  op.getStats().getCacheName(),
                                  op.getStats().isSrcCached(),
                                  op.getStats().getTotalPstmtRunTime(),
                                  op.getStats().getTotalPstmtExec());
  }

}
