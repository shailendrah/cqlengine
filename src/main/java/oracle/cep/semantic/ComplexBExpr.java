/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/ComplexBExpr.java /main/8 2012/07/30 19:52:53 pkali Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
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
    vikshukl    01/31/12 - group by expr
    rkomurav    06/25/07 - cleanup
    rkomurav    05/11/07 - add isClassB
    rkomurav    05/28/07 - add .equals
    parujain    02/05/07 - fix toString
    parujain    11/16/06 - NOT operator
    parujain    10/31/06 - Logical boolean Expr
    parujain    10/31/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/ComplexBExpr.java /main/8 2012/07/30 19:52:53 pkali Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import java.util.List;

import oracle.cep.common.LogicalOp;
import oracle.cep.common.Datatype;

/**
 * Post semantic analysis representation of a complex boolean expression
 * <p>
 * This is a boolean expression that does not involve any comparison operators
 * such as <, > etc.
 *
 * @since 1.0
 */

public class ComplexBExpr extends BExpr {
  private LogicalOp logOp;
  private Expr   leftOperand;
  private Expr   rightOperand;
  
  /**
   * Constructor for a boolean expression involving a logical
   * operator
   * @param logOp the Logical boolean operator
   * @param leftOperand the left operand
   * @param rightOperand the right operand
   */
  public ComplexBExpr(LogicalOp logOp, Expr leftOperand, Expr rightOperand) {
    this.logOp       = logOp;
    this.leftOperand  = leftOperand;
    this.rightOperand = rightOperand;
    this.dt = Datatype.BOOLEAN;
  }

  /**
   * Constructor for a boolean expression involving a logical
   * operator
   * @param logOp the Logical boolean operator
   * @param leftOperand the left operand
   */
  public ComplexBExpr(LogicalOp logOp, Expr leftOperand) {
    this.logOp       = logOp;
    this.leftOperand  = leftOperand;
    this.rightOperand = null;
    this.dt = Datatype.BOOLEAN;
  }

  public ExprType getExprType() {
    return ExprType.E_BOOL_EXPR;
  }

  public Datatype getReturnType() {
    return dt;
  }

  /**
   * Get the left operand
   * @return the left operand
   */
  public Expr getLeftOperand() {
    return leftOperand;
  } 

  /**
   * Get the right operand
   * @return the right operand
   */
  public Expr getRightOperand() {
    return rightOperand;
  }
  
  /**
   * Get the logical operator
   * @return Logical operator
   */
  public LogicalOp getLogicalOp() {
    return logOp;
  }
  
  public void getAllReferencedAggrs(List<AggrExpr> aggrs)
  {
    if(leftOperand != null)
      leftOperand.getAllReferencedAggrs(aggrs);
    if(rightOperand != null)
      rightOperand.getAllReferencedAggrs(aggrs);
  }
  
  public Expr getRewrittenExprForGroupBy(Expr[] gbyExprs)
  {
    Expr leftExpr = leftOperand.getRewrittenExprForGroupBy(gbyExprs);
    if(leftExpr != null)
    {
      Expr rightExpr =rightOperand.getRewrittenExprForGroupBy(gbyExprs);
      if( rightExpr != null)
      {
        ComplexBExpr complexBExpr = new ComplexBExpr(this.logOp, leftExpr, 
                                                           rightExpr);
        complexBExpr.setName(this.getName(), 
                             this.isUserSpecifiedName(), this.isExternal());
        complexBExpr.setAlias(this.getAlias());
        complexBExpr.setbNull(this.isNull());
        return complexBExpr;
      }
    }
    return null;
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type)
  {
    getAllReferencedAttrs(attrs, type, true);
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type,
      boolean includeAggrParams)
  {
    if(leftOperand != null)
      leftOperand.getAllReferencedAttrs(attrs, type, includeAggrParams);
    if(rightOperand != null)
      rightOperand.getAllReferencedAttrs(attrs, type, includeAggrParams);
  }
  
  public boolean equals(Object otherObject) {
    if(this == otherObject)
      return true;
    if(otherObject == null)
      return false;
    if(getClass() != otherObject.getClass())
      return false;
    ComplexBExpr other = (ComplexBExpr) otherObject;
    if(other.logOp != null)
    {
      return (logOp.equals(other.getLogicalOp()) &&
          leftOperand.equals(other.getLeftOperand()) &&
          rightOperand.equals(other.getRightOperand()));
    }
    return false;
  }
  
  //toString
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<ComplexBExpr compOp=\"" + logOp + "\" >");
    if(leftOperand != null)
      sb.append("<LeftOperand>" + leftOperand.toString() + "</LeftOperand>");
    if(rightOperand != null)
      sb.append("<RightOperand>" + rightOperand.toString() + "</RightOperand>");
    sb.append("</ComplexBExpr>");

    return sb.toString();
  }
  
}
