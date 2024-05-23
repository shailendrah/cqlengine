/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogPlanAggrHelper.java /main/8 2013/10/16 10:47:04 sbishnoi Exp $ */

/* Copyright (c) 2007, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    Logical Plan Generation. Helper related to aggregations.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    10/10/13 - bug 17321023
    pkali       07/05/13 - add count(*) if no aggr func present (bug 16444297)
    pkali       04/04/12 - included datatype arg in AttrNamed instance
    udeshmuk    06/04/08 - support for xmlagg.
    udeshmuk    04/16/08 - support for aggr distinct
    sbishnoi    08/21/07 - fix bug
    sbishnoi    06/08/07 - support for multi-arg UDAs
    rkomurav    05/28/07 - restructure collectAggrs
    rkomurav    05/28/07 - cleanup
    rkomurav    05/27/07 - 
    anasrini    05/24/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogPlanAggrHelper.java /main/8 2013/10/16 10:47:04 sbishnoi Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan;

import java.util.ArrayList;
import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.common.AggrFunction;
import oracle.cep.common.BaseAggrFn;
import oracle.cep.common.BuiltInAggrFn;
import oracle.cep.logplan.attr.AttrNamed;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprAttr;
import oracle.cep.logplan.expr.ExprOrderBy;
import oracle.cep.logplan.expr.factory.SemQueryExprFactory;
import oracle.cep.logplan.expr.factory.SemQueryExprFactoryContext;
import oracle.cep.semantic.SFWQuery;
import oracle.cep.semantic.AggrExpr;
import oracle.cep.semantic.XMLAggExpr;

/**
 * Logical Plan Generation. Helper related to aggregations.
 */

class LogPlanAggrHelper {

  /**
   * This class should not be instantiated.
   * Contains only static methods.
   */
  private LogPlanAggrHelper() {
  }

  static void addCount(List<BaseAggrFn> fns, List<Expr[]> aggrParamExprs,
                       Expr[] expr, List<Datatype> returnTypesList,
                       List<ExprOrderBy[]> orderByExprsList) {
    fns.add(BuiltInAggrFn.get(AggrFunction.COUNT));
    aggrParamExprs.add(expr);
    if (returnTypesList != null) 
      returnTypesList.add(Datatype.INT);
    if (orderByExprsList != null)
      orderByExprsList.add(null);
  }
  
  static void addCountStar(ArrayList<ArrayList<BaseAggrFn>> distinctFnsList,
                           ArrayList<ArrayList<Datatype>> returnTypesOfDistinctFnsList,
                           ArrayList<Expr[]> nonDistinctExprList,
                           ArrayList<BaseAggrFn> nonDistinctFnsList,
                           ArrayList<Datatype> returnTypeOfNonDistinctFnsList,
                           ArrayList<ExprOrderBy[]> orderByExprsList
                           ) {
    
    //count_star should be added to the function list for every distinct group.
    for (int i=0; i < distinctFnsList.size(); i++)
    {
      ((ArrayList<BaseAggrFn>)distinctFnsList.get(i)).add(BuiltInAggrFn.
                                                      get(AggrFunction.COUNT_STAR));
      ((ArrayList<Datatype>)returnTypesOfDistinctFnsList.get(i)).add(
                                                                 Datatype.INT);
    }

    Expr[] e = new Expr[1];
    e[0]     = new ExprAttr(Datatype.INT, new AttrNamed(0,0, Datatype.INT));
    
    //for the non-distinct group add count_star only if it does not exist earlier and
    //there is at least one non-distinct expr present.
    //The second condition pair, adds count(*) if there is no aggr function 
    //in the select clause
    if (((!existsCountStar(nonDistinctFnsList)) && (nonDistinctExprList.size() > 0))
            || (nonDistinctFnsList.size() == 0 && distinctFnsList.size() == 0))
    { 
      nonDistinctExprList.add(e);
      nonDistinctFnsList.add(BuiltInAggrFn.get(AggrFunction.COUNT_STAR));
      returnTypeOfNonDistinctFnsList.add(Datatype.INT);
      orderByExprsList.add(null);
    }
  }

