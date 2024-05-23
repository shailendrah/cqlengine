/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/storage/BaseStorage.java /main/19 2011/05/19 15:28:45 hopark Exp $ */

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
 *  @version $Header: BaseStorageDB.java 03-apr-2011.12:29:59 hopark   Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.storage;

import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILogLevelManager;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.service.CEPManager;
import oracle.cep.util.StringUtil;
@DumpDesc(evPinLevel=LogLevel.STORAGE_ARG)
public abstract class BaseStorage implements IStorage, ILoggable
{
  StorageStat m_stat;
  
  public BaseStorage()
  {
    m_stat = new StorageStat();
  }

  public	String getEnvLocation() {return null;}

  public StorageStat getStat() { return m_stat;}
  
  public void lockRecordForUpdate(IStorageContext context, String nameSpace,
                                  Object key) throws Exception {};
  public void unlockRecordForUpdate(IStorageContext txn, String nameSpace, 
          Object key) {};
  protected abstract boolean putRecordImpl(IStorageContext txn, 
              String nameSpace, Object indexKey, Object key, Object data);
  protected abstract Object getRecordImpl(IStorageContext txn, 
              String nameSpace, Object key);
  protected abstract boolean deleteRecordImpl(IStorageContext txn, 
              String nameSpace, Object indexKey, Object key);

  public boolean putRecord(IStorageContext context, 
                        String nameSpace, Object indexKey,
                        Object key, Object data)
  {
    LogLevelManager.trace(LogArea.STORAGE, LogEvent.DB_WRITE, this,
                  context, nameSpace, key, data);
    
    long lstarttime = System.currentTimeMillis();
    boolean b = putRecordImpl(context, nameSpace, indexKey, key, data);
    long lendtime = System.currentTimeMillis();
    long ldifftime = (lendtime - lstarttime);
    m_stat.addWrite(ldifftime);
    return b;
  }

  public Object getRecord(IStorageContext context, 
                          String nameSpace, Object key) 
  {
    long lstarttime = System.currentTimeMillis();
    Object result = getRecordImpl(context, nameSpace, key);
    long lendtime = System.currentTimeMillis();
    long ldifftime = (lendtime - lstarttime);
    m_stat.addRead(ldifftime);
    LogLevelManager.trace(LogArea.STORAGE, LogEvent.DB_READ, this,
                  context, nameSpace, key, result);
    return result;
  }

  public boolean deleteRecord(IStorageContext context, 
                              String nameSpace, Object indexKey, Object key) 
  {
    LogLevelManager.trace(LogArea.STORAGE, LogEvent.DB_DELETE, this,
                  context, nameSpace, key);
    long lstarttime = System.currentTimeMillis();
    boolean b = deleteRecordImpl(context, nameSpace, indexKey, key);
    long lendtime = System.currentTimeMillis();
    long ldifftime = (lendtime - lstarttime);
    m_stat.addDelete(ldifftime);
    return b;
  }

  public boolean updateRecord(IStorageContext context, 
                              String nameSpace, Object key, Object data)  
  {
    assert false : "not supported";
    return false;
  }
  
  public long getCacheSize() {return 0;}
  public long getLogSize() {return 0;}
  
  public void open() 
  {
    assert false : "not supported";
  }
  
  public void close() 
  {
    assert false : "not supported";
  }

  public void clean() 
  {
    assert false : "not supported";
  }
  
  public void addNameSpace(String ns, boolean persistent, boolean transactional, 
		 Class<?> indexKeyClass, Class<?> keyClass, Class<?> objClass) 
  {
    assert false : "not supported";
  }
  
  public IStorageContext beginTransaction(String nameSpace)
  {
    assert false : "not supported";
    return null;
  }
  
  public void   endTransaction(IStorageContext txn, boolean commit) 
  {
    assert false : "not supported";
  }

  public IStorageContext initQuery(String nameSpace, Object schema)
  {
    assert false : "not supported";
    return null;
  }

  public Object getNextKey(IStorageContext ctx)
  {
    assert false : "not supported";
    return null;
  }
  
  public Object getNextRecord(IStorageContext ctx)
  {
    assert false : "not supported";
    return null;
  }
  
  public void closeQuery(IStorageContext ctx)
  {
    assert false : "not supported";
  }

  
  public String getTargetName() 
  {
    return StringUtil.getBaseClassName(this);
  }

  public int getTargetId()
  {
    return 0;
  }

  public int getTargetType()
  {
    return 0;
  }

  public ILogLevelManager getLogLevelManager()
  {
    return CEPManager.getInstance().getLogLevelManager();
  }
    
  public void trace(IDumpContext dumper, ILogEvent event, int level, Object[] args)
  {
  }
}

