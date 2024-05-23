/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/windows/CurrentHourValueWindow.java /main/3 2011/11/03 10:21:51 sbishnoi Exp $ */

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

import oracle.cep.common.IntervalConverter;

/**
 *  @version $Header: CurrentHourValueWindow.java 26-sep-2011.06:42:29 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class CurrentHourValueWindow extends ValueWindow
{  
  /** Value window size; this will be equal to number of nanoseconds in an hour
    */
  private long winSize;    
  
  /** start value of current hour; if window represents time period 300PM-400PM
   * then we will have nanosecond value of 3PM timestamp */
  private long winBaseValue;
  
  /** size of slide value; Default is 1*/
  private long slideAmount;
  
  /** number of nanoseconds in an hour */
  private long numNanosInAnHour = IntervalConverter.HOUR * 1000000000l;
 
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

  /**
   * Return the time when the given tuple should expire.
   * Inn case of CurrentHour value window, we will expire all the current
   * hour tuples, when the left boundary of next hour starts.
   * e.g.
   * Suppose window is defined as [CurrentHour on c1]
   * and we have a tuple in the window whose column c1's value is 8:45 AM.
   * Assume that last input timestamp was 8:50 AM, so the window is containing
   * all tuples having c1's value between 8:00 AM - 8:59 AM (included)
   * 
   * Now suppose we get an input tuple having timestamp 9:10 AM, which will
   * move the window to next hour i.e. 9:00 AM - 10:00 AM
   * so we will expire all the previous tuples with an expired ts value of 
   * 9:00 AM
   * 
   * For a tuple with currVal = 8:15 AM, expTs = 9:00 AM
   */
  @Override
  public long getExpiredVal(long currVal)
  {
    long numHours  = currVal / numNanosInAnHour;      
    long expTs     = (numHours + 1) * numNanosInAnHour;   
    return expTs;
  }

  /**
   * Check whether the tuple with column value "currVal" is visible
   * Return true if the currVal is greater than/equal to window's left boundary
   *             and less than window's right boundary(left boundary + winSize)
   *             Here winSize = 1 Hour
   *        false otherwise
   * e.g. 
   * if window is defined as [CurrentHour on c1] and last input timestamp is
   * 8:15 AM, then the current hour is 8:00AM - 8:59 AM
   * Only those tuples will be visible whose c1's value is between 8 and 9
   */
  @Override
  public boolean visibleW(long currVal)
  {
    return currVal >= this.winBaseValue && 
           currVal <= this.winBaseValue + this.winSize;    
  }
  
  @Override
  public boolean visibleW(double currVal)
  {
    // column attribute will never be of type double
    assert false;
    return false;
  }

  /**
   * Check if the current tuple with column value "currVal" is expired or not
   * Return TRUE if currVal is less than left boundary of window
   *        FALSE otherwise
   *        
   * e.g. 
   * if window is defined as [CurrentHour on c1] and last input timestamp is
   * 8:15 AM, then the current hour is 8:00AM - 8:59 AM
   * then the tuple with c1's value less than 8:00 AM will be expired
   * 
   * Note: we won't consider those tuple as expired whose "currVal" is higher
   * than window's right boundary. Those tuple will not be visible now but on
   * future.
   */
  @Override
  public boolean expiredW(long currVal)
  {
    if(currVal < winBaseValue)
      return true;
    else
      return false;
  }  

  @Override
  public boolean expiredW(double currVal)
  {
    // column attribute will never be of type double
    assert false;
    return false;
  } 

  @Override
  public void setSlide(long slideSize)
  {
    this.slideAmount = slideSize; 
  }

  @Override
  public void setWindowSize(Object size)
  {
    if(size instanceof Long)
      this.winSize = (Long)size;
    else
      assert false;
  }
  
  @Override
  public void setBaseValue(long val)
  {    
    // winLBaseValue is the highest long multiple of numNanosInAnHour AND
    // less than currentElementLongValue
    if(val > winBaseValue  + winSize)
    {
      long numHours = val / numNanosInAnHour;
      long targetBaseValue = numHours * numNanosInAnHour;          
      this.winBaseValue = targetBaseValue; 
    }
  }
  
  @Override
  public void setBaseValue(double val)
  {
    // column attribute will never be of type double
    assert false;
  }  
}