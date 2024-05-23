/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/util/DebugLogger.java /main/14 2009/07/22 08:50:50 sbishnoi Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    DebugLogger is to help debugging refcount/pincount problem.
    It loggs activities related with refcount or pincount along with
    stack trace.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    07/21/09 - incorporating cep directory chahnges
    hopark      06/11/09 - print out leak info on system.out
    hopark      02/27/08 - add value
    hopark      12/05/07 - cleanup spill
    hopark      06/20/07 - cleanup
    parujain    06/07/07 - lint error
    hopark      04/24/07 - print individual reports from printAll
    hopark      03/29/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/util/DebugLogger.java /main/14 2009/07/22 08:50:50 sbishnoi Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import com.oracle.cep.common.util.SecureFile;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

public class DebugLogger
{
  public static boolean METHOD = true;
  public static boolean FIELD = false;
  public static final int QUICK_LEAKS = 1;
  public static final int FILTERED_HISTORY = 2;
  public static final int VALUE_HISTORY = 4;
  public static final int TRACE_HISTORY = 8;
  public static final int DEFAULT = QUICK_LEAKS | TRACE_HISTORY | VALUE_HISTORY;
  public static final int ALL = 0xFFFF;
  
  private static AtomicInteger s_nextId = new AtomicInteger();

  int m_id;
  boolean m_free;
  
  String    m_name;
  Map<Long, Entry> m_entries;
  List<Long> m_freedList;
  boolean m_isMethod;
  String m_field;
  Map<String, BreadCrumb> m_bcMap;
  List<BreadCrumb> m_bcList;
  Map<BreadCrumbs, BreadCrumbs> m_traceMap;
  List<BreadCrumbs> m_traceList;
  static IgnoreList s_ignoreList = new IgnoreList();
  
  enum EntryType {NORMAL, NOT_INTERESED, IGNORED, NOT_FOUND};
  
  public DebugLogger(String name, boolean isMethod, String field, boolean free)
  {
    m_name = name;
    m_free = free;
    m_id = s_nextId.getAndIncrement();
    m_isMethod = isMethod;
    m_field = field;
    clear();
  }

  public void clear()
  {
    m_entries = new HashMap<Long, Entry>();
    m_freedList = new LinkedList<Long>();
    m_bcMap = new HashMap<String, BreadCrumb>();
    m_bcList= new LinkedList<BreadCrumb>();
    m_traceMap = new HashMap<BreadCrumbs, BreadCrumbs>();
    m_traceList= new LinkedList<BreadCrumbs>();
  }

  private int addBreadCrumb(StackTraceElement curStack)
  {
    String className = curStack.getClassName();
    String methodName = curStack.getMethodName();
    int line = curStack.getLineNumber();
    StringBuffer res = new StringBuffer();
    res.append(className);
    res.append(".");
    res.append(methodName);
    res.append(":");
    res.append(line);
    String desc = res.toString();
    BreadCrumb bc = m_bcMap.get(desc);
    if (bc != null) return bc.m_id;
    
    bc = new BreadCrumb(desc, methodName);
    m_bcMap.put(desc, bc);
    int pos = m_bcList.size();
    bc.setId(pos);
    m_bcList.add(bc);
    return pos;
  }

  private int addTrace(StackTraceElement[] trace)
  {
    int len = trace.length;
    int[] ids = new int[len-1];
    int pos = 0;
    // skip the first one in the trace, it should be from DebugLogger.log
    for (int i = 1; i < trace.length; i++)
    {
      ids[pos++] = addBreadCrumb(trace[i]);
    }
    
    BreadCrumbs key = new BreadCrumbs(ids);
    BreadCrumbs bcs = m_traceMap.get(key);
    if (bcs != null) 
      return bcs.m_id;
    
    bcs = key;
    m_traceMap.put(bcs, bcs);
    int cur = m_traceList.size();
    bcs.setId(cur);
    m_traceList.add(bcs);
    return cur;
  }
  
  private Entry getEntry(long id, Object elem)
  {
    Entry e = m_entries.get(id);
    if (e == null && elem != null)
    {
      e = new Entry(id, elem.getClass().getName(), elem.toString());
      m_entries.put(id, e);
    }
    return e;
  }

