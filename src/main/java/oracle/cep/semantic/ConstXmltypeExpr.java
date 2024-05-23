/* $Header: ConstXmltypeExpr.java 13-jun-2008.10:04:01 skmishra Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 skmishra    06/05/08 - cleanup
 skmishra    05/16/08 - changing representation to node
 skmishra    05/05/08 - Creation
 */

package oracle.cep.semantic;

import java.util.List;

import oracle.cep.common.Datatype;

import org.w3c.dom.Node;

/**
 * @version $Header: ConstXmltypeExpr.java 13-jun-2008.10:04:01 skmishra Exp $
 * @author skmishra
 * @since release specific (what release of product did this appear in)
 */

public class ConstXmltypeExpr extends Expr
{
  Node item;

  public ConstXmltypeExpr(Node n)
  {
    super();
    item = n;
  }

  public Node getValue()
  {
    return item;
  }

  public void setValue(Node n)
  {
    this.item = n;
  }

  public boolean equals(Object other)
  {
    if (this == other)
      return true;
    if (other == null)
      return false;
    if (!(other instanceof ConstXmltypeExpr))
      return false;

    ConstXmltypeExpr xExpr = (ConstXmltypeExpr) other;
    return (this.item.equals(xExpr.getValue()));
  }

  public void getAllReferencedAggrs(List<AggrExpr> aggrs)
  {
    return;
  }

  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type)
  {
    return;
  }

  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type,
      boolean includeAggrParams)
  {
    return;
  }

  public ExprType getExprType()
  {
    return ExprType.E_CONST_VAL;
  }

  public Datatype getReturnType()
  {
    return Datatype.XMLTYPE;
  }
}