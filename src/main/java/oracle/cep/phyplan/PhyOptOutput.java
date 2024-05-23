/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptOutput.java /main/17 2013/04/25 21:06:16 vikshukl Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Output Physical Operator in the package oracle.cep.phyplan

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 vikshukl    04/17/13 - pass input operator to isDependentOnChildSynAndStore()
 anasrini    02/27/12 - bug 13739177, fix mem leak in inQueues
 sbishnoi    02/16/12 - create synopsis when batching is enabled
 udeshmuk    10/20/11 - API for knowing if this operator uses child's synopsis
 udeshmuk    08/26/11 - remove archive rel methods
 udeshmuk    06/20/11 - reflect the changed method names of archived relation
 udeshmuk    03/28/11 - archive relation support
 udeshmuk    09/24/10 - XbranchMerge udeshmuk_prop_hb_across_processors from
                        st_pcbpel_11.1.1.4.0
 udeshmuk    09/01/10 - add propagateHeartbeat
 sbishnoi    08/25/09 - support for batching output
 parujain    02/09/09 - execution error
 hopark      10/09/08 - remove statics
 sbishnoi    11/29/07 - add queryId to list of queryIds
 sbishnoi    11/23/07 - support for update semantics
 parujain    06/21/07 - fix delete
 parujain    02/06/07 - Fix remove output
 rkomurav    09/11/06 - cleanup of xmldump
 rkomurav    08/23/06 - add getXMLPlan2
 najain      06/20/06 - add delete 
 najain      05/11/06 - Dyanmic Query Support 
 najain      04/06/06 - cleanup
 anasrini    04/06/06 - constructor cleanup 
 najain      03/31/06 - fix bugs 
 najain      03/24/06 - cleanup
 anasrini    03/15/06 - add getter methods 
 najain      02/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptOutput.java /main/17 2013/04/25 21:06:16 vikshukl Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan;

import oracle.cep.exceptions.CEPException;
import oracle.cep.metadata.QueryDeletionContext;
import oracle.cep.metadata.Destination;
import oracle.cep.service.ExecContext;

/**
 * Output Physical Operator 
 */
public class PhyOptOutput extends PhyOpt {
  int outputId;
  int queryId;
  
  /** flag to check if primary key exists for this output*/
  boolean isPrimaryKeyExist;
  
  /** Output Destination */
  Destination eprDest;
  
  private final int OUT_SYN   = 0;
  private final int PLUS_SYN  = 1;
  private final int MINUS_SYN = 2;

  
  public PhyOptOutput(ExecContext ec, PhyOpt input, int queryId, 
                      Destination eprDest, boolean isPrimaryKeyExist) 
  throws PhysicalPlanException 
  {
    super(ec, PhyOptKind.PO_OUTPUT, input, true, true);
    this.queryId = queryId;
    this.eprDest = eprDest;
    this.isPrimaryKeyExist = isPrimaryKeyExist;
    super.addQryId(queryId);
    
    // Propagate ordering constraint from input to output
    setOrderingConstraint(input.getOrderingConstraint());
  }
  
  // getter methods

  /**
   * Get the internal query identifier (as given by the query manager) for
   * the query for whom this is the output operator 
   * (physical layer representaion)
   * @return the internal query identifier
   */
  public int getQueryId() {
    return queryId;
  }

  /**
   * @param queryId The queryId to set.
   */
  public void setQueryId(int queryId) {
    this.queryId = queryId;
  }

  /**
   * @return Returns the epr.
   */
  public String getEpr()
  {
    return eprDest.getExtDest();
  }
 
  /**
   * Get the external destination
   * @return Returns the destination
   */
  public Destination getDestination()
  {
    return eprDest;
  }
  
  /**
   * @return Return isUpdateSemantics flag for operator's EPR destination
   */
  public boolean getIsUpdateSemantics()
  {
    return eprDest.getIsUpdateSemantics();
  }
  
  /**
   * @return isPrimaryKeyExist
   */
  public boolean getIsPrimaryKeyExists()
  {
    return this.isPrimaryKeyExist;
  }
  
  /**
   * @return true if batching output is enabled for this EPR destination
   *         false otherwise
   */
  public boolean isBatchOutputTuples()
  {
    return eprDest.isBatchOutputTuples();
  }
 
