/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/TupleValue.java /main/39 2012/02/08 13:14:34 mjames Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares TupleValue in package oracle.cep.dataStructures.
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 NOTES
 <other useful comments, qualifications, etc.>
 MODIFIED    (MM/DD/YY)
 sbishnoi  11/07/11 - adding timestamp format api
 sbishnoi  08/27/11 - adding support for interval year to month
 anasrini  04/06/11 - include kind in toString
 udeshmuk  11/25/10 - XbranchMerge udeshmuk_bug-10328613_ps3 from
                      st_pcbpel_11.1.1.4.0
 udeshmuk  09/24/10 - XbranchMerge udeshmuk_prop_hb_across_processors from
                      st_pcbpel_11.1.1.4.0
 udeshmuk  09/23/10 - toSimpleString needs to change for hb
 mthatte   11/18/09 - bug 8534996
 sborah    06/21/09 - support for BigDecimal
 sbishnoi  04/13/09 - setting TupleKind for heartbeat
 sbishnoi  04/01/09 - adding getters and setters for totalOrderingGuarantee
 skmishra  03/28/09 - adding null check to toString()
 skmishra  03/28/09 - adding a null check to toString
 hopark    02/02/09 - add objtype
 anasrini  01/23/09 - add getEngineOutTime for perf measurements
 hopark    12/18/08 - handle null attrib on toString
 hopark    11/28/08 - remove tValueSet(str)
 hopark    10/15/08 - refactoring
 skmishra  10/15/08 - nullptrexception frm writeExternal
 hopark    09/04/08 - fix clone
 hopark    08/22/08 - fix externalization
 parujain  06/04/08 - fix epcis
 hopark    04/13/08 - add simplified toString
 mthatte   04/22/08 - removing isBNull
 mthatte   04/16/08 - adding Xmltype in writeExternal
 hopark    03/08/08 - make it externalizable
 mthatte   03/19/08 - adding generic setTime
 hopark    02/05/08 - parameterized error
 udeshmuk  01/30/08 - support for double data type.
 hopark    01/03/08 - support xmllog
 udeshmuk  01/17/08 - change data type of time field to long instead of
 java.sql.timestamp.
 najain    10/24/07 - xmltype support
 udeshmuk  11/22/07 - handle null timestamp in clone.
 mthatte   10/16/07 - adding check for attr==null in toString()
 parujain  09/26/07 - epr for push source
 najain    05/01/07 - implement serializable
 najain    03/12/07 - bug fix
 hopark    11/16/06 - add BIGINT datatype
 najain    10/29/06 - add toString
 anasrini  10/24/06 - add getKind
 dlenkov   10/18/06 - byte data type support
 parujain  10/06/06 - Interval datatype
 najain    09/06/06 - add setters
 parujain  08/04/06 - Datatype Timestamp
 parujain  08/03/06 - Timestamp datastructure
 najain    05/22/06 - bug fix 
 najain    05/18/06 - add +/- tuple 
 skaluska  03/17/06 - change names to String 
 ayalaman  03/08/06 - object name as a string 
 skaluska  02/18/06 - some checking 
 skaluska  02/17/06 - Creation
 skaluska  02/17/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/TupleValue.java /main/35 2010/11/26 10:14:09 udeshmuk Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.external;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.oracle.cep.api.event.Attr;
import com.oracle.cep.api.event.Event;
import oracle.cep.common.IntervalFormat;
import oracle.cep.common.TimestampFormat;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.DataStructuresError;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.IDumpable;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

/**
 * This is the external format for tuple value, passed between the engine and
 * input and output adaptors.
 * 
 * @author najain
 */
