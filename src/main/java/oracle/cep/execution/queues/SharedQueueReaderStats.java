/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/queues/SharedQueueReaderStats.java /main/7 2011/02/24 08:23:34 alealves Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    najain      04/24/08 - add more fields
    hopark      02/06/08 - fix autofields
    hopark      01/01/08 - trace cleanup
    hopark      10/22/07 - remove TimeStamp
    najain      07/23/07 - move stats to SharedQueueWriter
    najain      10/12/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/queues/SharedQueueReaderStats.java st_pcbpel_alealves_9261513/1 2010/04/30 11:07:04 alealves Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.queues;

import java.text.MessageFormat;

import oracle.cep.logging.DumpDesc;

/**
 * Statistics for the shared queue reader
 * 
 * FIXME When ordering is necessary, what should we do here? Should still keep the last TS seem
 *  or the highest TS?
 * 
 * @author najain
 */
@DumpDesc(autoFields=true)
public class SharedQueueReaderStats extends Queue.QueueStats
{
  /** Total Number Of Elements Dequeued */
  private int totalNumElements;
  
  /** Total Number Of Positive Elements Dequeued */
  private int totalNumPosElements;

  /** Total Number Of Negative Elements Dequeued */
  private int totalNumNegElements;

  /** Total Number Of heartbeats Dequeued */
  private int totalNumHeartbeats;
  
  /** Timestamp of last message dequeued */
  private long tsLastElement;

  /** Timestamp of last positive message dequeued */
  private long tsLastPosElement;

  /** Timestamp of last negative message dequeued */
  private long tsLastNegElement;

  /** Timestamp of last heartbeat dequeued */
  private long tsLastHeartbeat;

  /** initial number of total elements */
  private int initElements;

  /** initial number of total positive elements */
  private int initPosElements;

  /** initial number of total negative elements */
  private int initNegElements;

  /** initial number of total heartbeats */
  private int initHeartbeats;

  /** total number of elements enqueued for other readers */
  private int numOthers;

  /** total number of positive elements enqueued for other readers */
  private int numPosOthers;

  /** total number of negative elements enqueued for other readers */
  private int numNegOthers;

  /** total number of heartbeats for other readers */
  private int numHeartbeatOthers;

  public SharedQueueReaderStats()
  {
    tsLastElement = 0;
    tsLastPosElement = 0;
    tsLastNegElement = 0;
    tsLastHeartbeat = 0;
  }

  /**
   * @return Returns the totalNumElements.
   */
  public int getTotalNumElements()
  {
    return totalNumElements;
  }

  /**
   * @return Returns the numHeartbeats.
   */
  public int getTotalNumHeartbeats()
  {
    return totalNumHeartbeats;
  }

  /**
   * @return Returns the numNegElements.
   */
  public int getTotalNumNegElements()
  {
    return totalNumNegElements;
  }

  /**
   * @return Returns the numPosElements.
   */
  public int getTotalNumPosElements()
  {
    return totalNumPosElements;
  }

  /**
   * @return Returns the tsLastElement.
   */
  public long getTsLastElement()
  {
    return tsLastElement;
  }

  /**
   * @return Returns the tsLastHeartbeat.
   */
  public long getTsLastHeartbeat()
  {
    return tsLastHeartbeat;
  }

  /**
   * @return Returns the tsLastNegElement.
   */
  public long getTsLastNegElement()
  {
    return tsLastNegElement;
  }

  /**
   * @return Returns the tsLastPosElement.
   */
  public long getTsLastPosElement()
  {
    return tsLastPosElement;
  }

  /**
   * @param tsLastElement The tsLastElement to set.
   */
  public void setTsLastElement(long tsLastElement)
  {
    this.tsLastElement = tsLastElement;
  }

  /**
   * @param tsLastHeartbeat The tsLastHeartbeat to set.
   */
  public void setTsLastHeartbeat(long tsLastHeartbeat)
  {
    this.tsLastHeartbeat = tsLastHeartbeat;
  }

  /**
   * @param tsLastNegElement The tsLastNegElement to set.
   */
  public void setTsLastNegElement(long tsLastNegElement)
  {
    this.tsLastNegElement = tsLastNegElement;
  }

  /**
   * @param tsLastPosElement The tsLastPosElement to set.
   */
  public void setTsLastPosElement(long tsLastPosElement)
  {
    this.tsLastPosElement = tsLastPosElement;
  }

  public void incrTotalNumElements()
  {
    totalNumElements++;
  }

  public void incrTotalNumPosElements()
  {
    totalNumPosElements++;
  }

  public void incrTotalNumNegElements()
  {
    totalNumNegElements++;
  }

  public void incrTotalNumHeartbeats()
  {
    totalNumHeartbeats++;
  }

  public void initialize(SharedQueueWriterStats writerStats)
  {
    initElements = writerStats.getNumElements();
    initPosElements = writerStats.getNumPosElements();
    initNegElements = writerStats.getNumNegElements();
    initHeartbeats = writerStats.getNumHeartbeats();

    assert numOthers == 0 : numOthers ;
    assert numPosOthers == 0;
    assert numHeartbeatOthers == 0;
  }

  /**
   * @return Returns the initElements.
   */
  public int getInitElements()
  {
    return initElements;
  }

  /**
   * @return Returns the initHeartbeats.
   */
  public int getInitHeartbeats()
  {
    return initHeartbeats;
  }

  /**
   * @return Returns the initNegElements.
   */
  public int getInitNegElements()
  {
    return initNegElements;
  }

  /**
   * @return Returns the initPosElements.
   */
  public int getInitPosElements()
  {
    return initPosElements;
  }
  
  /**
   * @return Returns the numOthers.
   */
  public int getNumOthers()
  {
    return numOthers;
  }

  /**
   * @return Returns the numPosOthers.
   */
  public int getNumPosOthers()
  {
    return numPosOthers;
  }

  public void incrNumOthers()
  {
    numOthers++;
  }

  public void incrNumPosOthers()
  {
    numPosOthers++;
  }

  public void clear()
  {
    initElements = 0;
    initPosElements = 0;
    initNegElements = 0;
    initHeartbeats = 0;
    numOthers = 0;
    numPosOthers = 0;
  }

  public String toString()
  {
    return MessageFormat.format("initElements = {0} " +
                                "initPosElements = {1} " +
                                "initNegElements = {2} " +
                                "initHeartbeats = {3} " +
                                "numOthers = {4} " +
                                "numPosOthers = {5}",
        initElements, initPosElements, initNegElements, 
        initHeartbeats, numOthers, numPosOthers);
  }

}
