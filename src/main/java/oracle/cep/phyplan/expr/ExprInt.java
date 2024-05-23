/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprInt.java /main/7 2011/07/09 08:53:44 udeshmuk Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Integer Physical Operator Expression Class Definition

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk    06/20/11 - support getSQLEquivalent
 udeshmuk    11/08/09 - API to get all referenced attrs
 sborah      04/20/09 - define getSignature
 rkomurav    06/18/07 - cleanup
 parujain    03/08/07 - get Object
 rkomurav    10/10/06 - add equals method
 rkomurav    09/11/06 - cleanup for xmldump
 rkomurav    08/22/06 - add getXMLPlan2
 najain      02/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprInt.java /main/5 2009/12/03 21:27:59 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;

/**
 * Integer Physical Operator Expression Class Definition
 */
public class ExprInt extends Expr {
  /** integer value */
  int iValue;

  public int getIValue() {
    return iValue;
  }

  public void setIValue(int iValue) {
    this.iValue = iValue;
  }

  public Integer getObject()
  {
    return(new Integer(iValue));
  }
  
  public ExprInt(int iValue)
  {
    super(ExprKind.CONST_VAL);
    setType(Datatype.INT);
    this.iValue = iValue;
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * @return 
   *      The value of the Integer in String format
   */
  public String getSignature()
  {
    return this.getObject().toString();
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

    sb.append("<PhysicalOperatorIntExpression>");
    sb.append(super.toString());
    sb.append("<Value value=\"" + iValue + "\" />");
    sb.append("</PhysicalOperatorIntExpression>");
    return sb.toString();
  }
  
  //Generate and return visualiser compatible XMLPlan
  public String getXMLPlan2() {
    return String.valueOf(iValue);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    return;
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  {
    if(iValue < 0)
      return " ("+this.getObject().toString()+") ";
    else
      return " "+this.getObject().toString()+" ";
  }
}
