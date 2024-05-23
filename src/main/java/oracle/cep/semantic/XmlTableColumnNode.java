/* $Header: XmlTableColumnNode.java 26-dec-2007.09:36:09 mthatte Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 mthatte     12/26/07 - 
 najain      12/05/07 - Creation
 */

/**
 *  @version $Header: XmlTableColumnNode.java 26-dec-2007.09:36:09 mthatte Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

public class XmlTableColumnNode {
	int varId;
	int attrId;
	XQryFuncExpr expr;

	public XmlTableColumnNode(int varId, int attrId, XQryFuncExpr expr) 
	{
	  this.varId = varId;
	  this.attrId = attrId;
	  this.expr = expr;
	}
	
	public int getVarId()
	{
	  return varId;
	}
	
	public int getAttrId()
	{
	  return attrId;
	}
	
	public XQryFuncExpr getXQryFuncExpr()
	{
	  return expr;
	}
}
