/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ConcurrentStreamSource.java /main/6 2011/08/18 12:08:43 alealves Exp $ */

/* Copyright (c) 2009, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
      anasrini  08/09/11 - XbranchMerge anasrini_bug-12845846_ps5 from
                           st_pcbpel_11.1.1.4.0
      anasrini  08/08/11 - invoke enqueueConcurrent
      anasrini  04/25/11 - XbranchMerge anasrini_bug-11905834_ps5 from main
      anasrini  03/21/11 - set mut_state in resetMutableState
      alealves  02/24/11 - Concurrency infra and initial operators
      alealves  02/24/11 - Concurrency infra and initial operators
      alealves  04/23/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ConcurrentStreamSource.java st_pcbpel_alealves_9261513/5 2010/07/09 12:57:58 alealves Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.operators;

import java.util.logging.Level;

import oracle.cep.dataStructures.external.TupleKind;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.QueueElement.Kind;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.queues.DirectInteropQueue;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.service.ExecContext;
import oracle.cep.util.DebugUtil;

public class ConcurrentStreamSource extends StreamSource
{
  private IAllocator<QueueElement> queueElemFac;

  public ConcurrentStreamSource(ExecContext ec, int maxAttrs)
  {
    super(ec, maxAttrs);
    
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    queueElemFac = factoryMgr.get(FactoryManager.QUEUEELEMENT_FACTORY_ID);
  }
  
  private QueueElement allocQueueElement()
  {
    try
    {
      return queueElemFac.allocate();
    }
    catch (ExecException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }
    return null;
  }
  
  public int run(TupleValue inputValue, MutableState state)
      throws CEPException
  {
    StreamSourceState s = (StreamSourceState) state;
    QueueElement outputElement = allocQueueElement();
    
    try 
    {
      assert inputValue != null : "Heartbeats should be received as " +
      		"a valid input value and not as a null value";
      
      if (isStatsEnabled)
      {
        s.stats.incrNumExecutions();
        s.stats.incrNumInputs();
      }

      // Save  the original TupleValue -
      // used in partition parallelism by the EXCHANGE operator
      outputElement.setTupleValue(inputValue);
      
      // Mostly we just need to copy the input to the output
      ITuplePtr outputTuple = tupleStorageAlloc.allocate();
      populateOutput(outputTuple, inputValue);
      
      // REVIEW Even though it is unordered, it is still useful to set this to use as the 'starting time'
      populateOutputTS(outputTuple, inputValue.getTime(), numAttrs - 1);
      outputElement.setTs(inputValue.getTime());
      
      // Populate the rest of the attributes
      outputElement.setTuple(outputTuple);

      setKind(outputElement, inputValue);
      
      ((DirectInteropQueue)outputQueue).enqueueConcurrent(outputElement);

      if (isStatsEnabled)
      {
        s.stats.incrNumOutputs();
      }
    }
    catch (CEPException e)
    {
      // CQL's logging level FINE is equivalent to Apache's DEBUG 
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      if (e instanceof SoftExecException)
      {
        LogUtil.warning(LoggerType.TRACE, e.getMessage());
        return 0;
      }
      throw (e);
    }
    
    return 0;
  }

  private void setKind(QueueElement outputElement, TupleValue inputValue)
      throws SoftExecException
  {
    if (inputValue.getKind() == TupleKind.PLUS)
      outputElement.setKind(Kind.E_PLUS);
    else if (inputValue.getKind() == TupleKind.HEARTBEAT) // Only when upstream to exchange operator
      outputElement.setKind(Kind.E_HEARTBEAT);
    else 
    {
      if(inputValue.getKind() == TupleKind.MINUS)
      {
        throw new SoftExecException(ExecutionError.INVALID_MINUS_TUPLE,
            inputValue.toSimpleString(), this.toString());
      }
      else if(inputValue.getKind() == TupleKind.UPDATE)
      {
        throw new SoftExecException(ExecutionError.INVALID_UPDATE_TUPLE,
            inputValue.toSimpleString(), this.toString());
      }
    }
  }
  
  protected void populateOutput(ITuplePtr outputTuple, TupleValue inputTuple) throws CEPException
  {
    if (outputTuple == null)
      return;
    
    try
    {
      ITuple o = outputTuple.pinTuple(IPinnable.WRITE);
      o.copyFrom(inputTuple, numAttrs - 1, attrSpecs);
      outputTuple.unpinTuple();
    }
    catch(CEPException e)
    {
      throw new SoftExecException(ExecutionError.PROPAGATE_ERROR,e.getMessage(),
          e.getCauseMessage(), e.getAction());
    }
    catch(Throwable e)
    {
      LogUtil.config(LoggerType.TRACE, e.toString() + "\n" 
          + source.toString() + "\n" 
          + inputTuple.toString() + "\n" 
          + DebugUtil.getStackTrace(e));
      throw new SoftExecException(ExecutionError.INCORRECT_INPUT_TUPLE,
          source.toString(), this.toString());
    }
  }
  
  protected void populateOutputTS(ITuplePtr outputTuple, long timestamp, int pos)
  throws CEPException
  {
    if (outputTuple == null)
      return;
    ITuple o = outputTuple.pinTuple(IPinnable.WRITE);
    o.lValueSet(pos, timestamp);
    outputTuple.unpinTuple();
  }

  @Override
  public MutableState resetMutableState()
  {
    boolean collectStats =
      execContext.getServiceManager().getConfigMgr().isStatsEnabled();
    
    // Always generate a new object
    if (collectStats)
      mut_state = new StreamSourceState(execContext, collectStats);
    else
      mut_state = null;

    return mut_state;
  }

}
