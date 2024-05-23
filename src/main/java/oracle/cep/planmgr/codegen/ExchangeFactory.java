/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/ExchangeFactory.java /main/6 2014/10/14 06:35:34 udeshmuk Exp $ */

/* Copyright (c) 2011, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    09/24/14 - set isInternalDDL in execContext before executing
                           DDLs
    anasrini    08/09/11 - XbranchMerge anasrini_bug-12845846_ps5 from
                           st_pcbpel_11.1.1.4.0
    anasrini    08/08/11 - setup for insertFast
    anasrini    06/30/11 - XbranchMerge anasrini_bug-12675151_ps5 from
                           st_pcbpel_11.1.1.4.0
    anasrini    06/19/11 - support for partition parallel regression tests
    anasrini    04/05/11 - handle multi input scenario
    anasrini    03/28/11 - support for operator sharing / reinstantiation
    anasrini    03/20/11 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/ExchangeFactory.java /main/6 2014/10/14 06:35:34 udeshmuk Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.codegen;

import java.util.Collection;
import java.util.List;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.operators.Exchange;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.HInstr;
import oracle.cep.execution.internals.IHEval;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.factory.AEvalFactory;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.execution.internals.factory.HEvalFactory;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptExchange;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.service.ExecContext;
import oracle.cep.service.IServerContext;


/**
 * ExchangeFactory - factory for the EXCHANGE execution operator
 */
public class ExchangeFactory extends ExecOptFactory {

    @Override
    ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException
    {
      PhyOpt op    = ctx.getPhyopt();
      return new Exchange(ctx.getExecContext());
    }

    @Override
    void setupExecOpt(CodeGenContext ctx) throws CEPException
    {
      PhyOpt op = ctx.getPhyopt();
      assert op instanceof PhyOptExchange;

      ExecContext    ec         = ctx.getExecContext();
      Exchange       exchExecOp = (Exchange) ctx.getExecOpt();
      PhyOptExchange exchPhyOp  = (PhyOptExchange) op;
      int            numInputs  = op.getNumInputs();
      boolean        isDependentOnPartnStream = exchPhyOp.isDependentOnPartnStream();
      // Instantiate all the input queues if numInputs > 1
      // For numInputs = 1, this is handled by the base class
      if (numInputs > 1)
        instInQueues(op, ec);

      // Handle the partitioning Expressions
      IEvalContext    evalContext;
      IAEval[]        partnExprsEval = null;
      IHEval[]        hashEvals = null;
      EvalContextInfo evalCtxInfo;
      FactoryManager  factoryMgr;
      int             dop;

      dop = exchPhyOp.getDOP();
      evalContext   = EvalContextFactory.create(ec);
      factoryMgr    = ec.getServiceManager().getFactoryManager();
      evalCtxInfo   = new EvalContextInfo(factoryMgr);

      List<Expr> partnExprs = exchPhyOp.getPartitioningExprList();
      if(!isDependentOnPartnStream)
      {
        partnExprsEval = new IAEval[partnExprs.size()];
        hashEvals      = new IHEval[partnExprs.size()];
        
        // Setup the input roles. Whichever be the input number from where
        // the input is received, it will be bound to INPUT_ROLE
        int[] inpRoles = new int[numInputs];
        for(int j=0; j<numInputs; j++)
        {
          inpRoles[j] = IEvalContext.INPUT_ROLE;
        }

        int i=0;
        for (Expr expr : partnExprs)
        {
          partnExprsEval[i] = AEvalFactory.create(ec);
          hashEvals[i]      = HEvalFactory.create(ec, Constants.MAX_INSTRS);
  
          // Setup the AEval
  
          ExprHelper.instExprDest(ec, expr, partnExprsEval[i], evalCtxInfo,
                                  IEvalContext.NEW_OUTPUT_ROLE, i, 
                                  inpRoles);
          partnExprsEval[i].compile();
  
          // Setup the HEval
          HInstr hinstr = new HInstr(op.getAttrTypes(i), 
                                     IEvalContext.NEW_OUTPUT_ROLE,
                                     new Column(i));
          hashEvals[i].addInstr(hinstr);
          hashEvals[i].compile();
          
          i++;
        }
      }
      
      TupleSpec      st;
      ConstTupleSpec ct;
      IAllocator     stf;
      IAllocator     ctf;
      ITuplePtr      t;

      // Scratch Tuple
      st = evalCtxInfo.st;
      if (st != null) 
      {
        stf = factoryMgr.get(st);
        t = (ITuplePtr)stf.allocate(); //SCRATCH_TUPLE
        evalContext.bind(t, IEvalContext.SCRATCH_ROLE);
      }

      // Constant Tuple
      ct = evalCtxInfo.ct;
      if (ct != null) 
      {
        ctf = factoryMgr.get(ct.getTupleSpec());
        t = (ITuplePtr)ctf.allocate(); //CONSTANT_TUPLE
        ct.populateTuple(ec, t);
        evalContext.bind(t, IEvalContext.CONST_ROLE);
      }

      // Set the partition expression and hash evaluators in the execOp
      exchExecOp.setEvalContext(evalContext);
      exchExecOp.setPartitionExprEvals(partnExprsEval);
      exchExecOp.setHashEvals(hashEvals);
      exchExecOp.setDOP(dop);
      exchExecOp.setDependentOnPartnStream(isDependentOnPartnStream);

      // Now, handle the setting up of DOP schemas and the execution
      // of the DDLs in each of these schemas
      // This is the the setup for parallelism
      Collection<String> ddls  = exchPhyOp.getDDLs();
      List<String> entityNames = exchPhyOp.getEntityNames();
      String       schemaName  = ec.getSchemaName();
      String       partitionSchemaPrefix =
        exchPhyOp.getPartitionSchemaPrefix();

      executeDDLs(ec, exchPhyOp, ddls);

      // Set the partitionSchemaPrefix in the Exchange execOp
      exchExecOp.setPartitionSchemaPrefix(partitionSchemaPrefix);

      // Set the entiyNames in the Exchange execOp
      exchExecOp.setEntityNames(entityNames.toArray(new String[0]));

      // Setup for invoking ExecManager.insertFast at runtime
      setupForInsertFast(ec, exchPhyOp);
    }

