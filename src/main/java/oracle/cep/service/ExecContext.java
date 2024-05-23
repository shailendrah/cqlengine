/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/service/ExecContext.java hopark_cqlsnapshot/4 2016/02/26 11:55:07 hopark Exp $ */

/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 ExecContext represents a running instance of cep server.

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    12/21/15 - adding support for ha snapshot generation
 sbishnoi    09/23/14 - adding a new context flag variable to set if an
                        internal DDL is being executed
 sbishnoi    10/09/12 - XbranchMerge
                        sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0_11.1.1.7.0 from
                        st_pcbpel_11.1.1.4.0
 sbishnoi    10/08/12 - XbranchMerge sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0
                        from st_pcbpel_pt-11.1.1.7.0
 sbishnoi    09/14/12 - passing ExecContext to constructor of ExecStatsManager
 udeshmuk    08/17/12 - flag to indicate fully qualified name should be
                        returned by getSQLEquivalent
 anasrini    06/30/11 - XbranchMerge anasrini_bug-12675151_ps5 from
                        st_pcbpel_11.1.1.4.0
 anasrini    06/19/11 - support for partition parallel regression tests
 sbishnoi    10/19/10 - adding an API for getting only schema name without
                        service name
 parujain    05/21/10 - remove drop schema ddl
 parujain    11/24/09 - synonym support
 parujain    09/22/09 - dependency manager
 alealves    05/07/09 - Use service name by itself if schemae name is null
 hopark      03/26/09 - log api change
 hopark      03/01/09 - fix ref loop
 hopark      01/26/09 - change setConfig api
 parujain    01/28/09 - transaction mgmt
 hopark      01/21/09 - set Thread a name
 parujain    12/08/08 - stats cleanup
 hopark      12/02/08 - move LogLevelManaer to ExecContext
 hopark      12/03/08 - keep the installable funcs in execcontext to avoid
                        duplicate function creation
 parujain    11/18/08 - support statsRuntimeMBean
 hopark      11/20/08 - suppress msgs
 hopark      11/19/08 - move test related code out of ExecContext
 hopark      11/06/08 - lazy seeding supports
 hopark      11/07/08 - activate refactor
 hopark      11/06/08 - fix dumpplan
 hopark      10/31/08 - add dropSchema
 hopark      10/28/08 - support running multiple cqlx test files
 skmishra    11/04/08 - synchronizing execDDL
 hopark      11/03/08 - fix setSchema
 hopark      10/02/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/service/ExecContext.java hopark_cqlsnapshot/4 2016/02/26 11:55:07 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.service;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ParserError;
import oracle.cep.execution.ExecManager;
import oracle.cep.execution.statistics.ExecStatsManager;
import oracle.cep.execution.scheduler.SchedulerManager;
import oracle.cep.execution.scheduler.SchedulerManager2;
import oracle.cep.execution.snapshot.JournalSnapshot;
import oracle.cep.execution.snapshot.SnapshotGenerator;
import oracle.cep.execution.snapshot.SnapshotLoader;
import oracle.cep.execution.xml.XmlManager;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.metadata.DependencyManager;
import oracle.cep.metadata.QueryManager;
import oracle.cep.metadata.SchemaManager;
import oracle.cep.metadata.SourceManager;
import oracle.cep.metadata.TableManager;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.metadata.UserFunctionManager;
import oracle.cep.metadata.ViewManager;
import oracle.cep.metadata.WindowManager;
import oracle.cep.metadata.SynonymManager;
import oracle.cep.metadata.cache.Cache;
import oracle.cep.planmgr.PlanManager;
import oracle.cep.server.Command;
import oracle.cep.server.CommandInterpreter;
import oracle.cep.transaction.ITransaction;
import oracle.cep.transaction.TransactionManager;
import oracle.cep.install.Install;
import oracle.cep.interfaces.InterfaceManager;
import oracle.cep.jmx.CEPStatsIteratorFactory;
import oracle.cep.colt.install.ColtInstall;
import oracle.cep.colt.install.ColtAggrInstall;
import oracle.cep.common.Constants;
/**
 * This class manages the system services - starts up all the managers etc.
 *
 * @since 1.0
 */
