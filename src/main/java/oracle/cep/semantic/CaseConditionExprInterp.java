/* $Header: pcbpel/cep/server/src/oracle/cep/semantic/CaseConditionExprInterp.java /main/3 2008/09/17 15:19:47 parujain Exp $ */

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
    parujain    11/09/07 - external source
    parujain    10/26/07 - is expr ondemand
    parujain    03/29/07 - Case Condition Expr Interpreter
    parujain    03/29/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/CaseConditionExprInterp.java /main/3 2008/09/17 15:19:47 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPCaseConditionExprNode;

public class CaseConditionExprInterp extends NodeInterpreter {
  
  void interpretNode( CEPParseTreeNode node, SemContext ctx) 
  throws CEPException {
    CaseConditionExpr  conditionExpr;
    String             exprName;
    boolean            isExternal = false;
    
    assert node instanceof CEPCaseConditionExprNode;
    CEPCaseConditionExprNode caseNode = (CEPCaseConditionExprNode)node;
    
    super.interpretNode(node, ctx);
    
    NodeInterpreter interp = InterpreterFactory.getInterpreter(caseNode.getConditionExpr());
    interp.interpretNode(caseNode.getConditionExpr(), ctx);
    isExternal = isExternal || ctx.getExpr().isExternal;
    conditionExpr = new CaseConditionExpr((BExpr)(ctx.getExpr()));
    exprName = new String("(" + "WHEN" + conditionExpr.conditionExpr.getName());
    
    if(caseNode.getReturnExpr() != null)
    {
      interp = InterpreterFactory.getInterpreter(caseNode.getReturnExpr());
      interp.interpretNode(caseNode.getReturnExpr(), ctx);
      isExternal = isExternal || ctx.getExpr().isExternal;
      conditionExpr.setResultExpr(ctx.getExpr());
      exprName = exprName + "(" + "THEN" + conditionExpr.resultExpr.getName() + ")";
    }
    else
    {
      exprName = exprName + "ELSE null" + ")" ;
    }
    
    conditionExpr.setName(exprName, false, isExternal);
    ctx.setExpr(conditionExpr);
    
  }
  
}