    @Override
    public void reInstantiate(CodeGenContext ctx) throws CEPException
    {

      PhyOpt op = ctx.getPhyopt();
      assert op instanceof PhyOptExchange;

      LogUtil.fine(LoggerType.TRACE, "ReInstantiating " + op.getOptName());

      ExecContext    ec         = ctx.getExecContext();
      PhyOptExchange exchPhyOp  = (PhyOptExchange) op;

      Collection<String> newDDLs = exchPhyOp.getNewDDLs();
      executeDDLs(ec, exchPhyOp, newDDLs);
      newDDLs.clear();

      // Setup for invoking ExecManager.insertFast at runtime
      //
      // This has to be invoked here again because in the case of views and
      // queries, only when the query is started is the TableSource 
      // instantiated
      setupForInsertFast(ec, exchPhyOp);
    }

    private void executeDDLs(ExecContext ec, PhyOptExchange exchPhyOp,
                             Collection<String> ddls)
      throws CEPException
    {
      String  partitionSchemaName;
      int     dop                   = exchPhyOp.getDOP();
      String  partitionSchemaPrefix = exchPhyOp.getPartitionSchemaPrefix();
      String  schemaName            = ec.getSchemaName();
      boolean isRegressPushMode 
        = ec.getServiceManager().getConfigMgr().isRegressPushMode();

      //set the isInternalDDL flag to true.
      ec.setInternalDDL(true);
      
      for(int j=0; j<dop; j++)
      {
        StringBuilder sb = new StringBuilder();
        sb.append(partitionSchemaPrefix + j);
        partitionSchemaName = sb.toString();

        // Now create all these objects in a schema just for this "bucket"
        ec.setSchema(partitionSchemaName);
        if (isRegressPushMode)
          ec.setPartitionParallelContext(j + "");

        for(String ddl : ddls)
        {
          LogUtil.fine(LoggerType.TRACE,
                       "Running " + ddl + " in " + partitionSchemaName);
          ec.executeDDL(ddl, true);
        }

        ec.setPartitionParallelContext(null);
        ec.setSchema(schemaName);
      }
      
      //reset now that we have executed DDLs
      ec.setInternalDDL(false);
    }

    private void setupForInsertFast(ExecContext ec, PhyOptExchange op)
    {
      int    numInputs             = op.getNumInputs();
      int    dop                   = op.getDOP();
      String partitionSchemaPrefix = op.getPartitionSchemaPrefix();
      List<String> entityNames     = op.getEntityNames();
      Exchange exchExecOp          = (Exchange)op.getInstOp();

      IServerContext[][] entityContext = new IServerContext[numInputs][dop];
      for(int inp=0; inp<numInputs; inp++) 
      {
        String entityName = entityNames.get(inp);

        for (int j=0; j<dop; j++)
        {
          String schemaFQN  = 
            ec.getServiceSchema(partitionSchemaPrefix + j);
        
          entityContext[inp][j] =
            ec.getExecMgr().getLookUpId(entityName, schemaFQN);
        }
      }
      exchExecOp.setEntityContext(entityContext);
    }

}
