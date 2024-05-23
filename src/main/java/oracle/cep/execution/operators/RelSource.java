/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/RelSource.java /main/83 2015/04/14 02:49:38 udeshmuk Exp $ */
/* Copyright (c) 2006, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares RelSource in package oracle.cep.execution.operators.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 udeshmuk  04/08/15 - set initializedState to true even when zero archiver
                      tuples are returned
 sbishnoi  02/25/14 - bug 18278394
 pkali     10/02/13 - Refactored for primary key based retrieval (bug 17363573)
 udeshmuk  08/12/13 - fix timestamp OOO in special join - set snapshotid for hb
                      sent in enqueueHeartbeat
 udeshmuk  07/23/13 - bug 16813624: make use of useMillisTs param while
                      determining timeout
 udeshmuk  07/10/13 - fix logging related to archived relation framework
 sbishnoi  05/27/13 - bug 16804634
 udeshmuk  05/21/13 - 16820093 : set snapshot id for heartbeat
 sbishnoi  03/08/13 - bug 16484087 fix heartbeat propagation for archiver sources
 vikshukl  02/19/13 - save away heartbeat time
 udeshmuk  01/25/13 - no need to set state to archive_sia_done here, done in
                      planmanager.propagateArchivedrelationtuples
 vikshukl  10/10/12 - move isArchivedDim flag to ExecOpt
 udeshmuk  10/09/12 - set cid and tid values to null post computation of
                      snapshotid
 udeshmuk  09/09/12 - propagate snapshotid and handle event id
 sbishnoi  08/19/12 - bug 14502856
 vikshukl  07/30/12 - add archived dimension relation
 udeshmuk  05/27/12 - propagate snapshotId and archived flag
 udeshmuk  04/16/12 - set snapshotid
 udeshmuk  02/10/12 - send heartbeat after snapshot
 udeshmuk  09/03/11 - change run method to work properly when synopsis is not
                      present
 udeshmuk  08/29/11 - set tuple id while converting from TupleValue to ITuple
 udeshmuk  06/29/11 - support for archived relation
 anasrini  03/24/11 - support for PARTITION parallelism
 anasrini  12/19/10 - replace eval() with eval()
 sbishnoi  09/29/09 - support for table function
 sborah    07/16/09 - support for bigdecimal
 parujain  05/07/09 - lifecycle mgmt
 anasrini  05/08/09 - implement ExecSourceOpt interface
 sborah    04/13/09 - assertion check
 sbishnoi  04/08/09 - use TupleValue to set totalOrderingFlag
 sbishnoi  04/08/09 - removing followup Heartbeat
 anasrini  02/13/09 - fix for bug 8256792
 anasrini  02/13/09 - remove allowEnqueue
 parujain  01/29/09 - transaction mgmt
 sbishnoi  01/27/09 - total order optimization
 anasrini  01/22/09 - override allowEnqueue (bug 7872507)
 sbishnoi  01/21/09 - total order optimization
 hopark    12/04/08 - change exception on interfaceException
 sbishnoi  12/03/08 - support for generic data source
 hopark    10/15/08 - TupleValue refactoring
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 hopark    10/07/08 - use execContext to remove statics
 sbishnoi  08/03/08 - support for nanosecond;
 sbishnoi  03/26/08 - removing commented println statements
 udeshmuk  03/17/08 - add hb check in canbeScheduled.
 hopark    02/28/08 - resurrect refcnt
 hopark    02/05/08 - parameterized error
 udeshmuk  01/17/08 - change in the way of getting timestamp of tuple.
 hopark    12/06/07 - cleanup spill
 udeshmuk  12/17/07 - heartbeat support.
 sbishnoi  11/27/07 - initialize isUpdateTuple
 parujain  11/26/07 - Connection getter
 parujain  11/09/07 - External Source
 sbishnoi  11/07/07 - support for update semantics
 sbishnoi  11/07/07 - 
 udeshmuk  11/06/07 - modified populateOutput by using copyFrom method
 sbishnoi  10/31/07 - bug: destination attribute in tuple was not null if
                      corresponding attr in src is null
 hopark    10/30/07 - remove IQueueElement
 hopark    10/22/07 - remove TimeStamp
 sbishnoi  10/29/07 - support for primary key
 parujain  10/17/07 - cep-bam integration
 parujain  10/04/07 - end src when oper removed
 hopark    09/07/07 - eval refactor
 najain    07/09/07 - cleanup
 hopark    07/13/07 - dump stack trace on exception
 parujain  07/03/07 - cleanup
 parujain  06/26/07 - mutable state
 hopark    06/21/07 - cleanup
 hopark    06/11/07 - loggging - remove ExecContext
 najain    06/06/07 - fix lint errors
 hopark    05/22/07 - logging support
 parujain  05/24/07 - handle softexecException
 hopark    05/11/07 - remove System.out.println(use java.util.logging instead)
 parujain  05/08/07 - monitoring statistics
 hopark    04/26/07 - refcount debug
 hopark    04/08/07 - fix pincount
 hopark    04/05/07 - memmgr reorg
 najain    04/03/07 - bug fix
 hopark    03/23/07 - throws exceptions from QueueElement
 hopark    03/21/07 - add TuplePtr pin
 najain    03/16/07 - cleanup
 najain    03/14/07 - cleanup
 parujain  03/16/07 - debug level
 najain    03/12/07 - bug fix
 parujain  02/20/07 - fix RelSource bug
 parujain  02/13/07 - interfaces with ConfigManager
 najain    01/04/07 - spill over support
 parujain  12/19/06 - fullScanId for RelationSynopsis
 parujain  12/06/06 - propagating relation
 hopark    11/17/06 - override addToScheduler to add source op
 hopark    11/16/06 - add bigint datatype
 najain    11/07/06 - add getOldestTs
 najain    10/30/06 - debugging support
 parujain  10/06/06 - Interval Datatype
 anasrini  09/13/06 - attr name and initialize
 najain    08/11/06 - ref count optimizations
 najain    08/10/06 - add asserts
 najain    08/10/06 - bug in heartbeats
 parujain  08/04/06 - Timestamp datastructure
 najain    07/28/06 - handle static relations 
 najain    07/19/06 - ref-count tuples 
 najain    07/13/06 - ref-count timestamps 
 najain    07/13/06 - ref-count timeStamp support 
 najain    06/16/06 - bug fix 
 najain    06/13/06 - bug fix 
 najain    06/12/06 - bug fix 
 najain    06/08/06 - query addition re-entrant 
 najain    06/04/06 - add full scan 
 najain    05/23/06 - bug fix 
 najain    05/18/06 - implementation
 skaluska  03/14/06 - query manager 
 skaluska  02/06/06 - Creation
 skaluska  02/06/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/RelSource.java /main/83 2015/04/14 02:49:38 udeshmuk Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Constants;
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
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.queues.ISharedQueueWriter;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.execution.snapshot.SynopsisPersistenceContext;
import oracle.cep.execution.stores.RelStoreImpl;
import oracle.cep.execution.synopses.ExecSynopsis;
import oracle.cep.execution.synopses.RelationSynopsis;
import oracle.cep.extensibility.datasource.IExternalConnection;
import oracle.cep.extensibility.datasource.IExternalPreparedStatement;
import static oracle.cep.extensibility.datasource.IExternalPreparedStatement.*;
import oracle.cep.interfaces.InterfaceException;
import oracle.cep.interfaces.input.ExtSource;
import oracle.cep.interfaces.input.TableSource;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.service.ExecContext;
import oracle.cep.snapshot.SnapshotContext;

/**
 * RelSource is the execution operator that reads input tuples for registered
 * relations
 * 
 * @author najain
 */
