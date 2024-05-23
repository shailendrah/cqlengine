/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogPlanGen.java /main/45 2014/01/03 05:23:32 sbishnoi Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Logical Operator Plan Generator

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    12/20/13 - bug 17600010
 sbishnoi    05/25/12 - support of slide without window
 udeshmuk    05/12/12 - set a boolean in LogOptGroupAggr to indicate at least
                        one expression in group by
 pkali       03/29/12 - support for groupby expression
 vikshukl    09/27/11 - subquery and set operations
 vikshukl    08/24/11 - subquery support
 anasrini    07/01/11 - support for onlyValidate mode
 alealves    06/21/11 - Support for concurrent views
 alealves    06/21/11 - XbranchMerge alealves_bug-12584321_cep from main
 anasrini    04/25/11 - XbranchMerge anasrini_bug-11905834_ps5 from main
 anasrini    03/16/11 - partition parallelism
 vikshukl    03/14/11 - support for n-ary set operators
 vikshukl    09/03/09 - support for ISTREAM (R) DIFFERENCE USING (...)
 parujain    05/20/09 - ansi outer join support
 sbishnoi    03/17/09 - adding order by expressions in the project clause
 sbishnoi    03/06/09 - adding support to process partition by inside order by
                        clause
 sbishnoi    02/09/09 - support for top
 parujain    11/21/08 - handle constants
 hopark      10/09/08 - remove statics
 udeshmuk    06/04/08 - support for xmlagg
 udeshmuk    04/16/08 - support for aggr distinct.
 udeshmuk    10/30/07 - use new constructor for NOT IN and In.
 udeshmuk    10/20/07 - For IN setting comparison attrlist for outer
                        logoptminus.
 udeshmuk    10/04/07 - create separate functions for intersect and in.
 udeshmuk    10/03/07 - change signature of getTransformedExpr.
 udeshmuk    10/01/07 - implementation of intersect and in operators.
 sbishnoi    09/26/07 - support for minus
 sbishnoi    09/16/07 - support for NOTIN set operation
 anasrini    08/27/07 - add project over SetOp queries
 anasrini    07/02/07 - transformAttr cleanup
 parujain    06/27/07 - order by support
 sbishnoi    06/08/07 - support for multi-arg UDAs
 rkomurav    05/14/07 - classB
 anasrini    05/30/07 - get varid for binary op from SetOpQuery
 anasrini    05/24/07 - split into multiple files
 sbishnoi    05/10/07 - support for distinct
 sbishnoi    05/02/07 - support for having
 sbishnoi    03/23/07 - support for union all
 rkomurav    04/02/07 - fix calculation of final states
 rkomurav    03/13/07 - add bindlength to logoptpattern
 parujain    03/07/07 - Extensible windows
 rkomurav    02/27/07 - rename patternopt
 rkomurav    02/16/07 - pattern changes
 rkomurav    12/13/06 - count(*) vs count(expr) differentiataion
 hopark      12/06/06 - add validate
 rkomurav    11/27/06 - remove the project optimisation at the logical level
                        and push it to the stage of optimisation
 parujain    11/03/06 - Tree representation for conditions
 rkomurav    10/10/06 - expressions in aggregation
 rkomurav    09/22/06 - expr in aggr
 najain      09/29/06 - support multi-way join
 rkomurav    09/18/06 - bug 5446939
 anasrini    08/23/06 - fix XStream(binop) bug
 dlenkov     07/28/06 - 
 anasrini    07/12/06 - support for user defined aggregations 
 dlenkov     06/09/06 - support for union and except
 anasrini    06/05/06 - temporarily comment out t_pushSelect 
 najain      05/30/06 - more optimizations 
 najain      05/25/06 - stream join 
 najain      05/25/06 - various optimizations 
 dlenkov     05/19/06 - fix for ROW window
 ayalaman    04/29/06 - DStream implementation 
 ayalaman    04/23/06 - log plan for IStream operator 
 anasrini    04/20/06 - support for GROUP BY/Aggregation operator 
 najain      04/13/06 - bug fix
 anasrini    04/04/06 - support for rel to stream operators 
 najain      04/06/06 - cleanup
 anasrini    03/29/06 - support for Select operator 
 najain      02/21/06 - projections
 anasrini    02/21/06 - timeUnits should be long 
 najain      02/13/06 - add more stuff
 najain      02/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogPlanGen.java /main/45 2014/01/03 05:23:32 sbishnoi Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan;

import java.util.ArrayList;
import java.util.ListIterator;

import oracle.cep.common.BaseAggrFn;
import oracle.cep.common.Datatype;
import oracle.cep.common.RelSetOp;
import oracle.cep.common.RelToStrOp;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.LogicalPlanError;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrGroupBy;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprAttr;
import oracle.cep.logplan.expr.ExprOrderBy;
import oracle.cep.logplan.expr.factory.SemQueryExprFactory;
import oracle.cep.logplan.expr.factory.SemQueryExprFactoryContext;
import oracle.cep.logplan.factory.SemQueryInterpreterFactory;
import oracle.cep.logplan.factory.SemQueryInterpreterFactoryContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.Query;
import oracle.cep.semantic.BExpr;
import oracle.cep.semantic.DerivedTimeSpec;
import oracle.cep.semantic.GenericSetOpQuery;
import oracle.cep.semantic.GroupByExpr;
import oracle.cep.semantic.SFWQuery;
import oracle.cep.semantic.SemQuery;
import oracle.cep.semantic.SetOpQuery;
import oracle.cep.semantic.SetOpSubquery;
import oracle.cep.service.ExecContext;

