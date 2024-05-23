/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/PageManager.java /main/11 2008/12/10 18:55:55 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/02/08 - move LogLevelManager to ExecContext
    hopark      10/10/08 - remove statics
    hopark      09/17/08 - support schema
    hopark      06/19/08 - logging refactor
    hopark      03/26/08 - server reorg
    hopark      02/25/08 - add evict info on dump
    hopark      02/20/08 - support external page managerment
    hopark      02/25/08 - add evict info on dump
    hopark      02/20/08 - support external page managerment
    hopark      02/25/08 - add evict info on dump
    hopark      02/20/08 - support external page managerment
    hopark      02/07/08 - implement dump
    hopark      01/08/08 - change freeList impl.
    hopark      12/05/07 - cleanup spill
    hopark      11/03/07 - page list access optimize
    hopark      10/31/07 - move IPagePtr
    hopark      10/19/07 - fix bug
    hopark      07/30/07 - use empty list for maintaining empty slots
    hopark      07/12/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/PageManager.java /main/11 2008/12/10 18:55:55 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import oracle.cep.execution.ExecException;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.memmgr.IAllocator.NameSpace;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.service.CEPManager;
import oracle.cep.storage.IStorage;
import oracle.cep.util.DebugLogger;
import oracle.cep.util.DebugUtil;

public class PageManager
{
  private static final int PAGETYPE_BIT_SIZE = 32;
  private static final int PAGETYPE_BIT_POS = 64 -  PAGETYPE_BIT_SIZE;
  private static final long PAGETYPE_BIT_MASK = 0xffffffff00000000L;
  private static final int MIN_INDEX_LIST = 5;  //minimum size of free page list.
  
  int           m_id;
  int           m_initialSlots;
  int           m_pageSize;
  int           m_objsInPage;
  PageLayout    m_layout;
  PagePtr[]     m_pages;
  IndexList     m_freePageList;         // array of indexes of free pages 
  Constructor   m_pageConstructor;
  boolean       m_needRefCounts;        
  boolean       m_dynamicPage;
  
  boolean       m_evicted;      //only for testing..
  List<PageRef> m_freeReserved; //only for refcount debug
  private DebugLogger pageLogger;   
  private static HashMap<Long, PageBase> s_pageMap; //only for serialization debug

  EvictStat      m_stat;
  IStorage       m_storage;
  
  static
  {
    if (DebugUtil.DEBUG_PAGE_SERIALIZATION)
    {
      s_pageMap = new HashMap<Long,PageBase>();
    }
  }
  
  public static class PagePtr
  {
    int   m_pmId;
    int   m_pageId;
    IPage m_page;
    byte  m_flag;
    short[] m_refCounts;
    
    static final byte DIRTY = 1;
    static final byte STORED = 2;
    static final byte INFREELIST = 4;
    static final byte EMPTY = 8;
    
    public PagePtr(int pmId, int pgId, int refCnts)
    {
      m_pageId = pgId;
      m_pmId = pmId;
      m_page = null;
      m_flag = EMPTY;
      m_refCounts = (refCnts == 0) ? null : new short[refCnts];
    }
    
    public final void reset(PageManager pm)
    {
      if (DebugUtil.DEBUG_PAGE_APICALLS)
      {
        pm.pageLogger.log(m_pageId, this, "freePage");
      }
      if ((m_flag & STORED) != 0)
      {
        
        // if stored, remove it form the db
        long id = pm.getGlobalPageId(m_pageId);
        if (DebugUtil.DEBUG_PAGE_APICALLS)
        {
          pm.pageLogger.log(m_pageId, this, "deletePage", id);
        }
        pm.getStorage().deleteRecord(null, NameSpace.PAGE.toString(), null /* schema */, id);
      }
      m_flag = EMPTY;
      m_page = null;
    }

    public final void setPage(IPage pg)
    {
      m_page = pg;
      m_flag = 0;
      if (m_refCounts != null)
        Arrays.fill(m_refCounts, (short) 1);
    }
    
