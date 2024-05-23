/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprElementFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $ */

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
    parujain    05/16/08 - evalname
    parujain    04/23/08 - XMLElement ExprFactory
    parujain    04/23/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprElementFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprElement;
import oracle.cep.semantic.ElementExpr;

public class LogPlanExprElementFactory extends LogPlanExprFactory {

  
  @Override
  public Expr newExpr(Object ctx) {
    assert ctx instanceof SemQueryExprFactoryContext;
    SemQueryExprFactoryContext lpctx = (SemQueryExprFactoryContext) ctx;
    oracle.cep.semantic.Expr semExpr = lpctx.getExpr();
    assert semExpr instanceof ElementExpr;
    ElementExpr element = (ElementExpr)semExpr;
    
    String name = element.getElementName();
    Expr nameExpr;
    
    oracle.cep.semantic.Expr[] attrs = element.getAttrExprs();
    int numAttrs = element.getNumAttrs();
    Expr[] logAttrs = new Expr[numAttrs];
    for(int i=0; i<numAttrs; i++)
    {
      logAttrs[i] = SemQueryExprFactory.getInterpreter(attrs[i],
    		             new SemQueryExprFactoryContext(attrs[i], lpctx.getQuery()));
    }
    
    oracle.cep.semantic.Expr[] children = element.getChildExprs();
    int numChild = element.getNumChildren();
    Expr[] logChild = new Expr[numChild];
    for(int j=0; j<numChild; j++)
    {
      logChild[j] = SemQueryExprFactory.getInterpreter(children[j],
	                     new SemQueryExprFactoryContext(children[j], lpctx.getQuery()));
    }
   
    Expr logExpr;
    
    if(!element.isElementNameExpr())
      logExpr = new ExprElement(name, logAttrs, logChild, element.getReturnType());
    else
    {
      nameExpr = SemQueryExprFactory.getInterpreter(element.getElementNameExpr(), 
                     new SemQueryExprFactoryContext(element.getElementNameExpr(), lpctx.getQuery()));
      logExpr = new ExprElement(nameExpr, logAttrs, logChild, element.getReturnType());
    }
    return logExpr;
  }
	
}