/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logging/trace/LogArea.java /main/4 2010/01/06 20:33:11 parujain Exp $ */

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
    hopark      12/03/08 - add isSystem
    hopark      11/22/08 - add toString
    hopark      06/18/08 - refactor
    hopark      01/09/08 - add cache area
    hopark      12/20/07 - fix fromValue
    hopark      08/01/07 - add spill
    hopark      06/07/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logging/trace/LogArea.java /main/4 2010/01/06 20:33:11 parujain Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logging.trace;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import oracle.cep.logging.ILogArea;

public class LogArea implements ILogArea
{
  public static LogArea SYSTEMSTATE; 
  public static LogArea QUERY; 
  public static LogArea OPERATOR; 
  public static LogArea QUEUE; 
  public static LogArea SYNOPSIS; 
  public static LogArea STORE; 
  public static LogArea INDEX; 
  public static LogArea STORAGE;
  public static LogArea SPILL;
  public static LogArea METADATA_QUERY;
  public static LogArea METADATA_TABLE;
  public static LogArea METADATA_WINDOW;
  public static LogArea METADATA_USERFUNC;
  public static LogArea METADATA_VIEW;
  public static LogArea METADATA_SYSTEM;
  public static LogArea METADATA_SYNONYM;
  public static LogArea METADATA_CACHE;
  
  String  m_name;
  boolean m_system; // true : if the target is system service (e.g configManager, etc)
  boolean m_global; // true : if the target is global (e.g single instance)
                    // false : if the target can be identified by ids
  int     m_value;
  private static int s_ordinal;
  
  private static boolean GLOBAL = true;
  private static boolean SYSTEM = true;
  
  private static Map<Integer, ILogArea> s_logAreaMap;
  static 
  {
    s_ordinal = 0;
    s_logAreaMap = new HashMap<Integer, ILogArea>();
    SYSTEMSTATE = addLogArea("systemstate", GLOBAL, false); 
    QUERY = addLogArea("query", false, false); 
    OPERATOR = addLogArea("operator", false, false ); 
    QUEUE = addLogArea("queue", false, false); 
    SYNOPSIS = addLogArea("synopsis", false, false); 
    STORE = addLogArea("store", false, false); 
    INDEX = addLogArea("index", false, false); 
    STORAGE = addLogArea("storage", GLOBAL, SYSTEM);
    SPILL = addLogArea("spill", GLOBAL, SYSTEM);
    METADATA_QUERY = addLogArea("metadata_query", GLOBAL, false);
    METADATA_TABLE = addLogArea("metadata_table", GLOBAL, false);
    METADATA_WINDOW = addLogArea("metadata_window", GLOBAL, false);
    METADATA_USERFUNC = addLogArea("metadata_userfunc", GLOBAL, false);
    METADATA_VIEW = addLogArea("metadata_view", GLOBAL, false);
    METADATA_SYNONYM = addLogArea("metadata_synonym", GLOBAL, false);
    METADATA_SYSTEM = addLogArea("metadata_system", GLOBAL, SYSTEM);
    METADATA_CACHE = addLogArea("metadata_cache", GLOBAL, false);
  }
  
  private LogArea(String name, boolean global, boolean system, int v)
  {
    m_name = name;
    m_global = global;
    m_system = system;
    m_value = v;
  }

  public String toString() {return m_name;}
  
  private static LogArea addLogArea(String name, boolean global, boolean system)
  {
    LogArea a = new LogArea(name, global, system, s_ordinal++);
    s_logAreaMap.put(a.m_value, a);
    return a;
  }

  public boolean isSystem() {return m_system;}
  public boolean isGlobal() {return m_global;}
  public String getName() {return m_name;}
  public int getValue() {return m_value;}

  public static Collection<ILogArea> values()
  {
    return s_logAreaMap.values();
  }
  
  public static ILogArea fromValue(int v)
  {
    return s_logAreaMap.get(v);
  }
};
