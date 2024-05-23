/* $Header: pcbpel/cep/server/src/oracle/cep/statistics/iterator/StatsIterator.java /main/2 2009/02/06 15:51:04 parujain Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    01/29/09 - transaction mgmt
    parujain    12/08/08 - stats iterator
    parujain    12/08/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/statistics/iterator/StatsIterator.java /main/2 2009/02/06 15:51:04 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.statistics.iterator;

import oracle.cep.exceptions.CEPException;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;
import oracle.cep.statistics.IStats;
import oracle.cep.statistics.IStatsIterator;
import oracle.cep.statistics.IStatsFactory;

public abstract class StatsIterator implements IStatsIterator
{
  protected CEPManager cepMgr;
  protected ExecContext execContext;
  protected IStatsFactory factory;

  public StatsIterator(ExecContext ec)
  {
    this.execContext = ec;
    this.cepMgr = ec.getServiceManager();
    this.factory = null;
  }

  public void setStatsRowFactory(IStatsFactory factory)
  {
    this.factory = factory;
  }

  public abstract void init();
  public abstract IStats getNext() throws CEPException;
  public abstract void close();

}

