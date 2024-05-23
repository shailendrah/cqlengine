/* $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/OrderByTopState.java /main/4 2009/04/15 06:40:26 sbishnoi Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    04/13/09 - add minNextInputTs
    sbishnoi    04/07/09 - fix bug 5929284
    sbishnoi    03/23/09 - piggyback optimization - adding some state variables
    sbishnoi    03/10/09 - support for partition by clause inside order by
    sbishnoi    02/11/09 - Creation
 */

package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.QueueElement.Kind;
import oracle.cep.logging.DumpDesc;
import oracle.cep.service.ExecContext;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/OrderByTopState.java /main/4 2009/04/15 06:40:26 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class OrderByTopState extends MutableState 
{
  
  long                inputTs; 

  long                minNextInputTs; 

  /** current input element */
  QueueElement        inputElement;
  @DumpDesc(ignore=true) 
  QueueElement        inputElementBuf;
  QueueElement.Kind   lastInputKind;
  QueueElement.Kind   inputKind;
  
  /** current input tuple  */
  ITuplePtr    inputTuple;
  
  /** time-stamp of current output element being prepared */
  long         outputTs;
  
  /** current output element being prepared */
  QueueElement outputElement;
  
  /** PLUS or MINUS tuple which will be next output */
  ITuplePtr    outputTuple;
  
  /** nextOutputTuple allocated from tupleStorage as per new tuple spec*/
  ITuplePtr    nextOutputTuple;
  
  /** tuple lineage */  
  ITuplePtr[]  tupleLineage;
  
  /** tuple which will expire next after processing current input */
  ITuplePtr    expiredTuple;
  
  /** least tuple from backUpQueue to be inserted in finalQueue*/
  ITuplePtr    nextBackupTuple;
  
  /** state variable used to send pending PLUS tuples*/
  ExecState    nextState;  
  
  /** partition header tuple */
  ITuplePtr    partitionHdrTuple;
  
  /** flag to check whether the next input will come with higher time-stamp */
  boolean      lastInputOrderingFlag;
  
  /**
   * Constructor for OrderByTopState
   * @param ec TODO
   */
  public OrderByTopState(ExecContext ec) {
    /* MutableState constructor will initialize lastInputTs and lastOutputTs*/
    super(ec);    
    inputTs               = Constants.MIN_EXEC_TIME;
    minNextInputTs        = Constants.MIN_EXEC_TIME;
    outputTs              = Constants.MIN_EXEC_TIME;
    inputElementBuf       = allocQueueElement();
    inputTuple            = null;
    outputElement         = allocQueueElement();
    outputTuple           = null;
    state                 = ExecState.S_INIT;
    tupleLineage          = new ITuplePtr[1];
    expiredTuple          = null;    
    nextBackupTuple       = null;
    nextState             = null;   
    partitionHdrTuple     = null;
    lastInputOrderingFlag = false;
  }
  
  public OrderByTopState() 
  {
    super();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    super.writeExternal(out);
    out.writeLong(inputTs);    
    out.writeLong(minNextInputTs);
    out.writeLong(outputTs); 
    out.writeObject(nextState);
    out.writeBoolean(lastInputOrderingFlag);
    out.writeObject(inputKind);
    out.writeObject(lastInputKind);    
  }  
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    super.readExternal(in);
    inputTs = in.readLong();
    minNextInputTs = in.readLong();
    outputTs = in.readLong();
    nextState = (ExecState) in.readObject();
    lastInputOrderingFlag = in.readBoolean();
    inputKind = (Kind) in.readObject();
    lastInputKind = (Kind) in.readObject();
  }
  
  public void copyFrom(OrderByTopState other)
  {
    super.copyFrom(other);
    inputTs = other.inputTs;
    minNextInputTs = other.minNextInputTs;
    outputTs = other.outputTs;
    nextState = other.nextState;
    lastInputOrderingFlag = other.lastInputOrderingFlag;
    inputKind = other.inputKind;
    lastInputKind = other.lastInputKind;    
  }
}
