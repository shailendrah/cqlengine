/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprCaseCondition.java /main/6 2011/07/09 08:53:44 udeshmuk Exp $ */

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
    udeshmuk    06/20/11 - support getSQLEquivalent
    udeshmuk    11/08/09 - API to get all referenced attrs
    sborah      04/20/09 - define getSignature
    rkomurav    06/18/07 - cleanup
    parujain    03/30/07 - Case Condition expression
    parujain    03/30/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprCaseCondition.java /main/4 2009/12/03 21:27:59 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;

public class ExprCaseCondition extends Expr{
  
  private BoolExpr conditionExpr;
  
  private Expr resultExpr;
  
  public ExprCaseCondition(BoolExpr conditionExpr, Expr resultExpr,
      Datatype dt)
  {
    super(ExprKind.CASE_CONDITION);
    setType(dt);
    
    this.conditionExpr = conditionExpr;
    this.resultExpr    = resultExpr;
  }
  
  public BoolExpr getConditionExpr()
  {
    return conditionExpr;
  }
  
  public Expr getResultExpr()
  {
    return resultExpr;
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * 
   * @return 
   *      A concise String representation of the Case Condiion Expression.
   */
   public String getSignature()
   {
     StringBuilder regExpression = new StringBuilder();
     
     regExpression.append(" when{");
     
     // The condition node can either be complex boolean or base boolean
     regExpression.append(this.getConditionExpr().getSignature());
          
     regExpression.append("}then{");
     
     // add the then clause if it exists
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
    
    ExprCaseCondition other = (ExprCaseCondition)otherObject;
    if(other.resultExpr != null)
    {
      return(other.conditionExpr.equals(this.conditionExpr)&&other.resultExpr.equals(this.resultExpr));
    }
    else
    {
      if(resultExpr != null)
        return false;
      return(other.conditionExpr.equals(this.conditionExpr));
      
    }
    
  }

//toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<PhysicalOperatorCaseConditionExpression>");
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
    sb.append("</PhysicalOperatorCaseConditionExpression>");
    return sb.toString();
  }
  
  @Override
  public String getXMLPlan2() {
    StringBuilder xml = new StringBuilder();
    xml.append(conditionExpr.getXMLPlan2());
    if(resultExpr!=null)
      xml.append(resultExpr.getXMLPlan2());
    return xml.toString();
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    conditionExpr.getAllReferencedAttrs(attrs);
    if(resultExpr != null)
      resultExpr.getAllReferencedAttrs(attrs);
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  {
    StringBuilder regExpression = new StringBuilder();
    
    regExpression.append(" when (");
    
    String temp = this.getConditionExpr().getSQLEquivalent(ec);
    if(temp == null)
      return null;
    
    // The condition node can either be complex boolean or base boolean
    regExpression.append(temp);
         
    regExpression.append(") then (");
    
    // add the then clause if it exists
    if(this.getResultExpr() != null)
    {
      temp = this.getResultExpr().getSQLEquivalent(ec);
      if(temp == null)
        return null;
      regExpression.append(temp);
    }
    else
      regExpression.append("null");
    
    regExpression.append(") ");
    
    return regExpression.toString();
  }
  
}
