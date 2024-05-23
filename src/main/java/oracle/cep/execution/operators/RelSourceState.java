/* $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/RelSourceState.java /main/15 2009/04/16 07:38:31 udeshmuk Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares RelSourceState in package oracle.cep.execution.operators.

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sborah      04/13/09 - assertion check
 sbishnoi    04/09/09 - initializing timestamp variables to
                        Constant.MIN_EXEC_TIME
 sbishnoi    04/08/09 - adding flag for totalOrdering
 sbishnoi    04/08/09 - removing followup Heartbeat flag
 sbishnoi    01/28/09 - hbt based total order optimization
 sbishnoi    01/21/09 - total order optimization
 hopark      10/10/08 - remove statics
 sbishnoi    06/26/08 - moving lastOutputTs to MutableState
 hopark      01/31/08 - queue optimization
 hopark      11/27/07 - add operator specific dump
 sbishnoi    10/30/07 - added searchedTuple for Update support
 hopark      10/30/07 - remove IQueueElement
 hopark      10/22/07 - remove TimeStamp
 najain      03/14/07 - cleanup
 najain      03/12/07 - bug fix
 najain      01/04/07 - spill over support
 parujain    12/07/06 - propagating relation
 najain      06/16/06 - bug fix 
 najain      06/13/06 - bug fix 
 najain      06/08/06 - query addition re-entrant 
 najain      05/18/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/RelSourceState.java /main/15 2009/04/16 07:38:31 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.BitSet;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.external.TupleKind;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.service.ExecContext;

/**
 * RelSourceState
 *
 * @author skaluska
 */
public class RelSourceState extends MutableState
{

  /** timestamp of new input tuple (temporary) */
  long           inputTs;

  /** input tuple */
  TupleValue     inputTuple;

  /** output tuple */
  ITuplePtr      outputTuple;
  
  /** minus tuple */
  ITuplePtr      minusTuple;

  /** next output element */
  QueueElement   outputElement;

  /** output element timestamp */
  long           outputTs;
  
  /** the minimum value of the next timestamp that is expected */
  long           minNextTs;
  
  /** Stub ids. being processed currently */
  BitSet         stubIds;
  
  /** Searched Tuple*/
  ITuplePtr      searchedTuple;
  
  /** Flag to check which part of 'UPDATE Tuple' is processing 
   *  i.e. minus or plus*/
  boolean        isPlusProcessed;
  
  /** Flag to check whether it is a 'UPDATE tuple'*/
  boolean        isUpdateTuple;

  /** total ordering guarantee flag for next output tuple*/
  boolean        nextTotalOrderingGuarantee;
  
  /** total ordering guarantee flag for last output tuple*/
  boolean        lastTotalOrderingGuarantee;
  
  /** true if next input tuple will come with higher timeStamp */
  boolean        inpTotalOrderingGuarantee;
  
  /** kind of last input tuple */
  TupleKind      lastTupleKind;

  /**
   * Constructor for RelSourceState
   * @param ec TODO
   */
  public RelSourceState(ExecContext ec)
  {
    /* MutableState constructor will initialize lastInputTs and lastOutputTs*/
    super(ec);
    inputTs                    = Constants.MIN_EXEC_TIME;
    inputTuple                 = null;
    outputElement              = allocQueueElement();
    outputTuple                = null;
    minusTuple                 = null;
    stubIds                    = new BitSet();
    state                      = ExecState.S_INIT;
    searchedTuple              = null;    
    nextTotalOrderingGuarantee = false;
    lastTotalOrderingGuarantee = false;
    inpTotalOrderingGuarantee  = false;
    outputTs                   = Constants.MIN_EXEC_TIME;
    minNextTs                  = Constants.MIN_EXEC_TIME;
    lastTupleKind              = null; 
  }
  
  public RelSourceState()
  {
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException 
  {
    super.writeExternal(out);
    out.writeLong(inputTs);
    out.writeLong(outputTs);
    out.writeLong(minNextTs);
    out.writeBoolean(isPlusProcessed);
    out.writeBoolean(isUpdateTuple);
    out.writeBoolean(nextTotalOrderingGuarantee);
    out.writeBoolean(lastTotalOrderingGuarantee);
    out.writeBoolean(inpTotalOrderingGuarantee);
    out.writeObject(lastTupleKind);
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    super.readExternal(in);
    inputTs = in.readLong();
    outputTs = in.readLong();
    minNextTs = in.readLong();
    isPlusProcessed = in.readBoolean();
    isUpdateTuple = in.readBoolean();
    nextTotalOrderingGuarantee = in.readBoolean();
    lastTotalOrderingGuarantee = in.readBoolean();
    inpTotalOrderingGuarantee = in.readBoolean();
    lastTupleKind = (TupleKind)in.readObject();
  }
  
  public void copyFrom(RelSourceState other)
  {
    super.copyFrom(other);
    inputTs = other.inputTs;
    outputTs = other.outputTs;
    minNextTs = other.minNextTs;
    isPlusProcessed = other.isPlusProcessed;
    isUpdateTuple = other.isUpdateTuple;
    nextTotalOrderingGuarantee = other.nextTotalOrderingGuarantee;
    lastTotalOrderingGuarantee = other.lastTotalOrderingGuarantee;
    inpTotalOrderingGuarantee = other.inpTotalOrderingGuarantee;
    lastTupleKind = other.lastTupleKind;
  }

}
