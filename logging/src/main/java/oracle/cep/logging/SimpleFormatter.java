/* $Header: SimpleFormatter.java 01-aug-2007.15:45:19 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    This class is to provide a single line formatter which is similar to
    System.out.println

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      08/01/07 - suppress nullpointerexception
    hopark      05/11/07 - Creation
 */

/**
 *  @version $Header: SimpleFormatter.java 01-aug-2007.15:45:19 hopark Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logging;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * MyCustomFormatter formats the LogRecord as follows:
 * date   level   localized message with parameters
 */
public class SimpleFormatter extends Formatter
{
  private static SimpleDateFormat s_timeFormat = new SimpleDateFormat("MM/dd HH:mm:ss");
  public SimpleFormatter()
  {
    super();
  }

  public String format(LogRecord record)
  {
    StringBuffer sb = new StringBuffer();

    // Get the date from the LogRecord and add it to the buffer
    Date date = new Date(record.getMillis());
    sb.append(s_timeFormat.format(date));
    sb.append(" ");

    // Get the level name and add it to the buffer
    if (record.getLevel().intValue() < Level.INFO.intValue())
    {
      sb.append(record.getLevel().getName());
      sb.append(" ");
    }
    // Get the formatted message (includes localization
    // and substitution of paramters) and add it to the buffer
    record.setResourceBundle(null); //remove localization bundle
    sb.append(formatMessage(record));
    sb.append("\n");
    return sb.toString();
  }
}
