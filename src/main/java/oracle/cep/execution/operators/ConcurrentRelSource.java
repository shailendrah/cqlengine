/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ConcurrentRelSource.java /main/3 2011/08/09 07:16:02 anasrini Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    08/09/11 - XbranchMerge anasrini_bug-12845846_ps5 from
                           st_pcbpel_11.1.1.4.0
    anasrini    08/08/11 - invoke enqueueConcurrent
    anasrini    05/17/11 - XbranchMerge anasrini_bug-12560613_ps5 from main
    anasrini    05/17/11 - handle app ts heartbeat for parallelism
    anasrini    03/24/11 - Support for PARTITION parallelism
    anasrini    03/24/11 - Creation
 */

/**
 *  @version $Header: ConcurrentRelSource.java 24-mar-2011.02:54:19 anasrini Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.util.logging.Level;

import oracle.cep.dataStructures.external.TupleKind;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.QueueElement.Kind;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.queues.DirectInteropQueue;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.ExecContext;


public class ConcurrentRelSource extends RelSource
{

  public ConcurrentRelSource(ExecContext ec, int maxAttrs)
  {
    super(ec, maxAttrs);
  }
  
  public int run(TupleValue inputValue, MutableState state)
      throws CEPException
  {
    RelSourceState s = (RelSourceState) state;

    try 
    {
      s.stats.incrNumExecutions();

      s.stats.incrNumInputs();

      // Save  the original TupleValue -
      // used in partition parallelism by the EXCHANGE operator
      s.outputElement.setTupleValue(inputValue);

      s.inputTuple = inputValue;
      
      // Mostly we just need to copy the input to the output
      s.outputTuple = tupleStorageAlloc.allocate();
      populateOutput(s.outputTuple, s.inputTuple);
      
      // Populate the rest of the attributes
      s.outputElement.setTuple(s.outputTuple);
      if (s.inputTuple.getKind() == TupleKind.MINUS)
        s.outputElement.setKind(Kind.E_MINUS);
      else if (s.inputTuple.getKind() == TupleKind.HEARTBEAT)
        s.outputElement.setKind(Kind.E_HEARTBEAT);
      else
        s.outputElement.setKind(Kind.E_PLUS);

      s.outputElement.setTs(inputValue.getTime());

      ((DirectInteropQueue)outputQueue).enqueueConcurrent(s.outputElement);

      s.stats.incrNumOutputs();
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

  @Override
  public MutableState resetMutableState()
  {
    // Always generate a new object
    mut_state = new RelSourceState(execContext);
    return mut_state;
  }

}
