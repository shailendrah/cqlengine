/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/Distinct.java /main/30 2013/10/08 10:15:01 udeshmuk Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares Distinct in package oracle.cep.execution.operators.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 udeshmuk  08/14/13 - set snapshotid as current snapshotid for archiver records
 udeshmuk  07/10/13 - fix logging related to archived relation framework
 udeshmuk  09/09/12 - propagate snapshotid and handle event id
 sbishnoi  08/19/12 - bug 14502856
 udeshmuk  07/03/12 - fix heartbeat ordering flag getting overwritten in
                      S_ALLOCATE_ELEM
 udeshmuk  05/27/12 - snapshotid propagation and archived flag
 udeshmuk  05/26/12 - getTupleSpec
 udeshmuk  02/10/12 - send heartbeat after snapshot
 udeshmuk  06/26/11 - support for archived relation
 anasrini  12/19/10 - replace eval() with eval(ec)
 sborah    04/13/09 - assertion check
 sbishnoi  04/05/09 - piggyback optimization
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 hopark    10/07/08 - use execContext to remove statics
 sborah    09/25/08 - update stats.
 hopark    02/28/08 - resurrect refcnt
 hopark    12/07/07 - cleanup spill
 hopark    10/30/07 - remove IQueueElement
 hopark    10/21/07 - remove TimeStamp
 parujain  10/04/07 - delete op
 hopark    09/07/07 - eval refactor
 sbishnoi  07/20/07 - fix perform bug; introduced propagation old data
 hopark    07/13/07 - dump stack trace on exception
 parujain  06/26/07 - mutable state
 hopark    05/24/07 - debug logging
 parujain  05/16/07 - Statistics
 sbishnoi  05/13/07 - re-implementation
 najain    03/12/07 - bug fix
 skaluska  03/14/06 - query manager 
 skaluska  02/06/06 - Creation
 skaluska  02/06/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/Distinct.java /main/30 2013/10/08 10:15:01 udeshmuk Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;

import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.queues.ISharedQueueWriter;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.execution.snapshot.SynopsisPersistenceContext;
import oracle.cep.execution.synopses.ExecSynopsis;
import oracle.cep.execution.synopses.RelationSynopsis;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.service.ExecContext;

/**
 * Distinct is the execution time operator to compute distinct.
 * 
 * @author skaluska
 */
public class Distinct extends ExecOpt
{
  /** Synopsis for the output */
  RelationSynopsis relSyn;

  /** Relation Scan identifier */
  private int      relScanId;
  
  /** Full Scan Identifier for relSyn */
  private int      fullScanId;


  /* does the input operator only depend on silent Relations */
  boolean          isSilentRelns;
  

  // The list of silent Relations that the operator depends on: This is needed
  // to propagate the heartbeat in case of a stall or a silent relation.
  // Currently, silent streams/relations are not handled, only static relations
  // (one for which the time is not specifed) and handled appropriately.
  LinkedList<RelSource>      inputRelns;

  /** input store */
  private IAllocator<ITuplePtr> inputStore;

  
  /** Evaluation context in which all the action takes place */
  IEvalContext                evalContext;

  /** Evaluator to construct output tuples from input */
  IAEval                      outEval;
  
  IAEval                      initEval;
  
  IAEval                      incrEval;
  
  IAEval                      decrEval;
  
  IBEval                      oneEval;
  
  TupleSpec                   tupSpec;

  private boolean oldDataPropNeeded = true;
  
  
  /**
   * Constructor for Distinct
   * @param ec TODO
   */
  public Distinct(ExecContext ec)
  {
    super(ExecOptType.EXEC_DISTINCT, new DistinctState(ec), ec);
   
  }
  
  /**
   * Sets relSyn
   * @param syn
   */
  public void setRelSyn(RelationSynopsis syn)
  {
  this.relSyn = syn;
  }
  
  /**
   * @param relScanId
   *          The relScanId to set.
   */
  public void setRelScanId(int relScanId)
  {
    this.relScanId = relScanId;
  }
  
  /**Set fullScanId
   * @param fullScanID
   *        The fullScanID is set.
   */
  public void setFullScanID(int fullScanId)
  {
    this.fullScanId = fullScanId;
    this.propScanId = fullScanId;
  }
  
