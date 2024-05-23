/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/input/QueueSource.java /main/30 2015/02/06 15:09:31 sbishnoi Exp $ */

/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      03/09/16 - add execueDML with iterable
 udeshmuk    07/23/13 - bug 16813624: use param useMillisTs in hbtOutRemainder
 udeshmuk    05/21/13 - bug 16820093 - use the new run method for ordered case
                        in putnext
 sbishnoi    04/07/13 - XbranchMerge sbishnoi_bug-15962405_ps6 from
                        st_pcbpel_11.1.1.4.0
 sbishnoi    07/02/12 - XbranchMerge sbishnoi_bug-14254042_ps6 from
                        st_pcbpel_11.1.1.4.0
 sbishnoi    07/02/12 - bug 14254042
 alealves    08/18/11 - XbranchMerge alealves_bug-12888416_cep from main
 anasrini    08/12/11 - use nanotime for unordered
 sbishnoi    09/19/10 - XbranchMerge sbishnoi_bug-10068411_ps3 from
                        st_pcbpel_11.1.1.4.0
 sbishnoi    09/02/10 - support for input batching
 sbishnoi    05/11/09 - commenting the println statement
 anasrini    05/08/09 - system timestamped source lineage
 anasrini    02/13/09 - override supportsPushEmulation
 hopark      12/04/08 - add toString
 hopark      10/09/08 - remove statics
 hopark      04/16/08 - add stat
 hopark      03/12/08 - use IQueue
 udeshmuk    01/17/08 - change in the data type of time field in TupleValue.
 udeshmuk    12/17/07 - add getHeartbeatTime method.
 udeshmuk    11/29/07 - send isSystemtimestamped as input argument while
                        converting from xml to tuple.
 udeshmuk    11/22/07 - handle systemtimestamped.
 parujain    09/25/07 - epr for push source
 parujain    05/24/07 - softexecexceptions
 najain      03/12/07 - bug fix
 najain      11/07/06 - add getOldestTs
 anasrini    09/13/06 - extend TableSourceBase
 anasrini    09/11/06 - move the XML conversion routine to TupleValueHelper
 najain      08/21/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/input/QueueSource.java /main/30 2015/02/06 15:09:31 sbishnoi Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.interfaces.input;

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.dataStructures.external.TupleKind;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.ExecOptTask;
import oracle.cep.execution.queues.IQueue;
import oracle.cep.execution.queues.IQueue.QueueStats;
import oracle.cep.interfaces.InterfaceException;
import oracle.cep.interfaces.TupleValueHelper;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.ExecContext;

/**
 * QueueSource reads tuples from a queue and returns them.
 * 
 * @author najain
 */
public class QueueSource extends TableSourceBase
{  
  /** queue */
  private IQueue<TupleValue>     queue;

  /** To track null attributes */
  private BitSet                 attrsList;

  /** Temp flag */
  private boolean                directInterop;

  /** The current event - used only with direct interop scheduler */
  private TupleValue             currentEvent;

  /** For direct invocation easy ref to execution task */
  private ExecOptTask            orderedExecOpTask;
  private ExecOptTask            unorderedExecOpTask;

  /** Has this queue source been closed */
  private AtomicBoolean          isClosed;

  /** The name of the exec operator for which this is source */
  private String                 opName;

  /** 
   * Object to serialize invocatios to the execution operator.
   * In the directInterop mode, the client thread and the TIMER thread
   * could concurrently attempt to run the operator, this is used to
   * serialize them
   */
  private Object                 opRun;
  
  /**
   * A counter of number of pending inputs waiting to process.
   * In case of DI Scheduler, the pending input count depends on number of 
   * parallel running threads. 
   * Each thread contribute a value 1 in the count.
   * So for a single threaded run, pendingInput count will be either 0 or 1.
   */
  private AtomicLong             pendingInputs;
  
  private AtomicLong             lastInputTs;

  /** Unprocessed tuple of currently processed batch.
   *  Usage: In case of a fault while processing a batch of events,
   *  Queue source of query resets and remaining events of batch
   *  should flow to new queue source.
   *  To ensure that we don't miss any tuple, we preserve the last
   *  tuple which was remained unprocessed due to STALE queue source.
   */
  private TupleValue             pendingData;


  /**
   * Constructor
   * @param ec execution context for this service
   * @param innerSource the underlying pull source that requires push
   *                    emulation
   */
  public QueueSource(ExecContext ec, TableSource innerSource)
  {
    super(ec, innerSource);
    if (innerSource != null)
      isSystemTimeStamped = innerSource.isSystemTimeStamped();

    directInterop = ec.getServiceManager().getConfigMgr().getDirectInterop();
    currentEvent  = null;
    isClosed      = new AtomicBoolean(false);
    opRun         = new Object();
    pendingInputs = new AtomicLong();
    lastInputTs   = new AtomicLong();
    pendingData   = null;
  }

