/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/BinJoinState.java /main/16 2013/06/11 08:46:11 udeshmuk Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    05/22/13 - add prevOuterSnapshotId and prevInnerSnapshotId
    sbishnoi    05/11/09 - removing unused variables
    udeshmuk    04/13/09 - add lastLeftKind and lastRightKind
    parujain    03/23/09 - total ordering ts opt
    hopark      10/10/08 - remove statics
    sbishnoi    06/27/08 - moving lastOutputTs to MutableState
    hopark      01/31/08 - queue optimization
    hopark      12/26/07 - use DumpDesc
    parujain    12/05/07 - operator logging
    hopark      11/27/07 - add operator specific dump
    hopark      10/30/07 - remove IQueueElement
    hopark      10/22/07 - remove TimeStamp
    najain      03/14/07 - cleanup
    najain      03/12/07 - bug fix
    najain      01/05/07 - spill over support
    rkomurav    11/12/06 - add tmpElementKind
    najain      05/23/06 - bug fix 
    najain      04/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/BinJoinState.java /main/16 2013/06/11 08:46:11 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Hashtable;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement.Kind;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.logging.DumpDesc;
import oracle.cep.service.ExecContext;

/**
 * BinJoinState
 *
 * @author najain
 */
public class BinJoinState extends MutableState
{
    /** Timestamp of the last element dequeued from the outer */
  long       lastOuterTs;
  /** Timestamp of the last element dequeued from the inner */
  long       lastInnerTs;

  /** Timestamp of elements dequeued from the inner/outer queues */
  long       outerTs;
  long       innerTs;

  /** minimum timestamp of inner and outer queues */
  long       outerMinTs;
  long       innerMinTs;
  
  /** mininum expected next timestamp of inner and outer */
  long       minNextOuterTs;
  long       minNextInnerTs;

  QueueElement outerElement;
  @DumpDesc(ignore=true) QueueElement outerElementBuf;
  QueueElement innerElement;
  @DumpDesc(ignore=true) QueueElement innerElementBuf;

  /** next output element */
  QueueElement     outputElement;

  /** Iterator that scans the outer */
  @DumpDesc(ignore=true) TupleIterator outerScan;

  /** Iterator that scans the inner */
  @DumpDesc(ignore=true) TupleIterator innerScan;

  /** Tuple of outer that joins with innerElement.tuple */
  ITuplePtr outerTuple;

  /** Tuple of inner that joins with outerElement.tuple */
  ITuplePtr innerTuple;

  /** Joined + possibly projected output tuple */
  ITuplePtr outputTuple;

  ExecState tmpState;
  
  ITuplePtr tmpOuterTuple;
  ITuplePtr tmpInnerTuple;

  @DumpDesc(ignore=true) QueueElement outerPeekElementBuf;
  
  @DumpDesc(ignore=true) QueueElement innerPeekElementBuf;

  long      nextOutputTs;
  long      outputTs;
  QueueElement.Kind nextElementKind;
  
  QueueElement.Kind tmpElementKind;

  QueueElement.Kind lastLeftKind;
  QueueElement.Kind lastRightKind;
  
  /** count of no. of inner matches */
  int outerMatchCount;
  
  /** count of no. of outer matches */
  int innerMatchCount;
  
  /** hash table to maintain (outer-tuple,count of matched inner tuples) */
  @DumpDesc(ignore=true) Hashtable<ITuplePtr,Integer>   outerMatchHash;
  
  /** hash table to maintain (inner-tuple,count of matched outer tuples) */
  @DumpDesc(ignore=true) Hashtable<ITuplePtr,Integer>   innerMatchHash;
  
  /** is the inner Scan empty */
  boolean innerScanEmpty;
  
  /** is the outer Scan empty */
  boolean outerScanEmpty;
  
  /** whether to output (-ve)(null,inner) */
  boolean outputNegNullInner;
  
  /** whether to output (+ve)(null,inner) */
  boolean outputPosNullInner;
  
  /** whether to output (-ve)(outer,null) */
  boolean outputNegOuterNull;
  
  /** whether to output (+ve)(outer,null) */
  boolean outputPosOuterNull;
  
  /* total ordering flag in the QueueElement corresponding to output */
  boolean           nextOutputOrderingFlag;
  
  ITuplePtr         nextScannedTuple;
  
  /** snapshotId of recently consumed outer input */
  long prevOuterSnapshotId = Long.MAX_VALUE;
  
  /** snapshotId of recently consumed inner input */
  long prevInnerSnapshotId = Long.MAX_VALUE;
  
