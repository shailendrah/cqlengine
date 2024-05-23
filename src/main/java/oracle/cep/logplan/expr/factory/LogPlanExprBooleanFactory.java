/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprBooleanFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $ */

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
    mthatte     01/14/08 - 
    najain      01/02/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprBooleanFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprBoolean;
import oracle.cep.semantic.ConstBooleanExpr;

/**
 * LogPlanExprBooleanFactory
 *
 * @author najain
 */
public class LogPlanExprBooleanFactory extends LogPlanExprFactory {

  public Expr newExpr(Object ctx) {
    assert ctx instanceof SemQueryExprFactoryContext;
    SemQueryExprFactoryContext lpctx = (SemQueryExprFactoryContext) ctx;
    oracle.cep.semantic.Expr semExpr = lpctx.getExpr();
    assert semExpr instanceof ConstBooleanExpr;
    ConstBooleanExpr booleanExpr = (ConstBooleanExpr)semExpr;

    Expr op = new ExprBoolean(booleanExpr.getValue(), booleanExpr.getReturnType());
    return op;
  }

}
