/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/SchemaManager.java hopark_cqlsnapshot/2 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2008, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    12/21/15 - adding method to return list of queries in a given
                           schema
    parujain    05/21/10 - remove drop schema ddl
    parujain    12/07/09 - synonym
    hopark      03/01/09 - drop builtin func/windows
    parujain    01/28/09 - Transaction mgmt
    hopark      12/02/08 - move LogLevelManager to ExecContext
    hopark      11/04/08 - fix schema
    parujain    09/30/08 - drop schema
    parujain    09/30/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/SchemaManager.java hopark_cqlsnapshot/2 2016/02/26 10:21:33 hopark Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata;

import java.util.LinkedList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import oracle.cep.metadata.cache.Cache;
import oracle.cep.metadata.cache.CacheKey;
import oracle.cep.metadata.cache.CacheObject;
import oracle.cep.metadata.cache.NameSpace;
import oracle.cep.metadata.comparator.CacheObjectReverseComparator;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.storage.IStorageContext;
import oracle.cep.service.ExecContext;
import oracle.cep.transaction.ITransaction;
import oracle.cep.util.DebugUtil;
import oracle.cep.exceptions.CEPException;

@DumpDesc(autoFields=true)
public class SchemaManager extends CacheObjectManager
{
  ExecContext execContext;
  public SchemaManager(ExecContext ec, Cache cache)
  {
    super(ec.getServiceManager(), cache);
    execContext = ec;
  }

  public void dropSchema(String schemaName)
  throws CEPException
  {
    ITransaction txn = null;
    ITransaction oldTxn = null;

    try
    {
      oldTxn = execContext.getTransaction();
      txn = execContext.getTransactionMgr().begin();
      execContext.setTransaction(txn);
      dropSchemaInternal(schemaName);
      execContext.getTransactionMgr().commit(txn);
    }
    catch(CEPException ce)
    {
      if(txn != null)
        execContext.getTransactionMgr().rollback(txn);
      String cql = new String("drop schema " +schemaName);
      LogUtil.severe(LoggerType.CUSTOMER, cql);
       
      String msg = execContext.getCmdInt().getErrMsg(ce, cql);
      LogUtil.severe(LoggerType.CUSTOMER, msg);
       
      LogUtil.fine(LoggerType.TRACE, DebugUtil.getStackTrace(ce));
        
      throw ce;
    }
    finally
    {
      execContext.setTransaction(oldTxn);
    } 
  }
  
  
  private void dropSchemaInternal(String schemaName)
    throws CEPException
  {
   // String schemaName = n.getName();
    if(schemaName == null)
      return;
    
    if (storage == null)
      return;
   
    schemaName = execContext.getServiceSchema(schemaName); 
    LinkedList<CacheObject> list = new LinkedList<CacheObject>();
    NameSpace[] nSp = NameSpace.values();
    for(int i=0; i< nSp.length; i++)
    {
      NameSpace ns = nSp[i];
      switch(ns)
      {
        case QUERY: 
          {
            IStorageContext context = storage.initQuery(ns.name(), schemaName);
            Query qry = (Query)storage.getNextRecord(context);
            while(qry != null)
            { // it will be dropped as part of view
              if(qry.isNamed)
                list.add(qry);
	      qry = (Query)storage.getNextRecord(context);
	    }
            break;
          }
        case SOURCE:
        case USERFUNCTION:
        case WINDOW: 
        case SYNONYM:
          {
            IStorageContext ctx = storage.initQuery(ns.name(), schemaName);
            CacheObject obj = (CacheObject)storage.getNextRecord(ctx);
            while(obj != null)
            {
              list.add(obj);
              obj = (CacheObject)storage.getNextRecord(ctx);
            }
            break;
          }
        default: break;
      }
    }
    // sort the list
    Collections.sort(list, new CacheObjectReverseComparator());
    Iterator<CacheObject> iter = list.iterator();
    while(iter.hasNext())
    {
      CacheObject obj = iter.next();

      LogUtil.info(LoggerType.TRACE,
                   "Dropping " + schemaName + "." + (String)obj.getKey()
                   + " of object type " + obj.getType());

      switch(obj.getType())
      {
        case TABLE: 
          execContext.getTableMgr().dropTable(
                                  (String)obj.getKey(), schemaName,
                                   ((Table)obj).isBStream());
        	        break;
        case QUERY: 
                     // unnamed queries will be dropped as part of view
          execContext.getQueryMgr().dropNamedQuery
                                   ((String)obj.getKey(), schemaName);
                     break;
        case VIEW: 
          execContext.getViewMgr().dropView
                                              ((String)obj.getKey(),schemaName);
                     break;
        case SINGLE_FUNCTION:
        case AGGR_FUNCTION: 
          execContext.getUserFnMgr().dropSchemaFunction
                                            ((String)obj.getKey(), schemaName); 
        	         break;
        case SIMPLE_FUNCTION_SET: 
          execContext.getUserFnMgr().dropSchemaFunctionSet
                                            ((String)obj.getKey(), schemaName); 
        	         break;
        case WINDOW:
          execContext.getWindowMgr().dropWindow
                                            ((String)obj.getKey(), schemaName);
        	         break;
        case SYNONYM:
          execContext.getSynonymMgr().dropSynonym((String)obj.getKey(), schemaName);
          break;
        default: break;
      }
    }
  }

