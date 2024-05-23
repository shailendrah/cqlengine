/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/factory/LogPlanExprFuncFactory.java /main/6 2009/09/22 06:58:20 udeshmuk Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Conversion from semantic to logical layer representation of function
    expressions

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/09/09 - propagate funcname and linkname
    parujain    03/16/09 - stateless factory
    parujain    11/09/07 - external source
    mthatte     10/30/07 - adding onDemand
    rkomurav    05/28/07 - restructure funcAggr
    rkomurav    10/05/06 - expression in aggregations
    anasrini    07/12/06 - support for user defined aggregations 
    anasrini    06/19/06 - support for function expressions 
    anasrini    06/19/06 - support for function expressions 
    anasrini    06/19/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/factory/LogPlanExprFuncFactory.java /main/6 2009/09/22 06:58:20 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprUserDefFunc;
import oracle.cep.semantic.FuncExpr;
import oracle.cep.semantic.ExprType;

/**
 * LogPlanExprFuncFactory
 *
 * Conversion from semantic to logical layer representation of function
 * expressions
 *
 * @author anasrini
 * @since 1.0
 */

class LogPlanExprFuncFactory extends LogPlanExprFactory {

  public Expr newExpr(Object ctx) {

    assert ctx instanceof SemQueryExprFactoryContext;
    SemQueryExprFactoryContext lpctx = (SemQueryExprFactoryContext) ctx;
    oracle.cep.semantic.Expr semExpr = lpctx.getExpr();
    assert semExpr instanceof FuncExpr;
    FuncExpr funcExpr = (FuncExpr)semExpr;

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
    logExpr = new ExprUserDefFunc(funcExpr.getFunctionId(), logArgs,
                                  funcExpr.getReturnType(), 
                                  funcExpr.getFuncImpl(),
                                  funcExpr.getFuncName(),
                                  funcExpr.getCartridgeLinkName()
                                  );
    logExpr.setExternal(semExpr.isExternal());
    return logExpr;
  }
}
