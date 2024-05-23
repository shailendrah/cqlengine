/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/BufferFactory.java /main/2 2012/10/22 14:42:18 vikshukl Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    10/01/12 - propagate archived dimension stuff
    udeshmuk    07/07/12 - factory for buffer operator
    udeshmuk    07/07/12 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/BufferFactory.java /main/2 2012/10/22 14:42:18 vikshukl Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.common.Constants;
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
import oracle.cep.execution.internals.factory.BEvalFactory;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.execution.internals.factory.HEvalFactory;
import oracle.cep.execution.operators.BufferOp;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.LineageStore;
import oracle.cep.execution.stores.RelStore;
import oracle.cep.execution.stores.RelStoreImpl;
import oracle.cep.execution.synopses.LineageSynopsisImpl;
import oracle.cep.execution.synopses.RelationSynopsisImpl;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptBuffer;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;
import oracle.cep.service.ExecContext;

public class BufferFactory extends ExecOptFactory
{

  @Override
  ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException
  {
    PhyOpt op = ctx.getPhyopt();
    
    return new BufferOp(ctx.getExecContext(), op.getNumAttrs());
  }

  @Override
  void setupExecOpt(CodeGenContext ctx) throws CEPException
  {
    assert ctx.getExecOpt() instanceof BufferOp;
    PhyOpt op = ctx.getPhyopt();
    assert op instanceof PhyOptBuffer;

    ExecContext ec = ctx.getExecContext();
    BufferOp bufferExecOp = (BufferOp) ctx.getExecOpt();
    PhyOptBuffer bufferPhyOp = (PhyOptBuffer) op;
    
    bufferExecOp.setIsLineageSynStore(bufferPhyOp.getIsProjectInput());
    //add attrs to bufferOp - tuplespec will be formed.
    for(int i=0; i < bufferPhyOp.getNumAttrs(); i++)
    {
      bufferExecOp.addAttr(op.getAttrMetadata(i));
    }
    
    // if this operator represents output coming from a dimension
    if (bufferPhyOp.isArchivedDim())
      bufferExecOp.setArchivedDim(true);
    
    PhySynopsis syn = null;
    ExecStore outStore = null;
    if(bufferPhyOp.getIsProjectInput())
    {
      //instantiate a lineage store/syn
      syn = bufferPhyOp.getOutSyn();
      assert syn !=  null : "Synopsis of buffer operator cannot be null!";
      
      ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
      allCtx.setOpt(op);
      allCtx.setObjectType(LineageSynopsisImpl.class.getName());
      LineageSynopsisImpl linsyn = (LineageSynopsisImpl)ObjectManager.allocate(allCtx);
      outStore = ctx.getTupleStorage();
      assert (outStore != null);
      assert (outStore instanceof LineageStore);
      linsyn.setStore((LineageStore)outStore);
      linsyn.setStubId(outStore.addStub());
      syn.setSyn(linsyn);
      bufferExecOp.setLineageOutSynopsis(linsyn);
    }
    else
    {
      //instantiate a relational store/syn
      //Shared Evaluation context
      IEvalContext evalContext = EvalContextFactory.create(ec);

      syn = bufferPhyOp.getOutSyn();
      assert syn != null : "Synospsis of buffer operator cannot be null";
      RelationSynopsisImpl e_outSyn = null;
      
      assert syn.getKind() == SynopsisKind.REL_SYN : syn.getKind();
      ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
      allCtx.setOpt(op);
      allCtx.setObjectType(RelationSynopsisImpl.class.getName());
      e_outSyn = (RelationSynopsisImpl)ObjectManager.allocate(allCtx);
      syn.setSyn(e_outSyn);
      
      HashIndex idx = new HashIndex(ec);
        
      // Initialize the index
      initIndex(ec, bufferPhyOp, evalContext, idx, 
                ctx.getTupleStorage().getFactory());
            
      // Equality scan
      int scanId  = e_outSyn.setIndexScan(null, idx);
      int fullScanId = e_outSyn.setFullScan();

      e_outSyn.setEvalContext(evalContext);
      e_outSyn.initialize();
      
      bufferExecOp.setScanId(scanId);
      bufferExecOp.setFullScanId(fullScanId);
      bufferExecOp.setRelationSynopsis(e_outSyn);
      bufferExecOp.setEvalContext(evalContext);
      
      outStore = ctx.getTupleStorage();
      assert (outStore != null);
      assert (outStore instanceof RelStoreImpl);
      e_outSyn.setStore((RelStore)outStore);
      int id = outStore.addStub();
      e_outSyn.setStubId(id);
    }
  }
  
  private void initIndex(ExecContext ec, PhyOptBuffer op, IEvalContext evalCtx,
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
