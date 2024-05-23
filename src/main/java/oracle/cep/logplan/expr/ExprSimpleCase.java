/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprSimpleCase.java /main/3 2011/05/17 03:26:06 anasrini Exp $ */

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
    parujain    04/04/07 - Simple CASE Expression
    parujain    04/04/07 - Creation
 */

/**
 *  @version $Header: ExprSimpleCase.java 03-jun-2008.13:32:20 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrUnNamed;

public class ExprSimpleCase extends Expr implements Cloneable {

  Expr  compExpr;
  
  ExprCaseComparison[] comparisonExprs;
  
  Expr elseExpr;
  
  public ExprSimpleCase(Expr comp, ExprCaseComparison[] comps, Expr expr, Datatype dt)
  {
    setType(dt);
    this.compExpr = comp;
    this.comparisonExprs = comps;
    this.elseExpr = expr;
  }
  
  public Expr getCompExpr()
  {
    return compExpr;  
  }
  
  public ExprCaseComparison[] getComparisonExprs()
  {
    return comparisonExprs;
  }
  
  public int getNumComparisons()
  {
    return comparisonExprs.length;
  }
  
  public Expr getElseExpr()
  {
    return elseExpr;
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
    compExpr.getAllReferencedAttrs(attrs,includeAggrParams);
    for(int i=0; i< comparisonExprs.length; i++)
    {
      comparisonExprs[i].getAllReferencedAttrs(attrs, includeAggrParams);
    }
    if(elseExpr != null)
    {
      elseExpr.getAllReferencedAttrs(attrs, includeAggrParams);
    }
  }
  
  @Override
  public boolean equals(Object otherObject) {
    if(this == otherObject)
      return true;
    if(otherObject == null)
      return false;
    if(getClass() != otherObject.getClass())
      return false;
    
    ExprSimpleCase other = (ExprSimpleCase)otherObject;
    if(this.getNumComparisons() != other.getNumComparisons())
      return false;
    
    if(!other.compExpr.equals(other.compExpr))
      return false;
    for(int i=0; i< comparisonExprs.length; i++)
    {
      if(!comparisonExprs[i].equals(other.getComparisonExprs()[i]))
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
    if(!compExpr.check_reference(op))
      return false;
    for(int i=0; i<comparisonExprs.length; i++)
    {
      if(!comparisonExprs[i].check_reference(op))
        return false;
    }
    if(elseExpr != null)
    {
       return elseExpr.check_reference(op);
    }
    return true;
  }
  
//toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<LogicalSimpleCaseExpression>");
    sb.append(super.toString());
    sb.append("<CASE>");
    sb.append(compExpr.toString());
    for(int i=0; i<comparisonExprs.length; i++)
      sb.append(comparisonExprs[i].toString());
  
    sb.append("<ELSE>");
     if(elseExpr != null)
       sb.append(elseExpr.toString());
     else
       sb.append("null");
    sb.append("</ELSE>");
    sb.append("</CASE>");
    sb.append("</LogicalSimpleCaseExpression>");
    return sb.toString();
  }
  
}
