/* $Header: SinglyList.java 19-jun-2008.18:45:57 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 SinglyList is the memory version of ISinglyList implementation.

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 The methods in this class are not synchronized.
 It should be done on client side.

 MODIFIED    (MM/DD/YY)
 hopark      06/19/08 - logging refactor
 hopark      02/28/08 - resurrect refcnt
 hopark      12/26/07 - support xmllog
 hopark      01/03/08 - remove refcnt
 hopark      12/07/07 - cleanup spill
 hopark      12/04/07 - remove initFactory
 hopark      11/15/07 - init NodeFac
 hopark      10/30/07 - use IList
 hopark      12/18/07 - change iterator semantics
 mthatte     12/04/07 - synchronizing
 hopark      10/15/07 - add dummy evictable
 hopark      10/24/07 - add removeNode
 hopark      10/04/07 - optimize
 hopark      08/27/07 - move listnode
 hopark      07/13/07 - dump stack trace on exception
 hopark      06/20/07 - cleanup
 parujain    06/07/07 - lint error
 hopark      05/28/07 - logging support
 hopark      05/11/07 - remove System.out.println(use java.util.logging instead)
 hopark      04/05/07 - mem reorg
 najain      03/12/07 - bug fix
 hopark      03/02/07 - uses ISinglyList interface
 najain      02/05/07 - Creation
 */

