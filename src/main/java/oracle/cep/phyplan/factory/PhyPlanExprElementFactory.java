/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyPlanExprElementFactory.java /main/2 2009/04/28 10:24:09 sborah Exp $ */

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
    sborah      04/27/09 - passing phychildren to expr context
    parujain    05/19/08 - evalname
    parujain    04/25/08 - Element Expr Factory
    parujain    04/25/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyPlanExprElementFactory.java /main/2 2009/04/28 10:24:09 sborah Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.factory;

import oracle.cep.logplan.LogOpt;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprElement;

public class PhyPlanExprElementFactory extends PhyPlanExprFactory
{

  @Override
  public Expr newExpr(Object ctx) {
    oracle.cep.logplan.expr.Expr src;
    LogOpt op;
    
    assert ctx instanceof LogPlanExprFactoryContext;
    LogPlanExprFactoryContext lpctx = (LogPlanExprFactoryContext) ctx;
    
    src = lpctx.getLogExpr();
    assert src instanceof oracle.cep.logplan.expr.ExprElement;
    oracle.cep.logplan.expr.ExprElement srcExpr = (oracle.cep.logplan.expr.ExprElement)src;
    
    op = lpctx.getLogPlan();
    
    String name = srcExpr.getElementName();
    
    int numAttrs = srcExpr.getNumAttrs();
    oracle.cep.logplan.expr.Expr[] logAttrs = srcExpr.getAttrExprs();
    Expr[] attrs = new Expr[numAttrs];
    for(int i=0; i<numAttrs; i++)
    {
      attrs[i] = LogPlanExprFactory.getInterpreter(logAttrs[i],
    		         new LogPlanExprFactoryContext(logAttrs[i],
    		             op, lpctx.getPhyChildren(), lpctx.isMakeCorr()));
    }
    
    int numChild = srcExpr.getNumChildren();
    oracle.cep.logplan.expr.Expr[] logChild = srcExpr.getChildExprs();
    Expr[] child = new Expr[numChild];
    for(int j=0; j<numChild; j++)
    {
      child[j] = LogPlanExprFactory.getInterpreter(logChild[j],
    		         new LogPlanExprFactoryContext(logChild[j], op,
    		             lpctx.getPhyChildren(), lpctx.isMakeCorr()));
    }
    
    Expr phyExpr;
    if(name != null)
      phyExpr = new ExprElement(name, attrs, child, src.getType());
    else
    {
       Expr nameExpr = LogPlanExprFactory.getInterpreter(srcExpr.getElementNameExpr(), 
              new LogPlanExprFactoryContext(srcExpr.getElementNameExpr(), op,
                  lpctx.getPhyChildren(), lpctx.isMakeCorr()));
       phyExpr = new ExprElement(nameExpr, attrs, child, src.getType());
    }
    return phyExpr;
  }
	
}