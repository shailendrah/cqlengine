/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/PartitionWindowFactory.java /main/20 2013/06/26 09:27:40 sbishnoi Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares PartitionWindowFactory in package oracle.cep.planmgr.codegen.

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
    sbishnoi  05/10/13 - bug 16719717
    pkali     05/31/12 - support predicates eval in partnwin
    sbishnoi  12/06/11 - support for variable duration partition window
    anasrini  12/20/10 - remove eval.setEvalContext
    parujain  03/19/09 - stateless server
    sborah    12/16/08 - handle constants
    hopark    10/10/08 - remove statics
    hopark    10/09/08 - remove statics
    hopark    10/07/08 - use execContext to remove statics
    hopark    10/12/07 - use winspec
    rkomurav  09/17/07 - cleanup
    sbishnoi  09/07/07 - add ColRef
    hopark    09/04/07 - eval optimize
    hopark    07/18/07 - fix partition attribute
    najain    04/11/07 - bug fix
    najain    12/04/06 - stores are not storage allocators
    hopark    11/07/06 - bug 5465978 : refactor newExecOpt
    ayalaman  08/02/06 - partition window storage
    ayalaman  07/29/06 - implementations
    najain    06/18/06 - cleanup
    skaluska  02/28/06 - Creation
    skaluska  02/28/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/PartitionWindowFactory.java /main/20 2013/06/26 09:27:40 sbishnoi Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */


package oracle.cep.planmgr.codegen;

import java.util.PriorityQueue;

import oracle.cep.common.TimeUnit;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.comparator.ComparatorSpecs;
import oracle.cep.execution.comparator.TupleComparator;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.ExecException;
import oracle.cep.phyplan.PhyOptPrtnWin;
import oracle.cep.execution.synopses.PartnWindowSynopsisImpl;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.operators.PartitionWindow;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptRngWin;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.phyplan.expr.BoolExpr;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.window.PhyRowRangeWinSpec;
import oracle.cep.service.ExecContext;
import oracle.cep.execution.internals.AInstr;
import oracle.cep.execution.internals.factory.AEvalFactory;
import oracle.cep.execution.internals.factory.BEvalFactory;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.execution.internals.windows.Window;
import oracle.cep.execution.stores.PartnWindowStoreImpl;

/**
 * PartitionWindowFactory - The factory class to create PartitionWindow operator
 * and necessary synopsis and storage.
 *
 */
public class PartitionWindowFactory extends ExecOptFactory
{

    /**
   * Constructor for PartitionWindowFactory
   */
    public PartitionWindowFactory()
    {
      super();
    }

    /* (non-Javadoc)
   * @see oracle.cep.planmgr.codegen.ExecOptFactory#newExecOpt(oracle.cep.phyplan.PhyOpt)
   */
    @Override
    ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException
    {      
      return new PartitionWindow(ctx.getExecContext());
    }

