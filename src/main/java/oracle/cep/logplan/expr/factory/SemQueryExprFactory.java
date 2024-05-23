/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/factory/SemQueryExprFactory.java /main/18 2012/05/17 06:50:33 udeshmuk Exp $ */

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
 udeshmuk    05/10/12 - propagate name/alias of the expr to logical layer
 pkali       03/30/12 - added GroupByExpr class
 sborah      06/23/09 - support for bigdecimal
 parujain    03/16/09 - stateless factory
 udeshmuk    06/05/08 - support for xmlagg.
 skmishra    05/19/08 - adding xmlparseexpr
 skmishra    05/13/08 - adding exprxmltype
 mthatte     04/25/08 - adding XmlConcatExpr
 parujain    04/23/08 - XMLElement support
 udeshmuk    02/21/08 - add constTimestamp and constByte.
 udeshmuk    01/30/08 - support for double data type.
 udeshmuk    01/11/08 - set bNull appropriately.
 najain      10/31/07 - xmltype support
 parujain    06/27/07 - order by
 parujain    03/29/07 - support for CASE
 parujain    10/31/06 - Base/complex boolean exprs
 parujain    10/05/06 - Generic timestamp datatype
 parujain    08/10/06 - Timestamp datatype
 anasrini    06/19/06 - support for functions 
 anasrini    03/29/06 - support for BoolExpr 
 najain      03/15/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/factory/SemQueryExprFactory.java /main/18 2012/05/17 06:50:33 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr.factory;

import java.util.HashMap;

import oracle.cep.logplan.expr.Expr;

/**
 * Factory for the semantic query expression specific interpreters
 * <p>
 * This is private to the logical plan expression generation module.
 * This converts a semantic query expression to a logical plan expression.
 * 
 * @since 1.0
 */

public class SemQueryExprFactory {

  private static HashMap<String, LogPlanExprFactory> interpMap;

  static {
    populateInterpMap();
  }

  static void populateInterpMap() {
    interpMap = new HashMap<String, LogPlanExprFactory>();

    interpMap.put(oracle.cep.semantic.ConstIntExpr.class.getName(),
        new LogPlanExprIntFactory());
    interpMap.put(oracle.cep.semantic.ConstBigintExpr.class.getName(),
        new LogPlanExprBigintFactory());
    interpMap.put(oracle.cep.semantic.ConstFloatExpr.class.getName(),
        new LogPlanExprFloatFactory());
    interpMap.put(oracle.cep.semantic.ConstDoubleExpr.class.getName(),
        new LogPlanExprDoubleFactory());
    interpMap.put(oracle.cep.semantic.ConstBigDecimalExpr.class.getName(),
        new LogPlanExprBigDecimalFactory());
    interpMap.put(oracle.cep.semantic.ConstCharExpr.class.getName(),
        new LogPlanExprCharFactory());
    interpMap.put(oracle.cep.semantic.ConstByteExpr.class.getName(),
        new LogPlanExprByteFactory());
    interpMap.put(oracle.cep.semantic.ConstTimestampExpr.class.getName(),
        new LogPlanExprTimestampFactory());
    interpMap.put(oracle.cep.semantic.ConstIntervalExpr.class.getName(), 
    	new LogPlanExprIntervalFactory());
    interpMap.put(oracle.cep.semantic.ConstBooleanExpr.class.getName(), 
    	new LogPlanExprBooleanFactory());
    interpMap.put(oracle.cep.semantic.AttrExpr.class.getName(),
        new LogPlanExprAttrFactory());
    interpMap.put(oracle.cep.semantic.ComplexExpr.class.getName(),
        new LogPlanExprComplexFactory());
    interpMap.put(oracle.cep.semantic.AggrExpr.class.getName(),
        new LogPlanExprAggrFactory());
    interpMap.put(oracle.cep.semantic.XMLAggExpr.class.getName(),
        new LogPlanExprXMLAggFactory()); 
    interpMap.put(oracle.cep.semantic.BaseBExpr.class.getName(),
        new LogPlanExprBaseBoolFactory());
    interpMap.put(oracle.cep.semantic.ComplexBExpr.class.getName(), 
    	new LogPlanExprComplexBoolFactory());
    interpMap.put(oracle.cep.semantic.FuncExpr.class.getName(),
        new LogPlanExprFuncFactory());
    interpMap.put(oracle.cep.semantic.XQryFuncExpr.class.getName(),
        new LogPlanExprXQryFuncFactory());
    interpMap.put(oracle.cep.semantic.SearchedCaseExpr.class.getName(), 
        new LogPlanExprSearchCaseFactory());
    interpMap.put(oracle.cep.semantic.CaseConditionExpr.class.getName(), 
        new LogPlanExprCaseConditionFactory());
    interpMap.put(oracle.cep.semantic.SimpleCaseExpr.class.getName(),
        new LogPlanExprSimpleCaseFactory());
    interpMap.put(oracle.cep.semantic.CaseComparisonExpr.class.getName(), 
        new LogPlanExprCaseComparisonFactory());
    interpMap.put(oracle.cep.semantic.OrderByExpr.class.getName(),
        new LogPlanExprOrderByFactory());
    interpMap.put(oracle.cep.semantic.XMLConcatExpr.class.getName(), 
        new LogPlanExprXmlConcatFactory());
    interpMap.put(oracle.cep.semantic.ConstXmltypeExpr.class.getName(), 
        new LogPlanExprXmltypeFactory());
    interpMap.put(oracle.cep.semantic.XMLParseExpr.class.getName(), 
        new LogPlanExprXmlParseFactory());
    interpMap.put(oracle.cep.semantic.XmlAttrExpr.class.getName(), 
        new LogPlanExprXmlAttrFactory());
    interpMap.put(oracle.cep.semantic.ElementExpr.class.getName(), 
    	new LogPlanExprElementFactory());
    interpMap.put(oracle.cep.semantic.XmlForestExpr.class.getName(), 
        new LogPlanExprXmlForestFactory());
    interpMap.put(oracle.cep.semantic.XmlColAttValExpr.class.getName(), 
        new LogPlanExprXmlColAttValFactory());
    interpMap.put(oracle.cep.semantic.GroupByExpr.class.getName(),
            new LogPlanExprGroupByFactory());
  }

  public static Expr getInterpreter(oracle.cep.semantic.Expr expr,
                                    SemQueryExprFactoryContext ctx) {

    LogPlanExprFactory o = interpMap.get(expr.getClass().getName());

    assert o != null;

    Expr op = o.newExpr(ctx);
    op.setbNull(expr.isNull());
    op.setAlias(expr.getAlias());
    return op;
  }
}
