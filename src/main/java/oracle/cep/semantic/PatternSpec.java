/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/PatternSpec.java /main/15 2011/07/14 11:10:28 vikshukl Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Post semantic representation of pattern string and pattern definitions

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    07/12/11 - subquery support
    udeshmuk    03/09/10 - within clause support
    udeshmuk    02/02/09 - support for duration arith_expr in pattern.
    udeshmuk    09/07/08 - 
    sbishnoi    07/28/08 - support of nanosecond; changing variable names and
                           updating comments
    udeshmuk    07/12/08 - 
    rkomurav    07/07/08 - add multiple duration flag
    rkomurav    05/15/08 - add isNonevent and durationmilsecs fields
    rkomurav    05/13/08 - add fields for reglist,statemap and isGroup
    rkomurav    03/18/08 - add subsetcorrs
    rkomurav    03/03/08 - add numAlphs method
    anasrini    09/25/07 - ALL MATCHES support
    rkomurav    09/25/07 - add prev range
    rkomurav    09/06/07 - add previndex count
    anasrini    06/27/07 - support for partition by clause
    rkomurav    06/05/07 - add classB param
    rkomurav    05/14/07 - add classB flag
    anasrini    05/29/07 - add symbolTable
    anasrini    05/24/07 - add varId of base stream
    anasrini    05/23/07 - measures support
    rkomurav    02/07/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/PatternSpec.java /main/14 2010/03/23 01:50:14 udeshmuk Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import java.util.ArrayList;

import oracle.cep.common.RegexpOp;
import oracle.cep.common.TimeUnit;
import oracle.cep.pattern.PatternSkip;

public class PatternSpec
{
  /** varId of the base stream on which MATCH_RECOGNIZE is applied */
  private int                 baseVarId;

  /** attributes in the partition by clause */
  private Attr[]              pBySemAttrs;

  /** pattern tree */
  private Regexp              pattern;
  
  /** correlation definitions */
  private CorrNameDef[]       corrDefs;
  
  /** flag to maintain if its classB
   *  Pattern is ClassB if
   *  there is any reference to other correlations names in the
   *  correlation name definitions
   */
  private boolean             isClassB;

  /** Measure expressions */
  private Expr[]              measures;

  /** Symbol Table for this scope */
  protected SymbolTable       symTable;
  
  /** maximum index for the prev function in the current Recognize clause */
  private int                 maxPrevIndex;
  
  /** number of unique alphabets */
  private int                 numAlphs;
  
  /** maximum range specified for prev */
  private long                maxPrevRange;
  
  /** if range specified for prev */
  private boolean             prevRangeExists;

  /** SKIP clause */
  private PatternSkip         skipClause;
  
  /** subset correlations */
  private SubsetCorr[]        subsetCorrs;
  
  /** list of corrs collected used for building DFA, etc */
  private ArrayList<Integer>  stateMap;
  
  /** list of regexpOps of all the corrs collected in stateMap */
  private ArrayList<RegexpOp> regList;
  
  /** list of isGroup flags */
  private ArrayList<Boolean>  isGroup;
  
  /** is NonEvent Detection Pattern clause */
  private boolean             isNonEvent;
  
  /** duration in nanoseconds for a non event detection pattern clause */
  private long                durationValue;
  
  /** alphabet Index for the duration symbold */
  private int                 durationSymbolAlphIndex;
  
  /** is it Recurring Non Event Detection */
  private boolean             isRecurringNonEvent;
  
  /** does the duration clause contain expr  */
  private boolean             isDurationExpr;

  /** duration clause expression */
  private Expr                durationExpr;
  
  /** time unit for duration clause expression */
  private TimeUnit            durationUnit;

  /** within pattern clause */
  private boolean             isWithin;
  
  /** within inclusive pattern clause */
  private boolean             isWithinInclusive;

  /**
   * Constructor
   * @param baseVarId varId of the base stream on which MATCH_RECOGNIZE is
   *                  applied
   * @param pBySemattrs attributes in the partition by clause
   * @param pattern tree
   * @param corrDefs array of correlation Definitions
   * @param measures measure list expressions
   * @param symTable the symbol table for this scope
   * @param maxPrevIndex maximum value of prev index in RECOGNIZE clause
   * @param skipClause SKIP clause
   * @param subsetCorrs subset correlations
   * @param isNonEvent if its a non event detection pattern
   * @param isWithin if within clause is present 
   * @param isWithinInclusive if within inclusive clause is present 
   * @param duration duration in nanoseconds for a 
   *                        non event detection pattern
   */
  public PatternSpec(int baseVarId, Attr[] pBySemAttrs, Regexp pattern, 
                     CorrNameDef[] corrDefs, Expr[] measures,
                     SymbolTable symTable, boolean isClassB, int maxPrevIndex,
                     boolean prevRangeExists, long maxPrevRange,
                     PatternSkip skipClause, int numAlphs,
                     SubsetCorr[] subsetCorrs, ArrayList<RegexpOp> regList,
                     ArrayList<Integer> stateMap, ArrayList<Boolean> isGroup,
                     boolean isNonEvent, boolean isWithin, 
		     boolean isWithinInclusive, long duration,
                     int durationSymbolAlphIndex,
                     boolean isRecurringNonEvent,
                     boolean isDurationExpr,
                     Expr durationExpr, TimeUnit durationUnit)
  {
    this.baseVarId               = baseVarId;
    this.pBySemAttrs             = pBySemAttrs;
    this.pattern                 = pattern;
    this.corrDefs                = corrDefs;
    this.measures                = measures;
    this.symTable                = symTable;
    this.isClassB                = isClassB;
    this.maxPrevIndex            = maxPrevIndex;
    this.maxPrevRange            = maxPrevRange;
    this.prevRangeExists         = prevRangeExists;
    this.skipClause              = skipClause;
    this.numAlphs                = numAlphs;
    this.subsetCorrs             = subsetCorrs;
    this.regList                 = regList;
    this.stateMap                = stateMap;
    this.isGroup                 = isGroup;
    this.isNonEvent              = isNonEvent;
    this.isWithin                = isWithin;
    this.isWithinInclusive       = isWithinInclusive;
    this.durationValue           = duration;
    this.durationSymbolAlphIndex = durationSymbolAlphIndex;
    this.isRecurringNonEvent     = isRecurringNonEvent;
    this.isDurationExpr          = isDurationExpr;
    this.durationExpr            = durationExpr;
    this.durationUnit            = durationUnit;
  }

