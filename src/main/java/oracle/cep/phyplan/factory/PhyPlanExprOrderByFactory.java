/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyPlanExprOrderByFactory.java /main/3 2009/04/28 10:24:10 sborah Exp $ */

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
    sborah      04/27/09 - passing phychildren to expr context
    parujain    11/07/07 - on demand support
    parujain    06/28/07 - orderby expr factory
    parujain    06/28/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyPlanExprOrderByFactory.java /main/3 2009/04/28 10:24:10 sborah Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import oracle.cep.logplan.LogOpt;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprOrderBy;

public class PhyPlanExprOrderByFactory extends PhyPlanExprFactory {

  @Override
  public Expr newExpr(Object ctx) {
    oracle.cep.logplan.expr.Expr src;
    LogOpt                       op;
    Expr                         phyOrder;
    
    assert ctx instanceof LogPlanExprFactoryContext;
    LogPlanExprFactoryContext lpctx = (LogPlanExprFactoryContext) ctx;

    src = lpctx.getLogExpr();
    assert src instanceof oracle.cep.logplan.expr.ExprOrderBy;
    oracle.cep.logplan.expr.ExprOrderBy srcExpr =
                    (oracle.cep.logplan.expr.ExprOrderBy) src;
    
    op = lpctx.getLogPlan();
    oracle.cep.logplan.expr.Expr srcOrder = srcExpr.getOrderbyExpr();
    assert srcOrder != null;
    
    phyOrder = LogPlanExprFactory.getInterpreter( srcOrder,
        new LogPlanExprFactoryContext(srcOrder, op,lpctx.getPhyChildren(),
                                      lpctx.isMakeCorr()));
    
    Expr phyExpr = new ExprOrderBy(phyOrder, srcExpr.isNullsFirst(), 
                                   srcExpr.isAscending(), src.getType());
    phyExpr.setExternal(srcExpr.isExternal());
    return phyExpr;
  }
  
}
