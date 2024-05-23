/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptViewRelnSrc.java /main/11 2013/04/25 21:06:16 vikshukl Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
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
    sborah      04/20/09 - reorganize sharing hash
    sborah      03/18/09 - define sharingHash
    hopark      10/09/08 - remove statics
    hopark      10/25/07 - set synopsis
    parujain    06/21/07 - fix delete
    parujain    01/17/07 - fix ViewRelnSrc Operator sharing
    parujain    12/18/06 - operator sharing
    rkomurav    09/13/06 - PhySynPos OO restructuring
    rkomurav    08/25/06 - add linkSynStore
    anasrini    08/03/06 - remove shareRelStore
    najain      05/22/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptViewRelnSrc.java /main/11 2013/04/25 21:06:16 vikshukl Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan;

import oracle.cep.exceptions.CEPException;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptRelnSrc;
import oracle.cep.service.ExecContext;
import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.QueryDeletionContext;

/**
 * View Source Physical Operator outputting a relation 
 */
public class PhyOptViewRelnSrc extends PhyOpt {
  /** Id assigned by tableMgr */
  int relId;

  public int getRelId() {
    return relId;
  }

  public void setRelId(int relId) {
    this.relId = relId;
  }

  /** Synopsis of the relation (used to generate MINUS tuples) */

  public PhySynopsis getOutSyn() {
    return getSynopsis(0);
  }

  public void setOutSyn(PhySynopsis outSyn) {
    setSynopsis(0, outSyn);
  }
  
  //link syn and store
  public void linkSynStore() {
    PhySynopsis outSyn = getOutSyn();
    outSyn.makeStub(this.getStore());
  }
  
  @Override
  public boolean isDependentOnChildSynAndStore(PhyOpt input)
  {
    return true;
  }

  public PhyOptViewRelnSrc(ExecContext ec, LogOpt logPlan, PhyOpt input)
      throws PhysicalPlanException, MetadataException {
    super(ec, PhyOptKind.PO_VIEW_RELN_SRC, input, true, false);
    
    assert logPlan != null;
    assert logPlan.getNumInputs() == 0;
    assert logPlan instanceof LogOptRelnSrc;
    LogOptRelnSrc opRelnSrc = (LogOptRelnSrc) logPlan;

    int tableId = opRelnSrc.getRelationId();

    setRelId(tableId);

    // output is a relation
    setIsStream(false);
   
  }

  public boolean delete(QueryDeletionContext ctx) throws CEPException
  {
     //  isdeleted will be false when queryid has already been deleted
    // ex: select * from v1,v1; where v1 will be attempted to be dropped twice
    boolean isdeleted = super.delete(ctx);

    // remove it from the list, if no other query references this operator
    if (qryIds.size() == 0 && isdeleted)
      execContext.getPlanMgr().dropSourceOp(relId, this);
    
    return isdeleted;
  }

  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorViewRelationSource>");
    sb.append(super.toString());

    sb.append("<RelationId relId=\"" + relId + "\" />");

    sb.append("<PhysicalSynopsis>");
    PhySynopsis outSyn = getOutSyn();
    sb.append(outSyn.toString());
    sb.append("</PhysicalSynopsis>");

    sb.append("</PhysicalOperatorViewRelationSource>");
    return sb.toString();
  }
  
  //Generate and return visualiser compatible XML plan
  public String getXMLPlan2() throws CEPException {
    StringBuilder xml = new StringBuilder();
    xml.append("<name> ViewRelSrc </name>\n");
    xml.append("<lname> View Relation Source </lname>\n");
    xml.append(super.getXMLPlan2());
    xml.append("<property name = \"Relation\" value = \"");
    xml.append(execContext.getViewMgr().getView(relId).getName());
    xml.append("\"/>\n");
    return xml.toString();
  }
  
  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    assert(getOutSyn() == syn);
    return PhySynPos.RIGHT.getName();
  }
  
  /**
   * This method tells whether the two operators are partially equivalent or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(!(opt instanceof PhyOptViewRelnSrc))
      return false;
  
   // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
 
    PhyOptViewRelnSrc viewOpt = (PhyOptViewRelnSrc)opt;
  
    assert viewOpt.getOperatorKind() == PhyOptKind.PO_VIEW_RELN_SRC;
  
    if(viewOpt.getNumInputs() != this.getNumInputs())
      return false;
  
    if(this.relId != viewOpt.relId)
      return false;
    
    return true;
  }

}

