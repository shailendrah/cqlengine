/* $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/OrderByState.java /main/11 2009/05/05 21:14:50 sbishnoi Exp $ */

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
    sbishnoi    05/04/09 - cleanup
    udeshmuk    04/13/09 - add minNextInputTs
    sbishnoi    04/09/09 - initializing timestamp varible to
                           Constant.MIN_EXEC_TIME
    sbishnoi    04/01/09 - piggyback optimization
    sbishnoi    03/03/09 - removing state variable lastInputKind
    hopark      10/10/08 - remove statics
    sbishnoi    06/26/08 - moving lastOutputTS into MutableState
    hopark      01/31/08 - queue optimization
    hopark      12/26/07 - use DumpDesc
    parujain    12/05/07 - operator logging
    hopark      11/27/07 - add operator specific dump
    hopark      10/30/07 - remove IQueueElement
    hopark      10/22/07 - remove TimeStamp
    parujain    06/28/07 - orderby operator state
    parujain    06/28/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/OrderByState.java /main/11 2009/05/05 21:14:50 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.logging.DumpDesc;
import oracle.cep.service.ExecContext;

public class OrderByState extends MutableState {
  
  /** timestamp associated with input element */
  long              inputTs;
  
  /** minimum expected timestamp of the next input element */
  long              minNextInputTs;
  
  /** current input element */
  QueueElement      inputElement;
  
  @DumpDesc(ignore=true)
  QueueElement      inputElementBuf;
  
  QueueElement.Kind inputKind;
  
  /** current input tuple  */
  ITuplePtr         inputTuple;
  
  /** TimeStamp of current output element being prepared */
  long              outputTs;
  
  /** current output element being prepared */
  QueueElement      outputElement;
  
  /** current output tuple being prepared */
  ITuplePtr         outputTuple;
  
  /** true if next tuple will come with higher TimeStamp*/
  boolean           isTotalOrderGuarantee;
  
  /** next state to process */
  ExecState         outputState;
  
  /**
   * Constructor for OrderByState
   * @param ec TODO
   */
  public OrderByState(ExecContext ec) {
    /* MutableState constructor will initialize lastInputTs and lastOutputTs*/
    super(ec);    
    inputTs               = Constants.MIN_EXEC_TIME;   
    minNextInputTs        = Constants.MIN_EXEC_TIME;   
    inputElementBuf       = allocQueueElement();
    inputTuple            = null;
    outputElement         = allocQueueElement();
    outputTuple           = null;
    state                 = ExecState.S_INIT;
    isTotalOrderGuarantee = false;
    outputState           = ExecState.S_READ_LIST_ELEM;
  }
  
  public OrderByState() 
  {
    super();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    super.writeExternal(out);
    out.writeLong(inputTs);    
    out.writeBoolean(isTotalOrderGuarantee);
    out.writeLong(minNextInputTs);
	out.writeLong(outputTs); 
	out.writeObject(outputState);
    out.writeObject(state);    
  }
  
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
     super.readExternal(in);
     inputTs = in.readLong();
     isTotalOrderGuarantee = in.readBoolean();
     minNextInputTs = in.readLong();
	 outputTs = in.readLong();
	 outputState = (ExecState) in.readObject();
     state = (ExecState) in.readObject();
  }
  
  public void copyFrom(OrderByState other)
  {
    super.copyFrom(other);
    inputTs = other.inputTs;
    isTotalOrderGuarantee = other.isTotalOrderGuarantee;
    minNextInputTs = other.minNextInputTs;
	outputTs = other.outputTs;
	outputState = other.outputState; 
    state = other.state;    
  }
}
