/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ExecOpt.java hopark_cqlsnapshot/2 2016/02/26 10:21:32 hopark Exp $ */

/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/
/*
 DESCRIPTION
 Execution-time operator
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 sbishnoi    12/21/15 - adding support for ha snapshot generation
 hopark      12/15/15 - add snapshot api
 sbishnoi    07/10/13 - saving statistics in stat variable
 udeshmuk    04/28/13 - removing logging statement for archived SIA case - not
                        sending hb
 vikshukl    02/18/13 - add heartbeat info
 udeshmuk    01/24/13 - use archived_sia_started flag to decided whether to
                        send hb request or not
 vikshukl    10/10/12 - archived dimension flag
 sbishnoi    10/09/12 - XbranchMerge
                        sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0_11.1.1.7.0 from
                        st_pcbpel_11.1.1.4.0
 sbishnoi    10/08/12 - XbranchMerge sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0
                        from st_pcbpel_pt-11.1.1.7.0
 vikshukl    10/02/12 - differentiate between snapshot and streaming data
 sbishnoi    09/14/12 - initializing stats enabled check with config parameter
 udeshmuk    09/10/12 - eventid column position
 udeshmuk    05/28/12 - set archiver readers
 udeshmuk    05/22/12 - make getInputNo public
 alealves    12/20/11 - XbranchMerge alealves_bug-12873645_cep from main
 alealves    08/18/11 - XbranchMerge alealves_bug-12888416_cep from main
 anasrini    08/10/11 - check isStatsEnabled
 udeshmuk    06/28/11 - support for archived reln
 udeshmuk    04/18/11 - archived relation support
 anasrini    04/05/11 - support run(QueueElement, QueueReaderContext)
 sborah      06/12/09 - Memory Optimization
 parujain    05/29/09 - maintain nextId in ExecManager
 sbishnoi    05/15/09 - keeping unique list of sysTs source lineage
 parujain    05/08/09 - lifecycle mgmt
 sbishnoi    05/11/09 - commenting the println statements
 anasrini    05/07/09 - system timestamped source lineage
 sbishnoi    04/20/09 - handling runtime exception
 sbishnoi    04/13/09 - adding getInfo
 sbishnoi    03/03/09 - commenting Calendar calls in setting start and end time
 anasrini    02/13/09 - remove allowEnqueue
 anasrini    01/28/09 - remove Runnable
 sbishnoi    01/21/09 - fix scheduling problem related to BinStreamJoin
 parujain    01/08/09 - redesign stop/drop force
 udeshmuk    12/04/08 - add a new field to keep track of position in the
                        sourceOps array.
 hopark      12/02/08 - move LogLevelManager to ExecContext
 anasrini    11/08/08 - add requiresBufferedInput
 hopark      10/10/08 - remove statics
 hopark      10/09/08 - remove statics
 hopark      10/07/08 - use execContext to remove statics
 sborah      09/25/08 - update stats
 sbishnoi    06/25/08 - fix hbt propagation when timeslice is 1 and code alignment
 hopark      06/18/08 - logging refactor
 hopark      04/28/08 - fix lock
 udeshmuk    05/02/08 - shouldBeScheduled() - fix given by namit.
 najain      04/24/08 - stats
 najain      04/23/08 - cleanup
 sbishnoi    04/22/08 - remove relock
 najain      04/14/08 - add isSilentInput
 sbishnoi    03/26/08 - removing commented println statements 
 udeshmuk    03/18/08 - debugging purpose.
 hopark      02/25/08 - support paged queue
 hopark      02/06/08 - fix overlapping operator stats in dump
 hopark      01/31/08 - queue optimization
 hopark      12/26/07 - use DumperFactory
 hopark      12/07/07 - cleanup spill
 hopark      11/27/07 - add dump
 parujain    12/06/07 - reverse outer and inner
 hopark      10/18/07 - make evictable
 hopark      10/30/07 - remove IQueueElement
 hopark      10/21/07 - remove TimeStamp
 parujain    10/04/07 - delete op
 hopark      08/03/07 - structured log
 najain      07/31/07 - signal on operator compleation
 najain      07/23/07 - scheduling optimization
 najain      07/09/07 - cleanup
 najain      07/06/07 - remove valid
 parujain    07/03/07 - cleanup
 parujain    06/26/07 - mutable state
 hopark      06/20/07 - cleanup
 hopark      06/14/07 - add logging for ddls
 hopark      06/07/07 - use LogArea
 parujain    06/07/07 - lint error
 hopark      05/22/07 - debug logging
 hopark      05/16/07 - remove printStackTrace
 parujain    04/26/07 - stream id to get stats
 parujain    04/23/07 - runtime error handling for multithreaded
 parujain    04/09/07 - handle runtime Exceptions
 hopark      04/06/07 - fix pincount
 hopark      03/23/07 - throws exception from QueueElement
 najain      03/14/07 - cleanup
 parujain    03/16/07 - debug level
 najain      03/12/07 - bug fix
 parujain    02/13/07 - addToSched sched not instantiated
 najain      01/05/07 - spill over support
 parujain    12/19/06 - fullScanId for RelationSynopsis
 parujain    12/06/06 - propagating relation
 najain      12/04/06 - stores are not storage allocators
 hopark      11/17/06 - bug 5583899 : removed input/outputs from ExecOpt
 najain      10/17/06 - minor enhancements
 anasrini    09/22/06 - setName for execution operator, useful for debugging
 anasrini    08/24/06 - add id of PhyOpt from which instantiated
 anasrini    08/09/06 - set id as hashCode for debugging
 najain      07/31/06 - handle silent relations
 najain      07/19/06 - ref-count tuples 
 najain      07/10/06 - add inStore 
 najain      07/06/06 - move common fields in ancestor
 najain      06/10/06 - remove operator disable/enable 
 najain      06/09/06 - operator sharing ref-count 
 najain      05/31/06 - read/write locks 
 najain      03/30/06 - add add/remove ToScheduler 
 skaluska    03/26/06 - implementation
 skaluska    03/23/06 - set id correctly
 anasrini    03/24/06 - bug fix 
 skaluska    02/06/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ExecOpt.java hopark_cqlsnapshot/2 2016/02/26 10:21:32 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.operators;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.ExecManager;
import oracle.cep.execution.operators.MutableState.PropState;
import oracle.cep.execution.queues.ISharedQueueWriter;
import oracle.cep.execution.queues.Queue;
import oracle.cep.execution.queues.QueueReaderContext;
import oracle.cep.execution.queues.SharedQueueReaderStats;
import oracle.cep.execution.queues.SharedQueueWriterStats;
import oracle.cep.execution.snapshot.ICheckpointable;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.execution.synopses.ExecSynopsis;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.ILogLevelManager;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.logging.trace.LogTags;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IEvictableObj;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.planmgr.PlanManager;
import oracle.cep.serializer.ObjStreamFactory;
import oracle.cep.serializer.ObjStreamUtil;
import oracle.cep.service.ExecContext;
import oracle.cep.snapshot.SnapshotContext;

/**
 * This is the super class for all the execution operators.
 * 
 * @author skaluska
 */
@DumpDesc(attribTags={"Id", "Type", "Name"}, 
          attribVals={"getPhyOptId", "getOpttyp", "getOptName"},
          infoLevel=LogLevel.OPERATOR_INFO,
          dumpLevel=LogLevel.OPERATOR_DUMP,
          verboseDumpLevel=LogLevel.OPERATOR_DUMP_DETAIL)
public abstract class ExecOpt implements ILoggable, IEvictableObj, ICheckpointable
{
  /** operator id */
  protected int                              id;
  /** position in the op_array */
  //This is required to keep track of position
  //in the op_array of ExecManager. id used at a lot of other places
  protected int                              op_arrayPos = -1;
  /**  position in sourceOps array */
  //For source operators we also need to keep track of the operator's
  //pos in the sourceOps array.
  protected int                              sourceOpsPos = -1;

