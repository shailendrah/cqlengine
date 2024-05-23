/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkRangeSlide.java /main/4 2011/10/03 01:51:59 sbishnoi Exp $ */

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
    sbishnoi    10/01/11 - XbranchMerge sbishnoi_bug-12720971_ps5 from
                           st_pcbpel_11.1.1.4.0
    sbishnoi    09/26/11 - changed interface of GenericTimeWindow
    sbishnoi    09/15/11 - implementing new interface methods
    skmishra    08/27/08 - 
    sbishnoi    07/30/08 - support for nanosecond; changing interface
                           implementation
    hopark      05/13/08 - 
    mthatte     10/16/07 - 
    parujain    03/12/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/userfunctions/TkRangeSlide.java /main/3 2008/09/10 14:06:33 skmishra Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.userfunctions;

import java.io.IOException;

import oracle.cep.extensibility.windows.GenericTimeWindow;
import oracle.cep.common.EventTimestamp;

public class TkRangeSlide implements GenericTimeWindow {
  // range value in nanosecond  
  private long range;
  // slide value in nanosecond
  private long slide;
  
  public void setInputParams(Object[] obj) throws IOException{
   // assert obj.length == 2;
    if(obj.length != 2)
      throw new IOException("inappropriate number of arguments");
    // Fact: System's granularity is nanosecond unit of time for range and
    // slide values;
    // Assumption: User can assume default range and slide unit at any scale
    // User should do is to convert input range/slide values to conversion to appropriate value as below:
    // Here User assumed that range and slide are mentioned in time unit second
    // Thus converted input value to nanosecond by multiplying to 10^9
    range = (((Integer)obj[0]).longValue())*1000*1000*1000;
    slide = (((Integer)obj[1]).longValue())*1000*1000*1000;
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
