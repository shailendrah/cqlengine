/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprCaseCondition.java /main/3 2011/05/17 03:26:06 anasrini Exp $ */

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
    sborah      04/11/11 - override getAllReferencedAttrs()
    parujain    06/03/08 - check_reference
    parujain    03/29/07 - Case Condition Expression
    parujain    03/29/07 - Creation
 */

/**
 *  @version $Header: ExprCaseCondition.java 03-jun-2008.13:32:05 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;

public class ExprCaseCondition extends Expr implements Cloneable {

  BoolExpr conditionExpr;
  
  Expr  resultExpr;
  
  public ExprCaseCondition(BoolExpr cond, Expr res, Datatype rettype)
  {
    this.conditionExpr = cond;
    this.resultExpr = res;
    setType(rettype);
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    getAllReferencedAttrs(attrs,true);
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs, boolean includeAggrParams)
  {
    conditionExpr.getAllReferencedAttrs(attrs, includeAggrParams);
    if(resultExpr != null)
      resultExpr.getAllReferencedAttrs(attrs, includeAggrParams);
  }
  
  @Override
  public boolean equals(Object otherObject) {
    if(this == otherObject)
      return true;
    if(otherObject == null)
      return false;
    if(getClass() != otherObject.getClass())
      return false;
    ExprCaseCondition other = (ExprCaseCondition)otherObject;
    if(other.resultExpr != null)
    {
      return(this.conditionExpr.equals(other.conditionExpr) && other.resultExpr.equals(this.resultExpr));
    }
    else
    {
      return(this.conditionExpr.equals(other.conditionExpr)&& (this.resultExpr == null));
    }
    
  }
  
  public boolean check_reference(LogOpt op) {
    if(resultExpr != null)
    {
       if(!resultExpr.check_reference(op))
         return false;
    }
    return conditionExpr.check_reference(op);
  }

  public BoolExpr getConditionExpr()
  {
    return conditionExpr;
  }
  
  public Expr getResultExpr()
  {
    return resultExpr;
  }
  
//toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<LogicalCaseConditionExpression>");
    sb.append(super.toString());
    sb.append("<WHEN>");
    sb.append(conditionExpr.toString());
    sb.append("</WHEN>");
    sb.append("<THEN>");
    if(resultExpr != null)
      sb.append(resultExpr.toString());
    else
      sb.append("null");
    sb.append("</THEN>");
    sb.append("</LogicalCaseConditionExpression>");
    return sb.toString();
  }
    
}
