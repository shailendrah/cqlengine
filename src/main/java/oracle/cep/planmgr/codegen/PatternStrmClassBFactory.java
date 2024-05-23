/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/PatternStrmClassBFactory.java /main/38 2012/07/25 21:19:31 pkali Exp $ */

/* Copyright (c) 2007, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       07/20/12 - setting prevRangeExists for bind synopsis
    sborah      01/24/11 - remove setEvalContext
    udeshmuk    03/09/10 - within clause support
    udeshmuk    12/17/09 - fix invalid attr error while using FIRST/LAST
    sborah      10/14/09 - support for bigdecimal
    udeshmuk    08/13/09 - pattern re-org
    udeshmuk    04/22/09 - set partncopyeval n corresponding evalcontext.
    udeshmuk    04/21/09 - set isNonevent in bindsynopsis.
    udeshmuk    04/01/09 - partn by without all matches
    parujain    03/19/09 - stateless server
    udeshmuk    03/17/09 - use partnsynopsis only when partnattrs and prev with
                           range are present
    udeshmuk    03/05/09 - remove project for measures
    udeshmuk    02/02/09 - support for duration arith_expr in pattern.
    udeshmuk    11/05/08 - rename pattern store to private store.
    udeshmuk    10/17/08 - support for xmlagg orderby in pattern.
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    hopark      10/16/08 - fix invalid cast exception - TuplePtrFactory
    udeshmuk    10/10/08 - use pattern specific syn/store.
    udeshmuk    09/24/08 - using the input tuple's factory for partition store.
    skmishra    09/18/08 - adding null check for orderExpr
    skmishra    09/16/08 - adding param to getInitEval
    skmishra    07/17/08 - changing signatures for xmlagg orderby
    udeshmuk    07/12/08 - 
    rkomurav    07/07/08 - add isrecurringnon event flag
    rkomurav    05/15/08 - add fields for non event detection
    rkomurav    03/27/08 - rename alphSize to numCorrs
    rkomurav    03/20/08 - support subset
    hopark      02/29/08 - fix nullInputTuple creation
    rkomurav    02/25/08 - set alphSize for bind syn
    rkomurav    02/21/08 - replace DFA with NFA
    rkomurav    01/03/08 - remove stateToAlphMap
    hopark      12/07/07 - cleanup spill
    rkomurav    09/27/07 - support non mandatory correlation defs
    anasrini    09/26/07 - ALL MATCHES support
    rkomurav    09/20/07 - bind null input role
    rkomurav    09/12/07 - add partition store for partition by prev support
    hopark      09/04/07 - eval optimize
    rkomurav    09/06/07 - support prev(A.c1.n)
    hopark      07/27/07 - TupleSpec cannot be changed after TupleFactory is
                           created.
    rkomurav    07/25/07 - fix outstore
    rkomurav    07/03/07 - uda
    anasrini    07/12/07 - support for partition by
    rkomurav    06/11/07 - fix creation of init and incr evals
    rkomurav    05/15/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/PatternStrmClassBFactory.java /main/38 2012/07/25 21:19:31 pkali Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.codegen;

import java.util.ArrayList;

import oracle.cep.common.AggrFunction;
import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.BaseAggrFn;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.common.UserDefAggrFn;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.comparator.ComparatorSpecs;
import oracle.cep.execution.indexes.HashIndex;
import oracle.cep.execution.internals.AInstr;
import oracle.cep.execution.internals.AOp;
import oracle.cep.execution.internals.BInstr;
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
import oracle.cep.execution.operators.PatternStrmClassB;
import oracle.cep.execution.stores.BindStore;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.stores.PrivatePartnWindowStoreImpl;
import oracle.cep.execution.synopses.BindingSynopsisImpl;
import oracle.cep.execution.synopses.PrivatePartnWindowSynopsisImpl;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptPatternStrmClassB;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.phyplan.attr.CorrAttr;
import oracle.cep.phyplan.expr.BoolExpr;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprOrderBy;
import oracle.cep.phyplan.pattern.CorrName;
import oracle.cep.phyplan.pattern.CorrNameDef;
import oracle.cep.phyplan.pattern.SubsetCorr;
import oracle.cep.planmgr.codegen.ExprHelper.Addr;
import oracle.cep.service.ExecContext;

public class PatternStrmClassBFactory extends ExecOptFactory
{

  @Override
  public CodeGenContext createCodeGenContext(ExecContext ec, Query query, 
                                             PhyOpt phyopt)
  {
     return new PatternStrmClassContext(ec, query, phyopt); 
  }
  
  ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException
  {
    PhyOpt op = ctx.getPhyopt();
    assert ctx instanceof PatternStrmClassContext;
    PatternStrmClassContext pctx = (PatternStrmClassContext)ctx;
    assert op instanceof PhyOptPatternStrmClassB;
    ExecContext ec = ctx.getExecContext();
    initPatternInfo(ec, pctx, op);
    return new PatternStrmClassB(ctx.getExecContext());
  }
  
  void setupExecOpt(CodeGenContext ctx) throws CEPException
  {  
    assert ctx instanceof PatternStrmClassContext;
    PatternStrmClassContext pctx = (PatternStrmClassContext)ctx;
    assert ctx.getExecOpt() instanceof PatternStrmClassB;
    PhyOpt op = ctx.getPhyopt();
    PhyOptPatternStrmClassB phyPattStrmClassB = (PhyOptPatternStrmClassB) op;

    ExecContext ec = ctx.getExecContext();
    PatternStrmClassB pattStrmClassB = (PatternStrmClassB)ctx.getExecOpt();
    IEvalContext evalContext  = EvalContextFactory.create(ec);
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    EvalContextInfo evalCtxInfo  = new EvalContextInfo(factoryMgr);
    pctx.setEvalContext(evalContext);
    pctx.setEvalContextInfo(evalCtxInfo);
    
    CorrNameDef[] corrDefs = phyPattStrmClassB.getCorrDefs();
    pctx.setCorrDefs(corrDefs);
    
    int numCorrDefs  = corrDefs.length;
    pctx.setNumCorrDefs(numCorrDefs);
    
    SubsetCorr[] subsetCorrs  = phyPattStrmClassB.getSubsetCorrs();
    pctx.setSubsetCorrs(subsetCorrs);
    
    int maxPrevIndex = phyPattStrmClassB.getMaxPrevIndex();
    pctx.setMaxPrevIndex(maxPrevIndex);
    
    int numSubsetCorrs = 0;
    
    if(subsetCorrs != null) 
      numSubsetCorrs = subsetCorrs.length;
    
    pctx.setNumSubsetCorrs(numSubsetCorrs);

    IBEval[] defs  = new IBEval[numCorrDefs];
    int[] inpRoles = new int[1];

    //mapping from correlation name to binding pos
    int bindLength = phyPattStrmClassB.getBindLength();
    pctx.setBindLength(bindLength);
    int bindRole;
    int prevRole;
    
    //add dynamic roles for both both bindings and prev(n)
    if(maxPrevIndex > 1)
    {
      bindRole = evalContext.addRoles(bindLength + maxPrevIndex);
      prevRole = bindRole + bindLength;
    }
    else
    {
      bindRole = evalContext.addRoles(bindLength);
      prevRole = IEvalContext.PREV_INPUT_ROLE;
    }
    pctx.setBindRole(bindRole);
    pctx.setPrevRole(prevRole);
      
    // Handle the PARTITION clause now
    // NOTE: Can handle this only after evalContext.addRoles have been called
    if (pctx.getHasPartnAttrs())
    {
      // Get the index to locate the prevTuple for each partition
      HashIndex partnIndex = getPartnIndex(ec, pctx, op, evalContext, 
                                 getInputStore(op, ec, 0).getFactory());
      pctx.setPartnIndex(partnIndex);
    }
    
    // Eval Context role number for the aggregate tuple
    int aggrRole   = bindRole + bindLength - 1;
    pctx.setAggrRole(aggrRole);
                                                
    //inpRoles is not looked at for classB define expressions' conversion
    //its a dummy assignment
    inpRoles[0] = IEvalContext.INPUT_ROLE;
    
    //construct BEvals corresponding to DEFINE predicates
    //classB define expressions read input from dynamic bind roles
    //definition corresponding to undefined correlation name is null
    for(int i = 0; i < numCorrDefs; i++)
    {
      BoolExpr boolExpr = corrDefs[i].getBExpr();
      if(boolExpr == null)
      {
        defs[i] = null;
        continue;
      }
      defs[i] = BEvalFactory.create(ec);
      ExprHelper.instBoolExpr(ec, boolExpr, defs[i], evalCtxInfo,
                              false, inpRoles, bindRole, prevRole);
      defs[i].compile();
    }
    
    //populate subset pos
    int[][] subsetPos = new int[numCorrDefs][];
    int[] posArr;
    for(int i = 0; i < numCorrDefs; i++)
    {
      posArr = corrDefs[i].getSubsetPos();
      if(posArr != null)
      {
        subsetPos[i] = new int[posArr.length];
        System.arraycopy(posArr, 0, subsetPos[i], 0, posArr.length);
      }
    }
    pctx.setSubsetPos(subsetPos);
    
    //process MEASURES  
    Expr[] measureExprs = phyPattStrmClassB.getMeasureExprs();
    pctx.setMeasureExprs(measureExprs);
    IAEval measureEval = AEvalFactory.create(ec);
    for(int i=0; i < measureExprs.length; i++)
    {
      ExprHelper.instExprDest(ec, measureExprs[i], measureEval, evalCtxInfo,
                              IEvalContext.NEW_OUTPUT_ROLE, i, inpRoles, bindRole, prevRole);
    }
    measureEval.compile();
    
    // Handle the aggregations
    handleAggrs(ec, pctx);
    
    //process DURATION clause
    IAEval durationEval = null;
    Addr durAddr = null;
    if(phyPattStrmClassB.isDurationExpr())
    {
      Expr durationExpr = phyPattStrmClassB.getDurationExpr();
      durationEval = AEvalFactory.create(ec);
      durAddr = ExprHelper.instExpr(ec, durationExpr, durationEval,
                                    evalCtxInfo, inpRoles, bindRole, prevRole); 
      durationEval.compile();
    }
    
    //Scratch tuple
    TupleSpec st = evalCtxInfo.st;
    if(st != null)
    {
      IAllocator stf = factoryMgr.get(st);
      ITuplePtr tPtr = (ITuplePtr)stf.allocate(); //SCRATCH_TUPLE
      evalContext.bind(tPtr, IEvalContext.SCRATCH_ROLE);
    }
    
    //Constant tuple
    ConstTupleSpec ct = evalCtxInfo.ct;
    if(ct != null)
    {
      IAllocator ctf = factoryMgr.get(ct.getTupleSpec());
      ITuplePtr tPtr   = (ITuplePtr)ctf.allocate(); //SCRATCH_TUPLE
      ct.populateTuple(ec, tPtr);
      evalContext.bind(tPtr, IEvalContext.CONST_ROLE);
    }
    
    ITuplePtr nullInputTuple = getNullInputTuple(ec, op);
    evalContext.bind(nullInputTuple, IEvalContext.NULL_INPUT_ROLE);
    
    //Construct the synopsis
    PhySynopsis phySyn = phyPattStrmClassB.getBindSyn();
    assert phySyn != null;
    assert phySyn.getKind() == SynopsisKind.BIND_SYN : phySyn.getKind();

    ObjectFactoryContext allCtx = new ObjectFactoryContext(ec);
    allCtx.setOpt(op);
    allCtx.setObjectType(BindingSynopsisImpl.class.getName());
    BindingSynopsisImpl bindSyn = 
      (BindingSynopsisImpl)ObjectManager.allocate(allCtx);

    phySyn.setSyn(bindSyn);
    ExecStore bindStore;
    bindStore = StoreInst.instStore(new StoreGenContext(ec, phySyn.getStwstore()));
    bindSyn.setStore((BindStore) bindStore);
    
    bindSyn.setBindLength(bindLength);
    bindSyn.setNumCorrs(phyPattStrmClassB.getNfa().getNumAlphabets() + 
        numSubsetCorrs);
    bindSyn.setAggrTupleFactory(pctx.getAtf());
    bindSyn.setNullInputTuple(nullInputTuple);
    bindSyn.setMaxPrevIndex(maxPrevIndex);
    bindSyn.setHasPartnAttrs(pctx.getHasPartnAttrs());
    bindSyn.setPrevRangeExists(phyPattStrmClassB.isPrevRangeExists());
    bindSyn.setIsNonEvent(phyPattStrmClassB.isNonEvent());
    if(phyPattStrmClassB.isNonEvent())
      bindSyn.setIsVariableDuration(phyPattStrmClassB.isDurationExpr());
    bindSyn.setPatternSkip(phyPattStrmClassB.getSkipClause());
    bindSyn.initialize();
    
    PrivatePartnWindowSynopsisImpl partnSyn = null;
    
    // Construct partition synopsis
    if((maxPrevIndex > 0) && (pctx.getHasPartnAttrs())
            && (phyPattStrmClassB.isPrevRangeExists()))
    {
      phySyn = phyPattStrmClassB.getPartnSyn();
      assert phySyn != null;
      assert phySyn.getKind() == SynopsisKind.PRIVATE_PARTN_WIN_SYN : phySyn.getKind();
      
      assert pctx.getNumPartnAttrs() > 0;
      assert pctx.getPartnAttrs().length > 0;
      assert phySyn.getStwstore() != null;
      
      allCtx.setObjectType(PrivatePartnWindowSynopsisImpl.class.getName());
      partnSyn = (PrivatePartnWindowSynopsisImpl)ObjectManager.allocate(allCtx);
      
      TupleSpec ts = CodeGenHelper.getTupleSpec(ec,phyPattStrmClassB.getInputs()[0]);
      //copy eval has to be generated before forming the store because creation
      //of store adds few attrs that won't be present in the input tuple
      IAEval partnCopyEval = getCopyEval(ec, ts);
      StoreGenContext sgc = new StoreGenContext(ec, phySyn.getStwstore(), ts,
          pctx.getNumPartnAttrs(), pctx.getPartnAttrs()); 
      PrivatePartnWindowStoreImpl partnStore = (PrivatePartnWindowStoreImpl) StoreInst.instStore(sgc);
      
      IAllocator<ITuplePtr> ptf = sgc.getPartnTupleFac();
      pctx.setPtf(ptf);
      
      partnSyn.setStore(partnStore);
      int partnStubId = partnStore.addStub();
      partnStore.setPrimaryStub(partnStubId);
      partnSyn.setStubId(partnStubId);
      partnSyn.setNumRows(maxPrevIndex);
      partnSyn.setSupportRangeFunctionality(phyPattStrmClassB.isPrevRangeExists());
      partnSyn.setTimeRange(phyPattStrmClassB.getMaxPrevRange());
      IEvalContext partnCopyEvalContext = EvalContextFactory.create(ec);
      pctx.setPartnCopyEvalContext(partnCopyEvalContext);
      pctx.setPartnCopyEval(partnCopyEval);
    }
    
    pattStrmClassB.setBindSyn(bindSyn);
    pattStrmClassB.setNullInputTuple(nullInputTuple);
    pattStrmClassB.setSkipClause(phyPattStrmClassB.getSkipClause());
    pattStrmClassB.setNonEvent(phyPattStrmClassB.isNonEvent());
    pattStrmClassB.setIsVariableDuration(phyPattStrmClassB.isDurationExpr());
    pattStrmClassB.setWithin(phyPattStrmClassB.isWithin());
    pattStrmClassB.setWithinInclusive(phyPattStrmClassB.isWithinInclusive());
    //Fields needed in initialize of patternop are set now, so call initialize
    pattStrmClassB.initialize();
    
    pattStrmClassB.setHasPartnAttrs(pctx.getHasPartnAttrs());
    pattStrmClassB.setMeasureEval(measureEval);
    pattStrmClassB.setNullEval(pctx.getNullEval());
    pattStrmClassB.setInitEvals(pctx.getInitEvals());
    pattStrmClassB.setIncrEvals(pctx.getIncrEvals());
    pattStrmClassB.setBindRole(bindRole);
    pattStrmClassB.setEvalContext(evalContext);
    pattStrmClassB.setDefs(defs);
    pattStrmClassB.setNfa(phyPattStrmClassB.getNfa());
    pattStrmClassB.setHasAggrs(pctx.getHasAggrs());
    pattStrmClassB.setReleaseEval(pctx.getReleaseEval());
    pattStrmClassB.setNumUDA(pctx.getNumUDA());
    pattStrmClassB.setMaxPrevIndex(maxPrevIndex);
    pattStrmClassB.setPrevRole(prevRole);
    pattStrmClassB.setPartnSyn(partnSyn);
    pattStrmClassB.setPartnCopyEval(pctx.getPartnCopyEval());
    pattStrmClassB.setPartnCopyEvalContext(pctx.getPartnCopyEvalContext());
    pattStrmClassB.setPrevRangeExists(phyPattStrmClassB.isPrevRangeExists());
    pattStrmClassB.setSubsetPos(subsetPos);
    pattStrmClassB.setDurationValue(phyPattStrmClassB.getDurationValue());
    
    if(phyPattStrmClassB.isNonEvent())
    { //Non-event related variables
      pattStrmClassB.setDurationSymAlphIndex(
          phyPattStrmClassB.getDurationSymAlphIndex());
      pattStrmClassB.setRecurringNonEvent(
          phyPattStrmClassB.isRecurringNonEvent());
      pattStrmClassB.setIsDurationExpr(phyPattStrmClassB.isDurationExpr());
      pattStrmClassB.setDurationEval(durationEval);
      if(phyPattStrmClassB.isDurationExpr())
      {
        pattStrmClassB.setDurationRole(durAddr.role);
        pattStrmClassB.setDurationPos(durAddr.pos);
      }
      else
      {
        pattStrmClassB.setDurationRole(-1);
        pattStrmClassB.setDurationPos(-1);
      }
      pattStrmClassB.setDurationUnit(phyPattStrmClassB.getDurationUnit());
    }
    
    //set the aggregation tuple factory
    pattStrmClassB.setAggrTupleFactory(pctx.getAtf());
    
    //set the partition tuple factory
    pattStrmClassB.setPartnTupleFactory(pctx.getPtf());
    
    //setters for xmlagg
    pattStrmClassB.setOrderByAllocator(pctx.getOrderByAllocator());
    pattStrmClassB.setNumXmlAgg(pctx.getNumXmlAgg());
    pattStrmClassB.setReleaseIndexEval(pctx.getReleaseIndexEval());
    
  }

  private void initPatternInfo(ExecContext ec, PatternStrmClassContext ctx, 
                               PhyOpt op) 
  throws CEPException
  {

    BaseAggrFn[] aggFns;
    PhyOptPatternStrmClassB phyPattStrmClassB = (PhyOptPatternStrmClassB) op;   

    int numAggrParamExprs = phyPattStrmClassB.getNumAggrAttrs();
    ctx.setNumAggrParamExprs(numAggrParamExprs);
    
    int numAggrAttrs      = numAggrParamExprs;
    ctx.setNumAggrAttrs(numAggrAttrs);
    
    ctx.setAggrParamExprs(phyPattStrmClassB.getAggrParamExprs());
    
    aggFns            = phyPattStrmClassB.getAggrFns();
    AggrFunction[] fn = new AggrFunction[numAggrParamExprs];
    ctx.setAggrFunction(fn);
    
    Datatype[] aggrOutputTypes   = phyPattStrmClassB.getAggrOutputTypes();
    ctx.setAggrOutputTypes(aggrOutputTypes);
    
    CorrAttr[] aggrAttrs  = phyPattStrmClassB.getAggrAttrs();
    ctx.setAggrAttrs(aggrAttrs);
   
    AttributeMetadata[] attrMetadata = op.getAttrMetadata();
    
    Datatype[] types = new Datatype[attrMetadata.length];
    
    for(int i = 0 ; i < attrMetadata.length; i ++)
    {
      types[i] = attrMetadata[i].getDatatype();
    }
    
    ctx.setTypes(types);
    
    ArrayList<ExprOrderBy[]> orderByExprsList  = 
                        phyPattStrmClassB.getOrderByExprs();
    ctx.setOrderByExprsList(orderByExprsList);
    
    // Handle PARTITION BY clause
    ctx.setHasPartnAttrs(phyPattStrmClassB.hasPartnAttrs());
    ctx.setNumPartnAttrs(phyPattStrmClassB.getNumPartnAttrs());
    ctx.setPartnAttrs(phyPattStrmClassB.getPartnAttrs());
    
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    TupleSpec ats = null;
    
    if (numAggrParamExprs > 0) {
      ats = new TupleSpec(factoryMgr.getNextId());
      for (int i=0; i<numAggrParamExprs; i++)
      {
        
        if(aggrOutputTypes[i] == Datatype.BIGDECIMAL)
          ats.addAttr(new AttributeMetadata(aggrOutputTypes[i], 0, 
                                            Datatype.BIGDECIMAL.getPrecision(),
                                            0));
        else if((aggrOutputTypes[i] == Datatype.CHAR) ||
                (aggrOutputTypes[i] == Datatype.BYTE))
        {
          assert ((aggFns[i].getFnCode() == AggrFunction.FIRST) ||
                  (aggFns[i].getFnCode() == AggrFunction.LAST)  ||
                  (aggFns[i].getFnCode() == AggrFunction.USER_DEF)): "Aggr " +
                  "Function other than FIRST/LAST/USER_DEF cannot return " +
                  "variable length type: "+aggFns[i].getFnCode();
          
          int attrLength = 0;
          if(aggFns[i].getFnCode() == AggrFunction.USER_DEF)
          { //here we can have multiple arguments to the USER_DEF, so not sure
            //which one to use to find length of attr
            if(aggrOutputTypes[i] == Datatype.CHAR)
              attrLength = Constants.MAX_CHAR_LENGTH;
            else
              attrLength = Constants.MAX_BYTE_LENGTH;
          }
          else //first or last aggr functions
          {
            assert phyPattStrmClassB.getAggrParamExprs().get(i).length == 1;
            attrLength = phyPattStrmClassB.getAggrParamExprs().
                                           get(i)[0].getLength();
          }           
          ats.addAttr(new AttributeMetadata(aggrOutputTypes[i], attrLength, 
                                            0, 0));
        }
        else
        {
          ats.addAttr(new AttributeMetadata(aggrOutputTypes[i], 0, 0, 0));
        }
      }
      ctx.setAts(ats);
    }

    int numIncrUDA = 0;
    int numFullUDA = 0;
    int numXMLAgg = 0;

    for (int i=0; i<numAggrParamExprs; i++) {
      fn[i] = aggFns[i].getFnCode();
      if(fn[i] == AggrFunction.USER_DEF)
      {
        if(aggFns[i].supportsIncremental())
          numIncrUDA++;
        else
          numFullUDA++;
      }
      if(fn[i] == AggrFunction.XML_AGG)
      {
        numXMLAgg++;
      }
    }
    int numUDA = numIncrUDA + numFullUDA;
    ctx.setNumXmlAgg(numXMLAgg);
    
    if(numUDA > 0)
    {
      if(ats == null)
      {
        ats = new TupleSpec(factoryMgr.getNextId());
        ctx.setAts(ats);
      }
      UserDefAggrFn[] uda = new UserDefAggrFn[numAggrParamExprs];
      int[] udaPos  = new int[numAggrParamExprs];
      IAggrFnFactory[] udaFactory = new IAggrFnFactory[numAggrParamExprs];
      IAggrFunction[] udaHandler = new IAggrFunction[numAggrParamExprs];
      AggrHelper.initUDA(ec, numUDA, aggFns, uda, udaPos, udaFactory, udaHandler,
          numAggrParamExprs, fn, false, false, ats, null);
      ctx.setUserDefAggrFn(uda);
      ctx.setUdaFactory(udaFactory);
      ctx.setUdaHandler(udaHandler);
      ctx.setUdaPos(udaPos);
    }
    
    ctx.setNumUDA(numUDA);
    ctx.setNumIncrUDA(numIncrUDA);
    ctx.setNumFullUDA(numFullUDA);

    // Determine the SUM and COUNT positions for each AVG
    int[] sumPos   = new int[numAggrAttrs];
    int[] countPos = new int[numAggrAttrs];
    AggrHelper.getSumCountPos(sumPos, countPos, fn, ctx.getAggrParamExprs(),
                              numAggrParamExprs, 0);
    
    ctx.setSumPos(sumPos);
    ctx.setCountPos(countPos);
    
    if(numXMLAgg > 0)
    {
      int[] xmlAggIndexPos = new int[numAggrParamExprs];
      ComparatorSpecs[][] compareSpecs = new ComparatorSpecs[numAggrParamExprs][];
      for(int j=0;j<numAggrParamExprs;j++)
      {
        xmlAggIndexPos[j] = -1;
        compareSpecs[j]   = null;
      }
      for(int j=0; j < orderByExprsList.size(); j++)
      {
        if(orderByExprsList.get(j) != null)
          assert (orderByExprsList.get(j)).length != 0;
      }
      TupleSpec orderByTupleSpec = new TupleSpec(factoryMgr.getNextId());
      /*
       * One OBJECT attribute per xmlagg (pointing to the sorted list of
       * that xmlagg) is added in 'ats'
       */ 
      AggrHelper.initXmlAgg(numAggrParamExprs, fn, orderByExprsList,
                            xmlAggIndexPos, compareSpecs, ats,
                            orderByTupleSpec);
      if(orderByTupleSpec.getNumAttrs() > 0)
      {
       IAllocator<ITuplePtr> orderByAllocator = factoryMgr.get(orderByTupleSpec);
       ctx.setOrderByAllocator(orderByAllocator);
      }
      ctx.setXmlAggIndexPos(xmlAggIndexPos);
      ctx.setCompareSpecs(compareSpecs);
      ctx.setOrderByTupleSpec(orderByTupleSpec);
    }
    //Create the factory for the aggregate tuple
    if (numAggrParamExprs > 0) {
      IAllocator<ITuplePtr> atf = factoryMgr.get(ats);
      ctx.setAtf(atf);
    }
    
  }
  
  private void handleAggrs(ExecContext ec, PatternStrmClassContext ctx)
  throws CEPException
  {
    int                   aggrBindPos;
    int numAggrParamExprs = ctx.getNumAggrParamExprs();

    // Create the single NULL IAEval
    IAEval nullEval = 
      AggrHelper.getNullOutputEval(ec, ctx.getAggrRole(), 
                                   ctx.getEvalContextInfo(), 
                                   ctx.getAggrFunction(), 
                                   numAggrParamExprs);
    nullEval.compile();
    ctx.setNullEval(nullEval);

    int numCorrDefs = ctx.getNumCorrDefs();
    int numSubsetCorrs = ctx.getNumSubsetCorrs();
    CorrNameDef[] corrDefs = ctx.getCorrDefs();
    SubsetCorr[] subsetCorrs = ctx.getSubsetCors();
    
    
    // Create an init and incr IAEval for each correlation name
    boolean[] hasAggrs = new boolean[numCorrDefs + numSubsetCorrs];
    ctx.setHasAggr(hasAggrs);
    
    IAEval[] initEvals = new IAEval[numCorrDefs + numSubsetCorrs];
    IAEval[] incrEvals = new IAEval[numCorrDefs + numSubsetCorrs];
    
    ctx.setInitEvals(initEvals);
    ctx.setIncrEvals(incrEvals);
    
    aggrBindPos = 0;
 
    for(int i = 0; i < numCorrDefs; i++)
    {
      aggrBindPos = getInitIncrEvals(ec, ctx, corrDefs[i], i, aggrBindPos);
    }
    for(int i = 0; i < numSubsetCorrs; i++)
    {
      aggrBindPos = getInitIncrEvals(ec, ctx, subsetCorrs[i], i + numCorrDefs, aggrBindPos);
    }
    
    if(ctx.getNumUDA() > 0)
    {
      ArrayList<Integer> compressUdaPos = new ArrayList<Integer>();
      ArrayList<IAggrFnFactory> compressUdaFactory = new ArrayList<IAggrFnFactory>();
      ArrayList<IAggrFunction> compressUdaHandler = new ArrayList<IAggrFunction>();
      AggrHelper.compressUdaInfo(ctx.getUdaPos(), ctx.getUdaFactory(), 
                                 ctx.getUdaHandler(), compressUdaPos,
        compressUdaFactory, compressUdaHandler, ctx.getAggrFunction());
      Integer[] temp = compressUdaPos.toArray(new Integer[1]);
      int[] posArr;
      posArr = new int[temp.length];
      for(int i = 0; i <  temp.length; i++)
      {
        posArr[i] = temp[i].intValue();
      }
      IAEval releaseEval = AggrHelper.getReleaseHandlerEval(ec, posArr,
          compressUdaFactory.toArray(new IAggrFnFactory[1]));
      releaseEval.compile();
      ctx.setReleaseEval(releaseEval);
    }
    
    IAEval releaseIndexEval = null;
    if(ctx.getNumXmlAgg() > 0)
    {
      releaseIndexEval = AggrHelper.getReleaseIndexEval(ec, ctx.getXmlAggIndexPos());
      releaseIndexEval.compile();
    }
    ctx.setReleaseIndexEval(releaseIndexEval);
    
    Datatype[] aggrOutputTypes = ctx.getAggrOutputTypes();

    // Create the aggr copy eval and eval context
    if (numAggrParamExprs > 0) {
      IEvalContext aggrCopyEvalContext = EvalContextFactory.create(ec);
      IAEval aggrCopyEval        = AEvalFactory.create(ec);
      for (int i=0; i<numAggrParamExprs; i++) {
        AInstr instr = new AInstr();
        AOp    aop = ExprHelper.getCopyOp(aggrOutputTypes[i]);

        instr.op = aop;
        instr.r1 = IEvalContext.INPUT_ROLE;
        instr.c1 = i;
        instr.dr = IEvalContext.NEW_OUTPUT_ROLE;
        instr.dc = i;
        aggrCopyEval.addInstr(instr);
      }
      
      for(int i = numAggrParamExprs; i < numAggrParamExprs + ctx.getNumUDA(); i++)
      {
        AInstr instr = new AInstr();
         
        instr.op = AOp.UDA_HANDLER_CPY;
        instr.r1 = IEvalContext.INPUT_ROLE;
        instr.c1 = i;
        instr.dr = IEvalContext.NEW_OUTPUT_ROLE;
        instr.dc = i;
        aggrCopyEval.addInstr(instr);
      }
      
      //NOTE: aggrCopyEval is not used anywhere in the pattern exec operator
      //Its usage is replaced by directly calling copy on tuples. So nothing
      //is added here for copying object attrs introduced by xmlagg
      
      aggrCopyEval.compile();
      ctx.setAggrCopyEval(aggrCopyEval);
      ctx.setAggrCopyEvalContext(aggrCopyEvalContext);
    }
  }
  
  private int getInitIncrEvals(ExecContext ec, PatternStrmClassContext ctx,
                               CorrName corr, int index, int aggrBindPos)
      throws CEPException
  {
    int                   numAggrs;
    ArrayList<Datatype[]> inpTypes;
    Datatype[]            outTypes;
    ArrayList<Expr[]>     paramExprs;
    AggrFunction[]        fns;
    BaseAggrFn[]          baseFns;
    //Needed for xmlagg
    int[]                    xmlAggIndexPosForThisCorr;
    ComparatorSpecs[][]      compareSpecsForThisCorr;
    ArrayList<ExprOrderBy[]> orderByExprsListForThisCorr;
    ArrayList<ExprOrderBy[]> orderByExprsList = ctx.getOrderByExprsList();
    IAEval[] initEvals = ctx.getInitEvals();
    IAEval[] incrEvals = ctx.getIncrEvals();
    ComparatorSpecs[][] compareSpecs = ctx.getCompareSpecs();
    
    numAggrs = corr.getNumAggrs();
    boolean[] hasAggrs = ctx.getHasAggrs();
    
    if (numAggrs > 0) {
      hasAggrs[index] = true;
      inpTypes   = corr.getAggrInputTypes();
      outTypes   = corr.getAggrOutputTypes();
      paramExprs = corr.getAggrParamExprs();
      fns        = corr.getAggrFunctions();
      baseFns    = corr.getAggrFns();
      if(ctx.getNumXmlAgg() > 0)
      {
        xmlAggIndexPosForThisCorr = new int[numAggrs];
        compareSpecsForThisCorr   = new ComparatorSpecs[numAggrs][];
        orderByExprsListForThisCorr = new ArrayList<ExprOrderBy[]>();
        int[] xmlAggIndexPos = ctx.getXmlAggIndexPos();
      
        for(int k=0;k < numAggrs; k++)
        {
          xmlAggIndexPosForThisCorr[k] = xmlAggIndexPos[aggrBindPos+k];
          compareSpecsForThisCorr[k]   = compareSpecs[aggrBindPos+k];
          orderByExprsListForThisCorr.add(orderByExprsList.get(aggrBindPos+k));
        }
      }
      else
      {
        xmlAggIndexPosForThisCorr = new int[numAggrs];
        for(int k=0; k < numAggrs; k++)
        {
          xmlAggIndexPosForThisCorr[k] = -1;
        }
        compareSpecsForThisCorr = null;
        orderByExprsListForThisCorr = null;
      }

      initEvals[index] = AEvalFactory.create(ec);
            
      AggrHelper.getInitEval(ec, initEvals[index], IEvalContext.INPUT_ROLE, 
                             ctx.getAggrRole(), ctx.getAggrRole(), 
                             ctx.getBindRole(), 
                             ctx.getEvalContextInfo(), 
                             ctx.getEvalContext(), fns, paramExprs, numAggrs,
                             inpTypes, outTypes, aggrBindPos, baseFns, 
                             ctx.getUdaPos(), aggrBindPos, ctx.getUdaFactory(), 
                             ctx.getUdaHandler(),
                             true, ctx.getPrevRole(), true, xmlAggIndexPosForThisCorr, 
                             orderByExprsListForThisCorr, compareSpecsForThisCorr,
                             ctx.getOrderByTupleSpec());
      
      initEvals[index].compile();

      incrEvals[index] = AEvalFactory.create(ec);
      AggrHelper.getIncrEval(ec, incrEvals[index], IEvalContext.INPUT_ROLE,
                             ctx.getAggrRole(), ctx.getAggrRole(), 
                             ctx.getBindRole(), true,
                             ctx.getEvalContextInfo(), ctx.getEvalContext(), 
                             fns, paramExprs,
                             numAggrs, inpTypes, outTypes, ctx.getSumPos(), 
                             ctx.getCountPos(),
                             aggrBindPos, ctx.getUdaPos(), baseFns, aggrBindPos, 
                             ctx.getPrevRole(), xmlAggIndexPosForThisCorr,
                             compareSpecsForThisCorr, 
                             orderByExprsListForThisCorr,
                             ctx.getOrderByTupleSpec());
      
      incrEvals[index].compile();
      
      aggrBindPos = aggrBindPos + numAggrs;
    }
    else
      hasAggrs[index] = false;
    
    ctx.setInitEvals(initEvals);
    ctx.setIncrEvals(incrEvals);
    ctx.setHasAggr(hasAggrs);
    
    return aggrBindPos;
  }
  
  private ITuplePtr getNullInputTuple(ExecContext ec, PhyOpt op) throws ExecException
  {
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    TupleSpec spec = new TupleSpec(factoryMgr.getNextId(), op.getInputs()[0]);
    ExecOpt opt = op.getInputs()[0].getInstOp();
    IAllocator stf = opt.getTupleStorageAlloc();
    /*
    IAllocator stf = FactoryManager.get(spec);
    */
    //allocate pins in write mode
    ITuplePtr tPtr = (ITuplePtr)stf.allocate(); //SCRATCH_TUPLE
    
    ITuple t = (ITuple)tPtr.pinTuple(IPinnable.WRITE);
    t.init(spec, true /* nullValue */);
    tPtr.unpinTuple();
    return tPtr;
  }
  
  private HashIndex getPartnIndex(ExecContext ec, PatternStrmClassContext ctx, 
                                  PhyOpt op,
                                  IEvalContext evalCtx, IAllocator<ITuplePtr> factory) 
    throws CEPException
  {
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    HashIndex partnIndex = new HashIndex(ec);
    HInstr    hInstr;
    IHEval    updHash;
    IHEval    scanHash;
    IBEval    keyEqual;
    BInstr    bInstr;
    TupleSpec inpTupSpec = new TupleSpec(factoryMgr.getNextId(), op.getInputs()[0]);
    Attr      phyAttr;
    int       pos;
    int numPartnAttrs = ctx.getNumPartnAttrs();
    Attr[] partnAttrs = ctx.getPartnAttrs();
    
    updHash = HEvalFactory.create(ec, numPartnAttrs);
    
    for (int attr = 0; attr < numPartnAttrs; attr++)
    {
      phyAttr = partnAttrs[attr];
      pos     = phyAttr.getPos();
      hInstr  = new HInstr(inpTupSpec.getAttrType(pos), 
                           IEvalContext.UPDATE_ROLE, 
                           new Column(pos));
      
      updHash.addInstr(hInstr);
    }
    updHash.compile();
    
    scanHash = HEvalFactory.create(ec, numPartnAttrs);
   
    for (int attr = 0; attr < numPartnAttrs; attr++)
    {
      phyAttr = partnAttrs[attr];
      pos     = phyAttr.getPos();
      hInstr  = new HInstr(inpTupSpec.getAttrType(pos), 
                           IEvalContext.INPUT_ROLE, 
                           new Column(pos));
      
      scanHash.addInstr(hInstr);
    }
    
    scanHash.compile();
    
    keyEqual = BEvalFactory.create(ec);
    for (int attr = 0; attr < numPartnAttrs; attr++)
    {
      phyAttr = partnAttrs[attr];
      pos     = phyAttr.getPos();

      bInstr  = new BInstr();
      // compare the attributes in the INPUT tuple with the tuple
      // in the scan role
      bInstr.op = ExprHelper.getEqOp(inpTupSpec.getAttrType(pos));
      bInstr.r1 = IEvalContext.INPUT_ROLE;
      bInstr.c1 = new Column(pos);
      
      bInstr.r2 = IEvalContext.SCAN_ROLE;
      bInstr.c2 = new Column(pos);
      
      keyEqual.addInstr(bInstr);
    }

    keyEqual.compile();
    
    partnIndex.setUpdateHashEval(updHash);
    partnIndex.setScanHashEval(scanHash);
    partnIndex.setKeyEqual(keyEqual);
    partnIndex.setEvalContext(evalCtx);
    partnIndex.setFactory(factory);
    partnIndex.initialize();
    
    return partnIndex;
  }
  
  /**
   * Get the evaluator instance to copy the Input tuple
   * @param ec TODO
   * @param ts  tuple specication for the copy operatir
   *
   * @return
   * @throws ExecException
   */
  private IAEval getCopyEval(ExecContext ec, TupleSpec ts) throws ExecException
  {
    AInstr instr;
    IAEval aEval = AEvalFactory.create(ec);
    int numAttrs = ts.getNumAttrs();

    // copy the data columns
    for (int attr = 0; attr < numAttrs; attr++)
    {
      instr = new AInstr();

      instr.op = ExprHelper.getCopyOp(ts.getAttrType(attr));
      instr.r1 = IEvalContext.INPUT_ROLE;
      instr.c1 = attr;
      instr.r2 = 0;
      instr.c2 = 0;
      instr.dr = IEvalContext.SCRATCH_ROLE;
      instr.dc = attr;

      aEval.addInstr(instr);
    }
    
    aEval.compile();
    
    return aEval;
  }
}