  private boolean isInterested(long id)
  {
    if (DebugUtil.DEBUGI_IDs != null)
    {
      int found = Arrays.binarySearch(DebugUtil.DEBUGI_IDs, id);
      if (found < 0) return false;
    }	  
    return true;
  }

  public synchronized void log(long id, Object elem, Object... args)
  {
    if (!isInterested(id)) return;
	
    Entry e = getEntry(id, elem);
    boolean noreport = false;
    //try {
      //noreport = StoredFactory.isStaticPin(elem);
      if (noreport) 
      {
        m_entries.remove(id);        
        return;
      }
    //} catch(ExecException ee) {}
    StackTraceElement[] stack;
    try
    {
      throw new Exception();
    } catch (Throwable a)
    {
      stack = a.getStackTrace();
    }
    String curval = "0";
    try
    {
      if (m_field != null && elem != null)
      {
        Object v = null;
        if (m_isMethod)
        {
          Method method = elem.getClass().getMethod(m_field, (Class[])null);
          v = method.invoke(elem, (Object[])null);
        } else
        {
          Field field = null;
          Class c = elem.getClass();
          while (field == null && c != null)
          {
            try {
              field = c.getDeclaredField(m_field);
            } catch (NoSuchFieldException me) 
            {
              c = c.getSuperclass();
            }
          }
          if (field == null)
          {
            throw new NoSuchFieldException();
          }
          field.setAccessible(true);
          v = field.get(elem);
        }
        if (v != null)
          curval = v.toString();
      }
    } catch (InvocationTargetException me)
    {
      LogUtil.warning(LoggerType.TRACE, "Failed to access, " + m_field);
    } catch (NoSuchMethodException me)
    {
      LogUtil.warning(LoggerType.TRACE, "Cannot find the method, " + m_field);
    } catch (NoSuchFieldException me)
    {
      //LogUtil.warning(LoggerType.TRACE, "Cannot find the field, " + m_field);
    } catch (IllegalAccessException ie)
    {
      LogUtil.warning(LoggerType.TRACE, "Cannot access, " + m_field);
    }
    Thread curThread = Thread.currentThread();
    Trace s = new Trace(curThread, curval, args, stack);
    //LogUtil.warning(LoggerType.TRACE, elem.toString() + "\n" + s.toString());
    e.addTrace(s);
  }

  public synchronized void logVal(long id, Object elem, Object cval, Object... args)
  {
    if (!isInterested(id)) return;
        
    Entry e = getEntry(id, elem);
    boolean noreport = false;
    //try {
      //noreport = StoredFactory.isStaticPin(elem);
      if (noreport) 
      {
        m_entries.remove(id);        
        return;
      }
    //} catch(ExecException ee) {}
    StackTraceElement[] stack;
    try
    {
      throw new Exception();
    } catch (Throwable a)
    {
      stack = a.getStackTrace();
    }
    String curval = cval.toString();
    Thread curThread = Thread.currentThread();
    Trace s = new Trace(curThread, curval, args, stack);
    //LogUtil.warning(LoggerType.TRACE, elem.toString() + "\n" + s.toString());
    e.addTrace(s);
  }

  public synchronized void delete(long id)
  {
    m_entries.remove(id);
  }

  public static String getFileName(String baseName, String name, long id, String tag)
  {
    String filename = System.getProperty("twork");
    if (filename == null)
      filename = System.getenv("T_WORK");
    filename += "/cep/";
    filename += baseName;
    if (name != null)
      filename += "_" + name + "_";
    filename += "_" + id;
    if (tag != null)
      filename += "_" + tag;
    filename += ".txt";
    return filename;
  }
  
  public synchronized void print(long id, int showFlags)
  {
    printEntry(null, id, true, showFlags);
  }
  
  private synchronized EntryType printEntry(PrintWriter pw, long id, boolean showfilename, int showFlags)
  {
    if (!isInterested(id)) return EntryType.NOT_INTERESED;
    Entry e = getEntry(id, null);
    if (e == null)
    {
        LogUtil.warning(LoggerType.TRACE, "There is no entries for " + id);
        return EntryType.NOT_FOUND;
    }
    EntryType et = EntryType.NORMAL;
    Trace first = e.getFirstTrace();
    if (first != null && s_ignoreList.isIn(first)) 
    {
      et = EntryType.IGNORED;
    }
    String filename = getFileName(m_name, null, id, (et == EntryType.IGNORED) ? "Known":null);
    try {
      if (pw == null)
        pw = new PrintWriter(new FileWriter(SecureFile.getFile(filename)));
      e.print(pw, showFlags);
      pw.close();
    } catch(IOException er)
    {
      LogUtil.warning(LoggerType.TRACE, er.toString());
    }
    if (showfilename && (et != EntryType.IGNORED && et != EntryType.NOT_INTERESED)) 
    {
      LogUtil.warning(LoggerType.TRACE, filename);
    }
    return et;
  }

