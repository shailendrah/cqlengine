/* $Header: ConstByteExpr.java 21-feb-2008.02:18:16 udeshmuk Exp $ */

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
 *  @version $Header: ConstByteExpr.java 21-feb-2008.02:18:16 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import java.util.List;

import oracle.cep.common.Datatype;

/**
 * Class representing a byte constant expression
 */

public class ConstByteExpr extends Expr {

  private byte[] value;

  public ExprType getExprType() {
    return ExprType.E_CONST_VAL;
  }

  public Datatype getReturnType() {
    return dt;
  }

  /**
   * Constructor
   * @param value the byte constant value
   */
  public ConstByteExpr(byte[] value) {
    this.value = value;
    this.dt = Datatype.BYTE;
    setName(String.valueOf(value), false);
  }

  /**
   * Get the byte constant value
   * @return the byte constant value
   */
  public byte[] getValue() {
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
    
    ConstByteExpr other = (ConstByteExpr)otherObject;
    byte[] oc           = other.getValue();
    
    if(oc.length != this.value.length)
      return false;
    for (int i=0; i < oc.length; i++)
    {
      if(oc[i]!=this.value[i])
        return false;
    }
    return true;
  }
  
  // toString
  public String toString() {
    return "<ConstExpr datatype=\"" + getReturnType() + "\" value=\"" + value + "\" />";
  }

}
