/* $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/ExceptState.java /main/11 2009/04/15 06:40:26 sbishnoi Exp $ */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    04/13/09 - removing minNextTs
 hopark      04/06/09 - total ordering opt
 hopark      10/10/08 - remove statics
 hopark      01/31/08 - queue optimization
 hopark      12/26/07 - use DumpDesc
 hopark      11/27/07 - add operator specific dump
 hopark      10/30/07 - remove IQueueElement
 hopark      10/22/07 - remove TimeStamp
 najain      03/14/07 - cleanup
 najain      03/12/07 - bug fix
 najain      01/05/07 - spill over support
 najain      08/09/06 - fix Except
 dlenkov     06/24/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/ExceptState.java /main/11 2009/04/15 06:40:26 sbishnoi Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.logging.DumpDesc;
import oracle.cep.service.ExecContext;

/**
 * ExceptState
 *
 * @author dlenkov
 */
public class ExceptState extends MutableState
{
  ExecState     tmpState;

  /** timestamp of last output tuple */
  long          lastOutputTs;

  /** Timestamp of the last left element */
  long          lastLeftTs;

  /** Timestamp of the last right element */
  long          lastRightTs;

  /** Timestamp of elements dequeued from the left / right queues */
  long          leftTs;

  long          rightTs;

  /** minimum timestamp of left and right queues */
  long          leftMinTs;

  long          rightMinTs;

  QueueElement       leftElement;
  @DumpDesc(ignore=true) QueueElement       leftElementBuf;

  QueueElement       rightElement;
  @DumpDesc(ignore=true) QueueElement       rightElementBuf;

  /** Scan Id for scanning countSyn */
  int           outScanId;

  /** Scan Id for scanning countSyn */
  int           countScanId;

  /** Iterator for the count touples */
  @DumpDesc(ignore=true) TupleIterator countScan;

  /** Iterator for the output */
  @DumpDesc(ignore=true) TupleIterator outScan;

  ITuplePtr      leftTuple;
  ITuplePtr      rightTuple;

  ITuplePtr      countTuple;

  /** Except output tuple */
  ITuplePtr      outputTuple;

  long           nextOutputTs;

  long           outputTs;

  QueueElement.Kind   nextElementKind;

  QueueElement   outputElement;

  /** mininum expected next timestamp of left and right */
  long           minNextLeftTs;
  long           minNextRightTs;
  
  /** kind of last left element*/
  QueueElement.Kind lastLeftKind;
  
  /** kind of last right element*/
  QueueElement.Kind lastRightKind;
  
  /**
   * Constructor for ExceptState
   * @param ec TODO
   */
  public ExceptState(ExecContext ec)
  {
    super(ec);
    lastOutputTs    = Constants.MIN_EXEC_TIME;
    lastLeftTs      = Constants.MIN_EXEC_TIME;
    lastRightTs     = Constants.MIN_EXEC_TIME;
    minNextLeftTs   = Constants.MIN_EXEC_TIME;
    minNextRightTs  = Constants.MIN_EXEC_TIME;
    outputElement   = allocQueueElement();
    leftElementBuf  = allocQueueElement();
    rightElementBuf = allocQueueElement();
    lastLeftKind    = null;
    lastRightKind   = null;
    state           = ExecState.S_INIT;
  }
}
