/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/ComplexExpr.java /main/7 2012/07/30 19:52:53 pkali Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Class representing a complex expression

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       07/17/12 - meta data propagation for rewritten groupby expr
    sbishnoi    06/07/12 - bug 14100326
    vikshukl    01/31/12 - group by expr
    rkomurav    06/25/07 - cleanup
    rkomurav    05/11/07 - add isClassB
    rkomurav    05/28/07 - add equals.
    parujain    10/12/06 - return type not lefttype
    dlenkov     09/28/06 - 
    najain      08/28/06 - expr is a abstract class
    anasrini    02/27/06 - fix xml closing in toString 
    anasrini    02/26/06 - implement toString 
    anasrini    02/23/06 - add javadoc comments, cache return type 
    anasrini    02/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/ComplexExpr.java /main/7 2012/07/30 19:52:53 pkali Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.common.ArithOp;

/**
 * Post semantic analysis representation of a complex expression.
 * <p>
 * A complex expression is one that is built on top of other expressions.
 *
 * @since 1.0
 */

public class ComplexExpr extends Expr {

  private ArithOp  arithOp;
  private Expr     leftOperand;
  private Expr     rightOperand;
  
  // Cache the return type, so that we need not compute every time
 // private Datatype dt;


  public ComplexExpr(ArithOp arithOp, Expr leftOperand,
		     Expr rightOperand, Datatype dtype) {
    this.arithOp      = arithOp;
    this.leftOperand  = leftOperand;
    this.rightOperand = rightOperand;
    this.dt           = dtype;
  }

  public ExprType getExprType() {
    return ExprType.E_COMP_EXPR;
  }

  public Datatype getReturnType() {
    return dt;
  }

  /**
   * Get the arithmetic operator
   * @return the arithmetic operator
   */
  public ArithOp getArithOp() {
    return arithOp;
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
  
  public void getAllReferencedAggrs(List<AggrExpr> aggrs)
  {
    leftOperand.getAllReferencedAggrs(aggrs);
    if(rightOperand != null)
      rightOperand.getAllReferencedAggrs(aggrs);
  }
  
  public Expr getRewrittenExprForGroupBy(Expr[] gbyExprs)
  {
    //check this expression is a group by expression
    //eg: select c1+c2 ... group by c1+c2
    for (int i=0; i < gbyExprs.length; i++)
    {
      if (this.equals(gbyExprs[i])) 
        return new GroupByExpr(this);
    }
    
    //check whether leftOperand or rightOperand is part of gby expr
    //eg: select c1+c2+10 ... group by c1+c2
    Expr leftExpr = leftOperand.getRewrittenExprForGroupBy(gbyExprs);
    if(leftExpr != null)
    {
      Expr rightExpr =rightOperand.getRewrittenExprForGroupBy(gbyExprs);
      if( rightExpr != null)
      {        
        ComplexExpr newCompExpr 
          = new ComplexExpr(this.arithOp, leftExpr, rightExpr, this.dt);
        newCompExpr.setName(this.getName(), 
                            this.isUserSpecifiedName(),
                            this.isExternal());
        newCompExpr.setAlias(this.getAlias());
        newCompExpr.setbNull(this.isNull());
        return newCompExpr;
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
    leftOperand.getAllReferencedAttrs(attrs, type, includeAggrParams);
    if(rightOperand != null)
      rightOperand.getAllReferencedAttrs(attrs, type, includeAggrParams);
  }
  
  public boolean equals(Object otherObject) {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    ComplexExpr other = (ComplexExpr) otherObject;
    return (arithOp.equals(other.getArithOp()) &&
        leftOperand.equals(other.getLeftOperand()) &&
        rightOperand.equals(other.getRightOperand()));
  }
  // toString

  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<ComplexExpr arithOp=\"" + arithOp + "\" >");
    sb.append("<LeftOperand>" + leftOperand.toString() + "</LeftOperand>");
    sb.append("<RightOperand>" + rightOperand.toString() + "</RightOperand>");
    sb.append("</ComplexExpr>");

    return sb.toString();
  }
}

