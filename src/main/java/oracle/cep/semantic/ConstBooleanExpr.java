/* $Header: ConstBooleanExpr.java 14-jan-2008.14:04:46 mthatte Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    mthatte     01/14/08 - 
    najain      01/02/08 - Creation
 */

/**
 *  @version $Header: ConstBooleanExpr.java 14-jan-2008.14:04:46 mthatte Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import java.util.List;

import oracle.cep.common.Datatype;

/**
 * Class representing an boolean constant expression
 *
 * @since 1.0
 */

public class ConstBooleanExpr extends Expr {

  private boolean value;

  public ExprType getExprType() {
    return ExprType.E_CONST_VAL;
  }

  public Datatype getReturnType() {
    return dt;
  }

  /**
   * Constructor
   * @param value the boolean constant value
   */
  public ConstBooleanExpr(boolean value) {
    this.value = value;
    this.dt = Datatype.BOOLEAN;
    setName(String.valueOf(value), false);
  }

  /**
   * Get the boolean constant value
   * @return the boolean constant value
   */
  public boolean getValue() {
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
    
    ConstBooleanExpr other = (ConstBooleanExpr) otherObject;
    return (value == other.getValue());
  }

  // toString
  public String toString() {
    return "<ConstExpr datatype=\"" + getReturnType() + "\" value=\"" + value + "\" />";
  }

}
