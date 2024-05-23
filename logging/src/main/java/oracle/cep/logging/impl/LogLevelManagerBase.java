/* $Header: cep/wlevs_cql/modules/cqlengine/logging/src/oracle/cep/logging/impl/LogLevelManagerBase.java /main/8 2010/07/08 11:42:23 apiper Exp $ */

/* Copyright (c) 2007, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES

 MODIFIED    (MM/DD/YY)
 hopark      04/21/09 - change dump api
 hopark      03/17/09 - change level for tracing msg
 hopark      02/05/09 - change api
 hopark      01/26/09 - change setConfig api
 hopark      01/21/09 - fix NPE
 hopark      12/08/08 - synchronize trace
 hopark      12/02/08 - add setConfig
 hopark      10/10/08 - remove statics
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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/logging/src/oracle/cep/logging/impl/LogLevelManagerBase.java /main/8 2010/07/08 11:42:23 apiper Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logging.impl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.oracle.cep.common.util.SecureFile;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILogArea;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.ILogLevelManager;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.Levels;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.dumper.StrDumper;
import oracle.cep.logging.dumper.StrFileDumper;
import oracle.cep.logging.dumper.XMLDumper;
import oracle.cep.logging.dumper.XMLFileDumper;

public abstract class LogLevelManagerBase implements ILogLevelManager
{
  static final String LOG_TEMP_PREFIX = "LogTemp";

  static final int STACKTRACE_LEVEL = 0;

  static final String TRACES_TAG = "Traces";
  static final String AREAS_TAG = "Areas";
  static final String AREA_TAG = "Area";
  static final String IDS_TAG = "Ids";
  static final String ID_TAG = "Id";
  static final String EVENTS_TAG = "Events";
  static final String EVENT_TAG = "Event";
  
  String        m_traceFolder;
  String        m_tracePostfix;
  boolean       m_useXMLTag  = true;
  LevelGroup  m_flags[];

  /**
   * LevelMap defines levels that is accessed by index
   */
  private class LevelMap
  {
    ArrayList<Levels> m_levels;

    public LevelMap()
    {
      m_levels = new ArrayList<Levels>();
    }

    public void setLevel(boolean enable, int level)
    {
      if (m_levels == null)
        return;
      int pos = 0;
      for (Levels l : m_levels) {
        l.set(enable, level);
        if (l.isEmpty()) {
          m_levels.set(pos, null);
        }
        pos++;
      }
    }

    public void setLevel(boolean enable, int id, int level)
    {
      Levels l = null;
      if (m_levels.size() <= id) {
        while (m_levels.size() < (id + 1))
          m_levels.add(null);
      } 
      l = m_levels.get(id);
      if (l == null) {
        l = new Levels();
        m_levels.set(id, l);
      }
      l.set(enable, level);
      if (l.isEmpty()) {
        m_levels.set(id, null);
      }
    }

    public Levels getLevels(int id)
    {
      Levels l = null;
      if (id < m_levels.size()) {
        l = m_levels.get(id);
      }
      return l;
    }

    public List<Integer> getIds()
    {
      List<Integer> res = new ArrayList<Integer>(m_levels.size());
      for (int i = 0; i < m_levels.size(); i++) {
        Levels l = m_levels.get(i);
        if (l == null)
          continue;
        res.add(i);
      }
      return res;
    }

    public String getInfoXML()
    {
      StringBuffer buf = new StringBuffer();
      buf.append("<"); buf.append(EVENTS_TAG); buf.append(">\n");
      for (int i = 0; i < m_levels.size(); i++) {
        Levels l = m_levels.get(i);
        if (l != null) {
          buf.append("<"); buf.append(EVENT_TAG); buf.append(" ");
          ILogEvent ev = getLogEventFromValue(i);
          if (ev != null) 
          {
            buf.append("name=\"");
            buf.append(ev.getName());
            buf.append("\" ");
          }
          buf.append("value=\"");
          buf.append(ev.getValue());
          buf.append("\">\n");
          buf.append(l.getInfoXML());
          buf.append("</"); buf.append(EVENT_TAG); buf.append(">\n");
        }
      }
      buf.append("</"); buf.append(EVENTS_TAG); buf.append(">\n");
      return buf.toString();
    }

    public String toString()
    {
      StringBuffer buf = new StringBuffer();
      for (int i = 0; i < m_levels.size(); i++) {
        Levels l = m_levels.get(i);
        if (l != null) {
          ILogEvent ev = getLogEventFromValue(i);
          if (ev != null) buf.append(ev.toString());
          buf.append("(" + i + ")");
          buf.append(" : ");
          buf.append(l.toString());
          if (i < (m_levels.size() - 1) )
          {
            buf.append(" , ");
          }
        }
      }
      return buf.toString();
    }
  }

  private class LevelGroup
  {
    ILogArea             m_area;

    ArrayList<LevelMap> m_idLevels; // indexed by id, then event

    LevelGroup(ILogArea area)
    {
      m_area = area;
      m_idLevels = new ArrayList<LevelMap>();
    }

    public String getInfoXML()
    {
      StringBuffer buf = new StringBuffer();
      buf.append("<"); buf.append(IDS_TAG); buf.append(">\n");
      for (int i = 0; i < m_idLevels.size(); i++) {
        LevelMap l = m_idLevels.get(i);
        if (l == null)
          continue;
        buf.append("<"); buf.append(ID_TAG); buf.append(" ");
        buf.append("id=");
        buf.append(i);
        buf.append(">\n");
        buf.append(l.getInfoXML());
        buf.append("</"); buf.append(ID_TAG); buf.append(">\n");
      }
      buf.append("</"); buf.append(IDS_TAG); buf.append(">\n");
      return buf.toString();
    }

    public String toString()
    {
      StringBuffer buf = new StringBuffer();
      for (int i = 0; i < m_idLevels.size(); i++) {
        LevelMap l = m_idLevels.get(i);
        if (l == null)
          continue;
        buf.append(m_area.toString() + " id = " + i + " -  ");
        buf.append(l.toString());
        buf.append("\n");
      }
      return buf.toString();
    }

    private int getEventId(ILogEvent event)
    {
      return event.getValue();
    }

    public List<Integer> getIds()
    {
      List<Integer> res = new ArrayList<Integer>(m_idLevels.size());
      for (int i = 0; i < m_idLevels.size(); i++) {
        LevelMap l = m_idLevels.get(i);
        if (l == null)
          continue;
        res.add(i);
      }
      return res;
    }
    
    public List<Integer> getEventIds(int id)
    {
      LevelMap lm = null;
      if (id < m_idLevels.size())
        lm = m_idLevels.get(id);
      if (lm == null)
        return null;
      return lm.getIds();
    }
    
    public void setLevel(boolean enable, int id, ILogEvent event, int level)
    {
      assert (id >= 0);
      assert (level >= 0);

      assert (id >= 0);
      assert (level >= 0);

      LevelMap lm = null;
      if (m_idLevels.size() <= id) {
        while (m_idLevels.size() < (id + 1)) {
          m_idLevels.add(null);
        }
      } 
      lm = m_idLevels.get(id);
      if (lm == null) {
        lm = new LevelMap();
        m_idLevels.set(id, lm);
      }
      if (event == null) {
        // If event is not specified, turn on/off level for all known events
        ILogEvent[] events = getEvents(m_area);
        for (ILogEvent ev : events) {
          int eventId = getEventId(ev);
          lm.setLevel(enable, eventId, level);
        }
      } else {
        int eventId = getEventId(event);
        assert (eventId >= 0);
        lm.setLevel(enable, eventId, level);
      }
    }

    public Levels getLevels(int id, ILogEvent event)
    {
      LevelMap lm = null;
      if (id < m_idLevels.size())
        lm = m_idLevels.get(id);
      if (lm == null)
        return null;
      int eventId = getEventId(event);
      return lm.getLevels(eventId);
    }
  };

  public LogLevelManagerBase()
  {
    m_traceFolder = getTempFolder();
    m_tracePostfix = "@TRC_ID@_@TRC_DATETIME@";
    ILogArea[] areaValues = getLogAreas();
    m_flags = new LevelGroup[areaValues.length];
    Arrays.fill(m_flags, null);
  }

  private String getTempFolder()
  {
    Path temp = null;
	try {
		String tempDirStr = System.getProperty("java.io.tmpdir");
		File tempDir = SecureFile.getFile(tempDirStr);
		tempDir.mkdirs();
		temp = Files.createTempDirectory(LOG_TEMP_PREFIX);
	} catch (IOException e) {
		throw new RuntimeException(e);
	}
    return temp.toString();
  }

  public void setConfig(boolean useXML, String traceFolder)
  {
    m_traceFolder = traceFolder;
    m_useXMLTag = useXML;
  }

  public String getTraceFolder() {return m_traceFolder;}
  public boolean getUseXML() {return m_useXMLTag;}

  public void clear()
  {
    Arrays.fill(m_flags, null);
  }

  public void dumpLevels(ILogArea area, ILogEvent event, ILoggable target, List<Integer> levels)
  {
    try {
      Levels tlevels = Levels.fromList(levels);
      traceLevels(area, event, target, tlevels, (Object[]) null);
    }
    catch(Throwable e)
    {
      //eats up any exception
      LogUtil.warning(LoggerType.TRACE, e.toString());
    }
  }
  
  public void setLevels(boolean enable, ILogArea area, int id, 
                     List<ILogEvent> events, List<Integer> levels)
  {
    if (area == null)
      throw new IllegalArgumentException("area cannot be null.");
    if (levels == null)
      throw new IllegalArgumentException("levels cannot be null.");
    
    for (int level : levels) {
      if (events != null && events.size() > 0) 
      {
        for (ILogEvent event : events) 
        {
          setLevel(enable, area, id, event, level);
        }
      } 
      else 
      {
        setLevel(enable, area, id, null, level);
      }
    }
  }

  public void setLevel(boolean enable, ILogArea a, int id, ILogEvent event, int level)
  {
    assert (a != null);
    assert (level >= 0);

    int area = a.getValue();
    if (m_flags[area] == null)
      m_flags[area] = new LevelGroup(a);
    m_flags[area].setLevel(enable, id, event, level);
  }

  public Levels getLevels(ILogArea a, int id, ILogEvent event)
  {
    int area = a.getValue();
    if (m_flags[area] == null)
      return null;
    return m_flags[area].getLevels(id, event);
  }

  /**
   * only for unit testing. 
   */
  public List<Integer> getIds(ILogArea a)
  {
    int area = a.getValue();
    if (m_flags[area] == null)
      return null;
    return m_flags[area].getIds();
  }

  /**
   * only for unit testing
   */
  public List<Integer> getEventIds(ILogArea a, int id)
  {
    int area = a.getValue();
    if (m_flags[area] == null)
      return null;
    return m_flags[area].getEventIds(id);
  }

  public String getCurrentInfoXML()
  {
    StringBuffer buf = new StringBuffer();
    buf.append("<"); buf.append(TRACES_TAG); buf.append(">\n");
    buf.append("<"); buf.append(AREAS_TAG); buf.append(">\n");
    for (ILogArea a : getLogAreas()) {
      int area = a.getValue();
      if (m_flags[area] != null) {
        buf.append("<"); buf.append(AREA_TAG); buf.append(" ");
        buf.append("name=\"");
        buf.append(a.getName());
        buf.append("\" ");
        buf.append("value=\"");
        buf.append(a.getValue());
        buf.append("\">\n");
        buf.append(m_flags[area].getInfoXML());
        buf.append("<"); buf.append(AREA_TAG); buf.append(">\n");
      }
    }
    buf.append("</"); buf.append(AREAS_TAG); buf.append(">\n");
    buf.append("</"); buf.append(TRACES_TAG); buf.append(">\n");
    return buf.toString();
  }
  
  public String getCurrentInfo()
  {
    StringBuffer buf = new StringBuffer();
    for (ILogArea a : getLogAreas()) {
      int area = a.getValue();
      if (m_flags[area] != null) {
        buf.append(a.getName());
        buf.append("(");
        buf.append(area);
        buf.append(") ");
        buf.append(m_flags[area].toString());
        buf.append("\n");
      }
    }
    return buf.toString();
  }
  
  public String toString()
  {
    return getCurrentInfo();
  }
  
  public synchronized void dump(IDumpContext dumper)
  {
    dumper.beginTag(LogUtil.TAG_LEVELS, null, null);
    for (ILogArea a : getLogAreas()) 
    {
      int area = a.getValue();
      if (m_flags[area] != null) {
        assert (LogUtil.ATTR_EVENTTARGET.length == 3);
        String[] attrTags = {"Id"};
        Object[] attrs = new Object[1];
        String evTag = "Event";
        String[] evAttrTags = {"Id", "Name", "Levels"};
        Object[] evAttrs = new Object[3];
        for (int i = 0; i < m_flags[area].m_idLevels.size(); i++) {
          LevelMap l = m_flags[area].m_idLevels.get(i);
          if (l == null)
            continue;
          attrs[0] = i;
          String tag = a.toString();
          dumper.beginTag(tag, attrTags, attrs);
          for (int j = 0; j < l.m_levels.size(); j++) {
            Levels lvl = l.m_levels.get(j);
            if (lvl != null) {
              ILogEvent ev = getLogEventFromValue(j);
              evAttrs[0] = j;
              evAttrs[1] = "";
              if (ev != null) {
                evAttrs[1] =ev.toString();
              }
              evAttrs[2] = lvl.toString();
              dumper.beginTag(evTag, evAttrTags, evAttrs);
              dumper.endTag(evTag);
            }
          }
          dumper.endTag(tag);
        }
      }
    }
    dumper.endTag(LogUtil.TAG_LEVELS);
  }

  private String getCurrentStackTrace()
  {
    try 
    {
      throw new Exception();
    } catch(Throwable a)
    {
      Writer result = new StringWriter();
      PrintWriter printWriter = new PrintWriter(result);
      a.printStackTrace(printWriter);
      return result.toString();
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
  public synchronized void traceLevels(ILogArea area, ILogEvent event, ILoggable target, 
                           Levels levels, Object ... args) 
  {
    assert (target != null);
    assert (levels != null);    
    
    LogUtil.fine(LoggerType.TRACE, "trace : area=" + area.toString() + " event=" + event.toString() +
        " target=" + target.getClass().getSimpleName() + " levels=" + levels.toString());
    IDumpContext dumper = openDumper(null, null);
    Object attrs[];
    
    assert (LogUtil.ATTR_EVENTTARGET.length == 3);
    attrs = new Object[3];
    attrs[0] = event.toString();
    attrs[1] = Integer.toString(target.getTargetId());
    attrs[2] = target.getTargetName();
    dumper.beginTag(LogUtil.EVENT, LogUtil.ATTR_EVENTTARGET, attrs);

    int infoLevel = -1;
    int pinLevel = -1;
    int unpinLevel = -1;
    int dumpLevel = -1;
    int verboseDumpLevel = -1;
    DumpDesc dumpdesc = LogUtil.getDumpDesc(target);
    if (dumpdesc != null)
    {
      infoLevel = dumpdesc.infoLevel();
      pinLevel = dumpdesc.evPinLevel();
      unpinLevel = dumpdesc.evUnpinLevel();
      dumpLevel = dumpdesc.dumpLevel();
      verboseDumpLevel = dumpdesc.verboseDumpLevel();
    }
    Object levelVals[] = new Object[2];
    for (int level = levels.nextSetBit(0); level >= 0; level = levels.nextSetBit(level + 1))
    {
      // pinLevel and unpinLevel is not available for dump, skip them.
      if (event == DumpEvent.DUMP && (level == pinLevel || level == unpinLevel))
      {
        continue;
      }
      levelVals[0] = getLevelDesc(area, level);
      levelVals[1] = level;
      dumper.beginTag(LogUtil.LEVEL, LogUtil.ATTR_LEVEL, levelVals);
      if (level == STACKTRACE_LEVEL)
      {
        dumper.writeln(LogUtil.STACKTRACE, getCurrentStackTrace());
      } else  if (level == infoLevel)
      {
        String tag = LogUtil.beginDumpObj(dumper, target);
        LogUtil.endDumpObj(dumper, tag);
      } 
      else if (level == pinLevel || level == unpinLevel)
      {
        LogUtil.logMsg(dumper, level, target, args);
      } 
      else  if (level == dumpLevel || level == verboseDumpLevel)
      {
        dumper.setLevel(area, level, (level == verboseDumpLevel));
        target.dump(dumper);
      }
      else
      {
        target.trace(dumper, event, level, args);
      }
      dumper.endTag(LogUtil.LEVEL);
    }
    
    dumper.endTag(LogUtil.EVENT);
    closeDumper(null, null, dumper);
  }

  public IDumpContext openDumper(String key, IDumpContext prev)
  {
    if (prev != null) 
    {
      IDumpContext newdc;
      if (m_useXMLTag)
      {
        newdc = new XMLFileDumper(this, key, m_traceFolder, m_tracePostfix);
      }
      else 
      {
        newdc = new StrFileDumper(this, key, m_traceFolder, m_tracePostfix);
      }
      newdc.setLevel(prev.getArea(), prev.getLevel(), prev.isVerbose());
      return newdc;
    }

    // Otherwise, create one
    if (m_useXMLTag)
    {
      return new XMLDumper(this);
    }
    return new StrDumper(this);
  }
  
  public void closeDumper(String key, IDumpContext prev, IDumpContext current)
  {
    assert (current != null);
    if (!current.isVoid())
    {
      current.close();
      
      if (prev == null)
        LogUtil.info(LoggerType.TRACE, current.toString());
      else
        prev.writeln(key, current.toString());
    }
  }
  
}
