/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/snapshot/OperatorTraversal.java hopark_cqlsnapshot/4 2016/02/26 11:55:08 hopark Exp $ */

/* Copyright (c) 2015, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/15/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/snapshot/OperatorTraversal.java hopark_cqlsnapshot/4 2016/02/26 11:55:08 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.snapshot;

import java.util.ArrayList;
import java.util.List;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.cache.NameSpace;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.service.ExecContext;
import oracle.cep.util.DAGHelper;
import oracle.cep.util.DAGNode;

public abstract class OperatorTraversal
{
  ExecContext ec;
  
  public OperatorTraversal(ExecContext ec) 
  {
    this.ec = ec;  
  }
  

  protected void traverse() throws CEPException
  {
    // Determine all the queries whose plan will be traversed.
    List<Integer> qryIds = ec.getSchemaMgr().getCacheObjectsNames(ec.getSchemaName(), NameSpace.QUERY);
    int currQryId = Integer.MIN_VALUE;
    if(qryIds != null)
    {
      ec.getPlanMgr().getLock().readLock().lock();
      try
      {
        for(int id: qryIds)
        {
          // Check if the query is a view query
          boolean isViewQry = ec.getExecMgr().getQuery(id).isViewQuery();
          
          // Don't instantiate the query plan for view query as they will
          // be instantiate as part of the query which will use view as 
          // source
          if(isViewQry)
            continue;
          
          currQryId = id;
          PhyOpt root =ec.getPlanMgr().getQueryRootOpt(id);   
          if (root == null) {
              // From some runtime exception, the quey has stopped.
              throw new RuntimeException("No root operator for query:"+id);
          }
          ArrayList<DAGNode> nodes = DAGHelper.getTopologicalSort(root);
          for (DAGNode op : nodes) 
          {
            PhyOpt opt = (PhyOpt) op;
            ExecOpt execOpt = opt.getInstOp();
            process(execOpt); 
          }
        }
      } 
      catch (CEPException e)
      {           
        LogUtil.fine(LoggerType.TRACE, e.getMessage());
        LogUtil.logStackTrace(e);
        String queryName = ec.getExecMgr().getQueryName(currQryId);
        throw new CEPException(ExecutionError.SNAPSHOT_PROCESSING_ERROR, e, queryName, e.getCauseMessage());
      }
      finally
      {
        ec.getPlanMgr().getLock().readLock().unlock();
      }
    }
    
    //TODO traverse the operator tree from the source and invoke process
    // 1. Determine root of given query
    // 2. Get topological sort for the query plan
    // 3. Traverse the query plan and determine corresponding execution operator
    // 4. For each execution operator, invoke process() method
  }

  protected abstract void process(ExecOpt operator) throws CEPException;
}
