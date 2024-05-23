/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/comparator/Comparison.java /main/3 2009/11/09 10:10:58 sborah Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      06/30/09 - support for bigdecimal
    udeshmuk    01/31/08 - support for double data type.
    parujain    06/29/07 - Comparisons
    parujain    06/29/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/comparator/Comparison.java /main/3 2009/11/09 10:10:58 sborah Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.comparator;

import java.math.BigDecimal;

import oracle.cep.common.Datatype;

public class Comparison{

  /**
   * Compare 2 byte arrays lexicographically (dictionary order)
   * 
   * @param b1
   *          First array to be compared
   * @param l1
   *          Length of first argument
   * @param b2
   *          Second array to be compared
   * @param l2
   *          Length of second argument
   * @return -ve number if first array is less +ve number if first array is more
   *         else 0
   */
  public static int byteCompare(byte[] b1, int l1, byte[] b2, int l2)
  {
    int i;
    int limit = (l1 < l2) ? l1 : l2;
    char c1, c2;

    // scan until elements are the same
    for (i = 0; (i < limit) && (b1[i] == b2[i]); i++)
      ;
    if (i == limit)
      return (l1 - l2);
    else {
      c1 = Datatype.hexchars[(b1[i]&0xf0)>>>4];
      c2 = Datatype.hexchars[(b2[i]&0xf0)>>>4];
      if (c1 != c2)
       return (c1 - c2);
      else {
      c1 = Datatype.hexchars[b1[i]&0x0f];
      c2 = Datatype.hexchars[b2[i]&0x0f];
      return (c1 - c2);
      }
    }
  }
  
  /**
   * Compare 2 char arrays lexicographically (dictionary order)
   * 
   * @param c1
   *          First array to be compared
   * @param l1
   *          Length of first argument
   * @param c2
   *          Second array to be compared
   * @param l2
   *          Length of second argument
   * @return -ve number if first array is less +ve number if first array is more
   *         else 0
   */
  public static int charCompare(char[] c1, int l1, char[] c2, int l2)
  {
    int i;
    int limit = (l1 < l2) ? l1 : l2;
    int ret = (l1 - l2);

    // scan until elements are the same
    for (i = 0; (i < limit) && (c1[i] == c2[i]); i++)
      ;
    if (i == limit)
      return ret;
    else
      return (c1[i] - c2[i]);
  }
  
  /**
   * Compare two int values
   * 
   * @param i1
   *         First integer value
   * @param i2
   *         Second integer value
   * @return -ve number if first array is less +ve number if first array is more
   *         else 0
   */
  public static int intCompare(int i1, int i2)
  {
    if(i1 > i2)
      return 1;
    else if(i1 < i2)
      return -1;
    return 0;
  }
  
  /**
   * Compare two bigint values
   * 
   * @param val1
   *            First bigint value
   * @param val2
   *           Second bigint value
   * @return -ve number if first array is less +ve number if first array is more
   *         else 0
   */
  public static int bigintCompare(long val1, long val2)
  {
    if(val1 > val2)
      return 1;
    else if(val1 < val2)
      return -1;
    return 0;
  }
  
  /**
   * Compares two float values
   * 
   * @param val1
   *            First float value
   * @param val2
   *           Second float value
   * @return -ve number if first array is less +ve number if first array is more
   *         else 0
   */
  public static int floatCompare(float val1, float val2)
  {
    if(val1 > val2)
      return 1;
    else if(val1 < val2)
      return -1;
    return 0;
  }
  
  /**
   * Compares two double values
   * 
   * @param val1
   *            First double value
   * @param val2
   *           Second double value
   * @return -ve number if first array is less +ve number if first array is more
   *         else 0
   */
  public static int doubleCompare(double val1, double val2)
  {
    if(val1 > val2)
      return 1;
    else if(val1 < val2)
      return -1;
    return 0;
  }
  
  /**
   * Compares two BigDecimal values
   * 
   * @param val1
   *            First BigDecimal value
   * @param val2
   *           Second BigDecimal value
   * @return -ve number if first array is less +ve number if first array is more
   *         else 0
   */
  public static int bigDecimalCompare(BigDecimal val1, BigDecimal val2)
  {
    return val1.compareTo(val2);
  }
  /**
   * Compare two interval values
   * 
   * @param interval1
   *            First interval value
   * @param interval2
   *            Second interval value
   * @return -ve number if first array is less +ve number if first array is more
   *         else 0
   */
  public static int intervalCompare(long interval1, long interval2)
  {
    if(interval1 > interval2)
      return 1;
    else if(interval1 < interval2)
      return -1;
    return 0;
  }
  
  /**
   * Compare two timestamp values
   * 
   * @param time1
   *            First time value
   * @param time2
   *            Second time value
   * @return -ve number if first array is less +ve number if first array is more
   *         else 0
   */
  public static int timestampCompare(long time1,long time2)
  {
    if(time1 > (time2))
      return 1;
    else if(time1 < (time2))
      return -1;
    return 0;
  }
}
