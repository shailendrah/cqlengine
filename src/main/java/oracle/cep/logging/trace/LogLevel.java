/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logging/trace/LogLevel.java /main/3 2010/01/06 20:33:11 parujain Exp $ */

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
    hopark      11/22/08 - add toString
    hopark      06/18/08 - logging refactor
    hopark      06/18/08 - refactor
    hopark      02/05/08 - remove full systemstate dump
    hopark      01/09/08 - add levels for cache
    hopark      12/27/07 - add non-verbose dumps for index, store, queue.
    hopark      12/13/07 - fix typo
    hopark      08/03/07 - add levels for metadata
    hopark      07/03/07 - fix bug
    hopark      06/25/07 - add desc
    hopark      06/11/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logging/trace/LogLevel.java /main/3 2010/01/06 20:33:11 parujain Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logging.trace;

import oracle.cep.logging.ILogArea;
import oracle.cep.logging.LevelDesc;

public class LogLevel
{
  public static final int STACKTRACE = 0;
  public static final int QUEUE_INFO = 1;
  public static final int QUEUE_ELEMENT_PINNED = 2;
  public static final int QUEUE_ELEMENT_UNPINNED = 3;
  public static final int QUEUE_STATS = 4;
  public static final int QUEUE_DUMP = 5;
  public static final int QUEUE_DUMPELEMS = 6;
  
  public static final int STORE_INFO = 1;
  public static final int STORE_TUPLE_PINNED = 2;
  public static final int STORE_TUPLE_UNPINNED = 3;
  public static final int STORE_STATS = 4;
  public static final int STORE_DUMP = 5;
  public static final int STORE_DUMPELEMS = 6;
  
  public static final int INDEX_INFO = 1;
  public static final int INDEX_TUPLE_PINNED = 2;
  public static final int INDEX_TUPLE_UNPINNED = 3;
  public static final int INDEX_STATS = 4;
  public static final int INDEX_DUMP = 5;
  public static final int INDEX_DUMPELEMS = 6;
  
  public static final int SYNOPSIS_INFO = 1;
  public static final int SYNOPSIS_TUPLE_PINNED = 2;
  public static final int SYNOPSIS_TUPLE_UNPINNED = 3;
  public static final int SYNOPSIS_STATS = 4;
  public static final int SYNOPSIS_DUMP = 5;
  public static final int SYNOPSIS_DUMPELEMS = 6;
  public static final int SYNOPSIS_INDEX = 7;
  public static final int SYNOPSIS_DUMPELEMS1 = 8;

  public static final int OPERATOR_INFO = 1;
  public static final int OPERATOR_STATS = 2;
  public static final int OPERATOR_STRUCTURES_STATS = 3;
  public static final int OPERATOR_STRUCTURES_LEAST = 4;
  public static final int OPERATOR_STRUCTURES_MORE = 5;
  public static final int OPERATOR_STRUCTURES_MOST = 6;
  public static final int OPERATOR_DUMP = 7;
  public static final int OPERATOR_DUMP_DETAIL = 8;
  
  public static final int SPILL_ARG = 1;
  public static final int SPILL_EVICTINFO = 2;
  public static final int SPILL_STAT = 3;
  public static final int SPILL_DUMP = 4;
  public static final int SPILL_DUMP_DETAIL = 5;
  
  public static final int STORAGE_ARG = 1;
  public static final int STORAGE_DBINFO = 2;
  public static final int STORAGE_DBSTAT = 3;
  
  public static final int MQUERY_ARG = 1;
  public static final int MQUERY_INFO = 2;
  public static final int MQUERY_LOCKINFO = 3;
  
  public static final int MTABLE_ARG = 1;
  public static final int MTABLE_INFO = 2;
  public static final int MTABLE_LOCKINFO = 3;
  
  public static final int MWINDOW_ARG = 1;
  public static final int MWINDOW_INFO = 2;
  public static final int MWINDOW_LOCKINFO = 3;

  public static final int MUSERFUNC_ARG = 1;
  public static final int MUSERFUNC_INFO = 2;
  public static final int MUSERFUNC_LOCKINFO = 3;

  public static final int MVIEW_ARG = 1;
  public static final int MVIEW_INFO = 2;
  public static final int MVIEW_LOCKINFO = 3;

  public static final int MSYNONYM_ARG = 1;
  public static final int MSYNONYM_INFO = 1;
  public static final int MSYNONYM_LOCKINFO = 1;

  public static final int MSYSTEM_ARG = 1;
  public static final int MSYSTEM_INFO = 2;
  public static final int MSYSTEM_LOCKINFO = 3;

  public static final int MCACHE_ARG = 1;
  public static final int MCACHE_INFO = 2;
  public static final int MCACHE_LOCKINFO = 3;
  
  public static final int SYSTEMSTATE_QUERIES = 1;
  public static final int SYSTEMSTATE_TABLES = 2;
  public static final int SYSTEMSTATE_WINDOWS = 3;
  public static final int SYSTEMSTATE_USERFUNCS = 4;
  public static final int SYSTEMSTATE_VIEWS = 5;
  public static final int SYSTEMSTATE_OPERATORS = 6;
  public static final int SYSTEMSTATE_OPERATORS_DS = 7;
  public static final int SYSTEMSTATE_SYNONYMS = 8;
  public static final int SYSTEMSTATE_LOGLEVELS = 9;
 
