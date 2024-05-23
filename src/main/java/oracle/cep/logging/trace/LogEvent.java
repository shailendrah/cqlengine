/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logging/trace/LogEvent.java /main/6 2010/01/06 20:33:11 parujain Exp $ */

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
    parujain    12/07/09 - synonym
    hopark      01/26/09 - add getName
    parujain    11/26/08 - add view event
    hopark      12/05/08 - add getEvents
    hopark      11/22/08 - add toString
    hopark      06/18/08 - refactor
    hopark      01/15/08 - metadata logging for cache objects
    hopark      01/08/08 - Add Events
    hopark      09/27/07 - add SPILL_EVICTFAC
    hopark      08/01/07 - add dump
    hopark      06/11/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logging/trace/LogEvent.java /main/6 2010/01/06 20:33:11 parujain Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logging.trace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.cep.logging.ILogArea;
import oracle.cep.logging.ILogEvent;
import oracle.cep.util.ArrayUtil;

public class LogEvent implements ILogEvent
{
  public static LogEvent QUEUE_DDL;
  public static LogEvent QUEUE_ENQUEUE;
  public static LogEvent QUEUE_DEQUEUE;
  public static LogEvent QUEUE_PEEK;
  public static LogEvent QUEUE_GET;

  public static LogEvent STORE_DDL;
  public static LogEvent STORE_INSERT;
  public static LogEvent STORE_DELETE;
  public static LogEvent STORE_GET;
  public static LogEvent STORE_SCAN_START;
  public static LogEvent STORE_SCAN;
  public static LogEvent STORE_SCAN_STOP;
  
  public static LogEvent INDEX_DDL;
  public static LogEvent INDEX_INSERT;
  public static LogEvent INDEX_DELETE;
  public static LogEvent INDEX_SCAN_START;
  public static LogEvent INDEX_SCAN;
  public static LogEvent INDEX_SCAN_STOP;
  
  public static LogEvent SYNOPSIS_DDL;
  public static LogEvent SYNOPSIS_INSERT;
  public static LogEvent SYNOPSIS_DELETE;
  public static LogEvent SYNOPSIS_GET;
  public static LogEvent SYNOPSIS_SCAN_START;
  public static LogEvent SYNOPSIS_SCAN;
  public static LogEvent SYNOPSIS_SCAN_STOP;
  
  public static LogEvent OPERATOR_DDL;
  public static LogEvent OPERATOR_RUN_BEGIN;
  public static LogEvent OPERATOR_RUN_END;
  public static LogEvent OPERATOR_ALL_DS;
  public static LogEvent OPERATOR_QUEUE_ENQDEQ;
  public static LogEvent OPERATOR_QUEUE_PEEK;
  public static LogEvent OPERATOR_SYNOPSIS_INSDEL;
  public static LogEvent OPERATOR_SYNOPSIS_SCAN;
  public static LogEvent OPERATOR_INDEX_SCAN;
  
  public static LogEvent SPILL_GC;
  public static LogEvent SPILL_EVICT_BEGIN;
  public static LogEvent SPILL_EVICT_END;
  public static LogEvent SPILL_EVICTFAC_BEGIN;
  public static LogEvent SPILL_EVICTFAC_END;
  public static LogEvent SPILL_EVICT_BGCHK;
    
  public static LogEvent DB_OPEN;
  public static LogEvent DB_CLOSE;
  public static LogEvent DB_READ;
  public static LogEvent DB_WRITE;
  public static LogEvent DB_DELETE;
  public static LogEvent DB_TXN_BEGIN;
  public static LogEvent DB_TXN_END;
  public static LogEvent DB_QUERY_BEGIN;
  public static LogEvent DB_QUERY;
  
  public static LogEvent MQUERY_CREATE;
  public static LogEvent MQUERY_MODIFY;
  public static LogEvent MQUERY_DELETE;
  public static LogEvent MQUERY_START;
  public static LogEvent MQUERY_STOP;
  public static LogEvent MQUERY_RESTART;

  public static LogEvent MTABLE_CREATE;
  public static LogEvent MTABLE_MODIFY;
  public static LogEvent MTABLE_DELETE;

  public static LogEvent MWINDOW_CREATE;
  public static LogEvent MWINDOW_DELETE;

