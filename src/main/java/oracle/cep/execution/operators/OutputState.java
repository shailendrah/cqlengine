/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/OutputState.java /main/14 2012/04/02 03:50:32 sbishnoi Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares OutputState in package oracle.cep.execution.operators.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 udeshmuk  04/13/09 - add minNextInputTs
 sbishnoi  04/08/09 - setting totalordering flag
 sbishnoi  04/03/09 - initializing all timestamp variables to
                      Constants.MIN_EXEC_TIME
 hopark    10/10/08 - remove statics
 hopark    01/31/08 - queue optimization
 hopark    12/26/07 - use DumpDesc
 hopark    11/27/07 - add operator specific dump
 sbishnoi  11/26/07 - support for update semantics
 hopark    10/30/07 - remove IQueueElement
 hopark    10/22/07 - remove TimeStamp
 rkomurav  09/04/07 - bug 6356904
 hopark    03/23/07 - add inputTuple
 najain    03/14/07 - cleanup
 najain    03/12/07 - bug fix
 hopark    03/06/07 - spill over support
 skaluska  03/20/06 - minor change
 najain    03/17/06 - create
 najain    03/17/06 - create
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/OutputState.java /main/14 2012/04/02 03:50:32 sbishnoi Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.logging.DumpDesc;
import oracle.cep.service.ExecContext;

/**
 * OutputState
 *
 * @author skaluska
 */
public class OutputState extends MutableState
{
  /** current input element */
  QueueElement inputElement;
  @DumpDesc(ignore=true) QueueElement  inputElementBuf;
  
  /** output tuple */
  TupleValue    outputTuple;
  
  /** input tuple */
  ITuplePtr     inputTuple;
  
  /** Timestamp of current tuple */
  long          inputTs;

  /** Minimum expected timestamp for the next input tuple */
  long          minNextInputTs;
  
  /** searched tuple whose primary key column values matches with input tuple*/
  ITuplePtr     searchedTuple;
  
  TupleIterator plusIter;
  
  TupleIterator minusIter;
  
  TupleIterator outIter;
  
  ITuplePtr     plusTuple;
  
  ITuplePtr     minusTuple;
  
  ITuplePtr     outTuple;
  
  /** true if the next input tuple will come at higher timeStamp */
  boolean       isTotalOrderGuarantee;
  
  /** kind of input element */
  QueueElement.Kind inputKind;
  
  static enum OutState
  {
    S_INIT, S_PROCESSING1, S_PROCESSING2, S_PROCESSING3, S_PROCESSING4, S_PROCESSING5, S_PROCESSING6, S_OUTPUT_READY, S_OUTPUT_ENQUEUED, S_FINISHED
  };
  
  OutState outState;
  
  /**
   * Constructor for OutputState
   * @param ec TODO
   * @param maxAttrs
   *          Number of output attributes for this Output
   */
  public OutputState(ExecContext ec, int maxAttrs)
  {
    super(ec);
    inputElementBuf = allocQueueElement();
    state     = ExecState.S_INIT;
    outState  = OutState.S_INIT;
    plusIter  = null;
    minusIter = null;
    outIter   = null;
    inputTs         = Constants.MIN_EXEC_TIME;
    minNextInputTs  = Constants.MIN_EXEC_TIME;
    
    // set the guarantee flag to FALSE by default
    isTotalOrderGuarantee = false;
  }
}
