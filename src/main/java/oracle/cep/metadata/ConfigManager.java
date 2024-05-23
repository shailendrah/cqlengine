/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/ConfigManager.java /main/54 2013/07/25 08:36:51 udeshmuk Exp $ */

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
    udeshmuk    07/23/13 - bug 16813624: introduce useMillisTs as config param
                           so that system timestamped sources use
                           System.currentTimeMillis()
    sbishnoi    10/09/12 - XbranchMerge
                           sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0_11.1.1.7.0
                           from st_pcbpel_11.1.1.4.0
    sbishnoi    10/08/12 - XbranchMerge sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0
                           from st_pcbpel_pt-11.1.1.7.0
    sbishnoi    09/14/12 - adding config parameter for isStatsEnabled check
    udeshmuk    07/17/12 - add params for setting the DO name corresponding to
                           BEAM_TRANSACTION_CONTEXT and for transaction_cid and
                           transaction_tid columns
    udeshmuk    06/07/12 - add isJDBCTest property
    sbishnoi    04/04/12 - adding config parameter for targetSQLType in context
                           of archived relations
    alealves    01/27/12 - XbranchMerge
                           alealves_bug-13347280_cep_201112222259_git from main
    alealves    12/20/11 - XbranchMerge alealves_bug-12873645_cep from main
    alealves    08/18/11 - XbranchMerge alealves_bug-12888416_cep from main
    anasrini    08/10/11 - support for statsEnabled
    alealves    08/04/11 - XbranchMerge alealves_bug-12791498_cep from main
    sbishnoi    04/26/11 - adding archiver finder
    apiper      04/15/11 - XbranchMerge apiper_bug-9883831_cep_201007081133_git
                           from main
    hopark      03/31/11 - expose cleanup metadata
    sborah      07/18/10 - XbranchMerge sborah_bug-9536720_ps3_11.1.1.4.0 from
                           st_pcbpel_11.1.1.4.0
    sborah      07/17/10 - XbranchMerge sborah_bug-9536720_ps3 from main
    sbishnoi    06/23/10 - adding configuration parameter for externalrows
                           threshold
    hopark      01/27/10 - move LoggerFactory
    hopark      04/21/09 - add LoggerFactory
    hopark      01/26/09 - remove setTracePostfix
    hopark      01/28/09 - add QueryDestLocator
    anasrini    02/12/09 - add REGRESS_PUSH_MODE
    alealves    01/30/09 - support for pluggability of user function object instances
    hopark      12/29/08 - fix modifieble config from envConfig
    hopark      12/11/08 - fix getStorageFolder is not picking up envConfig
    hopark      12/08/08 - add getSchedulerClass
    hopark      12/05/08 - add QueueSrcSpill config params
    hopark      12/02/08 - move LogLevelManager to ExecContext
    hopark      11/28/08 - add setTimeStampFormat
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    hopark      09/26/08 - add setEnvConfig
    hopark      09/22/08 - add setDataSourceFinder
    hopark      08/14/08 - remove obsolete metadat.store
    hopark      06/18/08 - logging refactor
    sbishnoi    06/10/08 - adding isMetadataCleanupOnStartup
    hopark      05/05/08 - remove FullSpillMode
    hopark      03/17/08 - use springbean for config
    hopark      03/12/08 - add getSpillQueueSrc
    hopark      03/08/08 - add getFullSpill
    hopark      02/04/08 - add getTracePostfix
    hopark      01/01/08 - add getTraceFolder
    hopark      12/20/07 - add xmltag flag for log
    hopark      10/31/07 - add usePagedList
    hopark      09/04/07 - add getBooleanProperty
    hopark      08/28/07 - use singleton LogLevelManager
    hopark      07/31/07 - add dynamic gen off
    najain      07/09/07 - remove SCHEDULER_THREADED
    hopark      06/07/07 - change getLogLevels api
    hopark      06/08/07 - fix null exception
    dlenkov     06/05/07 - config fix
    hopark      06/01/07 - logging support
    hopark      05/22/07 - debug logging
    dlenkov     05/22/07 - 
    hopark      05/11/07 - remove System.out.println(use java.util.logging instead)
    parujain    03/21/07 - Threaded Scheduler
    parujain    03/16/07 - BitSet for DebugLevel
    hopark      02/27/07 - handle all properties
    parujain    02/09/07 - Manager handling system configuration
    parujain    02/09/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/ConfigManager.java /main/54 2013/07/25 08:36:51 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.TimeZone;

