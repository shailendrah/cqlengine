/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ExecStats.java /main/11 2012/04/02 03:50:32 sbishnoi Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares ExecStats in package oracle.cep.execution.operators.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    sbishnoi  02/16/12 - adding stats method for batch outputs
    sbishnoi  04/01/09 - commenting runningTime: citipoc optimization
    udeshmuk  09/17/08 - just print the values in toString.
    udeshmuk  09/13/08 - adding extra stats
    hopark    02/06/08 - fix autofields
    hopark    01/01/08 - support xmllog
    najain    06/11/07 - logging
    parujain  05/08/07 - add statistics
    najain    03/12/07 - bug fix
    hopark    02/27/07 - add toString
    najain    01/05/07 - spill over support
    skaluska  03/21/06 - clear everything in constructor 
    skaluska  03/14/06 - query manager 
    skaluska  03/05/06 - Creation
    skaluska  03/05/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ExecStats.java /main/11 2012/04/02 03:50:32 sbishnoi Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import oracle.cep.logging.DumpDesc;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Execution Statistics for an operator Since there can be multiple threads
 * executing an operator, it makes sense for each thread to collect private
 * stats and add them to the operator stats at the end of the run. Hence, the
 * add is synchronized. The gets are not synchronized, since the expected use of
 * stats is for scheduling, flow control, tuning, etc.
 * 
 * @author skaluska
 */
@DumpDesc(autoFields=true)
public class ExecStats
{
  /** sum of Input queue Sizes - used for average */
  private long sumInputSizes;
  /** number of executions (times the operator was run) */
  private long numExecutions;
  /** number of input tuples processed */
  private long numInputs;
  /** number of tuples output */
  private long numOutputs;
  /** number of input heartbeats*/
  private long numInputHeartbeats;
  /** number of output heartbeats*/
  private long numOutputHeartbeats;
  
  /** start time */
  private long startTime;
  /** End time */
  private long endTime;
  /** Num of input tuples within the duration */
  private long numInputsLatest;
  /** Number of output tuples within the duration */
  private long numOutputsLatest;
  /** Total time of Execution */
  private long totalTime;
  /** Total running time of all the operators combined */
  // Note: Commenting running time for citipoc optimization
  //private static AtomicLong runningTime = new AtomicLong(0);
  /** Whether Enabled or not - Relevant for latency */
  private boolean isEnabled;
  /** Total Latency for output and input streams */
  private long latency;
  /** Total time spent in common processing in pattern */
  private long commonProcTime;
  /** Total time spent in prev functionality related processing in pattern */
  private long prevFuncTime;
  /** Total time spent in creating and sorting readyToOutputList */
  private long readyToOutputListTime;
  /** Total time spent in reporting the bindings */
  private long bindingReportTime;
  /** Total time spent in removing empty partitions */
  private long removePartnTime;
  /** Total time spent in non-event other partns processing */
  private long nonEventTime;
  /** Total time spent in expiring out of range tuples */
  private long expireOutOfRangeTime;
  /** Total time spent in pre-processing for partn n prev */
  private long preProcessTime;
  /** Total time spent in post-processing for partn n prev */
  private long postProcessTime; 
  
  private long cacheHits;
  
  private long cacheMisses;
  
  private boolean isSrcCached;
  
  private String cacheName;
  
  private long totalPstmtRunTime;
  
  private long totalPstmtExec;

  /**
   * Constructor for ExecStats
   */
  public ExecStats()
  {
    clear();
  }

  /**
   * Clear everything
   */
  public void clear()
  {
    numExecutions = 0;
    numInputs = 0;
    numOutputs = 0;
    numOutputHeartbeats=0;
    numInputHeartbeats=0;
    startTime = 0;
    endTime = 0;
    numInputsLatest = 0;
    numOutputsLatest = 0;
    totalTime = 0;
    isEnabled = false;
    latency = 0;
    commonProcTime = 0;
    prevFuncTime = 0;
    readyToOutputListTime = 0;
    bindingReportTime = 0;
    removePartnTime = 0;
    nonEventTime = 0;
    expireOutOfRangeTime = 0;
    preProcessTime = 0;
    postProcessTime = 0;
    cacheHits=0;
    cacheMisses=0;
    isSrcCached = false;
    cacheName= "";
    totalPstmtExec = 0;
    totalPstmtRunTime = 0;
  }

