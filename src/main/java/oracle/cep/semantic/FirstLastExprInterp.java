/* $Header: pcbpel/cep/server/src/oracle/cep/semantic/FirstLastExprInterp.java /main/5 2009/03/19 20:24:41 parujain Exp $ */

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
    parujain    03/13/09 - stateless interp
    hopark      11/11/08 - use getFuncName instead of AggrFunc.name
    parujain    09/08/08 - support offset
    rkomurav    04/21/08 - restric usage of first and last beyond pattern clause
    udeshmuk    09/21/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/FirstLastExprInterp.java /main/5 2009/03/19 20:24:41 parujain Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.common.AggrFunction;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.parser.CEPAggrExprNode;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPParseTreeNode;

/**
 * The interpreter that is specific to the CEPFirstLastExprNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 
 */

public class FirstLastExprInterp extends AggrExprInterp {

  // NOTE: This class should be stateless in order to run DDLS in parallel 
  // Ref bug.8290135  
  void interpretNode(CEPParseTreeNode node, SemContext ctx)
    throws CEPException {

    CEPExprNode      exprNode;
    NodeInterpreter  exprInterp;
    
    super.interpretNode(node, ctx);
    CEPAggrExprNode aggrNode = (CEPAggrExprNode)node;
    AggrFunction   aggrFn = aggrNode.getAggrFunction();
    
    if(!ctx.isFirstLastAllowed())
    {
      throw new SemanticException(SemanticError.AGGR_FN_NOT_ALLOWED_HERE,
          aggrNode.getStartOffset(), aggrNode.getEndOffset(),
          new Object[]{aggrNode.getAggrFunction().getFuncName()});
    }
    boolean isAggrAllowed = ctx.isAggrAllowed();
    ctx.setIsAggrAllowed(false);
    exprNode   = aggrNode.getExprNode();
    exprInterp = InterpreterFactory.getInterpreter(exprNode);
    exprInterp.interpretNode(exprNode, ctx);
    Expr expr  = ctx.getExpr();
    Datatype paramType  = expr.getReturnType();
    ctx.setIsAggrAllowed(isAggrAllowed);
    String argName = expr.getName();
    
    buildAggrExpr(ctx, expr, paramType, argName, aggrFn);
  }
}
