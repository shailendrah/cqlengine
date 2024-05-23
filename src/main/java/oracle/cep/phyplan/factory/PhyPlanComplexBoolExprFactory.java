/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyPlanComplexBoolExprFactory.java /main/4 2009/04/28 10:24:09 sborah Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
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
    rkomurav    06/14/07 - cleanup
    parujain    10/31/06 - Complex Boolean Expr Factory
    parujain    10/31/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyPlanComplexBoolExprFactory.java /main/4 2009/04/28 10:24:09 sborah Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.factory;

import oracle.cep.logplan.LogOpt;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ComplexBoolExpr;

/**
 * PhyPlanComplexBoolExprFactory
 *
 * @author parujain
 */
public class PhyPlanComplexBoolExprFactory extends PhyPlanExprFactory
{
  public Expr newExpr(Object ctx)
  {
    oracle.cep.logplan.expr.Expr            src;
    oracle.cep.logplan.expr.ComplexBoolExpr srcExpr;
    LogOpt                                  op;
    Expr                                    left;
    Expr                                    right;
    
    assert ctx instanceof LogPlanExprFactoryContext;
    LogPlanExprFactoryContext lpctx = (LogPlanExprFactoryContext) ctx;
    
    src = lpctx.getLogExpr();
    assert src instanceof oracle.cep.logplan.expr.ComplexBoolExpr;
    srcExpr = (oracle.cep.logplan.expr.ComplexBoolExpr) src;

    op    = lpctx.getLogPlan();
    left  = null;
    right = null;
    
    // transform the left and right (arithmetic) expressions
    if(srcExpr.getOper() != null)
    {
      oracle.cep.logplan.expr.Expr srcLeft = srcExpr.getLeft();
      LogPlanExprFactoryContext ctx1 = 
        new LogPlanExprFactoryContext(srcLeft, op, lpctx.getPhyChildren(),
            lpctx.isMakeCorr());
      left = LogPlanExprFactory.getInterpreter(srcLeft, ctx1);

      oracle.cep.logplan.expr.Expr srcRight = srcExpr.getRight();
      if(srcRight == null)
         right = null;
      else
      {
        LogPlanExprFactoryContext ctx2 = 
          new LogPlanExprFactoryContext(srcRight, op, lpctx.getPhyChildren(),
                                        lpctx.isMakeCorr());
        right = LogPlanExprFactory.getInterpreter(srcRight, ctx2);
      }
    }

    Expr phyExpr = new ComplexBoolExpr(srcExpr.getOper(), left, right, src.getType());
    phyExpr.setExternal(srcExpr.isExternal());    
    return phyExpr;
  }
}
