/* $Header: LogItem.java 19-jun-2008.18:17:26 hopark Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      06/18/08 - logging refactor
    hopark      06/18/08 - logging refactor
    hopark      02/06/08 - Creation
 */

/**
 *  @version $Header: LogItem.java 19-jun-2008.18:17:26 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logging.trace;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import oracle.cep.exceptions.CEPException;
import oracle.cep.logging.ILogArea;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.util.CSVUtil;

/**
 * LogItem represents the level change action.
 * It can either be reapplied on adding queries
 * or be removed on dropping queries.
 *
 */
class LogItem
{
  static final boolean REAPPLY = true;
  static final boolean REMOVE = false;
  
  boolean             m_reapply;
  ILogArea             m_area;
  int                 m_type;
  int                 m_id;
  ILogEvent            m_event;
  List<Integer>       m_levels;

  List<Integer>       m_types;
  List<Integer>       m_ids;
  List<ILogEvent>      m_events;
  PlanMonitor         m_pm;
  
  LogItem(PlanMonitor pm,
          boolean reapply,
          ILogArea a, int type, int id, 
          ILogEvent event, List<Integer> levels)
  {   
    m_pm = pm;
    m_reapply = reapply;
    m_area = a;
    m_type = type;
    m_id = id;
    m_event = event;
    m_types = null;
    if (m_type >= 0) 
    {
      m_types = new ArrayList<Integer>(1);
      m_types.add(m_type);
    }
    m_ids = null;
    if (m_id >= 0) 
    {
      m_ids = new ArrayList<Integer>(1);
      m_ids.add(m_id);
    }
    m_events = new ArrayList<ILogEvent>(1);
    m_events.add(m_event);
    m_levels = new LinkedList<Integer>();
    if (levels != null)
      m_levels.addAll(levels);

  }
  
  int updateLevels(boolean enable, List<Integer> levels)
  {
    if (enable)
    {
      for (Integer level : levels)
      {
        if (!m_levels.contains(level))
          m_levels.add(level);
      }
    }
    else
    {
      for (Integer level : levels)
      {
        m_levels.remove(level);
      }
    }
    return m_levels.size();
  }
  
  void apply()
  {
    
  }
  boolean apply(boolean add, List<PlanMonitor.PlanChange> changes)
    throws CEPException
  {
    if (add != m_reapply) return false;
    boolean match = false;
    for (PlanMonitor.PlanChange c : changes)
    {
      if (c.getObjType() == m_area &&
          ((m_id == -1 && m_type == -1) ||
          c.getId() == m_id || 
          c.getType() == m_type) )
      {
        List<Integer> ids = m_ids;
        if (m_id == -1 && m_reapply)
        {
          ids = new ArrayList<Integer>(1);
          ids.add(c.getId());
        }
        LogUtil.finer(LoggerType.TRACE, "apply " + c.isAdd() + ","+ m_area + "," + c.getId() + ","+ m_type);
        m_pm.setLevel(c.isAdd(), m_area,
            m_types, ids, m_events,
            m_levels, false);
        match = true;
      }
    }
    return match;
  }
  
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append(m_reapply ? "reapply ":"remove ");
    sb.append(m_area);
    sb.append(", type=");
    sb.append(m_type);
    sb.append(", id=");
    sb.append(m_id);
    sb.append(", event=");
    sb.append(m_event);
    sb.append(", levels=");
    sb.append(CSVUtil.fromList(m_levels));
    return sb.toString();
  }
}

