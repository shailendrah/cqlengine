/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptRStrm.java /main/7 2013/04/25 21:06:16 vikshukl Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
  Physical RSTREAM Operator

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 vikshukl    04/17/13 - pass input operator to isDependentOnChildSynAndStore()
 udeshmuk    10/20/11 - API for knowing if this operator uses child's synopsis
 hopark      10/09/08 - remove statics
 sborah      09/23/08 - pass in equiv logopt in constructor
 hopark      10/25/07 - set synopsis
 parujain    12/20/06 - operator sharing
 rkomurav    09/13/06 - physynpos OO restructuring
 rkomurav    08/29/06 - add genXMLPlan2
 najain      06/16/06 - cleanup
 najain      05/25/06 - bug fix 
 najain      05/05/06 - sharing support 
 anasrini    04/04/06 - RSTREAM support 
 najain      04/06/06 - cleanup
 anasrini    04/06/06 - constructor cleanup 
 najain      03/24/06 - cleanup
 skaluska    02/15/06 - Move Synopsis from execution to phyplan 
 najain      02/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptRStrm.java /main/7 2013/04/25 21:06:16 vikshukl Exp $
 *  @author  najain  
 *  @since   1.0
 */
package oracle.cep.phyplan;

import oracle.cep.exceptions.CEPException;
import oracle.cep.service.ExecContext;
import oracle.cep.logplan.LogOpt;
/**
 * RSTREAM physical operator
 *
 * @since 1.0
 */
public class PhyOptRStrm extends PhyOpt {
  /** Synopsis for the input relation */

  /**
   * Constructor
   * @param ec TODO
   * @param input the physical operator that is the input to this operator
   * @param logop the equivalent logical operator 
   */
  public PhyOptRStrm(ExecContext ec, PhyOpt input, LogOpt logop) throws PhysicalPlanException {
    super(ec, PhyOptKind.PO_RSTREAM, input, logop, true, false);

    // output is always a stream
    setIsStream(true);
  }

  // Getter method
  
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
   * The synopsis of the store needs to be shared with its inputs.
   */
  public void synStoreReq()
  {
    PhyStore store = getInputs()[0].getSharedRelStore();
    assert store != null;
    PhySynopsis inSyn = getSynopsis();
    inSyn.makeStub(store);
  }
  
  @Override
  public boolean isDependentOnChildSynAndStore(PhyOpt inpt)
  {
    return true;
  }
  
  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<RStream>");
    sb.append(super.toString());

    PhySynopsis inSyn = getSynopsis();
    if (inSyn != null) {
      sb.append("<PhysicalSynopsis>");
      sb.append(inSyn.toString());
      sb.append("</PhysicalSynopsis>");
    }

    sb.append("</RStream>");
    return sb.toString();
  }
  
  //Create and return visualiser compatible XML Plan
  public String getXMLPlan2() throws CEPException {
    StringBuilder xml = new StringBuilder();
    xml.append("<name> Rstream </name>\n");
    xml.append("<lname> Rstream </lname>\n");
    xml.append(super.getXMLPlan2());
    return xml.toString();
  }
  
  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    assert(getSynopsis() == syn);
      return PhySynPos.CENTER.getName();
  }
   
  /**
   * This method tells whether the two operators are partially equivalent or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(!(opt instanceof PhyOptRStrm))
      return false;
  
   // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
 
    PhyOptRStrm strmOpt = (PhyOptRStrm)opt;
  
    assert strmOpt.getOperatorKind() == PhyOptKind.PO_RSTREAM;
  
    if(strmOpt.getNumInputs() != this.getNumInputs())
      return false;
  
    if(strmOpt.getNumAttrs() != this.getNumAttrs())
      return false;
    
    return true;
    
  }


}
