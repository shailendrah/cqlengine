/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/Datatype.java /main/28 2012/01/20 11:47:14 sbishnoi Exp $ */
/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
 Enumeration of CEP datatypes supported

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
 sbishnoi    10/03/11 - timestamp format
 sbishnoi    07/16/11 - adding new datatype interval year to month
    sbishnoi    09/27/10 - XbranchMerge sbishnoi_bug-10145105_ps3 from
                           st_pcbpel_11.1.1.4.0
 sbishnoi    09/24/10 - removing BigDecimal.class as an impl type of nativetype
                        bigdecimal
 udeshmuk    06/11/10 - add api to get CQL datatype from string
 udeshmuk    01/26/10 - add getSqlTypeEnumValue
 udeshmuk    01/12/10 - API to get Datatype from sqltype given as a string
 sborah      06/15/09 - support for BigDecimal
 hopark      03/05/09 - add opaque type
    hopark      02/17/09 - support boolean as external datatype
 parujain    02/13/09 - sql types
 hopark      02/02/09 - objtype support
 skmishra    10/14/08 - annotating with javadocs
 skmishra    08/22/08 - removing ExecException
 mthatte     03/19/08 - adding xmltype to all methods
 mthatte     03/13/08 - adding method to get public CEP datatypes
 hopark      02/05/08 - parameterized error
 udeshmuk    01/29/08 - support for double datatype.
 udeshmuk    01/11/08 - add a new datatype unknown for null handling.
 najain      10/19/07 - add xmltype
 mthatte     11/04/07 - methods to support SQLTypeDescriptor
 mthatte     10/17/07 - adding global constants for getLength()
 mthatte     10/05/07 - adding getTypeName(int)
 mthatte     09/26/07 - 
 najain      04/22/07 - add toType
 dlenkov     12/08/06 - added byteToHex with length
 parujain    11/21/06 - type conversions
 hopark      11/16/06 - add BIGINT
 dlenkov     11/16/06 - overload resolution
 dlenkov     10/25/06 - byte handling support
 parujain    10/06/06 - interval datatype
 najain      09/21/06 - add toString
 anasrini    07/16/06 - add OBJECT, VOID; needed by user defined 
 aggregations support 
 najain      03/24/06 - add BOOLEAN, TIMESTAMP
 anasrini    02/08/06 - Creation
 anasrini    02/08/06 - Creation
 anasrini    02/08/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/Datatype.java /main/26 2010/09/28 03:41:33 sbishnoi Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.common;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.extensibility.type.IType;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.DatabaseMetaData;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enumeration of CEP datatypes supported
 * 
 * @since 1.0
 */

public class Datatype implements Externalizable, IType
{
  private static final long serialVersionUID = -5773751087581149119L;
  
  // FIXME The extensible String type is mapped into CHAR. In this case,
  //  when a parameter is declared as String, we need to provide a default length, as we don't to force
  //  the user to type 'java.lang.String()(INT)'
  public static final int DEFAULT_MAX_CHAR_LEN = 1024;
  public static final int DEFAULT_MAX_BYTES_LEN = 1024;

  public static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.UNNECESSARY;
  public static final int DEFAULT_SCALE = 64;

  public static final int FLOAT_PRECISION = 32;
  public static final int DOUBLE_PRECISION = 64;
  public static final int SHORT_PRECISION = 15;
  public static final int INT_PRECISION = 31;
  public static final int BIGINT_PRECISION = 63;


  static Map<Integer, Datatype> s_sqlTypeMap = new HashMap<Integer, Datatype>();
  static Map<Integer, String> s_sqlTypeNames = new HashMap<Integer, String>();
  static Map<String, Integer> s_sqlNameTypes = new HashMap<String, Integer>();
  static Map<String, Datatype> s_cqlNameTypes = new HashMap<String, Datatype>();
  static Map<Class, Datatype> s_classToTypesMap = new HashMap<Class, Datatype>();