  public static LogEvent MUSERFUNC_CREATE;
  public static LogEvent MUSERFUNC_DELETE;
  
  public static LogEvent MSYNONYM_CREATE;
  public static LogEvent MSYNONYM_DELETE;

  public static LogEvent MVIEW_CREATE;
  public static LogEvent MVIEW_DELETE;
  public static LogEvent MVIEW_FORCESTOP;
  public static LogEvent MVIEW_START;
  public static LogEvent MVIEW_STOP;

  public static LogEvent MSYSTEM_CREATE;
  public static LogEvent MSYSTEM_UPDATE;
  public static LogEvent MSYSTEM_DELETE;
  
  public static LogEvent MCACHE_CREATE;
  public static LogEvent MCACHE_DELETE;
  public static LogEvent MCACHE_RELEASE_LOCK;
  public static LogEvent MCACHE_ACQUIRE_READ_LOCK;
  public static LogEvent MCACHE_ACQUIRE_WRITE_LOCK;

  String  m_name;
  ILogArea m_area;
  int     m_value;
  int     m_opdsIdx; //events for underlying datastructure from operators
  private static Map<Integer, ILogEvent> s_logEventMap;
  static {
    s_logEventMap = new HashMap<Integer, ILogEvent>();
    QUEUE_DDL = addLogEvent("QUEUE_DDL", LogArea.QUEUE, 21);
    QUEUE_ENQUEUE = addLogEvent("QUEUE_ENQUEUE", LogArea.QUEUE, 22);
    QUEUE_DEQUEUE = addLogEvent("QUEUE_DEQUEUE", LogArea.QUEUE, 23);
    QUEUE_PEEK = addLogEvent("QUEUE_PEEK", LogArea.QUEUE, 24);
    QUEUE_GET = addLogEvent("QUEUE_GET", LogArea.QUEUE, 25);

    STORE_DDL = addLogEvent("STORE_DDL", LogArea.STORE, 41);
    STORE_INSERT = addLogEvent("STORE_INSERT", LogArea.STORE, 42);
    STORE_DELETE = addLogEvent("STORE_DELETE", LogArea.STORE, 43);
    STORE_GET = addLogEvent("STORE_GET", LogArea.STORE, 44);
    STORE_SCAN_START = addLogEvent("STORE_SCAN_START", LogArea.STORE, 45);
    STORE_SCAN = addLogEvent("STORE_SCAN", LogArea.STORE, 46);
    STORE_SCAN_STOP = addLogEvent("STORE_SCAN_STOP", LogArea.STORE, 47);
    
    INDEX_DDL = addLogEvent("INDEX_DDL", LogArea.INDEX, 61);
    INDEX_INSERT = addLogEvent("INDEX_INSERT", LogArea.INDEX, 62);
    INDEX_DELETE = addLogEvent("INDEX_DELETE", LogArea.INDEX, 63);
    INDEX_SCAN_START = addLogEvent("INDEX_SCAN_START", LogArea.INDEX, 64);
    INDEX_SCAN = addLogEvent("INDEX_SCAN", LogArea.INDEX, 65);
    INDEX_SCAN_STOP = addLogEvent("INDEX_SCAN_STOP", LogArea.INDEX, 66);
    
    SYNOPSIS_DDL = addLogEvent("SYNOPSIS_DDL", LogArea.SYNOPSIS, 81);
    SYNOPSIS_INSERT = addLogEvent("SYNOPSIS_INSERT", LogArea.SYNOPSIS, 82);
    SYNOPSIS_DELETE = addLogEvent("SYNOPSIS_DELETE", LogArea.SYNOPSIS, 83);
    SYNOPSIS_GET = addLogEvent("SYNOPSIS_GET", LogArea.SYNOPSIS, 84);
    SYNOPSIS_SCAN_START = addLogEvent("SYNOPSIS_SCAN_START", LogArea.SYNOPSIS, 85);
    SYNOPSIS_SCAN = addLogEvent("SYNOPSIS_SCAN", LogArea.SYNOPSIS, 86);
    SYNOPSIS_SCAN_STOP = addLogEvent("SYNOPSIS_SCAN_STOP", LogArea.SYNOPSIS, 87);
    
    OPERATOR_DDL = addLogEvent("OPERATOR_DDL", LogArea.OPERATOR, 101);
    OPERATOR_RUN_BEGIN = addLogEvent("OPERATOR_RUN_BEGIN", LogArea.OPERATOR, 102);
    OPERATOR_RUN_END = addLogEvent("OPERATOR_RUN_END", LogArea.OPERATOR, 103);
    OPERATOR_ALL_DS = addLogEvent("OPERATOR_ALL_DS", LogArea.OPERATOR, 104, 0);
    OPERATOR_QUEUE_ENQDEQ = addLogEvent("OPERATOR_QUEUE_ENQDEQ", LogArea.OPERATOR, 105, 1);
    OPERATOR_QUEUE_PEEK = addLogEvent("OPERATOR_QUEUE_PEEK", LogArea.OPERATOR, 106, 2);
    OPERATOR_SYNOPSIS_INSDEL = addLogEvent("OPERATOR_SYNOPSIS_INSDEL", LogArea.OPERATOR, 107, 3);
    OPERATOR_SYNOPSIS_SCAN = addLogEvent("OPERATOR_SYNOPSIS_SCAN", LogArea.OPERATOR, 108, 4);
    OPERATOR_INDEX_SCAN = addLogEvent("OPERATOR_INDEX_SCAN", LogArea.OPERATOR, 109, 5);
    
    SPILL_GC = addLogEvent("SPILL_GC", LogArea.SPILL, 121);
    SPILL_EVICT_BEGIN = addLogEvent("SPILL_EVICT_BEGIN", LogArea.SPILL, 122);
    SPILL_EVICT_END = addLogEvent("SPILL_EVICT_END", LogArea.SPILL, 123);
    SPILL_EVICTFAC_BEGIN = addLogEvent("SPILL_EVICTFAC_BEGIN", LogArea.SPILL, 124);
    SPILL_EVICTFAC_END = addLogEvent("SPILL_EVICTFAC_END", LogArea.SPILL, 125);
    SPILL_EVICT_BGCHK = addLogEvent("SPILL_EVICT_BGCHK", LogArea.SPILL, 126);
      
    DB_OPEN = addLogEvent("DB_OPEN", LogArea.STORAGE, 141);
    DB_CLOSE = addLogEvent("DB_CLOSE", LogArea.STORAGE, 142);
    DB_READ = addLogEvent("DB_READ", LogArea.STORAGE, 143);
    DB_WRITE = addLogEvent("DB_WRITE", LogArea.STORAGE, 144);
    DB_DELETE = addLogEvent("DB_DELETE", LogArea.STORAGE, 145);
    DB_TXN_BEGIN = addLogEvent("DB_TXN_BEGIN", LogArea.STORAGE, 146);
    DB_TXN_END = addLogEvent("DB_TXN_END", LogArea.STORAGE, 147);
    DB_QUERY_BEGIN = addLogEvent("DB_QUERY_BEGIN", LogArea.STORAGE, 148);
    DB_QUERY = addLogEvent("DB_QUERY", LogArea.STORAGE, 149);
    
    MQUERY_CREATE = addLogEvent("MQUERY_CREATE", LogArea.METADATA_QUERY, 161);
    MQUERY_MODIFY = addLogEvent("MQUERY_MODIFY", LogArea.METADATA_QUERY, 162);
    MQUERY_DELETE = addLogEvent("MQUERY_DELETE", LogArea.METADATA_QUERY, 163);
    MQUERY_START = addLogEvent("MQUERY_START", LogArea.METADATA_QUERY, 164);
    MQUERY_STOP = addLogEvent("MQUERY_STOP", LogArea.METADATA_QUERY, 165);
    MQUERY_RESTART = addLogEvent("MQUERY_RESTART", LogArea.METADATA_QUERY, 166);

    MTABLE_CREATE = addLogEvent("MTABLE_CREATE", LogArea.METADATA_TABLE, 181);
    MTABLE_MODIFY = addLogEvent("MTABLE_MODIFY", LogArea.METADATA_TABLE, 182);
    MTABLE_DELETE = addLogEvent("MTABLE_DELETE", LogArea.METADATA_TABLE, 183);

    MWINDOW_CREATE = addLogEvent("MWINDOW_CREATE", LogArea.METADATA_WINDOW, 201);
    MWINDOW_DELETE = addLogEvent("MWINDOW_DELETE", LogArea.METADATA_WINDOW, 202);

    MUSERFUNC_CREATE = addLogEvent("MUSERFUNC_CREATE", LogArea.METADATA_USERFUNC, 221);
    MUSERFUNC_DELETE = addLogEvent("MUSERFUNC_DELETE", LogArea.METADATA_USERFUNC, 222);

    MVIEW_CREATE = addLogEvent("MVIEW_CREATE", LogArea.METADATA_VIEW, 241);
    MVIEW_DELETE = addLogEvent("MVIEW_DELETE", LogArea.METADATA_VIEW, 242);
    MVIEW_FORCESTOP = addLogEvent("MVIEW_FORCESTOP", LogArea.METADATA_VIEW, 243);
    MVIEW_START = addLogEvent("MVIEW_START", LogArea.METADATA_VIEW, 244);
    MVIEW_STOP = addLogEvent("MVIEW_STOP", LogArea.METADATA_VIEW, 245);

    MSYSTEM_CREATE = addLogEvent("MSYSTEM_CREATE", LogArea.METADATA_SYSTEM, 261);
    MSYSTEM_UPDATE = addLogEvent("MSYSTEM_UPDATE", LogArea.METADATA_SYSTEM, 262);
    MSYSTEM_DELETE = addLogEvent("MSYSTEM_DELETE", LogArea.METADATA_SYSTEM, 263);
    
    MCACHE_CREATE = addLogEvent("MCACHE_CREATE", LogArea.METADATA_CACHE, 281);
    MCACHE_DELETE = addLogEvent("MCACHE_DELETE", LogArea.METADATA_CACHE, 282);
    MCACHE_RELEASE_LOCK = addLogEvent("MCACHE_RELEASE_LOCK", LogArea.METADATA_CACHE, 283);
    MCACHE_ACQUIRE_READ_LOCK = addLogEvent("MCACHE_ACQUIRE_READ_LOCK", LogArea.METADATA_CACHE, 284);
    MCACHE_ACQUIRE_WRITE_LOCK = addLogEvent("MCACHE_ACQUIRE_WRITE_LOCK", LogArea.METADATA_CACHE, 285);
    
    MSYNONYM_CREATE = addLogEvent("MSYNONYM_CREATE", LogArea.METADATA_SYNONYM, 286);
    MSYNONYM_DELETE = addLogEvent("MSYNONYM_DELETE", LogArea.METADATA_SYNONYM, 287);
  }

