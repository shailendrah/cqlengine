/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logging/trace/LogLevelManager.java /main/9 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES

 MODIFIED    (MM/DD/YY)
 hopark      04/03/11 - refactor storage
 parujain    12/07/09 - synonym
 hopark      04/21/09 - change dump api
 hopark      02/05/09 - change api
 hopark      01/26/09 - add getTypes
 hopark      01/21/09 - fix NPE
 hopark      12/02/08 - move LogLevelManager to ExecContext
 hopark      10/10/08 - remove statics
 hopark      10/09/08 - remove statics
 hopark      10/07/08 - use execContext to remove statics
 hopark      06/18/08 - refactor
 hopark      05/12/08 - user singleton
 hopark      03/26/08 - remove singleton
 hopark      02/07/08 - fix logging dump format
 hopark      01/09/08 - metadata logging
 hopark      12/27/07 - support xmllog
 hopark      08/28/07 - use singleton
 hopark      08/01/07 - add dump
 hopark      06/27/07 - support plan changes
 hopark      05/22/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logging/trace/LogLevelManager.java /main/8 2010/01/06 20:33:11 parujain Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logging.trace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import oracle.cep.exceptions.CEPException;
import oracle.cep.logging.ILogArea;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.LevelDesc;
import oracle.cep.logging.Levels;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.impl.DumpEvent;
import oracle.cep.logging.impl.LogLevelManagerBase;
import oracle.cep.metadata.QueryManager;
import oracle.cep.metadata.SystemManager;
import oracle.cep.metadata.SynonymManager;
import oracle.cep.metadata.TableManager;
import oracle.cep.metadata.UserFunctionManager;
import oracle.cep.metadata.ViewManager;
import oracle.cep.metadata.WindowManager;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;
import oracle.cep.storage.StorageManager;
import oracle.cep.util.ArrayUtil;

public class LogLevelManager extends LogLevelManagerBase
{
  PlanMonitor m_planMonitor;
  ExecContext m_execContext = null;
  CEPManager  m_cepMgr;
  
  public LogLevelManager(CEPManager cepMgr)
  {
    m_cepMgr = cepMgr;
    m_planMonitor = null;
  }

  public ExecContext getExecContext() {return m_execContext;}
  public void setExecContext(ExecContext ec) { m_execContext = ec;}
  
  public PlanMonitor getPlanMonitor()
  {
    if (m_planMonitor == null) {
      m_planMonitor = new PlanMonitor(this);
    }
    return m_planMonitor;
  }

  public void clear()
  {
    super.clear();
    if (m_planMonitor != null)
      m_planMonitor.clear();
  }

  public void dumpLog(
    ILogArea area,
    List<Integer> types, List<Integer> ids, 
    List<Integer> levels) throws CEPException
  {
    if (area.isSystem())
    {
      LogLevelManager lm = m_execContext.getServiceManager().getLogLevelManager();
      lm.dumpLog0(area, types, ids, levels);
    } 
    else
    {
      dumpLog0(area, types, ids, levels);
    }
  }
  
  private synchronized void dumpLog0(
      ILogArea area,
      List<Integer> types, List<Integer> ids, 
      List<Integer> levels) throws CEPException
  {
    PlanMonitor pm = getPlanMonitor();
    ILoggable target = null;
    if (m_execContext == null)
    {
      if (area == LogArea.SPILL)
        target = m_cepMgr.getEvictPolicy();
      else if (area == LogArea.STORAGE) 
      {
        StorageManager sm = m_cepMgr.getStorageManager();
        if (sm != null)
        {
          //TODO currently assuming metadata storage = spill storage
          // it should be identified by type.
          target = (ILoggable) sm.getMetadataStorage();
          if (target == null)
            target = (ILoggable) sm.getSpillStorage();
        }
      } 
      if (target != null)
      {
        dumpLevels(area, DumpEvent.DUMP, target, levels);
      }
      return;
    }

    if (area == LogArea.SYSTEMSTATE)
    {
      target = new SystemState(m_execContext);
    }
    else if (area == LogArea.METADATA_QUERY)
    {
      QueryManager qm = m_execContext.getQueryMgr();
      target = qm;
    }
    else if (area == LogArea.METADATA_SYSTEM)
    {
      SystemManager sysman = m_execContext.getServiceManager().getSystemMgr();
      target = sysman;
    }
    else if (area == LogArea.METADATA_TABLE) 
    {
      TableManager tableman = m_execContext.getTableMgr();
      target = tableman;
    }
    else if (area == LogArea.METADATA_USERFUNC)
    {
      UserFunctionManager ufm = m_execContext.getUserFnMgr();
      target = ufm;
    }
    else if (area == LogArea.METADATA_VIEW)
    {
      ViewManager viewman = m_execContext.getViewMgr();
      target = viewman;
    }
    else if (area == LogArea.METADATA_WINDOW)
    {
      WindowManager winman = m_execContext.getWindowMgr();
      target = winman;
    }
    else if (area == LogArea.METADATA_SYNONYM)
    {
      SynonymManager synman = m_execContext.getSynonymMgr();
      target = synman;
    }
    else
    {
      List<ILogEvent> events = new ArrayList<ILogEvent>(1);
      events.add(DumpEvent.DUMP);
      pm.dumpLog(area, types, ids, events, levels);
    }
    if (target != null)
    {
      dumpLevels(area, DumpEvent.DUMP, target, levels);
    }
  }
  
