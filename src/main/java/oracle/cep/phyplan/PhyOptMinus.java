/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptMinus.java /main/11 2013/04/25 21:06:16 vikshukl Exp $ */

/* Copyright (c) 2007, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    04/17/13 - pass input operator to
                           isDependentOnChildSynAndStore()
    udeshmuk    10/20/11 - API for knowing if this operator uses child's
                           synopsis
    sborah      10/07/09 - bigdecimal support
    sbishnoi    05/15/09 - fixing Assertion Error
    sborah      04/28/09 - set byte length
    sborah      04/20/09 - reorganize sharing hash
    sborah      03/17/09 - define sharingHash
    hopark      10/09/08 - remove statics
    sborah      09/23/08 - ser numAttrs from equivalent LogOpt
    hopark      10/25/07 - set synopsis
    sbishnoi    09/26/07 - support for not in
    sbishnoi    09/26/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptMinus.java /main/11 2013/04/25 21:06:16 vikshukl Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */


package oracle.cep.phyplan;

import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.logplan.LogOpt;

import oracle.cep.logplan.LogOptKind;
import oracle.cep.logplan.LogOptMinus;
import oracle.cep.logplan.attr.AttrNamed;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;



/**
 * Minus Physical Operator 
 */
public class PhyOptMinus extends PhyOpt {
  
  /** Output lineage synopsis */
  public static final int OUTSYN_INDEX = 0;
  
  /** Left Input Synopsis */
  public static final int LEFTINPUTSYN_INDEX = 1;
  
  /** Right Input Synopsis */
  public static final int RIGHTINPUTSYN_INDEX = 2;
  
  /** Is this RelSetOp.NOTIN Operation */
  boolean     isNotInSetOp;
  
  /** 
   * V1(c1 integer, c2 float, c3 integer) IN / NOT IN V2(d1 integer, c2 float)
   * Here leftComparisonAttrs = {V1.c2}
   * and rightComparisonAttrs = {V2.c2}
   * and numComparisonAttrs   = 1
   */
  private Attr[] leftComparisonAttrs;
  private Attr[] rightComparisonAttrs;
  int            numComparisonAttrs;

  public PhySynopsis getOutSynopsis() {
    return getSynopsis(OUTSYN_INDEX);
  }

  public void setOutSynopsis( PhySynopsis syn) {
    setSynopsis(OUTSYN_INDEX, syn);
  }

  public PhySynopsis getLeftInputSynopsis() {
    return getSynopsis(LEFTINPUTSYN_INDEX);
  }
  
  public void setLeftInputSynopsis(PhySynopsis syn) {
    setSynopsis(LEFTINPUTSYN_INDEX, syn);
  }
  
  public PhySynopsis getRightInputSynopsis(){
    return getSynopsis(RIGHTINPUTSYN_INDEX);
  }
  
  public void setRightInputSynopsis(PhySynopsis syn) {
    setSynopsis(RIGHTINPUTSYN_INDEX, syn);
  }

  public PhyOptMinus(ExecContext ec) throws PhysicalPlanException {
    super( ec, PhyOptKind.PO_MINUS);
  }

  public PhyOptMinus( ExecContext ec, LogOpt logPlan, PhyOpt[] phyChildPlans)
      throws PhysicalPlanException {

    super( ec, PhyOptKind.PO_MINUS);

    LogOptMinus  logOptMinus;
    
    assert logPlan != null;
    assert logPlan.getNumInputs() == 2;
    assert logPlan.getOperatorKind() == LogOptKind.LO_MINUS;
    
    PhyOpt left = phyChildPlans [0];
    PhyOpt right = phyChildPlans [1];
    
    logOptMinus = (LogOptMinus)logPlan;
    this.isNotInSetOp = logOptMinus.getIsNotInSetOp();

    // If Set Operation == MINUS then output schema = one of the input schema
    // If Set Operation == NOT IN then output schema [= OR !=] input schema
    if(!this.isNotInSetOp)
    { 
      //assert ( left.getNumAttrs() == right.getNumAttrs());
       
      //CHECK !!
      //ALT code for assert condition
      LogOpt logOuter = logPlan.getInput(Constants.OUTER);
      LogOpt logInner = logPlan.getInput(Constants.INNER);
  
      assert ( logOuter.getNumOutAttrs() == logInner.getNumOutAttrs() );
      this.numComparisonAttrs = logOptMinus.getNumComparisonAttrs();
      //END OF CHECK !!
    }
    else
     {
      transformAttrs(logPlan);
     }

    copy(left, logPlan);
    
    int rl;
    Datatype ld;
    Datatype rd;
    
    // If RelSet Operation = MINUS then
    //   Assert left schema = right Schema and
    //   Adjust Character attribute length if required
    if(!this.isNotInSetOp)
    {
      for (int i = 0; i < getNumAttrs(); i++) 
      {
        ld = left.getAttrTypes(i);
        rd = right.getAttrTypes(i);
        assert ld == rd;

        rl = right.getAttrLen(i);
        if (ld == Datatype.CHAR || ld == Datatype.BYTE) 
        {
          if (rl > left.getAttrLen(i))
            setAttrLen(i, rl);
        }
        else
          assert rl == left.getAttrLen(i);
      }
    }
    //TODO: apply checks for Attr Length if RelSetOp = NOT IN
    
    setIsStream( false);
    setNumInputs( 2);
    getInputs()[0] = left;
    getInputs()[1] = right;

    left.addOutput( this);
    right.addOutput( this);

    setOutSynopsis(null);
    setLeftInputSynopsis(null);
    setRightInputSynopsis(null);
    
  }

