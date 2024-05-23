/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/BaseBoolExpr.java /main/10 2011/07/09 08:53:44 udeshmuk Exp $ */

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
    parujain    05/27/09 - fix signature
    sborah      04/20/09 - define getSignature
    rkomurav    06/11/07 - fix passing attrfactory part
    rkomurav    03/06/07 - restructure exprfactorycontext
    parujain    12/20/06 - fix comparison for unary
    rkomurav    11/09/06 - outer join support
    parujain    10/31/06 - Base Boolean Expr
    parujain    10/31/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/BaseBoolExpr.java /main/8 2009/12/03 21:27:59 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.expr;

import java.util.List;

import oracle.cep.common.CompOp;
import oracle.cep.common.Datatype;
import oracle.cep.common.UnaryOp;
import oracle.cep.common.OuterJoinType;
import oracle.cep.extensibility.expr.BaseBooleanExpression;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;

/**
 * Base Boolean Expression Logical Operator Class Definition
 */
public class BaseBoolExpr
    extends BoolExpr
    implements BaseBooleanExpression
{
  /** Comparison Operator */
  CompOp        oper;
  
  /** Unary Operator */
  UnaryOp       unaryOper;

  /** Join Type */
  OuterJoinType outerJoinType;
  
  /** Left Expression */
  Expr          left;

  /** Right Expression */
  Expr          right;
  
  /** Unary Expression */
  Expr          unary;

  public BaseBoolExpr(CompOp oper, Expr left, Expr right,
      OuterJoinType outerJoinType, Datatype dt)
  {
    super(ExprKind.BASE_BOOL_EXPR);
    setType(dt);
    
    this.oper          = oper;
    this.outerJoinType = outerJoinType;
    this.left          = left;
    this.right         = right;
    this.unary         = null;
    this.unaryOper     = null;
  }
  
  public BaseBoolExpr(UnaryOp oper, Expr unary, Datatype dt)
  {
    super(ExprKind.BASE_BOOL_EXPR);
    setType(dt);
    
    this.oper          = null;
    this.outerJoinType = null;
    this.left          = null;
    this.right         = null;
    this.unary         = unary;
    this.unaryOper     = oper;
  }

  // getter methods

  /**
   * Get the comparison operator
   * 
   * @return the comparison operator
   */
  public CompOp getOper() {
    return oper;
  }

  public CompOp getOperator() {
      return getOper();
  }

  /**
   * Get the unary operator 
   * 
   * @return the unary operator
   */
  public UnaryOp getUnaryOper() {
    return unaryOper;
  }

  public UnaryOp getUnaryOperator() {
      return getUnaryOper();
  }
  
  /**
   * Get the outer join type
   * @return Returns the outerJoinType.
   */
  public OuterJoinType getOuterJoinType() {
    return outerJoinType;
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
   * Get the unary operand
   * 
   * @return the unary operand
   */
  public Expr getUnary() {
    return unary;
  }

  public Expr getUnaryExpression()
  {
      return getUnary();
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
   * Sets the unary operand
   * 
   * @param unary Unary operand to set
   */
  public void setUnary(Expr unary) {
    this.unary = unary;
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * 
   * A boolean expression contains either binary operators like 
   * AND, OR , XOR or unary operators like NOT, IS NULL , IS NOT NULL
   * 
   * @return 
   *      A concise String representation of the Expression.
   */
   public String getSignature()
   {
     //BaseBoolExpr expr = (BaseBoolExpr)bexpr;    
     StringBuilder regExpression = new StringBuilder();
     
     regExpression.append("(");
     
     if(this.getUnaryOper() != null)
     {
       // if the expresion has an Unary operator
       regExpression.append(this.getUnary().getSignature());
       regExpression.append(this.getUnaryOper().getSymbol());
     }
     else
     {
       // binary opearator.
       regExpression.append(this.getLeft().getSignature());
       if(outerJoinType != null)
       {
         if(outerJoinType == OuterJoinType.RIGHT_OUTER || outerJoinType == OuterJoinType.FULL_OUTER)
           regExpression.append("(+)");
       }
       regExpression.append(this.getOper().getSymbol());
       regExpression.append(this.getRight().getSignature());
       if(outerJoinType != null)
       {
         if(outerJoinType == OuterJoinType.LEFT_OUTER || outerJoinType == OuterJoinType.FULL_OUTER)
           regExpression.append("(+)");
       }
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
    
    BaseBoolExpr other = (BaseBoolExpr)otherObject;
    if(other.right != null)
    {
      if(right == null)
        return false;
      if(left == null)
        return false;
      if(other.left == null)
        return false;
    
      if(outerJoinType != null)
        return (oper.equals(other.getOper()) && left.equals(other.getLeft())
              && right.equals(other.getRight()) && outerJoinType.equals(other.outerJoinType));
      else
        return (oper.equals(other.getOper()) && left.equals(other.getLeft())
              && right.equals(other.getRight()));
    }
    else
    {
      if(unary == null)
        return false;
      if(other.unary == null)
        return false;
    
      return (unaryOper.equals(other.getUnaryOper()) && unary.equals(other.getUnary()));
    }
  }
  
  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorBooleanExpression>");
    sb.append(super.toString());
    if(oper != null)
      sb.append("<ComparisonOperator oper=\"" + oper + "\" />");
    else
    {
      assert unaryOper != null;
      sb.append("<UnaryOperator oper=\"" + unaryOper + "\" />");
    }
    
    if(outerJoinType != null)
      sb.append("<OuterJoinType>" + outerJoinType.getOuterJoinType() + "</OuterJoinType>");

    if(left != null)
    {
      sb.append("<Left>");
      sb.append(left.toString());
      sb.append("</Left>");
    }
    
    if(unary != null)
    {
      sb.append("<Unary>");
      sb.append(unary.toString());
      sb.append("</Unary>");
    }

    if(right != null)
    {
      sb.append("<Right>");
      sb.append(right.toString());
      sb.append("</Right>");
    }

    sb.append("</PhysicalOperatorBooleanExpression>");
    return sb.toString();
  }
  
  //Create and return visualiser compatible XML Plan
  public String getXMLPlan2() {
    StringBuilder xml = new StringBuilder();
    if(oper != null) {
      xml.append(left.getXMLPlan2());
      xml.append(oper.getHtmlExpression());
      xml.append(right.getXMLPlan2());
      if(outerJoinType != null) {
        xml.append(outerJoinType.getXMLPlan2()); 
      }
    }
    return xml.toString();
  }
  
  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    if(this.getUnaryOper() != null)
    {
      unary.getAllReferencedAttrs(attrs);
    }
    else
    {
      left.getAllReferencedAttrs(attrs);
      right.getAllReferencedAttrs(attrs);
    }
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  {
    StringBuilder regExpression = new StringBuilder();
    
    regExpression.append(" (");
    
    if(this.getUnaryOper() != null)
    {
      // if the expression has an Unary operator - IS NULL or IS NOT NULL
      String temp = this.getUnary().getSQLEquivalent(ec);
      if(temp != null) 
      {
        regExpression.append(temp);
        regExpression.append(" "+this.getUnaryOper().getSymbol());
      }
      else
        return null;
    }
    else
    {
      // binary operator.
      String temp = this.getLeft().getSQLEquivalent(ec);
      if(temp == null) 
        return null;
      regExpression.append(temp);
      if(outerJoinType != null)
      {
        if(outerJoinType == OuterJoinType.RIGHT_OUTER || outerJoinType == OuterJoinType.FULL_OUTER)
          regExpression.append("(+)");
      }
      regExpression.append(" "+this.getOper().getSymbol()+" ");
      temp = this.getRight().getSQLEquivalent(ec);
      if(temp == null)
        return null;
      regExpression.append(temp);
      if(outerJoinType != null)
      {
        if(outerJoinType == OuterJoinType.LEFT_OUTER || outerJoinType == OuterJoinType.FULL_OUTER) 
          regExpression.append("(+)");
      }
    }
    
    regExpression.append(") ");
    
    return regExpression.toString();
  }
}
