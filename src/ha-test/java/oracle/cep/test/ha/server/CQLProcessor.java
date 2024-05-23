package oracle.cep.test.ha.server;

import java.rmi.RemoteException;

import oracle.cep.exceptions.CEPException;
import oracle.cep.extensibility.type.ITypeLocator;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.server.CEPServerRegistryImpl;
import oracle.cep.service.BaseCartridgeRegistry;
import oracle.cep.service.CEPManager;
import oracle.cep.service.CEPServerXface;
import oracle.cep.service.ICartridgeLocator;
import oracle.cep.service.IDataSourceFinder;

//import com.oracle.cep.cartridge.java.impl.JavaCartridge;
//import com.oracle.cep.cartridge.java.impl.JavaTypeSystemImpl;
//import com.oracle.cep.cartridge.spatial.SpatialCartridge;

/**
 * CQLProcessor is a class that handles processing continuous queries.
 *  
 *  @version $Header: beam/main/modules/cqservice/core/src/main/java/com.oracle.cep.spark/CQLProcessor.java /main/30 2015/04/22 21:33:30 shusun Exp $
 *  @author  hopark
 *  @since   12c
 */
public class CQLProcessor //implements IMetaObjProcessor
{
  protected QueryDestinationRegistry     m_destRegistry;
  protected EnvConfig                    m_envConfig;
  protected ConfigManager                m_configMgr;
  protected CEPServerRegistryImpl        m_serverRegistry;
  protected CEPManager                   m_cepMgr;
  protected CEPServerDriver              m_cepServer;
  protected CQLDataSourceFinder          m_dsFinder;
  
  public CQLProcessor()
  {
  }

  protected ICartridgeLocator setupCartridgeLocator()
    throws CEPException
  {
    BaseCartridgeRegistry registry = new CartridgeRegistryImpl();
    /*ITypeLocator typeLocator = new JavaTypeSystemImpl();
    registry.setJavaTypeSystem(typeLocator);

    try {
      JavaCartridge javaCartridge = new JavaCartridge(registry);
      javaCartridge.setTypeLocator(typeLocator);
      javaCartridge.afterPropertiesSet();

      SpatialCartridge.createInstance(registry);
    } catch(Exception e) 
    {
//      LogUtil.error("failed to create cartridge", "setupCartridgeLocator", e);
      throw new RuntimeException(e);
    }
     */
    return registry;
  }
  
  private CQLDataSourceFinder setupDataSourceFinder()
  {
	  CQLDataSourceFinder dsFinder = new CQLDataSourceFinder();
	  dsFinder.init();
	  return dsFinder;
  }
  
  public void addDataSource(String dsName, String dbURL)
  {
	  m_dsFinder.addDataSource(dsName, dbURL);
  }
  
  /**
   * starts the cqlprocessor.
   * It instantiate all required components and start them.
   * 
   * @throws CEPException
   */
  public synchronized void start() throws CEPException
  {
    m_destRegistry = new QueryDestinationRegistry();
    m_dsFinder = setupDataSourceFinder();
    m_envConfig = EnvConfig.getInstance();
    m_envConfig.setQueryDestLocator(m_destRegistry);
    m_envConfig.setCartridgeLocator(setupCartridgeLocator());
    m_envConfig.setDataSourceFinder(m_dsFinder);
    m_configMgr = new ConfigManager();
    m_configMgr.setEnvConfig(m_envConfig);
    m_configMgr.setIsRegressPushMode(true);
    m_serverRegistry = new CEPServerRegistryImpl();

    CEPManager.resetInstance();
    m_cepMgr = CEPManager.getInstance();
    m_cepMgr.setConfig(m_configMgr);
    m_cepMgr.setServerRegistry(m_serverRegistry);
    try
    {
      m_cepMgr.init();
    } catch (Exception e)
    {
      System.out.println("failed to initialize CEPServer" + " start " +  e.getMessage());
    }
    try
    {
      CEPServerXface serverx = m_serverRegistry.openConnection(getCEPServerName());
      m_cepServer = new CEPServerDriver(getCEPServerName(), serverx, m_destRegistry);
    } catch (RemoteException e)
    {
      System.out.println("Can't setup CEPServer " + "start");
    }

    /*
    DefinedFunctionLoader.loadFunction(m_cepServer);

    try
    {
      if (m_metadataMgr == null)
      {      
        if (LogUtil.isDebugEnabledForTrc(Level.FINE))
        {
          LogUtil.debugForTrc(Level.FINE, 
          		"cql metadata storage : "
              + m_configMgr.getMetadataStorageName() + ", "
              + m_configMgr.getMetadataStorageFolder(), 
              "start");
          LogUtil.debugForTrc(Level.FINE, 
              "cql data     storage : " + m_configMgr.getSpillStorageName()
               + ", " + m_configMgr.getSpillStorageFolder(),
               "start");
          LogUtil.debugForTrc(Level.FINE, 
              "cq  metadata storage : " + m_envConfig.getStorageName()
              + ", " + m_envConfig.getStorageFolder(),
              "start");
        }
        MetadataStorage storage = new MetadataBdbStorage();
        storage.init(m_envConfig);
        m_metadataMgr = new MetadataMgrImpl();
        m_metadataMgr.init(storage);
      }

      restore();
    } catch (Exception e)
    {
      LogUtil.debugForTrc(Level.SEVERE, "failed to start CQLProcessor", "start", e);
    }
    */
  }

