/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SFWQuery.java /main/26 2014/10/14 06:35:33 udeshmuk Exp $ */

/* Copyright (c) 2006, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    Post Semantic Analysis representation of a select-from-where query, a 
    query without set operations

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/18/14 - add isDependentOnPartnStream
    vikshukl    08/01/12 - add dimension attribute to addRelation
    pkali       03/21/12 - groupbyexpr support
    vikshukl    11/07/11 - group by expr
    vikshukl    07/13/11 - subquery support
    vikshukl    06/20/11 - subquery support
    udeshmuk    03/22/11 - isArchived flags for relations
    anasrini    03/19/11 - indentation fixes
    sbishnoi    09/24/09 - support for table function
    parujain    05/18/09 - ansi outer join support
    sbishnoi    03/17/09 - adding nonProjExprs
    sbishnoi    03/05/09 - adding support to process partition by in order by
                           clause
    sbishnoi    02/11/09 - changing isMonotonic
    sbishnoi    02/09/09 - support for ordered window
    sborah      11/21/08 - handle constants
    hopark      10/10/08 - remove statics
    parujain    09/08/08 - support offset
    udeshmuk    04/26/08 - parameterize remaining errors.
    udeshmuk    04/19/08 - support for aggr distinct
    parujain    03/10/08 - derived timestamp
    udeshmuk    02/05/08 - parameterize error.
    najain      12/05/07 - xmltable support
    parujain    06/22/07 - order by support
    rkomurav    05/29/07 - tostring resturucture
    anasrini    05/23/07 - symbol table reorg
    sbishnoi    04/25/07 - support for having clause
    rkomurav    03/05/07 - implement getTableId in semquery
    rkomurav    02/22/07 - cleanup reftables
    rkomurav    02/08/07 - add pattern spec
    dlenkov     11/16/06 - right operand can be null
    parujain    11/03/06 - Tree representation for conditions
    rkomurav    09/19/06 - bug 5446939
    anasrini    06/02/06 - support for syntax shortcuts 
    anasrini    02/28/06 - getFromClauseTables - return array of correct size 
    anasrini    02/27/06 - fix xml closing in toString 
    anasrini    02/26/06 - diagnostic information 
    anasrini    02/26/06 - add implementation 
    anasrini    02/23/06 - support for select clause 
    anasrini    02/23/06 - add support for select list and group by 
    anasrini    02/22/06 - Support for where clause 
    anasrini    02/21/06 - make it a class 
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SFWQuery.java /main/26 2014/10/14 06:35:33 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import java.util.ArrayList;
import java.util.Iterator;

import oracle.cep.common.QueryType;
import oracle.cep.common.WindowType;
import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ExpandableArray;
import oracle.cep.exceptions.CEPException;
import oracle.cep.service.ExecContext;

/**
 * This class is the post-semantic analysis representation for a 
 * select-from-where query, a query without set operations
 *
 * @since 1.0
 */

public class SFWQuery extends SemQuery {

  /** collection of all varIds referred by from clause in a plain list*/
  protected ArrayList<Integer>      fromClauseTables;
  
  /** collection of RelationSpec where each entry will belong to one item in
   *  the comma separated from clause entries.
   *  Note: For each from clause entry(comma separated entities),
   *  the corresponding RelationSpec Object will be in form of a tree structure
   *  = If from clause entry is a outer join relation node; this object will
   *  contain all the RelSpecs of the relations/streams referred in the node
   *  = Else it will have a simple RelSpec for that particular stream/relation
   *  Note: 
   *  Total count of RelationSpecs node in the tree will be same as 
   *  fromClauseTables.size();
   *  Need:
   *  This will be the list used in logical layer while creating LogOptCross
   */
  protected ArrayList<RelationSpec> fromClauseEntries;
  
