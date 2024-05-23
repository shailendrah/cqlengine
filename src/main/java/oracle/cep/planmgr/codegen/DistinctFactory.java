/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/DistinctFactory.java /main/14 2014/01/08 02:59:48 udeshmuk Exp $ */

/* Copyright (c) 2006, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares DistinctFactory in package oracle.cep.planmgr.codegen.

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
    udeshmuk  05/26/12 - set tuplespec in execopt
    anasrini  12/20/10 - remove eval.setEvalContext
    sborah    10/14/09 - support for bigdecimal
    parujain  03/19/09 - stateless server
    hopark    10/10/08 - remove statics
    hopark    10/09/08 - remove statics
    hopark    10/07/08 - use execContext to remove statics
    hopark    12/07/07 - cleanup spill
    hopark    09/04/07 - eval optimize
    sbishnoi  07/20/07 - fullScanId for RelationSynopsis
    sbishnoi  05/14/07 - re-implementation
    najain    12/04/06 - stores are not storage allocators
    hopark    11/09/06 - bug 5465978 : refactor newExecOpt
    najain    06/18/06 - cleanup
    skaluska  02/28/06 - Creation
    skaluska  02/28/06 - Creation
 */
 
/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/DistinctFactory.java /main/14 2014/01/08 02:59:48 udeshmuk Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */


package oracle.cep.planmgr.codegen;

import java.util.Iterator;

import oracle.cep.common.Datatype;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.ExecException;
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
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.Distinct;
import oracle.cep.execution.operators.RelSource;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.RelStore;
import oracle.cep.execution.synopses.RelationSynopsisImpl;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptKind;
import oracle.cep.phyplan.PhyOptDistinct;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.phyplan.SynopsisKind;
import oracle.cep.service.ExecContext;


/**
 * DistinctFactory
 *
 * @author skaluska
 */
public class DistinctFactory extends ExecOptFactory
{
  
  /**
  * Constructor for DistinctFactory
  */
  public DistinctFactory()
  {
    super();
  }
  
  @Override
  public CodeGenContext createCodeGenContext(ExecContext ec, Query query, 
                                             PhyOpt phyopt)
  {
    return new DistinctContext(ec, query, phyopt); 
  }
    
  public void setDistinctOp(ExecContext ec, DistinctContext ctx, PhyOpt op) 
  throws CEPException
  {
    TupleSpec tupSpec = CodeGenHelper.getTupleSpec(ec, op);
    
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    TupleSpec oldSpec = new TupleSpec(factoryMgr.getNextId(), tupSpec.getNumAttrs());
    oldSpec.copy(tupSpec);
    
    int countCol = tupSpec.addAttr(Datatype.INT);
    ctx.setCountCol(countCol);
    ctx.setTupSpec(tupSpec);
    ctx.setOldSpec(oldSpec);
  }

  private IAEval getInitEval(ExecContext ec, PhyOpt op, ConstTupleSpec cts,
                             int countCol) throws CEPException
  {
    IAEval  eval  = AEvalFactory.create(ec);
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

    // value to be copied 1
    instr.r1 = IEvalContext.CONST_ROLE;
    instr.c1 = cts.addInt(1); // add a column with fixed value

    // destination : count column
    instr.dr = IEvalContext.SYN_ROLE;
    instr.dc = countCol;

    // add the instruction to the evaluator
    eval.addInstr(instr);

    // now that last instruction has been added, compile
    eval.compile();

    return eval;
  }
  
  private IAEval getOutEval(ExecContext ec, PhyOpt op) throws ExecException
  {
      IAEval eval = AEvalFactory.create(ec);

      for (int i = 0; i < op.getNumAttrs(); i++)
      {
          AInstr instr = new AInstr();
          instr.op = ExprHelper.getCopyOp(op.getAttrTypes(i));

          // Source
          instr.r1 = IEvalContext.INPUT_ROLE;
          instr.c1 = i;

          // Destination
          instr.dr = IEvalContext.NEW_OUTPUT_ROLE;
          instr.dc = i;

          eval.addInstr(instr);
      }
      eval.compile();

      return eval;
  }
  
  private IAEval getIncrEval(ExecContext ec, ConstTupleSpec cts, int countCol)
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

    // now that last instruction has been added, compile
    aEval.compile();

