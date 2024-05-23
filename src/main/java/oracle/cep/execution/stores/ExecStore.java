/* $Header: pcbpel/cep/server/src/oracle/cep/execution/stores/ExecStore.java /main/20 2009/05/12 19:25:47 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares ExecStore in package oracle.cep.execution.stores.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 parujain  05/04/09 - lifecycle mgmt
 parujain  04/21/09 - store stats
 hopark    12/02/08 - move LogLevelManager to ExecContext
 hopark    10/10/08 - remove statics
 hopark    06/18/08 - logging refactor
 najain    04/25/08 - Add setId
 hopark    02/25/08 - fix tuple eviction
 hopark    12/27/07 - support xmllog
 hopark    11/07/07 - handle ExecException from dump
 hopark    10/15/07 - add evict
 hopark    08/03/07 - structured log
 najain    05/24/07 - add getNumElems
 hopark    06/14/07 - add phyid
 hopark    06/08/07 - add getInfo
 hopark    05/24/07 - logging support
 najain    03/14/07 - cleanup
 najain    12/04/06 - stores are not storage allocators
 najain    07/19/06 - ref-count tuples 
 najain    07/10/06 - add getFactory 
 najain    07/03/06 - implement StorageAlloc
 najain    06/27/06 - add removeStub 
 najain    05/08/06 - sharing support 
 najain    05/04/06 - sharing support 
 anasrini  03/24/06 - add getId 
 anasrini  03/12/06 - implement StorageAlloc
 skaluska  02/15/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/stores/ExecStore.java /main/20 2009/05/12 19:25:47 parujain Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.stores;

import oracle.cep.dataStructures.internal.ITuplePtr;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import oracle.cep.execution.ExecException;
import oracle.cep.execution.indexes.Index;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILogLevelManager;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IEvictableObj;
import oracle.cep.service.ExecContext;
import oracle.cep.util.StringUtil;

public abstract class ExecStore implements ILoggable, IEvictableObj
{
  /** number of stores */
  private static AtomicInteger   numStores = new AtomicInteger(0);

  /** id */
  public int             id;

  /** 
   * Id of the physical store from which this is instantiated
   * This is very useful for debugging as it provides a starting point
   * to identify this store in the global plan
   */
  protected int                     phyId;  
  
  /** store type : used by logging */
  private ExecStoreType  storeType;
  
  protected ExecContext  execContext;
  
  /** factory - this indicates the type of Tuples to be used in the store */
  protected IAllocator<ITuplePtr> factory;

  public abstract int addStub() throws ExecException;

  public abstract void removeStub(int stubId) throws ExecException;

  public abstract int getNumElems();

  /** Empty Constructor for HA */
  public ExecStore()
  {}
  
  /**
   * Constructor for ExecStore
   * @param ec TODO
   */
  public ExecStore(ExecContext ec, ExecStoreType stype)
  {
    execContext = ec;
    storeType = stype;
    id =  numStores.getAndIncrement();
    LogLevelManager.trace(LogArea.STORE, LogEvent.STORE_DDL, this, "new");
  }

  /**
   * @return Returns the factory.
   */
  public IAllocator<ITuplePtr> getFactory()
  {
    return factory;
  }

  /**
   * Get the internal identifer
   * 
   * @return the internal identifer
   */
  public int getId()
  {
    return id;
  }
  
  /**
   * @return Returns the physical store Id.
   */
  public int getPhyId()
  {
    return phyId;
  }

  /**
   * Setter for physical store id
   * 
   * @param phyOptId
   *          The id of the corresponding physical store to set.
   */
  public void setPhyId(int phyId)
  {
    this.phyId = phyId;
  }  
  
  public List<Index> getIndexes() {return null;}
  
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    String simpleClassName = StringUtil.getBaseClassName(this);
    sb.append("<" + simpleClassName + " id=\"" + id + "\" />");

    return sb.toString();
  }

  public boolean evict()
    throws ExecException
  {
    if (factory != null && (factory instanceof IEvictableObj))
    {
      return ((IEvictableObj)factory).evict();
    }
    return false;
  }
  
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
    return storeType.ordinal();
  }

  public ILogLevelManager getLogLevelManager()
  {
    return execContext.getLogLevelManager();
  }
    
  public void trace(IDumpContext dumper, ILogEvent event, int level, Object[] args)
  {
  }

}
