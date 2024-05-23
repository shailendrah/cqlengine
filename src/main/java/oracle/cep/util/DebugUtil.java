/* $Header: pcbpel/cep/server/src/oracle/cep/util/DebugUtil.java /main/19 2009/02/19 16:44:31 hopark Exp $ */

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
    hopark      02/18/09 - add invokeGC
    hopark      12/01/08 - add memory stats
    hopark      11/03/08 - remove DEBUG_DEADLOCK
    hopark      10/03/08 - add DEBUG_DEADLOCK
    hopark      07/10/08 - fix stack trace
    hopark      05/17/08 - fix stacktrace
    hopark      05/15/08 - add stacktrace scope
    hopark      05/16/07 - use getMessage
    hopark      04/25/07 - add isLoggingOn
    hopark      04/17/07 - storage leak debug
    hopark      04/14/07 - add DEBUG_IDs
    najain      03/02/07 - 
    hopark      02/23/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/util/DebugUtil.java /main/19 2009/02/19 16:44:31 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevelManager;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/util/DebugUtil.java /main/19 2009/02/19 16:44:31 hopark Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
public final class DebugUtil
{
  //It is used to debug leaks from the storage.
  public static final boolean DEBUG_STORAGELEAK = false;
  public static final boolean DEBUG_TUPLE_REFCOUNT = false;
  public static final boolean DEBUG_PAGEDLIST_VERIFY = false;
  public static final boolean DEBUG_PAGEDLIST_NODE = false;
  public static final boolean DEBUG_PAGEDTUPLE_SERIALIZATION = false;
  public static final boolean DEBUG_PAGE_APICALLS = false;
  public static final boolean DEBUG_PAGE_ASYNCFREE = false;
  public static final boolean DEBUG_PAGE_FACTORYCHK = false;
  public static final boolean DEBUG_PAGE_SERIALIZATION = false;
  public static final int DEBUGI_PAGEDLIST_NODE_LISTID = 23;
  
  //NOTE.
  //If you put ids of interested object in here, DebugLogger will only log
  //the activities for the intersted object only.
  //It will run faster than logging activities of all objects.
  //Make sure that the ids are sorted.
  //It's because DebugLogger will do binarySearch.
  //public static final long DEBUG_IDs[] = {268499776,269071301,269092784,269199973,269459270};
  public static final long DEBUGI_IDs[] = null;
  
  private static final boolean STACKTRACE_CEPONLY = true;
  private static final int STACKTRACE_AFTERCEP = 3;
  
  public static boolean isLoggingOn()
  {
    String configFile = System.getProperty("java.util.logging.config.file", null);
    return (configFile != null && configFile.length() > 0);
  }
  
  public static boolean isDebugModeOn()
  {
    Class c = DebugUtil.class;
    Field[] fields = c.getDeclaredFields();
    if (fields != null)
    {
      for (Field f : fields)
      {
        //ignore non static field
        if (!Modifier.isStatic(f.getModifiers()))
          continue;
        String fname = f.getName();
        if (fname.startsWith("DEBUG_"))
        {
          try
          {
            f.setAccessible(true);
            Object v = f.get(null);
            if (v != null && v.toString().equals("true"))
              return true;
          }
          catch (IllegalAccessException e)
          {
          }
        }
      }
    }
    return false;
  }

  public static String getStackTrace(StackTraceElement[] stack)
  {
    StringBuilder buf = new StringBuilder();
    int state = 0;
    int ncnt = 0;
    for(int i = 0; i < stack.length; i++)
    {
      StackTraceElement curStack = stack[i];
      String className = curStack.getClassName();
      boolean stop = false;
      buf.append(curStack.toString());
      buf.append("\n");
      if (STACKTRACE_CEPONLY)
      {
        //yyyy 0
        //oracle.cep 1
        //xxx 2
        switch (state)
        {
          case 0:
            if (className.startsWith("oracle.cep"))
            {
              state++;
            }
            break;
          case 1:
            if (!className.startsWith("oracle.cep"))
            {
              state++;
            }
            break;
          case 2:
            ncnt++;
            if (ncnt >= STACKTRACE_AFTERCEP)
            {
              stop = true;
            }
            break;
        }
        if (stop)
        {
          break;
        }
      } 
    }
    return buf.toString();
  }

  public static String getStackTrace(Throwable a)
  {
    return getStackTrace(a.getStackTrace());
  }
  
