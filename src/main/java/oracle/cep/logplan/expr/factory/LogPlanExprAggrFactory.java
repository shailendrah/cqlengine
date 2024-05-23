/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprAggrFactory.java /main/7 2009/04/06 23:26:51 sborah Exp $ */

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
    sborah      03/09/09 - setting actual name to corresponding aggr fn.
    udeshmuk    04/24/08 - support for aggr distinct
    parujain    11/09/07 - external source
    mthatte     10/30/07 - adding onDemand
    sbishnoi    06/08/07 - support for multi-arg UDAs
    rkomurav    05/28/07 - restructure builtinaggrs
    rkomurav    09/22/06 - exp in aggr
    anasrini    07/12/06 - support for user defined aggregations 
    najain      03/15/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/expr/factory/LogPlanExprAggrFactory.java /main/7 2009/04/06 23:26:51 sborah Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrAggr;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprAttr;
import oracle.cep.semantic.AggrExpr;

/**
 * LogPlanExprAggrFactory
 *
 * @author najain
 */
public class LogPlanExprAggrFactory extends LogPlanExprFactory {

  public LogPlanExprAggrFactory() {
    super();
  }
  
  public Expr newExpr(Object ctx) {
    assert ctx instanceof SemQueryExprFactoryContext;
    SemQueryExprFactoryContext lpctx = (SemQueryExprFactoryContext) ctx;
    oracle.cep.semantic.Expr semExpr = lpctx.getExpr();
    assert semExpr instanceof AggrExpr;
    AggrExpr aggrExpr = (AggrExpr)semExpr;
    oracle.cep.semantic.Expr[] semAggrParamExpr = aggrExpr.getExprs();
    
    Expr[] logAggrParamExpr = new Expr[aggrExpr.getNumParamExprs()];
    
    for(int i=0; i < aggrExpr.getNumParamExprs(); i++)
    {
      logAggrParamExpr[i] = SemQueryExprFactory.getInterpreter(semAggrParamExpr[i], 
         new SemQueryExprFactoryContext(semAggrParamExpr[i], lpctx.getQuery())); 
    }
 
    Attr attr = new AttrAggr(logAggrParamExpr, aggrExpr.getAggrFunction(),
                             aggrExpr.getIsDistinctAggr(), aggrExpr.getReturnType());
    Expr op   = new ExprAttr(aggrExpr.getReturnType(),attr, aggrExpr.getName());
    op.setExternal(semExpr.isExternal());
    return op;
  }

}

