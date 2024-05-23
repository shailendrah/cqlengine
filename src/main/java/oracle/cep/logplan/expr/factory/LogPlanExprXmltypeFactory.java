/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprXmltypeFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $ */

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
    skmishra    06/05/08 - cleanup
    skmishra    05/16/08 - changing representation to node
    skmishra    05/13/08 - Creation
 */

package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprXmltype;
import oracle.cep.semantic.ConstXmltypeExpr;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprXmltypeFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $
 *  @author  skmishra
 *  @since   release specific (what release of product did this appear in)
 */

public class LogPlanExprXmltypeFactory extends LogPlanExprFactory
{
  public Expr newExpr(Object ctx) {
    assert ctx instanceof SemQueryExprFactoryContext;
    SemQueryExprFactoryContext lpctx = (SemQueryExprFactoryContext) ctx;
    oracle.cep.semantic.Expr semExpr = lpctx.getExpr();
    assert semExpr instanceof ConstXmltypeExpr;
    ConstXmltypeExpr xmlExpr = (ConstXmltypeExpr)semExpr;

    Expr op = new ExprXmltype(xmlExpr.getValue());
    return op;
  }

}