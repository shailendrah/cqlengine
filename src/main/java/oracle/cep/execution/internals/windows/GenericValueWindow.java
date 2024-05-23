/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/windows/GenericValueWindow.java /main/2 2011/11/03 10:21:51 sbishnoi Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    09/26/11 - Creation
 */
package oracle.cep.execution.internals.windows;

/**
 *  @version $Header: GenericValueWindow.java 26-sep-2011.06:41:50 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class GenericValueWindow extends ValueWindow
{
  /** Value window size and the constant value is double*/
  long            winLSize; 
  
  /** Value window size when windows is of type GENERIC and
  *  the constant value is double*/
  double          winDSize;
    
  /** start value of windows when value is of type long */
  long            winLBaseValue;
  
  /** start value of windows when value is of type double */
  double          winDBaseValue;
  
  /** size of slide value; Default is 1*/
  long           slideAmount;
 
 
  /**
   * Get the visible time value for the tuple whose column's value is paramVal
   * Returns paramVal as visible time value if we are using default slide
   * Else calculate the visible time value as next highest multiple of
   * slideAmount
   */
  @Override
  public long getVisibleVal(long paramVal)
  {
    long visValue;
    
    if(this.slideAmount == 1)
      visValue = paramVal;
    else
    {
      long multiplier = paramVal/slideAmount;
      if(paramVal % slideAmount == 0)
        visValue = multiplier * slideAmount;
      else
        visValue = (multiplier + 1) * slideAmount;
    }
    return visValue;
  }

  @Override
  public long getExpiredVal(long currVal)
  {
    return currVal + this.winLSize;
  }

  /**
   * Check whether the tuple with column value "currVal" is visible
   * Return true if the currVal is greater than/equal to window's left boundary
   *             and less than window's right boundary(left boundary + winSize)
   *             Here winSize is specified in window's definition
   *        false otherwise
   */
  @Override
  public boolean visibleW(long currVal)
  {    
    return currVal <= this.winLBaseValue && 
           currVal >= this.winLBaseValue - this.winLSize;
  }
  
  @Override
  public boolean visibleW(double currVal)
  {
    // Tuple's visible timetamp depends on slide value;
    // As here the comparing column attribute is of type doublem we don't
    /// support slide with double; so slide value will be default value.
    // hence tuple will be visible at its input timestamp till it doesn't expire
    return true;
  }

  /**
   * Check if the current tuple with column value "currVal" is expired or not
   * Return TRUE if currVal is less than left boundary of window
   *        FALSE otherwise
   *  
   */
  @Override
  public boolean expiredW(long currVal)
  {
    if(currVal <= this.winLBaseValue - this.winLSize)
      return true;    
    else
      return false;
  }  

  /**
   * Check if the current tuple with column value "currVal" is expired or not
   * Return TRUE if currVal is less than left boundary of window
   *        FALSE otherwise
   */
  @Override
  public boolean expiredW(double currVal)
  {
    if(currVal <= this.winDBaseValue - this.winDSize)
      return true;
    else
      return false;
  } 

  @Override
  public void setBaseValue(long val)
  {
    winLBaseValue = val;
  }

  @Override
  public void setBaseValue(double val)
  {
    winDBaseValue = val;
  }
  
  
  @Override
  public void setSlide(long slideSize)
  {
    slideAmount = slideSize;
  }

  @Override
  public void setWindowSize(Object size)
  {
    if(size instanceof Long)
      winLSize = (Long)size;
    else if(size instanceof Double)
      winDSize = (Double)size;    
  }
}
