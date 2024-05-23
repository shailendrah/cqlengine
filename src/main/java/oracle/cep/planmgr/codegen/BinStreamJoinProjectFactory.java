/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/BinStreamJoinProjectFactory.java /main/30 2011/03/31 18:21:00 alealves Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      01/21/11 - remove eval.setContext
    sbishnoi    03/03/10 - reorg external relation handling
    sborah      12/21/09 - support for multiple external tables in join
    udeshmuk    11/10/09 - ext index followup
    sbishnoi    10/04/09 - table function support
    udeshmuk    10/04/09 - extensible index followup
    udeshmuk    09/14/09 - support extensible indexing
    sborah      03/30/09 - change index init methods
    sborah      03/19/09 - siggen optimization: removing viewstrmsrc
    parujain    03/19/09 - stateless server
    sbishnoi    01/14/09 - fix generic datasource; set predicate clause
    sbishnoi    12/04/08 - support for generic data source
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    hopark      02/05/08 - parameterized error
    hopark      12/07/07 - cleanup spill
    parujain    12/19/07 - inner and outer
    parujain    11/15/07 - external source
    hopark      09/04/07 - eval optimize
    najain      04/11/07 - bug fix
    hopark      04/06/07 - mark static pin for const tupleptr
    hopark      04/05/07 - memmgr reorg
    najain      03/14/07 - cleanup
    hopark      03/06/07 - use ITuplePtr
    parujain    12/21/06 - don't remove predicate from PhyOpt
    najain      12/04/06 - stores are not storage allocators
    hopark      11/14/06 - bug 5505056, turn off null = null
    parujain    11/16/06 - Logical OR Tree
    hopark      11/09/06 - bug 5465978 : refactor newExecOpt
    parujain    11/10/06 - Logical Operators implementation
    parujain    11/02/06 - Base/Complex Boolean Expr
    najain      08/30/06 - bug fix
    parujain    08/11/06 - cleanup planmgr
    parujain    08/07/06 - timstamp datatype
    najain      08/01/06 - handle silent relations
    najain      07/19/06 - ref-count tuples 
    najain      07/19/06 - ref-count tuples 
    najain      07/10/06 - set inputStores 
    najain      07/05/06 - cleanup
    najain      06/29/06 - factory allocation cleanup 
    najain      06/18/06 - cleanup
    najain      06/13/06 - bug fix 
    najain      05/30/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/BinStreamJoinProjectFactory.java /main/28 2010/03/22 08:42:29 sbishnoi Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.codegen;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import oracle.cep.common.Constants;
import oracle.cep.common.OrderingKind;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.indexes.ExtensibleIndexProxy;
import oracle.cep.execution.indexes.HashIndex;
import oracle.cep.execution.indexes.Index;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.factory.AEvalFactory;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.execution.operators.BinStreamJoin;
import oracle.cep.execution.operators.ConcurrentBinStreamJoin;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.RelSource;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.synopses.RelationSynopsisImpl;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptKind;
import oracle.cep.phyplan.PhyOptStrJoinProject;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;
import oracle.cep.phyplan.expr.BaseBoolExpr;
import oracle.cep.phyplan.expr.BoolExpr;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.service.ExecContext;

/**
 * BinStreamJoinProjectFactory
 *
 * @author najain
 */
public class BinStreamJoinProjectFactory extends ExecOptFactory
{

  /**
   * Constructor for BinStreamJoinProjectFactory
   */
  public BinStreamJoinProjectFactory()
  {
    super();
  }
  
  @Override
  public CodeGenContext createCodeGenContext(ExecContext ec, Query query, 
                                             PhyOpt phyopt)
  {
    return new BinStreamJoinProjectContext(ec, query, phyopt); 
  }

  /* (non-Javadoc)
   * @see oracle.cep.planmgr.codegen.ExecOptFactory#newExecOpt
   * (oracle.cep.planmgr.codegen.CodeGenContext)
   */
  @Override
  ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException
  {
    PhyOpt op = ctx.getPhyopt();
    assert ((op != null) &&
            (op.getOperatorKind() == PhyOptKind.PO_STR_JOIN_PROJECT));
    
    if (op.getOrderingConstraint() == OrderingKind.UNORDERED)
      return new ConcurrentBinStreamJoin(ctx.getExecContext());
    else
      return new BinStreamJoin(ctx.getExecContext());
  }