  public static String getCurrentStackTrace()
  {
    return getStackTrace(Thread.currentThread().getStackTrace());
  }

  public static String getCaller(int endlevel)
  {
    StackTraceElement[] stack;
    try
    {
      throw new Exception();
    } catch (Throwable a)
    {
      stack = a.getStackTrace();
    }
    StringBuffer caller = new StringBuffer();
    if (endlevel <= 0) endlevel = stack.length;
    for(int i = 2; i < endlevel; i++)
    {
      if ( i >= stack.length) break;
      StackTraceElement curStack = stack[i];
      String className = curStack.getClassName();
      String methodName = curStack.getMethodName();
      int line = curStack.getLineNumber();
      if (i > 2) 
      {
        caller.append("->");
      }
      caller.append(className);
      caller.append(".");
      caller.append(methodName);
      caller.append(":");
      caller.append(line);
    }
    return caller.toString();
  }

  public static String dumpGarbageCollectors() 
  {
    StringBuilder sb = new StringBuilder();
    
    List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();
    for (GarbageCollectorMXBean gc : gcs) 
    {
        long timePerGc = 0;
        if (gc.getCollectionCount() > 0) 
        {
            timePerGc = gc.getCollectionTime() / gc.getCollectionCount();
        } 
        sb.append(new Formatter().format("GC: %s(%s)  count=%d  time=%d(%,dms per collection)\n", gc.getName(), gc.isValid() ? "VALID" : "INVALID",
            gc.getCollectionCount(), gc.getCollectionTime(), timePerGc));
        sb.append(new Formatter().format("\tPools: \""));
        for (String s : gc.getMemoryPoolNames()) 
        {
            sb.append(s).append(", ");
        }
        sb.append("\"\n");
    }
    return sb.toString();
  }
    
  public static String dumpMemoryPools() 
  {
    StringBuilder sb = new StringBuilder();
    
    long totalUsed = 0;
    long totalReserved = 0;
    long totalMax = 0;
    long collectUsed = 0;

    List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
    for (MemoryPoolMXBean pool : pools) 
    {
        MemoryUsage usage = pool.getUsage();
        if (pool.getType() != MemoryType.HEAP) 
        {
            continue;
        }
        sb.append(new Formatter().format("\t\"%s\" memory used: %,d  reserved: %,d  max: %,d", pool.getName(), 
            usage.getUsed(), usage.getCommitted(), usage.getMax()));
        
        totalUsed += usage.getUsed();
        totalReserved += usage.getCommitted();
        totalMax += usage.getMax();
        
        MemoryUsage collect = pool.getCollectionUsage();
        if (collect != null) 
        {
            sb.append(new Formatter().format(" collectUsed: %,d", collect.getUsed()));
            if (collect.getUsed() > 0) 
            {
                collectUsed += collect.getUsed();
            } 
            else 
            {
                collectUsed += usage.getUsed();
            }
        } else {
            collectUsed += usage.getUsed();
        }
        sb.append('\n');
    }
    sb.append(new Formatter().format(
        "RuntimeTotal=%,d RuntimeMax=%,d  RuntimeFree=%,d  TotUsed=%,d  TotReserved=%,d  TotMax=%,d  CollectUsed=%,d\n",
        Runtime.getRuntime().totalMemory(), Runtime.getRuntime().maxMemory(), Runtime.getRuntime().freeMemory(), 
        totalUsed, totalReserved, totalMax, collectUsed));
    return sb.toString();
  }
    
  public static String[] getGarbageCollectorNames() 
  {
    List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();
    String[] toRet = new String[gcs.size()];
    int i = 0;
    for (GarbageCollectorMXBean gc : gcs) 
    {
        toRet[i] = gc.getName();
        i++;
    }
    return toRet;
  }

  public static String[] getHeapPoolNames() 
  {
    List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
    ArrayList<String> array = new ArrayList<String>(pools.size());
    
    for (MemoryPoolMXBean pool : pools) 
    {
        if (pool.getType() != MemoryType.HEAP) 
        {
            continue;
        }
        array.add(pool.getName());
    }
    String[] toRet = new String[array.size()];
    return array.toArray(toRet);
  }
 
  public static void invokeGC() 
  {
    System.out.println("invoking GC...");
    Runtime runtime = Runtime.getRuntime();    
    for (int i = 0; (i < 5); ++ i)
    {
      runtime.runFinalization();
      runtime.gc();
      Thread.yield();
    }
    System.out.println("GC completed");
  }

}
