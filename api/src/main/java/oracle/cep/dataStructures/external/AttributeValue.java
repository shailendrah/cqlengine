/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/AttributeValue.java /main/23 2012/08/31 01:51:24 pkali Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares AttributeValue in package oracle.cep.dataStructures.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 pkali     08/30/12 - XbranchMerge pkali_bug-14465875_ps6 from
                      st_pcbpel_11.1.1.4.0
 pkali     08/27/12 - fixed err msg arg
 sbishnoi  11/07/11 - adding timestamp format api
 sbishnoi  08/27/11 - adding support for interval year to month
 sborah    06/17/09 - support for BigDecimal
 hopark    02/02/09 - add oValueGet/set
 hopark    11/28/08 - remove tValueSet(str)
 hopark    10/15/08 - refactoring
 hopark    09/04/08 - fix TupleValue clone
 hopark    08/22/08 - fix externalization
 skmishra  08/22/08 - LogTags
 hopark    06/19/08 - 
 mthatte   02/12/08 - char xml casting
 hopark    02/05/08 - parameterized error
 udeshmuk  01/30/08 - support for double data type.
 hopark    01/03/08 - support xmllog
 udeshmuk  01/17/08 - change in the datatype of time field in TupleValue.
 najain    10/24/07 - xmltype support
 mthatte   12/05/07 - clearing bNull in setters
 najain    05/01/07 - implement serializable
 najain    03/12/07 - bug fix
 najain    10/29/06 - add toString
 parujain  10/06/06 - Interval datatype
 parujain  08/04/06 - Datatype Timestamp
 parujain  08/03/06 - Timestamp datastructure
 skaluska  03/17/06 - change names to String 
 ayalaman  03/08/06 - fix type for fValueGet 
 skaluska  02/17/06 - Creation
 skaluska  02/17/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/dataStructures/external/AttributeValue.java /main/23 2012/08/31 01:51:24 pkali Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.external;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.oracle.cep.api.event.Attr;
import com.oracle.cep.api.event.AttrDataType;
import com.oracle.cep.api.event.AttrSpec;
import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalFormat;
import oracle.cep.common.TimestampFormat;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.DataStructuresError;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.IDumpable;
import oracle.cep.logging.LogUtil;

import static oracle.cep.extensibility.type.IType.Kind.CHAR;

/**
 * AttributeValue
 * 
 * @author skaluska
 */
