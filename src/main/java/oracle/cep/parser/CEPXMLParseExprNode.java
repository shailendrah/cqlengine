/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPXMLParseExprNode.java /main/5 2011/05/19 15:28:46 hopark Exp $ */

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
    parujain    08/15/08 - error offset
    skmishra    06/05/08 - cleanup
    skmishra    05/05/08 - Creation
 */

package oracle.cep.parser;

import oracle.cep.common.XMLParseKind;
import oracle.cep.semantic.Expr;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPXMLParseExprNode.java /main/4 2009/02/23 00:45:57 skmishra Exp $
 *  @author  skmishra
 *  @since   release specific (what release of product did this appear in)
 */

public class CEPXMLParseExprNode extends CEPExprNode
{
  
  CEPExprNode value;
  boolean isWellFormed;
  XMLParseKind kind;
  
  public CEPXMLParseExprNode(CEPExprNode _value, boolean isWellFormed, boolean isContent)
  {
    super();
    this.value = _value;
    this.isWellFormed = isWellFormed;
    if(isContent)
      this.kind = XMLParseKind.CONTENT;
    else 
      this.kind = XMLParseKind.DOCUMENT;
    setStartOffset(_value.getStartOffset());
    setEndOffset(_value.getEndOffset());
  }

  public CEPExprNode getValue()
  {
    return value;
  }

  public void setValue(CEPExprNode _value)
  {
    this.value = _value;
  }

  public boolean isWellFormed()
  {
    return isWellFormed;
  }

  public void setWellFormed(boolean isWellFormed)
  {
    this.isWellFormed = isWellFormed;
  }

  public XMLParseKind getKind()
  {
    return kind;
  }

  public void setKind(XMLParseKind k)
  {
    this.kind = k;
  }

  public String toString()
  {
    return " XMLPARSE (" + value.toString() +") ";
  }

  public String getExpression()
  {
    return toString();
  }
}
