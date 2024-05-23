/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/RStreamFactory.java /main/7 2011/02/07 03:36:26 sborah Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
   Factory for creating the RSTREAM Execution operator

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
    anasrini  12/20/10 - remove eval.setEvalContext
    parujain  03/19/09 - stateless server
    hopark    10/10/08 - remove statics
    hopark    10/09/08 - remove statics
    najain    04/04/08 - silent reln support
    hopark    09/04/07 - eval optimize
    najain    12/04/06 - stores are not storage allocators
    hopark    11/07/06 - bug 5465978 : refactor newExecOpt
    najain    07/19/06 - ref-count tuples
    najain    07/05/06 - cleanup
    najain    06/29/06 - factory allocation cleanup
    najain    06/18/06 - cleanup
    anasrini  06/03/06 - do not get stubId from physical synopsis
    najain    04/20/06 - bug fixes
    anasrini  04/05/06 - implementation
    skaluska  02/28/06 - Creation
    skaluska  02/28/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/RStreamFactory.java st_pcbpel_anasrini_eval_parallelism_2/1 2010/12/20 07:47:46 anasrini Exp $
 *  @author  skaluska
 *  @since   1.0
 */


package oracle.cep.planmgr.codegen;

import java.util.Iterator;

import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.RStream;
import oracle.cep.execution.operators.RelSource;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.RelStore;
import oracle.cep.execution.synopses.RelationSynopsisImpl;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.AInstr;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.factory.AEvalFactory;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptRStrm;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.service.ExecContext;
import oracle.cep.exceptions.CEPException;

/**
 * RStreamFactory - Factory for creating the RSTREAM Execution operator
 *
 * @author skaluska
 * @since 1.0
 */
class RStreamFactory extends ExecOptFactory {


    /* (non-Javadoc)
     * @see oracle.cep.planmgr.codegen.ExecOptFactory#newExecOpt(oracle.cep.phyplan.PhyOpt)
     */
    @Override
    ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException {
        return new RStream(ctx.getExecContext());
    }

    @Override
    void setupExecOpt(CodeGenContext ctx) throws CEPException
    {
        assert ctx.getExecOpt() instanceof RStream;
        PhyOpt op = ctx.getPhyopt();
        assert op instanceof PhyOptRStrm;

        ExecContext ec = ctx.getExecContext();

        RStream rStream = (RStream) ctx.getExecOpt();
        PhyOptRStrm phyRStream = (PhyOptRStrm) op;
        ExecStore            outStore;
        RelStore             relStore;
        TupleSpec            ts;
        PhySynopsis          syn;
        RelationSynopsisImpl inSyn;
        IEvalContext          evalContext = EvalContextFactory.create(ec);
        IAEval                copyEval;

        // Set the out tuple spec
        ts = CodeGenHelper.getTupleSpec(ec, op);

        // Instantiate the relation synopsis
        syn = phyRStream.getSynopsis();
        ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
        allCtx.setOpt(op);
        allCtx.setObjectType(RelationSynopsisImpl.class.getName());
        inSyn =
            (RelationSynopsisImpl)ObjectManager.allocate(allCtx);

        relStore = (RelStore)syn.getStwstore().getInstStore();
        inSyn.setStore(relStore);
        inSyn.setStubId(((ExecStore)relStore).addStub());
        inSyn.setEvalContext(evalContext);
        rStream.setScan(inSyn.setFullScan());
        inSyn.initialize();
        syn.setSyn(inSyn);
        rStream.setSynopsis(inSyn);

        // copyEval
        copyEval = setupCopyEval(ec, ts);

        // Set the evaluation context
        rStream.setEvalContext(evalContext);

        // Set the copy evaluator
        rStream.setCopyEval(copyEval);
        
        boolean silentRelns = op.getInputs()[0].isSilentRelnDep();

        rStream.setSilentRelns(silentRelns);

        if (silentRelns)
        {
          Iterator<PhyOpt> iter = op.getInputs()[0].getSilentRelnDep().iterator();

          while (iter.hasNext())
          {
            PhyOpt opDep = iter.next();
            rStream.addInputRelns((RelSource) opDep.getInstOp());
          }
        }
    }

    private IAEval setupCopyEval(ExecContext ec, TupleSpec ts) 
      throws CEPException {

        IAEval copyEval = AEvalFactory.create(ec);
        AInstr instr;
        int numAttrs = ts.getNumAttrs();

        for (int i=0; i<numAttrs; i++) {
            instr = new AInstr();

            instr.op = ExprHelper.getCopyOp(ts.getAttrType(i));
            instr.r1 = IEvalContext.INPUT_ROLE;
            instr.c1 = i;
            instr.r2 = 0;
            instr.c2 = 0;
            instr.dr = IEvalContext.NEW_OUTPUT_ROLE;
            instr.dc = i;

            copyEval.addInstr(instr);
        }
        copyEval.compile();

        return copyEval;
    }

}
