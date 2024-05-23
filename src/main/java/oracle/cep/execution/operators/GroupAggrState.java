/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/GroupAggrState.java hopark_cqlsnapshot/3 2016/02/26 11:55:07 hopark Exp $ */

/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Mutable state for the GROUP/AGGREGATION operator

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    04/09/09 - changing timestamp variables to use
                        Constant.MIN_EXEC_TIME
 sbishnoi    01/19/09 - adding flag for total order optimization check
 hopark      10/10/08 - remove statics
 skmishra    08/11/08 - adding orderByIndexTuple
 sbishnoi    06/26/08 - moving lastOutputTs into MutableState
 hopark      01/31/08 - queue optimization
 hopark      12/26/07 - use DumpDesc
 hopark      11/27/07 - add operator specific dump
 hopark      10/30/07 - remove IQueueElement
 hopark      10/22/07 - remove TimeStamp
 sbishnoi    09/26/07 - support for dirtySyn
 sbishnoi    07/24/07 - add DirtyOutputState
 najain      03/14/07 - cleanup
 najain      03/12/07 - bug fix
 najain      01/05/07 - spill over support
 rkomurav    12/17/06 - add isGroupEmpty flag
 parujain    12/08/06 - propagating relations
 rkomurav    12/07/06 - add isZeroCountEmitted flag
 najain      08/31/06 - bug fix: heartbeats not handled
 anasrini    07/18/06 - support for user defined aggregations 
 najain      07/13/06 - ref-count timestamps 
 anasrini    05/30/06 - Creation
 anasrini    05/30/06 - Creation
 anasrini    05/30/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/GroupAggrState.java hopark_cqlsnapshot/3 2016/02/26 11:55:07 hopark Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.logging.DumpDesc;
import oracle.cep.service.ExecContext;
import oracle.cep.dataStructures.internal.QueueElement;

/**
 * Mutable state for the GROUP/AGGREGATION operator
 *
 * @author anasrini
 * @since 1.0
 */

public class GroupAggrState extends MutableState
{

  // Related to input
  /** Timestamp of current input element */
  long      inputTs;

  /** Current input element */
  QueueElement   inputElement;
  @DumpDesc(ignore=true) QueueElement    inputElementBuf;

  ITuplePtr  inputTuple;

  // Related to output
  /** timestamp of current output element being prepared */
  long      outputTs;

  /** current output element being prepared */
  QueueElement    outputElement;

  /** current output tuple being prepared */
  ITuplePtr  outputTuple;

  /** old aggregation tuple for the group */
  ITuplePtr  oldAggrTuple;

  /** tuple to store order by exprs, used by xmlagg */
  ITuplePtr orderByTuple;
  
  /** flag to maintain if the initial null row is emitted */
  boolean   isInitNullRowEmitted;

  /** if group is empty or not */
  boolean   isGroupEmpty;

  // Related to user defined aggregations
  /** The index of the UDA that should be processsed next */
  int       nextUDAIndex;
  
  /** flag to check whether current Group is dirty or not */
  boolean   isDirty;
  
  /** flag to check whether next input tuple will come with higher value of 
   * time-stamp. Always initialize before using
   */
  boolean isTotalOrderingGuarantee;
  
  /** totalOrderingGuarantee flag for next output tuple*/
  boolean nextTupleTotalOrderingGuarantee;
  
  /** totalOrdering flag for last input tuple*/
  boolean  lastTotalOrderingGuarantee;
  
  /** count of the number of tuples in dirty Synopsis*/
  long    dirtyTupleCount;

  /** minimum expected timestamp of next input tuple*/
  long    minNextTs;

