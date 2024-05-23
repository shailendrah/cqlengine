/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprXmlColAttValFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $ */

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
    parujain    05/29/08 - xmlcolattVal support
    parujain    05/29/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprXmlColAttValFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprXmlColAttVal;
import oracle.cep.semantic.XmlColAttValExpr;

public class LogPlanExprXmlColAttValFactory extends LogPlanExprFactory {

  @Override
  public Expr newExpr(Object ctx) {
    assert ctx instanceof SemQueryExprFactoryContext;
    SemQueryExprFactoryContext lpctx = (SemQueryExprFactoryContext) ctx;
    oracle.cep.semantic.Expr semExpr = lpctx.getExpr();
    assert semExpr instanceof XmlColAttValExpr;
    
    XmlColAttValExpr semColexpr = (XmlColAttValExpr)semExpr;
    oracle.cep.semantic.Expr[] semChild = semColexpr.getColAttExprs();
    Expr[] logChild = new Expr[semChild.length];
    
    for(int i=0; i<semChild.length; i++)
    {
      logChild[i] =  SemQueryExprFactory.getInterpreter(semChild[i],
	                 new SemQueryExprFactoryContext(semChild[i], lpctx.getQuery())); 
    }
    
    Expr logExpr = new ExprXmlColAttVal(logChild, semColexpr.getReturnType());
	return logExpr;
  }
	
}
