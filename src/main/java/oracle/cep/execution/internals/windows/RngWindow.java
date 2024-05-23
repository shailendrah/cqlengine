/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/windows/RngWindow.java /main/7 2011/12/15 01:06:31 sbishnoi Exp $ */

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
    sbishnoi    12/01/11 - support for variable duration windows
    sbishnoi    10/01/11 - XbranchMerge sbishnoi_bug-12720971_ps5 from
                           st_pcbpel_11.1.1.4.0
    sbishnoi    09/13/11 - cleanup
    sbishnoi    05/12/11 - XbranchMerge sbishnoi_bug-12359181_ps5 from
                           st_pcbpel_11.1.1.4.0
    sbishnoi    05/10/11 - fix 12359181
    sbishnoi    05/04/11 - fix 12359181
    sbishnoi    07/31/08 - support for nanosecond
    parujain    03/23/07 - cleanup
    parujain    03/08/07 - Range Window Specification
    parujain    03/08/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/execution/internals/windows/RngWindow.java /main/3 2008/08/18 21:52:48 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.internals.windows;

import oracle.cep.common.EventTimestamp;
import oracle.cep.execution.internals.TimeDuration;
import oracle.cep.phyplan.window.PhyRngWinSpec;
import oracle.cep.phyplan.window.PhyWinSpec;

public class RngWindow extends Window
{
  /** windowSize */
  private TimeDuration   windowSize;

  /** slideSize */
  private TimeDuration   slideSize;
  
  /** flag to check if the windows is variable duration */
  private boolean        isVariableDurationWindow;
  
  public RngWindow(PhyWinSpec spec)
  {
    assert spec instanceof PhyRngWinSpec;
    PhyRngWinSpec rng = (PhyRngWinSpec)spec;
    
    //  Set the window size
    windowSize = new TimeDuration(rng.getRangeUnits());
    // Set the slide size
    slideSize = new TimeDuration(rng.getSlideUnits());
    
    /** set the flag to check for variable duration*/
    if(spec instanceof PhyRngWinSpec)
    {
      this.isVariableDurationWindow 
        = ((PhyRngWinSpec)spec).isVariableDurationWindow();
    }
    else
      this.isVariableDurationWindow = false;
  }

  /**
   * Getter for windowSize in RangeWindow
   * 
   * @return Returns the windowSize
   */
  public TimeDuration getWindowSize()
  {
    return windowSize;
  }

  /**
   * Getter for slideSize in RangeWindow
   * 
   * @return Returns the slideSize
   */
  public TimeDuration getSlideSize()
  {
    return slideSize;
  }
  
  /**
   * Setter for windowSize in RangeWindow
   * 
   * @param windowSize
   *          The windowSize to set.
   */
  public void setWindowSize(TimeDuration windowSize)
  {
    this.windowSize = windowSize;
  }
  
  /**
   * Setter for slideSize in RangeWindow
   * 
   * @param slideSize
   *          The slideSize to set.
   */
  public void setSlideSize(TimeDuration slideSize)
  {
    this.slideSize = slideSize;
  }
  
  // False is returned when ts will never be visible
  public boolean visibleW(EventTimestamp ts, EventTimestamp visTs)
  {
    long actual = ts.getTime();
    long visibleTs = getVisibleTs(actual);
    if(visibleTs < actual)
      return false;
    visTs.setTime(visibleTs);
    return true;
  }
  
  private long getVisibleTs(long time)
  {
    long slideValue = slideSize.getValue();
    if (slideValue > 1)
    {
      long t = time / slideValue;
      if((time % slideValue) == 0)
        return(t*slideValue);
      else
        return((t+1)*slideValue);
    }
    else
      return time;
  }
  
  public boolean expiredW(EventTimestamp ts, EventTimestamp expTs)
  {
    long actual = ts.getTime();
    long visibleTs = getVisibleTs(actual);
    
    // First timestamp which will get expired
    long expiredTs = actual + windowSize.getValue();
    long expiredTupleVisTs = getVisibleTs(expiredTs);
    expTs.setTime(expiredTupleVisTs);    

    //If Visible Ts is less than the window size than nothing will get expired
    if(visibleTs < windowSize.getValue())
      return false;
    else
      return true;
    
  }

  /**
   * @return the isVariableDurationWindow
   */
  public boolean isVariableDurationWindow()
  {
    return isVariableDurationWindow;
  }
}
