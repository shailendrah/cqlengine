/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/GroupAggrFactory.java /main/28 2011/07/09 08:53:45 udeshmuk Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
  Factory performing instantiation (producing execution objects) for the
  Group/Aggregation operator

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
    udeshmuk  04/15/11 - archived relation support
    sborah    01/21/11 - remove eval.setContext
    sborah    10/14/09 - support for bigdecimal
    parujain  03/19/09 - stateless server
    udeshmuk  11/02/08 - adding xmlagg review comments.
    hopark    10/10/08 - remove statics
    hopark    10/09/08 - remove statics
    skmishra  09/16/08 - adding param to getInitEval
    skmishra  07/17/08 - adding order by for xmlagg
    skmishra  06/10/08 - adding xml_agg
    hopark    12/07/07 - cleanup spill
    sbishnoi  09/26/07 - support of dirtySyn
    hopark    09/04/07 - eval optimize
    sbishnoi  07/22/07 - add isDirty flag to output tuple
    rkomurav  07/04/07 - cleanup of Aggrhelper
    sbishnoi  06/12/07 - support for multi-arg UDAs.
    parujain  05/02/07 - UDA statistics
    najain    04/11/07 - bug fix
    hopark    04/06/07 - mark static pin for const tupleptr
    najain    03/14/07 - cleanup
    hopark    03/06/07 - use ITuplePtr
    rkomurav  01/05/07 - null Support for UDA
    rkomurav  12/13/06 - count(*) vs count(expr) differentiation
    parujain  12/19/06 - fullScanId for RelationSynopsis
    rkomurav  12/07/06 - emit count(*) for zero rows
    najain    12/04/06 - stores are not storage allocators
    hopark    11/07/06 - bug 5465978 : refactor newExecOpt
    najain    10/28/06 - bug fix
    rkomurav  09/28/06 - expressions support for aggregations
    parujain  08/11/06 - cleanup planmgr
    parujain  08/10/06 - max/min timestamp datatype
    najain    07/19/06 - ref-count tuples
    anasrini  07/16/06 - support for user defined aggregations
    anasrini  07/12/06 - support for user defined aggregations
    najain    07/05/06 - cleanup
    najain    06/29/06 - factory allocation cleanup
    najain    06/18/06 - cleanup
    najain    06/16/06 - cleanup
    anasrini  05/09/06 - Support for GROUP/AGGR operator
    skaluska  02/28/06 - Creation
    skaluska  02/28/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/GroupAggrFactory.java /main/25 2009/11/09 10:10:59 sborah Exp $
 *  @author  skaluska
 *  @since   1.0
 */

package oracle.cep.planmgr.codegen;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import oracle.cep.extensibility.datasource.IArchiver;
import oracle.cep.extensibility.datasource.IArchiverQueryResult;
import oracle.cep.extensibility.datasource.QueryRequest;
import oracle.cep.archiver.SampleArchiver;
import oracle.cep.common.AggrFunction;
import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.BaseAggrFn;
import oracle.cep.common.Datatype;
import oracle.cep.common.UserDefAggrFn;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.comparator.ComparatorSpecs;
import oracle.cep.execution.indexes.HashIndex;
import oracle.cep.execution.internals.AInstr;
import oracle.cep.execution.internals.AOp;
import oracle.cep.execution.internals.BInstr;
import oracle.cep.execution.internals.BOp;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.HInstr;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.IHEval;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.factory.AEvalFactory;
import oracle.cep.execution.internals.factory.BEvalFactory;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.execution.internals.factory.HEvalFactory;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.GroupAggr;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.RelStore;
import oracle.cep.execution.synopses.RelationSynopsisImpl;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptGroupAggr;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprOrderBy;
import oracle.cep.service.ExecContext;

/**
 * Factory performing instantiation (producing execution objects) for the
 * Group/Aggregation operator
 *
 * @author anasrini
 * @since 1.0
 */
public class GroupAggrFactory extends ExecOptFactory {
    
    @Override
    public CodeGenContext createCodeGenContext(ExecContext ec, Query query, 
                                               PhyOpt phyopt)
    {
      return new GroupAggrContext(ec, query, phyopt); 
    }
    
    /* (non-Javadoc)
   * @see oracle.cep.planmgr.codegen.ExecOptFactory#newExecOpt(oracle.cep.phyplan.PhyOpt)
   */
    @Override
    ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException
    {
        assert ctx instanceof GroupAggrContext;
        GroupAggrContext gctx = (GroupAggrContext)ctx;
        assert ctx.getPhyopt() instanceof PhyOptGroupAggr;
        PhyOptGroupAggr aggrPhyOp = (PhyOptGroupAggr) ctx.getPhyopt();

        // Transform the grouping & aggr. information to a form easier to code
        initGroupAggrInfo(ctx.getExecContext(), gctx, aggrPhyOp);
        
        return new GroupAggr(ctx.getExecContext());
    }

