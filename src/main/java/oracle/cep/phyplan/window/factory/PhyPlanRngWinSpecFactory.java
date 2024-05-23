/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/window/factory/PhyPlanRngWinSpecFactory.java /main/2 2011/05/09 23:12:07 sbishnoi Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    03/16/11 - support for variable duration expression in range
                           window operator
    parujain    03/07/07 - Window Spec Factory
    parujain    03/07/07 - Creation
 */

/**
 *  @version $Header: LogPlanRngWinSpecFactory.java 07-mar-2007.17:14:48 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.window.factory;

import oracle.cep.logplan.LogOptRngWin;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.factory.LogPlanExprFactory;
import oracle.cep.phyplan.factory.LogPlanExprFactoryContext;
import oracle.cep.phyplan.factory.LogPlanInterpreterFactoryContext;
import oracle.cep.phyplan.window.PhyRngWinSpec;
import oracle.cep.phyplan.window.PhyWinSpec;

public class PhyPlanRngWinSpecFactory extends PhyPlanWinSpecFactory {

  @Override
  public PhyWinSpec newWinSpec(Object ctx) {
    assert ctx instanceof LogPlanInterpreterFactoryContext;
    LogPlanInterpreterFactoryContext lctx = (LogPlanInterpreterFactoryContext)ctx;
    
    LogOptRngWin logOpt = (LogOptRngWin)lctx.getLogPlan();
    
    PhyRngWinSpec winspec = new PhyRngWinSpec(logOpt);
    
    if(logOpt.isVariableDurationWindow())
    {
      oracle.cep.logplan.expr.Expr logRangeExpr = logOpt.getRangeExpr();      
      Expr phyRangeExpr = LogPlanExprFactory.getInterpreter(logRangeExpr,
           new LogPlanExprFactoryContext(logRangeExpr, logOpt, 
                                         lctx.getPhyChildPlans(), false));
      
      winspec.setRangeExpr(phyRangeExpr);
      winspec.setRangeUnit(logOpt.getRangeUnit());
    }
    
    return winspec;
  }
  
}
