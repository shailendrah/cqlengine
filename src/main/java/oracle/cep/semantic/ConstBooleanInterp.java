/* $Header: ConstBooleanInterp.java 14-jan-2008.14:04:47 mthatte Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    mthatte     01/14/08 - 
    najain      01/02/08 - Creation
 */

/**
 *  @version $Header: ConstBooleanInterp.java 14-jan-2008.14:04:47 mthatte Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPBooleanConstExprNode;
import oracle.cep.exceptions.CEPException;

/**
 * The interpreter that is specific to the CEPBooleanConstExprNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class ConstBooleanInterp extends NodeInterpreter {

  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
    throws CEPException {

    CEPBooleanConstExprNode boolNode;
    ConstBooleanExpr        boolExpr;

    assert node instanceof CEPBooleanConstExprNode;
    boolNode = (CEPBooleanConstExprNode)node;

    super.interpretNode(node, ctx);

    boolExpr = new ConstBooleanExpr(boolNode.getValue());
    ctx.setExpr(boolExpr);
  }
}
