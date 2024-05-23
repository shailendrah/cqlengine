/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprTimestamp.java /main/5 2011/07/09 08:53:44 udeshmuk Exp $ */

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
    udeshmuk    02/21/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprTimestamp.java /main/3 2009/12/03 21:27:59 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;

/**
 * Timestamp Physical Operator Expression Class Definition
 */
public class ExprTimestamp extends Expr {
  /** timestamp long value */
  long timestampValue;

  public long getTValue() {
    return timestampValue;
  }

  public void setTValue(long value) {
    this.timestampValue = value;
  }
  
  public Long getObject()
  {
    return(new Long(timestampValue));
  }
  
  public ExprTimestamp(long timestampValue)
  {
    super(ExprKind.CONST_VAL);
    setType(Datatype.TIMESTAMP);
    this.timestampValue = timestampValue;
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * @return 
   *      The value of the Timestamp value in String format
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
    
    ExprTimestamp other = (ExprTimestamp) otherObject;
    return (timestampValue == other.getTValue()); 
  }

  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorTimestampExpression>");
    sb.append(super.toString());
    sb.append("<Value value=\"" + timestampValue + "\" />");
    sb.append("</PhysicalOperatorTimestampExpression>");
    return sb.toString();
  }
  
  //Generate and return visualiser compatible XML plan
  public String getXMLPlan2() {
    return String.valueOf(timestampValue);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    return;
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  {
    return  " "+this.getObject().toString()+" ";
  }

}
