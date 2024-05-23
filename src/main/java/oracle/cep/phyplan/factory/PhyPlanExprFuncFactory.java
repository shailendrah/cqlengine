/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyPlanExprFuncFactory.java /main/6 2009/09/22 06:58:20 udeshmuk Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Conversion from logical to physical layer representation of function
    expressions

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/09/09 - propagate func and link name.
    sborah      04/27/09 - passing phychildren to expr context
    parujain    11/07/07 - on demand support
    rkomurav    06/18/07 - cleanup
    anasrini    06/19/06 - Creation
    anasrini    06/19/06 - Creation
    anasrini    06/19/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyPlanExprFuncFactory.java /main/6 2009/09/22 06:58:20 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.phyplan.factory;

import oracle.cep.logplan.LogOpt;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprUserDefFunc;

/**
 * PhyPlanExprFuncFactory
 *
 * Conversion from logical to physical layer representation of function
 * expressions
 *
 * @author anasrini
 * @since 1.0
 */
public class PhyPlanExprFuncFactory extends PhyPlanExprFactory
{
  public Expr newExpr(Object ctx)
  {
    oracle.cep.logplan.expr.Expr src;
    LogOpt op;
    
    assert ctx instanceof LogPlanExprFactoryContext;
    LogPlanExprFactoryContext lpctx = (LogPlanExprFactoryContext) ctx;
    
    src = lpctx.getLogExpr();
    assert src instanceof oracle.cep.logplan.expr.ExprUserDefFunc;
    oracle.cep.logplan.expr.ExprUserDefFunc srcExpr = (oracle.cep.logplan.expr.ExprUserDefFunc) src;

    op = lpctx.getLogPlan();
    int numArgs = srcExpr.getNumArgs();

    oracle.cep.logplan.expr.Expr[] logArgs = srcExpr.getArgs();
    Expr[] args = new Expr[numArgs];
    
    for (int i=0; i<numArgs; i++) {
      args[i] = LogPlanExprFactory.getInterpreter(logArgs[i],
        new LogPlanExprFactoryContext(logArgs[i], op, lpctx.getPhyChildren(),
            lpctx.isMakeCorr()));
    }

    Expr phyExpr = new ExprUserDefFunc(srcExpr.getFuncId(), 
                                       args,
                                       src.getType(), 
                                       srcExpr.getFuncImpl(),
                                       srcExpr.getFuncName(), 
                                       srcExpr.getCartridgeLinkName()
                                       );
    phyExpr.setExternal(srcExpr.isExternal());
    return phyExpr;
  }
}


