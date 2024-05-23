/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/window/factory/PhyPlanRowRangeWinSpecFactory.java /main/2 2011/12/15 01:06:31 sbishnoi Exp $ */

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
    sbishnoi    12/04/11 - support of variable duration partition window
    hopark      10/12/07 - Creation
 */

/**
 *  @version $Header: PhyPlanRowRangeWinSpecFactory.java 12-oct-2007.13:03:55 hopark   Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.window.factory;

import oracle.cep.logplan.LogOptPrtnWin;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.factory.LogPlanExprFactory;
import oracle.cep.phyplan.factory.LogPlanExprFactoryContext;
import oracle.cep.phyplan.factory.LogPlanInterpreterFactoryContext;
import oracle.cep.phyplan.window.PhyRowRangeWinSpec;
import oracle.cep.phyplan.window.PhyWinSpec;

public class PhyPlanRowRangeWinSpecFactory extends PhyPlanWinSpecFactory {

  @Override
  public PhyWinSpec newWinSpec(Object ctx) {
    assert ctx instanceof LogPlanInterpreterFactoryContext;
    LogPlanInterpreterFactoryContext lctx = (LogPlanInterpreterFactoryContext)ctx;
    
    PhyRowRangeWinSpec winspec = new PhyRowRangeWinSpec(lctx.getLogPlan());
    
    LogOptPrtnWin logOpt = (LogOptPrtnWin) lctx.getLogPlan();
    
    if(logOpt.isVariableDurationWindow())
    {
      oracle.cep.logplan.expr.Expr logRangeExpr = logOpt.getRangeExpr();      
      Expr phyRangeExpr = 
        LogPlanExprFactory.getInterpreter(logRangeExpr,
          new LogPlanExprFactoryContext(logRangeExpr, 
                                        logOpt, 
                                        lctx.getPhyChildPlans(), 
                                        false));
      
      winspec.setRangeExpr(phyRangeExpr);
      winspec.setRangeUnit(logOpt.getRangeUnit());
    }
    return winspec;
  }
  
}