  // States
  public static enum PlusState
  {
    S_INIT, S_ORDER_BY_TUPLE, S_GET_OUTPUT_SCAN, S_RELEASE_OUTPUT_SCAN, S_GROUP_EXISTS, S_NEW_GROUP, S_ALLOC_AGGR_HANDLERS, S_INIT_NEW_GROUP, S_INSERT_INTO_OUT_SYNOPSIS_1, S_INSERT_INTO_OUT_SYNOPSIS_2, S_DELETE_FROM_OUT_SYNOPSIS, S_PREPARE_PLUS_ELEMENT_1, S_PREPARE_PLUS_ELEMENT_2, S_ENQUEUE_PLUS_1, S_ENQUEUE_PLUS_2, S_RESET_AGGR_HANDLERS, S_PREPARE_MINUS_ELEMENT, S_ENQUEUE_MINUS, S_UPDATE_INPUT_SYN, S_FINISHED, S_CHECK_IS_DIRTY, S_CHECK_IS_DIRTY_1, S_PROCESSING_1, S_PROCESS_IS_TOTAL_ORDERING_FLAG_1
  };

  public static enum MinusState
  {
    S_INIT, S_UPDATE_INPUT_SYN, S_GET_OUTPUT_SCAN, S_RELEASE_OUTPUT_SCAN, S_CHECK_EMPTY_GROUP, S_EMIT_NULL_OUTPUT, S_BIND_NULL_OUTPUT_TUPLE, S_EMPTY_GROUP, S_RELEASE_AGGR_HANDLERS, S_ACTIVE_GROUP, S_RESET_AGGR_HANDLERS_1, S_RESET_AGGR_HANDLERS_2, S_PREPARE_MINUS_ELEMENT_1, S_PREPARE_MINUS_ELEMENT_2, S_ENQUEUE_MINUS_1, S_ENQUEUE_MINUS_2, S_BIND_OUTPUT_TUPLE, S_RESCAN_INPUT_SYN, S_RESCAN_INPUT_SYN_1, S_RESCAN_INPUT_SYN_2, S_RELEASE_INPUT_SCAN, S_INSERT_INTO_OUT_SYNOPSIS, S_DELETE_FROM_OUT_SYNOPSIS, S_PREPARE_PLUS_ELEMENT, S_ENQUEUE_PLUS, S_RESET_DIRTY_TUPLE, S_CHECK_IS_DIRTY, S_PROCESSING_1, S_PROCESSING_2, S_FINISHED
  };

  public static enum DirtyOutputState
  {
    S_INIT, S_RESCAN_INPUT_SYN, S_OUTPUT_DIRTY_TUPLE, S_OUTPUT_ELEMENT, S_OUTPUT_TIMESTAMP, S_ENQUEUE_PLUS, S_OUTPUT_READY, S_SET_TOTAL_ORDERING_FLAG, S_FINISHED
  };
  
  PlusState        plusState;

  MinusState       minusState;
  
  DirtyOutputState dirtyOutputState;

  ExecState        oldState;

  // Related to Synopsis scans
  @DumpDesc(ignore=true) TupleIterator outTupleIter;

  @DumpDesc(ignore=true) TupleIterator inTupleIter;

  ITuplePtr     inTuple;
  
  // Related to Synopsis Scan required for Output of dirty tuples
  ITuplePtr     outDirtyTuple;
  
  // TuplePtr points to next tuple in iterator of dirtySynopsis
  ITuplePtr     nextOutDirtyTuple;
  
  @DumpDesc(ignore=true) TupleIterator outDirtyTupleIter;
  
  // Related to dirty Synopsis Scan required to check whether
  // current group is dirty or not
  // dirtyTuple = null derives that given group is not dirty
  @DumpDesc(ignore=true) TupleIterator dirtyTupleIter;
  
  ITuplePtr     dirtyTuple;

  /**
   * Constructor for GroupAggrState
   * @param ec TODO
   */
  public GroupAggrState(ExecContext ec)
  {
    /* MutableState constructor will initialize lastInputTs and lastOutputTs */
    super(ec);
    inputElementBuf = allocQueueElement();
    outputElement = allocQueueElement();
    
    state            = ExecState.S_INIT;
    oldState         = ExecState.S_INIT;
    plusState        = PlusState.S_INIT;
    minusState       = MinusState.S_INIT;
    dirtyOutputState = DirtyOutputState.S_INIT;

    isInitNullRowEmitted = false;
    isGroupEmpty = false;
    nextUDAIndex = 0;
    isDirty      = false;

    //initialize timestamp variables
    inputTs      = Constants.MIN_EXEC_TIME;
    outputTs     = Constants.MIN_EXEC_TIME;
    minNextTs    = Constants.MIN_EXEC_TIME;

    isTotalOrderingGuarantee        = false;
    nextTupleTotalOrderingGuarantee = false;
    dirtyTupleCount                 = 0;
    lastTotalOrderingGuarantee      = false;
  }
  