@DumpDesc(attribTags =
{ "objectName", "Time", "Kind", "HeartBeat" }, attribVals =
{ "@objectName", "@time", "@kind", "@bHeartBeat" })
public class TupleValue
    implements Externalizable, Cloneable, IDumpable, Event
{
  static final long     serialVersionUID = -1398424206431989945L;

  /** Event Type Name - consumed by epn components in evs */
  protected String         eventTypeName = null;
  
  /** Name of the owning stream or relation */
  protected String         objectName;

  /** TimeStamp associated with the tuple */
  protected long           time;

  /** Array of attribute values */
  protected AttributeValue attrs[];

  /** true if this is a heartbeat */
  protected boolean        bHeartBeat;

  /** type of the tuple */
  protected TupleKind      kind;

  /** engine output time - for performance measurements */
  protected long           engineOutTime;

  /** true if next tuple value will have higher TimeStamp value*/
  protected boolean        isTotalOrderGuarantee;

  private Map<String, Integer> attrMap;

  /**
   * Helper method to create HeartBeat TupleValue
   * @param objectName name of stream or relation
   * @param time the timestamp of tuple
   * @return
   * @throws CEPException
   */
  public static TupleValue heartbeat(String objectName, long time)  throws CEPException {
      return new TupleValue(objectName, time, null, true, false);
  }
  
  public TupleValue()
  {
  }

  /**
   * Constructor for TupleValue
   * 
   * @param objectName
   *                Name of stream/relation
   * @param time
   *                TimeStamp of tuple
   * @param attr
   *                Array of attribute values
   * @param isHeartBeat
   *                Whether a heartbeat
   * @param isBNull
   *                Whether null
   * @throws CEPException
   */
  public TupleValue(String objectName, long time, AttributeValue[] attr,
      boolean isHeartBeat) throws CEPException
  {
      // set isTotalOrderGuarantee to FALSE by default
      this(objectName, time, attr, isHeartBeat, false);
  }
  
  public TupleValue(String objectName, long time, AttributeValue[] attr,
      boolean isHeartBeat, boolean isTotalOrderGuarantee) throws CEPException
  {
    this.objectName = objectName;
    this.time       = time;
    this.attrs      = attr;
    this.bHeartBeat = isHeartBeat;
    this.isTotalOrderGuarantee = isTotalOrderGuarantee;
  }

  public int getNoAttributes()
  {
    return (attrs == null) ? 0 : attrs.length;
  }
  
  /**
   * @param attrs
   *                The attrs to set.
   */
  public void setAttrs(AttributeValue[] attrs)
  {
    this.attrs = attrs;
  }

  /**
   * Getter for time in TupleValue
   * 
   * @return the kind of the tuple
   */
  public TupleKind getKind()
  {
    return kind;
  }

  /**
   * @param kind
   *                The kind to set.
   */
  public void setKind(TupleKind kind)
  {
    this.kind = kind;
  }


  /**
   * Returns eventTypeName
   * @return
   */
  public String getEventTypeName()
  {
    return eventTypeName;
  }
  
  /**
   * @param eventTypeName
   *                The eventTypeName to set.
   */
  public void setEventTypeName(String eventTypeName)
  {
    this.eventTypeName = eventTypeName;
  }

  /**
   * Returns objectName
   * @return
   */
  public String getObjectName()
  {
    return objectName;
  }
  
  /**
   * @param objectName
   *                The objectName to set.
   */
  public void setObjectName(String objectName)
  {
    this.objectName = objectName;
  }

  /**
   * Getter for time in TupleValue
   * 
   * @return Returns the time
   */
  public long getTime()
  {
    return time;
  }

  /**
   * Setter for time in TupleValue
   * 
   * @param time
   *                The time to set.
   */
  public void setTime(long time)
  {
    this.time = time;
  }

  /**
   * Getter for engineOutTime in TupleValue
   * 
   * @return Returns the CQL engine output time
   */
  public long getEngineOutTime()
  {
    return engineOutTime;
  }

  /**
   * Setter for engineOutTime in TupleValue
   * 
   * @param engineOutTime
   *                The CQL engine output time to set.
   */
  public void setEngineOutTime(long engineOutTime)
  {
    this.engineOutTime = engineOutTime;
  }

  /**
   * Getter for bHeartBeat in TupleValue
   * 
   * @return Returns the bHeartBeat
   */
  public boolean isBHeartBeat()
  {
    return bHeartBeat;
  }

  /**
   * Event interface.
   * Same as isBHeartBeat but better name for external event.
   * @return
   */
  public boolean isHeartbeat()
  {
    return bHeartBeat;
  }

  /**
   * Setter for bHeartBeat in TupleValue
   * 
   * @param heartBeat
   *                The bHeartBeat to set.
   */
  public void setBHeartBeat(boolean heartBeat)
  {
    bHeartBeat = heartBeat;
    if(heartBeat)
     setKind(TupleKind.HEARTBEAT);
  }

  public AttributeValue[] getAttributes()
  {
    return attrs;
  }

  /**
   * Returns the attribute value for the specified position.
   * 
   * @param position
   *                Position of interest
   * @return Attribute value
   * @throws CEPException
   */
  public AttributeValue getAttribute(int position) throws CEPException
  {
    if ((attrs == null) || (position >= attrs.length))
      throw new CEPException(DataStructuresError.INVALID_POSITION, position);
    return attrs[position];
  }

  /**
   * Sets the attribute value for the specified position.
   * 
   * @param position
   *                Position of interest
   * @param value
   *                Value to be set
   * @throws CEPException
   */
  public void setAttribute(int position, AttributeValue value)
      throws CEPException
  {
    if ((attrs == null) || (position >= attrs.length))
      throw new CEPException(DataStructuresError.INVALID_POSITION, position);
    attrs[position] = value;
  }

  /**
   * Gets the object value of an attribute
   * @param position
   * @return
   */
  public Object getObjectValue(int position) throws CEPException
  {
    if(this.getAttribute(position).isBNull())
      return null;
    else
      return this.getAttribute(position).getObjectValue();
  }

  /**
   * Gets the value of an int attribute
   * 
   * @param position
   *                Position of interest
   * @return Attribute value
   * @throws CEPException
   */
  public int iValueGet(int position) throws CEPException
  {
    return this.getAttribute(position).iValueGet();
  }

  /**
   * Sets the value of an int attribute
   * 
   * @param position
   *                Position of interest
   * @param v
   *                Attribute value to set
   * @throws CEPException
   */
  public void iValueSet(int position, int v) throws CEPException
  {
    this.getAttribute(position).iValueSet(v);
  }

  /**
   * Gets the value of an boolean attribute
   * 
   * @param position
   *                Position of interest
   * @throws CEPException
   */
  public boolean boolValueGet(int position) throws CEPException
  {
    return this.getAttribute(position).boolValueGet();
  }

  /**
   * Sets the value of an boolean attribute
   * 
   * @param position
   *                Position of interest
   * @param v
   *                Attribute value to set
   * @throws CEPException
   */
  public void boolValueSet(int position, boolean v) throws CEPException
  {
    this.getAttribute(position).boolValueSet(v);
  }

  /**
   * Gets the value of a bigint attribute
   * 
   * @param position
   *                Position of interest
   * @return Attribute value
   * @throws CEPException
   */
  public long lValueGet(int position) throws CEPException
  {
    return this.getAttribute(position).lValueGet();
  }

  /**
   * Sets the value of a bigint attribute
   * 
   * @param position
   *                Position of interest
   * @param v
   *                Attribute value to set
   * @throws CEPException
   */
  public void lValueSet(int position, long v) throws CEPException
  {
    this.getAttribute(position).lValueSet(v);
  }

  /**
   * Gets the value of an float attribute
   * 
   * @param position
   *                Position of interest
   * @return Attribute value
   * @throws CEPException
   */
  public float fValueGet(int position) throws CEPException
  {
    return this.getAttribute(position).fValueGet();
  }

  /**
   * Sets the value of an float attribute
   * 
   * @param position
   *                Position of interest
   * @param v
   *                Attribute value to set
   * @throws CEPException
   */
  public void fValueSet(int position, float v) throws CEPException
  {
    this.getAttribute(position).fValueSet(v);
  }

  /**
   * Gets the value of a double attribute
   * 
   * @param position
   *                Position of interest
   * @return Attribute value
   * @throws CEPException
   */
  public double dValueGet(int position) throws CEPException
  {
    return this.getAttribute(position).dValueGet();
  }

  /**
   * Sets the value of a double attribute
   * 
   * @param position
   *                Position of interest
   * @param v
   *                Attribute value to set
   * @throws CEPException
   */
  public void dValueSet(int position, double v) throws CEPException
  {
    this.getAttribute(position).dValueSet(v);
  }
  
  
  /**
  * Gets the value of a BigDecimal attribute
  * 
  * @param position
  *                Position of interest
  * @return Attribute value
  * @throws CEPException
  */
 public BigDecimal nValueGet(int position) throws CEPException
 {
   return this.getAttribute(position).nValueGet();
 }

 /**
  * Sets the value of a BigDecimal attribute
  * 
  * @param position
  *                Position of interest
  * @param v
  *                Attribute value to set
  * @throws CEPException
  */
 public void nValueSet(int position, BigDecimal v, int precision, int scale) 
 throws CEPException
 {
   this.getAttribute(position).nValueSet(v, precision, scale);
 }
  
 /**
  * Gets the scale of an bigdecimal attribute
  * 
  * @param position
  *                Position of interest
  * @return Attribute bigdecimal scale value
  * @throws CEPException
  */
 public int nScaleGet(int position) throws CEPException
 {
   return this.getAttribute(position).nScaleGet();
 }

 /**
  * Gets the precision of an bigdecimal attribute
  * 
  * @param position
  *                Position of interest
  * @return Attribute bigdecimal precision value
  * @throws CEPException
  */
 public int nPrecisionGet(int position) throws CEPException
 {
   return this.getAttribute(position).nPrecisionGet();
 }

 
  /**
   * Gets the timestamp value of the timestamp attribute
   * 
   * @param position
   *                Position of interest
   * @return Attribute value
   * @throws CEPException
   */
  public long tValueGet(int position) throws CEPException
  {
    return this.getAttribute(position).tValueGet();
  }

  /**
   * Sets the timestamp value of the attribute
   * 
   * @param position
   *                Position of interest
   * @param ts
   *                Timestamp value
   * @throws CEPException
   */
  public void tValueSet(int position, long ts) throws CEPException
  {
    this.getAttribute(position).tValueSet(ts);
  }
 
  /**
   * Sets the timestamp format for the attribute
   * @param position
   * @param format
   * @throws CEPException
   */
  public void tFormatSet(int position, TimestampFormat format) throws CEPException
  {
    this.getAttribute(position).tFormatSet(format);
  }
  

  /**
   * Sets the interval value of the attribute
   * 
   * @param position
   *                Position of interest
   * @param interval
   *                Interval value in milliseconds(long)
   * @throws CEPException
   */
  public void vValueSet(int position, long interval) throws CEPException
  {
    this.getAttribute(position).vValueSet(interval);
  }
  /**
   * Sets the interval value of the attribute
   * 
   * @param position
   *                Position of interest
   * @param interval
   *                Interval taken as string as input
   * @throws CEPException
   */
  public void vValueSet(int position, String interval) throws CEPException
  {
    this.getAttribute(position).vValueSet(interval);
  }
  
  /**
   * Sets the interval value of the attribute
   * 
   * @param position
   *                Position of interest
   * @param interval
   *                Interval value in milliseconds(long)
   * @param format
   *                Format of the interval value
   * @throws CEPException
   */
  public void vValueSet(int position, long interval, IntervalFormat format) throws CEPException
  {
    this.getAttribute(position).vValueSet(interval, format);
  }
  /**
   * Sets the interval value of the attribute
   * 
   * @param position
   *                Position of interest
   * @param interval
   *                Interval taken as string as input
   * @throws CEPException
   */
  public void vValueSet(int position, String interval, IntervalFormat format) throws CEPException
  {
    this.getAttribute(position).vValueSet(interval);
  }
  
  
  /**
   * Gets the interval value of the attribute
   * 
   * @param position
   *                Position of interest
   * @return Interval value of attribute
   * @throws CEPException
   */
  public String vValueGet(int position) throws CEPException
  {
    return this.getAttribute(position).vValueGet();
  }
  /**
   * Gets the long value of interval attribute
   * 
   * @param position
   *                Position of interest
   * @return Long value of interval attribute
   * @throws CEPException
   */
  public long intervalValGet(int position) throws CEPException
  {
    return this.getAttribute(position).intervalValGet();
  }
  /**
   * Sets the interval year to month value of the attribute
   * 
   * @param position
   *                Position of interest
   * @param interval
   *                Interval value in milliseconds(long)
   * @throws CEPException
   */
  public void vymValueSet(int position, long interval, IntervalFormat format) throws CEPException
  {
    this.getAttribute(position).vymValueSet(interval, format);
  }
  /**
   * Sets the interval year to month value of the attribute
   * 
   * @param position
   *                Position of interest
   * @param interval
   *                Interval taken as string as input
   * @throws CEPException
   */
  public void vymValueSet(int position, String interval, IntervalFormat format) throws CEPException
  {
    this.getAttribute(position).vymValueSet(interval, format);
  }
  
  /**
   * Gets the interval year to month value of the attribute
   * 
   * @param position
   *                Position of interest
   * @return Interval value of attribute
   * @throws CEPException
   */
  public String vymValueGet(int position) throws CEPException
  {
    return this.getAttribute(position).vymValueGet();
  }
  
  /**
   * Gets the long value of interval year to month attribute
   * 
   * @param position
   *                Position of interest
   * @return Long value of interval attribute
   * @throws CEPException
   */
  public long intervalYMValGet(int position) throws CEPException
  {
    return this.getAttribute(position).intervalYMValGet();
  }
  
  /**
   * Gets the interval format for this interval value
   * @param position
   * @return
   * @throws CEPException
   */
  public IntervalFormat vFormatGet(int position) throws CEPException
  {
    return this.getAttribute(position).vFormatGet();  
  }
  
  /**
   * Gets the timestamp format for this timestamp value
   * @param position
   * @return
   * @throws CEPException
   */
  public TimestampFormat tFormatGet(int position) throws CEPException
  {
    return this.getAttribute(position).tFormatGet();
  }
  
  /**
   * Gets the value of an char attribute
   * 
   * @param position
   *                Position of interest
   * @return Attribute value
   * @throws CEPException
   */
  public char[] cValueGet(int position) throws CEPException
  {
    return this.getAttribute(position).cValueGet();
  }

  /**
   * Gets the value of an xmltype attribute
   * 
   * @param position
   *                Position of interest
   * @return Attribute value
   * @throws CEPException
   */
  public char[] xValueGet(int position) throws CEPException
  {
    return this.getAttribute(position).xValueGet();
  }

  /**
   * Sets the value of an char attribute
   * 
   * @param position
   *                Position of interest
   * @param v
   *                Attribute value to set
   * @throws CEPException
   */
  public void cValueSet(int position, char[] v) throws CEPException
  {
    this.getAttribute(position).cValueSet(v);
  }

  /**
   * Sets the value of an xmltype attribute
   * 
   * @param position
   *                Position of interest
   * @param v
   *                Attribute value to set
   * @throws CEPException
   */
  public void xValueSet(int position, char[] v) throws CEPException
  {
    this.getAttribute(position).xValueSet(v);
  }

  /**
   * Gets the length of an char attribute
   * 
   * @param position
   *                Position of interest
   * @return Attribute length
   * @throws CEPException
   */
  public int cLengthGet(int position) throws CEPException
  {
    return this.getAttribute(position).cLengthGet();
  }

  /**
   * Gets the length of an xmltype attribute
   * 
   * @param position
   *                Position of interest
   * @return Attribute length
   * @throws CEPException
   */
  public int xLengthGet(int position) throws CEPException
  {
    return this.getAttribute(position).xLengthGet();
  }

  /**
   * Sets the length of an char attribute
   * 
   * @param position
   *                Position of interest
   * @param v
   *                Attribute length to set
   * @throws CEPException
   */
  public void cLengthSet(int position, int v) throws CEPException
  {
    this.getAttribute(position).cLengthSet(v);
  }

  /**
   * Sets the length of an xmltype attribute
   * 
   * @param position
   *                Position of interest
   * @param v
   *                Attribute length to set
   * @throws CEPException
   */
  public void xLengthSet(int position, int v) throws CEPException
  {
    this.getAttribute(position).xLengthSet(v);
  }

  /**
   * Gets the value of an byte attribute
   * 
   * @param position
   *                Position of interest
   * @return Attribute value
   * @throws CEPException
   */
  public byte[] bValueGet(int position) throws CEPException
  {
    return this.getAttribute(position).bValueGet();
  }

  /**
   * Sets the value of an byte attribute
   * 
   * @param position
   *                Position of interest
   * @param v
   *                Attribute value to set
   * @throws CEPException
   */
  public void bValueSet(int position, byte[] v) throws CEPException
  {
    this.getAttribute(position).bValueSet(v);
  }

  /**
   * Gets the length of an byte attribute
   * 
   * @param position
   *                Position of interest
   * @return Attribute length
   * @throws CEPException
   */
  public int bLengthGet(int position) throws CEPException
  {
    return this.getAttribute(position).bLengthGet();
  }

  /**
   * Sets the length of an byte attribute
   * 
   * @param position
   *                Position of interest
   * @param v
   *                Attribute length to set
   * @throws CEPException
   */
  public void bLengthSet(int position, int v) throws CEPException
  {
    this.getAttribute(position).bLengthSet(v);
  }

  public int hashCode() {
     int[] hashcodes = new int[8 + (attrs == null ? 0 : attrs.length)];
     hashcodes[0] = Long.hashCode(time);
     hashcodes[1] = Boolean.hashCode(bHeartBeat);
     hashcodes[2] = kind == null ? 0 : kind.hashCode();
     hashcodes[3] = Long.hashCode(engineOutTime);
     hashcodes[4] = Boolean.hashCode(isTotalOrderGuarantee);
     hashcodes[5] = eventTypeName == null ? 0 : eventTypeName.hashCode();
     hashcodes[6] = objectName == null ? 0 : objectName.hashCode();
     hashcodes[7] = attrs == null ? 0 : attrs.length; 
     if (attrs != null) {
         int pos = 8;
         for (int i = 0; i < attrs.length; i++) {
             Object v = null;
             if (attrs[i] != null) v = attrs[i].getObjectValue();
             hashcodes[pos++] = v == null ? 0 : v.hashCode();
         }
     }
     return Arrays.hashCode(hashcodes); 
  }
  
  private static final boolean objectComp(Object a, Object b) {
      if (a == null || b == null) return (a == b);
      return a.equals(b);
  }
  
  public boolean equals(Object o) {
      if (!(o instanceof TupleValue)) return false;
      TupleValue other = (TupleValue) o;
      if (other.time != time) return false;
      if (other.bHeartBeat != bHeartBeat) return false;
      if (other.kind != kind) return false;
      if (other.engineOutTime != engineOutTime) return false;
      if (other.isTotalOrderGuarantee != isTotalOrderGuarantee) return false;
      if ( !objectComp(eventTypeName, other.eventTypeName)) return false;
      if ( !objectComp(objectName, other.objectName)) return false;
      if (attrs == null || other.attrs == null) return (attrs == other.attrs);
      if (attrs.length != other.attrs.length) return false;
      for (int i = 0; i < attrs.length; i++) {
          Object v = (attrs[i] == null) ? null : attrs[i].getObjectValue();
          Object v1 = (other.attrs[i] == null) ? null : other.attrs[i].getObjectValue();
          if ( !objectComp(v, v1) ) return false;
      }
      return true;
  }
  
  public TupleValue clone() throws CloneNotSupportedException
  {
    TupleValue tuple = (TupleValue) super.clone();

    // directly copy primitive types
    tuple.time                  = this.time;
    tuple.bHeartBeat            = this.bHeartBeat;
    tuple.kind                  = this.kind;
    tuple.engineOutTime         = this.engineOutTime;
    tuple.isTotalOrderGuarantee = this.isTotalOrderGuarantee;

    // assign new memory for String type
    if(this.eventTypeName!=null)
     tuple.eventTypeName = new String(eventTypeName);
    
    // assign new memory for String type
    if(this.objectName!=null)
     tuple.objectName = new String(objectName);

    // copy attrs if they exist
    if (this.attrs != null)
    {
      tuple.attrs = new AttributeValue[attrs.length];
      for (int i = 0; i < attrs.length; i++)
      {
          if (attrs[i] == null)
            tuple.attrs[i] = null;
          else
            tuple.attrs[i] = attrs[i].clone();
      }
    }
    return tuple;
  }

  /**
   * Gets the value of an object attribute
   * 
   * @param position
   *                Position of interest
   * @return Attribute value
   * @throws CEPException
   */
  public Object oValueGet(int position) throws CEPException
  {
    return this.getAttribute(position).oValueGet();
  }

  /**
   * Sets the value of an objectattribute
   * 
   * @param position
   *                Position of interest
   * @param v
   *                Attribute value to set
   * @throws CEPException
   */
  public void oValueSet(int position, Object v) throws CEPException
  {
    this.getAttribute(position).oValueSet(v);
  }

  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    eventTypeName = (String) in.readObject();
    objectName = (String) in.readObject();
    bHeartBeat = in.readBoolean();
    kind = null;
    
    kind = (TupleKind) in.readObject();

    time = in.readLong();

    int attrlen = in.readInt();
    if (attrlen > 0)
    {
      attrs = new AttributeValue[attrlen];
      for (int i = 0; i < attrlen; i++)
      {
        attrs[i] = (AttributeValue) in.readObject();
      }
    }
    isTotalOrderGuarantee = in.readBoolean();
  }

  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeObject(eventTypeName);
    out.writeObject(objectName);
    out.writeBoolean(bHeartBeat);

    out.writeObject(kind);

    out.writeLong(time);
    if(attrs == null)
      out.writeInt(0);
    else
      out.writeInt(attrs.length);
    if (attrs != null)
    {
      for (int i = 0; i < attrs.length; i++)
      {
        out.writeObject(attrs[i]);
      }
    }
    out.writeBoolean(isTotalOrderGuarantee);
  }
 
  public String toString() 
  {
      return toSimpleString();
  }
  
  public String toDetailedString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<TupleValue>");
    if (eventTypeName != null)
    {
      sb.append("<EventType>");
      sb.append(eventTypeName);
      sb.append("</EventType>");
    }
    if (objectName != null)
    {
      sb.append("<ObjectName>");
      sb.append(objectName);
      sb.append("</ObjectName>");
    }
    sb.append("<Timestamp>" + time + "</Timestamp>");
    sb.append("<TupleKind>" + kind + "</TupleKind>");

    if (attrs != null)
    {
      for (int i = 0; i < attrs.length; i++)
      {
        if (attrs[i] == null || attrs[i].isBNull())
        {
          sb.append("<Attr pos=\"" + i + "\">" + null + "</Attr>");
        } 
        else 
        {
          sb.append(attrs[i].toString());
          
        }
      }
    }
    sb.append("<IsTotalOrderGuarantee>" + isTotalOrderGuarantee + "</IsTotalOrderGuarantee>");
    sb.append("</TupleValue>");
    return sb.toString();
  }

  public String toSimpleString()
  {
    StringBuilder b = new StringBuilder();
    if (eventTypeName != null)
    {
      b.append("eventType=");
      b.append(eventTypeName);
      b.append(" ");
    }
    if (objectName != null)
    {
      b.append("object=");
      b.append(objectName);
      b.append(" ");
    }

    b.append("kind=");
    if (this.isBHeartBeat())
      b.append("heartbeat");
    else 
      b.append(kind);

    b.append(" time=");
    b.append(time);
    b.append(" ");
    if(!this.isBHeartBeat() && attrs != null)
    {
      for (int i = 0; i < attrs.length; i++)
      {
        AttributeValue attr = attrs[i];
        if (i > 0)
          b.append(", ");
        if (attr == null) 
        {
          b.append("attr=null");
          continue;
        }  
        if (attr.attributeName != null)
        {
      	  b.append(attr.attributeName);
    	  b.append("=");
        }
        if (attr.isBNull())
        {
          b.append("null");
          continue;
        }
        Object val = null;
        try
        {
          val = getObjectValue(i);
        }catch(CEPException e)
        {
          LogUtil.severe(LoggerType.TRACE, "TupleValue.toSimpleString: not supported datatype " + attr.attributeType);
          LogUtil.logStackTrace(e);
        }
        b.append(val == null ? "null" : val.toString());
      }
    }
    else
      b.append(" no attrs ");
    b.append(" isTotalOrderGuarantee=");
    b.append(isTotalOrderGuarantee);
    return b.toString();
  }
  
  public synchronized void dump(IDumpContext dumper)
  {
    String tag = LogUtil.beginDumpObj(dumper, this);
    if (attrs != null)
    {
      for (int i = 0; i < attrs.length; i++)
      {
        if (attrs[i] != null)
          attrs[i].dump(dumper);
        else
        {
          dumper.writeln("Attr", "null");
        }
      }
    }
    LogUtil.endDumpObj(dumper, tag);
  }

  /**
   * @return the isTotalOrderGuarantee
   */
  public boolean isTotalOrderGuarantee()
  {
    return isTotalOrderGuarantee;
  }

  /**
   * @param isTotalOrderGuarantee the isTotalOrderGuarantee to set
   */
  public void setTotalOrderGuarantee(boolean isTotalOrderGuarantee)
  {
    this.isTotalOrderGuarantee = isTotalOrderGuarantee;
  }

  @Override
  public Attr getAttr(int idx) {
    return attrs[idx];
  }

  @Override
  public synchronized Attr getAttr(String name) {
    Integer idx = attrMap.get(name);
    if (idx == null) return null;
    return getAttr(idx);
  }

  /**
   * NameMap contains the reverse mapping with current -> previous shape
   * e.g)
   * (message, messageText) means tuple.messageText <-- message
   *
   * @param name
   * @param nameMap
   * @return
   */
  public Map<String, Integer> buildAttrMap(Map<String,String> nameMap) {
      HashMap<String, Integer> amap = new HashMap<String, Integer>();
      //First set the non-mapped attributes
      int i = 0;
      for (AttributeValue a : attrs) {
        String aname = a.getRootAttrName();
        amap.put(aname, i);
        amap.put(aname.toUpperCase(), i);
        i++;
      }
      if (nameMap != null) {
          for (String k : nameMap.keySet()) {
              String mappedName = nameMap.get(k);
              if (amap.containsKey(mappedName)) {
                  i = amap.get(mappedName);
                  amap.put(k, i);
              }
          }
      }
      return amap;
  }

  public synchronized Map<String, Integer>  getAttrMap() {
    return attrMap;
  }
  public synchronized void setAttrMap(Map<String, Integer> attrMap) {
    this.attrMap = attrMap;
  }
}
