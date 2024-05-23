/* $Header: SinglyList.java 19-jun-2008.18:47:17 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 SinglyList is the stored version of ISinglyList implementation.

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      06/19/08 - logging refactor
 hopark      02/25/08 - Clear page reference on unpin
 hopark      12/26/07 - support xmllog
 hopark      12/26/07 - support xmllog
 hopark       12/06/07 - cleanup spill
 hopark       11/03/07 - Creation
 */

/**
 *  @version $Header: SinglyList.java 19-jun-2008.18:47:17 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.stored;

import oracle.cep.dataStructures.internal.IListNode;
import oracle.cep.dataStructures.internal.ISinglyList;
import oracle.cep.dataStructures.internal.ISinglyListIter;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.trace.LogTags;
import oracle.cep.memmgr.IPinnable;

/**
 *  @version $Header: SinglyList.java 19-jun-2008.18:47:17 hopark Exp $
 *  @author  najain
 *  @since   release specific (what release of product did this appear in)
 */
@DumpDesc(attribTags={"Id", "Length"}, 
    attribVals={"getId", "getSize"})
public class SinglyList<E> 
  extends oracle.cep.dataStructures.internal.paged.SinglyList<E>
{
  public SinglyList()
  {
    super();
  }

  public ListNode<E> createNode() {return new SinglyListNode<E>(this);}
  protected boolean isEvictable() {return true;}

  /**
   * Getter of the first element in the singly linked list
   * 
   * @return First Element
   */
  public E getFirst() throws ExecException
  {
    m_head.pin(IPinnable.READ);
    return super.getFirst();
  }

  /**
   * Gets the last element in the doubly linked list
   * 
   * @return Last element in the list
   */
  public E getLast() throws ExecException
  {
    m_head.pin(IPinnable.WRITE);
    m_tail.pin(IPinnable.READ);
    return super.getLast();
  }

  @SuppressWarnings("unchecked")
  public <T extends IListNode<E>> T getHead() throws ExecException
  {
    m_head.pin(IPinnable.READ);
    return (T) super.getHead();
  }

  @SuppressWarnings("unchecked")
  public <T extends IListNode<E>> T getTail() throws ExecException
  {
    m_tail.pin(IPinnable.READ);
    return (T) super.getTail();
  }
  
  public void clear() throws ExecException
  {
    m_head.pin(IPinnable.WRITE);
    m_tail.pin(IPinnable.WRITE);
    super.clear();
  }
  
  public synchronized void dump(IDumpContext dumper)
  {
    try
    {
      m_head.pin(IPinnable.READ);
      m_tail.pin(IPinnable.READ);
      super.dump(dumper);
    }
    catch(ExecException e)
    {
      dumper.writeln(LogTags.DUMP_ERR, e.toString());
    }
  }
  
  /**
   * Removes the first element in the singly linked list
   */
  public E remove() throws ExecException
  {
    m_head.pin(IPinnable.WRITE);
    m_tail.pin(IPinnable.WRITE);
    return super.remove();
  }

  /**
   * Removes the first element in the singly linked list
   */
  public E remove(boolean freeMem) throws ExecException
  {
    m_head.pin(IPinnable.WRITE);
    m_tail.pin(IPinnable.WRITE);
    return super.remove(freeMem);
  }

  /**
   * Add the last element from the list to the this list without allocatin
   * memory for the node
   *
   * @param list
   *          The list to allocate memory from
   */
  @SuppressWarnings("unchecked")
  public void addLast(ISinglyList<E> list) throws ExecException
  {
    m_head.pin(IPinnable.WRITE);
    m_tail.pin(IPinnable.WRITE);
    SinglyList<E> l = (SinglyList<E>) list;
    l.m_head.pin(IPinnable.READ);
    l.m_tail.pin(IPinnable.READ);
    super.addLast(list);
  }

  @SuppressWarnings("unchecked")
  public void addNext(ISinglyList<E> list) throws ExecException
  {
    m_head.pin(IPinnable.WRITE);
    m_tail.pin(IPinnable.WRITE);
    SinglyList<E> l = (SinglyList<E>) list;
    l.m_head.pin(IPinnable.READ);
    l.m_tail.pin(IPinnable.READ);
    super.addNext(list);
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
    m_head.pin(IPinnable.WRITE);
    m_tail.pin(IPinnable.WRITE);
    super.add(elem);
  }
  
  @SuppressWarnings("unchecked")
  public boolean isTailSame(ISinglyList<E> list) throws ExecException
  {
    m_head.pin(IPinnable.READ);
    m_tail.pin(IPinnable.READ);
    SinglyList<E> l = (SinglyList<E>) list;
    l.m_head.pin(IPinnable.READ);
    l.m_tail.pin(IPinnable.READ);
    return super.isTailSame(list);
  }

  public static class SinglyListNode<E> 
    extends oracle.cep.dataStructures.internal.paged.SinglyList.SinglyListNode<E> 
  {
    public SinglyListNode(oracle.cep.dataStructures.internal.paged.List<E> list)
    {
      super(list);
    }

    @SuppressWarnings("unchecked")
    public <T extends IPinnable<IListNode<E>>> T pin(int mode) throws ExecException 
    {
      // need to invoke pin to set the dirty flag correctly.
      m_page = pin(m_pm, mode);
      return (T) this;
    }

    public void unpin() throws ExecException 
    {
      m_page = null;
    }
  }
  
  public static class SinglyListIter<E>
    extends ListIter<E>
    implements ISinglyListIter<E>
  {
  }

}