  /**
   * @return true if this output operator would propagate heartbeat,
   *         false otherwise.
   */
  public boolean getPropagateHeartbeat()
  {
    return eprDest.getPropagateHeartbeat();
  }
   
  /**
   * @return output Synopsis
   */
  public PhySynopsis getOutputSyn() {
    return getSynopsis(OUT_SYN);
  }
  
  /**
   * Set output synopsis
   * @param outSyn
   */
  public void setOutputSyn(PhySynopsis outSyn) {
    setSynopsis(OUT_SYN, outSyn);
  }
  
  /**
   * Get input synopsis
   * @return
   */
  public PhySynopsis getPlusSyn() {
    return getSynopsis(PLUS_SYN);
  }
  
  /**
   * Set input Synopsis
   * @param inpSyn
   */
  public void setPlusSyn(PhySynopsis plusSyn)
  {
    setSynopsis(PLUS_SYN, plusSyn);
  }
  
  /**
   * Get input synopsis
   * @return
   */
  public PhySynopsis getMinusSyn() {
    return getSynopsis(MINUS_SYN);
  }
  
  /**
   * Set input Synopsis
   * @param inpSyn
   */
  public void setMinusSyn(PhySynopsis minusSyn)
  {
    setSynopsis(MINUS_SYN, minusSyn);
  }
  

  /**
   * Link Output Operator' synopsis with one of its underlying operators' store
   */
  public void linkSynStore()
  {
    // Synopsis exists only if Primary key exists
    if(this.getIsPrimaryKeyExists() || this.isBatchOutputTuples())
    {
      PhySynopsis outSyn = getOutputSyn();
      outSyn.makeStub(this.getStore());
      
      PhySynopsis plusSyn = getPlusSyn();
      plusSyn.makeStub(this.getStore());
      
      PhySynopsis minusSyn = getMinusSyn();
      minusSyn.makeStub(this.getStore());
      
    }
  }
  
  @Override
  public boolean isDependentOnChildSynAndStore(PhyOpt input)
  {
    if(this.getIsPrimaryKeyExists())
      return true;
    else
      return false;
  }
  
  public boolean delete(QueryDeletionContext ctx) throws CEPException
  {
    freeObjects();
    freeStore();
    freeSynopsis();
    freeInQueues();

    // remove it from the list of query outputs
    execContext.getPlanMgr().dropQueryOutput(ctx.getQuery().getId(), this);
    
    //remove from the physical operator list maintained by PlanManager
    execContext.getPlanMgr().removePhyOpt(this);
    
    return true;
  }
  
  //Generate and return visualiser compatible XML plan
  public String getXMLPlan2() throws CEPException {
    StringBuilder xml = new StringBuilder();
    xml.append("<name> Output </name>\n");
    xml.append("<lname> Output </lname>\n");
    xml.append(super.getXMLPlan2());
    xml.append("<query>");
    xml.append(queryId);
    xml.append("</query>\n");
    xml.append("<property name = \"Query\" value = \"");
    xml.append(queryId);
    //xml.append(" ");
    //xml.append(QueryManager.getQueryManager().getQuery(queryId).getText());
    xml.append(" \"/>\n");
    xml.append("<property name = \"Query String\" value = \"");
    String s = execContext.getQueryMgr().getQuery(queryId).getText();
    s = s.replaceAll("&","&amp;");
    s = s.replaceAll("\"","&quot;");
    s = s.replaceAll("<","&lt;");
    s = s.replaceAll(">","&gt;");
    xml.append(s);
    xml.append("\"/>\n");
    return xml.toString();
  }
  
  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    // Output has Relation Synopsis if it maintains a primary key over output
    if((this.getIsPrimaryKeyExists() || this.isBatchOutputTuples()) 
        && getOutputSyn() == syn)
      return PhySynPos.OUTPUT.getName();
    else if((this.getIsPrimaryKeyExists() || this.isBatchOutputTuples())
            && getMinusSyn() == syn)
      return PhySynPos.LEFT.getName();
    else if((this.getIsPrimaryKeyExists() || this.isBatchOutputTuples())
            && getPlusSyn() == syn)
      return PhySynPos.RIGHT.getName();
    else
     assert(false);
    return null;
  }

}
