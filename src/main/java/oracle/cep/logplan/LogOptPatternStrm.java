/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */
/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptPatternStrm.java /main/26 2011/05/17 03:26:06 anasrini Exp $ */


/*
   DESCRIPTION
    Logical operator for pattern stream operator

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      04/11/11 - use getAllReferencedAttrs()
    sborah      03/29/11 - override canPartitionExprBePushed()
    udeshmuk    03/09/10 - within clause support
    udeshmuk    03/05/09 - remove project op for measures
    udeshmuk    02/02/09 - support for duration arith_expr in pattern
    sborah      12/16/08 - handle constants
    udeshmuk    10/15/08 - support for xmlagg orderby in pattern
    sbishnoi    07/28/08 - support for nanosecond; updating comments
    udeshmuk    07/12/08 - 
    rkomurav    07/07/08 - add is recurring non event flag
    udeshmuk    07/01/08 - support for xmlagg.
    rkomurav    05/15/08 - add durationMilSecs for non event detection
    rkomurav    03/18/08 - add subset clause
    rkomurav    02/28/08 - parametereize errors
    rkomurav    02/21/08 - replace DFA with NFA
    rkomurav    01/03/08 - remove statetoalph map
    anasrini    09/26/07 - ALL MATCHES support
    rkomurav    09/25/07 - add prev range
    rkomurav    09/06/07 - add maxprevidex
    rkomurav    07/03/07 - uda
    anasrini    07/02/07 - support for partition by clause
    sbishnoi    06/08/07 - support for Multi-arg UDAs
    rkomurav    06/19/07 - add statetoalph map
    rkomurav    05/14/07 - add classB flag
    rkomurav    05/29/07 - change the datatype of map
    anasrini    05/25/07 - add methods addCorrAttr, addAggr
    rkomurav    03/13/07 - add bindlength
    rkomurav    02/27/07 - rename
    rkomurav    02/16/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptPatternStrm.java /main/24 2010/03/23 01:50:14 udeshmuk Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan;

import java.util.ArrayList;

import oracle.cep.common.AggrFunction;
import oracle.cep.common.BaseAggrFn;
import oracle.cep.common.TimeUnit;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrAggr;
import oracle.cep.logplan.attr.AttrXMLAgg;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprOrderBy;
import oracle.cep.logplan.pattern.CorrNameDef;
import oracle.cep.logplan.pattern.SubsetCorr;
import oracle.cep.pattern.PatternSkip;
import oracle.cep.util.DFA;
import oracle.cep.util.NFA;

public class LogOptPatternStrm extends LogOpt implements Cloneable
{

  /** Array of partition by attributes */
  Attr[]              partnAttrs;

  /** NFA for the pattern specified if class B */
  NFA                 nfa;
  
  /** DFA for the pattern specified if class A */
  DFA                 dfa;
  
  /** Correlation definitions */
  CorrNameDef[]       corrDefs;
  
  /** Mapping of alphabet to state number */
  int[]               alphabetToStateMap;
  
  /** aggregation attributes */
  ArrayList<AttrAggr> aggrAttrs;

  /** Number of attributes that are aggregation expressions */
  int                 numAggrAttrs;
  
  /** classB flag */
  boolean             isClassB;
  
  /** maximum prev index */
  int                 maxPrevIndex;
  
  /** maximum prev range */
  long                maxPrevRange;
  
  /** is prev range exists */
  boolean             prevRangeExists;

  /** SKIP clause */
  PatternSkip         skipClause;
  
  /** subset correlations */
  SubsetCorr[]        subsetCorrs;
  
  /** is non event detection case */
  boolean             isNonEvent;
  
  /** true if within clause present, false otherwise */
  boolean             isWithin;
  
  /** true if within inclusive clause present, false otherwise */
  boolean             isWithinInclusive;
  
  /** duration in nanoseconds */
  long                durationValue;
  
  /** alphabet Index for the duration symbold */
  int                 durationSymAlphIndex;
  
  /** is it recurring non event detection? */
  boolean             isRecurringNonEvent;
  
  /** does the duration clause contain expr */
  boolean             isDurationExpr;
  
  /** duration clause expression */
  Expr                durationExpr;
  
  /** timeunit of duration clause expression */
  TimeUnit            durationUnit;
  
  /** measure clause expressions */
  Expr[]              measureExprs;
  
  /**
   * Constructor
   * @param input the input logical operator
   * @param partnAttrs attributes to partition by
   * @param dfa dfa for the given pattern if class A
   * @param nfa nfa for the given pattern if class B
   * @param corrDefs logical bool definitions for correlation names
   * @param corrDefToAlphabet its a map from correlation name order 
   *                          to state number order
   * @param maxPrevIndex maximum index of all the prev functions in the
   *                          current recognize clause 
   * @param maxPrevRange max range for all the prev functions
   * @param prevRangeExists if any prev function has a range
   * @param skipClause the SKIP clause
   * @param subsetCorrs subset correlation list
   * @param isNonEvent flag indicating if its a non event detection case
   * @param isWithin true if within clause is present
   * @param isWithinInclusive true if within inclusive clause is present
   * @param duration duration in terms of nanoseconds for a non event
   *                        detection case
   * @param durationSymAlphIndex alphabet Index for the duration symbold
   */
  public LogOptPatternStrm(LogOpt input, Attr[] partnAttrs, DFA dfa, NFA nfa, 
                           CorrNameDef[] corrDefs,  int[] alphabetToStateMap,
                           boolean isClassB, int maxPrevIndex,
                           long maxPrevRange, boolean prevRangeExists,
                           PatternSkip skipClause, SubsetCorr[] subsetCorrs,
                           boolean isNonEvent, boolean isWithin,
			   boolean isWithinInclusive, long duration,
                           int durationSymAlphIndex,
                           boolean isRecurringNonEvent,
                           boolean isDurationExpr,
                           Expr durationExpr, TimeUnit durationUnit, 
                           Expr[] measureExprs)
  {
    super(LogOptKind.LO_PATTERN_STRM);
    assert input != null;
    
    setNumInputs(1);
    setInput(0, input);
    input.setOutput(this);

    //set that output of this operator is a stream
    setIsStream(true);
    
    this.partnAttrs           = partnAttrs;
    this.dfa                  = dfa;
    this.nfa                  = nfa;
    this.corrDefs             = corrDefs;
    this.alphabetToStateMap   = alphabetToStateMap;
    this.aggrAttrs            = new ArrayList<AttrAggr>();
    this.isClassB             = isClassB;
    this.numAggrAttrs         = 0;
    this.maxPrevIndex         = maxPrevIndex;
    this.maxPrevRange         = maxPrevRange;
    this.prevRangeExists      = prevRangeExists;
    this.skipClause           = skipClause;
    this.subsetCorrs          = subsetCorrs;
    this.isNonEvent           = isNonEvent;
    this.isWithin             = isWithin;
    this.isWithinInclusive    = isWithinInclusive;
    this.durationValue        = duration;
    this.durationSymAlphIndex = durationSymAlphIndex;
    this.isRecurringNonEvent  = isRecurringNonEvent;
    this.isDurationExpr       = isDurationExpr;
    this.durationExpr         = durationExpr;
    this.durationUnit         = durationUnit;
    this.measureExprs         = measureExprs;
    
    // Initialize out attribute counts    
    setNumOutAttrs(0);
  }

  /**
   * Add an aggregate attribute to the list of attributes 
   * @param fn aggregate function to be added to the list
   * @param expr aggregate parameter expression
   */
  void addAggr(BaseAggrFn fn, Expr[] expr, ExprOrderBy[] orderExpr) 
    throws LogicalPlanException {

    AttrAggr attrAggr; 
    
    if(fn.getFnCode() == AggrFunction.XML_AGG)
      attrAggr = new AttrXMLAgg(expr,fn,false,null,orderExpr);
    else
      attrAggr = new AttrAggr(expr, fn);

    aggrAttrs.add(attrAggr);
    numAggrAttrs++;
  }

  
  /**
   * Can this partitionParallel expression be pushed down to the 
   * specified input 
   * @param inputNo the specified input number 
   * @return the Logical layer form of the expression that can serve as
   *         the partitionParallel expression of input inputNo
   */
  public Expr canPartitionExprBePushed(int inputNo)
  {
    // cannot push down expressions if the pattern operator has no partition by
    // clause
    if(partnAttrs == null)
      return null;
    
    Expr ppExpr = getPartitionParallelExpr();
    
    if(ppExpr == null)
      return null;
    
    // get a list of all the attributes in the parallel partitioning expression
    ArrayList<Attr> attrs = new ArrayList<Attr>();
    ppExpr.getAllReferencedAttrs(attrs);
    
    // check if all attributes in the parallel paritioning expression
    // are present in the partition attributes.
    for(Attr attr : attrs)
    {
      boolean found = false;
      for(int i = 0; i < partnAttrs.length; i++)
      {
        if(attr.equals(partnAttrs[i]))
        {
          found = true;
          break;
        }
      }
      if(!found)
        return null;
    }
    
    LogUtil.fine(LoggerType.TRACE,
        "Partition parallelism possible for input " + inputNo
        + " of logical op " + operatorKind 
        + " with expression " + ppExpr);
    
    return ppExpr;
  }
  
  // Getter methods

  /**
   * @return the attributes in the partition by clause
   */
  public Attr[] getPartnAttrs() 
  {
    return partnAttrs;
  }

  /**
   * Get the number of partition by attributes
   * @return the number of partition by attributes 
   */
  public int getNumPartnAttrs() {
    if (partnAttrs != null)
      return partnAttrs.length;
    else
      return 0;
  }

  /**
   * @return the corrDefs
   */
  public CorrNameDef[] getCorrDefs()
  {
    return corrDefs;
  }

  /**
   * @return the dfa
   */
  public NFA getNfa()
  {
    return nfa;
  }

  /**
   * @return the dfa
   */
  public DFA getDfa()
  {
    return dfa;
  }

  /**
   * @return the map from correlation defs. order to state number order
   */
  public int[] getAlphabetToStateMap()
  {
    return alphabetToStateMap;
  }

  /**
   * Get the number of aggregation attributes
   * @return the number of aggregation attributes
   */
  public int getNumAggrAttrs() {
    return numAggrAttrs;
  }

  /**
   * Get the aggregation attributes
   * @return the aggregation attributes
   */
  public AttrAggr[] getAggrAttrs() {
    return aggrAttrs.toArray(new AttrAggr[0]);
  }

  /**
   * @return the bindLength
   */
  public int getBindLength()
  {
    // The number of tuples in  a binding is given as follows 
    // number of correlation names defined + number of subset corrs + 
    // 1 tuple for all aggregations
    int numCorrDefs;
    int numSubsetCorrs;
    numCorrDefs = corrDefs.length;
    if(subsetCorrs != null)
      numSubsetCorrs = subsetCorrs.length;
    else
      numSubsetCorrs = 0;
    return numCorrDefs + numSubsetCorrs + 1;
  }
  /**
   * @return the isClassB
   */
  public boolean isClassB()
  {
    return isClassB;
  }

  /**
   * @return the maxPrevIndex
   */
  public int getMaxPrevIndex()
  {
    return maxPrevIndex;
  }

  /**
   * Get the SKIP clause
   * @return the SKIP clause
   */
  public PatternSkip getSkipClause() {
    return skipClause;
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
   * @return the subsetCorrs
   */
  public SubsetCorr[] getSubsetCorrs()
  {
    return subsetCorrs;
  }

  /**
   * @return the duration value
   */
  public long getDurationValue()
  {
    return durationValue;
  }

  /**
   * @return true if the duration clause has expr
   */
  public boolean isDurationExpr()
  {
    return isDurationExpr;
  }
  
  /**
   * @return duration clause expression
   */
  public Expr getDurationExpr()
  {
    return durationExpr;
  }
  
  /**
   * @return time unit for duration clause expression
   */
  public TimeUnit getDurationUnit()
  {
    return durationUnit;
  }
  
  /**
   * @return the isNonEvent
   */
  public boolean isNonEvent()
  {
    return isNonEvent;
  }

  /**
   * @return isWithin value
   */
  public boolean isWithin()
  {
    return isWithin;
  }

  /**
   * @return isWithinInclusive value
   */
  public boolean isWithinInclusive()
  {
    return isWithinInclusive;
  }
   
  /**
   * @return the durationSymAlphIndex
   */
  public int getDurationSymAlphIndex()
  {
    return durationSymAlphIndex;
  }

  /**
   * @return the isRecurringNonEvent
   */
  public boolean isRecurringNonEvent()
  {
    return isRecurringNonEvent;
  }
  
  /**
   * @return measure clause expressions
   */
  public Expr[] getMeasureExprs()
  {
    return this.measureExprs;
  }
}

