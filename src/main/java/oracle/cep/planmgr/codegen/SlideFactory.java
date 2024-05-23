/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/SlideFactory.java /main/1 2012/06/07 03:24:37 sbishnoi Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    05/29/12 - Creation
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
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
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.Slide;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.RelStore;
import oracle.cep.execution.synopses.RelationSynopsisImpl;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptSlide;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;
import oracle.cep.service.ExecContext;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/SlideFactory.java /main/1 2012/06/07 03:24:37 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class SlideFactory extends ExecOptFactory
{

  /* (non-Javadoc)
   * @see oracle.cep.planmgr.codegen.ExecOptFactory#createCodeGenContext
   * (oracle.cep.service.ExecContext, oracle.cep.metadata.Query, 
   * oracle.cep.phyplan.PhyOpt)
   */
  @Override
  protected CodeGenContext createCodeGenContext(ExecContext ec, Query query,
      PhyOpt phyopt)
  {
    CodeGenContext ctx = new SlideContext(ec, query, phyopt);
    PhyOptSlide phyOp = (PhyOptSlide)phyopt;
    long numSlideNanos = phyOp.getNumSlideNanos();
    ((SlideContext)ctx).setNumSlideNanos(numSlideNanos);
    return ctx;
  }

  @Override
  ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException
  {
    return new Slide(ctx.getExecContext());
  }

  @Override
  void setupExecOpt(CodeGenContext ctx) throws CEPException
  {
    assert ctx instanceof SlideContext;    
    SlideContext sctx = (SlideContext)ctx;
    
    // Initilize EvalContext
    ExecContext ec = sctx.getExecContext();
    IEvalContext evalContext = EvalContextFactory.create(ec);
    
    // Get the Slide Physical operator from context
    assert sctx.getPhyopt() instanceof PhyOptSlide;
    PhyOptSlide phyOpt = (PhyOptSlide) sctx.getPhyopt();
    
    // Get the Slide Execution Operator
    assert sctx.getExecOpt() instanceof Slide;
    Slide execOpt = (Slide) sctx.getExecOpt();
    
    // Get the slide value from context and set into execution operator
    long numSlideNanos = sctx.getNumSlideNanos();
    execOpt.setNumSlideNanos(numSlideNanos);
        
    ///////// Create and initialize synopsis/////////////////////
    // Get physical relation synopsis from physical slide operator
    PhySynopsis phySyn = phyOpt.getOutputSyn();    
    assert phySyn != null;
    assert phySyn.getStwstore() == phyOpt.getStore();
    assert phySyn.getKind()  == SynopsisKind.REL_SYN;
    
    // Initialize execution time synopsis implementation
    RelationSynopsisImpl relSyn   = null;
    ExecStore            relStore = null;

    // Construct Relation Synopsis & associate it with other data structures
    ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
    allCtx.setOpt(phyOpt);
    allCtx.setObjectType(RelationSynopsisImpl.class.getName());
    relSyn = (RelationSynopsisImpl) ObjectManager.allocate(allCtx);
    phySyn.setSyn(relSyn);
       
    // Initialize the relation synopsis scan
    TupleSpec tupSpec = CodeGenHelper.getTupleSpec(ec, phyOpt);
    HashIndex outIdx = 
        getIndex(ec, evalContext, tupSpec, IEvalContext.INPUT_ROLE,
        ctx.getTupleStorage().getFactory());
    int relScanId = relSyn.setIndexScan(null, outIdx);
    
    // Initialize the full scan for relation propagation
    int fullScanId = relSyn.setFullScan();
    
    // Construct Relation Store & associate it with relation synopsis
    relStore = ctx.getTupleStorage();
    assert relStore instanceof RelStore;
    relSyn.setStore((RelStore) relStore);
    relSyn.setStubId(relStore.addStub());
    
    // Initialize the Relation Synopsis
    relSyn.setEvalContext(evalContext);
    relSyn.initialize();
    
    // Set the Synopsis into Execution operator
    execOpt.setOutSyn(relSyn);    
    execOpt.setRelScanId(relScanId);
    execOpt.setPropScanId(fullScanId);
    execOpt.setEvalContext(evalContext);
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
  
  /**
   * Get the hash index on relation synopsis
   * @param ec
   * @param evalCtx
   * @param tupSpec
   * @param roleNum
   * @param factory
   * @return
   * @throws CEPException
   */
  private HashIndex getIndex(ExecContext ec, 
                             IEvalContext evalCtx, 
                             TupleSpec tupSpec, 
                             int roleNum, 
                             IAllocator<ITuplePtr> factory)
          throws CEPException
  {
    HashIndex outIndex = new HashIndex(ec); // hash index on synopsis
    HInstr hInstr;
    IHEval updHash, scanHash;
    IBEval keyEqual;
    BInstr bInstr;
    int numAttrs = tupSpec.getNumAttrs();

    updHash = HEvalFactory.create(ec, numAttrs);
    for (int attr = 0; attr < numAttrs; attr++)
    {
      hInstr = new HInstr(tupSpec.getAttrType(attr), IEvalContext.UPDATE_ROLE,
          new Column(attr));
      updHash.addInstr(hInstr);
    }
    updHash.compile();

    scanHash = HEvalFactory.create(ec, numAttrs);
    for (int attr = 0; attr < numAttrs; attr++)
    {
      hInstr = new HInstr(tupSpec.getAttrType(attr), roleNum,
          new Column(attr));

      scanHash.addInstr(hInstr);
    }
    scanHash.compile();

    keyEqual = BEvalFactory.create(ec);
    for (int attr = 0; attr < numAttrs; attr++)
    {
      bInstr = new BInstr();

      bInstr.op = ExprHelper.getEqOp(tupSpec.getAttrType(attr));
      bInstr.r1 = roleNum;
      bInstr.c1 = new Column(attr);

      bInstr.r2 = IEvalContext.SCAN_ROLE;
      bInstr.c2 = new Column(attr);

      keyEqual.addInstr(bInstr);
    }
    keyEqual.compile();

    outIndex.setUpdateHashEval(updHash);
    outIndex.setScanHashEval(scanHash);
    outIndex.setKeyEqual(keyEqual);
    outIndex.setEvalContext(evalCtx);
    outIndex.setFactory(factory);
    outIndex.initialize();

    return outIndex;
  }
  
}