/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/VariableRangeWindowState.java /main/1 2011/05/09 23:12:07 sbishnoi Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    03/17/11 - Creation
 */
package oracle.cep.execution.operators;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.logging.DumpDesc;
import oracle.cep.service.ExecContext;


/**
 *  @version $Header: VariableRangeWindowState.java 17-mar-2011.00:48:10 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class VariableRangeWindowState extends MutableState
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
  
  /** tuple lineage */  
  ITuplePtr[]  tupleLineage;

  /** minimum timestamp for next input tuple*/
  long minNextTs;

  /** next output tuple */
  ITuplePtr nextOutputTuple;
  
  /** total ordering flag for current input tuple */
  boolean currentTotalOrderingFlag;

  VariableRangeWindowState(ExecContext ec)
  {
    super(ec);
    state = ExecState.S_INIT;
    inputElement = null;
    inputElementBuf =  allocQueueElement();
    outputElement = allocQueueElement();   
    minNextTs = Constants.MIN_EXEC_TIME;
    tupleLineage = new ITuplePtr[1];
  }
}
