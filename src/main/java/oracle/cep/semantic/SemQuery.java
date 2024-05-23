/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SemQuery.java /main/17 2014/10/14 06:35:33 udeshmuk Exp $ */

/* Copyright (c) 2006, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    Internal representation of a query after parse and semantic analysis

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/18/14 - add method to determine if a query is dependent on
                           partitioned stream
    udeshmuk    01/15/13 - XbranchMerge udeshmuk_bug-15962424_ps6 from
                           st_pcbpel_11.1.1.4.0
    vikshukl    08/01/12 - archived dimension
    sbishnoi    05/17/12 - adding slide expression for the feature slide
                           without window
    udeshmuk    07/12/11 - archived relation support
    vikshukl    06/16/11 - subquery support
    anasrini    03/23/11 - support for PARTITION_ORDERED
    vikshukl    09/02/09 - add using clause expression to SemQuery
    sborah      11/21/08 - handle constants
    hopark      10/10/08 - remove statics
    sbishnoi    12/04/07 - support for update semantics
    anasrini    05/25/07 - cleanup
    anasrini    05/23/07 - symbol table reorg
    rkomurav    03/05/07 - add getTableType
    rkomurav    02/22/07 - cleanup reftables
    anasrini    06/02/06 - support for syntax shortcuts 
    najain      05/12/06 - add getSelectListExprs here 
    anasrini    02/25/06 - add implementation 
    anasrini    02/21/06 - Change to class 
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SemQuery.java /main/17 2014/10/14 06:35:33 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import java.util.ArrayList;
import oracle.cep.common.RelToStrOp;
import oracle.cep.common.QueryType;
import oracle.cep.common.Constants;
import oracle.cep.dataStructures.internal.ExpandableArray;
import oracle.cep.service.ExecContext;

/**
 * Internal representation of a parsed and "annotated" query.  Internal
 * representation differs from the parse tree nodes produced by the
 * parser in the following ways:
 * 
 * <ul>
 * <li> All references to streams / relations are through their internal
 *      identifiers and not strings which are external names.
 * <li> Each occurrence of a stream/relation is identified by a variable-id
 *      (note: there could be multiple occurrences of a stream/relation in the
 *      from clause e.g. self-join).
 * <li> Each attribute is identified by a <variable-id , attri-id> - so
 *      the table to which an attribute belongs has been resolved.  For
 *      parse trees some attributes identify the owning table and for
 *      others this is implicit.
 * <li> Every stream in the FROM clause is associated with a window.  In
 *      the parse tree representation, there could be streams without a 
 *      window. The default UNBOUNDED window is added for every stream in the
 *      FROM clause without a window.
 * </ul>
 */

public abstract class SemQuery {

  protected RelToStrOp         r2sop;
  protected SymbolTable        symTable;

  /**
   * Symbol table used while interpreting PATTERN, DEFINE etc. clauses in 
   * MATCH_RECOGNIZE. This would have the entry for the base stream as well.
   * This is needed while interpreting the partition parallel expr.
   */
  protected SymbolTable        patternInlineViewSymTable;
  protected ArrayList<Integer> referencedTables;
  protected int                numReferencedTables;
  protected Integer[]          usingExprListMap; // using clause expr positions

  /**
   * The partition parallel expression associated with this query.
   * This applies only if user ordering constraint is PARTITION_ORDERED
   */
  private Expr                 partitionParallelExpr;
  
  //flag to check if primary key exists or not
  private boolean              isPrimaryKeyExists;
  
  //list of primary key attributes
  private String[]             outputConstraintAttrs;
  
  /** evaluation interval specified in number of nanoseconds */
  private long                 slideInterval;


  /**
   * Constructor
   */
  protected SemQuery() {
    r2sop                 = null;
    referencedTables      = new ExpandableArray<Integer>(
                            Constants.INITIAL_TABLES_CAPACITY);
    numReferencedTables   = 0;
    isPrimaryKeyExists    = false;
    outputConstraintAttrs = null;
    usingExprListMap      = null;
    // Default is -1. 
    slideInterval         = -1l;
    patternInlineViewSymTable = null;
  }

  // Getter methods

  /**
   * Get the type of the query. 
   * Currently either SFW_QUERY, BINARY_OP, NARY_OP and SetOpSubquery
   * @return the type of the query
   */
  public abstract QueryType getQueryType();

  /**
   * The  list of persistent tables  (stream/relations) that  this  query
   * references.  For SFW  queries this is the list  present in the
   * FROM  clause.  Different  instances of  the same  relation are
   * listed  separately.   For binary  operator  queries, the  list
   * contains the  two tables referenced  by the operator. 
   * <p>
   * A table is represented here by its internal identifier as given
   * by the metadata layer.
   * <p>
   * For example, consider the query 
   * <p>
   * select * from S[range 10], R, S[range 5], R
   * <p>
   * For the above query, this method would return an array of length 4 -
   * the number of relations in the from clause of the query. Further, ith
   * element of the returned array would contain the internal metadata layer
   * identifier corresponding to the base relation or base stream object
   * referenced by the ith relation in the from clause.
   * @return array of metadata internal identifiers corresponding to the 
   *         base relation or base stream referenced by the relations.
   *         Note that some identifiers may repeat since more
   *         than one from clause element could reference the same base table.
   */
  public ArrayList<Integer> getReferencedTables() {
    return referencedTables;
  }

  /**
   * Get the number of persistent tables referenced by this query. 
   * Repeated references of a table are counted as many times as the 
   * number of repetitions.
   * @return the number of tables referenced by this query. 
   */
  public int getNumRefTables() {
    return numReferencedTables;
  }

