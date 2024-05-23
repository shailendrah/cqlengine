/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/BufferOp.java /main/8 2015/04/14 02:49:38 udeshmuk Exp $ */

/* Copyright (c) 2012, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    04/08/15 - set initializedState to true even when zero archiver
                           tuples are returned
    udeshmuk    08/12/13 - assign snapshot id to archiver tuples
    udeshmuk    07/10/13 - fix logging related to archived relation framework
    vikshukl    02/18/13 - set heartbeat time
    udeshmuk    01/25/13 - use initializestate boolean instead of
                           ARCHIVED_SIA_DONE
    udeshmuk    01/25/13 - no need to set state to archive_sia_done here, done
                           in planmanager.propagateArchivedrelationtuples
    vikshukl    10/01/12 - note the fact that state initialization is done
    udeshmuk    09/09/12 - add error message on assertion failure
    sbishnoi    08/20/12 - bug 14502856
    udeshmuk    07/07/12 - buffer execution operator on top of
                           project/relnsrc/select. used in archived relation
                           setup only.
    udeshmuk    07/07/12 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/BufferOp.java /main/8 2015/04/14 02:49:38 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.queues.ISharedQueueWriter;
import oracle.cep.execution.synopses.LineageSynopsisImpl;
import oracle.cep.execution.synopses.RelationSynopsisImpl;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.service.ExecContext;

/**
 * This Buffer operator will be used only in archived relation based queries.
 * Only a project or a relation source or a select can be the input for this
 * operator. The phyopt for this operator is added directly at the physical 
 * layer (no logopt exists). 
 * 
 * The logic of when to add a buffer operator to the plan is present in the
 * PlanManager.
 * 
 * The buffer operator just passes every type of event received from input to 
 * output without changing it. It just maintains the syn/store which its
 * input would have maintained had it not been an archived relation based
 * query.
 * @author udeshmuk
 *
 */

public class BufferOp extends ExecOpt
{
  /**
   * True if the operator maintains a lineage synopsis,
   * False if the operator maintains a relational synopsis.
   */
  private boolean isLineageSynStore = false;
  
  /**
   * Lineage out synopsis
   */
  private LineageSynopsisImpl lineageOutSyn = null;
  
  /**
   * Relational out synopsis
   */
  private RelationSynopsisImpl relationOutSyn = null;
  
  /**
   * index scan id - used only if relational synopsis.
   */
  private int scanId = -1;
  
  /**
   * full scan id - used only if relational synopsis
   */
  @SuppressWarnings("unused")
  private int fullScanId = -1;
  
  /**
   * evaluation context
   */
  private IEvalContext evalContext = null;

  private int numAttrs;

  private TupleSpec attrSpecs;
  
  private boolean initializedState = false;
  
  public BufferOp(ExecContext ec, int maxAttrs)
  {
    super(ExecOptType.EXEC_BUFFER, new BufferState(ec), ec);
    numAttrs = 0;
    attrSpecs = new TupleSpec(factoryMgr.getNextId(), maxAttrs);
  }

