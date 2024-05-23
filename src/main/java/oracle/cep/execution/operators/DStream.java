/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/DStream.java /main/28 2013/08/01 09:22:43 udeshmuk Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares DStream in package oracle.cep.execution.operators.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 udeshmuk  07/24/13 - bug 16966411
 anasrini  12/19/10 - replace eval() with eval(ec)
 udeshmuk  04/14/09 - correct problem in S_PROCESSING7
 udeshmuk  04/13/09 - add getDebugInfo to assertion
 parujain  04/06/09 - total ordering
 parujain  04/03/09 - ordering fix
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 hopark    10/07/08 - use execContext to remove statics
 sborah    09/25/08 - update stats
 sbishnoi  06/30/08 - overriding isHeartbeatPending and code alignment
 najain    04/04/08 - silent reln support
 hopark    02/28/08 - resurrect refcnt
 hopark    12/07/07 - cleanup spill
 hopark    10/30/07 - remove IQueueElement
 hopark    10/22/07 - remove TimeStamp
 parujain  10/04/07 - delete op
 hopark    09/07/07 - eval refactor
 hopark    07/13/07 - dump stack trace on exception
 parujain  06/26/07 - mutable state
 hopark    06/11/07 - logging - remove ExecContext
 hopark    05/24/07 - debug logging
 hopark    05/16/07 - add arguments for OutOfOrderException
 parujain  05/08/07 - monitoring statistics
 hopark    03/21/07 - add TuplePtr pin
 najain    03/14/07 - cleanup
 najain    03/12/07 - bug fix
 najain    02/15/07 - bug fix
 najain    01/05/07 - spill over support
 najain    01/24/07 - bug fix
 najain    11/15/06 - free count tuple
 najain    08/10/06 - add asserts
 najain    07/19/06 - ref-count tuples 
 najain    07/13/06 - ref-count timestamps 
 najain    07/12/06 - ref-count elem protocol 
 najain    07/06/06 - cleanup
 najain    05/23/06 - heartbeat
 ayalaman  05/09/06 - fix javadoc errors 
 ayalaman  04/29/06 - fix the output based on the tuple count 
 ayalaman  04/28/06 - Fix the iterator after deleteTuple 
 ayalaman  04/17/06 - Implementation
 skaluska  03/14/06 - query manager 
 skaluska  02/06/06 - Creation
 skaluska  02/06/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/DStream.java /main/28 2013/08/01 09:22:43 udeshmuk Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.util.logging.Level;

import oracle.cep.execution.ExecException;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.execution.SoftExecException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.ExecContext;

/**
 * @author skaluska
 */
public class DStream extends XStream
{
  /** Evaluator to check if the count is < 0 */
  private IBEval negEval;

  /**
   * Constructor for IStream
   * @param ec TODO
   */
  public DStream(ExecContext ec)
  {
    super(ExecOptType.EXEC_DSTREAM, new XStreamState(ec), ec);
  }

  /**
   * Getter for the negative tuple count evaluator
   * 
   * @return the evaluator instance
   */
  public IBEval getNegEval()
  {
    return this.negEval;
  }

