/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprBoolean.java /main/5 2011/07/09 08:53:44 udeshmuk Exp $ */

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
    udeshmuk    06/20/11 - support getSQLEquivalent
    udeshmuk    11/08/09 - API to get all referenced attrs
    sborah      04/20/09 - define getSignature
    mthatte     01/14/08 - 
    najain      01/02/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprBoolean.java /main/3 2009/12/03 21:27:59 udeshmuk Exp $
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
 * Boolean Physical Operator Expression Class Definition
 */
public class ExprBoolean extends Expr {
  /** boolean value */
  boolean bvalue;

  public boolean getBValue() {
    return bvalue;
  }

  public void setBValue(boolean bvalue) {
    this.bvalue = bvalue;
  }

  public Boolean getObject()
  {
    return(new Boolean(bvalue));
  }
  
  public ExprBoolean(boolean bvalue)
  {
    super(ExprKind.CONST_VAL);
    setType(Datatype.BOOLEAN);
    this.bvalue = bvalue;
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * @return 
   *      The value of the Boolean Expr in String format
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
    
    ExprBoolean other = (ExprBoolean) otherObject;
    return (bvalue == other.getBValue());
  }
  
  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorBooleanExpression>");
    sb.append(super.toString());
    sb.append("<Value value=\"" + bvalue + "\" />");
    sb.append("</PhysicalOperatorBooleanExpression>");
    return sb.toString();
  }
  
  //Generate and return visualiser compatible XMLPlan
  public String getXMLPlan2() {
    return String.valueOf(bvalue);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    return;
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  {
    //no sql equivalent of boolean. SQL does not hv true and false as literals
    return null;
  }

}
