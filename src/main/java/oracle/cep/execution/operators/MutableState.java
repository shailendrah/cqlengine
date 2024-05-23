/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/MutableState.java hopark_cqlsnapshot/3 2016/02/26 11:55:07 hopark Exp $ */

/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares MutableState in package oracle.cep.execution.operators.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 sbishnoi  07/10/13 - invoking init method on stat
 udeshmuk  01/24/13 - add ARCHIVED_SIA_STARTED state
 anasrini  08/12/11 - use getFixedFactory
 anasrini  08/10/11 - check isStatsEnabled
 udeshmuk  08/17/09 - make stats and lastoutputts public
 sbishnoi  04/13/09 - adding lastInputKind
 sbishnoi  04/03/09 - initializing timestamp variables to
                      Constant.MIN_EXEC_TIME
 hopark    10/10/08 - remove statics
 sbishnoi  06/26/08 - support for pending hbt generation, added lastOutputTs
 hopark    06/19/08 - logging refactor
 hopark    02/06/08 - fix auto fields
 hopark    01/31/08 - queue optimization
 hopark    12/26/07 - use DumpDesc
 hopark    11/27/07 - add operator specific dump
 hopark    10/22/07 - remove TimeStamp
 parujain  06/26/07 - single mutable state
 najain    03/14/07 - cleanup
 najain    03/12/07 - bug fix
 najain    01/05/07 - spill over support
 parujain  12/07/06 - propagating relation
 skaluska  03/26/06 - implementation
 anasrini  03/24/06 - bug fix JNPE (AtomicBoolean) 
 skaluska  03/14/06 - query manager 
 skaluska  03/03/06 - Creation
 skaluska  03/03/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/MutableState.java hopark_cqlsnapshot/3 2016/02/26 11:55:07 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.BitSet;
import java.util.HashMap;
import java.util.logging.Level;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.QueueElement.Kind;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.snapshot.IPersistable;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.DumpDesc;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.service.ExecContext;

/**
 * Mutable State for an operator There is one instance per thread executing an
 * operator
 *
 * @author skaluska
 */
@DumpDesc(autoFields=true)
public class MutableState implements IPersistable
{
  /** Execution state */
  ExecState     state;
  /** stats accumulated during this run */
  public ExecStats stats;
  /** Last Execution state */
  ExecState     lastState;
  /** current tuple in process */
  ITuplePtr      tup;
  /** Current Iterator */
  @DumpDesc(ignore=true) TupleIterator scan;
  
  /** Reader ids. being processed currently */
  BitSet            readerIds;
  
  /** timestamp of last input tuple */
  long              lastInputTs;
  
  /** timestamp of last output tuple*/
  public long       lastOutputTs;
  
  /** kind of last input tuple */
  QueueElement.Kind lastInputKind;

  
  // Relation propagation state
  static enum PropState
  {
    // propagate old data (classic relation propagation, i.e., non-archived
    S_PROPAGATE_INIT, 
    S_PROP_RELN_NEXT_READER, 
    S_PROP_RELN_READER_GET_SCAN, 
    S_PROP_RELN_READER_GET_NEXT, 
    S_PROP_RELN_READER_PROC_TUPLE,
    // archived relation, used in join with slow changing dimension
    // Operators currently impacted: RelSource, StreamSource and BufferOp.
    // #(16196625) 
    // Also used in ExecOpt to bypass requesting heartbeats when we are in
    // query start phase.
    S_ARCHIVED_SIA_DONE,
    S_ARCHIVED_SIA_STARTED;

  };

  /** propagation state */
  PropState propState;

  protected IAllocator<QueueElement> queueElemFac;
  
  protected static HashMap<String, Boolean> s_ignoreMap;
  static {
    s_ignoreMap = new HashMap<String, Boolean>();
    s_ignoreMap.put("scan", true);
    
  }

  public QueueElement allocQueueElement()
  {
    try
    {
      return queueElemFac.allocate();
    }
    catch (ExecException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }
    return null;
  }
  
  /**
   * Empty Constructor is used while deserialization of state object
   */
  public MutableState()
  {}
  
  /**
   * Constructor for MutableState
   * @param ec ExecContext for this CEPService
   * @param isStatsEnabled is statistics gathering enabled
   */
  MutableState(ExecContext ec, boolean isStatsEnabled)
  {
    state = ExecState.S_UNINIT;
    lastInputTs  = Constants.MIN_EXEC_TIME;
    lastOutputTs = Constants.MIN_EXEC_TIME;

    if (isStatsEnabled)
      stats = new ExecStats();

    readerIds = new BitSet();
    propState = PropState.S_PROPAGATE_INIT;
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    queueElemFac = 
      factoryMgr.getFixedFactory(FactoryManager.QUEUEELEMENT_FACTORY_ID);
    lastInputKind = null;
  }

  /**
   * Constructor for MutableState
   * @param ec ExecContext for this CEPService
   */
  MutableState(ExecContext ec)
  {
    // Default case - continue to collect statisctics
    // since it requires a lot of change in the ORDERED operators
    // to go by the setting of the isStatsEnabled config param
    this(ec, true);
  }


  /**
   * Reserve MutableState
   * 
   * @return true if successful else false
   */
  void reset()
  {
    if (stats != null)
    {
      stats.clear();
      // Set start time
      stats.setStartTime(System.nanoTime());
    }
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {    
    out.writeLong(lastInputTs);
    out.writeLong(lastOutputTs);
    out.writeObject(lastInputKind);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    lastInputTs = in.readLong();
    lastOutputTs = in.readLong();
    lastInputKind = (Kind) in.readObject();
  }
  
  public void copyFrom(MutableState other)
  {
    this.lastInputTs = other.lastInputTs;
    this.lastOutputTs = other.lastOutputTs;
    this.lastInputKind = other.lastInputKind;
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
  public String toString()
  {
    /*return "MutableState [state=" + state + ", stats=" + stats + ", lastState="
        + lastState + ", tup=" + tup + ", scan=" + scan + ", readerIds="
        + readerIds + ", lastInputTs=" + lastInputTs + ", lastOutputTs="
        + lastOutputTs + ", lastInputKind=" + lastInputKind + ", propState="
        + propState + ", queueElemFac=" + queueElemFac + "]";*/
    return "MutableState[lastInputTs=" + lastInputTs + ", lastOutputTs=" + lastOutputTs 
           + ", lastInputKind=" + lastInputKind + "]"; 
  }
}
