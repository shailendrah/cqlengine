/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/StreamSourceState.java /main/14 2011/08/18 12:08:43 alealves Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares StreamSourceState in package oracle.cep.execution.operators.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 anasrini  08/11/11 - check isStatsEnabled
 sborah    04/13/09 - assertion check
 sbishnoi  04/08/09 - adding flag for totalOrdering
 sbishnoi  04/08/09 - removing followup Heartbeat flag
 sbishnoi  04/03/09 - initializing timestamp variables to
                      Constant.MIN_EXEC_TIME
 sbishnoi  01/27/09 - total order optimization
 hopark    10/10/08 - remove statics
 sbishnoi  06/26/08 - moving lastInputTs and lastOutputTs to MutableState
 mthatte   04/02/08 - derived timestamp
 hopark    01/31/08 - queue optimization
 hopark    11/27/07 - add operator specific dump
 hopark    10/30/07 - remove IQueueElement
 hopark    10/22/07 - remove TimeStamp
 najain    03/14/07 - cleanup
 najain    03/12/07 - bug fix
 najain    01/04/07 - spill over support
 skaluska  04/04/06 - add outputTs 
 skaluska  03/20/06 - implementation
 skaluska  03/14/06 - query manager 
 skaluska  03/03/06 - Creation
 skaluska  03/03/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/StreamSourceState.java /main/13 2009/04/16 07:38:31 udeshmuk Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.external.TupleKind;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.service.ExecContext;

/**
 * StreamSourceState
 *
 * @author skaluska
 */
public class StreamSourceState extends MutableState
{
  /** timestamp of new input tuple (temporary) */
  long         inputTs;
  
  /** the minimum value of the next timestamp that is expected */
  long        minNextTs;
  
  /** input tuple */
  TupleValue   inputTuple;
  
  /** output tuple */
  ITuplePtr    outputTuple;
  
  /** derived timestamp tuple */
  ITuplePtr    timestampTuple;
  
  /** next output element */
  QueueElement outputElement;
  
  /** output element timestamp */
  long         outputTs;
  
  /** true if the next tuple will come at higher timestamp */
  boolean      isTotalOrderGuarantee;

  /** kind of last input tuple */
  TupleKind    lastTupleKind;
  
  /**
   * Constructor for StreamSourceState
   * @param ec ExecContext for this CEPService
   * @param isStatsEnabled is statistics gathering enabled
   */
  public StreamSourceState(ExecContext ec, boolean isStatsEnabled)
  {
    /* MutableState constructor will initialize lastInputTs and lastOutputTs */
    super(ec, isStatsEnabled);
    inputTs       = Constants.MIN_EXEC_TIME;
    outputTs      = Constants.MIN_EXEC_TIME;
    minNextTs     = Constants.MIN_EXEC_TIME;
    inputTuple    = null;
    outputElement = allocQueueElement();
    outputTuple   = null;
    state         = ExecState.S_INIT;
    lastTupleKind = null;
    
    // set the total ordering guarantee FALSE
    isTotalOrderGuarantee = false;
  }


  /**
   * Constructor for MutableState
   * @param ec ExecContext for this CEPService
   */
  public StreamSourceState(ExecContext ec)
  {
    // Default case - continue to collect statisctics
    // since it requires a lot of change in the ORDERED operators
    // to go by the setting of the isStatsEnabled config param
    this(ec, true);
  }
  
  public StreamSourceState()
  {
	  super();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    super.writeExternal(out);
    out.writeLong(inputTs);
	out.writeLong(outputTs);
	out.writeLong(minNextTs);
	out.writeObject(inputTuple);
    out.writeBoolean(isTotalOrderGuarantee);	
	out.writeObject(lastTupleKind);
    out.writeObject(state);
  }
  
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
     super.readExternal(in);
     inputTs = in.readLong();
	 outputTs = in.readLong();
	 minNextTs = in.readLong();
	 inputTuple = (TupleValue)in.readObject();
     isTotalOrderGuarantee = in.readBoolean();	 
	 lastTupleKind = (TupleKind) in.readObject();
	 state = (ExecState) in.readObject();
  }
  
  public void copyFrom(StreamSourceState other)
  {
    super.copyFrom(other);
	inputTs = other.inputTs;
	outputTs = other.outputTs;
	minNextTs = other.minNextTs;
    inputTuple = other.inputTuple;
    isTotalOrderGuarantee = other.isTotalOrderGuarantee;
    lastTupleKind = other.lastTupleKind;
	state = other.state;    
  }
}