  protected int                     numFromClauseTables;
  protected ArrayList<WindowSpec>   winSpec;
  protected ArrayList<SubquerySpec> subquerySpec;
  protected BExpr                   predicate;
  protected ArrayList<Expr>         projExprs;
  protected boolean                 isDistinct; 
  protected ArrayList<Attr>         gbyAttrs;
  protected int                     numGbyAttrs;
  protected ArrayList<PatternSpec>  patternSpec;
  protected ArrayList<XmlTableSpec> xmlTableSpec;
  protected BExpr                   havingPredicate;
  protected ArrayList<Expr>         orderbyExprs;
  protected ArrayList<DerivedTimeSpec> derivedTsSpec;
  protected ArrayList<Boolean>      isArchivedReln;
  protected ArrayList<Boolean>      isDimensionReln; 
  protected ArrayList<Boolean>      isPartnStream;
  
  //added to support implementation of aggregate distinct
  protected int                     numGbyExprs;
  protected ArrayList<Expr>         gbyExprs; // this seems nothing but AttrExpr
                                              // wrapper around Attr. This has
                                              // to change as GROUP BY is no
                                              // longer constrained to Attrs

  /** 
   * variables for order-by window e.g. Select c1 from S order by c1 rows 10
   *  so numOrderByRows = 10 
   */
  protected int                     numOrderByRows;
  
  /** list of partition by attributes mentioned in order by clause */
  protected ArrayList<Expr>         partitionByAttrs;
  
  /** List of table function relation specification*/
  protected ArrayList<TableFunctionRelationSpec> tableFunctionRelationSpec;
  
  // Constructor

  protected SFWQuery() {
    fromClauseTables    = new ExpandableArray<Integer>(
                              Constants.INITIAL_TABLES_CAPACITY);
    fromClauseEntries   = new ExpandableArray<RelationSpec>(
                              Constants.INITIAL_TABLES_CAPACITY);
    winSpec             = new ExpandableArray<WindowSpec>(
                              Constants.INITIAL_TABLES_CAPACITY);
    subquerySpec       = new  ExpandableArray<SubquerySpec>(
                              Constants.INITIAL_TABLES_CAPACITY);
    patternSpec         = new ExpandableArray<PatternSpec>(
                              Constants.INITIAL_TABLES_CAPACITY);
    xmlTableSpec        = new ExpandableArray<XmlTableSpec>(
                               Constants.INITIAL_TABLES_CAPACITY);
    derivedTsSpec       = new ExpandableArray<DerivedTimeSpec>(
                              Constants.INITIAL_TABLES_CAPACITY);
    isArchivedReln      = new ExpandableArray<Boolean>(
                              Constants.INITIAL_TABLES_CAPACITY);
    isDimensionReln     = new ExpandableArray<Boolean>(
                              Constants.INITIAL_TABLES_CAPACITY);
    isPartnStream       = new ExpandableArray<Boolean>(
                              Constants.INITIAL_TABLES_CAPACITY);
    projExprs           = new ExpandableArray<Expr>();
    numFromClauseTables = 0;
    isDistinct          = false;
    gbyAttrs            = new ExpandableArray<Attr>(
                              Constants.INITIAL_NUM_GROUP_ATTRS);
    numGbyAttrs         = 0;
    predicate           = null;
    havingPredicate     = null;
    orderbyExprs        = new ExpandableArray<Expr>();
    gbyExprs            = new ExpandableArray<Expr>(
                              Constants.INITIAL_NUM_GROUP_EXPRS);
    numGbyExprs         = 0;
    numOrderByRows      = 0;
    
    tableFunctionRelationSpec = new ExpandableArray<TableFunctionRelationSpec>
                                (Constants.INITIAL_TABLES_CAPACITY); 
                        
  }



  public QueryType getQueryType() { 
    return QueryType.SFW_QUERY;
  }

  /**
   * Get the relations listed in the FROM clause of the query. The relation
   * will be represented by its internal identifier
   * @return array of internal relation identifiers for the relations occuring
   *         in the FROM clause
   */
  public int[] getFromClauseTables() {
    int[] retarr = new int[numFromClauseTables];
    
    for(int i=0; i<numFromClauseTables; i++)
      retarr[i] = fromClauseTables.get(i);
    return retarr;
  }
  
