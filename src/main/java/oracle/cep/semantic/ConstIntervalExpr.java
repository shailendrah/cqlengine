/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/ConstIntervalExpr.java /main/5 2011/09/05 22:47:27 sbishnoi Exp $ */

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
    sbishnoi    08/22/11 - support for standards based interval datatype
    rkomurav    06/25/07 - cleanup
    rkomurav    05/13/07 - add isClassB
    rkomurav    05/28/07 - add .equals
    parujain    10/13/06 - passing returntype to Logical
    parujain    10/09/06 - Interval datatype support
    parujain    10/09/06 - Creation
 */

/**
 *  @version $Header: ConstIntervalExpr.java 25-jun-2007.02:28:58 rkomurav Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalFormat;

/**
 * Expression for constant interval datatypes
 * 
 * @author parujain
 *
 */
public class ConstIntervalExpr extends Expr
{
  /** interval value will be either 1) number of months OR
   *                                2) number of nanoseconds */
  private long value;
  
  /** flag which will tell whether the value is number of months OR
   *                                            number of nanoseconds*/
  private boolean isYearToMonthInterval;
  
  /** the complete format of the interval value given by user; this will be
   *  useful while converting the above value back into interval format */ 
  private IntervalFormat format;
  
  /**
   * Constructor to create a constant interval value object at semantic layer
   * @param val
   */
  public ConstIntervalExpr(long val, boolean isYearToMonth)
  {
    this.value = val;
    if(isYearToMonth)
      this.dt = Datatype.INTERVALYM;
    else
      this.dt = Datatype.INTERVAL;
    setName(String.valueOf(value), false);
  }

  public ExprType getExprType()
  {
    return ExprType.E_CONST_VAL;
  }
    
  public Datatype getReturnType()
  {
    return dt;
  }
  
  public long getValue()
  {
    return value;
  }
  
  public void setFormat(IntervalFormat format)
  {
    this.format = format;
  }
  
  
  public void setIsYearToMonthInterval(boolean isYearToMonthInterval)
  {
    this.isYearToMonthInterval = isYearToMonthInterval;
  }
 
  public void getAllReferencedAggrs(List<AggrExpr> aggrs)
  {
    return;
  }
  
  /**
   * @return the isYearToMonthInterval
   */
  public boolean isYearToMonthInterval()
  {
    return isYearToMonthInterval;
  }

  /**
   * @return the format
   */
  public IntervalFormat getFormat()
  {
    return format;
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
    
    ConstIntervalExpr other = (ConstIntervalExpr) otherObject;
    long l      = other.getValue();
    IntervalFormat format = other.getFormat();
    
    
    return (l == this.value && format.equals(this.format));
  }
  
  //toString
  public String toString() {
    return "<ConstExpr datatype=\"" + getReturnType() + "\" value=\"" + value + "\" />";
  }
}