  /**
   * closes the cqlprocessor instance.
   * This is invoked from the spring context through 'destroy-method'.
   */
  public void close()
  {
	/*
    // Stop all queries before closing the cqlprocessor.
    try
    {
      Iterator<Metadata> itr = m_metadataMgr.iterator();
      while (itr.hasNext())
      {
        Metadata obj = itr.next();
        if (obj.isQuery())
        {
          try
          {
            Query query = (Query) obj;
            CQLQueryContext ctx = new CQLQueryContext(query.getUser(), query.getStopDDL(), this);
            query.stop(ctx);
          }catch(Exception e)
          {
            LogUtil.debugForTrc(Level.SEVERE, "failed to stop query", "close", e);
          }
        }
      }
    }catch(Exception e)
    {
      LogUtil.debugForTrc(Level.SEVERE, "failed to stop query", "close", e);
    }
    */
	  
    //Closes the cqlengine instance after all queries stopped.
    try
    {
      m_cepServer.close();
    } catch(Exception e)
    {
      System.out.println("fail to close CEPServer" + "close" + e.getMessage());
    }

    /*
    if (m_metadataMgr != null)
    {
      try
      {
        m_metadataMgr.close();
      } catch(Exception e)
      {
        LogUtil.error("fail to close MedatdataManager", "close", e);
      }
    }
     */
    
    try
    {
      m_serverRegistry.close();
    } catch (Exception e)
    {
    	System.out.println("fail to close CEPServerRegistery" + "close" + e.getMessage());
    }
  }

  /**
   * restores the last state.
   * It reads all metadata from the metadata manager and process the metadata object.
   * 
   * @throws Exception
   */
  protected void restore() throws Exception
  {
	/*
    if (m_envConfig.isRestoreMetadata())
    {
      Iterator<Metadata> itr = m_metadataMgr.iterator();
      while (itr.hasNext())
      {
        Metadata obj = itr.next();
        process(obj);
      }
    }
    */
  }

  /**
   * returns the list of current used cqlengine names.
   * 
   * @return list of cqlengine server names
   */
  public String  getCEPServerName() 
  {
    return Defaults.CQLENGINE_SERVICENAME;
  }
  
  /**
   * returns the cqlengine instance for the specified name
   * 
   * @param name  cqlengine name
   * @return
   */ 
  public CEPServerDriver getCEPServer()
  {
    return m_cepServer;
  }
  
  /**
   * removes the cqleninge instance for the specified name.
   * 
   * @param serviceName
   */
  public synchronized void removeCEPServer()
  {
    if (m_cepServer != null)
    {
    	m_cepServer.close();
      try
      {
        m_serverRegistry.closeConnection(getCEPServerName());
      } catch(Exception e)
      {
      }
    }
  }
    
  public QueryDestinationRegistry getQueryDestRegistry()
  {
    return m_destRegistry;
  }

  public String createQueryDest(String id)
  {
	  QueryDestination qryDest = new QueryDestination(id);
	  m_destRegistry.register(id, qryDest);	  
	  return id;
  }
  
  public QueryDestination getQueryDest(String id)
  {
    return (QueryDestination) m_destRegistry.find(id);
  }
  
  /**
   * Wait for Scheduler Manager to stop.
   * DI Scheduler of CQL Engine runs asynchronously and spawn one thread
   * per stream to read input from files.
   * @return
   */
  public boolean waitForServerStop()
  {
    try
    {
      // Waiting for a constant amount to prevent the situation where
      // waitForServerStop is invoked before server start.
      // In that case, the test won't execute any query and will fail.
      System.out.println("Waiting for server to stop");
      Thread.sleep(1000);
      boolean isRunning = m_cepServer.isRunning();
      while(isRunning)
      {
        Thread.sleep(1000);
        isRunning = m_cepServer.isRunning();
      }
    } 
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    System.out.println("**** Server Stopped *********");
    return true;
  }
}


