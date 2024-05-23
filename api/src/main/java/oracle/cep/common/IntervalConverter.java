/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/IntervalConverter.java /main/6 2013/08/21 05:52:13 sbishnoi Exp $ */

/* Copyright (c) 2007, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    08/20/13 - bug 17084336
    sbishnoi    08/22/11 - support Interval DaytoSecond and YeartoMonth
    sbishnoi    06/22/08 - modifying getInterval, dont throw exception
    hopark      02/05/08 - parameterized error
    parujain    12/05/07 - converts interval to string and vice versa
    parujain    12/05/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/IntervalConverter.java /main/6 2013/08/21 05:52:13 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.common;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.DataStructuresError;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

public class IntervalConverter 
{
  /** Lowest Unit of Time in CEP is NANOSECOND */
  public final static long NANOSECONDS_MULTIPLICATION_FACTOR = 1000000000l;
  
  /** Lowest Unit of Time in INTERVAL is SECOND */
  public final static long SECOND = 1 ;
  
  /** 1 MINUTE = 60 SECONDS */
  public final static long MINUTE = SECOND * 60l;
  
  /** 1 HOUR = 3600 SECONDS */
  public final static long HOUR = MINUTE * 60l;
  
  /** 1 DAY = 86400 SECONDS */
  public final static long DAYS = HOUR * 24l;
  
  /** A Pattern which'll match to an input string having non-digit characters*/
  private static Pattern NON_DIGITS = Pattern.compile("\\D");
  
  /** A Pattern representing DIGIT* DASH DIGIT*   */
  private static Pattern DIGITS_DASH_DIGITS = Pattern.compile("\\d+-\\d+");
  
  /**
   * Convert & Return a String INTERVAL value to a numeric INTERVAL value 
   * @param value input String INTERVAL Value
   * @param format format of input string
   * @return INTERVAL as number of nanos if IntervalFormat is DAY TO SECOND
   *         INTERVAL as number of months if IntervalFormat is YEAR TO MONTH
   * @throws CEPException
   */
  public static Long parseIntervalString(String value, IntervalFormat format)
    throws CEPException
  {
    try
    {
      // TODO: Currently we are parsing interval value using format INTERVAL DAY(9) TO SECONDS(9).
      // This is because interval type is created internally by CQL function (timestamp-timestamp or numtodsinterval) in
      // all our use-cases.
      // In both cases, the interval value format is INTERVAL DAY(9) TO SECONDS(9).
      // As we are not preserving format across multiple transformation in spark cql stages, we need a format value
      // in case if we want to convert interval to object and object to interval.
      if(format.isYearToMonthInterval())
      {
        return IntervalConverter.parseYToMIntervalString(value, format);
      }
      else
      {
        return IntervalConverter.parseDToSIntervalString(value, format);
      }
    }
    catch(CEPException ce)
    {
      throw ce;
    }
    catch(NumberFormatException e) 
    {
      throw new CEPException(DataStructuresError.INVALID_INTERVAL);
    }
  }
  
  /**
   * Convert & Return a String INTERVAL value to a numeric INTERVAL value 
   * @param input input String INTERVAL value
   * @param format format of input string
   * @return number of nanos as IntervalFormat is DAY TO SECOND
   * @throws CEPException
   */
  public static Long parseDToSIntervalString(String inputData, 
                                             IntervalFormat format)
    throws CEPException
  {
    int sign = 1;
    char firstCharacter = inputData.charAt(0);
    String value = null;
    if(firstCharacter == '-')
    {
      sign = -1;
      value = inputData.substring(1);
    }
    else if(firstCharacter == '+')
    {
      sign = 1;
      value = inputData.substring(1);
    }
    else
      value = inputData;
    
    // Remove unwanted white spaces
    value = value.trim();    
    
    long  intervalValue = 0l;
    
    boolean isError    = false;
    char[] input       = value.toCharArray();
    StringBuffer token = new StringBuffer();
    int spaceCount     = 0;
    int colonCount     = 0;
    int decimalCount   = 0;
    
    TimeUnit leadingField  = format.getLeadingField();
    TimeUnit trailingField = format.getTrailingField();
    TimeUnit currentField  = leadingField;
    
    for(char ch: input)
    {
      if(Character.isDigit(ch))
      {
        token.append(ch);       
      }
      else if(ch == ' ')
      {
        spaceCount++;
        if(spaceCount > 1)
        {
          isError = true;
          LogUtil.fine(LoggerType.TRACE, "Whitespace Character encountered " +
            "more than once in interval value");
          break;
        }       
        if(currentField != TimeUnit.DAY)
        {
          isError = true;
          LogUtil.fine(LoggerType.TRACE, "Leading Field is not DAY");
          break;
        }
        if(trailingField == null)
        {
          isError = true;
          LogUtil.fine(LoggerType.TRACE, "Trailing Field is missing");
          break;
        }
        if(currentField == TimeUnit.DAY)
        {
          int numDayDigits        = token.toString().length();
          int numAllowedDayDigits = format.getMaxLeadingPrecision();
          if(numDayDigits > numAllowedDayDigits)
          {
            isError = true;
            LogUtil.fine(LoggerType.TRACE, 
              "the leading precision of the interval is too small");
            break;
          }
        }
        
        long numNanos = getDToSMultiplicationFactor(currentField) * 
                        Long.parseLong(token.toString());
        
        intervalValue = intervalValue + numNanos;        
        currentField = TimeUnit.getNext(currentField);
        
        if(trailingField != null && 
           currentField.ordinal() < trailingField.ordinal())
        {
          isError = true;
          LogUtil.fine(LoggerType.TRACE, "Trailing Field doesn't accumulate " +
          	"interval data : " + trailingField);
          break;
        }
        token = new StringBuffer();
      }
      else if(ch == ':')
      {
        colonCount++;
        if(colonCount > 2)
        {
          isError = true;
          LogUtil.fine(LoggerType.TRACE, 
            "Colon Character encountered more than twice");
          break;
        }
        if(trailingField == null)
        {
          isError = true;
          LogUtil.fine(LoggerType.TRACE, "Trailing Field is missing");
          break;
        }         
        if(currentField != TimeUnit.HOUR && currentField != TimeUnit.MINUTE)
        {
          isError = true;
          LogUtil.fine(LoggerType.TRACE, "Unit HOUR or MINUTE missing: Got " 
            + currentField);
          break;
        }
        
        long numNanos = getDToSMultiplicationFactor(currentField) 
                        * Long.parseLong(token.toString());
        intervalValue = intervalValue + numNanos;
        
        currentField = TimeUnit.getNext(currentField);        
        if(trailingField != null 
            && currentField.ordinal() < trailingField.ordinal())
        {
          isError = true;
          LogUtil.fine(LoggerType.TRACE,
            "Trailing Field doesn't accumulate interval data : " + 
            trailingField);
          break;
        }
        token = new StringBuffer();
      }
      else if(ch == '.')
      {
        decimalCount++;
        if(decimalCount > 1)
        {
          isError = true;
          LogUtil.fine(LoggerType.TRACE, 
            "Decimal Character encountered more than once");
          break;
        }
        
        if(currentField != TimeUnit.SECOND)
        {
          isError = true;
          LogUtil.fine(LoggerType.TRACE, 
            "Unit SECOND is missing : Got " + currentField);
          break;
        }
        long numNanos = getDToSMultiplicationFactor(currentField) * 
                        Long.parseLong(token.toString());
        
        intervalValue = intervalValue + numNanos;
        
        currentField = TimeUnit.getNext(currentField);
        token = new StringBuffer();
      }
      else
      {
          isError = true;
          break;
      }
    }   
    if(isError)
    {
      throw new CEPException(DataStructuresError.INVALID_INTERVAL);
    }
    
    if(token.length() > 0)
    {
      if((currentField == TimeUnit.SECOND || currentField == null)
          && decimalCount == 1)
      {
        for(int i = token.length(); i < 9; i++)
        {
          token.append("0");
        }
        long numNanos = Long.parseLong(token.toString());
        intervalValue = intervalValue + numNanos;        
      }
      else
      {       
        long numNanos = getDToSMultiplicationFactor(currentField) * 
                        Long.parseLong(token.toString());
        intervalValue = intervalValue + numNanos;        
      }
    }
    
    return sign * intervalValue;
  }
  
  /**
   * Get the number of nano seconds for the parameter timeunit
   * @param timeunit input time unit
   * @return number of nanoseconds for one of unit of given timeunit
   */
  public static long getDToSMultiplicationFactor(TimeUnit timeunit)
  {
    switch (timeunit)
    {
    case DAY:
      return DAYS * NANOSECONDS_MULTIPLICATION_FACTOR;
    case HOUR:
      return HOUR * NANOSECONDS_MULTIPLICATION_FACTOR;
    case MINUTE:
      return MINUTE * NANOSECONDS_MULTIPLICATION_FACTOR;
    case SECOND:
      return SECOND * NANOSECONDS_MULTIPLICATION_FACTOR;
    default:
      return 1l;
    }
  }
   
  
  /**
   * Convert & Return a String INTERVAL value to a numeric INTERVAL value 
   * @param value input String INTERVAL value
   * @param format format of input string
   * @return number of months as IntervalFormat is YEAR TO MONTH
   * @throws CEPException
   */
  public static Long parseYToMIntervalString(String input, 
                                             IntervalFormat format)
    throws CEPException
  {
    int sign = 1;
    char firstCharacter = input.charAt(0);
    String value = null;
    if(firstCharacter == '-')
    {
      sign = -1;
      value = input.substring(1);
    }
    else if(firstCharacter == '+')
    {
      sign = 1;
      value = input.substring(1);
    }
    else
      value = input;
    
    // Remove unwanted white space characters
    value = value.trim();
    
    long numMonths = 0;
    String delimiter = "-";
    TimeUnit leadingField = format.getLeadingField();
    TimeUnit trailingField = format.getTrailingField();
   
    if(value == null)
      return null;
    
    String[] fields = value.split(delimiter);
    
    // Validation Checks
    // 1. Number of fields in the YEAR TO MONTH shouldn't be more than TWO
    if(fields.length > 2)
    {
      LogUtil.fine(LoggerType.TRACE, 
        "Numbers of Fields in YEAR TO MONTH interval value exceeds 2.");
      throw new CEPException(DataStructuresError.INVALID_INTERVAL);
    }
    
    // 2. If TRAILING UNIT is not specified, then there must be ONE field
    if(trailingField == null && fields.length != 1)
    {
      LogUtil.fine(LoggerType.TRACE,
        "Trailing Unit is not specified in interval format; But Interval Value"
        + " has more than ONE field.");
      throw new CEPException(DataStructuresError.INVALID_INTERVAL);
    }
    
    // 3. If TRAILING UNIT is specified, then there must be TWO field
    if(trailingField != null && fields.length != 2)
    {
      LogUtil.fine(LoggerType.TRACE,
        "Trailing Unit is specified in interval format; But Interval Value"
        + " does not have exactly TWO field.");
      throw new CEPException(DataStructuresError.INVALID_INTERVAL);
    }
    
    if(trailingField != null)
    {
      validateData(value, DIGITS_DASH_DIGITS, true);      
      Long fieldValue = new Long(fields[1]);
      if(fieldValue > 11)
        throw new CEPException(DataStructuresError.NOT_A_VALID_MONTH);
      
      int multiplicationFactor = getYToMMultiplicationFactor(trailingField);
      numMonths = numMonths + fieldValue * multiplicationFactor;      
    }
    else
      validateData(value, NON_DIGITS, false);
    
    Long leadingFieldValue   = new Long(fields[0]);
    int multiplicationFactor = getYToMMultiplicationFactor(leadingField);
    numMonths = numMonths + leadingFieldValue * multiplicationFactor;
    
    // Validate the precision
    long numYears = numMonths / 12;
    int leadingPrecision = format.getLeadingPrecision();
    int maxAllowedIntervalValue = 1;
    for(int i=0; i < leadingPrecision; i++)
      maxAllowedIntervalValue = maxAllowedIntervalValue * 10;
    if(numYears >= maxAllowedIntervalValue)
      throw new CEPException(DataStructuresError.LEADING_PRECISION_TOO_SMALL);
    
    return sign * numMonths;
  }
    
  /**
   * Helper method to validate whether the interval value is specified as per
   * allowed patterns.
   * @param value input data value
   * @param allowedPattern pattern
   * @param isMatchRequired flag to specify whether pattern should be matching
   *                        or non-matching
   * @throws CEPException
   */
  private static void validateData(String value, Pattern allowedPattern, 
                                   boolean isMatchRequired)
    throws CEPException
  {
    Matcher matcher = allowedPattern.matcher(value);
    boolean found = matcher.find();
    boolean isValidData = !(isMatchRequired ^ found);
    if(!isValidData)
      throw new CEPException(DataStructuresError.INVALID_INTERVAL);
  }

  /**
   * Get the number of MONTHS for input time unit
   * @param timeunit
   * @return number of months
   */
  public static int getYToMMultiplicationFactor(TimeUnit timeunit)
  {
    switch (timeunit)
    {
    case YEAR:
      return 12;
    case MONTH:
      return 1;
    default:
      assert false;
    }
    return 1;
  }

  /**
   * Convert Numeric INTERVAL Value to String INTERVAL value on the basis of
   * given input format
   * @param value Numeric interval value
   * @return desired format of output string
   */
  public static String getYMInterval(long value, IntervalFormat format)
  {
    // format should be YEAR TO MONTH Interval format
    assert format.isYearToMonthInterval();
    
    int leadingPrecision = format.getLeadingPrecision();
    int trailingPrecision = format.getTrailingFractionalSeconds();
    
    StringBuffer interval = new StringBuffer();
    
    // Add Symbol + OR - on the basis of whether the interval is 
    // POSITIVE OR NEGATIVE    
    if(value < 0)
    {
      interval.append('-');      
      value = value * -1;
    }
    else
      interval.append('+');
    
    long numYears  = value / 12;
    long numMonths = value % 12;
    
    interval.append(getPreciseString(numYears, leadingPrecision, false));
    interval.append('-');
    if(numMonths < 10)
      interval.append('0');
    interval.append(numMonths);
    
    return interval.toString();
  }
  
  /**
   * Convert Numeric INTERVAL value to String INTERVAL value on the basis of
   * given input format
   * @param value Numeric INTERVAL value
   * @param format Desired output string format
   * @return
   */
  public static String getDSInterval(long value, IntervalFormat format)
  {
    // format should be DAY TO SECOND Interval format
    assert !format.isYearToMonthInterval();
    
    int leadingPrecision  = format.getLeadingPrecision();
    int trailingFractions = format.getTrailingFractionalSeconds();
    
    StringBuffer interval = new StringBuffer();
    
    // Add Symbol + OR - on the basis of whether the interval is 
    // POSITIVE OR NEGATIVE
    if(value < 0)
    {      
      interval.append('-');
      value = value * -1;
    }
    else
      interval.append('+');
    
    long numNanos = value % 1000000000l;
    
    value = value / 1000000000l;
    
    long numDays = value / DAYS;
    value = value - numDays * DAYS;
    
    long numHours = value / HOUR;
    value = value - numHours * HOUR;
    
    long numMinutes = value / MINUTE;
    value = value - numMinutes * MINUTE;
    
    long numSeconds = value;
    
    interval.append(getPreciseString(numDays, leadingPrecision, false));
    
    interval.append(' ');
    
    if(numHours < 10)
      interval.append('0');
    interval.append(numHours);
    
    interval.append(':');
    
    if(numMinutes < 10)
      interval.append('0');
    interval.append(numMinutes);
    
    interval.append(':');
    
    if(numSeconds < 10)
      interval.append('0');
    interval.append(numSeconds);
    
    interval.append('.');

    interval.append(getPreciseString(numNanos, trailingFractions, true));
    
    return interval.toString(); 
  }
  
  /**
   * Get a Precision Based String; 
   * @param lvalue
   * @param precision
   * @param isSuffix
   * @return
   */
  private static String getPreciseString(long lvalue, int precision, boolean isSuffix)
  {
    String value = Long.toString(lvalue);
    
    if(value.length() < precision)
    {
      // If number of digits in value is less than precision, then add the extra
      // digits either as suffix or prefix based on the flag
      StringBuffer sb = new StringBuffer();
      int digitsDifference = precision - value.length();
      if(isSuffix)
      {
        sb.append(value);
        for(int i = 0; i < digitsDifference; i++)
          sb.append('0');        
      }
      else
      {
        for(int i = 0; i < digitsDifference; i++)
          sb.append('0');
        sb.append(value);
      }
      return sb.toString();
    }
    else if(value.length() > precision)
    {
      // If number of digits in value is less than precision, then remove the
      // extra digits
      return value.substring(0, precision);
    }
    else
      return value;
  }

}
