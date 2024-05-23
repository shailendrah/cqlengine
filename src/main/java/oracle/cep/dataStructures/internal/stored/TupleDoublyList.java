/* $Header: TupleDoublyList.java 03-mar-2008.13:31:59 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
   Partition is the stored version of IPartition implementation.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      11/03/07 - Creation
 */

/**
 *  @version $Header: TupleDoublyList.java 03-mar-2008.13:31:59 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.stored;

import oracle.cep.dataStructures.internal.ITupleDoublyList;
import oracle.cep.dataStructures.internal.ITupleDoublyListIter;
import oracle.cep.dataStructures.internal.ITupleDoublyListNode;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.paged.TuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPagePtr;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.memmgr.IAllocator.NameSpace;
import oracle.cep.memmgr.PageLayout.LayoutDesc;
import oracle.cep.memmgr.factory.paged.TupleFactory;

public class TupleDoublyList 
  extends DoublyList<ITuplePtr>
  implements ITupleDoublyList
{
  protected static final int TUPLE_POS = oracle.cep.dataStructures.internal.paged.TupleDoublyList.TUPLE_POS;

  protected TupleFactory m_tupleFac;
  
  public TupleDoublyList()
  {
    super();
  }

  protected NameSpace getNameSpace() 
  {
    return NameSpace.NODE;
  }
  
  protected LayoutDesc getPageLayoutDesc() 
  {
    return oracle.cep.dataStructures.internal.paged.TupleDoublyList.s_layoutDesc;
  }

  public ListNode<ITuplePtr> createNode() {return new TupleDoublyListNode(this);}

  @SuppressWarnings("unchecked")
  public void setTupleFactory(IAllocator<ITuplePtr> fac) 
  {
    m_tupleFac = (TupleFactory) fac;    
  }
  
  public IAllocator<ITuplePtr> getTupleFactory() {return m_tupleFac;}
  
  /**
   * It is important to extend stored.DoublyList.DoublyListNode.
   * It cannot extend memory.Partition.PartitionNode.
   * If we do, we will get ClassCastException in DoublyList.
   * It's because stored.DoublyList extends stored.DoublyList
   * an stored.ToublyList is expecting stored.DoublyList.DoublyListNode.
   */
  public static class TupleDoublyListNode 
    extends DoublyList.DoublyListNode<ITuplePtr>
    implements ITupleDoublyListNode
  {
    protected TupleFactory m_tupleFac;

    public TupleDoublyListNode(oracle.cep.dataStructures.internal.paged.List<ITuplePtr> list)
    {
      super(list);
      assert (list instanceof ITupleDoublyList);
      m_tupleFac = (TupleFactory) ((ITupleDoublyList)list).getTupleFactory();
      assert (m_tupleFac != null);
    }

    protected void freeElem()
      throws ExecException
    {
    }

    public void setNodeElem(ITuplePtr tuplePtr) throws ExecException
    {
      long tupleId = tuplePtr == null ? -1 : tuplePtr.getId();
      m_page.lValueSet(m_index, TUPLE_POS, tupleId);
    }

    public ITuplePtr getNodeElem() throws ExecException
    {
      long tupleId = m_page.lValueGet(m_index, TUPLE_POS);
      if (tupleId < 0)
        return null;
      return m_tupleFac.get(tupleId);
    }

    protected void getDesc(StringBuilder buf)
      throws ExecException
    {
      super.getDesc(buf);
      ITuplePtr tuple = getNodeElem();
      buf.append(",tuple=");
      buf.append(tuple);
    }
   }
  
  public static class TupleDoublyListIter 
    extends DoublyList.DoublyListIter<ITuplePtr>
    implements ITupleDoublyListIter
  {
  }
}
