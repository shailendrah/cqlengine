/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprComplexBoolFactory.java /main/3 2009/03/30 14:46:01 parujain Exp $ */

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
    parujain    11/07/07 - remove on demand for left and right
    mthatte     10/30/07 - adding onDemand
    parujain    11/03/06 - Tree representation for conditions
    parujain    10/31/06 - Complex Boolean Expr Factory
    parujain    10/31/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprComplexBoolFactory.java /main/3 2009/03/30 14:46:01 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr.factory;

import oracle.cep.semantic.ComplexBExpr;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ComplexBoolExpr;

/**
 * Factory for creation of logical layer boolean expression (simple predicate)
 *
 * @since 1.0
 */

class LogPlanExprComplexBoolFactory extends LogPlanExprFactory {

  public Expr newExpr(Object ctx) {
    ComplexBExpr               semExpr;
    ComplexBoolExpr            pred;
    Expr                       left = null;
    Expr                       right = null;
    SemQueryExprFactoryContext ctx1;

    assert ctx instanceof SemQueryExprFactoryContext;
    ctx1    = (SemQueryExprFactoryContext)ctx;
    semExpr = (ComplexBExpr)ctx1.getExpr();

    assert semExpr.getLogicalOp() != null;
    assert semExpr.getLeftOperand() != null;
    ctx1.expr = semExpr.getLeftOperand();
    left      = SemQueryExprFactory.getInterpreter(semExpr.getLeftOperand(), 
                           new SemQueryExprFactoryContext(
                            semExpr.getLeftOperand(), ctx1.getQuery()));
    
    if(semExpr.getRightOperand() != null)
    {
      ctx1.expr = semExpr.getRightOperand();
      right = SemQueryExprFactory.getInterpreter(semExpr.getRightOperand(), 
                  new SemQueryExprFactoryContext(
       	                 semExpr.getRightOperand(), ctx1.getQuery()));
    }

    pred = new ComplexBoolExpr(semExpr.getLogicalOp(), left, right, semExpr.getReturnType());
    pred.setExternal(semExpr.isExternal());
    return pred;
  }
}

