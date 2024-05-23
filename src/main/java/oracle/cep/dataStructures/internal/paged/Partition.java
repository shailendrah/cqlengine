/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/paged/Partition.java /main/4 2012/06/20 05:24:30 pkali Exp $ */

/* Copyright (c) 2007, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
   QSinglyList is the memory version of ISinglyList4 implementation.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       05/30/12 - added windows size data member
    hopark      02/28/08 - tupleptr serialization optimization
    hopark      11/03/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/paged/Partition.java /main/4 2012/06/20 05:24:30 pkali Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.paged;

import oracle.cep.dataStructures.internal.IPartition;
import oracle.cep.dataStructures.internal.IPartitionIter;
import oracle.cep.dataStructures.internal.IPartitionNode;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.memmgr.PageLayout;
import oracle.cep.memmgr.IAllocator.NameSpace;
import oracle.cep.memmgr.PageLayout.LayoutDesc;

public class Partition extends TupleDoublyList
  implements IPartition
{
  public static final int TUPLE_POS = MAX_DOUBLY_LIST_POS + 1;
  public static final int TS_POS = TUPLE_POS + 1;
  protected static final int MAX_PARTITON_LIST_POS = TS_POS + 1;

  /** Maintains the partition window size.
   * The list size may not represent the correct window size 
   * in the scenario of memory optimization where tuple is 
   * not added to the list, hence a separate counter is maintained
   */
  private int windowSize;
  
  public static LayoutDesc s_layoutDesc;
  static 
  {
    s_layoutDesc =  new LayoutDesc(MAX_PARTITON_LIST_POS, 0);

    s_layoutDesc.setType(PREV_POS, PageLayout.LONG);
    s_layoutDesc.setType(NEXT_POS, PageLayout.LONG);
    s_layoutDesc.setType(TUPLE_POS, PageLayout.LONG);
    s_layoutDesc.setType(TS_POS, PageLayout.LONG);
  }
  
  public Partition()
  {
    super();
  }

  public void incrementWindowSize()
  {
    ++windowSize;
  }
  
  public void decrementWindowSize()
  {
    --windowSize;
  }
  
  public int getWindowSize()
  {
    return windowSize;
  }
  
  protected NameSpace getNameSpace() 
  {
    return NameSpace.PARTNNODE;
  }
  
  protected LayoutDesc getPageLayoutDesc() {return s_layoutDesc;}
  
  public ListNode<ITuplePtr> createNode() {return new PartitionNode(this);}
  
  @SuppressWarnings("unchecked")
  public <T extends IPartitionNode> T  add(ITuplePtr elem, long ts) throws ExecException
  {
    IPartitionNode node4 = add(elem);
    node4.setTs(ts);
    return (T) node4;
  }

  public static class PartitionNode 
    extends TupleDoublyListNode
    implements IPartitionNode
  {
    public PartitionNode(List<ITuplePtr> list)
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

    public void setNodeElem(ITuplePtr tuplePtr) throws ExecException
    {
      long tupleId = tuplePtr == null ? -1 : tuplePtr.getId();
      m_page.lValueSet(m_index, TUPLE_POS, tupleId);
    }

    protected void freeElem()
      throws ExecException
    {
    }
    
    public void setTs(long ts) throws ExecException
    {
      m_page.lValueSet(m_index, TS_POS, ts);
    }

    public long getTs() throws ExecException
    {
      long ts =m_page.lValueGet(m_index, TS_POS);
      return ts;
    }

    protected void getDesc(StringBuilder buf)
      throws ExecException
    {
      super.getDesc(buf);
      long ts = getTs();
      buf.append(",ts=");
      buf.append(ts);
    }
  }

  public static class PartitionIter 
    extends DoublyList.DoublyListIter<ITuplePtr>
    implements IPartitionIter
  {
      @SuppressWarnings("unchecked")
      public long getTs() throws ExecException
      {
        assert (m_current != null);
        PartitionNode node = (PartitionNode) m_current;
        return node.getTs();
      }
  }
}

