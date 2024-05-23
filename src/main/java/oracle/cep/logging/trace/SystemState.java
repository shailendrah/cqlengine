/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logging/trace/SystemState.java /main/4 2010/01/06 20:33:11 parujain Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    SystemState implements the Facade pattern to provide system state dump

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    12/07/09 - synonym
    hopark      12/02/08 - move LogLevelManager to ExecContext
    hopark      10/10/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    hopark      06/18/08 - logging refactor
    hopark      06/18/08 - logging refactor
    hopark      05/12/08 - use singleton LogLevelManager
    hopark      02/05/08 - remove full systemstate dump
    hopark      01/18/08 - support dump
    hopark      12/27/07 - support xmllog
    hopark      08/03/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logging/trace/SystemState.java /main/4 2010/01/06 20:33:11 parujain Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logging.trace;

import java.util.LinkedList;
import java.util.List;

import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.ILogLevelManager;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.LogUtil;
import oracle.cep.service.ExecContext;

public class SystemState implements ILoggable
{
  ExecContext m_execContext;
  
  public SystemState(ExecContext ec)
  {
    this.m_execContext = ec;
  }
  
  /* (non-Javadoc)
   * @see oracle.cep.logging.ILoggable#getTargetId()
   */
  public int getTargetId()
  {
    return 0;
  }

  /* (non-Javadoc)
   * @see oracle.cep.logging.ILoggable#getTargetName()
   */
  public String getTargetName()
  {
    return "SystemState";
  }

  /* (non-Javadoc)
   * @see oracle.cep.logging.ILoggable#getTargetType()
   */
  public int getTargetType()
  {
    return 0;
  }

  public ILogLevelManager getLogLevelManager() {return m_execContext.getLogLevelManager();}
  
  public synchronized void dump(IDumpContext dump) {}

  /* (non-Javadoc)
   * @see oracle.cep.logging.ILoggable#trace(oracle.cep.logging.LogEvent, oracle.cep.logging.Levels, java.lang.Object[])
   */
  public void trace(IDumpContext dumper, ILogEvent event, int level, Object[] args)
  {
    int slevel = level;
    int elevel = level;
    LogLevelManager lm = m_execContext.getLogLevelManager();
    for (int l = slevel ; l <= elevel; l++)
    {
      List<Integer> levels = new LinkedList<Integer>();
      try 
      {
        switch (l)
        {
          case LogLevel.SYSTEMSTATE_QUERIES:
            //dumpQueries(dumper);
            levels.add(LogLevel.MQUERY_INFO);
            levels.add(LogLevel.MQUERY_LOCKINFO);
            lm.dumpLog(LogArea.METADATA_QUERY, null, null, levels);

            //Do not create logs for SystemState Event/Level. 
            dumper.setVoid();
            break;
            
          case LogLevel.SYSTEMSTATE_TABLES:
            //TableManager tableman = CEPManager.getTableMgr();
            //tableman.dump(dumper);
            levels.add(LogLevel.MTABLE_INFO);
            levels.add(LogLevel.MTABLE_LOCKINFO);
            lm.dumpLog(LogArea.METADATA_TABLE, null, null, levels);

            //Do not create logs for SystemState Event/Level. 
            dumper.setVoid();
            break;
            
          case LogLevel.SYSTEMSTATE_WINDOWS:
            //WindowManager winman = CEPManager.getWindowManager();
            //winman.dump(dumper);
            levels.add(LogLevel.MUSERFUNC_INFO);
            levels.add(LogLevel.MUSERFUNC_LOCKINFO);
            lm.dumpLog(LogArea.METADATA_USERFUNC, null, null, levels);

            //Do not create logs for SystemState Event/Level. 
            dumper.setVoid();
            break;
  
          case LogLevel.SYSTEMSTATE_USERFUNCS:
            //UserFunctionManager ufm = UserFunctionManager.getUserFunctionManager();
            //ufm.dump(dumper);
            levels.add(LogLevel.MUSERFUNC_INFO);
            levels.add(LogLevel.MUSERFUNC_LOCKINFO);
            lm.dumpLog(LogArea.METADATA_USERFUNC, null, null, levels);

            //Do not create logs for SystemState Event/Level. 
            dumper.setVoid();
            break;
  
          case LogLevel.SYSTEMSTATE_VIEWS:
            //ViewManager viewman = ViewManager.getViewManager();
            //viewman.dump(dumper);
            levels.add(LogLevel.MVIEW_INFO);
            levels.add(LogLevel.MVIEW_LOCKINFO);
            lm.dumpLog(LogArea.METADATA_VIEW, null, null, levels);

            //Do not create logs for SystemState Event/Level. 
            dumper.setVoid();
            break;
           
          case LogLevel.SYSTEMSTATE_SYNONYMS:
            levels.add(LogLevel.MSYNONYM_INFO);
            levels.add(LogLevel.MSYNONYM_LOCKINFO);
            lm.dumpLog(LogArea.METADATA_SYNONYM, null, null, levels);

            //Do not create logs for SystemState Event/Level. 
            dumper.setVoid();
            break; 

          case LogLevel.SYSTEMSTATE_OPERATORS:
            levels.add(LogLevel.OPERATOR_DUMP_DETAIL);
            lm.dumpLog(LogArea.OPERATOR, null, null, levels);

            //Do not create logs for SystemState Event/Level. 
            dumper.setVoid();
            break;
            
          case LogLevel.SYSTEMSTATE_OPERATORS_DS:
            levels.add(LogLevel.OPERATOR_DUMP_DETAIL);
            levels.add(LogLevel.OPERATOR_STRUCTURES_MOST);
            lm.dumpLog(LogArea.OPERATOR, null, null, levels);

            //Do not create logs for SystemState Event/Level. 
            dumper.setVoid();
            break;

          case LogLevel.SYSTEMSTATE_LOGLEVELS:
            lm.dump(dumper);
            break;
          
            
        }
      } catch (Throwable e)
      {
        dumper.writeln(LogUtil.DUMP_ERR, e);
      }
    }
  }

}