  public synchronized void free(long id)
  {
    if (!isInterested(id)) return;
    m_freedList.add(id);
    if (m_free)
    {
      delete(id);
    }
  }

  public synchronized boolean isFreed(long id)
  {
    if (!isInterested(id)) return false;
    return m_freedList.contains(id);
  }


  public synchronized int printAll(java.io.Writer writer, String desc, int showFlags)
  {
    String filename = getFileName(m_name, "full", m_id, null);
    int total = 0;
    int count = 0;
    int known = 0;
    try {
      FileWriter fw = new FileWriter(SecureFile.getFile(filename));
      PrintWriter pw = new PrintWriter(fw);
      Set<Long> keySet = m_entries.keySet();
      for (Long key: keySet)
      {
        if (!isFreed(key))
        {
          EntryType et = printEntry(pw, key, false, showFlags);
          total++;
          switch(et)
          {
            case IGNORED : known++; pw.println(key + " : known"); break;
            case NOT_INTERESED : pw.println(key + " : not interested"); break;
            default: count++; pw.println(key); break;
          }
        }
      }
      pw.close();
      fw.close();
    } catch(IOException e)
    {
      LogUtil.warning(LoggerType.TRACE, e.toString());
    }
    if (count>0)
      LogUtil.warning(LoggerType.TRACE, desc + "\n" + total + " leaks, " + known + " known : The details is in "+ filename);
    if (total > 0 || known > 0)
    {
      String msg = m_name + "_" + m_id + ":" + total + " leaks, " + known + " known\n";
      try
      {
        writer.write(msg);
      }
      catch(IOException e)
      {
        LogUtil.warning(LoggerType.TRACE, e.toString());
      }
      System.out.println(msg + "The details is in " + filename);
    } else {
      File f = new File(filename);
      f.delete();
    }
    return total + known;
  }

  private static class BreadCrumb
  {
    int m_id;  
    String m_desc;
    String m_method;
    
    BreadCrumb(String desc, String method)
    {
      m_desc = desc;
      m_method = method;
    }
    public void setId(int id)
    {
      m_id = id;
    }
    public String toString()
    {
      return m_id + " : " + m_desc;
    }
    public String getMethod() {return m_method;}
  }
  
  private class BreadCrumbs
  {
    int[] m_ids;
    int m_id;
    BreadCrumbs(int[] ids)
    {
      m_ids = ids;
    }
    public void setId(int id)
    {
      m_id = id;
    }
    public int hashCode()
    {
      return Arrays.hashCode(m_ids);
    }

    public boolean equals(Object other)
    {
      if (!(other instanceof BreadCrumbs)) return false;
      BreadCrumbs o = (BreadCrumbs) other;
      if (m_ids.length != o.m_ids.length) return false;
      for (int i = 0; i < m_ids.length; i++)
      {
        if (m_ids[i] != o.m_ids[i]) return false;
      }
      return true;
    }
    
