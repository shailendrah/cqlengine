/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprInterval.java /main/12 2013/08/21 05:52:13 sbishnoi Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    08/20/13 - bug 17084223
    sbishnoi    06/03/12 - bug 13948958
    sbishnoi    08/27/11 - adding support for interval year to month
    udeshmuk    06/20/11 - support getSQLEquivalent
    udeshmuk    11/08/09 - API to get all referenced attrs
    sborah      04/20/09 - define getSignature
    udeshmuk    02/21/08 - 
    rkomurav    06/18/07 - cleanup
    parujain    03/08/07 - get Object
    rkomurav    11/28/06 - add equals method
    parujain    10/09/06 - Interval datatype
    parujain    10/09/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprInterval.java /main/12 2013/08/21 05:52:13 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalConverter;
import oracle.cep.common.IntervalFormat;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;

/**
 * Interval Physical Operator Expression Class Definition
 */
public class ExprInterval extends Expr {
  /** interval long value */
  long intervalValue;
  
  /** flag to check if the intervalValue is YEAR TO MONTH or DAY TO SECOND*/
  boolean isYearToMonth;
  
  /** format of interval value*/
  IntervalFormat format;

  /**
   * @return the format
   */
  public IntervalFormat getFormat()
  {
    return format;
  }

  public long getVValue() {
    return intervalValue;
  }

  public void setVValue(long value) {
    this.intervalValue = value;
  }
  
  public Long getObject()
  {
    return(new Long(intervalValue));
  }
  

  private boolean getIsYearToMonth()
  {
    return isYearToMonth;
  }
  
  public ExprInterval(long intervalValue, 
                      boolean isYearToMonth,
                      IntervalFormat format) 
  {
    super(ExprKind.CONST_VAL);
    if(isYearToMonth)
      setType(Datatype.INTERVALYM);
    else
      setType(Datatype.INTERVAL);
    this.intervalValue = intervalValue;
    this.isYearToMonth = isYearToMonth;
    this.format = format;
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * @return 
   *      The value of the Interval in String format
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
    
    ExprInterval other = (ExprInterval) otherObject;
    
    if(isYearToMonth != other.getIsYearToMonth())
      return false;
    
    if(!format.equals(other.getFormat()))
      return false;
    
    return intervalValue == other.getVValue(); 
  }


  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorIntervalExpression>");
    sb.append(super.toString());
    sb.append("<Value value=\"" + intervalValue + "\" />");
    sb.append("</PhysicalOperatorIntervalExpression>");
    return sb.toString();
  }
  
  //Generate and return visualiser compatible XML plan
  public String getXMLPlan2() {
    return String.valueOf(intervalValue);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    return;
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  {    
    if(this.getIsYearToMonth())
    {
      return " " +
        "INTERVAL \'" +
        IntervalConverter.getYMInterval(intervalValue, format)+ 
        "\' YEAR TO MONTH "; 
    }
    else
    {
      return " " + 
        "INTERVAL \'" +
        IntervalConverter.getDSInterval(intervalValue, format) +
        "\' DAY TO SECOND ";
    }
  }

}
