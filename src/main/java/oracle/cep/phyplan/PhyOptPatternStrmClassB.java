/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptPatternStrmClassB.java /main/29 2010/06/09 22:13:58 sbishnoi Exp $ */

/* Copyright (c) 2007, 2010, Oracle and/or its affiliates. 
All rights reserved. */


/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    05/04/10 - setting the flag to receive automatic heartbeats
    udeshmuk    03/09/10 - within clause support
    sborah      10/07/09 - bigdecimal support
    sborah      04/28/09 - use getLength
    udeshmuk    03/18/09 - add partnattrs into isPartialEquivalent
    sborah      03/17/09 - define sharingHash
    udeshmuk    03/16/09 - don't set max length by default for variable length
                           types
    udeshmuk    03/05/09 - remove project operator for measures.
    udeshmuk    02/03/09 - add duration clause in isPartial equivalent
    udeshmuk    02/02/09 - support for duration arith_expr in pattern
    udeshmuk    10/17/08 - support for xmlagg orderby in pattern.
    hopark      10/09/08 - remove statics
    hopark      10/09/08 - remove statics
    udeshmuk    10/10/08 - use pattern specific partition window store.
    udeshmuk    09/07/08 - 
    sbishnoi    07/28/08 - support for nanosecond; updating comments
    udeshmuk    07/12/08 - 
    rkomurav    07/07/08 - add isrecurring non event flag
    rkomurav    05/15/08 - support non event detection
    rkomurav    03/19/08 - support subset
    rkomurav    02/26/08 - remove alphtostate map
    rkomurav    02/21/08 - replace DFA with NFA
    rkomurav    01/03/08 - remove stateToAlphMap
    hopark      10/25/07 - set synopsis
    mthatte     11/01/07 - using Datatype.getLength()
    anasrini    09/26/07 - ALL MATCHES support
    rkomurav    09/25/07 - prev range
    rkomurav    09/12/07 - add prtnsyn
    rkomurav    09/06/07 - add prevIndex
    rkomurav    07/25/07 - fix out store
    rkomurav    07/10/07 - uda
    anasrini    07/12/07 - support for partition by
    rkomurav    05/14/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptPatternStrmClassB.java /main/29 2010/06/09 22:13:58 sbishnoi Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan;

import java.util.ArrayList;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.BaseAggrFn;
import oracle.cep.common.Datatype;
import oracle.cep.common.TimeUnit;
import oracle.cep.exceptions.CEPException;
import oracle.cep.pattern.PatternSkip;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.phyplan.attr.CorrAttr;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprOrderBy;
import oracle.cep.phyplan.pattern.CorrName;
import oracle.cep.phyplan.pattern.CorrNameDef;
import oracle.cep.phyplan.pattern.SubsetCorr;
import oracle.cep.service.ExecContext;
import oracle.cep.util.NFA;

public class PhyOptPatternStrmClassB extends PhyOpt
{
  /** Correlation definitions */
  CorrNameDef[] corrDefs;
  
  /** NFA */
  NFA nfa;
  
  /** Binding length */
  int bindLength;

  /** Number of aggregate attributes */
  int numAggrAttrs;

  /** The partition attributes */
  Attr[] partnAttrs;

  /** The aggregate attributes */
  CorrAttr[] aggrAttrs;

  /** Input datatypes to the aggreagtions */
  ArrayList<Datatype[]> aggrInputTypes;

  /** Output datatypes of the aggregations */
  Datatype[] aggrOutputTypes;

  /** All the aggregate functions */
  BaseAggrFn[] aggrFns;

  /** All the aggregate function params */
  ArrayList<Expr[]> aggrParamExprs;

  /** list of order by exprs */
  ArrayList<ExprOrderBy[]> orderByExprsList;
  
  /** Binding Synopsis */
  public static final int BINDSYN_INDEX = 0;
  
  /** Partition window Synopsis for prev(n) support 
   *  when partition attrs are present
   */
  public static final int PARTNSYN_INDEX = 1;
  
  /** max prev index */
  int maxPrevIndex;
  
  /** max prev range */
  long maxPrevRange;
  
  /** is prev range exists */
  boolean prevRangeExists;

  /** SKIP clause */
  PatternSkip skipClause;
  
  /** Subset correlations */
  SubsetCorr[] subsetCorrs;
  
  /** is non event detection case */
  boolean isNonEvent;
  
