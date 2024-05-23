/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprXmlAttrFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $ */

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
    parujain    04/23/08 - XMLAttrExpr Factory
    parujain    04/23/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprXmlAttrFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprXmlAttr;
import oracle.cep.semantic.XmlAttrExpr;

public class LogPlanExprXmlAttrFactory  extends LogPlanExprFactory {
  
  @Override
  public Expr newExpr(Object ctx) {
    assert ctx instanceof SemQueryExprFactoryContext;
    SemQueryExprFactoryContext lpctx = (SemQueryExprFactoryContext) ctx;
    oracle.cep.semantic.Expr semExpr = lpctx.getExpr();
    assert semExpr instanceof XmlAttrExpr;
    XmlAttrExpr semAttrExpr = (XmlAttrExpr)semExpr;
    
    String name = semAttrExpr.getAttrName();
    
    Expr logAttr = SemQueryExprFactory.getInterpreter(semAttrExpr.getAttrExpr(),
    		              new SemQueryExprFactoryContext(semAttrExpr.getAttrExpr(), lpctx.getQuery()));
    
    Expr logExpr ;
    if(!semAttrExpr.isAttrNameExpr())
    {
      logExpr = new ExprXmlAttr(name, logAttr, semAttrExpr.getReturnType());
    }
    else
    {
      Expr nameExpr = SemQueryExprFactory.getInterpreter(semAttrExpr.getAttrNameExpr(), 
                          new SemQueryExprFactoryContext(semAttrExpr.getAttrNameExpr(), lpctx.getQuery()));
      logExpr = new ExprXmlAttr(nameExpr, logAttr, semAttrExpr.getReturnType());
    }
    
    return logExpr;
  }
		
	}