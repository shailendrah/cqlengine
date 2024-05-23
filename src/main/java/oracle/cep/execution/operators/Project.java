/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/Project.java /main/41 2013/12/18 06:54:32 sbishnoi Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares Project in package oracle.cep.execution.operators.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 udeshmuk  08/14/13 - set snapshotid as current snapshotid for archiver records
 udeshmuk  07/10/13 - fix logging related to archived relation framework
 udeshmuk  05/22/13 - bug 16820093 : set snapshotid for hb
 udeshmuk  09/09/12 - propagate snapshotid and handle event id
 sbishnoi  08/19/12 - bug 14502856
 udeshmuk  05/27/12 - propagate snapshotId and archived flag
 udeshmuk  02/10/12 - send heartbeat after snapshot
 udeshmuk  09/15/11 - remove prints
 udeshmuk  09/05/11 - change run method to work properly when synopsis is not
                      present
 udeshmuk  06/29/11 - support for archived relation
 anasrini  12/19/10 - replace eval() with eval(ec)
 udeshmuk  01/21/09 - total ts ordering optimization for systs cases
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 parujain  06/06/08 - invalid xmltype
 hopark    02/28/08 - resurrect refcnt
 anasrini  02/08/08 - 
 hopark    12/07/07 - cleanup spill
 parujain  12/17/07 - db-join
 hopark    10/30/07 - remove IQueueElement
 hopark    10/21/07 - remove TimeStamp
 parujain  10/04/07 - delete op
 hopark    09/07/07 - eval refactor
 najain    09/05/07 - bug fix
 hopark    07/13/07 - dump stack trace on exception
 parujain  07/03/07 - cleanup
 parujain  06/26/07 - mutable state
 hopark    05/24/07 - debug logging
 hopark    05/16/07 - add arguments of OutOfOrderException
 parujain  05/08/07 - monitoring statistics
 hopark    03/24/07 - add unpin
 najain    03/14/07 - cleanup
 najain    03/12/07 - bug fix
 najain    02/19/07 - bug fix
 najain    01/05/07 - spill over support
 parujain  12/13/06 - propagation of relations
 najain    08/02/06 - refCounting optimizations
 najain    08/10/06 - add asserts
 najain    07/19/06 - ref-count tuples 
 najain    07/13/06 - ref-count timestamps 
 najain    07/10/06 - move inStore to parent 
 najain    05/24/06 - bug fix 
 najain    05/23/06 - bug fix 
 najain    05/05/06 - dump
 ayalaman  04/26/06 - project with no Lineage synopsis 
 skaluska  04/04/06 - increment ref counts on new elements 
 anasrini  03/24/06 - add toString 
 skaluska  03/20/06 - implementation
 skaluska  03/14/06 - query manager 
 anasrini  03/16/06 - queue related APIs 
 anasrini  03/14/06 - make constructor public, add setter methods 
 skaluska  02/06/06 - Creation
 skaluska  02/06/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/Project.java /main/41 2013/12/18 06:54:32 sbishnoi Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.logging.Level;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.execution.snapshot.SynopsisPersistenceContext;
import oracle.cep.execution.synopses.ExecSynopsis;
import oracle.cep.execution.synopses.LineageSynopsis;
import oracle.cep.execution.queues.ISharedQueueWriter;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.service.ExecContext;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;

/**
 * Project
 *
 * @author skaluska
 */
public class Project extends ExecOpt
{
  /** evaluation context */
  private IEvalContext      evalContext;

  /** roles for evaluation context */
  protected static final int INPUT_ROLE  = IEvalContext.INPUT_ROLE;

  protected static final int OUTPUT_ROLE = IEvalContext.NEW_OUTPUT_ROLE;

  /** Arithmetic evaluator that computes the output tuple */
  private IAEval            projEvaluator;

  /** Synopsis storing the output of the project */
  private LineageSynopsis  outSynopsis;

  private boolean oldDataPropNeeded = true;
  
  /** A flag to decide whether we need to propagate the heartbeat */
  private boolean requiresHbtPropagation = false;

  /**
   * Constructor for Project
   * @param ec TODO
   */
  public Project(ExecContext ec)
  {
    super(ExecOptType.EXEC_PROJECT, new ProjectState(ec), ec);
  }

