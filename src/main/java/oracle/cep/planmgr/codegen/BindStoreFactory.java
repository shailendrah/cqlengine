/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/BindStoreFactory.java /main/14 2011/02/07 03:36:26 sborah Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Factory for creation of Binding store 

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      01/21/11 - remove eval.setContext
    udeshmuk    03/11/10 - within clause support
    sborah      10/14/09 - support for bigdecimal
    udeshmuk    08/21/09 - pattern re-org
    parujain    03/20/09 - stateless server
    udeshmuk    03/10/09 - restructure bindstore
    hopark      10/10/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    rkomurav    10/09/07 - cleanup
    rkomurav    10/03/07 - add bind and unsure partnIndex calls
    hopark      09/04/07 - eval optimize
    rkomurav    07/26/07 - cleanup
    anasrini    07/12/07 - support for partition by
    rkomurav    05/15/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/BindStoreFactory.java st_pcbpel_udeshmuk_reorg_and_variable_nonevent/5 2010/12/13 01:51:52 udeshmuk Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.common.Datatype;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.indexes.HashIndex;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.AInstr;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.BInstr;
import oracle.cep.execution.internals.IHEval;
import oracle.cep.execution.internals.HInstr;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.factory.AEvalFactory;
import oracle.cep.execution.internals.factory.BEvalFactory;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.execution.internals.factory.HEvalFactory;
import oracle.cep.execution.stores.BindStoreImpl;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.memmgr.StoreFactoryContext;
import oracle.cep.pattern.PatternSkip;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptPatternStrmClassB;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;

class BindStoreFactory extends ExecStoreFactory
{
  
  protected ExecStore newExecStore(StoreGenContext ctx)
    throws CEPException
  {
    PhyStore              store         = ctx.getPhyStore();
    PhyOpt                op            = store.getOwnOp();
    BindStoreImpl         bindStore;
    boolean               hasPartnAttrs = false;
    
    ExecContext ec = ctx.getExecContext();
    StoreFactoryContext objCtx = new StoreFactoryContext(ec, BindStoreImpl.class.getName());
    objCtx.setOpt(store.getOwnOp());
    bindStore = (BindStoreImpl)ObjectManager.allocate(objCtx);

    if (op instanceof PhyOptPatternStrmClassB) 
    {
      PhyOptPatternStrmClassB pattOp = (PhyOptPatternStrmClassB)op;

      hasPartnAttrs = pattOp.hasPartnAttrs();
      bindStore.setPatternSkip(pattOp.getSkipClause());
      bindStore.setWithinClausePresent(pattOp.isWithin()
                                      || pattOp.isWithinInclusive());
      bindStore.setIsNonEvent(pattOp.isNonEvent());
      if(pattOp.isNonEvent())
        bindStore.setIsVariableDuration(pattOp.isDurationExpr());
      if(hasPartnAttrs)
      {
        TupleSpec             hdrTupleSpec;
        IAEval                hdrCopyEval;
        IAllocator<ITuplePtr> hdrFac;
        IEvalContext          evalContext = EvalContextFactory.create(ec);
        HashIndex             bindListPartnIndex;

        hdrTupleSpec         = getPartnHdrTupleSpec(ec, pattOp);
        // Add an extra Object attribute to store the node element of the 
        // doubly list for this partition.
        // Each node of the doubly list points to an active list or a final
        // list for this partition
        int listIndexPos     = hdrTupleSpec.addAttr(Datatype.OBJECT);
        hdrCopyEval          = getHdrCopyEval(ec, evalContext, pattOp, hdrTupleSpec);
        FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
        hdrFac               = factoryMgr.get(hdrTupleSpec);
        bindListPartnIndex   = getPartnIndex(ec, evalContext, pattOp,
                                     hdrTupleSpec, hdrFac);

        bindStore.setListIndexPos(listIndexPos);
        bindStore.setHdrTupleFactory(hdrFac);
        bindStore.setHdrCopyEval(hdrCopyEval);
        bindStore.setPartnIndex(bindListPartnIndex);
        bindStore.setEvalContext(evalContext);
      }
    }
    bindStore.setHasPartnAttrs(hasPartnAttrs);

    store.setInstStore(bindStore);
  
    return bindStore;
  }