  /**
   * Setter for the zero tuple count evaluator
   * 
   * @param negEval
   *          the evaluator instance
   */
  public void setNegEval(IBEval negEval)
  {
    this.negEval = negEval;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.operators.ExecOpt#run(int)
   */
  @Override
  public int run(int timeSlice) throws ExecException
  {
    QueueElement inpElement;
    int numElements;
    XStreamState iss = (XStreamState) mut_state;
    boolean done = false;
    ITuplePtr synTuple = null;
    boolean exitState = true;

    // state should have been initialized
    assert iss.state != ExecState.S_UNINIT;

    // Stats
    iss.stats.incrNumExecutions();
    
    try
    {
      // number of input elements to process
      numElements = timeSlice;

      while ((iss.stats.getNumInputs() < numElements) || (!exitState))
      {
        switch (iss.state)
        {
          case S_INIT: // read an input element
            inpElement = inputQueue.dequeue(iss.inputElementBuf);
            if (inpElement != null)
            {
              iss.inputElement = inpElement;
              iss.state = ExecState.S_INPUT_DEQUEUED;
              iss.inputTuple = iss.inputElement.getTuple();
            }
            else
            {
              // input queue is empty

              // process silent relation inputs
              if (silentRelns && 
                  (execMgr.getMaxSourceTime(silentRelnsList) <= 
                  iss.lastInputTs))
              {
                iss.state = ExecState.S_PROCESSING2; // prepare for output
                iss.processSilnReln = true;
                exitState = false;
                break;
              }
              else
              {
                if (outputQueue.isFull())
                {
                 done = true;
                 break;
                }
                
                if(iss.nextOutputTs > iss.lastOutputTs)
                {
                  iss.outputTs     = iss.nextOutputTs;
                  iss.lastOutputTs = iss.outputTs;
                  iss.outputElement.heartBeat(iss.outputTs);
                  iss.outputElement.setTotalOrderingGuarantee(false); 
                  outputQueue.enqueue(iss.outputElement);
             
                  iss.stats.incrNumOutputHeartbeats();
                }
                done = true;
                break;
              }
            }
          case S_INPUT_DEQUEUED:
            // update on counts
            if(iss.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
              iss.stats.incrNumInputHeartbeats();
            else
              iss.stats.incrNumInputs();

            // Get the timestamp
            iss.inputTs = iss.inputElement.getTs();

            // We should have a progress of time.
            if (iss.lastInputTs > iss.inputTs)
            {
                throw ExecException.OutOfOrderException(
                        this,
                        iss.lastInputTs, 
                        iss.inputTs, 
                        iss.inputElement.toString());
            }
     
            assert (iss.inputTs >= iss.minNextInputTs) : getDebugInfo(iss.inputTs,
              iss.minNextInputTs, iss.inputElement.getKind().name(),
              iss.lastInputKind.name());

            if(iss.inputTs == iss.lastInputTs)
              iss.tsEqual = true;
            else
              iss.tsEqual =false;
            
            // Update the last input Ts now
            iss.lastInputTs   = iss.inputTs;
            iss.lastInputKind = iss.inputElement.getKind();
           
            iss.lastTotalOrderingGuarantee = 
                                 iss.inputElement.getTotalOrderingGuarantee();
              
            iss.minNextInputTs = (iss.lastTotalOrderingGuarantee) 
                                 ? iss.lastInputTs+1 : iss.lastInputTs;

            iss.state = ExecState.S_PROCESSING1;
          case S_PROCESSING1:
            // if the timestamp of the input element is greater than
            // the nextOutputTs, we can produce the output (data) elements
            if (iss.nextOutputTs < iss.lastInputTs)
            {
              iss.state = ExecState.S_PROCESSING2; // prepare for output
              exitState = false;
            }
            else
            {
              // next outputTs is equal to input element's TS. Hence,
              // cannot output an nextOutputTS for now.
              iss.state = ExecState.S_PROCESSING5;
              break;
            }
          case S_PROCESSING2:
            // Produce output for s.nextOutputTs
            iss.tupleIter = synopsis.getScan(fullScanId);
            iss.nextScannedTuple = iss.tupleIter.getNext();
            iss.state = ExecState.S_PROCESSING3;
          case S_PROCESSING3:
            synTuple = iss.nextScannedTuple;
            if (synTuple == null)
            {
              // No more tuples
              synopsis.releaseScan(fullScanId, iss.tupleIter);
              //  We need to update nextOutputTs since we are processing
              // with the info that the next ts is going to be bigger
              iss.nextOutputTs = iss.lastInputTs;
              if (iss.processSilnReln)
              {
                iss.state = ExecState.S_INIT;
                iss.processSilnReln = false;
                done = true;
              }
              else if ((!iss.tsEqual) && (iss.lastTotalOrderingGuarantee))
              {
                iss.state = ExecState.S_PROCESSING7;
                break;
              }
              else if ((iss.tsEqual) && (iss.lastTotalOrderingGuarantee))
              {
                // when ts is equal then we have already inserted the tuple
                // in the synopsis
                iss.state = ExecState.S_INPUT_ELEM_CONSUMED;
                iss.tsEqual = false;
                iss.lastTotalOrderingGuarantee = false;
                break;
              }
              else
                iss.state = ExecState.S_PROCESSING1;
              break;
            }
            else
            {
              evalContext.bind(synTuple, IEvalContext.SYN_ROLE);
              iss.nextScannedTuple = iss.tupleIter.getNext();
              if (negEval.eval(evalContext))
              {
                iss.state = ExecState.S_OUTPUT_TIMESTAMP;
              }
              else
              {
                // get the next tuple. This tuple has negative count and
                // we are not supposed to generate an output tuple for that.
                // also delete the tuple from the sysnopsis (without
                // generating any output - negative tuple).
                synopsis.deleteTuple(synTuple);
                synStoreTupleFactory.release(synTuple);
                synStoreTupleFactory.release(synTuple);
                synopsis.releaseScan(fullScanId, iss.tupleIter);
                iss.state = ExecState.S_PROCESSING2;
                break;
              }
            }
          case S_OUTPUT_TIMESTAMP:
            iss.outputTs = iss.nextOutputTs;
            iss.state = ExecState.S_PROCESSING4;
          case S_PROCESSING4:
            // allocate space for the current tuple and prepare for output
            iss.outputTuple = tupleStorageAlloc.allocate();
            evalContext.bind(iss.outputTuple, IEvalContext.NEW_OUTPUT_ROLE);
            outEval.eval(evalContext);
            iss.state = ExecState.S_OUTPUT_ELEMENT;
          case S_OUTPUT_ELEMENT:
            iss.outputElement.setKind(QueueElement.Kind.E_PLUS);
            iss.outputElement.setTs(iss.outputTs);
            iss.lastOutputTs = iss.outputTs;
            iss.outputElement.setTuple(iss.outputTuple);
            if(iss.lastTotalOrderingGuarantee &&
               iss.tsEqual &&
               (iss.nextScannedTuple == null))
            {
              iss.outputElement.setTotalOrderingGuarantee(true);
            }
            else
              iss.outputElement.setTotalOrderingGuarantee(false);
            iss.state = ExecState.S_OUTPUT_READY;
          case S_OUTPUT_READY:
            if (outputQueue.isFull())
            {
              done = true;
              break;
            }
            outputQueue.enqueue(iss.outputElement);
            // increment the count of the tuple (negative count)
            incrEval.eval(evalContext);
            iss.state = ExecState.S_OUTPUT_ENQUEUED;
          case S_OUTPUT_ENQUEUED:
            iss.stats.incrNumOutputs();
            if (negEval.eval(evalContext))
            {
              // if the tuple count is still negative we will continue
              // to produce more copies of the same tuple (with SYN_ROLE)
              iss.state = ExecState.S_PROCESSING4;
            }
            else
            {
              // We have generated the correct number of copies of the
              // tuple from input synopsis. Note that the synopsis
              // guarantees that the scan continues to be valid even after
              // this deletion.
              if (synTuple != null)
              {
                synopsis.deleteTuple(synTuple);
                synStoreTupleFactory.release(synTuple);
                synStoreTupleFactory.release(synTuple);
                synTuple = null;
                // delete tuple also deletes the tuple from the synopsis
                // tuple linked list completely. So, the scan is not valid
                // any more. Lets reinitialize it.
                synopsis.releaseScan(fullScanId, iss.tupleIter);
                iss.state = ExecState.S_PROCESSING2;
              }
              else
                // Else, get the next tuple in the scan to output
                iss.state = ExecState.S_PROCESSING3;
            }
            break;
          case S_PROCESSING5:
            // No more output for now, just process the current element
            // by updating the synopsis to reflect the instantaneous
            // input relation at timestamp s.inputElement.getTs()
            if (iss.inputElement.getKind() == QueueElement.Kind.E_PLUS)
            {
              handlePlus(iss.inputElement);
            }
            else if (iss.inputElement.getKind() == QueueElement.Kind.E_MINUS)
            {
              handleMinus(iss.inputElement);
            }
            // the heartbeat needs to be propagated
            else
            {
              iss.outputTs = iss.inputTs;
              iss.outputElement.heartBeat(iss.outputTs);
              iss.lastOutputTs = iss.outputTs;
              /*
               * Bug 16966411:
               * Encountered an issue of incorrect TOF setting by DStream while
               * investigating 16966411. The issue came only when all inputs 
               * in the test data file had TOF set to true. This was resulting
               * in assertion error in output operator downstream.
               * 
               * We cannot always copy over the last input TOF here.
               * Particularly if we have received a hb with TOF then
               * if(tsEqual)
               *   We go to s_processing2 where we could be outputting 
               *   additional tuples by scanning synopsis. So we should 
               *   set the TOF to false while propagating this heartbeat.
               * else
               *   We can copy over the lastTotalOrderingGuarantee.
               */
              if(iss.tsEqual)
                iss.outputElement.setTotalOrderingGuarantee(false);
              else
            	iss.outputElement.setTotalOrderingGuarantee(iss.lastTotalOrderingGuarantee);

              iss.nextOutputTs = iss.lastOutputTs;
              if (outputQueue.isFull())
              {
                done = true;
                break;
              }
              outputQueue.enqueue(iss.outputElement);
              
              iss.stats.incrNumOutputHeartbeats();
            }
            if(iss.tsEqual && iss.lastTotalOrderingGuarantee)
            {
              iss.state = ExecState.S_PROCESSING2;
              break;
            }
            else
              iss.state = ExecState.S_INPUT_ELEM_CONSUMED;

          case S_INPUT_ELEM_CONSUMED:
            assert iss.inputElement != null;

            if (iss.inputTuple != null)
            {
              inTupleStorageAlloc.release(iss.inputTuple);
            }

            iss.state = ExecState.S_INIT;
            exitState = true;
            break;
          case S_PROCESSING7:
            if (iss.inputElement.getKind() == QueueElement.Kind.E_PLUS)
            {
              iss.state = ExecState.S_INPUT_ELEM_CONSUMED;
              break;
            }
            else if(iss.inputElement.getKind() == QueueElement.Kind.E_MINUS)
            {
              iss.state = ExecState.S_PROCESSING8;
            }
            // the heartbeat needs to be propagated
            else
            {
              iss.outputTs = iss.inputTs;
              /* update lastOutputTs with heartbeat timestamp*/
              iss.lastOutputTs = iss.outputTs;
              iss.outputElement.heartBeat(iss.outputTs);
              iss.outputElement.setTotalOrderingGuarantee
                                       (iss.lastTotalOrderingGuarantee); 
              iss.nextOutputTs = iss.lastOutputTs;
              if (outputQueue.isFull())
              {
                done = true;
                break;
              }
              outputQueue.enqueue(iss.outputElement);
              iss.stats.incrNumOutputHeartbeats();
              iss.state = ExecState.S_INPUT_ELEM_CONSUMED;
              break;
            }
          case S_PROCESSING8:
            iss.outputTs = iss.nextOutputTs;
            // update lastOutputTs
            iss.lastOutputTs = iss.outputTs;
            evalContext.bind(iss.inputTuple, IEvalContext.SYN_ROLE);
            // allocate space for the current tuple and prepare for output
            iss.outputTuple = tupleStorageAlloc.allocate();
            evalContext.bind(iss.outputTuple, IEvalContext.NEW_OUTPUT_ROLE);
            outEval.eval(evalContext);
            iss.outputElement.setKind(QueueElement.Kind.E_PLUS);
            iss.outputElement.setTs(iss.outputTs);
            iss.outputElement.setTuple(iss.outputTuple);
            iss.outputElement.setTotalOrderingGuarantee(true);
            iss.lastTotalOrderingGuarantee = false;
            iss.state = ExecState.S_LAST_OUTPUT_ELEMENT;
              
          case S_LAST_OUTPUT_ELEMENT:
            if (outputQueue.isFull())
            {
              done = true;
              break;
            }
            outputQueue.enqueue(iss.outputElement);  
            iss.state = ExecState.S_INPUT_ELEM_CONSUMED;
            iss.stats.incrNumOutputs();
            break;
          default:
            assert false;
        }
        if (done)
          break;
      }
    }
    catch (SoftExecException e1)
    {
      // TODO Ignore them
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e1);
      return 0;
    }
    
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.operators.ExecOpt#deleteOp()
   */
  @Override
  public void deleteOp()
  {
    // TODO Auto-generated method stub

  }

}
