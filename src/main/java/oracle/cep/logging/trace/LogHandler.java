/* $Header: pcbpel/cep/server/src/oracle/cep/logging/trace/LogHandler.java /main/3 2009/05/01 16:16:48 hopark Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/09 - change dump api
    hopark      12/07/08 - fix dump
    hopark      12/02/08 - move LogLevelManager to ExecContext
    hopark      06/18/08 - logging refactor
    hopark      02/06/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logging/trace/LogHandler.java /main/3 2009/05/01 16:16:48 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logging.trace;

import java.util.List;

import oracle.cep.util.CSVUtil;
import oracle.cep.logging.ILogArea;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.Levels;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.impl.DumpEvent;

class LogHandler 
{
  static final int DUMP = 0;
  static final int ENABLE = 1;
  static final int DISABLE = 2;
  
  int m_cmd;
  ILogArea m_area;
  List<Integer> m_types;
  List<Integer> m_ids;
  List<ILogEvent> m_events;
  List<Integer> m_levels;
  boolean m_updateLogItem;
  Levels      m_traceLevels;
  PlanMonitor m_pm;
  
  LogHandler(PlanMonitor pm, int cmd, ILogArea area,
      List<Integer> types, List<Integer> ids, List<ILogEvent> events,
      List<Integer> levels, boolean updateLogItem)
  {
    m_pm = pm;
    m_cmd = cmd;
    m_area = area;
    m_types = types;
    m_ids = ids;
    m_events = events;
    m_levels = levels;
    m_updateLogItem = updateLogItem;
    m_traceLevels = Levels.fromList(m_levels);
  }
  
  public String toString()
  {
    return "processLogRequest " + m_cmd + 
    " "  + m_area + 
    " types="+(m_types==null ? "null":CSVUtil.fromList(m_types)) +
    " ids="+(m_ids==null ? "null":CSVUtil.fromList(m_ids)) +
    " events="+(m_events==null ? "null":CSVUtil.fromList(m_events))  +
    " levels="+(m_levels==null ? "null":CSVUtil.fromList(m_levels));
  }        

  public void handle(ILogArea area, PlanMonitor.LogTarget target, List<ILogEvent> events, List<Integer> levels)
  {
    if (m_cmd == ENABLE | m_cmd == DISABLE)
    {
      int targetid = (target == null ? -1 : target.m_id);
      m_pm.setEventsLevels((m_cmd == ENABLE), area, targetid, 
          (events == null ? m_events : events), 
          (levels == null ? m_levels : levels), m_updateLogItem);
    }
    else if (m_cmd == DUMP)
    {
      m_pm.m_logLevelManager.dumpLevels(area, DumpEvent.DUMP, target.m_target, (levels == null ? m_levels : levels));
    }
    else
    {
      Levels tlevels = null;
      if (events == null) 
        events = m_events;
      if (levels == null)
      {
        levels = m_levels;
        tlevels = m_traceLevels;
      } 
      else 
      {
        tlevels = Levels.fromList(levels);
      }
      ILoggable inst = target.m_target;
      if (events == null || inst == null)
        return;
      try 
      {
        for (ILogEvent ev: events)
          LogLevelManager.trace(area, ev, inst, tlevels, (Object[]) null);
      }
      catch(Throwable e)
      {
        //eats up any exception
        LogUtil.fine(LoggerType.TRACE, e.toString());
      }
    }
  }
}
