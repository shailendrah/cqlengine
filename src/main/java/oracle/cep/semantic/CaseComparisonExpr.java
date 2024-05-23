/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/CaseComparisonExpr.java /main/7 2012/07/30 19:52:52 pkali Exp $ */

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
    sbishnoi    05/31/12 - fix bug 14050762
    pkali       05/07/12 - added getRewrittenExprForGroupBy method
    rkomurav    06/25/07 - cleanup].
    rkomurav    05/13/07 - add isclassB
    rkomurav    05/28/07 - add .equals
    parujain    04/02/07 - Simple Case Comparison Expression
    parujain    04/02/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/CaseComparisonExpr.java /main/7 2012/07/30 19:52:52 pkali Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import java.util.List;

import oracle.cep.common.Datatype;

public class CaseComparisonExpr extends Expr{

  Expr comparisonExpr;
  
  Expr resultExpr;
  
  CaseComparisonExpr(Expr compExpr)
  {
    this.comparisonExpr = compExpr;
    this.resultExpr = null;
  }
  
  @Override
  public ExprType getExprType() {
   return ExprType.E_CASE_COMPARISON_EXPR;
  }

  @Override
  public Datatype getReturnType() {
    if(resultExpr == null)
      return null;
    
    return resultExpr.getReturnType();
  }
  
  public void setResultExpr(Expr res)
  {
    this.resultExpr = res;
  }
  
  public Expr getComparisonExpr()
  {
    return this.comparisonExpr;
  }
  
  /**
   * @param comparisonExpr the comparisonExpr to set
   */
  public void setComparisonExpr(Expr comparisonExpr)
  {
    this.comparisonExpr = comparisonExpr;
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
    comparisonExpr.getAllReferencedAggrs(aggrs);
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
    comparisonExpr.getAllReferencedAttrs(attrs, type, includeAggrParams);
    if(resultExpr != null)
      resultExpr.getAllReferencedAttrs(attrs, type, includeAggrParams);
  }
  
  public Expr getRewrittenExprForGroupBy(Expr[] gbyExprs)
  {
    Expr rwCompExpr = comparisonExpr.getRewrittenExprForGroupBy(gbyExprs);
    if(rwCompExpr == null)
      return null;
    Expr rwResultExpr = null;
    if(resultExpr != null)
    {
      rwResultExpr = resultExpr.getRewrittenExprForGroupBy(gbyExprs);
      if(rwResultExpr == null)
        return null;
    }
    CaseComparisonExpr caseCompExpr = new CaseComparisonExpr(rwCompExpr); 
    caseCompExpr.setResultExpr(rwResultExpr);
    caseCompExpr.setName(this.getName(), 
                         this.isUserSpecifiedName(), this.isExternal());
    caseCompExpr.setAlias(this.getAlias());
    caseCompExpr.setbNull(this.isNull());
    return caseCompExpr;
  }
  
  public boolean equals(Object otherObject) {
    if(this == otherObject)
      return true;
    if(otherObject == null)
      return false;
    if(getClass() != otherObject.getClass())
      return false;
    
    CaseComparisonExpr other = (CaseComparisonExpr)otherObject;
    if(other.resultExpr != null)
    {
      return(this.comparisonExpr.equals(this.comparisonExpr) && (other.resultExpr.equals(this.resultExpr)));
    }
    else
    {
      return(this.comparisonExpr.equals(this.comparisonExpr) && (this.resultExpr == null));
    }
  } 
}
