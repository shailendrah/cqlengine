/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SemContext.java /main/38 2014/10/14 06:35:33 udeshmuk Exp $ */

/* Copyright (c) 2006, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    This class encapsulates the global context passed around during semantic
    analysis

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/18/14 - set isPartnStream
    udeshmuk    01/15/13 - XbranchMerge udeshmuk_bug-15962424_ps6 from
                           st_pcbpel_11.1.1.4.0
    vikshukl    07/30/12 - archived dimension relation
    pkali       06/27/12 - added BaseStreamSpec data memeber
    vikshukl    11/07/11 - group by expr
    vikshukl    07/13/11 - subquery support
    udeshmuk    03/21/11 - isArchived flag
    udeshmuk    12/11/12 - field to denote if we are interpreting partition
                           parallel expr
    sborah      08/03/10 - support for table specific star clause in select
    sborah      03/29/10 - reset transient for tablespec
    sbishnoi    10/05/09 - support for table function
    vikshukl    08/25/09 - support for ISTREAM (r) DIFFERENCE USING (...)
    sbishnoi    05/20/09 - adding isOuterJoinTypeNode
    parujain    05/19/09 - outer join support
    sbishnoi    04/26/09 - adding isAggrExist
    sbishnoi    03/17/09 - adding context variables for order by node
    parujain    03/13/09 - aggr information
    hopark      02/01/09 - set table name to context
    parujain    01/28/09 - transaction mgmt
    hopark      11/17/08 - set transaction in context
    hopark      10/07/08 - use execContext to remove statics
    parujain    09/15/08 - multiple schema support
    udeshmuk    06/03/08 - support for xmlagg.
    rkomurav    04/21/08 - add isfirstlastallowed
    parujain    03/10/08 - derived timestamp
    rkomurav    03/12/08 - remove streamName field
    rkomurav    03/03/08 - add groupingExists flag
    rkomurav    02/01/08 - direct alternation pattern to classb
    sbishnoi    12/18/07 - cleanup
    najain      12/03/07 - add xmltable spec
    sbishnoi    12/04/07 - support for update semantics
    rkomurav    11/27/07 - pass corr varid for validating prev params
    rkomurav    09/28/07 - add stream name field
    rkomurav    09/25/07 - prev rantge supporte
    rkomurav    09/05/07 - add maxPrevParam
    rkomurav    08/20/07 - add isCountCorrStarAllowed flag
    parujain    04/04/07 - CASE handling
    rkomurav    02/07/07 - add pattern RegExp
    parujain    01/31/07 - drop function requires queryid
    parujain    11/03/06 - tree representation of conditions
    anasrini    07/10/06 - support for user defined aggregations 
    najain      04/06/06 - cleanup
    anasrini    03/02/06 - fix unchecked compiler warnings 
    anasrini    02/27/06 - transient fields 
    anasrini    02/26/06 - init vectors in the constructor 
    anasrini    02/26/06 - select list and where clause support 
    anasrini    02/23/06 - support for distinct in the select clause 
    anasrini    02/23/06 - add support for select list 
    anasrini    02/21/06 - support for expressions and conditions 
    anasrini    02/20/06 - Creation
    anasrini    02/20/06 - Creation
    anasrini    02/20/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SemContext.java /main/38 2014/10/14 06:35:33 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import java.util.ArrayList;
import java.util.Vector;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ExpandableArray;
import oracle.cep.metadata.Query;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.service.ExecContext;

/**
 * This class encapsulates the global context passed around during semantic
 * analysis
 * <p>
 * This class is private to the semantic analysis module.
 *
 * @since 1.0
 */

class SemContext {
  private ExecContext   execContext;
  private SymbolTable   symTable;
  private SemQuery      query;

  private Vector<Expr>  selectList;
  private boolean       isDistinct;

  // For user defined aggregations
  private boolean       isAggrAllowed;
  
  // For invalid countCorrStar in select and having list
  private boolean       isCountCorrStarAllowed;
  
  // For prev function
  private boolean       isPrevAllowed;
  
