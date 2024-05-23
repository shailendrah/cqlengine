/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/QSinglyList.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2007, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
   QSinglyList is the memory version of ISinglyList4 implementation.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    08/13/13 - set snapshotId
    udeshmuk    04/17/12 - add snapshot
    anasrini    03/22/11 - add method getTupleValue, setTupleValue
    sbishnoi    03/24/09 - piggyback optimization: fix ordering flag
    sbishnoi    01/19/09 - total ordering optimization
    hopark      02/28/08 - resurrect refcnt
    hopark      01/25/08 - return new node
    hopark      12/27/07 - support xmllog
    hopark      01/03/08 - remove refcnt
    hopark      12/07/07 - cleanup spill
    hopark      11/03/07 - remove getNodeStr
    hopark      10/30/07 - remove IQueueElement
    hopark      10/18/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/QSinglyList.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.memory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.logging.Level;

import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.IQSinglyList;
import oracle.cep.dataStructures.internal.IQSinglyListNode;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.ISinglyList;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.util.DebugUtil;

@DumpDesc(attribTags={"Id", "Length"}, 
          attribVals={"getId", "getSize"})
public class QSinglyList extends TupleSinglyList
  implements IQSinglyList
{

 /**
   * Empty Constructor
   * 
   */
  public QSinglyList()
  {
    super();
  }
  
  public QSinglyListNode add(QueueElement elem) throws ExecException
  {
    QSinglyListNode node = addElem(elem.getTuple());
    node.setKind(elem.getKind());
    node.setTs(elem.getTs());
    node.setSnapshotId(elem.getSnapshotId());
    return node;
  }  

  public QSinglyListNode add() throws ExecException
  {
    QSinglyListNode node = addElem(null);
    return node;
  }  

  public QueueElement getFirstElem(QueueElement buf) throws ExecException
  {
    QSinglyListNode node = getHead();
    if (node == null) return null;
    return node;  
  }
  
  public QueueElement removeElem(QueueElement buf) throws ExecException
  {
    QSinglyListNode node = removeNode();
    if (node == null) return null;
    nFactory.release(node);
    return node;  
  }
  
  @SuppressWarnings("unchecked")
  public void addLast(IQSinglyList list) throws ExecException
  {
    super.addLast((ISinglyList<ITuplePtr>) list);
  }
  
  @SuppressWarnings("unchecked")
  public void addNext(IQSinglyList list) throws ExecException
  {
    super.addNext((ISinglyList<ITuplePtr>) list);
  }
  
  @DumpDesc(attribTags={"Id", "Next"}, 
            attribVals={"getId", "getNextId"},
            valueTags={"Tuple", "Kind", "Ts"},
            values={"getTuple", "getKind", "getTs"})
  public static class QSinglyListNode 
    extends TupleSinglyListNode
    implements IQSinglyListNode, QueueElement
  {
    private static final long serialVersionUID = -126028846486847888L;

    int kind;
    long ts;
    int readers;

    TupleValue tv;

    long snapshotId = Long.MAX_VALUE;

    // flag to check whether the tuples will come in strictly increasing order
    // order of timestamp
    boolean isTotalOrderingGuarantee = false;

    public QSinglyListNode()
    {
      super();
      kind = 0;
      readers = 0;
      snapshotId = Long.MAX_VALUE;
    }
  
    public void setReaders(int n)
    {
      readers = n;
    }
    
    public int decAndGetReaders()
    {
      return --readers;
    }
    
    /**
     * Clears the node
     */
    public void clear()
    {
      super.clear();
      kind = 0;
      ts   = 0;
      snapshotId = Long.MAX_VALUE;
      isTotalOrderingGuarantee = false;
    }

    public ITuplePtr getTuple()
    {
      if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
      {
        try 
        {
          if (NodeElem != null)
            NodeElem.pinTuple(IPinnable.READ);
        }
        catch (ExecException e)
        {
          LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
        }
      }
      return NodeElem;
    }

    public ITuple getPinnedTuple()
    {
      if (NodeElem == null)
        return null;
      try 
      {
        return NodeElem.pinTuple(IPinnable.READ);
      }
      catch (ExecException e)
      {
        LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      }
      return null;
    }

    public long getSnapshotId()
    {
      return snapshotId;
    }

    public void setSnapshotId(long snapshotId)
    {
      this.snapshotId = snapshotId;
    }
    
    public void setTs(long ts)
    {
      this.ts = ts;
    }

    public void setTuple(ITuplePtr tuplePtr)
    {
      NodeElem = tuplePtr;
    }

    public Kind getKind()
    {
      return Kind.fromOrdinal(kind);
    }

    public long getTs()
    {
      return ts;
    }

    public void setKind(Kind kind)
    {
      this.kind = kind.ordinal();
    }

    public QueueElement get(QueueElement buf)
    {
      return this;
    }
    
    public void copy(QueueElement elem)
    {
      kind                     = elem.getKind().ordinal();
      NodeElem                 = elem.getTuple();
      ts                       = elem.getTs();
      isTotalOrderingGuarantee = elem.getTotalOrderingGuarantee();
      tv                       = elem.getTupleValue();
      snapshotId               = elem.getSnapshotId();
    }

    public void heartBeat(long t)
    {
      kind = Kind.E_HEARTBEAT.ordinal();
      NodeElem = null;
      ts = t;
      snapshotId = Long.MAX_VALUE;
    }
    
    public void set(QueueElement elem, int readers)
    {
      kind                     = elem.getKind().ordinal();
      NodeElem                 = elem.getTuple();
      ts                       = elem.getTs();
      this.readers             = readers;
      isTotalOrderingGuarantee = elem.getTotalOrderingGuarantee();
      tv                       = elem.getTupleValue();
      snapshotId               = elem.getSnapshotId();
    }

    public boolean getTotalOrderingGuarantee()
    {
      return isTotalOrderingGuarantee;
    }

    public void setTotalOrderingGuarantee(boolean isTotalOrderingGuarantee)
    {
      this.isTotalOrderingGuarantee = isTotalOrderingGuarantee;
    }

    public TupleValue getTupleValue()
    {
      return this.tv;
    }

    public void setTupleValue(TupleValue tv)
    {
      this.tv = tv;
    }

    public String toString()
    {
      StringBuilder buf = new StringBuilder();
      buf.append("next=");
      buf.append(next== null ? "null" : next.hashCode());
      buf.append(" elem=");
      String kstr = (kind >= 0) ? Kind.fromOrdinal(kind).toString() : "null";
      String tupleDesc = NodeElem == null ? "null" : NodeElem.toString();
      buf.append(" : kind=" + kstr + " " + tupleDesc + " ts=" + ts + 
        " isTotalOrderingGuarantee=" + isTotalOrderingGuarantee+
      " snapshotId="+snapshotId);
      return buf.toString();
    }

    @Override
    public void writeExternal(ObjectOutput out, IPersistenceContext ctx)
        throws IOException
    {
      throw new IOException("Invalid API Call for Serialization of object type " + this.getClass().getName());
    }

    @Override
    public void readExternal(ObjectInput in, IPersistenceContext ctx)
        throws IOException, ClassNotFoundException
    {
      throw new IOException("Invalid API Call for Deserialization of object type " + this.getClass().getName());
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
      out.writeInt(kind);
      out.writeLong(ts);
      out.writeInt(readers);
      out.writeLong(snapshotId);
      out.writeBoolean(isTotalOrderingGuarantee);
      out.writeObject(this.getNodeElem());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException
    {
      this.kind = in.readInt();
      this.ts = in.readLong();
      this.readers = in.readInt();
      this.snapshotId = in.readLong();
      this.isTotalOrderingGuarantee = in.readBoolean();
      this.NodeElem = (ITuplePtr) in.readObject();
    }
  }

}

