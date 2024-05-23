/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprComplexFactory.java /main/3 2009/03/30 14:46:01 parujain Exp $ */

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
    parujain    11/07/07 - isOnDemand support
    parujain    10/12/06 - return type from Semantic layer
    dlenkov     09/22/06 - conversion support
    najain      03/15/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprComplexFactory.java /main/3 2009/03/30 14:46:01 parujain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprComplex;
import oracle.cep.semantic.ComplexExpr;

/**
 * LogPlanExprIntFactory
 *
 * @author najain
 */
public class LogPlanExprComplexFactory extends LogPlanExprFactory {
  
  public Expr newExpr(Object ctx) {
    assert ctx instanceof SemQueryExprFactoryContext;
    SemQueryExprFactoryContext lpctx = (SemQueryExprFactoryContext) ctx;
    oracle.cep.semantic.Expr semExpr = lpctx.getExpr();
    assert semExpr instanceof ComplexExpr;
    ComplexExpr complexExpr = (ComplexExpr)semExpr;

    assert (complexExpr.getLeftOperand() != null);
    Expr left  = SemQueryExprFactory.getInterpreter(
		   complexExpr.getLeftOperand(),
		   new SemQueryExprFactoryContext(
		     complexExpr.getLeftOperand(), lpctx.getQuery()));

    Expr right = null;
    if (complexExpr.getRightOperand() != null)
    {
      right = SemQueryExprFactory.getInterpreter(
		complexExpr.getRightOperand(),
		new SemQueryExprFactoryContext(
		  complexExpr.getRightOperand(), lpctx.getQuery()));
    }

    Expr op = new ExprComplex(complexExpr.getArithOp(), left, right, semExpr.getReturnType()); 
    op.setExternal(complexExpr.isExternal());    
 
    return op;
  }

}

