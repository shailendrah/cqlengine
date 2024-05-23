/* Copyright (c) 2006, 2014, Oracle and/or its affiliates. 
All rights reserved.*/
/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/StreamSource.java /main/82 2014/02/26 00:16:23 sbishnoi Exp $ */

/*
 DESCRIPTION
 Declares StreamSource in package oracle.cep.execution.operators.

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk  08/14/13 - set snapshotid as current snapshotid for archiver records
 udeshmuk  07/23/13 - bug 16813624: make use of useMillisTs param while
                      determining timeout
 udeshmuk  07/11/13 - fix logging related to archived relation framework
 udeshmuk  05/21/13 - 16820093 : set snapshot id for heartbeat
 sbishnoi  04/07/13 - XbranchMerge sbishnoi_bug-15962405_ps6 from
                      st_pcbpel_11.1.1.4.0
 sbishnoi  04/01/13 - bug 16580667
 sbishnoi  03/08/13 - bug 16484087 fix heartbeat propagation for archiver sources
 sbishnoi  11/29/12 - throw exception if the total order for the timestamp is
                      not followed
 udeshmuk  10/09/12 - set cid and tid values to null post computation of
                      snapshotid
 sbishnoi  10/09/12 - XbranchMerge
                      sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0_11.1.1.7.0 from
                      st_pcbpel_11.1.1.4.0
 sbishnoi  10/08/12 - XbranchMerge sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0 from
                      st_pcbpel_pt-11.1.1.7.0
 sbishnoi  10/08/12 - modifying conditions to set the stats
 udeshmuk  09/09/12 - propagate snapshotid and handle event id
 sbishnoi  08/19/12 - bug 14502856
 udeshmuk  08/08/12 - do not use snapshot time
 vikshukl  07/30/12 - for now we don't support stream dimensions
 udeshmuk  05/27/12 - propagate snapshotId and archived flag
 udeshmuk  04/23/12 - set snapshot id for input event
 udeshmuk  03/19/12 - setNumRows for archived stream
 udeshmuk  02/10/12 - send heartbeat after snapshot
 udeshmuk  01/13/12 - propagate archiver resultset
 sbishnoi  01/13/12 - timestamp value is in nanos
 alealves  08/18/11 - XbranchMerge alealves_bug-12888416_cep from main
 alealves  08/17/11 - Improve memory usage by moving state to local variables
 vikshukl  06/05/11 - XbranchMerge vikshukl_bug-12324170_ps5 from
                      st_pcbpel_11.1.1.4.0
 vikshukl  06/01/11 - convert timestamp type to nanoseconds
 sborah    01/12/11 - refine INVALID_INPUT exception
 anasrini  12/19/10 - replace eval() with eval(ec)
 sbishnoi  03/29/10 - changing debug level
 sborah    07/15/09 - support for bigdecimal
 parujain  05/07/09 - lifecycle mgmt
 anasrini  05/08/09 - implement ExecSourceOpt interface
 sbishnoi  04/20/09 - logg the instance when inpTs crossover currentBaseTime
 sborah    04/13/09 - assertion check
 sbishnoi  04/08/09 - use TupleValue to set totalOrderingFlag
 sbishnoi  04/08/09 - removing followup Heartbeat
 sborah    03/31/09 - handle heartbeats for derived Ts
 anasrini  02/13/09 - fix for bug 8256792
 anasrini  02/13/09 - remove allowEnqueue
 parujain  01/29/09 - transaction mgmt
 sbishnoi  01/27/09 - total order optimization
 udeshmuk  01/16/09 - total ordering optimization
 sbishnoi  12/22/08 - changing element time to long value
 hopark    12/18/08 - handle NPE in populateOutput
 hopark    12/04/08 - have better message on populateOutput
 sborah    11/24/08 - alter base timeline for latency calc
 hopark    10/28/08 - fix heartbeat handling in stats
 hopark    10/15/08 - TupleValue refactoring
 hopark    10/07/08 - use execContext to remove statics
 hopark    10/16/08 - fix NPE
 sbishnoi  08/03/08 - support for nanosecond
 sbishnoi  07/24/08 - modify derived ts support to include TIMESTAMP datatype
 mthatte   04/02/08 - derived timestamp
 sbishnoi  03/26/08 - removing println statements
 udeshmuk  03/17/08 - add hb check in canBeScheduled.
 hopark    02/05/08 - parameterized error
 udeshmuk  01/17/08 - change in the way of getting timestamp of tuple.
 hopark    12/07/07 - cleanup spill
 udeshmuk  12/17/07 - heartbeat support.
 hopark    10/30/07 - remove IQueueElement
 hopark    10/22/07 - remove TimeStamp
 parujain  10/17/07 - cep-bam integration
 parujain  10/04/07 - end src when oper removed
 anasrini  08/27/07 - support for ELEMENT_TIME
 hopark    07/13/07 - dump stack trace on exception
 parujain  06/26/07 - mutable state
 hopark    06/19/07 - cleanup
 hopark    05/22/07 - logging support
 parujain  05/24/07 - handle softexecException
 hopark    05/11/07 - remove System.out.println(use java.util.logging instead)
 parujain  05/08/07 - monitoring statistics
 najain    05/09/07 - variable length datatype support
 hopark    05/08/07 - use tuple.copy in populateOutput
 hopark    03/23/07 - throws exceptions from QueueElement
 hopark    03/21/07 - add TuplePtr pin
 najain    03/14/07 - cleanup
 parujain  03/16/07 - debug level
 najain    03/12/07 - bug fix
 parujain  02/13/07 - interfaces with ConfigManager
 najain    02/19/07 - bug fix
 najain    01/04/07 - spill over support
 hopark    11/17/06 - override addToScheduler to add sourceop
 hopark    11/16/06 - add bigint datatype
 najain    11/06/06 - add canBeScheduled
 najain    10/30/06 - debugging support
 parujain  10/06/06 - Interval datatype
 anasrini  09/13/06 - attr name
 najain    08/10/06 - add asserts
 parujain  08/03/06 - Timestamp datastructure
 najain    07/28/06 - handle static relations 
 najain    07/28/06 - handle nulls 
 najain    07/13/06 - ref-count timestamps 
 najain    07/13/06 - ref-count timeStamp support 
 najain    05/23/06 - bug fix 
 najain    04/18/06 - time is a part of tuple 
 skaluska  04/04/06 - add tsStoreAlloc 
 najain    03/31/06 - bug fixes 
 skaluska  03/22/06 - implementation
 anasrini  03/24/06 - add toString 
 skaluska  03/20/06 - implementation
 skaluska  03/14/06 - query manager 
 anasrini  03/15/06 - change call to tupleSpec.addAttr 
 skaluska  02/06/06 - Creation
 skaluska  02/06/06 - Creation
 */
