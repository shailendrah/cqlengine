/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/LogPlanExprFactory.java /main/17 2012/05/17 06:50:33 udeshmuk Exp $ */

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
 udeshmuk    05/11/12 - propagate name/alias of the expr to physical layer
 sbishnoi    01/04/10 - table function cleanup
 sbishnoi    10/04/09 - support for table functions
 sborah      06/24/09 - support for bigdecimal
 sbishnoi    12/25/08 - adding entry for ExprTimestamp
 skmishra    05/13/08 - adding exprxmltype
 mthatte     04/30/08 - adding XmlConcat
 parujain    04/25/08 - XMLElement support
 udeshmuk    01/30/08 - support for double data type.
 udeshmuk    01/11/08 - set bNull appropriately.
 najain      10/31/07 - add xmltype
 parujain    06/28/07 - orderby support
 parujain    03/30/07 - support CASE
 parujain    10/31/06 - Complex/Base Boolean ExprFactory
 parujain    10/05/06 - Generic timestamp datatype
 parujain    08/10/06 - Timestamp datatype
 anasrini    06/19/06 - support for function expressions 
 najain      03/01/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/LogPlanExprFactory.java /main/17 2012/05/17 06:50:33 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import java.util.HashMap;

import oracle.cep.logplan.expr.Expr;

/**
 * Factory for the logical plan expression node specific interpreters
 * <p>
 * This is private to the physical plan generation module.
 * 
 * @since 1.0
 */

public class LogPlanExprFactory {

  private static HashMap<String, PhyPlanExprFactory> interpMap;

  static {
    populateInterpMap();
  }

  static void populateInterpMap() {
    interpMap = new HashMap<String, PhyPlanExprFactory>();

    interpMap.put(oracle.cep.logplan.expr.ExprInt.class.getName(), 
        new PhyPlanExprIntFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprBigint.class.getName(), 
        new PhyPlanExprBigintFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprFloat.class.getName(),
        new PhyPlanExprFloatFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprDouble.class.getName(),
        new PhyPlanExprDoubleFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprBigDecimal.class.getName(),
        new PhyPlanExprBigDecimalFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprComplex.class.getName(),
        new PhyPlanExprComplexFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprChar.class.getName(),
        new PhyPlanExprCharFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprByte.class.getName(),
        new PhyPlanExprByteFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprInterval.class.getName(),
    	new PhyPlanExprIntervalFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprBoolean.class.getName(),
    	new PhyPlanExprBooleanFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprAttr.class.getName(),
        new PhyPlanExprAttrFactory());
    interpMap.put(oracle.cep.logplan.expr.BaseBoolExpr.class.getName(),
        new PhyPlanBaseBoolExprFactory());
    interpMap.put(oracle.cep.logplan.expr.ComplexBoolExpr.class.getName(), 
    	new PhyPlanComplexBoolExprFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprUserDefFunc.class.getName(),
        new PhyPlanExprFuncFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprXQryFunc.class.getName(),
        new PhyPlanExprXQryFuncFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprCaseCondition.class.getName(), 
        new PhyPlanExprCaseConditionFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprSearchCase.class.getName(),
        new PhyPlanExprSearchCaseFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprCaseComparison.class.getName(), 
        new PhyPlanExprCaseComparisonFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprSimpleCase.class.getName(),
        new PhyPlanExprSimpleCaseFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprOrderBy.class.getName(), 
        new PhyPlanExprOrderByFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprXmlConcat.class.getName(), 
        new PhyPlanExprXmlConcatFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprXmltype.class.getName(), 
        new PhyPlanExprXmltypeFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprXmlParse.class.getName(), 
        new PhyPlanExprXmlParseFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprXmlAttr.class.getName(), 
    	new PhyPlanExprXmlAttrFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprElement.class.getName(),
    	new PhyPlanExprElementFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprXmlForest.class.getName(), 
    	new PhyPlanExprXmlForestFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprXmlColAttVal.class.getName(), 
    	new PhyPlanExprXmlColAttValFactory());
    interpMap.put(oracle.cep.logplan.expr.ExprTimestamp.class.getName(),
      new PhyPlanExprTimestampFactory());    
  }

  public static oracle.cep.phyplan.expr.Expr getInterpreter(Expr op,
      LogPlanExprFactoryContext ctx) {

    PhyPlanExprFactory o = interpMap.get(op.getClass().getName());

    assert o != null;

    oracle.cep.phyplan.expr.Expr opt = o.newExpr(ctx);
    opt.setbNull(op.isNull());
    opt.setAlias(op.getAlias());
    return opt;
  }
}
