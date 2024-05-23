/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprXmlParseFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $ */

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
    parujain   03/16/09 - stateless factory
    skmishra   05/29/08 - bug
    mthatte    05/19/08 - Creation
 */
package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprXmlParse;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprXmlParseFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $
 *  @author  mthatte
 *  @since   release specific (what release of product did this appear in)
 */

public class LogPlanExprXmlParseFactory extends LogPlanExprFactory
{  
  public Expr newExpr(Object ctx)
  {
    assert ctx instanceof SemQueryExprFactoryContext;
    
    SemQueryExprFactoryContext sctx = (SemQueryExprFactoryContext)ctx;
    oracle.cep.semantic.Expr semExpr = sctx.getExpr();
    assert semExpr instanceof oracle.cep.semantic.XMLParseExpr;
    oracle.cep.semantic.XMLParseExpr xsemExpr = (oracle.cep.semantic.XMLParseExpr)semExpr;
    
    //Get the logical expr pointed by the xmlparse expr
    Expr logExpr = SemQueryExprFactory.getInterpreter(xsemExpr.getValue(), 
        new SemQueryExprFactoryContext(xsemExpr.getValue(),sctx.getQuery()));
    
    return new ExprXmlParse(logExpr, xsemExpr.isWellformed(), xsemExpr.getKind());
  }
}