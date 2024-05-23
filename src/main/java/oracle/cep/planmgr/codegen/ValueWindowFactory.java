/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/ValueWindowFactory.java /main/11 2012/02/02 19:27:26 udeshmuk Exp $ */

/* Copyright (c) 2008, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    02/01/12 - add CEPException to executeArchiverQuery
    sbishnoi    10/03/11 - adding params for currenthour and currentperiod
    sbishnoi    09/29/11 - multiplying snapshot time to 10000000 to set it to
                           nanos
    sbishnoi    09/24/11 - support for slide in value window
    sbishnoi    09/06/11 - support for currenthour and current period value
                           window
    udeshmuk    04/16/11 - archived relation support
    sbishnoi    02/27/11 - instantiate new operator for value window over
                           relations
    parujain    03/19/09 - stateless server
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    parujain    07/07/08 - value based windows
    parujain    07/07/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/ValueWindowFactory.java /main/3 2009/03/30 14:46:02 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.planmgr.codegen;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.PriorityQueue;

import oracle.cep.extensibility.datasource.IArchiver;
import oracle.cep.extensibility.datasource.IArchiverQueryResult;
import oracle.cep.extensibility.datasource.QueryRequest;
import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalConverter;
import oracle.cep.common.StreamPseudoColumn;
import oracle.cep.common.ValueWindowType;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.comparator.ComparatorSpecs;
import oracle.cep.execution.comparator.TupleComparator;
import oracle.cep.execution.internals.AInstr;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.factory.AEvalFactory;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.execution.internals.windows.CurrentHourValueWindow;
import oracle.cep.execution.internals.windows.CurrentPeriodValueWindow;
import oracle.cep.execution.internals.windows.GenericValueWindow;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.ValueRelationWindow;
import oracle.cep.execution.operators.ValueWindow;
import oracle.cep.execution.synopses.LineageSynopsisImpl;
import oracle.cep.execution.synopses.WindowSynopsisImpl;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.LineageStore;
import oracle.cep.execution.stores.WinStore;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptValueWin;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;
import oracle.cep.phyplan.expr.ExprAttr;
import oracle.cep.phyplan.window.PhyValueWinSpec;
import oracle.cep.phyplan.window.PhyWinSpec;
import oracle.cep.service.ExecContext;
import oracle.cep.exceptions.CEPException;

/**
 * Factory for the Value Window Execution operator
 *
 * @author parujain
 */
class ValueWindowFactory extends ExecOptFactory {

    /* (non-Javadoc)
   * @see oracle.cep.planmgr.codegen.ExecOptFactory#newExecOpt(oracle.cep.phyplan.PhyOpt)
   */
    @Override
    ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException 
    {
      // Create the execution operator
      ValueWindowContext vCtx = (ValueWindowContext)ctx;
      //Note: There will be a separate operator constructed for windows over
      // relation inputs
      if(vCtx.isWindowOverRelation())
      {
        // tuple spec will be same as its input
        PhyOpt op = ctx.getPhyopt();
        TupleSpec tupSpec 
          = CodeGenHelper.getTupleSpec(ctx.getExecContext(), op);
        vCtx.setTupSpec(tupSpec);
        return new ValueRelationWindow(ctx.getExecContext());
      }
      else
        return new ValueWindow(ctx.getExecContext());
    }

    @Override
    public CodeGenContext createCodeGenContext(ExecContext ec, Query query, 
                                               PhyOpt phyopt)
    {
       ValueWindowContext ctx = new ValueWindowContext(ec, query, phyopt);       
       PhyOptValueWin vwop = (PhyOptValueWin)phyopt;       
       ctx.setWindowOverRelation(vwop.isWindowOverRelation());
       return ctx;
    }
    
