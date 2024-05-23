/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprTimestamp.java /main/2 2011/05/17 03:26:06 anasrini Exp $ */

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
    udeshmuk    02/21/08 - Creation
 */

/**
 *  @version $Header: ExprTimestamp.java 21-feb-2008.04:58:22 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan.expr;

/**
 * Timestamp logical operator class definition
 */
import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrUnNamed;

public class ExprTimestamp extends Expr implements Cloneable{
  
  long timestampVal;
  
  public ExprTimestamp(long value, Datatype dt)
  {
    assert dt == Datatype.TIMESTAMP;
    setType(dt);
    this.timestampVal = value;
  }
  
  public long getTValue() {
    return timestampVal;
  }

  public void setTValue(long value) {
    this.timestampVal = value;
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
    
    ExprTimestamp other = (ExprTimestamp) otherObject;
    long l      = other.getTValue();
    
    return (l == this.timestampVal);
  }

  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<LogicalTimestampExpression>");
    sb.append(super.toString());
    sb.append("<Value tValue=\"" + timestampVal + "\" />");

    sb.append("</LogicalTimestampExpression>");
    return sb.toString();
  }
}
