/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyPlanExprCaseConditionFactory.java /main/4 2009/04/28 10:24:10 sborah Exp $ */

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
    sborah      04/28/09 - passing phychildren to expr context
    parujain    11/07/07 - on demand support
    rkomurav    06/18/07 - cleanup
    parujain    03/30/07 - Case Condition Factory
    parujain    03/30/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyPlanExprCaseConditionFactory.java /main/4 2009/04/28 10:24:10 sborah Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import oracle.cep.logplan.LogOpt;
import oracle.cep.phyplan.expr.BoolExpr;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprCaseCondition;

public class PhyPlanExprCaseConditionFactory extends PhyPlanExprFactory{

  @Override
  public Expr newExpr(Object ctx) {
    oracle.cep.logplan.expr.Expr src;
    LogOpt                       op;
    BoolExpr                     conditionExpr;
    Expr                         resultExpr;
    
    
    assert ctx instanceof LogPlanExprFactoryContext;
    LogPlanExprFactoryContext lpctx = (LogPlanExprFactoryContext) ctx;
    
    src = lpctx.getLogExpr();
    assert src instanceof oracle.cep.logplan.expr.ExprCaseCondition;
    oracle.cep.logplan.expr.ExprCaseCondition logExpr = 
         (oracle.cep.logplan.expr.ExprCaseCondition)src;
    
    op = lpctx.getLogPlan();
    
    conditionExpr = (BoolExpr)LogPlanExprFactory.getInterpreter(
        logExpr.getConditionExpr(), new LogPlanExprFactoryContext(
            logExpr.getConditionExpr(), op,lpctx.getPhyChildren(),
            lpctx.isMakeCorr()));
    if(logExpr.getResultExpr() != null)
    {
      resultExpr = LogPlanExprFactory.getInterpreter(logExpr.getResultExpr(), 
       new LogPlanExprFactoryContext(logExpr.getResultExpr(), op,
           lpctx.getPhyChildren(), lpctx.isMakeCorr()));
    }
    else
      resultExpr = null;
    
    Expr phyExpr = new ExprCaseCondition(conditionExpr, resultExpr, src.getType());
    phyExpr.setExternal(logExpr.isExternal());    
    return phyExpr;
  }
  
}
