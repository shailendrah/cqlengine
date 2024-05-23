/* $Header: QueueSourceStat.java 22-apr-2008.11:48:31 parujain Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES

   MODIFIED    (MM/DD/YY)
    parujain    04/22/08 - 
    hopark      09/18/07 - Creation
 */

/**
 *  @version $Header: QueueSourceStat.java 22-apr-2008.11:48:31 parujain Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.interfaces.input;

public class QueueSourceStat
{
  public long m_tuplesInMem; 
  public long m_tuplesInDisk;
  
  public QueueSourceStat()
  {
    m_tuplesInMem = 0;
    m_tuplesInDisk = 0;
  }
  
  public long getTuplesInMem() {return m_tuplesInMem;}
  public long getTuplesInDisk() {return m_tuplesInDisk;}
}
