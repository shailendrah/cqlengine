package oracle.cep.test.ha.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import oracle.cep.interfaces.output.QueryOutput;
import oracle.cep.service.IQueryDestLocator;

/**
 * QueryDestinationRegistry represents the registry of query destination by implementing cqlengine's IQueryDestLocator.
 * 
 *  @version $Header: beam/main/modules/cqservice/core/src/main/java/com.oracle.cep.spark/QueryDestinationRegistry.java /main/3 2012/04/07 20:02:54 hopark Exp $
 *  @author  hopark  
 *  @since   12c
 */

public class QueryDestinationRegistry implements IQueryDestLocator
{
  Map<String, QueryOutput> m_registry;
  /*
   * OCEP maintains different registry, but I don't think we need them
  Map<String, QueryOutput> m_batchRegistry;
  Map<String, QueryOutput> m_hbRegistry;
  Map<String, QueryOutput> m_batchHbRegistry;
   */
  
  public QueryDestinationRegistry()
  {
    m_registry = new ConcurrentHashMap<String, QueryOutput>();
    /*
    m_batchRegistry = new ConcurrentHashMap<String, QueryOutput>();
    m_hbRegistry = new ConcurrentHashMap<String, QueryOutput>();
    m_batchHbRegistry = new ConcurrentHashMap<String, QueryOutput>();
    */
  }
  
  public void register(String id, QueryOutput qryDest)
  {
    m_registry.put(id, qryDest);
  }

  public void register(String id, QueryOutput qryDest, boolean isBatchEvents, boolean propagateHb)
  {
    register(id, qryDest);
    /*
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
      */
  }

  public  void deregister(String id)
  {
    m_registry.remove(id);
    /*
    m_batchRegistry.remove(id);
    m_batchHbRegistry.remove(id);
    m_hbRegistry.remove(id);
    */
  }
  
  @Override
  public  QueryOutput find(String id)
  {
    return m_registry.get(id);
  }

  @Override
  public  QueryOutput find(String id, boolean isBatchEvents)
  {
    return find(id);
    //return isBatchEvents ? m_batchRegistry.get(id) : m_registry.get(id);
  }
  
  @Override
  public  QueryOutput find(String id, boolean isBatchEvents, boolean propagateHb)
  {
    return find(id);
    /*
    if (isBatchEvents)
        return propagateHb ? m_batchHbRegistry.get(id):m_batchRegistry.get(id);
    else 
        return propagateHb ? m_hbRegistry.get(id):m_registry.get(id);
*/        
  }
}
