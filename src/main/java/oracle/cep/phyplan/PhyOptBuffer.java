/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptBuffer.java /main/4 2013/10/21 19:47:35 vikshukl Exp $ */

/* Copyright (c) 2012, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    10/14/13 - disable dim flag if above view root
    udeshmuk    08/13/12 - add canConstructQuery() method
    vikshukl    09/10/12 - propagate archived dimension stuff
    udeshmuk    07/06/12 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptBuffer.java /main/4 2013/10/21 19:47:35 vikshukl Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan;

import oracle.cep.exceptions.CEPException;
import oracle.cep.metadata.Query;

public class PhyOptBuffer extends PhyOpt
{
  private boolean isProjectInput = false;
  
  public PhyOptBuffer(PhyOpt input, PhyOpt output, int qryId,
                      boolean needToSetArchiverQueryRelatedFields)
    throws PhysicalPlanException
  {
    super(input.execContext, PhyOptKind.PO_BUFFER, input, null, true, true);
    //set archived relation setup related fields - copy over from input
    if(needToSetArchiverQueryRelatedFields)
    {
      this.setArchiverName(input.getArchiverName());
      this.setArchiverProjEntries(input.getArchiverProjEntries());
      this.setArchiverProjTypes(input.getArchiverProjTypes());
      // see similar code in PhyOpt.java. set it to false above view root.
      this.setArchivedDim(!input.getIsView() && input.isArchivedDim()); 
      this.setEventIdColAddedToProjClause(input.isEventIdColAddedToProjClause());
      this.setEventIdColName(input.getEventIdColName());
      this.setEventIdColNum(input.getEventIdColNum()); 
      this.setTxnIdColName(input.getTxnIdColName());
      this.setTxnIdColNum(input.getTxnIdColNum());
      this.setWorkerIdColName(input.getWorkerIdColName());
      this.setWorkerIdColNum(input.getWorkerIdColNum());
      this.setIsBelowViewRootInclusive(input.isBelowViewRootInclusive());
      
      //first set the outputSQL and then call the setIsQueryOperator method.
      this.setOutputSQL(input.getOutputSQL());
      this.setIsQueryOperator(input.isQueryOperator());
      this.setStateInitializationDone(true);
    }
    
    this.setCanBeConnectorOperator(true);
    this.setCanBeShared(false);
    this.setLHSConnector(false);
    this.setRHSConnector(false);
    
    //set the qryID and source lineage
    this.addQryId(qryId);
    this.setSourceLineages();
    //TODO: verify if hbtTimeoutRequired should always be false?
    this.setHbtTimeoutRequired(false); 
    this.setOrderingConstraint(input.getOrderingConstraint());
    
    //output could be null if this buffer operator is the new root
    if(output != null)
    {
      //add output to the list
      this.addOutput(output);
      //find the position of the input in output(parent's) input list
      int inputPos = input.getInputIndex(output);
      output.setInput(inputPos, this);
    }
    
    this.isProjectInput = (input instanceof PhyOptProject) ? true : false;

  }
  
  public boolean getIsProjectInput()
  {
    return this.isProjectInput;
  }
  
  @Override
  public String getRelnSynPos(PhySynopsis syn)
  {
    if(this.isProjectInput)
    { //lineage synopsis
      assert false;
      return null;
    }
    else //reln synopsis
    {
      assert(getOutSyn() == syn);
      return PhySynPos.OUTPUT.getName();
    }
  }
  
  public PhySynopsis getOutSyn()
  {
    return getSynopsis(0);
  }
  
  public boolean isProjectInput()
  {
    return this.isProjectInput;
  }
  
  public void setOutSyn(PhySynopsis outSyn) {
    setSynopsis(0, outSyn);
  }
  
  //archived relation related methods
  public boolean canBeQueryOperator()
  {
    return true;
  }
  
  public boolean canConstructQuery(Query q)
  {
    assert this.getInputs()[0].getOutputSQL() != null : "Buffer op "+
      this.getOptName()+ " child sql is null!";
    return true;
  }
  
  public boolean isStateFul()
  {
    return true;
  }
  
  public void updateArchiverQuery()
  {
    PhyOpt[] children = this.getInputs();
    PhyOpt input = children[0];
    
    this.setArchiverName(input.getArchiverName());
    this.setArchiverProjEntries(input.getArchiverProjEntries());
    this.setArchiverProjTypes(input.getArchiverProjTypes());
    this.setEventIdColAddedToProjClause(input.isEventIdColAddedToProjClause());
    this.setEventIdColName(input.getEventIdColName());
    this.setEventIdColNum(input.getEventIdColNum());
    this.setTxnIdColName(input.getTxnIdColName());
    this.setTxnIdColNum(input.getTxnIdColNum());
    this.setWorkerIdColName(input.getWorkerIdColName());
    this.setWorkerIdColNum(input.getWorkerIdColNum());
    //just copy over the children's SQL
    this.setOutputSQL(children[0].getOutputSQL());
  }
  
  //visualizer plan
  public String getXMLPlan2() throws CEPException{
    StringBuilder xml = new StringBuilder();
    xml.append("<name> Buffer </name>\n");
    xml.append("<lname> Buffer operator </lname>\n");
    xml.append(super.getXMLPlan2());
    return xml.toString();
  }
}
