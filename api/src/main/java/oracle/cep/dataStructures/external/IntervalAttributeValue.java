/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/IntervalAttributeValue.java /main/11 2013/10/09 08:04:48 sbishnoi Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    10/09/13 - bug 17532889
    pkali       11/30/12 - changed intervalValue to primitive type (bug
                           14842727)
    sbishnoi    07/16/11 - adding interval day to second functionalities
    hopark      10/30/09 - add attrib name in toString
    hopark      10/15/08 - refactoring
    hopark      09/04/08 - fix TupleValue clone
    hopark      08/22/08 - fix externalization
    parujain    12/06/07 - fix-interval
    najain      03/12/07 - bug fix
    hopark      03/01/07 - fix coverage by using NoSuchElementException
    najain      10/29/06 - add toString
    parujain    10/06/06 - Interval datatype
    parujain    10/06/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/IntervalAttributeValue.java /main/11 2013/10/09 08:04:48 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.dataStructures.external;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalConverter;
import oracle.cep.common.IntervalFormat;
import oracle.cep.exceptions.CEPException;

public class IntervalAttributeValue extends AttributeValue
{
  static final long serialVersionUID = -7060252621184862578L;
	
  private String interval;
  
  private IntervalFormat format;
  
  private long intervalValue;

  /**
   * Empty Argument Constructor
   */
  public IntervalAttributeValue()
  {
    super(Datatype.INTERVAL);
    setBNull(true);
  }
  
  public IntervalAttributeValue(String attributeName)
  {
    super(attributeName, Datatype.INTERVAL);
    setBNull(true);
  }

  public IntervalAttributeValue(String attributeName, String value) 
    throws CEPException
  {
    super(attributeName, Datatype.INTERVAL);
    this.interval = value;
  }

  public IntervalAttributeValue(IntervalAttributeValue other)
  {
    super(other);
    interval = other.interval;
    format   = other.format;
    intervalValue = other.intervalValue;   
  }
  
  public IntervalAttributeValue(String attributeName, long intervalValue, IntervalFormat fmt)
  {
    super(attributeName, Datatype.INTERVAL);
    this.intervalValue = intervalValue;
    this.format = fmt;
    this.interval = IntervalConverter.getDSInterval(intervalValue, format);
  }
  
  /**
   * Sets the value of interval attribute when 
   * have exact value in milliseconds (long)
   * 
   * @param interval 
   *                                                     Time interval value
   * @throws CEPException
   */
  public void vValueSet(long interval) throws CEPException
  {
    bNull = false;  //In case bNull was set to true for the previous tuple, 
                    //A call to ValueSet implies that this tuple is not null
    this.intervalValue = interval;
    this.interval = IntervalConverter.getDSInterval(interval, format);
  }
  
  /**
  * Sets the value of interval attribute when 
  * have exact value in String format (day to seconds)
  * 
  * @param interval
  *                                                            Time interval in String
  * @throws CEPException
  */
 public void vValueSet(String interval) throws CEPException
 {
   bNull = false;  //In case bNull was set to true for the previous tuple, 
                   //A call to ValueSet implies that this tuple is not null
   this.interval = interval;
 }
  /**
   * Sets the value of interval attribute when 
   * have exact value in String format (day to seconds)
   * 
   * @param interval  Time interval in String
   * @param format    interval value format
   * @throws CEPException
   */
  public void vValueSet(String interval, IntervalFormat format) 
     throws CEPException
  {
    bNull = false;  //In case bNull was set to true for the previous tuple, 
                    //A call to ValueSet implies that this tuple is not null
    this.interval = interval;
    this.format   = format;
  }
  
  /**
   * Sets the value of interval attribute when 
   * have exact value in milliseconds (long)
   * 
   * @param interval Time interval value
   * @param format format of interval value
   * @throws CEPException
   */
  public void vValueSet(long interval, IntervalFormat format) 
    throws CEPException
  {
    bNull = false;  //In case bNull was set to true for the previous tuple, 
                    //A call to ValueSet implies that this tuple is not null
    this.intervalValue = interval;
    this.format        = format;
    this.interval = IntervalConverter.getDSInterval(interval, format);
  }  
  
