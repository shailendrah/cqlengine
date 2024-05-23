/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyPlanExprIntervalFactory.java /main/3 2011/09/05 22:47:27 sbishnoi Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    08/27/11 - adding support for interval year to month
    rkomurav    06/18/07 - cleanup
    parujain    10/09/06 - Interval datatype
    parujain    10/09/06 - Creation
 */

/**
 *  @version $Header: PhyPlanExprIntervalFactory.java 18-jun-2007.09:17:39 rkomurav Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.factory;

import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprInterval;

/**
 * PhyPlanExprIntervalFactory
 *
 * @author parujain
 */
public class PhyPlanExprIntervalFactory extends PhyPlanExprFactory
{
  public Expr newExpr(Object ctx)
  {
    oracle.cep.logplan.expr.Expr src;
    
    assert ctx instanceof LogPlanExprFactoryContext;
    LogPlanExprFactoryContext lpctx = (LogPlanExprFactoryContext) ctx;

    src = lpctx.getLogExpr();
    assert src instanceof oracle.cep.logplan.expr.ExprInterval;
    oracle.cep.logplan.expr.ExprInterval srcExpr = (oracle.cep.logplan.expr.ExprInterval) src;
    
    Expr phyExpr = new ExprInterval(srcExpr.getVValue(), srcExpr.isYearToMonth()
        , srcExpr.getFormat());
    return phyExpr;
  }
}