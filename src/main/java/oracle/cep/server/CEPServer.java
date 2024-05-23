/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/server/CEPServer.java hopark_cqlsnapshot/2 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2007, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      12/15/15 - add snapshot api
 sbishnoi    08/01/13 - getter for CEPStats MBean
 sbishnoi    03/01/12 - fix 13774367
 anasrini    09/05/11 - XbranchMerge anasrini_bug-12943550_ps5 from
                        st_pcbpel_11.1.1.4.0
 anasrini    09/05/11 - do not extend UnicastRemoteObject
 anasrini    07/26/11 - fix in describeTableByName for views
 sbishnoi    09/19/10 - XbranchMerge sbishnoi_bug-10068411_ps3 from
                        st_pcbpel_11.1.1.4.0
 sbishnoi    09/01/10 - support input batching
 parujain    05/20/10 - remove drop schema
 alealves    05/25/10 - Log TableNotFound and StaleSource as informational and
                        not warning
 sbishnoi    02/03/10 - fix bug 9273983
 hopark      05/21/09 - add serverContext
 hopark      05/21/09 - fix utf
 sborah      04/22/09 - resolve compilation error
 skmishra    04/21/09 - adding encoding=utf-8
 skmishra    04/08/09 - adding external relation to qc xml
 hopark      03/24/09 - Use MessageCatalog
 sbishnoi    04/01/09 - setting schema prior to calling getLookupId
 sbishnoi    03/30/09 - implementing new API of CEPServerXFace
 parujain    03/25/09 - parallel ddl
 skmishra    03/13/09 - refactoring
 skmishra    03/12/09 - add more metadata to qc xml
 parujain    02/16/09 - getviewtypes
 sborah      02/22/09 - temp fix for 8256763
 parujain    01/29/09 - transaction mgmt
 skmishra    01/21/09 - adding getQCXML()
 parujain    01/15/09 - support of memstorage
 hopark      01/09/09 - add getReservedWords
 hopark      12/30/08 - fix jdbc metadata
 skmishra    12/26/08 - adding validateQuery
 parujain    12/08/08 - stats cleanup
 hopark      12/02/08 - add getLogLevel
 parujain    11/18/08 - support StatsRuntimeMBean
 hopark      11/19/08 - move main to TestServer
 hopark      11/17/08 - add setSchema
 hopark      10/15/08 - TupleValue refactoring
 skmishra    11/04/08 - adding synchronized to DDLs
 hopark      11/03/08 - fix schema
 hopark      10/09/08 - remove statics
 hopark      10/07/08 - use execContext to remove statics
 hopark      10/13/08 - fix jdbc
 hopark      09/26/08 - implement CEPServerEnvConfigurable
 parujain    09/23/08 - multiple schema
 sbishnoi    09/23/08 - incorporating changes of Constant
 sbishnoi    09/09/08 - support for schema
 skmishra    08/21/08 - import CEPServerXface
 hopark      08/01/08 - parser error handling
 hopark      05/09/08 - add main
 hopark      03/18/08 - reorg config
 rkomurav    04/18/08 - add explainplan
 mthatte     03/19/08 - jdbc reorg
 mthatte     02/25/08 - single task mode
 mthatte     11/26/07 - changing logging level to trace
 hopark      11/19/07 - localize customer log msg
 mthatte     11/26/07 - minor bug
 skmishra    11/14/07 - cleanup
 mthatte     11/06/07 - cleaning up BDB access
 mthatte     10/01/07 - cleaning stuff 
 mthatte     08/20/07 - bug fix; adding support for IStorage.getNextKey();
 mthatte     09/04/07 - 
 hopark      05/18/07 - use logging
 parujain    05/09/07 - 
 najain      04/24/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/server/CEPServer.java hopark_cqlsnapshot/2 2016/02/26 10:21:33 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.server;

import oracle.cep.colt.install.ColtAggrInstall;
import oracle.cep.colt.install.ColtInstall;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.descriptors.ArrayContext;
import oracle.cep.descriptors.ColumnMetadataDescriptor;
import oracle.cep.descriptors.TableMetadataDescriptor;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.CustomerLogMsg;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.exceptions.StaleTableSourceException;
import oracle.cep.install.Install;
import oracle.cep.logging.ILogLevelManager;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.*;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPQueryNode;
import oracle.cep.parser.LexerHelper;
import oracle.cep.parser.Parser;
import oracle.cep.service.CEPServerXface;
import oracle.cep.service.ExecContext;
import oracle.cep.service.IQueryChgListener;
import oracle.cep.service.IServerContext;
import oracle.cep.statistics.IStatsIterator;
import oracle.cep.storage.StorageException;
import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;
import oracle.xml.parser.v2.XMLDocument;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.*;
import java.util.logging.Level;

public class CEPServer implements CEPServerXface
{
  
  private static final long    serialVersionUID = 1L;
  String                       serviceName;
  ExecContext                  execContext;
   
  /**
   * Constructor
   * @throws Exception
   */
  public CEPServer(String sn, ExecContext ec) throws Exception
  {   
    this.serviceName = sn;
    this.execContext = ec;
  }
  
  public String getServiceName() {return serviceName;}
  public ExecContext getExecContext() {return execContext;}
  
  public void init()
  {
    try
    {
      execContext.init();
      LogUtil.fine(LoggerType.TRACE, "CEP Server(" + serviceName + ") started.");
    } catch (Exception e)
    {
      LogUtil.severe(LoggerType.CUSTOMER,
          CEPException.getMessage(CustomerLogMsg.SERVER_START_FAILURE, serviceName));
      LogUtil.logStackTrace(LoggerType.CUSTOMER, Level.SEVERE, e);
    }
  }