  /**
   * @param ec execution context for this service
   * @param numAttrs
   *          number of Attributes
   * @param dty
   *          datatype of the Attributes
   * @param names
   *          names of the Attributes
   * @param queue
   *          queue instance
   */
  public QueueSource(ExecContext ec, int numAttrs, Datatype[] dty,
                     String[] names, IQueue<TupleValue> queue)
  {
    super(ec);
    setNumAttrs(numAttrs);
    for (int i=0; i<numAttrs; i++) {
      setAttrInfo(i, names[i], new AttributeMetadata(dty[i], 0, dty[i].getPrecision(), 0));
    }

    this.queue     = queue;
    this.attrsList = new BitSet(numAttrs);

    directInterop = ec.getServiceManager().getConfigMgr().getDirectInterop();
    currentEvent  = null;
    isClosed      = new AtomicBoolean(false);
    opRun         = new Object();
    pendingInputs = new AtomicLong();
    lastInputTs   = new AtomicLong();
    pendingData   = null;
  }

  public synchronized void start() throws CEPException
  {
    super.start();
    
    if (orderedExecOp != null)
      opName = orderedExecOp.getOptName();
    else
      opName = unorderedExecOp.getOptName();
    
    if(innerSource != null)
      innerSource.start();

    // Reset this, since we may be re-starting the source
    isClosed.set(false);
  }
  
  public synchronized void end() throws CEPException 
  {
    super.end();
    if(innerSource != null)
      innerSource.end();
    isClosed.set(true);
  }

  /**
   * This method is called only during REGRESSION testing.
   * This is used to emulate push mode even though tests use pull mode
   * sources like files.
   */  
  public void run()
  {
    if(innerSource != null)
    {
      boolean    flag = true;
      TupleValue t    = null;

      try
      {
        while (flag)
        {
          try
          {
            synchronized(this)
            {
              if (!isClosed.get())
                t = innerSource.getNext();
              else
                t = null;
            }
            if (t != null)
              this.putNext(t);
            else
              flag = false;
          }
          catch(InterfaceException e)
          {
            //LogUtil.info(LoggerType.TRACE, e);
            LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
          }
        } 
      }
      catch (CEPException e)
      {
        e.printStackTrace();
      }

      LogUtil.info(LoggerType.TRACE, "End of run for thread : " + opName);
    }
  }
  
  public void setExecOp(ExecOpt op) 
  {
    super.setExecOp(op);
    orderedExecOpTask = op.getExecOpTask();
  }
  
  public void setUnorderedExecOp(ExecOpt op)
  {
    super.setUnorderedExecOp(op);
    unorderedExecOpTask = op.getExecOpTask();
  }
  
  public void putNext(TupleValue data) throws CEPException
  {
    putNext(data, true);
  }

  public void putNext(Iterator<TupleValue> dataBatch) throws CEPException
  {
    putNext(dataBatch, true);
  }

  public void putNext(Iterator<TupleValue> batchIter, boolean overrideTime) throws CEPException
  {
    assert directInterop : 
      "QueueSource.putNext(Collection<TupleValue>) should be called" + 
      " only in directInterop mode";

    synchronized(opRun)
    {
      long batchTimestamp = Constants.NULL_TIMESTAMP;
      TupleValue data     = null;
      if(isSystemTimeStamped && overrideTime)
      {
        batchTimestamp = getCorrectSystemTime();
        while(batchIter.hasNext())
        {
          data = batchIter.next();
          assert data != null;
          data.setTime(batchTimestamp);
          // Override the total ordering gurantee flag and set the flag to true
          // for last tuple of the batch
          if(batchIter.hasNext())
            data.setTotalOrderGuarantee(false);
          else
            data.setTotalOrderGuarantee(true);
 
          putNext(data, false, true);
        }     
      }
      else
      {
        //Note:In case of application timestamped, CQLEventReceiver has already
        // calculated and set the timestamp value in the TupleValue object
        // So we will not set the timestamp.
        // Also keep the total ordering guarantee flag same as set by upstream
        // CQLEventReceiver
        //int count=0;
        if(pendingData != null)
        {
          LogUtil.info(LoggerType.TRACE, "QueueSource is processing pending tuple "+
                       pendingData.toSimpleString());
          putNext(pendingData,false,true); 
          pendingData = null;
        }
        while(batchIter.hasNext())
        {
          data = batchIter.next();
          assert data != null;
          try 
          {
            putNext(data, false, true);
          }
          catch(CEPException e)
          {
            // In case of a stale queue, Set the current event as pendingData
            // So after resetting the queue source, we will process this tuple
            // first before remaining tuple in the iterator.
            if(e.getErrorCode() == InterfaceError.STALE_QUEUE_SOURCE)
            {
              LogUtil.info(LoggerType.TRACE, 
                "Stale Queue Source can't process:" + data.toSimpleString());
              pendingData = data;
            }
            throw e;
          }
        }
      } 
    }
  }

