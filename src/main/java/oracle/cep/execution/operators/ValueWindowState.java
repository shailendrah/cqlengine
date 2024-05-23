/* $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/ValueWindowState.java /main/6 2009/04/15 06:40:26 sbishnoi Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      04/13/09 - assertion check
    sbishnoi    04/09/09 - initializing timestamp varible to
                           Constant.MIN_EXEC_TIME
    udeshmuk    01/22/09 - total ts ordering opt
    hopark      10/10/08 - remove statics
    parujain    07/07/08 - value based windows
    parujain    07/07/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/ValueWindowState.java /main/6 2009/04/15 06:40:26 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.operators;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.logging.DumpDesc;
import oracle.cep.service.ExecContext;

/**
 * ValueWindowState
 *
 * @author parujain
 */
public class ValueWindowState extends MutableState
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
  /** output IElement to be enqueued */
  QueueElement      outputElement;
  /** output element timestamp */
  long        outputTs;
  /** the minimum value of the next timestamp that is expected */
  long        minNextTs;
  /** next Tuple to be expired from the window */
  ITuplePtr    expiredTuple;
  /** last column value in long */
  long    lastLColVal;
  /** last column value in double */
  double  lastDColVal;
  long    tmpLColVal;
  double  tmpDColVal;
  /** totalOrderingGuarantee flag for the last received input element */
  boolean lastInputOrderingFlag;

  /**
   * Constructor for ValueWindowState
   * @param ec TODO
   */
  public ValueWindowState(ExecContext ec)
  {
    super(ec);
    inputElementBuf = allocQueueElement();
    outputElement   = allocQueueElement();
    state           = ExecState.S_INIT;
    lastLColVal  = 0;
    lastDColVal  = 0.0;
    tmpLColVal   = 0;
    tmpDColVal   = 0.0;
    expiredTuple = null;
    lastInputOrderingFlag = false;
    inputTs  = Constants.MIN_EXEC_TIME;
    outputTs = Constants.MIN_EXEC_TIME;
    minNextTs = Constants.MIN_EXEC_TIME;
  }
}