    return aEval;
  }

  private IAEval getDecrEval(ExecContext ec, ConstTupleSpec cts, int countCol)
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

    // now that last instruction has been added, compile
    aEval.compile();

    return aEval;
  }

  private IBEval getOneEval(ExecContext ec, ConstTupleSpec cts, int countCol)
    throws CEPException
  {
    BInstr instr = new BInstr();
    IBEval bEval  = BEvalFactory.create(ec);

    // lhs = rhs check where rhs is 1
    instr.op = BOp.INT_EQ; // integer equal to operation

    // lhs value count column
    instr.r1 = IEvalContext.SYN_ROLE;
    instr.c1 = new Column(countCol); // index of the count column
    instr.e1 = null;

    instr.r2 = IEvalContext.CONST_ROLE;
    instr.c2 = new Column(cts.addInt(1)); // add a column with fixed value
    instr.e2 = null;

    // add the instruction to the evaluator
    bEval.addInstr(instr);

    // now that last instruction has been added, compile
    bEval.compile();

    return bEval;
  }

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
  
  /* (non-Javadoc)
  * @see oracle.cep.planmgr.codegen.ExecOptFactory#newExecOpt(oracle.cep.phyplan.PhyOpt)
  */
  @Override
  ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException
  {
    assert ctx != null;
    assert ctx instanceof DistinctContext;
    DistinctContext dctx = (DistinctContext)ctx;
    PhyOpt op = ctx.getPhyopt();
    assert (op != null) && (op.getOperatorKind() == PhyOptKind.PO_DISTINCT);
    setDistinctOp(ctx.getExecContext(), dctx, op);      
    return new Distinct(ctx.getExecContext());
  }

  @Override
  void setupExecOpt(CodeGenContext ctx) throws CEPException
  {
    assert ctx instanceof DistinctContext;
    DistinctContext dctx = (DistinctContext)ctx;
    assert ctx.getExecOpt() instanceof Distinct;
    PhyOpt op = ctx.getPhyopt();
    assert op instanceof PhyOptDistinct;
    
    Distinct distinctOp = (Distinct)ctx.getExecOpt();
    PhyOptDistinct opd  = (PhyOptDistinct)op;
    ExecContext ec = ctx.getExecContext();
    TupleSpec oldSpec = dctx.getOldSpec();
    int countCol = dctx.getCountCol();

    IEvalContext evalContext     = EvalContextFactory.create(ec);
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    ConstTupleSpec constTupSpec = new ConstTupleSpec(factoryMgr);
    
    IAEval outEval = getOutEval(ec, op);
    
    HashIndex outIdx = this.getIndex(ec, evalContext, oldSpec,
                                     IEvalContext.INPUT_ROLE,
                                     ctx.getTupleStorage().getFactory());
    
    IAEval initEval = this.getInitEval(ec, op, constTupSpec, countCol);
   
    assert (op.getStore() != null);
    assert (op.getStore().getStoreKind() == PhyStoreKind.PHY_REL_STORE);
    
    PhySynopsis outSyn = opd.getOutputSyn();
    
    RelationSynopsisImpl relSyn   = null;
    ExecStore            relStore = null;
    
    int relScanId = 0;
    int fullScanId;
    
    assert outSyn != null;
    assert (outSyn.getStwstore() == op.getStore());
    assert outSyn.getKind()  == SynopsisKind.REL_SYN;
    
    ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
    allCtx.setOpt(op);
    allCtx.setObjectType(RelationSynopsisImpl.class.getName());
    relSyn = (RelationSynopsisImpl) ObjectManager.allocate(allCtx);
    outSyn.setSyn(relSyn);
    relStore = ctx.getTupleStorage();
    assert relStore instanceof RelStore;
    relSyn.setStore((RelStore) relStore);
    relSyn.setStubId(relStore.addStub());

    relScanId = relSyn.setIndexScan(null, outIdx);
    fullScanId = relSyn.setFullScan();
    relSyn.setEvalContext(evalContext);
    relSyn.initialize();
        
    distinctOp.setRelSyn(relSyn);
    //unionOp.setRelTupleFactory(relStore.getFactory());
    distinctOp.setInitEval(initEval);
    distinctOp.setRelScanId(relScanId);
    distinctOp.setFullScanID(fullScanId);
      
    // Generate Eval Operator
      
    //count incrementer
    IAEval incrEval = this.getIncrEval(ec, constTupSpec, countCol);
    distinctOp.setIncrEval(incrEval);
      
    //count decrementer
    IAEval decrEval = this.getDecrEval(ec, constTupSpec, countCol);
    distinctOp.setDecrEval(decrEval);
      
    //check count = 1
    IBEval oneEval = this.getOneEval(ec, constTupSpec, countCol);
    distinctOp.setOneEval(oneEval);

    // above evaluators extend the constTupSpec to accommodate
    // some integer values. Using the extended constTupSpec, generate
    // a tuple with all the const values.
    IAllocator ctf = factoryMgr.get(constTupSpec.getTupleSpec());
    ITuplePtr constTuple = (ITuplePtr)ctf.allocate(); //SCRATCH_TUPLE
    constTupSpec.populateTuple(ec, constTuple);

    // bind the constant tuple to the evaluation context
    evalContext.bind(constTuple, IEvalContext.CONST_ROLE);
    
    distinctOp.setEvalContext(evalContext);
    distinctOp.setOutEval(outEval);
    
    //Setup the input stores
    distinctOp.setInputStore(getInputStore(op, ec, 0).getFactory());
    
    boolean isSilentRelns = op.getInputs()[0].isSilentRelnDep();
          
    distinctOp.setSilentRelns(isSilentRelns);
    

    if(isSilentRelns)
    {
      Iterator<PhyOpt> iter = op.getInputs()[0].getSilentRelnDep().iterator();

      while (iter.hasNext())
      {
        PhyOpt opDep = iter.next();
        distinctOp.addInputRelns((RelSource) opDep.getInstOp());
      }
    }
    
    distinctOp.setTupleSpec(((DistinctContext)ctx).getTupSpec());
    
  }
  
  @Override
  protected ExecStore instStore(CodeGenContext ctx) throws CEPException
  {
    assert ctx != null;
    assert ctx instanceof DistinctContext;
    DistinctContext dctx = (DistinctContext)ctx;
    assert ctx.getPhyopt() != null;
    PhyOptDistinct opd = (PhyOptDistinct)ctx.getPhyopt();
    PhySynopsis outSyn = opd.getOutputSyn();
    ExecStore outStore = null;
    
    outStore = StoreInst.instStore(new StoreGenContext(ctx.getExecContext(),
                                   outSyn.getStwstore(), dctx.getTupSpec()));
    ctx.setTupleStorage(outStore);
    return outStore;    
  }
    
  protected TupleSpec getArchiverTupleSpec(CodeGenContext ctx) throws CEPException
  {
    //return the tuple spec with the count column added
    return ((DistinctContext)ctx).getTupSpec();
  }
}
