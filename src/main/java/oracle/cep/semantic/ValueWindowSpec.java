/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/ValueWindowSpec.java /main/2 2011/10/01 09:28:39 sbishnoi Exp $ */

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
    sbishnoi    09/06/11 - support for currenthour and current period based
                           value windows
    parujain    07/01/08 - value based windows
    parujain    06/25/08 - value based windows spec
    parujain    06/25/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/semantic/ValueWindowSpec.java /main/1 2008/07/14 22:57:01 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import oracle.cep.common.TimeUnit;
import oracle.cep.common.ValueWindowType;
import oracle.cep.common.WindowType;


public class ValueWindowSpec implements WindowSpec 
{
  /** Constant Value Expression which will specify the window size when the
   * window type is ValueWindowType.GENERIC 
   */
  Expr  constVal;
  
  /** Flag to check if query has specified value window on element time. Please note that 
   *  this flag will be set only if window is applied on relation because relation doesn't
   *  contain pseudo column so class variable "column" will be null in this case.*/
  boolean isWindowOnElementTime;
  
  /**
   * Attribute whose value will decide whether the current set of window tuples
   */
  AttrExpr column;
  
  /**
   * Type of Value Window [GENERIC, CURRENT_HOUR OR CURRENT_PERIOD]
   */
  ValueWindowType type;
  
  /**
   * CurrentPeriod value window specifies startTime, endTime
   * Here currentPeriodStartTime is startTime in nanos and winSize will
   * be computed with the difference between startTime and endTime
   */
  Long currentPeriodStartTime;

  /**
   * Size of value window
   */
  long winSize;
  
  /**
   * Size of slide value window
   */
  long slideAmount;
  
  /**
   * Unit of slide value 
   * (unit will be TimeUnit.NOTIMEUNIT if the slide is not a time value)
   */
  TimeUnit slideUnit;
  
  /**
   * Constructor to create a GENERIC type value window
   * @param val
   * @param expr
   * @param isWindowOnElementTime
   */
  public ValueWindowSpec(Expr val, AttrExpr expr, boolean isWindowOnElementTime)
  {
    this.type = ValueWindowType.GENERIC;
    this.constVal = val;
    this.column = expr;
    this.isWindowOnElementTime = isWindowOnElementTime;
  }
  
  /**
   * Constructor to create a GENERIC type value window
   * @param val
   * @param expr
   */
  public ValueWindowSpec(Expr val, AttrExpr expr)
  {
    this.type = ValueWindowType.GENERIC;
    this.constVal = val;
    this.column = expr;
  }
  
  /**
   * Constructor to create a non-GENERIC type value window
   * @param type
   * @param expr
   * @param currentPeriodStartTime
   * @param winSize
   * @param slideSize
   */
  public ValueWindowSpec(ValueWindowType type, 
                         AttrExpr expr, 
                         Long currentPeriodStartTime, 
                         long winSize)
  {
    this.type = type;
    this.column = expr;
    this.currentPeriodStartTime = currentPeriodStartTime;
    this.winSize = winSize;
  }

  /**
   * Constructor to create a non-GENERIC type value window
   * @param type
   * @param expr
   * @param currentPeriodStartTime
   * @param winSize
   * @param isWindowOnElementTime
   */
  public ValueWindowSpec(ValueWindowType type, 
                         AttrExpr expr, 
                         Long currentPeriodStartTime, 
                         long winSize,
                         boolean isWindowOnElementTime)
  {
    this.type = type;
    this.column = expr;
    this.currentPeriodStartTime = currentPeriodStartTime;
    this.winSize = winSize;
    this.isWindowOnElementTime = isWindowOnElementTime;
  }
  
  public WindowType getWindowType() {
    return WindowType.VALUE;
  }
  
  public Expr getConstVal()
  {
    return this.constVal;
  }
  
  public AttrExpr getColumn()
  {
    return this.column;
  }
  
  public boolean isWindowOnElementTime()
  {
    return isWindowOnElementTime;
  }

  /**
   * @return the type
   */
  public ValueWindowType getType()
  {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(ValueWindowType type)
  {
    this.type = type;
  }
  
  /**
   * @return the currentPeriodStartTime
   */
  public long getCurrentPeriodStartTime()
  {
    if(currentPeriodStartTime != null)
      return currentPeriodStartTime;
    else
      return Long.MIN_VALUE;
  }

  /**
   * @return the winSize
   */
  public long getWinSize()
  {
    return winSize;
  }

  /**
   * @param slideAmount the slideAmount to set
   */
  public void setSlideAmount(long slideAmount)
  {
    this.slideAmount = slideAmount;
  }
  
  /**
   * @return the slideSize
   */
  public long getSlideAmount()
  {
    return slideAmount;
  }

  public String toString()
  {
    if(constVal != null)
      return "<ValueWindowSpec column=\"" + (isWindowOnElementTime ? "ELEMENT_TIME" : column.toString()) + "\"" + 
             " value=\""  + constVal.toString() + "\" />";
    else
      return "<ValueWindowSpec column=\"" + column.toString() + "\"" +
             " value=\"" +  type + "\" />";
  }

}