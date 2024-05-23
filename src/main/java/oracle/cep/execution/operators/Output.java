/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/Output.java /main/64 2012/10/09 05:16:40 sbishnoi Exp $ */
/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares Output in package oracle.cep.execution.operators.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 sbishnoi  10/09/12 - XbranchMerge
                      sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0_11.1.1.7.0 from
 sbishnoi  10/08/12 - XbranchMerge sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0 from
                      st_pcbpel_pt-11.1.1.7.0
 sbishnoi  10/08/12 - modifying conditions to set the stats
 alealves  12/20/11 - XbranchMerge alealves_bug-12873645_cep from main
 udeshmuk  09/24/10 - XbranchMerge udeshmuk_prop_hb_across_processors from
                      st_pcbpel_11.1.1.4.0
 udeshmuk  07/13/12 - instead of outputtuple use outtuple while throwing
                      unique constraint violation exception
 sbishnoi  02/12/12 - support for output batching with update semantics
 sbishnoi  10/03/11 - changing format to intervalformat
 sbishnoi  08/28/11 - adding support for interval year to month
 sbishnoi  08/02/11 - fix bug 12799851
 udeshmuk  09/24/10 - XbranchMerge udeshmuk_prop_hb_across_processors from
                      st_pcbpel_11.1.1.4.0
 udeshmuk  09/01/10 - add propagateHeartbeat
 sbishnoi  02/02/10 - fix NPE bug 9273983
 sbishnoi  01/29/10 - categorizing downstream component exceptions
 sbishnoi  01/21/10 - handling downstream components
 sbishnoi  12/06/09 - output tuple batching
 sborah    07/16/09 - support for bigdecimal
 sborah    06/30/09 - support for bigdecimal
 sbishnoi  06/22/09 - support to club output for a timestamp value
 hopark    05/12/09 - fix varchar memory issue
 sbishnoi  04/20/09 - logg the instance when inpTs crossover currentBaseTime
 udeshmuk  04/13/09 - add getDebugInfo to assertion
 sbishnoi  04/08/09 - setting totalordering flag
 hopark    02/05/09 - objtype support
 hopark    12/04/08 - change exception on no plus tuple in outSyn
 sborah    11/24/08 - alter base timeline for latency calc
 hopark    10/28/08 - fix heartbeat handling in stats
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 sbishnoi  08/05/08 - support for nanosecond
 sbishnoi  06/27/08 - update lastOutputTs
 mthatte   04/22/08 - removed isBnull from TupleValue cnstructor
 sbishnoi  03/19/08 - adding primary key information to QueryOutputBase(dest)
 hopark    02/28/08 - resurrect refcnt
 hopark    02/05/08 - parameterized error
 udeshmuk  01/31/08 - support for double data type.
 hopark    12/27/07 - support xmllog
 udeshmuk  01/17/08 - change in data type of timestamp in TupleValue.
 hopark    12/06/07 - cleanup spill
 sbishnoi  12/14/07 - bug fix ( bug id# 6691396)
 najain    10/24/07 - support for xmltype
 mthatte   12/07/07 - operator logging
 sbishnoi  11/26/07 - support for update semantics
 hopark    10/30/07 - remove IQueueElement
 hopark    10/21/07 - remove TimeStamp
 parujain  10/18/07 - cep-bam integration
 parujain  10/04/07 - delete op
 rkomurav  09/04/07 - bug 6356904
 najain    07/31/07 - debug Linear Road
 hopark    07/13/07 - dump stack trace on exception
 parujain  06/26/07 - mutable state
 hopark    06/19/07 - cleanup
 hopark    05/22/07 - logging support
 hopark    05/16/07 - use constant for debug level
 parujain  05/08/07 - monitoring stats
 najain    04/09/07 - bug fix
 hopark    03/21/07 - add TuplePtr pin
 parujain  03/20/07 - debuglevel
 najain    03/14/07 - cleanup
 parujain  03/16/07 - debug level
 najain    03/12/07 - bug fix
 najain    02/19/07 - bug fix
 najain    01/05/07 - spill over support
 hopark    12/26/06 - add refcounting facility
 rkomurav  11/29/06 - remove the redundant add_attr
 hopark    11/16/06 - add bigint datatype
 dlenkov   10/20/06 - byte data type fixes
 parujain  10/06/06 - interval datatype
 najain    08/02/06 - refCounting optimizations
 najain    08/10/06 - add asserts
 parujain  08/04/06 - Datatype Timestamp
 najain    07/28/06 - handle nulls 
 najain    07/19/06 - ref-count tuples 
 najain    07/13/06 - ref-count timestamps 
 najain    07/06/06 - cleanup
 najain    05/05/06 - dump
 skaluska  04/04/06 - add kind to putNext 
 skaluska  03/28/06 - implementation
 skaluska  03/27/06 - implementation
 anasrini  03/24/06 - add toString 
 skaluska  03/20/06 - implementation
 skaluska  03/14/06 - query manager 
 anasrini  03/16/06 - queue related API 
 anasrini  03/15/06 - add stubs for compilation 
 skaluska  02/06/06 - Creation
 skaluska  02/06/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/Output.java /main/64 2012/10/09 05:16:40 sbishnoi Exp $
 *  @author  skaluska
 *  @since   1.0
 */

package oracle.cep.execution.operators;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collection;
import java.util.logging.Level;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalFormat;

import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.BigDecimalAttributeValue;
import oracle.cep.dataStructures.external.BigintAttributeValue;
import oracle.cep.dataStructures.external.ByteAttributeValue;
import oracle.cep.dataStructures.external.CharAttributeValue;
import oracle.cep.dataStructures.external.DoubleAttributeValue;
import oracle.cep.dataStructures.external.ObjAttributeValue;
import oracle.cep.dataStructures.external.XmltypeAttributeValue;
import oracle.cep.dataStructures.external.FloatAttributeValue;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.external.IntAttributeValue;
import oracle.cep.dataStructures.external.BooleanAttributeValue;
import oracle.cep.dataStructures.external.TimestampAttributeValue;
import oracle.cep.dataStructures.external.IntervalAttributeValue;
import oracle.cep.dataStructures.external.IntervalYMAttributeValue;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.synopses.RelationSynopsis;
import oracle.cep.interfaces.output.QueryOutput;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.service.ExecContext;
import oracle.cep.dataStructures.internal.ITuplePtr;

/**
 * Output - The execution operator that delivers the output of a query to the
 * specified query output destination
 *
 * @author skaluska
 * @since 1.0
 */
public class Output extends ExecOpt
{
  /** Specification of the attributes in the output stream */
  private TupleSpec   attrSpecs;

  /** Number of attributes */
  private int         numAttrs;

  /** Query output */
  private QueryOutput output;

  /** Where do these columns come from? */
  private Column      inCols[];

  /** column names for output */
  private String      columnNames[];

  /** Query that uses this output */
  private String      outputName;
  
  /** Synopsis to keep output tuples*/
  private RelationSynopsis outSyn;
  
  /** Synopsis to keep plus input tuples*/
  private RelationSynopsis plusSyn;
  
  /** Synopsis to keep minus input tuples*/
  private RelationSynopsis minusSyn;
  
  /** evaluation context */
  private IEvalContext evalContext;
  
  /** flag to check if primary key exists over output relation */
  private boolean      isPrimaryKeyExist;
  
  /** flag to check if output relation will support update semantics */
  private boolean      isUpdateSemantics;
  
  /**
   * boolean indicates whether to use milliseconds or nanoseconds 
   * as base timeline. 
   * true = use Millisecond as base timeline
   * false = use Nanosecond as base timeline
   * Default is Nanosecond
   * */
  private boolean isBaseTimelineMillisecond;
  
   
  /** scan tuples on the basis of primary key values in output synopsis*/
  private int          keyScanId;
  
  /** a full scan over output synopsis*/
  private int          fullScanId;
  
  /** full scan Id in plus Synopsis */
  private int          plusFullScanId;
  
  /** scan tuples on the basis of all columns value match in plus synopsis */
  private int          plusScanId;
  
  /** scan tuples on the basis of all columns value match in minus synopsis */
  private int          minusScanId;
  
  /** full scan Id in minus Synopsis */
  private int          minusFullScanId;
  
  /** Positions of Primary key Attributes in tuple*/
  ArrayList<Integer>   primaryKeyAttrPos;
  
  /** batch the output tuples for each unit of time */
  private boolean     batchOutputTuples;

  /** flag to indicate whether this output operator should propagateHeartbeat */
  private boolean     propagateHeartbeat;


  /** collection of output tuples */
  Collection<TupleValue>        insertTuples;
  Collection<TupleValue>        deleteTuples;
  Collection<TupleValue>        updateTuples;

  /**
   * Constructor for Output
   * @param ec TODO
   * @param maxAttrs
   *          Number of output attributes for this Output
   */
  public Output(ExecContext ec, int maxAttrs)
  {
    super(ExecOptType.EXEC_OUTPUT, new OutputState(ec, maxAttrs), ec);
    attrSpecs = new TupleSpec(factoryMgr.getNextId(), maxAttrs);
    numAttrs = 0;
    inCols = new Column[maxAttrs];
    columnNames = new String[maxAttrs];
    isPrimaryKeyExist = false;
    isUpdateSemantics = false;
    isBaseTimelineMillisecond = false;
    outSyn = null;
    // by default, there will not be any output tuple accumulation
    batchOutputTuples = false;    
    // by default, heartbeat tuples won't be propagated.
    propagateHeartbeat = false;
    // initialize collections
    insertTuples = new LinkedList<TupleValue>();
    deleteTuples = new LinkedList<TupleValue>();
    updateTuples = new LinkedList<TupleValue>();
  }

  /**
   * Getter for output in Output
   * 
   * @return Returns the output
   */
  public QueryOutput getOutput()
  {
    return output;
  }

  /**
   * Setter for output in Output
   * 
   * @param output
   *          The output to set.
   */
  public void setOutput(QueryOutput output)
  {
    this.output = output;
  }

  /**
   * Setter for isPrimaryKeyExist flag
   * @param isPrimaryKeyExist
   *        The flag isPrimaryKeyExist to set.
   */
  public void setIsPrimaryKeyExist(boolean isPrimaryKeyExist)
  {
    this.isPrimaryKeyExist = isPrimaryKeyExist;
  }
  
  /**
   * Setter for keyScanId
   * @param keyScanId
   */
  public void setKeyScanId(int keyScanId)
  {
    this.keyScanId = keyScanId;
  }
  
  /**
   * Setter for fullScanId
   * @param fullScanId
   */
  public void setFullScanId(int fullScanId)
  {
    this.fullScanId = fullScanId;
  }
  
  /**
   * Setter for plusScanId
   * @param plusScanId
   */
  public void setPlusScanId(int plusScanId)
  {
    this.plusScanId = plusScanId;
  }
  
  /**
   * Setter for plusFullScanId
   * @param plusFullScanId
   */
  public void setPlusFullScanId(int plusFullScanId)
  {
    this.plusFullScanId = plusFullScanId;
  }
  
  /**
   * Setter for minusScanId
   * @param minusScanId
   */
  public void setMinusScanId(int minusScanId)
  {
    this.minusScanId = minusScanId;
  }
  
  /**
   * Setter for minusFullScanId
   * @param minusFullScanId
   */
  public void setMinusFullScanId(int minusFullScanId)
  {
    this.minusFullScanId = minusFullScanId;
  }
  
  /**
   * Setter for isUpdateSemantics flag
   * @param isUpdateSemantics
   */
  public void setIsUpdateSemantics(boolean isUpdateSemantics)
  {
    this.isUpdateSemantics = isUpdateSemantics;
  }
  
  /**
   * Setter for IsStatsEnabled flag Used while measuring the latency for
   * strm/reln/output operators
   * 
   * @param isenabled
   *          Whether enabled or not
   * @param isBaseTimelineMillisecond
   *          Whether millisecond or nanosecond
   */
  public void setIsStatsEnabled(boolean isenabled, 
                                boolean isBaseTimelineMillisecond)
  {
    this.isStatsEnabled 
      = isenabled ||
        this.execContext.getExecStatsMgr().isRunTimeOperatorStatsEnabled();
    this.isBaseTimelineMillisecond = isBaseTimelineMillisecond;
    stats.setIsEnabled(isenabled);
    // if disabled after disabling then latency needs to be reset
    if (!isenabled)
      stats.clearLatency();
  }
  
  /**
   * @return the isBaseTimelineMillisecond
   */
  public boolean getIsBaseTimelineMillisecond()
  {
    return isBaseTimelineMillisecond;
  }

    
  /**
   * Setter for outSyn
   * @param outSyn
   */
  public void setOutSyn(RelationSynopsis outSyn)
  {
    this.outSyn = outSyn;
  }
  
  /**
   * Getter for outSyn
   * @return outSyn
   */
  public RelationSynopsis getOutSyn()
  {
    return this.outSyn;
  }
  
  /**
   * Setter for plusSyn
   * @param plusSyn
   */
  public void setPlusSyn(RelationSynopsis plusSyn)
  {
    this.plusSyn = plusSyn;
  }
  
  /**
   * Getter for plusSyn
   * @return plusSyn
   */
  
  public RelationSynopsis getPlusSyn()
  {
    return this.plusSyn;
  }
  
  /**
   * Setter for minusSyn
   * @param minusSyn
   */
  public void setMinusSyn(RelationSynopsis minusSyn)
  {
    this.minusSyn = minusSyn;
  }
  
  /**
   * Getter for minusSyn
   * @return minusSyn
   */
  
  public RelationSynopsis geMinusSyn()
  {
    return this.minusSyn;
  }
 
  
  /**
   * Setter for evalContext for this operator
   * @param evalContext
   */
  public void setEvalContext(IEvalContext evalContext)
  {
    this.evalContext = evalContext;
  }
  
  /**
   * Set List of Primary Key Attribute positions in Output Tuple 
   * @param primaryKeyAttrPos
   */
  public void setPrimaryKeyAttrPos(ArrayList<Integer> primaryKeyAttrPos)
  {
    this.primaryKeyAttrPos = primaryKeyAttrPos;
  }
  
  /**
   * Add an attribute at the next position
   * 
   * @param type
   *          Attribute type
   * @param len
   *          Attribute maximum length
   * @param precision
   *          Attribute precision
   * @param scale
   *          Attribute scale
   * @param inCol
   *          Input column position
   * @param name
   *          Output column name
   * @throws ExecException
   */
  public void addAttr(AttributeMetadata attrMetadata, int inCol, String name)
      throws ExecException
  {
    attrSpecs.addAttr(numAttrs,attrMetadata);
    inCols[numAttrs] = new Column(inCol);
    columnNames[numAttrs] = name;
    numAttrs++;
  }

  /**
   * Initialize Must be called before the operator can run
   * 
   * @throws CEPException
   */
  public void initialize() throws CEPException
  {
    // Initialize the query output
    output.setNumAttrs(numAttrs);
    for (int i = 0; i < numAttrs; i++)
    {
      output.setAttrInfo(i, columnNames[i], attrSpecs.getAttrMetadata(i));
    }
    // Set whether stream or relation
    output.setIsStream(isStream);
    
    // Set Primary Key Attribute Information
    output.setPrimarKeyAttrList(primaryKeyAttrPos);
    
    // Start the output
    output.start();
  }

  /**
   * Allocate the output tuple
   * 
   * @param s
   *          Output state in which to allocate
   * @throws CEPException
   */
  protected void allocateOutput(OutputState s) throws CEPException
  {
    int i;
    AttributeValue[] attrval;
    // Allocate attributes
    attrval = new AttributeValue[numAttrs];
    for (i = 0; i < numAttrs; i++)
    {
      String attrName = columnNames[i];
      //int attrLen = attrSpecs.getAttrLen(i);
      switch (attrSpecs.getAttrType(i).getKind())
      {
        case INT:
          attrval[i] = new IntAttributeValue(attrName, 0);
          break;
        case BOOLEAN:
          attrval[i] = new BooleanAttributeValue(attrName, false);
          break;
        case BIGINT:
          attrval[i] = new BigintAttributeValue(attrName, 0);
          break;
        case FLOAT:
          attrval[i] = new FloatAttributeValue(attrName, 0);
          break;
        case DOUBLE:
          attrval[i] = new DoubleAttributeValue(attrName, 0);
          break;
        case BIGDECIMAL:
          attrval[i] = new BigDecimalAttributeValue(attrName, BigDecimal.ZERO);
          break;
        case CHAR:
          attrval[i] = new CharAttributeValue(attrName);
          break;
        case XMLTYPE:
          attrval[i] = new XmltypeAttributeValue(attrName);
          break;
        case BYTE:
          attrval[i] = new ByteAttributeValue(attrName);
          break;
        case TIMESTAMP:
          attrval[i] = new TimestampAttributeValue(attrName, 0);
          break;
        case INTERVAL:
          IntervalFormat fmt = attrSpecs.getAttrMetadata(i).getIntervalFormat();
          attrval[i] = new IntervalAttributeValue(attrName, new String());
          ((IntervalAttributeValue)attrval[i]).setFormat(fmt);
          break;
        case INTERVALYM:
          IntervalFormat ymfmt = attrSpecs.getAttrMetadata(i).getIntervalFormat();
          attrval[i] 
            = new IntervalYMAttributeValue(attrName, new String(), ymfmt);
          break;
        case OBJECT:
          attrval[i] = new ObjAttributeValue(attrName);
          break;
        default:
          assert false;
      }
    }

    s.outputTuple = new TupleValue(outputName, 0, attrval, false);
  }

  /**
   * Populate the output tuple
   * 
   * @param s
   *          Output state in which to populate
   * @param tPtr tuple pointer from where we will copy the attribute values
   * @param time timestamp value of output tuple
   * @param isTotalOrderGuarantee total ordering flag
   * @throws CEPException
   */
  protected void populateOutput(OutputState s, 
                              ITuplePtr   tPtr, 
                              long        time, 
                              boolean     isTotalOrderGuarantee) 
    throws CEPException
  {
    // Set timestamp
    s.outputTuple.setTime(time);
    // Set last output timestamp
    s.lastOutputTs = time;
    ITuple t = tPtr.pinTuple(IPinnable.WRITE);
    assert t != null;
    t.copyTo(s.outputTuple, numAttrs, attrSpecs, inCols);
    tPtr.unpinTuple();

    // This is for performance measurements - 
    // can be used to figure out latency due to CQL engine processing alone
    s.outputTuple.setEngineOutTime(System.nanoTime());
    
    // set total ordering guarantee flag
    s.outputTuple.setTotalOrderGuarantee(isTotalOrderGuarantee);
  }
  
  
  /**
   * Checks if inputTuple does not have null values for primary key columns
   * @param inputTuple
   * @throws CEPException
   */
  protected void checkPrimaryKeyConstraints(ITuplePtr inputTuple)
    throws CEPException
  {
    ITuple dest = inputTuple.pinTuple(IPinnable.WRITE);
    
    int numPrimaryKeyAttrs;
    numPrimaryKeyAttrs = (isPrimaryKeyExist) ? primaryKeyAttrPos.size() : 0;
    // CHECK: Primary key attributes cannot be null
    for(int i = 0 ; i < numPrimaryKeyAttrs; i++ )
    {
      if(dest.isAttrNull(primaryKeyAttrPos.get(i)))
      {
        inTupleStorageAlloc.release(inputTuple);
        throw new ExecException(ExecutionError.CANNOT_INSERT_NULL, dest.toString());
      }
    }
    inputTuple.unpinTuple();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.operators.ExecOpt#run(int)
   */
  @Override
  public int run(int timeSlice) throws CEPException
  {
    int numElements;
    boolean done = false;
    OutputState s = (OutputState) mut_state;
    boolean exitState = true;

    assert s.state != ExecState.S_UNINIT;
    try
    {
      // Number of input elements to process
      numElements = timeSlice;
      while ((s.stats.getNumInputs() < numElements) || (!exitState))
      {
        // Get next input element
        s.inputElement = inputQueue.dequeue(s.inputElementBuf);
        
        // If the input queue is empty, then break the loop
        if(s.inputElement == null)
        {
          exitState = true;
          break;
        }
                 
        // Update stats
        if(s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
          s.stats.incrNumInputHeartbeats();
        else
          s.stats.incrNumInputs();
        exitState = false;
        
        // Get input tuple from queue element
        s.inputTuple = s.inputElement == null ? null:s.inputElement.getTuple();
        

        // Get the timestamp
        s.inputTs = s.inputElement.getTs();

        // We should have a progress of time.
        if (s.lastInputTs > s.inputTs )
        {
          throw ExecException.OutOfOrderException(
                  this,
                  s.lastInputTs,
                  s.inputTs,
                  s.inputElement.toString());
        }

        assert s.inputTs >= s.minNextInputTs : getDebugInfo(s.inputTs,
          s.minNextInputTs, s.inputElement.getKind().name(),
          s.lastInputKind.name());

        s.minNextInputTs = s.inputElement.getTotalOrderingGuarantee() ?
                           s.inputTs + 1 : s.inputTs;

        s.inputKind = s.inputElement.getKind();

        // get the total ordering flag from input tuple
        s.isTotalOrderGuarantee
          = s.inputElement.getTotalOrderingGuarantee();
        
        // Calculate and Update Latency Statistics
        //logLatency(s);
        if(s.inputKind == QueueElement.Kind.E_PLUS)
        {
          handlePlus(s);
        }
        if(s.inputKind == QueueElement.Kind.E_MINUS)
        {
          handleMinus(s);
        }
        if(s.inputKind == QueueElement.Kind.E_HEARTBEAT)
        {
          handleHeartbeat(s);
        }

        // Update last input timestamp
        s.lastInputTs = s.inputTs;
        if (done)
          break;
      }
    }
    catch (SoftExecException e1)
    {
      // TODO Ignore them
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e1);
      return 0;
    }
    finally{
      s.stats.setEndTime(System.nanoTime());
    }
    return 0;
  }
  

  
  /**
   * Handle the PLUS input tuple
   * @param s
   */
  private void handlePlus(OutputState s) throws CEPException
  {
    // Handle if timestamp progressed
    if(s.inputTs > s.lastInputTs)
      handleTimestampProgress(s, true, false);
    
    // Case: Simplest case when there is no flag mentioned on query destination
    //       and no primary key specified
    if(!isPrimaryKeyExist && !batchOutputTuples)
    {
      allocateOutput(s);
      
      // Populate the output tuple with input tuple;
      populateOutput(s, s.inputTuple, s.inputElement.getTs(),
                      false);   //TODO review. getting assertion with propagateHeartbeat
                     //s.isTotalOrderGuarantee);
      
      // Output single plus tuple
      output(s.outputTuple, QueueElement.Kind.E_PLUS, s);      
      
      // Update last output timestamp
      s.lastOutputTs = s.inputElement.getTs();
      
      // Release input tuple
      releaseInputTuple(s);
    }
    else if(!isPrimaryKeyExist && batchOutputTuples)
    {
      allocateOutput(s);
      
      // Populate the output tuple with input tuple;
      populateOutput(s, s.inputTuple, s.inputElement.getTs(), 
                      false);   //TODO review. getting assertion with propagateHeartbeat
                     //s.isTotalOrderGuarantee);
      
      // Add the output tuple as new plus output tuple
      insertTuples.add(s.outputTuple);
    }
    // Case: In all other cases, we have to batch the tuples till timestamp
    //       progresses to next value.    
    else
    {
      // Check if we have any MINUS tuple with same attribute values in minus
      // synopsis; If there exists any such MINUS tuple, remove that from minus
      // synopsis and don't insert current PLUS tuple.
      evalContext.bind(s.inputTuple, IEvalContext.UPDATE_ROLE);
      s.minusIter = minusSyn.getScan(minusScanId);
      s.minusTuple = s.minusIter.getNext();
      minusSyn.releaseScan(minusScanId, s.minusIter);
      if(s.minusTuple == null)
      {
        plusSyn.insertTuple(s.inputTuple);
      }
      else
      {
        minusSyn.deleteTuple(s.minusTuple);
      }
    }
    // Note: If this is the last tuple of current timestamp value, then we have
    // to perform handleTimestampProgress scenario
    if(s.isTotalOrderGuarantee)
      handleTimestampProgress(s, false, false);
  }
  
  /**
   * Handle the MINUS input tuple
   * @param s
   */
  private void handleMinus(OutputState s) throws CEPException
  {
    // Handle if there is any timestamp progressed
    if(s.inputTs > s.lastInputTs)
      handleTimestampProgress(s, true, false);
    
    // Case: Simplest case when there is no flag mentioned on query destination
    //       and no primary key specified
    if(!isPrimaryKeyExist && !batchOutputTuples)
    {
      allocateOutput(s);
      
      // Populate the output tuple with input tuple;
      populateOutput(s, s.inputTuple, s.inputElement.getTs(), 
                      false);   //TODO review. getting assertion with propagateHeartbeat
                     //s.isTotalOrderGuarantee);
      
      // Output single minus tuple
      output(s.outputTuple, QueueElement.Kind.E_MINUS, s);      
      
      // Update last output timestamp
      s.lastOutputTs = s.inputElement.getTs();
      
      // Release input tuple
      releaseInputTuple(s);
    }
    else if(!isPrimaryKeyExist && batchOutputTuples)
    {
      allocateOutput(s);
      
      // Populate the output tuple with input tuple;
      populateOutput(s, s.inputTuple, s.inputElement.getTs(), 
                      false);   //TODO review. getting assertion with propagateHeartbeat
                     //s.isTotalOrderGuarantee);
      
      // Add the output tuple as new minus output tuple
      deleteTuples.add(s.outputTuple);
    }
    // Case: In all other cases, we have to batch the tuples till timestamp
    //       progresses to next value.    
    else
    {
      // Check if we have any PLUS tuple with same attribute values in plus
      // synopsis; If there exists any such PLUS tuple, remove that from plus
      // synopsis and don't insert current MINUS tuple.
      evalContext.bind(s.inputTuple, IEvalContext.UPDATE_ROLE);
      s.plusIter = plusSyn.getScan(plusScanId);
      s.plusTuple = s.plusIter.getNext();
      plusSyn.releaseScan(plusScanId, s.plusIter);
      if(s.plusTuple == null)
      {
        minusSyn.insertTuple(s.inputTuple);
      }
      else
      {
        plusSyn.deleteTuple(s.plusTuple);
      }      
    }
    
    // Note: If this is the last tuple of current timestamp value, then we have
    // to perform handleTimestampProgress scenario
    if(s.isTotalOrderGuarantee)
      handleTimestampProgress(s, false, false);
  }
  
  /**
   * Release the input tuple after completing the processing
   * @param s
   */
  private void releaseInputTuple(OutputState s)
  {
    if (s.inputTuple != null)
    {
      inTupleStorageAlloc.release(s.inputTuple);
    }
  }
  
  /**
   * Handle the heartbeat tuple
   * @param s
   */
  private void handleHeartbeat(OutputState s) 
    throws CEPException
  {
    if(s.inputTs > s.lastInputTs)
    {
      handleTimestampProgress(s, true, true);
    }
    else if(s.isTotalOrderGuarantee)
    {
      handleTimestampProgress(s, false, true);
    }
    else if(propagateHeartbeat && s.inputTs > s.lastOutputTs)
    {
      //propagate heartbeat only when its ts > lastOutputTs
      s.outputTuple =
        new TupleValue(outputName, s.inputTs, null, true);
      
      s.outputTuple.setTotalOrderGuarantee(s.isTotalOrderGuarantee);
      output(s.outputTuple, QueueElement.Kind.E_HEARTBEAT, s);
      
      // Update last output timestamp
      s.lastOutputTs = s.inputTs;      
    }
    // Release Input Tuple
    releaseInputTuple(s);
  }  
  
  /**
   * Handles the timestamp progress
   * @param s
   */
  private void handleTimestampProgress(OutputState s, 
                                       boolean hasTimestampChanged, boolean force) 
    throws CEPException
  {
    boolean done = false;
    boolean isMinusSynopsisEmpty = false;
    boolean isOutputBatchEmpty = true;
    
    // If there is no batching and no primary key; there is no pending tuples
    // to progress, so return.
    if(!batchOutputTuples && !isPrimaryKeyExist)
    {
      if(propagateHeartbeat)
      {
    	if (force) {
	        //propagate heartbeat only when its ts > lastOutputTs
	        s.outputTuple =
	          new TupleValue(outputName, s.inputTs, null, true);
	        
	        s.outputTuple.setTotalOrderGuarantee(false);
	        
	        output(s.outputTuple, QueueElement.Kind.E_HEARTBEAT, s);
	        
	        // Update last output timestamp
	        s.lastOutputTs = s.inputTs;
    	}   
        return;
      }
      else
      {
        return;
      }
    }
      
    // Calculate Output timestamp of the remaining processed tuple
    long outputTs;
    if(hasTimestampChanged)
      outputTs = s.lastInputTs;
    else
      outputTs = s.inputTs;
    
    // If there is no Primary Key exists & Only batching is enabled, then
    // Iterate synopsis and prepare the output collections for the
    // insert/update/delete tuples and propagate it to downstream channel.
    if(!isPrimaryKeyExist && batchOutputTuples)
    {
      // Prepare and send current batch of tuples to downstream channels
      outputBatch(s, outputTs);
      isOutputBatchEmpty = true;
      return;
    }    
    else if(batchOutputTuples && isPrimaryKeyExist)
    {
      insertTuples = new LinkedList<TupleValue>();
      deleteTuples = new LinkedList<TupleValue>();
      updateTuples = new LinkedList<TupleValue>();
      isOutputBatchEmpty = true;
    }
    
    while(!done)
    {      
      // Get next PLUS tuple
      s.plusIter = plusSyn.getScan(plusFullScanId);
      s.plusTuple = s.plusIter.getNext();
      plusSyn.releaseScan(plusFullScanId, s.plusIter);
      if(s.plusTuple != null)
      {
        // Check if there is already an output tuple with same primary key
        evalContext.bind(s.plusTuple, IEvalContext.UPDATE_ROLE);
        s.outIter = outSyn.getScan(keyScanId);
        s.outTuple = s.outIter.getNext();
        outSyn.releaseScan(keyScanId, s.outIter);
        
        if(s.outTuple == null)
        {
          //If there is no output tuple, this is first tuple with the current
          // primary key value
          allocateOutput(s);
          populateOutput(s, s.plusTuple, outputTs, false);
          
          // If BATCHING is enabled,then add the new plus tuple to insertTuples
          if(batchOutputTuples)
          {
            insertTuples.add(s.outputTuple);
            isOutputBatchEmpty = false;
          }
          else
            output(s.outputTuple, QueueElement.Kind.E_PLUS, s);
                  
          plusSyn.deleteTuple(s.plusTuple);
          outSyn.insertTuple(s.plusTuple);
          
          // Update last output ts
          s.lastOutputTs = outputTs;         
        }
        else
        {
          // If there is corresponding output tuple for this primary key value,
          // then check if we got MINUS for this output tuple 
          evalContext.bind(s.outTuple, IEvalContext.UPDATE_ROLE);
          s.minusIter = minusSyn.getScan(minusScanId);
          s.minusTuple = s.minusIter.getNext();
          minusSyn.releaseScan(minusScanId, s.minusIter);
          if(s.minusTuple == null)
          {
            throw new ExecException(ExecutionError.UNIQUE_CONSTRAINT_VIOLATION,
                s.outTuple.toString());
          }
          else
          {
            // If UPDATE semantics is enabled, then we will send only one
            // UPDATE tuple instead of a pair of MINUS & PLUS.
            if(isUpdateSemantics)
            {
              allocateOutput(s);
              // The new plus tuple will go as UPDATE tuple
              populateOutput(s, s.plusTuple, outputTs, false);
              // If BATCHING is enabled, then put this tuple to UPDATE list
              if(batchOutputTuples)
              {
                updateTuples.add(s.outputTuple);
                isOutputBatchEmpty = false;
              }
              else
                output(s.outputTuple, QueueElement.Kind.E_UPDATE, s);
              
              // Update last output ts
              s.lastOutputTs = outputTs;
            }
            else
            {
              // Allocate and populate a MINUS tuple
              allocateOutput(s);
              populateOutput(s, s.minusTuple, outputTs, false);
              if(batchOutputTuples)
              {
                deleteTuples.add(s.outputTuple);
                isOutputBatchEmpty = false;
              }
              else
                output(s.outputTuple, QueueElement.Kind.E_MINUS, s);
              
              // Allocate and populate a PLUS tuple
              allocateOutput(s);
              populateOutput(s, s.plusTuple, outputTs, false);
              if(batchOutputTuples)
              {
                insertTuples.add(s.outputTuple);
                isOutputBatchEmpty = false;
              }
              else
                output(s.outputTuple, QueueElement.Kind.E_PLUS, s);
              
              // Update last output ts
              s.lastOutputTs = outputTs;
            }            
            minusSyn.deleteTuple(s.minusTuple);
            outSyn.deleteTuple(s.outTuple);
            plusSyn.deleteTuple(s.plusTuple);
            outSyn.insertTuple(s.plusTuple);
            
            // Release minus tuple
            inTupleStorageAlloc.release(s.minusTuple);
            inTupleStorageAlloc.release(s.outTuple);
          }          
        }
      }
      else
      {
        while(!isMinusSynopsisEmpty)
        {
          s.minusIter = minusSyn.getScan(minusFullScanId);
          s.minusTuple = s.minusIter.getNext();
          if(s.minusTuple == null)
          {
            // Both PLUS and MINUS synopis has been scanned completely.
            done = true;
            isMinusSynopsisEmpty = true;
          }
          else
          {
            evalContext.bind(s.minusTuple, IEvalContext.UPDATE_ROLE);
            s.outIter = outSyn.getScan(fullScanId);
            s.outTuple = s.outIter.getNext();
            outSyn.releaseScan(fullScanId, s.outIter);
            // Throw exception if no plus tuple in outSyn for given Minus tuple
            if(s.outTuple == null)
            {
              throw new ExecException(ExecutionError.NO_PLUS_TUPLE_IN_SYNOPSIS,
                                     s.inputTuple.toString(), this.toString());
            }
            else
            {
              outSyn.deleteTuple(s.outTuple);
              minusSyn.deleteTuple(s.minusTuple);
              
              // Prepare OUTPUT tuple
              // Note: setting the total ordering flag to FALSE always
              // because this needs look-ahead approach to implement outputtuples()
              allocateOutput(s);
              populateOutput(s, s.outTuple, outputTs, false);
              
              // If BATCHING is enabled, then add the MINUS tuple to the
              // deleteTuples list.
              if(batchOutputTuples)
              {
                deleteTuples.add(s.outputTuple);
                isOutputBatchEmpty = false;
              }
              else
              {
                output(s.outputTuple, QueueElement.Kind.E_MINUS, s);
              }
              
              //Update last output timestamp
              s.lastOutputTs = outputTs;
              // Release tuple memory
              inTupleStorageAlloc.release(s.minusTuple);
              inTupleStorageAlloc.release(s.outTuple);
              
              // Loop to next minusTuple
              
            }  // End of else block (if(outTuple == null))
          } // End of else block (if(minusTuple == null))
        } // End of while(!isMinusSynopsisEmpty)
      }
    } // End of outermost loop
    
    
    // If BATCHING is enabled and batch is non-empty, then send this output
    // batch to downstream channel
    if(batchOutputTuples && !isOutputBatchEmpty)
    {
      outputBatch(insertTuples, deleteTuples, updateTuples, s);
      isOutputBatchEmpty = true;
    }   
  }
  
  /**
   * Prepare and output the batch tuples.
   * @param s
   * @throws CEPException
   */
  private void outputBatch(OutputState s, long outputTs) throws CEPException
  {
    assert batchOutputTuples;
    
    // check if the batch is empty or not
    boolean isOutputBatchEmpty = insertTuples.size() == 0 &&
                                 updateTuples.size() == 0 &&
                                 deleteTuples.size() == 0;
    if(!isOutputBatchEmpty)
    {
      outputBatch(insertTuples, deleteTuples, updateTuples, s);
      
      // Update last output timestamp
      s.lastOutputTs = outputTs;
      
      // Reinitialize the batch
      insertTuples = new LinkedList<TupleValue>();
      deleteTuples = new LinkedList<TupleValue>();
      updateTuples = new LinkedList<TupleValue>();
    }
    
  }

  /**
   * Send the current output batch to downstream channel
   * This batch is represented by three collections each for a tuple kind;
   * Each collection is passed as a parameter to the method.
   * @param insertTuples
   * @param deleteTuples
   * @param updateTuples
   * @throws CEPException
   */
  private void outputBatch(Collection<TupleValue> insertTuples,
                           Collection<TupleValue> deleteTuples,
                           Collection<TupleValue> updateTuples,
                           OutputState s) 
    throws CEPException
  {

    // insert the collection of out tuples in the output channel 
    try
    {
      output.putNext(insertTuples, deleteTuples, updateTuples);
    }
    catch(CEPException e)
    {
      // If the downstream components throws a soft exception, 
      // then dont drop the query; return nothing;
      if(e.getErrorCode() == ExecutionError.DOWNSTREAM_CHANNEL_SOFT_EXCEPTION)
      {                     
        LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);                     
      }
      else
        throw e;
    }
    catch(RuntimeException e)
    {
      throw new ExecException(ExecutionError.DOWNSTREAM_CHANNEL_EXCEPTION, e, getOptName());
    }
    
    // Update stats
    s.stats.incrNumOutputs(insertTuples.size() + 
                           deleteTuples.size() + 
                           updateTuples.size());
    
    // Increment batch number
    output.incrementBatchNumber();
  }
  
  
  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.operators.ExecOpt#deleteOp()
   */
  @Override
  public void deleteOp()
  {
    // TODO Auto-generated method stub

  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<OutputOp id=\"" + id + "\" numAttrs=\"" + numAttrs + "\">");
    sb.append("<Attrs>");
    for (int i = 0; i < numAttrs; i++)
    {
      Datatype type = attrSpecs.getAttrType(i);
      int len = attrSpecs.getAttrLen(i);
      int col = inCols[i].getColnum();
      sb.append("<Attr type=\"" + type + "\" len=\"" + len + "\" outcol=\""
          + col + "\"/>");
    }
    sb.append("</Attrs>");
    sb.append("<InputQueue>" + inputQueue.toString() + "</InputQueue>");
    sb.append("<InTupleAlloc id=\"" + 
        (tupleStorageAlloc == null ? "null":Long.toString(tupleStorageAlloc.getId())) 
        + "\" />");
    sb.append("</OutputOp>");
    return sb.toString();
  }

  /**
   * Update the latency for this output tuple
   * @param s
   */
  private void logLatency(OutputState s)
  {
    if(isStatsEnabled && s.inputElement.getKind() != QueueElement.Kind.E_HEARTBEAT)
    {
     long currentTime = 0;
      
      if(this.getIsBaseTimelineMillisecond())
      {
        currentTime = System.currentTimeMillis()*(long)Math.pow(10, 6);
      }
      else
      {
        currentTime = System.nanoTime();
      }
      
      long inputTs = s.inputElement.getTs();
      
      if(inputTs > currentTime)
      {
        LogUtil.warning(LoggerType.CUSTOMER, "input tuple for operator "
            + getOptName()
            + "has timestamp " + s.inputTs + 
            "ns higher than currrentBaseTime " + currentTime + "ns");
      }
      else                  
        s.stats.addLatency(currentTime - inputTs);
  
    }
  }
  
  /**
   * @return the propagateHeartbeat flag
   */
  public boolean isPropagateHeartbeat()
  {
    return this.propagateHeartbeat;
  }
  
  /**
   * @param propagateHeartbeat the propagateHeartbeat value to be set
   */
  public void setPropagateHeartbeat(boolean propagateHeartbeat)
  {
    this.propagateHeartbeat = propagateHeartbeat;
  }
  
  /**
   * @return the batchOutputTuples
   */
  public boolean isBatchOutputTuples()
  {
    return batchOutputTuples;
  }

  /**
   * @param batchOutputTuples the batchOuputTuples to set
   */
  public void setBatchOutputTuples(boolean batchOutputTuples)
  {
    this.batchOutputTuples = batchOutputTuples;    
  }
  
  /**
   * Either enqueue or accumulate the output tuple
   * @param tuple Output tuple
   * @param kind  Output tuple kind
   * @throws CEPException
   */
  private void output(TupleValue tuple, QueueElement.Kind kind, OutputState s) 
    throws CEPException
  {
    try
    {
      output.putNext(tuple, kind);        
    }
    catch(CEPException e)
    {
      // If the downstream components throws a soft exception, 
      // then dont drop the query; return nothing;
      if(e.getErrorCode() == ExecutionError.DOWNSTREAM_CHANNEL_SOFT_EXCEPTION)
      {                     
        LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);                     
      }
      else
        throw e;
    }
    catch(RuntimeException e)
    {
      throw new ExecException(ExecutionError.DOWNSTREAM_CHANNEL_EXCEPTION, e, getOptName());
    }
    
    // Update Stats
    if(kind == QueueElement.Kind.E_HEARTBEAT)
      s.stats.incrNumOutputHeartbeats();
    else
      s.stats.incrNumOutputs();
  }


  
  /** Check if heartbeat is pending */
  protected boolean isHeartbeatPending() 
  {
    if(propagateHeartbeat)
    {
      return super.isHeartbeatPending();
    }
    else
      return false;
  }
  
  protected boolean isPrimaryKeyExist()
  {
    return isPrimaryKeyExist;
  }

  protected IEvalContext getEvalContext()
  {
    return evalContext;
  }

  protected TupleSpec getAttrSpecs()
  {
    return attrSpecs;
  }

  protected int getNumAttrs()
  {
    return numAttrs;
  }

  protected Column[] getInCols()
  {
    return inCols;
  }
  
}
