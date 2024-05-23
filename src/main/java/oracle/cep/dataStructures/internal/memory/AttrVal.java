/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/AttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares AttrVal in package oracle.cep.execution.internals.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 sbishnoi  01/13/12 - improved timestamp support to include timezone
 sbishnoi  09/01/11 - support for interval format
 sbishnoi  08/27/11 - adding support for interval year to month
 sborah    06/17/09 - support for BigDecimal
 parujain  07/08/08 - value based windows
 skmishra  05/29/08 - debug
 hopark    02/05/08 - parameterized error
 udeshmuk  01/30/08 - support for double datatype.
 najain    10/24/07 - support XMLTYPE
 hopark    10/22/07 - remove TimeStamp
 hopark    09/04/07 - optimize
 najain    03/12/07 - bug fix
 najain    02/06/07 - coverage
 hopark    11/16/06 - add bigint datatype
 parujain  10/13/06 - interval datatype
 parujain  10/06/06 - Interval datatype
 parujain  09/25/06 - NVL Implementation
 parujain  08/31/06 - Handle Null values
 parujain  08/03/06 - Timestamp datastructure
 anasrini  07/17/06 - support for OBJECT 
 skaluska  03/27/06 - implementation
 skaluska  02/18/06 - add attrType 
 skaluska  02/13/06 - Creation
 skaluska  02/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/AttrVal.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.memory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigDecimal;

import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalFormat;
import oracle.cep.common.TimestampFormat;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.snapshot.IPersistable;
import oracle.cep.extensibility.type.IType.Kind;

/**
 * Internal representation of attribute value
 * 
 * @author skaluska
 */
public abstract class AttrVal implements Cloneable, IPersistable
{
  private static final long serialVersionUID = -4374635994600464794L;
      
  /** Attribute type */
  Datatype attrType;

  /** Whether attribute is Null */
  boolean  bNull;

  /**
   * Empty Constructor for AttrVal.
   * Invoked while deserialization of instances of AttrVal
   */
  public AttrVal()
  {}
  
  /**
   * Constructor for AttrVal
   * 
   * @param attrType
   *          Attribute type
   */
  public AttrVal(Datatype attrType)
  {
    this.attrType = attrType;
    this.bNull = false;
  }
  
  /**
   * Constructor for AttrVal
   * 
   * @param attrType
   *          Attribute type
   * @param null1
   *          Whether attribute is null
   */
  public AttrVal(Datatype attrType, boolean null1)
  {
    this.attrType = attrType;
    bNull = null1;
  }
  
  public AttrVal clone() throws CloneNotSupportedException 
  {
    return (AttrVal) super.clone();
  }

  /**
   * Getter for attrType in AttrVal
   * 
   * @return Returns the attrType
   */
  public Datatype getAttrType()
  {
    return attrType;
  }

  /**
   * Getter for bNull in AttrVal
   * 
   * @return Returns the bNull
   */
  public boolean isBNull()
  {
    return bNull;
  }

  /**
   * Setter for bNull in AttrVal
   * 
   * @param null1
   *          The bNull to set.
   */
  public void setBNull(boolean null1)
  {
    bNull = null1;
  }