    @Override
    void setupExecOpt(CodeGenContext ctx) throws CEPException
    {
    	assert ctx instanceof GroupAggrContext;
        GroupAggrContext gctx = (GroupAggrContext)ctx;
        assert ctx.getExecOpt() instanceof GroupAggr;

        GroupAggr groupAggr = (GroupAggr) ctx.getExecOpt();
        PhyOpt op = ctx.getPhyopt();
        PhyOptGroupAggr aggrPhyOp = (PhyOptGroupAggr)op;

        boolean              isRel;
        IAEval                initEval;
        IAEval                plusEval;
        IAEval minusEval = null;
        IAEval arithScanNotReqEval = null;
        IAEval nullOutputEval  = null;
        IBEval emptyGroupEval = null;
        IBEval scanNotReqEval = null;
        IAEval updateEval = null;
        
        //special evals for uda
        IAEval                releaseHandlerEval;
        IAEval                resetHandlerEval;
        IAEval                allocHandlerEval;

        //special evals for xmlagg (modeled on UDA)
        IAEval                allocIndexEval;
        IAEval                releaseIndexEval;
        IAEval                resetIndexEval;
        
        IAllocator         ctf;
        IAllocator         stf;
        
        ITuplePtr            t;
        ExecStore            outStore;
        ExecStore            inStore;
        PhySynopsis          outPhySyn;
        RelationSynopsisImpl outSyn;
        int                  outScanId;
        HashIndex            outIdx;
        PhySynopsis          phyInSyn;
        RelationSynopsisImpl inSyn    = null;
        int                  inScanId = 0;
        int                  inFullScanId=0;
        HashIndex            inIdx;
        RelationSynopsisImpl dirtySyn;
        PhySynopsis          dirtyPhySyn;
        
        
        isRel = !(op.getInputs()[0].getIsStream());
        ExecContext ec = ctx.getExecContext();

        // Populate countStarPos
        int countStarPos = getCountStarPos(aggrPhyOp, gctx.getNumAggrParamExprs(),
                                           gctx.getAggrFunction());
        gctx.setCountStarPos(countStarPos);

        // Populate sumPos [] array
        int[] sumPos   = new int[gctx.getNumAggrParamExprs()];
        int[] countPos = new int[gctx.getNumAggrParamExprs()];
        AggrHelper.getSumCountPos(sumPos, countPos, gctx.getAggrFunction(),
        		gctx.getAggrParamExprs(),
        		gctx.getNumAggrParamExprs(), gctx.getNumGroupAttrs());
        gctx.setSumPos(sumPos);
        gctx.setCountPos(countPos);

        // Eval Context
        IEvalContext evalContext = EvalContextFactory.create(ec);
        FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
        EvalContextInfo evalCtxInfo = new EvalContextInfo(factoryMgr);
        ConstTupleSpec ct = evalCtxInfo.ct;
        TupleSpec st = evalCtxInfo.st;
        
        gctx.setEvalContext(evalContext);
        gctx.setEvalContextInfo(evalCtxInfo);
        gctx.setConstTupleSpec(ct);
        gctx.setScratchTuple(st);

        // Setup all the evaluators

        // Init Eval
        initEval = getInitEval(ec, gctx, aggrPhyOp);

        // Plus Eval
        plusEval = getPlusEval(ec, gctx);
        
        //null output eval
        nullOutputEval = getNullOutputEval(ec, gctx, aggrPhyOp);

        if (isRel) {
            // Update Eval
            updateEval = getUpdateEval(ec, gctx);

            // Minus Eval
            minusEval = getMinusEval(ec, gctx);

            // arithScanNotReq Eval - Arith Operations that needs to be performed
            // before evaluating ScanNotReqEval
            // ScanNotReq Eval
            arithScanNotReqEval = AEvalFactory.create(ec);
            scanNotReqEval = getScanNotReqEval(ec,gctx, arithScanNotReqEval);
            
            arithScanNotReqEval.compile();
            // Empty Group Eval
            emptyGroupEval = getEmptyGroupEval(ec, gctx);
        }
        
      
        if(gctx.getNumUDA() > 0)
        {
          ArrayList<Integer> compressUdaPos     = new ArrayList<Integer>();
          ArrayList<IAggrFnFactory> compressUdaFactory = 
                                           new ArrayList<IAggrFnFactory>();
          ArrayList<IAggrFunction> compressUdaHandler = 
                                           new ArrayList<IAggrFunction>();
          AggrHelper.compressUdaInfo(gctx.getUdaPos(), gctx.getUdaFactory(),
                                     gctx.getUdaHandler(),compressUdaPos,
              compressUdaFactory, compressUdaHandler, gctx.getAggrFunction());
          Integer[] temp = compressUdaPos.toArray(new Integer[1]);
          int[] posArr;
          posArr = new int[temp.length];
          for(int i = 0; i <  temp.length; i++)
          {
            posArr[i] = temp[i].intValue();
          }
          
          releaseHandlerEval = AggrHelper.getReleaseHandlerEval(ec, posArr,
              compressUdaFactory.toArray(new IAggrFnFactory[1]));
          
          resetHandlerEval = AggrHelper.getResetHandlerEval(ec, posArr);
          
          allocHandlerEval = AggrHelper.getAllocHandlerEval(ec,
              IEvalContext.NEW_OUTPUT_ROLE, posArr,
              compressUdaFactory.toArray(new IAggrFnFactory[1]),
              compressUdaHandler.toArray(new IAggrFunction[1]));
          
          releaseHandlerEval.compile();
          resetHandlerEval.compile();
          allocHandlerEval.compile();
        }
        else
        {
          releaseHandlerEval = null;
          resetHandlerEval = null;
          allocHandlerEval = null;
        }
        
        if(gctx.getNumXmlAgg() > 0)
        {
          allocIndexEval = AggrHelper.getAllocIndexEval(ec,
              IEvalContext.NEW_OUTPUT_ROLE, gctx.getXmlAggIndexPos(), 
              gctx.getCompareSpecs(), gctx.getOrderByTupleSpec());
          
          resetIndexEval 
            = AggrHelper.getResetIndexEval(ec, gctx.getXmlAggIndexPos());
          
          releaseIndexEval 
            = AggrHelper.getReleaseIndexEval(ec, gctx.getXmlAggIndexPos());
          
          allocIndexEval.compile();
          releaseIndexEval.compile();
          resetIndexEval.compile();
        }
        
        else
        {
          allocIndexEval = null;
          releaseIndexEval = null;
          resetIndexEval = null;
        }

        //Scratch Tuple
        if (st != null) {
            stf = factoryMgr.get(st);

            t = (ITuplePtr)stf.allocate(); //SCRATCH_TUPLE
            evalContext.bind(t, IEvalContext.SCRATCH_ROLE);
        }

        // Constant Tuple
        ctf = factoryMgr.get(ct.getTupleSpec());
        t = (ITuplePtr)ctf.allocate(); //SCRATCH_TUPLE
        ct.populateTuple(ec, t);
        evalContext.bind(t, IEvalContext.CONST_ROLE);


        // Handle Synopsis
        // Output Synopsis
        outPhySyn = aggrPhyOp.getOutSyn();

        ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
        allCtx.setOpt(op);
        allCtx.setObjectType(RelationSynopsisImpl.class.getName());
        outSyn = (RelationSynopsisImpl)ObjectManager.allocate(allCtx);

        outStore = ctx.getTupleStorage();
        assert (outStore != null);
        assert (outStore instanceof RelStore);
        outSyn.setStore((RelStore) outStore);
        outSyn.setStubId(outStore.addStub());
        outPhySyn.setSyn(outSyn);

        int fullScanId;
        // Create an index for output synopsis if necessary (there exist
        // grouping attributes)
        if (gctx.getNumGroupAttrs() > 0) {
            outIdx = new HashIndex(ec);
            initOutIndex(ec, evalContext, gctx, outIdx, outStore.getFactory());
            outScanId = outSyn.setIndexScan(null, outIdx);
            fullScanId = outSyn.setFullScan();
        } 
        else {
            // outSyn contains only one tuple
            outScanId = outSyn.setFullScan();
            fullScanId = outScanId;
        }
        
        outSyn.setEvalContext(evalContext);
        outSyn.initialize();

        // Input Synopsis
        phyInSyn = aggrPhyOp.getInSyn();
        if (phyInSyn != null) {
            allCtx.setOpt(op);
            allCtx.setObjectType(RelationSynopsisImpl.class.getName());
            inSyn = (RelationSynopsisImpl)ObjectManager.allocate(allCtx);
            inStore = phyInSyn.getStwstore().getInstStore();
            inSyn.setStore((RelStore)inStore);
            inSyn.setStubId(inStore.addStub());

            // Create an index for input synopsis if necessary (there exist
            // grouping attributes)
            if (gctx.getNumGroupAttrs() > 0) {
                inIdx = new HashIndex(ec);
                initInIndex(ec, evalContext, gctx, inIdx, inStore.getFactory());
                inScanId = inSyn.setIndexScan(null, inIdx);
                inFullScanId = inSyn.setFullScan();
            } 
            else {
                inScanId = inSyn.setFullScan();
                inFullScanId = inScanId;
            }
            
            inSyn.setEvalContext(evalContext);
            inSyn.initialize();
        }

        // Dirty Synopsis
        dirtyPhySyn = aggrPhyOp.getDirtySyn();
        allCtx.setOpt(op);
        allCtx.setObjectType(RelationSynopsisImpl.class.getName());
        dirtySyn = (RelationSynopsisImpl)ObjectManager.allocate(allCtx);
        
        // for tuple storage, dirtySyn will refer to outStore
        dirtySyn.setStore((RelStore) outStore);
        dirtySyn.setStubId(outStore.addStub());
        dirtyPhySyn.setSyn(dirtySyn);
        HashIndex dirtyIdx = new HashIndex(ec);
        
        // As we search dirtySyn on basis of group attributes of INPUT Tuple
        // So dirtyIdx is similar to outIdx; so we will use initOutIdx
        initOutIndex(ec, evalContext,gctx, dirtyIdx, outStore.getFactory());
        
        int dirtyScanId = dirtySyn.setIndexScan(null, dirtyIdx);
        int dirtyFullScanId = dirtySyn.setFullScan();
        dirtySyn.initialize();
        
        // Setup the output synopsis
        groupAggr.setOutSynopsis(outSyn);

        // Set the outScanId
        groupAggr.setOutScanId(outScanId);

        // Set fullScanId
        groupAggr.setFullScanId(fullScanId);

        // Setup the input synopsis
        groupAggr.setInSynopsis(inSyn);

        // Set the inScanId
        groupAggr.setInScanId(inScanId);
        
        // Set the inFullScanId
        groupAggr.setInFullScanId(inFullScanId);
        
        // Setup the dirty Synopsis
        groupAggr.setDirtySyn(dirtySyn);
        
        //Setup index scan for dirtySyn
        groupAggr.setDirtyScanId(dirtyScanId);
        
        //Setup full scan for dirtySyn
        groupAggr.setDirtyFullScanId(dirtyFullScanId);
        
        // Set the evaluation context
        groupAggr.setEvalContext(evalContext);

        // Set the Init Evaluator
        groupAggr.setInitEval(initEval);

        // Set the Plus Evaluator
        groupAggr.setPlusEval(plusEval);

        // Set the update evaluator
        groupAggr.setUpdateEval(updateEval);

        // Set the arith evaluator for scanNotReqEval
        groupAggr.setArithScanNotReqEval(arithScanNotReqEval);

        // Set the evaluator that determines if a rescan is required
        groupAggr.setScanNotReqEval(scanNotReqEval);

        // Set the minus evaluator
        groupAggr.setMinusEval(minusEval);
        
        // Set the zero count evaluator
        groupAggr.setNullOutputEval(nullOutputEval);

        // Set the empty group check evaluator
        groupAggr.setEmptyGroupEval(emptyGroupEval);
        
        // Set number of attributes/expressions in group by clause
        groupAggr.setNumGroupByAttrs(gctx.getNumGroupAttrs());
        
        // Setup related to UDA
        groupAggr.setOneGroup(gctx.getOneGroup());
        groupAggr.setNumUDA(gctx.getNumUDA());
        groupAggr.setNumFullUDA(gctx.getNumFullUDA());
        groupAggr.setReleaseHandlerEval(releaseHandlerEval);
        groupAggr.setResetHandlerEval(resetHandlerEval);
        groupAggr.setAllocHandlerEval(allocHandlerEval);
        
        //setup allocator/evaluators for xmlagg orderby
        groupAggr.setOrderByAllocator(gctx.getOrderByAllocator());
        groupAggr.setNumXmlAgg(gctx.getNumXmlAgg());
        groupAggr.setAllocIndexEval(allocIndexEval);
        groupAggr.setReleaseIndexEval(releaseIndexEval);
        groupAggr.setResetIndexEval(resetIndexEval);
    }
    
