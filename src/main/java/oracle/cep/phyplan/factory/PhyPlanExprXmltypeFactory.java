/* $Header: PhyPlanExprXmltypeFactory.java 05-jun-2008.17:45:43 skmishra Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    skmishra    06/05/08 - cleanup
    mthatte     05/20/08 - changing char[] to xmlitem
    mthatte     05/13/08 - Creation
 */

package oracle.cep.phyplan.factory;

import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprXmltype;

import org.w3c.dom.Node;

/**
 *  @version $Header: PhyPlanExprXmltypeFactory.java 05-jun-2008.17:45:43 skmishra Exp $
 *  @author  mthatte
 *  @since   release specific (what release of product did this appear in)
 */

public class PhyPlanExprXmltypeFactory extends PhyPlanExprFactory
{
  public Expr newExpr(Object ctx)
  {
    oracle.cep.logplan.expr.Expr src;
    
    assert ctx instanceof LogPlanExprFactoryContext;
    LogPlanExprFactoryContext lpctx = (LogPlanExprFactoryContext) ctx;

    src = lpctx.getLogExpr();
    assert src instanceof oracle.cep.logplan.expr.ExprXmltype;
    oracle.cep.logplan.expr.ExprXmltype srcExpr = (oracle.cep.logplan.expr.ExprXmltype) src;
    Node value = srcExpr.getValue();
    
    Expr phyExpr = new ExprXmltype(value);
    return phyExpr;
  }
}