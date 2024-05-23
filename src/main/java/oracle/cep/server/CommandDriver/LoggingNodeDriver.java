/* $Header: pcbpel/cep/server/src/oracle/cep/server/CommandDriver/LoggingNodeDriver.java /main/10 2009/02/26 21:32:10 hopark Exp $ */

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
    hopark      12/05/08 - use proper LogLevelManager
    hopark      12/02/08 - move LogLevelManager to ExecContext
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    parujain    09/24/08 - multiple schema support
    parujain    09/23/08 - multiple schema support
    parujain    09/18/08 - multiple schema support
    hopark      06/18/08 - logging refactor
    hopark      05/12/08 - use singleton LogLevelManager
    udeshmuk    04/26/08 - parameterize invalid logging type error.
    hopark      01/17/08 - fix query id handling
    hopark      01/16/08 - fix systemstate dump bug
    hopark      01/02/08 - fix clear
    hopark      08/01/07 - add dump
    hopark      06/06/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/server/CommandDriver/LoggingNodeDriver.java /main/10 2009/02/26 21:32:10 hopark Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.server.CommandDriver;

import java.util.ArrayList;
import java.util.List;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.execution.stores.ExecStoreType;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILogArea;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.metadata.Query;
import oracle.cep.metadata.QueryManager;
import oracle.cep.parser.CEPLoggingNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.server.Command;
import oracle.cep.server.ICommandDriver;
import oracle.cep.service.ExecContext;


public class LoggingNodeDriver implements ICommandDriver
{
  public synchronized void execute(ExecContext ec, CEPParseTreeNode node, Command c, String schema) throws CEPException
  {
    CEPLoggingNode n = (CEPLoggingNode)node;
    int cmd = n.getCmd();
    ILogArea area = n.getArea();
    List<Integer> levels = n.getLevels();
    List<Integer> types = n.getTypes();
    List<Integer> ids = n.getIds();
    List<ILogEvent> events = n.getEvents();

    LogLevelManager lm = ec.getLogLevelManager();
    
    if (cmd == CEPLoggingNode.CLEAR)
    {
      lm.clear();
      return;
    }
    
    assert (levels != null);
    
    List<String> names = n.getNames();

    ILogArea chkarea = area;
    if (area == LogArea.QUERY) 
    {
      chkarea = LogArea.OPERATOR;
    }
    // check events
    if (events != null) 
    {
      if (cmd == CEPLoggingNode.DUMP)
      {
         throw new CEPException(SemanticError.LOG_NO_EVENTS_ALLOWED);
      }
      ILogEvent[] eventsForArea = LogEvent.getEvents(chkarea);
      for (ILogEvent ev: events) 
      {
        if (ev == null) 
        {
          throw new CEPException(SemanticError.LOG_UNKNOWN_EVENT, new Object[] {area.toString()});
        }
        boolean has = false;
        for (ILogEvent ll : eventsForArea) 
        {
          if (ll == ev) 
          {
            has = true;
            break;
          }
        }
        if (!has)
          throw new CEPException(SemanticError.LOG_INVALID_EVENT, new Object[] {ev.toString(), area.toString()});
      }
    } 

    // check levels
    if (levels == null) 
    {
      throw new CEPException(SemanticError.LOG_NO_LEVELS_SPECIFIED, new Object[] {area.toString()});
    }
    
    int[] levelsForArea = LogLevel.getLevels(chkarea);
    for (Integer l : levels) 
    {
      boolean has = false;
      if (l == LogLevel.STACKTRACE)
      {
        has = true;
      }
      else
      {
        for (int ll : levelsForArea) 
        {
          if (ll == l) 
          {
            has = true;
            break;
          }
        }
      }
      if (!has)
        throw new CEPException(SemanticError.LOG_INVALID_LEVEL, new Object[] {l, area.toString()});
    }
    if (area == LogArea.SYSTEMSTATE)
    {
        // no types or ids allowed.
        if (types != null && types.size() > 0)
          throw new CEPException(SemanticError.LOG_NO_TYPES_ALLOWED, new Object[] {area.toString()});
        if (ids != null && ids.size() > 0)
          throw new CEPException(SemanticError.LOG_NO_IDS_ALLOWED, new Object[] {area.toString()});
    }
    else if (area == LogArea.QUERY)
    {
        // no types allowed
        // ids should be given
        if (types != null && types.size() > 0)
          throw new CEPException(SemanticError.LOG_NO_TYPES_ALLOWED, new Object[] {area.toString()});
    }
    if (area == LogArea.SYNOPSIS || area == LogArea.STORE)
    {
        // only types for store can be used
        if (types != null) 
        {
          for (Integer type : types) 
          {
            if (type > ExecStoreType.values().length)
              throw new CEPException(SemanticError.INVALID_LOGGING_TYPE, 
                                     new Object[] {area.toString()});
          }
        }
    }
    else if (area == LogArea.INDEX)
    {
        if (types != null && types.size() > 0)
          throw new CEPException(SemanticError.LOG_NO_IDS_ALLOWED, new Object[] {area.toString()});
    }
    if (area == LogArea.QUERY) 
    {
      if (names != null)
      {
        QueryManager qm = ec.getQueryMgr();
        List<Integer> qids = new ArrayList<Integer>(names.size());
        for (String name : names)  
        {
          int qid = -1;
          try {
            qid = qm.findQuery(name, schema);
          }
          catch (CEPException ex)
          {
          }
          if (qid < 0)
          {
            try
            {
              int id = Integer.parseInt(name);
              Query q = qm.getQuery(id);
              qid = q.getId();
            }
            catch(Exception ex)
            {
            }
          }
          if (qid < 0) 
            throw new CEPException(SemanticError.LOG_UNKNOWN_QUERY, new Object[] {name, area.toString()});
          qids.add(qid);
        }
        n.setIds(qids);
        ids = qids;
      }
    }
    
    switch (cmd)
    {
    case CEPLoggingNode.CLEAR:
      lm.clear();
      break;

    case CEPLoggingNode.DUMP:
      lm.dumpLog(area, types, ids, levels);
      break;

    default:
      lm.setLevelsWithTypesIds(cmd == CEPLoggingNode.ENABLE, area, types, ids, events, levels);
      break;
    }
  }
}