public class ExecContext
{
  private String              serviceName;
  private boolean             systemService;

  private CEPManager          cepMgr;
  private TableManager        tableMgr;
  private QueryManager        queryMgr;
  private ExecManager         execMgr;
  private DependencyManager   depMgr;
  private CommandInterpreter  cmdInt;
  private ViewManager         viewMgr;
  private PlanManager         planMgr;
  private Command             cmd;
  private SchedulerManager    schMgr;
  private WindowManager       winMgr;
  private ExecStatsManager    execStatsMgr;
  private Thread              schedThread;
  private UserFunctionManager userFnMgr;
  private InterfaceManager    interfaceMgr;
  private SchemaManager       schemaMgr;
  private TransactionManager  txnMgr;
  private SynonymManager      synMgr;
  private XmlManager          xmlMgr;
  private CEPStatsIteratorFactory  statsFactory;
  private LogLevelManager     logLevelMgr;

  private Install               builtinFuncInstaller;
  private ColtInstall           coltInstaller;
  private ColtAggrInstall       coltAggrInstaller;
  
  /** Global cache for all the managers */
  private Cache              cache;

  /** Support for partition parallel regression tests */
  private String ppContext;

  /** Used in archived relation query generation. 
   * FIXME: may be we can specify whether we are interested in fully
   * qualified attr name or not using a different mechanism, rather than
   * having this variable here. 
   * For now, putting it here since execContext is argument to
   * getSQLEquivalent().
   */
  private boolean returnFullyQualifiedAttrName=false;
  
  /** A flag to check if the DDL executed in current context is an internal DDL.
   *  This will be used to differentiate between Query creation DDLs invoked by
   *  user and internally invoked by parallelism code.
   */
  private boolean isInternalDDL = false;
  
  public boolean shouldReturnFullyQualifiedAttrName()
  {
    return this.returnFullyQualifiedAttrName;
  }
  
  public void setReturnFullyQualifiedAttrName(boolean val)
  {
    this.returnFullyQualifiedAttrName = val;
  }
  
  public boolean isInternalDDL() {
    return isInternalDDL;
  }

  public void setInternalDDL(boolean isInternalDDL) {
    this.isInternalDDL = isInternalDDL;
  }

  /**
   * Full Name of current Schema composed as "Service Name"."Schema Name"
   */
  private ThreadLocal<String> currentSchema = new ThreadLocal<String>(){
    @Override protected String initialValue() 
    {
      return Constants.DEFAULT_SCHEMA;
    }
  };
  
  private ThreadLocal<String> currentSchemaName = new ThreadLocal<String>(){
    @Override protected String initialValue() 
    {
      return Constants.DEFAULT_SCHEMA;
    }
  };
  
  private ThreadLocal<ITransaction> currentTxn = new ThreadLocal<ITransaction>() {
 
  };
  
  public ExecContext(String serviceName, CEPManager cepMgr)
  {
    this.cepMgr = cepMgr;
    this.serviceName = serviceName;
    this.systemService = serviceName.equals(Constants.DEFAULT_CEP_SERVICE_NAME);
    this.cache = cepMgr.getCache();
    this.statsFactory = null;
  }

