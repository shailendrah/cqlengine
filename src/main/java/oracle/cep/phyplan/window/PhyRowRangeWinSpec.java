/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/window/PhyRowRangeWinSpec.java /main/2 2009/01/09 15:21:31 parujain Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    01/08/09 - fix multiple lnames
    hopark      10/12/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/window/PhyRowRangeWinSpec.java /main/2 2009/01/09 15:21:31 parujain Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.window;

import oracle.cep.logplan.LogOpt;

public class PhyRowRangeWinSpec extends PhyRngWinSpec
{
  private int rows;

  public PhyRowRangeWinSpec(LogOpt op)
  {
    super();
    
    assert op instanceof oracle.cep.logplan.LogOptPrtnWin;
    
    assert op != null;
    assert op.getNumInputs() == 1;
    
    oracle.cep.logplan.LogOptPrtnWin src = (oracle.cep.logplan.LogOptPrtnWin)op;
    setRangeUnits(src.getRangeUnits());
    setSlideUnits(src.getSlideUnits());
    rows = src.getNumRows();
    
    setWindowKind(WinKind.PARTITION);
  }
  
   
  public int getRows() {
    return rows;
  }

  public void setRows(int rows) {
    this.rows = rows;
  }

  @Override
  public boolean equals(Object other) {
    if(this == other)
      return true;
    
    if(other == null)
      return false;
    
    if(getClass() != other.getClass())
      return false;
    
    PhyRowRangeWinSpec partnWin = (PhyRowRangeWinSpec)other;
    if (rows != partnWin.rows)
      return false;
    
    return super.equals(other);
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<PhysicalRowRangeWindowSpecs>");
    
    sb.append("<Rows>");
    sb.append("<Value value=\"" +rows +"\" />");
    sb.append("</Rows>");
    sb.append("<RangeUnits>");
    sb.append("<Value value=\"" +getRangeUnits() +"\" />");
    sb.append("</RangeUnits>");
    sb.append("<SlideUnits>");
    sb.append("<Value value=\"" + getSlideUnits() +"\" />");
    sb.append("</SlideUnits>");

    sb.append("</PhysicalPartitionWindowSpecs>");
    return sb.toString();
  }
  
  public String getXMLPlan2(){
    StringBuilder xml = new StringBuilder();
    xml.append("<property name = \"Row\" value = \"[");
    xml.append(rows);
    xml.append("]\"/>\n");
    xml.append("<property name = \"Range\" value = \"[");
    xml.append(getRangeUnits());
    xml.append(",");
    xml.append(getSlideUnits());
    xml.append("]\"/>\n");
    return xml.toString();
  }

}