  public void putNext(TupleValue data, boolean overrideTime) 
    throws CEPException
  {
    putNext(data, overrideTime, false);
  }

  /**
   * Executes the operator
   * @param data input data
   * @param overrideTime flag to check if we need to set the timestamp of data
   * @param isBatchTuple flag to check if we need to set the ordering flag; In
   * the case of input batch, the total order guarantee flag will carry over;
   * CQLEventReciever will send the batch with correct total ordering guarantee
   */
  private void putNext(TupleValue data, 
                       boolean overrideTime, 
                       boolean isBatchTuple) 
    throws CEPException
  {
            
    if (isClosed.get())
    {
      LogUtil.info(LoggerType.TRACE, "QueueSource is closed. Can't process tuple:"
                   + data.toSimpleString());
      // QueueSource gets closed when the query stops (gracefully or not fault).
      // In case of fault, we need to redirect remaining batch events to a new
      // queue source of restarted query.
      // To achieve this, we will throw an exception here which will be caught
      // by ExecManager to reset the queue source.
      if(isBatchTuple)
      {
        throw new CEPException(InterfaceError.STALE_QUEUE_SOURCE);
      }
      // For single events, we don't need to reset queue because ExecManager
      // always refresh queue source for every event.
      return;
    }

    if (directInterop)
    {
      // Increment the pending inputs count by 1 if data is not null.
      if(data!= null && data.getKind() != TupleKind.HEARTBEAT)
      {
        pendingInputs.incrementAndGet();
      }
      // Run the operator directly
      // Before running lock on self is released to avoid deadlocks with 
      // any thread trying to close this source while handling a 
      // runtime exception

      // When events need to be ordered, then we need to synchronize at the root.
      // When events do not need to be ordered, then we let each operator is responsible for
      //  synchronizing its own state. 
      // We can haave both styles of execution for the same event, hence we check for both, 
      //  rather than doing an 'else'.
      if (unorderedExecOpTask != null)
      {
        // FIXME run this in a separate thread if execOpTask is also present.

        // It is true that unordered stream has no notion of time,
        // however we propagate the current time to aide with 
        // debugging/testing.
        // Since approximate time will be ok, we use nanoTime instead of
        // the more expensive insertTimestamp() call. Note that 
        // insertTimestamp() ends up calling the synchronized method
        // TableSouceBase.getCorrectSystemTime() which leads to contention
        if (isSystemTimeStamped && overrideTime) 
        {
          if (data != null && !isBatchTuple)
            data.setTime(System.nanoTime());
        }

        // If heart-beat, propagate only if flag is set
        if (data != null && data.getKind() == TupleKind.HEARTBEAT)
        {
          if (isPropagateHeartbeatforUnordered())
            unorderedExecOpTask.run(data);
        }
        else
        {
          unorderedExecOpTask.run(data);
        }
      }

      /**
       * Bug 16820093:
       * Don't insert timestamp before acquiring lock on planmgr as 
       * we may send tuple with old time (typically hb).
       * Can happen in BAM use-case.
       * 
       * Only ordered execution in BAM so doing the changes for this
       * case only.
       * 
       * - call the appropriate run method in ExecOptTask which
       *   overrides the timestamp
       * 
       */
      if (orderedExecOpTask != null)
      {
        synchronized(opRun)
        {
          if (isSystemTimeStamped && overrideTime && !isBatchTuple) 
          {
            //It is ok to assign ts here since we may need it 
            //in recaliberateTime call
            insertTimestamp(data);
            data.setTotalOrderGuarantee(true);
          }
          else if(isSystemTimeStamped && !isBatchTuple)
          {
            data.setTotalOrderGuarantee(true);
          }
          
          if(data.isBHeartBeat() && isSystemTimeStamped && 
             !recalibrateTime(data.getTime(), true))
            return;
          
          currentEvent = data;
          lastInputTs.set(data.getTime());
 
          // This is done to handle the case of having multiple queue sources
          // corresponding to one stream. In this case, only first queue source
          // override the timestamp, rest queue sources will just propagate
          // events with existing timestamp. Call to recalibrate is to ensure
          // the lastAssignedTs is updated.
          if(hasMultileTableSourcesForOneStream() && !overrideTime)
          {
            recalibrateTime(data.getTime(), true);          
          }

          //override the time after acquiring lock on planMgr if needed
          if(isSystemTimeStamped && overrideTime && !isBatchTuple)
            orderedExecOpTask.run(data, overrideTime);
          else
            orderedExecOpTask.run(data, false);
          currentEvent = null;
        }
      }
      // After processing the non null input data, decrement the pending count by 1
      // if data was not heartbeat
      if(data != null && data.getKind() != TupleKind.HEARTBEAT)
        pendingInputs.decrementAndGet();
    }
    else 
    {
      synchronized(opRun)
      {
        if (isSystemTimeStamped && !isBatchTuple)
          insertTimestamp(data);
        queue.enqueue(data);
      }
    }
  }