public class RelSource extends ExecSourceOpt
{
  /** relation id. */
  int                      relnId;

  boolean                  isSilent;

  /** Specification of the attributes in the input relation */
  private TupleSpec        attrSpecs;

  /** Number of attributes */
  private int              numAttrs;

  /** column names of relation */
  private String           columnNames[];

  /** Source who is feeding us the tuples */
  private TableSource      source;

  /** evaluation context */
  private IEvalContext      evalContext;

  /** Synopsis storing the output */
  private RelationSynopsis synopsis;

  /** relation store for the synopsis */
  RelStoreImpl             relStore;

  /** Scan identifier */
  private int              scanId;

  /** Full Scan identifier */
  private int              fullScanId;
  
  private int              keyScanId;

  private long             maxTime;

  /** Flag to check whether primary key exists or not for this relation*/
  boolean                  isPrimaryKeyExist;
  
  /** Positions of Primary key Attributes in tuple*/
  ArrayList<Integer>       primaryKeyAttrPos;
  
  long                     ts;
  
  boolean                  isExternalSource;
  
  /** Evaluator to check whether primary key attributes are null or not*/
  IBEval                   isPrimaryKeyAttrNullEval;
  
  private boolean          isSystemTimestamped;
  
  private long             timeoutDuration;
  
  /** Used only for archived relation.
   *  Indicates the position of worker identifier column in the schema */   
  private int              workerIdColNum = -1;

  /** Used only for archived relation.
   *  Indicates the position of txn identifier column in the schema */   
  private int              txnIdColNum = -1;
  /*
      fix bug-26984661 - we never need to handlePropOldData in spark-cql since we are not using dyna
      The handlePropOldData causing indefinite loop for geofence pattern query (where many views and
      in yarn cluster during loading the snapshot. since the dynamic feature is not used, we are dis
      as this bug fix for now.
    */
  private boolean oldDataPropNeeded = false;

  private boolean initializedState = false;
  
