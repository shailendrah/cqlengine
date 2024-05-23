/* $Header: ConstIntInterp.java 06-apr-2006.11:26:59 najain Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    The interpreter for the CEPIntConstExprNode parse tree node    

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    najain      04/06/06 - cleanup
    anasrini    02/23/06 - Creation
    anasrini    02/23/06 - Creation
    anasrini    02/23/06 - Creation
 */

/**
 *  @version $Header: ConstIntInterp.java 06-apr-2006.11:26:59 najain Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPIntConstExprNode;
import oracle.cep.exceptions.CEPException;

/**
 * The interpreter that is specific to the CEPIntConstExprNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class ConstIntInterp extends NodeInterpreter {

  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
    throws CEPException {

    CEPIntConstExprNode intNode;
    ConstIntExpr        intExpr;

    assert node instanceof CEPIntConstExprNode;
    intNode = (CEPIntConstExprNode)node;

    super.interpretNode(node, ctx);

    intExpr = new ConstIntExpr(intNode.getValue());
    ctx.setExpr(intExpr);
  }
}
