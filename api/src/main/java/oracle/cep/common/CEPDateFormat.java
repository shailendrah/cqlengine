/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/CEPDateFormat.java /main/12 2013/10/08 11:09:54 sbishnoi Exp $ */

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
 sbishnoi    07/30/13 - bug 17180183
 mthatte     12/23/09 - bug-9234317
 hopark      12/01/09 - keep tz
 hopark      05/22/09 - add timezone for xsd format
 hopark      11/28/08 - add getDefaultFormat
 anasrini    11/19/08 - support xsd:dateTime format
 hopark      11/09/08 - set timezone to UTC
 mthatte     12/06/07 - cleanup
 mthatte     12/05/07 - order of date formats
 mthatte     12/03/07 - correcting formats
 mthatte     11/20/07 - Support for multiple date formats for Timestamps.
 mthatte     11/20/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/CEPDateFormat.java /main/12 2013/10/08 11:09:54 sbishnoi Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.common;

import java.text.ParseException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

public class CEPDateFormat 
{ 
  ArrayList<CEPSimpleDateFormat> dateFormats = new ArrayList<CEPSimpleDateFormat>();
  
  /** A Collection of datetime pattern which are registered with CQL Engine*/
  ArrayList<String> dateTimePatterns = new ArrayList<String>();
  
  /** A Map from pattern string to corresponding pattern object */
  HashMap<String, Pattern> dateTimePatternRegistry = new HashMap<String, Pattern>();
  
  /** A Map from Pattern string to corresponding date format object*/
  HashMap<String, List<SimpleDateFormat>> patternDateFormatMap 
    = new HashMap<String, List<SimpleDateFormat>>();
  
  /** Pattern to match the timezone abbreviations tags. e.g. GMT, IST, PST */
  private Pattern abbrTagMatchPattern   = Pattern.compile("\\D{3}");
  
  /** Pattern to match the custom timezone. eg.  GMT+0530, +0630 */
  private Pattern customTimeZonePattern 
    = Pattern.compile("(\\D{3}){0,1}(\\s|\\W){0,1}\\d{1,2}(:| ){0,1}(\\d{2}){0,1}");
  
  /** Pattern to match the 24hr clock values. e.g. 11:30, 1130, 0530 etc*/
  private Pattern _24HrClockMatchPattern 
    = Pattern.compile("([01]?[0-9]|2[0-3])(:){0,1}[0-5][0-9]");

  /** Default format for timestamp values */
  private SimpleDateFormat defaultFormat = null;
  
  /** Default format for timestamp values coming from database tables */
  private SimpleDateFormat defaultDBFormat = null;
  
  // Current Default Timezone set by ConfigMananger
  private TimeZone tz = null;
  
  private static CEPDateFormat s_instance = null;  
  private static Calendar formatCalendar = null;
  
  /**
   * Get Singleton Instance of CEPDateFormat
   */
  public static synchronized CEPDateFormat getInstance() 
  {
    if (s_instance == null)
      s_instance = new CEPDateFormat();
    return s_instance;
  }
  
