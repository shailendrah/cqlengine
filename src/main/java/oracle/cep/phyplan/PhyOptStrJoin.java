/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptStrJoin.java /main/15 2013/04/25 21:06:16 vikshukl Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Stream Join Physical Operator in the package oracle.cep.phyplan

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 vikshukl    04/17/13 - pass input operator to isDependentOnChildSynAndStore()
 udeshmuk    10/20/11 - API for knowing if this operator uses child's synopsis
 sbishnoi    03/11/10 - fix sharing issue in table function join
 sbishnoi    03/03/10 - adding implementation of abstract methods
 sbishnoi    12/28/09 - table function followup
 sborah      04/19/09 - reorganize sharing hash
 sborah      03/19/09 - siggen optimization: removing viewstrmsrc
 sborah      03/17/09 - define sharingHash
 hopark      10/09/08 - remove statics
 sborah      09/23/08 - derieve output schema from equivalent logical op
 parujain    12/19/07 - inner and outer
 parujain    11/15/07 - external relation
 hopark      10/25/07 - set synopsis
 parujain    12/20/06 - operator sharing
 rkomurav    09/13/06 - physynopsis OO restructuring
 rkomurav    08/28/06 - add genXMLPlan2
 najain      06/21/06 - cleanup
 najain      06/16/06 - cleanup
 najain      05/26/06 - implementation
 najain      05/25/06 - bug fix 
 najain      04/20/06 - bug fix 
 najain      04/06/06 - cleanup
 anasrini    04/06/06 - constructor cleanup 
 najain      03/24/06 - cleanup
 skaluska    02/15/06 - Cleanup Phy/Exec Synopsis 
 najain      02/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptStrJoin.java /main/15 2013/04/25 21:06:16 vikshukl Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan;

import java.util.Iterator;
import java.util.LinkedList;

import oracle.cep.common.Constants;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptStrmCross;
import oracle.cep.metadata.MetadataException;
import oracle.cep.phyplan.expr.BoolExpr;
import oracle.cep.service.ExecContext;
import oracle.cep.exceptions.CEPException;

/**
 * Stream Join Physical Operator
 */
public class PhyOptStrJoin extends PhyOptJoinBase {
  /**
   * My output schema is concatenation of first numOuterAttrs from left input
   * and numInnerAttrs from right input. (Assert(numOuterAttrs + numInnerAttrs ==
   * numAttrs)
   */
  int         numOuterAttrs;

  int         numInnerAttrs;
  
  /** Join predicate */
  private LinkedList<BoolExpr>    preds;
  
  /** flag to check whether this operator is joining with a table function */
  private boolean isTableFunctionExternalJoin;
  
  /** table function details */
  private TableFunctionInfo tableFunctionInfo;
  

  /** Synopsis for the inner relation */

  /**
   * @return Returns the innerSyn.
   */
  @Override
  public PhySynopsis getInnerSyn() {
    return getSynopsis(0);
  }

  /**
   * @param innerSyn The innerSyn to set.
   */
  public void setInnerSyn(PhySynopsis innerSyn) {
    setSynopsis(0, innerSyn);
  }

  /**
   * @return Returns the predicates
   */
  public BoolExpr[] getPredicate() {
      return preds.toArray( new BoolExpr[0]);
  }

  /**
   * Add an atomic predicate
   * <p>
   * An atomic predicate is a boolean expression that does not involve
   * and logical operators such as AND. 
   * @param pred the atomic predicate to be added
   */
  public void addAtomicPred(BoolExpr pred)
  {
    preds.add(pred);
  }