  /**
   * @param varId the input table in its internal representation
   * @return the internal metadata identifier for the base table referenced
   */
  public int getBaseTableId()
  {
    SymbolTableSourceEntry sourceEntry;
    
    sourceEntry = symTable.lookupSource(baseVarId);
    
    /* see comments in SemQuery.getTableId */
    if (sourceEntry.getSourceType() == SymbolTableSourceType.INLINE_VIEW)
      return -1;
    else 
      return sourceEntry.getTableId();
  }

  /**
   * @return Returns the baseVarId
   */
  public int getBaseVarId() {
    return baseVarId;
  }

  /**
   * Get the attributes in the partition by clause
   * @return array of attributes in the partition by clause (if present)
   *         If there is no partition by clause, then return null
   */
  public Attr[] getPartitionByAttrs() {
    return pBySemAttrs;
  }

  /**
   * @return Returns the corrDefs.
   */
  public CorrNameDef[] getCorrDefs() {
    return corrDefs;
  }

  /**
   * @return Returns the pattern.
   */
  public Regexp getPattern() {
    return pattern;
  }

  /**
   * @return Returns the measure expressions.
   */
  public Expr[] getMeasures() {
    return measures;
  }

  /**
   * @return the isClassB
   */
  public boolean isClassB() {
    return isClassB;
  }

  /**
   * @return the maxPrevIndex
   */
  public int getMaxPrevIndex() {
    return maxPrevIndex;
  }

  /**
   * @return the maxPrevRange
   */
  public long getMaxPrevRange() {
    return maxPrevRange;
  }

  /**
   * @return the prevRangeExists
   */
  public boolean isPrevRangeExists() {
    return prevRangeExists;
  }

  /**
   * Get the SKIP clause
   * @return the SKIP clause
   */
  public PatternSkip getSkipClause() {
    return skipClause;
  }
  
  /**
   * get the number of alphabets
   * @return the numAlphs
   */
  public int getNumAlphs() {
    return numAlphs;
  }

  /**
   * get the subset correlations
   * @return the subsetCorrs
   */
  public SubsetCorr[] getSubsetCorrs() {
    return subsetCorrs;
  }

  /**
   * Is the SKIP clause ALL MATCHES
   * @return true iff it is ALL MATCHES
   */
  public boolean isAllMatches() {
    return (skipClause == PatternSkip.ALL_MATCHES);
  }

  /**
   * @return the isGroup
   */
  public ArrayList<Boolean> getIsGroup() {
    return isGroup;
  }

  /**
   * @return the regList
   */
  public ArrayList<RegexpOp> getRegList() {
    return regList;
  }

  /**
   * @return the stateMap
   */
  public ArrayList<Integer> getStateMap() {
    return stateMap;
  }

  /**
   * @return the duration
   */
  public long getDurationValue()
  {
    return durationValue;
  }

  /**
   * @return return the duration clause expr
   */
  public Expr getDurationExpr()
  {
    return this.durationExpr;
  }
  
  /**
   * @return time unit of the duration expression
   */
  public TimeUnit getDurationUnit()
  {
    return this.durationUnit;
  }
  
  /**
   * @return true if duration clause has expr
   */
  public boolean isDurationExpr()
  {
    return this.isDurationExpr;
  }
  
  /**
   * @return the isNonEvent
   */
  public boolean isNonEvent()
  {
    return isNonEvent;
  }

  /**
   * @return isWithin
   */
  public boolean isWithin()
  {
    return isWithin;
  }

  /**
   * @return isWithinInclusive
   */
  public boolean isWithinInclusive()
  {
    return isWithinInclusive;
  }
  
  /**
   * @return the durationSymbolAlphIndex
   */
  public int getDurationSymbolAlphIndex()
  {
    return durationSymbolAlphIndex;
  }

  /**
   * @return the isRecurringNonEvent
   */
  public boolean isRecurringNonEvent()
  {
    return isRecurringNonEvent;
  }

}
