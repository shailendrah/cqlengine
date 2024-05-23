/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ViewStrmSrc.java /main/27 2011/06/17 11:31:28 alealves Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 anasrini    12/19/10 - replace eval() with eval(ec)
 sbishnoi    04/10/09 - adding minNextTs
 sbishnoi    01/21/09 - total order optimization
 sbishnoi    12/22/08 - changing element time to long value
 hopark      10/10/08 - remove statics
 hopark      10/09/08 - remove statics
 hopark      02/28/08 - resurrect refcnt
 hopark      12/07/07 - cleanup spill
 hopark      10/30/07 - remove IQueueElement
 hopark      10/21/07 - remove TimeStamp
 parujain    10/04/07 - delete op
 hopark      09/07/07 - eval refactor
 anasrini    09/04/07 - ELEMENT_TIMEELEMENT_TIME support
 hopark      07/13/07 - dump stack trace on exception
 parujain    06/26/07 - mutable state
 hopark      05/24/07 - debug logging
 hopark      05/16/07 - add arguments for OutOfOrderException
 parujain    05/08/07 - monitoring statistics
 hopark      04/08/07 - fix pincount
 hopark      03/21/07 - add TuplePtr pin
 najain      03/14/07 - cleanup
 najain      03/12/07 - bug fix
 hopark      03/06/07 - spill-over support
 najain      02/15/07 - bug fix
 anasrini    09/21/06 - temp for debugging
 najain      08/02/06 - refCounting optimizations
 najain      08/10/06 - add asserts
 najain      08/03/06 - view strm share underlying store
 najain      07/19/06 - ref-count tuples 
 najain      07/13/06 - ref-count timestamps 
 najain      07/12/06 - ref-count elements 
 najain      07/10/06 - move inStore to parent 
 najain      05/24/06 - bug fix 
 najain      05/22/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ViewStrmSrc.java st_pcbpel_anasrini_eval_parallelism_2/1 2010/12/19 07:35:40 anasrini Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.operators;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.logging.Level;

import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.service.ExecContext;


/**
 * ViewStrmSrc is the execution operator that reads input tuples for registered
 * views.
 *
 * @author najain
 */
public class ViewStrmSrc extends ExecOpt
{
  private SimpleDateFormat sdf;

  /** The copy eval */
  private IAEval copyEval;

  /** evaluation context */
  private IEvalContext evalContext;

  /** Position of the ELEMENT_TIME column */
  private int elemTimePos;
  
  /**
   * Constructor for ViewStrmSrc
   * @param ec TODO
   * 
   */
  public ViewStrmSrc(ExecContext ec)
  {
    super(ExecOptType.EXEC_VIEW_STRM_SRC, new ViewStrmSrcState(ec), ec);
    
    sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    sdf.setLenient(false);
  }

  /**
   * Getter for evalContext
   * 
   * @return Returns the evalContext
   */
  public IEvalContext getEvalContext()
  {
    return evalContext;
  }

  /**
   * Setter for evalContext
   * 
   * @param evalContext
   *          The evalContext to set.
   */
  public void setEvalContext(IEvalContext evalContext)
  {
    this.evalContext = evalContext;
  }

  /**
   * Getter for copyEval
   * 
   * @return Returns the copyEval
   */
  public IAEval getCopyEval()
  {
    return copyEval;
  }

  /**
   * Setter for copyEval
   * 
   * @param copyEval
   *          The copyEval to set.
   */
  public void setCopyEval(IAEval copyEval)
  {
    this.copyEval = copyEval;
  }

  /**
   * Setter for ELEMENT_TIME column position
   * 
   * @param elemTimePos
   *          The ELEMENT_TIME column position
   */
  public void setElemTimePos(int elemTimePos)
  {
    this.elemTimePos = elemTimePos;
  }
  
