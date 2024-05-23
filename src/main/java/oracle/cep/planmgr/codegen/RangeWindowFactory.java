/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/RangeWindowFactory.java /main/9 2011/10/03 01:51:59 sbishnoi Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
  Factory for the Range Window Execution operator

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
    sbishnoi  10/01/11 - XbranchMerge sbishnoi_bug-12720971_ps5 from
                         st_pcbpel_11.1.1.4.0
    sbishnoi  03/16/11 - support for variable duration in range window
    sbishnoi  09/13/11 - silent relation cleanup
    parujain  03/19/09 - stateless server
    hopark    10/10/08 - remove statics
    hopark    10/09/08 - remove statics
    hopark    10/07/08 - use execContext to remove statics
    hopark    02/21/08 - stored WinStore
    parujain  03/23/07 - cleanup
    parujain  03/08/07 - Extensible Window support
    najain    12/04/06 - stores are not storage allocators
    hopark    11/07/06 - bug 5465978 : refactor newExecOpt
    najain    09/25/06 - static relations with slide
    najain    07/19/06 - ref-count tuples
    najain    07/05/06 - cleanup
    najain    06/29/06 - factory allocation cleanup
    najain    06/18/06 - cleanup
    najain    05/04/06 - sharing support
    skaluska  04/04/06 - add tsStorageAlloc
    anasrini  03/24/06 - set elemStorealloc
    anasrini  03/20/06 - fix up stores related
    anasrini  03/16/06 - process queues
    anasrini  03/13/06 - implementation
    skaluska  02/28/06 - Creation
    skaluska  02/28/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/RangeWindowFactory.java /main/7 2009/03/30 14:46:02 parujain Exp $
 *  @author  skaluska
 *  @since   1.0
 */


package oracle.cep.planmgr.codegen;

import oracle.cep.common.TimeUnit;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.comparator.ComparatorSpecs;
import oracle.cep.execution.comparator.TupleComparator;
import oracle.cep.execution.internals.AInstr;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.factory.AEvalFactory;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.execution.internals.windows.Window;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.RangeWindow;
import oracle.cep.execution.operators.VariableRangeWindow;
import oracle.cep.execution.synopses.LineageSynopsisImpl;
import oracle.cep.execution.synopses.WindowSynopsisImpl;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.LineageStore;
import oracle.cep.execution.stores.WinStore;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptRngWin;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.window.PhyRngWinSpec;
import oracle.cep.service.ExecContext;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.operators.RelSource;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * Factory for the Range Window Execution operator
 *
 * @author skaluska
 * @since 1.0
 */
class RangeWindowFactory extends ExecOptFactory {

    /* (non-Javadoc)
   * @see oracle.cep.planmgr.codegen.ExecOptFactory#newExecOpt(oracle.cep.phyplan.PhyOpt)
   */
    @Override
    ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException 
    {
        // Create the execution operator
      RangeWindowContext rctx = (RangeWindowContext)ctx;
      
      //Note: Construct RangeWindow execution operator if it is constant range
      // else Construct VariableRangeWindow execution operator
      if(rctx.isVariableDurationWindow())
      {
        PhyOptRngWin op = (PhyOptRngWin)ctx.getPhyopt();        
        TupleSpec tupSpec 
        = CodeGenHelper.getTupleSpec(ctx.getExecContext(), op);        
        rctx.setTupSpec(tupSpec);
        
        return new VariableRangeWindow(rctx.getExecContext());
      }
      else
        return new RangeWindow(ctx.getExecContext());
    }

