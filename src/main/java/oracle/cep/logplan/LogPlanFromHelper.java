/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogPlanFromHelper.java /main/31 2015/02/16 09:40:11 udeshmuk Exp $ */

/* Copyright (c) 2007, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    Logical Plan Generation. Helper methods for FROM clause processing.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    08/07/12 - propagate archived dimension
    pkali       04/04/12 - included datatype arg in AttrNamed instance
    vikshukl    03/29/12 - catch specific logical exceptions before generic
                           exceptions
    vikshukl    03/20/12 - propagate error message when a subquery evaluates to
                           unbounded stream for cross join
    udeshmuk    03/09/10 - within clause support
    sborah      01/12/10 - support for multiple external relations
    sbishnoi    12/14/09 - added review comments
    sbishnoi    09/30/09 - table function support
    sbishnoi    09/24/09 - support for table functions
    parujain    05/28/09 - set isexternal
    parujain    05/20/09 - ansi outer join support
    udeshmuk    03/04/09 - remove project operator for measures.
    udeshmuk    02/02/09 - support for duration arith_expr in pattern.
    hopark      10/09/08 - remove statics
    udeshmuk    09/07/08 - 
    sbishnoi    07/28/08 - support for nanosecond; changing variable names and
                           updating comments
    udeshmuk    07/12/08 - 
    rkomurav    07/07/08 - add isrecurringnonevent flag
    rkomurav    05/15/08 - support non event detection
    rkomurav    05/13/08 - move regList method to sematic layer
    rkomurav    04/21/08 - handle the case where define list has a corr name
                           which is not in the pattern clause
    parujain    03/11/08 - derived timestamp
    rkomurav    03/12/08 - call nfa.storealltrans
    rkomurav    02/29/08 - set a numalphs
    rkomurav    02/12/08 - call NFA method
    rkomurav    01/16/08 - support lazy quantifiers
    rkomurav    01/03/08 - remove statetoalph map
    najain      12/05/07 - xmltable support
    rkomurav    09/27/07 - remove mandatory correlation definition
    anasrini    09/26/07 - ALL MATCHES support
    rkomurav    09/25/07 - prev range
    rkomurav    09/06/07 - maxprevindex add
    anasrini    08/28/07 - get numBaseEntityAttrs from input
    anasrini    07/02/07 - support for partition by clause
    rkomurav    06/19/07 - add statetoalph map
    rkomurav    06/05/07 - add classB param to logoptpatternstrm
    rkomurav    05/29/07 - fix map paramerter to logoptpatternstrm
    anasrini    05/24/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogPlanFromHelper.java /main/31 2015/02/16 09:40:11 udeshmuk Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import oracle.cep.common.Datatype;
import oracle.cep.common.RegexpOp;
import oracle.cep.common.OuterJoinType;
import oracle.cep.semantic.BExpr;
import oracle.cep.semantic.DerivedTimeSpec;
import oracle.cep.semantic.OuterRelationSpec;
import oracle.cep.semantic.PatternSpec;
import oracle.cep.semantic.RelationSpec;
import oracle.cep.semantic.SFWQuery;
import oracle.cep.semantic.SubquerySpec;
import oracle.cep.semantic.TableFunctionRelationSpec;
import oracle.cep.semantic.XmlTableColumnNode;
import oracle.cep.semantic.XmlTableSpec;
import oracle.cep.semantic.WindowSpec;
import oracle.cep.service.ExecContext;
import oracle.cep.metadata.MetadataException;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrNamed;
import oracle.cep.logplan.expr.BoolExpr;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprXQryFunc;
import oracle.cep.logplan.expr.factory.SemQueryExprFactory;
import oracle.cep.logplan.expr.factory.SemQueryExprFactoryContext;
import oracle.cep.logplan.factory.SemQueryInterpreterFactory;
import oracle.cep.logplan.factory.SemQueryInterpreterFactoryContext;
import oracle.cep.logplan.window.WindowTypeFactory;
import oracle.cep.logplan.window.WindowTypeFactoryContext;
import oracle.cep.logplan.pattern.CorrNameDef;
import oracle.cep.logplan.pattern.SubsetCorr;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.LogicalPlanError;
import oracle.cep.util.DFA;
import oracle.cep.util.NFA;


/**
 * Logical Plan Generation. Helper methods for FROM clause processing.
 */

class LogPlanFromHelper {

  /**
   * This class should not be instantiated.
   * Contains only static methods.
   */
  private LogPlanFromHelper() {
  }
  
