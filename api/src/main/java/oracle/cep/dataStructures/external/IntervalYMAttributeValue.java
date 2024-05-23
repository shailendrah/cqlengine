/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/IntervalYMAttributeValue.java /main/1 2011/09/05 22:47:26 sbishnoi Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    07/16/11 - Creation
 */
package oracle.cep.dataStructures.external;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalConverter;
import oracle.cep.common.IntervalFormat;
import oracle.cep.common.TimeUnit;
import oracle.cep.exceptions.CEPException;

/**
 *  @version $Header: IntervalYMAttributeValue.java 16-jul-2011.22:29:40 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class IntervalYMAttributeValue extends AttributeValue
{
  static final long serialVersionUID = -7060252621184862579L;
  
  private String interval;
  
  private Long intervalValue = null;
  
  private IntervalFormat format;

  /**
   * Empty Argument Constructor
   */
  public IntervalYMAttributeValue()
  {
    super(Datatype.INTERVALYM);
    setBNull(true);
  }
  
  /**
   * Construct Interval YEAR TO MONTH attribute value
   * @param attributeName name of attribute
   * @param format format of the interval value
   */
  public IntervalYMAttributeValue(String attributeName)
  {
    super(attributeName, Datatype.INTERVALYM);
    setBNull(true);
  }
  
  /**
   * Construct Interval YEAR TO MONTH attribute value
   * @param attributeName name of attribute
   * @param format format of the interval value
   */
  public IntervalYMAttributeValue(String attributeName, IntervalFormat format)
  {
    super(attributeName, Datatype.INTERVALYM);
    setBNull(true);
    this.format = format;
  }

  /**
   * Construct Interval YEAR TO MONTH attribute value
   * @param attributeName name of attribute
   * @param value interval value string
   * @param format format of the interval value string
   */
  public IntervalYMAttributeValue(String attributeName, String value, 
                                  IntervalFormat format)
  {
    super(attributeName, Datatype.INTERVALYM);
    this.interval = value;    
    this.format   = format;
  }

  /**
   * Construct Interval YEAR TO MONTH attribute value
   * @param attributeName name of attribute
   * @param value interval value string
   * @param format format of the interval value string
   */
  public IntervalYMAttributeValue(String attributeName, long value, 
                                  IntervalFormat format)
  {
    super(attributeName, Datatype.INTERVALYM);
    this.intervalValue = value;    
    this.format   = format;
    this.interval = IntervalConverter.getYMInterval(intervalValue, vFormatGet());
  }
  
  /**
   * Construct INTERVAL YEAR TO MONTH attribute from other attribute
   * @param other INTERVAL attribute
   */
  public IntervalYMAttributeValue(IntervalYMAttributeValue other)
  {
    super(other);
    interval      = other.interval;
    intervalValue = other.intervalValue;
    format        = other.format;    
  }
  
  /**
   * Sets the value of interval attribute when 
   * have exact value in nanoseconds (long)
   * 
   * @param interval Time interval value
   * @throws CEPException
   */
  public void vymValueSet(long interval, IntervalFormat format) throws CEPException
  {
    bNull = false;  //In case bNull was set to true for the previous tuple, 
                    //A call to ValueSet implies that this tuple is not null
    this.intervalValue = interval;
    this.format        = format;    
  }
  
  /**
   * Sets the value of interval attribute when 
   * have exact value in String format (year to month)
   * 
   * @param interval Time interval in String
   * @throws CEPException
   */
  public void vymValueSet(String interval, IntervalFormat format) throws CEPException
  {
    bNull = false;  //In case bNull was set to true for the previous tuple, 
                    //A call to ValueSet implies that this tuple is not null
    this.interval = interval;
    this.format   = format;
    this.intervalValue 
      = IntervalConverter.parseYToMIntervalString(interval, format);
  }
  
  /**
   * Gets the value of interval attribute
   * 
   * @return Attribute value
   * @throws CEPException
   */
  public String vymValueGet() throws CEPException
  {
    if(interval == null || interval.equalsIgnoreCase(""))
      interval = IntervalConverter.getYMInterval(intervalValue, vFormatGet());
    return interval;
  }
  
  /**
   * Get the Interval Value format
   */
  public IntervalFormat vFormatGet() 
  {
	if(format == null) {
	   // If no format is specified yet, construct a default format
	   try {
		   format   = new IntervalFormat(TimeUnit.YEAR, TimeUnit.MONTH, 9, true);
	   } catch (CEPException e) {
		   assert true;
	   }
	}	     
    return format;
  }
 
  /**
   * Gets the long value(nanoseconds) of interval attribute
   * 
   * @return interval attribute value in milliseconds
   * @throws CEPException
   */
  public long intervalYMValGet() throws CEPException
  {
    if(intervalValue == null && interval != null)
      intervalValue = IntervalConverter.parseYToMIntervalString(
                      interval, vFormatGet());
    return intervalValue;
  }
  
  public Object getObjectValue()
  {
    if(this.isBNull())
      return null;
    
    if(interval == null || interval.equalsIgnoreCase(""))
      interval = IntervalConverter.getYMInterval(intervalValue, vFormatGet());
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
    if(interval == null || interval.equalsIgnoreCase(""))
      interval = IntervalConverter.getYMInterval(intervalValue, vFormatGet());
    return interval;
  }

  public long getLongInterval() throws CEPException
  {
    if(intervalValue == null && interval != null)
      intervalValue = IntervalConverter.parseYToMIntervalString(
                      interval, vFormatGet());
    return intervalValue;
  }

  public void setInterval(String value)
  {
    this.interval = value;
  }

  public void setInterval(long value) throws CEPException
  {
    interval = IntervalConverter.getYMInterval(value, vFormatGet());   
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
    return new IntervalYMAttributeValue(this);
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
  
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<IntervalYMAttribute ");
    sb.append("name=\"");
    sb.append(attributeName);
    sb.append("\">");
    if (bNull)
      sb.append("<Null/>");
    else
      sb.append("<Value>" + interval + "</Value>");
    sb.append("</IntervalYMAttribute>");
    return sb.toString();
  }
}
