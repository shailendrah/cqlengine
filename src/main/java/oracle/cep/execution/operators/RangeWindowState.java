/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/RangeWindowState.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares RangeWindowState in package oracle.cep.execution.operators.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 sbishnoi  10/01/11 - XbranchMerge sbishnoi_bug-12720971_ps5 from
                      st_pcbpel_11.1.1.4.0
 sbishnoi  04/10/09 - adding minNextTs
 sbishnoi  04/09/09 - initializing timestamp varible to Constant.MIN_EXEC_TIME
 udeshmuk  01/21/09 - add member to keep track of OrderingFlag of expired
                      tuple.
 hopark    10/10/08 - remove statics
 sbishnoi  08/01/08 - support for nanosecond
 sbishnoi  06/26/08 - moving lastOutputTs to MutableState
 hopark    01/31/08 - queue optimization
 hopark    12/26/07 - use DumpDesc
 hopark    11/27/07 - add operator specific dump
 hopark    10/30/07 - remove IQueueElement
 hopark    10/22/07 - remove TimeStamp
 najain    03/14/07 - cleanup
 parujain  03/09/07 - Extensible Windows
 najain    03/12/07 - bug fix
 najain    01/05/07 - spill over support
 parujain  12/11/06 - propagating relations
 najain    09/25/06 - static reln. support with slide
 najain    07/13/06 - ref-count timestamps 
 najain    07/13/06 - ref-count timeStamp support 
 najain    04/18/06 - time is a part of tuple 
 skaluska  04/04/06 - add outputTs 
 najain    03/30/06 - init state 
 skaluska  03/20/06 - implementation
 skaluska  03/14/06 - query manager 
 skaluska  03/03/06 - Creation
 skaluska  03/03/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/RangeWindowState.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Constants;
import oracle.cep.common.EventTimestamp; 
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement.Kind;
import oracle.cep.logging.DumpDesc;
import oracle.cep.service.ExecContext;

/**
 * RangeWindowState
 *
 * @author skaluska
 */
public class RangeWindowState extends MutableState
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
  /** output Element to be enqueued */
  QueueElement      outputElement;
  /** output element timestamp */
  long        outputTs;
  /** next Tuple to be expired from the window */
  ITuplePtr    expiredTuple;
  /** expired TimeStamp for that synopsis */
  long        expiredTimeStamp;
  /** temporary state */
  long        batchEndTime;
  /** Visible Timestamp */
  EventTimestamp   visTs;
  /** Expired Timestamp */
  EventTimestamp   expTs;
  /** Temporary Timestamp to be passed as an argument */
  EventTimestamp   tempTs;
  /** Temporary Visible timestamp */
  EventTimestamp   tempVisTs;
  /** ordering flag of tuple being expired */
  boolean          expiredTupleOrderingFlag;
  /** total ordering flag in the queue element corresponding to last input tuple */
  boolean          lastInputOrderingFlag;
  
  /** minimum expected timestamp of next tuple*/
  long             minNextTs;
  
  /**
   * Zero Argument Constructor
   */
  public RangeWindowState()
  {
    super();
  }
  
  /**
   * Constructor for RangeWindowState
   * @param ec TODO
   */
  public RangeWindowState(ExecContext ec)
  {
    /* MutableState constructor will initialize lastInputTs and lastOutputTs */
    super(ec);
    state = ExecState.S_INIT;
    visTs = new EventTimestamp(Constants.MIN_EXEC_TIME);
    expTs = new EventTimestamp(Constants.MIN_EXEC_TIME);
    tempTs = new EventTimestamp(Constants.MIN_EXEC_TIME);
    tempVisTs = new EventTimestamp(Constants.MIN_EXEC_TIME);
    minNextTs = Constants.MIN_EXEC_TIME;
    
    batchEndTime     = Constants.MIN_EXEC_TIME;
    inputTs          = Constants.MIN_EXEC_TIME;
    expiredTimeStamp = Constants.MIN_EXEC_TIME;    
    
    expiredTupleOrderingFlag = false; //default value
    lastInputOrderingFlag    = false; //default value
    inputElementBuf          = allocQueueElement();
    outputElement            = allocQueueElement();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    super.writeExternal(out);
    out.writeBoolean(this.lastInputOrderingFlag);
    out.writeObject(this.inputKind);
    out.writeBoolean(this.expiredTupleOrderingFlag);
    out.writeLong(this.batchEndTime);
    out.writeLong(this.minNextTs);
    out.writeLong(expiredTimeStamp);
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    super.readExternal(in);
    this.lastInputOrderingFlag = in.readBoolean();
    this.inputKind = (Kind) in.readObject();
    this.expiredTupleOrderingFlag = in.readBoolean();
    this.batchEndTime = in.readLong();
    this.minNextTs = in.readLong();
    this.expiredTimeStamp = in.readLong();
  }
  
  public void copyFrom(RangeWindowState other)
  {
    super.copyFrom(other);
    this.lastInputOrderingFlag = other.lastInputOrderingFlag;
    this.inputKind = other.inputKind;
    this.expiredTupleOrderingFlag = other.expiredTupleOrderingFlag;
    this.batchEndTime = other.batchEndTime;
    this.minNextTs = other.minNextTs;
  }
  
    
}