  // For first and last function
  private boolean       isFirstLastAllowed;

  // For archived relation
  private boolean       isArchived;
  private boolean       isDimension;
 
  // For partition stream
  private boolean       isPartnStream;
 
  // transient fields to hold the return status
  private WindowSpec    winspec;
  private Expr          expr;
  private Regexp        regExp;
  private SubquerySpec  subquerySpec;
  private PatternSpec   patternSpec;
  private XmlTableSpec  xmlTableSpec;
  private DerivedTimeSpec derivedTsSpec;
  private RelationSpec  rSpec;
  private TableFunctionRelationSpec tableFunctionSpec;
  private BaseStreamSpec  baseStreamSpec;
  // list of expressions from a particular relation when S.* clause 
  // is specified
  private Vector<Expr>  relationStarList;
  
  //end of transient fields
  
  //Query metadata object whose semantic analysis is getting done
  private Query         queryObj;         

  //Transient field For simple CASE
  // (vikshukl): this does not seem to be a transient member. (to verify)
  private CEPExprNode   compExpr;
  
  // maximum previous index
  private int           maxPrevIndex;
  
  // max previous range
  private long          maxPrevRange;
  
  // boolean indicating if prev range mentioned
  private boolean       prevRangeExists;
  
  // correlation var id
  private int           corrVarId;
  
  // if alternation exists in the pattern clause
  private boolean       alternationExists;
  
  // if grouping exists [eg. A (CBD)* ]
  private boolean       groupingExists;
  
  // true when order by clause interpreter is called from XMLAggInterp
  private boolean       orderByInsideXMLAgg;
  
  private String        schema;
  
  private String        windowRelName;
  private String        windowRelAlias;
  
  // Parameters for function
  private Expr[]        params;
  
  // GROUP BY expressions
  private Expr[]        gbyExprArray;
   
  // context variables related to order by clause
  private int                 numOrderByRows;
  private ArrayList<Expr>     orderByExprs;
  private ArrayList<Expr> partitionByAttrs;
  private boolean             isAggrExist;
  
  // This flag will decide whether fromClause table needs to be updated 
  // by relation/stream or not
  private boolean             isWindowOnStream;
  
  // outer join related context
  private boolean       isOuterJoinTypeNode;
  private boolean       isANSIOuterJoinInFromClause;
  
  // using clause related context, position of SELECT expressions
  private Vector<Integer>        usingExprListMap;
  
  /**
   * boolean to indicate if we are interpreting partition parallel expr.
   * Needed because in this case we have to use separate method to lookup
   * attrs only in SOURCE entries in symbol table.
   */
  private boolean isInterpretingPartnExpr = false;

  // Constructor
  SemContext(ExecContext ec) {
    execContext                 = ec;
    selectList                  = new Vector<Expr>();
    isAggrAllowed               = false;
    isCountCorrStarAllowed      = false;
    isPrevAllowed               = false;
    queryObj                    = null;
    compExpr                    = null;
    maxPrevIndex                = 0;
    prevRangeExists             = false;
    alternationExists           = false;
    groupingExists              = false;
    isFirstLastAllowed          = false;
    orderByInsideXMLAgg         = false;
    schema                      = null;
    params                      = null;
    gbyExprArray                = null;    
    orderByExprs                = null;
    numOrderByRows              = 0;
    partitionByAttrs            = null;
    isAggrExist                 = false;
    isWindowOnStream            = false;
    isOuterJoinTypeNode         = false;
    isANSIOuterJoinInFromClause = false;
    isArchived                  = false;
    isDimension                 = false;
    isPartnStream               = false;
    usingExprListMap            = new Vector<Integer>(0);
    relationStarList            = new Vector<Expr>(); 
    isInterpretingPartnExpr     = false;
  }

  // Getter methods

