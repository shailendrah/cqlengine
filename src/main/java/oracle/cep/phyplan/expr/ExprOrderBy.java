/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprOrderBy.java /main/6 2011/07/09 08:53:44 udeshmuk Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    06/20/11 - support getSQLEquivalent
    udeshmuk    11/08/09 - API to get all referenced attrs
    sborah      04/20/09 - define getSignature
    sbishnoi    02/10/09 - modifying getXMLPlan2
    parujain    06/28/07 - orderby expression
    parujain    06/28/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprOrderBy.java /main/4 2009/12/03 21:27:59 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;

public class ExprOrderBy extends Expr {

  Expr  orderExpr;
  
  boolean isNullsFirst;
  
  boolean isAsc;
  
  public ExprOrderBy(Expr order, boolean isfirst, boolean asc, Datatype dt)
  {
    super(ExprKind.ORDER_BY_EXPR);
    setType(dt);
    
    this.orderExpr = order;
    this.isNullsFirst = isfirst;
    this.isAsc = asc;
  }
  
  public Expr getOrderbyExpr()
  {
    return orderExpr;
  }
  
  public boolean isNullsFirst()
  {
    return isNullsFirst;
  }
  
  public boolean isAscending()
  {
    return isAsc;
  }
  
  // toString method override
  public String toString() {
  StringBuilder sb = new StringBuilder();

  sb.append("<PhysicalOperatorOrderByExpression>");
  sb.append(super.toString());
  sb.append("<OrderBy =\"" + orderExpr.toString() + "\" />");

  sb.append("<isNullsFirst>");
  sb.append(isNullsFirst);
  sb.append("</isNullsFirst>");

  sb.append("<isAscending>");
  sb.append(isAsc);
  sb.append("</isAscending>");
  
  sb.append("</PhysicalOperatorOrderByExpression>");
  return sb.toString();
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * 
   * @return 
   *      A concise String representation of the OrderBy Expression.
   */
   public String getSignature()
   {
     StringBuilder regExpression = new StringBuilder();
   
     regExpression.append(this.getKind() + "#"
                        + this.isAsc + "#" + this.isNullsFirst);
   
     // process the base expression of the attributes recursively.
     regExpression.append("(" + this.orderExpr.getSignature() + ")");
     
     return regExpression.toString();
   }
  
  @Override
  public boolean equals(Object otherObject) {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    ExprOrderBy other = (ExprOrderBy)otherObject;
    if(other.isAsc != this.isAsc)
      return false;
    
    if(other.isNullsFirst != this.isNullsFirst)
      return false;
    
    if(!(other.orderExpr.equals(this.orderExpr)))
      return false;
    
    return true;
  }

  @Override
  public String getXMLPlan2() {
    StringBuilder xml = new StringBuilder();
    xml.append(" orderByExpr = ");
    xml.append(orderExpr.getXMLPlan2());
    xml.append(", isAscending = ");
    xml.append(String.valueOf(this.isAsc));
    xml.append(", is Nulls First = ");
    xml.append(String.valueOf(this.isNullsFirst));
    xml.append(";");
    return xml.toString();
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    orderExpr.getAllReferencedAttrs(attrs);
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  {
    StringBuilder regExpression = new StringBuilder();
      
    String temp = this.orderExpr.getSQLEquivalent(ec);
    if(temp == null)
      return null;
    // process the base expression of the attributes recursively.
    regExpression.append(" " + temp + " ");
    
    //SQL syntax expects asc/desc earlier than nulls first/last
    if(isAsc)
      regExpression.append("asc ");
    else
      regExpression.append("desc ");
    
    if(isNullsFirst)
      regExpression.append("nulls first ");
    else
      regExpression.append("nulls last ");
    
    return regExpression.toString();
  }
  
}
