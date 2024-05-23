/* $Header: SinglyList.java 03-mar-2008.09:54:46 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 SinglyList is the memory version of ISinglyList implementation.

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark       02/28/08 - add Tuple typed lists
 hopark       10/31/07 - Creation
 */

/**
 *  @version $Header: SinglyList.java 03-mar-2008.09:54:46 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.paged;

import java.util.logging.Level;

import oracle.cep.dataStructures.internal.IListNode;
import oracle.cep.dataStructures.internal.ISinglyList;
import oracle.cep.dataStructures.internal.ISinglyListIter;
import oracle.cep.dataStructures.internal.ISinglyListNode;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.memmgr.PageLayout;
import oracle.cep.memmgr.IAllocator.NameSpace;
import oracle.cep.memmgr.PageLayout.LayoutDesc;
import oracle.cep.memmgr.PageManager.PageRef;

public class SinglyList<E> 
  extends List<E>
  implements ISinglyList<E>
{
  protected static final int MAX_SINGLY_LIST_POS = ELEM_POS + 1;
  
  public static LayoutDesc s_layoutDesc;
  static 
  {
    s_layoutDesc =  new LayoutDesc(MAX_SINGLY_LIST_POS, 0);
    initLayout(s_layoutDesc);
  }

  protected static void initLayout(LayoutDesc layout)
  {
    layout.setType(NEXT_POS, PageLayout.LONG);
    layout.setType(ELEM_POS, PageLayout.OBJ);
  }
 
  public SinglyList()
  {
    super();
  }

  protected NameSpace getNameSpace() 
  {
    return NameSpace.SNODE;
  }
    
  protected LayoutDesc getPageLayoutDesc() {return s_layoutDesc;}
    
  public ListNode<E> createNode() {return new SinglyListNode<E>(this);}

  /**
   * Removes the first element in the singly linked list
   */
  public E remove()
    throws ExecException
  {
    return remove(false);
  }

  /**
   * Removes the first element in the singly linked list
   */
  public E remove(boolean freeMem) 
    throws ExecException
  {
    if (m_head.isNull())
      return null;
    
    E elem = m_head.getNodeElem();
    removeNode(m_head, freeMem);
    return elem;
  }
  
  protected void removeNode(ListNode<E> e, boolean freeNode)
    throws ExecException
  {
    assert (e.equals(m_head));
    ListNode<E> n = createNode();
    n.copy(m_head);
    
    if (e.equals(m_tail))
    {
      m_head.clear();
      m_tail.clear();
      m_size = 0;
    }
    else
    {
      m_head.getNext(m_head);
      m_size--;
    }
    if (freeNode)
    {
      n.free(m_factory);
    }
  }

  /**
   * Add the last element from the list to the this list without allocatin
   * memory for the node
   *
   * @param list
   *          The list to allocate memory from
   */
  @SuppressWarnings("unchecked")
  public void addLast(ISinglyList<E> l)
    throws ExecException
  {
    SinglyList<E> list = (SinglyList<E>) l;
    ListNode<E> listTail = list.m_tail;
    if (m_head.isNull())
    {
      assert m_tail.isNull();
      assert m_size == 0;
      m_head.copy(listTail);
      m_tail.copy(listTail);
    }
    else
    {
      m_tail.getNext(m_tail);
      assert m_tail.equals(listTail);
    }
     
    m_size++;
    //m_modCount.incrementAndGet();
  }

  @SuppressWarnings("unchecked")
  public void addNext(ISinglyList<E> l)
    throws ExecException
  {
    SinglyList<E> list = (SinglyList<E>) l;
    if (!m_tail.isNull())
    {
      m_tail.getNext(m_tail);
      assert !m_tail.isNull();
      m_size++;
    }
    else
    {
      assert m_head.isNull();
      m_head.copy(list.m_head);
      m_tail.copy(m_head);
      m_size = 1;
    }
    //m_modCount.incrementAndGet();
  }

  /**
   * Adds an element in the singly linked list. Element will be inserted in the
   * end.
   * 
   * @param elem
   *          Element to be inserted in the list.
   */
  public void add(E elem) throws ExecException
  {
    addElem(elem);
  }
  
  @SuppressWarnings("unchecked")
  protected <T extends IListNode<E>> T addElem(E elem) throws ExecException
  {
    PageRef newElem = m_factory.allocate();
    ListNode<E> n = createNode();
    n.set(newElem, IPinnable.WRITE);
    n.setNodeElem(elem);
    n.setNext(null);
    
    add(n);
    return (T) n;
  }


  private void add(IListNode<E> n)
  {
    ListNode<E> elem = (ListNode<E>) n;
    try
    {
      if (m_head.isNull())
      {
        assert m_tail.isNull();
        m_head.copy(elem);
        m_tail.copy(elem);
      }
      else
      {
        m_tail.setNext(elem);
        m_tail.copy(elem);
      }
      
      m_size++;
      //m_modCount.incrementAndGet();
    }
    catch(ExecException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }
  }

  @SuppressWarnings("unchecked")
  public boolean isTailSame(ISinglyList<E> list)
    throws ExecException
  {
    ListNode<E> ltail = ((SinglyList<E>)list).m_tail;
    return m_tail.equals(ltail) ? true : false;
  }

  public static class SinglyListNode<E> 
    extends List.ListNode<E> 
    implements ISinglyListNode<E>
  {
    public SinglyListNode(List<E> list)
    {
      super(list);
    }

    protected void getDesc(StringBuilder buf)
      throws ExecException
    {
      super.getDesc(buf);
      buf.append(",");
      E obj = getNodeElem();
      buf.append(obj == null ? "null" : obj.toString());
    }
  }
  
  public static class SinglyListIter<E>
    extends ListIter<E>
    implements ISinglyListIter<E>
  {
  }

}

