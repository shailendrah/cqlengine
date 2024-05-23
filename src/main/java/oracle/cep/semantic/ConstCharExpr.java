/* $Header: ConstCharExpr.java 25-jun-2007.02:28:50 rkomurav Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Class representing a string constant expression 

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    06/25/07 - cleanup
    rkomurav    05/13/07 - add isClassB
    rkomurav    05/28/07 - add .equals
    parujain    10/13/06 - passing returntype to Logical
    anasrini    08/30/06 - set expr name
    najain      08/28/06 - expr is a abstract class
    anasrini    02/26/06 - implement toString 
    anasrini    02/23/06 - add constructor, getter methods 
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
 */

/**
 *  @version $Header: ConstCharExpr.java 25-jun-2007.02:28:50 rkomurav Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import java.util.List;

import oracle.cep.common.Datatype;

/**
 * Class representing a string constant expression
 *
 * @since 1.0
 */

public class ConstCharExpr extends Expr {

  private String value;

  public ExprType getExprType() {
    return ExprType.E_CONST_VAL;
  }

  public Datatype getReturnType() {
    return dt;
  }

  /**
   * Constructor
   * @param value the String constant value
   */
  public ConstCharExpr(String value) {
    this.value = value;
    this.dt = Datatype.CHAR;
    setName(value, false);
  }

  /**
   * Get the String constant value
   * @return the String constant value
   */
  public String getValue() {
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
    
    ConstCharExpr other = (ConstCharExpr)otherObject;
    String oc           = other.getValue();
    
    if(!oc.equals(this.value))
      return false;
    
    return true;
  }
  
  // toString
  public String toString() {
    return "<ConstExpr datatype=\"" + getReturnType() + "\" value=\"" + value + "\" />";
  }

}
