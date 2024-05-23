/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logging/trace/PlanMonitor.java /main/7 2012/08/01 21:02:00 alealves Exp $ */

/* Copyright (c) 2007, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    PlanMonitor handles setting logging levels that need to look up the global plan.
    It also monitors changes of the global plan and applies changes
    accordingly. 

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    The logic of applying changes of plan
    When the plan is changes, there are two types of operations:
    - reapplying set_levels
        This is for setLevel operations without ID.
          SystemState
          OPERATOR, SYNOPSIS, STORE with type
          all types without id and type
    - clear_levels
        This is for setLevel operations with ID.
    Basically, we are applying these operations on changes.
    These operations are encapsulated in LogItem and created from setLevel api.
    The changes are encapsulated in PlanChange and detected from IQueryChgNotifier.
    For adding a query, we simply iterate the plan for a query ID and include
    all objects as changes.
    For droping a query, we iterate the plan twice, before dropping and after dropping,
    and generate the change set from (before - after). 
    It is done this way because some objects are shared. 

   MODIFIED    (MM/DD/YY)
    parujain    05/08/09 - lifecycle mgmt
    hopark      12/02/08 - move LogLevelManager to ExecContext
    hopark      11/22/08 - show setLevel info
    hopark      10/10/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    hopark      06/18/08 - logging refactor
    hopark      02/06/08 - fix concurrent mod exception
    hopark      01/09/08 - metadata logging
    hopark      01/04/08 - make PlanVisition public
    hopark      12/21/07 - turn off test flags
    hopark      11/04/07 - fix plan update
    hopark      08/01/07 - add dump
    hopark      07/13/07 - bug fix
    hopark      06/27/07 - support plan changes
    hopark      06/12/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logging/trace/PlanMonitor.java /main/7 2012/08/01 21:02:00 alealves Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logging.trace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.lang.Thread;

import oracle.cep.exceptions.CEPException;
import oracle.cep.metadata.QueryManager;
import oracle.cep.phyplan.PhyIndex;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyQueue;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.planmgr.IPlanVisitor;
import oracle.cep.planmgr.PlanManager;
import oracle.cep.service.ExecContext;
import oracle.cep.service.IQueryChgListener;
import oracle.cep.util.ArrayUtil;
import oracle.cep.util.CSVUtil;
import oracle.cep.logging.ILogArea;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.impl.DumpEvent;

public class PlanMonitor implements IQueryChgListener
{
  //private static final boolean TEST_SETLEVEL = false;
  private static final boolean TEST_OPERATOR_SHORTCUT = false;
  private static final boolean TEST_UPDATEPLAN = false;
  
  LogLevelManager m_logLevelManager;

  int             m_subscription;

  /** Assumption is that plan change only happens within a single thread
  If a plan can be changed with different threads, the PlanState
  should be stored in a ThreadLocal 
  e.g
  ThreadLocal<PlanState> m_queryChgContext;
   */
  PlanState            m_queryChgContext;
  List<LogItem>        m_logItems;
  ILogLevelChgNotifier  m_chgNotifier;

  // For unit testing...
  public interface ILogLevelChgNotifier
  {
    void change(boolean enable, ILogArea area, int id,
          List<ILogEvent> events, List<Integer> levels);
  }
  public PlanMonitor(LogLevelManager lf)
  {
    this.m_logLevelManager = lf;
    m_subscription = -1;
    m_logItems = new LinkedList<LogItem>();
    m_chgNotifier = null;
    m_queryChgContext = null;
  }

  public void clear()
  {
    m_logItems = new LinkedList<LogItem>();
  }

  @Override
  public void onQueryAdded(String queryName, int queryId, String schemaName, Object context)
  {
    // empty
  }
  
  @Override
  public void onQueryStopped(String queryName, int qryId, String schemaName,
      Object execContext)
  {
    // empty
  }
  
  @Override
  public void onQueryStarted(String queryName, int queryId,
       String schemaName, Object context)
  {
    try 
    {
      m_queryChgContext = new PlanState(((ExecContext) context).getPlanMgr(), queryId);
      LogUtil.finer(LoggerType.TRACE, "query started " + queryId + "\n" + m_queryChgContext.toString());
      m_queryChgContext.apply(true, null, m_logItems);
    }
    catch (CEPException e)
    {
      LogUtil.fine(LoggerType.TRACE, e.toString());
    }
    if (TEST_UPDATEPLAN)
    {
      LogUtil.finer(LoggerType.TRACE, m_logLevelManager.toString());
    }
  }

  @Override
  public void onBeforeQueryDrop(String queryName, int queryId,
      String schemaName, Object context)
  {
    if (m_queryChgContext == null) 
    {
      m_queryChgContext = new PlanState(((ExecContext) context).getPlanMgr());
      LogUtil.finer(LoggerType.TRACE, "about to drop query " + queryId + "\n" + m_queryChgContext.toString());
    }
  }

  @Override
  public void onAfterQueryDrop(String queryName, int queryId,
      String schemaName, Object context)
  {
    if (m_queryChgContext == null) 
    {
      assert false : "drop query notification does not match.";
      m_queryChgContext = new PlanState(((ExecContext) context).getPlanMgr());
      return;
    } else {
      assert m_queryChgContext.getThreadId() == Thread.currentThread().getId();
    }
    try 
    {
      PlanState s = new PlanState(((ExecContext) context).getPlanMgr());
      LogUtil.finer(LoggerType.TRACE, "query dropped" + queryId + "\n" + s.toString());
      s.apply(false, m_queryChgContext, m_logItems);
      m_queryChgContext = s;
    }
    catch (CEPException e)
    {
      LogUtil.fine(LoggerType.TRACE, e.toString());
    }
    if (TEST_UPDATEPLAN)
    {
      //LogUtil.finer(LoggerType.TRACE, m_logLevelManager.toString());
    }
  }

  void setEventsLevels(boolean enable, ILogArea area, int id, 
                       List<ILogEvent> events, List<Integer> levels,
                       boolean updateLogItem)
  {
    if (levels == null)
      return;

    if (updateLogItem && enable)
    {
      addLogItem(LogItem.REMOVE, area, -1, id, events, levels);
    }
    if (m_chgNotifier != null)
    {
      m_chgNotifier.change(enable, area, id, events, levels);
    }
    if (id >= 0)
    {
      m_logLevelManager.setLevels(enable, area, id, events, levels);
    }
  }

  /** 
   * Gets all operators for given a list of query ids.
   * 
   * @param ids : list of query ids
   * @return
   */
  public synchronized List<PhyOpt> getOperatorsForQuery(List<Integer> ids)
  {
    List<PhyOpt> operators = new LinkedList<PhyOpt>();
    ExecContext  ec = m_logLevelManager.getExecContext();
    PlanManager pm = ec.getPlanMgr();
    PlanVisitor v = new PlanVisitor(operators);
    for (Integer id : ids) 
    {
      PhyOpt root = pm.getQueryRootOpt(id);
      if (root != null) 
      {
        root.accept(v);
      }
    }
    return operators;
  }

  private <T> List<LogTarget> getLogTargets(List<T> objs, List<Integer> ids, boolean checktype)
  {
    List<LogTarget> result = new ArrayList<LogTarget>(objs.size());
    LogTarget t;
    for (T obj : objs) 
    {
      if (obj == null) continue;
      int id = -1; 
      int type = -1;
      ILoggable target = null;
      if (obj instanceof PhyOpt)
      {
        PhyOpt opt = (PhyOpt) obj;
        id = opt.getId();
        type = opt.getOperatorKind().ordinal();
        target = opt.getInstOp();
      } else if (obj instanceof PhySynopsis)
      {
        PhySynopsis syn = (PhySynopsis) obj;
        id = syn.getId();         
        type = syn.getKind().ordinal(); 
        target = syn.getSyn();
      } else if (obj instanceof PhyStore)
      {
        PhyStore store = (PhyStore) obj;
        id = store.getId();         
        type = store.getStoreKind().ordinal(); 
        target = store.getInstStore();
      } else if (obj instanceof PhyQueue)
      {
        PhyQueue queue = (PhyQueue) obj;
        id = queue.getId();         
        type = -1; 
        target = queue.getInstQueue();
      } else if (obj instanceof PhyIndex)
      {
        PhyIndex index = (PhyIndex) obj;
        id = index.getId();         
        type = -1; 
        target = index.getInst();
      } else {
        assert false;
      }
      if ( ids == null ||
           (checktype && ids.contains(type)) ||
           (!checktype && ids.contains(id)) )
      {
        t = new LogTarget(id, target, obj);
        result.add(t);
      } 
    }
    if (result.size() == 0) return null;
    return result;
  }


  /**
   * Gets a list of LogTarget for a list of ids or types 
   * 
   * @param ids : list of ids or types (maybe null)
   * @param checktype : true to check types
   * @return
   */
  private List<LogTarget> getLogTargets(ILogArea area, List<Integer> ids, boolean checktype)
  {
    ExecContext  ec = m_logLevelManager.getExecContext();
    if (area == LogArea.QUERY)
    {
      if (ids == null)
      {
        ArrayList<Integer> qids = new ArrayList<Integer>();
        PlanManager pm = ec.getPlanMgr();
        ArrayList<Integer> qs= pm.getRootQueryIds();
        qids.addAll(qs);
        List<PhyOpt> objs = getOperatorsForQuery(qids);
        return getLogTargets(objs, null, false);
      }
      List<PhyOpt> objs = getOperatorsForQuery(ids);
      return getLogTargets(objs, null, false);
    }
    else if (area == LogArea.OPERATOR)
    {
      ArrayList<PhyOpt> objs = new ArrayList<PhyOpt>();
      PlanManager pm = ec.getPlanMgr();
      Collection<PhyOpt> pobjs = pm.getActivePhyOpt();
      objs.addAll(pobjs);
      return getLogTargets(objs, ids, checktype);
    }
    else if (area == LogArea.SYNOPSIS)   
    {
      ArrayList<PhySynopsis> objs = new ArrayList<PhySynopsis>();
      PlanManager pm = ec.getPlanMgr();
      Collection<PhySynopsis> pobjs = pm.getActivePhySyn();
      objs.addAll(pobjs);
      return getLogTargets(objs, ids, checktype);
    }
    else if (area == LogArea.STORE)
    {
      ArrayList<PhyStore> objs = new ArrayList<PhyStore>();
      PlanManager pm = ec.getPlanMgr();
      Collection<PhyStore> pobjs = pm.getActivePhyStore();
      objs.addAll(pobjs);
      return getLogTargets(objs, ids, checktype);
    }
    else if (area == LogArea.QUEUE)
    {
      List<PhyQueue> objs = new LinkedList<PhyQueue>();
      PlanManager pm = ec.getPlanMgr();
      Collection<PhyOpt> opts = pm.getActivePhyOpt();
      for (PhyOpt opt : opts) {
        if (opt == null)
          continue;
        PhyQueue outQ = opt.getOutQueue();
        if (outQ != null) {
          if (!objs.contains(outQ))
            objs.add(outQ);
        }
        PhyQueue[] inQs = opt.getInQueues();
        if (inQs != null) {
          for (PhyQueue q : inQs) {
            if (!objs.contains(q))
              objs.add(q);
          }
        }
      }
      return getLogTargets(objs, ids, false);
    }
    else if (area == LogArea.INDEX)
    {
      List<PhyIndex> objs = new LinkedList<PhyIndex>();
      PlanManager pm = ec.getPlanMgr();
      Collection<PhyStore> stores = pm.getActivePhyStore();
      Collection<PhySynopsis> syns = pm.getActivePhySyn();
      for (PhySynopsis syn : syns) 
      {
        if (syn == null)
          continue;
        List<PhyIndex> indexes = syn.getIndexes();
        if (indexes != null) 
        {
          for (PhyIndex idx : indexes) 
          {
            if (!objs.contains(idx))
              objs.add(idx);
          }
        }
      }
      for (PhyStore store : stores) 
      {
        if (store == null)
          continue;
        List<PhyIndex> indexes = store.getIndexes();
        if (indexes != null) {
          for (PhyIndex idx : indexes) 
          {
            if (!objs.contains(idx))
              objs.add(idx);
          }
        }
      }
      return getLogTargets(objs, ids, false);
    }
    else
    {
      assert false;
    }
    return null;
  }

  /**
   * Prepare event/level shotcuts for operator.
   * 
   * @param events - events specified in ddl
   * @param levels - levels specified in ddl
   * @param opevents - events that can be applied to operators only
   * @param oplevels - levels that can be applied to operators only
   * @param dsevents - events that can be applied to underlying data structures only
   * @param dslevels - levels that can be applied to underlying data structures only
   */
  private void applyOperatorShortcuts(List<ILogEvent> events,
      List<Integer> levels, List<ILogEvent> opevents, List<Integer> oplevels,
      EventMap dsevents, LevelMap dslevels)
  {
    boolean has_ds_events = false;
    if (events != null) {
      for (ILogEvent event : events) 
      {
        int opdsIdx = event.getOpDSIndex();
        if (opdsIdx >= 0)
        {
          EventMap evmap = null;
          if (event != DumpEvent.DUMP)
          {
            try 
            {
              evmap = operatorEvents[opdsIdx];
            } catch (ArrayIndexOutOfBoundsException ex) 
            {
            }
          } else 
          {
            evmap = dumpDSEVent;
            opevents.add(event);
          }
          if (evmap != null)
          {
            dsevents.add(evmap);
            has_ds_events = true;
          }
        } 
        else 
        {
          opevents.add(event);
        }
      }
    }
    assert (levels != null);
    for (Integer level : levels) 
    {
      if (level >= LogLevel.OPERATOR_STRUCTURES_STATS
          && level <= LogLevel.OPERATOR_STRUCTURES_MOST) 
      {
        LevelMap lmap = null;
        try 
        {
          lmap = operatorLevels[level - LogLevel.OPERATOR_STRUCTURES_STATS];
        } catch (ArrayIndexOutOfBoundsException ex) 
        {
        }
        if (lmap != null && has_ds_events) 
        {
          dslevels.add(lmap, dsevents);
        }
      } 
      else 
      {
        oplevels.add(level);
      }
    }
    if (oplevels.size() > 0 && (opevents == null || opevents.size() == 0))
    {
      if (opevents == null)
        opevents = new ArrayList<ILogEvent>(1);
      // if operator events is not specified but level is given, turn on the default levels
      opevents.add(LogEvent.OPERATOR_RUN_BEGIN);
    }
if (TEST_OPERATOR_SHORTCUT) 
{
    if (events != null) {
      LogUtil.finer(LoggerType.TRACE, "input events = " + CSVUtil.fromList(events));
    }
    LogUtil.finer(LoggerType.TRACE, "input levels = " + CSVUtil.fromList(levels));
    if (opevents != null) {
      LogUtil.finer(LoggerType.TRACE, "operator events = " + CSVUtil.fromList(opevents));
    }
    LogUtil.finer(LoggerType.TRACE, "operator levels = " + CSVUtil.fromList(oplevels));
    LogUtil.finer(LoggerType.TRACE, "ds events\n" + dsevents.toString());
    LogUtil.finer(LoggerType.TRACE, "ds levels\n" + dslevels.toString());
}    
  }

  private void addLogItem(boolean reapply,
      ILogArea area, int type, int id,
      List<ILogEvent> events, List<Integer> levels)
  {
    if (events == null)
    {
      events = new ArrayList<ILogEvent>(1);
      events.add(null);
    }
    for (ILogEvent event : events)
    {
      boolean found = false;
      for (LogItem item : m_logItems)
      {
        // find the same type or id and update events, levels
        if (item.m_area == area &&
            item.m_event == event &&
            ( (type >= 0 && item.m_type == type) ||
              (id >= 0 && item.m_id == id)) )
        {
          found = true;
          // found one, update the levels
          int cnt = item.updateLevels(reapply, levels);
          if (cnt == 0)
          {
            // no more levels for this item,
            // remove it.
            if (TEST_UPDATEPLAN)
            {
              LogUtil.finer(LoggerType.TRACE, "remove logitem " + item.toString());
            }
            m_logItems.remove(item);
          }
          break;
        }
      }
      if (!found)
      {
        LogItem newItem;
        newItem = new LogItem(this, reapply, area, type, id, event, levels);
if (TEST_UPDATEPLAN)
{
  LogUtil.finer(LoggerType.TRACE, "add logitem " + newItem.toString());
}
        m_logItems.add(newItem);
      }
    }
  }
  
  private void updateLogItems(
      ILogArea area, List<Integer> types, List<Integer> ids, List<ILogEvent> events,
      List<Integer> levels)
  {
    if (area == LogArea.QUERY)
    {
      if (ids == null)
      {
        addLogItem(LogItem.REAPPLY, LogArea.OPERATOR, -1, -1, events, levels);
        return;
      }
    }
    else if (area == LogArea.OPERATOR ||
             area == LogArea.QUEUE ||
             area == LogArea.SYNOPSIS ||
               area == LogArea.STORE) 
    {
      if (types != null)
      {
        for (Integer type : types)
        {
          addLogItem(LogItem.REAPPLY, area, type, -1, events, levels);
        }
      } 
    } 
    
    if (types == null && ids == null)
    {
      addLogItem(LogItem.REAPPLY, area, -1, -1, events, levels);
    }
  }

  public synchronized void dumpLog(ILogArea area,
      List<Integer> types, List<Integer> ids, List<ILogEvent> events,
      List<Integer> levels) throws CEPException
  {
    LogHandler h = new LogHandler(this, LogHandler.DUMP, area, types, ids, events, levels, false);
    processLogRequest(h);
  }

  public synchronized void setLevel(boolean enable, ILogArea area,
      List<Integer> types, List<Integer> ids, List<ILogEvent> events,
      List<Integer> levels,
      boolean updateLogItem) throws CEPException
  {
    LogHandler h = new LogHandler(this, (enable ? LogHandler.ENABLE : LogHandler.DISABLE), 
                                  area, types, ids, events, levels, updateLogItem);
    processLogRequest(h);
  }
  
  private synchronized void processLogRequest(LogHandler h) throws CEPException
  {
if (TEST_UPDATEPLAN)
{
    LogUtil.fine(LoggerType.TRACE, h.toString());
}    

    // need to listen to plan change
    if (m_subscription < 0) 
    {
      ExecContext ec = m_logLevelManager.getExecContext();
      QueryManager qm = ec.getQueryMgr();
      qm.addQueryChgListener(this);

      //TODO add subscription to new ExecContext
      m_subscription = 1;
    }

    if (h.m_updateLogItem)
      updateLogItems(h.m_area, h.m_types, h.m_ids, h.m_events, h.m_levels);

    // Build a list of interested objects .
    List<LogTarget> targets = null;
    if (h.m_ids == null) 
    {
      // find the list of targets that matches with specified types
      targets = getLogTargets(h.m_area, h.m_types, true);
    } else {
      // find the list of targets that matches with specified ids
      targets = getLogTargets(h.m_area, h.m_ids, false);
    }

    if ((targets != null) &&
        (h.m_area == LogArea.OPERATOR || h.m_area == LogArea.QUERY)) 
    {
      EventMap optEvents = new EventMap();
      LevelMap optLevels = new LevelMap();
      List<Integer> opLevels = new ArrayList<Integer>(h.m_levels.size());
      List<ILogEvent> opEvents = (h.m_events == null ? null :
                    new ArrayList<ILogEvent>(h.m_events.size()));

      // Separate operator area specific events, levels
      // Events, levels that need coversion to underlying data structure
      // are stored in m_curOptEvents and m_curOptLevels
      applyOperatorShortcuts(h.m_events, h.m_levels, opEvents, opLevels, 
          optEvents, optLevels);
      
      OptVisitor v = new OptVisitor(h, optEvents, optLevels);
      for (LogTarget target : targets) 
      {
        // initiate navigating the plan and apply events, levels in underlying
        // data structure of the operator using curOptEvents and curAreaLevels
        // the events, levels are set in the callbacks of OptVisitor.
        PhyOpt opt = (PhyOpt) target.m_obj;
        opt.accept(v);
        
        // set the operator specific events, levels
        h.handle(LogArea.OPERATOR, target, opEvents, opLevels);
      }
    } 
    else 
    {
      if (targets == null)
      {
        // Even thought there is no target, we need to invoke 'handle'
        // to add the logItem 
        h.handle(h.m_area, null, null, null);
        return;
      }
      for (LogTarget target : targets) 
      {
        h.handle(h.m_area, target, null, null);
      }
    }
    LogUtil.fine(LoggerType.TRACE, m_logLevelManager.toString());
  }

  /** for unit testing only */
  public ILogEvent[] getOperatorEvents(ILogEvent event, ILogArea a)
  {
    int opdsIdx = event.getOpDSIndex();
    if (opdsIdx >= 0) 
    {
      EventMap evmap = null;
      try 
      {
        evmap = operatorEvents[opdsIdx];
      } catch (ArrayIndexOutOfBoundsException ex) 
      {
        return null;
      }
      List<ILogEvent> res = evmap.get(a);
      if (res == null) return null;
      ILogEvent[] events = new ILogEvent[res.size()];
      ArrayUtil.fromCollection(res, events);
      return events;
    }
    return null;
  }
  
  /** for unit testing only */
  public int[] getOperatorLevels(ILogEvent event, int level, ILogArea a)
  {
    if (level >= LogLevel.OPERATOR_STRUCTURES_STATS
        && level <= LogLevel.OPERATOR_STRUCTURES_MOST) 
    {
      LevelMap lmap = null;
      try 
      {
        lmap = operatorLevels[level - LogLevel.OPERATOR_STRUCTURES_STATS];
      } catch (ArrayIndexOutOfBoundsException ex) 
      {
        return null;
      }
      EventMap evmap = null;
      int opdsIdx = event.getOpDSIndex();
      if (opdsIdx >= 0) 
      {
        try 
        {
          evmap = operatorEvents[opdsIdx];
        } catch (ArrayIndexOutOfBoundsException ex) 
        {
        }
      } 
      List<Integer> levels;
      if (evmap == null) 
      {
        levels = lmap.get(a);
      } 
      else 
      {
        levels = lmap.get(a, evmap);
      }
      return levels == null ? null : ArrayUtil.fromCollection(levels);
    }
    return null;
  }

  /** for unit testing only */
  public void addChgNotifier(ILogLevelChgNotifier notifier)
  {
    m_chgNotifier = notifier;
  }
  

  static class PlanChange
  {
    boolean     m_add;
    int         m_id;
    int         m_type;
    ILogArea     m_objType;
    
    PlanChange(boolean add, int type, int id, 
        ILogArea objtype)
    {
      m_add = add;
      m_type = type;
      m_id = id;
      m_objType = objtype;
    }
    boolean isAdd() {return m_add;}
    ILogArea getObjType() { return m_objType;}
    int getType() {return m_type;}
    int getId() {return m_id;}
    public String toString()
    {
      return (m_add ? "add " : "remove ") +
            m_objType.toString() + " id=" + m_id + " type="+m_type;
    }
  }

  /**
   * PlanState keeps the operators and underlying
   * data structure of the operators for a query.
   * It is used to determine changes from adding/dropping
   * a query.
   */
  class PlanState
  {
    long              m_threadId;
    int               m_queryId;
    List<PhyOpt>      m_operators;
    List<PhySynopsis> m_synopsis;
    List<PhyStore>    m_stores;
    List<PhyQueue>    m_queues;
    List<PhyIndex>    m_indexes;
    
    PlanState(PlanManager pm, int qryId)
    {
      init();
      m_queryId = qryId;
      addQuery(pm, qryId);
    }

    PlanState(PlanManager pm)
    {
      init();
      m_queryId = -1;
      ArrayList<Integer> qids= pm.getRootQueryIds();
      for (Integer qryId : qids)
      {
        addQuery(pm, qryId);
      }
    }

    void init()
    {
      m_threadId = Thread.currentThread().getId();
      m_operators = new LinkedList<PhyOpt>();
      m_synopsis = new LinkedList<PhySynopsis>();
      m_stores = new LinkedList<PhyStore>();
      m_queues = new LinkedList<PhyQueue>();
      m_indexes = new LinkedList<PhyIndex>();
    }

    long getThreadId() {return m_threadId;}
    
    void addQuery(PlanManager pm, int qryId)
    {
      QueryVisitor v = new QueryVisitor(this);
      PhyOpt root = pm.getQueryRootOpt(qryId);
      if (root != null) 
      {
        root.accept(v);
      }
    }

    public String toString()
    {
      StringBuilder sb = new StringBuilder();
      sb.append("operators "+m_operators.size()+"\n");
      for (PhyOpt opt : m_operators) 
      {
        sb.append(opt.getId());
        sb.append(":");
        sb.append(opt.getOperatorKind());
        sb.append(" ");
      }
      sb.append("\n");
      sb.append("synopsis "+m_synopsis.size()+"\n");
      for (PhySynopsis syn : m_synopsis) 
      {
        sb.append(syn.getId());
        sb.append(":");
        sb.append(syn.getKind());
        sb.append(" ");
      }
      sb.append("\n");
      sb.append("stores "+m_queues.size()+"\n");
      for (PhyStore store : m_stores) 
      {
        sb.append(store.getId());
        sb.append(":");
        sb.append(store.getStoreKind());
        sb.append(" ");
      }
      sb.append("\n");
      sb.append("queues "+m_queues.size()+"\n");
      for (PhyQueue queue : m_queues) 
      {
        sb.append(queue.getId());
        sb.append(" ");
      }
      sb.append("\n");
      sb.append("indexes "+m_indexes.size()+"\n");
      for (PhyIndex index : m_indexes) 
      {
        sb.append(index.getId());
        sb.append(" ");
      }
      sb.append("\n");
      return sb.toString();
    }
    
    void addOperator(PhyOpt opt)
    {
      if (!m_operators.contains(opt))
        m_operators.add(opt);
    }

    void addSynopsis(PhySynopsis syn)
    {
      if (!m_synopsis.contains(syn))
        m_synopsis.add(syn);
    }

    void addStore(PhyStore store)
    {
      if (!m_stores.contains(store))
        m_stores.add(store);
    }

    void addQueue(PhyQueue q)
    {
      if (!m_queues.contains(q))
        m_queues.add(q);
    }

    void addIndex(PhyIndex i)
    {
      if (!m_indexes.contains(i))
        m_indexes.add(i);
    }
    
    <T> void buildChangeSet(List<T> oldset, List<T> newset, List<T> dest)
    {
      for (T obj : oldset) 
      {
        if (!newset.contains(obj)) 
        {
          dest.add(obj);
        }
      }
    }
    
    void apply(boolean add, PlanState old, List<LogItem> items)
      throws CEPException
    {
      List<PlanChange> changes = new LinkedList<PlanChange>();
      List<PhyOpt> operators = m_operators;
      List<PhySynopsis> synopsis = m_synopsis;
      List<PhyStore>    stores = m_stores;
      List<PhyQueue>    queues = m_queues;
      List<PhyIndex>    indexes = m_indexes;
      if (old != null)
      {
        assert (add == false);  
        // build the change set
        // find objects that are removed
        // (e.g) exists in the old set, 
        //       but not in the new set.
        operators = new LinkedList<PhyOpt>();
        synopsis = new LinkedList<PhySynopsis>();
        stores = new LinkedList<PhyStore>();
        queues = new LinkedList<PhyQueue>();
        indexes = new LinkedList<PhyIndex>();
        buildChangeSet(old.m_operators, m_operators, operators);
        buildChangeSet(old.m_synopsis, m_synopsis, synopsis);
        buildChangeSet(old.m_stores, m_stores, stores);
        buildChangeSet(old.m_queues, m_queues, queues);
        buildChangeSet(old.m_indexes, m_indexes, indexes);
      } 
      int id, type;
      ILogArea objType;
      objType = LogArea.OPERATOR;
      for (PhyOpt opt : operators) 
      {
        type = opt.getOperatorKind().ordinal();
        id =opt.getId();
        changes.add(new PlanChange(add, type, id, objType));
      }
      objType = LogArea.SYNOPSIS;
      for (PhySynopsis syn : synopsis )
      {
        type = syn.getKind().ordinal();
        id = syn.getId();
        changes.add(new PlanChange(add, type, id, objType));
      }
      objType = LogArea.STORE;
      for (PhyStore store : stores )
      {
        type = store.getStoreKind().ordinal();
        id = store.getId();
        changes.add(new PlanChange(add, type, id, objType));
      }
      objType = LogArea.QUEUE;
      for (PhyQueue queue : queues )
      {
        type = -2;      // in order not to match with LogItem of type=-1 
        id = queue.getId();
        changes.add(new PlanChange(add, type, id, objType));
      }
      objType = LogArea.INDEX;
      for (PhyIndex idx : indexes )
      {
        type = -2;
        id = idx.getId();
        changes.add(new PlanChange(add, type, id, objType));
      }
      for (PlanChange chg : changes)
      {
        LogUtil.finer(LoggerType.TRACE, chg.toString());
      }
      LinkedList<LogItem> itemsToRemove = new LinkedList<LogItem>();
      for (LogItem item : items)
      {
        boolean match = item.apply(add, changes);
        if (!add && match)
        {
          itemsToRemove.add(item);
        }
      }
      for (LogItem ritem : itemsToRemove)
      {
        items.remove(ritem);
      }
    }
  }
  
  private static class AreaObjs<E>
  {
    ILogArea       m_area;
    List<E>       m_objs;

    public AreaObjs()
    {
      m_area = null;
      m_objs = new LinkedList<E>();
    }
    
    public AreaObjs(ILogArea area, E[] objs)
    {
      m_area = area;
      m_objs = new ArrayList<E>(objs.length);
      ArrayUtil.toCollection(objs, m_objs);
    }
    
    public void add(AreaObjs<E> other)
    {
      for (E obj : other.m_objs)
        m_objs.add(obj);
    }
    
    public String toString()
    {
      if (m_area == null) return "empty";
      StringBuilder sb = new StringBuilder();
      sb.append(m_area.toString());
      sb.append(" : ");
      if (m_objs != null) 
      {
        for (E e : m_objs) 
        {
          sb.append(e.toString());
          sb.append(" ");
        }
      }
      return sb.toString();
    }
  }
  private static class AreaMap<E>
  {
    List<AreaObjs<E>>  m_items;

    public AreaMap()
    {
      m_items = new LinkedList<AreaObjs<E>>();
    }

    public AreaMap(AreaObjs<E>[] areas)
    {
      m_items = new LinkedList<AreaObjs<E>>();
      for (AreaObjs<E> a : areas)
        m_items.add(a);
    }

    private void add(AreaObjs<E> item)
    {
      for (AreaObjs<E> i : m_items) 
      {
        if (i.m_area == item.m_area) 
        {
          i.add(item);
          return;
        }
      }
      m_items.add(item);
    }
    
    public void add(AreaMap<E> o)
    {
      for (AreaObjs<E> i : o.m_items) 
      {
        add(i);
      }
    }

    public void add(AreaMap<E> o, AreaMap<ILogEvent> evmap)
    {
      for (AreaObjs<E> l : o.m_items) 
      {
        if (evmap.m_items.size() == 0 || evmap.hasArea(l.m_area))
          add(l);
      }
    }
    
    public boolean hasArea(ILogArea a)
    {
      for (AreaObjs<E> item : m_items)
        if (item.m_area == a) return true;
      return false;
    }

    public List<E> get(ILogArea a)
    {
      for (AreaObjs<E> item : m_items)
        if (item.m_area == a) return item.m_objs;
      return null;
    }

    public List<E> get(ILogArea a, AreaMap<ILogEvent> evmap)
    {
      for (AreaObjs<E> item : m_items)
        if (item.m_area == a && evmap.hasArea(item.m_area)) 
          return item.m_objs;
      return null;
    }
    
    public String toString()
    {
      StringBuilder sb = new StringBuilder();
      for (AreaObjs<E> item : m_items) 
      {
        sb.append(item.toString());
        sb.append("\n");
      }
      return sb.toString();
    }
  }

  static class AreaEvents extends AreaObjs<ILogEvent> 
  {
    public AreaEvents(ILogArea area, ILogEvent[] events) 
    {
      super(area, events);
    }
  }
  
  static class AreaLevels extends AreaObjs<Integer> 
  {
    public AreaLevels(ILogArea area, Integer[] levels) 
    {
      super(area, levels);
    }
  }
  static class EventMap extends AreaMap<ILogEvent> 
  {
    public EventMap() {super();}
    public EventMap(AreaEvents[] areas) {super(areas);}
  }
  static class LevelMap extends AreaMap<Integer> 
  {
    public LevelMap() {super();}
    public LevelMap(AreaLevels[] areas) {super(areas);}
  }
  
  // The following table maps operator event to events of underlying data
  // structures.
  // e.g OPERATOR_QUEUE_ENQDEQ -> QUEUE_ENQUEUE and QUEUE_DEQUEUE
  private static final EventMap[] operatorEvents = {
// OPERATOR_ALL_DS
    new EventMap(new AreaEvents[] { 
        new AreaEvents(LogArea.QUEUE, new ILogEvent[] {LogEvent.QUEUE_DDL, LogEvent.QUEUE_ENQUEUE, LogEvent.QUEUE_DEQUEUE, LogEvent.QUEUE_PEEK, LogEvent.QUEUE_GET}), 
        new AreaEvents(LogArea.SYNOPSIS, new ILogEvent[] {LogEvent.SYNOPSIS_DDL, LogEvent.SYNOPSIS_INSERT, LogEvent.SYNOPSIS_DELETE, LogEvent.SYNOPSIS_GET, LogEvent.SYNOPSIS_SCAN_START, LogEvent.SYNOPSIS_SCAN, LogEvent.SYNOPSIS_SCAN_STOP}),
        new AreaEvents(LogArea.STORE, new ILogEvent[] {LogEvent.STORE_DDL, LogEvent.STORE_INSERT, LogEvent.STORE_DELETE, LogEvent.STORE_GET, LogEvent.STORE_SCAN_START, LogEvent.STORE_SCAN, LogEvent.STORE_SCAN_STOP}), 
        new AreaEvents(LogArea.INDEX, new ILogEvent[] {LogEvent.INDEX_DDL, LogEvent.INDEX_INSERT, LogEvent.INDEX_DELETE, LogEvent.INDEX_SCAN, LogEvent.INDEX_SCAN_START, LogEvent.INDEX_SCAN_STOP }),
    }), 
// OPERATOR_QUEUE_ENQDEQ:
    new EventMap(new AreaEvents[] {
        new AreaEvents(LogArea.QUEUE, new ILogEvent[] { LogEvent.QUEUE_ENQUEUE, LogEvent.QUEUE_DEQUEUE }),
    }), 
// OPERATOR_QUEUE_PEEK:
    new EventMap(new AreaEvents[] {
        new AreaEvents(LogArea.QUEUE, new ILogEvent[] { LogEvent.QUEUE_PEEK }),
    }), 
// OPERATOR_SYNOPSIS_INSDEL:
    new EventMap(new AreaEvents[] {
        new AreaEvents(LogArea.SYNOPSIS, new ILogEvent[] { LogEvent.SYNOPSIS_INSERT, LogEvent.SYNOPSIS_DELETE }),
    }), 
// OPERATOR_SYNOPSIS_SCAN:
    new EventMap(new AreaEvents[] {
        new AreaEvents(LogArea.SYNOPSIS, new ILogEvent[] { LogEvent.SYNOPSIS_SCAN }),
    }), 
// OPERATOR_INDEX_SCAN:
    new EventMap(new AreaEvents[] {
        new AreaEvents(LogArea.INDEX, new ILogEvent[] { LogEvent.INDEX_SCAN })                  
    }), 
  };
  private static final EventMap dumpDSEVent = new EventMap(new AreaEvents[] { 
      new AreaEvents(LogArea.QUEUE, new ILogEvent[] {DumpEvent.DUMP}), 
      new AreaEvents(LogArea.SYNOPSIS, new ILogEvent[] {DumpEvent.DUMP}),
      new AreaEvents(LogArea.STORE, new ILogEvent[] {DumpEvent.DUMP}), 
      new AreaEvents(LogArea.INDEX, new ILogEvent[] {DumpEvent.DUMP}),
  });

  // The following table maps operator level to levelss of underlying data
  // structures.
  // e.g OPERATOR_STRUCTURES_STATS -> QUEUE_ENQUEUE and QUEUE_DEQUEUE
  private static final LevelMap[] operatorLevels = {
// OPERATOR_STRUCTURES_STATS
    new LevelMap(new AreaLevels[]
      { new AreaLevels(LogArea.QUEUE, new Integer[] { LogLevel.QUEUE_STATS }), 
        new AreaLevels(LogArea.STORE, new Integer[] { LogLevel.STORE_STATS }), 
        new AreaLevels(LogArea.SYNOPSIS, new Integer[] { LogLevel.SYNOPSIS_STATS }),
        new AreaLevels(LogArea.INDEX, new Integer[] { LogLevel.INDEX_STATS }) 
      }),
// OPERATOR_STRUCTURES_LEAST
    new LevelMap(new AreaLevels[]
      { new AreaLevels(LogArea.QUEUE, new Integer[] { LogLevel.QUEUE_ELEMENT_UNPINNED }),
        new AreaLevels(LogArea.STORE, new Integer[] { LogLevel.STORE_TUPLE_UNPINNED }),
        new AreaLevels(LogArea.SYNOPSIS, new Integer[] { LogLevel.SYNOPSIS_TUPLE_UNPINNED }),
        new AreaLevels(LogArea.INDEX, new Integer[] { LogLevel.INDEX_TUPLE_UNPINNED }) 
     }),
// OPERATOR_STRUCTURES_MORE
    new LevelMap(new AreaLevels[]
      { new AreaLevels(LogArea.QUEUE, new Integer[] { LogLevel.QUEUE_ELEMENT_UNPINNED, LogLevel.QUEUE_STATS }),
        new AreaLevels(LogArea.STORE, new Integer[] { LogLevel.STORE_TUPLE_UNPINNED, LogLevel.STORE_STATS }),
        new AreaLevels(LogArea.SYNOPSIS, new Integer[] { LogLevel.SYNOPSIS_TUPLE_UNPINNED, LogLevel.SYNOPSIS_STATS }), 
        new AreaLevels(LogArea.INDEX, new Integer[] { LogLevel.INDEX_TUPLE_UNPINNED, LogLevel.INDEX_STATS }) 
      }),
// OPERATOR_STRUCTURES_MOST
      new LevelMap(new AreaLevels[]
      { new AreaLevels(LogArea.QUEUE, new Integer[] { LogLevel.QUEUE_ELEMENT_UNPINNED, LogLevel.QUEUE_STATS, LogLevel.QUEUE_DUMPELEMS }),
        new AreaLevels(LogArea.STORE, new Integer[] { LogLevel.STORE_TUPLE_UNPINNED, LogLevel.STORE_STATS, LogLevel.STORE_DUMPELEMS }),
        new AreaLevels(LogArea.SYNOPSIS, new Integer[] { LogLevel.SYNOPSIS_TUPLE_UNPINNED, LogLevel.SYNOPSIS_STATS, LogLevel.SYNOPSIS_DUMPELEMS }), 
        new AreaLevels(LogArea.INDEX, new Integer[] { LogLevel.INDEX_TUPLE_UNPINNED, LogLevel.INDEX_STATS, LogLevel.INDEX_DUMPELEMS }) 
      })         
  };

  /**
   * A visitor for navigating underlying data structure from an operator.
   * It does not navigate through sub-operators.
   *
   */
  class OptVisitor implements IPlanVisitor
  {
    LogHandler      m_handler;
    boolean         m_updateLogItem;
    EventMap        m_optEvents;
    LevelMap        m_optLevels;
    
    OptVisitor(LogHandler handler, EventMap evs, LevelMap levels)
    {
      m_handler = handler;
      m_optEvents = evs;
      m_optLevels = levels;
    }
    
    public boolean canVisit(ObjType which)
    {
      return (which != ObjType.INPUT_OPERATOR);
    }
    
    public void visit(PhyOpt opt)
    {
    }

    public void visit(PhySynopsis syn)
    {
      LogTarget target = new LogTarget(
          syn.getId(),
          syn.getSyn(),
          syn);
      visit(LogArea.SYNOPSIS, target);
    }

    public void visit(PhyStore store)
    {
      LogTarget target = new LogTarget(
            store.getId(),
            store.getInstStore(),
            store);
      visit(LogArea.STORE, target);
    }

    public void visit(PhyQueue queue)
    {
      LogTarget target = new LogTarget(
          queue.getId(),
          queue.getInstQueue(),
          queue);
      visit(LogArea.QUEUE, target);
    }

    public void visit(PhyIndex index)
    {
      LogTarget target = new LogTarget(
          index.getId(),
          index.getInst(),
          index);
      visit(LogArea.INDEX, target);
    }
    
    public void visit(ILogArea area,  LogTarget target)
    {
      List<ILogEvent>events = null;
      if (m_optEvents != null) 
      {
        if (m_optEvents.hasArea(area))
          events = m_optEvents.get(area);
      }
      List<Integer> levels = m_optLevels.get(area);
      if (levels != null) 
      {
        m_handler.handle(area, target, events, levels);
      }
    }
  }

  /**
   * A visitor for navigating operators from a query.
   */
  public static class PlanVisitor implements IPlanVisitor
  {
    List<PhyOpt> m_operators;
    
    PlanVisitor(List<PhyOpt> oprs)
    {
      m_operators = oprs;
    }
    
    public boolean canVisit(ObjType which)
    {
      return (which == ObjType.INPUT_OPERATOR);
    }
    
    public void visit(PhyOpt opt)
    {
      if (!m_operators.contains(opt))
        m_operators.add(opt);
    }

    public void visit(PhySynopsis syn)
    {
    }

    public void visit(PhyStore store)
    {
    }

    public void visit(PhyQueue queue)
    {
    }

    public void visit(PhyIndex queue)
    {
    }
  }

  /**
   * A visitor for navigating operators and underlying data structure for the query.
   *
   */
  class QueryVisitor implements IPlanVisitor
  {
    PlanState m_queryState;
    
    QueryVisitor(PlanState s)
    {
      m_queryState = s;
    }
    
    public boolean canVisit(ObjType which)
    {
      return true;
    }
    
    public void visit(PhyOpt opt)
    {
      m_queryState.addOperator(opt);
    }

    public void visit(PhySynopsis syn)
    {
      m_queryState.addSynopsis(syn);
    }

    public void visit(PhyStore store)
    {
      m_queryState.addStore(store);
    }

    public void visit(PhyQueue queue)
    {
      m_queryState.addQueue(queue);
    }

    public void visit(PhyIndex index)
    {
      m_queryState.addIndex(index);
    }
  }  
  
  static class LogTarget
  {
    ILoggable m_target;
    int       m_id;
    Object    m_obj;
    
    LogTarget(int id, ILoggable t, Object obj)
    {
      m_id = id;
      m_target = t;
      m_obj = obj;
    }
  }
  
}
