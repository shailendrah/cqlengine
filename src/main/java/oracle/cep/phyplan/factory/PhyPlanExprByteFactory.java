/* $Header: PhyPlanExprByteFactory.java 18-jun-2007.03:51:40 rkomurav Exp $ */

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
 *  @version $Header: PhyPlanExprByteFactory.java 18-jun-2007.03:51:40 rkomurav Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprByte;

/**
 * PhyPlanExprByteFactory
 *
 * @author najain
 */
public class PhyPlanExprByteFactory extends PhyPlanExprFactory {

  public Expr newExpr(Object ctx) {
    oracle.cep.logplan.expr.Expr src;
    
    assert ctx instanceof LogPlanExprFactoryContext;
    LogPlanExprFactoryContext lpctx = (LogPlanExprFactoryContext) ctx;

    src = lpctx.getLogExpr();
    assert src instanceof oracle.cep.logplan.expr.ExprByte;
    oracle.cep.logplan.expr.ExprByte srcExpr = (oracle.cep.logplan.expr.ExprByte) src;
    
    byte[] bValue = srcExpr.getBValue();
    Expr phyExpr = new ExprByte(bValue, src.getType());
    return phyExpr;
  }
}