  public synchronized Calendar getCalendar()
  {
    if(formatCalendar == null)
    {
      TimeZone currTimeZone = this.getDefaultTimeZone();
      formatCalendar = Calendar.getInstance(currTimeZone);
    }
    return formatCalendar;
  }
  

        
  private CEPDateFormat()
  {
    /**
     * Note: Below are addition of datetime format string into a list of formats.
     * The parsing of input string will be done by matching to below formats
     * one by one in the order of insertion into the list.
     * It is advisable to add format at such a position that no format above
     * the newly added one should match to string.
     */
    
    // Register pattern and add formats for following types of date time values:
    // // 11/21/2005 11:14:23.1111
    addPattern("\\d{2}/\\d{2}/\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}.\\d{4}");
    addFormat("\\d{2}/\\d{2}/\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}.\\d{4}", 
              "MM/dd/yyyy HH:mm:ss.SSSS");
    
    // 11/21/2005 11:14:23.111
    addPattern("\\d{2}/\\d{2}/\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}.\\d{3}");
    addFormat("\\d{2}/\\d{2}/\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}.\\d{3}", 
              "MM/dd/yyyy HH:mm:ss.SSS");
    
    // 11/21/2005 11:14:23.11
    addPattern("\\d{2}/\\d{2}/\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}.\\d{2}");
    addFormat("\\d{2}/\\d{2}/\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}.\\d{2}", 
              "MM/dd/yyyy HH:mm:ss.SS");
    
    // 11/21/2005 11:14:23.1
    addPattern("\\d{2}/\\d{2}/\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}.\\d{1}");
    addFormat("\\d{2}/\\d{2}/\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}.\\d{1}", 
              "MM/dd/yyyy HH:mm:ss.S");
    
    // Note: Initialize the default format if it is not set in the configuration
    // 11/21/2005 11:14:23
    if(defaultFormat == null)
    {
      addPattern("\\d{2}/\\d{2}/\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}");
      defaultFormat = addFormat("\\d{2}/\\d{2}/\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}", 
                                "MM/dd/yyyy HH:mm:ss");
    }
    else
      addFormat(defaultFormat.toPattern(), null, false);
    
    //// 11/21/2005 11:14
    addPattern("\\d{2}/\\d{2}/\\d{4}\\s\\d{1,2}:\\d{1,2}");
    addFormat("\\d{2}/\\d{2}/\\d{4}\\s\\d{1,2}:\\d{1,2}", 
              "MM/dd/yyyy HH:mm");
    
    // 11/21/2005 11:14
    addPattern("\\d{2}/\\d{2}/\\d{4}\\s\\d{1,2}");
    addFormat("\\d{2}/\\d{2}/\\d{4}\\s\\d{1,2}", 
              "MM/dd/yyyy HH");
   
    // 11/21/2005
    addPattern("\\d{2}/\\d{2}/\\d{4}");
    addFormat("\\d{2}/\\d{2}/\\d{4}", "MM/dd/yyyy"); 
    
    // 11-21-2005 11:14:23.1111
    addPattern("\\d{2}-\\d{2}-\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}.\\d{4}");
    addFormat("\\d{2}-\\d{2}-\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}.\\d{4}", 
              "MM-dd-yyyy HH:mm:ss.SSSS");
    
    // 11-21-2005 11:14:23.111
    addPattern("\\d{2}-\\d{2}-\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}.\\d{3}");
    addFormat("\\d{2}-\\d{2}-\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}.\\d{3}",
              "MM-dd-yyyy HH:mm:ss.SSS");
    
    // 11-21-2005 11:14:23.11
    addPattern("\\d{2}-\\d{2}-\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}.\\d{2}");
    addFormat("\\d{2}-\\d{2}-\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}.\\d{2}", 
              "MM-dd-yyyy HH:mm:ss.SS");
    
    // 11-21-2005 11:14:23.1
    addPattern("\\d{2}-\\d{2}-\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}.\\d{1}");
    addFormat("\\d{2}-\\d{2}-\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}.\\d{1}",
              "MM-dd-yyyy HH:mm:ss.S");
    
    // 11-21-2005 11:14:23
    addPattern("\\d{2}-\\d{2}-\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}");
    addFormat("\\d{2}-\\d{2}-\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}", 
              "MM-dd-yyyy HH:mm:ss");
    
    //11-21-2005 11:14
    addPattern("\\d{2}-\\d{2}-\\d{4}\\s\\d{1,2}:\\d{1,2}");
    addFormat("\\d{2}-\\d{2}-\\d{4}\\s\\d{1,2}:\\d{1,2}", 
              "MM-dd-yyyy HH:mm");
    
    // 11-21-2005 11
    addPattern("\\d{2}-\\d{2}-\\d{4}\\s\\d{1,2}");
    addFormat("\\d{2}-\\d{2}-\\d{4}\\s\\d{1,2}", 
              "MM-dd-yyyy HH");
    
    // 11-21-2005
    addPattern("\\d{2}-\\d{2}-\\d{4}");
    addFormat("\\d{2}-\\d{2}-\\d{4}", 
              "MM-dd-yyyy");
    
    // Note: This format allows timestamp values with atleast one digit as fractional second part
    // 11-Jan-99 11.14.23.111111 AM
    defaultDBFormat 
      = addFormat("dd-MMM-yy hh.mm.ss.SSSSSS a", 
                  "\\d{2}-\\D{3}-\\d{2}\\s\\d{2}(\\.){1}\\d{2}(\\.){1}\\d{2}(\\.){1}\\d{0,6}+\\s\\D{2}", 
                  false);
    ((CEPSimpleDateFormat)defaultDBFormat).setDefaultDBFormat(true);
    
    addPattern("\\d{2}-\\D{3}-\\d{2}\\s\\d{2}(\\.){1}\\d{2}(\\.){1}\\d{2}(\\.){1}\\d{0,6}+\\s((AM)|(PM)|(A.M.)|(P.M.)|(am)|(pm)|(a.m.)|(p.m.))");
    addFormat("\\d{2}-\\D{3}-\\d{2}\\s\\d{2}(\\.){1}\\d{2}(\\.){1}\\d{2}(\\.){1}\\d{0,6}+\\s((AM)|(PM)|(A.M.)|(P.M.)|(am)|(pm)|(a.m.)|(p.m.))", 
              "dd-MMM-yy hh.mm.ss.SSSSSS a"); 

    // 11-Jan-99 11.14.23.1111
    addPattern("\\d{2}-\\D{3}-\\d{2}\\s\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{4}");
    addFormat("\\d{2}-\\D{3}-\\d{2}\\s\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{4}", 
              "dd-MMM-yy hh.mm.ss.SSSS");
    
    // 15-DEC-01 11.14.14.111
    addPattern("\\d{2}-\\D{3}-\\d{2}\\s\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{3}");
    addFormat("\\d{2}-\\D{3}-\\d{2}\\s\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{3}", 
              "dd-MMM-yy hh.mm.ss.SSS");
    
    // 15-DEC-01 11.14.14.11
    addPattern("\\d{2}-\\D{3}-\\d{2}\\s\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{2}");
    addFormat("\\d{2}-\\D{3}-\\d{2}\\s\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{2}", 
              "dd-MMM-yy hh.mm.ss.SS");
    
    // 15-DEC-01 11.14.14.1
    addPattern("\\d{2}-\\D{3}-\\d{2}\\s\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{1}");
    addFormat("\\d{2}-\\D{3}-\\d{2}\\s\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{1}", 
              "dd-MMM-yy hh.mm.ss.S");
    
    // 15-DEC-01 11.14.14
    addPattern("\\d{2}-\\D{3}-\\d{2}\\s\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{1,2}");
    addFormat("\\d{2}-\\D{3}-\\d{2}\\s\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{1,2}", 
              "dd-MMM-yy hh.mm.ss");
    
    // 15-DEC-01 11.14
    addPattern("\\d{2}-\\D{3}-\\d{2}\\s\\d{1,2}(\\.){1}\\d{1,2}");
    addFormat("\\d{2}-\\D{3}-\\d{2}\\s\\d{1,2}(\\.){1}\\d{1,2}", 
              "dd-MMM-yy hh.mm");
    
    // 15-DEC-01 11
    addPattern("\\d{2}-\\D{3}-\\d{2}\\s\\d{1,2}");
    addFormat("\\d{2}-\\D{3}-\\d{2}\\s\\d{1,2}", "dd-MMM-yy hh");
    
    // 15-DEC-01
    addPattern("\\d{2}-\\D{3}-\\d{2}");
    addFormat("\\d{2}-\\D{3}-\\d{2}", "dd-MMM-yy");
    
    // 15/DEC/01
    addPattern("\\d{2}/\\D{3}/\\d{2}");
    addFormat("\\d{2}/\\D{3}/\\d{2}", "dd/MMM/yy");

    //2013-10-5 15:16:0.756000 +5:30
    //2013-10-5 15:16:0.756000
    addPattern("\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}(\\:){1}\\d{1,2}(\\:){1}\\d{1,2}(\\.){1}\\d{0,6}");
    addFormat("\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}(\\:){1}\\d{1,2}(\\:){1}\\d{1,2}(\\.){1}\\d{0,6}", "yyyy-MM-dd HH:mm:ss.SSSSSS");
    
    //2013-10-5 15.16.0.756000 +5:30
    //2013-10-5 15.16.0.756000
    addPattern("\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{0,6}");
    addFormat("\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{0,6}", "yyyy-MM-dd HH.mm.ss.SSSSSS");
    
    //2013-10-5 15:16:0 +5:30
    //2013-10-5 15:16:0
    addPattern("\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}(\\:){1}\\d{1,2}(\\:){1}\\d{1,2}");
    addFormat("\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}(\\:){1}\\d{1,2}(\\:){1}\\d{1,2}", "yyyy-MM-dd HH:mm:ss");
    
    //2013-10-5 15.16.0 +5:30
    //2013-10-5 15.16.0
    addPattern("\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{1,2}");
    addFormat("\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}(\\.){1}\\d{1,2}(\\.){1}\\d{1,2}", "yyyy-MM-dd HH.mm.ss");

    //2013-10-5 15:16 +5:30
    //2013-10-5 15:16
    addPattern("\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}(\\:){1}\\d{1,2}");
    addFormat("\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}(\\:){1}\\d{1,2}", "yyyy-MM-dd HH:mm");
    
    //2013-10-5 15.16 +5:30
    //2013-10-5 15.16
    addPattern("\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}(\\.){1}\\d{1,2}");
    addFormat("\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}(\\.){1}\\d{1,2}", "yyyy-MM-dd HH.mm");

    //2013-10-5 15 +5:30
    //2013-10-5 15
    addPattern("\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}");
    addFormat("\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}", "yyyy-MM-dd HH");
    
    // 2012-11-10
    addPattern("\\d{4}-\\d{2}-\\d{2}");
    addFormat("\\d{4}-\\d{2}-\\d{2}", "yyyy-MM-dd");
    
    // 11:14:14 PST
    addPattern("\\d{1,2}:\\d{1,2}:\\d{1,2}");
    addFormat("\\d{1,2}:\\d{1,2}:\\d{1,2}", "HH:mm:ss");
    
   
    
    // xsd:dateTime formats
    addFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS");
    addFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSSZ");
    addFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSSz");
    addFormat("yyyy-MM-dd'T'HH:mm:ss");
    addFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    addFormat("yyyy-MM-dd'T'HH:mm:ssz");
  }

