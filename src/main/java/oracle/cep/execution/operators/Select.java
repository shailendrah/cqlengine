/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/Select.java /main/43 2013/10/08 10:15:01 udeshmuk Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/
/*
 DESCRIPTION
 Declares Select in package oracle.cep.execution.operators.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 udeshmuk  08/12/13 - fix timestamp OOO in special join - set snapshotid for hb
                      sent in enqueueHeartbeat
 udeshmuk  07/10/13 - fix logging related to archived relation framework
 udeshmuk  06/11/13 - 16923890: send heartbeat if timestamp has progressed or
                      if ordering guarantee is true
 udeshmuk  05/22/13 - bug 16820093 : set snapshotid for hb
 vikshukl  02/18/13 - add enqueueHeartbeat()
 vikshukl  10/04/12 - archived dimension
 udeshmuk  09/09/12 - propagate snapshotid and handle event id
 sbishnoi  08/19/12 - bug 14502856
 udeshmuk  05/27/12 - propagate snapshotId and archived flag
 udeshmuk  02/10/12 - send heartbeat after snapshot
 udeshmuk  09/05/11 - change run method to work properly when synopsis is not
                      present
 sbishnoi  08/25/11 - XbranchMerge sbishnoi_bug-11675469_ps5 from
                      st_pcbpel_11.1.1.4.0
 udeshmuk  06/26/11 - support for archived relation
 anasrini  12/19/10 - replace eval() with eval(ec)
 sborah    07/16/09 - support for bigdecimal
 sborah    04/13/09 - assertion check
 udeshmuk  01/20/09 - total ordering ts opt
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 hopark    02/28/08 - resurrect refcnt
 hopark    12/06/07 - cleanup spill
 hopark    10/30/07 - remove IQueueElement
 hopark    10/21/07 - remove TimeStamp
 parujain  10/04/07 - delete op
 hopark    09/07/07 - eval refactor
 hopark    07/13/07 - dump stack trace on exception
 parujain  07/03/07 - cleanup
 parujain  06/26/07 - mutable state
 skmishra  06/07/07 - 
 hopark    05/24/07 - debug logging
 hopark    05/16/07 - add arguments for OutOfOrderException
 parujain  05/08/07 - monitoring statistics
 hopark    04/08/07 - fix pincount
 hopark    03/24/07 - add TuplePtr pin
 najain    03/14/07 - cleanup
 najain    03/12/07 - bug fix
 parujain  02/20/07 - fix exitState
 najain    01/05/07 - spill over support
 parujain  12/19/06 - fullScanId for RelationSynopsis
 parujain  12/12/06 - propagating relations
 najain    08/11/06 - refCnt optimizations
 parujain  08/08/06 - join test
 najain    08/03/06 - select can be shared
 anasrini  08/03/06 - support outSynopsis
 najain    07/19/06 - ref-count tuples 
 najain    07/13/06 - ref-count timestamps 
 najain    07/10/06 - add inStore 
 najain    05/23/06 - bug fix 
 najain    05/05/06 - dump
 anasrini  04/10/06 - bug fix 
 anasrini  03/31/06 - add toString 
 skaluska  03/14/06 - query manager 
 skaluska  02/06/06 - Creation
 skaluska  02/06/06 - Creation
 */
/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/Select.java /main/43 2013/10/08 10:15:01 udeshmuk Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.logging.Level;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.execution.snapshot.SynopsisPersistenceContext;
import oracle.cep.execution.synopses.ExecSynopsis;
import oracle.cep.execution.synopses.RelationSynopsis;
import oracle.cep.execution.queues.ISharedQueueWriter;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.service.ExecContext;

/**
 * Select is the execution time select operator.
 *
 * @author skaluska
 */
public class Select extends ExecOpt
{
  /** evaluation context */
  private IEvalContext      evalContext;

  /** roles for evaluation contex */
  protected static final int INPUT_CONTEXT = IEvalContext.INPUT_ROLE;

  /** selection predicate */
  private IBEval            predicate;

  /** Synopsis storing the output of the select */
  private RelationSynopsis outSynopsis;

  /** Number of attributes */
  private int              numAttrs;

  /** Scan identifier */
  private int              scanId;

  /** Specification of the attributes in the input relation */
  private TupleSpec        attrSpecs;

  /** Full Scan identifier */
  private int              fullScanId;

  private boolean          oldDataPropNeeded;