    @Override
    protected ExecStore instStore(CodeGenContext ctx) throws CEPException
    {
        assert ctx != null;
        GroupAggrContext gctx = (GroupAggrContext)ctx;
        PhyOpt op = ctx.getPhyopt();
        assert op != null;

        ExecStore outStore = null;

        // Handle Stores and StorageAllocs
        // Instantiate the store
        PhyStore store = op.getStore();
        StoreGenContext sgc;
        // Get storeGenContext for new TupleSpec
        sgc = new StoreGenContext(ctx.getExecContext(), store, gctx.getTupleSpec());
        outStore = StoreInst.instStore(sgc);
        ctx.setTupleStorage(outStore);
        return outStore;
    }

    private void initGroupAggrInfo(ExecContext ec, GroupAggrContext ctx, 
                                   PhyOptGroupAggr aggrPhyOp) 
    throws CEPException {

        BaseAggrFn[] aggFns;
        int len;
        Attr[] groupAttrs = aggrPhyOp.getGroupAttrs();

        ArrayList<Expr[]> aggrParamExprs = aggrPhyOp.getAggrParamExprs();
        ctx.setAggrParamExprs(aggrParamExprs);
        ArrayList<ExprOrderBy[]> orderByExprs = aggrPhyOp.getOrderByExprs();
        ctx.setOrderByExprs(orderByExprs);
        int numAggrParamExprs = aggrPhyOp.getNumAggrParamExprs();
        ctx.setNumAggrParamExprs(numAggrParamExprs);
        int numGroupAttrs     = aggrPhyOp.getNumGroupAttrs();
        ctx.setNumGroupAttrs(numGroupAttrs);
        aggFns    = aggrPhyOp.getAggrFunctions();
       
        int[] groupPos  = new int[numGroupAttrs];
        AggrFunction[] fn  = new AggrFunction[numAggrParamExprs];
        AttributeMetadata[] attrMetadata = aggrPhyOp.getAttrMetadata();
        
        Datatype[] types = new Datatype[attrMetadata.length];
        
        for(int i = 0 ; i < attrMetadata.length; i ++)
        {
          types[i] = attrMetadata[i].getDatatype();
        }
        
        ctx.setTypes(types);
        
        len       = types.length - numGroupAttrs;
        Datatype[] aggrTypes = new Datatype[len];
        System.arraycopy(types, numGroupAttrs, aggrTypes, 0, len);
        ctx.setAggrTypes(aggrTypes);
        ArrayList<Datatype[]> aggrInpTypes = aggrPhyOp.getAggrInputTypes();
        ctx.setAggrInpTypes(aggrInpTypes);
        
        TupleSpec ts = CodeGenHelper.getTupleSpec(ec, aggrPhyOp);
        ctx.setTupleSpec(ts);

        for (int i=0; i<numGroupAttrs; i++) {
            assert groupAttrs[i].getInput() == 0 : groupAttrs[i].getInput();
            groupPos[i] = groupAttrs[i].getPos();
        }
        ctx.setGroupPos(groupPos);

        assert aggrParamExprs != null;
        for(int i=0; i< aggrParamExprs.size(); i++)
        {
          assert aggrParamExprs.get(i) != null;
          assert aggrParamExprs.get(i).length > 0 : aggrParamExprs.get(i).length;
        }

        int numIncrUDA = 0;
        int numFullUDA = 0;
        int numXmlAgg  = 0;
        
        //Count # of UDA's and xmlaggs
        for (int i=0; i<numAggrParamExprs; i++) {
            fn[i] = aggFns[i].getFnCode();
            if (fn[i] == AggrFunction.USER_DEF) {
                if (aggFns[i].supportsIncremental())
                    numIncrUDA++;
                else
                    numFullUDA++;
            }
            
            if(fn[i] == AggrFunction.XML_AGG)
              numXmlAgg++;
        }
        int numUDA = numIncrUDA + numFullUDA;
        ctx.setNumUDA(numUDA);
        ctx.setNumIncrUDA(numIncrUDA);
        ctx.setNumFullUDA(numFullUDA);
        ctx.setAggrFunction(fn);
        ctx.setNumXmlAgg(numXmlAgg);
        
        boolean oneGroup;
        UserDefAggrFn[] uda;
        int[] udaPos;
        IAggrFnFactory[] udaFactory;
        IAggrFunction[] udaHandler;

        // Initialization for User Defined Aggregations (UDA)
        if (numUDA > 0)
        {
          oneGroup   = (numGroupAttrs == 0);
          uda        = new UserDefAggrFn[numAggrParamExprs];
          udaPos     = new int[numAggrParamExprs];
          udaFactory = new IAggrFnFactory[numAggrParamExprs];
          udaHandler = new IAggrFunction[numAggrParamExprs];
          ts         = CodeGenHelper.getTupleSpec(ec, aggrPhyOp);
  		
          AggrHelper.initUDA(ec, numUDA, aggrPhyOp.getAggrFunctions(), uda, udaPos,
              udaFactory, udaHandler, numAggrParamExprs, fn, oneGroup, true,
              ts, aggrPhyOp.getAggrInputTypes());
          ctx.setOneGroup(oneGroup);
          ctx.setUserDefAggrFn(uda);
          ctx.setUdaPos(udaPos);
          ctx.setUdaFactory(udaFactory);
          ctx.setUdaHandler(udaHandler);
          ctx.setTupleSpec(ts);
        }
        
        int[] xmlAggIndexPos; 
        ComparatorSpecs[][] compareSpecs;
        TupleSpec orderByTupleSpec;
        IAllocator<ITuplePtr> orderByAllocator = null;;
        //Initialization for xmlagg's.
        if(numXmlAgg > 0)
        {
          //allocate and init xmlAggPos
          xmlAggIndexPos = new int[numAggrParamExprs];
          compareSpecs = new ComparatorSpecs[numAggrParamExprs][];
          for(int j=0;j<numAggrParamExprs;j++)
          {
            xmlAggIndexPos[j] = -1;
            compareSpecs[j]   = null;
          }
          for(int j=0; j < orderByExprs.size(); j++)
          {
            if(orderByExprs.get(j)!=null)
              assert (orderByExprs.get(j)).length != 0;
          }
          FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
          orderByTupleSpec = new TupleSpec(factoryMgr.getNextId());
          AggrHelper.initXmlAgg(numAggrParamExprs, fn, orderByExprs, 
                                xmlAggIndexPos, compareSpecs, ts, 
                                orderByTupleSpec);
          if(orderByTupleSpec.getNumAttrs() > 0) {
            orderByAllocator = factoryMgr.get(orderByTupleSpec);
          }
          
          ctx.setXmlAggIndexPos(xmlAggIndexPos);
          ctx.setCompareSpecs(compareSpecs);
          ctx.setOrderByTupleSpec(orderByTupleSpec);
          ctx.setOrderByAllocator(orderByAllocator);
        }
    }
    
