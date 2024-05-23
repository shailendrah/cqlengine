/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ComplexBoolExpr.java /main/9 2011/07/09 08:53:44 udeshmuk Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
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
    udeshmuk    11/09/09 - API to get all referenced attrs
    sborah      04/20/09 - define getSignature
    rkomurav    06/14/07 - add attrfactory param
    rkomurav    03/06/07 - restructure exprfactorycontext
    parujain    02/05/07 - fix getXMLPlan
    parujain    12/28/06 - fix ToString
    parujain    12/21/06 - Fix equal
    parujain    11/07/06 - Logical Boolean Exprs
    parujain    10/31/06 - Complex Boolean Expr
    parujain    10/31/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ComplexBoolExpr.java /main/7 2009/12/03 21:27:59 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.common.LogicalOp;
import oracle.cep.extensibility.expr.ComplexBooleanExpression;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;

/**
 * Complex Boolean Expression Logical Operator Class Definition
 */
public class ComplexBoolExpr
    extends BoolExpr
    implements ComplexBooleanExpression
{
  /** Logical Operator */
  LogicalOp   oper;
  
  /** Left Expression */
  Expr     left;

  /** Right Expression */
  Expr     right;
  
  public ComplexBoolExpr(LogicalOp oper, Expr left, Expr right, Datatype dt)
  {
    super(ExprKind.COMP_BOOL_EXPR);
    setType(dt);
    
    this.oper  = oper;
    this.left  = left;
    this.right = right;
  }
  
  // getter methods

  /**
   * Get the Logical operator
   * 
   * @return the logical operator
   */
  public LogicalOp getOper() {
    return oper;
  }

  public LogicalOp getOperator() {
      return getOper();
  }

  
  /**
   * Get the left operand
   * 
   * @return the left operand
   */
  public Expr getLeft() {
    return left;
  }

  /**
   * Get the right operand
   * 
   * @return the right operand
   */
  public Expr getRight() {
    return right;
  }


  /**
   * @param left The left to set.
   */
  public void setLeft(Expr left) {
    this.left = left;
  }

  /**
   * @param right The right to set.
   */
  public void setRight(Expr right) {
    this.right = right;
  }
  
  /**
  * Method to calculate a concise String representation
  * of the Expression. The String is built recursively by calling 
  * this method on each type of Expr object which forms the expression.
  * 
  * A complex boolean expression contains either binary operators like 
  * AND, OR , XOR or unary operators like NOT.
  * 
  * @return 
  *      A concise String representation of the Expression.
  */
  public String getSignature()
  {
    BoolExpr left;
    BoolExpr right;
    StringBuilder regExpression = new StringBuilder();

    left = (BoolExpr)this.getLeft();
    right = (BoolExpr)this.getRight();

    assert left != null;
    LogicalOp oper = this.getOper();

    regExpression.append("(");
    if(right != null)
    {
      // Operators like AND, OR
      regExpression.append(left.getSignature());
      regExpression.append(oper.getSymbol());
      regExpression.append(right.getSignature());
    }
    else
    {
      // Unary operators
      regExpression.append(oper.getSymbol());
      regExpression.append(left.getSignature());
    }
    regExpression.append(")");   
    
    return regExpression.toString();
  
  }
  
  
  public boolean equals(Object otherObject) {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    ComplexBoolExpr other = (ComplexBoolExpr)otherObject;
    if(other.oper != null)
    {
      if(right != null)
      {
        if(other.getRight() == null)
          return false;
        else
          return (oper.equals(other.getOper()) && left.equals(other.getLeft())
                  && right.equals(other.getRight()));
      }
      else
      {
        if(other.getRight() != null)
          return false;
        else
          return (oper.equals(other.getOper()) && left.equals(other.getLeft()));
      }
    }
    return false;
  }
  
  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorComplexBooleanExpression>");
    sb.append(super.toString());
    if(oper != null)
      sb.append("<LogicalOperator oper=\"" + oper + "\" />");

   
      sb.append("<Left>");
      sb.append(left.toString());
      sb.append("</Left>");
    

      if(right != null)
      {
        sb.append("<Right>");
        sb.append(right.toString());
        sb.append("</Right>");
      }
    

    sb.append("</PhysicalOperatorComplexBooleanExpression>");
    return sb.toString();
  }
  
  //Create and return visualiser compatible XML Plan
  public String getXMLPlan2() {
    StringBuilder xml = new StringBuilder();
    if(left != null)
      xml.append(left.getXMLPlan2());
    xml.append(oper.getHtmlExpression());
    if(right != null)
      xml.append(right.getXMLPlan2());
    return xml.toString();
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    assert left != null;
    
    left.getAllReferencedAttrs(attrs);
    if(right != null)
      right.getAllReferencedAttrs(attrs);
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  {
    BoolExpr left;
    BoolExpr right;
    StringBuilder regExpression = new StringBuilder();

    left = (BoolExpr)this.getLeft();
    right = (BoolExpr)this.getRight();

    assert left != null;
    LogicalOp oper = this.getOper();

    regExpression.append(" (");
    if(right != null)
    {
      // Operators like AND, OR
      String temp = left.getSQLEquivalent(ec);
      if(temp == null)
        return null;
      regExpression.append(temp);
      regExpression.append(" "+oper.getSymbol()+" ");
      temp = right.getSQLEquivalent(ec);
      if(temp == null)
        return null;
      regExpression.append(temp);
    }
    else
    {
      // Unary operators
      String temp = left.getSQLEquivalent(ec);
      if(temp == null)
        return null;
      regExpression.append(oper.getSymbol()+" ");
      regExpression.append(temp);
    }
    regExpression.append(") ");   
    
    return regExpression.toString();
  }
  
}


