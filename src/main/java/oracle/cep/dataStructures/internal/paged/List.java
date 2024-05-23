/* $Header: pcbpel/cep/server/src/oracle/cep/dataStructures/internal/paged/List.java /main/7 2008/10/24 15:50:22 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
 DESCRIPTION
 List implements paged list where nodes are stored in pages.
 
 ListNode
 Since all nodes are stored in pages,  we need a temporary handle to access nodes.
 ListNode class provides such access.
 
 Eviction
 As the head and tail nodes are holding onto the reference of IPage, they need to release
 the reference in eviction so that the page can be reclaimed by the garbage collector.
 In order to track the evictable objects that holds references to pages, the page list
 uses a list of evictable objects and invokes 'evict' for each object in case of list eviction.
 The evictable objects are head, tail nodes and list iterator hat have current and next nodes.
 Head and tail nodes are registered in creation of list.
 List iterators are regiestered in init/release of list iterators.
 
 
 PRIVATE CLASSES

 NOTES
 The methods in this class are not synchronized.
 It should be done on client side.

 MODIFIED    (MM/DD/YY)
  hopark      10/10/08 - remove statics
  hopark      06/19/08 - logging refactor
  hopark      03/20/08 - fix NPE
  hopark      02/28/08 - tupleptr serialization optimization
  hopark      01/27/08 - handle null input on euqls
  hopark      12/06/07 - cleanup spill
  hopark      11/03/07 - Creation
 */

package oracle.cep.dataStructures.internal.paged;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.LinkedList;
import java.util.logging.Level;

import oracle.cep.dataStructures.internal.IList;
import oracle.cep.dataStructures.internal.IListNode;
import oracle.cep.dataStructures.internal.IListNodeHandle;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogTags;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IEvictableObj;
import oracle.cep.memmgr.IPage;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.memmgr.PageLayout;
import oracle.cep.memmgr.PageManager;
import oracle.cep.memmgr.PagedFactory;
import oracle.cep.memmgr.IAllocator.NameSpace;
import oracle.cep.memmgr.PageLayout.LayoutDesc;
import oracle.cep.memmgr.PageManager.PageRef;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.service.CEPManager;
import oracle.cep.util.DebugLogger;
import oracle.cep.util.DebugUtil;

/**
 * @version $Header: pcbpel/cep/server/src/oracle/cep/dataStructures/internal/paged/List.java /main/7 2008/10/24 15:50:22 hopark Exp $
 * @author parujain
 * @since release specific (what release of product did this appear in)
 */
public abstract class List<E> implements IList<E>, Externalizable
{
  protected static final int NEXT_POS = 0;
  protected static final int ELEM_POS = 1;
  protected static final int PREV_POS = 2;
  protected static final int MAX_POS = 3;
  
  protected ListNode<E> m_head;
  protected ListNode<E> m_tail;

  static int s_nextId = 0;
  protected int     m_id;
  protected int     m_size;

  protected PageLayout m_pageLayout;
  protected PagedFactory<PageRef> m_factory;
  protected PageManager m_pm;
  
  protected java.util.List<IEvictableObj> m_evictableNodes;
  
  static int s_initPageTableSize;
  static int s_pageSize;
  static int s_minObjsPage;

  static protected DebugLogger s_logger;

  static
  {
    ConfigManager cm = CEPManager.getInstance().getConfigMgr();
    s_initPageTableSize = cm.getListInitPageTableSize();
    s_pageSize = cm.getListPageSize();
    s_minObjsPage = cm.getListMinNodesPage();
    if (DebugUtil.DEBUG_PAGEDLIST_NODE)
    {
      s_logger = new DebugLogger("DoublyList", DebugLogger.FIELD,
          "m_id", false);
    }
  }  
  
  public List()
  {
    super();
    m_id = s_nextId++;
  }

  protected abstract LayoutDesc getPageLayoutDesc();
  
  protected abstract NameSpace getNameSpace();
  
  public ListNode<E> createNode() {assert false; return null;}

  // If the list can be evicted as part of eviction such as TuplePtr,
  // this method should be overriden and return true.
  // Currently, PagedDoublyList is the only one.
  protected boolean isExternalizable() {return false;}
  protected boolean isEvictable() {return false;}
  public int getId() {return m_id;}

  public boolean evict() 
  {
    if (DebugUtil.DEBUG_PAGEDLIST_NODE)
    {
      if (m_id == DebugUtil.DEBUGI_PAGEDLIST_NODE_LISTID) {
        s_logger.log(m_id, this, "evict");
        s_logger.print(m_id, DebugLogger.TRACE_HISTORY);
      }
    }
    try
    {
      if (m_evictableNodes != null)
      {
        for (IEvictableObj node : m_evictableNodes)
        {
          node.evict();
        }
      }
      return m_pm.evict();
    }
    catch(ExecException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }
    return false;
  }
  