/**
 * Generates logical query plan from a declarative representation of a CQL
 * query.
 * <p>
 * This represents the third phase of converting the query string into the
 * actual execution units:
 *
 * CQL query string --> parse tree representation ---> Query ---> LogOp tree -->
 * PhyOp tree ---> Execution units .
 *
 * @since 1.0
 */
public class LogPlanGen
{
  /**
   * Generates Logical plan from a Query tree. 
   * @param ec ExecContext corresponding to this service
   * @param query
   *          (Input) parsed, internal representation of CQL query
   * @param q
   *           Metadata layer representation of the query 
   * @param onlyValidate
   *           Generate logical plan only for validation. For example
   *           parallelism analysis is not required
   * @return Logical Operator tree
   */
  public LogOpt genLogPlan(ExecContext ec, SemQuery query, Query q, 
                           boolean onlyValidate)
    throws CEPException
  {
    // Generate naive plan
    LogOpt queryPlan = genPlan_n(ec, query);

    // Perform the transformations
    queryPlan = LogPlanTransformHelper.doTransforms(queryPlan);

    // Validations -- do this before determining parallelism
    validate(queryPlan);   

    // Skip the parallelism analysis if the purpose of generating
    // logical plan is only for validation
    if (onlyValidate)
      return queryPlan;

    // Determine Parallelism
    LogUtil.fine(LoggerType.TRACE, "User-defined ordering setting for " 
                 + q.getName() + " is "
                 + q.getUserOrderingConstraint());

    // Convert the partition parallel expression if it exists
    oracle.cep.semantic.Expr ppExpr = query.getPartitionParallelExpr();
    if (ppExpr != null)
    {
      Expr logExpr = 
        SemQueryExprFactory.getInterpreter(ppExpr,
            new SemQueryExprFactoryContext(ppExpr, query));
      queryPlan.setPartitionParallelExpr(logExpr);
    }
    
    // Need to check even for the ordered case, as it needs to be validated.
    queryPlan = LogPlanParallelismHelper.determineParallelism(ec, q, 
        queryPlan);
    
    return queryPlan;
  }

  public LogOpt genLogPlan(ExecContext ec, SemQuery query)
  throws CEPException
  {
    // generate a naive plan
    LogOpt queryPlan = genPlan_n(ec, query);

    // perform the transformations
    queryPlan = LogPlanTransformHelper.doTransforms(queryPlan);

    // validations -- do this before determining parallelism
    validate(queryPlan);    

    // REVIEW: no parallelism support with subqueries for now as 
    // the annotation is for the whole query. Maybe we can support
    // it if hints are embedded in the subquery?
    // Convert the partition parallel expression if it exists
    oracle.cep.semantic.Expr ppExpr = query.getPartitionParallelExpr();
    if (ppExpr != null)
    {
      Expr logExpr = 
        SemQueryExprFactory.getInterpreter(ppExpr,
            new SemQueryExprFactoryContext(ppExpr, query));
      queryPlan.setPartitionParallelExpr(logExpr);
    }

    // Need to check even for the ordered case, as it needs to be validated.
    queryPlan = 
        LogPlanParallelismHelper.determineParallelism(ec, null, 
                                                      queryPlan);

    return queryPlan;
  }  
  
  private void validate(LogOpt logPlan) throws LogicalPlanException
  {
    assert logPlan != null;

    // root operator in the logical plan
    LogOpt rootOp = logPlan;

    logPlan.validate();
    for (int i = 0; i < rootOp.getNumInputs(); i++) {
      validate(rootOp.getInput(i));
    }      
  }

  /**
   * Generate a naive query plan from a query:
   * @param ec Execution context
   * @param query
   *          Input query
   * 
   * @return Output plan
   */
  private LogOpt genPlan_n(ExecContext ec, SemQuery query)
    throws LogicalPlanException, MetadataException
  {
    LogOpt plan = null;
    
    switch (query.getQueryType()) {
    case SFW_QUERY:
    {
      plan = genSFWPlan_n(ec, (SFWQuery) query);
      break;
    }
    case BINARY_OP:
    {
      plan = genBinOpPlan_n(ec, query);
      break;
    }
    case NARY_OP:
    {
      plan = genNaryOpPlan_n(ec, query);
      break;
    }
    case SET_SUBQUERY:
    {
      plan = genSetOpSubqueryPlan_n(ec, query);
      break;
    }
    default:
      assert false;
    }
      
    assert plan != null; // if we reach here then plan can't be null    
    return plan;
  }
  
  private LogOpt applyEvaluate_n(SemQuery query, LogOpt input)
  {
    // Check if Evaluate Clause exists
    if(query.isEvaluateExists())
    {
      // Get the specified slide evaluation interval
      long numSlideNanos = query.getSlideInterval();
      
      // Create & Initialize new logical Slide operator
      LogOpt slide = new LogOptSlide(input, numSlideNanos);
      
      // Return the new operator as the root      
      return slide;
    }
    else
      return input;
  }

  /**
   * Generate a naive plan for an n-ary set operator tree
   * @param ec 
   *        Execution Context
   * @param query
   *        Post semantic query representation
   * @param plan
   *        Output Logical plan for the query
   * @throws CEPException 
   */