  /**
   * Method to load a pattern in existing pattern registry
   * @param regEx
   * @return Returns the regular expression string
   */
  private String addPattern(String regEx)
  {
    dateTimePatterns.add(regEx);
    dateTimePatternRegistry.put(regEx, Pattern.compile(regEx));
    return regEx;
  }
  
  /**
   * Method to create and add a format into pattern-format map
   * @param pattern
   * @param format
   * @return
   */
  private SimpleDateFormat addFormat(String pattern, String format)
  {
    List<SimpleDateFormat> formatList = patternDateFormatMap.get(pattern);
    if(formatList == null)
    {
      formatList = new ArrayList<SimpleDateFormat>();
      patternDateFormatMap.put(pattern, formatList);
    }
    SimpleDateFormat dateFormat = new SimpleDateFormat(format);
    dateFormat.setLenient(true);
    formatList.add(dateFormat);    
    return dateFormat;
  }
  
  /**
   * Add standard formats into a list of formats
   * @param format
   * @return
   */
  private SimpleDateFormat addFormat(String format)  
  {    
    return addFormat(format, null, false);
  }
  
  /**
   * Add standard Format into a list of standard formats which will be mainly
   * used to collect formats for xml values. 
   * @param format
   * @param pattern
   * @param isPatternRegistered
   * @return
   */
  private SimpleDateFormat addFormat(String format, String pattern, 
                                     boolean isPatternRegistered)
  {    
    CEPSimpleDateFormat df 
      = new CEPSimpleDateFormat(format, pattern, isPatternRegistered);
    df.setLenient(true);
    dateFormats.add(df);
    return df;
  }

