/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ConcurrentOutput.java /main/5 2011/11/23 09:58:43 alealves Exp $ */

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
      alealves  02/24/11 - Concurrency infra and initial operators
      alealves  02/24/11 - Concurrency infra and initial operators
    alealves    Apr 28, 2010 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ConcurrentOutput.java st_pcbpel_alealves_9261513/5 2010/07/09 12:57:58 alealves Exp $
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
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.service.ExecContext;

public class ConcurrentOutput extends Output
{
  private int maxAttrs;

  public ConcurrentOutput(ExecContext ec, int maxAttrs)
  {
    super(ec, maxAttrs);
    this.maxAttrs = maxAttrs;
  }

  @Override
  public MutableState resetMutableState()
  {
    return new OutputState(execContext, maxAttrs);
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
      OutputState s = (OutputState) state;
      s.stats.incrNumExecutions();

      assert !((inputElement == null) || 
          (inputElement.getKind() != QueueElement.Kind.E_PLUS) ||
          inputElement.getTotalOrderingGuarantee()) :
          "Relations, TotalOrdering not supported for unordered events.";

      s.stats.incrNumInputs();

      s.inputElement = inputElement;
      s.inputTuple = inputElement.getTuple();
      
      //calculateStatsLatency(s);
      
      allocateOutput(s);
      populateOutput(s, s.inputTuple);
      
      // REVIEW Even though they are not ordered, the ts can be used to deduce the starting time.
      s.outputTuple.setTime(s.inputElement.getTs());
      
      try
      {
        getOutput().putNext(s.outputTuple, s.inputElement.getKind());
      }
      catch(CEPException e)
      {
        // If the downstream components throws a soft exception, then consume exception (and event) 
        if(e.getErrorCode() == ExecutionError.DOWNSTREAM_CHANNEL_SOFT_EXCEPTION)
        {                     
          LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
        }
        else
          throw e;
      }
      catch(RuntimeException e)
      {
        throw new ExecException(ExecutionError.DOWNSTREAM_CHANNEL_EXCEPTION, e, getOptName());
      }

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
  
  protected void populateOutput(OutputState s, ITuplePtr   tPtr) 
  throws CEPException
  {
    ITuple t = tPtr.pinTuple(IPinnable.WRITE);
    assert t != null;

    t.copyTo(s.outputTuple, getNumAttrs(), getAttrSpecs(), getInCols());
    tPtr.unpinTuple();

    // This is for performance measurements - 
    // can be used to figure out latency due to CQL engine processing alone
    s.outputTuple.setEngineOutTime(System.nanoTime());
  }  
}
