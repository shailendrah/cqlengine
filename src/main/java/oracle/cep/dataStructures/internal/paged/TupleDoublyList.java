/* $Header: TupleDoublyList.java 03-mar-2008.13:31:54 hopark Exp $ */

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
import oracle.cep.dataStructures.internal.IDoublyListIter;
import oracle.cep.dataStructures.internal.ITupleDoublyListIter;
import oracle.cep.dataStructures.internal.ITupleDoublyListNode;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.PageLayout;
import oracle.cep.memmgr.IAllocator.NameSpace;
import oracle.cep.memmgr.PageLayout.LayoutDesc;
import oracle.cep.memmgr.factory.paged.TupleFactory;

/**
 * @version $Header: TupleDoublyList.java 03-mar-2008.13:31:54 hopark Exp $
 * @author parujain
 * @since release specific (what release of product did this appear in)
 */
public class TupleDoublyList 
  extends DoublyList<ITuplePtr>
  implements ITupleDoublyList
{
  public static final int TUPLE_POS = ELEM_POS;
  protected static final int MAX_TDOUBLY_LIST_POS = MAX_POS;
  
  protected TupleFactory m_tupleFac;

  public static LayoutDesc s_layoutDesc;
  static 
  {
    s_layoutDesc =  new LayoutDesc(MAX_TDOUBLY_LIST_POS, 0);
    initLayout(s_layoutDesc);
  }
  
  protected static void initLayout(LayoutDesc layout)
  {
    layout.setType(PREV_POS, PageLayout.LONG);
    layout.setType(NEXT_POS, PageLayout.LONG);
    layout.setType(TUPLE_POS, PageLayout.LONG);
  }
  
  public TupleDoublyList()
  {
    super();
  }

  protected NameSpace getNameSpace() 
  {
    return NameSpace.NODE;
  }
  
  protected LayoutDesc getPageLayoutDesc() {return s_layoutDesc;}
  
  public ListNode<ITuplePtr> createNode() {return new TupleDoublyListNode(this);}
  
  @SuppressWarnings("unchecked")
  public void setTupleFactory(IAllocator<ITuplePtr> fac) 
  {
    m_tupleFac = (TupleFactory) fac;    
  }
  
  public IAllocator<ITuplePtr> getTupleFactory() {return m_tupleFac;}
    
  public static class TupleDoublyListNode 
    extends DoublyList.DoublyListNode<ITuplePtr> 
    implements ITupleDoublyListNode
  {
    protected TupleFactory m_tupleFac;
    
    public TupleDoublyListNode(List<ITuplePtr> list)
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

  public static class TupleDoublyListIter 
    extends ListIter<ITuplePtr> 
    implements ITupleDoublyListIter
  {
  }
}
