/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyPlanExprXQryFuncFactory.java /main/3 2009/04/28 10:24:10 sborah Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
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
    najain      02/08/08 - 
    mthatte     12/26/07 - 
    najain      11/28/07 - 
    anasrini    11/28/07 - 
    najain      10/31/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyPlanExprXQryFuncFactory.java /main/3 2009/04/28 10:24:10 sborah Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.factory;

import oracle.cep.logplan.LogOpt;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprXQryFunc;
import oracle.cep.phyplan.expr.ExprXQryFuncKind;

/**
 * PhyPlanExprXQryFuncFactory
 *
 * Conversion from logical to physical layer representation of function
 * expressions
 *
 * @author anasrini
 * @since 1.0
 */
public class PhyPlanExprXQryFuncFactory extends PhyPlanExprFactory
{
  public Expr newExpr(Object ctx)
  {
    oracle.cep.logplan.expr.Expr src;
    LogOpt op;
    
    assert ctx instanceof LogPlanExprFactoryContext;
    LogPlanExprFactoryContext lpctx = (LogPlanExprFactoryContext) ctx;
    
    src = lpctx.getLogExpr();
    assert src instanceof oracle.cep.logplan.expr.ExprXQryFunc;
    oracle.cep.logplan.expr.ExprXQryFunc srcExpr = (oracle.cep.logplan.expr.ExprXQryFunc) src;

    op = lpctx.getLogPlan();
    int numParams = srcExpr.getNumParams();

    oracle.cep.logplan.expr.Expr[] logParams = srcExpr.getParams();
    Expr[] params = new Expr[numParams];
    
    for (int i=0; i<numParams; i++) {
      params[i] = LogPlanExprFactory.getInterpreter(logParams[i],
        new LogPlanExprFactoryContext(logParams[i], op,
            lpctx.getPhyChildren(), lpctx.isMakeCorr()));
    }

    ExprXQryFuncKind typ = null;
    if (srcExpr.getXmlQuery() == oracle.cep.logplan.expr.ExprXQryFuncKind.LO_EXPR_XQRY)
      typ = ExprXQryFuncKind.PO_EXPR_XQRY;
    else if (srcExpr.getXmlQuery() == oracle.cep.logplan.expr.ExprXQryFuncKind.LO_EXPR_XEXTS)
      typ = ExprXQryFuncKind.PO_EXPR_XEXTS;
    else
    {
      assert srcExpr.getXmlQuery() == oracle.cep.logplan.expr.ExprXQryFuncKind.LO_EXPR_XMLTBL;
      typ = ExprXQryFuncKind.PO_EXPR_XMLTBL;
    }
    
    Expr phyExpr = new ExprXQryFunc(srcExpr.getFuncId(), srcExpr.getXQryStr(),
                       params, srcExpr.getNames(), src.getType(), 
                       srcExpr.getLength(), typ);

    return phyExpr;
  }
}
