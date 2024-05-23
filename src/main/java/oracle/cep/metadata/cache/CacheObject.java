/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/cache/CacheObject.java /main/13 2009/11/23 21:21:22 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares CacheObject in package oracle.cep.metadata.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 parujain 09/28/09 - Dependency
 parujain 01/14/09 - metadata in-mem
 hopark   10/10/08 - remove statics
 parujain 09/12/08 - multiple schema support
 hopark   09/17/08 - support schema
 skmishra 08/21/08 - imports
 hopark   03/26/08 - server reorg
 hopark   02/05/08 - fix dump level
 hopark   01/17/08 - metadata dump
 mthatte  08/22/07 - 
 hopark   03/21/07 - implement base operation using IStorage
 parujain 02/02/07 - copy method
 parujain 01/11/07 - BDB integration
 parujain 01/09/07 - BDB integration
 parujain 09/11/06 - MDS Integration
 parujain 07/13/06 - check locks 
 parujain 07/11/06 - flag removal 
 anasri 07/06/06 - make getType public 
 parujain  06/27/06 - metadata cleanup 
 skaluska  03/10/06 - Creation
 skaluska  03/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/cache/CacheObject.java /main/13 2009/11/23 21:21:22 parujain Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.metadata.cache;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import oracle.cep.descriptors.IMetadataDescriptorFactory;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.IDumpable;
import oracle.cep.logging.LogUtil;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.storage.IStorage;
import oracle.cep.storage.IStorageContext;

/**
 * CacheObject
 *
 * @author skaluska
 */
