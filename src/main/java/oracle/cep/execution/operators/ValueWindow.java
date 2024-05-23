/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ValueWindow.java /main/9 2012/01/20 11:47:14 sbishnoi Exp $ */

/* Copyright (c) 2008, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    01/13/12 - modified timestamp unit to nanoseconds
    sbishnoi    09/07/11 - support for curernt hour and current period value
                           window
    sborah      04/13/09 - assertion check
    parujain    02/04/09 - heartbeat bug
    udeshmuk    01/22/09 - total ts ordering optimization
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    sborah      09/25/08 - update stats
    parujain    07/07/08 - value based windows
    parujain    07/07/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/ValueWindow.java /main/7 2009/04/16 07:38:31 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.operators;

import oracle.cep.common.Datatype;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.synopses.ExecSynopsis;
import oracle.cep.execution.synopses.WindowSynopsis;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.service.ExecContext;

/**
 * ValueWindow
 *
 * @author parujain
 */
public class ValueWindow extends ExecOpt
{
  /** flag true if long else double */
  private boolean        isWindowLong;
  
  /** position */
  private int            position;

  /** winSynopsis */
  private WindowSynopsis  winSynopsis;
  
  /** value window */
  private oracle.cep.execution.internals.windows.ValueWindow    window;
  
  /** datatype of the comparing column */
  private Datatype      colType;

  /**
   * Constructor
   * @param ec TODO
   */
  public ValueWindow(ExecContext ec)
  {
    super(ExecOptType.EXEC_VALUE_WIN, new ValueWindowState(ec), ec);
  }
 
  
  /**
   * Get the position of window attribute
   * @return
   *        Postion of attribute
   */
  public int getPosition()
  {
    return this.position;
  }
  
  /**
   * Sets the position of window attribute
   * @param pos
   *         Position of attribute
   */
  public void setPosition(int pos)
  {
    this.position = pos;
  }

  /**
   * Getter for winSynopsis
   * 
   * @return Returns the winSynopsis
   */
  public WindowSynopsis getWinSynopsis()
  {
    return winSynopsis;
  }

  /**
   * Setter for winSynopsis
   * 
   * @param winSynopsis
   *          The winSynopsis to set.
   */
  public void setWinSynopsis(WindowSynopsis winSynopsis)
  {
    this.winSynopsis = winSynopsis;
  }
  
  /**
   * @param window the window to set
   */
  public void setWindow(
    oracle.cep.execution.internals.windows.ValueWindow window)
  {
    this.window = window;
  }
  
 
  private boolean isAnyTupleExpired(ValueWindowState s) throws CEPException
  {
    ITuple o = winSynopsis.getOldestTuple().pinTuple(IPinnable.READ);
    
	  if(isWindowLong)
	  {
	    long oldLongAttrVal = o.longValueGet(position);
	       
	    if(window.expiredW(oldLongAttrVal))
	    {
	      winSynopsis.getOldestTuple().unpinTuple();
        s.inputTuple.unpinTuple();
        return true;
	    }	    
	  }
	  else
	  {
	    double oldDoubleAttrVal = o.dblValueGet(position);
	    if(window.expiredW(oldDoubleAttrVal))
	    {
	      winSynopsis.getOldestTuple().unpinTuple();
        s.inputTuple.unpinTuple();
        return true;
	    }
	  }
    winSynopsis.getOldestTuple().unpinTuple();
    s.inputTuple.unpinTuple();
    return false;
  }
  
  private void setBaseValue(ValueWindowState s) throws CEPException
  {
    if(s.inputKind != QueueElement.Kind.E_PLUS)
      return;
    
    ITuple inpTuple = s.inputTuple.pinTuple(IPinnable.READ);
    
    if(isWindowLong)
    {
      long currentElementLongValue = inpTuple.longValueGet(position);
           
      window.setBaseValue(currentElementLongValue);
    }
    else 
    {
      double currentElementDoubleValue = inpTuple.dblValueGet(position);
      window.setBaseValue(currentElementDoubleValue);
    }
    s.inputTuple.unpinTuple();
  }
    
    

  /**
   * @param isWindowLong the isWindowLong to set
   */
  public void setWindowLong(boolean isWindowLong)
  {
    this.isWindowLong = isWindowLong;
  }

  /**
   * @param colType the colType to set
   */
  public void setColType(Datatype colType)
  {
    this.colType = colType;
  }