    @Override
    void setupExecOpt(CodeGenContext ctx) throws CEPException
    {
      PhyOpt op = ctx.getPhyopt();
      assert ctx.getExecOpt() instanceof RangeWindow ||
             ctx.getExecOpt() instanceof VariableRangeWindow;      
      assert op instanceof PhyOptRngWin;

      
      ExecContext  ec = ctx.getExecContext();
      PhyOptRngWin rwop = (PhyOptRngWin) op;
      PhySynopsis  syn;
      
      if(rwop.isVariableDurationWindow())
      {
        VariableRangeWindow rwExecOp = (VariableRangeWindow) ctx.getExecOpt();        
        
        PhyRngWinSpec winSpec = (PhyRngWinSpec)rwop.getWinSpec();
        
        Expr rangeExpr     = winSpec.getRangeExpr();        
        
        PhySynopsis outSyn = rwop.getOutputSyn();
        
        LineageSynopsisImpl  linSyn   = null;
        ExecStore            linStore = null;
        
        assert outSyn != null;
        assert (outSyn.getStwstore() == op.getStore());
        assert outSyn.getKind()  == SynopsisKind.LIN_SYN;
        
        // Initialize the execution synopsis and store
        ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
        allCtx.setOpt(op);
        allCtx.setObjectType(LineageSynopsisImpl.class.getName());
        linSyn = (LineageSynopsisImpl) ObjectManager.allocate(allCtx);
        outSyn.setSyn(linSyn);
        linStore = ctx.getTupleStorage();
        assert linStore instanceof LineageStore;
        linSyn.setStore((LineageStore) linStore);
        linSyn.setStubId(linStore.addStub());
        
        IEvalContext evalContext  = EvalContextFactory.create(ec);             
        FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
        EvalContextInfo evalCtxInfo  = new EvalContextInfo(factoryMgr);
        int[] inpRoles = new int[1];
        inpRoles[0] = IEvalContext.INPUT_ROLE;
        
        //Note: Tuple will have one extra column
        // 1) ExpTs: Column having value of timestamp when this tuple should be
        //           expired
        
        int expTsRole = IEvalContext.NEW_OUTPUT_ROLE;
        int expTsPos  = rwop.getExpTsPos();
        
        // The last attribute of stream tuple will be element time
        int elementTimePos = expTsPos - 1;
        
        // Value of slide
        long slideAmount = winSpec.getSlideUnits();
        
        // Get units of range
        TimeUnit rangeUnit = winSpec.getRangeUnit();
        
        // Prepare evaluators for visTs and expTs
        IAEval expTsEval 
          = getExpTsEval(ec, rwop, expTsRole, expTsPos, elementTimePos, 
              rangeExpr, evalCtxInfo, evalContext, inpRoles, slideAmount,
              rangeUnit, factoryMgr);
        
        expTsEval.compile();
        
        
        RangeWindowContext rctx = (RangeWindowContext)ctx;
        
        PriorityQueue<ITuplePtr> expiryTimeOrderedElements =
          getPriorityQueue(expTsPos, rctx);
        
        LinkedList<ITuplePtr> pendingElements = new LinkedList<ITuplePtr>();
                
        // Set the instance variables in execution operator
        rwExecOp.setOutSynopsis(linSyn);        
        rwExecOp.setExpTsEval(expTsEval);
        rwExecOp.setExpTsPos(expTsPos);
        rwExecOp.setEvalContext(evalContext);
        rwExecOp.setElementTimePos(elementTimePos);
        rwExecOp.setExpiryTimeOrderedElements(expiryTimeOrderedElements);
        rwExecOp.setPendingElements(pendingElements);
        rwExecOp.setSlideAmount(slideAmount);
      }
      else
      {
        RangeWindow rwExecOp = (RangeWindow) ctx.getExecOpt();
        WindowSynopsisImpl winsyn;
        ExecStore          inStore; 
        WinStore           wStore;
          
        // Instantiate the window synopsis
        syn = rwop.getWinSyn();

        ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
        allCtx.setOpt(op);
        allCtx.setObjectType(WindowSynopsisImpl.class.getName());
        winsyn = (WindowSynopsisImpl)ObjectManager.allocate(allCtx);

        inStore = ctx.getTupleStorage();
        assert (inStore != null);
        assert (inStore instanceof WinStore);
        wStore = (WinStore) inStore;
        winsyn.setStore(wStore);
        int id = inStore.addStub();
        winsyn.setStubId(id);

        syn.setSyn(winsyn);
        rwExecOp.setWinSynopsis(winsyn);

       // Instantiate window specification
        Window window = WindowHelper.instantiateWindow(ctx.getExecContext(), rwop.getWinSpec());

        rwExecOp.setWindow(window);
      }
      
    }
   

