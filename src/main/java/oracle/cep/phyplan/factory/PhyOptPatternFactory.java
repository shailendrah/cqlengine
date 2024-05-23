/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyOptPatternFactory.java /main/21 2010/03/23 01:50:14 udeshmuk Exp $ */

/* Copyright (c) 2007, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    building of physical level pattern operator is done here

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    03/09/10 - within clause support
    sborah      04/24/09 - pass phyChildren to constructor
    udeshmuk    03/05/09 - remove project operator for measures.
    udeshmuk    02/02/09 - support for duration arith_expr in pattern
    udeshmuk    10/16/08 - support for xmlagg orderby in pattern
    hopark      10/09/08 - remove statics
    udeshmuk    07/12/08 - 
    rkomurav    07/07/08 - add isrecurring non event flag
    rkomurav    05/15/08 - support non event deteection
    rkomurav    05/13/08 - rename isIsmple to isSingleton
    rkomurav    03/19/08 - support subest
    rkomurav    02/26/08 - remove alphtostate map
    rkomurav    02/21/08 - refplace DFA with NFA
    rkomurav    01/03/08 - remove stateToAplhmap for classB
    rkomurav    09/27/07 - support non manadatory correlation defs
    anasrini    09/26/07 - ALL MATCHES support
    rkomurav    09/25/07 - prev range
    rkomurav    09/06/07 - add prev index
    rkomurav    07/03/07 - uda
    anasrini    07/02/07 - support for partition by clause
    rkomurav    06/19/07 - add statetoalph map
    rkomurav    06/15/07 - fix calls to init incr evals
    rkomurav    05/14/07 - classB
    rkomurav    05/30/07 - rename map
    anasrini    05/29/07 - create aggregation attributes
    anasrini    05/28/07 - measures support
    rkomurav    03/13/07 - add bindlength
    rkomurav    02/27/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyOptPatternFactory.java /main/21 2010/03/23 01:50:14 udeshmuk Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.factory;

import java.util.ArrayList;

import oracle.cep.exceptions.CEPException;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptPatternStrm;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.phyplan.attr.CorrAttr;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.BoolExpr;
import oracle.cep.phyplan.expr.ExprOrderBy;
import oracle.cep.phyplan.pattern.CorrNameDef;
import oracle.cep.phyplan.pattern.SubsetCorr;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptPatternStrm;
import oracle.cep.phyplan.PhyOptPatternStrmClassB;
import oracle.cep.service.ExecContext;


class PhyOptPatternFactory extends PhyOptFactory
{
  PhyOpt newPhyOpt(Object ctx) throws CEPException
  {
    assert ctx instanceof LogPlanInterpreterFactoryContext;
    LogPlanInterpreterFactoryContext lpctx = 
      (LogPlanInterpreterFactoryContext) ctx;
    ExecContext ec = lpctx.getExecContext();

    LogOpt                                  logop;
    PhyOpt[]                                phyChildren;
    LogOptPatternStrm                       logPattern;
    PhyOpt                                  phyChild;
    oracle.cep.logplan.expr.BoolExpr        expr;
    BoolExpr                                phyExpr;
    int                                     numPartnAttrs;
    oracle.cep.logplan.attr.Attr[]          logPartnAttrs;
    Attr[]                                  phyPartnAttrs;
    int                                     numAggrAttrs;
    oracle.cep.logplan.attr.Attr[]          logAggrAttrs;
    CorrAttr[]                              phyAggrAttrs;
    ArrayList<Expr[]>                       phyAggrParamExprs;
    ArrayList<ExprOrderBy[]>                phyOrderByExprs;
    SubsetCorr[]                            phySubsetCorrs;
    oracle.cep.logplan.pattern.SubsetCorr[] logSubsetCorrs;
    
    logop       = lpctx.getLogPlan();
    phyChildren = lpctx.getPhyChildPlans();

    assert logop != null;
    assert logop instanceof LogOptPatternStrm : logop.getClass().getName();
    logPattern = (LogOptPatternStrm)logop;

    assert logop.getNumInputs() == 1 : logop.getNumInputs();
    assert phyChildren != null;
    assert phyChildren.length == 1 : phyChildren.length;
    phyChild = phyChildren[0];
    assert phyChild != null;
    
    numAggrAttrs   = logPattern.getNumAggrAttrs();
    logAggrAttrs   = logPattern.getAggrAttrs();
    numPartnAttrs  = logPattern.getNumPartnAttrs();
    logPartnAttrs  = logPattern.getPartnAttrs();
    logSubsetCorrs = logPattern.getSubsetCorrs();

    // For now it is only pattern stream operator
    // later when recoginise relation and stream both are separated,
    // corresponding operator needs to be created

    // Convert the PARTITION BY attributes
    if (logPartnAttrs != null)
    {
      phyPartnAttrs = new Attr[numPartnAttrs];
      // create equivalent physical plan attributes for each logical one.
      for (int aidx = 0; aidx < numPartnAttrs ; aidx++)
      {
        phyPartnAttrs[aidx] = 
          LogPlanAttrFactory.getInterpreter(logop, logPartnAttrs[aidx]);
      }
    }
    else 
    {
      phyPartnAttrs = null;
    }

    // Convert the DEFINE clause predicate
    oracle.cep.logplan.pattern.CorrNameDef[] logCorrs = 
      logPattern.getCorrDefs();
    CorrNameDef[] phyCorrDefs = new CorrNameDef[logCorrs.length];
    for(int i = 0; i < logCorrs.length; i++)
    {
      expr    = logCorrs[i].getBExpr();
      // For CLASS A patterns, the predicate can be run on the INPUT_ROLE
      // itself
      // Note that in CLASS A patterns, by definition, the definition
      // of a correlation name does not reference any other correlation
      // name NOR does it include an aggregate
      //
      // So, in this case, convert a logical CorrAttr into a physical Attr.
      // That is, since we will not be looking into the bindings for the
      // predicate evaluation, we do not need to form a physical CorrAttr
      //
      // Based on the above, pass "index 0"
      
      // it doesn't matter if the logical corrattr is converted to
      // physical corrattr or attr as in the instantiation layer, ExprHelper is
      // called with appropriate parameter so that
      // for ClassA, it is considered as Attr
      // for ClassB, it is considered as CorrAttr
      // by converting it to physical CorrAttr, it will be uniform for 
      // both classA and B
      // correaltion definition can be null if there is no definition for a 
      // particular correlation name
      if(expr == null)
        phyExpr = null;
      else
      {
        phyExpr = (BoolExpr)LogPlanExprFactory.getInterpreter(expr,
          new LogPlanExprFactoryContext(expr, logop, phyChildren, true));
      }
      
      phyAggrParamExprs = handleAggrs(logCorrs[i], logop, phyChildren);
      phyOrderByExprs   = handleOrderExprs(logCorrs[i], logop, phyChildren);
      
      phyCorrDefs[i] = new CorrNameDef(phyExpr, logCorrs[i].isSingleton(),
                                       logCorrs[i].getBindPos(),
                                       logCorrs[i].getAggrFns(),
                                       phyAggrParamExprs,
                                       phyOrderByExprs,
                                       logCorrs[i].getSubsetPos());
    }
    
    // handle subset correlations
    if(logSubsetCorrs != null)
    {
      phySubsetCorrs = new SubsetCorr[logSubsetCorrs.length];
      for(int i = 0; i < logSubsetCorrs.length; i++)
      {
        phyAggrParamExprs = handleAggrs(logSubsetCorrs[i], logop,
                                        phyChildren);
        phyOrderByExprs   = handleOrderExprs(logSubsetCorrs[i], logop, 
                                             phyChildren);
        phySubsetCorrs[i] = new SubsetCorr(logSubsetCorrs[i].getBindPos(),
                                           logSubsetCorrs[i].getAggrFns(),
                                           phyAggrParamExprs, 
                                           phyOrderByExprs);
      }
    }
    else
      phySubsetCorrs = null;

    // These need to be propogated since they are used in patternstrmclassbfactory
    // for determining number of aggregations
    phyAggrAttrs = new CorrAttr[numAggrAttrs];
    for(int j = 0; j < numAggrAttrs; j++)
    {
      //transform logical attribute to physical attribute
      phyAggrAttrs[j] = 
        (CorrAttr) LogPlanAttrFactory.getInterpreter(logop, logAggrAttrs[j],
                                                     true);
    }
   
    //process DURATION clause
    Expr phyDurExpr = null;
    if(logPattern.isDurationExpr())
    { //convert duration expr into physical equivalent
      oracle.cep.logplan.expr.Expr logDurExpr = logPattern.getDurationExpr();
      phyDurExpr = LogPlanExprFactory.getInterpreter(logDurExpr,
         new LogPlanExprFactoryContext(logDurExpr, logop, phyChildren, false));
    }
    
    //process MEASURES clause
    oracle.cep.logplan.expr.Expr logMeasures[] = logPattern.getMeasureExprs();
    Expr phyMeasures[] = new Expr[logMeasures.length];
    for(int i=0; i < logMeasures.length; i++)
    {
      phyMeasures[i] = (Expr)LogPlanExprFactory.getInterpreter(logMeasures[i],
      new LogPlanExprFactoryContext(logMeasures[i], logop, phyChildren, true));
    }

    if(logPattern.isClassB())
      return new PhyOptPatternStrmClassB(ec, phyChild, phyPartnAttrs,
                                         phyCorrDefs,
                                         logPattern.getNfa(), 
                                         phyAggrAttrs,
                                         logPattern.getBindLength(),
                                         logPattern.getMaxPrevIndex(),
                                         logPattern.getMaxPrevRange(),
                                         logPattern.isPrevRangeExists(),
                                         logPattern.getSkipClause(),
                                         phySubsetCorrs,
                                         logPattern.isNonEvent(),
					 logPattern.isWithin(),
					 logPattern.isWithinInclusive(),
                                         logPattern.getDurationValue(),
                                         logPattern.getDurationSymAlphIndex(),
                                         logPattern.isRecurringNonEvent(),
                                         logPattern.isDurationExpr(),
                                         phyDurExpr, 
                                         logPattern.getDurationUnit(),
                                         phyMeasures);
    else
      return new PhyOptPatternStrm(ec, phyChild, phyCorrDefs,
                                   logPattern.getDfa(), 
                                   logPattern.getAlphabetToStateMap(), null,
                                   null, logPattern.getBindLength());
  }
  
  //Handle the aggregations for each correlation
  private ArrayList<Expr[]> handleAggrs(
                                   oracle.cep.logplan.pattern.CorrName logCorr,
                                   LogOpt logop, PhyOpt [] phyChildren)
  {
    int                                              numAggrs;
    ArrayList<oracle.cep.logplan.expr.Expr[]>        logAggrParamExprs;
    oracle.cep.logplan.expr.Expr[]                   logExprs;
    Expr[]                                           phyExprs;
    
    ArrayList<Expr[]>                                phyAggrParamExprs;
    
    numAggrs          = logCorr.getNumAggrs();
    logAggrParamExprs = logCorr.getAggrParamExprs();
    
    phyAggrParamExprs = new ArrayList<Expr[]>();    
    for (int j=0; j<numAggrs; j++)
    {
      logExprs = logAggrParamExprs.get(j);
      phyExprs = new Expr[logExprs.length];
      for(int k = 0; k < logExprs.length; k++)
      {
        phyExprs[k] = LogPlanExprFactory.getInterpreter(logExprs[k],
         new LogPlanExprFactoryContext(logExprs[k], logop, phyChildren, true));
      }
      phyAggrParamExprs.add(phyExprs);
    }
    assert logAggrParamExprs.size() == phyAggrParamExprs.size();
    return phyAggrParamExprs;
  }
  
  private ArrayList<ExprOrderBy[]> handleOrderExprs(
    oracle.cep.logplan.pattern.CorrName logCorr, LogOpt logop,
    PhyOpt[] phyChildren)
  {
    ArrayList<ExprOrderBy[]>                         phyOrderExprsList;
    ExprOrderBy[]                                    phyOrderExprs;
    ArrayList<oracle.cep.logplan.expr.ExprOrderBy[]> logOrderExprsList;
    oracle.cep.logplan.expr.ExprOrderBy[]            logOrderExprs;
    
    logOrderExprsList = logCorr.getOrderByExprs();
    phyOrderExprsList = new ArrayList<ExprOrderBy[]>();
    
    //handle orderby exprs
    for(int j=0; j < logCorr.getNumAggrs(); j++)
    {
      logOrderExprs = logOrderExprsList.get(j);
      if(logOrderExprs != null)
      {
        phyOrderExprs = new ExprOrderBy[logOrderExprs.length];
        for(int k=0; k < logOrderExprs.length; k++)
        {
          phyOrderExprs[k] = (ExprOrderBy)LogPlanExprFactory.getInterpreter(
            logOrderExprs[k],
            new LogPlanExprFactoryContext(logOrderExprs[k],logop,
                                          phyChildren, true));
        }
        phyOrderExprsList.add(phyOrderExprs);
      }
      else
        phyOrderExprsList.add(null);
    }
    assert logOrderExprsList.size() == phyOrderExprsList.size();
    return phyOrderExprsList;
  }
}