  /**
   * Constructor for Select
   * @param ec TODO
   */
  public Select(ExecContext ec, int maxAttrs)
  {
    super(ExecOptType.EXEC_SELECT, new SelectState(ec), ec);

    attrSpecs = new TupleSpec(factoryMgr.getNextId(), maxAttrs);
    numAttrs = 0;
    oldDataPropNeeded = true;
  }

  /**
   * Getter for evalContext in Select
   * 
   * @return Returns the evalContext
   */
  public IEvalContext getEvalContext()
  {
    return evalContext;
  }

  /**
   * Setter for evalContext in Select
   * 
   * @param evalContext
   *          The evalContext to set.
   */
  public void setEvalContext(IEvalContext evalContext)
  {
    this.evalContext = evalContext;
  }

  /**
   * Getter for predicate in Select
   * 
   * @return Returns the predicate
   */
  public IBEval getPredicate()
  {
    return predicate;
  }

  /**
   * Setter for predicate in Select
   * 
   * @param predicate
   *          The predicate to set.
   */
  public void setPredicate(IBEval predicate)
  {
    this.predicate = predicate;
  }

  /**
   * Setter for outSynopsis in Select
   * 
   * @param outSynopsis
   *          The outSynopsis to set.
   */
  public void setOutSynopsis(RelationSynopsis outSynopsis)
  {
    this.outSynopsis = outSynopsis;
  }

  /**
   * Add an attribute at the next position
   * 
   * @param type
   *          Attribute type
   * @param len
   *          Attribute maximum length
   * @throws ExecException
   */
  public void addAttr(AttributeMetadata attrMetadata) throws ExecException
  {
    attrSpecs.addAttr(numAttrs, attrMetadata);
    numAttrs++;
  }

  /**
   * @param numAttrs
   *          The numAttrs to set.
   */
  public void setNumAttrs(int numAttrs)
  {
    this.numAttrs = numAttrs;
  }

  /**
   * @param scanId
   *          The scanId to set.
   */
  public void setScanId(int scanId)
  {
    this.scanId = scanId;
  }

  /**
   * @return Returns the fullScanId.
   */
  public int getFullScanId()
  {
    return fullScanId;
  }

