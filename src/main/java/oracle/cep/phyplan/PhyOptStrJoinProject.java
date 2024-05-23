/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptStrJoinProject.java /main/15 2013/04/25 21:06:16 vikshukl Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Stream Join Project Physical Operator in the package oracle.cep.phyplan

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 vikshukl    04/17/13 - pass input operator to isDependentOnChildSynAndStore()
 udeshmuk    10/20/11 - API for knowing if this operator uses child's synopsis
 sbishnoi    07/13/10 - XbranchMerge sbishnoi_bug-9860418_ps3_11.1.1.4.0 from
                        st_pcbpel_11.1.1.4.0
 sbishnoi    07/12/10 - XbranchMerge sbishnoi_bug-9860418_ps3 from main
 sbishnoi    03/11/10 - fix sharing issue in table function join
 sbishnoi    03/03/10 - adding implementation of abstract methods
 sbishnoi    12/28/09 - table function followup
 sborah      10/12/09 - support for bigdecimal
 sborah      04/19/09 - reorganize sharing hash
 sborah      03/17/09 - define sharingHash
 hopark      10/09/08 - remove statics
 parujain    12/19/07 - inner and outer
 parujain    11/29/07 - fix bug
 hopark      10/25/07 - set synopsis
 parujain    12/20/06 - operator sharing
 rkomurav    09/13/06 - physynpos OO restructuring
 rkomurav    08/28/06 - add genXMLPlan2
 rkomurav    08/20/06 - adding toString
 najain      06/16/06 - cleanup
 najain      05/30/06 - add synStoreReq 
 najain      05/30/06 - add getters/setters etc. 
 najain      05/25/06 - bug fix 
 najain      04/20/06 - bug fix 
 najain      04/06/06 - cleanup
 anasrini    04/06/06 - constructor cleanup 
 najain      03/24/06 - cleanup
 skaluska    02/15/06 - Cleanup Phy/Exec Synopsis 
 najain      02/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptStrJoinProject.java /main/15 2013/04/25 21:06:16 vikshukl Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan;

import java.util.LinkedList;
import java.util.Iterator;

import oracle.cep.common.Constants;
import oracle.cep.exceptions.CEPException;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.BoolExpr;
import oracle.cep.service.ExecContext;

/**
 * Join Project Physical Operator 
 */
public class PhyOptStrJoinProject extends PhyOptJoinBase {
  /** Output projections */
  Expr[]      projs;
  
  /** Used to compute the sharing hash values */
  private String projList = "";
  private String predList = "";

  /** Join predicate */
  private LinkedList<BoolExpr>    preds;

  /** flag to check whether this operator is joining with a table function */
  private boolean isTableFunctionExternalJoin;
  
  /** table function details */
  private TableFunctionInfo tableFunctionInfo;
  
  /** Synopsis for inner 
   * @param ec TODO*/

  public PhyOptStrJoinProject(ExecContext ec) throws PhysicalPlanException {
    super(ec, PhyOptKind.PO_STR_JOIN_PROJECT);
  }
  
  /**
   * @return Returns the innerSyn.
   */
  public PhySynopsis getInnerSyn()
  {
    return getSynopsis(0);
  }

  /**
   * @param innerSyn The innerSyn to set.
   */
  public void setInnerSyn(PhySynopsis innerSyn)
  {
    setSynopsis(0, innerSyn);
  }

  /**
   * @return Returns the preds.
   */
  public LinkedList<BoolExpr> getPreds()
  {
    return preds;
  }

  /**
   * @param preds The preds to set.
   */
  public void setPreds(LinkedList<BoolExpr> preds)
  {
    this.preds = preds;
  }

  /**
   * @return Returns the projs.
   */
  public Expr[] getProjs()
  {
    return projs;
  }

