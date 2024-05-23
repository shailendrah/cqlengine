/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/cache/Cache.java /main/34 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares Cache in package oracle.cep.metadata.cache

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 vikshukl  02/26/10 - (#90266335): lookup without type at creation time
 parujain  11/24/09 - synonym
 parujain  01/28/09 - transaction support
 parujain  01/13/09 - metadata in-memory
 hopark    12/02/08 - move LogLevelManager to ExecContext
 skmishra  11/12/08 - adding type param
 hopark    10/10/08 - remove statics
 hopark    10/07/08 - use execContext to remove statics
 hopark    10/03/08 - use static init namespace
 skmishra  10/22/08 - changing getNextKey to getNextRecord
 parujain  09/30/08 - drop schema
 parujain  09/11/08 - multiple schema
 hopark    09/23/08 - use getNextRecord in getNext
 hopark    09/17/08 - support schema
 hopark    09/12/08 - add schema indexing
 hopark    06/18/08 - logging refactor
 hopark    03/26/08 - server reorg
 sbishnoi  05/07/08 - test lock issue
 hopark    01/15/08 - metadata logging
 mthatte   11/07/07 - adding methods for JDBC
 hopark    06/20/07 - fix potential bug
 parujain  06/21/07 - release read lock
 hopark    06/07/07 - use LogArea
 hopark    05/22/07 - logging support
 parujain  05/18/07 - refcounting problem while duplicate creation
 hopark    05/11/07 - remove System.out.println(use java.util.logging instead)
 parujain  03/22/07 - init throws exception
 hopark    03/21/07 - storage re-org
 parujain  03/20/07 - ageout testing
 parujain  03/05/07 - window object
 parujain  02/13/07 - system startup
 parujain  02/09/07 - System DDLS
 parujain  01/11/07 - BDB integration
 parujain  11/22/06 - Drop Query Problem
 dlenkov   11/16/06 - added SimpleFunctionSet
 najain    10/24/06 - integrate with mds
 parujain  09/11/06 - MDS Integration
 parujain  07/11/06 - linkedlist removal 
 parujain  07/11/06 - flag removal 
 parujain  07/10/06 - Namespace Implementation 
 najain    03/31/06 - fix bugs 
 skaluska  03/10/06 - Creation
 skaluska  03/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/cache/Cache.java /main/33 2010/03/05 10:50:28 vikshukl Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.metadata.cache;

import java.util.EnumMap;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Level;

import oracle.cep.descriptors.ArrayContext;
import oracle.cep.exceptions.StorageError;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILogLevelManager;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.logging.trace.LogTags;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.metadata.CacheObjectManager;
import oracle.cep.metadata.Dependency;
import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.ObjectId;
import oracle.cep.metadata.Query;
import oracle.cep.metadata.Source;
import oracle.cep.metadata.Synonym;
import oracle.cep.metadata.SystemObject;
import oracle.cep.metadata.Window;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;
import oracle.cep.transaction.ITransaction;
import oracle.cep.storage.IStorage;
import oracle.cep.storage.IStorageContext;
import oracle.cep.storage.IStorageMgr;
import oracle.cep.storage.StorageException;
import oracle.cep.storage.StorageManager;

/**
 * Cache of metadata objects
 *
 * @author skaluska
 */
@DumpDesc(autoFields=true,
          evPinLevel=LogLevel.MCACHE_ARG,
          infoLevel =LogLevel.MCACHE_INFO,
          verboseDumpLevel=LogLevel.MCACHE_LOCKINFO)
public class Cache implements ILoggable
{
  private static final boolean                      CACHE_AGEOUT_TEST = false;

  /** Mapping from object type to cache object */
  CacheObjectFactory                                objFactory;

  /** storage interface */
  @DumpDesc(ignore=true) 
  IStorage                                          storage;

  @DumpDesc(ignore=true) 
  FactoryManager                                    factoryMgr;
  
  /** hash table of all objects */
  @DumpDesc(ignore=true) 
  private Hashtable<CacheKey, CacheObject>            ht;

  /** lru list */
  @DumpDesc(ignore=true) 
  private CacheObject                               lru;

  /** end of lru list */
  @DumpDesc(ignore=true) 
  private CacheObject                               lruEnd;

  /** target for max objects to cache */
  private int                                       size;

  /** Mapping from object type to NameSpace */
  public static EnumMap<CacheObjectType, NameSpace> NameSpaceMap;

  static
  {
    NameSpaceMap = new EnumMap<CacheObjectType, NameSpace>(
        CacheObjectType.class);
    NameSpaceMap.put(CacheObjectType.OBJECTID, NameSpace.OBJECTID);
    NameSpaceMap.put(CacheObjectType.QUERY, NameSpace.QUERY);
    NameSpaceMap.put(CacheObjectType.TABLE, NameSpace.SOURCE);
    NameSpaceMap.put(CacheObjectType.VIEW, NameSpace.SOURCE);
    NameSpaceMap.put(CacheObjectType.SINGLE_FUNCTION, NameSpace.USERFUNCTION);
    NameSpaceMap.put(CacheObjectType.AGGR_FUNCTION, NameSpace.USERFUNCTION);
    NameSpaceMap.put(CacheObjectType.SIMPLE_FUNCTION_SET,
        NameSpace.USERFUNCTION);
    NameSpaceMap.put(CacheObjectType.SYSTEM_STATE, NameSpace.SYSTEM);
    NameSpaceMap.put(CacheObjectType.DEPENDENCY, NameSpace.DEPENDENCY);
    NameSpaceMap.put(CacheObjectType.WINDOW, NameSpace.WINDOW);
    NameSpaceMap.put(CacheObjectType.SYNONYM, NameSpace.SYNONYM);
  }

  /**
   * Constructor for Cache
   * 
   * @param s
   *          Target cache size
   */
  public Cache(int s, CacheObjectFactory f)
  {
    ht = new Hashtable<CacheKey, CacheObject>();
    objFactory = f;
    size = s;
    lru = null;
    lruEnd = null;
  }

  public void init(IStorage storage, FactoryManager factoryMgr) throws Exception
  {
    this.storage = storage;
    this.factoryMgr = factoryMgr;
  }

  public static void initNameSpace(IStorage storage) throws Exception
  {
    if (storage != null)
    {
      try
      {
        boolean transactional = true;
        storage.addNameSpace(StorageManager.CLASSDB_NAMESPACE, NameSpace.SYSTEM.toString(), 
            transactional, null, CacheKey.class, SystemObject.class);
        storage.addNameSpace(StorageManager.CLASSDB_NAMESPACE, NameSpace.OBJECTID.toString(),
            transactional, null, CacheKey.class, ObjectId.class);
        storage.addNameSpace(StorageManager.CLASSDB_NAMESPACE, NameSpace.DEPENDENCY.toString(), 
            transactional, null, CacheKey.class, Dependency.class);
        storage.addNameSpace(StorageManager.CLASSDB_NAMESPACE, NameSpace.QUERY.toString(),
            transactional, String.class /* index key */, CacheKey.class /* key */, Query.class /* obj */);
        storage.addNameSpace(StorageManager.CLASSDB_NAMESPACE, NameSpace.SOURCE.toString(),
            transactional, String.class /* index key */, CacheKey.class /* key */, Source.class /* obj */);
        storage.addNameSpace(StorageManager.CLASSDB_NAMESPACE, NameSpace.USERFUNCTION.toString(),
            transactional, String.class /* index key */, CacheKey.class /* key */, CacheObject.class /* obj */);
        storage.addNameSpace(StorageManager.CLASSDB_NAMESPACE, NameSpace.WINDOW.toString(),
            transactional, String.class /* index key */, CacheKey.class /* key */, Window.class /* obj */);
        storage.addNameSpace(StorageManager.CLASSDB_NAMESPACE, NameSpace.SYNONYM.toString(),
            transactional, String.class /* index key */, CacheKey.class /* key */, Synonym.class /* obj */);
      } catch (StorageException e)
      {
        LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
        throw (e);
      }
    }
  }

  public ITransaction beginTrans()
  {
    IStorageContext ctx = beginContext();
    return new MetadataTransaction(ctx);
  }

  public IStorageContext beginContext()
  {
    if (storage == null)
      return null;
    IStorageContext txn = storage.beginTransaction(NameSpace.SYSTEM.toString());
    return txn;
  }

  public void removeContext(IStorageContext context, boolean commit)
  {
    if (storage != null && context != null)
    {
      storage.endTransaction(context, commit);
    }
  }

  public void cleanRepository()
  {
    if (storage != null)
    {
      storage.clean();
    }
  }

  /**
   * Gets the corresponding NameSpace for any ObjectType
   * 
   * @return Returns the NameSpace of the Object
   */
  public static NameSpace getNameSpace(CacheObjectType type)
  {
    return NameSpaceMap.get(type);
  }

  /**
   * Getter for size in Cache
   * 
   * @return Returns the size
   */
  public int getSize()
  {
    return size;
  }

  /**
   * Setter for size in Cache
   * 
   * @param size
   *          The size to set.
   */
  public void setSize(int size)
  {
    this.size = size;
  }

  /**
   * Add object to end of lru list
   * 
   * @param o
   *          Object
   */
  private void addToLru(CacheObject o)
  {
    if (lruEnd != null)
      lruEnd.setNextlru(o);
    o.setNextlru(null);
    o.setPrevlru(lruEnd);
    lruEnd = o;
  }

  /**
   * Remove object from lru
   * 
   * @param o
   *          Object
   */
  private void removeFromLru(CacheObject o)
  {
    if (o.getNextlru() == null)
      lruEnd = o.getPrevlru();
    else
      o.getNextlru().setPrevlru(o.getPrevlru());

    if (o.getPrevlru() == null)
      lru = o.getNextlru();
    else
      o.getPrevlru().setNextlru(o.getNextlru());

    o.setNextlru(null);
    o.setPrevlru(null);
  }

  /**
   * Ageout an object from the cache
   */
  private void ageOut()
  {
    // to test ageout, every element will get aged out
    if (CACHE_AGEOUT_TEST)
    {
      setSize(2);
    }
    int excess = ht.size() - size;
    CacheObject o;

    if ((size > 0) && (excess > 0))
    {
      o = lru;
      if (CACHE_AGEOUT_TEST)
      {
        LogUtil.fine(LoggerType.TRACE, "Object of type : "
            + o.getType().toString() + " got aged out");
      }
      remove(o);
    }
  }

  /**
   * Load object
   * @param desc
   *          Object descriptor
   * @param o
   *          Object
   */
  private CacheObject load(MetadataTransaction txn, Descriptor desc)
  throws MetadataException
  {
    CacheLock l;
    CacheObject o;

    // Load object if not already loaded
    if ((storage != null))
    {
      NameSpace nameSpace = getNameSpace(desc.getType());
      o = readObject(txn.getStorageContext(), nameSpace, desc.getSchema(), desc.getKey());
      o.instantiateReadObject();
      //    Get lock
      l = new CacheReadLock(o);
   
      o.processReadObject(factoryMgr);
      o.setBLoaded(true);
      // Unlock
      l.release();
      return o;

    }

    return null;
  }

  /**
   * Look up based on name and namespace
   * @param ec TODO
   * @param name
   *          Name of the object
   * @param nameSpace
   *          NameSpace in which the object belongs
   * 
   * @return Returns either null if not found or loads the object and return the
   *         object
   */
  // Here name will contain schema.name or id if schema is not null
  private CacheObject lookup(MetadataTransaction txn,
      Object name, NameSpace nameSpace, String schema)
  {
    CacheObject o = null;

    synchronized (ht)
    {
      o = lookup(txn, name, nameSpace, schema, null);
    }

    return o;

  }

  /**
   * Looks up and insert if required in the hash table
   * @param name
   *          Name of the object
   * @param nameSpace
   *          NameSpace to which the object belongs
   * @param typ
   *          CacheObjectType of the object
   * @return Returns either null if object not found or returns the object
   */
  private CacheObject lookup(MetadataTransaction txn, Object name,
      NameSpace nameSpace, String schema, CacheObjectType typ)
  {
    CacheObject o = null;
    boolean objPres = false;
    CacheObjectType objectType = typ;

    synchronized (ht)
    {
      // Lookup the object
      o = getHashTable(name, schema, nameSpace);

      // Is the object already present
      if (o != null)
      {
        objPres = true;
      }

      if (!objPres && (storage == null))
        return null;

      // Verify the storage if the object is present or not
      if (!objPres)
      {
        objectType = getType(txn, name, nameSpace, schema);

        // Called the backend to verify if the object is present or not
        // returns null if object not objPres
        if (objectType == null)
          return null;
        else
        {
          // If the type of the object is known then verify whether both are
          // same or not
          // This check is required since NameSpace is shared
          if ((typ != null) && (objectType != typ))
            return null;
          else
          {
            typ = objectType;
            objPres = true;
          }
        }

        // Insert object if found in backend i.e. the function did not return
        // null
        if (objPres)
        {
          try
          {
            o = load(txn, new Descriptor(name, typ, schema, null));
            putHashTable(name, getNameSpace(typ), o);
          } catch (MetadataException me)
          {
            LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, me);
          }
        }
      }
      else {
        // Return null if types does not match
        if ((typ != null) && (o.getType() != typ))
          return null;
        removeFromLru(o);
      }

      if (o != null)
      {
        // Bump up ref count
        o.incrRef();

        // Add to end of lru list
        addToLru(o);
      }
    }

    // Object needs to be loaded
    if (!o.isBLoaded())
    {
      try
      {
        o = load(txn, new Descriptor(name, objectType, schema, null));
        putHashTable(name, getNameSpace(objectType), o);
      } catch (MetadataException me)
      {
        LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, me);
      }
    }

    return o;
  }

  /**
   * Lookup and insert if necessary, the specified object
   * @param ec TODO
   * @param desc
   *          TODO
   * 
   * @return Object
   */
  private CacheObject lookup(MetadataTransaction txn, Descriptor desc)
  {
    CacheObject o = null;
    Object key = desc.getKey();
    CacheObjectType typ = desc.getType();
    NameSpace nameSpace = getNameSpace(typ);

    synchronized (ht)
    {
      // Lookup the object
      o = lookup(txn, key, nameSpace, desc.getSchema(), typ);

    }

    return o;
  }

  /**
   * Remove the specified object
   * 
   * @param key
   *          Object key
   * @param typ
   *          Object type
   * @return Object if successful else false
   */
  private boolean remove(CacheObject o)
  {
    boolean r = false;

    assert o != null;

    synchronized (ht)
    {
      // Remove object if no references
      if (o.getRefCount() == 0)
      {
        CacheObject obj = getHashTable(o.getKey(), o.getSchema(), getNameSpace(o.getType()));
        assert obj != null;
        LogLevelManager.trace(LogArea.METADATA_CACHE, LogEvent.MCACHE_DELETE, this,o.getKey(), o.getType().name());
        removeHashTable(o.getKey(), o.getSchema(), o.getType());
        removeFromLru(o);
        r = true;
      }
    }

    return r;
  }

  /**
   * Finds or Loads the object in the cache
   * 
   * @param name
   *          Name of the object
   * @param nameSpace
   *          NameSpace to which the object belongs
   * @param for_Update
   *          Whether the object needs to be updated or not
   * @return Lock on object if found else null
   */
  public CacheLock find(ITransaction m_txn, Object name,
      NameSpace nameSpace, String schema, boolean for_Update)
  {
    CacheObject o;
    CacheLock l = null;

    assert name != null;
    MetadataTransaction txn = (MetadataTransaction)m_txn;

    // Get in hash table
    o = lookup(txn, name, nameSpace, schema);

    // Return null if object not found
    if (o == null)
      return null;

    // Get appropriate lock
    if (for_Update)
    {
      CacheKey key = CacheKeyGenerator.createCacheKey(name, schema, 
                                                      nameSpace);
      l = txn.getLock(key);
      if(l == null)
      { // Acquiring a write lock for the first time in txn
        l = new CacheWriteLock(o);
        try{
          storage.lockRecordForUpdate(txn.getStorageContext(),
                                      nameSpace.toString(), 
                                      o.getCacheKey());
        }catch(Exception e)
        {
   
        }
        LogLevelManager.trace(LogArea.METADATA_CACHE,
                      LogEvent.MCACHE_ACQUIRE_WRITE_LOCK, this, 
                      o.getKey(), o.getType().name());
        txn.addLock(key, l);
      }
      else
        o.decrRef(); // since we are re-using the same lock, no need 
                     // to increment the refcount
      o.setChange(ChangeType.UPDATED);
      o.setBDirty(true);
    } else
    {
      l = new CacheReadLock(o);
      LogLevelManager.trace(LogArea.METADATA_CACHE, LogEvent.MCACHE_ACQUIRE_READ_LOCK, this, o.getKey(), o.getType().name());
    }

    return l;

  }

  /**
   * Find/load an object in the cache
   * 
   * @param desc
   *          Object descriptor
   * @param for_update
   *          Whether the object needs to be updated
   * 
   * @return Lock on object if found else null
   */
  public CacheLock find(ITransaction m_txn, Descriptor desc,
      boolean for_update)
  {
    CacheObject o;
    CacheLock l = null;
    Object key = desc.getKey();
    MetadataTransaction txn = (MetadataTransaction)m_txn;

    assert key != null;

    // Get in hash table
    o = lookup(txn, desc);

    // Return null if object not found
    if (o == null)
      return null;

    // Get appropriate lock
    if (for_update)
    {
      NameSpace ns = getNameSpace(desc.getType());
      CacheKey ckey = CacheKeyGenerator.createCacheKey(key, desc.getSchema(), ns);
      l = txn.getLock(ckey);
      if(l == null)
      {
        l = new CacheWriteLock(o);
        try{
          storage.lockRecordForUpdate(txn.getStorageContext(), 
                                      ns.toString(), 
                                      o.getCacheKey());
        }catch(Exception e)
        {

        }
        txn.addLock(ckey, l);
        
        LogLevelManager.trace(LogArea.METADATA_CACHE, 
                    LogEvent.MCACHE_ACQUIRE_WRITE_LOCK, this, o.getKey(),
                    o.getType().name());
      }
      else
        o.decrRef();
      o.setChange(ChangeType.UPDATED);
      o.setBDirty(true);
    } else
    {
      l = new CacheReadLock(o);
      LogLevelManager.trace(LogArea.METADATA_CACHE, LogEvent.MCACHE_ACQUIRE_READ_LOCK, this, o.getKey(), o.getType().name());
    }

    return l;
  }

  /**
   * Create object with specified key and type
   * @param ec TODO
   * @param desc
   *          Descriptor for object
   * 
   * @return Lock on object if successful else null
   */
  public CacheLock create(ITransaction m_txn, Descriptor desc)
  {
    CacheObject o;
    CacheLock l;
    MetadataTransaction txn = (MetadataTransaction)m_txn;
    Object key = desc.getKey();
    CacheObjectType typ = desc.getType();
    NameSpace nameSpace = getNameSpace(typ);
    String schema = desc.getSchema();

    assert key != null;


    /* lookup on key in particular namespace considering schema.
     * don't include type check at creation time (e.g. a view, stream or
     * relation creation with the same name should fail even though their
     * types differ)
     */
    o = lookup(txn, key, nameSpace, schema);

    // Return if the object in same schema and namespace already exits.
    if (o != null)
    {
      // Ref count increased while referencing
      o.decrRef();
      return null;
    }

    LogLevelManager.trace(LogArea.METADATA_CACHE, LogEvent.MCACHE_CREATE, this, key, typ.name());
    // Create a new object if during lookup we don't find any object
    o = objFactory.newCacheObject(key, schema, typ);
    // put in hash table with  key 
    putHashTable(key, nameSpace, o);
    
    CacheKey cachekey = CacheKeyGenerator.createCacheKey(key, o.getSchema(), 
                                                    nameSpace);

    l = txn.getLock(cachekey);
    
    if(l == null)
    {
    
      // Bump up ref count
      o.incrRef();

      // add to end of LRU list
      addToLru(o);

      // Get write lock
      l = new CacheWriteLock(o);
      try{
        storage.lockRecordForUpdate(txn.getStorageContext(),
                                    nameSpace.toString(), 
                                    o.getCacheKey());
      }catch(Exception e)
      {
   
      }
    
      txn.addLock(cachekey, l);
    
      LogLevelManager.trace(LogArea.METADATA_CACHE, LogEvent.MCACHE_ACQUIRE_WRITE_LOCK, this, o.getKey(), o.getType().name());
    }
    
    // Initialize
    o.setChange(ChangeType.CREATED);
    o.setBDirty(true);
    // we don't have copy in backend
    // create view v1 as select * from S;
    // view needs the query to be loaded
    // The query select * from S will be created and should be found in
    // cache. View v1 when tries to read the object should lookup in cache
    // and not in storage since this is a new object got created as part of this
    // transaction and has not been committed. Object when committed only
    // is written in the storage.
    o.setBLoaded(true);

    return l;
  }

  /**
   * Delete object with specified key and type
   * 
   * @param desc
   *          Descriptor for object
   * 
   * @return Lock on object if successful else null
   */
  public CacheLock delete(ITransaction m_txn, Descriptor desc)
  {
    CacheObject o;
    CacheLock l;
    Object key = desc.getKey();
    MetadataTransaction txn = (MetadataTransaction)m_txn;

    assert key != null;

    // Lookup
    o = lookup(txn, desc);

    // Return null if object is not present
    if (o == null)
      return null;
    
    LogLevelManager.trace(LogArea.METADATA_CACHE, LogEvent.MCACHE_DELETE, this, o.getKey(), o.getType().name());

    CacheKey ckey = CacheKeyGenerator.createCacheKey(key, desc.getSchema(), 
                                               getNameSpace(desc.getType()));
    
    l = txn.getLock(ckey);
    if(l == null)
    {
      // Get write lock
      l = new CacheWriteLock(o);
      try{
        storage.lockRecordForUpdate(txn.getStorageContext(),
                                   (getNameSpace(desc.getType())).toString(), 
                                    o.getCacheKey());
      }catch(Exception e)
      {
      
      }
    
      LogLevelManager.trace(LogArea.METADATA_CACHE, 
                  LogEvent.MCACHE_ACQUIRE_WRITE_LOCK, this, o.getKey(),
                  o.getType().name());
      
      txn.addLock(ckey, l);
    }
    else
      o.decrRef();

    // Initialize
    o.setChange(ChangeType.DELETED);
    o.setBDirty(true);

    return l;
  }

  /**
   * Puts the Object in the Hash Table
   * 
   * @param key
   *          Name of the object
   * @param type
   *          Object Type
   * @param lst
   *          CacheObject
   */
  public void putHashTable(Object name, NameSpace nameSpace, CacheObject obj)
  {
    CacheKey key = CacheKeyGenerator.createCacheKey(name, obj.getSchema(), 
                                                    nameSpace);
    ht.put(key, obj);
  }


  /**
   * Gets the pointer to the linked list from HashTable
   * 
   * @param key
   *          Name of the object
   * @param namespace
   *          NameSpace of the object
   * @return CacheObject
   */
  private CacheObject getHashTable(Object key, String schema, NameSpace nameSpace)
  {
    CacheObject obj = null;
    CacheKey hashKey = CacheKeyGenerator.createCacheKey(key, schema, nameSpace);
    obj = ht.get(hashKey);
    return obj;
  }

  /**
   * Remove the object from the hash table
   * 
   * @param key
   *          Name of the object
   * @param type
   *          Type of the object
   */
  private void removeHashTable(Object key, String schema, CacheObjectType type)
  {
    NameSpace nameSpace = getNameSpace(type);
    CacheKey cacheKey = CacheKeyGenerator.createCacheKey(key, schema, nameSpace);
    ht.remove(cacheKey);

  }
  

  /**
   * Release Read lock on cacheObject
   * 
   * @param l
   *          Read Lock
   * @param purge
   *          To be purged or not         
   */
  public void releaseReadLock(ITransaction m_txn, CacheLock l,
      boolean purge)
  {
    assert l != null;
    assert l.getTyp() == CacheLockType.READ; 
    MetadataTransaction txn =(MetadataTransaction)m_txn; 
    LogLevelManager.trace(LogArea.METADATA_CACHE, LogEvent.MCACHE_RELEASE_LOCK, this, "released read lock", l.getObj().getKey(), l.getTyp().name());
    release(txn.getStorageContext(), l, purge);
  }
  
  public void onRollback(ExecContext ec, CacheLock l)
  {
    CacheObject o = l.getObj();
    CacheObjectManager mgr = CacheObjectHelper.getCacheObjectManager(ec, o);
    if(mgr !=  null)
      mgr.onRollback(o);
  }

  /**
   * Release lock on cache object
   * 
   * @param l
   *          Lock
   * @param purge
   *          TODO
   */
  public void release(IStorageContext context, CacheLock l, boolean purge)
  {
    CacheObject o;
    
    assert l != null;

    // Only write locked objects can be purged
    if (purge)
      assert l.getTyp() == CacheLockType.WRITE;

    // Get the oject
    o = l.getObj();

    if (l.getTyp() == CacheLockType.READ)
    {
      // Object can't be dirty unless we are sharing a write lock
      assert !o.isBDirty() || o.getLock().isWriteLockedByCurrentThread();
    } else
    {
      // Do we need to write the object
      if (o.isBDirty() && !purge)
      {
        assert o.getChange() != ChangeType.NONE;
        // Only one write lock in one txn so ref count never > 1
        assert o.getRefCount() == 1;
        
        if (storage != null)
        {
          ChangeType type = o.getChange();
          if (type == ChangeType.CREATED)
            o.writeObject(storage,context);
          else if (type == ChangeType.UPDATED)
            o.updateObject(storage,context);
          else if (type == ChangeType.DELETED)
            o.deleteObject(storage,context);
        }
        //Since the object is deleted, we need to remove it from Cache too
        if (o.getChange() == ChangeType.DELETED)
        {
          purge = true;
        }

        o.setChange(ChangeType.NONE);
        o.setBDirty(false);
        o.setBLoaded(true);
      }

      // Do we need to purge the object?
      if (purge)
      {
        o.setBLoaded(false);
      }
    }
    
    LogLevelManager.trace(LogArea.METADATA_CACHE, LogEvent.MCACHE_RELEASE_LOCK, this, o.getKey(), o.getType().name() );
    
    // Release lock
    l.release();

    // Decrement ref
    o.decrRef();

    // Now try to purge the object
    if (purge)
      remove(o);

    // See if we need to age out something
    ageOut();
  }

  public CacheObjectType getType(MetadataTransaction txn, Object key,
      NameSpace nameSpace, String schema)
  {
    assert (key != null);
    assert (nameSpace != null);
    CacheKey cachekey = CacheKeyGenerator.createCacheKey(key, schema, nameSpace);
    CacheObject data = (CacheObject) storage.getRecord(txn.getStorageContext(),
                                                       nameSpace.toString(), 
                                                       cachekey);
    return (data == null ? null : data.getType());
  }

  
  public CacheObject readObject(IStorageContext context,
      NameSpace nameSpace, String schema, Object key) throws MetadataException
  {
    assert (nameSpace != null);
    assert (key != null);
    CacheKey cacheKey = CacheKeyGenerator.createCacheKey(key, schema, nameSpace);
    return (CacheObject) storage.getRecord(context, 
                                           nameSpace.toString(), cacheKey);
  }

  /**
   * 
   * @param nameSpace namespace to initialize ref. enum NameSpace
   * @return The context(handle) for iterating through this namespace
   * @throws StorageException
   */
  public IStorageContext initQuery(String nameSpace, String schema)
      throws StorageException
  {
    IStorageContext ctx = null;

    if (storage == null)
      throw new StorageException(StorageError.INIT_FAILED);
    ctx = storage.initQuery(nameSpace, schema);
    return ctx;
  }
  
  /*
   * Close the query context
   */
  public void closeQuery(IStorageContext ctx)
    throws StorageException
  {
    if (storage == null)
      throw new StorageException(StorageError.INIT_FAILED);
    storage.closeQuery(ctx);
  }
  
  public void describeNameSpace(ArrayContext ctx, String nameSpace, 
                                String schema, String[] types)
  {
    IStorageContext storageCtx = initQuery(nameSpace, schema);
    boolean filterByTypes = false;
    CacheObject record = null;
    if(nameSpace.equals(NameSpace.SOURCE.toString()))
    {
      if(types != null)
        filterByTypes = true;
    }

    // Next key is logically next key wrt context
    while(true)
    {
      try
      {
        record = (CacheObject)storage.getNextRecord(storageCtx);
        // Is there a record?
        if (record == null)
          return;

        //Loop till we get the right type or return null.
        if (filterByTypes)
        {
            if (isValidType(record, types))
              ctx.add(record.allocateDescriptor());
        }
        else
          ctx.add(record.allocateDescriptor());
      }
      catch(UnsupportedOperationException e)
      {
        LogUtil.severe(LoggerType.TRACE, e.toString());
      }
      // If we have reached the end. Or some exception.
      catch (StorageException se)
      {
        return;
      }
    }

  }
  

  private boolean isValidType(CacheObject o, String[] types)
  {
    StringBuilder sb = new StringBuilder();
    for (String _type : types)
      sb.append(_type);
    if (o.getType().equals(CacheObjectType.VIEW))
    {
      int i = sb.indexOf("VIEW");
                        if (i == -1)
        return false;
      else
        return true;
    }

    if (o.getType().equals(CacheObjectType.TABLE))
    {
      Source src = (Source) o;
      
      
      //For ODI: ODI uses the "standard" JDBC types. They don't ask us for the 
      //table types that we support, i.e STREAM, RELATION
      int k = sb.indexOf("TABLE");
      if(k !=-1)
         return true;
          
      
      
      if (src.isBStream())
      {
        int i = sb.indexOf("STREAM");
        if (i == -1)
          return false;
        else
          return true;
      }

      else
      {
        int i = sb.indexOf("RELATION");
        if (i == -1)
          return false;
        else
          return true;
      }
    }

    return false;
  }

  
  public int getTargetId() {return 0;}
  
  public String getTargetName() {return "Cache";}
  
  public int getTargetType() {return 0;}
  
  public ILogLevelManager getLogLevelManager()
  {
    return CEPManager.getInstance().getLogLevelManager();
  }
     
  public void trace(IDumpContext dumper, ILogEvent event, int level, Object[] args)
  {
    // All levels are handled by the default implementation.
    // MCACHE_INFO - dumps using the fields specified in DumpDesc annotation
    // MCACHE_LOCKINFO - handled by overriden dump method.
  }

  public void dump(IDumpContext dumper) 
  {
    // TODO. need to implement to support MCACHE_LOCKINFO dump
    // dump the fields specified in DumpDesc annotation.
    String tag = LogUtil.beginDumpObj(dumper, this);
    // additional dumps..
    // dumper.writeln("tag", value);
    LogUtil.endDumpObj(dumper, tag);
  }
  
  public void dump(IDumpContext dumper, String tag, CacheObjectType type)
  {
    LogUtil.beginTag(dumper, tag, LogTags.ARRAY_ATTRIBS, ht.size());
    Enumeration<CacheObject> objs = ht.elements();
    while (objs.hasMoreElements())
    {
      CacheObject obj = objs.nextElement();
      if (obj.getType() == type)
      {
        obj.dump(dumper);
      }
    }
    dumper.endTag(tag);
  }
}