    public final int getPageManagerId() {return m_pmId;}
    public final int getPageId() {return m_pageId;}
    public final boolean isEmptySlot() {return (m_flag & EMPTY) != 0;}
    public final boolean isInFreeList() {return (m_flag & INFREELIST) != 0;}
    public final void setInFreeList(boolean b)  { m_flag = (byte) (b ? (m_flag | INFREELIST) : (m_flag & ~INFREELIST)); }
    public final synchronized void setDirty(boolean b) { m_flag = (byte) (b ? (m_flag | DIRTY) : (m_flag & ~DIRTY)); }
    public final synchronized boolean isStored() {return (m_flag & STORED) != 0;}
    public final synchronized void setStored(boolean b)  { m_flag = (byte) (b ? (m_flag | STORED) : (m_flag & ~STORED)); }
    public final synchronized boolean isEvicted() {return (m_page == null);}

    public final synchronized IPage peek() {return m_page;}
    
    @SuppressWarnings("unchecked")
    public synchronized IPage pin(PageManager pm, int mode)
      throws ExecException
    {
      if (m_page == null)
      {
        //it was evicted, read it back
        long id = pm.getGlobalPageId(m_pageId);
        IStorage storage = pm.getStorage();
        assert (storage != null);

        if (DebugUtil.DEBUG_PAGE_APICALLS)
        {
          pm.pageLogger.log(m_pageId, this, "pinPage", id, toString());
        }
        NameSpace ns = NameSpace.PAGE;
        m_page = (IPage)  storage.getRecord(null, ns.toString(), new Long(id));
        if (DebugUtil.DEBUG_PAGE_APICALLS)
        {
          if (m_page == null)
            pm.pageLogger.print(m_pageId, DebugLogger.TRACE_HISTORY);
        }
        if (!DebugUtil.DEBUG_TUPLE_REFCOUNT)
        {
          assert (m_page != null) : "failed to retrieve pm=" + pm.getId() + " page=" + m_pageId + ", gid=" + id;
        }
        if (DebugUtil.DEBUG_PAGE_SERIALIZATION)
        {
          PageBase old = s_pageMap.get(id);
          assert (old != null);
          assert (m_page.equals(old));
        }
        setDirty(false);
      } 
      if (mode != IPinnable.READ)
      {
        setDirty(true);
      }
      return m_page;
    }
    
    public synchronized boolean evict(PageManager pm)
      throws ExecException
    {
      if (m_page == null) return false;
      if (DebugUtil.DEBUG_PAGE_APICALLS)
      {
        pm.pageLogger.log(m_pageId, this, "evict", toString());
      }
      if ( ((m_flag & DIRTY) != 0) || ((m_flag & STORED) == 0) )
      {
        // dirty or first eviction, put to storage
        //TODO better handling of empty page..
        IStorage storage = pm.getStorage();
        assert (storage != null);
        long id = pm.getGlobalPageId(m_pageId);
        if (DebugUtil.DEBUG_PAGE_APICALLS)
        {
          pm.pageLogger.log(m_pageId, this, "writePage", id);
        }
        if (!storage.putRecord(null, NameSpace.PAGE.toString(), null /* schema */, new Long(id), m_page))
        {
          assert false : "failed to write a page pmId=" + m_pmId + " pageId=" + m_pageId +
                      " gid="+id;
          return false;
        }
        setStored(true);
        if (DebugUtil.DEBUG_PAGE_SERIALIZATION)
        {
          Page pg = new Page(m_page.getPageId(), 0, m_page.getPageLayout());
          pg.copy((PageBase) m_page);
          s_pageMap.put(id , pg);
        } 
      }
      pm.getStat().addEvict(m_page);
      m_page = null;
      return true;
    }
    
    public final void reset(int index)
    {
      if (m_refCounts != null)
        m_refCounts[index] = 1;
    }
    
    public final synchronized int getRefCnt(int index)
    {
      return m_refCounts[index];
    }
    
    public final synchronized int addRef(int index) 
    {
      return ++m_refCounts[index];
    }
    
    public final synchronized int addRef(int index, int ref) 
    {
      m_refCounts[index] += ref;
      return m_refCounts[index];
    }

    public final synchronized int release(int index) 
    {
      return --m_refCounts[index];
    }
    
    public String toString()
    {
      StringBuilder b = new StringBuilder();
      b.append("(id=");
      b.append(m_pageId);
      b.append(" flag=");
      if (m_page == null) b.append("!");
      if ((m_flag & DIRTY) != 0) b.append("d");
      if ((m_flag & STORED) != 0) b.append("s");
      if ((m_flag & INFREELIST) != 0) b.append("f");
      if ((m_flag & EMPTY) != 0) b.append("e");
      b.append(" pmId=");
      b.append(m_pmId);
      b.append(")");
      if (m_page != null)
      {
        PageBase pg = (PageBase) m_page;
        b.append(pg.toString());
      }
      return b.toString();
    }
  }
  