  /**
   * Get fullScanID
   * @return fullScanId
   */
  public int getFullScanId()
  {
    return this.fullScanId;
  }
  
  /**
   * @return tuple spec
   */
  public TupleSpec getTupleSpec()
  {
    return tupSpec;
  }
  
  /**
   * setter for the tuple spec
   * @param ts
   */
  public void setTupleSpec(TupleSpec ts)
  {
    tupSpec = ts;
  }
  
  /**
   * Sets Input Store
   * 
   * @param inputStore
   *          Sets inputStore.
   */
  public void setInputStore(IAllocator<ITuplePtr> inputStore)
  {
    this.inputStore = inputStore;
  }
  
  /**
   * @param evalContext
   *          Sets evalContext
   */
  public void setEvalContext(IEvalContext ctx)
  {
    this.evalContext = ctx;
  }

  /**
   * @param outEval
   *          Sets outEval
   */
  public void setOutEval(IAEval outEval)
  {
    this.outEval = outEval;
  }

  /**
   * @param initEval
   *          Sets initEval
   */
  public void setInitEval(IAEval initEval)
  {
    this.initEval = initEval;
  }
  
  public void setIncrEval(IAEval incrEval)
  {
  this.incrEval = incrEval; 
  }
  
  public void setDecrEval(IAEval decrEval)
  {
  this.decrEval = decrEval;
  }
  
  public void setOneEval(IBEval oneEval)
  {
  this.oneEval = oneEval;
  }
  
  /**
   * Setter for silentRelns
   * 
   * @param silentRelns
   *          The silentRelns to set.
   */
  public void setSilentRelns(boolean isSilentRelns)
  {
    this.isSilentRelns = isSilentRelns;
  }