  @SuppressWarnings("unchecked")
  @Override
  void setupExecOpt(CodeGenContext ctx) throws CEPException
  {
    assert ctx instanceof BinStreamJoinProjectContext;
    PhyOpt op = ctx.getPhyopt();
    assert ctx.getExecOpt() instanceof BinStreamJoin;
    assert op instanceof PhyOptStrJoinProject;

    BinStreamJoin strJoin = (BinStreamJoin) ctx.getExecOpt();
    PhyOptStrJoinProject opStrJoinProject = (PhyOptStrJoinProject) op;
    ExecContext ec = ctx.getExecContext();
    
    int inScanId = 0;
    int inFullScanId = -1;
    
    // Shared evaluation context
    IEvalContext evalContext = EvalContextFactory.create(ec);
    
    // Set the position of the ELEMENT_TIME attr
    strJoin.setElemTimePos(op.getNumAttrs() - 1);
    
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    EvalContextInfo evalCtxInfo = new EvalContextInfo(factoryMgr);

    // Join predicate
    LinkedList<BoolExpr> predList = opStrJoinProject.getPreds();
    
    // Setup the input queues
    strJoin.setOuterInputQueue(getInputQueue(op,ec, Constants.OUTER));
    strJoin.setInnerInputQueue(getInputQueue(op,ec, Constants.INNER));

    if(opStrJoinProject.isExternal())
    {
      JoinHelper.processExternalRelation(opStrJoinProject, 
          predList, 
          evalContext, 
          evalCtxInfo, 
          ec, 
          factoryMgr, 
          strJoin, 
          getInputStore(op, ec, Constants.OUTER).getFactory(),
          true,
          strJoin.getOuterInputQueue());
    }
    else
    {
      List<BaseBoolExpr> extensibleIndexPreds = new LinkedList<BaseBoolExpr>();
      List<BoolExpr> hashIndexPreds       = new LinkedList<BoolExpr>();
      List<BoolExpr> nonIndexPreds        = new LinkedList<BoolExpr>();
      List<Integer>  posList              = new LinkedList<Integer>();
      List<Index>    inIndexes            = new LinkedList<Index>();
      
      //split the join predicates into different categories
      JoinHelper.splitPred(predList, extensibleIndexPreds, hashIndexPreds, 
                           nonIndexPreds, posList);
      
      Index inIdx = null;
      boolean needBindToRoleOnRecovery = false;
            
      //Iterate through the extensible index predicates and create extensible
      //indexes wherever possible
      
      //Here INNER_ROLE won't be used
      JoinHelper.createExtensibleIndexes(extensibleIndexPreds, posList, 
                                         nonIndexPreds, inIndexes, ec,
                                         evalContext, evalCtxInfo, 
                                         IEvalContext.OUTER_ROLE, 
                                         IEvalContext.INNER_ROLE);
      
      for(int i=0; i < inIndexes.size(); i++)
      {
        //set the factory
        inIndexes.get(i).setFactory(getInputStore(op, ec, 1).getFactory());
        needBindToRoleOnRecovery = needBindToRoleOnRecovery || 
            (inIndexes.get(i) instanceof ExtensibleIndexProxy);
      }
      
      // Construct an index on inner input for equality predicate attributes
      if(hashIndexPreds.size() != 0)
      {
        // Construct & initialize the indexes
        inIdx = new HashIndex(ec);
        JoinHelper.initHashIndex(ec, op, hashIndexPreds, evalContext, 
                                 (HashIndex)inIdx, Constants.INNER, 
                                 IEvalContext.OUTER_ROLE);
        inIdx.setFactory(getInputStore(op, ec, 1).getFactory());
        ((HashIndex)inIdx).initialize();
        inIndexes.add(inIdx);
      }
      
      IBEval neEval_in = null;
 
      // If there are non-equality (or non-join) predicates, construct an
      // evaluator to check these predicates, while scanning the inner
      // tuples
      
      neEval_in = JoinHelper.getNonIndexPredsEval(nonIndexPreds, ec, 
                                                  IEvalContext.OUTER_ROLE,
                                                  IEvalContext.SCAN_ROLE,
                                                  evalContext, evalCtxInfo);
      
      if(neEval_in != null)
        neEval_in.compile();
      // Construct Inner Synopsis
      PhySynopsis p_inSyn = opStrJoinProject.getInnerSyn();
      assert p_inSyn != null;
      assert p_inSyn.getKind() == SynopsisKind.REL_SYN : p_inSyn.getKind();
      ExecStore innerStore = p_inSyn.getStwstore().getInstStore();
      RelationSynopsisImpl e_inSyn = null;
      
      e_inSyn  = JoinHelper.createSynopsis(ec, op, p_inSyn);
      inFullScanId = e_inSyn.setFullScan();
      inScanId = JoinHelper.setUpSynopsis(e_inSyn, neEval_in, evalContext,
                                          innerStore, inIndexes);
      
      strJoin.setInnerSyn(e_inSyn);
      strJoin.setInnerScanId(inScanId);    
      strJoin.setInnerFullScanId(inFullScanId);
      strJoin.setNeedBindToRoleOnRecovery(needBindToRoleOnRecovery);
    }
      
    
    IAEval outEval = getOutEval(ec, opStrJoinProject, evalCtxInfo);

    strJoin.setEvalContext(evalContext);

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

    // Output store
    assert (op.getStore() != null);
    assert op.getStore().getStoreKind() == PhyStoreKind.PHY_WIN_STORE;

    strJoin.setOutputConstructor(outEval);

   

    boolean outerSilentRelns =
      op.getInputs()[Constants.OUTER].isSilentRelnDep();
    boolean innerSilentRelns =
      op.getInputs()[Constants.INNER].isSilentRelnDep();

    strJoin.setOuterSilentRelns(outerSilentRelns);
    strJoin.setInnerSilentRelns(innerSilentRelns);

    if (outerSilentRelns)
    {
      Iterator<PhyOpt> iter =
        op.getInputs()[Constants.OUTER].getSilentRelnDep().iterator();

      while (iter.hasNext())
      {
        PhyOpt opDep = iter.next();
        strJoin.addOuterInputRelns((RelSource) opDep.getInstOp());
      }
    }

    if (innerSilentRelns)
    {
      Iterator<PhyOpt> iter = 
        op.getInputs()[Constants.INNER].getSilentRelnDep().iterator();

      while (iter.hasNext())
      {
        PhyOpt opDep = iter.next();
        strJoin.addInnerInputRelns((RelSource) opDep.getInstOp());
      }
    }
        
    if(!opStrJoinProject.isExternal())
    {
      // Setup the input stores
      strJoin.setOuterTupleStorageAlloc(
        getInputStore(op, ec, Constants.OUTER).getFactory());
      strJoin.setInnerTupleStorageAlloc(
        getInputStore(op, ec, Constants.INNER).getFactory());
    }
  }
    
  private IAEval getOutEval(ExecContext ec, 
                            PhyOptStrJoinProject opStrJoinProject, 
                            EvalContextInfo evalCtxInfo)
                            throws CEPException
  {
    IAEval outEval = AEvalFactory.create(ec);

    Expr[] projs = opStrJoinProject.getProjs();
    int[] inpRoles = new int[2];

    inpRoles[0] = IEvalContext.OUTER_ROLE;
    inpRoles[1] = IEvalContext.INNER_ROLE;
    
    for (int i = 0; i < projs.length; i++)
      ExprHelper.instExprDest(ec, projs[i], outEval, evalCtxInfo, 
                              IEvalContext.NEW_OUTPUT_ROLE, i, inpRoles);

    // Now that last instruction has been added, compile
    outEval.compile();

    return outEval;
  }
}
