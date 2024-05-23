/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/pattern/CorrName.java /main/2 2008/11/07 23:08:44 udeshmuk Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    10/15/08 - support for xmlagg orderby in pattern.
    rkomurav    03/18/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/pattern/CorrName.java /main/2 2008/11/07 23:08:44 udeshmuk Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan.pattern;

import java.util.ArrayList;

import oracle.cep.common.BaseAggrFn;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprOrderBy;

public abstract class CorrName
{
  /** ID of the correlation name */
  int                   varId;
  
  /** position in the binding */
  int                   bindPos;
  
  /** aggregation functions whose param references this correlation name **/
  ArrayList<BaseAggrFn> aggrFns;

  /** aggregation param exprs that reference this correlation name **/
  ArrayList<Expr[]>     aggrParamExprs;
  
  /** order by exprs that may be present in xmlagg referencing this corrname **/
  ArrayList<ExprOrderBy[]> orderByExprs;

  /** 
   * Number of aggregations whose param expressions reference this 
   * correlation name
   */
  int                   numAggrs;
  
  /**
   * Constructor
   * @param varId id of the correlation name
   */
  public CorrName(int varId)
  {
    this.varId          = varId;
    this.numAggrs       = 0;
    this.aggrFns        = new ArrayList<BaseAggrFn>();
    this.aggrParamExprs = new ArrayList<Expr[]>();
    this.orderByExprs   = new ArrayList<ExprOrderBy[]>();
  }
  
  /**
   * Add an aggregate attribute to the list of attributes 
   * @param fn aggregate function to be added to the list
   * @param expr aggregate parameter expression
   */
  public void addAggr(BaseAggrFn fn, Expr[] expr, ExprOrderBy[] orderExpr)
  {
    aggrFns.add(fn);
    aggrParamExprs.add(expr);
    orderByExprs.add(orderExpr);
    numAggrs++;
  }

  /**
   * @param bindPos the bindPos to set
   */
  public void setBindPos(int bindPos)
  {
    this.bindPos = bindPos;
  }
  
  /**
   * Get the number of aggregations that reference this correlation name
   * @return the number of aggregations that reference this correlation name
   */
  public int getNumAggrs()
  {
    return numAggrs;
  }

  /**
   * Get the aggregation functions whose param reference this correlation
   * @return the aggregation functions whose param reference this correlation
   */
  public BaseAggrFn[] getAggrFns()
  {
    return aggrFns.toArray(new BaseAggrFn[0]);
  }

  /**
   * Get the aggregation params that reference this correlation
   * @return the aggregation params that reference this correlation
   */
  public ArrayList<Expr[]> getAggrParamExprs()
  {
    return aggrParamExprs;
  }

  /**
   * Getter for orderByExprs
   * @return the orderByExprs that may be present in the xmlagg referencing this corrname 
   */
  public ArrayList<ExprOrderBy[]> getOrderByExprs()
  {
    return orderByExprs;
  }
  
  /**
   * @return the varId
   */
  public int getVarId()
  {
    return varId;
  }

  /**
   * @return the bindPos
   */
  public int getBindPos()
  {
    return bindPos;
  }
}