    public boolean matches(Object other)
    {
      if (other instanceof LinkedList)
      {
        LinkedList ol = (LinkedList) other;
        for (int pos = 0; pos < ol.size() ; pos++)
        {
          String s = (String) ol.get(pos);
          if (pos >= m_ids.length) 
          {
            return false;
          }
          BreadCrumb bc = m_bcList.get(m_ids[pos]);
          String desc = bc.m_desc;
          int col0 = s.indexOf(':');
          String m = desc;
          if (col0 < 0) 
          {
            int col = desc.indexOf(':');
            m = desc.substring(0, col);
          }
          if (!m.equals(s)) 
          {
            return false;
          }
        }
        return true;
      }
      if (!(other instanceof BreadCrumbs)) return false;
      BreadCrumbs o = (BreadCrumbs) other;
      // first method should be reversed
      int pos = 0;
      BreadCrumb bc = m_bcList.get(m_ids[pos]);
      if (o.m_ids.length < 1) return false;
      BreadCrumb bc1 = m_bcList.get(o.m_ids[pos]);
      String method = bc.getMethod();
      String method1 = bc1.getMethod();
      if ((method.equals("pin") && !method1.equals("unpin")) ||
          (method.equals("unpin") && !method1.equals("pin")) ||
          (method.equals("addRef") && !method1.equals("release")) ||          
          (method.equals("release") && !method1.equals("addRef")))
        return false;
      // look for the first method that is not pin/ref
      for (pos = 1; pos < m_ids.length; pos++)
      {
        bc = m_bcList.get(m_ids[pos]);
        method = bc.getMethod();
        if (!method.equals("pin") &&
            !method.equals("unpin") &&
            !method.equals("addRef") &&
            !method.equals("release"))
          break;
      }
      if (pos == m_ids.length) return false;
      for (pos = 1; pos < o.m_ids.length; pos++)
      {
        bc = m_bcList.get(o.m_ids[pos]);
        method1 = bc.getMethod();
        if (!method1.equals("pin") &&
            !method1.equals("unpin") &&
            !method1.equals("addRef") &&
            !method1.equals("release"))
          break;
      }
      if (pos == o.m_ids.length) return false;
      return method.equals(method1);
    }
    
    public String toString()
    {
      StringBuffer res = new StringBuffer();
      for (int id : m_ids)
      {
        BreadCrumb bc = m_bcList.get(id);
        res.append(bc.m_desc);
        res.append("\n");
      }
      return res.toString();
    }
  }
  
  private class Trace
  {
    int m_id;
    String m_value;
    int   m_traceId;
    long m_threadId;
    String m_threadName;
    Object[] m_args;

    Trace(Thread thread, String val, Object[] args, StackTraceElement[] traces)
    {
      m_threadId = thread.getId();
      m_threadName = thread.getName();
      m_traceId = addTrace(traces);
      m_value = val;
      m_args = args;
    }

    public void setId(int id) {m_id = id;}
    public int getId() {return m_id;}
    
    public String toString()
    {
      StringBuffer res = new StringBuffer();
      res.append(m_id);
      res.append(" ---------\n");
      res.append("Thread= ");
      res.append(m_threadName);
      res.append("(");
      res.append(m_threadId);
      res.append(")");
      res.append(" value = ");
      res.append(m_value);
      if (m_args != null)
      {
        res.append(" args = ");
        int p = 0;
        for (Object arg: m_args)
        {
          if (p > 0)
            res.append(",");
          res.append(arg.toString());
          p++;
        }
      }
      res.append("\n");
      BreadCrumbs trace = m_traceList.get(m_traceId);
      res.append(trace.toString());
      res.append("\n");
      return res.toString();
    }
    
    public BreadCrumbs getBreadCrumbs()
    {
      return m_traceList.get(m_traceId);
    }
  }

  private static class Entry
  {
    long m_id;
    String m_elemClass;
    String m_desc;
    LinkedList<Trace> m_traces;

    Entry(long id, String elemclass, String desc)
    {
      m_id = id;
      m_elemClass = elemclass;
      m_desc = desc;
      m_traces = new LinkedList<Trace>();
    }

    public void addTrace(Trace s)
    {
      int pos = m_traces.size();
      s.setId(pos);
      m_traces.add(s);
    }