  /**
   * Gets the value of an int attribute
   * 
   * @return Attribute value
   * @throws ExecException
   */
  public int iValueGet() throws ExecException
  {
    if (attrType.getKind() != Kind.INT)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.INT.toString());
    return ((IntAttrVal) this).getValue();
  }

  /**
   * Sets the value of an int attribute
   * 
   * @param v
   *          Attribute value to set
   * @throws ExecException
   */
  public void iValueSet(int v) throws ExecException
  {
    if (attrType.getKind() != Kind.INT)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.INT.toString());
    ((IntAttrVal) this).setValue(v);
  }

  /**
   * Gets the value of a bigint attribute
   * 
   * @return Attribute value
   * @throws ExecException
   */
  public long lValueGet() throws ExecException
  {
    if (attrType.getKind() != Kind.BIGINT)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.BIGINT.toString());
    return ((BigintAttrVal) this).getValue();
  }

  /**
   * Sets the value of an int attribute
   * 
   * @param v
   *          Attribute value to set
   * @throws ExecException
   */
  public void lValueSet(long v) throws ExecException
  {
    if (attrType.getKind() != Kind.BIGINT)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.BIGINT.toString());
    ((BigintAttrVal) this).setValue(v);
  }

  /**
   * Gets the value of an float attribute
   * 
   * @return Attribute value
   * @throws ExecException
   */
  public float fValueGet() throws ExecException
  {
    if (attrType.getKind() != Kind.FLOAT)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.FLOAT.toString());
    return ((FloatAttrVal) this).getValue();
  }

  /**
   * Gets the value of an double attribute
   * 
   * @return Attribute value
   * @throws ExecException
   */
  public double dValueGet() throws ExecException
  {
    if (attrType.getKind() != Kind.DOUBLE)
      throw new ExecException(ExecutionError.TYPE_MISMATCH,
          attrType.toString(), Datatype.DOUBLE.toString());
    return ((DoubleAttrVal) this).getValue();
  }
    
  /**
   * Gets the value of an BigDecimal attribute
   * 
   * @return Attribute value
   * @throws ExecException
   */
  public BigDecimal nValueGet() throws ExecException
  {
    if (attrType.getKind() != Kind.BIGDECIMAL)
      throw new ExecException(ExecutionError.TYPE_MISMATCH,
          attrType.toString(), Datatype.BIGDECIMAL.toString());
    return ((BigDecimalAttrVal) this).nValueGet();
  }
  
  /**
   * Gets the precision of a bigdecimal attribute
   * 
   * @return Attribute precision
   * @throws ExecException
   */
  public int nPrecisionGet() throws ExecException
  {
    if (attrType.getKind() != Kind.BIGDECIMAL)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.BIGDECIMAL.toString());
    return ((BigDecimalAttrVal) this).nPrecisionGet();
  }
  
  /**
   * Gets the precision of a bigdecimal attribute
   * 
   * @return Attribute precision
   * @throws ExecException
   */
  public int nScaleGet() throws ExecException
  {
    if (attrType.getKind() != Kind.BIGDECIMAL)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.BIGDECIMAL.toString());
    return ((BigDecimalAttrVal) this).nScaleGet();
  }
  
  /**
   * Gets the Float value of the attribute. This method can also convert int
   * value to float
   * 
   * @return Float Attribute value
   * @throws ExecException
   */
  public float floatValueGet() throws ExecException
  {
    switch(attrType.getKind())
    {
    case INT:
      return (float) ((IntAttrVal) this).getValue();
    case BIGINT:
      return (float) ((BigintAttrVal) this).getValue();
    case FLOAT:
      return ((FloatAttrVal) this).getValue();
    default:
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), 
          Datatype.FLOAT.toString() +"," + Datatype.INT.toString() + "," + Datatype.BIGINT.toString());
    }
  }

  /**
   * Gets the Double value of the attribute. This method can also convert int
   * value to double
   * 
   * @return Double Attribute value
   * @throws ExecException
   */
  public double doubleValueGet() throws ExecException
  {
    switch(attrType.getKind())
    {
    case INT:
      return (double) ((IntAttrVal) this).getValue();
    case BIGINT:
      return (double) ((BigintAttrVal) this).getValue();
    case FLOAT:
      return (double) ((FloatAttrVal) this).getValue();
    case DOUBLE:
      return ((DoubleAttrVal) this).getValue();
    default:
      throw new ExecException(ExecutionError.TYPE_MISMATCH,
          attrType.toString(), 
          Datatype.DOUBLE.toString() +"," + Datatype.INT.toString() + "," + Datatype.BIGINT.toString()+ "," + Datatype.FLOAT.toString()
          );
    }
  }
  
  /**
   *  Gets the double value of an attribute only if it is double or float
   * @return double value of the attribute
   * @throws ExecException
   */
  public double dblValueGet() throws ExecException
  {
   switch(attrType.getKind())
    {
    case FLOAT:
      return (double) ((FloatAttrVal) this).getValue();
    case DOUBLE:
      return ((DoubleAttrVal) this).getValue();
    default:
      throw new ExecException(ExecutionError.TYPE_MISMATCH,
          attrType.toString(), 
          Datatype.DOUBLE.toString() +"," + Datatype.FLOAT.toString() 
          ); 
    }
  }
  
  /**
   * Gets the long value of the attribute (int, bigint, timestamp and interval)
   * @return Long value
   * @throws ExecException
   */
  public long longValueGet() throws ExecException
  {
    switch(attrType.getKind())
    {
    case INT:
      return (long) ((IntAttrVal) this).getValue();
    case BIGINT:
      return ((BigintAttrVal) this).getValue();
    case TIMESTAMP:
      return ((TimestampAttrVal)this).getTime();
    case INTERVALYM:
      return ((IntervalYMAttrVal)this).getInterval();
    case INTERVAL:
      return ((IntervalAttrVal)this).getInterval();
    default:
      throw new ExecException(ExecutionError.TYPE_MISMATCH,
          attrType.toString(), 
          Datatype.BIGINT.toString() +"," + Datatype.INT.toString() + "," + Datatype.INTERVAL.toString()+ "," + Datatype.TIMESTAMP.toString() + "," + Datatype.INTERVALYM.toString()
          );  
    }
  }
  
  /**
   * Gets the BigDecimal value of the attribute (int, bigint, timestamp, 
   * interval, float and double)
   * @return BigDecimal value
   * @throws ExecException
   */
  public BigDecimal bigDecimalValueGet() throws ExecException
  {
    switch(attrType.getKind())
    {
      case INT:
        return new BigDecimal(String.valueOf(((IntAttrVal) this).getValue()));
      case BIGINT:
        return new BigDecimal(String.valueOf(((BigintAttrVal) this).getValue()));
      case TIMESTAMP:
        return new BigDecimal(String.valueOf(((TimestampAttrVal)this).getTime()));
      case INTERVAL: 
        return new BigDecimal(String.valueOf(((IntervalAttrVal)this).getInterval()));
      case INTERVALYM:
        return new BigDecimal(String.valueOf(((IntervalYMAttrVal)this).getInterval()));
      case FLOAT:
        return new BigDecimal(String.valueOf(((FloatAttrVal)this).getValue()));
      case DOUBLE :
        return new BigDecimal(String.valueOf(((DoubleAttrVal)this).getValue()));
      case BIGDECIMAL:
        return ((BigDecimalAttrVal)this).nValueGet();
      default:
        throw new ExecException(ExecutionError.TYPE_MISMATCH,
            attrType.toString(),
            Datatype.BIGINT.toString() +"," + Datatype.INT.toString() + "," + Datatype.INTERVAL.toString()+ "," +
            Datatype.TIMESTAMP.toString() + "," + Datatype.INTERVALYM.toString() + 
            Datatype.FLOAT.toString() +"," + Datatype.DOUBLE.toString() + "," + Datatype.BIGDECIMAL.toString()+ "," 
            );
        
    }
  }
  
  
  /**
   * Sets the value of an float attribute
   * 
   * @param v
   *          Attribute value to set
   * @throws ExecException
   */
  public void fValueSet(float v) throws ExecException
  {
    if (attrType.getKind() != Kind.FLOAT)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.FLOAT.toString());
    ((FloatAttrVal) this).setValue(v);
  }

  /**
   * Sets the value of a double attribute
   * 
   * @param v
   *          Attribute value to set
   * @throws ExecException
   */
  public void dValueSet(double v) throws ExecException
  {
    if (attrType.getKind() != Kind.DOUBLE)
      throw new ExecException(ExecutionError.TYPE_MISMATCH,
          attrType.toString(), Datatype.DOUBLE.toString());
    ((DoubleAttrVal) this).setValue(v);
  }

  /**
   * Sets the value of a BigDecimal attribute
   * 
   * @param v
   *          Attribute value to set
   * @throws ExecException
   */
  public void nValueSet(BigDecimal v, int precision, int scale) throws ExecException
  {
    if (attrType.getKind() != Kind.BIGDECIMAL)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, attrType.getKind(), Datatype.BIGDECIMAL.toString());
    ((BigDecimalAttrVal) this).nValueSet(v, precision, scale);
  }

  /**
   * Gets the value of timestamp
   * 
   * @return Timestamp attribute value
   * @throws ExecException
   */
  public long tValueGet() throws ExecException
  {
    if (attrType.getKind() != Kind.TIMESTAMP)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.TIMESTAMP.toString());
    return ((TimestampAttrVal) this).getTime();
  }

  /**
   * Sets the value of timestamp attribute
   * 
   * @param ts
   *          Timestamp attribute
   * @throws ExecException
   */
  public void tValueSet(long ts) throws ExecException
  {
    if (attrType.getKind() != Kind.TIMESTAMP)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.TIMESTAMP.toString());
    ((TimestampAttrVal) this).setTime(ts);
  }
  
  /**
   * Sets the format of timestamp attribute
   * 
   * @param format
   *          Timestamp format
   * @throws ExecException
   */
  public void tFormatSet(TimestampFormat tsFormat) throws ExecException
  {
    if (attrType.getKind() != Kind.TIMESTAMP)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.TIMESTAMP.toString());
    ((TimestampAttrVal) this).setFormat(tsFormat);
  }
  
  /**
   * Gets the format of timestamp value
   * 
   * @return Timestamp attribute format
   * @throws ExecException
   */
  public TimestampFormat tFormatGet() throws ExecException
  {
    if (attrType.getKind() != Kind.TIMESTAMP)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.TIMESTAMP.toString());
    return ((TimestampAttrVal) this).getFormat();
  }
  
  /**
   * Sets the value of the interval day to second attribute
   * 
   * @param interval
   *          Interval attribute
   * @throws ExecException
   */
  public void vValueSet(long interval, IntervalFormat format) throws ExecException
  {
    if (attrType.getKind() != Kind.INTERVAL)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.INTERVAL.toString());
    ((IntervalAttrVal) this).setInterval(interval);
    ((IntervalAttrVal) this).setFormat(format);
  }
 

  /**
   * Gets the value of interval day to second attribute
   * 
   * @return Interval attribute
   * @throws ExecException
   */
  public long vValueGet() throws ExecException
  {
    if (attrType.getKind() != Kind.INTERVAL)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.INTERVAL.toString());
    return ((IntervalAttrVal) this).getInterval();
  }
  
  /**
   * Sets the value of the interval year to month attribute
   * 
   * @param interval
   *          Interval attribute
   * @throws ExecException
   */
  public void vymValueSet(long interval, IntervalFormat format) throws ExecException
  {
    if (attrType.getKind() != Kind.INTERVALYM)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.INTERVALYM.toString());
    ((IntervalYMAttrVal) this).setInterval(interval);
    ((IntervalYMAttrVal)this).setFormat(format);
  }

  /**
   * Gets the value of interval year to month attribute
   * 
   * @return Interval attribute
   * @throws ExecException
   */
  public long vymValueGet() throws ExecException
  {
    if (attrType.getKind() != Kind.INTERVALYM)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.INTERVALYM.toString());
    return ((IntervalYMAttrVal) this).getInterval();
  }
  
  /**
   * Get Interval Format of interval attribute value
   */
  public IntervalFormat vFormatGet() throws ExecException
  {
    if (attrType.getKind() != Kind.INTERVALYM && 
        attrType.getKind() != Kind.INTERVAL)
    {
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.INTERVAL.toString() + " OR "+
          Datatype.INTERVALYM.toString());
    }
    if(this instanceof IntervalAttrVal)
      return ((IntervalAttrVal) this).format;
    else
      return ((IntervalYMAttrVal) this).format;
  }

  /**
   * Gets the value of an char attribute
   * 
   * @return Attribute value
   * @throws ExecException
   */
  public char[] cValueGet() throws ExecException
  {
    if (attrType.getKind() != Kind.CHAR)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.CHAR.toString());
    return ((CharAttrVal) this).getValue();
  }

  /**
   * Gets the length of an char attribute
   * 
   * @return Attribute length
   * @throws ExecException
   */
  public int cLengthGet() throws ExecException
  {
    if (attrType.getKind() != Kind.CHAR)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.CHAR.toString());
    return ((CharAttrVal) this).getLength();
  }

  /**
   * Gets the value of an xmltype attribute
   * 
   * @return Attribute value
   * @throws ExecException
   */
  public char[] xValueGet() throws ExecException
  {
    if (attrType.getKind() != Kind.XMLTYPE)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.XMLTYPE.toString());
    return ((XmltypeAttrVal) this).getValue();
  }

  /**
   * Gets the length of an xmltype attribute
   * 
   * @return Attribute length
   * @throws ExecException
   */
  public int xLengthGet() throws ExecException
  {
    if (attrType.getKind() != Kind.XMLTYPE)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.XMLTYPE.toString());
    return ((XmltypeAttrVal) this).getLength();
  }

  /**
   * Sets the value of an char attribute
   * 
   * @param v
   *          Attribute value to set
   * @param l
   *          Attribute length
   * @throws ExecException
   */
  public void cValueSet(char[] v, int l) throws ExecException
  {
    if (attrType.getKind() != Kind.CHAR)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.CHAR.toString());
    ((CharAttrVal) this).setValue(v, l);
  }

  /**
   * Sets the value of an xmltype attribute
   * 
   * @param v
   *          Attribute value to set
   * @param l
   *          Attribute length
   * @throws ExecException
   */
  public void xValueSet(char[] v, int l) throws ExecException
  {
    if (attrType.getKind() != Kind.XMLTYPE)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.XMLTYPE.toString());
    ((XmltypeAttrVal) this).setValue(v, l);
  }

  public void xValueSet(Object o) throws ExecException
  {
    if (attrType.getKind() != Kind.XMLTYPE)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, attrType.toString(), Datatype.XMLTYPE.toString());
    ((XmltypeAttrVal) this).setValue(o);
  }

  public Object getItem(Object ctx) throws Exception
  {
    if (attrType.getKind() != Kind.XMLTYPE)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, attrType.toString(), Datatype.XMLTYPE.toString());
    return ((XmltypeAttrVal) this).getItem(ctx);
  }

  /**
   * Gets the value of an byte attribute
   * 
   * @return Attribute value
   * @throws ExecException
   */
  public byte[] bValueGet() throws ExecException
  {
    if (attrType.getKind() != Kind.BYTE)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.BYTE.toString());
    return ((ByteAttrVal) this).getValue();
  }

  /**
   * Gets the length of a byte attribute
   * 
   * @return Attribute length
   * @throws ExecException
   */
  public int bLengthGet() throws ExecException
  {
    if (attrType.getKind() != Kind.BYTE)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.BYTE.toString());
    return ((ByteAttrVal) this).getLength();
  }

  /**
   * Sets the value of an byte attribute
   * 
   * @param v
   *          Attribute value to set
   * @param l
   *          Attribute length
   * @throws ExecException
   */
  public void bValueSet(byte[] v, int l) throws ExecException
  {
    if (attrType.getKind() != Kind.BYTE)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.BYTE.toString());
    ((ByteAttrVal) this).setValue(v, l);
  }

  /**
   * Gets the value of an Object attribute
   * 
   * @return Attribute value
   * @throws ExecException
   */
  public Object oValueGet() throws ExecException
  {
    if (attrType.getKind() != Kind.OBJECT)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.OBJECT.toString());
    return ((ObjectAttrVal) this).getValue();
  }

  /**
   * Sets the value of an Object attribute
   * 
   * @param v
   *          Attribute value to set
   * @throws ExecException
   */
  public void oValueSet(Object v) throws ExecException
  {
    if (attrType.getKind() != Kind.OBJECT)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.OBJECT.toString());
    ((ObjectAttrVal) this).setValue(v);
  }

  /**
   * Gets the value of an Boolean attribute
   * 
   * @return Attribute value
   * @throws ExecException
   */
  public boolean boolValueGet() throws ExecException
  {
    if (attrType.getKind() != Kind.BOOLEAN)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.BOOLEAN.toString());
    return ((BooleanAttrVal) this).getValue();
  }

  /**
   * Sets the value of an boolean attribute
   * 
   * @param v
   *          Attribute value to set
   * @throws ExecException
   */
  public void boolValueSet(boolean v) throws ExecException
  {
    if (attrType.getKind() != Kind.BOOLEAN)
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), Datatype.BOOLEAN.toString());
    ((BooleanAttrVal) this).setValue(v);
  }
  
  /**
   * Copies the value of an attribute
   * 
   * @param v
   *          Attribute value to copy
   * @throws ExecException
   */
  public void copy(AttrVal v) throws ExecException
  {
    if (attrType.getKind() != v.attrType.getKind())
      throw new ExecException(ExecutionError.TYPE_MISMATCH, 
          attrType.toString(), v.attrType.toString());
    
    // During copying the attribute value, also copy the interval format 
    IntervalFormat destinationFormat = null;
    
    switch (attrType.getKind())
    {
      case INT:
        this.iValueSet(v.iValueGet());
        break;
      case BIGINT:
        this.lValueSet(v.lValueGet());
        break;
      case FLOAT:
        this.fValueSet(v.fValueGet());
        break;
      case DOUBLE:
        this.dValueSet(v.dValueGet());
        break;
      case CHAR:
        this.cValueSet(v.cValueGet(), v.cLengthGet());
        break;
      case BYTE:
        this.bValueSet(v.bValueGet(), v.bLengthGet());
        break;
      case TIMESTAMP:
        this.tValueSet(v.tValueGet());
        this.tFormatSet(v.tFormatGet());
        break;
      case INTERVAL:
        destinationFormat = ((IntervalAttrVal)v).getFormat();
        this.vValueSet(v.vValueGet(), destinationFormat);
        break;
      case INTERVALYM:
        destinationFormat = ((IntervalYMAttrVal)v).getFormat();
        this.vymValueSet(v.vymValueGet(), destinationFormat);
        break;
      case OBJECT:
        this.oValueSet(v.oValueGet());
        break;
      default:
        assert false;
        break;
    }
  }

  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeObject(this.attrType);
    out.writeBoolean(this.bNull);
  }
  
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    this.attrType = (Datatype) in.readObject();
    this.bNull    = in.readBoolean();
  }

  @Override
  public String toString()
  {
    return "AttrVal [attrType=" + (attrType == null ? "null" : attrType) + ", bNull=" + bNull + "]";
  }
}