  /*
   * IndexList maintains the list of indexes in FIFO manner.
   * FIFO results less fragmentations than LIFO.
   */
  private class IndexList
  {
    int[]   m_indexes;
    int     m_head;
    int     m_tail;
    int     m_sz;
    
    IndexList()
    {
      m_sz = m_initialSlots;
      if (m_sz < MIN_INDEX_LIST)
        m_sz = MIN_INDEX_LIST;
      m_indexes = new int[m_sz];
      m_tail = 0;
      m_head = 0;
    }

    final synchronized PagePtr getTail() 
    {
      if (m_tail < m_head)
      {
        int idx = m_indexes[m_tail];
        return m_pages[idx];
      }
      return null;
    }
    
    final synchronized  void add(PagePtr pgptr)
    {
      m_indexes[m_head] = pgptr.getPageId();
      pgptr.setInFreeList(true);
      m_head = (m_head + 1) % m_sz;
      if (m_head <= m_tail)
      {
        // need to expand the array
        int newlen = m_sz + m_sz/4 + 1;
        int[] t = new int[newlen];
        int hsz = (m_sz - m_tail);
        // move and re-org
        if (m_tail == 0)
        {
          System.arraycopy(m_indexes, 0, t, 0, m_sz);
        }
        else
        {
          System.arraycopy(m_indexes, m_tail, t, 0, hsz);
          System.arraycopy(m_indexes, 0, t, hsz, m_tail);
        }
        m_indexes = t;
        m_tail = 0;
        m_head = m_sz;
        m_sz = newlen;
      } 
    }
    
    final synchronized PagePtr remove()
    {
      if (m_tail < m_head)
      {
        int idx = m_indexes[m_tail];
        m_tail = (m_tail + 1) % m_sz;
        return m_pages[idx];
      }
      return null;
    }
    
    final synchronized int length()
    {
      if (m_tail < m_head)
        return (m_head - m_tail + 1);
      return (m_sz - m_tail + m_head + 1);
    }
  }
  
  ThreadLocal<PageRef> m_pageRef = new ThreadLocal<PageRef>() 
  {
    protected synchronized PageRef initialValue() 
    {
      return new PageRef();
    }
  };
  
  public PageManager(CEPManager cepMgr, int id, PageLayout layout, int initPgs, boolean needRc)
  {
    m_id = id;
    m_layout = layout;
    m_needRefCounts = needRc;
    ConfigManager cm = cepMgr.getConfigMgr();
    m_storage = cepMgr.getStorageManager().getSpillStorage();
    IEvictPolicy epm = cepMgr.getEvictPolicy();
    m_stat = epm.getStat();
    m_dynamicPage = cm.getDynamicPageClass();
    m_initialSlots = initPgs;
    m_pageSize = layout.getPageSize();
    m_objsInPage = layout.getNoObjs();
    m_pageConstructor = null;
    if (m_dynamicPage)
      m_pageConstructor = m_layout.getPageConstructor();
    reset();
    if (DebugUtil.DEBUG_PAGE_APICALLS)
    {
      pageLogger = new DebugLogger("Page", DebugLogger.FIELD, "m_pageId", false);
    }
    if (DebugUtil.DEBUG_PAGE_ASYNCFREE)
    {
      m_freeReserved = new LinkedList<PageRef>();
    }
  }

  public int getId() {return m_id;}
  public IStorage getStorage() {return m_storage;}
  public EvictStat getStat() {return m_stat;}
  
  //only used by junit tests.
  public void setUseDynamicPage(boolean b)
  {
    m_dynamicPage = b;
    if (m_dynamicPage)
      m_pageConstructor = m_layout.getPageConstructor();
  }
  
  public PageRef createRef()
  {
    return new PageRef();
  }
  
  /*
   * Gets a global page id that includes the page type
   */
  public long getGlobalPageId(int id)
  {
    return (((long) m_id) << PAGETYPE_BIT_POS) | ((long) id);
  }
  
