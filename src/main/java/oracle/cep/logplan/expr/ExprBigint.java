/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprBigint.java /main/2 2011/05/17 03:26:06 anasrini Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Big Integer Logical Operator Expression Class Definition

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sborah       04/11/11 - override getAllReferencedAttrs()
 hopark       10/17/06 - Creation
 */

/**
 *  @version $Header: ExprBigint.java 13-oct-2006.11:07:09 parujain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr;

import java.util.List;

import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrUnNamed;
import oracle.cep.common.Datatype;

/**
 * Integer Logical Operator Expression Class Definition
 */
public class ExprBigint extends Expr implements Cloneable {
  /** big integer value */
  long lValue;

  public ExprBigint(long lValue, Datatype dt) {
  	assert dt == Datatype.BIGINT;
    setType(dt);
    this.lValue = lValue;
  }

  public long getLValue() {
    return lValue;
  }

  public void setLValue(long lValue) {
    this.lValue = lValue;
  }

  public Attr getAttr() {
    AttrUnNamed attr = new AttrUnNamed(getType());
    return (Attr) attr;
  }

  public boolean check_reference(LogOpt op) {
    return true;
  }
  
  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    return;
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, boolean includeAggrParams)
  {
    return;
  } 
    
  public boolean equals(Object otherObject) {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    ExprBigint other = (ExprBigint) otherObject;
    return (lValue == other.getLValue());
  }
    

  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<LogicalBigintExpression>");
    sb.append(super.toString());
    sb.append("<Value lValue=\"" + lValue + "\" />");

    sb.append("</LogicalBigintExpression>");
    return sb.toString();
  }

}