  /**
   * This method is related to heartbeat timeout support for 
   * system timestamped sources. This is invoked only in the directInterop
   * mode by the TIMER thread
   */
  public void hbtTimeoutReminder()
  {
    assert directInterop : 
      "QueueSource.hbtTimeoutReminder should be called" + 
      " only in directInterop mode";

    if (isClosed.get())
      return;

    // If there are pending inputs on input thread, then no need to propagate
    // timeout heartbeat for that source.
    if(pendingInputs.get() > 0)
      return;
    
    // If the source is archived or useMillists, the nanotime will be calculated according to
    // clock time
    // In case of non-archived relation, the nanotime is calculated as result of
    // System.nanotime
    long currTimeNanos = 
      (this.isArchived() || execContext.getServiceManager().getConfigMgr().getUseMillisTs())? 
      System.currentTimeMillis() * 1000000L : System.nanoTime();
      
    if(currTimeNanos - lastInputTs.get() < Constants.DEFAULT_HBT_TIMEOUT_NANOS)
    {
      return;
    }
    // There is NO need to propagate heartbeat for system-timestamped unordered sources, as these are not
    //  time sensitive.
    //    if (unorderedExecOpTask != null)
    //      unorderedExecOpTask.run(null);

    // Run the operator directly
    // Before running ensure no lock on self to avoid deadlocks with 
    // any thread trying to close this source while handling a 
    // runtime exception
    synchronized(opRun)
    {
      if (orderedExecOpTask != null)
        orderedExecOpTask.run();
    }
  }