/**
 *  @version $Header: SinglyList.java 19-jun-2008.18:45:57 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.memory;

import oracle.cep.dataStructures.internal.IList;
import oracle.cep.dataStructures.internal.IListNode;
import oracle.cep.dataStructures.internal.IListNodeHandle;
import oracle.cep.dataStructures.internal.ISinglyList;
import oracle.cep.dataStructures.internal.ISinglyListIter;
import oracle.cep.dataStructures.internal.ISinglyListNode;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.trace.LogTags;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.util.StringUtil;

@DumpDesc(attribTags={"Id", "Length"}, 
          attribVals={"getId", "getSize"})
public class SinglyList<E> implements ISinglyList<E>
{
  // Initially both head and tail are null
  SinglyListNode<E>           head = null;
  SinglyListNode<E>           tail = null;

  /** Storage manager for the stores */
  IAllocator<SinglyListNode<E>> nFactory;
  int                         size = 0;

  /**
   * Empty Constructor
   * 
   */
  public SinglyList()
  {
    head = null;
    tail = null;
    size = 0;
  }

  public int getId() {return hashCode();}
  public boolean evict() {return false;}
  
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

  public IAllocator getFactory()
  {
    return nFactory;
  }

  /**
   * @return Returns the size.
   */
  public int getSize()
  {
    return size;
  }

  /**
   * Getter of the first element in the singly linked list
   * 
   * @return First Element
   */
  public E getFirst()
  {
    if (head == null)
      return null;
    return head.NodeElem;
  }

  /**
   * Getter of the last element in the singly linked list
   * 
   * @return First Element
   */
  public E getLast()
  {
    if (tail == null)
      return null;
    return tail.NodeElem;
  }

  @SuppressWarnings("unchecked")
  public <T extends IListNode<E>> T getHead()
  {
    return (T) head;
  }

  @SuppressWarnings("unchecked")
  public <T extends IListNode<E>> T  getTail()
  {
    return (T) tail;
  }

  /**
   * Removes the first element in the singly linked list
   */
  public E remove()
  {
    SinglyListNode<E> n = removeNode();
    if (n != null)
      return n.NodeElem;
    return null;
  }

  /**
   * Removes the first element in the singly linked list
   */
  public E remove(boolean freeMem)
  {
    SinglyListNode<E> n = removeNode();
    E elem = n.NodeElem;
    if (freeMem)
       nFactory.release(n);
    return elem;
  }
  
  /**
   * Removes the first element in the singly linked list
   */
  @SuppressWarnings("unchecked")
  protected <T extends ISinglyListNode<E>> T removeNode()
  {
    if (head == null)
      return null;

    SinglyListNode<E> e = head;
    if (head == tail)
    {
      head = null;
      tail = null;
    }
    else
      head = head.next;
    
    size--;
    return (T) e;
  }

  /**
   * Add the last element from the list to the this list without allocating
   * memory for the node
   *
   * @param list
   *          The list to allocate memory from
   */
  public void addLast(ISinglyList<E> l)
  {
    SinglyList<E> list = (SinglyList<E>) l;
    SinglyListNode<E> listTail = list.tail;
    if (head == null)
    {
      assert tail == null;
      assert size == 0;
      head = listTail;
      tail = listTail;
    }
    else
    {
      assert tail.next == listTail;
      tail = tail.next;
    }
   
    size++;
  }

  public void addNext(ISinglyList<E> l)
  {
    SinglyList<E> list = (SinglyList<E>) l;
    if (tail != null)
    {
      assert tail.next != null;
      tail = tail.next;
      size++;
    }
    else
    {
      assert head == null;
      head = list.head;
      tail = head;
      size = 1;
    }
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
  public <T extends IListNode<E>> T addElem(E elem) throws ExecException
  {
    SinglyListNode<E> newElem = nFactory.allocate();
    newElem.NodeElem = elem;
    add(newElem);
    return (T) newElem;
  }


  private void add(ISinglyListNode<E> e)
  {
    SinglyListNode<E> elem = (SinglyListNode<E>) e;
    if (head == null)
    {
      assert tail == null;
      head = elem;
      tail = elem;
    }
    else
    {
      tail.next = elem;
      tail = tail.next;
    }
    
    size++;
  }

  public boolean isTailSame(ISinglyList<E> list)
    throws ExecException
  {
    SinglyListNode<E> ltail = ((SinglyList<E>)list).tail;
    return tail == ltail ? true : false;
  }

  /**
   * Checks if the singly linked list has any element or not
   * 
   * @return True/False depending on whether list is empty or not
   */
  public boolean isEmpty()
  {
    if (head == null)
    {
      assert size == 0;
      return true;
    }

    assert size > 0;
    return false;
  }

  public void clear()
  {
    while(true)
    {
      if (head == null)
        break;
      removeNode();
    }
    head = null;
    tail = null;
    size = 0;
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
    try
    {
      ISinglyListNode<E> n = getHead();
      while (n != null) 
      {
        n.dump(dumper);
        n = (ISinglyListNode<E>) n.getNext(this);
      }
    }
    catch(ExecException e)
    {
      dumper.writeln(LogTags.DUMP_ERR, e.toString());
    }
    LogUtil.endDumpObj(dumper, tag);
  }

  @DumpDesc(tag="SListNode", 
      attribTags={"Name", "Id", "Next"}, 
      attribVals={"getName", "getId", "getNextId"},
      valueTags={"E"},
      values={"getNodeElem"})
  public static class SinglyListNode<E> 
    implements ISinglyListNode<E>, IListNodeHandle<E>
  {
    static final long serialVersionUID = 5343111778913516458L;
  
    E NodeElem;
  
    SinglyListNode<E> next;
  
    public SinglyListNode()
    {
      super();
      this.NodeElem = null;
      this.next = null;
    }
    
    public String getName() {return StringUtil.getBaseClassName(this);}
    public int getId() {return hashCode();}
    public int getNextId() {return (next == null) ? -1 : (int) next.getId();}

    /**
     * Sets the Node Element value
     * 
     * @param elem
     */
    public void setNodeElem(E elem)
    {
      this.NodeElem = elem;
    }
  
    public E getNodeElem() 
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
    public final void setNext(IListNode<E> n )
    {
      this.next = (SinglyListNode<E>) n;
    }
    
    @SuppressWarnings("unchecked")
    public final <T extends IListNode<E>> T  getNext(IList<E> l)
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
    }

    // IPinnable 
    @SuppressWarnings("unchecked")
    public <T extends IPinnable<IListNode<E>>> T pin(int mode) throws ExecException { return (T) this;}
    public void unpin() throws ExecException {}
    
    public String toString()
    {
      StringBuilder buf = new StringBuilder();
      buf.append("next=");
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

  public static class SinglyListIter<E> implements ISinglyListIter<E>
  {
    private SinglyListNode<E>    current;
    private SinglyList<E>  list;

    /**
     * Constructor
     * 
     */
    public SinglyListIter(ISinglyList<E> list)
    {
      super();
      initialize(list);
    }

    /**
     * Constructor
     * 
     */
    public SinglyListIter()
    {
      super();
    }

    public void initialize(IList<E> list)
    {
      this.list = (SinglyList<E>)list;
      current = null;
    }

    public void release(IList<E> list) throws ExecException
    {
      list = null;
      current = null;
    }

    /**
     * Gets the next element in the list
     * 
     * @return Next ISinglyListNode element to be read.
     */
    public E next()
    {
      if (current == null)
      {
        current = list.getHead();
        list = null;
      }
      else
      {
        current = current.next;
      }
      E elem = null;
      if (current != null)
        elem = current.NodeElem;
      return elem;
    }
  }

}

