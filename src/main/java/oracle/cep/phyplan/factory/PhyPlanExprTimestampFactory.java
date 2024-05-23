/* $Header: PhyPlanExprTimestampFactory.java 21-feb-2008.06:11:42 udeshmuk Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    02/21/08 - Creation
 */

/**
 *  @version $Header: PhyPlanExprTimestampFactory.java 21-feb-2008.06:11:42 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.factory;

import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprTimestamp;

public class PhyPlanExprTimestampFactory extends PhyPlanExprFactory
{
  public Expr newExpr(Object ctx)
  {
    oracle.cep.logplan.expr.Expr src;
    
    assert ctx instanceof LogPlanExprFactoryContext;
    LogPlanExprFactoryContext lpctx = (LogPlanExprFactoryContext) ctx;

    src = lpctx.getLogExpr();
    assert src instanceof oracle.cep.logplan.expr.ExprTimestamp;
    oracle.cep.logplan.expr.ExprTimestamp srcExpr = (oracle.cep.logplan.expr.ExprTimestamp) src;
    
    Expr phyExpr = new ExprTimestamp(srcExpr.getTValue());
    return phyExpr;
  }
}
