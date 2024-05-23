/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPElementExprNode.java /main/7 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2008, 2011, Oracle and/or its affiliates. 
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
    skmishra    06/03/09 - adding isXmlType()
    skmishra    02/20/09 - adding toExprXml
    skmishra    02/13/09 - adding alias to toString
    parujain    08/14/08 - error offset
    parujain    04/21/08 - element expr node
    parujain    04/21/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPElementExprNode.java /main/6 2009/06/04 17:52:35 sborah Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.parser;

import java.util.List;

public class CEPElementExprNode extends CEPExprNode {
	
  protected String elementName;

  protected CEPExprNode elementNameExpr;
  
  protected CEPExprNode[] attrList;
  
  protected CEPExprNode[] exprList;
  
  public CEPElementExprNode(CEPStringTokenNode nameToken, List<CEPExprNode> attrs, List<CEPExprNode> exprs)
  {
    this.elementName = nameToken.getValue();
    setStartOffset(nameToken.getStartOffset());
    setEndOffset(nameToken.getEndOffset());
    this.elementNameExpr = null;
    if(attrs != null)
    {
      this.attrList = (CEPExprNode[])attrs.toArray(new CEPExprNode[0]);
      setEndOffset(attrs.get(attrs.size()-1).getEndOffset());
    }
    else
      this.attrList = null;
    if(exprs != null)
    {
      this.exprList = (CEPExprNode[])exprs.toArray(new CEPExprNode[0]);
      setEndOffset(exprs.get(exprs.size()-1).getEndOffset());
    }
    else
      this.exprList = null;
  }
  
  public CEPElementExprNode(CEPExprNode nameExpr, List<CEPExprNode> attrs, List<CEPExprNode> exprs)
  {
    this.elementName = null;
    this.elementNameExpr = nameExpr;
    setStartOffset(nameExpr.getStartOffset());
    setEndOffset(nameExpr.getEndOffset());
    if(attrs != null)
    {
      this.attrList = (CEPExprNode[])attrs.toArray(new CEPExprNode[0]);
      setEndOffset(attrs.get(attrs.size()-1).getEndOffset());
    }
    else
      this.attrList = null;
    if(exprs != null)
    {
      this.exprList = (CEPExprNode[])exprs.toArray(new CEPExprNode[0]);
      setEndOffset(exprs.get(exprs.size()-1).getEndOffset());
    }
    else
      this.exprList = null;
  }

  public String getElementName()
  {
    return elementName;
  }

  public CEPExprNode getElementNameExpr()
  {
    return elementNameExpr;
  }
  
  public CEPExprNode[] getAttrList()
  {
    return attrList;
  }
  
  public CEPExprNode[] getExprList()
  {
    return exprList;
  }
  
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(" " + "XMLELEMENT" + "( ");
    if(elementName != null)
      sb.append("\"" + elementName + "\"");
    else
      sb.append("NAME" + elementNameExpr.toString());
    if(attrList != null)
    {
      sb.append("," + "XMLATTRIBUTES" + " (");
      for(int i=0; i<attrList.length; i++)
      {
        sb.append(attrList[i].toString());
        if(i < (attrList.length-1))
          sb.append(", ");
      }
      sb.append(")");
    }
    if(exprList != null)
    {
      for(int j=0; j<exprList.length; j++)
        sb.append(exprList[j].toString());
    }
    sb.append(")" +" ");
    return sb.toString(); 
  }

  @Override
  public String getExpression()
  {
    throw new UnsupportedOperationException("Not supported for xmlelement clause");
  }
}
