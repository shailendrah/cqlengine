/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/statistics/ExecStatsManager.java /main/7 2013/10/08 10:15:00 udeshmuk Exp $ */

/* Copyright (c) 2007, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    08/01/13 - add getter for CEPStats mbean
    sbishnoi    07/09/13 - enable jmx framework
    sbishnoi    10/09/12 - XbranchMerge
                           sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0_11.1.1.7.0
                           from st_pcbpel_11.1.1.4.0
    sbishnoi    10/08/12 - XbranchMerge sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0
                           from st_pcbpel_pt-11.1.1.7.0
    sbishnoi    09/14/12 - adding a flag to check if stats is enabled for run
                           time operator statistics
    parujain    12/08/08 - stats cleanup
    hopark      10/07/08 - use execContext to remove statics
    parujain    07/16/08 - jar reorg
    parujain    09/12/07 - cep-em integration
    parujain    05/02/07 - Global execution stats manager
    parujain    05/02/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/statistics/ExecStatsManager.java /main/7 2013/10/08 10:15:00 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.statistics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import oracle.cep.jmx.CEPStats;
import oracle.cep.jmx.CEPStatsController;
import oracle.cep.jmx.stats.QueryStatsRow;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.service.ExecContext;

public class ExecStatsManager {
  
  private HashMap<Integer, UserFuncStats> funcStats;
  
  /** a flag to check if the operator stats should be collected or not */
  private boolean isRunTimeOperatorStatsEnabled;
  
  private CEPStatsController cepJmxStatsController;
  
  public ExecStatsManager(ExecContext ec)
  {
    funcStats = new HashMap<Integer, UserFuncStats>();
    ConfigManager confMgr = ec.getServiceManager().getConfigMgr();
    isRunTimeOperatorStatsEnabled = confMgr.isStatsEnabled();
    if(isRunTimeOperatorStatsEnabled)
    {
      LogUtil.info(LoggerType.TRACE, "Initializing ExecStatsManager");
      cepJmxStatsController = new CEPStatsController(ec);
    }
  }
  
  public Iterator<Integer> getFunctionKeysIterator()
  {
    return funcStats.keySet().iterator();
  }
  
  public synchronized void getFunctionStats(
    int id, 
    oracle.cep.statistics.iterator.UserFuncStats fnStat)
  {
    Integer key = new Integer(id);
    UserFuncStats stat = funcStats.get(key);
    if(stat != null)
    {
      fnStat.setNumInvokations(stat.getNumInvokations());
      fnStat.setTime(stat.getTime());
    }
    else
    {
      fnStat.setNumInvokations(0);
      fnStat.setTime(0);
    }
  }
  
  public synchronized void incrUserFuncStats(int id, long t)
  {
    Integer key = new Integer(id);
    UserFuncStats stat = funcStats.get(key);
    if(stat != null)
    {
      stat.incrNumInvokations();
      stat.incrTime(t);
    }
    else
    {
      funcStats.put(key, new UserFuncStats(t));
    }
  }
  
  public synchronized void removeFuncStats(int id)
  {
    Integer key = new Integer(id);
    if(funcStats.get(key) != null)
      funcStats.remove(key);
  }

  /**
   * @return the isRunTimeOperatorStatsEnabled
   */
  public boolean isRunTimeOperatorStatsEnabled()
  {
    return isRunTimeOperatorStatsEnabled;
  }

  /**
   * @param isRunTimeOperatorStatsEnabled the isRunTimeOperatorStatsEnabled 
   * to set
   */
  public void setRunTimeOperatorStatsEnabled(
    boolean isRunTimeOperatorStatsEnabled)
  {
    this.isRunTimeOperatorStatsEnabled = isRunTimeOperatorStatsEnabled;
  }

  public CEPStats getCEPStatsMBean()
  {
    if(cepJmxStatsController != null)
      return cepJmxStatsController.getCEPStatsMBean();
    else
      return null;
  }
  
  /**
   * Get Detailed Execution Statistics of Given Query
   * @param schema Name of Schema
   * @param queryId Query Name
   * @return A Map of Statistics property and values
   */
  public Map<String, Object> getQueryStats(String schema, String queryId)
  {
    HashMap<String,Object> qryStats = new HashMap<String,Object>();
    if(cepJmxStatsController != null)
    {
      CEPStats stats  = cepJmxStatsController.getCEPStatsMBean();
      
      // Obtain query stats
      QueryStatsRow statsRow = stats.getQueryStats(schema, queryId);
      
      // Obtain opertor stats
      Map<String,Object> opStatsRow = stats.getOperatorStatus(schema,queryId);
      
      // Merge result of query stats and operator stats into single map
      if(statsRow != null)
      {
        qryStats.put("num.outputs", statsRow.getNumOutMessages());
        qryStats.put("num.output.heartbeats", statsRow.getNumOutHeartbeats());        
      }
      if(opStatsRow != null)
      {
        qryStats.put("operator.list", opStatsRow.get("operator.list"));
        qryStats.put("operator.stats", opStatsRow.get("operator.stats"));
        qryStats.put("operator.parents", opStatsRow.get("operator.parents"));
        qryStats.put("operator.types", opStatsRow.get("operator.types"));
      }
    }
    return qryStats;
  }
} 
