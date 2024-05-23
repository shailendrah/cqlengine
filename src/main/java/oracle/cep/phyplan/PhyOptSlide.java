/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptSlide.java /main/2 2013/04/25 21:06:15 vikshukl Exp $ */

/* Copyright (c) 2012, 2013, Oracle and/or its affiliates. 
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
    sbishnoi    05/28/12 - Creation
 */

package oracle.cep.phyplan;

import oracle.cep.exceptions.CEPException;
import oracle.cep.logplan.LogOpt;
import oracle.cep.service.ExecContext;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptSlide.java /main/2 2013/04/25 21:06:15 vikshukl Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class PhyOptSlide extends PhyOpt
{
  /** number of slide interval; measured in nanos */
  private long numSlideNanos;
  
  /**
   * Construct Slide Physical Operator
   * @param ec
   * @param input
   * @param logOptSlide
   * @param numSlideNanos
   * @throws PhysicalPlanException
   */
  public PhyOptSlide(ExecContext ec, 
                     PhyOpt input,
                     LogOpt logOptSlide, 
                     long numSlideNanos) throws PhysicalPlanException
  {
    super(ec, PhyOptKind.PO_SLIDE, input, logOptSlide, true, true);    
    copy(input, logOptSlide);
    // Heartbeat timeout is required in case of silent stream
    setHbtTimeoutRequired(true);
    this.numSlideNanos = numSlideNanos;
  }

  /**
   * @return the numSlideNanos
   */
  public long getNumSlideNanos()
  {
    return numSlideNanos;
  }


  @Override
  public String getRelnSynPos(PhySynopsis syn)
  {
    assert(getOutputSyn() == syn);
    return PhySynPos.OUTPUT.getName();
  }
  
  public PhySynopsis getOutputSyn() {
    return getSynopsis(0);
  }
  
  public void setOutputSyn(PhySynopsis outSyn) {
    setSynopsis(0, outSyn);
  }

  /* (non-Javadoc)
   * @see oracle.cep.phyplan.PhyOpt#linkSynStore()
   */
  @Override
  public void linkSynStore()
  {
    PhySynopsis relSyn = getOutputSyn();
    relSyn.makeStub(this.getStore());
  }

  /* (non-Javadoc)
   * @see oracle.cep.phyplan.PhyOpt#isDependentOnChildSynAndStore()
   */
  @Override
  public boolean isDependentOnChildSynAndStore(PhyOpt input)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see oracle.cep.phyplan.PhyOpt#getSignature()
   */
  @Override
  protected String getSignature()
  {
    return (this.getOperatorKind() + "#" + 
            this.getNumAttrs() + "#" + 
            this.getNumSlideNanos());
  }

  /* (non-Javadoc)
   * @see oracle.cep.phyplan.PhyOpt#isPartialEquivalent(oracle.cep.phyplan.PhyOpt)
   */
  @Override
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(!(opt instanceof PhyOptSlide))
      return false;
  
    // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
 
    PhyOptSlide slideOpt = (PhyOptSlide)opt;
  
    assert slideOpt.getOperatorKind() == PhyOptKind.PO_SLIDE;
  
    if(slideOpt.getNumInputs() != this.getNumInputs())
      return false;
  
    if(slideOpt.getNumAttrs() != this.getNumAttrs())
      return false;
    
    // Both Operator should have same slide interval value
    if(slideOpt.getNumSlideNanos() != this.getNumSlideNanos())
      return false;
    
    return true;
  }

  /* (non-Javadoc)
   * @see oracle.cep.phyplan.PhyOpt#getXMLPlan2()
   */
  @Override
  public String getXMLPlan2() throws CEPException
  {
    StringBuilder xml = new StringBuilder();
    xml.append("<name> Slide </name>\n");
    xml.append("<lname> Slide </lname>\n");
    xml.append(super.getXMLPlan2());
    xml.append("<property name = \"Slide Interval\" value = \"");
    xml.append(this.getNumSlideNanos());    
    xml.append("\"/>");
    return xml.toString();
  }
  
 @Override
 public String toString()
 {
   StringBuilder sb = new StringBuilder();

   sb.append("<PhysicalOperatorSlide>");
   sb.append(super.toString());
   sb.append("<NumoSlideNanos value=\"" + this.getNumSlideNanos() + "\" />");
   sb.append("</PhysicalOperatorSlide>");
   return sb.toString();
 }
 
  
}