  /**
   * Add a list of predicates
   * <p>
   * @param apreds a list of predicates to be added
   */
  public void appendPreds(LinkedList<BoolExpr> apreds) 
  {
    preds.addAll(apreds);   
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Physical operator based on its attributes.
   * @return 
   *      A concise String representation of the Physical operator.
   */
  protected String getSignature()
  {
    String tableFunctionInfoSuffix = 
      (tableFunctionInfo != null) ? ("#" + tableFunctionInfo.toString()) :
                                         ("");
      
    return (this.getOperatorKind() + "#"
         + getExpressionList(preds) + tableFunctionInfoSuffix);
  }

  /**
   * Get preds
   * @return the list of predicates
   */
  public LinkedList<BoolExpr> getPreds() {
      return preds;
  }

  /**
   * @return Returns the number of OuterAttrs.
   */
  public int getNumOuterAttrs() {
    return numOuterAttrs;
  }

  /**
   * @return Returns the number of InnerAttrs.
   */
  public int getNumInnerAttrs() {
    return numInnerAttrs;
  }

  /**
   * @param numOuterAttrs The number of OuterAttrs
   */
  public void setNumOuterAttrs(int numOuterAttrs) {
    this.numOuterAttrs = numOuterAttrs;
  }

  public PhyOptStrJoin(ExecContext ec) throws PhysicalPlanException {
    super(ec, PhyOptKind.PO_STR_JOIN);
  }

  public PhyOptStrJoin(ExecContext ec, LogOpt logPlan, PhyOpt[] phyChildPlans)
      throws PhysicalPlanException, MetadataException {

    super(ec, PhyOptKind.PO_STR_JOIN);
    assert logPlan != null;
    assert logPlan.getNumInputs() == 2;
    assert phyChildPlans.length == 2;
    assert logPlan instanceof LogOptStrmCross;

    setInnerSyn(null);
    
    LogOpt logOuter = logPlan.getInput(Constants.OUTER);
    LogOpt logInner = logPlan.getInput(Constants.INNER);
    
    numOuterAttrs = logOuter.getNumOutAttrs();
    numInnerAttrs = logInner.getNumOutAttrs();

    createBinarySchema(phyChildPlans[Constants.OUTER], numOuterAttrs, 
                       phyChildPlans[Constants.INNER], numInnerAttrs,
                       true);

    // ensure space for the element time column also
    assert numOuterAttrs + numInnerAttrs + 1 == getNumAttrs();

    // output is a stream iff both inputs are streams
    setIsStream(true);
    
    // Set External
    if(logPlan.isExternal())
      this.setExternal(true);
   
    // inputs;
    setNumInputs(2);
    getInputs()[Constants.OUTER] = phyChildPlans[Constants.OUTER];
    getInputs()[Constants.INNER] = phyChildPlans[Constants.INNER];

    phyChildPlans[Constants.OUTER].addOutput(this);
    phyChildPlans[Constants.INNER].addOutput(this);

    // Allocate the list to hold the atomic predicates
    preds = new LinkedList<BoolExpr>();
  }
  
  public boolean getSharedSynType(int idx)
  {
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
  
  //toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorStreamJoin>");
    sb.append(super.toString());

    sb.append("<NumberOfInnerAttributes numInner=\"" + numInnerAttrs + "\" />");
    sb.append("<NumberOfOuterAttributes numOuter=\"" + numOuterAttrs + "\" />");
                                                                                                                             
    if (preds.size() != 0) {
      sb.append("<Predicate>");
      for (int i = 0; i < preds.size(); i++)
        sb.append(preds.get(i).toString());
      sb.append("</Predicate>");
    }
                                                                                                                             
    sb.append("<InnerSynopsis>");
    PhySynopsis innerSyn = getInnerSyn();
    sb.append(innerSyn.toString());
    sb.append("</InnerSynopsis>");
                       
    sb.append("<IsTableFunctionExternalJoin value=\"" + 
        isTableFunctionExternalJoin() + "\" />");
    
    sb.append("</PhysicalOperatorStreamJoin>");
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
    if(!(opt instanceof PhyOptJoin))
      return false;
  
   // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
 
    PhyOptStrJoin joinOpt = (PhyOptStrJoin)opt;
  
    assert joinOpt.getOperatorKind() == PhyOptKind.PO_STR_JOIN;
  
    if(joinOpt.isTableFunctionExternalJoin() ^ this.isTableFunctionExternalJoin())
      return false;
    
    if(joinOpt.getNumInputs() != this.getNumInputs())
      return false;
  
    if(joinOpt.getNumAttrs() != this.getNumAttrs())
      return false;

    if(joinOpt.numOuterAttrs != this.numOuterAttrs)
      return false;

    if(joinOpt.numInnerAttrs != this.numInnerAttrs)
      return false;
    
    if(joinOpt.preds.size() != preds.size())
      return false;
    
    return compareJoinBoolExpr(joinOpt);
    
  }

  private boolean compareJoinBoolExpr(PhyOptStrJoin joinOpt)
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
    //Note: This should not be called
    assert false;
    return null;
  }

 }
