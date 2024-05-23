/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/GroupByExpr.java /main/1 2012/05/02 03:06:03 pkali Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    02/06/12 - ,
    vikshukl    01/30/12 - GROUP BY expression in select list
    vikshukl    01/30/12 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/GroupByExpr.java /main/1 2012/05/02 03:06:03 pkali Exp $
 *  @author  vikshukl
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import java.util.List;

import oracle.cep.common.Datatype;

public class GroupByExpr extends Expr 
{
  private Expr expr;         // just a wrapper around expr
  
  public GroupByExpr(Expr expr) 
  {
    super();
    this.expr = expr;
    this.setName(expr.getName(), false);
  }

  public Expr getExpr() 
  {
    return expr;
  }

  public void setExpr(Expr expr) 
  {
    this.expr = expr;
  }

  public ExprType getExprType() 
  {
    return ExprType.E_GROUP_BY_EXPR;
  }

  public String getName() 
  {
    return expr.getName();
  }

  public Datatype getReturnType() 
  {
    return expr.getReturnType();
  }
  
  public Datatype getType()
  {
    return expr.getReturnType();
  }

  public void getAllReferencedAggrs(List<AggrExpr> aggrs) 
  {
    expr.getAllReferencedAggrs(aggrs);   
  }

  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type) 
  {
    expr.getAllReferencedAttrs(attrs, type);    
  }

  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type,
                                    boolean includeAggrParams) 
  {
    expr.getAllReferencedAttrs(attrs, type, includeAggrParams);    
  }

  public boolean equals(Object otherObject) 
  {
    // TODO: Verify and double check
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    GroupByExpr other = (GroupByExpr) otherObject;
    return this.getExpr().equals(other.getExpr());
  }
}
