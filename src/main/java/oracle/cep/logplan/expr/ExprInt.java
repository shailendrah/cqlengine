/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprInt.java /main/2 2011/05/17 03:26:06 anasrini Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Integer Logical Operator Expression Class Definition

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
 *  @version $Header: ExprInt.java 13-oct-2006.11:07:09 parujain Exp $
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
public class ExprInt extends Expr implements Cloneable {
  /** integer value */
  int iValue;

  public ExprInt(int iValue, Datatype dt) {
  	assert dt == Datatype.INT;
    setType(dt);
    this.iValue = iValue;
  }

  public int getIValue() {
    return iValue;
  }

  public void setIValue(int iValue) {
    this.iValue = iValue;
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
    
    ExprInt other = (ExprInt) otherObject;
    return (iValue == other.getIValue());
  }
    

  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<LogicalIntExpression>");
    sb.append(super.toString());
    sb.append("<Value iValue=\"" + iValue + "\" />");

    sb.append("</LogicalIntExpression>");
    return sb.toString();
  }

}
