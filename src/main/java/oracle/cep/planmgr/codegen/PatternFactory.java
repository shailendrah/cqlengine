/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/PatternFactory.java /main/23 2011/02/07 03:36:26 sborah Exp $ */
/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    12/20/10 - remove eval.setEvalContext
    sborah      10/14/09 - support for bigdecimal
    parujain    03/19/09 - stateless server
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    skmishra    09/16/08 - adding param to getInitEval
    skmishra    07/17/08 - changing signatures for xmlagg orderby
    hopark      02/29/08 - fix nullInputTuple creation
    hopark      12/07/07 - cleanup spill
    rkomurav    09/28/07 - support non mandatory correlation defs
    hopark      09/04/07 - eval optimize
    rkomurav    09/06/07 - add prevrole to aggrhelper calls
    rkomurav    07/03/07 - uda
    rkomurav    06/13/07 - fix init incr eval initializaions
    rkomurav    06/06/07 - remove isSimple
    rkomurav    05/15/07 - classB
    rkomurav    05/30/07 - 
    anasrini    05/30/07 - 
    rkomurav    05/30/07 - add inst created to copyeval
    rkomurav    05/30/07 - add map
    anasrini    05/29/07 - measures support
    najain      05/09/07 - variable length datatype support
    hopark      05/08/07 - ITuple api cleanup
    rkomurav    04/13/07 - fix unpin for nullTuple
    hopark      04/06/07 - mark static pin for const tupleptr
    hopark      04/05/07 - memmgr reorg
    rkomurav    04/02/07 - fix initialization
    najain      03/29/07 - cleanup
    najain      03/14/07 - cleanup
    rkomurav    03/10/07 - execution related changes
    rkomurav    03/02/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/PatternFactory.java st_pcbpel_anasrini_eval_parallelism_2/1 2010/12/20 07:47:47 anasrini Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.codegen;

import java.util.ArrayList;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Datatype;
import oracle.cep.common.AggrFunction;
import oracle.cep.common.BaseAggrFn;
import oracle.cep.common.UserDefAggrFn;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.AOp;
import oracle.cep.execution.internals.AInstr;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.factory.AEvalFactory;
import oracle.cep.execution.internals.factory.BEvalFactory;
import oracle.cep.execution.internals.factory.EvalContextFactory;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.PatternStrm;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.attr.CorrAttr;
import oracle.cep.phyplan.expr.BoolExpr;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.PhyOptPatternStrm;
import oracle.cep.phyplan.pattern.CorrNameDef;
import oracle.cep.service.ExecContext;

public class PatternFactory extends ExecOptFactory
{
  
  @Override
  public CodeGenContext createCodeGenContext(ExecContext ec, Query query, 
                                             PhyOpt phyopt)
  {
     return new PatternContext(ec, query, phyopt); 
  }

  ExecOpt newExecOpt(CodeGenContext ctx) throws CEPException
  {
    assert ctx instanceof PatternContext;
    PatternContext pctx = (PatternContext)ctx;
    PhyOpt op = ctx.getPhyopt();
    assert op instanceof PhyOptPatternStrm;

    ExecContext ec = ctx.getExecContext();
    initPatternInfo(ec, pctx, op);
    return new PatternStrm(ctx.getExecContext());
  }
  
