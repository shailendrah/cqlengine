/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ConcurrentBinStreamJoin.java /main/2 2011/08/09 07:16:02 anasrini Exp $ */

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
    alealves    Mar 22, 2011 - Creation
      anasrini  08/08/11 - invoke enqueueConcurrent
      alealves  03/22/11 - Creation
 */

/**
 *  @version $Header$
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.operators;

import java.util.logging.Level;

import oracle.cep.dataStructures.internal.ITuple;
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

public class ConcurrentBinStreamJoin extends BinStreamJoin
{
  public ConcurrentBinStreamJoin(ExecContext ec)
  {
    super(ec);
  }
  
  @Override
  public MutableState resetMutableState()
  {
    mut_state = new BinStreamJoinState(execContext); 
    return mut_state;
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
      BinStreamJoinState s = (BinStreamJoinState) state;
      s.stats.incrNumExecutions();

      assert !((inputElement == null) || 
          (inputElement.getKind() != QueueElement.Kind.E_PLUS) ||
          inputElement.getTotalOrderingGuarantee()) : 
            "Relations, TotalOrdering not supported for unordered events.";
      
      assert isExternal :
      "Unordered events only supported for external bin stream joins";
      
      // Events may be out-of-order, therefore do not try to establish what
      // should be the min timestamp to be processed on either outer or 
      // inner sides.
      s.outerElement = inputElement;
      s.outerTuple = inputElement.getTuple();
      
      // Returns new eval context that can be used as auto var so that operator
      // can run in parallel.
      IEvalContext evalCtx = newEvalContext();
      
      evalCtx.bind(s.outerTuple, IEvalContext.OUTER_ROLE);
      
      // Scan of inner tuples that join with outer tuple
      s.innerScan = innerExtSyn.getScan(evalCtx);
     
      // Get next inner tuple
      assert s.innerScan != null;
      
      processScanTuple(s, evalCtx);
      
      assert s.innerTuple == null;

      // No more tuples
      innerExtSyn.releaseScan(s.innerScan);
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
        getOuterTupleStorageAlloc().release(inputElement.getTuple());
      }
    }

    return 0;
  }

  private void processScanTuple(BinStreamJoinState s, IEvalContext evalCtx)
      throws ExecException
  {
    while ((s.innerTuple = s.innerScan.getNext()) != null)
    {
      evalCtx.bind(s.innerTuple, IEvalContext.INNER_ROLE);
    
      s.outputTuple = tupleStorageAlloc.allocate();

      // construct the output tuple
      evalCtx.bind(s.outputTuple, IEvalContext.NEW_OUTPUT_ROLE);
      outputConstructor.eval(evalCtx);
      
      // Although event are unordered, we still want to assign a timestamp to them,
      //  if, for no other reason, then to allow for monitoring.
      s.outputTs = s.outerElement.getTs();
      ITuple o = s.outputTuple.pinTuple(IPinnable.WRITE);
      o.lValueSet(getElemTimePos(), s.outputTs);
      s.outputTuple.unpinTuple();
      
      s.outputElement.setKind(QueueElement.Kind.E_PLUS);
      s.outputElement.setTs(s.outputTs);
      s.outputElement.setTuple(s.outputTuple);
    
      // Release the innerTuple
      getInnerTupleStorageAlloc().release(s.innerTuple);
      s.innerTuple = null;

      // Finally, output result.
      ((DirectInteropQueue)outputQueue).enqueueConcurrent(s.outputElement);
    
      s.stats.incrNumOutputs();
    }
  }

  private IEvalContext newEvalContext() throws ExecException
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
    return evalCtx;
  }

}
