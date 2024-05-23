/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/service/CEPManager.java /main/48 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      04/05/11 - add resetInstance for testing
 hopark      04/03/11 - refactor storage
 parujain    11/24/09 - synonym support
 hopark      01/26/09 - change setConfig api
 parujain    01/28/09 - txn mgmt
 hopark      12/04/08 - pass CEPManager for evitPolicy
 hopark      12/02/08 - move LogLevelManager to ExecContext
 hopark      11/19/08 - move test related code out of CEPManager
 hopark      11/06/08 - fix dumpplan
 hopark      10/31/08 - add dropSchema
 hopark      10/28/08 - initialize on SetEnvConfig
 hopark      10/07/08 - use execContext to remove statics
 hopark      10/22/08 - workaround stop/start with fabric
 hopark      10/06/08 - schema should be set before invoking executeDDL
 parujain    09/30/08 - drop schema
 hopark      09/26/08 - add setEnvConfig
 hopark      09/22/08 - move setDataSourceFinder to configManager
 sbishnoi    09/23/08 - incorporating changes made in constants
 sbishnoi    09/10/08 - support for schema
 parujain    08/26/08 - bug fix
 hopark      06/26/08 - add dataSourceManager
 sbishnoi    06/10/08 - adding isMetadatacleanup on start
 hopark      03/17/08 - use springbean for config
 sbishnoi    04/24/08 - enable colt function seeding
 sbishnoi    04/23/08 - removing colt install seeding
 sbishnoi    04/15/08 - add init for Colt Aggregate Install
 udeshmuk    03/12/08 - create user function mgr and add getter for it
 parujain    02/13/08 - shutdown problem
 mthatte     08/23/07 - 
 sbishnoi    08/08/07 - add init for Colt Install
 najain      07/11/07 - 
 skmishra    06/20/07 - cleanup scheduler code
 hopark      06/08/07 - fix config
 parujain    05/02/07 - support ExecStatsManager
 hopark      04/17/07 - storage leak debug
 parujain    03/26/07 - instantiate WindowManager
 parujain    03/22/07 - init throws exception
 parujain    03/22/07 - sched_on_new_thread support
 hopark      03/21/07 - metadata storage re-org
 parujain    03/21/07 - threaded scheduler
 najain      03/08/07 - cleanup
 parujain    02/13/07 - system startup
 parujain    02/09/07 - system startup
 hopark      01/12/07 - add storagemanager init
 parujain    01/29/07 - fix oc4j startup
 dlenkov     12/14/06 - system DDLs
 parujain    11/29/06 - Add sched.run for generalization
 anasrini    10/27/06 - add getScheduler method
 najain      11/07/06 - bug fix
 najain      10/30/06 - store debugging level
 najain      10/25/06 - use Properties for the config
 najain      10/24/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/service/CEPManager.java /main/46 2010/02/04 14:33:02 apiper Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import oracle.cep.execution.snapshot.SnapshotGenerator;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.metadata.CacheObjectFactoryImpl;
import oracle.cep.metadata.SystemManager;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.metadata.cache.Cache;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IEvictPolicy;
import oracle.cep.memmgr.IEvictPolicy.Source;
import oracle.cep.server.CEPServer;
import oracle.cep.server.CEPServerRegistryImpl;
import oracle.cep.storage.IStorage;
import oracle.cep.storage.StorageManager;
import oracle.cep.util.DebugUtil;

/**
 * This class manages the system services - starts up all the managers etc.
 *
 * @since 1.0
 */
public class CEPManager implements CEPServerEnvConfigurable
{
  private boolean             initialized = false;   
  private CEPServerRegistryImpl serverRegistry;
  private ConfigManager       configMgr;
  private StorageManager      storageMgr;  
  private FactoryManager      factoryMgr;
  private IEvictPolicy        evictPolicy;
  private LogLevelManager     logLevelMgr;
  /** cache for systemMgr */
  private Cache               cache;
  private SystemManager       systemMgr;

  private static CEPManager  s_instance;
  
  public static synchronized CEPManager getInstance()
  {
    if (s_instance == null)
    {
      s_instance = new CEPManager();
    }
    return s_instance;
  }
  
  //This method is only for unit testing...
  public static synchronized void resetInstance()
  {
	  s_instance = null;
  }
  
  private CEPManager()
  {
  }

  public void setServerRegistry(CEPServerRegistryImpl sr) { serverRegistry = sr;}
  public void setConfig(ConfigManager cfg) { configMgr = cfg; }
  public void setEvictPolicy(IEvictPolicy policy) { evictPolicy = policy; }

