/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/SimplePageManager.java /main/9 2009/01/23 11:07:30 hopark Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    SimplePageManager provides a page based list/queue with the following features:
    - Pages are singly linked.
    - It can supports either Single writer/Single reader or Single writer/Multiple reader
    - Page can be individually spillable.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      01/21/09 - set thread name
    hopark      12/02/08 - move LogLevelManager to ExecContext
    hopark      10/10/08 - remove statics
    hopark      09/17/08 - support schema
    hopark      06/19/08 - logging refactor
    hopark      06/18/08 - logging refactor
    hopark      04/16/08 - add stat
    hopark      03/12/08 - add asynch evictor

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/SimplePageManager.java /main/9 2009/01/23 11:07:30 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
 package oracle.cep.memmgr;

import java.io.Externalizable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.IDumpable;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.memmgr.IAllocator.NameSpace;
import oracle.cep.service.CEPManager;
import oracle.cep.storage.IStorage;
import oracle.cep.util.StringUtil;
 
 public class SimplePageManager
 {
  public static final boolean FREE = true;
  public static final boolean NO_FREE = false;
  public static final int EVICT_ASYNC = 2;
  public static final int EVICT_SYNC = 1;
  public static final int NO_EVICT = 0;
  
  private static AtomicInteger    s_nextPageId = new AtomicInteger(0);
  private int           id;
  private int           entriesPerPage;
  private int           nPages;
  private EntryGen      entryGen;
  private NameSpace     ns;

  private EntryRef       tail;
  private EntryRef       head;

  public SimplePageManager(int id, NameSpace ns, int pgSize, EntryGen egen)
  {
    this.id = id;
    this.ns = ns;
    entriesPerPage = pgSize;
    entryGen = egen;
    nPages = 1;
    tail = createPage();
    head = tail.clone();
  }
  
  public int getPMId() {return id;}
  
  public EntryRef createPage()
  {
    int pgid = s_nextPageId.getAndIncrement();
    return new EntryRef(new PagePtr(ns, pgid), 0);
  }
  
  public void stop()
  {
    SimpleQueueEvictor evictor = SimpleQueueEvictor.getInstance();
    evictor.stop();
  }
  
  public synchronized EntryRef getHead() {return head;}
  public synchronized EntryRef getTail() {return tail;}
  
  public synchronized boolean isEmpty() 
  {
    return head.equals(tail);
  }
  
  public synchronized int getPages()
  {
    return nPages;
  }
  
  public int getNoEvictablePage()
  {
    
    int cnt = 0;
    PagePtr t = head.getPage();
    while (t != null)
    {
      if (!t.isEvicted())
      {
        cnt++;
      }
      t = t.getNext();
    }
    return cnt;
  }
  
  public synchronized SimplePageManagerStat getStat()
  {
    SimplePageManagerStat stat = new SimplePageManagerStat();
    if (tail.equals(head))
    {
      if (head != null)
      {
        PagePtr pg = head.page;
        if (pg.isEvicted())
          stat.m_tuplesInDisk = pg.getUsedEntries();
        else
          stat.m_tuplesInMem = pg.getUsedEntries();
      }
      return stat;
    }
    
    int tuplesInMem = 0;
    int tuplesInDisk = 0;
    EntryRef t = head.clone();
    while (!t.equals(tail) )
    {
      PagePtr pg = t.page;
      if (pg.isEvicted())
        tuplesInDisk += pg.getUsedEntries();
      else
        tuplesInMem += pg.getUsedEntries();
      t.advance(false, NO_EVICT);
    } 
    stat.m_tuplesInMem = tuplesInMem;
    stat.m_tuplesInDisk = tuplesInDisk;
    return stat;
  }
  
  public synchronized int getSize()
  {
    if (tail.equals(head))
      return 0;
    
    EntryRef t = head.clone();
    int pos = 0;
    while (!t.equals(tail) )
    {
      t.advance(false, NO_EVICT);
      pos++;
    }
    return pos;
  }
  
  public synchronized boolean evict(boolean async)
  {
    return evict(async, 0);
  }
  
  public synchronized boolean evict(boolean async, int skipCnt)
  {
    int cnt = 0;
    int pos = 0;
    PagePtr t = head.getPage();
    while (t != null)
    {
      if (pos >= skipCnt)
      {
        if (async)
        {
          SimpleQueueEvictor evictor = SimpleQueueEvictor.getInstance();
          evictor.add(t);
        }
        else
        {
          if (t.evict())
             cnt++;
        }
      }
      pos++;
      t = t.getNext();
    }
    return (cnt > 0);
  }

  public synchronized void dump(IDumpContext dumper)
  {
    String dumperKey = StringUtil.getBaseClassName(this);
    LogLevelManager lm = CEPManager.getInstance().getLogLevelManager();
    IDumpContext w = lm.openDumper(dumperKey, dumper);
    String tag = LogUtil.beginDumpObj(w, this);
    EntryRef t = head.clone();
    while (!t.equals(tail) )
    {
      BaseEntry e = t.pin(IPinnable.READ);
      e.dump(dumper);
      t.advance(false, NO_EVICT);
    }
    LogUtil.endDumpObj(w, tag);
    LogUtil.endDumpObj(w, tag);
    lm.closeDumper(dumperKey, dumper, w);
  }
  
  // Marker class for page entries.
  public abstract static class BaseEntry  implements Externalizable, IDumpable
  {
    public synchronized void dump(IDumpContext dumper)
    {
      String tag = LogUtil.beginDumpObj(dumper, this);
      LogUtil.endDumpObj(dumper, tag);
    }
  }

  public interface EntryGen
  {
    BaseEntry create();
  }
  
  public final class EntryRef
  {
    PagePtr page;
    int           index;
    
    public EntryRef(PagePtr p, int idx)
    {
      page = p;
      index = idx;
    }
    
    public final PagePtr getPage() { return page;}
    public final int getIndex() {return index;}
    
    public final EntryRef clone()
    {
      return new EntryRef(page, index);
    }
    
    public final void copy(EntryRef o)
    {
      page = o.page;
      index = o.index;
    }

    @SuppressWarnings("unchecked")
    public final <E extends BaseEntry> E pin(int mode)
    {
      return (E) page.pin(mode, index);
    }
    
    public final void setDirty(boolean b)
    {
      page.setDirty(b);
    }
    
    public synchronized final boolean advance(boolean free, int evict)
    {
      if (free)
        page.free(index);
      index++;
      if (index == entriesPerPage)
      {
        PagePtr oldpage = page;
        synchronized(page)
        {
          if (page.next == null)
          {
            // allocate a new page
            int newId = s_nextPageId.getAndIncrement();
            page.next = new PagePtr(ns, newId);
            nPages++;
//System.out.println(id + ":" + nPages);            
          }
          page = page.next;
          index = 0;
        }
        if (free)
        {
          oldpage.freePage();
        }
        else
        {
          if (evict == EVICT_ASYNC)
          {
            SimpleQueueEvictor evictor = SimpleQueueEvictor.getInstance();
            evictor.add(oldpage);
          }
          else if (evict == EVICT_SYNC)
          {
            oldpage.evict();
          }
        }
        return true;
      }
      return false;
    }

    public final boolean equals(EntryRef o)
    {
      return (page == o.page) && (index == o.index);
    }
  }
  
  public final class PagePtr
  {
    int     id;
    NameSpace ns;
    SimplePage entries;
    PagePtr next;
    byte flag;
    int usedEntries;    //keeps the number of entries used on eviction
    
    static final byte DIRTY = 1;
    static final byte STORED = 2;
    
    public PagePtr(NameSpace ns, int id)
    {
      this.id = id;
      this.ns = ns;
      entries = new SimplePage(entriesPerPage);
      flag = 0;
      next = null;
    }
    
    public synchronized final int getUsedEntries() 
    {
      if (entries != null)
        return entries.getUsedEntries();
      return usedEntries;
    }
    public synchronized final PagePtr getNext() {return next;}
    public synchronized final void setDirty(boolean b) { flag = (byte) (b ? (flag | DIRTY) : (flag & ~DIRTY)); }
    public synchronized final boolean isStored() {return (flag & STORED) != 0;}
    public synchronized final void setStored(boolean b)  { flag = (byte) (b ? (flag | STORED) : (flag & ~STORED)); }
    public final boolean isEvicted() {return (entries == null);}
    
    public synchronized final void freePage()
    {
      if (isStored())
      {
        IStorage storage = CEPManager.getInstance().getStorageManager().getSpillStorage();
        assert (storage != null);
        storage.deleteRecord(null, ns.toString(), null /* schema */, new Long(id));
      }
      entries = null;
      usedEntries = 0;
      next = null;
      nPages--;
      //System.out.println(getPMId() + ":" + nPages);            
    }
    
    public synchronized final void free(int index)
    {
      pinEntries(IPinnable.WRITE);
      entries.free(index);
    }
    
    public synchronized boolean evict()
    {
      if (entries == null) return false;
      usedEntries =entries.getUsedEntries();
      if ( ((flag & DIRTY) != 0) || ((flag & STORED) == 0) )
      {
        // dirty or first eviction, put to storage
        IStorage storage = CEPManager.getInstance().getStorageManager().getSpillStorage();
        assert (storage != null);
        if (!storage.putRecord(null, ns.toString(), null /* schema */, new Long(id), entries))
        {
          assert false : "failed to store " + id;
          return false;
        }
        setStored(true);
      }
      entries = null;
      return true;
    }
    
    private void pinEntries(int mode)
    {
      if (entries == null)
      {
        //it was evicted, read it back
        IStorage storage = CEPManager.getInstance().getStorageManager().getSpillStorage();
        assert (storage != null);

        entries = (SimplePage)  storage.getRecord(null, ns.toString(), new Long(id));
        assert (entries != null) : "failed to retrieve " + id;
        setDirty(false);
      } 
      if (mode != IPinnable.READ)
      {
        setDirty(true);
      }
    }
    
    public synchronized BaseEntry pin(int mode, int index)
    {
      pinEntries(mode);
      BaseEntry e = entries.get(index);
      if (e == null)
      {
        e = entryGen.create();
        entries.put(index, e);
      }
      return e;
    }
  }

  public static class SimpleQueueEvictor
  {
    static SimpleQueueEvictor s_instance = null;
    
    boolean m_running;
    LinkedBlockingQueue<PagePtr> m_queue;
    
    public synchronized static SimpleQueueEvictor getInstance()
    {
      if (s_instance == null)
      {
        s_instance = new SimpleQueueEvictor();
      }
      return s_instance;
    }
    
    SimpleQueueEvictor()
    {
      m_queue = new LinkedBlockingQueue<PagePtr>();
      m_running = true;
      new Thread(new Runnable() {
        public void run() {
          try
          {
            while (m_running) {
              PagePtr e = m_queue.take();
              //System.out.println("evict " + e.id);
              e.evict();
            }
         }
         catch(InterruptedException ex) {}
        }
      }, "SimpleQueueEvictor").start();
    }
    
    public void evictAll()
    {
      PagePtr e = null;
      do {
        e = m_queue.poll();
        e.evict();
      } while (e != null);
    }

    public void stop()
    {
      m_running = false;
    }
    
    public void add(PagePtr pg)
    {
      try
      {
        m_queue.put(pg);
      }
      catch(InterruptedException ex) {}
    }
  }
}

