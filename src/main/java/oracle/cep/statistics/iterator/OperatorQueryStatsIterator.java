/* $Header: pcbpel/cep/server/src/oracle/cep/statistics/iterator/OperatorQueryStatsIterator.java /main/2 2009/02/06 15:51:04 parujain Exp $ */

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
    parujain    12/08/08 - operator query stats iterator
    parujain    12/08/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/statistics/iterator/OperatorQueryStatsIterator.java /main/2 2009/02/06 15:51:04 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.statistics.iterator;

import java.util.Iterator;

import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.planmgr.PlanManager;
import oracle.cep.service.ExecContext;
import oracle.cep.transaction.ITransaction;
import oracle.cep.statistics.IStats;
import oracle.cep.exceptions.CEPException;

enum InternalState
{
  INIT, GET_NEXT_OP, GET_NEXT_QRYID_ITER, GET_NEXT_QRY_ID;
}

public class OperatorQueryStatsIterator extends StatsIterator
{
   // iterator to traverse the operator array
  private Iterator<ExecOpt> itrOpt;

  private Iterator<Integer> itrQryIds;

  private InternalState     state;
  private ExecOpt           op;

  // Loop over all the execution operators and return the statistics
  // corresponding to them. Note that we deliberately do not take a latch
  // while traversing the operators. So, it is possible that we miss an
  // operator if it gets added in the beginning of the operator array after
  // we have traversed the initial segment of the array. Also, it is possible
  // that the different operators are showing statistics for different
  // timestamps
  public OperatorQueryStatsIterator(ExecContext ec)
  {
    super(ec);
  }

  public void init()
  {
    itrOpt = execContext.getExecMgr().getExecOpIterator();
    itrQryIds = null;
    state = InternalState.INIT;
  }

  public IStats getNext() throws CEPException
  {
    int qryId = 0;
    boolean found = false;
    ITransaction txn = execContext.getTransactionMgr().begin();
    execContext.setTransaction(txn);

    PlanManager planMgr = execContext.getPlanMgr();
    while (!found)
    {
      switch (state)
      {
        case INIT:
          if(factory == null)
            return null;
          if (itrOpt == null)
            return null;
              op = null;
          state = InternalState.GET_NEXT_OP;
        case GET_NEXT_OP:
          assert itrOpt != null;
          if (itrOpt.hasNext())
            op = itrOpt.next();
          if (op == null)
          {
            state = InternalState.INIT;
            return null;
          }
          state = InternalState.GET_NEXT_QRYID_ITER;
        case GET_NEXT_QRYID_ITER:
          assert op != null;
          assert itrQryIds == null;
          int phyOptId = op.getPhyOptId();
          PhyOpt phyOp = planMgr.getPhyOpt(phyOptId);
          assert phyOp != null;
          itrQryIds = phyOp.getQryIds().iterator();
          assert itrQryIds != null;
          state = InternalState.GET_NEXT_QRY_ID;
        case GET_NEXT_QRY_ID:
          if (itrQryIds.hasNext())
          {
            qryId = itrQryIds.next();
            found = true;
          }
          else
          {
            state = InternalState.GET_NEXT_OP;
            op = null;
            itrQryIds = null;
          }
          break;
        default:
          assert false;
      }
    }

    // Note that since we dont know when the memory for this row will get
    // freed, we haven't integrated this with the StorageFactory. Once the
    // JMX piece gets finalized and we have more clarity on this, we should
    // integrate this with the SimpleStorageFactory.
    IStats opQryStats = factory.createOperatorQueryStat(op.getId(), qryId);
    execContext.getTransactionMgr().commit(txn);
    execContext.setTransaction(null);
    return opQryStats;
  }

  public void close()
  {
    itrOpt = null;
    itrQryIds = null;
    state = InternalState.INIT;
  }

}