  /**
   * Get page address within a page manager
   * @param ref
   * @return
   */
  public long getPageAddr(PageRef ref)
  {
    long pgId = (long) ref.m_pagePtr.getPageId();
    long addr = (pgId * m_objsInPage) + ref.m_index;
    if (DebugUtil.DEBUG_PAGE_FACTORYCHK)
    {
      return getGlobalPageId((int) addr);
    }
    
    return addr;
  }
  
  public synchronized PagePtr getPage(int pgid)
  {
    if (pgid >= m_pages.length)
      return null;
    return m_pages[pgid];
  }
  
  public synchronized PageRef getPage(long pageAddr, PageRef res)
  {
    if (DebugUtil.DEBUG_PAGE_FACTORYCHK)
    {
      int pmId = (int) (pageAddr >> PAGETYPE_BIT_POS);
      assert (pmId == m_id);
    }
    
    pageAddr &= ~PAGETYPE_BIT_MASK;
    if (pageAddr < 0) {
      res.m_pagePtr = null;
      return null;
    }
    long pgid = pageAddr / m_objsInPage;
    PagePtr pg  = m_pages[(int)pgid];
    if (pg == null) {
      res.m_pagePtr = null;
      return null;
    }
    res.m_index = (int)(pageAddr % m_objsInPage);
    res.m_pagePtr = pg;
    return res;
  }
  
  public void reset()
  {
    m_pages = new PagePtr[m_initialSlots];
    m_freePageList = new IndexList();
    for (int i = 0; i < m_initialSlots; i++)
    {
      PagePtr pgptr  = new PagePtr(m_id, i, (m_needRefCounts ? m_objsInPage : 0));
      m_pages[i] = pgptr;
      m_freePageList.add(pgptr);
    }
  }

