/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprBaseBoolFactory.java /main/4 2009/03/30 14:46:01 parujain Exp $ */

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
    parujain    11/07/07 - removing onDemand for left and right
    mthatte     10/30/07 - adding onDemand
    rkomurav    11/08/06 - outer join support
    parujain    10/31/06 - Base Boolean Expr Factory
    parujain    10/31/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprBaseBoolFactory.java /main/4 2009/03/30 14:46:01 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan.expr.factory;

import oracle.cep.semantic.BaseBExpr;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.BaseBoolExpr;

/**
 * Factory for creation of logical layer boolean expression (simple predicate)
 *
 * @since 1.0
 */

class LogPlanExprBaseBoolFactory extends LogPlanExprFactory {

  LogPlanExprBaseBoolFactory() {
  }

  public Expr newExpr(Object ctx) {
    BaseBExpr                  semExpr;
    BaseBoolExpr               pred;
    Expr                       left;
    Expr                       right;
    Expr                       unary;
    SemQueryExprFactoryContext ctx1;

    ctx1    = (SemQueryExprFactoryContext)ctx;
    semExpr = (BaseBExpr)ctx1.getExpr();

    if(semExpr.getCompOp() != null)
    {
      ctx1.expr = semExpr.getLeftOperand();
      left      = SemQueryExprFactory.getInterpreter(semExpr.getLeftOperand(), 
                                                   ctx1);
    
      ctx1.expr = semExpr.getRightOperand();
      right = SemQueryExprFactory.getInterpreter(semExpr.getRightOperand(), 
                                               ctx1);
    
      if(semExpr.getOuterJoinType() != null) 
        pred = new BaseBoolExpr(semExpr.getCompOp(), left, right, semExpr.getReturnType(), semExpr.getOuterJoinType());
      else
        pred = new BaseBoolExpr(semExpr.getCompOp(), left, right, semExpr.getReturnType());
    
      pred.setExternal(semExpr.isExternal());
      return pred;
    }
    else
    {
      ctx1.expr = semExpr.getUnaryOperand();
      
      unary = SemQueryExprFactory.getInterpreter(semExpr.getUnaryOperand(), ctx1);
      
      pred = new BaseBoolExpr(semExpr.getUnaryOp(), unary, semExpr.getReturnType());
      pred.setExternal(semExpr.isExternal());
      return pred;
    }
  }
}
