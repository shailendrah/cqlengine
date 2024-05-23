/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/SelectState.java /main/15 2013/06/11 08:46:11 udeshmuk Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares SelectState in package oracle.cep.execution.operators.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 udeshmuk  05/22/13 - bug 16820093 : prevInputSnapshotId added
 sbishnoi  04/03/09 - initializing timestamp variables to
                      Constant.MIN_EXEC_TIME
 udeshmuk  01/20/09 - total ordering ts optimization
 hopark    10/10/08 - remove statics
 sbishnoi  06/26/08 - moving lastOutputTs to MutableState
 hopark    01/31/08 - queue optimization
 hopark    12/26/07 - use DumpDesc
 hopark    11/27/07 - add operator specific dump
 hopark    10/30/07 - remove IQueueElement
 hopark    10/22/07 - remove TimeStamp
 najain    03/14/07 - cleanup
 najain    03/12/07 - bug fix
 najain    01/05/07 - spill over support
 parujain  12/12/06 - propagating relations
 najain    08/03/06 - select can be shared
 najain    07/13/06 - ref-count timestamps 
 najain    05/23/06 - bug fix 
 skaluska  03/20/06 - implementation
 skaluska  03/14/06 - query manager 
 skaluska  03/03/06 - Creation
 skaluska  03/03/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/SelectState.java /main/15 2013/06/11 08:46:11 udeshmuk Exp $
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
import oracle.cep.logging.DumpDesc;
import oracle.cep.service.ExecContext;

/**
 * SelectState
 *
 * @author skaluska
 */
public class SelectState extends MutableState
{
  /** timestamp of tuple dequeued from inputQueue */
  long      inputTs;
  
  /** current input element */
  QueueElement   inputElement;
  
  @DumpDesc(ignore=true) QueueElement    inputElementBuf;
  
  /** input Tuple*/
  ITuplePtr       inputTuple;
  
  /** output element */
  QueueElement    outputElement;
  
  /** output tuple */
  ITuplePtr      outputTuple;
  
  ITuplePtr      minusTuple;
  
  /* output kind */
  QueueElement.Kind outputKind;

  /* total ordering flag in the QueueElement corresponding to last input */
  boolean           lastInputOrderingFlag;

  /** minimum expected timestamp for next input tuple */
  long              minNextTs;
  
  /** previous input's snapshotId */
  long              prevInputSnapshotId = Long.MAX_VALUE;
  /**
   * Constructor for SelectState
   * @param ec TODO
   */
  public SelectState(ExecContext ec)
  {
    /* MutableState constructor will initialize lastInputTs and lastOutputTs */
    super(ec);
    outputTuple           = null;
    inputElementBuf       = allocQueueElement();
    outputElement         = allocQueueElement();
    state                 = ExecState.S_INIT;
    lastInputOrderingFlag = false; //default is false
    inputTs               = Constants.MIN_EXEC_TIME;
    minNextTs             = Constants.MIN_EXEC_TIME;
  }
  
  public SelectState() 
  {
    super();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    super.writeExternal(out);
    out.writeLong(inputTs);    
    out.writeBoolean(lastInputOrderingFlag);
    out.writeLong(minNextTs);
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
     minNextTs = in.readLong();
     prevInputSnapshotId = in.readLong();
     state = (ExecState) in.readObject();
  }
  
  public void copyFrom(SelectState other)
  {
    super.copyFrom(other);
    inputTs = other.inputTs;
    lastInputOrderingFlag = other.lastInputOrderingFlag;
    minNextTs = other.minNextTs;
    prevInputSnapshotId = other.prevInputSnapshotId;
    state = other.state;    
  }
}
