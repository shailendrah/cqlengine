/* $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/OrderBy.java /main/12 2009/05/05 21:14:50 sbishnoi Exp $ */

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
    sbishnoi    04/28/09 - fixing assertion error
    udeshmuk    04/13/09 - add getDebugInfo to assertion
    sbishnoi    04/01/09 - piggyback optimization
    sbishnoi    03/03/09 - fix bug 8299728
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    sborah      09/25/08 - check for queue full before enqueue
    hopark      03/04/08 - fix refcount
    hopark      02/28/08 - resurrect refcnt
    hopark      12/07/07 - cleanup spill
    hopark      10/30/07 - remove IQueueElement
    hopark      10/21/07 - remove TimeStamp
    parujain    10/04/07 - delete op
    hopark      07/13/07 - dump stack trace on exception
    parujain    06/28/07 - Orderby operator
    parujain    06/28/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/OrderBy.java /main/12 2009/05/05 21:14:50 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.comparator.ComparatorSpecs;
import oracle.cep.execution.comparator.TupleComparator;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.ExecContext;

public class OrderBy extends ExecOpt
{
  List <ITuplePtr>      sortList;
  TupleComparator       listComparator;
  
  public OrderBy(ExecContext ec)
  {
    super(ExecOptType.EXEC_ORDER_BY, new OrderByState(ec), ec);
    sortList = new ArrayList<ITuplePtr>();    
  }
  
  public void initialize(ComparatorSpecs[] spec, TupleSpec ts)
  {
    listComparator = new TupleComparator(spec, ts);
  }
  
  public void deleteOp() {
   
  }