    @Override
    void setupExecOpt(CodeGenContext ctx) throws CEPException
    {
        assert ctx.getExecOpt() instanceof PartitionWindow;
        PhyOpt op = ctx.getPhyopt();
        // assert that the operator is of Partition window type
        assert op instanceof PhyOptPrtnWin;

        ExecContext ec = ctx.getExecContext();
        PartitionWindow partnWin = (PartitionWindow) ctx.getExecOpt();
        PhyOptPrtnWin phyPrtnWin = (PhyOptPrtnWin) op;

        IEvalContext            evalContext = EvalContextFactory.create(ec);
        TupleSpec               tupSpec;
        IAEval                  copyEval; 
        PhySynopsis             phySyn;
        PartnWindowSynopsisImpl pwSyn; 
        PartnWindowStoreImpl    pwinStore;
        int                     pwinStubId;
        
        PhyRowRangeWinSpec winSpec = (PhyRowRangeWinSpec)phyPrtnWin.getWinSpec();
        
        // Instantiate window specification
        Window window = WindowHelper.instantiateWindow(ctx.getExecContext(), phyPrtnWin.getWinSpec());
        partnWin.setWindow(window);
        
        // set the operator's evaluation context
        partnWin.setEvalContext(evalContext);
        
        // Get the tuple spec for this physical operator
        tupSpec = CodeGenHelper.getTupleSpec(ec, op);
        
        // Set the tupleSpec inside the PartitionWindowContext
        ((PartitionWindowContext) ctx).setTupSpec(tupSpec);
        
        // set the tuple copy evaluator
        copyEval = getCopyEval(ec, tupSpec, winSpec.isVariableDurationWindow());
        
        // Get the handle to tuple factory
        FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
        
        // Create the evalContext
        EvalContextInfo evalCtxInfo = new EvalContextInfo(factoryMgr);
        
        // Handle few things if the window is having variable range        
        if(winSpec.isVariableDurationWindow())
        {
          // Set the flag in execution operator
          partnWin.setVariableDurationWindow(true);
          
          // Get the physical range expression
          Expr rangeExpr = winSpec.getRangeExpr();
          
          // Construct the evaluator for range expression    
          int[] inpRoles = new int[1];
          inpRoles[0] = IEvalContext.INPUT_ROLE;
          int expTsRole = IEvalContext.NEW_OUTPUT_ROLE;
          int expTsPos  = phyPrtnWin.getExpTsPos();
          partnWin.setExpTsColumn(expTsPos);
          
          // The last attribute of stream tuple will be element time
          int elementTimePos = expTsPos - 1;
          
          // Value of slide
          long slideAmount = winSpec.getSlideUnits();
          
          // Get units of range
          TimeUnit rangeUnit = winSpec.getRangeUnit();
          
          // Prepare evaluators for visTs and expTs
          getExpTsEval(ec, phyPrtnWin, expTsRole, expTsPos, elementTimePos, 
                rangeExpr, evalCtxInfo, evalContext, inpRoles, slideAmount,
                rangeUnit, factoryMgr, copyEval);
          
          PriorityQueue<ITuplePtr> expiryTimeOrderedElements =
              getPriorityQueue(expTsPos, ctx);
          
          partnWin.setExpiryTimeOrderedElements(expiryTimeOrderedElements);
          partnWin.setSlideAmount(slideAmount);
          partnWin.setElementTimePos(elementTimePos);
        }
            
        // Set the copyEval inside execution operator
        copyEval.compile();
        partnWin.setCopyEval(copyEval);

        // create the partition window synopsis
        phySyn = phyPrtnWin.getSynopsis();

        ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
        allCtx.setOpt(op);
        allCtx.setObjectType(PartnWindowSynopsisImpl.class.getName());
        pwSyn = (PartnWindowSynopsisImpl)ObjectManager.allocate(allCtx);

        pwinStore = (PartnWindowStoreImpl) ctx.getTupleStorage();
        assert (pwinStore != null);

        pwSyn.setStore(pwinStore);
        pwinStubId = pwinStore.addStub();
        pwinStore.setPrimaryStub(pwinStubId);
        pwSyn.setStubId(pwinStubId);

        // set the execution synopsis for the physical synopsis
        phySyn.setSyn(pwSyn);

        // set the synopsis for the operator.
        partnWin.setSynopsis(pwSyn);
        
        // handle the predicates
        BoolExpr[] preds = phyPrtnWin.getPredicate();

        if( preds != null && preds.length > 0)
        {
          IBEval outEval;
          outEval = BEvalFactory.create(ec);
          outEval.setNullEqNull(false);
          
          // Create a BEval for the set of predicates
          for (int i = 0, l = preds.length; i < l; i++)
          {
            int[] inpRoles = new int[1];
            inpRoles[0] = IEvalContext.INPUT_ROLE;
  
            ExprHelper.instBoolExpr(ec, preds[i], outEval, evalCtxInfo, false,
                                    inpRoles);
          }
          outEval.compile();
          TupleSpec st = evalCtxInfo.st;
          if (st != null)
          {
            IAllocator<Object> stf = factoryMgr.get(st);
            ITuplePtr t = (ITuplePtr)stf.allocate();
            evalContext.bind(t, IEvalContext.SCRATCH_ROLE);
          }
          // Constant Tuple
          ConstTupleSpec ct = evalCtxInfo.ct;
          if (ct != null)
          {
            IAllocator<Object> ctf = factoryMgr.get(ct.getTupleSpec());
            ITuplePtr t = (ITuplePtr)ctf.allocate();
            ct.populateTuple(ec, t);
            evalContext.bind(t, IEvalContext.CONST_ROLE);
          }       
          partnWin.setPredicate(outEval);
        }
    }
    