  /** 
   * Process the from-clause entry
   * @param ec
   * @param spec
   * @param query
   * @return a LogOpt representation of from-clause entry
   * @throws LogicalPlanException
   * @throws MetadataException
   */
  static LogOpt processFromClauseEntry(ExecContext ec, 
                                       RelationSpec spec,
                                       SFWQuery query)
    throws LogicalPlanException, MetadataException
  {
    if(spec.isOuterRelation())
    {
      //Note: Given from-clause entry is for a outer join relation
      assert spec instanceof OuterRelationSpec;
      return processOuterRelation(ec, (OuterRelationSpec)spec, query);
    }
    else
      return processRelation(ec, spec.getVarId(), query);
  }
  
  /**
   * Process the outer join relation into logical operator(s)
   * @param ec
   * @param rspec
   * @param query
   * @return Logical operator tree for the parameter outer relation
   * @throws LogicalPlanException
   * @throws MetadataException
   */
  private static LogOpt processOuterRelation(ExecContext ec, 
                                             OuterRelationSpec rspec,
                                             SFWQuery query)
  throws LogicalPlanException, MetadataException
  {
   
    LogOpt left  = processFromClauseEntry(ec, rspec.getLeftSpec(), query);
    
    LogOpt right = processRelation(ec, rspec.getRightId(), query);
    
    OuterJoinType joinType = rspec.getOuterJoinType();
    
    // flag to set isExternal for the newly created binary cross
    boolean isExternal = false;
    
    LogOptCross ret = new LogOptCross();    
    
    // Note: switch the left and right input if left input is an external
    // relation; Also left external relation should be a relation source op    
    if(left.isPullOperator())
    {
      // Add the left and right input to the LogOptCross
      ret.addInput(right);
      ret.addInput(left);
      // Switch the Outer Join Type as we are switching the side
      // of external relation
      // Note:CQL doesn't allow FULL_OUTER join of stream with external relation.
      ret.setOuterJoinType(
          joinType == OuterJoinType.LEFT_OUTER ? 
              OuterJoinType.RIGHT_OUTER : 
              OuterJoinType.LEFT_OUTER);
      // Note: We will check whether the correctness of join type in
      // the method LogOptCross.validate()
    }    
    else
    {
      // Add the left and right input to the LogOptCross
      ret.addInput(left);
      ret.addInput(right);
      ret.setOuterJoinType(joinType);
      ret.setArchivedDim(right.isArchivedDim());
    }
    
    // join will be external only if
    // either left input will represent an external relation source OR
    // right input will represent an external relation source
    isExternal = (left.isPullOperator()) ||
                 (right.isPullOperator());
    
    ret.setExternal(isExternal);
    
    SemQueryExprFactoryContext ctx;
    BoolExpr pred;
    Vector<BExpr> condNodes = new Vector<BExpr>();
    LogPlanHelper.splitPredicate(rspec.getCondition(), condNodes);
    
    Iterator<BExpr> iterator = condNodes.iterator();    
    // Note: Semantic form of condition has outer join information already set;
    // so newly created Logical layer form of condition will carry that
    // information to further layers. 
    while(iterator.hasNext())
    {
      BExpr cond = iterator.next();
      ctx = new SemQueryExprFactoryContext(cond, query);
      pred = (BoolExpr) SemQueryExprFactory.getInterpreter(cond, ctx);     
      ret.addPredicate(pred);
    }    
    return ret;
  }
  
  // Process the FROM clause element into logical operator(s)
  private static LogOpt processRelation(ExecContext ec, int varId, SFWQuery query)
    throws LogicalPlanException, MetadataException
  {

    LogOpt  tablePlan;
    
    // Note that the portion of a FROM clause element excluding the
    // WINDOW could be either a PERSISTENT entity (like relation, stream -
    // either base or derived view) or an INLINE VIEW

    tablePlan = processPreWindow(ec, varId, query);
    
    WindowSpec wspec = query.getWindowSpec(varId);
    if(wspec == null)
      return tablePlan;

    return WindowTypeFactory.getInterpreter(wspec.getWindowType(),
        new WindowTypeFactoryContext(tablePlan, wspec, query));
  }

