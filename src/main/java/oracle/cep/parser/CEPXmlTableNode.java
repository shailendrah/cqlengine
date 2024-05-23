/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPXmlTableNode.java /main/4 2011/05/19 15:28:46 hopark Exp $ */

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
    mthatte     06/18/09 - adding xmlnamespace support
    parujain    08/11/08 - error offset
    mthatte     12/26/07 - 
    najain      12/03/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPXmlTableNode.java /main/3 2009/06/23 14:09:07 mthatte Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import java.util.List;

/**
 * The parse tree node corresponding to xmltable specification
 *
 * @since 1.0
 */

public class CEPXmlTableNode implements CEPParseTreeNode 
{
  String                          xqryStr;
  List<CEPXQryArgExprNode>        xqryArgs;
  List<CEPXmlTableColumnNode>     listCols;
  List<CEPXmlNamespaceNode>		  namespaces;	
  protected int startOffset;
  protected int endOffset;

  public CEPXmlTableNode(CEPStringTokenNode          xqryStr, 
			 List<CEPXQryArgExprNode>    xqryArgs, 
			 List<CEPXmlTableColumnNode> listCols)
  {
    this.xqryStr  = xqryStr.getValue();
    this.xqryArgs = xqryArgs;
    this.listCols = listCols;
    this.namespaces = null;
    setStartOffset(xqryStr.getStartOffset());
    if(!listCols.isEmpty())
      setEndOffset(listCols.get(listCols.size()-1).getEndOffset());
    else if(!xqryArgs.isEmpty())
      setEndOffset(xqryArgs.get(xqryArgs.size()-1).getEndOffset());
    else
      setEndOffset(xqryStr.getEndOffset());
  }
  
  public CEPXmlTableNode(CEPStringTokenNode          xqryStr, 
			 List<CEPXQryArgExprNode>    xqryArgs, 
			 List<CEPXmlTableColumnNode> listCols, List<CEPXmlNamespaceNode> namespaces)
  {
	  this.xqryStr  = xqryStr.getValue();
	  this.xqryArgs = xqryArgs;
	  this.listCols = listCols;
	  this.namespaces = namespaces;
	  
	  if(namespaces == null | namespaces.size() == 0)
		  setStartOffset(xqryStr.getStartOffset());
	  else
		  setStartOffset(namespaces.get(0).getStartOffset());
	  
	  if(!listCols.isEmpty())
		  setEndOffset(listCols.get(listCols.size()-1).getEndOffset());
	  else if(!xqryArgs.isEmpty())
		  setEndOffset(xqryArgs.get(xqryArgs.size()-1).getEndOffset());
	  else
		  setEndOffset(xqryStr.getEndOffset());
  }
  

  public String getXQryStr()
  {
    return xqryStr;
  }

  public List<CEPXmlTableColumnNode> getListCols()
  {
    return listCols;
  }

  public List<CEPXQryArgExprNode> getXQryArgs()
  {
    return xqryArgs;
  }
  
  
  public List<CEPXmlNamespaceNode> getNamespaces() 
  {
	return namespaces;
  }

/**
   * Sets startoffset corresponding to ddl
   */
  public void setStartOffset(int start)
  {
    this.startOffset = start;
  }
  
  /**
   * Gets the start offset
   */
  public int getStartOffset()
  {
    return this.startOffset;
  }
  
  /**
   * Sets the EndOffset corresponding to DDL
   */
  public void setEndOffset(int end)
  {
    this.endOffset = end;
  }
  
  /**
   * Gets the endoffset
   */
  public int getEndOffset()
  {
    return this.endOffset;
  }

  public void setXqryStr(String xqryStr) 
  {
	this.xqryStr = xqryStr;
  }
  
  
  
}
