/* $Header: EvictStat.java 06-feb-2008.10:46:05 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
   StorageStat is to collect statistics of storage/memmgr performance.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      02/06/08 - fix autofields
    hopark      01/01/08 - support xmllog
    hopark      10/30/07 - remove IQueueElement
    hopark      05/09/07 - show gc collection only in SPILL_DEBUG
    hopark      05/31/07 - fix timestr
    hopark      04/17/07 - fix collectedTuplePts
    najain      03/20/07 - cleanup
    najain      03/14/07 - cleanup
    najain      03/12/07 - bug fix
    najain      03/02/07 - 
    hopark      02/27/07 - Creation
 */

/**
 *  @version $Header: EvictStat.java 06-feb-2008.10:46:05 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.memmgr;

import oracle.cep.dataStructures.internal.IListNode;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.logging.DumpDesc;

@DumpDesc(autoFields=true)
public class EvictStat
{
  @DumpDesc(tag="startTime") long  m_startTime;
  @DumpDesc(tag="evictCount") long  m_evictCount;
  @DumpDesc(tag="evictTime") long  m_evictTime;

  @DumpDesc(tag="evictCount") long  m_gcCount;
  @DumpDesc(tag="gcTime") long  m_gcTime;
  
  @DumpDesc(tag="evictedTuples") long m_evictedTuples;
  @DumpDesc(tag="evictedRefs") long m_evictedRefs;
  @DumpDesc(tag="evictedNodes") long m_evictedNodes;
  @DumpDesc(tag="evictedPages") long m_evictedPages;
  
  @DumpDesc(tag="collectedTuples") long m_gccollectedTuples;
  @DumpDesc(tag="collectedRefs") long m_gccollectedRefs;
  @DumpDesc(tag="collectedNodes") long m_gccollectedNodes;
  @DumpDesc(tag="collectedPages") long m_gccollectedPages;
  
  public EvictStat()
  {
    m_startTime = System.currentTimeMillis();
    m_evictTime = 0;
    m_evictCount = 0;

    m_gcCount = 0;
    m_gcTime = 0;

    resetCollectGc();
  }
  
  public synchronized void addEvict(long tm)
  {
    m_evictCount++;
    m_evictTime += tm;
  }

  public synchronized void addGC(long tm)
  {
    m_gcCount++;
    m_gcTime += tm;
  }

  public synchronized void resetCollectGc()
  {
    m_gccollectedTuples = 0;
    m_gccollectedRefs = 0;
    m_gccollectedNodes = 0;
    m_gccollectedPages = 0;

    m_evictedTuples = 0;
    m_evictedRefs = 0;
    m_evictedNodes = 0;
    m_evictedPages = 0;
}
  
  public synchronized void addCollect(Object obj) 
  {
    if (obj instanceof ITuple)
    {
      m_gccollectedTuples++;
    } else if (obj instanceof ITuplePtr) 
    {
      m_gccollectedRefs++;
    } else if (obj instanceof IListNode) 
    {
      m_gccollectedNodes++;
    } else if (obj instanceof IPage) 
    {
      m_gccollectedPages++;
    } 
  }
  
  public synchronized void addEvict(Object obj) 
  {
    if (obj instanceof ITuple)
    {
      m_evictedTuples++;
    } else if (obj instanceof ITuplePtr) 
    {
      m_evictedRefs++;
    } else if (obj instanceof IListNode) 
    {
      m_evictedNodes++;
    } else if (obj instanceof IPage) 
    {
      m_evictedPages++;
    } 
  }
  
  private String timeStr(long t)
  {
    long s = t/ 1000;
    long m = s / 60;
    long h = m / 60;
    s = s % 60;
    m = m % 60;
    return ((h < 10) ? "0":"") + h + ":" + 
           ((m < 10) ? "0":"") + m + ":" + 
           ((s < 10) ? "0":"") + s;
  }

  private void appendRatio(StringBuffer buff, String name, long val, long total)
  {
     float ratio = ((float) val) / ((float)total)  * 100f;
     buff.append(name);
     buff.append("=");
     buff.append(val);
     buff.append("/");
     buff.append(total);
     buff.append(" ");
     buff.append(ratio);
     buff.append("% ");
  }

  private void appendTime(StringBuffer buff, long sttime, long tdiff)
  {
    buff.append(" ");
    buff.append(timeStr(sttime));
    buff.append(" ");
    float stpercent = ((float) sttime) / ((float) tdiff) * 100f;
    buff.append(stpercent);
    buff.append("% ");
  }
              
  public String toString()
  {
    long sttime = m_evictTime;
    long curTime = System.currentTimeMillis();
    long tdiff = curTime - m_startTime;
    StringBuffer buff = new StringBuffer();
    buff.append("RunTime: total=");
    buff.append(timeStr(tdiff));
    buff.append(" , memmgr(evict+read)=");
    appendTime(buff, sttime, tdiff);
    buff.append("\n");

    buff.append("evictCount=");
    buff.append(m_evictCount);
    appendTime(buff, m_evictTime, tdiff);
    buff.append(" , GC=");
    buff.append(m_gcCount);
    appendTime(buff, m_gcTime, tdiff);
    buff.append("\n");

    buff.append("Eviction: tuples=");
    buff.append( m_evictedTuples);
    buff.append( " , tuplePts=" );
    buff.append( m_evictedRefs );
    buff.append( " , nodes=" );
    buff.append( m_evictedNodes);
    buff.append( " , pages=" );
    buff.append( m_evictedPages);
    buff.append("\n");
    
  //BEGIN SPILL_DEBUG
    buff.append("Collection: tuples=");
    buff.append( m_gccollectedTuples);
    buff.append( " , tuplePts=" );
    buff.append( m_gccollectedRefs );
    buff.append( " , nodes=" );
    buff.append( m_gccollectedNodes);
    buff.append( " , pages=" );
    buff.append( m_gccollectedPages);
//END SPILL_DEBUG
    
    return buff.toString();           
  }
}
