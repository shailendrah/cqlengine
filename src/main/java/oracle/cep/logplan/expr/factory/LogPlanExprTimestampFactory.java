/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprTimestampFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates. 
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
    udeshmuk    02/21/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprTimestampFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprTimestamp;
import oracle.cep.semantic.ConstTimestampExpr;

public class LogPlanExprTimestampFactory extends LogPlanExprFactory {
  
  public Expr newExpr(Object ctx) {
    assert ctx instanceof SemQueryExprFactoryContext;
    SemQueryExprFactoryContext lpctx = (SemQueryExprFactoryContext) ctx;
    oracle.cep.semantic.Expr semExpr = lpctx.getExpr();
    assert semExpr instanceof ConstTimestampExpr;
    ConstTimestampExpr timestampExpr = (ConstTimestampExpr) semExpr;

    Expr op = new ExprTimestamp(timestampExpr.getValue(), timestampExpr.getReturnType());
    return op;
  }
}