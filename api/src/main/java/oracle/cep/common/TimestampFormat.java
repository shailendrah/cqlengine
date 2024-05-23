/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/TimestampFormat.java /main/4 2013/10/08 11:09:54 sbishnoi Exp $ */

/* Copyright (c) 2011, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    02/15/12 - making serializable
    sbishnoi    10/03/11 - Creation
 */
package oracle.cep.common;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.DataStructuresError;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/TimestampFormat.java /main/4 2013/10/08 11:09:54 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class TimestampFormat implements Externalizable
{

  private static final long serialVersionUID = 6464803821989438088L;

  /** Number of digits where the fractional part of the SECOND datetime field
   *  Allowed range is 0-9. Default is 6. */
  private int numFractionalSeconds; 
  
  /** Time zone of given time value */
  private TimeZone timeZone;
  
  /** flag to specify if the local timezone is to be considered*/  
  private boolean isLocalTimeZone;
  
  /** flag to specify if the timestamp data will contain timezone info*/
  private boolean hasTimeZone;
  
  /** frmat of given timevalue */
  private DateFormat dateFormat;
  
  /** default timestamp format */
  private static TimestampFormat defaultFormat;
  
  /** flag to specify whether the attribute value in stream event has timezone*/
  private boolean isUserSpecifiedTimeZone;
  
  /**
   * Default Constructor
   */
  public TimestampFormat()
  {
    this.numFractionalSeconds = 6; 
    hasTimeZone               = false;
    isLocalTimeZone           = false;
    isUserSpecifiedTimeZone   = false;
    
    // Get the timezone value
    timeZone = CEPDateFormat.getInstance().getDefaultTimeZone();
    dateFormat  = CEPDateFormat.getInstance().getDefaultFormat();
    dateFormat.setTimeZone(timeZone);    
    try
    {
      validateTimestampFormat();
    }
    catch(CEPException e)
    {
      // As all values are default, so no validation exception should arise
      assert false;
    }
  }
  
  /**
   * Constructor to get TimestampFormat for a specific time zone
   * @param tz
   */
  public TimestampFormat(TimeZone tz)
  {
    this.numFractionalSeconds = 6; 
    hasTimeZone               = true;
    isLocalTimeZone           = false;
    isUserSpecifiedTimeZone   = true;
    
    // Get the timezone value
    timeZone = tz;
    dateFormat  = CEPDateFormat.getInstance().getDefaultFormat();
    dateFormat.setTimeZone(timeZone);    
    try
    {
      validateTimestampFormat();
    }
    catch(CEPException e)
    {
      // As all values are default, so no validation exception should arise
      assert false;
    }
  }
  
  /**
   * Constructor with the field to specify number of allowed fractional seconds 
   * @param numFractionalSeconds
   */
  public TimestampFormat(int numFractionalSeconds) throws CEPException
  {
    this.numFractionalSeconds = numFractionalSeconds;
    validateTimestampFormat();
  }
  
  /** Validate given timestamp format */
  public void validateTimestampFormat() throws CEPException
  {
    if(numFractionalSeconds < 0 || numFractionalSeconds > 9)
      throw new CEPException(
        DataStructuresError.DATETIME_INTERVAL_PRECISION_OUT_OF_RANGE);
  }
  
  /**
   * Get the CEP's Default timestamp format
   * @return
   */
  public static TimestampFormat getDefault()
  {
    if(defaultFormat == null)
    {
      defaultFormat = new TimestampFormat();
    }
    return defaultFormat;
  }
  
  /**
   * Get Timestamp format for a specific time zone
   * @param tz
   * @return
   */
  public static TimestampFormat getTimestampWithTz(TimeZone tz)
  {
    TimestampFormat timestampFormatTz = new TimestampFormat(tz);
    return timestampFormatTz;
  }

  /**
   * @return the isLocalTimeZone
   */
  public boolean isLocalTimeZone()
  {
    return isLocalTimeZone;
  }

  /**
   * @param isLocalTimeZone the isLocalTimeZone to set
   */
  public void setLocalTimeZone(boolean isLocalTimeZone)
  {
    this.isLocalTimeZone = isLocalTimeZone;
    
    // Set the timezone property if there is a local timezone specified
    if(isLocalTimeZone)
      this.hasTimeZone = true;
  }

  /**
   * @return the hasTimeZone
   */
  public boolean isHasTimeZone()
  {
    return hasTimeZone;
  }

  /**
   * @param hasTimeZone the hasTimeZone to set
   */
  public void setHasTimeZone(boolean hasTimeZone)
  {
    this.hasTimeZone = hasTimeZone;
  }

  /**
   * @return the numFractionalSeconds
   */
  public int getNumFractionalSeconds()
  {
    return numFractionalSeconds;
  }

  /**
   * @return the dateFormat
   */
  public DateFormat getDateFormat()
  {
    return dateFormat;
  }
  
  /**
   * @param dateFormat the dateFormat to set
   */
  public void setDateFormat(DateFormat dateFormat)
  {
    this.dateFormat = dateFormat;
  }

  /**
   * @return the timeZone
   */
  public TimeZone getTimeZone()
  {
    return timeZone;
  }

  /**
   * @param timeZone the timeZone to set
   */
  public void setTimeZone(TimeZone timeZone)
  {
    this.timeZone = timeZone;
  } 
  
  public boolean isUserSpecifiedTimeZone() {
    return isUserSpecifiedTimeZone;
  }

  public void setUserSpecifiedTimeZone(boolean isUserSpecifiedTimeZone) {
    this.isUserSpecifiedTimeZone = isUserSpecifiedTimeZone;
  }

  public String toString()
  {
    StringBuffer b = new StringBuffer();
    
    // Append number of fractional seconds
    b.append("<NumFractionalSeconds>" + this.numFractionalSeconds + 
                       "</NumFractionalSeconds>");
    
    // Append timezone value
    if(hasTimeZone)
    {
      if(isLocalTimeZone)
        b.append("<TimeZone>LocalTimeZone</TimeZone>");
      else if(timeZone != null)
        b.append("<TimeZone" + timeZone.getDisplayName() + "</TimeZone>");
      else
        b.append("<TimeZone>InvalidTimeZone</TimeZone>");
    }
    b.append("<isUserSpecifiedTimezone>"+ this.isUserSpecifiedTimeZone
      + "</isUserSpecifiedTimezone>");
    
    // Append date format value
    if(dateFormat != null) {
      if (dateFormat instanceof SimpleDateFormat)
    	  b.append("<Format>" + ((SimpleDateFormat)dateFormat).toPattern() + "</Format>");
      else b.append("<Format>" + dateFormat.toString() + "</Format>");
    }
    
    return b.toString();  
  }
  
  /**
   * Deep Clone the TimestampFormat object
   */
  public TimestampFormat clone()
  {
    TimestampFormat value = new TimestampFormat();
    value.numFractionalSeconds = this.numFractionalSeconds;
    value.hasTimeZone = this.hasTimeZone;
    value.isUserSpecifiedTimeZone = this.isUserSpecifiedTimeZone;
    value.isLocalTimeZone = this.isLocalTimeZone;
    value.dateFormat = (DateFormat) this.dateFormat.clone();
    value.timeZone = this.timeZone;
    return value;
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
       out.writeBoolean(isLocalTimeZone); 
       out.writeBoolean(hasTimeZone); 
       out.writeBoolean(isUserSpecifiedTimeZone); 
       out.writeInt(numFractionalSeconds); 
       out.writeObject(timeZone); 
       out.writeObject(dateFormat); 
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
      isLocalTimeZone = in.readBoolean(); 
      hasTimeZone = in.readBoolean(); 
      isUserSpecifiedTimeZone = in.readBoolean(); 
      numFractionalSeconds = in.readInt(); 
      timeZone = (TimeZone) in.readObject(); 
      dateFormat = (SimpleDateFormat) in.readObject(); 
  }
 
}