  protected boolean                          valid;

  /** Id of Stream/relation maintained in metadata */
  protected int                              streamId;

  /**
   * Id of the physical operator from which this is instantiated This is very
   * useful for debugging as it provides a starting point to identify this
   * operator in the global plan
   */
  protected int                              phyOptId;

  /** True if the operator will produce a stream */
  protected boolean                          isStream;

  /** The name of the execution operator. Useful for debugging */
  protected String                           optName;

  /** operator type */
  public ExecOptType                         opttyp;

  /** Mutable state for every operator */
  @DumpDesc(ignore=true)
  protected MutableState                     mut_state;

  /** operator statistics */
  protected ExecStats                        stats;

  /** input queue */
  @DumpDesc(ignore=true)
  protected Queue                            inputQueue;

  /** The output queue */
  @DumpDesc(ignore=true)
  protected Queue                            outputQueue;

  /** The list of readersIds need to be propagated */
  protected ArrayList<Integer>               propReaders;

  /**
   * List of readerIds added when already propagation is going on Takes care of
   * concurrency within propagation state. It is required as a result of race
   * condition i.e. if propReaders have already started getting processed and in
   * between new Readers get propagated. They should get all the tuples.
   */
  protected ArrayList<Integer>               newReaders;

  /** Storage allocator for the tuples */
  protected IAllocator<ITuplePtr>            tupleStorageAlloc;

  /** Storeage alloc who allocs the input tuples */
  protected IAllocator<ITuplePtr>            inTupleStorageAlloc;

  /** Propagation Full Scan identifier */
  protected int                              propScanId;

  @DumpDesc(ignore=true)
  protected ExecSynopsis                     outSynopsis;

  protected boolean                          isStatsEnabled;

  protected int                              tsSlice;

  @DumpDesc(ignore=true)
  protected QueueElement                     peekBuf1;
  @DumpDesc(ignore=true)
  protected QueueElement                     peekBuf2;
  
  @DumpDesc(ignore=true)
  protected ExecContext                      execContext;
  
  @DumpDesc(ignore=true)
  protected ExecManager                      execMgr;
    
  @DumpDesc(ignore=true)
  protected FactoryManager                   factoryMgr;

  protected boolean                          requiresBufferedInput;
      
  /**
   * A boolean variable that is true when operator is scheduled for execution
   * or is currently executing, false otherwise.
   */
  public AtomicBoolean                       isScheduled;

  /** The execution task to run this execution operator */
  public ExecOptTask                         execOpTask;

  /**
   * The set of system timestamped base (not view) sources 
   * (streams and non-external relations that are part of the lineage of this
   * operator.
   *
   * This is maintained so that in the case of binary operators, when one
   * input (say outer) is waiting for progress of time on the 
   * other side (inner), then a request is made (via the scheduler) to the 
   * system timestamped sources in the lineage of the inner input to send
   * "as soon as possible" a heartbeat or an event at the desired time 
   * (time of waiting outer input + 1)
   *
   * Currrently this optimization works only in the DirectInterop mode
   */
  protected Set<ExecOpt>         systsSourceLineage;

  /**
   * For binary operators, this is systsSourceLineage of the left (outer)
   * input.
   */
  protected Set<ExecOpt>         outerSystsSourceLineage;

  /**
   * For binary operators, this is systsSourceLineage of the right (inner)
   * input.
   */
  protected Set<ExecOpt>         innerSystsSourceLineage;

  /**
   * For binary operators, this is complete systsSourceLineage of the 
   * left (outer) input.
   */
  protected Set<ExecOpt>         allOuterSystsSourceLineage;

  /**
   * For binary operators, this is complete systsSourceLineage of the 
   * right (inner) input.
   */
  protected Set<ExecOpt>         allInnerSystsSourceLineage;

  /**
   * fullSourceLineage is collection of all the source objects of this operator
   * which includes all system timestamped and application timestamped sources
   * fullSourceLineage is SUPERSET of systsSourceLineage
   */
  protected Set<ExecOpt>         fullSourceLineage;
  
  /**
   * Collection of tuples returned by Archiver
   */
  protected List<ITuplePtr>      archivedRelationTuples = null;
  
  /**
   * ReaderIds on which the archiver tuples will be sent. This is because
   * when queries are shared we don't want the archiver query results to
   * propagate on all outputs, but only to those that are in the CQL query
   * plan for which the archiver query was executed.
   */
  protected BitSet               archiverReaders;
  
  protected long                 snapShotTime;   
  
  protected long                 heartbeatTime;

  /** Used only for archived relation.
   *  Indicates the position of event identifier column in the schema */   
  protected int                  eventIdColNum = -1;
               
  /** Used only for archived relation/stream
   * If set, we will copy over the value in the column indicated by 
   * eventIdColNum and set it as tuple id.
   * This will be set if the operator is a view root or a source
   */
  protected boolean              useEventIdVal = false;
  
  /**
   * Used only for archived relations and streams.
   * This flag represents the fact the upstream operators upto
   * this operator are based on archived dimension
   */
  protected boolean              isArchivedDim = false;

  protected ObjectOutputStream   journalStream = null;
  protected ByteArrayOutputStream byteJournalStream = null;

  // Pending byte array of state snapshot which should be applied prior to process
  // next tuple
  /** Snapshot bytes for a full snapshot which is not yet applied to operator*/
  protected byte[] pendingSnapshotBytes;
  /** Snapshot version for a full snapshot which is not yet applied to operator*/
  protected double pendingSnapshotVersion;
  
  /** Snapshot bytes for a partial full snapshot which is not yet applied to operator*/
  protected byte[] pendingPartialSnapshotBytes;
  /** Snapshot version for a partial full snapshot which is not yet applied to operator*/
  protected double pendingPartialSnapshotVersion;
  
  /** A Flag to indicate whether an operator should process silent relation*/
  protected boolean enableSilentRelnProcessing = false;
  
  /**
   * Constructor for ExecOpt
   * 
   * @param typ
   *          The operator type
   * @param m
   *          TODO
   * @param ec TODO
   */
  ExecOpt(ExecOptType typ, MutableState m, ExecContext ec)
  {
    opttyp = typ;
    mut_state = m;
    stats = new ExecStats();
    id = ec.getExecMgr().getNextExecOptId();
    op_arrayPos = -1;
    sourceOpsPos = -1;
    propReaders = new ArrayList<Integer>();
    newReaders = new ArrayList<Integer>();
    propScanId = -1;
    outSynopsis = null;
    isStream = false;
    streamId = -1;
    peekBuf1 = m.allocQueueElement();
    peekBuf2 = m.allocQueueElement();
    requiresBufferedInput = false;
    valid = true;
    isScheduled = new AtomicBoolean(); //initial value will be false
    execContext = ec;
    execMgr = ec.getExecMgr();
    factoryMgr = ec.getServiceManager().getFactoryManager();
    LogLevelManager.trace(LogArea.OPERATOR, LogEvent.OPERATOR_DDL, this, "new");
   
    // Check if the run time operator stats are enabled/disabled by default
    setIsStatsEnabled(ec.getExecStatsMgr().isRunTimeOperatorStatsEnabled());
  }

  /**
   * Initialize state of the operator using results returned by archiver
   */
  public void initializeState() throws CEPException
  {
    //default implementation is empty.
    //Operator will override as needed.
  }
  
  public int getEventIdColNum()
  {
    return this.eventIdColNum;
  }
  
  public void setEventIdColNum(int nm)
  {
    this.eventIdColNum = nm;
  }
  
  public boolean shouldUseEventIdVal()
  {
    return this.useEventIdVal;
  }
  
  public void setUseEventIdVal(boolean val)
  {
    this.useEventIdVal = val;
  }
  
