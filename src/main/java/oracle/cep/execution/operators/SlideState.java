/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/SlideState.java /main/2 2013/11/29 05:16:31 sbishnoi Exp $ */

/* Copyright (c) 2012, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    11/28/13 - bug 17848329
    sbishnoi    05/29/12 - Creation
 */

package oracle.cep.execution.operators;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.logging.DumpDesc;
import oracle.cep.service.ExecContext;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/SlideState.java /main/2 2013/11/29 05:16:31 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class SlideState extends MutableState
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
  

  /** minimum timestamp for next input tuple*/
  long minNextTs;

  /** next output tuple */
  ITuplePtr nextOutputTuple;
  
  /** total ordering flag for current input tuple */
  boolean currentTotalOrderingFlag;
  
  /** total ordering flag for last output tuple*/
  boolean lastOutputTotalOrderingFlag;
  
  /** Iterator for relation synopsis */
  TupleIterator tupIter;
  
  /**
   * Construct State for Slide Operator
   * @param ec
   */
  public SlideState(ExecContext ec)
  {
    super(ec);
    state = ExecState.S_INIT;
    inputElement = null;
    inputElementBuf =  allocQueueElement();
    outputElement = allocQueueElement();   
    minNextTs = Constants.MIN_EXEC_TIME;  
  }
  
}