    private int getCountStarPos(PhyOptGroupAggr op, int numAggrParamExprs,
                                AggrFunction[] fn)
    {
      for (int i=0; i<numAggrParamExprs; i++) {
          if (fn[i] == AggrFunction.COUNT_STAR) {
              // We ensure in our earlier stages of processing that there exists
              // only one count_star per operator (there could be none)

              return i;
          }
      }

      // There is no COUNT_STAR
      return -1;
    }
    
    private IAEval getInitEval(ExecContext ec, GroupAggrContext ctx, 
                               PhyOptGroupAggr aggrPhyOp) 
    throws CEPException {
      
      IAEval initEval;
      initEval = AEvalFactory.create(ec);
      // Copy the group attributes from the new plus tuple to the new output
      // tuple
      copyGroupAttrs(initEval, ctx);
      
      boolean includeAllocIndex = ctx.getNumGroupAttrs() == 0;
      
      AggrHelper.getInitEval(ec, initEval, IEvalContext.INPUT_ROLE,
          IEvalContext.OLD_OUTPUT_ROLE, IEvalContext.NEW_OUTPUT_ROLE,
          ctx.getEvalContextInfo(), ctx.getEvalContext(), ctx.getAggrFunction(),
          ctx.getAggrParamExprs(), ctx.getNumAggrParamExprs(), ctx.getAggrInpTypes(),
          ctx.getAggrTypes(), ctx.getNumGroupAttrs(), aggrPhyOp.getAggrFunctions(),
          ctx.getUdaPos(), 0, ctx.getUdaFactory(), ctx.getUdaHandler(), 
          false, includeAllocIndex, ctx.getXmlAggIndexPos(),
          ctx.getOrderByExprs(), ctx.getCompareSpecs(), ctx.getOrderByTupleSpec());
      
      initEval.compile();
      
      return initEval;
    }
    private IAEval getNullOutputEval(ExecContext ec, GroupAggrContext ctx,
                                     PhyOptGroupAggr aggrPhyOp) 
    throws CEPException
    {
      //for every aggregate expression emit a zero or null.
      IAEval                     eval = AEvalFactory.create(ec);
      AInstr                    instr;
      AInstr                    instr1;
      AggrFunction[] fn = ctx.getAggrFunction();
      int numGroupAttrs = ctx.getNumGroupAttrs();
      
      if(numGroupAttrs > 0)
      {
        // Copy the group attributes from the new plus tuple to the new output
        // tuple
        copyGroupAttrs(eval, ctx);
      }
      
      for(int i = 0; i < ctx.getNumAggrParamExprs(); i++)
      {
        instr = new AInstr();
        switch(fn[i])
        {
        case COUNT:
        case COUNT_STAR:
          instr.op = AOp.INT_CPY;
          instr.r1 = IEvalContext.CONST_ROLE;
          instr.c1 = ctx.getConstTupleSpec().addInt(0);
          instr.dr = IEvalContext.NEW_OUTPUT_ROLE;
          instr.dc = i  + numGroupAttrs;
          eval.addInstr(instr);
          break;
        case SUM:
        case MAX:
        case MIN:
        case AVG:
        case XML_AGG:
          instr.op = AOp.NULL_CPY;
          instr.dr = IEvalContext.NEW_OUTPUT_ROLE;
          instr.dc = i + numGroupAttrs;
          eval.addInstr(instr);
          break;
        case USER_DEF:
          //handle next but collect uda info
          break;
        default:
            assert false;
        }
      }
      
      int[] udaPos = ctx.getUdaPos();
      for(int i = 0; i < ctx.getNumAggrParamExprs(); i++)
      {
        switch(fn[i])
        {
        case USER_DEF:
          instr  = new AInstr();
          instr1 = new AInstr();
          
          instr.op = AOp.NULL_CPY;
          instr.dr = IEvalContext.NEW_OUTPUT_ROLE;
          instr.dc = i + numGroupAttrs;
          
          // Initialise the UDA
          int fId = ((UserDefAggrFn)aggrPhyOp.getAggrFunctions()[i]).getFnId();
          instr1.op = AOp.UDA_INIT;
          instr1.r1 = IEvalContext.OLD_OUTPUT_ROLE;
          instr1.c1 = udaPos[i];
          instr1.dr = IEvalContext.NEW_OUTPUT_ROLE;
          instr1.dc = udaPos[i];
          instr1.setFunctionId(fId);
          
          eval.addInstr(instr);
          eval.addInstr(instr1);
          
          break;
        default:
          break;
        }
      }
      
      // Now that last instruction has been added, compile
      eval.compile();
      
      return eval;
    }

