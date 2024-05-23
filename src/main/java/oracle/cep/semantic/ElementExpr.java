/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/ElementExpr.java /main/3 2012/07/30 19:52:53 pkali Exp $ */

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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/ElementExpr.java /main/3 2012/07/30 19:52:53 pkali Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import java.util.List;

import oracle.cep.common.Datatype;

public class ElementExpr extends Expr {
	
  private String elementName;
  
  private Expr elementNameExpr;
  
  Expr[] attrExprs;
  
  //child nodes which can be either arith expr or another Element
  Expr[]  childExprs;
  
  boolean isNameExpr;
  
  ElementExpr()
  {
    this.elementName = null;
    this.elementNameExpr = null;
    this.attrExprs = null;
    this.childExprs = null;
    this.isNameExpr = false;
  }
  
  public void setElementName(String name)
  {
    this.elementName = name;
  }
  
  public int getNumAttrs()
  {
    if(attrExprs == null)
      return 0;
    return attrExprs.length;
  }
  
  public int getNumChildren()
  {
    if(childExprs == null)
      return 0;
    return childExprs.length;
  }
  
  public void setAttrs(Expr[] attrs)
  {
    this.attrExprs = attrs;
  }
  
  public void setElementNameExpr(Expr name)
  {
    this.elementNameExpr = name;
    this.isNameExpr = true;
  }
  
  public boolean isElementNameExpr()
  {
    return this.isNameExpr;
  }
  
  public Expr getElementNameExpr()
  {
    return this.elementNameExpr;
  }
  
  public void setChildExprs(Expr[] child)
  {
    this.childExprs = child;
  }
  
  public String getElementName()
  {
    return this.elementName;
  }
  
  public Expr[] getChildExprs()
  {
    return this.childExprs;
  }
  
  public Expr[] getAttrExprs()
  {
    return this.attrExprs;
  }

  @Override
  public boolean equals(Object otherObject) {
    if(this == otherObject)
      return true;
    if(otherObject == null)
      return false;
    if(getClass() != otherObject.getClass())
      return false;
    ElementExpr other = (ElementExpr)otherObject;
    if(this.isNameExpr != other.isNameExpr)
      return false;
    if(!this.isNameExpr)
    {
      if(!this.elementName.equalsIgnoreCase(other.elementName))
        return false;
    }
    else 
      if(!this.elementNameExpr.equals(other.elementNameExpr))
        return false;
    	  
    if(this.attrExprs != null)
    {
      if(other.attrExprs == null)
        return false;
      if(this.attrExprs.length != other.attrExprs.length)
        return false;
      for(int i=0; i<attrExprs.length; i++)
      {
        if(!this.attrExprs[i].equals(other.attrExprs[i]))
          return false;
      }
    }
    if((this.attrExprs == null) && (other.attrExprs != null))
      return false;
    if(this.childExprs != null)
    {
      if(other.childExprs == null)
        return false;
      if(this.childExprs.length != other.childExprs.length)
        return false;
      for(int i=0;i<childExprs.length; i++)
      {
        if(!this.childExprs[i].equals(other.childExprs[i]))
          return false;
      }
    }
    return true;
  }

  @Override
  public void getAllReferencedAggrs(List<AggrExpr> aggrs) {
    if(this.isNameExpr)
      this.elementNameExpr.getAllReferencedAggrs(aggrs);
    for(int i=0; i<getNumAttrs(); i++)
      attrExprs[i].getAllReferencedAggrs(aggrs);
    for(int j=0; j<getNumChildren(); j++)
      childExprs[j].getAllReferencedAggrs(aggrs);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type) {
    getAllReferencedAttrs(attrs, type, true);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type, 
                                     boolean includeAggrParams) {
    if(this.isNameExpr)
      this.elementNameExpr.getAllReferencedAttrs(attrs, type, includeAggrParams);
    for(int i=0; i<getNumAttrs(); i++)
      attrExprs[i].getAllReferencedAttrs(attrs, type, includeAggrParams);
    for(int j=0; j<getNumChildren(); j++)
      childExprs[j].getAllReferencedAttrs(attrs, type, includeAggrParams);
  }
	  
  public Expr getRewrittenExprForGroupBy(Expr[] gbyExprs)
  {
    ElementExpr elementExpr = new ElementExpr();
    if(this.isNameExpr)
    {
      Expr rwNameExpr = elementNameExpr.getRewrittenExprForGroupBy(gbyExprs);
      if(rwNameExpr == null)
        return null;
      elementExpr.setElementNameExpr(rwNameExpr);
    }
    
    Expr[] rwAttrExprs = new Expr[getNumAttrs()];
    for(int i=0; i<getNumAttrs(); i++)
    {
      Expr rwAttrExpr = attrExprs[i].getRewrittenExprForGroupBy(gbyExprs);
      if(rwAttrExpr == null)
        return null;
      rwAttrExprs[i] = rwAttrExpr;
    }
    elementExpr.setAttrs(rwAttrExprs);
    
    Expr[] rwChildExprs = new Expr[getNumChildren()];
    for(int j=0; j<getNumChildren(); j++)
    {
      Expr rwChildExpr = childExprs[j].getRewrittenExprForGroupBy(gbyExprs);
      if(rwChildExpr == null)
        return null;
      rwChildExprs[j] = rwChildExpr;
    }
    elementExpr.setChildExprs(rwChildExprs);
    
    elementExpr.setElementName(this.elementName);
    elementExpr.setName(this.getName(), 
                        this.isUserSpecifiedName(), this.isExternal());
    elementExpr.setAlias(this.getAlias());
    elementExpr.setbNull(this.isNull());
    return elementExpr;
  }
  
  @Override
  public ExprType getExprType() {

  return ExprType.E_ELEMENT_EXPR;
  }

  @Override
  public Datatype getReturnType() {
    return Datatype.XMLTYPE;
  }

}