    @SuppressWarnings({"unchecked"})
    public void print(java.io.Writer writer, int showFlags)
      throws IOException
    {
      writer.write("=========== ");
      writer.write(m_elemClass);
      writer.write(" ");
      writer.write(Long.toString(m_id));
      writer.write(" ");
      writer.write(m_desc);
      writer.write("\n");

      // look for leaks
      if ((showFlags & QUICK_LEAKS) != 0)
      {
        Stack<Integer> stack = new Stack<Integer>();
        long old = 0;
        for (Trace trace: m_traces)
        {
          String vstr = trace.m_value == null ? "0" : trace.m_value.toString();
          long v = Long.parseLong(vstr);
          if (old > v && !stack.empty())
          {
            stack.pop();
          } else
          {
            stack.push(trace.getId());
          }
          old = v;
        }
        if (!stack.empty())
        {
          writer.write("*** Quick leaks ***\n");
          
          while (!stack.empty())
          {
            Integer p = stack.pop();
            Trace trace = m_traces.get(p);
            writer.write(trace.toString());
            writer.write("\n");
          }
        }
      }
      
      if ((showFlags & FILTERED_HISTORY) != 0)
      {
        int pos = 0;
        LinkedList<Trace> traces = new LinkedList<Trace>();
        try {
        // filter repeating pairs
        int[] traceIds = new int[m_traces.size()];
        for (Trace trace : m_traces)
        {
          traceIds[pos++] = trace.m_traceId;
        }
        IntString is = new IntString(traceIds);
        while (true)
        {
          //System.out.println("s " + is);
          Pair lrs = lrs(is);
          if (lrs == null || lrs.empty())
            break;
          IntString seq = is.substring(lrs.m_b, lrs.m_e);
          //System.out.println("found " + seq + " " + lrs.m_b + "," + lrs.m_e);
          // remove repeated one except the first one
          is = is.remove(seq);
        }
        //System.out.println("r " + is);
        int ids[] = is.m_values;
        for (pos = 0; pos < ids.length; pos++)
        {
          int traceId = ids[pos];
          for (Trace trace : m_traces)
          {
            if (trace.m_traceId == traceId)
            {
              traces.add(trace);
              break;
            }
          }
        }
        } catch (Exception e)
        {
          traces = (LinkedList<Trace>) m_traces.clone();
        }
        
        // filter matched ones
        pos = 0;
        while(true)
        {
          Trace trace = traces.get(pos);
          boolean find = false;
          for (int p1 = pos+1; p1 < traces.size(); p1++)
          {
            Trace trace1 = traces.get(p1);
            BreadCrumbs bc = trace.getBreadCrumbs();
            BreadCrumbs bc1 = trace1.getBreadCrumbs();          
            if (bc.matches(bc1)) 
            {
              find = true;
              traces.remove(p1);
              break;
            }
          }
          if (find)
          {
            traces.remove(pos);
          } else {
            pos++;
          }
          if (pos >= traces.size()) break;
        }
        writer.write("*** Filtered history ***\n");
        for (Trace trace : traces)
        {
          writer.write(trace.toString());
          writer.write("\n");
        }
      }
    
      if ((showFlags & VALUE_HISTORY) != 0)
      {
        writer.write("*** Value history ***\n");
        writer.write("i\n");
        for (Trace trace: m_traces)
        {
          writer.write((trace.getId()+1) * 1000);
          writer.write(" ");
          String vstr = trace.m_value == null ? "0" : trace.m_value.toString();
          writer.write(vstr);
          writer.write("\n");
        }
      }
      
      if ((showFlags & TRACE_HISTORY) != 0)
      {
        writer.write("*** Trace history ***\n");
        for (Trace trace: m_traces)
        {
          writer.write(trace.toString());
          writer.write("\n");
        }
      }
      return;
    }
    
    public Trace getFirstTrace()
    {
      if (m_traces.size() == 0) return null;
      return m_traces.get(0);
    }
  }

  public static class Pair
  {
    int m_b;
    int m_e;

    Pair(int b, int e)
    {
      m_b = b;
      m_e = e;
    }

    public int length()
    {
      return m_e - m_b;
    }

    public boolean empty()
    {
      return m_b == m_e;
    }
  }

  public static class IntString implements Comparable
  {
    int m_b;
    int m_e;
    int[] m_values;

    IntString(int[] values)
    {
      m_b = 0;
      m_e = values.length;
      m_values = values;
    }

    IntString(int i, int j, int[] values)
    {
      m_b = i;
      m_e = j;
      m_values = values;
    }

    public int intAt(int b)
    {
      return m_values[b];
    }

    public int length()
    {
      return m_values.length;
    }

    public IntString substring(int b, int e)
    {
      int sz = e - b;
      int[] v = new int[sz];
      for (int i = b; i < e; i++)
        v[i - b] = m_values[i];
      return new IntString(m_b + b, m_b + e, v);
    }

    private boolean match(int pos, IntString o)
    {
      if (o == null) return false;
      for (int i = 0; i < o.m_values.length; i++)
      {
        if ((pos + i) >= m_values.length) 
          return false;
        if (m_values[pos + i] != o.m_values[i])
          return false;
      }
      return true;
    }

