/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/IStream.java /main/31 2012/06/18 06:29:07 udeshmuk Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares IStream in package oracle.cep.execution.operators.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 udeshmuk  06/07/12 - fix the problem when multiple tuples of same ts come
                      followed by hb of the same ts and ordering guarantee true
 anasrini  12/19/10 - replace eval() with eval(ec)
 sbishnoi  04/30/09 - fixing total ordering
 parujain  04/06/09 - total ordering
 parujain  04/03/09 - ordering fix
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 hopark    10/07/08 - use execContext to remove statics
 sborah    09/25/08 - update stats
 sbishnoi  06/26/08 - updating lastOutputTs
 najain    04/03/08 - silent relns support
 hopark    02/28/08 - resurrect refcnt
 hopark    12/07/07 - cleanup spill
 hopark    10/30/07 - remove IQueueElement
 hopark    10/22/07 - remove TimeStamp
 parujain  10/04/07 - delete op
 hopark    09/07/07 - eval refactor
 hopark    07/13/07 - dump stack trace on exception
 parujain  06/26/07 - mutable state
 hopark    05/18/07 - add logging in/out
 hopark    05/16/07 - add arguments for OutOfOrderException
 parujain  05/08/07 - monitoring statistics
 hopark    04/05/07 - memmgr reorg
 najain    04/02/07 - bug fix
 hopark    03/24/07 - add unpin
 najain    03/14/07 - cleanup
 najain    03/12/07 - bug fix
 najain    02/15/07 - bug fix
 najain    01/05/07 - spill over support
 najain    01/24/07 - bug fix
 najain    11/15/06 - free count tuple
 najain    08/02/06 - refCounting optimizations
 najain    08/10/06 - add asserts
 najain    07/19/06 - ref-count tuples 
 najain    07/13/06 - ref-count timestamps 
 najain    07/12/06 - ref-count elem protocol 
 najain    07/06/06 - cleanup
 najain    05/23/06 - heartbeat
 ayalaman  05/09/06 - fix javadoc errors 
 najain    05/05/06 - dump
 ayalaman  04/29/06 - fix the output based on the tuple count  
 ayalaman  04/28/06 - delete tuple -> new iterator instance 
 ayalaman  04/17/06 - Implementation
 skaluska  03/14/06 - query manager 
 skaluska  02/06/06 - Creation
 skaluska  02/06/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/IStream.java /main/31 2012/06/18 06:29:07 udeshmuk Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.HashSet;
import java.util.LinkedList;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.execution.snapshot.SynopsisPersistenceContext;
import oracle.cep.execution.synopses.RelationSynopsis;
import oracle.cep.execution.SoftExecException;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.ExecContext;

/**
 * IStream
 *
 * @author skaluska
 */
public class IStream extends XStream
{
  /** Evaluator to check if the count is > 0 */
  private IBEval    posEval;   

  /**
   * ISTREAM operator with NOT IN semantics - used when defined on a subset of
   * SELECT expressions. This is denoted in various places as ISTREAM2 or
   * ISTREAM (not in) in shorthand
   */
  private boolean   notInSem; 

  /**
   * Synopsis storing all tuples that appeared at (current time - 1) instant,
   * i.e. R(t-1).
   */
  private RelationSynopsis inSyn;

  /**
   * In memory list to holds tuples, negative and positive, as they arrive at
   * time t -- applicable only in ISTREAM(not in) case.
   *
   * !!! CAVEAT !!!
   * Ideally a synopsis should be used here, but currently synopses
   * store only tuples without the tuple kind. Tuple kind is necessary to
   * maintain a proper view of the relation. However, we need to make
   * sure that this list is captured properly during "spilling" or
   * "state capture".
   */
  private LinkedList<QueueElement> nowList; 

  /* scan id for scan on input synopsis */
  private int              inScanId;
  
  /**
   * full scan id for input synopsis.
   * this is required to create snapshot for HA
   */
  private int fullInScanId;

  /**
   * Constructor for IStream
   * @param ec TODO
   */
  public IStream(ExecContext ec)
  {
    super(ExecOptType.EXEC_ISTREAM, new XStreamState(ec), ec);
    inSyn = null;
    nowList = null;
  }

  /**
   * Getter for the positive tuple count evaluator
   * 
   * @return the evaluator instance
   */
  public IBEval getPosEval()
  {
    return this.posEval;
  }

