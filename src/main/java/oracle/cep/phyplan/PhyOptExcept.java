/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptExcept.java /main/7 2009/11/09 10:10:58 sborah Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Except Physical Operator in the package oracle.cep.phyplan

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sborah      10/06/09 - bigdecimal support
 sborah      04/28/09 - set byte length
 hopark      10/09/08 - remove statics
 anasrini    09/15/08 - set numAttrs from equivalen LogOpt
 hopark      10/25/07 - set synopsis
 parujain    12/20/06 - operator sharing
 rkomurav    09/13/06 - physynpos OO restructuring
 rkomurav    08/29/06 - add genXMLPlan2
 rkomurav    08/21/06 - adding toString
 najain      08/08/06 - fix except
 dlenkov     06/12/06 - implementation
 najain      04/06/06 - cleanup
 anasrini    04/06/06 - constructor cleanup 
 najain      03/24/06 - cleanup
 skaluska    02/15/06 - Move Synopsis from execution to phyplan 
 najain      02/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptExcept.java /main/7 2009/11/09 10:10:58 sborah Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan;

import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.logplan.LogOpt;

import oracle.cep.logplan.LogOptKind;
import oracle.cep.service.ExecContext;

/**
 * Except Physical Operator 
 */
public class PhyOptExcept extends PhyOpt {
  /** Internal Synopsis */
  public static final int COUNTSYN_IDX = 0;
  /** Output lineage synopsis */
  public static final int OUTPUTSYN_IDX = 1;

  public PhySynopsis getOutSynopsis() {
    return getSynopsis(COUNTSYN_IDX);
  }

  public void setOutSynopsis( PhySynopsis syn) {
    setSynopsis(COUNTSYN_IDX, syn);
  }

  public PhySynopsis getCountSynopsis() {
    return getSynopsis(OUTPUTSYN_IDX);  
  }

  public void setCountSynopsis( PhySynopsis syn) {
    setSynopsis(OUTPUTSYN_IDX, syn);
  }

  public PhyOptExcept(ExecContext ec) throws PhysicalPlanException {
    super( ec, PhyOptKind.PO_EXCEPT);
  }

  public PhyOptExcept( ExecContext ec, LogOpt logPlan, PhyOpt[] phyChildPlans)
      throws PhysicalPlanException {

    super( ec, PhyOptKind.PO_EXCEPT);

    assert logPlan != null;
    assert logPlan.getNumInputs() == 2;
    assert logPlan.getOperatorKind() == LogOptKind.LO_EXCEPT;

    PhyOpt left = phyChildPlans [0];
    PhyOpt right = phyChildPlans [1];
    
    LogOpt logOuter = logPlan.getInput(Constants.OUTER);
    LogOpt logInner = logPlan.getInput(Constants.INNER);

    assert ( logOuter.getNumOutAttrs() == logInner.getNumOutAttrs() );

    // output schema = one of the input schema
    copy(left, logPlan);

    int rl;
    Datatype ld;
    Datatype rd;
    for (int i = 0; i < getNumAttrs(); i++) {
      ld = left.getAttrTypes(i);
      rd = right.getAttrTypes(i);
      assert ld == rd;

      rl = right.getAttrLen(i);
      if (ld == Datatype.CHAR || ld == Datatype.BYTE) 
      {
        if (rl > left.getAttrLen(i))
          getAttrMetadata()[i].setLength(rl);
      }
      else
         assert rl == left.getAttrLen(i);
    }

    setIsStream( false);
    setNumInputs( 2);
    getInputs()[0] = left;
    getInputs()[1] = right;

    left.addOutput( this);
    right.addOutput( this);

    setOutSynopsis(null);
    setCountSynopsis(null);
  }

  public void synStoreReq() {
    PhySynopsis countSyn = getCountSynopsis();
    assert countSyn != null;
    PhyStore store = new PhyStore(execContext, PhyStoreKind.PHY_REL_STORE);    
    store.setOwnOp(this);
    countSyn.makeStub(store);
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<PhysicalOperatorExcept>");
    sb.append(super.toString());
    
    sb.append("<InnerSynopsis>");
    PhySynopsis countSyn = getCountSynopsis();
    sb.append(countSyn.toString());
    sb.append("</InnerSynopsis>");
                                                                                                                             
    sb.append("<OuterSynopsis>");
    PhySynopsis outSyn = getOutSynopsis();
    sb.append(outSyn.toString());
    sb.append("</OuterSynopsis>");
    
    sb.append("</PhysicalOperatorExcept>");
    return sb.toString();
  }
  
  //Create and return visualiser compatible XML Plan
  public String getXMLPlan2() throws CEPException {
    StringBuilder xml = new StringBuilder();
    xml.append("<name> Except </name>\n");
    xml.append("<lname> Except </lname>\n");
    xml.append(super.getXMLPlan2());
    return xml.toString();
  }
  
  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    assert(getOutSynopsis() == syn || getCountSynopsis() == syn);
    return PhySynPos.RIGHT.getName();
  }
  
  /**
   * This method tells whether the two operators are partially equivalent or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(!(opt instanceof PhyOptExcept))
      return false;
  
   // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
 
    PhyOptExcept exceptOpt = (PhyOptExcept)opt;
  
    assert exceptOpt.getOperatorKind() == PhyOptKind.PO_EXCEPT;
  
    if(exceptOpt.getNumInputs() != this.getNumInputs())
      return false;
  
    if(exceptOpt.getNumAttrs() != this.getNumAttrs())
      return false;
    
    return true;
    
  }

}
