/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPXmlAttrExprNode.java /main/6 2011/05/19 15:28:46 hopark Exp $ */

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
    skmishra    02/20/09 - adding toExprXml
    skmishra    02/13/09 - adding alias to toString
    parujain    08/14/08 - error offset
    parujain    04/21/08 - xml attribute expr node
    parujain    04/21/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPXmlAttrExprNode.java /main/5 2009/02/23 00:45:57 skmishra Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.parser;

public class CEPXmlAttrExprNode extends CEPExprNode {
	
  protected CEPExprNode attrExpr;
  
  protected String tagName;

  protected CEPExprNode tagNameExpr;

  protected boolean isTagExpr;
  
  public CEPXmlAttrExprNode(CEPExprNode attr, CEPStringTokenNode nameToken)
  {
    this.attrExpr = attr;
    this.tagName = nameToken.getValue();
    this.tagNameExpr = null;
    this.isTagExpr = false;
    setStartOffset(attr.getStartOffset());
    setEndOffset(nameToken.getEndOffset());
  }
  
  public CEPXmlAttrExprNode(CEPExprNode attr, CEPExprNode nameExpr)
  {
    this.attrExpr = attr;
    this.tagName = null;
    this.tagNameExpr = nameExpr;
    this.isTagExpr = true;
    setStartOffset(attr.getStartOffset());
    setEndOffset(nameExpr.getEndOffset());
  }
  
  public CEPXmlAttrExprNode(CEPExprNode attr)
  {
    this.attrExpr = attr;
    this.tagName = null;
    this.tagNameExpr = null;
    this.isTagExpr = false;
    setStartOffset(attr.getStartOffset());
    setEndOffset(attr.getEndOffset());
  }

  public boolean isTagNameExpr()
  {
    return this.isTagExpr;
  }

  public CEPExprNode getTagExpr()
  {
    return this.tagNameExpr;
  }

  public CEPExprNode getAttrExpr()
  {
    return this.attrExpr;
  }
  
  public String getTagName()
  {
    return this.tagName;
  }
  
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(" ");
    sb.append(attrExpr.toString());
    if(tagName != null)
    {
      sb.append(" AS ");
      sb.append(tagName + " ");
    }
    if(tagNameExpr != null)
    {
      sb.append(" AS ");
      sb.append(tagNameExpr.toString() + " ");
    }
    return sb.toString();
  }

  @Override
  public String getExpression()
  {
    return toString();
  }
	
}
