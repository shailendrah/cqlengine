/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPXmlNamespaceNode.java /main/2 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2009, 2011, Oracle and/or its affiliates. 
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
 mthatte     06/18/09 - xmlnamespaces stored as namespace, alias pairs
 mthatte     06/18/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPXmlNamespaceNode.java /main/1 2009/06/23 14:09:07 mthatte Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.parser;

public class CEPXmlNamespaceNode implements CEPParseTreeNode
{
	CEPStringTokenNode namespaceURL;
	
	CEPStringTokenNode namespaceAlias;

	/** is this the default namespace declaration */
	boolean isDefault;

	protected int startOffset;
	protected int endOffset;

	public CEPXmlNamespaceNode(CEPStringTokenNode url, CEPStringTokenNode alias)
	{
		this.namespaceURL = url;
		this.namespaceAlias = alias;
		this.isDefault = false;
		this.setStartOffset(url.getStartOffset());
		this.setEndOffset(alias.getEndOffset());
	}

	public CEPXmlNamespaceNode(CEPStringTokenNode url)
	{
		this.namespaceURL = url;
		this.namespaceAlias = null;
		this.isDefault = true;
		this.setStartOffset(url.getStartOffset());
		this.setEndOffset(url.getEndOffset());
	}

	public boolean isDefault()
	{
		return isDefault;
	}

	public CEPStringTokenNode getNamespaceUrl()
	{
		return this.namespaceURL;
	}

	public CEPStringTokenNode getNamespaceAlias()
	{
		return this.namespaceAlias;
	}

	public int getEndOffset()
	{
		return this.endOffset;
	}

	public int getStartOffset()
	{
		return this.startOffset;
	}

	public void setEndOffset(int end)
	{
		this.endOffset = end;
	}

	public void setStartOffset(int start)
	{
		this.startOffset = start;
	}

	/**
	 * returns a namespace declaration that will be appended 
	 * to the xquery block
	 * @return namespace declaration as expected by Xquery block
	 */
	public String getDeclareNSString()
	{
		if (isDefault)
			return " declare default element namespace "
			    + this.namespaceURL.getValue();
		else
			return " declare namespace " + this.namespaceAlias.getValue() + "=\""
			    + this.namespaceURL.getValue() + "\"";
	}
}