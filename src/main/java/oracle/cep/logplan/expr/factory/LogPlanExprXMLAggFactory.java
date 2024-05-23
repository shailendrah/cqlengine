/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprXMLAggFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $ */

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
    udeshmuk    06/05/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprXMLAggFactory.java /main/2 2009/03/30 14:46:01 parujain Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrXMLAgg;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprAttr;
import oracle.cep.logplan.expr.ExprOrderBy;
import oracle.cep.semantic.XMLAggExpr;

public class LogPlanExprXMLAggFactory extends LogPlanExprFactory {

  @Override
  public Expr newExpr(Object ctx)
  {
    assert ctx instanceof SemQueryExprFactoryContext;
    SemQueryExprFactoryContext lpctx = (SemQueryExprFactoryContext) ctx;
    oracle.cep.semantic.Expr semExpr = lpctx.getExpr();
    assert semExpr instanceof XMLAggExpr;
    XMLAggExpr aggrExpr = (XMLAggExpr)semExpr;
    oracle.cep.semantic.Expr[] semAggrParamExpr = aggrExpr.getExprs();
    
    Expr[] logAggrParamExpr = new Expr[aggrExpr.getNumParamExprs()];
    
    for(int i=0; i < aggrExpr.getNumParamExprs(); i++)
    {
      logAggrParamExpr[i] = SemQueryExprFactory.getInterpreter(semAggrParamExpr[i], 
        new SemQueryExprFactoryContext(semAggrParamExpr[i], lpctx.getQuery())); 
    }
    // transform orderby exprs
    ExprOrderBy[] logOrderByExprs;
    oracle.cep.semantic.Expr[] semOrderByExprs = aggrExpr.getOrderByExprs(); 
    if(semOrderByExprs != null)
    {
      logOrderByExprs = new ExprOrderBy[semOrderByExprs.length];
      for(int i=0; i < semOrderByExprs.length; i++)
      {
        logOrderByExprs[i] = (ExprOrderBy)SemQueryExprFactory.getInterpreter(semOrderByExprs[i],
          new SemQueryExprFactoryContext(semOrderByExprs[i],lpctx.getQuery()));
      }
    }
    else 
      logOrderByExprs = null;
    
    Attr attr = new AttrXMLAgg(logAggrParamExpr, aggrExpr.getAggrFunction(),
                               aggrExpr.getIsDistinctAggr(), aggrExpr.getReturnType(),
                               logOrderByExprs);
    Expr op   = new ExprAttr(aggrExpr.getReturnType(),attr);
    op.setExternal(semExpr.isExternal());
    return op;
  }
  
}