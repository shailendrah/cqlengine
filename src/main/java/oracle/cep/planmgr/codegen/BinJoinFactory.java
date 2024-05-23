/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/BinJoinFactory.java /main/38 2012/10/22 14:42:18 vikshukl Exp $ */
/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares BinJoinFactory in package oracle.cep.planmgr.codegen.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 vikshukl  10/04/12 - denote which side is based in archived dimension
 vikshukl  10/04/12 - don't allocate fact syn and output syn when joining with
                      a slow dimension
 sborah    01/21/11 - remove eval.setContext
 sbishnoi  02/25/10 - reorg external predicate evaluation
 sborah    12/21/09 - support for multiple external tables in join
 udeshmuk  11/10/09 - ext index followup
 sborah    10/14/09 - support for bigdecimal
 sbishnoi  09/30/09 - table function support
 sbishnoi  09/30/09 - table function support
 udeshmuk  09/28/09 - extensible indexing support
 sbishnoi  05/27/09 - ansi syntax support for outer join
 sborah    03/30/09 - remove warnings
 parujain  03/19/09 - stateless server
 parujain  03/02/09 - outer join for external relations
 sbishnoi  01/14/09 - fix generic datasource; set Predicate Clause
 sbishnoi  12/07/08 - support for generic data sources
 sbishnoi  12/04/08 - support for generic data source
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 hopark    10/07/08 - use execContext to remove statics
 sborah    10/17/08 - changing numLeftCols and numRightCols
 sborah    10/06/08 - changing index init methods
 hopark    02/05/08 - parameterized error
 hopark    12/07/07 - cleanup spill
 parujain  12/19/07 - inner and outer
 parujain  12/13/07 - external relation
 hopark    09/04/07 - eval optimize
 hopark    06/20/07 - cleanup
 hopark    05/08/07 - ITuple api cleanup
 najain    04/11/07 - bug fix
 hopark    03/22/07 - mark static pin for const tupleptr
 hopark    03/22/07 - memmgr reorg
 najain    03/14/07 - cleanup
 hopark    03/06/07 - use ITuplePtr
 rkomurav  01/16/07 - add bigInt support
 parujain  12/21/06 - don't remove predicate from PhyOpt
 najain    12/04/06 - stores are not storage allocators
 parujain  11/16/06 - Logical OR Tree
 rkomurav  11/15/06 - support outer join
 hopark    11/14/06 - bug 5505056, turn off null = null
 hopark    11/13/06 - bug 5465978 : refactor newExecOpt
 parujain  11/10/06 - Logical Operators implementation
 parujain  11/02/06 - Base/Complex Boolean Expr
 parujain  08/11/06 - cleanup planmgr
 parujain  08/07/06 - timestamp datatype
 najain    08/01/06 - handle silent relations
 najain    07/19/06 - ref-count tuples 
 najain    07/19/06 - ref-count tuples 
 najain    07/10/06 - set inputStores 
 najain    07/05/06 - cleanup
 najain    06/29/06 - factory allocation cleanup 
 najain    06/18/06 - cleanup
 anasrini  06/03/06 - do not get stubId from physical synopsis 
 najain    05/06/06 - bug fix 
 najain    05/04/06 - sharing support 
 najain    04/20/06 - bug fixes 
 najain    04/03/06 - implementation
 skaluska  02/28/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/BinJoinFactory.java /main/38 2012/10/22 14:42:18 vikshukl Exp $
 *  @author  najain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.codegen;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import oracle.cep.common.Constants;
import oracle.cep.common.OuterJoinType;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.indexes.ExtensibleIndexProxy;
import oracle.cep.execution.indexes.HashIndex;
import oracle.cep.execution.indexes.Index;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.AInstr;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.factory.AEvalFactory;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.execution.operators.BinJoin;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.RelSource;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.LineageStore;
import oracle.cep.execution.synopses.LineageSynopsisImpl;
import oracle.cep.execution.synopses.RelationSynopsisImpl;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptJoin;
import oracle.cep.phyplan.PhyOptKind;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;
import oracle.cep.phyplan.expr.BoolExpr;
import oracle.cep.phyplan.expr.BaseBoolExpr;
import oracle.cep.service.ExecContext;

/**
 * BinJoinFactory
 *
 * @author skaluska
 */
public class BinJoinFactory extends ExecOptFactory
{

  /**
   * Constructor for BinJoinFactory
   */
  public BinJoinFactory()
  {
    super();
  }
  