/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/StreamSource.java /main/82 2014/02/26 00:16:23 sbishnoi Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.common.SQLType;
import oracle.cep.dataStructures.external.TupleKind;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.memory.Tuple;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.comparator.ArchiverTupleComparator;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.queues.ISharedQueueWriter;
import oracle.cep.interfaces.InterfaceException;
import oracle.cep.interfaces.input.TableSource;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.service.ExecContext;
import oracle.cep.snapshot.SnapshotContext;
import oracle.cep.util.DebugUtil;

/**
 * StreamSource is the execution operator that reads input tuples for registered
 * streams.
 * 
 * @author skaluska
 */
public class StreamSource extends ExecSourceOpt
{  
  /** Specification of the attributes in the input stream */
  protected TupleSpec        attrSpecs;

  /** Number of attributes */
  protected int              numAttrs;

  /** Source who is feeding us the tuples */
  protected TableSource      source;

  /** Where do these go in the output */
  private Column           outCols[];

  /** column names of stream */
  private String           columnNames[];

  private long             maxTime;

  private long             ts;

  private boolean          isSystemTimestamped;
  
  /**
   * boolean indicates whether to use milliseconds or nanoseconds 
   * as base timeline. 
   * true = use Millisecond as base timeline
   * false = use Nanosecond as base timeline
   * Default is Nanosecond
   * */
  private boolean isBaseTimelineMillisecond;
  

  private long             timeoutDuration;

  /** Is the timestamp of a tuple a function of its attributes? */
  private boolean          isDerivedTS;

  /** evaluation context */
  private IEvalContext     evalContext;

  /** Type of timestamp: int,long */
  private Datatype         dtsType;

  /** roles for evaluation contex */
  protected static final int INPUT_ROLE  = IEvalContext.INPUT_ROLE;

  protected static final int OUTPUT_ROLE = IEvalContext.NEW_OUTPUT_ROLE;

  /** Arithmetic evaluator that computes the output tuple */
  private IAEval           derivedTSEvaluator;

  private int tsColNum = -1;
  
  private Datatype tsColType = null;

  private ArchiverTupleComparator ascComparator = null;
  
  private ArchiverTupleComparator descComparator = null;
  
  private boolean isReplayRange = true;
  
  /** Used only for archived stream.
   *  Indicates the position of worker identifier column in the schema */   
  private int              workerIdColNum = -1;

  /** Used only for archived stream.
   *  Indicates the position of txn identifier column in the schema */   
  private int              txnIdColNum = -1;

  private int              numRows = 0;

  /**
   * Constructor for StreamSource
   * @param ec TODO
   * @param maxAttrs
   *                Number of input attributes for this StreamSource
   */
  public StreamSource(ExecContext ec, int maxAttrs)
  {
    super(ExecOptType.EXEC_STREAM_SOURCE, new StreamSourceState(ec), ec);
    attrSpecs = new TupleSpec(factoryMgr.getNextId(), maxAttrs);
    numAttrs = 0;
    outCols = new Column[maxAttrs];
    columnNames = new String[maxAttrs];
    ts = 0;
    isSystemTimestamped = false;
    isBaseTimelineMillisecond = false;
    timeoutDuration = -1;
  }
 
  public int getWorkerIdColNum()
  {
    return this.workerIdColNum;
  }
  
