/* $Header: PhyPlanExprIntFactory.java 18-jun-2007.09:21:36 rkomurav Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 rkomurav    06/18/07 - cleanup
 najain      03/01/06 - Creation
 */

/**
 *  @version $Header: PhyPlanExprIntFactory.java 18-jun-2007.09:21:36 rkomurav Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.factory;

import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprInt;

/**
 * PhyPlanExprIntFactory
 *
 * @author najain
 */
public class PhyPlanExprIntFactory extends PhyPlanExprFactory
{
  public Expr newExpr(Object ctx)
  {
    oracle.cep.logplan.expr.Expr src;
    
    assert ctx instanceof LogPlanExprFactoryContext;
    LogPlanExprFactoryContext lpctx = (LogPlanExprFactoryContext) ctx;

    src = lpctx.getLogExpr();
    assert src instanceof oracle.cep.logplan.expr.ExprInt;
    oracle.cep.logplan.expr.ExprInt srcExpr = (oracle.cep.logplan.expr.ExprInt) src;
    
    Expr phyExpr = new ExprInt(srcExpr.getIValue());
    return phyExpr;
  }

}
