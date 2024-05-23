/* $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/UnionState.java /main/14 2009/05/08 09:18:26 sborah Exp $ */

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
    udeshmuk    04/13/09 - add lastLeftKind, lastRightKind
    sbishnoi    04/06/09 - piggyback optimization
    hopark      10/10/08 - remove statics
    sbishnoi    06/26/08 - moving lastOutputTs to MutableState
    hopark      01/31/08 - queue optimization
    hopark      12/26/07 - use DumpDesc
    hopark      11/27/07 - add operator specific dump
    hopark      10/30/07 - remove IQueueElement
    hopark      10/22/07 - remove TimeStamp
    sbishnoi    04/09/07 - support for union all
    najain      03/14/07 - cleanup
    najain      03/12/07 - bug fix
    najain      01/05/07 - spill over support
    dlenkov     06/19/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/UnionState.java /main/14 2009/05/08 09:18:26 sborah Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.logging.DumpDesc;
import oracle.cep.service.ExecContext;
import oracle.cep.dataStructures.internal.ITuplePtr;

/**
 * UnionState
 *
 * @author dlenkov
 */
public class UnionState extends MutableState {

  /** Timestamp of the last left element */
  long              lastLeftTs;
  /** Timestamp of the last right element */
  long              lastRightTs;

  /** Timestamp of elements dequeued from the left / right queues */
  long              leftTs;
  long              rightTs;

  /** minimum timestamp of left and right queues */
  long              leftMinTs;
  long              rightMinTs;

  QueueElement      leftElement;
  
  @DumpDesc(ignore=true) 
  QueueElement      leftElementBuf;
  
  QueueElement      rightElement;
  
  @DumpDesc(ignore=true)
  QueueElement      rightElementBuf;

  /** next output element */
  QueueElement      outputElement;

  ITuplePtr         leftTuple;
  ITuplePtr         rightTuple;

  /** Union output tuple */
  ITuplePtr         outputTuple;

  /** calculated next timeStamp*/
  long              nextOutputTs;
  
  QueueElement.Kind nextElementKind;

  QueueElement.Kind lastLeftKind;
  QueueElement.Kind lastRightKind;

  ExecState         tmpState;
  
  /** Iterator for the count tuples */
  @DumpDesc(ignore=true)
  TupleIterator     countScan;
  
  /** minimum possible next timeStamp of left queue element*/
  long              minNextLeftTs;
  
  /** minimum possible next timeStamp of right queue element*/
  long              minNextRightTs;
 
  
  /**
   * Constructor for UnionState
   * @param ec TODO
   */
  public UnionState(ExecContext ec)
  {
    /* MutableState constructor will initialize lastInputTs and lastOutputTs */
    super(ec);
    lastLeftTs      = Constants.MIN_EXEC_TIME;
    lastRightTs     = Constants.MIN_EXEC_TIME;    
    leftElementBuf  = allocQueueElement();
    rightElementBuf = allocQueueElement();
    outputElement   = allocQueueElement();
    state           = ExecState.S_INIT;
    
    // initialize timeStamp values
    
    minNextLeftTs   = Constants.MIN_EXEC_TIME;
    minNextRightTs  = Constants.MIN_EXEC_TIME;
    
    leftTs          = Constants.MIN_EXEC_TIME;
    rightTs         = Constants.MIN_EXEC_TIME;
    
    leftMinTs       = Constants.MIN_EXEC_TIME;
    rightMinTs      = Constants.MIN_EXEC_TIME;
    
    nextOutputTs    = Constants.MIN_EXEC_TIME;
  
    lastLeftKind    = null;
    lastRightKind   = null;  
  }

  public UnionState()
  {
      super();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    super.writeExternal(out);
    out.writeLong(lastLeftTs);
    out.writeLong(lastRightTs);
    out.writeLong(leftMinTs);
    out.writeLong(rightMinTs);
    out.writeLong(minNextLeftTs);
    out.writeLong(minNextRightTs);
    out.writeObject(lastLeftKind);
    out.writeObject(lastRightKind);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
     super.readExternal(in);
     lastLeftTs = in.readLong();
     lastRightTs = in.readLong();
     leftMinTs = in.readLong();
     rightMinTs = in.readLong();
     minNextLeftTs = in.readLong();
     minNextRightTs = in.readLong();
     lastLeftKind = (QueueElement.Kind) in.readObject();
     lastRightKind = (QueueElement.Kind) in.readObject();
  }

  public void copyFrom(UnionState other)
  {
    super.copyFrom(other);
    lastLeftTs = other.lastLeftTs;
    lastRightTs = other.lastRightTs;
    leftMinTs = other.leftMinTs;
    rightMinTs = other.rightMinTs;
    minNextLeftTs = other.minNextLeftTs;
    minNextRightTs = other.minNextRightTs;
    lastLeftKind = other.lastLeftKind;
    lastRightKind = other.lastRightKind;
  }
}
