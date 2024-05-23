/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/BaseBExpr.java /main/8 2012/07/30 19:52:52 pkali Exp $ */

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
    rkomurav    05/28/07 - add equals
    rkomurav    11/08/06 - outer join support
    parujain    10/31/06 - Comparison boolean Expr
    parujain    10/31/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/BaseBExpr.java /main/8 2012/07/30 19:52:52 pkali Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import java.util.List;

import oracle.cep.common.CompOp;
import oracle.cep.common.UnaryOp;
import oracle.cep.common.Datatype;
import oracle.cep.common.OuterJoinType;

/**
 * Post semantic analysis representation of a base boolean expression
 * <p>
 * This is a boolean expression that does not involve any logical operators
 * such as AND. This involves only comparison operators.
 *
 * @since 1.0
 */

public class BaseBExpr extends BExpr {
  private CompOp        compOp;
  private UnaryOp       unaryOp;
  private OuterJoinType outerJoinType;
  private Expr          leftOperand;
  private Expr          rightOperand;
  private Expr          unaryOperand;

  /**
   * Constructor for a boolean expression involving a comparison
   * operator
   * @param compOp the comparison operator
   * @param leftOperand the left operand
   * @param rightOperand the right operand
   */
  public BaseBExpr(CompOp compOp, Expr leftOperand, Expr rightOperand) {
    this.compOp        = compOp;
    this.leftOperand   = leftOperand;
    this.rightOperand  = rightOperand;
    this.unaryOp       = null;
    this.unaryOperand  = null;
    this.dt            = Datatype.BOOLEAN;
    this.outerJoinType = null;
  }
  
  /**
   * Constructor for a boolean expr involving comparsition operator and outer join
   * @param compOp the comparison operator
   * @param leftOperand the left operand
   * @param rightOperand the right operand
   * @param outerJoinType the outer join type
   */
  public BaseBExpr(CompOp compOp, Expr leftOperand, Expr rightOperand, OuterJoinType outerJoinType) {
    this.compOp        = compOp;
    this.leftOperand   = leftOperand;
    this.rightOperand  = rightOperand;
    this.unaryOp       = null;
    this.unaryOperand  = null;
    this.dt            = Datatype.BOOLEAN;
    this.outerJoinType = outerJoinType;
  }
  
  /**
   * Constructor for a boolean expression involving a unary
   * operator
   * @param unaryOp the unary operator
   * @param unaryOperand the unary operand
   */
  public BaseBExpr(UnaryOp unaryOp, Expr unaryOperand) {
    this.compOp        = null;
    this.leftOperand   = null;
    this.rightOperand  = null;
    this.unaryOp       = unaryOp;
    this.unaryOperand  = unaryOperand;
    this.dt            = Datatype.BOOLEAN;
    this.outerJoinType = null;
  }

  public ExprType getExprType() {
    return ExprType.E_BOOL_EXPR;
  }

  public Datatype getReturnType() {
    return dt;
  }

  /**
   * Get the comparison operator
   * @return the comparison operator
   */
  public CompOp getCompOp() {
    return compOp;
  }

  /**
   * Get the unary operator
   * @return unary operator
   */
  public UnaryOp getUnaryOp() {
    return unaryOp;
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
   * Get the unary operand when unary operator
   * @return unary operand 
   */
  public Expr getUnaryOperand() {
    return unaryOperand;  
  }
  
  public void getAllReferencedAggrs(List<AggrExpr> aggrs)
  {
    if(compOp != null)
    {
      leftOperand.getAllReferencedAggrs(aggrs);
      rightOperand.getAllReferencedAggrs(aggrs);
    }
    else
    {
      unaryOperand.getAllReferencedAggrs(aggrs);
    }
  }
  
  public Expr getRewrittenExprForGroupBy(Expr[] gbyExprs)
  {
    if (compOp != null)
    {
      // logical expression are not allowed in group by expressions
      // so no need to match the whole expression as in ComplexExpr.java
      Expr leftExpr = leftOperand.getRewrittenExprForGroupBy(gbyExprs);
      if(leftExpr != null)
      {
        Expr rightExpr =rightOperand.getRewrittenExprForGroupBy(gbyExprs);
        if(rightExpr != null)
        {
          BaseBExpr baseBExpr = new BaseBExpr(this.compOp, leftExpr, 
                                          rightExpr, this.outerJoinType);
          baseBExpr.setName(this.getName(), 
                            this.isUserSpecifiedName(), this.isExternal());
          baseBExpr.setAlias(this.getAlias());
          baseBExpr.setbNull(this.isNull());
          return baseBExpr;
        }
      }
    }
    else
    {
      Expr unaryExpr = unaryOperand.getRewrittenExprForGroupBy(gbyExprs);
      if(unaryExpr == null)
        return null;
      BaseBExpr baseBExpr = new BaseBExpr(this.unaryOp, unaryExpr);
      baseBExpr.setName(this.getName(), 
                        this.isUserSpecifiedName(), this.isExternal());
      baseBExpr.setAlias(this.getAlias());
      baseBExpr.setbNull(this.isNull());
      return baseBExpr;
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
    if(compOp != null)
    {
      leftOperand.getAllReferencedAttrs(attrs, type, includeAggrParams);
      rightOperand.getAllReferencedAttrs(attrs, type, includeAggrParams);
    }
    else
    {
      unaryOperand.getAllReferencedAttrs(attrs, type, includeAggrParams);
    }
  }
  
  public boolean equals(Object otherObject) {
    if(this == otherObject)
      return true;
    if(otherObject == null)
      return false;
    if(getClass() != otherObject.getClass())
      return false;
    BaseBExpr other = (BaseBExpr) otherObject;
    
    if((this.compOp != null && other.compOp == null) || (this.compOp == null && other.compOp != null))
      return false;
    
    if((this.unaryOp != null && other.unaryOp == null) || (this.unaryOp == null && other.unaryOp != null))
      return false;
    
    if(other.compOp != null)
    {
      if(other.outerJoinType != null)
        return (compOp.equals(other.getCompOp()) &&
            leftOperand.equals(other.getLeftOperand()) &&
            rightOperand.equals(other.getRightOperand()) &&
            outerJoinType.equals(other.outerJoinType));
      else
        return (compOp.equals(other.getCompOp()) && 
            leftOperand.equals(other.getLeftOperand()) &&
            rightOperand.equals(other.getRightOperand()));
    }
    else
    {
      return (unaryOp.equals(other.getUnaryOp()) && 
          unaryOperand.equals(other.getUnaryOperand()));
    }
  }
  
  // toString
  public String toString() {
    StringBuilder sb = new StringBuilder();

    if(compOp != null)
    {
      sb.append("<BaseBExpr compOp=\"" + compOp + "\" >");
      sb.append("<LeftOperand>" + leftOperand.toString() + "</LeftOperand>");
      sb.append("<RightOperand>" + rightOperand.toString() + "</RightOperand>");
      if(outerJoinType != null)
        sb.append("<JoinType>" + outerJoinType.getOuterJoinType() + "</JoinType>");
    }
    else
    {
      sb.append("<BaseBExpr unaryOp=\"" + unaryOp + "\" >");
      sb.append("<LeftOperand>" + unaryOperand.toString() + "</LeftOperand>");
    }
    sb.append("</BaseBExpr>");

    return sb.toString();
  }
}