  /**
   * This method is related to heartbeat request support for 
   * system timestamped sources. 
   *
   * This is triggered by binary operators when one queue is empty and
   * the other has an element waiting for progress of time.
   *
   * This is invoked only in the directInterop mode by the TIMER thread
   */
  public void requestForHeartbeat(long hbtTime) 
  {
    boolean flag;

    assert directInterop : 
      "QueueSource.requestForHeartbeat should be called" + 
      " only in directInterop mode";

    if (isClosed.get())
      return;

//    synchronized(opRun)
//    {
    // Reason for lock(disable hbt for scheduler thread):
    // Currently we need to disable heartbeat when there are multiple queue 
    // sources for a single stream and we are pushing an event to these queue
    // sources.
    //
    // Once we receive an input on this stream, we want to propagate the input
    // event to all queue sources and no heartbeat should be propagated until
    // the event is sent to all queuesources.
    //
    // Don't propagate a heartbeat from this QueueSource to Source operator
    // if the request for heartbeat is disabled for this QueueSource.
    // During disable heartbeat, ExecManager will acquire the hbtRequestLock 
    // object. So scheduler thread will wait for ExecManager.insertFastBase
    // to release the lock.
    if(hasMultileTableSourcesForOneStream())
      hbtRequestLock.lock();
     
    /**
     the flag updateLastAssignedTs is false because the lastAssignedTs should be 
     updated only at the time of invocation of execution operator which
     will process the heartbeat.
     
     This is in reference to bug: 16076056 where
     recalibrateTime has updated the lastAssignedTs in the timer thread by
     hbtTime.
     Now context switch has changed from timer thread to input thread.
     This has led input thread to send inputs higher than hbtTime.
     Now when the timer thread resumed, the heartbeat input resulted into
     out of order error.
     */
    flag = recalibrateTime(hbtTime ,false);

    if (flag)
    {
      /**
        System.out.println("Recalibrated time for " + super.getExecOp().getOptName() +
                          " to " + hbtTime);
       */
      // Send the heartbeat at the exactly requested time.
      // Otherwise it would lead to a scenario where there is an
      // unending flow of heartbeats, even when there is no "real" data
      // just to keep binary operators updated on time.
      // This is unnecessary and undesirable.
      //
      // Example on what could happen if we send hbt at "current time"
      // instead of request time -
      //
      // Consider a join whose outer input has data at time 10 and
      // inner input is empty - then we could get the following sequence
      // Inner hbt request leads to hbt at 20
      // This causes an outer hbt request, say at time 30
      // This in turn causes an inner hbt request and so on ...
      //
      // On the other hand if we send hbt at request time, 10 in this case,
      // then the data on the outer as well this hbt on the inner
      // are cleared. 
      //
      // Now both queues are empty and there are no further hbt requests

      TupleValue hbt = new TupleValue();
      hbt.setTime(hbtTime);
      hbt.setBHeartBeat(true);
      
      try
      {
        putNext(hbt, false);
      }
      catch(CEPException e)
      {
      }
//      }
    }
    if(hasMultileTableSourcesForOneStream())
      hbtRequestLock.unlock();
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.interfaces.input.TableSource#getNext()
   */
  public TupleValue getNext() throws CEPException
  {

    TupleValue temp;

    if (directInterop) {
      // In the DI case, When this method is called, 
      // lock on opRun will be held

      temp = currentEvent;
      currentEvent = null;
      return temp;
    }

    // In FIFO mode, synchronize on opRun
    TupleValue tuple;

    synchronized(opRun)
    {
      tuple = queue.dequeue(null);
    }
    
    return tuple;
  }

  /**
   * This method will not be called in the direct Interop mode
   * It is called fo example by the FIFO scheduler
   * @return the timestamp of next element in the input queue
   */
  public long getOldestTs() throws CEPException
  {
    assert directInterop == false : 
      "QueuesSource.getOldestTs should not be called in direct Interop mode";

    TupleValue data;
    synchronized(opRun)
    {
      data = queue.peek(null);
    }
  
    if (data == null)
      return Constants.NULL_TIMESTAMP;
    
    return data.getTime();
  }

  /**
   * This method is called to check if there is any further unprocessed input
   * at this point in time
   * @return true iff there is any further unprocessed input at this point
   *              in time
   */
  public boolean hasNext() throws ExecException
  {
    synchronized(opRun)
    {
      if (directInterop) 
      {
        return currentEvent != null;
      }

      TupleValue data = queue.peek(null);

      if (data == null)
        return false;

      return true;
    }
  }

  /**
   * This method is called by system timestamped sources to generate
   * a heartbeat corresponding to a heartbeat timeout
   */
  public long getHeartbeatTime()
  {
    assert this.isSystemTimeStamped == true;

    // In DI mode, when this method is called,
    // lock on opRun will already be held
    synchronized(opRun)
    {
      try
      {
        // If there are pending inputs which are waiting to process for this
        // source, then there is no need of timeout heartbeat.
        if(pendingInputs.get() > 0)
          return -1;
        
        if (!hasNext())
          return getCorrectSystemTime();  
        else // some new input added
          return -1; //value that indicates heartbeat need not be sent
      }
      catch(ExecException e)
      {
        return -1;
      }
    }
  }
  
  public QueueSourceStat getStat()
  {
    QueueSourceStat stat = new QueueSourceStat();
    QueueStats smstat = queue.getStats();
    stat.m_tuplesInDisk = smstat.getTuplesInDisk();
    stat.m_tuplesInMem = smstat.getTuplesInMemory();
    return stat;
  }

  /**
   * This method is used in REGRESSION testing to support push mode
   * emulation for pull sources
   */
  public boolean supportsPushEmulation()
  {
    // This does not apply to a QueueSource since it is already
    // a push source (i.e. it is not a pull source)
    return false;
  }

  public String toString()
  {
    return toString("QueueSource");
  }
 
  public TupleValue getPendingData()
  {
    return this.pendingData;
  }
 
  public void setPendingData(TupleValue data)
  {
    this.pendingData = data;
  }

  public synchronized void putNext(String data) throws CEPException
  {
    TupleValueHelper.convertXmlToTupleValue(data, tuple, numAttrs,
                                            attrMetadata, attrNames,
                                            attrsList, isSystemTimeStamped);

      try {
        TupleValue tup_val = tuple.clone();
        if (isSystemTimeStamped) insertTimestamp(tup_val);
        queue.enqueue(tup_val);
      } catch (CloneNotSupportedException e) 
        {
          LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
          throw new InterfaceException(InterfaceError.XML_FORMAT_ERROR, e);
        }
    
  }

}