    @Override
    void setupExecOpt(CodeGenContext ctx) throws CEPException
    {
        assert ctx.getExecOpt() instanceof ValueWindow ||
               ctx.getExecOpt() instanceof ValueRelationWindow;
        
        PhyOpt op = ctx.getPhyopt();
        assert op instanceof PhyOptValueWin;

        ValueWindowContext vctx = (ValueWindowContext)ctx;
        
        // get the flag if the window is applied over relation
        boolean isWindowOverRelation = vctx.isWindowOverRelation();
        
        ExecContext ec = ctx.getExecContext();
        PhyOptValueWin vwop = (PhyOptValueWin) op;
        PhySynopsis        syn;
        WindowSynopsisImpl winsyn;
        ExecStore          inStore; 
        WinStore           wStore;
    
        PhyWinSpec wspec = vwop.getWinSpec();
        assert wspec instanceof PhyValueWinSpec;
        PhyValueWinSpec vwspec = (PhyValueWinSpec)wspec;
        
        int pos;
        Datatype colType;
        
        if(isWindowOverRelation && vwspec.isWindowOnElementTime())
        {
          colType = StreamPseudoColumn.ELEMENT_TIME.getColumnType();
          pos = op.getNumAttrs() - 1;
        }
        else
        {
          ExprAttr col = vwspec.getColumn();
          pos = col.getAValue().getPos();
          colType = col.getType();
        }
        // Get the appropriate value window
        oracle.cep.execution.internals.windows.ValueWindow window = null;
        
        if(!isWindowOverRelation)
        {
          // Instantiate the window synopsis
          syn = vwop.getWinSyn();

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
          ValueWindow vwExecOp = (ValueWindow) ctx.getExecOpt();
          vwExecOp.setPosition(pos);
          vwExecOp.setColType(colType);
          
          // Construct ValueWindow
          ValueWindowType type = vwspec.getType();
          
          // Get the appropriate value window
          window = getWindow(type, vwspec);
          
          vwExecOp.setWindowLong(vwspec.isLong());
          vwExecOp.setWindow(window);          
          vwExecOp.setWinSynopsis(winsyn);
        }
        else
        {          
          IEvalContext evalContext = EvalContextFactory.create(ec);
          
          // Construct outEval which will copy attribute value from tuple at 
          // INPUT_ROLE to tuple at NEW_OUTPUT_ROLE
          IAEval outEval = getOutEval(ec, op);          
          
          // Construct copyEval which will copy all but last attribute values
          // from tuple at INPUT_ROLE to tuple at NEW_OUTPUT_ROLE
          IAEval copyEval =  vwspec.isWindowOnElementTime() ? getCopyEval(ec,op) : null;
          
          ValueRelationWindow vrwExecOp 
            = (ValueRelationWindow)ctx.getExecOpt();
          
          PhySynopsis outSyn = vwop.getOutputSyn();
          
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
          
          // Instantiate array of ComparatorSpec for output priority queue 
          ComparatorSpecs queueComparatorSpecs[] = new ComparatorSpecs[1];
          queueComparatorSpecs[0] 
            = new ComparatorSpecs(pos, vctx.isNullFirst(), vctx.isAscending());
          
          // Instantiate and Initialize comparator
          TupleComparator tupleComparator = 
            new TupleComparator(queueComparatorSpecs, vctx.getTupSpec());
        
          // Initialize the priority queue with appropriate comparator
          // Note: Keeping initial size to default 1
          PriorityQueue<ITuplePtr> valueWindowElementsQueue 
            = new PriorityQueue<ITuplePtr>(1,tupleComparator);
          
          // pending queue will keep those input elements which will be
          // part of window when timestamp will progress as their column value
          // is greater than current input timestamp
          PriorityQueue<ITuplePtr> pendingInputElements
            = new PriorityQueue<ITuplePtr>(1, tupleComparator);
          
          // Update the execution operator
          vrwExecOp.setOutSynopsis(linSyn);
          vrwExecOp.setPosition(pos);     
          vrwExecOp.setColType(colType);
          
          // Construct ValueWindow
          ValueWindowType type = vwspec.getType();
          
          // Get the appropriate value window
          window = getWindow(type, vwspec);        
                   
          vrwExecOp.setWindow(window);  
          vrwExecOp.setWindowOnElementTime(vwspec.isWindowOnElementTime());
          vrwExecOp.setValueWindowElements(valueWindowElementsQueue);
          vrwExecOp.setPendingInputTuples(pendingInputElements);
          vrwExecOp.setOutEval(outEval);
          vrwExecOp.setCopyEval(copyEval);
          vrwExecOp.setEvalContext(evalContext);
        }
        
        // Set the Value Window Context params
        vctx.setValueWindowSpec(vwspec);
        vctx.setColType(colType);
       
    }

    @Override
    protected ExecStore instStore(CodeGenContext ctx) throws CEPException
    {
        assert ctx != null;
        PhyOpt op = ctx.getPhyopt();
        assert op != null;
        ValueWindowContext vCtx = (ValueWindowContext)ctx;
        if(vCtx.isWindowOverRelation())
        {
          // Initialize lineage store
          PhyOptValueWin phyOp = (PhyOptValueWin)op;
          PhySynopsis outSyn = phyOp.getOutputSyn();
          ExecStore outStore = null;
          
          outStore = StoreInst.instStore(new StoreGenContext(ctx.getExecContext(),
                                         outSyn.getStwstore(),
                                         vCtx.getTupSpec()));
          ctx.setTupleStorage(outStore);
          return outStore;    
        }
        else
        {
          ExecContext ec = ctx.getExecContext();
          ExecStore inStore = getInputStore(op, ec, 0);
          assert inStore instanceof WinStore : inStore.getClass().getName();

          ctx.setTupleStorage(inStore);
          return inStore;
        }
        
    }
    