  void setupExecOpt(CodeGenContext ctx) throws CEPException
  {
    assert ctx instanceof PatternContext;
    PatternContext pctx = (PatternContext)ctx;
    
    assert ctx.getExecOpt() instanceof PatternStrm;
    PhyOpt op = ctx.getPhyopt();
    PhyOptPatternStrm phyPattStrm = (PhyOptPatternStrm)op;

    ExecContext ec = ctx.getExecContext();
    PatternStrm pattStrm = (PatternStrm)ctx.getExecOpt();
    IEvalContext evalContext  = EvalContextFactory.create(ec);
    pctx.setEvalContext(evalContext);
    
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    EvalContextInfo evalCtxInfo  = new EvalContextInfo(factoryMgr);
    pctx.setEvalContextInfo(evalCtxInfo);
    
    
    CorrNameDef[] corrDefs = phyPattStrm.getCorrDefs();
    pctx.setCorrDefs(corrDefs);
    
    int numCorrs = corrDefs.length;
    pctx.setNumCorrs(numCorrs);

    IBEval[] defs   = new IBEval[numCorrs];
    int[] inpRoles = new int[1];

    //mapping from correlation name to binding pos
    int[] map    = new int[numCorrs];
    int bindLength = phyPattStrm.getBindLength();
    int bindRole   = evalContext.addRoles(bindLength);
    
    pctx.setBindLength(bindLength);
    pctx.setBindRole(bindRole);
    
    // Eval Context role number for the aggregate tuple
    int aggrRole   = bindRole + bindLength - 1;
    pctx.setAggrRole(aggrRole);
                                                
    //For simple patterns where no referencing of other
    //correlation names is done, input is bound to input role
    inpRoles[0] = IEvalContext.INPUT_ROLE;
    
    //construct BEvals corresponding to DEFINE predicates
    //definition corresponding to undefined correlation name is null
    BoolExpr boolExpr;
    for(int i = 0; i < numCorrs; i++)
    {
      boolExpr = corrDefs[i].getBExpr();
      map[i]   = corrDefs[i].getBindPos();
      if(boolExpr == null)
      {
        defs[i] = null;
        continue;
      }
      defs[i] = BEvalFactory.create(ec);
      ExprHelper.instBoolExpr(ec, boolExpr, defs[i], evalCtxInfo,
                              false, inpRoles);
      defs[i].compile();
    }
    pctx.setMap(map);

    // Handle the aggregations
    handleAggrs(ec, pctx);
    
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
    
    //build copyeval - copy from bindrole to outputrole
    IAEval copyEval = getCopyEval(ec, pctx);
    ITuplePtr nullInputTuple = getNullInputTuple(ec, op);
    
    pattStrm.setNullInputTuple(nullInputTuple);
    pattStrm.setCopyEval(copyEval);
    pattStrm.setAggrCopyEvalContext(pctx.getAggrCopyEvalContext());
    pattStrm.setAggrCopyEval(pctx.getAggrCopyEval());
    pattStrm.setNullEval(pctx.getNullEval());
    pattStrm.setInitEvals(pctx.getInitEvals());
    pattStrm.setIncrEvals(pctx.getIncrEvals());
    pattStrm.setBindRole(bindRole);
    pattStrm.setEvalContext(evalContext);
    pattStrm.setDefs(defs);
    pattStrm.setBindLength(bindLength);
    pattStrm.setDfa(phyPattStrm.getDfa());
    pattStrm.setMap(map);
    pattStrm.setAlphToStateMap(phyPattStrm.getAlphabetToStateMap());
    pattStrm.setHasAggrs(pctx.getHasAggrs());
    pattStrm.setReleaseEval(pctx.getReleaseEval());

    // set the aggregation tuple factory
    pattStrm.setAggrTupleFactory(pctx.getAtf());

    //has to be called after all the fields of PatternStrm are set
    pattStrm.initialize();
  }

  private void initPatternInfo(ExecContext ec, PatternContext ctx, 
                               PhyOpt op)
  throws CEPException
  {
    BaseAggrFn[] aggFns;

    PhyOptPatternStrm phyPattStrm = (PhyOptPatternStrm)op;
    
    int numAggrParamExprs = phyPattStrm.getNumAggrAttrs();
    ctx.setNumAggrParamExprs(numAggrParamExprs);
    
    int numAggrAttrs      = numAggrParamExprs;
    ctx.setNumAggrAttrs(numAggrAttrs);
    
    ArrayList<Expr[]> aggrParamExprs    = phyPattStrm.getAggrParamExprs();
    ctx.setAggrParamExprs(aggrParamExprs);
    
    aggFns            = phyPattStrm.getAggrFns();
    
    AggrFunction[] fn = new AggrFunction[numAggrParamExprs];
    ctx.setAggrFunction(fn);
    
    Datatype[] aggrOutputTypes   = phyPattStrm.getAggrOutputTypes();
    ctx.setAggrOutputTypes(aggrOutputTypes);
    
    CorrAttr[] aggrAttrs         = phyPattStrm.getAggrAttrs(); 
    ctx.setAggrAttrs(aggrAttrs);
    
    int numCorrAttrs      = phyPattStrm.getNumCorrAttrs();
    ctx.setNumCorrAttrs(numCorrAttrs);
    
    CorrAttr[] corrAttrs         = phyPattStrm.getCorrAttrs(); 
    ctx.setCorrAttrs(corrAttrs);
     
    AttributeMetadata[] attrMetadata = op.getAttrMetadata();
    
    Datatype[] types = new Datatype[attrMetadata.length];
    
    for(int i = 0 ; i < attrMetadata.length; i ++)
    {
      types[i] = attrMetadata[i].getDatatype();
    }
    
    ctx.setTypes(types);
    
    int numIncrUDA = 0;
    int numFullUDA = 0;
    for (int i=0; i<numAggrParamExprs; i++) {
      fn[i] = aggFns[i].getFnCode();
      if(fn[i] == AggrFunction.USER_DEF)
      {
        if(aggFns[i].supportsIncremental())
          numIncrUDA++;
        else
          numFullUDA++;
      }
    }
    int numUDA = numIncrUDA + numFullUDA;
    
    ctx.setNumIncrUDA(numIncrUDA);
    ctx.setNumFullUDA(numFullUDA);
    ctx.setNumUDA(numUDA);
    
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    TupleSpec ats = ctx.getAts();
    if(numUDA > 0)
    {
      if(ats == null)
      {
        ats = new TupleSpec(factoryMgr.getNextId());
        ctx.setAts(ats);
      }
      UserDefAggrFn[] uda  = new UserDefAggrFn[numAggrParamExprs];
      int[] udaPos     = new int[numAggrParamExprs];
      IAggrFnFactory[] udaFactory = new IAggrFnFactory[numAggrParamExprs];
      IAggrFunction[] udaHandler = new IAggrFunction[numAggrParamExprs];
      AggrHelper.initUDA(ec, numUDA, aggFns, uda, udaPos, udaFactory, udaHandler,
          numAggrParamExprs, fn, false, false, ats, null);
      ctx.setUserDefAggrFn(uda);
      ctx.setUdaPos(udaPos);
      ctx.setUdaFactory(udaFactory);
      ctx.setUdaHandler(udaHandler);
    }

    // Determine the SUM and COUNT positions for each AVG
    int[] sumPos   = new int[numAggrAttrs];
    int[] countPos = new int[numAggrAttrs];
    AggrHelper.getSumCountPos(sumPos, countPos, fn, aggrParamExprs,
                              numAggrParamExprs, 0);
    ctx.setSumPos(sumPos);
    ctx.setCountPos(countPos);
  }
  