  public ArrayList<RelationSpec> getFromClauseEntries()
  {
    return fromClauseEntries;
  }

  /**
   * Get the number of relation entries in the from clause
   * @return the number of relations in the from clause
   */
  public int getNumFromClauseEntries() {
    return fromClauseEntries.size();
  }
  
  /**
   * Get the window specification for a relation in the from clause
   * @param varId the internal relation identifier
   * @return the window specification associated with the relation,
   *         if the relation references a base stream, null if the associated
   *         base table is a relation. For streams where a window
   *         clause has not been explicitly specified, the default spec for
   *         an UNBOUNDED window will be returned
   */
  public WindowSpec getWindowSpec(int varId) 
  {
    if (varId < 0 )
      throw new IllegalArgumentException(varId + "");
    
    return winSpec.get(varId);
  }

  /**
   * Get the subquery specification for a relation in the from clause
   * @param varId internal relation identifier
   * @return the subquery specification associated with this relation.
   */
  public SubquerySpec getSubquerySpec(int varId) {
    if (varId < 0 )
      throw new IllegalArgumentException(varId + "");
    
    return subquerySpec.get(varId);
  }
  
  /**
   * Get the derived time specs
   * @param varId the internal relation identifier
   * @return
   */
  public DerivedTimeSpec getDerivedTimeSpec(int varId) 
  {
    if (varId < 0 )
      throw new IllegalArgumentException(varId + "");

    return derivedTsSpec.get(varId);
  
  }

  /**
   * @return true if relation is archived, false otherwise
   * @param varId the internal relation identifier
   */
  public boolean isArchived(int varId)
  {
    if(varId < 0) 
      throw new IllegalArgumentException(varId + "");

    return isArchivedReln.get(varId);
  }

  /**
   * @return true if the query is dependent on partition stream, false otherwise
   */
  public boolean isDependentOnPartnStream()
  {
    for(Boolean b : isPartnStream)
    {
      if((b!= null) && (b.booleanValue()))
        return true;
    }
    return false;
  }
 
  /**
   * @return true if the query is dependent on archived relation, false otherwise
   */
  public boolean isDependentOnArchivedRelation()
  {
    for(Boolean b : isArchivedReln)
    {
      if((b != null) && (b.booleanValue()))
        return true;
    }
    return false;
  }
  
  /** 
   * @return true if the query is dependent on archived dimension, false
   *         otherwise.
   */
  public boolean isDependentOnArchivedDimension()
  {
    for(Boolean b : isDimensionReln)
    {
      if ((b != null) && (b.booleanValue()))
        return true;
    }
    return false;
  }  
  
  /**
   * get the pattern specification for a relation
   * @param varId the internal relation identifier
   * @return the pattern specification
   */
  public PatternSpec getPatternSpec(int varId)
  {
    if(varId < 0 )
      throw new IllegalArgumentException(varId + "");
  
    return patternSpec.get(varId);
    
  }

  public XmlTableSpec getXmlTableSpec(int varId)
  {
    if(varId < 0)
      throw new IllegalArgumentException(varId + "");
   
    return xmlTableSpec.get(varId);
    
  }
  
  public TableFunctionRelationSpec getTableFunctionRelSpec(int varId)
  {
     if(varId < 0)
       throw new IllegalArgumentException(varId + "");
     
     return tableFunctionRelationSpec.get(varId);
  }

  /**
   * Boolean  predicates  occurring  in  the  where  clause. 
   * @return the boolean predicate tree expression occurring in the where clause,
   *         null if a where clause does not exist
   */
  public BExpr getPredicate() {
    return predicate;
  }

  
  /**
   * Get all the select list expressions
   * @return all the select list expressions
   */
  public ArrayList<Expr> getSelectListExprs() {
    return projExprs;
  }
  
  public int getSelectListSize()
  {
    return projExprs.size();
  }
  
  /**
   * Get all the order by list expressions
   * @return all the order by list expressions
   */
  public ArrayList<Expr> getOrderByExprs()
  {
    return orderbyExprs;
  }
  