  private static class LevelForArea {
    ILogArea m_area;
    LevelDesc[]   m_levels;
    LevelForArea(ILogArea a, LevelDesc[] l) {
      m_area = a;
      m_levels = l;
    }
  }
  
  private static LevelForArea[] s_levelsForArea = new LevelForArea[] {
    new LevelForArea(LogArea.QUEUE, new LevelDesc[] {
      new LevelDesc(QUEUE_INFO, "QUEUE_INFO"),
      new LevelDesc(QUEUE_ELEMENT_PINNED, "QUEUE_ELEMENT_PINNED"),
      new LevelDesc(QUEUE_ELEMENT_UNPINNED, "QUEUE_ELEMENT_UNPINNED"),
      new LevelDesc(QUEUE_STATS, "QUEUE_STATS"),
      new LevelDesc(QUEUE_DUMP, "QUEUE_DUMP"),
      new LevelDesc(QUEUE_DUMPELEMS, "QUEUE_DUMPELEMS")}),
    
    new LevelForArea(LogArea.STORE, new LevelDesc[] {
      new LevelDesc(STORE_INFO, "STORE_INFO"),
      new LevelDesc(STORE_TUPLE_PINNED, "STORE_TUPLE_PINNED"),
      new LevelDesc(STORE_TUPLE_UNPINNED, "STORE_TUPLE_UNPINNED"),
      new LevelDesc(STORE_STATS, "STORE_STATS"),
      new LevelDesc(STORE_DUMP, "STORE_DUMP"),
      new LevelDesc(STORE_DUMPELEMS, "STORE_DUMPELEMS")}),
    
    new LevelForArea(LogArea.INDEX, new LevelDesc[] {
      new LevelDesc(INDEX_INFO, "INDEX_INFO"),
      new LevelDesc(INDEX_TUPLE_PINNED, "INDEX_TUPLE_PINNED"),
      new LevelDesc(INDEX_TUPLE_UNPINNED, "INDEX_TUPLE_UNPINNED"),
      new LevelDesc(INDEX_STATS, "INDEX_STATS"),
      new LevelDesc(INDEX_DUMP, "INDEX_DUMP"),
      new LevelDesc(INDEX_DUMPELEMS, "INDEX_DUMPELEMS")}),
    
    new LevelForArea(LogArea.SYNOPSIS, new LevelDesc[] {
      new LevelDesc(SYNOPSIS_INFO, "SYNOPSIS_INFO"),
      new LevelDesc(SYNOPSIS_TUPLE_PINNED, "SYNOPSIS_TUPLE_PINNED"),
      new LevelDesc(SYNOPSIS_TUPLE_UNPINNED, "SYNOPSIS_TUPLE_UNPINNED"),
      new LevelDesc(SYNOPSIS_STATS, "SYNOPSIS_STATS"),
      new LevelDesc(SYNOPSIS_DUMP, "SYNOPSIS_DUMP"),
      new LevelDesc(SYNOPSIS_DUMPELEMS, "SYNOPSIS_DUMPELEMS"),
      new LevelDesc(SYNOPSIS_INDEX, "SYNOPSIS_INDEX")}),

    new LevelForArea(LogArea.OPERATOR, new LevelDesc[] {
      new LevelDesc(OPERATOR_INFO, "OPERATOR_INFO"),
      new LevelDesc(OPERATOR_STATS, "OPERATOR_STATS"),
      new LevelDesc(OPERATOR_STRUCTURES_STATS, "OPERATOR_STRUCTURES_STATS"),
      new LevelDesc(OPERATOR_STRUCTURES_LEAST, "OPERATOR_STRUCTURES_LEAST"),
      new LevelDesc(OPERATOR_STRUCTURES_MORE, "OPERATOR_STRUCTURES_MORE"),
      new LevelDesc(OPERATOR_STRUCTURES_MOST, "OPERATOR_STRUCTURES_MOST"),
      new LevelDesc(OPERATOR_DUMP, "OPERATOR_DUMP"),
      new LevelDesc(OPERATOR_DUMP_DETAIL, "OPERATOR_DUMP_DETAIL")}),
    
    new LevelForArea(LogArea.SPILL, new LevelDesc[] {
      new LevelDesc(SPILL_ARG, "SPILL_ARG"),
      new LevelDesc(SPILL_EVICTINFO, "SPILL_EVICTINFO"),
      new LevelDesc(SPILL_STAT, "SPILL_STAT"),
      new LevelDesc(SPILL_DUMP, "SPILL_DUMP"),
      new LevelDesc(SPILL_DUMP_DETAIL, "SPILL_DUMP_DETAIL")}),

    new LevelForArea(LogArea.STORAGE, new LevelDesc[] {
      new LevelDesc(STORAGE_ARG, "STORAGE_ARG"),
      new LevelDesc(STORAGE_DBINFO, "STORAGE_DBINFO"),
      new LevelDesc(STORAGE_DBSTAT, "STORAGE_DBSTAT")}),
      
    new LevelForArea(LogArea.METADATA_QUERY, new LevelDesc[] {
      new LevelDesc(MQUERY_ARG, "MQUERY_ARG"),
      new LevelDesc(MQUERY_INFO, "MQUERY_INFO"),
      new LevelDesc(MQUERY_LOCKINFO , "MQUERY_LOCKINFO")}),
    
    new LevelForArea(LogArea.METADATA_TABLE, new LevelDesc[] {
      new LevelDesc(MTABLE_ARG, "MTABLE_ARG"),
      new LevelDesc(MTABLE_INFO, "MTABLE_INFO"),
      new LevelDesc(MTABLE_LOCKINFO , "MTABLE_LOCKINFO")}),
    
    new LevelForArea(LogArea.METADATA_WINDOW, new LevelDesc[] {
      new LevelDesc(MWINDOW_ARG, "MWINDOW_ARG"),
      new LevelDesc(MWINDOW_INFO, "MWINDOW_INFO"),
      new LevelDesc(MWINDOW_LOCKINFO, "MWINDOW_LOCKINFO")}),

    new LevelForArea(LogArea.METADATA_USERFUNC, new LevelDesc[] {
      new LevelDesc(MUSERFUNC_ARG, "MUSERFUNC_ARG"),
      new LevelDesc(MUSERFUNC_INFO, "MUSERFUNC_INFO"),
      new LevelDesc(MUSERFUNC_LOCKINFO , "MUSERFUNC_LOCKINFO")}),

    new LevelForArea(LogArea.METADATA_VIEW, new LevelDesc[] {
      new LevelDesc(MVIEW_ARG, "MVIEW_ARG"),
      new LevelDesc(MVIEW_INFO, "MVIEW_INFO"),
      new LevelDesc(MVIEW_LOCKINFO , "MVIEW_LOCKINFO")}),

    new LevelForArea(LogArea.METADATA_SYNONYM, new LevelDesc[] {
      new LevelDesc(MSYNONYM_ARG, "MSYNONYM_ARG"),
      new LevelDesc(MSYNONYM_INFO, "MSYNONYM_INFO"),
      new LevelDesc(MSYNONYM_LOCKINFO , "MSYNONYM_LOCKINFO")}),

    new LevelForArea(LogArea.METADATA_SYSTEM, new LevelDesc[] {
      new LevelDesc(MSYSTEM_ARG, "MSYSTEM_ARG"),
      new LevelDesc(MSYSTEM_INFO, "MSYSTEM_INFO"),
      new LevelDesc(MSYSTEM_LOCKINFO, "MSYSTEM_LOCKINFO")}),
      
    new LevelForArea(LogArea.METADATA_CACHE, new LevelDesc[] {
      new LevelDesc(MCACHE_INFO, "MCACHE_INFO"),
      new LevelDesc(MCACHE_LOCKINFO, "MCACHE_LOCKINFO")}),

    new LevelForArea(LogArea.SYSTEMSTATE, new LevelDesc[] {
      new LevelDesc(SYSTEMSTATE_QUERIES, "SYSTEMSTATE_QUERIES"),
      new LevelDesc(SYSTEMSTATE_TABLES, "SYSTEMSTATE_TABLES"),
      new LevelDesc(SYSTEMSTATE_WINDOWS, "SYSTEMSTATE_WINDOWS"),
      new LevelDesc(SYSTEMSTATE_USERFUNCS, "SYSTEMSTATE_USERFUNCS"),
      new LevelDesc(SYSTEMSTATE_VIEWS, "SYSTEMSTATE_VIEWS"),
      new LevelDesc(SYSTEMSTATE_OPERATORS, "SYSTEMSTATE_OPERATORS"),
      new LevelDesc(SYSTEMSTATE_OPERATORS_DS, "SYSTEMSTATE_OPERATORS_DS"),
      new LevelDesc(SYSTEMSTATE_SYNONYMS, "SYSTEMSTATE_SYNONYMS"),
      new LevelDesc(SYSTEMSTATE_LOGLEVELS, "SYSTEMSTATE_LOGLEVELS")}),
  };
  
  public static LevelDesc[] getLevelDescs(ILogArea a)
  {
    for (LevelForArea la : s_levelsForArea) {
      if (la.m_area == a) {
        return la.m_levels;
      }
    }
    return null;
  }

  public static int[] getLevels(ILogArea a)
  {
    LevelDesc[] lds = getLevelDescs(a);
    if (lds == null) return null;
    int[] res = new int[lds.length];
    int pos = 0;
    for (LevelDesc l : lds)
      res[pos++] = l.getLevel();
    return res;
  }
  
  public static String getLevelDesc(ILogArea a, int level)
  {
    if (level == STACKTRACE)
      return "STACKTRACE";
    LevelDesc[] lds = getLevelDescs(a);
    if (lds == null) return null;
    for (LevelDesc l : lds)
    {
      if (l.getLevel() == level) return l.getDesc();
    }
    return null;
  }
}