  /**
   * Add statistics from the specified operator
   * 
   * @param s
   */
  public void add(ExecStats s)
  {
    synchronized (this) {
      sumInputSizes += s.numInputs;
      numExecutions += s.numExecutions;
      numInputs += s.numInputs;
      numOutputs += s.numOutputs;
      numOutputHeartbeats += s.numOutputHeartbeats;
      numInputHeartbeats += s.numInputHeartbeats;
      startTime = s.startTime;
      endTime = s.endTime;
      numInputsLatest = s.numInputs;
      numOutputsLatest = s.numOutputs;
      totalTime += (s.endTime - s.startTime);
      latency += s.latency;
      isEnabled = s.isEnabled;      
      // Note: Commenting running time for citipoc optimization
      /*
      synchronized(runningTime)
      {
        runningTime.addAndGet((s.endTime - s.startTime));
      }*/
      commonProcTime += s.commonProcTime;
      prevFuncTime += s.prevFuncTime;
      readyToOutputListTime += s.readyToOutputListTime;
      bindingReportTime += s.bindingReportTime;
      removePartnTime += s.removePartnTime;
      nonEventTime += s.nonEventTime;
      expireOutOfRangeTime += s.expireOutOfRangeTime;
      preProcessTime += s.preProcessTime;
      postProcessTime += s.postProcessTime;
      cacheHits += s.cacheHits;
      cacheMisses += s.cacheMisses;
    }
  }

  /**
   * Getter for numExecutions in ExecStats
   * @return Returns the numExecutions
   */
  public long getNumExecutions()
  {
    return numExecutions;
  }
  
  /**
   * Getter for Total Time taken by the operator in execution
   * @return Returns the Total Execution time
   */
  public long getTotalTime()
  {
    return totalTime;
  }
  
  /**
   * Getter for the Total Running Time of all the operators
   * @return Returns the Total Running time
   */
  public static synchronized long getRunningTime()
  {
    return 0;
    //return runningTime.get();
  }
  
  /**
   * Getter of the latency
   * @return Returns the latency
   */
  public long getLatency()
  {
    return latency;
  }

  /**
   * Setter of the latency in ExecStats
   * @param delay
   *        Latency to set
   */
  public synchronized void addLatency(long delay)
  {
    this.latency += delay;
  }
  
  public void clearLatency()
  {
    this.latency = 0;
  }
  
  /**
   * Getter of whether enabled or not
   * @return
   */
  public boolean isEnabled()
  {
    return this.isEnabled;
  }
  
  /**
   * Setter for IsEnabled in ExecStats
   * 
   * @param isenabled Enabled or not
   */
  public void setIsEnabled(boolean isenabled)
  {
    this.isEnabled = isenabled;
  }
  
  /**
   * Setter for numExecutions in ExecStats
   * @param numExecutions The numExecutions to set.
   */
  public void setNumExecutions(long numExecutions)
  {
    this.numExecutions = numExecutions;
  }

  /**
   * Increment numExecutions in ExecStats (by 1)
   */
  public void incrNumExecutions()
  {
    this.numExecutions += 1;
  }

  /**
   * Getter for numInputs in ExecStats
   * @return Returns the numInputs
   */
  public long getNumInputs()
  {
    return numInputs;
  }
  
  /**
   * Start Time for the last duration
   * @return Start time
   */
  public long getStartTime()
  {
    return this.startTime;
  }
  
  /**
   * End time for the last duration
   * @return End time
   */
  public long getEndTime()
  {
    return this.endTime;
  }
  