  /**
   * Getter for evalContext in Project
   * 
   * @return Returns the evalContext
   */
  public IEvalContext getEvalContext()
  {
    return evalContext;
  }

  /**
   * Setter for evalContext in Project
   * 
   * @param evalContext
   *          The evalContext to set.
   */
  public void setEvalContext(IEvalContext evalContext)
  {
    this.evalContext = evalContext;
  }

  /**
   * Getter for outSynopsis in Project
   * 
   * @return Returns the outSynopsis
   */
  public LineageSynopsis getOutSynopsis()
  {
    return outSynopsis;
  }

  /**
   * Setter for outSynopsis in Project
   * 
   * @param outSynopsis
   *          The outSynopsis to set.
   */
  public void setOutSynopsis(LineageSynopsis outSynopsis)
  {
    this.outSynopsis = outSynopsis;
  }

  /**
   * Getter for projEval in Project
   * 
   * @return Returns the projEval
   */
  public IAEval getProjEvaluator()
  {
    return projEvaluator;
  }

  /**
   * Setter for projEval in Project
   * 
   * @param projEval
   *          The projEval to set.
   */
  public void setProjEvaluator(IAEval projEval)
  {
    this.projEvaluator = projEval;
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
    boolean done = false;
    ProjectState s = (ProjectState) mut_state;
    boolean exitState = true;

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
          case S_PROPAGATE_OLD_DATA:
            if(outSynopsis != null)
            {
              if(oldDataPropNeeded )
              {
                setExecSynopsis((ExecSynopsis) outSynopsis);
                handlePropOldData();
              }
              else
              {
                oldDataPropNeeded = true;
                s.state = s.lastState;
              }
            }
            else
              s.state = s.lastState;
            break;

          case S_INIT:
            // Get next input element
            s.inputElement = inputQueue.dequeue(s.inputElementBuf);
            s.state = ExecState.S_INPUT_DEQUEUED;
          case S_INPUT_DEQUEUED:
            // Determine the next step based on element kind
            if (s.inputElement == null)
            {
              if (s.lastInputTs <= s.lastOutputTs && !requiresHbtPropagation)
              {
                s.state = ExecState.S_INIT;
                done = true;
                break;
              }

              // Output a heartbeat
              s.outputTuple = null;
              s.outputKind = QueueElement.Kind.E_HEARTBEAT;
              s.state = ExecState.S_ALLOCATE_ELEM;
              // Reset the flag
              requiresHbtPropagation = false;
              break;
            }
            else
            {
              // Update our counts
              if(s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
                s.stats.incrNumInputHeartbeats();
              else
                s.stats.incrNumInputs();

              s.inputTuple = s.inputElement.getTuple();
              
              // Update last input ts
              s.inputTs = s.inputElement.getTs();

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
                this.getDebugInfo(s.inputTs, s.minNextInputTs,
                    s.inputElement.getKind().name(), s.lastInputKind.name());

              // Update the last input Ts now
              s.lastInputTs   = s.inputTs;
              s.lastInputKind = s.inputElement.getKind();
              s.prevInputSnapshotId = s.inputElement.getSnapshotId();
              
              s.lastInputOrderingFlag 
                = s.inputElement.getTotalOrderingGuarantee();
              
              // calculate the expected timeStamp of next input tuple
              s.minNextInputTs 
                = s.lastInputOrderingFlag ? s.inputTs + 1 : s.inputTs;
              
              exitState = false;

              // Nothing more to be done for heartbeats
              if (s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
              {
                // Need to propagate the heartbeat if the heartbeat is carrying
                // total ordering information
                requiresHbtPropagation = s.lastInputOrderingFlag;
                s.state = ExecState.S_INPUT_ELEM_CONSUMED;
                break;
              }
              else
              {
                s.outputKind = s.inputElement.getKind();
                if (s.inputElement.getKind() == QueueElement.Kind.E_PLUS)
                  s.state = ExecState.S_PROCESSING1;
                else if (s.inputElement.getKind() == QueueElement.Kind.E_MINUS)
                {
                  if(outSynopsis != null)
                    s.state = ExecState.S_PROCESSING4;
                  else 
                    //if synopsis is not present then MINUS is handled in the 
                    //same manner as PLUS
                    s.state = ExecState.S_PROCESSING1;
                  break;
                }
              }
            }
          case S_PROCESSING1:
            // Allocate an output tuple
            s.outputTuple = tupleStorageAlloc.allocate();
            if((outSynopsis == null) && (!this.isStream))
            { //copy over the id from input tuple to output tuple
              ITuple tup = s.outputTuple.pinTuple(IPinnable.WRITE);
              tup.setId(s.inputTuple.getId());
              s.outputTuple.unpinTuple();
            }
            s.state = ExecState.S_PROCESSING2;
          case S_PROCESSING2:
            // Compute the output tuple
            evalContext.bind(s.inputTuple, INPUT_ROLE);
            evalContext.bind(s.outputTuple, OUTPUT_ROLE);
            try {
              projEvaluator.eval(evalContext);
            }catch(SoftExecException se)
            {
              inTupleStorageAlloc.release(s.inputTuple);
              tupleStorageAlloc.release(s.outputTuple);
              throw se;
            }
            // outSynopsis is null if the operator is a stream 
            // or if the operator is a query operator or is in
            // the path from source to query operator.
            if (outSynopsis != null)
              s.state = ExecState.S_PROCESSING3;
            else
            {
              s.state = ExecState.S_ALLOCATE_ELEM;
              break;
            }
          case S_PROCESSING3:
            // Add output tuple to outer synopsis
            s.tupleLineage[0] = s.inputTuple;
            outSynopsis.insertTuple(s.outputTuple, s.tupleLineage);
            s.state = ExecState.S_ALLOCATE_ELEM;
            break;
          case S_PROCESSING4:
            assert s.outputKind == QueueElement.Kind.E_MINUS : s.outputKind;
            assert outSynopsis != null :"Synopsis is null, Can't handle MINUS";
            // Get the previously output tuple for this input
            s.tupleLineage[0] = s.inputTuple;

            // Currently we are assuming that scan will always
            // have a single tuple.
            TupleIterator projScan = outSynopsis.getScan_l(s.tupleLineage);
            s.outputTuple = projScan.getNext();
          
            // There should be only one tuple with this lineage
            assert projScan.getNext() == null;
            outSynopsis.releaseScan_l(projScan);

            if(s.outputTuple != null)
            { // Delete the output tuple from the synopsis
              outSynopsis.deleteTuple(s.outputTuple);
              s.state = ExecState.S_ALLOCATE_ELEM;
            }
            else
            { // If eval resulted in an exception then no 
              // tuple will be get inserted in the synopsis
              s.state = ExecState.S_PROCESSING5;
              break;
            }
            
          case S_ALLOCATE_ELEM:
            s.state = ExecState.S_OUTPUT_READY;
          case S_OUTPUT_READY:
            if (s.outputKind == QueueElement.Kind.E_HEARTBEAT)
            {
              assert s.inputElement == null;
              s.lastOutputTs = s.lastInputTs;
              s.outputElement.setTotalOrderingGuarantee(
                s.lastOutputTs < s.minNextInputTs);
              s.outputElement.heartBeat(s.lastInputTs);
            }
            else
            {
              s.outputElement.setTuple(s.outputTuple);
              s.outputElement.setTs(s.inputTs);
              s.outputElement.setKind(s.inputElement.getKind());
              // Update last output ts
              s.lastOutputTs = s.inputTs;
              s.outputElement.setTotalOrderingGuarantee(
                  s.lastOutputTs < s.minNextInputTs);
            }

            s.state = ExecState.S_OUTPUT_ELEMENT;
          case S_OUTPUT_ELEMENT:
            if (outputQueue.isFull())
            {
              done = true;
              break;
            }
            
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
            
            //snapshot id propagation
            if(s.inputElement != null)
              s.outputElement.setSnapshotId(s.inputElement.getSnapshotId());  
            else
            {
              //bug 16820093: 
              /*
               * HB is sent when prev input didn't result in output (not sure
               * if this can ever happen)
               *  OR
               * when prev input was HB and so nothing was done for it.
               * In the next iteration, no new tuple has come (inputElem=null)
               * so sending HB now. So the snapshotId should be prev
               * input's snapshotId.
               */
              s.outputElement.setSnapshotId(s.prevInputSnapshotId);
            }
            
            outputQueue.enqueue(s.outputElement);
            s.state = ExecState.S_OUTPUT_ENQUEUED;
          case S_OUTPUT_ENQUEUED:
            if(s.outputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
              s.stats.incrNumOutputHeartbeats();
            else
              s.stats.incrNumOutputs();
            s.state = ExecState.S_PROCESSING5;
          case S_PROCESSING5:
            if (s.inputElement != null)
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            else
            {
              s.state = ExecState.S_INIT;
              break;
            }
          case S_INPUT_ELEM_CONSUMED:
            assert s.inputElement != null;

            if (s.inputTuple != null)
            {
              inTupleStorageAlloc.release(s.inputTuple);
            }

            exitState = true;
            s.state = ExecState.S_INIT;
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
      s.state = ExecState.S_INIT;
      exitState = true;
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

  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<Project id=\"" + id + "\" >");
    sb.append("<InputQueue>" + inputQueue.toString() + "</InputQueue>");
    sb.append("<OutputQueue>" + outputQueue.toString() + "</OutputQueue>");
    sb.append("<Synopsis>");
    if (outSynopsis != null)
      sb.append(outSynopsis.toString());
    sb.append("</Synopsis>");
    sb.append("<OutTupleAlloc>" + tupleStorageAlloc.toString()
        + "</OutTupleAlloc>");
    sb.append("<ProjExpr>");
    sb.append(projEvaluator.toString());
    // sb.append("<Scratch>" + evalContext.st.toString() + "</Scratch>");
    // sb.append("<Constant>" + evalContext.ct.getTupleSpec().toString() +
    // "</Constant>");
    sb.append("</ProjExpr>");
    sb.append("</Project>");

    return sb.toString();
  }
  
  public void initializeState() throws CEPException
  {
    if(archivedRelationTuples != null)
    {
      ProjectState s = (ProjectState) mut_state;
      for(ITuplePtr currentTuple : archivedRelationTuples)
      {
        ITuple r = currentTuple.pinTuple(IPinnable.READ);
        //use the tuple itself as its lineage
        if(outSynopsis != null)
          outSynopsis.insertTuple(currentTuple,new ITuplePtr[]{currentTuple});
    
        s.inputTs = snapShotTime;
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
      s.lastOutputTs=snapShotTime + 1;
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
      //only if 
      //1. There are archived relation tuples which have been enqueued
      // AND
      //2. Propagation (of old data) is indicated asrequired at this point
      //Set the oldDataPropNeeded to false to avoid duplicate output.
      if((archivedRelationTuples.size() > 0) && (this.propagationReqd()))
        oldDataPropNeeded = false;
      
      //remove all the archived relation tuples.
      archivedRelationTuples.clear();
    }
  }
  
  /**
   * Create snapshot of Project operator by writing the operator state
   * into param java output stream.
   * State of Group Aggregate operator consists of following:
   * 1. Mutable State
   * 2. Output Synopsis
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
      output.writeObject((ProjectState)mut_state);

      // Write output synopsis to output stream
      if (outSynopsis == null)
      {
    	  output.writeBoolean(true);
      }
      else
      {
    	  output.writeBoolean(false);
    	  outSynopsis.writeExternal(output);
      }
    } 
    catch (IOException e)
    {
      LogUtil.logStackTrace(e); 	
      throw new CEPException(ExecutionError.SNAPSHOT_CREATION_ERROR, e, e.getLocalizedMessage());
    }
  }
  
  protected void loadSnapshot(ObjectInputStream input) throws ExecException
  {
    try
    {
      // Read MutableState from input stream
      ProjectState loaded_mutable_state = (ProjectState) input.readObject();
      ((ProjectState)mut_state).copyFrom(loaded_mutable_state);
      
      // Read output synopsis from input stream
      boolean isOutSynopsisNull = input.readBoolean();
      
      if (!isOutSynopsisNull)
    	  outSynopsis.readExternal(input);     
    } 
    catch (ClassNotFoundException e)
    {
      LogUtil.logStackTrace(e); 	
      throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR_CNF, e, e.getLocalizedMessage());
    } 
    catch (IOException e)
    {
      LogUtil.logStackTrace(e);	
      throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR,e,e.getLocalizedMessage());
    }
  }
}