import com.oracle.cep.common.util.SecureFile;
import oracle.cep.common.CEPDateFormat;
import oracle.cep.common.Constants;
import oracle.cep.common.SQLType;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.execution.scheduler.Scheduler;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILoggerFactory;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.service.IArchiverFinder;
import oracle.cep.service.ICartridgeLocator;
import oracle.cep.service.IDataSourceFinder;
import oracle.cep.service.IEnvConfig;
import oracle.cep.service.CEPManager;
import oracle.cep.service.IFaultHandler;
import oracle.cep.service.IUserFunctionLocator;
import oracle.cep.service.IQueryDestLocator;
import oracle.cep.util.StringUtil;

@DumpDesc(autoFields=true)
public class ConfigManager 
{
  String homeFolder = null;
  String schedulerName = Constants.DEFAULT_SCHED_NAME;
  long   schedRuntime =  Constants.DEFAULT_RUN_TIME;
  int schedNumThreads = Constants.DEFAULT_SCHED_NUM_THREADS;
  int schedThreadPoolQSize = Constants.DEFAULT_SCHED_NUM_THREADS * 2;
  int schedTimeSlice = Constants.DEFAULT_SCHED_TIME_SLICE;
  boolean useSpilledQueueSrc = Constants.DEFAULT_SPILL_QUEUESRC;
  String metadataStorageName = Constants.DEFAULT_METADATA_STORAGE_NAME;
  String spillStorageName = Constants.DEFAULT_SPILL_STORAGE_NAME;
  String storageFolder =  Constants.DEFAULT_STORAGE_FOLDER;
  long storageCacheSize = Constants.DEFAULT_STORAGE_CACHESIZE;
  boolean schedOnNewThread = Constants.DEFAULT_SCHED_ON_NEW_THREAD;
  boolean dynamicTupleClass = Constants.DEFAULT_DYNAMIC_TUPLE_CLASS;
  boolean usePagedTuple = Constants.DEFAULT_PAGED_TUPLE;
  boolean usePagedList = Constants.DEFAULT_PAGED_LIST;
  boolean dynamicPageClass = Constants.DEFAULT_DYNAMIC_PAGE_CLASS;
  int listInitPageTableSize = Constants.DEFAULT_LIST_PAGETABLE_SIZE;
  int listPageSize = Constants.DEFAULT_LIST_PAGESIZE;
  int listMinNodesPage = Constants.DEFAULT_LIST_MINNODES_PAGE;
  int tupleInitPageTableSize = Constants.DEFAULT_TUPLE_PAGETABLE_SIZE;
  int tuplePageSize = Constants.DEFAULT_TUPLE_PAGESIZE;
  int tupleMinNodesPage = Constants.DEFAULT_TUPLE_MINNODES_PAGE;
  boolean useLogXMLTag = Constants.DEFAULT_LOG_USE_XMLTAG;
  String traceFolder = Constants.DEFAULT_LOG_TRACE_FOLDER;
  String        dataSourceFinderClass = null;
  IDataSourceFinder dataSourceFinder = null;
  IQueryDestLocator queryDestLocator = null;
  IArchiverFinder   archiverFinder = null;
  TimeZone      tz = TimeZone.getDefault();
  int queueSrcSpillNormalThreshold = Constants.DEFAULT_SPILL_QUEUESRC_NORMAL_THRESHOLD;
  int queueSrcSpillPartialThreshold = Constants.DEFAULT_SPILL_QUEUESRC_PARTIAL_THRESHOLD;
  int queueSrcSpillFullThreshold = Constants.DEFAULT_SPILL_QUEUESRC_FULL_THRESHOLD;
  int queueSrcSpillSyncThreshold = Constants.DEFAULT_SPILL_QUEUESRC_SYNC_THRESHOLD;
  IUserFunctionLocator userFunctionLocator = null;
  ICartridgeLocator cartridgeLocator = null;
  IFaultHandler faultHandler = null;

  boolean directInterop = Constants.DEFAULT_DIRECT_INTEROP;
  boolean isRegressPushMode = Constants.DEFAULT_REGRESS_PUSH_MODE;
  boolean isJDBCTest = false;
  String  beamTxnCtxName = "BEAM_TRANSACTION_CONTEXT";
  String  contextColName = "TRANSACTION_CID";
  String  txnColName = "TRANSACTION_TID";
  String dateFormat = "MM/dd/yyyy HH:mm:ss";
  String timeZone = tz.getID();
    
