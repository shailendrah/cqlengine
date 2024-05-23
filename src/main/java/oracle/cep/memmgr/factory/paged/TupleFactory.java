/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/paged/TupleFactory.java /main/9 2009/06/15 22:04:06 hopark Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      06/11/09 - add refcount dump
    hopark      10/10/08 - remove statics
    hopark      10/16/08 - implement ITupleAllocator
    hopark      06/17/08 - fix needRefCounts
    hopark      03/17/08 - fix remove
    hopark      03/17/08 - fix remove
    hopark      03/17/08 - fix remove
    hopark      03/05/08 - xml spill
    hopark      02/28/08 - refcount
    hopark      10/31/07 - move IPagePtr
    hopark      07/27/07 - handle spec update
    hopark      07/09/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/paged/TupleFactory.java /main/9 2009/06/15 22:04:06 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.memmgr.factory.paged;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.xml.sax.SAXException;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.paged.TuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IManagedObj;
import oracle.cep.memmgr.IPage;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.memmgr.ITupleAllocator;
import oracle.cep.memmgr.PageLayout;
import oracle.cep.memmgr.PagedFactory;
import oracle.cep.memmgr.PageManager.PagePtr;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.service.CEPManager;
import oracle.cep.util.DebugLogger;
import oracle.cep.util.DebugUtil;

