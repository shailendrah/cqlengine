/* $Header: PhyPlanExprBooleanFactory.java 14-jan-2008.14:04:41 mthatte Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    mthatte     01/14/08 - 
    najain      01/02/08 - Creation
 */

/**
 *  @version $Header: PhyPlanExprBooleanFactory.java 14-jan-2008.14:04:41 mthatte Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.factory;

import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprBoolean;

/**
 * PhyPlanExprBooleanFactory
 *
 * @author najain
 */
public class PhyPlanExprBooleanFactory extends PhyPlanExprFactory
{
  public Expr newExpr(Object ctx)
  {
    oracle.cep.logplan.expr.Expr src;
    
    assert ctx instanceof LogPlanExprFactoryContext;
    LogPlanExprFactoryContext lpctx = (LogPlanExprFactoryContext) ctx;

    src = lpctx.getLogExpr();
    assert src instanceof oracle.cep.logplan.expr.ExprBoolean;
    oracle.cep.logplan.expr.ExprBoolean srcExpr = (oracle.cep.logplan.expr.ExprBoolean) src;
    
    Expr phyExpr = new ExprBoolean(srcExpr.getBValue());
    return phyExpr;
  }

}

