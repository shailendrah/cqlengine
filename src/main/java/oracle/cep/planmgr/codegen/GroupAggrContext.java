/* $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/GroupAggrContext.java /main/1 2009/03/30 14:46:02 parujain Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    03/20/09 - stateless server
    parujain    03/20/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/GroupAggrContext.java /main/1 2009/03/30 14:46:02 parujain Exp $
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
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprOrderBy;
import oracle.cep.service.ExecContext;

public class GroupAggrContext extends CodeGenContext
{
   int numGroupAttrs;
   int numAggrParamExprs;
   ArrayList<Datatype[]> aggrInpTypes;
   AggrFunction[] fn;
   int[] groupPos;
   int countStarPos;
   int[] sumPos;
   int[] countPos;
   Datatype[] types;
   Datatype[] aggrTypes;
   EvalContextInfo evalCtxInfo;
   IEvalContext evalContext;
   ConstTupleSpec ct;
   TupleSpec st;
   ArrayList<Expr[]> aggrParamExprs;
	    
   //for xmlagg
   ArrayList<ExprOrderBy[]> orderByExprs;
   int numXmlAgg; 
   int[] xmlAggIndexPos; //where will the index(sorted list) be stored in the output tuple?
   ComparatorSpecs[][] compareSpecs; //used to compare tuples reqd. for order by clause
   TupleSpec orderByTupleSpec; //stores the TupleSpec for the order by exprs across all xmlaggs
   IAllocator<ITuplePtr> orderByAllocator; //the allocator for the order by tuple
   // We will modify TupleSpec for UDAs as well as inbuilt aggregations (xmlagg)
   TupleSpec ts;
	    
   // For user defined aggregations
   boolean oneGroup;
   int numUDA;
   int numIncrUDA;
   int numFullUDA;
   UserDefAggrFn[] uda;
   int[] udaPos;
   IAggrFnFactory[] udaFactory;
   IAggrFunction[] udaHandler;
   
  public GroupAggrContext(ExecContext ec, Query query, PhyOpt phyopt)
  {
    super(ec, query, phyopt);
  }
  
  public void setNumGroupAttrs(int num)
  {
    this.numGroupAttrs = num;
  }
  
  public int getNumGroupAttrs()
  {
    return this.numGroupAttrs;
  }
  
  public void setNumAggrParamExprs(int num)
  {
    this.numAggrParamExprs = num;
  }
  
  public int getNumAggrParamExprs()
  {
    return this.numAggrParamExprs;
  }
  
  public void setAggrInpTypes(ArrayList<Datatype[]> aggrInp)
  {
   this.aggrInpTypes = aggrInp;
  }
  
  public ArrayList<Datatype[]> getAggrInpTypes()
  {
    return this.aggrInpTypes;
  }
  
  public void setAggrFunction(AggrFunction[] fn)
  {
    this.fn = fn;
  }
  
  public AggrFunction[] getAggrFunction()
  {
    return this.fn;
  }
  
  public void setGroupPos(int[] gp)
  {
    this.groupPos = gp;
  }
  
  public int[] getGroupPos()
  {
    return this.groupPos;
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
  
  public void setCountStarPos(int count)
  {
    this.countStarPos = count;
  }
  
  public int getCountStarPos()
  {
    return this.countStarPos;
  }
  
  public void setTypes(Datatype[] types)
  {
    this.types = types;
  }
  
  public Datatype[] getTypes()
  {
    return this.types;
  }
  
  public void setAggrTypes(Datatype[] types)
  {
    this.aggrTypes = types;
  }
  
  public Datatype[] getAggrTypes()
  {
    return this.aggrTypes;
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
  
  public void setConstTupleSpec(ConstTupleSpec ct)
  {
    this.ct = ct;
  }
  
  public ConstTupleSpec getConstTupleSpec()
  {
    return this.ct;
  }
  
  public void setScratchTuple(TupleSpec st)
  {
    this.st = st;
  }
  
  public TupleSpec getScratchTuple()
  {
    return this.st;
  }
  
  public void setAggrParamExprs(ArrayList<Expr[]> aggrParams)
  {
    this.aggrParamExprs = aggrParams;
  }
  
  public ArrayList<Expr[]> getAggrParamExprs()
  {
    return this.aggrParamExprs;
  }
  
  public void setOrderByExprs(ArrayList<ExprOrderBy[]> order)
  {
    this.orderByExprs = order;
  }
  
  public ArrayList<ExprOrderBy[]> getOrderByExprs()
  {
    return this.orderByExprs;
  }
  
  public void setNumXmlAgg(int num)
  {
    this.numXmlAgg = num;
  }
  
  public int getNumXmlAgg()
  {
    return this.numXmlAgg;
  }
  
  public void setXmlAggIndexPos(int[] xmlAggIndex)
  {
    this.xmlAggIndexPos = xmlAggIndex;
  }
  
  public int[] getXmlAggIndexPos()
  {
    return this.xmlAggIndexPos;
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
  
  public void setTupleSpec(TupleSpec ts)
  {
    this.ts = ts;
  }
  
  public TupleSpec getTupleSpec()
  {
    return this.ts;
  }
  
  public void setOneGroup(boolean one)
  {
    this.oneGroup = one;
  }
  
  public boolean getOneGroup()
  {
    return this.oneGroup;
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
  
  public void setUdaPos(int[] pos)
  {
    this.udaPos = pos;
  }
  
  public int[] getUdaPos()
  {
    return this.udaPos;
  }
}