  /**
   * Getter for id in ExecOpt
   * 
   * @return Returns the id
   */
  public int getId()
  {
    return id;
  }

  public int getOpArrayPos()
  {
    return this.op_arrayPos;
  }

  public void setOpArrayPos(int pos)
  {
    this.op_arrayPos = pos; 
  }
  
  public int getSourcOpsPos()
  {
    return this.sourceOpsPos;
  }
  
  public void setSourceOpsPos(int pos)
  {
    this.sourceOpsPos = pos;
  }
  /**
   * @param synopsis
   *          The synopsis to set.
   */
  public void setExecSynopsis(ExecSynopsis synopsis)
  {
    LogLevelManager.trace(LogArea.OPERATOR, LogEvent.OPERATOR_DDL, this,
        "setExecSynopsis", synopsis);
    this.outSynopsis = synopsis;
  }

  public void setValid(boolean valid)
  {
    this.valid = valid;
  }

  public boolean getValid()
  {
    return this.valid;
  }

  /**
   * Getter for Stream id in metadata
   * 
   * @return Returns the stream id
   */
  public int getStreamId()
  {
    return streamId;
  }

  /**
   * Setter for Stream id in ExecOpt
   * 
   * @param id
   *          The id to set.
   */
  public void setStreamId(int id)
  {
    this.streamId = id;
  }

  /**
   * Setter for timeslice for which operator will run
   * 
   * @param tsSlice
   *          Timeslice
   */
  public void setTsSlice(int tsSlice)
  {
    this.tsSlice = tsSlice;
  }

  /**
   * Setter for Stream flag
   * 
   * @param isstrm
   *          Whether produces a stream or not
   */
  public void setIsStream(boolean isstrm)
  {
    this.isStream = isstrm;
  }

  /**
   * Getter of whether stream will be produced or not
   * 
   * @return Returns whether stream will be produced or not
   */
  public boolean getIsStream()
  {
    return this.isStream;
  }

  /**
   * Setter for IsStatsEnabled flag Used while measuring the latency for
   * strm/reln/output operators
   * 
   * @param isenabled
   *          Whether enabled or not
   */
  public void setIsStatsEnabled(boolean isenabled)
  {
    this.isStatsEnabled = isenabled;
    stats.setIsEnabled(isenabled);
    // if disabled after disabling then latency needs to be reset
    if (!isenabled)
      stats.clearLatency();
  }

  /**
   * Getter of whether Stats for measuring latency enabled or not
   * 
   * @return Returns whether stats will be enabled or not
   */
  public boolean getIsStatsEnabled()
  {
    return this.isStatsEnabled;
  }

  /**
   * @return Returns the phyOptId.
   */
  public int getPhyOptId()
  {
    return phyOptId;
  }

  /**
   * Setter for physical operator id in ExecOpt
   * 
   * @param phyOptId
   *          The id of the corresponding physical operator to set.
   */
  public void setPhyOptId(int phyOptId)
  {
    this.phyOptId = phyOptId;
  }

  /**
   * @return Returns the optName.
   */
  public String getOptName()
  {
    return optName;
  }

  /**
   * @return Returns the opttyp.
   */
  public ExecOptType getOpttyp()
  {
    return opttyp;
  }

  /**
   * Setter for execution operator name in ExecOpt
   * 
   * @param name
   *          The name of this execution operator
   */
  public void setOptName(String optName)
  {
    this.optName = optName;
  }

  public long getMaxTime()
  {
    assert false;
    return 0;
  }

  /**
   * Setter for intupleStorageAlloc
   * 
   * @param intupleStorageAlloc
   *          The intupleStorageAlloc to set.
   */
  public void setInTupleStorageAlloc(IAllocator<ITuplePtr> inTupleStorageAlloc)
  {
    this.inTupleStorageAlloc = inTupleStorageAlloc;
  }

  /**
   * Reset Mutable State. This is done when we start the execution of any
   * operator. This will record all the necessary information for an operator.
   *
   * CONCURRENCY SUPPORT:
   * Generally speaking, the stats are cleared in both concurrent and single-threaded case.
   * In the concurrent case, we have a new state for each run, whereas for the latter case
   * we keep a single state for each operator instance.
   * 
   */
  public MutableState resetMutableState()
  {
    mut_state.reset();

    // Reason:
    //   Calendar.getTimeInMillis() call is very expensive in terms of time
    //   as it is taking almost 30 % of total operator execution time;
    //   For each execution of operator; this function will be called twice,
    //   (to set start time and end time)
    // TODO:
    //   Need to apply better solution
    //mut_state.stats.setStartTime(Calendar.getInstance().getTimeInMillis());
    
    return mut_state;
  }

  /**
   * Getter for inputQueue in IStream and DStream
   * 
   * @return Returns the inputQueue
   */
  public Queue getInputQueue()
  {
    return inputQueue;
  }

  /**
   * Setter for inputQueue in IStream and DStream
   * 
   * @param inputQueue
   *          The inputQueue to set.
   */
  public void setInputQueue(Queue inputQueue)
  {
    LogLevelManager.trace(LogArea.OPERATOR, LogEvent.OPERATOR_DDL, this,
        "setInputQueue", inputQueue);
    this.inputQueue = inputQueue;
  }

  protected int getNoInputQueues()
  {
    return 1;
  }

  public Queue getInputQueue(int n)
  {
    if (n < 1)
      return inputQueue;
    return null;
  }

  protected long getLastTs(int n)
  {
    assert false : "shouldn't be called";
    return -1;
  }

  protected boolean isSilentInput(int n)
  {
    assert false : "shouldn't be called";
    return false;
  }

  /**
   * This method returns ture if and only if this operator requires
   * its input(s) to be buffered completely.
   *
   * If this returns false, then this operator requires only the
   * latest input element to be stored, since it guarantees to consume
   * the input element each time it is called. Further, with direct interop,
   * since each enqueue will result in the operator being invoked immediately
   * (before the next input is enqueued), this suffices
   *
   * On the other hand, there are some operators, like binary operators,
   * that cannot guarantee to consume an input element each time it is called.
   * This can happen since binary operators are required to consume elements
   * across both their inputs in non-decreasing timestamp order. Since the
   * two inputs are independent, and if their speed varies, one may be ahead
   * of the other. Thus, this requires input buffering.
   */
  public boolean requiresBufferedInput()
  {
    return requiresBufferedInput;
  }

  public void setRequiresBufferedInput(boolean b) 
  {
    this.requiresBufferedInput = b;
  }

  public void setSystsSourceLineage(Set<ExecOpt> srcLineage)
  {
    this.systsSourceLineage = srcLineage;
    printLineageList(systsSourceLineage, "SYSTS_");
  }

  public void setAllOuterSystsSourceLineage(Set<ExecOpt> outerSrcLineage)
  {
    this.allOuterSystsSourceLineage = outerSrcLineage;
    printLineageList(outerSystsSourceLineage, "OUTER_");
  }

  public void setAllInnerSystsSourceLineage(Set<ExecOpt> innerSrcLineage)
  {
    this.allInnerSystsSourceLineage = innerSrcLineage;    
    printLineageList(innerSystsSourceLineage, "INNER_");
  }
  
  public void setFullSourceLineage(Set<ExecOpt> srcLineage)
  {
    this.fullSourceLineage = srcLineage;
    printLineageList(fullSourceLineage, "FULL_");
  }
  
