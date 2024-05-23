/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptPatternStrm.java /main/11 2009/11/09 10:10:59 sborah Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Physical level operator for pattern stream

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      10/07/09 - bigdecimal support
    sborah      04/20/09 - reorganize sharing hash
    sborah      03/17/09 - define sharingHash
    hopark      10/09/08 - remove statics
    mthatte     11/01/07 - using Datatype.getLength()
    rkomurav    05/30/07 - rename and change datatype for map
    anasrini    05/29/07 - aggregate types
    anasrini    05/28/07 - aggregates support
    rkomurav    04/12/07 - add xmlplan2
    rkomurav    04/02/07 - add isPartialequivalent function
    rkomurav    03/13/07 - add bindlength
    rkomurav    02/27/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptPatternStrm.java /main/11 2009/11/09 10:10:59 sborah Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan;

import java.util.ArrayList;

import oracle.cep.common.Datatype;
import oracle.cep.common.BaseAggrFn;
import oracle.cep.exceptions.CEPException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.attr.CorrAttr;
import oracle.cep.phyplan.pattern.CorrNameDef;
import oracle.cep.service.ExecContext;
import oracle.cep.util.DFA;

public class PhyOptPatternStrm extends PhyOpt
{
  /** Correlation definitions */
  CorrNameDef[] corrDefs;
  
  /** DFA */
  DFA dfa;
  
  /** Map from state number to correlation names */
  int[] alphabetToStateMap;
  
  /** Binding length */
  int bindLength;

  /** Number of correlation attributes */
  int numCorrAttrs;

  /** Number of aggregate attributes */
  int numAggrAttrs;

  /** The correlation attributes */
  CorrAttr[] corrAttrs;

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

  

