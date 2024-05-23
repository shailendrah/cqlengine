/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprOrderBy.java /main/2 2011/05/17 03:26:06 anasrini Exp $ */

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
    sborah      04/11/11 - override getAllReferencedAttrs()
    parujain    06/27/07 - order by expression
    parujain    06/27/07 - Creation
 */

/**
 *  @version $Header: ExprOrderBy.java 27-jun-2007.15:54:41 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrUnNamed;

public class ExprOrderBy extends Expr implements Cloneable {

  Expr orderExpr;
  
  boolean isNullsFirst;
  
  boolean isAsc;
  
  public ExprOrderBy(Expr order, boolean isfirst, boolean asc, Datatype dt)
  {
    this.orderExpr = order;
    this.isNullsFirst = isfirst;
    this.isAsc = asc;
    setType(dt);
  }
  
  public Expr getOrderbyExpr()
  {
    return this.orderExpr;
  }
  
  public boolean isAscending()
  {
    return this.isAsc;  
  }
  
  public boolean isNullsFirst()
  {
    return this.isNullsFirst;
  }
  
  public Attr getAttr() {
    AttrUnNamed attr = new AttrUnNamed(getType());    
    return (Attr)attr;
  }
  
  public boolean check_reference(LogOpt op) {
    if(!orderExpr.check_reference(op))
      return false;
    
    return true;
  }
  
  public ExprOrderBy clone() throws CloneNotSupportedException {
    ExprOrderBy exp = (ExprOrderBy)super.clone();
    
    exp.orderExpr = this.orderExpr.clone();
    exp.isAsc = this.isAsc;
    exp.isNullsFirst = this.isNullsFirst;
    return exp;
  }
  
  @Override
  public void getAllReferencedAttrs(List<Attr> attrs) 
  {
    orderExpr.getAllReferencedAttrs(attrs);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, boolean includeAggrParams) 
  {
    orderExpr.getAllReferencedAttrs(attrs, includeAggrParams);
  }
  
  public boolean equals(Object otherObject) {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    ExprOrderBy other = (ExprOrderBy)otherObject;
    if(!other.orderExpr.equals(this.orderExpr))
      return false;
    if(other.isAsc != this.isAsc)
      return false;
    if(other.isNullsFirst != this.isNullsFirst)
      return false;
    
    return true;
  }
  
//toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<LogicalOrderbyExpression>");
    sb.append(super.toString());
    sb.append("<Orderby" + orderExpr.toString() + "/>");
    sb.append("<NullsFirst" + isNullsFirst +"/>");
    sb.append("<Ascending" + isAsc +"/>");
    sb.append("</LogicalOrderbyExpression>");
    return sb.toString();
  }
  
}