  private IExternalPreparedStatement pstmt;

  
  public long getMaxTime()
  {
    return maxTime;
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
  
  /**
   * @return Returns the attrSpecs.
   */
  public TupleSpec getAttrSpecs()
  {
    return attrSpecs;
  }

  /**
   * @param attrSpecs
   *          The attrSpecs to set.
   */
  public void setAttrSpecs(TupleSpec attrSpecs)
  {
    this.attrSpecs = attrSpecs;
  }

  /**
   * @return Returns the numAttrs.
   */
  public int getNumAttrs()
  {
    return numAttrs;
  }

  /**
   * @param numAttrs
   *          The numAttrs to set.
   */
  public void setNumAttrs(int numAttrs)
  {
    this.numAttrs = numAttrs;
  }

  /**
   * @return Returns the source.
   */
  public TableSource getSource()
  {
    return source;
  }

  /**
   * @param source
   *          The source to set.
   */
  public void setSource(TableSource source)
  {
    this.source = source;
  }

  /**
   * @return Returns the evalContext.
   */
  public IEvalContext getEvalContext()
  {
    return evalContext;
  }

  /**
   * @param evalContext
   *          The evalContext to set.
   */
  public void setEvalContext(IEvalContext evalContext)
  {
    this.evalContext = evalContext;
  }

  /**
   * @return Returns the synopsis.
   */
  public RelationSynopsis getSynopsis()
  {
    return synopsis;
  }

  /**
   * @param synopsis
   *          The synopsis to set.
   */
  public void setSynopsis(RelationSynopsis synopsis)
  {
    this.synopsis = synopsis;
  }

  /**
   * @param relnId
   *          The relnId to set.
   */
  public void setRelnId(int relnId)
  {
    this.relnId = relnId;
  }

  /**
   * @return Returns the scanId.
   */
  public int getScanId()
  {
    return scanId;
  }

  /**
   * @param scanId
   *          The scanId to set.
   */
  public void setScanId(int scanId)
  {
    this.scanId = scanId;
  }

  /**
   * @return Returns the fullScanId.
   */
  public int getFullScanId()
  {
    return fullScanId;
  }

  /**
   * @param fullScanId
   *          The fullScanId to set.
   */
  public void setFullScanId(int fullScanId)
  {
    this.fullScanId = fullScanId;
    this.propScanId = fullScanId;
  }
  
  /**
   * @param keyScanId
   *          The keyScanId to set.
   */
  public void setKeyScanId(int keyScanId)
  {
    this.keyScanId = keyScanId;
  }

  /**
   * @param relStore
   *          The relation store to set.
   */
  public void setRelStore(RelStoreImpl relStore)
  {
    this.relStore = relStore;
  }
  
  public void setIsExternal(boolean isext)
  {
    this.isExternalSource = isext;
  }
  
  public boolean getIsExternal()
  {
    return this.isExternalSource;
  }
  
  public void setIsSystemTimestamped(boolean isSysTs)
  {
    this.isSystemTimestamped = isSysTs;
  }
  
  public boolean getIsSystemTimestamped()
  {
    return this.isSystemTimestamped;
  }
  
  public void setTimeoutDuration(long timeout)
  {
    this.timeoutDuration = timeout;
  }
  
  public long getTimeoutDuration()
  {
    return this.timeoutDuration;
  }
  
  /**
   * Set isPrimaryKeyExist Flag
   * @param isPrimaryKeyExist
   */
  public void setIsPrimarKeyExist(boolean isPrimaryKeyExist)
  {
    this.isPrimaryKeyExist = isPrimaryKeyExist;
  }

  /**
   * Get isPrimaryKeyExist Flag
   * @return true if primary key exist over relation
   *         else false
   */
  public boolean getIsPrimaryKeyExist()
  {
    return this.isPrimaryKeyExist;
  }
  
  /**
   * Set primaryKeyAttrPos
   * @param primaryKeyAttrPos
   *        List of Primary key attribute positions in tuple
   */
  public void setPrimaryKeyAttrPos(ArrayList<Integer> primaryKeyAttrPos)
  {
    this.primaryKeyAttrPos = primaryKeyAttrPos;
  }
  
  /**
   * Set isPrimaryKeyAttrNullEval to check whether the primary key attributes
   * contains null or not
   * @param isPrimaryKeyAttrNullEval
   */
  public void setIsPrimaryKeyAttrNull(IBEval isPrimaryKeyAttrNullEval)
  {
    this.isPrimaryKeyAttrNullEval = isPrimaryKeyAttrNullEval;
  }  
  
  public IExternalPreparedStatement getPstmt()
  {
	return pstmt;
  }

  public void setPstmt(IExternalPreparedStatement pstmt)
  {
	this.pstmt = pstmt;
  }

  /**
   * Getter for operator statistics
   *
   * @return Operator stats
   */
  public ExecStats getStats() {
    stats = super.getStats();
    if (isExternalSource && getPstmt() != null) {
      boolean isCachedSrc = getPstmt().execOnEachEvt();
      Map<String, Object> pstmtStat = getPstmt().getStat();
      long cacheMisses = pstmtStat.get(CACHE_MISS_ENTRY_KEY) != null
              ? (Long) pstmtStat.get(CACHE_MISS_ENTRY_KEY)
              : 0L;
      long cacheHits = pstmtStat.get(CACHE_HIT_ENTRY_KEY) != null
              ? (Long) pstmtStat.get(CACHE_HIT_ENTRY_KEY)
              : 0L;
      String cacheName = pstmtStat.get(CACHE_NAME_ENTRY_KEY) != null
              ? (String) pstmtStat.get(CACHE_NAME_ENTRY_KEY)
              : "";
      long runningExecTime = pstmtStat.get(RUNNING_EXEC_TIME) != null
              ? (Long) pstmtStat.get(RUNNING_EXEC_TIME)
              : 0L;
      long psmtExecs = pstmtStat.get(NO_OF_EXECUTION) != null
              ? (Long) pstmtStat.get(NO_OF_EXECUTION)
              : 0L;
      if (isCachedSrc) {
        stats.setCacheName(cacheName);
        stats.setCacheMisses(cacheMisses);
        stats.setCacheHits(cacheHits);
        stats.setSrcCached(isCachedSrc);
      }
      stats.setTotalPstmtExec(psmtExecs);
      stats.setTotalPstmtRunTime(runningExecTime);
    }
    return stats;
  }

/**
   * initialize
   * 
   * @throws CEPException
   */
  public void initialize() throws CEPException
  {
    source.setNumAttrs(numAttrs);
    source.setIsStream(false);
    source.setIsArchived(execContext.getTableMgr().getTable(relnId).isArchived());
    source.setIsDimension(execContext.getTableMgr().getTable(relnId).isDimension());
    for (int i = 0; i < numAttrs; i++)
    {
      source.setAttrInfo(i, columnNames[i], attrSpecs.getAttrMetadata(i));
    }

    source.start();

    // is it a silent relation
    isSilent = execContext.getTableMgr().getTable(relnId).getIsSilent();    
  }
 
  /**
   * Get External Connection for this External source
   * @return IExternalConnection object
   */
  public IExternalConnection getExtConnection()
  {
    assert source instanceof ExtSource;    
    return ((ExtSource)source).getExtConnection();
    
  }

  /**
   * Constructor for RelSource
   * @param ec Execution context
   */
  public RelSource(ExecContext ec, int maxAttrs)
  {
    super(ExecOptType.EXEC_RELN_SOURCE, new RelSourceState(ec), ec);
    attrSpecs = new TupleSpec(factoryMgr.getNextId(), maxAttrs);
    columnNames = new String[maxAttrs];
    ts = 0;
    numAttrs = 0;
    isExternalSource = false;
    isPrimaryKeyExist = false;
    isSystemTimestamped = false;
    timeoutDuration = -1;
  }

  /**
   * Add an attribute at the next position
   * 
   * @param name
   *          Attribute name
   * @param type
   *          Attribute type
   * @param len
   *          Attribute maximum length
   * @throws ExecException
   */
  public void addAttr(String name, AttributeMetadata attrMetadata)
  throws ExecException
  {
    attrSpecs.addAttr(numAttrs, attrMetadata);
    columnNames[numAttrs] = name;
    numAttrs++;
  }

  /**
   * Populate the output tuple
   * 
   * @throws CEPException
   */
  protected void populateOutput(ITuplePtr destPtr, TupleValue src)
      throws CEPException
  {
    if (destPtr == null)
      return;
    ITuple dest = destPtr.pinTuple(IPinnable.WRITE);
    dest.copyFrom(src, numAttrs, attrSpecs);
    
    //if archived source then eventIdColNum would not be equal to -1.
    if(eventIdColNum != -1)
      dest.setId(src.lValueGet(eventIdColNum));    
    destPtr.unpinTuple();
  }
  
   /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.operators.ExecOpt#run(int)
   */
  @Override
  public int run(int timeSlice) throws CEPException
  {
    boolean done = false;
    boolean exitState = true;
    RelSourceState s = (RelSourceState) mut_state;
    int numElements;

    assert s.state != ExecState.S_UNINIT;
    
    // Stats
    s.stats.incrNumExecutions();

    // A Flag to check that timeout heartbeat has been already propagated
    boolean isTimeoutHbtPropagated = false;
    
    boolean useMillisTs = 
      execContext.getServiceManager().getConfigMgr().getUseMillisTs();

    if(isRestoreTupleId())
      restoreTupleId();
    try
    {
      /** This lock is acquired to maintain the maximum time for this source */
      execMgr.getLock().readLock().lock();

      // Number of input elements to process
      numElements = timeSlice;
      while ((s.stats.getNumInputs() < numElements) || (!exitState))
      {
        switch (s.state)
        {
          case S_PROPAGATE_OLD_DATA:
            if(synopsis != null)
            {
              if(LogUtil.isFinerEnabled(LoggerType.TRACE)) {
                LogUtil.finer(LoggerType.TRACE, "s.laststate=" + s.lastState + " olddatapropneeded=" + oldDataPropNeeded);
              }
              if(oldDataPropNeeded )
              {
                setExecSynopsis((ExecSynopsis) synopsis);
                handlePropOldData();
              }
              else
              {
                //fix bug-26984661 - don't enable handlePropOldData. more details on oldDataPropNeeded
                //oldDataPropNeeded = true;
                s.state = s.lastState;
              }
            }
            else
              s.state = s.lastState;
            break;

          case S_INIT:
            // Get the next input tuple
            try
            {
              s.inputTuple = source.getNext();
            }
            catch (InterfaceException e)
            {
              throw new SoftExecException(ExecutionError.FAILED_TO_GET_TUPLE, 
                  source.toString(), this.toString(), e.getMessage() + "\n" +e.getAction());
            }
            /*
             * If this is an archived dimension and we're done initializing the 
             * state, then throw an exception on change.
             */

            //Don't throw exception for heartbeat. 
            //Use the variable initializedState to check whether state was
            //initialized for this operator.
            if (s.inputTuple != null &&
                (!(s.inputTuple.getKind() == TupleKind.HEARTBEAT)) &&
                this.isArchivedDim() &&
                this.initializedState)
            {
              throw new 
                ExecException(ExecutionError.ARCHIVED_DIMENSION_CHANGE_DETECTED); 
            }              
              
            s.state = ExecState.S_INPUT_DEQUEUED;

          case S_INPUT_DEQUEUED:
            if (s.inputTuple == null)
            { 
              //input queue is empty
              // Test and send heartbeat timeout.
              if (isSystemTimestamped && (timeoutDuration != -1))
              {
                //bug 16813624:  use the param useMillisTs. Review carefully.
                if(this.getSource().isArchived() ||
                   useMillisTs
                   )
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
                     (diff < timeoutDuration || isTimeoutHbtPropagated))
                  {
                    // return from run() method..let other operator execute
                    s.state = ExecState.S_INIT;
                    done    = true; 
                    break;
                  }
                  else if (s.lastInputTs == Constants.MIN_EXEC_TIME)
                  {
                    if(isTimeoutHbtPropagated)
                    {
                      s.state = ExecState.S_INIT;
                      done = true;
                      break;
                    }
                  }
                }
                else if ((System.nanoTime() - s.lastInputTs) < timeoutDuration ||
                         isTimeoutHbtPropagated)
                {
                  // return from run() method..let other operator execute
                  s.state = ExecState.S_INIT;
                  done    = true; 
                  break;
                }
                else
                {
                  // Set the flag to true as timeout heartbeat is already being
                  // propagated now.
                  isTimeoutHbtPropagated = true;
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
              // As we are sending a heartbeat in this case because either
              // 1) Operator is scheduled and lastInputTs > lastOutPutTs
              //    So we will set the flag nextTotalOrderingGuarantee to 
              //    lastTotalOrderingGuarantee
              // OR
              // 2) In case of system-timestamp, timeOutDuration has expired
              //    so we need to send a hBt
              // Timestamp of hBt will be calculated in later states
              s.nextTotalOrderingGuarantee = s.lastTotalOrderingGuarantee;
            }
            else
            {
              // Get the total ordering flag from input tuple value
              s.inpTotalOrderingGuarantee = s.inputTuple.isTotalOrderGuarantee();
              
              // Bump up our counts
              if(s.inputTuple.getKind() == TupleKind.HEARTBEAT || s.inputTuple.isBHeartBeat())
                s.stats.incrNumInputHeartbeats();
              else
                s.stats.incrNumInputs();

              // application timestamped case
              assert s.inputTuple.getTime() != Constants.NULL_TIMESTAMP;
              
              // Get the timestamp
              s.inputTs = s.inputTuple.getTime();
              maxTime = s.inputTs;
              // Set isTotalOrdering flag
              // Condition: If Relation is not silent; 
              //             then use input TupleValue's total ordering flag
              //             to set the next output's ordering flag 
              // Special Case:Operator will not use nextTotalOrdering flag
              // when we will encounter an input tuple of type UPDATE
              // In this case; we need send a MINUS first (with flag = false)
              // and next PLUS with flag determined as mentioned in the above
              // Condition
              s.nextTotalOrderingGuarantee = s.inpTotalOrderingGuarantee;
              
              /* 
               * Commenting the code which is processing silent relation flag.
               * Reason: Currently we want to use silent relation flag only
               *         in context of snapshot generation framework where
               *         we want to convert a full snapshot to an incremental
               *         if a synopsis is based on silent relation.
              if (!isSilent)
              {
                // application timestamped case
                assert s.inputTuple.getTime() != Constants.NULL_TIMESTAMP;
                // Get the timestamp
                s.inputTs = s.inputTuple.getTime();
                maxTime = s.inputTs;
                // Set isTotalOrdering flag
                // Condition: If Relation is not silent; 
                //             then use input TupleValue's total ordering flag
                //             to set the next output's ordering flag 
                // Special Case:Operator will not use nextTotalOrdering flag
                // when we will encounter an input tuple of type UPDATE
                // In this case; we need send a MINUS first (with flag = false)
                // and next PLUS with flag determined as mentioned in the above
                // Condition
                s.nextTotalOrderingGuarantee = s.inpTotalOrderingGuarantee;
               
              }
              else 
              {
                // silent relation case
                assert s.inputTuple.getTime() == Constants.NULL_TIMESTAMP;
                
                execMgr.getLock().readLock().unlock();
                // It is assumed that ExecManager cannot throw an exception
                maxTime = execMgr.getMaxSourceTime() + 1;
                execMgr.getLock().readLock().lock();
                s.inputTs = maxTime;
                // Set isTotalOrdering flag
                // Condition: If Relation is silent; 
                //            set the flag nextTotalOrderingGuarantee = false
                s.nextTotalOrderingGuarantee = false;                
              }*/

              // We should have a progress of time.
              if (s.lastInputTs > s.inputTs)
              {
                throw ExecException.OutOfOrderException(this, s.lastInputTs,
                    s.inputTs, s.inputTuple.toString());
              }
              
              // ensure that the time stamp value is as per the OrderingFlag 
              assert s.inputTs >= s.minNextTs :
                getDebugInfo(s.inputTs, s.minNextTs, 
                             s.inputTuple.getKind(),
                             s.lastTupleKind);
              
              s.minNextTs = s.inpTotalOrderingGuarantee ? 
                            s.inputTs+1 : s.inputTs;

              // Update the last input Ts now
              s.lastInputTs = s.inputTs;
              s.lastTupleKind = s.inputTuple.getKind();
              
              // Update the last total ordering flag
              s.lastTotalOrderingGuarantee = s.nextTotalOrderingGuarantee;
              
              // Ignore heartbeats
              if (s.inputTuple.isBHeartBeat())
              {
                s.state = ExecState.S_INIT;
                exitState = false;
                break;
              }
            }
            s.state = ExecState.S_PROCESSING1;
          case S_PROCESSING1:
            s.isUpdateTuple = false;
            // Allocate the output tuple
            if (s.inputTuple == null)
            {
              s.outputTuple = null;
              s.state = ExecState.S_OUTPUT_TIMESTAMP;
              break;
            }
            else if (s.inputTuple.getKind() == TupleKind.PLUS)
            {
              s.outputTuple = tupleStorageAlloc.allocate();
              // Since we have allocated the tuple, we need to make sure that
              // we process this tuple completely
              exitState = false;
              s.state = ExecState.S_PROCESS_PLUS;             
            }
            else if(s.inputTuple.getKind() == TupleKind.UPDATE)
            {
              s.outputTuple = tupleStorageAlloc.allocate();
              // Since we have allocated the tuple, we need to make sure that
              // we process this tuple completely
              exitState = false;
              s.state = ExecState.S_PROCESS_UPDATE;
              break;
            }
            else if(s.inputTuple.getKind() == TupleKind.UPSERT)
            {
              s.outputTuple = tupleStorageAlloc.allocate();
              // Since we have allocated the tuple, we need to make sure that
              // we process this tuple completely
              exitState = false;
              s.state = ExecState.S_PROCESS_UPSERT;
              break;
            }
            else
            {
              assert (s.inputTuple.getKind() == TupleKind.MINUS);
              if(synopsis != null)
              {
                if (s.minusTuple == null)
                {
                  IAllocator<ITuplePtr> tf = factoryMgr.get(attrSpecs);
                  s.minusTuple = tf.allocate(); //SCRATCH_TUPLE
                  // s.minusTuple will be kept in a memory all the time
                  // no unpin will be done.
                }
              }
              else{
                //synopsis is null
                s.outputTuple = tupleStorageAlloc.allocate();
              }
              exitState = false;
              s.state = ExecState.S_PROCESS_MINUS;
              break;
            }

          case S_PROCESS_PLUS:
            // Populate output tuple
            populateOutput(s.outputTuple, s.inputTuple);
            
            // Search if Primary key violates for current input tuple or not
            if(isPrimaryKeyExist)
            {
              assert synopsis != null : "Synopsis is null, can't enforce "+ 
                                        "primary key uniqueness constraint";
              evalContext.bind(s.outputTuple, IEvalContext.UPDATE_ROLE);
              // Primary key attributes should not be NULL
              if(isPrimaryKeyAttrNullEval.eval(evalContext))
              {
                tupleStorageAlloc.release(s.outputTuple);
                throw new SoftExecException(ExecutionError.CANNOT_INSERT_NULL,
                    s.outputTuple.toString());
              }
              TupleIterator tupIter = synopsis.getScan(keyScanId);
              s.searchedTuple       = tupIter.getNext();
              synopsis.releaseScan(keyScanId, tupIter);
              // Synopsis shouldn't contain any tuple with same values for PKey
              // attributes
              if(s.searchedTuple != null)
              {
                tupleStorageAlloc.release(s.outputTuple);
                throw new SoftExecException(
                    ExecutionError.UNIQUE_CONSTRAINT_VIOLATION,
                      s.outputTuple.toString());
              }
            }
            
            if(synopsis != null)
              synopsis.insertTuple(s.outputTuple);
            s.state = ExecState.S_OUTPUT_TIMESTAMP;
            break;

          case S_PROCESS_MINUS:

            if(synopsis == null)
            {
              populateOutput(s.outputTuple, s.inputTuple);
            }
            else
            {
              populateOutput(s.minusTuple, s.inputTuple);
              
              // Bind to UPDATE role to compare key attributes
              evalContext.bind(s.minusTuple, IEvalContext.UPDATE_ROLE);
              
              TupleIterator scan = null;
              if(isPrimaryKeyExist)
              {
                // PKey attributes should not be NULL
                if(isPrimaryKeyAttrNullEval.eval(evalContext))
                {
                  throw new SoftExecException(ExecutionError.CANNOT_INSERT_NULL,
                      s.minusTuple.toString());
                }
                
                // Search if Primary key violates for current input tuple or not
                evalContext.bind(s.minusTuple, IEvalContext.UPDATE_ROLE);
                TupleIterator tupIter = synopsis.getScan(keyScanId);
                s.outputTuple         = tupIter.getNext();
                synopsis.releaseScan(keyScanId, tupIter);
                
                // If given tuple(to which we want to update) exists in synopsis
                // remove it from synopsis and send a MINUS of this tuple
                // Next iteration is responsible for sending corresponding PLUS
                if(s.outputTuple != null)
                {
                  synopsis.deleteTuple(s.outputTuple);
                }
                else
                {
                  // There must be a corresponding PLUS tuple 
                  throw new SoftExecException(
                    ExecutionError.INVALID_NEGATIVE_RELATION_TUPLE, 
                    s.minusTuple.toString());
                }
              }
              else
              {
                //for primary key scenario also we need to compare 
                //all the columns for the minus tuple
                scan = synopsis.getScan(scanId);
  
                s.outputTuple = scan.getNext();
  
                // There must be a corresponding PLUS tuple 
                if (s.outputTuple == null)
                  throw new SoftExecException(
                      ExecutionError.INVALID_NEGATIVE_RELATION_TUPLE, 
                      s.minusTuple.toString());
  
                synopsis.releaseScan(scanId, scan);
              
                synopsis.deleteTuple(s.outputTuple);
              }
            }
            s.state = ExecState.S_OUTPUT_TIMESTAMP;
            break;
            
          case S_PROCESS_UPDATE:
            assert synopsis != null : "Synopsis is null, Cannot handle"+
                                      " update tuple";
            s.isUpdateTuple   = true;
            s.isPlusProcessed = false;
            s.searchedTuple   = null;
            
            // UPDATE tuple can appear only for a relation which holds some
            // PKey attributes
            if(!isPrimaryKeyExist)
            {
              tupleStorageAlloc.release(s.outputTuple);
              throw new SoftExecException(
                  ExecutionError.INVALID_UPDATE_RELATION_TUPLE,
                  s.inputTuple.toString());
            }
            else
            {
              // Populate output tuple
              populateOutput(s.outputTuple, s.inputTuple);
              
              // Search if Primary key violates for current input tuple or not
              evalContext.bind(s.outputTuple, IEvalContext.UPDATE_ROLE);
             
              // PKey attributes should not be NULL
              if(isPrimaryKeyAttrNullEval.eval(evalContext))
              {
                tupleStorageAlloc.release(s.outputTuple);
                throw new SoftExecException(ExecutionError.CANNOT_INSERT_NULL,
                    s.outputTuple.toString());
              }
            }
            
            // Relation must hold primary key to process update tuple
            assert isPrimaryKeyExist;
            
            TupleIterator tupIter = synopsis.getScan(keyScanId);
            s.searchedTuple       = tupIter.getNext();
            synopsis.releaseScan(keyScanId, tupIter);
            
            // If given tuple(to which we want to update) exists in synopsis
            // remove it from synopsis and send a MINUS of this tuple
            // Next iteration is responsible for sending corresponding PLUS
            if(s.searchedTuple != null)
            {
              synopsis.deleteTuple(s.searchedTuple);
              s.state = ExecState.S_OUTPUT_TIMESTAMP;
            }
            else
            {
              // If given Tuple to which we want to update is not found
              //    Ignore input Update Tuple
              s.state  = ExecState.S_INIT;
            //REMOVE_REFCNT tupleStorageAlloc.release(s.outputTuple);
              throw new SoftExecException(
                  ExecutionError.INVALID_UPDATE_RELATION_TUPLE,
                  s.inputTuple.toString());
            }
            break;
            
          case S_PROCESS_UPSERT:
            assert synopsis != null : "Synopsis is null, Cannot handle"+
                                      " update tuple";
            s.isUpdateTuple   = true;
            s.isPlusProcessed = false;
            s.searchedTuple   = null;
            
            // UPSERT tuple can appear only for a relation which holds some
            // PKey attributes
            if(!isPrimaryKeyExist)
            {
              tupleStorageAlloc.release(s.outputTuple);
              throw new SoftExecException(
                  ExecutionError.INVALID_UPDATE_RELATION_TUPLE,
                  s.inputTuple.toString());
            }
            else
            {
              // Populate output tuple
              populateOutput(s.outputTuple, s.inputTuple);
              
              // Search if Primary key violates for current input tuple or not
              evalContext.bind(s.outputTuple, IEvalContext.UPDATE_ROLE);
             
              // PKey attributes should not be NULL
              if(isPrimaryKeyAttrNullEval.eval(evalContext))
              {
                tupleStorageAlloc.release(s.outputTuple);
                throw new SoftExecException(ExecutionError.CANNOT_INSERT_NULL,
                    s.outputTuple.toString());
              }
            }
            
            // Relation must hold primary key to process update tuple
            assert isPrimaryKeyExist;
            
            tupIter = synopsis.getScan(keyScanId);
            s.searchedTuple       = tupIter.getNext();
            synopsis.releaseScan(keyScanId, tupIter);
            
            // If given tuple(to which we want to update) exists in synopsis
            // remove it from synopsis and send a MINUS of this tuple
            // Next iteration is responsible for sending corresponding PLUS
            if(s.searchedTuple != null)
              synopsis.deleteTuple(s.searchedTuple);
            
            s.state = ExecState.S_OUTPUT_TIMESTAMP;
            break;
              
          case S_PROCESSING2:
            assert synopsis != null : "Synopsis is null, Cannot handle"+
                                      " update tuple";
            synopsis.insertTuple(s.outputTuple);
            s.isPlusProcessed = true;
            s.state = ExecState.S_OUTPUT_TIMESTAMP;
            
          case S_OUTPUT_TIMESTAMP:
            s.state = ExecState.S_OUTPUT_READY;
          case S_OUTPUT_READY:
            if (s.outputTuple == null)
            {
              if (isSystemTimestamped && (timeoutDuration != -1))
              {
                long heartbeatTime = source.getHeartbeatTime();
                // Set the flag to true as timeout heartbeat is being propagated.
                isTimeoutHbtPropagated = true;

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
                // Output HeartBeat element with timestamp = lastInputTs
                s.outputTs = s.lastInputTs;
                s.outputElement.heartBeat(s.outputTs);
              }
              // Set total order flag for output heartbeat element
              s.outputElement.setTotalOrderingGuarantee(
                s.nextTotalOrderingGuarantee);
            }
            else
            {
              s.outputTs = s.inputTs;
              s.outputElement.setTs(s.outputTs);
              
              // Set total order flag for non-heartbeat output element
              // Note: we will overwrite this flag in case of UPDATE              
              s.outputElement.setTotalOrderingGuarantee(
                  s.nextTotalOrderingGuarantee);
              
              if (s.inputTuple.getKind() == TupleKind.PLUS)
              {
                s.outputElement.setKind(QueueElement.Kind.E_PLUS);
                s.outputElement.setTuple(s.outputTuple);                
              }
              else if(s.inputTuple.getKind() == TupleKind.UPDATE)
              {
                if(s.isPlusProcessed)
                {
                  s.outputElement.setKind(QueueElement.Kind.E_PLUS);
                  s.outputElement.setTuple(s.outputTuple);
                }
                else
                {
                  s.outputElement.setKind(QueueElement.Kind.E_MINUS);                  
                  assert s.searchedTuple != null;
                  s.outputElement.setTuple(s.searchedTuple);
                  // Set total order flag to false for output minus element
                  // as there is a pending PLUS on the same timestamp value.
                  s.outputElement.setTotalOrderingGuarantee(false);
                }                
              }
              else if(s.inputTuple.getKind() == TupleKind.UPSERT)
              {
                if(s.isPlusProcessed)
                {
                  s.outputElement.setKind(QueueElement.Kind.E_PLUS);
                  s.outputElement.setTuple(s.outputTuple);
                }
                else if(s.searchedTuple != null)
                {
                  s.outputElement.setKind(QueueElement.Kind.E_MINUS);                  
                  s.outputElement.setTuple(s.searchedTuple);
                  // Set total order flag to false for output minus element
                  // as there is a pending PLUS on the same timestamp value.
                  s.outputElement.setTotalOrderingGuarantee(false);
                }
                else
                {
                  s.state = ExecState.S_PROCESSING2;
                  break;
                }
              }
              else
              {
                assert s.inputTuple.getKind() == TupleKind.MINUS : 
                       s.inputTuple.getKind();
                s.outputElement.setKind(QueueElement.Kind.E_MINUS);
                s.outputElement.setTuple(s.outputTuple);
              }
            }
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
              //event id col value is set as tuple id in populateoutput method 
              outTuplePtr.unpinTuple();
            }
            
            //bug 16820093: set snapshotid for heartbeat too.
            //It could be auto heartbeat or requested heartbeat or input hb.
            //Setting snapshot id to current ensures all existing op receive it
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
            // Update last output timestamp value
            s.lastOutputTs = s.outputTs;
            if(s.isUpdateTuple && !s.isPlusProcessed)
            {
              s.state = ExecState.S_PROCESSING2;
              exitState = false;
            } 
            else
            {
              s.outputTuple = null;
              s.state = ExecState.S_INIT;
              exitState = true;
            }            
            break;
            
          default:
            assert false;
        }
        if (done)
          break;
      }
    }
    catch (CEPException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      if (e instanceof SoftExecException)
      {
        s.state = ExecState.S_INIT;
        return 0;
      }
      throw (e);
    }
    finally
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
    try{
     execMgr.removeTableSource(relnId);
     source.end();
    }catch(CEPException ce)
    {
      
    }

  }

