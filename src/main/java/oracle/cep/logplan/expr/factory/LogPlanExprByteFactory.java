/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprByteFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $ */

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
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprByteFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprByte;
import oracle.cep.semantic.ConstByteExpr;

public class LogPlanExprByteFactory extends LogPlanExprFactory {

  public Expr newExpr(Object ctx) {
    assert ctx instanceof SemQueryExprFactoryContext;
    SemQueryExprFactoryContext lpctx = (SemQueryExprFactoryContext) ctx;
    oracle.cep.semantic.Expr semExpr = lpctx.getExpr();
    assert semExpr instanceof ConstByteExpr;
    ConstByteExpr byteExpr = (ConstByteExpr)semExpr;

    Expr op = new ExprByte(byteExpr.getValue(), byteExpr.getReturnType());
    return op;
  }
}