  static Datatype[] s_dataTypes = new Datatype[Kind.values().length];
  static Map<Datatype, Datatype> s_dataTypesMap = new ConcurrentHashMap<Datatype, Datatype>();
  static Set<Class<?>> s_primitiveTypes = new HashSet<Class<?>>() {{
	  add(int.class); add(long.class); add(float.class); add(double.class); add(boolean.class);
  }};
  public static Datatype INT = new Datatype(Kind.INT, "int", int.class, Constants.INTEGER_LENGTH, 0,
        Types.INTEGER, "oracle.cep.dataStructures.external.IntAttributeValue", DatabaseMetaData.typeNoNulls); 
  public static Datatype BIGINT = new Datatype(Kind.BIGINT, "bigint", long.class, Constants.BIGINT_LENGTH, 0, 
        Types.BIGINT, "oracle.cep.dataStructures.external.BigintAttributeValue", DatabaseMetaData.typeNoNulls); 
  public static Datatype FLOAT = new Datatype(Kind.FLOAT, "float", float.class, Constants.FLOAT_LENGTH,  23,
        Types.FLOAT, "oracle.cep.dataStructures.external.FloatAttributeValue", DatabaseMetaData.typeNoNulls); 
  public static Datatype DOUBLE = new Datatype(Kind.DOUBLE, "double", double.class, Constants.DOUBLE_LENGTH, 52, 
        Types.DOUBLE, "oracle.cep.dataStructures.external.DoubleAttributeValue", DatabaseMetaData.typeNoNulls);
  //Note: Implementation type of Bigdecimal is changed to Datatype.class so that java.lang.BigDecimal will be
  // treated as java type and not convert to cql bigdecimal type
  public static Datatype BIGDECIMAL = getDecimalType(Constants.BIGDECIMAL_PRECISION, Constants.BIGDECIMAL_LENGTH); 
  public static Datatype BYTE = getByteType(DEFAULT_MAX_BYTES_LEN);
  public static Datatype CHAR = getCharType(DEFAULT_MAX_CHAR_LEN);
  public static Datatype BOOLEAN = new Datatype(Kind.BOOLEAN, "boolean", boolean.class, Constants.BOOLEAN_LENGTH,  0,
        Types.BOOLEAN, "oracle.cep.dataStructures.external.BooleanAttributeValue", DatabaseMetaData.typeNoNulls); 
  public static Datatype TIMESTAMP = getTimestampType(null, null);
  public static Datatype OBJECT = new Datatype(Kind.OBJECT, "object", Object.class, Constants.OBJECT_LENGTH,  0,
        Types.JAVA_OBJECT, "oracle.cep.dataStructures.external.ObjAttributeValue",  DatabaseMetaData.typeNullable);
  
  // INTERVAL DAY [(day_precision)] TO SECOND [(fractional_seconds)]
  // Default day_precision is TWO. Default fractional_seconds is SIX.
  public static Datatype INTERVAL = getIntervalType(null);
  
  // INTERVAL YEAR [(year_precision)] TO MONTH
  // Default year_precision is TWO.
  public static Datatype INTERVALYM = getIntervalYMType(null);
  
  public static Datatype VOID = new Datatype(Kind.VOID, "void", void.class, -1, -1,
        Types.OTHER, null,  DatabaseMetaData.typeNullableUnknown);
  public static Datatype XMLTYPE = getXMLType(Constants.MAX_XMLTYPE_LENGTH); 
  public static Datatype UNKNOWN = new Datatype(Kind.UNKNOWN, "unknown", null, -1, -1,
        -1, null,  DatabaseMetaData.typeNullableUnknown);
  
  public Kind kind;
  public String typeName;
  int   sqlType;
  String attributeValueType;
  Class<?> implementationType;
  int    length;
  int    precision;
  RoundingMode roundingMode= DEFAULT_ROUNDING_MODE;
  short  nullable;
  
  /** format specification for interval data types */
  IntervalFormat intervalFormat;
  
  /** format specification for timestamp data types */
  TimestampFormat timestampFormat;
  