  /**
   * Method to calculate a concise String representation
   * of the Physical operator based on its attributes.
   * @return 
   *      A concise String representation of the Physical operator.
   */
  protected String getSignature()
  {
    return (this.getOperatorKind() + "#"
          + this.getNumAttrs() + "#"
          + this.getIsNotInSetOp());
  }
  
  public void synStoreReq() {
    PhyStore leftInputStore  = this.getInputs()[0].getSharedRelStore();
    PhyStore rightInputStore = this.getInputs()[1].getSharedRelStore();
    assert leftInputStore != null;
    assert rightInputStore != null;
    PhySynopsis leftInputSyn = getLeftInputSynopsis();
    leftInputSyn.makeStub(leftInputStore);
    PhySynopsis rightInputSyn = getRightInputSynopsis();
    rightInputSyn.makeStub(rightInputStore);
  }
  
  @Override
  public boolean isDependentOnChildSynAndStore(PhyOpt input)
  {
    return true;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<PhysicalOperatorMinus>");
    sb.append(super.toString());
    
    sb.append("<OuterSynopsis>");
    PhySynopsis outSyn = getOutSynopsis();
    sb.append(outSyn.toString());
    sb.append("</OuterSynopsis>");
    
    sb.append("<LeftInputSynopsis>");
    PhySynopsis leftInputSyn = getLeftInputSynopsis();
    sb.append(leftInputSyn.toString());
    sb.append("</LeftInputSynopsis>");
    
    sb.append("<RightInputSynopsis>");
    PhySynopsis rightInputSyn = getRightInputSynopsis();
    sb.append(rightInputSyn.toString());
    sb.append("</RightInputSynopsis>");
    
    sb.append("</PhysicalOperatorMinus>");
    return sb.toString();
  }
  
  //Create and return visualiser compatible XML Plan
  public String getXMLPlan2() throws CEPException {
    StringBuilder xml = new StringBuilder();
    xml.append("<name> Minus </name>\n");
    xml.append("<lname> Minus </lname>\n");
    xml.append(super.getXMLPlan2());
    return xml.toString();
  }
  
  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    PhySynopsis leftInputSyn = getLeftInputSynopsis();
    PhySynopsis rightInputSyn = getRightInputSynopsis();
    assert(getOutSynopsis() == syn || leftInputSyn == syn || rightInputSyn == syn);
    if(leftInputSyn == syn)
      return PhySynPos.LEFT.getName();
    else if(rightInputSyn == syn)
      return PhySynPos.RIGHT.getName();
    else
      return PhySynPos.OUTPUT.getName();
  }
  
  /**
   * This method tells whether the two operators are partially equivalent or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(!(opt instanceof PhyOptMinus))
      return false;
  
   // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
 
    PhyOptMinus minusOpt = (PhyOptMinus)opt;
  
    assert minusOpt.getOperatorKind() == PhyOptKind.PO_MINUS;
  
    if(minusOpt.getNumInputs() != this.getNumInputs())
      return false;
  
    if(minusOpt.getNumAttrs() != this.getNumAttrs())
      return false;
    
    // Both Operator should be either NOTIN or MINUS
    if(minusOpt.getIsNotInSetOp() != this.getIsNotInSetOp())
      return false;
    
    // If Both Operator are NOTIN then
    //   Both should have same number of Comparison Attributes
    if(minusOpt.getIsNotInSetOp() && this.getIsNotInSetOp())
    {
      if(minusOpt.getNumComparisonAttrs() != this.getNumComparisonAttrs())
        return false;
    }
    
    return true;
    
  }
  
  /**
   * Transform logPlan Attributes to PhyPlan Attributes 
   * @param logPlan
   */
  public void transformAttrs(LogOpt logPlan)
  {
    LogOptMinus logOptMinus = (LogOptMinus)logPlan;
    AttrNamed leftComparisonAttr;
    AttrNamed rightComparisonAttr;
    
    this.numComparisonAttrs   = logOptMinus.getNumComparisonAttrs();
    this.leftComparisonAttrs  = new Attr[numComparisonAttrs];
    this.rightComparisonAttrs = new Attr[numComparisonAttrs];
    
    for(int i=0; i < this.numComparisonAttrs ; i++)
    {
      leftComparisonAttr = (AttrNamed)(logOptMinus.getLeftComparisonAttrs()[i]);
      this.leftComparisonAttrs[i] = new Attr(0, leftComparisonAttr.getAttrId());
      
      rightComparisonAttr = (AttrNamed)(logOptMinus.getRightComparisonAttrs()[i]);
      this.rightComparisonAttrs[i] = new Attr(1, rightComparisonAttr.getAttrId());
    }
  }
  
  /**
   * Get Number of comparison attributes
   * @return numComparisonAttrs
   */
  public int getNumComparisonAttrs()
  {
    return this.numComparisonAttrs;
  }
  
  /**
   * Get Left Comparison Attribute array
   * @return leftComparisonAttrs
   */
  public Attr[] getLeftComparisonAttrs()
  {
    return this.leftComparisonAttrs;
  }
  
  /**
   * Get Right Comparison Attribute Array
   * @return rightComparisonAttrs
   */
  public Attr[] getRightComparisonAttrs()
  {
    return this.rightComparisonAttrs;
  }
  
  /**Check whether RelSetOp = NOT IN */
  public boolean getIsNotInSetOp()
  {
    return this.isNotInSetOp;
  }

}
