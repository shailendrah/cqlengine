/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/ExceptFactory.java /main/16 2011/02/07 03:36:26 sborah Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares ExceptFactory in package oracle.cep.planmgr.codegen.

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 anasrini  12/20/10 - remove eval.setEvalContext
 sborah    10/14/09 - support for bigdecimal
 parujain  03/19/09 - stateless server
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 hopark    10/07/08 - use execContext to remove statics
 hopark    12/07/07 - cleanup spill
 hopark    09/04/07 - eval optimize
 najain    04/11/07 - bug fix
 hopark    04/06/07 - mark static pin for const tupleptr
 najain    03/14/07 - cleanup
 hopark    03/06/07 - use ITuplePtr
 parujain  12/19/06 - fullScanId for RelationSynopsis
 najain    12/04/06 - stores are not storage allocators
 najain    11/14/06 - free count tuple
 hopark    11/09/06 - bug 5465978 : refactor newExecOpt
 najain    08/14/06 - handle silent relations
 najain    08/08/06 - fix except
 dlenkov   06/24/06 - Implementation
 najain    06/18/06 - cleanup
 skaluska  02/28/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/ExceptFactory.java st_pcbpel_anasrini_eval_parallelism_2/1 2010/12/20 07:47:47 anasrini Exp $
 *  @author  najain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.codegen;

import java.util.Iterator;

import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.RelSource;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.indexes.HashIndex;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.AInstr;
import oracle.cep.execution.internals.AOp;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.BInstr;
import oracle.cep.execution.internals.BOp;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.IHEval;
import oracle.cep.execution.internals.HInstr;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.factory.AEvalFactory;
import oracle.cep.execution.internals.factory.BEvalFactory;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.execution.internals.factory.HEvalFactory;
import oracle.cep.execution.operators.Except;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.RelStore;
import oracle.cep.execution.synopses.RelationSynopsisImpl;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptExcept;
import oracle.cep.phyplan.PhyOptKind;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.service.ExecContext;

/**
 * ExceptFactory
 *
 * @author skaluska
 */
public class ExceptFactory extends ExecOptFactory
{

  /**
   * Constructor for ExceptFactory
   */
  public ExceptFactory()
  {

    super();
  }

  private IAEval getInitEval(ExecContext ec, PhyOpt op, ConstTupleSpec cts,
                             int countCol)
    throws CEPException
  {
    IAEval eval = AEvalFactory.create(ec);
    AInstr instr;
    int numAttrs = op.getNumAttrs();

    // copy the data columns
    for (int attr = 0; attr < numAttrs; attr++)
    {
      instr = new AInstr();

      instr.op = ExprHelper.getCopyOp(op.getAttrTypes(attr));
      instr.r1 = IEvalContext.INPUT_ROLE;
      instr.c1 = attr;
      instr.r2 = 0;
      instr.c2 = 0;
      instr.dr = IEvalContext.SYN_ROLE;
      instr.dc = attr;

      eval.addInstr(instr);
    }

    instr = new AInstr();

    // copy 0 into the column column which is known to be an integer.
    instr.op = AOp.INT_CPY;

    // value to be copied 0
    instr.r1 = IEvalContext.CONST_ROLE;
    instr.c1 = cts.addInt(0); // add a column with fixed value

    // destination : count column
    instr.dr = IEvalContext.SYN_ROLE;
    instr.dc = countCol;

    // add the instruction to the evaluator
    eval.addInstr(instr);

    // Now that last instruction has been added, compile
    eval.compile();

    return eval;
  }

  /**
   * Get the output evaluator. Ths method copies the data contents of
   * ConstantTupleSpec into the output TupleSpec.
   * 
   * @param ts
   *          tuple spec
   * 
   * @return instance of the arithmetic evaluator
   * 
   * @throws CEPException
   *           for errors encounted in creating instruction set
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
   * Get the evaluator instance to increment the value in the count column of
   * the tuple.
   * 
   * @param cts
   *          constant tuple specification
   * @param countCol
   *          index of the count column in the tuple
   * 
   * @return instance of the Arithmetic Evaluator.
   * 
   * @throws CEPException
   *           for errors encounted in creating instruction set
   */
  IAEval getIncrEval(ExecContext ec, ConstTupleSpec cts, int countCol) 
    throws CEPException
  {
    AInstr instr = new AInstr();
    IAEval aEval = AEvalFactory.create(ec);

    // integer add operation
    instr.op = AOp.INT_ADD;

    // Old value of the count
    instr.r1 = IEvalContext.SYN_ROLE;
    instr.c1 = countCol; // index of the count column

    // value to be added to old value
    instr.r2 = IEvalContext.CONST_ROLE;
    instr.c2 = cts.addInt(1); // add a column with fixed value

    // dest - new count
    instr.dr = IEvalContext.SYN_ROLE; // at the loc of the orig tuple
    instr.dc = countCol; // in the same count column

    // add the instruction to the evaluator
    aEval.addInstr(instr);

    // Now that last instruction has been added, compile
    aEval.compile();

    return aEval;
  }

