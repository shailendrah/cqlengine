/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprDouble.java /main/2 2011/05/17 03:26:06 anasrini Exp $ */

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
    udeshmuk    01/30/08 - Creation
 */

/**
 *  @version $Header: ExprDouble.java 30-jan-2008.05:19:12 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrUnNamed;

/**
 * Double logical expression representation
 */

public class ExprDouble extends Expr implements Cloneable {
  
  /** double value */
  double dValue;

  public ExprDouble(double dValue, Datatype dt)
  {
    assert dt == Datatype.DOUBLE;
    setType(dt);
    this.dValue = dValue;
  }
  
  public double getDValue()
  {
    return this.dValue;
  }
  
  public void setDValue(double dValue)
  {
    this.dValue = dValue;  
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
    
  @Override
  public boolean equals(Object otherObject)
  {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    ExprDouble other = (ExprDouble) otherObject;
    return(dValue == other.getDValue());
  }
  
//toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<LogicalDoubleExpression>");
    sb.append(super.toString());
    sb.append("<Value dValue=\"" + dValue + "\" />");

    sb.append("</LogicalDoubleExpression>");
    return sb.toString();
  }
  
}