  private LogOpt genSetOpSubqueryPlan_n(ExecContext ec, 
                                        SemQuery query) 
    throws LogicalPlanException
  {
    boolean isUnionAll;
    RelSetOp op;
    LogOpt left, right, ret=null;
    
    assert query instanceof SetOpSubquery;
    SetOpSubquery setQuery = (SetOpSubquery)query;
    isUnionAll = setQuery.isUnionAll();
    op = setQuery.getRelSetOp();
    
    try {
      left = genLogPlan(ec, setQuery.getLeft());      
      // now stick a subquery node on top of it
      // alias and varid do not exist (and matter) in case of set operations
      LogOptSubquerySrc subquery = new
        LogOptSubquerySrc(left, null, 0);      
      left = subquery;
    }
    catch (CEPException ce) {
      throw new LogicalPlanException(LogicalPlanError.LOGOPT_NOT_CREATED);
    }
    
    try {
      right = genLogPlan(ec, setQuery.getRight());
      // now stick a subquery node on top of it
      LogOptSubquerySrc subquery = new
        LogOptSubquerySrc(right, null, 0);      
      right = subquery;
    }
    catch (CEPException ce) {
      throw new LogicalPlanException(LogicalPlanError.LOGOPT_NOT_CREATED);
    }
    
    switch (op)
    {
      case UNION:
        ret = new LogOptUnion (left, right, isUnionAll);
        break;
      case EXCEPT:
        ret = new LogOptExcept(left, right);
        break;
      case MINUS:
        ret = new LogOptDistinct(new LogOptMinus(left, right));
        break;
      case INTERSECT:
        ret = handleIntersect(left, right);
        break;
      default:
        assert false;        
    }
    
    // apple project operator
    LogOpt project = doProjs_n(query, ret);
    
    // Apply EVALUATE EVERY <time_spec> clause (if exists)
    LogOpt evaluate = applyEvaluate_n(query, project);
    
    // apple r2s operator is any
    LogOpt r2s = applyR2SOps_n(query, evaluate);
    
    return r2s;
  }
  
  
  
  /**
   * Generate a naive plan for an n-ary set operator tree
   * @param ec 
   *        Execution Context
   * @param query
   *        Post semantic query representation
   * @param plan
   *        Output Logical plan for the query
   */

  private LogOpt genNaryOpPlan_n(ExecContext ec, SemQuery query) 
        throws LogicalPlanException {
    RelSetOp setop;
    int tableId;
    int varId;
    LogOpt left;
    LogOpt right;
    LogOpt ret;
    boolean isStream;
    boolean isUnionAll;
    
    assert query instanceof GenericSetOpQuery;
    GenericSetOpQuery setQuery = (GenericSetOpQuery)query;
        
    // set operators all have the same precedence and they are processed from
    // left to right.
    varId = setQuery.getLeft();
    tableId = setQuery.getTableId(varId);
        
    try 
    {
      isStream = ec.getSourceMgr().isStream(tableId);
      SemQueryInterpreterFactoryContext lCtx
        = new 
            SemQueryInterpreterFactoryContext(ec, query, varId, tableId);
      // Set the DerivedTsSpec if the left source is derived timestamped
      lCtx.setDerivedTimeSpec(setQuery.getDerivedTsSpec(tableId));
      left = SemQueryInterpreterFactory.getInterpreter(isStream,lCtx);
    }
    catch (CEPException ex) 
    {
      throw new LogicalPlanException(LogicalPlanError.LOGOPT_NOT_CREATED);
    }
        
    ret = null;
    
    // now iterate over each of the right operands (tables/views) and create
    // a LogOpt. Iteratively combine that LogOpt with the subsequent
    // right operand to create a LogOpt tree.
    for (int i=0; i < setQuery.getOperands().size(); i++) 
    {
      // is this a UNION ALL operation?
      varId = setQuery.getOperands().get(i);
      tableId = setQuery.getTableId(varId); 
      
      // get the type of set operation
      setop = setQuery.getOperators().get(i);
      isUnionAll = setQuery.getIsUnionAll().get(i);
      
      try {
        isStream = ec.getSourceMgr().isStream(tableId);
        SemQueryInterpreterFactoryContext rctx 
          = new SemQueryInterpreterFactoryContext(ec, query, varId, tableId);
        // Set the DerivedTsSpec if the left source is derived timestamped
        rctx.setDerivedTimeSpec(setQuery.getDerivedTsSpec(tableId));
        right = SemQueryInterpreterFactory.getInterpreter(isStream, rctx);
      }
      catch (CEPException ex) {
        throw new LogicalPlanException(LogicalPlanError.LOGOPT_NOT_CREATED);
      }

      switch (setop)
      {
      case UNION:
        left = new LogOptUnion(left, right, isUnionAll);
        ret = left;
        break;
      case EXCEPT: 
        left = new LogOptExcept(left, right);
        ret = left;
        break;
      case MINUS:
        left = new LogOptDistinct(new LogOptMinus(left, right));
        ret = left;
        break;
      case INTERSECT:
        left = handleIntersect(left, right);
        ret = left;
        break;
      default:  
        assert false;      
      }
    }
        
    // Apply project operator
    LogOpt project = doProjs_n(query, ret);
    
    // Apply EVALUATE EVERY <time_spec> clause (if exists)
    LogOpt evaluate = applyEvaluate_n(query, project);
    
    // Apply R2S (relation-to-stream) operator if present
    LogOpt r2s = applyR2SOps_n(query, evaluate);

    return r2s;         
  }

