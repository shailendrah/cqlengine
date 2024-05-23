/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogPlanHelper.java /main/6 2012/05/02 03:05:57 pkali Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Logical Plan Generation. Miscellaneous helper methods.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       04/04/12 - included datatype arg in AttrNamed instance
    udeshmuk    04/05/11 - propagate attrname
    parujain    05/20/09 - ansi outer join
    anasrini    07/02/07 - cleanup
    anasrini    05/25/07 - inline view support
    anasrini    05/24/07 - logplan reorg
    najain      02/16/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogPlanHelper.java /main/6 2012/05/02 03:05:57 pkali Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan;

import java.util.Iterator;
import java.util.Vector;

import oracle.cep.common.LogicalOp;
import oracle.cep.semantic.SemQuery;
import oracle.cep.semantic.SFWQuery;
import oracle.cep.semantic.BExpr;
import oracle.cep.semantic.ComplexBExpr;
import oracle.cep.logplan.attr.AttrNamed;
import oracle.cep.logplan.expr.BoolExpr;
import oracle.cep.logplan.expr.factory.SemQueryExprFactory;
import oracle.cep.logplan.expr.factory.SemQueryExprFactoryContext;


/**
 * Logical Plan Generation. Miscellaneous helper methods.
 */

class LogPlanHelper {

  /**
   * This class should not be instantiated.
   * Contains only static methods.
   */
  private LogPlanHelper() {
  }

  static AttrNamed transformAttr(oracle.cep.semantic.Attr semAttr) {
    AttrNamed attr = new AttrNamed(semAttr.getVarId(), semAttr.getAttrId(), 
                             semAttr.getDatatype());
    attr.setActualName(semAttr.getActualName());
    return attr;
  }
  
  static LogOpt applyPredsHelper_n(SFWQuery query, LogOpt input, 
                                   BExpr predicates)
  {
    LogOptSelect select = null;
    SemQueryExprFactoryContext ctx;
    BoolExpr pred;

    Vector<BExpr> condNodes = new Vector<BExpr>();
    
    splitPredicate(predicates, condNodes);

    // At this point we know that there is at least 1 predicate

    Iterator<BExpr> iterator = condNodes.iterator();
    // Include one select operator per predicate
    while(iterator.hasNext())
    {
      select = new LogOptSelect(input);
      BExpr cond = iterator.next();
      ctx = new SemQueryExprFactoryContext(cond, query);
      pred = (BoolExpr) SemQueryExprFactory.getInterpreter(cond, ctx);
      select.setBExpr(pred);
      input = select;
    }
    return select;
  }

  static void splitPredicate(BExpr bexpr, Vector<BExpr> condNodes)
  {
    if(bexpr == null)
      return;
  
    if(!(bexpr instanceof ComplexBExpr))
    {
      condNodes.add(bexpr);
      return;
    }
    ComplexBExpr boolExpr = (ComplexBExpr)bexpr;
    if(boolExpr.getLogicalOp() == LogicalOp.AND)
    {
      splitPredicate((BExpr)boolExpr.getLeftOperand(),condNodes);
      splitPredicate((BExpr)boolExpr.getRightOperand(),condNodes);
    }
    else
    {
      condNodes.add(bexpr);
      return;
    }
  }

}