  public void addInputRelns(RelSource execOp)
  {
    if (inputRelns == null)
      inputRelns = new LinkedList<RelSource>();

    inputRelns.add(execOp);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.operators.ExecOpt#run(int)
   */
  @Override
  public int run(int timeslice) throws ExecException
  {
    int numElements = timeslice;
    DistinctState s = (DistinctState) mut_state;
    boolean exitState = true;

    assert s.state != ExecState.S_UNINIT;
    
    boolean done = false;
    
    //  Stats
    s.stats.incrNumExecutions();

    try
    {
      while ((s.stats.getNumInputs() < numElements) || (!exitState))
      {
        switch (s.state)
        {
          case S_PROPAGATE_OLD_DATA:
            if(oldDataPropNeeded)
            {
              setExecSynopsis((ExecSynopsis) relSyn);
              handlePropOldData();
            }
            else
            {
              oldDataPropNeeded = true;
              s.state = s.lastState;
            }
            break;
          case S_INIT:
          {
            QueueElement inputPeekElement;

            // Peek to revise min input timestamp estimate
            inputPeekElement = inputQueue.peek(s.inputElementBuf);
            if (inputPeekElement != null)
            {
              s.inputMinTs = inputPeekElement.getTs();
            }
            else
              // Minimum timestamp possible on the next input element
              s.inputMinTs =s.minNextInputTs;

            s.inputElement = inputQueue.dequeue(s.inputElementBuf);
	    s.state = ExecState.S_OUTER_INPUT_DEQUEUED;
	   
            break;
          }          

          case S_PROCESSING1: 
          // scan the synopsis for required outputTuple
          
            s.countScan = relSyn.getScan(relScanId);
            assert s.countScan != null;

            s.outputTuple = s.countScan.getNext();
            
            relSyn.releaseScan(relScanId, s.countScan);
            s.state = s.tmpState;    // switch either to PROCESSING2 (Plus Tuple) or to PROCESSING3 (Minus Tuple)
            break;
            
          case S_PROCESSING2: 
            // handle PLUS tuples
          // The count tuple does not exist
            if (s.outputTuple == null)
            {
              s.state = ExecState.S_OUTPUT_TUPLE;
              s.tmpState = ExecState.S_PROCESSING5;
            }
            else
            {               
              evalContext.bind(s.outputTuple, IEvalContext.SYN_ROLE);
              incrEval.eval(evalContext);
              tupleStorageAlloc.release(s.outputTuple);
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            }
            break;

          case S_PROCESSING3:
          //Handle MINUS Tuples
            assert s.outputTuple != null;
            evalContext.bind(s.outputTuple, IEvalContext.SYN_ROLE);
            if(oneEval.eval(evalContext))
            { 
              relSyn.deleteTuple(s.outputTuple);
              s.state = ExecState.S_PROCESSING6;
            }
            else
            { 
              decrEval.eval(evalContext);
              tupleStorageAlloc.release(s.outputTuple);
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            }
            break;

            
          case S_PROCESSING5:
            //Initialize and insert tuple in Synopsis
            
            evalContext.bind(s.outputTuple, IEvalContext.SYN_ROLE);
            initEval.eval(evalContext);
            relSyn.insertTuple(s.outputTuple);
            s.state = ExecState.S_PROCESSING6;
              

          case S_PROCESSING6:
            evalContext.bind(s.outputTuple, IEvalContext.NEW_OUTPUT_ROLE);
            outEval.eval(evalContext);
            s.state = ExecState.S_ALLOCATE_ELEM;
            // set the output element ordering flag to the input ordering flag
            // as either we will send an output on each input(this case) or
            //           we will ignore the input(no output)
            s.outputElement.setTotalOrderingGuarantee(s.isTotalOrderGuarantee);
            break;

          case S_OUTER_INPUT_DEQUEUED:
            if (s.inputElement == null)
            {
              // Handle silent relations - as if you recieved a heartbeat
              if (enableSilentRelnProcessing
                  && isSilentRelns
                  && (execMgr.getMaxSourceTime(
                      inputRelns) <= s.inputMinTs))
              {
                // increment the time to the current time
                long max = execMgr.getMaxSourceTime() + 1;
                s.lastInputTs = max;
                s.state = ExecState.S_INIT;
              }
              else
              {
                // Action: Setting HeartBeat to FALSE
                // Conditions: HeartBeat can be transmitted only in following:
                // 
                // If Input element arrived but no action taken in terms of
                //    sending the output
                //    then in the very next iteration, we will transmit a hBt;
                //    Ordering Flag of this heartBeat should be FALSE
                
                // Reason: 
                // If input element has ordering flag TRUE; then inputMinTs has
                // been incremented by 1; So we cann't send this HBt with TRUE
                // If input element has ordering flag FALSE; then inputMinTs is
                // same; Here also we cann't send HBt with TRUE because we can 
                // receive future input with the current timeStamp value.
                // That is why flag is always FALSE
                
                s.outputElement.setTotalOrderingGuarantee(false);
                // we might still need to output a heartbeat
                s.state = ExecState.S_GENERATE_HEARTBEAT;
              }
            }
            else
            {
              exitState = false;
              if(s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
                s.stats.incrNumInputHeartbeats();
              else
                s.stats.incrNumInputs();

              // update left ts
              s.inputTs = s.inputElement.getTs();
              // get the total ordering flag from input element
              s.isTotalOrderGuarantee 
                = s.inputElement.getTotalOrderingGuarantee();

              // We should have a progress of time.
              if (s.lastInputTs > s.inputTs)
              {
                throw ExecException.OutOfOrderException(
                        this,
                        s.lastInputTs, 
                        s.inputTs, 
                        s.inputElement.toString());
              }
              
              // current input timeStamp should be greater than the calculated
              // expected timeStamp in previous execution
              assert s.inputTs >= s.minNextInputTs : 
                    getDebugInfo(s.inputTs, s.minNextInputTs,
                    s.inputElement.getKind().name(), s.lastInputKind.name());
             
              s.minNextInputTs 
                = s.isTotalOrderGuarantee ? s.inputTs + 1 : s.inputTs;
              
              // Update the last input Ts now
              s.lastInputTs = s.inputTs;
              s.lastInputKind = s.inputElement.getKind();

              s.nextOutputTs = s.inputElement.getTs();
              s.inputTuple = s.inputElement.getTuple();

              s.nextElementKind = s.inputElement.getKind();
              
              if (s.nextElementKind == QueueElement.Kind.E_PLUS)
              {
                assert s.inputTuple != null;
                evalContext.bind(s.inputTuple, IEvalContext.INPUT_ROLE);

                // allocate the output tuple, then back to processing left plus
                s.tmpState = ExecState.S_PROCESSING2;
                s.state = ExecState.S_PROCESSING1;
                
                break;
              }
              
              else if (s.nextElementKind == QueueElement.Kind.E_MINUS)
              {
                assert s.inputTuple != null;
                
                evalContext.bind(s.inputTuple, IEvalContext.INPUT_ROLE);
                s.tmpState = ExecState.S_PROCESSING3;
                s.state = ExecState.S_PROCESSING1;
                break;
              }
              // nothing to be done for heartbeats
              else
                s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            }
            break;

 
          case S_OUTPUT_TUPLE:
            s.outputTuple = tupleStorageAlloc.allocate();
            evalContext.bind(s.outputTuple, IEvalContext.NEW_OUTPUT_ROLE);
            s.state = s.tmpState;
            break;

          case S_ALLOCATE_ELEM:
            s.outputElement.setKind(s.nextElementKind);            
            s.outputElement.setTotalOrderingGuarantee(s.isTotalOrderGuarantee);            
            s.state = ExecState.S_OUTPUT_TIMESTAMP;
            
          case S_OUTPUT_TIMESTAMP:
            s.outputElement.setTs(s.nextOutputTs);
            s.outputElement.setTuple(s.outputTuple);
            s.state = ExecState.S_OUTPUT_READY;
            
          case S_OUTPUT_READY:
            if (outputQueue.isFull())
            {
              done = true;
              break;
            }

            s.lastOutputTs = s.nextOutputTs;
            if (s.outputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
            {
              //for heartbeat the snapshot id value would be default: Long.MAX_VALUE
              outputQueue.enqueue(s.outputElement);
              s.stats.incrNumOutputHeartbeats();
              s.state = ExecState.S_INIT;
              break;
            }
            else
            {
              //set event id val as tuple id if needed
              if(this.shouldUseEventIdVal())
              {
                assert eventIdColNum != -1 : "eventIdColNum not set in "
                                             +this.getOptName();
                ITuplePtr outTuplePtr = s.outputElement.getTuple();
                if(outTuplePtr != null)
                {
                  ITuple outTuple = outTuplePtr.pinTuple(IPinnable.WRITE);             
                  //use event identifier col value as tuple id if needed
                  outTuple.setId(outTuple.lValueGet(eventIdColNum));
                  outTuplePtr.unpinTuple();
                }
              }
              
              //propagation of snapshot id 
              if(s.inputElement != null)
                s.outputElement.setSnapshotId(s.inputElement.getSnapshotId());
              
              outputQueue.enqueue(s.outputElement);
              s.stats.incrNumOutputs();
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            }


          case S_INPUT_ELEM_CONSUMED:
          {
            QueueElement elem;

            assert (s.inputElement != null);
            
            elem = s.inputElement;

            assert elem != null;

            if (s.inputTuple != null)
            {
              inputStore.release(s.inputTuple);
            }
             
            
            elem = null;
            s.inputElement = null;
            
          exitState = true;
         
            s.state = ExecState.S_INIT;
            break;
          }

          case S_GENERATE_HEARTBEAT:
            assert (s.inputElement == null);
	    //heartbeat should be sent only when there has been no output
	    //for a received input tuple
            if (s.lastOutputTs < s.inputTs)
            {
              s.nextOutputTs = s.inputTs;
              s.outputTuple = null;
              s.nextElementKind = QueueElement.Kind.E_HEARTBEAT;
	      s.outputElement.setTuple(s.outputTuple);
	      s.outputElement.setTs(s.nextOutputTs);
	      s.outputElement.setKind(s.nextElementKind);
	      //total ordering guarantee is set to false in 
	      //state S_OUTER_INPUT_DEQUEUED
              s.state = ExecState.S_OUTPUT_READY;
            }
            else
            {
              s.state = ExecState.S_INIT;
              done = true;
            }
            break;

          default:
            assert false;
        }
        if (done)
          break;
      }
    }
    catch (SoftExecException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      return 0;
    }
    return 0;
  }

  /* (non-Javadoc)
   * @see oracle.cep.execution.operators.ExecOpt#deleteOp()
   */
  @Override
  public void deleteOp()
  {
    // TODO Auto-generated method stub
    
  }
  
  public void initializeState() throws CEPException
  {
    if(archivedRelationTuples != null)
    {
      DistinctState s = (DistinctState)mut_state;
      for(ITuplePtr currentTuple : archivedRelationTuples)
      {
        ITuple r = currentTuple.pinTuple(IPinnable.READ);
        
        // Insert into output synopsis
        relSyn.insertTuple(currentTuple);
                 
        // Insert into output queue
        s.inputTs = snapShotTime;
        s.lastInputTs = s.inputTs;
        s.outputElement.setTuple(currentTuple);
        s.outputElement.setTs(s.inputTs);    
        s.outputElement.setKind(QueueElement.Kind.E_PLUS);
        s.outputElement.setTotalOrderingGuarantee(false);
        s.outputElement.setSnapshotId(execContext.getPlanMgr().getCurrentSnapshotId());
        s.lastOutputTs = s.inputTs;
         
        ((ISharedQueueWriter)outputQueue).enqueue(s.outputElement, 
                                                  this.getArchiverReaders());   
        /*
        LogUtil.finest(LoggerType.TRACE, "ARF# "+
                     this.getOptName()+
                     " propagated archiver tuple: "+currentTuple);
        */
        s.stats.incrNumOutputs();
        currentTuple.unpinTuple();   
      }

      //send heartbeat with ordering guarantee false
      s.lastOutputTs=snapShotTime+1;
      s.outputElement.heartBeat(s.lastOutputTs);
      s.outputElement.setTotalOrderingGuarantee(false);
      s.outputElement.setKind(QueueElement.Kind.E_HEARTBEAT);
      s.outputElement.setSnapshotId(execContext.getPlanMgr().getCurrentSnapshotId());
      ((ISharedQueueWriter) outputQueue).enqueue(s.outputElement, 
          this.getArchiverReaders());
      LogUtil.finer(LoggerType.TRACE, "ARF# "+
                    this.getOptName()+
                    " sent heartbeat of "+ s.lastOutputTs + " with ordering guarantee false");
      s.stats.incrNumOutputs();
      
      LogUtil.finer(LoggerType.TRACE, "ARF# "+
                   "Initialized state of "+this.getOptName()+
                   " and propagated events received from archiver downstream");
      
      if((archivedRelationTuples.size() > 0) && (this.propagationReqd()))
        oldDataPropNeeded  = false;
      
      //remove all the archived relation tuples.
      archivedRelationTuples.clear();
    }
  }

  /**
   * Create snapshot of Distinct operator by writing the operator state
   * into param java output stream.
   * State of Distinct operator consists of following:
   * 1. Mutable State
   * 2. Relation Synopsis
   * 
   * Please note that we will write the state of operator in above sequence, so
   * the loadSnapshot should also read the state in the same sequence.
   * @param output
   * @throws IOException 
   */
  protected void createSnapshot(ObjectOutputStream output) throws CEPException
  {   
    try
    {
      // Write Mutable state to output stream
      output.writeObject((DistinctState)mut_state);

      // Write relation synopsis to output stream
      relSyn.writeExternal(output, new SynopsisPersistenceContext(fullScanId));      
    } 
    catch (IOException e)
    {
      LogUtil.logStackTrace(e);
      throw new CEPException(ExecutionError.SNAPSHOT_CREATION_ERROR, e, e.getLocalizedMessage(), this.getOptName());
    }
  }
  
  protected void loadSnapshot(ObjectInputStream input) throws ExecException
  {
    try
    {
      // Read MutableState from input stream
      DistinctState loaded_mutable_state = (DistinctState) input.readObject();
      ((DistinctState)mut_state).copyFrom(loaded_mutable_state);
      
      IPersistenceContext sharedSynopsisRecoveryCtx = new SynopsisPersistenceContext();
      sharedSynopsisRecoveryCtx.setCache(new HashSet());
      
      // Read relSyn synopsis from input stream
      relSyn.readExternal(input, sharedSynopsisRecoveryCtx);
    } 
    catch (ClassNotFoundException e)
    {
      LogUtil.logStackTrace(e);
      throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR_CNF, e, this.getOptName(), e.getMessage());
    } 
    catch (IOException e)
    {
      LogUtil.logStackTrace(e);
      throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR,e,e.getLocalizedMessage(), getOptName());
    }
  }

}