  /**
   * Computes the effective set of lineage of system timestamped sources
   * for binary operators
   * Method will computer two Set objects 
   * 1) outerSystsSourceLineage and 2) innerSystsSourceLineage
   * in the following way:
   * outerSystsSourceLineage = allOuterSystsSourceLineage MINUS allInnerSystsSourceLineage
   * innerSystsSourceLineage = allInnerSystsSourceLineage MINUS allOuterSystsSourceLineage
   */
  public void computeEffectiveSystsSourceLineage()
  {
    // If all inputs to this operator doesn't come from system timestamp stream
    //  then return without initializing final inner and outer lineages 
    if(this.systsSourceLineage == null || this.fullSourceLineage == null)
      return;
    
    if(this.systsSourceLineage.size() != this.fullSourceLineage.size())
      return;
    
       
    if(this.allInnerSystsSourceLineage != null)
    {
      if(this.allInnerSystsSourceLineage.size() > 0)
      {
        // lazy initialization
        if(this.innerSystsSourceLineage == null)
          this.innerSystsSourceLineage = new HashSet<ExecOpt>();
        
        this.innerSystsSourceLineage.addAll(this.allInnerSystsSourceLineage);
      }
    }
    
    if(this.allOuterSystsSourceLineage!= null)
    {
      if(this.allOuterSystsSourceLineage.size() > 0)
      {
        // lazy initialization
        if(this.outerSystsSourceLineage == null)
          this.outerSystsSourceLineage = new HashSet<ExecOpt>();
        
        this.outerSystsSourceLineage.addAll(this.allOuterSystsSourceLineage);
      }
    }
        
    try
    {   
      if(this.innerSystsSourceLineage != null && 
         this.allOuterSystsSourceLineage != null )
        this.innerSystsSourceLineage.removeAll(this.allOuterSystsSourceLineage);
      
      if(this.outerSystsSourceLineage != null && 
         this.allInnerSystsSourceLineage != null)
        this.outerSystsSourceLineage.removeAll(this.allInnerSystsSourceLineage);
    }
    catch(Exception e)
    {
      if(this.innerSystsSourceLineage != null)
        this.innerSystsSourceLineage.clear();
      
      if(this.outerSystsSourceLineage != null)
        this.outerSystsSourceLineage.clear();
    }
  }

  private void printLineageList(Collection<ExecOpt> lineageList, String prefix)
  {
    /*
    StringBuffer sbuff = new StringBuffer();
    sbuff.append(prefix + "SYSTS_LINEAGE(" + optName + ") = {");

    Iterator<ExecOpt> iter = lineageList.iterator();
    while(iter.hasNext())
    {
      sbuff.append(iter.next().getOptName() + ", ");
    }
    
    sbuff.append("}");
    System.out.println(sbuff.toString());
    */
  }

  /**
   * Request all the source operators to send a heartBeat tuple
   * of timestamp equal to param hbtTime
   * @param sourceList
   * @param side 0 for OUTER and 1 for INNER
   * @param hbtTime
   */
  protected void requestForHeartbeat(Collection<ExecOpt> sourceList, int side, 
                                     long hbtTime)
  { 
    /**
    System.out.println(getOptName() + ": Request for hbt on " + 
                       ((side == 0) ? "outer" : "inner") + 
                       " side at " + hbtTime + " on " 
                       + Thread.currentThread().getName());
    */
    if(!(mut_state.propState == PropState.S_ARCHIVED_SIA_STARTED))
      execContext.getSchedMgr().requestForHeartbeat(sourceList, hbtTime);
  }

  public long getOldestTs()
  {
      if (getNoInputQueues() == 1)
      {
        QueueElement e = inputQueue.peek(peekBuf1);
        if (e == null)
          return -1;
        long ts = e.getTs();
        return ts;
      }
      Queue queue1 = getInputQueue(0);
      assert (queue1 != null);
      Queue queue2 = getInputQueue(1);
      assert (queue2 != null);
      QueueElement elem1 = queue1.peek(peekBuf1);
      QueueElement elem2 = queue2.peek(peekBuf2);

      long ts = -1;
      if (elem1 == null)
      {
        if (elem2 != null)
        {
          ts = elem2.getTs();
        }
      }
      else
      {
        if (elem2 == null)
        {
          ts = elem1.getTs();
        }
        else
        {
          if (elem1.getTs() < elem2.getTs())
            ts = elem1.getTs();
          else
            ts = elem2.getTs();
        }
      }
      return ts;
  }

  protected boolean isHeartbeatPending() {
    return mut_state.lastOutputTs < mut_state.lastInputTs;
  }
  
  public synchronized boolean canBeScheduled()
  {
      //cannot schedule an operator if it is already scheduled or is
      //already executing.
      if (this.isScheduled.get() == true)
        return false;
      
      boolean ret = true;
      
      if (getNoInputQueues() == 1)
      {
        QueueElement e = inputQueue.peek(peekBuf1);
        if (e == null && !isHeartbeatPending())
          ret = false;
        return ret;
      }
      // By definition 0 corresponds to outer i.e. left
      Queue queue1 = getInputQueue(Constants.OUTER);
      assert (queue1 != null);
      // 1 corresponds to inner i.e. right
      Queue queue2 = getInputQueue(Constants.INNER);
      assert (queue2 != null);

      QueueElement outerEl = queue1.peek(peekBuf1);
      QueueElement innerEl = queue2.peek(peekBuf2);
       
      // If Both Input Queues are non empty; then
      // Opeator can be scheduled
      if ((innerEl != null) && (outerEl != null)) 
      {
        return true;
      }

      // If Both Input queues are empty; then
      //  EITHER return true if any heartbeat is pending
      //  OR     return false if no heartbeat pending;
      if ((innerEl == null) && (outerEl == null))
      {
        if(isHeartbeatPending())
          return true;
        else
          return false;
      }

      // If One Input Queue is empty and Other Input queue is non-empty then
      // First Check if Any Heartbeat pending;
      //  return true if any heartbeat is pending;
      //  else do further processing
      if(isHeartbeatPending())
        return true;
      
      long lastOuterTs = getLastTs(Constants.OUTER);
      long lastInnerTs = getLastTs(Constants.INNER);

      // If outer input queue(left) is empty; then
      //  if outer input queue contains tuples of a silent relation;
      //     return true;
      //  else if timestamp of last outer tuple is less than current inner tuple
      //     return false;
      if (outerEl == null)
      {
        if (isSilentInput(Constants.OUTER))
          ret = true;
        else if (lastOuterTs < innerEl.getTs())
          ret = false;
      }

      // If inner input queue(right) is empty; then
      //  if inner input queue contains tuples of a silent relation;
      //    return true;
      //  else if timestamp of last inner tuple is less than current outer tuple
      //    return false;
      if (innerEl == null) 
      {
        if (isSilentInput(Constants.INNER))
          ret = true;
        else if (lastInnerTs < outerEl.getTs())
          ret = false;
        else if((this instanceof BinStreamJoin) &&
                (lastInnerTs == outerEl.getTs()) )
          ret = false;
      }
        
      return ret;
  }

  /**
   * Getter for outputQueue
   * 
   * @return Returns the outputQueue
   */
  public Queue getOutputQueue()
  {
    return outputQueue;
  }

  /**
   * Setter for outputQueue
   * 
   * @param outputQueue
   *          The outputQueue to set.
   */
  public void setOutputQueue(Queue outputQueue)
  {
    LogLevelManager.trace(LogArea.OPERATOR, LogEvent.OPERATOR_DDL, this,
        "setOutputQueue", outputQueue);
    this.outputQueue = outputQueue;
  }

  /**
   * Getter for tupleStorageAlloc
   * 
   * @return Returns the tupleStorageAlloc
   */
  public IAllocator getTupleStorageAlloc()
  {
    return tupleStorageAlloc;
  }

  /**
   * Setter for tupleStorageAlloc
   * 
   * @param storeAlloc
   *          The tupleStorageAlloc to set.
   */
  public void setTupleStorageAlloc(IAllocator<ITuplePtr> storeAlloc)
  {
    this.tupleStorageAlloc = storeAlloc;
  }

  /**
   * Getter for operator statistics
   * 
   * @return Operator stats
   */
  public ExecStats getStats()
  {
    return stats;
  }

  /**
   * Run operator for the specified time.
   * 
   * @param timeslice
   *          The timeslice to run
   * @return Status
   */
  abstract protected int run(int timeslice) throws CEPException;
  