  /**
   * Reset the transient fields. This should be done before every node
   * performs an interpretation.
   */
  void resetTransient() {
    expr              = null;
    winspec           = null;
    regExp            = null;
    subquerySpec      = null;
    patternSpec       = null;
    xmlTableSpec      = null;
    derivedTsSpec     = null;
    rSpec             = null;
    tableFunctionSpec = null;
    relationStarList  = new Vector<Expr>();
  }

  public SubquerySpec getSubquerySpec() {
    return subquerySpec;
  }

  public void setSubquerySpec(SubquerySpec subquerySpec) {
    this.subquerySpec = subquerySpec;
  }

  /**
   * @return the execution context
   */
  public ExecContext getExecContext() {
    return execContext;
  }
  
  /**
   * Get the symbol table used during the semantic analysis
   * @return the symbol table used during the semantic analysis
   */
  SymbolTable getSymbolTable() {
    return symTable;
  }

  /**
   * @return the isPrevAllowed
   */
  public boolean isPrevAllowed() {
    return isPrevAllowed;
  }

  /**
   * @param isPrevAllowed the isPrevAllowed to set
   */
  public void setPrevAllowed(boolean isPrevAllowed) {
    this.isPrevAllowed = isPrevAllowed;
  }

  /**
   * Get the object encapsulating the annotated and parsed query
   * @return the output of semantic analysis. This is the object that is
   *         being constructed during the semantic analysis
   */
  SemQuery getSemQuery() {
    return query;
  }

  /**
   * Get the window specification of the window just processed
   * @return the window specification of the window just processed
   */
  WindowSpec getWindowSpec() {
    return winspec;
  }
  
  RelationSpec getRelationSpec() 
  {
    return rSpec;
  }

  public void setRelationSpec(RelationSpec rspec)
  {
    this.rSpec = rspec;
  }
  
  /**
   * Get the derived timestamp specification of the stream/relation just processed
   * @return the derived timestamp specification of the stream/relation just processed
   */
  DerivedTimeSpec getDerivedTimeSpec()
  {
    return derivedTsSpec;
  }

  public void setDerivedTimeSpec(DerivedTimeSpec tspec)
  {
    this.derivedTsSpec = tspec;  
  }
  
  BaseStreamSpec getBaseStreamSpec() 
  {
    return baseStreamSpec;
  }

  public void setBaseStreamSpec(BaseStreamSpec baseStreamspec)
  {
    this.baseStreamSpec = baseStreamspec;
  }
  
  public void setWindowOnStream(boolean is)
  {
    this.isWindowOnStream = is;
  }
  
  public boolean getWindowOnStream()
  {
    return this.isWindowOnStream;
  }
  
  /**
   * Returns the schema name under which query is getting registered
   * 
   * @return the schema name
   */
  public String getSchema()
  {
    return schema;
  }
  
  public void setSchema(String schema)
  {
    this.schema = schema;
  }
  
  /**
   * Get the current expression
   * @return the current expression
   */
  Expr getExpr() {
    return expr;
  }
  
  /**
   * Get the current regular expression
   * @return Returns the regExp.
   */
  public Regexp getRegExp() {
    return regExp;
  }
  
  /**
   * Get the current pattern specification
   * @return Returns the patternSpec.
   */
  public PatternSpec getPatternSpec() {
    return patternSpec;
  }

  /**
   * Get the current xmltable specification
   * @return Returns the xmltableSpec.
   */
  public XmlTableSpec getXmlTableSpec() {
    return xmlTableSpec;
  }

  /**
   * Set the current xmltable specification
   * @return 
   */
  public void setXmlTableSpec(XmlTableSpec xmlTableSpec) {
    this.xmlTableSpec = xmlTableSpec;
  }

  /**
   * Get the select list expressions
   * @return the select list expressions as an array
   */
  Expr[] getSelectList() {
    int sz = selectList.size();
    return selectList.toArray(new Expr[sz]);
  }
  
  /**
   * Get the relation star select list expressions
   * @return the relation star select list expressions as an array
   */
  Expr[] getRelationStarList() {
    int sz = relationStarList.size();
    return relationStarList.toArray(new Expr[sz]);
  }

