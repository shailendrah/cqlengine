/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/CacheObjectManager.java /main/13 2009/11/23 21:21:22 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY) 
    parujain    01/28/09 - transaction mgmt
    hopark      10/10/08 - remove statics
    parujain    09/11/08 - multiple schema
    hopark      03/26/08 - server reorg
    parujain    05/07/08 - fix lock problem
    hopark      01/17/08 - dump
    parujain    06/21/07 - release read lock
    hopark      03/21/07 - remove getStore
    parujain    02/14/07 - system startup
    parujain    02/02/07 - BDB Integration
    parujain    01/11/07 - BDB integration
    parujain    11/22/06 - Drop Query Problem
    najain      10/24/06 - integrate with mds
    parujain    09/13/06 - MDS Integration
    parujain    07/14/06 - moved from cache to metadata
    parujain    07/14/06 - metadata cleanup 
    parujain    07/14/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/CacheObjectManager.java /main/13 2009/11/23 21:21:22 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.metadata;

/**
 * This class is an abstract class for all the managers. All the managers will access
 * cache objects via CacheObjectManager. This will clean up the metadata.
 */

import oracle.cep.logging.IDumpContext;
import oracle.cep.metadata.cache.Cache;
import oracle.cep.metadata.cache.CacheLock;
import oracle.cep.metadata.cache.CacheObjectType;
import oracle.cep.metadata.cache.CacheObject;
import oracle.cep.metadata.cache.Descriptor;
import oracle.cep.metadata.cache.NameSpace;
import oracle.cep.service.CEPManager;
import oracle.cep.storage.IStorage;
import oracle.cep.storage.StorageManager;
import oracle.cep.transaction.ITransaction;

/**
 * @author parujain
 *
 */

 public abstract class CacheObjectManager {
   /**
    * Global cache for all the managers 
    * Will be instantiated only once.
    */
  protected Cache cache ;
  protected IStorage      storage;
  
  public static class Locks {
    /** Lock for OBJECT_ID  */
    CacheLock cidLock;
    /** Lock for Objects like TABLE etc. */
    CacheLock objLock;
  }
  
  public CacheObjectManager(CEPManager cepMgr, Cache cache)
  {
    StorageManager sm = cepMgr.getStorageManager();
    storage = sm.getMetadataStorage();
    this.cache = cache;
  }
  
  /**
   * This method creates the object in the cache if not already present.
   * If there is already one present then it returns null.
   * @param name String name of query, stream etc. 
   * @param type Type of the object which will be created 
   * @param loadContext Context mentioned by the user
   * @return lock acquired
   */
  protected CacheLock createObject(ITransaction txn, String name, 
                 String schema,CacheObjectType type,Object loadContext )
  {
    CacheLock cl = null;
    int cid;
    CacheObject obj;
    
    // Check for duplicates and create
     cl = cache.create(txn, new Descriptor(name, type, schema, loadContext));
        
    //Here we don't throw the exception, but instead return null so that 
    //the caller can throw the exception with appropriate exception reason.
     if (cl == null)
     {
       return null;
     }

     // Get id      
     cid = ObjectId.getNewObjectId(txn, cache, name, schema, type);

     // Initialize
     obj = cl.getObj();
     obj.setId(cid);
   
  
    return cl;
  } //end of createObject method

  /**
   * This method is called when the caller don't know the object type.
   * @param id Object id
   * @param forUpdate Whether object will be modified or not.
   * @return Lock lock acquired on the referenced object
   */
  protected CacheLock findCache(ITransaction txn, int id, boolean forUpdate) 
  {
    return findCache(txn, id,forUpdate,null);
  }
  
  /**
   * Finds the object with its name and namespace and take the appropriate lock
   * @param name Name of the object
   * @param nameSpace NameSpace to which the object belongs
   * @param forUpdate Whether the object modified or not
   * @return Lock acquired on the referenced object
   */
  protected CacheLock findCache(ITransaction txn, Object name, 
       String schema, NameSpace nameSpace, boolean forUpdate)
  {
    CacheLock l;
    
    l = cache.find(txn, name, nameSpace,schema, forUpdate);
    
    return l;
  }
  
  /**
   * Finds the object with its id and take the appropriate lock (Read or Exclusive)
   * @param id Object id
   * @param forUpdate Whether object will be modified or not
   * @param type Type of the object.
   * @return Lock acquired on the referenced object
   */
  protected CacheLock findCache(ITransaction txn, int id, 
                                boolean forUpdate,CacheObjectType type)
  {
    CacheLock id1;
    CacheLock l;
    ObjectId oid = null;
    
    // Get name
    // Here schema will be null because id lookup does not need schema
     id1 = cache.find(txn, new Descriptor((Integer) id, CacheObjectType.OBJECTID,
            null), false);
     oid = (id1 == null) ? null : (ObjectId) id1.getObj();

   // If the object not existing or the type doesn't match
   // Return null instead of throwing exception.
   // Caller can throw the appropriate exception
//      When null is returned we don't know whether id is invalid or
      // object could not be found.
        if((id1 == null) || ((oid.getObjectType() != type)&& (type != null)))
        {
          if(id1 != null)
            release(txn, id1);
          return null;
        }
             
        // If object found then get the lock
        l = cache.find(txn, new Descriptor(oid.getObjectName(), oid.getObjectType(),
            oid.getSchema(), null), forUpdate);
        
        release(txn, id1);
        
    return l;
  } //End of findCache method
  
  /**
   * Finds in the cache the required object
   * @param desc Descriptor of the object
   * @param forUpdate Object will be modifed or not.
   * @return Acquired lock
   */
  //Here desc will contain schema.name or id if schema is not null
  protected CacheLock findCache(ITransaction txn, Descriptor desc, 
                                boolean forUpdate)
  {
    CacheLock l;
    
    l = cache.find(txn, desc,forUpdate);
          
    return l;
  }
  
  /**
   * Deletes the object from the cache
   * @param objId Object Id
   * @return Lock acquired on the object
   */
  protected Locks deleteCache(ITransaction txn, int objId)
  {
    CacheLock l;
    CacheLock id1;
    ObjectId oid=null;
    Locks locks = new Locks();
    
    // schema will be null
      id1 = cache.delete(txn, new Descriptor((Integer) objId, CacheObjectType.OBJECTID,
                null));

      // When null is returned we don't know whether id is invalid or
      // object could not be found.
      oid = (id1 == null) ? null : (ObjectId) id1.getObj();
      if (id1 == null)
      {
        return null;
      }

       locks.cidLock = id1;
      
       // Delete
       l = cache.delete(txn, new Descriptor(oid.getObjectName(),
                oid.getObjectType(), oid.getSchema(), null));
       
       if(l == null)
         return null;
       
       locks.objLock = l;
       
    return locks;
  }
  
  /**
   * Release the readlock, where we dont want to rollback since
   * we dont have write lock
   * @param context
   * @param l
   */
  protected void release(ITransaction txn, CacheLock l)
  {
    assert l != null;
    cache.releaseReadLock(txn, l, false);
  }

  
  protected void cleanRepository()
  {
    cache.cleanRepository();
    return;
  }
  
  protected void dump(IDumpContext dumper, String tag, CacheObjectType type) 
  {
    cache.dump(dumper, tag, type);
  }
  
  public void onRollback(CacheObject obj)
  {
    return;
  }
}