  public void dropBuiltinObjs(String schemaName)
  {
    if (storage == null)
      return;
   
    schemaName = execContext.getServiceSchema(schemaName); 
    LinkedList<CacheObject> list = new LinkedList<CacheObject>();
    NameSpace[] nSp = NameSpace.values();
    for(int i=0; i< nSp.length; i++)
    {
      NameSpace ns = nSp[i];
      switch(ns)
      {
        case USERFUNCTION:
        case WINDOW: 
          {
            IStorageContext ctx = storage.initQuery(ns.name(), schemaName);
            CacheObject obj = (CacheObject)storage.getNextRecord(ctx);
            while(obj != null)
            {
              list.add(obj);
              obj = (CacheObject)storage.getNextRecord(ctx);
            }
            break;
          }
      }
    }
    Iterator<CacheObject> iter = list.iterator();
    while(iter.hasNext())
    {
      CacheObject obj = iter.next();
      CacheKey ckey = obj.getCacheKey();
      try
      {
        //System.out.println("delete "+ckey.getNameSpace().toString() + " "  + ckey.getSchema() + " "+ckey.getObjectName());
        storage.deleteRecord(null, ckey.getNameSpace().toString(), obj.getSecondaryIndexKey(), ckey);
      }
      catch(Exception e)
      {
        LogUtil.warning(LoggerType.TRACE, e.toString());
      }
      try
      {
        int id = obj.getId();
        CacheKey objidkey = new CacheKey(id, NameSpace.OBJECTID);
        CacheObject objid = (CacheObject) storage.getRecord(null, NameSpace.OBJECTID.toString(), objidkey);
        if (objid != null)
        {
          storage.deleteRecord(null, NameSpace.OBJECTID.toString(), null, objid.getKey());
        }
      }
      catch(Exception e)
      {
        LogUtil.warning(LoggerType.TRACE, e.toString());
      }
    }
  }
 
  /**
   * Returns a lists all cache objects of given type in given schema
   * @param schemaName
   * @param type
   * @return null if there is no elements of given type in the metadata 
   *              which belongs to given schema
   *         list of ids of all qualifying cache objects 
   */
  public List<Integer> getCacheObjectsNames(String schemaName, NameSpace type)
  {
    //System.out.println("Invoking list of cache objects for schema:" + schemaName + " and type:" + type);
    if (storage == null)
      return null;

    LinkedList<Integer> objectNames = new LinkedList<Integer>();
    schemaName = execContext.getServiceSchema(schemaName);
    IStorageContext context = storage.initQuery(type.name(), schemaName);
    CacheObject obj = (CacheObject)storage.getNextRecord(context);
    
    while(obj != null)
    {
      objectNames.add(obj.getId());
      obj = (CacheObject)storage.getNextRecord(context);
    }
    
    return objectNames;
  }
  
  public void dump()
  {
    LogLevelManager lm = execContext.getLogLevelManager();
    IDumpContext dumper = lm.openDumper(null, null);
    String tag1 = LogUtil.beginDumpObj(dumper, this);
    LogUtil.endDumpObj(dumper, tag1);
    lm.closeDumper(null, null, dumper);
  }
}
