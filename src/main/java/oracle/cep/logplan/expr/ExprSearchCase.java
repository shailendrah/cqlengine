/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprSearchCase.java /main/3 2011/05/17 03:26:06 anasrini Exp $ */

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
    parujain    03/29/07 - Search Case Expression
    parujain    03/29/07 - Creation
 */

/**
 *  @version $Header: ExprSearchCase.java 03-jun-2008.13:32:32 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrUnNamed;

public class ExprSearchCase extends Expr implements Cloneable {
  
  ExprCaseCondition[] conditions;

  Expr elseExpr;
  
  public ExprSearchCase(ExprCaseCondition[] conds, Expr expr, Datatype type)
  {
    setType(type);
    this.conditions = conds;
    this.elseExpr = expr;
  }
  
  public int getNumConditions()
  {
    return conditions.length;
  }
  
  public Expr getElseExpr()
  {
    return elseExpr;
  }
  
  public ExprCaseCondition[] getCaseConditions()
  {
    return conditions;
  }
  
  public Attr getAttr() {
    AttrUnNamed attr = new AttrUnNamed(getType());    
    return (Attr)attr;
  }
  
  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    getAllReferencedAttrs(attrs, true);
  }
  
  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, boolean includeAggrParams)
  {
    for(int i = 0; i < getNumConditions(); i++)
    {
      conditions[i].getAllReferencedAttrs(attrs,includeAggrParams);
    }
    if(elseExpr != null)
      elseExpr.getAllReferencedAttrs(attrs, includeAggrParams);
  }
  
  @Override
  public boolean equals(Object otherObject) {
    if(this == otherObject)
      return true;
    if(otherObject == null)
      return false;
    if(getClass() != otherObject.getClass())
      return false;
    ExprSearchCase other = (ExprSearchCase)otherObject;
    if(other.conditions.length != this.conditions.length)
      return false;
    for(int i=0; i<conditions.length; i++)
    {
      if(!conditions[i].equals(other.conditions[i]))
        return false;
    }
    if(other.elseExpr != null)
    {
      return(other.elseExpr.equals(this.elseExpr));
    }
    else
    {
      return(elseExpr == null);
    }
  }
  
   public boolean check_reference(LogOpt op) {
     for(int i=0; i<conditions.length; i++)
     {
       if(!conditions[i].check_reference(op))
         return false;
     }
     if(elseExpr != null)
       return elseExpr.check_reference(op);

     return true;
   }
  
//toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<LogicalSearchCaseExpression>");
    sb.append(super.toString());
    sb.append("<CASE>");
    for(int i=0; i<conditions.length; i++)
      sb.append(conditions[i].toString());
  
    sb.append("<ELSE>");
     if(elseExpr != null)
       sb.append(elseExpr.toString());
     else
       sb.append("null");
    sb.append("</ELSE>");
    sb.append("</CASE>");
    sb.append("</LogicalSearchCaseExpression>");
    return sb.toString();
  }
  
}
