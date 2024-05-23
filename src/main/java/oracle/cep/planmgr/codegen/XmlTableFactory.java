/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/XmlTableFactory.java /main/6 2011/02/07 03:36:26 sborah Exp $ */
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
    anasrini    12/20/10 - remove eval.setEvalContext
    parujain    03/19/09 - stateless server
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    hopark      03/07/08 - addManagedObj
    hopark      12/28/07 - spill cleanup
    mthatte     12/26/07 - 
    najain      12/13/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/XmlTableFactory.java st_pcbpel_anasrini_eval_parallelism_2/1 2010/12/20 07:47:46 anasrini Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.factory.AEvalFactory;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptXmlTable;
import oracle.cep.phyplan.expr.ExprXQryFunc;
import oracle.cep.service.ExecContext;
import oracle.cep.execution.operators.XmlTable;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.common.Datatype;

/**
 * XmlTableFactory
 *
 * @author najain
 * @since 1.0
 */
class XmlTableFactory extends ExecOptFactory
{
  @Override
  public CodeGenContext createCodeGenContext(ExecContext ec, Query query, 
                                             PhyOpt phyopt)
  {
    return new XmlTableContext(ec, query, phyopt); 
  }

  ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException
  {
    PhyOpt op = ctx.getPhyopt();
    assert op instanceof PhyOptXmlTable;

    int numAttrs = op.getNumAttrs();
    return new XmlTable(ctx.getExecContext());
  }

  void setupExecOpt(CodeGenContext ctx) throws CEPException
  {
    assert ctx instanceof XmlTableContext;
    XmlTableContext xctx = (XmlTableContext)ctx;
    
    assert ctx.getExecOpt() instanceof XmlTable;
    PhyOpt op = ctx.getPhyopt();
    assert op instanceof PhyOptXmlTable;

    ExecContext ec = ctx.getExecContext();
    XmlTable xmlTblExecOp = (XmlTable) ctx.getExecOpt();
    PhyOptXmlTable xmlTblPhyOp = (PhyOptXmlTable) op;

    // Create the evaluation context and instantiate the expressions
    IAEval outEval = AEvalFactory.create(ec);
    IEvalContext evalContext = EvalContextFactory.create(ec);
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    EvalContextInfo evalCtxInfo = new EvalContextInfo(factoryMgr);
    ExprXQryFunc expr = xmlTblPhyOp.getExprXQryFunc();

    assert expr != null;

    int[] inpRoles = new int[1];
    inpRoles[0] = IEvalContext.INPUT_ROLE;
    ExprHelper.instExprDest(ec, expr, outEval, evalCtxInfo,
                            IEvalContext.NEW_OUTPUT_ROLE, xctx.getResSetCol(), 
                            inpRoles);

    outEval.compile();
    // Scratch Tuple
    TupleSpec st = evalCtxInfo.st;
    if (st != null) 
    {
      IAllocator stf = factoryMgr.get(st);

      ITuplePtr t = (ITuplePtr)stf.allocate(); //SCRATCH_TUPLE
      evalContext.bind(t, IEvalContext.SCRATCH_ROLE);
    }

    // Constant Tuple
    ConstTupleSpec ct = evalCtxInfo.ct;
    if (ct != null) 
    {
      IAllocator ctf = factoryMgr.get(ct.getTupleSpec());

      ITuplePtr t = (ITuplePtr)ctf.allocate(); //SCRATCH_TUPLE
      ct.populateTuple(ec, t);
      evalContext.bind(t, IEvalContext.CONST_ROLE);
    } 

    // Set the evaluation context
    xmlTblExecOp.setEvalContext(evalContext);

    // Set the expression evaluator
    xmlTblExecOp.setXmlTableEval(outEval);

    xmlTblExecOp.setResultSetCol(xctx.getResSetCol());
  }  

  @Override
  protected ExecStore instStore(CodeGenContext ctx) throws CEPException
  {
    assert ctx != null;
    assert ctx instanceof XmlTableContext;
    XmlTableContext xctx = (XmlTableContext)ctx;
    PhyOpt op = ctx.getPhyopt();
    assert op != null;

    ExecContext ec = ctx.getExecContext();
    TupleSpec tupSpec = CodeGenHelper.getTupleSpec(ec, op);
    int resSetCol = tupSpec.addManagedObj(Datatype.OBJECT);
    assert resSetCol == 1;
    xctx.setResSetCol(resSetCol);
    
    assert op.getStore() != null;
    ExecStore outStore = StoreInst.instStore(new StoreGenContext(ec, op.getStore(), tupSpec));
    ctx.setTupleStorage(outStore);
    return outStore;    
  }
}
