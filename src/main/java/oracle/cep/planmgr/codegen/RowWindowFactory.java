/* $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/RowWindowFactory.java /main/6 2009/03/30 14:46:02 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares RowWindowFactory in package oracle.cep.planmgr.codegen.

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
    parujain  03/19/09 - stateless server
    hopark    10/10/08 - remove statics
    hopark    10/09/08 - remove statics
    parujain  06/17/08 - slide support
    hopark    02/21/08 - stored WinStore
    najain    12/04/06 - stores are not storage allocators
    hopark    11/07/06 - bug 5465978 : refactor newExecOpt
    najain    07/19/06 - ref-count tuples
    najain    07/05/06 - cleanup
    najain    06/29/06 - factory allocation cleanup
    najain    06/18/06 - cleanup
    najain    06/16/06 -
    dlenkov   05/17/06 - support for row windows
    skaluska  02/28/06 - Creation
    skaluska  02/28/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/RowWindowFactory.java /main/6 2009/03/30 14:46:02 parujain Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */


package oracle.cep.planmgr.codegen;

import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.RowWindow;
import oracle.cep.execution.synopses.WindowSynopsisImpl;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.WinStore;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptRowWin;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.service.ExecContext;
import oracle.cep.exceptions.CEPException;

/**
 * RowWindowFactory
 *
 * @author skaluska
 */
public class RowWindowFactory extends ExecOptFactory
{

    /**
     * Constructor for RowWindowFactory
     */
    public RowWindowFactory()
    {
        // TODO Auto-generated constructor stub
        super();
        
    }

    /*
    */

    @Override
    ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException
    {
        return new RowWindow(ctx.getExecContext());
    }

    @Override
    void setupExecOpt(CodeGenContext ctx) throws CEPException
    {
        assert ctx.getExecOpt() instanceof RowWindow;
        PhyOpt op = ctx.getPhyopt();
        assert op instanceof PhyOptRowWin;

        RowWindow rwExecOp = (RowWindow) ctx.getExecOpt();
        ExecContext ec = ctx.getExecContext();
        
        PhyOptRowWin rwop = (PhyOptRowWin) op;
        PhySynopsis        syn;
        WindowSynopsisImpl winsyn;
        ExecStore          inStore;
        WinStore           wStore;

        // Set the window size
        rwExecOp.setWindowSize( (long)rwop.getNumRows());
        // Set the slide size
        rwExecOp.setSlideSize(rwop.getSlide());


        // Instantiate the window synopsis
        syn = rwop.getWinSyn();

        ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
        allCtx.setOpt(op);
        allCtx.setObjectType(WindowSynopsisImpl.class.getName());
        winsyn = (WindowSynopsisImpl)ObjectManager.allocate(allCtx);

        inStore = ctx.getTupleStorage();
        assert (inStore != null);
        assert (inStore instanceof WinStore);
        wStore = (WinStore) inStore;

        winsyn.setStore(wStore);
        int id = inStore.addStub();
        winsyn.setStubId(id);

        syn.setSyn(winsyn);
        rwExecOp.setWinSynopsis(winsyn);
    }

    @Override
    protected ExecStore instStore(CodeGenContext ctx) throws CEPException
    {
        assert ctx != null;
        PhyOpt op = ctx.getPhyopt();
        assert op != null;

        ExecContext ec = ctx.getExecContext();
        ExecStore inStore = getInputStore(op, ec, 0);
        assert inStore instanceof WinStore : inStore.getClass().getName();

        ctx.setTupleStorage(inStore);
        return inStore;
    }
}