  /**
   * Specific operators that need to use this QueueReaderContext should
   * directly override this method. (Example EXCHANGE)
   *
   * Default behaviour is to just ignore the readerCtx

   */
  public int run(int timeSlice, QueueElement input, 
                 QueueReaderContext readerCtx)
    throws CEPException 
  {
    return run(timeSlice, input);
  }


  /**
   * OVERRIDE FOR MULTI THREADING
   * 
   * When not overriden, it acquires lock on self to simulate 
   * single-threaded behavior.
   */
  public int run(int timeSlice, QueueElement input)
    throws CEPException 
  {
    synchronized (this) 
    {
      // Setups mut_state
      resetMutableState();
      loadPendingSnapshot();
      int ret = run(timeSlice);
      
      // Now commit it. 
      commitMutableState(mut_state);

      return ret;
    }
  }

  public boolean shouldBeScheduled()
  {
    if (outputQueue == null)
      return true;
    boolean ret = false;
    ISharedQueueWriter oQueue = (ISharedQueueWriter)outputQueue;
    SharedQueueWriterStats stats = (SharedQueueWriterStats) oQueue.getStats();
    BitSet activeRdrs = oQueue.getReaders();
    int i = activeRdrs.nextSetBit(0);
    while (i >= 0)
    {
      SharedQueueReaderStats rdrStats = (SharedQueueReaderStats) oQueue.getReaderStats(i);
      int numPresent = 
        stats.getTotalNumElements() - rdrStats.getTotalNumElements() - 
        rdrStats.getInitElements() - rdrStats.getNumOthers();
      if (numPresent < (Constants.MIN_QUEUESZ * tsSlice)) 
        ret = true;
      else if (numPresent >= (Constants.MAX_QUEUESZ * tsSlice))
        return false;
      i = activeRdrs.nextSetBit(i + 1);
    }
    return ret;
  }

  /**
   * Get the execution task for this operator
   * @return the execution task for this operator
   */
  public ExecOptTask getExecOpTask()
  {
    if (execOpTask == null)
    {
      execOpTask = new ExecOptTask(this, execContext);
    }
    return execOpTask;
  }
  

  public void run(QueueElement input, QueueReaderContext readerCtx) 
    throws ExecException
  {
    ExecException ee = null;

    try
    {
      // Run only if the operator is valid
      if (valid)
      {
        LogLevelManager.trace(LogArea.OPERATOR, LogEvent.OPERATOR_RUN_BEGIN,
                              this, getOptName());
        
        run(tsSlice, input, readerCtx);

        LogLevelManager.trace(LogArea.OPERATOR, LogEvent.OPERATOR_RUN_END,
                              this, getOptName());
      }
    }
    catch (Exception e)
    {
      // FIXME do we need to make this atomic?
      valid = false;
      resetMutableState();
      
      if (!(e instanceof ExecException))
      {
        ee    = new ExecException(ExecutionError.GENERIC_ERROR, e);
        ee.op = this;
        
        if (LogUtil.isWarningEnabled(LoggerType.TRACE))
        {
          StringWriter stackTrace = new StringWriter();
          PrintWriter stackTraceWriter = new PrintWriter(stackTrace);
          
          e.printStackTrace(stackTraceWriter);
          LogUtil.warning(LoggerType.TRACE, "Runtime exception stack: " + 
              stackTrace.toString());
        }
      }
      else 
      {
        ee = (ExecException)e;
        if (ee.op == null)
          ee.op = this;
        
        if (LogUtil.isFineEnabled(LoggerType.TRACE))
        {
          StringWriter stackTrace = new StringWriter();
          PrintWriter stackTraceWriter = new PrintWriter(stackTrace);
          
          e.printStackTrace(stackTraceWriter);
          LogUtil.fine(LoggerType.TRACE, "Exec exception stack: " + 
              stackTrace.toString());
        }
      }
      
      throw ee;
    }
  }

  protected void commitMutableState(MutableState state)
  {
    if (isStatsEnabled)
    {
      stats.add(state.stats);
    } 
  }

  /**
   * Run the operator
   * @throws ExecException
   */
  public synchronized void run() throws ExecException
  {
    ExecException ee = null;

    try
    {
      // Run only if the operator is valid
      if (valid)
      {
        LogLevelManager.trace(LogArea.OPERATOR, LogEvent.OPERATOR_RUN_BEGIN,
                              this, getOptName());
        
        // Reset mutable state for the operator
        resetMutableState();
        loadPendingSnapshot();
        run(tsSlice);
        commitMutableState(mut_state);
        LogLevelManager.trace(LogArea.OPERATOR, LogEvent.OPERATOR_RUN_END,
                              this, getOptName());
      }
    }
    catch (Exception e)
    {
      // FIXME do we need to make this atomic?
      valid = false;
      resetMutableState();
      
      if (!(e instanceof ExecException))
      {
        ee    = new ExecException(ExecutionError.GENERIC_ERROR, e);
        ee.op = this;
      }
      else 
      {
        ee = (ExecException)e;
        if (ee.op == null)
          ee.op = this;
      }
      
      if (LogUtil.isWarningEnabled(LoggerType.TRACE))
      {
        StringWriter stackTrace = new StringWriter();
        PrintWriter stackTraceWriter = new PrintWriter(stackTrace);
        
        e.printStackTrace(stackTraceWriter);
        LogUtil.warning(LoggerType.TRACE, "Runtime exception stack: " + 
                       stackTrace.toString());
      }
      
      throw ee;
    }
  }

  /**
   * Add this execution operator to the scheduler.
   * 
   * @param sched
   * @throws ExecException
   */
  public synchronized boolean addExecOp() throws ExecException
  {
    return execMgr.addOp(this.id, this);
  }

  /**
   * Add the readerId which needs to be propagated
   * 
   * @param readerId
   */
  public synchronized void addReader(int readerId)
  {
    LogLevelManager.trace(LogArea.OPERATOR, LogEvent.OPERATOR_DDL, this, readerId);

    if (mut_state.propState == MutableState.PropState.S_PROPAGATE_INIT)
      propReaders.add(new Integer(readerId));
    else
      newReaders.add(new Integer(readerId));
  }

  public boolean propagationReqd()
  {
    return (!(propReaders.size() == 0));
  }

  public BitSet getReaders()
  {
    if (propReaders.size() == 0)
      return null;

    BitSet dummy = new BitSet();
    Iterator<Integer> iter = propReaders.iterator();
    while (iter.hasNext())
    {
      Integer value = iter.next();
      dummy.set(value.intValue());
    }
    return dummy;
  }

  public BitSet getNewReaders()
  {
    if (newReaders.size() == 0)
      return null;

    BitSet dummy = new BitSet();
    Iterator<Integer> iter = newReaders.iterator();
    while (iter.hasNext())
    {
      Integer value = iter.next();
      dummy.set(value.intValue());
    }
    return dummy;
  }

  /**
   * Removes all the readerIds from the List
   * 
   */
  public synchronized void removeReaders()
  {
    LogLevelManager.trace(LogArea.OPERATOR, LogEvent.OPERATOR_DDL, this,
        "removeReaders");
    propReaders.clear();
  }

  /**
   * Removes all the readerIds from the newReaders List
   * 
   */
  public synchronized void removeNewReaders()
  {
    LogLevelManager.trace(LogArea.OPERATOR, LogEvent.OPERATOR_DDL, this,
        "removeNewReaders");
    newReaders.clear();
  }

  /**
   * Delete operator: this removes references from the operator to any other
   * resources.
   */
  public abstract void deleteOp();

  public void propagateOldData()
  {
    if (mut_state.state != ExecState.S_PROPAGATE_OLD_DATA)
    {
      mut_state.lastState = mut_state.state;
      mut_state.state = ExecState.S_PROPAGATE_OLD_DATA;
    }

  }
  