  protected int getElemTimePos()
  {
    return this.elemTimePos;
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
    ViewStrmSrcState s = (ViewStrmSrcState) mut_state;
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
          case S_INIT:
            // Get next input element
            s.inputElement = inputQueue.dequeue(s.inputElementBuf);
            s.state = ExecState.S_INPUT_DEQUEUED;
          case S_INPUT_DEQUEUED:
            // Determine the next step based on element kind
            if (s.inputElement == null)
            {
              // Process if Input tuple is NULL
              if (s.lastInputTs <= s.lastOutputTs)
              {
                s.state = ExecState.S_INIT;
                done = true;
                break;
              }

              // Output a HeartBeat
              s.outputTuple = null;
              s.outputKind = QueueElement.Kind.E_HEARTBEAT;
              s.state = ExecState.S_OUTPUT_READY;
              break;
            }
            else
            { // Process if INPUT tuple is not NULL
              // Update our counts
              exitState = false;
              if(s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
                s.stats.incrNumInputHeartbeats();
              else
                s.stats.incrNumInputs();

              // Get input Tuple from queue element
              s.inputTuple = s.inputElement.getTuple();

              // Update last input ts
              s.inputTs = s.inputElement.getTs();
              
              //// FOR DEBUGGING - TEMPORARY
              /*
              LogUtil.fine(LoggerType.TRACE, optName + "," + 
                                sdf.format(new Timestamp(s.inputTs.getValue()))
                                 + "," + s.inputElement.getKind());
              */


              // We should have a progress of time.
              if (s.lastInputTs > s.inputTs)
              {
                //System.out.println("OutOfOrderException");
                //System.out.println("ERROR: event timestamp is out of order"+ 
                //" timestamp0="+ s.lastInputTs + " timestamp1="+ s.inputTs +
                //" event= " + s.inputElement.toString() + " in " + getOptName());
                throw ExecException.OutOfOrderException(
                        this,
                        s.lastInputTs, 
                        s.inputTs, 
                        s.inputElement.toString());
              }
             
              // ensure that the time stamp value is as per the OrderingFlag 
              assert s.inputTs >= s.minNextTs :
                getDebugInfo(s.inputTs, s.minNextTs, 
                             s.inputElement.getKind().name(),
                             s.lastInputKind.name());
              
              // Update the last input Ts now
              s.lastInputTs = s.inputTs;
              s.lastInputKind = s.inputElement.getKind();

              // Update current input tuple's total ordering flag
              // which will tell whether the next tuple will come at 
              // higher TimeStamp or not
              s.isTotalOrderingGuarantee 
                = s.inputElement.getTotalOrderingGuarantee();

              s.minNextTs = s.isTotalOrderingGuarantee ? s.inputTs + 1 :
                                                         s.inputTs;
              
              // Update lastTotalOrderingFlag to save the flag of last
              // input tuple in case if we need to propagate HeartBeat
              // in future
              s.lastTupleTotalOrderingGuarantee = s.isTotalOrderingGuarantee;
                
              // In case of HeartBeat input; Nothing more to be done.
              // Only update lastInputTs, isTotalOrderingGuarantee, 
              // nextTotalOrderingGuarantee
              if (s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
                s.state = ExecState.S_INPUT_ELEM_CONSUMED;
              else
              {
                // Stream never gets negative tuples
                assert (s.inputElement.getKind() == QueueElement.Kind.E_PLUS);
                s.outputKind = s.inputElement.getKind();
                s.state = ExecState.S_PROCESSING1;
              }
              break;
            }

          case S_PROCESSING1:
            // Allocate an output tuple
            s.outputTuple = tupleStorageAlloc.allocate();
            s.state = ExecState.S_PROCESSING2;
          case S_PROCESSING2:
            // Compute the output tuple
            evalContext.bind(s.inputTuple, IEvalContext.INPUT_ROLE);
            evalContext.bind(s.outputTuple, IEvalContext.NEW_OUTPUT_ROLE);
            copyEval.eval(evalContext);
            s.state = ExecState.S_PROCESSING3;
          case S_PROCESSING3:
            // Handle ELEMENT_TIME
            ITuple o = s.outputTuple.pinTuple(IPinnable.WRITE);
            o.lValueSet(elemTimePos, s.lastInputTs);
            s.outputTuple.unpinTuple();
            s.state = ExecState.S_OUTPUT_READY;
          case S_OUTPUT_READY:
            //Configure OutputElement
            if (s.outputKind == QueueElement.Kind.E_HEARTBEAT)
            {
              assert s.inputElement == null;
              s.lastOutputTs= s.lastInputTs;
              s.outputElement.heartBeat(s.lastInputTs);
              // Action: Output Element's total order flag set by last input 
              //         element's flag value
              // Reason: If last input comes with flag = TRUE; It means no that
              //         no input having TimeStamp more than last tuple will  
              //        come; So we can set the flag to last input tuple's flag 
              s.outputElement.setTotalOrderingGuarantee( 
                s.lastTupleTotalOrderingGuarantee);
            }
            else
            {
              s.outputElement.setTuple(s.outputTuple);
              s.outputElement.setTs(s.inputTs);
              s.outputElement.setKind(s.inputElement.getKind());
              
              // Action: Set Output Element' total Ordering flag by current I/P
              //         flag value
              // Reason: This operator will only pass the input tuple to output
              //         queue; so copy the flag as it is.
              s.outputElement.setTotalOrderingGuarantee(
                s.isTotalOrderingGuarantee);
              
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
    
            exitState = false;
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
   * Create snapshot of View Stream Source (ViewStrmSrc) operator by writing the operator state into output stream.
   * State of ViewStreamSrc operator consists of following:
   * 1. Mutable State (ViewStrmSrc)
   * 
   * Please note that we will write the state of operator in above sequence, so
   * the loadSnapshot should also read the state in the same sequence.
   * @param output
   * @throws CEPException 
   */
  @Override
  protected void createSnapshot(ObjectOutputStream output) throws CEPException
  {
    try
    {
      //snapshot mutable state
      output.writeObject((ViewStrmSrcState)mut_state);
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
      ViewStrmSrcState mutable_state = (ViewStrmSrcState)input.readObject();
      ((ViewStrmSrcState)mut_state).copyFrom(mutable_state);
    } catch (ClassNotFoundException | IOException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE,e);
      throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR, e, e.getLocalizedMessage(), getOptName());
    }
  }

}
