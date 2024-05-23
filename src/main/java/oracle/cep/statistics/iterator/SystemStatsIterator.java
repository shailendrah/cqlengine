/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/statistics/iterator/SystemStatsIterator.java /main/3 2010/07/27 10:57:00 vikshukl Exp $ */

/* Copyright (c) 2008, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    07/26/10 - #(9902008): getNext() - fix infinite loop
    parujain    01/29/09 - transaction mgmt
    parujain    12/08/08 - system stats iterator
    parujain    12/08/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/statistics/iterator/SystemStatsIterator.java /main/3 2010/07/27 10:57:00 vikshukl Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.statistics.iterator;

import oracle.cep.exceptions.CEPException;
import oracle.cep.service.ExecContext;
import oracle.cep.statistics.IStats;

public class SystemStatsIterator extends StatsIterator
{
  public SystemStatsIterator(ExecContext ec)
  {
    super(ec);
  }

  public void close() {}

  public IStats getNext() throws CEPException {
    if(factory == null)
      return null;

    IStats stats =
      factory.createSystemStat(cepMgr.getSystemMgr().getFreeMemory(),
          cepMgr.getSystemMgr().getMaxMemory(),
          cepMgr.getSystemMgr().getTime(),
          cepMgr.getSystemMgr().getTotalMemory(),
          cepMgr.getSystemMgr().getUsedMemory(),
          execContext.getSchedMgr().getNumThreads());

    /* #(9902008): there is only one entry for system stats, set the factory
     * to null. */
    setStatsRowFactory(null);

    return stats;
  }

  public void init() {}
}