  /**
   * Does the select list contain DISTINCT
   * @return true if and only if the select list contains DISTINCT
   */
  public boolean isDistinct() {
    return isDistinct;
  }

  /**
   * Is an aggregation function permitted here ?
   * <p>
   * For example, an aggregation function is not permitted in the 
   * where clause or as an argument to other functions 
   * (single element or aggregation). 
   * <p>
   * This check cannot be performed by the parser since the syntax for
   * expressions with single element functions and aggregation functions
   * is the same
   * <p>
   * @return true if and only if an aggregation function is permitted
   *         in this context
   */
  public boolean isAggrAllowed() {
    return isAggrAllowed;
  }

  // Setter methods
  /**
   * Set the symbol table to be used during the semantic analysis
   * @param symTable the symbol table to be used during the semantic analysis
   */
  void setSymbolTable(SymbolTable symTable) {
    this.symTable = symTable;
  }

  /**
   * Set the object which is the output of the semantic analysis. This is
   * the object which will be enhanced as the semantic analysis proceeds
   * @param query the output object to be enhanced during the semantic 
   *        analysis
   */
  void setSemQuery(SemQuery query) {
    this.query = query;
  }

  /**
   * Set the window specification of the window just processed
   * @param winspec the window specification of the window just processed
   */
  void setWindowSpec(WindowSpec winspec) {
    this.winspec = winspec;
  }

  /**
   * Set the current expression
   * @param cond the current expression
   */
  void setExpr(Expr expr) {
    this.expr = expr;
  }
  
  /**
   * @param regExp The regExp to set.
   */
  public void setRegExp(Regexp regExp) {
    this.regExp = regExp;
  }

  /**
   * @param patternSpec The patternSpec to set.
   */
  public void setPatternSpec(PatternSpec patternSpec) {
    this.patternSpec = patternSpec;
  }

  /**
   * Add the current expression to the set of select list expressions
   * @param selExpr the current select list expression
   */
  void addSelectListExpr(Expr selExpr) {
    selectList.add(selExpr);
  }
  
  /**
   * Add the current expression to the set of relation star
   * select list expressions
   * @param relationStarExpr the current relation star select list expression
   */
  void addRelationStarExpr(Expr relationStarExpr) {
    relationStarList.add(relationStarExpr);
  }

  /**
   * Set if the select clause has a DISTINCT
   * @param isDistinct true if and only if select clause has a DISTINCT
   */
  void setIsDistinct(boolean isDistinct) {
    this.isDistinct = isDistinct;
  }

  /**
   * Set if an aggregation function is permitted in this context
   * @param isAggrAllowed true if and only if an aggregation function is
   *                      permitted in this context
   */
  void setIsAggrAllowed(boolean isAggrAllowed) {
    this.isAggrAllowed = isAggrAllowed;
  }
  
  /**
   * Sets the metadata Query object 
   * @param qry metadata Query object
   */
  void setQueryObj(Query qry) {
    this.queryObj = qry;
  }
  
  /**
   * Gets the query id of the query getting processed
   * @return Query Id
   */
  Query getQueryObj()
  {
    return queryObj;
  }

  /**
   * Gets the Comparison Expression for Simple CASE
   * @return Comparison Expression
   */
  CEPExprNode getCompExpr()
  {
    return compExpr;
  }

  /**
   * Sets the Comparison Expression for Simple CASE
   * @param expr Comparison Expression
   */
  void setCompExpr(CEPExprNode expr)
  {
    this.compExpr = expr;
  }

  /**
   * @return the isCountCorrStarAllowed
   */
  public boolean isCountCorrStarAllowed()
  {
    return isCountCorrStarAllowed;
  }

  /**
   * @param isCountCorrStarAllowed the isCountCorrStarAllowed to set
   */
  public void setCountCorrStarAllowed(boolean isCountCorrStarAllowed)
  {
    this.isCountCorrStarAllowed = isCountCorrStarAllowed;
  }

  /**
   * @return the prevIndex
   */
  public int getMaxPrevIndex()
  {
    return maxPrevIndex;
  }

