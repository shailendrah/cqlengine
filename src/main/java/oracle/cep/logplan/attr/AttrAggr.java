/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/attr/AttrAggr.java /main/5 2012/05/02 03:05:58 pkali Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 pkali       04/03/12 - moved datatype member to Attr class
 udeshmuk    06/04/08 - support for xmlagg
 udeshmuk    04/17/08 - support for aggr distinct.
 sbishnoi    06/08/07 - support for multi-arg UDAs
 rkomurav    10/05/06 - expressions over aggregations
 anasrini    07/12/06 - support for user defined aggregations 
 anasrini    05/31/06 - add method getAttrNamed 
 anasrini    04/20/06 - add constructor(AttrNamed, AggrFunction) 
 najain      05/30/06 - add check_reference 
 najain      05/26/06 - add isSame 
 najain      02/13/06 - add constructors/setters and getters 
 najain      02/08/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/attr/AttrAggr.java /main/5 2012/05/02 03:05:58 pkali Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.attr;

import oracle.cep.common.BaseAggrFn;
import oracle.cep.common.Datatype;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.expr.Expr;

/**
 * Aggregate Attribute Class definitions used by logical operators
 */
public class AttrAggr extends Attr
{
  /** Expression Parameter */
  private Expr[] aggrParamExpr;
  
  /** Aggregation function */
  private BaseAggrFn   function;
  
  /** true if distinct is used inside aggr, false otherwise */
  private boolean isDistinct;

  public AttrAggr()
  {
    attrKind = AttrKind.AGGR;
  }

  public AttrAggr(Expr[] expr, BaseAggrFn function, boolean isDistinct, Datatype dt)
  {
    this.aggrParamExpr = expr;
    this.function = function;
    this.isDistinct = isDistinct;
    this.dt = dt;
    attrKind = AttrKind.AGGR;
  }
  
  public AttrAggr(Expr[] expr, BaseAggrFn function)
  {
    this.aggrParamExpr = expr;
    this.function = function;
    this.isDistinct = false;
    this.dt = null;
    attrKind = AttrKind.AGGR;
  }


  public boolean equals(Object otherObject)
  {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;

    AttrAggr other = (AttrAggr) otherObject;
    boolean  isEqual;

    if(aggrParamExpr.length != other.aggrParamExpr.length)
      return false;
    
    isEqual = (isDistinct == other.isDistinct);
    isEqual = isEqual && (function.equals(other.function));
        
    for(int i=0; i < aggrParamExpr.length; i++)
      isEqual = isEqual && (aggrParamExpr[i].equals((other.aggrParamExpr)[i])); 
      
    return isEqual;
    
  }

  /**
   * @return Returns the aggregate Parameter expresion
   */
  public Expr[] getAggrParamExpr()
  {
    return aggrParamExpr;
  }
  
  public int getAggrParamExprLength()
  {
    return aggrParamExpr.length; 
  }
  /**
   * @param paramExpr
   * The fuction to set the paramter expression
   */
  public void setAggrParamExpr(Expr[] paramExpr)
  {
    this.aggrParamExpr = paramExpr; 
  }
  
  /**
   * @return Returns the function.
   */
  public BaseAggrFn getFunction()
  {
    return function;
  }

  /**
   * @param function
   *          The function to set.
   */
  public void setFunction(BaseAggrFn function)
  {
    this.function = function;
  }

  public boolean isSame(Attr input)
  {
    if (!super.isSame(input))
      return false;

    AttrAggr inp = (AttrAggr) input;
    
    if (aggrParamExpr.length != inp.getAggrParamExprLength())
      return false;
    
    boolean isEqual = function.equals(inp.getFunction());
    isEqual = isEqual && (isDistinct == inp.getIsDistinct());
     
    for(int i=0; i < aggrParamExpr.length; i++)
      isEqual = isEqual && (aggrParamExpr[i].equals((inp.getAggrParamExpr())[i]));
     
    return isEqual;

  }

  public boolean getIsDistinct()
  {
    return this.isDistinct;
  }

  public void setIsDistinct(boolean value)
  {
    this.isDistinct = value;
  }
  
  public Datatype getReturnDt()
  {
    return this.dt;
  }
  
  public boolean check_reference(LogOpt op)
  {
    for (int i = 0; i < op.getNumOutAttrs(); i++)
    {
      Attr attr = op.getOutAttr(i);
      if (attr instanceof AttrAggr)
      {
        if(compareAttr(attr))
          return true;
      }
    }
    return false;
  }

  protected boolean compareAttr(Attr attr)
  {
    AttrAggr attrAggr = (AttrAggr) attr;
    if (aggrParamExpr.length != attrAggr.getAggrParamExprLength())
      return false;
    
    boolean isEqual = function.equals(attrAggr.getFunction());
    isEqual  = isEqual && (isDistinct == attrAggr.getIsDistinct());
    
    for(int j=0; j < aggrParamExpr.length; j++)
      isEqual = isEqual && (aggrParamExpr[j].equals((attrAggr.getAggrParamExpr())[j]));
    
    return isEqual;
  }
  
  // toString method override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<AggregateAttribute>");

    sb.append(super.toString());
    sb.append("<Expressions>");
    for(int i=0; i < aggrParamExpr.length; i++)
      sb.append("<Expression Expr=\"" + aggrParamExpr[i].toString() + "\" />");
    sb.append("</Expressions>");
    sb.append("<AggrFunc aggrFunction=\"" + function.getFnCode() + "\" />");
    
    sb.append("</AggregateAttribute>");
    return sb.toString();
  }

}
