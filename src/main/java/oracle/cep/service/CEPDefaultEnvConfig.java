/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/service/CEPDefaultEnvConfig.java /main/15 2013/07/25 08:36:50 udeshmuk Exp $ */

/* Copyright (c) 2008, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    Environments overrides the server configuration by providing an implementation of CEPDefaultEnvConfig.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    07/23/13 - bug 16813624: introduce useMillisTs as config param
                           so that system timestamped sources use
                           System.currentTimeMillis()
    sbishnoi    10/09/12 - XbranchMerge
                           sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0_11.1.1.7.0
                           from st_pcbpel_11.1.1.4.0
    udeshmuk    07/16/12 - add params for setting the DO name corresponding to
                           BEAM_TRANSACTION_CONTEXT and for transaction_cid and
                           transaction_tid columns
    udeshmuk    06/07/12 - add is jdbc test property
    sbishnoi    04/04/12 - use the config parameter to get the target sql type
    sbishnoi    04/26/11 - adding archiver finder
    sborah      07/18/10 - XbranchMerge sborah_bug-9536720_ps3_11.1.1.4.0 from
                           st_pcbpel_11.1.1.4.0
    sbishnoi    10/08/12 - XbranchMerge sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0
                           from st_pcbpel_pt-11.1.1.7.0
    sbishnoi    09/14/12 - adding config parameter for isStatsEnabled check
    alealves    01/27/12 - XbranchMerge
                           alealves_bug-13347280_cep_201112222259_git from main
    alealves    12/20/11 - XbranchMerge alealves_bug-12873645_cep from main
    alealves    08/04/11 - XbranchMerge alealves_bug-12791498_cep from main
    sborah      07/17/10 - XbranchMerge sborah_bug-9536720_ps3 from main
    sbishnoi    06/23/10 - adding configuration parameter for external rows
                           threshold
    hopark      04/21/09 - add LoggerFactory
    hopark      02/02/09 - add IQueryDestLocator 
    hopark      12/12/08 - fix schedRunTime default
    hopark      12/03/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/service/CEPDefaultEnvConfig.java /main/15 2013/07/25 08:36:50 udeshmuk Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.service;

import java.util.TimeZone;
import oracle.cep.common.Constants;
import oracle.cep.common.SQLType;
import oracle.cep.logging.ILoggerFactory;
import oracle.cep.logging.impl.JavaLoggerFactory;

public abstract class CEPDefaultEnvConfig implements IEnvConfig
{
  // Datasource
  public abstract IDataSourceFinder getDataSourceFinder();
  public abstract IQueryDestLocator getQueryDestLocator();
  public IArchiverFinder   getArchiverFinder() {return null;}
  public IFaultHandler getFaultHandler() {return null;}
  public IUserFunctionLocator getUserFunctionLocator() {return null;}
  
  public ILoggerFactory getLoggerFactory()
  {
    return new JavaLoggerFactory();
  }

  // Storage
  public abstract String getStorageFolder();
  public String getMetadataStorageName() {return Constants.DEFAULT_METADATA_STORAGE_NAME;}
  public String getSpillStorageName() {return Constants.DEFAULT_SPILL_STORAGE_NAME;}
  public long getStorageCacheSize() {return Constants.DEFAULT_STORAGE_CACHESIZE;}
  public boolean getIsMetadataCleanupOnStartup() {return  Constants.DEFAULT_IS_METADATA_CLEANUP_ON_STARTUP;}

  // Scheduler
  public String getSchedulerClassName() {return Constants.DEFAULT_SCHED_NAME;}
  public long getSchedRuntime() {return 0L;}
  public int getSchedNumThreads() {return Constants.DEFAULT_SCHED_NUM_THREADS;}
  public int getSchedThreadPoolQSize() {return  Constants.DEFAULT_SCHED_NUM_THREADS * 2;}
  public int getSchedTimeSlice() {return Constants.DEFAULT_SCHED_TIME_SLICE;}
  public boolean getSchedOnNewThread() {return Constants.DEFAULT_SCHED_ON_NEW_THREAD;}
  public boolean getDirectInterop() {return Constants.DEFAULT_DIRECT_INTEROP;}
  
  // Date
  public String getDateFormat() {return "MM/dd/yyyy HH:mm:ss";}
  public String getTimeZone() {return TimeZone.getDefault().getID();}
  
  // Spill
  public boolean getUseSpilledQueueSrc() {return Constants.DEFAULT_SPILL_QUEUESRC;}
  public int getQueueSrcSpillNormalThreshold() {return Constants.DEFAULT_SPILL_QUEUESRC_NORMAL_THRESHOLD; }
  public int getQueueSrcSpillPartialThreshold() {return Constants.DEFAULT_SPILL_QUEUESRC_PARTIAL_THRESHOLD; } 
  public int getQueueSrcSpillFullThreshold() {return  Constants.DEFAULT_SPILL_QUEUESRC_FULL_THRESHOLD; }
  public int getQueueSrcSpillSyncThreshold() {return Constants.DEFAULT_SPILL_QUEUESRC_SYNC_THRESHOLD; } 
  
  // Trace
  public boolean getUseLogXMLTag() {return Constants.DEFAULT_LOG_USE_XMLTAG;}
  public String getTraceFolder() {return Constants.DEFAULT_LOG_TRACE_FOLDER;}
  public String getTracePostfix() {return Constants.DEFAULT_LOG_TRACE_FILE_POSTFIX;}
  
  // Misc
  public long getExternalRowsThreshold(){ return Constants.DEFAULT_EXTERNAL_ROWS_THRESHOLD;}
  public int getDegreeOfParallelism() {return Constants.DEFAULT_DEGREE_OF_PARALLELISM;}
  
  // Target SQL Type for Archiver Query
  public String getTargetSQLType() {return Constants.DEFAULT_TARGET_SQL_TYPE.toString();}
  
  // Whether system ts sources should use millis or nanos
  public boolean getUseMillisTs() { return Constants.DEFAULT_USE_MILLIS_TS;}

  //IsJDBCTest
  public boolean getIsJDBCTest() { return false; }
  
  //related to BEAM_TRANSACTION_CONTEXT table
  public String getBeamTxnCtxName() {return Constants.DEFAULT_BEAM_TXN_CTX_NAME;}
  public String getContextColName() {return Constants.DEFAULT_CONTEXT_COL_NAME;}
  public String getTxnColName() {return Constants.DEFAULT_TXN_COL_NAME;}
  // Statistics
  public boolean getIsStatsEnabled() { return Constants.DEFAULT_STATS_ENABLED;}
}
