/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprCaseComparison.java /main/7 2015/05/19 04:04:41 udeshmuk Exp $ */

/* Copyright (c) 2007, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    06/20/11 - support getSQLEquivalent
    udeshmuk    11/08/09 - API to get all referenced attrs
    sborah      04/20/09 - define getSignature
    rkomurav    06/18/07 - cleanup
    parujain    04/04/07 - Case Comparison Expression
    parujain    04/04/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprCaseComparison.java /main/7 2015/05/19 04:04:41 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;

public class ExprCaseComparison extends Expr{

  private Expr comparisonExpr;
  
  private Expr resultExpr;
  
  public ExprCaseComparison(Expr comp, Expr res, Datatype dt)
  {
    super(ExprKind.CASE_COMPARISON);
    setType(dt);
    this.comparisonExpr = comp;
    this.resultExpr     = res;
  }
  
  public Expr getResultExpr()
  {
    return resultExpr;
  }
  
  public Expr getComparisonExpr()
  {
    return comparisonExpr;
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * 
   * @return 
   *      A concise String representation of the Case Comparison Expression.
   */
   public String getSignature()
   {
     StringBuilder regExpression = new StringBuilder();
     regExpression.append(" when{");
     
     // handle the comparison node 
     regExpression.append(this.getComparisonExpr().getSignature());
     
     regExpression.append("}then{");
     
     // handle the result expression if it exists.
     if(this.getResultExpr() != null)
       regExpression.append(this.getResultExpr().getSignature());
     else
       regExpression.append("null");
     
     regExpression.append("}");
     
     return regExpression.toString();
   }
  
  @Override
  public boolean equals(Object otherObject) {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    ExprCaseComparison other = (ExprCaseComparison)otherObject;
    if(other.resultExpr != null)
    {
      return(other.comparisonExpr.equals(this.comparisonExpr) && (other.resultExpr.equals(this.resultExpr)));
    }
    else
    {
      if(resultExpr != null)
        return false;
      
      return(other.comparisonExpr.equals(this.comparisonExpr));
    }
   
  }

//toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<PhysicalOperatorCaseComparisonExpression>");
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
    sb.append("</PhysicalOperatorCaseComparisonExpression>");
    return sb.toString();
  }
  
  @Override
  public String getXMLPlan2() {
    StringBuilder xml = new StringBuilder();
    xml.append(comparisonExpr.getXMLPlan2());
    if(resultExpr!=null)
      xml.append(resultExpr.getXMLPlan2());
    return xml.toString();
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    comparisonExpr.getAllReferencedAttrs(attrs);
    if(resultExpr != null)
      resultExpr.getAllReferencedAttrs(attrs);
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  {
    StringBuilder regExpression = new StringBuilder();
    regExpression.append(" when (");
    String temp = this.getComparisonExpr().getSQLEquivalent(ec);
    if(temp == null)
      return temp;   
   
    // handle the comparison node 
    regExpression.append(temp);
    
    regExpression.append(") then (");
    
    // handle the result expression if it exists.
    if(this.getResultExpr() != null)
    {
      temp = this.getResultExpr().getSQLEquivalent(ec);
      if(temp == null)
        return temp;
      regExpression.append(temp);
    }
    else
      regExpression.append("null");
    
    regExpression.append(") ");
    
    return regExpression.toString();
  }
  
}
