/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/storage/BaseStorageMgr.java /main/1 2011/05/19 15:28:46 hopark Exp $ */

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
    hopark      06/18/08 - logging refactor
    hopark      02/07/08 - fix dbinfo dumpg
    hopark      01/01/08 - trace cleanup
    hopark      08/03/07 - structured log
    hopark      06/20/07 - cleanup
    hopark      06/04/07 - logging
    parujain    05/03/07 - BDB statistics
    hopark      03/14/07 - support metadata
    hopark      03/13/07 - 
    najain      03/02/07 - 
    hopark      02/27/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/storage/BaseStorage.java /main/18 2009/01/16 22:55:00 parujain Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.storage;

public abstract class BaseStorageMgr implements IStorageMgr
{
  public BaseStorageMgr()
  {
  }
  
  @Override
  public void close() throws StorageException
  {
	  
  }
}
