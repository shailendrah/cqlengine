/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptRngWin.java /main/12 2013/04/25 21:06:16 vikshukl Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Range Window Physical Operator in the package oracle.cep.phyplan

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 vikshukl    04/17/13 - pass input operator to isDependentOnChildSynAndStore()
 udeshmuk    10/20/11 - API for knowing if this operator uses child's synopsis
 sbishnoi    03/16/11 - support for variable duration range window
 sbishnoi    03/16/11 - support for variable duration window
 sbishnoi    04/26/10 - setting requireHbtTimeout
 sborah      04/20/09 - reorganize sharing hash
 sborah      03/10/09 - modify getSharingHash()
 hopark      10/09/08 - remove statics
 hopark      10/25/07 - set synopsis
 hopark      07/13/07 - dump stack trace on exception
 parujain    03/07/07 - Extensible Windows
 parujain    12/15/06 - operator sharing
 rkomurav    09/13/06 - PhySynPos OO restructuring
 rkomurav    08/23/06 - add getXMLPlan2
 rkomurav    08/09/06 - slide and cleanup
 rkomurav    08/08/06 - remove unused import
 anasrini    08/03/06 - remove shareRelStore
 najain      06/21/06 - cleanup
 najain      05/25/06 - bug fix 
 najain      05/05/06 - sharing suppotrt 
 najain      04/06/06 - cleanup
 anasrini    04/06/06 - constructor cleanup 
 najain      03/24/06 - cleanup
 anasrini    03/24/06 - call getInputndex as child.getInputIndex(parent) 
 najain      02/23/06 - add createPhysicalOperator
 skaluska    02/15/06 - Move Synopsis from execution to phyplan 
 najain      02/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptRngWin.java /main/12 2013/04/25 21:06:16 vikshukl Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan;

import java.util.logging.Level;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.phyplan.window.PhyRngWinSpec;
import oracle.cep.phyplan.window.PhyWinSpec;
import oracle.cep.service.ExecContext;


/**
 * Row Window Physical Operator 
 */
public class PhyOptRngWin extends PhyOpt {
 
  /** Window specifications */
  private PhyWinSpec winSpec;
  
  /** position of attribute having value of expirty Ts */
  private int expTsPos;

  /** Synopsis for the window */
  
  public PhyWinSpec getWinSpec() {
    return winSpec;
  }

  public void setWinSpec(PhyWinSpec spec) 
  {
    this.winSpec = spec;
   
  }

  /**
   * Method to calculate a concise String representation
   * of the Physical operator based on its attributes.
   * @return 
   *      A concise String representation of the physical operator.
   */
  protected String getSignature()
  {
    return (this.getOperatorKind() + "#"
          + this.winSpec.toString());
  }
  
  
  
  public PhySynopsis getWinSyn() {
    return getSynopsis(0);
  }

  public void setWinSyn(PhySynopsis winSyn) {
    setSynopsis(0, winSyn);
  }
  
  /**
   * Return the lineage synopsis;Lineage synopsis will be used if this is a
   * variable duration window
   * @return
   */
  public PhySynopsis getOutputSyn() {
    return getSynopsis(0);
  }
  
  public void setOutputSyn(PhySynopsis outSyn) {
    setSynopsis(0, outSyn);
  }
  
  public boolean isVariableDurationWindow()
  {
    if(winSpec instanceof PhyRngWinSpec)
    {
      return ((PhyRngWinSpec)winSpec).isVariableDurationWindow();
    }
    else
      return false;
  }
  
  //link syn and store
  public void linkSynStore() {
    assert !this.isVariableDurationWindow();
    PhySynopsis winSyn = getWinSyn();
    winSyn.makeStub(this.getStore());
  }
  
  @Override
  public boolean isDependentOnChildSynAndStore(PhyOpt input)
  {
    return true;
  }

  public PhyOptRngWin(ExecContext ec, PhyWinSpec windowSpec, PhyOpt[] phyChildPlans)
      throws PhysicalPlanException {

    super(ec, PhyOptKind.PO_RANGE_WIN);
    // Initializations
    setStore(null);
    setInstOp(null);
    setWinSyn(null);
    
    // window specification:
    setWinSpec(windowSpec);
    
    setHbtTimeoutRequired(true);

    
    if(this.isVariableDurationWindow())
    {
      PhyOpt inp = phyChildPlans[0];
      
      // We will add one more attribute in the schema to keep the evaluated
      // expiryTs
      // Output schema of operator = 
      //  {Schema of the input stream + expirtyTs }      
      int numOutAttrs = inp.getNumAttrs() + 1;
      
      setNumAttrs(numOutAttrs);
      
      PhyRngWinSpec spec = (PhyRngWinSpec)winSpec;
      
      for(int i=0; i < numOutAttrs-1; i++)
      {
        setAttrMetadata(i, inp.getAttrMetadata(i));
      } 
      
      Datatype rangeColumnType = spec.getRangeExpr().getType();
      int rangeColumnLen = spec.getRangeExpr().getLength();
      int rangeColumnPrecision = rangeColumnType.getPrecision();
      
     AttributeMetadata expTsColumnMetadata = 
        new AttributeMetadata(rangeColumnType, rangeColumnLen, rangeColumnPrecision, 0);  
            
      expTsPos = numOutAttrs - 1;
      
      setAttrMetadata(expTsPos, expTsColumnMetadata);
    }
    else
    {
      // output schema = input schema :: since the instance of the class was 
      // allocated by the InterpreterFactory, we need to copy from the first 
      // child. Need to confirm with Anand whether there is a better way of 
      // doing the same.
      copy(phyChildPlans[0]);
    }
    

    // output is a relation, not a stream
    setIsStream(false);

    // input:
    setNumInputs(1);
    getInputs()[0] = phyChildPlans[0];

    try 
    {
      phyChildPlans[0].addOutput(this);
    } 
    catch (PhysicalPlanException ex) 
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, ex);
      // TODO::: just ignore it for now
    } 

    
  }

  public boolean getSharedSynType(int idx) {
    return true;
  }
  
  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorRangeWindow>");
    sb.append(super.toString());

    sb.append(winSpec.toString());
    sb.append("<PhysicalSynopsis>");
    if(this.isVariableDurationWindow())
    {
      PhySynopsis outSyn = getOutputSyn();
      sb.append(outSyn.toString());
    }
    else
    {
      PhySynopsis winSyn = getWinSyn();
      sb.append(winSyn.toString());        
    }
    sb.append("</PhysicalSynopsis>");

    sb.append("</PhysicalOperatorRangeWindow>");
    return sb.toString();
  }
  
  //Generate and return visualiser compatible XML plan
  public String getXMLPlan2() throws CEPException {
    StringBuilder xml = new StringBuilder();
    xml.append(super.getXMLPlan2());
    xml.append(winSpec.getXMLPlan2());
    return xml.toString();
  }
  
  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    // Range window has no Relation Synopsis
    assert(false);
    return null;
  }
  
  /**
   * This method tells whether the two operators are partially equivalent or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(!(opt instanceof PhyOptRngWin))
      return false;
  
   // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
 
    PhyOptRngWin rngOpt = (PhyOptRngWin)opt;
  
    assert rngOpt.getOperatorKind() == PhyOptKind.PO_RANGE_WIN;
  
    if(rngOpt.getNumInputs() != this.getNumInputs())
      return false;
  
    if(!(this.winSpec.equals(rngOpt.winSpec)))
      return false;
    
    return true;
  }

  /**
   * @return the expTsPos
   */
  public int getExpTsPos()
  {
    return expTsPos;
  }
  
}
