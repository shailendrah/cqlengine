/* $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/RStreamState.java /main/14 2009/05/13 09:38:22 sborah Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Mutable state for the RSTREAM operator

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    05/12/09 - piggybacking, new semantics
    sbishnoi    04/13/09 - initializing timestamp to Constants.MIN_EXEC_TIME
    hopark      10/10/08 - remove statics
    sbishnoi    06/26/08 - moving lastInputTs to MutableState
    najain      04/04/08 - silent reln support
    hopark      01/31/08 - queue optimization
    hopark      12/26/07 - use DumpDesc
    hopark      11/27/07 - add operator specific dump
    hopark      10/30/07 - remove IQueueElement
    hopark      10/22/07 - remove TimeStamp
    najain      03/14/07 - cleanup
    najain      03/12/07 - bug fix
    najain      01/05/07 - spill over support
    najain      07/13/06 - ref-count timestamps 
    najain      05/23/06 - more fields
    anasrini    04/07/06 - Creation
    anasrini    04/07/06 - Creation
    anasrini    04/07/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/RStreamState.java /main/14 2009/05/13 09:38:22 sborah Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.logging.DumpDesc;
import oracle.cep.service.ExecContext;

/**
 * Mutable state for the RSTREAM operator
 *
 * @author anasrini
 * @since 1.0
 */

public class RStreamState extends MutableState {

  /** 
   * Timestamp at which next output is required.
   * This implies that all output until "previous" time instant of
   * lastOutputTs has been completed
   */
  long          nextOutputTs;
  long          inputTs;
  
  // silent relation output is being processed
  boolean       processSilnReln;
  /** current input element */
  QueueElement inputElement;
  @DumpDesc(ignore=true) QueueElement  inputElementBuf;
  /** current input tuple  */
  ITuplePtr     inputTuple;
  /** timestamp of current output element being prepared */
  long          outputTs;
  /** current output element being prepared */
  QueueElement  outputElement;
  /** current output tuple being prepared */
  ITuplePtr      outputTuple;
  /** Iterator over the relation synopsis */
  @DumpDesc(ignore=true) TupleIterator tupleIter;
  
  /** expected timeStamp for next input tuple */
  long          minNextTs;

  /** total order guarantee flag of current input */
  boolean       inpGflag;

  /**
   * This is true iff there is pending processing for some
   * "old" value of time
   */
  boolean       backlogProcessing;

  /** Used to determine if total ordering flag should be set on output */
  ITuplePtr     lookAheadTuple;

  ExecState     nextState;

  public RStreamState()
  {
    super();
  }

  /**
   * Constructor for RStreamState
   * @param ec TODO
   */
  public RStreamState(ExecContext ec) {
    super(ec);
    nextOutputTs = Constants.MIN_EXEC_TIME;
    inputTs      = Constants.MIN_EXEC_TIME;
    outputTs     = Constants.MIN_EXEC_TIME;
    minNextTs    = Constants.MIN_EXEC_TIME;
    inputElementBuf = allocQueueElement();
    outputElement = allocQueueElement();
    state = ExecState.S_INIT;
    processSilnReln = false;

    // Initially there is no backlog processing
    backlogProcessing = false;
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException 
  {
    super.writeExternal(out);
    out.writeLong(nextOutputTs);
    out.writeLong(inputTs);
    out.writeLong(outputTs);
    out.writeLong(minNextTs);
    out.writeBoolean(processSilnReln);
    out.writeBoolean(backlogProcessing);
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    super.readExternal(in);
    nextOutputTs = in.readLong();
    inputTs = in.readLong();
    outputTs = in.readLong();
    minNextTs = in.readLong();
    processSilnReln = in.readBoolean();
    backlogProcessing = in.readBoolean();
  }
  
  public void copyFrom(RStreamState other)
  {
    super.copyFrom(other);
    nextOutputTs = other.nextOutputTs;
    inputTs = other.inputTs;
    outputTs = other.outputTs;
    minNextTs = other.minNextTs;
    processSilnReln = other.processSilnReln;
    backlogProcessing = other.backlogProcessing;
  }

}
