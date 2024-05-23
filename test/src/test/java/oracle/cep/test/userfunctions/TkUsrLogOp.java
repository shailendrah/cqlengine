/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkUsrLogOp.java /main/8 2011/07/20 13:46:36 alealves Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 anasrini    07/01/11 - change in genLogPlan signature
 anasrini    04/25/11 - XbranchMerge anasrini_bug-11905834_ps5 from main
 parujain    01/28/09 - txn support
 hopark      10/09/08 - remove statics
 hopark      10/07/08 - use execContext to remove statics
 parujain    09/24/08 - multiple schema
 hopark      05/13/08 - 
 mthatte     10/16/07 - 
 parujain    04/30/07 - statistics impl
 parujain    02/07/07 - interpretQuery interface modification
 skmishra    12/09/06 - 
 najain      04/06/06 - cleanup
 anasrini    03/23/06 - remove printing sem rep 
 skaluska    03/13/06 - interface changes for table manager
 najain      03/02/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkUsrLogOp.java /main/6 2009/08/31 10:57:18 alealves Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.userfunctions;

import oracle.cep.metadata.TableManager;
import oracle.cep.parser.CEPTableDefnNode;
import oracle.cep.parser.Parser;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.semantic.SemanticInterpreter;
import oracle.cep.semantic.SemQuery;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;
import oracle.cep.transaction.ITransaction;
import oracle.cep.exceptions.CEPException;
import oracle.cep.logplan.LogPlanGen;
import oracle.cep.logplan.LogOpt;
import oracle.cep.common.Constants;

/**
 * Test for parsing, semantic analysis and logical plan generation of a query
 */

public class TkUsrLogOp
{
  private static final String[] setup = {
      "register stream S (c1 integer, c2 float);",
      "register relation R (d1 integer, d2 char(10));" };

  public static void main(String[] args) throws CEPException
  {

    String command = args[0];
    CEPManager cepMgr = CEPManager.getInstance();
    ExecContext ec = cepMgr.getSystemExecContext();
    TableManager tableMgr = ec.getTableMgr();
    Parser parser = new Parser();
    SemanticInterpreter sem = new SemanticInterpreter();
    CEPParseTreeNode parseTree;
    SemQuery semquery;
    CEPTableDefnNode n;

    // Setup streams, relations
    for (int i = 0; i < setup.length; i++) {
      // Parse the command
      parseTree = parser.parseCommand(ec, setup[i]);
      assert (parseTree instanceof CEPTableDefnNode) : parseTree.getClass();
      ITransaction txn = ec.getTransactionMgr().begin();
      ec.setTransaction(txn);
      
      n = (CEPTableDefnNode) parseTree;

      if (n.isStreamDefn())
        tableMgr.registerStream(n, setup[i], Constants.DEFAULT_SCHEMA);
      else
        tableMgr.registerRelation(n, setup[i], Constants.DEFAULT_SCHEMA);
      
      txn.commit(ec);
    }

    // Parse the command
    parseTree = parser.parseCommand(ec, command);
    
    ITransaction txn = ec.getTransactionMgr().begin();
    ec.setTransaction(txn);

    // Peform semantic analysis
    semquery = sem.interpretQuery(ec, null,parseTree);
    
    txn.commit(ec);
    ec.setTransaction(null);
    
    txn = ec.getTransactionMgr().begin();
    ec.setTransaction(txn);

    LogPlanGen logplan = new LogPlanGen();
    LogOpt logOp = logplan.genLogPlan(ec, semquery, null, false);
    txn.commit(ec);
    ec.setTransaction(null);

    // Examine the semantic representation
    StringBuilder sb = new StringBuilder();
    sb.append("<Query>");
    sb.append("<Input query=\"" + command + "\"/>");
    sb.append("<Output>");
    sb.append(semquery.toString());
    sb.append("</Output>");
    sb.append("</Query>");

    // System.out.println(sb.toString());

    // Examine the logical operator tree
    StringBuilder sb2 = new StringBuilder();
    sb2.append("<Query>");
    sb2.append("<Input query=\"" + command + "\"/>");
    sb2.append("<LogOpOutput>");
    sb2.append(logOp.toString());
    sb2.append("</LogOpOutput>");
    sb2.append("</Query>");

    System.out.println(sb2.toString());

  }

}
