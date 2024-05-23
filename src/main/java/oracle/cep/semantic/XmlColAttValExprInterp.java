/* $Header: XmlColAttValExprInterp.java 29-may-2008.11:16:53 parujain Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    05/29/08 - xmlcolattval expr
    parujain    05/29/08 - Creation
 */

/**
 *  @version $Header: XmlColAttValExprInterp.java 29-may-2008.11:16:53 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPXmlColAttValExprNode;

class XmlColAttValExprInterp extends NodeInterpreter
{
  void interpretNode( CEPParseTreeNode node, SemContext ctx) throws CEPException
  {
    assert node instanceof CEPXmlColAttValExprNode;
    CEPXmlColAttValExprNode colNode = (CEPXmlColAttValExprNode)node;
    
    super.interpretNode(node, ctx);
    
    String name = new String("XMLCOLATTVAL" +"(");
    
    XmlColAttValExpr colExpr = new XmlColAttValExpr();
    
    CEPExprNode[] childNodes = colNode.getColAttExprs();
    
    Expr[] child = new Expr[childNodes.length];
    
    for(int i=0; i<childNodes.length; i++)
    {
      NodeInterpreter interp = InterpreterFactory.getInterpreter(childNodes[i]);
      interp.interpretNode(childNodes[i], ctx);
      child[i] = ctx.getExpr();
      name = name + child[i].getName();
    }
    colExpr.setColAttExprs(child);
    ctx.setExpr(colExpr);
    name = name + ")";
    colExpr.setName(name, false);
  }
}
