/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptViewStrmSrc.java /main/12 2009/11/09 10:10:59 sborah Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      10/12/09 - support for bigdecimal
    sborah      04/20/09 - reorganize sharing hash
    sborah      03/18/09 - define sharingHash
    sbishnoi    12/16/08 - fix bug 7647530 (get input attr from logopt)
    hopark      10/09/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    parujain    05/09/08 - fix viewrelnsrc drop
    anasrini    09/04/07 - ELEMENT_TIME support
    parujain    06/21/07 - fix delete
    parujain    01/17/07 - fix ViewStrmSrc Operator sharing
    parujain    12/18/06 - operator sharing
    rkomurav    09/13/06 - PhySynPos OO restructuring
    rkomurav    08/29/06 - add genXMLPlan2
    anasrini    08/03/06 - remove shareRelStore
    najain      05/22/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptViewStrmSrc.java /main/12 2009/11/09 10:10:59 sborah Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.StreamPseudoColumn;
import oracle.cep.exceptions.CEPException;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptStrmSrc;
import oracle.cep.service.ExecContext;

/**
 * View Source Physical Operator outputting a stream 
 */
public class PhyOptViewStrmSrc extends PhyOpt {
  /** Id assigned by tableMgr */
  int strId;
  
  public int getStrId() {
    return strId;
  }

  public void setStrId(int strId) {
    this.strId = strId;
  }

  public PhyOptViewStrmSrc(ExecContext ec, LogOpt logPlan, PhyOpt input)
      throws PhysicalPlanException {

    super(ec, PhyOptKind.PO_VIEW_STRM_SRC, input, false, true);

    int numOutAttrs;

    assert logPlan != null;
    assert logPlan.getNumInputs() == 0;
    assert logPlan instanceof LogOptStrmSrc;
    LogOptStrmSrc opStrmSrc = (LogOptStrmSrc) logPlan;

    int tableId = opStrmSrc.getStreamId();
    setStrId(tableId);

    // output is a stream
    setIsStream(true);

    // Output schema of the operator = schema of the stream + pseudo columns   
    // so numOutAttrs include the ELEMENT_TIME column also 
    //
    numOutAttrs = opStrmSrc.getNumOutAttrs();
    setNumAttrs(numOutAttrs);
    
    // Get Attribute type and length information from input operator
    // Note: Here we are setting data types and length from input PhyOpt
    //       because the input opt is super set of THIS opt in terms of
    //       attributes. Also THIS opt is a prefix of input opt.
       
    // Set Attribute type and length from input operator
    // except ELEMENT_TIME column 
    for(int i = 0 ; i < numOutAttrs-1 ; i++)
    {
      setAttrMetadata(i, input.getAttrMetadata(i));
      
    }
    
    // Set Attribute type and length for ELEMENT_TIME pseudo column
    StreamPseudoColumn elemTime = StreamPseudoColumn.ELEMENT_TIME;
    setAttrMetadata(numOutAttrs-1, new AttributeMetadata(elemTime.getColumnType(),
                                         elemTime.getColumnLen(),
                                         elemTime.getColumnType().getPrecision(),
                                         0));    
   
  }
 
//  public boolean delete(QueryDeletionContext ctx) throws CEPException
//  {
//    //  isdeleted will be false when queryid has already been deleted
//    // ex: select * from v1,v1; where v1 will be attempted to be dropped twice
//    boolean isdeleted = super.delete(ctx);
//
//    // remove it from the list, if no other query references this operator
//    if (qryIds.size() == 0 && isdeleted)
//      PlanManager.getPlanManager().dropSourceOp(strId, this);
//    
//    return isdeleted;
//  }

  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorViewStreamSource>");
    sb.append(super.toString());

    sb.append("<StreamId strId=\"" + strId + "\" />");

    sb.append("</PhysicalOperatorViewStreamSource>");
    return sb.toString();
  }
  
  //Generate and return visualiser compatible XML plan
  public String getXMLPlan2() throws CEPException {
    StringBuilder xml = new StringBuilder();
    xml.append("<name> ViewStrSrc </name>\n");
    xml.append("<lname> View Stream Source </lname>\n");
    xml.append(super.getXMLPlan2());
    xml.append("<property name = \"Stream\" value = \"");
    xml.append(execContext.getViewMgr().getView(strId).getName());
    xml.append("\"/>\n");
    return xml.toString();
  }
  
  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    // View Stream Source has no Relation Synopsis
    assert(false);
    return null;
  }
  
  /**
   * This method tells whether the two operators are partially equivalent or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(!(opt instanceof PhyOptViewStrmSrc))
      return false;
  
   // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
 
    PhyOptViewStrmSrc viewOpt = (PhyOptViewStrmSrc)opt;
  
    assert viewOpt.getOperatorKind() == PhyOptKind.PO_VIEW_STRM_SRC;
  
    if(viewOpt.getNumInputs() != this.getNumInputs())
      return false;
  
    if(this.strId != viewOpt.strId)
      return false;
    
    return true;
  }
}

