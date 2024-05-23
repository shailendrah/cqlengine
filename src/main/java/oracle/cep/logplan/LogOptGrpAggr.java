/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptGrpAggr.java /main/11 2012/05/17 06:50:33 udeshmuk Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Group Aggregation Logical Operator

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk    05/12/12 - setter and getter for isContainGroupByExpr
 sborah      04/11/11 - use getAllReferencedAttrs()
 sborah      03/29/11 - override canPartitionExprBePushed()
 sborah      01/29/09 - fix for bug 8208755
 sborah      12/16/08 - handle constants
 udeshmuk    06/04/08 - support for xmlagg.
 udeshmuk    04/17/08 - support for aggr distinct.
 rkomurav    02/28/08 - parameterize errors
 sbishnoi    06/08/07 - support for multi-arg UDAs
 hopark      12/06/06 - check unbound stream
 rkomurav    10/05/06 - expr in aggr
 anasrini    07/12/06 - support for user defined aggregations 
 anasrini    05/02/06 - add getter methods 
 anasrini    04/20/06 - support for GROUP BY operator 
 najain      02/08/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptGrpAggr.java /main/11 2012/05/17 06:50:33 udeshmuk Exp $
 *  @author  najain  
 *  @since   1.0
 */
package oracle.cep.logplan;

import java.util.ArrayList;

import oracle.cep.common.AggrFunction;
import oracle.cep.common.BaseAggrFn;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.dataStructures.internal.ExpandableArray;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrAggr;
import oracle.cep.logplan.attr.AttrXMLAgg;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprOrderBy;

/**
 * Group Aggregation Logical Operator
 *
 * @since 1.0
 */

public class LogOptGrpAggr extends LogOpt {

  /** group attributes */
  ArrayList<Attr> groupAttrs;

  /** aggregation attributes */
  ArrayList<AttrAggr>  aggrAttrs;

  /** Number of attributes in the group by clause */
  int            numGroupAttrs;
  
  /** Number of attributes that are aggregation expressions */
  int            numAggrAttrs;

  /** Group by expressions. Used in aggr distinct */
  ArrayList<Expr>  groupExprs;
  
  /** true if at least one expression in the group by clause */
  boolean isContainGroupByExpr = false;

  public boolean isContainGroupByExpr()
  {
    return isContainGroupByExpr;
  }

  public void setContainGroupByExpr(boolean isContainGroupByExpr)
  {
    this.isContainGroupByExpr = isContainGroupByExpr;
  }

  /**
   * Constructor
   * @param input the logical operator that is the input to this operator
   */
  public LogOptGrpAggr(LogOpt input) 
  {
    super(LogOptKind.LO_GROUP_AGGR);

    assert input != null;
    
    // Output is a relation
    setIsStream(false);

    setNumInputs(1);
    setInput(0, input);
    input.setOutput(this);

    // Initialize out attribute counts
    numGroupAttrs = 0;
    numAggrAttrs = 0;
    setNumOutAttrs(numGroupAttrs + numAggrAttrs);

    aggrAttrs  = new ExpandableArray<AttrAggr>(Constants.INITIAL_NUM_AGGR_ATTRS);
    groupAttrs = new ExpandableArray<Attr>(Constants.INITIAL_NUM_GROUP_ATTRS);
    groupExprs = new ExpandableArray<Expr>(Constants.INITIAL_NUM_GROUP_ATTRS);
  }

  /**
   * Add an attribute to the list of attributes in the group by clause
   * @param attr group by attribute to be added to the list
   */
  void addGroupByAttr(Attr attr) throws LogicalPlanException {

    assert numOutAttrs == numAggrAttrs + numGroupAttrs : numOutAttrs;

    groupAttrs.add(attr);
    numGroupAttrs++;
   
    setOutAttr(numOutAttrs, attr);
    numOutAttrs++;
  }
  
  void addGroupByExpr(Expr expr) throws LogicalPlanException 
  {
    groupExprs.set(numGroupAttrs-1, expr);
  }

  /**
   * Add an attribute to the list of attributes in the group by clause
   * @param fn aggregate function to be added to the list
   * @param expr aggregate parameter expression
   */
  void addAggr(BaseAggrFn fn, Expr[] expr, boolean isDistinct, Datatype dt,
               ExprOrderBy[] orderByExprs)
    throws LogicalPlanException {

    AttrAggr attrAggr;

    assert numOutAttrs == numAggrAttrs + numGroupAttrs : numOutAttrs;

    if(fn.getFnCode() == AggrFunction.XML_AGG)
    {
      attrAggr = new AttrXMLAgg(expr, fn, isDistinct, dt, orderByExprs);      
    }
    else 
      attrAggr = new AttrAggr(expr, fn, isDistinct, dt);
    
    aggrAttrs.add(attrAggr); 
    numAggrAttrs++;
   
    setOutAttr(numOutAttrs, attrAggr);
    numOutAttrs++;
  }
  
  /**
   * Can this partitionParallel expression be pushed down to the 
   * specified input 
   * @param inputNo the specified input number 
   * @return the Logical layer form of the expression that can serve as
   *         the partitionParallel expression of input inputNo
   */
  public Expr canPartitionExprBePushed(int inputNo)
  {
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
      for(int i = 0; i < groupAttrs.size(); i++)
      {
        if(attr.equals(groupAttrs.get(i)))
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

  // Getter methods

  /**
   * Get the number of attributes in the GROUP BY clause
   * @return the number of attributes in the GROUP BY clause
   */
  public int getNumGroupAttrs() {
    return numGroupAttrs;
  }

  /**
   * Get the number of attributes in the aggregation
   * @return the number of attributes in the aggregation
   */
  public int getNumAggrAttrs() {
    return numAggrAttrs;
  }

  /**
   * Get the attributes in the GROUP BY clause
   * @return the attributes in the GROUP BY clause
   */
  public ArrayList<Attr> getGroupAttrs() {
    return groupAttrs;
  }

  /**
   * Get the expressions form of attributes in the GROUP BY clause.
   * @return the expressions corresponding to attributes in GROUP BY clause.
   */
  public ArrayList<Expr> getGroupExprs() {
    return groupExprs;
  }
  
  /**
   * Get the aggregation attributes
   * @return the aggregation attributes
   */
  public ArrayList<AttrAggr> getAggrAttrs() {
    return aggrAttrs;
  }
  
}
