/* $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/PatternContext.java /main/1 2009/03/30 14:46:03 parujain Exp $ */

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
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/PatternContext.java /main/1 2009/03/30 14:46:03 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.planmgr.codegen;

import java.util.ArrayList;

import oracle.cep.common.AggrFunction;
import oracle.cep.common.Datatype;
import oracle.cep.common.UserDefAggrFn;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.attr.CorrAttr;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.pattern.CorrNameDef;
import oracle.cep.service.ExecContext;

public class PatternContext extends CodeGenContext
{
   int                   numAggrParamExprs;
   AggrFunction[]        fn;
   ArrayList<Expr[]>     aggrParamExprs;
   int[]                 sumPos;
   int[]                 countPos;
   Datatype[]            aggrOutputTypes;
   int                   numUDA;
   int                   numIncrUDA;
   int                   numFullUDA;
   UserDefAggrFn[]       uda;
   int[]                 udaPos;
   IAggrFnFactory[]      udaFactory;
   IAggrFunction[]       udaHandler;
   
   // Related to OUT physical attributes
   Datatype[]            types;
   int                   numCorrAttrs;
   int                   numAggrAttrs;
   CorrAttr[]            corrAttrs;
   CorrAttr[]            aggrAttrs;
   
   // Evals
   EvalContextInfo       evalCtxInfo;
   IEvalContext           evalContext;
   IEvalContext           aggrCopyEvalContext;
   IAEval                 aggrCopyEval;
   IAEval                 nullEval;
   IAEval[]               initEvals;
   IAEval[]               incrEvals;
   IAEval                 releaseEval;

   // Binding indices
   int[]                 map;
   int                   bindRole;
   int                   bindLength;
   int                   aggrRole;

   // Correlations
   CorrNameDef[]         corrDefs;
   int                   numCorrs;
   boolean[]             hasAggrs;

   // Factory for the aggregate tuple
   IAllocator<ITuplePtr> atf;
   
   // Tuple spec for the aggregate tuple
   TupleSpec             ats;
   
   TupleSpec             ts;
	  
  public PatternContext(ExecContext ec, Query query, PhyOpt phyopt)
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
  
  public void setUserDefAggrFn(UserDefAggrFn[] uda)
  {
    this.uda = uda;
  }
  
  public UserDefAggrFn[] getUserDefAggrFn()
  {
    return this.uda;
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
  
  public void setNumCorrAttrs (int num)
  {
    this.numCorrAttrs = num;
  }
  
  public int getNumCorrAttrs()
  {
    return this.numCorrAttrs;
  }
  
  public void setNumAggrAttrs(int num)
  {
    this.numAggrAttrs = num;
  }
  
  public int getNumAggrAttrs()
  {
    return this.numAggrAttrs;
  }
  
  public void setCorrAttrs(CorrAttr[] att)
  {
    this.corrAttrs = att;
  }
  
  public CorrAttr[] getCorrAttrs()
  {  
    return this.corrAttrs;
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
  
  public void setMap(int[] map)
  {
    this.map = map;
  }
  
  public int[] getMap()
  {
    return this.map;
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
  
  public void setCorrDefs(CorrNameDef[] def)
  {
    this.corrDefs = def;
  }
  
  public CorrNameDef[] getCorrDefs()
  {
    return this.corrDefs;
  }
  
  public void setNumCorrs(int num)
  { 
    this.numCorrs = num;
  }
  
  public int getNumCorrs()
  {
    return this.numCorrs;
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
   
   public void setAts(TupleSpec ats)
   {
     this.ats = ats;
   }
   
   public TupleSpec getAts()
   {
     return this.ats;
   }
   
   public void setTs(TupleSpec ts)
   {
     this.ts = ts;
   }
   
   public TupleSpec getTs()
   {
     return this.ts;
   }
   
}

