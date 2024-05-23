/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/CEPSimpleDateFormat.java /main/1 2013/10/08 11:09:54 sbishnoi Exp $ */

/* Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    10/06/13 - Creation
 */
package oracle.cep.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/CEPSimpleDateFormat.java /main/1 2013/10/08 11:09:54 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class CEPSimpleDateFormat extends SimpleDateFormat
{
  private static final long serialVersionUID = 8870866815472438070L;

  private Pattern pattern;
  
  boolean isPatternRegistered = false;
  
  /** A flag to check if the date format is default database format 
   * dd-MMM-yy hh.mm.ss.SSSSS
   */
  boolean isDefaultDBFormat = false;
  
  public CEPSimpleDateFormat(String format, String pattern, boolean isPatternRegistered)
  {
    super(format);      
    if(pattern != null)
    {
      this.pattern = Pattern.compile(pattern);
      this.isPatternRegistered = isPatternRegistered;
    }
  }

  /**
   * A Customized format function to handling the formatting of the time value
   * with default database format.
   * In CQL default database format, the timestamp values has fractional second 
   * component of length 6.
   * While formatting a millisecond time value using java.text.SimpleDateFormat,
   * if the number of milliseconds is higher than 1000, then we will calculate
   * the second part out of total milliseconds and add to seconds.
   * This will leave the millisecond fraction to have at most 3 DIGITS.
   * In Default DB format "dd-MMM-yy hh.mm.ss.SSSSSS a", the first 3 digits will
   * always be ZEROes.
   * But in database(oracle), the ZERO padding is for trailing digits and not
   * for heading digits.
   * To format as per database, we are formatting the ts value with trailling 
   * zero using format "dd-MMM-yy hh.mm.ss.SSS00"
   * @param date
   * @return
   */
  public String format(long date)
  {
    if(this.isDefaultDBFormat)
    {
      SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS000 a");
      df.setTimeZone(this.getTimeZone());
      return df.format(new Date(date));
    }
    else
      return this.format(new Date(date));
  }
  
  public Pattern getPattern()
  {
    return pattern;
  }
  
  public void setDefaultDBFormat(boolean isDefaultDBFormat)
  {
    this.isDefaultDBFormat = isDefaultDBFormat;
  }
}
