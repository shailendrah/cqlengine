/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/TimeUnit.java /main/5 2011/09/05 22:47:26 sbishnoi Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Enumeration of time units that may be used in windowing expressions 

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)  
    sbishnoi    07/18/11 - adding YEAR and MONTH
    anasrini    01/21/09 - add MICROSECOND
    mthatte     01/26/09 - adding toString()
    sbishnoi    07/24/08 - adding NANOSECOND time unit
    anasrini    02/08/06 - Creation
    anasrini    02/08/06 - Creation
    anasrini    02/08/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/common/TimeUnit.java /main/4 2009/02/19 11:21:29 skmishra Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.common;

/**
 * Enumeration of time units that may be used in windowing expressions 
 *
 * @since 1.0
 */

public enum TimeUnit 
{
  NOTIMEUNIT(""), 
  NANOSECOND("nanoseconds"), 
  MICROSECOND("microseconds"), 
  MILLISECOND("milliseconds"), 
  SECOND("seconds"), 
  MINUTE("minutes"), 
  HOUR("hours"), 
  DAY("days"),
  MONTH("months"),
  YEAR("years");
  
  private String myString;
  
  TimeUnit(String s)
  {
    myString = s;
  }
  
  public String toString() 
  {
    return myString;
  }

  public static TimeUnit fromString(String s) {
    s = s.toLowerCase();
    for (TimeUnit t : TimeUnit.values()) {
      if (t.equals(s)) return t;
    }
    for (TimeUnit t : TimeUnit.values()) {
      if (t.myString.equals(s)) return t;
    }
    return null;
  }

  public static TimeUnit getNext(TimeUnit unit)
  {
    switch(unit)
    {
    case YEAR:
      return MONTH;
    case MONTH:
      return DAY;
    case DAY:
      return HOUR;
    case HOUR:
      return MINUTE;
    case MINUTE:
      return SECOND;
    case SECOND:
      return null;
    default:
      return null;
    }
  }
}
