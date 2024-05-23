/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/storage/StorageManager.java /main/9 2011/05/19 15:28:45 hopark Exp $ */

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
    hopark      03/31/11 - storage refactor
    hopark      08/14/08 - add MemStorage
    hopark      03/17/08 - config reorg
    hopark      04/17/07 - add close
    hopark      03/14/07 - support metadata
    najain      03/08/07 - cleanup
    najain      03/02/07 - 
    hopark      01/19/07 - add BDBStorage
    hopark      01/17/07 - add init
    hopark      01/12/07 - add storage config
    najain      01/10/07 - add hoyong's changes
    najain      01/10/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/storage/StorageManager.java /main/8 2008/09/14 17:07:44 hopark Exp $
 *  @author  najain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.storage;

import java.io.File;
import java.util.Properties;

import oracle.cep.exceptions.CEPException;
import oracle.cep.metadata.ConfigManager;
//import oracle.cep.storage.BerkeleyDB.BDBStorageMgr;
import oracle.cep.storage.mem.MemStorageMgr;
import oracle.cep.storage.sfile.SimpleFileStorage;

//**************************************
//TODO move this to oracle.cep.service
//**************************************

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/storage/StorageManager.java /main/8 2008/09/14 17:07:44 hopark Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
public class StorageManager
{
    public static final String SPILL_STORAGE_PROPERTY = "spill";
    public static final String METADATA_STORAGE_PROPERTY = "metadata";

    private static final String METADATA_DB_NAME = "Metadata";
    private static final String SPILL_DB_NAME = "Spill";

    public static final String CLASSDB_NAMESPACE = "ClassDB";
    
    private static final int SFILE_STORAGE = 0;
    private static final int BDB_STORAGE = 1;
    private static final int MEM_STORAGE = 2;
    private static final int MAX_STORAGE = 3;
    
    private static final String SFILE_STORAGE_NAME = "SFileStorage";
    private static final String BDB_STORAGE_NAME = "BDBStorage";
    private static final String MEM_STORAGE_NAME = "MemStorage";

    private IStorageMgr[] m_storages;
    
    private IStorage m_spillStorage;
    private IStorage m_metadataStorage;
    
    private IStorageMgr m_spillStorageMgr;
    private IStorageMgr m_metadataStorageMgr;
    
    public StorageManager()
    {
        m_storages = new IStorageMgr[MAX_STORAGE];
        for (int i = 0; i < MAX_STORAGE; i++)
          m_storages[i] = null;
      
      m_spillStorage = null;
      m_metadataStorage = null;
    }

    public void init(ConfigManager cfg) throws CEPException
    {
        String spillStorage = cfg.getSpillStorageName();
        String metadataStorage = cfg.getMetadataStorageName();

        // For development purpose and junit testing, use system property to override config
        String val = System.getProperty(SPILL_STORAGE_PROPERTY, null);
        if (val != null && val.length() > 0) 
        {
            spillStorage = val;
        }
        val = System.getProperty(METADATA_STORAGE_PROPERTY, null);
        if (val != null && val.length() > 0) 
        {
            metadataStorage = val;
        }

        if (metadataStorage != null )
        {
          m_metadataStorageMgr = getStorageByName(metadataStorage);
          if (m_metadataStorageMgr != null)
          {
            Properties props = new Properties();
            String metaenv = cfg.getMetadataStorageFolder();
            props.put(IStorageMgr.DB_FOLDER_PROPERTY, metaenv);
            props.put(IStorageMgr.CACHE_SIZE_PROPERTY, Long.toString(IStorageMgr.USE_BDB_DEFAULT));
            props.put(IStorageMgr.START_CLEANUP_PROPERTY, Boolean.toString(cfg.getIsMetadataCleanupOnStartup()));
            m_metadataStorage = m_metadataStorageMgr.addDB(METADATA_DB_NAME, props);
            m_metadataStorage.addNameSpace(null, CLASSDB_NAMESPACE, false, null, null, null);
          }
        }
        if (spillStorage != null)
        {
          if (!spillStorage.equals(metadataStorage))
          {
            m_spillStorageMgr = getStorageByName(spillStorage);
            if (m_spillStorageMgr != null)
            {
              Properties props = new Properties();
              String spillfolder = cfg.getSpillStorageFolder();
              props.put(IStorageMgr.DB_FOLDER_PROPERTY, spillfolder);
              props.put(IStorageMgr.CACHE_SIZE_PROPERTY, Long.toString(cfg.getStorageCacheSize()));
              props.put(IStorageMgr.START_CLEANUP_PROPERTY, "true");
              m_spillStorage = m_spillStorageMgr.addDB(SPILL_DB_NAME, props);
            }
          } else m_spillStorage = m_metadataStorage;
        }
    }

    public void start()
    {
    }
    
    public void close()
    {
      if (m_spillStorageMgr != null)
        m_spillStorageMgr.close();
      if (m_metadataStorageMgr != null && (m_metadataStorage != m_spillStorage))
        m_metadataStorageMgr.close();
    }

    public IStorage getSpillStorage()
    {
      return m_spillStorage;
    }
    
    public IStorage getMetadataStorage()
    {
      return m_metadataStorage;  
    }

    private IStorageMgr getStorageByName(String name)
    {
      if (name == null || name.length() == 0)
        return null;
      
        if (name.equals(SFILE_STORAGE_NAME))
        {
          IStorageMgr s = m_storages[SFILE_STORAGE];
          if (s == null)
          {
            s = new SimpleFileStorage();
            m_storages[SFILE_STORAGE] = s;
          }
          return s;
        }
        if (name.equals(BDB_STORAGE_NAME))
        {
          IStorageMgr s = m_storages[BDB_STORAGE];
          if (s == null)
          {
            throw new RuntimeException("BDBStorgage is disabled.");
            //s = new BDBStorageMgr();
            //m_storages[BDB_STORAGE] = s;
          }
          return s;
        }
        if (name.equals(MEM_STORAGE_NAME))
        {
          IStorageMgr s = m_storages[MEM_STORAGE];
          if (s == null)
          {
            s = new MemStorageMgr();
            m_storages[MEM_STORAGE] = s;
          }
          return s;
        }
        //assert false : "unknown storage name";
        return null;
    }
}