  protected int run(int timeslice) throws CEPException {
    int numElements      = timeslice;
    boolean done         = false;
    OrderByState s       = (OrderByState)mut_state;
    boolean exitState    = true;
    boolean isSuccessful = false;
    
    assert s.state != ExecState.S_UNINIT;

    // Increment number of executions Statistics
    s.stats.incrNumExecutions();
    try
    {
      while ((s.stats.getNumInputs() < numElements) || (!exitState) )
      {
        switch (s.state)
        {
          case S_INIT:
            // Get next input element
            s.inputElement = inputQueue.dequeue(s.inputElementBuf);
            if(s.inputElement == null)
            {
              if(s.lastInputTs <= s.lastOutputTs)
              {
                s.state = ExecState.S_INIT;
                done = true;
                break;
              }
              s.state = ExecState.S_GENERATE_HEARTBEAT;
              exitState = false;
              break;
            }
            else if(s.inputElement != null)
            {
              s.inputTs   = s.inputElement.getTs();
              s.inputKind = s.inputElement.getKind();         
              s.inputTuple = s.inputElement.getTuple();
              // get the total ordering guarantee flag from input element
              s.isTotalOrderGuarantee 
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

            // Input should be a stream
            assert s.inputKind != QueueElement.Kind.E_MINUS : s.inputKind;
            
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
                        
            // Following are the cases and respective actions:
            // Case 1: currentInputTs = lastInputTs && totalOrderingFlag =false
            //   * Insert input tuple into sortList(if !heartBeat) and process
            //     next input tuple
            // Case 2: currentInputTs = lastInputTs && totalOrderingFlag = true
            //   * As this is the last tuple for this timeStamp; so 
            //     -> insert current input tuple in sortList(if !HB)
            //     -> sort the list
            //     -> read the list and send output
            // Case 3: currentInputTs > lastInputTs && totoalOrdering = false
            //   * Sort and output the list elements as they belong to previous
            //     timeStamp values.
            //   * Insert current input element into sortList (if !heartBeat)
            // Case 4: currentInputTS > lastInputTS && totalOrdering = true
            //   * Sort and output the list elements as they belong to previous
            //     timeStamp values.(outputTs = lastInputTs)
            //   * Output current element with outputTS =currentInputTs(if !HB)
            
            if(s.inputTs == s.lastInputTs)
            {
              s.state = s.isTotalOrderGuarantee ? ExecState.S_PROCESSING2 :
                                                  ExecState.S_PROCESSING1;
            }
            else
            {
              assert s.inputTs > s.lastInputTs;
              s.state = s.isTotalOrderGuarantee ? ExecState.S_PROCESSING5 :
                                                  ExecState.S_PROCESSING4;
            }
            break;            
 
          case S_PROCESSING1:
            // Case 1: currentInputTs = lastInputTs && totalOrderingFlag =false
            //    * Insert input tuple into sortList(if !heartBeat) and process
            //      next input tuple
            if(s.inputKind != QueueElement.Kind.E_HEARTBEAT)
              sortList.add(s.inputTuple);
            s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            break;
            
          case S_PROCESSING2:
            // Case 2: currentInputTs = lastInputTs && totalOrderingFlag = true
            //   * As this is the last tuple for this timeStamp; so 
            //     -> insert current input tuple in sortList(if !HB)
            //     -> sort the list
            //     -> read the list and send output
            if(s.inputKind != QueueElement.Kind.E_HEARTBEAT)
              sortList.add(s.inputTuple);
            
            sortList();
            s.state = ExecState.S_PROCESSING3;
                      
          case S_PROCESSING3:
            // Output list element with timeStamp outputTs = currentInputTS            
            isSuccessful = outputList(s.inputTs);
            if(!isSuccessful)
              done = true;
            else
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            break;
         
          case S_PROCESSING4:
            // Case 3: currentInputTs > lastInputTs && totalOrdering = false
            //   * Sort and output the list elements as they belong to previous
            //     timeStamp values.
            //   * Insert current input element into sortList (if !heartBeat)
            sortList();
            isSuccessful = outputList(s.lastInputTs);
            if(!isSuccessful)
            {
              done = true;
              break;
            }
            if(s.inputKind != QueueElement.Kind.E_HEARTBEAT)
              sortList.add(s.inputTuple);
            s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            break;
            
          case S_PROCESSING5:
            // Case 4: currentInputTS > lastInputTS && totalOrdering = true
            //   * Sort and output the list elements as they belong to previous
            //     timeStamp values.(outputTs = lastInputTs)
            //   * Output current element with outputTS =currentInputTs(if !HB)            
            sortList();
            isSuccessful = outputList(s.lastInputTs);
            if(!isSuccessful)
            {
             done = true;
             break;
            }
            if(s.inputKind == QueueElement.Kind.E_HEARTBEAT)
            {
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;
              break;
            }
            else
            {
              sortList.add(s.inputTuple);
              s.state = ExecState.S_PROCESSING6;
            }
          case S_PROCESSING6:    
            // No need to call sortList() here because list will only have
            // current input tuple
            isSuccessful = outputList(s.inputTs);
            if(!isSuccessful)
            {
              done = true;
              break;
            }
            s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            
          case S_INPUT_ELEM_CONSUMED:
            // Update lastInputTs and lastInputKind
            s.lastInputTs   = s.inputTs;
            s.lastInputKind = s.inputKind;
            
            s.state   = ExecState.S_INIT;
            exitState = true;
            break;
          case S_GENERATE_HEARTBEAT:
            s.outputTs     = s.lastInputTs;
            s.lastOutputTs = s.outputTs;            
            s.outputElement.setTotalOrderingGuarantee(false);
            s.outputElement.heartBeat(s.outputTs);
            if (outputQueue.isFull())
            {
              done = true;
              break;
            }
            outputQueue.enqueue(s.outputElement);         
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
   * Sort the sortList
   */
  private void sortList()
  {    
    if(sortList.size() > 1)
    {
      Collections.sort(sortList, listComparator);
    }      
  }
  
  /**
   * Output the list elements
   * @param outputTs
   * @return true if all the list elements enqueued successfully in output queue
   *         false otherwise
   * @throws ExecException
   */
  private boolean outputList(long outputTs) throws ExecException
  {   
    OrderByState s = (OrderByState)mut_state;   
    while(true)
    {
      switch(s.outputState)
      {
      case S_READ_LIST_ELEM:
        if(!sortList.isEmpty())
        {
          s.outputTuple = sortList.remove(0);
          s.outputState  = ExecState.S_ALLOCATE_ELEM;
        }
        else          
          return true;     
        
      case S_ALLOCATE_ELEM:
        s.outputElement.setTuple(s.outputTuple);
        s.outputState = ExecState.S_OUTPUT_READY;
      
      case S_OUTPUT_READY:
        s.outputElement.setTs(outputTs);
        s.outputElement.setTotalOrderingGuarantee(sortList.isEmpty());
        s.outputElement.setKind(QueueElement.Kind.E_PLUS);
        s.outputState = ExecState.S_OUTPUT_ELEMENT;
        
      case S_OUTPUT_ELEMENT:      
        if (outputQueue.isFull())
          return false;
        outputQueue.enqueue(s.outputElement);
        s.lastOutputTs = outputTs;
        s.outputState = ExecState.S_OUTPUT_ENQUEUED;
        
      case S_OUTPUT_ENQUEUED:
        s.stats.incrNumOutputs();
        s.outputState = ExecState.S_READ_LIST_ELEM;
        break;
      };
    }
   
  }
  
  /**
   * Create snapshot of Order By operator by writing the operator state
   * into param java output stream.
   * State of Group Aggregate operator consists of following:
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
      // Write Mutable state to output stream
      output.writeObject((OrderByState)mut_state);   
      output.writeObject(sortList);
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
      // Read MutableState from input stream
      OrderByState loaded_mutable_state = (OrderByState) input.readObject();
      ((OrderByState)mut_state).copyFrom(loaded_mutable_state);
      sortList = (ArrayList<ITuplePtr>) input.readObject();
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