    private IAEval getPlusEval(ExecContext ec, GroupAggrContext ctx)
    throws CEPException 
    {
      
      IAEval eval = AEvalFactory.create(ec);
      
      // Copy the group attributes from the new plus tuple to the new output
      // tuple
      copyGroupAttrs(eval, ctx);
      
      // handle aggregation attributes here
      handleAggr(ec, eval, ctx, (PhyOptGroupAggr)ctx.getPhyopt(),
          IEvalContext.INPUT_ROLE,
          IEvalContext.OLD_OUTPUT_ROLE, IEvalContext.NEW_OUTPUT_ROLE, 
          true);
      
      eval.compile();
      
      return eval;
    }

    private IAEval getUpdateEval(ExecContext ec, GroupAggrContext ctx)
    throws CEPException 
    {
      
      IAEval eval = AEvalFactory.create(ec);
      
      // handle aggregation attributes here
      handleAggr(ec, eval, ctx, (PhyOptGroupAggr)ctx.getPhyopt(),
          IEvalContext.INPUT_ROLE,
          IEvalContext.NEW_OUTPUT_ROLE, IEvalContext.NEW_OUTPUT_ROLE, 
          true);
      
      eval.compile();
      
      return eval;
    }

    private IAEval getMinusEval(ExecContext ec, GroupAggrContext ctx) 
    throws CEPException 
    {
      
      IAEval eval = AEvalFactory.create(ec);
      
      // Copy the group attributes from the new plus tuple to the new output
      // tuple
      copyGroupAttrs(eval, ctx);
      
      // handle aggregation attributes here
      handleAggr(ec, eval, ctx, (PhyOptGroupAggr)ctx.getPhyopt(),
          IEvalContext.INPUT_ROLE,
          IEvalContext.OLD_OUTPUT_ROLE, IEvalContext.NEW_OUTPUT_ROLE, false);
      
      eval.compile();
      
      return eval;
    }

