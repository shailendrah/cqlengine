/* $Header: pcbpel/cep/server/src/oracle/cep/semantic/ElementExprInterp.java /main/2 2008/09/17 15:19:47 parujain Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    09/08/08 - support offset
    parujain    05/16/08 - Evalname
    parujain    04/23/08 - element expr interpreter
    parujain    04/23/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/ElementExprInterp.java /main/2 2008/09/17 15:19:47 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPElementExprNode;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.SemanticError;

class ElementExprInterp extends NodeInterpreter
{
  void interpretNode( CEPParseTreeNode node, SemContext ctx) throws CEPException
  {
    assert node instanceof CEPElementExprNode;
    CEPElementExprNode elementNode = (CEPElementExprNode)node;
    
    super.interpretNode(node, ctx);
    
    String name = elementNode.getElementName();
    
    String elementName = new String("XMLELEMENT" + "(");
    
    ElementExpr elementExpr = new ElementExpr();
    
    if(name != null)
    {
      elementName = elementName + name;
      elementExpr.setElementName(name);
    }
    else
    {
      NodeInterpreter interpreter = InterpreterFactory.getInterpreter(elementNode.getElementNameExpr());
      interpreter.interpretNode(elementNode.getElementNameExpr(), ctx);
      Expr nameExpr = ctx.getExpr();
      if(nameExpr.getReturnType() != Datatype.CHAR)
        throw new SemanticException(SemanticError.INVALID_XML_ELEMENT_NAME_EXPR,
        		elementNode.getElementNameExpr().getStartOffset(),
        		elementNode.getElementNameExpr().getEndOffset());
      elementExpr.setElementNameExpr(nameExpr);
      elementName = elementName + nameExpr.getName();
    }
    
    CEPExprNode[] attrNodes = elementNode.getAttrList();
    CEPExprNode[] childNodes = elementNode.getExprList();
    

    if(attrNodes != null)
    {
      Expr[] attrs = null;
      if(attrNodes.length > 0)
      {
        attrs = new Expr[attrNodes.length];
        elementName = elementName + ", " + "XMLATTRIBUTES" + "(";
      }
      for(int i=0; i<attrNodes.length; i++)
      {
        if(i>0)
         elementName = elementName + ", "; 
        NodeInterpreter interp = InterpreterFactory.getInterpreter(attrNodes[i]);
        interp.interpretNode(attrNodes[i], ctx);
        attrs[i] = ctx.getExpr();
        elementName = elementName + attrs[i].getName();
      }
      if(attrNodes.length > 0)
       elementName = elementName + ")";
      elementExpr.setAttrs(attrs);
    }
    
    if(childNodes != null)
    {
      Expr[] child = null;
      if(childNodes.length > 0)
        child = new Expr[childNodes.length];
      for(int j=0; j<childNodes.length; j++)
      {
        elementName = elementName + ", ";
        NodeInterpreter interp = InterpreterFactory.getInterpreter(childNodes[j]);
        interp.interpretNode(childNodes[j], ctx);
        child[j] = ctx.getExpr();
        elementName = elementName + child[j].getName();
      }
      elementExpr.setChildExprs(child);
    }
    
    ctx.setExpr(elementExpr);
   
    elementName = elementName + ")";
    elementExpr.setName(elementName, false);
  }
}