  /**
   * Set the default format for timestamp values
   * @param df
   */
  public synchronized void setDefaultFormat(SimpleDateFormat df)
  {
    defaultFormat = df;    
  }

  /**
   * Set the default format for database timestamp values
   * @return
   */
  public synchronized SimpleDateFormat getDefaultDBFormat() 
  {
    return defaultDBFormat;
  }
  
  /**
   * @return the defaultFormat
   */
  public SimpleDateFormat getDefaultFormat()
  {
    return defaultFormat;
  }

  public synchronized TimeZone getDefaultTimeZone() 
  {
    if(tz == null)
    {
      tz = TimeZone.getDefault();
    }
    return tz;
  }
  
  public synchronized void setDefaultTimeZone(TimeZone tz)
  {
    this.tz = tz;
    for (SimpleDateFormat df : dateFormats)
    {
      df.setTimeZone(tz);
    }
  }
  
  
  /**
   * Parse the given date string using default available formats
   * @param dateString input date/timestamp value
   * @return CEPDate object
   * @throws ParseException
   */
  public CEPDate parse(String dateString) throws ParseException
  {
    return parse(dateString, null, null);
  }
  
  /**
   * Parse the given date string
   * @param dateString
   * @param tsFormat
   * @return
   * @throws ParseException
   */
  public CEPDate parse(String dateString, TimestampFormat tsFormat)
    throws ParseException
  {
    return parse(dateString, null, tsFormat); 
  }
  
  /**
   * Parse the given date string
   * @param dateString
   * @param formatString
   * @return
   * @throws ParseException
   */
  public CEPDate parse(String dateString, String formatString) 
    throws ParseException
  {
    return parse(dateString, formatString, null);
  }
  
  /**
   * Parse the given date/timestamp string value using given format string
   * @param dateString input date/timestamp value
   * @param formatString format of date/timestamp value
   * @return
   * @throws ParseException
   */
  public synchronized CEPDate parse(String dateString, 
                                    String formatString,
                                    TimestampFormat tsFormat) 
    throws ParseException
  {
    CEPDate outVal   = null;
    
    if(formatString == null)
    {
      // In the case if there is no format mentioned, then parse the string
      // as follows:
      // Case-1. Match the string with default standard formats
      // Case-2. If the string doesn't match standard formats,
      //    then Match the string with a pattern and then parse the value with
      //         the associated format.
      
      // Case-1
      outVal = parseWithStandardSDF(dateString, tsFormat);
          
      if(outVal == null)
        outVal = parseWithPatternBasedSDF(dateString, tsFormat);
    }
    else
    {
      // In the case if there is format mentioned, then parse the string with
      // user provided format
      outVal = parseWithCustomSDF(dateString, tsFormat, formatString);
    }
    
    // Throw ParseException if the value is not parsed
    if(dateString != null && !dateString.equals("") && outVal == null)
    {
      String errorMsgFormatName 
        = formatString == null ? "default CQL timestamp formats." : formatString;
      
      throw new ParseException("Failed to parse the timestamp value \"" +
        dateString + "\" with " + errorMsgFormatName, 0);
    }
    else
      return outVal;
  }
  
