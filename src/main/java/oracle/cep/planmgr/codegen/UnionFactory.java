/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/UnionFactory.java /main/13 2011/02/07 03:36:26 sborah Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares UnionFactory in package oracle.cep.planmgr.codegen.

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
 parujain  06/09/08 - xmltype
 hopark    12/07/07 - cleanup spill
 hopark    09/04/07 - eval optimize
 sbishnoi  07/27/07 - adding fullscan for union
 sbishnoi  04/18/07 - override instStore
 sbishnoi  04/06/07 - support for union all
 najain    12/04/06 - stores are not storage allocators
 najain    11/15/06 - bug fix
 hopark    11/09/06 - bug 5465978 : refactor newExecOpt
 najain    08/14/06 - fix union
 parujain  08/07/06 - timestamp datatype
 dlenkov   06/23/06 - fix bugs
 najain    06/18/06 - cleanup
 dlenkov   06/14/06 - Implementation
 skaluska  02/28/06 - Creation
 skaluska  02/28/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/UnionFactory.java st_pcbpel_anasrini_eval_parallelism_2/1 2010/12/20 07:47:46 anasrini Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.codegen;

import java.util.Iterator;

import oracle.cep.common.Datatype;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.RelSource;

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
import oracle.cep.execution.operators.Union;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.LineageStore;
import oracle.cep.execution.stores.RelStore;
import oracle.cep.execution.synopses.LineageSynopsisImpl;
import oracle.cep.execution.synopses.RelationSynopsisImpl;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptUnion;
import oracle.cep.phyplan.PhyOptKind;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;
import oracle.cep.service.ExecContext;

/**
 * UnionFactory
 *
 * @author skaluska
 */
public class UnionFactory extends ExecOptFactory
{
    /**
   * Constructor for UnionFactory
   */
    public UnionFactory()
    {
      super();
    }
    
    @Override
    public CodeGenContext createCodeGenContext(ExecContext ec, Query query, 
                                               PhyOpt phyopt)
    {
       return new UnionContext(ec, query, phyopt); 
    }
    
