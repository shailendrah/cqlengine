/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ConcurrentSelect.java /main/4 2011/08/09 07:16:02 anasrini Exp $ */

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
      alealves  02/24/11 - Concurrency infra and initial operators
      alealves  02/24/11 - Concurrency infra and initial operators
      alealves  04/29/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ConcurrentSelect.java st_pcbpel_alealves_9261513/5 2010/07/09 12:57:58 alealves Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.operators;

import java.util.logging.Level;

import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.queues.DirectInteropQueue;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.ExecContext;

public class ConcurrentSelect extends Select
{

  public ConcurrentSelect(ExecContext ec, int maxAttrs)
  {
    super(ec, maxAttrs);

  }

  @Override
  public MutableState resetMutableState()
  {
    return new SelectState(execContext);
  }
  
  @Override
  public int run(int timeSlice, QueueElement inputElement) throws CEPException
  {
    MutableState state = resetMutableState();
    int ret = run(timeSlice, inputElement, state);
    commitMutableState(state);
    
    return ret;
  }  

  public int run(int timeSlice, QueueElement inputElement, MutableState state)
      throws CEPException
  {

    try 
    {
      SelectState s = (SelectState) state;
      s.stats.incrNumExecutions();

      assert !((inputElement == null) || (getOutSynopsis() != null) 
          || (inputElement.getKind() != QueueElement.Kind.E_PLUS) ||
          inputElement.getTotalOrderingGuarantee()) : 
        "Relations, TotalOrdering not supported for unordered events.";

      s.stats.incrNumInputs();

      // Assing method arguments to state in case we may eventually need to persist the processing state.
      s.inputElement = inputElement;
      s.inputTuple = inputElement.getTuple();

      if (!performSelection(s))
        return 0;

      s.outputTuple = tupleStorageAlloc.allocate();
      s.outputTuple.copy(s.inputTuple, getNumAttrs());

      s.outputElement.setTuple(s.outputTuple);
      s.outputElement.setKind(s.inputElement.getKind());

      // REVIEW Even though they are not ordered, the ts can be used to deduce the starting time.
      s.outputElement.setTs(s.inputElement.getTs());

      ((DirectInteropQueue)outputQueue).enqueueConcurrent(s.outputElement);

      s.stats.incrNumOutputs();

    }
    catch (SoftExecException e1)
    {
      // TODO Ignore them
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e1);
    }
    finally 
    {
      if (inputElement != null && inputElement.getTuple() != null)
      {
        inTupleStorageAlloc.release(inputElement.getTuple());
      }
    }
    
    return 0;
  }

  private boolean performSelection(SelectState s) throws AssertionError,
      ExecException
  {
    IEvalContext evalCtx = null;
    try
    {
      evalCtx = (IEvalContext) getEvalContext().clone();
    } catch (CloneNotSupportedException e)
    {
      // This is a programmer's mistake, evaluation contexts used for 
      //  operators that support parallelism must be cloneable.
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      throw new ExecException(ExecutionError.GENERIC_ERROR, e);
    }
    
    evalCtx.bind(s.inputTuple, INPUT_CONTEXT);
    return getPredicate().eval(evalCtx);
  }

}
