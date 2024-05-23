/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptRowWin.java /main/12 2013/07/25 08:36:50 udeshmuk Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Row Window Physical Operator in the package oracle.cep.phyplan

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk    06/24/13 - set heartbeat timeout required as true
 vikshukl    04/17/13 - pass input operator to isDependentOnChildSynAndStore()
 udeshmuk    10/20/11 - API for knowing if this operator uses child's synopsis
 sborah      04/20/09 - reorganize sharing hash
 sborah      03/10/09 - modify getSharingHash()
 sborah      10/22/08 - correcting getXMLPlan2()
 hopark      10/09/08 - remove statics
 parujain    06/17/08 - slide support
 hopark      10/25/07 - set synopsis
 hopark      07/13/07 - dump stack trace on exception
 parujain    12/18/06 - operator sharing
 rkomurav    09/13/06 - PhySynPos OO restructuring
 rkomurav    08/24/06 - add linkSynStore
 anasrini    08/03/06 - remove shareRelStore
 najain      06/21/06 - cleanup
 najain      05/25/06 - bug fix 
 dlenkov     05/17/06 - ROW window support
 najain      04/06/06 - cleanup
 anasrini    04/06/06 - constructor cleanup 
 najain      03/24/06 - cleanup
 skaluska    02/15/06 - Move Synopsis from execution to phyplan 
 najain      02/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptRowWin.java /main/12 2013/07/25 08:36:50 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan;

import java.util.logging.Level;

import oracle.cep.exceptions.CEPException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptRowWin;
import oracle.cep.service.ExecContext;

/**
 * Row Window Physical Operator 
 */
public class PhyOptRowWin extends PhyOpt {
  /** Window size */
  int         numRows;
  
  /** slide */
  int         slide;

  /** Synopsis for the window */

  public long getNumRows() {
    return numRows;
  }
  
  public int getSlide() {
    return slide;
  }

  public void setNumRows( int par_numRows) {
    this.numRows = par_numRows;
  }
  
  public void setSlide(int par_slide)
  {
    this.slide = par_slide;
  }

  public PhySynopsis getWinSyn() {
    return getSynopsis(0);
  }

  public void setWinSyn( PhySynopsis par_winSyn) {
    setSynopsis(0, par_winSyn);
  }
  
  //link syn and store
  public void linkSynStore() {
    PhySynopsis winSyn = getWinSyn();
    winSyn.makeStub(this.getStore());
  }
  
  @Override
  public boolean isDependentOnChildSynAndStore(PhyOpt input)
  {
    return true;
  }

  public PhyOptRowWin( ExecContext ec, LogOpt logPlan, PhyOpt[] phyChildPlans)
      throws PhysicalPlanException {

    super( ec, PhyOptKind.PO_ROW_WIN);

    assert logPlan != null;
    assert logPlan.getNumInputs() == 1;
    assert logPlan instanceof LogOptRowWin;

    // Initializations
    setStore( null);
    setInstOp( null);
    setWinSyn( null);

    copy( phyChildPlans[0]);

    // Output is a relation not a stream
    setIsStream( false);

    // input:
    setNumInputs( 1);
    getInputs()[0] = phyChildPlans[0];

    try 
    {
      phyChildPlans[0].addOutput(this);
    } 
    catch (PhysicalPlanException ex) 
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, ex);
      // TODO
    }

    // copy number of rows and slide
    setNumRows( ((LogOptRowWin) logPlan).getNumRows());
    setSlide(((LogOptRowWin)logPlan).getSlide());
   
    //Bug 16813624 : row window also needs to indicate heartbeats are needed.
    //The streamsource would then send the auto heartbeats and it will clear
    //out the congestion.
    setHbtTimeoutRequired(true);
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Physical operator based on its attributes.
   * @return 
   *      A concise String representation of the Physical operator.
   */
  protected String getSignature()
  {
    return (this.getOperatorKind() + "#Rows:" + numRows
          +"#Slide:" + slide);
  }
  
  public boolean getSharedSynType( int idx) {
    return true;
  }
  
  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorRowWindow>");
    sb.append(super.toString());

    sb.append("<Rows numRows =\"" + numRows + "\" />");
    sb.append("<Slide slide =\"" + slide + "\" />");

    sb.append("<PhysicalSynopsis>");
    PhySynopsis winSyn = getWinSyn();
    sb.append( winSyn.toString());
    sb.append("</PhysicalSynopsis>");

    sb.append("</PhysicalOperatorRowWindow>");
    return sb.toString();
  }
  
  //Create and return visualiser compatible XML Plan
  public String getXMLPlan2() throws CEPException {
    StringBuilder xml = new StringBuilder();
    xml.append("<name> RowWin </name>\n");
    xml.append("<lname> Row Window </lname>\n");
    xml.append(super.getXMLPlan2());
    xml.append("<property name = \"Num Rows\" value = \"");
    xml.append(numRows);
    xml.append("\"/>");
    xml.append("<property name = \"Slide size\" value = \"");
    xml.append(slide);  
    xml.append("\"/>\n");
    
    return xml.toString();
  }
  
  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    // Row Window has no Relation Synopsis
    assert(false);
    return null;
  }

  
  /**
   * This method tells whether the two operators are partially equivalent or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(!(opt instanceof PhyOptRowWin))
      return false;
  
   // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
 
    PhyOptRowWin rowOpt = (PhyOptRowWin)opt;
  
    assert rowOpt.getOperatorKind() == PhyOptKind.PO_ROW_WIN;
  
    if(rowOpt.getNumInputs() != this.getNumInputs())
      return false;
  
    if((this.numRows != rowOpt.numRows))
      return false;
    
    if((this.slide != rowOpt.slide))
      return false;
    
    return true;
  }
  
}