@DumpDesc(autoFields=true)
public abstract class CacheObject implements Externalizable, 
IMetadataDescriptorFactory, IDumpable, Cloneable
{
  /**
   *  default suid
   */
  private static final long serialVersionUID = 1L;
  
  /** Can be either Query id, table id etc. */
  @DumpDesc(ignore=true) private int id;

  /** name */
  @DumpDesc(ignore=true) private Object key;

  /** type */
  private CacheObjectType typ;

  /** name space */
  private NameSpace nameSpace;
    
  /** schema name */
  private String  schemaName;
        
  /** lock */
  @DumpDesc(ignore=true)
  private transient ReentrantReadWriteLock lock;

  /** refCount */
  @DumpDesc(ignore=true)
  private transient int refCount;

  /** whether object is dirty */
  private transient boolean bDirty;

  /** whether object has been loaded */
  private transient boolean bLoaded;

  /** change type if the object has changed */
  private transient ChangeType change;

  /** next object on lru list */
  @DumpDesc(ignore=true) private transient CacheObject nextlru;

  /** previous object on lru list */
  @DumpDesc(ignore=true) private transient CacheObject prevlru;

  public CacheObject()
  {
  }
  
  /**
   * Constructor for CacheObject
   * 
   * @param k
   *          Object key
   * @param t
   *          Object type
   */
  public CacheObject(Object k, String schema, CacheObjectType t)
  {
    key = k;
    typ = t;
    schemaName = schema;
    bDirty = false;
    bLoaded = false;
    lock = new ReentrantReadWriteLock();
    refCount = 0;
    id = 0;
    nameSpace = Cache.getNameSpace(typ);
  }

  public CacheObject clone() throws CloneNotSupportedException {
    CacheObject o = (CacheObject)super.clone();
    return o;
  }
  

  /**
   * Getter for id in Query
   * 
   * @return Returns the id
   */
  public int getId()
  {
    return id;
  }

  /**
   * Setter for id in Query
   * 
   * @param id
   *          The id to set.
   */
  public void setId(int id)
  {
    this.id = id;
  }
  
  /**
   * Setter for schema name for cache object
   * 
   * @param schema
   *            Schema name
   */
  public void setSchema(String schema)
  {
      this.schemaName = schema;
  }
  
  /**
   * Getter for the schema to which object belongs
   * 
   * @return Returns Schema name
   */
  public String getSchema()
  {
    return schemaName;
  }
  
  /**
   * Setter for key in Query
   * 
   * @param key
   *          The key to set.
   */
  public void setKey(Object key)
  {
    this.key = key;
  }

  /**
   * Getter for key in CacheObject
   * 
   * @return Returns the key
   */
  public Object getKey()
  {
    return key;
  }
  
  /**
   * Getter for cachekey in cacheobject
   * 
   * @return Returns the cachekey
   */
  public CacheKey getCacheKey()
  {
    return CacheKeyGenerator.createCacheKey(key, schemaName, nameSpace);
  }
  
  /**
   * Getter of the secondary index key for this object
   * 
   * @return GetSecondary key
   */
  public String getSecondaryIndexKey()
  {
    switch(nameSpace)
    {
      case OBJECTID:
      case DEPENDENCY:
      case SYSTEM : return null;
      default: return schemaName;
    }
  }
  
  /**
   * Getter for change in CacheObject
   * 
   * @return Returns the change
   */
  public ChangeType getChange()
  {
    return change;
  }

  /**
   * Setter for change in CacheObject
   * 
   * @param change
   *          The change to set.
   */
  void setChange(ChangeType change)
  {
    this.change = change;
  }

  /**
   * Getter for bDirty in CacheObject
   * 
   * @return Returns the bDirty
   */
  boolean isBDirty()
  {
    return bDirty;
  }

  /**
   * Setter for bDirty in CacheObject
   * 
   * @param dirty
   *          The bDirty to set.
   */
  void setBDirty(boolean dirty)
  {
    bDirty = dirty;
  }

  /**
   * Getter for lock in CacheObject
   * 
   * @return Returns the lock
   */
  public ReentrantReadWriteLock getLock()
  {
    return lock;
  }

  /**
   * Increment refCount
   */
  void incrRef()
  {
    refCount++;
  }

  /**
   * Decrement refCount
   */
  void decrRef()
  {
    refCount--;
  }

  /**
   * Getter for refCount in CacheObject
   * 
   * @return Returns the refCount
   */
  int getRefCount()
  {
    return refCount;
  }

  /**
   * Getter for type in CacheObject
   * 
   * @return Returns the type
   */
  public CacheObjectType getType()
  {
    return typ;
  }

  /**
   * Setter for type in CacheObject
   * 
   * @param typ
   *          The type to set.
   */
  void setType(CacheObjectType typ)
  {
    this.typ = typ;
  }

  /**
   * Getter for bLoaded in CacheObject
   * 
   * @return Returns the bLoaded
   */
  boolean isBLoaded()
  {
    return bLoaded;
  }

  /**
   * Setter for bLoaded in CacheObject
   * 
   * @param loaded
   *          The bLoaded to set.
   */
  void setBLoaded(boolean loaded)
  {
    bLoaded = loaded;
  }

  /**
   * Getter for nextlru in CacheObject
   * 
   * @return Returns the nextlru
   */
  CacheObject getNextlru()
  {
    return nextlru;
  }

  /**
   * Setter for nextlru in CacheObject
   * 
   * @param nextlru
   *          The nextlru to set.
   */
  void setNextlru(CacheObject nextlru)
  {
    this.nextlru = nextlru;
  }

  /**
   * Getter for prevlru in CacheObject
   * 
   * @return Returns the prevlru
   */
  CacheObject getPrevlru()
  {
    return prevlru;
  }

  /**
   * Setter for prevlru in CacheObject
   * 
   * @param prevlru
   *          The prevlru to set.
   */
  void setPrevlru(CacheObject prevlru)
  {
    this.prevlru = prevlru;
  }

  /**
   * Verifies whether the object is locked by the current thread or not
   * 
   * @return true or false.
   */
  public boolean isWriteable()
  {
    return lock.isWriteLockedByCurrentThread();
  }
   
  /**
   * Additional processing to be done to the read object
   * Object is read from backend
   * @param factoryMgr TODO
   */
  public void processReadObject(FactoryManager factoryMgr)
  {
    //Default do nothing
  }
  
  public void instantiateReadObject()
  {
    lock = new ReentrantReadWriteLock();
    refCount = 0;
    bDirty = false;
    bLoaded = false;
  }
 
  /**
   * Write object to backing storage
   * 
   * @return true if successful
   */
  public boolean writeObject(IStorage storage, IStorageContext context)
  {
    assert (context != null);
    return storage.putRecord(context, 
        nameSpace.toString(), this.getSecondaryIndexKey(), 
        this.getCacheKey(), this);
  }

  /**
   * Update the cache Object when cache gets modified
   * 
   * @param context
   * @return true if successful
   */
  public boolean updateObject(IStorage storage, IStorageContext context)
  {
    return writeObject(storage, context);
  }
  
  /**
   * Deletes the cache Object when cache object gets deleted
   * 
   * @param context
   * @return
   */
  public boolean deleteObject(IStorage storage, IStorageContext context) 
  {
    assert (context != null);
    return storage.deleteRecord(context, nameSpace.toString(), 
    		getSecondaryIndexKey(), getCacheKey());
  }

  
  public synchronized void dump(IDumpContext dump)
  {
    String tag = LogUtil.beginDumpObj(dump, this);
    if (dump.isVerbose())
    {
      LogUtil.logTagVal(dump, "lock", lock);
      LogUtil.logTagVal(dump, "refCount", refCount);
    }
    LogUtil.endDumpObj(dump, tag);
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
      out.writeInt(id);
      out.writeObject(key);
      out.writeObject(schemaName);
      out.writeObject(nameSpace);
      out.writeObject(typ);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
      id = in.readInt();
      key = in.readObject();
      schemaName = (String) in.readObject();
      nameSpace = (NameSpace) in.readObject();
      typ = (CacheObjectType) in.readObject();
  }
  
}
