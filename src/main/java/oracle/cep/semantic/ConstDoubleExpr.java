/* $Header: ConstDoubleExpr.java 30-jan-2008.00:51:23 udeshmuk Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    01/30/08 - Creation
 */

/**
 *  @version $Header: ConstDoubleExpr.java 30-jan-2008.00:51:23 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import java.util.List;

import oracle.cep.common.Datatype;

/**
 * Class representing a double constant expression
 * @author udeshmuk
 */

public class ConstDoubleExpr extends Expr {

  private double value;
 
  @Override
  public ExprType getExprType()
  {
    return ExprType.E_CONST_VAL;
  }

  @Override
  public Datatype getReturnType()
  {
    return dt;
  }

  /**
   * Constructor
   * @param value double constant value
   */
  public ConstDoubleExpr(double value)
  {
    this.value = value;
    this.dt = Datatype.DOUBLE;
    setName(String.valueOf(value), false);
  }
  
  /**
   * get the double constant value
   * @return double constant value
   */
  public double getValue()
  {
    return value;
  }
  
  @Override
  public void getAllReferencedAggrs(List<AggrExpr> aggrs)
  {
    return;
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type)
  {
    return;
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type,
      boolean includeAggrParams)
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
    
    ConstDoubleExpr other = (ConstDoubleExpr) otherObject;
    return (value == other.getValue());
  }
  
  // toString
  public String toString() {
    return "<ConstExpr datatype=\"" + getReturnType() + "\" value=\"" + value + "\" />";
  }
}

