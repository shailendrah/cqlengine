/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/PartitionWindow.java /main/51 2013/11/29 05:16:31 sbishnoi Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares PartitionWindow in package oracle.cep.execution.operators.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 pkali     11/27/13 - propagation of heartbeat when predicate is not statisfied
 pkali     08/19/13 - enable memory optimization only for window size = 1 (bug 17268653)
 udeshmuk  05/15/13 - bug 16721549
 sbishnoi  05/11/13 - bug 16748056
 sbishnoi  05/10/13 - bug 16706956
 sbishnoi  02/20/13 - bug 16082593
 pkali     05/31/12 - added predicate eval logic
 sbishnoi  12/01/11 - support for variable duration partition window
 anasrini  12/19/10 - replace eval() with eval(ec)
 sbishnoi  04/13/09 - adding assertions to verify ordering flag
 hopark    04/09/09 - fix piggybacking
 hopark    05/08/08 - reorg
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 sborah    09/25/08 - update stats
 hopark    02/28/08 - resurrect refcnt
 hopark    12/07/07 - cleanup spill
 hopark    10/30/07 - remove QueueElement
 hopark    10/22/07 - remove TimeStamp
 hopark    10/12/07 - add oot debug util
 parujain  10/04/07 - delete op
 rkomurav  09/24/07 - make code inline
 najain    09/18/07 - fix synchronization problem
 hopark    09/07/07 - eval refactor
 hopark    07/23/07 - fix pin problem
 hopark    07/17/07 - reorg
 hopark    07/13/07 - dump stack trace on exception
 hopark    07/11/07 - remove redundant outputs
 parujain  07/03/07 - cleanup
 parujain  06/26/07 - mutable state
 hopark    05/24/07 - debug logging
 hopark    05/16/07 - remove printStackTrace
 hopark    05/16/07 - add arguments for OutOfOrderException
 parujain  05/08/07 - monitoring statistics
 hopark    05/07/07 - state cleanup
 hopark    04/30/07 - fix bug
 hopark    04/24/07 - fix refcount
 najain    04/09/07 - ref bug fix
 hopark    04/08/07 - fix pincount
 hopark    03/24/07 - add unpin
 najain    03/14/07 - cleanup
 parujain  03/09/07 - extensible window support
 najain    03/12/07 - bug fix
 najain    02/19/07 - bug fix
 najain    01/05/07 - spill over support
 hopark    01/26/07 - remove TimedTuple
 najain    01/25/07 - bug fix
 hopark    12/28/06 - supports range
 hopark    12/13/06 - use timestamp in inserting a tuple
 parujain  12/11/06 - propagating relations
 najain    12/04/06 - stores are not storage allocators
 ayalaman  08/04/06 - partition window impl
 ayalaman  07/29/06 - partition window implementation 
 skaluska  03/14/06 - query manager 
 skaluska  02/06/06 - Creation
 skaluska  02/06/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/PartitionWindow.java /main/51 2013/11/29 05:16:31 sbishnoi Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import java.util.PriorityQueue;
import java.util.logging.Level;

import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.synopses.ExecSynopsis;
import oracle.cep.execution.synopses.PartnWindowSynopsis; 
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError; 
import oracle.cep.execution.internals.windows.RowRangeWindow;
import oracle.cep.execution.internals.windows.Window;

import oracle.cep.execution.stores.PartnWindowStore;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.service.ExecContext;

/**
 * PartitionWindow operator implementation. This operator generates relations for
 * input streams. Based on the partion attribute and window size specification,
 * the plus tuples and minus tuples are queued to the output as new input tuples
 * are procesed.
 *
 * @author ayalaman
 */
public class PartitionWindow extends ExecOpt
{
  /** windowSpec */
  private RowRangeWindow       window;

  /** synopsis storing current window of tuples */
  private PartnWindowSynopsis  partWinSyn; 

  /** evaluation context in which all computations are performed */
  private IEvalContext         evalContext;
  
  /** evaluator to make a copy of the input tuple */
  private IAEval               copyEval;
  
  /** selection predicate */
  private IBEval            predicate;
  
  /** flag to check if the range is specified as a variable expression */
  private boolean              isVariableDurationWindow;
  
  /** a priority queue to maintain the synopsis tuples in an order of their
   *  expiry timestamp */
  private PriorityQueue<ITuplePtr> expiryTimeOrderedElements;
  
