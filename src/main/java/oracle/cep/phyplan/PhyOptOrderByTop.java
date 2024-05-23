/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptOrderByTop.java /main/8 2012/06/18 06:29:07 udeshmuk Exp $ */

/* Copyright (c) 2009, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    06/02/12 - removed the archived relation related methods
    udeshmuk    06/29/11 - support for archived relation
    sborah      10/07/09 - bigdecimal support
    sborah      04/20/09 - reorganize sharing hash
    sborah      03/17/09 - define sharingHash
    sbishnoi    03/17/09 - fixing ArrayIndexOutOfBound in compareOrderByExprs
    sbishnoi    03/09/09 - adding support to process partition by in order by
                           clause
    sbishnoi    02/10/09 - Creation
 */

package oracle.cep.phyplan;

import java.util.ArrayList; 

import oracle.cep.exceptions.CEPException;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptOrderBy;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.service.ExecContext;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptOrderByTop.java /main/8 2012/06/18 06:29:07 udeshmuk Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class PhyOptOrderByTop extends PhyOpt
{
  /** array of order by expressions */
  private Expr[] orderExprs;
 
  /** number of order by rows*/
  private int    numOrderByRows;
  
  /** list of partition by attributes */
  private ArrayList<Attr> partitionByAttrs;

  private StringBuffer orderBy = null;

  private StringBuffer partitionBy = null;
  
  /**
   * Constructor
   * @param ec
   * @param input
   * @param orders
   * @param numOrderByRows
   * @throws PhysicalPlanException
   */
  public PhyOptOrderByTop(ExecContext ec, PhyOpt input, Expr[] paramOrderExprs, 
                          LogOpt logPlan) 
    throws PhysicalPlanException, CEPException
  {
    super(ec, PhyOptKind.PO_ORDER_BY_TOP, input, logPlan, false, false);
        
    LogOptOrderBy logOptOrderBy = (LogOptOrderBy)logPlan;
    copy(input, logOptOrderBy);
    
    orderExprs     = paramOrderExprs;
    numOrderByRows = logOptOrderBy.getNumOrderByRows();
   
    // OrderByTop will always output a relation
    setIsStream(false);
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Physical operator based on its attributes.
   * @return 
   *      A concise String representation of the Physical operator.
   */
  protected String getSignature()
  {
    StringBuilder expr = new StringBuilder();
    
    expr.append(this.getOperatorKind() + "#"); 
    expr.append(getExpressionList(this.orderExprs));
    
    if(this.partitionByAttrs != null)
      for(Attr partitionAttr : this.partitionByAttrs)
      {
        if(partitionAttr != null)
          expr.append(partitionAttr.getSignature());
      }
    
    return expr.toString();
  }
  
  
  
  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) 
  {
    // OrderByTop doesn't have any Relation Synopisis
    // so it should not come here
    assert false;
    return null;
  }
  
  /**
   * Getter for orderExprs
   * @return the order-by expressions
   */
  public Expr[] getExprs()
  {
    return orderExprs;
  }
  
  /**
   * Getter for numOrderByRows
   * @return the number of top rows
   */
  public int getNumOrderByRows()
  {
    return numOrderByRows;  
  }
  
  /**
   * Add a partition by attribute to OrderBy operator
   * @param paramAttr parameter attribute
   */
  public void addPartitionByAttr(Attr paramAttr)
  {
    assert paramAttr != null;
    if(partitionByAttrs != null)
      partitionByAttrs.add(paramAttr);
    else
    {
      // Lazy Initialization
      partitionByAttrs = new ArrayList<Attr>();
      partitionByAttrs.add(paramAttr);
    }
   
  }
  
  /**
   * Get a list of partition by attributes
   * @return
   */
  public ArrayList<Attr> getPartitionByAttrs()
  {
    return partitionByAttrs;
  }
  
  public PhySynopsis getOutputSyn() {
    return getSynopsis(0);
  }
  
  public void setOutputSyn(PhySynopsis outSyn) {
    setSynopsis(0, outSyn);
  }
  
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(opt == null || !(opt instanceof PhyOptOrderByTop))
    return false;
  
    // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
    
    PhyOptOrderByTop other = (PhyOptOrderByTop)opt;
    assert other.getOperatorKind() == PhyOptKind.PO_ORDER_BY_TOP;
    
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
  
  public boolean compareOrderByExprs(PhyOptOrderByTop opt)
  {
    if(opt.orderExprs == null)
      return false;
    
    if(numOrderByRows != opt.getNumOrderByRows())
      return false;
    
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
    
    // compare partition by attributes
    if(partitionByAttrs != null && opt.partitionByAttrs != null)
    {
      if(partitionByAttrs.size() != opt.partitionByAttrs.size())
        return false;
      
      // Note:
      // {c1,c2} will be equal to {c1,c2}
      // {c1,c2} will not be equal to {c2,c1}
      for(int i = 0; i < partitionByAttrs.size(); i++)
      {
       if(! partitionByAttrs.get(i).equals(opt.partitionByAttrs.get(i)))
         return false;
      }
    }
    else if(partitionByAttrs != opt.partitionByAttrs)
      return false;
    
    return true;
  }
  
  /**
   * @override toString
   */
  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorOrderByTop>");
    sb.append(super.toString());

    if (orderExprs.length != 0)
    {
      sb.append("<NumofOrderByExpressions numExpr=\"" + orderExprs.length + "\" />");
      for (int i = 0; i < orderExprs.length; i++)
        sb.append(orderExprs[i].toString());
    }
    sb.append("<NumOrderByRows>");
      sb.append(numOrderByRows);
    sb.append("</NumOrderByRows>");
    
    if(partitionByAttrs != null)
    {
       sb.append("PartitionByAttrs numAttrs=\"" + partitionByAttrs.size() + "\"/>");       
    }
    sb.append("</PhysicalOperatorOrderByTop>");
    return sb.toString();
  }
  
  /**
   * Generate and return visualizer compatible XML plan
   * @return XML plan in java.lang.String type
   */  
  public String getXMLPlan2() throws CEPException
  {
    int i;
    StringBuilder xml = new StringBuilder();
    xml.append("<name> OrderByTop </name>\n");
    xml.append("<lname> OrderbyTop </lname>\n");
    xml.append(super.getXMLPlan2());
    xml.append("<property name = \"OrderbyTop Expr List\" value = \"");
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
    xml.append("<property name = \"OrderbyTop Rows\" value = \"");
    xml.append(numOrderByRows);
    xml.append("\"/>");
    
    if(partitionByAttrs != null)
    {
      xml.append("<property name = \"Number of Partition By Attributes\" value = \"");
      xml.append(partitionByAttrs.size());
      xml.append("\"/>");
    }
    return xml.toString();
  }

}