    public void setUnionOp(ExecContext ec, UnionContext ctx, PhyOpt op)
    throws CEPException
    {
      TupleSpec tupSpec = CodeGenHelper.getTupleSpec(ec, op);

      FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
      TupleSpec oldSpec = new TupleSpec(factoryMgr.getNextId(), tupSpec.getNumAttrs());
      oldSpec.copy(tupSpec);
                
      // Add a count attribute to the original tuple spec
      int countCol = tupSpec.addAttr(Datatype.INT);      
      ctx.setTupSpec(tupSpec);
      ctx.setOldSpec(oldSpec);
      ctx.setCountCol(countCol);
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
    
    /**
     * Get the evaluator instance to increment the value in the count column of
     * the tuple.
     * @param ec TODO
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
    private IAEval getIncrEval(ExecContext ec, ConstTupleSpec cts,
                               int countCol) throws CEPException
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

      // Last instruction added, compile the eval
      aEval.compile();

      return aEval;
    }

    /**
     * Get the evaluator instance to decrement the value in the count column of
     * the tuple.
     * @param ec TODO
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
    private IAEval getDecrEval(ExecContext ec, ConstTupleSpec cts,
                               int countCol) throws CEPException
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
      
      // Last instruction added, compile the eval
      aEval.compile();

      return aEval;
    }

    
    /**
     * Get the evaluator instance to check if the current value stored in the
     * count column is equal to 1.
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
    private IBEval getOneEval(ExecContext ec, ConstTupleSpec cts,
                              int countCol) throws CEPException
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

      // Last instruction added, compile the eval
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
        hInstr = new HInstr(tupSpec.getAttrType(attr),
                            IEvalContext.UPDATE_ROLE,
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
      assert ctx instanceof UnionContext;
      PhyOpt op = ctx.getPhyopt();
      assert ((op != null) && (op.getOperatorKind() == PhyOptKind.PO_UNION));
      
      // create new union operator with 'union all' flag check
      PhyOptUnion uop = (PhyOptUnion) op;
      setUnionOp(ctx.getExecContext(),(UnionContext)ctx, op);
      return new Union(ctx.getExecContext(), uop.isUnionAll());
    }

    @Override
    void setupExecOpt(CodeGenContext ctx) throws CEPException
    {
      assert ctx instanceof UnionContext;
      UnionContext uctx = (UnionContext)ctx;
      assert ctx.getExecOpt() instanceof Union;
      PhyOpt op = ctx.getPhyopt();
      assert op instanceof PhyOptUnion;

      ExecContext ec = ctx.getExecContext();
      Union unionOp   = (Union) ctx.getExecOpt();
      PhyOptUnion uop = (PhyOptUnion) op;

      // Shared evaluation context
      IEvalContext evalContext     = EvalContextFactory.create(ec);
      FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
      ConstTupleSpec constTupSpec = new ConstTupleSpec(factoryMgr);
     
      IAEval outEval = getOutEval(ec, op);
        
      HashIndex outIdx = null;
       
      // Handle Stores and StorageAllocs
      IAEval initEval = null;
      
      if(!uop.isUnionAll())
      {
        outIdx = getIndex(ec, evalContext, uctx.getOldSpec(), 
                          IEvalContext.INPUT_ROLE, 
                          ctx.getTupleStorage().getFactory());
        initEval = getInitEval(ec, op, constTupSpec, uctx.getCountCol());
      }
        
        

      // Check output store
      assert (op.getStore() != null);
      
      assert (((uop.isUnionAll())  && (op.getIsStream())    && (op.getStore().getStoreKind() == PhyStoreKind.PHY_WIN_STORE))
     ||((uop.isUnionAll())  && (!op.getIsStream())  && (op.getStore().getStoreKind() == PhyStoreKind.PHY_LIN_STORE)) 
     ||((!uop.isUnionAll()) && (op.getStore().getStoreKind() == PhyStoreKind.PHY_REL_STORE)));
      

       
      // Construct outer synopsis
      PhySynopsis outSyn = uop.getOutSynopsis();
        
      LineageSynopsisImpl  linSyn   = null;
      RelationSynopsisImpl relSyn   = null;
      
      ExecStore outStore   = null;
      ExecStore relStore   = null;
      
      int relScanId = 0;
      int fullScanId = 0;
        
      if(uop.isUnionAll())
      {
        if(!op.getIsStream())
        {
          assert outSyn != null;
          assert (outSyn.getStwstore() == op.getStore());
          assert outSyn.getKind() == SynopsisKind.LIN_SYN;

          ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
          allCtx.setOpt(op);
          allCtx.setObjectType(LineageSynopsisImpl.class.getName());
          linSyn = (LineageSynopsisImpl) ObjectManager.allocate(allCtx);

          outSyn.setSyn(linSyn);

          outStore = ctx.getTupleStorage();  
          assert (outStore != null);
          assert (outStore instanceof LineageStore);
          linSyn.setStore((LineageStore) outStore);
          linSyn.setStubId(outStore.addStub());
        }
      }
      else  //case for 'union'
      {   
        //work for rel syn
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
        
      }

      if(uop.isUnionAll())
        unionOp.setOutSyn(linSyn);
      else
      {
        unionOp.setRelSyn(relSyn);
        //unionOp.setRelTupleFactory(relStore.getFactory());
        unionOp.setInitEval(initEval);
        unionOp.setRelScanId(relScanId);
        unionOp.setFullScanId(fullScanId);
       
          
        // Generate Eval Operator
          
        //count incrementer
        IAEval incrEval = getIncrEval(ec, constTupSpec, uctx.getCountCol());
        unionOp.setIncrEval(incrEval);
          
        //count decrementer
        IAEval decrEval = getDecrEval(ec, constTupSpec, uctx.getCountCol());
        unionOp.setDecrEval(decrEval);
          
        //check count = 1
        IBEval oneEval = getOneEval(ec, constTupSpec, uctx.getCountCol());
        unionOp.setOneEval(oneEval);

        // above evaluators extend the constTupSpec to accommodate
        // some integer values. Using the extended constTupSpec, generate
        // a tuple with all the const values.
        IAllocator ctf = factoryMgr.get(constTupSpec.getTupleSpec());
        ITuplePtr constTuple = (ITuplePtr)ctf.allocate(); //SCRATCH_TUPLE
        constTupSpec.populateTuple(ec, constTuple);

        // bind the constant tuple to the evaluation context
        evalContext.bind(constTuple, IEvalContext.CONST_ROLE);
      }
        
      unionOp.setEvalContext(evalContext);
      unionOp.setOutEval(outEval);

      // Setup the input queues
      unionOp.setLeftInputQueue(getInputQueue(op, ec, 0));
      unionOp.setRightInputQueue(getInputQueue(op, ec, 1));

      // Setup the input stores
      unionOp.setLeftInputStore(getInputStore(op, ec, 0).getFactory());
      unionOp.setRightInputStore(getInputStore(op ,ec, 1).getFactory());
        
        
      boolean leftSilentRelns = op.getInputs()[0].isSilentRelnDep();
      boolean rightSilentRelns = op.getInputs()[1].isSilentRelnDep();
        
      unionOp.setLeftSilentRelns(leftSilentRelns);
      unionOp.setRightSilentRelns(rightSilentRelns);

      if(leftSilentRelns)
      {
        Iterator<PhyOpt> iter = op.getInputs()[0].getSilentRelnDep().iterator();

        while (iter.hasNext())
        {
          PhyOpt opDep = iter.next();
          unionOp.addLeftInputRelns((RelSource) opDep.getInstOp());
        }
      }

      if(rightSilentRelns)
      {
        Iterator<PhyOpt> iter = op.getInputs()[1].getSilentRelnDep().iterator();

        while(iter.hasNext())
        {
          PhyOpt opDep = iter.next();
          unionOp.addRightInputRelns((RelSource) opDep.getInstOp());
        }
      }
      
    } //end of SetUpExecOpt
    
    
    @Override
    protected ExecStore instStore(CodeGenContext ctx) throws CEPException
    {
      assert ctx != null;
      assert ctx instanceof UnionContext;
      UnionContext uctx = (UnionContext)ctx;
      PhyOpt op = ctx.getPhyopt();
      assert op != null;
      ExecContext ec = ctx.getExecContext();
      PhyOptUnion uop = (PhyOptUnion) op;
      PhySynopsis outSyn = uop.getOutSynopsis();
        
      ExecStore outStore = null;
        
      if(uop.isUnionAll())
      {
        if (op.getStore() != null)
          outStore = StoreInst.instStore(new StoreGenContext(ec, op.getStore()));    
      }
      else
      {
        outStore = StoreInst.instStore(new StoreGenContext(ec, 
                                       outSyn.getStwstore(), 
                                       uctx.getTupSpec()));
      }
      ctx.setTupleStorage(outStore);
      return outStore;
        
    }

}
