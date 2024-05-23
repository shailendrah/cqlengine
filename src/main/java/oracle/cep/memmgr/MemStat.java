/* $Header: MemStat.java 24-sep-2007.15:53:48 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    MemStat does not have exact statistics, it's more like an approximation.
    It's because there are cases which make the stat deviated from the actual one.
    - removing pinned object (totalPin + 1)
    - removing spilled object due to leaks (totalSpill + 1)
    Ideally, totalObjs = totalPinned + totalSpilled should hold.
    alloc : totalObjs+1
    pin   : totalPinned+1
    unpin : totalPinned-1
    evict : totalSpill+1
    load  : totalSpill-1,
    free  : totalObjs-1

   MODIFIED    (MM/DD/YY)
    hopark      09/18/07 - Creation
 */

/**
 *  @version $Header: MemStat.java 24-sep-2007.15:53:48 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr;

public class MemStat
{
  public long m_totalObjs;       // total objects in use (in memory mode, it's the number of allocation)
  public long m_totalPinnedObjs; // total number of pinned objects in use
  public long m_totalSpilledObjs; // total number of spilled objects in use
  public long m_totalPinHit; // total number of hit pin
  public long m_totalPinAccess; // total number of pin access
  
  public MemStat()
  {
    m_totalObjs = 0;
    m_totalPinnedObjs = 0;
    m_totalSpilledObjs = 0;
    m_totalPinHit = 0;
    m_totalPinAccess = 0;
  }
  
  public long getTotalObjs() {return m_totalObjs;}
  public long getTotalPinnedObjs() {return m_totalPinnedObjs;}
  public long getTotalSpilledObjs() {return m_totalSpilledObjs;}
  public long getTotalPinHit() {return m_totalPinHit;}
  public long getTotalPinAccess() {return m_totalPinAccess;}
  
  public void copy(MemStat o)
  {
    m_totalObjs = o.m_totalObjs;
    m_totalPinnedObjs = o.m_totalPinnedObjs;
    m_totalSpilledObjs = o.m_totalSpilledObjs;
    m_totalPinHit = o.m_totalPinHit;
    m_totalPinAccess = o.m_totalPinAccess;
  }

  public void add(MemStat o)
  {
    m_totalObjs += o.m_totalObjs;
    m_totalPinnedObjs += o.m_totalPinnedObjs;
    m_totalSpilledObjs += o.m_totalSpilledObjs;
    m_totalPinHit += o.m_totalPinHit;
    m_totalPinAccess += o.m_totalPinAccess;
  }
}
