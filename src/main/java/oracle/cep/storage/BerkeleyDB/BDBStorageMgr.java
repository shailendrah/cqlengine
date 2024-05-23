/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/storage/BerkeleyDB/BDBStorageMgr.java /main/1 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Storage implementation using the Berkeley DB JE.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES

   MODIFIED    (MM/DD/YY)
    hopark      03/31/11 - storage refactor
    parujain    08/20/09 - cache shared amongst env
    hopark      02/18/09 - remove unnecessary stack dump
    hopark      11/03/08 - add debug.deadLock property
    hopark      10/13/08 - fix deadlock on putRecord
    hopark      10/22/08 - add getTxnStats
    hopark      10/03/08 - add closeQuery
    hopark      09/23/08 - add getNextRecord
    hopark      09/12/08 - add schema indexing
    hopark      08/19/08 - cleanup metadata on startup
    hopark      06/18/08 - logging refactor
    hopark      05/08/08 - add logging
    hopark      04/21/08 - set classloader
    hopark      03/24/08 - add config log
    hopark      03/18/08 - reorg config
    hopark      02/07/08 - implement dump
    hopark      02/05/08 - parameterized error
    hopark      12/27/07 - support xmllog
    mthatte     08/16/07 - 
    hopark      08/01/07 - support dynamic tuple class
    hopark      06/03/07 - logging
    hopark      05/31/07 - do not dump reports by default
    hopark      05/16/07 - remove printStackTrace
    parujain    05/03/07 - Statistics info
    hopark      04/17/07 - storage leak debug
    hopark      03/14/07 - support metadata
    najain      03/08/07 - cleanup
    hopark      01/19/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/storage/BerkeleyDB/BDBStorage.java /main/32 2010/07/08 11:42:23 apiper Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.storage.BerkeleyDB;


import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import oracle.cep.exceptions.StorageError;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

import oracle.cep.storage.BaseStorageMgr;
import oracle.cep.storage.IStorage;
import oracle.cep.storage.IStorageMgr;
import oracle.cep.storage.StorageException;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/storage/BerkeleyDB/BDBStorage.java /main/32 2010/07/08 11:42:23 apiper Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
public class BDBStorageMgr extends BaseStorageMgr
{
  private List<BDBStorage> m_dbEnvs;
  public BDBStorageMgr()
  {
    super();
    m_dbEnvs = new LinkedList<BDBStorage>();
  }

  static long stringToLong(String val)
  {
	if (val == null || val.length() == 0)
		return USE_BDB_DEFAULT;
	long lval = 0;
	try
	{
		lval = Long.parseLong(val);
	} catch(NumberFormatException e)
	{
		LogUtil.warning(LoggerType.TRACE, "Failed to convert :"+val+"\n"+e.toString());
		return USE_BDB_DEFAULT;
	}
	return handlePercentVal(lval);
  }
  
  static long handlePercentVal(long lval)
  {
	if (lval < 0 && lval != USE_BDB_DEFAULT) {
		long totalMem = Runtime.getRuntime().totalMemory();
		lval = totalMem * (-lval) / 100;
	}
	return lval;
  }
  
  public IStorage addDB(String name, Properties props) throws StorageException
  {
	  String envFolder = props.getProperty(DB_FOLDER_PROPERTY);
	  if (envFolder == null)
	  {
		  throw new StorageException(StorageError.INIT_FAILED, null, name);
	  }
	  String cval = props.getProperty(CACHE_SIZE_PROPERTY);
	  String clrval = props.getProperty(START_CLEANUP_PROPERTY);
	  long cacheSize = stringToLong(cval);
	  boolean cleanup = clrval != null && clrval.equalsIgnoreCase("true");
	  BDBStorage dbenv = new BDBStorage(name);
	  dbenv.init(envFolder, cacheSize, cleanup);
	  m_dbEnvs.add(dbenv);
	  return dbenv;
  }
  
  public void close() throws StorageException
  {
	  for (BDBStorage dbenv : m_dbEnvs)
	  {
		  dbenv.close();
	  }
  }

}