  /**
   * Generate a naive plan for a binary operator query
   * @param ec Execution Context
   * @param query
   *          Input query
   * @param plan
   *          Output plan for the query
   */
  private LogOpt genBinOpPlan_n (ExecContext ec, SemQuery query) 
        throws LogicalPlanException {
    RelSetOp qKind;
    int tableId;
    int varId;
    LogOpt leftSource;
    LogOpt rightSource;
    LogOpt ret;
    boolean isStream;
    boolean isUnionAll;

    assert query instanceof SetOpQuery;
    SetOpQuery sq = (SetOpQuery)query;

    varId = sq.getLeftVarId();
    tableId = sq.getTableId(varId);

    isUnionAll = sq.isUnionAll();
    
    try {
      isStream = ec.getSourceMgr().isStream( tableId);
      leftSource = SemQueryInterpreterFactory.getInterpreter( isStream,
                  new SemQueryInterpreterFactoryContext(ec, query, varId, tableId));
    }
    catch (CEPException ex) {
      throw new LogicalPlanException(LogicalPlanError.LOGOPT_NOT_CREATED);
    }

    varId = sq.getRightVarid();
    tableId = sq.getTableId(varId);
    try {
      isStream = ec.getSourceMgr().isStream( tableId);
      rightSource = SemQueryInterpreterFactory.getInterpreter( isStream,
                   new SemQueryInterpreterFactoryContext(ec, query, varId, tableId));
    }
    catch (CEPException ex) {
      throw new LogicalPlanException( LogicalPlanError.LOGOPT_NOT_CREATED);
    }

    ret = null;
    qKind = ((SetOpQuery)query).getRelSetOp();
    
    switch (qKind)
    {
    case UNION:
      ret = new LogOptUnion(leftSource, rightSource, isUnionAll);
      break;
    case EXCEPT: 
      ret = new LogOptExcept(leftSource, rightSource);
      break;
    case MINUS:
      ret = new LogOptDistinct(new LogOptMinus(leftSource, rightSource));
      break;
    case NOT_IN:
      ret = new LogOptMinus(leftSource, rightSource, sq.getLeftComparisonAttrs(),
                  sq.getRightComparisonAttrs(), sq.getNumComparisonAttrs());
      break;
    case INTERSECT:
      ret = handleIntersect(leftSource, rightSource); 
      break;
    case IN:
      ret = handleIn(leftSource, rightSource, sq);
      break;
    default:  
      assert false;      
    }
  
    // Apply project operator
    LogOpt project = doProjs_n(query, ret);

    // Apply EVALUATE EVERY <time_spec> clause (if exists)
    LogOpt evaluate = applyEvaluate_n(query, project);
    
    // Apply R2S operators if present ...
    LogOpt r2s = applyR2SOps_n(query, evaluate);

    return r2s;
  }

  /**
   * Generate a naive plan for a Select From Where query
   * @param ec Execution context
   * @param query
   *          Input query
   * @param plan
   *          Output plan for the query
   */
  private LogOpt genSFWPlan_n(ExecContext ec, SFWQuery query)
    throws LogicalPlanException, MetadataException
  {

    // Generate a plan that joins the FROM clause tables
    LogOpt join = joinTables_n(ec, query);

    // Apply WHERE clause predicates over the join
    LogOpt select = applyPreds_n(query, join);

    // Apply Aggregations & perform group by if necessary
    LogOpt aggr = applyAggr_n(query, select);
    
    // Apply Having Clause predicates
    LogOpt having = applyHavingPreds_n(query, aggr);

    // Perform projections specified in SELECT clause
    LogOpt project = doProjs_n(query, having);
    
    // Apply order by Clause(optional)
    LogOpt orderby = applyOrderBy_n(query, project);

    // Apply Distinct operator if needed
    LogOpt distinct = applyDistinct_n(query, orderby);
    
    // Apply EVALUATE EVERY <time_spec> clause (if exists)
    LogOpt evaluate = applyEvaluate_n(query, distinct);
    
    // Apply R2S operators if present ...
    LogOpt r2s = applyR2SOps_n(query, evaluate);

    

    // ... which is the final plan
    return r2s;
  }

  /**
   * Generate a naive plan that joins all the tables in the from clause. Some
   * tables can be streams with windows: it generates the window operators as
   * required.
   * pattern clause is processed and pattern operator is added
   * @param ec Execution context
   * @param query
   *          Input query
   * 
   * @return Output plan that joins the tables
   */
  private LogOpt joinTables_n(ExecContext ec, SFWQuery query)
    throws LogicalPlanException, MetadataException
  {
    LogOpt ret;

    // number of from clause entries
    int numFromClauseEntries = query.getNumFromClauseEntries();

    assert numFromClauseEntries > 0;

    // More than one from clause entry: Construct a LogOptCross to join all
    // from clause entries
    if (numFromClauseEntries > 1)
    {
      // n-way cross product
      LogOptCross cross = new LogOptCross();

      // For each table
      for (int t = 0; t < numFromClauseEntries; t++)
      {
        LogOpt op = LogPlanFromHelper.processFromClauseEntry(              
                      ec, 
                      query.getFromClauseEntries().get(t), 
                      query);
        
        cross.addInput(op);
      }
      ret = cross;
    }
    else
    {
        ret = LogPlanFromHelper.processFromClauseEntry(ec,
                                   query.getFromClauseEntries().get(0), query);
    }

    return ret;
  }

