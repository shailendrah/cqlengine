/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SearchedCaseExpr.java /main/8 2014/12/10 18:12:47 sbishnoi Exp $ */

/* Copyright (c) 2007, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    11/25/14 - fix searched case to use expr in gby expressions
    pkali       07/17/12 - meta data propagation for rewritten groupby expr
    pkali       05/07/12 - added getRewrittenExprForGroupBy method
    parujain    07/06/07 - fix returntype
    rkomurav    06/25/07 - cleanup
    rkomurav    05/13/07 - add isClassB
    rkomurav    05/28/07 - add .equals
    parujain    03/28/07 - Searched Case Expression
    parujain    03/28/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SearchedCaseExpr.java /main/8 2014/12/10 18:12:47 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import java.util.ArrayList;
import java.util.List;

import oracle.cep.common.Datatype;

public class SearchedCaseExpr extends Expr{
  
  ArrayList<CaseConditionExpr> conditions;
  
  Expr elseExpr;
  
  public SearchedCaseExpr()
  {
    conditions = new ArrayList<CaseConditionExpr>();
    elseExpr = null;
  }
  
  public SearchedCaseExpr(Expr elsexpr)
  {
    conditions = new ArrayList<CaseConditionExpr>();
    elseExpr = elsexpr;
    if(elseExpr != null)
      this.dt = elseExpr.getReturnType();
  }
  
  public void addCaseCondition(CaseConditionExpr expr)
  {
    conditions.add(expr);
  }

  @Override
  public ExprType getExprType() {
    return ExprType.E_SEARCHED_CASE_EXPR;
  }
  
  public void setReturnType(Datatype type)
  {
    this.dt = type; 
  }

  @Override
  public Datatype getReturnType() {
    return dt;
  }
  
  public Expr getElseExpr()
  {
    return elseExpr;
  }
  
  public ArrayList<CaseConditionExpr> getConditions()
  {
    return conditions;
  }
  
  public int getNumConditions()
  {
    return conditions.size();
  }
  
  public CaseConditionExpr getCondition(int i)
  {
    return conditions.get(i);
  }
  
  public void getAllReferencedAggrs(List<AggrExpr> aggrs)
  {
    for(int i = 0; i < getNumConditions(); i++)
    {
      conditions.get(i).getAllReferencedAggrs(aggrs);
    }
    if(elseExpr != null)
      elseExpr.getAllReferencedAggrs(aggrs);
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type)
  {
    getAllReferencedAttrs(attrs, type, true);
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type,
      boolean includeAggrParams)
  {
    for(int i = 0; i < getNumConditions(); i++)
    {
      conditions.get(i).getAllReferencedAttrs(attrs, type, includeAggrParams);
    }
    if(elseExpr != null)
      elseExpr.getAllReferencedAttrs(attrs, type, includeAggrParams);
  }
  
  public Expr getRewrittenExprForGroupBy(Expr[] gbyExprs)
  {
    for(Expr gbyExr: gbyExprs)
    {
      if(this.equals(gbyExr))
        return new GroupByExpr(this);
    }
    Expr rwElseExpr = null;
    if(elseExpr != null)
    {
      rwElseExpr = elseExpr.getRewrittenExprForGroupBy(gbyExprs);
      if(rwElseExpr == null)
        return null;
    }
    SearchedCaseExpr searchCaseExpr = new SearchedCaseExpr(rwElseExpr);
    for(CaseConditionExpr ccExpr : conditions)
    {
      Expr expr = ccExpr.getRewrittenExprForGroupBy(gbyExprs);
      if(expr == null)
        return null;
      assert expr instanceof CaseConditionExpr;
      searchCaseExpr.addCaseCondition((CaseConditionExpr)expr);
    }
    searchCaseExpr.setReturnType(this.dt);
    searchCaseExpr.setName(this.getName(), 
                           this.isUserSpecifiedName(), this.isExternal());
    searchCaseExpr.setAlias(this.getAlias());
    searchCaseExpr.setbNull(this.isNull());
    return searchCaseExpr;
  }
  
  public boolean equals(Object otherObject) {
    if(this == otherObject)
      return true;
    if(otherObject == null)
      return false;
    if(getClass() != otherObject.getClass())
      return false;
    SearchedCaseExpr other = (SearchedCaseExpr)otherObject;
    if(other.conditions.size() != this.conditions.size())
      return false;
    for(int i=0; i<conditions.size(); i++)
    {
      if(!conditions.get(i).equals(other.conditions.get(i)))
        return false;
    }
    if(other.elseExpr != null)
    {
      return(other.elseExpr.equals(this.elseExpr));
    }
    else
    {
      return(elseExpr == null);
    }
  }  
}