  static
  {
    s_sqlTypeMap.put(Types.ARRAY, Datatype.OBJECT);
    s_sqlTypeMap.put(Types.BIGINT, Datatype.BIGINT);
    s_sqlTypeMap.put(Types.BINARY, Datatype.BYTE);
    s_sqlTypeMap.put(Types.BIT, Datatype.BOOLEAN);
    s_sqlTypeMap.put(Types.BLOB, Datatype.BYTE);
    s_sqlTypeMap.put(Types.BOOLEAN, Datatype.BOOLEAN);
    s_sqlTypeMap.put(Types.CHAR, Datatype.CHAR);
    s_sqlTypeMap.put(Types.CLOB, Datatype.CHAR);
    s_sqlTypeMap.put(Types.DATALINK, Datatype.VOID);
    s_sqlTypeMap.put(Types.DATE, Datatype.TIMESTAMP);
    s_sqlTypeMap.put(Types.DECIMAL, Datatype.DOUBLE);
    s_sqlTypeMap.put(Types.DISTINCT, Datatype.VOID);
    s_sqlTypeMap.put(Types.DOUBLE, Datatype.DOUBLE);
    s_sqlTypeMap.put(Types.FLOAT, Datatype.DOUBLE);
    s_sqlTypeMap.put(Types.INTEGER, Datatype.INT);
    s_sqlTypeMap.put(Types.JAVA_OBJECT, Datatype.OBJECT);
    s_sqlTypeMap.put(Types.LONGVARBINARY, Datatype.OBJECT);
    s_sqlTypeMap.put(Types.LONGVARCHAR, Datatype.OBJECT);
    s_sqlTypeMap.put(Types.NULL, Datatype.VOID);
    s_sqlTypeMap.put(Types.NUMERIC, Datatype.BIGDECIMAL);
    s_sqlTypeMap.put(Types.OTHER, Datatype.OBJECT);
    s_sqlTypeMap.put(Types.REAL, Datatype.FLOAT);
    s_sqlTypeMap.put(Types.REF, Datatype.OBJECT);
    s_sqlTypeMap.put(Types.SMALLINT, Datatype.INT);
    s_sqlTypeMap.put(Types.STRUCT, Datatype.OBJECT);
    s_sqlTypeMap.put(Types.TIME, Datatype.TIMESTAMP);
    s_sqlTypeMap.put(Types.TIMESTAMP, Datatype.TIMESTAMP);
    s_sqlTypeMap.put(Types.TINYINT, Datatype.INT);
    s_sqlTypeMap.put(Types.VARBINARY, Datatype.BYTE);
    s_sqlTypeMap.put(Types.VARCHAR, Datatype.CHAR);

    s_sqlTypeNames.put(Types.ARRAY, "ARRAY");
    s_sqlTypeNames.put(Types.BIGINT, "BIGINT");
    s_sqlTypeNames.put(Types.BINARY, "BINARY");
    s_sqlTypeNames.put(Types.BIT, "BIT");
    s_sqlTypeNames.put(Types.BLOB, "BLOB");
    s_sqlTypeNames.put(Types.CHAR, "CHAR");
    s_sqlTypeNames.put(Types.CLOB, "CLOB");
    s_sqlTypeNames.put(Types.DATE, "DATE");
    s_sqlTypeNames.put(Types.DECIMAL, "DECIMAL");
    s_sqlTypeNames.put(Types.DISTINCT, "DISTINCT");
    s_sqlTypeNames.put(Types.DOUBLE, "DOUBLE");
    s_sqlTypeNames.put(Types.FLOAT, "FLOAT");
    s_sqlTypeNames.put(Types.INTEGER, "INTEGER");
    s_sqlTypeNames.put(Types.JAVA_OBJECT, "JAVA_OBJECT");
    s_sqlTypeNames.put(Types.LONGVARBINARY, "LONGVARBINARY");
    s_sqlTypeNames.put(Types.LONGVARCHAR, "LONGVARCHAR");
    s_sqlTypeNames.put(Types.NULL, "NULL");
    s_sqlTypeNames.put(Types.NUMERIC, "NUMERIC");
    s_sqlTypeNames.put(Types.OTHER, "OTHER");
    s_sqlTypeNames.put(Types.REAL, "REAL");
    s_sqlTypeNames.put(Types.REF, "REF");
    s_sqlTypeNames.put(Types.SMALLINT, "SMALLINT");
    s_sqlTypeNames.put(Types.STRUCT, "STRUCT");
    s_sqlTypeNames.put(Types.TIME, "TIME");
    s_sqlTypeNames.put(Types.TIMESTAMP, "TIMESTAMP");
    s_sqlTypeNames.put(Types.TINYINT, "TINYINT");
    s_sqlTypeNames.put(Types.VARBINARY, "VARBINARY");
    s_sqlTypeNames.put(Types.VARCHAR, "VARCHAR");
    
    s_sqlNameTypes.put("ARRAY", Types.ARRAY);
    s_sqlNameTypes.put("BIGINT", Types.BIGINT);
    s_sqlNameTypes.put("BINARY", Types.BINARY);
    s_sqlNameTypes.put("BIT", Types.BIT);
    s_sqlNameTypes.put("BLOB", Types.BLOB);
    s_sqlNameTypes.put("CHAR", Types.CHAR);
    s_sqlNameTypes.put("CLOB", Types.CLOB);
    s_sqlNameTypes.put("DATE", Types.DATE);
    s_sqlNameTypes.put("DECIMAL", Types.DECIMAL);
    s_sqlNameTypes.put("DISTINCT", Types.DISTINCT);
    s_sqlNameTypes.put("DOUBLE", Types.DOUBLE);
    s_sqlNameTypes.put("FLOAT", Types.FLOAT);
    s_sqlNameTypes.put("INTEGER", Types.INTEGER);
    s_sqlNameTypes.put("JAVA_OBJECT", Types.JAVA_OBJECT);
    s_sqlNameTypes.put("LONGVARBINARY", Types.LONGVARBINARY);
    s_sqlNameTypes.put("LONGVARCHAR", Types.LONGVARCHAR);
    s_sqlNameTypes.put("NULL", Types.NULL);
    s_sqlNameTypes.put("NUMERIC", Types.NUMERIC);
    s_sqlNameTypes.put("OTHER", Types.OTHER);
    s_sqlNameTypes.put("REAL", Types.REAL);
    s_sqlNameTypes.put("REF", Types.REF);
    s_sqlNameTypes.put("SMALLINT", Types.SMALLINT);
    s_sqlNameTypes.put("STRUCT", Types.STRUCT);
    s_sqlNameTypes.put("TIME", Types.TIME);
    s_sqlNameTypes.put("TIMESTAMP", Types.TIMESTAMP);
    s_sqlNameTypes.put("TINYINT", Types.TINYINT);
    s_sqlNameTypes.put("VARBINARY", Types.VARBINARY);
    s_sqlNameTypes.put("VARCHAR", Types.VARCHAR);
    s_sqlNameTypes.put("VARCHAR2", Types.VARCHAR);
    
    s_cqlNameTypes.put("int", Datatype.INT);
    s_cqlNameTypes.put("bigint", Datatype.BIGINT);
    s_cqlNameTypes.put("float", Datatype.FLOAT);
    s_cqlNameTypes.put("double", Datatype.DOUBLE);
    s_cqlNameTypes.put("number", Datatype.BIGDECIMAL);
    s_cqlNameTypes.put("byte", Datatype.BYTE);
    s_cqlNameTypes.put("char", Datatype.CHAR);
    s_cqlNameTypes.put("boolean", Datatype.BOOLEAN);
    s_cqlNameTypes.put("timestamp", Datatype.TIMESTAMP);
    s_cqlNameTypes.put("object", Datatype.OBJECT);
    s_cqlNameTypes.put("interval", Datatype.INTERVAL);
    s_cqlNameTypes.put("intervalym", Datatype.INTERVALYM);
    s_cqlNameTypes.put("void", Datatype.VOID);
    s_cqlNameTypes.put("xmltype", Datatype.XMLTYPE);
    s_cqlNameTypes.put("unknown", Datatype.UNKNOWN);

    s_classToTypesMap.put(int.class, Datatype.INT);
    s_classToTypesMap.put(Integer.class, Datatype.INT);
    s_classToTypesMap.put(long.class, Datatype.BIGINT);
    s_classToTypesMap.put(Long.class, Datatype.BIGINT);
    s_classToTypesMap.put(float.class, Datatype.FLOAT);
    s_classToTypesMap.put(Float.class, Datatype.FLOAT);
    s_classToTypesMap.put(double.class, Datatype.DOUBLE);
    s_classToTypesMap.put(Double.class, Datatype.DOUBLE);
    s_classToTypesMap.put(BigDecimal.class, Datatype.BIGDECIMAL);
    s_classToTypesMap.put(byte[].class, Datatype.BYTE);
    s_classToTypesMap.put(char[].class, Datatype.CHAR);
    s_classToTypesMap.put(String.class, Datatype.CHAR);
    s_classToTypesMap.put(boolean.class, Datatype.BOOLEAN);
    s_classToTypesMap.put(Boolean.class, Datatype.BOOLEAN);
    s_classToTypesMap.put(Date.class, Datatype.TIMESTAMP);
    s_classToTypesMap.put(java.sql.Date.class, Datatype.TIMESTAMP);
    s_classToTypesMap.put(Timestamp.class, Datatype.TIMESTAMP);
  }


