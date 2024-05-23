/* $Header: PhyPlanExprDoubleFactory.java 30-jan-2008.06:23:56 udeshmuk Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    01/30/08 - Creation
 */

/**
 *  @version $Header: PhyPlanExprDoubleFactory.java 30-jan-2008.06:23:56 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprDouble;

/**
 * PhyPlanExprDoubleFactory
 */

public class PhyPlanExprDoubleFactory extends PhyPlanExprFactory
{

  public Expr newExpr(Object ctx)
  {
    oracle.cep.logplan.expr.Expr src;
    
    assert ctx instanceof LogPlanExprFactoryContext;
    LogPlanExprFactoryContext lpctx = (LogPlanExprFactoryContext) ctx;

    src = lpctx.getLogExpr();
    assert src instanceof oracle.cep.logplan.expr.ExprDouble;
    oracle.cep.logplan.expr.ExprDouble srcExpr = (oracle.cep.logplan.expr.ExprDouble) src;
    
    Expr phyExpr = new ExprDouble(srcExpr.getDValue());
    return phyExpr;
  }

}