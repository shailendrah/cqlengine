/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/TimestampAttributeValue.java /main/14 2013/08/20 02:38:32 sbishnoi Exp $ */

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
 sbishnoi    08/19/13 - bug 17317114
 sbishnoi    10/12/11 - support for timezone
 hopark      10/30/09 - add attrib name in toString
 hopark      11/28/08 - take timestamp format as an argument
 hopark      10/15/08 - refactoring
 hopark      09/04/08 - fix TupleValue clone
 hopark      08/22/08 - fix externalization
 udeshmuk    01/17/08 - change datatype of time to long.
 mthatte     12/05/07 - using CEPDateFormat
 rkomurav    08/27/07 - support 24 hr date format
 rkomurav    09/04/07 - support 24 hr format
 najain      05/02/07 - add constructor
 najain      03/12/07 - bug fix
 najain      10/29/06 - add toString
 najain      09/06/06 - add constructor
 najain      08/17/06 - concurrency issues
 parujain    08/03/06 - Timestamp datastructure
 parujain    08/03/06 - Creation
 */
package oracle.cep.dataStructures.external;

/**
 * @version $Header: TimestampAttributeValue.java1872 03-aug-2006.15:25:35
 *          parujain Exp $
 * @author parujain
 * @since release specific (what release of product did this appear in)
 */

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.ParseException;
import java.util.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import oracle.cep.common.CEPDate;
import oracle.cep.common.CEPDateFormat;
import oracle.cep.common.Datatype;
import oracle.cep.common.TimestampFormat;
import oracle.cep.exceptions.CEPException;

public class TimestampAttributeValue extends AttributeValue
{
  static final long serialVersionUID = -3414042793456205159L;

  /** timestamp/date attribute value in nanoseconds unit */
  private long time;
  
  /** format of timestamp/date value */
  private TimestampFormat format;

  /**
   * @param attributeName Attribute Name
   */
  public TimestampAttributeValue()
  {
    super(Datatype.TIMESTAMP);
    setBNull(true);
    format = TimestampFormat.getDefault();
  }

  public TimestampAttributeValue(String attributeName)
  {
    super(attributeName, Datatype.TIMESTAMP);
    setBNull(true);
    format = TimestampFormat.getDefault();
  }

  /**
   * @param attributeName
   *          Attribute Name
   * @param ts
   *          timestamp
   */
  public TimestampAttributeValue(long ts)
  {
    super(Datatype.TIMESTAMP);
    this.time = ts;
    setBNull(false);
    format = TimestampFormat.getDefault();
  }

  public TimestampAttributeValue(String attributeName, long ts)
  {
    super(attributeName, Datatype.TIMESTAMP);
    this.time = ts;
    setBNull(false);
    format = TimestampFormat.getDefault();
  }

  public TimestampAttributeValue(String attributeName, long ts, TimestampFormat fmt)
  {
    super(attributeName, Datatype.TIMESTAMP);
    this.time = ts;
    setBNull(false);
    format = fmt;
  }
  
  public TimestampAttributeValue(TimestampAttributeValue other)
  {
    super(other);
    time = other.time;
    try 
    {
      format = other.tFormatGet();
    } 
    catch (CEPException e) 
    {
      // Ideally this exception should never be thrown.
      // The throws block of tFormatGet() is because of interfaces.
      assert false;
    }
  } 
  
  /**
   * Gets the timestamp value of the timestamp attribute
   * 
   * @return Attribute value
   * @throws CEPException
   */
  public long tValueGet() throws CEPException
  {
    return time;
  }

  public long lValueGet() throws CEPException
  {
	return time;
  }
  
  /**
   * Sets the value of timestamp attribute when we have exact time
   * 
   * @param ts
   *          Timestamp value
   * @throws CEPException
   */
  public void tValueSet(long ts) throws CEPException
  {
    bNull = false;  //In case bNull was set to true for the previous tuple, 
                                        //A call to ValueSet implies that this tuple is not null
    time = ts;
  }
  
  public Object getObjectValue()
  {
    return time;
  }

  public void setObjectValue(Object val) {
    if (val == null) {
      setBNull(true);
      return;
    }
    if (val instanceof Timestamp) {
      time = ((Timestamp)val).getTime();
    } else if (val instanceof Date) {
      time = ((Date)val).getTime();
    } else if (val instanceof Number) {
      time = ((Number)val).longValue();
    } else {
      String strv = val.toString().trim();
      CEPDateFormat df = CEPDateFormat.getInstance();
      CEPDate dv = null;
      try {
        dv = df.parse(strv, format);
      } catch (ParseException ex) {
        throw new RuntimeException("Invalid Timestamp value, Failed to parse the timestamp value : " +"\"" +strv + "\"" ,ex);
      }
      time = dv.getValue();
    }
  }

  /**
   * Getter for value in TimestampAttributeValue
   * 
   * @return Returns the value
   */
  public long getTime()
  {
    return time;
  }

  /**
   * Sets the timestamp when the output tuple is set
   * 
   * @param ts
   *          Timestamp in long
   */
  public void setTime(long ts)
  {
    time = ts;
  }

  /**
   * Gets the timestamp format for given timestamp attribute
   * @return
   * @throws CEPException
   */
  public TimestampFormat tFormatGet() throws CEPException
  {
    return this.format;
  }
  
  /**
   * Sets the timestamp format for given timestamp attribute
   * @return
   * @throws CEPException
   */
  public void tFormatSet(TimestampFormat fmt) throws CEPException
  {
    this.format = fmt;
  }

  public String getStringValue(SimpleDateFormat sdf)
  {
    return sdf.format(time);
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<TimestampAttribute ");
    sb.append("name=\"");
    sb.append(attributeName);
    sb.append("\">");
    if (bNull)
      sb.append("<Null/>");
    else
      sb.append("<Value>" + time + "</Value>");
    
    // Append the timestamp format string
    if(this.format != null)
      sb.append("<Format>" + format.toString() + "</Format>");
    
    sb.append("</TimestampAttribute>");
    return sb.toString();
  }

  public void readExternalBody(ObjectInput in) 
    throws IOException, ClassNotFoundException
  {
    time = in.readLong();
    format = (TimestampFormat) in.readObject();
  }
  
  public void writeExternalBody(ObjectOutput out) throws IOException
  {
    out.writeLong(time);
    out.writeObject(format);
  }
  
  public AttributeValue clone() throws CloneNotSupportedException
  {
    return new TimestampAttributeValue(this);
  }  
}
