/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprCaseComparison.java /main/3 2011/05/17 03:26:06 anasrini Exp $ */

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
    parujain    04/04/07 - Comparison Expression
    parujain    04/04/07 - Creation
 */

/**
 *  @version $Header: ExprCaseComparison.java 03-jun-2008.13:31:58 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;

public class ExprCaseComparison extends Expr implements Cloneable{
  
  Expr comparisonExpr;
  
  Expr resultExpr;
  
  public ExprCaseComparison(Expr comp, Expr res, Datatype dt)
  {
    this.comparisonExpr = comp;
    this.resultExpr = res;
    setType(dt);
  }
  
  public Expr getComparisonExpr()
  {
    return comparisonExpr;
  }
  
  public Expr getResultExpr()
  {
    return resultExpr;
  }
  
  public boolean check_reference(LogOpt op) {
    if(resultExpr != null)
    {
      if(!resultExpr.check_reference(op))
        return false;
    } 
    return comparisonExpr.check_reference(op);
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    getAllReferencedAttrs(attrs, true);
  }
  
  public void getAllReferencedAttrs(List<Attr> attrs, boolean includeAggrParams)
  {
    comparisonExpr.getAllReferencedAttrs(attrs, includeAggrParams);
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
    
    ExprCaseComparison other = (ExprCaseComparison)otherObject;
    if(other.resultExpr != null)
    {
      return(this.comparisonExpr.equals(this.comparisonExpr) && (other.resultExpr.equals(this.resultExpr)));
    }
    else
    {
      return(this.comparisonExpr.equals(this.comparisonExpr) && (this.resultExpr == null));
    }
  }
  
//toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<LogicalCaseComparisonExpression>");
    sb.append(super.toString());
    sb.append("<WHEN>");
    sb.append(comparisonExpr.toString());
    sb.append("</WHEN>");
    sb.append("<THEN>");
    if(resultExpr != null)
      sb.append(resultExpr.toString());
    else
      sb.append("null");
    sb.append("</THEN>");
    sb.append("</LogicalCaseComparisonExpression>");
    return sb.toString();
  }
  
}