  /**
   * Get whether there is any order by expr or not.
   * 
   * @return Is List Empty or not
   */
  public boolean isOrderByListEmpty()
  {
    return orderbyExprs.isEmpty();
  }

  /**
   * Is there a distinct operation on top of a SFW_BLOCK
   * @return true if and only if there is a distinct operation
   */
  public boolean isDistinct() {
    return isDistinct;
  }

  /**
   * Get the expressions in the GROUP BY clause
   * @return the expressions in the GROUP BY clause
   */
  public Expr[] getGroupByExprs() {
    Expr[] retarr = new Expr[numGbyExprs];
    
    for(int i = 0; i < numGbyExprs; i++)
    {
      retarr[i] = gbyExprs.get(i);
    }
    return retarr;
  }
  
  /**
   * Boolean predicates occurring in Having clause. 
   * @return the boolean predicate tree expression occuring in the Having clause,
   *         null if a Having clause does not exist
   */
  public BExpr getHavingPredicate() {
    return havingPredicate;
  }

  boolean isMonotonicRel(ExecContext ec) {
    WindowSpec ws;

    // An SFWQuery is a monotonic relation in the following case
    // 1) All the from clause elements are streams with unbounded windows
    // 2) Select list contains no aggregation expressions
    // 3) Simple Order-by clause without any rows clause

    // This applies only to relations, so if stream return false
    if (isStreamQuery())
      return false;
    
    // If order-by clause is present and number of rows specified is non-zero
    //  then return false
    if(numOrderByRows > 0)
      return false;
    
    int index;
    for (int i=0; i < numFromClauseTables; i++) 
    {
      index = fromClauseTables.get(i);
      ws    = winSpec.get(index);
    
      // Is it a base relation ?
      if (ws == null)
        return false;

      // Is it a RANGE window
      if (ws.getWindowType() != WindowType.RANGE)
        return false;

      // It is an UNBOUNDED window spec
      assert ws instanceof TimeWindowSpec : ws.getClass().getName();
      if (!(((TimeWindowSpec)ws).isUnboundedSpec()))
        return false;
    }

    for (int i=0; i<projExprs.size(); i++) 
    {
      // Is it an aggregation 
      if (projExprs.get(i).getExprType() == ExprType.E_AGGR_EXPR)
        return false;
      else if (projExprs.get(i).getExprType() == ExprType.E_COMP_EXPR) {
        if (isAggrInCompExpr((ComplexExpr)projExprs.get(i)))
          return false;
      }
      else if (projExprs.get(i).getExprType() == ExprType.E_GROUP_BY_EXPR) {
        // GROUP BY is wrapper around other expression types, peek into 
        // the actual expression
        GroupByExpr gbyexpr = (GroupByExpr)(projExprs.get(i));
        Expr expr = gbyexpr.getExpr();        
        if (expr instanceof ComplexExpr)
        {
          ComplexExpr cexpr = (ComplexExpr) expr;
          if (isAggrInCompExpr(cexpr))
            return false;
        }       
      }
    }
    return true;
  }

  // Returns true if an aggregate expression is present in the complex expression
  private boolean isAggrInCompExpr(ComplexExpr e) {
    ExprType lt = e.getLeftOperand().getExprType();

    if (lt == ExprType.E_AGGR_EXPR)
    {
      return true;
    }
    else if (lt  == ExprType.E_COMP_EXPR) 
    {
      if (isAggrInCompExpr((ComplexExpr)e.getLeftOperand()))
        return true;
    }
    else if (lt == ExprType.E_GROUP_BY_EXPR) 
    {
      // GROUP BY is wrapper around other expression types, peek into 
      // the actual expression
      GroupByExpr gbyexpr = (GroupByExpr)(e.getLeftOperand());
      Expr expr = gbyexpr.getExpr();        
      if (expr instanceof ComplexExpr)
      {
        ComplexExpr cexpr = (ComplexExpr) expr;
        if (isAggrInCompExpr(cexpr))
          return false;
      }       
    }
        
    if (e.getRightOperand() == null)
      return false;
    ExprType rt = e.getRightOperand().getExprType();

    if(rt == ExprType.E_AGGR_EXPR)
    {
      return true;
    }
    else if(rt == ExprType.E_COMP_EXPR) 
    {
      if(isAggrInCompExpr((ComplexExpr)e.getRightOperand()))
        return true;
    }
    else if (rt == ExprType.E_GROUP_BY_EXPR) 
    {
      // GROUP BY is wrapper around other expression types, peek into 
      // the actual expression
      GroupByExpr gbyexpr = (GroupByExpr)(e.getRightOperand());
      Expr expr = gbyexpr.getExpr();        
      if (expr instanceof ComplexExpr)
      {
        ComplexExpr cexpr = (ComplexExpr) expr;
        if (isAggrInCompExpr(cexpr))
          return false;
      }       
    }
    return false;
  }

