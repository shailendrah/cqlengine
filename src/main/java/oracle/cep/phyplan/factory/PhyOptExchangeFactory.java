/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyOptExchangeFactory.java /main/3 2014/10/14 06:35:34 udeshmuk Exp $ */

/* Copyright (c) 2011, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    09/24/14 - propagate isDependentOnPartnStream to physical
                           operator
    anasrini    03/20/11 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyOptExchangeFactory.java /main/3 2014/10/14 06:35:34 udeshmuk Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import oracle.cep.exceptions.CEPException;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptExchange;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptExchange;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.service.ExecContext;

/**
 * PhyOptExchangeFactory
 */
class PhyOptExchangeFactory extends PhyOptFactory {

  PhyOpt newPhyOpt(Object ctx) throws CEPException {

    assert ctx instanceof LogPlanInterpreterFactoryContext;
    LogPlanInterpreterFactoryContext ctx1 = 
      (LogPlanInterpreterFactoryContext) ctx;
    ExecContext ec = ctx1.getExecContext();

    PhyOptExchange phyExchange;
    PhyOpt[]       phyChildren;
    LogOptExchange logExchange;
    LogOpt         logop;

    logop       = ctx1.getLogPlan();
    phyChildren = ctx1.getPhyChildPlans();

    assert phyChildren != null;
    assert logop != null;
    assert logop instanceof LogOptExchange : logop.getClass().getName();
    logExchange = (LogOptExchange)logop;

    List<oracle.cep.logplan.expr.Expr> logExprs =
      logExchange.getPartitioningExprList();
    List<Expr>         phyExprs    = new ArrayList<Expr>(logExprs.size());
    Collection<String> objDDLs     = logExchange.getDDLs();
    List<String>       entityNames = logExchange.getEntityNameList();
    int                dop         = logExchange.getDOP();
    boolean            isDependentOnPartnStream = logExchange.isDependentOnPartnStream();

    if(!isDependentOnPartnStream)
    {
      // Partitioning expressions
      for (oracle.cep.logplan.expr.Expr logExpr : logExprs)
      {
        Expr phyExpr = LogPlanExprFactory.getInterpreter(logExpr,
            new LogPlanExprFactoryContext(logExpr, logop, phyChildren));
        phyExprs.add(phyExpr);
      }
    }
    else
      phyExprs = null;

    phyExchange = new PhyOptExchange(ec, phyChildren, phyExprs, objDDLs, 
                                     entityNames, dop, isDependentOnPartnStream);
    return phyExchange;
  }

}