  /** isWithin clause */
  boolean isWithin;

  /** isWithinInclusive clause */
  boolean isWithinInclusive;
  
  /** duration in nanoseconds */
  long durationValue;
  
  /** alphabet Index for the duration symbold */
  int durationSymAlphIndex;
  
  /** is it recurring non event detection */
  boolean  isRecurringNonEvent;
  
  /** does the duration clause contain expr */
  boolean  isDurationExpr;
  
  /** duration clause expression */
  Expr     durationExpr;

  /** timeunit of duration clause expression */
  TimeUnit durationUnit;
  
  /** measure clause expressions */
  Expr[]   measureExprs;
  
  /**
   * Constructor
   * @param ec TODO
   * @param input the physical operator that is the input to this operator
   * @param partnAttrs the partition by attributes
   * @param corrDefs the correlation definitions
   * @param aggrAttrs the output attributes that are aggregation attributes
   * @param bindLength the number of tuples in the binding
   * @param maxPrevIndex maximum index of all the prev functions
   * @param skipClause the SKIP clause
   * @param subsetCorrs subset correlations
   * @param isNonEvent flag to indicate if its a non event detection case
   * @param isWithin true if within clause is present
   * @param isWithinInclusive true if within inclusive clause is present
   * @param duration duration in nanoseconds for a non event detection case
   * @param durationSymAlphIndex alphabet Index for the duration symbold
   * @param dfa the automaton
   * @param alphabetToStatemap map from alphabet index to automaton state 
   */
  public PhyOptPatternStrmClassB(ExecContext ec, PhyOpt input,
                                 Attr[] partnAttrs, CorrNameDef[] corrDefs, 
                                 NFA nfa, 
                                 CorrAttr[] aggrAttrs,
                                 int bindLength,
                                 int maxPrevIndex, long maxPrevRange,
                                 boolean prevRangeExists,
                                 PatternSkip skipClause,
                                 SubsetCorr[] subsetCorrs,
                                 boolean isNonEvent,
				 boolean isWithin,
				 boolean isWithinInclusive,
                                 long duration,
                                 int durationSymAlphIndex,
                                 boolean isRecurringNonEvent,
                                 boolean isDurationExpr,
                                 Expr durationExpr, TimeUnit durationUnit,
                                 Expr[] measureExprs)
      throws PhysicalPlanException
  {
    super(ec, PhyOptKind.PO_PATTERN_STRM_CLASSB, input, false, false);
    this.partnAttrs           = partnAttrs;
    this.corrDefs             = corrDefs;
    this.nfa                  = nfa;
    this.bindLength           = bindLength;
    this.aggrAttrs            = aggrAttrs;
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

    // Output is a stream
    setIsStream(true);

    numAggrAttrs = getNumAggrAttrs();

    aggrFns         = new BaseAggrFn[numAggrAttrs];
    aggrParamExprs  = new ArrayList<Expr[]>();
    aggrInputTypes  = new ArrayList<Datatype[]>();
    aggrOutputTypes = new Datatype[numAggrAttrs];

    orderByExprsList = new ArrayList<ExprOrderBy[]>();
    
    int c = 0;
    c = handleAggrs(corrDefs.length, corrDefs, c);
    if(subsetCorrs != null)
      c = handleAggrs(subsetCorrs.length, subsetCorrs, c);
   
    assert c == numAggrAttrs;
    assert aggrParamExprs.size() == numAggrAttrs;
    assert orderByExprsList.size() == numAggrAttrs;

    setNumAttrs(measureExprs.length);
    
    for (int i = 0; i < measureExprs.length; i++)
    {
      /*attrTypes[i] = measureExprs[i].getType();
      attrLen[i]   = measureExprs[i].getLength();
      */
      setAttrMetadata(i, new AttributeMetadata(measureExprs[i].getType(),
                               measureExprs[i].getLength(), 
                               measureExprs[i].getType().getPrecision(),0));
    }
    
    // Pattern operator will need a heartbeat timeout
    if(isWithin || isWithinInclusive || isNonEvent || isDurationExpr)
      setHbtTimeoutRequired(true);
  }
  
