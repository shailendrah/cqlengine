/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/server/CommandInterpreter.java /main/36 2011/04/03 09:24:45 anasrini Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares CommandInterpreter in package oracle.cep.server.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 anasrini  03/29/11 - save and reset oldTxn
 sborah    03/15/11 - add view ordering constraint
 sborah    07/18/10 - XbranchMerge sborah_bug-9536720_ps3_11.1.1.4.0 from
                      st_pcbpel_11.1.1.4.0
 sborah    07/17/10 - XbranchMerge sborah_bug-9536720_ps3 from main
 sborah    06/23/10 - add driver for altering external relation
 parujain  05/21/10 - remove drop schema ddl
 sborah    04/05/10 - change logging level
 parujain  11/24/09 - synonym support
 hopark    12/29/09 - add exception info
 parujain  01/28/09 - transaction mgmt
 skmishra  01/23/09 - adding getParser()
 hopark    01/15/09 - add better error reporting
 hopark    10/09/08 - remove statics
 hopark    10/07/08 - use execContext to remove statics
 parujain  10/01/08 - drop schema
 parujain  09/18/08 - multiple schema support
 hopark    07/31/08 - parser error handling
 hopark    03/17/08 - config reorg
 udeshmuk  12/19/07 - add drivers for alter statements related to heartbeat.
 hopark    05/22/07 - logging support
 hopark    05/11/07 - remove System.out.println(use java.util.logging instead)
 parujain  05/02/07 - userdefined functions creation text
 parujain  04/26/07 - Stream/Relation creation text
 hopark    05/14/07 - cleanup
 parujain  03/19/07 - drop window
 parujain  03/07/07 - Extensible Windows
 parujain  02/28/07 - stop query
 parujain  02/09/07 - System DDLS
 parujain  01/31/07 - drop function
 parujain  01/29/07 - fix oc4j startup
 parujain  01/28/07 - fix oc4j startup problem temporarily
 dlenkov   01/16/07 - system ddls
 hopark    12/04/06 - record exception
 dlenkov   12/01/06 - added set silent
 najain    10/24/06 - integrate with mds
 dlenkov   08/18/06 - support for named queries
 anasrini  07/06/06 - support for aggregate functions 
 anasrini  06/09/06 - support for user defined function 
 najain    05/09/06 - Support for Views 
 najain    03/30/06 - minor bug fixes 
 najain    03/16/06 - some future comments 
 skaluska  03/15/06 - Creation
 skaluska  03/15/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/server/CommandInterpreter.java /main/33 2010/07/19 02:36:41 sborah Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.server;

import java.util.LinkedList;
import java.util.List;

import oracle.cep.exceptions.CEPException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.Parser;
import oracle.cep.server.CommandDriver.AddPushSourceDriver;
import oracle.cep.server.CommandDriver.AddTableSourceDriver;
import oracle.cep.server.CommandDriver.AggrFnDefnNodeDriver;
import oracle.cep.server.CommandDriver.ExternalRelationDriver;
import oracle.cep.server.CommandDriver.FunctionDefnNodeDriver;
import oracle.cep.server.CommandDriver.FunctionRefNodeDriver;
import oracle.cep.server.CommandDriver.LoggingNodeDriver;
import oracle.cep.server.CommandDriver.QueryNodeDriver;
import oracle.cep.server.CommandDriver.QueryRefNodeDriver;
import oracle.cep.server.CommandDriver.RelOrStreamRefNodeDriver;
import oracle.cep.server.CommandDriver.RemoveHeartbeatTimeoutDriver;
import oracle.cep.server.CommandDriver.SetHeartbeatTimeoutDriver;
import oracle.cep.server.CommandDriver.SetParallelismDegreeDriver;
import oracle.cep.server.CommandDriver.SetSilentNodeDriver;
import oracle.cep.server.CommandDriver.SynonymDefnNodeDriver;
import oracle.cep.server.CommandDriver.SynonymRefNodeDriver;
import oracle.cep.server.CommandDriver.SystemNodeDriver;
import oracle.cep.server.CommandDriver.SystemRunNodeDriver;
import oracle.cep.server.CommandDriver.TableDefnNodeDriver;
import oracle.cep.server.CommandDriver.TableMonitorNodeDriver;
import oracle.cep.server.CommandDriver.ViewDefnNodeDriver;
import oracle.cep.server.CommandDriver.ViewDropNodeDriver;
import oracle.cep.server.CommandDriver.ViewOrderingConstraintDriver;
import oracle.cep.server.CommandDriver.WindowDefnNodeDriver;
import oracle.cep.server.CommandDriver.WindowRefNodeDriver;
import oracle.cep.service.ExecContext;
import oracle.cep.transaction.ITransaction;
import oracle.cep.util.DebugUtil;

/**
 * The purpose of this component is to interprete CEP commands. Note that this
 * does not include a listener functionality. The various listeners (network,
 * web services) should call the CommandIntepreter on receiving a command that
 * needs to be executed. This also means that all exceptions need to be caught
 * by the CommandInterpreter. A CommandInterpreter is instantiated by the CEP
 * server on startup.
 *
 * @author skaluska
 */
public class CommandInterpreter
{
  /** parser */
  private Parser parser;
  private ExecContext execContext;
  private static final String STATEMENT_ERROR_START_MARKER = ">>";
  private static final String STATEMENT_ERROR_END_MARKER = "<<";
  
  private static class CommandDriverMap
  {
    Class    nodeClass;
    ICommandDriver  driver;

