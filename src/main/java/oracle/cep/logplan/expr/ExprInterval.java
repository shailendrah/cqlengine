/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprInterval.java /main/5 2011/09/05 22:47:27 sbishnoi Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    08/27/11 - adding support for interval year to month
    sborah      04/11/11 - override getAllReferencedAttrs()
    udeshmuk    02/21/08 - 
    rkomurav    11/30/06 - add equals method
    parujain    10/13/06 - getting returntype from Semantic
    parujain    10/09/06 - Interval datatype
    parujain    10/09/06 - Creation
 */

/**
 *  @version $Header: ExprInterval.java 21-feb-2008.05:08:07 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalFormat;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrUnNamed;

/**
 * Interval Logical Operator Expression Class Definition
 * @author parujain
 *
 */
public class ExprInterval extends Expr implements Cloneable
{
  /** interval value*/
  long intervalVal;

  /** flag to check if the intervalValue is YEAR TO MONTH or DAY TO SECOND */
  boolean isYearToMonth;
  
  /** format of interval value */
  IntervalFormat format;

  public ExprInterval(long value, Datatype dt, IntervalFormat format)
  {
    assert dt == Datatype.INTERVAL || dt == Datatype.INTERVALYM;
    setType(dt);
    if(dt == Datatype.INTERVALYM)
      isYearToMonth = true;
    else
      isYearToMonth = false;
    this.intervalVal = value;
    this.format = format;
  }

  /**
   * @return the isYearToMonth
   */
  public boolean isYearToMonth()
  {
    return isYearToMonth;
  }

  public long getVValue() {
    return intervalVal;
  }

  public void setVValue(long value) {
    this.intervalVal = value;
  }

  public Attr getAttr() {
    AttrUnNamed attr = new AttrUnNamed(getType());
    return (Attr) attr;
  }

  /**
   * @return the format
   */
  public IntervalFormat getFormat()
  {
    return format;
  }

  public boolean check_reference(LogOpt op) {
    return true;
  }
  
  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    return;
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, boolean includeAggrParams)
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
    
    ExprInterval other = (ExprInterval) otherObject;
    long l      = other.getVValue();
    
    return (this.isYearToMonth == other.isYearToMonth() &&
            l == this.intervalVal);
  }

  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<LogicalIntervalExpression>");
    sb.append(super.toString());
    sb.append("<Value vValue=\"" + intervalVal + "\" />");

    sb.append("</LogicalIntervalExpression>");
    return sb.toString();
  }
}