  private TupleSpec getPartnHdrTupleSpec(ExecContext ec, PhyOptPatternStrmClassB op) 
    throws CEPException 
  {
    TupleSpec inpTupleSpec;
    TupleSpec partTupleSpec;
    int       numPartnAttrs = op.getNumPartnAttrs();
    Attr[]    partnAttrs    = op.getPartnAttrs();

    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    inpTupleSpec  = new TupleSpec(factoryMgr.getNextId(), op.getInputs()[0]);
    partTupleSpec = new TupleSpec(factoryMgr.getNextId());
    for (int pa = 0; pa < numPartnAttrs; pa++)
    {
      Attr phyAttr = partnAttrs[pa];
      int  pos     = phyAttr.getPos();

      // assert that there is a valid attribute at the given attr position
      assert inpTupleSpec.getNumAttrs() > pos;
      
      partTupleSpec.addAttr(inpTupleSpec.getAttrMetadata(pos));
    }

    // Add an extra Object attribute to store the node element of the 
    // doubly list for this partition.
    // Each node of the doubly list points to an active list or a final
    // list for this partition
  //  listIndexPos = partTupleSpec.addAttr(Datatype.OBJECT);

    return partTupleSpec;
  }
  
  private IAEval getHdrCopyEval(ExecContext ec, IEvalContext evalCtx, 
                               PhyOptPatternStrmClassB op,
                               TupleSpec hdrTupleSpec)
    throws CEPException
  {
    IAEval  hdrCopyEval   = AEvalFactory.create(ec);
    int    numPartnAttrs = op.getNumPartnAttrs();
    Attr[] partnAttrs    = op.getPartnAttrs();

    for (int pa = 0; pa < numPartnAttrs; pa++)
    {
      AInstr instr = new AInstr();
      
      instr.op = ExprHelper.getCopyOp(hdrTupleSpec.getAttrType(pa));
      instr.r1 = IEvalContext.INPUT_ROLE; 
      instr.c1 = partnAttrs[pa].getPos();
      instr.r2 = 0;
      instr.c2 = 0;
      instr.dr = IEvalContext.SYN_ROLE;
      instr.dc = pa;
      
      hdrCopyEval.addInstr(instr);
    }
    hdrCopyEval.compile();

    return hdrCopyEval;
  }

  private HashIndex getPartnIndex(ExecContext ec,
                                  IEvalContext evalCtx,
                                  PhyOptPatternStrmClassB op,
                                  TupleSpec hdrTupleSpec, IAllocator<ITuplePtr> factory) 
    throws CEPException
  {
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    HashIndex partnIndex    = new HashIndex(ec);
    int       numPartnAttrs = op.getNumPartnAttrs();
    Attr[]    partnAttrs    = op.getPartnAttrs();
    HInstr    hInstr;
    IHEval     updHash;
    IHEval     scanHash;
    IBEval     keyEqual;
    BInstr    bInstr;
    TupleSpec inpTupSpec    = new TupleSpec(factoryMgr.getNextId(), op.getInputs()[0]);
    Attr      phyAttr;
    int       pos;
    
    updHash = HEvalFactory.create(ec, numPartnAttrs);
    
    for (int attr = 0; attr < numPartnAttrs; attr++)
    {
      hInstr  = new HInstr(hdrTupleSpec.getAttrType(attr), 
                           IEvalContext.UPDATE_ROLE, 
                           new Column(attr));
      
      updHash.addInstr(hInstr);
    }
    updHash.compile();
    
    scanHash = HEvalFactory.create(ec, numPartnAttrs);
   
    for (int attr = 0; attr < numPartnAttrs; attr++)
    {
      phyAttr = partnAttrs[attr];
      pos     = phyAttr.getPos();
      hInstr  = new HInstr(inpTupSpec.getAttrType(pos), 
                           IEvalContext.INPUT_ROLE, 
                           new Column(pos));
      
      scanHash.addInstr(hInstr);
    }
    scanHash.compile();
    
    keyEqual = BEvalFactory.create(ec);
    for (int attr = 0; attr < numPartnAttrs; attr++)
    {
      phyAttr = partnAttrs[attr];
      pos     = phyAttr.getPos();

      bInstr  = new BInstr();
      // compare the attributes in the INPUT tuple with the tuple
      // in the scan role
      bInstr.op = ExprHelper.getEqOp(inpTupSpec.getAttrType(pos));
      bInstr.r1 = IEvalContext.INPUT_ROLE;
      bInstr.c1 = new Column(pos);
      
      bInstr.r2 = IEvalContext.SCAN_ROLE;
      bInstr.c2 = new Column(attr);
      
      keyEqual.addInstr(bInstr);
    }
    keyEqual.compile();
    
    partnIndex.setUpdateHashEval(updHash);
    partnIndex.setScanHashEval(scanHash);
    partnIndex.setKeyEqual(keyEqual);
    partnIndex.setEvalContext(evalCtx);
    partnIndex.setFactory(factory);
    partnIndex.initialize();
    
    return partnIndex;
  }

}
