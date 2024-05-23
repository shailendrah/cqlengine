/* $Header: XmlForestExprInterp.java 23-may-2008.11:05:48 parujain Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    05/23/08 - xmlforest expr interrp
    parujain    05/23/08 - Creation
 */

/**
 *  @version $Header: XmlForestExprInterp.java 23-may-2008.11:05:48 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPXmlForestExprNode;

class XmlForestExprInterp extends NodeInterpreter
{
  void interpretNode( CEPParseTreeNode node, SemContext ctx) throws CEPException
  {
    assert node instanceof CEPXmlForestExprNode;
    CEPXmlForestExprNode forestNode = (CEPXmlForestExprNode)node;
    
    super.interpretNode(node, ctx);
    
    String name = new String("XMLFOREST" +"(");
    
    XmlForestExpr forestExpr = new XmlForestExpr();
    
    CEPExprNode[] childNodes = forestNode.getForestExprs();
    
    Expr[] child = new Expr[childNodes.length];
    
    for(int i=0; i<childNodes.length; i++)
    {
      NodeInterpreter interp = InterpreterFactory.getInterpreter(childNodes[i]);
      interp.interpretNode(childNodes[i], ctx);
      child[i] = ctx.getExpr();
      name = name + child[i].getName();
    }
    forestExpr.setForestExprs(child);
    ctx.setExpr(forestExpr);
    name = name + ")";
    forestExpr.setName(name, false);
  }
}