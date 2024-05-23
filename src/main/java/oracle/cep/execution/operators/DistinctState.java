/* $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/DistinctState.java /main/8 2009/04/15 06:40:26 sbishnoi Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    04/05/09 - piggyback optimization
    hopark      10/10/08 - remove statics
    sbishnoi    06/26/08 - moving lastOutputTs into MutableState
    hopark      01/31/08 - queue optimization
    hopark      12/26/07 - use DumpDesc
    hopark      11/27/07 - add operator specific dump
    hopark      10/30/07 - remove IQueueElement
    hopark      10/22/07 - remove TimeStamp
    sbishnoi    05/14/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/DistinctState.java /main/8 2009/04/15 06:40:26 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.logging.DumpDesc;
import oracle.cep.service.ExecContext;

public class DistinctState extends MutableState {
  
  /** Timestamp of elements dequeued from the left / right queues */
  long              inputTs;
  
  /** minimum timestamp of left and right queues */
  long              inputMinTs;
  
  QueueElement      inputElement;
  
  @DumpDesc(ignore=true) 
  QueueElement      inputElementBuf;

  /** next output element */
  QueueElement      outputElement;

  ITuplePtr         inputTuple;
  
  /** Union output tuple */
  ITuplePtr         outputTuple;

  long              nextOutputTs;
  long              outputTs;
  
  QueueElement.Kind nextElementKind;

  ExecState         tmpState;
  
  /** Iterator for the count touples */
  @DumpDesc(ignore=true) 
  TupleIterator     countScan;
  
  /** true if next tuple will come at higher timestamp */
  boolean           isTotalOrderGuarantee;
  
  /** minimum possible next input timestamp */
  long              minNextInputTs;
  
  
  public DistinctState(ExecContext ec) {
    /* MutableState constructor will initialize lastInputTs and lastOutputTs */
    super(ec);
    inputElementBuf = allocQueueElement();
    outputElement   = allocQueueElement();
    state           = ExecState.S_INIT;
    // initialize all timestamp variables to Constant.MIN_EXEC_TIME
    inputTs         = Constants.MIN_EXEC_TIME;
    inputMinTs      = Constants.MIN_EXEC_TIME;
    nextOutputTs    = Constants.MIN_EXEC_TIME;
    outputTs        = Constants.MIN_EXEC_TIME;
    minNextInputTs  = Constants.MIN_EXEC_TIME;
    // initialize the flags
    isTotalOrderGuarantee = false;
  }
  
  
  public DistinctState() 
  {
    super();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    super.writeExternal(out);
    out.writeLong(inputTs);
    out.writeLong(inputMinTs);
    out.writeLong(nextOutputTs);
    out.writeLong(outputTs);
    out.writeLong(minNextInputTs);
    out.writeBoolean(isTotalOrderGuarantee);
    out.writeObject(state);
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
     super.readExternal(in);
     inputTs = in.readLong();
     inputMinTs = in.readLong();
     nextOutputTs = in.readLong();
     outputTs = in.readLong();
     minNextInputTs = in.readLong();     
     isTotalOrderGuarantee = in.readBoolean();	   
     state = (ExecState) in.readObject();
  }
  
  public void copyFrom(DistinctState other)
  {
    super.copyFrom(other);
    inputTs = other.inputTs;
    inputMinTs = other.inputMinTs;
    nextOutputTs = other.nextOutputTs;
    outputTs = other.outputTs;
    minNextInputTs = other.minNextInputTs;
    isTotalOrderGuarantee = other.isTotalOrderGuarantee;    
    state = other.state;    
  }
}
