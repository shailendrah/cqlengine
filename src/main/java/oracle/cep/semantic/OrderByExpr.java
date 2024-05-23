/* $Header: OrderByExpr.java 03-jul-2007.14:39:15 parujain Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    06/26/07 - order by expression
    parujain    06/26/07 - Creation
 */

/**
 *  @version $Header: OrderByExpr.java 03-jul-2007.14:39:15 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import java.util.List;

import oracle.cep.common.Datatype;

public class OrderByExpr extends Expr{
  
  private Expr orderExpr;
  
  private boolean isNullsFirst;
  
  private boolean isAsc;
  
  public OrderByExpr(Expr order, boolean nulls, boolean asc)
  {
    this.orderExpr = order;
    this.isNullsFirst = nulls;
    this.isAsc = asc;
    this.dt = order.dt;
  }
  
  public Expr getOrderbyExpr()
  {
    return orderExpr;
  }
  
  public boolean isNullsFirst()
  {
    return this.isNullsFirst;
  }
  
  public boolean isAscending()
  {
    return this.isAsc;
  }

  @Override
  public boolean equals(Object otherObject) {
    if(this == otherObject)
      return true;
    if(otherObject == null)
      return false;
    if(getClass() != otherObject.getClass())
      return false;
    OrderByExpr other = (OrderByExpr)otherObject;
    if(!other.orderExpr.equals(this.orderExpr))
      return false;
    if(other.isAsc != this.isAsc)
      return false;
    if(other.isNullsFirst != this.isNullsFirst)
      return false;
    return false;
  }

  @Override
  public void getAllReferencedAggrs(List<AggrExpr> aggrs) {
    orderExpr.getAllReferencedAggrs(aggrs);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type) {
    orderExpr.getAllReferencedAttrs(attrs, type);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type, boolean includeAggrParams) {
    orderExpr.getAllReferencedAttrs(attrs, type, includeAggrParams);
  }

  @Override
  public ExprType getExprType() {
    return ExprType.E_ORDER_BY_EXPR;
  }

  @Override
  public Datatype getReturnType() {
    return dt;
  }

  // toString
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<OrderByExpr>");
     sb.append("<OrderExpr>"+ orderExpr.toString() + "</OrderExpr>");
     sb.append("<IsNullsFirst>");
     sb.append(isNullsFirst);
     sb.append("</IsNullsFirst>");
     sb.append("<IsAscending>");
     sb.append(isAsc);
     sb.append("</IsAscending>");
    sb.append("</OrderByExpr>");
    return sb.toString();
  }
}
