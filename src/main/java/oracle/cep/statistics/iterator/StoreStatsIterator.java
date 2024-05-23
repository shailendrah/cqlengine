/* $Header: pcbpel/cep/server/src/oracle/cep/statistics/iterator/StoreStatsIterator.java /main/3 2009/05/12 19:25:47 parujain Exp $ */

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
    parujain    04/21/09 - store stats
    parujain    01/29/09 - transaction mgmt
    parujain    12/08/08 - store stats iterator
    parujain    12/08/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/statistics/iterator/StoreStatsIterator.java /main/3 2009/05/12 19:25:47 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.statistics.iterator;

import java.util.Iterator;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.service.ExecContext;
import oracle.cep.statistics.IStats;

public class StoreStatsIterator extends StatsIterator
{
  // iterator to traverse the stores array
  private Iterator<PhyStore> itr;

  public StoreStatsIterator(ExecContext ec)
  {
    super(ec);
  }

  public void init()
  {
    itr = execContext.getPlanMgr().getStoreListIterator();
  }

  public IStats getNext() throws CEPException
  {
    if(factory == null)
      return null;

    if (itr == null)
      return null;

    PhyStore store = null;
    
    if (itr.hasNext())
      store = itr.next();

    if (store == null)
      return null;

      // we can have nulls in the list
     ExecStore estore = store.getInstStore();

     if (estore != null){
       IStats storeStat = factory.createStoreStat(estore.getId(), 
                                  estore.getPhyId(), estore.getNumElems());
         
      return storeStat;
     }
    return null;
  }

  public void close()
  {
    itr = null;
  }


}
