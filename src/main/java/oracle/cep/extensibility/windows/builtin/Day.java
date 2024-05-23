/* $Header: pcbpel/cep/server/src/oracle/cep/extensibility/windows/builtin/Day.java /main/3 2008/12/01 16:48:52 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      11/29/08 - use timezone
    sbishnoi    07/31/08 - support for nanosecond timestamp
    parujain    03/23/07 - day extensible window
    parujain    03/23/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/extensibility/windows/builtin/Day.java /main/3 2008/12/01 16:48:52 hopark Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.extensibility.windows.builtin;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import oracle.cep.extensibility.windows.GenericTimeWindow;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.service.CEPManager;
import oracle.cep.common.EventTimestamp;

public class Day implements GenericTimeWindow {
  
  private Calendar calendar;
  
  public Day() {
    calendar = Calendar.getInstance();
    ConfigManager cfgm = CEPManager.getInstance().getConfigMgr();
    TimeZone tz = cfgm.getDefaultTimeZone();
    calendar.setTimeZone(tz);
  }

  public boolean canOutputTsGTInputTs() {
    return true;
  }

  public boolean expiredW(EventTimestamp t, EventTimestamp expTs) {    
    // Retain nanoTime up to millisecond granularity in milliTime
    // Ignore microsecond part of time stamp value
    long milliTime = t.getTime() / 1000000;
    calendar.clear();
    calendar.setTimeInMillis(milliTime);
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH);
    int day = calendar.get(Calendar.DAY_OF_MONTH);
    calendar.clear();
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.MONTH, month);
    calendar.set(Calendar.DAY_OF_MONTH, day+1);
    calendar.set(Calendar.HOUR_OF_DAY , 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    //System.out.println(calendar.getTimeZone().toString() + milliTime +" : " +" "+month+"/"+(day+1)+"/"+year+" " + calendar.getTimeInMillis());
    // Set calendar Time to expTs in nanosecond unit    
    expTs.setTime(calendar.getTimeInMillis() * 1000000);    
    return true;
  }

  public void setInputParams(Object[] obj) throws IOException {
    if(obj.length > 0)
      throw new IOException("inappropriate number of arguments");
  }

  public boolean visibleW(EventTimestamp t, EventTimestamp visTs) {
    expiredW(t, visTs);
    long nanoTime = visTs.getTime();
    visTs.setTime(nanoTime - 1);
    return true;
  }
  
}