    private IBEval getScanNotReqEval(ExecContext ec, GroupAggrContext ctx,
                                     IAEval aEval)
    throws CEPException 
    {
      
      IBEval eval;
      BInstr instr;
      int aggrIndex;
      
      eval = BEvalFactory.create(ec);
      AggrFunction[] fn = ctx.getAggrFunction();
      int numGroupAttrs = ctx.getNumGroupAttrs();
      Datatype[] types = ctx.getTypes();
      
      for (int i=0; i< ctx.getNumAggrParamExprs(); i++) {
        aggrIndex = i + numGroupAttrs;
        if (fn[i] == AggrFunction.MAX || fn[i] == AggrFunction.MIN) {
          
          int[] inpRoles = new int[1];
          inpRoles[0] = IEvalContext.INPUT_ROLE;
          ExprHelper.Addr addr = ExprHelper.instExpr(ec, 
              (ctx.getAggrParamExprs().get(i))[0],aEval,ctx.getEvalContextInfo(),inpRoles);
          
          instr = new BInstr();
          
          instr.op = ExprHelper.getNEOp(types[aggrIndex]);
          
          instr.r1 = IEvalContext.OLD_OUTPUT_ROLE;
          instr.c1 = new Column(aggrIndex);
          instr.e1 = null;
          instr.r2 = addr.role;
          instr.c2 = new Column(addr.pos);
          instr.e2 = null;
          
          eval.addInstr(instr);
        }
      }
      
      // Now that last instruction has been added, compile
      eval.compile();
      
      return eval;
    }

