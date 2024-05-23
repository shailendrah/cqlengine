/* $Header: DoublyList.java 28-feb-2008.10:30:15 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 DoublyList is the memory version of IDoublyList implementation.

 PRIVATE CLASSES

 NOTES
 The methods in this class are not synchronized.
 It should be done on client side.
 
 MODIFIED    (MM/DD/YY)
 hopark      02/28/08 - resurrect refcnt
 hopark      12/26/07 - support xmllog
 hopark      01/03/08 - remove refcnt
 hopark      12/07/07 - cleanup spill
 hopark      12/04/07 - remove initFactory
 hopark      11/15/07 - init NodeFac
 hopark      11/03/07 - remove getNodeStr
 hopark      10/30/07 - use IList
 hopark      12/18/07 - change iterator semantics
 hopark      10/15/07 - add dummy evictable
 hopark      09/22/07 - add ListNodeHandle
 najain      09/06/07 - 
 hopark      08/24/07 - add removing a node
 sbishnoi    08/27/07 - add addElement
 hopark      08/03/07 - fix dump
 hopark      07/13/07 - dump stack trace on exception
 hopark      06/19/07 - cleanup
 parujain    06/07/07 - lint error
 hopark      05/28/07 - logging support
 hopark      04/05/07 - memmgr reorg
 najain      03/12/07 - bug fix
 hopark      03/08/07 - class hiearchy cleanup
 najain      01/15/07 - spill-over support
 hopark      01/09/07 - allow passing any object for removing to support TimedTuple
 parujain    11/30/06 - DoublyListIter memory mgmt
 najain      11/08/06 - DoublyList is a StorageElement
 najain      11/08/06 - bug fix
 parujain    07/28/06 - Generic Doubly Linkedlist
 parujain    07/28/06 - Creation
 */
package oracle.cep.dataStructures.internal.memory;

import java.util.logging.Level;

import oracle.cep.dataStructures.internal.IDoublyList;
import oracle.cep.dataStructures.internal.IDoublyListIter;
import oracle.cep.dataStructures.internal.IDoublyListNode;
import oracle.cep.dataStructures.internal.IList;
import oracle.cep.dataStructures.internal.IListNode;
import oracle.cep.dataStructures.internal.IListNodeHandle;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.util.StringUtil;

/**
 * @version $Header: DoublyList.java 28-feb-2008.10:30:15 hopark Exp $
 * @author parujain
 * @since release specific (what release of product did this appear in)
 */
@DumpDesc(attribTags={"Id", "Length"}, 
          attribVals={"getId", "getSize"})
public class DoublyList<E> implements IDoublyList<E>
{
  // Initially both head and tail are null
  private DoublyListNode<E>             head = null;
  private DoublyListNode<E>             tail = null;

  private int                           size = 0;

  /** Storage manager for the stores */
  private IAllocator<DoublyListNode<E>> nFactory;

  /**
   * Empty Constructor
   * 
   */
  public DoublyList()
  {
    super();
    head = null;
    tail = null;
    size = 0;
  }

  public int getId() {return hashCode();}
  public boolean evict() {return false;}

  @SuppressWarnings("unchecked")
  public <T extends IListNode<E>> T getHead()
  {
    return (T) head;
  }

  @SuppressWarnings("unchecked")
  public <T extends IListNode<E>> T getTail()
  {
    return (T) tail;
  }

  /**
   * Sets the NodeFactory for a particular type of INode element
   * 
   * @param fact
   *          Factory
   */
  @SuppressWarnings("unchecked")
  public void setFactory(IAllocator fact)
  {
    this.nFactory = fact;
  }

  @SuppressWarnings("unchecked")
  public IAllocator getFactory()
  {
    return nFactory;
  }

  /**
   * Getter of the first element in the doubly linked list
   * 
   * @return First Element
   */
  public E getFirst() throws ExecException
  {
    assert size != 0;
    assert head != null;
    return head.NodeElem;
  }

  /**
   * Gets the last element in the doubly linked list
   * 
   * @return Last element in the list
   */
  public E getLast() throws ExecException
  {
    assert tail != null;
    return tail.NodeElem;
  }