  /**
   * @param prevIndex the prevIndex to set
   */
  public void setMaxPrevIndex(int prevIndex)
  {
    this.maxPrevIndex = prevIndex;
  }

  /**
   * @return the maxPrevRange
   */
  public long getMaxPrevRange()
  {
    return maxPrevRange;
  }

  /**
   * @return the prevRangeExists
   */
  public boolean isPrevRangeExists()
  {
    return prevRangeExists;
  }

  /**
   * @param maxPrevRange the maxPrevRange to set
   */
  public void setMaxPrevRange(long maxPrevRange)
  {
    this.maxPrevRange = maxPrevRange;
  }

  /**
   * @param prevRangeExists the prevRangeExists to set
   */
  public void setPrevRangeExists(boolean prevRangeExists)
  {
    this.prevRangeExists = prevRangeExists;
  }
  
  /**
   * @return the corrVarId
   */
  public int getCorrVarId()
  {
    return corrVarId;
  }

  /**
   * @param corrVarId the corrVarId to set
   */
  public void setCorrVarId(int corrVarId)
  {
    this.corrVarId = corrVarId;
  }

  /**
   * @return the alternationExists
   */
  public boolean isAlternationExists()
  {
    return alternationExists;
  }

  /**
   * @param alternationExists the alternationExists to set
   */
  public void setAlternationExists(boolean alternationExists)
  {
    this.alternationExists = alternationExists;
  }

  /**
   * @return the groupingExists
   */
  public boolean isGroupingExists()
  {
    return groupingExists;
  }

  /**
   * @param groupingExists the groupingExists to set
   */
  public void setGroupingExists(boolean groupingExists)
  {
    this.groupingExists = groupingExists;
  }

  /**
   * @return the isFirstLastAllowed
   */
  public boolean isFirstLastAllowed()
  {
    return isFirstLastAllowed;
  }

  /**
   * @param isFirstLastAllowed the isFirstLastAllowed to set
   */
  public void setFirstLastAllowed(boolean isFirstLastAllowed)
  {
    this.isFirstLastAllowed = isFirstLastAllowed;
  }
  
  /**
   * @return true if OrderByInterp is called from XMLAggExprInterp, false otherwise
   */
  public boolean isOrderByInsideXMLAgg()
  {
    return orderByInsideXMLAgg;
  }
  
  /**
   * @param val true if OrderByInterp is called from XMLAggExprInterp, false otherwise
   */
  public void setOrderByInsideXMLAgg(boolean val)
  {
    this.orderByInsideXMLAgg = val;
  }
  
  public String getWindowRelName()
  {
    return windowRelName;
  }
  public void setWindowRelName(String rname)
  {
    this.windowRelName = rname;
  }
  public String getWindowRelAlias()
  {
    return windowRelAlias;
  }
  public void setWindowRelAlias(String rname)
  {
    this.windowRelAlias = rname;
  }
  
  public void setParams(Expr[] param)
  {
    this.params = param;
  }
  
  public Expr[] getParams()
  {
    return this.params;
  }
  
  public void setGbyExprs(Expr[] gbyExprs)
  {
    this.gbyExprArray = gbyExprs;
  }
  
  public Expr[] getGbyExprs()
  {
    return this.gbyExprArray;
  }
  
  
  /**
   * Add parameter expression as an order by expression
   * @param paramExpr
   */
  public void addOrderByExprs(Expr paramExpr)
  {
    assert paramExpr != null;
    if(orderByExprs != null)
      orderByExprs.add(paramExpr);
    else
    {
      // lazy initialization
      orderByExprs = new ExpandableArray<Expr>();
      orderByExprs.add(paramExpr);
    }
  }
  
  /**
   * Get a list of order by expressions
   * @return ArrayList
   */
  public ArrayList<Expr> getOrderByExprs()
  {
    return orderByExprs;
  }
  
  /**
   * Set number of order by rows
   * @param paramNumRows
   */
  public void setNumOrderByRows(int paramNumRows)
  {
    numOrderByRows = paramNumRows;
  }
  
