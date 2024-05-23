/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/storage/mem/MemStorage.java /main/6 2011/05/19 15:28:45 hopark Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/03/11 - Creation
 */

/**
 *  @version $Header: MemDB.java 03-apr-2011.16:43:18 hopark   Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/storage/mem/MemStorage.java /main/6 2011/05/19 15:28:45 hopark Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    MemStorage is simple hashmap based storage.
    It's main purpose is to make evs tool validation faster.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      03/31/11 - storage refactor
    parujain    01/28/09 - Transaction mgmt
    parujain    01/13/09 - metadata in-memory
    hopark      09/16/08 - add schema indexing
    hopark      08/14/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/storage/mem/MemStorage.java /main/5 2009/03/19 20:24:41 parujain Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.storage.mem;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.storage.BaseStorage;
import oracle.cep.storage.IStorageContext;
import oracle.cep.storage.StorageException;
import oracle.cep.util.CloneCreator;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/storage/mem/MemStorage.java /main/5 2009/03/19 20:24:41 parujain Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
public class MemStorage extends BaseStorage
{
    String m_name;
    private HashMap<String, MemDb> m_nsMap;
    
  private static class MemStorageObject 
  {
    Object indexKey;
     
    Object data;
    
    MemStorageObject(Object k, Object v)
    {
      indexKey = k;
      data = v;
    }
    
    Object getIndexKey()
    {
      return indexKey;
    }
    
    Object getData()
    {
      return data;
    }
    
    public MemStorageObject deepCopy() throws Exception {
      Object indexCopy = null;
      Object dataCopy = null;
      if(indexKey != null)
      { // Currently indexkey is string for us. 
        indexCopy = CloneCreator.cloneObject(indexKey);
      }
      if(data != null)
      {
       // dataCopy = CloneCreator.cloneObject(data);
        Method m1 = data.getClass().getMethod("clone");
        dataCopy = m1.invoke(data);
      }
      // NOTE: IF we dont want to use clonecreator which requires class to be
      // serializable then we have two options:Reflection or a common interface
      // Reflection method is following which again has an overhead
      //  Method method = oldObj.getClass().getMethod("clone");
      //  clonedObj = method.invoke(oldObj);
      return new MemStorageObject(indexCopy, dataCopy);
    }
  }

  private static class MemDb
  {
    //store maintains key,data records
    HashMap<Object, MemStorageObject> m_store;
    //index maintains secondaryKey, (key,data)
   // HashMap<Object, List<Object>> m_index = null;
    HashMap<Object, HashMap<Object, MemStorageObject>> m_index = null;
    
    MemDb(Class<?> indexKeyClass)
    {
      m_store = new HashMap<Object, MemStorageObject>();
      if (indexKeyClass != null)
        m_index = new HashMap<Object, HashMap<Object, MemStorageObject>>();
    }
    
    synchronized MemStorageObject get(Object k)
    {
      return m_store.get(k);
    }
    
    @SuppressWarnings("unused")
  synchronized Object getData(Object k)
    {
      MemStorageObject obj = m_store.get(k);
      if(obj != null)
        return obj.getData();
      
      return null;
    }
    
    synchronized HashMap<Object, MemStorageObject> getIndex(Object indexKey)
    {
      assert (m_index != null);
      return m_index.get(indexKey);
    }
    
    synchronized void put(Object indexKey, Object k, Object v)
    {
     // m_store.put(k, new MemStorageObject(indexKey, v));
      MemStorageObject memObj = new MemStorageObject(indexKey, v);
      m_store.put(k, memObj);
      if (indexKey != null)
      {
        assert (m_index != null);
        HashMap<Object, MemStorageObject> sobjs = m_index.get(indexKey);
        if (sobjs == null)
        {
          sobjs = new HashMap<Object, MemStorageObject>();
          m_index.put(indexKey, sobjs);
        }
        sobjs.put(k, memObj);
       // sobjs.add(k);
      }
    }
    
    synchronized Object remove(Object indexKey, Object k)
    {
      Object v = m_store.remove(k);
      if (indexKey != null)
      {
        assert (m_index != null);
        HashMap<Object, MemStorageObject> sobjs = m_index.get(indexKey);
        if (sobjs != null)
        {
          sobjs.remove(k);
        }
      }
      return v;
    }
    
    synchronized void clear()
    {
      m_store.clear();
      if (m_index != null)
      {
        Set<Map.Entry<Object, HashMap<Object, MemStorageObject>>> entries = 
                                                            m_index.entrySet();
        for (Map.Entry<Object, HashMap<Object, MemStorageObject>> entry : entries)
        {
          HashMap<Object, MemStorageObject> sobjs = entry.getValue();
          sobjs.clear();
        }
        m_index.clear();
      }
    }
  }
  
  public MemStorage(String name)
  {
    m_name = name;
    m_nsMap = new HashMap<String, MemDb>();
  }
  
  @Override
  public String getName() 
  {
    return m_name;
  }

  @Override
  public void addNameSpace(String classDBNS, String ns,
      boolean transactional, Class<?> indexKeyClass, Class<?> keyClass,
      Class<?> objClass) throws StorageException 
  {
    LogUtil.finest(LoggerType.TRACE, "MemStorage addNameSpace " + ns + 
        " transactional: " + transactional +
        " indexKeyClass: " + (indexKeyClass==null ? "null" : indexKeyClass.getSimpleName()) +
        " keyClass: " + (keyClass==null ? "null" : keyClass.getSimpleName()) +
        " objClass: " + (objClass==null ? "null" : objClass.getSimpleName()) );
    MemDb store = m_nsMap.get(ns);
    assert (store == null);
    store = new MemDb(indexKeyClass);
    m_nsMap.put(ns, store);
  }
  
  protected void finalize() throws Throwable
  {
    try
    {
      Collection<MemDb> entries = m_nsMap.values();
      for (MemDb store : entries)
      {
        store.clear();
      }
      m_nsMap.clear();
    }
    finally 
    {
      super.finalize();
    }
  }

  protected boolean putRecordImpl(IStorageContext storageContext, 
      String nameSpace, Object indexKey, Object key, Object data)
  {
    MemDb store = m_nsMap.get(nameSpace);
    assert (store != null);
    MemStorageObject obj = store.get(key);
    // First remove old versions of the object in both store and indexStore.
    // And then update again
    if(obj != null)
      store.remove(obj.getIndexKey(), key);
    
    // atleast one of them is not null
    // Both can be null in case of rollback of a newly created object
    // since their previous versions will be null
    if((indexKey != null) || (data!= null))
      store.put(indexKey, key, data);
    return true;
  }

  protected Object getRecordImpl(IStorageContext storageContext,
      String nameSpace, Object key) 
  {
    MemStorageObject obj = getRecordInternal(storageContext, nameSpace, key);
    if(obj != null)
      return obj.getData();
    return null;
  }
  
  protected MemStorageObject getRecordInternal(IStorageContext storageContext,
                    String nameSpace, Object key)
  {
    MemDb store = m_nsMap.get(nameSpace);
    assert (store != null);
    return store.get(key);
  }

  protected boolean deleteRecordImpl(IStorageContext storageContext,
      String nameSpace, Object indexKey, Object key) 
  {
    MemDb store = m_nsMap.get(nameSpace);
    assert (store != null);
    Object v = store.remove(indexKey, key);
    return (v != null);
  }

  /*
   * Dummy IStorageContext as the caller would assert it's not null.
   */
  private static class MemTxn 
    implements IStorageContext
  {
    HashMap<String, HashMap<Object, MemStorageObject>> txn_store;
    
    MemTxn()
    {
      txn_store = new HashMap<String, HashMap<Object, MemStorageObject>>();
    }
    
    void put(String ns, Object key, MemStorageObject obj) 
     throws Exception
    {
      HashMap<Object, MemStorageObject> db = txn_store.get(ns);
      MemStorageObject clonedObj = null;
      if(db == null)
      {  
        db = new HashMap<Object, MemStorageObject>();
        // obj will be null when getting CREATED
        if(obj != null)
          clonedObj = obj.deepCopy();

        db.put(key, clonedObj);
        txn_store.put(ns, db);
      }
      else
      { // we do not have a previous version
        // If we are acquiring multiple writelocks in one transaction
        // then we need the oldest version in case of the rollback.
        if(db.get(key) == null)
        { // obj will be null when getting CREATED
          if(obj != null)
            clonedObj = obj.deepCopy();

          db.put(key, clonedObj);
        }
      }
    }
    
    HashMap<Object, MemStorageObject> get(String nameSpace)
    {
      return txn_store.get(nameSpace);
    }
   
  }
  
  public IStorageContext beginTransaction(String nameSpace)
  {
    return new MemTxn();
  }
  
  public void   endTransaction(IStorageContext txn, boolean commit) 
  { // if rollback
    if(!commit)
    {
      MemTxn mem_txn = (MemTxn)txn;
      Iterator<String> ns = mem_txn.txn_store.keySet().iterator();
      while(ns.hasNext())
      {
        String nameSpace = ns.next();
        Iterator<Map.Entry<Object, MemStorageObject>> itr =
                            mem_txn.get(nameSpace).entrySet().iterator();
        while(itr.hasNext())
        {
          Entry<Object, MemStorageObject> entry = itr.next();
          Object key = entry.getKey();
          MemStorageObject obj = entry.getValue();
          Object indexKey = null;
          Object oldObject = null;
          if(obj != null)
          {
            indexKey = obj.getIndexKey();
            oldObject = obj.getData();
          }
          putRecordImpl(txn, nameSpace,indexKey, key, oldObject);
        }
      }
    }
 
  }
  
  public void lockRecordForUpdate(IStorageContext txn, String nameSpace,
                                  Object key) throws Exception
  {
     MemTxn memTxn = (MemTxn)txn;
     
     // oldObj will be the previous version of the object in store
     // it will be null in case the object is newly created
     MemStorageObject memObj = getRecordInternal(txn, nameSpace, key);
     
     memTxn.put(nameSpace, key, memObj);
  }
  

  private static class StoreIterator 
    implements IStorageContext
  {
    Iterator<Map.Entry<Object, MemStorageObject>> itr;
    
    StoreIterator() {}
    
    StoreIterator(MemDb store)
    {
      if (store == null)
      {
        itr = null;
      }
      else
      {
        Set<Map.Entry<Object, MemStorageObject>> entries = store.m_store.entrySet();
        itr = entries.iterator();
      }
    }
    
    public Object nextKey()
    {
      if (itr == null)
        return null;
      if (!itr.hasNext()) 
        return null;
      Map.Entry<Object, MemStorageObject> entry = itr.next();
      return entry.getKey();
    }

    
    public Object nextRecord()
    {
      if (itr == null)
        return null;
      if (!itr.hasNext()) 
        return null;
      Map.Entry<Object, MemStorageObject> entry = itr.next();
      return entry.getValue().getData();
    }
    
    @SuppressWarnings("unused")
  public boolean hasNext()
    {
      if (itr == null) return false;
      return itr.hasNext();
    }

    @SuppressWarnings("unused")
  public void remove()
    {
      assert (false);
    }
  }
  
  private static class IndexIterator 
    extends StoreIterator
  {
    MemDb store;
    Iterator<Entry<Object, MemStorageObject>> itr;
    
    IndexIterator(MemDb store, Object indexKey)
    {
      this.store = store;
      itr = null;
      if (store != null)
      {
        assert (indexKey != null);
        HashMap<Object, MemStorageObject> objs = store.getIndex(indexKey);
        if (objs != null)
          itr = objs.entrySet().iterator();
      }
    }
    
    public Object nextKey()
    {
      if (itr == null)
        return null;
      if (!itr.hasNext()) 
        return null;
      return itr.next().getKey();
    }

    public Object nextRecord()
    {
      if(itr == null)
        return null;
      if(!itr.hasNext())
        return null;
      return itr.next().getValue().getData();
    }

    public boolean hasNext()
    {
      if (itr == null)
        return false;
      return itr.hasNext();
    }

    public void remove()
    {
      assert (false);
    }
  }  

  public IStorageContext initQuery(String nameSpace, Object indexKey)
  {
    MemDb store = m_nsMap.get(nameSpace);
    if (indexKey == null)
    {
      // If index key is not given, use the primary database 
      return new StoreIterator(store);
    }
    else
    {
      // If index key is given, use the secondary database 
      return new IndexIterator(store, indexKey);
    }
  }

  public Object getNextKey(IStorageContext ctx)
  {
    assert (ctx != null);
    StoreIterator itr = (StoreIterator) ctx;
    return itr.nextKey();
  }
  
  public Object getNextRecord(IStorageContext ctx)
  {
    assert (ctx != null);
    StoreIterator itr = (StoreIterator) ctx;
    return itr.nextRecord();
  }
  
  public synchronized void dump(IDumpContext dumper)
  {
    String tag = LogUtil.beginDumpObj(dumper, this);
    Set<Map.Entry<String, MemDb>> nsentries = m_nsMap.entrySet();
    String dbTag = "DB";
    String[] dbAttrs = {"Name", "Entries"};
    Object[] dbVals = new Object[2];
    for (Map.Entry<String, MemDb> nsentry : nsentries)
    {
      String ns = nsentry.getKey();
      MemDb store = nsentry.getValue();
      Set<Map.Entry<Object, MemStorageObject>> entries = store.m_store.entrySet();
      long count = entries.size();
      dbVals[0] = ns;
      dbVals[1] = count;
      dumper.beginTag(dbTag, dbAttrs, dbVals);
      dumper.endTag(dbTag);
    }
    LogUtil.endDumpObj(dumper, tag);
  }
}

