/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPCaseComparisonExprNode.java /main/9 2011/05/19 15:28:46 hopark Exp $ */

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
    hopark      04/21/11 - make public to be reused in cqservice
    skmishra    02/20/09 - adding toExprXml
    skmishra    02/13/09 - adding alias to toString
    parujain    08/15/08 - error offset
    mthatte     04/07/08 - adding toString()
    udeshmuk    01/11/08 - remove the constructor with only one argument.
    parujain    04/02/07 - Comparison For Simple CASE expression
    parujain    04/02/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPCaseComparisonExprNode.java /main/8 2009/02/27 14:19:31 skmishra Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.parser;

public class CEPCaseComparisonExprNode extends CEPExprNode{
  
  protected CEPExprNode comparisonExpr;
  
  protected CEPExprNode returnExpr;
  
  public CEPCaseComparisonExprNode(CEPExprNode cond, CEPExprNode ret)
  {
    this.comparisonExpr = cond;
    setStartOffset(cond.getStartOffset());
    setEndOffset(cond.getEndOffset());
    if(ret instanceof CEPNullConstExprNode)
      this.returnExpr = null;
    else 
      this.returnExpr = ret;
    setEndOffset(ret.getEndOffset());
  }
  
  public CEPExprNode getReturnExpr()
  {
    return returnExpr;
  }
  
  public CEPExprNode getComparisonExpr()
  {
    return comparisonExpr;
  }
  
  public String toString()
  {
    if(alias == null)
    {
      if(returnExpr != null)
        return " when " + comparisonExpr.toString() + " then " + returnExpr.toString() + " ";
      else
        return " when " + comparisonExpr.toString() + " then null ";
    }
    else
    {
      if(returnExpr != null)
        return " when " + comparisonExpr.toString() + " then " + returnExpr.toString() + " AS " + alias;
      else
        return " when " + comparisonExpr.toString() + " then null AS " + alias;
    }
  }

  public String getExpression()
  {
    if(returnExpr != null)
      return " when " + comparisonExpr.toString() + " then " + returnExpr.toString() + " ";
    else
      return " when " + comparisonExpr.toString() + " then null ";
  }
}