  /**
   * Constructor
   * @param ec TODO
   * @param input the physical operator that is the input to this operator
   * @param corrDefs the correlation definitions
   * @param dfa the automaton
   * @param corrAttrs the output attributes that are correlation attributes
   * @param aggrAttrs the output attributes that are aggregation attributes
   * @param bindLength the number of tuples in the binding
   * @param map map from correlation position to automaton state 
   */
  public PhyOptPatternStrm(ExecContext ec, PhyOpt input, CorrNameDef[] corrDefs,
                           DFA dfa, int[] alphabetToStateMap,
                           CorrAttr[] corrAttrs, CorrAttr[] aggrAttrs, int bindLength)
      throws PhysicalPlanException
  {
    super(ec, PhyOptKind.PO_PATTERN_STRM, input, false, false);
    this.corrDefs           = corrDefs;
    this.dfa                = dfa;
    this.alphabetToStateMap = alphabetToStateMap;
    this.bindLength         = bindLength;
    this.aggrAttrs          = aggrAttrs;
    this.corrAttrs          = corrAttrs;

    // Output is a stream
    setIsStream(true);

    int numCorrs = corrDefs.length;

    numCorrAttrs = getNumCorrAttrs();
    numAggrAttrs = getNumAggrAttrs();

    aggrFns         = new BaseAggrFn[numAggrAttrs];
    aggrParamExprs  = new ArrayList<Expr[]>();
    aggrInputTypes  = new ArrayList<Datatype[]>();
    aggrOutputTypes = new Datatype[numAggrAttrs];

    BaseAggrFn[]          fns;
    ArrayList<Expr[]>     params;
    ArrayList<Datatype[]> inpTypes;
    Datatype[]            outTypes;
    int                   numAggrs;
    int                   c = 0;

    for (int i = 0; i < numCorrs; i++)
    {
      numAggrs = corrDefs[i].getNumAggrs();
      fns      = corrDefs[i].getAggrFns();
      params   = corrDefs[i].getAggrParamExprs();
      inpTypes = corrDefs[i].getAggrInputTypes();
      outTypes = corrDefs[i].getAggrOutputTypes();

      if (numAggrs > 0) 
      {
        System.arraycopy(fns, 0, aggrFns, c, numAggrs);
        aggrParamExprs.addAll(params);
        aggrInputTypes.addAll(inpTypes);
        System.arraycopy(outTypes, 0, aggrOutputTypes, c, numAggrs);
      }
      c += numAggrs;
    }
    assert c == numAggrAttrs;

    int totalAttrs = numCorrAttrs + numAggrAttrs;
    setNumAttrs(totalAttrs);
    
    for (int i = 0; i < numCorrAttrs; i++)
    {
      /*attrTypes[i] = input.getAttrTypes()[corrAttrs[i].getPos()];
      attrLen[i]   = input.getAttrLen()[corrAttrs[i].getPos()];*/
      
      setAttrMetadata(i, input.getAttrMetadata()[corrAttrs[i].getPos()]);
    }
    
    int      index;
    
    for (int a=numCorrAttrs; a<totalAttrs; a++) 
    {
      index        = a-numCorrAttrs;
      setAttrTypes(a, aggrOutputTypes[index]);

      //lengths are defined as constants, getter defined in Datatype.java      
      setAttrLen(a, aggrOutputTypes[index].getLength());
    }
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
    for (CorrAttr ca : this.corrAttrs)
    {
      expr.append("(" + ca.getInput() + "," + ca.getPos() + ","
          + ca.getBindPos() + ")");
    }
    expr.append(this.getOperatorKind() + "#AggrAttrs:");
    for (CorrAttr aa : this.aggrAttrs)
    {
      expr.append("(" + aa.getInput() + "," + aa.getPos() + ","
          + aa.getBindPos() + ")");
    }
    expr.append("#" + this.bindLength);
    
    LogUtil.info(LoggerType.TRACE, "PatternStrm : ["+expr.toString()+"]");
    
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
  public CorrNameDef[] getCorrDefs() {
    return corrDefs;
  }

  /**
   * @return the dfa
   */
  public DFA getDfa() {
    return dfa;
  }

  /**
   * @return the map
   */
  public int[] getAlphabetToStateMap() {
    return alphabetToStateMap;
  }

  /**
   * @return the bindLength
   */
  public int getBindLength() {
    return bindLength;
  }

  /**
   * @return all the aggregate functions
   */
  public BaseAggrFn[] getAggrFns() {
    return aggrFns;
  }

  /**
   * @return all the aggregate param expressions 
   */
  public ArrayList<Expr[]> getAggrParamExprs() {
    return aggrParamExprs;
  }

  /**
   * @return the number of aggregate attributes
   */
  public int getNumAggrAttrs() {
    if (aggrAttrs == null)
      return 0;

    return aggrAttrs.length;
  }

  /**
   * @return the output aggregate attributes
   */
  public CorrAttr[] getAggrAttrs() {
    return aggrAttrs;
  }

  /**
   * @return array of input types to the aggregate functions
   */
  public ArrayList<Datatype[]> getAggrInputTypes() {
    return aggrInputTypes;
  }

  /**
   * @return array of return types of the aggregate functions
   */
  public Datatype[] getAggrOutputTypes() {
    return aggrOutputTypes;
  }


  /**
   * @return the number of correlation attributes
   */
  public int getNumCorrAttrs() {
    if (corrAttrs == null)
      return 0;

    return corrAttrs.length;
  }

  /**
   * @return the output correlation attributes
   */
  public CorrAttr[] getCorrAttrs() {
    return corrAttrs;
  }
  
  /**
   * This method tells whether the two operators are partially equivalent 
   * or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(!(opt instanceof PhyOptPatternStrm))
      return false;
  
   // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
 
    PhyOptPatternStrm patternOpt = (PhyOptPatternStrm)opt;
  
    assert patternOpt.getOperatorKind() == PhyOptKind.PO_PATTERN_STRM;
  
    if(patternOpt.getNumInputs() != this.getNumInputs())
      return false;
  
    if(patternOpt.getNumCorrAttrs() != this.getNumCorrAttrs())
      return false;

    if(patternOpt.getNumAggrAttrs() != this.getNumAggrAttrs())
      return false;
    
    if(patternOpt.getBindLength() != this.getBindLength())
      return false;

    if(!dfa.equals(patternOpt.getDfa()))
        return false;

    return compareCorrDefs(patternOpt);
    
  }
  
  private boolean compareCorrDefs(PhyOptPatternStrm patternOpt)
  {
    CorrNameDef[] corrCompare = patternOpt.getCorrDefs();
    for(int i = 0; i < corrDefs.length; i++)
    {
      if(!corrDefs[i].equals(corrCompare[i]))
        return false;
    }
    return true;
  }

  // Generate and return visualiser compatible XML plan
  public String getXMLPlan2() throws CEPException
  {
    StringBuilder xml = new StringBuilder();
    xml.append("<name> PatternStrm </name>\n");
    xml.append("<lname> Pattern Stream </lname>\n");
    xml.append(super.getXMLPlan2());
    //add properties
    return xml.toString();
  }
  
}

