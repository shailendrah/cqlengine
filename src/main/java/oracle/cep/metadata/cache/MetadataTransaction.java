/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/cache/MetadataTransaction.java /main/2 2009/11/23 21:21:22 parujain Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    09/29/09 - onCommit
    parujain    02/05/09 - metadata Transaction
    parujain    02/05/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/cache/MetadataTransaction.java /main/2 2009/11/23 21:21:22 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata.cache;

import java.util.Hashtable;
import java.util.Iterator;

import oracle.cep.service.ExecContext;
import oracle.cep.transaction.ITransaction;
import oracle.cep.storage.IStorageContext;

public class MetadataTransaction implements ITransaction
{
 // ArrayList<CacheLock>  locks;
  Hashtable<CacheKey, CacheLock>  locks;

  IStorageContext       storageCtx;

  public MetadataTransaction(IStorageContext ctx)
  {
    locks = new Hashtable<CacheKey, CacheLock>();
    storageCtx = ctx;
  }

  public void addLock(CacheKey key, CacheLock l)
  { 
    locks.put(key, l); 
//    locks.add(l); 
  }
  
  public CacheLock getLock(CacheKey key)
  {
    return locks.get(key);
  }

  public IStorageContext getStorageContext()
  {
    return storageCtx;
  }


  @Override
  public void commit(ExecContext ec) {
    // Release all the locks with purge false 
    Iterator<CacheLock> iter = locks.values().iterator(); 
    while(iter.hasNext())
    {
      ec.getCache().release(storageCtx, iter.next(), false);
    }
    ec.getCache().removeContext(storageCtx, true);
  }

  @Override
  public void rollback(ExecContext ec) {
        // Release all the locks with purge true
    Iterator<CacheLock> iter = locks.values().iterator();
    while(iter.hasNext())
    {
      CacheLock next = iter.next();
      ec.getCache().onRollback(ec, next);
      ec.getCache().release(storageCtx, next, true);
    }
    ec.getCache().removeContext(storageCtx, false);
  }

}