  public void close() throws RemoteException
  {
    try {
        execContext.close();
    } catch (Exception ce) {
         LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, ce);
         LogUtil.severe(LoggerType.TRACE, ce.toString());
         throw new RemoteException(ce.getMessage(), ce);
    }
  }
  
  /**
   * Accepts an unparsed DDL string from the client and executes it on the server
   */
  public synchronized int executeDDL(String cepCqlDDL, String schemaName) throws RemoteException  
  {
    LogUtil.info(LoggerType.TRACE, serviceName + "/" + schemaName + " : " + cepCqlDDL);
    Command c = execContext.getCmd();
    CommandInterpreter cmd = execContext.getCmdInt();
    execContext.setSchema(schemaName);
    LogUtil.fine(LoggerType.CUSTOMER, 
          CEPException.getMessage(CustomerLogMsg.EXEC_CQL_DDL, cepCqlDDL));
    c.setCql(cepCqlDDL);
    c.setValidate(false);
    cmd.execute(c);
    if (c.isBSuccess())
    {
        LogUtil.fine(LoggerType.CUSTOMER, 
            CEPException.getMessage(CustomerLogMsg.ACTIVATE_DDL_SUCCESS, cepCqlDDL));
    }
    else
    {
        LogUtil.fine(LoggerType.CUSTOMER, 
            CEPException.getMessage(CustomerLogMsg.ACTIVATE_DDL_FAILURE, cepCqlDDL));
        Exception ce = c.getException();
        LogUtil.severe(LoggerType.TRACE, ce.toString());
        throw new RemoteException(ce.getMessage(), ce);
    }
    return 0;
  }
 
  /**
   * Accepts a TupleValue from the client and inserts into the appropriate table
   * @return 
   */
  public int executeDML(TupleValue elem, String schemaName) throws RemoteException
  {
    return executeDMLBase(elem, schemaName, elem.getObjectName());
  }
  
  /**
   * Accepts a TupleValue from the client and inserts into the appropriate table
   * @return 
   */
  public int executeDML(TupleValue elem, String schemaName, String tableName) throws RemoteException
  {
    return executeDMLBase(elem, schemaName, tableName);
  }

  /**
   * Accepts a TupleValue from the client and inserts into the appropriate table
   * @param elem value which will be inserted
   * @param schemaName current schema
   * @param serverContext a lookup id
   * @return Integer 1 when executed successfully 
   */
  public int executeDML(TupleValue elem, String schemaName, IServerContext serverContext) 
    throws RemoteException
  {
    return executeDMLBase(null, elem, schemaName, serverContext);
  }

  private static class LookaheadIterator implements Iterator<TupleValue>
  {
	TupleValue lookahead;
	Iterator<TupleValue> iter;
	public LookaheadIterator(TupleValue f, Iterator<TupleValue> i)
	{
		lookahead = f;
		iter = i;
	}
	
	@Override
	public boolean hasNext() {
		if (lookahead != null) return true;
		return iter.hasNext();
	}

	@Override
	public TupleValue next() {
		if (lookahead != null)
		{
			TupleValue v = lookahead;
			lookahead = null;
			return v;
		}
		return iter.next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	  
  }
  
  /**
   * Accepts a batch of TupleValue from the client and inserts into thei
   * appropriate table
   * @param elemBatch batch of input tuples
   * @param schemaName current schema
   * @return 
   */
  public int executeDML(Iterator<TupleValue> elemBatch, String schemaName)
    throws RemoteException
  {
    String objectName = null;
    if(elemBatch.hasNext())
    {
      // Get objectName from TupleValue
      TupleValue firstTuple = elemBatch.next();      
      objectName = firstTuple.getObjectName();
      elemBatch = new LookaheadIterator(firstTuple, elemBatch);
    }
    else
    {
    	//Nothing to insert
    	return 0;
    }
    return executeDML(elemBatch, schemaName, objectName);
  }
  
  /**
   * Accepts a batch of TupleValue from the client and inserts into the
   * appropriate table
   * @param elemBatch batch of input tuples
   * @param schemaName current schema
   * @param serverContext a lookup id
   */ 
  public int executeDML(Iterator<TupleValue> elemBatch, 
                        String schemaName,
                        IServerContext serverContext) 
    throws RemoteException
  {
    return executeDMLBase(elemBatch, null, schemaName, serverContext);
  }


  /**
   * Accepts a batch of TupleValue from the client and inserts into thei
   * appropriate table
   * @param elemBatch batch of input tuples
   * @param schemaName current schema
   * @param objectName name of the stream which the event belongs to
   */
  public int executeDML(Iterator<TupleValue> elemBatch,
                             String schemaName,
                             String objectName) 
    throws RemoteException
  {
    //LogUtil.info(LoggerType.TRACE, "DML on the server:"+serviceName + " " + elem.toSimpleString());
    try
    {
      // Sets the current schema before executing DML
      execContext.setSchema(schemaName);      
      execContext.getExecMgr().insert(elemBatch, objectName, execContext.getSchema());

    } catch (Exception e)
    {
      LogUtil.severe(LoggerType.TRACE, e.toString());
      throw new RemoteException(e.getMessage(), e);
    }
    return 1;
  }
  

  /**
   * Helper method to process input tuples
   * @param elem single input tuple
   * @param schemaName current schema
   * @param objectName object name
   */
  private int executeDMLBase(TupleValue elem,
                        String schemaName,
                        String objectName)
          throws RemoteException
  {
    //LogUtil.info(LoggerType.TRACE, "DML on the server:"+serviceName + " " + elem.toSimpleString());
    try
    {
      // Sets the current schema before executing DML
      execContext.setSchema(schemaName);

      execContext.getExecMgr().insert(elem, objectName, execContext.getSchema());

    } catch (Exception e)
    {
      if (e.getCause() != null) {
    	  LogUtil.severe(LoggerType.TRACE, e.getCause().toString());
      }
      LogUtil.severe(LoggerType.TRACE, e.toString());
      throw new RemoteException(e.getMessage(), e);
    }
    return 1;
  }

  /**
   * Helper method to process input tuples
   * @param elemBatch batch of input tuples
   * @param elem single input tuple
   * @param schemaName current schema
   * @param serverContext a lookup id
   */
  private int executeDMLBase(Iterator<TupleValue> elemBatch, 
                             TupleValue elem, 
                             String schemaName, 
                             IServerContext serverContext) 
    throws RemoteException
  {
    //LogUtil.info(LoggerType.TRACE, "DML on the server:"+serviceName + " " + elem.toSimpleString());
    try
    {
      // Sets the current schema before executing DML
      execContext.setSchema(schemaName);
    
      if(elemBatch != null)
        execContext.getExecMgr().insertFast(elemBatch, serverContext);
      else
        execContext.getExecMgr().insertFast(elem, serverContext);

    } catch (Exception e)
    {      
      if(e instanceof CEPException)
      {
        CEPException exception = (CEPException)e;
        if(exception.getErrorCode() == oracle.cep.exceptions.InterfaceError.STALE_TABLE_SOURCE)
        {
          LogUtil.info(LoggerType.TRACE, e.toString());
          throw new RemoteException(e.getMessage(), 
              new StaleTableSourceException(e.getMessage(), e));
        }
        else
          LogUtil.severe(LoggerType.TRACE, e.toString());
      } 
      else
        LogUtil.severe(LoggerType.TRACE, e.toString());
      throw new RemoteException(e.getMessage(), e);
    }
    return 1;
  }
  
  
  public Map<String, ViewInfo> getViewInfo(String schema)
  throws RemoteException
  {
    try{
     execContext.setSchema(schema);
     execContext.setTransaction(execContext.getTransactionMgr().begin());
     return execContext.getViewMgr().getViewInfo();
    }finally
    {
      execContext.getTransactionMgr().commit(execContext.getTransaction());
    }
  }
  
  /**
   * Get runtime information for the query
   */
  public Map<String, QueryInfo> getQueryInfo(String schema)
      throws RemoteException
      {
        try
        {
         execContext.setSchema(schema);
         execContext.setTransaction(execContext.getTransactionMgr().begin());
         return execContext.getQueryMgr().getQueryInfo();
        }
        catch(CEPException e)
        {
          LogUtil.info(LoggerType.TRACE, e.toString());
          throw new RemoteException(e.getMessage(), e);
        }
        finally
        {
          execContext.getTransactionMgr().commit(execContext.getTransaction());
        }
      }
  
  @Override
  public Set<String> getSourceAttributeNamesForQueryOrView(String ruleId, String schema, boolean isView)
  throws RemoteException
  {
    try
    {
      execContext.setSchema(schema);
      execContext.setTransaction(execContext.getTransactionMgr().begin());
      
      int objId;
      
      if (!isView)
        objId = execContext.getQueryMgr().findQuery(ruleId, execContext.getSchema());
      else
        objId = execContext.getViewMgr().getViewId(ruleId, execContext.getSchema());
      
      return getSourceAttributeNames(objId);
    } 
    catch (CEPException e)
    {
      throw new RemoteException("Metadata exception: " + e.getMessage());
    }
    finally
    {
      execContext.getTransactionMgr().commit(execContext.getTransaction());
    }
  }
  
  @Override
  public Set<String> getQuerySourceNames(String queryId, String schema)
  throws RemoteException
  {
    try
    {
      execContext.setSchema(schema);
      execContext.setTransaction(execContext.getTransactionMgr().begin());
      
      int objId = 
        execContext.getQueryMgr().findQuery(queryId, execContext.getSchema());

      return getQuerySourceNames(objId);
    } 
    catch (CEPException e)
    {
      throw new RemoteException("Metadata exception: " + e.getMessage());
    }
    finally
    {
      execContext.getTransactionMgr().commit(execContext.getTransaction());
    }
  }
  
  private Set<String> getSourceAttributeNames(int id) throws MetadataException
  {
    Set<String> querySourceAttrs = new HashSet<String>();

    //
    // If it is query, get its table masters or view masters.
    //
    
    Integer [] tableIds =
      execContext.getDependencyMgr().getMasters(id, DependencyType.TABLE);

    if (tableIds != null)
      for (Integer tableId : tableIds)
      {
        Table table =
          execContext.getTableMgr().getTable(tableId);

        Collection<String> attrs = 
          table.getAttributes().keySet();

        querySourceAttrs.addAll(attrs);
      }
    
    Integer [] viewIds =
      execContext.getDependencyMgr().getMasters(id, DependencyType.VIEW);

    if (viewIds != null)
      for (Integer viewId: viewIds)
      {
        View view =
          execContext.getViewMgr().getView(viewId);

        Collection<String> attrs =
          view.getAttributes().keySet();

        querySourceAttrs.addAll(attrs);
      }
    
    // 
    // Or it could be a view, in which case we need to get its associated query.
    //
    if (tableIds == null && viewIds == null)
    {
      Integer [] queryIds =
        execContext.getDependencyMgr().getMasters(id, DependencyType.QUERY);
      
      if (queryIds != null)
      {
        assert queryIds.length == 1; // there should be only a query associated to a view.
        querySourceAttrs = getSourceAttributeNames(queryIds[0]);
      }
    }

    return querySourceAttrs;
  }
  
  private Set<String> getQuerySourceNames(int queryId) throws MetadataException
  {
    Set<String> querySources = new HashSet<String>();

    List<Integer> queries = new LinkedList<Integer>();
    queries.add(queryId);
    
    while (!queries.isEmpty())
    {
      int id = queries.remove(0);
      
      //
      // Get its table masters and view masters.
      //
      Integer [] tableIds =
        execContext.getDependencyMgr().getMasters(id, DependencyType.TABLE);

      if (tableIds != null)
        for (Integer tableId : tableIds)
        {
          Table table =
            execContext.getTableMgr().getTable(tableId);

          querySources.add(table.getName());
        }

      Integer [] viewIds =
        execContext.getDependencyMgr().getMasters(id, DependencyType.VIEW);

      if (viewIds != null)
        for (Integer viewId: viewIds)
        {
          // If view, get its associated query, and start over to compute its closure.
          Integer [] queryIds =
            execContext.getDependencyMgr().getMasters(viewId, DependencyType.QUERY);

          if (queryIds != null)
          {
            assert queryIds.length == 1; // there should be only a query associated to a view.
            queries.add(queryIds[0]);
          }
        }
    }
    
    return querySources;
  }
 
  /**
   * Initializes a handle to the namespace specified. 
   * getNext lets the client iterate through this namespace
   * @author mthatte
   * @param nameSpace the nameSpace to be described
   * @param types a filter, eg VIEW will return only views from Namespace.SOURCE
   * 
   */
  public ArrayContext describeNamespace(String nameSpace, String[] types, String schema)
      throws RemoteException
  {
    LogUtil.fine(LoggerType.TRACE, "describeNamespace(" + nameSpace
        + ") on the server:"+serviceName);
 
    // Ask the cache to initialize a cursor.
    ArrayContext ctx = new ArrayContext();
    try
    {
      execContext.setSchema(schema);
      String serviceSchema = execContext.getSchema();
      execContext.setTransaction(execContext.getTransactionMgr().begin());
      execContext.getCache().describeNameSpace
                         (ctx, nameSpace, serviceSchema,types);
      execContext.getTransactionMgr().commit(execContext.getTransaction());
    }

    // Was there a problem?
    catch (StorageException se)
    {
      execContext.getTransactionMgr().rollback(execContext.getTransaction());
      LogUtil.severe(LoggerType.TRACE, se.toString());
      throw new RemoteException("Error retrieving namespace: " + nameSpace);
    }
    finally
    {
      execContext.setTransaction(null);
    }

    return ctx;
  }

  /**
   * Ideally, fetches data about a Table whose name matches 
   * the tableName regex.
   * For now, exact match only.
   * @param tableName name of the table to lookup
   * 
   * @return an Array of MetadataDescriptors with TableMetadata of all tables
   *         that match the tableName.
   */
  public ArrayContext describeTableByName(String tableName, String schema, boolean isView)
      throws RemoteException
  {
    LogUtil.info(LoggerType.TRACE, "getTableByName() on the server:"+serviceName);
    TableManager tm = null;
    ViewManager vm = null;
    try
    {
      tm = execContext.getTableMgr();
      vm = execContext.getViewMgr();
    }

    catch (Exception e)
    {
      LogUtil.severe(LoggerType.TRACE, e.toString());
      throw new RemoteException("Table Manager missing.");
    }
    try
    {
      if (!isView && tm == null)
        return null;
      if (isView && vm == null)
        return null;
      execContext.setSchema(schema);
      String serviceSchema = execContext.getSchema();
      execContext.setTransaction(execContext.getTransactionMgr().begin());
      if (!isView)
      {
        Table tbl = tm.getTableByName(tableName, serviceSchema);
        if (tbl == null)
          return null;
        TableMetadataDescriptor desc = (TableMetadataDescriptor) tbl
            .allocateDescriptor();
        ArrayContext result = new ArrayContext();
        result.add(desc);
        execContext.getTransactionMgr().commit(execContext.getTransaction());
        return result;
      }

      else
      {
        View v = vm.getViewByName(tableName, serviceSchema);
        if (v == null)
          return null;
        TableMetadataDescriptor desc = (TableMetadataDescriptor) v
            .allocateDescriptor(execContext);
        ArrayContext result = new ArrayContext();
        result.add(desc);
        execContext.getTransactionMgr().commit(execContext.getTransaction());
        return result;
      }
    } catch (MetadataException e)
    {
      execContext.getTransactionMgr().rollback(execContext.getTransaction());
      LogUtil.severe(LoggerType.TRACE, e.toString());
      return null;
    }
    finally
    {
      execContext.setTransaction(null);
    }
  }

  /**
   * Returns an array of ColumnMetadataDescriptor's. 
   * Refer java.sql.DatabaseMetadata.getColumns() -- JDBC v. 5
   * @return Array of descriptors
   * @author mthatte
   */
  public ArrayContext describeColumns(String catalog, String schemaPattern, String tableNamePattern,
      String columnNamePattern) throws RemoteException
  {
    LogUtil.info(LoggerType.TRACE, "getColumns() on the server:"+serviceName);
    // We dont have a catalog or schema.
    if (catalog != null)
      assert catalog.equalsIgnoreCase(Constants.CEP_CATALOG);
    if (columnNamePattern != null)
      assert columnNamePattern.equals("%");
    boolean try_query = false;
    execContext.setSchema(schemaPattern);
    String serviceSchema = execContext.getSchema();
    ArrayContext ctx = new ArrayContext();
    TableManager tm = execContext.getTableMgr();
    if (tm == null)
      throw new RemoteException("No table manager in system.");
    execContext.setTransaction(execContext.getTransactionMgr().begin());
    try
    {
      ColumnMetadataDescriptor cmd;
      int position = 1;
      int id = tm.getId(tableNamePattern, serviceSchema);
      
      // If systemTimestamped || silent then do not return Timestamp as first column.
      if (tm.isClientTimeStamped(tableNamePattern, serviceSchema))
      {
        cmd = new ColumnMetadataDescriptor(tableNamePattern, "Timestamp",
            Datatype.TIMESTAMP.getSqlType(), Datatype.TIMESTAMP.getLength(),
            Datatype.TIMESTAMP.toString());
        cmd.setOrdinalPosition(position);
        ctx.add(cmd);
      }
      
      //get columns and create descriptors
      String[] attrs = tm.getAttrNames(id);
      for (String attr : attrs)
      {
        // Create new ColumnMetadataDescriptor
        int attrId = tm.getAttrId(id, attr);
        Datatype type = tm.getAttrType(id, attrId);
        int attrLen = tm.getAttrLen(id, attrId);
        position++;
        cmd = new ColumnMetadataDescriptor(tableNamePattern, attr, type
            .getSqlType(), attrLen, type.toString());
        cmd.setOrdinalPosition(position);
        ctx.add(cmd);
      }
    } catch (oracle.cep.metadata.MetadataException e)
    {
      if (e.getErrorCode() == MetadataError.OBJECT_NOT_FOUND)
      {
        try_query = true;
      }
      else
      {
        execContext.getTransactionMgr().rollback(execContext.getTransaction());
        execContext.setTransaction(null);
        LogUtil.severe(LoggerType.TRACE, e.toString());
        throw new RemoteException("Metadata exception: " + e.getMessage());
      }
    }
    /* TODO
       We need to modify Context to keep the type in it.
       As we need to fix these code any way, I am just using a hack.
    */
    if (try_query)
    {
      QueryManager qm = execContext.getQueryMgr();
      try
      {
        int qid = qm.findQuery(tableNamePattern, serviceSchema);
        String outNames[] = qm.getOutNames(qid);
        Datatype outTypes[] = qm.getOutTypes(qid);
        for (int i = 0; i < outNames.length; i++)
        {
          // Create new ColumnMetadataDescriptor
          String attr = outNames[i];
          if(attr == null)
            attr = new String("NULL");
          Datatype type = outTypes[i];
          int attrLen = 0;      //TODO??
          ColumnMetadataDescriptor cmd = new ColumnMetadataDescriptor(tableNamePattern, attr, type
              .getSqlType(), attrLen, type.toString());
          cmd.setOrdinalPosition(i);
          ctx.add(cmd);
        }
      } catch (CEPException e)
      {
        execContext.getTransactionMgr().rollback(execContext.getTransaction());
        execContext.setTransaction(null);
        LogUtil.severe(LoggerType.TRACE, e.toString());
        throw new RemoteException("Metadata exception: " + e.getMessage());
      }
    }
    execContext.getTransactionMgr().commit(execContext.getTransaction());
    execContext.setTransaction(null);
    return ctx;
  }

   
  public boolean isClientTimeStamped(String tableName, String schema) throws RemoteException
  {
    TableManager tm = execContext.getTableMgr();
    if(tm==null)
      throw new RemoteException("Table manager not present");
    execContext.setTransaction(execContext.getTransactionMgr().begin());
    try{
      execContext.setSchema(schema);
      String serviceSchema = execContext.getSchema();
      boolean ists = tm.isClientTimeStamped(tableName, serviceSchema);
      execContext.getTransactionMgr().commit(execContext.getTransaction());
      return ists;
    }catch(MetadataException me) {
      execContext.getTransactionMgr().rollback(execContext.getTransaction());
      LogUtil.severe(LoggerType.TRACE, me.toString());
      throw new RemoteException("Metadata exception");
    }
    finally
    {
      execContext.setTransaction(null);
    }
    
  }
  /**
   * Cleans up the hashtable entries associated with the connection.
   * @param ids
   */
  public void closeConnection(List<Long> ids) throws RemoteException
  {

  }
  
  public String getSchema() throws RemoteException
  {
      return execContext.getSchema();
  }
  
  public String getXMLPlan2() throws RemoteException
  {
    try
    {
      execContext.setTransaction(execContext.getTransactionMgr().begin());
      String plan = execContext.getQueryMgr().getXMLPlan2();
      execContext.getTransactionMgr().commit(execContext.getTransaction());
      return VisXMLHelper.xmlVersionEncodingTag + "\n" + plan;
    }
    catch(CEPException ex)
    {
      execContext.getTransactionMgr().rollback(execContext.getTransaction());
      LogUtil.severe(LoggerType.TRACE, ex.toString());
      LogUtil.logStackTrace(ex);
      throw new RemoteException("Get XML Plan failed");
    }
    finally
    {
      execContext.setTransaction(null);
    }
  }
  
  //'ugly' :( method to insert metadata into vis. xml.
  private String addMetadataToQCXml(String queryXml) throws Exception
  {
    String sourceTypeXml;
    try
    {
      XMLDocument doc = XMLHelper.getXMLDocument(queryXml);

      //add sourceType to Source operators
      NodeList sourceNames = doc.getElementsByTagName(VisXMLHelper.sourceNameTag);
      int sourceId = -1;
      
      for(int i = 0;i < sourceNames.getLength();i++)
      {
        boolean isView = false;
        Node sourceNameNode = sourceNames.item(i);
        String source = sourceNameNode.getTextContent();
        
        sourceId = execContext.getSourceMgr().getId(source, execContext.getSchema());
        //if it is a CacheObject.Table
        if(execContext.getSourceMgr().isTableObject(sourceId))
        {
          //is it a stream or relation?
          if(execContext.getSourceMgr().isExternal(sourceId))
          {
            sourceTypeXml = XMLHelper.buildElement(true, VisXMLHelper.sourceTypeTag, "ExtRelation", null, null);
          }
          
          else if(execContext.getSourceMgr().isStream(sourceId))
          {
            sourceTypeXml = XMLHelper.buildElement(true, VisXMLHelper.sourceTypeTag, "Stream", null, null);
          }
          
          else
            sourceTypeXml = XMLHelper.buildElement(true, VisXMLHelper.sourceTypeTag, "Relation", null, null);
        }
        
        else
        {
          sourceTypeXml = XMLHelper.buildElement(true, VisXMLHelper.sourceTypeTag, "View", null, null);
          isView=true;
        }

        //this is necessary as appending a node from a different document is not allowed
        Node childNode = XMLHelper.getXMLDocument(sourceTypeXml).getDocumentElement();
        Node importedChildNode = sourceNameNode.getOwnerDocument().importNode(childNode, true);
        sourceNameNode.getParentNode().appendChild(importedChildNode);
        
        //add view-output-type
        if(isView)
        {
          String viewOutputXml;
          String viewOutputType;
          viewOutputType= execContext.getSourceMgr().isStream(sourceId) ? "Stream" : "Relation";
          viewOutputXml = XMLHelper.buildElement(true, VisXMLHelper.viewOutputType, viewOutputType, null, null);
          //this is necessary as appending a node from a different document is not allowed
          childNode = XMLHelper.getXMLDocument(viewOutputXml).getDocumentElement();
          importedChildNode = sourceNameNode.getOwnerDocument().importNode(childNode, true);
          sourceNameNode.getParentNode().appendChild(importedChildNode);
        }
      }
     
      NodeList outputs = doc.getElementsByTagName(VisXMLHelper.outputTypeTag);
      
      //there is only one output per query/view
      assert outputs.getLength() == 1;
      
      //if output is a view, append view-schema
      String outputType = outputs.item(0).getTextContent();
      if(outputType.equals(VisXMLHelper.outputTypeView))
      {
        Node outputNameNode = doc.getElementsByTagName(VisXMLHelper.outputNameTag).item(0);
        String outputName = outputNameNode.getTextContent();
        int outputId = execContext.getSourceMgr().getId(outputName, execContext.getSchema());
        int numOutputAttrs = execContext.getSourceMgr().getNumAttrs(outputId);
        StringBuilder viewAttrs = new StringBuilder(50);
        for(int k = 0 ; k < numOutputAttrs; k++)
        {
          StringBuilder viewAttrXml = new StringBuilder(); 
          String attrName = execContext.getSourceMgr().getAttrName(outputId, k);
          String attrType = execContext.getSourceMgr().getAttrType(outputId, k).toString();
          viewAttrXml.append("\n\t\t\t" + XMLHelper.buildElement(true, VisXMLHelper.viewAttrNameTag, attrName, null, null));
          viewAttrXml.append("\n\t\t\t" + XMLHelper.buildElement(true, VisXMLHelper.viewAttrTypeTag, attrType, null, null));
          viewAttrs.append("\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.viewAttrTag, viewAttrXml.toString().trim(), null, null));
        }
        
        String viewSchema = XMLHelper.buildElement(true, VisXMLHelper.viewSchemaListTag, viewAttrs.toString().trim(), null, null);
        Node childNode = XMLHelper.getXMLDocument(viewSchema).getDocumentElement();
        Node importedChildNode = outputNameNode.getOwnerDocument().importNode(childNode, true);
        outputNameNode.getParentNode().appendChild(importedChildNode);
        
        String  viewOutputType;
        String viewOutputTypeXml;
        boolean isStreamView = execContext.getSourceMgr().isStream(sourceId);
        if(isStreamView)
          viewOutputType = "Stream";
        else
          viewOutputType = "Relation";
        viewOutputTypeXml = XMLHelper.buildElement(true, VisXMLHelper.viewOutputType, viewOutputType, null, null); 
        childNode = XMLHelper.getXMLDocument(viewOutputTypeXml).getDocumentElement();
        importedChildNode = outputNameNode.getOwnerDocument().importNode(childNode, true);
        outputNameNode.getParentNode().appendChild(importedChildNode);
      }

      ByteArrayOutputStream xmlByteArray = new ByteArrayOutputStream(1024);
      doc.print(xmlByteArray);
      return xmlByteArray.toString("UTF8");
    }
    catch(CEPException e)
    {
      throw new Exception("Not well-formed XML");
    }
  }
  
  public String getQCXML(String queryName, String schema, boolean isView) throws RemoteException
  {
    try
    {
      execContext.setSchema(schema);
      execContext.setTransaction(execContext.getTransactionMgr().begin());
      int qid = -1;
      String queryText = null;
      if(isView) {
        qid = execContext.getViewMgr().getViewId(queryName, execContext.getSchema());
        qid = execContext.getViewMgr().getView(qid).getQueryId(); //get associated query.
      }
      else
        qid = execContext.getQueryMgr().findQuery(queryName, execContext.getSchema());
      
      if(qid == -1)
        throw new Exception("Query not found");
      else
        queryText = execContext.getQueryMgr().getQuery(qid).getText();
      Parser p = execContext.getCmdInt().getParser();
      CEPParseTreeNode node = p.parseCommand(execContext, queryText);
      if(!(node instanceof CEPQueryNode))
        throw new RemoteException("Query constructor is only supported for query or view syntax. The ddl:\n " + queryText + "\nis not supported");
      String qXml = VisXMLHelper.getQCXML(queryName, queryText, isView, (CEPQueryNode)node);
      qXml = addMetadataToQCXml(qXml);
      execContext.getTransactionMgr().commit(execContext.getTransaction());
      return VisXMLHelper.xmlVersionEncodingTag + "\n" + qXml;
    }
    catch(Exception ex)
    {
      execContext.getTransactionMgr().rollback(execContext.getTransaction());
      LogUtil.severe(LoggerType.TRACE, ex.toString());
      LogUtil.logStackTrace(ex);
      if(ex.getMessage() != null)
        throw new RemoteException(ex.getMessage());
      else
        throw new RemoteException("Unsupported action. " +
        		"This CQL feature is not supported" +
        		" in this version");
    }
    finally
    {
      execContext.setTransaction(null);
    }
  }
  
  public void setSchema(String schemaName) throws RemoteException
  {
    execContext.setSchema(schemaName);
  }
  
  public void dropSchema(String schemaName) throws RemoteException
  {
    try
    {
      execContext.setSchema(schemaName);
      execContext.dropSchema(schemaName, true);
      
     // execContext.executeDDL("drop schema "+schemaName, true);
    }
    catch(CEPException ex)
    {
      LogUtil.severe(LoggerType.TRACE, ex.toString());
      LogUtil.logStackTrace(ex);
      throw new RemoteException("Failed to drop schema", ex);
    }
  }
  
  /**
   * Validates whether this query is well-defined. Used by visualizer query wizard. 
   * @return true if valid query, else throws exception
   * @throws RemoteException If the query is bad for any reason. Exception contains detailed error report.
   */
  public boolean validateQuery(String schema, String cql) throws RemoteException
  {
    execContext.setSchema(schema);
    Command c = execContext.getCmd();
    CommandInterpreter cmd = execContext.getCmdInt();
    c.setCql(cql);
    c.setValidate(true);
    cmd.execute(c);
    if (c.isBSuccess())
    {
      LogUtil.fine(LoggerType.TRACE, 
          CEPException.getMessage(CustomerLogMsg.VALIDATE_DDL_SUCCESS, cql));
      return true;
    }
    else
    {
      LogUtil.fine(LoggerType.TRACE, 
          CEPException.getMessage(CustomerLogMsg.VALIDATE_DDL_FAILURE, cql));
      Exception ce = c.getException();
      //LogUtil.severe(LoggerType.TRACE, ce.toString());
      throw new RemoteException(ce.getMessage(), ce);
    }
  }
    
  @Override
  public IStatsIterator getStatsIterator(StatType type) throws RemoteException {
     switch(type)
     {
        case Query: 
           return execContext.getStatsIteratorFactory().getQueryStatsIterator();
        case System: 
           return execContext.getStatsIteratorFactory().getSystemStatsIterator();
        case Stream: 
           return execContext.getStatsIteratorFactory().getStreamStatsIterator();
        case Operator:
           return execContext.getStatsIteratorFactory().getOperatorStatsIterator();
        case UserFunction:
           return execContext.getStatsIteratorFactory().getUserFunctionStatsIterator();
        default: return null;
     }
  }

  @Override
  public ILogLevelManager getLogLevelManager() throws RemoteException
  {
    return execContext.getLogLevelManager();
  }

  /**
   * Return the string having comma-separated values listing all aggregate
   * built-in functions
   * @return list of all built-in functions.
   * @throws RemoteException
   */
  public String getAggrSystemFunctions() throws RemoteException
  {
    Install sysFuncInstaller = execContext.getBuiltinFuncInstaller();
    String sysFuncs = sysFuncInstaller.getAggrFuncs();
    sysFuncs += ",";
    ColtInstall coltFuncInstaller = execContext.getColtInstaller();
    sysFuncs += coltFuncInstaller.getAggrFuncs();
    sysFuncs += ",";
    ColtAggrInstall coltaggrFuncInstaller = execContext.getColtAggrInstaller();
    sysFuncs += coltaggrFuncInstaller.getAggrFuncs();
    return sysFuncs;
  }
  
  /**
   * Return the string having comma-separated values listing all single-element
   * built-in functions (a.k.a scalar functions)
   * @return list of all built-in functions.
   * @throws RemoteException
   */
  public String getSingleElementSystemFunctions() throws RemoteException
  {
    Install sysFuncInstaller = execContext.getBuiltinFuncInstaller();
    String sysFuncs = sysFuncInstaller.getSingleElementFuncs();
    sysFuncs += ",";
    ColtInstall coltFuncInstaller = execContext.getColtInstaller();
    sysFuncs += coltFuncInstaller.getSingleElementFuncs();
    sysFuncs += ",";
    ColtAggrInstall coltaggrFuncInstaller = execContext.getColtAggrInstaller();
    sysFuncs += coltaggrFuncInstaller.getSingleElementFuncs();
    return sysFuncs;
  }
  
  /**
   * Return the string having comma-separated values listing all built-in functions
   * (aggregate and single-element functions)
   * @return list of all built-in functions.
   * @throws RemoteException
   */
  public String getSystemFunctions() throws RemoteException
  {
    Install sysFuncInstaller = execContext.getBuiltinFuncInstaller();
    String sysFuncs = sysFuncInstaller.getFuncs();
    sysFuncs += ",";
    ColtInstall coltFuncInstaller = execContext.getColtInstaller();
    sysFuncs += coltFuncInstaller.getFuncs();
    sysFuncs += ",";
    ColtAggrInstall coltaggrFuncInstaller = execContext.getColtAggrInstaller();
    sysFuncs += coltaggrFuncInstaller.getFuncs();
    return sysFuncs;
  }
  
  public String getReservedWords() throws RemoteException
  {
    return LexerHelper.getReservedWord();
  }

  /** 
   * this method helps to initialize prepare statement with a server context which can
   * be referred at run time for faster DML statement execution.
   * @param tableName parameter table name
   * @param schemaName parameter schema name
   */
  public IServerContext prepareStatement(String tableName, String schemaName)
   throws RemoteException
  {
    execContext.setSchema(schemaName);

    // Obtain an instance of IServerContext for given schema and table name
    IServerContext serverCtx = 
      execContext.getExecMgr().getLookUpId(tableName, execContext.getSchema());

    // In certain scenario, there are multiple QueueSource can be registered
    // against a particular channel(stream) with internally generated schema.
    // Possible Scenario:
    // Join operation of a partitioned stream with a non-partitioned stream.
    if(serverCtx == null)
    {
      execContext.setSchema(schemaName);

      // Obtain a list of schemas which are generated by CQL Engine internally
      // while registering queries involving partitioned streams
      List<String> similarSchemas 
        = execContext.getExecMgr().getAlternateSchemaNames(execContext.getSchema());

      // Get the server context if there are alternate internal schemas 
      // corresponding to the application schema
      if(similarSchemas != null && similarSchemas.size() > 0)
      {
        serverCtx = execContext.getExecMgr().getLookUpId(tableName, similarSchemas); 
      }
      return serverCtx;
    }
    else
      return serverCtx;
  }

  @Override
  public void addQueryChangeListener(String schema, IQueryChgListener notifier)
  {
    try
    {
      execContext.setSchema(schema);
      execContext.setTransaction(execContext.getTransactionMgr().begin());
      
      execContext.getQueryMgr().addQueryChgListener(notifier);
    }
    finally
    {
      execContext.getTransactionMgr().commit(execContext.getTransaction());
    }
  }
  
  @Override
  public void removeQueryChangeListener(String schema, IQueryChgListener notifier)
  {
    try
    {
      execContext.setSchema(schema);
      execContext.setTransaction(execContext.getTransactionMgr().begin());
      
      execContext.getQueryMgr().removeQueryChgListener(notifier);
    }
    finally
    {
      execContext.getTransactionMgr().commit(execContext.getTransaction());
    }
  }

  @Override
  public Object getCEPStatsMBean()
  {
    return execContext.getExecStatsMgr().getCEPStatsMBean();
  }

  @Override
  public Map<String, Object> getQueryStats(String schema, String queryId)
  {
    return execContext.getExecStatsMgr().getQueryStats(schema, queryId);
  }
  
  @Override
  public void createSnapshot(String schemaName, ObjectOutputStream output, boolean fullSnapshot)
    throws RemoteException
  {
    execContext.setSchema(schemaName);      
	  try
    {
      execContext.createSnapshot(output, fullSnapshot);
    } 
	  catch (CEPException e)
    {
      throw new RemoteException(e.getMessage(), e);
    }
  }
  
  @Override
  public void loadSnapshot(String schemaName, ObjectInputStream input, boolean fullSnapshot)
    throws RemoteException
  {
    execContext.setSchema(schemaName);      
	  try
    {
      execContext.loadSnapshot(input, fullSnapshot);
    } 
	  catch (CEPException e)
    {
      throw new RemoteException(e.getMessage(), e);
    }
  }
  
  @Override
  public void startBatch(String schemaName, boolean fullSnapshot)
    throws RemoteException
  {
    execContext.setSchema(schemaName);
    try
    {
      execContext.startBatch(fullSnapshot);  
    }
    catch(CEPException e)
    {
      throw new RemoteException(e.getMessage(), e);
    }
	  
  }

  @Override
  public void endBatch(String schemaName, boolean fullSnapshot)
    throws RemoteException
  {
    execContext.setSchema(schemaName);
    try
    {
      execContext.endBatch(fullSnapshot); 
    }
    catch(CEPException e)
    {
      throw new RemoteException(e.getMessage(), e);
    }
	  
  }
  
  @Override
  public boolean isRunning()
  {
    return execContext.isSchedulerRunning();
  }
}