  /**
   * Apply predicates that are present in the WHERE clause of a query to an
   * input subplan.
   * 
   * @param query
   *          Query for which we are generating plan
   * @param join
   *          Partial query plan (input)
   * 
   * @return Query plan which applies WHERE clause predicates
   */
  private LogOpt applyPreds_n(SFWQuery query, LogOpt join)
  {
    BExpr predicates = query.getPredicate();
    
    if (predicates == null)
      return join;
    else
      return LogPlanHelper.applyPredsHelper_n(query, join, predicates);
    
  }
  /**
   * Apply Aggregations & perform group by if necessary
   * 
   * @param query
   *          Query for which we are generating plan
   * @param select
   *          Partial query plan (input)
   * @return Query plan with aggregation operator if needed.
   */
  private LogOpt applyAggr_n(SFWQuery query, LogOpt select)
    throws LogicalPlanException {

    ArrayList<Expr[]>           aggrParamExprs   = new ArrayList<Expr[]>();
    ArrayList<BaseAggrFn>       aggrFns          = new ArrayList<BaseAggrFn>();
    ArrayList<Boolean>          isDistinctList   = new ArrayList<Boolean>();
    ArrayList<Datatype>         returnDtList     = new ArrayList<Datatype>();
    ArrayList<ExprOrderBy[]>    orderByExprsList = new ArrayList<ExprOrderBy[]>();
    int                         numProjExprs;
    ArrayList<oracle.cep.semantic.Expr> projExprs;
    BExpr                       havingExpr;
    Expr                        groupExpr;
    oracle.cep.semantic.Expr[]  gbyExprs;
    int                         actualAggrCount;
    LogOptGrpAggr               aggrOp;

    projExprs     = query.getSelectListExprs();
    numProjExprs  = projExprs.size();
    gbyExprs      = query.getGroupByExprs();
       
    havingExpr    = query.getHavingPredicate();
    
    // Compute the aggregations occurring the SELECT clause: 
    // We need this information to initialize the aggregation operator.
    for(int i =0; i < numProjExprs; i++)  
      LogPlanAggrHelper.collectAggrs(query, projExprs.get(i), aggrParamExprs,
                                     aggrFns, isDistinctList, returnDtList,
                                     orderByExprsList);
    
    //Collect aggregations occurring in Having Clause;
    if(havingExpr != null)
      LogPlanAggrHelper.collectAggrs(query, havingExpr, aggrParamExprs, aggrFns,
                                     isDistinctList, returnDtList, orderByExprsList);
    
    actualAggrCount = aggrParamExprs.size();
    
    // We do not need an aggregation operator if the group by clause is
    // empty and there are no aggregations in the SELECT & HAVING clause
    assert aggrFns.size() == actualAggrCount : "aggrs = " + actualAggrCount +
                                           " : " +"fns = " + aggrFns.size();   
    assert aggrFns.size() == isDistinctList.size() : "isDistinctList = " 
      + isDistinctList.size() + ":" + "fns = " + aggrFns.size();
    
    if (actualAggrCount == 0 && gbyExprs.length == 0) {
      return select;
    }
    
    boolean isContainGroupByExpression = false;
    for( int i = 0; i < gbyExprs.length; i++)
    {
      if( gbyExprs[i] instanceof oracle.cep.semantic.GroupByExpr)
      {
        isContainGroupByExpression= true;
        break;
      }
    }
    
    if(isContainGroupByExpression)
    {
      LogOptProject projLogOpt = insertProjectForGroupByExpr( query, select);
      select = projLogOpt; //set projLogOpt as input for GroupBy
    }
    
    ArrayList<Expr[]> distinctExprList = new ArrayList<Expr[]>();
    ArrayList<ArrayList <BaseAggrFn>> aggrFnsPerDistinctExprList = 
      new ArrayList<ArrayList <BaseAggrFn>>();
    ArrayList<ArrayList <Datatype>> returnTypesPerDistinctExprList =
      new ArrayList<ArrayList <Datatype>>();
    ArrayList<Expr[]> nonDistinctExprList = new ArrayList<Expr[]>();
    ArrayList<BaseAggrFn> aggrFnForNonDistinctExprList = new ArrayList<BaseAggrFn>();
    ArrayList<Datatype> returnTypeForNonDistinctExprList = new ArrayList<Datatype>();
    ArrayList<ExprOrderBy[]> orderByExprsForNonDistinctExprList = 
      new ArrayList<ExprOrderBy[]>();
    ArrayList<BaseAggrFn> aggrFnsList;
    ArrayList<Datatype> returnTypesList;
    
    for (int i=0; i < actualAggrCount; i++)
    {
      int index = 0;
      aggrFnsList = null;
      
      if ((boolean)isDistinctList.get(i))
      { //check if the expr is distinct
        
        Expr[] params = (Expr[])aggrParamExprs.get(i);
       
        assert params.length == 1 : "params length for distinct aggr expr" 
          + params.length;
        
        if ((index=LogPlanAggrHelper.alreadyExistsInDistinctList(params,
          distinctExprList)) == -1)
        { 
          // new param expr for aggregation function.
          // add it to distinct list and also create a list of functions which have
          // this param expr as a parameter also create a list for return type
          distinctExprList.add(params);
          aggrFnsList = new ArrayList<BaseAggrFn>();
          aggrFnsList.add((BaseAggrFn)aggrFns.get(i));
          aggrFnsPerDistinctExprList.add(aggrFnsList);
          returnTypesList = new ArrayList<Datatype>();
          returnTypesList.add(returnDtList.get(i));
          returnTypesPerDistinctExprList.add(returnTypesList);
        }
        else
        { // already the param expr exists in distinct list.
          // just add the current AggrFn to the end of the list of AggrFns in which
          // this param expr appears. also the return type.
          aggrFnsList = (ArrayList<BaseAggrFn>)aggrFnsPerDistinctExprList.get(index);
          aggrFnsList.add((BaseAggrFn)aggrFns.get(i));
          returnTypesList = (ArrayList<Datatype>)returnTypesPerDistinctExprList.get(index);
          returnTypesList.add(returnDtList.get(i));
        }
      }
      else
      { // not a distinct aggr expr
        // xmlagg can occur here only so need to populate orderByExprs list also
        nonDistinctExprList.add((Expr[])aggrParamExprs.get(i));
        aggrFnForNonDistinctExprList.add((BaseAggrFn)aggrFns.get(i));
        returnTypeForNonDistinctExprList.add(returnDtList.get(i));
        orderByExprsForNonDistinctExprList.add(orderByExprsList.get(i));
      }
    }
    
    // Implicitly add some aggregation expressions in some cases
    
    // If the input to a gby aggr. operator is not a stream we need a
    // maintain a count aggr. within the operator to determine when a
    // group no longer exists.
    if (!(select.getIsStream())) 
      LogPlanAggrHelper.addCountStar(aggrFnsPerDistinctExprList,
          returnTypesPerDistinctExprList,nonDistinctExprList,
          aggrFnForNonDistinctExprList, returnTypeForNonDistinctExprList,
          orderByExprsForNonDistinctExprList);
   
    // Need SUM and COUNT for incremental computation of average
    LogPlanAggrHelper.augmentDistinctAggrs(distinctExprList,
      aggrFnsPerDistinctExprList, returnTypesPerDistinctExprList);
    LogPlanAggrHelper.augmentNonDistinctAggrs(aggrFnForNonDistinctExprList, 
      nonDistinctExprList, returnTypeForNonDistinctExprList,
      orderByExprsForNonDistinctExprList);
        
    // Create the aggregation operator:
    aggrOp = new LogOptGrpAggr(select);
    if(isContainGroupByExpression)
      aggrOp.setContainGroupByExpr(isContainGroupByExpression);

    // specify the grouping attributes to the operator:
    for (int i = 0; i < gbyExprs.length ; i++)
    {
      groupExpr = SemQueryExprFactory.getInterpreter(gbyExprs[i],
                         new SemQueryExprFactoryContext(gbyExprs[i], query));
      aggrOp.addGroupByAttr(groupExpr.getAttr());
      aggrOp.addGroupByExpr(groupExpr);
    }
    
    // Specify the aggregations to the operator:
    for (int a=0; a < distinctExprList.size() ; a++) {
      ArrayList<BaseAggrFn> fnsList = aggrFnsPerDistinctExprList.get(a);
      ArrayList<Datatype> returnList = returnTypesPerDistinctExprList.get(a);
      for (int b=0; b < fnsList.size(); b++)
      {
        // the orderByExprs argument will be null here as xmlagg can't be called with distinct
        aggrOp.addAggr(fnsList.get(b), distinctExprList.get(a), true, 
                       returnList.get(b), null);
      }
    }
  
    for (int a=0; a < nonDistinctExprList.size();  a++)
      aggrOp.addAggr(aggrFnForNonDistinctExprList.get(a), nonDistinctExprList.get(a),
                     false,  returnTypeForNonDistinctExprList.get(a),
                     orderByExprsForNonDistinctExprList.get(a));
    return aggrOp;
  }
  
