/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/XMLParseExpr.java /main/3 2012/07/30 19:52:55 pkali Exp $ */

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
    skmishra    06/12/08 - exprtype
    skmishra    05/19/08 - Creation
 */
package oracle.cep.semantic;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.common.XMLParseKind;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/XMLParseExpr.java /main/3 2012/07/30 19:52:55 pkali Exp $
 *  @author  skmishra
 *  @since   release specific (what release of product did this appear in)
 */

public class XMLParseExpr extends Expr
{
  Expr value;
  boolean isWellformed;
  XMLParseKind kind;
  
  public XMLParseExpr(Expr e, boolean _isWF, XMLParseKind _kind)
  {
    value = e;
    isWellformed = _isWF;
    kind = _kind;
  }
  
  public Expr getValue()
  {
    return value;
  }

  public void setValue(Expr value)
  {
    this.value = value;
  }

  public boolean equals(Object other)
  {
    if(other == null)
      return false;
    if(this == other)
      return true;
    if(getClass() != other.getClass())
      return false;
    
    XMLParseExpr o = (XMLParseExpr)other;
    
    return this.value.equals(o.value);
  }

  public void getAllReferencedAggrs(List<AggrExpr> aggrs)
  {
    value.getAllReferencedAggrs(aggrs);
  }

  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type)
  {
    value.getAllReferencedAttrs(attrs, type);
  }

  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type,
      boolean includeAggrParams)
  {
    value.getAllReferencedAttrs(attrs, type,includeAggrParams);
  }

  public Expr getRewrittenExprForGroupBy(Expr[] gbyExprs)
  {
    Expr rwValExpr = value.getRewrittenExprForGroupBy(gbyExprs);
    if(rwValExpr == null)
      return null;
    XMLParseExpr xmlParseExpr =  new XMLParseExpr(rwValExpr, 
                                             this.isWellformed, this.kind);
    xmlParseExpr.setName(this.getName(), 
                          this.isUserSpecifiedName(), this.isExternal());
    xmlParseExpr.setAlias(this.getAlias());
    xmlParseExpr.setbNull(this.isNull());
    return xmlParseExpr;
  }
  
  public ExprType getExprType()
  {
    return ExprType.E_XML_PARSE_EXPR;
  }

  public Datatype getReturnType()
  {
    return Datatype.XMLTYPE;
  }

  public boolean isWellformed()
  {
    return isWellformed;
  }

  public void setWellformed(boolean isWellformed)
  {
    this.isWellformed = isWellformed;
  }

  public XMLParseKind getKind()
  {
    return kind;
  }

  public void setKind(XMLParseKind kind)
  {
    this.kind = kind;
  }
}