  public void handlePropOldData() throws ExecException
  {
    /** Used to check whether newReader list has been used or not */
    Boolean newReader = false;

    switch (mut_state.propState)
    {
      case S_PROPAGATE_INIT:
        assert (outputQueue instanceof ISharedQueueWriter);
        BitSet dummy = getReaders();

        if (dummy == null)
        { // Check the second list of readers
          dummy = getNewReaders();
          newReader = true;
        }

        if (dummy == null)
        {
          mut_state.propState = RelSourceState.PropState.S_PROPAGATE_INIT;
          mut_state.state = mut_state.lastState;
          break;
        }

        assert mut_state.readerIds.nextSetBit(0) == -1;
        mut_state.readerIds.clear();
        mut_state.readerIds.or(dummy);

        mut_state.propState = RelSourceState.PropState.S_PROP_RELN_READER_GET_SCAN;

      case S_PROP_RELN_READER_GET_SCAN:

        // Propogate messages for the new reader
        // Loop over all tuples R(t) where t is the current time ??
        // how do we obtain t -- propagate based on last input time
        mut_state.scan = outSynopsis.getScan(propScanId);
        mut_state.propState = RelSourceState.PropState.S_PROP_RELN_READER_GET_NEXT;

      case S_PROP_RELN_READER_GET_NEXT:
        mut_state.tup = mut_state.scan.getNext();
        if (mut_state.tup == null)
        {
          outSynopsis.releaseScan(propScanId, mut_state.scan);
          mut_state.readerIds.clear();

          if (newReader)
          {
            removeNewReaders();
            mut_state.propState = RelSourceState.PropState.S_PROPAGATE_INIT;
            mut_state.state = mut_state.lastState;
          }
          else
          {
            removeReaders();
            mut_state.propState = RelSourceState.PropState.S_PROPAGATE_INIT;
          }
          break;
        }
        else
          mut_state.propState = RelSourceState.PropState.S_PROP_RELN_READER_PROC_TUPLE;

      case S_PROP_RELN_READER_PROC_TUPLE:
        // Allocate element, ts and populate it (how)
        QueueElement element = mut_state.allocQueueElement();
        element.setKind(QueueElement.Kind.E_PLUS);
        element.setTuple(mut_state.tup);

        // In the absence of support for silent streams/static
        // relations, the current time is the same as last input
        // received by the relation
        element.setTs(mut_state.lastInputTs);

        ((ISharedQueueWriter) outputQueue).enqueue(element, mut_state.readerIds);
        mut_state.propState = RelSourceState.PropState.S_PROP_RELN_READER_GET_NEXT;
        break;
    }
  }

  public boolean evict()
    throws ExecException
  {
    // evict queue
    int evcount = 0;
    if (outputQueue != null) 
    {
      if (outputQueue.evict())
        evcount++;
    }
    int phyOptId = getPhyOptId();
    PlanManager pm = execContext.getPlanMgr();
    PhyOpt phyOpt = pm.getPhyOpt(phyOptId);
    if (phyOpt != null) 
    {  
      // evict synopses 
      PhySynopsis[] synopses = phyOpt.getSynopses();
      if (synopses != null)
      {
        for (PhySynopsis syn : synopses)
        {
          if (syn == null) continue;
          ExecSynopsis s = syn.getSyn();
          if (s!= null && s.evict())
            evcount++;
        }
      }
      // evict store (synopsis does not evict stores in it)
      PhyStore store = phyOpt.getStore();
      if (store != null)
      {
        ExecStore s = store.getInstStore();
        if (s!= null && s.evict())
          evcount++;
      }
    }
    return (evcount > 0);
  }
  
  public String getTargetName()
  {
    return getOptName();
  }

  public int getTargetId()
  {
    // use the same id what visualizer uses.
    return getPhyOptId();
  }

  public int getTargetType()
  {
    return getOpttyp().ordinal();
  }

  public ILogLevelManager getLogLevelManager()
  {
    return execContext.getLogLevelManager();
  }
  
  /**
   * Dump operator specific information
   */
  public synchronized void dump(IDumpContext dumper)
  {
    assert (dumper != null);

    String tag = LogUtil.beginDumpObj(dumper, this);
    LogUtil.logTagVal(dumper, LogTags.OPERATOR_STATE, mut_state);
    if (outputQueue != null) 
    {
      if (!dumper.isVerbose())
        dumper.writeln(outputQueue.getTargetName(), outputQueue.getTargetId());
      else 
      {
        outputQueue.dump(dumper);
      }
    }
    int phyOptId = getPhyOptId();
    PlanManager pm = execContext.getPlanMgr();
    PhyOpt phyOpt = pm.getPhyOpt(phyOptId);
    if (phyOpt != null) 
    {  
      // evict synopses 
      PhySynopsis[] synopses = phyOpt.getSynopses();
      if (synopses != null)
      {
        for (PhySynopsis syn : synopses)
        {
          if (syn == null) continue;
          ExecSynopsis s = syn.getSyn();
          if (s != null)
          {
            if (!dumper.isVerbose())
              dumper.writeln(s.getTargetName(), s.getTargetId());
            else 
            {
              s.dump(dumper);
            }
          }
        }
      }
      // store is dumped as part of synopsis dump
      // only write a brief info if it's not a verbose dump
      if (!dumper.isVerbose())
      {
        PhyStore store = phyOpt.getStore();
        if (store != null)
        {
          ExecStore s = store.getInstStore();
          if (s != null)
          {
            dumper.writeln(s.getTargetName(), s.getTargetId());
          }
        }
      }
    }
    //LogUtil.logTagVal(dumper, LogTags.STAT, getStats());
    LogUtil.endDumpObj(dumper, tag);
  }
  
  public void trace(IDumpContext dumper, ILogEvent event, int level, Object[] args)
  {
    if (level == LogLevel.OPERATOR_STATS)
    {
      LogUtil.logTagVal(dumper, LogTags.STAT, getStats());
    }
  }
  
  protected String getDebugInfo(long currentTime, long minNextTime,
                                Object currentInpKind, Object lastInputKind)
  {
    return "Operator:= " + getOptName() + ":"+ getOpttyp() +
           " currentTime:= " + currentTime +
           " minNextTime:= " + minNextTime +
           " currentInpKind:= " + currentInpKind +
           " lastInpKind:= " + lastInputKind;
           
  }

  public boolean isArchivedDim() 
  {
    return isArchivedDim;
  }
  
  /* This method is need to extract the heartbeat from the dimension side and
   * set it on the fact side in the special join */
  public long getHeartbeatTime()
  {
    return heartbeatTime;
  }

  public void setArchivedDim(boolean isArchivedDim) 
  {
    this.isArchivedDim = isArchivedDim;
  }

  public void setArchivedRelationTuples(List<ITuplePtr> tuples)
  {
    this.archivedRelationTuples = tuples;
  }
  
  public List<ITuplePtr> getArchivedRelationTuples()
  {
    return this.archivedRelationTuples;
  }
  
  public void setArchiverReaders(BitSet archReaders)
  {
    this.archiverReaders = archReaders;
  }
  
  public BitSet getArchiverReaders()
  {
    return this.archiverReaders;
  }
  
  public void setSnapShotTime(Long currentTime)
  {
    snapShotTime = currentTime;
  }
  
  /* For special join, enqueue the hearbeat on the "fact" side, even though it
   * does not participate in state initialization
   */
  public void enqueueHeartbeat(Long hbtTime) throws ExecException
  {
    this.heartbeatTime = hbtTime;
  }
  
  public void setPropStateToSIAStart()
  {
    mut_state.propState = PropState.S_ARCHIVED_SIA_STARTED;
  }

  public void setPropStateToSIADone()
  {
    mut_state.propState = PropState.S_ARCHIVED_SIA_DONE;
  }

  
  @Override
  public boolean usesJournaling()
  {
    return false;
  }
  