    @Override
    public CodeGenContext createCodeGenContext(ExecContext ec, Query query, 
                                               PhyOpt phyopt)      
    {
      RangeWindowContext ctx = new RangeWindowContext(ec, query, phyopt);
     
      // Set the flag if the current operator is variable duration window
      ctx.setVariableDurationWindow(
        ((PhyOptRngWin)phyopt).isVariableDurationWindow());      
      return ctx;
    }

    @Override
    protected ExecStore instStore(CodeGenContext ctx) throws CEPException
    {
        assert ctx != null;
        PhyOpt op = ctx.getPhyopt();
        assert op != null;
        PhyOptRngWin phyOp = (PhyOptRngWin)op;
        ExecContext ec = ctx.getExecContext();
        RangeWindowContext rctx = (RangeWindowContext)ctx;
        
        if(phyOp.isVariableDurationWindow())
        {
          // Initialize lineage store          
          PhySynopsis outSyn = phyOp.getOutputSyn();
          ExecStore outStore = null;
          
          outStore = StoreInst.instStore(new StoreGenContext(ctx.getExecContext(),
                                         outSyn.getStwstore(),
                                         rctx.getTupSpec()));
          ctx.setTupleStorage(outStore);
          return outStore;    
        }
        else
        {
          ExecStore inStore = getInputStore(op, ec, 0);
          assert inStore instanceof WinStore : inStore.getClass().getName();

          ctx.setTupleStorage(inStore);
          return inStore;
        }
    }
    
    private IAEval getExpTsEval(ExecContext ec, 
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
                                FactoryManager factoryMgr)
      throws CEPException
    {
      IAEval expTsEval = AEvalFactory.create(ec);
      
      /////////////////////////////////////////////////////////////////////////
      //Note: VisTsEval will contain following set of instructions:
      // 1) Instructions to copy all the attributes from input tuple
      // 2) Instructions to evaluate the range expression
      //    2-a) Conversion to nanosecond is handled in range expression at
      //         semantic layer
      // 3) Prepare the instruction to calculate the expiry timestamp
      /////////////////////////////////////////////////////////////////////////
      
      // Step-1 Add instructions to copy the attributes from input tuple to 
      //        output tuple
      for (int a = 0; a < rwop.getNumAttrs()-1; a++)
      {
        AInstr instr = new AInstr();
        
        // Operation: copy          
        instr.op = ExprHelper.getCopyOp(rwop.getAttrTypes(a));

        // Source: a'th column of left
        instr.r1 = IEvalContext.INPUT_ROLE;
        instr.c1 = a;

        // Destn: a'th column of output
        instr.dr = expTsRole;
        instr.dc = a;

        expTsEval.addInstr(instr);
      }      
      
      // Step-2 Add instructions to evaluate the range expression
      ExprHelper.instExprDest(ec, rangeExpr, expTsEval, evalCtxInfo, 
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
      expTsEval.addInstr(instr);     
      
      return expTsEval;
    }   
   
    /**
     * Get the priority queue to store the elements according to the order of
     * their expiry timestamp
     * @param comparableColumnPos
     * @param rctx
     * @return
     */
    private PriorityQueue<ITuplePtr> getPriorityQueue(int comparableColumnPos,
        RangeWindowContext rctx)
    {
      // Instantiate array of ComparatorSpec for visible time priority queue 
      ComparatorSpecs queueComparatorSpecs[] = new ComparatorSpecs[1];
      queueComparatorSpecs[0] 
        = new ComparatorSpecs(comparableColumnPos, 
                              rctx.isNullFirst(), 
                              rctx.isAscending());
      
      // Instantiate and Initialize comparator
      TupleComparator tupleComparator = 
        new TupleComparator(queueComparatorSpecs, rctx.getTupSpec());
    
      // Initialize the priority queue with appropriate comparator
      // Note: Keeping initial size to default 1
      return new PriorityQueue<ITuplePtr>(1, tupleComparator);
    }
    
}
