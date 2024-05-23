/* $Header: ConstNullInterp.java 17-jan-2008.22:52:09 udeshmuk Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    01/11/08 - Creation
 */

/**
 *  @version $Header: ConstNullInterp.java 17-jan-2008.22:52:09 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPNullConstExprNode;
import oracle.cep.exceptions.CEPException;

/**
 * The interpreter that is specific to the CEPNullConstExprNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class ConstNullInterp extends NodeInterpreter {

  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
    throws CEPException {

    CEPNullConstExprNode nullNode;
    ConstNullExpr        nullExpr;
    
    assert node instanceof CEPNullConstExprNode;
    nullNode = (CEPNullConstExprNode)node;

    super.interpretNode(node, ctx);
    
    nullExpr = new ConstNullExpr();
    nullExpr.setbNull(true);
    ctx.setExpr(nullExpr);
  }
}
