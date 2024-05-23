/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/CEPDate.java /main/1 2012/01/20 11:47:14 sbishnoi Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    01/11/12 - Creation
 */
package oracle.cep.common;

import java.sql.Timestamp;
/**
 *  @version $Header: CEPDate.java 11-jan-2012.03:53:02 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class CEPDate extends Timestamp
{
  /** timestamp/date value in the unit of nanoseconds*/
  long value;

  /** format of timestamp/date value */
  TimestampFormat format;

  /**
   * Constructor with single parameter
   * @param value number of nanoseconds
   */
  public CEPDate(long value)
  {
    super(value/1000000l);
    this.value = value;
    format = TimestampFormat.getDefault();
  }
  
  /**
   * Constructor with two parameters
   * @param value number of nanoseconds
   * @param format format of the given timestamp/date value
   */
  public CEPDate(long value, TimestampFormat format)
  {
    super(value/1000000l);
    this.value = value;
    this.format = format;
  }
  /**
   * @return the value
   */
  public long getValue()
  {
    return value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(long value)
  {
    this.value = value;
  }

  /**
   * @return the format
   */
  public TimestampFormat getFormat()
  {
    return format;
  }

  /**
   * @param format the format to set
   */
  public void setFormat(TimestampFormat format)
  {
    this.format = format;
  }

}
