/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptOrderBy.java /main/7 2011/05/17 03:26:06 anasrini Exp $ */

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
    sborah      04/11/11 - use getAllReferencedAttrs()
    sborah      03/29/11 - override canPartitionExprBePushed()
    sborah      05/19/10 - Typo
    sbishnoi    03/06/09 - adding support for partition by attributes in order
                           by clause
    sbishnoi    03/02/09 - adding new constructor to incorporate numOrderByRows
    sbishnoi    02/09/09 - adding support for orderby top
    parujain    06/27/07 - Order by Operator
    parujain    06/27/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptOrderBy.java /main/5 2010/05/19 07:12:23 sborah Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan;

import java.util.ArrayList;

import oracle.cep.exceptions.LogicalPlanError;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprOrderBy;

/**
 * Order by logical operator
 * @author parujain
 *
 */
public class LogOptOrderBy extends LogOpt implements Cloneable
{
  /** order by expressions */
  private ArrayList<ExprOrderBy> orderExprs;
   
  /** number of ordered output tuples from order-by*/
  private int                    numOrderByRows;
  
  /** partition by attributes*/
  private ArrayList<Attr>        partitionByAttrs;   
  
  
  public LogOptOrderBy()
  {
    super(LogOptKind.LO_ORDER_BY);
    orderExprs = new ArrayList<ExprOrderBy>();
  }
  
  public LogOptOrderBy(LogOpt input, int numOrderByRows)
  {
    super(LogOptKind.LO_ORDER_BY);
    
    assert input != null;

    // If number of order by rows are mentioned(will always greater than zero)
    //  output of this operator will be a relation
    // otherwise 
    //  output is a relation or a stream , will be decided by input
    if(numOrderByRows > 0)
    {
      this.numOrderByRows = numOrderByRows;
      setIsStream(false);
    }
    else
      setIsStream(input.getIsStream());

    setNumInputs(1);
    setInput(0, input);
    
    // output attributes of orderBy is same as input
    copy(input);
    
    orderExprs = new ArrayList<ExprOrderBy>();
    input.setOutput(this);
  }
  
  public LogOptOrderBy clone() throws CloneNotSupportedException
  {
    LogOptOrderBy op = (LogOptOrderBy)super.clone();
    
    // Sets the order expressions
    for(int i=0; i<orderExprs.size(); i++)
      op.orderExprs.add(this.orderExprs.get(i).clone());
    
    // Sets the number of order-by rows
    op.setNumOrderByRows(this.getNumOrderByRows());
    
    // Sets the partition by attributes (if any)
    if(partitionByAttrs != null)
    {
      for(int i = 0; i < partitionByAttrs.size(); i++)
        op.partitionByAttrs.add(partitionByAttrs.get(i).clone());
    }
    
    return op;
  }
  
  public Expr canPartitionExprBePushed(int inputNo)
  {
    // cannot push down expressions if the order by operator has no partition by
    // clause
    if(partitionByAttrs == null)
      return null;
    
    Expr ppExpr = getPartitionParallelExpr();
    
    if(ppExpr == null)
      return null;
    
    // get a list of all the attributes in the parallel partitioning expression
    ArrayList<Attr> attrs = new ArrayList<Attr>();
    ppExpr.getAllReferencedAttrs(attrs);
    
    // check if all attributes in the parallel paritioning expression
    // are present in the group by attributes.
    for(Attr attr : attrs)
    {
      boolean found = false;
      for(int i = 0; i < partitionByAttrs.size(); i++)
      {
        if(attr.equals(partitionByAttrs.get(i)))
        {
          found = true;
          break;
        }
      }
      if(!found)
        return null;
    }
    
    LogUtil.fine(LoggerType.TRACE,
        "Partition parallelism possible for input " + inputNo
        + " of logical op " + operatorKind 
        + " with expression " + ppExpr);
    
    return ppExpr;
  }
  
  /**
   * Add order by expression to the logical operator
   * Note: By calling this method will not include this expression into
   * output attributes
   * @param exp parameter expression
   */
  public void add_orderby_expr(ExprOrderBy exp)
  {
    assert exp != null;    
    orderExprs.add(exp);
    // Note: Order by expression will not be part of output tuple
    // of order by operator; so not increasing numOutAttrs;
  }
  
  
  /**
   * Returns the list of order expressions
   * @return List of Order by expressions
   */
  public ArrayList<ExprOrderBy> getOrderByExprs()
  {
    return this.orderExprs;
  }
  
  /**
   * Getter for numOrderByRows
   * @return number of ordered window rows
   */
  public int getNumOrderByRows()
  {
    return numOrderByRows;
  }
  
  /**
   * Setter for numOrderByRows
   * @param paramNumOrderByRows
   */
  public void setNumOrderByRows(int paramNumOrderByRows)
  {
    numOrderByRows = paramNumOrderByRows;
  }
  
  /**
   * Add an attribute to list of partition by attributes 
   * @param paramAttr
   */
  public void addPartitionByAttrs(Attr paramAttr)
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
    //Note: partition by attribute will not be part of output tuple of
    // the order by operator; so not modifying numOutAttrs;
  }
  
  /**
   * Get a list of partition by attributes
   * @return a list of partition by attributes
   */
  public ArrayList<Attr> getPartitionByAttrs()
  {
    return partitionByAttrs;
  }
  
  /** Override toString() */
  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<OrderbyLogicalOperator>");

    // Dump the common fields
    sb.append(super.toString());

    if (orderExprs.size() != 0)
    {
      sb.append("<NumberOfExprs numExprs=\"" + orderExprs.size() + "\" />");

      for (int i = 0; i < orderExprs.size(); i++)
        sb.append(orderExprs.get(i).toString());
    }
    if(numOrderByRows > 0)
    {
      sb.append("<numOrderByRows>");
      sb.append(numOrderByRows);
      sb.append("</numOrderByRows>");
    }
    
    sb.append("<NumberOfPartitionByAttrs>");
    sb.append(partitionByAttrs != null ? partitionByAttrs.size() : 0);
    sb.append("</NumberOfPartitionByAttrs>");
    
    sb.append("</OrderbyLogicalOperator>");

    return sb.toString();
  }
  
  protected void validate() throws LogicalPlanException
  { 
    // Condition:
    //  If order-by clause doesn't mention number of rows;
    //    then input should be a stream
    if(!this.getInput(0).getIsStream() && numOrderByRows == 0)
      throw new LogicalPlanException(LogicalPlanError.INVALID_ORDER_BY_USAGE);
  }
  
}
