/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/IntervalFormat.java /main/2 2012/02/16 10:38:18 sbishnoi Exp $ */

/* Copyright (c) 2011, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    02/15/12 - making serializable
    sbishnoi    06/21/11 - Creation
 */

package oracle.cep.common;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.logging.Level;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.DataStructuresError;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/IntervalFormat.java /main/2 2012/02/16 10:38:18 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class IntervalFormat implements Externalizable
{
  private static final long serialVersionUID = 1318865271654969661L;
  
  private TimeUnit leadingField;
  private TimeUnit trailingField;
  
  /** Precision is the maximum number of digits in the leading field. 
   *  The valid range of the leading field precision is 0 to 9 and 
   *  its default value is 2.*/
  private int leadingPrecision = Constants.DEFAULT_INTERVAL_LEADING_PRECISION;
  
  /**
   * trailing precision is for the fractional seconds part in the type definition
   * INTERVAL DAY [(day_precision)] TO SECOND [(fractional_seconds)]
   * Default value is SIX.
   */
  private int trailingFractionalSeconds = 
    Constants.DEFAULT_INTERVAL_FRACTIONAL_SECONDS_PRECISION;
  
  /** 
   * A flag to check if this interval format represent YEAR TO MONTH interval
   * or a DAY TO SECOND interval 
   */  
  private boolean isYearToMonthInterval;
  
  public IntervalFormat()
  {
  }
  
  /**
   * Constructor to create an interval format having only leading field with
   * a default precision value
   * @param leadingField
   * @throws CEPException
   */
  public IntervalFormat(TimeUnit leadingField)
    throws CEPException
  {
    this.leadingField = leadingField;
    isYearToMonthInterval = this.leadingField == TimeUnit.YEAR ||
                            this.leadingField == TimeUnit.MONTH;
    validateIntervalFormat();
  }
  
  /**
   * Constructor to create an interval format having only leading field
   * and with a user defined precision for leading field
   * @param leadingField
   * @param leadingPrecision
   * @throws CEPException
   */
  public IntervalFormat(TimeUnit leadingField, 
                        int leadingPrecision)
    throws CEPException
  {
    this.leadingField     = leadingField;    
    this.leadingPrecision = leadingPrecision;
    isYearToMonthInterval = this.leadingField == TimeUnit.YEAR ||
        this.leadingField == TimeUnit.MONTH;
    validateIntervalFormat();
  }
  
  /**
   * 
   * @param leadingField
   * @param leadingPrecision
   * @param trailingFractionalSeconds
   * @throws CEPException
   */
  public IntervalFormat(TimeUnit leadingField, 
                        int leadingPrecision,
                        int trailingFractionalSeconds)
    throws CEPException
  {
    this.leadingField     = leadingField;
    this.leadingPrecision = leadingPrecision;
    this.trailingFractionalSeconds = trailingFractionalSeconds;
    isYearToMonthInterval = this.leadingField == TimeUnit.YEAR ||
        this.leadingField == TimeUnit.MONTH;
    validateIntervalFormat();
  }
  
  /**
   * Constructor to create an interval format having both leading and trailing
   * fields with a default precision for leading field
   * @param leadingField
   * @param trailingField
   * @throws CEPException
   */
  public IntervalFormat(TimeUnit leadingField, 
                        TimeUnit trailingField)   
    throws CEPException
  {
    this.leadingField  = leadingField;
    this.trailingField = trailingField;
    isYearToMonthInterval = this.leadingField == TimeUnit.YEAR ||
        this.leadingField == TimeUnit.MONTH;
    validateIntervalFormat();
  }
  
  /**
   * Constructor to create an interval format having both leading and trailing
   * fields with a user defined precision for leading field
   * @param leadingField
   * @param trailingField
   * @param precision
   * @param isleadingPrecision
   * @throws CEPException
   */
  public IntervalFormat(TimeUnit leadingField, 
                        TimeUnit trailingField, 
                        Integer precision,
                        boolean isleadingPrecision)
    throws CEPException
  {
    this.leadingField     = leadingField;
    this.trailingField    = trailingField;
    if(isleadingPrecision)
      this.leadingPrecision = precision;
    else
      this.trailingFractionalSeconds = precision;
    isYearToMonthInterval = this.leadingField == TimeUnit.YEAR ||
        this.leadingField == TimeUnit.MONTH;
    validateIntervalFormat();
  }
  
  
  /**
   * Constructor to create an interval format having both leading and trailing
   * fields with both leading field precision and trailing field's fractions
   * @param leadingField
   * @param trailingField
   * @param leadingPrecision
   * @param trailingFractionalSeconds
   */
  public IntervalFormat(TimeUnit leadingField, 
                        TimeUnit trailingField, 
                        Integer leadingPrecision,
                        int trailingFractionalSeconds)
    throws CEPException
  {
    this.leadingField     = leadingField;
    this.trailingField    = trailingField;
    this.leadingPrecision = leadingPrecision;
    this.trailingFractionalSeconds = trailingFractionalSeconds;
    isYearToMonthInterval = this.leadingField == TimeUnit.YEAR ||
        this.leadingField == TimeUnit.MONTH;
    validateIntervalFormat();
  }

  /**
   * @return the leadingField
   */
  public TimeUnit getLeadingField()
  {
    return leadingField;
  }

  /**
   * @return the trailingField
   */
  public TimeUnit getTrailingField()
  {
    return trailingField;
  }

  /**
   * @return the leadingPrecision
   */
  public int getLeadingPrecision()
  {
    return leadingPrecision;
  }

  /**
   * maximum leading precisioin allowed
   * @return
   */
  public int getMaxLeadingPrecision()
  {
    return Constants.MAX_INTERVAL_LEADING_PRECISION;
  }
  
  /**
   * @return the trailingFractionalSeconds
   */
  public int getTrailingFractionalSeconds()
  {
    return trailingFractionalSeconds;
  }

  /**
   * max trailing fractional seconds.
   * @return
   */
  public int getMaxTrailingFractionalSeconds()
  {
    return Constants.MAX_INTERVAL_FRACTIONAL_SECONDS_PRECISION;
  }
  /**
   * @return the isYearToMonthInterval
   */
  public boolean isYearToMonthInterval()
  {
    return isYearToMonthInterval;
  }

  /**
   * @return the string representation of interval format
   */
  public String toString()
  {
    StringBuilder sb = new StringBuilder(leadingField.toString() + " (" + 
        leadingPrecision + ") ");
    
    if(trailingField != null)
      sb.append(" TO " + trailingField.toString());
    
    if(!isYearToMonthInterval)
      sb.append("(" + trailingFractionalSeconds + ")");
    
    return sb.toString();    
  }

  //External spec
  //from unit (precision) - to unit

  public String toSpec() {
    StringBuilder sb = new StringBuilder();
    sb.append(leadingField.toString());
    if(trailingField != null) {
      sb.append(" - " + trailingField.toString());
    }
    return sb.toString();
  }

  public static IntervalFormat fromSpec(String spec) throws CEPException {
    String[] elems = spec.split("-");
    String lunit = elems[0].trim();
    TimeUnit l = TimeUnit.fromString(lunit);
    TimeUnit t = null;
    if (l == null) throw new IllegalArgumentException("Unknown timeunit : "+lunit);
    if (elems.length > 1) {
      String hunit = elems[1].trim();
      t = TimeUnit.fromString(hunit);
      if (t == null) throw new IllegalArgumentException("Unknown timeunit : "+hunit);
    }
    return new IntervalFormat(l, t);
  }

  public boolean equals(IntervalFormat other)
  {
    if(leadingField != other.getLeadingField())
      return false;
    
    if(trailingField != other.getTrailingField())
      return false;
    
    if(leadingPrecision != other.getLeadingPrecision())
      return false;
    
    if(trailingFractionalSeconds != other.getTrailingFractionalSeconds())
      return false;
    
    return true;
  }
  
  public void validateIntervalFormat() throws CEPException
  {
    // Validation Check:
    // 1. Ensure that Leading Field is not same as Trailing field.
    if(leadingField != null && trailingField != null && 
       leadingField == trailingField)
      throw new CEPException(DataStructuresError.INVALID_INTERVAL);
    
    // 2. Precision is in allowed range [0-9]
    if(leadingPrecision < 0 || leadingPrecision > 9)
      throw new CEPException(
        DataStructuresError.DATETIME_INTERVAL_PRECISION_OUT_OF_RANGE);
    
    if(isYearToMonthInterval)
    {
      validateIntervalYMFormat();
    }
    else
    {
      validateIntervalDSFormat();
    }
  }
  public void validateIntervalYMFormat() throws CEPException
  {
    // Validation Checks: 
    // 1. Ensure that Trailing Field in YEAR TO MONTH interval should be MONTH.    
    if(trailingField != null && trailingField != TimeUnit.MONTH)
      throw new CEPException(
        DataStructuresError.MISSING_OR_INVALID_DATETIME_FIELD);   
    
    if(trailingField != null && 
       (trailingFractionalSeconds < 1 || trailingFractionalSeconds > 9))
    {
      throw new CEPException(
          DataStructuresError.DATETIME_INTERVAL_PRECISION_OUT_OF_RANGE);
    }
         
  }
  public void validateIntervalDSFormat() throws CEPException
  {
    // Validation Checks for trailing field: 
    if(trailingField != null)
    {
      // 1. Fractional precision is in allowed range [1-9]       
      if(trailingFractionalSeconds > 9 || trailingFractionalSeconds < 1)
        throw new CEPException(
          DataStructuresError.DATETIME_INTERVAL_PRECISION_OUT_OF_RANGE);
      
      //2. Leading field should be bigger unit than Trailing Field
      if(leadingField.ordinal() < trailingField.ordinal())
        throw new CEPException(
            DataStructuresError.MISSING_OR_INVALID_DATETIME_FIELD);
    }
  }

  public static IntervalFormat getDefaultInternalYMFormat()
  {
    try
    {
      return new IntervalFormat(TimeUnit.YEAR, TimeUnit.MONTH, 9, true);
    } 
    catch (CEPException e)
    {
      LogUtil.info(LoggerType.TRACE, "Error Creating Default Internal Interval YEAR TO MONTH format. " + e.getCauseMessage());
      LogUtil.logStackTrace(LoggerType.TRACE, Level.INFO, e);
      e.printStackTrace();
    }
    return null;
  }
  
  public static IntervalFormat getDefaultInternalDSFormat()
  {
    try
    {
      return new IntervalFormat(TimeUnit.DAY,TimeUnit.SECOND, 9,9);
    } 
    catch (CEPException e)
    {
      LogUtil.info(LoggerType.TRACE, "Error Creating Default Internal Interval DAY TO SECOND format. " + e.getCauseMessage());
      LogUtil.logStackTrace(LoggerType.TRACE, Level.INFO, e);
      e.printStackTrace();
    }
    return null;
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
      out.writeInt(leadingPrecision);
      out.writeInt(trailingFractionalSeconds);
      out.writeBoolean(isYearToMonthInterval);
      out.writeObject(leadingField) ;
      out.writeObject(trailingField) ;
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
      leadingPrecision = in.readInt();
      trailingFractionalSeconds = in.readInt();
      isYearToMonthInterval = in.readBoolean();
      leadingField = (TimeUnit)in.readObject() ;
      trailingField = (TimeUnit)in.readObject() ;
  }
  
}