  public void setWorkerIdColNum(int nm)
  {
    this.workerIdColNum = nm;
  }
  
  public int getTxnIdColNum()
  {
    return this.txnIdColNum;
  }
  
  public void setTxnIdColNum(int nm)
  {
    this.txnIdColNum = nm;
  }
  
  /** Gets the evaluation context if any for this sources derived timestamp */
  public IEvalContext getEvalContext()
  {
    return evalContext;
  }

  /** Sets the evaluation context if any for this sources derived timestamp */
  public void setEvalContext(IEvalContext evalContext)
  {
    this.evalContext = evalContext;
  }

  public long getMaxTime()
  {
    return maxTime;
  }

  /**
   * Getter for source in StreamSource
   * 
   * @return Returns the source
   */
  public TableSource getSource()
  {
    return source;
  }

  /**
   * Setter for source in StreamSource
   * 
   * @param source
   *                The source to set.
   */
  public void setSource(TableSource source)
  {
    this.source = source;
  }

  public void setIsSystemTimestamped(boolean isSysTs)
  {
    this.isSystemTimestamped = isSysTs;
  }

  public boolean isSystemTimestamped()
  {
    return this.isSystemTimestamped;
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

  
  public void setTimeoutDuration(long timeout)
  {
    this.timeoutDuration = timeout;
  }

  public long getTimeoutDuration()
  {
    return this.timeoutDuration;
  }

  public boolean isDerivedTS()
  {
    return isDerivedTS;
  }

  public void setDerivedTS(boolean derivedTS)
  {
    this.isDerivedTS = derivedTS;
  }

  /**
   * initialize
   * 
   * @throws CEPException
   */
  public void initialize() throws CEPException
  {
    if (source == null)
    {
      throw new ExecException(ExecutionError.STREAM_SRC_NOT_INITIALIZED, getSourceInfo(), this.toString());
    }
    source.setNumAttrs(numAttrs);
    source.setIsStream(true);
    source.setIsArchived(execContext.getTableMgr().getTable(streamId).isArchived());
    source.setIsDimension(false); // no support for (archived) stream dimension
    
    for (int i = 0; i < numAttrs; i++)
    {
      source.setAttrInfo(i, columnNames[i], attrSpecs.getAttrMetadata(i));
    }
    source.start();

    // If this stream has a derived TS, create a dummy tuple to hold the value
    if (this.isDerivedTS)
    {
      StreamSourceState s = (StreamSourceState) mut_state;
      TupleSpec derivedTSSpec = new TupleSpec(factoryMgr.getNextId());
      derivedTSSpec.addAttr(dtsType);
      IAllocator<ITuplePtr> tupleFactory = factoryMgr.get(derivedTSSpec);
      s.timestampTuple = (ITuplePtr) tupleFactory.allocate(); // SCRATCH_TUPLE
    }

  }

  /**
   * Add an attribute at the next position
   * 
   * @param name
   *                Attribute name
   * @param type
   *                Attribute type
   * @param len
   *                Attribute maximum length
   * @param precision 
   *                Attribute precision value
   * @param scale  
   *                Attribute scale value
   *                 
   * @param outColpopulateOutput
   *                Output column position
   * @throws ExecException
   */
  public void addAttr(String name, AttributeMetadata attrMetadata
                      , Column outCol)
      throws ExecException
  {
    attrSpecs.addAttr(numAttrs, attrMetadata);
    outCols[numAttrs]     = outCol;
    columnNames[numAttrs] = name;
    numAttrs++;
  }

  private String getSourceInfo()
  {
    StringBuilder sinfo = new StringBuilder();
    sinfo.append(" ( ");
    for (int i = 0; i < numAttrs; i++)
    {
      sinfo.append( columnNames[i] );
      sinfo.append(" ");
      sinfo.append(attrSpecs.getAttrType(i).name());
      if (i < (numAttrs-1))
        sinfo.append(", ");
    }
    sinfo.append(" ) ");
    return sinfo.toString();
  }
  
  /**
   * Populate element_time in output tuple.
   * 
   * @param s
   * @param timestamp
   * @throws CEPException
   */

  protected void populateOutputTS(StreamSourceState s, long timestamp)
      throws CEPException
  {
    if (s.outputTuple == null)
      return;
    ITuple o = s.outputTuple.pinTuple(IPinnable.WRITE);
    o.lValueSet(numAttrs - 1, timestamp);
    s.outputTuple.unpinTuple();
  }

  /**
   * Populate the output tuple
   * 
   * @throws CEPException
   */
  protected void populateOutput(StreamSourceState s) throws CEPException
  {
    if (s.outputTuple == null)
      return;
    
    try
    {
      ITuple o = s.outputTuple.pinTuple(IPinnable.WRITE);
      o.copyFrom(s.inputTuple, numAttrs - 1, attrSpecs);
      s.outputTuple.unpinTuple();
    }
    catch(CEPException e)
    {
      throw new SoftExecException(ExecutionError.PROPAGATE_ERROR,e.getMessage(),
          e.getCauseMessage(), e.getAction());
    }
    catch(Throwable e)
    {
      LogUtil.config(LoggerType.TRACE, e.toString() + "\n" 
          + source.toString() + "\n" 
          + s.inputTuple.toString() + "\n" 
          + DebugUtil.getStackTrace(e));
      throw new SoftExecException(ExecutionError.INCORRECT_INPUT_TUPLE,
          source.toString(), this.toString());
    }
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
    StreamSourceState s = (StreamSourceState) mut_state;
    boolean exitState = true;

    // A Flag to indicate that the timeout heartbeat has already propagated
    boolean isTimeOutHbtPropagated = false; 
    
    boolean useMillisTs = 
      execContext.getServiceManager().getConfigMgr().getUseMillisTs();
    
    assert s.state != ExecState.S_UNINIT;
    // timeoutDuration may have been updated
   // timeoutDuration = execContext.getTableMgr().getTableTimeOut(streamId);
   
    // Stats
    s.stats.incrNumExecutions();
    
    if(isRestoreTupleId())
      restoreTupleId();
    
    try
    {
      /** This lock is acquired to maintain the maximum time for this source */
      execMgr.getLock().readLock().lock();

      numElements = timeSlice;
      while ((s.stats.getNumInputs() < numElements) || (!exitState))
      {
        switch (s.state)
        {
        case S_INIT:
          // Get the next input tuple
          try
          {
            s.inputTuple = source.getNext();
          } catch (InterfaceException e)
          {
            throw new SoftExecException(
                ExecutionError.FAILED_TO_GET_TUPLE, 
                source.toString(),
                this.toString(), e.getMessage() + "\n" +e.getAction());
          }
          s.state = ExecState.S_INPUT_DEQUEUED;

        case S_INPUT_DEQUEUED:
          if (s.inputTuple == null)
          {
            // input queue is empty
            // Test and send heartbeat timeout.
            if (isSystemTimestamped && (timeoutDuration != -1))
            {
              //bug 16813624 - check for useMillisTs flag. Review carefully
              //whether isArchived should be used.
              if(this.getSource().isArchived() || 
                 useMillisTs)
              {
                // In case of system timestamped archived relation, test 
                // whether the nano equivalent of current clock time differs
                // from last input time by more than timeout period.
                // Send a heartbeat of time equal to nano equivalent of curr-
                // ent clock time. 
                // Note: lastInputTs == Constants.MIN_EXEC_TIME is the initi-
                // state.
                long currentNanoTime = System.currentTimeMillis() * 1000000L;
                long diff = currentNanoTime - s.lastInputTs;
                
                if((s.lastInputTs != Constants.MIN_EXEC_TIME) &&
                     (diff <= timeoutDuration || isTimeOutHbtPropagated))
                {
                  // return from run() method..let other operator execute
                  s.state = ExecState.S_INIT;
                  done = true;
                  break;
                }
                else if (s.lastInputTs == Constants.MIN_EXEC_TIME)
                {
                  if(isTimeOutHbtPropagated)
                  {
                    s.state = ExecState.S_INIT;
                    done = true;
                    break;
                  }
                }
              }
              else if ((System.nanoTime() - s.lastInputTs) < timeoutDuration ||
                       isTimeOutHbtPropagated)
              {
                // return from run() method..let other operator execute
                s.state = ExecState.S_INIT;
                done = true;
                break;
              }
              else
              {
                // Mark that timeout heartbeat is generated. 
                isTimeOutHbtPropagated = true;
              }
            }
            else
            {
              // we might still need to output a heartbeat
              if (s.lastInputTs <= s.lastOutputTs)
              {
                s.state = ExecState.S_INIT;
                done = true;
                break;
              }
            }
          }
          else
          { 
            if(s.inputTuple.getKind() == TupleKind.MINUS)
            {
              // bug no : 7707490
              // Minus tuples should not be allowed in streams.
              throw new SoftExecException(ExecutionError.INVALID_MINUS_TUPLE,
                 s.inputTuple.toSimpleString(), this.toString());
            }
            else if(s.inputTuple.getKind() == TupleKind.UPDATE)
            {
              // bug no : 7707490
              // Update tuples should not be allowed in streams.
              throw new SoftExecException(ExecutionError.INVALID_UPDATE_TUPLE,
                 s.inputTuple.toSimpleString(), this.toString());
            }
            else if(s.inputTuple.getKind() == TupleKind.UPSERT)
            {
              // UPSERT tuples should not be allowed in streams.
              throw new SoftExecException(ExecutionError.INVALID_UPSERT_TUPLE,
                 s.inputTuple.toSimpleString(), this.toString());
            }
            
            // Bump up our counts
            if(s.inputTuple.getKind() == TupleKind.HEARTBEAT || s.inputTuple.isBHeartBeat())
              s.stats.incrNumInputHeartbeats();
            else
              s.stats.incrNumInputs();

            //If it does NOT have a derived timestamp then assign it from input
            if (!isDerivedTS || s.inputTuple.isBHeartBeat())
            {

              // Get the timestamp from tuple value
              s.inputTs = s.inputTuple.getTime();

              /*
               * Use below to get latency from server processing times
               * Used first in context of algotrading benchmark
              s.inputTs = System.nanoTime();
              s.inputTuple.setTime(s.inputTs);
              */

              // We should have a progress of time.
              if (s.lastInputTs > s.inputTs)
              {
                throw ExecException.OutOfOrderException(this, s.lastInputTs,
                    s.inputTs, s.inputTuple.toString());
              }
              
              // Update the timeStamp
              maxTime = s.inputTs;
              // Update the last input Ts now
              s.lastInputTs = s.inputTs;
            }

            // Update stats if stream is derived timestamped
            //TODO: Fix Operator Latency Stats 
            /*if (isStatsEnabled && !s.inputTuple.isBHeartBeat())
            { 
              // WE do not allow stats for derived ts;
              assert !isDerivedTS;
              
              long currentBaseTime = 0;
              
              if(this.getIsBaseTimelineMillisecond())
              {
                currentBaseTime 
                  = System.currentTimeMillis()*(long)Math.pow(10,6);
              }
              else
              {
                currentBaseTime = System.nanoTime();
              }
              
              if (s.inputTs > currentBaseTime)
              {
                LogUtil.warning(LoggerType.CUSTOMER, "input tuple for operator "
                  + getOptName()
                  + "has timestamp " + s.inputTs + 
                  "ns higher than currrentBaseTime " + currentBaseTime + "ns");
              }
              else
                s.stats.addLatency(currentBaseTime - s.inputTs);
            }*/
            
            // Get the isTotalOrdering Flag from tuple value
            s.isTotalOrderGuarantee = s.inputTuple.isTotalOrderGuarantee();
            
            // ensure that the time stamp value is as per the OrderingFlag 
            if(!this.isDerivedTS)
            {
              assert s.inputTs >= s.minNextTs :
                getDebugInfo(s.inputTs, s.minNextTs, 
                             s.inputTuple.getKind(),
                             s.lastTupleKind);
              
              s.minNextTs = s.isTotalOrderGuarantee ? 
                            s.inputTs+1 : s.inputTs;
            }
            
            s.lastTupleKind = s.inputTuple.getKind();

            // Ignore heartbeats
            if (s.inputTuple.isBHeartBeat())
            {
              s.state = ExecState.S_INIT;
              exitState = false;
              break;
            }
          }
          exitState = false;
          s.state = ExecState.S_PROCESSING1;

        case S_PROCESSING1:
          // Allocate the output tuple
          if (s.inputTuple == null)
          {
            // Operator will propagate a Hbt
            s.outputTuple = null;
            s.state       = ExecState.S_OUTPUT_TUPLE;
            break;
          }
          else
          {
            s.outputTuple = tupleStorageAlloc.allocate();
                   
            if (this.isDerivedTS && !s.inputTuple.isBHeartBeat())
              s.state = ExecState.S_OUTPUT_TUPLE_DERIVED_TIMESTAMP;
            else
            {
              s.state = ExecState.S_OUTPUT_TUPLE;
              break;
            }              
          }

        case S_OUTPUT_TUPLE_DERIVED_TIMESTAMP:
          // copy tuple from input to output
          populateOutput(s);

          // evaluate derived timestamp expression
          evalContext.bind(s.outputTuple, INPUT_ROLE);
          evalContext.bind(s.timestampTuple, OUTPUT_ROLE);
          derivedTSEvaluator.eval(evalContext);

          // assign timestamp to output and update records
          ITuple tstuple = s.timestampTuple.pinTuple(IPinnable.READ);
          
          long ts = Constants.MIN_EXEC_TIME;
          if (dtsType == Datatype.BIGINT)
            ts = tstuple.lValueGet(0);
          else if (dtsType == Datatype.INT)
            ts = tstuple.iValueGet(0);
          else if(dtsType == Datatype.TIMESTAMP)
            // Remove this line #(12324170): convert timestamp type to nanos
            ts = tstuple.tValueGet(0);
          s.timestampTuple.unpinTuple();

          s.inputTs = ts;
          
          // We should have a progress of time.
          if (s.lastInputTs > s.inputTs)
          {
            throw ExecException.OutOfOrderException(this, s.lastInputTs,
                s.inputTs, s.inputTuple.toString());
          }
          
          // We should follow total ordering guarantee
          if(s.inputTs < s.minNextTs )
          {
            // throw exception if the total ordering is not satisfied in appl-
            // ication timestamped channel having total order flag value 'TRUE'
            throw new SoftExecException(
              ExecutionError.TOTAL_ORDER_NOT_OBSERVED_FOR_INPUT_TUPLE, 
              s.inputTs,
              s.minNextTs-1);
          }

          s.minNextTs = s.isTotalOrderGuarantee ? s.inputTs+1 : s.inputTs;

          // Update time
          maxTime = ts;
          s.lastInputTs = ts;
          populateOutputTS(s, ts);

          s.state = ExecState.S_OUTPUT_READY;
          break;

        case S_OUTPUT_TUPLE:
          // Populate output tuple
          populateOutput(s);
          populateOutputTS(s, s.inputTs);
          s.state = ExecState.S_OUTPUT_READY;

        case S_OUTPUT_READY:
          if (s.outputTuple == null)
          {
            if (isSystemTimestamped && (timeoutDuration != -1))
            {
              long heartbeatTime = source.getHeartbeatTime();
              // Set the flag true to release the lock of source operators
              isTimeOutHbtPropagated = true;
              if (heartbeatTime != -1)
              { // sending heartbeat
                s.outputElement.heartBeat(heartbeatTime);
                s.lastInputTs = heartbeatTime;
              } 
              else
              { // go to initial state to dequeue newly added i/p
                s.state = ExecState.S_INIT;
                break;
              }
            } 
            else
            {
              s.outputElement.heartBeat(s.lastInputTs);
            }
          } 
          else
          {
            s.outputElement.setKind(QueueElement.Kind.E_PLUS);
            s.outputTs = s.inputTs;
            s.outputElement.setTs(s.outputTs);
            s.outputElement.setTuple(s.outputTuple);            
          }          
          // set the total guarantee flag for output tuple
          s.outputElement.setTotalOrderingGuarantee(s.isTotalOrderGuarantee);
          s.state = ExecState.S_OUTPUT_ELEMENT;

        case S_OUTPUT_ELEMENT:
          if (outputQueue.isFull())
          {
            done = true;
            break;
          }
          ITuplePtr outTuplePtr = s.outputElement.getTuple();
          long snapshotId = Long.MAX_VALUE;
          if(outTuplePtr != null)
          {
            ITuple outTuple = outTuplePtr.pinTuple(IPinnable.WRITE);
            if((workerIdColNum != -1) && (txnIdColNum != -1))
            {
              long workerId = outTuple.lValueGet(workerIdColNum);
              long txnId = outTuple.lValueGet(txnIdColNum);
              snapshotId =
                execContext.getPlanMgr().findSnapshotId(workerId, txnId);
              /* Bug 14636097 and similar others.
               * The CID (workerId) and TID (txnId) are part of the tuple.
               * But mostly every event will have different values for these.
               * This creates problems when we rely on tuple.compare() while
               * processing a received minus tuple e.g. ValueRelationWindow
               * operator looks up the PriorityQueue of tuples in the window
               * on receiving a minus tuple and then deletes it from that 
               * list. Since CID and TID values could be different for plus 
               * and minus of the same event, the tuple.compare() would
               * return false.
               * To avoid such situation we would set the TID and CID values to
               * null here after their purpose of computing snapshotid is 
               * served.
               */
               outTuple.setAttrNull(workerIdColNum);
               outTuple.setAttrNull(txnIdColNum);
             }
           
             //use event identifier col value as tuple id if needed
             if(this.shouldUseEventIdVal())
             {
               assert eventIdColNum != -1 : "eventIdColNum not set in "
                                            +this.getOptName();
               outTuple.setId(outTuple.lValueGet(eventIdColNum));
             }
             outTuplePtr.unpinTuple();
           }
          
           //bug 16820093: set snapshotid for heartbeat too.
           //It could be auto heartbeat or requested heartbeat or input hb.
           //Setting snapshot id to current ensures all existing op receive it.
           //snapshotid is used only in BAM case. This is ensured by the
           //second condition.
           if((s.outputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
              && ((workerIdColNum != -1) && (txnIdColNum != -1))
             )
             snapshotId = execContext.getPlanMgr().getCurrentSnapshotId();
           
           s.outputElement.setSnapshotId(snapshotId);           
           outputQueue.enqueue(s.outputElement);      
           s.state = ExecState.S_OUTPUT_ENQUEUED;

        case S_OUTPUT_ENQUEUED:    
          if(s.outputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
            s.stats.incrNumOutputHeartbeats();
          else
            s.stats.incrNumOutputs();
          s.lastOutputTs = s.lastInputTs;
          s.state        = ExecState.S_INIT;
          exitState      = true;
          s.outputTuple  = null;
          // Reset the total ordering guarantee
          s.isTotalOrderGuarantee = false;
          break;
       
        default:
          assert false;
        }

        if (done)
          break;
      }
    } catch (CEPException e)
    {
      // CQL's logging level FINE is equivalent to Apache's DEBUG
      String errorMsg = "Dropping Tuple "+s.inputTuple+ "\nError:"+e.getMessage();
      LogUtil.severe(LoggerType.TRACE, errorMsg);
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      if (e instanceof SoftExecException)
      {
        s.state = ExecState.S_INIT;
        return 0;
      }

      throw (e);
    } finally
    {
      execMgr.getLock().readLock().unlock();
    }
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.operators.ExecOpt#deleteOp()
   */
  @Override
  public void deleteOp()
  {
    try
    {
      execMgr.removeTableSource(streamId);
      source.end();
    } catch (CEPException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      return;
    }

  }

  public long getOldestTs()
  {
    long sqlts;
    try
    {
      sqlts = source.getOldestTs();
    } catch (CEPException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      return Constants.NULL_TIMESTAMP;
    }

    if (sqlts == Constants.NULL_TIMESTAMP)
    {
      // Do I need to propaget heartbeats ?
      StreamSourceState s = (StreamSourceState) mut_state;
      if (s.lastInputTs <= s.lastOutputTs)
        return Constants.NULL_TIMESTAMP;

      ts = s.lastInputTs;
      return ts;
    }

    ts = sqlts;
    return ts;
  }

  /**
   * Add this execution operator to the scheduler.
   * 
   * @param sched
   * @throws ExecException
   */
  @Override
  public synchronized boolean addExecOp() throws ExecException
  {
    boolean added = super.addExecOp();
    if (added)
    {
      execMgr.addSourceOp(this);
    }
    return added;
  }
  
  /**Can this operator be scheduled? */
  
  public boolean canBeScheduled()
  {
    // cannot schedule if the operator is already scheduled or is executing.
    if (isScheduled.get() == true)
      return false;
    StreamSourceState s = (StreamSourceState) mut_state;
    if (isSystemTimestamped && (timeoutDuration != -1))
    {
      try
      {
        if (!(source.hasNext()))
        {
          //Assumption: Input Tuple time-stamp in file source is of millisecond
          //time unit and our system's granularity is nanosecond unit of time          
          long diff  = System.nanoTime() - s.lastInputTs;
          if (diff < timeoutDuration)
            return false;
        }
      }
      catch(Exception e){}
      return true;
    }
    else
    {
      try
      {
        // check if file has some tuples left or any hb needs to be propagated
        return (source.hasNext()||(s.lastInputTs > s.lastOutputTs));
      }
      catch (CEPException e){}
      return true;
    }
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<StreamSource id=\"" + id + "\" numAttrs=\"" + numAttrs +
              "\">");
    sb.append("<Attrs>");
    for (int i = 0; i < numAttrs; i++)
    {
      Datatype type = attrSpecs.getAttrType(i);
      int len = attrSpecs.getAttrLen(i);
      int col = outCols[i].getColnum();
      sb.append("<Attr type=\"" + type + "\" len=\"" + len + "\" outcol=\""
          + col + "\"/>");
    }
    sb.append("</Attrs>");
    sb.append("<OutputQueue>" + outputQueue.toString() + "</OutputQueue>");
    sb.append("<TupleAlloc>" + tupleStorageAlloc.toString() + "</TupleAlloc>");
    sb.append("</StreamSource>");
    return sb.toString();
  }

  public IAEval getDerivedTSEvaluator()
  {
    return derivedTSEvaluator;
  }

  public void setDerivedTSEvaluator(IAEval derivedTSEvaluator)
  {
    this.derivedTSEvaluator = derivedTSEvaluator;
  }

  public Datatype getDtsType()
  {
    return dtsType;
  }

  public void setDtsType(Datatype dtsType)
  {
    this.dtsType = dtsType;
  }

  public boolean requiresHbtTimeout()
  {
    return timeoutDuration != -1;
  }

  public int run(TupleValue input) throws CEPException
  {
    MutableState state = resetMutableState();
    int i = run(input, state);
    commitMutableState(state);
    
    return i; 
  }
  
  /**
   * Concurrent stream sources must override this method.
   * 
   * The reason we need a mutableState is to be able to aggregate the statistics.
   * 
   * @param inputValue
   * @param state
   * @return
   * @throws CEPException
   */
  public int run(TupleValue inputValue, MutableState state) throws CEPException 
  {
    return 0;
  }

  public void setTsColNum(int tsCol)
  {
    this.tsColNum = tsCol;
  }
  
  public void setTsColType(Datatype type)
  {
    this.tsColType = type;
  }

  public void setIsReplayRange(boolean replayRange)
  {
    this.isReplayRange = replayRange;
  }
  
  public void setAscArchiverTupleComparator(ArchiverTupleComparator ascComp)
  {
    this.ascComparator = ascComp;
  }
  
  public void setDescArchiverTupleComparator(ArchiverTupleComparator descComp)
  {
    this.descComparator = descComp;
  }
  
  public void setNumRows(int numRows)
  {
    this.             numRows  = numRows;
  }
  
  public void initializeState() throws CEPException
  {
    List<ITuplePtr> tempList = null;
    
    SQLType targetSQLType = 
      execContext.getServiceManager().getConfigMgr().getTargetSQLType();
    
    if((!isReplayRange) && (targetSQLType == SQLType.BI))
    {
      //In rows case for BI we do not get the top numRows,
      //instead we get back all the records.
      //So we should first sort in descending order, then get only
      //the top numRows 
      if(numRows > archivedRelationTuples.size())
        numRows = archivedRelationTuples.size();
      
      Collections.sort(archivedRelationTuples, descComparator);

      tempList = new LinkedList<ITuplePtr>();

      for(int i=0; i<numRows; i++)
      {
        tempList.add(0,archivedRelationTuples.get(i));
      }
      archivedRelationTuples.clear();
      if(tempList.size() > 0)
        archivedRelationTuples = tempList;
    }
    
    //sort in ascending order of timestamp
    Collections.sort(archivedRelationTuples, ascComparator);

    if(archivedRelationTuples != null)
    {
      StreamSourceState s = (StreamSourceState) mut_state;
      for(ITuplePtr currentTuple : archivedRelationTuples)
      {
        ITuple r = currentTuple.pinTuple(IPinnable.READ);
        assert tsColType != null : "timestamp column type not set in execopt!";
        assert tsColNum != -1 : "timestamp column position not set in execopt";
        if(r.isAttrNull(tsColNum))
        { //timestamp column is null!
          LogUtil.warning(LoggerType.TRACE, "ARF# "+
                       this.getOptName()+" : Excluding archiver resultset " +
                       "tuple "+r+" as timestamp column in it has null value");
          continue;
        }
          
        if(tsColType == Datatype.BIGINT)
          s.inputTs = r.lValueGet(tsColNum);
        else if (tsColType == Datatype.TIMESTAMP)
          //the value would be in nanos already
          s.inputTs = r.tValueGet(tsColNum);
        else
          assert false; //should never come here
        s.outputElement.setTuple(currentTuple);
        s.outputElement.setTs(s.inputTs);    
        s.outputElement.setKind(QueueElement.Kind.E_PLUS);
        s.outputElement.setTotalOrderingGuarantee(false);
        s.outputElement.setSnapshotId(execContext.getPlanMgr().getCurrentSnapshotId());
        s.lastOutputTs = s.inputTs;

        ((ISharedQueueWriter)outputQueue).enqueue(s.outputElement, 
                                                  this.getArchiverReaders());   
        /*
        LogUtil.finest(LoggerType.TRACE, "ARF# "+ 
                     this.getOptName()+
                     " propagated archiver tuple: "+currentTuple);
        */
        s.stats.incrNumOutputs();
        currentTuple.unpinTuple();   
 
      }
      
      //send heartbeat with ordering guarantee false
      s.lastOutputTs = s.lastOutputTs + 1;
      s.outputElement.heartBeat(s.lastOutputTs);
      s.outputElement.setTotalOrderingGuarantee(false);
      s.outputElement.setKind(QueueElement.Kind.E_HEARTBEAT);
      s.outputElement.setSnapshotId(execContext.getPlanMgr().getCurrentSnapshotId());
      ((ISharedQueueWriter) outputQueue).enqueue(s.outputElement, 
                                                 this.getArchiverReaders());
      LogUtil.finer(LoggerType.TRACE, "ARF# "+
                   this.getOptName()+
                   " sent heartbeat of "+s.lastOutputTs+ " with ordering guarantee false");
      
      s.stats.incrNumOutputs();

      LogUtil.finer(LoggerType.TRACE, "ARF# "+
                   "Initialized state of "+this.getOptName()+
                   " and propagated events received from archiver downstream");
      
      //remove all the archived relation tuples.
      archivedRelationTuples.clear();

    }
  }
  
  /**
   * Create snapshot of Stream Source operator by writing the operator state
   * into param java output stream.
   * State of Stream Source operator consists of following:
   * 1. Mutable State
   * 
   * Please note that we will write the state of operator in above sequence, so
   * the loadSnapshot should also read the state in the same sequence.
   * @param output
   * @throws IOException 
   */
  protected void createSnapshot(ObjectOutputStream output) throws CEPException
  {   
    try
    {
      // Write Mutable state to output stream
      output.writeObject((StreamSourceState)mut_state);
      
      if (SnapshotContext.getVersion() >= SnapshotContext.SOURCEOP_TUPID_VERSION)
      {
        // Save nextTupleId so that post recovery the new tuples will
        // start using this tuple id onwards      
        output.writeLong(Tuple.getNextTupleId());
      }
    } 
    catch (IOException e)
    {
      throw new CEPException(ExecutionError.SNAPSHOT_CREATION_ERROR, e, e.getLocalizedMessage());
    }
  }
  
  protected void loadSnapshot(ObjectInputStream input) throws ExecException
  {
    try
    {
      // Read MutableState from input stream
      StreamSourceState loaded_mutable_state = (StreamSourceState) input.readObject();
      ((StreamSourceState)mut_state).copyFrom(loaded_mutable_state);
      
      if (SnapshotContext.getVersion() >= SnapshotContext.SOURCEOP_TUPID_VERSION)
      {
        // Load next tuple id and set this in operator
        long loaded_nextTupleId = input.readLong(); 
        this.setMinTupleId(loaded_nextTupleId);
        
        // Mark operator to restore to a tuple id higher than loaded tuple id
        // to prevent allocating a tuple with older tuple ids.
        this.setRestoreTupleId(true);
        Tuple.setNextTupleId(loaded_nextTupleId);
      }
    } 
    catch (ClassNotFoundException e)
    {
      throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR_CNF, e, e.getLocalizedMessage());
    } 
    catch (IOException e)
    {
      throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR,e,e.getLocalizedMessage());
    }
  }
}
