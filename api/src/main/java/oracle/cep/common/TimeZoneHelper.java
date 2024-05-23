/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/TimeZoneHelper.java /main/1 2013/10/08 11:09:54 sbishnoi Exp $ */

/* Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    10/05/13 - Creation
 */
package oracle.cep.common;

import java.util.HashMap;
import java.util.TimeZone;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/TimeZoneHelper.java /main/1 2013/10/08 11:09:54 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class TimeZoneHelper
{
  private static HashMap<Integer, String> offsetTimeZoneIDMap = new HashMap<Integer, String>();
  
  static
  {
    offsetTimeZoneIDMap.put(-43200000,"-12:00");
    offsetTimeZoneIDMap.put(-39600000,"-11:00");
    offsetTimeZoneIDMap.put(-36000000,"-10:00");
    offsetTimeZoneIDMap.put(-34200000,"-09:30");
    offsetTimeZoneIDMap.put(-32400000,"-09:00");
    offsetTimeZoneIDMap.put(-28800000,"-08:00");
    offsetTimeZoneIDMap.put(-25200000,"-07:00");
    offsetTimeZoneIDMap.put(-21600000,"-06:00");
    offsetTimeZoneIDMap.put(-18000000,"-05:00");
    offsetTimeZoneIDMap.put(-16200000,"-04:30");
    offsetTimeZoneIDMap.put(-14400000,"-04:00");
    offsetTimeZoneIDMap.put(-12600000,"-03:30");
    offsetTimeZoneIDMap.put(-10800000,"-03:00");
    offsetTimeZoneIDMap.put(-7200000,"-02:00");
    offsetTimeZoneIDMap.put(-3600000,"-01:00");
    offsetTimeZoneIDMap.put(0,"+00:00");
    offsetTimeZoneIDMap.put(3600000,"+01:00");
    offsetTimeZoneIDMap.put(7200000,"+02:00");
    offsetTimeZoneIDMap.put(10800000,"+03:00");
    offsetTimeZoneIDMap.put(12600000,"+03:30");
    offsetTimeZoneIDMap.put(14400000,"+04:00");
    offsetTimeZoneIDMap.put(16200000,"+04:30");
    offsetTimeZoneIDMap.put(18000000,"+05:00");
    offsetTimeZoneIDMap.put(19800000,"+05:30");
    offsetTimeZoneIDMap.put(20700000,"+05:45");
    offsetTimeZoneIDMap.put(21600000,"+06:00");
    offsetTimeZoneIDMap.put(23400000,"+06:30");
    offsetTimeZoneIDMap.put(25200000,"+07:00");
    offsetTimeZoneIDMap.put(28800000,"+08:00");
    offsetTimeZoneIDMap.put(31500000,"+08:45");
    offsetTimeZoneIDMap.put(32400000,"+09:00");
    offsetTimeZoneIDMap.put(34200000,"+09:30");
    offsetTimeZoneIDMap.put(36000000,"+10:00");
    offsetTimeZoneIDMap.put(37800000,"+10:30");
    offsetTimeZoneIDMap.put(39600000,"+11:00");
    offsetTimeZoneIDMap.put(41400000,"+11:30");
    offsetTimeZoneIDMap.put(43200000,"+12:00");
    offsetTimeZoneIDMap.put(45900000,"+12:45");
    offsetTimeZoneIDMap.put(46800000,"+13:00");
    offsetTimeZoneIDMap.put(50400000,"+14:00");
  }
  
  public static String getCustomTimeZoneStr(String timezone)
  {
    TimeZone tz = TimeZone.getTimeZone(timezone);
    int tzOffset = tz.getOffset(0);
    return offsetTimeZoneIDMap.get(tzOffset);
  }
  
  public static String getCustomTimeZoneStr(TimeZone tz)
  {
    int tzOffset = tz.getOffset(0);
    return offsetTimeZoneIDMap.get(tzOffset);
  }
}