  /** A Flag to check whether metadata cleanup will occur on CEP Engine Startup*/
  boolean isMetadataCleanupOnStartup 
    = Constants.DEFAULT_IS_METADATA_CLEANUP_ON_STARTUP;
  
  long externalRowsThreshold = Long.MIN_VALUE;
  int dop = Constants.DEFAULT_DEGREE_OF_PARALLELISM;  
  boolean isStatsEnabled = Constants.DEFAULT_STATS_ENABLED;
  
  /** Property to specify the target SQL type for archiver query*/
  SQLType targetSQLType = Constants.DEFAULT_TARGET_SQL_TYPE;
  
  /** Property to indicate what timestamp (millis or nanos) should be used
   *  by System timestamped sources.
   */
  boolean useMillisTs = Constants.DEFAULT_USE_MILLIS_TS;
  
  public ConfigManager() 
  {
    homeFolder = getWorkFolder();
  }
  
  public static String getWorkFolder()
  {
   Path temp;
   try {
	   String tempDirStr = System.getProperty("java.io.tmpdir");
	   File tempDir = SecureFile.getFile(tempDirStr);
	   tempDir.mkdirs();
	   temp = Files.createTempDirectory(Constants.WORK_FOLDER_PREFIX);
   } catch (IOException e) {
	   throw new RuntimeException(e);
   }
   return temp.toString();
  }

  public static String getWorkFilePath(String postfix)
  {
    String work = getWorkFolder();
    if (File.separatorChar != '/') {
      postfix = postfix.replace('/', File.separatorChar);
    }
    return work + File.separator + postfix;
  }
  
  private String getFolder(String name, String path)
  {
	  return getFolder(name, path, false);
  }
  
  private String getFolder(String name, String path, boolean makeFolder)
  {
    HashMap<String,String> valMap = new HashMap<String,String>();
    valMap.put("HOME", homeFolder);
    path = StringUtil.expand(path, valMap);
    if (makeFolder)
    {
	    try
	    {
	      File f = SecureFile.getFile(path);
	      f.mkdirs();
	    }
	    catch(Exception e)
	    {
	      LogUtil.warning(LoggerType.CUSTOMER, 
	          "Failed to create "+ name + ":" + path + "\n" + e.toString());
	    }
    }
    return path;
  }

  /**
   * Sets the environment configuration
   */
  public void setEnvConfig(IEnvConfig config)
  {
    // copy config from env so that configuation can be modifed later
    // using 'alter system'
    dataSourceFinder = config.getDataSourceFinder();
    userFunctionLocator = config.getUserFunctionLocator();
    queryDestLocator = config.getQueryDestLocator();
    archiverFinder   = config.getArchiverFinder();
    cartridgeLocator = config.getCartridgeLocator();
    setFaultHandler(config.getFaultHandler());
    
    ILoggerFactory loggerFactory = config.getLoggerFactory();
    LogUtil.setLoggerFactory(loggerFactory);
    
    storageFolder = config.getStorageFolder();
    metadataStorageName = config.getMetadataStorageName();
    spillStorageName = config.getSpillStorageName();
    storageCacheSize = config.getStorageCacheSize();
    isMetadataCleanupOnStartup = config.getIsMetadataCleanupOnStartup();
    externalRowsThreshold = config.getExternalRowsThreshold();
    dop = config.getDegreeOfParallelism();

    // Scheduler
    schedulerName = config.getSchedulerClassName();
    schedRuntime = config.getSchedRuntime();
    schedNumThreads = config.getSchedNumThreads();
    schedThreadPoolQSize = config.getSchedThreadPoolQSize();
    schedTimeSlice = config.getSchedTimeSlice();
    schedOnNewThread = config.getSchedOnNewThread();
    directInterop = config.getDirectInterop();
    
    // Spill
    useSpilledQueueSrc = config.getUseSpilledQueueSrc();
    queueSrcSpillNormalThreshold = config.getQueueSrcSpillNormalThreshold();
    queueSrcSpillPartialThreshold = config.getQueueSrcSpillPartialThreshold();
    queueSrcSpillFullThreshold = config.getQueueSrcSpillFullThreshold();
    queueSrcSpillSyncThreshold = config.getQueueSrcSpillSyncThreshold();
    
    // Trace
    useLogXMLTag = config.getUseLogXMLTag();
    traceFolder = config.getTraceFolder();
    
    // Target SQL Type for archiver query
    setTargetSQLType(config.getTargetSQLType());
    setUseMillisTs(config.getUseMillisTs());
    
    setIsJDBCTest(config.getIsJDBCTest());
    
    setBeamTxnCtxName(config.getBeamTxnCtxName());
    setContextColName(config.getContextColName());
    setTxnColName(config.getTxnColName());
    
    String df = config.getDateFormat();
    if (!df.equals(dateFormat))
    {
      setDateFormat(df);
    }
    String tzstr = config.getTimeZone();
    if (!timeZone.equals(tzstr))
    {
      setTimeZone(tzstr);
    }
    
    // Statistics
    isStatsEnabled = config.getIsStatsEnabled();
  }