  /**
   * Invoked by FIFO Scheduler. Unused Code as we only use DI Scheduler now.
   */
  public long getOldestTs()
  {
    try
    {
      // if there is any input to process, then schedule it at the top
      if (isSilent)
      {
        if (source.hasNext())
        {
          ts = 0;
          return ts;
        }
        else
          return Constants.NULL_TIMESTAMP;
      }
    }
    catch (CEPException e)
    {
      return Constants.NULL_TIMESTAMP;
    }

    long sqlts;
    try
    {
      sqlts = source.getOldestTs();
    }
    catch (CEPException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      return Constants.NULL_TIMESTAMP;
    }
    ts = sqlts;
    return ts;
  }
  
  public boolean canBeScheduled()
  {
    //cannot be scheduled if external source or the operator is already scheduled or executing
    if(isExternalSource || isScheduled.get())
      return false;
    RelSourceState s = (RelSourceState) mut_state;
    if(isSystemTimestamped && (timeoutDuration != -1))
    {
      try
      {
        if (!(source.hasNext()))
        {
          //Assumption: Input Tuple time-stamp in file source is of millisecond
          //time unit and our system's granularity is nanosecond unit of time          
          long diff = System.nanoTime() - s.lastInputTs;
          if(diff < timeoutDuration)
            return false;
        }
      }
      catch(Exception e){}
      return true;
    }
    else 
    {
      try
      { //tuples are remaining or hb needs to be propagated
        return (source.hasNext()||(s.lastInputTs > s.lastOutputTs));
      }
      catch (CEPException e){}
      return true;
    }
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

  public boolean requiresHbtTimeout()
  {
    return timeoutDuration != -1;
  }

  @Override
  public int run(TupleValue input) throws CEPException
  {
    MutableState state = resetMutableState();
    int i = run(input, state);
    commitMutableState(state);
    
    return i; 
  }
  
  /**
   * Concurrent Relation source must override this method.
   * 
   * The reason we need a mutableState is to be able to aggregate
   * the statistics.
   * 
   * @param inputValue
   * @param state
   * @return
   * @throws CEPException
   */
  public int run(TupleValue inputValue, MutableState state) 
    throws CEPException 
  {
    return 0;
  }
  
  @Override
  public void enqueueHeartbeat(Long hbtTime) throws ExecException 
  {
    RelSourceState s = (RelSourceState)mut_state;
    
    // send heartbeat with ordering guarantee false
    s.lastOutputTs = hbtTime;
    s.outputElement.heartBeat(s.lastOutputTs);
    s.outputElement.setTotalOrderingGuarantee(false);
    s.outputElement.setKind(QueueElement.Kind.E_HEARTBEAT);
    //The snapshotid would be the current snapshotid
    s.outputElement.setSnapshotId(execContext.getPlanMgr().getCurrentSnapshotId());
    ((ISharedQueueWriter) outputQueue).enqueue(s.outputElement, 
                                               this.getArchiverReaders());    
    LogUtil.finer(LoggerType.TRACE, "ARF# "+
                 this.getOptName() + " sent heartbeat of "+ s.lastOutputTs + " " + s.outputElement +
                 " with ordering guarantee false (special join)"); 
    
    s.stats.incrNumOutputHeartbeats();    
  }
  
  public void initializeState() throws CEPException
  {
    RelSourceState s = (RelSourceState) mut_state;
    if(archivedRelationTuples != null)
    {
      
      for(ITuplePtr currentTuple : archivedRelationTuples)
      {
        @SuppressWarnings("unused")
        ITuple r = currentTuple.pinTuple(IPinnable.READ);
        
        //Here for an archived relation source, we don't 
        //normally maintain synopsis except when the
        //immediately downstream opeartor wants it to 
        //maintain one. e.g. select min(c1) from R
        if(synopsis != null)
        {
          synopsis.insertTuple(currentTuple);
        }

        s.inputTs = snapShotTime;
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
      s.lastOutputTs = snapShotTime + 1;
      heartbeatTime = snapShotTime + 1;  // needed for "special join"
      s.outputElement.heartBeat(s.lastOutputTs);
      s.outputElement.setTotalOrderingGuarantee(false);
      s.outputElement.setKind(QueueElement.Kind.E_HEARTBEAT);
      s.outputElement.setSnapshotId(execContext.getPlanMgr().getCurrentSnapshotId());

      ((ISharedQueueWriter) outputQueue).enqueue(s.outputElement, 
                                                 this.getArchiverReaders());
      LogUtil.finer(LoggerType.TRACE, "ARF# "+
          this.getOptName()+
          " sent heartbeat of "+ s.lastOutputTs + " with ordering guarantee false");
      s.stats.incrNumOutputs();
      
      LogUtil.finer(LoggerType.TRACE, "ARF# "+
                   "Initialized state of "+this.getOptName()+
                   " and propagated events received from archiver downstream");
      //only if 
      //1. There are archived relation tuples which have been enqueued
      // AND
      //2. Propagation (of old data) is indicated asrequired at this point
      //Set the oldDataPropNeeded to false to avoid duplicate output.
      if((archivedRelationTuples.size() > 0) && (this.propagationReqd()))
        oldDataPropNeeded = false;
      
      //remove all the archived relation tuples.
      archivedRelationTuples.clear();
    }
    initializedState = true;
    LogUtil.finer(LoggerType.TRACE, this.getOptName()+ " set initializedState to true.");
  }
  
  /**
   * Create snapshot of Relation Source (RelSource) operator by writing the operator state into output stream.
   * State of RelSource operator consists of following:
   * 1. Mutable State (RelSourceState)
   * 2. synopsis
   * 
   * Please note that we will write the state of operator in above sequence, so
   * the loadSnapshot should also read the state in the same sequence.
   * @param output
   * @throws CEPException 
   */
  @Override
  protected void createSnapshot(ObjectOutputStream output) throws CEPException
  {
    try
    {
      //snapshot mutable state
      output.writeObject((RelSourceState)mut_state);
      //snapshot state for relation propagation
      output.writeLong(maxTime);
      output.writeLong(ts);
      output.writeBoolean(oldDataPropNeeded);
      
      if (SnapshotContext.getVersion() >= SnapshotContext.SOURCEOP_TUPID_VERSION)
      {
        // Save nextTupleId so that post recovery the new tuples will
        // start using this tuple id onwards
        output.writeLong(Tuple.getNextTupleId());
      }
      
      //snapshot synopsis.
      if(!isExternalSource)
        synopsis.writeExternal(output, new SynopsisPersistenceContext(fullScanId));
    } catch (IOException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE,e);
      throw new CEPException(ExecutionError.SNAPSHOT_CREATION_ERROR, e, e.getLocalizedMessage(), this.getOptName());
    }
  }
  
  @Override
  protected void loadSnapshot(ObjectInputStream input) throws ExecException
  {
    try
    {
      long stTime = 0;
      if(LogUtil.isFinerEnabled(LoggerType.TRACE)){
        stTime = System.currentTimeMillis();
        LogUtil.fine(LoggerType.TRACE,"started loadSnapshot of RelSource");
      }
      //read mutable state
      RelSourceState mutable_state = (RelSourceState)input.readObject();
      ((RelSourceState)mut_state).copyFrom(mutable_state);
      //read state for relation propagation
      maxTime = input.readLong();
      ts = input.readLong();
      oldDataPropNeeded = input.readBoolean();
      
      if (SnapshotContext.getVersion() >= SnapshotContext.SOURCEOP_TUPID_VERSION)
      {        
        // Load nextTupleId and set this in operator
        long loaded_nextTupleId = input.readLong(); 
        this.setMinTupleId(loaded_nextTupleId);
        
        // Mark operator to restore to a tuple id higher than loaded tuple id
        // to prevent allocating a tuple with older tuple ids.
        this.setRestoreTupleId(true);
        Tuple.setNextTupleId(loaded_nextTupleId);
      }
      //read synopsis
      if(!isExternalSource)
      {
        IPersistenceContext persistenceContext = new SynopsisPersistenceContext();
        persistenceContext.setCache(new HashSet());
        persistenceContext.setScanId(fullScanId);
        // If Synopsis represent a silent relation
        // then the underlying relstore might have been
        // restored by other synopsis sharing same store.
        persistenceContext.setSilent(isSilent);
        synopsis.readExternal(input,persistenceContext);
      }
      if(LogUtil.isFinerEnabled(LoggerType.TRACE)){
        LogUtil.fine(LoggerType.TRACE,"Ended loadSnapshot of RelSource time taken(ms)=" + (System.currentTimeMillis() - stTime));
      }
    } catch (ClassNotFoundException | IOException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE,e);
      throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR, e, e.getLocalizedMessage(), getOptName());
    }
  }  
}
