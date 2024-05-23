/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/factory/LogPlanExprIntervalFactory.java /main/3 2011/09/05 22:47:27 sbishnoi Exp $ */

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
    sbishnoi    09/01/11 - support for interval format
    parujain    03/16/09 - stateless factory
    parujain    10/13/06 - getting returntype from Semantic
    parujain    10/09/06 - Interval datatype
    parujain    10/09/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprIntervalFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprInterval;
import oracle.cep.semantic.ConstIntervalExpr;

public class LogPlanExprIntervalFactory extends LogPlanExprFactory {

  public Expr newExpr(Object ctx) {
    assert ctx instanceof SemQueryExprFactoryContext;
    SemQueryExprFactoryContext lpctx = (SemQueryExprFactoryContext) ctx;
    oracle.cep.semantic.Expr semExpr = lpctx.getExpr();
    assert semExpr instanceof ConstIntervalExpr;
    ConstIntervalExpr intervalExpr = (ConstIntervalExpr) semExpr;

    Expr op = new ExprInterval(intervalExpr.getValue(), 
                               intervalExpr.getReturnType(),
                               intervalExpr.getFormat());
    return op;
  }

}