  @Override
  public boolean usesPartialJournaling()
  {
    return false;
  }

  
  /**
   * Start the batch. Typically initialized the bytearraystream for incremental snapshots for the batch
   */
  @Override
  public void startBatch(boolean fullSnapshot) throws CEPException
  {
    if ((usesJournaling() || usesPartialJournaling()) && !fullSnapshot)
    {
      byteJournalStream = new ByteArrayOutputStream();
      try 
      {
        journalStream = new ObjectOutputStream(byteJournalStream);
      } 
      catch (IOException e) 
      {
        LogUtil.logStackTrace(e);
        throw new CEPException(ExecutionError.SNAPSHOT_CREATION_ERROR, e, e.getLocalizedMessage(), this.getOptName());
      }
    }
    LogUtil.fine(LoggerType.TRACE, "Operator " + this.getOptName() + ": Batch Started.");
  }

  protected void writeToJournal(Object obj) throws CEPException
  {
    try 
    {
      journalStream.writeObject(obj);
    } 
    catch (IOException e) 
    {
      LogUtil.logStackTrace(e);
      throw new CEPException(ExecutionError.SNAPSHOT_CREATION_ERROR, e, e.getLocalizedMessage(), this.getOptName());
    }
  }
  
  @Override
  public void endBatch() throws CEPException
  {
    if (journalStream != null)
    {
      try 
      {
        journalStream.flush();
        journalStream.close();
        //byteJournalStream.close();
      } 
      catch (IOException e) 
      {
        LogUtil.logStackTrace(e);
        throw new CEPException(ExecutionError.SNAPSHOT_CREATION_ERROR, e, e.getLocalizedMessage(), this.getOptName());
      }
      journalStream = null;
    }
    LogUtil.fine(LoggerType.TRACE, "Operator " + this.getOptName() + ": Batch Ended.");
  }
  
  /*
   * Create snapshot of operator states
   * It can generate either full snapshot or incremental changes.
   */
  @Override
  public void createSnapshot(ObjectOutputStream output, boolean fullSnapshot) throws CEPException
  {
    try
    {
      // Create snapshot for inter-operator queues if required
      if(requiresBufferedInput())
        createQueueSnapshot(output);
      
      // Case-1: Operator Supports Journaling AND Request to create Journal Snapshot
      // Action: Write all bytes (having journal entries) into output stream
      if (usesJournaling() && !fullSnapshot)
      {
        byteJournalStream.flush();
        byte[] buf = byteJournalStream.toByteArray();
        output.writeObject(buf);
        output.flush();
        LogUtil.fine(LoggerType.TRACE, "Created Journal Snapshot [operator=" + this.getOptName()+ ", num-bytes=" + buf.length + "]");
      }
      // Case-2: Operator Supports Partial Journaling AND Request to create Journal Snapshot
      // Action: 1) Write all bytes (having journal entries) into output stream
      //            for all state objects which supports incremental snapshot
      //         2) Create a Partial Full Snapshot of remaining state objects which doesn't
      //            support journaling. Write this to output stream.
      else if(usesPartialJournaling() && !fullSnapshot)
      {
        // byteJournalStream will contain journal entries for all incremental changes
        // for state objects which supports journaling
        byteJournalStream.flush();
        byte[] buf = byteJournalStream.toByteArray();
        output.writeObject(buf);
        output.flush();
        
        // Create a Partial Full Snapshot for remaining state objects which are not
        // journalled.
        ObjStreamFactory fac = ObjStreamUtil.getObjStreamFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = fac.createObjectOutputStream(baos);
        createPartialSnapshot(oos);        
        oos.flush();
        byte[] partialFullSnapshotBytes = baos.toByteArray();
        output.writeObject(partialFullSnapshotBytes);
        oos.close();
        
        LogUtil.fine(LoggerType.TRACE, "Created Partial Full and Partial Journal Snapshot [operator=" 
          + this.getOptName()+ ", partial-full-num-bytes=" + partialFullSnapshotBytes.length +
          ", partial-journal-num-bytes=" + buf.length + "]");
      }
      // Case-3: Request to create a Full Snapshot
      // Action: Create a full snapshot irrespective of the fact whether operator supports
      //         journaling, partial journaling or full.
      else
      {    
        // Full Snapshot
        ObjStreamFactory fac = ObjStreamUtil.getObjStreamFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = fac.createObjectOutputStream(baos);
        createSnapshot(oos);        
        oos.flush();
        byte[] snapshotBytes = baos.toByteArray();
        output.writeObject(snapshotBytes);
        oos.close();
        LogUtil.fine(LoggerType.TRACE, "Created Full Snapshot [operator=" + this.getOptName()+
          ", num-bytes=" + snapshotBytes.length + "]");        
      }
    }
    catch (IOException e) {
      LogUtil.logStackTrace(e);
      throw new CEPException(ExecutionError.SNAPSHOT_CREATION_ERROR, e, e.getLocalizedMessage(), this.getOptName());
    }
  }

  /**
   * Create a Full Snapshot and write into output stream.
   * <p>
   * Default implementation for execution operator is empty.
   * It is assumed that all stateful operators will override this function
   * to write their respective state to given java object output stream.
   * For stateless operator, ExecOpt.createSnapshot() should be invoked.  
   * @param output
   */
  protected void createSnapshot(ObjectOutputStream output) throws CEPException
  { 
  }
  
  /**
   * Create a partial Full Snapshot and write snapshot bytes into {@output} stream.
   * <p>
   * Default implementation for execution operator is empty.
   * It is assumed that all stateful operators will override this function
   * to write their respective state to given java object output stream.
   * For stateless operator, ExecOpt.createSnapshot() should be invoked.  
   * @param output
   */
  protected void createPartialSnapshot(ObjectOutputStream output) throws CEPException
  {    
  }
  
  /**
   * Default implementation for creating snapshot of interoperator queue is empty.
   * Operator implementation should override this method to implement the 
   * logic to create a snapshot for the desired queue/queues.
   * @param output
   * @throws CEPException
   */
  protected void createQueueSnapshot(ObjectOutputStream output) throws ExecException
  {    
  }
  
  /**
   * Default implementation for loading snapshot of interoperator queue is empty.
   * Operator implementation should override this method to implement the 
   * logic to load a snapshot for the desired queue/queues.
   * @param output
   * @throws CEPException
   */
  protected void loadQueueSnapshot(ObjectInputStream input) throws ExecException
  {    
  }
  
