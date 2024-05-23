/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/statistics/iterator/DBStatsIterator.java /main/3 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2008, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/03/11 - refactor storage
    parujain    01/29/09 - transaction mgmt
    parujain    12/08/08 - DB stats iterator
    parujain    12/08/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/statistics/iterator/DBStatsIterator.java /main/2 2009/02/06 15:51:04 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.statistics.iterator;

import oracle.cep.exceptions.CEPException;
import oracle.cep.service.ExecContext;
import oracle.cep.transaction.ITransaction;
import oracle.cep.storage.IStorage;
import oracle.cep.storage.StorageStat;
import oracle.cep.statistics.IStats;

public class DBStatsIterator extends StatsIterator
{
  IStorage storage;

  public DBStatsIterator(ExecContext ec)
  {
    super(ec);
  }

  public void close() {
    storage = null;
  }

  public IStats getNext() throws CEPException
  {
    if(storage == null)
      return null;

    if(factory == null)
      return null;

    ITransaction txn = execContext.getTransactionMgr().begin();
    execContext.setTransaction(txn);
    StorageStat stat = storage.getStat();
    IStats stats = factory.createDBStat(storage.getEnvLocation(),
                                      storage.getCacheSize(),
                                      storage.getLogSize(),
                                      stat.getCacheMisses(),
                                      (int) stat.getTotalRequests());
    execContext.getTransactionMgr().commit(txn);
    execContext.setTransaction(null);
    return stats;
  }

  public void init() {
    storage = cepMgr.getStorageManager().getMetadataStorage();
  }

}

