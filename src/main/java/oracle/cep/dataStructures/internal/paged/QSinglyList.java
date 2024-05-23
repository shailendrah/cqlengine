/* $Header: pcbpel/cep/server/src/oracle/cep/dataStructures/internal/paged/QSinglyList.java /main/5 2009/05/29 19:35:21 hopark Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
   QSinglyList is the memory version of IQSinglyList implementation.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      05/18/09 - fix tsorder
    hopark      02/28/08 - tupleptr serialization optimization
    hopark      01/30/08 - optimize
    hopark      01/25/08 - return new node
    hopark      10/31/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/dataStructures/internal/paged/QSinglyList.java /main/5 2009/05/29 19:35:21 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.paged;

import oracle.cep.dataStructures.internal.IQSinglyList;
import oracle.cep.dataStructures.internal.IQSinglyListNode;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.internal.ISinglyList;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.QueueElement.Kind;
import oracle.cep.execution.ExecException;
import oracle.cep.memmgr.PageLayout;
import oracle.cep.memmgr.IAllocator.NameSpace;
import oracle.cep.memmgr.PageLayout.LayoutDesc;

public class QSinglyList extends TupleSinglyList
  implements IQSinglyList
{
  public static final int TUPLE_POS = ELEM_POS;
  public static final int KIND_POS = ELEM_POS + 1;
  public static final int TS_POS = ELEM_POS + 2;
  public static final int READER_POS = ELEM_POS + 3;
  public static final int TSORDER_POS = ELEM_POS + 4;
  protected static final int MAX_QSINGLY_LIST_POS = ELEM_POS + 5;
  
  public static LayoutDesc s_layoutDesc;
  static 
  {
    s_layoutDesc =  new LayoutDesc(MAX_QSINGLY_LIST_POS, 0);

    s_layoutDesc.setType(NEXT_POS, PageLayout.LONG);
    s_layoutDesc.setType(TUPLE_POS, PageLayout.LONG);
    s_layoutDesc.setType(KIND_POS, PageLayout.INT);
    s_layoutDesc.setType(TS_POS, PageLayout.LONG);
    s_layoutDesc.setType(READER_POS, PageLayout.INT);
    s_layoutDesc.setType(TSORDER_POS, PageLayout.BOOLEAN);
  }
  
  public QSinglyList()
  {
    super();
  }
  
  protected NameSpace getNameSpace() 
  {
    return NameSpace.QNODE;
  }
    
  protected LayoutDesc getPageLayoutDesc() {return s_layoutDesc;}
    
  public ListNode<ITuplePtr> createNode() {return new QSinglyListNode(this);}
  
  public IQSinglyListNode add(QueueElement elem) throws ExecException
  {
    IQSinglyListNode node = addElem(elem.getTuple());
    node.setKind(elem.getKind());
    node.setTs(elem.getTs());
    node.setTotalOrderingGuarantee(elem.getTotalOrderingGuarantee());
    return node;
  }

  public IQSinglyListNode add() throws ExecException
  {
    IQSinglyListNode node = addElem(null);
    return node;
  }

  public QueueElement getFirstElem(QueueElement buf) throws ExecException
  {
    QSinglyListNode node = getHead();
    if (node == null) return null;
    node.get(buf);
    return buf;  
  }
  
  public QueueElement removeElem(QueueElement buf) throws ExecException
  {
    QSinglyListNode node = getHead();
    if (node == null) return null;
    if (buf != null)
      node.get(buf);
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
  
  /*
   * QSinglyListNode in page mode cannot be QueueElement,
   * because the entry in the page will be remove on removeNode
   * and the node will not be valid.
   */
  public static class QSinglyListNode 
    extends TupleSinglyListNode
    implements IQSinglyListNode
  {
    public QSinglyListNode(List<ITuplePtr> list)
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

    public void setNodeElem(ITuplePtr tuplePtr) throws ExecException
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
      return buf;
    }
    
    public void set(QueueElement elem, int readers) 
      throws ExecException
    {
      setKind(elem.getKind());
      setTs(elem.getTs());
      setNodeElem(elem.getTuple());
      setReaders(readers);
      setTotalOrderingGuarantee(elem.getTotalOrderingGuarantee());
    }
    
    protected void getDesc(StringBuilder buf)
      throws ExecException
    {
      super.getDesc(buf);
      buf.append(",kind=");
      Kind kind = getKind();
      String kstr = (kind != null) ? kind.toString() : "null";
      buf.append(kstr);
      buf.append(",ts=");
      long ts = getTs();
      buf.append(ts);
      buf.append(",tsorder=");
      boolean tsorder = getTotalOrderingGuarantee();
      buf.append(tsorder);
    }
  }

}