  /**
   * Parse the parameter dateString value with parameter tsFormat
   * @param dateString input timestamp value
   * @param tsFormat format with which you want to parse the value
   * @return
   */
  private CEPDate parseWithStandardSDF(String dateString, 
                                       TimestampFormat tsFormat)
  {
    /** Return CEPDate Value */
    CEPDate outVal           = null;
    TimeZone parsingTimeZone = null;
    
    if(tsFormat != null && tsFormat.isLocalTimeZone())
    {
      // Case-1: Parse TIMESTAMP with local timezone
      parsingTimeZone = TimeZone.getDefault();            
      String logMsg = "The datatype of parsed attribute value is timestamp with"
       + " local timezone. Before parsing the timestamp value, setting the" +
       " Parser's  timezone to " + parsingTimeZone.getDisplayName(); 
      LogUtil.finest(LoggerType.TRACE, logMsg);
    }
    else
    {
      parsingTimeZone = getDefaultTimeZone();
      LogUtil.finest(LoggerType.TRACE, 
          "There is no timezone specified in the the timestamp value, " +
          "Setting the Parser's timezone to default timezone of server " + 
          parsingTimeZone.getDisplayName());
    }
    
    for (CEPSimpleDateFormat sdf : dateFormats) 
    {
      try 
      {
        sdf.setTimeZone(parsingTimeZone);
        outVal = parseDateTimeString(dateString, sdf, tsFormat, parsingTimeZone, 
                                     false);
        
        if(sdf.getPattern() != null && !sdf.isPatternRegistered)
        {
          Matcher m = sdf.getPattern().matcher(dateString);
          int startIndex = -1;
          int endIndex = -1;
          while(m.find())
          {
            startIndex = m.start();
            endIndex = m.end();
          }
          boolean isMatched = startIndex == 0 && endIndex != -1 &&
            endIndex - startIndex == dateString.length();
          
          if(!isMatched)
          {
            throw new ParseException("Given match is not a maximum match", 0);
          }
        }

        LogUtil.finest(LoggerType.TRACE, "Successfully parsed timestamp " 
          + dateString + " using pattern " + sdf.toPattern());
        
        return outVal;         
      }        
      catch (Exception e) 
      {
        LogUtil.finest(LoggerType.TRACE, "Failed to parse timestamp " + dateString
          + " using pattern " + sdf.toPattern());
      }      
    }
    return null;
  }
  
  /**
   * Parse the param timestamp value with pattern based date formats
   * @param dateString input timestamp values
   * @param tsFormat timestamp format
   * @return
   */
  private CEPDate parseWithPatternBasedSDF(String dateString, 
                                           TimestampFormat tsFormat)
  {
    CEPDate outVal = null;
    boolean isUserSpecifiedTimeZone = false;
    Iterator<String> patternIter = dateTimePatterns.iterator();
    while(patternIter.hasNext())
    {
      String pattern = patternIter.next();
      Pattern currPattern = dateTimePatternRegistry.get(pattern);
      Matcher matcher = currPattern.matcher(dateString);
      int startIndex = -1;
      int endIndex = -1;
      while(matcher.find())
      {
        startIndex = matcher.start();
        endIndex   = matcher.end();
      }
      boolean isMatchFound = startIndex == 0 && endIndex != -1;
      if(isMatchFound)
      {
        LogUtil.finest(LoggerType.TRACE, 
                     "Input timestamp value matches to pattern: " + pattern);
       
        // i) Extract TimeZone information (if any)
        // ii) Get the dateformats for given pattern string
        // iii) Try to parse using each format

        String timeZoneStr = dateString.substring(endIndex).trim();
        String dateStringWithoutTimeZone = dateString.substring(0, endIndex).trim();
        TimeZone parsingTimeZone = null;
        //Note: Following are the three cases of parsing timestamp values:
        //1. Parse TIMESTAMP with local timezone
        //2. Parse TIMESTAMP with timezone specified in timestamp value.
        //3. Parse TIMESTAMP with default timezone (default and local can differ.)
        if(tsFormat != null && tsFormat.isLocalTimeZone())
        {
          // Case-1: Parse TIMESTAMP with local timezone
          parsingTimeZone = TimeZone.getDefault();            
          String logMsg = "The datatype of parsed attribute value is timestamp with"
           + " local timezone. Before parsing the timestamp value, setting the" +
           " Parser's  timezone to " + parsingTimeZone.getDisplayName(); 
          LogUtil.finest(LoggerType.TRACE, logMsg);
        }
        else
        {
          // Case-2: Parse TIMESTAMP with timezone specified in timestamp value
          if(timeZoneStr != null && timeZoneStr.length() > 0)
          {
            parsingTimeZone = validateAndGetTimeZone(timeZoneStr);
          }
          // Case-3: Parse TIMESTAMP with default timezone
          if(parsingTimeZone == null)      
          {
            parsingTimeZone = getDefaultTimeZone();
            LogUtil.finest(LoggerType.TRACE, 
                "There is no timezone specified in the the timestamp value, " +
                "Setting the Parser's timezone to default timezone of server " + 
                parsingTimeZone.getDisplayName());
          }
          else
            isUserSpecifiedTimeZone = true;
        }
        
        List<SimpleDateFormat> formatList = patternDateFormatMap.get(pattern);
        Iterator<SimpleDateFormat> formatIter = formatList.iterator();
        boolean done = false;          
        while(formatIter.hasNext() && !done)
        {
          SimpleDateFormat format = formatIter.next();
          format.setTimeZone(parsingTimeZone);
          try 
          {
            outVal = parseDateTimeString(dateStringWithoutTimeZone, 
                                         format, 
                                         tsFormat, 
                                         parsingTimeZone, 
                                         isUserSpecifiedTimeZone);
          } 
          catch (ParseException e) {
            LogUtil.finest(LoggerType.TRACE, "Failed to parse timestamp value " +
              dateString + " with format " + format.toPattern());
          }
          done = outVal != null;
        }
        return outVal;
      } 
    }
    return null;
  }
  
