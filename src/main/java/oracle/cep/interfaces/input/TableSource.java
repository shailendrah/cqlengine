/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/input/TableSource.java /main/21 2015/02/06 15:09:31 sbishnoi Exp $ */

/* Copyright (c) 2006, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares TableSource in package oracle.cep.interfaces.input.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    udeshmuk  05/21/13 - bug 16820093 - add getCorrectsystemtime
    sbishnoi  03/08/13 - fix heartbeat propagation for archiver sources
    vikshukl  07/30/12 - archived dimension relation
    udeshmuk  05/13/11 - isArchived setter
    anasrini  03/21/11 - add getName
    sborah    07/14/09 - support for bigdecimal
    sborah    07/10/09 - cleanup
    anasrini  05/08/09 - system timestamped source lineage
    anasrini  02/13/09 - remove allowEnqueue, add hbtTimeoutReminder
    anasrini  02/13/09 - add supportsPushEmulation
    anasrini  11/17/08 - Add allowEnqueue
    sbishnoi  08/04/08 - support for nanosecond
    udeshmuk  03/17/08 - replace ExecException by CEPException in hasNext.
    udeshmuk  01/17/08 - cchange returntype of getOldestTs.
    udeshmuk  12/17/07 - add getHeartbeatTime method.
    parujain  10/17/07 - cep-bam integration
    najain    03/12/07 - bug fix
    najain    11/07/06 - add getOldestTs
    anasrini  09/13/06 - add setNumAttrs and setAttrInfo APIs
    skaluska  03/25/06 - implementation
    skaluska  03/23/06 - implementation
    skaluska  03/22/06 - implementation
    skaluska  02/18/06 - clarify interfaces 
    skaluska  02/17/06 - Creation
    skaluska  02/17/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/input/TableSource.java /main/21 2015/02/06 15:09:31 sbishnoi Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.interfaces.input;

import java.util.concurrent.locks.Lock;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.operators.ExecOpt;

/**
 * The interface that the STREAM server uses to get input stream / relation
 * tuples. Various kinds of inputs can be obtained by extending this class (e.g,
 * read from a file, read from a network etc) The main method is getNext(),
 * which the server uses to pull the next tuple whenever it desires. Before
 * consuming any input tuples, the server invokes the start() method, which can
 * be used to perform various kinds of initializations. Similarly, the end()
 * method is called ,when the server is not going to invoke any more getNext()s.
 *
 * @author skaluska
 */
public interface TableSource extends Runnable {


  /**
   * Set the number of attributes in the schema of the tuples flowing
   * through this object.  Along with setAttrInfo, used to specify the
   * schema of the tuples flowing through.
   *
   * @param       numAttrs      number of attributes in the schema
   */
  public void setNumAttrs(int numAttrs);

  
  /**
   * Set the information about one particular attribute in the schema of
   * the output tuples.
   *
   * @param   attrPos           position of the attribute among
   *                            attributes in the schema
   * @param   attrMetadata      metadata of this attribute, ie, its datatype
   *                            , length, precision and scale
   */
  public void setAttrInfo(int attrPos, String attrName, 
                          AttributeMetadata attrMetadata);
  
  /**
   * Set whether the source is a stream or relation
   * 
   * @param isStrm
   *             True if stream else false if relation
   */
  public void setIsStream(boolean isStrm);

  /**
   * Set whether the source is an archived one
   *
   * @param isArchived
   *        True if source is Archived, false otherwise
   */
  public void setIsArchived(boolean isArchived);
  
  /**
   * Get whether the source is an archived one
   * 
   * @return true if the source is archived, false otherwise
   */
  public boolean isArchived();
  
  /**
   * Set whether the source is an archived dimension. This is applicable
   * only to relations.
   * 
   * @param isDimension True if the source is a dimension archived relation
   *        
   */
  public void setIsDimension(boolean isDimension);

  /**
   * @return true iff this is associated with a system timestamped source
   */
  public boolean isSystemTimeStamped();

  /**
   * Signal that getNext will be invoked next.
   * @throws CEPException 
   */
  public void start() throws CEPException;

  /**
   * Get the next tuple from the table (stream/relation).
   * 
   * @return Reference to the next tuple value. The referenced
   * object needs to be valid only until the next getNext or end
   * invocation.
   * @throws CEPException 
   */
  public TupleValue getNext() throws CEPException;