  /**
   * Zero Argument Constructor
   * Invoked while deserializing instances of BinJoinState type
   */
  public BinJoinState()
  {
    super();
  }
  
  /**
   * Constructor for BinJoinState
   * @param ec TODO
   */
  public BinJoinState(ExecContext ec)
  {
    /* MutableState constructor will initialize lastInputTs and lastOutputTs */ 
    super(ec);
    lastInnerTs    = Constants.MIN_EXEC_TIME;
    lastOuterTs    = Constants.MIN_EXEC_TIME;
    innerTs        = Constants.MIN_EXEC_TIME;
    outerTs        = Constants.MIN_EXEC_TIME;
    outerMinTs     = Constants.MIN_EXEC_TIME;
    innerMinTs     = Constants.MIN_EXEC_TIME;    
    minNextOuterTs = Constants.MIN_EXEC_TIME;
    minNextInnerTs = Constants.MIN_EXEC_TIME;
    nextOutputTs   = Constants.MIN_EXEC_TIME;
    outputTs       = Constants.MIN_EXEC_TIME;
   
    lastLeftKind   = null;
    lastRightKind  = null;

    outerElementBuf = allocQueueElement();
    innerElementBuf = allocQueueElement();
    outerPeekElementBuf = allocQueueElement();
    innerPeekElementBuf = allocQueueElement();
    outputElement = allocQueueElement();
    tmpElementKind = null;
    state = ExecState.S_INIT;
    outputNegNullInner = false;
    outputNegOuterNull = false;
    outputPosNullInner = false;
    outputPosOuterNull = false;
    outerMatchHash     = new Hashtable<ITuplePtr,Integer>();
    innerMatchHash     = new Hashtable<ITuplePtr,Integer>();
    nextOutputOrderingFlag = false; //default is false
    nextScannedTuple = null;
  }
  
