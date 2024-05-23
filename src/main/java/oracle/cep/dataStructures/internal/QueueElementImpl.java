/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/QueueElementImpl.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares Element in package oracle.cep.execution.queues.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 udeshmuk  04/17/12 - add snapshotId methods
 anasrini  03/22/11 - setTupleValue
 sbishnoi  03/24/09 - modifying toString
 udeshmuk  01/16/09 - add flag for total ordering guarantee
 hopark    01/31/08 - 
 sbishnoi  11/27/07 - support for update semantics; add E_UPDATE
 hopark    10/25/07 - add equals
 hopark    09/11/07 - use ITuplePtr
 hopark    06/19/07 - cleanup
 hopark    05/28/07 - logging support
 najain    03/14/07 - Cleanup
 najain    03/12/07 - bug fix
 najain    01/04/07 - spill over support
 najain    07/05/06 - cleanup
 skaluska  04/04/06 - add copyTs 
 skaluska  02/07/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/QueueElementImpl.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.logging.Level;

import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IPinnable;

/**
 * @author skaluska
 */
public class QueueElementImpl implements QueueElement
{
  private static final long serialVersionUID = 2695905242330192716L;
        
  protected Kind kind;

  protected ITuplePtr tuple;

  protected TupleValue tupleValue;

  protected long   ts;

  // flag to check whether the tuples will come in strictly increasing order
  // order of timestamp
  protected boolean totalOrderingGuarantee=false;

  //snapshotId associated with the event. useful for archived rel/stream based
  //queries.
  //The default value is Long.MAX_VALUE so that as a default behavior the 
  //element should get propagated on all the readers of a queue.
  protected long snapshotId = Long.MAX_VALUE;

  /**
   * Constructor for Element
   */
  public QueueElementImpl()
  {
  }

  /**
   * Constructor for Element
   * 
   * @param kind
   * @param tuple
   * @param ts
   */
  public QueueElementImpl(Kind kind, ITuplePtr tuple, long ts)
  {
    super();
    this.kind = kind;
    this.tuple = tuple;
    this.tupleValue = null;
    this.ts = ts;
  }

  /**
   * treat it as a fresh Element
   */
  public void clear()
  {
    kind = null;
    tuple = null;
    ts = 0;
    this.tupleValue = null;
    this.totalOrderingGuarantee = false;
    this.snapshotId = Long.MAX_VALUE;
  }

  /**
   * Copy Element - this is a shallow copy
   * 
   * @param e
   *          Element to copy
   */
  public void copy(QueueElement e)
  {
    this.kind = e.getKind();
    this.tuple = e.getTuple();
    this.ts = e.getTs();
    this.totalOrderingGuarantee = e.getTotalOrderingGuarantee();
    this.tupleValue = e.getTupleValue();
    this.snapshotId = e.getSnapshotId();
  }

  public long getSnapshotId()
  {
    return this.snapshotId;
  }

  public void setSnapshotId(long snapshotId)
  {
    this.snapshotId = snapshotId;
  }

  /**
   * @return Returns the kind.
   */
  public Kind getKind()
  {
    return kind;
  }

  /**
   * @return Returns the ts.
   */
  public long getTs()
  {
    return ts;
  }

  /**
   * @return Returns the tuple.
   */
  public ITuplePtr getTuple()
  {
    return tuple;
  }

  /**
   * @return Returns the tupleValue.
   */
  public TupleValue getTupleValue()
  {
    return tupleValue;
  }

  /**
   * @param kind
   *          The kind to set.
   */
  public void setKind(Kind kind)
  {
    this.kind = kind;
  }

  /**
   * @param ts
   *          The ts to set.
   */
  public void setTs(long ts)
  {
    this.ts = ts;
  }

  /**
   * @param tuple
   *          The tuple to set.
   */
  public void setTuple(ITuplePtr tuple)
  {
    this.tuple = tuple;
  }

  /**
   * @param tuple
   *          The tuple to set.
   */
  public void setTupleValue(TupleValue tupleValue)
  {
    this.tupleValue = tupleValue;
  }
  
  public void heartBeat(long t)
  {
    kind = Kind.E_HEARTBEAT;
    tuple = null;
    ts = t;
  }

  /**
   * Get pinned tuple
   */
  public ITuple getPinnedTuple()
  {
    if (tuple == null)
      return null;
    try 
    {
      return tuple.pinTuple(IPinnable.READ);
    }
    catch (ExecException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }
    return null;
  }
  
  public String toString()
  {
    StringBuilder tempStr = new StringBuilder(); 
    tempStr.append(hashCode());
    tempStr.append(" : kind=");
    tempStr.append((kind == null) ? "" : kind.toString());
    tempStr.append(" ");
    tempStr.append(tuple == null ? "null" : tuple.toString());
    tempStr.append(" ts=");
    tempStr.append(ts);
    tempStr.append(" totalOrderingGuarantee=");
    tempStr.append(totalOrderingGuarantee);
    tempStr.append(" snapshotId=");
    tempStr.append(snapshotId);
    return tempStr.toString();
  }

  public boolean equals(Object other)
  {
    if (other == null) return false;
    //check for self-comparison
    if (this == other)
      return true;
    if (!(other instanceof QueueElement))
      return false;

    QueueElement ol = (QueueElement) other;
    if (kind != ol.getKind()) return false;
    if (ts != ol.getTs()) return false;
    if (tuple == null && ol.getTuple() == null) return true;
    if(totalOrderingGuarantee != ol.getTotalOrderingGuarantee()) return false;
    if(snapshotId != ol.getSnapshotId()) return true;
    return (tuple != null ? tuple.equals(ol.getTuple()) : false);
  }

  public boolean getTotalOrderingGuarantee()
  {
    return this.totalOrderingGuarantee;
  }

  public void setTotalOrderingGuarantee(boolean isGuaranteed)
  {
    this.totalOrderingGuarantee = isGuaranteed;
  }

  @Override
  public void writeExternal(ObjectOutput out, IPersistenceContext ctx)
      throws IOException
  {
    writeExternal(out);
  }

  @Override
  public void readExternal(ObjectInput in, IPersistenceContext ctx)
      throws IOException, ClassNotFoundException
  {
    readExternal(in);
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeObject(this.kind);
    out.writeObject(this.tuple);
    out.writeObject(this.tupleValue);
    out.writeLong(this.ts);
    out.writeBoolean(this.totalOrderingGuarantee);
    out.writeLong(this.snapshotId);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    this.kind = (Kind) in.readObject();
    this.tuple = (ITuplePtr) in.readObject();
    this.tupleValue = (TupleValue) in.readObject();
    this.ts = in.readLong();
    this.totalOrderingGuarantee = in.readBoolean();
    this.snapshotId = in.readLong();
  }

}

