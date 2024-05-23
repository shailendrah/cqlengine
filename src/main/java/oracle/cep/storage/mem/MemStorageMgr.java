/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/storage/mem/MemStorageMgr.java /main/1 2011/05/19 15:28:46 hopark Exp $ */

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

import java.util.Properties;

import oracle.cep.storage.BaseStorageMgr;
import oracle.cep.storage.IStorage;
import oracle.cep.storage.StorageException;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/storage/mem/MemStorage.java /main/5 2009/03/19 20:24:41 parujain Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
public class MemStorageMgr extends BaseStorageMgr
{
	public MemStorageMgr()
	{
		super();
	}
  
	@Override
	public IStorage addDB(String name, Properties property)
			throws StorageException 
			{
	  	MemStorage db = new MemStorage(name);
	  	return db;
	}

	@Override
	public void close() throws StorageException 
	{
	}

}
