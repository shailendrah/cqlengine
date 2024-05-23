/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/storage/BerkeleyDB/BDBStorage.java /main/35 2015/07/16 06:39:12 hopark Exp $ */

/* Copyright (c) 2011, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      04/01/11 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/storage/BerkeleyDB/BDBStorage.java /main/35 2015/07/16 06:39:12 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.storage.BerkeleyDB;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;

import oracle.cep.exceptions.StorageError;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.storage.BaseStorage;
import oracle.cep.storage.IStorageContext;
import oracle.cep.storage.IStorageMgr;
import oracle.cep.storage.StorageException;
import oracle.cep.util.DebugUtil;
import oracle.cep.util.PathUtil;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentStats;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.LockStats;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;
import com.sleepycat.je.TransactionStats;
import com.sleepycat.je.utilint.CmdUtil;

public class BDBStorage extends BaseStorage 
{
	String					m_name;
	Environment 			m_env;
	BDBStat			        m_bdbstat;
	
	  private Map<String, DBEntry> m_dbMap;
	  
	  private boolean m_debugDeadLock = false;
	  private HashMap<IStorageContext, String> m_txns;      //for debugging deadlock
	  private static class TxnContext implements IStorageContext
	  {
	    Transaction m_txn;

	    TxnContext(Transaction txn)
	    {
	      m_txn = txn;
	    }
	    
	    public String toString()
	    {
	      return m_txn.toString();
	    }
	  }
	  
	
	public BDBStorage(String name) {
	    m_dbMap = new HashMap<String, DBEntry>();
	    m_bdbstat = new BDBStat(m_env);
	    String deadLockDebug = System.getProperty("debug.deadLock");
	    m_debugDeadLock = (deadLockDebug != null && deadLockDebug.equals("true"));
	    if (m_debugDeadLock)
	    {
	      m_txns = new HashMap<IStorageContext, String>();
	    }
	}

	@Override
	public String getName() {return m_name;}
	
	public Environment getEnv() {return m_env;}
	
	public void init(String envfolder, long cacheSize, boolean cleanup) throws StorageException 
	{
		PathUtil.ensureFolder(envfolder);
		File envhome = new File(envfolder);
		String curPath = null;
		try {
			cacheSize = BDBStorageMgr.handlePercentVal(cacheSize);
			curPath = envhome.getAbsolutePath();
			EnvironmentConfig envConfig = new EnvironmentConfig();
			if (cacheSize != IStorageMgr.USE_BDB_DEFAULT) {
				envConfig.setCacheSize(cacheSize);
			}
			envConfig.setAllowCreate(true);
			// Different BDB environments should use shared cache
			envConfig.setSharedCache(true);
			envConfig.setTransactional(true);
			envConfig.setLockTimeout(30000000L); // Since we are not retrying
													// txn, we need to have a
													// big locktimeout.
			String deadLockDebug = System.getProperty("debug.deadLock");
			if (deadLockDebug != null && deadLockDebug.equals("true")) {
				envConfig.setConfigParam("je.txn.deadlockStackTrace", "true");
				envConfig.setConfigParam("je.txn.dumpLocks", "true");
			}
			m_env = new Environment(envhome, envConfig);
			LogUtil.fine(LoggerType.TRACE, m_env.getConfig().toString());
		} catch (DatabaseException e) {
			LogUtil.warning(LoggerType.TRACE, e.toString());
			throw new StorageException(StorageError.INIT_FAILED, e, curPath);
		}
		// remove all files in storage folder.
		// This needs to be removed if we support recover.
		if (cleanup) {
			LogUtil.fine(LoggerType.TRACE, "cleaning storage in "
					+ envhome.getAbsolutePath());
			String[] fileNames = envhome.list();
			cleanUpDBFiles(envhome.getAbsolutePath(), fileNames);
		}
	}

	private void cleanUpDBFiles(String dbfolder, String[] fileNames) {
		if (fileNames != null) {
			int i = 0;
			for (String filename : fileNames) {
				String path = dbfolder + File.separator + filename;
				File f = new File(path);
				if (f.isDirectory())
					continue;
				LogUtil.fine(LoggerType.TRACE, "deleting " + path);
				boolean success = f.delete();
				fileNames[i] = path;
				if (!success) {
					LogUtil.fine(LoggerType.TRACE, "failed to delete " + path);
				}
				i++;
			}
		}
	}

	  public synchronized void open()
	  {
	    Set<Map.Entry<String, DBEntry>> dbEntries = m_dbMap.entrySet();
	    for (Map.Entry<String, DBEntry> entry: dbEntries)
	    {
	      String nameSpace = entry.getKey();
	      DBEntry dbEntry = entry.getValue();
	      if (dbEntry.getDb() == null)
	      {
	        try
	        {
	          dbEntry.open(m_env);
	        }
	        catch(DatabaseException e)
	        {
	          LogUtil.warning(LoggerType.TRACE, e.toString());
	          dumpTxns();
	          throw new StorageException(StorageError.INIT_FAILED, e, dbEntry.getNamespace());
	        }
	        LogLevelManager.trace(LogArea.STORAGE, LogEvent.DB_OPEN, this, nameSpace);
	      }
	    }
	  }
	  
	  public synchronized void close()
	  {
	    if (m_env != null)
	    {
	      Set<Map.Entry<String, DBEntry>> dbEntries = m_dbMap.entrySet();
	      for (Map.Entry<String, DBEntry> entry: dbEntries)
	      {
	        DBEntry dbEntry = entry.getValue();
	        try
	        {
	          if (dbEntry.getDb() != null)
	          {
	            dumpDB(entry.getKey(), dbEntry);
	            dbEntry.close();
	          }
	          LogLevelManager.trace(LogArea.STORAGE, LogEvent.DB_CLOSE, this, entry.getKey());
	        } catch (DatabaseException dbe)
	        {
	          LogUtil.fine(LoggerType.TRACE, dbe.toString());
	        }
	        LogLevelManager.trace(LogArea.STORAGE, LogEvent.DB_CLOSE, this, dbEntry.getNamespace());
	        LogUtil.finer(LoggerType.TRACE, "DB Close " + dbEntry.getNamespace());
	        //LogUtil.finer(LoggerType.TRACE, DebugUtil.getCurrentStackTrace());
	      }
        try
        {
	        m_env.close();
        } catch (DatabaseException dbe)
        {
	          LogUtil.fine(LoggerType.TRACE, dbe.toString());
	      }
	    }
	  }

	@Override
	public void clean() {
	    // reopen
	    Set<Map.Entry<String, DBEntry>> dbEntries = m_dbMap.entrySet();
	    // close all database
	    for (Map.Entry<String, DBEntry> entry: dbEntries)
	    {
	      String ns = entry.getKey();
	      DBEntry dbEntry = entry.getValue();
	      Database db = dbEntry.getDb();
	      try
	      {
	    	  db.close();
	    	  LogLevelManager.trace(LogArea.STORAGE, LogEvent.DB_CLOSE, this, ns);
	      } catch (DatabaseException e)
	      {
	    	  LogUtil.fine(LoggerType.TRACE, e.toString());
	      }
	    }
	    //truncate databases
	    for (Map.Entry<String, DBEntry> e1: dbEntries)
	    {
	      String ns = e1.getKey();
	      DBEntry dbEntry = e1.getValue();
	      Database db = dbEntry.getDb();
	      try
	      {
	    	  Environment env = db.getEnvironment();
	    	  env.truncateDatabase(null, ns, false);
	    	  LogLevelManager.trace(LogArea.STORAGE, LogEvent.DB_CLOSE, this,
                        "truncate ", ns);
	      } catch (DatabaseException e)
	      {
	    	  LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
	      }
	    }
	    for (Map.Entry<String, DBEntry> e2: dbEntries)
	    {
	      String ns = e2.getKey();
	      DBEntry dbEntry = e2.getValue();
	      Database db = dbEntry.getDb();
	      try
	      {
	          Environment env = db.getEnvironment();
	          dbEntry.open(env);
	          LogLevelManager.trace(LogArea.STORAGE, LogEvent.DB_OPEN, this,
	                        "open ", ns);
	      } catch (DatabaseException e)
	      {
	          LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
	      }
	    }
	}
	
	@Override
	public String getEnvLocation() {
		try {
			return m_env.getHome().getAbsolutePath();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public long getLogSize() {
		try {
			return m_env.getStats(null).getBufferBytes();
		} catch (Exception e) {
			return 0;
		}
	}

	  @Override
	  public synchronized void addNameSpace(String classDbns, String nameSpace,
	                            boolean transactional, Class<?> indexKeyClass,
	                            Class<?> keyClass, Class<?> objClass) throws StorageException
	  {
	    assert (m_dbMap.get(nameSpace) == null);
	    LogUtil.finer(LoggerType.TRACE, "addNameSpace " + nameSpace + 
	                " db: " + getName()+
	                " transactional: " + transactional +
	                " indexKeyClass: " + (indexKeyClass==null ? "null" : indexKeyClass.getSimpleName()) +
	                " keyClass: " + (keyClass==null ? "null" : keyClass.getSimpleName()) +
	                " objClass: " + (objClass==null ? "null" : objClass.getSimpleName()) );
	    LogLevelManager.trace(LogArea.STORAGE, LogEvent.DB_OPEN, this, nameSpace);
	    try
	    {
	      Database classDb = null;
	      if (objClass != null)
	      {
	        DBEntry dbEntry = m_dbMap.get(classDbns);
	        assert (dbEntry != null) : "no db found for " + classDbns;
	        classDb = dbEntry.getDb();
	      }
	      DBEntry dbEntry =
	        new DBEntry(this, nameSpace, transactional, indexKeyClass, keyClass, objClass, classDb);
	      LogUtil.finer(LoggerType.TRACE, "addNameSpace entry: " + dbEntry.hashCode());
	      dbEntry.open(m_env);
	      m_dbMap.put(nameSpace, dbEntry);
	    }
	    catch(DatabaseException e)
	    {
	      LogUtil.warning(LoggerType.TRACE, e.toString());
	      dumpTxns();
	      throw new StorageException(StorageError.INIT_FAILED, e, nameSpace);
	    }
	    //System.out.println("******** After add " + nameSpace + "\" + getTxnStats());
	  }

	  private synchronized DBEntry getDbEntry(String nameSpace)
	  {
	    DBEntry dbEntry = m_dbMap.get(nameSpace);
	    assert (dbEntry != null) : "no db found for " + nameSpace;
	    return dbEntry;
	  }
	  
	  private Transaction beginTxn(String nameSpace)
	  {
	    try
	    {
	      DBEntry dbEntry = getDbEntry(nameSpace);
	      Database db = dbEntry.getDb();
	      assert (db != null) : "beginTransaction " + nameSpace + " dbEntry:" + dbEntry.hashCode();

	      Environment env = db.getEnvironment();
	      TransactionConfig txnConf = new TransactionConfig();
	      txnConf.setSync(true);
	      txnConf.setReadCommitted(true);
	      return env.beginTransaction(null, txnConf);
	    } catch (DatabaseException ec)
	    {
	      LogUtil.warning(LoggerType.TRACE, ec.toString() +"\n");
	      dumpTxns();
	      throw new StorageException(StorageError.TRANSACTION_FAILED, ec);
	    }
	  }

	  private void addContext(IStorageContext ctx)
	  {
	    if (m_debugDeadLock)
	    {
	      String ownerinfo = Thread.currentThread().toString() + "\n" + DebugUtil.getCurrentStackTrace();
	      m_txns.put(ctx, ownerinfo);
	    }
	  }
	  
	  private void removeContext(IStorageContext ctx)
	  {
	    if (m_debugDeadLock)
	    {
	      //String info = m_txns.get(ctx);
	      //System.out.println("Closing : " + info);
	      m_txns.remove(ctx);
	    }
	  }

	  public IStorageContext beginTransaction(String nameSpace)
	  {
	    Transaction txn = beginTxn(nameSpace);
	    IStorageContext result = new TxnContext(txn);
	    LogLevelManager.trace(LogArea.STORAGE, LogEvent.DB_TXN_BEGIN, this,
	                result);
	    addContext(result);
	    return result;
	  }
	  
	  public void endTransaction(IStorageContext ctx, boolean commit)
	  {
	    if (ctx == null)
	      return;
	    LogLevelManager.trace(LogArea.STORAGE, LogEvent.DB_TXN_END, this,
	                  ctx, commit);
	    
	    TxnContext bdbCtx = (TxnContext)ctx;
	    Transaction txn = bdbCtx.m_txn;
	    if (txn != null)
	    {
	      try
	      {
	        if (commit)
	          txn.commit();
	        else
	          txn.abort();
	      } catch (DatabaseException ec)
	      {
	        LogUtil.warning(LoggerType.TRACE, ec.toString() +"\n");
	        dumpTxns();
	        throw new StorageException(StorageError.TRANSACTION_FAILED, ec);
	      }
	    }
	    removeContext(ctx);
	  }

	  protected boolean putRecordImpl(IStorageContext context, 
	                                  String nameSpace, Object indexKey,
	                                  Object key, Object data)
	  {
	    DBEntry dbEntry = getDbEntry(nameSpace);
	    //deadlock if we synchronize on dbEntry 
	    //if a thread without a lock enter to the synchronized block and waiting for the lock.
	    //while the lock holder of transaction is blocked.
	    //changed to ThreadLocal for indexKey. 
	    //synchronized(dbEntry)
	    //{
	      dbEntry.setIndexKey(indexKey);
	      Database db = dbEntry.getDb();
	      assert (db != null) : "putRecord " + nameSpace + " dbEntry:" + dbEntry.hashCode();
	      
	      boolean res = false;
	      try
	      {
	        DatabaseEntry dbKey = dbEntry.keyToEntry(key);
	        DatabaseEntry dbData = dbEntry.objToEntry(data);
	        Transaction txn = null;
	        if (context != null)
	        {
	          TxnContext txnCtx = (TxnContext)context;
	          txn = txnCtx.m_txn;
	        }
	        m_bdbstat.incTotalRequests();
	        db.put(txn, dbKey, dbData);
	        res = true;
	      } catch (DatabaseException dbe)
	      {
	        LogUtil.warning(LoggerType.TRACE, dbe.toString() +"\n");
	        dumpTxns();
	        throw new StorageException(StorageError.OBJECT_NOT_WRITTEN, dbe, 
	            nameSpace + "." + key.toString());
	      }
	      return res;
	    //}
	    
	  }

	  protected Object getRecordImpl(IStorageContext context, 
	                                 String nameSpace, 
	                                 Object key)
	  {
	    DBEntry dbEntry = getDbEntry(nameSpace);
	    //We don't need to set schema name here for retrieving records.
	    //And also no synchronization is required in this case.
	    Database db = dbEntry.getDb();
	    assert (db != null) : "getRecord " + nameSpace + " dbEntry:" + dbEntry.hashCode();

	    Object result = null;
	    try
	    {
	      DatabaseEntry dbKey = dbEntry.keyToEntry(key);
	      DatabaseEntry dbData = new DatabaseEntry();
	      Transaction txn = null;
	      if (context != null)
	      {
	        TxnContext txnCtx = (TxnContext)context;
	        txn = txnCtx.m_txn;
	      }

	      if (db.get(txn, dbKey, dbData, LockMode.DEFAULT) ==
	          OperationStatus.SUCCESS)
	      {
	        result = dbEntry.entryToObj(dbData);
	      }
	        m_bdbstat.incTotalRequests();
	    } catch (DatabaseException dbe)
	    {
	      LogUtil.warning(LoggerType.TRACE, dbe.toString() +"\n");
	      dumpTxns();
	      throw new StorageException(StorageError.OBJECT_NOT_READ, dbe,
	          nameSpace + "." + key.toString());
	    }
	    return result;
	  }

	  protected boolean deleteRecordImpl(IStorageContext context, 
	                                     String nameSpace, Object indexKey,
	                                     Object key)
	  {
	    DBEntry dbEntry = getDbEntry(nameSpace);
	    //Since schema name is set to DBEntry, the access to the same namespace is synchronized
	    //Synchronization is not required if indexing on schema is turned off (e.g for spilling),
	    //but we do not have multi-threaded spilling in place yet.
	      //dealock..
	      //the lock holder of transaction cannot enter through dbEntry
	    //synchronized(dbEntry)
	    //{
	      dbEntry.setIndexKey(indexKey);
	      Database db = dbEntry.getDb();
	      assert (db != null) : "deleteRecord " + nameSpace + " dbEntry:" + dbEntry.hashCode();
	  
	      boolean res = false;

	      try
	      {
	        DatabaseEntry dbKey = dbEntry.keyToEntry(key);
	        // Perform the deletion. All records that use this key are
	        // deleted.
	        Transaction txn = null;
	        if (context != null)
	        {
	          TxnContext txnCtx = (TxnContext)context;
	          txn = txnCtx.m_txn;
	        }
	        db.delete(txn, dbKey);
	        res = true;
	        m_bdbstat.incTotalRequests();
	        return res;
	      }
	      catch (DatabaseException dbe)
	      {
	        LogUtil.warning(LoggerType.TRACE, dbe.toString() +"\n");
	        dumpTxns();
	        throw new StorageException(StorageError.OBJECT_NOT_DELETED, dbe,
	            nameSpace + "." + key.toString());
	      }
	    //}
	    
	  }

	  public IStorageContext initQuery(String nameSpace, Object indexKey)
	  {
	    LogLevelManager.trace(LogArea.STORAGE, LogEvent.DB_QUERY_BEGIN, this,
	                  nameSpace);
	    DBEntry dbEntry = getDbEntry(nameSpace);
	    IStorageContext result = null;
	    if (indexKey == null)
	    {
	      // If an index key is not given, use the primary database 
	      result = new QueryContext(dbEntry);
	    } 
	    else
	    {
	      // If an index key is given, use the secondary database 
	      result = new IndexQueryContext(dbEntry, indexKey);
	    }
	    addContext(result);
	    return result;
	  }

	  public Object getNextKey(IStorageContext ctx)
	  {
	    QueryContext qctx = (QueryContext)ctx;
	    Object result = qctx.getNextKey();
	    if (result != null)
	    {
	        m_bdbstat.incTotalRequests();
	    }
	    if (result == null)
	      removeContext(ctx);

	    return result;
	  }

	  public Object getNextRecord(IStorageContext ctx)
	  {
	    QueryContext qctx = (QueryContext)ctx;
	    Object result = qctx.getNextRecord();
	    if (result != null)
	    {
	        m_bdbstat.incTotalRequests();
	    }

	    if (result == null)
	      removeContext(ctx);

	    return result;
	  }
	  
	  public void closeQuery(IStorageContext ctx)
	  {
	    QueryContext qctx = (QueryContext)ctx;
	    qctx.close();
	    removeContext(ctx);
	  }
	  
	  public void dumpTxns()
	  {
	    Set<Entry<IStorageContext, String>> entries = m_txns.entrySet();
	    if (entries.size() > 0)
	    {
	      StringBuilder b = new StringBuilder();
	      b.append("Current Txns\n");
	      for (Entry<IStorageContext, String> entry : entries)
	      {
	        IStorageContext ctx = entry.getKey();
	        b.append(ctx.toString());
	        b.append("\n");
	        b.append(entry.getValue());
	        b.append("\n\n");
	      }
	      LogUtil.warning(LoggerType.TRACE, getTxnStats());
	      LogUtil.warning(LoggerType.TRACE, b.toString());
	    }
	  }
	  
	  private String getTxnStats()
	  {
	    StringBuilder b = new StringBuilder();
	    int pos = 0;
	      try
	      {
	        TransactionStats stat = m_env.getTransactionStats(null);
	        b.append("****** " + getName()+" ********\n");
	        b.append("----- TxnStats\n" + stat.toString() + "\n");
	        TransactionStats.Active actives[] = stat.getActiveTxns();
	        if (actives.length > 0)
	        {
	          for ( TransactionStats.Active active : actives)
	          {
	            b.append(active.toString());
	            b.append("\n");
	          }
	        }
	        LockStats lstat = m_env.getLockStats(null);
	        b.append("------ LockStats\n" + lstat.toString() + "\n");
	      } catch (Exception ex)
	      {
	        //eats up any exception
	      }
	      pos++;
	    return b.toString();
	  }

	  
	  protected void dumpOne(PrintWriter o, byte[] ba)
	  {
	    //je 6.2.31 StringBuilder sb = new StringBuilder();
	    StringBuffer sb = new StringBuffer();
	    sb.append(' ');
	    CmdUtil.formatEntry(sb, ba, true);
	    o.println(sb.toString());
	  }

	  private void dumpDB(String name, DBEntry dbEntry)
	  {
	    Database db = dbEntry.getDb();
	    try
	    {
	      long count = db.count();
	      if (count == 0)
	        return;
	      if (DebugUtil.DEBUG_STORAGELEAK)
	      {
	      /* Fortify sees FileWriter as Path Manupulation security issue
			  try {
				String filename = System.getProperty("java.io.tmpdir");
				filename += "/cep/BDBDump_";
				filename += name;
				filename += ".txt";
				LogUtil.fine(LoggerType.TRACE,
							 name + " has " + count + " entries : " + filename);
				Cursor cursor = db.openCursor(null, null);
				DatabaseEntry foundKey = new DatabaseEntry();
				DatabaseEntry foundData = new DatabaseEntry();
				PrintWriter pw = new PrintWriter(new FileWriter(filename));
				while (cursor.getNext(foundKey, foundData,
									  LockMode.READ_UNCOMMITTED) ==
					   OperationStatus.SUCCESS)
				{
				  dumpOne(pw, foundKey.getData());
				  dumpOne(pw, foundData.getData());
				}
				cursor.close();
				pw.close();
			} catch (IOException er)
			{
			  LogUtil.warning(LoggerType.TRACE, er.toString());
			}
		   */
	      } else
	      {
	        LogUtil.fine(LoggerType.TRACE, name + " has " + count + " entries.");
	      }
	    } catch (DatabaseException dbe) {
			LogUtil.warning(LoggerType.TRACE, dbe.toString());
		}
	  }

	  public synchronized void dump(IDumpContext dumper)
	  {
	    String tag = LogUtil.beginDumpObj(dumper, this);
	    String envsTag = "DBEnvironments";
	    String envTag = "DBEnvironment";
	    String[] envAttrs = {"Name", "CacheSize", "Transactional"};
	    Object[] envVals = new Object[3];
	    dumper.beginTag(envsTag, null, null);
	      try
	      {
	        File home = m_env.getHome();
	        
	        EnvironmentConfig cfg = m_env.getConfig();
	        envVals[0] = m_name;
	        envVals[1] = cfg.getCacheSize();
	        envVals[2] = cfg.getTransactional();
	        dumper.beginTag(envTag, envAttrs, envVals);
	        dumper.writeln("Folder", home.getCanonicalPath());
	        dumper.endTag(envTag);
	      } catch (Exception e)
	      {
	        //eats up any exception
	      }
	    dumper.endTag(envsTag);
	    String dbsTag = "DBs";
	    dumper.beginTag(dbsTag, null, null);
		    String dbTag = "DB";
		    String[] dbAttrs = {"Name", "Entries"};
		    Object[] dbVals = new Object[2];
		    for (Entry<String, DBEntry> entry : m_dbMap.entrySet())
		    {
		      try
		      {
		        DBEntry dbEntry = entry.getValue();
		        Database db = dbEntry.getDb();
		        long count = db.count();
		        dbVals[0] = entry.getKey();
		        dbVals[1] = count;
		        dumper.beginTag(dbTag, dbAttrs, dbVals);
		        dumper.endTag(dbTag);
		      } catch (Exception ex)
		      {
		        //eats up any exception
		      }
		    }
	    dumper.endTag(dbsTag);
	    LogUtil.endDumpObj(dumper, tag);
	  }

	  public void trace(IDumpContext dumper, ILogEvent event, int level, Object[] args)
	  {
	    if (level == LogLevel.STORAGE_DBINFO)
	    {
	      dump(dumper);
	      return;
	    }
	    if (level == LogLevel.STORAGE_DBSTAT)
	    {
	      String envsTag = "EnvStats";
	      String envTag = "EnvStat";
	      String[] envAttrs = {"Name"};
	      Object[] envVals = new Object[1];
	      dumper.beginTag(envsTag, null, null);
	      try
	      {
	          EnvironmentStats stat = m_env.getStats(null);
	          envVals[0] = m_name;
	          dumper.beginTag(envTag, envAttrs, envVals);
	          dumper.writeln("UsedBuffer", stat.getBufferBytes());
	          dumper.writeln("CachedData", stat.getDataBytes());
	          dumper.writeln("CachedTotal", stat.getCacheTotalBytes());
	          dumper.writeln("NoCacheMiss", stat.getNCacheMiss());
	          dumper.endTag(envTag);
	        } catch (Exception ex)
	        {
	          //eats up any exception
	        }
	      dumper.endTag(envsTag);
	    }
	  }

	  
}