  static void addSum(List<BaseAggrFn> fns, List<Expr[]> aggrParamExprs,
                     Expr[] expr, List<Datatype> returnTypesList,
                     List<ExprOrderBy[]> orderByExprsList) {
    fns.add(BuiltInAggrFn.get(AggrFunction.SUM));
    aggrParamExprs.add(expr);
    if (returnTypesList != null)
      returnTypesList.add(expr[0].getType());
    if (orderByExprsList != null)
      orderByExprsList.add(null);
  }

  static boolean existsCount(List<BaseAggrFn> fns,
                             List<Expr[]>aggrParamExprs,
                             Expr[] expr) {
    int        numFns = fns.size();
    BaseAggrFn fn;

    for (int i=0; i<numFns; i++) {
      fn = fns.get(i);
      if (isBuiltInAggrFn(fn, AggrFunction.COUNT) &&
          aggrParamExprs.get(i).equals(expr))
        return true;
    }
    return false;
  }
  
  static boolean existsCountStar(List<BaseAggrFn> fns) {
    int        numFns = fns.size();
    BaseAggrFn fn;

    for (int i=0; i<numFns; i++) {
      fn = fns.get(i);
      if (isBuiltInAggrFn(fn, AggrFunction.COUNT_STAR))
      {
        return true;
      }
    }
    return false;
  }

  static boolean existsSum(List<BaseAggrFn> fns, 
                           List<Expr[]>aggrParamExprs, Expr[] expr) {
    int        numFns = fns.size();
    BaseAggrFn fn;

    for (int i=0; i<numFns; i++) {
      fn = fns.get(i);
      if (isBuiltInAggrFn(fn, AggrFunction.SUM) &&
          aggrParamExprs.get(i).equals(expr))
        return true;
    }
    return false;
  }
  
  static boolean isBuiltInAggrFn(BaseAggrFn fn, AggrFunction fnCode) {
    return fn.getFnCode() == fnCode;
  }

