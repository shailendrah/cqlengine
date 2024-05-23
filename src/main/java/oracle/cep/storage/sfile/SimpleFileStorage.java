/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/storage/sfile/SimpleFileStorage.java /main/13 2011/05/19 15:28:45 hopark Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 SimpleFileStorage is to provide simpler debugging of storage layer especially for serialization issues. 
 It is not meant for use in normal situation. 
 The following is not implemented:
 - initQuery
 - schema indexing

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      03/31/11 - storage refactor
 hopark      10/10/08 - remove statics
 hopark      10/15/08 - fix ClassNotFoundException
 hopark      09/17/08 - support schema
 hopark      09/12/08 - add schema indexing
 hopark      08/22/08 - add dummy txn for CacheObject (it's asserting txn is not null).
 hopark      06/18/08 - logging refactor
 hopark      03/18/08 - reorg config
 hopark      12/27/07 - support xmllog
 hopark      11/08/07 - add more apis
 najain      03/08/07 - cleanup
 najain      03/02/07 - 
 hopark      01/17/07 - remove previous files
 hopark      01/12/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/storage/sfile/SimpleFileStorage.java /main/12 2008/10/24 15:50:21 hopark Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.storage.sfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.Properties;
import java.util.logging.Level;

import com.oracle.cep.common.util.SecureFile;
import oracle.cep.exceptions.StorageError;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogTags;
import oracle.cep.memmgr.ClassGenBase;
import oracle.cep.storage.BaseStorage;
import oracle.cep.storage.BaseStorageMgr;
import oracle.cep.storage.IStorage;
import oracle.cep.storage.IStorageContext;
import oracle.cep.storage.IStorageMgr;
import oracle.cep.storage.StorageException;
import oracle.cep.util.PathUtil;

/**
 * @version $Header:
 *          pcbpel/cep/server/src/oracle/cep/storage/sfile/SimpleFileStorage
 *          .java /main/12 2008/10/24 15:50:21 hopark Exp $
 * @author hopark
 * @since release specific (what release of product did this appear in)
 */
public class SimpleFileStorage extends BaseStorageMgr {
	static final String TAG_FILES = "Files";
	static final String TAG_FILE = "File";

	private static class SFileDB extends BaseStorage {
		String m_name;
		String m_folder;

		public SFileDB(String n, String folder) {
			m_name = n;
			m_folder = folder;
			PathUtil.ensureFolder(m_folder);
		}

		@Override
		public String getEnvLocation() {
			return m_folder;
		}

		@Override
		public String getName() {
			return m_name;
		}

		@SuppressWarnings("unchecked")
		public void addNameSpace(String classdbns, String ns,
				boolean transactional, Class indexKeyClass, Class keyClass,
				Class objClass) {
		}

		private String getFileName(String nameSpace, Object key) {
			String dbfolder = getEnvLocation();
			assert (dbfolder != null);
			return dbfolder + File.separator + nameSpace + "_" + key.toString();
		}

		protected boolean putRecordImpl(IStorageContext storageContext,
				String nameSpace, Object indexKey, Object key, Object data) {
			String fileName = getFileName(nameSpace, key);
			FileOutputStream fos = null;
			ObjectOutputStream oos = null;
			boolean res = false;
			try {
				fos = new FileOutputStream(fileName);
				oos = new ObjectOutputStream(fos);
				oos.writeObject(data);
				oos.close();
				fos.close();
				res = true;
			} catch (NotSerializableException se) {
				LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, se);
				assert false : se.toString();
			} catch (IOException e) {
				LogUtil.warning(LoggerType.TRACE, e.toString());
			}
			return res;
		}

		protected Object getRecordImpl(IStorageContext storageContext,
				String nameSpace, Object key) {
			String fileName = getFileName(nameSpace, key);
			Object result = null;
			FileInputStream fis = null;
			ObjectInputStream ois = null;
			try {
				fis = new FileInputStream(fileName);
				ois = new ObjectInputStream(fis) {
					protected Class<?> resolveClass(ObjectStreamClass desc)
							throws IOException, ClassNotFoundException {
						if (desc.getName()
								.startsWith("oracle.cep.memmgr.Page_"))
							return ClassGenBase.getClassLoader().loadClass(
									desc.getName());
						return super.resolveClass(desc);
					}
				};
				result = ois.readObject();
			} catch (IOException e) {
				LogUtil.warning(LoggerType.TRACE, e.toString());
			} catch (ClassNotFoundException e) {
				LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
				assert false : e.toString();
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						LogUtil.warning(LoggerType.TRACE, e.toString());
					}
				}
				if (ois != null) {
					try {
						ois.close();
					} catch (IOException e) {
						LogUtil.warning(LoggerType.TRACE, e.toString());
					}
				}
			}
			return result;
		}

		protected boolean deleteRecordImpl(IStorageContext storageContext,
				String nameSpace, Object indexKey, Object key) {
			String fileName = getFileName(nameSpace, key);
			boolean success = false;
			try {
				File sfile = SecureFile.getFile(fileName);
				success = sfile.delete();
			} catch (IOException e) {
				LogUtil.warning(LoggerType.TRACE, e.toString());
			}
			if (!success) {
				// Deletion failed
				LogUtil.warning(LoggerType.TRACE, "failed to delete "
						+ fileName);
			}
			return success;
		}

		public IStorageContext beginTransaction(String nameSpace) {
			return new IStorageContext() {
			};
		}

		public void endTransaction(IStorageContext txn, boolean commit) {
		}

		public IStorageContext initQuery(String nameSpace, Object indexKey) {
			return null;
		}

		public Object getNextKey(IStorageContext ctx) {
			return null;
		}

		public synchronized void dump(IDumpContext dumper) {
			String tag = LogUtil.beginDumpObj(dumper, this);
			String dbfolder = getEnvLocation();
			assert (dbfolder != null);
			File file = new File(dbfolder);
			String[] fileNames = file.list();
			if (fileNames != null) {
				LogUtil.beginTag(dumper, TAG_FILES, LogTags.ARRAY_ATTRIBS,
						fileNames.length);
				for (String filename : fileNames) {
					String path = dbfolder + File.separator + filename;
					dumper.writeln(TAG_FILE, path);
				}
				dumper.endTag(TAG_FILES);
			}
			LogUtil.endDumpObj(dumper, tag);
		}

	}

	public SimpleFileStorage() {
		super();
	}

	public IStorage addDB(String name, Properties props)
			throws StorageException {
		String envFolder = props.getProperty(DB_FOLDER_PROPERTY);
		if (envFolder == null) {
			throw new StorageException(StorageError.INIT_FAILED, null, name);
		}
		String clrval = props.getProperty(START_CLEANUP_PROPERTY);
		boolean cleanup = clrval != null && clrval.equalsIgnoreCase("true");
		File file = new File(envFolder);
		file.mkdir();
		if (cleanup) {
			String[] fileNames = file.list();
			if (fileNames != null) {
				for (String filename : fileNames) {
					String path = envFolder + File.separator + filename;
					boolean success = (new File(path)).delete();
					if (!success) {
						LogUtil.warning(LoggerType.TRACE, "failed to delete "
								+ path);
					}
				}
			}
		}
		SFileDB db = new SFileDB(name, envFolder);
		return db;
	}

}