  // Setter methods

  /**
   * Add each relation that is encountered in the FROM clause. If a relation
   * is a window relation, then the window specification is also provided
   * pattern specification is provided for every from clause relation
   * if no pattern is specified it is null
   * 
   * @param varId the internal relation identifier
   * @param winspec the window specification if one is associated with the
   *                relation (either specified explicitly or implicitly added
   *                by default), null otherwise
   * @param subqSpec the subquery from which the relation is derived               
   * @param patternspec pattern specification
   * @param XmlTableSpec XML table specification (if any)
   * @param DerivedTimeSpec Spec for derived timestamp
   * @param TableFunctionRelationSpec table function spec
   * @param isArchived if archived relation
   * @param isDimension if the archived relation is a dimension
   * @param isPartitioned if the stream is partitioned
   */
  void addRelation(RelationSpec spec, 
                   SubquerySpec subqSpec,
                   WindowSpec winspec, 
                   PatternSpec pSpec, 
                   XmlTableSpec xSpec, 
                   DerivedTimeSpec dtspec,
                   TableFunctionRelationSpec tableFuncRelSpec,
                   boolean isArchived,
                   boolean isDimension,
                   boolean isPartitioned) 
  {
    // RelationSpec should be initialized for each relation
    assert spec != null;
    int varID = spec.getVarId();
    fromClauseTables.set(numFromClauseTables, varID);  
    numFromClauseTables++;

    winSpec.set(varID, winspec);
    subquerySpec.set(varID, subqSpec);
    patternSpec.set(varID, pSpec);
    xmlTableSpec.set(varID, xSpec);
    derivedTsSpec.set(varID,dtspec);
    tableFunctionRelationSpec.set(varID, tableFuncRelSpec);
    isArchivedReln.set(varID, new Boolean(isArchived));
    isDimensionReln.set(varID, new Boolean(isDimension));
    isPartnStream.set(varID, new Boolean(isPartitioned));
  }

  /**
   * Add the from clause entries
   * @param spec
   */
  void addFromClauseEntries(RelationSpec spec)
  {
    fromClauseEntries.add(spec);
  }

  /**
   * Set the where clause predicate. The predicate is a tree of BExprs.   
   * @param condition the where clause predicate as a tree of predicates 
   * @throws CEPException 
   *
   */
  void setPredicate(BExpr condition) throws CEPException {

    predicate = condition;
  }

  /**
   * Add an expression to the GROUP BY clause
   * @param gbyExpr the expression to be added
   * @throws CEPException if the limit on the number of attributes in the
   *                      GROUP BY clause is exceeded
   */
  void addGroupByExpr(Expr gbyExpr) throws CEPException
  {
    gbyExprs.add(gbyExpr);
    numGbyExprs++;
  }
  
  void setHavingPredicate(BExpr condition) throws CEPException {
    havingPredicate = condition;
  }

  /**
   * Set the Order by List expressions
   * @param orderList ArrayList of orderby list expressions
   */
  void setOrderByList(ArrayList<Expr> orderList)
  {
    orderbyExprs = orderList;
  }
  

  /**
   * Set the select list expressions.
   * @param selectList array of select list expressions
   */
  void setSelectList(Expr[] selectList)  
  {
    for(int i = 0; i < selectList.length; i++)
    {
      projExprs.set(i, selectList[i]);
    }
  }

