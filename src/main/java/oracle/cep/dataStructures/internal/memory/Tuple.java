/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/Tuple.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2007, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 pkali     09/20/13 - return avg as null if first input is null (Bug 17194899)
 sbishnoi  08/19/13 - bug 17317114
 pkali     07/30/13 - initialized IntervalYMAttrVal if it is null (bug
                      17201416)
 sbishnoi  07/30/13 - bug 17180183
 sbishnoi  02/22/12 - fix apple bug
 sbishnoi  01/12/12 - implement new interfaces related to timestamp
 sbishnoi  11/07/11 - formatting timestamp value
 udeshmuk  10/12/11 - XbranchMerge udeshmuk_bug-13060688_ps5 from
                      st_pcbpel_11.1.1.4.0
 udeshmuk  10/10/11 - XbranchMerge udeshmuk_bug-11933156_ps5 from
 sbishnoi  10/03/11 - changing format to intervalformat
 udeshmuk  09/26/11 - clone objectattrval if it is cloneable
 sbishnoi  08/29/11 - adding support for interval year to month
 sbishnoi  08/27/11 - adding support for interval year to month
 vikshukl  02/21/11 - XbranchMerge vikshukl_bug-10145509_ps3 from
                      st_pcbpel_11.1.1.4.0
 udeshmuk  11/11/10 - support for to_bigint(timestamp)
 vikshukl  09/26/10 - XbranchMerge vikshukl_bug-10145509_ps3 from main
 sborah    04/08/10 - char to number functions
 sborah    02/09/10 - equality op for xmltype
 hopark    12/02/09 - fix copyTo
 hopark    10/30/09 - support large string
 sborah    06/22/09 - support for BigDecimal
 sborah    06/01/09 - support for xmltype in to_char
 hopark    04/09/09 - add copy
 hopark    03/16/09 - add obj heval
 hopark    02/17/09 - support boolean as external datatype
 hopark    02/17/09 - add OBJ_CPY - objtype support
 hopark    02/02/09 - objtype support
 sborah    02/11/09 - support for is_not_null
 hopark    11/28/08 - use CEPDateFormat
 skmishra  08/22/08 - byteToHEx throws CEPException
 sbishnoi  07/10/08 - fix systimestamp bug
 parujain  07/08/08 - value based windows
 sbishnoi  06/24/08 - modifying length() to return result null if input
                      attribute is null
 sbishnoi  06/20/08 - support of to_char for other datatypes
 skmishra  06/06/08 - bug
 sbishnoi  06/19/08 - adding support for to_char(integer)
 skmishra  06/06/08 - bug
 parujain  06/03/08 - support xmltype for heval
 parujain  05/16/08 - fix compare for xmltype
 hopark    05/16/08 - fix xmltype copy
 parujain  05/12/08 - getItem from tuple
 sbishnoi  04/24/08 - modifying mod calculation to include case where divisor
                      is zero
 sbishnoi  04/21/08 - support for modulus function
 sbishnoi  03/17/08 - modify div operations to throw DIVIDE_BY_ZERO error
 hopark    02/05/08 - parameterized error
 najain    02/04/08 - object representation of xml
 udeshmuk  01/30/08 - support for double data type.
 sbishnoi  01/20/08 - adding support for built-in char functions
 hopark    01/11/08 - 
 udeshmuk  01/17/08 - change in the data type of time field of TupleValue.
 najain    10/24/07 - add xmltype
 hopark    10/23/07 - remove TimeStamp
 udeshmuk  10/16/07 - commenting code that supports sum(interval).
 udeshmuk  10/12/07 - support for max and min on char and byte data types.
 hopark    09/04/07 - optimize
 hopark    07/12/07 - add compare
 parujain  06/29/07 - getAttr
 hopark    06/20/07 - cleanup
 hopark    06/17/07 - fix toString for emptyString
 najain    05/11/07 - variable length support
 hopark    03/08/07 - move externalization code to stored.Tuple
 najain    03/12/07 - bug fix
 hopark    01/23/07 - spill-over support
 hopark    12/20/06 - add toString, logging
 hopark    11/16/06 - add bigint datatype
 parujain  10/06/06 - Interval datatype
 parujain  09/25/06 - NVL Implementation
 parujain  08/30/06 - Handle Null values
 parujain  08/03/06 - Timestamp datastructure
 najain    07/19/06 - ref-count tuples
 anasrini  07/17/06 - support for OBJECT type 
 najain    07/13/06 - ref-count timeStamp support 
 najain    04/18/06 - time is a part of tuple 
 najain    03/14/06 - add getId
 skaluska  02/08/06 - Creation
 skaluska  02/08/06 - Creation
 najain      03/20/07 - Creation
 */

package oracle.cep.dataStructures.internal.memory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import oracle.cep.common.CEPDateFormat;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalFormat;
import oracle.cep.common.TimeUnit;
import oracle.cep.common.TimestampFormat;
import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.dataStructures.internal.memory.AttrVal;
import oracle.cep.dataStructures.internal.memory.BigintAttrVal;
import oracle.cep.dataStructures.internal.memory.ByteAttrVal;
import oracle.cep.dataStructures.internal.memory.CharAttrVal;
import oracle.cep.execution.internals.Column;
import oracle.cep.dataStructures.internal.memory.FloatAttrVal;
import oracle.cep.dataStructures.internal.memory.IntAttrVal;
import oracle.cep.dataStructures.internal.memory.BooleanAttrVal;
import oracle.cep.dataStructures.internal.memory.IntervalAttrVal;
import oracle.cep.dataStructures.internal.memory.IntervalYMAttrVal;
import oracle.cep.dataStructures.internal.memory.ObjectAttrVal;
import oracle.cep.dataStructures.internal.memory.TimestampAttrVal;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

/**
 * @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/Tuple.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 * @author najain
 * @since release specific (what release of product did this appear in)
 */
public class Tuple extends TupleBase
{
  static final long   serialVersionUID = 3530554457754365944L;

  /** Array of attribute values */
  protected AttrVal[] attrs;

  /**
   * Constructor for Tuple
   */
  public Tuple()
  {
    super();
    attrs = null;
  }
  /**
   * Constructor for Tuple
   * 
   * @param max
   *          Maximum number of attributes
   */
  public Tuple(int max)
  {
    super();
    attrs = new AttrVal[max];
  }

  public void init(TupleSpec spec, boolean nullValue) throws ExecException
  {
    int max = spec.getNumAttrs();
    if (attrs == null || attrs.length != max)
      attrs = new AttrVal[max];

    // Allocate attributes
    for (int i = 0; i < max; i++)
    {
      switch (spec.getAttrType(i).getKind())
      {
        case INT:
          attrs[i] = new IntAttrVal(0);
          break;
        case BIGINT:
          attrs[i] = new BigintAttrVal(0);
          break;
        case FLOAT:
          attrs[i] = new FloatAttrVal(0);
          break;
        case DOUBLE:
          attrs[i] = new DoubleAttrVal(0);
          break;
        case BIGDECIMAL:
          attrs[i] = new BigDecimalAttrVal(BigDecimal.ZERO);
          break;
        case CHAR:
          attrs[i] = new CharAttrVal(spec.getAttrLen(i));
          break;
        case XMLTYPE:
          attrs[i] = new XmltypeAttrVal();
          break;
        case BYTE:
          attrs[i] = new ByteAttrVal(spec.getAttrLen(i));
          break;
        case OBJECT:
          attrs[i] = new ObjectAttrVal(null);
          break;
        case TIMESTAMP:
          attrs[i] = new TimestampAttrVal();
          break;
        case INTERVAL:
          attrs[i] = new IntervalAttrVal(0, spec.getAttrMetadata(i).getIntervalFormat());
          break;
        case INTERVALYM:
          attrs[i] = new IntervalYMAttrVal(0, spec.getAttrMetadata(i).getIntervalFormat());
          break;
        case BOOLEAN:
          attrs[i] = new BooleanAttrVal(true);
          break;
        default:
          assert false;
      }
      if (nullValue)
        setAttrNull(i);
    }
  }

  public int getNumAttrs()
  {
    return attrs.length;
  }

  public Datatype getAttrType(int pos)
  {
    return attrs[pos].getAttrType();
  }

  /**
   * Return true if the Value of the attribute is null
   * 
   * @param position
   *          Position of the attribute
   * @return True if Null else False
   * @throws ExecException
   */
  public boolean isAttrNull(int position) throws ExecException
  {
    if (position >= attrs.length)
    {
      throw new ExecException(ExecutionError.INVALID_ATTR, (ExecOpt)null, getRef(), 
            position, attrs.length);
    }
    return attrs[position].bNull;
  }

  /**
   * Sets the Attribute to be null
   * 
   * @param position
   *          Position of the attribute in the tuple
   * @throws ExecException
   */
  public void setAttrNull(int position) throws ExecException
  {
    if (position >= attrs.length)
      throw new ExecException(ExecutionError.INVALID_ATTR, null, getRef());

    attrs[position].bNull = true;
  }

  /**
   * Resets the value of bNull to false. This is the responsibility of the
   * caller to reset Attribute bNull if the value becomes non-null
   * 
   * @param position
   *          Postion of interest
   * @throws ExecException
   */
  public void setAttrbNullFalse(int position) throws ExecException
  {
    if (position >= attrs.length)
      throw new ExecException(ExecutionError.INVALID_ATTR, null, getRef());

    attrs[position].setBNull(false);
  }

  /**
   * Gets the value of an int attribute
   * 
   * @param position
   *          Position of interest
   * @return Attribute value
   * @throws ExecException
   */
  public int iValueGet(int position) throws ExecException
  {
    assert attrs[position].isBNull() == false;
    return ((IntAttrVal) attrs[position]).value;
  }

  /**
   * Sets the value of an int attribute
   * 
   * @param position
   *          Position of interest
   * @param v
   *          Attribute value to set
   * @throws ExecException
   */
  public void iValueSet(int position, int v) throws ExecException
  {
    attrs[position].bNull = false;
    ((IntAttrVal) attrs[position]).value = v;
  }

  /**
   * Gets the value of an boolean attribute
   * 
   * @param position
   *          Position of interest
   * @return Attribute value
   * @throws ExecException
   */
  public boolean boolValueGet(int position) throws ExecException
  {
    assert attrs[position].isBNull() == false;
    return ((BooleanAttrVal) attrs[position]).value;
  }

  /**
   * Sets the value of an boolean attribute
   * 
   * @param position
   *          Position of interest
   * @param v
   *          Attribute value to set
   * @throws ExecException
   */
  public void boolValueSet(int position, boolean v) throws ExecException
  {
    attrs[position].bNull = false;
    ((BooleanAttrVal) attrs[position]).value = v;
  }

  /**
   * Gets the value of an bigint attribute
   * 
   * @param position
   *          Position of interest
   * @return Attribute value
   * @throws ExecException
   */
  public long lValueGet(int position) throws ExecException
  {
    assert attrs[position].isBNull() == false;
    return ((BigintAttrVal) attrs[position]).value;
  }

  /**
   * Sets the value of an int attribute
   * 
   * @param position
   *          Position of interest
   * @param v
   *          Attribute value to set
   * @throws ExecException
   */
  public void lValueSet(int position, long v) throws ExecException
  {
    attrs[position].bNull = false;
    ((BigintAttrVal) attrs[position]).value = v;
  }

  /**
   * Sets the value of an int attribute
   * 
   * @param position
   *          Position of interest
   * @return Attribute value
   * @throws ExecException
   */
  public float fValueGet(int position) throws ExecException
  {
    assert attrs[position].isBNull() == false;
    return ((FloatAttrVal) attrs[position]).value;
  }

  /**
   * Gets the float value of an attribute (either int or float)
   * 
   * @param position
   *          Position of interest
   * @return Float Attibute value
   * @throws ExecException
   */
  public float floatValueGet(int position) throws ExecException
  {
    assert attrs[position].isBNull() == false;
    return attrs[position].floatValueGet();
  }

  /**
   * Sets the value of an float attribute
   * 
   * @param position
   *          Position of interest
   * @param v
   *          Attribute value to set
   * @throws ExecException
   */
  public void fValueSet(int position, float v) throws ExecException
  {
    attrs[position].bNull = false;
    ((FloatAttrVal) attrs[position]).value = v;
  }

  /**
   * Sets the value of a double attribute
   * 
   * @param position
   *          Position of interest
   * @return Attribute value
   * @throws ExecException
   */
  public double dValueGet(int position) throws ExecException
  {
    assert attrs[position].isBNull() == false;
    return ((DoubleAttrVal) attrs[position]).value;
  }

  /**
   * Gets the double value of an attribute (either int or float)
   * 
   * @param position
   *          Position of interest
   * @return Double Attribute value
   * @throws ExecException
   */
  public double doubleValueGet(int position) throws ExecException
  {
    assert attrs[position].isBNull() == false;
    return attrs[position].doubleValueGet();
  }
  
  /**
   * Gets the double value of an attribute (either float or double)
   * @param position
   *          Position of interest
   * @return Double Attribute value
   * @throws ExecException
   */
  public double dblValueGet(int position) throws ExecException
  {
    assert attrs[position].isBNull() == false;
    return attrs[position].dblValueGet();
  }
  
  
  /**
   * Sets the value of a BigDecimal attribute
   * 
   * @param position
   *          Position of interest
   * @return BigDecimal Attribute value
   * @throws ExecException
   */
  public BigDecimal nValueGet(int position) throws ExecException
  {
    assert attrs[position].isBNull() == false;
    return ((BigDecimalAttrVal) attrs[position]).nValueGet();
  }
  
  /**
   * Gets the precision of a bigdecimal attribute
   * 
   * @param position
   *          Position of interest
   * @return Attribute length
   * @throws ExecException
   */
  public int nPrecisionGet(int position) throws ExecException
  {
    return ((BigDecimalAttrVal) attrs[position]).nPrecisionGet();
  }
  
  /**
   * Gets the precision of a bigdecimal attribute
   * 
   * @param position
   *          Position of interest
   * @return Attribute length
   * @throws ExecException
   */
  public int nScaleGet(int position) throws ExecException
  {
    return ((BigDecimalAttrVal) attrs[position]).nScaleGet();
  }
  
