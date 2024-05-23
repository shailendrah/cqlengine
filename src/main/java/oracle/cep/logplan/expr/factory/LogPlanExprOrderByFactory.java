/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprOrderByFactory.java /main/3 2009/03/30 14:46:01 parujain Exp $ */

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
    parujain    03/16/09 - stateless factory
    parujain    11/07/07 - add OnDemand
    parujain    06/27/07 - Order by expression factory
    parujain    06/27/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprOrderByFactory.java /main/3 2009/03/30 14:46:01 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprOrderBy;
import oracle.cep.semantic.OrderByExpr;

public class LogPlanExprOrderByFactory extends LogPlanExprFactory {
  
  @Override
  public Expr newExpr(Object ctx) {
    assert ctx instanceof SemQueryExprFactoryContext;
    SemQueryExprFactoryContext lpctx = (SemQueryExprFactoryContext) ctx;
    oracle.cep.semantic.Expr semExpr = lpctx.getExpr();
    assert semExpr instanceof OrderByExpr;
    OrderByExpr orderExpr = (OrderByExpr)semExpr;
    
    Expr order  = SemQueryExprFactory.getInterpreter(
                       orderExpr.getOrderbyExpr(),
                       new SemQueryExprFactoryContext(
                          orderExpr.getOrderbyExpr(), lpctx.getQuery()));
    Expr op = new ExprOrderBy(order, orderExpr.isNullsFirst(), orderExpr.isAscending(), order.getType());
    op.setExternal(orderExpr.isExternal());
    return op;
  }
  
}
