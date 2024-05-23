/* $Header: AggrExpr.java 05-jun-2008.01:55:40 udeshmuk Exp $ */

/* Copyright (c) 2006, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Class representing an aggregate expression

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    06/03/08 - check isDistinctAggr equality in equals().
    udeshmuk    04/15/08 - support for aggr distinct
    rkomurav    06/25/07 - cleanup
    sbishnoi    06/04/07 - support for multiple argument UDAs
    rkomurav    05/11/07 - add isClassB
    rkomurav    05/28/07 - add getAllReferenced* methods
    parujain    10/13/06 - passing returntype to Logical
    rkomurav    09/19/06 - bug 5446939
    najain      08/28/06 - expr is a abstract class
    anasrini    02/27/06 - fix xml closing in toString 
    anasrini    02/26/06 - implement toString 
    anasrini    02/24/06 - add constructor, getter methods 
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
 */

/**
 *  @version $Header: AggrExpr.java 05-jun-2008.01:55:40 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import java.util.List;

import oracle.cep.common.BaseAggrFn;
import oracle.cep.common.Datatype;

/**
 * Class representing an aggregate expression
 *
 * @since 1.0
 */

public class AggrExpr extends Expr {

  private BaseAggrFn   aggrFn;
  private Expr[]       aggrParamExprs;
  
  /** flag = true indicates usage of distinct inside aggregate expression */
  private boolean      isDistinctAggr;

  public ExprType getExprType() {
    return ExprType.E_AGGR_EXPR;
  }

  public Datatype getReturnType() {
    return dt;
  }
 
  /**
   * Constructor
   * @param aggrFn the aggregation function
   * @param dt the datatype of the return of the aggregation function
   * @param inpExpr the input expression array
   */
  public AggrExpr(BaseAggrFn aggrFn, Datatype dt, Expr inpExpr)
  {
    this.aggrFn            = aggrFn;
    this.dt                = dt;
    this.aggrParamExprs    = new Expr[1];
    this.aggrParamExprs[0] = inpExpr;
    this.isDistinctAggr    = false;
  }
  
  /**
   * Constructor
   * @param aggrFn the aggregation function
   * @param dt the datatype of the return of the aggregation function
   * @param inpExpr the input expression array
   */
  public AggrExpr(BaseAggrFn aggrFn, Datatype dt, Expr[] inpExpr)
  {
    this.aggrFn         = aggrFn;
    this.dt             = dt;
    this.aggrParamExprs = inpExpr;
    this.isDistinctAggr = false;
  }
  
  
  /**
   * Get the aggregate function
   * @return the aggregate function
   */
  public BaseAggrFn getAggrFunction()
  {
    return aggrFn;
  }

  /**
   * Get the expression
   * @return the expression
   */
  public Expr[] getExprs() {  
    return aggrParamExprs;
  }
  
 /**
  * Get number of aggregate parameters
  * @return number of aggregate parameters
  */
  public int getNumParamExprs(){
    return aggrParamExprs.length;
  }

  /**
   * Get isDistinctAggr flag
   * @return flag indicating whether AggrExpr uses Distinct or not
   */
  public boolean getIsDistinctAggr()
  {
    return this.isDistinctAggr;
  }

  /**
   * Set isDistinctAggr flag
   * @param isDistinctAggr
   *        a flag indicating whether AggrExpr uses Distinct or not
   */
  public void setIsDistinctAggr(boolean isDistinctAggr)
  {
    this.isDistinctAggr = isDistinctAggr;
  }

  public void getAllReferencedAggrs(List<AggrExpr> aggrs)
  {
    if(!aggrs.contains(this))
      aggrs.add(this);
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type)
  {
    getAllReferencedAttrs(attrs, type, true);
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type,
      boolean includeAggrParams)
  {
    if(includeAggrParams)
    {
      for(int i=0; i < aggrParamExprs.length ; i++)
        aggrParamExprs[i].getAllReferencedAttrs(attrs, type, includeAggrParams);
    }
    else
      return;
  }
  
  public boolean equals(Object otherObject)
  {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    AggrExpr other = (AggrExpr)otherObject;
  
    boolean isEqual;
    isEqual = aggrFn.equals(other.aggrFn);
    
    for(int i=0; i < getNumParamExprs(); i++)
      isEqual = isEqual && aggrParamExprs[i].equals(other.aggrParamExprs[i]);
    
    isEqual = isEqual && (this.isDistinctAggr == other.isDistinctAggr);
    
    return isEqual;    
  }
  
  // toString
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<AggrExpr aggrFunction=\"" + aggrFn +"\" datatype=\""+ dt + "\" >");
    sb.append(aggrParamExprs.toString());
    sb.append("</AggrExpr>");
    
    return sb.toString();
  }

}