  private IPage createPage(int pgid)
  {
    IPage pg = null;
    if (m_dynamicPage)
    {
      try {
        if (m_pageConstructor != null)
        {
          Object[] args = new Object[3];
          args[0] = pgid;
          args[1] = m_pageSize;
          args[2] = m_layout;
          pg = (IPage) m_pageConstructor.newInstance(args);
        }
        else
        {
          // just in case of a problem in generating the page class
          assert false : "dynamic page generation failed.";
        }
       } catch(Exception ex) {
         LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, ex);
       }
    } 
    if (pg == null)
    {
      // In case of a problem in generating the page classs 
      pg = new Page(pgid, m_pageSize, m_layout);
    }
    return pg;
  }
  
  private synchronized PagePtr addPage()
  {
    int sz = m_pages.length;
    // no empty slot left, expand the array
    int newsz = sz + sz / 4 + 1;
    PagePtr[] npgs = new PagePtr[newsz];
    System.arraycopy(m_pages, 0, npgs, 0, sz);
    m_pages = npgs;
    for (int i = sz; i < newsz; i++)
    {
      PagePtr pgptr = new PagePtr(m_id, i, (m_needRefCounts ? m_objsInPage : 0));
      m_pages[i] = pgptr;
      m_freePageList.add(pgptr);
    }
    
    int pgid = sz;
    IPage pg = createPage(pgid);
    PagePtr ptr = m_pages[pgid];
    ptr.setPage(pg);
    return ptr;
  }
  
  public synchronized PageRef allocate() 
    throws ExecException
  {
    if (DebugUtil.DEBUG_PAGE_ASYNCFREE)
    {
      synchronized(m_freeReserved)
      {
        for (PageRef ref : m_freeReserved)
        {
          free0(ref.m_pagePtr, ref.m_index);
        }
        m_freeReserved.clear();
      }
    }
    PagePtr cur = null;
    int index = -1;
    while (index < 0)
    {
      cur =  m_freePageList.getTail();
      if (cur == null)
      {
        cur = addPage();
      }
      if (cur.isEmptySlot())
      {
        IPage pg = createPage(cur.getPageId());
        cur.setPage(pg);
      }
      
      IPage curpg = cur.pin(this, IPinnable.WRITE);
      index = curpg.alloc();
      if (index < 0)
      {
        assert (cur == m_freePageList.getTail());
        // curpage is full, remove it from the free list
        PagePtr s = m_freePageList.remove();
        if (s != null)
        {
          s.setInFreeList(false);
        }
      } 
    }
    if (DebugUtil.DEBUG_PAGE_APICALLS)
    {
      pageLogger.log(cur.getPageId(), cur, "allocIndex", index, cur.toString());
    }
    PageRef ref = m_pageRef.get();
    ref.m_pagePtr = cur;
    ref.m_index = index;
    return ref;
  }
  
  public synchronized boolean free(PagePtr pgptr, int idx)
    throws ExecException
  {
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      if (pgptr.getPageManagerId() != m_id)
        return false;
    }
    assert pgptr.getPageManagerId() == m_id : "wrong free : the page is allocated from PageManager_" + pgptr.getPageManagerId();
    if (DebugUtil.DEBUG_PAGE_APICALLS)
    {
      pageLogger.log(pgptr.getPageId(), pgptr, "freeIndex", idx, pgptr.toString());
    }
    if (DebugUtil.DEBUG_PAGE_ASYNCFREE)
    {
      assert (!pgptr.isEmptySlot()) : "the page is already freed.";
      
      PageRef ref = new PageRef();
      ref.m_pagePtr = pgptr;
      ref.m_index = idx;
      m_freeReserved.add(ref);
      return true;
    }
    return free0(pgptr, idx);
  }

  
  private synchronized boolean free0(PagePtr pgptr, int idx)
    throws ExecException
  {
    pgptr.reset(idx);
    IPage pg = pgptr.pin(this, IPinnable.WRITE);
    if (pg == null)
      return false;
    boolean emptypg = pg.free(idx);
    
    if (emptypg)
    {
      //remove from the db if it was stored
      // reset the page ptr so that it can be reused.
      pgptr.reset(this);
    }
    // check if this pg is already in the free list
    if (!pgptr.isInFreeList()) 
    {
      // add to free list
      m_freePageList.add(pgptr);
    }
    return true;
  }
  
  public void setEvicted(boolean b)
  {
    m_evicted = b;
  }
  
  public boolean evict() 
    throws ExecException
  {
    m_evicted = true;
    int count = 0;
    for (PagePtr s : m_pages)
    {
      if (s != null)
      {
        if (s.evict(this)) 
          count++;
      }
    }
    return count > 0;
  }
  public String dump(boolean verbose)
  {
    LogLevelManager lm = CEPManager.getInstance().getLogLevelManager();
    IDumpContext dumper = lm.openDumper(null, null);
    dumper.setLevel(LogArea.SPILL, (verbose ? LogLevel.SPILL_DUMP : LogLevel.SPILL_DUMP_DETAIL), verbose);
    dump(dumper);
    dumper.close();
    return dumper.toString();
  }
  
  public void dump(IDumpContext dumper)
  {
    String tag = "Factory";
    String[] attribs = {"Id", "Pages"};
    Object[] vals = new Object[2];
    vals[0] = m_id;
    vals[1] = m_pages.length;
    dumper.beginTag(tag, attribs, vals);
    int cnt = 0;
    int evcnt = 0;
    for (PagePtr s : m_pages)
    {
      if (s != null && s.m_page != null)
      {
        if (s.isEvicted())
          evcnt++;
        cnt++;
        if (dumper.isVerbose())
        {
          dumper.writeln("Page"+s.m_pageId, s.m_page.toString());
        }
      }
    }
    int pgempty = 0;
    for (PagePtr p : m_pages)
    {
      if (p.isEmptySlot())
      {
        pgempty++;
      }
    } 
    dumper.writeln("Evicted", m_evicted);
    dumper.writeln("EvictedPages", evcnt);
    dumper.writeln("EmptySlots", pgempty);
    dumper.writeln("FreePages", m_freePageList.length());
    int pagesz = m_layout.getSize();
    int objs = (int)(m_pageSize / ((float) pagesz));
    dumper.writeln("PageSize", m_pageSize);
    dumper.writeln("LayoutSize", pagesz);
    dumper.writeln("ObjsInPage", objs);
    dumper.endTag(tag);
  }

  public static class PageRef
  {
    public PagePtr m_pagePtr;
    public int  m_index;
    
    public PageRef()
    {
      m_pagePtr = null;
    }
    
    public PageRef(PagePtr pg, int idx)
    {
      m_pagePtr = pg;
      m_index = idx;
    }
    
    public IPage pin(PageManager pm, int mode)
      throws ExecException
    {
      if (m_pagePtr == null) return null;
      return m_pagePtr.pin(pm, mode);
    }
  }
}