  /**
   * Gets the value of interval attribute
   * 
   * @return Attribute value
   * @throws CEPException
   */
  public String vValueGet() throws CEPException
  {
    if(interval == null || interval.equalsIgnoreCase(""))
      interval = IntervalConverter.getDSInterval(intervalValue, format);
    return interval;
  }
  
  /**
   * Get the interval format
   */
  public IntervalFormat vFormatGet()
  {
    if (format == null) 
    {
      // TODO: Currently we are creating object value using format INTERVAL DAY(9) TO SECONDS(9).
      // This is because interval type is created internally by CQL function (timestamp-timestamp or numtodsinterval) in
      // all our use-cases.
      // In both cases, the interval value format is INTERVAL DAY(9) TO SECONDS(9).
      // As we are not preserving format across multiple transformation in spark cql stages, we need a format value
      // in case if we want to convert interval to object and object to interval.
      format = IntervalFormat.getDefaultInternalDSFormat();
      assert format != null;
      /*
	    // Assign format to a default format
	    try
	    {
	      format 
	        = new IntervalFormat(TimeUnit.DAY, 
	                        TimeUnit.SECOND, 
	                        Constants.DEFAULT_INTERVAL_LEADING_PRECISION,
	                        Constants.DEFAULT_INTERVAL_FRACTIONAL_SECONDS_PRECISION);
	      
	    } catch (CEPException e)
	    {
	      // As this is a correct and default format, this error will not be raised
	      assert false;
	    }
	    */
    }
    return format;
  }
 
  /**
   * Gets the long value(nanoseconds) of interval attribute
   * 
   * @return interval attribute value in nanoseconds
   * @throws CEPException
   */
  public long intervalValGet() throws CEPException
  {
    if(interval != null)
    {
      intervalValue = 
        IntervalConverter.parseDToSIntervalString(interval, vFormatGet());
    }
    else
      assert false;
    
    return intervalValue;
  }
  
  public Object getObjectValue()
  {
    if(this.isBNull())
      return null;
    
    if(interval == null || interval.equalsIgnoreCase("")) 
      interval = IntervalConverter.getDSInterval(intervalValue, vFormatGet());
    return interval;
  }

  public void setObjectValue(Object val)
  {
    if (val == null) {
        setBNull(true);
        return;
    }
    if (val instanceof Number) {
        try {
            vValueSet( ((Number)val).longValue() );
        } catch (CEPException e) {
            throw new RuntimeException("Failed to set interval from :"+val.toString());
        }
    } else if (val instanceof String) {
        interval = ((String)val).trim();
    }
    interval = val.toString().trim();
  }

  public String getInterval()
  {
    if(this.isBNull())
      return null;
    
    if(interval == null || interval.equalsIgnoreCase(""))
      interval = IntervalConverter.getDSInterval(intervalValue, vFormatGet());
    return interval;
  }

  public void setInterval(String value)
  {
    this.interval = value;
  }

  public void setInterval(long value) throws CEPException
  {
	  interval = IntervalConverter.getDSInterval(value, vFormatGet());
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<IntervalAttribute ");
    sb.append("name=\"");
    sb.append(attributeName);
    sb.append("\">");
    if (bNull)
      sb.append("<Null/>");
    else
      sb.append("<Value>" + interval + "</Value>");
    sb.append("</IntervalAttribute>");
    return sb.toString();
  }

  public void readExternalBody(ObjectInput in) 
    throws IOException, ClassNotFoundException
  {
    interval = (String) in.readObject();
    format = (IntervalFormat) in.readObject();
  }
  
  public void writeExternalBody(ObjectOutput out) throws IOException
  {
    out.writeObject(interval);
    out.writeObject(format);
  }
  
  public AttributeValue clone() throws CloneNotSupportedException
  {
	  return new IntervalAttributeValue(this);
  }

  /**
   * @return the format
   */
  public IntervalFormat getFormat()
  {
    return vFormatGet();
  }

  /**
   * @param format the format to set
   */
  public void setFormat(IntervalFormat format)
  {
    this.format = format;
  }  
}