  public void setSchedulerClassName(String schedName) 
  {
    schedulerName = schedName;
  }
  
  public String getSchedulerClassName()
  {
    return schedulerName;
  }
  
  public Scheduler getScheduler() 
    throws CEPException
  {
    Scheduler sched = null;
    Throwable err = null;
    if (schedulerName != null)
    {
      try
      {
        Class<?> cf = Class.forName(schedulerName);
        Object f = cf.newInstance();
        assert f instanceof Scheduler;
        sched = (Scheduler) f;
      }
      catch (ClassNotFoundException e)
      {
        err = e;
      }
      catch (InstantiationException e)
      {
        err = e;
      }
      catch (IllegalAccessException e)
      {
        err = e;
      }
    }
    if (sched == null)
    {
      if (err != null)
      {
        LogUtil.severe(LoggerType.TRACE, schedulerName + " : " + err.getMessage());
      }
      throw new CEPException(InterfaceError.NAME_NOT_FOUND);
    }
    return sched;
  }

  public long getSchedRuntime() {return schedRuntime;}
  public void setSchedRuntime(long t) { schedRuntime = t; }

  public int getSchedNumThreads() { return schedNumThreads;}
  public void setSchedNumThreads(int t) { schedNumThreads = t; }
  
  public int getSchedThreadPoolQSize() {return schedThreadPoolQSize;}
  public void setSchedThreadPoolQSize(int v) { schedThreadPoolQSize = v; }
  
  public int getSchedTimeSlice() {return schedTimeSlice;}
  public void setSchedTimeSlice(int v) { schedTimeSlice = v; }
  
  public String getMetadataStorageName(){ return metadataStorageName; }
  public void setMetadataStorageName(String v) { metadataStorageName = v; }

  public boolean getUseSpilledQueueSrc() {return useSpilledQueueSrc;}
  public void setUseSpilledQueueSrc(boolean v) { useSpilledQueueSrc = v; }

  public String getSpillStorageName() {return spillStorageName; }
  public void setSpillStorageName(String v) { spillStorageName = v; }

  public boolean getSchedOnNewThread() { return schedOnNewThread; }
  public void setSchedOnNewThread(boolean v) { schedOnNewThread = v; }

  public boolean getDynamicTupleClass() { return dynamicTupleClass; }
  public void setDynamicTupleClass(boolean v) { dynamicTupleClass = v; }

  public boolean getDirectInterop() { return directInterop; }
  public void setDirectInterop(boolean v) { directInterop = v; }

  public boolean isRegressPushMode() { return isRegressPushMode; }
  public void setIsRegressPushMode(boolean v) { isRegressPushMode = v; }
  
  public boolean isJDBCTest() { return isJDBCTest; }
  public void setIsJDBCTest(boolean v ) { isJDBCTest = v; }
  
  public String getBeamTxnCtxName() { return beamTxnCtxName; }
  public void setBeamTxnCtxName(String name) { beamTxnCtxName = name; }
  
  public String getContextColName() { return contextColName; }
  public void setContextColName(String name) { contextColName = name; }
 
  public String getTxnColName() { return txnColName; }
  public void setTxnColName(String name) { txnColName = name; }
  
  public boolean getUsePagedTuple() { return usePagedTuple; }
  public void setUsePagedTuple(boolean v) { usePagedTuple = v; }

  public boolean getUsePagedList() { return usePagedList; }
  public void setUsePagedList(boolean v) { usePagedList = v; }