    @Override
    public CodeGenContext createCodeGenContext(ExecContext ec, Query query, 
                                               PhyOpt phyopt)
    {
      PartitionWindowContext ctx 
        = new PartitionWindowContext(ec, query, phyopt);
      
      // Set the flag if the current operator is variable duration window
      ctx.setVariableDurationWindow(
        ((PhyOptPrtnWin)phyopt).isVariableDurationWindow());      
      return ctx;
    }
    
    /**
     * Get the priority queue to store the elements according to the order of
     * their expiry timestamp
     * @param comparableColumnPos
     * @param pctx
     * @return
     */
    private PriorityQueue<ITuplePtr> getPriorityQueue(
      int comparableColumnPos,
      CodeGenContext ctx)
    {
      assert ctx instanceof PartitionWindowContext;
      PartitionWindowContext pctx = (PartitionWindowContext) ctx;
      
      // Instantiate array of ComparatorSpec for visible time priority queue 
      ComparatorSpecs queueComparatorSpecs[] = new ComparatorSpecs[1];
      queueComparatorSpecs[0] 
        = new ComparatorSpecs(comparableColumnPos, 
                              pctx.isNullFirst(), 
                              pctx.isAscending());
      
      // Instantiate and Initialize comparator
      TupleComparator tupleComparator = 
        new TupleComparator(queueComparatorSpecs, pctx.getTupSpec());
    
      // Initialize the priority queue with appropriate comparator
      // Note: Keeping initial size to default 1
      return new PriorityQueue<ITuplePtr>(1, tupleComparator);
    }

    @Override
    protected ExecStore instStore(CodeGenContext ctx) throws CEPException
    {
        assert ctx != null;
        PhyOpt op = ctx.getPhyopt();
        assert op != null;
        PhyOptPrtnWin phyPrtnWin = (PhyOptPrtnWin) op;

        ExecContext ec = ctx.getExecContext();
        PartnWindowStoreImpl pwinStore = instPartnWinStore(ec, phyPrtnWin);
        ctx.setTupleStorage(pwinStore);
        return pwinStore;
    }

    /**
   *  Allocate a store for the partition window operator
     * @param ec TODO
     * @param  phyPrtnWin  partition window operator information
   *
   *  @return an instance of the Partition window store.
   *
   *  @throws  CEPException for any errors caught during allocation.
   */
    private PartnWindowStoreImpl instPartnWinStore(ExecContext ec, PhyOptPrtnWin phyPrtnWin) 
        throws CEPException
    {
        PartnWindowStoreImpl pwStore;
        PhyStore phyStore = phyPrtnWin.getStore();
        int numPartAttrs;
        
        assert phyStore.getStoreKind() == PhyStoreKind.PHY_PARTN_WIN_STORE;

        numPartAttrs = phyPrtnWin.getNumPartnAttrs();
        // the partition window spec is confirmed to have atleast one partition
        // attribute and has fewer than maximum number of attribute allowed
        assert numPartAttrs > 0; 
        assert phyStore != null;

        // create an instance of the partition window store and configure it
        // with the special column positions and a hash index.
        pwStore = (PartnWindowStoreImpl)StoreInst.instStore(
                     new StoreGenContext(
                         ec, phyStore, CodeGenHelper.getTupleSpec(ec, phyPrtnWin),
                         numPartAttrs, phyPrtnWin.getPartnAttrs()));

        return pwStore;
    }

