/* $Header: TupleSinglyList.java 03-mar-2008.13:31:54 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 DoublyList is the memory version of IDoublyList implementation.

 PRIVATE CLASSES

 NOTES

 MODIFIED    (MM/DD/YY)
 hopark       10/31/06 - Creation
 */
package oracle.cep.dataStructures.internal.paged;

import oracle.cep.dataStructures.internal.ITupleDoublyList;
import oracle.cep.dataStructures.internal.ITupleSinglyList;
import oracle.cep.dataStructures.internal.ITupleSinglyListIter;
import oracle.cep.dataStructures.internal.ITupleSinglyListNode;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.PageLayout;
import oracle.cep.memmgr.IAllocator.NameSpace;
import oracle.cep.memmgr.PageLayout.LayoutDesc;
import oracle.cep.memmgr.factory.paged.TupleFactory;

/**
 * @version $Header: TupleSinglyList.java 03-mar-2008.13:31:54 hopark Exp $
 * @author parujain
 * @since release specific (what release of product did this appear in)
 */
public class TupleSinglyList 
  extends SinglyList<ITuplePtr>
  implements ITupleSinglyList
{
  public static final int TUPLE_POS = ELEM_POS;
  protected static final int MAX_TSINGLY_LIST_POS = TUPLE_POS + 1;
  
  protected TupleFactory m_tupleFac;
  
  public static LayoutDesc s_layoutDesc;
  static 
  {
    s_layoutDesc =  new LayoutDesc(MAX_TSINGLY_LIST_POS, 0);
    initLayout(s_layoutDesc);
  }
  
  protected static void initLayout(LayoutDesc layout)
  {
    layout.setType(NEXT_POS, PageLayout.LONG);
    layout.setType(TUPLE_POS, PageLayout.LONG);
  }
  
  public TupleSinglyList()
  {
    super();
  }

  protected NameSpace getNameSpace() 
  {
    return NameSpace.NODE;
  }
  
  protected LayoutDesc getPageLayoutDesc() {return s_layoutDesc;}
  
  public ListNode<ITuplePtr> createNode() {return new TupleSinglyListNode(this);}
  
  @SuppressWarnings("unchecked")
  public void setTupleFactory(IAllocator<ITuplePtr> fac) 
  {
    m_tupleFac = (TupleFactory) fac;    
  }
  
  public IAllocator<ITuplePtr> getTupleFactory() {return m_tupleFac;}
  
  
  public static class TupleSinglyListNode 
    extends SinglyList.SinglyListNode<ITuplePtr> 
    implements ITupleSinglyListNode
  {
    protected TupleFactory m_tupleFac;
    
    public TupleSinglyListNode(List<ITuplePtr> list)
    {
      super(list);
      assert (list instanceof ITupleSinglyList);
      m_tupleFac = (TupleFactory) ((ITupleSinglyList)list).getTupleFactory();
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
      buf.append(",");
      if (m_page != null)
      {
        long prev = m_page.lValueGet(m_index, PREV_POS);
        buf.append("prev=");
        buf.append(prev);
        long tuple = m_page.lValueGet(m_index, TUPLE_POS);
        buf.append("tuple=");
        buf.append(tuple);
      }
    }
  }

  public static class TupleSinglyListIter 
    extends ListIter<ITuplePtr> 
    implements ITupleSinglyListIter
  {
  }
}
