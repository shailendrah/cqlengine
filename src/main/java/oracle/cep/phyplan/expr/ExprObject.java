/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprObject.java /main/5 2011/07/09 08:53:44 udeshmuk Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
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
    mthatte     12/26/07 - 
    najain      11/28/07 - 
    anasrini    11/28/07 - 
    najain      11/02/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprObject.java /main/3 2009/12/03 21:27:59 udeshmuk Exp $
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
 * Byte Physical Operator Expression Class Definition
 */
public class ExprObject extends Expr 
{
  /** byte value */
  Object oValue;

  public Object getOValue() 
  {
    return oValue;
  }

  public void setOValue(Object oValue) 
  {
    this.oValue = oValue;
  }
 
  public ExprObject(Object oValue)
  {
    super(ExprKind.CONST_VAL);
    setType(Datatype.OBJECT);
    this.oValue = oValue;
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * @return 
   *      The value of the Object in String format
   */
  public String getSignature()
  {
    if(this.getOValue() != null)
      return this.getOValue().toString();
    else 
      return "null";
  }

  public boolean equals(Object otherObject) {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    ExprObject other = (ExprObject) otherObject;
    Object ob      = other.getOValue();
    if (!ob.equals(oValue))
      return false;

    return true;
  }

  // toString method override
  public String toString() 
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorObjectExpression>");
    sb.append(super.toString());
    sb.append("<Value value=\"" + oValue.toString() + "\" />");
    sb.append("</PhysicalOperatorObjectExpression>");
    return sb.toString();
  }
  
  //Generate and return visualiser compatible XML plan
  public String getXMLPlan2() 
  {
    return new String(oValue.toString());
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    return;
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  { 
    //FIXME: For now, return null as not sure what is SQLEquivalent for the 
    //       object value that this expr represents
    return null;
  }

}
