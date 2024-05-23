/* $Header: SharedQueueWriterStats.java 24-apr-2008.15:17:57 najain Exp $ */

/* Copyright (c) 2006, 2008, Oracle. All rights reserved.  */

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
    najain      02/05/07 - coverage
    najain      10/12/06 - Creation
 */

/**
 *  @version $Header: SharedQueueWriterStats.java 24-apr-2008.15:17:57 najain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.queues;

import java.text.MessageFormat;

import oracle.cep.logging.DumpDesc;

/**
 * Statistics for the shared queue writer
 * 
 * @author najain
 */
@DumpDesc(autoFields=true)
public class SharedQueueWriterStats  extends Queue.QueueStats
{
  /** Total Number Of Elements Enqueued */
  private int totalNumElements;

  /** Total Number Of Positive Elements Enqueued */
  private int totalNumPosElements;

  /** Total Number Of Negative Elements Enqueued */
  private int totalNumNegElements;

  /** Total Number Of Heartbeats Enqueued */
  private int totalNumHeartbeats;

  /** Timestamp of Last Element Enqueued */
  private long tsLastElement;

  /** Timestamp of Last Positive Element Enqueued */
  private long tsLastPosElement;

  /** Timestamp of Last Negative Element Enqueued */
  private long tsLastNegElement;

  /** Timestamp of heartbeat Enqueued */
  private long tsLastHeartbeat;

  /** Queue Execution Identifier */
  private int qId;

  /** Queue Physical Identifier */
  private int qPhyId;

  /** Number Of Elements in the queue present currently */
  private int numElements;

  /** Number Of Positive Elements in the queue present currently */
  private int numPosElements;

  /** Number Of Negative Elements in the queue present currently */
  private int numNegElements;

  /** Number Of Heartbeats in the queue present currently */
  private int numHeartbeats;

  public SharedQueueWriterStats(int id, int phyId) {
	super();
	qId = id;
	qPhyId = phyId;
  }

  /**
   * @return Returns the numElements.
   */
  public int getNumElements()
  {
    return numElements;
  }

  /**
   * @return Returns the numHeartbeats.
   */
  public int getNumHeartbeats()
  {
    return numHeartbeats;
  }

  /**
   * @return Returns the numNegElements.
   */
  public int getNumNegElements()
  {
    return numNegElements;
  }

  /**
   * @return Returns the numPosElements.
   */
  public int getNumPosElements()
  {
    return numPosElements;
  }
  
  public void setNumElements(int numElements) {
	this.numElements = numElements;
  }

  public void setNumPosElements(int numPosElements) {
	this.numPosElements = numPosElements;
  }

  public void setNumNegElements(int numNegElements) {
	this.numNegElements = numNegElements;
  }

  public void setNumHeartbeats(int numHeartbeats) {
	this.numHeartbeats = numHeartbeats;
  }

  /**
   * @return Returns the totalNumElements.
   */
  public int getTotalNumElements()
  {
    return totalNumElements;
  }

  /**
   * @return Returns the totalNumHeartbeats.
   */
  public int getTotalNumHeartbeats()
  {
    return totalNumHeartbeats;
  }

  /**
   * @return Returns the totalNumNegElements.
   */
  public int getTotalNumNegElements()
  {
    return totalNumNegElements;
  }

  /**
   * @return Returns the totalNumPosElements.
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
   * @param tsLastElement The tsLastElement to set.
   */
  public void setTsLastElement(long tsLastElement)
  {
    this.tsLastElement = tsLastElement;
  }

  /**
   * @return Returns the tsLastHeartbeat.
   */
  public long getTsLastHeartbeat()
  {
    return tsLastHeartbeat;
  }

  /**
   * @param tsLastHeartbeat The tsLastHeartbeat to set.
   */
  public void setTsLastHeartbeat(long tsLastHeartbeat)
  {
    this.tsLastHeartbeat = tsLastHeartbeat;
  }

  /**
   * @return Returns the tsLastNegElement.
   */
  public long getTsLastNegElement()
  {
    return tsLastNegElement;
  }

  /**
   * @param tsLastNegElement The tsLastNegElement to set.
   */
  public void setTsLastNegElement(long tsLastNegElement)
  {
    this.tsLastNegElement = tsLastNegElement;
  }

  /**
   * @return Returns the tsLastPosElement.
   */
  public long getTsLastPosElement()
  {
    return tsLastPosElement;
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

  public String toString()
  {
    return MessageFormat.format("totalNumElements = {0}" +
                                "totalNumPosElements = {1}" +
                                "totalNumNegElements = {2}" +
                                "totalNumHeartbeats = {3}" +
                                "tsLastElement = {4}" +
                                "tsLastPosElement = {5}" +
                                "tsLastNegElement = {6}" +
                                "tsLastHeartbeat = {7}",
        totalNumElements, totalNumPosElements, totalNumNegElements, totalNumHeartbeats,
        tsLastElement, tsLastPosElement, tsLastNegElement, tsLastHeartbeat);
    }
}