  /* only for serialization */
  public Datatype() {}

  private Datatype(Kind kind, String typeName, Class<?> underlyingType, int len, int precision,
        int sqlType, String attrvaltype, int nullable)
  {
	
    this.kind = kind;
    this.typeName = typeName;
    this.implementationType = underlyingType;
    this.length = len;
    this.precision = precision;
    this.sqlType = sqlType;
    this.attributeValueType = attrvaltype;
    this.nullable = (short) nullable;
    
    s_dataTypes[kind.ordinal()] = this;
  }
  
  private Datatype(Kind kind, String typeName, Class<?> underlyingType, int len, int precision,
	        RoundingMode roundingMode,int sqlType, String attrvaltype, int nullable)
  {
	this.kind = kind;
	this.typeName = typeName;
	this.implementationType = underlyingType;
	this.length = len;
	this.precision = precision;
	this.roundingMode = roundingMode;
	this.sqlType = sqlType;
	this.attributeValueType = attrvaltype;
	this.nullable = (short) nullable;
	    
	s_dataTypes[kind.ordinal()] = this;
  }

  public Datatype(String extensibleTypeName, Class<?> implClass)
  {
    this.kind = OBJECT.kind;
    this.typeName = extensibleTypeName;
    this.implementationType = implClass;
    this.length = OBJECT.length;
    this.precision = OBJECT.precision;
    this.sqlType = OBJECT.sqlType;
    this.attributeValueType = OBJECT.attributeValueType;
    this.nullable = OBJECT.nullable;
  }

