/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/RStream.java /main/30 2011/02/07 03:36:26 sborah Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Execution layer RSTREAM operator

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 anasrini  12/19/10 - replace eval() with eval(ec)
 anasrini  05/12/09 - piggybacking, new semantics
 sbishnoi  04/13/09 - adding assertion for minNextTs
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 sborah    09/25/08 - update stats
 sbishnoi  07/01/08 - Overriding isHeartbeatPending
 sbishnoi  06/26/08 - updating lastOutputTs
 najain    04/04/08 - silent reln support
 hopark    02/28/08 - resurrect refcnt
 hopark    12/07/07 - cleanup spill
 hopark    10/30/07 - remove IQueueElement
 hopark    10/22/07 - remove TimeStamp
 najain    10/10/07 - RStream optimization 
 parujain  10/04/07 - delete op
 hopark    09/07/07 - eval refactor
 hopark    07/13/07 - dump stack trace on exception
 parujain  06/26/07 - mutable state
 hopark    05/24/07 - debug logging
 hopark    05/16/07 - add arguments for OutOfOrderException
 parujain  05/08/07 - monitoring statistics
 hopark    04/09/07 - fix pincount
 hopark    03/24/07 - add unpin
 najain    03/14/07 - cleanup
 najain    03/12/07 - bug fix
 najain    02/19/07 - bug fix
 najain    01/05/07 - spill over support
 parujain  12/19/06 - fullScan for Relation Synopsis
 najain    11/15/06 - bug fix
 najain    08/02/06 - refCounting optimizations
 najain    08/10/06 - add asserts
 najain    07/19/06 - ref-count tuples 
 najain    07/13/06 - bug fix 
 najain    07/13/06 - ref-count timestamps 
 najain    07/12/06 - ref-count elem protocol
 najain    05/23/06 - heartbeat
 anasrini  04/05/06 - implementation
 skaluska  03/14/06 - query manager 
 skaluska  02/06/06 - Creation
 skaluska  02/06/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/RStream.java st_pcbpel_anasrini_eval_parallelism_2/1 2010/12/19 07:35:41 anasrini Exp $
 *  @author  skaluska
 *  @since   1.0
 */

package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.execution.snapshot.SynopsisPersistenceContext;
import oracle.cep.execution.synopses.RelationSynopsis;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.ExecContext;

/**
 * Execution layer RSTREAM operator
 * 
 * @author skaluska
 * @since 1.0
 */
public class RStream extends ExecOpt {
  /** evaluation context */
  private IEvalContext evalContext;

  /** Evaluator that copies a tuple in the input synopsis to the output */
  private IAEval copyEval;

  /** roles for evaluation contex */
  private static final int INPUT_ROLE = IEvalContext.INPUT_ROLE;

  private static final int OUTPUT_ROLE = IEvalContext.NEW_OUTPUT_ROLE;

  /** Synopsis storing the input relation */
  private RelationSynopsis synopsis;

  /** scan id to get all the tuples in the synopsis */
  private int scanId;

  /* does the operator only depend on silent Relations */
  boolean silentRelns;

  // The list of silent Relations that the operator depends on: This is needed
  // to propagate the heartbeat in case of a stall or a silent relation.
  // Currently, silent streams/relations are not handled, only static
  // relations
  // (one for which the time is not specifed) and handled appropriately.
  LinkedList<RelSource> silentRelnsList;

  public void setSilentRelns(boolean silentRelns) {
    this.silentRelns = silentRelns;
  }

  public void addInputRelns(RelSource execOp) {
    if (silentRelnsList == null)
      silentRelnsList = new LinkedList<RelSource>();

    silentRelnsList.add(execOp);
  }

  /**
   * Constructor for RStream
   * @param ec TODO
   */
  public RStream(ExecContext ec) {
    super(ExecOptType.EXEC_RSTREAM, new RStreamState(ec), ec);
  }

  /**
   * Getter for synopsis in RStream
   * 
   * @return Returns the synopsis
   */
  public RelationSynopsis getSynopsis() {
    return synopsis;
  }

