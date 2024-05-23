/* $Header: ConstDoubleInterp.java 30-jan-2008.03:39:05 udeshmuk Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    01/30/08 - Creation
 */

/**
 *  @version $Header: ConstDoubleInterp.java 30-jan-2008.03:39:05 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPDoubleConstExprNode;
import oracle.cep.parser.CEPParseTreeNode;

/**
 * The const double interpreter that is specific to CEPDoubleConstExprNode
 * <p>
 * This is private to semantic analysis module
 */

class ConstDoubleInterp extends NodeInterpreter {
  
  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
  throws CEPException {
    
    CEPDoubleConstExprNode doubleNode;
    ConstDoubleExpr        doubleExpr;
    
    assert node instanceof CEPDoubleConstExprNode;
    doubleNode = (CEPDoubleConstExprNode) node;
    
    super.interpretNode(node, ctx);
    
    doubleExpr = new ConstDoubleExpr(doubleNode.getValue());
    ctx.setExpr(doubleExpr);
  }
}