  public Datatype(Datatype other, Class<?> underlyingType) {
    this.kind = other.kind;
    this.typeName = other.typeName;
    this.implementationType = underlyingType;
    this.length = other.length;
    this.precision = other.precision;
    this.sqlType = other.sqlType;
    this.attributeValueType = other.attributeValueType;
    this.nullable = other.nullable;
  }

  public static Datatype getDecimalType(int precision, int scale) {
    return new Datatype(Kind.BIGDECIMAL, "number", java.math.BigDecimal.class, scale, precision,
            Types.NUMERIC, "oracle.cep.dataStructures.external.BigDecimalAttributeValue", DatabaseMetaData.typeNoNulls);
  }
  
  public static Datatype getDecimalType(int precision, int scale, RoundingMode roundingMode) {
	  return new Datatype(Kind.BIGDECIMAL, "number", java.math.BigDecimal.class, scale, precision, roundingMode,
	            Types.NUMERIC, "oracle.cep.dataStructures.external.BigDecimalAttributeValue", DatabaseMetaData.typeNoNulls);
  }

  public static Datatype getByteType(int len) {
    return new Datatype(Kind.BYTE, "byte", byte[].class, len, 0,
            Types.VARBINARY, "oracle.cep.dataStructures.external.ByteAttributeValue", DatabaseMetaData.typeNullable);
  }

  public static Datatype getCharType(int len) {
    return new Datatype(Kind.CHAR, "char", String.class, len,   0,
            Types.VARCHAR, "oracle.cep.dataStructures.external.CharAttributeValue", DatabaseMetaData.typeNullable);
  }

  public static Datatype getXMLType(int len) {
    return new Datatype(Kind.XMLTYPE, "xmltype", String.class, len,  0,
            Types.VARCHAR, "oracle.cep.dataStructures.external.XmltypeAttributeValue",  DatabaseMetaData.typeNullableUnknown);
  }

  public static Datatype getTimestampType(DateFormat df, TimeZone tz) {
	  Datatype dataType= new Datatype(Kind.TIMESTAMP, "timestamp", long.class, Constants.TIMESTAMP_LENGTH,  0,
		        Types.TIMESTAMP, "oracle.cep.dataStructures.external.TimestampAttributeValue",  DatabaseMetaData.typeNullableUnknown);
	  if (df != null || tz != null) {
		  TimestampFormat tsformat = new TimestampFormat();
		  if (df != null) tsformat.setDateFormat(df);
		  if (tz != null) tsformat.setTimeZone(tz);
		  dataType.setTimestampFormat(tsformat);
	  }
	  return dataType;
  }
  
  public static Datatype getIntervalType(IntervalFormat intervalFormat) {
	  Datatype dataType = new Datatype(Kind.INTERVAL, "interval", long.class, Constants.INTERVAL_LENGTH,  0,
		        Types.VARCHAR, "oracle.cep.dataStructures.external.IntervalAttributeValue",  DatabaseMetaData.typeNullableUnknown);
	  if (intervalFormat != null) dataType.setIntervalFormat(intervalFormat);
	  return dataType;
  }

  public static Datatype getIntervalYMType(IntervalFormat intervalFormat) {
	  Datatype dataType = new Datatype(Kind.INTERVALYM, "intervalym", long.class, Constants.INTERVAL_LENGTH,  0,
		      Types.VARCHAR, "oracle.cep.dataStructures.external.IntervalYMAttributeValue",  DatabaseMetaData.typeNullableUnknown);
	  if (intervalFormat != null) dataType.setIntervalFormat(intervalFormat);
	  return dataType;
  }
  