  /**
   * This is called to register a persistent table (stream or relation,
   * base or view) that is referenced by this query. In particular, 
   * this should not be called for inline (non persistent) views
   * @param tableId metadata identifier of the referenced table
   */
  void addReferencedTable(int tableId) {
    referencedTables.add(new Integer(tableId));
    numReferencedTables++;
  }  
  
   
  /**
   * Get the internal metadata identifier for the base table referenced
   * by the input relation. The input relation is identified by its
   * internal representation (namely variable id). Note that the base table
   * referenced will be a stream when the input relation is a window relation
   * 
   * @param varId the input relation in its internal representation
   * @return the internal metadata identifier for the base table referenced
   *         return -1 for in-line views and subquery.
   */
  public int getTableId(int varId)
  {
    SymbolTableSourceEntry sourceEntry;
    
    sourceEntry = symTable.lookupSource(varId);
    if (sourceEntry.getSourceType() == SymbolTableSourceType.INLINE_VIEW)
      return -1;
    else 
      return sourceEntry.getTableId();
  }
  
  /**
   * Get the name for the base table referenced by the input relation. 
   * The input relation is identified by its internal representation 
   * (namely variable id). 
   * Note that the base table referenced will be a stream when the input 
   * relation is a window relation
   * @param varId
   * @return
   */
  public String getTableName(int varId)
  {
    SymbolTableSourceEntry sourceEntry;
    sourceEntry = symTable.lookupSource(varId);
    return sourceEntry.getVarName();
  }
  
  /**
   * Get all the select list expressions
   * @return all the select list expressions
   */
  public abstract ArrayList<Expr> getSelectListExprs(); 
  
  public abstract int getSelectListSize();

  /**
   * Does this query evaluate to a stream or a relation
   * @return true if and only if this query evaluates to a stream
   */
  public boolean isStreamQuery() {
    return (r2sop != null);
  }

  /**
   * Get the relation to stream operator
   * @return the relation to stream operator if this query evaluates to a
   *         stream, null if this query evaluates to a relation
   */
  public RelToStrOp getR2SOp() {
    return r2sop;
  }

  /**
   * Get USING clause expressions
   */
  public Integer[] getUsingExprListMap()
  {
    return usingExprListMap;
  }

  /**
   * Get the partition parallel expression associated with this query
   * @return the associated partition parallel exprsssion or null
   */
  public Expr getPartitionParallelExpr() 
  {
    return partitionParallelExpr;
  }

  // Setter methods

  /**
   * Set the associated partition parallel expression
   * @param partitionParallelExpr the associated partition parallel expression
   */
  void setPartitionParallelExpr(Expr partitionParallelExpr)
  {
    this.partitionParallelExpr = partitionParallelExpr;
  }

  /**
   * Set USING clause expressions (ISTREAM only)
   */
  public void setUsingExprListMap(Integer[] exprListMap)
  {
    usingExprListMap = exprListMap;
  }

  /**
   * Set the relation to stream operator
   * @param r2sop the relation to stream operator
   */
  void setR2SOp(RelToStrOp r2sop) {
    this.r2sop = r2sop;
  }

  /**
   * @param symTable the symTable to set
   */
  public void setSymTable(SymbolTable symTable)
  {
    this.symTable = symTable;
  }
  
  /**
   * Set is primary key exist flag
   * @param isPrimaryKeyExists
   */
  public void setIsPrimaryKeyExists(boolean isPrimaryKeyExists)
  {
    this.isPrimaryKeyExists = isPrimaryKeyExists;
  }
  
  /**
   * Get is primary key exist flag
   * @return true if primary key exists
   */
  public boolean getIsPrimaryKeyExists()
  {
    return this.isPrimaryKeyExists;
  }
  
  /**
   * Set output Constraint attribute list
   * @param outputConstraintAttrs
   */
  public void setOutputConstraintAttrs(String[] outputConstraintAttrs)
  {
    this.outputConstraintAttrs = outputConstraintAttrs;
  }
  
  /**
   * Get list of output constraint attributes
   * @return
   */
  public String[] getOutputConstraintAttrs()
  {
    return this.outputConstraintAttrs;
  }

  /**
   * @return the slideInterval
   */
  public long getSlideInterval()
  {
    return slideInterval;
  }

  /**
   * @param slideInterval the slideInterval to set
   */
  public void setSlideInterval(long slideInterval)
  {
    this.slideInterval = slideInterval;
  }
  
  /**
   * Check if the evaluate clause exists or not
   * @return
   */
  public boolean isEvaluateExists()
  {
    return this.slideInterval != -1l;
  }

  /**
   * Does this query evaluate to a monotonic relation ?
   * <p>
   * A relation R is monotonic iff R(t1) <= R(t2) when t1 <= t2
   * Determining whether an arbitrary relation R is monotonic is a 
   * hard problem. Here we just have a simple conservative static test.
   * <p>
   * It is fine that this method returns false negatives i.e. method
   * can return false even if actually relation is monotonic
   * @param ec Execution Context
   */
  boolean isMonotonicRel(ExecContext ec) {
    // safe implementation
    return false;
  }

  public abstract boolean isDependentOnArchivedRelation();
  
  public abstract boolean isDependentOnArchivedDimension();

  public abstract boolean isDependentOnPartnStream();
  
  public SymbolTable getPatternInlineViewSymTable()
  {
    return patternInlineViewSymTable;
  }

  public void setPatternInlineViewSymTable(SymbolTable symTab)
  {
    this.patternInlineViewSymTable = symTab;
  }
}
