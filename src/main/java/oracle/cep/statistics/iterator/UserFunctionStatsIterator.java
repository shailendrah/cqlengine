/* $Header: pcbpel/cep/server/src/oracle/cep/statistics/iterator/UserFunctionStatsIterator.java /main/2 2009/02/06 15:51:04 parujain Exp $ */

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
    parujain    12/08/08 - user function stats iterator
    parujain    12/08/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/statistics/iterator/UserFunctionStatsIterator.java /main/2 2009/02/06 15:51:04 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.statistics.iterator;

import java.util.Iterator;

import oracle.cep.exceptions.CEPException;
import oracle.cep.metadata.UserFunction;
import oracle.cep.metadata.cache.CacheObjectType;
import oracle.cep.service.ExecContext;
import oracle.cep.transaction.ITransaction;
import oracle.cep.statistics.IStats;

public class UserFunctionStatsIterator extends StatsIterator
{
  private Iterator<Integer> iter;
  private UserFuncStats fnStat;

  public UserFunctionStatsIterator(ExecContext ec)
  {
    super(ec);
  }

  public void init()
  {
    iter = execContext.getExecStatsMgr().getFunctionKeysIterator();
    fnStat = new UserFuncStats();
  }

  public IStats getNext() throws CEPException
  {
    if(factory == null)
      return null;

    if(iter == null)
      return null;

    IStats stats = null;
    ITransaction txn = execContext.getTransactionMgr().begin();
    execContext.setTransaction(txn);

    if( iter.hasNext())
    {
       Integer fnId = iter.next();

       if(fnId == null)
       {
         execContext.getTransactionMgr().commit(txn);
         execContext.setTransaction(null);
         return null;
       }

       stats = getFunctionStat(fnId);

       execContext.getTransactionMgr().commit(txn);
       execContext.setTransaction(null);
       return stats;
     }
     execContext.getTransactionMgr().commit(txn);
     execContext.setTransaction(null);
     return null;

  }

  public void close()
  {
    iter = null;
    fnStat = null;
  }

  private IStats getFunctionStat(Integer fnId) throws CEPException
  {
    UserFunction fn = execContext.getUserFnMgr().getSimpleOrAggFunction(fnId.intValue());
    execContext.getExecStatsMgr().getFunctionStats(fnId, fnStat);

    boolean isAggr = (fn.getType() == CacheObjectType.AGGR_FUNCTION);

    return factory.createUserFunctionStat(fn.getName(), fnId.intValue(),
                         isAggr, fn.getCreationText(), fn.getImplClassName(),
                               fnStat.getNumInvokations(), fnStat.getTime());
  }
}

