/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/WindowManager.java /main/21 2012/02/24 11:44:51 alealves Exp $ */

/* Copyright (c) 2007, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*

 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 parujain    09/24/09 - dependency support
 sborah      07/10/09 - support for bigdecimal
 parujain    01/28/09 - transaction mgmt
 hopark      12/02/08 - move LogLevelManager to ExecContext
 hopark      11/04/08 - fix schema
 hopark      10/10/08 - remove statics
 hopark      10/09/08 - remove statics
 hopark      10/07/08 - use execContext to remove statics
 hopark      10/03/08 - fix deadlockexception
 parujain    10/01/08 - drop schema
 parujain    09/12/08 - multiple schema support
 parujain    09/04/08 - maintain offset
 hopark      06/18/08 - logging refactor
 hopark      03/17/08 - config reorg
 sbishnoi    04/02/08 - modifying class.forName to incorporate ClassLoader
 mthatte     02/26/08 - parametrizing metadata errors
 hopark      01/15/08 - metadata logging
 parujain    06/21/07 - release read lock
 parujain    03/26/07 - instantiate in CEPManager
 hopark      03/21/07 - storage re-org
 parujain    03/19/07 - drop window
 parujain    03/05/07 - Window Object Manager
 parujain    03/05/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/WindowManager.java /main/21 2012/02/24 11:44:51 alealves Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata;

import java.util.List;
import java.util.logging.Level;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.extensibility.windows.GenericTimeWindow;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILogLevelManager;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.logging.trace.LogTags;
import oracle.cep.metadata.cache.Cache;
import oracle.cep.metadata.cache.CacheLock;
import oracle.cep.metadata.cache.CacheObjectType;
import oracle.cep.metadata.cache.Descriptor;
import oracle.cep.parser.CEPAttrSpecNode;
import oracle.cep.parser.CEPWindowDefnNode;
import oracle.cep.service.ExecContext;
import oracle.cep.transaction.ITransaction;

@DumpDesc(evPinLevel = LogLevel.MWINDOW_ARG,
          dumpLevel = LogLevel.MWINDOW_INFO, 
          verboseDumpLevel = LogLevel.MWINDOW_LOCKINFO)
public class WindowManager extends CacheObjectManager implements ILoggable
{
  ExecContext execContext;
  
  public WindowManager(ExecContext ec, Cache cache)
  {
    super(ec.getServiceManager(), cache);
    execContext = ec;
  }

  public void init(ConfigManager cfg)
  {

  }

  public int registerWindow(CEPWindowDefnNode wdn, String schema) throws MetadataException
  {
    ITransaction txn = execContext.getTransaction();

    //Check if any other window with the same name exists
    CacheLock l = null;
    int winId;

    l = createObject(txn, wdn.getName(), schema, CacheObjectType.WINDOW, null);

    if (l == null)
      throw new MetadataException(MetadataError.WINDOW_ALREADY_EXISTS,
          wdn.getStartOffset(), wdn.getEndOffset(),
          new Object[]
          { wdn.getName() });

    LogLevelManager.trace(LogArea.METADATA_WINDOW, LogEvent.MWINDOW_CREATE, this, wdn
        .getName());
    Window window = (Window) l.getObj();
    winId = window.getId();

    // parameters
    CEPAttrSpecNode[] specs = wdn.getParamSpecList();
    int numParams = wdn.getNumParams();
    CEPAttrSpecNode attrSpec;

    for (int i = 0; i < numParams; i++)
    {
      attrSpec = specs[i];
      try{
        window.addParam(new Attribute(attrSpec.getName(), attrSpec
            .getAttributeMetadata()));
      }catch(MetadataException me)
      {
        me.setStartOffset(attrSpec.getStartOffset());
        me.setEndOffset(attrSpec.getEndOffset());
        throw me;
      }
    }

    window.setImplClassName(wdn.getImplClassName());
 
    return winId;

  }

  // Used by Window interpreter in Semantic layer
  // So search first in the same schema and then the builtin public schema
  public Window getValidWindow(ExecContext ec, String winName,String schema, 
                               Datatype[] types)
      throws MetadataException
  {
    CacheLock l = null;
    Window window = null;
   
    ITransaction txn = execContext.getTransaction();

    try{
    //   Get cache object
      l = findCache(txn,
          new Descriptor(winName, CacheObjectType.WINDOW, schema, null), false);

      if (l == null)
      { // Look into built-in windows
        l = findCache(txn,
      	      new Descriptor(winName, CacheObjectType.WINDOW, 
    	      		ec.getDefaultSchema(), null), false);
        if(l == null)
          throw new MetadataException(MetadataError.WINDOW_NOT_FOUND, 
                                      new Object[]{winName});
      }

      window = (Window) l.getObj();

      // Number of parameters should be same
      if (window.getNumParams() != types.length)
        throw new MetadataException(MetadataError.VALID_WINDOW_NOT_FOUND, 
                                    new Object[]{winName});

      for (int i = 0; i < types.length; i++)
      {
        if (types[i] != window.getParam(i).getType())
          throw new MetadataException(MetadataError.VALID_WINDOW_NOT_FOUND,
                                      new Object[]{winName});
      }
    }
    finally
    {
      //   Release
      if (l != null)
        release(txn,  l);
    }
    return window;
  }

  public Window getWindow(int id)
      throws MetadataException
  {
    Window window = null;
    CacheLock l = null;
    ITransaction txn = execContext.getTransaction();
    try
    {
      // Get name
      l = findCache(txn, id, false, CacheObjectType.WINDOW);
      if (l == null)
      {
        throw new MetadataException(MetadataError.INVALID_WINDOW_IDENTIFIER, new Object[]{"id =" + id});
      }
      window = (Window) l.getObj();
    } finally
    {
      // Release
      if (l != null)
        release(txn, l);
    }
    return window;
  }

  public GenericTimeWindow getWindowInstance(int id) throws MetadataException
  {
    
    GenericTimeWindow gtw = null;
    ITransaction txn = execContext.getTransaction();
    CacheLock l = findCache(txn, id, false, CacheObjectType.WINDOW);
    Window window = null;
    try
    {
      if (l == null)
        throw new MetadataException(MetadataError.INVALID_WINDOW_IDENTIFIER, new Object[]{"id=" + id});

      window = (Window) l.getObj();

      //   Create the implementation class execution object
      Class<?> cf;
      
      try {
        // First try with own class-loader, as it could be a built-in class.
        // If it fails, then try the Thread's CCL.
        cf = Class.forName(window.getImplClassName());
      } catch (ClassNotFoundException cnf)
      {
        try {
          cf = Class.forName(window.getImplClassName(), true, 
              Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException cnf2) {
          throw new MetadataException(MetadataError.WINDOW_IMPL_CLASS_NOT_FOUND,new Object[]{window.getImplClassName()});
        }
      }

      gtw = (GenericTimeWindow) cf.newInstance();
    } catch (Exception e)
    {
      throw new MetadataException(MetadataError.INVALID_IMPL_CLASS_FOR_WINDOWS, new Object[]{window.getImplClassName()});
    } finally
    {
      // Release
      if (l != null)
        release(txn, l);
    }

    return gtw;
  }

  public Window getWindow(String name, String schema)
  {
    CacheLock l = null;
    Window window = null;
    ITransaction txn = execContext.getTransaction();
    
    // Get cache object
    l = findCache(txn, new Descriptor(name, CacheObjectType.WINDOW,
    		              schema, null), false);
    if (l == null)
    {
      return null;
    }

    window = (Window) l.getObj();

    // Release
    if (l != null)
      release(txn, l);
    return window;
  }

  public void dropWindow(String winName, String schema) throws MetadataException
  {
	ITransaction txn = execContext.getTransaction();
    Locks locks = null;
    CacheLock l = null;
  
    Window window = getWindow(winName, schema);

    if (window == null)
      throw new MetadataException(MetadataError.WINDOW_NOT_FOUND,
           new Object[]{winName});
  //  Integer[] destQueries = execContext.getDependencyMgr().
    //                                  getDependents(window.getId(), 
      //                                              DependencyType.QUERY);
   // if ((destQueries != null) && (destQueries.length > 0))
    if(execContext.getDependencyMgr().areDependentsPresent(window.getId()))
      throw new MetadataException(
          MetadataError.CANNOT_DROP_WINDOW_QUERY_EXISTS,
          new Object[]{winName});

    locks = deleteCache(txn, window.getId());
    if (locks == null)
      throw new MetadataException(MetadataError.INVALID_WINDOW_IDENTIFIER,
      		new Object[]{winName});

    LogLevelManager.trace(LogArea.METADATA_WINDOW, LogEvent.MWINDOW_DELETE, this,
          winName);
    l = locks.objLock;
      
    window = null;
    window = (Window) l.getObj();
 
  }

  public int getTargetId()
  {
    return 0;
  }

  public String getTargetName()
  {
    return "WindowManager";
  }

  public int getTargetType()
  {
    return 0;
  }

  public ILogLevelManager getLogLevelManager()
  {
    return execContext.getLogLevelManager();
  }
    
  public void trace(IDumpContext dumper, ILogEvent event, int level,
      Object[] args)
  {
    // All levels are handled by the default implementation.
    // MWINDOW_INFO - dumps using the fields specified in DumpDesc annotation
    // MWINDOW_LOCKINFO - handled by overriden dump method in this class
  }

  public void dump(IDumpContext dumper)
  {
    super.dump(dumper, LogTags.TAG_WINDOWS, CacheObjectType.WINDOW);
  }

}
