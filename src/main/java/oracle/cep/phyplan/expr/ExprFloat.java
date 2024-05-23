/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprFloat.java /main/7 2011/07/09 08:53:44 udeshmuk Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Float Physical Operator Expression Class Definition

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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprFloat.java /main/5 2009/12/03 21:27:59 udeshmuk Exp $
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
 * Float Physical Operator Expression Class Definition
 */
public class ExprFloat extends Expr {
  /** float value */
  float fValue;

  public float getFValue() {
    return fValue;
  }

  public void setFValue(float fValue) {
    this.fValue = fValue;
  }
  
  public Float getObject()
  {
    return(new Float(fValue));
  }
  
  public ExprFloat(float fValue)
  {
    super(ExprKind.CONST_VAL);
    setType(Datatype.FLOAT);
    this.fValue = fValue;
  }

  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorFloatExpression>");
    sb.append(super.toString());
    sb.append("<Value value=\"" + fValue + "\" />");
    sb.append("</PhysicalOperatorFloatExpression>");
    return sb.toString();
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * @return 
   *      The value of the Float in String format
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
    
    ExprFloat other = (ExprFloat) otherObject;
    return (fValue == other.getFValue());
  }
  
  //Generate and return visualiser compatible XML plan
  public String getXMLPlan2() {
    return String.valueOf(fValue);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    return;
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  {
    if(fValue < 0)
      return " ("+this.getObject().toString()+") ";
    else
      return " "+this.getObject().toString()+" ";
  }

}
