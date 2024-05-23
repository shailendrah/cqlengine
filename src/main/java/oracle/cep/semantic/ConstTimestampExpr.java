/* $Header: ConstTimestampExpr.java 21-feb-2008.02:17:09 udeshmuk Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    02/21/08 - Creation
 */

/**
 *  @version $Header: ConstTimestampExpr.java 21-feb-2008.02:17:09 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import java.util.List;

import oracle.cep.common.Datatype;

/**
* Class representing a constant timestamp expression
*/

public class ConstTimestampExpr extends Expr{

  private long value;
  
  public ExprType getExprType()
  {
     return ExprType.E_CONST_VAL;
  }

  
  public Datatype getReturnType()
  {
    return dt;
  }
  
  
  public ConstTimestampExpr(long val)
  {
    this.value = val;
    this.dt = Datatype.TIMESTAMP;
    setName(String.valueOf(value), false);
  }
  
  public long getValue()
  {
    return value;
  }
  
  public void getAllReferencedAggrs(List<AggrExpr> aggrs)
  {
    return;
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type)
  {
    return;
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type,
      boolean includeAggrParams)
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
    
    ConstTimestampExpr other = (ConstTimestampExpr) otherObject;
    long l      = other.getValue();
    
    return (l == this.value);
  }
  
//toString
  public String toString() {
    return "<ConstExpr datatype=\"" + getReturnType() + "\" value=\"" + value + "\" />";
  }
  
}
