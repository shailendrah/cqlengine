/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ConcurrentViewStrmSrc.java /main/2 2011/08/09 07:16:02 anasrini Exp $ */

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
      alealves  06/21/11 - Support for concurrent views
      alealves  02/24/11 - Concurrency infra and initial operators
      alealves  05/18/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ConcurrentProject.java st_pcbpel_alealves_9261513/3 2010/07/09 12:57:59 alealves Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.operators;

import java.util.logging.Level;

import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.queues.DirectInteropQueue;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.service.ExecContext;

public class ConcurrentViewStrmSrc extends ViewStrmSrc
{

  public ConcurrentViewStrmSrc(ExecContext ec)
  {
    super(ec);
  }

  @Override
  public MutableState resetMutableState()
  {
    return new ViewStrmSrcState(execContext);
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
    ViewStrmSrcState s = (ViewStrmSrcState) state;

    try 
    {
      s.stats.incrNumExecutions();

      assert !((inputElement == null)
          || (inputElement.getKind() != QueueElement.Kind.E_PLUS) ||
          inputElement.getTotalOrderingGuarantee()) :
        "Relations, TotalOrdering not supported for unordered events";

      // Update our counts
      s.stats.incrNumInputs();

      // Get tuple and ts
      s.inputElement = inputElement;
      s.inputTuple = inputElement.getTuple();
      
      s.outputKind = s.inputElement.getKind();
      s.outputTuple = tupleStorageAlloc.allocate();

      copyTuple(s.inputTuple, s.outputTuple);

      s.outputElement.setTuple(s.outputTuple);
      s.outputElement.setKind(s.inputElement.getKind());

      // REVIEW Even though they are not ordered, the ts can be used
      // to deduce the starting time.
      populateTS(s);

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
      if (s.inputTuple != null)
      {
        inTupleStorageAlloc.release(s.inputTuple);
      }
    }
    
    return 0;
  }

  private void populateTS(ViewStrmSrcState s) throws ExecException
  {
    s.outputElement.setTs(s.inputElement.getTs());
    
    ITuple o = s.outputTuple.pinTuple(IPinnable.WRITE);
    o.lValueSet(getElemTimePos(), s.inputElement.getTs());
    s.outputTuple.unpinTuple();
  }

  private void copyTuple(ITuplePtr inputTuple, ITuplePtr outputTuple) throws ExecException
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
    
    evalCtx.bind(inputTuple, IEvalContext.INPUT_ROLE);
    evalCtx.bind(outputTuple, IEvalContext.NEW_OUTPUT_ROLE);
    
    try {
      getCopyEval().eval(evalCtx);
    }catch(SoftExecException se)
    {
      tupleStorageAlloc.release(outputTuple);
      throw se;
    }
  }
}