  public GroupAggrState()
  {
    super();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    super.writeExternal(out);
    out.writeLong(inputTs);    
    out.writeLong(outputTs);
    out.writeBoolean(isInitNullRowEmitted);
    out.writeBoolean(lastTotalOrderingGuarantee);
    out.writeLong(dirtyTupleCount);
    out.writeLong(minNextTs);
    out.writeObject(plusState);
    out.writeObject(minusState);
    out.writeObject(dirtyOutputState);
    out.writeObject(oldState);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
     super.readExternal(in);
     inputTs = in.readLong();
     outputTs = in.readLong();
     isInitNullRowEmitted = in.readBoolean();
     lastTotalOrderingGuarantee = in.readBoolean();
     dirtyTupleCount = in.readLong();
     minNextTs = in.readLong();
     plusState = (PlusState) in.readObject();
     minusState = (MinusState) in.readObject();
     dirtyOutputState = (DirtyOutputState) in.readObject();
     oldState = (ExecState) in.readObject();
  }
  
  public void copyFrom(GroupAggrState other)
  {
    super.copyFrom(other);
    inputTs = other.inputTs;
    outputTs = other.outputTs;
    isInitNullRowEmitted = other.isInitNullRowEmitted;
    lastTotalOrderingGuarantee = other.lastTotalOrderingGuarantee;
    dirtyTupleCount = other.dirtyTupleCount;
    minNextTs = other.minNextTs;
    plusState = other.plusState;
    minusState = other.minusState;
    dirtyOutputState = other.dirtyOutputState;
    oldState = other.oldState;
  }
  
  @Override
  public String toString()
  {
    return super.toString() + "; " + "GroupAggrState [inputTs=" 
        + inputTs + ", outputTs=" + outputTs +", plusState=" + plusState
        + ", minusState=" + minusState + ", dirtyOutputState="  + dirtyOutputState 
        + ", oldState=" + oldState + ", isInitNullRowEmitted=" + isInitNullRowEmitted +
        ", dirtyTupleCount=" + dirtyTupleCount +  ", minNextTs=" + minNextTs +
        ", lastTotalOrderingGuarantee="   + lastTotalOrderingGuarantee; 
         
    /*return super.toString() + ";" + "GroupAggrState [inputTs=" + inputTs + ", inputElement="
        + inputElement + ", inputTuple=" + inputTuple + ", outputTs="
        + outputTs + ", outputElement=" + outputElement + ", outputTuple="
        + outputTuple + ", oldAggrTuple=" + oldAggrTuple + ", orderByTuple="
        + orderByTuple + ", isInitNullRowEmitted=" + isInitNullRowEmitted
        + ", isGroupEmpty=" + isGroupEmpty + ", nextUDAIndex=" + nextUDAIndex
        + ", isDirty=" + isDirty + ", isTotalOrderingGuarantee="
        + isTotalOrderingGuarantee + ", nextTupleTotalOrderingGuarantee="
        + nextTupleTotalOrderingGuarantee + ", lastTotalOrderingGuarantee="
        + lastTotalOrderingGuarantee + ", dirtyTupleCount=" + dirtyTupleCount
        + ", minNextTs=" + minNextTs + ", plusState=" + plusState
        + ", minusState=" + minusState + ", dirtyOutputState="
        + dirtyOutputState + ", oldState=" + oldState + ", outTupleIter="
        + outTupleIter + ", inTupleIter=" + inTupleIter + ", inTuple="
        + inTuple + ", outDirtyTuple=" + outDirtyTuple + ", nextOutDirtyTuple="
        + nextOutDirtyTuple + ", outDirtyTupleIter=" + outDirtyTupleIter
        + ", dirtyTupleIter=" + dirtyTupleIter + ", dirtyTuple=" + dirtyTuple
        + "]";*/
  }

}
