/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/factory/LogPlanExprAttrFactory.java /main/6 2012/05/02 03:05:59 pkali Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       04/03/12 - added datatype arg
    parujain    03/16/09 - stateless factory
    parujain    11/06/07 - actualName
    mthatte     10/30/07 - adding onDemand
    anasrini    05/25/07 - semantic corr attr support
    anasrini    05/25/07 - inline view support
    rkomurav    03/05/07 - add corrAttr
    najain      03/15/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/factory/LogPlanExprAttrFactory.java /main/6 2012/05/02 03:05:59 pkali Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr.factory;

import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprAttr;
import oracle.cep.semantic.AttrExpr;
import oracle.cep.semantic.SemCorrAttr;
import oracle.cep.semantic.SemAttrType;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrNamed;
import oracle.cep.logplan.attr.CorrAttr;

/**
 * LogPlanExprIntFactory
 *
 * @author najain
 */
public class LogPlanExprAttrFactory extends LogPlanExprFactory {

  public LogPlanExprAttrFactory() {
    super();
  }
  
  public Expr newExpr(Object ctx) {
    assert ctx instanceof SemQueryExprFactoryContext;
    SemQueryExprFactoryContext lpctx = (SemQueryExprFactoryContext) ctx;
    oracle.cep.semantic.Expr semExpr = lpctx.getExpr();
    assert semExpr instanceof AttrExpr;
    AttrExpr attrExpr = (AttrExpr)semExpr;
    oracle.cep.semantic.Attr semAttr = attrExpr.getAttr();
    int varId = semAttr.getVarId();
    SemAttrType type = semAttr.getSemAttrType();
    
    Attr attr;
    if(type == SemAttrType.NAMED)
    {
      attr = new AttrNamed(varId, semAttr.getAttrId(), attrExpr.getReturnType());
      attr.setActualName( semAttr.getActualName());
    }
    else {
      SemCorrAttr semCorrAttr;

      assert type == SemAttrType.CORR;
      assert semAttr instanceof SemCorrAttr;
      semCorrAttr = (SemCorrAttr) semAttr;
      attr = new CorrAttr(varId, semCorrAttr.getBaseEntityVarId(),
                          semAttr.getAttrId(), attrExpr.getReturnType());
    }
    Expr op = new ExprAttr(attrExpr.getReturnType(), attr);
    op.setExternal(semExpr.isExternal());
    ((ExprAttr)op).setActualName(attrExpr.getActualName());
    return op;
  }

}