  /**
   * Checks if the param type is native CQL numeric type
   * @param type
   * @return True if datatype is integer, float, bigint, double, bigdecimal or number
   */
  public static boolean isNumeric(Datatype type)
  {
    return type != null &&
        (type.isAssignableFrom(Datatype.BIGDECIMAL) ||
        type.isAssignableFrom(Datatype.BIGINT) || 
        type.isAssignableFrom(Datatype.DOUBLE) || 
        type.isAssignableFrom(Datatype.FLOAT) || 
        type.isAssignableFrom(Datatype.INT));
  }

  public String name()
  {
    return typeName;
  }
  
  public Kind getKind()
  {
    return kind;
  }
  
  public int ordinal() 
  {
    return getKind().ordinal();
  }
  
  public String toString()
  {
    if (kind != Kind.OBJECT)
      return typeName;
    StringBuilder b = new StringBuilder();
    b.append(typeName);
    return b.toString();
  }
  
  public Class<?> getImplementationType()
  {
    return implementationType;
  }
 
  public short getNullable()
  {
    return nullable;
  }
    
  public int getLength()
  {
    assert (length >= 0);
    return length;
  }
  
  public int getPrecision()
  {
    assert (precision >= 0);
    return precision;
  }
  
  public RoundingMode getRoundingMode() {
	  return roundingMode;	  
  }
  
  public String getAttrValClass() 
    throws UnsupportedOperationException
  {
    if (attributeValueType == null)    
      throw new UnsupportedOperationException("Unknown type");
    return attributeValueType;
  }

  public int getSqlType()
  {
    return sqlType;
  }
  
  public static char[] hexchars =
                                { '0', '1', '2', '3', '4', '5', '6', '7', '8',
      '9', 'a', 'b', 'c', 'd', 'e', 'f' };

  private static int hexVal(char c) throws CEPException
  {
    if ('0' <= c && c <= '9')
      return (c - '0');
    else if ('a' <= c && c <= 'f')
      return (c - 'a' + 0xa);
    else if ('A' <= c && c <= 'F')
      return (c - 'A' + 0xa);
    else
      throw new CEPException(ExecutionError.INVALID_ATTR, c, "[0-9A-Za-z]");
  }

  public static byte[] hexToByte(char[] cv, int len) throws CEPException
  {
    int blen, start;
  
    if (len % 2 == 0)
    {
      blen = len / 2;
      start = 0;
    } else
    {
      blen = (len + 1) / 2;
      start = 1;
  }
  
    byte[] bytes = new byte[blen];
    int left, right;

    if (start == 1)
      bytes[0] = (byte) hexVal(cv[0]);

    for (int ob = start; ob < blen; ob++)
  {
      left = hexVal(cv[2 * ob - start]);
      right = hexVal(cv[2 * ob + 1 - start]);
      bytes[ob] = (byte) ((left << 4) | right);
    }
    return bytes;
  }
  
  public static byte[] hexToByte(char[] cv) throws CEPException
  {
    return hexToByte(cv, cv.length);
  }

  public static char[] byteToHex(byte[] bv) throws CEPException
  {
    char[] hex = new char[bv.length * 2];
    for (int oo = 0; oo < bv.length; oo++)
  {
      hex[2 * oo] = hexchars[(bv[oo] & 0xf0) >>> 4];
      hex[2 * oo + 1] = hexchars[(bv[oo] & 0x0f)];
  }
    return hex;
  }

  public static char[] byteToHex(byte[] bv, int len) throws CEPException
  {
    char[] hex = new char[len * 2];
    for (int oo = 0; oo < len; oo++)
  {
      hex[2 * oo] = hexchars[(bv[oo] & 0xf0) >>> 4];
      hex[2 * oo + 1] = hexchars[(bv[oo] & 0x0f)];
    }
    return hex;
  }

    /**
   * List of types that can be used in stream/relation schemas. Add new types to
   * this list.
   * 
   * @return A list of public datatypes.
     */
  public static Datatype[] getPublicTypes()
  {
    Datatype[] pubTypes = {
      BIGINT, 
      BOOLEAN, 
      BYTE, 
      CHAR, 
      DOUBLE, 
      BIGDECIMAL, 
      FLOAT, 
      INT, 
      INTERVAL,     
      OBJECT,
      TIMESTAMP, 
      XMLTYPE,
      INTERVALYM };
    return pubTypes;
  }
    
  
  public boolean isCaseSensitive()
    {
    return (this == CHAR);
  }
      