  /**
   * Is there a DISTINCT operation for this query
   * @param isDistinct true if and only if there a DISTINCT operation 
   * for this query
   */
  void setIsDistinct(boolean isDistinct) {
    this.isDistinct = isDistinct;
  }
  
  /**
   * Setter for numOrderByRows
   * @param paramNumOrderByRows
   */
  public void setNumOrderByRows(int paramNumOrderByRows)
  {
    numOrderByRows = paramNumOrderByRows;
  }
  
  /**
   * Getter for numOrderByRows
   * @return maximum number of rows at any time in output of OrderBy
   */
  public int getNumOrderByRows()
  {
    return numOrderByRows;
  }
  
  /**
   * Set partition by attributes array list
   * @param paramPartitionByAttrs
   */
  public void setPartitionByAttrs(ArrayList<Expr> paramPartitionByAttrs)
  {
    partitionByAttrs = paramPartitionByAttrs;
  }
  
  /**
   * Get partition by attributes ( sub-clause of order by top)
   * @return array list of partition by attributes
   */
  public ArrayList<Expr> getPartitionByAttrs()
  {
    return partitionByAttrs != null ? new ArrayList<Expr>(partitionByAttrs) 
                                    : null;
  }
  
   // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    int           varId;
    
    if (isStreamQuery()) 
      sb.append("<SFWQuery streamOp=\"" + r2sop + "\" >");
    else
      sb.append("<SFWQuery>");

    // Process from clause list
    sb.append("<FromClause>");
    for (int i=0; i<numFromClauseTables; i++) {
      varId   = fromClauseTables.get(i);
      sb.append("<Relation varId=\"" + varId + "\" >");
      if (winSpec.get(i) != null)
        sb.append(winSpec.get(i).toString());
      sb.append("</Relation>");
    }
    sb.append("</FromClause>");

    // Process where clause
    sb.append("<WhereClause>");
    if (predicate != null) {
      sb.append(predicate.toString());
      
    }
    sb.append("</WhereClause>");

    // Process select list
    sb.append("<SelectList distinct=\"" + isDistinct + "\" >");
    for (int i=0; i<projExprs.size(); i++) {
      sb.append(projExprs.get(i).toString());
    }
    sb.append("</SelectList>");

    // Process the GROUP BY clause
    sb.append("<GroupBy>");
    for (int i=0; i<numGbyAttrs; i++) {
      sb.append(gbyAttrs.get(i).toString());
    }
    sb.append("</GroupBy>");

    //Process Having Clause
    sb.append("<Having>");
    if(havingPredicate != null)
      sb.append(havingPredicate.toString());
    
    sb.append("</Having>");
    
    // Process Order by Clause
    if(orderbyExprs != null)
    {
      sb.append("<OrderBy>");
      for(int i=0; i<orderbyExprs.size(); i++) {
        sb.append(orderbyExprs.get(i).toString());
      }
      if(numOrderByRows > 0)
      {
        sb.append("<numOrderByRows>");
        sb.append(numOrderByRows);
        sb.append("</numOrderByRows>");
      }
      if(partitionByAttrs != null)
      {
        sb.append("<numPartitionByAttributes>");
        sb.append(partitionByAttrs.size());
        sb.append("</numPartitionByAttributes>");
      }
      sb.append("</OrderBy>");
    }
     
    sb.append("</SFWQuery>");

    return sb.toString();
  }

  public void printFromClauseTables()
  {
    Iterator<Integer> iter = fromClauseTables.iterator();
    System.out.print("From Clause Tables: ");
    while(iter.hasNext())
    {
      System.out.print(iter.next() + " ");
    }
    System.out.println("\n");
  }
  
  public void printFromClauseEntries()
  {
    Iterator<RelationSpec> iter = fromClauseEntries.iterator();
    System.out.print("From Clause Entries: ");
    while(iter.hasNext())
    {
      System.out.print(iter.next() + " ");
    }
    System.out.println("\n");
  }
}
