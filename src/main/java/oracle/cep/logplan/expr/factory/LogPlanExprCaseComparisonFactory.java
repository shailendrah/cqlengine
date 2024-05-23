/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprCaseComparisonFactory.java /main/3 2009/03/30 14:46:01 parujain Exp $ */

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
    parujain    11/07/07 - isOnDemand support
    parujain    04/04/07 - Comparison Expression Factory
    parujain    04/04/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprCaseComparisonFactory.java /main/3 2009/03/30 14:46:01 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprCaseComparison;
import oracle.cep.semantic.CaseComparisonExpr;

public class LogPlanExprCaseComparisonFactory extends LogPlanExprFactory {
  
  @Override
  public Expr newExpr(Object ctx) {
    CaseComparisonExpr semExpr;
    ExprCaseComparison  caseExpr = null;
    Expr                comparison = null;
    Expr                result = null;
    SemQueryExprFactoryContext ctx1;

    assert ctx instanceof SemQueryExprFactoryContext;
    ctx1    = (SemQueryExprFactoryContext)ctx;
    semExpr = (CaseComparisonExpr)ctx1.getExpr();
    
    ctx1.expr = semExpr.getComparisonExpr();
    comparison = SemQueryExprFactory.getInterpreter(semExpr.getComparisonExpr(),
                  new SemQueryExprFactoryContext(
                        semExpr.getComparisonExpr(), ctx1.getQuery()));
    
    if(semExpr.getResultExpr() != null)
    {
      result = SemQueryExprFactory.getInterpreter(semExpr.getResultExpr(), 
                  new SemQueryExprFactoryContext(
                         semExpr.getResultExpr(), ctx1.getQuery()));
    }
    
    caseExpr = new ExprCaseComparison(comparison, result, semExpr.getReturnType());
    caseExpr.setExternal(semExpr.isExternal());
    return caseExpr;
  }
}
