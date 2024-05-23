/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprCharFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
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
    parujain    10/13/06 - getting returntype from Semantic
    najain      03/15/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprCharFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprChar;
import oracle.cep.semantic.ConstCharExpr;

/**
 * LogPlanExprIntFactory
 *
 * @author najain
 */
public class LogPlanExprCharFactory extends LogPlanExprFactory {
  
  public Expr newExpr(Object ctx) {
    assert ctx instanceof SemQueryExprFactoryContext;
    SemQueryExprFactoryContext lpctx = (SemQueryExprFactoryContext) ctx;
    oracle.cep.semantic.Expr semExpr = lpctx.getExpr();
    assert semExpr instanceof ConstCharExpr;
    ConstCharExpr charExpr = (ConstCharExpr)semExpr;

    // TODO: need to work with Anand so that both semantic and logical expressions
    // use either String or char array
    Expr op = new ExprChar(charExpr.getValue().toCharArray(), charExpr.getReturnType());
    return op;
  }

}