  /**
   * Parse the given timestamp value with custom format string
   * @param dateString
   * @param tsFormat
   * @param formatString
   * @return
   */
  private CEPDate parseWithCustomSDF(String dateString, 
                                     TimestampFormat tsFormat, 
                                     String formatString)
  {
    CEPDate outVal = null;
    SimpleDateFormat dFormat = null;
    TimeZone parsingTimeZone = null;
    
    // A flag to check whether the parsed timestamp value has timezone information
    boolean isUserSpecifiedTimeZone = false;
    if(tsFormat != null && tsFormat.isLocalTimeZone())
    {
      // Case-1: Parse TIMESTAMP with local timezone
      parsingTimeZone = TimeZone.getDefault();            
      String logMsg = "The datatype of parsed attribute value is timestamp with"
       + " local timezone. Before parsing the timestamp value, setting the" +
       " Parser's  timezone to " + parsingTimeZone.getDisplayName(); 
      LogUtil.finest(LoggerType.TRACE, logMsg);
    }
    else
    {
      parsingTimeZone = getDefaultTimeZone();
      LogUtil.finest(LoggerType.TRACE, 
          "There is no timezone specified in the the timestamp value, " +
          "Setting the Parser's timezone to default timezone of server " + 
          parsingTimeZone.getDisplayName());
    }
    try
    {
      dFormat = new SimpleDateFormat(formatString);
      dFormat.setTimeZone(parsingTimeZone);
      dFormat.setLenient(true);
    }
    catch(Exception e)
    {
      LogUtil.finest(LoggerType.TRACE, "Can't instantiate datetime parser as the " +
        "format " + formatString + " is not valid.");
      return null;
    }
    
    try 
    {
      outVal = parseDateTimeString(dateString, dFormat, tsFormat, 
                                   parsingTimeZone, isUserSpecifiedTimeZone);
    }
    catch (ParseException e) 
    {
      LogUtil.finest(LoggerType.TRACE, "Failed to parse timestamp value " +
          dateString + " with format " + formatString);
      return null;
    }    
    return outVal;
  }

  
  /**
   * Helper method to parse the given datetime string according to given format
   * string.
   * @param dataString input date time string
   * @param dateFormat format of date/time string
   * @param tsFormat timestamp format object
   * @param timezone timezone for the formatter object
   * @param isUserSpecifiedTimeZone flag to check whether timezone is specified 
   * by user in timestamp value.
   * @return
   * @throws ParseException 
   */
  private synchronized CEPDate parseDateTimeString(String dataString, 
                                                   SimpleDateFormat dateFormat,
                                                   TimestampFormat tsFormat,
                                                   TimeZone timezone,
                                                boolean isUserSpecifiedTimeZone) 
    throws ParseException
  {
    // parse the given input string
    Date dateVal;
     
    LogUtil.finest(LoggerType.TRACE, 
                 "Attempting to parse the timestamp value " + dataString + 
                 " using format: " + dateFormat.toPattern());
    
    // Parse the date string
    dateVal = dateFormat.parse(dataString);
    
    LogUtil.finest(LoggerType.TRACE, 
        "Successfully parsed the timestamp value " + dataString + 
        " using format: " + dateFormat.toPattern());
    
    // Calculate saved timezone
    if(tsFormat == null)
    {
      tsFormat = new TimestampFormat();    
    }
    else
    {
      tsFormat = tsFormat.clone();
    }
    
    // Set the parsed timestamp value's timezone
    tsFormat.setTimeZone(timezone);
    tsFormat.setUserSpecifiedTimeZone(isUserSpecifiedTimeZone);
    
    // Calculate number of nanoseconds as dateVal is 
    // number of milliseconds from epoch.
    long numNanos = dateVal.getTime() * 1000000l;   
    
    tsFormat.setDateFormat(dateFormat);
    LogUtil.finest(LoggerType.TRACE, "Initialized timestamp value and set the" +
    		" timezone to " + tsFormat.getTimeZone().getDisplayName());
    
    CEPDate outVal = new CEPDate(numNanos, tsFormat);
    
    return outVal;
  }