  public void addEvictableNode(IEvictableObj node)
  {
    if (m_evictableNodes == null) return;
    synchronized (m_evictableNodes)
    {
      if (!m_evictableNodes.contains(node))
        m_evictableNodes.add(node);
    }
  }
  
  public void removeEvictableNode(IEvictableObj node)
  {
    if (m_evictableNodes == null) return;

    synchronized (m_evictableNodes)
    {
      m_evictableNodes.remove(node);
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends IListNode<E>> T getHeadPtr()  throws ExecException
  {
    return (T) m_head;
  }

  @SuppressWarnings("unchecked")
  public <T extends IListNode<E>> T getHead()  throws ExecException
  {
    if (m_head.isNull())
      return null;
    return (T) m_head;
  }

  @SuppressWarnings("unchecked")
  public <T extends IListNode<E>> T getTail()  throws ExecException
  {
    if (m_tail.isNull())
      return null;
    return (T) m_tail;
  }

  private void init()
  {
    int id = m_factory.getId();
    LayoutDesc desc = getPageLayoutDesc();
    desc.setPageSize(s_pageSize);
    desc.setMinObjs(s_minObjsPage);
    m_pageLayout = PageLayout.create(id, desc);
    m_factory.setPageLayout(m_pageLayout);
    m_pm = m_factory.getPageManager();
    if (isEvictable())
    {
      m_evictableNodes = new LinkedList<IEvictableObj>();
    }

    m_head = createNode();
    m_tail = createNode();
    m_size = 0;
    
    addEvictableNode(m_head);
    addEvictableNode(m_tail);
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
    m_factory = (PagedFactory<PageRef>) fact;
    init();
  }

  @SuppressWarnings("unchecked")
  public IAllocator getFactory()
  {
    return m_factory;
  }
  
  public PageManager getPageManager() {return m_pm;}
  
  /**
   * Getter of the first element in the doubly linked list
   * 
   * @return First Element
   */
  @SuppressWarnings("unchecked")
  public E getFirst() throws ExecException
  {
    if ( m_head.isNull())
      return null;

    return m_head.getNodeElem();
  }

  /**
   * Gets the last element in the doubly linked list
   * 
   * @return Last element in the list
   */
  @SuppressWarnings("unchecked")
  public E getLast() throws ExecException
  {
    assert (!m_tail.isNull());

    return m_tail.getNodeElem();
  }

  /**
   * Removes the first element in the doubly linked list
   * 
   * @return Success/Failure
   */
  public boolean removeFirst() throws ExecException
  {
    if (m_head.isNull())
      return false;

    if (DebugUtil.DEBUG_PAGEDLIST_VERIFY)
      verify();
    
    removeNode(m_head, true);
    if (DebugUtil.DEBUG_PAGEDLIST_NODE)
    {
      if (m_id == DebugUtil.DEBUGI_PAGEDLIST_NODE_LISTID) {
        s_logger.log(m_id, this, "removeFirst", toString());
        s_logger.print(m_id, DebugLogger.TRACE_HISTORY);
      }
    }
    return true;
  }

  /**
   * Removes the last element in the doubly linked list
   * 
   * @return Success/Failure
   */
  public boolean removeLast() throws ExecException
  {
    if (m_tail.isNull())
      return false;

    if (DebugUtil.DEBUG_PAGEDLIST_VERIFY)
      verify();
    
    removeNode(m_tail, true);
    if (DebugUtil.DEBUG_PAGEDLIST_NODE)
    {
      if (m_id == DebugUtil.DEBUGI_PAGEDLIST_NODE_LISTID) {
        s_logger.log(m_id, this, "removeLast", toString());
        s_logger.print(m_id, DebugLogger.TRACE_HISTORY);
      }
    }
    return true;
  }

  private ListNode<E> search(Object elem) 
    throws ExecException
  {
    if (m_head.isNull()) return null;

    ListNode<E> node = createNode();
    node.copy(m_head);
    while (!node.isNull())
    {
      Object ne = node.getNodeElem();
      if (ne != null && ne.equals(elem))
      {
        return node;
      }
      node.getNext(node);
    }
    return null;
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
    ListNode<E> node = search(elem);
    if (node == null)
      return false;
    if (DebugUtil.DEBUG_PAGEDLIST_NODE)
    {
      if (m_id == DebugUtil.DEBUGI_PAGEDLIST_NODE_LISTID) {
        s_logger.log(m_id, this, "remove", node.toString(), toString());
        s_logger.print(m_id, DebugLogger.TRACE_HISTORY);
      }
    }
    removeNode(node, true);
    return true;
  }

  /**
   * Removes a INode from the linked list
   * 
   * @param node
   *          Node to be removed from the list
   * @return Success/Failure
   */
  public void remove(IListNodeHandle<E> node) throws ExecException
  {
    @SuppressWarnings("unchecked")
    ListNodeHandle<E> h = (ListNodeHandle<E>) node;
    ListNode<E> e = h.getNode(this, IPinnable.WRITE);
    if (DebugUtil.DEBUG_PAGEDLIST_NODE)
    {
      if (m_id == DebugUtil.DEBUGI_PAGEDLIST_NODE_LISTID) {
        s_logger.log(m_id, this, "remove", e.toString(), "h="+h.toString(), toString());
        s_logger.print(m_id, DebugLogger.TRACE_HISTORY);
      }
    }
    removeNode(e, true);
  }
  
  protected abstract void removeNode(ListNode<E> e, boolean freeNode) throws ExecException;
  
  /**
   * Gets the first element in the linked list
   * 
   * @return First Element
   */
  public E peek() throws ExecException
  {
    if (m_size == 0)
      return null;
    return getFirst();
  }

  /**
   * Checks if the doubly linked list has any element or not
   * 
   * @return True/False depending on whether list is empty or not
   */
  public boolean isEmpty()
  {
    return (m_size == 0);
  }

  /**
   * Get the m_size of the doubly linked list
   * 
   * @return m_size of the list
   */
  public int getSize()
  {
    return m_size;
  }

  /**
   * Clears the complete linked list
   * 
   */
  public void clear() throws ExecException
  {
    while (!m_head.isNull())
    {
      removeNode(m_head, true);
    }
    m_head.clear();
    m_tail.clear();
    m_size = 0;
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
    ListNode<E> node = search(elem);
    return (node != null);
  }

  public void verify() 
  {
    try
    {
      if (m_size == 0)
      {
        assert (m_head.isNull());
        assert (m_tail.isNull());
        assert (m_head.m_pageAddr == -1);
        assert (m_tail.m_pageAddr == -1);
      }
      int pos = 0;
      ListNode<E> n = createNode();
      n.copy(m_head);
      while (!n.isNull())
      {
        pos++;
        n.pin(IPinnable.READ);
        n.getNext(n);
      }
      assert (m_size == pos);
    }
    catch(ExecException e)
    {
      
    }
  }

  public String toString()
  {
    try
    {
      StringBuilder buf = new StringBuilder();
      buf.append(getClass().getName() + "\n");
      int pos = 0;
      ListNode<E> n = createNode();
      n.copy(m_head);
      while (!n.isNull())
      {
        n.pin(IPinnable.READ);
        buf.append(pos);
        buf.append(" : ");
        buf.append(n.toString());
        buf.append("\n");
        pos++;
        n.getNext(n);
      }
      return buf.toString();
    }
    catch(ExecException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }
    return null;
  }
  
  public void writeExternal(ObjectOutput stream)
     throws IOException
   {
     if (DebugUtil.DEBUG_PAGEDLIST_VERIFY) verify();
     stream.writeInt(m_id);
     stream.writeInt(m_size); 
     long headId = m_head.isNull() ? -1l :m_head.m_pageAddr;
     long tailId = m_tail.isNull() ? -1l :m_tail.m_pageAddr;
     stream.writeLong(headId);
     stream.writeLong(tailId);
     stream.writeInt(m_pm.getId());
     int facId = (m_factory == null ? -1 : m_factory.getId());
     stream.writeInt(facId); 
   }
   
  @SuppressWarnings("unchecked")
   public void readExternal(ObjectInput stream)
     throws IOException
   {
     int id = stream.readInt(); 
     int size = stream.readInt(); 
     long headId = stream.readLong();
     long tailId = stream.readLong();
     int pmid = stream.readInt();
     int facId = stream.readInt();
     FactoryManager factoryMgr = CEPManager.getInstance().getFactoryManager();
     IAllocator tmp = (facId < 0 ? null : 
        (IAllocator) factoryMgr.get(facId));
     PagedFactory<PageRef> nodeFac = (PagedFactory<PageRef>) tmp;
     assert (nodeFac != null);

     setFactory(nodeFac);
     
     // It is important to set size here as setFactory will initialize the list.
     m_id = id;
     m_size = size;
     
     assert (pmid == m_pm.getId());
     
     try {
       PageRef ref = new PageRef();
       if (headId >= 0)
       {
         m_pm.getPage(headId, ref);
         m_head.set(ref, IPinnable.READ);
       }
       if (tailId >= 0)
       {
         m_pm.getPage(tailId, ref);
         m_tail.set(ref, IPinnable.READ);
       }
       if (DebugUtil.DEBUG_PAGEDLIST_VERIFY)
         verify();
     }
     catch (ExecException e)
     {
       LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
     }
     if (DebugUtil.DEBUG_PAGEDLIST_NODE)
     {
       if (m_id == DebugUtil.DEBUGI_PAGEDLIST_NODE_LISTID) {
         s_logger.log(m_id, this, "read", toString());
         s_logger.print(m_id, DebugLogger.TRACE_HISTORY);
       }
     }
  }
  
  @SuppressWarnings("unchecked")
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
    if (m_head != null)
    {
      try {
        ListNode<E> n = createNode();
        n.copy(m_head);
        while (!n.isNull())
        {
          n.pin(IPinnable.READ);
          n.dump(dumper);
          n.getNext(n);
        }      
      } 
      catch(ExecException ex)
      {
        dumper.writeln(LogTags.DUMP_ERR, ex.toString());
      }
    }
    LogUtil.endDumpObj(dumper, tag);
  }  
  
  public static class ListNode<E>
      extends PageManager.PageRef
      implements IListNode<E>, IListNodeHandle<E>, IEvictableObj
  {
    protected IPage       m_page;
    protected long        m_pageAddr;
    protected PageManager m_pm;

    public ListNode(List<E> list)
    {
      m_pm = list.m_pm;
    }
    
    public long getId()
    {
      return m_pageAddr;
    }
    
    public int getNextId() 
    {
      long next = -1L;
      if (m_page != null)
      {
        next = m_page.lValueGet(m_index, NEXT_POS);
      }
      return (int) next;
    }

    public void set(PageRef e, int mode)
      throws ExecException
    {
      m_pageAddr = m_pm.getPageAddr(e);
      m_pagePtr = e.m_pagePtr;
      m_page = e.pin(m_pm, mode);
      m_index = e.m_index;
    }

    public final boolean isNull()
    {
      return (m_pagePtr == null);
    }
    
    public final void copy(ListNode<E> e)
    {
      m_pageAddr = e.m_pageAddr;
      m_page = e.m_page;
      m_pagePtr = e.m_pagePtr;
      m_index = e.m_index;
    }
    
    @SuppressWarnings("unchecked")
    public boolean equals(Object o)
    {
      if (!(o instanceof ListNode)) return false;
      ListNode<E> other = (ListNode<E>) o;
      return (m_pageAddr == other.m_pageAddr);
    }
    
    protected void freeElem()
      throws ExecException
    {
      // we need to clear the reference from the page
      m_page.oValueSet(m_index, ELEM_POS, null);
    }
    
    protected void free(PagedFactory<PageRef> factory)  throws ExecException
    {
      pin(IPinnable.WRITE);
      freeElem();
      unpin();
      factory.freeBody(m_pagePtr, m_index);    
    }
    
    /**
     * Sets the Node Element value
     * 
     * @param elem
     */
    public void setNodeElem(E elem) throws ExecException
    {
      m_page.oValueSet(m_index, ELEM_POS, elem);
    }

    @SuppressWarnings("unchecked")
    public E getNodeElem() throws ExecException
    {
      return (E) m_page.oValueGet(m_index, ELEM_POS);
    }


    @SuppressWarnings("unchecked")
    public final void setNext(IListNode<E> n) throws ExecException
    {
      if (n == null)
      {
        m_page.lValueSet(m_index, NEXT_POS, -1);
      }
      else
      {
        ListNode<E> next = (ListNode<E>) n;
        m_page.lValueSet(m_index, NEXT_POS, next.m_pageAddr);
      }
    }

    @SuppressWarnings("unchecked")
    public final ListNode<E> getNext(ListNode<E> nbuf) throws ExecException
    {
      long next = m_page.lValueGet(m_index, NEXT_POS);
      if (next < 0)
      {
        nbuf.clear();
        return null;
      }
      m_pm.getPage(next, nbuf);
      if (nbuf.m_pagePtr != null)
      {
        nbuf.set(nbuf, IPinnable.READ);
        return nbuf;
      }
      nbuf.clear();
      return null;
    }
    
    
    @SuppressWarnings("unchecked")
    public final void setPrev(IListNode<E> p) throws ExecException
    {
      if (p == null)
      {
        m_page.lValueSet(m_index, PREV_POS, -1);
      }
      else
      {
        ListNode<E> prev = (ListNode<E>) p;
        m_page.lValueSet(m_index, PREV_POS, prev.m_pageAddr);
      }
    }
    
    @SuppressWarnings("unchecked")
    public final ListNode<E> getPrev(ListNode pbuf) throws ExecException
    {
      long prev = m_page.lValueGet(m_index, PREV_POS);
      if (prev < 0)
      {
        pbuf.clear();
        return null;
      }
      m_pm.getPage(prev, pbuf);
      if (pbuf.m_pagePtr != null)
      {
        pbuf.set(pbuf, IPinnable.READ);
        return pbuf;
      }
      pbuf.clear();
      return null;
    }

    @SuppressWarnings("unchecked")
    public final <T extends IListNode<E>> T getPrev(IList<E> l) throws ExecException
    {
      List<E> list = (List<E>) l;
      ListNode<E> buf = list.createNode(); 
      return (T)getPrev(buf);
    }

    @SuppressWarnings("unchecked")
    public final <T extends IListNode<E>> T getNext(IList<E> l) throws ExecException
    {
      List<E> list = (List<E>) l;
      ListNode<E> buf = list.createNode(); 
      return (T)getNext(buf);
    }
    
    /**
     * Clears the node
     */
    public void clear()
    {
      m_pagePtr = null;
      m_page = null;
      m_index = -1;
      m_pageAddr = -1;
    }
    
    public String toString()
    {
      StringBuilder buf = new StringBuilder();
      try 
      {
        getDesc(buf);
      }
      catch (ExecException e)
      {
        LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      }
      return buf.toString();
    }
    
    protected void getDesc(StringBuilder buf)
      throws ExecException
    {
      buf.append("pageAddr=");
      buf.append(m_pageAddr);
      buf.append(",");
      if (m_page != null)
      {
        E obj = getNodeElem();
        buf.append(obj == null ? "null" : obj.toString());
        long next = m_page.lValueGet(m_index, NEXT_POS);
        buf.append(" next=");
        buf.append(next);
      }
    }   
    
    public final IListNodeHandle<E> getHandle(IList<E> l) throws ExecException
    {
      IListNodeHandle<E> res = new ListNodeHandle<E>(this);
      if (DebugUtil.DEBUG_PAGEDLIST_NODE)
      {
        if (l.getId() == DebugUtil.DEBUGI_PAGEDLIST_NODE_LISTID) {
          s_logger.log(l.getId(), l, "getHandle", this.toString(), 
              "h="+((ListNodeHandle<E>)res).toString(), 
              l.toString());
          s_logger.print(l.getId(), DebugLogger.TRACE_HISTORY);
        }
      }
      return res;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends IListNode<E>> T getNode(IList<E> l, int mode) throws ExecException
    {
      List<E> list = (List<E>) l;
      ListNode<E> n = list.createNode();
      n.copy(this);
      return (T) n;
    }
    
    // IPinnable 
    @SuppressWarnings("unchecked")
    public <T extends IPinnable<IListNode<E>>> T pin(int mode) throws ExecException { return (T) this;}
    public void unpin() throws ExecException {}
    public boolean evict() throws ExecException {m_page = null; return true;}

    public synchronized void dump(IDumpContext dumper)
    {
      String tag = LogUtil.beginDumpObj(dumper, this);
      LogUtil.endDumpObj(dumper, tag);
    }
  }

  private static class ListNodeHandle<E> 
    implements IListNodeHandle<E>, Externalizable
  {
    long m_pageAddr;
    
    public ListNodeHandle()
    {
      
    }
    
    public ListNodeHandle(ListNode<E> n)
    {
      m_pageAddr = n.m_pageAddr;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends IListNode<E>> T getNode(IList<E> l, int mode)
        throws ExecException
    {
      List<E> list = (List<E>) l;
      
      PageManager pm = list.m_pm;
      PageRef res = new PageRef();
      pm.getPage(m_pageAddr, res);
      ListNode<E> n = list.createNode();
      n.set(res, mode);
      return (T) n;
    }
    
    @SuppressWarnings("unchecked")
    public boolean equals(Object other)
    {
      if (other == null) return false;
      ListNodeHandle<E> o = (ListNodeHandle<E>) other;
      return m_pageAddr == o.m_pageAddr;
    }
    
    public void writeExternal(ObjectOutput stream)
      throws IOException
    {
      stream.writeLong(m_pageAddr);
    }
  
    public void readExternal(ObjectInput stream)
      throws IOException
    {
      m_pageAddr = stream.readLong();
    }
    
    public String toString()
    {
      return "pageAddr="+m_pageAddr;
    }
  }
}