  /**
   * Gets the BigDecimal value of an attribute (int, bigint, timestamp, 
   * interval, float and double)
   * @param position
   *          Position of interest
   * @return BigDecimal Attribute value
   * @throws ExecException
   */
  public BigDecimal bigDecimalValueGet(int position) throws ExecException
  {
    assert attrs[position].isBNull() == false;
    return ((BigDecimalAttrVal)attrs[position]).bigDecimalValueGet();
  }
  
  /**
   * Gets the long value of an attribute (int, bigint, timestamp or interval)
   * @param position
   *          Position of interest
   * @return long value
   * @throws ExecException
   */
  public long longValueGet(int position) throws ExecException
  {
    assert attrs[position].isBNull() == false;
    return attrs[position].longValueGet();
  }

  /**
   * Sets the value of a double attribute
   * 
   * @param position
   *          Position of interest
   * @param v
   *          Attribute value to set
   * @throws ExecException
   */
  public void dValueSet(int position, double v) throws ExecException
  {
    attrs[position].bNull = false;
    ((DoubleAttrVal) attrs[position]).value = v;
  }

  /**
   * Sets the value of a BigDecimal attribute
   * 
   * @param position
   *          Position of interest
   * @param v
   *          Attribute value to set
   * @throws ExecException
   */
  public void nValueSet(int position, BigDecimal v, int precision, int scale) 
  throws ExecException
  {
    attrs[position].bNull = false;
    ((BigDecimalAttrVal) attrs[position]).nValueSet(v, precision, scale);
  }

  /**
   * Returns the timestamp value of the attribute
   * 
   * @param position
   *          Position of interest
   * @return Long value of timestamp attribute
   * @throws ExecException
   */
  public long tValueGet(int position) throws ExecException
  {
    assert attrs[position].isBNull() == false;
    return ((TimestampAttrVal)attrs[position]).time;
  }

  /**
   * Sets the value of the TimeStamp attribute
   * 
   * @param position
   *          Position of interest
   * @param ts
   *          Timestamp from which value needs to be extracted in milliseconds
   * @throws ExecException
   */
  public void tValueSet(int position, Timestamp ts) throws ExecException
  {
    attrs[position].bNull = false;
    ((TimestampAttrVal)attrs[position]).time = ts.getTime() * 1000000l;
  }  
  
  /**
   * Sets the value of the TimeStamp attribute
   * 
   * @param position
   *          Position of interest
   * @param ts
   *          Timestamp value which needs to be saved
   * @throws ExecException
   */
  public void tValueSet(int position, long ts) throws ExecException
  {
    attrs[position].bNull = false;
    ((TimestampAttrVal)attrs[position]).time = ts;
  }
  
  /**
   * Sets the value of the timestamp attribute
   * @param position position of interest
   * @param format format of timestamp value
   * @throws ExecException
   */
  @Override
  public void tFormatSet(int position, TimestampFormat format)
      throws ExecException
  {
    attrs[position].bNull = false;
    ((TimestampAttrVal)attrs[position]).setFormat(format); 
  }  
  
  /**
   * Get the timestamp format
   * @param position position of interest
   */
  @Override
  public TimestampFormat tFormatGet(int position) throws ExecException
  {
    return ((TimestampAttrVal)attrs[position]).getFormat();
  }

  /**
   * Sets the value of the interval attribute
   * 
   * @param position
   *          Position of interest
   * @param interval
   *          Interval value
   * @param format
   *          Interval format
   * @throws ExecException
   */
  public void vValueSet(int position, long interval, IntervalFormat format) throws ExecException
  {
    attrs[position].bNull = false;
    ((IntervalAttrVal) attrs[position]).interval = interval;
    ((IntervalAttrVal) attrs[position]).format   = format;
  }

  /**
   * Gets the interval attribute value
   * 
   * @param position
   *          Position of interval
   * @return Interval value
   * @throws ExecException
   */
  public long vValueGet(int position) throws ExecException
  {
    assert attrs[position].isBNull() == false;
    return ((IntervalAttrVal) attrs[position]).interval;
  }
  
  /**
   * Sets the value of the interval year to month attribute
   * 
   * @param position
   *          Position of interest
   * @param interval
   *          Interval value
   * @throws ExecException
   */
  public void vymValueSet(int position, long interval, IntervalFormat format) throws ExecException
  {
    attrs[position].bNull = false;
    ((IntervalYMAttrVal) attrs[position]).interval = interval;
    ((IntervalYMAttrVal) attrs[position]).format   = format;
  }

  /**
   * Gets the interval attribute year to month value
   * 
   * @param position
   *          Position of interval
   * @return Interval value
   * @throws ExecException
   */
  public long vymValueGet(int position) throws ExecException
  {
    assert attrs[position].isBNull() == false;
    return ((IntervalYMAttrVal) attrs[position]).interval;
  }
  
  @Override
  /**
   * Gets the interval format
   */
  public IntervalFormat vFormatGet(int position) throws ExecException
  {
    if(attrs[position] instanceof IntervalAttrVal)
      return ((IntervalAttrVal)attrs[position]).getFormat();
    else if(attrs[position] instanceof IntervalYMAttrVal)
      return ((IntervalYMAttrVal)attrs[position]).getFormat();
    else
      assert false;
    return null;
  }

  /**
   * Gets the value of an char attribute
   * 
   * @param position
   *          Position of interest
   * @return Attribute value
   * @throws ExecException
   */
  public char[] cValueGet(int position) throws ExecException
  {
    assert attrs[position].isBNull() == false;
    return ((CharAttrVal) attrs[position]).value;
  }

  public boolean xIsObj(int position) throws ExecException
  {
    return ((XmltypeAttrVal) attrs[position]).isObject;
  }
  
  /**
   * Gets the length of a xmltype attribute
   * 
   * @param position
   *          Position of interest
   * @return Attribute length
   * @throws ExecException
   */
  public int xLengthGet(int position) throws ExecException
  {
    return ((XmltypeAttrVal) attrs[position]).length;
  }

  /**
   * Gets the value of an xmltype attribute
   * 
   * @param position
   *          Position of interest
   * @return Attribute value
   * @throws ExecException
   */
  public char[] xValueGet(int position) throws ExecException
  {
    assert attrs[position].isBNull() == false;
    return ((XmltypeAttrVal) attrs[position]).getValue();
  }

  /**
   * Gets the length of a char attribute
   * 
   * @param position
   *          Position of interest
   * @return Attribute length
   * @throws ExecException
   */
  public int cLengthGet(int position) throws ExecException
  {
    return ((CharAttrVal) attrs[position]).length;
  }

  /**
   * Sets the value of an char attribute
   * 
   * @param position
   *          Position of interest
   * @param v
   *          Attribute value to set
   * @param l
   *          Attribute length
   * @throws ExecException
   */
  public void cValueSet(int position, char[] v, int l) throws ExecException
  {
    attrs[position].bNull = false;
    attrs[position].cValueSet(v, l);
  }

  /**
   * Sets the value of an xmltype attribute
   * 
   * @param position
   *          Position of interest
   * @param v
   *          Attribute value to set
   * @param l
   *          Attribute length
   * @throws ExecException
   */
  public void xValueSet(int position, char[] v, int l) throws ExecException
  {
    attrs[position].bNull = false;
    attrs[position].xValueSet(v, l);
  }

  /**
   * Sets the value of an xmltype attribute
   * 
   * @param position
   *          Position of interest
   * @param o
   *          Object value to set
   * @throws ExecException
   */
  public void xValueSet(int position, Object o) throws ExecException
  {
    attrs[position].bNull = false;
    attrs[position].xValueSet(o);
  }

  public Object getItem(int position, Object ctx) throws Exception
  {
    return attrs[position].getItem(ctx);
  }
  
  /**
   * Gets the value of an byte attribute
   * 
   * @param position
   *          Position of interest
   * @return Attribute value
   * @throws ExecException
   */
  public byte[] bValueGet(int position) throws ExecException
  {
    assert attrs[position].isBNull() == false;
    return ((ByteAttrVal) attrs[position]).value;
  }

  /**
   * Gets the length of a byte attribute
   * 
   * @param position
   *          Position of interest
   * @return Attribute length
   * @throws ExecException
   */
  public int bLengthGet(int position) throws ExecException
  {
    return ((ByteAttrVal) attrs[position]).length;
  }

  /**
   * Sets the value of an byte attribute
   * 
   * @param position
   *          Position of interest
   * @param v
   *          Attribute value to set
   * @param l
   *          Attribute length
   * @throws ExecException
   */
  public void bValueSet(int position, byte[] v, int l) throws ExecException
  {
    attrs[position].bNull = false;
    attrs[position].bValueSet(v, l);
  }

  /**
   * Gets the value of an Object attribute
   * 
   * @param position
   *          Position of interest
   * @return Attribute value
   * @throws ExecException
   */
  @SuppressWarnings("unchecked")
  public <T> T oValueGet(int position) throws ExecException
  {
    assert attrs[position].isBNull() == false;
    return (T) ((ObjectAttrVal) attrs[position]).value;
  }

  /**
   * Sets the value of an Object attribute
   * 
   * @param position
   *          Position of interest
   * @param v
   *          Attribute value to set
   * @throws ExecException
   */
  public void oValueSet(int position, Object v) throws ExecException
  {
    attrs[position].bNull = false;
    ((ObjectAttrVal) attrs[position]).value = v;
  }

  public void copy(ITuple src) throws ExecException
  {
    assert src instanceof Tuple;
    AttrVal[] srcAttrs = ((Tuple) src).attrs;
    copy(src, srcAttrs.length);
  }