  /**
   * Removes the first element in the doubly linked list
   * 
   * @return Success/Failure
   */
  public boolean removeFirst() throws ExecException
  {
    if (head == null)
      return false;

    remove(head);
    return true;
  }

  /**
   * Removes the last element in the doubly linked list
   * 
   * @return Success/Failure
   */
  public boolean removeLast() throws ExecException
  {
    if (tail == null)
      return false;
    remove(tail);
    return true;
  }

  private DoublyListNode<E> search(Object elem) throws ExecException
  {
    if (head == null) return null;

    for (DoublyListNode<E> e = head; e != null; e = e.next)
    {
      if (e.NodeElem.equals(elem))
      {
        return e;
      }
    }
    return null;
  }

  private boolean hasNode(DoublyListNode<E> n) throws ExecException
  {
    for (DoublyListNode<E> e = head; e != null; e = e.next)
    {
      if (e == n)
        return true;
    }
    return false;
  }  
  
  /**
   * Search and removes an element from the doubly linked list
   * 
   * @param elem
   *          Element to be removed from the list
   * @return Success/Failure
   */
  public boolean remove(Object elem) throws ExecException
  {
    DoublyListNode<E> node = search(elem);
    if (node == null)
      return false;
    remove(node);
    return true;
  }

  /**
   * Removes a INode from the doubly linked list
   * 
   * @param node
   *          Node to be removed from the list
   * @return Success/Failure
   */
  public void remove(IListNodeHandle<E> node) throws ExecException
  {
    assert head != null;
    assert tail != null;

    @SuppressWarnings("unchecked")
    DoublyListNode<E> e = (DoublyListNode<E>) node;
    assert hasNode(e);
    
    if (e.next != null)
    {
      DoublyListNode<E> n = e.next;
      n.prev = e.prev;
    } 
    else
    {
      assert tail == e;
      tail = tail.prev;
      if (tail != null)
	tail.next = null;
    }

    DoublyListNode<E> p = e.prev;
    if (p != null)
    {
      p.next = e.next;
    } 
    else
    {
      assert head == e;
      head = head.next;
      if (head != null)
	head.prev = null;
    }      

    e.next = null;
    e.prev = null;
    assert size > 0;
    size--;
    nFactory.release(e);
  }

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
    DoublyListNode<E> newElem = nFactory.allocate();