  private static LogOpt processPreWindow(ExecContext ec, int varId, 
                                         SFWQuery query)
    throws LogicalPlanException, MetadataException 
  {
    // Note that the portion of a FROM clause element excluding the
    // WINDOW could be either a PERSISTENT entity (like relation, stream -
    // either base or derived view) or an INLINE VIEW
    //
    // The only INLINEVIEWs we support today are MATCH_RECOGNIZE, XMLTable and
    // TABLE clause, but this would be extended tomorrow to SUBQUERIES as well
    //
    // Today, since there is no nested INLINE_VIEW support, we just handle
    // for the MATCH_RECOGNIZE, XMLTable and TABLE part. This part of the 
    // approach would change when we add support for sub queries.
    PatternSpec  pSpec = query.getPatternSpec(varId);
    SubquerySpec subqSpec = query.getSubquerySpec(varId);
    XmlTableSpec xSpec = query.getXmlTableSpec(varId);
    DerivedTimeSpec dSpec = query.getDerivedTimeSpec(varId); 
    
    TableFunctionRelationSpec tableFunctionSpec 
       = query.getTableFunctionRelSpec(varId);
    
    int         baseVarId;
    int         tableId;
    LogOpt      tableSource = null;
    LogOpt      tablePlan;
    boolean     isStream = false;
    boolean     isArchivedDim = false;

    assert ((pSpec == null) || (xSpec == null) );

    // Check whether it is a PERSISTENT entity or an INLINE_VIEW
    // This check suffices for today       
    if (pSpec != null) 
    {
      baseVarId = pSpec.getBaseVarId();

      // Today, since there is no nested sub-query support, this baseVarId
      // corresponds to a PERSISTENT entity      
      tableId = pSpec.getBaseTableId();
    }
    else if (xSpec != null) 
    {
      baseVarId = xSpec.getBaseVarId();

      // Today, since there is no nested sub-query support, this baseVarId
      // corresponds to a PERSISTENT entity
      
      tableId = xSpec.getBaseTableId();
    }
    else if (tableFunctionSpec != null)
    {
      // Construct LogOptRelnSrc for this table function
      tablePlan
        = processTableFunctionRelSpec(tableFunctionSpec, query, varId);
      return tablePlan;
    }
    else 
    {
      baseVarId = varId;
      tableId = query.getTableId(baseVarId);
    }

    // process from clause entries, persistent or subqueries.
    try
    {
      // Get the source operator for the table
      if (tableId != -1) {
        // regular persistent entity
        isStream = ec.getTableMgr().isStream(tableId);
        isArchivedDim = ec.getTableMgr().isDimension(tableId);
        
        SemQueryInterpreterFactoryContext qryCtx =
          new SemQueryInterpreterFactoryContext(ec, query, baseVarId, tableId);
        if(dSpec != null)
          qryCtx.setDerivedTimeSpec(dSpec);
        tableSource = 
          SemQueryInterpreterFactory.getInterpreter(isStream,qryCtx);
      }
      else {        
        assert subqSpec != null; // subquery spec better be present
        isArchivedDim = subqSpec.getQuery().isDependentOnArchivedDimension();
        tableSource = ec.getQueryMgr().getLog().genLogPlan(ec, 
                                                           subqSpec.getQuery());
        
        // now add a subquery source node
        LogOptSubquerySrc subquery = 
          new LogOptSubquerySrc(tableSource, subqSpec.getSubqname(), 
              subqSpec.getVarid());
        
        tableSource = subquery;        
      }        
    }
    catch (LogicalPlanException lpe)    		
    {
      // with subquery support we need to explicitly catch logical plan
      // exceptions and send them up the chain as is rather than
      // throwing LOGOPT_NOT_CREATED.
      throw lpe;
    }
    catch (CEPException ex)
    {
    	throw new LogicalPlanException(LogicalPlanError.LOGOPT_NOT_CREATED);
    }
    
    if (pSpec != null) 
    {
      tablePlan = processPattern(pSpec, query, tableSource, varId);
    }
    else if (xSpec != null)
    {
      tablePlan = processXmlTable(xSpec, query, tableSource, varId);
    }    
    else 
    {
      tablePlan = tableSource;
    }
    tablePlan.setArchivedDim(isArchivedDim);
    
    return tablePlan;
  }

  
  private static LogOpt processXmlTable(XmlTableSpec xSpec, SFWQuery query,
					LogOpt input, int aliasVarId) 
    throws LogicalPlanException, MetadataException 
  {
    LogOptXmlTable xmlTbl = new LogOptXmlTable(input);
    AttrNamed attr = new AttrNamed(xSpec.getVarId(), xSpec.getAttrId(), 
                                       Datatype.XMLTYPE);
    xmlTbl.addAttr(attr);
    
    xmlTbl.setXQryExpr(xSpec.getXQryFuncExpr());

    // Create a project on top of the LogOptXmlTable
    LogOptProject project = new LogOptProject(xmlTbl);
    XmlTableColumnNode[] lstCols = xSpec.getListCols();
    for (int i = 0; i < lstCols.length; i++)
    {
      attr = new AttrNamed(lstCols[i].getVarId(), lstCols[i].getAttrId(), 
                              lstCols[i].getXQryFuncExpr().getReturnType());
      project.addAttr(attr);
      oracle.cep.semantic.Expr semExpr = lstCols[i].getXQryFuncExpr();
      ExprXQryFunc expr = 
        (ExprXQryFunc)SemQueryExprFactory.getInterpreter(
          semExpr, new SemQueryExprFactoryContext(semExpr, null));
      project.add_project_expr_noattr(expr);
    }

    return project;
  }