  /**
   * Get the evaluator instance to decrement the value in the count column of
   * the tuple.
   * 
   * @param cts
   *          constant tuple specification
   * @param countCol
   *          index of the count column in the tuple
   * 
   * @return instance of the Arithmetic Evaluator.
   * 
   * @throws CEPException
   *           for errors encounted in creating instruction set
   */
  IAEval getDecrEval(ExecContext ec, ConstTupleSpec cts, int countCol) 
    throws CEPException
  {
    AInstr instr = new AInstr();
    IAEval aEval = AEvalFactory.create(ec);

    // integer add operation
    instr.op = AOp.INT_SUB;

    // Old value of the count
    instr.r1 = IEvalContext.SYN_ROLE;
    instr.c1 = countCol; // index of the count column

    // value to be subtracted from old value
    instr.r2 = IEvalContext.CONST_ROLE;
    instr.c2 = cts.addInt(1); // add a column with fixed value

    // dest - new count
    instr.dr = IEvalContext.SYN_ROLE; // at the loc of the original tuple
    instr.dc = countCol; // in the same count column

    // add the instruction to the evaluator
    aEval.addInstr(instr);

    // Now that last instruction has been added, compile
    aEval.compile();

    return aEval;
  }

  /**
   * Get the evaluator instance to check if the current value stored in the
   * count column is greater than 0.
   * @param ec TODO
   * @param cts
   *          constant tuple specification
   * @param countCol
   *          index of the count column in the tuple
   * 
   * @return instance of the Boolean Evaluator.
   * 
   * @throws CEPException
   *           for errors encounted in creating instruction set
   */
  IBEval getPosEval(ExecContext ec, ConstTupleSpec cts, int countCol)
    throws CEPException
  {
    BInstr instr = new BInstr();
    IBEval bEval = BEvalFactory.create(ec);

    // lhs > rhs check where rhs is 0
    instr.op = BOp.INT_GT; // integer greater than operation

    // lhs value count column
    instr.r1 = IEvalContext.SYN_ROLE;
    instr.c1 = new Column(countCol); // index of the count column

    instr.r2 = IEvalContext.CONST_ROLE;
    instr.c2 = new Column(cts.addInt(0)); // add a column with fixed value

    // add the instruction to the evaluator
    bEval.addInstr(instr);

    // Now that last instruction has been added, compile
    bEval.compile();

    return bEval;
  }

  /**
   * Get the evaluator instance to check if the current value stored in the
   * count column is greater than or equal to 0.
   * @param ec TODO
   * @param cts
   *          constant tuple specification
   * @param countCol
   *          index of the count column in the tuple
   * 
   * @return instance of the Boolean Evaluator.
   * 
   * @throws CEPException
   *           for errors encounted in creating instruction set
   */
  IBEval getNonNegEval(ExecContext ec, ConstTupleSpec cts, int countCol) 
    throws CEPException
  {
    BInstr instr = new BInstr();
    IBEval bEval = BEvalFactory.create(ec);

    // lhs > rhs check where rhs is 0
    instr.op = BOp.INT_GE; // integer greater than operation

    // lhs value count column
    instr.r1 = IEvalContext.SYN_ROLE;
    instr.c1 = new Column(countCol); // index of the count column

    instr.r2 = IEvalContext.CONST_ROLE;
    instr.c2 = new Column(cts.addInt(0)); // add a column with fixed value

    // add the instruction to the evaluator
    bEval.addInstr(instr);

    // Now that last instruction has been added, compile
    bEval.compile();

    return bEval;
  }

