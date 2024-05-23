/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/CaseConditionExpr.java /main/6 2012/07/30 19:52:52 pkali Exp $ */

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
    pkali       07/17/12 - meta data propagation for rewritten groupby expr
    pkali       05/07/12 - added getRewrittenExprForGroupBy method
    rkomurav    06/25/07 - cleanup
    rkomurav    05/13/07 - add isClassB
    rkomurav    05/28/07 - add .equals
    parujain    03/29/07 - Case Condition Expr
    parujain    03/29/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/CaseConditionExpr.java /main/6 2012/07/30 19:52:52 pkali Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import java.util.List;

import oracle.cep.common.Datatype;

public class CaseConditionExpr extends Expr {
  
  Expr conditionExpr;
  
  Expr resultExpr;
  
  
  public CaseConditionExpr(Expr cond)
  {
    this.conditionExpr = cond;
    this.resultExpr = null;
  }
  
  public void setResultExpr(Expr res)
  {
    this.resultExpr = res;
  }
  
  @Override
  public ExprType getExprType() {
   return ExprType.E_CASE_CONDITION_EXPR;
  }

  @Override
  public Datatype getReturnType() {
    if(resultExpr == null)
      return null;
    
    return resultExpr.getReturnType();
  }
 
  public Expr getConditionExpr()
  {
    return conditionExpr;
  }
  
  public Expr getResultExpr()
  {
    return resultExpr;
  }
  
  public boolean isResultNull()
  {
    return(resultExpr==null);
  }
  
  public void getAllReferencedAggrs(List<AggrExpr> aggrs)
  {
    conditionExpr.getAllReferencedAggrs(aggrs);
    if(resultExpr != null)
      resultExpr.getAllReferencedAggrs(aggrs);
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type)
  {
    getAllReferencedAttrs(attrs, type, true);
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type,
      boolean includeAggrParams)
  {
    conditionExpr.getAllReferencedAttrs(attrs, type, includeAggrParams);
    if(resultExpr != null)
      resultExpr.getAllReferencedAttrs(attrs, type, includeAggrParams);
  }
  
  public Expr getRewrittenExprForGroupBy(Expr[] gbyExprs)
  {
    Expr rwCondExpr = conditionExpr.getRewrittenExprForGroupBy(gbyExprs);
    if(rwCondExpr == null)
      return null;
    Expr rwResultExpr = null;
    if(resultExpr != null)
    {
      rwResultExpr = resultExpr.getRewrittenExprForGroupBy(gbyExprs);
      if(rwResultExpr == null)
        return null;
    }
    CaseConditionExpr caseCondExpr = new CaseConditionExpr(rwCondExpr); 
    caseCondExpr.setResultExpr(rwResultExpr);
    caseCondExpr.setName(this.getName(), 
                         this.isUserSpecifiedName(), this.isExternal());
    caseCondExpr.setAlias(this.getAlias());
    caseCondExpr.setbNull(this.isNull());
    return caseCondExpr;
  }
  
  public boolean equals(Object otherObject) {
    if(this == otherObject)
      return true;
    if(otherObject == null)
      return false;
    if(getClass() != otherObject.getClass())
      return false;
    CaseConditionExpr other = (CaseConditionExpr)otherObject;
    if(other.resultExpr != null)
    {
      return(this.conditionExpr.equals(other.conditionExpr) && other.resultExpr.equals(this.resultExpr));
    }
    else
    {
      return(this.conditionExpr.equals(other.conditionExpr)&& (this.resultExpr == null));
    }
    
  }
}
