/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprSimpleCase.java /main/6 2011/07/09 08:53:44 udeshmuk Exp $ */

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
    sborah      04/21/09 - override getVariableTypeLength()
    sborah      04/20/09 - define getSignature
    rkomurav    06/18/07 - cleanup
    parujain    04/04/07 - Simple Case Expression
    parujain    04/04/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprSimpleCase.java /main/4 2009/12/03 21:27:59 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;

public class ExprSimpleCase extends Expr{

  private Expr compExpr;
  
  private ExprCaseComparison[] comparisonExprs;
  
  private Expr elseExpr;
  
  public ExprSimpleCase(Expr compExpr, ExprCaseComparison[] comparisonExprs,
      Expr elseExpr, Datatype dt)
  {
    super(ExprKind.SIMPLE_CASE);
    setType(dt);
    
    this.compExpr        = compExpr;
    this.comparisonExprs = comparisonExprs;
    this.elseExpr        = elseExpr;
  }

  public Expr getElseExpr()
  {
    return elseExpr;
  }
  
  public Expr getCompExpr()
  {
    return compExpr;
  }
  
  public int getNumComparisons()
  {
    return comparisonExprs.length;
  }
  
  public ExprCaseComparison[] getComparisons()
  {
    return comparisonExprs;
  }
  
  /**
   * Return the maximum length amongst the lengths of all 
   * its result expressions and its else expression.
   * Min value returned is 1
   * 
   * @return maximum length 
   */
  protected int getVariableTypeLength()
  {
    // minimum value to be returned is 1 in case all result exprs are null
    int maxLength = 1;
    Expr expr;
    int length = 0;
    for(ExprCaseComparison comparison : comparisonExprs)
    {
      expr =  comparison.getResultExpr();
      if(expr != null)
      {
        length = expr.getLength();

        // check and update the maxLength
        if(length > maxLength)
          maxLength = length;
      }
    }

    // finally compare with the else expr if it exists
    if(elseExpr != null)
    {
      length = elseExpr.getLength();

      // check and update the maxLength
      if(length > maxLength)
        maxLength = length;
    }
    
    return maxLength;
  }
  
  
  /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * 
   * @return 
   *      A concise String representation of the Simple case Expression.
   */
   public String getSignature()
   {
     StringBuilder regExpression = new StringBuilder();
     regExpression.append(this.getKind() + " case ");
     
     // handle the comparison expression node 
     regExpression.append(this.getCompExpr().getSignature()+ "{");
     
     // handle all the comparisons recursively
     for(ExprCaseComparison ecc : this.getComparisons())
     {
       if(ecc != null)
         regExpression.append("#" + ecc.getSignature());
       else
         regExpression.append("null");
     }
     
     regExpression.append("}else{");
     
     // handle the else expression if it exists. 
     if(this.getElseExpr() != null)
       regExpression.append(this.getElseExpr().getSignature() + "}");
     else
       regExpression.append("null}");
     
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
    
    ExprSimpleCase other = (ExprSimpleCase)otherObject;
    if(!other.compExpr.equals(this.compExpr))
      return false;
    
    if(this.getNumComparisons() != other.getNumComparisons())
      return false;
    
    ExprCaseComparison[] comps = other.comparisonExprs;
    for(int i=0; i<this.getNumComparisons(); i++)
    {
      if(!comps[i].equals(comparisonExprs[i]))
        return false;
    }
    if(other.elseExpr!=null)
      return(other.elseExpr.equals(this.elseExpr));
    else
      return(this.elseExpr == null);
  }

//toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<PhysicalOperatorSimpleCaseExpression>");
    sb.append(super.toString());
    sb.append("<CASE>");
    sb.append(compExpr.toString());
    for(int i=0; i< getNumComparisons(); i++)
      sb.append(comparisonExprs[i].toString());
    sb.append("<ELSE>");
    if(elseExpr != null)
      sb.append(elseExpr.toString());
    else
      sb.append("null");
    sb.append("</ELSE>");
    sb.append("</CASE>");
    sb.append("</PhysicalOperatorSimpleCaseExpression>");
    return sb.toString(); 
  }
  
  @Override
  public String getXMLPlan2() {
    StringBuilder xml = new StringBuilder();
    for(int i=0; i< getNumComparisons(); i++)
      xml.append(comparisonExprs[i].getXMLPlan2());
    if(elseExpr != null)
      xml.append(elseExpr.getXMLPlan2());
    return xml.toString();
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    compExpr.getAllReferencedAttrs(attrs);
    for(int i=0; i < comparisonExprs.length; i++)
      comparisonExprs[i].getAllReferencedAttrs(attrs);
    if(elseExpr != null)
      elseExpr.getAllReferencedAttrs(attrs);
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  {
    StringBuilder regExpression = new StringBuilder();
    regExpression.append(" case ");
    
    // handle the comparison expression node 
    String temp = this.getCompExpr().getSQLEquivalent(ec);
    if(temp == null)
      return null;
    regExpression.append(temp+ " ");
    
    // handle all the comparisons recursively
    for(ExprCaseComparison ecc : this.getComparisons())
    {
      if(ecc != null)
      {
        temp = ecc.getSQLEquivalent(ec);
        if(temp == null)
          return null;
        regExpression.append(temp + " ");
      }
    }
    
    // handle the else expression if it exists. 
    if(this.getElseExpr() != null)
    {
      regExpression.append(" else ");
      temp = this.getElseExpr().getSQLEquivalent(ec);
      if(temp == null)
        return null;
      regExpression.append(temp + " ");
    }
    
    regExpression.append(" end ");
    
    return regExpression.toString();
  }
  
}