  /**
   * Write selected instance variables to output stream.
   */
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    super.writeExternal(out);
    out.writeLong(lastOuterTs);
    out.writeLong(lastInnerTs);
    out.writeLong(innerTs);
    out.writeLong(outerTs);
    out.writeLong(minNextInnerTs);
    out.writeLong(minNextOuterTs);
    out.writeLong(nextOutputTs);
    out.writeBoolean(outputNegNullInner);
    out.writeBoolean(outputNegOuterNull);
    out.writeBoolean(outputPosNullInner);
    out.writeBoolean(outputPosOuterNull);
    out.writeBoolean(nextOutputOrderingFlag);
    out.writeInt(innerMatchCount);
    out.writeInt(outerMatchCount);
    out.writeLong(prevInnerSnapshotId);
    out.writeLong(prevOuterSnapshotId);
    out.writeObject(lastLeftKind);
    out.writeObject(lastRightKind);
    out.writeObject(tmpElementKind);
    out.writeObject(outerMatchHash);
    out.writeObject(innerMatchHash);    
  }

  /**
   * Read selected instance variables from input stream.
   */
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
     super.readExternal(in); 
     lastOuterTs = in.readLong();
     lastInnerTs = in.readLong();
     innerTs = in.readLong();
     outerTs = in.readLong();
     minNextInnerTs = in.readLong();
     minNextOuterTs = in.readLong();
     nextOutputTs = in.readLong();
     outputNegNullInner = in.readBoolean();
     outputNegOuterNull = in.readBoolean();
     outputPosNullInner = in.readBoolean();
     outputPosOuterNull = in.readBoolean();
     nextOutputOrderingFlag = in.readBoolean();
     innerMatchCount = in.readInt();
     outerMatchCount = in.readInt();
     prevInnerSnapshotId = in.readLong();
     prevOuterSnapshotId = in.readLong();
     lastLeftKind = (Kind) in.readObject();
     lastRightKind = (Kind) in.readObject();
     tmpElementKind = (Kind) in.readObject();     
     outerMatchHash = (Hashtable<ITuplePtr, Integer>) in.readObject();
     innerMatchHash = (Hashtable<ITuplePtr, Integer>) in.readObject();     
  }
  
  /**
   * Copy the state from other state variable.
   * Used to load state from snapshot.
   * @param other
   */
  public void copyFrom(BinJoinState other)
  {
    super.copyFrom(other);
    this.lastOuterTs = other.lastOuterTs;
    this.lastInnerTs = other.lastInnerTs;
    this.innerTs = other.innerTs;
    this.outerTs = other.outerTs;
    this.minNextInnerTs = other.minNextInnerTs;
    this.minNextOuterTs = other.minNextOuterTs;
    this.nextOutputTs = other.nextOutputTs;
    this.lastLeftKind = other.lastLeftKind;
    this.lastRightKind = other.lastRightKind;
    this.tmpElementKind = other.tmpElementKind;
    this.outputNegNullInner = other.outputNegNullInner;
    this.outputNegOuterNull = other.outputNegOuterNull;
    this.outputPosNullInner = other.outputPosOuterNull;
    this.outputPosOuterNull = other.outputPosOuterNull;
    this.outerMatchHash = other.outerMatchHash;
    this.innerMatchHash = other.innerMatchHash;
    this.nextOutputOrderingFlag = other.nextOutputOrderingFlag;
    this.innerMatchCount = other.innerMatchCount;
    this.outerMatchCount = other.outerMatchCount;
    this.prevInnerSnapshotId = other.prevInnerSnapshotId;
    this.prevOuterSnapshotId = other.prevOuterSnapshotId;
  }

  /**
   * Return the string representation for binary join state
   */
  @Override
  public String toString()
  {
    return "BinJoinState [lastOuterTs=" + lastOuterTs + ", lastInnerTs="
        + lastInnerTs + ", outerTs=" + outerTs + ", innerTs=" + innerTs
        + ", minNextOuterTs=" + minNextOuterTs + ", minNextInnerTs="
        + minNextInnerTs + ", outerElement=" + outerElement + ", innerElement="
        + innerElement + ", outerTuple=" + outerTuple + ", innerTuple="
        + innerTuple + ", outputTuple=" + outputTuple + ", tmpState="
        + tmpState + ", tmpOuterTuple=" + tmpOuterTuple + ", tmpInnerTuple="
        + tmpInnerTuple + ", nextOutputTs=" + nextOutputTs + ", outputTs="
        + outputTs + ", nextElementKind=" + nextElementKind
        + ", tmpElementKind=" + tmpElementKind + ", lastLeftKind="
        + lastLeftKind + ", lastRightKind=" + lastRightKind
        + ", outerMatchCount=" + outerMatchCount + ", innerMatchCount="
        + innerMatchCount + ", outerMatchHash=" + outerMatchHash
        + ", innerMatchHash=" + innerMatchHash + ", innerScanEmpty="
        + innerScanEmpty + ", outerScanEmpty=" + outerScanEmpty
        + ", outputNegNullInner=" + outputNegNullInner
        + ", outputPosNullInner=" + outputPosNullInner
        + ", outputNegOuterNull=" + outputNegOuterNull
        + ", outputPosOuterNull=" + outputPosOuterNull
        + ", nextOutputOrderingFlag=" + nextOutputOrderingFlag
        + ", nextScannedTuple=" + nextScannedTuple + ", prevOuterSnapshotId="
        + prevOuterSnapshotId + ", prevInnerSnapshotId=" + prevInnerSnapshotId
        + "]";
  }

  /**
   * Return the string representation of the BinJoinState with only those
   * instance variables which are stored in snapshot for HA.
   * @return
   */
  public String toDumpString()
  {
    return "BinJoinState [lastOuterTs=" + lastOuterTs + ", lastInnerTs="
        + lastInnerTs + ", outerTs=" + outerTs + ", innerTs=" + innerTs
        + ", minNextOuterTs=" + minNextOuterTs + ", minNextInnerTs="
        + minNextInnerTs + ", outerElement=" + outerElement + ", innerElement="
        + innerElement + ", outerTuple=" + outerTuple + ", innerTuple="
        + innerTuple + ", nextOutputTs=" + nextOutputTs + ", outputTs="
        + outputTs   + ", tmpElementKind=" + tmpElementKind + ", lastLeftKind="
        + lastLeftKind + ", lastRightKind=" + lastRightKind
        + ", outerMatchCount=" + outerMatchCount + ", innerMatchCount="
        + innerMatchCount + ", outerMatchHash=" + outerMatchHash
        + ", innerMatchHash=" + innerMatchHash 
        + ", outputNegNullInner=" + outputNegNullInner
        + ", outputPosNullInner=" + outputPosNullInner
        + ", outputNegOuterNull=" + outputNegOuterNull
        + ", outputPosOuterNull=" + outputPosOuterNull
        + ", nextOutputOrderingFlag=" + nextOutputOrderingFlag +
        ", prevOuterSnapshotId="  + prevOuterSnapshotId +
        ", prevInnerSnapshotId=" + prevInnerSnapshotId
        + "]";
  }

}