  @Override 
  protected int run(int timeslice) throws CEPException
  {
    BufferState s = (BufferState)mut_state;
    
    assert s.state != ExecState.S_UNINIT;
    
    s.stats.incrNumExecutions();
    
    try
    {
      while(s.stats.getNumInputs() < timeslice)
      {
        //dequeue the input element
        s.inputElement = inputQueue.dequeue(s.inputElementBuf);
        s.inputTuple   = null; 
        
        if(s.inputElement == null)
        {
          break;
        }
        
        //if input element is not null then 
        //1. set variables
        //2. call appropriate handler based on kind of the element.
        // Update input stats
        if(s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
          s.stats.incrNumInputHeartbeats();
        else
          s.stats.incrNumInputs();
        
        // Get the input tuple from queue element
        s.inputTuple = s.inputElement.getTuple();
        
        // Get the input timestamp
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
        
        assert s.inputTs >= s.minNextInputTs:
          getDebugInfo(s.inputTs, s.minNextInputTs,
            s.inputElement.getKind().name(), s.lastInputKind.name());

        // calculate the expected timestamp for next input tuple
        s.minNextInputTs = s.inputElement.getTotalOrderingGuarantee() ?
                           s.inputTs+1 : s.inputTs;

        
        // Process the input according to the type
        if (s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
        {
          //propagate the heartbeat as is
          sendOutput(s);
        }
        else if (s.inputElement.getKind() == QueueElement.Kind.E_PLUS)
        {
          handlePlus(s);
        }
        else if (s.inputElement.getKind() == QueueElement.Kind.E_MINUS)
        {
          handleMinus(s);         
        }

        // update the last input timestamp and input kind
        s.lastInputTs = s.inputTs;
        s.lastInputKind = s.inputElement.getKind();
        
        // input element consumed
        if (s.inputTuple != null)
        {
          inTupleStorageAlloc.release(s.inputTuple);
        }

      }
    }
    catch(SoftExecException e1)
    {
      
    }
    return 0;
  }

  private void handleMinus(BufferState s) throws ExecException
  {
    if (this.isArchivedDim())
    {
      // this operator is part of a branch that is based on archived
      // dimension. We have already initialized this operator, 
      // so this must be a streaming change.
      if(this.initializedState)
      {
        throw new 
          ExecException(ExecutionError.ARCHIVED_DIMENSION_CHANGE_DETECTED);
      }
    }
    
    if(this.isLineageSynStore)
    {
      assert lineageOutSyn != null;
      // Get the previously output tuple for this input
      s.tupleLineage[0] = s.inputTuple;

      // Currently we are assuming that scan will always
      // have a single tuple.
      TupleIterator projScan = lineageOutSyn.getScan_l(s.tupleLineage);
      s.outputTuple = projScan.getNext();
    
      // There should be only one tuple with this lineage
      assert projScan.getNext() == null : "More than one tuple in lineage "+
        "synopsis match the input minus tuple "+s.inputTuple;
      lineageOutSyn.releaseScan_l(projScan);
      if(s.outputTuple != null)
        lineageOutSyn.deleteTuple(s.outputTuple);
    }
    else
    {
      assert relationOutSyn != null;
      if(s.minusTuple == null)
      {
        IAllocator<ITuplePtr> tf = factoryMgr.get(attrSpecs);
        s.minusTuple = tf.allocate(); //SCRATCH_TUPLE
        // s.minusTuple will be kept in a memory all the time
        // no unpin will be done.
      }
      
      //lookup in out relation synopsis
      s.minusTuple.copy(s.inputTuple, numAttrs);
      evalContext.bind(s.minusTuple, IEvalContext.UPDATE_ROLE);
      
      TupleIterator scan = relationOutSyn.getScan(scanId);
      s.outputTuple = scan.getNext();
      
      //release the scan and delete tuple
      relationOutSyn.releaseScan(scanId, scan);
      if(s.outputTuple != null)
        relationOutSyn.deleteTuple(s.outputTuple);
    }
    
    if(s.outputTuple == null)
    {
      LogUtil.warning(LoggerType.TRACE, "ARF# "+
                   "Buffer operator received a negative tuple that does not "
                   +"have a corresponding plus in synopsis. "+s.inputTuple);
      
      throw new ExecException(ExecutionError.INVALID_NEGATIVE_RELATION_TUPLE,
                              new Object[]{s.inputTuple});
    }
    else
      //output the tuple    
      sendOutput(s);
  }

  private void handlePlus(BufferState s) throws ExecException
  {
    if (this.isArchivedDim())
    {
      // this operator is part of a branch that is based on archived
      // dimension. We have already initialized this operator, 
      // so this must be a streaming change.
      if(this.initializedState)
      {
        throw new 
          ExecException(ExecutionError.ARCHIVED_DIMENSION_CHANGE_DETECTED);
      }
    }
        
    //allocate a new output tuple
    s.outputTuple = tupleStorageAlloc.allocate();
    //copy the input tuple into output tuple
    s.outputTuple.copy(s.inputTuple, numAttrs);
    // put the outputTuple in the synopsis
    if(this.isLineageSynStore)
    {
      assert lineageOutSyn != null;
      s.tupleLineage[0] = s.inputTuple;
      lineageOutSyn.insertTuple(s.outputTuple, s.tupleLineage);
    }
    else
    {
      assert relationOutSyn != null;
      relationOutSyn.insertTuple(s.outputTuple);
    }
    
    //output the tuple
    sendOutput(s);
  }

  private void sendOutput(BufferState s) throws ExecException
  {
    s.outputElement.setKind(s.inputElement.getKind());
    s.outputElement.setTotalOrderingGuarantee(
      s.inputElement.getTotalOrderingGuarantee());
    if(s.outputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
    {
      s.outputTuple = null;
      s.outputElement.heartBeat(s.inputTs);
    }
    else
    {
      s.outputElement.setTs(s.inputTs);
      s.outputElement.setTuple(s.outputTuple);
    }
    s.outputElement.setSnapshotId(s.inputElement.getSnapshotId());
    s.lastOutputTs = s.inputTs;
    outputQueue.enqueue(s.outputElement);
    
    if(s.outputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
      s.stats.incrNumOutputHeartbeats();
    else
      s.stats.incrNumOutputs();    
  }
  
  @Override
  public void deleteOp()
  {

  }

  public void setIsLineageSynStore(boolean isProjectInput)
  {
    this.isLineageSynStore = isProjectInput;
  }

  public void setLineageOutSynopsis(LineageSynopsisImpl linsyn)
  {
    this.lineageOutSyn = linsyn;
  }

  public void setScanId(int scanId)
  {
    this.scanId = scanId;
  }
  
  public void setFullScanId(int fullScanId)
  {
    this.fullScanId = fullScanId;
  }

  public void setEvalContext(IEvalContext evalContext)
  {
    this.evalContext = evalContext;
  }

  public void setRelationSynopsis(RelationSynopsisImpl e_outSyn)
  {
    this.relationOutSyn = e_outSyn;
  }
  
  public void addAttr(AttributeMetadata attrMetadata) throws ExecException
  {
    attrSpecs.addAttr(numAttrs, attrMetadata);
    numAttrs++;
  }
  
  public void initializeState() throws ExecException
  {
    if(archivedRelationTuples != null)
    {
      BufferState s = (BufferState) mut_state;
      for(ITuplePtr currentTuple : archivedRelationTuples)
      {
        if(this.isLineageSynStore)
        {
          assert lineageOutSyn != null;
          //use the tuple itself as lineage
          lineageOutSyn.insertTuple(currentTuple, new ITuplePtr[]{currentTuple});
        }
        else
        {
          assert relationOutSyn != null;
          relationOutSyn.insertTuple(currentTuple);
        }
        
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
      
      // send heartbeat with ordering guarantee false
      // also save heartbeat away. Needed for special join.
      s.lastOutputTs=snapShotTime + 1;
      heartbeatTime = snapShotTime + 1;  
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
      
      //remove all the archived relation tuples.
      archivedRelationTuples.clear();
    }    
    initializedState = true;
    LogUtil.finer(LoggerType.TRACE, this.getOptName()+ " set initializedState to true.");
  }
}