  private void handleAggrs(ExecContext ec, PatternContext ctx) throws CEPException
  {
    ArrayList<Datatype[]> inpTypes;
    Datatype[]            outTypes;
    ArrayList<Expr[]>     paramExprs;
    AggrFunction[]        fns;
    BaseAggrFn[]          baseFns;
    int                   numAggrs;
    int                   aggrBindPos;

    // Create the single NULL IAEval
    IAEval nullEval = 
      AggrHelper.getNullOutputEval(ec, ctx.getAggrRole(), 
                                   ctx.getEvalContextInfo(), 
                                   ctx.getAggrFunction(), 
                                   ctx.getNumAggrParamExprs());
    
    nullEval.compile();
    ctx.setNullEval(nullEval);

    // Create an init and incr IAEval for each correlation name
    int numCorrs = ctx.getNumCorrs();
    
    boolean[] hasAggrs = new boolean[numCorrs];
    IAEval[] initEvals = new IAEval[numCorrs];
    IAEval[] incrEvals = new IAEval[numCorrs];
    
    aggrBindPos = 0;
    CorrNameDef[] corrDefs = ctx.getCorrDefs();
    for (int i=0; i<numCorrs; i++) {
      numAggrs = corrDefs[i].getNumAggrs();
      if (numAggrs > 0) {
        hasAggrs[i] = true;
        inpTypes   = corrDefs[i].getAggrInputTypes();
        outTypes   = corrDefs[i].getAggrOutputTypes();
        paramExprs = corrDefs[i].getAggrParamExprs();
        fns        = corrDefs[i].getAggrFunctions();
        baseFns    = corrDefs[i].getAggrFns();

        initEvals[i] = AEvalFactory.create(ec);
        AggrHelper.getInitEval(ec, initEvals[i], IEvalContext.INPUT_ROLE, 
                               ctx.getAggrRole(), ctx.getAggrRole(),
                               ctx.getBindRole(), ctx.getEvalContextInfo(),
                               ctx.getEvalContext(),
                               fns, paramExprs, numAggrs, inpTypes, outTypes,
                               aggrBindPos, baseFns, ctx.getUdaPos(),
                               aggrBindPos,
                               ctx.getUdaFactory(), ctx.getUdaHandler(), true, 
                               -1, false,null, null, null, null);

        initEvals[i].compile();
        
        incrEvals[i] = AEvalFactory.create(ec);
        AggrHelper.getIncrEval(ec, incrEvals[i], IEvalContext.INPUT_ROLE,
                               ctx.getAggrRole(), ctx.getAggrRole(),
                               ctx.getBindRole(), true,
                               ctx.getEvalContextInfo(),
                               ctx.getEvalContext(), fns, paramExprs,
                               numAggrs, inpTypes, outTypes,
                               ctx.getSumPos(), ctx.getCountPos(),
                               aggrBindPos, ctx.getUdaPos(), baseFns,
                               aggrBindPos, -1, null, null, null, null);
        
        incrEvals[i].compile();
        aggrBindPos = aggrBindPos + numAggrs;
      }
      else
        hasAggrs[i] = false;
    }
    ctx.setHasAggr(hasAggrs);
    ctx.setInitEvals(initEvals);
    ctx.setIncrEvals(incrEvals);

    int numUDA = ctx.getNumUDA();
    if(numUDA > 0)
    {
      ArrayList<Integer> compressUdaPos     = new ArrayList<Integer>();
      ArrayList<IAggrFnFactory> compressUdaFactory = new ArrayList<IAggrFnFactory>();
      ArrayList<IAggrFunction> compressUdaHandler = new ArrayList<IAggrFunction>();
      AggrHelper.compressUdaInfo(ctx.getUdaPos(), ctx.getUdaFactory(), 
                                 ctx.getUdaHandler(), compressUdaPos,
                                 compressUdaFactory, compressUdaHandler, 
                                 ctx.getAggrFunction());
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
    
    // Create the aggr copy eval and eval context
    if (ctx.getNumAggrParamExprs() > 0) {
      IEvalContext aggrCopyEvalContext = EvalContextFactory.create(ec);
      ctx.setAggrCopyEvalContext(aggrCopyEvalContext);
      IAEval aggrCopyEval        = AEvalFactory.create(ec);
      ctx.setAggrCopyEval(aggrCopyEval);
      
      Datatype[] aggrOutputTypes = ctx.getAggrOutputTypes();
      
      for (int i=0; i<ctx.getNumAggrParamExprs(); i++) {
        AInstr instr = new AInstr();
        AOp    aop = ExprHelper.getCopyOp(aggrOutputTypes[i]);

        instr.op = aop;
        instr.r1 = IEvalContext.INPUT_ROLE;
        instr.c1 = i;
        instr.dr = IEvalContext.NEW_OUTPUT_ROLE;
        instr.dc = i;
        aggrCopyEval.addInstr(instr);
      }
      aggrCopyEval.compile();
    }

    // Create the factory for the aggregate tuple
    if (ctx.getNumAggrParamExprs() > 0) {
      FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
      Datatype[] aggrOutputTypes = ctx.getAggrOutputTypes();
      TupleSpec ats = new TupleSpec(factoryMgr.getNextId());
      for (int i=0; i<ctx.getNumAggrParamExprs(); i++)
      {
        if(aggrOutputTypes[i] == Datatype.BIGDECIMAL)
          ats.addAttr(new AttributeMetadata(aggrOutputTypes[i], 0, 
                                            Datatype.BIGDECIMAL.getPrecision(),
                                            0));
        else
          ats.addAttr(new AttributeMetadata(aggrOutputTypes[i], 0, 0, 0));
      }

      ctx.setAts(ats);
      IAllocator<ITuplePtr> atf = factoryMgr.get(ats);
      ctx.setAtf(atf);
    }
  }
  
  private ITuplePtr getNullInputTuple(ExecContext ec, PhyOpt op) 
    throws ExecException
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
  
  private IAEval getCopyEval(ExecContext ec, PatternContext ctx)
    throws ExecException
  {
    IAEval  copyEval = AEvalFactory.create(ec);
    AInstr instr;
    int    cntr = 0;
    int numCorrAttrs = ctx.getNumCorrAttrs();
    int bindRole = ctx.getBindRole();
    CorrAttr[]  corrAttrs = ctx.getCorrAttrs();
    Datatype[] types = ctx.getTypes();

    // Copy the correlation attribites first
    for(int i=0; i < numCorrAttrs; i++)
    {
      instr = new AInstr();
      instr.op = ExprHelper.getCopyOp(types[cntr]);
      instr.r1 = corrAttrs[i].getBindPos() + bindRole;
      instr.c1 = corrAttrs[i].getPos();
      instr.dr = IEvalContext.NEW_OUTPUT_ROLE;
      instr.dc = cntr;
      copyEval.addInstr(instr);
      cntr++;
    }

    CorrAttr[]  aggrAttrs = ctx.getAggrAttrs();
    // Copy the aggregation attribites 
    for(int i=0; i < ctx.getNumAggrAttrs(); i++)
    {
      instr = new AInstr();
      instr.op = ExprHelper.getCopyOp(types[cntr]);
      instr.r1 = aggrAttrs[i].getBindPos() + bindRole;
      instr.c1 = aggrAttrs[i].getPos();
      instr.dr = IEvalContext.NEW_OUTPUT_ROLE;
      instr.dc = cntr;
      copyEval.addInstr(instr);
      
      cntr++;
    }
    assert cntr == (numCorrAttrs + ctx.getNumAggrAttrs());

    copyEval.compile();
    return copyEval;
  }
  
}

