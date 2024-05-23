/* $Header: DoublyList.java 06-mar-2008.11:05:20 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 DoublyList is the memory version of IDoublyList implementation.

 PRIVATE CLASSES

 NOTES

 MODIFIED    (MM/DD/YY)
 hopark       02/28/08 - add Tuple typed lists
 hopark       10/31/06 - Creation
 */
package oracle.cep.dataStructures.internal.paged;

import oracle.cep.dataStructures.internal.IDoublyList;
import oracle.cep.dataStructures.internal.IDoublyListIter;
import oracle.cep.dataStructures.internal.IDoublyListNode;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.DumpDesc;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.memmgr.PageLayout;
import oracle.cep.memmgr.IAllocator.NameSpace;
import oracle.cep.memmgr.PageLayout.LayoutDesc;
import oracle.cep.memmgr.PageManager.PageRef;
import oracle.cep.util.DebugLogger;
import oracle.cep.util.DebugUtil;

/**
 * @version $Header: DoublyList.java 06-mar-2008.11:05:20 hopark Exp $
 * @author parujain
 * @since release specific (what release of product did this appear in)
 */
public class DoublyList<E> 
  extends List<E>
  implements IDoublyList<E>
{
  protected static final int MAX_DOUBLY_LIST_POS = MAX_POS + 1;
  public static LayoutDesc s_layoutDesc;
  static 
  {
    s_layoutDesc =  new LayoutDesc(MAX_DOUBLY_LIST_POS, 0);
    initLayout(s_layoutDesc);
  }

  protected static void initLayout(LayoutDesc layout)
  {
    layout.setType(PREV_POS, PageLayout.LONG);
    layout.setType(NEXT_POS, PageLayout.LONG);
    layout.setType(ELEM_POS, PageLayout.OBJ);
  }

  public DoublyList()
  {
    super();
  }

  protected NameSpace getNameSpace() 
  {
    return NameSpace.NODE;
  }
  
  protected LayoutDesc getPageLayoutDesc() {return s_layoutDesc;}
  
  public ListNode<E> createNode() {return new DoublyListNode<E>(this);}
  
  /**
   * Adds an element in the doubly linked list. Element will be inserted in the
   * end.
   * 
   * @param elem
   *          Element to be inserted in the list.
   */
  @SuppressWarnings("unchecked")
  public <T extends IDoublyListNode<E>> T add(E elem)
      throws ExecException
  {
    PageRef newElem = m_factory.allocate();
    ListNode<E> n = createNode();
    n.set(newElem, IPinnable.WRITE);
    n.setNodeElem(elem);
    n.setNext(null);
    
    if (m_head.isNull())
    {
      assert m_tail.isNull();
      n.setPrev(null);
      m_head.copy(n);
      m_tail.copy(n);
    } 
    else
    {
      m_tail.setNext(n);
      n.setPrev(m_tail);
      m_tail.copy(n);
    }
    m_size++;
    //m_modCount.incrementAndGet();

    if (DebugUtil.DEBUG_PAGEDLIST_VERIFY)
      verify();
    if (DebugUtil.DEBUG_PAGEDLIST_NODE)
    {
      if (m_id == DebugUtil.DEBUGI_PAGEDLIST_NODE_LISTID) {
        s_logger.log(m_id, this, "add", elem.toString(), toString());
        s_logger.print(m_id, DebugLogger.TRACE_HISTORY);
      }
    }
    
    return (T) n;
  }

  protected void removeNode(ListNode<E> node, boolean freeNode) throws ExecException
  {
    assert !m_head.isNull();
    assert !m_tail.isNull();
    if (DebugUtil.DEBUG_PAGEDLIST_VERIFY)
      verify();

    // we may removing either head or tail.
    // since we are manipulating head or tail directly, we need to copy the given node
    ListNode<E> e = createNode();
    e.copy(node);
    
    ListNode<E> n = createNode();
    ListNode<E> p = createNode();
    e.getNext(n);
    e.getPrev(p);
    e.pin(IPinnable.WRITE);
    n.pin(IPinnable.WRITE);
    p.pin(IPinnable.WRITE);
    if (!n.isNull())
    {
      //e.next.prev = e.prv;
      n.setPrev(p);
    } 
    else
    {
      assert (m_tail.equals(e));
      //m_tail = m_tail.prev;
      m_tail.getPrev(m_tail);
      if (!m_tail.isNull())
      {
        m_tail.setNext(null);
      }
    }
    if (!p.isNull())
    {
      //p.next = e.next;
      e.getNext(n);
      p.setNext(n);
    } 
    else 
    {
      assert (m_head.equals(e));
      //head=head.next;
      m_head.getNext(m_head);
      if (!m_head.isNull())
      {
        m_head.setPrev(null);
      }
    }

    m_size--;

    if (freeNode)
      e.free(m_factory);

    if (DebugUtil.DEBUG_PAGEDLIST_VERIFY)
      verify();
  }

  @DumpDesc(attribTags={"Id", "Prev", "Next"}, 
      attribVals={"getId", "getPrevId", "getNextId"},
      valueTags={"E"},
      values={"getNodeElem"})
  public static class DoublyListNode<E> 
    extends List.ListNode<E> 
    implements IDoublyListNode<E>
  {
    public DoublyListNode(List<E> list)
    {
      super(list);
    }

    public int getPrevId() 
    {
      long prev = -1L;
      if (m_page != null)
      {
        prev = m_page.lValueGet(m_index, PREV_POS);
      }
      return (int) prev;
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
      }
    }
  }

  public static class DoublyListIter<E> 
    extends ListIter<E> 
    implements IDoublyListIter<E>
  {
  }
}
