/* $Header: DoublyList.java 19-jun-2008.18:47:17 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 DoublyList is the stored version of IDoublyList implementation.

 PRIVATE CLASSES

 NOTES

 MODIFIED    (MM/DD/YY)
 hopark      06/19/08 - logging refactor
 hopark      02/25/08 - Clear page reference on unpin
 hopark      12/26/07 - support xmllog
 hopark      12/26/07 - support xmllog
 hopark       12/06/07 - cleanup spill
 hopark       10/31/06 - Creation
 */
package oracle.cep.dataStructures.internal.stored;

import oracle.cep.dataStructures.internal.IDoublyListIter;
import oracle.cep.dataStructures.internal.IDoublyListNode;
import oracle.cep.dataStructures.internal.IListNode;
import oracle.cep.dataStructures.internal.IListNodeHandle;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.trace.LogTags;
import oracle.cep.memmgr.IPinnable;

/**
 * @version $Header: DoublyList.java 19-jun-2008.18:47:17 hopark Exp $
 * @author parujain
 * @since release specific (what release of product did this appear in)
 */
@DumpDesc(attribTags={"Id", "Length"}, 
          attribVals={"getId", "getSize"})
public class DoublyList<E> 
  extends oracle.cep.dataStructures.internal.paged.DoublyList<E>
{
  static final long serialVersionUID = -2400917584259853074L;

  public DoublyList()
  {
    super();
  }

  public ListNode<E> createNode() {return new DoublyListNode<E>(this);}
  protected boolean isExternalizable() {return true;}
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
   * Removes the first element in the doubly linked list
   * 
   * @return Success/Failure
   */
  public boolean removeFirst() throws ExecException
  {
    m_head.pin(IPinnable.WRITE);
    m_tail.pin(IPinnable.WRITE);
    return super.removeFirst();
  }

  /**
   * Removes the last element in the doubly linked list
   * 
   * @return Success/Failure
   */
  public boolean removeLast() throws ExecException
  {
    m_head.pin(IPinnable.WRITE);
    m_tail.pin(IPinnable.WRITE);
    return super.removeLast();
  }
  
  /**
   * Removes a Inode element from the doubly link list
   * @param e
   *         Element to be removed from the list
   * @throws ExecException
   */
  public void remove(IListNodeHandle<E> elem) throws ExecException
  {
    m_head.pin(IPinnable.WRITE);
    m_tail.pin(IPinnable.WRITE);
    super.remove(elem);
  }
  
  /**
   * Removes a INode element from the doubly linked list
   * 
   * @param elem
   *          Element to be removed from the list
   * @return Success/Failure
   */
  public boolean remove(Object objNode) throws ExecException
  {
    m_head.pin(IPinnable.WRITE);
    m_tail.pin(IPinnable.WRITE);
    return super.remove(objNode);
  }

  /**
   * Adds an element in the linked list. Element will be inserted in the
   * end.
   * 
   * @param elem
   *          Element to be inserted in the list.
   */
  @SuppressWarnings("unchecked")
  public <T extends IDoublyListNode<E>> T add(E elem) throws ExecException
  {
    m_head.pin(IPinnable.WRITE);
    m_tail.pin(IPinnable.WRITE);
    return (T) super.add(elem);
  }
  
  /**
   * Gets the first element in the linked list
   * 
   * @return First Element
   */
  public E peek() throws ExecException
  {
    m_head.pin(IPinnable.READ);
    return super.peek();
  }

  /**
   * Checks whether Doubly linked list contains the elment or not
   * 
   * @param elem
   *          Element to be searched
   * @return Success/Failure
   */
  public boolean contains(Object elem) throws ExecException
  {
    m_head.pin(IPinnable.READ);
    m_tail.pin(IPinnable.READ);
    return super.contains(elem);
  }
  
  
  public static class DoublyListNode<E> 
    extends oracle.cep.dataStructures.internal.paged.DoublyList.DoublyListNode<E> 
  {
    public DoublyListNode(oracle.cep.dataStructures.internal.paged.List<E> list)
    {
      super(list);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends IPinnable<IListNode<E>>> T pin(int mode) throws ExecException 
    {
      m_page = pin(m_pm, mode);
      return (T) this;
    }

    public void unpin() throws ExecException 
    {
      m_page = null;
    }
  }
  
  public static class DoublyListIter<E> 
    extends ListIter<E>
    implements IDoublyListIter<E>
  {
  }
}
