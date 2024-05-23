/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/MinusFactory.java /main/6 2011/02/07 03:36:26 sborah Exp $ */

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
    anasrini    12/19/10 - remove eval.setEvalContext
    parujain    03/19/09 - stateless server
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    sborah      10/17/08 - setting numCompAttrs
    hopark      03/17/08 - fix tupleFactory
    sbishnoi    09/26/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/MinusFactory.java st_pcbpel_anasrini_eval_parallelism_2/1 2010/12/20 07:47:45 anasrini Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.codegen;

import java.util.Iterator;

import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.RelSource;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.indexes.HashIndex;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.AInstr;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.BInstr;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.IHEval;
import oracle.cep.execution.internals.HInstr;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.factory.AEvalFactory;
import oracle.cep.execution.internals.factory.BEvalFactory;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.execution.internals.factory.HEvalFactory;
import oracle.cep.execution.operators.Minus;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.RelStore;
import oracle.cep.execution.stores.LineageStore;
import oracle.cep.execution.synopses.RelationSynopsisImpl;
import oracle.cep.execution.synopses.LineageSynopsisImpl;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptMinus;
import oracle.cep.phyplan.PhyOptKind;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.service.ExecContext;

public class MinusFactory extends ExecOptFactory
{

  /**
   * Constructor for MinusFactory
   */
  public MinusFactory()
  {
    super();
  }

  
  /**
   * Get the output evaluator. Ths method copies the data contents of
   * ConstantTupleSpec into the output TupleSpec.
   * @param ec TODO
   * @param ts
   *          tuple specification
   * 
   * @return instance of the arithmetic evaluator
   * 
   * @throws CEPException
   *           for errors encountered in creating instruction set
   */
  IAEval getOutEval(ExecContext ec, TupleSpec ts) throws CEPException
  {
    AInstr instr;
    IAEval aEval = AEvalFactory.create(ec);
    int numAttrs = ts.getNumAttrs();

    // copy the data columns
    for (int attr = 0; attr < numAttrs; attr++)
    {
      instr = new AInstr();

      instr.op = ExprHelper.getCopyOp(ts.getAttrType(attr));
      instr.r1 = IEvalContext.SYN_ROLE;
      instr.c1 = attr;
      instr.r2 = 0;
      instr.c2 = 0;
      instr.dr = IEvalContext.NEW_OUTPUT_ROLE;
      instr.dc = attr;

      aEval.addInstr(instr);
    }
    aEval.compile();

    return aEval;
  }

   
  /**
   * Setup and return the index for searching on the synopsis
   * @param ec TODO
   * @param evalCtx
   *          evaluation context for the operator
   * @param tupSpec
   *          tuple specification
   * @param roleNum
   *          role Number
   * 
   * @return an instance of the HashIndex set up to search the synopsis
   * 
   * @throws CEPException
   *           when not able create instructions for the index
   */
  HashIndex getIndex(ExecContext ec, IEvalContext evalCtx, TupleSpec tupSpec, int roleNum, 
                     IAllocator<ITuplePtr> factory,PhyOptMinus minusOp)
      throws CEPException
  {
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    HashIndex outIndex = new HashIndex(ec); // hash index on synopsis
    HInstr hInstr;
    IHEval updHash, scanHash;
    IBEval keyEqual;
    BInstr bInstr;
    //int numAttrs = tupSpec.getNumAttrs();
    int numAttrs = minusOp.getNumComparisonAttrs();

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
  
  /**
   * Setup and return the index for searching on the left input synopsis
   * @param ec TODO
   * @param evalCtx
   *          evaluation context for the operator
   * @param tupSpec
   *          tuple specification
   * @param  rightTupSpec
   *          right input tuple specification
   * @param roleNum
   *          role Number
   * 
   * @return an instance of the HashIndex set up to search the synopsis
   * 
   * @throws CEPException
   *           when not able create instructions for the index
   */
  
  HashIndex getLeftSelectedInputIndex(ExecContext ec, IEvalContext evalCtx, 
                                      TupleSpec tupSpec,
                                      TupleSpec rightTupSpec, int roleNum, 
                                      IAllocator<ITuplePtr> factory,
                                      PhyOpt op)
    throws CEPException
  {
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    HashIndex leftInputIndex = new HashIndex(ec); // hash index on left inp syn
    HInstr hInstr;
    IHEval updHash, scanHash;
    IBEval keyEqual;
    BInstr bInstr;
    
    PhyOptMinus opMinus        = (PhyOptMinus)op;
    int numComparisonAttrs     = opMinus.getNumComparisonAttrs();
    int leftComparisonAttrPos  = 0;
    int rightComparisonAttrPos = 0;
    
    updHash = HEvalFactory.create(ec, numComparisonAttrs);
    for (int attr = 0; attr < numComparisonAttrs; attr++)
    {
      leftComparisonAttrPos = opMinus.getLeftComparisonAttrs()[attr].getPos();
      hInstr = 
        new HInstr(tupSpec.getAttrType(leftComparisonAttrPos), 
                   IEvalContext.UPDATE_ROLE,
                   new Column(leftComparisonAttrPos));

      updHash.addInstr(hInstr);
    }
    updHash.compile();

    scanHash = HEvalFactory.create(ec, numComparisonAttrs);
    for (int attr = 0; attr < numComparisonAttrs; attr++)
    {
      rightComparisonAttrPos = 
        opMinus.getRightComparisonAttrs()[attr].getPos();
      hInstr = new HInstr(rightTupSpec.getAttrType(rightComparisonAttrPos),
                          roleNum, 
                          new Column(rightComparisonAttrPos));

      scanHash.addInstr(hInstr);
    }
    scanHash.compile();

    keyEqual = BEvalFactory.create(ec);
    for (int attr = 0; attr < numComparisonAttrs; attr++)
    {
      leftComparisonAttrPos  = opMinus.getLeftComparisonAttrs()[attr].getPos();
      rightComparisonAttrPos =
        opMinus.getRightComparisonAttrs()[attr].getPos();
      
      bInstr = new BInstr();

      bInstr.op = 
        ExprHelper.getEqOp(rightTupSpec.getAttrType(rightComparisonAttrPos));
      bInstr.r1 = roleNum;
      bInstr.c1 = new Column(rightComparisonAttrPos);

      bInstr.r2 = IEvalContext.SCAN_ROLE;
      bInstr.c2 = new Column(leftComparisonAttrPos);

      keyEqual.addInstr(bInstr);
    }
    keyEqual.compile();

    leftInputIndex.setUpdateHashEval(updHash);
    leftInputIndex.setScanHashEval(scanHash);
    leftInputIndex.setKeyEqual(keyEqual);
    leftInputIndex.setEvalContext(evalCtx);
    leftInputIndex.setFactory(factory);
    leftInputIndex.initialize();

    return leftInputIndex;
  }
  
  /**
   * Setup and return the index for searching on the right input synopsis
   * @param ec TODO
   * @param evalCtx
   *          evaluation context for the operator
   * @param tupSpec
   *          tuple specification
   * @param  rightTupSpec
   *          right input tuple specification
   * @param roleNum
   *          role Number
   * 
   * @return an instance of the HashIndex set up to search the synopsis
   * 
   * @throws CEPException
   *           when not able create instructions for the index
   */
  
  HashIndex getRightSelectedInputIndex(ExecContext ec, IEvalContext evalCtx,
                                       TupleSpec tupSpec,
                                       TupleSpec rightTupSpec, 
                                       int roleNum,
                                       IAllocator<ITuplePtr> factory,
                                       PhyOpt op)
    throws CEPException
  {
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    HashIndex rightInputIndex = new HashIndex(ec); // hash index on right inp syn
    HInstr hInstr;
    IHEval updHash, scanHash;
    IBEval keyEqual;
    BInstr bInstr;
    
    PhyOptMinus opMinus       = (PhyOptMinus)op;
    int numComparisonAttrs     = opMinus.getNumComparisonAttrs();
    int leftComparisonAttrPos  = 0;
    int rightComparisonAttrPos = 0;
    
    updHash = HEvalFactory.create(ec, numComparisonAttrs);
    for (int attr = 0; attr < numComparisonAttrs; attr++)
    {
      rightComparisonAttrPos =
        opMinus.getRightComparisonAttrs()[attr].getPos();

      hInstr = 
        new HInstr(rightTupSpec.getAttrType(rightComparisonAttrPos), 
                   IEvalContext.UPDATE_ROLE,
                   new Column(rightComparisonAttrPos));

      updHash.addInstr(hInstr);
    }
    updHash.compile();

    scanHash = HEvalFactory.create(ec, numComparisonAttrs);
    for (int attr = 0; attr < numComparisonAttrs; attr++)
    {
      leftComparisonAttrPos = opMinus.getLeftComparisonAttrs()[attr].getPos();
      hInstr = new HInstr(tupSpec.getAttrType(leftComparisonAttrPos),
                          roleNum, 
                          new Column(leftComparisonAttrPos));

      scanHash.addInstr(hInstr);
    }
    scanHash.compile();
    

    keyEqual = BEvalFactory.create(ec);
    for (int attr = 0; attr < numComparisonAttrs; attr++)
    {
      leftComparisonAttrPos  = opMinus.getLeftComparisonAttrs()[attr].getPos();
      rightComparisonAttrPos =
        opMinus.getRightComparisonAttrs()[attr].getPos();
      
      bInstr = new BInstr();

      bInstr.op = 
        ExprHelper.getEqOp(tupSpec.getAttrType(leftComparisonAttrPos));
      bInstr.r1 = roleNum;
      bInstr.c1 = new Column(leftComparisonAttrPos);

      bInstr.r2 = IEvalContext.SCAN_ROLE;
      bInstr.c2 = new Column(rightComparisonAttrPos);

      keyEqual.addInstr(bInstr);
    }
    keyEqual.compile();

    rightInputIndex.setUpdateHashEval(updHash);
    rightInputIndex.setScanHashEval(scanHash);
    rightInputIndex.setKeyEqual(keyEqual);
    rightInputIndex.setEvalContext(evalCtx);
    rightInputIndex.setFactory(factory);
    rightInputIndex.initialize();

    return rightInputIndex;
  }
  
  HashIndex getRightInputIndex(ExecContext ec, IEvalContext evalCtx,
                               TupleSpec tupSpec, TupleSpec rightTupSpec, 
                               int roleNum, IAllocator<ITuplePtr> factory,
                               PhyOpt op)
  throws CEPException
  {
    // Here We will compare two tuples; both of them are from
    // RightInputSynopsis
    // leftComparisonAttrPos represent Tuple1 from RightInputSynopsis
    // rightcomparisonAttrPos represent Tuple2 from RightInputSynopsis
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();

    // hash index on right inp syn
    HashIndex rightInputIndex = new HashIndex(ec);
    HInstr hInstr;
    IHEval updHash, scanHash;
    IBEval keyEqual;
    BInstr bInstr;
    
    PhyOptMinus opMinus        = (PhyOptMinus)op;
    int numComparisonAttrs     = opMinus.getNumComparisonAttrs();
    int rightComparisonAttrPos = 0;
    
    updHash = HEvalFactory.create(ec, numComparisonAttrs);
    for (int attr = 0; attr < numComparisonAttrs; attr++)
    {
      rightComparisonAttrPos =
        opMinus.getRightComparisonAttrs()[attr].getPos();
      hInstr = 
        new HInstr(rightTupSpec.getAttrType(rightComparisonAttrPos), 
      IEvalContext.UPDATE_ROLE,
      new Column(rightComparisonAttrPos));
    
      updHash.addInstr(hInstr);
    }
    updHash.compile();
    
    scanHash = HEvalFactory.create(ec, numComparisonAttrs);
    for (int attr = 0; attr < numComparisonAttrs; attr++)
    {
      rightComparisonAttrPos = 
        opMinus.getRightComparisonAttrs()[attr].getPos();
      hInstr = new HInstr(rightTupSpec.getAttrType(rightComparisonAttrPos),
                          roleNum, 
                          new Column(rightComparisonAttrPos));
    
      scanHash.addInstr(hInstr);
    }
    scanHash.compile();
    
    
    keyEqual = BEvalFactory.create(ec);
    for (int attr = 0; attr < numComparisonAttrs; attr++)
    {
      rightComparisonAttrPos =
        opMinus.getRightComparisonAttrs()[attr].getPos();
    
      bInstr = new BInstr();
      
      bInstr.op = 
      ExprHelper.getEqOp(rightTupSpec.getAttrType(rightComparisonAttrPos));
      bInstr.r1 = roleNum;
      bInstr.c1 = new Column(rightComparisonAttrPos);
      
      bInstr.r2 = IEvalContext.SCAN_ROLE;
      bInstr.c2 = new Column(rightComparisonAttrPos);
      
      keyEqual.addInstr(bInstr);
    }
    keyEqual.compile();
    
    rightInputIndex.setUpdateHashEval(updHash);
    rightInputIndex.setScanHashEval(scanHash);
    rightInputIndex.setKeyEqual(keyEqual);
    rightInputIndex.setEvalContext(evalCtx);
    rightInputIndex.setFactory(factory);
    rightInputIndex.initialize();
    
    return rightInputIndex;
  }

  /*
   * NewExecOpt
   */

  @Override
  ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException
  {
    assert ctx != null;
    PhyOpt op = ctx.getPhyopt();
    assert ((op != null) && (op.getOperatorKind() == PhyOptKind.PO_MINUS));
    return new Minus(ctx.getExecContext());

  }

  @Override
  void setupExecOpt(CodeGenContext ctx) throws CEPException
  {
    assert ctx.getExecOpt() instanceof Minus;
    PhyOpt op = ctx.getPhyopt();
    assert op instanceof PhyOptMinus;

    ExecContext ec = ctx.getExecContext();
    Minus minusOp = (Minus) ctx.getExecOpt();
    PhyOptMinus opMinus = (PhyOptMinus) op;

    IEvalContext evalContext = EvalContextFactory.create(ec);
    
    TupleSpec tupSpec;
    TupleSpec rightTupSpec;
    
    // Index for looking up in synopsis
    tupSpec = CodeGenHelper.getTupleSpec(ec, op);
    
    // Tuple Specification for right input Tuple
    rightTupSpec = CodeGenHelper.getTupleSpec(ec, opMinus.getInputs()[1]);

    // set the out evaluator for the IStream using the unaltered
    // tuple (tuple with added count column);
    IAEval outEval = getOutEval(ec, tupSpec);
    
    assert (op.getStore() != null);
    assert ((op.getIsStream() == false) && 
            (op.getStore().getStoreKind() == PhyStoreKind.PHY_LIN_STORE));

    minusOp.setIsNotInSetOp(opMinus.getIsNotInSetOp());
    
    // Handle Synopsis
    
    // Left Input Synopsis
    PhySynopsis leftInputPhySyn = opMinus.getLeftInputSynopsis();
    assert leftInputPhySyn != null;
    ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
    allCtx.setOpt(op);
    allCtx.setObjectType(RelationSynopsisImpl.class.getName());
    RelationSynopsisImpl leftInputSyn = 
      (RelationSynopsisImpl) ObjectManager.allocate(allCtx);
    
    ExecStore leftInputStore = leftInputPhySyn.getStwstore().getInstStore();
    assert leftInputStore instanceof RelStore;
    leftInputSyn.setStore((RelStore)leftInputStore);
    leftInputSyn.setStubId(leftInputStore.addStub());
    
    //Right Input Synopsis
    PhySynopsis rightInputPhySyn = opMinus.getRightInputSynopsis();
    assert rightInputPhySyn != null;     
    allCtx.setOpt(op);
    allCtx.setObjectType(RelationSynopsisImpl.class.getName());
    RelationSynopsisImpl rightInputSyn =
      (RelationSynopsisImpl) ObjectManager.allocate(allCtx);
    ExecStore rightInputStore = rightInputPhySyn.getStwstore().getInstStore();
    
    assert rightInputStore instanceof RelStore;
    rightInputSyn.setStore((RelStore)rightInputStore);
    rightInputSyn.setStubId(rightInputStore.addStub());
    
    minusOp.setLeftInputSyn(leftInputSyn);
    minusOp.setLeftInputTupleFactory(leftInputStore.getFactory());
    minusOp.setRightInputSyn(rightInputSyn);
    minusOp.setRightInputTupleFactory(rightInputStore.getFactory());
    
    
    if(minusOp.getIsNotInSetOp())
    {
   // a selected column index over left input synopsis 
      HashIndex leftSelectedInputIndex = 
        getLeftSelectedInputIndex(ec,
                          evalContext,
                          tupSpec,
                          rightTupSpec,
                          IEvalContext.INPUT_ROLE,
                          getInputStore(op, ec, 0).getFactory(), op);
      
      // a selected column index over right input synopsis
      HashIndex rightSelectedInputIndex = 
        getRightSelectedInputIndex(ec,
                           evalContext,
                           tupSpec,
                           rightTupSpec,
                           IEvalContext.INPUT_ROLE,
                           getInputStore(op, ec, 1).getFactory(), op);
      
   // a selected column index over right input synopsis where we search right
   // tuple inside right
      HashIndex rightInputIndex = 
        getRightInputIndex(ec,
                           evalContext,
                           tupSpec,
                           rightTupSpec,
                           IEvalContext.INPUT_ROLE,
                           getInputStore(op, ec, 1).getFactory(), op);
      
      
      assert leftSelectedInputIndex != null;
      int leftSelectedInputScanId = 
        leftInputSyn.setIndexScan(null, leftSelectedInputIndex);
      
      assert rightSelectedInputIndex != null;
      int rightSelectedInputScanId = 
        rightInputSyn.setIndexScan(null, rightSelectedInputIndex);
      int rightInputScanId = 
        rightInputSyn.setIndexScan(null, rightInputIndex);
      
         
      minusOp.setLeftSelectedScanId(leftSelectedInputScanId);
      minusOp.setRightSelectedScanId(rightSelectedInputScanId);
      minusOp.setRightInputScanId(rightInputScanId);
    }
    else
    {

      // an all column index over left input synopsis
      HashIndex leftFullInputIndex = 
        getIndex(ec,
                 evalContext, 
                 tupSpec, 
                 IEvalContext.INPUT_ROLE, 
                 getInputStore(op, ec, 0).getFactory(),opMinus);
      
      // an all column index over right input synopsis
      HashIndex rightFullInputIndex = 
        getIndex(ec,
                 evalContext, 
                 rightTupSpec, 
                 IEvalContext.INPUT_ROLE, 
                 getInputStore(op, ec, 1).getFactory(),opMinus);
            
      int leftFullInputScanId =
        leftInputSyn.setIndexScan(null, leftFullInputIndex);
      
      int rightFullInputScanId = 
        rightInputSyn.setIndexScan(null, rightFullInputIndex);
      
      minusOp.setLeftFullScanId(leftFullInputScanId);
      minusOp.setRightFullScanId(rightFullInputScanId);
     
    }
    
    leftInputSyn.setEvalContext(evalContext);
    leftInputSyn.initialize();
    
    rightInputSyn.setEvalContext(evalContext);
    rightInputSyn.initialize();
    
    // Output Synopsis
    PhySynopsis outPhySyn = opMinus.getOutSynopsis();

    allCtx.setOpt(op);
    allCtx.setObjectType(LineageSynopsisImpl.class.getName());
    LineageSynopsisImpl outSyn = 
      (LineageSynopsisImpl) ObjectManager.allocate(allCtx);
    ExecStore outStore = ctx.getTupleStorage();
    assert (outStore != null);
    assert (outStore instanceof LineageStore);

    outSyn.setStore((LineageStore) outStore);
    outSyn.setStubId(outStore.addStub());
    outPhySyn.setSyn(outSyn);

    minusOp.setOutSyn(outSyn);
    minusOp.setEvalContext(evalContext);
    minusOp.setOutEval(outEval);
    
    // Setup the input queues
    minusOp.setLeftInputQueue(getInputQueue(op, ec, 0));
    minusOp.setRightInputQueue(getInputQueue(op, ec, 1));

    // Setup the input stores
    minusOp.setLeftTupleStorageAlloc(getInputStore(op, ec, 0).getFactory());
    minusOp.setRightTupleStorageAlloc(getInputStore(op, ec, 1).getFactory());


    boolean leftSilentRelns = op.getInputs()[0].isSilentRelnDep();
    boolean rightSilentRelns = op.getInputs()[1].isSilentRelnDep();

    minusOp.setLeftSilentRelns(leftSilentRelns);
    minusOp.setRightSilentRelns(rightSilentRelns);

    if (leftSilentRelns)
    {
      Iterator<PhyOpt> iter = op.getInputs()[0].getSilentRelnDep().iterator();

      while (iter.hasNext())
      {
        PhyOpt opDep = iter.next();
        minusOp.addLeftInputRelns((RelSource) opDep.getInstOp());
      }
    }

    if (rightSilentRelns)
    {
      Iterator<PhyOpt> iter = op.getInputs()[1].getSilentRelnDep().iterator();

      while (iter.hasNext())
      {
        PhyOpt opDep = iter.next();
        minusOp.addRightInputRelns((RelSource) opDep.getInstOp());
      }
    }
  }
}