  /**
   * Validate and Get the java.util.TimeZone object for input timezone string
   * TimeZone string can be specified in one of the following formats:
   * 1. TimeZone ID:               America/Sau_Paulo, America/Los_Angeles etc
   * 2. Custom TimeZone ID:        +0530, -0700 etc
   * 3. Three-Letter Timezone IDs: IST, PST etc  
   * @param timeZone
   * @return
   */
  private TimeZone validateAndGetTimeZone(String timeZone)
  {
    timeZone = timeZone.trim();
    //System.out.println(timeZone);
    // check if the timezone is a three letter timezone 
    boolean isThreeLetterTimeZone = false;
      
    Matcher m = abbrTagMatchPattern.matcher(timeZone);
    int abbrTagStart = -1;
    int abbrTagEnd   = -1;

    while(m.find())
    {
      abbrTagStart = m.start();
      abbrTagEnd = m.end();
    }
    
    // Check if it is three letter timezone 
    isThreeLetterTimeZone = abbrTagStart == 0 && 
                            abbrTagEnd == 3   && 
                            abbrTagEnd - abbrTagStart == timeZone.length();
      
    if(isThreeLetterTimeZone)
    {
      TimeZone returnVal = TimeZone.getTimeZone(timeZone);
      if(returnVal.getDisplayName().equalsIgnoreCase("Greenwich Mean Time") &&
         !timeZone.equalsIgnoreCase("GMT"))
      {
        LogUtil.fine(LoggerType.TRACE, "Invalid Timezone Abbreviation " + timeZone);
      }
      LogUtil.finest(LoggerType.TRACE, 
                   "Before parsing, setting the three-letter timezone to " 
                   + returnVal.getDisplayName());
      
      return returnVal; 
    }
    
    Matcher customTimeZoneMatcher = customTimeZonePattern.matcher(timeZone);
    int startCustomTimeZone = -1;
    int endCustomTimeZone = -1;
    while(customTimeZoneMatcher.find())
    {
      startCustomTimeZone = customTimeZoneMatcher.start();
      endCustomTimeZone   = customTimeZoneMatcher.end();
    }
    
    boolean isCustomTimeZoneMatch 
      = startCustomTimeZone != -1 &&
        endCustomTimeZone != -1 &&
        endCustomTimeZone - startCustomTimeZone == timeZone.length();
        
    if(isCustomTimeZoneMatch)
    {
      return validateAndGetCustomTimeZone(timeZone, abbrTagStart, abbrTagEnd);
    }
    
    TimeZone returnVal = TimeZone.getTimeZone(timeZone);
    
    LogUtil.finest(LoggerType.TRACE, 
                 "Before parsing, setting the timezone to " 
                 + returnVal.getDisplayName());
    return returnVal;
  }
  
  /**
   * Parse and return the custom timezone
   *  As per JAVA SDK, Custom TimeZone can be defined as follows:
       CustomID:
        GMT Sign Hours : Minutes
        GMT Sign Hours Minutes
        GMT Sign Hours
        Sign: one of
          + -
        Hours:
              Digit
              Digit Digit
        Minutes:
              Digit Digit
        Digit: one of
              0 1 2 3 4 5 6 7 8 9
     
   * @param timeZone
   * @return
   */
  private TimeZone validateAndGetCustomTimeZone(String timeZone, int abbrTagStart, int abbrTagEnd)
  {
    boolean hasGMTTag = abbrTagStart == 0 && abbrTagEnd == 3;      
    String startGMTTag = hasGMTTag ? timeZone.substring(0,abbrTagEnd) : null;
    String sign = null;
    
    if(hasGMTTag && !startGMTTag.equals("GMT"))
    {
      LogUtil.fine(LoggerType.TRACE, 
        "Invalid Timezone Abbreviation " + startGMTTag + ". Please use GMT.");
    }
    
    if(hasGMTTag)
      timeZone = timeZone.substring(abbrTagEnd);
    
    if(timeZone.startsWith("+") || timeZone.startsWith("-"))
    {
      sign = timeZone.substring(0,1);
      timeZone = timeZone.substring(1);
    }
    else
      sign = "+";
    
    Matcher clockMatcher = _24HrClockMatchPattern.matcher(timeZone);
    int startIndex = -1;
    int endIndex = -1;
    boolean isClockMatched = false;
    while(clockMatcher.find())
    {
      startIndex = clockMatcher.start();
      endIndex = clockMatcher.end();
    }
    
    isClockMatched = startIndex != -1 && 
                     endIndex   != -1 && 
                     (endIndex-startIndex == timeZone.length());
    
    if(!isClockMatched)
    {
      LogUtil.fine(LoggerType.TRACE, "Timezone value should match to 24hr clock");
    }
    
    String finalTimeZoneStr = "GMT" + sign + timeZone;
    LogUtil.finest(LoggerType.TRACE, "Setting custom timezone to " 
      + finalTimeZoneStr);
    TimeZone returnVal = TimeZone.getTimeZone(finalTimeZoneStr);
    return returnVal;
  }
  
