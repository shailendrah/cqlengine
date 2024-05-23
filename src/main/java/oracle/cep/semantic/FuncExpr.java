/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/FuncExpr.java /main/12 2012/07/30 19:52:54 pkali Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Semantic layer representation of a function expression

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       07/17/12 - meta data propagation for rewritten groupby expr
    sbishnoi    07/10/12 - bug 14214115
    vikshukl    01/31/12 - group by expr
    alealves    11/27/09 - Data cartridge context, default package support
    udeshmuk    09/09/09 - add funcname and cartridge name
    rkomurav    06/25/07 - cleanup
    sbishnoi    06/19/07 - support for collecting aggrs in func arg
    rkomurav    05/13/07 - add isClassB
    rkomurav    05/28/07 - restructure.. removeing aggr from funcexpr
    rkomurav    05/28/07 - add .equals
    parujain    10/13/06 - passing returntype to Logical
    najain      08/28/06 - expr is a abstract class
    anasrini    07/10/06 - support for user defined aggregations 
    anasrini    06/13/06 - Creation
    anasrini    06/13/06 - Creation
    anasrini    06/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/FuncExpr.java /main/12 2012/07/30 19:52:54 pkali Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.functions.UserDefinedFunction;

/**
 * Post semantic analysis representation of a function expression.
 *
 * @since 1.0
 */

public class FuncExpr extends Expr {

  /** internal identifier of the function */
  private int fnId;

  /** parameters of the function */
  private Expr[] params;
  
  /** runtime function implementation instance */
  private UserDefinedFunction funcImpl;
  
  /** name of the function */
  private String funcName;
  
  /** name of the link e.g. cartridge */
  private String cartridgeLinkName;
  
  /**
   * When this is constructor is used, the expressions
   *  are only considered equal if the fnId are equal.
   * 
   * @param fnId internal identifier of the function 
   * @param params parameters of the function
   * @param returnType return type of the function
   */
  public FuncExpr(int fnId, Expr[] params, Datatype returnType)
  {
    this.fnId      = fnId;
    this.params    = params;
    this.dt        = returnType;
    this.funcName  = null;
    this.cartridgeLinkName = null;
  }

  /**
   * When this constructor is used, the expressions
   *  are considered equal if their name and link match.
   * 
   * This constructor should be used by extension types that wish
   *  to be optimized for sharing.
   * 
   * @param fnId internal id of the function
   * @param params arguments to the function
   * @param returnType return type of the function
   * @param funcName name of the function
   * @param linkName name of the link (e.g. cartridge)
   */
  
  public FuncExpr(int fnId, Expr[] params, Datatype returnType,
                  String funcName, String linkName)
  {
    this.fnId      = fnId;
    this.params    = params;
    this.dt        = returnType;
    this.funcName  = funcName;
    this.cartridgeLinkName = linkName;
  }
  
  public ExprType getExprType() 
  {
    return ExprType.E_FUNC_EXPR;
  }

  public Datatype getReturnType() 
  {
    return dt;
  }

  /**
   * Get the function internal identifier
   * @return the function internal identifier
   */
  public int getFunctionId() 
  {
    return fnId;
  }

  /**
   * Get number of parameters
   * @return number of parameters
   */
  public int getNumParams() 
  {
    if (params == null)
      return 0;

    return params.length;
  }

  /**
   * Get the array of parameters
   * @return the array of parameters
   */
  public Expr[] getParams()
  {
    return params;
  }
  
  /**
   * Get the function name
   * @return function name
   */
  public String getFuncName()
  {
    return this.funcName;
  }
  
  /**
   * Get the cartridge link name
   * @return link name
   */
  public String getCartridgeLinkName()
  {
    return this.cartridgeLinkName;
  }
  
  public void getAllReferencedAggrs(List<AggrExpr> aggrs)
  {
    for(int i=0; i < getNumParams(); i++)
      params[i].getAllReferencedAggrs(aggrs);   
  }
  
  public Expr getRewrittenExprForGroupBy(Expr[] gbyExprs)
  {
    //check this expression is a group by expression
    for (int i=0; i < gbyExprs.length; i++)
    {
      if (this.equals(gbyExprs[i])) 
        return new GroupByExpr(this);
    }
    
    //check whether expressions are in the gby expr
    Expr exprs[] = new Expr[getNumParams()];
    for (int i=0; i < getNumParams(); i++)
    {
      Expr expr = params[i].getRewrittenExprForGroupBy(gbyExprs);
      if (expr != null)
        exprs[i] = expr;
      else
        return null; // at least one of the params did not match      
    }
    FuncExpr fexpr = new FuncExpr(this.fnId, exprs, this.dt,
                       this.getFuncName(), this.cartridgeLinkName);
    fexpr.setFuncImpl(this.getFuncImpl()); // no constructor that takes impl?
    fexpr.setName(this.getName(), this.isUserSpecifiedName(), 
                                                    this.isExternal());
    fexpr.setAlias(this.getAlias());
    fexpr.setbNull(this.isNull());
    return fexpr;
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type)
  {
    getAllReferencedAttrs(attrs, type, true);
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type,
      boolean includeAggrParams)
  {
    for(int i = 0; i < getNumParams(); i++)
    {
      params[i].getAllReferencedAttrs(attrs, type, includeAggrParams);
    }
  }
  
  public boolean equals(Object otherObject)
  {
    if (this == otherObject)
      return true;
  
    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    FuncExpr other = (FuncExpr) otherObject;
    
    // A function is identified first by its name and its schema (e.g. link), 
    //  and then, if these are not available, by its id.
    if ((funcName != null) || (other.funcName != null))
    {
      if ((funcName == null) || (!funcName.equals(other.funcName)))
        return false;
      
      if ((cartridgeLinkName != null) || (other.cartridgeLinkName != null))
        if ((cartridgeLinkName == null) || (!cartridgeLinkName.equals(other.cartridgeLinkName)))
          return false;
    }
    else
    {
      if (fnId != other.getFunctionId())
        return false;
    }
    
    if(params.length != other.getParams().length)
      return false;
    
    for(int i = 0; i < getNumParams(); i++) {
      if(!params[i].equals(other.getParams()[i]))
        return false;
    }
    
    // No need to compare funcImpl.
    
    return true;
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<FuncExpr fnId =\"" + fnId + "\" >");
    for (int i=0; i<getNumParams(); i++) {
      sb.append("<Parameter>" + params[i].toString() + "</Parameter>");
    }
    sb.append("</FuncExpr>");

    return sb.toString();
  }

  public UserDefinedFunction getFuncImpl()
  {
    return funcImpl;
  }

  public void setFuncImpl(UserDefinedFunction funcImpl)
  {
    this.funcImpl = funcImpl;
  }


}
