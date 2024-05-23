/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/XmlTableStreamInterp.java /main/13 2013/10/23 18:48:37 sbishnoi Exp $ */

/* Copyright (c) 2007, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    10/22/13 - bug 17623837
    pkali       04/03/12 - included datatype arg in Attr instance
    udeshmuk    04/01/11 - store name of attr
    mthatte     06/18/09 - query rewrite if xmlnamespaces are defined
    parujain    03/12/09 - make interpreters stateless
    parujain    01/28/09 - transaction mgmt
    hopark      12/03/08 - keep installer in ExecContext
    hopark      11/20/08 - lazy seeding
    hopark      10/09/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    parujain    09/15/08 - multiple schema support
    parujain    09/04/08 - maintain offsets
    parujain    08/26/08 - semantic exception offset
    najain      12/03/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/XmlTableStreamInterp.java /main/13 2013/10/23 18:48:37 sbishnoi Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import java.util.List;

import oracle.cep.install.Install;
import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.UserFunction;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPStreamNode;
import oracle.cep.parser.CEPXmlNamespaceNode;
import oracle.cep.parser.CEPXmlTableNode;
import oracle.cep.parser.CEPXmlTableStreamNode;
import oracle.cep.parser.CEPXmlTableColumnNode;
import oracle.cep.parser.CEPXQryArgExprNode;
import oracle.cep.service.ExecContext;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;

class XmlTableStreamInterp extends NodeInterpreter 
{
  // NOTE: This class should be stateless in order to run DDLS in parallel 
  // Ref bug.8290135  
  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
    throws CEPException 
  {
    CEPXmlTableStreamNode  xmlTableStreamNode;
    CEPXmlTableNode        xmlTableNode;
    CEPStreamNode          streamNode;
    NodeInterpreter        streamInterp;
    
    assert node instanceof CEPXmlTableStreamNode;
    xmlTableStreamNode = (CEPXmlTableStreamNode)node;

    super.interpretNode(node, ctx);

    // First process the base stream
    streamNode   = xmlTableStreamNode.getBaseStream();
    streamInterp = InterpreterFactory.getInterpreter(streamNode);
    try {
      streamInterp.interpretNode(streamNode, ctx);
    }
    catch (CEPException e) {
      if (e.getErrorCode() == SemanticError.NOT_A_STREAM_ERROR)
        throw new SemanticException(SemanticError.XMLTABLE_OVER_REL_ERROR, e,
        	                 streamNode.getStartOffset(), streamNode.getEndOffset());
      else
      {
        e.setStartOffset(streamNode.getStartOffset());
        e.setEndOffset(streamNode.getEndOffset());
        throw e;
      }
    }

    String streamName          = streamNode.getName();
    assert streamName         != null;
    SymbolTableEntry entry = null;
    try{
    entry = ctx.getSymbolTable().lookupSource(streamName);
    }catch(CEPException ce)
    {
      ce.setStartOffset(streamNode.getStartOffset());
      ce.setEndOffset(streamNode.getEndOffset());
      throw ce;
    }
    int              baseVarId = entry.getVarId();

    // Now process the XmlTable clause
    xmlTableNode = xmlTableStreamNode.getXmlTableDesc();

    // Add entries in this "parent" symbol table for the inline view
    // corresponding to this xmlTableStream
    String xmlTableStreamAlias = xmlTableStreamNode.getAlias();
    int varId = 0;
    try {
      varId = ctx.getSymbolTable().addInlineSourceEntry(xmlTableStreamAlias, 
					      xmlTableStreamAlias, true);
    }catch(CEPException ce)
    {
      ce.setStartOffset(xmlTableStreamNode.getStartOffset());
      ce.setEndOffset(xmlTableStreamNode.getEndOffset());
      throw ce;
    }
    

    //Process XMLNAMESPACES declaration, if any. This involves prefixing all
    //xquery, xpath strings with "declare namespaces ..." 
    if(xmlTableNode.getNamespaces() != null && xmlTableNode.getNamespaces().size() > 0)
    {
    	String namespaceDeclaration = createDeclareNamespaceString(xmlTableNode.getNamespaces());
    	String xQueryString = xmlTableNode.getXQryStr();
    	if(xQueryString.charAt(0) == '\'' || xQueryString.charAt(0) == '\"')
    	{
    		char start = xQueryString.charAt(0);
    		xQueryString = xQueryString.substring(1);
    		xQueryString = namespaceDeclaration + xQueryString;
    		xQueryString = String.valueOf(start) + xQueryString;
    	}
    	else
    		xQueryString = namespaceDeclaration + xQueryString;
    	xmlTableNode.setXqryStr(xQueryString);
    	
    	//foreach columns xquery expression, append namespace declaration
    	List<CEPXmlTableColumnNode> xqryCols = xmlTableNode.getListCols();
    	for(CEPXmlTableColumnNode xcol : xqryCols)
    	{
    		String xs = xcol.getXQryStr();
    		String newXs = namespaceDeclaration + xs;
    		xcol.setXqryStr(newXs);
    	}
    }

    List<CEPXQryArgExprNode> xqryArgs = xmlTableNode.getXQryArgs();
    Expr params[] = new Expr[xqryArgs.size()];
    String names[] = new String[xqryArgs.size()];
    
    // Processsing of xml query argument might reset transient variable.
    // Preserve the following transient variables:
    // 1. derivedTsSpec
    DerivedTimeSpec derivedTsSpec = ctx.getDerivedTimeSpec();
    
    for (int i = 0; i < xqryArgs.size(); i++)
    {
      CEPExprNode expr = xqryArgs.get(i).getExpr();
      NodeInterpreter interp = InterpreterFactory.getInterpreter(expr);
      interp.interpretNode(expr, ctx);
      params[i] = ctx.getExpr();
      names[i] = xqryArgs.get(i).getName();
    }

    // Set the following saved transient variables:
    // 1. derivedTsSpec
    ctx.setDerivedTimeSpec(derivedTsSpec);
    
    // Create a dummy entry in the symbol table for the xquery string. The name
    // of the entry has to be different from the stream name and the alias name
    String xqryStreamName = streamName.concat("$xmlTable");
    if (xqryStreamName.compareTo(xmlTableStreamAlias) == 0)
      xqryStreamName = streamName.concat("_$xmlTable");
    int dummyVarId = 0;
    try {
      dummyVarId = ctx.getSymbolTable().addInlineSourceEntry(xqryStreamName, 
						   xqryStreamName, true);
      ctx.getSymbolTable().addAttrEntry("Dummy", xqryStreamName, 0, 
	                        Datatype.XMLTYPE, 
			                Datatype.XMLTYPE.getLength());
    }catch(CEPException ce)
    {
      ce.setStartOffset(xmlTableStreamNode.getStartOffset());
      ce.setEndOffset(xmlTableStreamNode.getEndOffset());
      throw ce;
    }

    String name = new String("XMLQUERY");
    Datatype[] dts = new Datatype[1];
    dts[0] = Datatype.CHAR;

    ExecContext ec = ctx.getExecContext();
    String uniqName = ec.getUserFnMgr().getUniqueFunctionName(name, dts);

    UserFunction fn = null;
    try{
      fn = ec.getUserFnMgr().getFunction(uniqName, ec.getDefaultSchema());
      if (fn == null)
      {
        boolean b = ec.getBuiltinFuncInstaller().installFuncs(ec, name, dts);
        if (b) 
        {
          fn = ec.getUserFnMgr().getFunction(uniqName, ec.getDefaultSchema());
        }
      }
    }catch(MetadataException me)
    {
      me.setStartOffset(xmlTableStreamNode.getStartOffset());
      me.setEndOffset(xmlTableStreamNode.getEndOffset());
      throw me;
    }

    XQryFuncExpr fexpr = new XQryFuncExpr(fn.getId(), 
					  xmlTableNode.getXQryStr(), 
					  params, names,
					  fn.getReturnType(), 0, XQryFuncExprKind.EX_EXPR_XMLTBL);
    fexpr.setName(uniqName, false);
    
    // Also add entries for the attributes of this inline view
    List<CEPXmlTableColumnNode> lst = xmlTableNode.getListCols();
    int numCols = lst.size();
    XmlTableColumnNode[] lstCols = new XmlTableColumnNode[lst.size()];

    names = new String[1];
    names[0] = new String(".");
    Expr[] exprs = new Expr[1];
    AttrExpr aExpr = new AttrExpr(new Attr(dummyVarId, 0,"", Datatype.XMLTYPE),
                                      Datatype.XMLTYPE);
    exprs[0] = aExpr;
    for (int i = 0; i < numCols; i++) 
    {
      CEPXmlTableColumnNode xmlCol = lst.get(i);
      try {
      ctx.getSymbolTable().addAttrEntry(xmlCol.getName(), 
			    xmlTableStreamAlias, i,
			    xmlCol.getDatatype(), 
			    xmlCol.getLength());
      }catch(CEPException ce)
      {
        ce.setStartOffset(xmlCol.getStartOffset());
        ce.setEndOffset(xmlCol.getEndOffset());
        throw ce;
      }
      XQryFuncExpr colExpr = 
	  new XQryFuncExpr(fn.getId(), xmlCol.getXQryStr(),
			 exprs, names, 
			 xmlCol.getDatatype(), xmlCol.getLength(),
			 XQryFuncExprKind.EX_EXPR_XQRY);
      fexpr.setName(uniqName, false);
      lstCols[i] = new XmlTableColumnNode(varId, i, colExpr);
    }
    
    XmlTableSpec xmlTableSpec = new XmlTableSpec(baseVarId, dummyVarId, 
						 0, fexpr, 
						 lstCols, ctx.getSymbolTable());
    
    ctx.setXmlTableSpec(xmlTableSpec);
  }
  
  private String createDeclareNamespaceString(List<CEPXmlNamespaceNode> namespaces)
  {
	  StringBuffer declString = new StringBuffer(64);
	  for(CEPXmlNamespaceNode ns : namespaces) 
	  {
			  declString.append(ns.getDeclareNSString() + ";\n");
	  }
	  return declString.toString();
  }
}

