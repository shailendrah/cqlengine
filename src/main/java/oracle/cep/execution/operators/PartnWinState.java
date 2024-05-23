/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/PartnWinState.java /main/18 2010/09/25 15:01:41 hopark Exp $ */

/* Copyright (c) 2006, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      09/09/10 - add lastInputOrderingFlag
    sbishnoi    04/13/09 - adding minNextTs
    hopark      04/06/09 - initializing timestamp variables to
                           Constant.MIN_EXEC_TIME
    hopark      10/10/08 - remove statics
    sbishnoi    08/01/08 - support for nanosecond
    sbishnoi    06/26/08 - moving lastOutputTs to MutableState
    hopark      01/31/08 - queue optimization
    hopark      12/26/07 - use DumpDesc
    hopark      11/27/07 - add operator specific dump
    hopark      10/30/07 - remove IQueueElement
    hopark      10/22/07 - remove TimeStamp
    hopark      10/12/07 - add oot debug util
    hopark      03/24/07 - add inputTuple
    najain      03/14/07 - cleanup
    najain      03/12/07 - bug fix
    najain      01/05/07 - spill over support
    hopark      12/29/06 - add expiring flag
    parujain    12/11/06 - propagating relations
    ayalaman    08/03/06 - partition window scan state
    ayalaman    08/03/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/PartnWinState.java /main/18 2010/09/25 15:01:41 hopark Exp $
 *  @author  ayalaman
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import oracle.cep.common.EventTimestamp;
import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.logging.DumpDesc;
import oracle.cep.service.ExecContext;

/**
 *  State information for the partition window operator
 *
 */
public class PartnWinState extends MutableState
{
  /** input Timestamp */
  long          inputTs;

  /** current input element */
  QueueElement inputElement;
  @DumpDesc(ignore=true) QueueElement  inputElementBuf;

  /** current input tuple */
  ITuplePtr     inputTuple;

  /** timestamp of current output element being prepared */
  long          outputTs;

  /** current output element being prepared */
  QueueElement  outputElement;

  /** current output tuple being prepared */
  ITuplePtr     outputTuple;

  /** element kind being processes */
  QueueElement.Kind   prcngElemKind; 

  /** next Tuple to be expired from the window */
  ITuplePtr     expiredTuple;

  /** expired TimeStamp for that synopsis */
  long          expiredTimeStamp;
  
  /** input tuple is replaced */
  ITuplePtr     inputReplaced;
  
  /** Visible Timestamp */
  EventTimestamp     visTs;
  /** Expired Timestamp */
  EventTimestamp     expTs;
  /** Temporary Timestamp to be passed as an argument */
  EventTimestamp     tempTs;
  
  /** expected timestamp of next input tuple */
  long         minNextTs;

 /** total ordering flag in the queue element corresponding to last input tuple */
  boolean          lastInputOrderingFlag;
 
   /**
   *  Constructor
   * @param ec TODO
   */
  public PartnWinState(ExecContext ec)
  {
    /* MutableState constructor will initialize lastInputTs and lastOutputTs */
    super(ec);
    state = ExecState.S_INIT;
    expiredTuple = null;
    visTs = new EventTimestamp(Constants.MIN_EXEC_TIME);
    expTs = new EventTimestamp(Constants.MIN_EXEC_TIME);
    tempTs = new EventTimestamp(Constants.MIN_EXEC_TIME);
    expiredTimeStamp = Constants.MIN_EXEC_TIME;  
    inputElement = null;
    inputElementBuf =  allocQueueElement();
    outputElement = allocQueueElement();
    minNextTs = Constants.MIN_EXEC_TIME;
  }
}
