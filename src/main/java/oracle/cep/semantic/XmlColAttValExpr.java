/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/XmlColAttValExpr.java /main/3 2012/07/30 19:52:54 pkali Exp $ */

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
    pkali       05/09/12 - added getRewrittenExprForGroupBy method
    parujain    05/29/08 - xmlcolattval expr
    parujain    05/29/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/XmlColAttValExpr.java /main/3 2012/07/30 19:52:54 pkali Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import java.util.List;

import oracle.cep.common.Datatype;

public class XmlColAttValExpr extends Expr {
	
  Expr[] colExprs;
  
  XmlColAttValExpr()
  {
    colExprs = null;
  }
  
  public int getNumColAttExprs()
  {
    return colExprs.length;
  }
  
  public Expr[] getColAttExprs()
  {
    return this.colExprs;
  }
  
  public void setColAttExprs(Expr[] exprs)
  {
    this.colExprs = exprs;
  }

  @Override
  public boolean equals(Object otherObject) {
    if(this == otherObject)
      return true;
    if(otherObject == null)
      return false;
    if(getClass() != otherObject.getClass())
      return false;
    XmlColAttValExpr other = (XmlColAttValExpr)otherObject;
    
    if(colExprs.length != other.colExprs.length)
      return false;
    for(int i=0; i<colExprs.length; i++)
    {
      if(!colExprs[i].equals(other.colExprs[i]))
        return false;
    }
    
	return true;
  }

  @Override
  public void getAllReferencedAggrs(List<AggrExpr> aggrs) {
    for(int i=0; i<colExprs.length; i++)
      colExprs[i].getAllReferencedAggrs(aggrs);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type) {
    getAllReferencedAttrs(attrs, type, true);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type, boolean includeAggrParams) {
    for(int i=0; i<colExprs.length; i++)
      colExprs[i].getAllReferencedAttrs(attrs, type, includeAggrParams);
  }

  public Expr getRewrittenExprForGroupBy(Expr[] gbyExprs)
  {
    Expr[] rwColExprs = new Expr[colExprs.length];
    for(int i=0; i<colExprs.length; i++)
    {
      Expr rwColExpr = colExprs[i].getRewrittenExprForGroupBy(gbyExprs);
      if(rwColExpr == null)
        return null;
      rwColExprs[i] = rwColExpr;
    }
    XmlColAttValExpr xmlColAttValExpr = new XmlColAttValExpr();
    xmlColAttValExpr.setColAttExprs(rwColExprs);
    xmlColAttValExpr.setName(this.getName(), 
                        this.isUserSpecifiedName(), this.isExternal());
    xmlColAttValExpr.setAlias(this.getAlias());
    xmlColAttValExpr.setbNull(this.isNull());
    return xmlColAttValExpr;
  }
  
  @Override
  public ExprType getExprType() {
	return ExprType.E_XMLCOLATTVAL_EXPR;
  }

  @Override
  public Datatype getReturnType() {
	return Datatype.XMLTYPE;
  }

}