  /**
   * Load the operator states from the given snapshot(full or incremental).
   * If fullSnapshot is set, the operator states should be loaded from the full snapshot.
   * Otherwise, the input snapshot is incremental changes and should be applied to current snapshot.
   */
  @Override
  public void loadSnapshot(ObjectInputStream input, boolean fullSnapshot) throws ExecException
  {   
    // Create snapshot for inter-operator queues if required
    if(requiresBufferedInput())
      loadQueueSnapshot(input);
    
    // Five Cases:
    // 1. If operator supports journaling and we need to apply journal snapshot
    //    Action: Apply recovered journal entry to operator
    //
    // 2. If operator supports journaling and we need to apply full snapshot
    //    Action: Load full snapshot to operator right away without adding in pending snapshot
    //            so that following incremental snapshots can be applied on this state.
    //
    // 3. If operator supports partial journaling and we need to apply journal snapshot
    //    Action: The partial journal snapshot will contain journal entry for few operator constructs and
    //            full snapshot for remaining operator constructs).
    //            In this step, we will apply only journal snapshot and full snapshot component will be kept pending.
    //
    // 4. If operator supports partial journaling and we need to apply full snapshot
    //    Action: Apply full snapshot to operator right away without adding in pending shot
    //            so that following partial journal snapshot can be applied on this state.
    //            Note: While applying the full snapshot, It is operator's decision to 
    //                  load it partially and keep the pending snapshot until first event
    //                  arrives after state restore. Please check individual operator implementation.
    //
    // 5. If operator doesn't support journaling or partial journaling and we need to apply a snapshot (full or journal)
    //    Action: i) Don't apply full snapshot now because typically a fullsnapshot apply followed by incremental snapshots.
    //               For this case, incremental is also a full snapshot so we will only apply the last snapshot to optimize state restore
    //            ii) If it is a journal snapshot, it should be treated as full snapshot since operator doesn't
    //                support journaling so the snapshot obtained originally was a full snapshot
    try
    {
      ObjStreamFactory fac = ObjStreamUtil.getObjStreamFactory();
      // CASE-1: If operator supports journaling and we need to apply journal snapshot
      if(usesJournaling() && !fullSnapshot)
      {
        byte[] buf = (byte[])input.readObject();
        ByteArrayInputStream bais = new ByteArrayInputStream(buf);
        ObjectInputStream ois = fac.createObjectInputStream(bais);
        Object journalEntry = ois.readObject();
        applySnapshot(journalEntry);     
        ois.close();
        LogUtil.fine(LoggerType.TRACE, "Applied Journal Snapshot [operator=" + this.getOptName()+ ", num-bytes=" + buf.length + "]");
      }
      // CASE-2: If operator supports journaling and we need to apply full snapshot
      else if(usesJournaling())
      {
        byte[] buf = (byte[]) input.readObject();
        ByteArrayInputStream bais = new ByteArrayInputStream(buf);
        ObjectInputStream ois = fac.createObjectInputStream(bais);
        loadSnapshot(ois);
        ois.close();
        LogUtil.fine(LoggerType.TRACE, "Loaded Full Snapshot [operator=" + this.getOptName()+ ", num-bytes=" + buf.length + "]");
      }
      // CASE-3: If operator supports partial journaling and we need to apply journal snapshot
      else if(usesPartialJournaling() && !fullSnapshot)
      {
        // Read Journal Snapshot component from input stream
        byte[] journalingBuf = (byte[]) input.readObject();
        // Read Full Snapshot component from input stream
        // Note: Full Snapshot Bytes will not be loaded into operator now.
        // Reason: It is possible that operator might need to apply few more journal snapshots.
        //         It is important to know that we load full snapshot only once and full snapshot
        //         load isn't cleaning existing state. So loading full snapshot more than once
        //         can create duplicate objects in operator state.
        pendingPartialSnapshotBytes = (byte[]) input.readObject();        
        pendingPartialSnapshotVersion = SnapshotContext.getVersion();
        
        // Apply Journal component now.
        ByteArrayInputStream bais = new ByteArrayInputStream(journalingBuf);
        ObjectInputStream ois = fac.createObjectInputStream(bais);
        Object journalEntry = ois.readObject();
        applySnapshot(journalEntry);
        ois.close();
        
        LogUtil.fine(LoggerType.TRACE, "Applied Partial Journal Snapshot [operator=" + this.getOptName()+ ", num-bytes=" + journalingBuf.length + "]");
        LogUtil.fine(LoggerType.TRACE, "Pending Partial Full Snapshot [operator=" + this.getOptName()+ ", num-bytes=" + pendingPartialSnapshotBytes.length + "]");
      }
      // CASE-4: If operator supports partial journaling and we need to apply full snapshot
      else if(usesPartialJournaling())
      {
        byte[] buf = (byte[]) input.readObject();
        ByteArrayInputStream bais = new ByteArrayInputStream(buf);
        ObjectInputStream ois = fac.createObjectInputStream(bais);
        loadSnapshot(ois);
        ois.close();
        LogUtil.fine(LoggerType.TRACE, "Loaded Full Snapshot [operator=" + this.getOptName()+ ", num-bytes=" + buf.length + "]");
      }
      // CASE-5: If operator doesn't support journaling or partial journaling and we need to apply a snapshot (full or journal)
      else
      {
        pendingSnapshotVersion = SnapshotContext.getVersion();
        pendingSnapshotBytes = (byte[]) input.readObject();
        LogUtil.fine(LoggerType.TRACE, "Pending Full Snapshot [operator=" + this.getOptName()+ ", num-bytes=" + pendingSnapshotBytes.length + "]");
      }
    }
    catch (ClassNotFoundException e) 
    {
      LogUtil.logStackTrace(e);
      throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR_CNF, e, this.getOptName(), e.getMessage());
    }
    catch(IOException e)
    {
      LogUtil.logStackTrace(e);
      throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR, e, e.getLocalizedMessage(), getOptName());
    }
  }

  /**
   * Load the pending snapshot bytes into operator state.
   * <p>
   * As we don't load full snapshot until first event doesn't arrive at the
   * operator after state restore. Prior to process the first event, operator
   * will load pending snapshot bytes into state first. The reason for lazy
   * initialization is to prevent loading full snapshots multiple times.
   * Since full snapshot load doesn't clean the state, so loading multiple
   * times creates duplicate objects in operator state.
   * @throws ExecException
   */
  protected void loadPendingSnapshot() throws ExecException
  {    
    // Check if operator has any pending snapshot(full or partial full) to apply
    boolean hasPendingSnapshot = pendingSnapshotBytes != null ||
                                 pendingPartialSnapshotBytes != null;
        
    if(hasPendingSnapshot)
    {      
      // isFullSnapshot = true  means the bytes are full snapshot for all state objects.
      // isFullSnapshot = false means the bytes are full snapshot for only few state objects 
      //                              which doesn't support incremental.
      boolean isFullSnapshot = pendingSnapshotBytes != null;
      byte[] snapshotBytes = isFullSnapshot ? pendingSnapshotBytes : pendingPartialSnapshotBytes;
      double snapshotVersion = isFullSnapshot ? pendingSnapshotVersion : pendingPartialSnapshotVersion;
      
      ByteArrayInputStream bais = new ByteArrayInputStream(snapshotBytes);
      ObjectInputStream ois = null;
      try 
      {
        ObjStreamFactory fac = ObjStreamUtil.getObjStreamFactory();
        ois = fac.createObjectInputStream(bais);
        SnapshotContext.setVersion(snapshotVersion);
        if(isFullSnapshot)
          loadSnapshot(ois);          
        else
          loadPartialSnapshot(ois);          
        LogUtil.fine(LoggerType.TRACE, "Loaded Pending "+ (isFullSnapshot ? "Full" : "Partial Full") +
           " Snapshot [operator=" + this.getOptName()+ ", num-bytes=" + snapshotBytes.length + "]");
        
      } 
      catch (IOException e) 
      {
        LogUtil.logStackTrace(e);
        throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR, e, e.getLocalizedMessage(), getOptName());
      }
      finally
      {
        pendingSnapshotBytes = null;
        pendingPartialSnapshotBytes = null;
        try 
        {
          if(ois != null)
            ois.close();
        } 
        catch (IOException e) 
        {
          LogUtil.logStackTrace(e);
          throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR, e, e.getLocalizedMessage(), getOptName());
        }
      }      
    } 
  }
  
  /**
   * Load Full Snapshot into operator from given input object stream<p>
   * Must be implemented by operator.
   * @param input
   * @throws ExecException
   */
  protected void loadSnapshot(ObjectInputStream input) throws ExecException
  {}
  
  /**
   * Load Partial Full Snapshot into operator from given object stream.<p>
   * Must be implemented by operator.
   * @param input
   * @throws ExecException
   */
  protected void loadPartialSnapshot(ObjectInputStream input) throws ExecException
  {}
  
  /**
   * Apply Journal Snapshot Entry into operator
   * @param journalEntry
   * @throws ExecException
   */
  protected void applySnapshot(Object journalEntry) throws ExecException {}

  public ExecStats getMutStats()
  {
    if(mut_state!=null)return mut_state.stats;
    return null;
  }
}
