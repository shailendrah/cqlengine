/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyPlanExprComplexFactory.java /main/4 2009/04/28 10:24:09 sborah Exp $ */

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
 rkomurav    06/18/07 - cleanup
 najain      03/01/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyPlanExprComplexFactory.java /main/4 2009/04/28 10:24:09 sborah Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import oracle.cep.logplan.LogOpt;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprComplex;

/**
 * PhyPlanExprComplexFactory
 *
 * @author najain
 */
public class PhyPlanExprComplexFactory extends PhyPlanExprFactory {

  public Expr newExpr(Object ctx) 
  {
    oracle.cep.logplan.expr.Expr src;
    LogOpt                       op;
    Expr                         left;
    Expr                         right;
    
    assert ctx instanceof LogPlanExprFactoryContext;
    LogPlanExprFactoryContext lpctx = (LogPlanExprFactoryContext) ctx;

    src = lpctx.getLogExpr();
    assert src instanceof oracle.cep.logplan.expr.ExprComplex;
    oracle.cep.logplan.expr.ExprComplex srcExpr =
    (oracle.cep.logplan.expr.ExprComplex) src;
    
    op = lpctx.getLogPlan();
    
    oracle.cep.logplan.expr.Expr srcLeft = srcExpr.getLeft();
    assert (srcLeft != null);
    
    left = LogPlanExprFactory.getInterpreter( srcLeft,
        new LogPlanExprFactoryContext( srcLeft, op, lpctx.getPhyChildren(), 
            lpctx.isMakeCorr()));

    oracle.cep.logplan.expr.Expr srcRight = srcExpr.getRight();
    
    if (srcRight == null)
      right = null;
    else {
      right = LogPlanExprFactory.getInterpreter( srcRight,
          new LogPlanExprFactoryContext( srcRight, op, lpctx.getPhyChildren(),
              lpctx.isMakeCorr()));
    }
    
    Expr phyExpr = new ExprComplex(srcExpr.getOper(), left, right, src.getType());
    phyExpr.setExternal(srcExpr.isExternal());    
    return phyExpr;
  }

}
