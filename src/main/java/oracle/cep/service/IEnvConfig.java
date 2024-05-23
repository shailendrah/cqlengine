/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/service/IEnvConfig.java /main/17 2013/07/25 08:36:50 udeshmuk Exp $ */

/* Copyright (c) 2008, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    Environments overrides the server configuration by providing an implementation of IEnvConfig.

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
    udeshmuk    06/07/12 - add isJDBCtest property
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
    sbishnoi    06/23/10 - adding configuration parameter for externalrows
                           threshold
    hopark      04/21/09 - add LoggerFactory
    hopark      01/26/09 - remove setTracePostfix
    hopark      01/28/09 - add getOutputDestLocator
    hopark      12/03/08 - add more configurations
    hopark      09/26/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/service/IEnvConfig.java /main/17 2013/07/25 08:36:50 udeshmuk Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.service;

import oracle.cep.common.SQLType;
import oracle.cep.logging.ILoggerFactory;


public interface IEnvConfig 
{
  IDataSourceFinder getDataSourceFinder();
  IQueryDestLocator getQueryDestLocator();
  ICartridgeLocator getCartridgeLocator();
  IArchiverFinder   getArchiverFinder();
  ILoggerFactory    getLoggerFactory();
  IFaultHandler     getFaultHandler();
  IUserFunctionLocator getUserFunctionLocator();
  
  // Storage
  String getStorageFolder();
  String getMetadataStorageName();
  String getSpillStorageName();
  long getStorageCacheSize();
  boolean getIsMetadataCleanupOnStartup();

  // Scheduler
  String getSchedulerClassName() ;
  long getSchedRuntime();
  int getSchedNumThreads();
  int getSchedThreadPoolQSize();
  int getSchedTimeSlice();
  boolean getSchedOnNewThread();
  boolean getDirectInterop();
  
  // Date
  String getDateFormat();
  String getTimeZone();
  
  // Spill
  boolean getUseSpilledQueueSrc();
  int getQueueSrcSpillNormalThreshold();
  int getQueueSrcSpillPartialThreshold();
  int getQueueSrcSpillFullThreshold();
  int getQueueSrcSpillSyncThreshold();
  
  // Trace
  boolean getUseLogXMLTag();
  String getTraceFolder();
  
  // Misc
  long getExternalRowsThreshold();
  int getDegreeOfParallelism();
  
  // Target SQL Type for archiver query
  String getTargetSQLType();
  
  // System timestamped sources should use System.currentTimeMillis
  // if this returns true.
  boolean getUseMillisTs();

  //isjdbc test
  boolean getIsJDBCTest();
  
  //related to BEAM_TRANSACTION_CONTEXT
  String getBeamTxnCtxName();
  String getContextColName();
  String getTxnColName();
  // Statistics
  boolean getIsStatsEnabled();
  
}