  static void collectAggrs(SFWQuery query, oracle.cep.semantic.Expr e, 
                           List<Expr[]> aggrParamExprs, List<BaseAggrFn> aggrFns,
                           ArrayList<Boolean> isDistinctList,
                           ArrayList<Datatype> returnDtList,
                           ArrayList<ExprOrderBy[]> orderByExprsList) 
  {
    ArrayList<AggrExpr> aggrs = new ArrayList<AggrExpr>();
    e.getAllReferencedAggrs(aggrs);
    
    int numAggrs = aggrParamExprs.size();
    boolean bNew;
    BaseAggrFn aggrFn;
    boolean isDistinct = false;
    Datatype returnDt = null;
    for(int i = 0; i < aggrs.size(); i++)
    {
      bNew = true;
      ExprOrderBy[] logOrderByExprs;
      oracle.cep.semantic.Expr[] semAggrParamExprs = aggrs.get(i).getExprs();
      Expr[] logAggrParamExprs = new Expr[semAggrParamExprs.length];
      //convert argument exprs to their logical equivalent forms
      for(int j=0; j < semAggrParamExprs.length ; j++){
        logAggrParamExprs[j] = SemQueryExprFactory.getInterpreter(semAggrParamExprs[j],
          new SemQueryExprFactoryContext(semAggrParamExprs[j], query));
      }
      
      aggrFn     = aggrs.get(i).getAggrFunction();
      isDistinct = aggrs.get(i).getIsDistinctAggr();
      returnDt   = aggrs.get(i).getReturnType();
     
      // Convert the orderby exprs if present to their logical equivalent forms
      if(aggrs.get(i) instanceof XMLAggExpr)
      {
        oracle.cep.semantic.OrderByExpr[] semOrderByExprs = ((XMLAggExpr)aggrs.get(i))
                                                            .getOrderByExprs();
        if(semOrderByExprs != null)
        {
          logOrderByExprs = new ExprOrderBy[semOrderByExprs.length];
        
          for (int k=0; k < semOrderByExprs.length; k++){
            logOrderByExprs[k] = (ExprOrderBy)SemQueryExprFactory.getInterpreter(
              semOrderByExprs[k],new SemQueryExprFactoryContext(semOrderByExprs[k],query));
          }
        }
        else 
          logOrderByExprs = null;
      }
      else 
        logOrderByExprs = null;
      
      Expr[]        tempAggrParamExprs;
      ExprOrderBy[] tempOrderByExprs;
      int           len;
      int           orderByLen;
      boolean       existingExprDistinct;
      //check if this aggr has occurred before
      for (int a=0; a < numAggrs && bNew; a++) 
      {
        tempAggrParamExprs   = aggrParamExprs.get(a);
        existingExprDistinct = isDistinctList.get(a);
        len = tempAggrParamExprs.length;
        if(len == logAggrParamExprs.length && aggrFns.get(a).equals(aggrFn)
           && (isDistinct == existingExprDistinct))
        {
          boolean areParamsIdentical = true;
          for(int t=0; t < len && areParamsIdentical; t++) {
            areParamsIdentical = 
              areParamsIdentical && (tempAggrParamExprs[t].equals(logAggrParamExprs[t]));
          }
          //Compare order by exprs if present and if params are identical
          boolean areOrderByExprsSame = true;
          if((aggrs.get(i) instanceof XMLAggExpr) && areParamsIdentical)
          {
            tempOrderByExprs = orderByExprsList.get(a);
            if(tempOrderByExprs == null)
            { //the aggr in the list is either not XMLAggExpr or
              //even though it is XMLAggExpr it does not have orderByExprs
              areOrderByExprsSame = (((XMLAggExpr)aggrs.get(i)).getOrderByExprs()
                                    == null);
            }
            else
            { // aggr in the list has orderby exprs
              if(((XMLAggExpr)aggrs.get(i)).getOrderByExprs() != null)
              { // the current aggr also has orderby exprs so compare
                orderByLen = tempOrderByExprs.length;
                if(orderByLen == logOrderByExprs.length) {
                  for(int t=0; t < orderByLen && areOrderByExprsSame; t++) {
                    areOrderByExprsSame = 
                      areOrderByExprsSame && (tempOrderByExprs[t].equals(
                                              logOrderByExprs[t]));
                  }
                }
                else
                  areOrderByExprsSame = false;
              }
              else
              { // the current aggr does not hv orderby exprs but 
                // the one in the list has, so they are different
                areOrderByExprsSame = false;
              }
            }
          }
          /*
           * ---------------------------------------------------------
           *  allParamsIdentical  |  areOrderByExprsSame  |   bNew
           *  --------------------------------------------------------
           *       true           |       true            |   false (since everything is same)
           *       true           |       false           |   true
           *       false          |       true            |   true
           *       false          |       false           |   true
           *  --------------------------------------------------------
           */
          bNew = !(areParamsIdentical && areOrderByExprsSame);
        }
      }

      // New aggregation: add it to the list
      if (bNew)
      {
        aggrParamExprs.add(logAggrParamExprs);
        aggrFns.add(aggrFn);
        isDistinctList.add(isDistinct);
        returnDtList.add(returnDt);
        orderByExprsList.add(logOrderByExprs);
      }
    }
  }

  static void augmentNonDistinctAggrs(List<BaseAggrFn> aggrFns,
                                      List<Expr[]> aggrParamExprs,
                                      List<Datatype> returnTypesList,
                                      List<ExprOrderBy[]> orderByExprsList) 
  {
    BaseAggrFn fn;
    int        actualAggrCount = aggrParamExprs.size();

    for(int i = 0; i < actualAggrCount; i++) {
      fn = aggrFns.get(i);
      if(isBuiltInAggrFn(fn, AggrFunction.AVG)) {
        if(!existsCount(aggrFns, aggrParamExprs, aggrParamExprs.get(i)))
          addCount(aggrFns, aggrParamExprs, aggrParamExprs.get(i), returnTypesList,
                   orderByExprsList);
        
        if(!existsSum(aggrFns, aggrParamExprs, aggrParamExprs.get(i)))
          addSum(aggrFns, aggrParamExprs, aggrParamExprs.get(i), returnTypesList,
                 orderByExprsList);
      }
    }
  }
  
