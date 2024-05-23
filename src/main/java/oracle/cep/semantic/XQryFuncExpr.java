/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/XQryFuncExpr.java /main/4 2012/07/30 19:52:55 pkali Exp $ */

/* Copyright (c) 2007, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       07/17/12 - meta data propagation for rewritten groupby expr
    pkali       05/09/12 - added getRewrittenExprForGroupBy method
    najain      02/07/08 - add length
    mthatte     12/26/07 - 
    najain      10/26/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/XQryFuncExpr.java /main/4 2012/07/30 19:52:55 pkali Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import java.util.List;
import oracle.cep.common.Datatype;

/**
 * Post semantic analysis representation of a function expression.
 *
 * @since 1.0
 */

public class XQryFuncExpr extends Expr {
  /** internal identifier of the function - will always be XMLQUERY */
  private int fnId;

  /** xquery string */
  private String xqryStr;

  /** parameters of the function */
  private Expr[] params;

  /** names to be bound to the xquery string */
  private String[] names;

  // is it xmlquery or xmlexists
  private XQryFuncExprKind xmlQuery;

  // length: optional
  private int length;

  /**
   * Constructor
   */
  public XQryFuncExpr(int fnId, String xqryStr, Expr[] params, String[] names, 
                Datatype ret, int length, XQryFuncExprKind xmlQuery)
  {
    this.fnId = fnId;
    this.xqryStr = xqryStr;
    this.params = params;
    this.names = names;
    this.dt = ret;
    this.length = length;
    this.xmlQuery = xmlQuery;
  }

  /**
   * Get the xquery string
   * @return the xquery string
   */
  public String getXqryStr()
  {
    return xqryStr;
  }

  public int getLength()
  {
	return length;  
  }
  
  /**
   * Get the array of names to be bound
   * @return the array of names
   */
  public String[] getNames()
  {
    return names;
  }

  public int getFunctionId()  
  {
    return fnId;
  }

  public Expr[] getParams()
  {
    return params;
  }

  public int getNumParams() 
  {
    if (params == null)
      return 0;

    return params.length;
  }

  public ExprType getExprType() 
  {
    return ExprType.E_FUNC_EXPR;
  }

  public Datatype getReturnType() 
  {
    return dt;
  }

  public XQryFuncExprKind getXmlQuery()
  {
    return xmlQuery;
  }
  
  public boolean equals(Object otherObject)
  {
    if (this == otherObject)
      return true;
  
    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;

    XQryFuncExpr other = (XQryFuncExpr) otherObject;
    if (fnId != other.getFunctionId())
      return false;
    
    if(params.length != other.getParams().length)
      return false;
    
    for(int i = 0; i < getNumParams(); i++) {
      if(!params[i].equals(other.getParams()[i]))
        return false;
    }

    if (xqryStr != other.xqryStr) 
      return false;

    for(int i = 0; i < getNumParams(); i++) {
      if(!names[i].equals(other.getNames()[i]))
        return false;
    }

    if (xmlQuery != other.xmlQuery)
      return false;
    
    return true;
  }
  
  public void getAllReferencedAggrs(List<AggrExpr> aggrs)
  {
  }


  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type)
  {
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type, 
                                boolean includeAggrParams)
  {
  }
  
  public Expr getRewrittenExprForGroupBy(Expr[] gbyExprs)
  {
    Expr[] rwParamExprs = new Expr[getNumParams()];
    for(int i = 0; i < getNumParams(); i++) 
    {
      Expr rwParamExpr = params[i].getRewrittenExprForGroupBy(gbyExprs);
      if(rwParamExpr == null)
        return null;
      rwParamExprs[i] = rwParamExpr;
    }
    XQryFuncExpr xQryFuncExpr =  new XQryFuncExpr(this.fnId, this.xqryStr, 
                                         rwParamExprs,this.names, this.dt, 
                                         this.length, this.xmlQuery);
    xQryFuncExpr.setName(this.getName(), 
                         this.isUserSpecifiedName(), this.isExternal());
    xQryFuncExpr.setAlias(this.getAlias());
    xQryFuncExpr.setbNull(this.isNull());
    return xQryFuncExpr;
  }
}
