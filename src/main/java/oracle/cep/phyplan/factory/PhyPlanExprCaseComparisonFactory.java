/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyPlanExprCaseComparisonFactory.java /main/4 2009/04/28 10:24:09 sborah Exp $ */

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
    sborah      04/27/09 - passing phychildren to expr context
    parujain    11/07/07 - on demand support
    rkomurav    06/18/07 - cleanup
    parujain    04/04/07 - Comparison Case Expression Factory
    parujain    04/04/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyPlanExprCaseComparisonFactory.java /main/4 2009/04/28 10:24:09 sborah Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import oracle.cep.logplan.LogOpt;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprCaseComparison;

public class PhyPlanExprCaseComparisonFactory extends PhyPlanExprFactory{

  @Override
  public Expr newExpr(Object ctx) {
    oracle.cep.logplan.expr.Expr src;
    LogOpt                       op;
    Expr                         comparisonExpr;
    Expr                         resultExpr;
    
    assert ctx instanceof LogPlanExprFactoryContext;
    LogPlanExprFactoryContext lpctx = (LogPlanExprFactoryContext) ctx;
    
    src = lpctx.getLogExpr();
    assert src instanceof oracle.cep.logplan.expr.ExprCaseComparison;
    oracle.cep.logplan.expr.ExprCaseComparison logExpr =
                  (oracle.cep.logplan.expr.ExprCaseComparison)src;
    
    op  = lpctx.getLogPlan();
 
    comparisonExpr = LogPlanExprFactory.getInterpreter(
        logExpr.getComparisonExpr(), new LogPlanExprFactoryContext(
            logExpr.getComparisonExpr(), op, lpctx.getPhyChildren(), 
            lpctx.isMakeCorr()));

    if(logExpr.getResultExpr() != null)
    {
      resultExpr = LogPlanExprFactory.getInterpreter(logExpr.getResultExpr(),
        new LogPlanExprFactoryContext(logExpr.getResultExpr(), op, 
            lpctx.getPhyChildren(), lpctx.isMakeCorr()));
    }
    else
      resultExpr = null;
    Expr phyExpr = new ExprCaseComparison(comparisonExpr, resultExpr, src.getType());
    phyExpr.setExternal(logExpr.isExternal());    
    return phyExpr;
  }
  
}