  /**
   * @param projs The projs to set.
   */
  public void setProjs(Expr[] projs)
  {
    this.projs = projs;   
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Physical operator based on its attributes.
   * @return 
   *      A concise String representation of the Physical operator.
   */
  protected String getSignature()
  {
    
    // compute the projList
    this.projList = getExpressionList(projs);
    // compute the predList 
    this.predList = getExpressionList(preds);
    
    String tableFunctionInfoSuffix = 
      (tableFunctionInfo != null) ? ("#" + tableFunctionInfo.toString()) :
                                    ("");
      
    // compute the sharing Hash value
    return  (this.getOperatorKind() + "#"
           + this.projList + "#"
           + this.predList +
           tableFunctionInfoSuffix);
  }

  public boolean getSharedSynType(int idx) {
    assert getNumInputs() > idx;
    assert getNumInputs() == 2;
    if (idx == 1)
      return true;
    return false;
  }

  /**
   * The synopsis of the store needs to be shared with its inputs.
   */
  public void synStoreReq()
  {
    PhyStore innerStore = getInputs()[Constants.INNER].getSharedRelStore();
    assert innerStore != null;
    PhySynopsis innerSyn = getInnerSyn();
    innerSyn.makeStub(innerStore);
  }

  @Override
  public boolean isDependentOnChildSynAndStore(PhyOpt input)
  {
    return true;
  }
  
  // toString override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorStreamJoinproject>");
    sb.append(super.toString());
    if (preds.size() != 0) {
      sb.append("<Predicate>");
      for (int i = 0; i < preds.size(); i++)
        sb.append(preds.get(i).toString());
      sb.append("</Predicate>");
    }
    
    if ( projs.length != 0) {
      sb.append("<Projection>");
      for (int i = 0; i < projs.length; i++)
        sb.append(projs[i].toString());
      sb.append("</Projection>");
    }
                                                                                                                             
    sb.append("<InnerSynopsis>");
    PhySynopsis innerSyn = getInnerSyn();
    sb.append(innerSyn.toString());
    sb.append("</InnerSynopsis>");
    
    sb.append("<IsTableFunctionExternalJoin value=\"" + 
        isTableFunctionExternalJoin() + "\" />");
                                                                                                                             
    sb.append("</PhysicalOperatorStreamJoinproject>");
    return sb.toString();
  }
  
//Create and return visualiser compatible XML Plan
  public String getXMLPlan2() throws CEPException {
    StringBuilder xml = new StringBuilder();
    int i = 0;
    xml.append("<name> StrJoin </name>\n");
    xml.append("<lname> Stream Relation Join </lname>\n");
    xml.append(super.getXMLPlan2());
    xml.append("<property name = \"Join Predicate\" value = \"");
    if (preds.size() != 0) {
      for (i = 0; i < (preds.size() - 1); i++) {
        xml.append(preds.get(i).getXMLPlan2());
        xml.append(", ");
      }
      xml.append(preds.get(i).getXMLPlan2());
    }
    else {
      xml.append("(null)");
    }
    xml.append("\"/>\n");
    
    xml.append("<property name = \"Project List\" value = \"");
    if(projs.length != 0) {
      for(i = 0; i < (projs.length - 1); i++) {
        xml.append(projs[i].getXMLPlan2());
        xml.append(",");
      }
      xml.append(projs[i].getXMLPlan2());
    }
    else {
      xml.append("(null)");
    }
    xml.append("\"/>\n");
    xml.append(
        "<property name = \"Is Table Function External Join\" value = \"" +
        isTableFunctionExternalJoin() + "\"/>\n");
    return xml.toString();
  }

  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    assert(getInnerSyn() == syn);
    return PhySynPos.RIGHT.getName();
  }
 
  /**
   * This method tells whether the two operators are partially equivalent or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(!(opt instanceof PhyOptStrJoinProject))
      return false;
  
   // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
 
    PhyOptStrJoinProject joinOpt = (PhyOptStrJoinProject)opt;
  
    assert joinOpt.getOperatorKind() == PhyOptKind.PO_STR_JOIN_PROJECT;
  
    if(joinOpt.getNumInputs() != this.getNumInputs())
      return false;
  
    if(joinOpt.getNumAttrs() != this.getNumAttrs())
      return false;

    if(joinOpt.preds.size() != preds.size())
      return false;

    if(joinOpt.projs.length != projs.length)
      return false;
    
    if(joinOpt.isTableFunctionExternalJoin() ^ this.isTableFunctionExternalJoin())
      return false;

    if(!(compareProjectExpr(joinOpt)))
      return false;
    
    return compareJoinBoolExpr(joinOpt);
    
  }

  private boolean compareJoinBoolExpr(PhyOptStrJoinProject joinOpt)
  {
    Iterator<BoolExpr> iter1 = preds.iterator();
    Iterator<BoolExpr> iter2 = joinOpt.preds.iterator();
    while(iter1.hasNext())
    {
      assert iter2.hasNext();
      BoolExpr expr1 = (BoolExpr)iter1.next();
      BoolExpr expr2 = (BoolExpr)iter2.next();
      if(!(expr1.equals(expr2)))
        return false;
    }
    return true;
  }

  private boolean compareProjectExpr(PhyOptStrJoinProject opt)
  {
    for(int i=0; i < projs.length; i++)
    {
  
      if(projs[i].getKind() != opt.projs[i].getKind())
        return false;
 
      if(!projs[i].getType().equals(opt.projs[i].getType()))
        return false;
  
      if(getAttrLen(i) != opt.getAttrLen(i))
        return false;
  
      if(! projs[i].equals(opt.projs[i]))
        return false;
    }
    return true;
  }
  
  /**
   * @return the tableFunctionInfo
   */
  public TableFunctionInfo getTableFunctionInfo()
  {
    return tableFunctionInfo;
  }

  /**
   * @param tableFunctionInfo the tableFunctionInfo to set
   */
  public void setTableFunctionInfo(TableFunctionInfo tableFunctionInfo)
  {
    this.tableFunctionInfo = tableFunctionInfo;
    this.setTableFunctionExternalJoin(tableFunctionInfo != null);
  }

  /**
   * @return the isTableFunctionExternalJoin
   */
  public boolean isTableFunctionExternalJoin()
  {
    return isTableFunctionExternalJoin;
  }

  /**
   * @param isTableFunctionExternalJoin the isTableFunctionExternalJoin to set
   */
  public void setTableFunctionExternalJoin(boolean isTableFunctionExternalJoin)
  {
    this.isTableFunctionExternalJoin = isTableFunctionExternalJoin;
  }

  @Override
  public PhySynopsis getOuterSyn()
  {
    //Note: This function should not be called
    assert false;
    return null;
  }
   
}
