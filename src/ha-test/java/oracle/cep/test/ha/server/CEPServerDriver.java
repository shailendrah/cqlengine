package oracle.cep.test.ha.server;

import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.service.CEPServerXface;
import oracle.cep.logging.LogUtil;
/**
 * CEPServerDriver represents a CEPServerXFace instance.
 * 
 *  @version %I%, %G%
 *  @author  hopark  
 *  @since   12c
 */

public class CEPServerDriver
{
  public enum MetadataType {TABLE, VIEW, QUERY};
  
  private CEPServerXface m_cepServer;
  private QueryDestinationRegistry m_qryDestReg;
  
  private String         m_serviceName;
  private Set<String>    m_schemas;
  
  public CEPServerDriver(String serviceName, CEPServerXface server, QueryDestinationRegistry qryDestReg)
  {
    m_serviceName = serviceName;
    m_cepServer = server;
    m_qryDestReg = qryDestReg;
    m_schemas = new HashSet<String>();
  }

  public void close()
  {
    m_cepServer = null;
  }

  public String getServiceName() {return m_serviceName;}
  public Collection<String> getSchemas() {return m_schemas;}

  /**
   * formats a DDL with the given arguments and executes the DDL.
   * 
   * @param format - the pattern for this message format.
   * @param args - the variable arguments of objects to be formatted and substituted.
   * @throws RemoteException - if the execution of ddl fails from the CEPServer.
   */
  public synchronized void executeDDL(String schema, String format, Object... args) throws Exception
  {
    String ddl;
    if (args.length > 0)
    {
      ddl = MessageFormat.format(format, args);
    }
    else
    {
      ddl = format;
    }

    System.out.println("Execute DDL " + ddl );

    //Setting the same classloader in order to provide access to user functions defined in CQService.
    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

    m_cepServer.executeDDL(ddl, schema);


  	System.out.println("Finish executing DDL " + ddl);

    if (!m_schemas.contains(schema))
    {
      m_schemas.add(schema);
    }
  }

  /**
   * sends a tuple to CEPServer.
   * 
   * @param tuple - the tuple to send 
   * @return
   * 
   * @throws RemoteException - if the dml operation fails from CEPServer.
   */
  public int executeDML(String schema, TupleValue tuple) 
  {
    String objName = tuple.getObjectName();
    //String schema = "myschema";//QualifiedName.getSchema(objName);
    String name =objName;// QualifiedName.getName(objName);
    tuple.setObjectName(name);   
    /*if (LogUtil.isDebugEnabledForTrc())
    {
      LogUtil.debugForTrc(m_serviceName+"/"+schema + " execute DML [" + tuple.toSimpleString() + "]", "executeDML");
    }*/
    int result;
    try
    {
      result = m_cepServer.executeDML(tuple, schema);
    } catch (Throwable ex)
    {
      //LogUtil.error( "Unable to execute DML ", "executeDML", ex);
      throw new RuntimeException("UnableToExecuteDML", ex);
    }
    /*if (LogUtil.isDebugEnabledForTrc())
    {
      LogUtil.debugForTrc("Finish execute DML [" + tuple.toSimpleString() + "]", "executeDML");
    }*/
    return result;
  }

  /**
   * sends a collection of tuples to CEPServer.
   * 
   * @param tuple - the tuples to send 
   * @return
   * 
   * @throws RemoteException - if the dml operation fails from CEPServer.
   */
  public int executeDML(Collection<TupleValue> tuples) 
  {
    if (tuples.size() == 1)
    {
      return executeDML("myschema", tuples.iterator().next());
    }
    String schema = null;
    for (TupleValue tuple : tuples)
    {
      String objName = tuple.getObjectName();
      schema = "myschema";
      String name = objName;// QualifiedName.getName(objName);
      tuple.setObjectName(name);
    }

//    if (LogUtil.isDebugEnabledForTrc())
//    {
//      LogUtil.debugForTrc(m_serviceName+"/"+schema + " execute DML [" + tuples + "]", "executeDML");
//    }

    int result;
    try
    {
      result = m_cepServer.executeDML(tuples.iterator(), schema);
    } catch (Throwable ex)
    {
//      LogUtil.error("Unable to execute DML ", "executeDML", ex);
      throw new RuntimeException("UnableToExecuteDML", ex);
    }

//    if (LogUtil.isDebugEnabledForTrc())
//    {
//      LogUtil.debugForTrc("Finish execute DML [" + tuples + "]", "executeDML");
//    }

    return result;
  }  
 
  public void createSnapshot(String schemaName, ObjectOutputStream output, boolean fullSnapshot)
  {
    try
    {
      m_cepServer.createSnapshot(schemaName,output,fullSnapshot);
    }
    catch(Throwable ex)
    {
      throw new RuntimeException("Unable to execute createSnapshot", ex);
    }
  }
    
  public void loadSnapshot(String schemaName, ObjectInputStream input, boolean fullSnapshot)
  {
    try
    {
      m_cepServer.loadSnapshot(schemaName,input,fullSnapshot);
    }
    catch(Throwable ex)
    {
      throw new RuntimeException("Unable to execute loadSnapshot", ex);
    }
  }
  
  public void startBatch(String schemaName, boolean fullSnapshot)
  {
    try 
    {
      m_cepServer.startBatch(schemaName, fullSnapshot);
    } 
    catch (Throwable ex) 
    {
      throw new RuntimeException("Unable to execute startBatch", ex);
    }
  }
  
  public void endBatch(String schemaName, boolean fullSnapshot)
  {
    try
    {
      m_cepServer.endBatch(schemaName, fullSnapshot);
    }
    catch(Throwable ex)
    {
      throw new RuntimeException("Unable to execute endBatch", ex);
    }
  }

  public Iterable<TupleValue> getOutputs(String schema)
  {
	  return null;
  }

  
  /**
   * drops the schema from the cqlengine
   * 
   * @param schemaName
   */
  public void dropSchema(String schemaName)
  {
    try
    {
      m_cepServer.dropSchema(schemaName);
    }
    catch(RemoteException e)
    {
//      LogUtil.error( "Unable to drop schema " + schemaName, "dropSchema", e);
    }
  }
  
  public QueryDestinationRegistry getQueryDestRegistry()
  {
    return m_qryDestReg;
  }

  public CEPServerXface getCQLEngine() 
  {
    return m_cepServer;
  }

  public boolean validateQuery(String schema, String ddl) throws RemoteException
  {
    return m_cepServer.validateQuery(schema, ddl);
  }
  
  public String dumpQueryPlan() throws RemoteException
  {
    return m_cepServer.getXMLPlan2();  
  }
  
  /**
   * Return true if the Scheduler Manager of CQL Engine is running
   * @return
   */
  public boolean isRunning()
  {
    return m_cepServer.isRunning();
  }
}

