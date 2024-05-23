/* $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/XStreamState.java /main/13 2009/04/15 06:40:26 sbishnoi Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    common mutable state for IStream and DStream 

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    04/06/09 - total ordering
    parujain    04/03/09 - ordering fix
    hopark      10/10/08 - remove statics
    sbishnoi    06/26/08 - moving lastInputTs to MutableState
    najain      04/04/08 - add processSilnReln
    hopark      01/31/08 - queue optimization
    hopark      12/26/07 - use DumpDesc
    hopark      11/27/07 - add operator specific dump
    hopark      10/30/07 - remove IQueueElement
    hopark      10/22/07 - remove TimeStamp
    najain      03/14/07 - cleanup
    najain      03/12/07 - bug fix
    najain      01/05/07 - spill over support
    najain      07/13/06 - ref-count timestamps 
    najain      05/23/06 - heartbeat
    ayalaman    04/26/06 - common mutable state for IStream and DStream 
    ayalaman    04/26/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/XStreamState.java /main/13 2009/04/15 06:40:26 sbishnoi Exp $
 *  @author  ayalaman
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Constants;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.logging.DumpDesc;
import oracle.cep.service.ExecContext;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.ITuplePtr;

public class XStreamState extends MutableState 
{
  /** input Timestamp */
  long          inputTs;
  /** min next input timestamp */
  long          minNextInputTs;
  /** Timestamp at which next output is required */
  long          nextOutputTs;
  // silent relation output is being processed
  boolean       processSilnReln;
  /** current input element */
  QueueElement inputElement;
  @DumpDesc(ignore=true) QueueElement  inputElementBuf;
  ITuplePtr     inputTuple;
  /** timestamp of current output element being prepared */
  long          outputTs;
  /** current output element being prepared */
  QueueElement  outputElement;
  /** current output tuple being prepared */
  ITuplePtr     outputTuple;
  /** Iterator over the relation synopsis */
  @DumpDesc(ignore=true) TupleIterator tupleIter;
  
  ITuplePtr     nextScannedTuple;
  boolean       lastTotalOrderingGuarantee;
  // true if inputTs equals to lastInputTs
  boolean       tsEqual;
  
  public XStreamState(){
    super();
  }

  /**
   * Constructor for the IStreamState. Initialize the state information 
   * @param ec TODO
   */
  XStreamState(ExecContext ec)
  {
    /* MutableState will initialize lastInputTs and lastOutputTs */
    super(ec); 
    inputTs  = Constants.MIN_EXEC_TIME;
    minNextInputTs = Constants.MIN_EXEC_TIME;
    nextOutputTs = Constants.MIN_EXEC_TIME;
    inputElementBuf = allocQueueElement();
    outputElement = allocQueueElement();
    state = ExecState.S_INIT;
    processSilnReln = false;
    nextScannedTuple = null;
    lastTotalOrderingGuarantee = false;
    tsEqual = false;                                                                                                                
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException 
  {
    super.writeExternal(out);
    out.writeLong(inputTs);
    out.writeLong(minNextInputTs);
    out.writeLong(nextOutputTs);
    out.writeBoolean(processSilnReln);
    out.writeLong(outputTs);
    out.writeBoolean(lastTotalOrderingGuarantee);
    out.writeBoolean(tsEqual);
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    super.readExternal(in);
    inputTs = in.readLong();
    minNextInputTs = in.readLong();
    nextOutputTs = in.readLong();
    processSilnReln = in.readBoolean();
    outputTs = in.readLong();
    lastTotalOrderingGuarantee = in.readBoolean();
    tsEqual = in.readBoolean();
  }
  
  public void copyFrom(XStreamState other)
  {
    super.copyFrom(other);
    inputTs = other.inputTs;
    minNextInputTs = other.minNextInputTs;
    nextOutputTs = other.nextOutputTs;
    processSilnReln = other.processSilnReln;
    outputTs = other.outputTs;
    lastTotalOrderingGuarantee = other.lastTotalOrderingGuarantee;
    tsEqual = other.tsEqual;
  }

}
