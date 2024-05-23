/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyPlanExprXmlConcatFactory.java /main/3 2009/04/28 10:24:10 sborah Exp $ */

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
 sborah      04/28/09 - passing phychildren to expr context
 parujain    03/16/09 - stateless
 skmishra    06/06/08 - cleanup
 skmishra    06/04/08 - bug
 skmishra    05/15/08 - 
 mthatte     04/30/08 - Creation
 */
package oracle.cep.phyplan.factory;

import java.util.ArrayList;
import java.util.List;

import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprXmlConcat;

/**
 * @version $Header: PhyPlanExprXmlConcatFactory.java 30-apr-2008.16:17:04
 *          mthatte Exp $
 * @author mthatte
 * @since release specific (what release of product did this appear in)
 */

public class PhyPlanExprXmlConcatFactory extends PhyPlanExprFactory
{
  public PhyPlanExprXmlConcatFactory()
  {
    super();
  }

  public Expr newExpr(Object ctx)
  {
    oracle.cep.logplan.expr.Expr srcExpr;

    Expr phyExpr;

    assert ctx instanceof LogPlanExprFactoryContext;
    LogPlanExprFactoryContext lpctx = (LogPlanExprFactoryContext) ctx;

    srcExpr = lpctx.getLogExpr();
    assert srcExpr instanceof oracle.cep.logplan.expr.ExprXmlConcat;
    oracle.cep.logplan.expr.ExprXmlConcat logExpr = (oracle.cep.logplan.expr.ExprXmlConcat) srcExpr;

    List<Expr> concatList = new ArrayList<Expr>();
    oracle.cep.logplan.expr.Expr[] concatExprs = logExpr.getConcatExprs();

    for (oracle.cep.logplan.expr.Expr e : concatExprs)
    {
      phyExpr = LogPlanExprFactory.getInterpreter(e,
          new LogPlanExprFactoryContext(e, lpctx.getLogPlan(), 
              lpctx.getPhyChildren(), lpctx.isMakeCorr()));
      concatList.add(phyExpr);
    }
    Expr retExpr = new ExprXmlConcat(concatList);
    return retExpr;
  }
}