public abstract class AttributeValue
    implements IDumpable, Externalizable, Attr {
  private static final long serialVersionUID = 4918205306564790497L;

  /**
   * Name of the attribute
   */
  protected String attributeName;

  /**
   * attribute's datatype
   */
  protected Datatype attributeType;

  /**
   * true if this attribute is Null
   */
  protected boolean bNull;

  private String rootAttrName;  //If attrname has dot in it,

  /**
   * log tags for this class
   */
  protected static final String ATTR_ATTRIBUTEVAL[] = {"Name", "Null"};

  public AttributeValue() {

  }

  /**
   * Constructor for AttributeValue
   *
   * @param attributeName Attribute name
   * @param attributeType Attribute type
   */
  public AttributeValue(Datatype attributeType) {
    this.attributeType = attributeType;
  }

  public AttributeValue(String attributeName, Datatype attributeType) {
    this.attributeName = attributeName;
    this.attributeType = attributeType;
  }

  public AttributeValue(AttributeValue other) {
    attributeName = other.attributeName;
    attributeType = other.attributeType;
    bNull = other.bNull;
  }

  public abstract Object getObjectValue();
  public abstract void setObjectValue(Object v);

  public String getAttributeName() {
    return attributeName;
  }

  public String getRootAttrName() {
    if (rootAttrName == null) {
      int idx = attributeName.lastIndexOf('.');
      rootAttrName = (idx >= 0) ? attributeName.substring(idx + 1) : attributeName;
    }
    return rootAttrName;
  }

  public Datatype getAttributeType() {
    return attributeType;
  }

  public String getStringValue() {
    return null;
  }

  /**
   * Getter for bNull in AttributeValue
   *
   * @return Returns the bNull
   */
  public boolean isBNull() {
    return bNull;
  }

  /**
   * Setter for bNull in AttributeValue
   *
   * @param null1 The bNull to set.
   */
  public void setBNull(boolean null1) {
    bNull = null1;
  }

  /**
   * Gets the value of an int attribute
   *
   * @return Attribute value
   * @throws CEPException
   */
  public int iValueGet() throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.INT.toString());
  }

  /**
   * Sets the value of an int attribute
   *
   * @param v Attribute value to set
   * @throws CEPException
   */
  public void iValueSet(int v) throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.INT.toString());
  }

  /**
   * Gets the value of an boolean attribute
   *
   * @throws CEPException
   */
  public boolean boolValueGet() throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.BOOLEAN.toString());
  }

  /**
   * Sets the value of an boolean attribute
   *
   * @param v Attribute value to set
   * @throws CEPException
   */
  public void boolValueSet(boolean v) throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.BOOLEAN.toString());
  }

  /**
   * Gets the value of an int attribute
   *
   * @return Attribute value
   * @throws CEPException
   */
  public long lValueGet() throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.BIGINT.toString());
  }

  /**
   * Sets the value of an int attribute
   *
   * @param v Attribute value to set
   * @throws CEPException
   */
  public void lValueSet(long v) throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.BIGINT.toString());
  }


  /**
   * Gets the value of a float attribute
   *
   * @return Attribute value
   * @throws CEPException
   */
  public float fValueGet() throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.FLOAT.toString());
  }

  /**
   * Sets the value of an float attribute
   *
   * @param v Attribute value to set
   * @throws CEPException
   */
  public void fValueSet(float v) throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.FLOAT.toString());
  }

  /**
   * Get the value of double attribute
   *
   * @return double attribute value
   * @throws CEPException
   */
  public double dValueGet() throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.DOUBLE.toString());
  }

  public void dValueSet(double v) throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.DOUBLE.toString());
  }

  /**
   * Get the value of BigDecimal attribute
   *
   * @return BigDecimal attribute value
   * @throws CEPException
   */
  public BigDecimal nValueGet() throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.BIGDECIMAL.toString());
  }

  /**
   * Sets the value of an bigdecimal attribute
   *
   * @param v         The bigdecimal value to be set
   * @param precision Attribute precision to set
   * @param scale     Attribute scale to set
   * @throws CEPException
   */
  public void nValueSet(BigDecimal v, int precision, int scale) throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.BIGDECIMAL.toString());
  }

  /**
   * Get the precision of bigdecimal attribute
   *
   * @return precision attribute value
   * @throws CEPException
   */
  public int nPrecisionGet() throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.BIGDECIMAL.toString());
  }

  /**
   * Get the precision of bigdecimal attribute
   *
   * @return precision attribute value
   * @throws CEPException
   */
  public int nScaleGet() throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.BIGDECIMAL.toString());
  }

  /**
   * Sets the precision of an bigdecimal attribute
   *
   * @param precision Attribute precision to set
   * @throws CEPException
   */
  public void nPrecisionSet(int precision) throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.BIGDECIMAL.toString());
  }

  /**
   * Sets the scale of an bigdecimal attribute
   *
   * @param scale Attribute precision to set
   * @throws CEPException
   */
  public void nScaleSet(int scale) throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.BIGDECIMAL.toString());
  }

  /**
   * Get the value of object attribute
   *
   * @return double attribute value
   * @throws CEPException
   */
  public Object oValueGet() throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.OBJECT.toString());
  }

  public void oValueSet(Object v) throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.OBJECT.toString());
  }

  /**
   * Gets the timestamp value of the timestamp attribute
   *
   * @return Attribute value
   * @throws CEPException
   */
  public long tValueGet() throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.TIMESTAMP.toString());
  }

  /**
   * Sets the value of timestamp attribute when we have exact time
   *
   * @param ts Timestamp value
   * @throws CEPException
   */
  public void tValueSet(long ts) throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.TIMESTAMP.toString());
  }

  /**
   * Gets the timestamp format for given timestamp attribute
   *
   * @return
   * @throws CEPException
   */
  public TimestampFormat tFormatGet() throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.TIMESTAMP.toString());
  }

  /**
   * Sets the timestamp format for given timestamp attribute
   *
   * @return
   * @throws CEPException
   */
  public void tFormatSet(TimestampFormat fmt) throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.TIMESTAMP.toString());
  }

  /**
   * Sets the value of interval attribute when
   * have exact value in milliseconds (long)
   *
   * @param interval Time interval value
   * @throws CEPException
   */
  public void vValueSet(long interval) throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.INTERVAL.toString());
  }

  /**
   * Sets the value of interval attribute when
   * have exact value in milliseconds (long)
   *
   * @param interval Time interval value
   * @throws CEPException
   */
  public void vValueSet(long interval, IntervalFormat format)
          throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.INTERVAL.toString());
  }


  /**
   * Sets the value of interval attribute when
   * have exact value in String format (day to seconds)
   *
   * @param interval Time interval in String
   * @throws CEPException
   */
  public void vValueSet(String interval) throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.INTERVAL.toString());
  }

  /**
   * Sets the value of interval attribute when
   * have exact value in String format (day to seconds)
   *
   * @param interval Time interval in String
   * @throws CEPException
   * @format interval value format
   */
  public void vValueSet(String interval, IntervalFormat format)
          throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.INTERVAL.toString());
  }

  /**
   * Gets the value of interval attribute
   *
   * @return Attribute value
   * @throws CEPException
   */
  public String vValueGet() throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.INTERVAL.toString());
  }

  /**
   * Gets the long value(milliseconds) of interval attribute
   *
   * @return interval attribute value in milliseconds
   * @throws CEPException
   */
  public long intervalValGet() throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.INTERVAL.toString());
  }

  /**
   * Sets the value of interval attribute when
   * have exact value in number of months (long)
   *
   * @param interval Time interval value
   * @throws CEPException
   */
  public void vymValueSet(long interval, IntervalFormat format) throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.INTERVALYM.toString());
  }

  /**
   * Sets the value of interval attribute when
   * have exact value in String format (year to month)
   *
   * @param interval Time interval in String
   * @throws CEPException
   */
  public void vymValueSet(String interval, IntervalFormat format) throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.INTERVALYM.toString());
  }

  /**
   * Gets the value of interval year to month attribute
   *
   * @return Attribute value
   * @throws CEPException
   */
  public String vymValueGet() throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.INTERVALYM.toString());
  }

  /**
   * Gets the long value(number of months) of interval year to
   * month attribute
   *
   * @return interval attribute value in number of months
   * @throws CEPException
   */
  public long intervalYMValGet() throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.INTERVALYM.toString());
  }

  /**
   * Get the format of interval value
   *
   * @return
   * @throws CEPException
   */
  public IntervalFormat vFormatGet() throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH);
  }

  /**
   * Gets the value of an char attribute
   *
   * @return Attribute value
   * @throws CEPException
   */
  public char[] cValueGet() throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.CHAR.toString());
  }

  /**
   * Gets the value of an xmltype attribute
   *
   * @return Attribute value
   * @throws CEPException
   */
  public char[] xValueGet() throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.XMLTYPE.toString());
  }

  /**
   * Sets the value of an char attribute
   *
   * @param v Attribute value to set
   * @throws CEPException
   */
  public void cValueSet(char[] v) throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.CHAR.toString());
  }

  /**
   * Sets the value of an xmltype attribute
   *
   * @param v Attribute value to set
   * @throws CEPException
   */
  public void xValueSet(char[] v) throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.XMLTYPE.toString());
  }

  /**
   * Gets the length of an char attribute
   *
   * @return Attribute length
   * @throws CEPException
   */
  public int cLengthGet() throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.CHAR.toString());
  }

  /**
   * Gets the length of an xmltype attribute
   *
   * @return Attribute length
   * @throws CEPException
   */
  public int xLengthGet() throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.XMLTYPE.toString());
  }

  /**
   * Sets the length of an char attribute
   *
   * @param l Attribute length to set
   * @throws CEPException
   */
  public void cLengthSet(int l) throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.CHAR.toString());
  }

  /**
   * Sets the length of an xmltype attribute
   *
   * @param l Attribute length to set
   * @throws CEPException
   */
  public void xLengthSet(int l) throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.XMLTYPE.toString());
  }

  /**
   * Gets the value of an byte attribute
   *
   * @return Attribute value
   * @throws CEPException
   */
  public byte[] bValueGet() throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.BYTE.toString());
  }

  /**
   * Sets the value of an byte attribute
   *
   * @param v Attribute value to set
   * @throws CEPException
   */
  public void bValueSet(byte[] v) throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.BYTE.toString());
  }

  /**
   * Gets the length of an byte attribute
   *
   * @return Attribute length
   * @throws CEPException
   */
  public int bLengthGet() throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.BYTE.toString());
  }

  /**
   * Sets the length of an byte attribute
   *
   * @param l Attribute length to set
   * @throws CEPException
   */
  public void bLengthSet(int l) throws CEPException {
    throw new CEPException(DataStructuresError.TYPE_MISMATCH,
            attributeType.toString(), Datatype.BYTE.toString());
  }

  public abstract String toString();

  public synchronized void dump(IDumpContext dumper) {
    assert (ATTR_ATTRIBUTEVAL.length == 2);
    String tag = attributeType.toString();
    LogUtil.beginTag(dumper, tag, ATTR_ATTRIBUTEVAL, attributeName, bNull);
    if (!bNull) {
      dumper.writeln("Value", getStringValue());
    }
    dumper.endTag(tag);
  }

  protected abstract void readExternalBody(ObjectInput in) throws IOException, ClassNotFoundException;

  protected abstract void writeExternalBody(ObjectOutput in) throws IOException;

  public void readExternal(ObjectInput in)
          throws IOException, ClassNotFoundException {
    attributeName = (String) in.readObject();
    attributeType = (Datatype) in.readObject();
    bNull = in.readBoolean();
    readExternalBody(in);
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(attributeName);
    out.writeObject(attributeType);
    out.writeBoolean(bNull);
    writeExternalBody(out);
  }

  public abstract AttributeValue clone() throws CloneNotSupportedException;

  // AttributeAccessor impl
  //TODO eventually we could consolidate AttributeAccessor, TupleValue, and Tuple
  @Override
  public String getName() {
    return attributeName;
  }

  @Override
  public void setName(String name) {
    attributeName = name;
  }

  @Override
  public boolean isNull() { return bNull;}

  @Override
  public void setNull(boolean isnull) { this.bNull = isnull; }

  public static class AttrSpecWrapper implements AttrSpec, Serializable {
    Datatype dtype;
    String name;
    public AttrSpecWrapper(String name, Datatype dtype) {
      this.name = name;
      this.dtype = dtype;
    }

    @Override
    public String getName() { return name;}

    @Override
    public AttrDataType getDataType() {
      switch(dtype.kind) {
        case INT: return AttrDataType.INT;
        case BIGINT: return AttrDataType.BIGINT;
        case FLOAT: return AttrDataType.FLOAT;
        case DOUBLE: return AttrDataType.DOUBLE;
        case BOOLEAN: return AttrDataType.BOOLEAN;
        case TIMESTAMP: return AttrDataType.TIMESTAMP;
        case INTERVAL: return AttrDataType.INTERVAL;
        case INTERVALYM: return AttrDataType.INTERVAL;
        case BYTE: return AttrDataType.BYTES;
        case CHAR:  return AttrDataType.STRING;
        case BIGDECIMAL: return AttrDataType.BIGDECIMAL;
        case OBJECT: return AttrDataType.OBJECT;
      }
      throw new RuntimeException("Not supported datatype:"+dtype);
    }

    @Override
    public int getLength() { return dtype.getLength(); }

    @Override
    public int getPrecision() { return dtype.getPrecision();}

    @Override
    public int getScale() { return dtype.getLength();}

    @Override
    public RoundingMode getRoundingMode() { return dtype.getRoundingMode();}

    @Override
    public DateFormat getDateFormat() {
      if (dtype.getTimestampFormat() == null) return null;
      return dtype.getTimestampFormat().getDateFormat();
    }

    @Override
    public TimeZone getTimeZone() {
      if (dtype.getTimestampFormat() == null) return null;
      return dtype.getTimestampFormat().getTimeZone();
    }

    @Override
    public String getIntervalFormat() {
      if (dtype.getIntervalFormat() == null) return null;
      return dtype.getIntervalFormat().toSpec();
    }

    @Override
    public String getClsName() {
      Class<?> cls = dtype.getImplementationType();
      if (cls != null) return cls.getName();
      return null;
    }

  }

  @Override
  public AttrSpec getAttrSpec() { return new AttrSpecWrapper(this.getName(), this.getAttributeType());}

}
