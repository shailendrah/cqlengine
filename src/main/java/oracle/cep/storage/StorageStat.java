/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/storage/StorageStat.java /main/11 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
   StorageStat is to collect statistics of storage/memmgr performance.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      02/06/08 - fix autofields
    hopark      09/18/07 - move memstat
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
 *  @version $Header: StorageStat.java 06-feb-2008.10:45:16 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.storage;

import java.util.concurrent.atomic.AtomicLong;

import oracle.cep.logging.DumpDesc;

@DumpDesc(autoFields=true)
public class StorageStat
{
  long  m_startTime;
  
  long  m_readTime;
  long  m_readCount;

  long  m_writeTime;
  long  m_writeCount;
  
  long  m_deleteTime;
  long  m_deleteCount;
  private AtomicLong 	m_numReq;
  
  
  public StorageStat()
  {
    m_startTime = System.currentTimeMillis();
    m_readTime = 0;
    m_readCount = 0;

    m_writeCount = 0;
    m_writeTime = 0;
    
    m_deleteTime = 0;
    m_deleteCount = 0;
    m_numReq = new AtomicLong(0);
  }
  
  public long getCacheMisses()
  {
	  return 0;
  }
  
  public void incTotalRequests()
  {
	  m_numReq.addAndGet(1);
  }
  
  public long getTotalRequests()
  {
	  return m_numReq.get();
  }
  
  public synchronized void addRead(long tm)
  {
    m_readCount++;
    m_readTime += tm;
  }
  
  public synchronized void addWrite(long tm)
  {
    m_writeCount++;
    m_writeTime += tm;
  }
  
  public synchronized void addDelete(long tm)
  {
    m_deleteCount++;
    m_deleteTime += tm;
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
    long sttime = m_readTime + m_deleteTime;
    long curTime = System.currentTimeMillis();
    long tdiff = curTime - m_startTime;
    StringBuffer buff = new StringBuffer();
    buff.append("RunTime: total=");
    buff.append(timeStr(tdiff));
    buff.append(" , memmgr(evict+read)=");
    appendTime(buff, sttime, tdiff);
    buff.append("\n");

    buff.append("read=");
    buff.append(m_readCount);
    appendTime(buff, m_readTime, tdiff);
    buff.append(" , write=");
    buff.append(m_writeCount);
    appendTime(buff, m_writeTime, tdiff);
    buff.append(" , delete=");
    buff.append(m_deleteCount);
    appendTime(buff, m_deleteTime, tdiff);
    buff.append("\n");

    return buff.toString();           
  }
}
