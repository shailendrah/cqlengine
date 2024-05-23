/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyPlanExprBigDecimalFactory.java /main/1 2009/11/09 10:10:58 sborah Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      06/24/09 - support for bigdecimal
    sborah      06/24/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyPlanExprBigDecimalFactory.java /main/1 2009/11/09 10:10:58 sborah Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.factory;

import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprBigDecimal;

/**
 * PhyPlanExprDoubleFactory
 */

public class PhyPlanExprBigDecimalFactory extends PhyPlanExprFactory
{

  public Expr newExpr(Object ctx)
  {
    oracle.cep.logplan.expr.Expr src;
    
    assert ctx instanceof LogPlanExprFactoryContext;
    LogPlanExprFactoryContext lpctx = (LogPlanExprFactoryContext) ctx;

    src = lpctx.getLogExpr();
    assert src instanceof oracle.cep.logplan.expr.ExprBigDecimal;
    oracle.cep.logplan.expr.ExprBigDecimal srcExpr = (oracle.cep.logplan.expr.ExprBigDecimal) src;
    
    Expr phyExpr = new ExprBigDecimal(srcExpr.getNValue());
    return phyExpr;
  }

}