  /**
   * Format the java.util.Date object to String Object on the basis of given
   * format
   * @param value timestamp value in number of nanoseconds 
   * @param tsFormat timestamp format
   * @return
   */
  public synchronized String format(long tsValue, TimestampFormat tsFormat) 
  {
    String formattedStr = null;
    
    if(tsFormat != null)
    {      
      DateFormat dateFormat = tsFormat.getDateFormat();
      
      if(dateFormat == null)
        dateFormat = defaultFormat;
      
      // Keep the existing timezone of the date format
      TimeZone resetTzValue = dateFormat.getTimeZone();
      
      TimeZone currTimeZone = tsFormat.getTimeZone();
      if(currTimeZone == null)
      {
        // Get the timezone value
        currTimeZone= getDefaultTimeZone();  
      }
      // Set the desired format for the timetsamp value
      dateFormat.setTimeZone(currTimeZone);
      
      // Format the input timestamp val (Divide by 10^6 to convert into millis)
      formattedStr = dateFormat.format(new Date(tsValue/1000000l));
      
      dateFormat.setTimeZone(resetTzValue);
    }
    else
    {
      // Format the input timestamp val (Divide by 10^6 to convert into millis)
      formattedStr = format(tsValue);
    }
    return formattedStr;
  }
  
  /**
   * Format the java.util.Date object to String Object on the basis of given
   * format
   * @param value timestamp value in number of nanoseconds 
   * @param destFormat destination format
   * @return
   */
  public synchronized String format(long tsValue, String destFormat) 
      throws ParseException
  {
    return format(tsValue, destFormat, null);
  }
  
  /**
   * Format the java.util.Date object to String Object on the basis of given
   * format
   * @param value timestamp value in number of nanoseconds 
   * @param destFormat destination format
   * @param timeZone timezone target timezone
   * @return
   */
  public synchronized String format(long tsValue, String destFormat, 
    TimeZone timezone) throws ParseException
  {
    String formattedStr = null;
    
    if(destFormat != null)
    {     
      formattedStr = processFormatElements(tsValue, destFormat, timezone);
      if(formattedStr != null)
      {
        return formattedStr;
      }
      SimpleDateFormat dateFormat = null;
      try
      {
        dateFormat = new SimpleDateFormat(destFormat);
      }
      catch(IllegalArgumentException e)
      {
        throw new ParseException("Invalid date/time format", 0);
      }
            
      // Get the timezone value
      TimeZone currTimeZone = 
        timezone == null ? getDefaultTimeZone() : timezone;  
      
      // Set the desired format for the timetsamp value
      dateFormat.setTimeZone(currTimeZone);
      
      // Format the input timestamp val (Divide by 10^6 to convert into millis)      
      formattedStr = dateFormat.format(new Date(tsValue / 1000000l));      
    }
    else
    {
      // Format the input timestamp val (Divide by 10^6 to convert into millis)
      formattedStr = format(tsValue);
    }
    return formattedStr;
  }
  
  private String processFormatElements(long tsValue, String destFormat, TimeZone timezone)
  {
    Calendar cal = getCalendar();
    if(timezone != null)
      cal.setTimeZone(timezone);
    cal.setTimeInMillis(tsValue/1000000l);
    
    if(destFormat.equalsIgnoreCase("DAY")) // Calculate NAME OF DAY
    {
      int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
      switch(dayOfWeek)
      {
      case Calendar.SUNDAY:
        return "Sunday";
      case Calendar.MONDAY:
        return "Monday";
      case Calendar.TUESDAY:
        return "Tuesday";
      case Calendar.WEDNESDAY:
        return "Wednesday";
      case Calendar.THURSDAY:
        return "Thursday";
      case Calendar.FRIDAY:
        return "Friday";
      case Calendar.SATURDAY:
        return "Saturday";
      }
    }
    else if(destFormat.equalsIgnoreCase("D")) // Calculate DAY OF WEEK
    {
      return Integer.toString(cal.get(Calendar.DAY_OF_WEEK));
    }
    else if(destFormat.equalsIgnoreCase("DDD")) // Calculate DAY OF YEAR
    {
      return Integer.toString(cal.get(Calendar.DAY_OF_YEAR));
    }
    else if((destFormat.equalsIgnoreCase("IW"))) // Calculate WEEK OF YEAR
    {
      return Integer.toString(cal.get(Calendar.WEEK_OF_YEAR));
    }
    return null;
  }

  public synchronized String format(long tsValue)
  {
    // Format the input timestamp val (Divide by 10^6 to convert into millis)
    String formattedStr = defaultFormat.format(new Date(tsValue / 1000000l));
    return formattedStr;
  }
  
}
