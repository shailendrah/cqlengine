/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprSimpleCaseFactory.java /main/3 2009/03/30 14:46:01 parujain Exp $ */

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
    parujain    11/07/07 - add OnDemand
    parujain    04/04/07 - Simple Case Expression Factory
    parujain    04/04/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprSimpleCaseFactory.java /main/3 2009/03/30 14:46:01 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprCaseComparison;
import oracle.cep.logplan.expr.ExprSimpleCase;
import oracle.cep.semantic.SimpleCaseExpr;

public class LogPlanExprSimpleCaseFactory extends LogPlanExprFactory {
   
  @Override
  public Expr newExpr(Object ctx) {
    SimpleCaseExpr semExpr;
    Expr  compExpr = null;
    ExprSimpleCase simpleExpr = null;
    Expr  elseExpr = null;
    SemQueryExprFactoryContext ctx1;
    
    assert ctx instanceof SemQueryExprFactoryContext;
    ctx1    = (SemQueryExprFactoryContext)ctx;
    semExpr = (SimpleCaseExpr)ctx1.getExpr();
    
    compExpr = SemQueryExprFactory.getInterpreter(semExpr.getCompExpr(), 
                  new SemQueryExprFactoryContext(semExpr.getCompExpr(), ctx1.getQuery()));
    int len = semExpr.getNumComparisonExprs();
    ExprCaseComparison[] compExprs = new ExprCaseComparison[len];
    for(int i=0; i<len; i++)
    {
      compExprs[i] = (ExprCaseComparison)SemQueryExprFactory.getInterpreter(semExpr.getComparisonExpr(i),
                          new SemQueryExprFactoryContext(semExpr.getComparisonExpr(i), ctx1.getQuery()));
    }
    if(semExpr.getElseExpr() != null)
    {
      elseExpr = SemQueryExprFactory.getInterpreter(semExpr.getElseExpr(), 
                    new SemQueryExprFactoryContext(semExpr.getElseExpr(), ctx1.getQuery()));
    }
    
    simpleExpr = new ExprSimpleCase(compExpr, compExprs, elseExpr, semExpr.getReturnType());
    simpleExpr.setExternal(semExpr.isExternal());
    return simpleExpr;
  }
}