  /**
   * Signal that the server needs no more tuples.
   * @throws CEPException 
   */
  public void end() throws CEPException;

  /**
   * Get the name of this TableSource
   * @return the name of this TableSource
   */
  public String getName();

  /**
   * Get the number of attributes in a tuple
   * @return Number of attributes
   */
  public int getNumAttrs();
  
  /**
   * Get attribute type
   * @param pos Attribute position
   * @return Datatype
   */
  //public Datatype getAttrType(int pos);
  
  /**
   * Get attribute length
   * @param pos Attribute position
   * @return Attribute length
   */
  //public int getAttrLength(int pos);
  
  /**
   * Get attribute precision value
   * @param pos Attribute position
   * @return Attribute precision value
   */
  //public int getAttrPrecision(int pos);
  
  /**
   * Get attribute scale value
   * @param pos Attribute position
   * @return Attribute scale value
   */
  //public int getAttrScale(int pos);

  public long getOldestTs() throws CEPException;
  
  public boolean hasNext() throws CEPException;
  
  /**
   * Get correct timestamp for the heartbeat to be sent
   * @return long timestamp value
   */
  public long getHeartbeatTime();
  
  /**
   * Set a flag whether we should convert a tuple time-stamp unit
   * before input it to StreamSrc/RelSrc
   * @param convertTs
   */
  public void setConvertTs(boolean convertTs);  

  /**
   * This method sets the execution source operator that this source
   * is supplying input data to
   * @param op the associated execution source operator
   */
  public void setExecOp(ExecOpt op);
  
  /**
   * This methods sets the execution source operator for an unordered source.
   * 
   * The reason we need to differentiate a totally ordered (i.e. setExecOp)
   *  from an unordered execution operator is that both may be present simultaneously.  
   *  
   * @param op
   */
  public void setUnorderedExecOp(ExecOpt op);
  
  /**
   * Getter for the associated execution operator
   * @return the associated execution operator
   */
  public ExecOpt getExecOp();

  /**
   * This method is called only during REGRESSION testing
   * This gets the underlying pull source that requires push emulation
   * @return the underlying pull source that requires push emulation
   */
  public TableSource getInnerSource();

  /**
   * This method is used in REGRESSION testing to support push mode
   * emulation for pull sources
   */
  public boolean supportsPushEmulation();

  /**
   * This method is related to heartbeat timeout support for 
   * system timestamped sources. This is invoked only in the directInterop
   * mode by the TIMER thread
   */
  public void hbtTimeoutReminder();

  /**
   * This method is called to request for a heartbeat. These are typically
   * made by binary operators when one queue is empty and the other has
   * an element waiting for progress of time.
   *
   * This is invoked only in the directInterop mode by the TIMER thread
   * @param hbtTime the time at which a heartbeat is sought
   */
  public void requestForHeartbeat(long hbtTime);

  /**
   * This method is called to get the instance of Lock object which is
   * required by input thread prior to start pushing heartbeat for a
   * particular input source object.
   */ 
  public Lock getRequestForHeartbeatLock();

  /**
   * Returns true if this table source belong to a stream which feeds input
   * events to multiple table sources
   * Return false otherwise;
   */
  public boolean hasMultileTableSourcesForOneStream();
  
  /**
   * Set the flag to true if this table source belong to a stream which feeds
   * input events to multiple table sources
   * Set the flag to false otherwise
   */
  public void setHasMultileTableSourcesForOneStream(boolean flag);

  /**
   * Generally unordered operators do not need to propagate heart-beat, 
   *  as they are not time-sensitive (e.g. source, selector, projector, output).
   * However, in the case of an application-timestamped partitioned ordered query, 
   * the partitioned operators may be setup with an upstream unordered operator, 
   * which would then need to propagate the heart-beat. 
   * This method is used to indicate that the queue source
   *  does need to propagate the heart-beat to the unordered source. 
   *
   * @param propagate true if table source should propagate heart-beat to unordered source operators. 
   */
  public void setPropagateHeartbeatforUnordered(boolean propagate);
  
  /**
   * Returns the correct system time for this source.
   * @return
   */
  public long getCorrectSystemTime();
  
}
