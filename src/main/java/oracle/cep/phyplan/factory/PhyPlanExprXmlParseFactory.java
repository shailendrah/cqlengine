/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyPlanExprXmlParseFactory.java /main/2 2009/04/28 10:24:10 sborah Exp $ */

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
 skmishra    05/19/08 - Creation
 */

package oracle.cep.phyplan.factory;

import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprXmlParse;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyPlanExprXmlParseFactory.java /main/2 2009/04/28 10:24:10 sborah Exp $
 *  @author  skmishra
 *  @since   release specific (what release of product did this appear in)
 */

public class PhyPlanExprXmlParseFactory extends PhyPlanExprFactory
{

  public PhyPlanExprXmlParseFactory()
  {
    super();
  }

  public Expr newExpr(Object ctx)
  {
    oracle.cep.logplan.expr.Expr srcExpr;

    Expr phyExpr;
    
    assert ctx instanceof LogPlanExprFactoryContext;
    LogPlanExprFactoryContext lpctx = (LogPlanExprFactoryContext) ctx;

    srcExpr = lpctx.getLogExpr();
    assert srcExpr instanceof oracle.cep.logplan.expr.ExprXmlParse;
    oracle.cep.logplan.expr.ExprXmlParse logExpr = (oracle.cep.logplan.expr.ExprXmlParse) srcExpr;

    oracle.cep.logplan.expr.Expr valueExpr = logExpr.getValue();

    phyExpr = LogPlanExprFactory.getInterpreter(valueExpr,
        new LogPlanExprFactoryContext(valueExpr, lpctx.getLogPlan(),
            lpctx.getPhyChildren(), lpctx.isMakeCorr()));
    
    return new ExprXmlParse(phyExpr,logExpr.isWellformed(), logExpr.getKind());
  }

}