    /**
     * Get output eval which will copy all the tuple attribute values from 
     * input tuple to output tuple
     * @param ec
     * @param op
     * @return
     * @throws ExecException
     */
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
     * Get output eval which will copy all the tuple attribute values but last
     * from input tuple to output tuple
     * @param ec
     * @param op
     * @return
     * @throws ExecException
     */
    private IAEval getCopyEval(ExecContext ec, PhyOpt op) throws CEPException
    {
      IAEval     eval     = AEvalFactory.create(ec);
      int       numAttrs = op.getNumAttrs();

      // copy all the columns except the last one - the ELEMENT_TIME col
      for (int attr = 0; attr < numAttrs-1; attr++)
      {
        AInstr instr = new AInstr();
        
        instr.op = ExprHelper.getCopyOp(op.getAttrTypes(attr));
        instr.r1 = IEvalContext.INPUT_ROLE;
        instr.c1 = attr;

        instr.dr = IEvalContext.NEW_OUTPUT_ROLE;
        instr.dc = attr;
        
        eval.addInstr(instr);
      }
      eval.compile();

      return eval;
    }
    
    protected IArchiverQueryResult executeArchiverQuery(CodeGenContext cgctx,
                                                        String archiverQuery,
                                                        IArchiver archiver)
							throws CEPException
    {
      ValueWindowContext ctx = (ValueWindowContext)cgctx;
      
      // Get the current system time/query start time to be set as snapShotTime
      // Multiply it with 10^6 to make it in nanosecond unit
      Long currentTime = System.currentTimeMillis()  * 1000000l ; //default
      
      //Check if the start time value is set
      if(ctx.getQuery().getQueryStartTime() != Long.MIN_VALUE)
      {
        LogUtil.info(LoggerType.TRACE, 
                     "Using user specified query start time as snapshot time");
        currentTime = ctx.getQuery().getQueryStartTime(); 
      }
      
      // Determine the snapshot time on the basis of value window type
      // Snapshot time will be nanosecond unit
      long snapShotTime 
        = getSnapShotTime(currentTime, (ValueWindowContext) ctx);
      
      ctx.getExecOpt().setSnapShotTime(currentTime);
      
      QueryRequest[] requests = null;
      //Prepare query requests and give parameters(If any)
      if(ctx.getColType() == Datatype.TIMESTAMP)
      {  
        // Timestamp constructor requires parameter timestamp in millisecond unit
        Timestamp ts = new Timestamp(snapShotTime / 1000000l);
        requests     = new QueryRequest[]
                       { 
                         new QueryRequest(archiverQuery, new Object[]{ts})
                       };  
        LogUtil.info(LoggerType.TRACE,
            "Snapshot time sent as parameter "+ts);
      }
      else
      {
        requests = new QueryRequest[] 
                   {
                     new QueryRequest(archiverQuery, 
                                      new Object[]{snapShotTime})
                   };
        LogUtil.info(LoggerType.TRACE,
            "Snapshot time sent as parameter "+snapShotTime);
      }
      
      // Execute query
      IArchiverQueryResult results = archiver.execute(requests);
      assert results.getResultCount() == requests.length ;
      return results;
    }
    
    private long getSnapShotTime(long currentTime, ValueWindowContext ctx)
    {
      switch(ctx.getValueWindowSpec().getType())
      {
      case GENERIC:
        return currentTime;
      case CURRENT_HOUR:
        long numNanosInHour = IntervalConverter.HOUR * 1000000000l;
        long numHours = currentTime / numNanosInHour;
        return numHours * numNanosInHour;
      case CURRENT_PERIOD:
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTime / 1000000l);
        int numHour = cal.get(Calendar.HOUR_OF_DAY);   
        int numMins = cal.get(Calendar.MINUTE);
        int numSecs = cal.get(Calendar.SECOND);
        // Set the time to DD/MM/YY 00:00:00AM
        long dayBaseValue 
          = currentTime - numHour * IntervalConverter.HOUR * 1000000000l
                    - numMins * IntervalConverter.MINUTE * 1000000000l
                    - numSecs * IntervalConverter.SECOND * 1000000000l;
        
        long periodBaseValue 
         = dayBaseValue + ctx.getValueWindowSpec().getCurrentPeriodStartTime();
        return periodBaseValue;
      }
      return Long.MIN_VALUE;
    }
    
    /**
     * Get the appropriate value window
     * @param type
     * @return
     */
    public oracle.cep.execution.internals.windows.ValueWindow getWindow(
      ValueWindowType type, PhyValueWinSpec vwspec)
    {
      switch(type)
      {
      case GENERIC:
        GenericValueWindow window = new GenericValueWindow();
        window.setSlide(vwspec.getSlideAmount());
        if(vwspec.isLong())
          window.setWindowSize(vwspec.getLongConstVal());          
        else
          window.setWindowSize(vwspec.getDoubleConstVal());          
        return window;
      case CURRENT_HOUR:
        CurrentHourValueWindow chwindow = new CurrentHourValueWindow();
        chwindow.setSlide(vwspec.getSlideAmount());
        chwindow.setWindowSize(vwspec.getWinSize());
        return chwindow;
      case CURRENT_PERIOD:
        CurrentPeriodValueWindow cpwindow = new CurrentPeriodValueWindow();
        cpwindow.setSlide(vwspec.getSlideAmount());
        cpwindow.setCurrentPeriodStartTime(vwspec.getCurrentPeriodStartTime());
        cpwindow.setWindowSize(vwspec.getWinSize());
        return cpwindow;
      }
      return null;
    }
    
   }

