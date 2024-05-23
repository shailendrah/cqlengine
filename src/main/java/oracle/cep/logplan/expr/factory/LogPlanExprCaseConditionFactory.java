/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprCaseConditionFactory.java /main/3 2009/03/30 14:46:01 parujain Exp $ */

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
    parujain    03/29/07 - Condition Case Expression Factory
    parujain    03/29/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprCaseConditionFactory.java /main/3 2009/03/30 14:46:01 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.expr.BoolExpr;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprCaseCondition;
import oracle.cep.semantic.CaseConditionExpr;

public class LogPlanExprCaseConditionFactory extends LogPlanExprFactory {
  
  @Override
  public Expr newExpr(Object ctx) {
    CaseConditionExpr   semExpr;
    ExprCaseCondition   caseExpr = null;
    BoolExpr            condition = null;
    Expr                result = null;
    SemQueryExprFactoryContext ctx1;
    
    assert ctx instanceof SemQueryExprFactoryContext;
    ctx1    = (SemQueryExprFactoryContext)ctx;
    semExpr =  (CaseConditionExpr)ctx1.getExpr();
    
    assert semExpr.getConditionExpr() != null;
    ctx1.expr = semExpr.getConditionExpr();
    condition = (BoolExpr)SemQueryExprFactory.getInterpreter(semExpr.getConditionExpr(),
                        new SemQueryExprFactoryContext(
                            semExpr.getConditionExpr(), ctx1.getQuery()));
    
    if(semExpr.getResultExpr()!= null)
    {
      ctx1.expr = semExpr.getResultExpr();
      result = SemQueryExprFactory.getInterpreter(semExpr.getResultExpr(), 
                         new SemQueryExprFactoryContext(
                             semExpr.getResultExpr(), ctx1.getQuery()));
    }
    
    caseExpr = new ExprCaseCondition(condition, result, semExpr.getReturnType());
    caseExpr.setExternal(semExpr.isExternal());
    return caseExpr;
  }
  
}
