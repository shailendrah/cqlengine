/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprFloat.java /main/2 2011/05/17 03:26:06 anasrini Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Float Logical Operator Expression Class Definition

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sborah      04/11/11 - override getAllReferencedAttrs()
 parujain    10/13/06 - getting returntype from Semantic
 rkomurav    09/25/06 - adding equals method
 najain      05/30/06 - add check_reference 
 najain      02/08/06 - Creation
 */

/**
 *  @version $Header: ExprFloat.java 13-oct-2006.11:07:00 parujain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrUnNamed;

/**
 * Float Logical Operator Expression Class Definition
 */
public class ExprFloat extends Expr implements Cloneable {
  /** float value */
  float fValue;

  public ExprFloat(float fValue, Datatype dt) {
  	assert dt == Datatype.FLOAT;
    setType(dt);
    this.fValue = fValue;
  }

  public float getFValue() {
    return fValue;
  }

  public void setFValue(float fValue) {
    this.fValue = fValue;
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
    
    ExprFloat other = (ExprFloat) otherObject;
    return (fValue == other.getFValue());
  }
    

  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<LogicalFloatExpression>");
    sb.append(super.toString());
    sb.append("<Value fValue=\"" + fValue + "\" />");

    sb.append("</LogicalFloatExpression>");
    return sb.toString();
  }

}