  /** column id of tuple where expired timestamp value is saved*/
  private int                  expTsColumn;
  
  /** value of slide component */
  private long                 slideAmount;
  
  /** column id of tuple where element time is stored */
  private int                  elementTimePos;

  /**
   * Constructor for PartitionWindow
   * @param ec
   */
  public PartitionWindow(ExecContext ec)
  {
    super(ExecOptType.EXEC_PARTN_WIN, new PartnWinState(ec), ec);
    window = null;
  }

  /**
   * Getter for Window Specifications
   * 
   * @return Returns the Window Specs
   */
  public Window getWindow()
  {
    return window;
  }
  
  /**
   * Setter for Window Specifications
   * 
   * @param win WindowSpec
   */
  public void setWindow(Window win)
  {
    this.window = (RowRangeWindow) win;
  }
  /**
   * Getter for PartitionWindow evalContext
   * 
   * @return Returns the evalContext
   */
  public IEvalContext getEvalContext() 
  {
    return evalContext;
  }

  /**
   * Setter for PartitionWindow evalContext
   * 
   * @param evalContext The evalContext to set.
   */
  public void setEvalContext(IEvalContext evalContext) 
  {
    this.evalContext = evalContext;
  }

  /**
   * Getter for PartitionWindow synopsis
   * 
   * @return Returns the partition window synopsis
   */
  public ExecSynopsis getSynopsis() 
  {
    return (ExecSynopsis) partWinSyn;
  }

  /**
   * Setter for PartitionWindow synopsis
   * 
   * @param winSynopsis    The synopsis to be set
   *         
   */
  public void setSynopsis(ExecSynopsis  winSynopsis)
  {
    this.partWinSyn = (PartnWindowSynopsis) winSynopsis;
  }

  /**
   * Setter for the tuple copy evaluator
   * 
   * @param  copyEval the evaluator instance 
   */
  public void setCopyEval(IAEval copyEval)
  {
    this.copyEval = copyEval; 
  }


  /**
   * Getter for the tuple copy evaluator
   * 
   * @return  the evaluator instance 
   */
  public IAEval getCopyEval()
  {
    return this.copyEval;
  }

  /**
   * Getter for predicate in Select
   * 
   * @return Returns the predicate
   */
  public IBEval getPredicate()
  {
    return predicate;
  }

  /**
   * Setter for predicate in Select
   * 
   * @param predicate
   *          The predicate to set.
   */
  public void setPredicate(IBEval predicate)
  {
    this.predicate = predicate;
  }
  