  private static LogOpt processPattern(PatternSpec pSpec, SFWQuery query,
                                       LogOpt input, int aliasVarId) 
    throws LogicalPlanException, MetadataException {

    LogOpt patternPlan;

    // Create the Pattern Stream operator
    patternPlan = doPatternStrm(query, pSpec, input, aliasVarId);

    return patternPlan;
  }
  
  /**
   * Helper function to construct source operator for table function
   * external relation
   * @param tableFunctionSpec
   * @param query
   * @param aliasVarId
   * @return
   * @throws LogicalPlanException
   * @throws MetadataException
   */
  private static LogOpt processTableFunctionRelSpec( 
                              TableFunctionRelationSpec tableFunctionSpec,
                              SFWQuery query,
                              int aliasVarId)
    throws LogicalPlanException, MetadataException
  {  
    // Get the table function expression from spec
    oracle.cep.semantic.Expr tableFunctionSemExpr 
      = tableFunctionSpec.getTableFunctionExpr();
    
    // Interpret the table function expression
    Expr tableFunctionExpr = 
      SemQueryExprFactory.getInterpreter(
          tableFunctionSemExpr, 
          new SemQueryExprFactoryContext(tableFunctionSemExpr, query));
    
    // Construct new table function logical operator having 
    // table alias, column name , column type
    
    LogOptTableFunctionRelSource logPlan 
      = new LogOptTableFunctionRelSource(tableFunctionSpec.getTableAlias(),
                                         tableFunctionSpec.getColumnAlias(),
                                         tableFunctionSpec.getReturnCollectionDatatype(),
                                         tableFunctionSpec.getComponentDatatype(),
                                         tableFunctionSpec.getAttrId(),
                                         tableFunctionSpec.getVarId(),
                                         tableFunctionSpec.getNumAttrs());
    
    // Set the table function logical expression into LogOpt 
    logPlan.setTableFunctionExpr(tableFunctionExpr);
    
    return logPlan;
  }

