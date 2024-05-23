/* $Header: pcbpel/cep/common/src/oracle/cep/common/RangeConverter.java /main/2 2009/01/29 19:41:50 anasrini Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    01/21/09 - add MICROSECOND
    sbishnoi    07/25/08 - Creation
 */

package oracle.cep.common;

/**
 *  RangeConverter is responsible to convert time values to NANOSECOND unit
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/common/RangeConverter.java /main/2 2009/01/29 19:41:50 anasrini Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class RangeConverter 
{
  /**
   * Convert a Given Time Value to NANOSECOND time unit
   * @param rangeAmount
   * @param rangeUnit
   * @return
   */
  public static long interpRange(long rangeAmount, TimeUnit rangeUnit)
  {
    switch(rangeUnit)
    {
    case NANOSECOND:
      return rangeAmount;

    case MICROSECOND:
      return rangeAmount * 1000;

    case MILLISECOND:
      return rangeAmount * 1000 * 1000;

    case NOTIMEUNIT:
    case SECOND:
      return rangeAmount * 1000 * 1000 * 1000;

    case MINUTE:
      return rangeAmount * 60 * 1000 * 1000 * 1000;
        
    case HOUR:
      return rangeAmount * 60 * 60 * 1000 * 1000 * 1000;
        
    case DAY:
      return rangeAmount * 60 * 60 * 24 * 1000 * 1000 * 1000;
        
    default:
      assert false;
    }
    return 0;
  }
}
