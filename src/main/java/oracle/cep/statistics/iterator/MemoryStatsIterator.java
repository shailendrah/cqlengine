/* $Header: pcbpel/cep/server/src/oracle/cep/statistics/iterator/MemoryStatsIterator.java /main/2 2009/02/06 15:51:04 parujain Exp $ */

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
    parujain    01/29/09 - transaction mgmt
    parujain    12/08/08 - Memory stats iterator
    parujain    12/08/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/statistics/iterator/MemoryStatsIterator.java /main/2 2009/02/06 15:51:04 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.statistics.iterator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import oracle.cep.exceptions.CEPException;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.MemStat;
import oracle.cep.service.ExecContext;
import oracle.cep.statistics.IStats;


public class MemoryStatsIterator extends StatsIterator
{
  // Type definition for MemoryStatsRow
  static final String TUPLE = "Tuple";
  static final String NODE = "Node";

  Map<String, MemStat> m_stats;
  Iterator<String> m_keys;
  FactoryManager  m_factoryMgr;

  static Map<IAllocator.NameSpace, String> s_nsMap;

  static
  {
    s_nsMap = new HashMap<IAllocator.NameSpace, String>();
    s_nsMap.put(IAllocator.NameSpace.TUPLEPTR, TUPLE);
    s_nsMap.put(IAllocator.NameSpace.NODE, NODE);
    s_nsMap.put(IAllocator.NameSpace.NODE2, NODE);
    s_nsMap.put(IAllocator.NameSpace.SNODE, NODE);
    s_nsMap.put(IAllocator.NameSpace.SNODE2, NODE);
    s_nsMap.put(IAllocator.NameSpace.SNODE3, NODE);
    s_nsMap.put(IAllocator.NameSpace.PARTNNODE, NODE);
    s_nsMap.put(IAllocator.NameSpace.QNODE, NODE);
    s_nsMap.put(IAllocator.NameSpace.QNODE1, NODE);
    s_nsMap.put(IAllocator.NameSpace.TTNODE, NODE);
  }

  public MemoryStatsIterator(ExecContext ec)
  {
    super(ec);
    m_factoryMgr = cepMgr.getFactoryManager();
  }

  public void init()
  {
    m_stats = new HashMap<String, MemStat>();
    Iterator<IAllocator> itr = m_factoryMgr.getIterator();
    while (itr.hasNext())
    {
      IAllocator fac = itr.next();
      IAllocator.NameSpace ns = fac.getNameSpace();
      if (ns == null) continue;
      String name = s_nsMap.get(ns);
      if (name == null) continue;
      MemStat r = m_stats.get(name);
      if (r == null)
      {
        r = new MemStat();
        m_stats.put(name, r);
      }
      MemStat stat = fac.getStat();
//      System.out.println(ns + " " + stat.getTotalPinHit() + "/" + stat.getTotalPinAccess());
      r.add(stat);
    }
    m_keys = m_stats.keySet().iterator();
  }

  public IStats getNext() throws CEPException
  {
    while (true)
    {
      if(factory == null)
        return null;

      if (!m_keys.hasNext())
        return null;

      String type = m_keys.next();
      MemStat stat = m_stats.get(type);
      if (stat.getTotalPinAccess() == 0 && stat.getTotalPinHit() == 0)
        continue;

      float pin = (float) stat.getTotalPinAccess();
      float pinhit = (float) stat.getTotalPinHit();
      float hitRatio = pinhit/pin;
      return factory.createMemoryStat(type, hitRatio);
    }
  }


  public void close()
  {
    m_stats.clear();
  }


}

