/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprBigDecimal.java /main/2 2011/05/17 03:26:06 anasrini Exp $ */

/* Copyright (c) 2009, 2011, Oracle and/or its affiliates. 
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
    sborah      06/23/09 - support for bigdecimal
    sborah      06/23/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprBigDecimal.java /main/1 2009/11/09 10:10:58 sborah Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan.expr;

import java.math.BigDecimal;
import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrUnNamed;

/**
 * BigDecimal logical expression representation
 */

public class ExprBigDecimal extends Expr implements Cloneable {
  
  /** BigDecimal value */
  BigDecimal nValue;

  public ExprBigDecimal(BigDecimal nValue, Datatype dt)
  {
    assert dt == Datatype.BIGDECIMAL;
    setType(dt);
    this.nValue = nValue;
  }
  
  public BigDecimal getNValue()
  {
    return this.nValue;
  }
  
  public void setNValue(BigDecimal nValue)
  {
    this.nValue = nValue;  
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
    
    ExprBigDecimal other = (ExprBigDecimal) otherObject;
    return(nValue.compareTo(other.getNValue()) == 0);
  }
  
//toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<LogicalBigDecimalExpression>");
    sb.append(super.toString());
    sb.append("<Value nValue=\"" + nValue + "\" />");

    sb.append("</LogicalBigDecimalExpression>");
    return sb.toString();
  }
  
}