  public boolean getDynamicPageClass() { return dynamicPageClass; }
  public void setDynamicPageClass(boolean v) { dynamicPageClass = v; }

  public boolean getUseLogXMLTag() { return useLogXMLTag; }
  public void setUseLogXMLTag(boolean v) { useLogXMLTag = v; }
  
  public int getListInitPageTableSize() { return listInitPageTableSize; }
  public void setListInitPageTableSize(int v) { listInitPageTableSize  = v; }
  
  public int getListPageSize() { return listPageSize; }
  public void setListPageSize(int v) { listPageSize = v; }

  public int getListMinNodesPage() { return listMinNodesPage; }
  public void setListMinNodesPage(int v) { listMinNodesPage = v; }

  public int getTupleInitPageTableSize() { return tupleInitPageTableSize; }
  public void setTupleInitPageTableSize(int v) { tupleInitPageTableSize = v; }

  public int getTuplePageSize() { return tuplePageSize; }
  public void setTuplePageSize(int v) { tuplePageSize  = v; }

  public int getTupleMinNodesPage() { return tupleMinNodesPage; }
  public void setTupleMinNodesPage(int v) { tupleMinNodesPage = v; }

  public int getQueueSrcSpillNormalThreshold() { return queueSrcSpillNormalThreshold; }
  public void setQueueSrcSpillNormalThreshold(int v) { queueSrcSpillNormalThreshold = v; }

  public int getQueueSrcSpillPartialThreshold() { return queueSrcSpillPartialThreshold; }
  public void setQueueSrcSpillPartialThreshold(int v) { queueSrcSpillPartialThreshold = v; }

  public int getQueueSrcSpillFullThreshold() { return queueSrcSpillFullThreshold; }
  public void setQueueSrcSpillFullThreshold(int v) { queueSrcSpillFullThreshold = v; }

  public int getQueueSrcSpillSyncThreshold() { return queueSrcSpillSyncThreshold; }
  public void setQueueSrcSpillSyncThreshold(int v) { queueSrcSpillSyncThreshold = v; }
  
  public String getTraceFolder() { return getFolder("traceFolder", traceFolder); }
  public void setTraceFolder(String v) { traceFolder = v; }
  
  public long getExternalRowsThreshold() { return externalRowsThreshold; }
  public void setExternalRowsThreshold(long externalRowsThreshold)
  {
    this.externalRowsThreshold = externalRowsThreshold;
  }
  
  public int getDegreeOfParallelism() { return dop; }

  public boolean isStatsEnabled() { return isStatsEnabled; }
  
  /**
   * Return the target SQL Type for generated SQL Query
   * @return
   */
  public SQLType getTargetSQLType() 
  {
    return this.targetSQLType;
  }
  
  /**
   * Return the useMillisTs param value
   * @return
   */
  public boolean getUseMillisTs()
  {
    return this.useMillisTs;
  }
  
  /**
   * Set the target SQL Type for generated SQL Query
   * @param targetSQLType
   */
  public void setTargetSQLType(String targetSQLType)
  {
    if(targetSQLType.equalsIgnoreCase(SQLType.BI.toString()))
    {
      this.targetSQLType = SQLType.BI;
    }
    else if(targetSQLType.equalsIgnoreCase(SQLType.ORACLE.toString()))
    {
      this.targetSQLType = SQLType.ORACLE;
    }
    else
    {
      // Invalid Target SQL Type is specified in config file. Log the problem.
      String logMessage = "invalid value " + targetSQLType + " for environment " 
        + "config targetSQLType. Only BI or ORACLE types are supported";
      LogUtil.info(LoggerType.TRACE, logMessage);
      
      // Set the value to default sql type
      this.targetSQLType = Constants.DEFAULT_TARGET_SQL_TYPE;
    }    
  }
  
  /**
   * Setter for the param useMillisTs.
   * If set to true, system timestamped sources would use System.currentTimeMillis()
   * else System.nanoTime() (default)
   * @param useMillisTs
   */
  public void setUseMillisTs(boolean useMillisTs)
  {
    this.useMillisTs = useMillisTs;
  }

  public void setDateFormat(String s)
  {
    dateFormat = s;
    CEPDateFormat df = CEPDateFormat.getInstance();
    SimpleDateFormat f = new SimpleDateFormat(s);
    df.setDefaultFormat(f);
  }

