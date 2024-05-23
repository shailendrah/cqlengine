/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ComplexBoolExpr.java /main/6 2011/05/17 03:26:06 anasrini Exp $ */

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
    sborah      04/11/11 - override getAllReferencedAttrs()
    parujain    02/20/09 - outerjoin for external relation
    parujain    12/18/07 - outerjoin with external relations
    najain      10/04/07 - bug fix
    parujain    02/05/07 - fix toString
    parujain    10/31/06 - Complex Boolean Expr
    parujain    10/31/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/ComplexBoolExpr.java /main/5 2009/03/04 20:01:25 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr;

import java.util.List;

import oracle.cep.common.LogicalOp;
import oracle.cep.common.Datatype;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;

/**
 * Complex Boolean Expression Logical Operator Class Definition
 */
public class ComplexBoolExpr extends BoolExpr implements Cloneable {
  /** Logical Operator */
  LogicalOp logOper;

  /** Left Expression */
  Expr   left;

  /** Right Expression */
  Expr   right;
  

  public ComplexBoolExpr(LogicalOp oper, Expr left, Expr right, Datatype dt) {
    setType(dt);
    
    this.logOper = oper;
    this.left = left;
    this.right = right;
  }
  

  // clone has been re-implemented to perform a deep copy instead of the
  // default shallow copy
  public ComplexBoolExpr clone() throws CloneNotSupportedException {
    ComplexBoolExpr op = (ComplexBoolExpr) super.clone();
    if(op.logOper != null)
      op.logOper = (LogicalOp) this.logOper.clonedummy();
   
    if(op.left != null)
       op.left = (Expr) this.left.clone();
    if(op.right != null)
      op.right = (Expr) this.right.clone();
    
    return op;
  }

  public Expr getLeft() {
    return left;
  }

  public Expr getRight() {
    return right;
  }
  
  public LogicalOp getOper() {
    return logOper;
  }

  public void setLeft(Expr left) {
    this.left = left;
  }

  public void setRight(Expr right) {
    this.right = right;
  }
  
  public boolean check_reference(LogOpt op) {
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
  
  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    getAllReferencedAttrs(attrs, true);
  }
  
  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, boolean includeAggrParams)
  {
    if(left != null)
      left.getAllReferencedAttrs(attrs, includeAggrParams);
    if(right != null)
      right.getAllReferencedAttrs(attrs, includeAggrParams);
  }

  public boolean equals(Object otherObject) {
    if(this == otherObject)
      return true;
    if(otherObject == null)
      return false;
    if(getClass() != otherObject.getClass())
      return false;
    ComplexBoolExpr other = (ComplexBoolExpr) otherObject;
    if(other.logOper != null)
    {
      return (logOper.equals(other.getOper()) && left.equals(other.getLeft())
              && right.equals(other.getRight()));
    }
    return false;
  }
  
  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<LogicalComplexBooleanExpression>");
    sb.append(super.toString());
    sb.append("<LogicalOperator logOp=\"" + logOper + "\" />");
    if(left != null)
      sb.append(left.toString());
    if(right != null)
      sb.append(right.toString());
    
    sb.append("</LogicalComplexBooleanExpression>");
    return sb.toString();
  }


@Override
public boolean isValidOuterJoin() {
  if(this.right != null)
    return (((BoolExpr)this.left).isValidOuterJoin() 
         && ((BoolExpr)this.right).isValidOuterJoin());
  return((BoolExpr)this.left).isValidOuterJoin();
}

@Override
public boolean isOuterJoin()
{
  if(this.right != null)
  { 
    return (((BoolExpr)this.left).isOuterJoin() ||
            ((BoolExpr)this.right).isOuterJoin());
  }
  return((BoolExpr)this.left).isOuterJoin();
}

}

