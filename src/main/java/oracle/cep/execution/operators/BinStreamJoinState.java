/* $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/BinStreamJoinState.java /main/15 2009/04/27 10:18:28 udeshmuk Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    04/23/09 - add lastEffectiveOutputTs
    udeshmuk    04/13/09 - add lastLeftKind, lastRightKind
    sbishnoi    04/09/09 - adding two state variables minNextInnerTs and
                           minNextOuterTs
    udeshmuk    03/25/09 - total ordering ts
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
    najain      05/26/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/BinStreamJoinState.java /main/15 2009/04/27 10:18:28 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement.Kind;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.logging.DumpDesc;
import oracle.cep.service.ExecContext;
import oracle.cep.dataStructures.internal.QueueElement;

/**
 * BinStreamJoinState
 *
 * @author najain
 */
public class BinStreamJoinState extends MutableState
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

  QueueElement  outerElement;
  
  @DumpDesc(ignore=true) 
  QueueElement  outerElementBuf;
  
  QueueElement  innerElement;
  
  @DumpDesc(ignore=true) 
  QueueElement  innerElementBuf;

  /** next output element */
  QueueElement  outputElement;

  /** Iterator that scans the inner */
  @DumpDesc(ignore=true)
  TupleIterator innerScan;

  /** Tuple of outer that joins with innerElement.tuple */
  ITuplePtr     outerTuple;

  /** Tuple of inner that joins with outerElement.tuple */
  ITuplePtr     innerTuple;

  /** Joined + possibly projected output tuple */
  ITuplePtr     outputTuple;
  
  QueueElement  outerPeekElement;
  
  @DumpDesc(ignore=true) QueueElement  outerPeekElementBuf;
  
  QueueElement  innerPeekElement;
  
  @DumpDesc(ignore=true) QueueElement  innerPeekElementBuf;

  QueueElement.Kind lastLeftKind;
  QueueElement.Kind lastRightKind;

  long          outputTs;
  
  /** totalOrdering flag of last input received */
  boolean       lastInputOrderingFlag;
  
  /** minimum possible timeStamp for next inner(right) queue element */
  long          minNextInnerTs;
  
  /** minimum possible timeSTamp for next outer(left) queue element*/
  long          minNextOuterTs;

  /** 
   * This indicates the last "Effective" timestamp communicated downstream.
   * This becomes meaningful in conjunction with the piggybacking feature.
   * For example, if lastOutputTs was 10 and orderingGuarantee flag was set,
   * then although there was no explicit tuple sent with time 11, 11 is the
   * "Effective" timestamp communictaed downstream
   */
  long          lastEffectiveOutputTs;

  /**
   * Zero Argument Constructor
   * Invoked while deserializing instances of BinStreamJoinState type
   */
  public BinStreamJoinState()
  {
    super();
  }
  
  /**
   * Constructor for BinStreamJoinState
   * @param ec TODO
   */
  public BinStreamJoinState(ExecContext ec)
  {
    /* MutableState constructor will initialize lastOutputTs and lastInputTs */
    super(ec);    
    lastOuterTs           = Constants.MIN_EXEC_TIME;
    lastInnerTs           = Constants.MIN_EXEC_TIME;
    outerTs               = Constants.MIN_EXEC_TIME;
    innerTs               = Constants.MIN_EXEC_TIME;    
    minNextOuterTs        = Constants.MIN_EXEC_TIME;
    minNextInnerTs        = Constants.MIN_EXEC_TIME;
    lastInputOrderingFlag = false;
    outerElementBuf       = allocQueueElement();
    innerElementBuf       = allocQueueElement();
    outerPeekElementBuf   = allocQueueElement();
    innerPeekElementBuf   = allocQueueElement();
    outputElement         = allocQueueElement();
    minNextInnerTs        = Constants.MIN_EXEC_TIME;
    minNextOuterTs        = Constants.MIN_EXEC_TIME;
    
    outputTs              = Constants.MIN_EXEC_TIME;
    outerMinTs            = Constants.MIN_EXEC_TIME;
    innerMinTs            = Constants.MIN_EXEC_TIME;

    state                 = ExecState.S_INIT;
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
    out.writeObject(lastLeftKind);
    out.writeObject(lastRightKind);
    out.writeLong(minNextInnerTs);
    out.writeLong(minNextOuterTs);
    out.writeLong(lastEffectiveOutputTs);
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
    lastLeftKind = (Kind) in.readObject();
    lastRightKind = (Kind) in.readObject();
    minNextInnerTs = in.readLong();
    minNextOuterTs = in.readLong();
    lastEffectiveOutputTs = in.readLong();
  }
  
  /**
   * Copy the state from other state variable.
   * Used to load state from snapshot.
   * @param other
   */
  public void copyFrom(BinStreamJoinState other)
  {
    super.copyFrom(other);
    this.lastOuterTs = other.lastOuterTs;
    this.lastInnerTs = other.lastInnerTs;
    this.lastLeftKind = other.lastLeftKind;
    this.lastRightKind = other.lastRightKind;
    this.minNextInnerTs = other.minNextInnerTs;
    this.minNextOuterTs = other.minNextOuterTs;
    this.lastEffectiveOutputTs = other.lastEffectiveOutputTs;
    
  }
  /**
   * Return the string representation of the BinStreamJoinState with only those
   * instance variables which are stored in snapshot for HA.
   * @return
   */
  public String toDumpString()
  {
    return "BinStreamJoinState [lastOuterTs=" + lastOuterTs + ", lastInnerTs="
        + lastInnerTs + ", lastLeftKind=" + lastLeftKind + ", lastRightKind="
        + lastRightKind + ", minNextInnerTs=" + minNextInnerTs
        + ", minNextOuterTs=" + minNextOuterTs + ", lastEffectiveOutputTs="
        + lastEffectiveOutputTs + "]";
  }
}