  /**
   * This method is used to augment existing set of aggregate functions
   * with any other supporting aggregate functions that may be required
   * to support their implementation.
   * <p>
   * For example, SUM and COUNT are needed for incremental computation of 
   * AVG (average)
   */
  static void augmentAggrs(List<BaseAggrFn> aggrFns,
                           List<Expr[]> aggrParamExprs)
  {
    BaseAggrFn fn;
    int        actualAggrCount = aggrParamExprs.size();

    for(int i = 0; i < actualAggrCount; i++) {
      fn = aggrFns.get(i);
      if(isBuiltInAggrFn(fn, AggrFunction.AVG)) {
        if(!existsCount(aggrFns, aggrParamExprs, aggrParamExprs.get(i)))
          addCount(aggrFns, aggrParamExprs, aggrParamExprs.get(i), null, null);
    
      if(!existsSum(aggrFns, aggrParamExprs, aggrParamExprs.get(i)))
        addSum(aggrFns, aggrParamExprs, aggrParamExprs.get(i), null, null);
      }
    }
  }
  
  static int alreadyExistsInDistinctList(Expr[] expr, ArrayList<Expr[]> distinctExprList)
  {
    if (distinctExprList != null) 
    {
      for (int i=0; i < distinctExprList.size(); i++)
      {
        Expr[] exprList = distinctExprList.get(i);
        boolean match   = true;
        for (int j=0; j < exprList.length; j++)
        {
          if (!(expr[j].equals(exprList[j])))
          {
            match = false; 
            break; 
          }
        }
        if (match) return i;
      }
    }
    return -1;
  }

  static void augmentDistinctAggrs(
      ArrayList<Expr[]> distinctExprsList,
      ArrayList<ArrayList<BaseAggrFn>> aggrFnsPerDistinctExprList,
      ArrayList<ArrayList<Datatype>> returnTypesPerDistinctExprList)
  {
    ArrayList<BaseAggrFn> fnsList;
    ArrayList<Datatype> returnList;
    for (int i=0; i < distinctExprsList.size(); i++)
    {
      fnsList = (ArrayList<BaseAggrFn>)aggrFnsPerDistinctExprList.get(i);
      returnList = (ArrayList<Datatype>)returnTypesPerDistinctExprList.get(i);
      for (int j=0; j < fnsList.size(); j++)
      {
        BaseAggrFn fn = fnsList.get(j);
        if(isBuiltInAggrFn(fn, AggrFunction.AVG)) {
          if(!existsCountInDistinct(fnsList))
          {
            fnsList.add(BuiltInAggrFn.get(AggrFunction.COUNT));
            returnList.add(Datatype.INT);
          }
          if(!existsSumInDistinct(fnsList))
          {
            fnsList.add(BuiltInAggrFn.get(AggrFunction.SUM));
            //in case of sum return type is same as the datatype of argument
            returnList.add((distinctExprsList.get(i))[0].getType());
          }
        }
      }
    }
    
  }

  private static boolean existsSumInDistinct(ArrayList<BaseAggrFn> fnsList)
  {
    BaseAggrFn fn;
    for (int i=0; i < fnsList.size(); i++)
    {
      fn = fnsList.get(i);
      if (isBuiltInAggrFn(fn, AggrFunction.SUM))
        return true;
    }
    return false;
  }

  private static boolean existsCountInDistinct(ArrayList<BaseAggrFn> fnsList)
  {
    BaseAggrFn fn;
    for (int i=0; i < fnsList.size(); i++)
    {
      fn = fnsList.get(i);
      if (isBuiltInAggrFn(fn, AggrFunction.COUNT))
        return true;
    }
    return false;
  }

}