  /**
   * @param fullScanId
   *          The fullScanId to set.
   */
  public void setFullScanId(int fullScanId)
  {
    this.fullScanId = fullScanId;
    this.propScanId = fullScanId;
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
    boolean exitState = true;
    boolean heartBeatSent = false;
    boolean filterPassed = true;
    SelectState s = (SelectState) mut_state;
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
              //When archived relation tuples are propagated old data
              //propagation is not needed as it will result in duplicate output
              if(oldDataPropNeeded)
              {
                setExecSynopsis((ExecSynopsis) outSynopsis);
                handlePropOldData();
              }
              else 
              {
                //make sure next time if the code comes here we propagate tuples
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
            if (s.inputElement == null)
            {
              /* Bug 16823890:
               * lastInputTs can never be less than lastOutputTs.
               * We send heartbeat when lastInputTs > lastOutputTs.
               * We want to send heartbeat even when lastInputTs==lastOutputTs
               * and when the lastInputOrderingFlag is true and when last input
               * has not passed the filter.
               * We want to send only one such heartbeat so the heartBeatSent
               * flag is used when it is sent. If it is set, we don't send
               * further heartbeat and quit the processing.
               */

              // Output a heartbeat
              if((s.lastInputTs > s.lastOutputTs)
                 || ((s.lastInputTs == s.lastOutputTs) && (s.lastInputOrderingFlag)
                     && (!heartBeatSent) && (!filterPassed))
                )
              {
                s.outputTuple = null;
                s.outputKind = QueueElement.Kind.E_HEARTBEAT;
                s.state = ExecState.S_ALLOCATE_ELEM;
                break;
              }
              else
              {
                s.state = ExecState.S_INIT;
                done = true;
                break;
              }
            }
            else
            {
              // Bump up counts
              if(s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
                s.stats.incrNumInputHeartbeats();
              else
                s.stats.incrNumInputs();

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
             

              // Update the last input Ts  and ordering flag value now
              s.lastInputTs = s.inputTs;
              s.lastInputOrderingFlag = s.inputElement.getTotalOrderingGuarantee();   
              s.prevInputSnapshotId = s.inputElement.getSnapshotId();
          
              // current input timeStamp should be greater than the calculated
              // expected timeStamp in previous execution
              assert s.inputTs >= s.minNextTs : 
                this.getDebugInfo(s.inputTs, s.minNextTs,
                    s.inputElement.getKind().name(), s.lastInputKind.name());
              
              // calculate expected timestamp of next input tuple
              s.minNextTs
                = s.lastInputOrderingFlag ? s.inputTs + 1  : s.inputTs;
  
              // update last input kind
              s.lastInputKind = s.inputElement.getKind();
             
              // Heartbeat: no filtering to be done
              if (s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
              {
                s.state = ExecState.S_INPUT_ELEM_CONSUMED;
                s.inputTuple = null;
                break;
              }
              else
              {
                s.inputTuple = s.inputElement.getTuple();
              }
            }
            s.state = ExecState.S_PROCESSING1;
          case S_PROCESSING1:
            assert ((s.inputElement != null) && (predicate != null));
            assert (s.inputTuple != null);
            evalContext.bind(s.inputTuple, INPUT_CONTEXT);
            if (!predicate.eval(evalContext))
            {
              // Tuple doesn't satisfy the predicate
              filterPassed = false;
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;
              break;
            }
            filterPassed = true;
            s.outputKind = s.inputElement.getKind();
            s.state = ExecState.S_PROCESSING2;
          case S_PROCESSING2:
            assert s.inputElement != null;

            if (s.outputKind == QueueElement.Kind.E_PLUS)
            {
              s.outputTuple = tupleStorageAlloc.allocate();

              // Since we have allocated the tuple, we need to make sure that
              // we process this tuple completely
              exitState = false;
              s.state = ExecState.S_PROCESS_PLUS;
            }
            else
            {
              assert (s.outputKind == QueueElement.Kind.E_MINUS);
              if(outSynopsis != null)
              {
                if (s.minusTuple == null)
                {
                  IAllocator<ITuplePtr> tf = factoryMgr.get(attrSpecs);
                  s.minusTuple = tf.allocate(); //SCRATCH_TUPLE
                  // s.minusTuple will be kept in a memory all the time
                  // no unpin will be done.
                }
              }
              else
              { // a new tuple needs to be allocated even for every minus input
                s.outputTuple = tupleStorageAlloc.allocate();
              }
              exitState = false;
              s.state = ExecState.S_PROCESS_MINUS;
            }
            
            //if synopsis is null even when the output is a relation
            //copy over the id from input to output for both PLUS and MINUS
            if((outSynopsis == null) && (!this.isStream))
            {
              ITuple tup = s.outputTuple.pinTuple(IPinnable.WRITE);
              tup.setId(s.inputTuple.getId());
              s.outputTuple.unpinTuple();
            }
            break;

          case S_PROCESS_PLUS:
            // Populate output tuple
            s.outputTuple.copy(s.inputTuple, numAttrs);
            if (outSynopsis != null)
              outSynopsis.insertTuple(s.outputTuple);
            s.state = ExecState.S_ALLOCATE_ELEM;
            break;

          case S_PROCESS_MINUS:
            if(outSynopsis != null)
            {
              s.minusTuple.copy(s.inputTuple, numAttrs);
              evalContext.bind(s.minusTuple, IEvalContext.UPDATE_ROLE);
  
              TupleIterator scan = outSynopsis.getScan(scanId);
              
              s.outputTuple = scan.getNext();
              if(s.outputTuple == null)
              {
                LogUtil.fine(LoggerType.TRACE, "Call to a non-deterministic" +
                	"function is returning different results for same input");
                
                throw new ExecException(
                  ExecutionError.NON_DETERMINISTIC_FUNCTION_NOT_ALLOWED_IN_PREDICATE);
              }
              
              outSynopsis.releaseScan(scanId, scan);
              outSynopsis.deleteTuple(s.outputTuple);
            }
            else
            { // archived rel is in the lineage and this operator is either
              // query operator or is in the path from source to query op
              s.outputTuple.copy(s.inputTuple, numAttrs);
            }

            s.state = ExecState.S_ALLOCATE_ELEM;

          case S_ALLOCATE_ELEM:
            s.state = ExecState.S_OUTPUT_TIMESTAMP;

          case S_OUTPUT_TIMESTAMP:
          {
            s.state = ExecState.S_OUTPUT_READY;
          }
          case S_OUTPUT_READY:
            if (s.outputKind == QueueElement.Kind.E_HEARTBEAT)
            {
              assert s.inputElement == null;
              s.outputTuple = null;
              s.lastOutputTs = s.lastInputTs;
              //The flag should be the same as last input received.
              s.outputElement.setTotalOrderingGuarantee(s.lastInputOrderingFlag); 
              s.outputElement.heartBeat(s.lastInputTs);
              //Bug 16923890: sent a heartbeat now so set the flag.
              //This is to ensure that we don't enter into loop sending the hbs.
              heartBeatSent = true;
            }
            else
            {
              s.outputElement.setTuple(s.outputTuple);
              s.outputElement.setTs(s.inputTs);
              s.outputElement.setKind(s.outputKind);
              //copy input flag as is
              s.outputElement.setTotalOrderingGuarantee(s.inputElement.getTotalOrderingGuarantee());
              // Update last output ts
              s.lastOutputTs = s.inputTs;
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
              //bug 16923890: 
              /*
               * HB is sent when prev input doesn't match predicate OR
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
            exitState = true;
            s.state = ExecState.S_PROCESSING5;
          case S_PROCESSING5:
            if (s.outputKind == QueueElement.Kind.E_HEARTBEAT)
            {
              s.state = ExecState.S_INIT;
              break;
            }
            else
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;

          case S_INPUT_ELEM_CONSUMED:
            assert s.inputElement != null;

            if (s.inputTuple != null)
            {
              inTupleStorageAlloc.release(s.inputTuple);
            }

            s.state = ExecState.S_INIT;
            break;

          default:
            break;
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

  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<Select id=\"" + id + "\" >");
    sb.append("<InputQueue>" + inputQueue.toString() + "</InputQueue>");
    sb.append("<OutputQueue>" + outputQueue.toString() + "</OutputQueue>");
    sb.append("<Predicate>");
    sb.append(predicate.toString());
    sb.append("</Predicate>");
    sb.append("</Select>");

    return sb.toString();
  }

  protected RelationSynopsis getOutSynopsis()
  {
    return outSynopsis;
  }

  protected int getNumAttrs()
  {
    return numAttrs;
  }

  protected int getScanId()
  {
    return scanId;
  }

  protected TupleSpec getAttrSpecs()
  {
    return attrSpecs;
  }
  
  @Override
  public void enqueueHeartbeat(Long hbtTime) throws ExecException 
  {
    SelectState s = (SelectState)mut_state;
    
    // send heartbeat with ordering guarantee false
    s.lastOutputTs = hbtTime;
    s.outputElement.heartBeat(s.lastOutputTs);
    s.outputElement.setTotalOrderingGuarantee(false);
    s.outputElement.setKind(QueueElement.Kind.E_HEARTBEAT);
    s.outputElement.setSnapshotId(execContext.getPlanMgr().getCurrentSnapshotId());
    ((ISharedQueueWriter) outputQueue).enqueue(s.outputElement, 
                                               this.getArchiverReaders());    
    LogUtil.info(LoggerType.TRACE, 
                 this.getOptName() + " sent heartbeat of "+ s.lastOutputTs + " " + s.outputElement +
                 " with ordering guarantee false (special join)");
    
    s.stats.incrNumOutputHeartbeats();    
  }

  @Override
  public void initializeState() throws CEPException
  {
    if(archivedRelationTuples != null)
    {
      SelectState s = (SelectState) mut_state;
      for(ITuplePtr currentTuple : archivedRelationTuples)
      {
        if(outSynopsis != null)
          outSynopsis.insertTuple(currentTuple);

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
      s.lastOutputTs=snapShotTime + 1;
      heartbeatTime = snapShotTime + 1;  // needed for "special join"
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
                    ", propagated events received from archiver downstream");
      //only if 
      //1. There are archived relation tuples which have been enqueued
      // AND
      //2. Propagation (of old data) is indicated as required at this point
      //Set the oldDataPropNeeded to false to avoid duplicate output.
      if((archivedRelationTuples.size() > 0) && (this.propagationReqd()))
        oldDataPropNeeded = false;
      
      //remove all the archived relation tuples.
      archivedRelationTuples.clear();
    }
    
  }

  /**
   * Create snapshot of Group Aggregate operator by writing the operator state
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
      output.writeObject((SelectState)mut_state);

      // Write output synopsis to output stream
      if (outSynopsis != null)
      {
    	  outSynopsis.writeExternal(output, new SynopsisPersistenceContext(fullScanId));
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
      SelectState loaded_mutable_state = (SelectState) input.readObject();
      ((SelectState)mut_state).copyFrom(loaded_mutable_state);
      
      IPersistenceContext sharedSynopsisRecoveryCtx = new SynopsisPersistenceContext();
      sharedSynopsisRecoveryCtx.setCache(new HashSet());
      
      // Read output synopsis from input stream
      if (outSynopsis != null)
    	  outSynopsis.readExternal(input, sharedSynopsisRecoveryCtx);     
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
