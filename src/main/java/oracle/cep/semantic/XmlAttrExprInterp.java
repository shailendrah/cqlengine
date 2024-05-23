/* $Header: pcbpel/cep/server/src/oracle/cep/semantic/XmlAttrExprInterp.java /main/2 2008/09/17 15:19:46 parujain Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    08/26/08 - semantic exception offset
    parujain    05/16/08 - Evalname
    parujain    05/16/08 - AS name not given
    parujain    04/23/08 - xml attribute expr interp
    parujain    04/23/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/XmlAttrExprInterp.java /main/2 2008/09/17 15:19:46 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.parser.CEPAttrNode;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPXmlAttrExprNode;

class XmlAttrExprInterp extends NodeInterpreter {
	
  void interpretNode( CEPParseTreeNode node, SemContext ctx) throws CEPException
  {
    assert node instanceof CEPXmlAttrExprNode;
    CEPXmlAttrExprNode xmlAttrNode = (CEPXmlAttrExprNode)node;
    
    super.interpretNode(node, ctx);
    
    XmlAttrExpr attrExpr = new XmlAttrExpr();
    String tagname = xmlAttrNode.getTagName();
    boolean isTagExpr = xmlAttrNode.isTagNameExpr();
    
    CEPExprNode exprNode = xmlAttrNode.getAttrExpr();
    
    if(tagname == null && (!isTagExpr))
    {
      assert exprNode instanceof CEPAttrNode;
      tagname = ((CEPAttrNode)exprNode).getAttrName();
    }
    if(isTagExpr)
    {
      NodeInterpreter interpreter = InterpreterFactory.getInterpreter(xmlAttrNode.getTagExpr());
      interpreter.interpretNode(xmlAttrNode.getTagExpr(), ctx);
      Expr nameExpr = ctx.getExpr();
      if(nameExpr.getReturnType() != Datatype.CHAR)
        throw new SemanticException(SemanticError.INVALID_XML_ATTRIBUTE_NAME_EXPR,
          xmlAttrNode.getTagExpr().getStartOffset(), xmlAttrNode.getTagExpr().getEndOffset());
      attrExpr.setAttrNameExpr(nameExpr);
    }
    
    String attrName = "";
    
    NodeInterpreter interp = InterpreterFactory.getInterpreter(exprNode);
    interp.interpretNode(exprNode, ctx);
    
    attrExpr.setAttrExpr(ctx.getExpr());
    attrName = attrName + attrExpr.getAttrExpr().getName();
    
    if(!isTagExpr)
      attrExpr.setAttrName(tagname);
    
    if(attrExpr.getAttrName() != null)
    {
      attrName = attrName + " AS " + attrExpr.getAttrName();
    }
    else
    {
      attrName = attrName + "AS" + attrExpr.getAttrNameExpr().getName();
    }
    
    ctx.setExpr(attrExpr);
    attrExpr.setName(attrName, false);
  }
}