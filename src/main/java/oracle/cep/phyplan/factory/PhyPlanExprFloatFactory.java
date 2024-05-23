/* $Header: PhyPlanExprFloatFactory.java 18-jun-2007.08:48:11 rkomurav Exp $ */

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
 *  @version $Header: PhyPlanExprFloatFactory.java 18-jun-2007.08:48:11 rkomurav Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprFloat;

/**
 * PhyPlanExprFloatFactory
 *
 * @author najain
 */
public class PhyPlanExprFloatFactory extends PhyPlanExprFactory
{

  public Expr newExpr(Object ctx)
  {
    oracle.cep.logplan.expr.Expr src;
    
    assert ctx instanceof LogPlanExprFactoryContext;
    LogPlanExprFactoryContext lpctx = (LogPlanExprFactoryContext) ctx;

    src = lpctx.getLogExpr();
    assert src instanceof oracle.cep.logplan.expr.ExprFloat;
    oracle.cep.logplan.expr.ExprFloat srcExpr = (oracle.cep.logplan.expr.ExprFloat) src;
    
    Expr phyExpr = new ExprFloat(srcExpr.getFValue());
    return phyExpr;
  }

}
