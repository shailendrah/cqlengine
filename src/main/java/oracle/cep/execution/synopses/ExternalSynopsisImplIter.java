/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/synopses/ExternalSynopsisImplIter.java /main/16 2013/10/08 11:09:54 sbishnoi Exp $ */

/* Copyright (c) 2007, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    10/09/12 - XbranchMerge
                           sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0_11.1.1.7.0
                           from st_pcbpel_11.1.1.4.0
    sbishnoi    10/08/12 - XbranchMerge sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0
                           from st_pcbpel_pt-11.1.1.7.0
    sbishnoi    09/28/12 - passing cause in the error message
    sbishnoi    01/13/12 - improved timestamp support to include timezone
    sbishnoi    10/03/11 - changing format to intervalformat
    sbishnoi    08/29/11 - adding support for interval year to month
    anasrini    12/19/10 - replace eval() with eval(ec)
    sborah      07/18/10 - XbranchMerge sborah_bug-9536720_ps3_11.1.1.4.0 from
                           st_pcbpel_11.1.1.4.0
    sborah      07/17/10 - XbranchMerge sborah_bug-9536720_ps3 from main
    sbishnoi    06/22/10 - adding limit on maximum number of allowed external
                           relation rows
    sbishnoi    03/02/10 - support for scan predicate
    hopark      12/01/09 - workaround for timestamp with tz
    sborah      10/22/09 - support for bigdecimal
    hopark      02/17/09 - support boolean as external datatype
    sbishnoi    12/03/08 - support for generic data source
    hopark      02/28/08 - resurrect refcnt
    udeshmuk    01/31/08 - support for double data type.
    hopark      12/14/07 - spill cleanup
    parujain    12/07/07 - external synopsis iter
    parujain    11/20/07 - Result set iterator
    parujain    11/20/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/synopses/ExternalSynopsisImplIter.java /main/16 2013/10/08 11:09:54 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.synopses;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Iterator;
import java.util.logging.Level;

import oracle.cep.common.CEPDate;
import oracle.cep.common.CEPDateFormat;
import oracle.cep.common.IntervalConverter;
import oracle.cep.common.IntervalFormat;
import oracle.cep.common.TimestampFormat;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.memory.EvalContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;

public class ExternalSynopsisImplIter implements TupleIterator {

  /** Factory to allocate TuplePtrs*/
  IAllocator<ITuplePtr> factory ;

  /** ResultSet Object for which we will create an iterator*/
  ResultSet             resultSet;

  /** Java Iterator Object which has output of IExternalPreparedStatement. 
   *  We will create TupleIterator for this object 
   */
  Iterator<TupleValue>  resultIter;
  
  /** Result Tuple specification*/
  TupleSpec             tspec;
  
  /** Is the Result object an Java Iterator Object*/
  boolean               isResultObjectIter;
  
  /** Predicate which needs to be evaluated for each tuple of synopsis */
  private IBEval        predEval;
  
  /** evalContext*/
  private IEvalContext  evalCtx;
  
  /** flag to check whether the given query is having run away predicate */
  private boolean       isRunAwayPredicate;
  
  /** maximum number of external relation rows that can be fetched */
  private long          externalRowsThreshold;
  
  /** current count of fetched matching external relation rows */
  private long          numExtractedExternalRows;
  
  /** name of external data source */
  private String        extSourceName;

  /**
   * Constructor to obtain TupleIterator for java ResultSet Object
   * @param factory Tuple Allocation factory
   * @param rs ResultSet Object
   * @param spec Tuple Specification
   * @param predEval predicate to check for each tuple
   * @throws ExecException
   */
  public ExternalSynopsisImplIter(IAllocator<ITuplePtr> factory, 
      ResultSet rs, 
      TupleSpec spec,
      IBEval predEval,
      IEvalContext evalCtx,
      boolean isRunAwayPredicate,
      long externalRowsThreshold,
      String extSourceName)
    throws ExecException
  {
    try
    {
      this.factory   = factory;
      this.resultSet = rs;
      this.tspec     = spec;
      ResultSetMetaData rsMetadata = rs.getMetaData();      
      assert rsMetadata.getColumnCount() == tspec.getNumAttrs() ;
      /** Result Object is Java ResultSet */
      isResultObjectIter = false;
      this.predEval      = predEval;
      this.evalCtx       = evalCtx;
      this.isRunAwayPredicate = isRunAwayPredicate;
      this.externalRowsThreshold = externalRowsThreshold;
      this.numExtractedExternalRows = 0L;
      this.extSourceName = extSourceName;
    }
    catch(Exception e)
    {
   
    }
  }
  
  /**
   * Constructor to obtain TupleIterator for java Iterator Object
   * @param factory Tuple Allocation Factory
   * @param resultIter ResultSet Object
   * @param spec Tuple Specification
   * @param predEval predicate to check for each tuple
   * @throws ExecException
   */
  public ExternalSynopsisImplIter(IAllocator<ITuplePtr> factory, 
                                  Iterator<TupleValue> resultIter,
                                  TupleSpec spec,
                                  IBEval predEval,
                                  IEvalContext evalCtx,
                                  boolean isRunAwayPredicate,
                                  long externalRowsThreshold,
                                  String extSourceName)  throws ExecException
  {
    this.factory    = factory;
    this.resultIter = resultIter;
    tspec           = spec;
    //Assumption: TupleValue has the same number of columns and column types
    //            as TupleSpec does have.
    /** Result Object is Java Iterator Object*/
    isResultObjectIter = true;
    this.predEval      = predEval;
    this.evalCtx       = evalCtx;
    this.isRunAwayPredicate = isRunAwayPredicate;
    this.externalRowsThreshold = externalRowsThreshold;
    this.numExtractedExternalRows = 0L;
    this.extSourceName = extSourceName;
  }
  
  
  public ITuplePtr getNext() throws ExecException {
    // Return null if system reaches on the max limit of allowed rows to be
    // fetched from external relation
    if(isRunAwayPredicate)
    {
      if(numExtractedExternalRows >= externalRowsThreshold)
      {
        LogUtil.fine(LoggerType.TRACE, 
          "external rows threshold [" + externalRowsThreshold
          +"] reached while joining with external relation" + getExtSourceName());
        return null;
      }
    }
    
    ITuplePtr nextTuple = null;
    
    // Get the next tuple in the iterator
    try
    {
      nextTuple 
       = isResultObjectIter ? getNextIterTuple() : getNextRSTuple();
    }
    catch(ExecException ee)
    {
      throw ee;
    }
    catch(CEPException ce)
    {
      LogUtil.fine(LoggerType.TRACE, "An internal execution exception occured." +
      		" Please check the log for detailed stack trace.");
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, ce);
      throw new ExecException(ExecutionError.ERROR_RUNNING_EXTERNAL_QUERY, 
                              getExtSourceName());
    }
      
    // Increment the counter for extracted external relation rows
    if(isRunAwayPredicate && nextTuple != null)
      numExtractedExternalRows++;
      
    // If predicate is null; return the tuple
    // Else calculate the predicate on the "nextTuple"
    if(predEval == null)
    {
      return nextTuple;
    }
    else if(nextTuple != null)
    {
      evalCtx.bind(nextTuple, EvalContext.SCAN_ROLE);
      if(predEval.eval(evalCtx))
        return nextTuple;
      else
      {          
        while(true)
        {
          try
          {
            nextTuple 
              = isResultObjectIter ? getNextIterTuple() : getNextRSTuple();
          }
          catch(ExecException ee)
          {
            throw ee;
          }
          catch(CEPException ce)
          {
            LogUtil.fine(LoggerType.TRACE, "An internal execution exception occured." +
                " Please check the log for detailed stack trace.");
            LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, ce);
            throw new ExecException(ExecutionError.ERROR_RUNNING_EXTERNAL_QUERY,
                                    getExtSourceName());
          }
            
          if(nextTuple != null)
            evalCtx.bind(nextTuple, EvalContext.SCAN_ROLE);
          
          // Exit the loop if either there is no tuple in the synopsis
          // or no tuple satisfies the predicate
          if(nextTuple == null || (nextTuple != null &&
                                   predEval.eval(evalCtx)))
            break;
        }
        return nextTuple;
      }
    }
    else
      return null;
   
  }

  /**
   * Get Next tuple from TupleValue Iterator and construct a TuplePtr object
   * Set TuplePtr's TupleValue to ResultSet' Tuple Value
   * @return TuplePtr Object
   * @throws ExecException
   */
  public ITuplePtr getNextIterTuple() throws CEPException 
  {
    if(resultIter.hasNext())
    {
      ITuplePtr tPtr = (ITuplePtr)factory.allocate(); 
      if(tPtr != null)
      {
        ITuple t = tPtr.pinTuple(IPinnable.WRITE);
        t.init(tspec, true);
        TupleValue currentTuple = resultIter.next();
        
        for(int i=0; i < tspec.getNumAttrs(); i++)
        {
          if(currentTuple.getAttribute(i).isBNull())
            t.setAttrNull(i);
          else
          {
            IntervalFormat destinationFormat = null;
            switch(tspec.getAttrType(i).getKind())
            {
              case INT:       
                int j = currentTuple.iValueGet(i);
                t.iValueSet(i, j);
                break;
                
              case BOOLEAN:  
                boolean b = currentTuple.boolValueGet(i);
                t.boolValueSet(i, b);
                break;
                
              case BIGINT:
                long l = currentTuple.lValueGet(i);
                t.lValueSet(i, l);
                break;
                
              case CHAR:
                char[] s = currentTuple.cValueGet(i);
                int  len = currentTuple.cLengthGet(i);
                t.cValueSet(i, s, len);
                break;
                
              case FLOAT:
                float f = currentTuple.fValueGet(i);
                t.fValueSet(i, f);
                break;
                
              case DOUBLE:
                double d = currentTuple.dValueGet(i);
                t.dValueSet(i, d);
                break;
                
              case BIGDECIMAL:
                t.nValueSet(i, currentTuple.nValueGet(i), 
                            currentTuple.nPrecisionGet(i),
                            currentTuple.nScaleGet(i));
                break;
                
              case BYTE:
                byte[] by = currentTuple.bValueGet(i);
                t.bValueSet(i, by, by.length);
                break;
                
              case TIMESTAMP:
                long ts = currentTuple.tValueGet(i);
                t.tValueSet(i, ts);
                t.tFormatSet(i, currentTuple.tFormatGet(i));
                break;
                
              case INTERVAL:
                long interval = currentTuple.intervalValGet(i);
                destinationFormat = currentTuple.vFormatGet(i);
                t.vValueSet(i, interval, destinationFormat);
                break;
                
              case INTERVALYM:
                long intervalym = currentTuple.intervalYMValGet(i);
                destinationFormat = currentTuple.vFormatGet(i);
                t.vymValueSet(i, intervalym, destinationFormat);
                break;
                
              case OBJECT:
                Object object = currentTuple.oValueGet(i);
                t.oValueSet(i, object);
                break;                
                
              default:
            }
          }
        }
        factory.addRef(tPtr);
        return tPtr;
      }
    }
    
    return null;    
  }
  
  /**
   * Get next tuple from ResultSet object and construct a TuplePtr object
   * Set TuplePtr's Tuple Value to ResultSet's Tuple Value 
   * @return TuplePtr Object
   * @throws ExecException
   */
  public ITuplePtr getNextRSTuple() throws CEPException
  {
    boolean hasNext = false;
    while(true)
    {      
      try
      {
        hasNext = resultSet.next();
        if(!hasNext) 
          break;
        
        ITuplePtr tPtr = (ITuplePtr)factory.allocate(); 
        if(tPtr != null)
        {
          ITuple t = tPtr.pinTuple(IPinnable.WRITE);
          t.init(tspec, true);
          
          IntervalFormat destinationFormat = null;
          
          for(int i=0; i<tspec.getNumAttrs(); i++)
          {
              switch(tspec.getAttrType(i).getKind())
              {
                case INT       : 
                  int j = resultSet.getInt(i+1);
                  
                  if(resultSet.wasNull())
                    t.setAttrNull(i);
                  else
                    t.iValueSet(i, j);
                  break;
                  
                case BOOLEAN    : 
                  boolean bv = resultSet.getBoolean(i+1);
                  
                  if(resultSet.wasNull())
                    t.setAttrNull(i);
                  else
                    t.boolValueSet(i, bv);
                  break;
                  
                case BIGINT    : 
                  Long l = resultSet.getLong(i+1);
                  
                  if(resultSet.wasNull())
                    t.setAttrNull(i);
                  else
                    t.lValueSet(i, l);
                  break;
                  
                case CHAR      : 
                  String s = resultSet.getString(i+1);
                  
                  if(resultSet.wasNull())
                    t.setAttrNull(i);
                  else
                    t.cValueSet(i, s.toCharArray(), s.length());
                  break;
                case FLOAT     :
                  float f = resultSet.getFloat(i+1);
                  
                  if(resultSet.wasNull())
                    t.setAttrNull(i);
                  else
                    t.fValueSet(i, f);
                  break;
                  
                case DOUBLE    : 
                  double d = resultSet.getDouble(i+1);
                  
                  if(resultSet.wasNull())
                    t.setAttrNull(i);
                  else
                    t.dValueSet(i, d);
                  break;
                  
                case BIGDECIMAL: 
                  BigDecimal bd = resultSet.getBigDecimal(i+1);
                  
                  if(resultSet.wasNull())
                    t.setAttrNull(i);
                  else
                    t.nValueSet(i, bd, bd.precision(), bd.scale());
                  break;
                  
                case BYTE      : 
                  byte[] b = resultSet.getBytes(i+1);
                  
                  if(resultSet.wasNull())
                    t.setAttrNull(i);
                  else
                    t.bValueSet(i, b, b.length);
                  break;
                  
                case TIMESTAMP : 
                  String ti = resultSet.getString(i+1);
                  Timestamp tsVal= null; 
                  if(resultSet.wasNull())
                    t.setAttrNull(i);
                  else
                  {
                    // JDBC API ResultSet.getTimestamp() will return java.sql.Timestamp
                    // which only keeps the millisecond value and loose the timezone
                    // information.
                    // To extract timezone from external table record, we will
                    // fetch the attribute value as a string and parse the string
                    // using formats registered in CQL Engine.
                    // If date can be parsed using engine, then it will extract
                    // timezone information from the value.
                    try 
                    {
                      tsVal = CEPDateFormat.getInstance().parse(ti);
                    } 
                    catch (ParseException e) 
                    {
                      tsVal = resultSet.getTimestamp(i+1);
                    }
                    
                    t.tValueSet(i, tsVal);     
                    
                    if(tsVal instanceof CEPDate)
                    {
                      TimestampFormat tsFormat = ((CEPDate)tsVal).getFormat();
                      tsFormat.setDateFormat(CEPDateFormat.getInstance().getDefaultFormat());
                      t.tFormatSet(i, tsFormat);
                    }
                    else
                      t.tFormatSet(i,TimestampFormat.getDefault());
                  }
                  //Issue with timestamp with TZ
                  //It looks like we need to use pstmt.setString for setting timestamp.
              	  //However, resultSet.getTimestamp() looks fine.
                  /*                	
                	String sval = resultSet.getString(i+1);
                    if(resultSet.wasNull())
                        t.setAttrNull(i);
                    else
                    {
                        TimeZone tz = CEPDateFormat.getInstance().getDefaultTimeZone();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-M-d HH.mm.ss.S z");
                        df.setLenient(false);
                        df.setTimeZone(tz);
                    	Date date = df.parse(sval);
                    	t.tValueSet(i, date.getTime());
                    }
                    */
                	
                  break;
                  
                case INTERVAL  : 
                  String interval = resultSet.getString(i+1);
                  destinationFormat = tspec.getAttrMetadata(i).getIntervalFormat();
                  if(resultSet.wasNull())
                    t.setAttrNull(i);
                  else
                  {
                    t.vValueSet(
                      i, 
                      IntervalConverter.parseDToSIntervalString(
                        interval, destinationFormat),
                      destinationFormat);
                  }
                    
                  break;
                  
                case INTERVALYM  : 
                  String intervalVal = resultSet.getString(i+1);
                  destinationFormat = tspec.getAttrMetadata(i).getIntervalFormat();
                  if(resultSet.wasNull())
                    t.setAttrNull(i);
                  else
                  {
                    t.vymValueSet(
                      i, 
                      IntervalConverter.parseYToMIntervalString(
                        intervalVal, destinationFormat),
                      destinationFormat);
                  }
                    
                  break;
                  
                case OBJECT    :
                  /*
                  bug#26932327, bug#26865837
                  in case of geometry object retrieved from database (i.e., sdo_geometry), we can not store the object,
                  since the object is of type STRUCT of oracle driver (equivalent to oracle.sql.STRUCT), which is not serializable.
                  this cause Serialization Error while processing in distributed env. such as spark runtime. Therefore, we retrieve
                  and store corrensponding Geometry object(com.oracle.cep.cartridge.spatial.Geometry) as an  attribute value.
                   */
                  if(resultSet.getMetaData().getColumnTypeName(i+1).equals("MDSYS.SDO_GEOMETRY")){
                    Object struct = resultSet.getObject(i+1);
                    try {
                      Class<?> cl = Class.forName("com.oracle.cep.cartridge.spatial.Geometry");
                      Class<?>[] argTypes = new Class[]{java.sql.Struct.class};
                      Method m = cl.getDeclaredMethod("toGeometry",argTypes);
                      Object geom = m.invoke(null,struct);
                      if(resultSet.wasNull())
                        t.setAttrNull(i);
                      else
                        t.oValueSet(i,geom);
                    } catch (ClassNotFoundException e) {
                      LogUtil.fine(LoggerType.TRACE, "could not load Geometry class. please ensure spatial cartridge in the classpath." +
                              ". Check the log for detailed stack trace.");
                      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
                    } catch (NoSuchMethodException e) {
                      LogUtil.fine(LoggerType.TRACE, "could not find toGeometry(Struct) method of Geometry." +
                              " please ensure method is defined in Geometry class and spatial cartridge is in the classpath." +
                              ". Check the log for detailed stack trace.");
                      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
                    } catch (IllegalAccessException e) {
                      LogUtil.fine(LoggerType.TRACE, "could not invoke toGeometry(Struct) method of Geometry." +
                              " please ensure method is defined in Geometry class and spatial cartridge is in the classpath." +
                              ". Check the log for detailed stack trace.");
                      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
                    } catch (InvocationTargetException e) {
                      LogUtil.fine(LoggerType.TRACE, "could not invoke toGeometry(Struct) method of Geometry." +
                              " please ensure method is defined in Geometry class and spatial cartridge is in the classpath." +
                              ". Check the log for detailed stack trace.");
                      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
                    }
                  }else{
                    Object object = resultSet.getObject(i+1);
                    if(resultSet.wasNull())
                      t.setAttrNull(i);
                    else
                      t.oValueSet(i, object);
                  }
                  break;
                  
                default:
              }
          }
          factory.addRef(tPtr);  
          // break from loop and return the single tuple pointer objet
          return tPtr;
        }
      }
      catch(SQLException e)
      {
        LogUtil.fine(LoggerType.TRACE, "SQL Exception in iterating over external" +
      		" relation data. Check the log for detailed stack trace.");
        LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      }
    }
    return null;
   }
  
  public void setRunAwayPredicate(boolean isRunAwayPredicate)
  {
    this.isRunAwayPredicate = isRunAwayPredicate;
  }

  public void setExternalRowsThreshold(long externalRowsThreshold)
  {
    this.externalRowsThreshold = externalRowsThreshold;
  }

  /**
   * @return the extSourceName
   */
  public String getExtSourceName()
  {
    return extSourceName;
  }

  /**
   * @param extSourceName the extSourceName to set
   */
  public void setExtSourceName(String extSourceName)
  {
    this.extSourceName = extSourceName;
  }


}