  @SuppressWarnings("unchecked")
  public void copy(ITuple s, int numAttrs) throws ExecException
  {
    Tuple src = (Tuple) s;
    AttrVal[] srcAttrs = ((Tuple) src).attrs;
    if (numAttrs <= 0)
      numAttrs = srcAttrs.length;
    assert numAttrs <= srcAttrs.length;

    // Get the attributes
    for (int a = 0; a < numAttrs; a++)
    {
      assert attrs[a].getAttrType().getKind() == srcAttrs[a].getAttrType().getKind();

      if (srcAttrs[a].isBNull())
        attrs[a].bNull = true;
      else
      {
        attrs[a].bNull = false;
        switch (attrs[a].attrType.getKind())
        {
          case INT:
            ((IntAttrVal) attrs[a]).value = ((IntAttrVal) src.attrs[a]).value;
            break;
          case BIGINT:
            ((BigintAttrVal) attrs[a]).value = ((BigintAttrVal) src.attrs[a]).value;
            break;
          case FLOAT:
            ((FloatAttrVal) attrs[a]).value = ((FloatAttrVal) src.attrs[a]).value;
            break;
          case DOUBLE:
            ((DoubleAttrVal) attrs[a]).value = ((DoubleAttrVal) src.attrs[a]).value;
            break;
          case BIGDECIMAL:
            attrs[a].nValueSet(src.attrs[a].nValueGet(),
                               src.attrs[a].nPrecisionGet(),
                               src.attrs[a].nScaleGet());
            break;
          case BYTE:
            ((ByteAttrVal) attrs[a]).setValue(
                ((ByteAttrVal) src.attrs[a]).value,
                ((ByteAttrVal) src.attrs[a]).length);
            break;
          case CHAR:
            ((CharAttrVal) attrs[a]).setValue(
              ((CharAttrVal) src.attrs[a]).value,
              ((CharAttrVal) src.attrs[a]).length);
            break;
          case XMLTYPE:
          {
            XmltypeAttrVal xv = (XmltypeAttrVal) attrs[a];
            XmltypeAttrVal sxv = (XmltypeAttrVal) src.attrs[a];
            if (sxv.isObject)
            {
              xv.setValue(sxv.objVal);
            }
            else
            {
              xv.setValue(sxv.value, sxv.length);
            }
          }
            break;
          case BOOLEAN:
            ((BooleanAttrVal) attrs[a]).value = ((BooleanAttrVal) src.attrs[a]).value;
            break;
          case TIMESTAMP:
            ((TimestampAttrVal)attrs[a]).time 
              = ((TimestampAttrVal)src.attrs[a]).time;
            ((TimestampAttrVal)attrs[a]).format 
              = ((TimestampAttrVal)src.attrs[a]).format;
            break;
          case OBJECT:
            if((((ObjectAttrVal) src.attrs[a]).value == null) 
              || (!(((ObjectAttrVal) src.attrs[a]).value instanceof Cloneable)))
            {
              ((ObjectAttrVal) attrs[a]).value = ((ObjectAttrVal) src.attrs[a]).value;
            }
            else 
            {
              Object val = ((ObjectAttrVal) src.attrs[a]).value;
              try
              {
                Method m = val.getClass().getMethod("clone", (Class<?>[])null);
                if(m != null)
                  ((ObjectAttrVal)attrs[a]).value = m.invoke(val);
                else
                  ((ObjectAttrVal) attrs[a]).value = ((ObjectAttrVal) src.attrs[a]).value;
              }
              catch(NoSuchMethodException nsme)
              {
                ((ObjectAttrVal) attrs[a]).value = ((ObjectAttrVal) src.attrs[a]).value;
              }
              catch(InvocationTargetException e)
              {
                if(e.getCause() != null)
                  throw new ExecException(ExecutionError.OBJECT_CLONE_ERROR, e.getCause(), (Object[])null);
                else
                  throw new ExecException(ExecutionError.OBJECT_CLONE_ERROR);
              }
              catch(IllegalAccessException e)
              { 
                //have to catch this since it's not a subclass of RuntimeException
                ((ObjectAttrVal) attrs[a]).value = ((ObjectAttrVal) src.attrs[a]).value;
              }                    
              catch(RuntimeException e)
              { 
                throw new ExecException(ExecutionError.OBJECT_CLONE_ERROR);
              }
            }
            break;
          case INTERVAL:
            ((IntervalAttrVal)attrs[a]).vValueSet(
                ((IntervalAttrVal) src.attrs[a]).interval,
                ((IntervalAttrVal) src.attrs[a]).format);
            break;
          case INTERVALYM:
            ((IntervalYMAttrVal)attrs[a]).vymValueSet(
                ((IntervalYMAttrVal) src.attrs[a]).interval,
                ((IntervalYMAttrVal) src.attrs[a]).format);
            break;
          default:
            // Should not come
            assert false;
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void copy(ITuple s, int[] srcPoss, int[] destPoss) throws ExecException
  {
    int numAttrs = srcPoss.length;
    assert (numAttrs == destPoss.length);
    Tuple srcT = (Tuple) s;
    AttrVal[] srcAttrs = ((Tuple) srcT).attrs;
    
    // Get the attributes
    for (int a = 0; a < numAttrs; a++)
    {
      int src = srcPoss[a];
      int dest = destPoss[a];
      
      assert attrs[dest].getAttrType().getKind() == srcAttrs[src].getAttrType().getKind();

      if (srcAttrs[src].isBNull())
        attrs[dest].bNull = true;
      else
      {
        attrs[dest].bNull = false;
        switch (attrs[dest].attrType.getKind())
        {
          case INT:
            ((IntAttrVal) attrs[dest]).value = ((IntAttrVal) srcAttrs[src]).value;
            break;
          case BIGINT:
            ((BigintAttrVal) attrs[dest]).value = ((BigintAttrVal) srcAttrs[src]).longValueGet();
            break;
          case FLOAT:
            ((FloatAttrVal) attrs[dest]).value = ((FloatAttrVal) srcAttrs[src]).floatValueGet();
            break;
          case DOUBLE:
            ((DoubleAttrVal) attrs[dest]).value = ((DoubleAttrVal) srcAttrs[src]).doubleValueGet();
            break;
          case BIGDECIMAL:
            BigDecimal val = ((BigDecimalAttrVal)srcAttrs[src]).bigDecimalValueGet();
            ((BigDecimalAttrVal)attrs[dest]).nValueSet(val, val.precision(), val.scale()); 
            break;
          case BYTE:
            ((ByteAttrVal) attrs[dest]).setValue(
                ((ByteAttrVal) srcAttrs[src]).value,
                ((ByteAttrVal) srcAttrs[src]).length);
            break;
          case CHAR:
            ((CharAttrVal) attrs[dest]).setValue(
              ((CharAttrVal) srcAttrs[src]).value,
              ((CharAttrVal) srcAttrs[src]).length);
            break;
          case XMLTYPE:
          {
            XmltypeAttrVal xv = (XmltypeAttrVal) attrs[dest];
            XmltypeAttrVal sxv = (XmltypeAttrVal) srcAttrs[src];
            if (sxv.isObject)
            {
              xv.setValue(sxv.objVal);
            }
            else
            {
              xv.setValue(sxv.value, sxv.length);
            }
          }
            break;
          case BOOLEAN:
            ((BooleanAttrVal) attrs[dest]).value = ((BooleanAttrVal) srcAttrs[src]).value;
            break;
          case TIMESTAMP:
            ((TimestampAttrVal)attrs[dest]).time 
              = ((TimestampAttrVal)srcAttrs[src]).time;
            ((TimestampAttrVal)attrs[dest]).format 
            = ((TimestampAttrVal)srcAttrs[src]).format;
            break;
          case OBJECT:
            if((((ObjectAttrVal) srcAttrs[src]).value == null) 
              || (!(((ObjectAttrVal) srcAttrs[src]).value instanceof Cloneable)))
            {
              ((ObjectAttrVal) attrs[dest]).value = ((ObjectAttrVal) srcAttrs[src]).value;
            }
            else 
            {
              Object srcval = ((ObjectAttrVal) srcAttrs[src]).value;
              try
              {
                Method m = srcval.getClass().getMethod("clone", (Class<?>[])null);
                if(m != null)
                  ((ObjectAttrVal)attrs[dest]).value = m.invoke(srcval);
                else
                  ((ObjectAttrVal) attrs[dest]).value = ((ObjectAttrVal) srcAttrs[src]).value;
              }
              catch(NoSuchMethodException nsme)
              {
                ((ObjectAttrVal) attrs[dest]).value = ((ObjectAttrVal) srcAttrs[src]).value;
              }
              catch(InvocationTargetException e)
              {
                if(e.getCause() != null)
                  throw new ExecException(ExecutionError.OBJECT_CLONE_ERROR, e.getCause(), (Object[])null);
                else
                  throw new ExecException(ExecutionError.OBJECT_CLONE_ERROR);
              }
              catch(IllegalAccessException e)
              {
                ((ObjectAttrVal) attrs[dest]).value = ((ObjectAttrVal) srcAttrs[src]).value;
              }   
              catch(RuntimeException e)
              {
                throw new ExecException(ExecutionError.OBJECT_CLONE_ERROR);
              }
            }
            
            break;
          case INTERVAL:
            ((IntervalAttrVal) attrs[dest]).interval 
              = ((IntervalAttrVal) srcAttrs[src]).interval;
            ((IntervalAttrVal) attrs[dest]).format 
              = ((IntervalAttrVal) srcAttrs[src]).format;
            break;
          case INTERVALYM:
            ((IntervalYMAttrVal) attrs[dest]).interval 
              = ((IntervalYMAttrVal) srcAttrs[src]).interval;
            ((IntervalYMAttrVal) attrs[dest]).format 
               = ((IntervalYMAttrVal) srcAttrs[src]).format;
            break;
          default:
            // Should not come
            assert false;
        }
      }
    }
  }
  
  public void copyTo(TupleValue s, int numAttrs, TupleSpec attrSpecs,
      Column inCols[]) throws CEPException
  {
    // Set attributes
    for (int i = 0; i < numAttrs; i++)
    {
      boolean isNull = false;
      AttrVal inAttr = attrs[i];
      AttributeValue outAttr = s.getAttribute(i);
      isNull = inAttr.isBNull();
      outAttr.setBNull(isNull);

      if (!isNull)
      {
        int pos = inCols[i].getColnum();
        switch (attrSpecs.getAttrType(i).getKind())
        {
          case INT:
            s.iValueSet(i, ((IntAttrVal) attrs[pos]).value);
            break;
          case BOOLEAN:
            s.boolValueSet(i, ((BooleanAttrVal) attrs[pos]).value);
            break;
          case BIGINT:
            s.lValueSet(i, ((BigintAttrVal) attrs[pos]).value);
            break;
          case FLOAT:
            s.fValueSet(i, ((FloatAttrVal) attrs[pos]).value);
            break;
          case DOUBLE:
            s.dValueSet(i, ((DoubleAttrVal) attrs[pos]).value);
            break;
          case BIGDECIMAL:
            s.nValueSet(i,((BigDecimalAttrVal)attrs[pos]).nValueGet(),
                          ((BigDecimalAttrVal)attrs[pos]).nPrecisionGet(),
                          ((BigDecimalAttrVal)attrs[pos]).nScaleGet());
            break;
          case CHAR:
            s.cValueSet(i, ((CharAttrVal) attrs[pos]).value);
            s.cLengthSet(i, ((CharAttrVal) attrs[pos]).length);
            break;
          case XMLTYPE:
          {
            //external type is always char[] 
            XmltypeAttrVal xv = (XmltypeAttrVal) attrs[pos];
            //xValueGet will convert to char[] from obj 
            //if it has an object representation
            char[] xval = xv.xValueGet();
            s.xValueSet(i, xval);
          }
            break;
          case BYTE:
            s.bValueSet(i, ((ByteAttrVal) attrs[pos]).value);
            s.bLengthSet(i, ((ByteAttrVal) attrs[pos]).length);
            break;
          case TIMESTAMP:
            s.tValueSet(i, ((TimestampAttrVal)attrs[pos]).time);
            s.tFormatSet(i, ((TimestampAttrVal)attrs[pos]).format);
            break;
          case INTERVAL:
            s.vValueSet(i, 
              ((IntervalAttrVal) attrs[pos]).interval,
              ((IntervalAttrVal) attrs[pos]).format);
            break;
          case INTERVALYM:
            s.vymValueSet(i, 
              ((IntervalYMAttrVal) attrs[pos]).interval,
              ((IntervalYMAttrVal) attrs[pos]).format);
            break;
          case OBJECT:
            s.oValueSet(i, ((ObjectAttrVal) attrs[pos]).value);
            break;
          default:
            assert false;
        }
      }
    }
  }

  public void copyFrom(TupleValue s, int numAttrs, TupleSpec attrSpecs)
      throws CEPException
  {
    // Set attributes
    for (int pos = 0; pos < numAttrs; pos++)
    {
      boolean isNull = false;
      AttributeValue inpAttr = s.getAttribute(pos);
      isNull = inpAttr.isBNull();
      AttrVal outAttr = attrs[pos];
      outAttr.bNull = isNull;

      if (!isNull)
      {
        switch (attrSpecs.getAttrType(pos).getKind())
        {
          case INT:
            ((IntAttrVal) attrs[pos]).value = s.iValueGet(pos);
            break;
          case BIGINT:
            ((BigintAttrVal) attrs[pos]).value = s.lValueGet(pos);
            break;
          case FLOAT:
            ((FloatAttrVal) attrs[pos]).value = s.fValueGet(pos);
            break;
          case DOUBLE:
            ((DoubleAttrVal) attrs[pos]).value = s.dValueGet(pos);
            break;
          case BIGDECIMAL:
            ((BigDecimalAttrVal)attrs[pos]).nValueSet(s.nValueGet(pos), 
                                                     s.nPrecisionGet(pos), 
                                                     s.nScaleGet(pos));
            break;
          case CHAR:
            ((CharAttrVal) attrs[pos]).setValue(s.cValueGet(pos), s
                .cLengthGet(pos));
            break;
          case XMLTYPE:
            ((XmltypeAttrVal) attrs[pos]).setValue(s.xValueGet(pos), s
                .xLengthGet(pos));
            break;
          case BYTE:
            ((ByteAttrVal) attrs[pos]).setValue(s.bValueGet(pos), s
                .bLengthGet(pos));
            break;
          case TIMESTAMP:
            ((TimestampAttrVal)attrs[pos]).time = s.tValueGet(pos);
            ((TimestampAttrVal)attrs[pos]).format = s.tFormatGet(pos);
            break;
          case INTERVAL:
            ((IntervalAttrVal) attrs[pos]).interval = s.intervalValGet(pos);
            ((IntervalAttrVal) attrs[pos]).format   = s.vFormatGet(pos);
            break;
          case INTERVALYM:
            ((IntervalYMAttrVal) attrs[pos]).interval = s.intervalYMValGet(pos);
            ((IntervalYMAttrVal) attrs[pos]).format   = s.vFormatGet(pos);
            break;
          case OBJECT:
            ((ObjectAttrVal) attrs[pos]).value = s.oValueGet(pos);
            break;
          case BOOLEAN:
            ((BooleanAttrVal)attrs[pos]).value = s.boolValueGet(pos);
            break;
          default:
            assert false;
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public boolean compare(ITuple s) throws ExecException
  {
    return compare(s, null);
  }
  
  @SuppressWarnings("unchecked")
  public boolean compare(ITuple s, int[] skipPos) throws ExecException
  {
    Tuple src = (Tuple) s;
    boolean same = true;
    for (int a = 0; a < attrs.length; a++)
    {
      if(skipPos != null)
      {
        boolean found = false;
        for(int next: skipPos)
        {
          found = next == a;
          if(found) 
            break;
        }
        if(found)
          continue;
      }
        
      if (attrs[a].bNull)
      {
        same = src.attrs[a].bNull;
      }
      else
      {
        switch (attrs[a].attrType.getKind())
        {
          case INT:
            same = (((IntAttrVal) attrs[a]).value == ((IntAttrVal) src.attrs[a]).value);
            break;
          case BIGINT:
            same = (((BigintAttrVal) attrs[a]).value == ((BigintAttrVal) src.attrs[a]).value);
            break;
          case FLOAT:
            same = (((FloatAttrVal) attrs[a]).value == ((FloatAttrVal) src.attrs[a]).value);
            break;
          case DOUBLE:
            same = (((DoubleAttrVal) attrs[a]).value == ((DoubleAttrVal) src.attrs[a]).value);
            break;
          case BIGDECIMAL:
            same = (((BigDecimalAttrVal)attrs[a]).nValueGet().compareTo(
                   ((BigDecimalAttrVal)src.attrs[a]).nValueGet()) == 0);
            break;
          case BYTE:
            same = (((ByteAttrVal) attrs[a]).length == ((ByteAttrVal) src.attrs[a]).length);
            if (!same)
              break;
            same = Arrays.equals(((ByteAttrVal) attrs[a]).value,
                ((ByteAttrVal) src.attrs[a]).value);
            break;
          case CHAR:
            same = (((CharAttrVal) attrs[a]).length == ((CharAttrVal) src.attrs[a]).length);
            if (!same)
              break;
            same = Arrays.equals(((CharAttrVal) attrs[a]).value,
                ((CharAttrVal) src.attrs[a]).value);
            break;
          case XMLTYPE:
            XmltypeAttrVal x = (XmltypeAttrVal) attrs[a];
            XmltypeAttrVal srcx = (XmltypeAttrVal) src.attrs[a];
           
            same = (x.isObject != srcx.isObject);
            if(!same)
              break;
            //xValueGet will convert to char[] from obj 
            //if it has an object representation
            char[] xval = x.xValueGet();
              
            //xValueGet will convert to char[] from obj 
            //if it has an object representation
            char[] srcxval = srcx.xValueGet();
           
            // Due to the way in which a XmltypeAttributeValue and a 
            // XmltypeAttrVal is implemented , 
            // the character array sizes may differ along with the total 
            // contents. We only need to compare the character arrays
            // till their specified length if it does not have object 
            // representation.
            int val = -1;
            if(x.isObject)
              val = cCompare(xval, xval.length, 
                  srcxval, srcxval.length);
            else
              val = cCompare(xval, x.xLengthGet(), 
                srcxval, srcx.xLengthGet());
            

            same = val == 0;
            break;
          case BOOLEAN:
            same = (((BooleanAttrVal) attrs[a]).value == ((BooleanAttrVal) src.attrs[a]).value);
            break;
          case TIMESTAMP:
            // Note: Ensure that timezone doesn't impact this comparison
            same = ( ((TimestampAttrVal)attrs[a]).time == 
                     ((TimestampAttrVal)src.attrs[a]).time);
            break;
          case OBJECT:            
            Object curval = ((ObjectAttrVal) attrs[a]).value;

            if (((ObjectAttrVal) src.attrs[a]) != null) {
              Object srcval = ((ObjectAttrVal) src.attrs[a]).value; 

              if (srcval != null) {
                if (curval.equals(srcval))
                  same = true;
                else
                  same = false;
              }
            }
            break;            
          case INTERVAL:
            same = (((IntervalAttrVal) attrs[a]).interval 
                     == ((IntervalAttrVal) src.attrs[a]).interval);
            break;
          case INTERVALYM:
            same = (((IntervalYMAttrVal) attrs[a]).interval 
                     == ((IntervalYMAttrVal) src.attrs[a]).interval);
            break;
          default:
            // Should not come
            assert false;
        }
      }
      if (!same)
        break;
    }
    return same;
  }

  public boolean beval(Datatype type, Op op, int col, int bit, ITuple o,
      int col2, int bit2, Pattern pattern, boolean n2ntrue)
      throws ExecException
  {
    Tuple other = (Tuple) o;
    AttrVal attr1 = attrs[col];
    if (op == Op.IS_NULL)
      return attr1.bNull;
    
    if(op == Op.IS_NOT_NULL)
      return !attr1.bNull;

    AttrVal attr2 = null;

    if (op.args == 2)
    {
      attr2 = other.attrs[col2];
    }
    switch (op.nullType)
    {
      case ANY_N2N:
        if (n2ntrue && attr1.bNull && attr2.bNull)
          return true;
        else if (attr1.bNull || attr2.bNull)
          return false;
        break;
      case ANY:
        if (attr1.bNull || attr2.bNull)
          return false;
        break;
    }

    assert (attr1 != null);
    assert (!attr1.bNull);
    if (op.args == 2)
    {
      assert (attr2 != null);
      assert (!attr2.bNull);
    }
    switch (type.getKind())
    {
      case INT:
      {
        int val1, val2;
        val1 = ((IntAttrVal) attr1).value;
        val2 = ((IntAttrVal) attr2).value;
        switch (op)
        {
          case LT:
            return (val1 < val2);
          case LE:
            return (val1 <= val2);
          case GT:
            return (val1 > val2);
          case GE:
            return (val1 >= val2);
          case EQ:
            return (val1 == val2);
          case NE:
            return (val1 != val2);
          default:
            assert false;
        }
      }
        break;

      case BIGINT:
      {
        long val1, val2;
        val1 = ((BigintAttrVal) attr1).value;
        val2 = ((BigintAttrVal) attr2).value;
        switch (op)
        {
          case LT:
            return (val1 < val2);
          case LE:
            return (val1 <= val2);
          case GT:
            return (val1 > val2);
          case GE:
            return (val1 >= val2);
          case EQ:
            return (val1 == val2);
          case NE:
            return (val1 != val2);
          default:
            assert false;
        }
      }
        break;

      case FLOAT:
      {
        float val1, val2;
        val1 = ((FloatAttrVal) attr1).value;
        val2 = ((FloatAttrVal) attr2).value;
        switch (op)
        {
          case LT:
            return (val1 < val2);
          case LE:
            return (val1 <= val2);
          case GT:
            return (val1 > val2);
          case GE:
            return (val1 >= val2);
          case EQ:
            return (val1 == val2);
          case NE:
            return (val1 != val2);
          default:
            assert false;
        }
      }
        break;

      case DOUBLE:
      {
        double val1, val2;
        val1 = ((DoubleAttrVal) attr1).value;
        val2 = ((DoubleAttrVal) attr2).value;
        switch (op)
        {
          case LT:
            return (val1 < val2);
          case LE:
            return (val1 <= val2);
          case GT:
            return (val1 > val2);
          case GE:
            return (val1 >= val2);
          case EQ:
            return (val1 == val2);
          case NE:
            return (val1 != val2);
          default:
            assert false;
        }
      }
        break;

      case BIGDECIMAL:
      {
        BigDecimal val1, val2;
        val1 = ((BigDecimalAttrVal)attr1).nValueGet();
        val2 = ((BigDecimalAttrVal)attr2).nValueGet();
        switch(op)
        {
          case LT:
            return (val1.compareTo(val2) < 0);
          case LE:
            return (val1.compareTo(val2) <= 0);
          case GT:
            return (val1.compareTo(val2) > 0);
          case GE:
            return (val1.compareTo(val2) >= 0);
          case EQ:
            return (val1.compareTo(val2) == 0);
          case NE:
            return (val1.compareTo(val2) != 0);
          default:
            assert false;
        }
      }
        break;
      case TIMESTAMP:
      {
        long val1, val2;
        val1 = ((TimestampAttrVal)attr1).time;
        val2 = ((TimestampAttrVal)attr2).time;
        // Note: Ensure that timestamp format /zones doesn't impact the 
        // comparison between two timetamp values
        switch(op)
        {
          case LT: return (val1 < val2);
          case LE: return (val1 <= val2);
          case GT: return (val1 > val2);
          case GE: return (val1 >= val2);
          case EQ: return (val1 == val2);
          case NE: return (val1 != val2);
          default: assert false;
        }
      }
        break;

      case BYTE:
      {
        int val = bCompare(((ByteAttrVal) attr1).value,
            ((ByteAttrVal) attr1).length, ((ByteAttrVal) attr2).value,
            ((ByteAttrVal) attr2).length);
        switch (op)
        {
          case LT:
            return (val < 0);
          case LE:
            return (val <= 0);
          case GT:
            return (val > 0);
          case GE:
            return (val >= 0);
          case EQ:
            return (val == 0);
          case NE:
            return (val != 0);
          default:
            assert false;
        }
      }
        break;

      case CHAR:
      {
        int val = 0;
        if (op != Op.LIKE)
        {
          val = cCompare(((CharAttrVal) attr1).value,
              ((CharAttrVal) attr1).length, ((CharAttrVal) attr2).value,
              ((CharAttrVal) attr2).length);
        }
        switch (op)
        {
          case LT:
            return (val < 0);
          case LE:
            return (val <= 0);
          case GT:
            return (val > 0);
          case GE:
            return (val >= 0);
          case EQ:
            return (val == 0);
          case NE:
            return (val != 0);
          case LIKE: // CHR_LIKE
          {
            int len = ((CharAttrVal) attr1).length;
            if (len == 0)
              return false;

            // Pattern can either be a constant string or an arithmetic expression
            // Case-1 If pattern is a constant string(e.g. c1 LIKE "%HelloWorld%"
            //        CQL Engine will compile the pattern at instruction creation time.
            //        Please see processBaseBoolExpr() method in ExprHelper.java.
            // Case-2 Expression (e.g. c1 LIKE ".*"|| "ABCD" || ".*")
            //        CQL Engine will compile the pattern on condition evaluation time (Tuple.beval())
            if(pattern == null)
            {
              assert attr2 != null;
              String attr2Val = new String(((CharAttrVal) attr2).value);
              try
              {
                pattern = Pattern.compile(attr2Val);
              }
              catch(PatternSyntaxException e)
              {
                LogUtil.severe(LoggerType.TRACE, "Failed to evaluate LIKE condition because pattern is invalid regular expression."
                  + "value=" + new String(((CharAttrVal) attr1).value) + " pattern=" + attr2Val + " error-message="+ e.getMessage());
                throw new SoftExecException(ExecutionError.INVALID_PATTERN_SYNTAX, attr2Val);
              }
            }
            
            Scratch buf = scratchBuf.get();
            StringBuilder charSeq = buf.charSeq;
            charSeq.setLength(len);

            char[] c1 = ((CharAttrVal) attr1).value;
            for (int i = 0; i < len; i++)
            {
              charSeq.setCharAt(i, c1[i]);
            }

            Matcher matcher = pattern.matcher(charSeq);
            return (matcher.find(0));
          }

          default:
            assert false;
        }
      }
        break;
      case INTERVAL:
      {
        long val1, val2;
        val1 = ((IntervalAttrVal) attr1).interval;
        val2 = ((IntervalAttrVal) attr2).interval;
        switch (op)
        {
          case LT:
            return (val1 < val2);
          case LE:
            return (val1 <= val2);
          case GT:
            return (val1 > val2);
          case GE:
            return (val1 >= val2);
          case EQ:
            return (val1 == val2);
          case NE:
            return (val1 != val2);
          default:
            assert false;
        }
      }
        break;
        
      case INTERVALYM:
      {
        long val1, val2;
        val1 = ((IntervalYMAttrVal) attr1).interval;
        val2 = ((IntervalYMAttrVal) attr2).interval;
        switch (op)
        {
          case LT:
            return (val1 < val2);
          case LE:
            return (val1 <= val2);
          case GT:
            return (val1 > val2);
          case GE:
            return (val1 >= val2);
          case EQ:
            return (val1 == val2);
          case NE:
            return (val1 != val2);
          default:
            assert false;
        }
      }
        break;

      case BOOLEAN:
      {
        if (op == Op.EQ)
        {
          boolean val1 = ((BooleanAttrVal) attr1).value;
          boolean val2 = ((BooleanAttrVal) attr2).value;
          return (val1 == val2);
        } 
        else if (op == Op.NE)
        {
          boolean val1 = ((BooleanAttrVal) attr1).value;
          boolean val2 = ((BooleanAttrVal) attr2).value;
          return (val1 != val2);
        }
        
        byte[] arr1 = ((ByteAttrVal) attr1).value;
        int bpos = bit / (Constants.BITS_PER_BYTE);
        assert arr1.length >= bpos;
        boolean val1 = ((arr1[bpos] & (1 << (bit % Constants.BITS_PER_BYTE))) != 0);
        boolean val2 = false;
        if (op != Op.NOT)
        {
          byte[] arr2 = ((ByteAttrVal) attr2).value;
          bpos = bit2 / (Constants.BITS_PER_BYTE);
          assert arr2.length >= bpos;
          val2 = ((arr2[bpos] & (1 << (bit2 % Constants.BITS_PER_BYTE))) != 0);
        }
        
        switch (op)
        {
          case AND:
            return (val1 && val2);
          case OR:
            return (val1 || val2);
          case NOT:
            return (!val1);
          case XOR:
            return (val1 || val2) && (!(val1 && val2));
          default:
            assert false;
        }
        break;
      }
      
      case XMLTYPE:
      {
        XmltypeAttrVal val1 = (XmltypeAttrVal) attr1;
        XmltypeAttrVal val2 = (XmltypeAttrVal) attr2;
       
        boolean same;
        
        switch(op)
        {
          case EQ:
          {
            char[] xval1 = val1.getValue();
            char[] xval2 = val2.getValue();
            // Due to the way in which a XmltypeAttributeValue and a 
            // XmltypeAttrVal is implemented , 
            // the character array sizes may differ along with the total 
            // contents. We only need to compare the character arrays
            // till their specified length.
            int length1 = val1.isObject ? xval1.length : val1.xLengthGet();
            int length2 = val2.isObject ? xval2.length : val2.xLengthGet();
            
            int val = cCompare(xval1, length1, 
                               xval2, length2);
            
            same = val == 0;
            
            return same;
          }
          default:
            assert false;
        }        
        break;
      }
      case OBJECT:
      {
      /* #(10145509): fix missing equality comparison for OBJECT types. */
      boolean same;
        ObjectAttrVal val1 = (ObjectAttrVal) attr1;
        ObjectAttrVal val2 = (ObjectAttrVal) attr2;
        
        switch(op) 
        {
        case EQ:
        {
        Object oval1, oval2;
        if (val1 != null) {
          oval1 = val1.value;
          if (val2 != null) {
            /* (1) val1 and val2 are not null */
            oval2 = val2.value;
            if (oval1 != null) {
              if (oval2 != null) {
                if (oval1.equals(oval2))
                  same = true;
                else
                  same = false;
              } else
                same = false;
            } else {
              if (oval2 != null) {
                same = false;
              } else {
                same = true;
              }
            }
          }
            else {
              /* (2). val2 is null, return false */
                same = false;
            }              
          }
          else {            
            if (val2 != null) {
              /* (3). val1 is null, but val2 is not */
              same = false;
            }
            else {
              /* (4). both val1 and val2 are null */
                same = true;
            }
          }
          return same;
        }
        default:
          assert false;
        }
        break;
      }
    }      
    return true;
  }
  
  public int heval(Datatype dtype, int col, int hash) throws ExecException
  {
    AttrVal attr = attrs[col];

    if (!attr.bNull)
    {
      switch (dtype.getKind())
      {
        case INT:
          hash = ((hash << 5) + hash) + inthash(((IntAttrVal) attr).value);
          break;

        case BIGINT:
          hash = ((hash << 5) + hash) + longhash(((BigintAttrVal) attr).value);
          break;

        case BYTE:
          hash = ((hash << 5) + hash) + ((ByteAttrVal) attr).value[0];
          break;

        case CHAR:
          char cptr[] = ((CharAttrVal) attr).value;
          int clen = ((CharAttrVal) attr).length;
          for (int j = 0; j < clen; j++)
            hash = ((hash << 5) + hash) + cptr[j];
          break;

        case FLOAT:
          hash = ((hash << 5) + hash)
              + inthash((int) ((FloatAttrVal) attr).value);
          break;
          
        case DOUBLE:
          hash = ((hash << 5) + hash)
               + inthash((int) ((DoubleAttrVal) attr).value);
          break;
          
        case BIGDECIMAL:
          hash = ((hash << 5) + hash)
               + longhash(((BigDecimalAttrVal)attr).nValueGet().longValue());
          break;

        case TIMESTAMP:
          hash = ((hash << 5) + hash) + inthash((int)((TimestampAttrVal)attr).time);
          break;

        case INTERVAL:
          hash = ((hash << 5) + hash)
              + longhash(((IntervalAttrVal) attr).interval);
          break;
          
        case INTERVALYM:
          hash = ((hash << 5) + hash)
              + longhash(((IntervalYMAttrVal) attr).interval);
          break;
          
        case BOOLEAN:
          hash = ((hash << 5) + hash) + inthash(((BooleanAttrVal) attr).value ? 1:0);
          break;

        case XMLTYPE:
          char xptr[] = ((XmltypeAttrVal)attr).getValue();
          int xlen = 0;
          if(((XmltypeAttrVal)attr).isObject)
            xlen = xptr.length;
          else
            xlen = ((XmltypeAttrVal)attr).length;
          for(int k=0; k<xlen; k++)
            hash = ((hash << 5) + hash) + xptr[k]; 
          break;

        case OBJECT:
          hash = ((hash << 5) + hash);
          Object oval = ((ObjectAttrVal)attr).oValueGet();
          if (oval != null)
          {
            hash += oval.hashCode();
          }
          break;

        default:
          assert false;
      }
    }
    return hash;
  }

  public void aeval(Datatype type, Op op, int col, ITuple s, int col1,
      ITuple o, int col2) throws ExecException
  {
    Tuple src = (Tuple) s;
    AttrVal dattr = attrs[col];

    if (op == Op.NULL_CPY)
    {
      dattr.bNull = true;
      return;
    }
   
    // SYSTIME Op doesnot need any argument 
    if(op == Op.SYSTIME)
    {
      assert dattr instanceof TimestampAttrVal;
      // Presently As we are dealing with millisecond time value;
      // Result value is set as current system time in millisecond
      long currentTimeMillis = System.currentTimeMillis();
      ((TimestampAttrVal)dattr).time = currentTimeMillis * 1000000l;
      ((TimestampAttrVal)dattr).format = TimestampFormat.getDefault();
      dattr.bNull = false;
      return;
    }

    AttrVal attr1 = src.attrs[col1];
    AttrVal attr2 = null;
    if (op.args == 1)
    {
      if (op.nullType != NullType.NOOP && attr1.bNull)
      {
        dattr.bNull = true;
        return;
      }
      if(op == Op.SYSTIMEWITHTZ)
      {
        // Negative Case Handling: SELECT SYSTIMESTAMP(null) FROM INPUT;
      	if(attr1.bNull)
      	{
      	  LogUtil.warning(LoggerType.TRACE,"specified timezone value is null");
          throw new SoftExecException(ExecutionError.INVALID_TIME_ZONE_ID, "null");
      	}
      	
      	// Parse timezone string
      	char[] val1 = ((CharAttrVal) attr1).value;
      	ZoneId zid = null;
      	try
      	{
          zid = ZoneId.of(String.valueOf(val1));
        }
      	catch( DateTimeException ze)
      	{
          LogUtil.warning(LoggerType.TRACE, ze.toString());
          throw new SoftExecException(ExecutionError.INVALID_TIME_ZONE_ID, String.valueOf(val1));
        }
        assert dattr instanceof TimestampAttrVal;
        // Presently As we are dealing with millisecond time value;
        // Result value is set as current system time in millisecond
        long currentTimeMillis = System.currentTimeMillis();
        ((TimestampAttrVal)dattr).time = currentTimeMillis * 1000000l;
        ((TimestampAttrVal)dattr).format = new TimestampFormat(TimeZone.getTimeZone(zid));
        
        dattr.bNull = false;
        return;
      }
    }
    
   

    if (op.args == 2)
    {
      // prepare the second attr for binary op
      Tuple other = (Tuple) o;
      attr2 = other.attrs[col2];
      switch (op.nullType)
      {
        case ANY:
          if (attr1.bNull || attr2.bNull)
          {
            dattr.bNull = true;
            return;
          }
          break;

        case BOTH:
          if (attr1.bNull && attr2.bNull)
          {
            dattr.bNull = true;
            return;
          }
          break;
      }
    }

    // evaluate
    switch (type.getKind())
    {
      case INT:
      {
        int val1 = 0, val2 = 0, dval = 0;
        if (op != Op.CLEN && op != Op.BLEN)
        {
          val1 = ((IntAttrVal) attr1).value;
          if (attr2 != null)
            val2 = ((IntAttrVal) attr2).value;
        }
        switch (op)
        {
          case ADD: // INT_ADD
            dval = val1 + val2;
            break;
          case SUB: // INT_SUB
            dval = val1 - val2;
            break;
          case MUL: // INT_MUL
            dval = val1 * val2;
            break;
          case DIV: // INT_DIV
            if(val2 == 0)
              throw new ExecException(ExecutionError.DIVIDE_BY_ZERO);
            dval = val1 / val2;
            break;
          case SUM_ADD: // INT_SUM_ADD
            /*
             * INT_SUM_ADD(x,y) = x + y if x is non null and y is non null x, if
             * x is non null and y is null y, if x is null and y is non null
             * null, if x and y are null
             */
            if (!attr1.bNull && !attr2.bNull)
              dval = val1 + val2;
            else if (!attr1.bNull)
              dval = val1;
            else if (!attr2.bNull)
              dval = val2;
            break;

          case SUM_SUB: // INT_SUM_SUB
            /*
             * INT_SUM_SUB(x,y) = x - y if x is non null and y is non null x, if
             * x is non null and y is null y, if x is null and y is non null
             * null, if x and y are null
             */
            if (!attr1.bNull && !attr2.bNull)
              dval = val1 - val2;
            else if (!attr1.bNull)
              dval = val1;
            else
              assert (false);
            break;

          case NVL: // INT_NVL
            dval = attr1.bNull ? val2 : val1;
            break;

          case CPY: // INT_CPY
            dval = val1;
            break;

          case TO_BIGINT: // INT_TO_BIGINT
            ((BigintAttrVal) dattr).value = (long) val1;
            dattr.bNull = false;
            return;

          case TO_FLT: // INT_TO_FLT
            ((FloatAttrVal) dattr).value = (float) val1;
            dattr.bNull = false;
            return;
          
          case TO_BOOLEAN: // INT_TO_BOOLEAN
            ((BooleanAttrVal) dattr).value = (val1 != 0);
            dattr.bNull = false;
            return;

          case TO_CHR1: //INT_TO_CHAR
            Scratch buf = scratchBuf.get();
            
            // Allocate and Cleaning temp Buffer
            StringBuilder tmpData = buf.charSeq;
            tmpData.delete(0, tmpData.length());
            
            char[] tmpArray = buf.charBuf;            
            
            tmpData.append(val1);
            //Read tmpData into tmpArray
            tmpData.getChars(0, tmpData.length(), tmpArray, 0);
            ((CharAttrVal)dattr).setValue(tmpArray, tmpData.length());            
            return;
            
          case TO_DBL: // INT_TO_DBL
            ((DoubleAttrVal) dattr).value = (double) val1;
            dattr.bNull = false;
            return;
            
          case TO_BIGDECIMAL: //INT_TO_BIGDECIMAL
            BigDecimal val = new BigDecimal(String.valueOf(val1));
            ((BigDecimalAttrVal)dattr).nValueSet(val, val.precision(), 
                                                 val.scale());
            return;

          case UMX: // INT_UMX
            if (attr1.bNull)
            {
              dval = val2;
            }
            else if (attr2.bNull)
            {
              dval = val1;
            }
            else
            {
              dval = (val1 < val2) ? val2 : val1;
            }
            break;

          case UMN: // INT_UMN
            if (attr1.bNull)
            {
              dval = val2;
            }
            else if (attr2.bNull)
            {
              dval = val1;
            }
            else
            {
              dval = (val1 > val2) ? val2 : val1;
            }
            break;

          case AVG: // INT_AVG
            if(val2 <= 0 || attr1.bNull) //attr1.bNull will be null for first input
            {
              dattr.bNull = true;
              return;
            }
            else
              dattr.bNull = false;
            
            float fdval;            
            if (attr1.bNull)
            {
              fdval = (float) val2;
            }
            else if (attr2.bNull)
            {
              fdval = (float) val1;
            }
            else
            {
              fdval = (float) (1.0 * val1) / (float) (1.0 * val2);
            }
            ((FloatAttrVal) dattr).value = fdval;
            return;

          case CLEN: // CHR_LEN
            // if argument to length(..) is null; then it returns null
            // else if argument to length(..) is empty string; then it returns null
            // else it returns length of argument
            if (attr1.bNull || ((CharAttrVal) attr1).length == 0){
               dattr.bNull = true;
               return;
            }
            else
              dval = ((CharAttrVal) attr1).length;
            break;

          case BLEN: // BYTE_LEN
            // if argument to length(..) is null; then it returns null
            // else if argument to length(..) is empty string; then it returns null
            // else it returns length of argument
            if (attr1.bNull || ((ByteAttrVal) attr1).length == 0){
              dattr.bNull = true;
              return;
            }
            else
              dval = ((ByteAttrVal) attr1).length;
            break;
          
          case MOD: // INT_MOD
            if(val2 == 0)
              dval = val1;
            else
              dval = val1 % val2;
            break;            

          default:
            assert false;
        }
        ((IntAttrVal) dattr).value = dval;
        dattr.bNull = false;
      }
        break;

      case BIGINT:
      {
        long val1 = 0, val2 = 0, dval = 0;
        val1 = ((BigintAttrVal) attr1).value;
        if (attr2 != null && op != Op.AVG)
          val2 = ((BigintAttrVal) attr2).value;
        switch (op)
        {
          case ADD: // BIGINT_ADD
            dval = val1 + val2;
            break;
          case SUB: // BIGINT_SUB
            dval = val1 - val2;
            break;
          case MUL: // BIGINT_MUL
            dval = val1 * val2;
            break;
          case DIV: // BIGINT_DIV
            if(val2 == 0l)
              throw new ExecException(ExecutionError.DIVIDE_BY_ZERO);
            dval = val1 / val2;
            break;
          case SUM_ADD: // BIGINT_SUM_ADD
            /*
             * INT_SUM_ADD(x,y) = x + y if x is non null and y is non null x, if
             * x is non null and y is null y, if x is null and y is non null
             * null, if x and y are null
             */
            if (!attr1.bNull && !attr2.bNull)
              dval = val1 + val2;
            else if (!attr1.bNull)
              dval = val1;
            else if (!attr2.bNull)
              dval = val2;
            break;

          case SUM_SUB: // BIGINT_SUM_SUB
            /*
             * INT_SUM_SUB(x,y) = x - y if x is non null and y is non null x, if
             * x is non null and y is null y, if x is null and y is non null
             * null, if x and y are null
             */
            if (!attr1.bNull && !attr2.bNull)
              dval = val1 - val2;
            else if (!attr1.bNull)
              dval = val1;
            else
              assert (false);
            break;

          case NVL: // BIGINT_NVL
            dval = attr1.bNull ? val2 : val1;
            break;

          case TO_BOOLEAN: // BIGINT_TO_BOOLEAN
            ((BooleanAttrVal) dattr).value = (val1 != 0);
            dattr.bNull = false;
            return;

          case TO_FLT: // BIGINT_TO_FLT
            ((FloatAttrVal) dattr).value = (float) val1;
            dattr.bNull = false;
            return;
          
          case TO_DBL: // BIGINT_TO_DBL
            ((DoubleAttrVal) dattr).value = (double) val1;
            dattr.bNull = false;
            return;
          
          case TO_BIGDECIMAL: //BIGINT_TO_BIGDECIMAL
            BigDecimal val = new BigDecimal(val1);
            ((BigDecimalAttrVal)dattr).nValueSet(val, val.precision(), 
                                                 val.scale());
            return;
            
          case TO_CHR1: //BIGINT_TO_CHAR
            Scratch buf = scratchBuf.get();
            
            // Allocate and Cleaning temp Buffer
            StringBuilder tmpData = buf.charSeq;
            tmpData.delete(0, tmpData.length());
            
            char[] tmpArray = buf.charBuf;            
            
            tmpData.append(val1);
            //Read tmpData into tmpArray
            tmpData.getChars(0, tmpData.length(), tmpArray, 0);
            ((CharAttrVal)dattr).setValue(tmpArray, tmpData.length());            
            return;
            
          case CPY: // BIGINT_CPY
            dval = val1;
            break;

          case UMX: // BIGIN_UMX
            if (attr1.bNull)
            {
              dval = val2;
            }
            else if (attr2.bNull)
            {
              dval = val1;
            }
            else
            {
              dval = (val1 < val2) ? val2 : val1;
            }
            break;

          case UMN: // BIGINT_UMN
            if (attr1.bNull)
            {
              dval = val2;
            }
            else if (attr2.bNull)
            {
              dval = val1;
            }
            else
            {
              dval = (val1 > val2) ? val2 : val1;
            }
            break;
            
          case MOD:  //BIGINT_MOD
            if(val2 == 0L)
              dval = val1;
            else
              dval = val1 % val2;
            break; 

          case AVG: // BIGINT_AVG
            int ival2 = ((IntAttrVal) attr2).value;
            if(ival2 <= 0 || attr1.bNull)
            {
              dattr.bNull = true;
              return;
            }
            else
              dattr.bNull = false;
	    double ddval;
            if (attr1.bNull)
            {
              ddval = (double) ival2;
            }
            else if (attr2.bNull)
            {
              ddval = val1;
            }
            else
            {
              ddval = val1 / (double) (1.0 * ival2);
            }
            ((DoubleAttrVal) dattr).value = ddval;
            return;

          default:
            assert false;
        }
        ((BigintAttrVal) dattr).value = dval;
        dattr.bNull = false;
      }
        break;

      case FLOAT:
      {
        float val1 = 0, val2 = 0, dval = 0;
        val1 = attr1.floatValueGet();
        if (attr2 != null && op != Op.AVG)
          val2 = attr2.floatValueGet();
        switch (op)
        {
          case ADD: // FLT_ADD
            dval = val1 + val2;
            break;
          case SUB: // FLT_SUB
            dval = val1 - val2;
            break;
          case MUL: // FLT_MUL
            dval = val1 * val2;
            break;
          case DIV: // FLT_DIV
            if(val2 == 0.0f)
              throw new ExecException(ExecutionError.DIVIDE_BY_ZERO);
            dval = val1 / val2;
            break;
          case SUM_ADD: // FLT_SUM_ADD
            /*
             * INT_SUM_ADD(x,y) = x + y if x is non null and y is non null x, if
             * x is non null and y is null y, if x is null and y is non null
             * null, if x and y are null
             */
            if (!attr1.bNull && !attr2.bNull)
              dval = val1 + val2;
            else if (!attr1.bNull)
              dval = val1;
            else if (!attr2.bNull)
              dval = val2;
            break;

          case SUM_SUB: // FLT_SUM_SUB
            /*
             * INT_SUM_SUB(x,y) = x - y if x is non null and y is non null x, if
             * x is non null and y is null y, if x is null and y is non null
             * null, if x and y are null
             */
            if (!attr1.bNull && !attr2.bNull)
              dval = val1 - val2;
            else if (!attr1.bNull)
              dval = val1;
            else
              assert (false);
            break;

          case TO_DBL: // FLT_TO_DBL
            ((DoubleAttrVal) dattr).value = (double) val1;
            dattr.bNull = false;
            return;
            
          case TO_BIGDECIMAL: //FLT_TO_BIGDECIMAL
            BigDecimal val = new BigDecimal(String.valueOf(val1));
            ((BigDecimalAttrVal)dattr).nValueSet(val, val.precision(), 
                                                 val.scale());
            return;
          case TO_CHR1: //FLT_TO_CHR
            Scratch buf = scratchBuf.get();
            
            // Allocate and Cleaning temp Buffer
            StringBuilder tmpData = buf.charSeq;
            tmpData.delete(0, tmpData.length());
            
            char[] tmpArray = buf.charBuf;            
            
            tmpData.append(val1);
            //Read tmpData into tmpArray
            tmpData.getChars(0, tmpData.length(), tmpArray, 0);
            ((CharAttrVal)dattr).setValue(tmpArray, tmpData.length());            
            return;
            
          case NVL: // FLT_NVL
            dval = attr1.bNull ? val2 : val1;
            break;

          case CPY: // FLT_CPY
            dval = val1;
            break;

          case UMX: // FLT_UMX
            if (attr1.bNull)
            {
              dval = val2;
            }
            else if (attr2.bNull)
            {
              dval = val1;
            }
            else
            {
              dval = (val1 < val2) ? val2 : val1;
            }
            break;

          case UMN: // FLT_UMN
            if (attr1.bNull)
            {
              dval = val2;
            }
            else if (attr2.bNull)
            {
              dval = val1;
            }
            else
            {
              dval = (val1 > val2) ? val2 : val1;
            }
            break;

          case AVG: // FLT_AVG
            int ival2 = ((IntAttrVal) attr2).value;
            if(ival2 <= 0 || attr1.bNull)
            {
              dattr.bNull = true;
              return;
            }
            else
              dattr.bNull = false;
            if (attr1.bNull)
            {
              dval = (float) ival2;
            }
            else if (attr2.bNull)
            {
              dval = val1;
            }
            else
            {
              dval = val1 / (float) (1.0 * ival2);
            }
            ((FloatAttrVal) dattr).value = dval;
            return;
            
          case MOD:  //FLOAT_MOD
            if(val2 == 0.0f)
              dval = val1;
            else
              dval = val1 % val2;
            break; 

          default:
            assert false;
        }
        ((FloatAttrVal) dattr).value = dval;
        dattr.bNull = false;
      }
        break;
      
      case DOUBLE:
      {
        double val1 = 0, val2 = 0, dval = 0;
        val1 = attr1.doubleValueGet();
        if (attr2 != null && op != Op.AVG)
          val2 = attr2.doubleValueGet();
        switch (op)
        {
          case ADD: // DBL_ADD
            dval = val1 + val2;
            break;
          case SUB: // DBL_SUB
            dval = val1 - val2;
            break;
          case MUL: // DBL_MUL
            dval = val1 * val2;
            break;
          case DIV: // DBL_DIV
            if(val2 == 0d)
              throw new ExecException(ExecutionError.DIVIDE_BY_ZERO);
            dval = val1 / val2;
            break;
          case SUM_ADD: // DBL_SUM_ADD
            /*
             * DBL_SUM_ADD(x,y) = x + y if x is non null and y is non null 
             * = x, if x is non null and y is null 
             * = y, if x is null and y is non null
             * = null, if x and y are null
             */
            if (!attr1.bNull && !attr2.bNull)
              dval = val1 + val2;
            else if (!attr1.bNull)
              dval = val1;
            else if (!attr2.bNull)
              dval = val2;
            break;

          case SUM_SUB: // DBL_SUM_SUB
            /*
             * DBL_SUM_SUB(x,y) = x - y if x is non null and y is non null 
             * x, if x is non null and y is null 
             * y, if x is null and y is non null
             * null, if x and y are null
             */
            if (!attr1.bNull && !attr2.bNull)
              dval = val1 - val2;
            else if (!attr1.bNull)
              dval = val1;
            else
              assert (false);
            break;

          case TO_BIGDECIMAL: //DBL_TO_BIGDECIMAL
            BigDecimal val = new BigDecimal(String.valueOf(val1));
            ((BigDecimalAttrVal)dattr).nValueSet(val, val.precision(), 
                                                 val.scale());
            return;
            
          case TO_CHR1: //DBL_TO_CHR
            Scratch buf = scratchBuf.get();
            
            // Allocate and Cleaning temp Buffer
            StringBuilder tmpData = buf.charSeq;
            tmpData.delete(0, tmpData.length());
            
            char[] tmpArray = buf.charBuf;            
            
            tmpData.append(val1);
            //Read tmpData into tmpArray
            tmpData.getChars(0, tmpData.length(), tmpArray, 0);
            ((CharAttrVal)dattr).setValue(tmpArray, tmpData.length());            
            return;
            
          case NVL: // DBL_NVL
            dval = attr1.bNull ? val2 : val1;
            break;

          case CPY: // DBL_CPY
            dval = val1;
            break;

          case UMX: // DBL_UMX
            if (attr1.bNull)
            {
              dval = val2;
            }
            else if (attr2.bNull)
            {
              dval = val1;
            }
            else
            {
              dval = (val1 < val2) ? val2 : val1;
            }
            break;

          case UMN: // DBL_UMN
            if (attr1.bNull)
            {
              dval = val2;
            }
            else if (attr2.bNull)
            {
              dval = val1;
            }
            else
            {
              dval = (val1 > val2) ? val2 : val1;
            }
            break;

          case AVG: // DBL_AVG
            int ival2 = ((IntAttrVal) attr2).value;
            if(ival2 <= 0 || attr1.bNull)
            {
              dattr.bNull = true;
              return;
            }
            else
              dattr.bNull = false;
            if (attr1.bNull)
            {
              dval = (double) ival2;
            }
            else if (attr2.bNull)
            {
              dval = val1;
            }
            else
            {
              dval = val1 / (double) (1.0 * ival2);
            }
            ((DoubleAttrVal) dattr).value = dval;
            return;
          
          case MOD: //DOUBLE_MOD
            if(val2 == 0d)
              dval = val1;
            else
              dval = val1 % val2;
            break; 

          default:
            assert false;
        }
        ((DoubleAttrVal) dattr).value = dval;
        dattr.bNull = false;
      }
        break;
      case BIGDECIMAL :
      {
        BigDecimal val1 = BigDecimal.ZERO, val2 = BigDecimal.ZERO,
                   nval = BigDecimal.ZERO;
        val1 = attr1.bigDecimalValueGet();
        if (attr2 != null && op != Op.AVG)
          val2 = attr2.bigDecimalValueGet();
        switch (op)
        {
          case ADD: // BIGDECIMAL_ADD
            nval = val1.add(val2);
            break;
          case SUB: // BIGDECIMAL_SUB
            nval = val1.subtract(val2);
            break;
          case MUL: // BIGDECIMAL_MUL
            nval = val1.multiply(val2);
            break;
          case DIV: // BIGDECIMAL_DIV
            if(val2.compareTo(BigDecimal.ZERO) == 0)
              throw new ExecException(ExecutionError.DIVIDE_BY_ZERO);
            
            nval = val1.divide(val2, RoundingMode.HALF_UP);
            break;
          case SUM_ADD: // BIGDECIMAL_SUM_ADD
            /*
             * BIGDECIMAL_SUM_ADD(x,y) = x + y if x is non null and y is non null 
             * = x, if x is non null and y is null 
             * = y, if x is null and y is non null
             * = null, if x and y are null
             */
            if (!attr1.bNull && !attr2.bNull)
              nval = val1.add(val2);
            else if (!attr1.bNull)
              nval = val1;
            else if (!attr2.bNull)
              nval = val2;
            break;

          case SUM_SUB: // BIGDECIMAL_SUM_SUB
            /*
             * BIGDECIMAL_SUM_ADD(x,y) = x - y if x is non null and y is non null
             *  x, if x is non null and y is null 
             *  y, if x is null and y is non null
             * null, if x and y are null
             */
            if (!attr1.bNull && !attr2.bNull)
              nval = val1.subtract(val2);
            else if (!attr1.bNull)
              nval = val1;
            else
              assert (false);
            break;
            
          case TO_BIGDECIMAL:
            ((BigDecimalAttrVal)dattr).nValueSet(val1, val1.precision(),
                                                 val1.scale());
            return;

          case TO_CHR1: //BIGDECIMAL_TO_CHR
            Scratch buf = scratchBuf.get();
            
            // Allocate and Cleaning temp Buffer
            StringBuilder tmpData = buf.charSeq;
            tmpData.delete(0, tmpData.length());
            
            char[] tmpArray = buf.charBuf;            
            
            tmpData.append(val1);
            //Read tmpData into tmpArray
            tmpData.getChars(0, tmpData.length(), tmpArray, 0);
            ((CharAttrVal)dattr).setValue(tmpArray, tmpData.length());            
            return;
            
          case NVL: // BIGDECIMAL_NVL
            nval = attr1.bNull ? val2 : val1;
            break;

          case CPY: // BIGDECIMAL_CPY
            nval = val1;
            break;

          case UMX: // BIGDECIMAL_UMX
            if (attr1.bNull)
            {
              nval = val2;
            }
            else if (attr2.bNull)
            {
              nval = val1;
            }
            else
            {
              nval = (val1.compareTo(val2) < 0) ? val2 : val1;
            }
            break;

          case UMN: // BIGDECIMAL_UMN
            if (attr1.bNull)
            {
              nval = val2;
            }
            else if (attr2.bNull)
            {
              nval = val1;
            }
            else
            {
              nval = (val1.compareTo(val2) > 0) ? val2 : val1;
            }
            break;

          case AVG: // BIGDECIMAL_AVG
            int ival2 = ((IntAttrVal) attr2).value;
            if (attr1.bNull)
            {
              nval = new BigDecimal(ival2);
            }
            else if (attr2.bNull)
            {
              nval = val1;
            }
            else if(ival2 > 0) //to avoid divide by zero error
            {
              nval = val1.divide(new BigDecimal(ival2),RoundingMode.HALF_UP);
            }
            if(ival2 <= 0 || attr1.bNull)
            {
              dattr.bNull = true;
            }
            else
            {
              ((BigDecimalAttrVal) dattr).nValueSet(nval, 
                                                    nval.precision(),
                                                    nval.scale());
              dattr.bNull = false;
            }
            
            return;
          
          case MOD: //BIGDECIMAL_MOD
            if(val2.compareTo(BigDecimal.ZERO) == 0)
              nval = val1;
            else
              nval = val1.remainder(val2);
            break; 

          default:
            assert false;
        }
        ((BigDecimalAttrVal) dattr).nValueSet(nval, 
                                              nval.precision(),
                                              nval.scale());
        dattr.bNull = false;
      }
        break;
      case CHAR:
      {
        char[] val1 = null;
        int len1 = 0;
        char[] val2 = null;
        int len2 = 0;
        boolean destIsChar = true;
        
        if (op != Op.BYT_TO_HEX)
        {
          val1 = ((CharAttrVal) attr1).value;
          len1 = ((CharAttrVal) attr1).length;
        }
        if (attr2 != null && op != Op.SUBSTR && op != Op.LPAD && op != Op.RPAD)
        {
          val2 = ((CharAttrVal) attr2).value;
          len2 = ((CharAttrVal) attr2).length;
        }
        char[] dval = null;
        Scratch buf = scratchBuf.get();
        char[] tempArray = buf.charBuf;
        int dlen = 0;
        int i = 0;
        int j = 0;
        switch (op)
        {
          case CPY: // CHAR_CPY
            dval = val1;
            dlen = len1;
            break;

          case LOWER: // CHR_LOWER
           if (val1 != null)
              tempArray = buf.getCharBuf(val1.length);
            for(i=0; i<len1; i++)
              tempArray[i] = Character.toLowerCase(val1[i]);
            dval = tempArray;
            dlen = len1;
            break;
          case UPPER: // CHR_UPPER
            if (val1 != null)
              tempArray = buf.getCharBuf(val1.length);
            for(i=0; i<len1; i++)
              tempArray[i] = Character.toUpperCase(val1[i]);
            dval = tempArray;
            dlen = len1;
            break;
          case INITCAP:
            boolean isConvertible = true;
            if (val1 != null)
              tempArray = buf.getCharBuf(val1.length);
            for(i=0; i < len1; i++)
            {
              if(isConvertible && Character.isLetterOrDigit(val1[i]))
              {
                tempArray[i] = Character.toUpperCase(val1[i]);
                isConvertible = false;
              }
              else
              {
                tempArray[i] = Character.toLowerCase(val1[i]);
                if(!Character.isLetterOrDigit(val1[i]))
                  isConvertible = true;
              }
            }
            dval = tempArray;
            dlen = len1;
            break;
          case LTRIM1:
          case RTRIM1:
            if (val1 != null)
              tempArray = buf.getCharBuf(val1.length);

            if(op == Op.LTRIM1)
            {
              for(i=0; i < len1 && Character.getType(val1[i]) == 
                Character.SPACE_SEPARATOR ; i++);
              System.arraycopy(val1, i, tempArray, 0, len1-i);
              dlen = len1-i;
            }
            else if(op == Op.RTRIM1)
            {
              for(i=len1-1; i >= 0 && Character.getType(val1[i]) == 
                Character.SPACE_SEPARATOR; i--);
              System.arraycopy(val1, 0, tempArray, 0, i+1);
              dlen = i+1;
            }
            dval = tempArray;
            break;
          case LTRIM2:
          case RTRIM2:
            LinkedHashSet<Character> set = buf.charLinkedHashSet;
            for(i = 0; i < len2; i++)
              set.add(val2[i]);
            if (val1 != null)
              tempArray = buf.getCharBuf(val1.length);

            if(op == Op.LTRIM2)
            {
              for(j = 0; j < len1 && set.contains(val1[j]); j++);
              System.arraycopy(val1, j,tempArray, 0, len1-j);
              dlen = len1-j;
            }
            else if(op == Op.RTRIM2)
            {
              for(j = len1-1 ; j >= 0 && set.contains(val1[j]); j--);
              System.arraycopy(val1, 0, tempArray, 0, j+1);
              dlen = j+1;
            }
            dval = tempArray;
              
            break;
          case SUBSTR:
            if(((IntAttrVal)attr2).value <= len1 &&
                ((IntAttrVal)attr2).value > 0)
            {
              if (val1 != null)
                tempArray = buf.getCharBuf(val1.length);
              int attr2val = ((IntAttrVal) attr2).value;
              System.arraycopy(val1, attr2val-1 , tempArray, 0, len1 - attr2val + 1 );
              dval = tempArray;
              dlen = len1 - attr2val + 1;
            }
            break;
          case LPAD:
          case RPAD:
            if(((IntAttrVal)attr2).value > 0)
            {
              int attr2val     = ((IntAttrVal) attr2).value;
              final char BLANK = ' '; 
              
              if (val1 != null)
                tempArray = buf.getCharBuf(val1.length);

              if(attr2val <= len1)
                System.arraycopy(val1, 0, tempArray, 0, attr2val);
              else
              {
                if(op == Op.LPAD)
                {
                  for(i=0; i < attr2val -len1; i++)
                    tempArray[i] = BLANK;
                  System.arraycopy(val1, 0, tempArray, i, len1);
                }
                else if(op == Op.RPAD)
                {
                  System.arraycopy(val1, 0, tempArray, 0, len1);
                  for(i= len1; i < attr2val; i++)
                    tempArray[i] = BLANK;
                } 
              }
              dval = tempArray;
              dlen = attr2val;
            }
            break;
          case CONCAT: // CHR_CONCAT
            if (attr1.bNull)
            {
              dval = val2;
              dlen = len2;
            }
            else if (attr2.bNull)
            {
              dval = val1;
              dlen = len1;
            }
            else
            {
              if (val1 != null && val2 != null)
                tempArray = buf.getCharBuf(val1.length + val2.length);

              for (j = 0; j < len1; j++)
                tempArray[j] = val1[j];
              for (j = 0; j < len2; j++)
                tempArray[j + len1] = val2[j];
              dval = tempArray;
              dlen = len1 + len2;
            }
            break;

          case NVL: // CHR_NVL
            if (attr1.bNull)
            {
              dval = val2;
              dlen = len2;
            }
            else
            {
              dval = val1;
              dlen = len1;
            }
            break;

          case BYT_TO_HEX: // BYT_TO_HEX
          {
            byte[] bval1 = ((ByteAttrVal) attr1).value;
            int blen1 = ((ByteAttrVal) attr1).length;
            try
            {
              dval = Datatype.byteToHex(bval1, blen1);
              dlen = dval.length;
            } catch (CEPException e)
            {
              throw new ExecException(ExecutionError.INVALID_ATTR, e.getMessage());
            }
          }
            break;

          case UMX: // CHR_UMX
            if (attr1.bNull)
            {
              dval = val2;
              dlen = len2;
            }
            else if (attr2.bNull)
            {
              dval = val1;
              dlen = len1;
            }
            else
            {
              String value1 = new String(val1);
              String value2 = new String(val2);
              if ((value1.compareTo(value2)) < 0)
              {
                dval = val2;
                dlen = len2;
              }
              else
              {
                dval = val1;
                dlen = len1;
              }
            }
            break;

          case UMN: // CHR_UMN
            if (attr1.bNull)
            {
              dval = val2;
              dlen = len2;
            }
            else if (attr2.bNull)
            {
              dval = val1;
              dlen = len1;
            }
            else
            {
              String value1 = new String(val1);
              String value2 = new String(val2);
              if ((value1.compareTo(value2)) > 0)
              {
                dval = val2;
                dlen = len2;
              }
              else
              {
                dval = val1;
                dlen = len1;
              }
            }
            break;

            /**
             * Character to Number conversions 
             */
          case TO_INT:
            if (val1 != null)
            {
              String value1 = new String(val1);
              Integer dvalue = null;
              try
              {
               dvalue = Integer.parseInt(value1);
              }
              catch(NumberFormatException e)
              { 
                LogUtil.fine(LoggerType.TRACE, e.toString());
                throw new ExecException(ExecutionError.INVALID_NUMBER, value1,
                    "integer");
              }
              dattr.iValueSet(dvalue);
              dattr.bNull = false;
              destIsChar = false;
            }
            break;
          case TO_BIGINT:
            if (val1 != null)
            {
              String value1 = new String(val1);
              Long dvalue = null;
              try
              {
               dvalue = Long.parseLong(value1);
              }
              catch(NumberFormatException e)
              {
                LogUtil.fine(LoggerType.TRACE, e.toString());
                throw new ExecException(ExecutionError.INVALID_NUMBER, value1,
                    "bigint");
              }
              dattr.lValueSet(dvalue);
              dattr.bNull = false;
              destIsChar = false;
            }
            break;
          case TO_FLT:
            if (val1 != null)
            {
              String value1 = new String(val1);
              Float dvalue = null;
              try
              {
               dvalue = Float.parseFloat(value1);
              }
              catch(NumberFormatException e)
              {
                LogUtil.fine(LoggerType.TRACE, e.toString());
                throw new ExecException(ExecutionError.INVALID_NUMBER, value1, 
                    "float");
              }
              dattr.fValueSet(dvalue);
              dattr.bNull = false;
              destIsChar = false;
            }
            break;
          case TO_DBL:
            if (val1 != null)
            {
              String value1 = new String(val1);
              Double dvalue = null;
              try
              {
               dvalue = Double.parseDouble(value1);
              }
              catch(NumberFormatException e)
              {
                LogUtil.fine(LoggerType.TRACE, e.toString());
                throw new ExecException(ExecutionError.INVALID_NUMBER, value1,
                    "double");
              }
              dattr.dValueSet(dvalue);
              dattr.bNull = false;
              destIsChar = false;
            }
            break;
          case TO_BIGDECIMAL:
            if (val1 != null)
            {
              BigDecimal dvalue = null;
              try
              {
               dvalue = new BigDecimal(val1);
              }
              catch(NumberFormatException e)
              {
                LogUtil.fine(LoggerType.TRACE, e.toString());
                throw new ExecException(ExecutionError.INVALID_NUMBER, 
                    new String(val1), "bigdecimal");
              }
              dattr.nValueSet(dvalue, dvalue.precision(), dvalue.scale());
              dattr.bNull = false;
              destIsChar = false;
            }
            break;
            
          default:
            assert false;
        }
        if(destIsChar)
        {
          ((CharAttrVal) dattr).setValue(dval, dlen);
          dattr.bNull = false;
        }
      }
        break;

      case BYTE:
      {
        byte[] val1 = null;
        int len1 = 0;
        byte[] val2 = null;
        int len2 = 0;
        if (op != Op.HEX_TO_BYT)
        {
          val1 = ((ByteAttrVal) attr1).value;
          len1 = ((ByteAttrVal) attr1).length;
        }
        if (attr2 != null)
        {
          val2 = ((ByteAttrVal) attr2).value;
          len2 = ((ByteAttrVal) attr2).length;
        }
        byte[] dval = null;
        int dlen = 0;
        switch (op)
        {
          case CPY: // BYT_CPY
            dval = val1;
            dlen = len1;
            break;

          case NVL: // BYT_NVL
            if (attr1.bNull)
            {
              dval = val2;
              dlen = len2;
            }
            else
            {
              dval = val1;
              dlen = len1;
            }
            break;

          case CONCAT: // BYT_CONCAT
            if (attr1.bNull)
            {
              dval = val2;
              dlen = len2;
            }
            else if (attr2.bNull)
            {
              dval = val1;
              dlen = len1;
            }
            else
            {
              Scratch buf = scratchBuf.get();
              byte[] btmpArray = buf.byteBuf;
              for (int j = 0; j < len1; j++)
                btmpArray[j] = val1[j];
              for (int j = 0; j < len2; j++)
                btmpArray[j + len1] = val2[j];
              dval = btmpArray;
              dlen = len1 + len2;
            }
            break;

          case HEX_TO_BYT: // HEX_TO_BYT
            char[] cval1 = ((CharAttrVal) attr1).value;
            int clen1 = ((CharAttrVal) attr1).length;
            try
            {
              dval = Datatype.hexToByte(cval1, clen1);
              dlen = dval.length;
            } catch (CEPException e)
            {
              throw new ExecException(ExecutionError.INVALID_ATTR, e.getMessage());
            }
            break;

          case UMX: // BYT_UMX
            if (attr1.bNull)
            {
              dval = val2;
              dlen = len2;
            }
            else if (attr2.bNull)
            {
              dval = val1;
              dlen = len1;
            }
            else
            {
              int len = len1 < len2 ? len1 : len2;
              int i = 0;
              for (i = 0; i < len; i++)
              {
                if (val1[i] < val2[i])
                {
                  dval = val2;
                  dlen = len2;
                  break;
                }
                else if (val1[i] > val2[i])
                {
                  dval = val1;
                  dlen = len1;
                  break;
                }
              }
              if (i == len) // when val1==val2 after loop, value having higher
                            // length is bigger.
              {
                if (len1 > len2)
                {
                  dval = val1;
                  dlen = len1;
                }
                else
                {
                  dval = val2;
                  dlen = len2;
                }
              }
            }
            break;

          case UMN: // BYT_UMN
            if (attr1.bNull)
            {
              dval = val2;
              dlen = len2;
            }
            else if (attr2.bNull)
            {
              dval = val1;
              dlen = len1;
            }
            else
            {
              int len = len1 < len2 ? len1 : len2;
              int i = 0;
              for (i = 0; i < len; i++)
              {
                if (val1[i] < val2[i])
                {
                  dval = val1;
                  dlen = len1;
                  break;
                }
                else if (val1[i] > val2[i])
                {
                  dval = val2;
                  dlen = len2;
                  break;
                }
              }
              if (i == len) // when val1==val2 after loop, value with shorter
                            // length is smaller.
              {
                if (len1 > len2)
                {
                  dval = val2;
                  dlen = len2;
                }
                else
                {
                  dval = val1;
                  dlen = len1;
                }
              }
            }
            break;

          default:
            assert false;
        }
        ((ByteAttrVal) dattr).setValue(dval, dlen);
      }
      break;

        case TIMESTAMP:
        {
          long val1 = 0, val2 = 0, dval = 0;
          String destFormatString = null;
          TimestampFormat resultFormat = null;
          TimestampFormat inpAttrFormat1 = null;
          TimestampFormat inpAttrFormat2 = null;
                   
          if (op != Op.TIM_ADD && op != Op.TO_TIMESTAMP)
          {
            val1 = ((TimestampAttrVal)attr1).time;
            inpAttrFormat1 = ((TimestampAttrVal)attr1).format;
          }
          if (attr2 != null && op != Op.INTERVAL_ADD && op != Op.INTERVAL_SUB
              && op != Op.INTERVALYM_ADD && op != Op.INTERVALYM_SUB && op != Op.TO_CHR2)
          {
            val2 = ((TimestampAttrVal)attr2).time;
            inpAttrFormat2 = ((TimestampAttrVal)attr2).format;
          }
          if(attr2 != null && op == Op.TO_CHR2)
          {
            destFormatString = new String(((CharAttrVal)attr2).value);
          }
          
          switch(op)
          {
            case INTERVAL_ADD:  //TIM_INTERVAL_ADD
              val2 = ((IntervalAttrVal)attr2).interval;
              // interval value is in the unit of nanoseconds
              //val2 = val2 / 1000000l;
              dval = val1 + val2; 
              resultFormat = inpAttrFormat1;
              break;
            case INTERVAL_SUB:  //TIM_INTERVAL_SUB
              val2 = ((IntervalAttrVal)attr2).interval;
              // interval value is in the unit of nanoseconds
              //val2 = val2 / 1000000l;
              dval = val1 - val2; 
              resultFormat = inpAttrFormat1;
              break;

            case TIM_ADD:  //INTERVAL_TIM_ADD & INTERVALYM_TIM_ADD
              if(attr1 instanceof IntervalAttrVal)
              {
                val1 = (long)((IntervalAttrVal)attr1).interval;
                //val1 = val1 / 1000000l;
                dval = val1 + val2;
                resultFormat = inpAttrFormat2;
              }
              else
              {
                assert attr1 instanceof IntervalYMAttrVal;
                // interval value as number of months
                Long lval1 = ((IntervalYMAttrVal)attr1).interval;
                Calendar cal = Calendar.getInstance();
                // Note: Timestamp is in nanosecond, so convert this to millis
                // before calendar addition
                long val2Millis = val2 / 1000000l;
                long val2Offset = val2 - val2Millis * 1000000l;
                cal.setTimeInMillis(val2);
                cal.add(Calendar.MONTH, lval1.intValue());
                long resultMillis = cal.getTimeInMillis();
                dval = resultMillis * 1000000l + val2Offset;        
                resultFormat = inpAttrFormat2;
              }              
              break;
              
            case INTERVALYM_ADD:  //TIM_INTERVALYM_ADD
              {
                Long lVal2 = ((IntervalYMAttrVal)attr2).interval;              
                Calendar cal = Calendar.getInstance();
                // Note: Timestamp is in nanosecond, so convert this to millis
                // before calendar addition
                long val1Millis = val1 / 1000000l;
                long val1Offset = val1 - val1Millis * 1000000l;
                cal.setTimeInMillis(val1Millis);
                // interval value is in the unit of months
                cal.add(Calendar.MONTH, lVal2.intValue());
                long resultMillis = cal.getTimeInMillis();
                dval = resultMillis * 1000000l + val1Offset;
                resultFormat = inpAttrFormat1;
              }
              break;
            case INTERVALYM_SUB:  //TIM_INTERVALYM_SUB
              {
                Long lval2 = ((IntervalYMAttrVal)attr2).interval;              
                Calendar cal2 = Calendar.getInstance();
                // Note: Timestamp is in nanosecond, so convert this to millis
                // before calendar addition
                long val1Millis = val1 / 1000000l;
                long val1Offset = val1 - val1Millis * 1000000l;
                cal2.setTimeInMillis(val1Millis);
                // interval value is in the unit of months
                cal2.add(Calendar.MONTH, 0-lval2.intValue());
                long resultMillis = cal2.getTimeInMillis();
                dval = resultMillis * 1000000l + val1Offset;
                resultFormat = inpAttrFormat1;
              }
              break;
            case NVL:   //TIM_NVL
              dval = attr1.bNull ? val2 : val1;
              resultFormat = attr1.bNull ? inpAttrFormat2 : inpAttrFormat1;
              break;
              
            case CPY:   //TIM_CPY
              dval = val1;
              resultFormat = inpAttrFormat1;
              break;
              
            case UMX:   //TIM_UMX
              if (attr1.bNull)
              {
                dval = val2;
                resultFormat = inpAttrFormat2;
              } 
              else if (attr2.bNull)
              {
                dval = val1;
                resultFormat = inpAttrFormat1;
              } 
              else
              {
                dval = (val1 < val2) ? val2 : val1;
                resultFormat = (val1 < val2) ? inpAttrFormat2 : inpAttrFormat1;
              }
              break;

            case UMN:   //TIM_UMN
              if (attr1.bNull)
              {
                dval = val2;
                resultFormat = inpAttrFormat2;
              }
              else if (attr2.bNull)
              {
                dval = val1;
                resultFormat = inpAttrFormat1;
              } 
              else
              {
                dval = (val1 > val2) ? val2 : val1;
                resultFormat = (val1 > val2) ? inpAttrFormat2 : inpAttrFormat1;
              }
              break;
            
            case TO_BIGINT: //TIMESTAMP_TO_BIGINT
              ((BigintAttrVal)dattr).value = val1;
              dattr.bNull = false;
              return;
              
            case TO_CHR1: //TIMESTAMP_TO_CHAR
            case TO_CHR2:
              Scratch buf = scratchBuf.get();
              
              CEPDateFormat sdf1 = CEPDateFormat.getInstance();
              //sdf1.setLenient(false); ??? what is this for? Lenient is supposed to be used with parsing.
              
              // Allocate and Cleaning temp Buffer
              StringBuilder tmpData = buf.charSeq;
              tmpData.delete(0, tmpData.length());
              
              char[] tmpArray = buf.charBuf;            
              
              // Reason for synchronized SimpleDateFormat object sdf1:
              // sdf1 object is oracle.cep.common.Constant.TIMESTAMP_FORMAT
              // sdf1 will be referred by all threads during execution time.
              String formattedOutStr = null;
              try
              {
                if(destFormatString != null)
                {
                  if(inpAttrFormat1 != null)
                  {
                    formattedOutStr = sdf1.format(val1, destFormatString, 
                      inpAttrFormat1.getTimeZone());
                  }
                  else
                  {
                    formattedOutStr = sdf1.format(val1, destFormatString);
                  }
                }
                else
                {
                  formattedOutStr = sdf1.format(val1, inpAttrFormat1);
                }
              }
              catch(ParseException e)
              {
                throw new ExecException(ExecutionError.INVALID_TIMEFORMAT);
              }
              
              synchronized(sdf1)
              {
                tmpData.append(formattedOutStr);
              }
              //Read tmpData into tmpArray
              tmpData.getChars(0, tmpData.length(), tmpArray, 0);
              ((CharAttrVal)dattr).setValue(tmpArray, tmpData.length());            
              return;
              
            default: 
              assert false;
          }
          ((TimestampAttrVal)dattr).time = dval;
          ((TimestampAttrVal)dattr).format = resultFormat;
          dattr.bNull = false;
        }
        break;
        
        case INTERVAL:
        {
          long val1 = 0, val2 = 0, dval = 0;
          if (op != Op.TIM_SUB)
          {
            val1 = ((IntervalAttrVal)attr1).interval;
            if (attr2 != null && op != Op.AVG)
              val2 = ((IntervalAttrVal)attr2).interval;
          }
                    
          // If there are two interval attributes, Set precisions to maximum
          // Else destination's format will be same as attribute's format
          IntervalFormat destinationFormat = null;
          if(attr2 != null || op == Op.TIM_SUB)
          {
            try
            {
              destinationFormat = 
                new IntervalFormat(TimeUnit.DAY, TimeUnit.SECOND, 9, 9);              
            } 
            catch (CEPException e)
            {
              // As this is a valid interval format, there should be no exception
              assert false;
            }
          }
          else
            destinationFormat = ((IntervalAttrVal)attr1).getFormat();
          
          // Set Result Attribute's interval format if the type of result is 
          // an interval value
          if(dattr instanceof IntervalAttrVal)
            ((IntervalAttrVal)dattr).setFormat(destinationFormat);
          
          switch(op)
          {
            case ADD: //INTERVAL_ADD
              dval = val1 + val2; 
              break;
            case SUB: //INTERVAL_SUB
              dval = val1 - val2; 
              break;
            case TIM_SUB: //TIM_SUB
              val1 = ((TimestampAttrVal)attr1).time;
              val2 = ((TimestampAttrVal)attr2).time;               
              // Note: dval is in the unit of nanoseconds as val1 and vol2
              // represents timestamp values
              dval = val1 - val2;
              //TODO: Database doesn't allow subtraction between two timestamp
              // values if any of them is having format mentioned.
              break;
            case SUM_ADD: //INTERVAL_SUM_ADD
              /*
              INT_SUM_ADD(x,y) = x + y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null 
              */
              if (!attr1.bNull && !attr2.bNull)
                 dval = val1 + val2;
              else if(!attr1.bNull)
                dval = val1;
              else if(!attr2.bNull)
                dval = val2;
              break;
              
            case SUM_SUB: //INTERVAL_SUM_SUB
              /*
              INT_SUM_SUB(x,y) = x - y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null 
              */
              if (!attr1.bNull && !attr2.bNull)
                dval = val1 - val2;
              else if(!attr1.bNull)
                dval = val1;
              else
                assert(false);
              break;
              
            case AVG:   //INTERVAL_AVG
              int ival2 = ((IntAttrVal) attr2).value;
              if(ival2 <= 0 || attr1.bNull)
              {
                dattr.bNull = true;
                return;
              }
              else
                dattr.bNull = false;
              if (attr1.bNull)
              {
                dval = (long) ival2;
              }
              else if (attr2.bNull)
              {
                dval = val1;
              }
              else
              {
                dval = val1 / ival2;
              }
              break;
            
            case NVL:   //INTERVAL_NVL
              dval = attr1.bNull ? val2 : val1;
              break;
              
            case CPY: //INTERVAL_CPY
              dval = val1;
              break;
              
            case UMX:   //INTERVAL_UMX
              if (attr1.bNull)
              {
                dval = val2;
              } else if (attr2.bNull)
              {
                dval = val1;
              } else
              {
                dval = (val1 < val2) ? val2 : val1;
              }
              break;

            case UMN:   //INTERVAL_UMN
              if (attr1.bNull)
              {
                dval = val2;
              } else if (attr2.bNull)
              {
                dval = val1;
              } else
              {
                dval = (val1 > val2) ? val2 : val1;
              }
              break;
            
            case TO_CHR1: //INTERVAL_TO_CHAR
              Scratch buf = scratchBuf.get();
              
              // Allocate and Cleaning temp Buffer
              StringBuilder tmpData = buf.charSeq;
              tmpData.delete(0, tmpData.length());
              
              char[] tmpArray = buf.charBuf;
              String strInterval = 
                  oracle.cep.common.IntervalConverter.getDSInterval(
                    val1, destinationFormat);
              
              tmpData.append(strInterval);
              //Read tmpData into tmpArray
              tmpData.getChars(0, tmpData.length(), tmpArray, 0);
              ((CharAttrVal)dattr).setValue(tmpArray, tmpData.length());            
              return;
              
            default: 
              assert false;
          }
          ((IntervalAttrVal)dattr).interval = dval;
          dattr.bNull = false;
        }
      break;

        case INTERVALYM:
        {
          long val1 = 0, val2 = 0, dval = 0;
          if (op != Op.TIM_SUB)
          {
            val1 = ((IntervalYMAttrVal)attr1).interval;
            if (attr2 != null && op != Op.AVG)
              val2 = ((IntervalYMAttrVal)attr2).interval;
          }
          
          // If ther are two interval attributes, set precision to maximum
          // Else destination format will be same as first input attribute
          IntervalFormat destinationFormat = null;          
          if(attr2 != null || op == Op.TIM_SUB)
          {
            try
            {
              destinationFormat 
                = new IntervalFormat(TimeUnit.YEAR, TimeUnit.MONTH, 9, true);
              
            } catch (CEPException e)
            {
              // This exception shouldn't be raised as the interval is 
              // correctly formatted.
              assert false;
            }
          }
          else
          {
            destinationFormat =((IntervalYMAttrVal)attr1).attrType.getIntervalFormat();
            if(destinationFormat == null && attr1 instanceof IntervalYMAttrVal)
              destinationFormat =((IntervalYMAttrVal)attr1).getFormat();
          }
          
          if(dattr instanceof IntervalYMAttrVal)
            ((IntervalYMAttrVal)dattr).setFormat(destinationFormat);
          
          switch(op)
          {
            case ADD: //INTERVALYMADD
              dval = val1 + val2; 
              break;
            case SUB: //INTERVALYM_SUB
              dval = val1 - val2; 
              break;
            case TIM_SUB: //TIM_SUB
              val1 = ((TimestampAttrVal)attr1).time;
              val2 = ((TimestampAttrVal)attr2).time;
              dval = val1 - val2; 
              // Note: dval is in the unit of nanoseconds as val1 and vol2
              // represents timestamp values
              // Convert it to number of months
              dval = dval / (30l*24l*3600l*1000000000l);              
              break;
            case SUM_ADD: //INTERVALYM_SUM_ADD
              /*
              INT_SUM_ADD(x,y) = x + y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null 
              */
              if (!attr1.bNull && !attr2.bNull)
                 dval = val1 + val2;
              else if(!attr1.bNull)
                dval = val1;
              else if(!attr2.bNull)
                dval = val2;
              break;
              
            case SUM_SUB: //INTERVALYM_SUM_SUB
              /*
              INT_SUM_SUB(x,y) = x - y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null 
              */
              if (!attr1.bNull && !attr2.bNull)
                dval = val1 - val2;
              else if(!attr1.bNull)
                dval = val1;
              else
                assert(false);
              break;
              
            case AVG:   //INTERVALYM_AVG
              int ival2 = ((IntAttrVal) attr2).value;
              if(ival2 <= 0 || attr1.bNull)
              {
                dattr.bNull = true;
                return;
              }
              else
                dattr.bNull = false;
              if (attr1.bNull)
              {
                dval = (long) ival2;
              }
              else if (attr2.bNull)
              {
                dval = val1;
              }
              else
              {
                dval = val1 / ival2;
              }
              break;
              
            case NVL:   //INTERVALYM_NVL
              dval = attr1.bNull ? val2 : val1;
              break;
              
            case CPY: //INTERVALYM_CPY
              dval = val1;
              break;
              
            case UMX:   //INTERVALYM_UMX
              if (attr1.bNull)
              {
                dval = val2;
              } else if (attr2.bNull)
              {
                dval = val1;
              } else
              {
                dval = (val1 < val2) ? val2 : val1;
              }
              break;

            case UMN:   //INTERVALYM_UMN
              if (attr1.bNull)
              {
                dval = val2;
              } else if (attr2.bNull)
              {
                dval = val1;
              } else
              {
                dval = (val1 > val2) ? val2 : val1;
              }
              break;
            
            case TO_CHR1: //INTERVALYM_TO_CHAR
              Scratch buf = scratchBuf.get();
              
              // Allocate and Cleaning temp Buffer
              StringBuilder tmpData = buf.charSeq;
              tmpData.delete(0, tmpData.length());
              
              char[] tmpArray = buf.charBuf;
              
              String strIntervalValue = 
                oracle.cep.common.IntervalConverter.getYMInterval(
                  val1,
                  destinationFormat);
              
              tmpData.append(strIntervalValue);
              
              //Read tmpData into tmpArray
              tmpData.getChars(0, tmpData.length(), tmpArray, 0);
              ((CharAttrVal)dattr).setValue(tmpArray, tmpData.length());            
              return;
              
            default: 
              assert false;
          }
          ((IntervalYMAttrVal)dattr).interval = dval;          
          dattr.bNull = false;
        }
      break;
      case XMLTYPE:
      {
        char[] dval = ((XmltypeAttrVal) attr1).xValueGet();
        int dlen = 0;
        switch (op)
        {
          case CPY: // XMLTYPE_CPY
            if(((XmltypeAttrVal)attr1).isObject)
              dlen = dval.length;
            else
              dlen = ((XmltypeAttrVal) attr1).xLengthGet();
            ((XmltypeAttrVal) dattr).setValue(dval, dlen);
            dattr.bNull = false;
            break;
          
          case TO_CHR1: //XMLTYPE_TO_CHAR
            if(((XmltypeAttrVal)attr1).isObject)
              dlen = dval.length;
            else
              dlen = ((XmltypeAttrVal) attr1).xLengthGet();
            ((CharAttrVal)dattr).setValue(dval, dlen); 
            dattr.bNull = false;
            break;
           
          default:
            assert false;
        }
      }
      break;

      case OBJECT:
      {
        switch (op)
        {
          case CPY: // OBJ_CPY
            Object dval = ((ObjectAttrVal) attr1).oValueGet();
            ((ObjectAttrVal) dattr).setValue(dval);
            dattr.bNull = false;
            break;
          default:
            assert false;
        }
      }
      break;

      case BOOLEAN:
      {
        boolean val1 = false, val2 = false, dval = false;
        val1 = ((BooleanAttrVal) attr1).value;
        switch (op)
        {
          case NVL: // BOOLEAN_NVL
            val2 = ((BooleanAttrVal) attr2).value;
            dval = attr1.bNull ? val2 : val1;
            ((BooleanAttrVal) dattr).setValue(dval);
            dattr.bNull = false;
            break;

          case CPY: // BOOLEAN_CPY
            dval = val1;
            ((BooleanAttrVal) dattr).setValue(dval);
            dattr.bNull = false;
            break;

          case TO_CHR1: //BOOLEAN_TO_CHAR
            Scratch buf = scratchBuf.get();
            
            // Allocate and Cleaning temp Buffer
            StringBuilder tmpData = buf.charSeq;
            tmpData.delete(0, tmpData.length());
            
            char[] tmpArray = buf.charBuf;            
            
            tmpData.append(val1);
            //Read tmpData into tmpArray
            tmpData.getChars(0, tmpData.length(), tmpArray, 0);
            ((CharAttrVal)dattr).setValue(tmpArray, tmpData.length());            
            return;

          default:
            assert false;
        }
      }
      break;
      
      default:
        assert false;
    }
  }
  
  @Override
  public void writeExternal(ObjectOutput out, IPersistenceContext ctx)
      throws IOException
  {
    writeExternal(out);
  }
  @Override
  public void readExternal(ObjectInput in, IPersistenceContext ctx)
      throws IOException, ClassNotFoundException
  {
    readExternal(in);
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    super.writeExternal(out);
    out.writeObject(attrs);
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {    
    super.readExternal(in);
    attrs = (AttrVal[]) in.readObject();    
  }
}