  /**
   * Perform projections for the GroupBy expression to treat it 
   * as attributes at the above layer
   * 
   * @param query
   *          Query for which we are generating plan
   * @param inputPlan
   *          Partial query plan (input)
   * @return Query plan with the projections of GroupByExpr clause
   */
  private LogOptProject insertProjectForGroupByExpr(SFWQuery query, 
         LogOpt inputPlan) throws LogicalPlanException
  {
    LogOptProject project = new LogOptProject(inputPlan);
    
    oracle.cep.semantic.Expr[]  gbyExprs = query.getGroupByExprs();
    Expr[] logGroupByExprs = new Expr[gbyExprs.length];
    
    for( int i = 0; i < gbyExprs.length; i++)
    {
      Expr expr = null;
      //expressions are wrapped as GroupByExpr and attributes as AttrExpr
      if( gbyExprs[i] instanceof GroupByExpr)
      {
        GroupByExpr gbyExpr = (GroupByExpr)gbyExprs[i];
        expr = SemQueryExprFactory.getInterpreter(gbyExpr.getExpr(),
           new SemQueryExprFactoryContext(gbyExpr.getExpr(),query));
      }
      else
      {
        expr = SemQueryExprFactory.getInterpreter(gbyExprs[i],
                       new SemQueryExprFactoryContext(gbyExprs[i], query));
      }
      logGroupByExprs[i] = expr;
      project.add_project_expr_noattr(expr);
      project.addAttr(new AttrGroupBy(expr, expr.getType()));
    }

    //copy all the attrs from the relation
    ArrayList<Attr> attrs = inputPlan.getOutAttrs();
    assert attrs != null;
    for(Attr attr : attrs)
    {
      ExprAttr exprAttr = new ExprAttr(attr.getDatatype(), 
                                     attr, attr.getActualName());
      if(!isAlreadyPresentInGroupByExprs(exprAttr, logGroupByExprs))
         project.add_project_expr(exprAttr);
    }
    
    return project;
  }
 
