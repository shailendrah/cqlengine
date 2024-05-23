/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/server/CEPServerRegistryImpl.java /main/6 2011/06/14 04:42:26 udeshmuk Exp $ */

/* Copyright (c) 2008, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    06/13/11 - XbranchMerge udeshmuk_bug-10044635_ps5 from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    05/27/11 - move the entire createServer method in synchronized
                           block
    hopark      03/24/09 - Use MessageCatalog
    hopark      11/22/08 - remove default sys creation
    hopark      10/28/08 - system service creation on demand
    hopark      10/20/08 - Creation
 */

package oracle.cep.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import oracle.cep.common.Constants;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.CustomerLogMsg;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.CEPManager;
import oracle.cep.service.CEPServerRegistry;
import oracle.cep.service.CEPServerXface;
import oracle.cep.service.ExecContext;
import oracle.cep.util.DebugUtil;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/server/CEPServerRegistryImpl.java /main/4 2009/04/03 07:40:37 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

public class CEPServerRegistryImpl
  implements CEPServerRegistry
{
  private CEPManager             cepMgr;
  private CEPServer              systemServer = null;
  private Map<String, ServerHandle> serverMap;
  private boolean                createSystemServer = false;
  
  private static class ServerHandle
  {
    int refCount;
    CEPServer server;
    
    ServerHandle(CEPServer server)
    {
      this.server = server;
      refCount = 1;
    }
    
    void addRef() {refCount++;}
    int release() {return --refCount;}
  }
    
  public CEPServerRegistryImpl()
  {
    serverMap = new HashMap<String, ServerHandle>();
  }
  
  public void setCreateSystemServer(boolean b) {createSystemServer = b;}
  
  public void init(CEPManager cepMgr)
    throws Exception
  {
    this.cepMgr = cepMgr;
    if (createSystemServer)
    {
      try
      {
        systemServer = createServer(Constants.DEFAULT_CEP_SERVICE_NAME);
      }
      catch(Exception e)
      {
        LogUtil.severe(LoggerType.CUSTOMER, "Failed to create the default cep service");
        throw e;
      }
    }
  }

  public void close()
  {
    Collection<ServerHandle> nodes = serverMap.values();
    for (ServerHandle node : nodes)
    {
      try
      {
        node.server.close();
      }
      catch(Exception e)
      {
        LogUtil.warning(LoggerType.TRACE, "Failed to close : " + node.server.getServiceName());
      }
    }
  }
  
  public CEPServerXface openConnection(String serviceName)
      throws RemoteException
  {
    CEPServerXface server = getServer(serviceName);
    if (server == null)
    {
      try
      {
        server = createServer(serviceName);
        return server;
      }
      catch(Exception e)
      {
        LogUtil.severe(LoggerType.CUSTOMER, 
            CEPException.getMessage(CustomerLogMsg.INIT_COMPONENT, serviceName));
        LogUtil.severe(LoggerType.TRACE, e.toString());
        throw new RemoteException(e.getMessage(), e);
      }
    }
    return server;
  }

  public void closeConnection(String serviceName) throws RemoteException
  {
    removeServer(serviceName);
  }

  public CEPServer createServer(String serviceName)
    throws Exception
  {
    synchronized(serverMap)
    {
      ServerHandle s = serverMap.get(serviceName);
      if (s != null)
      {
        s.addRef();
        return s.server;
      }
      
      LogUtil.info(LoggerType.TRACE, "create new cep service : " + serviceName);
      ExecContext ec = new ExecContext(serviceName, cepMgr);
      CEPServer server = new CEPServer(serviceName, ec);
      server.init();
      serverMap.put(serviceName, new ServerHandle(server));
      return server;
    }
  }
  
  public synchronized void removeServer(String serviceName)
  {
    ServerHandle s = null;
    boolean remove = false;
    synchronized(serverMap)
    {
      s = serverMap.get(serviceName);
      if (s != null)
      {
        int rc = s.release();
        remove = (rc == 0);
      }
    }
    if (remove)
    {
      //We should not remove 'sys' service.
      if (!serviceName.equals(Constants.DEFAULT_CEP_SERVICE_NAME))
      {
        LogUtil.info(LoggerType.TRACE, "closing cep service : " + serviceName);
        try
        {
          s.server.close();
        }
        catch(Exception e)
        {
          LogUtil.warning(LoggerType.TRACE, "Failed to close " + serviceName + "\n" + e.toString());
        }
        synchronized(serverMap)
        {
          serverMap.remove(serviceName);
        }
      }
    }
  }
  
  public CEPServer getServer(String serviceName)
  {
    synchronized(serverMap)
    {
      ServerHandle s = serverMap.get(serviceName);
      if (s != null)
      {
        return s.server;
      }
    }
    return null;
  }
  
  public CEPServer getSystemServer()
  {
    if (systemServer == null)
    {
      try
      {
        systemServer = createServer(Constants.DEFAULT_CEP_SERVICE_NAME);
      }
      catch(Exception e)
      {
        LogUtil.severe(LoggerType.CUSTOMER, "Failed to create the default cep service: " + e.getMessage());
      }
    }
    return systemServer;
  }

  public Collection<CEPServer> getServers()
  {
    synchronized(serverMap)
    {
      Collection<ServerHandle> nodes = serverMap.values();
      ArrayList<CEPServer> r = new ArrayList<CEPServer>();
      for (ServerHandle node : nodes)
      {
        r.add(node.server);
      }
      return r;    
    }
  }
}
