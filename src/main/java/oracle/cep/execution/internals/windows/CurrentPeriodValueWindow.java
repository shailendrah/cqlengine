/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/windows/CurrentPeriodValueWindow.java /main/3 2011/11/03 10:21:51 sbishnoi Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    10/08/11 - truncate millis part
    sbishnoi    09/26/11 - Creation
 */
package oracle.cep.execution.internals.windows;

import java.util.Calendar;

import oracle.cep.common.IntervalConverter;

/**
 *  @version $Header: CurrentPeriodValueWindow.java 26-sep-2011.06:42:10 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class CurrentPeriodValueWindow extends ValueWindow
{

  /** Value window size specified the size of the window */
  long            winSize; 
  
  /** start value of windows when value is of type long */
  long            winBaseValue;
  
  /** In case of CURRENT_PERIOD window, currentPeriodStartTime represents
   * start offset to winStartValue */
  long            currentPeriodStartTime;
  
  /** calendar instance */
  Calendar        cal;
  
  /** size of slide value; Default is 1*/
  long           slideAmount;
  
  /** start value of current day */
  long           dayStartValue;
  
  public CurrentPeriodValueWindow()
  {
    winSize      = 0;
    winBaseValue = Long.MIN_VALUE;    
    slideAmount  = 1;
    cal          = Calendar.getInstance();
    currentPeriodStartTime = Long.MIN_VALUE;
    dayStartValue = Long.MIN_VALUE;
  }
  
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
    
    // If a tuple is inside window but its timestamp is less than windows's
    // left boundary,then we will set the visible value of tuple to window's 
    // left boundary
    // e.g.
    // Suppose window is defined as [CurrentPeriod("0800", "1700") on C1]
    // Now if a tuple comes at 7:00 AM with column C1's value 8:15AM
    // This tuple satisfies the window criteria as the time is between 8:00AM &
    // 5:00 PM..
    // Now the tuple came at 7 AM which is earlier than window's left boundary,
    // so we will send this tuple at the timestamp when window's left boundary
    // will start
    if(paramVal < this.winBaseValue && paramVal >= dayStartValue)
    {
      paramVal = this.winBaseValue;
    }
    
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
   * Calculate the expired time stamp for a tuple where "currVal" is its 
   * window column's value
   * For a current period value window, the expired timestamp will be equal to
   * left boundary + winSize where window size is the width of specified period
   * e.g.
   * Suppose window is defined as [CurrentPeriod("0800", "1700") on c1]
   * and we are calculating expired value of a tuple whose c1's value is 9:00AM
   * So it will be equal to 0800 + 9 HRS i.e. 5:00 PM
   */
  @Override
  public long getExpiredVal(long currVal)
  {
    currVal = (currVal / 1000000000l ) * 1000000000l;
    cal.setTimeInMillis(currVal / 1000000l);
    int numHour = cal.get(Calendar.HOUR_OF_DAY);   
    int numMins = cal.get(Calendar.MINUTE);
    int numSecs = cal.get(Calendar.SECOND);
    // Set the time to DD/MM/YY 00:00:00AM
    long dayBaseValue 
      = currVal - numHour * IntervalConverter.HOUR * 1000000000l
                - numMins * IntervalConverter.MINUTE * 1000000000l
                - numSecs * IntervalConverter.SECOND * 1000000000l;
    
    long periodBaseValue = dayBaseValue + this.currentPeriodStartTime;
    long expTs = periodBaseValue + this.winSize;
    return expTs;
  }

  /**
   * Check whether the tuple with column value "currVal" is visible
   * Return true if the currVal is greater than/equal to window's left boundary
   *             and less than window's right boundary(left boundary + winSize)
   *             Here winSize = 1 Hour
   *        false otherwise
   * e.g. 
   * if window is defined as [CurrentPeriod("0800","1700") on c1] and the last
   * input timestamp is 8:15 AM, then the current period is 8:00AM - 5:00 PM
   * Only those tuples will be visible whose c1's value is between 8AM and 5PM
   */
  @Override
  public boolean visibleW(long currVal)
  {    
    return currVal >= this.winBaseValue && 
           currVal <= this.winBaseValue + winSize;
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
   * if window is defined as [CurrentPeriod("0800", "1700") on c1] and 
   * the last input timestamp is 8:15 AM, 
   * then the current period is 8:00 AM - 5:00 PM
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
    if(val < winBaseValue + winSize)
      return;
    
    cal.setTimeInMillis(val / 1000000l);
    int numHour = cal.get(Calendar.HOUR_OF_DAY);   
    int numMins = cal.get(Calendar.MINUTE);
    int numSecs = cal.get(Calendar.SECOND);
    // Set the time to DD/MM/YY 00:00:00AM
    this.dayStartValue 
      = val - numHour * IntervalConverter.HOUR * 1000000000l
                                - numMins * IntervalConverter.MINUTE * 1000000000l
                                - numSecs * IntervalConverter.SECOND * 1000000000l;
    this.winBaseValue = dayStartValue + this.currentPeriodStartTime;
    if(val > winBaseValue + winSize)
    {
      // If the column value is greater than today's current period, set the
      // base value to next day's base value.
      this.winBaseValue = winBaseValue + IntervalConverter.DAYS * 1000000000l;
      this.dayStartValue = dayStartValue + IntervalConverter.DAYS * 1000000000l;
    }
  }
  
  @Override
  public void setBaseValue(double val)
  {
    // column attribute will never be of type double
    assert false;
  }
  
  public void setCurrentPeriodStartTime(long currentPeriodStartTime)
  {
    this.currentPeriodStartTime = currentPeriodStartTime;
  }
}