  public boolean isAlreadyPresentInGroupByExprs(ExprAttr exprAttr, Expr[] logGroupByExprs)
  {
    for(Expr exp : logGroupByExprs)
    {
      if(exprAttr.equals(exp))
      {
        return true;
      }
        
    }
    return false;
  }
  
  /**
   * Apply predicates that are present in the HAVING clause of a query to an
   * input subplan.
   * 
   * @param query
   *          Query for which we are generating plan
   * @param aggr
   *          Partial query plan (input)
   * 
   * @return Query plan which applies HAVING clause predicates
   */
  private LogOpt applyHavingPreds_n(SFWQuery query, LogOpt aggr)
  {
    BExpr predicates = query.getHavingPredicate();
    
    if (predicates == null)
      return aggr;
    else
      return LogPlanHelper.applyPredsHelper_n(query, aggr, predicates);
    
  }

  /**
   * Perform projections specified in SELECT clause
   * 
   * @param query
   *          Query for which we are generating plan
   * @param inputPlan
   *          Partial query plan (input)
   * @return Query plan with the projections in SELECT clause
   */
  private LogOpt doProjs_n(SemQuery query, LogOpt having)
      throws LogicalPlanException
  {
    LogOptProject project = new LogOptProject(having);
    ArrayList<oracle.cep.semantic.Expr> arrExpr = query.getSelectListExprs();
    int numProjExprs = arrExpr.size();

    for(int e = 0;e < numProjExprs; e++)
    {
      Expr projExpr = SemQueryExprFactory.getInterpreter(arrExpr.get(e),
          new SemQueryExprFactoryContext(arrExpr.get(e), query));
      project.add_project_expr(projExpr);
    }

    // Prepare array list of those order by and partition clause's attributes
    // which are not part of select list
    ArrayList<Expr> extendedProjExprList = new ArrayList<Expr>();
    processOrderByExprs(extendedProjExprList, query, project);

    // insert non-select list expressions into list of output attributes of
    // project operator
    // Note: output of query will only contain select list expressions
    
    ListIterator<Expr> tempIter = extendedProjExprList.listIterator();
    while(tempIter.hasNext())
    {
      project.add_project_expr(tempIter.next());
    }
          
    return (LogOpt) project;
  }

  /**
   * Perform orderby clause
   * 
   * @param query
   *         Query for which we are generating plan
   * @param project
   *         Query plan output after applying Project 
   * @return Query plan after applying order by clause
   */
  private LogOpt applyOrderBy_n(SFWQuery query, LogOpt project)
  {
    if(!query.isOrderByListEmpty())
    {
      LogOptOrderBy orderby = new LogOptOrderBy(project, 
                                                query.getNumOrderByRows());
      // process order by expressions
      ArrayList<oracle.cep.semantic.Expr> exprList = query.getOrderByExprs();
      for(oracle.cep.semantic.Expr expr : exprList)
      {
        ExprOrderBy orderExpr 
                   = (ExprOrderBy)SemQueryExprFactory.getInterpreter(expr, 
                     new SemQueryExprFactoryContext(expr, query));
        orderby.add_orderby_expr(orderExpr);
      }
      
      // process partition by attributes (if any)
      ArrayList<oracle.cep.semantic.Expr> partitionByAttrList
        = query.getPartitionByAttrs();
      
      if(partitionByAttrList != null)
      {
        for(oracle.cep.semantic.Expr semExpr : partitionByAttrList)
        {
          // Assumption: that partition by clause will always contain attributes
          //  as we are not allowing other expression into partition by clause
          assert semExpr instanceof oracle.cep.semantic.AttrExpr;
          
          oracle.cep.semantic.AttrExpr semAttrExpr = 
             (oracle.cep.semantic.AttrExpr)semExpr;
          
          Attr partitionByAttr = LogPlanHelper.transformAttr(semAttrExpr.getAttr());          
          orderby.addPartitionByAttrs(partitionByAttr);
        }
      }
     
      return orderby;
    }
    else
      return project;
  }
  
  /**
   * Apply distinct operator: remove duplicates
   * 
   * @param query
   *          Query for which we are generating plan
   * @param inputPlan
   *          Partial query plan (input)
   * @return Query plan that applies distinct
   */
  private LogOpt applyDistinct_n(SFWQuery query, LogOpt orderby)
  {
    if (query.isDistinct())
      return new LogOptDistinct(orderby);
    else
      return orderby;
  }

  /**
   * Apply a R2S operator if required by the query:
   * 
   * @param query
   *          Query for which we are generating plan
   * @param inputPlan
   *          Partial uqery plan (input)
   * @return Query plan with the R2S operator
   */
  private LogOpt applyR2SOps_n(SemQuery query, LogOpt distinct)
  {

    RelToStrOp r2s = query.getR2SOp();

    // Query does not have an R2S operator
    if (r2s == null)
      return distinct;

    switch (r2s)
    {
      case RSTREAM:
        return new LogOptRStream(distinct);
      case ISTREAM:
        Integer[] exprListMap = query.getUsingExprListMap();
        if (exprListMap != null) {
          /* ISTREAM operator with a USING clause is specified.
           */
          LogOptIStream istreamOpt = new LogOptIStream(distinct);
          istreamOpt.addUsingClauseExprMap(exprListMap);
          return istreamOpt;
        }
        else {
          return new LogOptIStream(distinct);
        }
      case DSTREAM:
        return new LogOptDStream(distinct);
      default:
        assert false;
    }

    // should not come here
    assert false;
    return null;
  }
  