    public IntString remove(IntString s)
    {
      int i = 0;
      int len = 0;
      int p = 0;
      while (i < m_values.length)
      {
        if (match(i, s))
        {
          if (p > 0)
          {
            for (int j = 0; j < s.length(); j++)
            {
              m_values[i + j] = -1;
              len++;
            }
          }
          p++;
          i += s.length();
        }
        else
          i++;
      }
      int[] nvals = new int[m_values.length - len];
      i = 0;
      int pos = 0;
      for (i = 0; i < m_values.length; i++)
      {
        if (m_values[i] >= 0)
          nvals[pos++] = m_values[i];
      }
      return new IntString(nvals);
    }

    public int hashCode()
    {
      return Arrays.hashCode(m_values);
    }

    public boolean equals(Object o)
    {
      return Arrays.equals(m_values, ((IntString) o).m_values);
    }

    public int compareTo(Object o)
    {
      int[] ovals = ((IntString) o).m_values;
      int sz = 
        (m_values.length > ovals.length) ? m_values.length : ovals.length;
      for (int i = 0; i < sz; i++)
      {
        int a = (i >= m_values.length) ? -1 : m_values[i];
        int b = (i >= ovals.length) ? -1 : ovals[i];
        if (a < b)
          return -1;
        if (a > b)
          return 1;
      }
      return 0;
    }

    public String toString()
    {
      StringBuffer b = new StringBuffer();
      for (int i = 0; i < m_values.length; i++)
      {
        if (i > 0) b.append(",");
        b.append(m_values[i]);
      }
      return b.toString();
    }
  }

  // return the longest common prefix of s and t
  public static Pair lcp(IntString s, IntString t)
  {
    int n = Math.min(s.length(), t.length());
    int j = n;
    for (int i = 0; i < n; i++)
    {
      if (s.intAt(i) != t.intAt(i))
      {
        j = i;
        break;
      }
    }
    if (j <= 1) return null;
    // ignore the pattern of same chars.
    boolean same = true;
    int old = s.intAt(0);
    for (int i = 1; i < j; i++)
    {
      if (s.intAt(i) != old)
      {
        same = false;
        break;
      }
      old = s.intAt(i);
    }
    if (same) return null;
    return new Pair(s.m_b, s.m_b + j);
  }

  public static Pair lrs(IntString s)
  {

    // form the N suffixes
    int N = s.length();
    IntString[] suffixes = new IntString[N];
    for (int i = 0; i < N; i++)
      suffixes[i] = s.substring(i, N);

    // sort them
    Arrays.sort(suffixes);

    // find a repeating pair
    Pair lrs = null;
    for (int i = 0; i < N - 1; i++)
    {
      //System.out.println(suffixes[i].m_b + ":" + suffixes[i].toString());
      Pair x = lcp(suffixes[i], suffixes[i + 1]);
      if (x != null && x.length() == 2)
      {
        lrs = x;
        break;
      }
    }
    return lrs;
  }
  
  private static class IgnoreList
  {
    private LinkedList<LinkedList<String>> m_list;
    
    public IgnoreList()
    {
      m_list = new LinkedList<LinkedList<String>>();
      String filename = System.getenv("ADE_VIEW_ROOT");
      filename += "/cep/wlevs_cql/modules/cqlengine/utl/debuglogger_ignore.txt";
      try 
      {
        BufferedReader in = new BufferedReader(new FileReader(filename));
        String str;
        LinkedList<String> l = new LinkedList<String>();
        while((str = in.readLine()) != null)
        {
          if (str.startsWith("#")) continue;
          if (str.length() == 0) 
          {
            if (l.size() > 0)
              m_list.add(l);
            l = new LinkedList<String>();
          } else 
          {
            l.add(str);
          }
        }
        if (l.size() > 0)
          m_list.add(l);
        in.close();
      } catch(IOException e)
      {
        System.out.println(e);
      }
    }
    
    public boolean isIn(Trace t)
    {
      BreadCrumbs bc = t.getBreadCrumbs();
      for (LinkedList l : m_list)
      {
        if (bc.matches(l)) return true;
      }
      return false;
    }
  }
}


