/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/XmlAttrExpr.java /main/3 2012/07/30 19:52:54 pkali Exp $ */

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
    parujain    05/16/08 - Evalname
    parujain    04/23/08 - xml attribute expr
    parujain    04/23/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/XmlAttrExpr.java /main/3 2012/07/30 19:52:54 pkali Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import java.util.List;

import oracle.cep.common.Datatype;

public class XmlAttrExpr extends Expr{
  
  private Expr attrExpr;
  
  // attrName will be null in case of attrnameExpr
  private String attrName;
  
  private Expr attrNameExpr;
  
  private boolean isNameExpr;
  
  XmlAttrExpr()
  {
    this.attrName = null;
    this.isNameExpr = false;
  }
  
  public void setAttrName(String name)
  {
    this.attrName = name;
  }

  public void setAttrExpr(Expr attr)
  {
    this.attrExpr = attr;
  }
  
  public Expr getAttrExpr()
  {
    return this.attrExpr;
  }
  
  public String getAttrName()
  {
    return this.attrName;
  }
  
  public Expr getAttrNameExpr()
  {
    return this.attrNameExpr;
  }
  
  public void setAttrNameExpr(Expr attrexpr)
  {
    this.attrNameExpr = attrexpr;
    this.isNameExpr = true;
  }
  
  public boolean isAttrNameExpr()
  {
    return this.isNameExpr;
  }
  
  @Override
  public boolean equals(Object otherObject) {
    if(this == otherObject)
      return true;
    if(otherObject == null)
      return false;
    if(getClass() != otherObject.getClass())
      return false;
    XmlAttrExpr other = (XmlAttrExpr)otherObject;
    if((this.attrName == null) && (other.attrName != null))
      return false;
    if((this.attrName != null) && (other.attrName == null))
      return false;
    if(this.attrName == null)
    {
      if(!this.attrNameExpr.equals(other.attrNameExpr))
        return false;
    }
    else if(!this.attrName.equalsIgnoreCase(other.attrName))
      return false;
    
    return (this.attrExpr.equals(other.attrExpr));
  }

  @Override
  public void getAllReferencedAggrs(List<AggrExpr> aggrs) {
    if(this.isNameExpr)
      this.attrNameExpr.getAllReferencedAggrs(aggrs);
    attrExpr.getAllReferencedAggrs(aggrs);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type) {
    getAllReferencedAttrs(attrs, type, true);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type, 
                             boolean includeAggrParams) {
    if(this.isNameExpr)
      this.attrNameExpr.getAllReferencedAttrs(attrs, type, includeAggrParams);
    attrExpr.getAllReferencedAttrs(attrs, type, includeAggrParams);
  }

  public Expr getRewrittenExprForGroupBy(Expr[] gbyExprs)
  {
    XmlAttrExpr xmlAttrExpr = new XmlAttrExpr();
    if(this.isNameExpr)
    {
      Expr rwNameExpr = attrNameExpr.getRewrittenExprForGroupBy(gbyExprs);
      if(rwNameExpr == null)
        return null;
      xmlAttrExpr.setAttrNameExpr(rwNameExpr);
    }
    Expr rwAttrExpr = attrExpr.getRewrittenExprForGroupBy(gbyExprs);
    if(rwAttrExpr == null)
      return null;
    xmlAttrExpr.setAttrExpr(rwAttrExpr);
    xmlAttrExpr.setAttrName(this.attrName);
    xmlAttrExpr.setName(this.getName(), 
                        this.isUserSpecifiedName(), this.isExternal());
    xmlAttrExpr.setAlias(this.getAlias());
    xmlAttrExpr.setbNull(this.isNull());
    return xmlAttrExpr;
  }
  
	@Override
  public ExprType getExprType() {

	return ExprType.E_XML_ATTR_EXPR;
  }

  @Override
  public Datatype getReturnType() {
    return Datatype.XMLTYPE;
  }
	
}