  /*
   * @see oracle.cep.execution.operators.ExecOpt#run(int)
   */
  public int run(int timeSlice) throws CEPException
  { 
    int numElements = timeSlice;
    boolean done = false;
    ValueWindowState s = (ValueWindowState)mut_state;
    boolean exitState = true;

    assert s.state != ExecState.S_UNINIT;

    // Stats
    s.stats.incrNumExecutions();

    try
    {
      while ((s.stats.getNumInputs() < numElements) || (!exitState))
      {
        switch (s.state)
        {
          case S_PROPAGATE_OLD_DATA:
            setExecSynopsis((ExecSynopsis)winSynopsis);
            handlePropOldData();
            break;
            
          case S_INIT:
            // Get next input element
            // Inputs: none
            // Outputs: inputElement

            s.inputElement = inputQueue.dequeue(s.inputElementBuf);
            s.state = ExecState.S_INPUT_DEQUEUED;
          case S_INPUT_DEQUEUED:
            // Sanity checks, initialize variables based on input element
            // Inputs: inputElement, lastInputTs
            // Outputs: inputKind, inputTs, inputTuple, lastInputTs
            if (s.inputElement == null)
            {
              if (s.lastInputTs <= s.lastOutputTs)
              {
                s.state = ExecState.S_INIT;
                done = true;
                break;
              }

              // Output a heartbeat
              s.state = ExecState.S_GENERATE_HEARTBEAT;
              exitState = false;
              break;
            }
            else
            {
              exitState = false;
              // Bump our counts
              if(s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
                s.stats.incrNumInputHeartbeats();
              else
                s.stats.incrNumInputs();

              s.inputKind = s.inputElement.getKind();
              s.inputTs = s.inputElement.getTs();
              // We should not get MINUS tuples in the input
              assert s.inputKind != QueueElement.Kind.E_MINUS : s.inputElement;
              // Input should be timestamp ordered
              if (s.lastInputTs > (s.inputTs))
              {
                s.state = ExecState.S_INIT;
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
              
              s.lastInputTs           = s.inputTs;
              s.lastInputOrderingFlag = s.inputElement.getTotalOrderingGuarantee();
              s.minNextTs             = s.lastInputOrderingFlag ? 
                                        s.inputTs+1 : s.inputTs;
              s.lastInputKind         = s.inputElement.getKind(); 

              s.inputTuple = s.inputElement.getTuple();
              if (s.inputKind == QueueElement.Kind.E_HEARTBEAT)
              {
                // Nothing more to be done
                assert s.inputTuple == null;
                s.state = ExecState.S_INPUT_ELEM_CONSUMED;
                break;
              }
              ITuple o = s.inputTuple.pinTuple(IPinnable.READ);
              
              // compare the value and throw exception if not non-decreasing
              if(isWindowLong)
              {
                long tmpval = o.longValueGet(position);
      
                if(s.lastLColVal > tmpval)
                {
                  s.state = ExecState.S_INIT;
                  s.inputTuple.unpinTuple();
                  
                  throw ExecException.ValueOutOfOrderException(
                          this,
                          s.lastLColVal, 
                          tmpval, 
                          s.inputElement.toString());
                }
                s.lastLColVal = tmpval;
              }
              else
              {
                double tmpval = o.dblValueGet(position);
              	if(s.lastDColVal > tmpval)
            	  {
                  s.state = ExecState.S_INIT;
                  s.inputTuple.unpinTuple();
                  
                  throw ExecException.ValueOutOfOrderException(
                          this,
                          s.lastDColVal, 
                          tmpval, 
                          s.inputElement.toString());
              	}
                
                s.lastDColVal = o.dblValueGet(position);
              }
              s.inputTuple.unpinTuple();
            }
            
            s.state = ExecState.S_PROCESSING1;
          case S_PROCESSING1:
            // Increment ref count of input tuple
            // Inputs: inputTuple
            // Outputs: inputTuple

            assert s.inputElement != null;

            if (s.inputKind == QueueElement.Kind.E_PLUS)
            {
              setBaseValue(s);
              assert s.inputTuple != null;
            }
            s.state = ExecState.S_PROCESSING2;
          case S_PROCESSING2:
            // Determine tuple to be expired
            // Inputs: inputKind, inputTs
            // Outputs: expiredTuple

            if(winSynopsis.isEmpty())
              s.expiredTuple = null;
            else if(isAnyTupleExpired(s))
              s.expiredTuple = winSynopsis.getOldestTuple();
            else
              s.expiredTuple = null;
        	
            if (s.inputKind == QueueElement.Kind.E_HEARTBEAT)
            {
              if (s.expiredTuple == null)
              {
                // Nothing more to be done
                s.state = ExecState.S_INPUT_ELEM_CONSUMED;
                continue;
              }
              else
              {
                s.inputTuple = null;
              }
            }
            
            // If the current tuple is not visible then, nothing to do
            if(s.expiredTuple == null && s.inputTuple != null)
            {
              ITuple inpTuple = s.inputTuple.pinTuple(IPinnable.READ);
              boolean isVisible =true;
              if(isWindowLong)
              {
                long currentElementLongValue = inpTuple.longValueGet(position);  
                isVisible = window.visibleW(currentElementLongValue);
              }
              else 
              {
                double currentElementDoubleValue = inpTuple.dblValueGet(position);
                isVisible = window.visibleW(currentElementDoubleValue);
              }
              s.inputTuple.unpinTuple();
              if(!isVisible)
              {
                // Nothing more to be done
                s.state = ExecState.S_INPUT_ELEM_CONSUMED;
                continue;
              }
            }
              
            s.state = ExecState.S_OUTPUT_TIMESTAMP;
           case S_OUTPUT_TIMESTAMP:
            if(s.expiredTuple == null)
              s.outputTs = s.inputTs;
            s.state = ExecState.S_OUTPUT_READY;
          case S_OUTPUT_READY:
            // Allocate queue element for output
            // Inputs: inputTuple, inputTs, expiredTuple
            // Outputs: outputElement
            assert ((s.inputTuple != null) || (s.expiredTuple != null));

            if(s.expiredTuple == null)
              s.outputElement.copy(s.inputElement);
            s.state = ExecState.S_PROCESSING4;
          case S_PROCESSING4:
            if (s.expiredTuple != null)
            {
              s.outputElement.setKind(QueueElement.Kind.E_MINUS);
              s.outputTs = s.inputTs;
              s.outputElement.setTs(s.outputTs);
              s.outputElement.setTuple(s.expiredTuple);
              //Since all expired tuples are output at the same ts flag is false
              //For the last expired tuple, it will be followed by the current input tuple
              //so again the flag is false
              s.outputElement.setTotalOrderingGuarantee(false);
              s.lastOutputTs = s.outputTs;
            }
            else
            {
              // Add input tuple to the window
              // Inputs: inputTuple
              // Outputs: inputTuple

              assert (s.inputKind == QueueElement.Kind.E_PLUS) : s.inputKind;

              assert s.outputElement.equals(s.inputElement);
              //copy input flag as is
              s.outputElement.setTotalOrderingGuarantee(
                              s.inputElement.getTotalOrderingGuarantee());
              winSynopsis.insertTuple(s.inputTuple, s.inputTs);
              s.lastOutputTs = s.outputTs;
            }
            s.state = ExecState.S_OUTPUT_ELEMENT;
          case S_OUTPUT_ELEMENT:
            // Enqueue output element
            // Inputs: outputElement
            // Outputs: none

            if (outputQueue.isFull())
            {
              done = true;
              break;
            }
            outputQueue.enqueue(s.outputElement);
            s.state = ExecState.S_OUTPUT_ENQUEUED;
          case S_OUTPUT_ENQUEUED:
            // Determine next state
            // Inputs: outputElement, expiredTuple
            // Outputs: none
            if(s.outputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
              s.stats.incrNumOutputHeartbeats();
            else
              s.stats.incrNumOutputs();

            if (s.expiredTuple != null)
            {
              // It is now safe to delete the oldest tuple
              winSynopsis.deleteOldestTuple();
              // Look for more expired elements
              s.state = ExecState.S_PROCESSING2;
              break;
            }
            else
            {
              // The plus corresponding to the input tuple is output last
              s.state = ExecState.S_INPUT_ELEM_CONSUMED;
            }
          case S_INPUT_ELEM_CONSUMED:
            assert s.inputElement != null;
            assert s.expiredTuple == null;

            s.state = ExecState.S_INIT;
            exitState = true;
            break;

          case S_GENERATE_HEARTBEAT:
            s.state = ExecState.S_PROCESSING5;

          case S_PROCESSING5:
            s.lastOutputTs = s.lastInputTs;
            s.outputElement.heartBeat(s.lastInputTs);
            //send the flag of last input which must be a heartbeat
            s.outputElement.setTotalOrderingGuarantee(s.lastInputOrderingFlag);
            s.state = ExecState.S_PROCESSING6;

          case S_PROCESSING6:
            if (outputQueue.isFull())
            {
              done = true;
              break;
            }
            outputQueue.enqueue(s.outputElement);
            s.stats.incrNumOutputHeartbeats();
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
    catch (SoftExecException e)
    {
      // Ignore it for now
      return 0;
    }

    return 0;
  }

  /*
   * @see oracle.cep.execution.operators.ExecOpt#deleteOp()
   */

  public void deleteOp()
  {
    // TODO

  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<ValueWindow id=\"" + id + "\" winSize=\"" + "\" >");
    sb.append("<InputQueue>" + inputQueue.toString() + "</InputQueue>");
    sb.append("<OutputQueue>" + outputQueue.toString() + "</OutputQueue>");
    sb.append("<Synopsis>" + winSynopsis.toString() + "</Synopsis>");
    sb.append("</ValueWindow>");

    return sb.toString();
  }

}

