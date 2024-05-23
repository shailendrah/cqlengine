/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprSearchCaseFactory.java /main/3 2009/03/30 14:46:01 parujain Exp $ */

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
    parujain    03/29/07 - Search Case Expression Factory
    parujain    03/29/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprSearchCaseFactory.java /main/3 2009/03/30 14:46:01 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprCaseCondition;
import oracle.cep.logplan.expr.ExprSearchCase;
import oracle.cep.semantic.SearchedCaseExpr;

public class LogPlanExprSearchCaseFactory extends LogPlanExprFactory {
  
  @Override
  public Expr newExpr(Object ctx) {
    SearchedCaseExpr  semExpr;
    ExprSearchCase    searchExpr = null;
    Expr  elseExpr = null;
    SemQueryExprFactoryContext ctx1;
    
    assert ctx instanceof SemQueryExprFactoryContext;
    ctx1    = (SemQueryExprFactoryContext)ctx;
    semExpr = (SearchedCaseExpr)ctx1.getExpr();
    
    int len = semExpr.getNumConditions();
    ExprCaseCondition[] conds = new ExprCaseCondition[len];
    for(int i=0; i<len; i++)
    {
      conds[i] = (ExprCaseCondition)SemQueryExprFactory.getInterpreter(semExpr.getCondition(i),
                   new SemQueryExprFactoryContext(semExpr.getCondition(i), ctx1.getQuery()));
    }
    if(semExpr.getElseExpr() != null)
    {
      elseExpr = SemQueryExprFactory.getInterpreter(semExpr.getElseExpr(), 
                    new SemQueryExprFactoryContext(semExpr.getElseExpr(), ctx1.getQuery()));
    }
    
    searchExpr = new ExprSearchCase(conds, elseExpr, semExpr.getReturnType());
    searchExpr.setExternal(semExpr.isExternal());
    return searchExpr;
  }
  
}