  private boolean hasRange()
  {
    return (window != null && 
             (window.getWindowSize().getValue() > 0 ||
              window.isVariableDurationWindow()));
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.operators.ExecOpt#run(int)
   */ 
  public int run(int timeSlice) throws ExecException
  {
    PartnWinState s = (PartnWinState) mut_state;
    boolean      done = false;

    // state should have been initialized 
    assert s.state != ExecState.S_UNINIT;

    // Stats
    s.stats.incrNumExecutions();

    if (s.state == ExecState.S_PROPAGATE_OLD_DATA)
    {
      setExecSynopsis((ExecSynopsis)partWinSyn);
      handlePropOldData();
    } 

    try 
    {
      while ((s.stats.getNumInputs() < timeSlice) && (!done))
      {
        done = false;
        
        // read an input element 
        s.inputElement = inputQueue.dequeue(s.inputElementBuf);
        s.inputReplaced = null;
        s.inputTuple = null;
            
        if (s.inputElement == null)
        {
          done = handleEmpty(s);
        }
        else if (s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
        {
          done = handleHeartbeat(s);
        }
        else
        {
          done = handleTuple(s);
        }
        
        // input element consumed
        if (s.inputTuple != null)
        {
          inTupleStorageAlloc.release(s.inputTuple);
        }
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

  private boolean handleEmpty(PartnWinState s) throws ExecException
  {
      boolean done = true;
      // if the last element in the input queue was a heartbeat,
      // generate a heartbeat in the output queue. The fact that
      // last tuple is a heartbeat is determined by the lastInputTs
      // being more recent than the lastOutputTs. 
      if (s.lastInputTs > s.lastOutputTs)
      {
        // there is no input TS as we are generating the heartbeat 
        // after the input queue becomes empty. But the last inputTs
        // which would be that of a heartbeat element is the outputTs
        // for this heartbeat and we will use that as the last
        // outputTs. 
        s.outputTs = s.lastInputTs;  
        s.outputElement.setTs(s.outputTs);
        s.outputElement.heartBeat(s.lastInputTs);
        // we do not know which partition is going to be expired
        s.outputElement.setTotalOrderingGuarantee(false);
        outputQueue.enqueue(s.outputElement);
        s.stats.incrNumOutputHeartbeats();
        s.lastOutputTs = s.outputTs;
      }
      return done;
  }
  
  private boolean handleHeartbeat(PartnWinState s) throws ExecException
  {
    boolean done = false;
    s.lastInputTs = s.inputElement.getTs();
   
    if (hasRange()) 
    {
      // Bump up our counts
      s.stats.incrNumInputHeartbeats();

      s.inputTs = s.inputElement.getTs();

      // We should have a progress of time.
      if (s.lastInputTs > s.inputTs)
      {
        throw ExecException.OutOfOrderException(
                this,
                s.lastInputTs, 
                s.inputTs, 
                s.inputElement.toString());
      }
      
      assert s.inputTs >= s.minNextTs:
        getDebugInfo(s.inputTs, s.minNextTs,
          s.inputElement.getKind().name(), s.lastInputKind.name());

      // calculate the expected timestamp for next input tuple
      s.minNextTs = s.inputElement.getTotalOrderingGuarantee() ?
                    s.inputTs+1 : s.inputTs;

      // update the last input kind
      s.lastInputKind = s.inputElement.getKind();

      if(this.isVariableDurationWindow)
        done = expireOutOfVariableRange(s);
      else
        done = expireOutOfRange(s);
    } 
  
    /* 
     * Bug 17701008
     * There has been no output and the flag is true for input
     * then output a heartbeat to convey progress of time.
     */
    if((s.lastInputTs > s.lastOutputTs) 
      && (s.inputElement.getTotalOrderingGuarantee()))
    {
      s.outputElement.setKind(QueueElement.Kind.E_HEARTBEAT);
      s.outputElement.heartBeat(s.lastInputTs);
      s.outputElement.setTotalOrderingGuarantee(true);
      if(outputQueue.isFull())
        return true;
      outputQueue.enqueue(s.outputElement);
      s.lastOutputTs = s.lastInputTs;       
    } 
    return done;
  }

  private boolean handleTuple(PartnWinState s) throws ExecException
  {
    boolean done = false;

    // Bump up our counts
    s.stats.incrNumInputs();

    s.inputTuple = s.inputElement.getTuple();
    
    // Get the timestamp
    s.inputTs = s.inputElement.getTs();

    // We should have a progress of time.
    if (s.lastInputTs > s.inputTs)
      throw new ExecException(ExecutionError.OUT_OF_ORDER);

    assert s.inputTs >= s.minNextTs:
      getDebugInfo(s.inputTs, s.minNextTs,
        s.inputElement.getKind().name(), s.lastInputKind.name());

    // calculate the expected timestamp for next input tuple
    s.minNextTs = s.inputElement.getTotalOrderingGuarantee() ?
                  s.inputTs+1 : s.inputTs;

    // update the last input kind
    s.lastInputKind = s.inputElement.getKind();


    // we cannot have minus tuples in the input of a window. 
    assert (s.inputElement.getKind() == QueueElement.Kind.E_PLUS); 

    // Update the last input Ts now
    s.lastInputTs = s.inputTs;
    
    // expire tuples out of range
    if (hasRange()) 
    {
      if(this.isVariableDurationWindow)
        done = expireOutOfVariableRange(s);
      else
        done = expireOutOfRange(s);
      
      if (done) return done;
    }
    
    // expire tuples out of row
    done = expireOutOfRow(s);
    if (done) return done;

    // generate plus
    ITuplePtr inTupleCopy = null;
    if (s.inputReplaced != null)
    {
      // if the input tuple matches with an expiring tuple,
      // use it as the inputCopy.
      inTupleCopy = s.inputReplaced;
    } 
    else 
    {
      // allocate to make a copy of the input tuple
      inTupleCopy = tupleStorageAlloc.allocate();
      evalContext.bind(s.inputTuple, IEvalContext.INPUT_ROLE); 
      evalContext.bind(inTupleCopy, IEvalContext.NEW_OUTPUT_ROLE);

      copyEval.eval(evalContext);  // copy the tuple into outputTuple 
      
      // Validate the tuple after range expression evaluation
      if(isVariableDurationWindow)
        validateTuple(inTupleCopy);
    }
    
    // insert the copy of the tuple into the synopsis. The copy has few
    // additional columns to maintain the data in partitioned windows
    
    evalContext.bind(inTupleCopy, IEvalContext.INPUT_ROLE);
    
    boolean tsorderflag = s.inputElement.getTotalOrderingGuarantee();
    
    //if window size is 1, perform perdicate optimization
    //store the tuples which statisfies the predicate in the window store
    if(window.getWindowRows() == 1)
    {
      if (predicate == null || predicate.eval(evalContext))
      {
        tupleStorageAlloc.addRef(inTupleCopy);
        partWinSyn.insertTuple(inTupleCopy, s.inputTs);
      }
      else //did not satisfied the predicate;
      {
        //propagate the heartbeat
        if(tsorderflag)
          propagateHeartbeat(s,tsorderflag);
        done = true; 
      }
    }
    //if window size > 1 then store all the tuples and 
    //do the predicate evaluation while output the tuple.
    //TODO: Need to extend the optimization for window size > 1
    else 
    {
      tupleStorageAlloc.addRef(inTupleCopy);
      partWinSyn.insertTuple(inTupleCopy, s.inputTs);
    }
    partWinSyn.incrementWindowSize(inTupleCopy);
    
    if (done) return done;
    
    // Add to priority queue to sort tuples according to their expiry timestamp
    if(isVariableDurationWindow)
      expiryTimeOrderedElements.add(inTupleCopy);

    if (s.inputReplaced == null)
    {
      //We do not know which partition is going to expire.
      //However, if the total ordering is guaranteed 
      //(e.g. the next TS is inpTS + 1),
      //the ts of next expiring tuple will be guaranteed to be > inpTS.
                  
      // generate + only if input has not replaced
      //do the predicate evaluation and emit tuple if statisfies the predicate
      if (predicate == null || predicate.eval(evalContext))
        done = outputTuple(s, 
                         QueueElement.Kind.E_PLUS, 
                         s.inputTs, 
                         inTupleCopy,
                         tsorderflag); 
      else if (predicate != null && tsorderflag)
      {
         /*
          * Bug 17612106 and 17644400 : 
          * Predicate was not null but the new plus didn't satisfy it.
          * In such a case, if tsorderflag=true (input elem flag) then
          * we should output hb with flag=true to convey progress of time.
          * This will allow downstream operator to flush out its output at 
          * the same time.
          */
        if(outputQueue.isFull())
          return true;
        else
          propagateHeartbeat(s, tsorderflag);
      }          
    }            
    return done;
  }
   
  private boolean expireOutOfRow(PartnWinState s)  throws ExecException
  {
    boolean done = false;
    int windowSize = partWinSyn.getPartnWindowSize(s.inputTuple); 

    // if partition size is just over the window size by 1 tuple, 
    // delete the oldest tuple in the partition. 
    if (windowSize == window.getWindowRows()) 
    {
      if(partWinSyn.getPartnSize(s.inputTuple) > 0)
      {
        // allocate the output tuple and the timestamp for the 
        // negative tuple 
        ITuplePtr ttuple = partWinSyn.getPartnOldestTuple(s.inputTuple);
        assert ttuple != null;
        ITuplePtr oldestTuple = partWinSyn.deleteOldestTuple(s.inputTuple); 
        assert (oldestTuple != null);
        assert ttuple == oldestTuple;
        
      //Update the priority queue which is maintained in the case when their
      // is a requirement to sort elements according to their expiry timestamp
      if(isVariableDurationWindow)
        expiryTimeOrderedElements.remove(oldestTuple);
      
      // this - has same ts as next + from input tuple
      boolean tsorderflag = false;
      
      //do the predicate evaluation now and emit the oldest tuple only 
      //if statisfies the predicate
      if(predicate != null)
        evalContext.bind(oldestTuple, IEvalContext.INPUT_ROLE); 
      if (predicate == null || predicate.eval(evalContext))
        done = outputTuple(s, 
                         QueueElement.Kind.E_MINUS, 
                         s.inputTs, 
                         oldestTuple,
                         tsorderflag);
      else if (predicate != null && tsorderflag)
      {
        if(outputQueue.isFull())
          return true;
        else
          propagateHeartbeat(s, tsorderflag);
      }
      tupleStorageAlloc.release(oldestTuple);
      }
      partWinSyn.decrementWindowSize(s.inputTuple);
    }
    return done;
  }

  private boolean expireOutOfRange(PartnWinState s)  throws ExecException
  {
    boolean done = false;

    ITuplePtr     lastTuple = null;
    long          lastTimeStamp = 0;
    boolean tsorderflag = false;
    
    while (true)
    {
      s.expiredTuple = null;
      // Find tuple with timestamp <= (inputTs - windowSize)
      s.tempTs.setTime(s.inputTs);
      window.visibleW(s.tempTs, s.visTs);
      if (window.expiredW(s.tempTs, s.expTs))
      {
        if (!partWinSyn.isEmpty())
        {
          // Get the oldest timeStamp
          PartnWindowStore.TimedTuple ttuple = partWinSyn.getOldestTimedTuple();
          if (ttuple != null) 
          {
            long oldestTs = ttuple.timeStamp;
            // Return the oldest tuple if it has a timestamp <= expTs
            // Return the oldest tuple if its expiry time <= current visible time
            if (oldestTs != -1)
            {
              s.tempTs.setTime(oldestTs);
              window.expiredW(s.tempTs, s.expTs);
              if (s.expTs.getTime() <= s.visTs.getTime())
              {
                s.expiredTuple = ttuple.tuple;
                s.expiredTimeStamp = s.expTs.getTime();
              }
            }
            if (s.expiredTuple == null)
            {
              tupleStorageAlloc.release(ttuple.tuple);
            }

          }
        }
      }
      if (lastTuple != null)
      {
        // expiring out-of-row tuples will have s.inputTS
        // so next ts will be inputTs regardless of out-of-row expiring
    	/*
    	 * Bug # 16721549
    	 * The TOF flag setting logic needs changes.
    	 * 1. If expiredTuple is NOT null
    	 *    then if (expiredTupleTs > lastTimestamp)
    	 *    		 set TOF=true
    	 *    	   else 
    	 *    		 set TOF=false
    	 * 2. If expiredTuple is null
    	 *    then 
    	 *       if input is heartbeat set TOF = input TOF
    	 *       else if input is tuple (will be plus only)
    	 *            set TOF = false as we don't know whether this tuple
    	 *            will be output or not.
    	 */
    	tsorderflag = false;
    	if(s.expiredTuple != null)
    	{
    	  if(lastTimeStamp < s.expiredTimeStamp)
    	    tsorderflag = true;
    	  else
    	    tsorderflag = false;
    	}
    	else
    	{
    	  if(s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
    	    tsorderflag = s.inputElement.getTotalOrderingGuarantee();
    	  else
            tsorderflag =false;
    	}
    	//do predicate eval during output tuple, since all the tuples are 
    	//included in the window store
    	if(predicate != null)
          evalContext.bind(lastTuple, IEvalContext.INPUT_ROLE); 
        if (predicate == null || predicate.eval(evalContext))  
          done = outputTuple(s, 
                         QueueElement.Kind.E_MINUS, 
                         lastTimeStamp, 
                         lastTuple,
                         tsorderflag);
      } 
      lastTuple = s.expiredTuple;
      lastTimeStamp = s.expiredTimeStamp;

      if (s.expiredTuple != null)
      {
        tupleStorageAlloc.release(s.expiredTuple);
        ITuplePtr delTuple = partWinSyn.deleteOldestTuple(s.expiredTuple);
        assert(delTuple != null);
        partWinSyn.decrementWindowSize(s.expiredTuple);
      }  
      else
      {
        if (lastTuple != null && predicate != null && 
            !predicate.eval(evalContext) && tsorderflag)
        {
          if(outputQueue.isFull())
            return true;
          else
            propagateHeartbeat(s, tsorderflag);
        }
        // no more tuples to expire
        break;
      }
      if (done) break;
    }
    return done;
  }
  
  /**
   * 
   * @param s
   * @return
   * @throws ExecException
   */
  private boolean expireOutOfVariableRange(PartnWinState s) 
    throws ExecException
  {
    boolean done = false;
    while(true)
    {
      // Get the next tuple from sorted list
      ITuplePtr currentTuple = expiryTimeOrderedElements.peek();
      if(currentTuple == null)
        break;
      // Check if the tuple should be expired
      Long expTs = isTupleExpired(s, currentTuple);
      if(expTs == null)
        break;
      
      // Delete tuple from synopsis
      ITuple o = currentTuple.pinTuple(IPinnable.READ);
      long elementTs = o.longValueGet(this.elementTimePos);
      currentTuple.unpinTuple();
      
      // Update the synopsis
      partWinSyn.deleteTuple(currentTuple, elementTs);
      partWinSyn.decrementWindowSize(currentTuple);
      
      // Update the priority queue
      expiryTimeOrderedElements.remove();
      
      // Calculate total ordering guarantee
      boolean totalOrderingGuarantee = false;
      ITuplePtr nextTuple = expiryTimeOrderedElements.peek();
      if(nextTuple == null || isTupleExpired(s, nextTuple) == null)
      {
        if(s.inputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
        {
          totalOrderingGuarantee = s.inputElement.getTotalOrderingGuarantee();
        }
        else
          totalOrderingGuarantee = false;
      }
      else
        totalOrderingGuarantee = false;
      // Propagate MINUS tuple 
      if(predicate != null)
        evalContext.bind(currentTuple, IEvalContext.INPUT_ROLE); 
      if (predicate == null || predicate.eval(evalContext))
        done = outputTuple(s, 
                         QueueElement.Kind.E_MINUS, 
                         expTs, 
                         currentTuple, 
                         totalOrderingGuarantee);
      else if (predicate != null && totalOrderingGuarantee)
      {
        if(outputQueue.isFull())
          return true;
        else
          propagateHeartbeat(s, totalOrderingGuarantee);
      }
      
     
      // Release tuple storage
      tupleStorageAlloc.release(currentTuple);
    }
    return done;
  }
  
  private void propagateHeartbeat(PartnWinState s, boolean tsorderflag) throws ExecException
  {
    s.outputElement.setKind(QueueElement.Kind.E_HEARTBEAT);
    s.outputElement.heartBeat(s.inputTs);
    s.outputElement.setTotalOrderingGuarantee(tsorderflag);
    if(!outputQueue.isFull())
    {  
      outputQueue.enqueue(s.outputElement);
      s.lastOutputTs = s.inputTs;
    }
  }
    
  /**
   * @param isVariableDurationWindow the isVariableDurationWindow to set
   */
  public void setVariableDurationWindow(boolean isVariableDurationWindow)
  {
    this.isVariableDurationWindow = isVariableDurationWindow;
  }

  private boolean outputTuple(PartnWinState     s,
                              QueueElement.Kind kind, 
                              long              ts, 
                              ITuplePtr         tuple,
                              boolean           tsorderflag)
    throws ExecException
  {
    // check if the minus tuple is same as the input
    if (kind == QueueElement.Kind.E_MINUS &&
        s.inputElement.getKind() == QueueElement.Kind.E_PLUS &&
        s.inputTuple != null &
        s.inputTuple.compare(tuple) )
    {
      // eats up the output Element and replace the input tuple with it
      s.inputReplaced = tuple;
      return false;
    }
    // Calculate the visible time for the given timestamp as in the case of 
    // explicit slide values, the visible output timestamp should be calculate.
    s.tempTs.setTime(ts);
    window.visibleW(s.tempTs, s.visTs);
    s.outputTs = s.visTs.getTime();
    // ordering flag is true only if the visible ts for output ts is same.
    boolean modifiedOrderingFlag = (s.outputTs == ts) ? tsorderflag : false;
    
    s.outputElement.setKind(kind);
    s.outputElement.setTs(s.outputTs); 
    s.outputElement.setTuple(tuple); 
    s.outputElement.setTotalOrderingGuarantee(modifiedOrderingFlag);
    if (outputQueue.isFull())
    {
      //TODO set state and come back to the state from run
      //It's ok for now as queue has infinite size.
      return true;
    }
    
    outputQueue.enqueue(s.outputElement);
    if(s.outputElement.getKind() == QueueElement.Kind.E_HEARTBEAT)
      s.stats.incrNumOutputHeartbeats();
    else
      s.stats.incrNumOutputs();
    s.lastOutputTs = s.outputTs;
    return false;
 }
  
  /**
   * Check if the parameter tuple can be expired or not 
   * @param s
   * @param currentTuple
   * @return the expiry time OR null if the tuple is not expired yet
   * @throws CEPException
   */
  private Long isTupleExpired(PartnWinState s, ITuplePtr currentTuple)
    throws ExecException
  {
    ITuple o = currentTuple.pinTuple(IPinnable.READ);
    
    // Throw Soft Exception if range expression value is null
    if(o.isAttrNull(expTsColumn))
      throw new SoftExecException(
        ExecutionError.INVALID_TIMESTAMP_COLUMN_VALUE);
        
    Long expiryTs = null;
    if(slideAmount > 1)
    {
      long elementTs = o.longValueGet(elementTimePos);
      long rangeVal = o.longValueGet(expTsColumn) - elementTs;
      // The element will be expired at timestamp elementTs + rangeVal
      // The expired element will be visilbe at the next slide.
      expiryTs = getVisibleTs(elementTs + rangeVal);
    }
    else
    {
      expiryTs = o.longValueGet(expTsColumn);
    }
    
    if(expiryTs > s.inputTs)
    {
      currentTuple.unpinTuple();
      return null;
    }
    else
    {
      currentTuple.unpinTuple();
      return expiryTs;
    }
  }

  /**
   * Helper method to calculate visible timestamp
   * @param expTs
   * @return
   */
  private long getVisibleTs(long expTs)
  {
    if(slideAmount > 1)
    {
      long t = expTs / slideAmount;
      if((expTs % slideAmount) == 0)
        return(t*slideAmount);
      else
        return((t+1)*slideAmount);
    }
    else
    {
      return expTs;
    }
    
  }

  /**
   * @param expiryTimeOrderedElements the expiryTimeOrderedElements to set
   */
  public void setExpiryTimeOrderedElements(
      PriorityQueue<ITuplePtr> expiryTimeOrderedElements)
  {
    this.expiryTimeOrderedElements = expiryTimeOrderedElements;
  }
  
  /**
   * Do the following checks on given tuple
   * 1) Range should not be a negative value
   * 2) Slide should not be greater than range
   * @param outputTuple
   */
  private void validateTuple(ITuplePtr currentTuple) throws ExecException
  {
    ITuple o = currentTuple.pinTuple(IPinnable.READ);
    long expTs     = o.longValueGet(this.expTsColumn);
    long elementTs = o.longValueGet(this.elementTimePos);
    long rangeVal  = expTs - elementTs;
    
    // Check-1 (For Negative Range Values)    
    if(rangeVal < 0)
    {
      throw new ExecException(ExecutionError.NEGATIVE_RANGE_VALUE, 
                             new Object[]{rangeVal});
    }
    
    // Check-2 (For Range-Slide Values)
    if(slideAmount > rangeVal)
    {
      throw new ExecException(ExecutionError.SLIDE_GREATER_THAN_RANGE,
                             new Object[]{slideAmount, rangeVal});
    }
    
    currentTuple.unpinTuple();
  }

  /**
   * @param expTsColumn the expTsColumn to set
   */
  public void setExpTsColumn(int expTsColumn)
  {
    this.expTsColumn = expTsColumn;
  }

  /**
   * @param slideAmount the slideAmount to set
   */
  public void setSlideAmount(long slideAmount)
  {
    this.slideAmount = slideAmount;
  }

  /**
   * @param elementTimePos the elementTimePos to set
   */
  public void setElementTimePos(int elementTimePos)
  {
    this.elementTimePos = elementTimePos;
  }

  /* (non-Javadoc)
   * @see oracle.cep.execution.operators.ExecOpt#deleteOp()
   */
  @Override
  public void deleteOp()
  {
  }

  /**
   * Get the string representation for the operator 
   */
  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<PartitionWindow id=\"" + id + "\" >");
    sb.append("<InputQueue id=\"" + inputQueue.getId() + "\"/>");
    sb.append("<OutputQueue>" + outputQueue.toString() + "</OutputQueue>");
    sb.append("<Synopsis>");
    sb.append(partWinSyn.toString());
    sb.append("</Synopsis>");
    sb.append("<OutTupleAlloc>" + tupleStorageAlloc.toString()
        + "</OutTupleAlloc>");
    sb.append("<CopyEval>");
    sb.append(copyEval.toString());
    sb.append("</CopyEval>");
    sb.append("</PartitionWindow>");

    return sb.toString();
  }
}
