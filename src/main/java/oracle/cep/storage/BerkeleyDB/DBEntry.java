/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/storage/BerkeleyDB/DBEntry.java /main/5 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/01/11 - IStorageDB
    hopark      10/15/08 - fix StreamCorruptedException
    hopark      09/12/08 - add keyGenerator
    hopark      05/08/08 - add namespace
    mthatte     08/16/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/storage/BerkeleyDB/DBEntry.java /main/4 2008/10/17 15:45:36 hopark Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.storage.BerkeleyDB;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.ClassGenBase;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryDatabase;

 public class DBEntry implements Serializable
  {
    private static final long serialVersionUID = 4068702533586257287L;

    private BDBStorage  m_dbenv;
    private String      m_namespace;
    private boolean     m_transactional;
    private Database    m_db;
    @SuppressWarnings("unchecked")
	private EntryBinding m_indexKeyBinding;
    @SuppressWarnings("unchecked")
	private EntryBinding m_keyBinding;
    @SuppressWarnings("unchecked")
	private EntryBinding m_binding;
    private SecondaryDatabase m_secDb;
    private IndexKeyCreator m_keyCreator;
    
    @SuppressWarnings("unchecked")
    DBEntry(BDBStorage env, String ns, boolean transactional, 
                   Class<?> indexKeyClass, Class<?> keyClass, Class<?> objClass,
                   Database classDB)
    {
      m_dbenv = env;
      m_namespace = ns;
      m_transactional = transactional;
      m_indexKeyBinding = null;
      m_keyBinding = null;
      m_binding = null;
      if (classDB != null)
      {
        try 
        {
          StoredClassCatalog catalog = new StoredClassCatalog(classDB);
          if (keyClass != null)
            m_keyBinding = new SerialBinding(catalog, keyClass);
          if (objClass != null)
            m_binding = new SerialBinding(catalog, objClass);
          if (indexKeyClass != null)
            m_indexKeyBinding = new SerialBinding(catalog, indexKeyClass);
        } 
        catch (IllegalArgumentException e)
        {
          LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
        } 
        catch (DatabaseException e)
        {
          LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
        }
      }
    }
    
    BDBStorage			getDbEnv() {return m_dbenv;}
    String              getNamespace() {return m_namespace;}
    Database            getDb() {return m_db;}
    SecondaryDatabase   getIndexDb() {return m_secDb;}
    
    void setIndexKey(Object indexKey)
    {
      if (m_keyCreator != null)
      {
        m_keyCreator.setIndexKey(indexKey);
      }
    }

    
    void open(Environment env) 
      throws DatabaseException
    {
      assert (m_namespace != null);
      DatabaseConfig dbConfig = new DatabaseConfig();
      dbConfig.setAllowCreate(true);
      dbConfig.setTransactional(m_transactional);
      m_db = env.openDatabase(null, m_namespace, dbConfig);
      m_keyCreator = null;
      m_secDb = null;
      if (m_indexKeyBinding != null)
      {
        String dbName = m_namespace + "_Index";
        m_keyCreator = new IndexKeyCreator(m_indexKeyBinding);
        SecondaryConfig secdbConfig = new SecondaryConfig();
        secdbConfig.setAllowCreate(true);
        secdbConfig.setTransactional(m_transactional);
        secdbConfig.setSortedDuplicates(true);
        secdbConfig.setKeyCreator(m_keyCreator);
        m_secDb = env.openSecondaryDatabase(null, dbName, m_db, secdbConfig);
      }
    }
    
    void close()
      throws DatabaseException
    {
        if (m_secDb != null)
        {
          m_secDb.close();
          m_secDb = null;
        }
        if (m_db!= null)
        {
          m_db.close();
          m_db = null;
        }
    }
    
    @SuppressWarnings("unchecked")
    private DatabaseEntry objToEntry(EntryBinding binding, Object key)
    {
      DatabaseEntry keyEntry = new DatabaseEntry();
      if (binding == null)
      {
        //try tuplebinding from the class of given key.
        binding = TupleBinding.getPrimitiveBinding(key.getClass());
      }
      if (binding != null)
      {
        binding.objectToEntry(key, keyEntry);
        return keyEntry;
      }
      // if key is not primitive or no binding is set.
      // use string representation of key.
      try
      {
        String str = key.toString();
        keyEntry.setData(str.getBytes("UTF-8"));
        return keyEntry;
      } catch (UnsupportedEncodingException e)
      {
        LogUtil.fine(LoggerType.TRACE, e.toString());
      }
      return null;
    }    
        
    DatabaseEntry keyToEntry(Object key)
    {
      return objToEntry(m_keyBinding, key);
    }    
    
    DatabaseEntry indexKeyToEntry(Object key)
    {
      return objToEntry(m_indexKeyBinding, key);
    }    

    @SuppressWarnings("unchecked")
    DatabaseEntry objToEntry(Object data)
    {
      if (m_binding == null)
      {
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try
        {
          bos = new ByteArrayOutputStream();
          oos = new ObjectOutputStream(bos);
          oos.writeObject(data);
          oos.flush();
          oos.close();
          bos.close();
          return new DatabaseEntry(bos.toByteArray());
        } 
        catch (NotSerializableException se)
        {
          LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, se);
          assert false : se.toString();
        } catch (IOException e)
        {
          LogUtil.warning(LoggerType.TRACE, e.toString());
        } 
        return null;
      } else
      {
        DatabaseEntry dbData = new DatabaseEntry();
        m_binding.objectToEntry(data, dbData);
        return dbData;
      }
    }
    
    Object entryToKey(DatabaseEntry dbKey)
    {
      assert (m_keyBinding != null);
      return m_keyBinding.entryToObject(dbKey);
    }
    
    Object entryToObj(DatabaseEntry dbData)
    {
      if (m_binding == null)
      {
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try
        {
          bis = new ByteArrayInputStream(dbData.getData());
          // Need to use the classloader from ClassGenBase
          // to resolve dynamically generated tuple/page classes.
          ois = new ObjectInputStream(bis) {
            protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
              if (desc.getName().startsWith("oracle.cep.memmgr.Page_"))
                return ClassGenBase.getClassLoader().loadClass(desc.getName());
              return super.resolveClass(desc);
            }
          }; 
          return ois.readObject();
        } catch (IOException e)
        {
          LogUtil.warning(LoggerType.TRACE, e.toString());
        } catch (ClassNotFoundException e)
        {
          LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
          assert false : e.toString();
        } finally
        {
          if (ois != null)
          {
            try
            {
              ois.close();
            } catch (IOException e)
            {
              LogUtil.warning(LoggerType.TRACE, e.toString());
            }
          }
          if (bis != null)
          {
            try
            {
              bis.close();
            } catch (IOException e)
            {
              LogUtil.warning(LoggerType.TRACE, e.toString());
            }
          }

        }
        return null;
      } else
      {
        return m_binding.entryToObject(dbData);
      }
    }
}
