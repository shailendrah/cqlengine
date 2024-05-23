/* $Header: PhyPlanExprBigintFactory.java 18-jun-2007.03:35:59 rkomurav Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 rkomurav     06/18/07 - cleanup.
 hopark       10/17/06 - Creation
 */

/**
 *  @version $Header: PhyPlanExprBigintFactory.java 18-jun-2007.03:35:59 rkomurav Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.factory;

import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprBigint;

/**
 * PhyPlanExprBigintFactory
 *
 * @author najain
 */
public class PhyPlanExprBigintFactory extends PhyPlanExprFactory {

  public Expr newExpr(Object ctx) {
    oracle.cep.logplan.expr.Expr src;
    assert ctx instanceof LogPlanExprFactoryContext;
    LogPlanExprFactoryContext lpctx = (LogPlanExprFactoryContext) ctx;

    src = lpctx.getLogExpr();
    
    assert src instanceof oracle.cep.logplan.expr.ExprBigint;
    oracle.cep.logplan.expr.ExprBigint srcExpr = (oracle.cep.logplan.expr.ExprBigint) src;
    
    long lValue = srcExpr.getLValue();
    Expr phyExpr = new ExprBigint(lValue, src.getType());
    return phyExpr;
  }

}