  static class CacheEvent 
  {
    ILogArea    area;
    ILogEvent   event;
    ILogEvent   cacheEvent;
    CacheEvent(LogArea a, ILogEvent e, ILogEvent ce)
    {
      area = a;
      event = e;
      cacheEvent = ce;
    }
  }
  static CacheEvent[] s_cacheEventMaps = {
    new CacheEvent(LogArea.METADATA_QUERY, LogEvent.MQUERY_CREATE, LogEvent.MCACHE_CREATE),
    new CacheEvent(LogArea.METADATA_QUERY, LogEvent.MQUERY_DELETE, LogEvent.MCACHE_DELETE),
    new CacheEvent(LogArea.METADATA_TABLE, LogEvent.MTABLE_CREATE, LogEvent.MCACHE_CREATE),
    new CacheEvent(LogArea.METADATA_TABLE, LogEvent.MTABLE_DELETE, LogEvent.MCACHE_DELETE),
    new CacheEvent(LogArea.METADATA_WINDOW, LogEvent.MWINDOW_CREATE, LogEvent.MCACHE_CREATE),
    new CacheEvent(LogArea.METADATA_WINDOW, LogEvent.MWINDOW_DELETE, LogEvent.MCACHE_DELETE),
    new CacheEvent(LogArea.METADATA_USERFUNC, LogEvent.MUSERFUNC_CREATE, LogEvent.MCACHE_CREATE),
    new CacheEvent(LogArea.METADATA_USERFUNC, LogEvent.MUSERFUNC_DELETE, LogEvent.MCACHE_DELETE),
    new CacheEvent(LogArea.METADATA_VIEW, LogEvent.MVIEW_CREATE, LogEvent.MCACHE_CREATE),
    new CacheEvent(LogArea.METADATA_VIEW, LogEvent.MVIEW_DELETE, LogEvent.MCACHE_DELETE),
    new CacheEvent(LogArea.METADATA_SYSTEM, LogEvent.MSYSTEM_CREATE, LogEvent.MCACHE_CREATE),
    new CacheEvent(LogArea.METADATA_SYSTEM, LogEvent.MSYSTEM_DELETE, LogEvent.MCACHE_DELETE),
    new CacheEvent(LogArea.METADATA_SYNONYM, LogEvent.MSYNONYM_CREATE, LogEvent.MCACHE_CREATE),
    new CacheEvent(LogArea.METADATA_SYNONYM, LogEvent.MSYNONYM_DELETE, LogEvent.MCACHE_DELETE),
  };
    
  public synchronized void setLevelsWithTypesIds(boolean enable, ILogArea area,
      List<Integer> types, List<Integer> ids, List<ILogEvent> events,
      List<Integer> levels) throws CEPException
  {
    if (area.isSystem())
    {
      //copy it to system loglevelmanager as well..
      //this is to avoid if (area.isSystem()) in trace..
      LogLevelManager lm = m_execContext.getServiceManager().getLogLevelManager();
      lm.setLevels(enable, area, 0, events, levels);
    }
    if (area.isGlobal())
    {
      setLevels(enable, area, 0, events, levels);
      // If LOCK_INFO level is used, apply the same to METADATA_CACHE.
      if ( (area == LogArea.METADATA_QUERY ||
            area == LogArea.METADATA_SYSTEM ||
            area == LogArea.METADATA_TABLE ||
            area == LogArea.METADATA_USERFUNC ||
            area == LogArea.METADATA_VIEW ||
            area == LogArea.METADATA_SYNONYM ||
            area == LogArea.METADATA_WINDOW))
      {
        List<Integer> clevels = new ArrayList<Integer>(1);
        clevels.add(LogLevel.MCACHE_LOCKINFO);
        List<ILogEvent> cevents = new LinkedList<ILogEvent>();
        cevents.add(LogEvent.MCACHE_RELEASE_LOCK);
        cevents.add(LogEvent.MCACHE_ACQUIRE_READ_LOCK);
        cevents.add(LogEvent.MCACHE_ACQUIRE_WRITE_LOCK);
        if (events != null)
        {
          for (ILogEvent ev : events)
          {
            for (CacheEvent ce : s_cacheEventMaps)
            {
              if (ce.area == area && ce.event == ev)
              {
                cevents.add(ce.cacheEvent);
              }
            }
          }
        }
        if (levels != null)
        {
          for (Integer level : levels)
          {
            // LogLevels for metadata lockinfo are same. 
            if (level == LogLevel.MCACHE_LOCKINFO)
            {
              setLevels(enable, LogArea.METADATA_CACHE, 0, cevents, clevels);
              break;
            }
          }
        }
      }
    }
    else
    {
      //identify targets using the plan monitor
      PlanMonitor pm = getPlanMonitor();
      pm.setLevel(enable, area, types, ids, 
                  events, levels, true);
    }
  }
  