  /** returns true if this ISTREAM operator implements NOT IN semantics,
   * else it is regular multiset difference
   */
  public boolean getIstreamSem()
  {
    return this.notInSem;
  }

  /**
   * Getter for input synopsis in ISTREAM (not in)
   * 
   * @return returns the input synopsis
   */
  public RelationSynopsis getInSynopsis()
  {
    return inSyn;
  }

  /**
   * Getter for input synopsis scan id 
   * 
   * @return returns the input synopsis scan id
   */
  public int getInScanId()
  {
    return inScanId;
  }
  
  public int getFullInScanId()
  {
    return fullInScanId;
  }
  
  public void setFullInScanId(int fullInScanId)
  {
    this.fullInScanId = fullInScanId;
  }

  /**
   * Setter for the now list
   * 
   * @param nowlist
   *          list of tuples at current time t.
   */
  public void setNowList(LinkedList<QueueElement> nowlist)
  {
    this.nowList = nowlist;
  }

  /**
   * Setter for the zero tuple count evaluator
   * 
   * @param posEval
   *          the evaluator instance
   */
  public void setPosEval(IBEval posEval)
  {
    this.posEval = posEval;
  }

  /**
   * Setter for the zero tuple count evaluator
   * 
   * @param posEval
   *          the evaluator instance
   */
  public void setIstreamSem(boolean sem) 
  {
    this.notInSem = sem;
  }

  /**
   * Setter for the input synopsis
   * 
   * @param inSyn
   *          input synopsis
   */
  public void setInSynopsis(RelationSynopsis inSyn)
  {
    this.inSyn = inSyn;
  }

