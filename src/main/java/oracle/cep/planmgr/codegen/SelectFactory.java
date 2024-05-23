/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/SelectFactory.java /main/19 2012/10/22 14:42:18 vikshukl Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Factory for the Select Execution operator
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 vikshukl  10/10/12 - archived dimension stuff
 anasrini  12/20/10 - remove eval.setEvalContext
 sborah    07/16/09 - support for bigdecimal
 parujain  03/19/09 - stateless server
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 hopark    12/07/07 - cleanup spill
 hopark    09/04/07 - eval optimize
 najain    04/11/07 - bug fix
 hopark    04/06/07 - mark static pin for const tupleptr
 hopark    04/05/07 - memmgr reorg
 najain    03/14/07 - cleanup
 hopark    03/06/07 - use ITuplePtr
 parujain  01/19/07 - null bug
 parujain  12/19/06 - fullScanId for RelationSynopsis
 najain    12/04/06 - stores are not storage allocators
 hopark    11/14/06 - bug 5505056, turn off null = null
 hopark    11/07/06 - bug 5465978 : refactor newExecOpt
 parujain  11/09/06 - Logical Operator execution
 parujain  08/11/06 - cleanup planmgr
 parujain  08/07/06 - timestamp datatype
 najain    08/03/06 - select can be shared
 anasrini  08/03/06 - support for outsyn and outStore
 najain    07/19/06 - ref-count tuples 
 najain    07/05/06 - cleanup
 najain    06/29/06 - factory allocation cleanup 
 najain    06/18/06 - cleanup
 najain    05/23/06 - bug fix 
 najain    04/24/06 - bug fix 
 anasrini  03/30/06 - support for select operator 
 skaluska  02/28/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/SelectFactory.java /main/19 2012/10/22 14:42:18 vikshukl Exp $
 *  @author  najain
 *  @since   1.0
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.common.Constants;
import oracle.cep.common.OrderingKind;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.indexes.HashIndex;
import oracle.cep.execution.internals.BInstr;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.HInstr;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.IHEval;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.factory.BEvalFactory;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.execution.internals.factory.HEvalFactory;
import oracle.cep.execution.operators.ConcurrentSelect;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.Select;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.RelStore;
import oracle.cep.execution.synopses.RelationSynopsisImpl;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptSelect;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.expr.BoolExpr;
import oracle.cep.service.ExecContext;

/**
 * Factory for the Select Execution operator
 *
 * @author skaluska
 * @since 1.0
 */
class SelectFactory extends ExecOptFactory
{
  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.planmgr.codegen.ExecOptFactory#newExecOpt(oracle.cep.phyplan.PhyOpt)
   */
  @Override
  ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException
  {
    PhyOpt op = ctx.getPhyopt();
    // Create the execution operator
    int numAttrs = op.getNumAttrs();

    // Create the execution operator
    if (op.getOrderingConstraint() == OrderingKind.UNORDERED)
      return new ConcurrentSelect(ctx.getExecContext(), numAttrs);
    else
      return new Select(ctx.getExecContext(), numAttrs);
  }

  @Override
  void setupExecOpt(CodeGenContext ctx) throws CEPException
  {
    assert ctx.getExecOpt() instanceof Select;
    PhyOpt op = ctx.getPhyopt();
    assert op instanceof PhyOptSelect;
    
    ExecContext ec = ctx.getExecContext();
    Select execSel = (Select) ctx.getExecOpt();
    PhyOptSelect phySel = (PhyOptSelect) op;
    IBEval outEval;
    IEvalContext evalContext;
    EvalContextInfo evalCtxInfo;
    TupleSpec st;
    ConstTupleSpec ct;
    IAllocator<Object> stf;
    IAllocator<Object> ctf;
    ITuplePtr  t;
    BoolExpr[] preds;
    PhySynopsis syn;
    RelationSynopsisImpl relsyn = null;
    ExecStore outStore = null;

    // Create the evaluation context and instantiate the expressions
    outEval = BEvalFactory.create(ec);
    outEval.setNullEqNull(false);
    evalContext = EvalContextFactory.create(ec);
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    evalCtxInfo = new EvalContextInfo(factoryMgr);
    preds = phySel.getPredicate();

    assert preds != null;
    assert preds.length > 0 : preds.length;

    for (int i = 0, l = preds.length; i < l; i++)
    {
      int[] inpRoles = new int[1];
      inpRoles[0] = IEvalContext.INPUT_ROLE;

      ExprHelper.instBoolExpr(ec, preds[i], outEval, evalCtxInfo, false,
                              inpRoles);
    }
    outEval.compile();

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

      t = (ITuplePtr)ctf.allocate(); //SCRATCH_TUPLE
      ct.populateTuple(ec, t);
      evalContext.bind(t, IEvalContext.CONST_ROLE);
    }