  @Override
  public CodeGenContext createCodeGenContext(ExecContext ec, Query query, 
                                             PhyOpt phyopt)
  {
    return new BinJoinContext(ec, query, phyopt); 
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.planmgr.codegen.ExecOptFactory#newExecOpt
   * (oracle.cep.phyplan.PhyOpt)
   */
  @Override
  ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException
  {
    assert ((ctx.getPhyopt() != null) && 
    		(ctx.getPhyopt().getOperatorKind() == PhyOptKind.PO_JOIN));

        return new BinJoin(ctx.getExecContext());
    }

  @Override
  void setupExecOpt(CodeGenContext ctx) throws CEPException
  {
    assert ctx instanceof BinJoinContext;
    assert ctx.getExecOpt() instanceof BinJoin;
    assert ctx.getPhyopt() instanceof PhyOptJoin;

    BinJoin join = (BinJoin) ctx.getExecOpt();
    PhyOptJoin opJoin = (PhyOptJoin) ctx.getPhyopt();
    ExecContext ec = ctx.getExecContext();
    BinJoinContext binCtx = (BinJoinContext)ctx;
    
    int inScanId = 0;
    int outScanId = 0;
    int inFullScanId = -1;
    int outFullScanId = -1;
    IAllocator ialloc = null;
        
    binCtx.setNumLeftCols(opJoin.getNumOuterAttrs());
    
    PhyOpt leftOpt = opJoin.getInputs()[Constants.OUTER];
    PhyOpt rightOpt = opJoin.getInputs()[Constants.INNER];
    
    // Shared evaluation context
    IEvalContext evalContext = EvalContextFactory.create(ec);

    // Join predicate
    LinkedList<BoolExpr> pred = opJoin.getPreds();
    
    // Set outerJoinType
    binCtx.setOuterJoinType(opJoin.getOuterJoinType());
    binCtx.setANSIOuterJoin(opJoin.getOuterJoinType() != null);
    
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    EvalContextInfo evalCtxInfo = new EvalContextInfo(factoryMgr);
    
    // Setup the input queues
    join.setOuterInputQueue(getInputQueue(binCtx.getPhyopt(), ec, 
                            Constants.OUTER));
    join.setInnerInputQueue(getInputQueue(binCtx.getPhyopt(), ec, 
                            Constants.INNER));
    
    // if this join is dependent on archived dimension
    // note which input operator works off archived dimension 
    // It is possible that both right and left are dependent
    // on archived dimension. This happens in multiway star join,
    // as in A x B, A x C, A x D. The plan here will be
    // ((A x B) x C) x D
    if (opJoin.isArchivedDim())
    {
      if (leftOpt.isArchivedDim() &&
          !(leftOpt instanceof PhyOptJoin))
      {
        // that is becauase join could be between fact and dimension
        join.setArchivedDim(Constants.OUTER);
      }        
      else if (rightOpt.isArchivedDim() &&
          !(rightOpt instanceof PhyOptJoin))
      {
        // that is becauase join could be between fact and dimension 
        join.setArchivedDim(Constants.INNER);
      }
    }
    
    if(opJoin.isExternal())
    {
      if(binCtx.isANSIOuterJoin())
      {
        // OuterJoinType is already set
        assert binCtx.getOuterJoinType() != null;
        assert binCtx.getOuterJoinType() == OuterJoinType.LEFT_OUTER;
      }
      else
      {
        // Note: This check is valid for the case when OuterJoinType will be
        // decided by join predicate (using + operator)
        if(pred.size() > 0) {
          if(pred.getFirst().getKind().equals(ExprKind.BASE_BOOL_EXPR))
          {
            BaseBoolExpr bBoolExpr = (BaseBoolExpr)pred.getFirst();
            OuterJoinType outerJoinType = bBoolExpr.getOuterJoinType();
            if(outerJoinType != null)
            {
              assert(pred.size() == 1);
              // External relation is always INNER and Stream is OUTER.
              binCtx.setOuterJoinType(OuterJoinType.LEFT_OUTER);
            }
          }
        }
      }
      
      JoinHelper.processExternalRelation(opJoin, 
          pred, 
          evalContext, 
          evalCtxInfo, 
          ec, 
          factoryMgr, 
          join,
          getInputStore(opJoin, ec, Constants.OUTER).getFactory(),
          false,
          join.getOuterInputQueue());      
    }
    else
    {
      //As of now outer join is supported only for single predicate.
      //an exception is however thrown at semantic level if violated.
      if(binCtx.isANSIOuterJoin())
      {
        // Outer join type is already set above
        assert binCtx.getOuterJoinType() != null;
      }
      else
      {
        if(pred.size() > 0) {
          if(pred.getFirst().getKind().equals(ExprKind.BASE_BOOL_EXPR)) {
            BaseBoolExpr bBoolExpr = (BaseBoolExpr)pred.getFirst();
            OuterJoinType outerJoinType = bBoolExpr.getOuterJoinType();
            if(outerJoinType != null)
             assert(pred.size() == 1);
            binCtx.setOuterJoinType(outerJoinType);
          }
        }
      }
      
      List<BaseBoolExpr> leftExtenPreds  = new LinkedList<BaseBoolExpr>();
      List<BaseBoolExpr> rightExtenPreds = new LinkedList<BaseBoolExpr>();
      List<BoolExpr> hashIndexPreds  = new LinkedList<BoolExpr>();
      List<BoolExpr> nonIndexPreds   = new LinkedList<BoolExpr>();
      List<BoolExpr> nonIndexLeftPreds  = new LinkedList<BoolExpr>();
      List<BoolExpr> nonIndexRightPreds = new LinkedList<BoolExpr>();
      List<Integer>  leftPosList  = new LinkedList<Integer>();
      List<Integer>  rightPosList = new LinkedList<Integer>();
       
      // Split the join pred
      JoinHelper.splitPred(pred, leftExtenPreds, rightExtenPreds,
                           hashIndexPreds, nonIndexPreds,
                           leftPosList, rightPosList, binCtx);

      LinkedList<Index> outIndexes = new LinkedList<Index>();
      LinkedList<Index> inIndexes  = new LinkedList<Index>();
     
      Index outIdx = null;
      Index inIdx  = null;
      boolean needBindToRoleOnRecovery = false; 
      
      nonIndexLeftPreds.addAll(nonIndexPreds);
      nonIndexLeftPreds.addAll(rightExtenPreds);
      nonIndexRightPreds.addAll(nonIndexPreds);
      nonIndexRightPreds.addAll(leftExtenPreds);
      nonIndexPreds.clear();

      //Create extensible indexes if possible
      //Here OUTER_ROLE won't be used
      JoinHelper.createExtensibleIndexes(leftExtenPreds, leftPosList, 
                                         nonIndexLeftPreds, outIndexes, ec,
                                         evalContext, evalCtxInfo,
                                         IEvalContext.OUTER_ROLE, 
                                         IEvalContext.INNER_ROLE
                                         );

      for(int i=0; i < outIndexes.size(); i++)
      {
        //set the factory
        outIndexes.get(i).setFactory(getInputStore(opJoin, ec, 0).getFactory());
        needBindToRoleOnRecovery = needBindToRoleOnRecovery || 
            (outIndexes.get(i) instanceof ExtensibleIndexProxy); 
      }
      
      //Here INNER_ROLE won't be used
      JoinHelper.createExtensibleIndexes(rightExtenPreds, rightPosList, 
                                         nonIndexRightPreds, inIndexes, ec, 
                                         evalContext, evalCtxInfo, 
                                         IEvalContext.OUTER_ROLE, 
                                         IEvalContext.INNER_ROLE);      
      
      for(int i=0; i < inIndexes.size(); i++)
      {
        //set the factory
        inIndexes.get(i).setFactory(getInputStore(opJoin, ec, 1).getFactory());
        needBindToRoleOnRecovery = needBindToRoleOnRecovery || 
            (inIndexes.get(i) instanceof ExtensibleIndexProxy); 
      }

      // Construct indexes on both inputs for equality predicate attributes
      if(hashIndexPreds.size() != 0)
      {
        // Construct & initialize the indexes
      
        outIdx = new HashIndex(ec);
        JoinHelper.initHashIndex(ec, opJoin, hashIndexPreds, evalContext, 
                                 (HashIndex)outIdx, Constants.OUTER, 
                                 IEvalContext.INNER_ROLE);
        outIdx.setFactory(getInputStore(opJoin, ec, 0).getFactory());
        ((HashIndex)outIdx).initialize();
        outIndexes.add(outIdx);
        
        inIdx = new HashIndex(ec);
        JoinHelper.initHashIndex(ec, opJoin, hashIndexPreds, evalContext, 
                                 (HashIndex)inIdx, Constants.INNER,
                                 IEvalContext.OUTER_ROLE);
        inIdx.setFactory(getInputStore(opJoin, ec, 1).getFactory());
        ((HashIndex)inIdx).initialize();
        inIndexes.add(inIdx);
      }
      
      IBEval neEval_out = null;
      IBEval neEval_in = null;
      
      neEval_out = JoinHelper.getNonIndexPredsEval(nonIndexLeftPreds, ec,
                                                   IEvalContext.SCAN_ROLE,
                                                   IEvalContext.INNER_ROLE,
                                                   evalContext, evalCtxInfo);
      
      neEval_in  = JoinHelper.getNonIndexPredsEval(nonIndexRightPreds, ec, 
                                                   IEvalContext.OUTER_ROLE,
                                                   IEvalContext.SCAN_ROLE,
                                                   evalContext, evalCtxInfo); 
         
      if(neEval_out != null)
        neEval_out.compile();
      if(neEval_in != null)
       neEval_in.compile();
      
      // Construct outer synopsis
      PhySynopsis p_outSyn = opJoin.getOuterSyn();
      assert p_outSyn != null;
      assert p_outSyn.getKind() == SynopsisKind.REL_SYN : p_outSyn.getKind();
      ExecStore outerStore = p_outSyn.getStwstore().getInstStore();
      RelationSynopsisImpl e_outSyn = null;
      
      e_outSyn  = JoinHelper.createSynopsis(ec, opJoin, p_outSyn);
      outFullScanId = e_outSyn.setFullScan();
      outScanId = JoinHelper.setUpSynopsis(e_outSyn, neEval_out, evalContext,
                                           outerStore, outIndexes);
      
      // Construct Inner Synopsis
      PhySynopsis p_inSyn = opJoin.getInnerSyn();
      assert p_inSyn != null;
      assert p_inSyn.getKind() == SynopsisKind.REL_SYN : p_outSyn.getKind();
      ExecStore innerStore = p_inSyn.getStwstore().getInstStore();
      RelationSynopsisImpl e_inSyn = null;
      
      e_inSyn  = JoinHelper.createSynopsis(ec, opJoin, p_inSyn);
      inFullScanId = e_inSyn.setFullScan();
      inScanId = JoinHelper.setUpSynopsis(e_inSyn, neEval_in, evalContext,
                                          innerStore, inIndexes);
      
      join.setOuterSyn(e_outSyn);
      join.setInnerSyn(e_inSyn);
      join.setNeedBindToRoleOnRecovery(needBindToRoleOnRecovery);
    }
    
    IAEval outEval = getSimpleOutEval(binCtx, ec, opJoin);
    
    ITuplePtr nullLeftTuple  = JoinHelper.getNullTuple(ec, leftOpt);
    ITuplePtr nullRightTuple = JoinHelper.getNullTuple(ec, rightOpt);
    
    // Scratch Tuple
    TupleSpec st = evalCtxInfo.st;

    if (st != null)
    {
      IAllocator stf = factoryMgr.get(st);
      ITuplePtr tPtr = (ITuplePtr)stf.allocate(); //SCRATCH_TUPLE
      evalContext.bind(tPtr, IEvalContext.SCRATCH_ROLE);
    }

    // Constant Tuple
    ConstTupleSpec ct = evalCtxInfo.ct;
    if (ct != null)
    {
      IAllocator ctf = factoryMgr.get(ct.getTupleSpec());
      ITuplePtr tPtr = (ITuplePtr)ctf.allocate(); //SCRATCH_TUPLE
      ct.populateTuple(ec, tPtr);
      evalContext.bind(tPtr, IEvalContext.CONST_ROLE);
    }

    // (Optional) output synopsis
    PhySynopsis p_joinSyn = opJoin.getJoinSyn();

    LineageSynopsisImpl e_joinSyn = null;

    if (p_joinSyn != null)
    {
      assert opJoin.getIsStream() == false;
      assert (p_joinSyn.getStwstore() == opJoin.getStore());
      assert (p_joinSyn.getKind() == SynopsisKind.LIN_SYN);
      
      ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
      allCtx.setOpt(opJoin);
      allCtx.setObjectType(LineageSynopsisImpl.class.getName());
      e_joinSyn = (LineageSynopsisImpl) ObjectManager.allocate(allCtx);
      
      p_joinSyn.setSyn(e_joinSyn);
      ExecStore outStore = ctx.getTupleStorage();
      assert (outStore != null);
      assert (outStore instanceof LineageStore);
      e_joinSyn.setStore((LineageStore) outStore);
      e_joinSyn.setStubId(outStore.addStub());
    }

    join.setJoinSyn(e_joinSyn);
    join.setOuterScanId(outScanId);
    join.setInnerScanId(inScanId);
    join.setInnerFullScanId(inFullScanId);
    join.setOuterFullScanId(outFullScanId);
    join.setEvalContext(evalContext);
    join.setOutputConstructor(outEval);
    join.setNullLefTuple(nullLeftTuple);
    join.setNullRightTuple(nullRightTuple);
    
    //Set the join type
    join.setOuterJoinType(binCtx.getOuterJoinType());
   

    boolean outerSilentRelns =
      opJoin.getInputs()[Constants.OUTER].isSilentRelnDep();
    boolean innerSilentRelns = 
      opJoin.getInputs()[Constants.INNER].isSilentRelnDep();

    join.setOuterSilentRelns(outerSilentRelns);
    join.setInnerSilentRelns(innerSilentRelns);

    if (outerSilentRelns)
    {
      Iterator<PhyOpt> iter =
        opJoin.getInputs()[Constants.OUTER].getSilentRelnDep().iterator();

      while (iter.hasNext())
      {
        PhyOpt opDep = iter.next();
        join.addOuterInputRelns((RelSource) opDep.getInstOp());
      }
    }

    if (innerSilentRelns)
    {
      Iterator<PhyOpt> iter = 
        opJoin.getInputs()[Constants.INNER].getSilentRelnDep().iterator();

      while (iter.hasNext())
      {
        PhyOpt opDep = iter.next();
        join.addInnerInputRelns((RelSource) opDep.getInstOp());
      }
    }
   
    if(!opJoin.isExternal())
    {

      // Setup the input stores
      join.setOuterTupleStorageAlloc(getInputStore(opJoin, ec, 0).getFactory());
      join.setInnerTupleStorageAlloc(getInputStore(opJoin, ec, 1).getFactory());
    }
  }

    
  @Override
  protected ExecStore instStore(CodeGenContext ctx) throws CEPException
  {
    assert ctx != null;
    assert ctx.getPhyopt() != null;

    PhyOpt op = ctx.getPhyopt();
    PhyOptJoin opJoin = (PhyOptJoin) ctx.getPhyopt();
    PhySynopsis p_joinSyn = opJoin.getJoinSyn();

    assert (op.getStore() != null);
    assert (((op.getIsStream() == true) && 
             (op.getStore().getStoreKind() == PhyStoreKind.PHY_WIN_STORE)) || 
            ((op.getIsStream() == false) && 
             (op.getStore().getStoreKind() == PhyStoreKind.PHY_LIN_STORE)));

    ExecStore outStore = null;
    if (p_joinSyn != null)
    {
      assert op.getIsStream() == false;
      assert (p_joinSyn.getStwstore() == op.getStore());
      assert (p_joinSyn.getKind() == SynopsisKind.LIN_SYN);

      outStore = StoreInst.instStore(
        new StoreGenContext(ctx.getExecContext(), p_joinSyn.getStwstore()));
    } 
    else
    {
      outStore = StoreInst.instStore(
        new StoreGenContext(ctx.getExecContext(), op.getStore()));
    }
    
    ctx.setTupleStorage(outStore);
    return outStore;
  }

  private IAEval getSimpleOutEval(BinJoinContext binCtx, ExecContext ec, 
                                  PhyOptJoin opJoin) throws ExecException
  {
    IAEval outEval = AEvalFactory.create(ec);

    // Copy the attributes of the left input
    for (int a = 0; a < opJoin.getNumOuterAttrs(); a++)
    {
      AInstr instr = new AInstr();
      
      // Operation: copy
      instr.op = ExprHelper.getCopyOp(opJoin.getAttrTypes(a));

      // Source: a'th column of left
      instr.r1 = IEvalContext.OUTER_ROLE;
      instr.c1 = a;

      // Destn: a'th column of output
      instr.dr = IEvalContext.NEW_OUTPUT_ROLE;
      instr.dc = a;

      outEval.addInstr(instr);
    }

    // Copy the attributes of the right input
    for (int a = 0; a < opJoin.getNumInnerAttrs(); a++)
    {
      AInstr instr = new AInstr();

      // Operation: copy
      instr.op = ExprHelper.getCopyOp(
        opJoin.getAttrTypes(a + binCtx.getNumLeftCols()));
      
      // Source: a'th column of right
      instr.r1 = IEvalContext.INNER_ROLE;
      instr.c1 = a;

      // Destn: (a + numLeftAttr) column of output
      instr.dr = IEvalContext.NEW_OUTPUT_ROLE;
      instr.dc = a + binCtx.getNumLeftCols();

      outEval.addInstr(instr);
    }
    
    // Now that last instruction has been added, compile
    outEval.compile();

    return outEval;
  }

}