  public String toString()
  {
    return m_name;
  }
  
  private LogEvent(String name, ILogArea area, int v, int opdsIdx) 
  {
    m_name = name;
    m_area = area;
    m_value = v;
    m_opdsIdx = opdsIdx;
  }

  private static LogEvent addLogEvent(String name, ILogArea area, int v)
  {
    LogEvent a = new LogEvent(name, area, v, -1);
    s_logEventMap.put(a.m_value, a);
    return a;
  }  

  private static LogEvent addLogEvent(String name, ILogArea area, int v, int opdsIdx)
  {
    LogEvent a = new LogEvent(name, area, v, opdsIdx);
    s_logEventMap.put(a.m_value, a);
    return a;
  }  
  
  public ILogArea getLogArea() {return m_area;}
  public int getOpDSIndex() {return m_opdsIdx;}
  public int getValue() {return m_value;}
  public String getName() {return m_name;}
  public static ILogEvent fromValue(int v)
  {
    return s_logEventMap.get(v);
  }
  
  public static ILogEvent[] getEvents(ILogArea area)
  {
    Collection<ILogEvent> vals = s_logEventMap.values();
    List<ILogEvent> events = new ArrayList<ILogEvent>(vals.size());
    for (ILogEvent ev : vals)
    {
      if (ev.getLogArea() == area) 
      {
        events.add(ev);
      }
    }
    ILogEvent[] res = new ILogEvent[events.size()];
    ArrayUtil.fromCollection(events, res);
    return res;
  }

  public static String getEventsStr(ILogArea area)
  {
    StringBuilder b = new StringBuilder();
    Collection<ILogEvent> vals = s_logEventMap.values();
    int i = 0;
    for (ILogEvent ev : vals)
    {
      if (ev.getLogArea() == area) 
      {
        if (i > 0)
          b.append(",");
        i++;
        b.append(ev.getValue());
      }
    }
    return b.toString();
   }
}

