/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/ProjectFactory.java /main/13 2011/07/09 08:53:45 udeshmuk Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
  Factory for the Project Execution operator

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
    udeshmuk  06/29/11 - support for archived relation
    anasrini  12/20/10 - remove eval.setEvalContext
    parujain  03/19/09 - stateless server
    hopark    10/10/08 - remove statics
    hopark    10/09/08 - remove statics
    hopark    12/07/07 - cleanup spill
    hopark    09/04/07 - eval optimize
    hopark    04/06/07 - mark static pin for const tupleptr
    hopark    04/05/07 - memmgr reorg
    najain    03/14/07 - cleanup
    hopark    03/06/07 - use ITuplePtr
    najain    12/04/06 - stores are not storage allocators
    hopark    11/07/06 - bug 5465978 : refactor newExecOpt
    najain    07/19/06 - ref-count tuples
    najain    07/05/06 - cleanup
    najain    06/29/06 - factory allocation cleanup
    najain    06/18/06 - cleanup
    najain    06/16/06 - cleanup
    anasrini  06/03/06 - do not get stubId from physical synopsis
    najain    05/23/06 - bug fix
    najain    04/24/06 - bug fix
    anasrini  03/24/06 - bug fix related to outStore
    anasrini  03/22/06 - setInStore
    anasrini  03/20/06 - fix up stores related
    najain    03/17/06 - fix problems
    anasrini  03/16/06 - process queues
    anasrini  03/14/06 - implementation
    skaluska  02/28/06 - Creation
    skaluska  02/28/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/ProjectFactory.java st_pcbpel_anasrini_eval_parallelism_2/1 2010/12/20 07:47:46 anasrini Exp $
 *  @author  skaluska
 *  @since   1.0
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.execution.operators.ConcurrentProject;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.Project;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.factory.AEvalFactory;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.execution.synopses.LineageSynopsisImpl;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.LineageStore;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptProject;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.service.ExecContext;
import oracle.cep.exceptions.CEPException;
import oracle.cep.common.OrderingKind;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.memmgr.IAllocator;

/**
 * ProjectFactory - factory for the PROJECT execution operator
 *
 * @author skaluska
 * @since 1.0
 */
public class ProjectFactory extends ExecOptFactory {

    @Override
    ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException
    {
      PhyOpt op = ctx.getPhyopt();
      
      if (op.getOrderingConstraint() == OrderingKind.UNORDERED)
        return new ConcurrentProject(ctx.getExecContext());
      else
        return new Project(ctx.getExecContext());
    }

    @Override
    void setupExecOpt(CodeGenContext ctx) throws CEPException
    {
        assert ctx.getExecOpt() instanceof Project;
        PhyOpt op = ctx.getPhyopt();
        assert op instanceof PhyOptProject;

        ExecContext ec = ctx.getExecContext();
        Project projExecOp = (Project) ctx.getExecOpt();
        PhyOptProject projPhyOp = (PhyOptProject) op;
        IAEval               outEval;
        IEvalContext         evalContext;
        EvalContextInfo     evalCtxInfo;
        TupleSpec           st;
        ConstTupleSpec      ct;
        IAllocator        stf;
        IAllocator        ctf;
        ITuplePtr  t;
        Expr[]              projs;
        PhySynopsis         syn;
        LineageSynopsisImpl linsyn = null;
        ExecStore           outStore = null;    

        // Create the evaluation context and instantiate the expressions
        outEval = AEvalFactory.create(ec);
        evalContext = EvalContextFactory.create(ec);
        FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
        evalCtxInfo = new EvalContextInfo(factoryMgr);
        projs = projPhyOp.getExprs();

        assert projs != null;
        assert projs.length > 0 : projs.length;

        for (int i=0, l=projs.length; i<l; i++) {
            int[] inpRoles = new int[1];
            inpRoles[0] = IEvalContext.INPUT_ROLE;
            ExprHelper.instExprDest(ec, projs[i], outEval, evalCtxInfo,
                                    IEvalContext.NEW_OUTPUT_ROLE, i, 
                                    inpRoles);
        }

        outEval.compile();
        
        // Scratch Tuple
        st = evalCtxInfo.st;
        if (st != null) {
            stf = factoryMgr.get(st);

            t = (ITuplePtr)stf.allocate(); //SCRATCH_TUPLE
            evalContext.bind(t, IEvalContext.SCRATCH_ROLE);
        }

        // Constant Tuple
        ct = evalCtxInfo.ct;
        if (ct != null) {
            ctf = factoryMgr.get(ct.getTupleSpec());

            t = (ITuplePtr)ctf.allocate(); //SCRATCH_TUPLE
            ct.populateTuple(ec, t);
            evalContext.bind(t, IEvalContext.CONST_ROLE);
        }

        // Instantiate the synopsis if necessary
        syn = projPhyOp.getOutSyn();
        if (syn != null) {
            ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
            allCtx.setOpt(op);
            allCtx.setObjectType(LineageSynopsisImpl.class.getName());
            linsyn = (LineageSynopsisImpl)ObjectManager.allocate(allCtx);
            outStore = ctx.getTupleStorage();
            assert (outStore != null);
            assert (outStore instanceof LineageStore);
            linsyn.setStore((LineageStore)outStore);
            linsyn.setStubId(outStore.addStub());
            syn.setSyn(linsyn);
            projExecOp.setOutSynopsis(linsyn);
        }

        // Set the evaluation context
        projExecOp.setEvalContext(evalContext);

        // Set the expression evaluator
        projExecOp.setProjEvaluator(outEval);
    }


}