    private IBEval getEmptyGroupEval(ExecContext ec, GroupAggrContext ctx)
    throws CEPException 
    {
      
      IBEval eval;
      BInstr instr;
      
      eval = BEvalFactory.create(ec);
      instr = new BInstr();
      
      //Here Count_star is needed as it has to count even the null rows.
      instr.op = BOp.INT_EQ;
      instr.r1 = IEvalContext.OLD_OUTPUT_ROLE;
      instr.c1 = new Column(ctx.getCountStarPos() + ctx.getNumGroupAttrs());
      instr.e1 = null;
      instr.r2 = IEvalContext.CONST_ROLE;
      instr.c2 = new Column(ctx.getConstTupleSpec().addInt(1));
      instr.e2 = null;
      
      eval.addInstr(instr);
      
      // Now that last instruction has been added, compile
      eval.compile();
      
      return eval;
    }
    
    private void copyGroupAttrs(IAEval eval, GroupAggrContext ctx)
    throws CEPException 
    {
      AInstr instr;
      Datatype[] types = ctx.getTypes();
      int[] groupPos = ctx.getGroupPos();
      
      // Copy the group attributes from the new plus tuple to the new output
      // tuple
      for (int i=0; i< ctx.getNumGroupAttrs(); i++) {
        instr = new AInstr();
        instr.op = ExprHelper.getCopyOp(types[i]);
        instr.r1 = IEvalContext.INPUT_ROLE;
        instr.c1 = groupPos[i];
        instr.dr = IEvalContext.NEW_OUTPUT_ROLE;
        instr.dc = i;
        
        eval.addInstr(instr);
      }
    }
    
    
    private void handleAggr(ExecContext ec, IAEval eval, 
                            GroupAggrContext ctx, PhyOptGroupAggr aggrPhyOp,
                            int inputRole,
                            int oldOutputRole, int newOutputRole, boolean plus) 
        throws CEPException {
      
      AggrHelper.getIncrEval(ec, eval, inputRole, oldOutputRole, newOutputRole,
          plus, ctx.getEvalContextInfo(), ctx.getEvalContext(),
          ctx.getAggrFunction(), ctx.getAggrParamExprs(),
          ctx.getNumAggrParamExprs(), ctx.getAggrInpTypes(), ctx.getAggrTypes(),
          ctx.getSumPos(), ctx.getCountPos(), ctx.getNumGroupAttrs(),
          ctx.getUdaPos(), aggrPhyOp.getAggrFunctions(), 0,
          ctx.getXmlAggIndexPos(), ctx.getCompareSpecs(), 
          ctx.getOrderByExprs(), ctx.getOrderByTupleSpec());
    }