  public void init()
    throws Exception
  {
    try
    {
      LogUtil.info(LoggerType.TRACE, "Initializing cep service : " + serviceName);
      instantiate();
      
      initAndSeed();

      //Run scheduler only when SchedOnNewThread is turned on.
      //If SchedOnNewThread is turned off, runScheduler will be 
      //invoked with 'alter system run'.
      ConfigManager configMgr = cepMgr.getConfigMgr();
      if (configMgr.getSchedOnNewThread())
      {
        runScheduler();
      }
    } catch (Exception ex) {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, ex);
      throw(ex);
    }
  }

  public void close() throws Exception
  {    
    dropSchema(serviceName, false);
    schMgr.shutdown();  
    schemaMgr.dropBuiltinObjs(Constants.DEFAULT_SCHEMA);
    execMgr.close();    

    schedThread = null;
    logLevelMgr = null;
    tableMgr = null;
    queryMgr = null;
    execMgr  = null;
    depMgr   = null;
    cmdInt = null;
    viewMgr = null;
    planMgr = null;
    txnMgr = null;
    cmd = null;
    schMgr = null;
    winMgr = null;
    execStatsMgr = null;
    userFnMgr = null;
    interfaceMgr = null;
    xmlMgr = null;
    synMgr = null;
    schemaMgr = null;
    currentSchema.set(Constants.DEFAULT_SCHEMA);
    currentSchemaName.set(Constants.DEFAULT_SCHEMA);
    currentTxn = null;
  }
  
  private void initAndSeed() throws Exception
  {
    try {
      interfaceMgr.init();
      schMgr.init();

      LogUtil.fine(LoggerType.TRACE, serviceName + " : seeding started.");
      seed();
      LogUtil.fine(LoggerType.TRACE, serviceName + " : seeding completed.");
      //creates static metadata objects
      userFnMgr.initialize();
      // restore old state
      LogUtil.fine(LoggerType.TRACE, serviceName + " : restoring query started.");
      queryMgr.startup(serviceName);
      LogUtil.fine(LoggerType.TRACE, serviceName + " : restoring query completed.");
    } catch (Exception ex) {
      throw(ex);
    }
  }
  
  private void instantiate()
  {
    logLevelMgr = new LogLevelManager(cepMgr);
    logLevelMgr.setExecContext(this);
    ConfigManager configMgr = cepMgr.getConfigMgr();
    logLevelMgr.setConfig(configMgr.getUseLogXMLTag(), configMgr.getTraceFolder());
    //cache = new Cache(0, new CacheObjectFactoryImpl());
    tableMgr = new TableManager(this, cache);
    queryMgr = new QueryManager(this, cache);
    execMgr  = new ExecManager(this);
    depMgr = new DependencyManager(this, cache);
    synMgr = new SynonymManager(this, cache);
    cmdInt = new CommandInterpreter(this);
    viewMgr = new ViewManager(this, cache);
    planMgr = new PlanManager(this);
    txnMgr = new TransactionManager(this);
    cmd = new Command();

    if (configMgr.getDirectInterop())
      schMgr = new SchedulerManager2(this);
    else
      schMgr = new SchedulerManager(this);

    winMgr = new WindowManager(this, cache);
    execStatsMgr = new ExecStatsManager(this);
    schedThread = null;
    userFnMgr = new UserFunctionManager(this, cache);
    interfaceMgr = new InterfaceManager(this);
    xmlMgr = new XmlManager(cepMgr);
    schemaMgr = new SchemaManager(this, cache);
  }

  public boolean isSystemService() 
  {
    return systemService;
  }

  private void seed()
  {
    //TODO remove it once metadata deadlock is resolved..
    synchronized(ExecContext.class)
    {
      builtinFuncInstaller = Install.init(this);
      coltInstaller = ColtInstall.init(this);
      coltAggrInstaller = ColtAggrInstall.init(this);
    }
  }
 
  /**
   * Returns the system service manager
   * @return cepManager
   */
  public CEPManager getServiceManager()
  {
    return cepMgr;
  }
  
 /**
  *  Get the scheduler thread 
  * @return Returns the thread 
  */
  public Thread getSchedThread()
  {
    return schedThread;  
  }
  
  /**
   * @return Returns the cache.
   */
  public Cache getCache()
  {
    return cache;
  }

  /**
   * @return Returns the cmd.
   */
  public Command getCmd()
  {
    return cmd;
  }

  /**
   * @return Returns the cmdInt.
   */
  public CommandInterpreter getCmdInt()
  {
    return cmdInt;
  }
  
  /**
   * Get the instance of DependencyManager
   * @return Returns the depMgr
   */
  public DependencyManager getDependencyMgr()
  {
    return this.depMgr;
  }

  /**
   * @return Returns the queryMgr.
   */
  public QueryManager getQueryMgr()
  {
    return queryMgr;
  }
  
  /**
   * @return Returns the tableMgr.
   */
  public TableManager getTableMgr()
  {
    return tableMgr;
  }

  /**
   * @return Returns the source manager.
   */
  public SourceManager getSourceMgr()
  {
    return (SourceManager) tableMgr;
  }
  
  public ViewManager getViewMgr()
  {
    return viewMgr;
  }

  /**
   * @return Return the userFnMgr.
   */
  public UserFunctionManager getUserFnMgr()
  {
    return userFnMgr;
  }
  
  /**
   * @return Returns the execMgr.
   */
  public ExecManager getExecMgr()
  {
    return execMgr;
  }
  
  public TransactionManager getTransactionMgr()
  {
    return txnMgr;
  }
 
  /**
   * Getter for Execution Statistics Manager
   * @return
   */
  public ExecStatsManager getExecStatsMgr()
  {
    return execStatsMgr;
  }
  
  /**
   * Getter of Synonym Manager
   * @return Returns the SynonymMgr
   */
  public SynonymManager getSynonymMgr()
  {
    return synMgr;
  }
  
  /**
   * @return Returns the planMgr
   */
  public PlanManager getPlanMgr()
  {
    return planMgr;
  }

  /**
   * Get WindowManager
   * @return Returns the WindowManager
   */
  public WindowManager getWindowMgr()
  {
    return winMgr;
  }
  
  /**
   * Get the scheduler Manager
   * @return Returns the SchedulerManager
   */
  public SchedulerManager getSchedMgr()
  {
    return schMgr;
  }
  
  /**
   * Get the interface manager
   */
  public InterfaceManager getInterfaceMgr()
  {
    return interfaceMgr;
  }
  
  public XmlManager getXmlMgr()
  {
    return xmlMgr;
  }
  
  /**
   * @return Returns the schemaMgr
   */
  public SchemaManager getSchemaMgr()
  {
    return schemaMgr;
  }

  public Install getBuiltinFuncInstaller()
  {     
    return builtinFuncInstaller;
  }
  
  public ColtInstall getColtInstaller()
  {
    return coltInstaller;
  }
  
  public ColtAggrInstall getColtAggrInstaller()
  {
    return coltAggrInstaller;
  }
  
  /**
   * @return Returns the LogLevelManager
   */
  public LogLevelManager getLogLevelManager()
  {
    return logLevelMgr;
  }    
  
  public CEPStatsIteratorFactory getStatsIteratorFactory()
  {
    if(statsFactory == null)
      this.statsFactory = new CEPStatsIteratorFactory(this);
    
    return this.statsFactory;
  }

  public String getServiceName() {return serviceName;}
  
  /**
   * Get schema name for current thread
   * Schema name is composed as "Service Name"."Schema Name"
   * Note that currentSchema is a ThreadLocal
   * @return
   */
  public String getSchema()
  {
    return currentSchema.get();
  }
  
  /**
   * Get Schema name for current thread
   * @return schema name
   */
  public String getSchemaName()
  {
    return currentSchemaName.get();
  }
 
  public String getServiceSchema(String schemaName)
  {
    if (schemaName == null) 
    {
      // return immediately without the cost of appending of strings
      return serviceName;
    } 
    else 
    {
      StringBuilder builder = new StringBuilder(serviceName);
      builder.append(".");
      builder.append(schemaName);
      
      return builder.toString();
    }
  }
  
  public String getDefaultSchema()
  {
    return getServiceSchema(Constants.DEFAULT_SCHEMA);
  }
  
  /**
   * Set schema name for current thread 
   * Note that currentSchema is a ThreadLocal
   * @param schemaName
   */
  public void setSchema(String schemaName)
  {
    String serviceSchema = getServiceSchema(schemaName);
    currentSchema.set(serviceSchema);
    currentSchemaName.set(schemaName);
  }
  
  public void setTransaction(ITransaction txn)
  {
    currentTxn.set(txn);
  }
  
  public ITransaction getTransaction()
  {
    return currentTxn.get();
  }

  // Support for partiton parallel regression tests
  public void setPartitionParallelContext(String ppContext)
  {
    this.ppContext = ppContext;
  }

  public String getPartitionParallelContext()
  {
    return ppContext;
  }
  
  public synchronized void dropSchema(String schemaName, boolean ifthrow)
  throws CEPException
  {
    LogUtil.info(LoggerType.CUSTOMER, "Dropping schema: " + getSchema());
    try{
      SchemaManager scm = getSchemaMgr();
      scm.dropSchema(schemaName);
      LogUtil.info(LoggerType.CUSTOMER, "Activate : Drop Schema success");
    }
    catch(CEPException ce)
    {
      LogUtil.info(LoggerType.CUSTOMER, "Activate : Drop Schema failure");
      if(ifthrow)
        throw ce;
    }
    return;
  }
  
  public synchronized boolean executeDDL(String ddl, boolean ifthrow) 
    throws CEPException
  {
    Command c = new Command();
    
    LogUtil.fine(LoggerType.CUSTOMER, "Activate : DDL = " + ddl );
      
    c.setCql(ddl);

    /* Schema is supposted to be set before invoking this.
     //Set the current Schema before executing DDL
     //setSchema(Constants.DEFAULT_SCHEMA);
     */
    
    LogUtil.fine(LoggerType.CUSTOMER, "Current schema: " + currentSchema);
    CommandInterpreter cmd = getCmdInt();
    cmd.execute(c);
    boolean err = false;
    
    if (c.isBSuccess())
    {
      LogUtil.fine(LoggerType.CUSTOMER, "Activate : DDL success");
    }
    else 
    {
      LogUtil.fine(LoggerType.CUSTOMER, "Activate : DDL Failed ");
      if(ifthrow)
      {
        Exception ex = c.getException();
        if(ex instanceof CEPException)
          throw (CEPException)ex;
        else
          throw new CEPException( ParserError.PARSER_ERROR, ex);
      }
      err = true;
    }
    return err;
  }
  
  public void runScheduler()
  {
    ConfigManager configMgr = cepMgr.getConfigMgr();
    runScheduler(configMgr.getSchedRuntime(), false);
  }
  
  public void stopScheduler()
  {
    schMgr.stop();
    schedThread = null;
  }
  
  public void runScheduler(long runtime, boolean initialized)
  {
    ConfigManager configMgr = cepMgr.getConfigMgr();
    LogUtil.config(LoggerType.TRACE, serviceName + " : runScheduler" +
        "rutime="+runtime + " schedOnNewThread=" + configMgr.getSchedOnNewThread() + " schedRuntime=" +
        configMgr.getSchedRuntime() + " ");
    if(initialized)
      schMgr.setRunTime(runtime);
    else
      schMgr.setRunTime(configMgr.getSchedRuntime());
    
    if(configMgr.getSchedOnNewThread()) 
    {
      if(schedThread != null)
      { // Don't fork the thread if a thread is already running
        if((schedThread.getState() == Thread.State.RUNNABLE)
         ||(schedThread.getState() == Thread.State.BLOCKED)
         ||(schedThread.getState() == Thread.State.TIMED_WAITING)
         ||(schedThread.getState() == Thread.State.WAITING))
            return;
      }
      schedThread = new Thread(schMgr, "scheduler");
    
      schedThread.start();
    }
    else
      schMgr.run();
      
  }
  
  public boolean isSchedulerRunning()
  {
    return schMgr.isRunning() && !schMgr.isRegressTestDone();
  }


  public synchronized void startBatch(boolean fullSnapshot) throws CEPException
  {
	  if (!fullSnapshot)
	  {
		  JournalSnapshot journal = new JournalSnapshot(this);
		  journal.startBatch();
	  }
  }
  
  public synchronized void endBatch(boolean fullSnapshot) throws CEPException
  {
	  if (!fullSnapshot)
	  {
		  JournalSnapshot journal = new JournalSnapshot(this);
		  journal.endBatch();
	  }
  }
  
  public synchronized void createSnapshot(ObjectOutputStream output, boolean fullSnapshot) throws CEPException
  {
	  SnapshotGenerator snapgen = new SnapshotGenerator(this);
	  snapgen.createSnapshot(output, fullSnapshot);
  }
  
  public synchronized void loadSnapshot(ObjectInputStream input, boolean fullSnapshot) throws CEPException
  {
	  SnapshotLoader loader = new SnapshotLoader(this);
	  loader.loadSnapshot(input, fullSnapshot);
  }

}


