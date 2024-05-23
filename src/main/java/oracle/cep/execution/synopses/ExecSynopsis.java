/* $Header: pcbpel/cep/server/src/oracle/cep/execution/synopses/ExecSynopsis.java /main/14 2008/12/10 18:55:56 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates.
All rights reserved. */

/*
 DESCRIPTION
 Declares ExecSynopsis in package oracle.cep.execution.synopses.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    hopark    12/02/08 - move LogLevelManager to ExecContext
    hopark    06/18/08 - logging refactor
    hopark    02/07/08 - fix index dump filename
    hopark    12/27/07 - support xmllog
    hopark    11/08/07 - handle exception
    hopark    10/25/07 - make evictable
    hopark    08/03/07 - structured log
    hopark    06/14/07 - add phyid
    hopark    06/08/07 - add getInfo
    hopark    06/01/07 - logging support
    hopark    05/24/07 - logging support
    najain    03/14/07 - cleanup
    hopark    01/06/07 - make getScan abstract
    parujain  01/12/07 - fix bug
    parujain  12/07/06 - propagating relation
    anasrini  03/24/06 - add getId and toString 
    najain    03/10/06 - 
    skaluska  02/15/06 - Creation
    skaluska  02/15/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/synopses/ExecSynopsis.java /main/14 2008/12/10 18:55:56 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */


package oracle.cep.execution.synopses;

import java.util.List;

import oracle.cep.execution.ExecException;
import oracle.cep.execution.indexes.Index;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.stores.ExecStoreIter;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILogLevelManager;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.memmgr.IEvictableObj;
import oracle.cep.service.ExecContext;
import oracle.cep.util.StringUtil;

/**
 * ExecSynopsis is the super class for the various types of synopses
 * used during execution.
 *
 * @author skaluska
 */
public abstract class ExecSynopsis implements ILoggable, IEvictableObj
{
  /** number of Synopses */
  private static int numSynopses = 0;
  
  /** Synopsis id */
  public int id;

  /** 
   * Id of the physical synopsis from which this is instantiated
   * This is very useful for debugging as it provides a starting point
   * to identify this synopsis in the global plan
   */
  protected int                     phyId;  
    
  /** The stubId used to identify the synopsis with the store */
  protected int stubId;
  
  private ExecSynopsisType synopsisType;
  
  protected ExecContext    execContext;
  
  public ExecSynopsis()
  {}
  
  /**
   * Constructor for ExecSynopsis
   */
  public ExecSynopsis(ExecSynopsisType stype, ExecContext ec)
  {
    id = numSynopses++;
    synopsisType = stype;
    execContext = ec;
    LogLevelManager.trace(LogArea.SYNOPSIS, LogEvent.SYNOPSIS_DDL, this, "new");
  }

  /**
   * Get the internal identifer 
   * @return the internal identifer
   */
  public int getId() {
    return id;
  }

  /**
   * @return Returns the physical synopsis Id.
   */
  public int getPhyId()
  {
    return phyId;
  }

  /**
   * Setter for physical synopsis id
   * 
   * @param phyOptId
   *          The id of the corresponding physical synopsis to set.
   */
  public void setPhyId(int phyId)
  {
    this.phyId = phyId;
  }  
  
  /**
   * Getter for stubId 
   * @return Returns the stubId
   */
  public int getStubId()
  {
    return stubId;
  }

  /**
   * Setter for stubId 
   * @param stubId The stubId to set.
   */
  public void setStubId(int stubId)
  {
    this.stubId = stubId;
  }

  public String toString() {
    StringBuffer sb              = new StringBuffer();
    String simpleClassName = StringUtil.getBaseClassName(this);
    sb.append("<" + simpleClassName + " id=\"" + id + "\" stubId=\"" + stubId + "\" />");

    return sb.toString();
  }
 
  //allocate a dummy iterator which will always return getNext as null
  // This is required for any synopsis which does not support Scan
  public TupleIterator getScan(int scanId) throws ExecException
  {
    ExecStoreIter iter = new ExecStoreIter();
    return iter;
  }
  
  public void releaseScan(int scanId, TupleIterator iter) throws ExecException
  {
    // Do nothing
  }
  
  public List<Index> getIndexes() {return null;}
  
  /******************************************************************/
  // ILoggable implementation
  public String getTargetName() 
  {
    return StringUtil.getBaseClassName(this);
  }

  public int getTargetId()
  {
    return phyId;
  }

  public int getTargetType()
  {
    return synopsisType.ordinal();
  }

  public ILogLevelManager getLogLevelManager()
  {
    return execContext.getLogLevelManager();
  }
  
  protected void dumpIndex(IDumpContext dumper) {}
  
  public boolean evict()
    throws ExecException
  {
    return false;
  }
  
  public void trace(IDumpContext dumper, ILogEvent event, int level, Object[] args)
  {
    switch (level)
    {
      case LogLevel.SYNOPSIS_INDEX:
        dumpIndex(dumper);
        break;
    }
  }
}