  /**
   * log common levels for queue, store, synopsis, and index.
   * 
   * @param area : target area
   * @param ctx  : execution context
   * @param target : target context
   * @param args : arguments for building a message
   */
  @SuppressWarnings("unchecked")
  public static void trace(ILogArea area, ILogEvent event, ILoggable target, 
                           Object ... args) 
  {
    assert (target != null);
    
    LogLevelManager lm = (LogLevelManager) target.getLogLevelManager();
    if (lm == null)
    {
      LogUtil.warning(LoggerType.TRACE, "No ILoggable target is set for class:" +
        target.getClass().getName() + 
        " id:" + target.getTargetId() );
      return;
    }
    Levels levels = lm.getLevels(area, target.getTargetId(), event);
    if (levels != null)
    {
      lm.traceLevels(area, event, target, levels, args);
    }
  }

  public ILogEvent[] getEvents(ILogArea area)
  {
    if (area == LogArea.QUERY)
      area = LogArea.OPERATOR;
    return LogEvent.getEvents(area);
  }

  public List<Integer> getEvents(ILogArea area, int id)
  {
    if (area == LogArea.QUERY)
      area = LogArea.OPERATOR;
    List<Integer> events = new LinkedList<Integer>();
    ILogEvent[] evs = LogEvent.getEvents(area);
    if (evs != null && evs.length > 0)
    {
      for (ILogEvent ev : evs)
      {
        events.add(ev.getValue());
      }
    }
    return events;
  }

  public String getLevelDesc(ILogArea area, int level)
  {
    if (area == LogArea.QUERY)
      area = LogArea.OPERATOR;
    return LogLevel.getLevelDesc(area, level);
  }

  public LevelDesc[] getLevelDescs(ILogArea area)
  {
    // Query uses the same event as operators.
    if (area == LogArea.QUERY)
      area = LogArea.OPERATOR;
    return LogLevel.getLevelDescs(area);
  }

  public int[] getLevels(ILogArea area)
  {
    if (area == LogArea.QUERY)
      area = LogArea.OPERATOR;
    return LogLevel.getLevels(area);
  }

  public ILogArea getLogAreaFromValue(int v)
  {
    return LogArea.fromValue(v);
  }

  public ILogArea[] getLogAreas()
  {
    Collection<ILogArea> vs = LogArea.values();
    ILogArea[] res = new ILogArea[vs.size()];
    ArrayUtil.fromCollection(vs, res);
    return res;
  }

  public ILogEvent getLogEventFromValue(int v)
  {
    return LogEvent.fromValue(v);
  }

  @Override
  public void set(boolean enable, ILogArea area, int id, ILogEvent event, int level)
  {
    List<Integer> types = null;
    List<ILogEvent> events = null;
    if (event != null)
    {
      events = new ArrayList<ILogEvent>();
      events.add(event);
    }
    List<Integer> ids = null;
    if (id >= 0)
    {
      ids = new ArrayList<Integer>();
      ids.add(id);
    }
    List<Integer> levels = null;
    if (level >= 0)
    {
      levels = new ArrayList<Integer>();
      levels.add(level);
    }
    try
    {
      setLevelsWithTypesIds(enable, area, types, ids, events, levels);
    }
    catch (CEPException e)
    {
      LogUtil.warning(LoggerType.TRACE, e.toString());
      LogUtil.logStackTrace(e);
    }
  }

  @Override
  public void dump(ILogArea area, int id, int level)
  {
    List<Integer> types = null;
    List<Integer> ids = null;
    if (id >= 0)
    {
      ids = new ArrayList<Integer>();
      ids.add(id);
    }
    List<Integer> levels = null;
    if (level >= 0)
    {
      levels = new ArrayList<Integer>();
      levels.add(level);
    }
    try
    {
      dumpLog(area, types, ids, levels);
    }
    catch (CEPException e)
    {
      LogUtil.warning(LoggerType.TRACE, e.toString());
      LogUtil.logStackTrace(e);
    }
  }
}
