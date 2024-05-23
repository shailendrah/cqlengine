/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/factory/LogPlanExprGroupByFactory.java /main/1 2012/05/02 03:05:59 pkali Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       03/29/12 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/factory/LogPlanExprGroupByFactory.java /main/1 2012/05/02 03:05:59 pkali Exp $
 *  @author  pkali   
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrGroupBy;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprAttr;
import oracle.cep.semantic.GroupByExpr;

/**
 * LogPlanExprGroupByFactory
 *
 * Conversion from semantic to logical layer representation of group by
 * expressions
 *
 * @author pkali
 */
public class LogPlanExprGroupByFactory extends LogPlanExprFactory 
{
  public LogPlanExprGroupByFactory()
  {
    super();
  }

  public Expr newExpr(Object ctx)
  {
    assert ctx instanceof SemQueryExprFactoryContext;
    SemQueryExprFactoryContext lpctx = (SemQueryExprFactoryContext) ctx;
    oracle.cep.semantic.Expr semExpr = lpctx.getExpr();
    assert semExpr instanceof GroupByExpr;
    GroupByExpr gbyExpr = (GroupByExpr)semExpr;
    Expr expr = SemQueryExprFactory.getInterpreter(gbyExpr.getExpr(),
          new SemQueryExprFactoryContext(gbyExpr.getExpr(),lpctx.getQuery()));

    Attr attr = new AttrGroupBy(expr, expr.getType());
    attr.setActualName( gbyExpr.getName());
    Expr op = new ExprAttr(gbyExpr.getReturnType(), attr, gbyExpr.getName());
    op.setExternal(semExpr.isExternal());
    return op;
  }
}
    
