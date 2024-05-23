/* $Header: cep/wlevs_cql/modules/cqlengine/standaloneEnv/src/oracle/cep/env/standalone/CQLEngine.java /main/1 2011/05/19 15:28:45 hopark Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    CQLEngine is the helper class for customer who want to avoid using spring
    application context.

   PRIVATE CLASSES
    QueryDestirnationRegistry - simple query destination registry

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      03/01/11 - Creation
 */

/**
 *  @version $Header: CQLEngine.java 01-mar-2011.11:42:24 hopark   Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.env.standalone;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import oracle.cep.server.CEPServer;
import oracle.cep.server.CEPServerRegistryImpl;
import oracle.cep.service.CEPManager;
import oracle.cep.service.CEPServerRegistry;
import oracle.cep.service.IQueryDestLocator;
import oracle.cep.interfaces.output.QueryOutput;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.ConfigManager;

public class CQLEngine
{
  EnvConfig             m_envConfig;
  ConfigManager         m_configMgr;
  QueryDestinationRegistry	m_destRegistry;
  CEPServerRegistryImpl m_serverRegistry;
  CEPManager            m_cepMgr;

  public CQLEngine(EnvConfig envCfg) throws Exception
  {
    m_envConfig = envCfg;
    m_destRegistry = new QueryDestinationRegistry() ;
    m_envConfig.setQueryDestLocator(m_destRegistry);

    m_configMgr = new ConfigManager();
    m_configMgr.setEnvConfig(m_envConfig);

    m_serverRegistry = new CEPServerRegistryImpl();
    m_serverRegistry.setCreateSystemServer(true);

    m_cepMgr = CEPManager.getInstance();
    m_cepMgr.setConfig(m_configMgr);
    m_cepMgr.setServerRegistry(m_serverRegistry);
    try
    {
      m_cepMgr.init();
    }
    catch(Exception e)
    {
      if (LogUtil.isSevereEnabled(LoggerType.CUSTOMER))
      {
    	  LogUtil.logStackTrace(e);
      }
      throw e;
    }
  }

  public void close()
  {
	  m_serverRegistry.close();
  }

  public CEPServer getServer()
  {
	 return m_serverRegistry.getSystemServer();
  }
  
  public CEPServerRegistry getServerRegistry()
  {
	  return m_serverRegistry;
  }
  
  public void registerQueryOutput(String id, QueryOutput qryDest)
  {
	  m_destRegistry.register(id, qryDest);
  }

  public void register(String id, QueryOutput qryDest, boolean isBatchEvents, boolean propagateHb)
  {
	  m_destRegistry.register(id, qryDest, isBatchEvents, propagateHb);
  }

  public void deregisterQueryOutput(String id)
  {
	  m_destRegistry.deregister(id);
  }  
  
  private static class QueryDestinationRegistry implements IQueryDestLocator
  {
    Map<String, QueryOutput> m_registry;
    Map<String, QueryOutput> m_batchRegistry;
    Map<String, QueryOutput> m_hbRegistry;
    Map<String, QueryOutput> m_batchHbRegistry;
    
    public QueryDestinationRegistry()
    {
      m_registry = new ConcurrentHashMap<String, QueryOutput>();
      m_batchRegistry = new ConcurrentHashMap<String, QueryOutput>();
      m_hbRegistry = new ConcurrentHashMap<String, QueryOutput>();
      m_batchHbRegistry = new ConcurrentHashMap<String, QueryOutput>();
    }
    
    public void register(String id, QueryOutput qryDest)
    {
      m_registry.put(id, qryDest);
    }

    public void register(String id, QueryOutput qryDest, boolean isBatchEvents, boolean propagateHb)
    {
        if (isBatchEvents)
        {
      	  if (propagateHb)  m_batchHbRegistry.put(id, qryDest);
      	  else m_batchRegistry.put(id, qryDest);
        }
        else 
        {
        	if (propagateHb) m_hbRegistry.put(id, qryDest);
        	else m_registry.put(id, qryDest);
        }
    }

    public  void deregister(String id)
    {
      m_registry.remove(id);
      m_batchRegistry.remove(id);
      m_batchHbRegistry.remove(id);
      m_hbRegistry.remove(id);
    }
    
    @Override
    public  QueryOutput find(String id)
    {
      return m_registry.get(id);
    }

    @Override
    public  QueryOutput find(String id, boolean isBatchEvents)
    {
      return isBatchEvents ? m_batchRegistry.get(id) : m_registry.get(id);
    }
    
    @Override
    public  QueryOutput find(String id, boolean isBatchEvents, boolean propagateHb)
    {
      if (isBatchEvents)
    	  return propagateHb ? m_batchHbRegistry.get(id):m_batchRegistry.get(id);
      else 
    	  return propagateHb ? m_hbRegistry.get(id):m_registry.get(id);
    }
  }
}

