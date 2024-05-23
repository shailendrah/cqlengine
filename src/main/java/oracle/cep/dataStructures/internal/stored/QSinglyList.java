/* $Header: pcbpel/cep/server/src/oracle/cep/dataStructures/internal/stored/QSinglyList.java /main/7 2009/05/29 19:35:21 hopark Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
   QSinglyList is the stored version of IQSinglyList implementation.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      05/18/09 - fix tsorder
    hopark      02/28/08 - tupleptr serialization optimization
    hopark      01/30/08 - optimize
    hopark      01/25/08 - return new node
    hopark      12/27/07 - support xmllog
    hopark      11/03/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/dataStructures/internal/stored/QSinglyList.java /main/7 2009/05/29 19:35:21 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.stored;

import java.util.logging.Level;

import oracle.cep.dataStructures.internal.IQSinglyList;
import oracle.cep.dataStructures.internal.IQSinglyListNode;
import oracle.cep.dataStructures.internal.ISinglyList;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.QueueElement.Kind;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.memmgr.IAllocator.NameSpace;
import oracle.cep.memmgr.PageLayout.LayoutDesc;
import oracle.cep.util.DebugLogger;
import oracle.cep.util.DebugUtil;

@DumpDesc(attribTags={"Id", "Length"}, 
          attribVals={"getId", "getSize"})
public class QSinglyList 
  extends TupleSinglyList
  implements IQSinglyList
{
  protected static final int TUPLE_POS = oracle.cep.dataStructures.internal.paged.QSinglyList.TUPLE_POS;
  protected static final int KIND_POS = oracle.cep.dataStructures.internal.paged.QSinglyList.KIND_POS;
  protected static final int TS_POS = oracle.cep.dataStructures.internal.paged.QSinglyList.TS_POS;
  protected static final int READER_POS = oracle.cep.dataStructures.internal.paged.QSinglyList.READER_POS;
  protected static final int TSORDER_POS = oracle.cep.dataStructures.internal.paged.QSinglyList.TSORDER_POS;

  public QSinglyList()
  {
    super();
  }

  protected NameSpace getNameSpace() 
  {
    return NameSpace.QNODE;
  }
    
  protected LayoutDesc getPageLayoutDesc() 
  {
    return oracle.cep.dataStructures.internal.paged.QSinglyList.s_layoutDesc;
  }
  
  public ListNode<ITuplePtr> createNode() {return new QSinglyListNode(this);}

  public IQSinglyListNode add(QueueElement elem) throws ExecException
  {
    m_head.pin(IPinnable.WRITE);
    m_tail.pin(IPinnable.WRITE);
    IQSinglyListNode node = addElem(elem.getTuple());
    node.setKind(elem.getKind());
    node.setTs(elem.getTs());
    node.setTotalOrderingGuarantee(elem.getTotalOrderingGuarantee());
    return node;
  }
  
  public IQSinglyListNode add() throws ExecException
  {
    m_head.pin(IPinnable.WRITE);
    m_tail.pin(IPinnable.WRITE);
    IQSinglyListNode node = addElem(null);
    return node;
  }
  
  public QueueElement getFirstElem(QueueElement buf) throws ExecException
  {
    m_head.pin(IPinnable.READ);
    QSinglyListNode node = getHead();
    if (node == null) return null;
    node.get(buf);
    return buf;  
  }
  public QueueElement removeElem(QueueElement buf) throws ExecException
  {
    m_head.pin(IPinnable.WRITE);
    QSinglyListNode node = getHead();
    if (node == null) return null;
    if (buf != null)
      node.get(buf);
    m_tail.pin(IPinnable.WRITE);
    removeNode(m_head, true);
    return buf;  
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
  
  /**
   * It is important to extend stored.SinglyList.SinglyListNode.
   * It cannot extend memory.QSinglyList.QSinglyListNode.
   * If we do, we will get ClassCastException in QSinglyList.
   * It's because stored.SinglyList extends stored.SinglyList
   * an stored.SinglyList is expecting stored.SinglyList.SinglyListNode.
   */
  public static class QSinglyListNode 
    extends TupleSinglyListNode
    implements IQSinglyListNode
  {
    public QSinglyListNode(oracle.cep.dataStructures.internal.paged.List<ITuplePtr> list)
    {
      super(list);
    }

    public ITuplePtr getNodeElem() throws ExecException
    {
      long tupleId = m_page.lValueGet(m_index, TUPLE_POS);
      if (tupleId < 0)
        return null;
      return m_tupleFac.get(tupleId);
    }

    public void setTs(long ts)
    {
      m_page.lValueSet(m_index, TS_POS, ts);
    }

    public void setNodeElem(ITuplePtr tuplePtr)  throws ExecException
    {
      long tupleId = tuplePtr == null ? -1 : tuplePtr.getId();
      m_page.lValueSet(m_index, TUPLE_POS, tupleId);
    }

    protected void freeElem()
      throws ExecException
    {
    }
    
    public Kind getKind()
    {
      int kind = m_page.iValueGet(m_index, KIND_POS);
      return Kind.fromOrdinal(kind);
    }

    public long getTs()
    {
      long ts =m_page.lValueGet(m_index, TS_POS);
      return ts;
    }

    public boolean getTotalOrderingGuarantee()
    {
      boolean tsorder =m_page.boolValueGet(m_index, TSORDER_POS);
      return tsorder;
    }
    
    public void setKind(Kind kind)
    {
      m_page.iValueSet(m_index, KIND_POS, kind.ordinal());
    }

    public void setReaders(int readers)
    {
      m_page.iValueSet(m_index, READER_POS, readers);
    }

    public int decAndGetReaders()
    {
      int readers = m_page.iValueGet(m_index, READER_POS);
      readers--;
      m_page.iValueSet(m_index, READER_POS, readers);
      return readers;
    }

    public void setTotalOrderingGuarantee(boolean isGuaranteed)
    {
      m_page.boolValueSet(m_index, TSORDER_POS, isGuaranteed);
    }
       
    public QueueElement get(QueueElement buf) throws ExecException
    {
      buf.setKind(getKind());
      buf.setTs(getTs());
      buf.setTuple(getNodeElem());
      buf.setTotalOrderingGuarantee(getTotalOrderingGuarantee());
      if (DebugUtil.DEBUG_PAGEDLIST_NODE)
      {
        if (buf.getKind() != QueueElement.Kind.E_HEARTBEAT &&
              buf.getTuple() == null)
        {
          long tupleId = m_page.lValueGet(m_index, TUPLE_POS);
          s_logger.log(getId(), this, "getQueue", toString(), "tupleId="+tupleId);
          s_logger.print(getId(), DebugLogger.TRACE_HISTORY);
        }
      }
      assert (buf.getKind() == QueueElement.Kind.E_HEARTBEAT ||
              buf.getTuple() != null);
      return buf;
    }
    
    public void set(QueueElement elem, int readers) 
      throws ExecException
      {
      try
      {
        pin(IPinnable.WRITE);
        setKind(elem.getKind());
        setTs(elem.getTs());
        setNodeElem(elem.getTuple());
        setReaders(readers);
        setTotalOrderingGuarantee(elem.getTotalOrderingGuarantee());
      }
      catch(ExecException e)
      {
        LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      }
    }
  }
}
