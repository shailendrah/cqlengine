/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SimpleCaseExpr.java /main/9 2014/12/10 18:12:47 sbishnoi Exp $ */

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
    sbishnoi    11/25/14 - fix simplecase expression to use in gby expressions
    pkali       07/17/12 - meta data propagation for rewritten groupby expr
    pkali       05/07/12 - added getRewrittenExprForGroupBy method
    udeshmuk    02/20/08 - handle nulls.
    parujain    07/06/07 - fix returntype
    rkomurav    06/25/07 - leanup.cleanup
    rkomurav    05/14/07 - add isClassb
    rkomurav    05/28/07 - add .equals.
    parujain    04/02/07 - Simple Case Expression
    parujain    04/02/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SimpleCaseExpr.java /main/9 2014/12/10 18:12:47 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import java.util.ArrayList;
import java.util.List;

import oracle.cep.common.Datatype;

public class SimpleCaseExpr extends Expr{
  
  Expr compExpr;
  
  ArrayList<CaseComparisonExpr> comparisonExprs;

  Expr elseExpr;
  
  public SimpleCaseExpr(Expr comp)
  {
    this.compExpr = comp;
    comparisonExprs = new ArrayList<CaseComparisonExpr>();
    elseExpr = null;
  }
  
  public SimpleCaseExpr(Expr comp, Expr elsexpr)
  {
    this.compExpr = comp;
    comparisonExprs = new ArrayList<CaseComparisonExpr>();
    this.elseExpr = elsexpr;
    if(elseExpr != null)
      this.dt = elseExpr.getReturnType();
  }
  
  @Override
  public ExprType getExprType() {
    return ExprType.E_SIMPLE_CASE_EXPR;
  }
  
  public void setReturnType(Datatype datatype)
  {
    this.dt = datatype;
  }

  @Override
  public Datatype getReturnType() {
    return dt;
  }
  
  public Expr getElseExpr()
  {
    return elseExpr;
  }
  
  public Expr getCompExpr()
  {
    return compExpr;
  }
  
  public void setCompExpr(Expr compExpr)
  {
    this.compExpr = compExpr;  
  }
 
  public int getNumComparisonExprs()
  {
    return comparisonExprs.size();
  }
  
  public ArrayList<CaseComparisonExpr> getComparisonExprs()
  {
    return comparisonExprs;
  }
  
  public void addComparisonExpr(CaseComparisonExpr expr)
  {
    comparisonExprs.add(expr);
  }
  
  public void setComparisonExpr(int index, CaseComparisonExpr newExpr)
  {
    comparisonExprs.set(index, newExpr);
  }
  
  public CaseComparisonExpr getComparisonExpr(int i)
  {
    return comparisonExprs.get(i);
  }
  
  public void getAllReferencedAggrs(List<AggrExpr> aggrs)
  {
    compExpr.getAllReferencedAggrs(aggrs);
    for(int i=0; i< getNumComparisonExprs(); i++)
    {
      comparisonExprs.get(i).getAllReferencedAggrs(aggrs);
    }
    if(elseExpr != null)
    {
      elseExpr.getAllReferencedAggrs(aggrs);
    }
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type)
  {
    getAllReferencedAttrs(attrs, type, true);
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type,
      boolean includeAggrParams)
  {
    compExpr.getAllReferencedAttrs(attrs, type, includeAggrParams);
    for(int i=0; i< getNumComparisonExprs(); i++)
    {
      comparisonExprs.get(i).getAllReferencedAttrs(attrs, type, includeAggrParams);
    }
    if(elseExpr != null)
    {
      elseExpr.getAllReferencedAttrs(attrs, type, includeAggrParams);
    }
  }
  
  public Expr getRewrittenExprForGroupBy(Expr[] gbyExprs)
  {
    for(Expr gbyExpr: gbyExprs)
    {
      if(this.equals(gbyExpr))
        return new GroupByExpr(this);
    }

    Expr rwCompExpr = compExpr.getRewrittenExprForGroupBy(gbyExprs);
    if(rwCompExpr == null)
      return null;
    Expr rwElseExpr = null;
    if(elseExpr != null)
    {
      rwElseExpr = elseExpr.getRewrittenExprForGroupBy(gbyExprs);
      if(rwElseExpr == null)
        return null;
    }
    SimpleCaseExpr simpleCaseExpr = new SimpleCaseExpr(rwCompExpr, rwElseExpr);
    for(CaseComparisonExpr ccExpr : comparisonExprs)
    {
      Expr expr = ccExpr.getRewrittenExprForGroupBy(gbyExprs);
      if(expr == null)
        return null;
      assert expr instanceof CaseComparisonExpr;
      simpleCaseExpr.addComparisonExpr((CaseComparisonExpr)expr);
    }
    simpleCaseExpr.setReturnType(this.dt);
    simpleCaseExpr.setName(this.getName(), 
                           this.isUserSpecifiedName(), this.isExternal());
    simpleCaseExpr.setAlias(this.getAlias());
    simpleCaseExpr.setbNull(this.isNull());
    return simpleCaseExpr;
  }
  
  public boolean equals(Object otherObject) {
    if(this == otherObject)
      return true;
    if(otherObject == null)
      return false;
    if(getClass() != otherObject.getClass())
      return false;
    
    SimpleCaseExpr other = (SimpleCaseExpr)otherObject;
    if(this.getNumComparisonExprs() != other.getNumComparisonExprs())
      return false;
    
    if(!other.compExpr.equals(other.compExpr))
      return false;
    for(int i=0; i< getNumComparisonExprs(); i++)
    {
      if(!comparisonExprs.get(i).equals(other.getComparisonExprs().get(i)))
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