  public void init()
    throws Exception
  {
    if (initialized)
      return;
    try
    {
      // check if Debug Utility is turned on and print warning
      if (DebugUtil.isDebugModeOn()) {
        System.out.println("***********************************");
        System.out.println("WARNING: Debug mode is turned on.");
        System.out.println("**********************************");
      }
      LogUtil.info(LoggerType.TRACE, "Initializing CEPManager");
      instantiate();
      
      initAndSeed();

    } catch (Exception ex) {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, ex);
      throw(ex);
    }
    initialized = true;
  }

  public void close() throws Exception
  {
    if (serverRegistry != null) {
      serverRegistry.close();
      serverRegistry = null;
    }
    storageMgr.close();
    if (evictPolicy != null && storageMgr.getSpillStorage() != null) {
      evictPolicy.stopEvictor();
      evictPolicy = null;
    }
    initialized = false;
  }
  
  private void initAndSeed() throws Exception
  {
    try {
      // check if Debug Utility is turned on and print warning
      if (DebugUtil.isDebugModeOn()) {
        System.out.println("***********************************");
        System.out.println("WARNING: Debug mode is turned on.");
        System.out.println("**********************************");
      }
      SnapshotGenerator.registerClasses();
      
      storageMgr.init(configMgr);
      IStorage metaStorage = storageMgr.getMetadataStorage();
      Cache.initNameSpace(metaStorage);
      cache.init(metaStorage, factoryMgr);
      if (evictPolicy != null && storageMgr.getSpillStorage() != null)
      {
        evictPolicy.init(this);
      }
      // It is imporatant that storageMgr.start needs to be called
      // after cache and evictMgr is initialized.
      storageMgr.start();
        
      systemMgr = new SystemManager(this, cache);
      systemMgr.init(configMgr);

      /**
       * Find System state
       * If state is null;create new system state and do seed & other operations
       * else If is Metadata cleanup required; Set System state to ZERO
       */
//       SystemState state = systemMgr.findSystemState();
//       if (state == null)
//       {
//         systemMgr.beginSystemState();
//       }
//       else
//       {
//       	 if (configMgr.getIsMetadataCleanupOnStartup())
//       	 {
//         	systemMgr.deleteSystemState();
//            systemMgr.beginSystemState();
//       	 }
//       }

       if (evictPolicy != null && storageMgr.getSpillStorage() != null)
         evictPolicy.startEvictor();

        if (serverRegistry != null) {
            serverRegistry.init(this);
        }
       LogUtil.info(LoggerType.TRACE, "CEPManager initAndSeed completed.");
    } catch (Exception ex) {
      throw(ex);
    }
  }
  
  private void instantiate() throws Exception
  {
    logLevelMgr = new LogLevelManager(this);
    logLevelMgr.setConfig(configMgr.getUseLogXMLTag(), configMgr.getTraceFolder());
    cache = new Cache(0, new CacheObjectFactoryImpl());
    storageMgr = new StorageManager();
    factoryMgr = new FactoryManager(this);
  }

  /**
   * @return Returns the configMgr
   */
  public ConfigManager getConfigMgr()
  {
    return configMgr;
  }
  
  public IEvictPolicy getEvictPolicy()
  {
    return evictPolicy;
  }
  
  public synchronized void runEvictor() 
  {
    if (evictPolicy != null)  
    {
      if (evictPolicy.needEviction(Source.Factory))
        evictPolicy.runEvictor(Source.Factory);
    }
  }

  /**
   * @return Returns the StorageManager
   */
  public StorageManager getStorageManager()
  {
    return storageMgr;
  }

  /**
   * @return Returns the System Manager
   */
  public SystemManager getSystemMgr()
  {
    return systemMgr;
  }
  
  public Cache getCache() {return cache;}
  
  /**
   * @return Returns the StorageManager
   */
  public FactoryManager getFactoryManager()
  {
    return factoryMgr;
  }  
  
  public CEPServerRegistryImpl getServerRegistry() {return serverRegistry;}
  
  public CEPServer getSystemServer()
  {
    return serverRegistry.getSystemServer();
  }

  /**
   * @return Returns the LogLevelManager
   */
  public LogLevelManager getLogLevelManager()
  {
    return logLevelMgr;
  }    
  
  public ExecContext getSystemExecContext()
  {
    CEPServer server = serverRegistry.getSystemServer();
    return server.getExecContext();
  }
  
  public Collection<ExecContext> getExecContexts()
  {
    Collection<CEPServer> servers = serverRegistry.getServers();
    Collection<ExecContext> r = new ArrayList<ExecContext>(servers.size());
    for (CEPServer s : servers)
    {
      r.add(s.getExecContext());
    }
    return r;
  }
    
  /**
   * Sets the environment configuration
   */
  public void setEnvConfig(IEnvConfig config) throws Exception
  {
      assert (configMgr != null);
      configMgr.setEnvConfig(config);
      init();
  }  
}
