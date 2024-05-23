/* $Header: ConstBigintInterp.java 06-apr-2006.11:26:59 najain Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    The interpreter for the CEPBigintConstExprNode parse tree node    

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/17/06 - Creation
 */

/**
 *  @version $Header: ConstBigintInterp.java 06-apr-2006.11:26:59 najain Exp $
 *  @author  hopark
 *  @since   1.0
 */

package oracle.cep.semantic;

import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPBigintConstExprNode;
import oracle.cep.exceptions.CEPException;

/**
 * The interpreter that is specific to the CEPBigintConstExprNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class ConstBigintInterp extends NodeInterpreter {

  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
    throws CEPException {

    CEPBigintConstExprNode intNode;
    ConstBigintExpr        intExpr;

    assert node instanceof CEPBigintConstExprNode;
    intNode = (CEPBigintConstExprNode)node;

    super.interpretNode(node, ctx);

    intExpr = new ConstBigintExpr(intNode.getValue());
    ctx.setExpr(intExpr);
  }
}
