/* $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/MinusState.java /main/10 2009/05/29 08:58:34 sborah Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    05/11/09 - removing unused variables
    udeshmuk    04/13/09 - add lastLeftKind, lastRightKind
    sbishnoi    04/09/09 - initializing timestamp varible to
                           Constant.MIN_EXEC_TIME
    udeshmuk    04/07/09 - total ordering optimization
    hopark      10/10/08 - remove statics
    sbishnoi    07/04/08 - adding processSilentRelation flag
    hopark      01/31/08 - queue optimization
    hopark      12/26/07 - use DumpDesc
    hopark      11/27/07 - add operator specific dump
    hopark      10/30/07 - remove IQueueElement
    hopark      10/22/07 - remove TimeStamp
    sbishnoi    09/26/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/MinusState.java /main/10 2009/05/29 08:58:34 sborah Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.logging.DumpDesc;
import oracle.cep.service.ExecContext;

public class MinusState extends MutableState
{
  ExecState     tmpState;

  /** Timestamp of the last left element */
  long          lastLeftTs;

  /** Timestamp of the last right element */
  long          lastRightTs;

  /** Timestamp of elements dequeued from the left / right queues */
  long          leftTs;

  long          rightTs;

  /** minimum timestamp of left and right queues */
  long           leftMinTs;

  long           rightMinTs;

  QueueElement  leftElement;
  @DumpDesc(ignore=true) QueueElement   leftElementBuf;

  QueueElement  rightElement;
  @DumpDesc(ignore=true) QueueElement   rightElementBuf;

  ITuplePtr      leftTuple;
  
  ITuplePtr      rightTuple;


  /** Minus output tuple */
  ITuplePtr     outputTuple;

  long          nextOutputTs;

  long          outputTs;

  QueueElement.Kind   nextElementKind;

  QueueElement.Kind   lastLeftKind;
  QueueElement.Kind   lastRightKind;
  
  QueueElement  outputElement;
  
  @DumpDesc(ignore=true) TupleIterator tupleIter;
  
  ITuplePtr     searchedTuple;
  
  //Total ordering related
  boolean      lastInputTotalOrderingFlag;

  long         minNextLeftTs;
  long         minNextRightTs;

  /**
   * Constructor for MinusState
   * @param ec TODO
   */
  public MinusState(ExecContext ec)
  {
    // MutableState will initialize lastInputTs and lastOutputTs
    super(ec);
    lastLeftTs   = Constants.MIN_EXEC_TIME;
    lastRightTs  = Constants.MIN_EXEC_TIME;
    
    leftElementBuf = allocQueueElement();
    rightElementBuf = allocQueueElement();
    outputElement = allocQueueElement();
    
    state        = ExecState.S_INIT;
    leftTuple    = null;
    rightTuple   = null;
    
    lastInputTotalOrderingFlag = false;
   
    minNextLeftTs  = Constants.MIN_EXEC_TIME;
    minNextRightTs = Constants.MIN_EXEC_TIME;
    
    leftTs         = Constants.MIN_EXEC_TIME;
    rightTs        = Constants.MIN_EXEC_TIME;
    leftMinTs      = Constants.MIN_EXEC_TIME;
    rightMinTs     = Constants.MIN_EXEC_TIME;
 
    lastLeftKind   = null;
    lastRightKind  = null;
  }
}