  /**
   * Setter for synopsis in RStream
   * 
   * @param synopsis
   *            The outSynopsis to set.
   */
  public void setSynopsis(RelationSynopsis synopsis) {
    this.synopsis = synopsis;
  }

  /**
   * Setter for full scan id
   * 
   * @param scanId
   *            the id of the scan
   */
  public void setScan(int scanId) {
    this.scanId = scanId;
    this.propScanId = scanId;
  }

  /**
   * Getter for evalContext in RStream
   * 
   * @return Returns the evalContext
   */
  public IEvalContext getEvalContext() {
    return evalContext;
  }

  /**
   * Setter for evalContext in RStream
   * 
   * @param evalContext
   *            The evalContext to set.
   */
  public void setEvalContext(IEvalContext evalContext) {
    this.evalContext = evalContext;
  }

  /**
   * Getter for copyEval in RStream
   * 
   * @return Returns the copyEval
   */
  public IAEval getCopyEval() {
    return copyEval;
  }

  /**
   * Setter for copyEval in RStream
   * 
   * @param copyEval
   *            The copyEval to set.
   */
  public void setCopyEval(IAEval copyEval) {
    this.copyEval = copyEval;
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
    boolean done = false;
    RStreamState s = (RStreamState) mut_state;
    boolean exitState = true;
    boolean outGflag = false;
    assert s.state != ExecState.S_UNINIT;

    // Stats
    s.stats.incrNumExecutions();

    try 
    {
      // Number of input elements to process
      numElements = timeSlice;
      while ((s.stats.getNumInputs() < numElements) || (!exitState)) 
      {
        switch (s.state) 
        {
        case S_INIT:
          // Get next input element
          inpElement = inputQueue.dequeue(s.inputElementBuf);
          if (inpElement != null) 
          {
            s.inputElement = inpElement;
            s.inputTuple = inpElement.getTuple();
            //System.out.println(s.inputElement);
            s.state = ExecState.S_INPUT_DEQUEUED;
          } 
          else
          {
            // process silent relation inputs
            if (enableSilentRelnProcessing 
                && silentRelns
                && (execMgr.getMaxSourceTime(silentRelnsList) <= s.lastInputTs)) 
            {
              s.state = ExecState.S_PROCESSING2; // prepare for output
              s.processSilnReln = true;
              exitState = false;
              break;
            } 
            else 
            {
              if (s.lastOutputTs < s.lastInputTs)
                s.state = ExecState.S_GENERATE_HEARTBEAT;
              else
                done = true;
              break;
            }
          }
        case S_INPUT_DEQUEUED:
          // Update our counts
          exitState = false;
          if(s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
            s.stats.incrNumInputHeartbeats();
          else
            s.stats.incrNumInputs();

          // Get the timestamp
          s.inputTs = s.inputElement.getTs();

          // We should have a progress of time.
          if (s.lastInputTs > s.inputTs)
          {
            throw ExecException.OutOfOrderException(this,
                s.lastInputTs, s.inputTs, s.inputElement
                .toString());
          }
          
          // Check if the current input timeStamp shouldn't be less than the
          // calculated expected timeStamp in previous execution
          assert s.inputTs >= s.minNextTs :
            getDebugInfo(s.inputTs, s.minNextTs, 
                s.inputElement.getKind().name(), s.lastInputKind.name());

          // Update the minNextTs
          s.minNextTs = s.inputElement.getTotalOrderingGuarantee() ?
                        s.inputTs + 1 :
                        s.inputTs;

          // Update the last input Ts now
          s.lastInputTs   = s.inputTs;
          s.lastInputKind = s.inputElement.getKind();
          s.inpGflag      = s.inputElement.getTotalOrderingGuarantee();

          s.state = ExecState.S_PROCESSING1;
        case S_PROCESSING1:
          if (s.backlogProcessing && s.nextOutputTs < s.lastInputTs)
          {
            // stream the contents of the synopsis corresponding
            // to nextOutputTs
            s.state = ExecState.S_PROCESSING2;

            // Then, process current input
            s.nextState = ExecState.S_PROCESSING5;
          }
          else
          {
            // Process current input first
            s.nextOutputTs = s.lastInputTs;
            s.state = ExecState.S_PROCESSING5;
            break;
          }
        case S_PROCESSING2:
          // Produce output for s.nextOutputTs
          s.tupleIter      = synopsis.getScan(scanId);
          s.lookAheadTuple = s.tupleIter.getNext();
          s.state = ExecState.S_PROCESSING3;
        case S_PROCESSING3:
          s.outputTuple = s.lookAheadTuple;
          if (s.outputTuple == null) 
          {
            // No more tuples
            synopsis.releaseScan(scanId, s.tupleIter);
            s.nextOutputTs = s.lastInputTs;
            s.backlogProcessing = false;
            
            if (s.processSilnReln) 
            {
              s.state = ExecState.S_INIT;
              s.processSilnReln = false;
              done = true;
            }
            else
              s.state = s.nextState;

            break;
          }
          else 
          {
            s.lookAheadTuple = s.tupleIter.getNext();
            outGflag = (s.lookAheadTuple == null);
            evalContext.bind(s.outputTuple, INPUT_ROLE);
            s.state = ExecState.S_OUTPUT_TIMESTAMP;
          }
        case S_OUTPUT_TIMESTAMP:
          s.outputTs = s.nextOutputTs;
          /* update lastOuputTs with the output Tuple's timestamp*/
          s.lastOutputTs = s.outputTs;
          s.state = ExecState.S_PROCESSING4;
        case S_PROCESSING4:
          s.outputTuple = tupleStorageAlloc.allocate();
          evalContext.bind(s.outputTuple, OUTPUT_ROLE);
          copyEval.eval(evalContext);
          s.state = ExecState.S_OUTPUT_ELEMENT;
        case S_OUTPUT_ELEMENT:
          s.outputElement.setKind(QueueElement.Kind.E_PLUS);
          s.outputElement.setTs(s.outputTs);

          s.outputElement.setTotalOrderingGuarantee(outGflag);
          s.outputElement.setTuple(s.outputTuple);
          s.state = ExecState.S_OUTPUT_READY;
        case S_OUTPUT_READY:
          if (outputQueue.isFull())
          {
            done = true;
            break;
          }
          outputQueue.enqueue(s.outputElement);
          s.state = ExecState.S_OUTPUT_ENQUEUED;
        case S_OUTPUT_ENQUEUED:
          s.stats.incrNumOutputs();
          // Now, get the next tuple in the scan to output
          s.state = ExecState.S_PROCESSING3;
          break;
        case S_PROCESSING5:
          // No more output for now, just process the current element
          // by updating the synopsis to reflect the instantaneous
          // input relation at timestamp s.inputElement.getTs()
          if (s.inputElement.getKind() == QueueElement.Kind.E_PLUS)
          {
            synopsis.insertTuple(s.inputTuple);

            if (s.inpGflag)
            {
              s.state = ExecState.S_PROCESSING2;
              s.nextState = ExecState.S_INPUT_ELEM_CONSUMED;
              break;
            }
            else
            {
              s.backlogProcessing = true;
            }
          }
          else if (s.inputElement.getKind() == QueueElement.Kind.E_MINUS) 
          {
            synopsis.deleteTuple(s.inputTuple);
            //inTupleStorageAlloc.release(s.inputTuple);

            if (s.inpGflag)
            {
              s.state = ExecState.S_PROCESSING2;
              s.nextState = ExecState.S_INPUT_ELEM_CONSUMED;
              break;
            }
            else
            {
              s.backlogProcessing = true;
            }
          }
          else 
          {
            if (s.inpGflag && s.backlogProcessing)
            {
              s.state = ExecState.S_PROCESSING2;
              s.nextState = ExecState.S_INPUT_ELEM_CONSUMED;
              break;
            }
          }


          s.state = ExecState.S_INPUT_ELEM_CONSUMED;
          
        case S_INPUT_ELEM_CONSUMED:
          assert s.inputElement != null;

          if (s.inputTuple != null) 
          {
            inTupleStorageAlloc.release(s.inputTuple);
            s.inputTuple = null;
          }

          exitState = true;
          s.state = ExecState.S_INIT;
          break;

        case S_GENERATE_HEARTBEAT:
          if (s.lastOutputTs < s.lastInputTs)
          {
            s.outputTs = s.lastInputTs;
            s.lastOutputTs = s.outputTs;
            s.outputElement.heartBeat(s.outputTs);
            outGflag = s.inpGflag && !s.backlogProcessing;
            s.outputElement.setTotalOrderingGuarantee(outGflag);
            if (outputQueue.isFull()) 
            {
              done = true;
              break;
            }
            outputQueue.enqueue(s.outputElement);

            s.stats.incrNumOutputHeartbeats();
          }
          done = true;
          s.state = ExecState.S_INIT;
          break;

        default:
          assert false;
        }
        if (done){
          s.stats.setEndTime(System.nanoTime());
          break;
        }
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
  public void deleteOp() {
    // TODO Auto-generated method stub

  }

  // If this operator is scheduled; and input element is null;
  // It doesn't perform any task;
  // So isHeartbeatPending() is false;
  @Override
  public boolean isHeartbeatPending() {
    return false;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<RStream id=\"" + id + "\" >");
    sb.append("<InputQueue id=\"" + inputQueue.getId() + "\"/>");
    sb.append("<OutputQueue>" + outputQueue.toString() + "</OutputQueue>");
    sb.append("<Synopsis>");
    sb.append(synopsis.toString());
    sb.append("</Synopsis>");
    sb.append("<OutTupleAlloc>" + tupleStorageAlloc.toString()
        + "</OutTupleAlloc>");
    sb.append("<CopyEval>");
    sb.append(copyEval.toString());
    sb.append("</CopyEval>");
    sb.append("</RStream>");

    return sb.toString();
  }
  
  /**
   * Create snapshot of RStream operator by writing the RStream operator state.
   */
  @Override
  protected void createSnapshot(ObjectOutputStream output) throws CEPException
  {
    try
    {
      //snapshot mutable state
      output.writeObject((RStreamState)mut_state);
      //snapshot synopsis.
      synopsis.writeExternal(output, new SynopsisPersistenceContext(scanId));
    } catch (IOException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE,e);
      throw new CEPException(ExecutionError.SNAPSHOT_CREATION_ERROR, e, e.getLocalizedMessage(), this.getOptName());
    }
  }
  
  @Override
  protected void loadSnapshot(ObjectInputStream input) throws ExecException
  {
    try
    {
      //read mutable state
      RStreamState mutable_state = (RStreamState)input.readObject();
      ((RStreamState)mut_state).copyFrom(mutable_state);
      
      // Synopsis of RStream is sharing store of parent operator
      // which is producing a relation.
      // While recovering, the parent operator will populate the store
      // and we don't want to add duplicate tuples in the store while
      // loading synopsis of this operator.
      // To avoid duplicate, We will scan the store and determine the
      // set of tuple ids which are already recovered.
      // While recovering the synopsis, we will not save the events
      // which are already existing in this set.
      IPersistenceContext persistenceContext = new SynopsisPersistenceContext();
      HashSet existingRecoveredTuples = new HashSet();
      persistenceContext.setCache(existingRecoveredTuples);
      TupleIterator itr = synopsis.getScan(scanId);
      ITuplePtr next= itr.getNext();
      while(next != null) {
    	  existingRecoveredTuples.add(next.getId());
    	  next = itr.getNext();
      }
      
    //read synopsis
      synopsis.readExternal(input,persistenceContext);
    } catch (ClassNotFoundException | IOException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE,e);
      throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR, e, e.getLocalizedMessage(), getOptName());
    }
  }

}
