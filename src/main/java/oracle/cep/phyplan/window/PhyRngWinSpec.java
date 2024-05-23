/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/window/PhyRngWinSpec.java /main/3 2011/05/09 23:12:07 sbishnoi Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    03/16/11 - support for variable duration expression in range
                           window operator
    hopark      10/12/07 - support PhyPartnWinSpec
    parujain    03/07/07 - Range Window spec
    parujain    03/07/07 - Creation
 */

/**
 *  @version $Header: PhyRngWinSpec.java 12-oct-2007.12:58:50 hopark Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.window;

import oracle.cep.common.TimeUnit;
import oracle.cep.logplan.LogOpt;
import oracle.cep.phyplan.expr.Expr;

public class PhyRngWinSpec extends PhyWinSpec 
{
  /** Window size */
  private long        rangeUnits;

  /** Slide size */
  private long        slideUnits;
  
  /** flag to check if the current window is variable duration window */
  private boolean     isVariableDurationWindow;
  
  /** Expression which will evaluate to range in case of variable dur. win.*/
  private Expr        rangeExpr;
  
  /** time unit of range value */
  private TimeUnit    rangeUnit;

  public PhyRngWinSpec()
  {
  }
  
  public PhyRngWinSpec(LogOpt op)
  {
    assert op instanceof oracle.cep.logplan.LogOptRngWin;
    
    assert op != null;
    assert op.getNumInputs() == 1;
    
    oracle.cep.logplan.LogOptRngWin src = (oracle.cep.logplan.LogOptRngWin)op;
   
    this.rangeUnits = src.getRangeUnits();
    this.slideUnits = src.getSlideUnits();
    setWindowKind(WinKind.RANGE);
  }
  
  public long getRangeUnits() {
    return rangeUnits;
  }

  public void setRangeUnits(long rangeUnits) {
    this.rangeUnits = rangeUnits;
  }

  public long getSlideUnits() {
    return slideUnits;
  }

  public void setSlideUnits(long slideUnits) {
    this.slideUnits = slideUnits;
  }

  public boolean isVariableDurationWindow()
  {
    return isVariableDurationWindow;
  }

  public Expr getRangeExpr()
  {
    return rangeExpr;
  }

  public void setVariableDurationWindow(boolean isVariableDurationWindow)
  {
    this.isVariableDurationWindow = isVariableDurationWindow;
  }

  public void setRangeExpr(Expr rangeExpr)
  {
    this.rangeExpr = rangeExpr;
    setVariableDurationWindow(true);
  }

  
  /**
   * @param rangeUnit the rangeUnit to set
   */
  public void setRangeUnit(TimeUnit rangeUnit)
  {
    this.rangeUnit = rangeUnit;
  }

  /**
   * @return the rangeUnit
   */
  public TimeUnit getRangeUnit()
  {
    return rangeUnit;
  }

  @Override
  public boolean equals(Object other) {
    if(this == other)
      return true;
    
    if(other == null)
      return false;
    
    if(getClass() != other.getClass())
      return false;
    
    PhyRngWinSpec rngWin = (PhyRngWinSpec)other;
    
    if(isVariableDurationWindow ^ rngWin.isVariableDurationWindow())
    {
      return false;
    }
    
    if(isVariableDurationWindow)
    {
      if(!this.rangeExpr.equals(rngWin.getRangeExpr()))
        return false;
      
      if(!this.rangeUnit.equals(rngWin.getRangeUnit()))
        return false;
    }
    else
    {
      if(rangeUnits != rngWin.rangeUnits)
        return false;
    }   
    
    if(slideUnits != rngWin.slideUnits)
      return false;
    
    return true;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<PhysicalRangeWindowSpecs>");
    
    sb.append("<RangeUnits>");
    if(isVariableDurationWindow)
    {
      sb.append("<Value value=\"" +rangeExpr.toString() +" " + rangeUnit.name()
          +  "\" />");
    }
    else
      sb.append("<Value value=\"" +rangeUnits +"\" />");
    sb.append("</RangeUnits>");
    sb.append("<SlideUnits>");
    sb.append("<Value value=\"" + slideUnits +"\" />");
    sb.append("</SlideUnits>");

    sb.append("</PhysicalRangeWindowSpecs>");
    return sb.toString();
  }
  
  public String getXMLPlan2(){
    StringBuilder xml = new StringBuilder();
    xml.append("<name> TimeWin </name>\n");
    xml.append("<lname> Time Based Window </lname>\n");
    xml.append("<property name = \"Range\" value = \"[");
    if(isVariableDurationWindow)
      xml.append(rangeExpr.getXMLPlan2());
    else
      xml.append(rangeUnits);
    xml.append(",");
    xml.append(slideUnits);
    xml.append("]\"/>\n");
    return xml.toString();
  }

}
