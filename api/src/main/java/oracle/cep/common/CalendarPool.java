/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/CalendarPool.java /main/1 2013/09/24 19:06:36 sbishnoi Exp $ */

/* Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    09/24/13 - Creation
 */

package oracle.cep.common;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/CalendarPool.java /main/1 2013/09/24 19:06:36 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class CalendarPool
{
  private ThreadLocal<Calendar> calendar = new ThreadLocal<Calendar>() 
    {
      protected Calendar initialValue() 
      {
        return new GregorianCalendar();
      }
  };
  
  private static CalendarPool calendarPool = new CalendarPool();

  public static CalendarPool getCalendarPool() 
  { 
    return calendarPool; 
  }

  public Calendar getCalendar() 
  {
    return calendar.get();
  }
  
  private CalendarPool() {}
}