/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprBigDecimal.java /main/4 2011/07/09 08:53:44 udeshmuk Exp $ */

/* Copyright (c) 2009, 2011, Oracle and/or its affiliates. 
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
    udeshmuk    11/18/09 - add getAllReferencedAttrs
    sborah      06/23/09 - support for bigdecimal
    sborah      06/23/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprBigDecimal.java /main/2 2009/12/03 21:28:00 udeshmuk Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.expr;

import java.math.BigDecimal;
import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;

/**
 * BigDecimal Physical Operator Expression Class Definition
 */
public class ExprBigDecimal extends Expr 
{
  /** BigDecimal value */
  BigDecimal nValue;

  public BigDecimal getNValue() 
  {
    return nValue;
  }

  public void setNValue(BigDecimal nValue) 
  {
    this.nValue = nValue;
  }
  
  public BigDecimal getObject()
  {
    return(nValue);
  }
  
  public ExprBigDecimal(BigDecimal nValue)
  {
    super(ExprKind.CONST_VAL);
    setType(Datatype.BIGDECIMAL);
    this.nValue = nValue;
  }

  // toString method override
  public String toString() 
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorBigDecimalExpression>");
    sb.append(super.toString());
    sb.append("<Value value=\"" + nValue + "\" />");
    sb.append("</PhysicalOperatorBigDecimalExpression>");
    return sb.toString();
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * @return 
   *      The value of the BigDecimal in String format
   */
  public String getSignature()
  {
    return this.getObject().toString();
  }
  
  public boolean equals(Object otherObject) 
  {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    ExprBigDecimal other = (ExprBigDecimal) otherObject;
    return (nValue.compareTo(other.getNValue()) == 0);
  }
  
  //Generate and return visualiser compatible XML plan
  public String getXMLPlan2() 
  {
    return nValue.toString();
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    return;
  }
  
  @Override
  public String getSQLEquivalent(ExecContext ec)
  {
     return " ("+this.getObject().toString()+") ";  
  }
}
