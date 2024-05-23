/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ProjectState.java /main/16 2013/06/11 08:46:11 udeshmuk Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares ProjectState in package oracle.cep.execution.operators.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    udeshmuk  05/22/13 - bug 16820093 : prevInputSnapshotId added
    sbishnoi  04/09/09 - initializing timestamp varible to
                         Constant.MIN_EXEC_TIME
    udeshmuk  01/21/09 - total ts ordering optimization for systs cases.
    hopark    10/10/08 - remove statics
    sbishnoi  06/26/08 - moving lastOutputTs to MutableState
    hopark    01/31/08 - queue optimization
    hopark    12/26/07 - use DumpDesc
    hopark    11/27/07 - add operator specific dump
    parujain  12/17/07 - db-join
    hopark    10/30/07 - remove IQueueElement
    hopark    10/22/07 - remove TimeStamp
    hopark    03/24/07 - add inputTuple
    najain    03/14/07 - cleanup
    najain    03/12/07 - bug fix
    najain    01/05/07 - spill over support
    parujain  12/13/06 - propagation of relations
    najain    07/13/06 - ref-count timestamps 
    najain    05/24/06 - bug fix 
    najain    05/23/06 - bug fix 
    skaluska  03/21/06 - implementation
    skaluska  03/20/06 - creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ProjectState.java /main/16 2013/06/11 08:46:11 udeshmuk Exp $
 *  @author  skaluska
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

/**
 * ProjectState
 *
 * @author skaluska
 */
public class ProjectState extends MutableState
{
  /** timestamp of tuple dequeued from inputQueue */
  long              inputTs;
  
  /** current input element */
  QueueElement      inputElement;
  @DumpDesc(ignore=true) QueueElement      inputElementBuf;
  /** current input tuple */
  ITuplePtr         inputTuple;
  /** current output element */
  QueueElement      outputElement;
  /** current output tuple */
  ITuplePtr         outputTuple;
  /** current output kind */
  QueueElement.Kind outputKind;
  /** tuple lineage */
  ITuplePtr[]       tupleLineage;
  /** Scan lineage synopsis */
  TupleIterator     scan;
  /** totalOrderingGuarantee flag value */
  boolean           lastInputOrderingFlag;
  
  /** minimum timeStamp expected for next input tuple*/
  long              minNextInputTs;
  
  /** snapshot id of previous input */
  long              prevInputSnapshotId = Long.MAX_VALUE;

  /**
   * Constructor for ProjectState
   * @param ec TODO
   */
  public ProjectState(ExecContext ec)
  {
    /* MutableState constructor will initialize lastOutputTs and lastInputTs*/
    super(ec);
    inputElementBuf = allocQueueElement();
    outputElement   = allocQueueElement();
    tupleLineage    = new ITuplePtr[1];
    state           = ExecState.S_INIT;
    inputTs         = Constants.MIN_EXEC_TIME;
    minNextInputTs  = Constants.MIN_EXEC_TIME;
    lastInputOrderingFlag = false;
  }
  
  public ProjectState() 
  {
	super();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    super.writeExternal(out);
    out.writeLong(inputTs);    
    out.writeBoolean(lastInputOrderingFlag);
    out.writeLong(minNextInputTs);
    out.writeLong(prevInputSnapshotId);
    out.writeObject(state);    
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    super.readExternal(in);
    inputTs = in.readLong();
    lastInputOrderingFlag = in.readBoolean();
    minNextInputTs = in.readLong();
    prevInputSnapshotId = in.readLong();
    state = (ExecState) in.readObject();
  }
  
  public void copyFrom(ProjectState other)
  {
    super.copyFrom(other);
	inputTs = other.inputTs;
    lastInputOrderingFlag = other.lastInputOrderingFlag;
	minNextInputTs = other.minNextInputTs;
	prevInputSnapshotId = other.prevInputSnapshotId;
	state = other.state;    
  }
}