  /**
   * Number of inputs during the last duration
   * @return Number of Inputs in the latest duration
   */
  public long getNumInputsLatest()
  {
    return this.numInputsLatest;
  }
  
  /**
   * Number of outputs during the last duration
   * @return Number of outputs in the latest durtion
   */
  public long getNumOutputsLatest()
  {
    return this.numOutputsLatest;
  }
  
  /**
   * Setter of the startTime of the duration  
   * @param t StartTime
   */
  public void setStartTime(long t)
  {
    this.startTime = t;
  }
  
  /**
   * Setter of the EndTime of the duration of observation
   * @param t EndTime
   */
  public void setEndTime(long t)
  {
    this.endTime = t;
  }
  
  /**
   * Setter of numInputsLatest in ExecStats
   * @param num The numInputsLatest to set
   */
  public void setNumInputsLatest(long num)
  {
    this.numInputsLatest = num;
  }
  
  /**
   * Setter of numOutputsLatest in ExecStats
   * @param num The numOutputsLatest to set
   */
  public void setNumOutputsLatest(long num)
  {
    this.numOutputsLatest = num;
  }

  /**
   * Setter for numInputs in ExecStats
   * @param numInputs The numInputs to set.
   */
  public void setNumInputs(long numInputs)
  {
    this.numInputs = numInputs;
  }

  /**
   * Increment numInputs in ExecStats (by 1)
   */
  public synchronized void incrNumInputs()
  {
    this.numInputs += 1;
  }

  /**
   * Getter for numOutputs in ExecStats
   * @return Returns the numOutputs
   */
  public long getNumOutputs()
  {
    return numOutputs;
  }

  /**
   * Setter for numOutputs in ExecStats
   * @param numOutputs The numOutputs to set.
   */
  public void setNumOutputs(long numOutputs)
  {
    this.numOutputs = numOutputs;
  }

  public long getNumOutputHeartbeats()
  {
    return numOutputHeartbeats;
  }

  public void setNumOutputHeartbeats(long numOutputHeartbeats)
  {
    this.numOutputHeartbeats = numOutputHeartbeats;
  }

  public long getNumInputHeartbeats()
  {
    return numInputHeartbeats;
  }

  public void setNumInputHeartbeats(long numInputHeartbeats)
  {
    this.numInputHeartbeats = numInputHeartbeats;
  }

  /**
   * Increment numOutputs in ExecStats (by 1)
   */
  public synchronized void incrNumOutputs()
  {
    this.numOutputs += 1;
  }
  
  /**
   * Increment numOutputHeartbeats by 1
   */
  public synchronized void incrNumOutputHeartbeats()
  {
    this.numOutputHeartbeats += 1;
  }
  
  /**
   * Increment numInputHeartbeats by 1
   */
  public synchronized void incrNumInputHeartbeats()
  {
    this.numInputHeartbeats += 1;
  }
  
  /**
   * Increment numOutputs in ExecStats (by count)
   * @param count
   */
  public synchronized void incrNumOutputs(long count)
  {
    this.numOutputs += count;
  }

  /**
   * Getter for sumInputSizes in ExecStats
   * @return Returns the sumInputSizes
   */
  public long getSumInputSizes()
  {
    return sumInputSizes;
  }

  /**
   * Setter for sumInputSizes in ExecStats
   * @param sumInputSizes The sumInputSizes to set.
   */
  public void setSumInputSizes(long sumInputSizes)
  {
    this.sumInputSizes = sumInputSizes;
  }

  /**
   * Increment sumInputSizes in ExecStats by the specified amount
   * @param amount The amount to increment.
   */
  public synchronized void incrSumInputSizes(int amount)
  {
    this.sumInputSizes += amount;
  }

  /**
   * Average size of input queue when the operator is run
   * 
   * @return Average value
   */
  public synchronized long avgInputSize()
  {
    return sumInputSizes / numExecutions;
  }

