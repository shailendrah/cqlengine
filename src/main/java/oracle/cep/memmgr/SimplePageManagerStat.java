/* $Header: SimplePageManagerStat.java 22-apr-2008.11:48:35 parujain Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

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
    parujain    04/22/08 - 
    hopark      09/18/07 - Creation
 */

/**
 *  @version $Header: SimplePageManagerStat.java 22-apr-2008.11:48:35 parujain Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr;

public class SimplePageManagerStat
{
  public long m_tuplesInMem; 
  public long m_tuplesInDisk;
  
  public SimplePageManagerStat()
  {
    m_tuplesInMem = 0;
    m_tuplesInDisk = 0;
  }
  
  public long getTuplesInMem() {return m_tuplesInMem;}
  public long getTuplesInDisk() {return m_tuplesInDisk;}
}
