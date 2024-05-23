/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyPlanExprXmlAttrFactory.java /main/2 2009/04/28 10:24:10 sborah Exp $ */

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
    parujain    05/19/08 - evalname
    parujain    04/25/08 - XMLAttr Factory
    parujain    04/25/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyPlanExprXmlAttrFactory.java /main/2 2009/04/28 10:24:10 sborah Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.factory;

import oracle.cep.logplan.LogOpt;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprXmlAttr;

public class PhyPlanExprXmlAttrFactory extends PhyPlanExprFactory
{

  @Override
  public Expr newExpr(Object ctx) {
    oracle.cep.logplan.expr.Expr src;
    LogOpt op;
    
    assert ctx instanceof LogPlanExprFactoryContext;
    LogPlanExprFactoryContext lpctx = (LogPlanExprFactoryContext) ctx;
    
    src = lpctx.getLogExpr();
    assert src instanceof oracle.cep.logplan.expr.ExprXmlAttr;
    oracle.cep.logplan.expr.ExprXmlAttr srcExpr = (oracle.cep.logplan.expr.ExprXmlAttr)src;
    
    op = lpctx.getLogPlan();
    
    String name = srcExpr.getAttrName();
    
    Expr attr = LogPlanExprFactory.getInterpreter(srcExpr.getAttrExpr(),
    		new LogPlanExprFactoryContext(srcExpr.getAttrExpr(), op,
    		    lpctx.getPhyChildren(), lpctx.isMakeCorr()));
    
    Expr phyExpr;
    if(name != null)
      phyExpr = new ExprXmlAttr(name, attr, srcExpr.getType());
    else
    {
      Expr nameExpr = LogPlanExprFactory.getInterpreter(srcExpr.getNameExpr(), 
    	     new LogPlanExprFactoryContext(srcExpr.getNameExpr(), op,
    	         lpctx.getPhyChildren(), lpctx.isMakeCorr()));
      phyExpr = new ExprXmlAttr(nameExpr, attr, srcExpr.getType());
    }
	return phyExpr;
  }
 
}