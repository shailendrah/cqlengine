/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPSearchedCaseExprNode.java /main/8 2011/05/19 15:28:46 hopark Exp $ */

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
    udeshmuk    01/11/08 - eliminate constructor specific to null else clause.
    sbishnoi    06/07/07 - fix xlint warning
    parujain    03/27/07 - Searched Case Expression
    parujain    03/27/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPSearchedCaseExprNode.java /main/7 2009/02/23 00:45:57 skmishra Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.parser;

import java.util.List;

public class CEPSearchedCaseExprNode extends CEPExprNode {
  protected CEPCaseConditionExprNode[] caseExprs;
  
  protected CEPExprNode   elseExpr;
  
  public CEPSearchedCaseExprNode()
  {
  }

  public CEPSearchedCaseExprNode(List<CEPCaseConditionExprNode> exprs, CEPExprNode node)
  {
    constructParams(exprs, node);
  }

  protected void constructParams(List<CEPCaseConditionExprNode> exprs, CEPExprNode node) {
    setStartOffset(exprs.get(0).getStartOffset());
    setEndOffset(exprs.get(exprs.size()-1).getEndOffset());
    if (node instanceof CEPNullConstExprNode)
      this.elseExpr = null;
    else
    {
      this.elseExpr = node;
      setEndOffset(node.getEndOffset());
    }
    this.caseExprs = (CEPCaseConditionExprNode[])exprs.toArray(new CEPCaseConditionExprNode[0]);
  }

  public CEPCaseConditionExprNode[] getCaseExprs()
  {
    return caseExprs;
  }
  
  public CEPExprNode getElseExpr()
  {
    return elseExpr;
  }
  
  public boolean isElse()
  {
    if(elseExpr == null)
      return false;
    
    return true;
  }
  
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append(" case ");
    for(CEPCaseConditionExprNode expr: caseExprs)
      sb.append(expr.toString());
    if(elseExpr!=null) {
      sb.append(" else ");
      sb.append(elseExpr.toString());
    }
    sb.append(" end ");
    if(alias != null)
      sb.append(" AS " + alias);
    return sb.toString();
  }

  @Override
  public String getExpression()
  {
    StringBuffer sb = new StringBuffer();
    sb.append(" case ");
    for(CEPCaseConditionExprNode expr: caseExprs)
      sb.append(expr.toString());
    if(elseExpr!=null) {
      sb.append(" else ");
      sb.append(elseExpr.toString());
    }
    sb.append(" end ");
    return sb.toString();
  }
}
