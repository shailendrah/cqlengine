/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/XmlTable.java /main/10 2011/02/07 03:36:26 sborah Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
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
    hopark      06/02/09 - fix RefCount issue
    hopark      05/21/09 - fix tsorder assertion
    sborah      04/13/09 - assertion check
    hopark      04/06/09 - total ordering opt
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    hopark      03/05/08 - xml spill
    hopark      02/28/08 - resurrect refcnt
    najain      02/04/08 - 
    hopark      12/28/07 - spill cleanup
    mthatte     12/26/07 - 
    najain      12/13/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/XmlTable.java st_pcbpel_anasrini_eval_parallelism_2/1 2010/12/19 07:35:40 anasrini Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.util.logging.Level;

import java.lang.Exception;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.xml.XMLItem;
import oracle.cep.execution.xml.XMLSequence;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.service.ExecContext;

public class XmlTable extends ExecOpt
{
  IEvalContext evalContext;
  IAEval       xmlTableEval;
  int          resultSetCol;
  
  /** roles for evaluation contex */
  private static final int INPUT_ROLE  = IEvalContext.INPUT_ROLE;

  private static final int OUTPUT_ROLE = IEvalContext.NEW_OUTPUT_ROLE;

  /**
   * Getter for evalContext in XmlTable
   * 
   * @return Returns the evalContext
   */
  public IEvalContext getEvalContext()
  {
    return evalContext;
  }

  /**
   * Setter for evalContext in XmlTable
   * 
   * @param evalContext
   *          The evalContext to set.
   */
  public void setEvalContext(IEvalContext evalContext)
  {
    this.evalContext = evalContext;
  }

  /**
   * Getter for xmlTableEval in XmlTable
   * 
   * @return Returns the xmlTableEval
   */
  public IAEval getXmlTableEval()
  {
    return xmlTableEval;
  }

  /**
   * Setter for xmlTableEval in XmlTable
   * 
   * @param xmlTableEval
   *          The xmlTableEval to set.
   */
  public void setXmlTableEval(IAEval xmlTableEval)
  {
    this.xmlTableEval = xmlTableEval;
  }

  public int getResultSetCol()
  {
    return resultSetCol;
  }

  public void setResultSetCol(int resultSetCol)
  {
    this.resultSetCol = resultSetCol;
  }

  public XmlTable(ExecContext ec)
  {
    super(ExecOptType.EXEC_XMLTABLE, new XmlTableState(ec), ec);  
  }
  
  public int run(int timeSlice) throws CEPException
  {
    int numElements;
    boolean done = false;
    XmlTableState s = (XmlTableState) mut_state;
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
              if (s.lastInputTs <= s.lastOutputTs)
              {
                s.state = ExecState.S_INIT;
                done = true;
                break;
              }

              // Output a heartbeat
              s.resultSetTupleP = null;
              s.outputKind = QueueElement.Kind.E_HEARTBEAT;
              s.state = ExecState.S_OUTPUT_READY;
              break;
            }
            else
            {
              // Update our counts
              if(s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
                s.stats.incrNumInputHeartbeats();
              else
                s.stats.incrNumInputs();

              s.inputTuple = s.inputElement.getTuple();
              
              // Update last input ts
              s.inputTs = s.inputElement.getTs();

              // We should have a progress of time.
              if (s.lastInputTs > s.inputTs)
                throw ExecException.OutOfOrderException(
                  this, s.lastInputTs, s.inputTs, s.inputElement.toString());

              // ensure that the time stamp value is as per the OrderingFlag 
              assert s.inputTs >= s.minNextTs :
                getDebugInfo(s.inputTs, s.minNextTs, 
                             s.inputElement.getKind().name(),
                             s.lastInputKind.name());
              
              // Update the last input Ts now
              s.lastInputTs = s.inputTs;
              s.lastInputKind = s.inputElement.getKind();
              
              // calculate the expected timeStamp for next input tuple
              s.minNextTs = s.inputElement.getTotalOrderingGuarantee() ? 
                            s.inputTs+1 : s.inputTs;
              
              // Update the last input Ts now
              s.lastInputTs = s.inputTs;
	            exitState = false;

              // Nothing more to be done for heartbeats
              if (s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
              {
                s.state = ExecState.S_INPUT_ELEM_CONSUMED;
                break;
              }
              else
              {
                s.outputKind = s.inputElement.getKind();
                assert s.inputElement.getKind() == QueueElement.Kind.E_PLUS;
		s.state = ExecState.S_PROCESSING1;
              }
            }
          case S_PROCESSING1:
            // Allocate an result set tuple
            s.resultSetTupleP = tupleStorageAlloc.allocate();
            s.state = ExecState.S_PROCESSING2;
          case S_PROCESSING2:
            // Compute the output tuple
            evalContext.bind(s.inputTuple, INPUT_ROLE);
            evalContext.bind(s.resultSetTupleP, OUTPUT_ROLE);
            xmlTableEval.eval(evalContext);
	    s.state = ExecState.S_PROCESSING3;
	  case S_PROCESSING3:
	    ITuple resultSetTuple = s.resultSetTupleP.pinTuple(IPinnable.READ);
            s.res = (XMLSequence) resultSetTuple.oValueGet(resultSetCol);
            s.hasNext = s.res.next();
	    s.state = ExecState.S_PROCESSING4;

  	  case S_PROCESSING4:
	    if (!s.hasNext)
	    {
	      s.state = ExecState.S_INPUT_ELEM_CONSUMED;
	      s.hasNext = false;
	      s.res = null;
	      break;
	    }
	    else
	    {
	      try 
	      {
	        XMLItem resItem = s.res.getItem();
		s.outputTupleP = tupleStorageAlloc.allocate();
		ITuple outputTuple = s.outputTupleP.pinTuple(IPinnable.WRITE);
	        outputTuple.xValueSet(0, (Object)resItem);
		s.outputTupleP.unpinTuple();
	        s.state = ExecState.S_OUTPUT_READY;
	      }
	      catch (Exception x)
	      {
	        s.state = ExecState.S_INPUT_ELEM_CONSUMED;
		s.outputTupleP = null;
                s.hasNext = false;
	        s.res = null;
	        break;
	      }
	    }
          case S_OUTPUT_READY:
            if (s.outputKind == QueueElement.Kind.E_HEARTBEAT)
            {
              assert s.inputElement == null;
              s.lastOutputTs = s.lastInputTs;
              s.outputElement.heartBeat(s.lastInputTs);
            }
            else
            {
              s.outputElement.setTuple(s.outputTupleP);
              s.outputElement.setTs(s.inputTs);
              s.outputElement.setKind(s.inputElement.getKind());
              s.hasNext = (s.res == null ? false : s.res.next());
              s.outputElement.setTotalOrderingGuarantee(s.hasNext ?
                  false :
                  s.inputElement.getTotalOrderingGuarantee());
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
	      s.state = ExecState.S_PROCESSING4;
	    else
	      s.state = ExecState.S_INIT;
	    break;
  	  case S_INPUT_ELEM_CONSUMED:
	    assert s.inputElement != null;

	    if (s.inputTuple != null)
	    {
	      inTupleStorageAlloc.release(s.inputTuple);
              s.inputTuple = null;
	    }

	    if (s.resultSetTupleP != null)
	    {
	      s.resultSetTupleP.unpinTuple();
	      tupleStorageAlloc.release(s.resultSetTupleP);
	      s.resultSetTupleP = null;
	    }

	    exitState = true;
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

  public void deleteOp()
  {
  }
}
