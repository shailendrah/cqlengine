/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyPlanExprXmlColAttValFactory.java /main/2 2009/04/28 10:24:10 sborah Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      04/28/09 - passing phychildren to expr context
    parujain    05/29/08 - xmlcolattval support
    parujain    05/29/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyPlanExprXmlColAttValFactory.java /main/2 2009/04/28 10:24:10 sborah Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import oracle.cep.logplan.LogOpt;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprXmlColAttVal;

public class PhyPlanExprXmlColAttValFactory extends PhyPlanExprFactory
{

  @Override
  public Expr newExpr(Object ctx) {
    oracle.cep.logplan.expr.Expr src;
    LogOpt op;
    
    assert ctx instanceof LogPlanExprFactoryContext;
    LogPlanExprFactoryContext lpctx = (LogPlanExprFactoryContext) ctx;
    
    src = lpctx.getLogExpr();
    assert src instanceof oracle.cep.logplan.expr.ExprXmlColAttVal;
    oracle.cep.logplan.expr.ExprXmlColAttVal srcExpr = (oracle.cep.logplan.expr.ExprXmlColAttVal)src;
    
    op = lpctx.getLogPlan();
    
    oracle.cep.logplan.expr.Expr[] logExprs = srcExpr.getColAttExprs();
    int numExprs = srcExpr.getNumColAttExprs();
    
    Expr[] phyExprs = new Expr[numExprs];
    for(int i=0; i<numExprs; i++)
    {
       phyExprs[i] = LogPlanExprFactory.getInterpreter(logExprs[i],
		         new LogPlanExprFactoryContext(logExprs[i], op, 
		             lpctx.getPhyChildren(), lpctx.isMakeCorr()));
    }
    
    Expr phyExpr = new ExprXmlColAttVal(phyExprs, srcExpr.getType());
    return phyExpr;
  }
}
