/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptUnion.java /main/12 2012/07/13 02:49:24 sbishnoi Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Union Physical Operator in the package oracle.cep.phyplan

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 pkali       07/05/12 - added isAssignableFrom for datatype comparison
 sborah      10/12/09 - support for bigdecimal
 sborah      04/28/09 - set byte length
 sborah      04/20/09 - reorganize sharing hash
 sborah      03/18/09 - define sharingHash
 hopark      10/09/08 - remove statics
 sborah      09/23/08 - set numAttrs from equivalent LogOpt
 hopark      10/25/07 - set synopsis
 sbishnoi    07/12/07 - modified xml tag string in genXMLPlan2
 sbishnoi    04/12/07 - countsyn code cleanup
 sbishnoi    04/04/07 - support for union all
 parujain    12/20/06 - operator sharing
 rkomurav    09/13/06 - PhySynPos OO restructuring
 rkomurav    08/29/06 - add genXMLPlan2
 rkomurav    08/20/06 - adding toString
 dlenkov     06/12/06 - union support
 najain      04/06/06 - cleanup
 anasrini    04/06/06 - constructor cleanup 
 najain      03/24/06 - cleanup
 skaluska    02/15/06 - Move Synopsis from execution to phyplan 
 najain      02/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptUnion.java /main/12 2012/07/13 02:49:24 sbishnoi Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan;

import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptKind;
import oracle.cep.logplan.LogOptUnion;
import oracle.cep.service.ExecContext;

/**
 * Union Physical Operator 
 */
public class PhyOptUnion extends PhyOpt {
  
  boolean isUnionAll;
  
  public PhySynopsis getOutSynopsis() {
    return getSynopsis(0);
  }
  
  public void setOutSynopsis( PhySynopsis syn) {
    setSynopsis(0, syn);
  }
  
  
  /**
   * check for union all flag
   * @return true if operation is 'union all' 
   */
  public boolean isUnionAll() {
    return isUnionAll;
  }
  
  /**
   * set the union all flag
   * @param isUnionAll will be true for [union all] operation
   */
  public void setIsUnionAll( boolean isUnionAll) {
    this.isUnionAll = isUnionAll;
  }
  
  public PhyOptUnion(ExecContext ec) throws PhysicalPlanException {
    super(ec, PhyOptKind.PO_UNION);
  }
  
  public PhyOptUnion( ExecContext ec, LogOpt logPlan, PhyOpt[] phyChildPlans)
  throws PhysicalPlanException 
  {
    
    super( ec, PhyOptKind.PO_UNION);
    
    LogOptUnion logPlanUnion = (LogOptUnion)logPlan;
    
    assert logPlanUnion != null;
    assert logPlanUnion.getNumInputs() == 2;
    assert logPlanUnion.getOperatorKind() == LogOptKind.LO_UNION;
    
    setIsUnionAll(logPlanUnion.isUnionAll());
    PhyOpt left  = phyChildPlans [0];
    PhyOpt right = phyChildPlans [1];
    
    LogOpt logOuter = logPlan.getInput(Constants.OUTER);
    LogOpt logInner = logPlan.getInput(Constants.INNER);
    
    assert ( logOuter.getNumOutAttrs() == logInner.getNumOutAttrs() );
    
    // output schema = one of the input schema
    copy(left,logPlan);
    
    int rl;
    Datatype ld;
    Datatype rd;
    for (int i = 0; i < getNumAttrs(); i++) 
    {
      ld = left.getAttrTypes(i);
      rd = right.getAttrTypes(i);
      assert ld == rd || ld.isAssignableFrom(rd);
      
      rl = right.getAttrLen(i);
      if(ld == Datatype.CHAR || ld == Datatype.BYTE)
      {
        if (rl > left.getAttrLen(i))
          setAttrLen(i, rl);
      }
      else
        assert rl == left.getAttrLen(i);
    }
    
    setIsStream( left.getIsStream() && right.getIsStream());
    setNumInputs(2);
    getInputs()[0] = left;
    getInputs()[1] = right;
    
    left.addOutput( this);
    right.addOutput( this);
    
    setOutSynopsis(null);
    
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
        + this.isUnionAll() + "#"
        + this.getNumAttrs());
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<PhysicalOperatorUnion>");
    sb.append(super.toString());
    PhySynopsis outSyn = getOutSynopsis();
    if(outSyn != null) {
      sb.append("<OuterSynopsis>");
      sb.append(outSyn.toString());
      sb.append("</OuterSynopsis>");
    }
    
    sb.append("</PhysicalOperatorUnion>");
    return sb.toString();
  }
  
  //Create and return visualiser compatible XML Plan
  public String getXMLPlan2() throws CEPException 
  {
    StringBuilder xml = new StringBuilder();
    if(this.isUnionAll())
    {
      xml.append("<name> Union All </name>\n");
      xml.append("<lname> Union All </lname>\n");
      xml.append(super.getXMLPlan2());
      return xml.toString();
    }
    else
    {
      xml.append("<name> Union </name>\n");
      xml.append("<lname> Union </lname>\n");
      xml.append(super.getXMLPlan2());
      return xml.toString();
    }
  }
  
  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    // Union has no Relation Synopsis
    if(this.isUnionAll())
    {
      assert(false);
      return null;
    }
    else
    {
      assert(getOutSynopsis() == syn);
      return PhySynPos.RIGHT.getName();
    }
  }
  
  /**
   * This method tells whether the two operators are partially equivalent or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(!(opt instanceof PhyOptUnion))
      return false;
    
    // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
    
    PhyOptUnion unionOpt = (PhyOptUnion)opt;
    
    assert unionOpt.getOperatorKind() == PhyOptKind.PO_UNION;
    
    if(unionOpt.isUnionAll() != this.isUnionAll())
      return false;
    
    if(unionOpt.getNumInputs() != this.getNumInputs())
      return false;
    
    if(unionOpt.getNumAttrs() != this.getNumAttrs())
      return false;
    
    return true;
    
  }
  
  
}
