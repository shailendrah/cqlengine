/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptOrderBy.java /main/7 2009/11/09 10:10:59 sborah Exp $ */

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
    sborah      10/07/09 - bigdecimal support
    sborah      04/20/09 - reorganize sharing hash
    sborah      03/17/09 - define sharingHash
    sbishnoi    03/17/09 - fixing compareOrderByExprs
    sbishnoi    03/03/09 - fix bug 8299581
    hopark      10/09/08 - remove statics
    parujain    06/28/07 - Order by Physical operator
    parujain    06/28/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptOrderBy.java /main/7 2009/11/09 10:10:59 sborah Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan;

import oracle.cep.exceptions.CEPException;
import oracle.cep.logplan.LogOpt;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.service.ExecContext;

public class PhyOptOrderBy extends PhyOpt {

  Expr[] orderExprs;
  
  public PhyOptOrderBy (ExecContext ec, PhyOpt input, Expr[] orders, 
    LogOpt logOptOrderBy) throws PhysicalPlanException
  {
    super(ec, PhyOptKind.PO_ORDER_BY, input, logOptOrderBy, false, true);    
    copy(input, logOptOrderBy);
    this.orderExprs = orders;
   
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Physical operator based on its attributes.
   * @return 
   *      A concise String representation of the Physical operator.
   */
  protected String getSignature()
  {
    return (this.getOperatorKind() + "#"
          + getExpressionList(orderExprs));
    
  }
  
  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    // Order by has no Relation Synopsis
    assert(false);
    return null;
  }
  
  /**
   * Get the order by expressions
   * 
   * @return the orderby expressions
   */
  public Expr[] getExprs()
  {
    return orderExprs;
  }
  
  /**
   * This method tells whether the two operators are partially equivalent or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(opt == null || !(opt instanceof PhyOptOrderBy))
      return false;
    
    //  this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
    
    PhyOptOrderBy other = (PhyOptOrderBy)opt;
    assert other.getOperatorKind() == PhyOptKind.PO_ORDER_BY;
    
    if(other.getNumInputs() != this.getNumInputs())
      return false;
  
    if(other.getNumAttrs() != this.getNumAttrs())
      return false;
    
    for (int i=0; i < getNumAttrs(); i++)
    {
      if(getAttrLen(i) != other.getAttrLen(i))
        return false;
    }
    return compareOrderByExprs(other);
  }
  
  private boolean compareOrderByExprs(PhyOptOrderBy opt)
  {
    if(orderExprs.length != opt.orderExprs.length)
      return false;
    
    for(int i=0; i < orderExprs.length; i++)
    {
  
      if(orderExprs[i].getKind() != opt.orderExprs[i].getKind())
        return false;
 
      if(orderExprs[i].getType() != opt.orderExprs[i].getType())
        return false;
  
      if(! orderExprs[i].equals(opt.orderExprs[i]))
        return false;
    }
    return true;
  }
  
  // toString method override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorOrderBy>");
    sb.append(super.toString());

    if (orderExprs.length != 0)
    {
      sb.append("<NumofOrderByExpressions numExpr=\"" + orderExprs.length + "\" />");
      for (int i = 0; i < orderExprs.length; i++)
        sb.append(orderExprs[i].toString());
    }

    sb.append("</PhysicalOperatorOrderBy>");
    return sb.toString();
  }
  
//Generate and return visualiser compatible XML plan
  public String getXMLPlan2() throws CEPException
  {
    int i;
    StringBuilder xml = new StringBuilder();
    xml.append("<name> OrderBy </name>\n");
    xml.append("<lname> Orderby </lname>\n");
    xml.append(super.getXMLPlan2());
    xml.append("<property name = \"Orderby Expr List\" value = \"");
    if (orderExprs.length != 0)
    {
      for (i = 0; i < (orderExprs.length - 1); i++)
      {
        xml.append(orderExprs[i].getXMLPlan2());
        xml.append(",");
      }
      xml.append(orderExprs[i].getXMLPlan2());
    }
    else
    {
      xml.append("(null)");
    }
    xml.append("\"/>");
    return xml.toString();
  }
  
  
  
}
