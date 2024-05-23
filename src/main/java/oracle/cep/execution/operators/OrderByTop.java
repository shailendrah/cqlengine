/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/OrderByTop.java /main/8 2013/08/01 09:22:43 udeshmuk Exp $ */

/* Copyright (c) 2009, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    07/24/13 - bug 16966411 and 16968207
    anasrini    12/19/10 - replace eval() with eval(ec)
    udeshmuk    04/13/09 - add getDebugInfo to assertion
    sbishnoi    03/23/09 - piggyback optimization
    sbishnoi    03/10/09 - introducing partition by
    sbishnoi    02/11/09 - Creation
 */

package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.logging.Level;

import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.comparator.TupleComparator;
import oracle.cep.execution.indexes.HashIndex;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.orderby.PartitionByContext;
import oracle.cep.execution.synopses.ExecSynopsis;
import oracle.cep.execution.synopses.LineageSynopsis;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.service.ExecContext;


/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/OrderByTop.java /main/8 2013/08/01 09:22:43 udeshmuk Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class OrderByTop extends ExecOpt
{
  /** evaluation context */
  private IEvalContext             evalContext;
  
  /** number of order-by rows*/
  private int                      numOrderByRows;
  
  /** flag to check if input to this operator is stream or relation*/
  private boolean                  isInputStream;
  
  /** final queue will contain top n elements*/ 
  private PriorityQueue<ITuplePtr> finalQueue;
  
  /** backUp queue will keep remaining relation*/
  private PriorityQueue<ITuplePtr> backUpQueue;
  
  /** Synopsis storing the output of the project */
  private LineageSynopsis          outSynopsis;
  
  /** flag to check if plus tuple is pending for this iteration*/
  private boolean                  isPlusPending;
  
  /** iterator over final queue*/
  private Iterator<ITuplePtr>      iter;
  
  
  
  /** eval to copy attribute value from outputTuple to nextOutputTuple */
  private IAEval                   outEval;
  
  /** iterator over output synopsis */
  private TupleIterator            outSynIter;
  
  /** number of partition by attributes*/
  private int                      numPartitionByAttrs;
  
  /** position of partition context object inside partition header tuple */
  private int                      partitionContextPos;
  
  /** index to partition's header */
  private HashIndex                partitionHdrIndex;
  
  /** tuple comparator for final queue */
  private TupleComparator          finalQueueTupComparator;
  
  /** tuple comparator for backup queue */
  private TupleComparator          backupQueueTupComparator;
 
  /** factory to allocate partition tuple */
  private IAllocator<ITuplePtr>    partitionTupleFactory;
  
  /** eval to copy partition attributes' from input tuple to partition tuple */
  private IAEval                   copyEval;
  
  /** local variable used to keep partition header tuple*/
  private ITuple                   partitionHdrTuple;
  
  /** local variable used to refer to partitionContext object */
  private PartitionByContext       pContext;
  
  /** local flag variable to check whether partition exist or not */
  private boolean                  isPartitionExist;
  
  /** local flag to check if current MINUS element' corresponding PLUS 
   * is in finalQ */
  private boolean                  found ;
  
  /** flag to determine total ordering for next output tuple */
  private boolean                  nextTupleTotalOrdering;
  
  
  public OrderByTop(ExecContext ec)
  {
    super(ExecOptType.EXEC_ORDER_BY_TOP, new OrderByTopState(ec), ec);
    numOrderByRows = 0;
    isInputStream  = false;
    finalQueue     = null;
    outSynopsis    = null;
    isPlusPending  = false;
    found          = false;
    iter           = null;    
    outEval        = null;
    outSynIter     = null;    
    
    pContext                 = null;
    numPartitionByAttrs      = 0;
    partitionContextPos      = -1;
    partitionHdrIndex        = null;
    partitionHdrTuple        = null;
    finalQueueTupComparator  = null;
    backupQueueTupComparator = null;
    copyEval                 = null;
    isPartitionExist         = false;
    nextTupleTotalOrdering   = false;
  }
        
  protected int run(int timeslice) throws CEPException 
  {
    int numElements = timeslice;
    boolean done = false;
    OrderByTopState s = (OrderByTopState)mut_state;
    boolean exitState = true;
    boolean heartBeatSent = false;
    
    assert s.state != ExecState.S_UNINIT;

    // Increment number of executions Statistics
    s.stats.incrNumExecutions();
    try
    {
      while ((s.stats.getNumInputs() < numElements) || (!exitState) )
      {
        switch (s.state)
        {
          case S_PROPAGATE_OLD_DATA:
            setExecSynopsis((ExecSynopsis) outSynopsis);
            handlePropOldData();
          break;
          
          case S_INIT:
            // Get next input element
            s.inputElement = inputQueue.dequeue(s.inputElementBuf);  
            if(s.inputElement == null)
            {
              /*
               * Bug 16966411 and 16968207: Need to ensure that hb with TOF
               * as true is propagated downstream.
               */
              if((s.lastInputTs > s.lastOutputTs) ||
                 ((s.lastInputTs == s.lastOutputTs) &&
                  (!heartBeatSent) && 
                  (s.inputKind == QueueElement.Kind.E_HEARTBEAT) && 
                  (s.lastInputOrderingFlag)))
              {
                s.state = ExecState.S_GENERATE_HEARTBEAT;
                exitState = false;
                break;
              }
              
              s.state = ExecState.S_INIT;
              done = true;
              break;
            }
            else if(s.inputElement != null)
            {
              s.inputTs = s.inputElement.getTs();
              s.inputKind = s.inputElement.getKind();
              // get the total ordering flag from input element
              s.lastInputOrderingFlag 
                = s.inputElement.getTotalOrderingGuarantee();
              s.state = ExecState.S_INPUT_DEQUEUED;             
            }
            
          case S_INPUT_DEQUEUED:
            exitState = false;
            // update our count for total inputs
            if(s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
              s.stats.incrNumInputHeartbeats();
            else
              s.stats.incrNumInputs();

            // We should have a progress of time.
            if (s.lastInputTs > s.inputTs)
            {
                throw ExecException.OutOfOrderException(
                        this,
                        s.lastInputTs, 
                        s.inputTs, 
                        s.inputElement.toString());
            }
          
            assert s.inputTs >= s.minNextInputTs : getDebugInfo(s.inputTs,
              s.minNextInputTs, s.inputKind.name(), s.lastInputKind.name());
        
            s.minNextInputTs = s.inputElement.getTotalOrderingGuarantee() ? 
              s.inputTs + 1 : s.inputTs;
            s.lastInputKind = s.inputKind;
            
            if(s.inputKind == QueueElement.Kind.E_HEARTBEAT)
            {
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;         
              s.lastInputTs = s.inputTs;
              break;
            }
            else 
            {
              s.inputTuple = s.inputElement.getTuple();
              s.lastInputTs = s.inputTs;
              if(s.inputKind == QueueElement.Kind.E_PLUS)
              {
                s.state = ExecState.S_PROCESS_PLUS;                
              }
              else if(s.inputKind == QueueElement.Kind.E_MINUS)
              {
                s.state = ExecState.S_PROCESS_MINUS;
              }
              
              if(numPartitionByAttrs > 0)
              {
                s.nextState = s.state;
                s.state = ExecState.S_FIND_PARTITION;
              }
              else
                break;
            }
          
          case S_FIND_PARTITION:
            // scan hash table for the current input partition attribute value
            evalContext.bind(s.inputTuple, IEvalContext.INPUT_ROLE);
            TupleIterator tupIter = partitionHdrIndex.getScan();
            
            // Check if partition exists or not
            if(tupIter != null)
            {
              s.partitionHdrTuple = tupIter.getNext();
              isPartitionExist = s.partitionHdrTuple != null;
              
              // switch to appropriate state on the basis of isPartitionExist
              s.state = isPartitionExist ? 
                          ExecState.S_PROCESSING8 : 
                          ExecState.S_PROCESSING7;
              
              // release the scan; it will reinitialize TupleIterator
              partitionHdrIndex.releaseScan(tupIter);
              break;
            }
            else
            {
              isPartitionExist = false;
              s.state = ExecState.S_PROCESSING7;
            }           
            
          case S_PROCESSING7:
            // Assertion: there is no partition for current input tuple
            assert !isPartitionExist;
            
            // Assertion: If Partition doesn't exist; input shouldn't be MINUS
            assert s.inputKind != QueueElement.Kind.E_MINUS : s.inputKind;
            
            // construct partition header tuple
            s.partitionHdrTuple = partitionTupleFactory.allocate();
            
            // copy partition attributes
            evalContext.bind(s.partitionHdrTuple, IEvalContext.UPDATE_ROLE);
            copyEval.eval(evalContext);
            
            // construct PartitionByContext and set its reference in
            // partition header tuple
            partitionHdrTuple
              = s.partitionHdrTuple.pinTuple(IPinnable.WRITE);
            
            pContext
              = new PartitionByContext(finalQueueTupComparator,
                                       backupQueueTupComparator, 
                                       numOrderByRows,
                                       isInputStream);
            // partitionContextPos should not be -1;
            assert partitionContextPos != -1;
            
            // set the PartitionByContext object inside partitionHdr tuple
            partitionHdrTuple.oValueSet(partitionContextPos, pContext);
            s.partitionHdrTuple.unpinTuple();
            
            partitionHdrIndex.insertTuple(s.partitionHdrTuple);
            
            // initialize final and backup queue from partition context
            finalQueue = pContext.getFinalQueue();
            backUpQueue = pContext.getBackUpQueue();
            
            s.state = s.nextState;
            break;
            
          case S_PROCESSING8:
            // Assertion: Partition Exist for the current input tuple
            assert isPartitionExist;
           
            // get ITuple from Tuple pointer in READ mode
            partitionHdrTuple
              = s.partitionHdrTuple.pinTuple(IPinnable.READ);
            
            // get the reference to PartitionByContext from tuple
            pContext = (PartitionByContext)partitionHdrTuple.oValueGet(
              partitionContextPos);
            
            // initialize the queues
            finalQueue  = pContext.getFinalQueue();
            backUpQueue = pContext.getBackUpQueue();
            
            s.partitionHdrTuple.unpinTuple();
            s.state     = s.nextState;            
            break;
            
          case S_PROCESS_PLUS:
            finalQueue.add(s.inputTuple);
            
            // Insert into output synopsis will be at later stages
                        
            if(finalQueue.size() <= numOrderByRows)
            {             
              //As final queue is not grown beyond the limit; expiredTuple=null
              s.expiredTuple = null;
              s.state = ExecState.S_PROCESSING1;
            }
            else
            {
              s.state = ExecState.S_PROCESSING2;
            }
            break;            
          case S_PROCESS_MINUS:
            // Check if this MINUS's corresponding PLUS is in finalQueue            
            iter = finalQueue.iterator();
            s.expiredTuple = null;
            while(iter.hasNext())
            {
              s.expiredTuple = iter.next();
              if(s.expiredTuple.compare(s.inputTuple))
              {
                found = true;
                break;
              }
            }
            // If it is found in finalQueue; this means that it is not inside
            //  backupQueue; so we will remove it from finalQueue and insert
            //  next candidate from backUpQueue to finalQueue with 2 steps:
            //   1) send MINUS of this tuple
            //   2) send PLUS of the tuple which is just now inserted into 
            //      finalQueue from backUpQueue
            //   [some of above steps are implemented in PROCESSING5]
            // If it is not inside finalQueue; this means the tuple will be in
            //  backupQueue; so we will remove it from backUpQueue
            if(found)
            {
              // reset found variable
              found = false;
              finalQueue.remove(s.expiredTuple);        
              // Remove from output synopsis will happen in ALLOCATE_ELEM              
              s.state = ExecState.S_PROCESSING5;
            }
            else
            {
              // input must be a relation
              assert !isInputStream;
              
              backUpQueue.remove(s.inputTuple);
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            }
            break;
          case S_PROCESSING1:
            // Note: This state is reached when finalQueue is not full
            //      and input element is a PLUS element
            
            // Send input tuple as PLUS output tuple
            s.outputTuple = s.inputTuple;
            s.outputElement.setKind(QueueElement.Kind.E_PLUS);
            // Conditions to set total ordering flag for output element:
            //  1) If current input tuple ordering flag is TRUE
            //      set output element's flag to TRUE
            //  2) If current input tuple ordering flag is FALSE
            //      set output element's flag to FALSE
            // [i.e. copy input element flag to output element]
            s.outputElement.setTotalOrderingGuarantee(s.lastInputOrderingFlag);
            
            s.state = ExecState.S_ALLOCATE_ELEM;
            break;
            
          case S_PROCESSING2:         
            // Note: This state will be reached when final Queue is full
            //       and we received a PLUS input tuple
            
            s.expiredTuple = finalQueue.remove();
            // Remove from output synopsis will happen in ALLOCATE_ELEM
            
            // expiredTuple will never be null
            assert s.expiredTuple != null;
            // If input is relation
            //  then insert the expired tuple into backUpQueue to consider
            //  it later for processing
            if(!isInputStream)               
              backUpQueue.add(s.expiredTuple);
            //if(s.expiredTuple.compare(s.inputTuple))
            if(s.expiredTuple.equals(s.inputTuple))
            {
              // If the input Tuple doesn't deserve to be inside priority
              // queue; 
              //  then discard it if input is stream
              //  else insert it into backUpQueue(already done before if)
              // and process next input tuple              
              //Bug #16677757: 
	      //We should send a heartbeat in this case if the input ordering 
	      //flag is true in order to convey time progress.
	      if(s.lastInputOrderingFlag)
                s.state = ExecState.S_GENERATE_HEARTBEAT;
              else
                s.state= ExecState.S_INPUT_ELEM_CONSUMED;
              break;
            }
            else
            {
              s.state = ExecState.S_PROCESSING3;
            }
            
          case S_PROCESSING3:
            // Condition to reach here:
            //   input tuple will replace one tuple from finalQueue and 
            //   that tuple is expiredTuple
            // Next Step:
            //    Send input tuple as PLUS output tuple and
            //    Send expired Tuple as MINUS output tuple
           
            s.outputTuple = s.expiredTuple;
            s.outputElement.setKind(QueueElement.Kind.E_MINUS);
            isPlusPending = true;
            
            // Here expiredTuple is being expired by a PLUS input tuple
            // so we will send that PLUS tuple as output tuple after this MINUS
            // expired tuple; So the next tuple will be of same time-stamp
            // which means expired tuple will go with ordering flag = false
            s.outputElement.setTotalOrderingGuarantee(false);
            
            s.nextState = ExecState.S_PROCESSING4;            
            s.state = ExecState.S_ALLOCATE_ELEM;
            break;
          case S_PROCESSING4:
            s.outputTuple = s.inputTuple;
            s.outputElement.setKind(QueueElement.Kind.E_PLUS);
            s.nextState = null;
            isPlusPending = false;
            
            // As this is the pending PLUS tuple; so next output tuple's
            // time-stamp can be equal or higher depending on next input
            // time-stamp; so this pending PLUS' flag is same as input 
            // ordering flag
            s.outputElement.setTotalOrderingGuarantee(s.lastInputOrderingFlag);
            
            s.state = ExecState.S_ALLOCATE_ELEM;
            break;
          case S_PROCESSING5:
            // At this state
            // check if backUpQueue has any tuple which can be inserted into
            // final Queue
            // if backUpQueue is empty then 
            //    a) send MINUS of expired tuple
            //    b) if finalQueue is empty AND partition by attributes are 
            //       present; then 
            //       delete partition tuple corresponding to this input tuple
            // else
            //    a) send MINUS of the tuple which is expired from final queue
            //    a) send PLUS of nextBackupTuple
            
            // setting the flag to input element's flag value
            nextTupleTotalOrdering = s.lastInputOrderingFlag;
            
            
            // input must be a relation
            assert !isInputStream;
            
            if(! backUpQueue.isEmpty())
            {
              s.nextBackupTuple= backUpQueue.remove();            
              // finalQueue must have atleast one empty slot for
              // new insertions
              finalQueue.add(s.nextBackupTuple);
              // Insert into output synopsis will be at later states
                    
              isPlusPending = true;
              
              // As there is a pending plus output tuple;
              // so the total order flag for the current expired tuple will be
              // false
              nextTupleTotalOrdering = false;
              
              s.nextState = ExecState.S_PROCESSING6;              
            }
            else if(numPartitionByAttrs > 0 && finalQueue.isEmpty())
            {
              // Assertion: At this point; there should be one partition
              // corresponding to the MINUS input tuple
              // Note: this state can be reached after receiving a MINUS
              // tuple only.
              assert s.partitionHdrTuple != null;
              
              // reset flags and variables
              isPartitionExist  = false;
              finalQueue        = null;
              backUpQueue       = null;
              pContext          = null;
              partitionHdrTuple = null;  
              
              // remove partition tuple from hashIndex
              partitionHdrIndex.deleteTuple(s.partitionHdrTuple);
              
              // deallocate the tuple
              partitionTupleFactory.release(s.partitionHdrTuple);            
            }
            // Send a MINUS Of s.expiredTuple which we removed from finalQueue
            s.outputTuple = s.expiredTuple;
            s.outputElement.setKind(QueueElement.Kind.E_MINUS);
            
            // set total ordering flag for this expired tuple
            s.outputElement.setTotalOrderingGuarantee(nextTupleTotalOrdering);
            
            s.state = ExecState.S_ALLOCATE_ELEM;
            break;
          case S_PROCESSING6:
            s.outputTuple = s.nextBackupTuple;
            s.outputElement.setKind(QueueElement.Kind.E_PLUS);
            
            // As this is the pending PLUS tuple; so next output tuple's
            // time-stamp can be equal or higher depending on the next input
            // time-stamp value; so this pending PLUS' flag is same as input 
            // ordering flag
            s.outputElement.setTotalOrderingGuarantee(s.lastInputOrderingFlag);
            
            s.nextState = null;
            isPlusPending = false;
            s.nextBackupTuple = null;            
            s.state = ExecState.S_ALLOCATE_ELEM;         
          case S_ALLOCATE_ELEM:            
            s.tupleLineage[0] = s.outputTuple;
            try
            {              
              // update output synopsis
              if(s.outputElement.getKind() == QueueElement.Kind.E_PLUS)
              {
                // If next output element is PLUS; then allocate a new tuple
                //  and copy the attributes from outputTuple
                s.nextOutputTuple = tupleStorageAlloc.allocate();
                evalContext.bind(s.nextOutputTuple, IEvalContext.NEW_OUTPUT_ROLE);
                evalContext.bind(s.outputTuple, IEvalContext.INPUT_ROLE);
                outEval.eval(evalContext);
                outSynopsis.insertTuple(s.nextOutputTuple, s.tupleLineage);
              }
              else if(s.outputElement.getKind() == QueueElement.Kind.E_MINUS)
              {       
                // If next output element is MINUS; then delete the lineage's tuple
                // from outSynopsis and send MINUS of this to output queue
                outSynIter = outSynopsis.getScan_l(s.tupleLineage);
                s.nextOutputTuple = outSynIter.getNext();
                outSynopsis.deleteTuple(s.nextOutputTuple);                
                outSynopsis.releaseScan_l(outSynIter);
              }
            }
            catch(SoftExecException se)
            {
              tupleStorageAlloc.release(s.nextOutputTuple);
              throw se;
            }
            s.state = ExecState.S_OUTPUT_READY;          
          case S_OUTPUT_READY:
            s.outputElement.setTuple(s.nextOutputTuple);
            s.outputElement.setTs(s.inputTs);           
            s.lastOutputTs = s.inputTs;
            s.state = ExecState.S_OUTPUT_ELEMENT;
          case S_OUTPUT_ELEMENT:
            if (outputQueue.isFull())
            {
              done = true;
              break;
            }
            outputQueue.enqueue(s.outputElement);
            s.state = ExecState.S_OUTPUT_ENQUEUED;
          case S_OUTPUT_ENQUEUED:
            if(s.outputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
              s.stats.incrNumOutputHeartbeats();
            else
              s.stats.incrNumOutputs();
            if(isPlusPending)
            {
              s.state = s.nextState;
              break;
            }
            else
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;           
          case S_INPUT_ELEM_CONSUMED:                     
            s.state = ExecState.S_INIT;
            // reset state variables
            s.outputTuple = null;
            s.expiredTuple = null;            
            exitState = true;
            break;
          case S_GENERATE_HEARTBEAT:
            s.outputTs = s.lastInputTs;
            s.outputElement.heartBeat(s.outputTs);
            
            // Set the total order flag to last input's flag
            //  If we are sure that next input is coming with higher time-stamp
            //  then we can send a heartBeat with flag TRUE as next output will
            //  be of higher time-stamp value for sure. 
            s.outputElement.setTotalOrderingGuarantee(s.lastInputOrderingFlag);
            
            s.lastOutputTs= s.outputTs;
            if (outputQueue.isFull())
            {
              done = true;
              break;
            }
            outputQueue.enqueue(s.outputElement);
            heartBeatSent = true;
            s.stats.incrNumOutputHeartbeats();
            s.state = ExecState.S_INIT;
            exitState = true;
            break; 
          default:
            assert false;
          
        }
        if(done)
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
   * Getter
   * @return number of rows in order-by clause
   */
  public int getNumOrderByRows()
  {
    return numOrderByRows;
  }
  
  /**
   * Setter
   * @param paramNumOrderByRows number of rows in order-by clause
   */
  public void setNumOrderByRows(int paramNumOrderByRows)
  {
    numOrderByRows = paramNumOrderByRows;
  }
  
  /**
   * Setter
   * @param paramFlag flag value indicates that input to this operator is a 
   *   stream or a relation
   */
  public void setIsInputStream(boolean paramFlag)
  {
    isInputStream = paramFlag;
  }
  
  /**
   * Setter
   * @param refOutSynopsis output synopsis for this operator
   */
  public void setOutSynopsis(LineageSynopsis refOutSynopsis)
  {
    outSynopsis = refOutSynopsis;
  }
  
  /**
   * Getter for outEval
   * @return
   */
  public IAEval getOutEval()
  {
    return outEval;
  }
  
  /**
   * Setter for outEval
   * @param paramOutEval
   */
  public void setOutEval(IAEval paramOutEval)
  {
    outEval = paramOutEval;  
  }
  
  /**
   * Set number of partition by attributes
   * @param numAttrs parameter
   */
  public void setNumPartitionByAttrs(int numAttrs)
  {
    numPartitionByAttrs = numAttrs; 
  }
  
  /**
   * Get number of partition by attributes
   * @return number of partition by attributes
   */
  public int getNumPartitionByAttrs()
  {
    return numPartitionByAttrs; 
  }
  
  /**
   * Set partition header index
   * @param paramHashIndex
   */
  public void setPartitionHdrIndex(HashIndex paramHashIndex)
  {
    partitionHdrIndex = paramHashIndex;  
  }
  
  /**
   * Set partition context position
   * @param pos integer position value
   */
  public void setPartitionContextPos(int pos)
  {
    partitionContextPos = pos;
  }
  
  /**
   * Set factory for partition tuple
   * @param paramAllocator
   */
  public void setPartitionTupleFactory(IAllocator<ITuplePtr> paramAllocator)
  {
    partitionTupleFactory = paramAllocator;
  }
  
  /**
   * Set tuple comparator for final priority queue
   * @param paramTupComparator
   */
  public void setFinalQueueTupleComparator(TupleComparator paramTupComparator)
  {
    finalQueueTupComparator = paramTupComparator;
  }
  
  /**
   * Set tuple comparator for backup priority queue
   * @param paramTupComparator
   */
  public void setBackUpQueueTupleComparator(TupleComparator paramTupComparator)
  {
    backupQueueTupComparator = paramTupComparator;
  }
  
  /**
   * Set copyEval for the execution operator OrderByTop
   * @param paramEval parameter eval
   */
  public void setCopyEval(IAEval paramEval)
  {
    copyEval = paramEval;
  }
  
  /**
   * Initialize the final and backup queues when no partition by attributes
   * are specified
   */
  public void initializeQueues()
  {
    // number of partition by attributes should be zero
    assert numPartitionByAttrs == 0;
    
    // tuple comparator for both queues must be initialized
    assert finalQueueTupComparator != null;
    assert backupQueueTupComparator != null;
    
    // Although we Initialize orderedQueue with initial capacity equal to
    // (numOrderByRows + 1); Extra ONE is the new tuples which will be
    // inserted at the time of arrival to compare with existing top tuples
    // Although PQueue is a growing data structure but finalQueue will never
    // grow larger than numOrderByRows
    finalQueue = new PriorityQueue<ITuplePtr>
         ((new Long(numOrderByRows + 1)).intValue(), 
          finalQueueTupComparator);
    
    // Initialize orderedQueue with initial capacity equal to 
    // ONE; It can grow as per the window size
    // Condition: 
    //  If input is a stream; then there is no need to save the tuples in
    //  backup queue
  
    if(!isInputStream)
      backUpQueue = new PriorityQueue<ITuplePtr>(1, backupQueueTupComparator);
  }
    
  public void deleteOp() 
  {}
  
  /**
   * Create snapshot of Order By Top operator by writing the operator state
   * into param java output stream.
   * State of OrderByTop operator consists of following:
   * 1. Mutable State
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
      if (outSynopsis == null) 
      {
         output.writeBoolean(true);
      }
      else
      { 
        output.writeBoolean(false);
        outSynopsis.writeExternal(output);
      }
      output.writeInt(numOrderByRows);      
      output.writeBoolean(isPlusPending);      
      output.writeBoolean(isPartitionExist);
      output.writeBoolean(found);
      output.writeBoolean(nextTupleTotalOrdering);
      
      if (finalQueue == null)
      {
          output.writeBoolean(true);
      }
      else
      {
          output.writeBoolean(false);
          output.writeObject(finalQueue);
      }
      
      if (backUpQueue == null)
      {
          output.writeBoolean(true);
      }
      else
      {
          output.writeBoolean(false);
          output.writeObject(backUpQueue);
      }

      if (partitionHdrIndex == null)
      {
          output.writeBoolean(true);
      }
      else
      {
          output.writeBoolean(false);
          output.writeInt(partitionHdrIndex.getSize());
          TupleIterator fullscan = partitionHdrIndex.getFullScan();
          ITuplePtr hdrTupPtr;
          while ( (hdrTupPtr = fullscan.getNext()) != null)
          {
              output.writeObject(hdrTupPtr);
              ITuple hdrTuple = hdrTupPtr.pinTuple(IPinnable.READ);
              PartitionByContext pCtx = hdrTuple.oValueGet(partitionContextPos);
              pCtx.writeExternal(output);
              hdrTupPtr.unpinTuple();
          }
          partitionHdrIndex.releaseScan(fullscan);
      }
      // Write Mutable state to output stream
      output.writeObject((OrderByTopState)mut_state);      
    } 
    catch (IOException e)
    {
      LogUtil.logStackTrace(e); 
      throw new CEPException(ExecutionError.SNAPSHOT_CREATION_ERROR, e, e.getLocalizedMessage(), this.getOptName());
    }
  }
  
@SuppressWarnings("unchecked")
protected void loadSnapshot(ObjectInputStream input) throws ExecException
  {
    try
    {
      boolean isOutSynposisNull = input.readBoolean();
      if (!isOutSynposisNull) 
      {
         outSynopsis.readExternal(input);
      }
    	
      numOrderByRows = input.readInt();
      isPlusPending = input.readBoolean();
      isPartitionExist = input.readBoolean();
      found = input.readBoolean();
      nextTupleTotalOrdering = input.readBoolean();
      
      boolean isQNull = input.readBoolean();      
      if (!isQNull) 
      {
          finalQueue  = (PriorityQueue<ITuplePtr>) input.readObject();
      }
  
      isQNull = input.readBoolean();
      if (!isQNull) 
      {      
          backUpQueue = (PriorityQueue<ITuplePtr>) input.readObject();
      }

      boolean isNull = input.readBoolean();
      if (!isNull)
      {
          int htSize = input.readInt();
          int idx = 0;
          while (idx < htSize)
          {
              ITuplePtr tuplePtr = (ITuplePtr)input.readObject();
              partitionHdrTuple = tuplePtr.pinTuple(IPinnable.WRITE);

              PartitionByContext pCtx = new PartitionByContext(finalQueueTupComparator,
                      backupQueueTupComparator, 
                      numOrderByRows,
                      isInputStream);

              // partitionContextPos should not be -1;
              assert partitionContextPos != -1;

              pCtx.readExternal(input);

              // set the PartitionByContext object inside partitionHdr tuple
              partitionHdrTuple.oValueSet(partitionContextPos, pCtx);
              tuplePtr.unpinTuple();

              partitionHdrIndex.insertTuple(tuplePtr);
              ++idx;
          }
      }
      // Read MutableState from input stream
      OrderByTopState loaded_mutable_state = (OrderByTopState) input.readObject();
      ((OrderByTopState)mut_state).copyFrom(loaded_mutable_state);      
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
