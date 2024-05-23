/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/PagedFactory.java /main/8 2008/10/24 15:50:19 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/10/08 - remove statics
    hopark      03/26/08 - server reorg
    hopark      02/25/08 - fix test code
    hopark      02/25/08 - fix test code
    hopark      02/25/08 - fix test code
    hopark      02/07/08 - remove assertion on dump
    hopark      12/05/07 - cleanup spill
    hopark      10/31/07 - move IPagePtr
    hopark      10/18/07 - add refcount
    hopark      07/27/07 - handle spec update
    hopark      07/09/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/PagedFactory.java /main/8 2008/10/24 15:50:19 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.memmgr;

import oracle.cep.execution.ExecException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.PageLayout;
import oracle.cep.memmgr.PageManager;
import oracle.cep.memmgr.PageManager.PagePtr;
import oracle.cep.service.CEPManager;

public class PagedFactory<E> extends AbsAllocator<E> implements IEvictableObj
{
  protected CEPManager m_cepMgr;
  protected int m_id;
  protected NameSpace m_nameSpace;
  protected MemStat m_stat;

  protected PageLayout  m_pageLayout;
  protected PageManager m_pageMan;
  
  protected int m_initialPages; //initial page table size

  public PagedFactory(FactoryManager factoryMgr, int id, NameSpace ns, int initPages)
  {
    super(factoryMgr);
    m_cepMgr = factoryMgr.getServiceManager();
    m_id = id;
    m_nameSpace = ns;
    m_stat = new MemStat();
    m_pageLayout = null;
    m_pageMan = null;
    m_initialPages = initPages;
  }  

  public int getId() {return m_id;}
  public NameSpace getNameSpace() {return m_nameSpace;}
  public MemStat getStat() {return m_stat;}

  @SuppressWarnings("unchecked")
  public E allocate() throws ExecException 
  {
    m_stat.m_totalObjs++;
    return (E) allocBody();
  }

  protected boolean needRefCounts() {return false;}

  public Object allocBody() throws ExecException
  {
    m_cepMgr.runEvictor();

    PageManager pm = getPageManager();
    PageManager.PageRef item = pm.allocate();
    return item;
  }

  public boolean freeBody(PagePtr pg, int idx) throws ExecException
  {
    boolean b = m_pageMan.free(pg, idx);
    m_stat.m_totalObjs--;
    return b;
  }
  
  public PagePtr getPage(int pageId) 
  {
    return m_pageMan.getPage(pageId);
  }
  
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    if (m_pageMan != null)
      sb.append(m_pageMan.dump(false));
    return sb.toString();
  }

  // only used by unit testing
  public void reset()
  {
    m_pageMan.reset();
  }
 
  public void setInitialPages(int pgs)
  {
    m_initialPages = pgs;
  }
  
  protected PageLayout createPageLayout(short pageSize, short minNoObjs)
  {
    assert false;
    return null;
  }
  
  public void setPageLayout(PageLayout layout)
  {
    m_pageLayout = layout;
  }
  
  public PageLayout getPageLayout()
  {
    return m_pageLayout;
  }
  
  public PageManager getPageManager()
  {
    if (m_pageMan != null)
      return m_pageMan;
    
    // we might want to access un-initialized factory for dumping.
    // make it valid..
    if (m_pageLayout == null)
      return null;
        
    // If pageLayout is not set, create one.
    // Currently, pagedList is setting pageLayout while pagedTuple is using createPageLayout

    boolean needRc = needRefCounts(); 
    m_pageMan = new PageManager(m_cepMgr, getId(), m_pageLayout, m_initialPages, needRc);
    return m_pageMan;
  }
  
  /* (non-Javadoc)
   * @see oracle.cep.memmgr.IAllocator#dump()
   */
  public void dump()
  {
    if (m_pageMan != null)
    {
      LogUtil.finest(LoggerType.TRACE, 
          getClass().getSimpleName() + " " + this.getId() + "\n" + 
          m_pageMan.dump(false));
    }
  }

  public boolean evict() throws ExecException
  {
    if (m_pageMan != null)
      return m_pageMan.evict();
    return false;
  }

}

