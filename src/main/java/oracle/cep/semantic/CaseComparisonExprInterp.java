/* $Header: pcbpel/cep/server/src/oracle/cep/semantic/CaseComparisonExprInterp.java /main/4 2008/09/17 15:19:47 parujain Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    09/08/08 - support offset
    udeshmuk    02/20/08 - handle nulls.
    parujain    11/09/07 - external source
    parujain    10/26/07 - is expr ondemand
    parujain    04/02/07 - Simple Case Comparison Expression Interpreter
    parujain    04/02/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/CaseComparisonExprInterp.java /main/4 2008/09/17 15:19:47 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPCaseComparisonExprNode;
import oracle.cep.parser.CEPParseTreeNode;

public class CaseComparisonExprInterp extends NodeInterpreter{
 
  void interpretNode( CEPParseTreeNode node, SemContext ctx) 
  throws CEPException {
    CaseComparisonExpr compExpr;
    String             exprName;
    boolean            isExternal = false;
    
    assert node instanceof CEPCaseComparisonExprNode;
    CEPCaseComparisonExprNode compNode = (CEPCaseComparisonExprNode)node;
    
    super.interpretNode(node, ctx);
    
    NodeInterpreter interp = InterpreterFactory.getInterpreter(compNode.getComparisonExpr());
    interp.interpretNode(compNode.getComparisonExpr(), ctx);
    isExternal = isExternal || ctx.getExpr().isExternal;
    compExpr = new CaseComparisonExpr(ctx.getExpr());
    exprName = new String("(" + "WHEN" + compExpr.getComparisonExpr().getName());
    
    if(compNode.getReturnExpr() != null)
    {
      interp = InterpreterFactory.getInterpreter(compNode.getReturnExpr());
      interp.interpretNode(compNode.getReturnExpr(), ctx);
      isExternal = isExternal || ctx.getExpr().isExternal;
      compExpr.setResultExpr(ctx.getExpr());
      exprName = exprName + "(" + "THEN" + compExpr.getResultExpr().getName() + ")";
    }
    else
      exprName = exprName + "(" + "THEN null" +")";
    
    compExpr.setName(exprName, false, isExternal);
    ctx.setExpr(compExpr);
    
  }
}
