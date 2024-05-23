/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprComplex.java /main/6 2011/05/17 03:26:06 anasrini Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Complex Expression Logical Operator Class Definition

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sborah      04/11/11 - override getAllReferencedAttrs()
 sborah      06/23/09 - support for bigdecimal
 hopark      02/17/09 - support boolean as external datatype
 udeshmuk    01/31/08 - support for double data type.
 hopark      11/16/06 - add bigint datatype
 parujain    10/12/06 - return type from Semantic layer
 rkomurav    09/25/06 - adding equals method
 parujain    10/05/06 - Generic timestamp datatype
 dlenkov     09/22/06 - conversion support
 najain      05/30/06 - add check_reference 
 najain      02/08/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprComplex.java /main/5 2009/11/09 10:10:58 sborah Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr;

import java.util.List;

import oracle.cep.common.ArithOp;
import oracle.cep.common.Datatype;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrUnNamed;

/**
 * Complex Expression Logical Operator Expression Class Definition
 */
public class ExprComplex extends Expr implements Cloneable {
  /** Arithmetic Operator */
  ArithOp oper;

  /** Left Expression */
  Expr    left;

  /** Right Expression */
  Expr    right;

  
  public ExprComplex(ArithOp oper, Expr left, Expr right, Datatype dt) {

    if (right == null && (oper == ArithOp.ITOF || oper == ArithOp.LTOF))
      setType( Datatype.FLOAT);
    else if (right == null && (oper == ArithOp.ITOB || oper == ArithOp.LTOB))
      setType( Datatype.BOOLEAN);
    else if (right == null && oper == ArithOp.ITOL)
      setType( Datatype.BIGINT);
    else if (right == null && oper == ArithOp.CTOT)
    	setType(Datatype.TIMESTAMP);
    else if (right == null && (oper == ArithOp.ITOD || oper == ArithOp.LTOD
             || oper == ArithOp.FTOD))
      setType(Datatype.DOUBLE);
    else if (right == null && (oper == ArithOp.ITON || oper == ArithOp.LTON
        || oper == ArithOp.FTON || oper == ArithOp.DTON))
      setType(Datatype.BIGDECIMAL);
    else
      setType( dt);
    
    this.oper = oper;
    this.left = left;
    this.right = right;
  }

  public ArithOp getOper() {
    return oper;
  }

  public Expr getLeft() {
    return left;
  }

  public Expr getRight() {
    return right;
  }

  // clone has been re-implemented to perform a deep copy instead of the
  // default shallow copy
  public ExprComplex clone() throws CloneNotSupportedException {
    ExprComplex exp = (ExprComplex) super.clone();

    // TODO: this can be further optimized to allocate the whole 
    // array in a batch
    exp.oper = (ArithOp) this.oper.clonedummy();
    exp.left = (Expr) this.left.clone();
    if (this.right == null)
      exp.right = null;
    else
      exp.right = (Expr) this.right.clone();

    return exp;
  }

  public Attr getAttr() {
    AttrUnNamed attr = new AttrUnNamed(getType());    
    return (Attr)attr;
  }

  public boolean check_reference(LogOpt op) {
    if (!left.check_reference(op))
      return false;

    if (right == null)
      return true;
    else
      return right.check_reference(op);
  }
  
  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    getAllReferencedAttrs(attrs,true);
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs, boolean includeAggrParams)
  {
    left.getAllReferencedAttrs(attrs, includeAggrParams);
    if(right != null)
      right.getAllReferencedAttrs(attrs, includeAggrParams);
  }
  
  @Override
  public boolean equals(Object otherObject) {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    ExprComplex other = (ExprComplex) otherObject;
    return (oper.equals(other.getOper()) && left.equals(other.getLeft())
        && right.equals(other.getRight()));
    
  }
    

  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<LogicalComplexExpression>");
    sb.append(super.toString());
    sb.append("<ArithmeticOperator arithOp=\"" + oper + "\" />");
    sb.append(left.toString());
    if (right != null)
      sb.append(right.toString());
    sb.append("</LogicalComplexExpression>");
    return sb.toString();
  }

}