    newElem.NodeElem = elem;
    if (head == null)
    {
      assert tail == null;
      head = newElem;
      tail = newElem;
    } else
    {
      tail.next = newElem;
      newElem.prev = tail;
      tail = tail.next;
    }
    size++;
    return (T) newElem;
  }

  /**
   * Gets the first element in the linked list
   * 
   * @return First Element
   */
  public E peek() throws ExecException
  {
    if (size == 0)
      return null;
    return head.NodeElem;
  }

  /**
   * Checks if the doubly linked list has any element or not
   * 
   * @return True/False depending on whether list is empty or not
   */
  public boolean isEmpty()
  {
    if (size == 0)
    {
      assert head == null;
      return true;
    }

    return false;
  }

  /**
   * Get the size of the doubly linked list
   * 
   * @return size of the list
   */
  public int getSize()
  {
    return size;
  }

  /**
   * Clears the complete doubly linked list
   * 
   */
  public void clear()
  {
    try
    {
      while(true)
      {
        if (!removeFirst())
          break;
      }
    }
    catch(ExecException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }
    head = null;
    tail = null;
    size = 0;
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
    DoublyListNode<E> node = search(elem);
    return (node != null);
  }

  public synchronized void dump(IDumpContext dumper)
  {
    assert (dumper != null);
    if (!dumper.isVerbose())
    {
      String tag = LogUtil.beginDumpObj(dumper, this);
      LogUtil.endDumpObj(dumper, tag);
      return;
    }
    String tag = LogUtil.beginDumpObj(dumper, this);
    for (DoublyListNode<E> n = head; n != null; n = n.next)
    {
      n.dump(dumper);
    }
    LogUtil.endDumpObj(dumper, tag);
  }

  @DumpDesc(attribTags={"Id", "Prev", "Next"}, 
            attribVals={"getId", "getPrevId", "getNextId"},
            valueTags={"E"},
            values={"getNodeElem"})
  public static class DoublyListNode<E> 
      implements IDoublyListNode<E>, IListNodeHandle<E>
  {
    static final long serialVersionUID = 871951043818885524L;

    E                 NodeElem;

    DoublyListNode<E> prev;
    DoublyListNode<E> next;

    public DoublyListNode()
    {
      super();
      clear();
    }

    public String getName() {return StringUtil.getBaseClassName(this);}
    public int getId() {return hashCode();}
    public int getPrevId() {return (prev == null) ? -1 : (int) prev.getId();}
    public int getNextId() {return (next == null) ? -1 : (int) next.getId();}
    
    /**
     * Sets the Node Element value
     * 
     * @param elem
     */
    public void setNodeElem(E elem) throws ExecException
    {
      this.NodeElem = elem;
    }

    public E getNodeElem() throws ExecException
    {
      return NodeElem;
    }

    public IListNodeHandle<E> getHandle(IList<E> l) throws ExecException
    {
      return this;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends IListNode<E>> T getNode(IList<E> l, int mode) throws ExecException
    {
      return (T) this;
    }
    
    @SuppressWarnings("unchecked")
    public void setPrev(IListNode<E> p) throws ExecException
    {
      this.prev = (DoublyListNode<E>) p;
    }

    @SuppressWarnings("unchecked")
    public void setNext(IListNode<E> n) throws ExecException
    {
      this.next = (DoublyListNode<E>) n;
    }

    @SuppressWarnings("unchecked")
    public <T extends IListNode<E>> T getPrev(IList<E> l) throws ExecException
    {
      return (T) prev;
    }

    @SuppressWarnings("unchecked")
    public <T extends IListNode<E>> T getNext(IList<E> l) throws ExecException
    {
      return (T) next;
    }

    /**
     * Clears the node
     */
    public void clear()
    {
      this.next = null;
      this.NodeElem = null;
      this.prev = null;
    }

    // IPinnable 
    @SuppressWarnings("unchecked")
    public <T extends IPinnable<IListNode<E>>> T pin(int mode) throws ExecException { return (T) this;}
    public void unpin() throws ExecException {}
    
    public String toString()
    {
      StringBuilder buf = new StringBuilder();
      buf.append("prev=");
      buf.append(prev == null ? "null" : prev.hashCode());
      buf.append(" next=");
      buf.append(next== null ? "null" : next.hashCode());
      buf.append(" elem=");
      Object e = NodeElem;
      buf.append(e == null ? "null" : e.toString());
      return buf.toString();
    }

    public synchronized void dump(IDumpContext dumper)
    {
      String tag = LogUtil.beginDumpObj(dumper, this);
      LogUtil.endDumpObj(dumper, tag);
    }
  }

  public static class DoublyListIter<E> implements IDoublyListIter<E>
  {
    DoublyListNode<E>       current;
    IDoublyList<E>          list;

    /**
     * Constructor
     * 
     */
    public DoublyListIter()
    {
      super();
      
      current = null;
      list = null;
    }

    @SuppressWarnings("unchecked")
    public void initialize(IList<E> list) throws ExecException
    {
      current = null;
      this.list = (IDoublyList<E>) list;
    }

    public void release(IList<E> list) throws ExecException
    {
      current = null;
      list = null;
    }

    /**
     * Resets the current node
     */
    public void resetCurrent() throws ExecException
    {
      assert (current != null);
    }
    
    /**
     * Gets the next element in the list
     * 
     * @return Next Node element to be read.
     */
    public E next()
    {
      try
      {
        if (current == null)
        {
          current = list.getHead();
          list = null;
        }
        else
        {
          current.unpin();
          current = current.next;
        }
        E elem = null;
        if (current != null)
          elem = current.NodeElem;
        return elem;
      }
      catch (ExecException e)
      {
        LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      }
      return null;
    }
  }  
  
}
