/* $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/XmlTableState.java /main/7 2009/05/29 08:58:34 sborah Exp $ */

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
    hopark      05/21/09 - add hasNext
    sborah      04/13/09 - assertion check
    hopark      04/06/09 - total ordering opt
    hopark      10/10/08 - remove statics
    sbishnoi    06/26/08 - moving lastOutputTs to MutableState
    hopark      03/05/08 - xml spill
    hopark      01/31/08 - queue optimization
    mthatte     12/26/07 - 
    najain      12/19/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/XmlTableState.java /main/7 2009/05/29 08:58:34 sborah Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.xml.XMLSequence;
import oracle.cep.service.ExecContext;

/**
 * XmlTableState
 *
 * @author skaluska
 */
public class XmlTableState extends MutableState
{
  /** timestamp of tuple dequeued from inputQueue */
  long        inputTs;
  /** the minimum value of the next timestamp that is expected */
  long        minNextTs;
  /** current input element */
  QueueElement     inputElement;
  QueueElement      inputElementBuf;
  /** current input tuple */
  ITuplePtr    inputTuple;
  /** current output element */
  QueueElement      outputElement;
  /** current output tuple */
  ITuplePtr    outputTupleP;
  ITuplePtr    resultSetTupleP;

  /** current output kind */
  QueueElement.Kind outputKind;
  XMLSequence  res;
  boolean       hasNext;
  
  /**
   * Constructor for XmlTableState
   * @param ec TODO
   */
  public XmlTableState(ExecContext ec)
  {
    /* MutableState constructor will initialize lastInputTs and lastOutputTs */
    super(ec);
    lastInputTs = Constants.MIN_EXEC_TIME;
    lastOutputTs = Constants.MIN_EXEC_TIME;
    minNextTs    = Constants.MIN_EXEC_TIME;
    inputElementBuf = allocQueueElement();
    outputElement = allocQueueElement();
    state = ExecState.S_INIT;
    res = null;
  }
}
