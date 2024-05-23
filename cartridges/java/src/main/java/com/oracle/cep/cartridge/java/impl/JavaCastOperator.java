/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/main/java/com/oracle/cep/cartridge/java/impl/JavaCastOperator.java /main/14 2016/03/20 15:20:17 sbishnoi Exp $ */

/* Copyright (c) 2009, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
      sbishnoi  09/29/13 - bug 17532543
      sbishnoi  09/26/13 - bug 17253337
      sbishnoi  09/25/13 - bug 17232810
      sbishnoi  09/23/13 - bug 17319310
      alealves  06/18/13 - Protect against NPEs.
    alealves    Feb 14, 2011 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/java/src/main/java/com/oracle/cep/cartridge/java/impl/JavaCastOperator.java /main/14 2016/03/20 15:20:17 sbishnoi Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package com.oracle.cep.cartridge.java.impl;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import oracle.cep.common.CalendarPool;
import oracle.cep.common.Datatype;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.RuntimeInvocationException;
import oracle.cep.extensibility.functions.IAttribute;
import oracle.cep.extensibility.functions.ISimpleFunction;
import oracle.cep.extensibility.functions.ISimpleFunctionMetadata;
import oracle.cep.extensibility.functions.UserDefinedFunction;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.Attribute;
import oracle.cep.metadata.MetadataException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.oracle.cep.cartridge.java.JavaCartridgeLogger;

class JavaCastOperator implements ISimpleFunctionMetadata, ISimpleFunction
{
  private static final Log logger = 
    LogFactory.getLog(JavaCartridge.JAVA_CARTRIDGE_LOGGER);
  
  private static Map<Class<?>,Class<?>> wrapperMap = new Hashtable<Class<?>, Class<?>>();
  static {
    wrapperMap.put( Boolean.TYPE, Boolean.class );
    wrapperMap.put( Byte.TYPE, Byte.class );
    wrapperMap.put( Short.TYPE, Short.class );
    wrapperMap.put( Character.TYPE, Character.class );
    wrapperMap.put( Integer.TYPE, Integer.class );
    wrapperMap.put( Long.TYPE, Long.class );
    wrapperMap.put( Float.TYPE, Float.class );
    wrapperMap.put( Double.TYPE, Double.class );
  }
  
  /** constant integer value equivalent to Numeric 1*/
  private static Integer JAVA_INT_CONST    = 1;
  
  /** constant float value equivalent to Numeric 1*/
  private static Float   JAVA_FLOAT_CONST  = 1.0f;
  
  /** constant double value equivalent to Numeric 1*/
  private static Double  JAVA_DOUBLE_CONST = 1.0d;
  
  /** constant long value equivalent to Numeric 1*/
  private static Long    JAVA_LONG_CONST   = 1L;
  
  /** constant short value equivalent to Numeric 1*/
  private static Short   JAVA_SHORT_CONST  = 1;
  
  /** constant byte value equivalent to Numeric 1*/
  private static Byte    JAVA_BYTE_CONST   = 1;
  
  private IAttribute[] attrs;
  private String name;
  private Datatype returnType;
  
  JavaCastOperator(String name, Datatype[] paramTypes)
  {
    this.name = name;
    this.attrs = new IAttribute [paramTypes.length];
    
    for (int i = 0; i < paramTypes.length; i++)
      attrs[i] = new Attribute("attr" + i, paramTypes[i], 0);
  }
  
  public JavaCastOperator(String name, Datatype fromType, Datatype toType)
  {
    this(name, new Datatype[] {fromType});
    
    returnType = toType;
  }
  
  public JavaCastOperator(String name, Datatype [] fromTypes, Datatype toType)
  {
    this(name, fromTypes);
    
    returnType = toType;
  }

  @Override
  public int getNumParams()
  {
    return attrs.length;
  }

  @Override
  public IAttribute getParam(int pos) throws MetadataException
  {
    return attrs[pos];
  }

  @Override
  public Datatype getReturnType()
  {
    return returnType;
  }

  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public String getSchema()
  {
    return null; // REVIEW
  }

  @Override
  public UserDefinedFunction getImplClass()
  {
    return this;
  }

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Object execute(Object[] args, ICartridgeContext context)
      throws RuntimeInvocationException
  {
    if (name.equals("to_cql"))
    {
      // Only the special types need treatment, the rest works automatically 
      // simply by the fact that the JVM will unbox appropriately. The real work
      // is only to establish the return type (metadata).
      if (LogUtil.isFinestEnabled(LoggerType.TRACE))
        LogUtil.finest(LoggerType.TRACE, "Invoking java cartridge cast operator to "
                +	"convert from " + (args[0] != null ? args[0].getClass().getName() : "null") +
                " to CQL native type " + returnType);
      
      if(args[0] == null)
      {
        // Bug 22538505 - CQL: UNEXPECTED EXPECTION THROWN FOR WITHOUT SET VALUE PROPERTY
        // Calling function AEval.invoke() will handle the returned null value 
        // and will set the attribute's isNull flag to true.
        return null;
        /*throw new RuntimeInvocationException(
          "java", 
          "to_cql", 
          JavaCartridgeLogger.illegalConvertLoggable((args[0] != null ? args[0].getClass().getName() : "null"),
              returnType.getKind().name()).getMessageText());*/                
      }
      
      if (returnType.equals(Datatype.TIMESTAMP))
      {
        // Type Map from various java types to CQL native TIMESTAMP data type 
        // java.sql.Date => java.sql.Timestamp(CQL TIMESTAMP)
        // java.util.Date => java.sql.Timestamp.
        
        //Note: Although java.sql.Date is subclass of java.util.Date but we need
        // special processing because java.sql.Date is equivalent to SQL DATE
        // type which doesn't save hour, minute and second values.
                
        if(args[0] instanceof java.sql.Date)
        {
          java.sql.Date dateObj = (java.sql.Date)args[0];
          CalendarPool calPool = CalendarPool.getCalendarPool();
          Calendar cal = calPool.getCalendar();
          cal.setTimeInMillis(dateObj.getTime());
          // Set HOUR, MINUTE, SECOND and MILLISECOND field to ZERO
          cal.set(Calendar.HOUR, 0);
          cal.set(Calendar.MINUTE, 0);
          cal.set(Calendar.SECOND, 0);
          cal.set(Calendar.MILLISECOND, 0);          
          return new Timestamp(cal.getTimeInMillis());
        }
        else if(args[0] instanceof Time)
        {
          // Note: java.sql.Time is expected to store only time portion of
          // date or timestamp value. Hence before converting from java.sql.Time
          // to native CQL TIMESTAMP, we will extract the time portion from 
          // the object using Calendar APIs.
          // After obtaining the time portion, we will parse this using a 
          // formatter with format "hh:mm:ss"
          // The formatter will return a java.sql.Timestamp which is compatible
          // with native CQL TIMESTAMP.
          Time time = (Time)args[0];
          CalendarPool calPool = CalendarPool.getCalendarPool();
          Calendar cal = calPool.getCalendar();
          cal.setTime(time);
          int numHours = cal.get(Calendar.HOUR);
          int numMinutes = cal.get(Calendar.MINUTE);
          int numSeconds = cal.get(Calendar.SECOND);
          StringBuilder sb = new StringBuilder();
          sb.append(numHours);
          sb.append(":");
          sb.append(numMinutes);
          sb.append(":");
          sb.append(numSeconds);
          SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
          try 
          {
            Date date = sdf.parse(sb.toString());
            return new Timestamp(date.getTime());
          } 
          catch (ParseException e) 
          {
            LogUtil.info(LoggerType.TRACE, e.getMessage());
            throw new RuntimeInvocationException(
              "java", 
              "to_cql", 
              JavaCartridgeLogger.illegalTimeDateFormatLoggable(sb.toString()).getMessageText());
          }         
        }
        else if(args[0] instanceof Date)
        {
          return new Timestamp(((Date) args[0]).getTime());
        }
        else if(args[0].getClass().getName().equalsIgnoreCase(
                "oracle.sql.TIMESTAMP"))
        {
          // To convert a value of type oracle.sql.TIMESTAMP into a native CQL 
          // TIMESTAMP, we have to convert the value into a jdbc format;
          // toJdbc(oracle.sql.TIMESTAMP) will return java.sql.Timestamp            
          try 
          {
            Class tsClass = args[0].getClass();
            Method method = tsClass.getMethod("toJdbc", null);
            Object obj = method.invoke(args[0], null);
            return obj;
          } 
          catch (Exception e) 
          {
            LogUtil.info(LoggerType.TRACE, e.getMessage());
            throw new RuntimeInvocationException(
              "java", 
              "to_cql", 
              JavaCartridgeLogger.illegalTimestampConvertLoggable(
                      returnType.getKind().name()).getMessageText());
          } 
        }
      }
      
      boolean isConversionAllowed = true;
      switch(returnType.getKind())
      {
      case INT:
        if(args[0] instanceof Number)
          return ((Number)args[0]).intValue();
        else
          isConversionAllowed = false;
        break;
      case BIGINT:
        if(args[0] instanceof Number)
          return ((Number)args[0]).longValue();
        else
          isConversionAllowed = false;
        break;
      case FLOAT:
        if(args[0] instanceof Number)
          return ((Number)args[0]).floatValue();
        else
          isConversionAllowed = false;
        break;
      case DOUBLE:
        if(args[0] instanceof Number)
          return ((Number)args[0]).doubleValue();
        else
          isConversionAllowed = false;
        break;
      case BIGDECIMAL:
        if(args[0] instanceof java.math.BigDecimal)
          return args[0];
        else if(args[0] instanceof Number)
        {
          if(args[0] instanceof Integer)
            return new BigDecimal((Integer)args[0]);
          else  if(args[0] instanceof Float)
            return new BigDecimal((Float)args[0]);
          else if(args[0] instanceof Double)
            return new BigDecimal((Double)args[0]);
          else if(args[0] instanceof Long)
            return new BigDecimal((Long)args[0]);
          else if(args[0] instanceof Short)
            return new BigDecimal((Short)args[0]);
          else if(args[0] instanceof Byte)
            return new BigDecimal((Byte)args[0]);
          else
            isConversionAllowed = false;
        }
        break;
      case BOOLEAN:
        if(args[0] instanceof Boolean)
          return args[0];
        else if(args[0] instanceof String)
          return Boolean.parseBoolean((String)args[0]);
        else if(args[0] instanceof Number)
        {
          // If the fromType is Numeric Value, 
          // then return true if the value is equal to Numeric 1.
          // else return false.
          int comparisonScore = -1;
          if(args[0] instanceof Integer)
            comparisonScore = ((Integer)args[0]).compareTo(JAVA_INT_CONST);
          else  if(args[0] instanceof Float)
            comparisonScore = ((Float)args[0]).compareTo(JAVA_FLOAT_CONST);
          else if(args[0] instanceof Double)
            comparisonScore = ((Double)args[0]).compareTo(JAVA_DOUBLE_CONST);
          else if(args[0] instanceof Long)
            comparisonScore = ((Long)args[0]).compareTo(JAVA_LONG_CONST);
          else if(args[0] instanceof Short)
            comparisonScore = ((Short)args[0]).compareTo(JAVA_SHORT_CONST);
          else if(args[0] instanceof Byte)
            comparisonScore = ((Byte)args[0]).compareTo(JAVA_BYTE_CONST);
          
          return new Boolean(comparisonScore==0);
        }
        else
            isConversionAllowed = false;
      }
      if(!isConversionAllowed)
      {
        throw new RuntimeInvocationException(
          "java", 
          "to_cql", 
          JavaCartridgeLogger.illegalConvertLoggable((args[0] != null ? args[0].getClass().getName() : "null"),
              returnType.getKind().name()).getMessageText());
      }
     
      // Type conversion from char array to CQL native type CHAR
      // Setter of CQL native type CHAR expects java.lang.String values.
      if(returnType.equals(Datatype.CHAR))
      {
        if(args[0] instanceof char[])
        {
          return new String((char[])args[0]);
        }
      }
      // CQL BigDecimal is implemented as java.math.BigDecimal, so in theory nothing is needed to be done.
      
      // FIXME Still need to treat Interval
      
      return args[0];
    }
    else if (name.equals("cast"))
    {
      // First try a reference conversion
      try
      {
        return applyConversion(args[0]);
      }
      catch (Exception e)
      {
        String message = 
          JavaCartridgeLogger.illegalTypeCastLoggable((args[0] != null ? args[0].getClass().getName() : "null"),
              returnType.getImplementationType().getName()).getMessageText();

        if (logger.isDebugEnabled())
          logger.debug(message, e);

        throw new RuntimeInvocationException(JavaCartridge.JAVA_CARTRIDGE_NAME, name, message);
      }
    }
    
    // Should never reach here, instead it would have failed during type check.
    throw new IllegalStateException("Unknown function = " + name);
  }
  
  private Object applyConversion(Object arg)
  {
    Class<?> fromType = attrs[0].getType().getImplementationType();
    Class<?> toType = returnType.getImplementationType();
    
    if (wrapperMap.get(fromType) != null)
      fromType = wrapperMap.get(fromType);
    
    if (wrapperMap.get(toType) != null)
      toType = wrapperMap.get(toType);
    
    // As per section $5.1 in the JLS 1.7
    
    //1. Check for the identity conversion
    if (fromType.equals(toType))
      return arg; // nothing to do in the identity conversion, but just allowed it.
    
    //2. Check for the String conversion
    if (toType.equals(String.class))
    {
      if (arg == null)
        return "null"; // per $5.1.11
      else
        return arg.toString();
    }
    
    //3. If numeric, then apply widening or narrowing conversions
    if (Number.class.equals(fromType.getSuperclass()) && 
        Number.class.equals(toType.getSuperclass()))
    {
      // All together, you can widen or narrow between any of the number types.
      if (toType.equals(Byte.class))
      {
        return ((Number) arg).byteValue();
      } else if (toType.equals(Short.class))
      {
        return ((Number) arg).shortValue();
      } else if (toType.equals(Integer.class))
      {
        return ((Number) arg).intValue();
      } else if (toType.equals(Long.class))
      {
        return ((Number) arg).longValue();
      } else if (toType.equals(Float.class))
      {
        return ((Number) arg).floatValue();
      } else if (toType.equals(Double.class))
      {
        return ((Number) arg).doubleValue();
      }  
    }
    
    // Char is a bit off an odd-ball
    if (fromType.equals(Character.class) && 
        Number.class.equals(toType.getSuperclass()))
    {
      if (toType.equals(Byte.class))
      {
        return (byte) ((Character) arg).charValue();
      } else if (toType.equals(Short.class))
      {
        return (short) ((Character) arg).charValue();
      } else if (toType.equals(Integer.class))
      {
        return (int) ((Character) arg).charValue();
      } else if (toType.equals(Long.class))
      {
        return (long) ((Character) arg).charValue();
      } else if (toType.equals(Float.class))
      {
        return (float) ((Character) arg).charValue();
      } else if (toType.equals(Double.class))
      {
        return (double) ((Character) arg).charValue();
      }  
    }
    
    if (Number.class.equals(fromType.getSuperclass()) && 
        toType.equals(Character.class))
    {
      if (fromType.equals(Byte.class))
      {
        return (char) ((Number) arg).byteValue();
      } else if (fromType.equals(Short.class))
      {
        return (char) ((Number) arg).shortValue();
      } else if (fromType.equals(Integer.class))
      {
        return (char) ((Number) arg).intValue();
      } else if (fromType.equals(Long.class))
      {
        return (char) ((Number) arg).longValue();
      } else if (fromType.equals(Float.class))
      {
        return (char) ((Number) arg).floatValue();
      } else if (fromType.equals(Double.class))
      {
        return (char) ((Number) arg).doubleValue();
      }  
    }
    
    //4. Finally, do reference conversion
    return toType.cast(arg);
  }
}