  /**
   * Returns the selectivity defined as number of outputs per input tuple
   * processed
   * 
   * @return Selectivity
   */
  public synchronized float selectivity()
  {
    return numOutputs / numInputs;
  }
  
  public synchronized void addCommonProcTime (long tm)
  {
    this.commonProcTime += tm;
  }

  public synchronized void addPrevFuncTime(long tm)
  {
    this.prevFuncTime += tm;
  }
 
  public synchronized void addReadyToOutputListTime(long tm)
  { 
    this.readyToOutputListTime += tm;
  }

  public synchronized void addBindingReportTime(long tm)
  {
    this.bindingReportTime += tm;
  }

  public synchronized void addRemovePartnTime(long tm)
  {
    this.removePartnTime += tm;
  }

  public synchronized void addNonEventTime(long tm)
  {
    this.nonEventTime += tm;
  }

  public synchronized void addExpireOutOfRangeTime(long tm)
  {
    this.expireOutOfRangeTime += tm;
  }
  
  public synchronized void addPreProcessTime(long tm)
  {
    this.preProcessTime += tm;
  }
  
  public synchronized void addPostProcessTime(long tm)
  {
    this.postProcessTime += tm;
  }
  
  
  public long getCacheHits()
  {
	return cacheHits;
  }

  public void setCacheHits(long cacheHits)
  {
	this.cacheHits = cacheHits;
  }

  public long getCacheMisses()
  {
	return cacheMisses;
  }

  public void setCacheMisses(long cacheMisses)
  {
	this.cacheMisses = cacheMisses;
  }

  public boolean isSrcCached()
  {
	return isSrcCached;
  }

  public void setSrcCached(boolean isSrcCached)
  {
	this.isSrcCached = isSrcCached;
  }

  public String getCacheName()
  {
	return cacheName;
  }

  public void setCacheName(String cacheName)
  {
	this.cacheName = cacheName;
  }
  

	public long getTotalPstmtRunTime() {
		return totalPstmtRunTime;
	}

	public void setTotalPstmtRunTime(long totalPstmtRunTime) {
		this.totalPstmtRunTime = totalPstmtRunTime;
	}

	public long getTotalPstmtExec() {
		return totalPstmtExec;
	}

	public void setTotalPstmtExec(long totalPstmtExec) {
		this.totalPstmtExec = totalPstmtExec;
	}

public void setTotalTime(long totalTime) {
	this.totalTime = totalTime;
}

/**
   * Returns the summary of stats.
   */
  public String toString()
  {
/*    return "sumInputSizes=" + sumInputSizes + 
      ", numExecutions=" + numExecutions + 
      ", numInputs=" + numInputs + 
      ", numOutputs=" + numOutputs + 
      ", startTime=" + startTime +
      ", endTime=" + endTime +
      ", numInputsLatest=" + numInputsLatest +
      ", numOutputsLatest=" + numOutputsLatest +
      ", totalTime=" + totalTime +
      ", commonProcTime=" + commonProcTime +
      ", prevFuncTime=" + prevFuncTime +
      ", readyToOutputListTime=" + readyToOutputListTime +
      ", bindingReportTime=" + bindingReportTime +
      ", removePartnTime=" + removePartnTime +
      ", nonEventTime=" + nonEventTime;*/

    return sumInputSizes + 
      "," + numExecutions + 
      "," + numInputs + 
      "," + numOutputs + 
      "," + numOutputHeartbeats +
      "," + numInputHeartbeats +
/*      "," + startTime +
      "," + endTime +
      "," + numInputsLatest +
      "," + numOutputsLatest +*/
      "," + totalTime +
      "," + commonProcTime +
      "," + prevFuncTime +
      "," + expireOutOfRangeTime +
      "," + preProcessTime +
      "," + postProcessTime +
      "," + readyToOutputListTime +
      "," + bindingReportTime +
      "," + removePartnTime +
      "," + cacheHits +
      "," + cacheMisses +
      "," + totalPstmtExec+
      "," + totalPstmtRunTime;
  }
}