  private static LogOpt doPatternStrm(SFWQuery query, PatternSpec pSpec, 
                                      LogOpt input, int aliasVarId)
    throws LogicalPlanException, MetadataException
  {

    // Process the PARTITION BY clause
    Attr[] partnAttrs;
    partnAttrs = LogPlanPatternHelper.processPartnBy(pSpec);

    // Process the PATTERN clause
    ArrayList<Integer> stateMap = pSpec.getStateMap();
    ArrayList<RegexpOp> regList = pSpec.getRegList();
    ArrayList<Boolean> isGroup  = pSpec.getIsGroup();
    
    // process the SUBSET clause
    // convert the subset correlations
    SubsetCorr[] subsetCorrs;
    oracle.cep.semantic.CorrNameDef[] semCorrDefs = pSpec.getCorrDefs();
    if(pSpec.getSubsetCorrs() != null)
    {
      subsetCorrs = LogPlanPatternHelper.convertSubsetCorrs(
                                         pSpec.getSubsetCorrs(), query, 
                                         semCorrDefs.length);
    }
    else
      subsetCorrs = null;
    
    // Process the DEFINE clause
    // convert the correlation definitions - the predicates
    CorrNameDef[] logCorrs = 
      LogPlanPatternHelper.convertCorrDefs(semCorrDefs, query, subsetCorrs);

    //if a correlation name is not defined then its always true and
    //the pattern declared has distinct correlation names
    int stateMapSize = stateMap.size();
    
    // Map the pattern symbol positions to the Correlation name
    // positions in the DEFINE clause and vice versa
    //
    //alphabettocorrdef maintains map from alphabet order
    //to correlation definition order
    //
    //corrdeftoalphabet maintains map from correlation
    //definition order to alphabet order
    int[] alphabetToCorrDef = new int[logCorrs.length];
    int[] corrDefToAlphabet = new int[logCorrs.length];
    int map;
    
    int temp = 0;
    for(int i = 0; i < logCorrs.length; i++) {
      map = stateMap.indexOf(logCorrs[i].getVarId());
      //case where define list defines a corr name which is not specified in
      //the pattern expression and statemap cannot record it.
      //for these assign dummy maps appending at the end
      if(map == -1)
      {
        map = stateMapSize + temp;
        temp++;
      }
      alphabetToCorrDef[map] = i;
      corrDefToAlphabet[i]   = map;
    }
    
    // Determine if a correlation name is SINGLETON or GROUP
    for(int i = 0; i < logCorrs.length; i++) {
      // A correlation name is SINGLETON iff reglist entry is null
      
      //case where define list defines a corr name which is not specified in
      //the pattern expression and not hence not listed in regList. for these
      //skip setting singleton flag. Its not going to be used anyway
      if(corrDefToAlphabet[i] >= regList.size())
        continue;
      if(!isGroup.get(corrDefToAlphabet[i])) 
        logCorrs[i].setSingleton(true);
      else
        logCorrs[i].setSingleton(false);
    }
    
    DFA dfa;
    NFA nfa;
    dfa = null;
    nfa = null;
    
    // construct the NFA for class B and
    // construct the DFA for class A and
    // pre compute the ordered transitions and store them
    if(pSpec.isClassB())
    {
      nfa = LogPlanPatternHelper.getNFA(pSpec.getPattern());
      if(pSpec.isNonEvent())
        nfa = LogPlanPatternHelper.getNonEventNFA(nfa, pSpec.getDurationSymbolAlphIndex());
      nfa.storeOrderedTransitions();
      nfa.storeAllTransitions();
      nfa.setNumAlphabets(pSpec.getNumAlphs());
    }
    else
    {
      dfa = LogPlanPatternHelper.getDFA(pSpec.getPattern(), pSpec.getNumAlphs());
      dfa.storeOrderedTransitions();
    }
   
    // process DURATION clause
    Expr logDurExpr = null;
    if(pSpec.isDurationExpr())
    { //Convert duration expr to logical equivalent
      oracle.cep.semantic.Expr semDurExpr = pSpec.getDurationExpr();
      logDurExpr = SemQueryExprFactory.getInterpreter(semDurExpr,
        new SemQueryExprFactoryContext(semDurExpr, query));
    }
    
    //process MEASURES clause
    
    oracle.cep.semantic.Expr[] arrExpr = pSpec.getMeasures();
    Expr[] measureExprs = new Expr[arrExpr.length];
    int numProjExprs = arrExpr.length;

    for (int e = 0; e < numProjExprs; e++)
    {
      measureExprs[e] = SemQueryExprFactory.getInterpreter(arrExpr[e],
          new SemQueryExprFactoryContext(arrExpr[e], query));
      
    }
    
    // Create the PatternStrm logical operator
    LogOptPatternStrm patternPlan = 
      new LogOptPatternStrm(input, partnAttrs, dfa, nfa, logCorrs, 
                            corrDefToAlphabet, pSpec.isClassB(),
                            pSpec.getMaxPrevIndex(), pSpec.getMaxPrevRange(),
                            pSpec.isPrevRangeExists(), pSpec.getSkipClause(),
                            subsetCorrs, pSpec.isNonEvent(),
			    pSpec.isWithin(), pSpec.isWithinInclusive(),
                            pSpec.getDurationValue(),
                            pSpec.getDurationSymbolAlphIndex(),
                            pSpec.isRecurringNonEvent(),
                            pSpec.isDurationExpr(), logDurExpr, 
                            pSpec.getDurationUnit(), measureExprs);

    //setup aggrs - used just for finding the position of particular aggr attr in AGGRROLE..
    //used by phyplancorrattrfactory 
    LogPlanPatternHelper.setupAggrs(logCorrs, patternPlan);
    if(subsetCorrs != null)
      LogPlanPatternHelper.setupAggrs(subsetCorrs, patternPlan); 

    // set the OUTPUT schema for the PatternStrm operator
    //out schema consists of only measure clause exprs.
    for(int i=0; i < measureExprs.length; i++)
    {
      Attr attr = new AttrNamed(aliasVarId, i, measureExprs[i].getType());
      patternPlan.setOutAttr(i, attr);
      patternPlan.numOutAttrs++;
    }
    
    return patternPlan;
  }

}
