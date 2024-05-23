/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/XmlForestExpr.java /main/3 2012/07/30 19:52:55 pkali Exp $ */

/* Copyright (c) 2008, 2012, Oracle and/or its affiliates. 
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
    pkali       05/08/12 - added getRewrittenExprForGroupBy method
    parujain    05/23/08 - XMLForest Expr
    parujain    05/23/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/XmlForestExpr.java /main/3 2012/07/30 19:52:55 pkali Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import java.util.List;

import oracle.cep.common.Datatype;

public class XmlForestExpr extends Expr {
	
  Expr[] forestExprs;
  
  XmlForestExpr()
  {
    forestExprs = null;
  }
  
  public int getNumForestExprs()
  {
    return forestExprs.length;
  }
  
  public Expr[] getForestExprs()
  {
    return this.forestExprs;
  }
  
  public void setForestExprs(Expr[] exprs)
  {
    this.forestExprs = exprs;
  }

  @Override
  public boolean equals(Object otherObject) {
    if(this == otherObject)
      return true;
    if(otherObject == null)
      return false;
    if(getClass() != otherObject.getClass())
      return false;
    XmlForestExpr other = (XmlForestExpr)otherObject;
    
    if(forestExprs.length != other.forestExprs.length)
      return false;
    for(int i=0; i<forestExprs.length; i++)
    {
      if(!forestExprs[i].equals(other.forestExprs[i]))
        return false;
    }
    
	return true;
  }

  @Override
  public void getAllReferencedAggrs(List<AggrExpr> aggrs) {
    for(int i=0; i<forestExprs.length; i++)
      forestExprs[i].getAllReferencedAggrs(aggrs);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type) {
    getAllReferencedAttrs(attrs, type, true);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type, 
                             boolean includeAggrParams) {
    for(int i=0; i<forestExprs.length; i++)
      forestExprs[i].getAllReferencedAttrs(attrs, type, includeAggrParams);
  }

  public Expr getRewrittenExprForGroupBy(Expr[] gbyExprs)
  {
    XmlForestExpr xmlForestExpr = new XmlForestExpr();
    Expr[] rwForestExprs = new Expr[forestExprs.length];
    for(int i=0; i<forestExprs.length; i++)
    {
      Expr rwForestExpr = forestExprs[i].getRewrittenExprForGroupBy(gbyExprs);
      if(rwForestExpr == null)
        return null;
      rwForestExprs[i] = rwForestExpr;
    }
    xmlForestExpr.setForestExprs(rwForestExprs);
    xmlForestExpr.setName(this.getName(), 
                          this.isUserSpecifiedName(), this.isExternal());
    xmlForestExpr.setAlias(this.getAlias());
    xmlForestExpr.setbNull(this.isNull());
    return xmlForestExpr;
  }
  
  @Override
  public ExprType getExprType() {
	return ExprType.E_XMLFOREST_EXPR;
  }

  @Override
  public Datatype getReturnType() {
	
	return Datatype.XMLTYPE;
  }

}