  /**
   * Get number of order by rows
   * @return
   */
  public int getNumOrderByRows()
  {
    return numOrderByRows;
  }
  
  /**
   * Add order by clause's partition by attributes
   */
  public void addPartitionByAttrs(Expr paramAttr)
  {
    assert paramAttr != null;
    if(partitionByAttrs != null)
      partitionByAttrs.add(paramAttr);
    else
    {
      // Lazy Initialization
      partitionByAttrs = new ArrayList<Expr>(
                             Constants.INITIAL_NUM_PARTN_ATTRS);
      partitionByAttrs.add(paramAttr);
    }
  }
  
  /**
   * Get the list of partition by attributes associated with order by clause
   * @return
   */
  public ArrayList<Expr> getPartitionByAttrs()
  {
    return partitionByAttrs;
  }

  /**
   * Add USING clause expression's index in SELECT list
   */
  public void addUsingClauseExprIndex(int pos)
  {
    usingExprListMap.add(pos);
  }

  /** 
   * Get USING clause expressions 
   */
  public Integer[] getUsingExprListMap()
  {
    int sz = usingExprListMap.size();
    return usingExprListMap.toArray(new Integer[sz]);
  }

  /**
   * @return the isAggrExist
   */
  public boolean isAggrExist()
  {
    return isAggrExist;
  }

  /**
   * @param isAggrExist the isAggrExist to set
   */
  public void setAggrExist(boolean isAggrExist)
  {
    this.isAggrExist = isAggrExist;
  }
  
  /**
   * @param isPartnStream - true if stream is partitioned, false default
   */
  public void setIsPartnStream(boolean isPartnStream)
  {
    this.isPartnStream = isPartnStream;
  }

  /**
   * @return value of isPartnStream flag
   */
  public boolean isPartnStream()
  {
    return this.isPartnStream;
  }
  

  /**
   * @param isArchived - true if relation is archived, false default
   */
  public void setIsArchived(boolean isArchived)
  {
    this.isArchived = isArchived;
  }

  /**
   * @return value of isArchived flag
   */
  public boolean isArchived()
  {
    return this.isArchived;
  }
  
  /**
   * @param isDimension - true if the archived relation is a dimension
   */
  public void setIsDimension(boolean isDimension) 
  {
    this.isDimension = isDimension;
  }  
  
  
  /**
   * @return whether the archived relation is a dimension
   */
  public boolean isDimension()
  {
    return this.isDimension;
  }
  
  /**
   * @return the isOuterJoinTypeNode
   */
  public boolean isOuterJoinTypeNode()
  {
    return isOuterJoinTypeNode;
  }

  /**
   * @param isOuterJoinTypeNode the isOuterJoinTypeNode to set
   */
  public void setOuterJoinTypeNode(boolean isOuterJoinTypeNode)
  {
    this.isOuterJoinTypeNode = isOuterJoinTypeNode;
    // set the flag once any outer join type reported
    isANSIOuterJoinInFromClause = !isANSIOuterJoinInFromClause ?
                                  isOuterJoinTypeNode:
                                  isANSIOuterJoinInFromClause;       
  }

  /**
   * @return the isANSIOuterJoinInFromClause
   */
  public boolean isANSIOuterJoinInFromClause()
  {
    return isANSIOuterJoinInFromClause;
  }
  
  /**
   * @return the tableFunctionSpec
   */
  public TableFunctionRelationSpec getTableFunctionSpec()
  {
    return tableFunctionSpec;
  }

  /**
   * @param tableFunctionSpec the tableFunctionSpec to set
   */
  public void setTableFunctionSpec(TableFunctionRelationSpec tableFunctionSpec)
  {
    this.tableFunctionSpec = tableFunctionSpec;
  }

  public void setInterpretingPartnExpr(boolean val)
  {
    this.isInterpretingPartnExpr = val;
  }

  public boolean isInterpretingPartnExpr()
  {
    return this.isInterpretingPartnExpr;
  }

}
