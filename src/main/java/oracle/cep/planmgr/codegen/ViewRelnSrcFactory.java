/* $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/ViewRelnSrcFactory.java /main/5 2009/03/30 14:46:02 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 parujain    03/19/09 - stateless server
 hopark      10/10/08 - remove statics
 hopark      10/09/08 - remove statics
 hopark      10/07/08 - use execContext to remove statics
 hopark      02/25/08 - support paged queue
 najain      12/04/06 - stores are not storage allocators
 hopark      11/07/06 - bug 5465978 : refactor newExecOpt
 anasrini    09/22/06 - set name for execution operator
 najain      08/03/06 - view reln does not have its own store
 najain      07/19/06 - ref-count tuples
 najain      07/05/06 - cleanup
 najain      06/29/06 - factory allocation cleanup
 najain      06/18/06 - cleanup
 najain      06/16/06 - bug fix
 najain      06/13/06 - bug fix
 najain      06/09/06 - query addition support
 najain      06/07/06 - add processExecOpt
 najain      06/05/06 - add full scan
 najain      05/22/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/ViewRelnSrcFactory.java /main/5 2009/03/30 14:46:02 parujain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.planmgr.codegen;

import java.util.BitSet;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.ViewRelnSrc;
import oracle.cep.execution.queues.ISharedQueueWriter;
import oracle.cep.execution.queues.Queue;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.RelStore;
import oracle.cep.execution.synopses.RelationSynopsisImpl;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptViewRelnSrc;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;
import oracle.cep.service.ExecContext;

/**
 * ViewRelnSrcFactory
 *
 * @author najain
 * @since 1.0
 */
class ViewRelnSrcFactory extends ExecOptFactory
{

    /*
   * (non-Javadoc)
   *
   * @see oracle.cep.planmgr.codegen.ExecOptFactory#newExecOpt(oracle.cep.planmgr.codegen.CodeGenContext)
   */
    @Override
    ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException
    {
        return new ViewRelnSrc(ctx.getExecContext());
    }

    @Override
    void setupExecOpt(CodeGenContext ctx) throws CEPException
    {
        assert ctx.getExecOpt() instanceof ViewRelnSrc;
        PhyOpt op = ctx.getPhyopt();
        assert op instanceof PhyOptViewRelnSrc;

        ViewRelnSrc sourceOp = (ViewRelnSrc) ctx.getExecOpt();
        ExecContext ec = ctx.getExecContext();
        
        PhyOptViewRelnSrc vsop = (PhyOptViewRelnSrc) op;

        // Create the output synopsis
        PhySynopsis p_outSyn = vsop.getOutSyn();

        assert p_outSyn != null;
        assert p_outSyn.getKind() == SynopsisKind.REL_SYN : p_outSyn.getKind();
        ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
        allCtx.setOpt(op);
        allCtx.setObjectType(RelationSynopsisImpl.class.getName());
        RelationSynopsisImpl e_outSyn =
            (RelationSynopsisImpl)ObjectManager.allocate(allCtx);
        p_outSyn.setSyn(e_outSyn);

        // Equality scan
        int fullScanId = e_outSyn.setFullScan();
        e_outSyn.initialize();

        sourceOp.setSynopsis(e_outSyn);
        sourceOp.setFullScanId(fullScanId);
        sourceOp.setRelnId(vsop.getRelId());

        ExecStore outStore = ctx.getTupleStorage();
        assert (outStore != null);
        assert (outStore instanceof RelStore);

        e_outSyn.setStore((RelStore) outStore);
        int id = outStore.addStub();
        e_outSyn.setStubId(id);

        int relId = vsop.getRelId();
        String optName = ctx.getExecContext().getViewMgr().getView(relId).getName();
        sourceOp.setOptName(optName + "#" + vsop.getId());
    }

    @Override
    protected ExecStore instStore(CodeGenContext ctx) throws CEPException
    {
        assert ctx != null;
        PhyOpt op = ctx.getPhyopt();
        assert op != null;

        ExecContext ec = ctx.getExecContext();
        ExecStore outStore = getInputStore(op, ec, 0);
        assert outStore instanceof RelStore;

        ctx.setTupleStorage(outStore);
        return outStore;
    }

    public void processExecOpt(CodeGenContext ctx)
    {
        Query query = ctx.getQuery();
        PhyOpt op = ctx.getPhyopt();

        assert op instanceof PhyOptViewRelnSrc;
        int relnId = ((PhyOptViewRelnSrc) op).getRelId();

        // Add the relation to the query, if not already done so
        if (!query.isRefRelnPresent(relnId))
        {
            ExecOpt execOp = op.getInstOp();
            assert execOp != null;
            assert (execOp instanceof ViewRelnSrc);

            Queue q = ((ViewRelnSrc) execOp).getOutputQueue();
            assert q != null;
            assert q instanceof ISharedQueueWriter;
            BitSet readers = ((ISharedQueueWriter) q).getReaders();


            query.addRefReln(relnId, readers);
        }
    }
}
