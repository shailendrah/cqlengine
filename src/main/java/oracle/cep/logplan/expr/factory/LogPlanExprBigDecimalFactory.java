/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/factory/LogPlanExprBigDecimalFactory.java /main/1 2009/11/09 10:10:58 sborah Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      06/23/09 - support for bigdecimal
    sborah      06/23/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/factory/LogPlanExprBigDecimalFactory.java /main/1 2009/11/09 10:10:58 sborah Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprBigDecimal;
import oracle.cep.semantic.ConstBigDecimalExpr;

/**
* LogPlanExprBigDecimalFactory
*/

public class LogPlanExprBigDecimalFactory extends LogPlanExprFactory 
{
    
  @Override
  public Expr newExpr(Object ctx)
  {
    assert ctx instanceof SemQueryExprFactoryContext;
    SemQueryExprFactoryContext lpctx = (SemQueryExprFactoryContext) ctx;
    oracle.cep.semantic.Expr semExpr = lpctx.getExpr();
    assert semExpr instanceof ConstBigDecimalExpr;
    ConstBigDecimalExpr bigDecimalExpr = (ConstBigDecimalExpr) semExpr; 
   
    Expr op = new ExprBigDecimal(bigDecimalExpr.getValue(), bigDecimalExpr.getReturnType());
    return op;
  }
}