/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhyOptDStrm.java /main/5 2008/10/24 15:50:14 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates.
 All rights reserved. */

/*
 DESCRIPTION
 Delete Stream Physical Operator in the package oracle.cep.phyplan

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      10/07/08 - use execContext to remove statics
 sborah      09/23/08 - pass in equiv logopt in constructor
 hopark      10/25/07 - set synopsis
 parujain    12/20/06 - operator sharing
 rkomurav    09/13/06 - physynpos OO restructuring
 rkomurav    08/29/06 - add genXMLPlan2
 najain      06/17/06 - bug fix 
 najain      06/15/06 - cleanup
 najain      05/25/06 - bug fix 
 ayalaman    04/23/06 - implementation
 najain      04/06/06 - cleanup
 najain      03/24/06 - cleanup
 skaluska    02/15/06 - Move Synopsis from execution to phyplan 
 najain      02/10/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhyOptDStrm.java /main/5 2008/10/24 15:50:14 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan;

import oracle.cep.exceptions.CEPException;
import oracle.cep.logplan.LogOpt;
import oracle.cep.service.ExecContext;

/**
 * Delete Stream Physical Operator 
 */
public class PhyOptDStrm extends PhyOpt {
  /**
  *Constructor.
  *@param input
  *           the physical operator which is the input to this operator
  *@param logopt
  *           the equivalent logical operator
  */
  public PhyOptDStrm(ExecContext ec, PhyOpt input,LogOpt logopt) throws PhysicalPlanException {
    super(ec, PhyOptKind.PO_DSTREAM,  input,logopt, true, false);

    // output is always a stream
    setIsStream(true);
  }

  /**
   * Get the in synopsis
   * @return the in synopsis
   */
  public PhySynopsis getSynopsis() {
    return getSynopsis(0);
  }

  // Setter API
  /**
   * Set the in synopsis
   * @param inSyn the in synopsis
   */
  public void setSynopsis(PhySynopsis inSyn) {
    setSynopsis(0, inSyn);
  }

  // Related to store Sharing 
  public boolean getSharedSynType(int idx) {
    return true;
  }

  /**
   * The synopsis of the store needs to create a new store for itself
   */
  public void synStoreReq()
  {
    PhyStore nowStore = new PhyStore(execContext, PhyStoreKind.PHY_REL_STORE);
    nowStore.setOwnOp(this);
    PhySynopsis nowSyn = getSynopsis();
    nowSyn.makeStub(nowStore);
  }

  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<IStream>");
    sb.append(super.toString());

    PhySynopsis nowSyn = getSynopsis();
    if (nowSyn != null) {
      sb.append("<PhysicalSynopsis>");
      sb.append(nowSyn.toString());
      sb.append("</PhysicalSynopsis>");
    }

    sb.append("</IStream>");
    return sb.toString();
  }

  //Create and return visualiser compatible XML Plan
  public String getXMLPlan2() throws CEPException {
    StringBuilder xml = new StringBuilder();
    xml.append("<name> Dstream </name>\n");
    xml.append("<lname> Dstream </lname>\n");
    xml.append(super.getXMLPlan2());
    return xml.toString();
  }
  
  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    assert(getSynopsis() == syn);
    return PhySynPos.RIGHT.getName();
  }
 
  /**
   * This method tells whether the two operators are partially equivalent or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(!(opt instanceof PhyOptDStrm))
      return false;
  
   // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
 
    PhyOptDStrm strmOpt = (PhyOptDStrm)opt;
  
    assert strmOpt.getOperatorKind() == PhyOptKind.PO_DSTREAM;
  
    if(strmOpt.getNumInputs() != this.getNumInputs())
      return false;
  
    if(strmOpt.getNumAttrs() != this.getNumAttrs())
      return false;
    
    return true;
    
  }


}