public class TupleFactory extends PagedFactory<ITuplePtr>
  implements TupleSpec.IChgNotifier, ITupleAllocator
{
  protected Map<Long, ITuplePtr> m_tupleMap;
  public static DebugLogger s_refCountLogger;

  static
  {
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      s_refCountLogger = new DebugLogger("RefCount", DebugLogger.METHOD, "getRefCnt", false);
    }
   }
  
  /** TupleSpec for this factory */
  protected TupleSpec m_tupleSpec;
  
  public TupleFactory(CEPManager cepMgr, TupleSpec spec, int id, int initPageTableSize)
  {
    super(cepMgr.getFactoryManager(), id, NameSpace.TUPLEPTR, initPageTableSize);
    m_tupleSpec = spec;
    m_tupleSpec.addListener(this);

    ConfigManager cm = cepMgr.getConfigMgr();
    short pageSize = (short) cm.getTuplePageSize();
    short minObjsPage = (short) cm.getTupleMinNodesPage();
    PageLayout layout = TuplePtr.getPageLayout(id, m_tupleSpec, pageSize, minObjsPage);
    this.setPageLayout(layout);
    m_tupleMap = new HashMap<Long, ITuplePtr>();
  }

  /**
   * We need the reference counting for TuplePtr.
   * This flag makes PagePtr allocate reference counters additionaly.
   */
  protected boolean needRefCounts() {return true;}
    
  protected TuplePtr allocTuplePtr() {return new TuplePtr();}
  
  public ITuplePtr allocate() throws ExecException
  {
    TuplePtr ref = allocTuplePtr();
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      s_refCountLogger.log(ref.getId(), ref, "allocate", "factoryId="+m_id);
    }    
    ref.setFactory(this);
    put(ref);
    return ref;
  }
  
  /**
   * @return Returns the tupleSpec.
   */
  public TupleSpec getTupleSpec()
  {
    return m_tupleSpec;
  }
  
  public int addRef(ITuplePtr element)
  {
    assert (element instanceof TuplePtr);
    TuplePtr t = (TuplePtr) element;
    int rc = t.addRef(1);
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      s_refCountLogger.log(element.getId(), element, "addRef");
    }    
    return rc;
  }
  
  /**
   * Adds new references to the specified StorageElement. This increments an
   * internal reference count by the specified count.
   * 
   * @param element
   *          The StorageElement to which the reference needs to be added.
   * @param ref
   *          The number of new references.
   */
  public int addRef(ITuplePtr element, int ref)
  {
    assert (element instanceof TuplePtr) : element.getClass().getName();
    TuplePtr t = (TuplePtr) element;
    int rc = t.addRef(ref);
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      s_refCountLogger.log(element.getId(), element, "addRef", ref);
    }    
    return rc;
  }
  
  /**
   * Removes existing reference to the specified StorageElement. This decrements
   * an internal reference count by 1.
   * 
   * @param element
   *          The StorageElement to which the reference needs to be added.
   */
  public int release(ITuplePtr element)
  {
    assert (element instanceof TuplePtr);
    TuplePtr t = (TuplePtr) element;
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      if (t.getFactory() != this)
      {
        TupleFactory fac2 = t.getFactory();
        fac2.s_refCountLogger.print(element.getId(), DebugLogger.TRACE_HISTORY);
      }
    }
    assert (t.getFactory() == this) : "freeing from wrong factory.";
    int rc = t.release();
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      s_refCountLogger.log(element.getId(), element, "release");
    }    
    if (rc == 0)
    {
      long id = element.getId();
      if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
      {
        s_refCountLogger.log(id, element, "free");
      }    
      // It's possible that PagePtr is null for TuplePtr
      // e.g. created and not used (RelSource:172)
      PagePtr pg = t.getPage();
      if (pg != null)
      {
        // free the entry from the page.
        int idx = t.getIndex();
        try
        {
          // Free managed objects if any.
          short[] objattrs = m_pageLayout.getManagedObjAttribs();
          if (objattrs != null)
          {
            IPage p = pg.pin(this.m_pageMan, IPinnable.READ);
            byte[] types = m_pageLayout.getTypes();
            for (short objattr : objattrs)
            {
              Object obj = null;
              if (types[objattr] == PageLayout.OBJ)
              {
                obj = p.oValueGet(idx, objattr);
                p.oValueSet(idx, objattr, null);
              }
              else if (types[objattr] == PageLayout.XML)
              {
                obj = p.xObjValueGet(idx, objattr, null);
                p.xObjValueSet(idx, objattr, null);
              }
              if (obj != null)
              {
                if (obj instanceof IManagedObj)
                {
                  IManagedObj mobj = (IManagedObj) obj;
                  mobj.free(this);
                }
              }
            }
          }
        }
        catch(Exception e)
        {
          LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
        }
        try
        {
          boolean b = freeBody(pg, idx);
          if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
          {
            if (!b) 
            {
              s_refCountLogger.print(element.getId(), DebugLogger.TRACE_HISTORY);
              assert false : "failed to free. potential ref count problem id=" + t.getId();
            }
          }
        }
        catch(Exception e)
        {
          LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
        }
      }
      remove(id);
    }
    return rc;
  }
  
  public void logRefCount(long id, Object elem, Object... args)
  {
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      s_refCountLogger.log(id, elem, args);
    }
  }
  
  public void dumpRefCount(long id)
  {
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      s_refCountLogger.print(id, DebugLogger.ALL);
    }
  }
  
  public int dumpRefCount(java.io.Writer writer)
  {
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      String desc = this.getClass().getName() + "_" + this.getId();
      return s_refCountLogger.printAll(writer, desc, DebugLogger.DEFAULT);
    }
    return 0;
  }

  public void clearRefCount()
  {
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      s_refCountLogger.clear();
    }
  }
  
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("<TupleFactory id=\"" + getId() + "\">");
    if (m_tupleSpec != null)
      sb.append(m_tupleSpec.toString());
    sb.append("</TupleFactory>\n");
    if (m_pageMan != null)
      sb.append(m_pageMan.dump(false));
    return sb.toString();
  }

  /* (non-Javadoc)
   * @see oracle.cep.execution.internals.TupleSpec.IChgNotifier#attrAdded(int)
   */
  public void attrAdded(int pos)
  {
    assert (false);
    m_pageMan.reset();
    m_pageMan = null;
  }

  /* (non-Javadoc)
   * @see oracle.cep.execution.internals.TupleSpec.IChgNotifier#attrUpdated(int)
   */
  public void attrUpdated(int pos)
  {
    assert (false);
    m_pageMan.reset();
    m_pageMan = null;
  }

  public synchronized void put(ITuplePtr tp)
  {
    long id = tp.getId();
    ITuplePtr e = m_tupleMap.get(id);
    if (e == null)
    {
      m_tupleMap.put(id, tp);
      if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
      {
        TupleFactory.s_refCountLogger.log(id, null, "put to map, factory="+getId());
      }
    } 
  }
  
  public synchronized ITuplePtr get(long id)
  {
    ITuplePtr res = m_tupleMap.get(id);
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      if (res == null)
      {
        TupleFactory.s_refCountLogger.log(id, null, "failed to get from map, factory="+getId());
        TupleFactory.s_refCountLogger.print(id, DebugLogger.TRACE_HISTORY);
      }
    }
    assert (res != null) : "Failed to get a tuple from map : potential refcount problem " + id + " factory=" + getId();
    return res;
  }
  
  public synchronized void remove(long id)
  {
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      TupleFactory.s_refCountLogger.log(id, null, "remove from map, factory="+getId());
    }
    m_tupleMap.remove(id);
  }
  
  public void addRefCountLog(long id, String msg)
  {
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      TupleFactory.s_refCountLogger.log(id, null, msg);
    }
  }
}