  /**
   * Get the evaluator instance to check if the current value stored in the
   * count column is less than 0.
   * @param ec TODO
   * @param cts
   *          constant tuple specification
   * @param countCol
   *          index of the count column in the tuple
   * 
   * @return instance of the Boolean Evaluator.
   * 
   * @throws CEPException
   *           for errors encounted in creating instruction set
   */
  IBEval getNegEval(ExecContext ec, ConstTupleSpec cts, int countCol)
    throws CEPException
  {
    BInstr instr = new BInstr();
    IBEval bEval = BEvalFactory.create(ec);

    // lhs < rhs check where rhs is 0
    instr.op = BOp.INT_LT; // integer less than operation

    // lhs value count column
    instr.r1 = IEvalContext.SYN_ROLE;
    instr.c1 = new Column(countCol); // index of the count column
    instr.e1 = null;

    instr.r2 = IEvalContext.CONST_ROLE;
    instr.c2 = new Column(cts.addInt(0)); // add a column with fixed value
    instr.e2 = null;

    // add the instruction to the evaluator
    bEval.addInstr(instr);

    // Now that last instruction has been added, compile
    bEval.compile();

    return bEval;
  }

  /**
   * Get the evaluator instance to check if the current value stored in the
   * count column is equal to 0.
   * @param ec TODO
   * @param cts
   *          constant tuple specification
   * @param countCol
   *          index of the count column in the tuple
   * 
   * @return instance of the Boolean Evaluator.
   * 
   * @throws CEPException
   *           for errors encounted in creating instruction set
   */
  IBEval getZeroEval(ExecContext ec, ConstTupleSpec cts, int countCol) 
    throws CEPException
  {
    BInstr instr = new BInstr();
    IBEval bEval = BEvalFactory.create(ec);

    // lhs = rhs check where rhs is 0
    instr.op = BOp.INT_EQ; // integer equal to operation

    // lhs value count column
    instr.r1 = IEvalContext.SYN_ROLE;
    instr.c1 = new Column(countCol); // index of the count column
    instr.e1 = null;

    instr.r2 = IEvalContext.CONST_ROLE;
    instr.c2 = new Column(cts.addInt(0)); // add a column with fixed value
    instr.e2 = null;

    // add the instruction to the evaluator
    bEval.addInstr(instr);

    // Now that last instruction has been added, compile
    bEval.compile();

    return bEval;
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
  HashIndex getIndex(ExecContext ec, IEvalContext evalCtx, TupleSpec tupSpec, 
                     int roleNum, IAllocator<ITuplePtr> factory)
      throws CEPException
  {
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
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

  /*
   * NewExecOpt
   */

  @Override
  ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException
  {
    assert ctx != null;
    PhyOpt op = ctx.getPhyopt();
    assert ((op != null) && (op.getOperatorKind() == PhyOptKind.PO_EXCEPT));
    return new Except(ctx.getExecContext());

  }

  @Override
  void setupExecOpt(CodeGenContext ctx) throws CEPException
  {
    assert ctx.getExecOpt() instanceof Except;
    PhyOpt op = ctx.getPhyopt();
    assert op instanceof PhyOptExcept;

    Except exceptOp = (Except) ctx.getExecOpt();
    PhyOptExcept eop = (PhyOptExcept) op;
    ExecContext ec = ctx.getExecContext();
    
    IEvalContext evalContext = EvalContextFactory.create(ec);
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    ConstTupleSpec constTupSpec = new ConstTupleSpec(factoryMgr);

    TupleSpec tupSpec;
    // Index for looking up in synopsis
    tupSpec = CodeGenHelper.getTupleSpec(ec, op);

    // set the out evaluator for the IStream using the unaltered
    // tuple (tuple with added count column);
    IAEval outEval = getOutEval(ec, tupSpec);

    // Create an index over the out/count synopsis
    HashIndex outIdx = getIndex(ec, evalContext, tupSpec, 
                                IEvalContext.SYN_ROLE,
                                ctx.getTupleStorage().getFactory());

    TupleSpec oldSpec = new TupleSpec(factoryMgr.getNextId(),
                                      tupSpec.getNumAttrs());
    oldSpec.copy(tupSpec);

    // add a count attribute to the original tuple spec
    int countCol = tupSpec.addAttr(Datatype.INT);

    // Handle Stores and StorageAllocs
    IAEval initEval = getInitEval(ec, op, constTupSpec, countCol);

    assert (op.getStore() != null);
    assert ((op.getIsStream() == false) &&
            (op.getStore().getStoreKind() == PhyStoreKind.PHY_REL_STORE));

    // Handle Synopsis
    // Output Synopsis
    PhySynopsis outPhySyn = eop.getOutSynopsis();

    ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
    allCtx.setOpt(op);
    allCtx.setObjectType(RelationSynopsisImpl.class.getName());
    RelationSynopsisImpl outSyn = (RelationSynopsisImpl) ObjectManager
        .allocate(allCtx);
    ExecStore outStore = ctx.getTupleStorage();
    assert (outStore != null);
    assert (outStore instanceof RelStore);

    outSyn.setStore((RelStore) outStore);
    outSyn.setStubId(outStore.addStub());
    outPhySyn.setSyn(outSyn);

    int outScanId = outSyn.setIndexScan(null, outIdx);
    int fullScanId = outSyn.setFullScan();
    outSyn.setEvalContext(evalContext);
    outSyn.initialize();

    // Input Synopsis
    PhySynopsis countPhySyn = eop.getCountSynopsis();
    assert countPhySyn != null;
    allCtx.setOpt(op);
    allCtx.setObjectType(RelationSynopsisImpl.class.getName());
    RelationSynopsisImpl countSyn = (RelationSynopsisImpl) ObjectManager
        .allocate(allCtx);
    ExecStore countStore = StoreInst.instStore(new StoreGenContext(ec, countPhySyn
        .getStwstore(), tupSpec));
    assert countStore instanceof RelStore;
    HashIndex countIdx = getIndex(ec, evalContext, oldSpec, IEvalContext.INPUT_ROLE, countStore.getFactory());

    countSyn.setStore((RelStore) countStore);
    countSyn.setStubId(countStore.addStub());

    int countScanId = countSyn.setIndexScan(null, countIdx);
    countSyn.setEvalContext(evalContext);
    countSyn.initialize();

    exceptOp.setOutSyn(outSyn);
    exceptOp.setCountSyn(countSyn);
    exceptOp.setCountTupleFactory(countStore.getFactory());
    exceptOp.setEvalContext(evalContext);
    exceptOp.setInitEval(initEval);
    exceptOp.setOutEval(outEval);
    exceptOp.setOutScanId(outScanId);
    exceptOp.setFullScanId(fullScanId);
    exceptOp.setCountScanId(countScanId);

    // Setup the input queues
    exceptOp.setLeftInputQueue(getInputQueue(op, ec, 0));
    exceptOp.setRightInputQueue(getInputQueue(op, ec, 1));

    // Setup the input stores
    exceptOp.setLeftTupleStorageAlloc(getInputStore(op, ec, 0).getFactory());
    exceptOp.setRightTupleStorageAlloc(getInputStore(op, ec, 1).getFactory());

    // generate the required eval operators
    // count incrementor
    IAEval incrEval = getIncrEval(ec, constTupSpec, countCol);
    exceptOp.setIncrEval(incrEval);

    // count decrementor
    IAEval decrEval = getDecrEval(ec, constTupSpec, countCol);
    exceptOp.setDecrEval(decrEval);

    // 'count is positive' evaluator
    IBEval posEval = getPosEval(ec, constTupSpec, countCol);
    exceptOp.setPosEval(posEval);

    // 'count is not-negative' evaluator
    IBEval nonNegEval = getNonNegEval(ec, constTupSpec, countCol);
    exceptOp.setNonNegEval(nonNegEval);

    // 'count is negative' evaluator
    IBEval negEval = getNegEval(ec, constTupSpec, countCol);
    exceptOp.setNegEval(negEval);

    // 'count is zero' evaluator
    IBEval zeroEval = getZeroEval(ec, constTupSpec, countCol);
    exceptOp.setZeroEval(zeroEval);

    // above evaluators extend the constTupSpec to accommodate
    // some integer values. Using the extended constTupSpec, generate
    // a tuple with all the const values.
    IAllocator<ITuplePtr> ctf = factoryMgr.get(constTupSpec.getTupleSpec());
    ITuplePtr constTuple = (ITuplePtr)ctf.allocate(); //SCRATCH_TUPLE
    constTupSpec.populateTuple(ec, constTuple);

    // bind the constant tuple to the evaluation context
    evalContext.bind(constTuple, IEvalContext.CONST_ROLE);

    boolean leftSilentRelns = op.getInputs()[0].isSilentRelnDep();
    boolean rightSilentRelns = op.getInputs()[1].isSilentRelnDep();

    exceptOp.setLeftSilentRelns(leftSilentRelns);
    exceptOp.setRightSilentRelns(rightSilentRelns);

    if (leftSilentRelns)
    {
      Iterator<PhyOpt> iter = op.getInputs()[0].getSilentRelnDep().iterator();

      while (iter.hasNext())
      {
        PhyOpt opDep = iter.next();
        exceptOp.addLeftInputRelns((RelSource) opDep.getInstOp());
      }
    }

    if (rightSilentRelns)
    {
      Iterator<PhyOpt> iter = op.getInputs()[1].getSilentRelnDep().iterator();

      while (iter.hasNext())
      {
        PhyOpt opDep = iter.next();
        exceptOp.addRightInputRelns((RelSource) opDep.getInstOp());
      }
    }
  }
}
