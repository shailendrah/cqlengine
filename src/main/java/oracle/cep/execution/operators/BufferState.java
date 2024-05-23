/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/BufferState.java /main/1 2012/07/16 08:14:06 udeshmuk Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    07/08/12 - state for buffer operator
    udeshmuk    07/08/12 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/BufferState.java /main/1 2012/07/16 08:14:06 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.logging.DumpDesc;
import oracle.cep.service.ExecContext;

public class BufferState extends MutableState
{
  /** timestamp of tuple dequeued from inputQueue */
  long              inputTs;
  
  /** current input element */
  QueueElement      inputElement;
  @DumpDesc(ignore=true) QueueElement inputElementBuf;
  /** current input tuple */
  ITuplePtr         inputTuple;
  /** current output element */
  QueueElement      outputElement;
  /** current output tuple */
  ITuplePtr         outputTuple;
  /** current output kind */
  QueueElement.Kind outputKind;
  /** tuple lineage */
  ITuplePtr[]       tupleLineage;
  /** Scan lineage synopsis */
  TupleIterator     scan;
  
  /** minimum timeStamp expected for next input tuple*/
  long              minNextInputTs;

  /** used when relation synopsis is maintained */
  ITuplePtr         minusTuple;


  BufferState(ExecContext ec)
  {
    super(ec);
    outputTuple           = null;
    minusTuple            = null;
    tupleLineage          = new ITuplePtr[1];
    inputElementBuf       = allocQueueElement();
    outputElement         = allocQueueElement();
    state                 = ExecState.S_INIT;
    inputTs               = Constants.MIN_EXEC_TIME;
    minNextInputTs        = Constants.MIN_EXEC_TIME;
  }
  
}