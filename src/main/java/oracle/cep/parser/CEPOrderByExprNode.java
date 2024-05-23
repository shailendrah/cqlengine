/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPOrderByExprNode.java /main/7 2011/05/19 15:28:46 hopark Exp $ */

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
    skmishra    03/12/09 - adding order-by-attr to xml
    skmishra    02/20/09 - adding toExprXml
    mthatte     01/30/09 - override toVisualizerString()
    parujain    08/15/08 - error offset
    mthatte     04/07/08 - adding toString()
    parujain    06/22/07 - Order by Expr
    parujain    06/22/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPOrderByExprNode.java /main/6 2009/03/17 23:10:41 skmishra Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.parser;

import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;

public class CEPOrderByExprNode extends CEPExprNode{
  
  protected CEPExprNode orderByExpr;
  
  protected boolean isNullsFirst;
  
  protected boolean isAsc;
  
  /**
   * Constructor for CEPOrderByExprNode
   * 
   * @param expr
   *           order by expression(can be attribute, const etc)
   * @param isasc
   *           Ascending or Descending
   * @param isfirst
   *           Nulls first or last
   */
  public CEPOrderByExprNode(CEPExprNode expr, Boolean isasc, Boolean isfirst)
  {
    this.orderByExpr = expr;
    
    this.isAsc = isasc.booleanValue();
    
    this.isNullsFirst = isfirst.booleanValue();
    
    setStartOffset(expr.getStartOffset());
    
    setEndOffset(expr.getEndOffset());
  }
  
  public CEPExprNode getOrderByExpr()
  {
    return this.orderByExpr;
  }
  
  public boolean getIsNullsFirst()
  {
    return this.isNullsFirst;
  }
  
  public boolean isAscending()
  {
    return this.isAsc;
  }
  
  public String toString()
  {
      StringBuilder myString = new StringBuilder(10);
      myString.append(orderByExpr.toString());
      if(!isAsc)
        myString.append(" desc ");
      if(isNullsFirst)
        myString.append(" nulls first ");
      if(alias != null)
        myString.append(" AS " + alias);
      return myString.toString();
  }
  
  public String toVisualizerXML()
  {
    StringBuilder s = new StringBuilder(25);
    s.append("\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.orderBySymbolTag, orderByExpr.toString(), null, null));
    s.append("\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.ascendingTag, String.valueOf(isAsc), null, null));
    s.append("\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.nullsFirstTag, String.valueOf(isNullsFirst), null, null));
    return XMLHelper.buildElement(true, VisXMLHelper.orderByAttrTag, s.toString(), null, null);
  }

  @Override
  public String getExpression()
  {
    StringBuilder myString = new StringBuilder(10);
    myString.append(orderByExpr.toString());
    if(!isAsc)
      myString.append(" desc ");
    if(isNullsFirst)
      myString.append(" nulls first ");
    return myString.toString();
  }
}
