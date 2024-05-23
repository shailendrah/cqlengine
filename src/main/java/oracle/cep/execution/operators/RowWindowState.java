/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/RowWindowState.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      04/13/09 - assertion check
    parujain    04/03/09 - negative ts
    udeshmuk    01/22/09 - total ts ordering optimization
    hopark      10/10/08 - remove statics
    sbishnoi    06/26/08 - moving lastOutputTs to MutableState
    parujain    06/17/08 - slide support
    hopark      01/31/08 - queue optimization
    hopark      12/26/07 - use DumpDesc
    hopark      11/27/07 - add operator specific dump
    hopark      10/30/07 - remove IQueueElement
    hopark      10/22/07 - remove TimeStamp
    najain      03/14/07 - cleanup
    najain      03/12/07 - bug fix
    najain      02/19/07 - bug fix
    najain      01/05/07 - spill over support
    parujain    12/11/06 - propagating relations
    dlenkov     05/23/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/RowWindowState.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedList;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.operators.GroupAggrState.DirtyOutputState;
import oracle.cep.execution.operators.GroupAggrState.MinusState;
import oracle.cep.execution.operators.GroupAggrState.PlusState;
import oracle.cep.logging.DumpDesc;
import oracle.cep.service.ExecContext;

/**
 * RowWindowState
 *
 * @author skaluska
 */
public class RowWindowState extends MutableState
{  
  /** inputElement which is dequeued but not output */
  QueueElement     inputElement;
  @DumpDesc(ignore=true) QueueElement      inputElementBuf;
  /** inputTuple from inputElement which is dequeued but not output */
  ITuplePtr    inputTuple;
  /** inputKind from inputElement */
  QueueElement.Kind inputKind;
  /** inputTs from inputElement */
  long        inputTs;
  
  long        minNextTs;
  /** output IElement to be enqueued */
  QueueElement      outputElement;
  /** output element timestamp */
  long        outputTs;
  /** next Tuple to be expired from the window */
  ITuplePtr    expiredTuple;
  /** true if any tuple can get expired else false */
  boolean      canTupleExpire;
  /** Accumulate the input elements and produce the output when slide count */
  LinkedList<QueueElement>    inputElems;
  /** can be true if slide > 1 and populating outputs */
  boolean      isPopulatingOutputs;
  /** number of rows processed */
  long         numProcessed;
  /** totalOrderingGuarantee flag value for the last received inputElement */ 
  boolean      lastInputOrderingFlag;
  /** Number of events present in window synopsis. 
   *  If slide size is 1 then numWindowElements will be equal to window size
   *  If slide size is greater than 1, then numWindowElements can grow beyond window size.
   */
  long         numWindowElements;

  /**
   * Zero Argument Constructor
   * Invoked while deserializing instances of RowWindowState type
   */
  public RowWindowState()
  {
    super();
  }
  
  /**
   * Constructor for RowWindowState
   * @param ec TODO
   */
  public RowWindowState(ExecContext ec)
  {
    /* MutableState constructor will initialize lastOutputTs and lastInputTs*/
    super(ec);
    numProcessed = 0L;
    numWindowElements = 0L;
    inputElementBuf = allocQueueElement();
    outputElement = allocQueueElement();
    state = ExecState.S_INIT;
    canTupleExpire = false;
    isPopulatingOutputs = false;
    inputElems = new LinkedList<QueueElement>();
    lastInputOrderingFlag = false; //default value
    inputTs               = Constants.MIN_EXEC_TIME;
    minNextTs             = Constants.MIN_EXEC_TIME;
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    super.writeExternal(out);    
    out.writeLong(this.minNextTs);
    out.writeLong(this.numProcessed);
    out.writeLong(this.numWindowElements);
    out.writeBoolean(this.isPopulatingOutputs);
    out.writeObject(this.inputElems);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
     super.readExternal(in);     
     this.minNextTs = in.readLong();
     this.numProcessed = in.readLong();
     this.numWindowElements = in.readLong();
     this.isPopulatingOutputs = in.readBoolean();
     this.inputElems = (LinkedList<QueueElement>) in.readObject();
  }
  
  public void copyFrom(RowWindowState other)
  {
    super.copyFrom(other);
    this.minNextTs = other.minNextTs;
    this.numProcessed = other.numProcessed;
    this.numWindowElements = other.numWindowElements;
    this.isPopulatingOutputs = other.isPopulatingOutputs;
    this.inputElems = other.inputElems;
  }
  
  @Override
  public String toString()
  {
    return super.toString() + " RowWindowState[minNextTs=" + minNextTs + 
        ", numProcessed=" + numProcessed + ", numWindowElements=" + 
        numWindowElements + " isPopulatingOutputTs=" + isPopulatingOutputs
        + ", sizeof(inputElems)=" + inputElems.size();
  }
}
