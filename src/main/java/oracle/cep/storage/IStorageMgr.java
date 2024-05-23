/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/storage/IStorageMgr.java /main/1 2011/05/19 15:28:46 hopark Exp $ */

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
    parujain    01/13/09 - metadata in-memory
    hopark      12/02/08 - move LogLevelManager to ExecContext
    hopark      10/03/08 - add closeQuery
    hopark      09/23/08 - add getNextRecord
    hopark      09/12/08 - add schema indexing
    hopark      03/18/08 - reorg config
    hopark      08/01/07 - extend from ILoggable
    parujain    05/03/07 - BDB statistics
    hopark      03/14/07 - support metadata
    najain      03/02/07 - 
    hopark      01/17/07 - add init
    najain      01/10/07 - add hoyong's changes
    najain      01/10/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/storage/IStorage.java /main/10 2009/01/16 22:55:00 parujain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.storage;

import java.util.Properties;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/storage/IStorage.java /main/10 2009/01/16 22:55:00 parujain Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
public interface IStorageMgr
{
  public static final long USE_BDB_DEFAULT = -100L;
  public static final String DB_FOLDER_PROPERTY = "storage.folder";
  public static final String CACHE_SIZE_PROPERTY = "storage.cache";
  public static final String START_CLEANUP_PROPERTY = "storage.start.cleanup";

  IStorage addDB(String name, Properties property) throws StorageException;
  void close() throws StorageException;
}

