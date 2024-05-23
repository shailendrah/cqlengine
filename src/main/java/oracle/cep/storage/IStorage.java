/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/storage/IStorage.java /main/11 2011/05/19 15:28:45 hopark Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

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
 *  @version $Header: IStorageDB.java 01-apr-2011.12:07:21 hopark   Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.storage;

public interface IStorage {
	String getName();

	void addNameSpace(String classDBNS, String nameSpace,
			boolean transactional, Class<?> indexKeyClass, Class<?> keyClass,
			Class<?> objClass) throws StorageException;

	IStorageContext beginTransaction(String nameSpace);

	void endTransaction(IStorageContext txn, boolean commit);

	boolean putRecord(IStorageContext context, String nameSpace,
			Object indexKey, Object key, Object data);

	Object getRecord(IStorageContext context, String nameSpace, Object key);

	boolean deleteRecord(IStorageContext context, String nameSpace,
			Object indexKey, Object key);

	boolean updateRecord(IStorageContext context, String nameSpace, Object key,
			Object data);

	IStorageContext initQuery(String nameSpace, Object indexKey);

	Object getNextKey(IStorageContext context);

	Object getNextRecord(IStorageContext context);

	void closeQuery(IStorageContext context);

	void lockRecordForUpdate(IStorageContext context, String nameSpace,
			Object key) throws Exception;

	void unlockRecordForUpdate(IStorageContext txn, String nameSpace, Object key);

	StorageStat getStat();

	String getEnvLocation();
	long getCacheSize();
	long getLogSize();
	
	void clean();
	void open();
	void close();
}
