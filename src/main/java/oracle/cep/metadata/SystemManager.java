/* $Header: pcbpel/cep/server/src/oracle/cep/metadata/SystemManager.java /main/12 2009/02/06 15:51:03 parujain Exp $ */

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
    parujain    01/28/09 - transaction mgmt
    hopark      12/02/08 - move LogLevelManager to ExecContext
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    parujain    09/12/08 - multiple schema support
    hopark      06/18/08 - logging refactor
    hopark      03/17/08 - config reorg
    hopark      02/05/08 - fix dump level
    hopark      01/15/08 - metadata logging
    parujain    06/21/07 - release read lock
    parujain    05/03/07 - system statistics
    hopark      03/21/07 - storage re-org
    parujain    02/08/07 - System Manager
    parujain    02/08/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/metadata/SystemManager.java /main/12 2009/02/06 15:51:03 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata;

import oracle.cep.common.Constants;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILogLevelManager;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.logging.trace.LogTags;
import oracle.cep.metadata.cache.Cache;
import oracle.cep.metadata.cache.CacheLock;
import oracle.cep.metadata.cache.CacheObjectType;
import oracle.cep.metadata.cache.Descriptor;
import oracle.cep.metadata.cache.NameSpace;

import oracle.cep.service.CEPManager;
import oracle.cep.storage.IStorageContext;

@DumpDesc(evPinLevel=LogLevel.MSYSTEM_ARG,
          dumpLevel=LogLevel.MSYSTEM_INFO,
          verboseDumpLevel=LogLevel.MSYSTEM_LOCKINFO)