  public static boolean strToBoolean(String value)
  {
    boolean bValue = false;
    if (value != null)
    {
      if (value.equalsIgnoreCase("true") )
        bValue = true;
      else if (value.equalsIgnoreCase("false") )
        bValue = false;
      else
    {
        int iValue = Integer.parseInt(value);
        bValue = iValue != 0;
      }
    }
    return bValue;
   }
      
  public static Datatype valueOf(int sqlType) {
    return s_sqlTypeMap.get(sqlType);
  }
      
  public static Datatype getTypeFromClass(Class<?> clz) {
    Datatype datatype = s_classToTypesMap.get(clz);
    if (datatype == null) {
      return new Datatype(clz.getSimpleName(), clz);
    }
    datatype = new Datatype(datatype, clz);
    Datatype datatypeInMap = s_dataTypesMap.get(datatype);
    //If there is already registered type, use it.
    if (datatypeInMap != null)
      return datatypeInMap;
    s_dataTypesMap.put(datatype, datatype);
    return datatype;
  }

  public static Datatype getTypeFromSqlName(String sqlTypeName) {
    Integer sqlType = s_sqlNameTypes.get(sqlTypeName);
    if(sqlType == null)
    {
      return null;
    } 
    else
      return valueOf(sqlType);
  }
  
  public static Datatype getTypeFromCqlName(String cqlTypeName) {
 
  	Datatype tmp = s_cqlNameTypes.get(cqlTypeName);
    return tmp;
  }
  
  public static int getSqlTypeEnumValue(String sqlTypeName) {
    Integer type = s_sqlNameTypes.get(sqlTypeName);
    if(type == null)
    {
      return -1; //TODO: is returning -1 correct?
    }
    else
      return type.intValue();
    }
    
  public static String getSqlTypeName(int stype)
    {
    String tname = s_sqlTypeNames.get(stype);
    if (tname == null)
    {
      tname = "Unknown:"+Integer.toString(stype);
    }
    return tname;
  }

    /* Due to object types, readResolve is not used anymore.
  public Object readResolve() throws ObjectStreamException
      {
    return s_dataTypes[kind.ordinal()];
      }
  */
  
  public static Datatype getDatatype(Kind kind)
  {
    return s_dataTypes[kind.ordinal()];
    }
  
  public boolean isAssignableFrom(IType fromDatatype) 
  {
    // We need to treat Kind.OBJECT as a Java type, otherwise nothing will be 
    //  assignable to java.lang.Object
    // FIXME we need to re-factor this, maybe OBJECT should be considered an extensible type as well...
    if ((this == OBJECT) && (fromDatatype instanceof Datatype)) 
      return this.getImplementationType().isAssignableFrom(
          ((Datatype) fromDatatype).getImplementationType());
    else 
      return this.equals(fromDatatype);
  }

  /**
   * @return the format
   */
  public IntervalFormat getIntervalFormat()
  {
    return intervalFormat;
  }
    
  /**
   * @param format the format to set
     */
  public void setIntervalFormat(IntervalFormat format)
  {
    assert this.getKind() == Datatype.INTERVAL.kind || this.getKind() == Datatype.INTERVALYM.kind;
    this.intervalFormat = format;
  }
    
  /** 
   * @return the format
   */
  public TimestampFormat getTimestampFormat()
  {
    return this.timestampFormat;
  }

  /**
   * @param format the format to set
   */
  public void setTimestampFormat(TimestampFormat format)
  {
    this.timestampFormat = format;
  }
    