  //handle aggregates of correlations (corrDefs and subsets)
  private int handleAggrs(int numCorrs, CorrName[] corrs, int c)
  {
    BaseAggrFn[]             fns;
    ArrayList<Expr[]>        params;
    ArrayList<ExprOrderBy[]> orderByExprs;
    ArrayList<Datatype[]>    inpTypes;
    Datatype[]               outTypes;
    int                      numAggrs;
    
    for (int i=0; i<numCorrs; i++) {
      numAggrs     = corrs[i].getNumAggrs();
      fns          = corrs[i].getAggrFns();
      params       = corrs[i].getAggrParamExprs();
      orderByExprs = corrs[i].getOrderByExprs();
      inpTypes     = corrs[i].getAggrInputTypes();
      outTypes     = corrs[i].getAggrOutputTypes();
      
      if (numAggrs > 0) {
        System.arraycopy(fns, 0, aggrFns, c, numAggrs);
        aggrParamExprs.addAll(params);
        if(orderByExprs != null)
          orderByExprsList.addAll(orderByExprs);
        else{
          for(int j=0; j < numAggrs; j++)
            orderByExprsList.add(null);
        }
          
        aggrInputTypes.addAll(inpTypes);
        System.arraycopy(outTypes, 0, aggrOutputTypes, c, numAggrs);
      }
      c += numAggrs;
    }
    return c;
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Physical operator based on its attributes.
   * @return 
   *      A concise String representation of the Physical operator.
   */
  protected String getSignature()
  {
    StringBuilder expr = new StringBuilder();
    expr.append(this.getOperatorKind() + "#CorrAttrs:");
    for (CorrNameDef cd : this.corrDefs)
    {
      expr.append("(" + cd.getBindPos() + "," + cd.isSingleton() + " )");
    }
    expr.append(this.getOperatorKind() + "#AggrAttrs:");
    for (CorrAttr aa : this.aggrAttrs)
    {
      expr.append(aa.getSignature());
    }
    expr.append("#" + this.bindLength);
    expr.append("#" + getExpressionList(this.measureExprs));
   
    return expr.toString();
  }
  
  
  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn)
  {
    //Pattern doesn't have reln syn
    assert(false);
    return null;
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
   * @return the bindLength
   */
  public int getBindLength()
  {
    return bindLength;
  }
  
  /**
   * @param bindSyn the bindSyn to set
   */
  public void setBindSyn(PhySynopsis bindSyn)
  {
    setSynopsis(BINDSYN_INDEX, bindSyn);
  }
  
  /**
   * @param partnSyn the partnSyn to set
   */
  public void setPartnSyn(PhySynopsis partnSyn)
  {
    setSynopsis(PARTNSYN_INDEX, partnSyn);
  }

  public void synStoreReq()
  {
    PhyStore bindStore = new PhyStore(execContext, PhyStoreKind.PHY_BIND_STORE);
    bindStore.setOwnOp(this);
    PhySynopsis bindSyn = getBindSyn();
    bindSyn.makeStub(bindStore);
    PhySynopsis partnSyn = getPartnSyn();
    if(partnSyn != null)
    {
      PhyStore prtnStore = new PhyStore(execContext, PhyStoreKind.PHY_PVT_PARTN_STORE);
      prtnStore.setOwnOp(this);
      partnSyn.makeStub(prtnStore);
    }
  }

  /**
   * @return the bindSyn
   */
  public PhySynopsis getBindSyn()
  {
    return getSynopsis(BINDSYN_INDEX);
  }
  
  /**
   * @return the partnSyn
   */
  public PhySynopsis getPartnSyn()
  {
    return getSynopsis(PARTNSYN_INDEX);
  }

  /**
   * @return all the aggregate functions
   */
  public BaseAggrFn[] getAggrFns()
  {
    return aggrFns;
  }

  /**
   * @return all the aggregate param expressions 
   */
  public ArrayList<Expr[]> getAggrParamExprs()
  {
    return aggrParamExprs;
  }

  /**
   * @return all the orderby expressions
   */
  public ArrayList<ExprOrderBy[]> getOrderByExprs()
  {
    return orderByExprsList;
  }
  
  /**
   * @return the number of aggregate attributes
   */
  public int getNumAggrAttrs()
  {
    if (aggrAttrs == null)
      return 0;

    return aggrAttrs.length;
  }

  /**
   * @return the output aggregate attributes
   */
  public CorrAttr[] getAggrAttrs()
  {
    return aggrAttrs;
  }

  /**
   * @return array of input types to the aggregate functions
   */
  public ArrayList<Datatype[]> getAggrInputTypes()
  {
    return aggrInputTypes;
  }

  /**
   * @return array of return types of the aggregate functions
   */
  public Datatype[] getAggrOutputTypes()
  {
    return aggrOutputTypes;
  }

  /**
   * @return the number of partition attributes
   */
  public int getNumPartnAttrs()
  {
    if (partnAttrs == null)
      return 0;

    return partnAttrs.length;
  }

  /**
   * @return true if and only if partition by clause is present
   */
  public boolean hasPartnAttrs()
  {
    return (getNumPartnAttrs() != 0);
  }

  /**
   * @return the partition attributes
   */
  public Attr[] getPartnAttrs()
  {
    return partnAttrs;
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
   * @return true if duration clause contains expr
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
   * @retrun measure clause expressions
   */
  public Expr[] getMeasureExprs()
  {
    return this.measureExprs;
  }
  
  /**
   * This method tells whether the two operators are partially equivalent or
   * not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  { 
    if(!(opt instanceof PhyOptPatternStrmClassB))
      return false;
  
   // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
 
    PhyOptPatternStrmClassB patternOpt = (PhyOptPatternStrmClassB)opt;
  
    assert patternOpt.getOperatorKind() == PhyOptKind.PO_PATTERN_STRM_CLASSB;
  
    if(patternOpt.getNumInputs() != this.getNumInputs())
      return false;
  
    if(patternOpt.getNumAttrs() != this.getNumAttrs())
      return false;
    
    if(patternOpt.getBindLength() != this.getBindLength())
      return false;

    if(!nfa.equals(patternOpt.getNfa()))
      return false;

    if (!skipClause.equals(patternOpt.getSkipClause()))
      return false;
    
    //partition attrs comparison
    if(getNumPartnAttrs() != patternOpt.getNumPartnAttrs())
      return false;

    if(getNumPartnAttrs() > 0)
    {
      Attr[] otherPartnAttrs = patternOpt.getPartnAttrs();
      for(int i=0; i < otherPartnAttrs.length; i++)
      {
        if(!partnAttrs[i].equals(otherPartnAttrs[i]))    
          return false;
      }
    }

    CorrNameDef[] corrCompare = patternOpt.getCorrDefs();
    if(corrCompare.length != this.corrDefs.length)
      return false;
    for(int i = 0; i < corrDefs.length; i++)
    {
      if(!corrDefs[i].equals(corrCompare[i]))
        return false;
    }
    
    if(subsetCorrs != null)
    {
      SubsetCorr[] subsetCompare = patternOpt.getSubsetCorrs();
      if(subsetCompare == null)
        return false;
      if(subsetCompare.length != this.subsetCorrs.length)
        return false;
      for(int i = 0; i < subsetCorrs.length; i++)
      {
        if(!subsetCorrs[i].equals(subsetCompare[i]))
          return false;
      }
    }
    else
    {
      if(patternOpt.getSubsetCorrs() != null)
        return false;
    }
    
    Expr[] compareMeasures = patternOpt.getMeasureExprs();
    if(compareMeasures.length != this.measureExprs.length)
      return false;
    for(int i=0; i < measureExprs.length; i++)
    {
      if(!measureExprs[i].equals(compareMeasures[i]))
        return false;
    }
   
    if((isWithin != patternOpt.isWithin)
       || (isWithinInclusive != patternOpt.isWithinInclusive))
    {
    	return false;
    	//the duration value will get compared in the if below.
    }
    
    if(isDurationExpr)
    {
      if(patternOpt.isDurationExpr())
      {
        if(!durationExpr.equals(patternOpt.getDurationExpr()))
          return false;
        if(durationUnit != patternOpt.getDurationUnit())
          return false;
      }
      else
        return false;
    }
    else
    {
      if(!patternOpt.isDurationExpr())
      {  
        if(durationValue != patternOpt.durationValue)
          return false;
      }
      else
        return false;
    }
    
    
    return true;    
  }
  
  // Generate and return visualiser compatible XML plan
  public String getXMLPlan2() throws CEPException
  {
    StringBuilder xml = new StringBuilder();
    xml.append("<name> PatternStrmClasB </name>\n");
    xml.append("<lname> Pattern Stream Class B</lname>\n");
    xml.append(super.getXMLPlan2());
    //add properties
    return xml.toString();
  }

}