    public CommandDriverMap(String className, ICommandDriver driver)
    {
      try {
        this.nodeClass = Class.forName(className);
        this.driver = driver;
      } catch (ClassNotFoundException e)
      {
        assert false : className + " not found.";
      }
    }
  }

  private List<CommandDriverMap> drivers;

  /**
   * Constructor for CommandInterpreter
   */
  public CommandInterpreter(ExecContext ec)
  {
    this.execContext = ec;
    parser = new Parser();
    drivers = new LinkedList<CommandDriverMap>();
    
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPSystemNode", new SystemNodeDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPQueryRefNode", new QueryRefNodeDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPQueryNode", new QueryNodeDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPTableDefnNode", new TableDefnNodeDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPAddTableSourceNode", new AddTableSourceDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPAddPushSourceNode", new AddPushSourceDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPSetHeartbeatTimeoutNode", new SetHeartbeatTimeoutDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPRemoveHeartbeatTimeoutNode", new RemoveHeartbeatTimeoutDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPSetSilentNode", new SetSilentNodeDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPRelOrStreamRefNode", new RelOrStreamRefNodeDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPViewDefnNode", new ViewDefnNodeDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPViewDropNode", new ViewDropNodeDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPAggrFnDefnNode", new AggrFnDefnNodeDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPFunctionDefnNode", new FunctionDefnNodeDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPFunctionRefNode", new FunctionRefNodeDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPSystemRunNode", new SystemRunNodeDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPWindowDefnNode", new WindowDefnNodeDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPWindowRefNode", new WindowRefNodeDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPTableMonitorNode", new TableMonitorNodeDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPLoggingNode", new LoggingNodeDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPSynonymDefnNode", new SynonymDefnNodeDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPSynonymRefNode", new SynonymRefNodeDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPExternalRelationNode", new ExternalRelationDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPViewOrderingConstraintNode", new ViewOrderingConstraintDriver()));
    drivers.add(new CommandDriverMap("oracle.cep.parser.CEPSetParallelismDegreeNode", new SetParallelismDegreeDriver()));
  }

  public void init(ConfigManager config)
  {
  }

  public Parser getParser()
  {
    return parser;
  }
  
  public String getErrMsg(CEPException e, String statement)
  {
      int startOffset = e.getStartOffset();
      int endOffset = e.getEndOffset();
      String markedStatement = statement;
          
      if (statement != null && (startOffset >= 0 && endOffset >= 0) ) 
      {
          int soffset = startOffset;
          int eoffset = endOffset + 1; // point to the character next to the end offset
          
          if (soffset < statement.length()) 
          {
              markedStatement = 
                  statement.substring(0, soffset) + 
                  STATEMENT_ERROR_START_MARKER + 
                  statement.substring(soffset);
          }
          
          if (eoffset < statement.length()) 
          {
              markedStatement = 
                  markedStatement.substring(0, eoffset + STATEMENT_ERROR_START_MARKER.length()) + 
                  STATEMENT_ERROR_END_MARKER + 
                  markedStatement.substring(eoffset + STATEMENT_ERROR_START_MARKER.length());
          }
      }
      String m = "";
      String action = "";
      try
      {
	      m = e.getMessage();
	      if (m == null) m = "";
	      action = e.getAction();
	      if (action == null) action = "";
      } catch(Throwable ex) {}
      String msg = markedStatement + "\n" + m + ". " + action;
      Throwable c = e.getCause();
      if (c != null)
      {
          msg += "\n"+ c.getMessage();
      }
      return msg;
  }
        
  /**
   * Execute a command
   * 
   * @param c
   *          Command
   */
  public void execute(Command c)
  {
    CEPParseTreeNode parseTree;
    ITransaction txn = null;
    ITransaction oldTxn = null;

    // Optimistic setup
    c.setBSuccess(true);
    c.setErrorMsg(null);

    // Process the command
    try
    {
      // Parse
      parseTree = parser.parseCommand(execContext, c.getCql());
      String schema = execContext.getSchema();
      oldTxn = execContext.getTransaction();
      txn = execContext.getTransactionMgr().begin();
      execContext.setTransaction(txn);

      // look for the execution driver of given parse tree node
      ICommandDriver driver = null;
      for (CommandDriverMap m : drivers)
      {
        if (m.nodeClass.isInstance(parseTree))
        {
          driver = m.driver;
          break;
        }
      }
      if (driver != null)
      {
        driver.execute(execContext, parseTree, c, schema);
      }
      if(!c.isValidate())
        execContext.getTransactionMgr().commit(txn);
      else
        execContext.getTransactionMgr().rollback(txn);    
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      LogUtil.severe(LoggerType.CUSTOMER, c.getCql());
      // In case of parser exception
      if(txn != null)
        execContext.getTransactionMgr().rollback(txn);
      if (e instanceof CEPException)
      {
        String msg = getErrMsg((CEPException) e, c.getCql());
        LogUtil.severe(LoggerType.CUSTOMER, msg);
      } 
      else
      {
        LogUtil.severe(LoggerType.CUSTOMER, c.getCql() + "\n"+ e.toString());
      }
      LogUtil.fine(LoggerType.TRACE, DebugUtil.getStackTrace(e));
      c.setException(e);
      c.setBSuccess(false);
      c.setErrorMsg(e.getMessage());
      return;
    }
    finally
    {
      execContext.setTransaction(oldTxn);
    }
  }

}