  public void readExternal(ObjectInput in)
          throws IOException, ClassNotFoundException
  {
    int kord = in.readInt();
    Datatype dt = s_dataTypes[kord];
    kind = dt.kind;
    typeName = dt.typeName;
    sqlType = dt.sqlType;
    attributeValueType = dt.attributeValueType;
    implementationType = dt.implementationType;
    length = dt.length;
    precision = dt.precision;
    nullable = dt.nullable;
    roundingMode = dt.roundingMode;
    switch(kind) {
      case BYTE:
      case XMLTYPE:
      case CHAR:
        length = in.readInt();
        break;
      case TIMESTAMP:
        timestampFormat = (TimestampFormat) in.readObject();
        break;
      case INTERVAL:
      case INTERVALYM:
        intervalFormat = (IntervalFormat) in.readObject();
        break;
      case BIGDECIMAL:
        precision = in.readInt();
        length = in.readInt();
        roundingMode = (RoundingMode)in.readObject();
        break;
      case OBJECT:
      {
        typeName = (String) in.readObject();
        nullable = in.readShort();
        break;
      }
    }
    boolean isPrimitive = in.readBoolean();
    if (!isPrimitive) {
        String cls = (String) in.readObject();
        if (cls != null) 
        	implementationType = Class.forName(cls);
        else implementationType = null;
    }
  }

  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeInt(kind.ordinal());
    switch(kind) {
      case BYTE:
      case XMLTYPE:
      case CHAR:
        out.writeInt(length);
        break;
      case TIMESTAMP:
        out.writeObject(timestampFormat);
        break;
      case INTERVAL:
      case INTERVALYM:
        out.writeObject(intervalFormat);
        break;
      case BIGDECIMAL:
        out.writeInt(precision);
        out.writeInt(length);
        out.writeObject(roundingMode);
        break;
      case OBJECT:
        out.writeObject(typeName);
        out.writeShort(nullable);
        break;
    }
    boolean isPrimitive = s_primitiveTypes.contains(implementationType);
    out.writeBoolean(isPrimitive);
    if (!isPrimitive) {
        out.writeObject(implementationType == null ? null : implementationType.getName());
    }
  }

  public boolean equals(Object other) {
	  if (!(other instanceof Datatype)) return false;
	  Datatype that = (Datatype) other;
	  if (kind.ordinal() != that.kind.ordinal()) return false;
	  switch(kind) {
	      case BYTE:
	      case XMLTYPE:
	      case CHAR:
	        if (length != that.length) return false;
	        break;
	      case TIMESTAMP:
	    	if ((timestampFormat == null && timestampFormat != that.timestampFormat) || 
	    		(timestampFormat != null && !timestampFormat.equals(that.timestampFormat))) return false;
	        break;
	      case INTERVAL:
	      case INTERVALYM:
	    	if ((intervalFormat == null && intervalFormat != that.intervalFormat) || 
	    		(intervalFormat != null && !intervalFormat.equals(that.intervalFormat))) return false;
	        break;
	      case BIGDECIMAL:
	        if (precision != that.precision) return false;
	        if (length != that.length) return false;
	        if (roundingMode != that.roundingMode) return false;
	        break;
	      case OBJECT:
	    	if (!typeName.equals(that.typeName)) return false;
	        if (nullable != that.nullable) return false;
	        break;
        default:
	  }
      if ((implementationType == null && implementationType != that.implementationType) ||
            !implementationType.equals(that.implementationType)) return false;
	  return true;
  }

  public int hashCode() {
    int[] vals;
    int pos = 0;
    switch(kind) {
      case BYTE:
      case XMLTYPE:
      case CHAR:
        vals = new int[1 + 2];
        vals[0] = length;
        pos = 1;
        break;
      case TIMESTAMP:
        vals = new int[1 + 2];
        vals[0] = timestampFormat == null ? 0 : timestampFormat.hashCode();
        pos = 1;
        break;
      case INTERVAL:
      case INTERVALYM:
        vals = new int[1 + 2];
        vals[0] = intervalFormat == null ? 0 : intervalFormat.hashCode();
        pos = 1;
        break;
      case BIGDECIMAL:
        vals = new int[3 + 2];
        vals[0] = precision;
        vals[1] = length;
        vals[2] = roundingMode.hashCode();
        pos = 3;
        break;
      case OBJECT:
        vals = new int[2 + 2];
        vals[0] = typeName.hashCode();
        vals[1] = nullable;
        pos = 2;
        break;
      default:
        vals = new int[2];
        pos = 0;
        break;
    }
    vals[pos++] = kind.ordinal();
    vals[pos] = (implementationType == null) ? 0 : implementationType.hashCode();
    return Arrays.hashCode(vals);
  }
  
  public static Datatype getNumberTypeFromBigDecimal(Datatype src) {
	  if (src.kind != Kind.BIGDECIMAL) return src;
	  int scale = src.getLength();
	  int precision = src.getPrecision();
	  if (scale != 0) {
		  if (precision == FLOAT_PRECISION) return Datatype.FLOAT;
		  if (precision == DOUBLE_PRECISION) return Datatype.DOUBLE;
	  } else {
		  if (precision == INT_PRECISION) return Datatype.INT;
		  if (precision == BIGINT_PRECISION) return Datatype.BIGINT;
	  }
	  return src;
  }
}
