/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprBoolean.java /main/2 2011/05/17 03:26:06 anasrini Exp $ */

/* Copyright (c) 2008, 2011, Oracle and/or its affiliates. 
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
    mthatte     01/14/08 - 
    najain      01/02/08 - Creation
 */

/**
 *  @version $Header: ExprBoolean.java 14-jan-2008.14:04:27 mthatte Exp $
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
 * Boolean Logical Operator Expression Class Definition
 */
public class ExprBoolean extends Expr implements Cloneable {
  /** boolean value */
  boolean bvalue;

  public ExprBoolean(boolean bvalue, Datatype dt) {
    assert dt == Datatype.BOOLEAN;
    setType(dt);
    this.bvalue = bvalue;
  }

  public boolean getBValue() {
    return bvalue;
  }

  public void setBValue(boolean bvalue) {
    this.bvalue = bvalue;
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
    
    ExprBoolean other = (ExprBoolean) otherObject;
    return (bvalue == other.getBValue());
  }
    

  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<LogicalBooleanExpression>");
    sb.append(super.toString());
    sb.append("<Value bvalue=\"" + bvalue + "\" />");

    sb.append("</LogicalBooleanExpression>");
    return sb.toString();
  }

}
