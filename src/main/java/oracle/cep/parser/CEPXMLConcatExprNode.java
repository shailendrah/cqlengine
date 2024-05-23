

/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPXMLConcatExprNode.java /main/5 2011/05/19 15:28:46 hopark Exp $ */

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
    skmishra    05/02/08 - 
    mthatte     04/28/08 - adding toString
    mthatte     04/17/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPXMLConcatExprNode.java /main/4 2009/02/23 00:45:57 skmishra Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.parser;

import java.util.List;

public class CEPXMLConcatExprNode extends CEPExprNode
{
  /** A list of expressions to be concatenated. 
   *  Each expr must be of xmltype, to be validated by Semantic layer. 
   */
  
  CEPExprNode[] concatExprList;

  public CEPXMLConcatExprNode(List<CEPExprNode> concatExprList)
  {
    super();
    this.concatExprList = (CEPExprNode[])concatExprList.toArray(new CEPExprNode[0]);
    setStartOffset(concatExprList.get(0).getStartOffset());
    setEndOffset(concatExprList.get(concatExprList.size()-1).getEndOffset());
  }

  public CEPExprNode[] getConcatExprList()
  {
    return concatExprList;
  }

  public void setConcatExprList(CEPExprNode[] concatExprList)
  {
    this.concatExprList = concatExprList;
    setStartOffset(concatExprList[0].getStartOffset());
    setEndOffset(concatExprList[concatExprList.length-1].getEndOffset());
  }
  
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append(" XMLCONCAT ( ");
    for(CEPExprNode e:concatExprList)
    {
      sb.append(e.toString());
    }
    sb.append(") ");
    return sb.toString();
  }

  @Override
  public String getExpression()
  {
    return toString();
  }
}