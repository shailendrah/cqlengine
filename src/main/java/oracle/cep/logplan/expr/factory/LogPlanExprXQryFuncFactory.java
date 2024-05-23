/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprXQryFuncFactory.java /main/3 2009/03/30 14:46:01 parujain Exp $ */

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
    parujain    03/16/09 - stateless factory
    najain      02/08/08 - 
    mthatte     12/26/07 - 
    najain      11/28/07 - 
    anasrini    11/28/07 - 
    najain      10/31/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprXQryFuncFactory.java /main/3 2009/03/30 14:46:01 parujain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprXQryFunc;
import oracle.cep.logplan.expr.ExprXQryFuncKind;
import oracle.cep.semantic.XQryFuncExpr;
import oracle.cep.semantic.ExprType;
import oracle.cep.semantic.XQryFuncExprKind;

/**
 * LogPlanExprXQryFuncFactory
 *
 * Conversion from semantic to logical layer representation of xquery 
 * function expressions
 *
 * @author najain
 * @since 1.0
 */

class LogPlanExprXQryFuncFactory extends LogPlanExprFactory {

  public Expr newExpr(Object ctx) {

    assert ctx instanceof SemQueryExprFactoryContext;
    SemQueryExprFactoryContext lpctx = (SemQueryExprFactoryContext) ctx;
    oracle.cep.semantic.Expr semExpr = lpctx.getExpr();
    assert semExpr instanceof XQryFuncExpr;
    XQryFuncExpr funcExpr = (XQryFuncExpr)semExpr;

    // Handle the arguments
    int numArgs = funcExpr.getNumParams();
    oracle.cep.semantic.Expr[] args = funcExpr.getParams();
    Expr[] logArgs = new Expr[numArgs];
    for (int i=0; i<numArgs; i++) {
      logArgs[i] = 
        SemQueryExprFactory.getInterpreter(args[i],
                 new SemQueryExprFactoryContext(args[i], lpctx.getQuery()));
    }

    Expr logExpr;
    ExprType exprType = funcExpr.getExprType();
    assert exprType == ExprType.E_FUNC_EXPR;
    XQryFuncExprKind typ = funcExpr.getXmlQuery();
    ExprXQryFuncKind logTyp = null;
    if (typ == XQryFuncExprKind.EX_EXPR_XQRY)
      logTyp = ExprXQryFuncKind.LO_EXPR_XQRY;
    else if (typ == XQryFuncExprKind.EX_EXPR_XEXTS)
      logTyp = ExprXQryFuncKind.LO_EXPR_XEXTS;
    else
    {
      assert typ == XQryFuncExprKind.EX_EXPR_XMLTBL;
      logTyp = ExprXQryFuncKind.LO_EXPR_XMLTBL;
    }
    
    logExpr = new ExprXQryFunc(funcExpr.getFunctionId(), logArgs,
			       funcExpr.getNames(), funcExpr.getXqryStr(),
			       funcExpr.getReturnType(), funcExpr.getLength(), logTyp);
    return logExpr;
  }
}
