/* $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/ViewRelnSrcState.java /main/13 2009/04/15 06:40:26 sbishnoi Exp $ */

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
    sbishnoi    04/09/09 - initializing timestamp variables to
                           Constant.MIN_EXEC_TIME
    hopark      10/10/08 - remove statics
    sbishnoi    06/26/08 - moving lastOutputTs to MutableState
    hopark      01/31/08 - queue optimization
    hopark      12/26/07 - use DumpDesc
    hopark      11/27/07 - add operator specific dump
    hopark      10/30/07 - remove IQueueElement
    hopark      10/22/07 - remove TimeStamp
    najain      03/14/07 - cleanup
    najain      03/12/07 - bug fix
    najain      01/05/07 - spill over support
    parujain    12/07/06 - propagating relation
    najain      08/02/06 - optimize
    najain      07/13/06 - ref-count timestamps 
    najain      06/16/06 - bug fix 
    najain      06/13/06 - bug fix 
    najain      06/08/06 - query addition re-entrant 
    najain      05/24/06 - bug fix 
    najain      05/22/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/ViewRelnSrcState.java /main/13 2009/04/15 06:40:26 sbishnoi Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.logging.DumpDesc;
import oracle.cep.service.ExecContext;

/**
 * ViewRelnSrcState
 *
 * @author najain
 */
public class ViewRelnSrcState extends MutableState
{
  /** input element */
  QueueElement       inputElement;

  @DumpDesc(ignore=true) QueueElement inputElementBuf;

  /* input tuple */
  ITuplePtr          inputTuple;

  /** timestamp of input tuple */
  long               inputTs;  

  /** output element kind */
  QueueElement.Kind  outputKind;
  
  /**
   * Constructor for ViewRelnSrcState
   * @param ec TODO
   */
  public ViewRelnSrcState(ExecContext ec)
  {
    /* MutableState constructor will initialize lastInputTs and lastOutputTs */   
    super(ec);
    inputElementBuf = allocQueueElement();
    state           = ExecState.S_INIT;
    inputTs         = Constants.MIN_EXEC_TIME;
  }
  
  //for snapshot
  public ViewRelnSrcState()
  {
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException 
  {
    super.writeExternal(out);
    out.writeLong(inputTs);
    out.writeObject(outputKind);
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    super.readExternal(in);
    inputTs = in.readLong();
    outputKind = (QueueElement.Kind)in.readObject();
  }
  
  public void copyFrom(ViewRelnSrcState other)
  {
    super.copyFrom(other);
    inputTs = other.inputTs;
    outputKind = other.outputKind;
  }
}

