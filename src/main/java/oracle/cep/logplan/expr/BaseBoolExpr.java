/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/BaseBoolExpr.java /main/7 2014/12/10 18:12:47 sbishnoi Exp $ */

/* Copyright (c) 2006, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    11/25/14 - fix usage of expr in gby clause
    sborah      04/11/11 - override getAllReferencedAttrs()
    parujain    02/20/09 - outerjoin for external relation
    parujain    12/18/07 - outerjoin with external relations
    najain      10/04/07 - bug fix
    rkomurav    11/08/06 - outer join support
    parujain    10/31/06 - Base Boolean Expr
    parujain    10/31/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/BaseBoolExpr.java /main/7 2014/12/10 18:12:47 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan.expr;

import java.util.List;

import oracle.cep.common.CompOp;
import oracle.cep.common.UnaryOp;
import oracle.cep.common.Datatype;
import oracle.cep.common.OuterJoinType;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;

/**
 * Base Boolean Expression Logical Operator Class Definition
 */
public class BaseBoolExpr extends BoolExpr implements Cloneable {
  /** Comparison Operator */
  CompOp compOper;
  
  /** Unary Operator */
  UnaryOp unaryOper;
  
  /** Join Type */
  OuterJoinType outerJoinType;

  /** Left Expression */
  Expr   left;

  /** Right Expression */
  Expr   right;
  
  /** Unary Expression */
  Expr   unary;

  public BaseBoolExpr(CompOp oper, Expr left, Expr right, Datatype dt) {
    setType(dt);
    
    this.compOper  = oper;
    this.left      = left;
    this.right     = right;
    this.unary     = null;
    this.unaryOper = null;
    this.outerJoinType  = null;
  }
  
  public BaseBoolExpr(CompOp oper, Expr left, Expr right, Datatype dt, OuterJoinType joinType) {
    setType(dt);
    
    this.compOper  = oper;
    this.left      = left;
    this.right     = right;
    this.unary     = null;
    this.unaryOper = null;
    this.outerJoinType  = joinType;
  }
  
  public BaseBoolExpr(UnaryOp oper, Expr unary, Datatype dt) {
    setType(dt);
    
    this.compOper  = null;
    this.unaryOper = oper;
    this.left      = null;
    this.right     = null;
    this.unary     = unary;
    this.outerJoinType  = null;
  }

  // clone has been re-implemented to perform a deep copy instead of the
  // default shallow copy
  public BaseBoolExpr clone() throws CloneNotSupportedException {
    BaseBoolExpr op = (BaseBoolExpr) super.clone();
    if(op.compOper != null)
      op.compOper = (CompOp) this.compOper.clonedummy();
    if(op.unaryOper != null)
      op.unaryOper = (UnaryOp) this.unaryOper.clonedummy();
    if(op.outerJoinType != null)
      op.outerJoinType  = (OuterJoinType) this.outerJoinType.clonedummy();
    if(op.left != null)
       op.left = (Expr) this.left.clone();
    if(op.right != null)
      op.right = (Expr) this.right.clone();
    if(op.unary != null)
      op.unary = (Expr)this.unary.clone();
    return op;
  }

  public Expr getLeft() {
    return left;
  }

  public Expr getRight() {
    return right;
  }

  public Expr getUnary() {
    return unary;
  }
  
  public CompOp getOper() {
    return compOper;
  }
  
  public OuterJoinType getOuterJoinType() {
    return outerJoinType;
  }

  public UnaryOp getUnaryOper(){
    return unaryOper;
  }
  
  public void setLeft(Expr left) {
    this.left = left;
  }

  public void setRight(Expr right) {
    this.right = right;
  }

  public void setUnary(Expr unary) {
    this.unary = unary;
  }
  
  public boolean check_reference(LogOpt op) {
    if (unary != null)
      return unary.check_reference(op);
    if (left != null)
    {
      if (right != null)
	return (left.check_reference(op) && right.check_reference(op));
      return left.check_reference(op);
    }
    else if (right != null)
      return right.check_reference(op);

    return false;
  }

  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    getAllReferencedAttrs(attrs, true);
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs, boolean includeAggrParams)
  {
    if(compOper != null)
    {
      left.getAllReferencedAttrs(attrs, includeAggrParams);
      right.getAllReferencedAttrs(attrs, includeAggrParams);
    }
    else
    {
      unary.getAllReferencedAttrs(attrs, includeAggrParams);
    }
  }
  
  public boolean equals(Object otherObject) {
    if(this == otherObject)
      return true;
    if(otherObject == null)
      return false;
    if(getClass() != otherObject.getClass())
      return false;
    BaseBoolExpr other = (BaseBoolExpr) otherObject;
    
    if((other.compOper != null && this.compOper == null)||(other.compOper == null && this.compOper != null))
      return false;
    
    if((other.unaryOper != null && this.unaryOper == null)||(other.unaryOper == null && this.unaryOper != null))
      return false;
    
    if(other.compOper != null)
    {
      if(other.outerJoinType != null)
        return (compOper.equals(other.getOper()) && left.equals(other.getLeft())
              && right.equals(other.getRight()) && outerJoinType.equals(other.outerJoinType));
      else
        return (compOper.equals(other.getOper()) && left.equals(other.getLeft())
              && right.equals(other.getRight()));
    }
    else
    {
      return (unaryOper.equals(other.getUnaryOper()) && unary.equals(other.getUnary()));
    }
  }
  
  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<LogicalBaseBooleanExpression>");
    sb.append(super.toString());
    if(compOper != null)
    {
      sb.append("<ComparisonOperator compOp=\"" + compOper + "\" />");
      sb.append(left.toString());
      sb.append(right.toString());
      if(outerJoinType != null)
        sb.append(outerJoinType.getOuterJoinType());
    }
    else if(unaryOper != null)
    {
    	sb.append("<ComparisonUnaryOperator unaryOp=\"" + unaryOper + "\" />");	
      sb.append(unary.toString());
    }
    sb.append("</LogicalBaseBooleanExpression>");
    return sb.toString();
  }

@Override
public boolean isValidOuterJoin() {
  if(this.outerJoinType == null)
    return true;
  // If no one is external relation
  if(!(left.isExternal || right.isExternal))
    return true;
  // CQL doesn't support FULL OUTER JOIN of stream and external relation
  if((left.isExternal && (outerJoinType == OuterJoinType.RIGHT_OUTER))
   ||(right.isExternal && (outerJoinType == OuterJoinType.LEFT_OUTER)))
     return true;
  return false;
}

@Override
public boolean isOuterJoin()
{
  return (this.outerJoinType != null);
}

}