public class SystemManager extends CacheObjectManager 
implements ILoggable{
  private long startTime;
  private long maxMemory;
  private long totalMemory;
  private CEPManager cepMgr;
  
  public void init(ConfigManager cfg)
  {
  }
  
  public SystemManager(CEPManager cepMgr, Cache cache)
  {
    super(cepMgr, cache);
    this.cepMgr = cepMgr;
    startTime = System.currentTimeMillis();
    maxMemory = Runtime.getRuntime().maxMemory();
    totalMemory = Runtime.getRuntime().totalMemory();
  }
  
  public long getTime()
  {
    return(System.currentTimeMillis() - startTime);
  }
  
  public long getMaxMemory()
  {
    return maxMemory;
  }
  
  public long getTotalMemory()
  {
    return totalMemory;
  }
  
  public long getFreeMemory()
  {
    return Runtime.getRuntime().freeMemory();
  }
  
  public long getUsedMemory()
  {
    return(totalMemory - Runtime.getRuntime().freeMemory());
  }
  
//  public SystemState findSystemState() throws MetadataException
//  {
//    boolean commit = false;
//    IStorageContext context = beginContext();
//    SystemState state = null;
//  
//    try {
//      
//      SystemObject obj = getSystemObject(context);
//      if(obj != null)
//        state = obj.getState();
//      commit = true;
//    } 
//    finally
//    {
//      removeContext(context, commit);
//    }
//    //here null will be returned only when there is no instance of system object present
//    return state;
//  }
//  
//  private SystemObject getSystemObject(IStorageContext context) throws MetadataException
//  {
//    SystemObject obj= null;
//    CacheLock l = null;
//    try {
//      String name = new String("system");
//      l = findCache(context, new Descriptor(name, CacheObjectType.SYSTEM_STATE, 
//    		  Constants.DEFAULT_SCHEMA, null), false);
//      if(l == null)
//        return null;
//      
//      obj = (SystemObject)l.getObj();
//    } finally {
//      if(l != null)
//      {
//        // Release read lock
//        release(context, l);
//      }
//    }
//    return obj;
//  }
//  
//  public SystemObject beginSystemState() throws MetadataException
//  {
//    boolean commit = false;
//    IStorageContext context = beginContext();
//    SystemObject obj = null;
//    CacheLock l = null;
//    try {
//      obj = getSystemObject(context);
//      if(obj != null)
//        throw new MetadataException(MetadataError.SYSTEM_STATE_ALREADY_EXISTS);
//      
//      String name = new String("system");
//      l = createObject(context, name, Constants.DEFAULT_SCHEMA,
//    		            CacheObjectType.SYSTEM_STATE, null);
//      
//      if(l == null)
//        throw new MetadataException(MetadataError.SYSTEM_STATE_ALREADY_EXISTS);
//      
//      LogLevelManager.trace(LogArea.METADATA_SYSTEM, LogEvent.MSYSTEM_CREATE, this, "zero system state");
//      obj = (SystemObject)l.getObj();
//      obj.setState(SystemState.ZERO);
//      
//      if(l != null)
//        commit(context, l);
//      commit = true;
//    } finally
//    {
//      removeContext(context, commit);
//      if(l != null && !commit)
//        rollback(context, l);
//    }
//    return obj;
//  }
//  
//  public void updateSystemState(SystemState state) throws MetadataException
//  {
//    boolean commit = false;
//    IStorageContext context = beginContext();
//    CacheLock l = null;
//    try {
//      String name = new String("system");
//      l = findCache(context, name, Constants.DEFAULT_SCHEMA,
//    		      NameSpace.SYSTEM, true);
//      
//      if(l == null)
//        throw new MetadataException(MetadataError.SYSTEM_STATE_NOT_FOUND);
//      
//      LogLevelManager.trace(LogArea.METADATA_SYSTEM, LogEvent.MSYSTEM_UPDATE, this, state.name());
//      SystemObject obj = (SystemObject)l.getObj();
//      obj.setState(state);
//      
//      if(l != null)
//        commit(context, l);
//      commit = true;
//    } 
//    finally
//    {
//      removeContext(context, commit);
//      if(l != null && !commit)
//        rollback(context, l);
//    }
//  }
  
  public void cleanSystemState()
  {
    cleanRepository();
  }
  
    
//  public void deleteSystemState() throws MetadataException
//  {
//    boolean commit = false;
//    IStorageContext context = beginContext();
//    Locks locks = null;
//    CacheLock cid = null;
//    CacheLock l = null;
//    try {
//      SystemObject obj = getSystemObject(context);
//      
//      if(obj == null)
//        throw new MetadataException(MetadataError.SYSTEM_STATE_NOT_FOUND);
//      
//      int id = obj.getId();
//      
////    Delete the object
//      // l will be null if no object existed
//      locks = deleteCache(context, id);
//      if(locks == null)
//        throw new MetadataException(MetadataError.SYSTEM_STATE_NOT_FOUND);
//      
//      LogLevelManager.trace(LogArea.METADATA_SYSTEM, LogEvent.MSYSTEM_DELETE, this, "ystem state deleted");
//      l = locks.objLock;
//      cid = locks.cidLock;
//      
////    Commit
//      commit(context, cid);
//      commit(context, l);
//      
//      commit = true;
//    } finally
//    {
////    Discard changes
//      if (l != null && !commit)
//        rollback(context, l);
//      removeContext(context, commit);
//    }
//  }

  public int getTargetId() {
    return 0;
  }
  
  public String getTargetName() {
    return "SystemManager";
  }
  
  public int getTargetType() {
    return 0;
  }

  public ILogLevelManager getLogLevelManager()
  {
    return cepMgr.getLogLevelManager();
  }
    
  public void trace(IDumpContext dumper, ILogEvent event, int level, Object[] args)
  {
    // All levels are handled by the default implementation.
    // MSYSTEM_INFO - dumps using the fields specified in DumpDesc annotation
    // MSYSTEM_LOCKINFO - handled by overriden dump method in this class
  }
  
  public void dump(IDumpContext dumper) 
  {
    super.dump(dumper, LogTags.TAG_SYSTEMOBJS, CacheObjectType.SYSTEM_STATE);
  }
  
}