  /**
   * Setter for the input synopsis scan id
   * 
   * @param inScan
   *          input synopsis scan id
   */
  public void setInScanId(int scanid)
  {
    this.inScanId = scanid;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.operators.ExecOpt#run(int)
   */
  @Override
  public int run(int timeSlice) throws ExecException
  {
    int numElements;
    XStreamState iss = (XStreamState) mut_state;
    boolean done = false;
    ITuplePtr synTuple = null;
    boolean exitState = true;
    boolean istream2 = false;   

    /* first figure out whether we're implementing NOT IN semantics */
    if (getIstreamSem()) {
      istream2 = true;
    }

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
          case S_INIT:
            // read an input element
            iss.inputElement = inputQueue.dequeue(iss.inputElementBuf);
            iss.state = ExecState.S_INPUT_DEQUEUED;
          case S_INPUT_DEQUEUED:
            if (iss.inputElement == null)
            {
              // process silent relation inputs
              if (enableSilentRelnProcessing &&
                  silentRelns && 
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
                  /* Generate HEARTBEAT equal to nextOutputTs and update
                   * lastOutputTs
                   *
                   * (vikshukl): update r(t-1) here?
                   */
                  iss.outputTs     = iss.nextOutputTs;
                  iss.lastOutputTs = iss.outputTs;
                  iss.outputElement.heartBeat(iss.outputTs);
                  iss.outputElement.setTotalOrderingGuarantee(false); 
                  outputQueue.enqueue(iss.outputElement);
                  iss.stats.incrNumOutputHeartbeats();
                }

                iss.state = ExecState.S_INIT;
                done = true;
                iss.stats.setEndTime(System.nanoTime());
                break;
              }
            }
            else
            {
              // Bump up our counts
              if(iss.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
                iss.stats.incrNumInputHeartbeats();
              else
                iss.stats.incrNumInputs();

              // extract tuple and timestampe
              iss.inputTuple = iss.inputElement.getTuple();
              iss.inputTs = iss.inputElement.getTs();
              
              assert iss.inputTs >= iss.minNextInputTs :
                getDebugInfo(iss.inputTs, iss.minNextInputTs,
                  iss.inputElement.getKind().name(), iss.lastInputKind.name());

              // We should have a progress of time.
              if (iss.lastInputTs > iss.inputTs)
              {
                throw ExecException.OutOfOrderException(
                        this,
                        iss.lastInputTs, 
                        iss.inputTs, 
                        iss.inputElement.toString());
              }
              
              if(iss.inputTs == iss.lastInputTs)
                iss.tsEqual = true;
              else {
                iss.tsEqual = false;
                if (istream2) {
                  /* current input time is greater than lastInputTs, so update
                   * the synopsis R(t-1).
                   */
                  while (nowList.size() > 0) {

                    QueueElement inputElem = nowList.poll();

                    if (inputElem.getKind() == QueueElement.Kind.E_PLUS) {
                      inSyn.insertTuple(inputElem.getTuple());
                    }
                    else if (inputElem.getKind() == QueueElement.Kind.E_MINUS) {
                      inSyn.deleteTuple(inputElem.getTuple());
                    }

                  }
                }
              }        
              // Update the last input Ts now
              iss.lastInputTs = iss.inputTs;
              iss.lastInputKind = iss.inputElement.getKind();

              iss.lastTotalOrderingGuarantee = 
                                 iss.inputElement.getTotalOrderingGuarantee();
              
              iss.minNextInputTs = (iss.lastTotalOrderingGuarantee) 
                                 ? iss.lastInputTs+1 : iss.lastInputTs;
           
              iss.state = ExecState.S_PROCESSING1;
            }

          case S_PROCESSING1:
            if (iss.nextOutputTs < iss.lastInputTs)
            {
              /* if the timestamp of the input element is greater than the
               * nextOutputTs, we can produce the output (data) elements
               */
              iss.state = ExecState.S_PROCESSING2; 
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
              // We need to update nextOutputTs since we are processing
              // with the info that the next ts is going to be bigger
              iss.nextOutputTs =  iss.lastInputTs;

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
              iss.nextScannedTuple = iss.tupleIter.getNext();
              evalContext.bind(synTuple, IEvalContext.SYN_ROLE);
              if (posEval.eval(evalContext)) // uses tuple in SYN_ROLE
              {
                iss.state = ExecState.S_OUTPUT_TIMESTAMP;
              }
              else
              {
                // get the next tuple. This tuple has negative count and
                // we are not supposed to generate an output tuple for that.
                // also delete the tuple from the synopsis (without
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
            // update lastOutputTs
            iss.lastOutputTs = iss.outputTs;         
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
            iss.outputElement.setTuple(iss.outputTuple);

            // decrement the count of the tuple
            decrEval.eval(evalContext);

            if (iss.lastTotalOrderingGuarantee &&
                iss.tsEqual &&
                iss.nextScannedTuple == null &&
                !posEval.eval(evalContext))
            {
              iss.outputElement.setTotalOrderingGuarantee(true);
            }
            else
            {
              iss.outputElement.setTotalOrderingGuarantee(false);
            }
            iss.state = ExecState.S_OUTPUT_READY;
          case S_OUTPUT_READY:
            if (outputQueue.isFull())
            {
              done = true;
              break;
            }
            outputQueue.enqueue(iss.outputElement);
            iss.state = ExecState.S_OUTPUT_ENQUEUED;
          case S_OUTPUT_ENQUEUED:
            if(iss.outputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
              iss.stats.incrNumOutputHeartbeats();
            else
              iss.stats.incrNumOutputs();
            if (posEval.eval(evalContext)) {
              // if the tuple count is still positive we will continue
              // to produce more copies of the same tuple (with SYN_ROLE)
              iss.state = ExecState.S_PROCESSING4;
            }
            else {
              // We have generated the correct number of copies of the
              // tuple from input synopsis.
              // TODO: (This does not work here) Note that the synopsis
              // guarantees that the scan continues to be valid even after
              // this deletion.
              if (synTuple != null) {
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
              else {
                // Else, get the next tuple in the scan to output
                iss.state = ExecState.S_PROCESSING3;
              }
            }
            break;
          case S_PROCESSING5:
            // No more output for now, just process the current element by
            // updating the synopsis to reflect the instantaneous input
            // relation at timestamp s.inputElement.getTs()
            if (iss.inputElement.getKind() == QueueElement.Kind.E_PLUS)
            {
              if (istream2) {
                /* 
                 * for ISTREAM (not in) we can output this tuple only if 
                 * it does not exist in R(t-1) synopsis, where the match key
                 * is a subset of columns.
                 */
                TupleIterator tupIter;
                ITuplePtr inSynTuple;
                ITuplePtr inTuple; 

                inTuple = iss.inputElement.getTuple();
                evalContext.bind(inTuple, IEvalContext.INPUT_ROLE);
                  
                /* scan the synopsis */
                tupIter = inSyn.getScan(inScanId);
                inSynTuple = tupIter.getNext();  

                /* release the scan, as the synopsis will be updated later */
                inSyn.releaseScan(inScanId, tupIter);

                /* save away the elements for updating r(t-1) synopsis later */
                QueueElement temp = mut_state.allocQueueElement();
                temp.copy(iss.inputElement);
                nowList.add(temp); 

                if (inSynTuple != null) {
                  /* found a match, so can't output this tuple */
                  iss.state = ExecState.S_INPUT_ELEM_CONSUMED;
                  break;
                }
              }

              handlePlus(iss.inputElement);

            }
            else if (iss.inputElement.getKind() == QueueElement.Kind.E_MINUS)
            {
              if (istream2) {
                /* add this to the list to properly maintain R(t-1) input
                 * synopsis.
                 */
                QueueElement temp = mut_state.allocQueueElement();
                temp.copy(iss.inputElement);
                nowList.add(temp);
              }            

              handleMinus(iss.inputElement);

            }
            else
            {
              /* propagate heartbeat */
	      if(iss.inputTs > iss.lastOutputTs)
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
              }
            }

            if(iss.tsEqual && iss.lastTotalOrderingGuarantee)
            {
              iss.state = ExecState.S_PROCESSING2;
              break;
            }
            else {
              iss.state = ExecState.S_INPUT_ELEM_CONSUMED;
            }
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
            if (iss.inputElement.getKind() == QueueElement.Kind.E_MINUS)
            {
              if (istream2) {
                /* add this to the list to properly maintain R(t-1) input
                 * synopsis.
                 */
                QueueElement temp = mut_state.allocQueueElement();
                temp.copy(iss.inputElement);
                nowList.add(temp);
              }
              iss.state = ExecState.S_INPUT_ELEM_CONSUMED;
              break;
            }
            else if(iss.inputElement.getKind() == QueueElement.Kind.E_PLUS)
            {
              if (istream2) {
                /* 
                 * for ISTREAM (not in) we can output this tuple only if 
                 * it does not exist in R(t-1) synopsis, where the match key
                 * is a subset of columns.
                 */
                TupleIterator tupIter;
                ITuplePtr inSynTuple;
                ITuplePtr inTuple; 

                inTuple = iss.inputElement.getTuple();
                evalContext.bind(inTuple, IEvalContext.INPUT_ROLE);
                  
                /* scan the synopsis */
                tupIter = inSyn.getScan(inScanId);
                inSynTuple = tupIter.getNext();  

                /* release the scan, as the synopsis will be updated later */
                inSyn.releaseScan(inScanId, tupIter);

                QueueElement temp = mut_state.allocQueueElement();
                temp.copy(iss.inputElement);
                nowList.add(temp); 

                if (inSynTuple != null) {
                  /* found a match, so can't output this tuple. but add it to
                   * the list that captures tuples at time t.
                   */
                  iss.state = ExecState.S_INPUT_ELEM_CONSUMED;
                  break;
                }

              }
              iss.state = ExecState.S_PROCESSING8;
            }
            else
            {
              /* the heartbeat needs to be propagated */
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
  
  /**
   * Create snapshot of IStream operator by writing the IStream operator state.
   */
  @Override
  protected void createSnapshot(ObjectOutputStream output) throws CEPException
  {
    try
    {
      super.createSnapshot(output);
      if(notInSem){
        inSyn.writeExternal(output,new SynopsisPersistenceContext(fullInScanId));
        output.writeInt(nowList.size());
        if(nowList.size() > 0)
        {
          for (QueueElement queueElement : nowList)
          {
            queueElement.writeExternal(output);
            
          }
        }
      }
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
      super.loadSnapshot(input);
      //read nowList for not-in semantics
      if(notInSem)
      {
        IPersistenceContext inSynpersistenceContext = new SynopsisPersistenceContext();
        inSynpersistenceContext.setCache(new HashSet());
        inSyn.readExternal(input,inSynpersistenceContext);
        int nowListSize = input.readInt();
        if(nowListSize > 0){
          nowList = new LinkedList<QueueElement>();
          for(int i=0; i<nowListSize; i++)
          {
            QueueElement qe = mut_state.allocQueueElement();
            qe.readExternal(input);
            nowList.add(qe);
          }
      }
      }
    } catch (ClassNotFoundException | IOException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE,e);
      throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR, e, e.getLocalizedMessage(), getOptName());
    }
  }
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<IStreamOp>");
    sb.append("<InputQueue>" + inputQueue.toString() + "</InputQueue>");
    sb.append("<OutputQueue>" + outputQueue.toString() + "</OutputQueue>");
    sb.append("</IStreamOp>");
    return sb.toString();
  }
}
