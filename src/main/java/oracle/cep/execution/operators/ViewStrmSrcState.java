/* $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/ViewStrmSrcState.java /main/14 2009/04/15 06:40:26 sbishnoi Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    04/10/09 - adding minNextTs
    sbishnoi    04/09/09 - initializing timestamp variables to
                           Constant.MIN_EXEC_TIME
    sbishnoi    01/21/09 - total order optimization
    hopark      10/10/08 - remove statics
    sbishnoi    06/26/08 - moving lastInputTs and lastOutputTs to MutableState
    hopark      01/31/08 - queue optimization
    hopark      12/26/07 - use DumpDesc
    hopark      11/27/07 - add operator specific dump
    hopark      10/30/07 - remove IQueueElement
    hopark      10/22/07 - remove TimeStamp
    anasrini    09/04/07 - add inputTuple
    najain      03/14/07 - cleanup
    najain      03/12/07 - bug fix
    najain      01/05/07 - spill over support
    najain      08/03/06 - view strm share underlying store
    najain      07/13/06 - ref-count timestamps 
    najain      05/24/06 - bug fix 
    najain      05/22/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/ViewStrmSrcState.java /main/14 2009/04/15 06:40:26 sbishnoi Exp $
 *  @author  najain  
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
 * ViewStrmSrcState
 *
 * @author najain
 */
public class ViewStrmSrcState extends MutableState
{
  /** input element */
  QueueElement      inputElement;
  @DumpDesc(ignore=true) QueueElement     inputElementBuf;
  
  /** output element */
  QueueElement      outputElement;
  
  /** timestamp of input tuple */
  long              inputTs;  
  
  /** current input tuple */
  ITuplePtr         inputTuple;
  
  /** output tuple */
  ITuplePtr         outputTuple;
  
  /** output kind */
  QueueElement.Kind outputKind;
  
  /** flag to check total ordering guarantee for current input element*/
  boolean isTotalOrderingGuarantee;
  
  /** flag to check total ordering guarantee for last input element */
  boolean lastTupleTotalOrderingGuarantee;

  /** minimum expected ts of the next input */
  long    minNextTs;

  /**
   * Constructor for ViewStrmSrcState
   * @param ec TODO
   */
  public ViewStrmSrcState(ExecContext ec)
  {
    /* MutableState constructor will initialize lastInputTs and lastOutputTs */
    super(ec);
    inputTs         = Constants.MIN_EXEC_TIME;
    minNextTs       = Constants.MIN_EXEC_TIME;
    inputTuple      = null;
    outputTuple     = null;
    inputElementBuf = allocQueueElement();
    outputElement   = allocQueueElement();
    state           = ExecState.S_INIT;
    
    // Set All Flag to FALSE
    isTotalOrderingGuarantee        = false;
    lastTupleTotalOrderingGuarantee = false;
  }
  
//for snapshot
  public ViewStrmSrcState()
  {
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException 
  {
    super.writeExternal(out);
    out.writeLong(inputTs);
    out.writeLong(minNextTs);
    out.writeBoolean(isTotalOrderingGuarantee);
    out.writeBoolean(lastTupleTotalOrderingGuarantee);
    out.writeObject(outputKind);
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    super.readExternal(in);
    inputTs = in.readLong();
    minNextTs = in.readLong();
    isTotalOrderingGuarantee = in.readBoolean();
    lastTupleTotalOrderingGuarantee = in.readBoolean();
    outputKind = (QueueElement.Kind)in.readObject();
  }
  
  public void copyFrom(ViewStrmSrcState other)
  {
    super.copyFrom(other);
    inputTs = other.inputTs;
    minNextTs = other.minNextTs;
    isTotalOrderingGuarantee = other.isTotalOrderingGuarantee;
    lastTupleTotalOrderingGuarantee = other.lastTupleTotalOrderingGuarantee;
    outputKind = other.outputKind;
  }
}