    // Create the execution operator
    int numAttrs = op.getNumAttrs();

    // Instantiate the synopsis if necessary
    syn = phySel.getOutSyn();
    if (syn != null)
    {
      ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
      allCtx.setOpt(op);
      allCtx.setObjectType(RelationSynopsisImpl.class.getName());
      relsyn = (RelationSynopsisImpl) ObjectManager.allocate(allCtx);
            outStore = ctx.getTupleStorage();
            assert (outStore != null);
            assert (outStore instanceof RelStore);

      relsyn.setStore((RelStore) outStore);
      relsyn.setStubId(outStore.addStub());
      syn.setSyn(relsyn);

      // Create an index over this synopsis
      HashIndex idx = new HashIndex(ec);

      // Initialize the index
      initIndex(ec, phySel, evalContext, idx, outStore.getFactory());

      // Equality scan
      int scanId = relsyn.setIndexScan(null, idx);
      int fullScanId = relsyn.setFullScan();
      relsyn.setEvalContext(evalContext);
      relsyn.initialize();

      execSel.setOutSynopsis(relsyn);
      execSel.setScanId(scanId);
      execSel.setFullScanId(fullScanId);

      for (int i = 0; i < numAttrs; i++)
        execSel.addAttr(op.getAttrMetadata(i));
    }
    else
      execSel.setNumAttrs(numAttrs);

    // Set the evaluation context
    execSel.setEvalContext(evalContext);

    // Set the predicate
    execSel.setPredicate(outEval);
    
    // Set if archived dimension
    execSel.setArchivedDim(phySel.isArchivedDim());
  }

  private void initIndex(ExecContext ec, PhyOptSelect op, IEvalContext evalCtx,
			 HashIndex idx, IAllocator<ITuplePtr> factory)
      throws ExecException
  {
    // Create the execution operator
    int numAttrs = op.getNumAttrs();

    IHEval updateHash = HEvalFactory.create(ec, Constants.MAX_INSTRS);
    for (int a = 0; a < numAttrs; a++)
    {
      HInstr hinstr = new HInstr(op.getAttrTypes(a), IEvalContext.UPDATE_ROLE,
          new Column(a));

      updateHash.addInstr(hinstr);
    }
    updateHash.compile();

    IHEval scanHash = HEvalFactory.create(ec, Constants.MAX_INSTRS);
    for (int a = 0; a < numAttrs; a++)
    {
      HInstr hinstr = new HInstr(op.getAttrTypes(a), IEvalContext.UPDATE_ROLE,
          new Column(a));

      scanHash.addInstr(hinstr);
    }
    scanHash.compile();

    IBEval keyEqual = BEvalFactory.create(ec);
    for (int a = 0; a < numAttrs; a++)
    {
      BInstr binstr = new BInstr();

      binstr.op = ExprHelper.getEqOp(op.getAttrTypes(a));
     
      // lhs
      binstr.r1 = IEvalContext.UPDATE_ROLE;
      binstr.c1 = new Column(a);
      binstr.e1 = null;

      // rhs
      binstr.r2 = IEvalContext.SCAN_ROLE;
      binstr.c2 = new Column(a);
      binstr.e2 = null;

      keyEqual.addInstr(binstr);
    }
    keyEqual.compile();

    idx.setUpdateHashEval(updateHash);
    idx.setScanHashEval(scanHash);
    idx.setKeyEqual(keyEqual);
    idx.setEvalContext(evalCtx);
    idx.setFactory(factory);
    idx.initialize();
  }
}