    private void initOutIndex(ExecContext ec, IEvalContext evalContext, 
                              GroupAggrContext ctx,
                              HashIndex index, IAllocator<ITuplePtr> factory) 
        throws CEPException {

        int[] posInIndexItem = new int[ctx.getNumGroupAttrs()];
        for (int i=0; i<ctx.getNumGroupAttrs(); i++)
            posInIndexItem[i] = i;

        setupIndex(ec, evalContext, ctx, index, posInIndexItem, 
                   ctx.getGroupPos(), factory);
    }

    private void initInIndex(ExecContext ec, IEvalContext evalContext,
                 GroupAggrContext ctx,
			     HashIndex index, IAllocator<ITuplePtr> factory) 
        throws CEPException {

        setupIndex(ec,evalContext, ctx, index, ctx.getGroupPos(), 
                   ctx.getGroupPos(), factory);
    }

    private void setupIndex(ExecContext ec, IEvalContext evalContext,
                            GroupAggrContext ctx,
                            HashIndex index, int[] posInIndexItem, 
			    int[] posInKey, IAllocator<ITuplePtr> factory) 
        throws CEPException 
    {
      
      IHEval updateHash;
      IHEval scanHash;
      IBEval keyEqual;
      HInstr hinstr;
      BInstr binstr;
      Datatype types[] = ctx.getTypes();
      
      // Setup update hash
      updateHash = HEvalFactory.create(ec, ctx.getNumGroupAttrs());
      for (int i=0; i<ctx.getNumGroupAttrs(); i++) {
        hinstr = new HInstr(types[i], IEvalContext.UPDATE_ROLE, 
            new Column(posInIndexItem[i]));
        updateHash.addInstr(hinstr);
      }
      updateHash.compile();
      
      // Setup scan hash
      scanHash = HEvalFactory.create(ec, ctx.getNumGroupAttrs());
      for (int i=0; i<ctx.getNumGroupAttrs(); i++) {
        hinstr = new HInstr(types[i], IEvalContext.INPUT_ROLE, 
            new Column(posInKey[i]));
        scanHash.addInstr(hinstr);
      }
      scanHash.compile();
      
      // Setup key equal
      keyEqual = BEvalFactory.create(ec, ctx.getNumGroupAttrs());
      for (int i=0; i<ctx.getNumGroupAttrs(); i++) {
        binstr = new BInstr();
        binstr.op = ExprHelper.getEqOp(types[i]);
        binstr.r1 = IEvalContext.INPUT_ROLE;
        binstr.c1 = new Column(posInKey[i]);
        binstr.e1 = null;
        binstr.r2 = IEvalContext.SCAN_ROLE;
        binstr.c2 = new Column(posInIndexItem[i]);
        binstr.e2 = null;
        
        keyEqual.addInstr(binstr);
      }
      keyEqual.compile();
      
      index.setEvalContext(evalContext);
      index.setUpdateHashEval(updateHash);
      index.setScanHashEval(scanHash);
      index.setKeyEqual(keyEqual);
      index.setFactory(factory);
      index.initialize();
    }
   

}
