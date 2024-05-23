/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptIStrm.java /main/9 2013/05/07 18:03:18 sbishnoi Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Insert Stream Physical Operator in the package oracle.cep.phyplan

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    05/07/13 - setting hbtTimeOutRequired to true
 vikshukl    04/17/13 - pass input operator to isDependentOnChildSynAndStore()
 udeshmuk    10/20/11 - API for knowing if this operator uses child's synopsis
 vikshukl    09/04/09 - add support for ISTREAM (R) DIFFERENCE USING (...)
 hopark      10/09/08 - remove statics
 anasrini    09/16/08 - pass in equiv logopt in constructor
 hopark      10/25/07 - set synopsis
 parujain    12/20/06 - operator sharing
 rkomurav    09/13/06 - physynpos OO restructuring
 rkomurav    08/29/06 - add genXMLPlan2
 najain      06/17/06 - bug fix 
 najain      06/15/06 - cleanup
 najain      05/25/06 - bug fix 
 ayalaman    04/24/06 - physical operator for IStream 
 ayalaman    04/21/06 - setter/getter for synopsis 
 najain      04/06/06 - cleanup
 anasrini    04/06/06 - constructor cleanup 
 najain      03/24/06 - cleanup
 skaluska    02/15/06 - Move Synopsis from execution to phyplan 
 najain      02/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptIStrm.java /main/9 2013/05/07 18:03:18 sbishnoi Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan;

import oracle.cep.exceptions.CEPException;
import oracle.cep.logplan.LogOpt;
import oracle.cep.service.ExecContext;

/**
 * Insert Stream Physical Operator 
 */
public class PhyOptIStrm extends PhyOpt {

  private Integer[] usingExprListMap;

  /** Synopsis for input relation as of R(t-1).
   * 
   * This is different from NOW synopsis for I/D STREAM which is used
   * only for capturing multiple tuples that arrive with the same 
   * timestamp. This, on the other hand, captures relation as it
   * existed at time T-1, where T is the current timestamp.
   *
   * NOW synopsis is used in both versions of ISTREAM.
   *
   */

  /* instantaneous relation of tuples at T */
  private int NOW_SYN   = 0;  

  /* applicable only for ISTREAM with NOT IN semantics. */
  private int INPUT_SYN = 1;  

  /**
   * Constructor
   * @param input the physical operator that is the input to this operator
   * @param logop the equivalent logical operator
   */
  public PhyOptIStrm(ExecContext ec, PhyOpt input, LogOpt logop, 
                     Integer[] exprListMap)
      throws PhysicalPlanException 
  {
    super(ec, PhyOptKind.PO_ISTREAM,  input, logop, true, false);
    this.usingExprListMap = exprListMap;
    setIsStream(true);    // output is always a stream
    
    // ISTream needs timeout heartbeat in case if input goes silent
    setHbtTimeoutRequired(true);
  }

  /**
   * Get the in synopsis. ("now" synopsis)
   * @return the in synopsis
   */
  public PhySynopsis getSynopsis() {
    return getSynopsis(NOW_SYN);
  }

  /**
   * Get the input synopsis (R(t-1) synopsis)
   * @return the input synopsis
   */
  public PhySynopsis getInSynopsis() {
    return getSynopsis(INPUT_SYN);
  }


  // Setter API
  /**
   * Set the in synopsis
   * @param inSyn the in synopsis
   */
  public void setSynopsis(PhySynopsis inSyn) {
    setSynopsis(NOW_SYN, inSyn);
  }

  /**
   * Set the input synopsis
   * @param inSyn the in synopsis
   */
  public void setInSynopsis(PhySynopsis inSyn) {
    setSynopsis(INPUT_SYN, inSyn);
  }


  /**
   * Get USING clause expressions
   *
   * @return USING clause expressions
   */

  public Integer[] getUsingExprListMap()
  {
    return usingExprListMap;
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
    PhySynopsis nowSyn = getSynopsis();
    nowStore.setOwnOp(this);
    nowSyn.makeStub(nowStore);

    /* store for inSyn will be shared */
    if (getUsingExprListMap() != null) {
      PhySynopsis inSyn = getInSynopsis();
      assert (inSyn != null);
      PhyStore inStore = getInputs()[0].getSharedRelStore();
      assert inStore != null;
      inSyn.makeStub(inStore);
    }
  }

  @Override
  public boolean isDependentOnChildSynAndStore(PhyOpt input)
  {
    if(getUsingExprListMap() != null)
      return true;
    else
      return false;
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

    PhySynopsis inSyn = getInSynopsis();
    if (inSyn != null) {
      sb.append("<PhysicalSynopsis>");
      sb.append(inSyn.toString());
      sb.append("</PhysicalSynopsis>");
    }

    sb.append("</IStream>");
    return sb.toString();
  }
  
  //Create and return visualiser compatible XML Plan
  public String getXMLPlan2() throws CEPException {
    StringBuilder xml = new StringBuilder();
    xml.append("<name> Istream </name>\n");
    xml.append("<lname> Istream </lname>\n");
    xml.append(super.getXMLPlan2());
    return xml.toString();
  }

  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    PhySynopsis nowSyn = getSynopsis();
    PhySynopsis inSyn = getInSynopsis();

    assert(nowSyn == syn || inSyn == syn);

    if (nowSyn == syn)
      return PhySynPos.RIGHT.getName();
    else 
      return PhySynPos.CENTER.getName();
  }
 
  /**
   * This method tells whether the two operators are partially equivalent or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(!(opt instanceof PhyOptIStrm))
      return false;
  
   // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
 
    PhyOptIStrm strmOpt = (PhyOptIStrm)opt;
  
    assert strmOpt.getOperatorKind() == PhyOptKind.PO_ISTREAM;
  
    if(strmOpt.getNumInputs() != this.getNumInputs())
      return false;
  
    if(strmOpt.getNumAttrs() != this.getNumAttrs())
      return false;

    return compareUsingExprListMaps(strmOpt);

  }

  private boolean compareUsingExprListMaps(PhyOptIStrm opt)
  {
    if (this.usingExprListMap != null &&
        opt.usingExprListMap != null) 
    {
      if (this.usingExprListMap.length != opt.usingExprListMap.length)
        return false;
    
      for(int i = 0; i < usingExprListMap.length; i++)
      {
        if (usingExprListMap[i] != opt.usingExprListMap[i])
          return false;
      }
      return true;
    }
    else 
      return false;
  }

}
