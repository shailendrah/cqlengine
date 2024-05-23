/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyPlanExprSearchCaseFactory.java /main/4 2009/04/28 10:24:10 sborah Exp $ */

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
    parujain    03/30/07 - Searched CASE
    parujain    03/30/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyPlanExprSearchCaseFactory.java /main/4 2009/04/28 10:24:10 sborah Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.factory;

import oracle.cep.logplan.LogOpt;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprCaseCondition;
import oracle.cep.phyplan.expr.ExprSearchCase;

public class PhyPlanExprSearchCaseFactory extends PhyPlanExprFactory
{
  @Override
  public Expr newExpr(Object ctx)
  {
    oracle.cep.logplan.expr.Expr              src;
    LogOpt                                    op;
    oracle.cep.logplan.expr.ExprCaseCondition conds[];
    ExprCaseCondition[]                       conditions;
    Expr                                      elseExpr;
    int                                       len;
    
    assert ctx instanceof LogPlanExprFactoryContext;
    LogPlanExprFactoryContext lpctx = (LogPlanExprFactoryContext) ctx;
    
    src = lpctx.getLogExpr();
    assert src instanceof oracle.cep.logplan.expr.ExprSearchCase;
    oracle.cep.logplan.expr.ExprSearchCase logExpr = 
                           (oracle.cep.logplan.expr.ExprSearchCase)src;
    
    op         = lpctx.getLogPlan();
    len        = logExpr.getNumConditions();
    conds      = logExpr.getCaseConditions();
    conditions = new ExprCaseCondition[len];
    
    for(int i=0; i<len; i++)
    {
      conditions[i] = (ExprCaseCondition)LogPlanExprFactory.getInterpreter(
          conds[i], new LogPlanExprFactoryContext(conds[i], op,
                        lpctx.getPhyChildren(), lpctx.isMakeCorr()));
    }
    if(logExpr.getElseExpr() != null)
    {
      elseExpr = LogPlanExprFactory.getInterpreter(logExpr.getElseExpr(),
          new LogPlanExprFactoryContext(logExpr.getElseExpr(), op, 
              lpctx.getPhyChildren(), lpctx.isMakeCorr()));
    }
    else
      elseExpr = null;

    Expr phyExpr = new ExprSearchCase(conditions, elseExpr, src.getType());
    phyExpr.setExternal(logExpr.isExternal());    
    return phyExpr;
  }
  
}
