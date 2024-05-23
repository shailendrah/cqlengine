/* $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/PatternStrmClassContext.java /main/1 2009/03/30 14:46:03 parujain Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    03/20/09 - pattern context
    parujain    03/20/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/PatternStrmClassContext.java /main/1 2009/03/30 14:46:03 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.planmgr.codegen;

import java.util.ArrayList;

import oracle.cep.common.AggrFunction;
import oracle.cep.common.Datatype;
import oracle.cep.common.UserDefAggrFn;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.comparator.ComparatorSpecs;
import oracle.cep.execution.indexes.HashIndex;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.phyplan.attr.CorrAttr;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprOrderBy;
import oracle.cep.phyplan.pattern.CorrNameDef;
import oracle.cep.phyplan.pattern.SubsetCorr;
import oracle.cep.service.ExecContext;

public class PatternStrmClassContext extends CodeGenContext
{
	
  boolean                   hasPartnAttrs;
  int                       numPartnAttrs;
  Attr[]                    partnAttrs;
  HashIndex                 partnIndex;
  
//Related to aggregations
  int                       numAggrParamExprs;
  AggrFunction[]            fn;
  ArrayList<Expr[]>         aggrParamExprs;
  int[]                     sumPos;
  int[]                     countPos;
  Datatype[]                aggrOutputTypes;
  int                       numUDA;
  int                       numIncrUDA;
  int                       numFullUDA;
  UserDefAggrFn[]           uda;
  int[]                     udaPos;
  IAggrFnFactory[]          udaFactory;
  IAggrFunction[]           udaHandler;
  ArrayList<ExprOrderBy[]>  orderByExprsList;
  
//Related to physical aggr attributes
  Datatype[]                types;
  int                       numAggrAttrs;
  CorrAttr[]                aggrAttrs;
  
  // Evals
  EvalContextInfo           evalCtxInfo;
  IEvalContext              evalContext;
  IEvalContext              aggrCopyEvalContext;
  IAEval                    aggrCopyEval;
  IAEval                    nullEval;
  IAEval[]                  initEvals;
  IAEval[]                  incrEvals;
  IAEval                    releaseEval;
  IEvalContext              partnCopyEvalContext;
  IAEval                    partnCopyEval;
  
  // Binding indices
  int                       bindRole;
  int                       bindLength;
  int                       aggrRole;
  int                       prevRole;

  // Correlations
  CorrNameDef[]             corrDefs;
  int                       numCorrDefs;
  SubsetCorr[]              subsetCorrs;
  int                       numSubsetCorrs;
  boolean[]                 hasAggrs;
  
  // Referenced subset pos for corrDefs
  int[][]                   subsetPos;
  
  // measure clause expressions
  Expr[]                    measureExprs;

  // Factory for the aggregate tuple
  IAllocator<ITuplePtr>     atf;
  
  // Factory for the partition tuple
  IAllocator<ITuplePtr>     ptf;
  
  // Tuple spec for the aggregate tuple
  TupleSpec                 ats;
  
  int                       maxPrevIndex;
  
  //xmlagg related data structures
  /** count of xmlaggs */
  int                       numXMLAgg = 0;
  /** release eval for xmlagg indices */
  IAEval                    releaseIndexEval;
  /** 
   * Array contains position in the aggr tuple which has
   * the pointer to the sorted list of that xmlagg.
   * -1 for non-xmlagg functions.
   */
  int[]                     xmlAggIndexPos;
  /**
   * Compare attrs for xmlaggs. 
   */
  ComparatorSpecs[][]       compareSpecs;
  /**
   * Tuple specification for the tuple that will contain orderby attrs
   * of various xmlaggs during execution
   */
  TupleSpec                 orderByTupleSpec;
  /**
   * Factory for orderby tuple
   */
  IAllocator<ITuplePtr>     orderByAllocator = null;
  
  public PatternStrmClassContext (ExecContext ec, Query query, PhyOpt phyopt)
  {
    super(ec, query, phyopt);
  }
  
  public void setNumAggrParamExprs(int num)
  {
    this.numAggrParamExprs = num;
  }
  
  public int getNumAggrParamExprs()
  {
    return this.numAggrParamExprs;
  }
  
  public void setAggrFunction(AggrFunction[] fn)
  {
    this.fn = fn;
  }
  
  public AggrFunction[] getAggrFunction()
  {
    return this.fn;
  }
  
  public void setAggrParamExprs(ArrayList<Expr[]> aggrParams)
  {
    this.aggrParamExprs = aggrParams;
  }
  
  public ArrayList<Expr[]> getAggrParamExprs()
  {
    return this.aggrParamExprs;
  }
  
  public void setSumPos(int[] sum)
  {
    this.sumPos = sum;
  }
  
  public int[] getSumPos()
  {  
    return this.sumPos;
  }
  
  public void setCountPos(int[] count)
  {
    this.countPos = count;
  }
  
  public int[] getCountPos()
  {
    return this.countPos;
  }
  
  public void setAggrOutputTypes(Datatype[] aggrOut)
  {
   this.aggrOutputTypes = aggrOut;
  }
  
  public Datatype[] getAggrOutputTypes()
  {
    return this.aggrOutputTypes;
  }
  
  public void setNumUDA(int num)
  {
    this.numUDA = num;
  }
  
  public int getNumUDA()
  {
    return this.numUDA;
  }
  
  public void setNumIncrUDA(int incr)
  {
    this.numIncrUDA = incr;
  }
  
  public int getNumIncrUDA()
  {
    return this.numIncrUDA;
  }
  
  public void setNumFullUDA(int full)
  {
    this.numFullUDA = full;
  }
  
  public int getNumFullUDA()
  {
    return this.numFullUDA;
  }
  
  public void setUserDefAggrFn(UserDefAggrFn[] uda)
  {
    this.uda = uda;
  }
  
  public UserDefAggrFn[] getUserDefAggrFn()
  {
    return this.uda;
  }
  
  public void setUdaPos(int[] pos)
  {
    this.udaPos = pos;
  }
  
  public int[] getUdaPos()
  {
    return this.udaPos;
  }
  
  public void setUdaFactory(IAggrFnFactory[] fact)
  {
    this.udaFactory = fact;
  }
  
  public IAggrFnFactory[] getUdaFactory()
  {
    return this.udaFactory;
  }
  
  public void setUdaHandler(IAggrFunction[] fn)
  {
    this.udaHandler = fn;
  }
  
  public IAggrFunction[] getUdaHandler()
  {
    return this.udaHandler;
  }
  
  public void setTypes(Datatype[] types)
  {
    this.types = types;
  }
  
  public Datatype[] getTypes()
  {
    return this.types;
  }
  
  public void setNumAggrAttrs(int num)
  {
    this.numAggrAttrs = num;
  }
  
  public int getNumAggrAttrs()
  {
    return this.numAggrAttrs;
  }
  
  public void setAggrAttrs(CorrAttr[] agg)
  {
    this.aggrAttrs = agg;
  }
  
  public CorrAttr[] getAggrAttrs()
  {
    return this.aggrAttrs;
  }
  
  public void setEvalContextInfo(EvalContextInfo info)
  {
    this.evalCtxInfo = info;
  }
  
  public EvalContextInfo getEvalContextInfo()
  {
    return this.evalCtxInfo;
  }
  
  public void setEvalContext(IEvalContext ctx)
  {
    this.evalContext = ctx;
  }
  
  public IEvalContext getEvalContext()
  {
    return this.evalContext;
  }
  
  public void setAggrCopyEvalContext(IEvalContext ctx)
  {
    this.aggrCopyEvalContext = ctx;
  }
  
  public IEvalContext getAggrCopyEvalContext()
  {
    return this.aggrCopyEvalContext;
  }
  
  public void setAggrCopyEval(IAEval eval)
  {
    this.aggrCopyEval = eval;
  }
  
  public IAEval getAggrCopyEval()
  {
    return this.aggrCopyEval;
  }
  
  public void setPartnCopyEvalContext(IEvalContext ctx)
  {
    this.partnCopyEvalContext = ctx;
  }
  
  public IEvalContext getPartnCopyEvalContext()
  {
    return this.partnCopyEvalContext;
  }
  
  public void setPartnCopyEval(IAEval eval)
  {
    this.partnCopyEval = eval;
  }
  
  public IAEval getPartnCopyEval()
  {
    return this.partnCopyEval;
  }
  
  public void setNullEval(IAEval eval)
  {
    this.nullEval = eval;
  }
  
  public IAEval getNullEval()
  {
    return this.nullEval;
  }
  
  public void setInitEvals(IAEval[] eval)
  {
    this.initEvals = eval;
  }
  
  public IAEval[] getInitEvals()
  { 
    return this.initEvals;
  }
  
  public void setIncrEvals(IAEval[] eval)
  {
    this.incrEvals = eval;
  }
  
  public IAEval[] getIncrEvals()
  {
    return this.incrEvals;
  }
  
  public void setReleaseEval(IAEval eval)
  {
    this.releaseEval = eval;
  }
  
  public IAEval getReleaseEval()
  {
    return this.releaseEval;
  }
  
  public void setReleaseIndexEval(IAEval eval)
  {
    this.releaseIndexEval = eval;
  }
  
  public IAEval getReleaseIndexEval()
  {
    return this.releaseIndexEval;
  }
  
  public void setBindRole(int role)
  {
    this.bindRole = role;
  }
  
  public int getBindRole()
  {
    return this.bindRole;
  }
  
  public void setBindLength(int len)
  {
    this.bindLength = len;
  }
  
  public int getBindLength()
  {
    return this.bindLength;
  }
  
  public void setAggrRole(int role)
  {
    this.aggrRole = role;
  }
  
  public int getAggrRole()
  {
    return this.aggrRole;
  }
  
  public void setPrevRole(int role)
  {
    this.prevRole = role;
  }
  
  public int getPrevRole()
  {
    return this.prevRole;
  }
  
  public void setCorrDefs(CorrNameDef[] def)
  {
    this.corrDefs = def;
  }
  
  public CorrNameDef[] getCorrDefs()
  {
    return this.corrDefs;
  }
  
  public void setNumCorrDefs(int num)
  { 
    this.numCorrDefs = num;
  }
  
  public int getNumCorrDefs()
  {
    return this.numCorrDefs;
  }
  
  public void setSubsetCorrs(SubsetCorr[] corrs)
  {
    this.subsetCorrs = corrs;
  }
  
  public SubsetCorr[] getSubsetCors()
  {
    return this.subsetCorrs;
  }
  
  public void setNumSubsetCorrs(int num)
  { 
    this.numSubsetCorrs = num;
  }
  
  public int getNumSubsetCorrs()
  {
    return this.numSubsetCorrs;
  }
  
  public void setHasAggr(boolean[] has)
  {
    this.hasAggrs = has;
  }
  
  public boolean[] getHasAggrs()
  {
    return this.hasAggrs;
  }
  
  public void setAtf(IAllocator<ITuplePtr> atf)
  {
    this.atf = atf;
  }
  
  public IAllocator<ITuplePtr> getAtf()
  {
    return this.atf;
  }
  
  public void setPtf(IAllocator<ITuplePtr> ptf)
  {
    this.ptf = ptf;
  }
  
  public IAllocator<ITuplePtr> getPtf()
  {
    return this.ptf;
  }
  
  public void setAts(TupleSpec ats)
  {
    this.ats = ats;
  }
  
  public TupleSpec getAts()
  {
    return this.ats;
  }
  
  public void setNumXmlAgg(int num)
  {
    this.numXMLAgg = num;
  }
  
  public int getNumXmlAgg()
  {
    return this.numXMLAgg;
  }
  
  public void setMaxPrevIndex(int max)
  {
    this.maxPrevIndex = max;
  }
  
  public int getMaxPrevIndex()
  {
    return this.maxPrevIndex;
  }
  
  public void setXmlAggIndexPos(int[] xmlAggIndex)
  {
    this.xmlAggIndexPos = xmlAggIndex;
  }
  
  public int[] getXmlAggIndexPos()
  {
    return this.xmlAggIndexPos;
  }
  
  public void setOrderByExprsList(ArrayList<ExprOrderBy[]> list)
  {
    this.orderByExprsList = list;
  }
  
  public ArrayList<ExprOrderBy[]> getOrderByExprsList()
  {
    return this.orderByExprsList;
  }
  
  public void setHasPartnAttrs(boolean has)
  {
    this.hasPartnAttrs = has;
  }
  
  public boolean getHasPartnAttrs()
  {
    return this.hasPartnAttrs;
  }
  
  public void setNumPartnAttrs(int num)
  {
    this.numPartnAttrs = num;
  }
  
  public int getNumPartnAttrs()
  {
    return this.numPartnAttrs;
  }
  
  public void setPartnAttrs(Attr[] atts)
  { 
    this.partnAttrs = atts;
  }
  
  public Attr[] getPartnAttrs()
  {
    return this.partnAttrs;
  }
  
  public void setPartnIndex(HashIndex index)
  {
    this.partnIndex = index;
  }
  
  public HashIndex getPartnIndex()
  {
    return this.partnIndex;
  }
  
  public void setSubsetPos(int[][] pos)
  {
    this.subsetPos = pos;
  }
  
  public int[][] getSubsetPos()
  {
    return this.subsetPos;
  }
  
  public void setMeasureExprs(Expr[] exprs)
  {
    this.measureExprs = exprs;
  }
  
  public Expr[] getMeasureExprs()
  {
    return this.measureExprs;
  }
  
  public void setCompareSpecs(ComparatorSpecs[][] compare)
  {
    this.compareSpecs = compare;
  }
  
  public ComparatorSpecs[][] getCompareSpecs()
  {
    return this.compareSpecs;
  }
  
  public void setOrderByTupleSpec(TupleSpec osp)
  {
    this.orderByTupleSpec = osp;
  }
  
  public TupleSpec getOrderByTupleSpec()
  {
    return this.orderByTupleSpec;
  }
  
  public void setOrderByAllocator(IAllocator<ITuplePtr> order)
  {
    this.orderByAllocator = order;
  }
  
  public IAllocator<ITuplePtr> getOrderByAllocator()
  {
    return this.orderByAllocator;
  }
}
