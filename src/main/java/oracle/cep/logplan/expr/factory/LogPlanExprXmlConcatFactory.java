/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprXmlConcatFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $ */

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
 skmishra    06/04/08 - bug
 skmishra    05/02/08 - 
 mthatte     04/25/08 - Creation
 */
package oracle.cep.logplan.expr.factory;

import java.util.ArrayList;
import java.util.List;

import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprXmlConcat;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprXmlConcatFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */

public class LogPlanExprXmlConcatFactory extends LogPlanExprFactory
{
  public Expr newExpr(Object ctx)
  {
    assert ctx instanceof SemQueryExprFactoryContext;
    
    SemQueryExprFactoryContext sctx = (SemQueryExprFactoryContext)ctx;
    oracle.cep.semantic.Expr semExpr = sctx.getExpr();
    assert semExpr instanceof oracle.cep.semantic.XMLConcatExpr;
    oracle.cep.semantic.XMLConcatExpr xsemExpr = (oracle.cep.semantic.XMLConcatExpr)semExpr;
    List<Expr> concatList = new ArrayList<Expr>();
    
    for(oracle.cep.semantic.Expr e : xsemExpr.getConcatExprs())
    {
      Expr logExpr = SemQueryExprFactory.getInterpreter(e, 
          new SemQueryExprFactoryContext(e,sctx.getQuery()));
      concatList.add(logExpr);
    }
    
    return new ExprXmlConcat(concatList);
  }
}