  /**
   * Expresses
   *   A INTERSECT B as (distinct(A minus (A minus B)))
   * @param leftSource left input relation
   * @param rightSource right input relation
   * @param sq semantic representation of query
   * @return root node corresponding to the changed representation of  A INTERSECT B
   */
  private LogOpt handleIntersect(LogOpt leftSource,  LogOpt rightSource)
  {
    LogOpt ret;
    LogOpt temp;
    
    temp = new LogOptMinus(leftSource, rightSource);
    ret  = new LogOptMinus(leftSource, temp);
    return new LogOptDistinct(ret);
  }
  
  /**
   * Expresses
   *   A IN B as  (A NOT IN (A NOT IN B))
   * @param leftSource left input relation
   * @param rightSource right input relation
   * @param sq semantic representation of query
   * @return root node corresponding to the changed representation of A IN B
   */
  private LogOpt handleIn(LogOpt leftSource, LogOpt rightSource, SetOpQuery sq)
  {
    LogOpt ret;
    LogOpt temp;
    
    temp = new LogOptMinus(leftSource, rightSource, sq.getLeftComparisonAttrs(),
                           sq.getRightComparisonAttrs(), sq.getNumComparisonAttrs());
    //For outer minus operator the number of comp. attrs will be same for left and right input.
    //The comparison attrs will be all the attrs. in the left input.
    ret  = new LogOptMinus(leftSource, temp, sq.getLeftAttrs(), sq.getLeftAttrs(),
                           (sq.getLeftAttrs()).length);
    return ret;
  }
  
  /**
   * Prepare a list of non-select list expressions which will be added
   * into Project operator for future references by OrderByTop operator
   * @param restProjExprs list of non select list expressions
   * @param query semantic representation of query
   * @param project logical project operator
   */
  private void processOrderByExprs(ArrayList<Expr> restProjExprs, 
                                   SemQuery query,
                                   LogOptProject project )
  {
    // clear the list
    restProjExprs.clear();
    
    // Order by clause will only be part of a SFW query
    if(!(query instanceof SFWQuery))
      return;
    
    SFWQuery sfwQuery = (SFWQuery)query;
    
    if(!sfwQuery.isOrderByListEmpty())
    {
      // Note: Usage of LinkedHashSet datastructure to keep a collection of
      // partition by and order by expressions without any duplicates
      
      // initialize exprList by list of order by expressions
      ArrayList<oracle.cep.semantic.Expr> orderByExprList = sfwQuery.getOrderByExprs();
      int numOrderByExprs  = orderByExprList.size();
      
      // process partition by attributes (if any)
      ArrayList<oracle.cep.semantic.Expr> partitionByAttrList
        = sfwQuery.getPartitionByAttrs();
      int numPartitionByAttrs 
        = partitionByAttrList != null ? partitionByAttrList.size() : 0;
      
      // below datastructure will not grow beyong the initial limits
      ArrayList<oracle.cep.semantic.Expr> exprList 
        = new ArrayList<oracle.cep.semantic.Expr>
        (numOrderByExprs + numPartitionByAttrs);
      
      // Prepare a list from order by and partition by attributes without
      // any duplicates
      oracle.cep.semantic.OrderByExpr tempExpr;
      for(int i =0 ; i < orderByExprList.size(); i++)
      {
        assert orderByExprList.get(i) instanceof 
               oracle.cep.semantic.OrderByExpr;
        tempExpr = (oracle.cep.semantic.OrderByExpr)orderByExprList.get(i);
        if(!isDuplicateExpr(exprList, tempExpr.getOrderbyExpr()))
          exprList.add(tempExpr.getOrderbyExpr());
      }
      
      for(int i =0; i < numPartitionByAttrs; i++)
      {
        if(!isDuplicateExpr(exprList, partitionByAttrList.get(i)))
          exprList.add(partitionByAttrList.get(i));
      }
      
      // initialize the parameter list with only those attributes or expression
      // which are not present in select list      
      for(oracle.cep.semantic.Expr expr : exprList)
      {        
        Expr logExpr = SemQueryExprFactory.getInterpreter(expr, 
                     new SemQueryExprFactoryContext(expr, query));

        if(!isDuplicateProjExpr(logExpr, project))  
            restProjExprs.add(logExpr);
        
      }
      
    } // end of if order by list IS NOT EMPTY
    
  } //end of method
  

  /**
   * Check whether the parameter expression is in select list
   * @param paramExpr parameter expression
   * @param proj logical project operator which contains a reference of select
   * list
   * @return true if paramExpr is already present in select list
   */
  private boolean isDuplicateProjExpr(Expr paramExpr, LogOptProject proj)
  {
    ListIterator<Expr> iter = proj.getBexpr().listIterator();
    Expr tempExpr;
    while(iter.hasNext())
    {
      tempExpr = iter.next();
      if(tempExpr.equals(paramExpr))
       return true;
    }
    return false;
  }
  
  /**
   * Check whether the param expression is inside param list
   * @param exprList
   * @param elem
   * @return
   */
  private boolean isDuplicateExpr(ArrayList<oracle.cep.semantic.Expr> exprList,
                                  oracle.cep.semantic.Expr elem)
  {
    ListIterator<oracle.cep.semantic.Expr> iter 
       = exprList.listIterator();
    oracle.cep.semantic.Expr tempExpr;
    while(iter.hasNext())
    {
      tempExpr = iter.next();
      if(tempExpr.equals(elem))
        return true;
    }
    return false;
  }

}  
  
    