  public void setTimeZone(String s)
  {
    timeZone = s;
    tz = TimeZone.getTimeZone(s);
    CEPDateFormat df = CEPDateFormat.getInstance();
    df.setDefaultTimeZone(tz);
    //NOTE. if the timezone string is not understood the GMT zone will be used
    //It's getTimeZone's behavior.
  }
  
  public TimeZone getDefaultTimeZone() 
  {
    return tz;
  }
  
  public long getStorageCacheSize() {return storageCacheSize; }
  public void setStorageCacheSize(long v) {storageCacheSize = v;}
  
  public String getStorageFolder()
  {
    return getFolder("storageFolder", storageFolder); 
  }
  
  public void setStorageFolder(String folder) { storageFolder = folder;}
  
  /**
   * Get Flag whether metadata cleanup will occur on CEP Engine startup
   * @return true if metadata cleanup will occur
   *         false otherwise
   */
  public boolean getIsMetadataCleanupOnStartup() {return isMetadataCleanupOnStartup;}
  
  /**
   * Set Flag whether metadata cleanup should occur on CEP Engine startup
   * Called by Spring Framework
   * @param isMetadataCleanupOnStartup
   */
  public void setIsMetadataCleanupOnStartup(boolean isMetadataCleanupOnStartup)
  {
    this.isMetadataCleanupOnStartup = isMetadataCleanupOnStartup;
  }
  
  
  public String getMetadataStorageFolder()
  {
    String folder = getStorageFolder();
    if (folder != null && 
       !Constants.DEFAULT_METADATA_STORAGE_FOLDER.equals("."))
    {
      folder += File.separator;
      folder += Constants.DEFAULT_METADATA_STORAGE_FOLDER;
    }
    return folder;
  }

  public String getSpillStorageFolder()
  {
    String folder = getStorageFolder();
    if (folder != null)
    {
      folder += File.separator;
      folder += Constants.DEFAULT_SPILL_STORAGE_FOLDER;
    }    
    return folder;
  }
 
  public void setDataSourceFinderClass(String className) 
  {
    dataSourceFinderClass = className;
  }

  public String setDataSourceFinderClass() 
  {
    return dataSourceFinderClass;
  }
  
  public IDataSourceFinder getDataSourceFinder() 
  { 
    if (dataSourceFinderClass != null && dataSourceFinder == null)
    {
      Throwable err = null;
      try
      {
        Class<?> cf = Class.forName(dataSourceFinderClass);
        Object f = cf.newInstance();
        assert f instanceof IDataSourceFinder;
        dataSourceFinder = (IDataSourceFinder) f;
      }
      catch (ClassNotFoundException e)
      {
        err = e;
      }
      catch (InstantiationException e)
      {
        err = e;
      }
      catch (IllegalAccessException e)
      {
        err = e;
      }
      if (err != null)
      {
        LogUtil.severe(LoggerType.TRACE, dataSourceFinderClass + "\n" + err.getMessage());
      }
    }
    return dataSourceFinder;
  }
  
  public IUserFunctionLocator getUserFunctionLocator() 
  {
    // REVIEW should we support a user function locator class concept as well?
    return userFunctionLocator;
  }
  
  public void setUserFunctionLocator(IUserFunctionLocator locator) 
  {
    this.userFunctionLocator = locator;
  }
  
  public IQueryDestLocator getQueryDestLocator() 
  { 
    return queryDestLocator;
  }
  
  public ICartridgeLocator getCartridgeLocator() 
  {
    return cartridgeLocator;
  }
  
  public void dump()
  {
    LogLevelManager lm = CEPManager.getInstance().getLogLevelManager();
    IDumpContext dumper = lm.openDumper(null, null);
    String tag1 = LogUtil.beginDumpObj(dumper, this);
    LogUtil.endDumpObj(dumper, tag1);
   lm.closeDumper(null, null, dumper);
  }

  /**
   * @return the archiverFinder
   */
  public IArchiverFinder getArchiverFinder()
  {
    return archiverFinder;
  }

  public IFaultHandler getFaultHandler()
  {
    return faultHandler;
  }
  
  public synchronized void setFaultHandler(final IFaultHandler handler)
  {
    // After a fault handler is set, do not allow it to be un-set.
    //
    // REVIEW This solves the race-condition between the IEnvConfig and setting the FH directly 
    //  in the ConfigManager. We may come up with a better way of doing this, but this fixes it 
    //  for the time being.
    if (handler != null)
      this.faultHandler = handler;
  }
}