  /**
   * Get the evaluator instance to copy the Input tuple
   * @param ec TODO
   * @param ts  tuple specication for the copy operatir
   *
   * @return
   * @throws ExecException
   */
    private IAEval getCopyEval(ExecContext ec, TupleSpec ts, 
                               boolean isVariableDurationWindow)
      throws ExecException
    {
        AInstr instr;
        IAEval aEval = AEvalFactory.create(ec);
        int numAttrs = ts.getNumAttrs();
        
        // Last column should be added by partition window operator
        // so copy only those attributes which are present in input tuple
        if(isVariableDurationWindow)
          numAttrs--;
        

        // copy the data columns
        for (int attr = 0; attr < numAttrs; attr++)
        {
            instr = new AInstr();

            instr.op = ExprHelper.getCopyOp(ts.getAttrType(attr));
            instr.r1 = IEvalContext.INPUT_ROLE;
            instr.c1 = attr;
            instr.r2 = 0;
            instr.c2 = 0;
            instr.dr = IEvalContext.NEW_OUTPUT_ROLE;
            instr.dc = attr;

            aEval.addInstr(instr);
        }
      
        return aEval;
    }
    
    /**
     * Get Evaluator for range expression
     * @param ec
     * @param rwop
     * @param expTsRole
     * @param expTsPos
     * @param elementTimePos
     * @param rangeExpr
     * @param evalCtxInfo
     * @param evalContext
     * @param inpRoles
     * @param slideAmount
     * @param rangeUnit
     * @param factoryMgr
     * @return
     * @throws CEPException
     */
    private void getExpTsEval(ExecContext ec, 
                                PhyOpt rwop, 
                                int expTsRole,
                                int expTsPos, 
                                int elementTimePos,
                                Expr rangeExpr, 
                                EvalContextInfo evalCtxInfo, 
                                IEvalContext evalContext,
                                int[] inpRoles,
                                long slideAmount,
                                TimeUnit rangeUnit,
                                FactoryManager factoryMgr,
                                IAEval eval)
      throws CEPException
    {
      //Add instructions to evaluate the range expression
      ExprHelper.instExprDest(ec, rangeExpr, eval, evalCtxInfo, 
                              expTsRole, expTsPos, inpRoles);
      
      // Allocate Scratch Tuple and Constant Tuple for expression evaluation
      TupleSpec st = evalCtxInfo.st;
      if(st != null) 
      {
        IAllocator stf = factoryMgr.get(st);
        ITuplePtr t = (ITuplePtr)stf.allocate(); //SCRATCH_TUPLE
        evalContext.bind(t, IEvalContext.SCRATCH_ROLE);
      }
      
      ConstTupleSpec ct = evalCtxInfo.ct;
      if (ct != null) 
      {
        IAllocator ctf = factoryMgr.get(ct.getTupleSpec());
        ITuplePtr  t = (ITuplePtr)ctf.allocate(); 
        ct.populateTuple(ec, t);
        evalContext.bind(t, IEvalContext.CONST_ROLE);
      }
      
      // Step-3 Prepare the instruction to calculate the expiry timestamp
      AInstr instr = new AInstr();
      
      instr.op = ExprHelper.getSumOp(rangeExpr.getType(), true);        
      instr.r1 = IEvalContext.INPUT_ROLE;
      instr.c1 = elementTimePos;        
      instr.r2 = expTsRole;
      instr.c2 = expTsPos;        
      instr.dr = expTsRole;
      instr.dc = expTsPos;
      eval.addInstr(instr);     
      
    } 
}
