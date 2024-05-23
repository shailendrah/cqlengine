/* $Header: pcbpel/cep/src/oracle/cep/extensibility/windows/builtin/RangeSlide.java /main/2 2008/08/18 21:52:48 sbishnoi Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    07/31/08 - support for nanosecond timestamp
    parujain    03/13/07 - Range Slide window
    parujain    03/13/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/extensibility/windows/builtin/RangeSlide.java /main/2 2008/08/18 21:52:48 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.extensibility.windows.builtin;

import java.io.IOException;

import oracle.cep.extensibility.windows.GenericTimeWindow;
import oracle.cep.common.EventTimestamp;

public class RangeSlide implements GenericTimeWindow {
  // Range value in nanosecond unit of time
  private long range;
  // Slide value in nanosecond unit of time
  private long slide;
  
  public void setInputParams(Object[] obj) throws IOException{
    if(obj.length != 2)
      throw new IOException("inappropriate number of arguments");
    // Fact: System's granularity is nanosecond unit of time for range and
    // slide values;
    // Assumption: User can assume default range and slide unit at any scale
    // User should do is to convert input range/slide values to conversion to appropriate value as below:
    // Here User assumed that range and slide are mentioned in time unit second
    // Thus converted input value to nanosecond by multiplying to 10^9    
    range = (((Integer)obj[0]).longValue())*1000000000;
    slide = (((Integer)obj[1]).longValue())*1000000000;    
  }
  
  public boolean visibleW(EventTimestamp t, EventTimestamp visTs) {
    long actual = t.getTime();    
    long visibleTs = getVisibleTs(actual);    
    if(visibleTs < actual)
      return false;
    visTs.setTime(visibleTs);
    return true;
  }
  
  private long getVisibleTs(long time) {
    if(slide > 1)
    {
      long t = time / slide;
      if((time % slide) == 0)
        return(t*slide);
      else
        return((t+1)*slide);
    }
    else
      return time;
  }

  public boolean canOutputTsGTInputTs() {
    if(slide > 1)
      return true;
    
    return false;
  }

  public boolean expiredW(EventTimestamp ts, EventTimestamp expTs) {
    long actual = ts.getTime();    
    long visibleTs = getVisibleTs(actual);
    long expiredTs = visibleTs + range;    
    expTs.setTime(expiredTs);
    // This is the border line case, when range > slide and visibleTs < range
    if(visibleTs < range)
      return false;    
    return true;
  }
  
}

